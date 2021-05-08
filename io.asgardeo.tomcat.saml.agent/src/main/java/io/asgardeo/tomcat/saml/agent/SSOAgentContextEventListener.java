/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.asgardeo.tomcat.saml.agent;

import io.asgardeo.java.saml.sdk.bean.SSOAgentConfig;
import io.asgardeo.java.saml.sdk.exception.SSOAgentException;
import io.asgardeo.java.saml.sdk.security.SSOAgentX509Credential;
import io.asgardeo.java.saml.sdk.security.SSOAgentX509KeyStoreCredential;
import io.asgardeo.java.saml.sdk.util.SSOAgentConstants;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Context EventListner Class for SAML2 SSO.
 */
public class SSOAgentContextEventListener implements ServletContextListener {

    private static Logger logger = Logger.getLogger(SSOAgentContextEventListener.class.getName());
    Properties properties;
    boolean skipKeystoreConfigs = false;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        properties = new Properties();
        try {

            ServletContext servletContext = servletContextEvent.getServletContext();

            // Load the client property-file, if not specified throw SSOAgentException
            String propertyFileName = servletContext.getInitParameter(SSOAgentConstants.PROPERTY_FILE_PARAMETER_NAME);
            if (StringUtils.isNotBlank(propertyFileName)) {
                properties.load(servletContextEvent.getServletContext().
                        getResourceAsStream("/WEB-INF/classes/" + propertyFileName));
            } else {
                throw new SSOAgentException(SSOAgentConstants.PROPERTY_FILE_PARAMETER_NAME
                        + " context-param is not specified in the web.xml");
            }

            properties.putAll(resolvePropertiesFromEnvironmentVariables(properties));
            SSOAgentConfig config = new SSOAgentConfig();
            config.initConfig(properties);

            // Load the client post binding request page.
            String postBindingRequestPageFileName =
                    servletContext.getInitParameter(SSOAgentConstants.POST_BINDING_REQUEST_PAGE_PARAMETER_NAME);

            InputStream fileInputStream;
            if (StringUtils.isNotBlank(postBindingRequestPageFileName)) {
                fileInputStream = servletContext.getResourceAsStream("/WEB-INF/classes/" +
                        postBindingRequestPageFileName);
                if (fileInputStream != null && StringUtils.isNotBlank(fileInputStream.toString())) {
                    String payload = new Scanner(fileInputStream, "UTF-8").useDelimiter("\\A").next();
                    config.getSAML2().setPostBindingRequestHTMLPayload(payload);
                } else {
                    logger.log(Level.WARNING, "The file " + postBindingRequestPageFileName + " is unreachable or the " +
                            "file content might be empty.");
                }
            } else {
                logger.log(Level.WARNING, SSOAgentConstants.POST_BINDING_REQUEST_PAGE_PARAMETER_NAME
                        + " context-param is not specified in the web.xml");
            }


            // Load the client security certificate, if not specified throw SSOAgentException.
            String certificateFileName = servletContext.getInitParameter(SSOAgentConstants
                    .CERTIFICATE_FILE_PARAMETER_NAME);
            InputStream keyStoreInputStream;
            if (StringUtils.isNotBlank(certificateFileName)) {
                keyStoreInputStream = servletContext.getResourceAsStream("/WEB-INF/classes/"
                        + certificateFileName);
            } else {
                throw new SSOAgentException(SSOAgentConstants.CERTIFICATE_FILE_PARAMETER_NAME
                        + " context-param is not specified in the web.xml");
            }

            String keyStorePassword = getKeystoreConfig(SSOAgentConstants.KEY_STORE_PASSWORD);
            String IdPPublicCertAlias = getKeystoreConfig(SSOAgentConstants.IDP_PUBLIC_CERT_ALIAS);
            String IdPPublicCert = getKeystoreConfig(SSOAgentConstants.IDP_PUBLIC_CERT);
            String PrivateKeyAlias = getKeystoreConfig(SSOAgentConstants.PRIVATE_KEY_ALIAS);
            String privateKeyPassword = getKeystoreConfig(SSOAgentConstants.PRIVATE_KEY_PASSWORD);

            if (!skipKeystoreConfigs) {
                SSOAgentX509Credential credential = new SSOAgentX509KeyStoreCredential(keyStoreInputStream,
                        keyStorePassword.toCharArray(), IdPPublicCertAlias, IdPPublicCert, PrivateKeyAlias,
                        privateKeyPassword.toCharArray());
                config.getSAML2().setSSOAgentX509Credential(credential);
            }

            servletContext.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);

        } catch (IOException | SSOAgentException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private String getKeystoreConfig(String property) {

        if (StringUtils.isNotBlank(properties.getProperty(property))) {
            return properties.getProperty(property);
        }
        skipKeystoreConfigs = true;
        return null;
    }

    private Map<String, String> resolvePropertiesFromEnvironmentVariables(Properties properties) throws SSOAgentException {

        Map<String, String> processedPropertyMap = properties.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey()),
                        String.valueOf(entry.getValue())))
                .filter(entry -> entry.getValue().matches("\\$\\{(.*?)}"))
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(),
                        StringUtils.substringsBetween(entry.getValue(), "${", "}")[0]))
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(),
                        System.getenv(entry.getValue())))
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(),
                        StringUtils.isNotBlank(entry.getValue()) ? entry.getValue() : ""))
                .peek(entry -> {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Inferred value: " + entry.getValue() + " for property: " + entry.getKey()
                                + " from environment.");
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!processedPropertyMap.isEmpty() &&
                StringUtils.isBlank(processedPropertyMap.get(SSOAgentConstants.IDP_PUBLIC_CERT_ALIAS)) &&
                StringUtils.isBlank(processedPropertyMap.get(SSOAgentConstants.IDP_PUBLIC_CERT))) {
            throw new SSOAgentException("Environment variable value was not set for neither `IDP_PUBLIC_CERT` nor " +
                    "`IDP_PUBLIC_CERT_ALIAS`");
        }
        return processedPropertyMap;
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}

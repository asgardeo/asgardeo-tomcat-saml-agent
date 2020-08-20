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

package io.asgardio.tomcat.saml.agent;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.sso.agent.saml.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.saml.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.saml.security.SSOAgentX509Credential;
import org.wso2.carbon.identity.sso.agent.saml.security.SSOAgentX509KeyStoreCredential;
import org.wso2.carbon.identity.sso.agent.saml.util.SSOAgentConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Context EventListner Class for SAML2 SSO.
 */
public class SSOAgentContextEventListener implements ServletContextListener {

    private static Logger logger = Logger.getLogger(SSOAgentContextEventListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        Properties properties = new Properties();
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

            SSOAgentX509Credential credential = new SSOAgentX509KeyStoreCredential(keyStoreInputStream,
                    properties.getProperty(SSOAgentConstants.KEY_STORE_PASSWORD).toCharArray(),
                    properties.getProperty(SSOAgentConstants.IDP_PUBLIC_CERT),
                    properties.getProperty(SSOAgentConstants.PRIVATE_KEY_ALIAS),
                    properties.getProperty(SSOAgentConstants.PRIVATE_KEY_PASSWORD).toCharArray());

            SSOAgentConfig config = new SSOAgentConfig();
            config.initConfig(properties);
            config.getSAML2().setSSOAgentX509Credential(credential);
            servletContext.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);

        } catch (IOException | SSOAgentException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}

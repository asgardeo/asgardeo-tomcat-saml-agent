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

package org.wso2.carbon.identity.sso.agent.saml.artifact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.opensaml.common.SAMLObject;
import org.opensaml.ws.soap.common.SOAPObjectBuilder;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.wso2.carbon.identity.sso.agent.exception.ArtifactResolutionException;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConstants;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This class is used for handling SAML SOAP Binding
 */
public class SAMLSSOSoapMessageService {

    private static final String CONTENT_TYPE = "text/xml; charset=utf-8";
    private static final String MIME_TYPE = "text/xml";
    private static final Log log = LogFactory.getLog(SAMLSSOSoapMessageService.class);

    /**
     * Build a SOAP Message.
     *
     * @param samlMessage SAMLObject.
     * @return Envelope soap envelope
     */
    public Envelope buildSOAPMessage(SAMLObject samlMessage) {

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

        SOAPObjectBuilder<Envelope> envBuilder = (SOAPObjectBuilder<Envelope>) builderFactory.getBuilder(
                Envelope.DEFAULT_ELEMENT_NAME);
        Envelope envelope = envBuilder.buildObject();

        SOAPObjectBuilder<Body> bodyBuilder = (SOAPObjectBuilder<Body>) builderFactory.getBuilder(
                Body.DEFAULT_ELEMENT_NAME);
        Body body = bodyBuilder.buildObject();
        body.getUnknownXMLObjects().add(samlMessage);
        envelope.setBody(body);
        return envelope;
    }

    /**
     * Send SOAP message
     *
     * @param message message that needs to be send
     * @param url     url that the artifact resolve request should be sent
     * @return response of invoking artifact resolve endpoint
     * @throws ArtifactResolutionException
     */
    public String sendSOAP(String message, String url) throws ArtifactResolutionException {

        if (message == null) {
            throw new ArtifactResolutionException("Cannot send null SOAP message.");
        }
        if (url == null) {
            throw new ArtifactResolutionException("Cannot send SOAP message to null URL.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending SOAP message to the URL: " + url);
        }

        StringBuilder soapResponse = new StringBuilder();
        try {
            HttpPost httpPost = new HttpPost(url);
            setRequestProperties(url, message, httpPost);
            HttpClient httpClient = getHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpPost);

            int responseCode = httpResponse.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                throw new ArtifactResolutionException("Problem in communicating with: " + url + ". Received response: "
                        + responseCode);
            } else {
                log.info("Successful response from the URL: " + url);
                soapResponse.append(getResponseBody(httpResponse));
            }
        } catch (UnknownHostException e) {
            throw new ArtifactResolutionException("Unknown targeted host: " + url, e);
        } catch (IOException e) {
            throw new ArtifactResolutionException("Could not open connection with host: " + url, e);
        }
        return soapResponse.toString();
    }

    private void setRequestProperties(String url, String message, HttpPost httpPost) {

        httpPost.addHeader(SSOAgentConstants.SSOAgentConfig.SAML2.CONTENT_TYPE_PARAM_KEY, CONTENT_TYPE);
        httpPost.addHeader(SSOAgentConstants.SSOAgentConfig.SAML2.ACCEPT_PARAM_KEY, CONTENT_TYPE);
        String sbSOAPAction = "\"" + url + "\"";
        httpPost.addHeader(SSOAgentConstants.SSOAgentConfig.SAML2.SOAP_ACTION_PARAM_KEY, sbSOAPAction);
        httpPost.addHeader(SSOAgentConstants.SSOAgentConfig.SAML2.PRAGMA_PARAM_KEY, "no-cache");
        httpPost.addHeader(SSOAgentConstants.SSOAgentConfig.SAML2.CACHE_CONTROL_PARAM_KEY, "no-cache, no-store");

        httpPost.setEntity(new StringEntity(message, ContentType.create(MIME_TYPE, StandardCharsets.UTF_8)));
    }

    private static String getResponseBody(HttpResponse response) throws ArtifactResolutionException {

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody;
        try {
            responseBody = responseHandler.handleResponse(response);
        } catch (IOException e) {
            throw new ArtifactResolutionException("Error when retrieving the HTTP response body.", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Response Body:" + responseBody);
        }
        return responseBody;
    }

    private HttpClient getHttpClient() throws ArtifactResolutionException {

        CloseableHttpClient httpClient = null;
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            httpClient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new ArtifactResolutionException("Error while building trust store.", e);
        } catch (KeyManagementException e) {
            throw new ArtifactResolutionException("Error while building socket factory.", e);
        }

        return httpClient;
    }
}

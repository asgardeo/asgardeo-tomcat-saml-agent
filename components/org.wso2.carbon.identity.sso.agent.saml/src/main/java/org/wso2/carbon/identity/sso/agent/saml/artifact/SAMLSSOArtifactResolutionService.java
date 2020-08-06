/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Artifact;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.opensaml.saml.saml2.core.ArtifactResponse;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.wso2.carbon.identity.sso.agent.saml.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.saml.exception.ArtifactResolutionException;
import org.wso2.carbon.identity.sso.agent.saml.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.saml.security.X509CredentialImpl;
import org.wso2.carbon.identity.sso.agent.saml.util.SSOAgentUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.UUID;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This class is used for handling SAML2 Artifact Binding.
 */
public class SAMLSSOArtifactResolutionService {

    private static final Log log = LogFactory.getLog(SAMLSSOArtifactResolutionService.class);

    private SSOAgentConfig ssoAgentConfig;

    public SAMLSSOArtifactResolutionService(SSOAgentConfig ssoAgentConfig) {

        this.ssoAgentConfig = ssoAgentConfig;
    }

    /**
     * Create a SAML artifactResolveObject based on given samlArt parameter and wrapped into a soapRequest
     * Send the soapRequest to the Artifact Resolve Endpoint
     *
     * @param samlArtReceived SAML Artifact reference needed to get the actual data
     * @return ArtifactResponse
     */
    public ArtifactResponse getSAMLArtifactResponse(String samlArtReceived) throws ArtifactResolutionException {

        validateArtifactResolveConfig();
        ArtifactResolve artifactResolve = generateArtifactResolveReq(samlArtReceived);
        return sendArtifactResolveRequest(artifactResolve);
    }

    /**
     * Create SAML ArtifactResolve Object and sign
     *
     * @param samlArtReceived object reference to actual data
     * @return SAML ArtifactResolve Object
     */
    public ArtifactResolve generateArtifactResolveReq(String samlArtReceived) throws ArtifactResolutionException {

        ArtifactResolve artifactResolve = createArtifactResolveObject(samlArtReceived);
        if (ssoAgentConfig.getSAML2().isEnableArtifactResolveSigning()) {
            if (log.isDebugEnabled()) {
                log.debug("Signing artifact resolve request for the received SAML artifact: " + samlArtReceived);
            }
            artifactResolve = signArtifactResolveReq(artifactResolve);
        }

        if (log.isDebugEnabled()) {
            log.debug("Created Artifact Resolve object: " + artifactResolve);
        }

        return artifactResolve;
    }

    /**
     * Send Artifact Resolve Request to Artifact Resolution Service
     *
     * @param artifactResolve Artifact Resolve Request
     * @return ArtifactResponse
     * @throws ArtifactResolutionException
     */
    public ArtifactResponse sendArtifactResolveRequest(ArtifactResolve artifactResolve)
            throws ArtifactResolutionException {

        SAMLSSOSoapMessageService soapMessageService = new SAMLSSOSoapMessageService();
        Envelope envelope = soapMessageService.buildSOAPMessage(artifactResolve);
        String envelopeElement;
        try {
            envelopeElement = SSOAgentUtils.marshall(envelope);
        } catch (SSOAgentException e) {
            throw new ArtifactResolutionException("Encountered error marshalling SOAP message with artifact " +
                    "resolve, into its DOM representation", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Artifact Resolve Request as a SOAP Message: " + envelopeElement);
        }

        String artifactResponseString = soapMessageService.sendSOAP(envelopeElement,
                ssoAgentConfig.getSAML2().getArtifactResolveURL());
        if (log.isDebugEnabled()) {
            log.debug("Received artifact response string: " + artifactResponseString);
        }
        ArtifactResponse artifactResponse = extractArtifactResponse(artifactResponseString);
        validateArtifactResponse(artifactResolve, artifactResponse);
        return artifactResponse;
    }

    /**
     * Extract Artifact response object from soap message string. Return null if fail.
     *
     * @param artifactResponseString Response string from artifact resolver.
     * @return Extracted artifact response object.
     * @throws ArtifactResolutionException
     */
    public ArtifactResponse extractArtifactResponse(String artifactResponseString)
            throws ArtifactResolutionException {

        ArtifactResponse artifactResponse = null;
        InputStream stream = new ByteArrayInputStream(artifactResponseString.getBytes(StandardCharsets.UTF_8));
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage(new MimeHeaders(), stream);
            SOAPBody soapBody = soapMessage.getSOAPBody();
            Iterator iterator = soapBody.getChildElements();

            while (iterator.hasNext()) {
                SOAPBodyElement artifactResponseElement = (SOAPBodyElement) iterator.next();

                if (StringUtils.equals(SAMLConstants.SAML20P_NS, artifactResponseElement.getNamespaceURI()) &&
                        StringUtils.equals(ArtifactResponse.DEFAULT_ELEMENT_LOCAL_NAME,
                                artifactResponseElement.getLocalName())) {

                    DOMSource source = new DOMSource(artifactResponseElement);
                    StringWriter stringResult = new StringWriter();
                    TransformerFactory.newInstance().newTransformer().transform(
                            source, new StreamResult(stringResult));
                    artifactResponse = (ArtifactResponse) SSOAgentUtils.unmarshall(stringResult.toString());
                    if (log.isDebugEnabled()) {
                        log.debug("Extracted Artifact Response: " + stringResult);
                    }
                } else {
                    throw new ArtifactResolutionException("Received invalid artifact response with nameSpaceURI: " +
                            artifactResponseElement.getNamespaceURI() + " and localName: " +
                            artifactResponseElement.getLocalName());
                }
            }
        } catch (SOAPException | IOException | TransformerException e) {
            throw new ArtifactResolutionException("Didn't receive valid artifact response.", e);
        } catch (SSOAgentException e) {
            throw new ArtifactResolutionException("Encountered error unmarshalling response into SAML2 object", e);
        }
        return artifactResponse;
    }

    private void validateArtifactResponse(ArtifactResolve artifactResolve, ArtifactResponse artifactResponse)
            throws ArtifactResolutionException {

        if (artifactResponse == null) {
            throw new ArtifactResolutionException("Received artifact response message was null.");
        }

        String artifactResolveId = artifactResolve.getID();
        String artifactResponseInResponseTo = artifactResponse.getInResponseTo();
        if (!artifactResolveId.equals(artifactResponseInResponseTo)) {
            throw new ArtifactResolutionException("Artifact resolve ID: " + artifactResolveId + " is not equal to " +
                    "artifact response InResponseTo : " + artifactResponseInResponseTo);
        }

        String artifactResponseStatus = artifactResponse.getStatus().getStatusCode().getValue();
        if (!StatusCode.SUCCESS.equals(artifactResponseStatus)) {
            throw new ArtifactResolutionException("Unsuccessful artifact response with status: " +
                    artifactResponseStatus);
        }

        SAMLObject message = artifactResponse.getMessage();
        if (message == null) {
            throw new ArtifactResolutionException("No SAML response embedded into the artifact response.");
        }
    }

    private void validateArtifactResolveConfig() throws ArtifactResolutionException {

        if (StringUtils.isEmpty(ssoAgentConfig.getSAML2().getArtifactResolveURL())) {
            throw new ArtifactResolutionException("Artifact Resolve Url is not configured.");
        }
        if (StringUtils.isEmpty(ssoAgentConfig.getSAML2().getSPEntityId())) {
            throw new ArtifactResolutionException("Artifact Resolve Issuer is not configured.");
        }
    }

    private ArtifactResolve createArtifactResolveObject(String samlArtReceived) {

        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        SAMLObjectBuilder<ArtifactResolve> artifactResolveBuilder =
                (SAMLObjectBuilder<ArtifactResolve>) builderFactory.getBuilder(ArtifactResolve.DEFAULT_ELEMENT_NAME);
        ArtifactResolve artifactResolve = artifactResolveBuilder.buildObject();
        artifactResolve.setVersion(SAMLVersion.VERSION_20);
        artifactResolve.setID(UUID.randomUUID().toString());
        artifactResolve.setIssueInstant(new DateTime());

        SAMLObjectBuilder<Artifact> artifactBuilder =
                (SAMLObjectBuilder<Artifact>) builderFactory.getBuilder(Artifact.DEFAULT_ELEMENT_NAME);
        Artifact artifact = artifactBuilder.buildObject();
        artifact.setArtifact(samlArtReceived);

        SAMLObjectBuilder<Issuer> issuerBuilder = (SAMLObjectBuilder<Issuer>) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(ssoAgentConfig.getSAML2().getSPEntityId());

        artifactResolve.setIssuer(issuer);
        artifactResolve.setArtifact(artifact);

        return artifactResolve;
    }

    private ArtifactResolve signArtifactResolveReq(ArtifactResolve artifactResolve)
            throws ArtifactResolutionException {

        try {
            return SSOAgentUtils.setSignature(artifactResolve, XMLSignature.ALGO_ID_SIGNATURE_RSA,
                    new X509CredentialImpl(ssoAgentConfig.getSAML2().getSSOAgentX509Credential()));
        } catch (SSOAgentException e) {
            throw new ArtifactResolutionException("Error in signing the Artifact Resolve request", e);
        }
    }
}

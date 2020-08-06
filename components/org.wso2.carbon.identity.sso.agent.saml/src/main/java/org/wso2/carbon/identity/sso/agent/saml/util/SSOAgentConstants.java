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

package org.wso2.carbon.identity.sso.agent.saml.util;

public class SSOAgentConstants {

    public static final String KEY_STORE_PASSWORD = "KeyStorePassword";
    public static final String IDP_PUBLIC_CERT = "IdPPublicCertAlias";
    public static final String PRIVATE_KEY_ALIAS = "PrivateKeyAlias";
    public static final String PRIVATE_KEY_PASSWORD = "PrivateKeyPassword";

    public static final String LOGGER_NAME = "org.wso2.carbon.identity.sso.agent";

    public static final String SESSION_BEAN_NAME = "org.wso2.carbon.identity.sso.agent.LoggedInSessionBean";
    public static final String CONFIG_BEAN_NAME = "org.wso2.carbon.identity.sso.agent.SSOAgentConfig";

    public static final String SHOULD_GO_TO_WELCOME_PAGE = "shouldGoToWelcomePage";
    public static final String PROPERTY_FILE_PARAMETER_NAME = "property-file";
    public static final String CERTIFICATE_FILE_PARAMETER_NAME = "certificate-file";

    public static class SAML2SSO {

        private SAML2SSO() {}

        public static final String HTTP_POST_PARAM_SAML2_AUTH_REQ = "SAMLRequest";
        public static final String HTTP_POST_PARAM_SAML2_RESP = "SAMLResponse";
        public static final String SAML2_ARTIFACT_RESP = "SAMLart";
        public static final String SUCCESS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success";
    }

    private SSOAgentConstants() {}

    public static class SSOAgentConfig {

        public static final String ENABLE_SAML2_SSO_LOGIN = "EnableSAML2SSOLogin";
        public static final String SAML2_SSO_URL = "SAML2SSOURL";
        public static final String SKIP_URIS = "SkipURIs";
        public static final String QUERY_PARAMS = "QueryParams";
        public static final String PASSWORD_FILEPATH = "/conf/password_temp.txt";

        private SSOAgentConfig() {}

        public static class SAML2 {

            public static final String HTTP_BINDING = "SAML2.HTTPBinding";
            public static final String SP_ENTITY_ID = "SAML2.SPEntityId";
            public static final String ACS_URL = "SAML2.AssertionConsumerURL";
            public static final String IDP_ENTITY_ID = "SAML2.IdPEntityId";
            public static final String IDP_URL = "SAML2.IdPURL";
            public static final String ATTRIBUTE_CONSUMING_SERVICE_INDEX =
                    "SAML2.AttributeConsumingServiceIndex";
            public static final String ENABLE_SLO = "SAML2.EnableSLO";
            public static final String SLO_URL = "SAML2.SLOURL";
            public static final String ENABLE_ASSERTION_SIGNING =
                    "SAML2.EnableAssertionSigning";
            public static final String ENABLE_ASSERTION_ENCRYPTION =
                    "SAML2.EnableAssertionEncryption";
            public static final String ENABLE_RESPONSE_SIGNING =
                    "SAML2.EnableResponseSigning";
            public static final String ENABLE_REQUEST_SIGNING = "SAML2.EnableRequestSigning";
            public static final String IS_PASSIVE_AUTHN = "SAML2.IsPassiveAuthn";
            public static final String IS_FORCE_AUTHN = "SAML2.IsForceAuthn";
            public static final String RELAY_STATE = "SAML2.RelayState";
            public static final String POST_BINDING_REQUEST_HTML_PAYLOAD =
                    "SAML2.PostBindingRequestHTMLPayload";
            public static final String POST_BINDING_REQUEST_HTML_FILE_PATH =
                    "SAML2.PostBindingRequestHTMLFilePath";
            public static final String SIGNATURE_VALIDATOR = "SAML2.SignatureValidatorImplClass";
            public static final String TIME_STAMP_SKEW = "SAML2.TimestampSkew";

            public static final String SOAP_ACTION_PARAM_KEY = "SOAPAction";
            public static final String ACCEPT_PARAM_KEY = "Accept";
            public static final String CONTENT_TYPE_PARAM_KEY = "Content-Type";
            public static final String PRAGMA_PARAM_KEY = "Pragma";
            public static final String CACHE_CONTROL_PARAM_KEY = "Cache-Control";
            public static final String IS_ARTIFACT_RESOLVE_REQ_SIGNED = "ISArtifactResolveReqSigned";
            public static final String ARTIFACT_RESOLVE_URL = "SAML2.ArtifactResolveUrl";
            public static final String ENABLE_ARTIFACT_RESOLVE_SIGNING = "SAML2.EnableArtifactResolveSigning";

            private SAML2() {}
        }
    }

}

#!/bin/bash

# ------------------------------------------------------------------------
#
# Copyright 2020 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

docker container run --rm --name tomcat-sample -itdp 8080:8080 tomcat-saml-agent

docker container run --rm -itdp 9443:9443 --link tomcat-sample wso2/wso2is

sleep 100s

curl -kv --location --request POST 'https://localhost:9443/services/IdentityApplicationManagementService' \
--header 'SOAPAction: createApplication' \
--header 'Content-Type: text/xml' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://model.common.application.identity.carbon.wso2.org/xsd" xmlns:xsd2="http://script.model.common.application.identity.carbon.wso2.org/xsd">    <soapenv:Header/>    <soapenv:Body>       <xsd:createApplication>          <xsd:serviceProvider>             <xsd1:applicationName>tomcat-saml-agent-sample</xsd1:applicationName>             <xsd1:inboundAuthenticationConfig>             <xsd1:inboundAuthenticationRequestConfigs>               <xsd1:inboundAuthKey>sample-app</xsd1:inboundAuthKey>               <xsd1:inboundAuthType>samlsso</xsd1:inboundAuthType>             </xsd1:inboundAuthenticationRequestConfigs>             </xsd1:inboundAuthenticationConfig>          </xsd:serviceProvider>       </xsd:createApplication>    </soapenv:Body> </soapenv:Envelope>'

curl -kv --location --request POST 'https://localhost:9443/services/IdentitySAMLSSOConfigService' \
--header 'SOAPAction: addRPServiceProvider' \
--header 'Content-Type: text/xml' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://dto.saml.sso.identity.carbon.wso2.org/xsd">    <soapenv:Header/>    <soapenv:Body>       <xsd:addRPServiceProvider>          <xsd:spDto>             <xsd1:assertionConsumerUrl>http://localhost.com:8080/sample-app/home.jsp</xsd1:assertionConsumerUrl>             <xsd1:assertionConsumerUrls>http://localhost.com:8080/sample-app/home.jsp</xsd1:assertionConsumerUrls>             <xsd1:assertionEncryptionAlgorithmURI>http://www.w3.org/2001/04/xmllenc#aes256-cbc</xsd1:assertionEncryptionAlgorithmURI>             <xsd1:assertionQueryRequestProfileEnabled>false</xsd1:assertionQueryRequestProfileEnabled>             <xsd1:attributeConsumingServiceIndex>1223160754</xsd1:attributeConsumingServiceIndex>             <xsd1:certAlias>wso2carbon</xsd1:certAlias>             <xsd1:defaultAssertionConsumerUrl>http://localhost.com:8080/sample-app/home.jsp</xsd1:defaultAssertionConsumerUrl>             <xsd1:digestAlgorithmURI>http://www.w3.org/2000/09/xmldsig#sha1</xsd1:digestAlgorithmURI>             <xsd1:doEnableEncryptedAssertion>false</xsd1:doEnableEncryptedAssertion>             <xsd1:doSignAssertions>true</xsd1:doSignAssertions>             <xsd1:doSignResponse>true</xsd1:doSignResponse>             <xsd1:doSingleLogout>true</xsd1:doSingleLogout>             <xsd1:doValidateSignatureInRequests>true</xsd1:doValidateSignatureInRequests>             <xsd1:enableAttributeProfile>true</xsd1:enableAttributeProfile>             <xsd1:enableAttributesByDefault>true</xsd1:enableAttributesByDefault>             <xsd1:idPInitSLOEnabled>false</xsd1:idPInitSLOEnabled>             <xsd1:idPInitSSOEnabled>false</xsd1:idPInitSSOEnabled>             <xsd1:idpEntityIDAlias>localhost</xsd1:idpEntityIDAlias>             <xsd1:issuer>sample-app</xsd1:issuer>             <xsd1:keyEncryptionAlgorithmURI>http://www.w3.org/2001/04/xmllenc#rsa-oaep-mgf1p</xsd1:keyEncryptionAlgorithmURI>             <xsd1:nameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</xsd1:nameIDFormat>             <xsd1:signingAlgorithmURI>http://www.w3.org/2000/09/xmldsig#rsa-sha1</xsd1:signingAlgorithmURI>          </xsd:spDto>       </xsd:addRPServiceProvider>    </soapenv:Body> </soapenv:Envelope>'

curl -kv --location --request POST 'https://localhost:9443/services/IdentityApplicationManagementService' \
--header 'SOAPAction: updateApplication' \
--header 'Content-Type: text/xml' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://model.common.application.identity.carbon.wso2.org/xsd" xmlns:xsd2="http://script.model.common.application.identity.carbon.wso2.org/xsd">    <soapenv:Header/>    <soapenv:Body>       <xsd:updateApplication>          <xsd:serviceProvider>             <xsd1:applicationID>2</xsd1:applicationID>             <xsd1:applicationName>tomcat-saml-agent-sample</xsd1:applicationName>             <xsd1:inboundAuthenticationConfig>                 <xsd1:inboundAuthenticationRequestConfigs>                     <xsd1:inboundAuthKey>sample-app</xsd1:inboundAuthKey>                     <xsd1:inboundAuthType>samlsso</xsd1:inboundAuthType>                 </xsd1:inboundAuthenticationRequestConfigs>             </xsd1:inboundAuthenticationConfig>          </xsd:serviceProvider>       </xsd:updateApplication>    </soapenv:Body> </soapenv:Envelope>'
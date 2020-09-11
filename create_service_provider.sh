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

# This script will create a SAML Service Provider in WSO2 Identity Server to represent the sample app.

service_provider_name=tomcat-saml-agent-sample
wso2_is_url='https://localhost:9443'
sample_app_url='http://localhost.com:8080/sample-app'
application_mgt_service_url="$wso2_is_url/services/IdentityApplicationManagementService"
saml_config_service_url="$wso2_is_url/services/IdentitySAMLSSOConfigService"
health_check_api_url="$wso2_is_url/api/health-check/v1.0/health"
application_id_xpath_filter="//*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='getApplicationResponse']/*[local-name()='return']/*[local-name()='applicationID']/text()"
create_sp_request_body='<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://model.common.application.identity.carbon.wso2.org/xsd" xmlns:xsd2="http://script.model.common.application.identity.carbon.wso2.org/xsd">    <soapenv:Header/>    <soapenv:Body>       <xsd:createApplication>          <xsd:serviceProvider>             <xsd1:applicationName>'"$service_provider_name"'</xsd1:applicationName>             <xsd1:inboundAuthenticationConfig>             <xsd1:inboundAuthenticationRequestConfigs>               <xsd1:inboundAuthKey>sample-app</xsd1:inboundAuthKey>               <xsd1:inboundAuthType>samlsso</xsd1:inboundAuthType>             </xsd1:inboundAuthenticationRequestConfigs>             </xsd1:inboundAuthenticationConfig>          </xsd:serviceProvider>       </xsd:createApplication>    </soapenv:Body> </soapenv:Envelope>'
retrieve_sp_id_request_body='<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://model.common.application.identity.carbon.wso2.org/xsd" xmlns:xsd2="http://script.model.common.application.identity.carbon.wso2.org/xsd">    <soapenv:Header/>    <soapenv:Body>       <xsd:getApplication>         <xsd1:applicationName>'"$service_provider_name"'</xsd1:applicationName>    </xsd:getApplication>    </soapenv:Body> </soapenv:Envelope>'
create_saml_config_request_body='<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://dto.saml.sso.identity.carbon.wso2.org/xsd">    <soapenv:Header/>    <soapenv:Body>       <xsd:addRPServiceProvider>          <xsd:spDto>             <xsd1:assertionConsumerUrl>'"$sample_app_url"'/home.jsp</xsd1:assertionConsumerUrl>             <xsd1:assertionConsumerUrls>'"$sample_app_url"'/home.jsp</xsd1:assertionConsumerUrls>             <xsd1:assertionEncryptionAlgorithmURI>http://www.w3.org/2001/04/xmllenc#aes256-cbc</xsd1:assertionEncryptionAlgorithmURI>             <xsd1:assertionQueryRequestProfileEnabled>false</xsd1:assertionQueryRequestProfileEnabled>             <xsd1:attributeConsumingServiceIndex>1223160754</xsd1:attributeConsumingServiceIndex>             <xsd1:certAlias>wso2carbon</xsd1:certAlias>             <xsd1:defaultAssertionConsumerUrl>'"$sample_app_url"'/home.jsp</xsd1:defaultAssertionConsumerUrl>             <xsd1:digestAlgorithmURI>http://www.w3.org/2000/09/xmldsig#sha1</xsd1:digestAlgorithmURI>             <xsd1:doEnableEncryptedAssertion>false</xsd1:doEnableEncryptedAssertion>             <xsd1:doSignAssertions>true</xsd1:doSignAssertions>             <xsd1:doSignResponse>true</xsd1:doSignResponse>             <xsd1:doSingleLogout>true</xsd1:doSingleLogout>             <xsd1:doValidateSignatureInRequests>true</xsd1:doValidateSignatureInRequests>             <xsd1:enableAttributeProfile>true</xsd1:enableAttributeProfile>             <xsd1:enableAttributesByDefault>true</xsd1:enableAttributesByDefault>             <xsd1:idPInitSLOEnabled>false</xsd1:idPInitSLOEnabled>             <xsd1:idPInitSSOEnabled>false</xsd1:idPInitSSOEnabled>             <xsd1:idpEntityIDAlias>localhost</xsd1:idpEntityIDAlias>             <xsd1:issuer>sample-app</xsd1:issuer>             <xsd1:keyEncryptionAlgorithmURI>http://www.w3.org/2001/04/xmllenc#rsa-oaep-mgf1p</xsd1:keyEncryptionAlgorithmURI>             <xsd1:nameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</xsd1:nameIDFormat>             <xsd1:signingAlgorithmURI>http://www.w3.org/2000/09/xmldsig#rsa-sha1</xsd1:signingAlgorithmURI>          </xsd:spDto>       </xsd:addRPServiceProvider>    </soapenv:Body> </soapenv:Envelope>'

while ! curl -sk "$health_check_api_url" > /dev/null
do
  sleep 5s
  echo "Waiting for the WSO2 Identity Server to start..."
done

echo "Creating Service Provider: $service_provider_name"
curl -sku admin:admin --location --request POST "$application_mgt_service_url" \
--header 'SOAPAction: createApplication' \
--header 'Content-Type: text/xml' \
--data-raw "$create_sp_request_body" \
> /dev/null

echo "Retrieveing Service Provider ID"
service_provider_id=$(curl -sku admin:admin --location --request POST "$application_mgt_service_url" \
--header 'SOAPAction: getApplication' \
--header 'Content-Type: text/xml' \
--data-raw "$retrieve_sp_id_request_body" \
| xmllint --format --xpath "$application_id_xpath_filter" -)
echo "Service Provider ID: $service_provider_id"

echo "Creating SAML SSO Config"
curl -sku admin:admin --location --request POST "$saml_config_service_url" \
--header 'SOAPAction: addRPServiceProvider' \
--header 'Content-Type: text/xml' \
--data-raw "$create_saml_config_request_body" \
> /dev/null

echo "Associating SAML config to the Service Provider: $service_provider_name"
assign_saml_config_to_sp_request_body='<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://org.apache.axis2/xsd" xmlns:xsd1="http://model.common.application.identity.carbon.wso2.org/xsd" xmlns:xsd2="http://script.model.common.application.identity.carbon.wso2.org/xsd">    <soapenv:Header/>    <soapenv:Body>       <xsd:updateApplication>          <xsd:serviceProvider>             <xsd1:applicationID>'"$service_provider_id"'</xsd1:applicationID>             <xsd1:applicationName>'"$service_provider_name"'</xsd1:applicationName>             <xsd1:inboundAuthenticationConfig>                 <xsd1:inboundAuthenticationRequestConfigs>                     <xsd1:inboundAuthKey>sample-app</xsd1:inboundAuthKey>                     <xsd1:inboundAuthType>samlsso</xsd1:inboundAuthType>                 </xsd1:inboundAuthenticationRequestConfigs>             </xsd1:inboundAuthenticationConfig>          </xsd:serviceProvider>       </xsd:updateApplication>    </soapenv:Body> </soapenv:Envelope>'
curl -sku admin:admin --location --request POST "$application_mgt_service_url" \
--header 'SOAPAction: updateApplication' \
--header 'Content-Type: text/xml' \
--data-raw "$assign_saml_config_to_sp_request_body" \
> /dev/null

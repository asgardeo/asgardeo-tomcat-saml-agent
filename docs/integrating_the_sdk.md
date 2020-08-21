# Asgardio Tomcat SAML Agent

## Table of Contents
- [Trying out the sample](../README.md/#trying-out-the-sample)
  * [Prerequisites](../README.md/#prerequisites)
  * [Running the Sample](../README.md/#running-the-sample)
- [How it works](../docs/how_it_works.md/#how-it-works)
  * [Classify secure resources, unsecured resources](../docs/how_it_works.md/#classify-secure-resources--unsecured-resources)
  * [Trigger authentication](../docs/how_it_works.md/#trigger-authentication)
  * [Retrieve user attributes](../docs/how_it_works.md/#retrieve-user-attributes)
  * [Trigger logout](../docs/how_it_works.md/#trigger-logout)
- [Integrating SAML into your Java application](#integrating-saml-into-your-java-application)
  * [Getting Started](#getting-started)
  * [Configuring the web app](#configuring-the-web-app)
  * [Retrieving User Attributes](#retrieving-user-attributes)
- [Installing the SDK](../README.md/#installing-the-sdk)
  * [Github](../README.md/#github)
  * [Building from the source](../README.md/#building-from-the-source)
  * [Maven](../README.md/#maven)
- [Contributing](../README.md/#contributing)
  * [Reporting Issues](../README.md/#reporting-issues)
- [Versioning](../README.md/#versioning)
- [License](../README.md/#license)

## Integrating SAML into your Java application

### Getting Started

These instructions will guide you on integrating SAML into your Java application with the Asgardio SAML SDK for Java.
This allows the developers to turn a Java application into a SAML SP (Service Provider) that can be connected to an IdP
 (Identity Provider) which can support the following main features among many others.

- Single Sign-On (SSO) and Single Log-Out (SLO) (SP-Initiated and IdP-Initiated).
- Assertion and nameID encryption.
- Assertion signatures.
- Message signatures: AuthNRequest, LogoutRequest, LogoutResponses.
- Enable an Assertion Consumer Service endpoint.
- Enable a Single Logout Service endpoint.
- Publish the SP metadata.

A sample application boilerplate is included in https://github.com/asgardio/asgardio-tomcat-saml-agent/tree/master/resources/SampleApp-boilerplate which we would use for the following section. 

The structure of the web app boilerplate would be as follows:

[![INSERT YOUR GRAPHIC HERE](https://miro.medium.com/max/1400/1*M9-eI8gcUugJD_6u7PXN1Q.png)]()

### Configuring the web app

1. Starting with the pom.xml, the following dependencies should be added for the webApp to be using the SAML SDK.
Install it as a maven dependency:
      ```
      <dependency>
          <groupId>org.wso2.carbon.identity.agent.sso.java</groupId>
          <artifactId>org.wso2.carbon.identity.sso.tomcat.server</artifactId>
          <version>5.5.5</version>
      </dependency>
      ```
   The SDK is hosted at the WSO2 Internal Repository. Point to the repository as follows:


      ```
      <repositories>
          <repository>
              <id>wso2.releases</id>
              <name>WSO2 internal Repository</name>
              <url>http://maven.wso2.org/nexus/content/repositories/releases/</url>
              <releases>
                  <enabled>true</enabled>
                  <updatePolicy>daily</updatePolicy>
                  <checksumPolicy>ignore</checksumPolicy>
              </releases>
          </repository>
      </repositories>
      ```
2. Next, the webapp itself has two pages, index.html and home.jsp, and a web.xml file.
The index.html contains a login button which we would use to forward the user to the secured page.

      `<form method="post" action="home.jsp">`

3. Now we need to update the form action to trigger a SAML authentication request as follows,

      `<form method="post" action="samlsso?SAML2.HTTPBinding=HTTP-POST">`

4. The home.jsp page is a page which we want to secure i.e. in case there are no active sessions, the http://localhost:8080/SampleApp/home.jsp should not be accessible. In the sampleApp we are using, if there is no active session in
 place, we would redirect the user for authentication. In the home.jsp, there is a logout link which will be used to
  create a SLO request.

      `<a href="logout?SAML2.HTTPBinding=HTTP-POST">Logout</a>`

5. Before the web.xml configurations, we will look at adding the resources files.
   In the sampleApp, create a file named sampleApp.properties in the resources directory. The sampleApp.properties file
 contains properties similar to the following:

      ```
      EnableSAML2SSOLogin=true

      #Url to do send SAML2 SSO AuthnRequest
      SAML2SSOURL=samlsso

      #URIs to skip SSOAgentFilter; comma separated values
      SkipURIs=

      IndexPage=/SampleApp/index.html

      #A unique identifier for this SAML 2.0 Service Provider application
      SAML2.SPEntityId=SampleApp

      #The URL of the SAML 2.0 Assertion Consumer
      SAML2.AssertionConsumerURL=http://localhost:8080/SampleApp/home.jsp

      #A unique identifier for this SAML 2.0 Service Provider application
      SAML2.IdPEntityId=localhost

      #The URL of the SAML 2.0 Identity Provider
      SAML2.IdPURL=https://localhost:9443/samlsso

      #Specify if SingleLogout is enabled/disabled
      SAML2.EnableSLO=true

      #This is the URL that is used for SLO
      SAML2.SLOURL=logout

      #Specify if SAMLResponse element is signed
      SAML2.EnableResponseSigning=true

      #Specify if SAMLAssertion element is signed
      SAML2.EnableAssertionSigning=true

      #Specify if SAMLAssertion element is encrypted
      SAML2.EnableAssertionEncryption=false

      #Specify if AuthnRequests and LogoutRequests should be signed
      SAML2.EnableRequestSigning=true

      #Specify if SAML request is a passive
      SAML2.IsPassiveAuthn=false

      #Password of the KeyStore
      KeyStorePassword=wso2carbon

      #Alias of the IdP's public certificate
      IdPPublicCertAlias=wso2carbon

      #Alias of the SP's private key 
      PrivateKeyAlias=wso2carbon

      #Private key password to retrieve the private key used to sign 
      #AuthnRequest and LogoutRequest messages
      PrivateKeyPassword=wso2carbon
      ```
6. These properties are required for the SAML SDK to communicate with the WSO2 Identity Server. Next, copy a keystore
 file to the resources directory. In our example,

    properties file: **“sampleApp.properties”**

    keystore file: **“wso2carbon.jks”**

   You may need to update the properties file entries to reflect the properties of your keystore. For simplicity, we are
    using the wso2carbon.jks keystore file of the WSO2 Identity Server which resides in “<IS_HOME>/repository/resources
    /security/” directory.

7. Finally, copy and paste the following web.xml configurations to the WEB-INF/web.xml file. Make sure that you update
 param-values of the context-params,

    `<param-name>property-file</param-name>`

    `<param-name>certificate-file</param-name>`

   to match yours.

      ```
      <?xml version="1.0" encoding="UTF-8"?>

      <!--
        ~ Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
        ~
        ~ WSO2 Inc. licenses this file to you under the Apache License,
        ~ Version 2.0 (the "License"); you may not use this file except
        ~ in compliance with the License.
        ~ You may obtain a copy of the License at
        ~
        ~ http://www.apache.org/licenses/LICENSE-2.0
        ~
        ~ Unless required by applicable law or agreed to in writing,
        ~ software distributed under the License is distributed on an
        ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        ~ KIND, either express or implied.  See the License for the
        ~ specific language governing permissions and limitations
        ~ under the License.
        -->

      <web-app id="SampleApp" version="2.5"
              xmlns="http://java.sun.com/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

          <display-name>SampleApp</display-name>

          <filter>
              <filter-name>SAML2SSOAgentFilter</filter-name>
              <filter-class>io.asgardio.tomcat.saml.agent.SAML2SSOAgentFilter</filter-class>
          </filter>
          <filter-mapping>
              <filter-name>SAML2SSOAgentFilter</filter-name>
              <url-pattern>*.jsp</url-pattern>
          </filter-mapping>
          <filter-mapping>
              <filter-name>SAML2SSOAgentFilter</filter-name>
              <url-pattern>*.html</url-pattern>
          </filter-mapping>
          <filter-mapping>
              <filter-name>SAML2SSOAgentFilter</filter-name>
              <url-pattern>/samlsso</url-pattern>
          </filter-mapping>
          <filter-mapping>
              <filter-name>SAML2SSOAgentFilter</filter-name>
              <url-pattern>/logout</url-pattern>
          </filter-mapping>

          <listener>
              <listener-class>io.asgardio.tomcat.saml.agent.SSOAgentContextEventListener</listener-class>
          </listener>
          <context-param>
              <param-name>property-file</param-name>
              <param-value>sampleApp.properties</param-value>
          </context-param>
          <context-param>
              <param-name>certificate-file</param-name>
              <param-value>wso2carbon.jks</param-value>
          </context-param>
      </web-app>

      ```
### Retrieving User Attributes

1. The web app needs to be configured to read the attributes sent from the Identity Server upon successful
 authentication. In the SampleApp, we would customize the home.jsp file as follows to retrieve the user attributes.
 
      ```
      .
      .
      </head>
      <%
          LoggedInSessionBean sessionBean = (LoggedInSessionBean) session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
          String subjectId = sessionBean.getSAML2SSO().getSubjectId();
          Map<String, String> saml2SSOAttributes = sessionBean.getSAML2SSO().getSubjectAttributes();
      %>
      <body>
      .
      .
      ```
2. Then, we would use the `saml2SSOAttributes` in the **home.jsp** to display the user attributes via a table:

      ```
      <table>
         <%
             if (saml2SSOAttributes != null) {
                 for (Map.Entry<String, String> entry : saml2SSOAttributes.entrySet()) {
         %>
         <tr>
             <td><%=entry.getKey()%>
             </td>
             <td><%=entry.getValue()%>
             </td>
         </tr>
         <%
                 }
             }
         %>
      </table>
      ```
   After the above configurations, your app would be able to try out the authentication, logout and attribute 
   retrieval flows with SAML.
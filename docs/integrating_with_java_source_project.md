## Integrating SAML into your Java Source Project

  * [Prerequisites](#prerequisites-1)
  * [Configuring the sample](#configuring-the-sample)
  * [Enable login](#enable-login)
  * [Enable logout](#enable-logout)
  * [Retrieving user attributes](#retrieving-user-attributes)
  
Throughout this section we will refer to the Identity Server installation directory as IS_HOME.

### Prerequisites
1. [Maven](https://maven.apache.org/download.cgi) 3.6.x or higher

These instructions will guide you on integrating SAML into your Java application with the Asgardeo SAML SDK for Java.
This allows an application (i.e. Service Provider) to connect with an IDP using the Asgardeo SAML SDK for Java.

The SDK supports the following features.

- Single Sign-On (SSO) and Single Log-Out (SLO) (SP-Initiated and IdP-Initiated).
- Assertion and nameID encryption.
- Assertion signatures.
- Message signatures: AuthNRequest, LogoutRequest, LogoutResponses.
- Enable an Assertion Consumer Service endpoint.
- Enable a Single Logout Service endpoint.
- Publish the SP metadata.

A sample application is included in 
https://github.com/asgardeo/asgardeo-tomcat-saml-agent/tree/master/io.asgardeo.tomcat.saml.agent.sample
which we would use for the following section. 
Here, we are using the sample as a reference only, we can follow the same approach to build our own app as well.
The structure of the sample would be as follows:

[![INSERT YOUR GRAPHIC HERE](https://miro.medium.com/max/1400/1*M9-eI8gcUugJD_6u7PXN1Q.png)]()

### Configuring the sample

1. Starting with the pom.xml, the following dependencies should be added for the webApp to be using the SAML SDK.
      ```
      <dependency>
          <groupId>io.asgardeo.tomcat.saml.agent/groupId>
          <artifactId>io.asgardeo.tomcat.saml.agent</artifactId>
          <version>0.1.36</version>
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

2. Before the web.xml configurations, we will look at adding the resources files.
   In the sample-app, create a file named sample-app.properties in the `<APP_HOME>/resources` directory. The 
   sample-app.properties file contains properties similar to the following:

      ```
      #Enable SAML Single Sign On Login
      EnableSAML2SSOLogin=true

      #Url to do send SAML2 SSO AuthnRequest
      SAML2SSOURL=samlsso

      #URIs to skip SSOAgentFilter; comma separated values
      SkipURIs=

      IndexPage=/sample-app/index.html

      #A unique identifier for this SAML 2.0 Service Provider application
      SAML2.SPEntityId=sample-app

      #The URL of the SAML 2.0 Assertion Consumer
      SAML2.AssertionConsumerURL=http://localhost.com:8080/sample-app/home.jsp

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
3. Next, generate keystore file and copy it to the `APP_HOME/resources`resources directory.
   For simplicity, we are using the wso2carbon.jks keystore file of the WSO2 Identity Server which resides in 
    `<IS_HOME>/repository/resources/security/` directory.
   You may need to update the following properties of `<APP_HOME/WEB-INF/web.xml` file
   if you are using different keystore than `wso2carbon.jks`. 

4. Finally, copy and paste the following configurations to the `<APP_HOME/WEB-INF/web.xml` file. 

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

      <web-app id="io.asgardeo.tomcat.saml.agent.sample" version="2.5"
              xmlns="http://java.sun.com/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

          <display-name>io.asgardeo.tomcat.saml.agent.sample</display-name>

          <filter>
              <filter-name>SAML2SSOAgentFilter</filter-name>
              <filter-class>io.asgardeo.tomcat.saml.agent.SAML2SSOAgentFilter</filter-class>
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
              <listener-class>io.asgardeo.tomcat.saml.agent.SSOAgentContextEventListener</listener-class>
          </listener>
          <context-param>
              <param-name>property-file</param-name>
              <param-value>sample-app.properties</param-value>
          </context-param>
          <context-param>
              <param-name>certificate-file</param-name>
              <param-value>wso2carbon.jks</param-value>
          </context-param>
      </web-app>

      ```
### Enable login    
1. Next, the webapp itself has two pages, index.html and home.jsp, and a web.xml file.
The index.html contains a login button which we would use to forward the user to the secured page.
      ```
        <form action="home.jsp" method="post">
            <div class="element-padding">
                <input style="height: 30px; width: 60px" type="submit" value="log in">
            </div>
        </form>
      ```

2. Now we need to update the form action to trigger a SAML authentication request as follows,
      ```
        <form action="samlsso?SAML2.HTTPBinding=HTTP-POST" method="post">
            <div class="element-padding">
                <input style="height: 30px; width: 60px" type="submit" value="log in">
            </div>
        </form>
      ```

### Enable logout
1. The home.jsp page is a page which we want to secure i.e. in case there are no active sessions, the http://localhost.com:8080/sample-app/home.jsp should not be accessible. In the sample we are using, if there is no active session in
 place, we would redirect the user for authentication. In the home.jsp, there is a logout link which will be used to
  create a SLO request.

      `<a href="logout?SAML2.HTTPBinding=HTTP-POST">Logout</a>`

### Retrieving user attributes

1. The web app needs to be configured to read the attributes sent from the Identity Server upon successful
 authentication. In the sample-app, we would customize the home.jsp file as follows to retrieve the user attributes.
 
 First, we would need the following imports to be added to the home.jsp file.
 
       <%@ page import="io.asgardeo.java.saml.sdk.util.SSOAgentConstants" %>
       <%@ page import="io.asgardeo.java.saml.sdk.bean.LoggedInSessionBean" %>
       <%@ page import="io.asgardeo.java.saml.sdk.bean.LoggedInSessionBean.SAML2SSO" %>
       <%@ page import="java.util.Map" %>
       
Next, by adding the following snippets, we would be able to retrieve the user claims as provided by the Identity Provider.
 
      <%
        // Retrieve the session bean.
        LoggedInSessionBean sessionBean = (LoggedInSessionBean) session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
    
        // SAML response
        SAML2SSO samlResponse = sessionBean.getSAML2SSO();
    
        // Autheticated username
        String subjectId = samlResponse.getSubjectId();
    
        // Authenticated user's attributes
        Map<String, String> saml2SSOAttributes = samlResponse.getSubjectAttributes();
       %>
      
2. Then, we would use the `saml2SSOAttributes` in the **<APP_HOME>/home.jsp** to display the user attributes via a 
table:

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
   
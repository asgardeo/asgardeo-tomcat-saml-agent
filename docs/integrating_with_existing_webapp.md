## Integrating SAML into your Existing Webapp 

### Getting started
Throughout this section we will refer to the existing web application as sample-app.
#### Prerequisites
1. [Apache Tomcat](http://tomcat.apache.org/tomcat-8.5-doc/) 8.x or higher

These instructions will guide you on integrating SAML into your web application with the Asgardio SAML SDK for Java.
This allows an application (i.e. Service Provider) to connect with an IDP using the Asgardio SAML SDK for Java.

### Configuring the web app

The structure of the sample-app we are configuring would be as follows:

<img width="326" alt="structure" src="https://user-images.githubusercontent.com/25428696/91556626-aa2db880-e950-11ea-9203-72d2a68d4148.png">

1. Download the `lib.zip` from the [latest release](https://github.com/asgardio/asgardio-tomcat-saml-agent/releases/latest).
1. Extract the downloaded `lib.zip` file to the `<APP_HOME>/WEB-INF` directory. (If you already have a `lib` folder in
 your web app, merge the content of the downloaded `lib.zip` file into the existing `lib` folder.)

2. Before the web.xml configurations, we will look at adding the resources files.
   In the sample-app, create a file named sample-app.properties in the `<APP_HOME>/WEB-INF/classes` directory. The 
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
3. Next, generate keystore file and copy it to the `APP_HOME/WEB-INF/classes` directory.
   For simplicity, we are using the wso2carbon.jks keystore file of the WSO2 Identity Server which resides in 
    `<IS_HOME>/repository/resources/security/` directory.
   You may need to update the following properties of `<APP_HOME/WEB-INF/web.xml` file
   if you are using different keystore than `wso2carbon.jks`. 

4. Finally, copy and paste the following configurations to the `<APP_HOME>/WEB-INF/web.xml` file. 

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

      <web-app id="io.asgardio.tomcat.saml.agent.sample" version="2.5"
              xmlns="http://java.sun.com/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

          <display-name>io.asgardio.tomcat.saml.agent.sample</display-name>

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
 
       <%@ page import="io.asgardio.java.saml.sdk.util.SSOAgentConstants" %>
       <%@ page import="io.asgardio.java.saml.sdk.bean.LoggedInSessionBean" %>
       <%@ page import="io.asgardio.java.saml.sdk.bean.LoggedInSessionBean.SAML2SSO" %>
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
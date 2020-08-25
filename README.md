# Asgardio Tomcat SAML Agent

## Table of Contents
- [Trying out the sample](#trying-out-the-sample)
  * [Prerequisites](#prerequisites)
  * [Running the Sample](#running-the-sample)
    + [Configuring Identity Server](#configuring-identity-server)
    + [Configuring The Webapp](#configuring-the-webapp)
- [How it works](#how-it-works)
  * [Classify secure resources, unsecured resources](#classify-secure-resources--unsecured-resources)
  * [Trigger authentication](#trigger-authentication)
  * [Retrieve user attributes](#retrieve-user-attributes)
  * [Trigger logout](#trigger-logout)
- [Integrating SAML into your Java application](#integrating-saml-into-your-java-application)
  * [Getting Started](#getting-started)
    + [Prerequisites](#prerequisites-1)
  * [Configuring the web app](#configuring-the-web-app)
  * [Enable Login](#enable-login)
  * [Enable Logout](#enable-logout)
  * [Retrieving User Attributes](#retrieving-user-attributes)
- [Building from the source](#building-from-the-source)
- [Contributing](#contributing)
  * [Reporting Issues](#reporting-issues)
- [License](#license)

Throughout this guide we will refer to Identity Server installation directory as IS_HOME and SampleApp installation directory as APP_HOME.

## Trying out the sample

### Prerequisites
1. WSO2 Identity Server and it's [prerequisites](https://is.docs.wso2.com/en/next/setup/installing-the-product/).
2. [Apache Tomcat](http://tomcat.apache.org/tomcat-8.5-doc/) 8.x or higher

### Running the Sample
Follow the steps below to tryout SAML based SSO authentication, SLO and attribute retrieval functionalities.

#### Configuring Identity Server
1. Start the WSO2 IS. 
2. Access WSO2 IS management console from https://localhost:9443/carbon/ and create a service provider.
   ![Management Console](https://user-images.githubusercontent.com/15249242/91068131-6fc2d380-e651-11ea-9d0a-d58c825bbb68.png)
   i. Navigate to the `Service Providers` tab listed under the `Identity` section in the management console and click `Add`.<br/>
   ii. Provide a name for the Service Provider (ex:- sampleApp) and click `Register`. Now you will be redirected to the `Edit Service Provider` page.<br/>
   iii. Expand the  `Inbound Authentication Configuration` section and click `Configure` under the `SAML2 Web SSO Configuration` section.<br/>
   iv. Provide the following values for the respective fields and click `Update` while keeping other default settings as it is.
   
       Issuer - SampleApp  
       Assertion Consumer URLs - http://localhost.com:8080/SampleApp/home.jsp 
       Enable Attribute Profile - True
       Include Attributes in the Response Always - True

   v. Next, in the `Edit Service Provider` page, expand the 
   [Claim Configuration](https://is.docs.wso2.com/en/latest/learn/configuring-claims-for-a-service-provider/#configuring-claims-for-a-service-provider) 
   section. In this configuration, Set the following config and add the claims you 
   need to retrieve (ex: http://wso2.org/claims/lastname) from the web app.
   
       Select Claim mapping Dialect - Use Local Claim Dialect
       
   See the example claim config below.
   ![Claim Config](https://user-images.githubusercontent.com/15249242/90488235-38d45580-e159-11ea-8beb-52d6b5c35034.png)
   
   vi. Click `Update` to save.

#### Configuring The Webapp
1. Download the `SampleApp.war` from the [latest release](https://github.com/asgardio/asgardio-tomcat-saml-agent/releases/latest).
2. Deploy the application, `SampleApp.war` using Apache Tomcat.
3. Add the entry `127.0.0.1   localhost.com` to the `/etc/hosts` file of your machine to configure the hostname.
4. Try out the application by accessing the `http://localhost.com:8080/SampleApp/index.html`.

![Recordit GIF](http://g.recordit.co/IvrtWnDnZ8.gif)    

## How it works
### Classify secure resources, unsecured resources
In the SampleApp sample, we have two pages. A landing page (`index.html`) which we have not secured, and another 
page (`home.jsp`) which we have secured.

`IndexPage` property of the sampleApp.properties file in the `<APP_HOME>/WEB-INF/classes` directory is used to define 
the landing page of the webapp which is also considered as an unsecured page.
Also the same page is used as the page that user get redirected once the logout is done.
Here we have set `<APP_HOME>/index.html` as the value of `IndexPage` property.
    IndexPage=/SampleApp/index.html

By default, all other pages considered as secured pages. Hence `home.jsp` will be secured without any other configurations.

### Trigger authentication
In the `<APP_HOME>/index.html` page, we have added the action for the login button to trigger a SAML authentication:
```
<form action="samlsso?SAML2.HTTPBinding=HTTP-POST" method="post">
    <div class="element-padding">
        <input style="height: 30px; width: 60px" type="submit" value="log in">
    </div>
</form>
```

The button click would trigger an authentication request, and redirect the user to the IdP authentication page.
Upon successful authentication, the user would be redirected to the `<APP_HOME>/home.jsp` page.

### Retrieve user attributes

In the `<APP_HOME>/home.jsp` file, we have added the following to get the user subject value and the user attributes 
referring the SDK API.

```
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
```

### Trigger logout
In the `<APP_HOME>/home.jsp` file, we have added the following to trigger a SLO flow:

``<a href="logout?SAML2.HTTPBinding=HTTP-POST">Logout</a>``

Clicking on the logout link would trigger the SLO flow engaging the same filter mentioned above. The user would be
 redirected to the page configured via the `IndexPage` property previously discussed.
 
## Integrating SAML into your Java application
### Getting Started
#### Prerequisites
1. [Maven](https://maven.apache.org/download.cgi) 3.6.x or higher

These instructions will guide you on integrating SAML into your Java application with the Asgardio SAML SDK for Java.
This allows an application (i.e. Service Provider) to connect with an IDP using the Asgardio SAML SDK for Java.

The SDK supports the following features.

- Single Sign-On (SSO) and Single Log-Out (SLO) (SP-Initiated and IdP-Initiated).
- Assertion and nameID encryption.
- Assertion signatures.
- Message signatures: AuthNRequest, LogoutRequest, LogoutResponses.
- Enable an Assertion Consumer Service endpoint.
- Enable a Single Logout Service endpoint.
- Publish the SP metadata.

A sample application boilerplate is included in 
https://github.com/asgardio/asgardio-tomcat-saml-agent/tree/master/resources/SampleApp-boilerplate 
which we would use for the following section. 
Here, we are using the boilerplate app as a reference only, we can follow the same approach to build our own app as well.
The structure of the web app boilerplate would be as follows:

[![INSERT YOUR GRAPHIC HERE](https://miro.medium.com/max/1400/1*M9-eI8gcUugJD_6u7PXN1Q.png)]()

### Configuring the web app

1. Starting with the pom.xml, the following dependencies should be added for the webApp to be using the SAML SDK.
Install it as a maven dependency:
      ```
      <dependency>
          <groupId>io.asgardio.tomcat.saml.agent/groupId>
          <artifactId>io.asgardio.tomcat.saml.agent</artifactId>
          <version>0.1.0</version>
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
   In the sampleApp, create a file named sampleApp.properties in the `<APP_HOME>/resources` directory. The 
   sampleApp.properties file contains properties similar to the following:

      ```
      #Enable SAML Single Sign On Login
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
### Enable Login    
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

### Enable Logout
1. The home.jsp page is a page which we want to secure i.e. in case there are no active sessions, the http://localhost:8080/SampleApp/home.jsp should not be accessible. In the sampleApp we are using, if there is no active session in
 place, we would redirect the user for authentication. In the home.jsp, there is a logout link which will be used to
  create a SLO request.

      `<a href="logout?SAML2.HTTPBinding=HTTP-POST">Logout</a>`

### Retrieving User Attributes

1. The web app needs to be configured to read the attributes sent from the Identity Server upon successful
 authentication. In the SampleApp, we would customize the home.jsp file as follows to retrieve the user attributes.
 
      ```
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
      ```
      
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

## Building from the source
If you want to build **tomcat-saml-agent** from the source code:

1. Install Java 8
2. Install Apache Maven 3.x.x (https://maven.apache.org/download.cgi#)
3. Get a clone or download the source from this repository (https://github.com/asgardio/asgardio-tomcat-saml-agent.git)
4. Run the Maven command ``mvn clean install`` from the ``asgardio-tomcat-saml-agent`` directory.

## Contributing

Please read [Contributing to the Code Base](http://wso2.github.io/) for details on our code of conduct, and the
 process for submitting pull requests to us.
 
### Reporting Issues
We encourage you to report issues, improvements, and feature requests creating [git Issues](https://github.com/wso2-extensions/identity-samples-dotnet/issues).

Important: And please be advised that security issues must be reported to security@wso2.com, not as GitHub issues, 
in order to reach the proper audience. We strongly advise following the WSO2 Security Vulnerability Reporting Guidelines
 when reporting the security issues.

## License
This project is licensed under the Apache License 2.0. See the [LICENSE
](LICENSE) file for details.


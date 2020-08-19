# WSO2 SAML SDK for Java

The WSO2 SAML SDK for Java enables software developers to integrate SAML based SSO authentication with Java Web
 applications. The SDK is built on top of the OpenSAML library which allows Java developers to develop cross-domain
  single sign-on and federated access control solutions with minimum hassle.

## Trying out the sample

### Prerequisites
1. WSO2 Identity Server and it's [prerequisites](https://is.docs.wso2.com/en/next/setup/installing-the-product/).

A sample app for demonstrating SAML based SSO authentication, SLO and attribute retrieval is hosted at:
https://github.com/wso2-extensions/identity-agent-sso/tree/master/resources/SampleApp

You can download the pre-built SampleApp.war from https://github.com/wso2-extensions/identity-agent-sso/releases/latest

### Running the SampleApp

In order to check SSO using SAML2, please follow these steps 
 
1. Start the WSO2 IS. 
2. Access WSO2 IS management console and create a service provider (ex:- sampleApp)
   
   For the service provider, configure SAML2 Web SSO under Inbound Authentication Configuration. In this configuration,
   use following parameters and options,
     
       Issuer - SampleApp  
       Assertion Consumer URLs - http://localhost:8080/SampleApp/home.jsp 
       Enable Attribute Profile - True
       Include Attributes in the Response Always - True


   Keep other default settings as it is and save the configuration.
   
   Next, expand the [Claim Configuration](https://is.docs.wso2.com/en/latest/learn/configuring-claims-for-a-service-provider/#configuring-claims-for-a-service-provider) section. In this configuration, Set the following config and add the claims you 
   need to retrieve (ex: http://wso2.org/claims/lastname) from the web app.
   
       Select Claim mapping Dialect - Use Local Claim Dialect
       
   See the example claim config below.
   ![Claim Config](https://user-images.githubusercontent.com/15249242/90488235-38d45580-e159-11ea-8beb-52d6b5c35034.png)

       
3. Deploy the application, `SampleApp.war` using Apache Tomcat.
4. Try out the application by accessing the `http://localhost:8080/SampleApp/index.html`.

   By default, the application runs on url `http://localhost:8080/SampleApp/`
 

![Recordit GIF](http://g.recordit.co/IvrtWnDnZ8.gif)

**NOTE:** Some browsers do not support cookie creation for naked host names (ex:- localhost). SSO functionality
 require cookies 
in the browser. 

In that case, use `localhost.com` host name for the sample application. You will require to edit the SampleApp
.properties file in <TOMCAT_HOME>/webapps/SampleApp/WEB-INF/classes directory and set the following:

`SAML2.AssertionConsumerURL=http://localhost.com:8080/SampleApp/home.jsp`

and update the Assertion Consumer URLs in the Identity Server Service Provider configurations accordingly.

You will also require to add this entry 
to `hosts` file. For windows this file locations is at `<Windows-Installation-Drive>\Windows\System32\drivers\etc
\hosts`.
For Linux/Mac OS, this file location is at `/etc/hosts`.

## How it works

In the SampleApp sample, we have two pages. A landing page (index.html) which we have not secured, and a secondary
 page (home.jsp) which we have secured.

In the SampleApp.properties file in the `identity-agent-sso/resources/SampleApp/src/main/resources` directory, we
 have set the /SampleApp/index.html as the index page via the following property:

    IndexPage=/SampleApp/index.html

Hence, the sso agent regards the index.html page as the landing page and would be added to the skipURIs. Then, the
 index page would be regarded as a page that is not secured.

When a SLO sequence is initiated, the sso agent would redirect the user to this exact page which is configured via
 the `IndexPage` property.

In the **index.html** page of the SampleApp, we have added the action for the login button to trigger a SAML
 authentication:

`<form method="post" action="samlsso?SAML2.HTTPBinding=HTTP-POST">`

This would engage the SAML2SSOAgentFilter which is specified in the **web.xml** file in the `identity-agent-sso
/resources/SampleApp/src/main/webapp/WEB-INF` directory, and redirect the user to the IdP authentication page.

Upon successful authentication, the user would be redirected to the **home.jsp** page.
In the **home.jsp** file, we have added the following to trigger a SLO flow:

``<a href="logout?SAML2.HTTPBinding=HTTP-POST">Logout</a>``

Clicking on the logout link would trigger the SLO flow engaging the same filter mentioned above. The user would be
 redirected to the page configured via the `IndexPage` property previously discussed.


## Integrating SAML into your Java application

### Getting Started

These instructions will guide you on integrating SAML into your Java application with the WSO2 SAML SDK.
This allows the developers to turn a Java application into a SP (Service Provider) that can be connected to an IdP
 (Identity Provider) which can support the following main features among many others.

- Single Sign-On (SSO) and Single Log-Out (SLO) (SP-Initiated and IdP-Initiated).
- Assertion and nameID encryption.
- Assertion signatures.
- Message signatures: AuthNRequest, LogoutRequest, LogoutResponses.
- Enable an Assertion Consumer Service endpoint.
- Enable a Single Logout Service endpoint.
- Publish the SP metadata.

A sample application boilerplate is included in https://github.com/wso2-extensions/identity-agent-sso/tree/master/resources/SampleApp-boilerplate which we would use for the following section. 

The structure of the web app boilerplate would be as follows:

[![INSERT YOUR GRAPHIC HERE](https://miro.medium.com/max/1400/1*M9-eI8gcUugJD_6u7PXN1Q.png)]()

### Configuring the web app

Starting with the pom.xml, the following dependencies should be added for the webApp to be using the SAML SDK.

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
Next, the webapp itself has two pages, index.html and home.jsp, and a web.xml file.

The index.html contains a login button which we would use to forward the user to the secured page.

`<form method="post" action="home.jsp">`

Now we need to update the form action to trigger a SAML authentication request as follows,

`<form method="post" action="samlsso?SAML2.HTTPBinding=HTTP-POST">`

The home.jsp page is a page which we want to secure i.e. in case there are no active sessions, the http://localhost:8080/SampleApp/home.jsp should not be accessible. In the sampleApp we are using, if there is no active session in
 place, we would redirect the user for authentication. In the home.jsp, there is a logout link which will be used to
  create a SLO request.

`<a href="logout?SAML2.HTTPBinding=HTTP-POST">Logout</a>`

Before the web.xml configurations, we will look at adding the resources files.

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
These properties are required for the SAML SDK to communicate with the WSO2 Identity Server. Next, copy a keystore
 file to the resources directory. In our example,

properties file: **“sampleApp.properties”**

keystore file: **“wso2carbon.jks”**

You may need to update the properties file entries to reflect the properties of your keystore. For simplicity, we are
 using the wso2carbon.jks keystore file of the WSO2 Identity Server which resides in “<IS_HOME>/repository/resources
 /security/” directory.

Finally, copy and paste the following web.xml configurations to the WEB-INF/web.xml file. Make sure that you update
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
        <filter-class>org.wso2.carbon.identity.sso.tomcat.server.SAML2SSOAgentFilter</filter-class>
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
        <listener-class>org.wso2.carbon.identity.sso.tomcat.server.SSOAgentContextEventListener</listener-class>
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

The web app needs to be configured to read the attributes sent from the Identity Server upon successful
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
Then, we would use the `saml2SSOAttributes` in the **home.jsp** to display the user attributes via a table:

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
 
## Installing the SDK

### Github
The SDK is hosted on github. You can download it from:
- Latest release: https://github.com/wso2-extensions/identity-agent-sso/releases/latest
- Master repo: https://github.com/wso2-extensions/identity-agent-sso/tree/master/

### Building from the source

If you want to build **identity-agent-sso** from the source code:

1. Install Java 8
2. Install Apache Maven 3.x.x (https://maven.apache.org/download.cgi#)
3. Get a clone or download the source from this repository (https://github.com/wso2-extensions/identity-agent-sso.git)
4. Run the Maven command ``mvn clean install`` from the ``identity-agent-sso`` directory.

### Maven

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

## Contributing

Please read [Contributing to the Code Base](http://wso2.github.io/) for details on our code of conduct, and the
 process for submitting pull requests to us.
 
### Reporting Issues
We encourage you to report issues, improvements, and feature requests creating [git Issues](https://github.com/wso2-extensions/identity-samples-dotnet/issues).

Important: And please be advised that security issues must be reported to security@wso2.com, not as GitHub issues, 
in order to reach the proper audience. We strongly advise following the WSO2 Security Vulnerability Reporting Guidelines
 when reporting the security issues.

## Versioning

For the versions available, see the [tags on this repository](https://github.com/wso2-extensions/identity-agent-sso/tags). 

## Authors


See also the list of [contributors](https://github.com/wso2-extensions/identity-agent-sso/contributors) who
 participated in this project.

## License

This project is licensed under the Apache License 2.0 under which WSO2 Carbon is distributed. See the [LICENSE
](LICENSE) file for details.


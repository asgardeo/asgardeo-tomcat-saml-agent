# Asgardio Tomcat SAML Agent

## Table of Contents
- [Trying out the sample](#trying-out-the-sample)
  * [Prerequisites](#prerequisites)
  * [Running the Sample](#running-the-sample)
- [How it works](./docs/how_it_works.md/#how-it-works)
  * [Classify secure resources, unsecured resources](./docs/how_it_works.md/#classify-secure-resources--unsecured-resources)
  * [Trigger authentication](./docs/how_it_works.md/#trigger-authentication)
  * [Retrieve user attributes](./docs/how_it_works.md/#retrieve-user-attributes)
  * [Trigger logout](./docs/how_it_works.md/#trigger-logout)
- [Integrating SAML into your Java application](./docs/integrating_the_sdk.md/#integrating-saml-into-your-java-application)
  * [Getting Started](./docs/integrating_the_sdk.md/#getting-started)
  * [Configuring the web app](./docs/integrating_the_sdk.md/#configuring-the-web-app)
  * [Retrieving User Attributes](./docs/integrating_the_sdk.md/#retrieving-user-attributes)
- [Installing the SDK](#installing-the-sdk)
  * [Github](#github)
  * [Building from the source](#building-from-the-source)
  * [Maven](#maven)
- [Contributing](#contributing)
  * [Reporting Issues](#reporting-issues)
- [Versioning](#versioning)
- [License](#license)
## Trying out the sample

### Prerequisites
1. WSO2 Identity Server and it's [prerequisites](https://is.docs.wso2.com/en/next/setup/installing-the-product/).

A sample app for demonstrating SAML based SSO authentication, SLO and attribute retrieval is hosted at:

### Running the Sample

Follow below steps to tryout SAML based SSO authentication, SLO and attribute retrieval functionalities.
 
1. Download the `SampleApp.war` from the [latest release].(https://github.com/asgardio/asgardio-tomcat-saml-agent/releases/latest)
2. Deploy the application, `SampleApp.war` using Apache Tomcat.
3. Start the WSO2 IS. 
4. Access WSO2 IS management console and create a service provider (ex:- sampleApp)
   
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

       
5. Try out the application by accessing the `http://localhost:8080/SampleApp/index.html`.

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
## Installing the SDK

### Github
The SDK is hosted on github. You can download it from:
- Latest release: https://github.com/asgardio/asgardio-tomcat-saml-agent/releases/latest
- Master repo: https://github.com/asgardio/asgardio-tomcat-saml-agent/tree/master/

### Building from the source

If you want to build **tomcat-saml-agent** from the source code:

1. Install Java 8
2. Install Apache Maven 3.x.x (https://maven.apache.org/download.cgi#)
3. Get a clone or download the source from this repository (https://github.com/asgardio/asgardio-tomcat-saml-agent.git)
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

For the versions available, see the [tags on this repository](https://github.com/asgardio/asgardio-tomcat-saml-agent/tags). 

## License

This project is licensed under the Apache License 2.0 under which WSO2 Carbon is distributed. See the [LICENSE
](LICENSE) file for details.


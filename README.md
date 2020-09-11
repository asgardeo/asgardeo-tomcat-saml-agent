# Asgardio Tomcat SAML Agent

[![Build Status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fwso2.org%2Fjenkins%2Fjob%2Fasgardio%2Fjob%2Fasgardio-tomcat-saml-agent%2F&style=flat)](https://wso2.org/jenkins/job/asgardio/job/asgardio-tomcat-saml-agent/) [![Stackoverflow](https://img.shields.io/badge/Ask%20for%20help%20on-Stackoverflow-orange)](https://stackoverflow.com/questions/tagged/wso2is)
[![Join the chat at https://join.slack.com/t/wso2is/shared_invite/enQtNzk0MTI1OTg5NjM1LTllODZiMTYzMmY0YzljYjdhZGExZWVkZDUxOWVjZDJkZGIzNTE1NDllYWFhM2MyOGFjMDlkYzJjODJhOWQ4YjE](https://img.shields.io/badge/Join%20us%20on-Slack-%23e01563.svg)](https://join.slack.com/t/wso2is/shared_invite/enQtNzk0MTI1OTg5NjM1LTllODZiMTYzMmY0YzljYjdhZGExZWVkZDUxOWVjZDJkZGIzNTE1NDllYWFhM2MyOGFjMDlkYzJjODJhOWQ4YjE)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/wso2/product-is/blob/master/LICENSE)
[![Twitter](https://img.shields.io/twitter/follow/wso2.svg?style=social&label=Follow)](https://twitter.com/intent/follow?screen_name=wso2)
---

Asgardio Tomcat SAML Agent enables you to add SAML based login, logout to your Apache Tomcat web apps in simple manner.

- [Getting Started](#getting-started)
- [How it works](#how-it-works)
- [Integrating Asgardio Tomcat SAML Agent](#integrating-asgardio-tomcat-saml-agent)
  * [To your existing webapp](#to-your-existing-webapp)
  * [To your Java source project](#to-your-java-source-project)
- [Building from the source](#building-from-the-source)
- [Contributing](#contributing)
  * [Reporting issues](#reporting-issues)
- [License](#license)

## Getting started

You can experience the capabilities of Asgardio Tomcat SAML Agent by following this small guide which contains main sections listed below.

  * [Prerequisites](#prerequisites)
  * [Configuring the sample](#configuring-the-sample)
  * [Configuring Identity Server](#configuring-identity-server)
  * [Running the sample](#running-the-sample)

### Prerequisites
1. [Docker](https://docs.docker.com/get-docker/)

### Configuring the sample
1. Open a terminal window inside a preferred directory on your machine.
2. Execute the following command to start the sample Docker container.
   ```
   $ docker container run --rm --name tomcat-saml-agent-sample -itdp 8080:8080 asgardio/tomcat-saml-agent-sample
   ```
3. Add the following entry to the `/etc/hosts` file of your machine to configure the hostname.
   ```
   127.0.0.1 localhost.com
   ```

### Configuring Identity Server
Here we are using WSO2 Identity Server as the SAML Identity Provider. The sample can be configured with any other preferred Identity Provider as well.
1. Open a terminal window inside a preferred directory on your machine.
2. Execute the following command to start the WSO2 IS Docker container.
   ```
   $ docker container run --rm -itdp 9443:9443 --link tomcat-saml-agent-sample wso2/wso2is
   ```
3. Execute the following command to download the shell script to create a Service Provider in WSO2 IS.
   ```
   $ curl -o create_service_provider.sh https://raw.githubusercontent.com/asgardio/asgardio-tomcat-saml-agent/master/create_service_provider.sh
   ```
4. Execute the following command to assign executable permission to the `create_service_provider.sh` script.
   ```
   $ chmod +x create_service_provider.sh
   ```
5. Execute the following command to create a SAML Service Provider in WSO2 IS.
   ```
   $ sh create_service_provider.sh
   ```

### Running the sample
Try out the application by accessing the URL http://localhost.com:8080/sample-app/index.html in your web browser.

![Recordit GIF](http://g.recordit.co/IvrtWnDnZ8.gif)    

## How it works

This section explains detailed walkthrough on how key aspects handled in the Asgardio Tomcat SAML Agent.

  * [Classify secure resources, unsecured resources](#classify-secure-resources-unsecured-resources)
  * [Trigger authentication](#trigger-authentication)
  * [Retrieve user attributes](#retrieve-user-attributes)
  * [Trigger logout](#trigger-logout)

### Classify secure resources, unsecured resources
In the sample-app, we have two pages. A landing page (`index.html`) which we have not secured, and another 
page (`home.jsp`) which we have secured.

`IndexPage` property of the sample-app.properties file in the `<APP_HOME>/WEB-INF/classes` directory is used to define 
the landing page of the webapp which is also considered as an unsecured page.
Also the same page is used as the page that the user get redirected once the logout is done.
Here we have set `<APP_HOME>/index.html` as the value of `IndexPage` property.
    IndexPage=/sample-app/index.html

By default, all the other pages are considered as secured pages. Hence `home.jsp` will be secured without any other configurations.

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

## Integrating Asgardio Tomcat SAML Agent

Asgardio Tomcat SAML Agent can be integrated in to your applications in two different ways. 

It can be integrated to your java source project of the webapp when the web application is in development stage.

And, the Tomcat SAML agent can be integrated into a pre-built webapp as well.

#### To your existing webapp

To integrate the Tomcat SAML Agent into your pre-built webapps, follow the guide [here](docs/integrating_with_existing_webapp.md/#Integrating_SAML_into_your_existing_Webapp).

#### To your Java source project

To integrate the Tomcat SAML Agent into your java source project, follow the guide [here](docs/integrating_with_java_source_project.md/#Integrating_SAML_into_your_java_source_project).

## Contributing

Please read [Contributing to the Code Base](http://wso2.github.io/) for details on our code of conduct, and the
 process for submitting pull requests to us.
 
### Reporting issues
We encourage you to report issues, improvements, and feature requests creating [git Issues](https://github.com/asgardio/asgardio-tomcat-saml-agent/issues).

Important: And please be advised that security issues must be reported to security@wso2.com, not as GitHub issues, 
in order to reach the proper audience. We strongly advise following the WSO2 Security Vulnerability Reporting Guidelines
 when reporting the security issues.

## License
This project is licensed under the Apache License 2.0. See the [LICENSE
](LICENSE) file for details.


# Asgardio Tomcat SAML Agent

## Table of Contents
- [Trying out the sample](../README.md/#trying-out-the-sample)
  * [Prerequisites](../README.md/#prerequisites)
  * [Running the Sample](../README.md/#running-the-sample)
- [How it works](#how-it-works)
  * [Classify secure resources, unsecured resources](#classify-secure-resources--unsecured-resources)
  * [Trigger authentication](#trigger-authentication)
  * [Retrieve user attributes](#retrieve-user-attributes)
  * [Trigger logout](#trigger-logout)
- [Integrating SAML into your Java application](../docs/integrating_the_sdk.md/#integrating-saml-into-your-java-application)
  * [Getting Started](../docs/integrating_the_sdk.md/#getting-started)
  * [Configuring the web app](../docs/integrating_the_sdk.md/#configuring-the-web-app)
  * [Retrieving User Attributes](../docs/integrating_the_sdk.md/#retrieving-user-attributes)
- [Installing the SDK](../README.md/#installing-the-sdk)
  * [Github](../README.md/#github)
  * [Building from the source](../README.md/#building-from-the-source)
  * [Maven](../README.md/#maven)
- [Contributing](../README.md/#contributing)
  * [Reporting Issues](../README.md/#reporting-issues)
- [Versioning](../README.md/#versioning)
- [License](../README.md/#license)
## How it works
### Classify secure resources, unsecured resources
In the SampleApp sample, we have two pages. A landing page (`index.html`) which we have not secured, and another 
page (`home.jsp`) which we have secured.

In the SampleApp.properties file in the `identity-agent-sso/resources/SampleApp/src/main/resources` directory, 
we have set the /SampleApp/index.html as the unsecured index page  via the following property:

    IndexPage=/SampleApp/index.html

Hence, the sso agent regards the index.html page as the landing page. Then, the index page would be regarded as a page 
that is not secured. Also the same page is used as the page that user get redirected once the logout is done.

By default, all other pages considered as secured pages. Hence `home.jsp` will be secured without any other configurations.

### Trigger authentication
In the `index.html` page, we have added the action for the login button to trigger a SAML authentication:

`<form method="post" action="samlsso?SAML2.HTTPBinding=HTTP-POST">`

This would engage the SAML2SSOAgentFilter which is specified in the `web.xml` file in the `asgardio-tomcat-saml-agent`
/resources/SampleApp/src/main/webapp/WEB-INF` directory, and redirect the user to the IdP authentication page.

Upon successful authentication, the user would be redirected to the `home.jsp` page.

### Retrieve user attributes

In the `home.jsp` file, we have added the following to get the user subject value and the user attributes refering the SDK API.

```
<%
    LoggedInSessionBean sessionBean = (LoggedInSessionBean) session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
    String subjectId = sessionBean.getSAML2SSO().getSubjectId();
    Map<String, String> saml2SSOAttributes = sessionBean.getSAML2SSO().getSubjectAttributes();
%>
```

### Trigger logout

In the `home.jsp` file, we have added the following to trigger a SLO flow:

``<a href="logout?SAML2.HTTPBinding=HTTP-POST">Logout</a>``

Clicking on the logout link would trigger the SLO flow engaging the same filter mentioned above. The user would be
 redirected to the page configured via the `IndexPage` property previously discussed.
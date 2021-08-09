<%--
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
  --%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="io.asgardeo.java.saml.sdk.exception.SSOAgentException" %>
<%@ page import="io.asgardeo.java.saml.sdk.util.SSOAgentConstants" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>An error has occurred</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" type="text/css" href="theme.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
</head>
<%
    SSOAgentException exception = (SSOAgentException) request.getAttribute(SSOAgentConstants.SSO_AGENT_EXCEPTION);
%>
<body>
    <div class="ui two column centered grid">
        <div class="column center aligned">
            <img src="images/logo-dark.svg" class="logo-image">
        </div>
        <div class="container">
            <div class="header-title">
                <h1>
                    Java-Based SAML Authentication Sample
                </h1>
            </div>
            <div class="content">
                <h2>
                    An error has occurred!
                </h2>
                <h3>
                    <%=exception.getMessage()%>
                </h3>
            </div>
            <div class="message">
                    <div class="banner">
                        <span><i class="fa fa-exclamation-circle icon"></i></span>
                        <div><b>It's possible that you tried an unsupported feature with the sample application.
                        Currently, the below features are supported by this version of the sample 
                        application</b></div>
                    </div>
                    <ul >
                        <li>SAML single sign on</li>
                        <li>SAML single logout</li>
                        <li>Attribute profie</li>
                        <li>Request signing</li>
                        <li>Response signing</li>
                        <li>Assertion signing</li>
                    </ul> 
                </div>
                <form action="index.html" method="post">
                    <div class="element-padding">
                        <button class="btn primary" type="submit">Back</button>
                    </div>
                </form>
            </div>
        <img src="images/footer.png" class="footer-image">
    </div>
</body>
</html>

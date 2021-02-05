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


<%@ page import="io.asgardeo.java.saml.sdk.bean.LoggedInSessionBean" %>
<%@ page import="io.asgardeo.java.saml.sdk.util.SSOAgentConstants" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Home</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" type="text/css" href="theme.css">
</head>
<%
    LoggedInSessionBean sessionBean = (LoggedInSessionBean) session.getAttribute(SSOAgentConstants.SESSION_BEAN_NAME);
    String subjectId = sessionBean.getSAML2SSO().getSubjectId();
    Map<String, String> saml2SSOAttributes = sessionBean.getSAML2SSO().getSubjectAttributes();
%>
<body>
    <div class="ui two column centered grid">
        <div class="column center aligned">
            <img src="images/logo-dark.svg" class="logo-image">
        </div>
        <div class="container">
            <div class="header-title">
                <h1>
                    Java Based SAML Authentication Sample <br> (OIDC - Authorization Code Grant)
                </h1>
            </div>
            <div class="content">
                <%
                    if (subjectId != null) {
                %>
                <h3>
                    You are logged in as <%=subjectId%>
                </h3>
                <%
                    }
                %>
                <!-- <h3>Below are the basic details retrieve from the server on a successful login.</h3> -->
                <div>
                    <%
                        if (saml2SSOAttributes != null) {
                            for (Map.Entry<String, String> entry : saml2SSOAttributes.entrySet()) {
                    %>
                    <dl class="details">
                        <dt><%=entry.getKey()%></dt>
                        <dt><%=entry.getValue()%></dt>
                    </dl>
                    <%
                            }
                        }
                    %>
                </div>
                <button class="btn primary" onClick="location.href=logout?SAML2.HTTPBinding=HTTP-POST">Logout</button>
            </div>
        </div>
        <img src="images/footer.png" class="footer-image">
    </div>
</body>
</html>

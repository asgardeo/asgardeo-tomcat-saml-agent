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
<%@ page isErrorPage="true" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>An error has occurred</title>
    <style>
        html, body {
            height: 100%;
        }

        body {
            flex-direction: column;
            display: flex;
        }

        main {
            flex-shrink: 0;
        }

        main.center-segment {
            margin: auto;
            display: flex;
            align-items: center;
        }

        .element-padding {
            margin: auto;
            padding: 15px;
        }
    </style>
</head>
<body>
<main class="center-segment">
    <div class="element-padding">
        <div class="element-padding">
            <%=exception.getMessage()%>
        </div>
    </div>
</main>
</body>
</html>

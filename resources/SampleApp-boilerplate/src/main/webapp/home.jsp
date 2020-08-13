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

<%--
  Created by IntelliJ IDEA.
  User: chamaths
  Date: 7/27/20
  Time: 21:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Home</title>
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
    <div style="text-align: center">
        <div class="element-padding">
            <h1>Home Page!</h1>
        </div>
        
        <div class="element-padding">
            <a href="/SampleApp/index.html">Logout</a>
        </div>
    </div>
</main>
</body>
</html>

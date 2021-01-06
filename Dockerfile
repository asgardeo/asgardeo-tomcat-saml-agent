# ------------------------------------------------------------------------
#
# Copyright 2020 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

# Builder Stage
FROM tomcat:8-jdk8-openjdk-slim AS builder

COPY io.asgardeo.tomcat.saml.agent.sample/target/sample-app.war webapps

RUN mkdir webapps/sample-app 

WORKDIR /usr/local/tomcat/webapps/sample-app

RUN jar -xvf ../sample-app.war

WORKDIR /usr/local/tomcat

COPY sample-app.properties webapps/sample-app/WEB-INF/classes

# Final Stage
FROM tomcat:8.5-jre8-alpine

LABEL name="Asgardeo Tomcat SAML Agent Sample" \
      maintainer="WSO2"

COPY --from=builder /usr/local/tomcat/webapps/sample-app webapps/sample-app

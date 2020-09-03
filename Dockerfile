FROM tomcat:8.5-jre8

WORKDIR /usr/local/tomcat

ADD io.asgardio.tomcat.saml.agent.sample/target/sample-app.war webapps

EXPOSE 8080

CMD ["bin/catalina.sh", "run"]

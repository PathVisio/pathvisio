@echo off
echo This test assumes a server URL of http://localhost:8080/axis/servlet/
echo Deploying the addressbook2 service...
java org.apache.axis.client.AdminClient deploy.wsdd %*%
echo .
echo Running demo...
java samples.addr.Main %*%

rem this assumes webserver is running on port 8080
@echo off 
java org.apache.axis.client.AdminClient %* securitydeploy.wsdd 

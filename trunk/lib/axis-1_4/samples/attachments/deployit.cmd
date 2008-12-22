rem this assumes webserver is running on port 8080
@echo off 
java org.apache.axis.client.AdminClient %* attachdeploy.wsdd 
java org.apache.axis.client.AdminClient %* testref.wsdd 
rem java org.apache.axis.client.AdminClient list 

#!/bin/sh
# this assumes webserver is running on port 8080
java org.apache.axis.client.AdminClient $* attachdeploy.wsdd

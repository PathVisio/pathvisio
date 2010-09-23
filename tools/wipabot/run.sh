#!/bin/sh

CLASSPATH=build:\
lib/pircbot.jar:\
../../lib/apache/xmlrpc-common-3.0.jar:\
../../lib/apache/xmlrpc-client-3.0.jar:\
../../wikipathways-client.jar 

java -classpath $CLASSPATH org.wikipathways.WipaBotMain

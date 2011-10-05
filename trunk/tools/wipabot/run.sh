#!/bin/sh

CLASSPATH=build:\
lib/pircbot.jar:\
../../lib/apache/xmlrpc-common-3.0.jar:\
../../lib/apache/xmlrpc-client-3.0.jar:\
../../modules/wikipathways-client.jar:\
../../lib/axis-1_4/lib/axis-ant.jar:\
../../lib/axis-1_4/lib/axis.jar:\
../../lib/axis-1_4/lib/commons-discovery-0.2.jar:\
../../lib/axis-1_4/lib/commons-logging-1.0.4.jar:\
../../lib/axis-1_4/lib/jaxrpc.jar:\
../../lib/axis-1_4/lib/log4j-1.2.8.jar:\
../../lib/axis-1_4/lib/saaj.jar:\
../../lib/axis-1_4/lib/wsdl4j-1.5.1.jar:\
../../modules/org.pathvisio.core.jar

java -classpath $CLASSPATH org.wikipathways.WipaBotMain

#!/bin/bash

CLASSPATH=\
modules/org.pathvisio.core.jar:\
lib/com.springsource.org.jdom-1.1.0.jar:\
lib/org.bridgedb.jar:\
lib/org.bridgedb.bio.jar
  
java -ea -classpath $CLASSPATH org.pathvisio.core.gpmldiff.GpmlDiff "$@"

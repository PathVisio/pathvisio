#!/bin/sh
# Make sure we run with correct java version
export JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun
export JAVA_JAVACMD=/usr/lib/jvm/java-1.5.0-sun/jre/bin/java 
./pathvisio_daily_build.pl 


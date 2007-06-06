#!/bin/sh
MYCLASSPATH=\
build:\
./lib/jdom.jar

java -cp $MYCLASSPATH org.pathvisio.gpmldiff.GpmlDiff $1 $2
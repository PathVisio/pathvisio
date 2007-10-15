#!/bin/bash

source classpath.sh;

MYCLASSPATH1=$PATHVISIO_CP:build/v1:build/core

java -ea -cp $MYCLASSPATH1 org.pathvisio.gpmldiff.GpmlDiff $1 $2 $3 $4 $5 $6 $7 $8 $9
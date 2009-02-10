#!/bin/sh

# change to directory of this script
cd $(dirname $0)

# run PathVisio, try to load plugins in visplugins.jar 
# this option will be ignored if visplugins.jar does not exist.
java -jar pathvisio.jar -p visplugins.jar "$@"

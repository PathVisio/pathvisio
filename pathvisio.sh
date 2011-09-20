#!/bin/sh

# change to directory of this script
cd $(dirname $0)

# Run PathVisio
java -jar pathvisio.jar "$@"

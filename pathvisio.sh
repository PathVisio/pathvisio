#!/bin/sh

# change to directory of this script
cd $(dirname $0)

# run PathVisio, try to load plugins in visplugins.jar 
# this option will be ignored if visplugins.jar does not exist.
java -cp modules/org.pathvisio.launcher.jar:lib/org.eclipse.osgi.jar:modules/org.pathvisio.core.jar org.pathvisio.launcher.PathVisioMain "$@"

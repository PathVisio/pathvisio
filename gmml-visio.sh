#!/bin/sh
export LD_LIBRARY_PATH=/usr/local/lib:/usr/lib/atlas:/usr/lib/mozilla
export MOZILLA_FIVE_HOME=/usr/lib/mozilla
java -cp lib/JRI.jar:lib/org.eclipse.equinox.common.jar:lib/org.eclipse.equinox.supplement.jar:lib/org.eclipse.jface.jar:lib/swt-linux.jar:lib/org.eclipse.core.commands.jar:lib/jdom.jar:build:lib/hsqldb.jar:lib/swt-linux-lib.jar:lib/resources.jar -Djava.library.path="/usr/lib/mozilla:./lib/swt-linux-lib" gmmlVision.GmmlVisionMain

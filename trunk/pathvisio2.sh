#!/bin/sh
export LD_LIBRARY_PATH=/usr/local/lib:/usr/lib/atlas:/usr/lib/firefox:/usr/lib/R/lib
export MOZILLA_FIVE_HOME=/usr/lib/firefox
export R_HOME=/usr/lib/R
java -cp lib/plugins.jar:lib/JRI.jar:lib/org.eclipse.equinox.common.jar:lib/org.eclipse.equinox.supplement.jar:lib/org.eclipse.jface.jar:lib/swt-linux.jar:lib/org.eclipse.core.commands.jar:lib/jdom.jar:build/v2:lib/derby.jar:lib/swt-linux-lib.jar:lib/resources.jar:lib/R-resources.jar -Djava.library.path="/usr/lib/firefox:./lib/swt-linux-lib" org.pathvisio.gui.GuiMain -ur

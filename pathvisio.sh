#!/bin/sh
export LD_LIBRARY_PATH=/usr/local/lib:/usr/lib/atlas:/usr/lib/firefox
export MOZILLA_FIVE_HOME=/usr/lib/firefox
java -cp lib/JRI.jar:lib/BrowserLauncher.jar:lib/org.eclipse.equinox.common.jar:lib/org.eclipse.equinox.supplement.jar:lib/org.eclipse.jface.jar:lib/swt-linux-lib/swt.jar:lib/org.eclipse.core.commands.jar:lib/jdom.jar:build/v1:lib/derby.jar:lib/swt-linux-lib.jar:lib/resources.jar -Djava.library.path="/usr/lib/firefox:./lib/swt-linux-lib" org.pathvisio.gui.GuiMain 

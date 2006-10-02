#!/bin/sh
java -cp lib/JRI.jar:lib/org.eclipse.equinox.common.jar:lib/org.eclipse.equinox.supplement.jar:lib/org.eclipse.jface.jar:lib/swt-linux.jar:lib/org.eclipse.core.commands.jar:lib/jdom.jar:build:lib/hsqldb.jar:lib/swt-linux-lib.jar:lib/resources.jar -Djava.library.path=/home/martijn/prg/gmml-visio/trunk/lib/swt-linux-lib/ gmmlVision.GmmlVisionMain

#!/bin/sh
export LD_LIBRARY_PATH=/usr/local/lib:/usr/lib/atlas:/usr/lib/firefox:/usr/lib/R/lib
export MOZILLA_FIVE_HOME=/usr/lib/firefox
export R_HOME=/usr/lib/R

# read classpath from file named CLASSPATH
MYCLASSPATH=`perl -e 'while (<>) { chomp; push @l, $_ } print join ":", @l' CLASSPATH`

java -cp $MYCLASSPATH -Djava.library.path="/usr/lib/firefox:./lib/swt-linux-lib" org.pathvisio.gui.GuiMain -ur

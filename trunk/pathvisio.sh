#!/bin/bash

# RUN_MODE can be "DIRECT" or "WEBSTART"
RUN_MODE=DIRECT
# MAIN_CLASS contains the main class to run when RUN_MODE is DIRECT
MAIN_CLASS=org.pathvisio.gui.swt.GuiMain
# BASE_URL contains the webstart url (without pathvisio_v1.jnlp or pathvisio_v2.jnlp)
BASE_URL=
# Set USE_EXPERIMENTAL to 1 if you want to run with Data visualizatoin and R mode
USE_EXPERIMENTAL=0

while getopts ":gerds" options; do
	case $options in
		g )
			RUN_MODE=DIRECT
			MAIN_CLASS=org.pathvisio.gui.swing.GuiMain
			BASE_URL=
			;;
		e )
			USE_EXPERIMENTAL=1
			;;
		s )
			USE_EXPERIMENTAL=0
			;;
		r )
			RUN_MODE=WEBSTART
			MAIN_CLASS=
			BASE_URL=http://blog.bigcat.unimaas.nl/~gmmlvisio
			;;
		d )
			RUN_MODE=WEBSTART
			MAIN_CLASS=
			BASE_URL=http://ftp2.bigcat.unimaas.nl/~martijn.vaniersel/pathvisio/daily/webstart
			;;
		\? )
			echo "Usage: `basename $0` [-g|-r|-d] [-e|-s] [-?]"
			echo "  -g : Use swing instead of swt"
			echo "  -r : Use webstart, latest stable release"
			echo "  -d : Use webstart, daily build"
			echo "  -e : Turn on experimental features (Data visualization, statistics)"
			echo "  -s : Turn off experimental features (default)"
			echo "  -? : show this help message"
			exit;
			;;
	esac
done

export LD_LIBRARY_PATH=/usr/local/lib:/usr/lib/atlas:/usr/lib/firefox
export MOZILLA_FIVE_HOME=/usr/lib/firefox

MYCLASSPATHBASE=lib/JRI.jar:\
lib/BrowserLauncher.jar:\
lib/org.eclipse.equinox.common.jar:\
lib/org.eclipse.equinox.supplement.jar:\
lib/org.eclipse.jface.jar:\
lib/swt-linux-lib/swt.jar:\
lib/org.eclipse.core.commands.jar:\
lib/jdom.jar:\
lib/derby.jar:\
lib/swt-linux-lib.jar:\
lib/resources.jar:\
lib/batik/batik-awt-util.jar:\
lib/batik/batik-ext.jar:\
lib/batik/batik-script.jar:\
lib/batik/batik-util.jar:\
lib/batik/batik-dom.jar:\
lib/batik/xml-apis.jar:\
lib/batik/batik-xml.jar:\
lib/batik/batik-extension.jar:\
lib/batik/pdf-transcoder.jar:\
lib/batik/batik-css.jar:\
lib/batik/batik-transcoder.jar:\
lib/batik/batik-svg-dom.jar:\
lib/batik/batik-parser.jar:\
lib/batik/batik-svggen.jar:\
lib/batik/batik-bridge.jar:\
lib/batik/batik-gvt.jar

MYCLASSPATH1=$MYCLASSPATHBASE:build/v1
MYCLASSPATH2=$MYCLASSPATHBASE:build/v2

if [ $RUN_MODE = "DIRECT" ]; then
	if [ $USE_EXPERIMENTAL = "0" ]; then
		java -cp $MYCLASSPATH1 -Djava.library.path="/usr/lib/firefox:./lib/swt-linux-lib" $MAIN_CLASS
	elif [ $USE_EXPERIMENTAL = "1" ]; then
		java -cp $MYCLASSPATH2 -Djava.library.path="/usr/lib/firefox:./lib/swt-linux-lib" $MAIN_CLASS -ur
	fi	
elif [ $RUN_MODE = "WEBSTART" ]; then
	if [ $USE_EXPERIMENTAL = "0" ]; then
		javaws "$BASE_URL/pathvisio_v1.jnlp"
	elif [ $USE_EXPERIMENTAL = "1" ]; then
		javaws "$BASE_URL/pathvisio_v2.jnlp" -ur
	fi
fi
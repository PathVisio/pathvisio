#!/bin/bash

WSDL_URI="http://localhost/wikipathways/wpi/webservice/webservice.php?wsdl"
export JAVA_HOME="/usr/lib/jvm/java-1.5.0-sun"
export AXIS2_HOME="axis2"

while getopts ":gerdsj" options; do
	case $options in
		url )
			BASE_URL=$1
			;;
		\? )
			echo "Usage: `basename $0` -uri"
			echo "  -url : the url of the wikipathways webservice wsdl"
			echo "  -? : show this help message"
			exit;
			;;
	esac
done

axis2/bin/wsdl2java.sh -or -uri $WSDL_URI -p org.pathvisio.wikipathways.webservice -d adb -s -o .
ant jar.all

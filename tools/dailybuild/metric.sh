#!/bin/sh

cd ../..
ant clean dist-clean
ant checkstyle tarbin jar jar-wikipathways

LOG="$HOME/pv_mut2.log";
DATE=`date`;

VALUE=`find src -iname "*.java" -exec cat '{}' \; | wc -l`
echo "$DATE\tLOC in src/**/*.java\t$VALUE\tLOC" >> $LOG

VALUE=`find src -iname "*.java" | wc -l`
echo "$DATE\tjava files in src\t$VALUE\tfiles" >> $LOG

VALUE=`find src/core -iname "*.java" -exec cat '{}' \; | wc -l`
echo "$DATE\tLOC in src/core/**/*.java\t$VALUE\tLOC" >> $LOG

VALUE=`find src/core -iname "*.java" | wc -l`
echo "$DATE\tjava files in src/core\t$VALUE\tfiles" >> $LOG

VALUE=`find src/gui -iname "*.java" -exec cat '{}' \; | wc -l`
echo "$DATE\tLOC in src/gui/**/*.java\t$VALUE\tLOC" >> $LOG

VALUE=`find src/gui -iname "*.java" | wc -l`
echo "$DATE\tjava files in src/gui\t$VALUE\tfiles" >> $LOG

VALUE=`find src/wikipathways -iname "*.java" -exec cat '{}' \; | wc -l`
echo "$DATE\tLOC in src/wikipathways/**/*.java\t$VALUE\tLOC" >> $LOG

VALUE=`find src/wikipathways -iname "*.java" | wc -l`
echo "$DATE\tjava files in src/wikipatways\t$VALUE\tfiles" >> $LOG

VALUE=`find src/swing -iname "*.java" -exec cat '{}' \; | wc -l`
echo "$DATE\tLOC in src/swing/**/*.java\t$VALUE\tLOC" >> $LOG

VALUE=`find src/swing -iname "*.java" | wc -l`
echo "$DATE\tjava files in src/swing\t$VALUE\tfiles" >> $LOG

VALUE=`stat -c"%s" pathvisio_bin*.tar.gz`
echo "$DATE\tsize of pathvisio_bin_xxx.tar.gz\t$VALUE\tbytes" >> $LOG

VALUE=`stat -c"%s" pathvisio.jar`
echo "$DATE\tsize of pathvisio.jar\t$VALUE\tbytes" >> $LOG

VALUE=`stat -c"%s" pathvisio_core.jar`
echo "$DATE\tsize of pathvisio_core.jar\t$VALUE\tbytes" >> $LOG

VALUE=`stat -c"%s" wikipathways.jar`
echo "$DATE\tsize of wikipathways.jar\t$VALUE\tbytes" >> $LOG

VALUE=`cat warnings.txt | wc -l`
echo "$DATE\tnumber of checkstyle warnings\t$VALUE\twarnings" >> $LOG

VALUE=`find src -iname "*.java" -exec grep TODO '{}' \; | wc -l`
echo "$DATE\tnumber of TODO items\t$VALUE\ttasks" >> $LOG

VALUE=`du build/wpi/applet/ -b --max-depth=0`
echo "$DATE\tsize of applet dir\t$VALUE\tbytes" >> $LOG

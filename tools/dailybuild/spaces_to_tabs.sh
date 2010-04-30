#!/bin/sh
#find ../.. -iname "*.java" -exec unexpand -t4 '{}' > '{}'.new && mv '{}'.new '{}' \;
for i in `find ../../src -iname "*.java"`; do unexpand --tabs=4 --first-only $i > $i.new; mv $i.new $i; done

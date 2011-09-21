#!/bin/sh

# extract the license header from a file that has it:
head -n 16 ../../modules/org.pathvisio.core/src/org/pathvisio/core/Engine.java > license.txt
# create a list of files missing the license header (exclude Revision.java)
find ../.. -wholename "**/org/pathvisio/**/*.java" \
	! -name "Revision.java" \
	! -wholename "**/org.wikipathways.client/gensrc/**" \
	| xargs -d '\n' grep -L "Apache License, Version 2.0" \
  > license_check.txt
# add contents of license.txt at the beginning of those files.
for i in $( cat license_check.txt ); do
  cp $i $i.bak;
  cat license.txt $i.bak > $i;
done

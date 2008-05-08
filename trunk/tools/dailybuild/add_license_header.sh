#!/bin/sh

# extract the license header from a file that has it:
head -n 16 ../../src/core/org/pathvisio/Engine.java > license.txt
# create a list of files missing the license header (exclude Revision.java)
find ../.. -name "*.java" ! -name "Revision.java" \
 | xargs -d '\n' grep -L "Apache License, Version 2.0" \
 | xargs -d '\n' grep -l "package org.pathvisio" \
  > license_check.txt
# add contents of license.txt at the beginning of those files.
for i in $( cat license_check.txt ); do
  cp $i $i.bak;
  cat license.txt $i.bak > $i;
done

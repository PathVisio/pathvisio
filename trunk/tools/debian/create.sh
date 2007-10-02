#!/bin/sh
TEMPDIR=`mktemp -d` || exit 1
find nbx-pathvisio -! -iwholename "*.svn*" -exec cp --parent '{}' $TEMPDIR \;
dpkg-deb --build $TEMPDIR/nbx-pathvisio .

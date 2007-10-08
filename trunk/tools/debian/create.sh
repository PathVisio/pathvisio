#!/bin/sh

# create a temporary dir
TEMPDIR=`mktemp -d` || exit 1

# copy all files to temporary dir, excluding .svn dirs
find nbx-pathvisio -type f -! -iwholename "*.svn*" -exec cp --parent '{}' $TEMPDIR \;

# build the package (fakeroot needed to simulate root:root ownership)
fakeroot dpkg-deb --build $TEMPDIR/nbx-pathvisio .

echo Checking package with lintian

# do sanity checks
lintian nbx-pathvisio*.deb
#!/bin/sh

# Run the stable release version through webstart
# For running the daily build, or enabling experimental features, see pathvisio.sh

export MOZILLA_FIVE_HOME=/usr/lib/firefox
export LD_LIBRARY_PATH=/usr/lib/firefox:$LD_LIBRARY_PATH
javaws "http://blog.bigcat.unimaas.nl/~gmmlvisio/pathvisio_v1.jnlp"
#!/bin/sh
export MOZILLA_FIVE_HOME=/usr/lib/firefox
export LD_LIBRARY_PATH=/usr/lib/firefox:$LD_LIBRARY_PATH
javaws "http://ftp2.bigcat.unimaas.nl/~martijn.vaniersel/pathvisio/daily/webstart/pathvisio_v1.jnlp"
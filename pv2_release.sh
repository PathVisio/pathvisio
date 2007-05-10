#!/bin/sh
export MOZILLA_FIVE_HOME=/usr/lib/firefox
export LD_LIBRARY_PATH=/usr/lib/R/lib:/usr/lib/firefox:$LD_LIBRARY_PATH
export R_HOME=/usr/lib/R
javaws "http://blog.bigcat.unimaas.nl/~gmmlvisio/pathvisio_v2.jnlp"
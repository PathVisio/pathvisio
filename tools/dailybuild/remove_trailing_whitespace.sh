#!/bin/sh
find ../.. -iname "*.java" -exec sed -i 's/[ \t]*$//' '{}' \;

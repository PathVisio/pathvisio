cd /d %~dp0

java -Xmx1024m -jar -Dfile.encoding=UTF-8 pathvisio.jar "$@"

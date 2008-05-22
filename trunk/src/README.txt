Layout of the src directory:

core - contains non-gui parts
gui - contains gui parts that are shared by at least two of wikipathways, swt and swing
wikipathways - applet
swing - standalone swing version
swt - swt version
test - Junit tests for each package

To get a running version of any command line utility, you only need to compile core
To run tests, compile core + test
To run the swing version, compile core + gui + swing 
To run the swt version, compile core + gui + swt
To run the wikipathways applet, compile core + gui + wikipathways
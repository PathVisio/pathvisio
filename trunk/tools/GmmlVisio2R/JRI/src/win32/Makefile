# helper tools and libs for building and running rJava for Windows
# Author: Simon Urbanek

TARGETS=libjvm.dll.a findjava.exe

# libjvm.dll.a    - wrapper lib for jvm.dll from Java
# findjava.exe    - helper tool to find the current JDK from the registry

all: $(TARGETS)

libjvm.dll.a: jvm.def
	dlltool --input-def jvm.def --kill-at --dllname jvm.dll --output-lib libjvm.dll.a

# compile findjava.exe from source - no magic here, no special libs necessary
findjava.o: findjava.c
	$(CC) -O2 -c -o $@ $^

findjava.exe: findjava.o
	$(CC) -s -o $@ $^

# just cleanup everything
clean:
	rm -f *.o *~ $(TARGETS)

.PHONY: all clean

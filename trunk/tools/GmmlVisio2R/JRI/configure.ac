AC_INIT([JRI],[0.1],[urbanek@research.att.com])
AC_CONFIG_SRCDIR([src/jri.h])
AC_CONFIG_HEADER([src/config.h])
AC_CONFIG_AUX_DIR([tools])

AC_CANONICAL_BUILD
AC_CANONICAL_HOST

if test "$host" != "$build"; then
   AC_MSG_ERROR([I'm sorry, but cross-compiling is not supported, because we need to run Java code against the target JVM in order to determine paths and flags.])
fi

# find R home
: ${R_HOME=`R RHOME`}
if test -z "${R_HOME}"; then
  echo "could not determine R_HOME"
  exit 1
fi
# we attempt to use the same compiler as R did
R_CC=`${R_HOME}/bin/R CMD config CC`
R_CFLAGS=`${R_HOME}/bin/R CMD config CFLAGS`


# find R_SHARE_DIR
: ${R_SHARE_DIR=`R CMD sh -c 'echo $R_SHARE_DIR'`}
if test -z "${R_SHARE_DIR}"; then
  echo "could not determine R_SHARE_DIR"
  exit 1
fi

# find R_DOC_DIR
: ${R_DOC_DIR=`R CMD sh -c 'echo $R_DOC_DIR'`}
if test -z "${R_DOC_DIR}"; then
  echo "could not determine R_DOC_DIR"
  exit 1
fi

# find R_INCLUDE_DIR
: ${R_INCLUDE_DIR=`R CMD sh -c 'echo $R_INCLUDE_DIR'`}
if test -z "${R_INCLUDE_DIR}"; then
  echo "could not determine R_INCLUDE_DIR"
  exit 1
fi

# if user did not specify CC then we use R's settings.
# if CC was set then user is responsible for CFLAGS as well!
if test -z "${CC}"; then
   CC=$R_CC
   CFLAGS=$R_CFLAGS
fi

RINC=`${R_HOME}/bin/R CMD config --cppflags`
RLD=`${R_HOME}/bin/R CMD config --ldflags`

if test -z "$RLD"; then
   AC_MSG_ERROR([*** R was not compiled with --enable-R-shlib!
*** You must have libR.so or equivalent in order to use JRI])
fi

AC_SUBST(R_HOME)
AC_SUBST(R_SHARE_DIR)
AC_SUBST(R_DOC_DIR)
AC_SUBST(R_INCLUDE_DIR)

AC_PROG_CC
AC_HEADER_STDC

## RUN_JAVA(variable for the result, parameters)
## ----------
## runs the java interpreter ${JAVA_PROG} with specified parameters and
## saves the output to the supplied variable. The exit value is ignored.
AC_DEFUN([RUN_JAVA],
[
  acx_java_result=
  if test -z "${JAVA_PROG}"; then
    echo "$as_me:$LINENO: JAVA_PROG is not set, cannot run java $2" >&AS_MESSAGE_LOG_FD
  else
    echo "$as_me:$LINENO: running ${JAVA_PROG} $2" >&AS_MESSAGE_LOG_FD
    acx_java_result=`${JAVA_PROG} $2 2>&AS_MESSAGE_LOG_FD`
    echo "$as_me:$LINENO: output: '$acx_java_result'" >&AS_MESSAGE_LOG_FD
  fi
  $1=$acx_java_result
])

## find java compiler binaries
if test -z "${JAVA_HOME}" ; then
  JAVA_PATH=${PATH}
else
  JAVA_PATH=${JAVA_HOME}:${JAVA_HOME}/bin:${PATH}
fi

## if 'java' is not on the PATH or JAVA_HOME, add some guesses as of
## where java could live
JAVA_PATH=${JAVA_PATH}:/usr/java/bin:/usr/jdk/bin:/usr/lib/java/bin:/usr/lib/jdk/bin:/usr/local/java/bin:/usr/local/jdk/bin:/usr/local/lib/java/bin:/usr/local/lib/jdk/bin
AC_PATH_PROGS(JAVA_PROG,java,,${JAVA_PATH})
## FIXME: we may want to check for jikes, kaffe and others...
AC_PATH_PROGS(JAVAC,javac,,${JAVA_PATH})
AC_PATH_PROGS(JAVAH,javah,,${JAVA_PATH})
AC_PATH_PROGS(JAR,jar,,${JAVA_PATH})

have_all_java=yes
if test -z "$JAVA_PROG"; then have_all_java=no; fi
if test -z "$JAVAC"; then have_all_java=no; fi
if test -z "$JAVAH"; then have_all_java=no; fi
if test -z "$JAR"; then have_all_java=no; fi
if test ${have_all_java} = no; then
  AC_MSG_ERROR([*** JDK is incomplete! Please make sure you have a complete JDK. JRE is *not* sufficient.])
fi

## this is where our test-class lives
getsp_cp=tools

AC_MSG_CHECKING([whether Java interpreter works])
acx_java_works=no
if test -n "${JAVA_PROG}" ; then
  RUN_JAVA(acx_jc_result,[-classpath ${getsp_cp} getsp -test])
  if test "${acx_jc_result}" = "Test1234OK"; then
    acx_java_works=yes
  fi
  acx_jc_result=
fi

if test ${acx_java_works} = yes; then
  AC_MSG_RESULT([yes])

  AC_MSG_CHECKING([for Java environment])
  ## retrieve JAVA_HOME from Java itself if not set 
  if test -z "${JAVA_HOME}" ; then
    RUN_JAVA(JAVA_HOME,[-classpath ${getsp_cp} getsp java.home])
  fi

  ## the availability of JAVA_HOME will tell us whether it's supported
  if test -z "${JAVA_HOME}" ; then
    if test x$acx_java_env_msg != xyes; then
      AC_MSG_RESULT([not found])
    fi
  else
    AC_MSG_RESULT([in ${JAVA_HOME}])

    case "${host_os}" in
      darwin*)
        JAVA_LIBS="-framework JavaVM"
        JAVA_LD_PATH=
        ;;
      *)
        RUN_JAVA(JAVA_LIBS, [-classpath ${getsp_cp} getsp -libs])
        JAVA_LIBS="${JAVA_LIBS} -ljvm"
        RUN_JAVA(JAVA_LD_PATH, [-classpath ${getsp_cp} getsp java.library.path])
        ;;
    esac
    ## note that we actually don't test JAVA_LIBS - we hope that the detection
    ## was correct. We should also test the functionality for javac.

    have_java=yes
  fi
else
  AC_MSG_RESULT([no])
  AC_MSG_ERROR([Java not found. Please install JDK 1.4 or later, make sure that the binaries are on the PATH and re-try. If that doesn't work, set JAVA_HOME correspondingly.])
fi

AC_CHECK_FILE(${JAVA_HOME}/include/jni.h,
 [JNI_H="${JAVA_HOME}/include"],
 [AC_CHECK_FILE(${JAVA_HOME}/jni.h,
  [JNI_H="${JAVA_HOME}"],
  [AC_CHECK_FILE(${JAVA_HOME}/../include/jni.h,
   [JNI_H="${JAVA_HOME}/../include"],
   [AC_MSG_ERROR([jni headers not found. Please make sure you have a proper JDK installed.])
  ])
 ])
])

JAVA_INC="-I${JNI_H}"

# Sun's JDK needs jni_md.h in in addition to jni.h and unfortunately it's stored somewhere else ...
# this should be become more general at some point - so far we're checking linux and solaris only
# (well, those are presumably the only platforms supported by Sun's JDK and others don't need this
# at least as of now - 01/2004)
jac_found_md=no
for mddir in . linux solaris ppc irix alpha aix hp-ux genunix cygwin win32 freebsd; do
AC_CHECK_FILE(${JNI_H}/$mddir/jni_md.h,[JAVA_INC="${JAVA_INC} -I${JNI_H}/$mddir" jac_found_md=yes])
if test $jac_found_md == yes; then break; fi
done

LIBS="${LIBS} ${JAVA_LIBS}"
CFLAGS="${CFLAGS} ${JAVA_INC}"

AC_MSG_CHECKING([whether JNI programs can be compiled])
AC_LINK_IFELSE([
#include <jni.h>
int main(void) {
    jobject o;
    return 0;
}
        ],[AC_MSG_RESULT(yes)],
        [AC_MSG_ERROR([Cannot compile a simple JNI program. See config.log for details.])])

LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${JAVA_LD_PATH}
export LD_LIBRARY_PATH

AC_MSG_CHECKING([whether JNI programs can be run])
AC_RUN_IFELSE([
#include <jni.h>
int main(void) {
    jobject o;
    return 0;
}
        ],[AC_MSG_RESULT(yes)],
        [AC_MSG_ERROR([Cannot run a simple JNI program - probably your jvm library is in non-standard location or JVM is unsupported. See config.log for details.])])

AC_MSG_CHECKING([JNI data types])
AC_RUN_IFELSE([AC_LANG_SOURCE([[
#include <jni.h>
int main(void) {
  return (sizeof(int)==sizeof(jint) && sizeof(long)==sizeof(long) && sizeof(jbyte)==sizeof(char) && sizeof(jshort)==sizeof(short) && sizeof(jfloat)==sizeof(float) && sizeof(jdouble)==sizeof(double))?0:1;
}
        ]])],[AC_MSG_RESULT([ok])],[AC_MSG_ERROR([One or more JNI types differ from the corresponding native type. You may need to use non-standard compiler flags or a different compiler in order to fix this.])],[])

JNIPREFIX=lib
CPICF=-fPIC
JNISO=.so
JNILD="-shared ${JAVA_LIBS}"

# we need to adjust a few things according to OS ..
case "${host_os}" in
  darwin*)
    JNISO=.jnilib
    JNILD="-dynamiclib -framework JavaVM"
    CPICF=-fno-common

    if test -e "${R_HOME}/lib/i386" -a -e "${R_HOME}/lib/ppc" -a -e "${R_HOME}/lib/libR.dylib"; then
       # we have an universal framework, so we will use stubs and fat lib
       RLD="-framework R"
       RINC="-I${R_HOME}/include"
       # we can even cross-compile, maybe
       if test `arch` = i386 -a -z "${FORCE_NATIVE}"; then
          CFLAGS=""
	  LDFLAGS=""
	  CC="gcc -arch i386 -arch ppc"
	  echo "=== building universal JNI library - use FORCE_NATIVE=yes to avoid this ==="
       fi
    fi
    ;;
  *)
    ;;
esac

origCFLAGS=$CFLAGS
CFLAGS="${CFLAGS} ${R_CFLAGS} -I${R_INCLUDE_DIR}"

AC_MSG_CHECKING([whether Rinterface.h exports R_CStackXXX variables])
AC_COMPILE_IFELSE([
#define CSTACK_DEFNS
#include <R.h>
#include <Rinterface.h>
int main(void) {
    return R_CStackLimit?0:1;
}
        ],[AC_MSG_RESULT(yes)
           DEFFLAGS="${DEFFLAGS} -DRIF_HAS_CSTACK"],
        [AC_MSG_RESULT(no)])

AC_MSG_CHECKING([whether Rinterface.h exports R_SignalHandlers])
AC_COMPILE_IFELSE([
#include <R.h>
#include <Rinterface.h>
int main(void) {
    return R_SignalHandlers;
}
        ],[AC_MSG_RESULT(yes)
           DEFFLAGS="${DEFFLAGS} -DRIF_HAS_RSIGHAND"],
        [AC_MSG_RESULT(no)])

CFLAGS=${origCFLAGS}

AC_SUBST(JAVA_HOME)
AC_SUBST(JAVA_PROG)
AC_SUBST(JAVA_LD_PATH)
AC_SUBST(JAVA_LIBS)
AC_SUBST(JAVA_INC)
AC_SUBST(JAVAC)
AC_SUBST(JAVAH)
AC_SUBST(JAR)
AC_SUBST(JNILD)
AC_SUBST(JNISO)
AC_SUBST(JNIPREFIX)
AC_SUBST(CPICF)
AC_SUBST(CFLAGS)
AC_SUBST(LDFLAGS)
AC_SUBST(RINC)
AC_SUBST(RLD)
AC_SUBST(CC)
AC_SUBST(DEFFLAGS)

AC_CONFIG_FILES([src/Makefile])
AC_CONFIG_FILES([Makefile])
AC_CONFIG_FILES([run], [chmod +x run])

AC_OUTPUT
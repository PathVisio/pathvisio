#include "jri.h"
#include <jni.h>
#include <R.h>
#include <Rdefines.h>
#include <R_ext/Parse.h>

#include <stdarg.h>

/* debugging output (enable with -DRJ_DEBUG) */
#ifdef RJ_DEBUG
void rjprintf(char *fmt, ...) {
  va_list v;
  va_start(v,fmt);
  vprintf(fmt,v);
  va_end(v);
}
#else
#define rjprintf(...)
#endif

void jri_error(char *fmt, ...) {
    va_list v;
    va_start(v,fmt);
    vprintf(fmt,v);
    va_end(v);
}

/* profiling code (enable with -DRJ_PROFILE) */
#ifdef RJ_PROFILE
#include <sys/time.h>

long time_ms() {
#ifdef Win32
  return 0; /* in Win32 we have no gettimeofday :( */
#else
  struct timeval tv;
  gettimeofday(&tv,0);
  return (tv.tv_usec/1000)+(tv.tv_sec*1000);
#endif
}

long profilerTime;

#define profStart() profilerTime=time_ms()
void profReport(char *fmt, ...) {
  long npt=time_ms();
  va_list v;
  va_start(v,fmt);
  vprintf(fmt,v);
  va_end(v);
  printf(" %ld ms\n",npt-profilerTime);
  profilerTime=npt;
}
#else
#define profStart()
#define profReport(...)
#endif

jstring jri_putString(JNIEnv *env, SEXP e, int ix) {
    return (TYPEOF(e)!=STRSXP || LENGTH(e)<=ix)?0:(*env)->NewStringUTF(env, CHAR(STRING_ELT(e,ix)));
}

jarray jri_putStringArray(JNIEnv *env, SEXP e)
{
    if (TYPEOF(e)!=STRSXP) return 0;
    {
        int j=0;
        jobjectArray sa=(*env)->NewObjectArray(env, LENGTH(e), (*env)->FindClass(env, "java/lang/String"), 0);
        if (!sa) { jri_error("Unable to create string array."); return 0; }
        while (j<LENGTH(e)) {
            jobject s=(*env)->NewStringUTF(env, CHAR(STRING_ELT(e,j)));
            rjprintf (" [%d] \"%s\"\n",j,CHAR(STRING_ELT(e,j)));
            (*env)->SetObjectArrayElement(env,sa,j,s);
            j++;
        }
        return sa;
    }
}

jarray jri_putIntArray(JNIEnv *env, SEXP e)
{
    if (TYPEOF(e)!=INTSXP) return 0;
    rjprintf(" integer vector of length %d\n",LENGTH(e));
    {
        unsigned len=LENGTH(e);
        jintArray da=(*env)->NewIntArray(env,len);
        jint *dae;

        if (!da) {
            jri_error("newIntArray.new(%d) failed",len);
            return 0;
        }
        
        if (len>0) {
            dae=(*env)->GetIntArrayElements(env, da, 0);
            if (!dae) {
                (*env)->DeleteLocalRef(env,da);
                jri_error("newIntArray.GetIntArrayElements failed");
                return 0;
            }
            memcpy(dae,INTEGER(e),sizeof(jint)*len);
            (*env)->ReleaseIntArrayElements(env, da, dae, 0);
        }
        return da;
    }
}

jarray jri_putBoolArrayI(JNIEnv *env, SEXP e)
{
    if (TYPEOF(e)!=LGLSXP) return 0;
    rjprintf(" integer vector of length %d\n",LENGTH(e));
    {
        unsigned len=LENGTH(e);
        jintArray da=(*env)->NewIntArray(env,len);
        jint *dae;

        if (!da) {
            jri_error("newIntArray.new(%d) failed",len);
            return 0;
        }
        
        if (len>0) {
            dae=(*env)->GetIntArrayElements(env, da, 0);
            if (!dae) {
                (*env)->DeleteLocalRef(env,da);
                jri_error("newIntArray.GetIntArrayElements failed");
                return 0;
            }
            memcpy(dae,INTEGER(e),sizeof(jint)*len);
            (*env)->ReleaseIntArrayElements(env, da, dae, 0);
        }
        return da;
    }
}

jarray jri_putSEXPLArray(JNIEnv *env, SEXP e)
{
    rjprintf(" general vector of length %d\n",LENGTH(e));
    {
        unsigned len=LENGTH(e);
        jlongArray da=(*env)->NewLongArray(env,len);
        jlong *dae;
        
        if (!da) {
            jri_error("newLongArray.new(%d) failed",len);
            return 0;
        }
        
        if (len>0) {
            int i=0;
            
            dae=(*env)->GetLongArrayElements(env, da, 0);
            if (!dae) {
                (*env)->DeleteLocalRef(env,da);
                jri_error("newLongArray.GetLongArrayElements failed");
                return 0;
            }
            while (i<len) {
                dae[i] = SEXP2L(VECTOR_ELT(e, i));
                i++;
            }
            (*env)->ReleaseLongArrayElements(env, da, dae, 0);
        }
        return da;
    }
}

jarray jri_putDoubleArray(JNIEnv *env, SEXP e)
{
    if (TYPEOF(e)!=REALSXP) return 0;
    rjprintf(" real vector of length %d\n",LENGTH(e));
    {
        unsigned len=LENGTH(e);
        jdoubleArray da=(*env)->NewDoubleArray(env,len);
        jdouble *dae;

        if (!da) {
            jri_error("newDoubleArray.new(%d) failed",len);
            return 0;
        }
        if (len>0) {
            dae=(*env)->GetDoubleArrayElements(env, da, 0);
            if (!dae) {
                (*env)->DeleteLocalRef(env,da);
                jri_error("newDoubleArray.GetDoubleArrayElements failed");
                return 0;
            }
            memcpy(dae,REAL(e),sizeof(jdouble)*len);
            (*env)->ReleaseDoubleArrayElements(env, da, dae, 0);
        }
        return da;
    }
}

/** jobjRefInt object : string */
SEXP jri_getString(JNIEnv *env, jstring s) {
  SEXP r;
  const char *c;

  if (!s) return R_NilValue;
  profStart();
  c=(*env)->GetStringUTFChars(env, s, 0);
  if (!c) {
      jri_error("jri_getString: can't retrieve string content");
      return R_NilValue;
  }
  PROTECT(r=allocVector(STRSXP,1));
  SET_STRING_ELT(r, 0, mkChar(c));
  UNPROTECT(1);
  (*env)->ReleaseStringUTFChars(env, s, c);
  profReport("jri_getString:");
  return r;
}

SEXP jri_installString(JNIEnv *env, jstring s) {
    SEXP r;
    const char *c;
    
    if (!s) return R_NilValue;
    profStart();
    c=(*env)->GetStringUTFChars(env, s, 0);
    if (!c) {
        jri_error("jri_getString: can't retrieve string content");
        return R_NilValue;
    }
    r = install(c);
    (*env)->ReleaseStringUTFChars(env, s, c);
    profReport("jri_getString:");
    return r;
}

jstring jri_putSymbolName(JNIEnv *env, SEXP e) {
	SEXP pn;
    if (TYPEOF(e)!=SYMSXP) return 0;
	pn=PRINTNAME(e);
	return (TYPEOF(pn)!=CHARSXP)?0:(*env)->NewStringUTF(env, CHAR(pn));
}

/** calls .toString() of the object and returns the corresponding string java object */
jstring jri_callToString(JNIEnv *env, jobject o) {
  jclass cls;
  jmethodID mid;

  cls=(*env)->GetObjectClass(env,o);
  if (!cls) {
      jri_error("RtoString: can't determine class of the object");
      return 0;
  }
  mid=(*env)->GetMethodID(env, cls, "toString", "()Ljava/lang/String;");
  if (!mid) {
      jri_error("RtoString: toString not found for the object");
      return 0;
  }
  return (jstring)(*env)->CallObjectMethod(env, o, mid);  
}

SEXP jri_getObjectArray(JNIEnv *env, jarray o) {
  SEXP ar;
  jobject el;
  int l,i;
  jint *ap;

  profStart();
  rjprintf(" jarray %d\n",o);
  if (!o) return R_NilValue;
  l=(int)(*env)->GetArrayLength(env, o);
  rjprintf("convert object array of length %d\n",l);
  if (l<1) return R_NilValue;
  PROTECT(ar=allocVector(INTSXP,l));
  i=0;
  while (i<l) {
    INTEGER(ar)[i]=(int)(*env)->GetObjectArrayElement(env, o, i);
    i++;
  }
  UNPROTECT(1);
  profReport("RgetObjectArrayCont[%d]:",o);
  return ar;
}

/** get contents of the object array in the form of int* */
SEXP jri_getStringArray(JNIEnv *env, jarray o) {
  SEXP ar;
  jobject el;
  int l,i;
  jint *ap;
  const char *c;

  profStart();
  rjprintf(" jarray %d\n",o);
  if (!o) return R_NilValue;
  l=(int)(*env)->GetArrayLength(env, o);
  rjprintf("convert string array of length %d\n",l);
  if (l<1) return R_NilValue;
  PROTECT(ar=allocVector(STRSXP,l));
  i=0;
  while (i<l) {
    jobject sobj=(*env)->GetObjectArrayElement(env, o, i);
    c=0;
    if (sobj) {
      /* we could (should?) check the type here ...
      if (!(*env)->IsInstanceOf(env, sobj, javaStringClass)) {
	printf(" not a String\n");
      } else
      */
      c=(*env)->GetStringUTFChars(env, sobj, 0);
    }
    if (!c)
      SET_STRING_ELT(ar, i, R_NaString);
    else {
      SET_STRING_ELT(ar, i, mkChar(c));
      (*env)->ReleaseStringUTFChars(env, sobj, c);
    }
    i++;
  }
  UNPROTECT(1);
  profReport("RgetStringArrayCont[%d]:",o);
  return ar;
}

/** get contents of the integer array object (int) */
SEXP jri_getIntArray(JNIEnv *env, jarray o) {
  SEXP ar;
  int l;
  jint *ap;

  profStart();
  rjprintf(" jarray %d\n",o);
  if (!o) return R_NilValue;
  l=(int)(*env)->GetArrayLength(env, o);
  rjprintf("convert int array of length %d\n",l);
  if (l<1) return R_NilValue;
  ap=(jint*)(*env)->GetIntArrayElements(env, o, 0);
  if (!ap) {
      jri_error("RgetIntArrayCont: can't fetch array contents");
      return 0;
  }
  PROTECT(ar=allocVector(INTSXP,l));
  memcpy(INTEGER(ar),ap,sizeof(jint)*l);
  UNPROTECT(1);
  (*env)->ReleaseIntArrayElements(env, o, ap, 0);
  profReport("RgetIntArrayCont[%d]:",o);
  return ar;
}

/** get contents of the integer array object (int) into a logical R vector */
SEXP jri_getBoolArrayI(JNIEnv *env, jarray o) {
  SEXP ar;
  int l;
  jint *ap;

  profStart();
  rjprintf(" jarray %d\n",o);
  if (!o) return R_NilValue;
  l=(int)(*env)->GetArrayLength(env, o);
  rjprintf("convert int array of length %d into R bool\n",l);
  if (l<1) return R_NilValue;
  ap=(jint*)(*env)->GetIntArrayElements(env, o, 0);
  if (!ap) {
      jri_error("RgetBoolArrayICont: can't fetch array contents");
      return 0;
  }
  PROTECT(ar=allocVector(LGLSXP,l));
  memcpy(LOGICAL(ar),ap,sizeof(jint)*l);
  UNPROTECT(1);
  (*env)->ReleaseIntArrayElements(env, o, ap, 0);
  profReport("RgetBoolArrayICont[%d]:",o);
  return ar;
}

/** get contents of the boolean array object into a logical R vector */
SEXP jri_getBoolArray(JNIEnv *env, jarray o) {
  SEXP ar;
  int l;
  jboolean *ap;

  profStart();
  rjprintf(" jarray %d\n",o);
  if (!o) return R_NilValue;
  l=(int)(*env)->GetArrayLength(env, o);
  rjprintf("convert boolean array of length %d into R bool\n",l);
  if (l<1) return R_NilValue;
  ap=(jboolean*)(*env)->GetBooleanArrayElements(env, o, 0);
  if (!ap) {
      jri_error("RgetBoolArrayCont: can't fetch array contents");
      return 0;
  }
  PROTECT(ar=allocVector(LGLSXP,l));
  {
    int i=0;
    int *lgl = LOGICAL(ar);
    while (i<l) { lgl[i]=ap[i]?1:0; i++; }
  }
  UNPROTECT(1);
  (*env)->ReleaseBooleanArrayElements(env, o, ap, 0);
  profReport("RgetBoolArrayCont[%d]:",o);
  return ar;
}

SEXP jri_getSEXPLArray(JNIEnv *env, jarray o) {
    SEXP ar;
    int l,i=0;
    jlong *ap;
    
    profStart();
    rjprintf(" jarray %d\n",o);
    if (!o) return R_NilValue;
    l=(int)(*env)->GetArrayLength(env, o);
    rjprintf("convert SEXPL array of length %d\n",l);
    if (l<1) return R_NilValue;
    ap=(jlong*)(*env)->GetLongArrayElements(env, o, 0);
    if (!ap) {
        jri_error("getSEXPLArray: can't fetch array contents");
        return 0;
    }
    PROTECT(ar=allocVector(VECSXP,l));
    while (i<l) {
        SET_VECTOR_ELT(ar, i, L2SEXP(ap[i]));
        i++;
    }
    UNPROTECT(1);
    (*env)->ReleaseLongArrayElements(env, o, ap, 0);
    profReport("jri_getSEXPLArray[%d]:",o);
    return ar;
}

/** get contents of the double array object (int) */
SEXP jri_getDoubleArray(JNIEnv *env, jarray o) {
  SEXP ar;
  int l;
  jdouble *ap;

  profStart();
  rjprintf(" jarray %d\n",o);
  if (!o) return R_NilValue;
  l=(int)(*env)->GetArrayLength(env, o);
  rjprintf("convert double array of length %d\n",l);
  if (l<1) return R_NilValue;
  ap=(jdouble*)(*env)->GetDoubleArrayElements(env, o, 0);
  if (!ap) {
      jri_error("RgetDoubleArrayCont: can't fetch array contents");
      return 0;
  }
  PROTECT(ar=allocVector(REALSXP,l));
  memcpy(REAL(ar),ap,sizeof(jdouble)*l);
  UNPROTECT(1);
  (*env)->ReleaseDoubleArrayElements(env, o, ap, 0);
  profReport("RgetDoubleArrayCont[%d]:",o);
  return ar;
}

void jri_checkExceptions(JNIEnv *env, int describe)
{
    jthrowable t=(*env)->ExceptionOccurred(env);
    if (t) {
#ifndef JRI_DEBUG
        if (describe)
#endif
            (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
}

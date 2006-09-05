/* Rengine - implements native rni methods called from the Rengine class */
#include <stdio.h>

#include "jri.h"
#include "org_rosuda_JRI_Rengine.h"
#include <R_ext/Parse.h>

#include "Rcallbacks.h"
#include "Rinit.h"
#include "globals.h"

#ifdef Win32
#include <windows.h>
#ifdef _MSC_VER
__declspec(dllimport) int UserBreak;
#else
#define UserBreak     (*_imp__UserBreak)
extern int UserBreak;
#endif
#else
/* for R_runHandlers */
#include <R_ext/eventloop.h>
#include <signal.h>
#include <unistd.h>
#endif

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniGetVersion
(JNIEnv *env, jclass this)
{
    return (jlong) JRI_API;
}

JNIEXPORT jint JNICALL Java_org_rosuda_JRI_Rengine_rniSetupR
  (JNIEnv *env, jobject this, jobjectArray a)
{
      int initRes;
      char *fallbackArgv[]={"Rengine",0};
      char **argv=fallbackArgv;
      int argc=1;
      
#ifdef JRI_DEBUG
      printf("rniSetupR\n");
#endif
	  
      engineObj=(*env)->NewGlobalRef(env, this);
      engineClass=(*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, engineObj));
      eenv=env;
      
      if (a) { // retrieve the content of the String[] and construct argv accordingly
          int len = (int)(*env)->GetArrayLength(env, a);
          if (len>0) {              
              int i=0;
              argv=(char**) malloc(sizeof(char*)*(len+2));
              argv[0]=fallbackArgv[0];
              while (i<len) {
                  jobject o=(*env)->GetObjectArrayElement(env, a, i);
                  i++;
                  if (o) {
                      const char *c;
                      c=(*env)->GetStringUTFChars(env, o, 0);
                      if (!c)
                          argv[i]="";
                      else {
                          argv[i]=(char*) malloc(strlen(c)+1);
                          strcpy(argv[i],c);
                          (*env)->ReleaseStringUTFChars(env, o, c);
                      }
                  } else
                      argv[i]="";
              }
              argc=len+1;
              argv[argc]=0;
          }
      }
      initRes=initR(argc, argv);
      // we don't release the argv in case R still needs it later (even if it shouldn't), but it's not really a significant leak
      
      return initRes;
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniParse
  (JNIEnv *env, jobject this, jstring str, jint parts)
{
      ParseStatus ps;
      SEXP pstr, cv;

      PROTECT(cv=jri_getString(env, str));
#ifdef JRI_DEBUG
      printf("parsing \"%s\"\n", CHAR(STRING_ELT(cv,0)));
#endif
      pstr=R_ParseVector(cv, parts, &ps);
#ifdef JRI_DEBUG
      printf("parse status=%d, result=%x, type=%d\n", ps, (int) pstr, (pstr!=0)?TYPEOF(pstr):0);
#endif
      UNPROTECT(1);

      return SEXP2L(pstr);
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniEval
  (JNIEnv *env, jobject this, jlong exp, jlong rho)
{
      SEXP es, exps=L2SEXP(exp);
      int er=0;
      int i=0,l;

      if (exp<1) return -1;

      if (TYPEOF(exps)==EXPRSXP) { /* if the object is a list of exps, eval them one by one */
          l=LENGTH(exps);
          while (i<l) {
              es=R_tryEval(VECTOR_ELT(exps,i), R_GlobalEnv, &er);
              i++;
          }
      } else
          es=R_tryEval(exps, R_GlobalEnv, &er);

      return SEXP2L(es);
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniAssign
(JNIEnv *env, jobject this, jstring symName, jlong valL, jlong rhoL)
{
    SEXP sym, val, rho;
    
    sym = jri_installString(env, symName);
    if (!sym || sym==R_NilValue) return;

    rho=(rhoL==0)?R_GlobalEnv:L2SEXP(rhoL);
    val=(valL==0)?R_NilValue:L2SEXP(valL);
   
    defineVar(sym, val, rho);
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniProtect
(JNIEnv *env, jobject this, jlong exp)
{
	PROTECT(L2SEXP(exp));
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniUnprotect
(JNIEnv *env, jobject this, jint count)
{
	UNPROTECT(count);
}

JNIEXPORT jobject JNICALL Java_org_rosuda_JRI_Rengine_rniXrefToJava
(JNIEnv *env, jobject this, jlong exp)
{
	SEXP xp = L2SEXP(exp);
	if (TYPEOF(xp) != EXTPTRSXP) return 0;
	return (jobject) EXTPTR_PTR(xp);
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniJavaToXref
(JNIEnv *env, jobject this, jobject o)
{
	// this is pretty much from Rglue.c of rJava
	return SEXP2L(R_MakeExternalPtr(o, R_NilValue, R_NilValue));
}

JNIEXPORT jstring JNICALL Java_org_rosuda_JRI_Rengine_rniGetString
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putString(env, L2SEXP(exp), 0);
}


JNIEXPORT jobjectArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetStringArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putStringArray(env, L2SEXP(exp));
}

JNIEXPORT jintArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetIntArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putIntArray(env, L2SEXP(exp));
}

JNIEXPORT jintArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetDoubleArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putDoubleArray(env, L2SEXP(exp));
}

JNIEXPORT jlongArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetVector
(JNIEnv *env, jobject this, jlong exp)
{
    return jri_putSEXPLArray(env, L2SEXP(exp));
}

JNIEXPORT jint JNICALL Java_org_rosuda_JRI_Rengine_rniExpType
  (JNIEnv *env, jobject this, jlong exp)
{
    return (exp<0)?0:TYPEOF(L2SEXP(exp));
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniIdle
  (JNIEnv *env, jobject this)
{
#ifndef Win32
    R_runHandlers(R_InputHandlers, R_checkActivity(0, 1));
#endif
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniRunMainLoop
  (JNIEnv *env, jobject this)
{
      run_Rmainloop();
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniCustomLoop
  (JNIEnv *env, jobject this)
{
		printf("C: starting REPL loop\n");
		jboolean interrupted = 0;

		// Get the instance of the java Rengine, and the isInterrupted method
		jclass cls = (*env)->GetObjectClass(env, this);
		jmethodID mid = (*env)->GetMethodID(env, cls, "isInterrupted", "()Z");
		if(mid == 0) {
			return;
		}
		R_ReplDLLinit();
		while(!interrupted) {
			printf("C: not interrupted: iteration\n");
			int state = R_ReplDLLdo1();
		 	printf("C: checking interrupt");
			interrupted = (*env)->CallBooleanMethod(env, this, mid, 1);
		}
		printf("C: Ending.....\n");
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniPutString
(JNIEnv *env, jobject this, jstring s)
{
    return SEXP2L(jri_getString(env, s));
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniPutStringArray
(JNIEnv *env, jobject this, jobjectArray a)
{
    return SEXP2L(jri_getStringArray(env, a));
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniPutIntArray
(JNIEnv *env, jobject this, jintArray a)
{
    return SEXP2L(jri_getIntArray(env, a));
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniPutDoubleArray
(JNIEnv *env, jobject this, jdoubleArray a)
{
    return SEXP2L(jri_getDoubleArray(env, a));
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniPutVector
(JNIEnv *env, jobject this, jlongArray a)
{
    return SEXP2L(jri_getSEXPLArray(env, a));
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniGetAttr
(JNIEnv *env, jobject this, jlong exp, jstring name)
{
    SEXP an = jri_installString(env, name);
    if (!an || an==R_NilValue || exp==0 || L2SEXP(exp)==R_NilValue) return 0;
    {
        SEXP a = getAttrib(L2SEXP(exp), an);
        return (a==R_NilValue)?0:SEXP2L(a);
    }
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniSetAttr
(JNIEnv *env, jobject this, jlong exp, jstring aName, jlong attr)
{
    SEXP an = jri_installString(env, aName);
    if (!an || an==R_NilValue || exp==0 || L2SEXP(exp)==R_NilValue) return;

    setAttrib(L2SEXP(exp), an, (attr==0)?R_NilValue:L2SEXP(attr));
    
    // this is not official API, but whoever uses this should know what he's doing
    // it's ok for directly constructing attr lists, and that's what it should be used for
    //SET_ATTRIB(L2SEXP(exp), (attr==0)?R_NilValue:L2SEXP(attr));
    
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniInstallSymbol
(JNIEnv *env, jobject this, jstring s)
{
    return SEXP2L(jri_installString(env, s));
}

JNIEXPORT jstring JNICALL Java_org_rosuda_JRI_Rengine_rniGetSymbolName
(JNIEnv *env, jobject this, jlong exp)
{
	return jri_putSymbolName(env, L2SEXP(exp));
}

JNIEXPORT jboolean JNICALL Java_org_rosuda_JRI_Rengine_rniInherits
(JNIEnv *env, jobject this, jlong exp, jstring s)
{
	jboolean res = 0;
	const char *c;
	c=(*env)->GetStringUTFChars(env, s, 0);
	if (c) {
		if (inherits(L2SEXP(exp), (char*)c)) res = 1;
		(*env)->ReleaseStringUTFChars(env, s, c);
	}
	return res;
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniCons
(JNIEnv *env, jobject this, jlong head, jlong tail)
{
    return SEXP2L(CONS((head==0)?R_NilValue:L2SEXP(head), (tail==0)?R_NilValue:L2SEXP(tail)));
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniCAR
(JNIEnv *env, jobject this, jlong exp)
{
    if (exp) {
        SEXP r = CAR(L2SEXP(exp));
        return (r==R_NilValue)?0:SEXP2L(r);
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniCDR
(JNIEnv *env, jobject this, jlong exp)
{
    if (exp) {
        SEXP r = CDR(L2SEXP(exp));
        return (r==R_NilValue)?0:SEXP2L(r);
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniTAG
(JNIEnv *env, jobject this, jlong exp)
{
    if (exp) {
        SEXP r = TAG(L2SEXP(exp));
        return (r==R_NilValue)?0:SEXP2L(r);
    }
    return 0;
}

// creates a list from SEXPs provided in long[]
JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniPutList
(JNIEnv *env, jobject this, jlongArray o)
{
    SEXP t=R_NilValue;
    int l,i=0;
    jlong *ap;
    
    if (!o) return 0;
    l=(int)(*env)->GetArrayLength(env, o);
    if (l<1) return SEXP2L(CONS(R_NilValue, R_NilValue));
    ap=(jlong*)(*env)->GetLongArrayElements(env, o, 0);
    if (!ap) return 0;
    
    while(i<l) {
        t=CONS((ap[i]==0)?R_NilValue:L2SEXP(ap[i]), t);
        i++;
    }
    (*env)->ReleaseLongArrayElements(env, o, ap, 0);    
    
    return SEXP2L(t);
}

// retrieves a list (shallow copy) and returns the SEXPs in long[]
JNIEXPORT jlongArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetList
(JNIEnv *env, jobject this, jlong exp)
{
    SEXP e=L2SEXP(exp);
    
    if (exp==0 || e==R_NilValue) return 0;

    {
        unsigned len=0;
        SEXP t=e;
        
        while (t!=R_NilValue) { t=CDR(t); len++; };
        
        {
            jlongArray da=(*env)->NewLongArray(env,len);
            jlong *dae;
        
            if (!da) return 0;
        
            if (len>0) {
                int i=0;
                dae=(*env)->GetLongArrayElements(env, da, 0);
                if (!dae) {
                    (*env)->DeleteLocalRef(env,da);
                    jri_error("rniGetList: newLongArray.GetLongArrayElements failed");
                    return 0;
                }

                t=e;
                while (t!=R_NilValue && i<len) {
                    dae[i]=(CAR(t)==R_NilValue)?0:SEXP2L(CAR(t));
                    i++; t=CDR(t);
                }
                
                (*env)->ReleaseLongArrayElements(env, da, dae, 0);
            }
            
            return da;
        }
    }
    
}

/* by default those are disabled as it's a problem on Win32 ... */
#ifdef JRI_ENV_CALLS

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniSetEnv
(JNIEnv *env, jclass this, jstring key, jstring val) {
    const char *cKey, *cVal;
    if (!key || !val) return;
    cKey=(*env)->GetStringUTFChars(env, key, 0);
    cVal=(*env)->GetStringUTFChars(env, val, 0);
    if (!cKey || !cVal) {
        jri_error("rniSetEnv: can't retrieve key/value content");
        return;
    }
#ifdef Win32
    SetEnvironmentVariable(cKey, cVal);
#else
    setenv(cKey, cVal, 1);
#endif
    (*env)->ReleaseStringUTFChars(env, key, cKey);
    (*env)->ReleaseStringUTFChars(env, val, cVal);
}

JNIEXPORT jstring JNICALL Java_org_rosuda_JRI_Rengine_rniGetEnv
(JNIEnv *env, jclass this, jstring key) {
    const char *cKey, *cVal;
    if (!key) return;
    cKey=(*env)->GetStringUTFChars(env, key, 0);
    if (!cKey) {
        jri_error("rniSetEnv: can't retrieve key/value content");
        return;
    }
    cVal=getenv(cKey);
    (*env)->ReleaseStringUTFChars(env, key, cKey);
    if (!cVal) return 0;
    return (*env)->NewStringUTF(env, cVal);
}

#endif

JNIEXPORT jint JNICALL Java_org_rosuda_JRI_Rengine_rniStop
(JNIEnv *env, jobject this, jint flag) {
#ifdef Win32
    UserBreak=1;
#else
    /* not really a perfect solution ... need to clarify what's the best ... */
    kill(getpid(), SIGINT);
#endif
}



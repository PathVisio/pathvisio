#include <Rversion.h>
#include <jni.h>
#include "rversion_GetRVersion.h"

JNIEXPORT jstring JNICALL Java_rversion_GetRVersion_rniGetVersionR
(JNIEnv  *env, jclass this)
{
	char Rversion[25];
	sprintf(Rversion, "%s.%s", R_MAJOR, R_MINOR);
	return (*env)-> NewStringUTF(env,Rversion);
}

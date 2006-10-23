#include <Rversion.h>
#include <R.h>
#include <Rinternals.h>
#include "Rinit.h"
#include "Rcallbacks.h"
#include "Rdecl.h"

/*-------------------------------------------------------------------*
 * UNIX initialization (includes Darwin/Mac OS X)                    *
 *-------------------------------------------------------------------*/

#ifndef Win32

/* more recent R versions have Rinterface */
#if (R_VERSION >= R_Version(2,3,0))
#define R_INTERFACE_PTRS 1
#define CSTACK_DEFNS 1
#include <Rinterface.h>
/* unfortunately 2.3.0 doesn't export R_CStackLimit */
#ifndef RIF_HAS_CSTACK
#if !defined(HAVE_UINTPTR_T) && !defined(uintptr_t)
typedef unsigned long uintptr_t;
#endif
extern uintptr_t R_CStackLimit; /* C stack limit */
extern uintptr_t R_CStackStart; /* Initial stack address */
#endif
/* and SaveAction is not officially exported */
extern SA_TYPE SaveAction;
#else
/*---------------------------------------------------- old R init --*/
/* for older version we need to copy/paste it */
/* from Defn.h */
extern Rboolean R_Interactive;   /* TRUE during interactive use*/

extern FILE*    R_Consolefile;   /* Console output file */
extern FILE*    R_Outputfile;   /* Output file */
extern char*    R_TempDir;   /* Name of per-session dir */

typedef enum {
    SA_NORESTORE,/* = 0 */
    SA_RESTORE,
    SA_DEFAULT,/* was === SA_RESTORE */
    SA_NOSAVE,
    SA_SAVE,
    SA_SAVEASK,
    SA_SUICIDE
} SA_TYPE;

extern SA_TYPE SaveAction;

/* from src/unix/devUI.h */

extern void (*ptr_R_Suicide)(char *);
extern void (*ptr_R_ShowMessage)();
extern int  (*ptr_R_ReadConsole)(char *, unsigned char *, int, int);
extern void (*ptr_R_WriteConsole)(char *, int);
extern void (*ptr_R_ResetConsole)();
extern void (*ptr_R_FlushConsole)();
extern void (*ptr_R_ClearerrConsole)();
extern void (*ptr_R_Busy)(int);
/* extern void (*ptr_R_CleanUp)(SA_TYPE, int, int); */
extern int  (*ptr_R_ShowFiles)(int, char **, char **, char *, Rboolean, char *);
extern int  (*ptr_R_ChooseFile)(int, char *, int);
extern void (*ptr_R_loadhistory)(SEXP, SEXP, SEXP, SEXP);
extern void (*ptr_R_savehistory)(SEXP, SEXP, SEXP, SEXP);
#endif
/*-------------------------------------- end of old definitions ----*/


int initR(int argc, char **argv) {
    structRstart rp;
    Rstart Rp = &rp;
    /* getenv("R_HOME","/Library/Frameworks/R.framework/Resources",1); */
    if (!getenv("R_HOME")) {
        fprintf(stderr, "R_HOME is not set. Please set all required environment variables before running this program.\n");
        return -1;
    }

    /* this is probably unnecessary, but we could set any other parameters here */
    R_DefParams(Rp);
    Rp->NoRenviron = 0;
    R_SetParams(Rp);

#ifdef RIF_HAS_RSIGHAND
    R_SignalHandlers=0;
#endif
    {
      int stat=Rf_initialize_R(argc, argv);
      if (stat<0) {
        printf("Failed to initialize embedded R! (stat=%d)\n",stat);
        return -1;
      }
    }

#ifdef RIF_HAS_RSIGHAND
    R_SignalHandlers=0;
#endif
#if (R_VERSION >= R_Version(2,3,0))
    /* disable stack checking, because threads will thow it off */
    R_CStackLimit = (uintptr_t) -1;
#endif

#ifdef JGR_DEBUG
    printf("R primary initialization done. Setting up parameters.\n");
#endif

    R_Outputfile = NULL;
    R_Consolefile = NULL;
    R_Interactive = 1;
    SaveAction = SA_SAVEASK;

    /* ptr_R_Suicide = Re_Suicide; */
    /* ptr_R_CleanUp = Re_CleanUp; */
    ptr_R_ShowMessage = Re_ShowMessage;
    ptr_R_ReadConsole = Re_ReadConsole;
    ptr_R_WriteConsole = Re_WriteConsole;
    ptr_R_ResetConsole = Re_ResetConsole;
    ptr_R_FlushConsole = Re_FlushConsole;
    ptr_R_ClearerrConsole = Re_ClearerrConsole;
    ptr_R_Busy = Re_Busy;
    ptr_R_ShowFiles = Re_ShowFiles;
    ptr_R_ChooseFile = Re_ChooseFile;
	ptr_R_loadhistory = Re_loadhistory;
    ptr_R_savehistory = Re_savehistory;

#ifdef JGR_DEBUG
	printf("Setting up R event loop\n");
#endif

    setup_Rmainloop();

#ifdef JGR_DEBUG
    printf("R initialized.\n");
#endif

    return 0;
}

#else

/*-------------------------------------------------------------------*
 * Windows initialization is different and uses Startup.h            *
 *-------------------------------------------------------------------*/

#define NONAMELESSUNION
#include <windows.h>
#include <winreg.h>
#include <stdio.h>
#include "Rversion.h"
#if R_VERSION < R_Version(2,1,0)
#include <config.h>
#include "Startup.h"
#else
/* since 2.1.0 we don't need R sources */
#include "R_ext/RStartup.h"
#endif

#if (R_VERSION >= R_Version(2,3,0))
/* according to fixed/config.h Windows has uintptr_t, my windows hasn't */
#if !defined(HAVE_UINTPTR_T) && !defined(uintptr_t)
typedef unsigned long uintptr_t;
#endif
extern uintptr_t R_CStackLimit; /* C stack limit */
extern uintptr_t R_CStackStart; /* Initial stack address */
#endif

/* for signal-handling code */
/* #include "psignal.h" - it's not included, so just get SIGBREAK */
#define	SIGBREAK 21	/* to readers pgrp upon background tty read */

/* one way to allow user interrupts: called in ProcessEvents */
#ifdef _MSC_VER
__declspec(dllimport) int UserBreak;
#else
#define UserBreak     (*_imp__UserBreak)
extern int UserBreak;
#endif

/* calls into the R DLL */
extern char *getDLLVersion();
extern void R_DefParams(Rstart);
extern void R_SetParams(Rstart);
extern void setup_term_ui(void);
extern void ProcessEvents(void);
extern void end_Rmainloop(void), R_ReplDLLinit(void);
extern int R_ReplDLLdo1();
extern void run_Rmainloop(void);

void myCallBack()
{
    /* called during i/o, eval, graphics in ProcessEvents */
}

#ifndef YES
#define YES    1
#endif
#ifndef NO
#define NO    -1
#endif
#ifndef CANCEL
#define CANCEL 0
#endif

int myYesNoCancel(char *s)
{
    char  ss[128];
    unsigned char a[3];

    sprintf(ss, "%s [y/n/c]: ", s);
    Re_ReadConsole(ss, a, 3, 0);
    switch (a[0]) {
    case 'y':
    case 'Y':
	return YES;
    case 'n':
    case 'N':
	return NO;
    default:
	return CANCEL;
    }
}

static void my_onintr(int sig)
{
    UserBreak = 1;
}

static char Rversion[25], RUser[MAX_PATH], RHome[MAX_PATH];

int initR(int argc, char **argv)
{
    structRstart rp;
    Rstart Rp = &rp;
    char *p;
    char rhb[MAX_PATH+10];
    LONG h;
    DWORD t,s=MAX_PATH;
    HKEY k;
    int cvl;

    sprintf(Rversion, "%s.%s", R_MAJOR, R_MINOR);
    cvl=strlen(R_MAJOR)+2;
    if(strncmp(getDLLVersion(), Rversion, cvl) != 0) {
        char msg[512];
	sprintf(msg, "Error: R.DLL version does not match (DLL: %s, expecting: %s)\n", getDLLVersion(), Rversion);
	fprintf(stderr, msg);
	MessageBox(0, msg, "Version mismatch", MB_OK|MB_ICONERROR);
	return -1;
    }

    R_DefParams(Rp);
    if(getenv("R_HOME")) {
	strcpy(RHome, getenv("R_HOME"));
    } else { /* fetch R_HOME from the registry */
	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,"SOFTWARE\\R-core\\R",0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
	    RegQueryValueEx(k,"InstallPath",0,&t,RHome,&s)!=ERROR_SUCCESS) {
	    fprintf(stderr, "R_HOME must be set or R properly installed (\\Software\\R-core\\R\\InstallPath registry entry must exist).\n");
	    MessageBox(0, "R_HOME must be set or R properly installed (\\Software\\R-core\\R\\InstallPath registry entry must exist).\n", "Can't find R home", MB_OK|MB_ICONERROR);
	    return -2;
	};
	sprintf(rhb,"R_HOME=%s",RHome);
	putenv(rhb);
    }
    /* on Win32 this should set R_Home (in R_SetParams) as well */
    Rp->rhome = RHome;
    /*
     * try R_USER then HOME then working directory
     */
    if (getenv("R_USER")) {
	strcpy(RUser, getenv("R_USER"));
    } else if (getenv("HOME")) {
	strcpy(RUser, getenv("HOME"));
    } else if (getenv("HOMEDIR")) {
	strcpy(RUser, getenv("HOMEDIR"));
	strcat(RUser, getenv("HOMEPATH"));
    } else
	GetCurrentDirectory(MAX_PATH, RUser);
    p = RUser + (strlen(RUser) - 1);
    if (*p == '/' || *p == '\\') *p = '\0';
    Rp->home = RUser;
    Rp->ReadConsole = Re_ReadConsole;
    Rp->WriteConsole = Re_WriteConsole;

#if R_VERSION >= R_Version(2,1,0)
    Rp->Busy = Re_Busy;
    Rp->ShowMessage = Re_ShowMessage;
    Rp->YesNoCancel = myYesNoCancel;
#else
    Rp->busy = Re_Busy;
    Rp->message = Re_ShowMessage;
    Rp->yesnocancel = myYesNoCancel;
#endif   
    Rp->CallBack = myCallBack;
	Rp->CharacterMode = LinkDLL;

    Rp->R_Quiet = FALSE;
    Rp->R_Interactive = TRUE;
    Rp->RestoreAction = SA_RESTORE;
    Rp->SaveAction = SA_SAVEASK;
#if R_VERSION < R_Version(2,0,0)
    Rp->CommandLineArgs = argv;
    Rp->NumCommandLineArgs = argc;
#else
    R_set_command_line_arguments(argc, argv);
#endif

    /* Rp->nsize = 300000;
    Rp->vsize = 6e6; */
    R_SetParams(Rp); /* so R_ShowMessage is set */
    R_SizeFromEnv(Rp);
    R_SetParams(Rp);

#if (R_VERSION >= R_Version(2,3,0))
    /* R_SetParams implicitly calls R_SetWin32 which sets the
       stack start/limit which we need to override */
    R_CStackLimit = (uintptr_t) -1;
#endif

    FlushConsoleInputBuffer(GetStdHandle(STD_INPUT_HANDLE));

    signal(SIGBREAK, my_onintr);
    setup_term_ui();
    setup_Rmainloop();

    return 0;
}
#endif

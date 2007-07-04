#ifndef __R_CALLBACKS__H__
#define __R_CALLBACKS__H__

#include <R.h>
#include <Rinternals.h>

/* functions provided as R callbacks */

int  Re_ReadConsole(char *prompt, unsigned char *buf, int len, int addtohistory);
void Re_Busy(int which);
void Re_WriteConsole(char *buf, int len);
void Re_WriteConsoleEx(char *buf, int len, int oType);
void Re_ResetConsole();
void Re_FlushConsole();
void Re_ClearerrConsole();
int  Re_ChooseFile(int new, char *buf, int len);
void Re_ShowMessage(char *buf);
void Re_read_history(char *buf);
void Re_loadhistory(SEXP call, SEXP op, SEXP args, SEXP env);
void Re_savehistory(SEXP call, SEXP op, SEXP args, SEXP env);
int  Re_ShowFiles(int nfile, char **file, char **headers, char *wtitle, Rboolean del, char *pager);

#endif

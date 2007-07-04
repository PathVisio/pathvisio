#include "rjava.h"
#include <unistd.h>

int ipcout;
int resin;
int *rjctrl = 0;

typedef void(callbackfn)(void *);

int RJava_request_lock() {
  long buf[4];
  int n;
  if (rjctrl && *rjctrl) return 2;

  buf[0] = IPCC_LOCK_REQUEST;
  write(ipcout, buf, sizeof(long));
  n = read(resin, buf, sizeof(long));
  return (buf[0] == IPCC_LOCK_GRANTED)?1:0;
}

int RJava_clear_lock() {
  long buf[4];
  buf[0] = IPCC_CLEAR_LOCK;
  write(ipcout, buf, sizeof(long));
  return 1;
}

void RJava_request_callback(callbackfn *fn, void *data) {
  long buf[4];
  buf[0] = IPCC_CALL_REQUEST;
  buf[1] = (long) fn;
  buf[2] = (long) data;
  write(ipcout, buf, sizeof(long)*3);
}

void RJava_setup(int _in, int _out) {
  long buf[4];
  ipcout = _out;
  resin = _in;
}

void RJava_init_ctrl() {
  long buf[4];
  buf[0] = IPCC_CONTROL_ADDR;
  write(ipcout, buf, sizeof(long));
  read(resin, buf, sizeof(long)*2);
  if (buf[0] == IPCC_CONTROL_ADDR) {
    rjctrl= (int*) buf[1];
  }
}

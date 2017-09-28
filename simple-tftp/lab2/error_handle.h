#ifndef _ERROR_HANDLE
#define _ERROR_HANDLE
#include <errno.h>
#include <string.h>
#include <stdio.h>
#define error_handle(call) if( (call) == -1){ \
	fprintf(stderr, "ERROR: %s (errno:%d) \t--line %d in %s\n", \
		strerror(errno), errno, __LINE__, __FILE__);\
	exit(1); \
}
#endif

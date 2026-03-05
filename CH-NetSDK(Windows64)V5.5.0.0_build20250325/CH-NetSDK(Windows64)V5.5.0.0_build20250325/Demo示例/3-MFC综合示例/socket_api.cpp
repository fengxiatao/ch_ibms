#include "stdafx.h"
#include "socket_api.h"
#include "RetValue.h"
#include <Windows.h>

//define function pointer
typedef int	(__stdcall* psocket_inet_pton)(int af, const char *csrc, void *dst);
typedef const char* (__stdcall* psocket_inet_ntop)(int af, const void *src, char *dst, unsigned int cnt);


//Define static function pointer variable for interface export
static HMODULE s_socket_hdl = NULL;
static psocket_inet_pton s_psocket_inet_pton = NULL;
static psocket_inet_ntop s_psocket_inet_ntop = NULL;

int load_socket_lib()
{
	if (NULL == s_socket_hdl) {
		s_socket_hdl = LoadLibrary("ws2_32.dll");
		if(NULL == s_socket_hdl) {
				return RET_FAILED;
			}
	}

	s_psocket_inet_pton	= (psocket_inet_pton)GetProcAddress(s_socket_hdl, "inet_pton");
	s_psocket_inet_ntop = (psocket_inet_ntop)GetProcAddress(s_socket_hdl, "inet_ntop");

	return RET_SUCCESS;
}

void free_socket_lib()
{
	if(NULL != s_socket_hdl) {
		FreeLibrary(s_socket_hdl);
		s_socket_hdl = NULL;
	}
}

int api_socket_inet_pton(int af, const char *csrc, void *dst)
{
	if(NULL == s_psocket_inet_pton)
	{
		return NULL;
	}
	return s_psocket_inet_pton(af, csrc, dst);
}

const char* api_psocket_inet_ntop(int af, const void *src, char *dst, unsigned int cnt)
{
	if(NULL == s_psocket_inet_ntop)
	{
		return NULL;
	}
	return s_psocket_inet_ntop(af, src, dst, cnt);
}


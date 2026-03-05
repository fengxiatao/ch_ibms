#ifndef __SOCKET_API_H__
#define __SOCKET_API_H__

int load_socket_lib();
void free_socket_lib();

int api_socket_inet_pton(int af, const char *csrc, void *dst);
const char* api_psocket_inet_ntop(int af, const void *src, char *dst, unsigned int cnt);

#endif


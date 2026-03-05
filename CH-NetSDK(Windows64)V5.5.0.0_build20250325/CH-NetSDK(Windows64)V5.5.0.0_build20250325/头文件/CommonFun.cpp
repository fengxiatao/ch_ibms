#include "Commonfun.h"

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#ifndef  WIN32
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <net/if.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/times.h>
#include <sys/time.h>
#include <stdint.h>
#include <dlfcn.h>
#endif

int ToIntDef(const char* _pstrFrom, int _iDef)
{
	if (NULL == _pstrFrom || strlen(_pstrFrom) <= 0)
	{
		return _iDef;
	}
	size_t i=0;
	if (0 == memcmp("-",_pstrFrom,1))
	{
		i++;
	}
	for (; i<strlen(_pstrFrom); i++)
	{
		if (!isdigit(_pstrFrom[i]))
		{
			return _iDef;
		}
	}

	int iFlag = 10;
	if(NULL != strstr(_pstrFrom, "0x") || NULL != strstr(_pstrFrom, "0X"))
	{
		iFlag = 16;
	}

	return (int)strtoul(_pstrFrom, 0, iFlag);
}

#ifndef WIN32
int gets_s(char *_pcStr, int _iCount)
{
	if (_pcStr == NULL)
		return -1;

	char* pcRet = fgets(_pcStr, _iCount, stdin);
	size_t uiLen = strlen(_pcStr);
	if (pcRet == NULL || uiLen == 0)
		return -2;

	if (_iCount - 1 > (int)uiLen || _pcStr[uiLen-1] == '\n')
	{
		_pcStr[uiLen-1] = '\0';
	}

	stdin->_IO_read_ptr = stdin->_IO_read_end;
	return 0;
}
#else	
#endif

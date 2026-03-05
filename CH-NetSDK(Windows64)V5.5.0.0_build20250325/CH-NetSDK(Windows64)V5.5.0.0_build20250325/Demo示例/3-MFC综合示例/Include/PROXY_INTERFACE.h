#pragma once
#include "../Common/CommonFun.h"

namespace PROXY_INTERFACE
{
	int 	LoadProxySDK(const char* _pszPath = "ProxySdk.dll");
	bool    ReleaseProxySDK();

	TYPEDEF_FUNCTION(NetClient_ProxyStart)(int _iProxyPort, int _iType);
	DECLARE_FUNCTION(NetClient_ProxyStart);

	TYPEDEF_FUNCTION(NetClient_ProxyCleanup)();
	DECLARE_FUNCTION(NetClient_ProxyCleanup);

	TYPEDEF_FUNCTION(NetClient_ProxyDSMSetDirectoryServer)(char* _cDServerIP1, unsigned short _wPort1, char* _cDServerIP2, unsigned short _wPort2,
		char* _cAccount, char* _cPassword);
	DECLARE_FUNCTION(NetClient_ProxyDSMSetDirectoryServer);

	TYPEDEF_FUNCTION(NetClient_ProxyBindInterface)(char* _pcIP);
	DECLARE_FUNCTION(NetClient_ProxyBindInterface);

	TYPEDEF_FUNCTION(NetClient_ProxySetConfig)(int _iCmdID, void* _pvParaBuf, int _iBufSize);
	DECLARE_FUNCTION(NetClient_ProxySetConfig);
}
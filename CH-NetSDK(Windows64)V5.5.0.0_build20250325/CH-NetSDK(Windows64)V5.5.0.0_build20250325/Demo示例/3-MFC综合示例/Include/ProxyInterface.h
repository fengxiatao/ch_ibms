#ifndef __PROXYINTERFACE__
#define __PROXYINTERFACE__
#include "ProxyTypes.h"

#ifndef WIN32
#define __stdcall
#endif

#ifdef __cplusplus
extern "C"  {
#endif
/************************************************************************/
/*
	NetClient_ProxyStart Initialize the port number passed in this interface will not change internally, use this port to listen directly
	_iProxyPort: listening port number
	_iType: TYPE_PROXY_ONLY or TYPE_PROXY_CLIENT
	
*/
/************************************************************************/
int __stdcall NetClient_ProxyStart(int _iProxyPort, int _iType); //The port number passed in by this interface will not change internally, just use this port to listen directly
int __stdcall NetClient_ProxySetWorkMode(int _iWorkMode);
int __stdcall NetClient_ProxyGetInterface(st_NetInterface *_stInterface);
int __stdcall NetClient_ProxyBindInterface(char *_cIP);
int __stdcall NetClient_ProxySetMaxInOutVideo(int _iMaxIn, int _iMaxOut);
int __stdcall NetClient_ProxySetNotifyFunc(NOTIFY_PROXYMAIN _ntyMAIN,NOTIFY_CLIENTCONNECT _ntyCONNECT,NOTIFY_USERSTRING _ntyUSERSTRING);
int __stdcall NetClient_ProxyStartup(int _iProxyPort = 3001);//The port number passed in by this interface will be incremented by 1 internally, and the port number after incrementing by 1 will be used to monitor

int __stdcall NetClient_ProxyCleanup();

int __stdcall NetClient_ProxyAddProxy(st_EncoderProxy *_pstEncoderProxy);
int __stdcall NetClient_ProxyDeleteProxy( int _iProxyID);

int __stdcall NetClient_ProxyGetEncoderList(PUIDLIST _pIDList);
int __stdcall NetClient_ProxyGetEncoderInfo(int _iProxyID, PENCODERINFO _pEncoderInfo);

int __stdcall NetClient_ProxyHasConnected(int _iProxyID, int _iChannel, int _iStreamNO = MAIN_STREAM);
int __stdcall NetClient_ProxyGetAllLinks(int _iProxyID, int _iChannel, PUIDLIST _pIDList, int _iStreamNO = MAIN_STREAM);
int __stdcall NetClient_ProxyGetLinkInfo(int _iProxyID, unsigned int _iConnectID, PLINKINFO _pLinkInfo);
int __stdcall NetClient_ProxyDeleteLink(int _iProxyID, unsigned int _iConnectID);
int __stdcall NetClient_ProxyCloseAllLinks( int _iProxyID);

int __stdcall NetClient_ProxyDSMSetDirectoryServer(char* _cDServerIP1, unsigned short _wPort1, char* _cDServerIP2, unsigned short _wPort2,
									char* _cAccount, char* _cPassword);

int __stdcall NetClient_ProxySendStringToClient(char* _cIpAddress, char* _cMsg, int _iLen);
int __stdcall NetClient_ProxySendStringToClientByID(int _iProxyID, unsigned int _iConnectID, char* _cMsg, int _iLen);

int __stdcall NetClient_ProxyIsActiveServer(int _iProxyID);
int __stdcall NetClient_ProxyAddProxyEx(char* _cIP, char* _proxy, int _iNetMode, char* _pcProID ,unsigned short _iPort, char* _pcUserName, char* _pcPassword);
int __stdcall NetClient_ProxySetPort(int _iServerPort, int _iClientPort);
int __stdcall NetClient_ProxyGetChannelNum(int _iProxyID, int *_iChannelNum);
int __stdcall NetCliet_ProxyGetLogonStatus(int _iProxyID);

int __stdcall NetClient_DSMSetDirectoryServer(char* _cDServerIP1, unsigned short _wPort1, char* _cDServerIP2, unsigned short _wPort2,
												   char* _cAccount, char* _cPassword);

/*******************************************************************************************
Function    : NetClient_ProxySetConfig
Description	: Proxy library settings configuration parameter interface
Prameter:    [IN]_iCmd: Configuration ID, different functions correspond to different _iCmdID
			 when [IN]_iCmdID == 0(CMD_PROXYCMD_DSMCFG):Set Directory Server Information
					[IN]_pvParaBuf: input directory server info, struct ProxyDsmCfg variable
					[IN]_iBufSize: the size of struct,  sizeof(ProxyDsmCfg)
			 when [IN]_iCmdID == 1(CMD_PROXYCMD_BINDIP):Set and bind the public IP address of the local proxy
					[IN]_pvParaBuf: input directory server info, struct ProxyBindIp variable
					[IN]_iBufSize: the size of struct,  sizeof(ProxyBindIp)
return       :	== 0: Success; other:Failed, Return Value Description by RetValue.h.
others       :	2019.11.18
********************************************************************************************/
int __stdcall NetClient_ProxySetConfig(int _iCmdID, void* _pvParaBuf, int _iBufSize);

/*******************************************************************************************
Function    : NetClient_ProxyGetConfig
Description	: Proxy library settings configuration parameter interface
Prameter:    [IN]_iCmd: Configuration ID, different functions correspond to different _iCmdID
			 when [IN]_iCmdID == 2(CMD_PROXYCMD_GET_ENCODER):Get encoding device information, including ipc and nvr
					[IN]_iProxyID: Valid parameters, pass in ProxyID
					[IN]_iConnectID:Connection ID, invalid parameter, just pass 0
					[OUT]_pvParaBuf: input directory server info, struct EncoderInfoEx variable
					[IN]_iBufSize: the size of struct,  sizeof(EncoderInfoEx)
			 when [IN]_iCmdID == 3(CMD_PROXYCMD_GET_LINK):Get proxy connection information
					[IN]_iProxyID: Valid parameters, pass in ProxyID
					[IN]_iConnectID:Connection ID, valid parameters, pass the corresponding connection ID
					[OUT]_pvParaBuf: input directory server info, struct ProxyLinkInfo variable
					[IN]_iBufSize: the size of struct,  sizeof(ProxyLinkInfo)
return       :	== 0: Success; other:Failed, Return Value Description by RetValue.h.
others       :	2019.11.19
********************************************************************************************/
int __stdcall NetClient_ProxyGetConfig(int _iCmdID, int _iProxyID, unsigned int _iConnectID, void* _pvParaBuf, int _iBufSize);

#ifdef __cplusplus
}
#endif

#endif

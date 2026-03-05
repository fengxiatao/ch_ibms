
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <ctime>
#include "NetSdk.h"
#include "RetValue.h"
#include "ActionControl.h"
#include "AVPlaySdkTypes.h"


#ifndef __WIN__
#include <sys/times.h>
#include <sys/time.h>
#include <stdint.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h> 
#else
#include <sys/timeb.h>
#endif

int g_iLogonId = -1;
int g_iChannel = -1;
int g_iNetMode = NETMODE_TCP;	//1-private tcp connect, 2-private udp connect, 3-private multicast connect, 6-rtsp stream via RTP-over-TCP,
		//7-rtsp stream via RTP-over-UDP, 8-rtsp stream via RTP-over-Multicast, 9-rtsps stream via SRTP-over-UDP, 10-rtsps stream via SRTP-over-Multicast
unsigned int g_uConnectID = -1;
int g_iStopRecv = 1;
int g_IsRecv = 0;
int  m_iComNo = -1;
int  m_iAddress = -1;
char m_cDeviceType[64] = {0};
FILE *pFileYuv = NULL;
FILE *pFilePcm = NULL;

const char g_cTemUnit[][64] = {"", "Centigrade", "Fahrenheit"};
const char g_cSex[][64] = {"Female", "Male"};
const char g_cMask[][64] = {"Reserved", "No", "Yse"};

typedef int ( *pTC_PlayerControl)(int _iID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
typedef int ( *pTC_RegisterNotifyGetDecAV)( int _iID, pfCBGetDecAV _GetDecAVCbk, void* _pvUserData);
typedef int ( *pTC_SetDecCallBack)(int _iID, DECYUV_NOTIFY_V4 _cbkGetYUV, void* _pvUserData);

pTC_PlayerControl TC_PlayerControl = NULL;
pTC_RegisterNotifyGetDecAV TC_RegisterNotifyGetDecAV = NULL;
pTC_SetDecCallBack TC_SetDecCallBack = NULL;

void* g_pPLaySdkInstance = NULL;
int LoadPlaySDK()
{
	if (g_pPLaySdkInstance)
	{
		return 0;
	}
	
#ifdef  WIN32
	g_pPLaySdkInstance = LoadLibrary("PlaySdkM4.dll");
#else
	g_pPLaySdkInstance = dlopen("libplaysdk.so", RTLD_LAZY);
#endif
	if (NULL == g_pPLaySdkInstance)
	{
		printf("LoadLibrary playsdk failed.\n");
		return -1;
	}
#ifdef  WIN32
	TC_PlayerControl = (pTC_PlayerControl)GetProcAddress((HMODULE)g_pPLaySdkInstance, "TC_PlayerControl");
	TC_RegisterNotifyGetDecAV = (pTC_RegisterNotifyGetDecAV)GetProcAddress((HMODULE)g_pPLaySdkInstance, "TC_RegisterNotifyGetDecAV");
	TC_SetDecCallBack = (pTC_SetDecCallBack)GetProcAddress((HMODULE)g_pPLaySdkInstance, "TC_SetDecCallBack");
#else
	TC_PlayerControl = (pTC_PlayerControl)dlsym(g_pPLaySdkInstance, "TC_PlayerControl");
	TC_RegisterNotifyGetDecAV = (pTC_RegisterNotifyGetDecAV)dlsym(g_pPLaySdkInstance, "TC_RegisterNotifyGetDecAV");
	TC_SetDecCallBack = (pTC_SetDecCallBack)dlsym(g_pPLaySdkInstance, "TC_SetDecCallBack");
#endif

	return 0; 
}

#ifndef __WIN__

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h> 

#define min(a,b)	(((a) < (b)) ? (a) : (b))

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
#include<direct.h>
void usleep(int _iMicroSecond)
{
	if (_iMicroSecond < 1000 && _iMicroSecond != 0)
	{
		_iMicroSecond = 1000;
	}
	Sleep(_iMicroSecond/1000);
}
#endif

int strncpy_ss(char *_pcDest, char *_pcSrc, int _iSize)
{
	if (NULL == _pcDest || NULL == _pcSrc)
	{
		return -1;
	}

	if(_iSize < 0)
	{
		strcpy((char *)_pcDest,_pcSrc);
	}
	else
	{
		if(strlen(_pcSrc) >= (size_t)(_iSize - 1)) 
		{
			memcpy((char *)_pcDest, _pcSrc, _iSize - 1);
			memset((char *)_pcDest + _iSize - 1, 0, 1);
		}
		else
		{
			strcpy((char *)_pcDest,_pcSrc);
		}    
	}

	return 0;
}

void Notify_Main(int _iLogonID, long  _lWparam, void*  _pvLParam, void* _pvUsr)
{
	switch (LOWORD(_lWparam))
	{
	case WCM_LOGON_NOTIFY:	//logon message 
		{
			int iLogonStatus = NetClient_GetLogonStatus(_iLogonID);
			if (LOGON_SUCCESS == iLogonStatus) {		//Login successful	
				printf("[Notify_Main] logon, id=%d, success.\n", _iLogonID);
				g_iLogonId = _iLogonID;
			} else if (LOGON_FAILED == iLogonStatus) {	//Login failed
				printf("[Notify_Main] logon, id=%d, failed.\n", _iLogonID);
			} else if (LOGON_TIMEOUT == iLogonStatus) {	//login timeout
				printf("[Notify_Main] logon, id=%d, timeout.\n", _iLogonID);
			} else if (LOGON_RETRY == iLogonStatus) {	//Re login
				printf("[Notif	y_Main] logon, id=%d, retry.\n", _iLogonID);
			} else if (LOGON_ING == iLogonStatus) {		//Logging in
				printf("[Notify_Main] logon, id=%d, ing.\n", _iLogonID);
			} else {
				printf("[Notify_Main] logon, id=%d, %d.\n", _iLogonID, iLogonStatus);
			}		
		}
		break;
	case WCM_VIDEO_HEAD:
		g_IsRecv = 1;
		break;
	default:
		break;
	}
}

int LogonDevice()
{
	LogonPara tInfo = {0};
	tInfo.iSize = sizeof(LogonPara);
	tInfo.iNvsPort = 3000;

	printf("IP: ");
	gets_s(tInfo.cNvsIP, 32);
	if (0 == strlen(tInfo.cNvsIP)) {
		strncpy_ss(tInfo.cNvsIP, "192.168.1.2", sizeof(tInfo.cNvsIP));
	}

	printf("user name: ");
	gets_s(tInfo.cUserName, 16);
	if (0 == strlen(tInfo.cUserName)) {
		strncpy_ss(tInfo.cUserName, "Admin", sizeof(tInfo.cUserName));
	}

	printf("password: ");	
	gets_s(tInfo.cUserPwd, 16);
	if (0 == strlen(tInfo.cUserPwd)) {
		strncpy_ss(tInfo.cUserPwd, "1111", sizeof(tInfo.cUserPwd));
	}

	printf("port: ");
	char cPort[16] = {0};
	gets_s(cPort, 16);
	tInfo.iNvsPort = atoi(cPort);
	if (tInfo.iNvsPort <= 0) {
		tInfo.iNvsPort = 3000;
	}

	int iLogonId = NetClient_Logon_V4(SERVER_NORMAL, &tInfo, tInfo.iSize);
	if(iLogonId < 0)
	{
		fprintf(stderr,"[LogonDevice] NetClient_Logon_V4(%s) failed, %d\n", tInfo.cNvsIP, iLogonId);
		return -1;
	}

	int iTimes = 0;
	while (LOGON_SUCCESS != NetClient_GetLogonStatus(iLogonId))
	{
		if (iTimes++ > 100)	
		{
			fprintf(stderr,"[LogonDevice] logon(%s) timeout.\n", tInfo.cNvsIP);
			return -1;
		}
		usleep(50000);		
	}
	return 0;
}

void __stdcall GetNotifyTest(unsigned int _ulID, unsigned int _ulStreamType, unsigned char *_cData, int _iLen, void* _iUser, void* _iUserData)
{
	static long long  iGetNotifyTest = 0;
	if (iGetNotifyTest++%50 == 0) {
		printf("FullStream:_ulStreamType is %d, _iLen is %d\n", _ulStreamType, _iLen);
	}
}

void __stdcall GetRawNotify(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser)
{
	static long long iGetRawNotify = 0;
	if (iGetRawNotify++%50 == 0) {
		printf("Raw:_iLen is %d\n", _iLen);
	}
}

//This Callback you can get Yuv data
void __stdcall GetYUVNotify(unsigned int _ulID,unsigned char *_cData, int _iLen, const FRAME_INFO *_pFrameInfo, void* _iUser)
{
	static long long iyuvtime = 0;
	if (iyuvtime++%50 == 0) {
		printf("GetYUVNotify  TimeStamp=%d, dataType=%d,iWidth=%d,iHeight=%d,iDataLen=%d\n", _pFrameInfo->nStamp, _pFrameInfo->nType, _pFrameInfo->nWidth,_pFrameInfo->nHeight, _iLen);
	}
}

//This Callback You Can Get Target Information(include coordinate and so on) + Target Attribute Information(include temperature value)
void __stdcall GetTargetNotify(unsigned int _uiID, int _iType, void* _pvData , int _iDataLen, CurrentFrameInfo* _ptInfo, void* _pvUdata)
{
	//tartget information
	if(_iType == GET_USERDATA_INFO_VCA)
	{
		Vca_TargetArrInfo tVca_TargetArrInfo = {0};
		TargetAttr   tTargetAttr[MAX_SAVE_TARGET_NUM_NEW] = {0};
		int iCpySize = min(sizeof(Vca_TargetArrInfo), _iDataLen);
		memcpy(&tVca_TargetArrInfo, _pvData, iCpySize);

		iCpySize = min(sizeof(TargetAttr), tVca_TargetArrInfo.iAttrLen);
		memcpy(&tTargetAttr, tVca_TargetArrInfo.pTargetAttr, iCpySize*tVca_TargetArrInfo.iTargetCount);

		printf("GetTargetNotify  TimeStamp=%d, TargetCount=%d\n", _ptInfo->uiTimeStamp, tVca_TargetArrInfo.iTargetCount);
		for(int i = 0; i < tVca_TargetArrInfo.iTargetCount; ++i)
		{

			printf("***TargetId=%d,left=%d,top=%d,right=%d,bottom=%d\n", tTargetAttr[i].iTargetId, tTargetAttr[i].rect.left , tTargetAttr[i].rect.top, tTargetAttr[i].rect.right, tTargetAttr[i].rect.bottom);
			//printf age info
			if(tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_AGE] > 0)
			{
				printf("***Age=%d\n", tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_AGE]);
			}
			//printf sex info
			if (tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_GENDER] >= 0 && tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_GENDER] < (int)(sizeof(g_cSex) / 64))
			{
				printf("***Sex:%s\n", g_cSex[tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_GENDER]]);
			}
			//printf tem info
			if(tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_TEMP_VALUE] > 0)
			{
				printf("***Tem=%.2f\n", (float)((tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_TEMP_VALUE] - 100000) / 100.0));
			}
			//printf tem unit info
			if (tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_TEMP_UNIT] > 0 && tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_TEMP_UNIT] < (int)(sizeof(g_cTemUnit) / 64))
			{
				printf("***TemUnit:%s\n", g_cTemUnit[tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_TEMP_UNIT]]);
			}
			//printf mask info
			if (tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_MASK] >= 0 && tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_MASK] < (int)(sizeof(g_cMask) / 64))
			{
				printf("***Mask:%s\n", g_cMask[tTargetAttr[i].iAttrParam[TARGET_ATTR_TYPE_MASK]]);
			}
			
		}
	}
}

void GetYuvAndTarget()
{
	if(-1 == g_uConnectID)
	{
		return;
	}
	int iPlayerID = NetClient_GetRealPlayerIndex((unsigned int *)&g_uConnectID);
	if(-1 == iPlayerID)
	{
		printf("get play id failed!\n");
		return;
	}
	//must set face target enable
	DrawEnable tEnable = {0};
	tEnable.iSize = sizeof(tEnable);
	tEnable.iType = DRAW_TYPE_FACE_TARGET;
	tEnable.iEnable = 1;
	if (TC_PlayerControl) {
		TC_PlayerControl(iPlayerID, CTRL_DRAW_FACE_PARAM, &tEnable, tEnable.iSize, NULL);
	}
	
	//for get yuv data
	if (TC_SetDecCallBack) {
		TC_SetDecCallBack(iPlayerID, (DECYUV_NOTIFY_V4)GetYUVNotify, NULL);
	}

	//for get target information 
	UserDataNotify tInfo = {0};
	tInfo.iSize = sizeof(UserDataNotify);
	tInfo.pNotifyFun = &GetTargetNotify;
	if (TC_PlayerControl) {
		TC_PlayerControl(iPlayerID, CTRL_DRAW_TARGET_NOTIFY, &tInfo, tInfo.iSize, NULL);
	}
}

void DecodeYuvNotify(unsigned int _ulID, unsigned char* _pucData, int _iLen, const FRAME_INFO* _ptFrameInfo, void* _pvUser)
{
	printf("DecodeYuvNotify, _ulID=%u, _iLen=%d\n", _ulID, _iLen);
	if (NULL == _ptFrameInfo) {
		return;
	}

	printf("DecodeYuvNotify, _ulID=%u, _iLen=%d, nType=%u\n", _ulID, _iLen, _ptFrameInfo->nType);
	switch(_ptFrameInfo->nType)
	{
	case T_YUV420:
		{
			printf("DecodeYuvNotify T_YUV420, w=%d, h=%d, Stamp=%d, FrmRate=%d\n"
				, _ptFrameInfo->nWidth, _ptFrameInfo->nHeight, _ptFrameInfo->nStamp, _ptFrameInfo->nFrameRate);
		}
		break;
	case T_AUDIO8:
		{
			if (NULL != _ptFrameInfo->nReserved) {
				FRAME_EXT_INFO tExInfo;
				memcpy(&tExInfo, _ptFrameInfo->nReserved, sizeof(FRAME_EXT_INFO));
				if (NULL != tExInfo.m_pvExtInfo && tExInfo.m_iInfoLen > 0) {
					AudioSampleValue tAudioPara;
					memcpy(&tAudioPara, tExInfo.m_pvExtInfo, min(tExInfo.m_iInfoLen, (int)sizeof(AudioSampleValue)));
					printf("DecodeYuvNotify T_AUDIO8, iAudioChannel=%d, iMaxSampleValue1=%d, iMinSampleValue1=%d\n"
						, tAudioPara.iChannel, tAudioPara.iMaxSampleValue1, tAudioPara.iMinSampleValue1);
				}
			}
		}
		break;
	default:
		break;
	}
}

#ifndef __WIN__
void MyCbGetDecAV(int _iID, const DecAVInfo* _pDecAVInfo, void* _iUser)
{
	printf("MyCbGetDecAV, _iID=%d, _pDecAVInfo=%p\n", _iID, _pDecAVInfo);
	if (NULL == _pDecAVInfo) {
		return;
	}

	DecAVInfoEx* ptDecExInfo = (DecAVInfoEx*)_pDecAVInfo;
	printf("MyCbGetDecAV, _iID=%d, _iType=%d, w=%d, h=%d\n", _iID, ptDecExInfo->m_pInfo.iType, ptDecExInfo->m_pInfo.iWidth, ptDecExInfo->m_pInfo.iHeight);
	switch(ptDecExInfo->m_pInfo.iType)
	{
	case AV_CBK_TYPE_VIDEO:
		{
			printf("MyCbGetDecAV AV_CBK_TYPE_VIDEO, iDataLen=%d\n", ptDecExInfo->m_pInfo.iDataLen);
		}
		break;
	case AV_CBK_TYPE_AUDIO:
		{
			printf("MyCbGetDecAV AV_CBK_TYPE_AUDIO, iDataLen=%d\n", ptDecExInfo->m_pInfo.iDataLen);
		}
		break;
	default:
		break;
	}
}
#endif

int ConnectVideo()
{
	RECT rt = {0};
	int iTimes = 0;
	int iChannelNum = 0, iRet = -1;
	char cTemp[16] = {0};
	NetClientPara tPara = {0};
	iRet = NetClient_GetChannelNum(g_iLogonId, &iChannelNum);
	if (RET_SUCCESS != iRet) {
		printf("NetClient_GetChannelNum fail, iRet=%d\n", iRet);
		return -1;
	}

	if (iChannelNum > 1) {
		printf("Input the Channel Num(0 - %d)(default is 0):", iChannelNum-1);
		gets_s(cTemp, 16);
		if (0 == strlen(cTemp)) {
			g_iChannel = 0;
		} else {
			g_iChannel = atoi(cTemp);
		}
	} else {
		g_iChannel = 0;
	}

	printf("1-private tcp connect, 2-private udp connect, 3-private multicast connect, 6-rtsp stream via RTP-over-TCP, 7-rtsp stream via RTP-over-UDP\n");
	printf("8-rtsp stream via RTP-over-Multicast, 9-rtsps stream via SRTP-over-UDP, 10-rtsps stream via SRTP-over-Multicast\n");
	printf("Input net mode(default is 1-private tcp connect):");
	gets_s(cTemp, 16);
	if (0 == strlen(cTemp)) {
		g_iNetMode = NETMODE_TCP;
	} else {
		g_iNetMode = atoi(cTemp);
	}
	tPara.iSize = sizeof(NetClientPara);
	tPara.tCltInfo.m_iServerID = g_iLogonId;
	tPara.tCltInfo.m_iChannelNo = g_iChannel;
	tPara.tCltInfo.m_iStreamNO = 0;
	tPara.tCltInfo.m_iNetMode = g_iNetMode;
	tPara.tCltInfo.m_iTimeout = 20;
	iRet = NetClient_StartRecv_V5(&g_uConnectID, &tPara, sizeof(NetClientPara));
	if (RET_SUCCESS != iRet) {
		printf("NetClient_StartRecv_V5 fail, iRet=%d\n", iRet);
		return -1;
	}

	g_iStopRecv = 0;
	printf("Input 0 to stop....\n");
	//NetClient_SetFullStreamNotify_V4(g_uConnectID, GetNotifyTest, 0);
	//NetClient_SetRawFrameCallBack(g_uConnectID, GetRawNotify, 0);

	while (!g_IsRecv)
	{
		if (iTimes++ > 100)
		{
			printf("Connect Video Failed\n");
			NetClient_StopRecv(g_uConnectID);
			g_uConnectID = -1;
			return -1;
		}
		usleep(50000);
	}

	//register calback avdec
	//iRet = NetClient_SetDecCallBack_V4(g_uConnectID, DecodeYuvNotify, NULL);
	//if (RET_SUCCESS == iRet) {
	//	printf("NetClient_SetDecCallBack_V4 success\n");
	//} else {
	//	printf("NetClient_SetDecCallBack_V4 Failed, iRet=%d\n", iRet);
	//}
#ifndef __WIN__
	//iRet = NetClient_SetDecCallBack(g_uConnectID, MyCbGetDecAV, NULL);
	//if (RET_SUCCESS == iRet) {
	//	printf("NetClient_SetDecCallBack success\n");
	//} else {
	//	printf("NetClient_SetDecCallBack Failed, iRet=%d\n", iRet);
	//}
#endif

	iRet = NetClient_StartPlay(g_uConnectID, NULL, rt, 0);
	if(RET_SUCCESS != iRet) {
		printf("start play failed, iRet=%d\n", iRet);
		return -1;
	}
	printf("start play suceess!\n");

	GetYuvAndTarget();
	memset(cTemp, 0, sizeof(cTemp));
	gets_s(cTemp, 16);
	int iFlag = atoi(cTemp);
	if (0 ==iFlag) {
		NetClient_StopRecv(g_uConnectID);
	}

	return 0;
}
int CapturePic()
{
	char cTemp[16] = {0};
	RECT rt = {0};
	int iRet = -1;
	int iTimes = 0;
	int iOpt = 0;
	time_t rawtime;
	struct tm *ptminfo;
	NetClientPara tPara = {0};
	tPara.iSize = sizeof(NetClientPara);
	tPara.tCltInfo.m_iServerID = g_iLogonId;
	tPara.tCltInfo.m_iChannelNo = 0;
	tPara.tCltInfo.m_iStreamNO = 0;
	tPara.tCltInfo.m_iNetMode = 1;
	tPara.tCltInfo.m_iTimeout = 20;
	iRet = NetClient_StartRecv_V5(&g_uConnectID, &tPara, sizeof(NetClientPara));
	while (!g_IsRecv)
	{
		if (iTimes++ > 100)
		{
			printf("Connect Video Failed\n");
			break;
		}
		usleep(50000);
	}
	iRet = -1;
	iTimes = 0;
	while (-1 == iRet)
	{
		iRet = NetClient_StartPlay(g_uConnectID, NULL, rt, 0);
		if (0 == iRet)
		{
			break;
		}
	}
	printf("NetClient_StartPlay(%d)\n", iRet);
	while (1)
	{
		printf("Choose Snap Type:0:Exit 1:BMP, 2:JPG\n");
		gets_s(cTemp, 16);

		iOpt = atoi(cTemp);

		time(&rawtime);
		ptminfo = localtime(&rawtime);
		char cFullFilePath[256] = {0};
		if (1 == iOpt)
		{
			sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.bmp", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_mday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
		}
		else if (2 == iOpt)
		{
			sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.jpg", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_yday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
		}
		else if (0 == iOpt)
		{
			break;
		}

		iRet = NetClient_CapturePicture(g_uConnectID, iOpt, cFullFilePath);

		if (iRet > 0)
		{
			printf("Snap Successfully(%s)\n", cFullFilePath);
		}
		else
		{
			printf("Snap Failed(%d)\n", iRet);
		}

	}
	NetClient_StopPlay(g_uConnectID);
	NetClient_StopRecv(g_uConnectID);
	g_IsRecv = 0;
	return 0;
}

void DecodeYuvNotify( unsigned int _ulID,unsigned char *_cData, int _iLen, const FRAME_INFO *_pFrameInfo)
{
	TStreamData StreamData = {0};

	StreamData.iSize = sizeof(TStreamData);
	time_t rawtime;
	struct tm *ptminfo;
	time(&rawtime);
	ptminfo = localtime(&rawtime);
	int iRet = NetClient_GetUserDataInfo(_ulID,  GET_USERDATA_INFO_USER_DEFINE, &StreamData, sizeof(StreamData));
	char cData[65] = {0};
	if (StreamData.iStreamLen < sizeof(cData) && StreamData.iStreamLen >0)
	{
		memcpy(cData, StreamData.cStreamData, StreamData.iStreamLen);
	}

	switch(_pFrameInfo->nType)
	{
	case T_YUV420:
		{
			char cFullFilePath[256] = {0};
			sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.yuv420", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_yday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
			pFileYuv= fopen(cFullFilePath,"wb+");
		}
		if (pFileYuv)
		{
			fwrite(_cData,sizeof(unsigned char),_iLen,pFileYuv);
		}
		break;
	case T_AUDIO8:
		{
			if (NULL == pFilePcm)
			{
				char cFullFilePath[256] = {0};
				sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.pcm", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_yday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
				pFilePcm = fopen(cFullFilePath,"wb+");
				if (NULL == pFilePcm)
				{
					printf("Write File Error\n");
					return;
				}
			}

			fwrite(_cData,sizeof(unsigned char),_iLen,pFilePcm);
		}
		break;
	default:
		break;
	}
}

int Record()
{
	char cTemp[16] = {0};
	RECT rt = {0};
	int iTimes = 0;
	int iRet = -1;
	int iOpt = 0;
	time_t rawtime;
	struct tm *ptminfo;
	NetClientPara tPara = {0};
	tPara.iSize = sizeof(NetClientPara);
	tPara.tCltInfo.m_iServerID = g_iLogonId;
	tPara.tCltInfo.m_iChannelNo = 0;
	tPara.tCltInfo.m_iStreamNO = 0;
	tPara.tCltInfo.m_iNetMode = 1;
	tPara.tCltInfo.m_iTimeout = 20;
	iRet = NetClient_StartRecv_V5(&g_uConnectID, &tPara, sizeof(NetClientPara));
	while(!g_IsRecv)
	{
		if (iTimes++ > 100)	//5s over time
		{
			printf("Connect Video Failed\n");
			NetClient_StopPlay(g_uConnectID);
			NetClient_StopRecv(g_uConnectID);
			return -1;
		}
		usleep(50000);	
	}
	iRet = NetClient_StartPlay(g_uConnectID, NULL, rt, 0);
	iTimes = 0;
	while (0 != iRet)
	{
		if (iTimes++ > 100)
		{
			printf("Play Video Failed\n");
			NetClient_StopPlay(g_uConnectID);
			NetClient_StopRecv(g_uConnectID);
			return -1;
		}
		iRet = NetClient_StartPlay(g_uConnectID, NULL, rt, 0);
		usleep(50000);
	}
//  printf("Choose Record Type:0:Exit, 1:SDV, 2:PS(MP4), 3:ZFMP4, 4:TS\n");
	printf("Choose Record Type:0:Exit, 1:SDV, 2:PS(MP4)\n");
	gets_s(cTemp, 16);
	
	iOpt = atoi(cTemp);

	time(&rawtime);
	ptminfo = localtime(&rawtime);

	if (iOpt == 1)
	{
		char cFullFilePath[256] = {0};
		sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.sdv", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_yday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
		iRet = NetClient_StartCaptureFile(g_uConnectID,cFullFilePath,REC_FILE_TYPE_NORMAL);
		
	}
	else if (iOpt == 2)
	{
		char cFullFilePath[256] = {0};
		sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.mp4", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_yday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
		iRet = NetClient_StartCaptureFile(g_uConnectID,cFullFilePath,REC_FILE_TYPE_PS);
	}
	/*else if (iOpt == 3)
	{
		char cFullFilePath[256] = {0};
		sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.mp4", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_yday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
		iRet = NetClient_StartCaptureFile(g_uConnectID,cFullFilePath,REC_FILE_TYPE_ZFMP4);
	}
    else if (iOpt == 4)
    {
        char cFullFilePath[256] = {0};
        sprintf(cFullFilePath, ".//Pic//%d%02d%02d%02d%02d%02d.ts", ptminfo->tm_year+1900,ptminfo->tm_mon+1, ptminfo->tm_yday, ptminfo->tm_hour, ptminfo->tm_min, ptminfo->tm_sec);
        iRet = NetClient_StartCaptureFile(g_uConnectID,cFullFilePath,REC_FILE_TYPE_TS);
    }*/

	if (0 == iRet)
	{
		printf("Recording......\n");
	}
	else
	{
		printf("Record Failed\n");
		return -1;
	}
	printf("Stop(Y/N)?\n");
	
	while (1)
	{
		gets_s(cTemp, 16);
		if (0 == strcmp(cTemp,"Y") || 0 == strcmp(cTemp, "y"))
		{
			if (1 == iOpt || 2 == iOpt || 3 == iOpt)
			{
				NetClient_StopCaptureFile(g_uConnectID);
			}
			NetClient_StopPlay(g_uConnectID);
			NetClient_StopRecv(g_uConnectID);
			g_uConnectID = -1;
			break;
		}
	}
	
	return 0;
}
int New3DLocation()
{
	int iRet = -1;
	Locate3DPosition t3dInfo[3] = {0};
	for (int i = 0; i < 3; i++)
	{
		t3dInfo[i].iBufSize = sizeof(Locate3DPosition);
		t3dInfo[i].iPointNum = 2; //Rectangle;
	}
	t3dInfo[0].tPoint[0].iX = 2711;
    t3dInfo[0].tPoint[0].iY = 2803;
	t3dInfo[0].tPoint[1].iX = 5043;
	t3dInfo[0].tPoint[1].iY = 5568;

	t3dInfo[1].tPoint[0].iX = 7084;
	t3dInfo[1].tPoint[0].iY = 8106;
	t3dInfo[1].tPoint[1].iX = 9329;
	t3dInfo[1].tPoint[1].iY = 9962;

	t3dInfo[2].tPoint[0].iX = 262;
	t3dInfo[2].tPoint[0].iY = 8030;
	t3dInfo[2].tPoint[1].iX = 3061;
	t3dInfo[2].tPoint[1].iY = 9848;
	NetClientPara tPara = {0};
	tPara.iSize = sizeof(NetClientPara);
	tPara.tCltInfo.m_iServerID = g_iLogonId;
	tPara.tCltInfo.m_iChannelNo = 0;
	tPara.tCltInfo.m_iStreamNO = 0;
	tPara.tCltInfo.m_iNetMode = 1;
	tPara.tCltInfo.m_iTimeout = 20;
	iRet = NetClient_StartRecv_V5(&g_uConnectID, &tPara, sizeof(NetClientPara));
	if (0 == iRet)
	{
		char cTemp[16] = {0};
		int iOpt = 0;
		while(1)
		{
			printf("Choose Area:0--Exit,1--Area1,2--Area2,3--Area3\n");
			gets_s(cTemp, 16);
			iOpt = atoi(cTemp);
			if (0 != iOpt)
			{
				NetClient_SendCommand(g_iLogonId, COMMAND_ID_3D_POSITION, 0, &t3dInfo[iOpt-1], sizeof(t3dInfo));
			}
			else
				break;
		}
	}
	NetClient_StopRecv(g_uConnectID);
	return 0;
};

int VideoSet()
{
	int iRet = -1;
	int iOpt = 0;
	char cTemp[16] = {0};
	STR_VideoParam tVideoParam = {0};
	while(1)
	{
		iRet = NetClient_GetVideoPara(g_iLogonId,0,&tVideoParam);

		printf("Choose Video Param:0.Exit 1.Chroma 2.Bright 3.Contrast Radio 4.Saturation\n");
		gets_s(cTemp, 16);
		iOpt = atoi(cTemp);
		memset(cTemp, 0, sizeof(cTemp));

		if (1 == iOpt)
		{
			printf("the current Chroma of video is %d\nSet the data of Chroma(1~255):",tVideoParam.m_u16Hue);
			gets_s(cTemp, 16);
			tVideoParam.m_u16Hue = atoi(cTemp);
		}
		else if (2 == iOpt)
		{
			printf("the current Bright of video is %d\nSet the data of Bright(1~255):",tVideoParam.m_u16Brightness);
			gets_s(cTemp, 16);
			tVideoParam.m_u16Brightness = atoi(cTemp);
		}
		else if (3 == iOpt)
		{
			printf("the current Contrast ratio of video is %d\nSet the data of Contrast Ratio(1~255):",tVideoParam.m_u16Contrast);
			gets_s(cTemp, 16);
			tVideoParam.m_u16Contrast = atoi(cTemp);
		}
		else if (4 == iOpt)
		{
			printf("the current Saturation of video is %d\nSet the data of Saturation(1~255):",tVideoParam.m_u16Saturation);
			gets_s(cTemp, 16);
			tVideoParam.m_u16Saturation = atoi(cTemp);
		}
		else if (0 == iOpt)
		{
			break;
		}

		iRet = NetClient_SetVideoPara(g_iLogonId, 0, &tVideoParam);

		usleep(100*1000);//The value obtained immediately after preventing set is not up to date

		if (0 == iRet)
		{
			printf("Setting Successfully\n");
		}
	}
	return 0;
}

int PTZControl()
{
	int iRet = -1;
	char cComFormat[32] = {0};
	char cTemp[16] = {0};
	int m_iWorkMode = -1;
	int iTimes = 0;
	int iOpt = -1;
	printf("Only Support the Mode of Protocal\n");
	while(1)
	{
		printf("0.Exit 1.Up 2.Down 3.Left 4.Right 5.Up_Right 6.Up_Left 7.Down_Right 8.Down_Left\n");
		iTimes = 0;
		iRet = NetClient_GetDeviceType(g_iLogonId,0,&m_iComNo,&m_iAddress,m_cDeviceType);

		if (0 != iRet)
		{
			printf("NetClient_GetDeviceType(%d)\n", iRet);
			return -1;
		}

		iRet = NetClient_GetComFormat(g_iLogonId,m_iComNo,cComFormat,&m_iWorkMode);

		if (0 != iRet)
		{
			printf("NetClient_GetComFormat Failed(%d)", iRet);
			return -1;
		}
		m_iWorkMode = 1;//Set the work mode of Com is Control of Protocal

		iRet = NetClient_SetComFormat(g_iLogonId, m_iComNo, m_cDeviceType, cComFormat, m_iWorkMode);
		if (0 != iRet)
		{
			printf("NetClient_SetComFormat Failed(%d)", iRet);
			return -1;
		}
		gets_s(cTemp, 16);
		iOpt = atoi(cTemp);
		if (0 == iOpt)
		{
			break;
		}

		while (1)
		{
			if (iTimes++ < 100)
			{
				iRet = NetClient_DeviceCtrlEx(g_iLogonId, 0, iOpt, 50, 50, 0);
			}
			else
			{
				break;
			}
			usleep(30000);		//Get login status once in 50 ms
		}
		NetClient_DeviceCtrlEx(g_iLogonId, 0, PROTOCOL_MOVE_STOP, 0, 50, 0);
	}
	return 0;
}
int main(int argc, char* argv[])
{
	int iRet = LoadNVSSDK();
	iRet = LoadPlaySDK();
	//Register main callback function
	iRet = NetClient_SetNotifyFunction_V4(Notify_Main, NULL, NULL, NULL, NULL);

	//start SDK
	iRet = NetClient_Startup_V4(0, 0, 0);

	if (LogonDevice() < 0)
	{
		fprintf(stderr,"[main] LogonDevice failed.\n");
		getchar();
		return -1;
	}
#ifdef __WIN__
	_mkdir(".\\Pic");
#else
	mkdir(".//Pic", S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH); 
#endif
	while (1)
	{
		fprintf(stderr, "choose: 0--Exit,1--Connect Video,2--Snap,3--Record,4--3DLocation,5--Video Setting,6--PTZ Control\n");
		char cTemp[16] = {0};
		gets_s(cTemp, 16);
		if (0 == strlen(cTemp))
		{
			continue;
		}

		int iOpt = atoi(cTemp);
		if (0 == iOpt)
		{
			break;
		}
		else if (1 == iOpt)	
		{
			ConnectVideo();
		}
		else if (2 == iOpt)
		{
			CapturePic();
		}
		else if (3 == iOpt)
		{
			Record();
		}
		else if (4 == iOpt)
		{
			New3DLocation();
		}
		else if (5 == iOpt)
		{
			VideoSet();
		}
		else if (6 == iOpt)
		{
			PTZControl();
		}
	}
	NetClient_Logoff(g_iLogonId);
	NetClient_Cleanup();
	return 0;
}


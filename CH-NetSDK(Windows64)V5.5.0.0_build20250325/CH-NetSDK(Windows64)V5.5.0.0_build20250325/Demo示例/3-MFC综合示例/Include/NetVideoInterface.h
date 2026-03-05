#pragma once
#include "NetClientTypes.h"

static HINSTANCE s_hInstance = NULL;		//	NetVideoConfig.dll handle

enum PARAM_TYPE
{
	PARAM_TYPE_MIN = 0,
	PARAM_TYPE_LANGUAGE = PARAM_TYPE_MIN,
	PARAM_TYPE_LOG_OUTPUT_LEVEL,
	PARAM_TYPE_REC_FILE_TYPE,
	PARAM_TYPE_SAVE_PATH,
	PARAM_TYPE_HIDE_ENCRYPT_NODE,
	PARAM_TYPE_SET_VIDEO_DECRYPT,
	PARAM_TYPE_SET_SIP_INFO,
	PARAM_TYPE_LOCAL_SAVE_PATH,
	PARAM_TYPE_PREVIEW_PIC_PATH,
	PARAM_TYPE_PLAYBACK_PIC_PATH,
	PARAM_TYPE_RECEDIT_PATH,
	PARAM_TYPE_DOWNLOAD_PATH,
	PARAM_TYPE_PROTOCAL_TYPE,
	PARAM_TYPE_WINDOW_RECT,
	PARAM_TYPE_SHOW_HAND,
	PARAM_TYPE_SHOW_WINDOW,
	PARAM_TYPE_MAX
};

#define CFG_ERR_INVALID_PARAM		(-1)
#define	LEN_IP	(16)
typedef struct tagSipInfo
{
	char cSipIp[LEN_IP+1];
	int	iPort;
	char cSipServerID[LEN_64];
	char cSipDeviceID[LEN_64];
	char cUserName[LEN_64];
	char cPassWord[LEN_64];
	int iSIP_RegVality;
	int iSIP_Keepalive;
	int iSIP_HeartInterval;
	int iSIP_HeartTimes;
	int iEnable;
}SipInfo, *pSipInfo;

int LoadNetVideoConfig();
int FreeNetVideoConfig();

//NetVideoConfig.dll Interface

typedef int (__stdcall	*pfNetVideo_ShowConfigWindow)(CLIENTINFO *_pClientInfo, MAIN_NOTIFY_V4 _cbkMainNotify, ALARM_NOTIFY_V4 _cbkAlarmNotify, PARACHANGE_NOTIFY_V4 _cbkParaChangeNotify, COMRECV_NOTIFY_V4 _cbkComRecvNotify,PROXY_NOTIFY _cbkProxyNotify);
extern pfNetVideo_ShowConfigWindow NetVideo_ShowConfigWindow;

typedef int (__stdcall *pfNetVideo_GetParaChangeNotify)(int _iLogonID, int _iCh, PARATYPE _iParaType, void * _strPara,void* _lpNoitfyUserData);
extern pfNetVideo_GetParaChangeNotify NetVideo_GetParaChangeNotify;

typedef int (__stdcall *pfNetVideo_GetMainNotify)(int _iLogonID,long _lWParam,void* _pLParam,void* _lpNoitfyUserData);
extern pfNetVideo_GetMainNotify NetVideo_GetMainNotify;

typedef int (__stdcall *pfNetVideo_GetAlarmNotify)(int _iLogonID, int _iChannel, void* _pValue,ALARMTYPE _iType, void* _lpNoitfyUserData);
extern pfNetVideo_GetAlarmNotify NetVideo_GetAlarmNotify;

typedef int (__stdcall	*pfNetVideo_GetParam)(int _iType,void *_pValue,int _iLen);
extern pfNetVideo_GetParam NetVideo_GetParam;

typedef int (__stdcall	*pfNetVideo_SetParam)(int _iType,void *_pValue,int _iLen);
extern pfNetVideo_SetParam NetVideo_SetParam;





























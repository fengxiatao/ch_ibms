
#include <stdio.h>
#include <stdlib.h>
#include <map>
#include <string>
#include <string.h>
#include <iostream>
#include <time.h>
using namespace std;
#if defined(_WIN32) || defined(_WIN64)
#include <windows.h>
#include <windows.h>
#include <process.h >
#else
#include <sys/times.h>
#include <sys/time.h>
#include <stdint.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h> 
#endif
#include "NetSdk.h"
#include "RetValue.h"
#include "AVPlaySdkApi.h"

#ifndef BOOL
#define BOOL int
#endif

#ifndef TRUE
#define TRUE 1
#endif

#ifndef FALSE
#define FALSE 0
#endif

typedef struct tagVideoConn
{
	int				iReciveHandle;
	int				iDecodeHandle;
} VideoConn, *pVideoConn;

#define MAX_DEVICE_COUNT	1
static int s_iLogonID[MAX_DEVICE_COUNT] = {-1};
#define MAX_VIDEO_COUNT		1		//Demo only connects one video channel by default
static VideoConn s_tVideoConn[MAX_VIDEO_COUNT] = {-1, -1};
static void __stdcall FullFrameCallback(unsigned int _uiID, unsigned int _uiStreamType, unsigned char* _pucData, int _iLen, void* _pvHead, void* _pvUsrData);
static void __stdcall RawFrameCallback(unsigned int _uiID, unsigned char* _pucData, int _iLen, RAWFRAME_INFO* _ptRawFrameInfo, void* _pvUsrData);

//Define demo operation command value
#define CMD_QUIT				-1
#define CMD_SYNCLOGON			1
#define CMD_LOGOFF				2
#define CMD_STARTREALPALY		3
#define CMD_STOPREALPALY		4
#define CMD_SYNCQUERYFILE		5
#define CMD_SYNCQUERYLOG		6
#define CMD_SUSPENDVCA			7
#define CMD_OPENVCA				8
#define CMD_SETPTZ				9
#define CMD_GETPTZ				10

//Store operation instructions
static map<string, int> s_mapCmd;
static void MakeCmd();
static void TestSdkApi();
static int SyncLogon();
static int Logoff();
static int StartRealPlay();
static int StopRealPlay();
static int SyncQueryFile();
static int SyncQueryLog();
static int SyncSuspendVca();
static int SyncOpenVca();
static int SyncSetPtz();
static int SyncGetPtz();

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

int main(int argc, char* argv[])
{
	int iRet = RET_FAILED;

	//Dynamic loading SDK
	iRet = LoadNVSSDK();
	CLS_AVPlaySdkApi::Instance()->LoadSdkLibrary();
	if (RET_SUCCESS != iRet)
	{
		printf("LoadSdkLib fail!\n");
		return -1;
	}

	//To start the SDK, you only need to call it once
	iRet = NetClient_Startup_V4(0, 0, 0);
	CLS_AVPlaySdkApi::Instance()->CreateSystem(NULL);
	if(RET_SUCCESS != iRet )
	{
		printf("NetClient_Startup_V4 fail!\n");
		return -1;
	}

	MakeCmd();
	TestSdkApi();

	NetClient_Cleanup();
	return 0;
}

void MakeCmd()
{   
	s_mapCmd[string("Quit")] = CMD_QUIT;
	s_mapCmd[string("q")] = CMD_QUIT;

	s_mapCmd[string("SyncLogon")] = CMD_SYNCLOGON;
	s_mapCmd[string("1")] = CMD_SYNCLOGON;

	s_mapCmd[string("Logoff")] = CMD_LOGOFF;
	s_mapCmd[string("2")] = CMD_LOGOFF;

	s_mapCmd[string("StartRealPlay")] = CMD_STARTREALPALY;
	s_mapCmd[string("3")] = CMD_STARTREALPALY;

	s_mapCmd[string("StopRealPlay")] = CMD_STOPREALPALY;
	s_mapCmd[string("4")] = CMD_STOPREALPALY;

	s_mapCmd[string("QueryFile")] = CMD_SYNCQUERYFILE;
	s_mapCmd[string("5")] = CMD_SYNCQUERYFILE;

	s_mapCmd[string("QueryLog")] = CMD_SYNCQUERYLOG;
	s_mapCmd[string("6")] = CMD_SYNCQUERYLOG;

	s_mapCmd[string("SuspendVca")] = CMD_SUSPENDVCA;
	s_mapCmd[string("7")] = CMD_SUSPENDVCA;

	s_mapCmd[string("OpenVca")] = CMD_OPENVCA;
	s_mapCmd[string("8")] = CMD_OPENVCA;

	s_mapCmd[string("SetPtz")] = CMD_SETPTZ;
	s_mapCmd[string("9")] = CMD_SETPTZ;

	s_mapCmd[string("GetPtz")] = CMD_GETPTZ;
	s_mapCmd[string("10")] = CMD_GETPTZ;
}

void TestSdkApi()
{
	int iCmd = -1;
	string strCmd;
	bool blQuit = false;
	while(!blQuit)
	{
		cout<<"*********************************************************************************************"<<endl;
		fprintf(stderr,
			"[q]Quit\n"
			"[1]SyncLogon	[2]Logoff	[3]StartRealPlay	[4]StopRealPlay		\n"
			"[5]QueryFile	[6]QueryLog	[7]SuspendVca		[8]OpenVca			\n"
			"[9]SetPtz	[10]GetPtz				\n");
		cout<<"*********************************************************************************************"<<endl;

		cout<<endl<<"Please input your correct command: "<<endl;
		cout<<">";
		cin>>strCmd;
		iCmd = s_mapCmd[strCmd];
		switch(iCmd)  
		{
		case CMD_QUIT:   //q
			cout<<"Goodbye, my friend!"<<endl;
			blQuit = true;
			break;
		case CMD_SYNCLOGON:
			SyncLogon();
			break;
		case CMD_LOGOFF:
			Logoff();
			break;
		case CMD_STARTREALPALY:
			StartRealPlay();
			break;
		case CMD_STOPREALPALY:
			StopRealPlay();
			break;
		case CMD_SYNCQUERYFILE:
			SyncQueryFile();
			break;
		case CMD_SYNCQUERYLOG:
			SyncQueryLog();
			break;
		case CMD_SUSPENDVCA:
			SyncSuspendVca();
			break;
		case CMD_OPENVCA:
			SyncOpenVca();
			break;
		case CMD_SETPTZ:
			SyncSetPtz();
			break;
		case CMD_GETPTZ:
			SyncGetPtz();
			break;
		default:
			cout<<"Command \""<<strCmd<<"\" not found!"<<endl;
			break;
		}
	}
}

int SyncLogon()
{
	int iRet = RET_FAILED;
	int iLogonType = SERVER_NORMAL;
	fprintf(stderr, "Please input LogonType: 0----Normal  1----Active\n");
	scanf("%d", &iLogonType);
	if (SERVER_ACTIVE == iLogonType)
	{
		int iLocalListenPort = 6004;
		char cProductID[32] = {0};
		DsmOnline tOnline = {0};
		LogonActiveServer tActive = {0};
		//Active mode login logic
		fprintf(stderr, "Please input local listen port:");
		scanf("%d", &iLocalListenPort);
		iRet = NetClient_SetPort(iLocalListenPort, 0);
		if(RET_SUCCESS != iRet )
		{
			printf("NetClient_SetPort fail!\n");
			goto END;
		}

		ActiveNetWanInfo tLocalWanInfo = {0};
		tLocalWanInfo.iSize = sizeof(ActiveNetWanInfo);
		fprintf(stderr, "Please input wan IP: ");
		scanf("%s", tLocalWanInfo.cWanIP);
		if (0 == strlen(tLocalWanInfo.cWanIP)) {
			strncpy_ss(tLocalWanInfo.cWanIP, (char*)("192.168.1.2"), sizeof(tLocalWanInfo.cWanIP));
			fprintf(stderr, "wan IP is null:default set 192.168.1.2");
		}
		fprintf(stderr, "Please input local wan port:");
		scanf("%d", &(tLocalWanInfo.iWanPort));
		iRet = NetClient_SetDsmConfig(DSM_CMD_SET_NET_WAN_INFO, &tLocalWanInfo, sizeof(ActiveNetWanInfo));
		if(RET_SUCCESS != iRet )
		{
			printf("NetClient_SetDsmConfig:DSM_CMD_SET_NET_WAN_INFO fail!\n");
			goto END;
		}

		fprintf(stderr, "Please input ProductID: ");
		scanf("%s", cProductID);
		tOnline.iSize = sizeof(DsmOnline);
		strncpy(tOnline.cProductID, cProductID, LEN_32);
		int iOutTime = 0;
		while (1)
		{
			//Get registration online status
			NetClient_GetDsmRegstierInfo(DSM_CMD_GET_ONLINE_STATE, &tOnline, sizeof(DsmOnline));
			if (DSM_STATE_ONLINE == tOnline.iOnline) {
				break;
			}

			if (iOutTime >= 30)
			{
				fprintf(stderr, "Device not register!\n");
				return -1;
			}

#if (defined(_WIN32) || defined(_WIN64)) 
			Sleep(1000);
#else	
			usleep(1000 * 1000);
#endif
			iOutTime++;
		}

		tActive.iSize = sizeof(LogonActiveServer);
		fprintf(stderr, "Please input UserName: ");
		scanf("%s", tActive.cUserName);
		fprintf(stderr, "Please input Password: ");
		scanf("%s", tActive.cUserPwd);
		strcpy(tActive.cProductID, cProductID);
		//Active mode synchronous blocking landing device
		s_iLogonID[0] = NetClient_SyncLogon(iLogonType, &tActive, sizeof(LogonActiveServer));
	}
	else
	{
		LogonPara tNormal = {0};
		char cIP[32] = {0};
		fprintf(stderr, "Please input Device IP: ");
		scanf("%s", cIP);
		tNormal.iSize = sizeof(LogonPara);
		tNormal.iNvsPort = 3000;
		strcpy(tNormal.cNvsIP, cIP);
		fprintf(stderr, "Please input UserName: ");
		scanf("%s", tNormal.cUserName);
		fprintf(stderr, "Please input Password: ");
		scanf("%s", tNormal.cUserPwd);
		strcpy(tNormal.cCharSet, "UTF-8");
		//Normal mode synchronous blocking login device
		s_iLogonID[0] = NetClient_SyncLogon(iLogonType, &tNormal, sizeof(LogonPara));
	}
	if (s_iLogonID[0] >= 0)
	{
		printf("NetClient_LogonSync Success!s_iLogonID[0]=%d, iLogonType=%d.\n", s_iLogonID[0], iLogonType);
	}
	else if (RET_SYNCLOGON_TIMEOUT == s_iLogonID[0])
	{
		printf("NetClient_LogonSync Timeout!\n");
		goto END;
	}
	else if (RET_SYNCLOGON_USENAME_ERROR == s_iLogonID[0])
	{
		printf("NetClient_LogonSync username error!\n");
		goto END;
	}
	else if (RET_SYNCLOGON_USRPWD_ERROR == s_iLogonID[0])
	{
		printf("NetClient_LogonSync password error!\n");
		goto END;
	}
	else if (RET_SYNCLOGON_PWDERRTIMES_OVERRUN == s_iLogonID[0])
	{
		printf("NetClient_LogonSync passwor times overrun!\n");
		goto END;
	}
	else if (RET_SYNCLOGON_NET_ERROR == s_iLogonID[0])
	{
		printf("NetClient_LogonSync net error!\n");
		goto END;
	}
	else if (RET_SYNCLOGON_PORT_ERROR == s_iLogonID[0])
	{
		printf("NetClient_LogonSync port error!\n");
		goto END;
	}
	else if (RET_SYNCLOGON_UNKNOW_ERROR == s_iLogonID[0])
	{
		printf("NetClient_LogonSync unknow error!\n");
		goto END;
	}
	else
	{
		printf("NetClient_LogonSync fail iRet=%d.!\n", s_iLogonID[0]);
		goto END;
	}

END:
	return iRet;
}

int Logoff()
{
	if (s_iLogonID[0] >= 0)
	{
		NetClient_Logoff(s_iLogonID[0]);
		s_iLogonID[0] = -1;
	}

	return 0;
}

int StartRealPlay()
{
	int iRet = RET_FAILED;
	int iChanTotalCount = 0;
	int iDigitalChanCount = 0;
	int iVideoChan = 0;
	int iNetMode = NETMODE_TCP;
	NetClientPara tVideoPara = {0};

	iRet = NetClient_GetChannelNum(s_iLogonID[0], &iChanTotalCount);
	if (RET_SUCCESS != iRet)
	{
		printf("NetClient_GetChannelNum fail iRet=%d.!\n", iRet);
		goto END;
	}

	iRet = NetClient_GetDigitalChannelNum(s_iLogonID[0], &iDigitalChanCount);
	if (RET_SUCCESS != iRet)
	{
		printf("NetClient_GetDigitalChannelNum fail iRet=%d.!\n", iRet);
		goto END;
	}

	printf("Input video Channel Number[0-%d](default:0):", iChanTotalCount - 1);
	scanf("%d", &iVideoChan);
	printf("1-private tcp connect, 2-private udp connect, 3-private multicast connect, 6-rtsp stream via RTP-over-TCP, 7-rtsp stream via RTP-over-UDP\n");
	printf("8-rtsp stream via RTP-over-Multicast, 9-rtsps stream via SRTP-over-UDP, 10-rtsps stream via SRTP-over-Multicast\n");
	printf("Input net mode(default is 1-private tcp connect):");
	scanf("%d", &iNetMode);
	tVideoPara.iSize = sizeof(NetClientPara);
	tVideoPara.tCltInfo.m_iServerID = s_iLogonID[0];
	tVideoPara.tCltInfo.m_iChannelNo = iVideoChan; 
	tVideoPara.tCltInfo.m_iStreamNO = 0;
	tVideoPara.tCltInfo.m_iNetMode = iNetMode;
	tVideoPara.tCltInfo.m_iTimeout = 20;
	tVideoPara.pCbkFullFrm = FullFrameCallback;	//Private frame format video data callback
	tVideoPara.pvCbkFullFrmUsrData = NULL;	
	tVideoPara.pCbkRawFrm = RawFrameCallback;	//Raw frame data (raw data) callback
	tVideoPara.pvCbkRawFrmUsrData = NULL;
	tVideoPara.iIsForbidDecode = 1;
	tVideoPara.pvWnd = NULL;	//console demo not show video
	iRet = NetClient_SyncRealPlay((unsigned int*)&s_tVideoConn[0].iReciveHandle, &tVideoPara, sizeof(NetClientPara));
	if (RET_SUCCESS == iRet)
	{
		printf("NetClient_StartRecvSync Success! uiRecvID=%u.\n", s_tVideoConn[0].iReciveHandle);
	}
	else if (RET_SYNCREALPLAY_TIMEOUT == iRet)
	{
		printf("NetClient_StartRecvSync Timeout!\n");
		goto END;
	}
	else
	{
		printf("NetClient_StartRecvSync fail!iRet=%d.\n", iRet);
		goto END;
	}

END:
	return iRet;
}

int StopRealPlay()
{
	//The demo sample only connects one channel of video, so only one channel is stopped
	if (s_tVideoConn[0].iDecodeHandle >= 0)
	{
		CLS_AVPlaySdkApi::Instance()->DeletePlayer(s_tVideoConn[0].iDecodeHandle);
		s_tVideoConn[0].iDecodeHandle = -1;
	}

	if (s_tVideoConn[0].iReciveHandle >= 0)
	{
		NetClient_StopRealPlay(s_tVideoConn[0].iReciveHandle, 0);
		s_tVideoConn[0].iReciveHandle = -1;
	}

	return 0;
}

int SyncQueryFile()
{
	//Users can modify the query period by themselves to ensure that there are videos in the modified time period
	
	time_t now;
	time(&now);
	struct  tm* now_time = localtime(&now);
	
	NETFILE_QUERY_V5 tQueryFileV5 = {0};
	tQueryFileV5.iType = 0xFF;
	tQueryFileV5.iQueryChannelNo = 0;
	tQueryFileV5.iStreamNo = 0;
	tQueryFileV5.tStartTime.iYear = 1900 + now_time->tm_year;
	tQueryFileV5.tStartTime.iMonth = 1 + now_time->tm_mon;
	tQueryFileV5.tStartTime.iDay = now_time->tm_mday;
	tQueryFileV5.tStartTime.iHour = now_time->tm_hour-1;
	tQueryFileV5.tStartTime.iMinute = 0;
	tQueryFileV5.tStartTime.iSecond = 0;

	tQueryFileV5.tStopTime.iYear = 1900 + now_time->tm_year;
	tQueryFileV5.tStopTime.iMonth = 1 + now_time->tm_mon;
	tQueryFileV5.tStopTime.iDay = now_time->tm_mday;
	tQueryFileV5.tStopTime.iHour = now_time->tm_hour;
	tQueryFileV5.tStopTime.iMinute = 0;
	tQueryFileV5.tStopTime.iSecond = 0;
	tQueryFileV5.iPageSize = MAX_QUERY_PAGE_COUNT;
	tQueryFileV5.iPageNo = 0;
	tQueryFileV5.iTrigger = 0;
	tQueryFileV5.iFiletype = 1;
	tQueryFileV5.iTriggerType = 0x7FFFFFFF;

	NVS_FILE_DATA tResult[MAX_QUERY_PAGE_COUNT] = {{0}};
	int iOutTotalLen = MAX_QUERY_PAGE_COUNT * sizeof(NVS_FILE_DATA);
	int iSingleLen = sizeof(NVS_FILE_DATA);
	int iRet = NetClient_SyncQuery(s_iLogonID[0], 0, CMD_NETFILE_QUERY_FILE, &tQueryFileV5, sizeof(NETFILE_QUERY_V5), &tResult, iOutTotalLen, iSingleLen);
	if (iRet < 0)
	{
		printf("Err: NetClient_Query_V5\n");
		return -1;
	}

	printf("TotalCount(%d), CurrentCount(%d)\n", tQueryFileV5.iTotalQueryCount, tQueryFileV5.iCurQueryCount);
	for(int i = 0; i < tQueryFileV5.iCurQueryCount; ++i)
	{          
		printf("File[%d] : Name(%s), Type(%d), Channel(%d), Size(%d)\n", i, tResult[i].cFileName
			, tResult[i].iType, tResult[i].iChannel, tResult[i].iFileSize); 
	}

	return 0;
}

int SyncQueryLog()
{
	NVS_LOG_QUERY tQueryLog = {0};
	tQueryLog.iChannelNo = 0xFF; //Query all channels
	tQueryLog.iLogType = 0xFF;		//0, system log, 1, warning, 2, alarm, 3, operation, 4, user, 5, other, 0xff, all types
	tQueryLog.iLanguage = 1;	//0, English; 1, GB2312 simplified; 2, BIG5 traditional ; 3, Korean; 4, Spain; 5, Italy,
	tQueryLog.iPageSize = MAX_QUERY_PAGE_COUNT;
	tQueryLog.iPageNo = 0;

	time_t now;
	time(&now);
	struct  tm* now_time = localtime(&now);


	tQueryLog.struStartTime.iYear = 1900 + now_time->tm_year;
	tQueryLog.struStartTime.iMonth = 1 + now_time->tm_mon;
	tQueryLog.struStartTime.iDay = now_time->tm_mday;
	tQueryLog.struStartTime.iHour = now_time->tm_hour - 1;
	tQueryLog.struStartTime.iMinute = 0;
	tQueryLog.struStartTime.iSecond = 0;

	tQueryLog.struStopTime.iYear = 1900 + now_time->tm_year;
	tQueryLog.struStopTime.iMonth = 1 + now_time->tm_mon;
	tQueryLog.struStopTime.iDay = now_time->tm_mday;
	tQueryLog.struStopTime.iHour = now_time->tm_hour;
	tQueryLog.struStopTime.iMinute = 0;
	tQueryLog.struStopTime.iSecond = 0;

	NVS_LOG_DATA tResult[MAX_QUERY_PAGE_COUNT] = {{0}};
	int iOutTotalLen = MAX_QUERY_PAGE_COUNT * sizeof(NVS_LOG_DATA);
	int iSingleLen = sizeof(NVS_LOG_DATA);
	int iRet = NetClient_SyncQuery(s_iLogonID[0], 0, CMD_NETFILE_QUERY_LOG, &tQueryLog, sizeof(NVS_LOG_QUERY), &tResult, iOutTotalLen, iSingleLen);
	if (iRet < 0)
	{
		printf("Err: NetClient_Query_V5 fail!iRet=%d.\n", iRet);
		return -1;
	}

	int iTotalCount = 0;
	int iCurrentCount = 0;
	iRet = NetClient_NetLogGetLogCount(s_iLogonID[0], &iTotalCount, &iCurrentCount);
	if (iRet < 0)
	{
		printf("Err: NetClient_NetLogGetLogCount fail!iRet=%d.\n", iRet);
		return -1;
	}

	printf("TotalCount(%d), CurrentCount(%d)\n", iTotalCount, iCurrentCount);
	for(int i = 0; i < iCurrentCount; ++i)
	{          
		printf("File[%d] : Type(%d), Content(%s)\n", i, tResult[i].iLogType, tResult[i].szLogContent); 
	}

	return 0;
}

int SyncSuspendVca()
{
	VCASuspend tInPara = {0};
	tInPara.iStatus = 0;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
	tInPara.iDevType = 0;	//0-IPC, 1-NVR

	VCASuspendResult tOutResult = {0};
	tOutResult.iBufSize = sizeof(VCASuspendResult);
	tOutResult.iDevType = 0;
	int iRet = NetClient_SyncSetDevCfg(s_iLogonID[0], 0, SYNC_NET_CLIENT_VCA_SUSPEND, &tInPara, sizeof(VCASuspend), &tOutResult, sizeof(VCASuspendResult));
	if (RET_SUCCESS == iRet)
	{
		printf("Suspend Vca success!\n");
	}
	else if (RET_SYNCSUSPENDVCA_CONFIGING == iRet)
	{
		printf("Suspend Vca fail, Vca Configing by other client!\n");
	}
	else if (RET_SYNCSUSPENDVCA_FAIL == iRet)
	{
		printf("Suspend Vca fail, device not return code!\n");
	}

	return 0;
}

int SyncOpenVca()
{
	VCASuspend tInPara = {0};
	tInPara.iStatus = 1;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
	tInPara.iDevType = 0;	//0-IPC, 1-NVR

	VCASuspendResult tOutResult = {0};
	tOutResult.iBufSize = sizeof(VCASuspendResult);
	tOutResult.iDevType = 0;
	int iRet = NetClient_SyncSetDevCfg(s_iLogonID[0], 0, SYNC_NET_CLIENT_VCA_SUSPEND, &tInPara, sizeof(VCASuspend), &tOutResult, sizeof(VCASuspendResult));
	if (RET_SUCCESS == iRet)
	{
		printf("Open Vca success!\n");
	}
	else if (RET_SYNCOPENVCA_CONFIGING == iRet)
	{
		printf("Open Vca fail, Vca Configing by other client!\n");
	}
	else if (RET_SYNCOPENVCA_FAIL == iRet)
	{
		printf("Open Vca fail, device not return code!\n");
	}

	return 0;
}

int SyncSetPtz()
{
	SetPtz tSetPtz = {0};
	tSetPtz.iSize = sizeof(SetPtz);
	tSetPtz.iType = 1;		//0-control speed 1-control position 2-relative control position
	tSetPtz.iPan = 18000;	//When iType is 0, it means horizontal speed (0-100)
							//When iType is 1, it means the moving target horizontal position is 0-36000, corresponding to 0-360 degrees, accurate to 0.01
							//When iType is 2, it means the relative movement angle is 0-36000, which means -180~180 degrees
	tSetPtz.iTilt = 8000;	//When iType is 0, it means vertical speed (0-100)
							//When iType is 1 and 2, it means that the vertical position of the moving target is 1000~19000, corresponding to -90-90 degrees, accurate to 0.01
	tSetPtz.iZoom = 50000;	//When iType is 0, it means zoom speed (0-100)
							//When iType is 1, it means that the moving target zoom position 0~100000 corresponds to 0-1000 times, accurate to 0.01
							//When iType is 2, it means the relative movement multiple is 0~100000, corresponding to -500~500 times, accurate to 0.01

	int iRet = NetClient_SendCommand(s_iLogonID[0], COMMAND_ID_SYNC_SETPTZ, 0, &tSetPtz, sizeof(SetPtz));
	if(RET_SUCCESS == iRet) {
		printf("NetClient_SendCommand COMMAND_ID_SYNC_SETPTZ success\n");
	} else {
		printf("NetClient_SendCommand COMMAND_ID_SYNC_SETPTZ fail, iRet=%d\n", iRet);
	}

	return 0;
}

int SyncGetPtz()
{
	GetPtz tGetPtz = {0};
	tGetPtz.iSize = sizeof(GetPtz);
	int iRet = NetClient_RecvCommand(s_iLogonID[0], COMMAND_ID_SYNC_GETPTZ, 0, &tGetPtz, sizeof(GetPtz));
	if(RET_SUCCESS == iRet) {
		printf("NetClient_RecvCommand COMMAND_ID_SYNC_GETPTZ success, iPosP=%d, iPosT=%d, iPosZ=%d\n", tGetPtz.iPosP, tGetPtz.iPosT, tGetPtz.iPosZ);
	} else {
		printf("NetClient_RecvCommand COMMAND_ID_SYNC_GETPTZ fail, iRet=%d\n", iRet);
	}

	return 0;
}

//A kind of Uidechandle playback handle_ Pcyuvdata decodes YUV data_ Iyuvlen data length_ Pframeinfo decodes data information_ Iuser user data (set callback is incoming)
void DecYuvCallback (unsigned int _uiDecHandle, unsigned char *_pcYuvData, int _iYuvLen, const FRAME_INFO *_pFrameInfo, void* _iUser)
{
	if (NULL == _pcYuvData || _iYuvLen <= 0 || NULL == _pFrameInfo)
	{
		return;
	}
	
	int iLen = _pFrameInfo->nHeight*_pFrameInfo->nWidth*3/2;
	if (_iYuvLen != iLen)
	{
		return;
	}
	printf("DecYuvCallback, _iLen=%d.\n", _iYuvLen);
	//do something
}

//A kind of Iuser: file header data_ Iuserdata: user data
void __stdcall FullFrameCallback(unsigned int _uiID, unsigned int _uiStreamType, unsigned char* _pucData, int _iLen, void* _pvHead, void* _pvUsrData)
{
	//Find connection information
	int iIndex = -1;
	for (int i = 0; i < MAX_VIDEO_COUNT; ++i)
	{
		if (s_tVideoConn[i].iReciveHandle == (int)_uiID)
		{
			iIndex = i;
			break;
		}
	}
	//This connection was not found
	if (iIndex < 0)
	{
		printf("find connect info failed, uiId=%d, iReciveHandle=%d.\n", _uiID, s_tVideoConn[0].iReciveHandle);
		return;
	}

	if (STREAM_INFO_SYSHEAD == _uiStreamType) //Video header data callback, need to create or re create decoder
	{
		//There is a playback handle. Delete it first
		if (s_tVideoConn[iIndex].iDecodeHandle >= 0)
		{
			CLS_AVPlaySdkApi::Instance()->DeletePlayer(s_tVideoConn[iIndex].iDecodeHandle);
			s_tVideoConn[iIndex].iDecodeHandle = -1;
		}
		//Create playback handle
		int iDecodeHandle = CLS_AVPlaySdkApi::Instance()->CreatePlayer(E_STREAM_ONLYDECODE, _pucData, _iLen, NULL);
		if (iDecodeHandle < 0)
		{
			printf("TC_CreatePlayer failed, len=%d.\n", _iLen);
			return;
		}
		//Save decode handle
		s_tVideoConn[iIndex].iDecodeHandle = iDecodeHandle;

		//Set YUV data callback after decoding
		CLS_AVPlaySdkApi::Instance()->SetDecCallBack(iDecodeHandle, DecYuvCallback, NULL);
	}
	else if (STREAM_INFO_STREAMDATA == _uiStreamType)	//Video data callback, sent to decoder decoding
	{
		if (s_tVideoConn[iIndex].iDecodeHandle >= 0)
		{
			CLS_AVPlaySdkApi::Instance()->PutStreamToPlayer(s_tVideoConn[iIndex].iDecodeHandle, _pucData, _iLen);
		}
	}
	//printf("FullFrame:_uiID=%u, _uiStreamType=%u, _iLen=%d.\n", _uiID, _uiStreamType, _iLen);
}

void __stdcall RawFrameCallback(unsigned int _uiID, unsigned char* _pucData, int _iLen, RAWFRAME_INFO* _ptRawFrameInfo, void* _pvUsrData)
{
//	printf("RawFrame:_uiID=%u, _iLen=%d.\n", _uiID, _iLen);
}


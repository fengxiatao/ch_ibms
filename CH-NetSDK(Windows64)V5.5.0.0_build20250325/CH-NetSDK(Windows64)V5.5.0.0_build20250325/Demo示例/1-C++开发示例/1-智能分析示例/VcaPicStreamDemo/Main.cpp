#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "NetSdk.h"
#include "IniFile.h"
#include <iostream>
#include <fstream>

#define MAX_SAVE_PCTURE_COUNT	20000
const char g_cVcaEventType[][64] = {"Single trip wire", "Double trip wire", "Perimeter detection", "linger", "parking", "running", "Personnel density in the area", "Abandoned objects", "Stolen goods",  "Face recognition"
, "Video diagnosis", "Intelligent tracking", "Traffic statistics", "The crowd gathered", "Off duty detection", "Water level monitoring", "Audio diagnosis", "Face occlusion", "Floating debris in river course", "Illegal mining and unloading"
, "Illegal parking", "fight", "Alert", "License plate recognition", "Heat map", "Water monitoring", "Window detection", "Face recognition", "Parking guard", "", "Safety helmet detection algorithm"};

const char* g_color[] = {"unknow", "white","gray", "brown", "red", "blue", "yellow", "green", "pink", "orange", "cyan", "purple", "lightblue", "black", "colors"};
#define MAX_ATTR_COLOR_NUM			(sizeof(g_color)/sizeof(char*))

const char* g_cTargetType[] = {"unknow", "person","other", "car"};
#define MAX_ATTR_TARGETTYPE_NUM			(sizeof(g_cTargetType)/sizeof(char*))

#define CONFIG_FILE_PATH "./VcaPicStreamDemo.ini"

#ifndef __WIN__
	#include <unistd.h>
	#include <sys/types.h>
	#include <sys/stat.h> 
	int gets_s(char *_pcStr, int _iCount);
	#define min(a,b)	(((a) < (b)) ? (a) : (b))
#else
	#include<direct.h>
	void usleep(int _iMicroSecond);
#endif

int g_iLogonID = -1;
unsigned int g_uiRecvID = -1;
int m_iChannelNo = 0;
int to_int_def(const char* _pstrFrom, int _iDef = 0);
void LogonDevice(int _iLogonType,int _iConfig = 0);
void StartRecvPicture(void);
void StopRecvPicture(void);
void Notify_Main(int _iLogonID, long  _lWparam, void*  _pvLParam, void* _pvUsr);
void Alarm_Notify( int _iLogonID, int _iChan, int _iAlarmState, ALARMTYPE _iAlarmType, void* _pvUser);
void ParamChangeNotify( int _iLogonID, int _iChan, PARATYPE _iParaType, STR_Para* _pPara, void* _pvUser);
int __stdcall Notify_NetPicStream(unsigned int _uiRecvID, long _lCommand, void* _pvCallBackInfo, int _iBufLen, void* _pvUser);

int main(int argc,char *argv[])
{
    std::ifstream file(CONFIG_FILE_PATH);
    int iConfig = 0;
    if (file.is_open()) {
        iConfig = 1;
        file.close();
    } 

	LoadNVSSDK();

	int iLogonType = SERVER_NORMAL;
	char cTemp[8] = {0};
    if (1 == iConfig)
    {
        iLogonType =readIntValue((char*)"connect", (char*)"type", 2048, CONFIG_FILE_PATH);
    }
    else
    {
        fprintf(stderr, "Please input LogonType: 0----Normal  1----Active\n");
        gets_s(cTemp, 8);
        iLogonType = to_int_def(cTemp, 0);
    }
	
	
    
	if (SERVER_ACTIVE == iLogonType)
	{
		fprintf(stderr, "Please input listening port:");
		gets_s(cTemp, 8);
		int iLlisteningPort = to_int_def(cTemp, 0);
		NetClient_Startup_V4(iLlisteningPort, 0, 0);
	}
	else
	{
		NetClient_Startup_V4(0, 0, 0);
	}
	
	//Register main callback function
	NetClient_SetNotifyFunction_V4(Notify_Main, Alarm_Notify, ParamChangeNotify, NULL, NULL);

	//Login device
	LogonDevice(iLogonType,iConfig);

#ifdef __WIN__
	_mkdir(".\\PicStream");
#else
	mkdir(".//PicStream", S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH); 
#endif

	getchar();

	StopRecvPicture(); 
	NetClient_Logoff(g_iLogonID);
	NetClient_Cleanup();

	return 0;
}

int to_int_def(const char* _pstrFrom, int _iDef)
{
	if(NULL == _pstrFrom)
	{
		return _iDef;
	}

	for (size_t i=0; i<strlen(_pstrFrom); i++)
	{
		if (!isdigit(_pstrFrom[i]))
		{
			if(i == 0 && _pstrFrom[i] == '-' && strlen(_pstrFrom) > 1)
			{
				continue;
			}
			return _iDef;
		}
	}

	return atoi(_pstrFrom);
}

#ifndef __WIN__
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
void usleep(int _iMicroSecond)
{
	if (_iMicroSecond < 1000 && _iMicroSecond != 0)
	{
		_iMicroSecond = 1000;
	}
	Sleep(_iMicroSecond/1000);
}
#endif

void LogonDevice(int _iLogonType,int _iConfig)
{
	char cIP[16] = {0};
	char cUserName[16] = {0};
	char cPassword[16] = {0};
	char cProductID[32] = {0};
	char cChannelID[LEN_8] = {0};


    if (1 == _iConfig)
    {
        readStringValue("connect", "username", cUserName, CONFIG_FILE_PATH);
        readStringValue("connect", "password", cPassword, CONFIG_FILE_PATH);
        fprintf(stderr, "cUserName %s cPassword %s\n",cUserName,cPassword);
    }
    else
    {
         fprintf(stderr, "Please input user name: ");
         gets_s(cUserName, 16);
         fprintf(stderr, "Please input password: ");
         gets_s(cPassword, 16);
    }

	LogonPara tNormal = {0};
	LogonActiveServer tActive = {0};
	void* pvPara = NULL;
	int iBufLen = 0;
	if (SERVER_ACTIVE == _iLogonType)
	{
		fprintf(stderr, "Please input channel: ");
		gets_s(cChannelID, 8);
		m_iChannelNo = to_int_def(cChannelID, 0);
		fprintf(stderr, "Please input ProductID: ");
		gets_s(cProductID, 32);
		tActive.iSize = sizeof(LogonActiveServer);
		strcpy(tActive.cUserName, cUserName);
		strcpy(tActive.cUserPwd, cPassword);
		strcpy(tActive.cProductID, cProductID);
		pvPara = &tActive;
		iBufLen = sizeof(LogonActiveServer);

		DsmOnline tOnline = {0};
		tOnline.iSize = sizeof(DsmOnline);
		strncpy(tOnline.cProductID, cProductID, LEN_32);
		NetClient_GetDsmRegstierInfo(DSM_CMD_GET_ONLINE_STATE, &tOnline, sizeof(DsmOnline));
		int iOutTime = 0;
		while (DSM_STATE_ONLINE != tOnline.iOnline)
		{
			if (iOutTime >= 20)
			{
				fprintf(stderr, "Device not register!\n");
				break;
			}
			usleep(1000 * 1000);
			NetClient_GetDsmRegstierInfo(DSM_CMD_GET_ONLINE_STATE, &tOnline, sizeof(DsmOnline));
			iOutTime++;
		}
	}
	else
	{
        if (1==_iConfig)
        {
            readStringValue("connect", "ip", cIP, CONFIG_FILE_PATH);
            readStringValue("connect", "channel", cChannelID, CONFIG_FILE_PATH);
            fprintf(stderr, "ip %s channel %s\n",cIP,cChannelID);
        }
        else
        {
             fprintf(stderr, "Please input server IP: ");
             gets_s(cIP, 16);
             fprintf(stderr, "Please input channel: ");
             gets_s(cChannelID, 8);
        }


		m_iChannelNo = to_int_def(cChannelID, 0);
		tNormal.iSize = sizeof(LogonPara);
		tNormal.iNvsPort = 3000;
		strcpy(tNormal.cNvsIP, cIP);
		strcpy(tNormal.cUserName, cUserName);
		strcpy(tNormal.cUserPwd, cPassword);
		strcpy(tNormal.cCharSet, "UTF-8");
		pvPara = &tNormal;
		iBufLen = sizeof(LogonPara);
	}

	g_iLogonID = NetClient_Logon_V4(_iLogonType, pvPara, iBufLen);
	if(g_iLogonID < 0)
	{
		fprintf(stderr,"[NetClient_Logon_V4] fail! %d\n", g_iLogonID);
	}
}

void StartRecvPicture(void)
{
	NetPicPara tPara = {0};
	tPara.iStructLen = sizeof(NetPicPara);

	tPara.iChannelNo = m_iChannelNo;
	tPara.cbkPicStreamNotify = Notify_NetPicStream;
	tPara.pvUser = NULL;
	int iRet = NetClient_StartRecvNetPicStream(g_iLogonID, &tPara, sizeof(NetPicPara), &g_uiRecvID);
	if (0 != iRet)
	{
		fprintf(stderr,"[NetClient_StartRecvNetPicStream] fail!");
	}
}

void StopRecvPicture(void)
{
	int iRet = NetClient_StopRecvNetPicStream(g_uiRecvID);
	if (0 != iRet)
	{
		fprintf(stderr,"[NetClient_StopRecvNetPicStream] fail!");
	}
}

void Alarm_Notify( int _iLogonID, int _iChan, int _iAlarmState, ALARMTYPE _iAlarmType, void*  _pvUser)
{
	switch (_iAlarmType)
	{
	case ALARM_VDO_MOTION:				//Video Motion Alarm
		{
			printf("%d ALARM_VDO_MOTION!\n", _iChan);
			break;
		} 
	case ALARM_VDO_REC:					//Video recording alarm
		{
			printf("%d ALARM_VDO_REC!\n", _iChan);
			break;
		} 
	case ALARM_VDO_LOST:				//Video loss alarm
		{
			printf("%d ALARM_VDO_LOST!\n", _iChan);
			break;
		} 
	case ALARM_VDO_INPORT:				//Input port alarm
		{
			printf("%d ALARM_VDO_INPORT!\n", _iChan);
			break;
		} 
	case ALARM_VDO_OUTPORT:				//output port alarm
		{
			printf("%d ALARM_VDO_OUTPORT!\n", _iChan);
			break;
		} 
	case ALARM_VDO_COVER:				//Video occlusion alarm
		{
			printf("%d ALARM_VDO_COVER!\n", _iChan);
			break;
		}
	case ALARM_VCA_INFO_EX:
		{
			int iVcaAlarmInfoIndex = _iAlarmState;
			vca_TAlarmInfo tVcaAlarmInfo = {0};
			//Obtain the current intelligent analysis alarm information according to the alarm index
			int iRet = NetClient_VCAGetAlarmInfo(g_iLogonID, iVcaAlarmInfoIndex, &tVcaAlarmInfo, sizeof(vca_TAlarmInfo));
			if (iRet < 0) {
				break;
			}
			printf("%d ALARM_VCA EventType=(%d) State=(%d)!\n", _iChan, tVcaAlarmInfo.iEventType, tVcaAlarmInfo.iState);
			break;
		}
	}
}

void Notify_Main(int _iLogonID, long _lWparam, void* _pvLParam, void* _pvUsr)
{
	int iMsgType = LOWORD(_lWparam);
	long lMsgValue = (long)_pvLParam;
	switch (iMsgType)
	{
	case WCM_LOGON_NOTIFY:
		{
			printf("WCM_LOGON_NOTIFY!\n");
			if((LOGON_SUCCESS == lMsgValue) && (-1 == (int)g_uiRecvID))
			{
				printf("Logon success!\n");
				StartRecvPicture(); 
			}
			else
			{
				printf("Logon failed!\n");
			}
		}
		break;
	default:
		break;
	}
}

void ParamChangeNotify( int _iLogonID, int _iChan, PARATYPE _iParaType, STR_Para* _pPara, void* _pvUser)
{
	if( NULL == _pPara)
	{
		return;
	}
	if(_iLogonID < 0 || _iChan < 0 )
	{
		printf("[ParamChangeNotify] error para, _iLogonID = %d, _iChan = %d\n", _iLogonID, _iChan);
	}
	switch (_iParaType)
	{
	case PARA_VCA_ALARMSTAT:
		{
			STR_Para* _strPara;
			_strPara = (STR_Para*) _pPara;

			int iIn = (int)(long)_strPara->m_iPara[0];
			int iOut = (int)(long)_strPara->m_iPara[1];
			int iInDiff = (int)(long)_strPara->m_iPara[2];
			int iOutDiff = (int)(long)_strPara->m_iPara[3];
			printf("[ParamChangeNotify] InNumber=%d,OutNumber=%d,InDiff=%d,OutDiff=%d\n", iIn, iOut, iInDiff, iOutDiff);
		}
		break;
	default:
		break;
	}

}

int g_iCount = 0;
int __stdcall Notify_NetPicStream(unsigned int _uiRecvID, long _lCommand, void* _pvCallBackInfo, int _iBufLen, void* _pvUser)
{
	if (NULL == _pvCallBackInfo)
	{
		return -1;
	}

	if (_uiRecvID != g_uiRecvID)
	{
		return -1;
	}

	if (g_iCount >= MAX_SAVE_PCTURE_COUNT)
	{
		printf("save picture over 20000!\n");
		return -1;
	}

	if (NET_PICSTREAM_CMD_VCA == _lCommand)
	{
		VcaPicStream* ptVca = (VcaPicStream*)_pvCallBackInfo;
		int iEventType = ptVca->iEventType;
		if (iEventType >= 0 && iEventType < (int)(sizeof(g_cVcaEventType) / 64))
		{
			printf("picture info:iWidth(%d),iHeight(%d),iPicCount(%d),cCameraIP(%s),iEventType(%s)\n"
				, ptVca->iWidth, ptVca->iHeight, ptVca->iPicCount, ptVca->cCameraIP, g_cVcaEventType[iEventType]);
		}
		
		char cTargetType[LEN_32] = {0};
		if (ptVca->iTargetType >= 0 && ptVca->iTargetType < MAX_ATTR_TARGETTYPE_NUM)
		{
			sprintf(cTargetType, "%s", g_cTargetType[ptVca->iTargetType]);
		}
		else
		{
			sprintf(cTargetType, "%d", ptVca->iTargetType);
		}

		char cColor[LEN_32] = {0};
		if (VCA_EVENT_ACTIVE_STRATEGY == ptVca->iEventType)
		{
			int iColor = (int)ptVca->cTmp[0];
			if (iColor >= 0 && iColor < MAX_ATTR_COLOR_NUM)
			{
				sprintf(cTargetType, "%s", g_color[iColor]);
			}
			else
			{
				sprintf(cColor, "%d", iColor);
			}
		}
		

		PicData tPicData = {{0}};
		for (int i = 0; i < ptVca->iPicCount; ++i)
		{
			if (NULL == ptVca->ptPicData[i])
			{
				continue;
			}

			memset(&tPicData, 0, sizeof(PicData));
			memcpy(&tPicData, ptVca->ptPicData[i], min(ptVca->iSize, (int)sizeof(PicData)));
			char cFileName[256] = {0};
			sprintf(cFileName, ".//PicStream//VcaPic_%s_No%d_Time(2%03d%02d%02d%02d%02d%02d%d_targettype-%s_color-%s).jpg", ptVca->cCameraIP, g_iCount++ 
				, tPicData.tPicTime.uiYear, tPicData.tPicTime.uiMonth,  tPicData.tPicTime.uiDay, tPicData.tPicTime.uiHour
				, tPicData.tPicTime.uiMinute, tPicData.tPicTime.uiSecondsr, tPicData.tPicTime.uiMilliseconds, cTargetType, cColor);
			FILE* pFile  = fopen(cFileName, "wb");
			if (NULL != pFile)
			{
				fwrite(tPicData.pcPicData, tPicData.iDataLen, 1, pFile);
				fclose(pFile);
				pFile = NULL;
			}
		}
		if (ptVca->iPtzInfoLen > 0  && ptVca->iPtzInfoLen  <= sizeof(PtzInfo) && ptVca->pPtzInfo != NULL)
		{
			printf("PtzInfo: iPtzInfoLen=%d\n", ptVca->iPtzInfoLen);
			printf("PtzInfo: iPanPosition=%d, iTiltPosition=%d, iZoomPosition=%d\n"
				, ptVca->pPtzInfo->iPanPosition, ptVca->pPtzInfo->iTiltPosition, ptVca->pPtzInfo->iZoomPosition);
			printf("PtzInfo: iNorthAngle=%d, iPanStep=%d, iTiltStep=%d\n"
				, ptVca->pPtzInfo->iNorthAngle, ptVca->pPtzInfo->iPanStep, ptVca->pPtzInfo->iTiltStep);
		}
		if (ptVca->iDevGPSInfoLen > 0  && ptVca->iDevGPSInfoLen  <= sizeof(DevGPSInfo) && ptVca->pDevGps != NULL)
		{
			printf("DevGpsInfo: iDevGPSInfoLen=%d\n", ptVca->iDevGPSInfoLen);
			printf("DevGpsInfo: iLongitude=%d, iLongitudeDegree=%d, iLongitudeMinute=%d, iLongitudeSecond=%d\n"
				, ptVca->pDevGps->iLongitude, ptVca->pDevGps->iLongitudeDegree, ptVca->pDevGps->iLongitudeMinute, ptVca->pDevGps->iLongitudeSecond);
			printf("DevGpsInfo: iLatitude=%d, iLatitudeDegree=%d, iLatitudeMinute=%d, iLatitudeSecond=%d\n"
				, ptVca->pDevGps->iLatitude, ptVca->pDevGps->iLatitudeDegree, ptVca->pDevGps->iLatitudeMinute, ptVca->pDevGps->iLatitudeSecond);
		}
		if (ptVca->iAlarmGPSInfoLen > 0 && ptVca->iAlarmGPSInfoLen <= sizeof(EventTargetGPSInfo) && ptVca->pAlarmGPSInfo != NULL)
		{
			printf("AlarmGPSInfo: iDevGPSInfoLen=%d, iEventType=%d, iTargetId=%d\n",ptVca->iAlarmGPSInfoLen, ptVca->pAlarmGPSInfo->iEventType, ptVca->pAlarmGPSInfo->iTargetId);
			printf("AlarmGPSInfo: iLongitude=%d, iLongitudeDegree=%d, iLongitudeMinute=%d, iLongitudeSecond=%d\n"
				, ptVca->pAlarmGPSInfo->iLongitude, ptVca->pAlarmGPSInfo->iLongitudeDegree, ptVca->pAlarmGPSInfo->iLongitudeMinute, ptVca->pAlarmGPSInfo->iLongitudeSecond);
			printf("AlarmGPSInfo: iLatitude=%d, iLatitudeDegree=%d, iLatitudeMinute=%d, iLatitudeSecond=%d\n"
				, ptVca->pAlarmGPSInfo->iLatitude, ptVca->pAlarmGPSInfo->iLatitudeDegree, ptVca->pAlarmGPSInfo->iLatitudeMinute, ptVca->pAlarmGPSInfo->iLatitudeSecond);
		}
	}

	return 0;
}




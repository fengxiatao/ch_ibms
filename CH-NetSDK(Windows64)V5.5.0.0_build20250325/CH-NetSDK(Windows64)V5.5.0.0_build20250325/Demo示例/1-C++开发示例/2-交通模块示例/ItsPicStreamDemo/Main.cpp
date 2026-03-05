#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "NetSdk.h"

#define MAX_SAVE_PCTURE_COUNT	20000
const char g_cPlateColor[][64] = {"unknown", "White characters on a blue background", "Black characters on yellow background", "Black characters on white background", "White characters on black background", "White characters on green background"};
const char g_cCarColor[][64] = {"White", "Red", "Yellow", "Yellow", "Blue", "Green", "Green", "Purple", "Pink", "Block"
								, "Red", "Yellow", "Yellow", "Grey", "Yellow", "Blue", "Blue", "Yellow", "Green", "White"
								, "Green", "Cyan", "yellow", "Red", "Blue", "Blue", "Grey", "Purple", "Purple", "Brown"
								, "Brown", "Brown"};

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
int to_int_def(const char* _pstrFrom, int _iDef = 0);
void LogonDevice(int _iLogonType);
void StartRecvPicture(void);
void StopRecvPicture(void);
void Notify_Main(int _iLogonID, long  _lWparam, void*  _pvLParam, void* _pvUsr);
int __stdcall Notify_NetPicStream(unsigned int _uiRecvID, long _lCommand, void* _pvCallBackInfo, int _BufLen, void* _pvUser);

FILE* OpenFile(char* _pcPath)
{
	FILE* pFile  = NULL;
	pFile = fopen(_pcPath, "wb");
	return pFile;
}

int main(int argc,char *argv[])
{
	LoadNVSSDK();//Initialization interface library

	int iLogonType = SERVER_NORMAL;
	char cTemp[8] = {0};
	fprintf(stderr, "Please input LogonType: 0----Normal  1----Active\n");
	gets_s(cTemp, 8);
	iLogonType = to_int_def(cTemp, 0);
	if (SERVER_ACTIVE == iLogonType)
	{
		fprintf(stderr, "Please input listening port:");
		gets_s(cTemp, 8);
		int iLlisteningPort = to_int_def(cTemp, 0);
		NetClient_Startup_V4(iLlisteningPort, 0, 0);//initialization SDK
	}
	else
	{
		NetClient_Startup_V4(0, 0, 0);//initialization SDK
	}
	
	//Register main callback function
	NetClient_SetNotifyFunction_V4(Notify_Main, NULL, NULL, NULL, NULL);

	//Login device
	LogonDevice(iLogonType);

	//In the main callback notify_ After receiving the message of successful login in main, it starts to receive the image stream
	//Create storage picture directory
#ifdef __WIN__
	_mkdir(".\\PicStream");
#else
	mkdir(".//PicStream", S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH); 
#endif

	getchar();//Waiting for user input

	StopRecvPicture(); //Stop receiving picture stream
	NetClient_Logoff(g_iLogonID);//Log off user
	NetClient_Cleanup();//Release SDK resources

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

void LogonDevice(int _iLogonType)
{
	char cIP[16] = {0};
	char cUserName[16] = {0};
	char cPassword[16] = {0};
	char cProductID[32] = {0};

	fprintf(stderr, "Please input user name: ");
	gets_s(cUserName, 16);
	fprintf(stderr, "Please input password: ");
	gets_s(cPassword, 16);

	LogonPara tNormal = {0};
	LogonActiveServer tActive = {0};
	void* pvPara = NULL;
	int iBufLen = 0;
	if (SERVER_ACTIVE == _iLogonType)
	{
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
		strcpy(tOnline.cProductID, cProductID);
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
		fprintf(stderr, "Please input server IP: ");
		gets_s(cIP, 16);
		tNormal.iSize = sizeof(LogonPara);
		tNormal.iNvsPort = 3000;
		strcpy(tNormal.cNvsIP,  cIP);
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
	tPara.iChannelNo = 0;
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

void Notify_Main(int _iLogonID, long  _lWparam, void*  _pvLParam, void* _pvUsr)
{
	int iMsgType = LOWORD(_lWparam);
	int iMsgValue = (int)(long)_pvLParam;
	switch (iMsgType)
	{
	case WCM_LOGON_NOTIFY:
		{
			printf("WCM_LOGON_NOTIFY!\n");
			if((LOGON_SUCCESS == iMsgValue) && (-1 == g_uiRecvID))
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

int g_iCount = 0;
int __stdcall Notify_NetPicStream(unsigned int _uiRecvID, long _lCommand, void* _pvCallBackInfo, int _BufLen, void* _pvUser)
{
	if (NULL == _pvCallBackInfo)
	{
		return -1;
	}

	if (_uiRecvID != g_uiRecvID)
	{
		return -1;
	}

	if(NET_PICSTREAM_CMD_ITS != _lCommand)
	{
		return 0;
	}

	if (g_iCount++ >= MAX_SAVE_PCTURE_COUNT)
	{
		printf("save picture over 20000!\n");
		return -1;
	}

	ItsPicStream tIts = {0};
	memcpy(&tIts, _pvCallBackInfo, min(_BufLen, (int)sizeof(ItsPicStream)));
	//License plate color
	int iPlateColor = tIts.iPlateColor;
	char cPlateColor[64] = {0};
	if (iPlateColor < 0 || iPlateColor > (int)(sizeof(g_cPlateColor) / 64))
	{
		iPlateColor = 0;		
	}
	sprintf(cPlateColor, "%s", g_cPlateColor[iPlateColor]);
	
	//Body color
	int iCarColor = tIts.iCarColor;
	char cCarColor[32] = "Unknown";
	if (iCarColor >= 0 && iCarColor < sizeof(g_cCarColor) / 64)
	{
		sprintf(cCarColor, "%s", g_cCarColor[iPlateColor]);
	}

	//Vehicle weighing ID
	ITS_TCZInfo tTCZInfo = {0};
	if(NULL != tIts.pITS_TCZInfo)
	{
		memcpy(&tTCZInfo, tIts.pITS_TCZInfo, min(tIts.iITS_TCZInfoLen, sizeof(ITS_TCZInfo)));
	}
	
	//Output information
	char cMsg[1024] = {0};
	sprintf(cMsg, "picture info:iPicCount(%d),cCameraIP(%s),cPlate(%s),PlateColor(%s),CarColor(%s),CZCarID(%d)\n", tIts.iPicCount, tIts.cCameraIP, tIts.cPlate, cPlateColor, cCarColor, tTCZInfo.iCarID);
	printf(cMsg);

	//Output whistle capture license plate recognition results
	if (NULL != tIts.pPlateRecognize)
	{
		RecgPlateList tPlateList = {0};
		memcpy(&tPlateList, tIts.pPlateRecognize, min(tIts.iPlateRecognizeLen, sizeof(RecgPlateList)));
		for (int i = 0;i<tPlateList.iPlateCount && i<MAX_RECOGNIZE_PLATE_COUNT;i++)
		{
			char cMsgPlate[1024] = {0};
			sprintf(cMsgPlate, "Plate list recognize info:cCameraIP(%s),PlateCount(%d),cPlate(%s),iPlateColor(%s),iPlateType(%d)\n"
				, tIts.cCameraIP, tPlateList.iPlateCount, tPlateList.tPlateInfo[i].cPlate, g_cPlateColor[tPlateList.tPlateInfo[i].iPlateColor], tPlateList.tPlateInfo[i].iPlateType);
			printf(cMsgPlate);
		}
	}
	
	//Save snapshot	
	for (int i = 0; i < tIts.iPicCount && i < MAX_ITS_CAP_PIC_COUNT; ++i)
	{
		if (NULL == tIts.ptPicData[i])
		{
			continue;
		}
		PicData tData = {0};
		memcpy(&tData, tIts.ptPicData[i], min(tIts.iSize, sizeof(PicData)));
		char cFileName[256] = {0};
		sprintf(cFileName, ".//PicStream//ItsPic_%s_No%d_Time(2%03d%02d%02d%02d%02d%02d%d)_%d.jpg", tIts.cCameraIP, g_iCount
		, tData.tPicTime.uiYear, tData.tPicTime.uiMonth,  tData.tPicTime.uiDay, tData.tPicTime.uiHour
		, tData.tPicTime.uiMinute, tData.tPicTime.uiSecondsr, tData.tPicTime.uiMilliseconds, i);
		FILE* pFile  = OpenFile(cFileName);
		if (NULL != pFile)
		{
			fwrite(tData.pcPicData, tData.iDataLen, 1, pFile);
			fclose(pFile);
			pFile = NULL;
		}
	}
	//Save license plate image
	if (tIts.iPlatCount > 0 && NULL != tIts.ptPlatData)
	{
		PicData tData = {0};
		memcpy(&tData, tIts.ptPlatData, min(tIts.iSize, sizeof(PicData)));
		char cFileName[256] = {0};
		sprintf(cFileName, ".//PicStream//ItsPlat_%s_No%d-CP.jpg", tIts.cCameraIP, g_iCount);
		FILE* pFile  = OpenFile(cFileName);
		if (NULL != pFile)
		{
			fwrite(tData.pcPicData, tData.iDataLen, 1, pFile);
			fclose(pFile);
			pFile = NULL;
		}
	}
	//Save face image
	for (int i = 0; i < tIts.iFaceCount && i < MAX_ITS_CAP_FACE_COUNT; ++i)
	{
		if (NULL == tIts.ptFaceData[i])
		{
			continue;
		}

		PicData tData = {0};
		memcpy(&tData, tIts.ptFaceData[i], min(tIts.iSize, sizeof(PicData)));

		char cFileName[256] = {0};
		sprintf(cFileName, ".//PicStream//ItsFace_%s_No%d-RL_%d.jpg", tIts.cCameraIP, g_iCount,i);
		FILE* pFile  = OpenFile(cFileName);
		if (NULL != pFile)
		{
			fwrite(tData.pcPicData, tData.iDataLen, 1, pFile);
			fclose(pFile);
			pFile = NULL;
		}
	}
	return 0;
}




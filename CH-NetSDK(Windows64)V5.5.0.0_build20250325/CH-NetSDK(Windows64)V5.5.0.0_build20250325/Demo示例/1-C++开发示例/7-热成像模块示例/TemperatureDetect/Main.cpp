
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "NetSdk.h"

int g_iLogonId = -1;
int g_iConnectId = -1;					
int g_iVcaStatus = 0;

FaceLibInfo g_tLastLibInfo = {0};				//Save the last library information for easy modification and deletion
FaceInfo	g_tLastPicInfo = {0};				//Save the last face information for easy modification and deletion

#define VCA_SUSPEND_STATUS_PAUSE		0		//Pause intelligent analysis
#define VCA_SUSPEND_STATUS_RESUME		1		//Recovery intelligence analysis

#define VCA_SUSPEND_RESULT_SUCCESS		1		//Intelligent analysis paused successfully
#define VCA_SUSPEND_RESULT_CONFIGING	2		//Intelligent analysis pause failed, setting, can't set parameter
#define MAX_LOGON_WAIT_TIME				5 * 1000

#define TEM_DETECT_ENABLE               1		//Enable human body temperature measurement
#define TEM_DETECT_DISABLE              2       //Turn off human body temperature measurement enable
const char g_cTemUnit[][64] = {"", "Centigrade", "Fahrenheit"};
const char g_cTemAbnormalAlarm[][64] = {"", "High temperature alarm"};

#ifndef __WIN__

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h> 
#include <sys/time.h>

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

int ToIntDef(const char* _pstrFrom, int _iDef=0)
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

unsigned long netsdk_get_tick_count()
{
	unsigned long  currentTime = 0;
#ifndef WIN32
	struct timeval current;
	gettimeofday(&current, NULL);
	currentTime = current.tv_sec * 1000 + current.tv_usec/1000;	
#else
	currentTime = GetTickCount();
#endif
	return currentTime;
}

int IntToStr(int _iValue,char *_cDst)
{
	int   m = abs(_iValue);
	int   n = 0;
	int   k = 0;
	char  cTmp[16] = {0};
	char  str[16] = {0};

	if (m < 10)
	{
		str[0] = m + 48;
		str[1] = 0;
		strcpy(_cDst,str);
		return 0;
	}

	while(m/10 > 0)
	{
		n = m - m/10*10;
		cTmp[k] = n + 48;
		m /= 10;
		if(m < 10)
			cTmp[k+1] = m + 48;
		k++;
	}

	for(int i=k;i>=0;i--)
	{
		str[i] = cTmp[k-i];
	}

	strcpy(_cDst,str);
	return 0;
}

int LogonDevice()
{
	int iRet = -1;
	int iLogonType = SERVER_NORMAL;
	fprintf(stderr, "Please input LogonType: 0----Normal  1----Active\n");
	scanf("%d", &iLogonType);
	int iLogonID = -1;

	if (SERVER_ACTIVE == iLogonType)
	{	
		//Active mode login logic
		int iLocalListenPort = 6004;
		char cProductID[32] = {0};
		DsmOnline tOnline = {0};
		LogonActiveServer tActive = {0};
		fprintf(stderr, "Please input local listen port:");
		scanf("%d", &iLocalListenPort);
		iRet = NetClient_SetPort(iLocalListenPort, 0);
		if(0 != iRet )
		{
			printf("NetClient_SetPort fail!\n");
			return -1;
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
		if(0 != iRet )
		{
			printf("NetClient_SetDsmConfig:DSM_CMD_SET_NET_WAN_INFO fail!\n");
			return -1;
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
		iLogonID = NetClient_Logon_V4(SERVER_ACTIVE, &tActive, sizeof(LogonActiveServer));
	}
	else
	{
		LogonPara tInfo = {0};
		tInfo.iSize = sizeof(LogonPara);
		tInfo.iNvsPort = 3000;

		fprintf(stderr, "Please input server IP: ");
		scanf("%s", tInfo.cNvsIP);
		if (0 == strlen(tInfo.cNvsIP)) {
			strncpy_ss(tInfo.cNvsIP, (char*)("192.168.1.2"), sizeof(tInfo.cNvsIP));
		}

		fprintf(stderr, "Please input user name: ");
		scanf("%s", tInfo.cUserName);
		if (0 == strlen(tInfo.cUserName)) {
			strncpy_ss(tInfo.cUserName, (char*)("Admin"), sizeof(tInfo.cUserName));
		}

		fprintf(stderr, "Please input password: ");	
		scanf("%s", tInfo.cUserPwd);
		if (0 == strlen(tInfo.cUserPwd)) {
			strncpy_ss(tInfo.cUserPwd, (char*)("Admin"), sizeof(tInfo.cUserPwd));
		}

		fprintf(stderr, "Please input port: ");
		scanf("%d", &(tInfo.iNvsPort));
		if (tInfo.iNvsPort <= 0) {
			tInfo.iNvsPort = 3000;
		}

		iLogonID = NetClient_Logon_V4(SERVER_NORMAL, &tInfo, tInfo.iSize);
	}

	if(iLogonID < 0)
	{
		fprintf(stderr,"[LogonDevice] NetClient_Logon_V4 failed, %d.\n", iLogonID);
		getchar();
		return -1;
	}
	

	//The main thread is blocked here and the login status is obtained circularly
	int iTimes = 0;
	while (LOGON_SUCCESS != NetClient_GetLogonStatus(iLogonID))
	{
		if (iTimes++ > 100)	//5 seconds timeout
		{
			fprintf(stderr,"[LogonDevice] logon timeout.\n");
			return -1;
		}
		usleep(50000);		//Get login status once in 50ms
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
			} else if (LOGON_RETRY == iLogonStatus) {	//Log in again
				printf("[Notify_Main] logon, id=%d, retry.\n", _iLogonID);
			} else if (LOGON_ING == iLogonStatus) {		//Logging in
				printf("[Notify_Main] logon, id=%d, ing.\n", _iLogonID);
			} else {
				printf("[Notify_Main] logon, id=%d, %d.\n", _iLogonID, iLogonStatus);
			}		
		}
		break;
	case WCM_VCA_SUSPEND:	//Intelligent analysis pause message
		{
			int iResult = 0;
			VCASuspendResult tParam = {0};
			tParam.iBufSize = sizeof(VCASuspendResult);
			NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_VCA_SUSPEND, 0, &tParam, sizeof(tParam), &iResult);
			g_iVcaStatus = tParam.iResult;		//result
			if (VCA_SUSPEND_STATUS_PAUSE == tParam.iStatus) {
				if (VCA_SUSPEND_RESULT_SUCCESS == tParam.iResult) {
					printf("[Notify_Main] pause vca success.\n");
				} else {
					printf("[Notify_Main] pause vca failed.\n");
				}				
			}
		}	
		break;
	default:
		break;
	}
}

int FaceDetectionEnable()
{	
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = 0;	//Scene number0-15
	tParam.iDevType = 1;	//0-IPC, 1-NVR
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_ANYSCENE, 0, &tParam, sizeof(tParam), &iBytesReturned);
	
	if (iRet >= 0)
	{
		tParam.iArithmetic = 1<<2;//Face detection algorithm on
		tParam.iDevType = 1;	//0-IPC, 1-NVR
		iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_ANYSCENE, 0, &tParam, sizeof(tParam));	
		if (iRet >= 0)
		{
			printf("The face detection algorithm is enable successfully.\n");
		}
		else
		{
			printf("NetClient_SetDevConfig  NET_CLIENT_ANYSCENE failed.\n");
		}
	}
	else
	{
		printf("NetClient_GetDevConfig  NET_CLIENT_ANYSCENE failed.\n");
	}

	return iRet;
}

int GetFaceDetectionEnable()
{
	fprintf(stderr, "Please input channel num:\n");
	char cChanNo[16] = {0};
	scanf("%s", cChanNo);
	int iChanNo = atoi(cChanNo);

	fprintf(stderr, "Please input device type:\n");
	char cDevType[16] = {0};
	scanf("%s", cDevType);
	int iDevType = atoi(cDevType);

	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = 0;	//Scene number0-15
	tParam.iDevType = iDevType;	//0-IPC, 1-NVR
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_ANYSCENE, iChanNo, &tParam, sizeof(tParam), &iBytesReturned);

	if (iRet >= 0)
	{
		int iEnable = tParam.iArithmetic>>2 &&0x1;
		printf("FaceDetectionEnable(%d).\n", iEnable);
	}
	else
	{
		printf("NetClient_GetDevConfig  NET_CLIENT_ANYSCENE failed.\n");
	}

	return iRet;
}

int SetDetectParam()
{		
	FaceDetectArithmetic fda = {0};
	fda.iBufSize = sizeof(FaceDetectArithmetic);
	fda.iDevType = 1;	//0-IPC, 1-NVR
	int iByteReturn = 0;
	int iRet = NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_FACE_DETECT_ARITHMETIC, 0, &fda, sizeof(FaceDetectArithmetic), &iByteReturn);
	if (iRet < 0)
	{
		printf("NetClient_GetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
		return -1;
	}
	
	if (fda.iMinSize>=fda.iMaxSize)
	{
		fda.iMaxSize = fda.iMinSize+1;//Effectiveness of adjustment parameters (avoidance)
	}

	fda.iDevType = 1;
	fda.iPushMode = 2;
	fda.iSnapTimes = 1;
	fda.iDisplayTarget = 1;

	iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_FACE_DETECT_ARITHMETIC, 0, &fda, sizeof(FaceDetectArithmetic));
	if (iRet >= 0)
	{
		printf("NetClient_SetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC success.\n");
	}
	else
	{
		printf("NetClient_SetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
	}
	
	return iRet;
}

int SetPicStreamUploadParam()
{
	//Set background image quality and upload enable
	PicStreamUploadParam tInfo = {0};
	tInfo.iSize		= sizeof(PicStreamUploadParam);
	tInfo.iSceneId	= 0;
	tInfo.iPicType	= 0;//0-Background map

	int iRet = NetClient_VCAGetConfig(g_iLogonId, VCA_CMD_PICSTREAM_UPLOADPARAM, 0, &tInfo, sizeof(PicStreamUploadParam));

	if (iRet < 0)
	{
		printf("NetClient_GetDevConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
	}
	else
	{
		tInfo.iSnapEnable	= 1;
		tInfo.iQpvalue		= 80;
		tInfo.iIsOsd		= 1;

		iRet = NetClient_VCASetConfig(g_iLogonId, VCA_CMD_PICSTREAM_UPLOADPARAM, 0, &tInfo, sizeof(PicStreamUploadParam));
		if (iRet >= 0)
		{
			printf("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM success.\n");
		}
		else
		{
			printf("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
		}
	}

	//Set up close-up image quality and upload enable
	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSize		= sizeof(PicStreamUploadParam);
	tInfo.iSceneId	= 0;
	tInfo.iPicType	= 1;//1- feature
	
	iRet = NetClient_VCAGetConfig(g_iLogonId, VCA_CMD_PICSTREAM_UPLOADPARAM, 0, &tInfo, sizeof(PicStreamUploadParam));
	if (iRet < 0)
	{
		printf("NetClient_GetDevConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
	}
	else
	{
		tInfo.iSnapEnable	= 1;
		tInfo.iQpvalue		= 30;

		iRet = NetClient_VCASetConfig(g_iLogonId, VCA_CMD_PICSTREAM_UPLOADPARAM, 0, &tInfo, sizeof(PicStreamUploadParam));
		if (iRet < 0)
		{
			printf("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
		}
	}

	return iRet;
}

int SetVcaStatue(int _iStatus)
{
	int iChanNo = 0;	//Channel number, 0 means the first channel, IPC only has 1 channel
	VCASuspend tInfo = {0};
	tInfo.iStatus = _iStatus;
	tInfo.iDevType = 1;//0-IPC, 1-NVR
	return NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_VCA_SUSPEND, iChanNo, &tInfo, sizeof(tInfo));
}

int SavePicture(char* _pcFileName, char* _pcData, int _iLen)
{
	if (NULL == _pcFileName || NULL == _pcData || _iLen <= 0)
	{
		return -1;
	}
	FILE* pFile  = fopen(_pcFileName, "wb");
	if (NULL == pFile)
	{
		return -1;
	}
	fwrite(_pcData, _iLen, 1, pFile);
	fclose(pFile);
	pFile = NULL;
	return 0;
}

//Face Picture Stream CallBack
int __stdcall CallBack_PicStreamInfo(unsigned int _uiRecvID, long _lCommand, void* _lpInfo, int _iBufLen, void* _pvUser)
{
	if (NULL == _lpInfo || NET_PICSTREAM_CMD_FACE != _lCommand)
	{
		return -1;
	}
	FacePicStream* pFace = (FacePicStream*)_lpInfo;

	PicData tFullData;
	memset(&tFullData, 0, sizeof(tFullData));
	if (NULL != pFace->ptFullData)
	{		
		memcpy(&tFullData, pFace->ptFullData, min(pFace->iSizeOfFull, (int)sizeof(PicData)));
		char cFullPicName[256] = {0};
		sprintf(cFullPicName, ".//Pic//FullPic_Time(2%03d%02d%02d%02d%02d%02d%d).jpg"
			, tFullData.tPicTime.uiYear, tFullData.tPicTime.uiMonth,  tFullData.tPicTime.uiDay, tFullData.tPicTime.uiHour
			, tFullData.tPicTime.uiMinute, tFullData.tPicTime.uiSecondsr, tFullData.tPicTime.uiMilliseconds);
		SavePicture(cFullPicName, tFullData.pcPicData, tFullData.iDataLen);
	}

	printf("\n******************ActualFacePictureInfo******************\n");
	printf("face count:%d\n", pFace->iFaceCount);
	//Save Face Picture And Bitmap
	for (int i = 0; i < pFace->iFaceCount && i < MAX_FACE_PICTURE_COUNT; ++i)
	{
		FacePicData tFaceData = {0};
		memcpy(&tFaceData, pFace->ptFaceData[i], min(pFace->iSizeOfFace, (int)sizeof(FacePicData)));

		char cFacePicName[256] = {0};
		sprintf(cFacePicName, ".//Pic//FacePic_No%d_Time(2%03d%02d%02d%02d%02d%02d%d).jpg", i
			, tFullData.tPicTime.uiYear, tFullData.tPicTime.uiMonth,  tFullData.tPicTime.uiDay, tFullData.tPicTime.uiHour
			, tFullData.tPicTime.uiMinute, tFullData.tPicTime.uiSecondsr, tFullData.tPicTime.uiMilliseconds);
		//Face Picture
		SavePicture(cFacePicName, tFaceData.pcPicData, tFaceData.iDataLen);

		//Print Temperature Infomation
		for (int j = 0; j < tFaceData.iFaceAttrCount && j < LEN_256; ++j)
		{
			FaceAttribute tFaceAttr = {0};
			memcpy(&tFaceAttr, tFaceData.ptFaceAttr[j], min(tFaceData.iFaceAttrStructSize, (int)sizeof(FaceAttribute)));
			switch (tFaceAttr.iType)
			{
			case FACEATTR_TYPE_TEM_VALUE:
				{
					printf("TemValue:%.2f\n", (float)((tFaceAttr.iValue - 100000) / 100.0));
				}
				break;
			case FACEATTR_TYPE_TEM_UNIT:
				{
					if (tFaceAttr.iValue >= 0 && tFaceAttr.iValue < (int)(sizeof(g_cTemUnit) / 64))
					{
						printf("TemUnit:%s\n", g_cTemUnit[tFaceAttr.iValue]);
					}
				}
				break;
			case FACEATTR_TYPE_ABNORMAL_ALARM:
				{
					if (tFaceAttr.iValue >= 0 && tFaceAttr.iValue < (int)(sizeof(g_cTemAbnormalAlarm) / 64))
					{
						printf("TemAbnormalAlarm:%s\n", g_cTemAbnormalAlarm[tFaceAttr.iValue]);
					}
				}
				break;
			default:
				break;
			}
		}

		if (1 == tFaceData.iAlramType)
		{
			char cNegPicName[256] = {0};
			sprintf(cNegPicName, ".//Pic//NegPic_No%d_Time(2%03d%02d%02d%02d%02d%02d%d).jpg", i
				, tFullData.tPicTime.uiYear, tFullData.tPicTime.uiMonth,  tFullData.tPicTime.uiDay, tFullData.tPicTime.uiHour
				, tFullData.tPicTime.uiMinute, tFullData.tPicTime.uiSecondsr, tFullData.tPicTime.uiMilliseconds);

			SavePicture(cNegPicName, tFaceData.pcNegPicData, tFaceData.iNegPicLen);
		}	
	}
	return 0;
}

//Start Picture Stream
int StartSnap() 
{
	NetPicPara tNetPicParam = {0};
	tNetPicParam.iStructLen = sizeof(tNetPicParam);
	tNetPicParam.iChannelNo = 0;
	tNetPicParam.cbkPicStreamNotify = CallBack_PicStreamInfo;
	tNetPicParam.pvUser = NULL;

	unsigned int iConnectID = -1;
	int iRet = NetClient_StartRecvNetPicStream(g_iLogonId, &tNetPicParam, tNetPicParam.iStructLen, &iConnectID);
	if (iRet < 0) {
		g_iConnectId = -1;
		fprintf(stderr,"StartRecvNetPicStream Failed!");
	} else {
		g_iConnectId = (int)iConnectID;
		fprintf(stderr,"StartRecvNetPicStream Success! ConnectID(%d)\n\n", g_iConnectId);
	}
	return 0;
}

//Enable/Disenable Tempeture Check Function
int SetTemDetectEnable(int _iEnable)
{
	int iRet = NetClient_SetCommonEnable(g_iLogonId, CI_COMMON_ID_TEMDETECT, 0, _iEnable); //Channel 0 is the visible light channel, and the thermal imaging equipment sets this parameter according to channel 0
	if (iRet < 0)
	{
		printf("NetClient_SetCommonEnable  CI_COMMON_ID_TEMDETECT failed.\n");
	} else {
		printf("NetClient_SetCommonEnable  CI_COMMON_ID_TEMDETECT success.\n");
	}
	return 0;
}

//Set temperature scale type
int SetTemScaleType(int _iType)
{
	if (1 != _iType && 2 != _iType)
	{
		printf("SetTemScaleType  inValid param!\n");
		return -1;
	}
	TemperatureScaleType tInfo = {0};
	tInfo.iChanNo = 0;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iTempStandard = _iType;
	int iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_TEMPERATURE_STANDARD, 0, &tInfo, (int)sizeof(tInfo));
	if (iRet < 0)
	{
		printf("NetClient_SetDevConfig  NET_CLIENT_TEMPERATURE_STANDARD failed.\n");
	} else {
		printf("NetClient_SetDevConfig  NET_CLIENT_TEMPERATURE_STANDARD success.\n");
	}
	return 0;
}
//Set blackbody correction parameters
int SetBlackbodyCorrection(int _iChannelNo)
{
	BlackbodyCorrection tInfo = {0};
	int iReturn = -1;
	int iRet = NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_BLACKBODY_CORRECT, _iChannelNo, &tInfo, (int)sizeof(tInfo), &iReturn);
	if (0 <= iRet)
	{
		tInfo.iChanNo = 0;
		tInfo.iSize = sizeof(tInfo);
		tInfo.iBlackBodyCorrectEnable = 1;       //Blackbody correction enable, 0-off, 1-on, default on
		tInfo.iBlackBodyCorrectType = 2;         //Blackbody correction type, 1-single correction, 2-continuous correction, default continuous correction
		//temperature unit: 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin
		if(1 == tInfo.tParam[0].iBlackBodyTempUnit)
		{
			tInfo.tParam[0].iBlackBodyTemp = 3500;   //Temperature value * 100,  value 30-45  
		}
		else if(2 == tInfo.tParam[0].iBlackBodyTempUnit)
		{
			tInfo.tParam[0].iBlackBodyTemp = 9000;//Temperature value * 100 86 - 113
		}

		tInfo.tParam[0].iBlackBodyDistance = 100;//Unit: cm
		tInfo.tParam[0].tRect.left = 2000;       //Left margin - the X coordinate of the upper left corner, which is the coordinate of ten thousandth ratio, 0~10000
		tInfo.tParam[0].tRect.top = 2000;        //Top margin - Y coordinate of the upper left corner
		tInfo.tParam[0].tRect.right = 8000;      //Right margin - bottom right X coordinate
		tInfo.tParam[0].tRect.bottom = 8000;     //Bottom margin - bottom right y coordinate

		iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_BLACKBODY_CORRECT, _iChannelNo, &tInfo, (int)sizeof(tInfo));
		if (iRet < 0)
		{
			printf("NetClient_SetDevConfig  NET_CLIENT_BLACKBODY_CORRECT failed.\n");
		} else {
			printf("NetClient_SetDevConfig  NET_CLIENT_BLACKBODY_CORRECT success.\n");
		}
	}
	else
	{
		printf("NetClient_GetDevConfig  NET_CLIENT_BLACKBODY_CORRECT failed.\n");
	}
	return 0;
}

//Set temperature conversion parameters
int SetBodyTemCompensation(int _iChannelNo)
{
	BodyTempCorrect tInfo = {0};
	tInfo.iChanNo = _iChannelNo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iBodyTempCorrectEnable = 1;//Body temperature compensation enable, 0-not enabled, 1-enabled
	tInfo.iBodyTempCorrectSensitivity = 50;//Temperature compensation sensitivity, 0-100
	int iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_BODYTEMP_CORRECT, _iChannelNo, &tInfo, (int)sizeof(tInfo));
	if (iRet < 0)
	{
		printf("NetClient_SetDevConfig  NET_CLIENT_BODYTEMP_CORRECT failed.\n");
	} else {
		printf("NetClient_SetDevConfig  NET_CLIENT_BODYTEMP_CORRECT success.\n");
	}
	return 0;
}

//Set intelligent correction parameters
int SetIntelligentCorretct(int _iChannelNo)
{
	IntelligentCorretct tInfo = {0};
	tInfo.iChanNo = _iChannelNo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iIntelligentCorrectEnable = 1;//Intelligent correction enable, 0-not enabled, 1-enabled
	tInfo.iIntelligentCorrectSensitivity = 50;//Intelligent correction sensitivity,0-100
	int iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_INTELLIGENT_CORRECT, _iChannelNo, &tInfo, (int)sizeof(tInfo));
	if (iRet < 0)
	{
		printf("NetClient_SetDevConfig  NET_CLIENT_INTELLIGENT_CORRECT failed.\n");
	} else {
		printf("NetClient_SetDevConfig  NET_CLIENT_INTELLIGENT_CORRECT success.\n");
	}
	return 0; 
}
//Set temperature abnormal alarm parameters
int SetVcaTemDetect()
{
	VCATemDetect vc = {0};
	vc.iSize = sizeof(vc);
	vc.iRuleID = 14;              //For temperature detection, the rule number parameter of the interface is recommended to be bound 14
	vc.iSceneID = 0;              //Scene number, 0~32
	vc.iModelType = 1;
	int iRet = NetClient_VCAGetConfig(g_iLogonId, VCA_CMD_TEMDETECT, 0, &vc, sizeof(VCATemDetect));
	if (0 <= iRet)
	{
		vc.iValid = 1;                //Whether this event detection is valid, 0-invalid, 1-valid 
		vc.iTemThreshold = 3600;      //Temperature threshold, the value is the actual temperature * 100
		vc.iTempLoseEnable = 1;       //Temperature abnormal alarm, 0-not enabled, 1-enabled
		iRet = NetClient_VCASetConfig(g_iLogonId, VCA_CMD_TEMDETECT, 0, &vc, sizeof(VCATemDetect));
		if (iRet < 0)
		{
			printf("NetClient_VCASetConfig  VCA_CMD_TEMDETECT failed.\n");
		} else {
			printf("NetClient_VCASetConfig  VCA_CMD_TEMDETECT success.\n");
		}
	}
	else
	{
		printf("NetClient_VCAGetConfig  VCA_CMD_TEMDETECT failed.\n");
	}
	return 0; 
}

int main(int argc,char *argv[])
{
	//Load Library
	int iRet = LoadNVSSDK();
	if (0 != iRet)
	{
		fprintf(stderr,"[main] LoadLib failed.\n");
		return -1;
	}

	NetClient_SetSDKWorkMode(2);//SDK working mode platform lightweight
	
	//Register main callback function
	NetClient_SetNotifyFunction_V4(Notify_Main, NULL, NULL, NULL, NULL);

	//Launch SDK
	NetClient_Startup_V4(0, 0, 0);

	//Login device
	if (LogonDevice() < 0)
	{
		fprintf(stderr,"[main] LogonDevice failed.\n");
		getchar();
		return -1;
	}

	//Open picture stream
#ifdef __WIN__
	_mkdir(".\\Pic");
#else
	mkdir(".//Pic", S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH); 
#endif
	StartSnap();

	while (1)
	{
		fprintf(stderr, "Select: 0-Quit  1-TurnOnFaceFetect  2-SetFaceDetectInfo  3-EnableBodyTemDetect  4-TemperatureScaleSelection  5-BlackbodyCorrection\n6-BodyTemperatureConversion  7-IntelligentCorrection  8-SetTemperatureAbnormalAlarmPara\n");
		int iOpt = 0;
		scanf("%d", &iOpt);
		fflush(stdin);
		if (0 == iOpt)
		{
			break;
		}
		else if (1 == iOpt)	//Turn on face detection
		{
			FaceDetectionEnable();
		}
		else if (2 == iOpt)//Set face detection parameters
		{
			SetVcaStatue(VCA_SUSPEND_STATUS_PAUSE);	//Pause intelligent analysis
			getchar();
			if (VCA_SUSPEND_RESULT_SUCCESS != g_iVcaStatus) {
				fprintf(stderr,"[main] Intelligent analysis pause failed!\n");
				getchar();
				continue;
			}
			SetDetectParam();//Parameters related to face detection (need to pause intelligent analysis)
			SetVcaStatue(VCA_SUSPEND_STATUS_RESUME);//Intelligent analysis needs to be restored after setting

			SetPicStreamUploadParam();//Capture image quality, upload related parameters
		}
		else if (3 == iOpt)//Enable human body temperature measurement
		{
			SetTemDetectEnable(TEM_DETECT_ENABLE);
		}
		else if (4 == iOpt)//Set temperature scale type
		{
			fprintf(stderr,"1-centigrade  2-Fahrenheit degree\n");
			int iType = 1;
			scanf("%d", &iType);
			SetTemScaleType(iType);
		}
		else if (5 == iOpt)//Blackbody correction
		{
			fprintf(stderr,"Please Input Thermal imaging ChannelNo,(default:1)\n");
			int iChannelNo = 1;
			char szInput[16] = {0};

			fgets( szInput ,sizeof(szInput),stdin);
			if(strlen(szInput) > 0)
			{
				iChannelNo = atoi(szInput);
			}

			SetBlackbodyCorrection(iChannelNo);
		}
		else if (6 == iOpt)//Body temperature conversion
		{
			fprintf(stderr,"Please Input Thermal imaging ChannelNo,(default:1)\n");
			int iChannelNo = 1;
			char szInput[16] = {0};
			fgets( szInput ,sizeof(szInput),stdin);
			if(strlen(szInput) > 0)
			{
				iChannelNo = atoi(szInput);
			}
			SetBodyTemCompensation(iChannelNo);
		}
		else if (7 == iOpt)//Intelligent correction
		{
			fprintf(stderr,"Please Input Thermal imaging ChannelNo,(default:1)\n");
			int iChannelNo = 1;
			char szInput[16] = {0};
			fgets( szInput ,sizeof(szInput),stdin);
			if(strlen(szInput) > 0)
			{
				iChannelNo = atoi(szInput);
			}
			SetIntelligentCorretct(iChannelNo);
		}
		else if (8 == iOpt)//Set temperature abnormal alarm parameters
		{
			SetVcaTemDetect();
		}
	}

	//Log off users and release SDK resources
	NetClient_Logoff(g_iLogonId);	
	NetClient_Cleanup();

	return 0;
}

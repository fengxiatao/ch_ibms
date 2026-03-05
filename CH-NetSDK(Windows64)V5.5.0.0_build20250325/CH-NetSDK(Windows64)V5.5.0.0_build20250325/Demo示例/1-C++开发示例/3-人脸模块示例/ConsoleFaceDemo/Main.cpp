
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "NetSdk.h"

int g_iLogonId = -1;
int g_iConnectId = -1;					
int g_iVcaStatus = 0;

FaceLibInfo g_tLastLibInfo = {0};				//Save the last library information, easy to modify and delete
FaceInfo	g_tLastPicInfo = {0};				//Save the last face information, easy to modify and delete

#define VCA_SUSPEND_STATUS_PAUSE		0		//Pause intelligent analysis
#define VCA_SUSPEND_STATUS_RESUME		1		//Recovery intelligence analysis

#define VCA_SUSPEND_RESULT_SUCCESS		1		//Intelligent analysis suspended successfully
#define VCA_SUSPEND_RESULT_CONFIGING	2		//Failed to pause intelligent analysis. Setting is in progress. Parameters cannot be set
#define MAX_LOGON_WAIT_TIME				5 * 1000


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
	//return atoi(_pstrFrom);--This method may overflow the signed number
	return (int)strtoul(_pstrFrom, 0, iFlag);
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
	

	//The main thread is blocked here to get the login status in a loop
	int iTimes = 0;
	while (LOGON_SUCCESS != NetClient_GetLogonStatus(iLogonID))
	{
		if (iTimes++ > 100)	//5seconds until timeout
		{
			fprintf(stderr,"[LogonDevice] logon timeout.\n");
			return -1;
		}
		usleep(50000);		//Get login status once in 50 milliseconds
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

//Face database query
int FaceLibraryQuery() 
{
	int iRet = -1;
	FaceLibQuery tQuery = {0};
	tQuery.iSize = sizeof(tQuery);
	tQuery.iChanNo = 0;		//Channel number, 0 means the first channel, IPC has only 1 channel
	tQuery.iPageCount = FACE_MAX_PAGE_COUNT;	//The number of queries per page is 20
	//Clear previously queried data
	usleep(20*1000);		//Prevent immediate query after operation


	printf("Face library information---------------\n");

	int iPageNo = 0;		//Query page number, 0 means the first page
	while (true) 
	{
		//Query database information
		tQuery.iPageNo = iPageNo;

		FaceLibQueryResult tResult[FACE_MAX_PAGE_COUNT];
		memset(&tResult, 0, sizeof(tResult));
		iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_LIB_QUERY, tQuery.iChanNo, &tQuery, tQuery.iSize, &tResult, sizeof(FaceLibQueryResult));
		if (0 != iRet) {
			printf("[FaceLibraryQuery] failed, ret=%d.\n", iRet);
			return iRet;
		}

		for(int i = 0; i < tResult[0].iPageCount && i < FACE_MAX_PAGE_COUNT; ++i) {
			g_tLastLibInfo = tResult[i].tFaceLib;	

			int iIndex =i + 1 + iPageNo*FACE_MAX_PAGE_COUNT;
			char* strType = "Upload";
			if(1 == tResult[i].tFaceLib.iAlarmType){
				strType = "Not upload";
			}
			printf("Serial number: :%d, Library key: %d, Similarity: %d, Library name: %s, Identification information: %s, Description: %s\n",  iIndex, tResult[i].tFaceLib.iLibKey, 
				tResult[i].tFaceLib.iThreshold, tResult[i].tFaceLib.cName, strType, tResult[i].tFaceLib.cExtrInfo);
		}

		//Calculate total pages
		int iTotalPage = tResult[0].iTotal / FACE_MAX_PAGE_COUNT;
		if (tResult[0].iTotal % FACE_MAX_PAGE_COUNT > 0) {
			iTotalPage = iTotalPage + 1;
		}
		iPageNo++;
		if (iPageNo >= iTotalPage || iPageNo > 1) {
			break;
		}
	}

	printf("\n");
	return iRet;
}

//Add face database
int FaceLibraryAdd() 
{
	FaceLibEdit tInfo = {0};
	//Required field
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = 0;		//Channel number, 0 means the first channel, IPC has only 1 channel
	tInfo.tFaceLib.iThreshold = 70;	//Identification threshold, range 0 ~ 100
	tInfo.tFaceLib.iLibKey = 0;		//0 Indicates an addition
	tInfo.tFaceLib.iAlarmType = 0;	//0No upload, 1 upload	
	//end

	//Non required fields
	strncpy_ss(tInfo.tFaceLib.cName, "libname", sizeof(tInfo.tFaceLib.cName));
	strncpy_ss(tInfo.tFaceLib.cExtrInfo, "facelib~~~~~~add~~~~~", sizeof(tInfo.tFaceLib.cExtrInfo));
	//end

	//This field is not required for normal devices
	tInfo.tFaceLib.iOptType = 1;	//1 add, 2 modify
	//end

	//add
	FaceReply tReply = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_LIB_EDIT, tInfo.iChanNo, &tInfo, tInfo.iSize, &tReply, sizeof(FaceReply));
	if(0 != iRet){
		printf("Face library addition failed:%d\n", iRet);
	} else {
		printf("Face library addition results:%d\n", tReply.iResult);
	}

	//Display the result of adding face database
	FaceLibraryQuery();
	return iRet;
}

//Face database modification
int FaceLibraryModify() 
{
	if (g_tLastLibInfo.iLibKey <= 0)
	{
		printf("Face library modification failed: please add or query face library first.\n");
		return -1;
	}
	FaceLibEdit tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = 0;		//Channel number, 0 means the first channel, IPC has only 1 channel
	tInfo.tFaceLib.iThreshold = 80;	//Identification threshold, range 0 ~ 100
	tInfo.tFaceLib.iLibKey = g_tLastLibInfo.iLibKey;//If it is greater than 0, the last library will be modified by default
	tInfo.tFaceLib.iAlarmType = 1;	//0 does not upload, 1 uploads
	strncpy_ss(tInfo.tFaceLib.cName, "libname2", sizeof(tInfo.tFaceLib.cName));
	strncpy_ss(tInfo.tFaceLib.cExtrInfo, "facelib~~~~~~modify~~~~~", sizeof(tInfo.tFaceLib.cExtrInfo));
	//This field is not required for normal devices
	tInfo.tFaceLib.iOptType = 2;	//1 add, 2 modify
	//end
	//modify
	FaceReply tReply = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_LIB_EDIT, tInfo.iChanNo, &tInfo, tInfo.iSize, &tReply, sizeof(FaceReply));
	if(0 != iRet){
		printf("Face library modification failed:%d\n", iRet);
	} else {
		printf("Face library modification results:%d\n", tReply.iResult);
	}

	//Display the result of face database modification
	FaceLibraryQuery();
	return iRet;
}

//Face database deletion
int FaceLibraryDelete() 
{
	if (g_tLastLibInfo.iLibKey <= 0)
	{
		printf("Face library deletion failed: Please add or query face library first.\n");
		return -1;
	}

	FaceLibDelete tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = 0;		//Channel number, 0 means the first channel, IPC has only 1 channel
	tInfo.iLibKey = g_tLastLibInfo.iLibKey;//The last library is deleted by default;

	FaceReply tReply = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_LIB_DELETE, tInfo.iChanNo, &tInfo, tInfo.iSize, &tReply, sizeof(FaceReply));//Synchronous interface, face base map in the library more, the interface needs to wait a long time to return
	if(0 != iRet){
		printf("Face library deletion failed:%d\n", iRet);
	} else {
		printf("Face library deletion results:%d\n", tReply.iResult);
	}

	FaceLibraryQuery();
	return iRet;
}

//Face base map query
int FacePictureQuery(int _iPageNo) 
{
	//The last face database is selected
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("Face library modification failed: please add or query face library first.\n\n");
		return -1;
	}

	usleep(20*1000);		//Prevent immediate query after operation

	//query criteria
	FaceQuery tInfo = {0};
	//Required field
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = 0;		
	tInfo.iLibKey = iLibKey;
	tInfo.iPageNo = _iPageNo;
	tInfo.iPageCount = FACE_MAX_PAGE_COUNT;
	strncpy_ss(tInfo.cBirthStart, "1970-01-01", sizeof(tInfo.cBirthStart));	//Date of birth
	strncpy_ss(tInfo.cBirthEnd, "2018-10-16", sizeof(tInfo.cBirthEnd));		//End date of birth
	//end	
	tInfo.iSex = 0;			//Gender, 0 unknown, 1 male, 2 female
	tInfo.iNation = 0;		//Nationality, 0 unknown
	tInfo.iPlace = 0;		//Native place, 0 unknown
	tInfo.iCertType = 0;	//Certificate type, 0 unknown, 1 second generation ID card, 2 officer card
	tInfo.iModeling = 0;	//Modeling state, 0 unknown, 1 modeling success, 2 modeling failure, 3 not modeling
	//end
	//query 
	FaceQueryResult tResult[FACE_MAX_PAGE_COUNT];
	memset(&tResult, 0, sizeof(tResult));
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_QUERY, tInfo.iChanNo, &tInfo, tInfo.iSize, &tResult, sizeof(FaceQueryResult));
	if(0 != iRet){
		printf("Face map query failed:%d\n", iRet);
	} else {
		printf("Face map information(first page)---------------\n");
		for(int i= 0; i < tResult[0].iPageCount && i < FACE_MAX_PAGE_COUNT; ++i) {
			g_tLastPicInfo = tResult[i].tFace;	//Save the face information. Only the last one is saved here for easy modification and deletion

			int iIndex = i + 1;
			printf("Serial:%d, Library key:%d, Face key:%d, Name:%s, Birth:%s, Modeling state:%d\n" , iIndex, tResult[i].tFace.iLibKey, 
				tResult[i].tFace.iFaceKey, tResult[i].tFace.cName, tResult[i].tFace.cBirthTime, tResult[i].tFace.iModeling);
		}
		printf("\n");
	}	
	return iRet;
}

//Add face base map
int FacePictureAdd() 
{
	//The last face database is selected
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("Facial basemap addition failed: please query or add face library first.\n\n");
		return -1;
	}

	FaceEdit tInfo = {0}; 
	//Required field
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = 0;				//Channel number, 0 means the first channel, IPC has only 1 channel

	tInfo.tFace.iLibKey = iLibKey;	//Face database key value
	tInfo.tFace.iModeling = 1;		//Modeling or not, 1 modeling, 0 not modeling
	tInfo.tFace.iFaceKey = 0;		//Face base map key value, 0 means add
	strncpy_ss(tInfo.tFace.cName, "ZhangSan", sizeof(tInfo.tFace.cName));	//Name of face base map
	strncpy_ss(tInfo.tFace.cBirthTime, "2000-01-01", sizeof(tInfo.tFace.cBirthTime));//date of birth
	strncpy_ss(tInfo.cFacePic,".//face.jpg", sizeof(tInfo.cFacePic));		//Image path of base map
	//end

	//Non required fields
	tInfo.tFace.iSex = 0;			//Gender, 0 unknown, 1 male, 2 female
	tInfo.tFace.iNation = 0;		//Nationality, 0 unknown
	tInfo.tFace.iPlace = 0;			//Native place, 0 unknown;
	tInfo.tFace.iCertType = 1;		//Certificate type, 0 unknown, 1 second generation ID card, 2 officer card;
	strncpy_ss(tInfo.tFace.cCertNum, "232321199909090909", sizeof(tInfo.tFace.cCertNum));//Identification Number
	//end

	tInfo.tFace.iOptType = 1;		//1add
	//end

	FaceReply tReply = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_EDIT, tInfo.iChanNo, &tInfo, tInfo.iSize, &tReply, sizeof(FaceReply));
	if(0 != iRet){
		printf("Face basemap addition failed:%d\n", iRet);
	} else {
		printf("Face basemap add results:%d\n", tReply.iResult);
	}

	FacePictureQuery(0);
	return 0;
}

//Face base map modification
int FacePictureModify() 
{
	//The last base image is modified by default here
	if (g_tLastPicInfo.iFaceKey <= 0) {
		printf("Face map modification failed: please first query or add a face base map.\n\n");
		return -1;
	}

	FaceEdit tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = 0;				
	tInfo.tFace.iLibKey = g_tLastPicInfo.iLibKey;	//Face database key value
	tInfo.tFace.iFaceKey = g_tLastPicInfo.iFaceKey;//Key value of face base map
	strncpy_ss(tInfo.tFace.cName, "Lisi", sizeof(tInfo.tFace.cName));	//Name of face base map
	strncpy_ss(tInfo.tFace.cBirthTime, "2014-04-04", sizeof(tInfo.tFace.cBirthTime));//date of birth
	//end

	tInfo.tFace.iSex = 0;			
	tInfo.tFace.iNation = 0;		
	tInfo.tFace.iPlace = 0;			
	tInfo.tFace.iCertType = 1;		
	strncpy_ss(tInfo.tFace.cCertNum, "232321201404040404", sizeof(tInfo.tFace.cCertNum));//Identification Number
	//end

	tInfo.tFace.iOptType = 2;		//2modify
	//end

	FaceReply tReply = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_EDIT, tInfo.iChanNo, &tInfo, tInfo.iSize, &tReply, sizeof(FaceReply));
	if(0 != iRet){
		printf("Face basemap modification failed:%d\n", iRet);
	} else {
		printf("Face basemap modification result:%d\n", tReply.iResult);
	}

	FacePictureQuery(0);
	return 0;
}

//Face base map deletion
int FacePictureDelete() 
{
	//The last base map is deleted by default
	if(g_tLastPicInfo.iFaceKey <= 0) {
		printf("Facial basemap deletion failed: please first query or add a face basemap.\n\n");
		return -1;
	}

	FaceDelete tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = 0;		
	tInfo.iLibKey = g_tLastPicInfo.iLibKey;
	tInfo.iFaceKey =  g_tLastPicInfo.iFaceKey;	

	FaceReply tReply = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_DELETE, tInfo.iChanNo, &tInfo, tInfo.iSize, &tReply, sizeof(FaceReply));
	if(0 != iRet){
		printf("Face basemap deletion failed:%d\n", iRet);
	} else {
		printf("Face basemap deletion results:%d\n", tReply.iResult);
	}
	FacePictureQuery(0);
	return iRet;
}

int FaceDetectionEnable()
{
	fprintf(stderr, "Please input channel num:\n");
	char cChanNo[16] = {0};
	scanf("%s", cChanNo);
	int iChanNo = atoi(cChanNo);

	fprintf(stderr, "Please input device type:\n");
	char cDevType[16] = {0};
	scanf("%s", cDevType);
	int iDevType = atoi(cDevType);

	fprintf(stderr, "Please input enable:\n");
	char cEnable[16] = {0};
	scanf("%s", cEnable);
	int iEnable = atoi(cEnable);
	
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = 0;	//Scene number 0-15
	tParam.iDevType = iDevType;	//0-IPC, 1-NVR
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_ANYSCENE, iChanNo, &tParam, sizeof(tParam), &iBytesReturned);
	
	if (iRet >= 0)
	{
		tParam.iArithmetic = iEnable<<2;//Face detection algorithm on
		tParam.iDevType = iDevType;	//0-IPC, 1-NVR
		iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_ANYSCENE, iChanNo, &tParam, sizeof(tParam));	
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
	tParam.iSceneID = 0;	//Scene number 0-15
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

int FaceRecognitionEnable()
{
	fprintf(stderr, "Please input channel num:\n");
	char cChanNo[16] = {0};
	scanf("%s", cChanNo);
	int iChanNo = atoi(cChanNo);

	fprintf(stderr, "Please input device type:\n");
	char cDevType[16] = {0};
	scanf("%s", cDevType);
	int iDevType = atoi(cDevType);

	fprintf(stderr, "Please input enable:(1-disable, 2-enable)\n");
	char cEnable[16] = {0};
	scanf("%s", cEnable);
	int iEnable = atoi(cEnable);
	
	FaceDetectArithmetic fda = {0};
	fda.iBufSize = sizeof(FaceDetectArithmetic);
	fda.iDevType = iDevType;	//0-IPC, 1-NVR
	int iByteReturn = 0;
	int iRet = NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_FACE_DETECT_ARITHMETIC, iChanNo, &fda, sizeof(FaceDetectArithmetic), &iByteReturn);
	if (iRet < 0)
	{
		printf("NetClient_GetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
		return -1;
	}

	if (fda.iMinSize>=fda.iMaxSize)
	{
		fda.iMaxSize = fda.iMinSize+1;//Effectiveness of adjustment parameters (avoidance)
	}

	fda.iDentification = iEnable;	//Face recognition switch 0 - not supported, 1 - off, 2 - on
	fda.iDevType = iDevType;	//0-IPC, 1-NVR	

	iRet = NetClient_SetDevConfig(g_iLogonId, NET_CLIENT_FACE_DETECT_ARITHMETIC, iChanNo, &fda, sizeof(FaceDetectArithmetic));
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

int GetFaceRecognitionEnable()
{
	fprintf(stderr, "Please input channel num:\n");
	char cChanNo[16] = {0};
	scanf("%s", cChanNo);
	int iChanNo = atoi(cChanNo);

	fprintf(stderr, "Please input device type:\n");
	char cDevType[16] = {0};
	scanf("%s", cDevType);
	int iDevType = atoi(cDevType);
	
	
	FaceDetectArithmetic fda = {0};
	fda.iBufSize = sizeof(FaceDetectArithmetic);
	fda.iDevType = iDevType;	//0-IPC, 1-NVR
	int iByteReturn = 0;
	int iRet = NetClient_GetDevConfig(g_iLogonId, NET_CLIENT_FACE_DETECT_ARITHMETIC, iChanNo, &fda, sizeof(FaceDetectArithmetic), &iByteReturn);
	if (iRet < 0)
	{
		printf("NetClient_GetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
		return -1;
	}
	else
	{
		printf("FaceDentificationEnable(%d).\n", fda.iDentification);
	}
	
	return 0;
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
	//Set the background image quality and upload enable
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

	//Set the close-up image quality and upload enable
	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSize		= sizeof(PicStreamUploadParam);
	tInfo.iSceneId	= 0;
	tInfo.iPicType	= 1;//1-Close up
	
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

int SetFaceScheduleEnable()
{
	//The last face database is selected
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("Face schedule enable set failed: please query or add face library first.\n\n");
		return -1;
	}

	fprintf(stderr, "Please input MainAlarmType(20-FaceRecognition 21-NVRVCA other-NVRVCA):\n");
	char cMainAlarmType[16] = {0};
	scanf("%s", cMainAlarmType);
	int iMainAlarmType = atoi(cMainAlarmType);
	char cSubAlarmType[16] = {0};
	int iSubAlarmType = 0;
	if (ALARM_TYPE_FACE_IDENT == iMainAlarmType)
	{
		fprintf(stderr, "Please input SubAlarmTypWhite liste(0-Blacklist 1-Whitelist other-Blacklist):\n");
		scanf("%s", cSubAlarmType);
		iSubAlarmType = atoi(cSubAlarmType);
		if (1 == iSubAlarmType)//The whitelist has nothing to do with the library, so the value of ilibkey is 0
		{
			iLibKey = 0;
		}
	}
	else
	{
		iMainAlarmType = ALARM_TYPE_NVR_VCA;
		fprintf(stderr, "Please input SubAlarmTypWhite liste(0:Face detection. 1:Face recognition - comparison. 2:Face recognition - stranger. 3:Face recognition - frequency. 4-Face recognition - retention. other-Face recognition - comparison.):\n");
		scanf("%s", cSubAlarmType);
		iSubAlarmType = atoi(cSubAlarmType);
	}

	usleep(20*1000);		//Prevent immediate query after operation

	//Gain defense enable
	int iChanNo = 0;		//Channel number, 0 means the first channel
	TAlarmScheEnableParam tSchEnabel = {0};	
	tSchEnabel.iBuffSize = sizeof(tSchEnabel);
	tSchEnabel.iSceneID = 0;
	tSchEnabel.iParam1 = iLibKey;//Face database ID
	tSchEnabel.iParam2 = iSubAlarmType;	//Alarm type_ iAlarmType= ALARM_ TYPE_ FACE_ When ident 20, iparam2: 0-blacklist, 1-white list; alarm type_ iAlarmType=ALARM_ TYPE_ NVR_ Iparam2: 0-face detection 1-face recognition - comparison 2 - face recognition - Stranger 3 - face recognition - frequency 4 - face recognition - retention
	int iRet = NetClient_GetAlarmConfig(g_iLogonId, iChanNo, iMainAlarmType, CMD_GET_ALARMSCH_ENABLE, &tSchEnabel);
	if (iRet >= 0)
	{
		//Setting up defense enabling
		tSchEnabel.iEnable = 1;
		iRet = NetClient_SetAlarmConfig(g_iLogonId, iChanNo, iMainAlarmType, CMD_ALARMSCH_ENABLE_EX, &tSchEnabel);
		if (iRet >= 0)
		{
			printf("NetClient_SetAlarmConfig  CMD_ALARMSCH_ENABLE_EX success.\n");
		}
		else
		{
			printf("NetClient_SetAlarmConfig  CMD_ALARMSCH_ENABLE_EX failed.\n");
		}
	}
	else
	{
		printf("NetClient_GetAlarmConfig  CMD_GET_ALARMSCH_ENABLE failed.\n");
	}

	return iRet;
}

int GetFaceScheduleEnable()
{
	//The last face database is selected
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("Face schedule enable get failed: please query or add face library first.\n\n");
		return -1;
	}

	fprintf(stderr, "Please input MainAlarmType(20-FaceRecognition 21-NVRVCA other-NVRVCA):\n");
	char cMainAlarmType[16] = {0};
	scanf("%s", cMainAlarmType);
	int iMainAlarmType = atoi(cMainAlarmType);
	char cSubAlarmType[16] = {0};
	int iSubAlarmType = 0;
	if (ALARM_TYPE_FACE_IDENT == iMainAlarmType)
	{
		fprintf(stderr, "Please input SubAlarmTypWhite liste(0-Blacklist 1-Whitelist other-Blacklist):\n");
		scanf("%s", cSubAlarmType);
		iSubAlarmType = atoi(cSubAlarmType);
		if (1 == iSubAlarmType)
		{
			iLibKey = 0;
		}
	}
	else
	{
		iMainAlarmType = ALARM_TYPE_NVR_VCA;
		fprintf(stderr, "Please input SubAlarmTypWhite liste(0:Face detection. 1:Face recognition - comparison. 2:Face recognition - stranger. 3:Face recognition - frequency. 4-Face recognition - retention. other-Face recognition - comparison.):\n");
		scanf("%s", cSubAlarmType);
		iSubAlarmType = atoi(cSubAlarmType);
	}

	usleep(20*1000);		

	//Gain defense enable
	int iChanNo = 0;		
	TAlarmScheEnableParam tSchEnabel = {0};	
	tSchEnabel.iBuffSize = sizeof(tSchEnabel);
	tSchEnabel.iSceneID = 0;
	tSchEnabel.iParam1 = iLibKey;
	tSchEnabel.iParam2 = iSubAlarmType;	//Alarm type_ iAlarmType= ALARM_ TYPE_ FACE_ When ident 20, iparam2: 0-blacklist, 1-white list; alarm type_ iAlarmType=ALARM_ TYPE_ NVR_ Iparam2: 0-face detection 1-face recognition - comparison 2 - face recognition - Stranger 3 - face recognition - frequency 4 - face recognition - retention
	int iRet = NetClient_GetAlarmConfig(g_iLogonId, iChanNo, iMainAlarmType, CMD_GET_ALARMSCH_ENABLE, &tSchEnabel);
	if (iRet >= 0)
	{
		printf("NetClient_GetAlarmConfig  CMD_GET_ALARMSCH_ENABLE success. Enable(%d).\n", tSchEnabel.iEnable);
	}
	else
	{
		printf("NetClient_GetAlarmConfig  CMD_GET_ALARMSCH_ENABLE failed.\n");
	}

	return iRet;
}

int SetFaceSchedule()
{
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("Face schedule set failed: please query or add face library first.\n\n");
		return -1;
	}

	fprintf(stderr, "Please input weekday num(0-6 Sunday-Saturday):\n");
	char cWeekday[16] = {0};
	scanf("%s", cWeekday);
	int iWeekday = atoi(cWeekday);
	if (iWeekday < 0 || iWeekday > 6)
	{
		iWeekday = 0;
	}

	fprintf(stderr, "Please input enable (0:disenable 1:enable other:enable):\n");
	char cEnable[16] = {0};
	scanf("%s", cEnable);
	int iEnable = atoi(cEnable);
	if (0 != iEnable)
	{
		iEnable = 1;
	}

	usleep(20*1000);		
	
	int iChannelNo = 0;

	//Defense template
	TAlarmScheduleParam tSchParam = {0};
	tSchParam.iBuffSize = sizeof(tSchParam);
	tSchParam.iWeekday = iWeekday;
	tSchParam.iSceneID = 0;	
	tSchParam.iParam1 = iLibKey;//The whitelist of face database ID has nothing to do with the database, so when_ When ilalarmtype = 20 iparam2 = 1, the value of ilibkey is 0
	tSchParam.iParam2 = 0;	//Alarm type_ iAlarmType= ALARM_ TYPE_ FACE_ When ident 20, iparam2: 0-blacklist, 1-white list; alarm type_ iAlarmType=ALARM_ TYPE_ NVR_ Iparam2: 0-face detection 1-face recognition - comparison 2 - face recognition - Stranger 3 - face recognition - frequency 4 - face recognition - retention
	for(int i = 0; i < MAX_TIMESEGMENT; i++)
	{
		if (0 == i)//Only the first time period is set here by default
		{
			NVS_SCHEDTIME &tSeg = tSchParam.timeSeg[iWeekday][i];
			tSeg.iRecordMode = iEnable;//Time period enabled
			if (tSeg.iRecordMode)
			{	
				tSeg.iStartHour = 0;
				tSeg.iStartMin = 0;
				tSeg.iStopHour = 23;
				tSeg.iStopMin = 59;
			}
		}
	}
	int iRet = NetClient_SetAlarmConfig(g_iLogonId, iChannelNo, ALARM_TYPE_FACE_IDENT, CMD_SET_ALARMSCHEDULE,  &tSchParam);
	if (iRet >= 0)
	{
		printf("NetClient_SetAlarmConfig CMD_SET_ALARMSCHEDULE success.\n\n");
	}
	else
	{
		printf("NetClient_SetAlarmConfig CMD_SET_ALARMSCHEDULE failed.\n\n");
	}

	return iRet;
}

int GetFaceSchedule()
{
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("Face schedule get failed: please query or add face library first.\n\n");
		return -1;
	}

	fprintf(stderr, "Please input weekday num(0-6 Sunday-Saturday):\n");
	char cWeekday[16] = {0};
	scanf("%s", cWeekday);
	int iWeekday = atoi(cWeekday);
	if (iWeekday < 0 || iWeekday > 6)
	{
		iWeekday = 0;
	}

	usleep(20*1000);		
	
	int iChannelNo = 0;

	TAlarmScheduleParam tSchParam = {0};
	tSchParam.iBuffSize = sizeof(tSchParam);
	tSchParam.iWeekday = iWeekday;
	tSchParam.iSceneID = 0;	
	tSchParam.iParam1 = iLibKey;
	tSchParam.iParam2 = 0;	
	int iRet = NetClient_GetAlarmConfig(g_iLogonId, iChannelNo, ALARM_TYPE_FACE_IDENT, CMD_GET_ALARMSCHEDULE,  &tSchParam);
	if (iRet >= 0)
	{
		for(int i = 0; i < MAX_TIMESEGMENT; i++)
		{
			NVS_SCHEDTIME &tSeg = tSchParam.timeSeg[iWeekday][i];
			
			if (tSeg.iRecordMode)
			{
				printf("Time%d enable(%d) (%02d:%02d:00 %02d:%02d:00 ).\n\n", i+1, tSeg.iRecordMode, tSeg.iStartHour, tSeg.iStartMin, tSeg.iStopHour, tSeg.iStopMin);
			}			
		}
	}

	return iRet;
}

int SetFaceAlarm()
{
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("Face alarm set failed: Please query or add face library first.\n\n");
		return -1;
	}

	usleep(20*1000);		

	int iChannelNo = 0;		

	FaceAlarmParam tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = iChannelNo;
	tInfo.iAlarmType = ALARM_TYPE_NVR_VCA;	//Alarm type, 20 face detection, 21 face recognition
	tInfo.iParam1 = 1;	//Alarm type_ iAlarmType= ALARM_ TYPE_ FACE_ When ident 20, iparam1: 0-blacklist, 1-whitelist; alarm type_ iAlarmType=ALARM_ TYPE_ NVR_ Iparm1: 0 - face detection 1 - face recognition - comparison 2 - face recognition - Stranger 3 - face recognition - frequency 4 - face recognition - retention
	tInfo.iEnable = 1;	//Enable algorithm 0 - no enable 1 - enable
	tInfo.iParam2 = 0;	//Iparm1 is transferred to 0 when iparm1 = 0,1,2, and represents time when iparam1 is 3,4
	tInfo.iParam3 = 0;	//Iparm1 = 0,1,2, when iparm1 is 3, it represents frequency
	tInfo.iSimilar = 75;//	Similarity
	tInfo.iDevType = 1;	//0-IPC 1-NVR
	tInfo.iRecognition = 2;	//0 - not supported, 1 - not uploaded, 2 - uploaded
	sprintf(tInfo.cLibkey, "%d", iLibKey);
	tInfo.iLibEnable = 1;//0 not enabled, 1 enabled
	
	int iRet = NetClient_SetAlarmConfig(g_iLogonId, iChannelNo, ALARM_TYPE_NVR_VCA, CMD_ALARM_FACE_PARAM, &tInfo);	
	if (iRet >= 0)
	{
		printf("NetClient_SetAlarmConfig CMD_ALARM_FACE_PARAM success.\n");
	}
	else
	{
		printf("NetClient_SetAlarmConfig CMD_ALARM_FACE_PARAM failed.\n");
	}

	return iRet;
}

int GetFaceAlarm()
{
	fprintf(stderr, "Please input subalarm type (1:comparison. 2:stranger. 3:frequency. 4:retention. other:comparison.):\n");
	char cType[16] = {0};
	scanf("%s", cType);
	int iType = atoi(cType);
	if (iType<=0 || iType>4)
	{
		iType = 1;
	}

	int iChannelNo = 0;		
	
	FaceAlarmParamIn tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iType = iType;		//NVR local intelligent analysis type 1 - face recognition - comparison 2 - face recognition - Stranger 3 - face recognition - frequency 4 - face recognition - retention
	
	FaceAlarmParam	tFaceAlarmInfo[FACE_MAX_KEY_COUNT];
	memset(tFaceAlarmInfo, 0, sizeof(tFaceAlarmInfo));

	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_ALARM_PARAM, iChannelNo, &tInfo, sizeof(FaceAlarmParamIn), &tFaceAlarmInfo, sizeof(FaceAlarmParam));	
	if (iRet >= 0)
	{
			for (int i = 0; i < FACE_MAX_KEY_COUNT; ++i)
			{
				if (tFaceAlarmInfo[i].iSize <= 0)
				{
					break;
				}
				if (0 == i)
				{			
					printf("iParam2(%d) iParam3(%d) iSimilar(%d) iRecognition(%d) iEnable(%d).\n",
						tFaceAlarmInfo[i].iParam2, tFaceAlarmInfo[i].iParam3, tFaceAlarmInfo[i].iSimilar, tFaceAlarmInfo[i].iRecognition, tFaceAlarmInfo[i].iEnable);
				}
			
				printf("%d:Libkey(%d)  iLibEnable(%d).\n",
					i+1, ToIntDef(tFaceAlarmInfo[i].cLibkey), tFaceAlarmInfo[i].iLibEnable);
			}	
	}
	else
	{
		printf("NetClient_GetAlarmConfig CMD_ALARM_FACE_PARAM failed.\n");
	}

	return iRet;
}

int SetVcaStatue(int _iStatus)
{
	int iChanNo = 0;	
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

//Face image stream callback
int __stdcall CallBack_PicStreamInfo(unsigned int _uiRecvID, long _lCommand, void* _lpInfo, int _iBufLen, void* _pvUser)
{
	if (NULL == _lpInfo || NET_PICSTREAM_CMD_FACE != _lCommand)
	{
		return -1;
	}
	FacePicStream* pFace = (FacePicStream*)_lpInfo;

	//Save panorama
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

	//Save face map and map	
	for (int i = 0; i < pFace->iFaceCount && i < MAX_FACE_PICTURE_COUNT; ++i)
	{
		FacePicData tFaceData = {0};
		memcpy(&tFaceData, pFace->ptFaceData[i], min(pFace->iSizeOfFace, (int)sizeof(FacePicData)));

		char cFacePicName[256] = {0};
		sprintf(cFacePicName, ".//Pic//FacePic_No%d_Time(2%03d%02d%02d%02d%02d%02d%d).jpg", i
			, tFullData.tPicTime.uiYear, tFullData.tPicTime.uiMonth,  tFullData.tPicTime.uiDay, tFullData.tPicTime.uiHour
			, tFullData.tPicTime.uiMinute, tFullData.tPicTime.uiSecondsr, tFullData.tPicTime.uiMilliseconds);
		//Face map
		SavePicture(cFacePicName, tFaceData.pcPicData, tFaceData.iDataLen);

		//Base map
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

//Open picture stream
int StartSnap() 
{
	NetPicPara tNetPicParam = {0};
	tNetPicParam.iStructLen = sizeof(tNetPicParam);
	tNetPicParam.iChannelNo = 0;
	tNetPicParam.cbkPicStreamNotify = CallBack_PicStreamInfo; //Snapshot callback function
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

int SearchFace()
{
	//Select the face database, the last database in this demo is default, users can choose according to the actual situation
	int iLibKey = g_tLastLibInfo.iLibKey;
	if(iLibKey <= 0) {
		printf("[SearchFace]Face library modification failed: please add or query face library first.\n\n");
		return -1;
	}

	//Matting first
	FaceCutEx tCut = {0};
	tCut.iSize = sizeof(tCut);
	tCut.iPicType = 1;		//0-jpg, 1-png. The image type in this demo is PNG, and users can assign values according to the actual situation
	tCut.iChanNo = 0;       //Channel 0 is used in this demo, and users can assign values according to the actual situation
	tCut.iPageNo = 0;       //This demonstration uses channel 0, and users can assign values according to the actual situation
	tCut.iPageCount = 1;	//In this demo, only one face is selected for retrieval, and users can assign values according to the actual situation
	strncpy_ss(tCut.cPicPath,"./Pic/1111.png", sizeof(tCut.cPicPath)); //Picture path
	if (0 == strlen(tCut.cPicPath))
	{
		printf("[SearchFace]Please select a picture first!\n");
		return -1;
	}

	FaceCutQueryResult tCutRet = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_CUT_EX, 0,  &tCut, sizeof(tCut), &tCutRet,sizeof(FaceCutQueryResult));
	if (0 != iRet || 0 == strlen(tCutRet.cFileName)) 
	{
		printf("[SearchFace]Face matching failure!\n");
		return -1;
	}

	//Matting results were retrieved
	FaceSearch tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iTaskId = tCutRet.iTaskId;
	tInfo.iSimilar = 10;        //This demo uses 10 demo, users can assign values according to the actual requirements
	tInfo.iLibKey = iLibKey;
	strncpy(tInfo.cPicName,tCutRet.cFileName,sizeof(tInfo.cPicName));
	tInfo.iPageCount = FACE_MAX_PAGE_COUNT;

	FaceQueryResult tSearchRet[FACE_MAX_PAGE_COUNT];
	iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_SEARCH, 0, &tInfo, tInfo.iSize, &tSearchRet, sizeof(FaceQueryResult));
	if (0 != iRet) 
	{
		printf("[SearchFace]Face searching failure!\n");
		return -1;
	}
	int iTotalCount = tSearchRet[0].iTotal;
	printf("Face Search Result(first page)---------------\n");
	for(int i= 0; i < tSearchRet[0].iPageCount && i < FACE_MAX_PAGE_COUNT; ++i) 
	{
		printf("Serial:%d, Library key:%d, Face key:%d, Name:%s, Birth:%s, Modeling state:%d\n" , tSearchRet[i].iIndex, tSearchRet[i].tFace.iLibKey, 
			tSearchRet[i].tFace.iFaceKey, tSearchRet[i].tFace.cName, tSearchRet[i].tFace.cBirthTime, tSearchRet[i].tFace.iModeling);
	}
	printf("\n");
	return 0;
}

int SearchCapture()
{
	//Select the face database, the last database in this demo is default, users can choose according to the actual situation
	int iLibKey = g_tLastLibInfo.iLibKey;
	unsigned long ulStartWaitTime = 0;
	unsigned long ulTimeSpan = 0;
	QueryChanNo	tQueryChan[32];

	if(iLibKey <= 0) {
		printf("[SearchCapture] Face library modification failed: please add or query face library first\n");
		return -1;
	}

	//Matting first
	FaceCutEx tCut = {0};
	tCut.iSize = sizeof(tCut);
	tCut.iPicType = 1;		//0-jpg, 1-png. The image type in this demo is PNG, and users can assign values according to the actual situation
	tCut.iChanNo = 0;		//Channel 0 is used in this demo, and users can assign values according to the actual situation
	tCut.iPageNo = 0;		//This demonstration uses channel 0, and users can assign values according to the actual situation
	tCut.iPageCount = 1;	//In this demo, only one face is selected for retrieval, and users can assign values according to the actual situation
	strncpy_ss(tCut.cPicPath,"./Pic/1111.png", sizeof(tCut.cPicPath));
	if (0 == strlen(tCut.cPicPath))
	{
		printf("[SearchCapture] Please select a picture first!\n");
		return -1;
	}

	FaceCutQueryResult tCutRet[FACE_MAX_PAGE_COUNT] = {0};
	int iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_CUT_EX, 0,  &tCut, sizeof(tCut), &tCutRet,sizeof(FaceCutQueryResult));
	if (0 != iRet || 0 == strlen(tCutRet->cFileName)) 
	{
		printf("[SearchCapture] Face matching failure!\n");
		return -1;
	}

	//Matting result processing
	for(int i = 0; i < tCutRet[0].iTotal && i < FACE_MAX_PAGE_COUNT; ++i)
	{
		printf("[SearchCapture] Cut Result: Index = %d, FileName = %s\n", tCutRet[i].iIndex, tCutRet[i].cFileName);
	}

	if (0 == strlen(tCutRet[0].cFileName))
	{
		printf("[SearchCapture] Face Cut Failed!\n");
		return -1;
	}

	//Query by condition

	if (tCutRet[0].iTaskId <= 0)
	{
		printf("[SearchCapture] Please cutout first!\n");
		return -1;
	}

	FaceSearchSnap tQuery = {0};
	tQuery.iSize = sizeof(FaceSearchSnap);
	//Channel list, channel 0 is used in this demo, and the main stream is demonstrated. Users can assign values according to the actual situation
	tQuery.iChanCount = 0;
	tQuery.iChanSize = sizeof(QueryChanNo);
	tQueryChan->iChanNo = 0;
	tQueryChan->iStream = 0;
	tQuery.pChanList = tQueryChan;
	//The start and end time can be assigned according to the actual situation
	tQuery.tBegTime.iYear = 2019;
	tQuery.tBegTime.iMonth = 8;
	tQuery.tBegTime.iDay = 5;
	tQuery.tBegTime.iHour = 0;
	tQuery.tBegTime.iMinute = 0;
	tQuery.tBegTime.iSecond = 0;
	tQuery.tEndTime.iYear = 2019;
	tQuery.tEndTime.iMonth = 8;
	tQuery.tEndTime.iDay = 21;
	tQuery.tEndTime.iHour = 15;
	tQuery.tEndTime.iMinute = 0;
	tQuery.tEndTime.iSecond = 0;
	strncpy_ss(tQuery.cPicturePath, tCutRet[0].cFileName, sizeof(tQuery.cPicturePath));
	tQuery.iSimilarity = 9;   //Users can assign values according to the actual situation
	tQuery.iSortMode = 1;     //Users can assign values according to the actual situation
	tQuery.iTaskId = tCutRet[0].iTaskId;

	iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_SEARCH_SNAP, 0, &tQuery, sizeof(tQuery), NULL, 0);
	if (0 != iRet) 
	{
		printf("[SearchCapture] Start search failed!\n");
		return -1;
	}

	usleep(20*1000);
    
	ulStartWaitTime = netsdk_get_tick_count();
	//Progress inquiry
	while(1)
	{
		FaceReply tOutInfo = {0};
		FaceSearchSnapProcess tInfo = {0};
		tInfo.iSize = sizeof(FaceSearchSnapProcess);
		tInfo.iTaskId = tCutRet[0].iTaskId;
		iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_SEARCH_SNAP_PROCESS, 0, &tInfo, sizeof(FaceSearchSnapProcess), &tOutInfo, sizeof(FaceReply));
		if (0 != iRet) 
		{
			printf("[SearchCapture] Progress query failed!\n");
			return -1;
		}
		if(6 == tOutInfo.iResult)
		{
			if(100 == tOutInfo.iDelLibProgress)
			{
				printf("[SearchCapture] Search Progress is 100%\n");
				break;
			}

		}
		ulTimeSpan = netsdk_get_tick_count() - ulStartWaitTime;
		if (ulTimeSpan >= MAX_LOGON_WAIT_TIME)
		{
			printf("[SearchCapture] Wait TIMEOUT!\n");
			return -1;
		}
	}

	usleep(20*1000);

	FaceSearchSnapQuery tInfo = {0};
	tInfo.iSize = sizeof(FaceSearchSnapQuery);
	tInfo.iTaskId = tCutRet[0].iTaskId;
	tInfo.iPageSize = MAX_QUERY_PAGE_COUNT;
	tInfo.iPageNo = 0;

	FaceSearchSnapResult tOutInfo[MAX_QUERY_PAGE_COUNT] = {0};
	iRet = NetClient_FaceConfig(g_iLogonId, FACE_CMD_SEARCH_SNAP_RESULT, 0, &tInfo, sizeof(tInfo), &tOutInfo, sizeof(FaceSearchSnapResult));
	if (0 != iRet)
	{
		printf("[SearchCapture] The result query failed!\n");
		return -1;
	}
    printf("[SearchCapture] Search Result,Total Number = %d-----------------", tOutInfo[0].iTotal);
	for (int i = 0; i < tOutInfo[0].iCurPageCount && i < MAX_QUERY_PAGE_COUNT; ++i)
	{
		printf("Num: %d, Chan: %d, SnapName: %s, NegName: %s\n", tOutInfo[i].iItemIndex, tOutInfo[i].iChanNo, tOutInfo[i].tPicNeg.cFileName, tOutInfo[i].tPicSnap.cFileName);
	}
	return 0;
}

int SearchEvent()
{
	QueryChanNo	tQueryChan[32] = {0};
	NetFileQueryVca tQuery = {0};
	tQuery.iSize = sizeof(NetFileQueryVca);
	tQuery.iChanCount = 0;
	tQuery.iChanSize = sizeof(QueryChanNo);
	tQueryChan->iChanNo = 0;
	tQueryChan->iStream = 0;
	tQuery.pChanList = tQueryChan;

	tQuery.iVcaCount = 1;//This demo only queries one type of intelligent analysis, and users can choose according to their actual needs
	tQuery.iVcaList[0] = VCA_EVENT_FACEREC;   //9: Face recognition
	//The start and end time can be assigned according to the actual situation
	tQuery.tBegTime.iYear = 2019;
	tQuery.tBegTime.iMonth = 8;
	tQuery.tBegTime.iDay = 5;
	tQuery.tBegTime.iHour = 0;
	tQuery.tBegTime.iMinute = 0;
	tQuery.tBegTime.iSecond = 0;
	tQuery.tEndTime.iYear = 2019;
	tQuery.tEndTime.iMonth = 8;
	tQuery.tEndTime.iDay = 21;
	tQuery.tEndTime.iHour = 15;
	tQuery.tEndTime.iMinute = 0;
	tQuery.tEndTime.iSecond = 0;
	tQuery.iPageCount = 20;
	tQuery.iPageNo = 0;
	tQuery.iFileType = 2;   //Query file type 2 - picture
	tQuery.iConditionCount = 2; //Number of query conditions
	IntToStr((1<<16) + 7, tQuery.cQueryCondition[0]);//Retrieval type 1 - retrieve by event
	IntToStr(1, tQuery.cQueryCondition[1]);//This demo uses face detection demonstration, users can choose according to the actual situation, event type 1 - face detection 2 - face comparison 3 - Stranger 4 - frequency 5 - duration
													 
	NetFileQueryVcaResult tResult[MAX_QUERY_PAGE_COUNT] = {0};
	int iRet = NetClient_Query_V5(g_iLogonId, CMD_NETFILE_QUERY_VCA, 0, &tQuery, sizeof(NetFileQueryVca), &tResult, sizeof(NetFileQueryVcaResult));
	if (iRet < 0)
	{
		printf("[SearchEvent] NetClient_Query_V5 Failed!\n");
		return -1;
	}
	printf("[SearchEvent] Search Total Num Is %d -------------------------------\n", tResult[0].iTotal);
	//This demo only displays 20 items for demonstration, users can choose according to the actual situation
	for(int i = 0; i < tResult[0].iCurPageCount; ++i)
	{
		printf("Num: %d Age: %s FileName: %s\n", tResult[i].iItemIndex, tResult[i].cExAttr[0], tResult[i].tFileAttr[1].cFileName);
	}
	return 0;
}

int SearchFeature()
{

	QueryChanNo	tQueryChan[32] = {0};
	NetFileQueryVca tQuery = {0};
	tQuery.iSize = sizeof(FaceSearchSnap);
	//Channel list, channel 0 is used in this demo, and the main stream is demonstrated. Users can assign values according to the actual situation
	tQuery.iChanCount = 0;
	tQuery.iChanSize = sizeof(QueryChanNo);
	tQueryChan->iChanNo = 0;
	tQueryChan->iStream = 0;
	tQuery.pChanList = tQueryChan;

	tQuery.iVcaCount = 1;//This demo only queries one type of intelligent analysis, and users can choose according to their actual needs
	tQuery.iVcaList[0] = VCA_EVENT_FACEREC;   //9: Face recognition
	//The start and end time can be assigned according to the actual situation
	tQuery.tBegTime.iYear = 2019;
	tQuery.tBegTime.iMonth = 8;
	tQuery.tBegTime.iDay = 5;
	tQuery.tBegTime.iHour = 0;
	tQuery.tBegTime.iMinute = 0;
	tQuery.tBegTime.iSecond = 0;
	tQuery.tEndTime.iYear = 2019;
	tQuery.tEndTime.iMonth = 8;
	tQuery.tEndTime.iDay = 21;
	tQuery.tEndTime.iHour = 15;
	tQuery.tEndTime.iMinute = 0;
	tQuery.tEndTime.iSecond = 0;
	tQuery.iPageCount = 20;
	tQuery.iPageNo = 0;
	tQuery.iFileType = 2;   //Query file type 2 - picture
	tQuery.iConditionCount = 7; //Number of query conditions
	IntToStr((0<<16) + 7, tQuery.cQueryCondition[0]);//Search type 0 - Search by feature
	IntToStr(1, tQuery.cQueryCondition[1]);//This demo uses 1-juvenile demo, users can choose according to the actual situation, table age, 1-juvenile, 2-young, 3-middle-aged, 4-old
	IntToStr(1, tQuery.cQueryCondition[2]);//This demo uses 1-male demo, users can choose according to the actual situation, indicating gender, 1-male, 2-female, 3-unknown
	IntToStr(1, tQuery.cQueryCondition[3]);//This demo uses 1-han demo, users can choose according to the actual situation, indicating ethnic, 1-han, 2-ethnic minorities
	IntToStr(2, tQuery.cQueryCondition[5]);//This demo uses 2-not wearing the demo, users can choose according to the actual situation, which means wearing glasses 0-reserved, 1-wearing, 2-not wearing
	IntToStr(2, tQuery.cQueryCondition[6]);//This demo uses 2-not wearing the demonstration, the user can choose according to the actual situation, that is, wearing mask, 0-reserved, 1-wearing, 2-not wearing

	NetFileQueryVcaResult tResult[MAX_QUERY_PAGE_COUNT] = {0};
	int iRet = NetClient_Query_V5(g_iLogonId, CMD_NETFILE_QUERY_VCA, 0, &tQuery, sizeof(NetFileQueryVca), &tResult, sizeof(NetFileQueryVcaResult));
	if (iRet < 0)
	{
		printf("[SearchEvent] NetClient_Query_V5 Failed!\n");
		return -1;
	}
	printf("[SearchEvent] Search Total Num Is %d -------------------------------\n", tResult[0].iTotal);
	for(int i = 0; i < tResult[0].iCurPageCount; ++i)
	{
		printf("Num: %d FileName: %s BeginTime:%d-%d-%d %d:%d:%d EndTime: %d-%d-%d %d:%d:%d\n", tResult[i].iItemIndex, tResult[i].tFileAttr[1].cFileName, tResult[i].tBegTime.iYear,
			tResult[i].tBegTime.iMonth, tResult[i].tBegTime.iDay, tResult[i].tBegTime.iHour, tResult[i].tBegTime.iMinute, tResult[i].tBegTime.iSecond, 
			tResult[i].tEndTime.iYear,tResult[i].tEndTime.iMonth, tResult[i].tEndTime.iDay, tResult[i].tEndTime.iHour, tResult[i].tEndTime.iMinute, tResult[i].tEndTime.iSecond);
	}
	return 0;
}



int main(int argc,char *argv[])
{
	int iRet = LoadNVSSDK();
	if (0 != iRet)
	{
		fprintf(stderr,"[main] LoadLib failed.\n");
		return -1;
	}

	NetClient_SetSDKWorkMode(2);//SDKWorking mode platform lightweight
	
	//Register main callback function
	NetClient_SetNotifyFunction_V4(Notify_Main, NULL, NULL, NULL, NULL);

	//start-up SDK
	NetClient_Startup_V4(0, 0, 0);

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
		fprintf(stderr, "*********************************************************************************************\n");
		fprintf(stderr,
			"[0]Quit                      [1]QueryFaceLibrary		[2]AddFaceLibrary\n"
			"[3]ModifyFaceLibrary         [4]DeleteFaceLibrary		[5]QueryFaceBasemap  \n"
			"[6]AddFaceBasemap            [7]ModifyFaceBasemap		[8]DeleteFaceBasemap\n"
			"[9]EnableFaceDetection       [10]EnableFaceRecognition		[11]SetFaceDetectionParameters\n"
			"[12]SetFaceScheduleEnable    [13]GetFaceScheduleEnable		[14]SetFaceScheduleTemplate\n"
			"[15]GetFaceScheduleTemplate  [16]SetFaceRecognitionAlarm	[17]GetFaceRecognitionAlarm\n"
			"[18]FaceSearch               [19]SearchCapture			[20]SearchEvent\n"
			"[21]FaceFeature              [22]GetFaceRecognitionEnable	[23]GetFaceDetectionEnable \n");
		fprintf(stderr, "*********************************************************************************************\n");
		
		fprintf(stderr, "Please select:");
		int iOpt = 0;
		
		scanf("%d", &iOpt);
		fflush(stdin);
		if (0 == iOpt)
		{
			break;
		}
		else if (1 == iOpt)	//Query face database
		{
			FaceLibraryQuery();
		}
		else if (2 == iOpt)	//Add face database
		{
			FaceLibraryAdd();
		}
		else if (3 == iOpt)	//Modify face database
		{
			FaceLibraryModify();
		}
		else if (4 == iOpt)	//Delete face database
		{			
			FaceLibraryDelete();  
		}
		else if (5 == iOpt)	//Query face base map
		{
			FacePictureQuery(0);		//Only the first page is queried here
		}
		else if (6 == iOpt)	//Add face base map
		{
			//Add face needs to pause intelligent analysis
			fprintf(stderr,"[main] Please wait for the smart analysis to pause the results!\n");
			g_iVcaStatus = 0;
			SetVcaStatue(VCA_SUSPEND_STATUS_PAUSE);	//Pause intelligent analysis
			
			usleep(500*1000);

			if (VCA_SUSPEND_RESULT_SUCCESS != g_iVcaStatus) {
				fprintf(stderr,"[main] Smart analysis pause failed, exit add!\n");
				getchar();
				continue;
			}
			FacePictureAdd();			//After the pause is successful, you can add it multiple times
			SetVcaStatue(VCA_SUSPEND_STATUS_RESUME);//Intelligent analysis needs to be restored after adding face
		}
		else if (7 == iOpt)	//Modify face base map
		{
			FacePictureModify();
		}
		else if (8 == iOpt)	//Delete face base map
		{
			FacePictureDelete();
		}
		else if (22 == iOpt)	//Get face recognition enable
		{
			GetFaceRecognitionEnable();
		}
		else if (23 == iOpt)	//Get face detection enable
		{
			GetFaceDetectionEnable();
		}
		else if (9 == iOpt)	//Turn on face detection
		{
			FaceDetectionEnable();
		}
		else if (10 == iOpt)//Turn on face recognition
		{
			fprintf(stderr,"[main] Please wait for the smart analysis to pause the results!\n");
			g_iVcaStatus = 0;
			SetVcaStatue(VCA_SUSPEND_STATUS_PAUSE);	//Pause intelligent analysis
			usleep(500*1000);
			if (VCA_SUSPEND_RESULT_SUCCESS != g_iVcaStatus) {
				fprintf(stderr,"[main] Smart analysis pause failed!\n");
				getchar();
				continue;
			}
			FaceRecognitionEnable();//Need to pause intelligent analysis
			SetVcaStatue(VCA_SUSPEND_STATUS_RESUME);//Intelligent analysis needs to be restored after setting
		}
		else if (11 == iOpt)//Setting face detection parameters
		{
			fprintf(stderr,"[main] Please wait for the smart analysis to pause the results!\n");
			g_iVcaStatus = 0;
			SetVcaStatue(VCA_SUSPEND_STATUS_PAUSE);	//Pause intelligent analysis
			usleep(500*1000);
 			if (VCA_SUSPEND_RESULT_SUCCESS != g_iVcaStatus) {
 				fprintf(stderr,"[main] Smart analysis pause failed!\n");
 				getchar();
 				continue;
 			}
			SetDetectParam();//Face detection related parameters (need to pause intelligent analysis)
			SetVcaStatue(VCA_SUSPEND_STATUS_RESUME);//Intelligent analysis needs to be restored after setting

			SetPicStreamUploadParam();//Capture image quality and upload related parameters
		}
		else if (12 == iOpt)	//Set face deployment enable
		{
			SetFaceScheduleEnable();
		}
		else if (13 == iOpt)	//Get face deployment enable
		{
			GetFaceScheduleEnable();
		}
		else if (14 == iOpt)	//Set up face protection template
		{
			SetFaceSchedule();
		}
		else if (15 == iOpt)	//Get face protection template
		{
			GetFaceSchedule();
		}
		else if (16 == iOpt)	//Set face recognition alarm
		{
			SetFaceAlarm();
		}
		else if (17 == iOpt)	//Get face recognition alarm
		{
			GetFaceAlarm();
		}
		else if (18 == iOpt)	//Search for pictures with pictures
		{
			SearchFace();
		}
		else if(19 == iOpt)
		{
		   SearchCapture();
		}
		else if(20 == iOpt)  //Retrieve by event
		{
			SearchEvent();
		}
		else if(21 == iOpt)   //Search by feature
		{
			SearchFeature();
		}
	}

	//Log off users and release SDK resources
	NetClient_Logoff(g_iLogonId);	
	NetClient_Cleanup();

	return 0;
}

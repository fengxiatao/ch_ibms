#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "NetSdk.h"
#include <time.h>


#ifdef __WIN__
#include <process.h>

#else
#include <pthread.h>
#include <unistd.h>
#include<dlfcn.h>

#endif

int g_iLogonID = -1;
unsigned int g_uiConnID = -1;//connect ID
int g_iPos = 0;
int g_iSize = 0;
int g_iFileNum = -1;
int g_iEndThread = 0;
int g_iCurrentCount = -1;
NVS_FILE_DATA g_NvsFileInfo[100] = {{0}};
DOWNLOAD_FILE g_DownloadFile[100] = {{0}};

int LogonDevice(int _iLogonType);
void PrintFileInfo();
void QueryFile();
void DwonloadByTime();
int DownloadFileByFile( int _iFileNum,int _iFlag, int _iDownloadPos , int _iDownloadSpeed );
void __stdcall RecvRawFramNotify(unsigned int _uiID, unsigned char* _pucData,int _iLen, RAWFRAME_INFO* _ptRawFrameInfo, void* _pvUser);
void StartSuperPlayBackByFile();
void StartSuperPlayBackByTime();

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

#ifdef __WIN__
void WinGetDownloadProcessThread( void * _p)
{
	while(1)
	{
		if( 1 == g_iEndThread )
		{
			return;
		}
		if( -1 != g_uiConnID)
		{
			int iRet = NetClient_NetFileGetDownloadPos(g_uiConnID, &g_iPos, &g_iSize);
			if( 0 == iRet)
			{
				printf("Position = %d, Size = %d \n", g_iPos, g_iSize);
			}
			else
			{
				printf("Err : NetClient_NetFileGetDownloadPos\n");
				return ;
			}
		}
		Sleep(1000);
	}
}

void usleep(int _iMicroSecond)
{
	if (_iMicroSecond < 1000 && _iMicroSecond != 0)
	{
		_iMicroSecond = 1000;
	}
	Sleep(_iMicroSecond/1000);
}
#else
void *LinuxGetDownloadProcessThread( void * _p)
{
	while(1)
	{
		if( 1 == g_iEndThread )
		{
			return((void*)0);

		}
		if( -1 != (int)g_uiConnID)
		{
			int iRet = NetClient_NetFileGetDownloadPos(g_uiConnID, &g_iPos, &g_iSize);
			if( 0 == iRet)
			{
				printf("Position = %d, Size = %d \n", g_iPos, g_iSize);
			}
			else
			{
				printf("Err : NetClient_NetFileGetDownloadPos\n");
				return((void*)0);

			}
		}
		sleep(1);
	}
	return((void*)0);

}
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
	//stdin->_IO_read_ptr = stdin->_IO_read_end;
	return 0;
}


#endif


void Notify_Main(int _iLogonID, long  _lWparam, void*  _pvLParam, void* _pvUsr)
{
	int lMsgType = LOWORD(_lWparam);
	long iMsgValue = (long)_pvLParam;
	switch (lMsgType)
	{
	case WCM_LOGON_NOTIFY:
		{
			printf("[Notify_Main]->WCM_LOGON_NOTIFY\n");
			if((LOGON_SUCCESS == iMsgValue) )
			{
				printf("[Notify_Main]->Success : Logon\n");
			}
			else
			{
				printf("[Notify_Main]->Err = %d : Logon, Please Retry\n", iMsgValue);
				return;
			}
		}
		break;
	case WCM_QUERYFILE_FINISHED:
		{
			printf("[Notify_Main]->WCM_QUERYFILE_FINISHED\n");
			int iTotalCount = 0;	
			NetClient_NetFileGetFileCount(_iLogonID, &iTotalCount, &g_iCurrentCount);//Number of query files
			printf("[Notify_Main]->TotalCount(%d),CurrentCount(%d)\n",iTotalCount,g_iCurrentCount);
			if( (g_iCurrentCount == 0) || (iTotalCount == 0) )
			{
				printf( "[Notify_Main]->Err : Find No File\n");
				break;
			}
			printf("[Notify_Main]-><Press [Enter] Key To Get File Info>\n");
		}
		break;
	case WCM_DWONLOAD_FINISHED:
		{
			g_iEndThread = 1;
			g_iCurrentCount = -1;
			printf("[Notify_Main]->WCM_DWONLOAD_FINISHED\n");
			printf("[Notify_Main]-><Download Position Is = 100%% >\n");
			unsigned int uiConnID = (unsigned int)iMsgValue;
			NetClient_NetFileStopDownloadFile( uiConnID);
			if (uiConnID == g_uiConnID)
			{
				g_uiConnID = -1;
			}
		}
		break;		
	case WCM_DWONLOAD_FAULT:
		{
			g_iEndThread = 1;
			g_iCurrentCount = -1;
			printf("[Notify_Main]->WCM_DWONLOAD_FAULT\n");				
			unsigned int uiConnID = (unsigned int)iMsgValue;
			NetClient_NetFileStopDownloadFile( uiConnID);
			if (uiConnID == g_uiConnID)
			{
				g_uiConnID = -1;
			}
		}
		break;
	case WCM_DOWNLOAD_INTERRUPT:
		{
			g_iEndThread = 1;
			g_iCurrentCount = -1;
			printf("[Notify_Main]->WCM_DOWNLOAD_INTERRUPT\n");
			unsigned int uiConnID = (unsigned int)iMsgValue;
			NetClient_NetFileStopDownloadFile( uiConnID);
			if (uiConnID == g_uiConnID)
			{
				g_uiConnID = -1;
			}
		}
		break;
	default:
		break;
	}
}

//Original stream callback function
void __stdcall GetRawNotify( unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser )
{
	if( -1 == (int)_ulID)
	{
		return;
	}
	if( NULL != _pRawFrameInfo)
	{
		//printf("Log : Raw Stream Type = %d, Length = %d\n", _pRawFrameInfo->nType, _iLen);
	}
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

int main(int argc,char *argv[])
{
	char cCmd[16] = {0};
	int iLogonType = SERVER_NORMAL;
	char cTemp[8] = {0};
	int iRet = LoadNVSSDK();
	if (0 != iRet)
	{
		printf("Err : LoadNVSSDK\n");
		return -1;
	}
	
	fprintf(stderr, "Please input LogonType: 0----Normal  1----Active\n");
	gets_s(cTemp, 8);
	iLogonType = to_int_def(cTemp, 0);
	if (SERVER_ACTIVE == iLogonType)
	{
		fprintf(stderr, "Please input listening port:");
		gets_s(cTemp, 8);
		int iLlisteningPort = to_int_def(cTemp, 0);
		iRet = NetClient_Startup_V4(iLlisteningPort, 0, 0);//initialization SDK
	}
	else
	{
		iRet = NetClient_Startup_V4(0, 0, 0);//initialization SDK
	}
	if( 0 == iRet )
	{
		printf("Success : NetClient_Startup_V4()\n");
	}
	else
	{
		printf("Err : NetClient_Startup_V4()\n");
		return -1;
	}
	NetClient_SetNotifyFunction_V4(Notify_Main, NULL, NULL, NULL, NULL);
	//Login device
	iRet = LogonDevice(iLogonType);
	if( 0 == iRet )
	{
		printf("Success : LogonDevice()\n");
	}
	else
	{
		printf("Err : LogonDevice()\n");
	}
	while(1)
	{
		if( g_iCurrentCount > 0 )
		{
			if(-1 != g_iLogonID)
			{
				PrintFileInfo();
			}
		}
		g_iEndThread = 0;
		printf("<Please Input Download Mode[f=File, t=Time, pf=PlaybackFile, pt=PlaybackTime, s=Stop, q=Exit]>:\n");
		gets_s( cCmd, 16);
		//sign out
		if( 0 == strcmp(cCmd, "q"))
		{
			if( -1 != g_iLogonID)
			{
				NetClient_Logoff(g_iLogonID);//Log off user
			}
			NetClient_Cleanup();//Release SDK resources
			FreeNVSSDK();//Release interface library
			return 0;
		}
		//File mode
		else if( 0 == strcmp(cCmd, "f"))
		{
			if(-1 != g_iLogonID)
			{
				QueryFile();
			}
		}
		//Time period mode
		else if( 0 == strcmp(cCmd, "t"))
		{
			if(-1 != g_iLogonID)
			{
				DwonloadByTime();
			}
		}
		//File mode playback
		else if( 0 == strcmp(cCmd, "pf"))
		{
			if(-1 != g_iLogonID)
			{
				StartSuperPlayBackByFile();
			}
		}
		//Time period mode playback
		else if( 0 == strcmp(cCmd, "pt"))
		{
			if(-1 != g_iLogonID)
			{
				StartSuperPlayBackByTime();
			}
		}
		else if (0 == strcmp(cCmd, "s"))
		{
			g_iCurrentCount = 0;
			if( -1 !=  (int)g_uiConnID)
			{
				int iRet =  NetClient_NetFileStopDownloadFile( g_uiConnID);
				if( 0 == iRet)
				{
					printf("Success : NetClient_NetFileStopDownloadFile\n");
				}
				else
				{
					g_uiConnID = -1;
					printf("Err : NetClient_NetFileStopDownloadFile\n");
				}
			}
		}
	}
	getchar();
	return 0;
}




int LogonDevice( int _iLogonType )
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
		fprintf(stderr, "Please input server IP: ");
		gets_s(cIP, 16);
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
	else
	{
		fprintf(stderr,"[NetClient_Logon_V4] Success! %d\n", g_iLogonID);
	}
	return g_iLogonID;
}


void PrintFileInfo()
{
	NVS_FILE_DATA fileInfo = {0};
	for(int i = 0; i < g_iCurrentCount; ++i)
	{          
		int iRet = NetClient_NetFileGetQueryfile(g_iLogonID, i, &fileInfo);//Get file information
		if(0 == iRet)
		{
			printf("File[%d] : Name(%s), Type(%d), Channel(%d), Size(%d)\n", i, fileInfo.cFileName
				, fileInfo.iType, fileInfo.iChannel, fileInfo.iFileSize); 
			g_NvsFileInfo[i] = fileInfo;
		}
		else
		{
			printf("Err : NetFileGetMultiChanQueryFilefile\n");
			break;
		}
	}
	printf("<Please Choose The File[x] Number To Download>, Press [Enter] Will Download File[0],Press 'q' To Exit:");
	char cTemp[6] = {0};
	gets_s(cTemp, 6);
	if( NULL != cTemp )
	{
		if( 0 == strcmp("q",cTemp))
		{
			g_iCurrentCount = -1;
			return;
		}
		g_iFileNum = atoi(cTemp);
		memset(cTemp, 0 , 6);
		if( g_iFileNum < 0 || g_iFileNum > 19)
		{
			printf("<Please Input Correct File Number>\n");
			return;
		}
		else
		{
			DownloadFileByFile( g_iFileNum , 0, -1, 8);

#ifdef __WIN__
			_beginthread( WinGetDownloadProcessThread, 0, NULL);
#else
			pthread_t t;
			pthread_create(&t, NULL,LinuxGetDownloadProcessThread, NULL);
#endif
		}
	}
}

int DownloadFileByFile( int _iFileNum,int _iFlag, int _iDownloadPos , int _iDownloadSpeed )
{
	if( -1 == _iFileNum)
	{
		return -1;
	}
	char cLocalFileName[256] = {0};
	DOWNLOAD_FILE downloadFile = {0};
    strncpy_ss(cLocalFileName, g_NvsFileInfo[_iFileNum].cFileName, (int)(strlen(g_NvsFileInfo[_iFileNum].cFileName))-3);
	printf("Input 0 = SDV; 1(3) = PS; 4-MP4; 5-AVI");
	char cTemp[6] = {0};
	gets_s(cTemp, 6);
	if( 0 == strcmp("0", cTemp)) {
		strcat(cLocalFileName, ".sdv");
		downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_SDV;
	} else if(0 == strcmp("1", cTemp) || 0 == strcmp("3", cTemp)) {
		strcat(cLocalFileName, ".ps");
		downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_PS;
	} else if(0 == strcmp("4", cTemp)) {
		strcat(cLocalFileName, ".mp4");
		downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_ZFMP4;
	} else if(0 == strcmp("5", cTemp)) {
		strcat(cLocalFileName, ".avi");
		downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_AVI;
	} else {
		printf("Please Input Correctly!");
		return -1;
	}
	memset(cTemp, 0 , 6);
	downloadFile.m_iSize = sizeof(DOWNLOAD_FILE);
	downloadFile.m_iPosition = -1;
	strcpy(downloadFile.m_cLocalFilename ,cLocalFileName);
	strcpy(downloadFile.m_cRemoteFilename ,g_NvsFileInfo[_iFileNum].cFileName);
	//downloadFile.m_iSaveFileType = 0;
	downloadFile.m_iSpeed = 32;
	int iRet = NetClient_NetFileDownload(&g_uiConnID, g_iLogonID, DOWNLOAD_CMD_FILE,&downloadFile, sizeof(DOWNLOAD_FILE));
	if( 0 == iRet)
	{	
		printf("Success : NetClient_NetFileDownload\n");
		if( -1 != (int)g_uiConnID)
		{
			//Set original stream callback
			iRet = NetClient_SetRawFrameCallBack(g_uiConnID, GetRawNotify, NULL);
			if( iRet < 0 )
			{
				printf("Err : NetClient_SetRawFrameCallBack\n");
				return -1;
			}
		}
	}
	else
	{
		printf("Err : NetClient_NetFileDownload\n");
		return -1;
	}
	return g_uiConnID;
}



void QueryFile()
{
	time_t now;
	time(&now);
	struct  tm* now_time = localtime(&now);

	NETFILE_QUERY_V5 tMultiChanQueryFile = {0};
	tMultiChanQueryFile.iType = 0xFF;
	tMultiChanQueryFile.iQueryChannelNo = 0;
	tMultiChanQueryFile.iStreamNo = 0;
	tMultiChanQueryFile.tStartTime.iYear = 1900 + now_time->tm_year;
	tMultiChanQueryFile.tStartTime.iMonth = 1 + now_time->tm_mon;
	tMultiChanQueryFile.tStartTime.iDay = now_time->tm_mday;
	tMultiChanQueryFile.tStartTime.iHour = 0;
	tMultiChanQueryFile.tStartTime.iMinute = 0;
	tMultiChanQueryFile.tStartTime.iSecond = 0;
	
	tMultiChanQueryFile.tStopTime.iYear = 1900 + now_time->tm_year;
	tMultiChanQueryFile.tStopTime.iMonth = 1 + now_time->tm_mon;
	tMultiChanQueryFile.tStopTime.iDay = now_time->tm_mday;
	tMultiChanQueryFile.tStopTime.iHour = 23;
	tMultiChanQueryFile.tStopTime.iMinute = 59;
	tMultiChanQueryFile.tStopTime.iSecond = 59;
	tMultiChanQueryFile.iPageSize = 20;
	tMultiChanQueryFile.iPageNo = 0;
	tMultiChanQueryFile.iTrigger = 0;
	tMultiChanQueryFile.iFiletype = 1;
	tMultiChanQueryFile.iTriggerType = 0x7FFFFFFF;

	int iRet = NetClient_Query_V4(g_iLogonID, CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE, 0, &tMultiChanQueryFile, sizeof(tMultiChanQueryFile));
	if(0 == iRet)
	{
		char cDate[128];
		sprintf(cDate, "%d-%02d-%02d %02d:%02d:%02d ~ %d-%02d-%02d %02d:%02d:%02d",
			tMultiChanQueryFile.tStartTime.iYear,
			tMultiChanQueryFile.tStartTime.iMonth,
			tMultiChanQueryFile.tStartTime.iDay,
			tMultiChanQueryFile.tStartTime.iHour,
			tMultiChanQueryFile.tStartTime.iMinute,
			tMultiChanQueryFile.tStartTime.iSecond,
			tMultiChanQueryFile.tStopTime.iYear,
			tMultiChanQueryFile.tStopTime.iMonth,
			tMultiChanQueryFile.tStopTime.iDay,
			tMultiChanQueryFile.tStopTime.iHour,
			tMultiChanQueryFile.tStopTime.iMinute,
			tMultiChanQueryFile.tStopTime.iSecond);
		printf("Success : NetFileQuery.  default Interval: [%s].\n", cDate);
	}
	else
	{
		printf("Err : NetFileQuery\n");
		return;
	}
}


void DwonloadByTime()
{
	time_t now;
	time(&now);
	struct  tm* now_time = localtime(&now);

	DOWNLOAD_TIMESPAN downlaodTimespan = {0};
	downlaodTimespan.m_iSize = sizeof(DOWNLOAD_TIMESPAN);
	downlaodTimespan.m_tTimeBegin.iYear = 1900 + now_time->tm_year;
	downlaodTimespan.m_tTimeBegin.iMonth = 1 + now_time->tm_mon;
	downlaodTimespan.m_tTimeBegin.iDay = now_time->tm_mday;
	downlaodTimespan.m_tTimeBegin.iHour = now_time->tm_hour - 1;
	downlaodTimespan.m_tTimeBegin.iMinute = 0;
	downlaodTimespan.m_tTimeBegin.iSecond = 0;

	downlaodTimespan.m_tTimeEnd.iYear = 1900 + now_time->tm_year;
	downlaodTimespan.m_tTimeEnd.iMonth = 1 + now_time->tm_mon;
	downlaodTimespan.m_tTimeEnd.iDay = now_time->tm_mday;
	downlaodTimespan.m_tTimeEnd.iHour = now_time->tm_hour;
	downlaodTimespan.m_tTimeEnd.iMinute = 0;
	downlaodTimespan.m_tTimeEnd.iSecond = 0;
	
	downlaodTimespan.m_iChannelNO = 0;
	downlaodTimespan.m_iPosition = -1;
	downlaodTimespan.m_iSpeed = 32;
	downlaodTimespan.m_iReqMode = 1;

	printf("Input 0 = SDV; 1(3) = PS; 4-MP4; 5-AVI");
	char cTemp[6] = {0};
	gets_s(cTemp, 6);
	if( 0 == strcmp( "0", cTemp)) {
		downlaodTimespan.m_iSaveFileType = DOWNLOAD_FILE_TYPE_SDV;
		sprintf(downlaodTimespan.m_cLocalFilename, "%d%d%d%d%d%d-%d%d%d%d%d%d.sdv",
			downlaodTimespan.m_tTimeBegin.iYear,
			downlaodTimespan.m_tTimeBegin.iMonth,
			downlaodTimespan.m_tTimeBegin.iDay,
			downlaodTimespan.m_tTimeBegin.iHour,
			downlaodTimespan.m_tTimeBegin.iMinute,
			downlaodTimespan.m_tTimeBegin.iSecond,
			downlaodTimespan.m_tTimeEnd.iYear,
			downlaodTimespan.m_tTimeEnd.iMonth ,
			downlaodTimespan.m_tTimeEnd.iDay ,
			downlaodTimespan.m_tTimeEnd.iHour ,
			downlaodTimespan.m_tTimeEnd.iMinute,
			downlaodTimespan.m_tTimeEnd.iSecond);
	} else if(0 == strcmp( "1", cTemp) || 0 == strcmp("3", cTemp)) {
		downlaodTimespan.m_iSaveFileType = DOWNLOAD_FILE_TYPE_PS;
		sprintf(downlaodTimespan.m_cLocalFilename, "%d%d%d%d%d%d-%d%d%d%d%d%d.ps",
			downlaodTimespan.m_tTimeBegin.iYear,
			downlaodTimespan.m_tTimeBegin.iMonth,
			downlaodTimespan.m_tTimeBegin.iDay,
			downlaodTimespan.m_tTimeBegin.iHour,
			downlaodTimespan.m_tTimeBegin.iMinute,
			downlaodTimespan.m_tTimeBegin.iSecond,
			downlaodTimespan.m_tTimeEnd.iYear,
			downlaodTimespan.m_tTimeEnd.iMonth ,
			downlaodTimespan.m_tTimeEnd.iDay ,
			downlaodTimespan.m_tTimeEnd.iHour ,
			downlaodTimespan.m_tTimeEnd.iMinute,
			downlaodTimespan.m_tTimeEnd.iSecond);
	} else if(0 == strcmp("4", cTemp)) {
		downlaodTimespan.m_iSaveFileType = DOWNLOAD_FILE_TYPE_ZFMP4;
		sprintf(downlaodTimespan.m_cLocalFilename, "%d%d%d%d%d%d-%d%d%d%d%d%d.mp4",
			downlaodTimespan.m_tTimeBegin.iYear,
			downlaodTimespan.m_tTimeBegin.iMonth,
			downlaodTimespan.m_tTimeBegin.iDay,
			downlaodTimespan.m_tTimeBegin.iHour,
			downlaodTimespan.m_tTimeBegin.iMinute,
			downlaodTimespan.m_tTimeBegin.iSecond,
			downlaodTimespan.m_tTimeEnd.iYear,
			downlaodTimespan.m_tTimeEnd.iMonth ,
			downlaodTimespan.m_tTimeEnd.iDay ,
			downlaodTimespan.m_tTimeEnd.iHour ,
			downlaodTimespan.m_tTimeEnd.iMinute,
			downlaodTimespan.m_tTimeEnd.iSecond);
	} else if(0 == strcmp("5", cTemp)) {
		downlaodTimespan.m_iSaveFileType = DOWNLOAD_FILE_TYPE_AVI;
		sprintf(downlaodTimespan.m_cLocalFilename, "%d%d%d%d%d%d-%d%d%d%d%d%d.avi",
			downlaodTimespan.m_tTimeBegin.iYear,
			downlaodTimespan.m_tTimeBegin.iMonth,
			downlaodTimespan.m_tTimeBegin.iDay,
			downlaodTimespan.m_tTimeBegin.iHour,
			downlaodTimespan.m_tTimeBegin.iMinute,
			downlaodTimespan.m_tTimeBegin.iSecond,
			downlaodTimespan.m_tTimeEnd.iYear,
			downlaodTimespan.m_tTimeEnd.iMonth ,
			downlaodTimespan.m_tTimeEnd.iDay ,
			downlaodTimespan.m_tTimeEnd.iHour ,
			downlaodTimespan.m_tTimeEnd.iMinute,
			downlaodTimespan.m_tTimeEnd.iSecond);
	} else {
		printf("Please Input Correctly!");
		return;
	}

	memset(cTemp, 0 , 6);
	if( -1 == g_iLogonID)
	{
		printf("Err : LogonId\n");
		return;
	}
	int iRet = NetClient_NetFileDownload(&g_uiConnID, g_iLogonID, DOWNLOAD_CMD_TIMESPAN,&downlaodTimespan, sizeof(DOWNLOAD_TIMESPAN));
	if(iRet < 0)
	{
		printf("Err : NetClient_NetFileDownload\n");
		return;
	}
	else
	{
		char cDate[128];
		sprintf(cDate, "%d-%02d-%02d %02d:%02d:%02d ~ %d-%02d-%02d %02d:%02d:%02d",
			downlaodTimespan.m_tTimeBegin.iYear,
			downlaodTimespan.m_tTimeBegin.iMonth,
			downlaodTimespan.m_tTimeBegin.iDay,
			downlaodTimespan.m_tTimeBegin.iHour,
			downlaodTimespan.m_tTimeBegin.iMinute,
			downlaodTimespan.m_tTimeBegin.iSecond,
			downlaodTimespan.m_tTimeEnd.iYear,
			downlaodTimespan.m_tTimeEnd.iMonth,
			downlaodTimespan.m_tTimeEnd.iDay,
			downlaodTimespan.m_tTimeEnd.iHour,
			downlaodTimespan.m_tTimeEnd.iMinute,
			downlaodTimespan.m_tTimeEnd.iSecond);
		printf("Success : NetClient_NetFileDownload. default Interval: [%s].\n", cDate);
		if( -1 != (int)g_uiConnID)
		{
			iRet = NetClient_SetRawFrameCallBack(g_uiConnID, GetRawNotify, NULL);
			if( iRet < 0 )
			{
				printf("Err : NetClient_SetRawFrameCallBack\n");
				return;
			}
		}
		
#ifdef __WIN__
		_beginthread( WinGetDownloadProcessThread, 0, NULL);
#else
		pthread_t t;
		pthread_create(&t, NULL,LinuxGetDownloadProcessThread, NULL);
#endif
	}
}

void StartSuperPlayBackByFile()
{
	PlayerParam tParam = {0};
	tParam.iSize = sizeof(PlayerParam);
	tParam.iLogonID = g_iLogonID;
	tParam.tRawNotifyInfo.pcbkRawFrameNotify = &RecvRawFramNotify;
	tParam.tRawNotifyInfo.pUserData = NULL;
	tParam.tRawNotifyInfo.iForbidDecodeEnable = RAW_NOTIFY_FORBID_DECODE;

	printf("input file name:\n");
	char cTemp[128] = {0};
	gets_s(cTemp, 128);

	strcpy(tParam.strFileName, cTemp);

	int iRet = NetClient_PlayBack(&g_uiConnID, PLAYBACK_TYPE_FILE, &tParam, NULL);
	if (iRet < 0)
	{
		printf("Err : NetClient_PlayBack:PLAYBACK_TYPE_FILE fail!\n");
	}
}

void StartSuperPlayBackByTime()
{
	time_t now;
	time(&now);
	struct  tm* now_time = localtime(&now);

	PlayerParam tParam = {0};
	tParam.iSize = sizeof(PlayerParam);
	tParam.iLogonID = g_iLogonID;
	tParam.iChannNo = 0;
	tParam.tBeginTime.iYear = 1900 + now_time->tm_year;
	tParam.tBeginTime.iMonth = 1 + now_time->tm_mon;
	tParam.tBeginTime.iDay = now_time->tm_mday;
	tParam.tBeginTime.iHour = now_time->tm_hour - 1;
	tParam.tBeginTime.iMinute = 0;
	tParam.tBeginTime.iSecond = 0;
	tParam.tEndTime.iYear = 1900 + now_time->tm_year;
	tParam.tEndTime.iMonth = 1 + now_time->tm_mon;
	tParam.tEndTime.iDay = now_time->tm_mday;
	tParam.tEndTime.iHour = now_time->tm_hour;
	tParam.tEndTime.iMinute = 0;
	tParam.tEndTime.iSecond = 0;
	tParam.tRawNotifyInfo.pcbkRawFrameNotify = &RecvRawFramNotify;
	tParam.tRawNotifyInfo.pUserData = NULL;
	tParam.tRawNotifyInfo.iForbidDecodeEnable = RAW_NOTIFY_FORBID_DECODE;

	int iRet =	NetClient_PlayBack(&g_uiConnID, PLAYBACK_TYPE_TIME, &tParam, NULL);
	if (iRet < 0)
	{
		printf("Err : NetClient_PlayBack:PLAYBACK_TYPE_TIME fail!\n");
		return ;
	}
	else
	{
		char cDate[128];
		sprintf(cDate, "%d-%02d-%02d %02d:%02d:%02d ~ %d-%02d-%02d %02d:%02d:%02d",
			tParam.tBeginTime.iYear,
			tParam.tBeginTime.iMonth,
			tParam.tBeginTime.iDay,
			tParam.tBeginTime.iHour,
			tParam.tBeginTime.iMinute,
			tParam.tBeginTime.iSecond,
			tParam.tEndTime.iYear,
			tParam.tEndTime.iMonth,
			tParam.tEndTime.iDay,
			tParam.tEndTime.iHour,
			tParam.tEndTime.iMinute,
			tParam.tEndTime.iSecond);
		printf("Success : NetClient_PlayBack:PLAYBACK_TYPE_TIME. default Interval: [%s].\n", cDate);
	}
}

int g_iCnt = 0;
void __stdcall RecvRawFramNotify(unsigned int _uiID, unsigned char* _pucData,int _iLen, RAWFRAME_INFO* _ptRawFrameInfo, void* _pvUser)
{
	RAWFRAME_INFO tRawFrameInfo = {0};
	memcpy(&tRawFrameInfo, _ptRawFrameInfo, sizeof(tRawFrameInfo));
	char cLog[1024] = {0};
	sprintf(cLog, "[RecvRawFramNotify PlayerId(%d), FrameType(%d), DataLen(%d), nWidth(%d), nHeight(%d),nStamp(%d) nEnCoder(%d), nFrameRate(%d) nAbsStamp(%d) ucBitsPerSample(%d), uiSamplesPerSec(%d)\n]"
		, _uiID, tRawFrameInfo.nType, _iLen, tRawFrameInfo.nWidth, tRawFrameInfo.nHeight, tRawFrameInfo.nStamp, tRawFrameInfo.nEnCoder, tRawFrameInfo.nFrameRate, tRawFrameInfo.nAbsStamp, tRawFrameInfo.ucBitsPerSample, tRawFrameInfo.uiSamplesPerSec);
//#ifdef _DEBUG_LOG_
	if (0 == g_iCnt % 1000) {
		printf(cLog);
	}
	g_iCnt++;
//#endif

	if (0 == tRawFrameInfo.nType || 1 == tRawFrameInfo.nType)
	{	//Video data
	}
	else if (5 == tRawFrameInfo.nType)
	{
		//Audio data
	}
}



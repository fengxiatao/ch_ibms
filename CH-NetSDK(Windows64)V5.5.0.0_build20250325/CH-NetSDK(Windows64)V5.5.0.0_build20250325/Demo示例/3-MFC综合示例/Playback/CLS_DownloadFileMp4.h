
#ifndef __H_DOWNLOAD_MP4_H__
#define __H_DOWNLOAD_MP4_H__

#include "../Common/CommonFun.h"
#include "../Include/NVSSDK_INTERFACE.h"
#include "../Include/MP4_INTERFACE.h"

using namespace NVSSDK_INTERFACE;
using namespace MP4_INTERFACE;

#define TEMP_SRC_FILE_EXTENSION		".tmp"

#define WM_DOWNLOAD_FINSH_MSG          (WM_USER + 0x3021)

enum FILE_DOWNLOAD_STATUS
{
	n_DLStatus_Reserved = -1,	//unknown state
	n_DLStatus_wait     =  0,	//ready to download
	n_DLStatus_Doing,			//downloading
	n_DLStatus_Pause,			//pause download
	n_DLStatus_DL_Complete,		//download successful
	n_DLStatus_DL_Fail,			//download failed
	n_DLStatus_Converting,		// is converting
	n_DLStatus_Convert_Complete,//converted successfully
	n_DLStatus_Convert_Fail,	//Conversion failed
	n_DLStatus_Finish			//Completed
};

enum SDV_CONVERT_STATUS
{
	CONVERT_STATUS_READY = 0,	// prepare to convert
	CONVERT_STATUS_SUCCESS,		//converted successfully
	CONVERT_STATUS_FAILED		//Conversion failed
};

#define DOWN_LOAD_SPEED_PAUSE			0
#define DOWN_LOAD_SPEED_START			16
#define DOWN_LOAD_SPEED_32				32
#define DOWN_LOAD_POS_RESERVE			-1

#define RATIO_BYTE_CONVERT				1024

class CLS_DownloadFileMp4
{
public:
	CLS_DownloadFileMp4(HWND _pParent, int _iLogonId, DOWNLOAD_FILE _tDownLoadInfo, BOOL _blBreakContinue = FALSE);
	~CLS_DownloadFileMp4();

private:
	int				m_iLogonId;					//Login ID
	DOWNLOAD_FILE	m_tDownLoadInfo;			// download information
	int				m_iConnectId;				//Connection ID
	int				m_iDownLoadStatus;			//download status
	BOOL			m_blBreakContinue;			//http
	int				m_iDownLoadPos;				// download progress
	BOOL			m_blKeepSdvFile;
	HWND			m_hParents;

public:
	static unsigned int __stdcall FileConvertThread(LPVOID _Param);

public:
	int	StartDownLoad(BOOL blRestart = FALSE);					//start download
	int StartBreakContineDownLoad();
	int StopDowmLoad(BOOL _blDownLoadSuccess = TRUE, BOOL _blKeepSdvFile = FALSE, BOOL _blConvertToMp4 = TRUE);	//stop download
	BOOL CheckDownLoadInfo(int _iLogonId, char* _pcRemoteFileName);	//Determine whether it is the current download
	BOOL CheckDownLoadInfo(int _iConnectId);						//Determine whether it is the current download
	int SetDownLoadSpeed(DOWNLOAD_CONTROL _tDownLoadControl);		//Control the download speed 0: Pause the download, 16: Start the download at the default download speed
	char* GetDownLoadProgress(char* _pcProgress, int _iLen);		//Get download progress
	BOOL CheckBreakContinue();
	int GetConnectId();		
	int GetLogonId();
	CString GetFileName();										//Get local file, full name
	int GetDownLoadStatus();									//Get download status
	int InnerDealConvertResult(int _iConvertSatus, BOOL _blKeepSdvFile);

};

#endif

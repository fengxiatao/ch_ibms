// RemoteByTimePage.cpp : implementation file
//

#include "stdafx.h"
#include <shlwapi.h>
#include "RemoteByTimePage.h"
#include "mmsystem.h"
#pragma comment(lib, "Winmm.lib")

#define PLAYBACK_FORWARD 0
#define PLAYBACK_REVERSE 1

CString g_Path = "C:\\netclientdemo_download\\";
// CLS_RemoteByTimePage dialog

int g_iTimeInterval = 0;

void PsDataCallBackFunction(unsigned int _ulID, unsigned char* _cData, int _iLen, int _iType, void* _pvUserData)
{
	if (NULL == _cData)
	{
		return;
	}

#ifdef _DEBUG
	CString cstrLog;
	int iTime = timeGetTime();
	cstrLog.Format("[RecvPsDataNotify ConnectId(%d) Time(%d) DataLen(%d)]\n", _ulID, iTime - g_iTimeInterval, _iLen);
	g_iTimeInterval = iTime;
	OutputDebugString(cstrLog);
#endif
}

IMPLEMENT_DYNAMIC(CLS_RemoteByTimePage, CDialog)

CLS_RemoteByTimePage::CLS_RemoteByTimePage(CWnd* pParent /*=NULL*/)
	:  CLS_BasePage(CLS_RemoteByTimePage::IDD, pParent)
{
	m_ulConnID = INVALID_ID;
	m_iLogonID = INVALID_ID;
	memset(&m_tDownloadTimeSpan,0x00,sizeof(m_tDownloadTimeSpan));

	m_iMaxVodNum = DEFAULT_PLAY_PAGE_NUM;
}

CLS_RemoteByTimePage::~CLS_RemoteByTimePage()
{
}

void CLS_RemoteByTimePage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_DATETIMEPICKER1, m_DTDownloadBeginTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER2, m_DTDownloadEndTime);
	DDX_Control(pDX, IDC_COMBO1, m_ComboChannelNo);
	DDX_Control(pDX, IDC_PROGRESS1, m_ProgressDownload);
	DDX_Control(pDX, IDC_COMBO_STREAMNO, m_cboStreamNo);
	DDX_Control(pDX, IDC_CBO_DOWNLOADTYPE, m_cboDownloadFileType);
	DDX_Control(pDX, IDC_CBO_DOWNLOAD_FILE_FLAG, m_cboDownloadFileFlag);
	DDX_Control(pDX, IDC_COMBO_TIME_REQ_MODE, m_cboTimeReqMode);
	DDX_Control(pDX, IDC_CBO_TIME_DOWNLOAD_SPEED, m_cboTimeDLSpeed);
	DDX_Control(pDX, IDC_CHECK_TIME_ONLY_I_FRAME, m_chkTimeIframe);
	DDX_Control(pDX, IDC_COMBO_DISK_BY_TIME, m_VCADiskGroupByTime);
}


BEGIN_MESSAGE_MAP(CLS_RemoteByTimePage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_PLAY_BY_TIME_DOWNLOAD, &CLS_RemoteByTimePage::OnBnClickedButtonPlayByTimeDownload)
	ON_WM_TIMER()
	ON_BN_CLICKED(IDC_BUTTON_PLAYBACK_BY_TIME_PLAY, &CLS_RemoteByTimePage::OnBnClickedButtonPlaybackByTimePlay)
	ON_BN_CLICKED(IDC_BUTTON_STOP, &CLS_RemoteByTimePage::OnBnClickedButtonStop)
	ON_BN_CLICKED(IDC_BTN_SUPER_VOD_BY_TIME, &CLS_RemoteByTimePage::OnBnClickedBtnSuperVodByTime)
	ON_MESSAGE(WM_PLAY_PAGE_DESTORY, &CLS_RemoteByTimePage::OnPlayPageDestory)
	ON_CBN_SELCHANGE(IDC_CBO_TIME_DOWNLOAD_SPEED, &CLS_RemoteByTimePage::OnCbnSelchangeCboTimeDownloadSpeed)
    ON_BN_CLICKED(IDC_BUTTON_PLAYBACK_BY_TIME_REVERSE, &CLS_RemoteByTimePage::OnBnClickedButtonPlaybackByTimeReverse)
	ON_BN_CLICKED(IDC_BUTTON_TIMESPAN_CONTINUE, &CLS_RemoteByTimePage::OnBnClickedButtonTimespanContinue)
	ON_BN_CLICKED(IDC_BUTTON_CHECK_VOD, &CLS_RemoteByTimePage::OnBnClickedButtonCheckVod)
	ON_CBN_SELCHANGE(IDC_COMBO_TIME_REQ_MODE, &CLS_RemoteByTimePage::OnCbnSelchangeComboTimeReqMode)
	ON_CBN_SELCHANGE(IDC_CBO_DOWNLOADTYPE, &CLS_RemoteByTimePage::OnCbnSelchangeCboDownloadtype)
	ON_BN_CLICKED(IDC_CHECK_TIME_ONLY_I_FRAME, &CLS_RemoteByTimePage::OnBnClickedCheckTimeOnlyIFrame)
END_MESSAGE_MAP()

void __stdcall testRAWFRAME_NOTIFY(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser)
{
	//
}
// CLS_RemoteByTimePage message handlers

void CLS_RemoteByTimePage::OnBnClickedButtonPlayByTimeDownload()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID == INVALID_ID || m_ulConnID != INVALID_ID)
	{
		return;
	}
	CTime BeginTime;
	CTime EndTime;
	CTime tempTime;
	m_DTDownloadBeginTime.GetTime(tempTime);
	m_timeBegin.iYear = tempTime.GetYear();
	m_timeBegin.iMonth = tempTime.GetMonth();
	m_timeBegin.iDay = tempTime.GetDay();
	m_timeBegin.iHour = tempTime.GetHour();
	m_timeBegin.iMinute = tempTime.GetMinute();
	m_timeBegin.iSecond = tempTime.GetSecond();
	BeginTime = tempTime;

	m_DTDownloadEndTime.GetTime(tempTime);
	m_timeEnd.iYear = tempTime.GetYear();
	m_timeEnd.iMonth = tempTime.GetMonth();
	m_timeEnd.iDay = tempTime.GetDay();
	m_timeEnd.iHour = tempTime.GetHour();
	m_timeEnd.iMinute = tempTime.GetMinute();
	m_timeEnd.iSecond = tempTime.GetSecond();
	EndTime = tempTime;

	if (BeginTime >= EndTime)
	{
		return;
	}

	CString cstrLocalFileName;
	int iDidkGrpNo = m_VCADiskGroupByTime.GetCurSel();
	cstrLocalFileName.Format("Demo_Download%d%d%d%d%d%d-%d%d%d%d%d%d-%d.sdv",
		m_timeBegin.iYear,
		m_timeBegin.iMonth,
		m_timeBegin.iDay,
		m_timeBegin.iHour,
		m_timeBegin.iMinute,
		m_timeBegin.iSecond,
		m_timeEnd.iYear,
		m_timeEnd.iMonth ,
		m_timeEnd.iDay ,
		m_timeEnd.iHour ,
		m_timeEnd.iMinute,
		m_timeEnd.iSecond,
		iDidkGrpNo);
	cstrLocalFileName = GetLocalSaveDirectory() + "\\" + cstrLocalFileName;

	int iChannelNo = m_ComboChannelNo.GetCurSel();
	int iStreamNo = m_cboStreamNo.GetCurSel();

	DOWNLOAD_TIMESPAN tDownloadTimeSpan = {0};
	tDownloadTimeSpan.m_iSize = sizeof(DOWNLOAD_TIMESPAN);

	tDownloadTimeSpan.m_iSaveFileType = m_cboDownloadFileType.GetItemData(m_cboDownloadFileType.GetCurSel());
	tDownloadTimeSpan.m_iFileFlag = m_cboDownloadFileFlag.GetItemData(m_cboDownloadFileFlag.GetCurSel());
	if (DOWNLOAD_FILE_TYPE_ZFMP4 == tDownloadTimeSpan.m_iSaveFileType)
	{
		PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".mp4"));
		cstrLocalFileName.ReleaseBuffer();
	}
	else if (DOWNLOAD_FILE_TYPE_MP4 == tDownloadTimeSpan.m_iSaveFileType || DOWNLOAD_FILE_TYPE_PS == tDownloadTimeSpan.m_iSaveFileType)
	{
		PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".ps"));
		cstrLocalFileName.ReleaseBuffer();
	}else if (DOWNLOAD_FILE_TYPE_AVI == tDownloadTimeSpan.m_iSaveFileType)
	{
		PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".avi"));
		cstrLocalFileName.ReleaseBuffer();
	}
    else if (DOWNLOAD_FILE_TYPE_TS == tDownloadTimeSpan.m_iSaveFileType)
    {
        PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".ts"));
        cstrLocalFileName.ReleaseBuffer();
    }
	else if (DOWNLOAD_FILE_TYPE_SDV == tDownloadTimeSpan.m_iSaveFileType)
	{
	}
	else{
		AddLog(LOG_TYPE_FAIL, "",  "Not have this download type.");
	}

	int iCpyLen =  min(cstrLocalFileName.GetLength() + 1, 255);

	strcpy_s(tDownloadTimeSpan.m_cLocalFilename, iCpyLen, cstrLocalFileName.GetBuffer());
	cstrLocalFileName.ReleaseBuffer();
	tDownloadTimeSpan.m_iChannelNO = iChannelNo;
	tDownloadTimeSpan.m_iStreamNo = iStreamNo;

	tDownloadTimeSpan.m_tTimeBegin = m_timeBegin;
	tDownloadTimeSpan.m_tTimeEnd = m_timeEnd;
	tDownloadTimeSpan.m_iPosition = -1;
	tDownloadTimeSpan.m_iSpeed = m_cboTimeDLSpeed.GetItemData(m_cboTimeDLSpeed.GetCurSel());
	tDownloadTimeSpan.m_iIFrame = m_chkTimeIframe.GetCheck();
	tDownloadTimeSpan.m_iReqMode = m_cboTimeReqMode.GetCurSel();	//1:down frame mode,0= Flow pattern; if (mode == 0) Device do not send download time !
	tDownloadTimeSpan.m_iDiskGroup = iDidkGrpNo;
	//start dowmload
	tDownloadTimeSpan.m_iBitRateFlag = ((CButton*)GetDlgItem(IDC_CHECK_COMPRESS))->GetCheck();
	int iRet = NetClient_NetFileDownload((unsigned int*)&m_ulConnID, m_iLogonID, DOWNLOAD_CMD_TIMESPAN,&tDownloadTimeSpan,sizeof(DOWNLOAD_TIMESPAN));
 	if (iRet >= 0)
 	{
		//set dowmlload speed
		DOWNLOAD_CONTROL tControl = {sizeof(DOWNLOAD_CONTROL)};
		tControl.m_iPosition = -1;
		tControl.m_iSpeed = 16;
		tControl.m_iIFrame = m_chkTimeIframe.GetCheck();
		tControl.m_iReqMode = m_cboTimeReqMode.GetCurSel();

		PDEVICE_INFO pDeviceInfo = FindDevice(m_iLogonID);
		if (pDeviceInfo)
		{
			//单通道IPC的主动模式，下载之后不要立即调用调速接口，此时主动模式的数据通道连接有可能还没建立起来
			if (SERVER_ACTIVE != pDeviceInfo->iServerType && SERVER_REG_ACTIVE != pDeviceInfo->iServerType)
			{
				NetClient_NetFileDownload((unsigned int*)&m_ulConnID, m_iLogonID, DOWNLOAD_CMD_CONTROL, &tControl, sizeof(DOWNLOAD_CONTROL));
			}
		}
 	}
	m_ProgressDownload.SetPos(0);

    //Standard data (PS/TS) callback
    if (DOWNLOAD_FILE_TYPE_TS == tDownloadTimeSpan.m_iSaveFileType && NULL != NetClient_SetDataPackCallBack)
    {
        //If you don't need to write a file, you only need to call back, you can pass the local file name empty
        NetClient_SetDataPackCallBack(m_ulConnID, DTYPE_TS, (void*)&PsDataCallBackFunction, NULL);
    }
    else if (DOWNLOAD_FILE_TYPE_PS == tDownloadTimeSpan.m_iSaveFileType && NULL != NetClient_SetDataPackCallBack)
    {
        //If you don't need to write a file, you only need to call back, you can pass the local file name empty
        NetClient_SetDataPackCallBack(m_ulConnID, DTYPE_PS, (void*)&PsDataCallBackFunction, NULL);
    }

	SetDlgItemTextEx(IDC_STATIC_PBK_DOWNLOAD_BY_TIME_STATUS, IDS_PLAYBACK_DOWNLOAD_STATUS);

	memcpy(&m_tDownloadTimeSpan,&tDownloadTimeSpan,sizeof(DOWNLOAD_TIMESPAN));
	SetDlgItemText(IDC_STATIC_PBK_CHECKVOD_BY_TIME_STATUS,"");
}

BOOL CLS_RemoteByTimePage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	m_DTDownloadBeginTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_DTDownloadEndTime.SetFormat("yyyy-MM-dd HH:mm:ss");

	CTime SystemTime; 
	m_DTDownloadBeginTime.GetTime(SystemTime);
	CTime BeginTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 0, 0, 0);
	m_DTDownloadBeginTime.SetTime(&BeginTime);
	CTime EndTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 23, 59, 0);
	m_DTDownloadEndTime.SetTime(&EndTime);

	m_ProgressDownload.SetRange(1, 100);
	m_ProgressDownload.SetPos(1);
	memset(&m_timeBegin, 0, sizeof(m_timeBegin));
	memset(&m_timeEnd, 0, sizeof(m_timeEnd));
	UI_UpdateText();
	SetTimer(TIMER_DOWNLOAD_BY_TIME, 1000, NULL);
	m_cboTimeReqMode.SetCurSel(1);
	m_cboTimeDLSpeed.SetCurSel(5);
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

BOOL CLS_RemoteByTimePage::DestroyWindow()
{
	// TODO: Add your specialized code here and/or call the base class

	return CLS_BasePage::DestroyWindow();
}

void CLS_RemoteByTimePage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (_iLogonID < 0 || m_ulConnID != INVALID_ID)
	{
		return;
	}
	m_iLogonID = _iLogonID;	
	Update_UI_IPAndID();

	//更新是否压缩按钮状态
	Update_UI_CompressButtonState();

	UpdateMaxVodNum();
}

void CLS_RemoteByTimePage::ProccessDownloadMsg( int _iResourceID )
{
	NetClient_NetFileStopDownloadFile(m_ulConnID);
	m_ulConnID = INVALID_ID;
	SetDlgItemTextEx(IDC_STATIC_PBK_DOWNLOAD_BY_TIME_STATUS, _iResourceID);
}
void CLS_RemoteByTimePage::OnMainNotify( int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser )
{
	if (m_iLogonID < 0)
	{
		return;
	}

	int iMsgType = LOWORD(_iWparam);
	int iErrType = HIWORD(_iWparam);
	switch(iMsgType)
	{

	case WCM_DWONLOAD_FINISHED:
		if (m_ulConnID == (unsigned long)_iLParam)
		{
			m_ProgressDownload.SetPos(100);
			ProccessDownloadMsg(IDS_PLAYBACK_DOWNLOAD_FINISH);	
		}
		break;
	case WCM_DWONLOAD_FAULT:
		if (m_ulConnID == (unsigned long)_iLParam)
		{
			ProccessDownloadMsg(IDS_PLAYBACK_DOWNLOAD_FAULT);
		}
		break;
	case WCM_ERR_ORDER: 
		{
			if (m_iLogonID == _ulLogonID)
			{
				ProccessDownloadMsg(IDS_PLAYBACK_BREAK_OFF);
			}
		}
		break;
	case WCM_DOWNLOAD_INTERRUPT:
		if (m_ulConnID == (unsigned long)_iLParam)
		{
			ProccessDownloadMsg(IDS_PLAYBACK_DOWNLOAD_INTERRUPT);
		}
		break;
	case WCM_LOGON_NOTIFY:
		{
			if (m_iLogonID == _ulLogonID && LOGON_SUCCESS == (int)_iLParam)
			{
				ProccessDownloadMsg(IDS_PLAYBACK_DOWNLOAD_STATUS);
			}
		}
		break;
	case WCM_CHECKVOD_PROGRESS:
		{
			if(NULL != _iLParam)
			{
				CheckVodFileResponse *ptInfo = (CheckVodFileResponse *)_iLParam;

				if (ptInfo->iChannelNo == m_tDownloadTimeSpan.m_iChannelNO && ptInfo->iStreamNo == m_tDownloadTimeSpan.m_iStreamNo
					&&ptInfo->tTimeBegin.iYear == ptInfo->tTimeBegin.iYear&&ptInfo->tTimeBegin.iMonth == ptInfo->tTimeBegin.iMonth
					&&ptInfo->tTimeBegin.iDay == ptInfo->tTimeBegin.iDay&&ptInfo->tTimeBegin.iHour == ptInfo->tTimeBegin.iHour
					&&ptInfo->tTimeBegin.iMinute == ptInfo->tTimeBegin.iMinute&&ptInfo->tTimeBegin.iSecond == ptInfo->tTimeBegin.iSecond
					&&ptInfo->tTimeEnd.iYear == ptInfo->tTimeEnd.iYear&&ptInfo->tTimeEnd.iMonth == ptInfo->tTimeEnd.iMonth
					&&ptInfo->tTimeEnd.iDay == ptInfo->tTimeEnd.iDay&&ptInfo->tTimeEnd.iHour == ptInfo->tTimeEnd.iHour
					&&ptInfo->tTimeEnd.iMinute == ptInfo->tTimeEnd.iMinute&&ptInfo->tTimeEnd.iSecond == ptInfo->tTimeEnd.iSecond)
				{
					if(0 == ptInfo->iResult)
					{
						CString str;
						str.Format("Check progress %d%%",ptInfo->iProgress);
						SetDlgItemText(IDC_STATIC_PBK_CHECKVOD_BY_TIME_STATUS,str);
					}
					else
					{
						OutPutCheckResult(ptInfo->iResult);
					}

				}
			}
		}
	case WCM_DOWNLOAD_COMMON_MSG:
		{
			int iSourceId[] = {IDS_STORAGE_PROGRESS_REPORT, 
				IDS_STRORAGE_CONNECT_COUNT_LIMIT, 
				IDS_STORAGE_NO_MATCH_FILE, 
				IDS_STORAGE_TRANSFER_FAILED, 
				IDS_STORAGE_LOCALTION_FAILED, 
				IDS_STORAGE_TRANSCODING_FAILED, 
				IDS_STORAGE_BANDWIDTH_INSUFFICIENT, 
				IDS_USER_Message33, 
				IDS_STORAGE_PARAMETER_ERR, 
				IDS_STORAGE_OPEN_VIDEOFILE_FAILED, 
				IDS_STORAGE_ILLEGAL_DATA_FRAME, 
				IDS_STORAGE_PERFORMANCE_LIMIT, 
				IDS_STORAGE_MODULE_IS_STARTING};
			int iCount = (sizeof(iSourceId) / sizeof(iSourceId[0]));
			//0, 正常进度上报；1，已达最大连接数； 2，没有找到符合条件的文件；3，VOD传送失败； 4，定位失败；5，转码资源不足；6，带宽不足；7，用户权限不足；8，参数错误； 9，打开录像文件失败；10，非法数据帧。11， 视频压缩失败:性能达到上限。12，视频压缩失败：压缩模块启动中。
			if (0 <= iErrType && iErrType < iCount)
			{
				ProccessDownloadMsg(iSourceId[iErrType]);
			}
			else
			{
				AddLog(LOG_TYPE_FAIL, "",  "CMD:WCM_DOWNLOAD_COMMON_MSG report unknow error, iErrType = %d.", iErrType);
			}
		}
		break;
	default:
		break;
	}
	for (list <CLS_FilePlayReviewPage *>::iterator it = m_lstPlayPage.begin(); it != m_lstPlayPage.end(); it++)
	{
		CLS_FilePlayReviewPage * pclsPlayPage = *it;
		if (NULL == pclsPlayPage)
		{
			continue;
		}

		if (NULL == pclsPlayPage->GetSafeHwnd() || !IsWindow(pclsPlayPage->GetSafeHwnd()))
		{
			continue;
		}
		pclsPlayPage->OnMainNotify(_ulLogonID, _iWparam, _iLParam, _iUser);
	}	
}

void CLS_RemoteByTimePage::OnLanguageChanged( int )
{
	UI_UpdateText();
}

LRESULT CLS_RemoteByTimePage::OnPlayPageDestory( WPARAM wParam, LPARAM lParam )
{
	int iSize = m_lstPlayPage.size();
	for (list <CLS_FilePlayReviewPage *>::iterator it = m_lstPlayPage.begin(); it != m_lstPlayPage.end();)
	{
		CLS_FilePlayReviewPage * pclsPlayPage = *it;
		if (NULL == pclsPlayPage)
		{
			it = m_lstPlayPage.erase(it);
			continue;
		}

		if (NULL == pclsPlayPage->GetSafeHwnd() || !IsWindow(pclsPlayPage->GetSafeHwnd()))
		{
			it = m_lstPlayPage.erase(it);
			delete pclsPlayPage;
			pclsPlayPage = NULL;
			continue;
		}

		++it;
	}

	return RET_SUCCESS;
}

void CLS_RemoteByTimePage::Update_UI_IPAndID()
{
	PDEVICE_INFO DeviceInfo = FindDevice(m_iLogonID);
	if (DeviceInfo)
	{
		CString strDeviceIP = GetTextEx(IDS_PLAYBACK_DEVICE_IP) + ":" + DeviceInfo->cIP;
		CString strDeviceID = GetTextEx(IDS_PLAYBACK_DEVICE_ID) + ":" + DeviceInfo->cID;
		SetDlgItemText(IDC_STATIC_PLAYBACK_BY_TIME_DEVICE_IP ,strDeviceIP);
		SetDlgItemText(IDC_STATIC_PLAYBACK_BY_TIME_DEVICE_ID ,strDeviceID);
		int iChannelNum = 0;
		NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
		m_ComboChannelNo.ResetContent();
		for (int i = 0; i < iChannelNum; i++)
		{
			CString str;
			str.Format("%d", i);
			m_ComboChannelNo.AddString(str);
		}
		m_ComboChannelNo.SetCurSel(0);
		m_cboStreamNo.SetCurSel(0);
	}
}
void CLS_RemoteByTimePage::OnTimer(UINT_PTR nIDEvent)
{
	// TODO: Add your message handler code here and/or call default
	switch(nIDEvent)
	{
	case TIMER_DOWNLOAD_BY_TIME:
		QueryDownloadProgress();
		break;
	default:
		return;

	}
	CLS_BasePage::OnTimer(nIDEvent);
}

void CLS_RemoteByTimePage::QueryDownloadProgress()
{
	if (m_ulConnID != INVALID_ID)
	{
		int uiCurrentTime = 0;
		int iSize = 0;
		if (NetClient_NetFileGetDownloadPos(m_ulConnID, &uiCurrentTime, &iSize) >= 0)
		{
			//如果进度小于100说明发的是进度
			if (uiCurrentTime > 100)
			{
				unsigned int uiBeginTime = NvsFileTimeToAbsSeconds(&m_timeBegin);
				unsigned int uiEndTime = NvsFileTimeToAbsSeconds(&m_timeEnd);
				int iTimeInterval = uiEndTime - uiBeginTime;
				if (iTimeInterval > 0)
				{
					int iCurrentInterval = uiCurrentTime - uiBeginTime;
					if (iCurrentInterval < iTimeInterval)
					{

						int iProcess = iCurrentInterval*100/iTimeInterval;
						if (iProcess > 100)
						{
							return;
						}
						m_ProgressDownload.SetPos(iProcess);
					}

				}
				NVS_FILE_TIME CurrentTime = {0};
				AbsSecondsToNvsFileTime(&CurrentTime, uiCurrentTime);
				CString szTime;
				szTime.Format("%04d-%02d-%02d %02d:%02d:%02d", CurrentTime.iYear, CurrentTime.iMonth, CurrentTime.iDay,
					CurrentTime.iHour, CurrentTime.iMinute, CurrentTime.iSecond);
				SetDlgItemText(IDC_STATIC_PBK_DOWNLOAD_BY_TIME_STATUS, szTime);
			}
			else
			{
				m_ProgressDownload.SetPos(uiCurrentTime);
			}
		}
	}
}

void CLS_RemoteByTimePage::PlaybackByTime(int iDirection)
{
    if (m_iLogonID == INVALID_ID)
    {
        return;
    }

    NVS_FILE_TIME begintime = {0};
    NVS_FILE_TIME endtime = {0};

    CTime tempTime,timeBegin, timeEnd;
    m_DTDownloadBeginTime.GetTime(tempTime);
    timeBegin= tempTime;
    begintime.iYear = tempTime.GetYear();
    begintime.iMonth = tempTime.GetMonth();
    begintime.iDay = tempTime.GetDay();
    begintime.iHour = tempTime.GetHour();
    begintime.iMinute = tempTime.GetMinute();
    begintime.iSecond = tempTime.GetSecond();

    m_DTDownloadEndTime.GetTime(tempTime);
    timeEnd = tempTime;
    endtime.iYear = tempTime.GetYear();
    endtime.iMonth = tempTime.GetMonth();
    endtime.iDay = tempTime.GetDay();
    endtime.iHour = tempTime.GetHour();
    endtime.iMinute = tempTime.GetMinute();
    endtime.iSecond = tempTime.GetSecond();

    if (timeBegin >= timeEnd)
    {
        return;
    }

    int iChannelNo = m_ComboChannelNo.GetCurSel();
    int iStreamNo = m_cboStreamNo.GetCurSel();
    int iChannelNum = 0;
    NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
    int iRealChannel = iStreamNo*iChannelNum+iChannelNo;

    int iCurDevPlayPageNum = 0;
    for (list <CLS_FilePlayReviewPage *>::iterator it = m_lstPlayPage.begin(); it != m_lstPlayPage.end(); it++)
    {
        CLS_FilePlayReviewPage * pclsPlayPage = *it;
        if (NULL == pclsPlayPage || NULL == pclsPlayPage->GetSafeHwnd() || !IsWindow(pclsPlayPage->GetSafeHwnd()))
        {
            continue;
        }

        if (pclsPlayPage->GetLogonID() == m_iLogonID)
        {
            iCurDevPlayPageNum++;
            continue;
        }
    }

    if (m_iMaxVodNum <= iCurDevPlayPageNum)
    {
        AddLog(LOG_TYPE_MSG, "", "Added too much file with same device to playback list!");
        return;
    }

    CLS_FilePlayReviewPage* pclsPlayReviewPage = new CLS_FilePlayReviewPage(this);
    if (NULL != pclsPlayReviewPage)
    {
        pclsPlayReviewPage->SetPlaybackDirection(iDirection);
		pclsPlayReviewPage->SetBitRateFlag(((CButton*)GetDlgItem(IDC_CHECK_COMPRESS))->GetCheck());
		pclsPlayReviewPage->SetIframeFlag(m_chkTimeIframe.GetCheck());
		pclsPlayReviewPage->SetDiskGroupFlag(m_VCADiskGroupByTime.GetCurSel());
        pclsPlayReviewPage->SetDownloadParam(m_iLogonID, iRealChannel/*iChannelNo*/, &begintime, &endtime);
        pclsPlayReviewPage->Create(IDD_DLG_PBK_REVIEW, this);
        pclsPlayReviewPage->ShowWindow(SW_SHOW);
    }

    m_lstPlayPage.push_back(pclsPlayReviewPage);
}

void CLS_RemoteByTimePage::OnBnClickedButtonPlaybackByTimePlay()
{
	PlaybackByTime(PLAYBACK_FORWARD);
}

void CLS_RemoteByTimePage::UI_UpdateText()
{
	SetDlgItemTextEx(IDC_STATIC_PLAYBACK_BY_TIME_DEVICE_IP, IDS_PLAYBACK_DEVICE_IP);
	SetDlgItemTextEx(IDC_STATIC_PLAYBACK_BY_TIME_DEVICE_ID, IDS_PLAYBACK_DEVICE_ID);
	SetDlgItemTextEx(IDC_STATIC_DOWNLOAD_AND_PLAY, IDS_PLAYBACK_DOWNLOAD_AND_PLAY);
	SetDlgItemTextEx(IDC_BUTTON_PLAY_BY_TIME_DOWNLOAD, IDS_PLAYBACK_DOWNLOAD);
	SetDlgItemTextEx(IDC_BUTTON_PLAYBACK_BY_TIME_PLAY, IDS_PLAYBACK_PLAY);
	SetDlgItemTextEx(IDC_STATIC_DOWNLOAD_BY_TIME_BEGINTIME, IDS_PLAYBACK_DOWNLOAD_BEGINTIME);
	SetDlgItemTextEx(IDC_STATIC_DOWNLOAD_BY_TIME_ENDTIME, IDS_PLAYBACK_DOWNLOAD_ENDTIME);
	SetDlgItemTextEx(IDC_STATIC_DOWNLOAD_BY_TIME_CHANNELNO, IDS_PLAYBACK_DOWNLOAD_CHANNELNO);
	SetDlgItemTextEx(IDC_STATIC_PBK_DOWNLOAD_BY_TIME_STATUS, IDS_PLAYBACK_DOWNLOAD_STATUS);
	SetDlgItemTextEx(IDC_BUTTON_STOP, IDS_CONFIG_DONWLOAD_STOP);
	SetDlgItemTextEx(IDC_STATIC_DOWNLOAD_BY_TIME_STREAMNO, IDS_CONFIG_ADV_STREAMTYPE);
	SetDlgItemText(IDC_BUTTON_PLAYBACK_BY_TIME_REVERSE, GetTextByLan("退播", "Reverse"));
	SetDlgItemText(IDC_BUTTON_TIMESPAN_CONTINUE, GetTextByLan("续传", "Continuation"));
	SetDlgItemText(IDC_BUTTON_CHECK_VOD, GetTextByLan("校验", "ChekVod"));
	InsertString(m_cboStreamNo,0,GetTextEx(IDS_MAJOR));
	InsertString(m_cboStreamNo,1,GetTextEx(IDS_MINOR));

	int iIndex = m_cboDownloadFileType.GetCurSel();
	m_cboDownloadFileType.ResetContent();
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("SDV")), DOWNLOAD_FILE_TYPE_SDV);
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("PS")), DOWNLOAD_FILE_TYPE_PS);
    m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("TS")), DOWNLOAD_FILE_TYPE_TS);
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("MP4")), DOWNLOAD_FILE_TYPE_ZFMP4);
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("AVI")), DOWNLOAD_FILE_TYPE_AVI);
	iIndex = (iIndex >= m_cboDownloadFileType.GetCount() || iIndex < 0) ? 0 : iIndex;
	m_cboDownloadFileType.SetCurSel(iIndex);

	iIndex = m_cboDownloadFileFlag.GetCurSel();
	m_cboDownloadFileFlag.ResetContent();
	m_cboDownloadFileFlag.SetItemData(m_cboDownloadFileFlag.AddString(GetTextEx(IDS_MULTI_FILE)), DOWNLOAD_FILE_FLAG_MULTI);
	m_cboDownloadFileFlag.SetItemData(m_cboDownloadFileFlag.AddString(GetTextEx(IDS_SINGLE_FILE)), DOWNLOAD_FILE_FLAG_SINGLE);
	iIndex = (iIndex >= m_cboDownloadFileFlag.GetCount() || iIndex < 0) ? 0 : iIndex;
	m_cboDownloadFileFlag.SetCurSel(iIndex);

	iIndex = 0;
	InsertString( m_cboTimeReqMode, iIndex++, IDS_PBK_REQ_MODE_STREAM);
	InsertString( m_cboTimeReqMode, iIndex++, IDS_PBK_REQ_MODE_FRAME);
	InsertString( m_cboTimeReqMode, iIndex++, GetTextByLan("流模式V2","Stream Mode v2"));

	m_cboTimeDLSpeed.ResetContent();
	m_cboTimeDLSpeed.SetItemData(m_cboTimeDLSpeed.AddString(_T("0")), 0);
	m_cboTimeDLSpeed.SetItemData(m_cboTimeDLSpeed.AddString(_T("1")), 1);
	m_cboTimeDLSpeed.SetItemData(m_cboTimeDLSpeed.AddString(_T("2")), 2);
	m_cboTimeDLSpeed.SetItemData(m_cboTimeDLSpeed.AddString(_T("4")), 4);
	m_cboTimeDLSpeed.SetItemData(m_cboTimeDLSpeed.AddString(_T("8")), 8);
	m_cboTimeDLSpeed.SetItemData(m_cboTimeDLSpeed.AddString(_T("16")), 16);
	m_cboTimeDLSpeed.SetItemData(m_cboTimeDLSpeed.AddString(_T("32")), 32);

	SetDlgItemTextEx(IDC_CHECK_COMPRESS, IDS_TEXT_COMPRESS_OR_NOT);
	SetDlgItemText(IDC_CHECK_TIME_ONLY_I_FRAME, GetTextByLan("I 帧", "I Frame"));
	SetDlgItemText(IDC_STATIC_DISK_BY_TIME, GetTextByLan("盘组", "Disk Group"));
	m_VCADiskGroupByTime.ResetContent();
	m_VCADiskGroupByTime.AddString(GetTextEx(IDS_STRING_NOT_SPECIFY_DISK));
	for (int i = 1; i< MAX_DISK_GROUP_NUM_FOR_PLAYBACK; ++i)
	{
		CString strNo;
		strNo.Format("%d",i);
		m_VCADiskGroupByTime.AddString(strNo);
	}
	m_VCADiskGroupByTime.SetCurSel(0);	
}
void CLS_RemoteByTimePage::OnBnClickedButtonStop()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID != INVALID_ID && m_ulConnID != INVALID_ID)
	{
		NetClient_NetFileStopDownloadFile(m_ulConnID);
		m_ProgressDownload.SetPos(0);
		m_ulConnID = INVALID_ID;
		SetDlgItemTextEx(IDC_STATIC_PBK_DOWNLOAD_BY_TIME_STATUS, IDS_PLAYBACK_DOWNLOAD_STATUS);
	}
}

void CLS_RemoteByTimePage::OnLogoffDevice( int _iLogonID )
{
	if (_iLogonID == m_iLogonID)
	{
		ProccessDownloadMsg(IDS_PLAYBACK_DOWNLOAD_STATUS);
	}
}

void CLS_RemoteByTimePage::OnBnClickedBtnSuperVodByTime()
{
	if (m_iLogonID == INVALID_ID)
	{
		return;
	}

	NVS_FILE_TIME begintime = {0};
	NVS_FILE_TIME endtime = {0};

	CTime tempTime,timeBegin, timeEnd;
	m_DTDownloadBeginTime.GetTime(tempTime);
	timeBegin= tempTime;
	begintime.iYear = tempTime.GetYear();
	begintime.iMonth = tempTime.GetMonth();
	begintime.iDay = tempTime.GetDay();
	begintime.iHour = tempTime.GetHour();
	begintime.iMinute = tempTime.GetMinute();
	begintime.iSecond = tempTime.GetSecond();

	m_DTDownloadEndTime.GetTime(tempTime);
	timeEnd = tempTime;
	endtime.iYear = tempTime.GetYear();
	endtime.iMonth = tempTime.GetMonth();
	endtime.iDay = tempTime.GetDay();
	endtime.iHour = tempTime.GetHour();
	endtime.iMinute = tempTime.GetMinute();
	endtime.iSecond = tempTime.GetSecond();

	if (timeBegin >= timeEnd)
	{
		return;
	}

	int iChannelNo = m_ComboChannelNo.GetCurSel();
	int iStreamNo = m_cboStreamNo.GetCurSel();
	int iChannelNum = 0;
	NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	int iRealChannel = iStreamNo*iChannelNum+iChannelNo;

	if (m_iMaxVodNum <= m_lstPlayPage.size())
	{
		AddLog(LOG_TYPE_MSG, "", "Added too much file to playback list!");
		return;
	}

	CLS_FilePlayReviewPage* pclsPlayReviewPage = new CLS_FilePlayReviewPage(this);
	if (NULL != pclsPlayReviewPage)
	{
		pclsPlayReviewPage->SetDownloadParamEx(m_iLogonID, iRealChannel, &begintime, &endtime);
		pclsPlayReviewPage->Create(IDD_DLG_PBK_REVIEW, this);
		pclsPlayReviewPage->ShowWindow(SW_SHOW);
	}

	m_lstPlayPage.push_back(pclsPlayReviewPage);
}

void CLS_RemoteByTimePage::OnCbnSelchangeCboTimeDownloadSpeed()
{
	if (m_iLogonID != INVALID_ID && m_ulConnID != INVALID_ID)
	{
		DOWNLOAD_CONTROL tdc = {sizeof(DOWNLOAD_CONTROL)};
		tdc.m_iPosition = -1;
		tdc.m_iSpeed = m_cboTimeDLSpeed.GetItemData(m_cboTimeDLSpeed.GetCurSel());
		tdc.m_iIFrame = m_chkTimeIframe.GetCheck();
		tdc.m_iReqMode = m_cboTimeReqMode.GetCurSel();
		int iRet = NetClient_NetFileDownload((unsigned int*)&m_ulConnID, m_iLogonID, DOWNLOAD_CMD_CONTROL, &tdc, sizeof(DOWNLOAD_CONTROL));
	}
}

void CLS_RemoteByTimePage::OnBnClickedButtonPlaybackByTimeReverse()
{
    PlaybackByTime(PLAYBACK_REVERSE);
}

void CLS_RemoteByTimePage::OnBnClickedButtonTimespanContinue()
{

	if (m_ulConnID != INVALID_ID)
	{
		AddLog(LOG_TYPE_FAIL, "", "Timespan download continue faild ,vod is downloading filename:%s",\
			m_tDownloadTimeSpan.m_cLocalFilename);
		return;
	}

	//打开文件，计算文件大小
	FILE*fp = fopen(m_tDownloadTimeSpan.m_cLocalFilename,"rb");
	if(NULL == fp)
	{
		AddLog(LOG_TYPE_FAIL, "", "Timespan download continue faild open file faild err = %d filename:%s",\
			GetLastError(),m_tDownloadTimeSpan.m_cLocalFilename);
		return ;
	}

	_fseeki64(fp,0,SEEK_END);
	__int64 iFileSize = _ftelli64(fp);
	fclose(fp);
	fp = NULL;
	//发给设备继续下载
	m_tDownloadTimeSpan.m_llPosition = iFileSize;
	int iRet = NetClient_NetFileDownload((unsigned int*)&m_ulConnID,m_iLogonID, 
		DOWNLOAD_CMD_TIMESPAN_CONTINUE,&m_tDownloadTimeSpan,sizeof(m_tDownloadTimeSpan));
	if (iRet >= 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Timespan download continue success");
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "Timespan download continue faild");
	}
}

void CLS_RemoteByTimePage::OutPutCheckResult(int _iResult)
{
	switch (_iResult)
	{
	case 0:
		AddLog(LOG_TYPE_MSG, "", "File VOD Checking...");
		break;
	case 1:
		AddLog(LOG_TYPE_MSG, "", "File VOD Check Result success");
		break;
	case -1:
		AddLog(LOG_TYPE_MSG, "", "File VOD Check Result File Size Error");
		break;
	case -2:
		AddLog(LOG_TYPE_MSG, "", "File VOD Check Result VerifyCode Error");
		break;
	case -3:
		AddLog(LOG_TYPE_MSG, "", "File VOD Check Result Device is busy");
		break;
	case -4:
		AddLog(LOG_TYPE_MSG, "", "File VOD Check Result Not Find file");
		break;
	case -5:
		AddLog(LOG_TYPE_MSG, "", "File VOD Check Result don't support this VerifyMode");
		break;
	default:
		break;
	}

	if(0 != _iResult)
	{
		SetDlgItemText(IDC_STATIC_PBK_CHECKVOD_BY_TIME_STATUS, "Check progress 100%");
	}
}


#include "md5.h"
int GetFileMd5ForVodCheck(char *_pcFileName,char *_pcOutputBuf, int _iLen,__int64 *_pi64FileSize,CLS_RemoteByTimePage *pDlg)
{
	int iRet = -1;
	if (NULL == _pcFileName || NULL == _pcOutputBuf || _iLen < LEN_32 || NULL == pDlg)
	{
		return iRet;
	}

	FILE*fp = fopen(_pcFileName,"rb");
	if(NULL == fp)
	{
		return iRet;
	}

	//bufszie 1024*1024 = 1M
	char *pBuf = new char[LEN_1024*LEN_1024];
	if(NULL == pBuf)
	{
		return iRet;
	}

	_fseeki64(fp,0,SEEK_END);
	*_pi64FileSize = _ftelli64(fp);

	__int64 i64FileSize = 0;

	_fseeki64(fp,0,SEEK_SET);
	MD5_CTX context;
	unsigned char digest[16] = {0};
	MD5Init (&context);
	while (!feof(fp))
	{
		size_t iRealSize = fread(pBuf,1,LEN_1024*LEN_1024,fp);
		if (iRealSize >= LEN_1024)
		{
			MD5Update (&context, (unsigned char *)pBuf, LEN_1024);
		}

		i64FileSize += iRealSize;

		if(*_pi64FileSize > 0)
		{
			CString str;
			str.Format("Calcu Md5 progress %.2f%%",i64FileSize / (double)*_pi64FileSize * 100);
			pDlg->SetDlgItemText(IDC_STATIC_PBK_CHECKVOD_BY_TIME_STATUS,str);
		}

	}
	if(NULL != pBuf)
	{
		delete []pBuf;
		pBuf = NULL;
	}

	_fseeki64(fp,0,SEEK_END);
	*_pi64FileSize = _ftelli64(fp);
	fclose(fp);
	fp = NULL;

	MD5Final (digest, &context);

	unsigned char digeststr[LEN_32+1] = {0};
	MDPrint (digest,(unsigned char *)digeststr);
	memcpy(_pcOutputBuf,digeststr,LEN_32);

	iRet = 0;
	return iRet;
}


DWORD WINAPI ThreadProc(LPVOID lpParameter)  
{  
	CLS_RemoteByTimePage *pDlg = (CLS_RemoteByTimePage*)lpParameter;  
	if(NULL != pDlg)
	{
		AddLog(LOG_TYPE_MSG, "", "Timespan download CheckVod is running Please Wait...");
		memset(pDlg->m_cDigest,0x00,LEN_32);
		pDlg->m_iFileSize = 0;
		int iRet = GetFileMd5ForVodCheck(pDlg->m_tDownloadTimeSpan.m_cLocalFilename,pDlg->m_cDigest,LEN_32,&pDlg->m_iFileSize,pDlg);
		if(iRet >= 0)
		{
			int iChannelNo = pDlg->m_ComboChannelNo.GetCurSel();
			int iStreamNo = pDlg->m_cboStreamNo.GetCurSel();
			int iChannelNum = 0;
			NetClient_GetChannelNum(pDlg->m_iLogonID, &iChannelNum);
			int iRealChannel = iStreamNo*iChannelNum+iChannelNo;

			CheckVodTimeSpan tVodFile = {0};
			tVodFile.iChannelNO = pDlg->m_tDownloadTimeSpan.m_iChannelNO;
			tVodFile.iStreamNo = pDlg->m_tDownloadTimeSpan.m_iStreamNo;
			tVodFile.iVerifyMode = 1;
			memcpy(tVodFile.cVerifyCode,pDlg->m_cDigest,LEN_32);
			tVodFile.tTimeBegin = pDlg->m_tDownloadTimeSpan.m_tTimeBegin;
			tVodFile.tTimeEnd = pDlg->m_tDownloadTimeSpan.m_tTimeEnd;
			tVodFile.ullFileSize = pDlg->m_iFileSize;

			CheckVodFileResponse tCheckVodFileResponse = {0};
			int iRetValue = NetClient_CmdConfig(pDlg->m_iLogonID, CMD_CHECK_VODTIMESPAN, iRealChannel, &tVodFile, \
				sizeof(tVodFile), &tCheckVodFileResponse, sizeof(tCheckVodFileResponse));
			if(RET_SUCCESS == iRetValue)
			{
				pDlg->OutPutCheckResult(tCheckVodFileResponse.iResult);
			}
		}
		else
		{
			AddLog(LOG_TYPE_FAIL, "", "Timespan download get File md5 faild");
		}
	}

	return true;  
}  


void CLS_RemoteByTimePage::OnBnClickedButtonCheckVod()
{
	SetDlgItemText(IDC_STATIC_PBK_CHECKVOD_BY_TIME_STATUS,"0%");

	//线程中执行
	DWORD dwID;  
	HANDLE hThread;  
	hThread = CreateThread(0,0,ThreadProc,this,0,&dwID); 
	//等待结束，会卡界面
	//WaitForSingleObject(hThread,INFINITE);
}

void CLS_RemoteByTimePage::OnCbnSelchangeComboTimeReqMode()
{
	UpdateBtnState();
}

void CLS_RemoteByTimePage::OnCbnSelchangeCboDownloadtype()
{
	UpdateBtnState();
}

void CLS_RemoteByTimePage::UpdateBtnState()
{
	int iFileType = m_cboDownloadFileType.GetCurSel();
	int iReqMode = m_cboTimeReqMode.GetCurSel();
	if(DOWNLOAD_FILE_TYPE_SDV == iFileType && 2 == iReqMode)
	{
		GetDlgItem(IDC_BUTTON_CHECK_VOD)->ShowWindow(TRUE);
		GetDlgItem(IDC_BUTTON_TIMESPAN_CONTINUE)->ShowWindow(TRUE);
	}
	else
	{
		GetDlgItem(IDC_BUTTON_CHECK_VOD)->ShowWindow(FALSE);
		GetDlgItem(IDC_BUTTON_TIMESPAN_CONTINUE)->ShowWindow(FALSE);
	}
	SetDlgItemText(IDC_STATIC_PBK_CHECKVOD_BY_TIME_STATUS,"");
}

void CLS_RemoteByTimePage::Update_UI_CompressButtonState()
{
	//获取是否支持视频压缩能力集
	FuncAbilityLevel tFunAbility = {0};
	tFunAbility.iSize = sizeof(tFunAbility);
	tFunAbility.iMainFuncType = 0x40;
	tFunAbility.iSubFuncType = 152;
	int iEnable = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, 0x7FFFFFFF, &tFunAbility, sizeof(tFunAbility), &iEnable);
	if (RET_SUCCESS != iRet || 0 != strcmp(tFunAbility.cParam, "1"))
	{
		GetDlgItem(IDC_CHECK_COMPRESS)->ShowWindow(SW_HIDE);
		AddLog(LOG_TYPE_MSG,"", "NetClient_GetDevConfig cmd:NET_CLIENT_GET_FUNC_ABILITY, MainFuncType = %d, SubFuncType = %d, cParam = %s.", 
			tFunAbility.iMainFuncType, tFunAbility.iSubFuncType, tFunAbility.cParam);
		return;
	}
	GetDlgItem(IDC_CHECK_COMPRESS)->ShowWindow(SW_SHOW);
}

void CLS_RemoteByTimePage::OnBnClickedCheckTimeOnlyIFrame()
{
	if (BST_CHECKED == m_chkTimeIframe.GetCheck()) {
		MessageBox(GetTextByLan("只请求I帧默认按8倍速回放，8倍速以下回放不需要选择I帧！", "Only request that I frames be played back at 8 times the speed by default. Playback below 8 times does not require selecting I frames!"), GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
}

void CLS_RemoteByTimePage::UpdateMaxVodNum()
{
	//根据能力级获取设备支持的最大vod路数，默认5路
	int iMaxVodNum = DEFAULT_PLAY_PAGE_NUM;
	int iRetBytes = 0;
	FuncAbilityLevel tFuncAbilityLevel = {0};
	tFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	tFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_NVR_SYSTEM;
	tFuncAbilityLevel.iSubFuncType = 84;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, 0, &tFuncAbilityLevel, sizeof(tFuncAbilityLevel), &iRetBytes);
	if (RET_SUCCESS == iRet) {
		iMaxVodNum = (int)strtoul(tFuncAbilityLevel.cParam, 0, 10);
	}

	if (iMaxVodNum <= 0) {
		iMaxVodNum = DEFAULT_PLAY_PAGE_NUM;
	}

	if (iMaxVodNum > MAX_PLAY_PAGE_NUM) {
		iMaxVodNum = MAX_PLAY_PAGE_NUM;
	}

	m_iMaxVodNum = iMaxVodNum;

	return;
}

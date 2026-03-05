// RemoteFileTab.cpp : implementation file
//

#include "stdafx.h"
#include "RemoteFilePage.h"
#include <shlwapi.h>
#include "mmsystem.h"
#include "DualUsersAuthDlg.h"
#pragma comment(lib, "Winmm.lib")

#define YEARCOUNT			0		//year detection value
#define MONTHMIN			1		//month minimum
#define MONTHMAX			12		//Maximum value of month
#define MOONCOLUMN			1		//Number of columns in the moonlight information list
#define MONTHDAYNUM			31		// maximum number of days per month

#define PLAYBACK_FORWARD 0
#define PLAYBACK_REVERSE 1

int _g_iTimeInterval = 0;

void _PsDataCallBackFunction(unsigned int _ulID, unsigned char* _cData, int _iLen, int _iType, void* _pvUserData)
{
	if (NULL == _cData)
	{
		return;
	}

#ifdef _DEBUG
	CString cstrLog;
	int iTime = timeGetTime();
	cstrLog.Format("[RecvPsDataNotify ConnectId(%d) Time(%d) DataLen(%d)]\n", _ulID, iTime - _g_iTimeInterval, _iLen);
	_g_iTimeInterval = iTime;
	OutputDebugString(cstrLog);
#endif
}

// CLS_RemoteFilePage dialog
IMPLEMENT_DYNAMIC(CLS_RemoteFilePage, CDialog)
CString g_szDownloadPath = "c:\\netclientdemo_download\\";
CLS_RemoteFilePage::CLS_RemoteFilePage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_RemoteFilePage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iStreamNo = 0;

	m_iTotalCount = 0;
	m_iCurrentCount = 0;
	m_iCurrentPage = 0;
	m_iTotalPage = 0;

	m_iMaxVodNum = DEFAULT_PLAY_PAGE_NUM;
}

CLS_RemoteFilePage::~CLS_RemoteFilePage()
{
}

void CLS_RemoteFilePage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_FILE_TYPE, m_ComboFileType);
	DDX_Control(pDX, IDC_COMBO_REC_TYPE, m_ComboRecType);
	DDX_Control(pDX, IDC_COMBO_Channel, m_ComboChannelNo);
	DDX_Control(pDX, IDC_DATETIMEPICKER_QUERY_BEGINTIME, m_DTQueryBeginTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER_QUERY_ENDTIME, m_DTQueryEndTime);
	DDX_Control(pDX, IDC_EDIT_OSD, m_EditOSD);
	DDX_Control(pDX, IDC_LIST_QUERY_FILE, m_ListQueryFile);
	DDX_Control(pDX, IDC_COMBO_QUERY_PAGE, m_ComboQueryPage);
	DDX_Control(pDX, IDC_COMBO_ALARM_TYPE, m_cboAlarmType);
	DDX_Control(pDX, IDC_COMBO_VCA_DETAIL_TYPE, m_VCAdetailType);
	DDX_Control(pDX, IDC_COMBO_DISK_NUMBER, m_VCADiskNo);
	DDX_Control(pDX, IDC_COMBO_DISK_GROUP, m_VCADiskGroup);
	DDX_Control(pDX, IDC_COMBO_ALARMPORT, m_cboAlarmPort);
	DDX_Control(pDX, IDC_COMBO_REQ_MODE, m_cboReqMode);
	DDX_Control(pDX, IDC_DATETIMEPICKER_PLAYBACK_RECORINGDAY, m_DTRecordingDate);
	DDX_Control(pDX, IDC_LST_PLAYBACK_MOON_LIST, m_lstMoonInfoList);
	DDX_Control(pDX, IDC_CBO_DOWNLOAD_FILETYPE, m_cboDownloadFileType);
	DDX_Control(pDX, IDC_CBO_STREAM, m_cboStreamNo);
	DDX_Control(pDX, IDC_CHECK_BREAK_CONTINUE, m_chkBreakNetContinue);
	DDX_Control(pDX, IDC_CHECK_BATCH_QUERY, m_chkBatchQuery);
	DDX_Control(pDX, IDC_COMBO_FILE_ATTR, m_cboFileAtrr);
	DDX_Control(pDX, IDC_COMBO_DL_TIMEOUT, m_cboDLTimeOut);
	DDX_Control(pDX, IDC_CHECK_ONLY_I_FRAME, m_chkIframe);
}


BEGIN_MESSAGE_MAP(CLS_RemoteFilePage, CLS_BasePage)


	ON_BN_CLICKED(IDC_BUTTON_PLAYBACK, &CLS_RemoteFilePage::OnBnClickedButtonPlayback)
	ON_CBN_SELCHANGE(IDC_COMBO_Channel, &CLS_RemoteFilePage::OnCbnSelchangeComboChannel)
	ON_CBN_SELCHANGE(IDC_COMBO_FILE_TYPE, &CLS_RemoteFilePage::OnCbnSelchangeComboFileType)
	ON_CBN_SELCHANGE(IDC_COMBO_REC_TYPE, &CLS_RemoteFilePage::OnCbnSelchangeComboRecType)
	ON_CBN_SELCHANGE(IDC_COMBO_QUERY_PAGE, &CLS_RemoteFilePage::OnCbnSelchangeComboQueryPage)
	ON_BN_CLICKED(IDC_BUTTON_PREPAGE, &CLS_RemoteFilePage::OnBnClickedButtonPrepage)
	ON_BN_CLICKED(IDC_BUTTON_NEXT_PAGE, &CLS_RemoteFilePage::OnBnClickedButtonNextPage)
	ON_BN_CLICKED(IDC_BUTTON_FIRST_PAGE, &CLS_RemoteFilePage::OnBnClickedButtonFirstPage)
	ON_BN_CLICKED(IDC_BUTTON_LAST_PAGE, &CLS_RemoteFilePage::OnBnClickedButtonLastPage)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_QUERY_BEGINTIME, &CLS_RemoteFilePage::OnDtnDatetimechangeDatetimepickerQueryBegintime)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_QUERY_ENDTIME, &CLS_RemoteFilePage::OnDtnDatetimechangeDatetimepickerQueryEndtime)
	ON_NOTIFY(NM_CLICK, IDC_LIST_QUERY_FILE, &CLS_RemoteFilePage::OnNMClickListQueryFile)
	ON_BN_CLICKED(IDC_BUTTON_DOWNLOAD, &CLS_RemoteFilePage::OnBnClickedButtonDownload)
	ON_WM_TIMER()
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BUTTON_QUERY, &CLS_RemoteFilePage::OnBnClickedButtonQuery)
	ON_BN_CLICKED(IDC_BUTTON_STOP_DOWNLOAD, &CLS_RemoteFilePage::OnBnClickedButtonStopDownload)
	ON_BN_CLICKED(IDC_BUTTON_PAUSE_DOWNLOAD, &CLS_RemoteFilePage::OnBnClickedButtonPauseDownload)
	ON_BN_CLICKED(IDC_BUTTON_CONTINUE_DOWNLOAD, &CLS_RemoteFilePage::OnBnClickedButtonContinueDownload)
	ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_QUERY_FILE, &CLS_RemoteFilePage::OnLvnItemchangedListQueryFile)
	ON_BN_CLICKED(IDC_BTN_PLAYBACK_MOON_REFRESH, &CLS_RemoteFilePage::OnBnClickedBtnPlaybackMoonRefresh)
	ON_MESSAGE(WM_PLAY_PAGE_DESTORY, &CLS_RemoteFilePage::OnPlayPageDestory)
	ON_BN_CLICKED(IDC_BUTTON_BREAK_NET_CONTINUE, &CLS_RemoteFilePage::OnBnClickedButtonBreakNetContinue)
    ON_BN_CLICKED(IDC_BUTTON_PLAYBACK_BY_FILE_REVERSE, &CLS_RemoteFilePage::OnBnClickedButtonPlaybackByFileReverse)
	ON_BN_CLICKED(IDC_BUTTON_FILE_CHECKVOD, &CLS_RemoteFilePage::OnBnClickedButtonFileCheckvod)
	ON_CBN_SELCHANGE(IDC_COMBO_REQ_MODE, &CLS_RemoteFilePage::OnCbnSelchangeComboReqMode)
	ON_CBN_SELCHANGE(IDC_COMBO_DL_TIMEOUT, &CLS_RemoteFilePage::OnCbnSelchangeComboDlTimeout)
	ON_BN_CLICKED(IDC_BUTTON_DUALUSERS_AUTH, &CLS_RemoteFilePage::OnBnClickedButtonDualusersAuth)
	ON_CBN_SELCHANGE(IDC_COMBO_ALARM_TYPE, &CLS_RemoteFilePage::OnCbnSelchangeComboAlarmType)
END_MESSAGE_MAP()


// CLS_RemoteFilePage message handlers

BOOL CLS_RemoteFilePage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	m_DTQueryBeginTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_DTQueryEndTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_DTRecordingDate.SetFormat("yyyy-MM");


	CTime SystemTime; 
	m_DTQueryBeginTime.GetTime(SystemTime);
	CTime BeginTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 0, 0, 0);
	m_DTQueryBeginTime.SetTime(&BeginTime);
	CTime EndTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 23, 59, 0);
	m_DTQueryEndTime.SetTime(&EndTime);

	m_ListQueryFile.SetExtendedStyle(m_ListQueryFile.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT|LVS_EX_CHECKBOXES);
	m_ListQueryFile.DeleteAllItems();
	Update_UI_Text();
	m_cboAlarmType.SetCurSel(0);
	SetTimer(TIMER_QUERY_DOWNLOAD_PROGRESS, 1000, NULL);
	//CreateDirectory(g_szDownloadPath, NULL);
	g_szDownloadPath = GetLocalSaveDirectory() + "\\";

	imageList.Create(16,16,ILC_COLOR32,2,2);
	imageList.SetBkColor(RGB(255,255,255));
	HICON hICON=AfxGetApp()->LoadIcon(IDI_LOCK_OPEN);
	imageList.Add(hICON);
	hICON=AfxGetApp()->LoadIcon(IDI_LOCK);
	imageList.Add(hICON);
	m_ListQueryFile.SetImageList(&imageList,LVSIL_SMALL);

	m_cboReqMode.SetCurSel(1);

	m_lstMoonInfoList.DeleteAllItems();
	m_lstMoonInfoList.SetExtendedStyle(m_lstMoonInfoList.GetExtendedStyle()| LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT);
	CString cstrDay = "";
	CString cstrMoon = "";
	cstrMoon = GetTextEx(IDS_PLAYBACK_FILE_MOON_NO);
	for(int i = 0; i < MONTHDAYNUM; i++)
	{
		cstrDay.Format("%d", i + 1);
		m_lstMoonInfoList.InsertItem(i, cstrDay);
		m_lstMoonInfoList.SetItemText(i,  MOONCOLUMN, cstrMoon);
	}
	GetDlgItem(IDC_BUTTON_BREAK_NET_CONTINUE)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_BUTTON_FILE_CHECKVOD)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_CHECK_COMPRESS)->ShowWindow(SW_HIDE);

	m_VCADiskNo.ResetContent();
	m_VCADiskNo.AddString(GetTextEx(IDS_PLAYBACK_TYPE_ALL));
	for (int i = 0; i< MAX_DISK_GROUP_NUM; ++i)
	{
		CString strNo;
		strNo.Format("%d",i);
		m_VCADiskNo.AddString(strNo);
	}
	m_VCADiskNo.SetCurSel(0);

	m_VCADiskGroup.ResetContent();
	m_VCADiskGroup.AddString(GetTextEx(IDS_STRING_NOT_SPECIFY_DISK));
	for (int i = 1; i< MAX_DISK_GROUP_NUM_FOR_PLAYBACK; ++i)
	{
		CString strNo;
		strNo.Format("%d",i);
		m_VCADiskGroup.AddString(strNo);
	}
	m_VCADiskGroup.SetCurSel(0);	

	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_RemoteFilePage::Update_UI_DualUsersAuthButtonState()
{
	//获取双用户认证能力集
	FuncAbilityLevel tFunAbility = {0};
	tFunAbility.iSize = sizeof(tFunAbility);
	tFunAbility.iMainFuncType = 0x40;
	tFunAbility.iSubFuncType = 151;
	int iEnable = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, 0x7FFFFFFF, &tFunAbility, sizeof(tFunAbility), &iEnable);
	if (RET_SUCCESS != iRet || 0 != strcmp(tFunAbility.cParam, "1"))
	{
		GetDlgItem(IDC_BUTTON_DUALUSERS_AUTH)->ShowWindow(SW_HIDE);
		AddLog(LOG_TYPE_MSG,"", "NetClient_GetDevConfig cmd:NET_CLIENT_GET_FUNC_ABILITY, MainFuncType = %d, SubFuncType = %d, cParam = %s.", 
			tFunAbility.iMainFuncType, tFunAbility.iSubFuncType, tFunAbility.cParam);
		return;
	}
	GetDlgItem(IDC_BUTTON_DUALUSERS_AUTH)->ShowWindow(SW_SHOW);
}

void CLS_RemoteFilePage::UpdateMoonInfo()
{
	if(m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_RemoteFilePage::UpdateMoonInfo]Invalid logon id(%d)", m_iLogonID);
		return;
	}

	CString cstrMoon = "";
	FileMap stFileMap = {0};
	stFileMap.iSize = sizeof(FileMap);
	int iRet = NetClient_RecvCommand(m_iLogonID, COMMAND_ID_FILE_MAP, m_iChannelNo, &stFileMap, sizeof(FileMap));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[RemoteFilePage::UpdateMoonInfo][FILE_MAP] Set fail,error = %d", GetLastError());
		return;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_RecvCommand(%d)", m_iLogonID);
	}

	int iArryLength = sizeof(stFileMap.iRecFile)/sizeof(stFileMap.iRecFile[0]);//get the length of the array
	for(int i = 0; i < iArryLength; i++)
	{
		if(0 != stFileMap.iRecFile[i])
		{
			cstrMoon = GetTextEx(IDS_PLAYBACK_FILE_MOON_HAS);
		}
		else
		{
			cstrMoon = GetTextEx(IDS_PLAYBACK_FILE_MOON_NO);
		}
		m_lstMoonInfoList.SetItemText(i,  MOONCOLUMN, cstrMoon);
	}
}

void CLS_RemoteFilePage::RefreshMoonInfo()
{
	if(m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_RemoteFilePage::RefreshMoonInfo]Invalid logon id(%d)", m_iLogonID);
		return;
	}

	CTime tempTime;
	m_DTRecordingDate.GetTime(tempTime);

	FileMap stFileMap = {0};
	stFileMap.iSize = sizeof(FileMap);
	stFileMap.iChanNo = m_iChannelNo;	
	stFileMap.iStreamNo = m_iStreamNo;
	stFileMap.iYear = tempTime.GetYear();
	stFileMap.iMonth = tempTime.GetMonth();

	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_FILE_MAP, m_iChannelNo, &stFileMap, sizeof(FileMap));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_RemoteFilePage][FILE_MAP] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SendCommand(%d)", m_iLogonID);
	}
}

void CLS_RemoteFilePage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (_iLogonID < 0)
	{
		return;
	}
	m_iLogonID = _iLogonID;
	m_iChannelNo = (_iChannelNo < 0) ? FLAG_QUERY_ALL_CHANNEL : _iChannelNo;
	m_iStreamNo = (_iStreamNo < MAIN_STREAM) ? MAIN_STREAM : _iStreamNo;

	//get the length of the array
	m_iStreamNo = (m_iStreamNo > SUB_STREAM) ? MAIN_STREAM : m_iStreamNo;

	Update_UI_IPAndID();
	ResetQuery();
	QueryFile();
	//RefreshMoonInfo();
	UpdateMoonInfo();

	//更新双用户认证按钮状态
	Update_UI_DualUsersAuthButtonState();

	//更新是否压缩按钮状态
	Update_UI_CompressButtonState();

	UpdateMaxVodNum();
}

void CLS_RemoteFilePage::OnLogonNotify( int _ulLogonID, int _iStatus ) 
{
	if (LOGON_SUCCESS == _iStatus)
	{
		DOWNLOAD_FILE tdf = {sizeof(DOWNLOAD_FILE)};
		unsigned int iConnID = -1;
			for (list <CLS_DownloadFile *>::iterator it = m_lstDownloadFile.begin(); it != m_lstDownloadFile.end(); it++)
			{
				CLS_DownloadFile * downloadFile = *it;
				if ( downloadFile->GetLogonID() == _ulLogonID)
				{
					if (1 == downloadFile->GetBreakContinue())
					{
						strcpy_s(tdf.m_cRemoteFilename,downloadFile->GetFilename().GetLength()+1,(char *)(LPCSTR)downloadFile->GetFilename());
						strcpy_s(tdf.m_cLocalFilename,(g_szDownloadPath + downloadFile->GetFilename()).GetLength()+1,(char*)(LPCSTR)(g_szDownloadPath + downloadFile->GetFilename()));
						tdf.m_iPosition = downloadFile->GetPosition();
						tdf.m_iSpeed = 16;
						tdf.m_iIFrame = downloadFile->GetIframeFlag();
						tdf.m_iReqMode = downloadFile->GetReqMode();
						int iRet = NetClient_NetFileDownload((unsigned int*)&iConnID, m_iLogonID, DOWNLOAD_CMD_FILE_CONTINUE,&tdf,sizeof(DOWNLOAD_FILE));
						if (iRet >= 0)
						{
							downloadFile->SetConnID(iConnID);
						}
					}
					break;
				}
			}
		
	}
	else
	{
		DeleteDownload(_ulLogonID,0);
	}
}

void CLS_RemoteFilePage::OnMainNotify( int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser )
{
	if (m_iLogonID < 0)
	{
		return;
	}

	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_QUERYFILE_FINISHED:
		{
			NVS_IPAndID *pNvs = (NVS_IPAndID *)_iLParam;
			if (pNvs)
			{
				int iLogonID = *pNvs->m_piLogonID;
				if (iLogonID != m_iLogonID)
				{
					return;
				}
				NetClient_NetFileGetFileCount(m_iLogonID, &m_iTotalCount, &m_iCurrentCount);
				Update_UI_Query_Result();
			}
			//break;
		}
	case WCM_DWONLOAD_FINISHED:
		ProcessDownloadFinished((unsigned long)_iLParam);
		break;
	case WCM_DWONLOAD_FAULT:
		{
			ProcessDownloadInterrupt((unsigned long)_iLParam);
			break;
		}
	case WCM_ERR_ORDER: 
		{
			ProcessErrorOrder(_ulLogonID);
			break;
		}
	case WCM_DOWNLOAD_INTERRUPT:
		{
			ProcessDownloadInterrupt((unsigned long)_iLParam);
			break;
		}
	case WCM_LOCALSTORE_LOCK_FILE:
		{
			OnNetFileLockFiles(_iWparam, (LPARAM)_iLParam);
			break;
		}	
	case WCM_LOGON_NOTIFY:
		{
			OnLogonNotify(_ulLogonID,(int)_iLParam);			
		}
		break;
	case WCM_FILE_MAP:
		{
			UpdateMoonInfo();
		}
		break;
	case WCM_CHECKVOD_PROGRESS:
		{
			if(NULL != _iLParam)
			{
				CheckVodFileResponse *ptInfo = (CheckVodFileResponse *)_iLParam;
				if (0 == strcmp(ptInfo->cFileName,m_szFileName.GetBuffer(0)))
				{
					if(0 == ptInfo->iResult)
					{
						CString str;
						str.Format("%d%%",ptInfo->iProgress);
						SetDlgItemText(IDC_STATIC_CHECKVOD, str);
					}
					else
					{
						OutPutCheckResult(ptInfo->iResult);
					}

				}
			}
		}
		break;
	case WCM_DOWNLOAD_FORCESTOPRECV://录像下载长时间没数据强制断开消息
		{
			AddLog(LOG_TYPE_MSG,"","Download the video file timed out! LogonID(%d),ConnectID(%d) FileName(%s)",m_iLogonID,(unsigned long)_iLParam, (LPSTR)(LPCTSTR)GetFileNameByConID((unsigned long)_iLParam));
			DeleteDownload((unsigned long)_iLParam);
		}
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

void CLS_RemoteFilePage::OnBnClickedButtonPlayback()
{
    PlaybackByFile(PLAYBACK_FORWARD);
}

void CLS_RemoteFilePage::Update_UI_Text()
{
	SetDlgItemTextEx(IDC_STATIC_PLAYBACK_QUERY_CONDITION, IDS_PLAYBACK_QUERY_CONDITION);

	m_ComboFileType.ResetContent();
	m_ComboFileType.AddString(GetTextByLan(_T("所有文件"), _T("All File")));
	m_ComboFileType.AddString(GetTextByLan(_T("视频-sdv"), _T("Video-sdv")));
	m_ComboFileType.AddString(GetTextByLan(_T("图片"), _T("Picture")));
	m_ComboFileType.AddString(GetTextByLan(_T("视频-mp4"), _T("Video-mp4")));
	m_ComboFileType.AddString(GetTextByLan(_T("全部视频"), _T("All Video")));
	m_ComboFileType.SetCurSel(1);

	m_ComboRecType.ResetContent();
	m_ComboRecType.AddString(GetTextEx(IDS_PLAYBACK_TYPE_ALL));
	m_ComboRecType.AddString(GetTextEx(IDS_PLAYBACK_REC_TYPE_MANUEL));
	m_ComboRecType.AddString(GetTextEx(IDS_PLAYBACK_REC_TYPE_TIMER));
	m_ComboRecType.AddString(GetTextEx(IDS_PLAYBACK_REC_TYPE_ALARM));
	for (int i = 32; i < 64; i++)
	{
		CString str;
		str.Format("%d", i);
		m_ComboRecType.AddString(str);
	}
	m_ComboRecType.SetCurSel(0);

	m_VCAdetailType.ResetContent();
	int iDetail = 0;
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("全部"), _T("All")));
	m_VCAdetailType.SetItemData(iDetail, 0x7fffffff);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("单绊线"), _T("Single Trip Wire")));
	m_VCAdetailType.SetItemData(iDetail, 0);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("双绊线"), _T("Double Trip Wire")));
	m_VCAdetailType.SetItemData(iDetail, 1);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("周界检测"), _T("Perimeter Trip Wire")));
	m_VCAdetailType.SetItemData(iDetail, 2);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("徘徊"), _T("Hover")));
	m_VCAdetailType.SetItemData(iDetail, 3);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("停车"), _T("Park")));
	m_VCAdetailType.SetItemData(iDetail, 4);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("奔跑"), _T("Run")));
	m_VCAdetailType.SetItemData(iDetail, 5);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("被盗物"), _T("Res Furtiva")));
	m_VCAdetailType.SetItemData(iDetail, 7);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("遗弃物"), _T("Abandum")));
	m_VCAdetailType.SetItemData(iDetail, 8);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("人脸检测"), _T("Face Detection")));
	m_VCAdetailType.SetItemData(iDetail, 9);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("视频诊断"), _T("Video Diagnosis")));
	m_VCAdetailType.SetItemData(iDetail, 10);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("人群聚集"), _T("Crowd")));
	m_VCAdetailType.SetItemData(iDetail, 13);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("违章停车"), _T("Illegal Parking")));
	m_VCAdetailType.SetItemData(iDetail, 20);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("警戒"), _T("Warning")));
	m_VCAdetailType.SetItemData(iDetail, 22);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("车牌识别"), _T("Plate Recognition")));
	m_VCAdetailType.SetItemData(iDetail, 23);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("车位看守"), _T("Parking Warden")));
	m_VCAdetailType.SetItemData(iDetail, 28);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("安全帽检测"), _T("Helmet Detection")));
	m_VCAdetailType.SetItemData(iDetail, 30);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("烟火检测"), _T("Pyrotechnic Detection")));
	m_VCAdetailType.SetItemData(iDetail, 64);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("室内电动车检测"), _T("Indoor Elec-vehicle Detection")));
	m_VCAdetailType.SetItemData(iDetail, 68);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("人脸比对"), _T("Face Comparison")));
	m_VCAdetailType.SetItemData(iDetail, 10000);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("陌生人"), _T("Stranger")));
	m_VCAdetailType.SetItemData(iDetail, 10001);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("值岗检测"), _T("Duty Detection")));
	m_VCAdetailType.SetItemData(iDetail, 10002);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("音频异常"), _T("Audio Anomaly")));
	m_VCAdetailType.SetItemData(iDetail, 10003);
	iDetail = m_VCAdetailType.AddString(GetTextByLan(_T("温度检测"), _T("Temp Detection")));
	m_VCAdetailType.SetItemData(iDetail, 10004);
	m_VCAdetailType.SetCurSel(0);

	SetDlgItemTextEx(IDC_STATIC_FILE_TYPE, IDS_PLAYBACK_FILE_TYPE);
	SetDlgItemTextEx(IDC_STATIC_REC_TYPE, IDS_PLAYBACK_REC_TYPE);
	SetDlgItemTextEx(IDC_STATIC_CHANNEL, IDS_PLAYBACK_CHANNEL);
	SetDlgItemTextEx(IDC_STATIC_TIME_RANGE, IDS_PLAYBACK_TIME_RANGE);
	SetDlgItemTextEx(IDC_STATIC_OSD, IDS_PLAYBACK_OSD);
	SetDlgItemTextEx(IDC_STATIC_DEVICE_IP, IDS_PLAYBACK_DEVICE_IP);
	SetDlgItemTextEx(IDC_STATIC_DEVICE_ID, IDS_PLAYBACK_DEVICE_ID);
	SetDlgItemTextEx(IDC_STATIC_FILE_COUNT, IDS_PLAYBACK_TOTAL_FILE_COUNT);
	SetDlgItemTextEx(IDC_BUTTON_PLAYBACK, IDS_PLAYBACK_PLAY);
	SetDlgItemTextEx(IDC_BUTTON_DOWNLOAD, IDS_PLAYBACK_DOWNLOAD);
	SetDlgItemTextEx(IDC_BUTTON_STOP_DOWNLOAD, IDS_PLAYBACK_STOP);
	SetDlgItemTextEx(IDC_BUTTON_PAUSE_DOWNLOAD, IDS_PLAYBACK_PAUSE);
	SetDlgItemTextEx(IDC_BUTTON_CONTINUE_DOWNLOAD, IDS_PLAYBACK_CONTINUE);
	SetDlgItemTextEx(IDC_BUTTON_PREPAGE, IDS_PLAYBACK_PREPAGE);
	SetDlgItemTextEx(IDC_BUTTON_NEXT_PAGE, IDS_PLAYBACK_NEXT_PAGE);
	SetDlgItemTextEx(IDC_BUTTON_FIRST_PAGE, IDS_PLAYBACK_FIRST_PAGE);
	SetDlgItemTextEx(IDC_BUTTON_LAST_PAGE, IDS_PLAYBACK_LAST_PAGE);
	SetDlgItemTextEx(IDC_STATIC_QUERY_PAGE, IDS_PLAYBACK_QUERY_PAGE);	
	SetDlgItemTextEx(IDC_BUTTON_QUERY, IDS_PBK_QUERY);
	SetDlgItemTextEx(IDC_STATIC_ALARM_TYPE, IDS_PLAYBACK_ALARM_TYPE);
	SetDlgItemTextEx(IDC_STATIC_VCA_DETAIL_TYPE, IDS_STRING_VCA_DETAIL_TYPE);
	SetDlgItemTextEx(IDC_STATIC_ALARMPORT, IDS_IOPORT_INPORT);
	SetDlgItemText(IDC_BUTTON_PLAYBACK_BY_FILE_REVERSE, GetTextByLan("退播", "Reverse"));
	SetDlgItemText(IDC_BUTTON_FILE_CHECKVOD, GetTextByLan("校验", "CheckVod"));
	SetDlgItemText(IDC_STATIC_DISK_NUMBER, GetTextByLan("磁盘编号", "Disk"));
	SetDlgItemText(IDC_STATIC_DISK_GROUP, GetTextByLan("盘组", "Disk Group"));
	
	
	int iIndex = 0;
	InsertString(m_cboAlarmType, iIndex++, IDS_CONFIG_ITS_NO_ENABLE);
	InsertString(m_cboAlarmType, iIndex++, IDS_CONFIG_DNVR_PORTALARM);
	InsertString(m_cboAlarmType, iIndex++, IDS_CONFIG_DNVR_MOTIONDETECT);
	InsertString(m_cboAlarmType, iIndex++, IDS_CONFIG_DNVR_VIDEOLOST);
	InsertString(m_cboAlarmType, iIndex++, IDS_CONFIG_DNVR_AUDIOLOST);
	InsertString(m_cboAlarmType, iIndex++, IDS_CONFIG_DNVR_VIDEOCOVER);
	InsertString(m_cboAlarmType, iIndex++, IDS_CONFIG_DVR_VCA);
	InsertString(m_cboAlarmType, iIndex++, IDS_VCA_MOVE_AND_DETECT);

	int iColumnIndex = 0;
	InsertColumn( m_ListQueryFile, iColumnIndex++, "ID", LVCFMT_LEFT, 60 );//insert column
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_PLAYBACK_FILE_NAME, LVCFMT_LEFT, 180 );
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_PLAYBACK_REC_TYPE, LVCFMT_LEFT, 70 );
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_PLAYBACK_CHANNEL, LVCFMT_LEFT, 70 );
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_PLAYBACK_FILE_SIZE, LVCFMT_LEFT, 80 );
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_PLAYBACK_DOWNLOAD_BEGINTIME, LVCFMT_LEFT, 150 );
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_PLAYBACK_DOWNLOAD_ENDTIME, LVCFMT_LEFT, 150 );
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_STRING_VIDEO_ALIAS, LVCFMT_LEFT, 180 );
	InsertColumn( m_ListQueryFile, iColumnIndex++, IDS_PLAYBACK_DOWNLOAD_PROGRESS, LVCFMT_LEFT, 120 );


	iIndex = 0;
	InsertString( m_cboReqMode, iIndex++, IDS_PBK_REQ_MODE_STREAM);
	InsertString( m_cboReqMode, iIndex++, IDS_PBK_REQ_MODE_FRAME);

	SetDlgItemText(IDC_CHECK_ONLY_I_FRAME, GetTextByLan("I 帧", "I Frame"));
	SetDlgItemTextEx(IDC_STATIC_REQ_MODE, IDS_PBK_REQ_MODE);
	SetDlgItemTextEx(IDC_CHECK_BREAK_CONTINUE, IDS_PBK_BREAK_CONTINUE);
	SetDlgItemTextEx(IDC_GBO_PLAYBACK_MOON_INFO, IDS_PLAYBACK_FILE_MOON_GBO_MOONINFO);
	SetDlgItemTextEx(IDC_STC_PLAYBACK_MOON_YEAR, IDS_PLAYBACK_FILE_MOON_STC_YEAR);	
	SetDlgItemTextEx(IDC_STC_PLAYBACK_MOON_MONTH, IDS_PLAYBACK_FILE_MOON_STCMONTH);
	SetDlgItemTextEx(IDC_BTN_PLAYBACK_MOON_REFRESH, IDS_PLAYBACK_FILE_MOON_REFRESH);
	SetDlgItemTextEx(IDC_CHECK_BATCH_QUERY, IDS_BATCH);

	int iCountMoonIndex = 0;
	InsertColumn(m_lstMoonInfoList, iCountMoonIndex++, GetTextEx(IDS_PLAYBACK_FILE_MOON_DATE), LVCFMT_LEFT, 200);
	InsertColumn(m_lstMoonInfoList, iCountMoonIndex++, GetTextEx(IDS_PLAYBACK_FILE_MOON_SATUS), LVCFMT_LEFT, 235);

	SetDlgItemTextEx(IDC_STC_DOWNLOAD_FILETYPE, IDS_PLAYBACK_FILE_TYPE);
	iIndex = m_cboDownloadFileType.GetCurSel();
	m_cboDownloadFileType.ResetContent();
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("SDV")), DOWNLOAD_FILE_TYPE_SDV);
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("PS")), DOWNLOAD_FILE_TYPE_PS);
    m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("TS")), DOWNLOAD_FILE_TYPE_TS);
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("MP4")), DOWNLOAD_FILE_TYPE_ZFMP4);
	m_cboDownloadFileType.SetItemData(m_cboDownloadFileType.AddString(_T("AVI")), DOWNLOAD_FILE_TYPE_AVI);
	iIndex = (iIndex >= m_cboDownloadFileType.GetCount() || iIndex < 0) ? 0 : iIndex;
	m_cboDownloadFileType.SetCurSel(iIndex);

	SetDlgItemTextEx(IDC_STC_STREAM, IDS_CONFIG_DNVR_DIGIT_STREAMTYPE);
	iIndex = m_cboStreamNo.GetCurSel();
	m_cboStreamNo.ResetContent();
	m_cboStreamNo.SetItemData(m_cboStreamNo.AddString(GetTextEx(IDS_MAJOR)), MAIN_STREAM);
	m_cboStreamNo.SetItemData(m_cboStreamNo.AddString(GetTextEx(IDS_MINOR)), SUB_STREAM);
	iIndex = (iIndex >= m_cboStreamNo.GetCount() || iIndex < 0) ? 0 : iIndex;
	m_cboStreamNo.SetCurSel(iIndex);
	OnCbnSelchangeComboChannel();

	m_chkBreakNetContinue.SetCheck(BST_UNCHECKED);

	m_cboFileAtrr.ResetContent();
	m_cboFileAtrr.InsertString(0, "NvrLocal");
	m_cboFileAtrr.InsertString(1, "NvrLocalIpc");
	m_cboFileAtrr.InsertString(2, "AllNvrLocal");
	m_cboFileAtrr.InsertString(3, "IpcStorage");
	m_cboFileAtrr.SetCurSel(0);

	SetDlgItemText(IDC_BUTTON_BREAK_NET_CONTINUE,GetTextByLan("续传", "Sequel"));
	SetDlgItemText(IDC_STATIC_DL_TIMEOUT,GetTextByLan("下载超时时间", "Download timeout"));

	m_cboDLTimeOut.ResetContent();
	m_cboDLTimeOut.SetItemData(m_cboDLTimeOut.AddString(_T("OFF")), 0);
	m_cboDLTimeOut.SetItemData(m_cboDLTimeOut.AddString(_T("1min")), 60);
	m_cboDLTimeOut.SetItemData(m_cboDLTimeOut.AddString(_T("5min")), 300);
	m_cboDLTimeOut.SetItemData(m_cboDLTimeOut.AddString(_T("10min")), 600);
	m_cboDLTimeOut.SetCurSel(3);

	SetDlgItemText(IDC_BUTTON_DUALUSERS_AUTH, GetTextByLan("双用户认证", "Dual users authentication"));
	SetDlgItemTextEx(IDC_CHECK_COMPRESS, IDS_TEXT_COMPRESS_OR_NOT);
}

void CLS_RemoteFilePage::Update_UI_Query_Result()
{
	CString strTotalFile = GetTextEx(IDS_PLAYBACK_TOTAL_FILE_COUNT) + ":";
	strTotalFile.AppendFormat("%d", m_iTotalCount);
	SetDlgItemText(IDC_STATIC_FILE_COUNT ,strTotalFile);
	m_iTotalPage = (m_iTotalCount+MAX_PAGESIZE-1)/MAX_PAGESIZE;

	m_ComboQueryPage.ResetContent();
	for (int i = 0; i < m_iTotalPage; i++)
	{
		CString strPage;
		strPage.Format("%d", i+1);
		m_ComboQueryPage.AddString(strPage);
	}
	m_ComboQueryPage.SetCurSel(m_iCurrentPage);

	m_ListQueryFile.DeleteAllItems();
	NVS_FILE_DATA_EX fileInfo = {0};
	fileInfo.iSize = sizeof(NVS_FILE_DATA_EX);
	for(int i=0; i<m_iCurrentCount; i++)
	{
		int iRet = NetClient_NetFileGetQueryfileEx(m_iLogonID, i, &fileInfo);
 		if(0 == iRet)
 		{
 			int iCellID = m_iCurrentPage*MAX_PAGESIZE + i+1;
 			if (iCellID <= m_iTotalCount)
 			{
				CString szID;
				szID.Format("%d", iCellID);
				m_ListQueryFile.InsertItem(i, "");

				if(m_ListQueryFile.GetCheck(i) != fileInfo.iLocked)
				{
					m_ListQueryFile.SetCheck(i,fileInfo.iLocked);
				}
				m_ListQueryFile.SetItem(i,0,LVIF_IMAGE,NULL,fileInfo.iLocked,0,0,0,0);
				
				int iColumn = 0;
				m_ListQueryFile.SetItemText(i, iColumn++, szID);
 				m_ListQueryFile.SetItemText(i, iColumn++, fileInfo.tFileData.cFileName);				
				CString szType;
				szType.Format("%d", fileInfo.tFileData.iType);
				m_ListQueryFile.SetItemText(i, iColumn++, szType);
				CString szChannel;
				szChannel.Format("%d", fileInfo.tFileData.iChannel);
				m_ListQueryFile.SetItemText(i, iColumn++, szChannel);
				CString szFileSize;
				szFileSize.Format("%d", fileInfo.tFileData.iFileSize);
				m_ListQueryFile.SetItemText(i, iColumn++, szFileSize);
				CString szTime;
				szTime.Format("%d-%02d-%02d %02d:%02d:%02d", fileInfo.tFileData.struStartTime.iYear
					, fileInfo.tFileData.struStartTime.iMonth, fileInfo.tFileData.struStartTime.iDay
					, fileInfo.tFileData.struStartTime.iHour, fileInfo.tFileData.struStartTime.iMinute
					, fileInfo.tFileData.struStartTime.iSecond);
				m_ListQueryFile.SetItemText(i, iColumn++, szTime);
			
				szTime.Format("%d-%02d-%02d %02d:%02d:%02d", fileInfo.tFileData.struStoptime.iYear
					, fileInfo.tFileData.struStoptime.iMonth, fileInfo.tFileData.struStoptime.iDay
					, fileInfo.tFileData.struStoptime.iHour, fileInfo.tFileData.struStoptime.iMinute
					, fileInfo.tFileData.struStoptime.iSecond);
				m_ListQueryFile.SetItemText(i, iColumn++, szTime);
				unsigned long iConnID;
				m_ListQueryFile.SetItemText(i, iColumn++, fileInfo.cRecAliasName);
				if (IsFileInDownLoadList(m_iLogonID, fileInfo.tFileData.cFileName, &iConnID))
				{
					int iPos = 0, iSize = 0;
					NetClient_NetFileGetDownloadPos(iConnID, &iPos, &iSize);
					//优先使用文件大小计算进度
					if(fileInfo.tFileData.iFileSize > 0 && iSize > 0)
					{
						iPos = (iSize / (float)fileInfo.tFileData.iFileSize) * 100;
					}
					CString szProgress;
					szProgress.Format("%d", iPos);
					szProgress += "%/";
					CString szSize;
					szSize.Format("%dB", iSize);
					szProgress += szSize;
					m_ListQueryFile.SetItemText(i, iColumn++, szProgress);
				}
 			}
 		}
	}
}

void CLS_RemoteFilePage::Update_UI_IPAndID()
{
	PDEVICE_INFO DeviceInfo = FindDevice(m_iLogonID);
	if (DeviceInfo)
	{
		CString strDeviceIP = GetTextEx(IDS_PLAYBACK_DEVICE_IP) + ":" + DeviceInfo->cIP;
		CString strDeviceID = GetTextEx(IDS_PLAYBACK_DEVICE_ID) + ":" + DeviceInfo->cID;
		SetDlgItemText(IDC_STATIC_DEVICE_IP ,strDeviceIP);
		SetDlgItemText(IDC_STATIC_DEVICE_ID ,strDeviceID);
		int iChannelNum = 0;
		NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
		m_ComboChannelNo.ResetContent();
		for (int i = 0; i < iChannelNum; i++)
		{
			CString str;
			str.Format("%d", i);
			m_ComboChannelNo.SetItemData(m_ComboChannelNo.AddString(str), i);
		}
		m_ComboChannelNo.SetItemData(m_ComboChannelNo.AddString(GetTextEx(IDS_PLAYBACK_TYPE_ALL)), FLAG_QUERY_ALL_CHANNEL);
		m_ComboChannelNo.SetCurSel(0);

		OnCbnSelchangeComboChannel();

		int iAlarmChannelNo = 0,iAlarmOutPortNum = 0;
		int iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iAlarmChannelNo, &iAlarmOutPortNum);
		m_cboAlarmPort.ResetContent();
		for (int i = 0; i < iAlarmChannelNo; i++)
		{
			CString szChannel;
			szChannel.Format("%d", i);
			m_cboAlarmPort.AddString(szChannel);
		}
		m_cboAlarmPort.SetCurSel(0);
	}
}

void CLS_RemoteFilePage::QueryFile()
{
	m_ListQueryFile.DeleteAllItems();
	NETFILE_QUERY_V5 tMultiChanQueryFile = {0};
	int iType = m_ComboRecType.GetCurSel();
	if (0 == iType)
	{
		tMultiChanQueryFile.iType = 0xFF;
	}
	else if (iType <= 3)
	{
		tMultiChanQueryFile.iType = iType;
	}
	else
	{
		tMultiChanQueryFile.iType = GetDlgItemInt(IDC_COMBO_REC_TYPE);
	}

	int iDetail = m_VCAdetailType.GetCurSel();
	tMultiChanQueryFile.iVcaDetailType = m_VCAdetailType.GetItemData(iDetail);
	int iSelIndex = m_ComboChannelNo.GetCurSel();
	int iChannelNo = m_ComboChannelNo.GetItemData(iSelIndex); 
	tMultiChanQueryFile.iQueryChannelNo = iChannelNo;
	tMultiChanQueryFile.iStreamNo = m_cboStreamNo.GetItemData(m_cboStreamNo.GetCurSel());
	CTime tempTime;
	m_DTQueryBeginTime.GetTime(tempTime);
	tMultiChanQueryFile.tStartTime.iYear = tempTime.GetYear();
	tMultiChanQueryFile.tStartTime.iMonth = tempTime.GetMonth();
	tMultiChanQueryFile.tStartTime.iDay = tempTime.GetDay();
	tMultiChanQueryFile.tStartTime.iHour = tempTime.GetHour();
	tMultiChanQueryFile.tStartTime.iMinute = tempTime.GetMinute();
	tMultiChanQueryFile.tStartTime.iSecond = tempTime.GetSecond();

	m_DTQueryEndTime.GetTime(tempTime);
	tMultiChanQueryFile.tStopTime.iYear = tempTime.GetYear();
	tMultiChanQueryFile.tStopTime.iMonth = tempTime.GetMonth();
	tMultiChanQueryFile.tStopTime.iDay = tempTime.GetDay();
	tMultiChanQueryFile.tStopTime.iHour = tempTime.GetHour();
	tMultiChanQueryFile.tStopTime.iMinute = tempTime.GetMinute();
	tMultiChanQueryFile.tStopTime.iSecond = tempTime.GetSecond();

	tMultiChanQueryFile.iPageSize = MAX_PAGESIZE;
	tMultiChanQueryFile.iPageNo = m_iCurrentPage;
	tMultiChanQueryFile.iFiletype = m_ComboFileType.GetCurSel();
	GetDlgItemText(IDC_EDIT_OSD, tMultiChanQueryFile.cOtherQuery, sizeof(tMultiChanQueryFile.cOtherQuery));
	if (0 == m_cboAlarmType.GetCurSel())
	{
		tMultiChanQueryFile.iTriggerType = 0x7FFFFFFF;
	}
	else if (6 == m_cboAlarmType.GetCurSel())
	{
		tMultiChanQueryFile.iTriggerType = m_cboAlarmType.GetCurSel() + 3;
	}
	else if (7 == m_cboAlarmType.GetCurSel())
	{
		tMultiChanQueryFile.iTriggerType = m_cboAlarmType.GetCurSel() + 6;
	}
	else
	{
		tMultiChanQueryFile.iTriggerType = m_cboAlarmType.GetCurSel() + 2;
	}
	tMultiChanQueryFile.iTrigger = m_cboAlarmPort.GetCurSel();

	if(m_VCADiskNo.GetCurSel() < 9){
		tMultiChanQueryFile.iQueryCondition = m_VCADiskNo.GetCurSel();
	}else{
		tMultiChanQueryFile.iQueryCondition = m_VCADiskNo.GetCurSel()+ 1000;
	}

	tMultiChanQueryFile.iQueryTypeValue[0] = m_VCADiskGroup.GetCurSel();
	if (BST_CHECKED == m_chkBatchQuery.GetCheck())
	{
		tMultiChanQueryFile.iQueryChannelCount = 2;
	}
	else
	{
		tMultiChanQueryFile.iQueryChannelCount = 0;
	}
	QueryFileChannel ch_list_tmp[2] = {0};
	ch_list_tmp[0].iChannelNo = iChannelNo;
	ch_list_tmp[0].iStreamNo = 0;
	ch_list_tmp[1].iChannelNo = iChannelNo;
	ch_list_tmp[1].iStreamNo = 1;
	tMultiChanQueryFile.ptChannelList = ch_list_tmp;
	tMultiChanQueryFile.iBufferSize = sizeof(QueryFileChannel);
	if(3 == m_cboFileAtrr.GetCurSel())
	{
		tMultiChanQueryFile.iFileAttr = 10000;
	}
	else
	{
		tMultiChanQueryFile.iFileAttr = m_cboFileAtrr.GetCurSel();
	}

	int iRet = NetClient_Query_V4(m_iLogonID, CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE, 0, &tMultiChanQueryFile, sizeof(tMultiChanQueryFile));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_NetFileQueryEx(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_NetFileQueryEx(%d)", m_iLogonID);
	}
}

void CLS_RemoteFilePage::ResetQuery()
{
	m_ComboQueryPage.ResetContent();
    m_iCurrentPage = 0;
	m_szFileName = "";
}
void CLS_RemoteFilePage::OnCbnSelchangeComboChannel()
{
	int iShowStream = SW_SHOW;
	if (0 > m_ComboChannelNo.GetCurSel())
	{
		iShowStream = SW_HIDE;
	}

	m_cboStreamNo.ShowWindow(iShowStream);
	GetDlgItem(IDC_STC_STREAM)->ShowWindow(iShowStream);
}

void CLS_RemoteFilePage::OnCbnSelchangeComboFileType()
{

}

void CLS_RemoteFilePage::OnCbnSelchangeComboRecType()
{

}

void CLS_RemoteFilePage::OnCbnSelchangeComboQueryPage()
{
	// TODO: Add your control notification handler code here
	m_iCurrentPage = m_ComboQueryPage.GetCurSel();
	QueryFile();
}

void CLS_RemoteFilePage::OnBnClickedButtonPrepage()
{
	// TODO: Add your control notification handler code here
	if (m_iCurrentPage > 0)
	{
		m_iCurrentPage--;
		QueryFile();
	}
}

void CLS_RemoteFilePage::OnBnClickedButtonNextPage()
{
	// TODO: Add your control notification handler code here
	if (m_iCurrentPage < m_iTotalPage -1)
	{
		m_iCurrentPage++;
		QueryFile();
	}
}

void CLS_RemoteFilePage::OnBnClickedButtonFirstPage()
{
	// TODO: Add your control notification handler code here
	m_iCurrentPage = 0;
	QueryFile();
}

void CLS_RemoteFilePage::OnBnClickedButtonLastPage()
{
	// TODO: Add your control notification handler code here
	if (m_iTotalPage > 0)
	{
		m_iCurrentPage = m_iTotalPage - 1;
		QueryFile();
	}
}

void CLS_RemoteFilePage::OnDtnDatetimechangeDatetimepickerQueryBegintime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMDATETIMECHANGE pDTChange = reinterpret_cast<LPNMDATETIMECHANGE>(pNMHDR);

	*pResult = 0;
}

void CLS_RemoteFilePage::OnDtnDatetimechangeDatetimepickerQueryEndtime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMDATETIMECHANGE pDTChange = reinterpret_cast<LPNMDATETIMECHANGE>(pNMHDR);
	*pResult = 0;
}

void CLS_RemoteFilePage::OnLanguageChanged( int )
{
	Update_UI_Text();
	if (m_iLogonID >= 0)
	{
		m_iCurrentPage = 0;
		QueryFile();
	}
	UpdateMoonInfo();
	Update_UI_IPAndID();
}

void CLS_RemoteFilePage::OnNMClickListQueryFile(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	// TODO: Add your control notification handler code here
	*pResult = 0;
	int nItem = pNMItemActivate->iItem;
	m_szFileName = m_ListQueryFile.GetItemText(nItem, 1);
	CString csFileSize = m_ListQueryFile.GetItemText(nItem,4);
	m_iFileSize = atoi(csFileSize);
	SetDlgItemText(IDC_STATIC_CHECKVOD, "");
}

void CLS_RemoteFilePage::OnBnClickedButtonDownload()
{
	// TODO: Add your control notification handler code here
	if (0 > m_iLogonID || "" == m_szFileName)
	{
		return;
	}

	unsigned long iConnID = INVALID_ID;
	if (IsFileInDownLoadList(m_iLogonID, m_szFileName, &iConnID))
	{
		return;
	}
	CString cstrLocalFileName = g_szDownloadPath + m_szFileName;
	DOWNLOAD_FILE tDownloadFileInfo = {sizeof(DOWNLOAD_FILE)};
	tDownloadFileInfo.m_iSaveFileType = m_cboDownloadFileType.GetItemData(m_cboDownloadFileType.GetCurSel());
	if (DOWNLOAD_FILE_TYPE_ZFMP4 == tDownloadFileInfo.m_iSaveFileType)
	{
		PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".mp4"));
		cstrLocalFileName.ReleaseBuffer();
	}
	else if (DOWNLOAD_FILE_TYPE_MP4 == tDownloadFileInfo.m_iSaveFileType || DOWNLOAD_FILE_TYPE_PS == tDownloadFileInfo.m_iSaveFileType)
	{
		PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".ps"));
		cstrLocalFileName.ReleaseBuffer();
	}
	else if (DOWNLOAD_FILE_TYPE_AVI == tDownloadFileInfo.m_iSaveFileType)
	{
		PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".avi"));
		cstrLocalFileName.ReleaseBuffer();
	}
    else if (DOWNLOAD_FILE_TYPE_TS == tDownloadFileInfo.m_iSaveFileType)
    {
        PathRenameExtension(cstrLocalFileName.GetBuffer(),  _T(".ts"));
        cstrLocalFileName.ReleaseBuffer();
    }
	else if (DOWNLOAD_FILE_TYPE_SDV == tDownloadFileInfo.m_iSaveFileType)
	{
	}
	else{
		AddLog(LOG_TYPE_FAIL, "",  "Not have this download type.");
	}

	strcpy_s(tDownloadFileInfo.m_cRemoteFilename,m_szFileName.GetLength()+1,(char *)(LPCSTR)m_szFileName);
	strcpy_s(tDownloadFileInfo.m_cLocalFilename, cstrLocalFileName.GetLength() + 1,cstrLocalFileName.GetBuffer());
	cstrLocalFileName.ReleaseBuffer();
	tDownloadFileInfo.m_iPosition = -1;
	tDownloadFileInfo.m_iSpeed = 16;
	tDownloadFileInfo.m_iIFrame = m_chkIframe.GetCheck();
	tDownloadFileInfo.m_iReqMode = m_cboReqMode.GetCurSel();
	if(3 == m_cboFileAtrr.GetCurSel()) {
		tDownloadFileInfo.m_iFileAttr = 10000;
	}
	tDownloadFileInfo.m_iBitRateFlag = ((CButton*)GetDlgItem(IDC_CHECK_COMPRESS))->GetCheck();
	int iRet = NetClient_NetFileDownload((unsigned int*)&iConnID, m_iLogonID, DOWNLOAD_CMD_FILE,&tDownloadFileInfo,sizeof(DOWNLOAD_FILE));
	if (iRet >= 0)
	{
		//DOWNLOAD_CONTROL tdc = {sizeof(DOWNLOAD_CONTROL)};
		//tdc.m_iPosition = -1;
		//tdc.m_iSpeed = 16;
		//tdc.m_iReqMode = m_cboReqMode.GetCurSel();
		//int iRet = NetClient_NetFileDownload((unsigned int*)&iConnID, m_iLogonID, DOWNLOAD_CMD_CONTROL,&tdc,sizeof(DOWNLOAD_CONTROL));
		CLS_DownloadFile *downloadfile = new CLS_DownloadFile(m_iLogonID, iConnID, m_szFileName);
		downloadfile->SetBreakContinue(IsDlgButtonChecked(IDC_CHECK_BREAK_CONTINUE));
		downloadfile->SetIframeFlag(m_chkIframe.GetCheck());
		downloadfile->SetReqMode(m_cboReqMode.GetCurSel());
		m_lstDownloadFile.push_back(downloadfile);
		downloadfile->SetFileSize(m_iFileSize);

		//Standard data (PS/TS) callback
		if (DOWNLOAD_FILE_TYPE_TS == tDownloadFileInfo.m_iSaveFileType && NULL != NetClient_SetDataPackCallBack)
		{
			//If you don't need to write a file, you only need to call back, you can pass the local file name empty
			NetClient_SetDataPackCallBack(iConnID, DTYPE_TS, (void*)&_PsDataCallBackFunction, NULL);
		}
        else if (DOWNLOAD_FILE_TYPE_PS == tDownloadFileInfo.m_iSaveFileType && NULL != NetClient_SetDataPackCallBack)
        {
            //If you don't need to write a file, you only need to call back, you can pass the local file name empty
            NetClient_SetDataPackCallBack(iConnID, DTYPE_PS, (void*)&_PsDataCallBackFunction, NULL);
        }
	}
}

void CLS_RemoteFilePage::OnTimer(UINT_PTR nIDEvent)
{
	// TODO: Add your message handler code here and/or call default
	switch(nIDEvent)
	{
	case TIMER_QUERY_DOWNLOAD_PROGRESS:
		return QueryDownloadProgress();

	}
	//CLS_BasePage::OnTimer(nIDEvent);
}

void CLS_RemoteFilePage::QueryDownloadProgress()
{
	if (m_iLogonID < 0)
	{
		return;
	}
	for (int i = 0; i < MAX_PAGESIZE; i++)
	{
		CString szFileName = m_ListQueryFile.GetItemText(i, 1);
		if ("" == szFileName)
		{
			return;
		}
 	 	unsigned long iConnID;
		CLS_DownloadFile* pFile;
 	 	if (IsFileInDownLoadList(m_iLogonID, szFileName, &iConnID,&pFile))
 	 	{
 	 		int iPos = 0, iSize = 0;
 	 		NetClient_NetFileGetDownloadPos(iConnID, &iPos, &iSize);

			//优先使用文件大小计算进度
			if(NULL != pFile && pFile->GetFileSize() > 0 && iSize > 0)
			{
				iPos = (iSize / (float)pFile->GetFileSize()) * 100;
			}

			CString szProgress;
			szProgress.Format("%d", iPos);
			szProgress += "%/";
			CString szSize;
			szSize.Format("%dB", iSize);
			szProgress += szSize;
 	 		m_ListQueryFile.SetItemText(i, 8, szProgress);
			if (NULL != pFile && iSize > 0)
			{
				pFile->SetPosition(iSize);
			}
 	 	}
		else
		{
			CString szProgress;
			m_ListQueryFile.SetItemText(i, 8, szProgress);
		}
	}
	
}

BOOL CLS_RemoteFilePage::IsFileInDownLoadList( int _iLogonID, CString _szFileName , unsigned long * _iConnID, CLS_DownloadFile** _pFile)
{
	BOOL bIn = FALSE; 
	for (list <CLS_DownloadFile *>::iterator it = m_lstDownloadFile.begin(); it != m_lstDownloadFile.end(); it++)
	{
		CLS_DownloadFile * downloadFile = *it;
		if (downloadFile->GetFilename() == _szFileName && downloadFile->GetLogonID() == _iLogonID)
		{
			bIn = TRUE;
			*_iConnID = downloadFile->GetConnID();
			if (_pFile)
			{
				*_pFile = downloadFile;
			}
			break;
		}
	}
	return bIn;
}

BOOL CLS_RemoteFilePage::IsFileInPlayBackList(int _iLogonID, CString _szFileName, int* _piCurrentDevPlayPageNum)
{
	BOOL blFind = FALSE; 
	int iCurDevPlayPageNum = 0;
	for (list <CLS_FilePlayReviewPage *>::iterator it = m_lstPlayPage.begin(); it != m_lstPlayPage.end(); it++)
	{
		CLS_FilePlayReviewPage * pclsPlayPage = *it;
		if (NULL == pclsPlayPage || NULL == pclsPlayPage->GetSafeHwnd() || !IsWindow(pclsPlayPage->GetSafeHwnd()))
		{
			continue;
		}

		if (pclsPlayPage->GetLogonID() != _iLogonID)
		{
			continue;
		}

		//The same device can play no more than 5 channels at the same time
		iCurDevPlayPageNum++;

		if (pclsPlayPage->GetFilename() == _szFileName)
		{
			blFind = TRUE;
			break;
		}
	}

	if (NULL != _piCurrentDevPlayPageNum)
	{
		*_piCurrentDevPlayPageNum = iCurDevPlayPageNum;
	}
	return blFind;
}

void CLS_RemoteFilePage::OnDestroy()
{
	CLS_BasePage::OnDestroy();
	KillTimer(TIMER_QUERY_DOWNLOAD_PROGRESS);
	while(!m_lstDownloadFile.empty())
	{
		CLS_DownloadFile * downloadfile = m_lstDownloadFile.front();
		m_lstDownloadFile.pop_front();
		delete downloadfile;
	}

	while(!m_lstPlayPage.empty())
	{
		CLS_FilePlayReviewPage * pclsPlayer = m_lstPlayPage.front();
		m_lstPlayPage.pop_front();

		if (IsWindow(pclsPlayer->GetSafeHwnd()))
		{
			pclsPlayer->DestroyWindow();
		}
		delete pclsPlayer;
		pclsPlayer = NULL;
	}
	// TODO: Add your message handler code here
}

LRESULT CLS_RemoteFilePage::OnPlayPageDestory( WPARAM wParam, LPARAM lParam )
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

void CLS_RemoteFilePage::DeleteDownload( unsigned long  _ulConnID )
{
	for (list <CLS_DownloadFile *>::iterator it = m_lstDownloadFile.begin(); it != m_lstDownloadFile.end(); it++)
	{
		CLS_DownloadFile * downloadFile = *it;
		if ( downloadFile->GetConnID() == _ulConnID)
		{
			m_lstDownloadFile.erase(it);
			delete downloadFile;
			break;
		}
	}
}

void CLS_RemoteFilePage::DeleteDownload( const int _iLogonID , int _iFlag)
{
	int iDelete = 0;
	do
	{
		iDelete = 0;
		for (list <CLS_DownloadFile *>::iterator it = m_lstDownloadFile.begin(); it != m_lstDownloadFile.end(); it++)
		{
			CLS_DownloadFile * downloadFile = *it;
			if ( downloadFile->GetLogonID() == _iLogonID)
			{
				if (1 == _iFlag && 1 == downloadFile->GetBreakContinue())
				{
					continue;
				}
				m_lstDownloadFile.erase(it);
				delete downloadFile;
				iDelete = 1;
				break;
			}
		}
	}while(iDelete);
}

void CLS_RemoteFilePage::ProcessDownloadFinished( unsigned long _ulID )
{
	OpenFile(_ulID);
	DeleteDownload(_ulID);
}

void CLS_RemoteFilePage::ProcessErrorOrder( const int _iLogonID )
{
	DeleteDownload(_iLogonID, 1/*0*/);
}

void CLS_RemoteFilePage::ProcessDownloadFault( unsigned long _ulID )
{
	DeleteDownload(_ulID);
}

void CLS_RemoteFilePage::ProcessDownloadInterrupt( unsigned long _ulID )
{
	DeleteDownload(_ulID,1);
}


void CLS_RemoteFilePage::OnBnClickedButtonQuery()
{
	m_ListQueryFile.DeleteAllItems();
	m_iCurrentPage =0;
	QueryFile();
}

void CLS_RemoteFilePage::OnBnClickedButtonStopDownload()
{
	// TODO: Add your control notification handler code here
	unsigned long iConnID = INVALID_ID;
	if (IsFileInDownLoadList(m_iLogonID, m_szFileName, &iConnID))
	{
		DeleteDownload(iConnID);
	}
}

void CLS_RemoteFilePage::OnBnClickedButtonPauseDownload()
{
	// TODO: Add your control notification handler code here
	unsigned long iConnID = INVALID_ID;
	if (IsFileInDownLoadList(m_iLogonID, m_szFileName, &iConnID))
	{
		//NetClient_NetFileDownloadFile(&iConnID, m_iLogonID, "", "", 1, -1, 0);
		DOWNLOAD_CONTROL tdc = {sizeof(DOWNLOAD_CONTROL)};
		tdc.m_iPosition = -1;
		tdc.m_iSpeed = 0;
		tdc.m_iIFrame = m_chkIframe.GetCheck();
		tdc.m_iReqMode = m_cboReqMode.GetCurSel();
		int iRet = NetClient_NetFileDownload((unsigned int*)&iConnID, m_iLogonID, DOWNLOAD_CMD_CONTROL,&tdc,sizeof(DOWNLOAD_CONTROL));
	}
}

void CLS_RemoteFilePage::OnBnClickedButtonContinueDownload()
{
	// TODO: Add your control notification handler code here
	unsigned long iConnID = INVALID_ID;
	if (IsFileInDownLoadList(m_iLogonID, m_szFileName, &iConnID))
	{
		//NetClient_NetFileDownloadFile(&iConnID, m_iLogonID, "", "", 1, -1, 16);
		DOWNLOAD_CONTROL tdc = {sizeof(DOWNLOAD_CONTROL)};
		tdc.m_iPosition = -1;
		tdc.m_iSpeed = 16;
		tdc.m_iIFrame = m_chkIframe.GetCheck();
		tdc.m_iReqMode = m_cboReqMode.GetCurSel();
		NetClient_NetFileDownload((unsigned int*)&iConnID, m_iLogonID, DOWNLOAD_CMD_CONTROL,&tdc,sizeof(DOWNLOAD_CONTROL));
	}
}

CString CLS_RemoteFilePage::GetFileNameByConID( unsigned long _ulConnID )
{
	for (list <CLS_DownloadFile *>::iterator it = m_lstDownloadFile.begin(); it != m_lstDownloadFile.end(); it++)
	{
		CLS_DownloadFile * downloadFile = *it;
		if ( downloadFile->GetConnID() == _ulConnID)
		{
			return g_szDownloadPath + ((CLS_DownloadFile *)*it)->GetFilename();
		}
	}
	return "";
}

void CLS_RemoteFilePage::OpenFile( unsigned long _ulConnID )
{
	CString szFile = GetFileNameByConID(_ulConnID);
	CString strResult;
	if (szFile.GetLength()	>=	4)
	{
		CString strExt	=	szFile.Right(4).MakeLower();
		if(strExt	==	".bmp"	||	strExt	==	".jpg")
		{
			CString	strKey	=	"jpegfile\\shell\\open\\command";
			HKEY  	hTempKey   = (HKEY)0;
			RegOpenKeyEx(HKEY_CLASSES_ROOT, strKey,	0,	KEY_READ,	&hTempKey);
			DWORD	dwType	=	0;
			CString	strFullPath;
			DWORD	dwBufferLength	=	256;
			TCHAR	taTmp[MAX_PATH*2];
			memset(taTmp,	0,	MAX_PATH*2*sizeof(TCHAR));
			LONG	ret	=	RegQueryValueEx(hTempKey,	_T(""),	NULL,	&dwType,	(LPBYTE)taTmp,	&dwBufferLength);
			RegCloseKey(hTempKey);

			CString	strCmd	=	taTmp;
			INT	iPos	=	strCmd.ReverseFind(_T(' '));
			strCmd.Delete(iPos+1,	strCmd.GetLength()-iPos-1);

			GetWindowsDirectory(taTmp,	MAX_PATH*2);
			strCmd.Replace(_T("%SystemRoot%"),	taTmp);
			CString	strDir(taTmp);
			iPos	=	strDir.ReverseFind(_T('\\'));
			strDir.Delete(iPos+1,	strDir.GetLength()-iPos-1);
			strDir.Append(_T("Program Files"));
			strCmd.Replace(_T("%ProgramFiles%"),	strDir);
			strCmd	+=	szFile;

			memcpy(taTmp,	strCmd.GetBuffer(),	strCmd.GetLength()+2);
			//////////////////////////////////////////////////////////////////////////
			STARTUPINFO si;
			PROCESS_INFORMATION pi;

			ZeroMemory( &si, sizeof(si) );
			si.cb = sizeof(si);
			ZeroMemory( &pi, sizeof(pi) );

			// Start the child process. 
			if( !CreateProcess( NULL,   // No module name (use command line)
				taTmp,        // Command line
				NULL,           // Process handle not inheritable
				NULL,           // Thread handle not inheritable
				FALSE,          // Set handle inheritance to FALSE
				0,              // No creation flags
				NULL,           // Use parent's environment block
				NULL,           // Use parent's starting directory 
				&si,            // Pointer to STARTUPINFO structure
				&pi )           // Pointer to PROCESS_INFORMATION structure
				) 
			{
				strResult.Format("%d",	GetLastError());
			}
			else
			{
				CloseHandle( pi.hProcess );
				CloseHandle( pi.hThread );						
			}
		}
	}	
}

void CLS_RemoteFilePage::OnLogoffDevice( int _iLogonID )
{
	DeleteDownload(_iLogonID, 0);
}


void CLS_RemoteFilePage::OnLvnItemchangedListQueryFile(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
	int iOldState = ((pNMLV->uOldState >> 12) & 3);
	int iNewState = ((pNMLV->uNewState >> 12) & 3);
	if (iOldState && iNewState)
	{
		int iLButtonDown = (GetKeyState(VK_LBUTTON) >> 15) & 0x0001;
		if (iLButtonDown)
		{

			CString csMsg = GetTextByLan("该复选框是选择文件是否锁定，想下载文件直接选中行即可",
				"This checkbox is used to select whether the file is locked. To download the file, simply select the line");
			AfxMessageBox(csMsg);

			CString strFileName = m_ListQueryFile.GetItemText(pNMLV->iItem,1);
			if (0 == strFileName.Right(4).Compare(_T(".sdv")))
			{
				int iRet = NetClient_NetFileLockFile(m_iLogonID,(LPSTR)(LPCTSTR)strFileName,iNewState-1);
				if (0 != iRet)
				{
					AddLog(LOG_TYPE_FAIL,"","NetFileLockFile failed! LogonID(%d),FileName(%s),Locked(%d)"
						,m_iLogonID,(LPSTR)(LPCTSTR)strFileName,iNewState-1);
				}
			}
			else
			{
				AddLog(LOG_TYPE_MSG,"","Not record file! LogonID(%d),FileName(%s)"
					,m_iLogonID,(LPSTR)(LPCTSTR)strFileName);
			}
		}
	}
	*pResult = 0;
}

void CLS_RemoteFilePage::OnNetFileLockFiles( WPARAM wParam, LPARAM lParam )
{
	NVS_IPAndID* pIPID = (NVS_IPAndID*)lParam;
	if (pIPID && pIPID->m_piLogonID)
	{
		int iLogonID = *pIPID->m_piLogonID;
		if (iLogonID != m_iLogonID)
		{
			return;
		}

		int iIndex = (int)wParam >> 16;
		NVS_FILE_DATA_EX tFileDataEx = {0};
		tFileDataEx.iSize = sizeof(NVS_FILE_DATA_EX);
		if(0 == NetClient_NetFileGetQueryfileEx(iLogonID, iIndex, &tFileDataEx))
		{
			for(int i = 0; i < m_ListQueryFile.GetItemCount(); ++i)
			{
				if (0 == m_ListQueryFile.GetItemText(i,1).Compare(tFileDataEx.tFileData.cFileName))
				{
					if(m_ListQueryFile.GetCheck(iIndex) != tFileDataEx.iLocked)
					{
						m_ListQueryFile.SetCheck(i,tFileDataEx.iLocked);
					}
					m_ListQueryFile.SetItem(i,0,LVIF_IMAGE,NULL,tFileDataEx.iLocked,0,0,0,0);
					break;
				}
			}
		}
	}
}

void CLS_RemoteFilePage::OnBnClickedBtnPlaybackMoonRefresh()
{
	// TODO: Add your control notification handler code here
	//m_lstMoonInfoList.DeleteAllItems();
	RefreshMoonInfo();
}

void CLS_RemoteFilePage::OnBnClickedButtonBreakNetContinue()
{
	if (0 > m_iLogonID|| "" == m_szFileName)
	{
		return;
	}

	unsigned long iConnID = INVALID_ID;
	if (IsFileInDownLoadList(m_iLogonID, m_szFileName, &iConnID))
	{
		return;
	}
	CString cstrLocalFileName = g_szDownloadPath + m_szFileName;

	__int64 iOffset = 0;
	FILE* pFile = fopen(cstrLocalFileName, "r+b");
	if (NULL == pFile)
	{
		return;
	}
	_fseeki64(pFile, 0, SEEK_END);
	iOffset = _ftelli64(pFile);
	fclose(pFile);

	if(iOffset >= m_iFileSize)
	{
		AddLog(LOG_TYPE_FAIL,"","File is DownloadFinish,FileName(%s)"
			,m_iLogonID,(LPSTR)(LPCTSTR)cstrLocalFileName);
		return;
	}

	DOWNLOAD_FILE tFileInfo = {sizeof(DOWNLOAD_FILE)};
	tFileInfo.m_iSaveFileType = m_cboDownloadFileType.GetItemData(m_cboDownloadFileType.GetCurSel());
	strcpy_s(tFileInfo.m_cRemoteFilename, m_szFileName.GetLength() + 1, (char *)(LPCSTR)m_szFileName);
	strcpy_s(tFileInfo.m_cLocalFilename, cstrLocalFileName.GetLength() + 1, cstrLocalFileName.GetBuffer());
	cstrLocalFileName.ReleaseBuffer();
	tFileInfo.m_iPosition = (int)iOffset;
	tFileInfo.m_iSpeed = 16;
	tFileInfo.m_iIFrame = m_chkIframe.GetCheck();
	tFileInfo.m_iReqMode = m_cboReqMode.GetCurSel();
	int iRet = NetClient_NetFileDownload((unsigned int*)&iConnID, m_iLogonID, DOWNLOAD_CMD_FILE_CONTINUE, &tFileInfo, sizeof(DOWNLOAD_FILE));
	if (iRet >= 0)
	{
		CLS_DownloadFile *downloadfile = new CLS_DownloadFile(m_iLogonID, iConnID, m_szFileName);
		downloadfile->SetBreakContinue(IsDlgButtonChecked(IDC_CHECK_BREAK_CONTINUE));
		downloadfile->SetIframeFlag(m_chkIframe.GetCheck());
		downloadfile->SetReqMode(m_cboReqMode.GetCurSel());
		downloadfile->SetFileSize(m_iFileSize);
		m_lstDownloadFile.push_back(downloadfile);
	}
}

void CLS_RemoteFilePage::OnBnClickedButtonPlaybackByFileReverse()
{
    PlaybackByFile(PLAYBACK_REVERSE);
}

void CLS_RemoteFilePage::PlaybackByFile(int iDirection)
{
    if (0 > m_iLogonID || "" == m_szFileName)
    {
        return;
    }

    if (m_szFileName.GetLength()	>=	4)
    {
        CString strExt	=	m_szFileName.Right(4).MakeLower();
        if(".bmp" == strExt || ".jpg" == strExt)
        {
            return;
        }
    }

    unsigned long iConnID = -1;
    if (IsFileInDownLoadList(m_iLogonID, m_szFileName, &iConnID))
    {
        AddLog(LOG_TYPE_MSG, "", "File(%s) is already in download list!", m_szFileName);
        return;
    }

    int iCurDevPlayPage = 0;
    if (IsFileInPlayBackList(m_iLogonID, m_szFileName, &iCurDevPlayPage))
    {
        AddLog(LOG_TYPE_MSG, "", "File(%s) is already in playback list!", m_szFileName);
        return;
    }

    if (m_iMaxVodNum <= iCurDevPlayPage)
    {
        AddLog(LOG_TYPE_MSG, "", "Added too much file with same device to playback list!");
        return;
    }

    CLS_FilePlayReviewPage* pclsPlayReviewPage = new CLS_FilePlayReviewPage(this);

    if (NULL != pclsPlayReviewPage/* && IsWindow(pclsPlayReviewPage->GetSafeHwnd())*/)
    {
        pclsPlayReviewPage->SetPlaybackDirection(iDirection);
        int iFileArrt = 0;
        if(3 == m_cboFileAtrr.GetCurSel()) {
            iFileArrt = 10000;
        }
		pclsPlayReviewPage->SetBitRateFlag(((CButton*)GetDlgItem(IDC_CHECK_COMPRESS))->GetCheck());
		pclsPlayReviewPage->SetIframeFlag(m_chkIframe.GetCheck());
		pclsPlayReviewPage->SetDiskGroupFlag(m_VCADiskGroup.GetCurSel());
        pclsPlayReviewPage->SetDownloadParam(m_iLogonID, m_szFileName, iFileArrt);
        pclsPlayReviewPage->Create(IDD_DLG_PBK_REVIEW, this);
        pclsPlayReviewPage->ShowWindow(SW_SHOW);
    }

    m_lstPlayPage.push_back(pclsPlayReviewPage);
}

void CLS_RemoteFilePage::OutPutCheckResult(int _iResult)
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
		SetDlgItemText(IDC_STATIC_CHECKVOD, "100%");
	}
}

void CLS_RemoteFilePage::OnBnClickedButtonFileCheckvod()
{
	if (0 > m_iLogonID|| "" == m_szFileName)
	{
		return;
	}

	unsigned long iConnID = INVALID_ID;
	if (IsFileInDownLoadList(m_iLogonID, m_szFileName, &iConnID))
	{
		AddLog(LOG_TYPE_MSG, "", "File is Downloading %s",m_szFileName);
		return;
	}

	SetDlgItemText(IDC_STATIC_CHECKVOD,"0%");
	CString cstrLocalFileName = g_szDownloadPath + m_szFileName;

	char cDigest[LEN_32] = {0};
	//打开文件，计算文件大小
	__int64 iFileSize = 0;
	int iRet = GetFileMd5ForVodCheck(cstrLocalFileName.GetBuffer(0),cDigest,LEN_32,&iFileSize);
	if(iRet > RET_FAILED)
	{
		CheckVodFile tVodFile = {0};
		tVodFile.iVerifyMode = 1;
		memcpy(tVodFile.cFileName,m_szFileName.GetBuffer(0),min((int)sizeof(tVodFile.cFileName),m_szFileName.GetLength()));
		memcpy(tVodFile.cVerifyCode,cDigest,LEN_32);
		tVodFile.ullFileSize = iFileSize;

		CheckVodFileResponse tCheckVodFileResponse = {0};
		int iRetValue = NetClient_CmdConfig(m_iLogonID, CMD_CHECK_VODFILE, m_iChannelNo, &tVodFile, \
			sizeof(tVodFile), &tCheckVodFileResponse, sizeof(tCheckVodFileResponse));
		if(RET_SUCCESS == iRetValue)
		{
			OutPutCheckResult(tCheckVodFileResponse.iResult);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "File download get File md5 faild %s",cstrLocalFileName);
	}
}

void CLS_RemoteFilePage::OnCbnSelchangeComboReqMode()
{
	int index = m_cboReqMode.GetCurSel();

	//流模式再显示续传和校验的按钮
	if(0 == index)
	{
		GetDlgItem(IDC_BUTTON_BREAK_NET_CONTINUE)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_BUTTON_FILE_CHECKVOD)->ShowWindow(SW_SHOW);
	}
	else
	{
		GetDlgItem(IDC_BUTTON_BREAK_NET_CONTINUE)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_BUTTON_FILE_CHECKVOD)->ShowWindow(SW_HIDE);
	}
}

void CLS_RemoteFilePage::OnCbnSelchangeComboDlTimeout()
{
	NetFileForceStopPara tParam = {0};
	tParam.uiNoDataTimeout = m_cboDLTimeOut.GetItemData(m_cboDLTimeOut.GetCurSel());

	int iRet = NetClient_SetDevConfig(-1, NET_CLIENT_NETFILE_FORCESTOP_PARA, m_iChannelNo, &tParam, sizeof(tParam));
	if (iRet != RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig[NET_CLIENT_NETFILE_FORCESTOP_PARA] (%d, %d), error(%d)"
			, m_iLogonID, m_iChannelNo, GetLastError());
	}

}

void CLS_RemoteFilePage::OnBnClickedButtonDualusersAuth()
{
	// TODO: 在此添加控件通知处理程序代码
	int iSourceCode = 0;
	int iRet = -1;
	DualUsersAuthenticationResult tDualUsersAuthResult = {0};
	for (int i = 1; i <= 2; i++)
	{
		DualUsersAuthDlg dlgAuth(i);//第i个用户
		if(IDOK != dlgAuth.DoModal())
		{
			AddLog(LOG_TYPE_MSG,"", "Dual users authentication operating be canceled.");
			return;
		}
		T_UserInfo tUserInfo;
		dlgAuth.GetUserInfo(tUserInfo);

		DualUsersAuthentication tDualUserAuth = {0};
		tDualUserAuth.iAuthCount = i;
		tDualUserAuth.iSourceCode = iSourceCode;
		memcpy(tDualUserAuth.cUserName, tUserInfo.strUser, min(sizeof(tDualUserAuth.cUserName), tUserInfo.strUser.GetLength() + 1));
		memcpy(tDualUserAuth.cPassword, tUserInfo.strPwd, min(sizeof(tDualUserAuth.cPassword), tUserInfo.strPwd.GetLength() + 1));

		memset(&tDualUsersAuthResult, 0x0, sizeof(tDualUsersAuthResult));
		iRet = NetClient_CmdConfig(m_iLogonID, CMD_DUAL_USERS_AUTHENTICATION, 0x7FFFFFFF, &tDualUserAuth, sizeof(tDualUserAuth), &tDualUsersAuthResult, sizeof(tDualUsersAuthResult));
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL,"", "When %d times authentication NetClient_CmdConfig return failed iRet = %d", i, iRet);
			return;
		}
		if (0 != tDualUsersAuthResult.iResult)
		{
			AddLog(LOG_TYPE_FAIL,"", "When %d times authentication tDualUsersAuthResult.iResult = %d.", i, tDualUsersAuthResult.iResult);
			return;
		}
		iSourceCode = tDualUsersAuthResult.iSourceCode;
	}

	DualUsersAuthSession tDualUsersAuthSession = {0};
	tDualUsersAuthSession.iSize = sizeof(tDualUsersAuthSession);
	memcpy(tDualUsersAuthSession.cSessionID, tDualUsersAuthResult.cSessionID, min(sizeof(tDualUsersAuthSession.cSessionID), sizeof(tDualUsersAuthResult.cSessionID)));
	//设置双用户证SessionID
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DUALUSERS_AUTH_SESSION, 0, &tDualUsersAuthSession, sizeof(tDualUsersAuthSession));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"", "NetClient_SetDevConfig cmd:NET_CLIENT_DUALUSERS_AUTH_SESSION failed iRet = %d.", iRet);
		return;
	}
}

void CLS_RemoteFilePage::Update_UI_CompressButtonState()
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
#if 0//与设备确认按文件下载及加放不支持压缩流，所以注释掉显示的代码，后续项有需要再放开
	GetDlgItem(IDC_CHECK_COMPRESS)->ShowWindow(SW_SHOW);
#endif
}

void CLS_RemoteFilePage::OnCbnSelchangeComboAlarmType()
{
	UpdateBtnState();
}
void CLS_RemoteFilePage::UpdateBtnState()
{
	int iReqMode = m_cboAlarmType.GetCurSel();
	if(6 == iReqMode)
	{
		GetDlgItem(IDC_STATIC_VCA_DETAIL_TYPE)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_COMBO_VCA_DETAIL_TYPE)->ShowWindow(SW_SHOW); 
	}
	else
	{
		GetDlgItem(IDC_STATIC_VCA_DETAIL_TYPE)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_COMBO_VCA_DETAIL_TYPE)->ShowWindow(SW_HIDE);
	}
}

void CLS_RemoteFilePage::UpdateMaxVodNum()
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

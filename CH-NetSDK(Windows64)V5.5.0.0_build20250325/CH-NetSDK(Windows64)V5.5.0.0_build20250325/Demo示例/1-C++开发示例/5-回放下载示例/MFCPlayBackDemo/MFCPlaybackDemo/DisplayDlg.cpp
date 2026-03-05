// DisplayDialog.cpp : Implementation file
//

#include "stdafx.h"
#include "VideoDisplay.h"
#include "DisplayDlg.h"
#include "ConnectServer.h"
#include "OperateByFileDlg.h"
#include "OperateByTimeDlg.h"
#include "VideoDisplayDlg.h"
#include "MacroDefine.h"





// CDisplayDialog dialog
BOOL gPlayFlag = TRUE;
CLS_CDisplayDlg* CLS_CDisplayDlg::m_pThis = NULL;
IMPLEMENT_DYNAMIC(CLS_CDisplayDlg, CDialog)

CLS_CDisplayDlg::CLS_CDisplayDlg( CWnd* pParent )
	: CDialog(CLS_CDisplayDlg::IDD, pParent)
{
	m_iCaptureType = 2;
}

CLS_CDisplayDlg::~CLS_CDisplayDlg()
{
}

void CLS_CDisplayDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_STATIC_DISPLAY, m_txtDisplayVideo);
	DDX_Control(pDX, IDC_COMBO_DISPLAY_SPEED, m_cboFastPlay);
	DDX_Control(pDX, IDC_COMBO_DISPLAY_SPEED2, m_cboSlowPlay);
	DDX_Control(pDX, IDC_EDIT1, m_edtPlayPos);
	DDX_Control(pDX, IDC_BUTTON_START_DISPLAY3, m_btnPlayPosSet);
	DDX_Control(pDX, IDC_STATIC_DISPLAY_SPEED2, m_txtPlayPos);
	DDX_Control(pDX, IDC_SLIDER_VOICE_CTRL, m_sliVolumeCtr);
	DDX_Control(pDX, IDC_STATIC_VOLUME_DISPLAY, m_txtVolumeDisplay);
}


BEGIN_MESSAGE_MAP(CLS_CDisplayDlg, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_START_DISPLAY, &CLS_CDisplayDlg::OnBnClickedButtonStartDisplay)
	ON_BN_CLICKED(IDC_BUTTON_PAUSE_PLAY, &CLS_CDisplayDlg::OnBnClickedButtonPause)
	ON_BN_CLICKED(IDC_BUTTON_FAST_PLAY, &CLS_CDisplayDlg::OnBnClickedFastPlay)
	ON_BN_CLICKED(IDC_BUTTON_SLOW_SET, &CLS_CDisplayDlg::OnBnClickedSlowPlay)
	ON_BN_CLICKED(IDC_BUTTON_POS_SET, &CLS_CDisplayDlg::OnBnClickedPlayPosSet)
	ON_COMMAND_RANGE(ID_SNATCH_BMP, ID_SNATCH_JPG, &CLS_CDisplayDlg::OnSnatch)
	ON_WM_DESTROY()
	ON_WM_CLOSE()
	ON_BN_CLICKED(IDC_BUTTON_CAPTURE, &CLS_CDisplayDlg::OnBnClickedButtonCapture)
	ON_WM_HSCROLL()
	ON_BN_CLICKED(IDC_BUTTON_FORWARD_STEP, &CLS_CDisplayDlg::OnBnClickedButtonForwardStep)
	ON_BN_CLICKED(IDC_BUTTON_VOLUME_CTRL, &CLS_CDisplayDlg::OnBnClickedButtonVolumeCtrl)
END_MESSAGE_MAP()

BOOL CLS_CDisplayDlg::OnInitDialog()
{
	CDialog::OnInitDialog();
	
	m_pThis = this;
	LanguageChange(m_bLanguage);
	
	/*
	* When gPlayFlag is true, it is play by file mode
	*When gPlayFlag is false, it is playback mode by time period
	*When playing by time period, hide the controls related to the playback positioning setting
	*/
	if( !gPlayFlag )
	{
		m_txtPlayPos.ShowWindow( FALSE );
		m_edtPlayPos.ShowWindow(FALSE);
		m_btnPlayPosSet.ShowWindow( FALSE );	
	}
	HWND hWnd = m_txtDisplayVideo.GetSafeHwnd();
	PlayerParam stParam = {0};
	stParam.iSize = sizeof(PlayerParam);
	stParam.iLogonID = CLS_ConnectServer::GetInstance()->m_iLogonID;
	m_cboFastPlay.SetWindowText(DOWNLOAD_DEMO_ONE);
	m_edtPlayPos.SetWindowText(DOWNLOAD_DEMO_ZERO);
	//Get the file name when playing by file
	if( gPlayFlag )	
	{
		CString csFileName = CLS_OperateByFileDlg::m_sDownLoadFileName;
		strcpy_s(stParam.strFileName ,csFileName.GetBuffer());
		//Play interface
		int iRet =  NetClient_PlayBack( &m_uiConnID, PLAYBACK_TYPE_FILE, &stParam, (void*)hWnd);
		if (iRet < 0)
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VIDEO_PLAY_FAILED, 0, 0);
			return FALSE;
		}
		if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID)
		{
			return FALSE;
		}
		if( -1 == m_uiConnID)
		{
			return FALSE;
		}
		//Set the original stream callback
		iRet = NetClient_SetRawFrameCallBack(m_uiConnID, GetRawNotify, NULL);
		if( iRet < 0 )
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_Set_RAW_FRAME_CallBACK_FAILED, 0, 0);
		}
	}
	else
	{	//Get start and end time when playing by time
		//get channel number
		CString csChannelNo = CLS_OperateByTime::GetInstance()->m_csChannelId;
		int iChannelNo = 0;
		if( DOWNLOAD_DEMO_ALL == csChannelNo || DOWNLOAD_DEMO_ALL_EN == csChannelNo)
		{
			iChannelNo = DOWNLOAD_DEMO_CHANNEL_NUM_ALL;
		}
		else
		{
			iChannelNo = _ttoi( csChannelNo );
		}
		
		CString sStreamType = CLS_OperateByTime::GetInstance()->m_csStreamType;
		int iStreamType = 0;
		if ("副码流" == sStreamType || "Minor" == sStreamType)
		{
			iStreamType = 1;
		}

		int iChannelNum = 0;
		NetClient_GetChannelNum( CLS_ConnectServer::GetInstance()->m_iLogonID, &iChannelNum);
		int iRealChannel = iStreamType*iChannelNum+iChannelNo;

		stParam.iChannNo = iRealChannel;
		stParam.iIFrame = 2;
		stParam.iReqMode = 0;
		stParam.tBeginTime.iYear = CLS_OperateByTime::GetInstance()->m_nvsStartTime.iYear;
		stParam.tBeginTime.iMonth=CLS_OperateByTime::GetInstance()->m_nvsStartTime.iMonth;
		stParam.tBeginTime.iDay=CLS_OperateByTime::GetInstance()->m_nvsStartTime.iDay;
		stParam.tBeginTime.iHour=CLS_OperateByTime::GetInstance()->m_nvsStartTime.iHour;
		stParam.tBeginTime.iMinute= CLS_OperateByTime::GetInstance()->m_nvsStartTime.iMinute;
		stParam.tBeginTime.iSecond=CLS_OperateByTime::GetInstance()->m_nvsStartTime.iSecond;

		stParam.tEndTime.iYear = CLS_OperateByTime::GetInstance()->m_nvsStopTime.iYear;
		stParam.tEndTime.iMonth= CLS_OperateByTime::GetInstance()->m_nvsStopTime.iMonth;
		stParam.tEndTime.iDay=CLS_OperateByTime::GetInstance()->m_nvsStopTime.iDay;
		stParam.tEndTime.iHour=CLS_OperateByTime::GetInstance()->m_nvsStopTime.iHour;
		stParam.tEndTime.iMinute=CLS_OperateByTime::GetInstance()->m_nvsStopTime.iMinute;
		stParam.tEndTime.iSecond=CLS_OperateByTime::GetInstance()->m_nvsStopTime.iSecond;

		int iRet =  NetClient_PlayBack( &m_uiConnID, PLAYBACK_TYPE_TIME, &stParam, (void*)hWnd);
		if (iRet < 0)
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VIDEO_PLAY_FAILED, 0, 0);
			return FALSE;
		}
		if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID)
		{
			return FALSE;
		}
		//Set the original stream callback
		iRet = NetClient_SetRawFrameCallBack(m_uiConnID, GetRawNotify, NULL);
		if( iRet < 0 )
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_Set_RAW_FRAME_CallBACK_FAILED, 0, 0);
		}
	}
	return TRUE;
}

void CLS_CDisplayDlg::GetRawNotify(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser)
{
	if( NULL != m_pThis )
	{
		m_pThis->OnGetRawNotify(_ulID, _cData,_iLen, _pRawFrameInfo);
	}

}

void CLS_CDisplayDlg::OnGetRawNotify( unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo )
{
	if( -1 == _ulID)
	{
		return;
	}
	if( NULL != _pRawFrameInfo)
	{
		if( DOWNLOAD_DEMO_INT_ZERO == _pRawFrameInfo->nType)
		{
			//::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VI_FRAME, 0, 0);
		}
		else if( DOWNLOAD_DEMO_TYPE_AUDIO_FRAME == _pRawFrameInfo->nType)
		{
			//::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_AUDIO_FRAME, 0, 0);
		}
		else
		{
			//::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_OTHER_TYPE, 0, 0);
		}
	}

}
// CDisplayDialog message handler

void CLS_CDisplayDlg::OnBnClickedButtonStartDisplay()
{
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VIDEO_PLAY_FAILED, 0, 0);
		return;
	}
	int iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_PLAY, NULL, 0, NULL, 0 );
	if( iRet < 0 )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VIDEO_PLAY_FAILED, 0, 0);
	}
}

void CLS_CDisplayDlg::OnBnClickedButtonPause()
{
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_STOP_SET_FAILED, 0, 0);
		return;
	}
	int iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_PAUSE, NULL, 0, NULL, 0 );
	if( iRet < 0 )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_STOP_SET_FAILED, 0, 0);
	}

}

void CLS_CDisplayDlg::OnBnClickedPlayPosSet()
{
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_POS_SET_FAILED, 0, 0);
		return;
	}
	CString csPlayPos;
	m_edtPlayPos.GetWindowText( csPlayPos );
	int iInput = _ttoi( csPlayPos );
	int iRet = NetClient_PlayBackControl( m_uiConnID, PLAY_CONTROL_SEEK, (void*)(&iInput), sizeof(int), NULL, 0 );
	if( iRet < 0 )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_POS_SET_FAILED, 0, 0);
	}
}

void CLS_CDisplayDlg::OnBnClickedFastPlay()
{
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SPEED_SET_FAILED, 0, 0);
		return;
	}
	CString csPlaySpeed;
	m_cboFastPlay.GetWindowText( csPlaySpeed );
	int iInput = _ttoi(csPlaySpeed);
	if( DOWNLOAD_DEMO_INT_ONE == iInput )
	{
		int iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_PLAY, (void*)(&iInput), sizeof(int), NULL, 0 );
		if( iRet < 0 )
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SPEED_SET_FAILED, 0, 0);
		}
		
	}
	else
	{
		int iRet = NetClient_PlayBackControl( m_uiConnID, PLAY_CONTROL_FAST_FORWARD, (void*)(&iInput), sizeof(int), NULL, 0 );
		if( iRet < 0 )
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SPEED_SET_FAILED, 0, 0);
		}

	}
	
}

void CLS_CDisplayDlg::OnBnClickedSlowPlay()
{
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SPEED_SET_FAILED, 0, 0);
		return;
	}
	CString csPlaySpeed;
	m_cboSlowPlay.GetWindowText( csPlaySpeed );
	int iInput = _ttoi(csPlaySpeed);
	//1x speed
	if( DOWNLOAD_DEMO_PLAY_SPEED_ONE_TIME == iInput )
	{
		int iRet = NetClient_PlayBackControl( m_uiConnID, PLAY_CONTROL_PLAY, (void*)(&iInput), sizeof(int), NULL, 0 );
		if( iRet < 0 )
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SPEED_SET_FAILED, 0, 0);
		}
	}
	else
	{
		int iRet = NetClient_PlayBackControl( m_uiConnID, PLAY_CONTROL_SLOW_FORWARD, (void*)(&iInput), sizeof(int), NULL, 0 );
		if( iRet < 0 )
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SPEED_SET_FAILED, 0, 0);
		}
	}
}

void CLS_CDisplayDlg::StartDisplay()
{
	RECT rc = {0};
	HWND hWnd = m_txtDisplayVideo.GetSafeHwnd();
	unsigned int uiConnID = CLS_ConnectServer::GetInstance()->m_uiConnID;
	//stop playing video
	NetClient_StopPlay( uiConnID );
	//start playing video
	int iRet = NetClient_StartPlay( uiConnID, (int)hWnd, rc, H264DEC_DECTWO );
	if( iRet < 0 )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VIDEO_PLAY_FAILED, 0, 0);
	}
}


void CLS_CDisplayDlg::OnDestroy()
{
	CDialog::OnDestroy();
	StopPlay();
}

void CLS_CDisplayDlg::OnBnClickedButtonStopPlay()
{
	StopPlay();
}

void CLS_CDisplayDlg::StopPlay()
{
	if (-1 == m_uiConnID)
	{
		return;
	}
	int iRet = NetClient_StopPlayBack(m_uiConnID);
	if( iRet < 0 )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_STOP_PLAY_FAILED, 0, 0);
	}
	m_uiConnID = -1;
}

void CLS_CDisplayDlg::SetConfig(BOOL _bPlayMode, BOOL _bLanguage)
{
	gPlayFlag = _bPlayMode;
	m_bLanguage = _bLanguage;
	m_uiConnID = -1;
	m_bAudioStart = FALSE;
}
void CLS_CDisplayDlg::LanguageChange( BOOL _bLanguage )
{
	//Set playback speed drop-down menu
	m_cboFastPlay.ResetContent();
	m_cboFastPlay.AddString(DOWNLOAD_DEMO_ONE);
	m_cboFastPlay.AddString(DOWNLOAD_DEMO_DOUBLE);
	m_cboFastPlay.AddString(DOWNLOAD_DEMO_TRIPLE);
	m_cboFastPlay.AddString(DOWNLOAD_DEMO_QUADRUPLE);
	m_cboFastPlay.SetWindowText(DOWNLOAD_DEMO_ONE);

	m_cboSlowPlay.ResetContent();
	m_cboSlowPlay.AddString(DOWNLOAD_DEMO_ONE);
	m_cboSlowPlay.AddString(DOWNLOAD_DEMO_DOUBLE);
	m_cboSlowPlay.AddString(DOWNLOAD_DEMO_TRIPLE);
	m_cboSlowPlay.AddString(DOWNLOAD_DEMO_QUADRUPLE);
	m_cboSlowPlay.SetWindowText(DOWNLOAD_DEMO_ONE);

	m_sliVolumeCtr.SetRange(DOWNLOAD_DEMO_INT_ZERO, DOWNLOAD_DEMO_MAX_VALUE);
	GetDlgItem(IDC_BUTTON_START_DISPLAY)->SetWindowText( FALSE == _bLanguage ?_T("播放") : _T("Play"));
	GetDlgItem(IDC_BUTTON_PAUSE_PLAY)->SetWindowText( FALSE == _bLanguage ?_T("暂停") : _T("Pause"));
	GetDlgItem(IDC_STATIC_FAST_PLAY)->SetWindowText( FALSE == _bLanguage ?_T("快进") : _T("Fast"));
	GetDlgItem(IDC_BUTTON_FAST_PLAY)->SetWindowText( FALSE == _bLanguage ?_T("设置") : _T("Set"));
	GetDlgItem(IDC_STATIC_SLOW_PLAY)->SetWindowText( FALSE == _bLanguage ?_T("慢放") : _T("Slow"));
	GetDlgItem(IDC_BUTTON_SLOW_SET)->SetWindowText( FALSE == _bLanguage ?_T("设置") : _T("Set"));
	GetDlgItem(IDC_BUTTON_POS_SET)->SetWindowText( FALSE == _bLanguage ?_T("设置") : _T("Set"));
	GetDlgItem(IDC_STATIC_PLAY_POS)->SetWindowText( FALSE == _bLanguage ?_T("播放定位(0-100)") : _T("Pos(0-100)"));
	//GetDlgItem(IDC_BUTTON_STOP_PLAY)->SetWindowText( FALSE == _bLanguage ?_T("停止") : _T("Stop"));
	//GetDlgItem(IDC_BUTTON_BACKWORD)->SetWindowText( FALSE == _bLanguage ?_T("快退") : _T("Back"));
	//GetDlgItem(IDC_BUTTON_RETURN_TO_BEGIN)->SetWindowText( FALSE == _bLanguage ?_T("返回开始") : _T("ToBegin"));
	//GetDlgItem(IDC_BUTTON_BACK_STEP)->SetWindowText( FALSE == _bLanguage ?_T("单帧步退") : _T("BackStep"));
	GetDlgItem(IDC_BUTTON_FORWARD_STEP)->SetWindowText( FALSE == _bLanguage ?_T("步进") : _T("StepForward"));
	GetDlgItem(IDC_BUTTON_CAPTURE)->SetWindowText( FALSE == _bLanguage ?_T("抓拍") : _T("Capture"));
	//GetDlgItem(IDC_STATIC_VOLUME)->SetWindowText( FALSE == _bLanguage ?_T("音量") : _T("Volume"));
	GetDlgItem(IDC_BUTTON_VOLUME_CTRL)->SetWindowText( FALSE == _bLanguage ?_T("开启声音") : _T("VolumeOn"));
}
void CLS_CDisplayDlg::OnBnClickedButtonCapture()
{
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == m_uiConnID)
	{
		return;
	}
	RECT rcShow = {0};
	GetDlgItem(IDC_BUTTON_CAPTURE)->GetWindowRect(&rcShow);
	CMenu menu;
	menu.CreatePopupMenu();
	menu.AppendMenu(MF_STRING,ID_SNATCH_BMP,_T("bmp"));
	menu.AppendMenu(MF_STRING,ID_SNATCH_JPG,_T("jpg"));
	menu.TrackPopupMenu(TPM_LEFTALIGN|TPM_RIGHTBUTTON, rcShow.left+2,rcShow.bottom,this);
	menu.DestroyMenu();
}


void CLS_CDisplayDlg::OnSnatch( UINT nID )
{

	CString strFileName = MakeCaptureName();
	switch(nID)
	{
	case ID_SNATCH_BMP:
		{
			strFileName.AppendFormat(_T(".bmp"));
			int iRet = NetClient_CapturePicture(m_uiConnID, 1, strFileName.GetBuffer());
			if( iRet < 0 )
			{
				::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_CAPTURE_FAILED, 0, 0);
			}
			else
			{
				::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_CAPTURE_SUCCESS, 0, 0);
			}
		}
		break;
	case ID_SNATCH_JPG:
		{
			strFileName.AppendFormat(_T(".jpg"));	
			int iRet = NetClient_CapturePicture(m_uiConnID, 2, strFileName.GetBuffer());
			if( iRet < 0 )
			{
				::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_CAPTURE_FAILED, 0, 0);
			}
			else
			{
				::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_CAPTURE_SUCCESS, 0, 0);
			}
		}
		break;
	default:
		break;
	}
}

CString CLS_CDisplayDlg::GetCurrentPath()
{
	CString strCurPath;
	char cCurPath[MAX_PATH] = {0};
	int iSize = GetModuleFileName(NULL,  (LPCH)cCurPath, sizeof(cCurPath));
	if (iSize <= 0)
	{
		strcpy_s(cCurPath,sizeof(cCurPath),"C:\\");
	}
	strCurPath.Format(_T("%s"),cCurPath);
	int iPos = strCurPath.ReverseFind('\\');
	if (iPos >= 0)
	{
		strCurPath = strCurPath.Left(iPos);
	}
	return strCurPath;
}

CString CLS_CDisplayDlg::MakeCaptureName()
{
	SYSTEMTIME tmNow = {0};
	GetLocalTime(&tmNow);
	CString	strFileName;
	strFileName.Format(_T("%s\\%04d%02d%02d%02d%02d%02d")
		,GetCurrentPath(),tmNow.wYear,tmNow.wMonth,tmNow.wDay,tmNow.wHour
		,tmNow.wMinute,tmNow.wSecond);
	return strFileName;
}


void CLS_CDisplayDlg::OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar)
{
	if( -1 == m_uiConnID)
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SET_VOLUME_FAILED, 0, 0);
		return;
	}
	if( FALSE == m_bAudioStart )
	{
		//Turn on audio
		int iPlayAudioFlag = 1;
		int iOutLen = -1;
		int iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_START_AUDIO, &iPlayAudioFlag, sizeof(int), NULL, &iOutLen );
		if( iRet < 0)
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VOLUME_CTRL_FAILED, 0, 0);
		}
		else
		{
			m_bAudioStart = TRUE;
			GetDlgItem(IDC_BUTTON_VOLUME_CTRL)->SetWindowText( FALSE == m_bLanguage ?_T("关闭声音") : _T("VolumeOff"));
		}
	}
	int iPos = m_sliVolumeCtr.GetPos();
	CString csTemp;
	int fTemp = (int)(iPos*100/65535.0);
	csTemp.Format(_T("%d"), fTemp); 
	m_txtVolumeDisplay.SetWindowText(csTemp + _T("%"));
	
	
	tPlaybackVolume stPlaybackVolume = {0};
	stPlaybackVolume.iSize = sizeof(tPlaybackVolume);
	stPlaybackVolume.usVolume = iPos;
	int iOutLen = -1;
	int iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_SET_VOLUME, &stPlaybackVolume, stPlaybackVolume.iSize, NULL, &iOutLen);
	if (0 == iRet)
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SET_VOLUME_SUCCESS, 0, 0);
	}
	else
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SET_VOLUME_FAILED, 0, 0);
	}
	CDialog::OnHScroll(nSBCode, nPos, pScrollBar);
}

void CLS_CDisplayDlg::OnBnClickedButtonForwardStep()
{
	if( -1 == m_uiConnID)
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FORWARD_STEP_FAILED, 0, 0);
		return;
	}
	int iRet = NetClient_PlayBackControl( m_uiConnID, PLAY_CONTROL_STEPFORWARD , NULL, 0 , NULL, 0 );
	if( 0 == iRet )
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FORWARD_STEP_SUCCESS, 0, 0);
	}
	else
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FORWARD_STEP_FAILED, 0, 0);
	}
}

//Turn sound on and off
void CLS_CDisplayDlg::OnBnClickedButtonVolumeCtrl()
{
	if( -1 == m_uiConnID )
	{
		return;
	}
	if( FALSE == m_bAudioStart )
	{
		//Turn on audio
		int iPlayAudioFlag = 1;
		int iOutLen = -1;
		int iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_START_AUDIO, &iPlayAudioFlag, sizeof(int), NULL, &iOutLen );
		if( iRet < 0)
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VOLUME_CTRL_FAILED, 0, 0);
			return;
		}
		else
		{
			
			tPlaybackVolume stPlaybackVolume = {0};
			stPlaybackVolume.iSize = sizeof(tPlaybackVolume);
			int iOutLen = -1;
			iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_GET_VOLUME, &stPlaybackVolume, 0, NULL, &iOutLen);
			if( iRet < 0)
			{
				::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VOLUME_CTRL_FAILED, 0, 0);
				return;
			}
			else
			{
				m_bAudioStart = TRUE;
				unsigned short iVolume = stPlaybackVolume.usVolume;
				m_sliVolumeCtr.SetPos(iVolume);
				CString csTemp;
				int fTemp = (int)(iVolume*100/65535.0);
				csTemp.Format(_T("%d"), fTemp); 
				m_txtVolumeDisplay.SetWindowText(csTemp + _T("%"));
				GetDlgItem(IDC_BUTTON_VOLUME_CTRL)->SetWindowText( FALSE == m_bLanguage ?_T("关闭声音") : _T("VolumeOff"));
				return;
			}
		}
	}
	if( TRUE == m_bAudioStart )
	{
		//turn off audio
		int iPlayAudioFlag = 0;
		int iOutLen = -1;
		int iRet = NetClient_PlayBackControl(m_uiConnID, PLAY_CONTROL_STOP_AUDIO, &iPlayAudioFlag, sizeof(int), NULL, &iOutLen );
		if( iRet < 0)
		{
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_VOLUME_CTRL_FAILED, 0, 0);
			return;
		}
		else
		{
			m_bAudioStart = FALSE;
			m_sliVolumeCtr.SetPos(0);
			m_txtVolumeDisplay.SetWindowText(_T(""));
			GetDlgItem(IDC_BUTTON_VOLUME_CTRL)->SetWindowText( FALSE == m_bLanguage ?_T("开启声音") : _T("VolumeOn"));
			return;
		}
	}
}

// OperateByTime.cpp : Implementation file
//

#include "stdafx.h"
#include <time.h>
#include "NetSdk.h"
#include "VideoDisplay.h"
#include "DisplayDlg.h"
#include "OperateByTimeDlg.h"
#include "ConnectServer.h"
#include "VideoDisplayDlg.h"
#include "MacroDefine.h"


CLS_OperateByTime* CLS_OperateByTime::m_sInstance = NULL;
CString CLS_OperateByTime::m_csChannelId = DOWNLOAD_DEMO_NULL_CHARACTER;
CString CLS_OperateByTime::m_csStreamType = DOWNLOAD_DEMO_NULL_CHARACTER;
CLS_OperateByTime::CLS_DeleteInstace CLS_OperateByTime::m_deleteInstace;
// COperateByTime dialog



IMPLEMENT_DYNAMIC(CLS_OperateByTime, CDialog)

CLS_OperateByTime::CLS_OperateByTime(CWnd* pParent /*=NULL*/)
	: CDialog(CLS_OperateByTime::IDD, pParent)
{

}

CLS_OperateByTime::~CLS_OperateByTime()
{

}

void CLS_OperateByTime::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_DATETIMEPICKER3, m_dtcStartTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER1, m_dtcStopTime);
	DDX_Control(pDX, IDC_COMBO_TIME_MODE_CHANNEL_NUM, m_cboChannelIDCtr);
	DDX_Control(pDX, IDC_PROGRESS1, m_pgrsProcessCtrl);
	DDX_Control(pDX, IDC_COMBO3, m_cboStreamType);
	DDX_Control(pDX, IDC_BUTTON7, m_btnStopDownload);
	DDX_Control(pDX, IDC_COMBO4, m_cboDownloadSpeed);
	DDX_Control(pDX, IDC_STATIC_PROCESS, m_txtProcecssCtrl);
	DDX_Control(pDX, IDC_COMBO_STREAM_SAVEFILE_TYPE, m_cboSaveFileType);
}


BEGIN_MESSAGE_MAP(CLS_OperateByTime, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_TIME_MODE_DOWNLOAD, &CLS_OperateByTime::OnBnClickedDownload)
	ON_BN_CLICKED(IDC_BUTTON2, &CLS_OperateByTime::OnBnClickedSpeedSet)
	ON_BN_CLICKED(IDC_BUTTON4, &CLS_OperateByTime::OnBnClickedPlay)
	ON_BN_CLICKED(IDC_BUTTON7, &CLS_OperateByTime::OnBnClickedStopDownload)
	ON_WM_TIMER()
	ON_STN_CLICKED(IDC_STATIC_PROCESS, &CLS_OperateByTime::OnStnClickedStaticProcess)
	ON_CBN_SELCHANGE(IDC_COMBO_TIME_MODE_DOWNLOAD_SPEED, &CLS_OperateByTime::OnCbnSelchangeComboTimeModeDownloadSpeed)
END_MESSAGE_MAP()


BOOL CLS_OperateByTime::OnInitDialog()
{
	CDialog::OnInitDialog();
	//initialize date format
	m_dtcStopTime.SetFormat( _T("''yyy'-'MM'-'dd' 'HH':'mm':'ss''") );
	m_dtcStartTime.SetFormat( _T("''yyy'-'MM'-'dd' 'HH':'mm':'ss''") );

	CTime tTemp;
	tTemp = CTime::GetCurrentTime();
	//CTime startTime(tTemp.GetYear(), tTemp.GetMonth(), tTemp.GetDay(), 0, 0, 0,0);
	CTime startTime(tTemp.GetYear(), tTemp.GetMonth(), 19, 13, 26, 0,0);
	m_dtcStartTime.SetTime( &startTime );
	
	CTime stopTime(tTemp.GetYear(), tTemp.GetMonth(), 19, 13, 30, 0, 0);
	m_dtcStopTime.SetTime( &stopTime );

	//Set the channel number drop-down menu
	m_cboChannelIDCtr.ResetContent();
	CString temp;
	for( int i = 0; i < DOWNLOAD_DEMO_CHANNEL_NUM_COUNTER; ++i )
	{
		temp.Format(_T("%d"),i ); 
		m_cboChannelIDCtr.AddString( temp );
	}
	m_cboChannelIDCtr.SetCurSel(0);
	//Set the stream type drop-down menu
	m_cboStreamType.ResetContent();
	m_cboStreamType.AddString(DOWNLOAD_DEMO_MAIN_STREAM);//0
	m_cboStreamType.AddString(DOWNLOAD_DEMO_SECONDARY_STREAM);//1
	m_cboStreamType.SetCurSel(0);
	//Initialize progress bar
	m_pgrsProcessCtrl.SetRange( DOWNLOAD_DEMO_INT_ZERO, DOWNLOAD_DEMO_PROCESS_RANGE );
	m_pgrsProcessCtrl.SetPos( DOWNLOAD_DEMO_INT_ZERO );

	m_cboDownloadSpeed.ResetContent();
	m_cboDownloadSpeed.AddString(DOWNLOAD_DEMO_ONE);
	m_cboDownloadSpeed.AddString(DOWNLOAD_DEMO_DOUBLE);
	m_cboDownloadSpeed.AddString(DOWNLOAD_DEMO_QUADRUPLE);
	m_cboDownloadSpeed.AddString(DOWNLOAD_DEMO_EIGHT);
	m_cboDownloadSpeed.SetCurSel(0);

	m_cboSaveFileType.ResetContent();
	m_cboSaveFileType.InsertString(0, "sdv");
	m_cboSaveFileType.InsertString(1, "ps");
	m_cboSaveFileType.InsertString(2, "mp4");
	m_cboSaveFileType.InsertString(3, "avi");
	m_cboSaveFileType.SetCurSel(0);

	SetTimer( ID_TIMER_TIME_DOWNLOAD_POS, 100, NULL ); //100 ms
	m_bLanguage = FALSE;
	return TRUE;  // return TRUE unless you set the focus to a control
}


void CLS_OperateByTime::OnLanguageChange(int _iLanguage)
{
	GetDlgItem(IDC_STATIC_TIME_MODE_TIME_SPACE)->SetWindowText( 0 == _iLanguage ?_T("时间范围") : _T("Time"));
	GetDlgItem(IDC_STATIC_TIME_MODE_TO)->SetWindowText( 0 == _iLanguage ?_T("至") : _T("To"));
	GetDlgItem(IDC_STATIC_TIME_MODE_CHANNEL_NUM)->SetWindowText( 0 == _iLanguage ?_T("通道号") : _T("ChannNo"));
	GetDlgItem(IDC_BUTTON_TIME_MODE_DOWNLOAD)->SetWindowText( 0 == _iLanguage ?_T("下载") : _T("Download"));
	GetDlgItem(IDC_BUTTON_TIME_MODE_STOP_DOWNLAOD)->SetWindowText( 0 == _iLanguage ?_T("停止下载") : _T("Stop"));
	GetDlgItem(IDC_STATIC_TIME_MODE_DWONLOAD_SPEED)->SetWindowText( 0 == _iLanguage ?_T("下载速度") : _T("Speed"));
	GetDlgItem(IDC_BUTTON_TIME_MODE_SET_DOWN_LAOD_SPEED)->SetWindowText( 0 == _iLanguage ?_T("设置") : _T("Set"));
	GetDlgItem(IDC_BUTTON_TIME_MODE_PLAY)->SetWindowText( 0 == _iLanguage ?_T("播放") : _T("Play"));
	GetDlgItem(IDC_STATIC_TIME_MODE_DOWNLAOD_POS)->SetWindowText( 0 == _iLanguage ?_T("下载进度") : _T("DownloadPos(0-100)"));

	int iLogonId = CLS_ConnectServer::GetInstance()->m_iLogonID;
	int iChanNum = DOWNLOAD_DEMO_CHANNEL_NUM_COUNTER;
	if (iLogonId >= 0)
	{
		NetClient_GetChannelNum(iLogonId, &iChanNum);
	}

	//Set the channel number drop-down menu
	m_cboChannelIDCtr.ResetContent();
	CString temp;
	for( int i = 0; i < iChanNum; ++i )
	{
		temp.Format(_T("%d"),i ); 
		m_cboChannelIDCtr.AddString( temp );
	}
	m_cboChannelIDCtr.SetCurSel(0);
	
	if( 0 == _iLanguage)
	{
		m_bLanguage = FALSE;	
		//Set the stream type drop-down menu
		m_cboStreamType.ResetContent();
		m_cboStreamType.AddString(DOWNLOAD_DEMO_MAIN_STREAM);//0
		m_cboStreamType.AddString(DOWNLOAD_DEMO_SECONDARY_STREAM);//1
		m_cboStreamType.SetCurSel(0);
	}
	else if( 1 == _iLanguage)
	{
		m_bLanguage = TRUE;
		//Set the stream type drop-down menu
		m_cboStreamType.ResetContent();
		m_cboStreamType.AddString(DOWNLOAD_DEMO_MAIN_STREAM_EN);//0
		m_cboStreamType.AddString(DOWNLOAD_DEMO_SECONDARY_STREAM_EN);//1
		m_cboStreamType.SetCurSel(0);
	}

}
//convert time to timestamp
unsigned int CLS_OperateByTime::NvsFileTimeToAbsSeconds( NVS_FILE_TIME * _pFileTime )
{
	tm tm_Begin = {0};
	tm_Begin.tm_year = _pFileTime->iYear - 1900;
	tm_Begin.tm_mon  = _pFileTime->iMonth - 1;
	tm_Begin.tm_mday = _pFileTime->iDay;
	tm_Begin.tm_hour = _pFileTime->iHour;
	tm_Begin.tm_min  = _pFileTime->iMinute;
	tm_Begin.tm_sec  = _pFileTime->iSecond;

	time_t timenow, timeSpace;
	tm  pTimeg= {0} ;
	time(&timenow);
	gmtime_s(&pTimeg, &timenow);
	timeSpace = timenow - mktime(&pTimeg);
	time_t begin = mktime(&tm_Begin) + timeSpace;
	unsigned int uiSeconds = (unsigned int)begin;
	return uiSeconds;
}

// COperateByTime message handler

//Download button message processing
void CLS_OperateByTime::OnBnClickedDownload()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		GetQueryCondition();
		DownloadFileByTime( CLS_ConnectServer::GetInstance()->m_iLogonID, 8 );
	}
}
int CLS_OperateByTime::DownloadFileByTime( int _iLogonID, int _iSpeed )
{
	int iChannelNo = 0;
	if( DOWNLOAD_DEMO_ALL == m_csChannelId || DOWNLOAD_DEMO_ALL_EN == m_csChannelId)
	{
		iChannelNo = DOWNLOAD_DEMO_CHANNEL_NUM_ALL;
	}
	else
	{
		iChannelNo = _ttoi( m_csChannelId );
	}
	int iStreamType = -1;
	if( _T("主码流") == m_csStreamType || DOWNLOAD_DEMO_MAIN_STREAM_EN == m_csStreamType)
	{
		iStreamType = 0;
	}
	if( _T("副码流") == m_csStreamType || DOWNLOAD_DEMO_SECONDARY_STREAM_EN == m_csStreamType )
	{
		iStreamType = 1;
	}
	
	int iChannelNum = 0;
	NetClient_GetChannelNum( CLS_ConnectServer::GetInstance()->m_iLogonID, &iChannelNum);
	int iRealChannel = iStreamType*iChannelNum + iChannelNo;
	DOWNLOAD_TIMESPAN tTemp = {0};
	char cTmpName[128] = {0};
	sprintf_s(cTmpName,"./%d%d%d%d%d%d-%d%d%d%d%d%d.",
				m_nvsStartTime.iYear,
				m_nvsStartTime.iMonth,
				m_nvsStartTime.iDay,
				m_nvsStartTime.iHour,
				m_nvsStartTime.iMinute,
				m_nvsStartTime.iSecond,
				m_nvsStopTime.iYear,
				m_nvsStopTime.iMonth ,
				m_nvsStopTime.iDay ,
				m_nvsStopTime.iHour ,
				m_nvsStopTime.iMinute,
				m_nvsStopTime.iSecond);

	if (0 == m_cboSaveFileType.GetCurSel()) {
		sprintf_s(tTemp.m_cLocalFilename,"%ssdv", cTmpName);
		tTemp.m_iSaveFileType = DOWNLOAD_FILE_TYPE_SDV;
	} else if (1 == m_cboSaveFileType.GetCurSel()) {
		sprintf_s(tTemp.m_cLocalFilename,"%sps", cTmpName);
		tTemp.m_iSaveFileType = DOWNLOAD_FILE_TYPE_PS;
	} else if (2 == m_cboSaveFileType.GetCurSel()) {
		sprintf_s(tTemp.m_cLocalFilename,"%smp4", cTmpName);
		tTemp.m_iSaveFileType = DOWNLOAD_FILE_TYPE_ZFMP4;
	} else if (3 == m_cboSaveFileType.GetCurSel()) {
		sprintf_s(tTemp.m_cLocalFilename,"%savi", cTmpName);
		tTemp.m_iSaveFileType = DOWNLOAD_FILE_TYPE_AVI;
	}

	tTemp.m_iSize = sizeof(DOWNLOAD_TIMESPAN);
	int iSz = sizeof(DOWNLOAD_FILE);
	//tTemp.m_iPosition = -1;
	tTemp.m_iSpeed = _iSpeed;
	tTemp.m_iReqMode = 1;
	tTemp.m_iChannelNO = iRealChannel;
	tTemp.m_tTimeBegin = m_nvsStartTime;
	tTemp.m_tTimeEnd = m_nvsStopTime;
	int iRet = NetClient_NetFileDownload(  &(CLS_ConnectServer::GetInstance()->m_uiConnID), 
											(CLS_ConnectServer::GetInstance()->m_iLogonID), 
											DOWNLOAD_CMD_TIMESPAN,
											&tTemp,
											sizeof(DOWNLOAD_TIMESPAN));
	//int iRet = NetClient_NetFileDownloadByTimeSpanEx(&(CLS_ConnectServer::GetInstance()->m_uiConnID),(CLS_ConnectServer::GetInstance()->m_iLogonID),tTemp.m_cLocalFilename,iChannelNo,&m_nvsStartTime,&m_nvsStopTime, 0, -1, 1);//录像下载

	if(iRet < 0)
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_DOWNLOAD_BY_TIME_FAILED, 0, 0);
	}
	return 0;
}
//Get query conditions
void CLS_OperateByTime::GetQueryCondition()
{
	//get start time
	CTime startTime, stopTime;
	m_dtcStartTime.GetTime( startTime );
	m_nvsStartTime.iYear = startTime.GetYear();
	m_nvsStartTime.iMonth = startTime.GetMonth();
	m_nvsStartTime.iDay = startTime.GetDay();
	m_nvsStartTime.iHour = startTime.GetHour();
	m_nvsStartTime.iMinute = startTime.GetMinute();
	m_nvsStartTime.iSecond = startTime.GetSecond();
	// get the deadline
	m_dtcStopTime.GetTime( stopTime );
	m_nvsStopTime.iYear = stopTime.GetYear();
	m_nvsStopTime.iMonth = stopTime.GetMonth();
	m_nvsStopTime.iDay = stopTime.GetDay();
	m_nvsStopTime.iHour = stopTime.GetHour();
	m_nvsStopTime.iMinute = stopTime.GetMinute();
	m_nvsStopTime.iSecond = stopTime.GetSecond();

	//get timestamp
	m_tStop = NvsFileTimeToAbsSeconds(&m_nvsStopTime);
	m_tStart = NvsFileTimeToAbsSeconds(&m_nvsStartTime);
	if( m_tStop <= m_tStart )
	{
		MessageBox(DOWNLOAD_DEMO_INPUT_TIME_SPACE);
		return;
	}
	//Get the input channel number
	int nIndex = m_cboChannelIDCtr.GetCurSel();
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == nIndex )
	{
		m_cboChannelIDCtr.GetWindowText( m_csChannelId);
	}
	else
	{
		m_cboChannelIDCtr.GetLBText( nIndex, m_csChannelId );
	}
	nIndex = m_cboStreamType.GetCurSel();
	if( -1 == nIndex )
	{
		m_cboStreamType.GetWindowText( m_csStreamType);
	}
	else
	{
		m_cboStreamType.GetLBText( nIndex, m_csStreamType );
	}
}


void CLS_OperateByTime::SetSpeed()
{
	int iChannelNo = _ttoi( m_csChannelId );
	int iStreamType = m_cboStreamType.GetCurSel();
	int iChannelNum = 0;
	NetClient_GetChannelNum( CLS_ConnectServer::GetInstance()->m_iLogonID, &iChannelNum);
	int iRealChannel = iStreamType*iChannelNum + iChannelNo;

	char cLocalFileName[MAX_PATH] = {0};
	sprintf_s(cLocalFileName,"./%d%d%d%d%d%d-%d%d%d%d%d%d.sdv",
		m_nvsStartTime.iYear,
		m_nvsStartTime.iMonth,
		m_nvsStartTime.iDay,
		m_nvsStartTime.iHour,
		m_nvsStartTime.iMinute,
		m_nvsStartTime.iSecond,
		m_nvsStopTime.iYear,
		m_nvsStopTime.iMonth ,
		m_nvsStopTime.iDay ,
		m_nvsStopTime.iHour ,
		m_nvsStopTime.iMinute,
		m_nvsStopTime.iSecond);
	
	m_cboDownloadSpeed.GetWindowText(m_csDownloadSpeed);
	int iRet = NetClient_NetFileDownloadByTimeSpanEx(&(CLS_ConnectServer::GetInstance()->m_uiConnID),
							(CLS_ConnectServer::GetInstance()->m_iLogonID),
							cLocalFileName,iChannelNo,
							&m_nvsStartTime,&m_nvsStopTime
							, 1, -1, _ttoi(m_csDownloadSpeed));//Video download
	if(iRet < 0)
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_SPEED_SET_FAILED, 0, 0);;
	}
}

//Download speed setting button response
void CLS_OperateByTime::OnBnClickedSpeedSet()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		m_cboDownloadSpeed.GetWindowText(m_csDownloadSpeed);
		SetSpeed();
	}
}
//Play button message processing
void CLS_OperateByTime::OnBnClickedPlay()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{

		GetQueryCondition();
		GetInstance()->m_nvsStartTime.iYear = m_nvsStartTime.iYear;
		GetInstance()->m_nvsStartTime.iMonth = m_nvsStartTime.iMonth;
		GetInstance()->m_nvsStartTime.iDay = m_nvsStartTime.iDay;
		GetInstance()->m_nvsStartTime.iHour = m_nvsStartTime.iHour;
		GetInstance()->m_nvsStartTime.iMinute = m_nvsStartTime.iMinute;
		GetInstance()->m_nvsStartTime.iSecond = m_nvsStartTime.iSecond;

		GetInstance()->m_nvsStopTime.iYear = m_nvsStopTime.iYear;
		GetInstance()->m_nvsStopTime.iMonth = m_nvsStopTime.iMonth;
		GetInstance()->m_nvsStopTime.iDay = m_nvsStopTime.iDay;
		GetInstance()->m_nvsStopTime.iHour = m_nvsStopTime.iHour;
		GetInstance()->m_nvsStopTime.iMinute = m_nvsStopTime.iMinute;
		GetInstance()->m_nvsStopTime.iSecond = m_nvsStopTime.iSecond;
		if( FALSE == m_bLanguage)
		{
			CLS_CDisplayDlg displayDialog;
			displayDialog.SetConfig(FALSE, FALSE);
			displayDialog.DoModal();	
		}
		if( TRUE == m_bLanguage)
		{
			CLS_CDisplayDlg displayDialog;
			displayDialog.SetConfig(FALSE, TRUE);
			displayDialog.DoModal();	
		}
		
	}
}

//Stop download button message processing
void CLS_OperateByTime::OnBnClickedStopDownload()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		m_txtProcecssCtrl.SetWindowText(_T(""));
		//KillTimer( ID_TIMER_TIME_DOWNLOAD_POS );
		if( 0 == NetClient_NetFileStopDownloadFile( CLS_ConnectServer::GetInstance()->m_uiConnID ))
		{
			m_pgrsProcessCtrl.SetPos( 0 );
		}
	}
}

CString CLS_OperateByTime::IntToCString( int _iData )
{
	CString strData;
	strData.Format(_T("%d"),_iData);
	return strData;
}

void CLS_OperateByTime::OnTimer(UINT_PTR nIDEvent)
{
	switch(nIDEvent)
	{
	case ID_TIMER_TIME_DOWNLOAD_POS:
		{
			if( FALSE == CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
			{
				m_pgrsProcessCtrl.SetPos(0);
				NetClient_NetFileStopDownloadFile( CLS_ConnectServer::GetInstance()->m_uiConnID );
			}
			int iFileSize = 0;
			int	iDownloadPos = 0;
			int iRet = NetClient_NetFileGetDownloadPos( CLS_ConnectServer::GetInstance()->m_uiConnID, &iDownloadPos, &iFileSize );
			int iTimeInterval = m_tStop - m_tStart;
			if( iTimeInterval > 0)
			{
				int iCurrentInterval = iDownloadPos - m_tStart;
				if ( (iCurrentInterval >= 0) && (iCurrentInterval <= iTimeInterval))
				{
					int iProcess = iCurrentInterval*100/iTimeInterval;
					//m_txtProcecssCtrl.SetWindowText(IntToCString(iProcess));
					m_pgrsProcessCtrl.SetPos(iProcess);
				}
			}			
			break;
		}
	default:
		KillTimer( nIDEvent );
		break;
	}
	CDialog::OnTimer(nIDEvent);
}

void CLS_OperateByTime::OnStnClickedStaticProcess()
{
	// TODO: Add control notification handler code here
}



void CLS_OperateByTime::OnCbnSelchangeComboTimeModeDownloadSpeed()
{
	// TODO: Add control notification handler code here
}

void CLS_OperateByTime::OnLogonSucc()
{
	int iLogonId = CLS_ConnectServer::GetInstance()->m_iLogonID;
	int iChanNum = DOWNLOAD_DEMO_CHANNEL_NUM_COUNTER;
	if (iLogonId >= 0)
	{
		NetClient_GetChannelNum(iLogonId, &iChanNum);
	}

	//Set the channel number drop-down menu
	m_cboChannelIDCtr.ResetContent();
	CString temp;
	for( int i = 0; i < iChanNum; ++i )
	{
		temp.Format(_T("%d"),i ); 
		m_cboChannelIDCtr.AddString( temp );
	}
	m_cboChannelIDCtr.SetCurSel(0);
}
// OperateByFileDlg.cpp : implementation file
//

#include "stdafx.h"
#include <vector>
#include <algorithm>
#include "VideoDisplay.h"
#include "OperateByFileDlg.h"
#include "ConnectServer.h"
#include "VideoDisplayDlg.h"
#include "MacroDefine.h"

using namespace std;

CString CLS_OperateByFileDlg::m_sDownLoadFileName = DOWNLOAD_DEMO_NULL_CHARACTER;
map<CString, int> CLS_OperateByFileDlg::m_mapConnId = map<CString, int>();
// COperateByFileDlg dialog

#define MAX_PAGESIZE    20

IMPLEMENT_DYNAMIC(CLS_OperateByFileDlg, CDialog)

CLS_OperateByFileDlg::CLS_OperateByFileDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CLS_OperateByFileDlg::IDD, pParent)
{
	m_iFileCounter = 0;
	m_iRowNum = 0;
	m_iLanguage = 0;
	m_iDownloadSize = 0;
	m_iDownloadPos = 0;
	m_csDownloadPos = DOWNLOAD_DEMO_NULL_CHARACTER;
	m_csDownloadSize = DOWNLOAD_DEMO_NULL_CHARACTER;
	m_iCurrentPage = 0;
}

CLS_OperateByFileDlg::~CLS_OperateByFileDlg()
{
}

void CLS_OperateByFileDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FILE_MODE_START_TIME, m_dtpStartTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FILE_MODE_STOP_TIME, m_dtpStopTime);
	DDX_Control(pDX, IDC_COMBO_FILE_MODE_CHANNEL, m_cboChannelNum);
	DDX_Control(pDX, IDC_COMBO_FILE_MODE_FILE_TYPE, m_cboFileType);
	DDX_Control(pDX, IDC_FILE_MODE_DOWNLOAD_POS, m_edtDownloadPos);
	DDX_Control(pDX, IDC_LIST3, m_lstDownloadFile);
	DDX_Control(pDX, IDC_COMBO_FILE_MODE_CHANNEL2, m_cboDownLoadSpeed);
	DDX_Control(pDX, IDC_COMBO_FILE_MODE_SREAM, m_cboStreamNo);
	DDX_Control(pDX, IDC_COMBO_FILE_SAVEFILE_TYPE, m_cboSaveFileType);
	DDX_Control(pDX, IDC_COMBO_FILE_PAGE, m_cboPageNo);
}


BEGIN_MESSAGE_MAP(CLS_OperateByFileDlg, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_QUERY, &CLS_OperateByFileDlg::OnBnClickedQuery)
	ON_BN_CLICKED(IDC_BUTTON_DOWNLOAD, &CLS_OperateByFileDlg::OnBnClickedDownload)
	ON_BN_CLICKED(IDC_BUTTON_PLAY, &CLS_OperateByFileDlg::OnBnClickedPlay)
	ON_BN_CLICKED(IDC_BUTTON_DOWNLOAD_SPEED, &CLS_OperateByFileDlg::OnBnClickedDownloadSpeedSet)
	ON_BN_CLICKED(IDC_BUTTON_DOWNLOAD_POS, &CLS_OperateByFileDlg::OnBnClickedDownloadPosSet)
	
	ON_WM_TIMER()
	ON_BN_CLICKED(IDC_BUTTON_STOP_DOWNLOAD, &CLS_OperateByFileDlg::OnBnClickedButtonStopDownload)
	ON_BN_CLICKED(IDC_BUTTON_PAUSE, &CLS_OperateByFileDlg::OnBnClickedPauseDownload)
	ON_BN_CLICKED(IDC_BUTTON_CONTINUE_DOWNLOAD, &CLS_OperateByFileDlg::OnBnClickedContinueDownload)
	ON_WM_DESTROY()
	ON_CBN_SELCHANGE(IDC_COMBO_FILE_PAGE, &CLS_OperateByFileDlg::OnCbnSelchangeComboFilePage)
END_MESSAGE_MAP()


BOOL CLS_OperateByFileDlg::OnInitDialog()
{
	CDialog::OnInitDialog();
	InitDlg();

	return TRUE;  // return TRUE unless you set the focus to a control
	
}


// COperateByFileDlg message handler
void CLS_OperateByFileDlg::InitDlg()
{
	//initialize date format
	m_dtpStartTime.SetFormat(_T("''yyy'-'MM'-'dd' 'HH':'mm':'ss''"));
	m_dtpStopTime.SetFormat(_T("''yyy'-'MM'-'dd' 'HH':'mm':'ss''"));
	//Set the default time range
	CTime temp;
	temp = CTime::GetCurrentTime();
	CTime startTime( temp.GetYear(), temp.GetMonth(), temp.GetDay(),0, 0, 0);
	m_dtpStartTime.SetTime(&startTime);
	CTime stopTime(temp.GetYear(), temp.GetMonth(), temp.GetDay(),23, 59, 59);
	m_dtpStopTime.SetTime(&stopTime);
	
	//Set the channel number drop-down menu
	m_cboChannelNum.ResetContent();
	CString csTemp;
	m_cboChannelNum.AddString(DOWNLOAD_DEMO_ALL);
	for( int i = 0; i < DOWNLOAD_DEMO_CHANNEL_NUM_COUNTER; ++i )
	{
		csTemp.Format(_T("%d"),i ); 
		m_cboChannelNum.AddString( csTemp );
	}
	m_cboChannelNum.SetCurSel(0);
	//Set the file type drop-down menu
	m_cboFileType.ResetContent();
	m_cboFileType.AddString(DOWNLOAD_DEMO_ALL);//0
	m_cboFileType.AddString(DOWNLOAD_DEMO_FILE_TYPE_VIDOE);//1
	m_cboFileType.AddString(DOWNLOAD_DEMO_FILE_TYPE_PICTURE);//2
	m_cboFileType.SetCurSel(1);
	m_edtDownloadPos.SetWindowText(DOWNLOAD_DEMO_GEGATIVE_ONE);

	//initialize grid
	DWORD dwStyle = m_lstDownloadFile.GetExtendedStyle();
	//Select a row to highlight the entire line (report style)
	dwStyle |= LVS_EX_FULLROWSELECT;
	//Grid lines (report style)
	dwStyle |= LVS_EX_GRIDLINES; 
	dwStyle |= WS_HSCROLL;
	//m_lstDownloadFile.ShowScrollBar(SB_HORZ, FALSE);
	//m_lstDownloadFile.SetColumnWidth(1,LVSCW_AUTOSIZE_USEHEADER); 
	
	//Set the extension style
	m_lstDownloadFile.SetExtendedStyle(dwStyle); 
	m_lstDownloadFile.InsertColumn( 0, DOWNLOAD_DEMO_FILE_INFO_NAME, LVCFMT_LEFT, 180);
	m_lstDownloadFile.InsertColumn( 1, DOWNLOAD_DEMO_FILE_INFO_DOWNLOAD_PROCESS, LVCFMT_LEFT, 180);
	m_lstDownloadFile.InsertColumn( 2, DOWNLOAD_DEMO_FILE_INFO_VIDEO_TYPE, LVCFMT_LEFT, 80);
	m_lstDownloadFile.InsertColumn( 3, DOWNLOAD_DEMO_FILE_INFO_CHANNEL_NUL, LVCFMT_LEFT, 80);
	m_lstDownloadFile.InsertColumn( 4, DOWNLOAD_DEMO_FILE_INFO_SIZE, LVCFMT_LEFT, 80);
	m_lstDownloadFile.InsertColumn( 5, DOWNLOAD_DEMO_FILE_INFO_START_TIME, LVCFMT_LEFT, 180);
	m_lstDownloadFile.InsertColumn( 6, DOWNLOAD_DEMO_FILE_INFO_STOP_TIME, LVCFMT_LEFT, 180);
	
	m_lstDownloadFile.ShowScrollBar(SB_HORZ, TRUE);
	

	//set download speed
	m_cboDownLoadSpeed.ResetContent();
	m_cboDownLoadSpeed.AddString(DOWNLOAD_DEMO_ONE);
	m_cboDownLoadSpeed.AddString(DOWNLOAD_DEMO_DOUBLE);
	m_cboDownLoadSpeed.AddString(DOWNLOAD_DEMO_QUADRUPLE);
	m_cboDownLoadSpeed.AddString(DOWNLOAD_DEMO_EIGHT);
	m_cboDownLoadSpeed.SetCurSel(0);

	SetTimer( ID_TIMER_FILE_DOWNLOAD_POS, 500, NULL ); //50 ms

	m_cboStreamNo.ResetContent();
	m_cboStreamNo.InsertString(0, "1st");
	m_cboStreamNo.InsertString(1, "2nd");
	m_cboStreamNo.SetCurSel(0);

	m_cboSaveFileType.ResetContent();
	m_cboSaveFileType.InsertString(0, "sdv");
	m_cboSaveFileType.InsertString(1, "ps");
	m_cboSaveFileType.InsertString(2, "mp4");
	m_cboSaveFileType.InsertString(3, "avi");
	m_cboSaveFileType.SetCurSel(0);
}

void CLS_OperateByFileDlg::OnLanguageChange(int _iLanguage)
{
	m_lstDownloadFile.DeleteAllItems();
	m_mapConnId.clear();
	m_mapcsPause.clear();
	m_mapRowNum.clear();
	GetDlgItem(IDC_STATIC_FILE_MODE_TIMESPACE)->SetWindowText( 0 == _iLanguage ?_T("时间范围") : _T("Time"));
	GetDlgItem(IDC_STATIC_FILE_MODE_TO)->SetWindowText( 0 == _iLanguage ?_T("至") : _T("To"));
	GetDlgItem(IDC_STATIC_FILE_MODE_CHANNEL_NUM)->SetWindowText( 0 == _iLanguage ?_T("通道号") : _T("ChannNo"));
	GetDlgItem(IDC_STATIC_FILE_MODE_FILE_TYPE)->SetWindowText( 0 == _iLanguage ?_T("文件类型") : _T("FileType"));
	GetDlgItem(IDC_BUTTON_QUERY)->SetWindowText( 0 == _iLanguage ?_T("查询") : _T("Query"));
	GetDlgItem(IDC_BUTTON_DOWNLOAD)->SetWindowText( 0 == _iLanguage ?_T("下载") : _T("Download"));
	GetDlgItem(IDC_STATIC_FILE_MODE_DOWNLOAD_SPEED)->SetWindowText( 0 == _iLanguage ?_T("下载速度") : _T("   Speed"));
	GetDlgItem(IDC_BUTTON_DOWNLOAD_SPEED)->SetWindowText( 0 == _iLanguage ?_T("设置") : _T("Set"));
	GetDlgItem(IDC_STATIC_FILE_MODE_DOWNLOAD_POS)->SetWindowText( 0 == _iLanguage ?_T("下载进度") : _T("DownloadPos(0-100)"));
	GetDlgItem(IDC_BUTTON_DOWNLOAD_POS)->SetWindowText( 0 == _iLanguage ?_T("设置") : _T("Set"));
	GetDlgItem(IDC_BUTTON_PLAY)->SetWindowText( 0 == _iLanguage ?_T("播放") : _T("Play"));
	GetDlgItem(IDC_BUTTON_STOP_DOWNLOAD)->SetWindowText( 0 == _iLanguage ?_T("停止下载") : _T("Stop"));
	GetDlgItem(IDC_BUTTON_PAUSE)->SetWindowText( 0 == _iLanguage ?_T("暂停下载") : _T("Pause"));
	GetDlgItem(IDC_BUTTON_CONTINUE_DOWNLOAD)->SetWindowText( 0 == _iLanguage ?_T("继续下载") : _T("Continue"));
	GetDlgItem(IDC_STATIC_STREAM_TYPE)->SetWindowText( 0 == _iLanguage ?_T("码流类型") : _T("StreamType"));

	int iLogonId = CLS_ConnectServer::GetInstance()->m_iLogonID;
	int iChanNum = DOWNLOAD_DEMO_CHANNEL_NUM_COUNTER;
	if (iLogonId >= 0)
	{
		NetClient_GetChannelNum(iLogonId, &iChanNum);
	}

	//Set the channel number drop-down menu
	m_cboChannelNum.ResetContent();
	m_cboChannelNum.AddString((1 == _iLanguage)? _T("ALL") : DOWNLOAD_DEMO_ALL);
	CString temp;
	for( int i = 0; i < iChanNum; ++i )
	{
		temp.Format(_T("%d"),i ); 
		m_cboChannelNum.AddString( temp );
	}
	m_cboChannelNum.SetCurSel(0);

	if( 1 == _iLanguage)
	{
		m_iLanguage = 1;		
		m_cboFileType.ResetContent();
		m_cboFileType.AddString(_T("ALL"));//0
		m_cboFileType.AddString(_T("Video"));//1
		m_cboFileType.AddString(_T("Picture"));//2
		m_cboFileType.SetCurSel(1);

		LVCOLUMN col;
		for(int i = 0; i < 7; i++)
		{
			ZeroMemory(&col, sizeof(col));
			char cText[64] = {0};
			col.mask = LVCF_TEXT;//This sentence is necessary to prevent crashes under release
			col.pszText = cText;//This sentence is necessary to prevent crashes under release
			col.cchTextMax = 64;//This sentence is necessary to prevent crashes under release
			m_lstDownloadFile.GetColumn(i, &col);
			switch(i)
			{
				case 0:
					col.pszText = _T("FileName");
					m_lstDownloadFile.SetColumn(0, &col);
					break;
				case 1:
					col.pszText = _T("DownloadProcess");
					m_lstDownloadFile.SetColumn(1, &col);
					break;
				case 2:
					col.pszText = _T("RecType");
					m_lstDownloadFile.SetColumn(2, &col);
					break;
				case 3:
					col.pszText = _T("Channel No");
					m_lstDownloadFile.SetColumn(3, &col);
					break;
				case 4:
					col.pszText = _T("FileSize");
					m_lstDownloadFile.SetColumn(4, &col);
					break;
				case 5:
					col.pszText = _T("StartTime");
					m_lstDownloadFile.SetColumn(5, &col);
					break;
				case 6:
					col.pszText = _T("EndTime");
					m_lstDownloadFile.SetColumn(6, &col);
					break;
				default:
					break;

			}

		}
	}
	if( 0 == _iLanguage)
	{
		m_iLanguage = 0;

		m_cboFileType.ResetContent();
		m_cboFileType.AddString(DOWNLOAD_DEMO_ALL);//0
		m_cboFileType.AddString(DOWNLOAD_DEMO_FILE_TYPE_VIDOE);//1
		m_cboFileType.AddString(DOWNLOAD_DEMO_FILE_TYPE_PICTURE);//2
		m_cboFileType.SetCurSel(1);

		LVCOLUMN col;
		for(int i = 0; i < 7; i++)
		{
			ZeroMemory(&col, sizeof(col));
			char cText[64] = {0};
			col.mask = LVCF_TEXT;//This sentence is necessary to prevent crashes under release
			col.pszText = cText;//This sentence is necessary to prevent crashes under release
			col.cchTextMax = 64;//This sentence is necessary to prevent crashes under release
			m_lstDownloadFile.GetColumn(i, &col);
			switch(i)
			{
			case 0:
				col.pszText = _T("文件名");
				m_lstDownloadFile.SetColumn(0, &col);
				break;
			case 1:
				col.pszText = _T("下载进度");
				m_lstDownloadFile.SetColumn(1, &col);
				break;
			case 2:
				col.pszText = _T("文件类型");
				m_lstDownloadFile.SetColumn(2, &col);
				break;
			case 3:
				col.pszText = _T("通道号");
				m_lstDownloadFile.SetColumn(3, &col);
				break;
			case 4:
				col.pszText = _T("文件大小");
				m_lstDownloadFile.SetColumn(4, &col);
				break;
			case 5:
				col.pszText = _T("开始时间");
				m_lstDownloadFile.SetColumn(5, &col);
				break;
			case 6:
				col.pszText = _T("结束时间");
				m_lstDownloadFile.SetColumn(6, &col);
				break;
			default:
				break;

			}

		}
	}
	
}

void CLS_OperateByFileDlg::DisplayFileInfo(int _iFileTotalCount)
{
	int iTotalPage = (_iFileTotalCount+MAX_PAGESIZE-1)/MAX_PAGESIZE;

	m_cboPageNo.ResetContent();
	for (int i = 0; i < iTotalPage; i++)
	{
		CString strPage;
		strPage.Format("%d", i+1);
		m_cboPageNo.AddString(strPage);
	}
	m_cboPageNo.SetCurSel(m_iCurrentPage);

	m_lstDownloadFile.InsertItem( m_iFileCounter ,  CLS_ConnectServer::m_sFileInfo[m_iFileCounter].cFileName ); //first row, first column
	CString csTemp;
	//1-Manual record, 2-Schedule record, 3-Alarm record
	if( DOWNLOAD_DEMO_INT_ONE == CLS_ConnectServer::m_sFileInfo[m_iFileCounter].iType )
	{
		if( DOWNLOAD_DEMO_INT_ZERO == m_iLanguage )
		{
			m_lstDownloadFile.SetItemText( m_iFileCounter, 2, DOWNLOAD_DEMO_FILE_INFO_MANNUL_RECORD );
		}
		if( DOWNLOAD_DEMO_INT_ONE == m_iLanguage)
		{
			m_lstDownloadFile.SetItemText( m_iFileCounter, 2, DOWNLOAD_DEMO_FILE_INFO_MANNUL_RECORD_EN );
		}
		
	}
	
	if( DOWNLOAD_DEMO_INT_TWO == CLS_ConnectServer::m_sFileInfo[m_iFileCounter].iType )
	{
		if( DOWNLOAD_DEMO_INT_ZERO == m_iLanguage)
		{
			m_lstDownloadFile.SetItemText( m_iFileCounter, 2, DOWNLOAD_DEMO_FILE_INFO_SCHEDULE_RECORD );
		}
		if( DOWNLOAD_DEMO_INT_ONE == m_iLanguage)
		{
			m_lstDownloadFile.SetItemText( m_iFileCounter, 2, DOWNLOAD_DEMO_FILE_INFO_SCHEDULE_RECORD_EN );
		}
		
	}
	if( DOWNLOAD_DEMO_INT_THREE == CLS_ConnectServer::m_sFileInfo[m_iFileCounter].iType )
	{
		if( DOWNLOAD_DEMO_INT_ZERO == m_iLanguage)
		{
			m_lstDownloadFile.SetItemText( m_iFileCounter, 2, DOWNLOAD_DEMO_FILE_INFO_ALARM_RECORD );
		}
		if( DOWNLOAD_DEMO_INT_ONE == m_iLanguage)
		{
			m_lstDownloadFile.SetItemText( m_iFileCounter, 2, DOWNLOAD_DEMO_FILE_INFO_ALARM_RECORD_EN );
		}
		
	}
	//m_lstDownloadFile.SetItemText( m_iFileCounter, 1,csTemp );//first row, second column
	csTemp.Format(_T("%d"), CLS_ConnectServer::m_sFileInfo[m_iFileCounter].iChannel );
	m_lstDownloadFile.SetItemText( m_iFileCounter, 3, csTemp );//first row, third column
	csTemp.Format(_T("%d"), CLS_ConnectServer::m_sFileInfo[m_iFileCounter].iFileSize );
	m_lstDownloadFile.SetItemText( m_iFileCounter, 4, csTemp );//first row, 4th column
	CTime tStartTime(CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStartTime.iYear,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStartTime.iMonth,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStartTime.iDay,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStartTime.iHour,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStartTime.iMinute,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStartTime.iSecond);
	CString cstrStartTime = tStartTime.Format("%Y-%m-%d- %X");
	m_lstDownloadFile.SetItemText( m_iFileCounter, 5, cstrStartTime );
	
	CTime tStopTime(CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStoptime.iYear,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStoptime.iMonth,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStoptime.iDay,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStoptime.iHour,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStoptime.iMinute,
					CLS_ConnectServer::m_sFileInfo[m_iFileCounter].struStoptime.iSecond);
	CString cstrStopTime = tStopTime.Format("%Y-%m-%d- %X");
	m_lstDownloadFile.SetItemText( m_iFileCounter, 6, cstrStopTime );
	m_iFileCounter++;
}

//Query button response function
void CLS_OperateByFileDlg::OnBnClickedQuery()
{
	m_iCurrentPage = 0;
	NetFileQuery();
}

int CLS_OperateByFileDlg::NetFileQuery()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
		return 0;
	}

	m_lstDownloadFile.DeleteAllItems();
	m_iFileCounter = 0;
	for(map<CString, int>::iterator it = m_mapConnId.begin(); it != m_mapConnId.end(); ++it )
	{
		NetClient_NetFileStopDownloadFile( it->second );
	}
	m_mapConnId.erase(m_mapConnId.begin(), m_mapConnId.end());
	for(map<CString, int>::iterator it = m_mapcsPause.begin(); it != m_mapcsPause.end(); ++it )
	{
		NetClient_NetFileStopDownloadFile( it->second );
	}
	m_mapcsPause.clear();
	m_mapRowNum.clear();
	GetQueryCondition();

	char cTemp[MAX_PATH] = {0};
	NETFILE_QUERY_V5 tMultiChanQueryFile = {0};

	if( DOWNLOAD_DEMO_ALL == m_csChannelNum || DOWNLOAD_DEMO_ALL_EN == m_csChannelNum )
	{
		tMultiChanQueryFile.iQueryChannelNo = FLAG_QUERY_ALL_CHANNEL;
	}
	else
	{
		tMultiChanQueryFile.iQueryChannelNo = _ttoi( m_csChannelNum );
	}
	if( DOWNLOAD_DEMO_ALL == m_csFileType || DOWNLOAD_DEMO_ALL_EN == m_csFileType)
	{
		tMultiChanQueryFile.iFiletype = 0;//0: All 
	}
	if( DOWNLOAD_DEMO_FILE_TYPE_VIDOE == m_csFileType || DOWNLOAD_DEMO_FILE_TYPE_VIDOE_EN == m_csFileType)
	{
		tMultiChanQueryFile.iFiletype = 1;//1: AVstream
	}
	if( DOWNLOAD_DEMO_FILE_TYPE_PICTURE == m_csFileType || DOWNLOAD_DEMO_FILE_TYPE_PICTURE_EN == m_csFileType)
	{
		tMultiChanQueryFile.iFiletype = 2;//2: picture
	}
	
	tMultiChanQueryFile.iPageSize = MAX_PAGESIZE;
	tMultiChanQueryFile.iPageNo = m_iCurrentPage;
	tMultiChanQueryFile.iType = 0xFF;//Video type
	tMultiChanQueryFile.iStreamNo = m_cboStreamNo.GetCurSel();
	tMultiChanQueryFile.iTriggerType = 0x7FFFFFFF; //alarm type

	tMultiChanQueryFile.tStartTime.iYear = m_nvsStartTime.iYear;
	tMultiChanQueryFile.tStartTime.iMonth = m_nvsStartTime.iMonth;
	tMultiChanQueryFile.tStartTime.iDay = m_nvsStartTime.iDay;
	tMultiChanQueryFile.tStartTime.iHour = m_nvsStartTime.iHour;
	tMultiChanQueryFile.tStartTime.iMinute = m_nvsStartTime.iMinute;
	tMultiChanQueryFile.tStartTime.iSecond = m_nvsStartTime.iSecond;

	tMultiChanQueryFile.tStopTime.iYear = m_nvsStopTime.iYear;
	tMultiChanQueryFile.tStopTime.iMonth = m_nvsStopTime.iMonth;
	tMultiChanQueryFile.tStopTime.iDay = m_nvsStopTime.iDay;
	tMultiChanQueryFile.tStopTime.iHour = m_nvsStopTime.iHour;
	tMultiChanQueryFile.tStopTime.iMinute = m_nvsStopTime.iMinute;
	tMultiChanQueryFile.tStopTime.iSecond = m_nvsStopTime.iSecond;

	int iRet = NetClient_Query_V4(CLS_ConnectServer::GetInstance()->m_iLogonID, CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE, 0, &tMultiChanQueryFile, sizeof(tMultiChanQueryFile));
	if(0 != iRet)
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_QUERY_FAILED, 0, 0);
	}
	return 0;
}

void CLS_OperateByFileDlg::GetQueryCondition()
{
	//get start time
	CTime ctStartTime, ctStopTime;
	m_dtpStartTime.GetTime( ctStartTime );
	m_nvsStartTime.iYear = ctStartTime.GetYear();
	m_nvsStartTime.iMonth = ctStartTime.GetMonth();
	m_nvsStartTime.iDay = ctStartTime.GetDay();
	m_nvsStartTime.iHour = ctStartTime.GetHour();
	m_nvsStartTime.iMinute = ctStartTime.GetMinute();
	m_nvsStartTime.iSecond = ctStartTime.GetSecond();
	//Get deadline
	m_dtpStopTime.GetTime( ctStopTime );
	m_nvsStopTime.iYear = ctStopTime.GetYear();
	m_nvsStopTime.iMonth = ctStopTime.GetMonth();
	m_nvsStopTime.iDay = ctStopTime.GetDay();
	m_nvsStopTime.iHour = ctStopTime.GetHour();
	m_nvsStopTime.iMinute = ctStopTime.GetMinute();
	m_nvsStopTime.iSecond = ctStopTime.GetSecond();

	int iStartTime = CLS_OperateByTime::GetInstance()->NvsFileTimeToAbsSeconds( &m_nvsStartTime);
	int iStopTime = CLS_OperateByTime::GetInstance()->NvsFileTimeToAbsSeconds( &m_nvsStopTime);
	if( iStopTime <= iStartTime )
	{
		MessageBox(DOWNLOAD_DEMO_INPUT_TIME_SPACE);
		return;
	}
	//Get the input channel number
	int iIndex = m_cboChannelNum.GetCurSel();
	if( -1 == iIndex )
	{
		m_cboChannelNum.GetWindowText( m_csChannelNum);
	}
	else
	{
		m_cboChannelNum.GetLBText( iIndex, m_csChannelNum );
	}
	
	//get file type
	iIndex = m_cboFileType.GetCurSel();
	//get default value when no selection
	if( DOWNLOAD_DEMO_INT_GEGATIVE_ONE == iIndex )
	{
		m_cboFileType.GetWindowText( m_csFileType);
	}
	else
	{
		m_cboFileType.GetLBText( iIndex, m_csFileType );
	}
}


void CLS_OperateByFileDlg::OnBnClickedDownloadSpeedSet()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		CString csTemp = GetDownloadFileName();
		if( DOWNLOAD_DEMO_NULL_CHARACTER == csTemp)
		{
			return;
		}
		CString csDownLoadSpeed;
		m_cboDownLoadSpeed.GetWindowText( csDownLoadSpeed );
		char cFileName[MAX_PATH] = {0};
		sprintf_s( cFileName,"%S", m_sDownLoadFileName );
		
		map<CString, int>::iterator iter = m_mapConnId.find( csTemp);
		if( iter !=m_mapConnId.end())
		{
			
			DownloadFile( CLS_ConnectServer::GetInstance()->m_iLogonID, cFileName ,-1 , _ttoi( csDownLoadSpeed ), 1, DOWNLOAD_CMD_FILE);
		}
		else
		{
			DownloadFile( CLS_ConnectServer::GetInstance()->m_iLogonID, cFileName ,-1 , _ttoi( csDownLoadSpeed ), 0, DOWNLOAD_CMD_FILE);
		}

		
	}
}

void CLS_OperateByFileDlg::OnBnClickedDownloadPosSet()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		CString csTemp = GetDownloadFileName();
		if( DOWNLOAD_DEMO_NULL_CHARACTER == csTemp )
		{
			return;
		}
		CString csDownLoadSpeed;
		CString csDownloadPos;
		m_edtDownloadPos.GetWindowText( csDownloadPos);
		m_cboDownLoadSpeed.GetWindowText( csDownLoadSpeed );
		char cFileName[MAX_PATH] = {0};
		sprintf_s( cFileName,"%s", m_sDownLoadFileName );
		map<CString, int>::iterator iter = m_mapConnId.find( csTemp);
		if( iter !=m_mapConnId.end())
		{
			DownloadFile( CLS_ConnectServer::GetInstance()->m_iLogonID, cFileName , _ttoi(csDownloadPos) , _ttoi( csDownLoadSpeed ), 1, DOWNLOAD_CMD_FILE);
		}
		else
		{
			DownloadFile( CLS_ConnectServer::GetInstance()->m_iLogonID, cFileName , _ttoi(csDownloadPos) , _ttoi( csDownLoadSpeed ), 0, DOWNLOAD_CMD_FILE);
		}
		
	}
}

//play button message response
void CLS_OperateByFileDlg::OnBnClickedPlay()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		if( DOWNLOAD_DEMO_NULL_CHARACTER == GetDownloadFileName() )
		{
			return;
		}
		if( DOWNLOAD_DEMO_INT_ZERO == m_iLanguage )
		{
			CLS_CDisplayDlg displayDialog;
			displayDialog.SetConfig(TRUE, FALSE);
			displayDialog.DoModal();	
		}
		if( DOWNLOAD_DEMO_INT_ONE == m_iLanguage )
		{
			CLS_CDisplayDlg displayDialog;
			displayDialog.SetConfig(TRUE, TRUE);
			displayDialog.DoModal();	
		}
	}
}



CString CLS_OperateByFileDlg::GetDownloadFileName()
{
	//Get the clicked position first
	POSITION pos = m_lstDownloadFile.GetFirstSelectedItemPosition();
	if(pos == NULL)
	{
		MessageBox(DOWNLOAD_DEMO_PLEASE_CHOSE_ONE_FILE);
		return DOWNLOAD_DEMO_NULL_CHARACTER;
	}
	//Get the line number, convert it through POSITION
	m_iRowNum = (int)m_lstDownloadFile.GetNextSelectedItem(pos);
	m_sDownLoadFileName = m_lstDownloadFile.GetItemText( m_iRowNum, 0 );
	if( m_sDownLoadFileName.IsEmpty())
	{
		MessageBox(DOWNLOAD_DEMO_NULL_FILE_NAME);
		return DOWNLOAD_DEMO_NULL_CHARACTER;
	}
	m_mapRowNum.insert(pair<CString, int>(m_sDownLoadFileName, m_iRowNum));
	return m_sDownLoadFileName;
}
void CLS_OperateByFileDlg::OnTimer(UINT_PTR nIDEvent)
{
	switch(nIDEvent)
	{
	case ID_TIMER_FILE_DOWNLOAD_POS:
		{
			if( FALSE == CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
			{
				m_cboPageNo.ResetContent();
				m_lstDownloadFile.DeleteAllItems();
			}
			map<CString, int>::iterator iter;
			for( iter = m_mapConnId.begin(); iter != m_mapConnId.end(); ++iter)
			{
				int iRet = NetClient_NetFileGetDownloadPos( iter->second, &m_iDownloadPos, &m_iDownloadSize);
				m_csDownloadPos.Format(_T("%d"), m_iDownloadPos );
				m_csDownloadSize.Format(_T("%d"), m_iDownloadSize );
				map<CString, int>::iterator it = m_mapRowNum.find(iter->first);
				if( it != m_mapRowNum.end())
				{
					m_lstDownloadFile.SetItemText( it->second, 1, (m_csDownloadPos + _T("%/") + m_csDownloadSize));
				}
				if( DOWNLOAD_DEMO_WOWNLOAD_FINISH == m_iDownloadPos)
				{
					NetClient_StopRecv( iter->second );
					iter = m_mapConnId.erase(iter);		
					//When the size is 0 or the last one, break directly, and then execute the for ++ operation to access the invalid pointer
					if( 0 == m_mapConnId.size() || iter == m_mapConnId.end())
					{
						break;
					}
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
unsigned long CLS_OperateByFileDlg::DownloadFile( int _iLogonID,char* _pcFileName,int _iDownloadPos , int _iDownloadSpeed ,int _iFlag, int _iDownloadCmdID)
{
	if (NULL == _pcFileName)
	{
		return -1;
	}
	CString cstrFileName = _pcFileName;
	CString cstrType = cstrFileName.Right(3);

	DOWNLOAD_FILE downloadFile = {0};
	downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_SDV;  //0-SDV 1(3)-PS; 4-MP4; 5-AVI
	downloadFile.m_iSize = sizeof(DOWNLOAD_FILE);
	downloadFile.m_iPosition = -1;
	downloadFile.m_iSpeed = _iDownloadSpeed;
	strcpy(downloadFile.m_cRemoteFilename ,_pcFileName);

	if ("jpg" == cstrType || 0 == m_cboSaveFileType.GetCurSel())
	{
		strcpy(downloadFile.m_cLocalFilename ,_pcFileName);
	} else {
		TCHAR szFilePath[MAX_PATH + 1]={0};
		GetModuleFileName(NULL, szFilePath, MAX_PATH);
		(_tcsrchr(szFilePath, _T('\\')))[1] = 0; // Remove the filename, get only the path string
		CString str_url = szFilePath;  // For example str_url==e:\program\Debug\

		CString cstrLocal = str_url+cstrFileName.Left(cstrFileName.GetLength() - 3);

		if (1 == m_cboSaveFileType.GetCurSel()) {
			cstrLocal += "ps";
			downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_PS;
		} else if (2 == m_cboSaveFileType.GetCurSel()) {
			cstrLocal += "mp4";
			downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_ZFMP4;
		} else if (3 == m_cboSaveFileType.GetCurSel()) {
			cstrLocal += "avi";
			downloadFile.m_iSaveFileType = DOWNLOAD_FILE_TYPE_AVI;
		}
		strcpy(downloadFile.m_cLocalFilename ,cstrLocal.GetBuffer());
	}

	downloadFile.m_iSpeed = _iDownloadSpeed;

	int iRet = -1;
	
	if (DOWNLOAD_CMD_CONTROL == _iDownloadCmdID)
	{
		DOWNLOAD_CONTROL tdc = {sizeof(DOWNLOAD_CONTROL)};
		tdc.m_iPosition = -1;
		tdc.m_iSpeed = _iDownloadSpeed;
		tdc.m_iReqMode = 0;
		iRet = NetClient_NetFileDownload(&(CLS_ConnectServer::GetInstance()->m_uiConnID), _iLogonID, _iDownloadCmdID, &tdc, sizeof(tdc));
	}
	else
	{
		iRet = NetClient_NetFileDownload(&(CLS_ConnectServer::GetInstance()->m_uiConnID), _iLogonID, _iDownloadCmdID, &downloadFile, sizeof(DOWNLOAD_FILE));
	}

	if( 0 == iRet)
	{	
		m_mapConnId.insert(pair<CString, int>(m_sDownLoadFileName,  CLS_ConnectServer::GetInstance()->m_uiConnID));
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_START_DOWNLOAD, 0, 0);
	}
	else
	{
		::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_DOWNLOAD_FAILED, 0, 0);
	}
	return CLS_ConnectServer::GetInstance()->m_uiConnID;
}

void CLS_OperateByFileDlg::OnBnClickedDownload()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else
	{
		if( m_mapConnId.size() >= 5)
		{
			MessageBox(DOWNLOAD_DEMO_MAX_DOWNLOAD_FILE);
			return;
		}
		CString csTemp = GetDownloadFileName();
		map<CString, int>::iterator iter = m_mapConnId.find( csTemp);
		map<CString, int>::iterator iterPause = m_mapcsPause.find( csTemp);
		if( iter !=m_mapConnId.end() || DOWNLOAD_DEMO_NULL_CHARACTER == csTemp || iterPause != m_mapcsPause.end())
		{
			return;
		}
		CString csDownLoadSpeed;
		m_cboDownLoadSpeed.GetWindowText( csDownLoadSpeed );
		DownloadFile( CLS_ConnectServer::GetInstance()->m_iLogonID, m_sDownLoadFileName.GetBuffer() ,-1 , _ttoi(csDownLoadSpeed), 0, DOWNLOAD_CMD_FILE);
	}
}

void CLS_OperateByFileDlg::OnBnClickedButtonStopDownload()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else
	{
		CString csTemp = GetDownloadFileName();
		if( DOWNLOAD_DEMO_NULL_CHARACTER == csTemp )
		{
			return;
		}
		map<CString, int>::iterator iter = m_mapConnId.find( csTemp);
		
		if( iter != m_mapConnId.end())
		{
			NetClient_NetFileStopDownloadFile( iter->second );
			m_lstDownloadFile.SetItemText( m_iRowNum, 1, DOWNLOAD_DEMO_NULL_CHARACTER);
			m_mapConnId.erase(iter);
		}
		map<CString, int>::iterator iterPause = m_mapcsPause.find( csTemp);
		if( iterPause != m_mapcsPause.end())
		{
			NetClient_NetFileStopDownloadFile( iterPause->second );
			m_lstDownloadFile.SetItemText( m_iRowNum, 1, DOWNLOAD_DEMO_NULL_CHARACTER);
			m_mapcsPause.erase(iterPause);
		}
	}
}

void CLS_OperateByFileDlg::OnBnClickedPauseDownload()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		CString csTemp = GetDownloadFileName();
		if( DOWNLOAD_DEMO_NULL_CHARACTER == csTemp )
		{
			return;
		}
		map<CString, int>::iterator iter = m_mapConnId.find( csTemp);
		if( iter != m_mapConnId.end())
		{
			CLS_ConnectServer::GetInstance()->m_uiConnID = iter->second;
			DownloadFile( CLS_ConnectServer::GetInstance()->m_iLogonID, m_sDownLoadFileName.GetBuffer() ,-1 , 0, 1, DOWNLOAD_CMD_CONTROL);
			m_mapcsPause.insert(pair<CString, int>( iter->first, iter->second));
		}
	}
}

void CLS_OperateByFileDlg::OnBnClickedContinueDownload()
{
	if( !CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(DOWNLOAD_DEMO_LOGON_FIRST);
	}
	else{
		CString csTemp = GetDownloadFileName();
		if( DOWNLOAD_DEMO_NULL_CHARACTER == csTemp )
		{
			return;
		}
		map<CString, int>::iterator iter = m_mapcsPause.find( csTemp);
		if( iter != m_mapcsPause.end())
		{
			m_lstDownloadFile.SetItemText( m_iRowNum, 1, m_csDownloadPos + _T("%/") + m_csDownloadSize );
			CString csDownLoadSpeed;
			m_cboDownLoadSpeed.GetWindowText( csDownLoadSpeed );
			CLS_ConnectServer::GetInstance()->m_uiConnID = iter->second;
			
			DownloadFile( CLS_ConnectServer::GetInstance()->m_iLogonID, m_sDownLoadFileName.GetBuffer() ,-1 ,_ttoi(csDownLoadSpeed), 1, DOWNLOAD_CMD_CONTROL);				
			m_mapConnId.insert(pair<CString, int>(iter->first, iter->second));
			m_mapcsPause.erase(iter);
		}
	}
}

void CLS_OperateByFileDlg::ClearList()
{
	m_lstDownloadFile.DeleteAllItems();
}

void CLS_OperateByFileDlg::OnLogonSucc()
{
	//Set the channel number drop-down menu
	int iLogonId = CLS_ConnectServer::GetInstance()->m_iLogonID;
	int iChanNum = DOWNLOAD_DEMO_CHANNEL_NUM_COUNTER;
	if (iLogonId >= 0)
	{
		NetClient_GetChannelNum(iLogonId, &iChanNum);
	}
	m_cboChannelNum.ResetContent();
	m_cboChannelNum.AddString((1 == m_iLanguage)? _T("ALL") : DOWNLOAD_DEMO_ALL);
	CString temp;
	for( int i = 0; i < iChanNum; ++i )
	{
		temp.Format(_T("%d"),i ); 
		m_cboChannelNum.AddString( temp );
	}
	m_cboChannelNum.SetCurSel(0);
}

void CLS_OperateByFileDlg::OnCbnSelchangeComboFilePage()
{
	m_iCurrentPage = m_cboPageNo.GetCurSel();
	NetFileQuery();
}

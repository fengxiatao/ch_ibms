// VideoDisplayDlg.cpp : Implementation file
//

#include "stdafx.h"
#include "VideoDisplay.h"
#include "VideoDisplayDlg.h"
#include "ConnectServer.h"
#include "MacroDefine.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CAboutDlg dialog for application "About" menu item

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// dialog data
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// accomplish
protected:
	DECLARE_MESSAGE_MAP()

	
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
	 
END_MESSAGE_MAP()


// CVideoDisplayDlg dialog




CVideoDisplayDlg::CVideoDisplayDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CVideoDisplayDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDD_MAIN_DIALOG);
	m_iRow = 0;
	m_iColumn = 0;
	m_iFileCount = 0;
	//m_OperateByFileDlg = NULL;
	//m_OperateByTime = NULL;
}


void CVideoDisplayDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_PORT_INPUT, m_edtPortCtrl );
	DDX_Control(pDX, IDC_EDIT_USERNAME, m_edtUsernameCtrl );
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_edtPasswordCtrl);
	DDX_Control(pDX, IDC_EDIT_IP_ADRESS, m_edtIpAddressCtrl);
	DDX_Control(pDX, IDC_LIST_LOG, m_lstLog);
	DDX_Control(pDX, IDC_COMBO_LANGUAGE, m_cboLanguageSelect);
}


BEGIN_MESSAGE_MAP(CVideoDisplayDlg, CDialog)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_MESSAGE( WM_MSG_NEW_DISPLAY_DIALOG, &CVideoDisplayDlg::OnLogOnSuccess )    // OnNewDisplayDlg is a custom message handler
	ON_MESSAGE( WM_MSG_LOGON_FAILED, &CVideoDisplayDlg::OnLogonFailed )
	ON_MESSAGE( WM_MSG_FILE_INFO, &CVideoDisplayDlg::OnFileInfo )
	ON_MESSAGE( WM_MSG_FILE_COUNT, &CVideoDisplayDlg::OnFileCount )
	ON_MESSAGE( WM_MSG_FILE_START_DOWNLOAD, &CVideoDisplayDlg::OnFileStartDownload )
	ON_MESSAGE( WM_MSG_FILE_DOWNLOAD_FAILED, &CVideoDisplayDlg::OnFileDownloadFailed )

	ON_MESSAGE( WM_MSG_FILE_DOWNLOAD_FINISH, &CVideoDisplayDlg::OnFileDownloadFinish )
	ON_MESSAGE( WM_MSG_FILE_DOWNLOAD_FAULT, &CVideoDisplayDlg::OnFileDownloadFault )
	ON_MESSAGE( WM_MSG_FILE_DOWNLOAD_INPURRT, &CVideoDisplayDlg::OnFileDownloadInpurrt )
	
	ON_MESSAGE( WM_MSG_SPEED_SET_FAILED, &CVideoDisplayDlg::OnSpeedSetFailed )
	ON_MESSAGE( WM_MSG_STOP_SET_FAILED, &CVideoDisplayDlg::OnStopSetFailed )
	ON_MESSAGE( WM_MSG_POS_SET_FAILED, &CVideoDisplayDlg::OnPosSetFailed )
	ON_MESSAGE( WM_MSG_VIDEO_PLAY_FAILED, &CVideoDisplayDlg::OnVideoPlayFailed )
	ON_MESSAGE( WM_MSG_FILE_QUERY_FAILED, &CVideoDisplayDlg::OnFileQueryFailed )
	ON_MESSAGE( WM_MSG_DOWNLOAD_BY_TIME_FAILED, &CVideoDisplayDlg::OnDownloadByTimeFailed )
	ON_MESSAGE( WM_MSG_STOP_PLAY_FAILED, &CVideoDisplayDlg::OnDownloadByTimeFailed )
	ON_MESSAGE( WM_MSG_Set_RAW_FRAME_CallBACK_FAILED, &CVideoDisplayDlg::OnSetRawFrameCallbackFailed )
	ON_MESSAGE( WM_MSG_CAPTURE_FAILED, &CVideoDisplayDlg::OnCaptureFailed )
	ON_MESSAGE( WM_MSG_CAPTURE_SUCCESS, &CVideoDisplayDlg::OnCaptureSuccess )
	ON_MESSAGE( WM_MSG_SET_VOLUME_SUCCESS, &CVideoDisplayDlg::OnSetVolumeSuccess )
	ON_MESSAGE( WM_MSG_SET_VOLUME_FAILED, &CVideoDisplayDlg::OnSetVolumeFailed )
	ON_MESSAGE( WM_MSG_FORWARD_STEP_FAILED, &CVideoDisplayDlg::OnForwardStepFailed )
	ON_MESSAGE( WM_MSG_FORWARD_STEP_SUCCESS, &CVideoDisplayDlg::OnForwardStepSuccess )
	ON_MESSAGE( WM_MSG_VOLUME_CTRL_FAILED, &CVideoDisplayDlg::OnVolumeCtrlFailed )
	ON_MESSAGE( WM_MSG_VI_FRAME, &CVideoDisplayDlg::OnViFrame )
	ON_MESSAGE( WM_MSG_OTHER_TYPE, &CVideoDisplayDlg::OnOtherType )
	ON_MESSAGE( WM_MSG_AUDIO_FRAME, &CVideoDisplayDlg::OnAudioFrame )

	ON_BN_CLICKED(IDC_BUTTON_LOGON, &CVideoDisplayDlg::OnBnClickedLogon)
	ON_BN_CLICKED(IDC_BUTTON_FILE, &CVideoDisplayDlg::OnBnClickedDealByFile)
	ON_BN_CLICKED(IDC_BUTTON_TIME_SPACE, &CVideoDisplayDlg::OnBnClickedDealByTime)
	
	ON_BN_CLICKED(IDC_BUTTON_LOGOFF, &CVideoDisplayDlg::OnBnClickedLogOff)
	ON_WM_DESTROY()

	ON_CBN_SELCHANGE(IDC_COMBO_LANGUAGE, &CVideoDisplayDlg::OnCbnSelchangeComboLanguage)
	ON_WM_CLOSE()
END_MESSAGE_MAP()


// CVideoDisplayDlg message handler

BOOL CVideoDisplayDlg::OnInitDialog()
{
	CDialog::OnInitDialog();


	// Added "About..." menu item to the system menu.

	// IDM_ABOUTBOX Must be in system command scope.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Sets the icon for this dialog. When the application main window is not a dialog, the framework will automatically
	//  do this
	SetIcon(m_hIcon, TRUE);			// set large icons
	SetIcon(m_hIcon, FALSE);		// set small icon

	// TODO: Add extra initialization code here
	InitDlg();
	//The following m_OperateByFileDlg and m_OperateByTime are different classes that will be displayed in the same area, of which the first one is displayed by default
	m_OperateByFileDlg.Create( IDD_FILE_DISPLAY_DIALOG, this );  
	m_OperateByTime.Create( IDD_TIME_DISPLAYDIALOG,this );

	//read embedded location, IDC_STATIC_CHILD_AREA.
	GetDlgItem(IDC_STATIC_CHILD_AREA)->GetWindowRect( &m_DialogChild );
	ScreenToClient( m_DialogChild );
	
	m_OperateByFileDlg.MoveWindow( m_DialogChild );
	m_OperateByFileDlg.ShowWindow( SW_SHOW );
	GetDlgItem( IDC_STATIC_CHILD_AREA )->ShowWindow( false );

	OnCbnSelchangeComboLanguage();

	return TRUE;  // Returns TRUE unless focus is set to the control
}

void CVideoDisplayDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to the dialog, you need the code below
//  to draw the icon. For MFC applications using document/view models,
//  This will be done automatically by the framework.

void CVideoDisplayDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for drawing

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center the icon in the workspace rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// draw icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

//When the user drags the minimized window, the system calls this function to get the cursor
//show.
HCURSOR CVideoDisplayDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

//Login button message response function
void CVideoDisplayDlg::OnBnClickedLogon()
{
	if( TRUE == CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		MessageBox(_T("Please log out first!"));
		return;
	}
	CString cstrIp;
	CString cstrPort;
	CString cstrUsername;
	CString cstrPassword;
	m_edtIpAddressCtrl.GetWindowText( cstrIp );
	m_edtPortCtrl.GetWindowText( cstrPort );
	m_edtUsernameCtrl.GetWindowText( cstrUsername );
	m_edtPasswordCtrl.GetWindowText( cstrPassword );
	CLS_ConnectServer::GetInstance()->ConnectServerProcess( cstrIp,  cstrPort, cstrUsername, cstrPassword );
}

void CVideoDisplayDlg::OnBnClickedLogOff()
{
	if( TRUE == CLS_ConnectServer::GetInstance()->m_bLogOnFlag )
	{
		//logout user
		for(map<CString, int>::iterator it = CLS_OperateByFileDlg::m_mapConnId.begin(); it != CLS_OperateByFileDlg::m_mapConnId.end(); ++it )
		{
			NetClient_NetFileStopDownloadFile( it->second );
		}
		CLS_OperateByFileDlg::m_mapConnId.clear();
		int iRet = NetClient_Logoff( CLS_ConnectServer::GetInstance()->m_iLogonID );
		if( 0 == iRet)
		{	
			CLS_ConnectServer::GetInstance()->m_bLogOnFlag = FALSE;
			WriteLog(DOWNLOAD_DEMO_LOG_MSG_SUCCESS_LOGOFF);
		}

	}
}

extern void Notify_Main(int _iLogonID, long _iWparam, void* _iLParam,void* _iUser );
//Initialize the log information grid
void CVideoDisplayDlg::InitDlg()
{
	CString strSavePath;
	char cFilePath[MAX_PATH] = {0};
	int iSize = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
	if (iSize <= 0)
	{
		strSavePath = _T("C:\\");
	}
	strSavePath.Format(_T("%s"),cFilePath);
	int iPos = strSavePath.ReverseFind('\\');
	if (iPos >= 0)
	{
		strSavePath = strSavePath.Left(iPos);
	}
	strSavePath.AppendFormat(_T("\\Log"));
	SetLogSaveDirectory(strSavePath);

	m_strLogFileName.Format(_T("%s\\DownloadDemo.log"),GetLogSaveDirectory());
	m_pFile = NULL;

	DWORD dwStyle = m_lstLog.GetExtendedStyle();
	//Select a line to highlight the entire line (in report style)
	dwStyle |= LVS_EX_FULLROWSELECT; 
	//Gridlines (in report style)
	dwStyle |= LVS_EX_GRIDLINES; 
	//set extension style
	m_lstLog.SetExtendedStyle(dwStyle); 
	
	m_lstLog.InsertColumn(0, DOWNLOAD_DEMO_LOG_TIME, LVCFMT_LEFT,180);
	m_lstLog.InsertColumn(1, DOWNLOAD_DEMO_LOG_INFO, LVCFMT_LEFT,520);
	//Set default login information
	m_edtIpAddressCtrl.SetWindowText(DOWNLOAD_DEMO_IP);
	m_edtPortCtrl.SetWindowText(DOWNLOAD_DEMO_PORT);
	m_edtUsernameCtrl.SetWindowText(DOWNLOAD_DEMO_USERNAME);
	m_edtPasswordCtrl.SetWindowText(DOWNLOAD_DEMO_PASSWORD);
	m_edtPasswordCtrl.SetPasswordChar('*');

	//Initialize language settings
	m_cboLanguageSelect.ResetContent();
	m_cboLanguageSelect.AddString(_T("中文"));
	m_cboLanguageSelect.AddString(_T("英文"));
	
	LANGID Systemlid = GetSystemDefaultLangID(); 
	if (0x0804 == Systemlid || 0x1004 == Systemlid)
	{
		m_cboLanguageSelect.SetCurSel(0);
	}
	else
	{
		m_cboLanguageSelect.SetCurSel(1);
	}

	LoadNVSSDK();//Initialize the interface library
	int iRet = NetClient_Startup_V4( DOWNLOAD_DEMO_SERVER_PORT, DOWNLOAD_DEMO_CLIENT_PORT,DOWNLOAD_DEMO_INT_ZERO );
	//set callback function
	NetClient_SetNotifyFunction_V4(Notify_Main, NULL, NULL, NULL, NULL);
}

int CVideoDisplayDlg::SetLogSaveDirectory( CString _strPath )
{
	CreateDirectory(_strPath,NULL);
	m_strLogSaveDirectory = _strPath;
	return 0;
}

CString CVideoDisplayDlg::GetLogSaveDirectory()
{
	return m_strLogSaveDirectory;
}
void CVideoDisplayDlg::AddLog( CString _strData )
{
	if( NULL == m_pFile )
	{
		fopen_s(&m_pFile,(LPSTR)(LPCTSTR)m_strLogFileName,"wb+");
	}
	if (m_pFile)
	{
		_strData.AppendFormat(_T("\n"));
		fwrite((LPSTR)(LPCTSTR)_strData,sizeof(char),_strData.GetLength(),m_pFile);
	}
}
//Press file play button message response function
void CVideoDisplayDlg::OnBnClickedDealByFile()
{
	m_OperateByTime.ShowWindow( SW_HIDE );
	m_OperateByFileDlg.MoveWindow( m_DialogChild );
	m_OperateByFileDlg.ShowWindow( SW_SHOW );
	GetDlgItem( IDC_STATIC_CHILD_AREA )->ShowWindow( false );
}
//Play key message response function by time period
void CVideoDisplayDlg::OnBnClickedDealByTime()
{
	m_OperateByFileDlg.ShowWindow( SW_HIDE );
	m_OperateByTime.MoveWindow( m_DialogChild );
	m_OperateByTime.ShowWindow( SW_SHOW );
	GetDlgItem( IDC_STATIC_CHILD_AREA )->ShowWindow( false );
}



//Get file information message response function
LRESULT CVideoDisplayDlg::OnFileInfo( WPARAM wParam, LPARAM lParam )
{
	int iFileTotalCount = (int)wParam;
	m_OperateByFileDlg.DisplayFileInfo(iFileTotalCount);
	return 0;
}


//Login success message response function
LRESULT CVideoDisplayDlg::OnLogOnSuccess( WPARAM wParam, LPARAM lParam)
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_SUCCESS_LOGON);
	m_OperateByFileDlg.OnLogonSucc();
	m_OperateByTime.OnLogonSucc();
	return 0;
}
//Login failure message response function
LRESULT CVideoDisplayDlg::OnLogonFailed( WPARAM wParam, LPARAM lParam)
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_FAILED_LOGON);
	return 0;
}

LRESULT CVideoDisplayDlg::OnSpeedSetFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_FAILED_SET_SPEED);
	return 0;
}

LRESULT CVideoDisplayDlg::OnStopSetFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_FAILED_PAUSE);
	return 0;

}
LRESULT CVideoDisplayDlg::OnPosSetFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_FAILED_SET_POS);
	return 0;

}

LRESULT CVideoDisplayDlg::OnFileDownloadFinish( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_FINISH_DWONLOAD);
	return 0;
}

LRESULT CVideoDisplayDlg::OnFileDownloadInpurrt( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_DOWNLOAD_INPURRT);
	return 0;
}


LRESULT CVideoDisplayDlg::OnFileDownloadFault( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_DOWNLOAD_FALUT);
	return 0;
}
//Get the number of files message response function
LRESULT CVideoDisplayDlg::OnFileCount( WPARAM wParam, LPARAM lParam )
{
	m_OperateByFileDlg.m_cboPageNo.ResetContent();
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_NOT_QUERY_FILE);
	return 0;
}
LRESULT CVideoDisplayDlg::OnFileStartDownload( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_START_DOWNLOAD);
	return 0;
}

LRESULT CVideoDisplayDlg::OnFileDownloadFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_DOWNLOAD_FAILED);
	return 0;
}
LRESULT CVideoDisplayDlg::OnVideoPlayFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_MSG_PLAY_FAILED);
	return 0;
}

LRESULT CVideoDisplayDlg::OnFileQueryFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_QUERY_FAILED);
	return 0;
}

LRESULT CVideoDisplayDlg::OnDownloadByTimeFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_TIME_DOWNLOAD_FIAILED);
	return 0;
}

LRESULT CVideoDisplayDlg::OnSetRawFrameCallbackFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_SET_RAW_CALLBACK_FIAILED);
	return 0;
}

LRESULT CVideoDisplayDlg::OnCaptureFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_CAPTURE_FIAILED);
	return 0;
}
LRESULT CVideoDisplayDlg::OnCaptureSuccess( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_CAPTURE_SUCCESS);
	return 0;
}
LRESULT CVideoDisplayDlg::OnSetVolumeFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_SET_VOLUME_FAILED);
	return 0;
}
LRESULT CVideoDisplayDlg::OnSetVolumeSuccess( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_SET_VOLUME_SUCCESS);
	return 0;
}

LRESULT CVideoDisplayDlg::OnForwardStepFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_STEP_FORWARD_FAILED);
	return 0;
}

LRESULT CVideoDisplayDlg::OnForwardStepSuccess( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_STEP_FORWARD_SUCCESS);
	return 0;
}
LRESULT CVideoDisplayDlg::OnVolumeCtrlFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_VOLUME_CTRL_FAILED);
	return 0;
}
LRESULT CVideoDisplayDlg::OnViFrame( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_VI_FRAME);
	return 0;
}
LRESULT CVideoDisplayDlg::OnOtherType( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_OTHER_TYPE);
	return 0;
}
LRESULT CVideoDisplayDlg::OnAudioFrame( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_AUDIO_FRAME);
	return 0;
}

LRESULT CVideoDisplayDlg::OnStopPlayFailed( WPARAM wParam, LPARAM lParam )
{
	WriteLog(DOWNLOAD_DEMO_LOG_STOP_PLAY_FIAILED);
	return 0;
}
void CVideoDisplayDlg::WriteLog( CString _cstrLogInfo )
{
	m_tTime = CTime::GetCurrentTime();
	m_cstrTime =m_tTime.Format("%Y-%m-%d- %X");
	m_lstLog.InsertItem( DOWNLOAD_DEMO_INT_ZERO, m_cstrTime ); //first row, first column
	m_lstLog.SetItemText( DOWNLOAD_DEMO_INT_ZERO, DOWNLOAD_DEMO_INT_ONE, _cstrLogInfo);//first row, second column
	m_iRow++;
	AddLog(m_cstrTime + _T(" : ") + _cstrLogInfo);
}
void CVideoDisplayDlg::OnDestroy()
{
	CDialog::OnDestroy();

	//fclose(m_pFile);
	//m_pFile = NULL;
	//Release SDK resources
	if( 0 == NetClient_Cleanup())
	{
		//free interface library
		FreeNVSSDK();
	}
}


void CVideoDisplayDlg::OnCbnSelchangeComboLanguage()
{
	CString strLanguage;
	int iLanguage = 0;
	int iIndex = m_cboLanguageSelect.GetCurSel();
	if( CB_ERR == iIndex)
	{
		WriteLog(_T("Err : No Chose Language!"));
		return;
	}
	m_cboLanguageSelect.GetLBText(iIndex, strLanguage );
	if( strLanguage.IsEmpty())
	{
		WriteLog(_T("Err : Change Language Failed!"));
		return;
	}
	if( _T("中文") == strLanguage || _T("Chinese") == strLanguage )
	{
		iLanguage = 0;
		m_cboLanguageSelect.ResetContent();
		m_cboLanguageSelect.AddString(_T("中文"));
		m_cboLanguageSelect.AddString(_T("英文"));
		m_cboLanguageSelect.SetCurSel(0);
		
		LVCOLUMN col;
		for(int i = 0; i < 2; i++)
		{
			ZeroMemory(&col, sizeof(col));
			char cText[64] = {0};
			col.mask = LVCF_TEXT;//This sentence is necessary to prevent crashes under release
			col.pszText = cText;//This sentence is necessary to prevent crashes under release
			col.cchTextMax = 64;//This sentence is necessary to prevent crashes under release
			m_lstLog.GetColumn(i, &col);
			switch(i)
			{
			case 0:
				col.pszText = _T("时间");
				m_lstLog.SetColumn(0, &col);
				break;
			case 1:
				col.pszText = _T("日志信息");
				m_lstLog.SetColumn(1, &col);
				break;
			default:
				break;
			}

		}
	}
	if( _T("英文") == strLanguage || _T("English") == strLanguage )
	{
		iLanguage = 1;
		m_cboLanguageSelect.ResetContent();
		m_cboLanguageSelect.AddString(_T("English"));
		m_cboLanguageSelect.AddString(_T("Chinese"));
		m_cboLanguageSelect.SetCurSel(0);
		
		LVCOLUMN col;
		for(int i = 0; i < 2; i++)
		{
			ZeroMemory(&col, sizeof(col));
			char cText[64] = {0};
			col.mask = LVCF_TEXT;//This sentence is necessary to prevent crashes under release
			col.pszText = cText;//This sentence is necessary to prevent crashes under release
			col.cchTextMax = 64;//This sentence is necessary to prevent crashes under release
			m_lstLog.GetColumn(i, &col);
			switch(i)
			{
			case 0:
				col.pszText = _T("Time");
				m_lstLog.SetColumn(0, &col);
				break;
			case 1:
				col.pszText = _T("LogInfo");
				m_lstLog.SetColumn(1, &col);
				break;
			default:
				break;
			}

		}

	}
	GetDlgItem(IDC_BUTTON_LOGON)->SetWindowText( 0 == iLanguage ?_T("登录") : _T("Logon"));
	GetDlgItem(IDC_BUTTON_LOGOFF)->SetWindowText( 0 == iLanguage ?_T("退出") : _T("Logoff"));
	GetDlgItem(IDC_STATIC_USERNAME)->SetWindowText( 0 == iLanguage ?_T("用户名") : _T("Username"));
	GetDlgItem(IDC_STATIC_PASSWORD)->SetWindowText( 0 == iLanguage ?_T("密码") : _T("Password"));
	GetDlgItem(IDC_STATIC_PORT)->SetWindowText( 0 == iLanguage ?_T("端口") : _T("Port"));

	GetDlgItem(IDC_BUTTON_FILE)->SetWindowText( 0 == iLanguage ?_T("按文件操作") : _T("OperateByFile"));
	GetDlgItem(IDC_BUTTON_TIME_SPACE)->SetWindowText( 0 == iLanguage ?_T("按时间段操作") : _T("OperateByTime"));

	if (NULL != m_OperateByFileDlg)
	{
		m_OperateByFileDlg.OnLanguageChange(iLanguage);
	}
	if (NULL != m_OperateByTime)
	{
		m_OperateByTime.OnLanguageChange(iLanguage);
	}
}


void CVideoDisplayDlg::OnCancel()
{
	//Solve the problem of pressing the ESC program to exit

	//CDialog::OnCancel();
}

void CVideoDisplayDlg::OnOK()
{
	// Overload OnOK() to solve the problem of program exit by pressing the Enter key
	//CDialog::OnOK();
}

void CVideoDisplayDlg::OnClose()
{
	//Solve the problem that the program does not close when clicking the dialog box close button

	CDialog::OnCancel();
}

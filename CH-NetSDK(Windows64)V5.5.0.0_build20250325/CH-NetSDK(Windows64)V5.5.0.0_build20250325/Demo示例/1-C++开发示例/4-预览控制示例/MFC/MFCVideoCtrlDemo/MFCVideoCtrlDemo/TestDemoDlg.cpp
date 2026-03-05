// TestDemoDlg.cpp : Implementation file
//

#include "stdafx.h"
#include "TestDemo.h"
#include "TestDemoDlg.h"
#include "LanguageManager.h"
#include "ActionControl.h"
#include "NVSSDK_INTERFACE.h"
#include "CommonFun.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif
enum ABILITY_TYPE_BASSPARA
{
	ABILITY_TYPE_BASEPARA_WIPEFOG = 0, //The 0th bit represents whether defogging is supported
	ABILITY_TYPE_BASEPARA_NEW3D = 1, //The first digit represents whether the new 3D positioning protocol is supported
	ABILITY_TYPE_BASEPARA_COMPASS = 2 //The second digit represents whether the compass is supported
};

#define MAX_LOG_MSG_LEN		4096
#define MAX_PTZ_NUM         9999
#define MIN_PTZ_NUM         1
#define ID_SNATCH_BMP       100
#define ID_SNATCH_YUV       101
#define ID_SNATCH_JPG       102

#define ID_RECORD_SDV       103
#define ID_RECORD_PS        104 
#define WM_MAIN_MSG    WM_USER+100
#define MAX_STREAM           2
// CAboutDlg dialog for the application's "About" menu item
CString GetTextByLan(CString _cstrTextCH, CString _cstrTextEn)
{
	DWORD dwCurLan = CLS_LanguageManager::Instance()->GetLanguage();
	if (g_dwLanChinese == dwCurLan)
	{
		return _cstrTextCH;
	}
	return _cstrTextEn;
}

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// dialog data
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX); // DDX/DDV support

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


// CTestDemoDlg dialog




CTestDemoDlg::CTestDemoDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CTestDemoDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);

	m_iChannel = -1;
	m_iConnectID = -1;
	m_LogonID = -1;
	m_bIsLogon = FALSE;
	m_bIsDisplay = FALSE;
	m_bIsOpen3DLocation = FALSE;
	memset(m_cDeviceType, 0, sizeof(m_cDeviceType));
	
}

void CTestDemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_IPADDRESS1, m_DevIP);
	DDX_Control(pDX, IDC_EDIT2, m_edtPassword);
	DDX_Control(pDX, IDC_EDIT3, m_edtPort);
	DDX_Control(pDX, IDC_COMBO_LANGUAGE, m_cboLanguage);
	DDX_Control(pDX, IDC_EDT_USERNAME, m_edtUser);
	DDX_Control(pDX, IDC_CBO_CHANNEL, m_ChannelNo);
	DDX_Control(pDX, IDC_COMBO_STREAM, m_cboStreamNo);
	DDX_Control(pDX, IDC_SLIDER_SPEED, m_sldSpeed);
	DDX_Control(pDX, IDC_COMBO_PRENUM, m_cboPreNum);
	DDX_Control(pDX, IDC_SLIDER_HUE, m_slider_hue);
	DDX_Control(pDX, IDC_SLIDER_BRIGHTNESS, m_slider_bright);
	DDX_Control(pDX, IDC_SLIDER_CONTRAST, m_slider_contrast);
	DDX_Control(pDX, IDC_SLIDER_SATURATION, m_slider_saturation);
	DDX_Control(pDX, IDC_LIST_LOG, m_listInfo);
}

BEGIN_MESSAGE_MAP(CTestDemoDlg, CDialog)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_CBN_SELCHANGE(IDC_COMBO_LANGUAGE, &CTestDemoDlg::OnCbnSelchangeComboLanguage)
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BUTTON_LOGON, &CTestDemoDlg::OnBnClickedButtonLogon)
    ON_MESSAGE(WM_MAIN_MSG,OnMainNotify)
	ON_BN_CLICKED(IDC_BUTTON_LOGOFF, &CTestDemoDlg::OnBnClickedButtonLogoff)
	ON_CBN_SELCHANGE(IDC_CBO_CHANNEL, &CTestDemoDlg::OnCbnSelchangeCboChannel)
	ON_CBN_SELCHANGE(IDC_COMBO_STREAM, &CTestDemoDlg::OnCbnSelchangeComboStream)
	ON_BN_CLICKED(IDC_BUTTON_SNAP, &CTestDemoDlg::OnBnClickedButtonSnatch)
	ON_COMMAND_RANGE(ID_SNATCH_BMP, ID_SNATCH_JPG, &CTestDemoDlg::OnSnatch)
	ON_COMMAND_RANGE(ID_RECORD_SDV, ID_RECORD_PS, &CTestDemoDlg::OnRecord)
	ON_BN_CLICKED(IDC_BUTTON_3DLOCATION, &CTestDemoDlg::OnBnClickedButton3dlocation)
	ON_WM_LBUTTONDOWN()
	ON_WM_MOUSEMOVE()
	ON_WM_LBUTTONUP()
	ON_WM_TIMER()
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER1, &CTestDemoDlg::OnNMCustomdrawSlider1)
	ON_BN_CLICKED(IDC_CHECK_AUTO, &CTestDemoDlg::OnBnClickedCheckAuto)
	ON_BN_CLICKED(IDC_BUTTON_SETPTZ, &CTestDemoDlg::OnBnClickedButtonSetptz)
	ON_BN_CLICKED(IDC_BUTTON_CALLPTZ, &CTestDemoDlg::OnBnClickedButtonCallptz)
	ON_WM_HSCROLL()
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_HUE, &CTestDemoDlg::OnNMCustomdrawSliderHue)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_BRIGHTNESS, &CTestDemoDlg::OnNMCustomdrawSliderBrightness)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_CONTRAST, &CTestDemoDlg::OnNMCustomdrawSliderContrast)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_SATURATION, &CTestDemoDlg::OnNMCustomdrawSliderSaturation)
	ON_BN_CLICKED(IDC_BUTTON_DEFAULT, &CTestDemoDlg::OnBnClickedButtonDefault)
	ON_BN_CLICKED(IDC_CHECK_RECORD, &CTestDemoDlg::OnBnClickedCheckRecord)
	ON_BN_CLICKED(IDC_BUTTON_CONNECTVIDEO, &CTestDemoDlg::OnBnClickedButtonConnectvideo)
	ON_BN_CLICKED(IDC_BUTTON_DISCONNECT, &CTestDemoDlg::OnBnClickedButtonDisconnect)
	ON_BN_CLICKED(IDC_BUTTON_SAVELOG, &CTestDemoDlg::OnBnClickedButtonSavelog)
	ON_BN_CLICKED(IDC_BUTTON_CLEARLOG, &CTestDemoDlg::OnBnClickedButtonClearlog)
END_MESSAGE_MAP()


// CTestDemoDlg message handler

BOOL CTestDemoDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add the "About..." menu item to the system menu.

	// IDM_ABOUTBOX must be in system command scope.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog. When the application main window is not a dialog, the framework will automatically
	// do this
	SetIcon(m_hIcon, TRUE); // set large icon
	SetIcon(m_hIcon, FALSE); // set the small icon

	// TODO: add extra initialization code here
	m_pVideo = CLS_VideoView::CreateInstance(0,this);
	m_pVideo->DrawRect(GetSysColor(COLOR_BTNFACE));
	RECT rcShow = {0};
	GetDlgItem(IDC_STATIC_VIDEO)->GetWindowRect(&rcShow);
	ScreenToClient(&rcShow);
	int iWidth = (rcShow.right - rcShow.left);
	int iHeight = (rcShow.bottom - rcShow.top);
	m_pVideo->SetParent((CWnd *)this);
	m_pVideo->ModifyStyle(WS_POPUP, WS_CHILD);
	RECT rcVideo = {0};
	m_pVideo->MoveWindow(&rcShow);
	m_pVideo->ShowWindow(SW_SHOW);
	m_pVideo->RedrawWindow();
	CLS_LanguageManager::Instance()->SetLanguage(0); //Init Language is Chinese
	UpdateDialogText();
	m_cboLanguage.AddString(GetTextByLan(_T("中文"), _T("Chinese")));
	m_cboLanguage.AddString(GetTextByLan(_T("英文"), _T("English")));
	
	LANGID Systemlid = GetSystemDefaultLangID(); 
	if (0x0804 == Systemlid || 0x1004 == Systemlid)
	{
		m_cboLanguage.SetCurSel(0);
	}
	else
	{
		m_cboLanguage.SetCurSel(1);
	}
	
	m_DevIP.SetWindowText("192.168.1.2");
	m_edtUser.SetWindowText("Admin");
	m_edtPassword.SetWindowText("1111");
    m_edtPort.SetWindowText("3000");//default port 3000
	//LoadDLL
	int iRet = SDKInit();

	if (-1 == iRet)
	{
		MessageBox("Warning: Init SDK failed! Porgram will exit!");
		exit(0);
	}

	for (int i = 0; i < MAX_PTZ_NUM; i++)
	{
		CString strPTZNo;
		strPTZNo.Format("%d", i+1);
		m_cboPreNum.InsertString(i, strPTZNo);
	}

	m_slider_bright.SetRange(0,255,TRUE);
	m_slider_bright.SetPos(128);
	m_slider_hue.SetRange(0,255,TRUE);
	m_slider_hue.SetPos(128);
	m_slider_saturation.SetRange(0,255,TRUE);
	m_slider_saturation.SetPos(128);
	m_slider_contrast.SetRange(0,255,TRUE);
	m_slider_contrast.SetPos(128);
	m_sldSpeed.SetRange(0,100,TRUE);
	m_sldSpeed.SetPos(50);

	m_listInfo.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	SetLogCtrl(&m_listInfo);
	m_listInfo.InsertColumn(0, _T("Time"), LVCFMT_LEFT, 120);
    m_listInfo.InsertColumn(1, _T("State"), LVCFMT_LEFT, 120);
	m_listInfo.InsertColumn(2, _T("Operate"),LVCFMT_LEFT, 360);


	OnCbnSelchangeComboLanguage();

	return TRUE;  // Returns TRUE unless focus is set to the control
}

void CTestDemoDlg::OnSysCommand(UINT nID, LPARAM lParam)
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

// If you add a minimize button to the dialog, you need the following code
// to draw the icon. For MFC applications using document/view models,
// This will be done automatically by the framework.

void CTestDemoDlg::OnPaint()
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

		// draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

//When the user drags the minimized window, the system calls this function to get the cursor
//show.
HCURSOR CTestDemoDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CTestDemoDlg::UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_LANGUAGE, GetTextByLan(_T("语言"), _T("Language")));
	SetDlgItemText(IDC_STATIC_LOG, GetTextByLan(_T("设备登陆"), _T("Device Log")));
	SetDlgItemText(IDC_STATIC_DEVIP, GetTextByLan(_T("设备IP"), _T("Device IP")));
	SetDlgItemText(IDC_STATIC_USERNAME, GetTextByLan(_T("用户名"), _T("UserName")));
	SetDlgItemText(IDC_STATIC_PSD, GetTextByLan(_T("密码"), _T("Password")));
	SetDlgItemText(IDC_STATIC_PORT, GetTextByLan(_T("端口号"), _T("PortNo")));
	SetDlgItemText(IDC_BUTTON_LOGON, GetTextByLan(_T("登陆"),_T("Log In")));
	SetDlgItemText(IDC_BUTTON_LOGOFF, GetTextByLan(_T("注销"), _T("Log Off")));
	SetDlgItemText(IDC_STATIC_CHANNEL, GetTextByLan(_T("通道号"), _T("Channel No")));
	SetDlgItemText(IDC_STATIC_STREAMNO, GetTextByLan(_T("码流类型"),_T("Stream Type")));
	SetDlgItemText(IDC_BUTTON_SNAP, GetTextByLan(_T("抓拍"), _T("Snap")));
	SetDlgItemText(IDC_BUTTON_3DLOCATION, GetTextByLan(_T("3D定位开启"), _T("Open 3D Location")));
	SetDlgItemText(IDC_STATIC_SPEED, GetTextByLan(_T("速度"), _T("Speed")));
	SetDlgItemText(IDC_STATIC_PTZLIST, GetTextByLan(_T("预置位编号"), _T("PTZNo")));
	SetDlgItemText(IDC_BUTTON_SETPTZ, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_CALLPTZ, GetTextByLan(_T("调用"),_T("Call")));
	SetDlgItemText(IDC_STATIC_COLOR, GetTextByLan(_T("色度"),_T("Hue")));
	SetDlgItemText(IDC_STATIC_LIANG,GetTextByLan(_T("亮度"),_T("Brightness")));
	SetDlgItemText(IDC_STATIC_COMPARE, GetTextByLan(_T("对比度"),_T("Contrast")));
	SetDlgItemText(IDC_STATIC_SATURATION, GetTextByLan(_T("饱和度"),_T("Saturation")));
	SetDlgItemText(IDC_BUTTON_DEFAULT, GetTextByLan(_T("默认值"), _T("Default")));
	SetDlgItemText(IDC_BUTTON_CONNECTVIDEO, GetTextByLan(_T("连接视频"), _T("Connect Video")));
	SetDlgItemText(IDC_BUTTON_DISCONNECT, GetTextByLan(_T("断开视频"), _T("Disconnect Video")));
	SetDlgItemText(IDC_BUTTON_SAVELOG, GetTextByLan(_T("保存日志"), _T("Save Log")));
	SetDlgItemText(IDC_BUTTON_CLEARLOG, GetTextByLan(_T("清除日志"),_T("Clear Log")));
	SetDlgItemText(IDC_CHECK_RECORD, GetTextByLan(_T("录像"), _T("Record")));
}

void CTestDemoDlg::OnCbnSelchangeComboLanguage()
{
	// TODO: Add your control notification handler code here
	int iSel = m_cboLanguage.GetCurSel();
	CLS_LanguageManager::Instance()->SetLanguage(iSel);
	UpdateDialogText();
	m_cboLanguage.ResetContent();
	m_cboLanguage.AddString(GetTextByLan(_T("中文"), _T("Chinese")));
	m_cboLanguage.AddString(GetTextByLan(_T("英文"), _T("English")));
	m_cboLanguage.SetCurSel(iSel);

	int iCount = m_cboStreamNo.GetCount();
	int iSelStream = m_cboStreamNo.GetCurSel();
	m_cboStreamNo.ResetContent();
	if (MAX_STREAM == iCount)
	{
		m_cboStreamNo.InsertString(0, GetTextByLan(_T("主码流"), _T("Main Stream")));
		m_cboStreamNo.InsertString(1, GetTextByLan(_T("副码流"), _T("Sub Stream")));
	}
	else if (1 == iCount)
	{
		m_cboStreamNo.InsertString(0, GetTextByLan(_T("主码流"), _T("Main Stream")));
	}
	if (-1 != iSelStream)
	{
		m_cboStreamNo.SetCurSel(iSelStream);
	}
}

void CTestDemoDlg::OnDestroy()
{
	CDialog::OnDestroy();
	CLS_LanguageManager::Destroy();
	Destroy();
	m_pVideo->DestroyWindow();
	delete m_pVideo;
	m_pVideo = NULL;
	if (m_bIsLogon)
	{
		NetClient_Logoff(m_LogonID);
	}
	{
		NetClient_Cleanup();
	}
	// TODO: Add your message handler code here
}

void CTestDemoDlg::OnBnClickedButtonLogon()
{
	// TODO: Add your control notification handler code here
	//Login Device
	CString strIP,strUserName,strPassWord,strPort;
	m_DevIP.GetWindowText(strIP);
	m_edtUser.GetWindowText(strUserName);
	m_edtPassword.GetWindowText(strPassWord);
	m_edtPort.GetWindowText(strPort);
	if (strIP == "" || strUserName == "" || strPort == "" || strPassWord == "")
	{
		MessageBox("User Infomation is not valid");
		return ;
	}
	LogonPara tLogonPara = {0};
	tLogonPara.iSize = sizeof(LogonPara);
	strcpy_s(tLogonPara.cUserName, sizeof(tLogonPara.cUserName), strUserName.GetBuffer());
	strcpy_s(tLogonPara.cUserPwd, sizeof(tLogonPara.cUserPwd), strPassWord.GetBuffer());
	strcpy_s(tLogonPara.cNvsIP, sizeof(tLogonPara.cNvsIP), strIP.GetBuffer());
	tLogonPara.iNvsPort = _ttoi(strPort);

	m_LogonID = NetClient_Logon_V4(SERVER_NORMAL, &tLogonPara, sizeof(tLogonPara));

}

int CTestDemoDlg::SDKInit()
{
	int iRet = LoadNVSSDK();
	if (DLL_LOAD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","LoadNVSSDK fail!");
		return -1;
	}
	iRet = NetClient_SetSDKWorkMode(0);//Default heavyweight login

	//Set the TCP window size, which can alleviate the problem of video freezing when the network delay is large to a certain extent
	NetClient_SetDevConfig(-1, NET_CLIENT_ENABLE_NET_OPTIMIZE, 0, NULL, 0);

	//Initialize the interface library
	int iServerPort = 0;
	int iClientPort = 6000;

	NetClient_SetNotifyFunction_V4(MainNotify, NULL, NULL, NULL, NULL);
	return NetClient_Startup_V4(iServerPort,iClientPort,0);
}

void CTestDemoDlg::MainNotify(int _ulLogonID, long _iWparam, void* _iParam,void* _iUser)
{
	int iMsgType = LOWORD(_iWparam);
	CWnd* pWnd = AfxGetApp()->GetMainWnd();	
	pWnd->PostMessage(WM_MAIN_MSG, _iWparam, (long)_iParam);
}

LRESULT CTestDemoDlg::OnMainNotify( WPARAM wParam, LPARAM lParam )
{
	int iMsgType = LOWORD(wParam);
	switch (iMsgType)
	{
	case WCM_LOGON_NOTIFY:
		if (LOGON_SUCCESS == (int) lParam)
		{
			AddLog(LOG_TYPE_SUCC,"%d","WCM_LOGON_NOTIFY(%d)", m_LogonID, lParam);
			int iChannelNum = 0, iType = 0;
			m_bIsLogon = TRUE;
			GetDlgItem(IDC_BUTTON_LOGON)->EnableWindow(FALSE);
			//Judge The Number of Channel
			int iRet = NetClient_GetChannelNum(m_LogonID,&iChannelNum);
			if (0 == iRet)
			{
				m_ChannelNo.ResetContent();
				for (int i = 0;i < iChannelNum; i++)
				{
					CString str;
					str.Format("%d", i);
					m_ChannelNo.AddString(str);
				}
			}
			m_ChannelNo.SetCurSel(0);
			iRet = NetClient_GetProductType(m_LogonID, &iType);
			if (0 == iRet)
			{
				m_cboStreamNo.ResetContent();  
				if (iType & 0x080000)
				{
					m_cboStreamNo.AddString(GetTextByLan(_T("主码流"), _T("Main Stream")));
					m_cboStreamNo.AddString(GetTextByLan(_T("副码流"), _T("Sub Stream")));
				}
				else
					m_cboStreamNo.AddString(GetTextByLan(_T("主码流"), _T("Main Stream")));
			}
			m_cboStreamNo.SetCurSel(0);
			//start to receive data
			//StartRecv();

			if(SuppotNew3D())
			{
				GetDlgItem(IDC_BUTTON_3DLOCATION)->EnableWindow(TRUE);
			}
			else 
			{
				GetDlgItem(IDC_BUTTON_3DLOCATION)->EnableWindow(FALSE);
			}
			UI_UpdatePTZ();
			UI_UpdateVideoParam();
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"%d","WCM_LOGON_NOTIFY(%d)", m_LogonID, lParam);
			m_bIsLogon = FALSE;
			GetDlgItem(IDC_BUTTON_LOGOFF)->EnableWindow(TRUE);
		}
		break;
	case WCM_VIDEO_HEAD:
		{
			//receive the head video data
			StartPlay(m_iConnectID);
			m_bIsDisplay = TRUE;
			int iWidth = 0, iHeight = 0,iStreamNo = 0, iChannelNo = 0;
			memset(&m_rcVideo, 0, sizeof(RECT));
			iStreamNo = m_cboStreamNo.GetCurSel();
			iChannelNo = m_ChannelNo.GetCurSel();
			int iRet = NetClient_GetVideoSize(m_LogonID, iChannelNo, &iWidth,&iHeight, iStreamNo);
			if (0 == iRet)
			{
				m_rcVideo.right = iWidth;
				m_rcVideo.bottom = iHeight;
			}

		}
		break;
	}
	return 0;
}
void CTestDemoDlg::OnBnClickedButtonLogoff()
{
	// TODO: Add your control notification handler code here
	if (m_LogonID < 0)
	{
		return;
	}
	else{
		int iRet = NetClient_Logoff(m_LogonID);
		if (iRet == 0)
		{
			GetDlgItem(IDC_BUTTON_LOGON)->EnableWindow(TRUE);
			m_bIsLogon = FALSE;
			m_bIsDisplay = FALSE;
			m_ChannelNo.ResetContent();
			m_cboStreamNo.ResetContent();
			m_LogonID = -1;
		}
		else
		{
			GetDlgItem(IDC_BUTTON_LOGON)->EnableWindow(FALSE);
		}
	}
}

void CTestDemoDlg::StartPlay(unsigned int _uConID)
{
	RECT rc = {0};
	HWND hWnd = GetDlgItem(IDC_STATIC_VIDEO)->GetSafeHwnd();
	NetClient_StopPlay(_uConID);//Stop playing video
	if (hWnd)
	{
	int iRet = NetClient_StartPlay(_uConID, (int)m_pVideo->GetSafeHwnd(), rc, H264DEC_DECTWO);//Start playing video
		if(iRet >= 0)
		{
			printf("StartPlay success!\n");
		}
		else
		{
			printf("StartPlay failed!\n");
		}
	}

}

void CTestDemoDlg::SwitchPara()
{
	//Switch the Channel
	StartRecv();
	if(SuppotNew3D())
	{
		GetDlgItem(IDC_BUTTON_3DLOCATION)->EnableWindow(TRUE);
	}
	else 
	{
		GetDlgItem(IDC_BUTTON_3DLOCATION)->EnableWindow(FALSE);
	}
	UI_UpdatePTZ();
	UI_UpdateVideoParam();
}

void CTestDemoDlg::StartRecv()
{
	OnBnClickedButtonDisconnect();

	NetClientPara tPara = {0};
	tPara.iSize = sizeof(NetClientPara);
	tPara.tCltInfo.m_iServerID = m_LogonID;
	tPara.tCltInfo.m_iChannelNo = m_ChannelNo.GetCurSel();
	tPara.tCltInfo.m_iStreamNO = m_cboStreamNo.GetCurSel();
	tPara.tCltInfo.m_iNetMode = NETMODE_TCP;//1-private tcp connect, 2-private udp connect, 3-private multicast connect, 6-rtsp stream via RTP-over-TCP,
	//7-rtsp stream via RTP-over-UDP, 8-rtsp stream via RTP-over-Multicast, 9-rtsps stream via SRTP-over-UDP, 10-rtsps stream via SRTP-over-Multicast
	tPara.tCltInfo.m_iTimeout = 20;
	unsigned int uiConnId = -1;
	int iRet = NetClient_StartRecv_V5(&uiConnId, &tPara, sizeof(NetClientPara));
	if(iRet >= 0)
	{
		//add success log
		m_iConnectID = uiConnId;
		m_pVideo->SetConnID(m_iConnectID, m_ChannelNo.GetCurSel(), m_LogonID, tPara.tCltInfo.m_iStreamNO);
		AddLog(LOG_TYPE_SUCC,"","NetClient_StartRecv_V5(%d,%d,%d)"
			, m_LogonID, tPara.tCltInfo.m_iChannelNo, m_iConnectID);
	}
	else 
	{
		//add fail log
		m_pVideo->SetConnID(-1, -1, -1, -1);
		AddLog(LOG_TYPE_FAIL,"","NetClient_StartRecv_V5(%d,%d,%d)"
			, m_LogonID, tPara.tCltInfo.m_iChannelNo,m_iConnectID);
	}
}
void CTestDemoDlg::OnCbnSelchangeCboChannel()
{
	// TODO: Add your control notification handler code here
	SwitchPara();

}

void CTestDemoDlg::OnCbnSelchangeComboStream()
{
	// TODO: Add your control notification handler code her
	SwitchPara();
}


void CTestDemoDlg::OnBnClickedButtonSnatch()
{
	// TODO: Add your control notification handler code here
	if (m_bIsDisplay && m_iConnectID != -1)
	{
		RECT rcShow = {0};
		GetDlgItem(IDC_BUTTON_SNAP)->GetWindowRect(&rcShow);
		CMenu menu;
		menu.CreatePopupMenu();
		menu.AppendMenu(MF_STRING,ID_SNATCH_BMP,"bmp");
		menu.AppendMenu(MF_STRING,ID_SNATCH_YUV,"yuv");
		menu.AppendMenu(MF_STRING,ID_SNATCH_JPG,"jpg");
		menu.TrackPopupMenu(TPM_LEFTALIGN | TPM_RIGHTBUTTON,rcShow.left+2,rcShow.bottom,this);
		menu.DestroyMenu();
	}
}

void CTestDemoDlg::OnSnatch(UINT nID)
{
	if (-1 == m_iConnectID)
	{
		return;
	}

	CString strFileName = MakeFileName();
	int  iType = 0;
	switch(nID)
	{
	case ID_SNATCH_BMP:
	case ID_SNATCH_JPG:
		{
			if (ID_SNATCH_BMP == nID)
			{
				strFileName.AppendFormat(_T(".bmp"));
				iType = 1;
			}
			else
			{
				strFileName.AppendFormat(_T(".jpg"));
				iType = 2;
			}
			int iSize = NetClient_CapturePicture(m_iConnectID,iType, (LPSTR)(LPCTSTR)strFileName);
			if (iSize >= 0)
			{
				AddLog(LOG_TYPE_SUCC,"","(%d)NetClient_CaptureBmpPic(%u,%s)",iSize,m_iConnectID,(LPSTR)(LPCTSTR)strFileName);
			}
			else
			{
				AddLog(LOG_TYPE_FAIL,"","(%d)NetClient_CaptureBmpPic(%u,%s)",iSize,m_iConnectID,(LPSTR)(LPCTSTR)strFileName);
			}
		}
		break;
	case ID_SNATCH_YUV:
		{
			unsigned char *dataP = NULL;
			int iSize = NetClient_CapturePic(m_iConnectID,&dataP);
			if(iSize <= 0)
			{
				AddLog(LOG_TYPE_FAIL,"","(%d)NetClient_CapturePic(%u,%#x)",iSize,m_iConnectID,dataP);
				return;
			}
			strFileName.AppendFormat(_T(".yuy2"));
			FILE *fp = _fsopen((LPSTR)(LPCTSTR)strFileName,"wb+",0x40);
			if (fp == NULL)
			{
				AddLog(LOG_TYPE_FAIL,"","fopen(%s)",strFileName);
				return;
			}
			fwrite(dataP,sizeof(char),iSize,fp);
			fflush(fp);
			fclose(fp);
			AddLog(LOG_TYPE_SUCC,"","(%d)NetClient_CapturePic(%u,%s)",iSize,m_iConnectID,strFileName);
		}
		break;
	default:break;
	}
}

CString CTestDemoDlg::MakeFileName()
{
	CString strSavePath;
	char cFilePath[MAX_PATH] = {0};
	int iSize = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
	if (iSize <= 0)
	{
		strcpy_s(cFilePath,sizeof(cFilePath),"C:\\");
	}
	strSavePath.Format(_T("%s"),cFilePath);
	int iPos = strSavePath.ReverseFind('\\');
	if (iPos >= 0)
	{
		strSavePath = strSavePath.Left(iPos);
	}
	strSavePath.AppendFormat(_T("\\PreviewDemo"));
	CreateDirectory(strSavePath, NULL);

	SYSTEMTIME tmNow = {0};
	GetLocalTime(&tmNow);
	strSavePath.Format(_T("%s\\%04d%02d%02d%02d%02d%02d%04d"),strSavePath,tmNow.wYear,tmNow.wMonth,tmNow.wDay,tmNow.wHour,tmNow.wMinute,tmNow.wSecond,tmNow.wMilliseconds);

	return strSavePath;
}

BOOL CTestDemoDlg::SuppotNew3D()
{
	int iLogonID = m_LogonID;
	int iChannelNo = m_ChannelNo.GetCurSel();
	bool bFlag = false;

	//bit0 defogging: 0-not supported, 1-supported defogging
	//bit1 New 3D positioning protocol: 0-not supported, 1-supported
	//bit2 supports compass: 0-not supported, 1-supported
	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_DOME_PARA;
	stFuncAbilityLevel.iSubFuncType = 0;
	int iReturnByte = -1;
	int iRet = NetClient_GetDevConfig(iLogonID, NET_CLIENT_GET_FUNC_ABILITY, iChannelNo, &stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (0 == iRet)
	{
		int iFuncPara = _ttoi(stFuncAbilityLevel.cParam);
		if (0 != (iFuncPara & 0x02))
		{
			bFlag = true;
		}
	}
	return bFlag;
}
void CTestDemoDlg::OnBnClickedButton3dlocation()
{
	// TODO: Add your control notification handler code here
	CString strState = "";
	GetDlgItem(IDC_BUTTON_3DLOCATION)->GetWindowText(strState);
	if (strState == GetTextByLan(_T("3D定位开启"), _T("Open 3D Location")))
	{
		strState = GetTextByLan(_T("3D定位关闭"), _T("Close 3D Location"));
	}
	else
	{
		strState =  GetTextByLan(_T("3D定位开启"), _T("Open 3D Location"));
	}
	GetDlgItem(IDC_BUTTON_3DLOCATION)->SetWindowText(strState);
	m_pVideo->m_bIsOpen3DLocation = !m_pVideo->m_bIsOpen3DLocation;
	m_bIsOpen3DLocation = !m_bIsOpen3DLocation;//this function just set a parameter,if want to see how to use
	                                           //the interface of 3D Location, look it in the function"OnLButtonUp" in class "VideoView"
}

void CTestDemoDlg::OnLButtonDown(UINT nFlags, CPoint point)
{
	// TODO: Add your message handler code here and/or call default
	CDialog::OnLButtonDown(nFlags, point);
}

BOOL CTestDemoDlg::IsInVideoView()
{
	POINT pointCursor = {0};
	CRect rect;
	GetCursorPos(&pointCursor);
	GetDlgItem(IDC_STATIC_VIDEO)->GetWindowRect(&rect);
	//GetDlgItem(IDC_STATIC_VIDEO)->ScreenToClient(&rect);
	if (PtInRect(&rect, pointCursor))
	{
		return TRUE;
	}
	return FALSE;
}

void CTestDemoDlg::OnMouseMove(UINT nFlags, CPoint point)
{
	// TODO: Add your message handler code here and/or call default
	if (MK_LBUTTON == nFlags && m_bIsOpen3DLocation && IsInVideoView() && m_bIsDisplay)
	{
		/*RECT rect;
		memset(&rect, 0, sizeof(rect));
		GetDlgItem(IDC_STATIC_VIDEO)->GetWindowRect(&rect);
		m_rcDrag.right = point.x - rect.left;
		m_rcDrag.bottom = point.y - rect.top;

		RECT rcVideo = {0};
		ClientToVideo(m_rcDrag,rcVideo);
		NetClient_DrawRectOnLocalVideo(m_iConnectID,&rcVideo,1);

		TRACKMOUSEEVENT tme; 
		tme.cbSize = sizeof(TRACKMOUSEEVENT);
		tme.dwFlags = TME_LEAVE;
		tme.dwHoverTime = HOVER_DEFAULT;
		tme.hwndTrack = m_hWnd;
		TrackMouseEvent(&tme);*/
	}
	CDialog::OnMouseMove(nFlags, point);
}

int CTestDemoDlg::ClientToVideo(RECT& _rcScreen,OUT RECT& _rcVideo)
{
	if (0 == m_rcVideo.right || 0 == m_rcVideo.bottom)
	{
		memset(&_rcVideo,0,sizeof(RECT));
		return -1;
	}

	_rcVideo.left = min(_rcScreen.left,_rcScreen.right);
	_rcVideo.top = min(_rcScreen.top,_rcScreen.bottom);
	_rcVideo.right = max(_rcScreen.left,_rcScreen.right);
	_rcVideo.bottom = max(_rcScreen.top,_rcScreen.bottom);

	int iWidth = m_rcVideo.right-m_rcVideo.left;
	int iHeight = m_rcVideo.bottom-m_rcVideo.top;
	int iSWidth = 0;
	int iSHeight = 0;
	RECT rcClient = {0};
	GetDlgItem(IDC_STATIC_VIDEO)->GetClientRect(&rcClient);
	_rcVideo.left = (_rcVideo.left*iWidth + rcClient.right/2)/rcClient.right;
	_rcVideo.top = (_rcVideo.top*iHeight + rcClient.bottom/2)/rcClient.bottom;
	_rcVideo.right = (_rcVideo.right*iWidth + rcClient.right/2)/rcClient.right;
	_rcVideo.bottom = (_rcVideo.bottom*iHeight + rcClient.bottom/2)/rcClient.bottom;	


	return 0;
}


void CTestDemoDlg::OnLButtonUp(UINT nFlags, CPoint point)
{
	// TODO: Add your message handler code here and/or call default
	if (m_bIsOpen3DLocation && IsInVideoView())
	{
		/*RECT rect;
		memset(&rect, 0, sizeof(rect));
		GetDlgItem(IDC_STATIC_VIDEO)->GetWindowRect(&rect);
		m_rcDrag.right = point.x - rect.left;
		m_rcDrag.bottom = point.y - rect.top;
		DrawVideoArea(m_rcDrag);
		memset(&m_rcDrag,0xff,sizeof(RECT));*/
	}
	CDialog::OnLButtonUp(nFlags, point);
}

int CTestDemoDlg::DrawVideoArea(RECT& _rcDrag)
{
	RECT rcVideo = {0};
	ClientToVideo(_rcDrag,rcVideo);
	if (m_bIsOpen3DLocation && m_bIsDisplay )
	{
		Locate3DPosition t3dInfo = {0}; 
		t3dInfo.iBufSize = sizeof(t3dInfo);

		CRect rcShow;
		GetDlgItem(IDC_STATIC_VIDEO)->GetClientRect(&rcShow);
		CRect rcDraw = _rcDrag; //The area, point or rectangle drawn on the video

		//Need to convert the coordinates to ten thousand points
		if (rcDraw.left == rcDraw.right && rcDraw.top == rcDraw.bottom)	
		{ // draw 1 point
			t3dInfo.iPointNum = 1;
			t3dInfo.tPoint[0].iX = rcDraw.left*10000/rcShow.Width();
			t3dInfo.tPoint[0].iY = rcDraw.top*10000/rcShow.Height();
		}
		else // draw a rectangle
		{
			t3dInfo.iPointNum = 2;
			t3dInfo.tPoint[0].iX = rcDraw.left*10000/rcShow.Width();
			t3dInfo.tPoint[0].iY = rcDraw.top*10000/rcShow.Height();
			t3dInfo.tPoint[1].iX = rcDraw.right*10000/rcShow.Width();
			t3dInfo.tPoint[1].iY = rcDraw.bottom*10000/rcShow.Height();
		}

		int iRet = NetClient_SendCommand(m_LogonID, COMMAND_ID_3D_POSITION, m_ChannelNo.GetCurSel(), &t3dInfo, sizeof(t3dInfo));
		if(0 != iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","3D NetClient_SendCommand(%d,%d)",m_LogonID, m_ChannelNo.GetCurSel());
		}
		NetClient_DrawRectOnLocalVideo(m_iConnectID,NULL,0);
	}
	return 0;
}

BOOL CTestDemoDlg::PreTranslateMessage(MSG* pMsg)
{
	if( pMsg->message == WM_LBUTTONDOWN)
	{
		switch(::GetDlgCtrlID(pMsg->hwnd))
		{
		case IDC_BUTTON_MOVE_UP://The control button to move up is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_UP,0,iSpeed,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_UP,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		case IDC_BUTTON_DOWN://Control button to move down is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_DOWN,0,iSpeed,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_DOWN,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		case IDC_BUTTON_MOVE_LEFT://Control button to move left is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_LEFT,iSpeed,0,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_LEFT,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		case IDC_BUTTON_MOVE_RIGHT://Control button to move right is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_RIGHT,iSpeed,0,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_RIGHT,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		case IDC_BUTTON_MOVE_UPLEFT://The control button to move up and left is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_UP_LEFT,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_UP_LEFT,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		case IDC_BUTTON_MOVE_UP_RIGHT://The control button to move up to the right is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_UP_RIGHT,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_UP_RIGHT,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		case IDC_BUTTON_DOWN_LEFT://The control button that moves down to the left is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_DOWN_LEFT,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_DOWN_LEFT,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		case IDC_BUTTON_DOWN_RIGHT://The control button that moves down to the right is pressed
			{
				BOOL bRet = FALSE;
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					bRet = ProtocolControl(PROTOCOL_MOVE_DOWN_RIGHT,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					bRet = TransparentControl(MOVE_DOWN_RIGHT,m_iAddress,iSpeed,0);
				}
				if (TRUE == bRet)
				{
					CheckDlgButton(IDC_CHECK_AUTO,FALSE);
				}
				m_hReverse = pMsg->hwnd;
				SetTimer(1,500,NULL);
			}
			break;
		/*case IDC_BUTTON_FOCUS_NEAR://Focus near button press
			{
				if (1 == m_iWorkMode)
				{
					int iSpeed = m_sldSpeed.GetPos();
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_FOCUS_ON,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(FOCUS_NEAR,m_iAddress,0,0);
				}
			}
			break;
		case IDC_BUTTON_FOCUS_FAR://Focus far button pressed
			{
				if (1 == m_iWorkMode)
				{
					int iSpeed = m_sldSpeed.GetPos();
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_FOCUS_OFF,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(FOCUS_FAR,m_iAddress,0,0);
				}
			}
			break;
		case IDC_BUTTON_ZOOM_BIG://Zoom button press
			{
				if (1 == m_iWorkMode)
				{
					int iSpeed = m_sldSpeed.GetPos();
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_ZOOMIN,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(ZOOM_BIG,m_iAddress,0,0);
				}
			}
			break;
		case IDC_BUTTON_ZOOM_SMALL://Zoom small button press
			{
				if (1 == m_iWorkMode)
				{
					int iSpeed = m_sldSpeed.GetPos();
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_ZOOMOUT,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(ZOOM_SMALL,m_iAddress,0,0);
				}
			}
			break;
		case IDC_BUTTON_IRIS_OPEN://The button to open the aperture is pressed
			{
				if (1 == m_iWorkMode)
				{
					int iSpeed = m_sldSpeed.GetPos();
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_IRIS_OPEN,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(IRIS_OPEN,m_iAddress,0,0);
				}
			}
			break;
		case IDC_BUTTON_IRIS_CLOSE://The button to close the iris is pressed
			{
				if (1 == m_iWorkMode)
				{
					int iSpeed = m_sldSpeed.GetPos();
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_IRIS_CLOSE,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(IRIS_CLOSE,m_iAddress,0,0);
				}
			}
			break;*/
		default :
			break;
		}
	}
	else if( pMsg->message == WM_LBUTTONUP)
	{
		switch(::GetDlgCtrlID(pMsg->hwnd))
		{
		case IDC_BUTTON_MOVE_UP://release the control button to move up
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,0,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_UP_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		case IDC_BUTTON_DOWN://Release the control button to move down
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,0,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_DOWN_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		case IDC_BUTTON_MOVE_LEFT://release the control button to move left
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,iSpeed,0,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_LEFT_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		case IDC_BUTTON_MOVE_RIGHT://Release the control button to move right
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,iSpeed,0,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_RIGHT_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		case IDC_BUTTON_MOVE_UPLEFT://Release the control button to move up and to the left
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_UP_LEFT_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		case IDC_BUTTON_MOVE_UP_RIGHT://Release the control button to move up to the right
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_UP_RIGHT_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		case IDC_BUTTON_DOWN_LEFT://release the control button that moves down to the left
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_DOWN_LEFT_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		case IDC_BUTTON_DOWN_RIGHT://Release the lower right control button
			{
				int iSpeed = m_sldSpeed.GetPos();
				if (1 == m_iWorkMode)
				{
					BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
					ProtocolControl(PROTOCOL_MOVE_STOP,iSpeed,iSpeed,bEPTZ);
				}
				else
				{
					TransparentControl(MOVE_DOWN_RIGHT_STOP,m_iAddress,iSpeed,0);
				}
				m_hReverse = NULL;
				KillTimer(1);
			}
			break;
		default :
			break;
		}
	}
	else if (pMsg->message == WM_KEYDOWN && (pMsg->wParam == VK_ESCAPE || pMsg->wParam == VK_RETURN))
	{
		return TRUE;
	}

	return CDialog::PreTranslateMessage(pMsg);
}

BOOL CTestDemoDlg::ProtocolControl( int _iAction,int _iParam1,int _iParam2,int _iEPTZ )
{
	if (m_LogonID < 0)
	{
		return FALSE;
	}

	int iRet = NetClient_DeviceCtrlEx(m_LogonID,m_ChannelNo.GetCurSel()
		,_iAction,_iParam1,_iParam2,_iEPTZ);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_DeviceCtrlEx(%d)",m_LogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_DeviceCtrlEx(%d)",m_LogonID);
	}
	return TRUE;
}

BOOL CTestDemoDlg::TransparentControl(int _iAction,int _iAddress, int _iSpeed, int _iPreset)
{
	if (m_LogonID < 0)
	{
		return FALSE;
	}

	CONTROL_PARAM ctrlParam = {0};
	ctrlParam.m_iAddress = _iAddress;
	if(0 == memcmp(m_cDeviceType,"DOME",4))
	{
		if(_iAction>0 && _iAction <= 16)
		{
			switch(_iAction)
			{
			case MOVE_UP:
				ctrlParam.m_ptMove.x = 0;
				ctrlParam.m_ptMove.y = _iSpeed;
				_iAction = MOVE;
				break;
			case MOVE_DOWN:
				ctrlParam.m_ptMove.x = 0;
				ctrlParam.m_ptMove.y = -_iSpeed;
				_iAction = MOVE;
				break;
			case MOVE_LEFT:
				ctrlParam.m_ptMove.x = -_iSpeed;
				ctrlParam.m_ptMove.y = 0;
				_iAction = MOVE;
				break;
			case MOVE_RIGHT:
				ctrlParam.m_ptMove.x = _iSpeed;
				ctrlParam.m_ptMove.y = 0;
				_iAction = MOVE;
				break;
			case MOVE_UP_LEFT:
				ctrlParam.m_ptMove.x = -_iSpeed;
				ctrlParam.m_ptMove.y = _iSpeed;
				_iAction = MOVE;
				break;
			case MOVE_UP_RIGHT:
				ctrlParam.m_ptMove.x = _iSpeed;
				ctrlParam.m_ptMove.y = _iSpeed;
				_iAction = MOVE;
				break;
			case MOVE_DOWN_LEFT:
				ctrlParam.m_ptMove.x = -_iSpeed;
				ctrlParam.m_ptMove.y = -_iSpeed;
				_iAction = MOVE;
				break;
			case MOVE_DOWN_RIGHT:
				ctrlParam.m_ptMove.x = _iSpeed;
				ctrlParam.m_ptMove.y = -_iSpeed;
				_iAction = MOVE;
				break;
			default:
				_iAction = MOVE_STOP;
				break;
			}
		}
	}
	else
	{
		AddLog(LOG_TYPE_MSG,"","[DeviceControl] device type(%s) is not DOME",m_cDeviceType);
	}
	//preset:
	if((_iAction == CALL_VIEW)||(_iAction == SET_VIEW))
	{
		ctrlParam.m_iPreset = _iPreset;
	}

	//Get the real action code
	int iRet = m_tDevCtrl.GetCtrlCode(m_cDeviceType,_iAction,&ctrlParam);
	int m_iChannelNo = m_ChannelNo.GetCurSel();
	if(0 == iRet)
	{
		CString strOut = Bytes2HexString(ctrlParam.m_btBuf,ctrlParam.m_iCount);
		int iChannelProperty = -1;
		int iRet = NetClient_GetChannelProperty(m_LogonID, m_iChannelNo
			, GENERAL_CMD_GET_CHANNEL_TYPE, &iChannelProperty, sizeof(iChannelProperty));
		if (0 == iRet)
		{
			if(iChannelProperty == 2)
			{
				int iRet = NetClient_DigitalChannelSend(m_LogonID, m_iChannelNo
					,ctrlParam.m_btBuf, ctrlParam.m_iCount);
				if (0 == iRet)
				{
					AddLog(LOG_TYPE_SUCC,"","NetClient_DigitalChannelSend(%d,%d,%s,%d)",m_LogonID
						, m_iChannelNo,(LPSTR)(LPCTSTR)strOut, ctrlParam.m_iCount);
				}
				else
				{
					AddLog(LOG_TYPE_FAIL,"","NetClient_DigitalChannelSend(%d,%d,%s,%d)",m_LogonID
						, m_iChannelNo,(LPSTR)(LPCTSTR)strOut, ctrlParam.m_iCount);
				}
				return TRUE;
			}
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetChannelProperty(%d,%d,CHANNEL_TYPE,,)",m_LogonID
				,m_ChannelNo.GetCurSel());
		}

		iRet = NetClient_ComSend(m_LogonID,ctrlParam.m_btBuf, ctrlParam.m_iCount,m_iComNo);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_ComSend(%d,%s,%d,%d)",m_LogonID,
				(LPSTR)(LPCTSTR)strOut, ctrlParam.m_iCount,m_iComNo);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_ComSend(%d,%s,%d,%d)",m_LogonID
				,(LPSTR)(LPCTSTR)strOut, ctrlParam.m_iCount,m_iComNo);
		}
		return TRUE;
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","GetCtrlCode(%s,%d)",m_cDeviceType,_iAction);
		return FALSE;
	}
}

BOOL CTestDemoDlg::UI_UpdatePTZ()
{
	GetDlgItem(IDC_CHECK_EPTZ)->ShowWindow(SW_HIDE);
	if (m_LogonID < 0)
	{
		return FALSE;
	}

	int iProductType = 0;
	int iRet = NetClient_GetProductType(m_LogonID,&iProductType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetProductType(%d,%d)"
			,m_LogonID, iProductType);

		iProductType &= 0xFFFF;
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetProductType(%d,%d)"
			,m_LogonID, iProductType);
	}

	int iChannelNo = 0;
	if (IsDVR(iProductType)||iProductType==0x64)
	{
		iChannelNo = m_ChannelNo.GetCurSel();
	}

	m_iComNo = 1;
	m_iAddress = 0;
	iRet = NetClient_GetDeviceType(m_LogonID,iChannelNo,&m_iComNo,&m_iAddress,m_cDeviceType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDeviceType(%d,%d,%d,%d,%s)"
			,m_LogonID,iChannelNo,m_iComNo,m_iAddress,m_cDeviceType);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDeviceType(%d,%d,%d,%d,%s)"
			,m_LogonID,iChannelNo,m_iComNo,m_iAddress,m_cDeviceType);
	}
	m_iAddress -= 1;


	char cComFormat[32] = {0};
	m_iWorkMode = 1;
	if(m_iComNo <= 0)
	{
		m_iComNo = 1;
	}
	iRet = NetClient_GetComFormat(m_LogonID,m_iComNo,cComFormat,&m_iWorkMode);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetComFormat(%d,%d,%s,%d)"
			,m_LogonID,m_iComNo,cComFormat,m_iWorkMode);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetComFormat(%d,%d,%s,%d)"
			,m_LogonID,m_iComNo,cComFormat,m_iWorkMode);
	}
	if (1 == m_iWorkMode)
	{
		GetDlgItem(IDC_CHECK_EPTZ)->ShowWindow(SW_SHOW);
	}

	return TRUE;
}



void CTestDemoDlg::OnTimer(UINT_PTR nIDEvent)
{
	// TODO: Add your message handler code here and/or call default

	if (1 == nIDEvent)
	{
		MSG msg = {0};
		msg.message = WM_LBUTTONDOWN;
		msg.hwnd = m_hReverse;
		PreTranslateMessage(&msg);
	}
	CDialog::OnTimer(nIDEvent);
}

void CTestDemoDlg::OnNMCustomdrawSlider1(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO: Add your control notification handler code here
	int iSpeed = m_sldSpeed.GetPos();
	if (iSpeed <= 1)
	{
		iSpeed = 0;
	}
	SetDlgItemInt(IDC_STATIC_SPEEDNO,iSpeed);
	*pResult = 0;
}


void CTestDemoDlg::OnBnClickedCheckAuto()
{
	// TODO: Add your control notification handler code here
	if (m_LogonID < 0)
	{
		CheckDlgButton(IDC_CHECK_AUTO,FALSE);
		return;
	}

	BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
	if(IsDlgButtonChecked(IDC_CHECK_AUTO))
	{
		if (1 == m_iWorkMode)
		{
			ProtocolControl(PTZ_START_ROUTINE,0,0,bEPTZ);
		}
		else
		{
			TransparentControl(HOR_AUTO,m_iAddress,0,0);
		}
	}
	else
	{
		if (1 == m_iWorkMode)
		{
			ProtocolControl(PTZ_STOP_ROUTINE,0,0,bEPTZ);
		}
		else
		{
			TransparentControl(HOR_AUTO_STOP,m_iAddress,0,0);
		}
	}
}

void CTestDemoDlg::OnBnClickedButtonSetptz()
{
	// TODO: Add your control notification handler code here
	if (m_LogonID < 0)
	{
		return;
	}

	int iSpeed = m_sldSpeed.GetPos();
	int iPreset = m_cboPreNum.GetCurSel() + 1;
	if (iPreset < MIN_PTZ_NUM || iPreset > MAX_PTZ_NUM)
	{
		AddLog(LOG_TYPE_MSG,"","please select a correct preset(%d)",iPreset);
		return;
	}

	BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
	if (1 == m_iWorkMode)
	{
		ProtocolControl(PROTOCOL_SET_PRESET,iPreset,iSpeed,bEPTZ);
	}
	else
	{
		TransparentControl(SET_VIEW,m_iAddress,0,iPreset);
	}

}

void CTestDemoDlg::OnBnClickedButtonCallptz()
{
	// TODO: Add your control notification handler code here
	if (m_LogonID < 0)
	{
		return;
	}

	int iSpeed = m_sldSpeed.GetPos();
	int iPreset = m_cboPreNum.GetCurSel() + 1;
	if (iPreset < MIN_PTZ_NUM || iPreset > MAX_PTZ_NUM)
	{
		AddLog(LOG_TYPE_MSG,"","please select a correct preset(%d)",iPreset);
		return;
	}

	BOOL bEPTZ = IsDlgButtonChecked(IDC_CHECK_EPTZ);
	if (1 == m_iWorkMode)
	{
		ProtocolControl(PROTOCOL_PRESET,iPreset,iSpeed,bEPTZ);
	}
	else
	{
		TransparentControl(CALL_VIEW,m_iAddress,0,iPreset);
	}
}

void CTestDemoDlg::OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar)
{
	// TODO: Add your message handler code here and/or call default
	if (SB_ENDSCROLL == nSBCode)
	{
		int iCtrlID = pScrollBar->GetDlgCtrlID();
		switch(iCtrlID)
		{
		case IDC_SLIDER_BRIGHTNESS:
		case IDC_SLIDER_CONTRAST:
		case IDC_SLIDER_HUE:
		case IDC_SLIDER_SATURATION:
			{
				SetVideoParam();
			}
			break;
		default:
			break;
		}
	}
	CDialog::OnHScroll(nSBCode, nPos, pScrollBar);
}

void CTestDemoDlg::OnNMCustomdrawSliderHue(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO: Add your control notification handler code here
	int iPos = m_slider_hue.GetPos();
	SetDlgItemInt(IDC_STATIC_VALUECOLOR, iPos);
	*pResult = 0;
}

void CTestDemoDlg::OnNMCustomdrawSliderBrightness(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO: Add your control notification handler code here
	int iPos = m_slider_bright.GetPos();
	SetDlgItemInt(IDC_STATIC_BRIGHT_VALUE, iPos);
	*pResult = 0;
}

void CTestDemoDlg::OnNMCustomdrawSliderContrast(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO: Add your control notification handler code here
	int iPos = m_slider_contrast.GetPos();
	SetDlgItemInt(IDC_STATIC_COMPARE_VALUE, iPos);
	*pResult = 0;
}

void CTestDemoDlg::OnNMCustomdrawSliderSaturation(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO: Add your control notification handler code here
	int iPos = m_slider_saturation.GetPos();
	SetDlgItemInt(IDC_STATIC_SATURATIONVALUE, iPos);
	*pResult = 0;
}

void CTestDemoDlg::SetVideoParam()
{
	if (m_bIsDisplay)
	{
		STR_VideoParam tVideoParam = {0};
		tVideoParam.m_u16Saturation = m_slider_saturation.GetPos();
		tVideoParam.m_u16Brightness = m_slider_bright.GetPos();
		tVideoParam.m_u16Contrast = m_slider_contrast.GetPos();
		tVideoParam.m_u16Hue = m_slider_hue.GetPos();

		int iRet = NetClient_SetVideoPara(m_LogonID, m_ChannelNo.GetCurSel(),&tVideoParam);	
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetVideoPara(%d,%d,)",m_LogonID,m_ChannelNo.GetCurSel());
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetVideoPara(%d,%d,)",m_LogonID,m_ChannelNo.GetCurSel());
		}
	}
}

void CTestDemoDlg::UI_UpdateVideoParam()
{
	if (m_LogonID < 0 && m_ChannelNo.GetCurSel() < 0)
	{
		return ;
	}
	STR_VideoParam tVideoParam = {0};
	int iRet = NetClient_GetVideoPara(m_LogonID,m_ChannelNo.GetCurSel(),&tVideoParam);
	if (0 == iRet)
	{
		m_slider_bright.SetPos(tVideoParam.m_u16Brightness);
		m_slider_contrast.SetPos(tVideoParam.m_u16Contrast);
		m_slider_hue.SetPos(tVideoParam.m_u16Hue);
		m_slider_saturation.SetPos(tVideoParam.m_u16Saturation);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetVideoPara(%d,%d,)",m_LogonID,m_ChannelNo.GetCurSel());
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetVideoPara(%d,%d,)",m_LogonID,m_ChannelNo.GetCurSel());
	}
}

void CTestDemoDlg::OnBnClickedButtonDefault()
{
	// TODO: Add your control notification handler code here
	m_slider_bright.SetPos(128);
	m_slider_contrast.SetPos(128);
	m_slider_hue.SetPos(128);
	m_slider_saturation.SetPos(128);
	SetVideoParam();
}

void CTestDemoDlg::OnBnClickedCheckRecord()
{
	// TODO: Add your control notification handler code here
	if (m_iConnectID >= 0 && m_bIsDisplay)
	{
		int iRet = 0;
		BOOL bChecked = IsDlgButtonChecked(IDC_CHECK_RECORD);
		if(bChecked)
		{
			RECT rcShow = {0};
			GetDlgItem(IDC_CHECK_RECORD)->GetWindowRect(&rcShow);
			CMenu menu;
			menu.CreatePopupMenu();
			menu.AppendMenu(MF_STRING,ID_RECORD_SDV,"SDV");
			menu.AppendMenu(MF_STRING,ID_RECORD_PS,"PS");
			//menu.CheckMenuRadioItem(ID_RECORD_SDV,ID_RECORD_PS,ID_RECORD_SDV,MF_CHECKED);
			menu.TrackPopupMenu(TPM_LEFTALIGN | TPM_RIGHTBUTTON,rcShow.left-3,rcShow.bottom,this);
			menu.DestroyMenu();
		}
		else
		{
			iRet = NetClient_StopCaptureFile(m_iConnectID);
			if (0 == iRet)
			{
				AddLog(LOG_TYPE_SUCC,"","NetClient_StopCaptureFile(%u)",m_iConnectID);
			}
			else
			{
				AddLog(LOG_TYPE_FAIL,"","NetClient_StopCaptureFile(%u)",m_iConnectID);
			}
		}
	}

	CheckDlgButton(IDC_CHECK_RECORD,FALSE);
}

void CTestDemoDlg::OnRecord(UINT nID)
{
	BOOL bIsRecording = FALSE;
	if (m_iConnectID >= 0 && m_bIsDisplay)
	{
		CString strFileName = MakeFileName();
		int iType = REC_FILE_TYPE_NORMAL;
		if (nID == ID_RECORD_PS)
		{
			iType = REC_FILE_TYPE_MP4;
			strFileName.AppendFormat(_T(".ps"));//convert mp4 to stream of ps
		}
		else
		{
			strFileName.AppendFormat(_T(".sdv"));
		}
		int iRet = NetClient_StartCaptureFile(m_iConnectID,(LPSTR)(LPCTSTR)strFileName,iType);
		if (0 == iRet)
		{
			bIsRecording = TRUE;
			AddLog(LOG_TYPE_SUCC,"","NetClient_StartCaptureFile(%u,%s,%d)",m_iConnectID,(LPSTR)(LPCTSTR)strFileName,iType);
		}
		else
		{ 
			bIsRecording = FALSE;
			AddLog(LOG_TYPE_FAIL,"","NetClient_StartCaptureFile(%u,%s,%d)",m_iConnectID,(LPSTR)(LPCTSTR)strFileName,iType);
		}
	}

	CheckDlgButton(IDC_CHECK_RECORD, bIsRecording);
}

void CTestDemoDlg::OnBnClickedButtonConnectvideo()
{
	// TODO: Add your control notification handler code here
	StartRecv();
}

void CTestDemoDlg::OnBnClickedButtonDisconnect()
{
	// TODO: Add your control notification handler code here
	if (m_iConnectID >=0 )
	{
		int iRet = NetClient_StopPlay(m_iConnectID);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_StopPlay(%d)",m_iConnectID);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StopPlay(%d)",m_iConnectID);
		}
		iRet = NetClient_StopRecv(m_iConnectID);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_StopRecv(%d)",m_iConnectID);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StopRecv(%d)",m_iConnectID);
		}
		m_iConnectID = -1;
	}
}

void CTestDemoDlg::OnBnClickedButtonSavelog()
{
	// TODO: Add your control notification handler code here
	CString strFileName;
	CListCtrl* plvLog = NULL;
	plvLog = &m_listInfo;
	CString strSavePath;
	char cFilePath[MAX_PATH] = {0};
	int iSize = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
	if (iSize <= 0)
	{
		strcpy_s(cFilePath,sizeof(cFilePath),"C:\\");
	}
	strSavePath.Format(_T("%s"),cFilePath);
	int iPos = strSavePath.ReverseFind('\\');
	if (iPos >= 0)
	{
		strSavePath = strSavePath.Left(iPos);
	}
	strSavePath.AppendFormat(_T("\\local.log"));

	CHeaderCtrl* pHeader = plvLog->GetHeaderCtrl();
	if (NULL == pHeader)
	{
		return;
	}

	FILE* pFile = NULL;
	fopen_s(&pFile,(LPSTR)(LPCTSTR)strSavePath,"wb+");
	int iRetValue = GetLastError();
	if (NULL == pFile)
	{
		AddLog(LOG_TYPE_FAIL,"","fopen_s(%d,%s,wb+)",pFile,(LPSTR)(LPCTSTR)strSavePath);
		return;
	}

	int iItemCount = plvLog->GetItemCount();
	int iColunmCount = pHeader->GetItemCount();
	for (int i = 0; i < iItemCount; ++i)
	{
		CString strData;
		for (int j = 0; j < iColunmCount; ++j)
		{
			strData.AppendFormat(_T("%s\t"),plvLog->GetItemText(i,j));
		}
		strData.AppendFormat(_T("\n"));
		fwrite((LPSTR)(LPCTSTR)strData,sizeof(char),strData.GetLength(),pFile);
	}
	fclose(pFile);
	pFile = NULL;

	AddLog(LOG_TYPE_MSG,"","save file(%s) finished",(LPSTR)(LPCTSTR)strSavePath);
}

void CTestDemoDlg::OnBnClickedButtonClearlog()
{
	// TODO: Add your control notification handler code here
	m_listInfo.DeleteAllItems();
}

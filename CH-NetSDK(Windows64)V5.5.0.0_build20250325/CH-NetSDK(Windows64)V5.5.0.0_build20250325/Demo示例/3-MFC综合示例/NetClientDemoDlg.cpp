// NetClientDemoDlg.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "NetClientDemoDlg.h"
#include "Preview\PreviewWindow.h"
#include "Playback\PlaybackWindow.h"
#include "Config\ConfigWindow.h"
#include "Manage\ManageWindow.h"
#include "LogonView.h"
#include "LogView.h"
#include "LogonActive.h"
#include "Include/NetVideoInterface.h"
#include "socket_api.h"
#include "Include/PROXY_INTERFACE.h"
using namespace PROXY_INTERFACE;

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define MSG_MAIN				(WM_USER + 1001)
#define MSG_PARAMCHANGE			(WM_USER + 1002)
#define MSG_ALARM				(WM_USER + 1003)
#define MSG_MAIN_NOTIFY			(WM_USER + 1004)
#define MSG_PARAMCHANGE_NOTIFY	(WM_USER + 1005)
#define MSG_ALARM_NOTIFY		(WM_USER + 1006)
#define MSG_COMRECV_NOTIFY		(WM_USER + 1007)
#define MSG_PROXY_NOTIFY		(WM_USER + 1008)
#define MSG_ALARM_NOTIFY_V5		(WM_USER + 1009)

#define  FLAG_THIRD_STREAM	100

#define  CHANNEL_TYPE_RECVPIC  99	//Channel type, receive the image stream channel (only used in the upper layer of Demo, to distinguish whether the tree node is an image stream channel)
#define  IPV6_VERSION		   1



int g_iSdkUseMode = ASYN_MODE;		//0----asynchronous mode; 1----synchronous mode

BOOL CNetClientDemoDlg::m_blProcStats = FALSE;

struct _MAIN_NOTIFY_DATA 
{
	int m_iLogonID;
	int m_wParam;
	void* m_lParam;
	void* m_iUserData;
};

struct _ALARM_NOTIFY_DATA 
{
	int m_iLogonID;
	int m_iChannelNo;
	int m_iAlarmState;
	ALARMTYPE m_iAlarmType;
	int m_iUserData;
};

struct _ALARM_NOTIFY_DATA_V5 
{
	int m_iLogonID;
	int m_iAlarmType;
	void* m_pData;
	int m_iBufSize;
	void* m_iUserData;
};

typedef union tagParamChangeNotifyParam
{
	int					iEnable;	
	ReportQueryResult	tReportQueryResult;
	IrrigationPara      tIrrigationPara;
	GeografhyLocation   tGeografhyLocation;
	TargetQueryResult	tTargetQueryResult;
}ParamChangeNotifyParam, *pParamChangeNotifyParam;

struct _PARAMCHANGE_NOTIFY_DATA 
{
	int m_iLogonID;
	int m_iChannelNo;
	PARATYPE m_iParaType;
	STR_Para m_pPara;
	int m_iUserData;
	ParamChangeNotifyParam m_utParam;
};
BOOL m_bConfigWindowShow = FALSE;
// CNetClientDemoDlg dialog
#pragma comment(lib, "Version.lib")

CNetClientDemoDlg::CNetClientDemoDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CNetClientDemoDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
	for(int i = 0; i < WINDOW_MAX; ++i)
	{
		m_pWindow[i] = NULL;
	}
	m_pCurrent = NULL;
	m_bLogCtrlStretched = FALSE;
	m_bFlagThreeStream = FALSE;
	m_iSDKWorkMode = HEAVY_MODE;
	m_hTreeMainItem = NULL;
}

void CNetClientDemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_LOG_LOCAL, m_lvLocalLog);
	DDX_Control(pDX, IDC_LIST_LOG_ALARM, m_lvAlarmLog);
	DDX_Control(pDX, IDC_TREE_DEVICE_LIST, m_tvDevice);
	DDX_Control(pDX, IDC_TAB_MAIN, m_tabMain);
	DDX_Control(pDX, IDC_COMBO_LANGUAGE, m_cboLanguage);
}

BEGIN_MESSAGE_MAP(CNetClientDemoDlg, CDialog)
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_WM_DESTROY()
	ON_WM_TIMER()
	ON_NOTIFY(NM_RCLICK, IDC_TREE_DEVICE_LIST, &CNetClientDemoDlg::OnNMRClickTreeDeviceList)
	ON_NOTIFY(NM_DBLCLK, IDC_TREE_DEVICE_LIST, &CNetClientDemoDlg::OnNMDblclkTreeDeviceList)
	ON_COMMAND(ID_DELETE_DEVICE, &CNetClientDemoDlg::OnDeleteDevice)
	ON_COMMAND(ID_ADD_DEVICE, &CNetClientDemoDlg::OnAddDevice)
	ON_COMMAND(ID_ADD_DEVICE_ACTIVE, &CNetClientDemoDlg::OnAddActiveDevice)
	ON_COMMAND(ID_LOGON, &CNetClientDemoDlg::OnLogon)
	ON_COMMAND(ID_LOGOFF, &CNetClientDemoDlg::OnLogoff)
	ON_COMMAND_RANGE(ID_CONNECT_TCP,ID_CONNECT_MUC, &CNetClientDemoDlg::OnConnect)
	ON_COMMAND_RANGE(ID_CONNECT_RTSP_OVER_TCP,ID_CONNECT_SRTP_OVER_MUC, &CNetClientDemoDlg::OnConnect)
	ON_COMMAND(ID_DISCONNECT, &CNetClientDemoDlg::OnDisconnect)
	ON_COMMAND(ID_CONNECT_PIC, &CNetClientDemoDlg::OnStartRecvPic)
	ON_COMMAND(ID_DISCONNECT_PIC, &CNetClientDemoDlg::OnStopRecvPic)
	ON_NOTIFY(TCN_SELCHANGE, IDC_TAB_MAIN, &CNetClientDemoDlg::OnTcnSelchangeTabMain)
	ON_MESSAGE(MSG_MAIN,OnMainMessage)//Register SDK system messages
	ON_MESSAGE(MSG_PARAMCHANGE,OnParamChangeMessage)//Register SDK parameter change message
	ON_MESSAGE(MSG_ALARM,OnAlarmMessage)//Register for SDK alert messages
	ON_MESSAGE(MSG_MAIN_NOTIFY,OnMainNotify)
	ON_MESSAGE(MSG_ALARM_NOTIFY,OnAlarmNotify)
	ON_MESSAGE(MSG_ALARM_NOTIFY_V5,OnAlarmNotify_V5)
	ON_MESSAGE(MSG_PARAMCHANGE_NOTIFY,OnParamChangeNotify)
	ON_NOTIFY(TVN_SELCHANGED, IDC_TREE_DEVICE_LIST, &CNetClientDemoDlg::OnTvnSelchangedTreeDeviceList)
	ON_BN_CLICKED(IDC_RADIO_LOCAL_LOG, &CNetClientDemoDlg::OnBnClickedRadioLocalLog)
	ON_BN_CLICKED(IDC_RADIO_ALARM_INFO, &CNetClientDemoDlg::OnBnClickedRadioAlarmInfo)
	ON_BN_CLICKED(IDC_BTN_LOG_CLEAR, &CNetClientDemoDlg::OnBnClickedBtnLogClear)
	ON_CBN_SELCHANGE(IDC_COMBO_LANGUAGE, &CNetClientDemoDlg::OnCbnSelchangeComboLanguage)
	ON_BN_CLICKED(IDC_BTN_LOG_CONFIG, &CNetClientDemoDlg::OnBnClickedBtnLogConfig)
	ON_NOTIFY(HDN_ITEMCLICK, 0, &CNetClientDemoDlg::OnHdnItemclickListLogLocal)
	ON_MESSAGE(DM_GETDEFID,&CNetClientDemoDlg::OnGetDefID)
	ON_BN_CLICKED(IDC_BTN_LOG_SAVE, &CNetClientDemoDlg::OnBnClickedBtnLogSave)
	ON_BN_CLICKED(IDC_BTN_LOG_TEST, &CNetClientDemoDlg::OnBnClickedBtnLogTest)
	ON_BN_CLICKED(IDC_BUTTON_GET_LASTERROR, &CNetClientDemoDlg::OnBnClickedButtonGetLasterror)
END_MESSAGE_MAP()

// CNetClientDemoDlg message handlers

BOOL CNetClientDemoDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon

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
	strSavePath.AppendFormat(_T("\\NetClientDemo"));
	SetLocalSaveDirectory(strSavePath);

	SetCurrentLanguage(0);

	GetDemoUseMode();

	CString strLogFileName;
	strLogFileName.Format(_T("%s\\nvssdk.log"),GetLocalSaveDirectory());
	SetLogFileName((LPSTR)(LPCTSTR)strLogFileName);
	//SetLogCaps(LOG_CAP_ALL,LOG_CAP_FAIL|LOG_CAP_MSG|LOG_CAP_ADD_LIST);

	int iRet = SDKInit();
	if (-1 == iRet)
	{
		MessageBox("Warning: Init SDK failed! Porgram will exit!");
		exit(0);
	}

	iRet = LoadMp4Dll();
	if(iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_MSG, "", GetTextEx(IDS_LOADLIBMP4_FAIL));
	}

	SetComDataShow();

	iRet = LoadNetVideoConfig();
	if (iRet != RET_SUCCESS)
	{
		AddLog(LOG_TYPE_MSG, "","Failed to load configuration page, please try again later");
	}

	m_lstImage.Create(16,16,ILC_COLOR32,4,4);
	m_lstImage.SetBkColor(RGB(255,255,255));
	HICON hICON=AfxGetApp()->LoadIcon(IDI_NOT_LOGON);
	m_lstImage.Add(hICON);
	hICON=AfxGetApp()->LoadIcon(IDI_LOGON_SUCCESS);
	m_lstImage.Add(hICON);
	hICON=AfxGetApp()->LoadIcon(IDI_ERROR_ORDER);
	m_lstImage.Add(hICON);
	hICON=AfxGetApp()->LoadIcon(IDI_NOT_CONNECT);
	m_lstImage.Add(hICON);
	hICON=AfxGetApp()->LoadIcon(IDI_CONNECT_SUCCESS);
	m_lstImage.Add(hICON);
	hICON=AfxGetApp()->LoadIcon(IDI_ERROR_DATANET);
	m_lstImage.Add(hICON);
	hICON=AfxGetApp()->LoadIcon(IDI_DEV_TREE);
	m_lstImage.Add(hICON);
	m_tvDevice.SetImageList(&m_lstImage,TVSIL_NORMAL);	
	
	m_hTreeMainItem = m_tvDevice.InsertItem(GetTextByLan("设备树-右击添加设备","Device tree-right click add device"),ICON_DEV_TREE,ICON_DEV_TREE);
	
	CSize sizeItem;
	sizeItem.cx = 100;
	sizeItem.cy = 35;
	m_tabMain.SetItemSize(sizeItem);

	((CButton*)GetDlgItem(IDC_RADIO_LOCAL_LOG))->SetCheck(TRUE);
	m_lvLocalLog.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lvAlarmLog.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	SetLogCtrl(&m_lvLocalLog);

	LANGID Systemlid = GetSystemDefaultLangID(); 
	if (0x0804 == Systemlid || 0x1004 == Systemlid)
	{
		UI_UpdateDialog(0);
	}
	else
	{
		UI_UpdateDialog(1);
	}

	OnCbnSelchangeComboLanguage();

	ShowBaseWindow(WINDOW_PREVIEW);

	UI_UpdateDateTime();
	SetTimer(TIMER_DATETIME,1000,NULL);

	UI_UpdateDemoVersion();

	LoadDevice();

	//Program startup settings local settings
	LoadSdkVideoMode();

	load_socket_lib();

	return TRUE;  // return TRUE  unless you set the focus to a control
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CNetClientDemoDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CNetClientDemoDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

//#define _DEBUG_LOG_

void __stdcall GetNotifyTest(unsigned int _ulID, unsigned int _ulStreamType, unsigned char *_cData, int _iLen, void* _iUser, void* _iUserData)
{
#ifdef _DEBUG_LOG_
	TRACE("_ulStreamType is %d, _iUser is %d, _iUserData is %d\n", _ulStreamType, _iUser, _iUserData);
#endif
}

void __stdcall GetRawNotify(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser)
{
#ifdef _DEBUG_LOG_
	TRACE("_cData is %p, _iLen is %d\n", _cData, _iLen);
#endif
}
void __stdcall GetRawNotifyEx(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFOEX *_pRawFrameInfo, void* _iUser)
{
#ifdef _DEBUG_LOG_
	RAWFRAME_INFOEX tmp = {0};
	memcpy(&tmp, _pRawFrameInfo, sizeof(RAWFRAME_INFOEX));
	TRACE("_cData is %p, Ex _iLen is %d, frameNo=%d\n", _cData, _iLen, tmp.ui32FrameNO);
	//AddLog(LOG_TYPE_MSG,"","EX  _cData is %p, _iLen is %d, frameNo=%d\n", _cData, _iLen, tmp.ui32FrameNO);

#endif
}

void __stdcall GetDevUserDataNotify(unsigned int _uiID, int _iType, void* _pvData
									 , int _iDataLen, CurrentFrameInfo* _ptInfo, void* _pvUdata)
{
#ifdef _DEBUG_LOG_
	int iFrameNo = -1;
	if (_ptInfo != NULL)
	{
		iFrameNo = _ptInfo->uiFrameNO;
	}
	TRACE("_iType(%d),_pvData(%p),_iDataLen(%d),iFrameNo(%d),_pvUdata(%p)\n",_iType, _pvData, _iDataLen, iFrameNo, _pvUdata);
#endif
}

int CNetClientDemoDlg::ShowBaseWindow( int _iIndex )
{
	if (_iIndex < WINDOW_MIN || _iIndex >= WINDOW_MAX)
	{
		return -1;
	}
	if(NULL != m_pWindow[WINDOW_CONFIG])
	{
		m_pWindow[WINDOW_CONFIG]->ChangeCurrentPage();
	}
	if (NULL == m_pWindow[_iIndex])
	{
		switch(_iIndex)
		{
		case WINDOW_PREVIEW:
			{
				CLS_PreviewWindow* pPreview = new CLS_PreviewWindow(this);
				if (pPreview)
				{
					pPreview->Create(IDD_DLG_PREVIEW,this);
					m_pWindow[_iIndex] = pPreview;
					pPreview->SetVideoChangedNotify(OnVideoChanged,this);
				}
			}
			break;
		case WINDOW_PLAYBACK:
			{
				m_pWindow[_iIndex] = new CLS_PlaybackWindow(this);
				m_pWindow[_iIndex]->Create(IDD_DLG_PLAYBACK,this);
			}
			break;
		case WINDOW_CONFIG:
			{
				m_pWindow[_iIndex] = new CLS_ConfigWindow(this);
				m_pWindow[_iIndex]->Create(IDD_DLG_CONFIG,this);
			}
			break;
		case WINDOW_MANAGE:
			{
				m_pWindow[_iIndex] = new CLS_ManageWindow(this);
				m_pWindow[_iIndex]->Create(IDD_DLG_MANAGE,this);
			}
			break;
		}
		
		if (m_pWindow[_iIndex])
		{
			RECT rcShow = {0};
			GetDlgItem(IDC_TAB_MAIN)->GetWindowRect(&rcShow);
			ScreenToClient(&rcShow);
			rcShow.top += 40;
			m_pWindow[_iIndex]->MoveWindow(&rcShow);
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","ShowBaseWindow(%d) window is null",_iIndex);
			return -1;
		}
	}

	if(m_pCurrent)
	{
		m_pCurrent->ShowWindow(SW_HIDE);
	}
	m_pCurrent = m_pWindow[_iIndex];

	StretchLogCtrl(FALSE);
	//Notify before display
	ChangeChannel();
	m_pCurrent->ShowWindow(SW_SHOW);
	
	return 0;
}

void CNetClientDemoDlg::UI_UpdateDateTime()
{
	CString strTime;
	CTime tNow = CTime::GetCurrentTime();   
	strTime.Format("%04d-%02d-%02d  %02d:%02d:%02d"
		,tNow.GetYear(),tNow.GetMonth(),tNow.GetDay()
		,tNow.GetHour(),tNow.GetMinute(),tNow.GetSecond());
	SetDlgItemText(IDC_STATIC_TIME,strTime);
}

void CNetClientDemoDlg::OnDestroy()
{
	CDialog::OnDestroy();

	if (NULL != NetClient_Cleanup) {
		NetClient_Cleanup();
	}
	if (NULL != NetClient_ProxyCleanup) {
		NetClient_ProxyCleanup();
	}

	CLS_EZoomManager::Destroy();

	for (int i = 0; i < WINDOW_MAX; ++i)
	{
		if (m_pWindow[i])
		{
			m_pWindow[i]->DestroyWindow();
			delete m_pWindow[i];
			m_pWindow[i] = NULL;
		}
	}
	m_pCurrent = NULL;

	KillTimer(TIMER_DATETIME);

	SaveDevice();
	DestroyManager();

	free_socket_lib();
}

void CNetClientDemoDlg::OnTimer(UINT_PTR nIDEvent)
{
	switch (nIDEvent)
	{
	case TIMER_DATETIME:
		{
			UI_UpdateDateTime();
		}
		break;
	}
	CDialog::OnTimer(nIDEvent);
}

void CNetClientDemoDlg::OnNMRClickTreeDeviceList(NMHDR *pNMHDR, LRESULT *pResult)
{
	POINT pointCursor = {0};
	HTREEITEM hItem = NULL;	
	HTREEITEM hParentItem = NULL;
	int iImage = -1;
	int iParentImage = -1;
	CMenu menu;
	CMenu submenu;

	GetCursorPos(&pointCursor);
	m_tvDevice.ScreenToClient(&pointCursor);
	menu.CreatePopupMenu();
	
	hItem = m_tvDevice.HitTest(pointCursor);	
	m_tvDevice.SelectItem(hItem);
	m_tvDevice.GetItemImage(hItem,iImage,iImage);
	
	if (hItem && ICON_DEV_TREE!= iImage)
	{
		hParentItem = m_tvDevice.GetParentItem(hItem);
		
		m_tvDevice.GetItemImage(hParentItem,iParentImage,iParentImage);
		if (hParentItem && ICON_DEV_TREE!= iParentImage)
		{
			switch(iImage)
			{
			case ICON_NOT_CONNECT:
				{
					int iLogonID = (int)m_tvDevice.GetItemData(hParentItem);
					unsigned int uChannel = (unsigned int)m_tvDevice.GetItemData(hItem);
					int iChannelNo = -1;
					int iStreamNo = -1;
					AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
					unsigned int uConnID = -1;
					PCHANNEL_INFO pChannel = FindChannel(iLogonID,iChannelNo,iStreamNo,&uConnID);
					if (NULL == pChannel || NULL == pChannel->pVideo)
					{
						m_tvDevice.GetItemImage(hParentItem,iImage,iImage);
						if (ICON_LOGON_SUCCESS == iImage && CHANNEL_TYPE_RECVPIC != iStreamNo)
						{
							submenu.CreatePopupMenu();
							submenu.AppendMenu(MF_STRING,ID_CONNECT_TCP,_T("TCP"));
							submenu.AppendMenu(MF_STRING,ID_CONNECT_UDP,_T("UDP"));
							submenu.AppendMenu(MF_STRING,ID_CONNECT_MUC,_T("MUC"));
							submenu.AppendMenu(MF_STRING,ID_CONNECT_RTSP_OVER_TCP,_T("RTSP over TCP"));
							submenu.AppendMenu(MF_STRING,ID_CONNECT_RTSP_OVER_UDP,_T("RTSP over UDP"));
							submenu.AppendMenu(MF_STRING,ID_CONNECT_RTSP_OVER_MUC,_T("RTSP over MUC"));
							submenu.AppendMenu(MF_STRING,ID_CONNECT_SRTP_OVER_UDP,_T("SRTP over UDP"));
							submenu.AppendMenu(MF_STRING,ID_CONNECT_SRTP_OVER_MUC,_T("SRTP over MUC"));
							menu.AppendMenu(MF_POPUP,(UINT)submenu.GetSafeHmenu(),GetTextEx(IDS_MENU_CONNECT));
						}
						else
						{
							menu.AppendMenu(MF_STRING,ID_CONNECT_PIC,GetTextEx(IDS_RECVPIC_START));
						}
						break;
					}
				}
			case ICON_CONNECT_SUCCESS:
			case ICON_ERROR_DATANET:
				{
					int iLogonID = (int)m_tvDevice.GetItemData(hParentItem);
					unsigned int uChannel = (unsigned int)m_tvDevice.GetItemData(hItem);
					int iChannelNo = -1;
					int iStreamNo = -1;
					AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);

					if (CHANNEL_TYPE_RECVPIC == iStreamNo)
					{
						menu.AppendMenu(MF_STRING,ID_DISCONNECT_PIC,GetTextEx(IDS_RECVPIC_STOP));
					}
					else
					{
						menu.AppendMenu(MF_STRING,ID_DISCONNECT,GetTextEx(IDS_MENU_DISCONNECT));
					}
				}
				break;
			}
		}
		else
		{
			switch(iImage)
			{
			case ICON_NOT_LOGON:
				{
					menu.AppendMenu(MF_STRING,ID_LOGON,GetTextEx(IDS_MENU_LOGON));
				}
				break;
			case ICON_LOGON_SUCCESS:
			case ICON_ERROR_ORDER:
				{
					menu.AppendMenu(MF_STRING,ID_LOGOFF,GetTextEx(IDS_MENU_LOGOFF));
				}
				break;
			}
			menu.AppendMenu(MF_STRING,ID_DELETE_DEVICE,GetTextEx(IDS_MENU_DELETE_DEVICE));
		}
	}
	else
	{
		menu.AppendMenu(MF_STRING,ID_ADD_DEVICE,GetTextEx(IDS_MENU_ADD_DEVICE));
	}
	
	m_tvDevice.ClientToScreen(&pointCursor);
	menu.TrackPopupMenu(TPM_LEFTALIGN | TPM_RIGHTBUTTON,pointCursor.x,pointCursor.y,this);
	menu.DestroyMenu();
	submenu.DestroyMenu();

	*pResult = 0;
}

void CNetClientDemoDlg::OnNMDblclkTreeDeviceList(NMHDR *pNMHDR, LRESULT *pResult)
{
	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (hItem)
	{
		int iImage = 0;
		m_tvDevice.GetItemImage(hItem,iImage,iImage);
		if (ICON_NOT_LOGON == iImage)
		{
			OnLogon();	
		}
		else if (ICON_NOT_CONNECT == iImage)
		{
			unsigned int uChannel = (unsigned int)m_tvDevice.GetItemData(hItem);
			int iChannelNo = -1;
			int iStreamNo = -1;
			AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
			if (CHANNEL_TYPE_RECVPIC == iStreamNo)
			{
				OnStartRecvPic();
			}
			else
			{
				OnConnect(ID_CONNECT_TCP);
			}			
		}
	}

	*pResult = 0;
}

void CNetClientDemoDlg::OnAddDevice()
{
	CLS_LogonView logon;
	if(IDOK != logon.DoModal())
	{
		return;
	}

	int iOldLogonID = -1;
	PDEVICE_INFO pDevLogon = logon.GetDeviceInfo();
	PDEVICE_INFO pDevFind = FindDevice(pDevLogon->cIP,pDevLogon->iPort,pDevLogon->cProxy,pDevLogon->cID,&iOldLogonID);
	if (pDevFind)
	{
		if(LOGON_SUCCESS == NetClient_GetLogonStatus(iOldLogonID))
		{
			AddLog(LOG_TYPE_MSG,"","FindDevice(%s,%d,%s,%s) device already exist!"
				,pDevFind->cIP,pDevFind->iPort,pDevFind->cProxy,pDevFind->cID);
			return;
		}
	}

	int iLogonID = -1;
	LogonPara tLogonPara = {0};
	tLogonPara.iSize = sizeof(LogonPara);
	tLogonPara.iNvsPort = pDevLogon->iPort;
	strcpy(tLogonPara.cUserName, pDevLogon->cUserName);
	strcpy(tLogonPara.cUserPwd, pDevLogon->cPassword);
	strcpy(tLogonPara.cCharSet, "UTF-8");
	strcpy(tLogonPara.cPlatFeature,pDevLogon->cPlatFeature);
	tLogonPara.pfTransCbk = CNetClientDemoDlg::TransProtocolData;
	tLogonPara.pvTransUdata = this;
	int iLogonType = SERVER_NORMAL;
	if (IsValidIPv6(pDevLogon->cIP) >= 1)
	{
		iLogonType = SERVER_NORMAL_IPV6;
		strcpy(tLogonPara.cNvsIpV6, pDevLogon->cIP);
	}
	else
	{
		if(strlen(pDevLogon->cIP) > 0 && 1 != IsValidIP(pDevLogon->cIP))
		{
			AddLog(LOG_TYPE_MSG,"","invalid ip(%s)", pDevLogon->cIP);
			return;
		}
		if(strlen(pDevLogon->cProxy) > 0 && 1 != IsValidIP(pDevLogon->cIP))
		{
			AddLog(LOG_TYPE_MSG,"","invalid proxy(%s)",pDevLogon->cIP);
			return;
		}
		iLogonType = SERVER_NORMAL;
		strcpy(tLogonPara.cNvsIP, pDevLogon->cIP);
	}

	if (SYNC_MODE == g_iSdkUseMode)
	{
		//Synchronous blocking login device
		iLogonID = NetClient_SyncLogon(iLogonType, &tLogonPara, sizeof(LogonPara));
	}
	else
	{
		iLogonID = NetClient_Logon_V4(iLogonType, &tLogonPara, sizeof(LogonPara));
	}
	if (iLogonID >= 0)
	{
		AddLog(LOG_TYPE_SUCC,pDevLogon->cIP,"(%d)NetClient_LogonEx",iLogonID);
		if (pDevFind)
		{
			return;
		}
		PDEVICE_INFO pDevNew = AddDevice(iLogonID);
		if (pDevNew)
		{
			memcpy_s(pDevNew,sizeof(DEVICE_INFO),pDevLogon,sizeof(DEVICE_INFO));
			pDevNew->hItem = NULL;
			pDevNew->uiInterTalkID = -1;
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","AddDevice(%d) Can add device",iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,pDevLogon->cIP,"NetClient_LogonEx(%d,%s,%s,%s.)",iLogonID, pDevLogon->cProxy, pDevLogon->cIP, pDevLogon->cID);
	}
}

void CNetClientDemoDlg::OnAddActiveDevice()
{
	CLS_LogonActive logon;
	if(IDOK != logon.DoModal())
	{
		return;
	}

	int iOldLogonID = -1;
	PDEVICE_INFO pDevLogon = logon.GetDeviceInfo();
	PDEVICE_INFO pDevFind = FindDevice(pDevLogon->cIP,pDevLogon->iPort,pDevLogon->cProxy,pDevLogon->cID,&iOldLogonID);
	if (pDevFind)
	{
		if(LOGON_SUCCESS == NetClient_GetLogonStatus(iOldLogonID))
		{
			AddLog(LOG_TYPE_MSG,"","[OnAddActiveDevice]FindDevice(%s,%d,%s,%s) device already exist!"
				,pDevFind->cIP,pDevFind->iPort,pDevFind->cProxy,pDevFind->cID);
			return;
		}
	}

	LogonActiveServer tLogonPara = {0};
	tLogonPara.iSize = sizeof(LogonActiveServer);
	strcpy_s(tLogonPara.cUserName, sizeof(tLogonPara.cUserName), pDevLogon->cUserName);
	strcpy_s(tLogonPara.cUserPwd, sizeof(tLogonPara.cUserPwd), pDevLogon->cPassword);
	strcpy_s(tLogonPara.cProductID, sizeof(tLogonPara.cProductID), pDevLogon->cID);
	int iLogonID = NetClient_Logon_V4(SERVER_REG_ACTIVE, &tLogonPara, sizeof(LogonActiveServer));
	if (iLogonID >= 0)
	{
		AddLog(LOG_TYPE_SUCC,pDevLogon->cIP,"(%d)NetClient_Logon_V4",iLogonID);
		if (pDevFind)
		{
			return;
		}
		PDEVICE_INFO pDevNew = AddDevice(iLogonID);
		if (pDevNew)
		{
			memcpy_s(pDevNew,sizeof(DEVICE_INFO),pDevLogon,sizeof(DEVICE_INFO));
			pDevNew->hItem = NULL;
			pDevNew->uiInterTalkID = -1;
			pDevNew->iServerType = SERVER_REG_ACTIVE;
			pDevNew->clockStartRecvProtTime = 0;
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","[OnAddActiveDevice]AddDevice(%d) Can add device",iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,pDevLogon->cIP,"NetClient_Logon_V4(%s,%s)", pDevLogon->cIP, pDevLogon->cID);
	}
}

void CNetClientDemoDlg::OnDeleteDevice()
{
	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (hItem)
	{
		OnLogoff();
		m_tvDevice.DeleteItem(hItem);
	}
}

void CNetClientDemoDlg::OnLogon()
{
	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (NULL == hItem)
	{
		return;
	}

	DEVICE_INFO tDevInit = {0};
	int iLogonIDOld = (int)m_tvDevice.GetItemData(hItem);
	if (iLogonIDOld != -1)
	{
		PDEVICE_INFO Device= FindDevice(iLogonIDOld);
		if (Device)
		{
			if (Device->uiInterTalkID != -1)
			{
				NetClient_InterTalkEnd(Device->uiInterTalkID, TRUE);
				Device->bIsTalking = false;
				Device->uiInterTalkID = -1;
			}
			else if (Device->bIsTalking)
			{
				NetClient_TalkEnd(iLogonIDOld);
				Device->bIsTalking = false;
			}
		}
		OnLogoffDevice(iLogonIDOld);
		NetClient_Logoff(iLogonIDOld);
	}
	PDEVICE_INFO pDevRemove = RemoveDevice(iLogonIDOld);
	if (pDevRemove)
	{
		memcpy_s(&tDevInit,sizeof(DEVICE_INFO),pDevRemove,sizeof(DEVICE_INFO));
	}
	else
	{
		CString strIP = m_tvDevice.GetItemText(hItem);
		strcpy_s(tDevInit.cIP,sizeof(tDevInit.cIP),(LPCSTR)(LPCTSTR)strIP);
		tDevInit.iPort = 3000;
	}

	CLS_LogonView logon(&tDevInit);
	if(IDOK != logon.DoModal())
	{
		return;
	}

	PDEVICE_INFO pDevLogon = logon.GetDeviceInfo();
	int iLogonID = -1;
	LogonPara tLogonPara = {0};
	tLogonPara.iSize = sizeof(LogonPara);
	tLogonPara.iNvsPort = pDevLogon->iPort;
	strcpy(tLogonPara.cUserName, pDevLogon->cUserName);
	strcpy(tLogonPara.cUserPwd, pDevLogon->cPassword);
	strcpy(tLogonPara.cCharSet, "UTF-8");
	strcpy(tLogonPara.cPlatFeature,pDevLogon->cPlatFeature);
	tLogonPara.pfTransCbk = CNetClientDemoDlg::TransProtocolData;
	tLogonPara.pvTransUdata = this;
	int iLogonType = SERVER_NORMAL;
	
	if (IsValidIPv6(pDevLogon->cIP) >= 1)
	{
		iLogonType = SERVER_NORMAL_IPV6;
		strcpy(tLogonPara.cNvsIpV6, pDevLogon->cIP);
	}
	else
	{
		if(strlen(pDevLogon->cIP) > 0 && 1 != IsValidIP(pDevLogon->cIP))
		{
			AddLog(LOG_TYPE_MSG,"","invalid ip(%s)", pDevLogon->cIP);
			return;
		}
		if(strlen(pDevLogon->cProxy) > 0 && 1 != IsValidIP(pDevLogon->cIP))
		{
			AddLog(LOG_TYPE_MSG,"","invalid proxy(%s)",pDevLogon->cIP);
			return;
		}
		iLogonType = SERVER_NORMAL;
		strcpy(tLogonPara.cNvsIP, pDevLogon->cIP);
	}
	if (SYNC_MODE == g_iSdkUseMode)
	{
		//Synchronous blocking login device
		iLogonID = NetClient_SyncLogon(iLogonType, &tLogonPara, sizeof(LogonPara));
	}
	else
	{
		//iLogonID = NetClient_LogonEx(pDevLogon->cProxy,pDevLogon->cIP,pDevLogon->cUserName,pDevLogon->cPassword,pDevLogon->cID,pDevLogon->iPort,"UTF-8");
		//Asynchronous non-blocking login device
		iLogonID = NetClient_Logon_V4(iLogonType, &tLogonPara, sizeof(LogonPara));
	}
	if (iLogonID >= 0)
	{
		AddLog(LOG_TYPE_SUCC,pDevLogon->cIP,"(%d)NetClient_LogonEx",iLogonID);
		PDEVICE_INFO pDevNew = AddDevice(iLogonID);
		if (pDevNew)
		{
			m_tvDevice.SetItemImage(hItem,ICON_NOT_LOGON,ICON_NOT_LOGON);
			m_tvDevice.SetItemData(hItem,iLogonID);
			pDevLogon->hItem = hItem;
			memcpy_s(pDevNew,sizeof(DEVICE_INFO),pDevLogon,sizeof(DEVICE_INFO));

			UI_UpdateDeviceInfo(pDevNew);
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","AddDevice(%d) Can not add device",iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, pDevLogon->cIP, "NetClient_LogonEx(%d,%s,%s,%s,iLogonID)"
			, iLogonID, pDevLogon->cProxy, pDevLogon->cIP, pDevLogon->cID);
	}
}

void CNetClientDemoDlg::OnLogoff()
{
	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (hItem)
	{
		int iLogonID = (int)m_tvDevice.GetItemData(hItem);
		PDEVICE_INFO Device= FindDevice(iLogonID);
		if (NULL != Device)
		{
			if (Device->uiInterTalkID != -1)
			{
				NetClient_InterTalkEnd(Device->uiInterTalkID, TRUE);
				Device->bIsTalking = false;
				Device->uiInterTalkID = -1;
			}
			else if (Device->bIsTalking)
			{
				NetClient_TalkEnd(iLogonID);
				Device->bIsTalking = false;
			}
		}
		OnLogoffDevice(iLogonID);
		NetClient_Logoff(iLogonID);
		UI_DeleteDevice(hItem, iLogonID);
		AddLog(LOG_TYPE_SUCC,"","NetClient_Logoff(%d)",iLogonID);
	}
}

int CNetClientDemoDlg::GetNetModeByMenuID(UINT _uiID)
{
	int iNetMode = NETMODE_TCP;
	switch (_uiID)
	{
	case ID_CONNECT_TCP:
		{
			iNetMode = NETMODE_TCP;
			break;
		}
	case ID_CONNECT_UDP:
		{
			iNetMode = NETMODE_UDP;
			break;
		}
	case ID_CONNECT_MUC:
		{
			iNetMode = NETMODE_MC;
			break;
		}
	case ID_CONNECT_RTSP_OVER_TCP:
		{
			iNetMode = RTP_OVER_TCP;
			break;
		}
	case ID_CONNECT_RTSP_OVER_UDP:
		{
			iNetMode = RTP_OVER_UDP;
			break;
		}
	case ID_CONNECT_RTSP_OVER_MUC:
		{
			iNetMode = RTP_OVER_MCAST;
			break;
		}
	case ID_CONNECT_SRTP_OVER_UDP:
		{
			iNetMode = SRTP_OVER_UDP;
			break;
		}
	case ID_CONNECT_SRTP_OVER_MUC:
		{
			iNetMode = SRTP_OVER_MCAST;
			break;
		}
	default:
		{
			break;
		}
	}
	return iNetMode;
}

void CNetClientDemoDlg::OnConnect(UINT _uiID)
{
	if (WINDOW_PREVIEW != m_tabMain.GetCurSel() || TRUE == m_bLogCtrlStretched)
	{
		return;
	}
	int iNetMode = GetNetModeByMenuID(_uiID);
	//int iNetMode = _uiID - ID_CONNECT_TCP + 1;
	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (hItem)
	{
		HTREEITEM hParent = m_tvDevice.GetParentItem(hItem);
		if (hParent)
		{
			CLS_VideoView* pVideo = NULL;
			int iLogonID = (int)m_tvDevice.GetItemData(hParent);
			unsigned int uChannel = (unsigned int)m_tvDevice.GetItemData(hItem);
			int iChannelNo = -1;
			int iStreamNo = -1;
			AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
			//if (2 == uChannel)
			if (iStreamNo == CHANNEL_THREE_STREAM)
			{
				m_bFlagThreeStream = TRUE;
			}
			unsigned int uConnID = -1;
			PCHANNEL_INFO pChannel = RemoveChannel(iLogonID,iChannelNo,iStreamNo,&uConnID);
			if (pChannel)
			{
				pVideo = (CLS_VideoView*)pChannel->pVideo;
				if (pVideo)
				{
					pVideo->SetConnID(-1);
				}
				NetClient_StopRecv(uConnID);
				AddLog(LOG_TYPE_SUCC,"","NetClient_StopRecv(%u)",uConnID);
				
				m_tvDevice.SetItemImage(pChannel->hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
				
				uConnID = -1;
				pChannel = NULL;
			}

			if (NULL == pVideo)
			{
				pVideo = (CLS_VideoView*)GetFreeVideoView();
			}
			if (NULL == pVideo)
			{
				AddLog(LOG_TYPE_MSG,"","Can not get free video window");
				return;
			}

			PDEVICE_INFO pDevice = FindDevice(iLogonID);
			if(pDevice)
			{
				CLIENTINFO tInfo = {0};
				tInfo.m_iServerID = iLogonID;
				tInfo.m_iChannelNo = iChannelNo;
				tInfo.m_iStreamNO = iStreamNo;
				tInfo.m_iNetMode = iNetMode;
				tInfo.m_iTimeout = 20;
				if (m_bFlagThreeStream)
				{
					tInfo.m_iFlag = FLAG_THREE_STREAM;
					m_bFlagThreeStream = FALSE;
					if (0 != strcmp(pDevice->cProxy, ""))
					{
						AddLog(LOG_TYPE_FAIL,"%s CH%d-%d","not support three streamm!",tInfo.m_cRemoteIP
							,tInfo.m_iChannelNo,tInfo.m_iStreamNO);
						return;
					}
				}
				memcpy_s(tInfo.m_cRemoteIP,sizeof(tInfo.m_cRemoteIP),pDevice->cIP,sizeof(tInfo.m_cRemoteIP));
				
				//int iRet = NetClient_StartRecvEx(&uConnID,&tInfo,NULL,NULL);
				int	iRet = RET_FAILED;
				NetClientPara tPara = {0};
				tPara.iSize = sizeof(NetClientPara);
				memcpy(&tPara.tCltInfo, &tInfo, sizeof(CLIENTINFO));
				
				if (SYNC_MODE == g_iSdkUseMode)
				{
					tPara.pCbkFullFrm = GetNotifyTest;
					tPara.pvCbkFullFrmUsrData = this;
					tPara.pCbkRawFrm = GetRawNotify;
					tPara.pvCbkRawFrmUsrData = this;
					tPara.pvWnd = pVideo->GetSafeHwnd();
					iRet = NetClient_SyncRealPlay(&uConnID, &tPara, sizeof(NetClientPara));
				}
				else
				{
					iRet = NetClient_StartRecv_V5(&uConnID, &tPara, sizeof(NetClientPara));
				}
				
				if (0 == iRet)
				{
					//Full frame and raw code stream callback function usage example
					if (SYNC_MODE == g_iSdkUseMode)
					{
						int iRet0 = NetClient_SetFullStreamNotify_V4(uConnID, GetNotifyTest, 0);
						int iRet1 = NetClient_SetRawFrameCallBack(uConnID, GetRawNotify, 0);
						int iRet3 = NetClient_SetRawFrameCallBackEx(uConnID, GetRawNotifyEx, 0);
						//int iRet2 = NetClient_SetDevUserDataNotify(uConnID, GetDevUserDataNotify, this);
					}

					AddLog(LOG_TYPE_SUCC,"%s CH%d-%d","(%d)NetClient_StartRecvEx(%u,%d,NULL,NULL)",tInfo.m_cRemoteIP
						,tInfo.m_iChannelNo,tInfo.m_iStreamNO,iRet,uConnID,iNetMode);
					PCHANNEL_INFO pChannel = AddChannel(uConnID);
					if (pChannel)
					{
						pChannel->iLogonID = iLogonID;
						pChannel->iChannelNo = iChannelNo;
						pChannel->iStreamNo = iStreamNo;
						pChannel->hItem = hItem;
						pVideo->SetConnID(uConnID);
						pChannel->pVideo = pVideo;
						ChangeChannel(hItem);
					}
					else
					{
						AddLog(LOG_TYPE_MSG,"%s CH%d-%d","AddChannel(%u) Can not add channel",tInfo.m_cRemoteIP,tInfo.m_iChannelNo
							,tInfo.m_iStreamNO,uConnID);
					}
				}
				else if (1 == iRet)//Start playing
				{
					AddLog(LOG_TYPE_SUCC,"%s CH%d-%d","(%d)NetClient_StartRecvEx(%u)",tInfo.m_cRemoteIP
						,tInfo.m_iChannelNo,tInfo.m_iStreamNO,iRet,uConnID);

					PCHANNEL_INFO pChannel = FindChannel(uConnID);
					if (NULL != pChannel && NULL != pChannel->pVideo)
					{
						pVideo = (CLS_VideoView*)pChannel->pVideo;
						RECT rcShow = {0};
						iRet = NetClient_StartPlay(uConnID,pVideo->GetSafeHwnd(), rcShow, 0);
						if (iRet == 0)
						{
							ChangeChannel(pChannel->hItem);
#ifdef GDYHRW_DZ
							ProcessDZVideoCover(iLogonID, uConnID, iChannelNo, iStreamNo);
#endif
							AddLog(LOG_TYPE_SUCC,"%s CH%d-%d","NetClient_StartPlay(%u,,,)",tInfo.m_cRemoteIP,tInfo.m_iChannelNo
								,tInfo.m_iStreamNO,uConnID);
						}
						else
						{
							AddLog(LOG_TYPE_FAIL,"%s CH%d-%d","NetClient_StartPlay(%u,,,)",tInfo.m_cRemoteIP,tInfo.m_iChannelNo
								,tInfo.m_iStreamNO,uConnID);
						}
					}
					else
					{
						if (SERVER_ACTIVE == pDevice->iServerType || SERVER_REG_ACTIVE == pDevice->iServerType)
						{
							PCHANNEL_INFO pChannel = AddChannel(uConnID);
							if (NULL != pChannel)
							{
								pChannel->iLogonID = iLogonID;
								pChannel->iChannelNo = iChannelNo;
								pChannel->iStreamNo = iStreamNo;
								pChannel->hItem = hItem;
								pVideo->SetConnID(uConnID);
								pChannel->pVideo = pVideo;
								RECT rcShow = {0};
								iRet = NetClient_StartPlay(uConnID,pVideo->GetSafeHwnd(), rcShow, 0);
								if (0 == iRet)
								{
									m_tvDevice.SetItemImage(pChannel->hItem,ICON_CONNECT_SUCCESS,ICON_CONNECT_SUCCESS);
									ChangeChannel(pChannel->hItem);
#ifdef GDYHRW_DZ
									ProcessDZVideoCover(iLogonID, uConnID, iChannelNo, iStreamNo);
#endif
									AddLog(LOG_TYPE_SUCC,"%s CH%d-%d","Proxy:NetClient_StartPlay(%u)"
										, tInfo.m_cRemoteIP, tInfo.m_iChannelNo, tInfo.m_iStreamNO, uConnID);
								}
								else
								{
									AddLog(LOG_TYPE_FAIL,"%s CH%d-%d","Proxy:NetClient_StartPlay(%u)" 
										, tInfo.m_cRemoteIP, tInfo.m_iChannelNo, tInfo.m_iStreamNO, uConnID);
								}
							}
							else
							{
								AddLog(LOG_TYPE_MSG,"%s CH%d-%d","Proxy:AddChannel(%u) Can not add channel"
									, tInfo.m_cRemoteIP, tInfo.m_iChannelNo, tInfo.m_iStreamNO, uConnID);
							}
						}
						else
						{
							AddLog(LOG_TYPE_MSG,pDevice->cIP,"FindChannel(%u) Can not find channel",uConnID);
						}
					}
				}
				else
				{
					AddLog(LOG_TYPE_FAIL,"%s CH%d-%d","(%d)NetClient_StartRecvEx(%u,%d,NULL,NULL)",tInfo.m_cRemoteIP
						,tInfo.m_iChannelNo,tInfo.m_iStreamNO,iRet,uConnID,iNetMode);
				}
			}
			else
			{
				AddLog(LOG_TYPE_MSG,"","FindDevice(%d) Can not find device",iLogonID);
			}
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","GetParentItem(%d) Can not get parent item",hItem);
		}
	}
}

void CNetClientDemoDlg::OnDisconnect()
{
	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (hItem)
	{
		HTREEITEM hParent = m_tvDevice.GetParentItem(hItem);
		if (hParent)
		{
			int iLogonID = (int)m_tvDevice.GetItemData(hParent);
			unsigned int uChannel = (int)m_tvDevice.GetItemData(hItem);
			int iChannelNo = -1;
			int iStreamNo = -1;
			AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
			if (2 == uChannel)
			{
				iStreamNo = CHANNEL_THREE_STREAM;
			}
			unsigned int uConnID = -1;
			PCHANNEL_INFO pChannel = RemoveChannel(iLogonID,iChannelNo,iStreamNo,&uConnID);
			if (pChannel)
			{
				if (SYNC_MODE == g_iSdkUseMode)
				{
					NetClient_StopRealPlay(uConnID, 1);
				}
				else
				{
					NetClient_StopRecv(uConnID);
				}

				if (pChannel->pVideo)
				{
					CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
					pVideo->SetConnID(-1);
					pVideo->Invalidate();
				}
				AddLog(LOG_TYPE_SUCC,"","NetClient_StopRecv(%u)",uConnID);
			}
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","GetParentItem(%d) Can not get parent item",hItem);
		}
		m_tvDevice.SetItemImage(hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
	}
}

void CNetClientDemoDlg::OnStartRecvPic()
{
	if (WINDOW_PREVIEW != m_tabMain.GetCurSel() || TRUE == m_bLogCtrlStretched)
	{
		return;
	}

	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (NULL != hItem)
	{
		HTREEITEM hParent = m_tvDevice.GetParentItem(hItem);
		if (NULL != hParent)
		{
			int iLogonID = (int)m_tvDevice.GetItemData(hParent);
			unsigned int uChannel = (unsigned int)m_tvDevice.GetItemData(hItem);
			int iChannelNo = -1;
			int iStreamNo = -1;
			AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);

			if (iStreamNo != CHANNEL_TYPE_RECVPIC)
			{
				return;
			}

			unsigned int uConnID = -1;
			PCHANNEL_INFO pChannel = RemoveChannel(iLogonID,iChannelNo,iStreamNo,&uConnID);
			if (NULL != pChannel)
			{
				int iRet = NetClient_StopRecvNetPicStream(uConnID);

				AddLog(LOG_TYPE_SUCC,"","NetClient_StopRecvNetPicStream(%u)",uConnID);

				m_tvDevice.SetItemImage(pChannel->hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);

				uConnID = -1;
				pChannel = NULL;
			}

			PDEVICE_INFO pDevice = FindDevice(iLogonID);
			if(NULL != pDevice)
			{
				//open image stream
				NetPicPara tPara = {0};
				tPara.iStructLen = sizeof(NetPicPara);
				tPara.iChannelNo = iChannelNo;
				tPara.cbkPicStreamNotify = OnNotify_PicStream;
				tPara.pvUser = this;
#if 0
				tPara.iPicType = 2;
				tPara.iPicStreamCmd = NET_PICSTREAM_CMD_TRANS_ALGDATA;
#endif
				int iRet = NetClient_StartRecvNetPicStream(iLogonID, &tPara, sizeof(NetPicPara), &uConnID);	
				
				if (RET_SUCCESS == iRet)
				{
					AddLog(LOG_TYPE_SUCC,"","NetClient_StartRecvNetPicStream(%u,%s)",uConnID,(LPSTR)(LPCTSTR)GetLocalSaveDirectory());
				}
				else
				{
					AddLog(LOG_TYPE_FAIL,"","NetClient_StartRecvNetPicStream(%u,%s)",uConnID,(LPSTR)(LPCTSTR)GetLocalSaveDirectory());
				}

				PCHANNEL_INFO pChannel = AddChannel(uConnID);
				if (NULL != pChannel)
				{
					pChannel->iLogonID = iLogonID;
					pChannel->iChannelNo = iChannelNo;
					pChannel->iStreamNo = iStreamNo;
					pChannel->hItem = hItem;

					m_tvDevice.SetItemImage(pChannel->hItem,ICON_CONNECT_SUCCESS,ICON_CONNECT_SUCCESS);

				}
				else
				{
					AddLog(LOG_TYPE_MSG,"%s CH%d-%d","AddChannel(%u) Can not add channel",pDevice->cIP,iChannelNo
						,iStreamNo,uConnID);
				}
			}
			else
			{
				AddLog(LOG_TYPE_MSG,"","FindDevice(%d) Can not find device",iLogonID);
			}
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","GetParentItem(%d) Can not get parent item",hItem);
		}
	}
}

void CNetClientDemoDlg::OnStopRecvPic()
{
	HTREEITEM hItem = m_tvDevice.GetSelectedItem();
	if (NULL != hItem)
	{
		HTREEITEM hParent = m_tvDevice.GetParentItem(hItem);
		if (NULL != hParent)
		{
			int iLogonID = (int)m_tvDevice.GetItemData(hParent);
			unsigned int uChannel = (int)m_tvDevice.GetItemData(hItem);
			int iChannelNo = -1;
			int iStreamNo = -1;
			AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
			
			if (iStreamNo != CHANNEL_TYPE_RECVPIC)
			{
				return;
			}

			unsigned int uConnID = -1;
			PCHANNEL_INFO pChannel = RemoveChannel(iLogonID,iChannelNo,iStreamNo,&uConnID);
			if (NULL != pChannel)
			{
				int iRet = NetClient_StopRecvNetPicStream(uConnID);
				AddLog(LOG_TYPE_SUCC,"","NetClient_StopRecvNetPicStream(%u)",uConnID);
			}
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","GetParentItem(%d) Can not get parent item",hItem);
		}
		m_tvDevice.SetItemImage(hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
	}
}

int CNetClientDemoDlg::OnNotify_PicStream(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser)
{
	//Image stream callback function
	CNetClientDemoDlg* pThis = (CNetClientDemoDlg*)_pvUser;
	if (NULL != pThis)
	{	
		//Image stream data processing
		if (NET_PICSTREAM_CMD_VCA == _lCommand)
		{
			DealPicStreamVCA(_uiRecvID, _lCommand, _pvBuf, _iBufLen, _pvUser);
		}
		else if (NET_PICSTREAM_CMD_FACE == _lCommand)
		{
			DealPicStreamFace(_uiRecvID, _lCommand, _pvBuf, _iBufLen, _pvUser);
		}
		else if (NET_PICSTREAM_CMD_ITS == _lCommand)
		{
			DealPicStreamITS(_uiRecvID, _lCommand, _pvBuf, _iBufLen, _pvUser);
		}
		else if (NET_PICSTREAM_CMD_NORMALSNAP == _lCommand)
		{
			DealSnapPicStream(_uiRecvID, _lCommand, _pvBuf, _iBufLen, _pvUser);
		} else if (NET_PICSTREAM_CMD_TRANS_ALGDATA == _lCommand) {
			DealTransAlgData(_uiRecvID, _pvBuf, _iBufLen, _pvUser);
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","OnNotify_PicStream(%u,%d)",_uiRecvID,_lCommand);
		}
	}

	return 0;
}

void CNetClientDemoDlg::OnTcnSelchangeTabMain(NMHDR *pNMHDR, LRESULT *pResult)
{
	ShowBaseWindow(m_tabMain.GetCurSel());
	*pResult = 0;
}

int CNetClientDemoDlg::SDKInit()
{
	int iRet = LoadNVSSDK();
	if (DLL_LOAD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","LoadNVSSDK fail!");
		return -1;
	}

	SetSdkWorMode(); //Default heavyweight login
	SetExternDevLogonMode(); 

	//Set the TCP window size, which can alleviate the problem of video freezing when the network delay is large to a certain extent
	NetClient_SetDevConfig(-1, NET_CLIENT_ENABLE_NET_OPTIMIZE, 0, NULL, 0);

	//Save AlarmLink Param
	NetClient_SetDevConfig(-1, NET_CLIENT_SAVE_ALARMLINK, 0, NULL, 0);

	//Initialize the interface library
	int iServerPort = 0;
	int iClientPort = 6000;

	while(-2 == NetClient_SetPort(iServerPort,iClientPort))//Set the default network port used by the client and the host
	{
		iServerPort++;
		iClientPort++;
	}
#if 0
	NetClient_SetMSGHandle(MSG_MAIN, this->GetSafeHwnd(), MSG_PARAMCHANGE, MSG_ALARM);//Set the response message
	return NetClient_Startup();//Start the SDK
#else
	NetClient_SetNotifyFunction_V4(&CNetClientDemoDlg::MainNotify
		,&CNetClientDemoDlg::AlarmNotify,&CNetClientDemoDlg::ParamChangeNotify
		,&CNetClientDemoDlg::ComRecvNotify,&CNetClientDemoDlg::ProxyNotify);

	NetClient_SetAlarmNotify_V5(&CNetClientDemoDlg::AlarmNotify_V5);
	NetClient_Startup_V4(iServerPort,iClientPort,0);
#endif

#if 0
	NetClient_SetDevConfig(-1, NET_CLIENT_SSL_ENCRYPT_NVR, 0, NULL, 0);
#endif

	return 0;
}

LRESULT CNetClientDemoDlg::OnMainMessage( WPARAM wParam, LPARAM lParam )
{
	AddLog(LOG_TYPE_MSG,"","OnMainMessage(%u,%u)",wParam,lParam);
	return 0;
}

LRESULT CNetClientDemoDlg::OnParamChangeMessage( WPARAM wParam, LPARAM lParam )
{
	AddLog(LOG_TYPE_MSG,"","OnParamChangeMessage(%u,%u)",wParam,lParam);
	return 0;
}

LRESULT CNetClientDemoDlg::OnAlarmMessage( WPARAM wParam, LPARAM lParam )
{
	AddLog(LOG_TYPE_MSG,"","OnAlarmMessage(%u,%u)",wParam,lParam);
	return 0;
}

int CNetClientDemoDlg::AddChannelItem(int _iChannelNo,int _iChannelType,HTREEITEM _hParent)
{
	CString strTitle;
	unsigned int uChannel = 0;
	if (CHANNEL_TYPE_THIRDSTREAM != _iChannelType && (CHANNEL_TYPE_MAINSTREAM == _iChannelType || CHANNEL_TYPE_SUBSTREAM == _iChannelType))
	{
		MakeChannel(_iChannelNo,_iChannelType == 1 ? 1 : 0, &uChannel);
	} 
	else if (CHANNEL_TYPE_AUDIO == _iChannelType)
	{
		MakeChannel(_iChannelNo,_iChannelType, &uChannel);
	}
	else if (CHANNEL_TYPE_RECVPIC == _iChannelType)
	{
		MakeChannel(_iChannelNo,_iChannelType, &uChannel);
	}
	else
	{
		//uChannel = 2;
		MakeChannel(_iChannelNo, CHANNEL_THREE_STREAM, &uChannel);
	}
	
	switch(_iChannelType)
	{
	case CHANNEL_TYPE_MAINSTREAM:
		strTitle.Format(_T("CH%d-MAIN"),_iChannelNo);
		break;
	case CHANNEL_TYPE_SUBSTREAM:
		strTitle.Format(_T("CH%d-SUB"),_iChannelNo);
		break;
	case CHANNEL_TYPE_DIGITAL:
		strTitle.Format(_T("CH%d-DIGITAL"),_iChannelNo);
		break;
	case CHANNEL_TYPE_COMBINE:
		strTitle.Format(_T("CH%d-COMBINE"),_iChannelNo);
		break;
	case CHANNEL_TYPE_THIRDSTREAM:
		strTitle.Format(_T("CH%d-THIRD"),_iChannelNo);
		break;
	case CHANNEL_TYPE_AUDIO:
		strTitle.Format(_T("CH%d-AUDIO"),_iChannelNo);
		break;
	case CHANNEL_TYPE_RECVPIC:
		strTitle.Format(_T("CH%d-PIC"),_iChannelNo);
		break;
	default:
		strTitle.Format(_T("CH%d-%d"),_iChannelNo,_iChannelType);
		break;
	}
	HTREEITEM hItem = m_tvDevice.InsertItem(strTitle,ICON_NOT_CONNECT,ICON_NOT_CONNECT,_hParent);
	m_tvDevice.SetItemData(hItem,uChannel);

	return 0;
} 

void CNetClientDemoDlg::LogonNotify( int _iLogonID,int _iStatus)
{
	AddLog(LOG_TYPE_MSG,"%d","WCM_LOGON_NOTIFY(%d)",_iLogonID,_iStatus);

	PDEVICE_INFO pDevice = NULL;
	if (LOGON_SUCCESS == _iStatus)
	{
		pDevice = FindDevice(_iLogonID);
		if (pDevice)
		{
			EncoderInfoEx tInfo = {0};
			int iRetBytes = -1;
			//NetClient_GetDevInfo(_iLogonID,&tInfo);
			int iRet = NetClient_GetDevConfig(_iLogonID, NET_CLIENT_GET_ENCODERINFO, 0, &tInfo, sizeof(EncoderInfoEx), &iRetBytes);
			if (iRet < 0)
			{
				AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_GET_ENCODERINFO fail!");
			}
			else
			{
				AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_GET_ENCODERINFO success!");
			}

			if (NULL == pDevice->hItem)
			{
				if (IPV6_VERSION == tInfo.iIpVersion)
				{
					pDevice->hItem = m_tvDevice.InsertItem(tInfo.cNvsIpV6,ICON_NOT_LOGON,ICON_NOT_LOGON,m_hTreeMainItem);
				}
				else
				{
					pDevice->hItem = m_tvDevice.InsertItem(tInfo.cNvsIpV4,ICON_NOT_LOGON,ICON_NOT_LOGON,m_hTreeMainItem);
				}
				m_tvDevice.SetItemData(pDevice->hItem,_iLogonID);
				m_tvDevice.SelectItem(pDevice->hItem);
			}
			int iImage = 0;
			m_tvDevice.GetItemImage(pDevice->hItem,iImage,iImage);
			if (ICON_NOT_LOGON == iImage)
			{
				int iChannelNum = 0;
				int iRet = NetClient_GetChannelNum(_iLogonID,&iChannelNum);
				if (0 == iRet)
				{
					AddLog(LOG_TYPE_MSG,pDevice->cIP,"NetClient_GetChannelNum(%d,%d)",_iLogonID,iChannelNum);
					//int iType = CHANNEL_TYPE_MAINSTREAM;
					for (int i = 0; i < iChannelNum; ++i)
					{
						//modify by zhy 2013.04.15
						int iType = 0;
						iRet = NetClient_GetProductType(_iLogonID, &iType);
						if (iRet == 0)
						{
							if (iType&0x080000)
							{
								AddChannelItem(i,CHANNEL_TYPE_MAINSTREAM,pDevice->hItem);
								AddChannelItem(i,CHANNEL_TYPE_SUBSTREAM,pDevice->hItem);
								AddChannelItem(i,CHANNEL_TYPE_THIRDSTREAM,pDevice->hItem);
								AddChannelItem(i,CHANNEL_TYPE_AUDIO,pDevice->hItem);
								AddChannelItem(i,CHANNEL_TYPE_RECVPIC,pDevice->hItem);

							}
							else
							{
								AddChannelItem(i,CHANNEL_TYPE_MAINSTREAM,pDevice->hItem);
							}
						}
						else
						{
							AddLog(LOG_TYPE_FAIL,pDevice->cIP,"NetClient_GetProductType(%d,%d)",_iLogonID,iType);
						}
						/*iRet = NetClient_GetChannelProperty(_iLogonID,i,GENERAL_CMD_GET_CHANNEL_TYPE,&iType,sizeof(int));
						if (0 == iRet)
						{
							if (CHANNEL_TYPE_MAINSTREAM == iType || CHANNEL_TYPE_COMBINE == iType)
							{
								AddChannelItem(i,CHANNEL_TYPE_MAINSTREAM,pDevice->hItem);
								AddChannelItem(i,CHANNEL_TYPE_SUBSTREAM,pDevice->hItem);
							}
							else
							{
								AddChannelItem(i,iType,pDevice->hItem);
							}
						}
						else
						{
							AddLog(LOG_TYPE_FAIL,pDevice->cIP,"NetClient_GetChannelProperty(%d,%d,CHANNEL_TYPE)",_iLogonID,i);
							AddChannelItem(i,CHANNEL_TYPE_MAINSTREAM,pDevice->hItem);
						}*/
						//modify by zhy end
					}
				}
				else
				{
					AddLog(LOG_TYPE_FAIL,pDevice->cIP,"NetClient_GetChannelNum(%d,%d)",_iLogonID,iChannelNum);
				}
			}
			else
			{
				AddLog(LOG_TYPE_MSG,pDevice->cIP,"GetItemImage(%d,%d)",pDevice->hItem,iImage);
			}
			if (IPV6_VERSION == tInfo.iIpVersion)
			{
				strcpy_s(pDevice->cIP,sizeof(pDevice->cIP),tInfo.cNvsIpV6);
			}
			else
			{
				strcpy_s(pDevice->cIP,sizeof(pDevice->cIP),tInfo.cNvsIpV4);
			}
			strcpy_s(pDevice->cID,sizeof(pDevice->cID),tInfo.cFactoryID);
			m_tvDevice.SetItemImage(pDevice->hItem,ICON_LOGON_SUCCESS,ICON_LOGON_SUCCESS);
			m_tvDevice.SetItemText(pDevice->hItem,pDevice->cIP);
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","FindDevice(%d) Can not find device",_iLogonID);
		}

		ChangeChannel();	
	}
	else if (LOGON_SUCCESS_LIGHT == _iStatus)
	{
		return;
	}
	else
	{
		GetLogonFaildReason(_iLogonID, _iStatus);

		pDevice = FindDevice(_iLogonID);
		if (NULL != pDevice)
		{
			if (pDevice->uiInterTalkID != -1)
			{
				NetClient_InterTalkEnd(pDevice->uiInterTalkID, TRUE);
				pDevice->bIsTalking = false;
				pDevice->uiInterTalkID = -1;
			}
			else if (pDevice->bIsTalking)
			{
				NetClient_TalkEnd(_iLogonID);
				pDevice->bIsTalking = false;
			}
		}
		OnLogoffDevice(_iLogonID);
		NetClient_Logoff(_iLogonID);
		if (NULL != pDevice && NULL != pDevice->hItem)
		{
			UI_DeleteDevice(pDevice->hItem, _iLogonID);
			m_tvDevice.DeleteItem(pDevice->hItem);
		}
	}
}

int	CNetClientDemoDlg::GetLogonFaildReason(int _iLogonID, int _iStatus)
{
	int iLogonResult = RET_FAILED;
	NetClient_GetDevConfig(_iLogonID, NET_CLIENT_LOGONFAILED_REASON, 0, NULL, 0, &iLogonResult);
	
	CString cstrReason;
	if (CIPHER_USERNAME_ERROR == iLogonResult)
	{
		cstrReason = GetTextByLan("登录失败，用户名不存在！","Login failed, username does not exist!");
	}
	else if (CIPHER_USERPASS_ERROR == iLogonResult)
	{
		cstrReason = GetTextByLan("登录失败，密码错误！","Login failed, password incorrect!");
	}
	else if (PSWD_ERR_TIMES_OVERRUN == iLogonResult)
	{
		cstrReason = GetTextByLan("登录失败，密码错误次数超限，账号已锁定！","Login failed, the number of password errors has exceeded the limit, and the account has been locked!");
	}
	else if (ILLEGAL_PLATFORM == iLogonResult)
	{
		cstrReason = GetTextByLan("登录失败，非法平台！","Login failed, illegal platform!");
	}
	else if (LOGON_NET_ERROR == iLogonResult)
	{
		cstrReason = GetTextByLan("登录失败，网络错误！","Login failed, network error!");
	}
	else if (LOGON_PORT_ERROR == iLogonResult)
	{
		cstrReason = GetTextByLan("登录失败，端口拒绝！","Login failed, port denied!");
	}
	else
	{
		if (LOGON_TIMEOUT == _iStatus)
		{
			cstrReason = GetTextByLan("登录超时！","Login timeout!");
		}
		else
		{
			cstrReason = GetTextByLan("登录失败！","Login failed!");
		}
	}

	AddLog(LOG_TYPE_FAIL,"%d","%s",_iLogonID, cstrReason);

	return iLogonResult;
}

void CNetClientDemoDlg::VideoArrive( int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	AddLog(LOG_TYPE_MSG,"%d CH%d-%d","WCM_VIDEO_HEAD",_iLogonID,_iChannelNo,_iStreamNo);

	if (SYNC_MODE == g_iSdkUseMode)
	{
		return;
	}

	PDEVICE_INFO pDevice = FindDevice(_iLogonID);
	if (NULL == pDevice)
	{
		AddLog(LOG_TYPE_MSG,"%d CH%d-%d","FindDevice(%d) Can not find device!",_iLogonID,_iChannelNo,_iStreamNo,_iLogonID);
		return;
	}

	unsigned int uConnID = -1;
	PCHANNEL_INFO pChannel = FindChannel(_iLogonID,_iChannelNo,_iStreamNo,&uConnID);
	if (NULL != pChannel && NULL != pChannel->pVideo)
	{
		if (pChannel->hItem)
		{
			m_tvDevice.SetItemImage(pChannel->hItem,ICON_CONNECT_SUCCESS,ICON_CONNECT_SUCCESS);
		}
		
		CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
		RECT rcShow = {0};
		NetClient_StopPlay(uConnID);
		int iRet = NetClient_StartPlay(uConnID,pVideo->GetSafeHwnd(),rcShow,0);
		if (0 == iRet)
		{			
#ifdef GDYHRW_DZ
			ProcessDZVideoCover(_iLogonID, uConnID, _iChannelNo, _iStreamNo);
#endif
			AddLog(LOG_TYPE_SUCC,"%s CH%d-%d","NetClient_StartPlay(%u,,,)",pDevice->cIP,_iChannelNo,_iStreamNo,uConnID);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"%s CH%d-%d","NetClient_StartPlay(%u,,,)",pDevice->cIP,_iChannelNo,_iStreamNo,uConnID);
		}
	}
	else
	{
		if (SERVER_ACTIVE == pDevice->iServerType || SERVER_REG_ACTIVE == pDevice->iServerType)
		{
			AddLog(LOG_TYPE_MSG, pDevice->cIP, "Proxy:WCM_VIDEO_HEAD(%d,%d,%d)", _iLogonID, _iChannelNo, _iStreamNo);
		}
		else
		{
			AddLog(LOG_TYPE_MSG, pDevice->cIP, "FindChannel(%d,%d,%d) Can not find channel(%u)"
				, _iLogonID, _iChannelNo, _iStreamNo, uConnID);
		}
	}
}

void CNetClientDemoDlg::ErrorOrder( int _iLogonID )
{
	AddLog(LOG_TYPE_MSG,"%d","WCM_ERR_ORDER",_iLogonID);

	PDEVICE_INFO pDevice = FindDevice(_iLogonID);
	if (pDevice)
	{
		int iImage = -1;
		m_tvDevice.SetItemImage(pDevice->hItem,ICON_ERROR_ORDER,ICON_ERROR_ORDER);
		HTREEITEM hItem = m_tvDevice.GetChildItem(pDevice->hItem);
		while(hItem)
		{
			m_tvDevice.GetItemImage(hItem,iImage,iImage);
			if (ICON_CONNECT_SUCCESS == iImage)
			{
				unsigned int uChannel = (unsigned int)m_tvDevice.GetItemData(hItem);
				int iChannelNo = -1;
				int iStreamNo = -1;
				AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
				PCHANNEL_INFO pChannel = FindChannel(_iLogonID,iChannelNo,iStreamNo);
				if (pChannel)
				{
					CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
					if (pVideo)
					{
						pVideo->Invalidate();
					}
				}

				m_tvDevice.SetItemImage(hItem,ICON_ERROR_DATANET,ICON_ERROR_DATANET);
			}
			hItem = m_tvDevice.GetNextSiblingItem(hItem);
		}
		ChangeChannel();
	}
}

void CNetClientDemoDlg::ErrorDataNet( unsigned int _uConnID )
{
	PCHANNEL_INFO pChannel = FindChannel(_uConnID);
	if (pChannel)
	{
		CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
		if (pVideo)
		{
			pVideo->Invalidate();
		}

		m_tvDevice.SetItemImage(pChannel->hItem,ICON_ERROR_DATANET,ICON_ERROR_DATANET);
	}
	CString cstrLog = GetTextByLan("数据通道错误，未接收到数据流！","Network data channel error, no data stream received!");
	AddLog(LOG_TYPE_MSG,"","%s WCM_ERR_DATANET or WCM_SHORT_ERR_DATANET (%u)",cstrLog,_uConnID);
}

void CNetClientDemoDlg::OnTvnSelchangedTreeDeviceList(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMTREEVIEW pNMTreeView = reinterpret_cast<LPNMTREEVIEW>(pNMHDR);

	HTREEITEM hItem = pNMTreeView->itemNew.hItem;	
	if (NULL == hItem || hItem == pNMTreeView->itemOld.hItem)
	{
		return;
	}
	int iLogonID = -1;
	unsigned uChannel = -1;
	int iImage = ICON_NOT_LOGON;
	HTREEITEM hParent = m_tvDevice.GetParentItem(hItem);
	m_tvDevice.GetItemImage(hParent,iImage,iImage);
	if (hParent && ICON_DEV_TREE != iImage)
	{
		
		if (ICON_LOGON_SUCCESS == iImage)
		{
			iLogonID = (int)m_tvDevice.GetItemData(hParent);
		}
		else
		{
			iLogonID = -1;
		}
		uChannel = (int)m_tvDevice.GetItemData(hItem);
		int iChannelNo = -1;
		int iStreamNo = -1;
		AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
		ChangeChannel(iLogonID,iChannelNo,iStreamNo);
	}
	else
	{
		m_tvDevice.GetItemImage(hItem,iImage,iImage);
		if (ICON_LOGON_SUCCESS == iImage)
		{
			iLogonID = (int)m_tvDevice.GetItemData(hItem);
		}
		else
		{
			iLogonID = -1;
		}
		ChangeChannel(iLogonID,-1,-1);
		PDEVICE_INFO pDevice = FindDevice(iLogonID);
		UI_UpdateDeviceInfo(pDevice);
	}

	*pResult = 0;
}

void CNetClientDemoDlg::UI_UpdateDeviceInfo(PDEVICE_INFO _pDevice)
{
	if (_pDevice)
	{
		SetDlgItemText(IDC_EDIT_IP,_pDevice->cIP);
		SetDlgItemText(IDC_EDIT_PROXY,_pDevice->cProxy);
		SetDlgItemText(IDC_EDIT_ID,_pDevice->cID);
	}
	else
	{
		SetDlgItemText(IDC_EDIT_IP,_T(""));
		SetDlgItemText(IDC_EDIT_PROXY,_T(""));
		SetDlgItemText(IDC_EDIT_ID,_T(""));
	}
}

void CNetClientDemoDlg::UI_UpdateDemoVersion()
{
#if 0
	static char szVerInfo[MAX_PATH] = {0};

	char cFilePath[MAX_PATH*2] = {0};
	BOOL bRet = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
	if (bRet)
	{
		unsigned int uInfoSize = 0;  
		VS_FIXEDFILEINFO *pFileInfo = NULL;  

		char* lpvMem = szVerInfo;
		::GetFileVersionInfo(cFilePath,0,sizeof(szVerInfo),  lpvMem);  
		::VerQueryValue(lpvMem,  "\\",  (void**)&pFileInfo,  &uInfoSize);  

		CString strVersion;
		strVersion.Format(_T("%d.%d.%d.%d"),HIWORD(pFileInfo->dwFileVersionMS)
			,LOWORD(pFileInfo->dwFileVersionMS),HIWORD(pFileInfo->dwFileVersionLS)
			,LOWORD(pFileInfo->dwFileVersionLS));
		SetDlgItemText(IDC_EDIT_VERSION,strVersion);
	}
#else
	SDK_VERSION tVersion = {0};
	int iRet = NetClient_GetVersion(&tVersion);
	if (0 == iRet)
	{
		SetDlgItemText(IDC_EDIT_VERSION,tVersion.m_cVerInfo);
	}
#endif
}

void CNetClientDemoDlg::OnBnClickedRadioLocalLog()
{
	m_lvLocalLog.ShowWindow(SW_SHOW);
	m_lvAlarmLog.ShowWindow(SW_HIDE);
}

void CNetClientDemoDlg::OnBnClickedRadioAlarmInfo()
{
	m_lvLocalLog.ShowWindow(SW_HIDE);
	m_lvAlarmLog.ShowWindow(SW_SHOW);
}

void CNetClientDemoDlg::AddAlarmInfo(int _iLogonID, int _iChannel, int _iAlarmType, int _iAlarmState, void* _pvAlmPara, int _iParaSize)
{
	CTime  cTime = CTime::GetCurrentTime();
	CString strTime = cTime.Format("%y-%m-%d %H:%M:%S");

	CString strDevInfo;
	int iAlarmState = -1;
	CommonAlarmNotify	tCommonAlm = {0};
	FaceAlarmNotify		tFaceAlm = {0};
	if (NULL != _pvAlmPara && _iParaSize > 0) {
		if (ALARM_TYPE_FACE_IDENT == _iAlarmType) {
			memcpy(&tFaceAlm, _pvAlmPara, min(_iParaSize, (int)sizeof(FaceAlarmNotify)));
			iAlarmState = tFaceAlm.iState;
			strDevInfo.Format(_T("%d CH%d PARA(%d,%d,%d,%s)"), _iLogonID, tFaceAlm.iChanNo
				, tFaceAlm.iType, tFaceAlm.iFaceKey, tFaceAlm.iTrackId, tFaceAlm.cAlarmTime);
		} else {
			memcpy(&tCommonAlm, _pvAlmPara, min(_iParaSize, (int)sizeof(CommonAlarmNotify)));
			iAlarmState = tCommonAlm.iState;
			strDevInfo.Format(_T("%d CH%d PARA(%d,%d,%d,%d,%s,%s)"), _iLogonID, tCommonAlm.iChanNo
				, tCommonAlm.iAddlPara1, tCommonAlm.iAddlPara2, tCommonAlm.iAddlPara3, tCommonAlm.iAddlPara4
				, tCommonAlm.cAddlPara5, tCommonAlm.cAlarmTime);
		}
	} else {
		iAlarmState = _iAlarmState;
		strDevInfo.Format(_T("%d CH%d"), _iLogonID, _iChannel);
	}

	CString strAlarmType;
	switch(_iAlarmType)
	{
	case ALARM_VDO_MOTION:
		strAlarmType.Format(_T("%s"),_T("MOTION"));
		break;
	case ALARM_VDO_REC:
		strAlarmType.Format(_T("%s"),_T("REC"));
		break;
	case ALARM_VDO_LOST:
		strAlarmType.Format(_T("%s"),_T("VIDEOLOST"));
		break;
	case ALARM_VDO_INPORT:
		strAlarmType.Format(_T("%s"),_T("INPORT"));
		break;
	case ALARM_VDO_OUTPORT:
		strAlarmType.Format(_T("%s"),_T("OUTPORT"));
		break;
	case ALARM_VDO_COVER:
		strAlarmType.Format(_T("%s"),_T("COVER"));
		break;
	case ALARM_VCA_INFO:
		strAlarmType.Format(_T("%s"),_T("VCA"));
		break;
	case ALARM_AUDIO_LOST:
		strAlarmType.Format(_T("%s"),_T("AUDIOLOST"));
		break;
	case ALARM_EXCEPTION:
		strAlarmType.Format(_T("%s"),_T("EXCEPTION"));
		break;
	case ALARM_VCA_INFO_EX:
		strAlarmType.Format(_T("%s"),_T("VCAEX"));
		break;
	case ALARM_UNIQUE_ALERT_MSG:
		strAlarmType.Format(_T("%s"),_T("UNIQUEALERT"));
		break;
	case ALARM_TYPE_BLACK_BODY_DETECT:
		strAlarmType.Format(_T("%s"),_T("BLACKBODY"));
		break;
	case ALARM_TYPE_VEHICLE_IDENTIFICATION:
		strAlarmType.Format(_T("%s"),_T("VEHICLEIDENTIFICATION"));
		break;
	default:
		strAlarmType.Format(_T("%d"),_iAlarmType);
		break;
	}

	CString strAlarmState;
	switch(iAlarmState)
	{
	case 0:
		strAlarmState.Format(_T("%s"),_T("OFF"));
		break;
	case 1:
		strAlarmState.Format(_T("%s"),_T("ON"));
		break;
	default:
		strAlarmState.Format(_T("%d"),iAlarmState);
		break;
	}

	if (m_lvAlarmLog.GetItemCount() >= 5000) {
		m_lvAlarmLog.DeleteAllItems();
	}	

	m_lvAlarmLog.InsertItem(0,strTime);
	m_lvAlarmLog.SetItemText(0,1,strAlarmState);
	m_lvAlarmLog.SetItemText(0,2,strAlarmType);
	m_lvAlarmLog.SetItemText(0,3,strDevInfo);
}

void CNetClientDemoDlg::OnBnClickedBtnLogClear()
{
	//Messagebox
	CButton* pButton = ((CButton*)GetDlgItem(IDC_RADIO_LOCAL_LOG));
	if (pButton->GetCheck())
	{
		m_lvLocalLog.DeleteAllItems();
	}
	else
	{
		m_lvAlarmLog.DeleteAllItems();
	}
}

int CNetClientDemoDlg::ChangeChannel( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (m_pCurrent)
	{
		m_pCurrent->OnChannelChanged(_iLogonID,_iChannelNo,_iStreamNo);
		return 0;
	}

	return -1;
}

int CNetClientDemoDlg::ChangeChannel(HTREEITEM _hItem /*= NULL*/ )
{
	if (NULL == _hItem)
	{
		_hItem = m_tvDevice.GetSelectedItem();
	}
	if (NULL == _hItem)
	{
		return ChangeChannel(-1,-1,-1);
	}

	int iLogonID = -1;
	int iImage = ICON_NOT_LOGON;
	m_tvDevice.GetItemImage(_hItem,iImage,iImage);
	if (ICON_DEV_TREE == iImage)
	{
		return ChangeChannel(-1,-1,-1);
	}
	
	HTREEITEM hParent = m_tvDevice.GetParentItem(_hItem);
	m_tvDevice.GetItemImage(hParent,iImage,iImage);
	
	//The current node is a device
	if (ICON_DEV_TREE == iImage)
	{
		m_tvDevice.GetItemImage(_hItem,iImage,iImage);
		if (ICON_LOGON_SUCCESS == iImage)
		{
			iLogonID = (int)m_tvDevice.GetItemData(_hItem);
		}
		else
		{
			iLogonID = -1;
		}
		return ChangeChannel(iLogonID,-1,-1);
	}

	//The current node is a channel
	m_tvDevice.GetItemImage(hParent,iImage,iImage);
	if (ICON_LOGON_SUCCESS == iImage)
	{
		iLogonID = (int)m_tvDevice.GetItemData(hParent);
	}
	else
	{
		iLogonID = -1;
	}
	unsigned int uChannel = (unsigned int)m_tvDevice.GetItemData(_hItem);
	int iChannelNo = -1;
	int iStreamNo = -1;
	AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);

	return ChangeChannel(iLogonID,iChannelNo,iStreamNo);
}

int CNetClientDemoDlg::ChangeLanguage( int _iLanguage )
{
	SetCurrentLanguage(_iLanguage);
	for(int i = 0; i < WINDOW_MAX; ++i)
	{
		if(m_pWindow[i])
		{
			m_pWindow[i]->OnLanguageChanged(_iLanguage);
		}
	}

	UI_UpdateDialog(_iLanguage);
	return 0;
}

void OnVideoChanged( void* _pVideo,void* _pUserData )
{
	if ( _pVideo && _pUserData)
	{
		CNetClientDemoDlg* pThis = (CNetClientDemoDlg*)_pUserData;
		CLS_VideoView* pVideo = (CLS_VideoView*)_pVideo;
		HTREEITEM hItem = NULL;
		PCHANNEL_INFO pChannel = FindChannel(pVideo->GetConnID());
		if (pChannel)
		{
			hItem = pChannel->hItem;
		}
		pThis->m_tvDevice.SelectItem(hItem);
	}
}

void* CNetClientDemoDlg::GetFreeVideoView()
{
	CLS_PreviewWindow* pPreview = (CLS_PreviewWindow*)m_pWindow[WINDOW_PREVIEW];
	if (pPreview)
	{
		return pPreview->GetFreeVideoView();
	}
	return NULL;
}

int CNetClientDemoDlg::MakeChannel( int _iChannelNo,int _iStreamNo,unsigned int* _puChannel )
{
	if (NULL == _puChannel)
	{
		return -1;
	}

	*_puChannel = (_iChannelNo << 16 | _iStreamNo);
	return 0;
}

int CNetClientDemoDlg::AnalyzeChannel( unsigned int _uChannel,int* _piChannelNo,int* _piStreamNo )
{
	if (NULL == _piChannelNo || NULL == _piStreamNo || -1 == _uChannel)
	{
		return -1;
	}
	*_piChannelNo = HIWORD(_uChannel);
	*_piStreamNo = LOWORD(_uChannel);
	return 0;
}

void CNetClientDemoDlg::OnCbnSelchangeComboLanguage()
{
	ChangeLanguage(m_cboLanguage.GetCurSel());
	m_tvDevice.SetItemText(m_hTreeMainItem, GetTextByLan("设备树-右击添加设备","Device tree-right click add device"));
	Invalidate();
}

void CNetClientDemoDlg::UI_UpdateDialog(int _iLanguge)
{
	InsertItem(m_tabMain,WINDOW_PREVIEW,IDS_PREVIEW);
	InsertItem(m_tabMain,WINDOW_CONFIG,IDS_CONFIG);
	InsertItem(m_tabMain,WINDOW_PLAYBACK,IDS_PLAYBACK);
	InsertItem(m_tabMain,WINDOW_MANAGE,IDS_MANAGE);

	InsertColumn(m_lvLocalLog,0,IDS_LOG_TIME,LVCFMT_LEFT,120);
	InsertColumn(m_lvLocalLog,1,IDS_LOG_STATUS,LVCFMT_LEFT,50);
	InsertColumn(m_lvLocalLog,2,IDS_LOG_OPERATE,LVCFMT_LEFT,250);
	InsertColumn(m_lvLocalLog,3,IDS_LOG_DEVICE_INFO,LVCFMT_LEFT,140);
	InsertColumn(m_lvLocalLog,4,IDS_LOG_ERROR_INFO,LVCFMT_LEFT,100);

	InsertColumn(m_lvAlarmLog,0,IDS_LOG_TIME,LVCFMT_LEFT,100);
	InsertColumn(m_lvAlarmLog,1,IDS_LOG_STATUS,LVCFMT_LEFT,50);
	InsertColumn(m_lvAlarmLog,2,IDS_LOG_TYPE,LVCFMT_LEFT,150);
	InsertColumn(m_lvAlarmLog,3,IDS_LOG_DEVICE_INFO,LVCFMT_LEFT,360);

	InsertString(m_cboLanguage,0,IDS_LANGUAGE_CHINESE);
	InsertString(m_cboLanguage,1,IDS_LANGUAGE_ENGILIST);
	m_cboLanguage.SetCurSel(_iLanguge);

	SetDlgItemTextEx(IDC_RADIO_LOCAL_LOG,IDS_LOG_LOCAL_LOG);
	SetDlgItemTextEx(IDC_RADIO_ALARM_INFO,IDS_LOG_ALARM_INFO);
	SetDlgItemTextEx(IDC_BTN_LOG_CLEAR,IDS_LOG_CLEAR);
	SetDlgItemTextEx(IDC_BTN_LOG_SAVE,IDS_LOG_SAVE);
	SetDlgItemTextEx(IDC_BTN_LOG_CONFIG,IDS_LOG_CONFIG);
	SetDlgItemTextEx(IDC_BTN_LOG_TEST,IDS_LOG_TEST);

	SetDlgItemTextEx(IDC_STATIC_IP,IDS_LOGON_IP);
	SetDlgItemTextEx(IDC_STATIC_PROXY,IDS_LOGON_PROXY);
	SetDlgItemTextEx(IDC_STATIC_ID,IDS_LOGON_ID);

	SetDlgItemTextEx(IDC_STATIC_DEMO_VERSION,IDS_VERSION_DEMO);
	SetDlgItemText(IDC_BUTTON_GET_LASTERROR, GetTextByLan(_T("获取错误码"), _T("GetLastError")));
}

void CNetClientDemoDlg::OnBnClickedBtnLogConfig()
{
	CButton* pButton = ((CButton*)GetDlgItem(IDC_RADIO_LOCAL_LOG));
	if (pButton->GetCheck())
	{
		CLS_LogView dlgLog;
		dlgLog.DoModal();
	}
}

int CNetClientDemoDlg::StretchLogCtrl(BOOL _bStretch)
{
	if (_bStretch == m_bLogCtrlStretched)
	{
		return 0;
	}

	RECT rcTree = {0};
	RECT rcShow = {0};
	GetDlgItem(IDC_TREE_DEVICE_LIST)->GetWindowRect(&rcTree);
	m_lvLocalLog.GetWindowRect(&rcShow);
	int iTop = 0;
	int iCmdShow = SW_HIDE;
	if (TRUE == _bStretch)
	{
		iTop = rcTree.top;
		iCmdShow = SW_HIDE;
	}
	else
	{
		iTop = rcTree.bottom+4;
		iCmdShow = SW_SHOW;
	}
	rcShow.top = iTop;
	ScreenToClient(&rcShow);
	m_lvLocalLog.MoveWindow(&rcShow);
	m_lvAlarmLog.MoveWindow(&rcShow);
	m_pCurrent->ShowWindow(iCmdShow);
	m_bLogCtrlStretched = _bStretch;

	return 0;
}

void CNetClientDemoDlg::OnHdnItemclickListLogLocal(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMHEADER phdr = reinterpret_cast<LPNMHEADER>(pNMHDR);
	
	StretchLogCtrl(1-m_bLogCtrlStretched);

	*pResult = 0;
}

LRESULT CNetClientDemoDlg::OnGetDefID( WPARAM _wParam,LPARAM _lParam )
{
	return (DC_HASDEFID << 16 | 0);
}

void CNetClientDemoDlg::OnBnClickedBtnLogSave()
{
	CString strFileName;
	CListCtrl* plvLog = NULL;
	CButton* pButton = ((CButton*)GetDlgItem(IDC_RADIO_LOCAL_LOG));
	if (pButton->GetCheck())
	{
		plvLog = &m_lvLocalLog;
		strFileName.Format(_T("%s\\local.log"),GetLocalSaveDirectory());
	}
	else
	{
		plvLog = &m_lvAlarmLog;
		strFileName.Format(_T("%s\\alarm.log"),GetLocalSaveDirectory());
	}

	CHeaderCtrl* pHeader = plvLog->GetHeaderCtrl();
	if (NULL == pHeader)
	{
		return;
	}

	FILE* pFile = NULL;
	fopen_s(&pFile,(LPSTR)(LPCTSTR)strFileName,"wb+");
	if (NULL == pFile)
	{
		AddLog(LOG_TYPE_FAIL,"","fopen_s(%d,%s,wb+)",pFile,(LPSTR)(LPCTSTR)strFileName);
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

	AddLog(LOG_TYPE_MSG,"","save file(%s) finished",(LPSTR)(LPCTSTR)strFileName);
}

void CNetClientDemoDlg::OnBnClickedBtnLogTest()
{
	AddLog(LOG_TYPE_MSG,"","test");
}


#ifdef SDK_X64
void CNetClientDemoDlg::MainNotify( int _ulLogonID, long _iwParam, void* _ilParam, void* _iUser )
#else
void CNetClientDemoDlg::MainNotify(int _ulLogonID, long _iwParam, void* _ilParam,void* _iUser)
#endif
{
	MainNotify_SynchronousMode(_ulLogonID, _iwParam, (void*)(long)_ilParam, _iUser);
	
	_MAIN_NOTIFY_DATA* pData = (_MAIN_NOTIFY_DATA*)MallocMsgMemory(sizeof(_MAIN_NOTIFY_DATA));

	if (NULL != NetVideo_GetMainNotify)
	{
		int iRet = NetVideo_GetMainNotify(_ulLogonID, _iwParam, _ilParam, _iUser);
	}
	
	if (NULL != pData)
	{
		int iMsgType = _iwParam & 0xFFFF;
		pData->m_iLogonID = _ulLogonID;
		pData->m_wParam = _iwParam;
		pData->m_lParam = (void*)(long)_ilParam;
		pData->m_iUserData = (void*)(long)_iUser;
		if (WCM_DZ_TRANSPARENT == iMsgType) {
			//回调的参数是栈上的局部变量，需要拷贝保存
			DzTransparentPara* ptTmp = (DzTransparentPara*)_ilParam;
			if (NULL != ptTmp) {
				DzTransparentPara* ptPara = (DzTransparentPara*)malloc(sizeof(DzTransparentPara));
				if (NULL != ptPara) {
					memcpy(ptPara, ptTmp, min(ptTmp->iSize, (int)sizeof(DzTransparentPara)));
					pData->m_lParam = ptPara;
				}
			}
		} 

		CWnd* pWnd = AfxGetApp()->GetMainWnd();	
		if (WCM_ITS_PARKCARNUM == LOWORD(_iwParam) || WCM_FACE_MODEING == LOWORD(_iwParam) || WCM_SEEK_NVSS == LOWORD(_iwParam) || WCM_SHDB_SERVICETYPE == LOWORD(_iwParam) || WCM_SHDB_APPREPAIRSYS == LOWORD(_iwParam) || WCM_SHDB_TESTMAINTAIN == LOWORD(_iwParam) || WCM_SHDB_CHECKMANAGE == LOWORD(_iwParam) || WCM_CLOUD_AUTODETECT == LOWORD(_iwParam)|| WCM_NORTH_ANGLE == LOWORD(_iwParam)|| WCM_VISUAL_RANGE == LOWORD(_iwParam) || WCM_RADAR_STATUSINFO == LOWORD(_iwParam) || WCM_TO_DEFAULT_PARAM == LOWORD(_iwParam)) 
		{	//No message is thrown, processed directly in the callback
			CNetClientDemoDlg* pcls = (CNetClientDemoDlg*)pWnd;
			if (pcls->m_pWindow[WINDOW_CONFIG]) 
			{
				pcls->m_pWindow[WINDOW_CONFIG]->OnMainNotify(_ulLogonID, _iwParam, _ilParam, _iUser);
			}
		}
		else 
		{//Throw the message to the main thread for processing
			pWnd->PostMessage(MSG_MAIN_NOTIFY,(LPARAM)pData,0);
		}	
	}
}


//Block the main callback, handle some messages specially
void CNetClientDemoDlg::MainNotify_SynchronousMode( int _ulLogonID, int _iWparam, void* _pvLParam,void* _pvUser )
{
	int iMsgType = _iWparam & 0xFFFF;

	switch (iMsgType)
	{
	case WCM_HU_TEM_VALUE:
		{
			HuTemValue* ptValue = (HuTemValue*)_pvLParam;
			if (NULL == ptValue)
			{
				break;
			}

			int iCpySize = min((unsigned int)ptValue->iSize, sizeof(HuTemValue));
			HuTemValue tHumTemValue = {0};  
			memcpy(&tHumTemValue, ptValue, iCpySize);
			CString cstrTime;
			cstrTime.Format("%d-%d-%d  %d:%d:%d", tHumTemValue.tOccurTime.iYear, tHumTemValue.tOccurTime.iMonth, 
				tHumTemValue.tOccurTime.iDay, tHumTemValue.tOccurTime.iHour, 
				tHumTemValue.tOccurTime.iMinute, tHumTemValue.tOccurTime.iSecond);
			float fiValue = ((float)tHumTemValue.iValue - 1000)/10;

			OutPutLogMsg("[HUM-TEM-REPORT] Channel(%d) Type(%d) Time(%s) iValue(%2f)!", tHumTemValue.iChannelNo
				, tHumTemValue.iType, cstrTime, fiValue);
		}
		break;
	}
}

void CNetClientDemoDlg::AlarmNotify( int _iLogonID, int _iChannelNo, int _iAlarmState, ALARMTYPE _iAlarmType, void* _iUser )
{
// 	_ALARM_NOTIFY_DATA tData = {0};
// 	tData.m_iLogonID = _iLogonID;
// 	tData.m_iChannelNo = _iChannelNo;
// 	tData.m_iAlarmState = _iAlarmState;
// 	tData.m_iAlarmType = _iAlarmType;
// 	tData.m_iUserData = _iUser;
// 	
// 	CWnd* pWnd = AfxGetApp()->GetMainWnd();
// 	SendMessageTimeout(pWnd->GetSafeHwnd(),MSG_ALARM_NOTIFY,(LPARAM)&tData,0,SMTO_NORMAL,1000,NULL);
	_ALARM_NOTIFY_DATA* pData = (_ALARM_NOTIFY_DATA*)MallocMsgMemory(sizeof(_ALARM_NOTIFY_DATA));
	if (NULL != NetVideo_GetAlarmNotify)
	{
		int iRet = NetVideo_GetAlarmNotify(_iLogonID, _iChannelNo, &_iAlarmState, _iAlarmType, _iUser);
	}
	if (pData)
	{
		pData->m_iLogonID = _iLogonID;
		pData->m_iChannelNo = _iChannelNo;
		pData->m_iAlarmState = _iAlarmState;
		pData->m_iAlarmType = _iAlarmType;
		pData->m_iUserData = (int)_iUser;
		CWnd* pWnd = AfxGetApp()->GetMainWnd();
		pWnd->PostMessage(MSG_ALARM_NOTIFY,(LPARAM)pData,0);
	}
}

void CNetClientDemoDlg::AlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser)
{
	_ALARM_NOTIFY_DATA_V5* pData = (_ALARM_NOTIFY_DATA_V5*)MallocMsgMemory(sizeof(_ALARM_NOTIFY_DATA_V5));
	if (NULL != pData)
	{
		void* pvParam = NULL;
		int iCpySize = 0;
		if (ALARM_TYPE_FACE_IDENT == _iAlarmType) {
			pvParam = malloc(sizeof(FaceAlarmNotify));
			iCpySize = min(_iSize, (int)sizeof(FaceAlarmNotify));
		} else {
			pvParam = malloc(sizeof(CommonAlarmNotify));
			iCpySize = min(_iSize, (int)sizeof(CommonAlarmNotify));
		}
		memcpy(pvParam, _pInfo, min(_iSize, iCpySize));
		pData->m_iLogonID = _iLogonID;
		pData->m_iAlarmType = _iAlarmType;
		pData->m_pData =  pvParam;
		pData->m_iBufSize = iCpySize;
		pData->m_iUserData = _pUser;
		CWnd* pWnd = AfxGetApp()->GetMainWnd();
		pWnd->PostMessage(MSG_ALARM_NOTIFY_V5,(LPARAM)pData,0);
	}
}


void CNetClientDemoDlg::ParamChangeNotify( int _iLogonID, int _iChan, PARATYPE _iParaType,STR_Para* _pPara,void* _iUser )
{
	int iMallocSize = sizeof(_PARAMCHANGE_NOTIFY_DATA);
	_PARAMCHANGE_NOTIFY_DATA* pData = (_PARAMCHANGE_NOTIFY_DATA*)MallocMsgMemory(iMallocSize);
	if (NULL != NetVideo_GetParaChangeNotify)
	{
		int iRet = NetVideo_GetParaChangeNotify(_iLogonID, _iChan, _iParaType, _pPara, _iUser);
	}
	if (NULL == pData)
	{
		return;
	}

	pData->m_iLogonID = _iLogonID;
	pData->m_iChannelNo = _iChan;
	pData->m_iParaType = _iParaType;
	if (_pPara)
	{
		memcpy_s(&pData->m_pPara,sizeof(STR_Para),_pPara,sizeof(STR_Para));
	}
	pData->m_iUserData = (int)_iUser;
	CWnd* pWnd = AfxGetApp()->GetMainWnd();
 
	switch (pData->m_iParaType)
	{
	case PARA_REPORT_QUERY:
		{
			ReportQueryResult* ptTemp = (ReportQueryResult*)pData->m_pPara.m_iPara[0];
			if (NULL == ptTemp || ptTemp->iSize < 0)
			{
				break;
			}

			int iCpySize = min(ptTemp->iSize, sizeof(ReportQueryResult));

			memcpy(&pData->m_utParam.tReportQueryResult, ptTemp, iCpySize);
		}
		break;
	case PARA_IRRIGATION_NOTIFY:
		{
			IrrigationPara* ptTemp = (IrrigationPara*)pData->m_pPara.m_iPara[0];
			if (NULL == ptTemp || ptTemp->iSize < 0)
			{
				break;
			}

			int iCpySize = min(ptTemp->iSize, sizeof(IrrigationPara));

			memcpy(&pData->m_utParam.tIrrigationPara, ptTemp, iCpySize);
		}
		break;
	case PARA_GEOGRAFHY_LOCATION:
		{
			GeografhyLocation* ptTemp = (GeografhyLocation*)pData->m_pPara.m_iPara[0];
			if (NULL == ptTemp || ptTemp->iBufSize < 0)
			{
				break;
			}

			int iCpySize = min(ptTemp->iBufSize, sizeof(GeografhyLocation));

			memcpy(&pData->m_utParam.tGeografhyLocation, ptTemp, iCpySize);
		}
	case PARA_TARGET_REALTIME_RESULT:
		{
			TargetQueryResult* ptTemp = (TargetQueryResult*)pData->m_pPara.m_iPara[0];
			if (NULL == ptTemp || ptTemp->iSize < 0)
			{
				break;
			}

			int iCpySize = min(ptTemp->iSize, sizeof(TargetQueryResult));

			memcpy(&pData->m_utParam.tTargetQueryResult, ptTemp, iCpySize);
		}
		break;
		}
	
	pWnd->PostMessage(MSG_PARAMCHANGE_NOTIFY,(LPARAM)pData,0);
}

void CNetClientDemoDlg::ComRecvNotify( int _iLogonID, char *_cData, int _iLen,int _iComNo,void* _iUser )
{
	//TRACE("ComRecvNotify(%d,%#x,%d,%d,%d)\n",_iLogonID,_cData,_iLen,_iComNo,_iUser);
}

void CNetClientDemoDlg::ProxyNotify( int _iLogonID,int _iCmdKey, char* _cData, int _iLen,void* _iUser )
{
	//TRACE("ProxyNotify(%d,%d,%#x,%d,%d)\n",_iLogonID,_iCmdKey,_cData,_iLen,_iUser);
}

void CNetClientDemoDlg::TransProtocolData(int _iLogonID, int _iCmd, void* _pvData, int _iLen, void* _pvUsr)
{
	PDEVICE_INFO pDevice = FindDevice(_iLogonID);
	if(NULL == pDevice) {
		return;
	}

	if (!m_blProcStats) {
		return;
	}

	if (CMD_TRANS_PROTOCOL_DATA != _iCmd) {
		return;
	}

	if(0 == pDevice->clockStartRecvProtTime) {
		pDevice->clockStartRecvProtTime = clock();
	}

	if (clock() - pDevice->clockStartRecvProtTime > 10 * CLOCKS_PER_SEC) {
		return;
	}

	char* pcTmpBuf = NULL;
	pcTmpBuf = (char*)malloc(sizeof(char) * _iLen + 1);
	if(NULL == pcTmpBuf) {
		return;
	}

	memcpy(pcTmpBuf, (char*)_pvData, _iLen);
	pcTmpBuf[_iLen] = '\0';
#if 0
	char cOneProtocol[LEN_2048] = {0};
	sprintf(cOneProtocol, "%s\n", pcTmpBuf);
	OutputDebugString(cOneProtocol);
#endif
	GetProtocolQueue(_iLogonID)->push(pcTmpBuf);
}

LRESULT CNetClientDemoDlg::OnMainNotify( WPARAM wParam, LPARAM lParam )
{
	_MAIN_NOTIFY_DATA* pData = (_MAIN_NOTIFY_DATA*)wParam;
	__try
	{
		int iMsgType = pData->m_wParam & 0xFFFF;
		switch(iMsgType)
		{
			//login status message
			//lParam: login status
		case WCM_LOGON_NOTIFY:
			LogonNotify(pData->m_iLogonID,(int)pData->m_lParam);			
			break;

			//Video header message
			//lParam: channel
		case WCM_VIDEO_HEAD_EX:
		case WCM_AUDIO_HEAD:
			{
				RecvInfo tInfo = {0};
				int iRet = NetClient_GetRecvInfoById((int)pData->m_lParam, &tInfo, sizeof(tInfo));
				if (RET_SUCCESS == iRet)
				{
					int iStreamNo = tInfo.iStreamNo;
					if (SUB_STREAM == iStreamNo && FLAG_THREE_STREAM == tInfo.iFlag)
					{
						iStreamNo = CHANNEL_THREE_STREAM;
					}
					VideoArrive(tInfo.iLogonId, tInfo.iChanNo, iStreamNo);	
				}
			}
			break;	
			//Network command disconnect message, generated when the network connection is disconnected unexpectedly
		case WCM_ERR_ORDER:
			ErrorOrder(pData->m_iLogonID);
			break;

			// network data error
			//lParam, connection ID
		case WCM_ERR_DATANET:
		case WCM_SHORT_ERR_DATANET:
			ErrorDataNet((unsigned int)pData->m_lParam);
			break;

		case WCM_RECVMESSAGE:
			{
				int iLen = (pData->m_wParam >> 16) & 0xFFFF;
				char* pcData = (char*)pData->m_lParam;
				AddLog(LOG_TYPE_MSG,"%d","WCM_RECVMESSAGE %s,%d",pData->m_iLogonID,pcData,iLen);
			}
			break;

		case WCM_ERR_DATANET_MAX_COUNT:
			{
				unsigned int uiConnID = (unsigned int)pData->m_lParam;
				char cLog[LEN_512]={0};
				DWORD dwCurLan = CLS_LanguageManager::Instance()->GetLanguage();
				if (2052 == dwCurLan){
					sprintf_s(cLog, sizeof(cLog),"总连接数已达到最大值！");
				}else{
					sprintf_s(cLog, sizeof(cLog),"The number of connections has reached its maximum!");
				}
				AddLog(LOG_TYPE_MSG,"%d","%s WCM_ERR_DATANET_MAX_COUNT %u",pData->m_iLogonID,cLog, uiConnID);

				PCHANNEL_INFO pChannel = RemoveChannel(uiConnID);
				if (pChannel)
				{
					if (pChannel->pVideo)
					{
						CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
						pVideo->SetConnID(-1);
						pVideo->Invalidate();
					}
					if(pChannel->hItem)
					{
						m_tvDevice.SetItemImage(pChannel->hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
					}
				}
				NetClient_StopRecv(uiConnID);
				
			}
			break;

		case WCM_ERR_DIGITCHANNEL_NOT_ENABLED:
			{
				unsigned int uiConnID = (unsigned int)pData->m_lParam;
				char cLog[LEN_512]={0};
				DWORD dwCurLan = CLS_LanguageManager::Instance()->GetLanguage();
				if (2052 == dwCurLan){
					sprintf_s(cLog, sizeof(cLog),"数字通道未使能！");
				}else{
					sprintf_s(cLog, sizeof(cLog),"Digital channel not enabled!");
				}
				AddLog(LOG_TYPE_MSG,"%d","%s WCM_ERR_DIGITCHANNEL_NOT_ENABLED %u",pData->m_iLogonID,cLog,uiConnID);
				NetClient_StopRecv(uiConnID);
				PCHANNEL_INFO pChannel = RemoveChannel(uiConnID);
				if (pChannel)
				{
					if (pChannel->pVideo)
					{
						CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
						pVideo->SetConnID(-1);
						pVideo->Invalidate();
					}
					if(pChannel->hItem)
					{
						m_tvDevice.SetItemImage(pChannel->hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
					}
				}
				NetClient_StopRecv(uiConnID);
			}
			break;
		
		case WCM_ERR_DIGITCHANNEL_NOT_CONNECTED:
			{
				unsigned int uiConnID = (unsigned int)pData->m_lParam;
				char cLog[LEN_512]={0};
				DWORD dwCurLan = CLS_LanguageManager::Instance()->GetLanguage();
				if (2052 == dwCurLan){
					sprintf_s(cLog, sizeof(cLog),"数字通道未连接！");
				}else{
					sprintf_s(cLog, sizeof(cLog),"Digital channel is not connected!");
				}
				AddLog(LOG_TYPE_MSG,"%d","%s WCM_ERR_DIGITCHANNEL_NOT_CONNECTED %u",pData->m_iLogonID,cLog,uiConnID);
				PCHANNEL_INFO pChannel = RemoveChannel(uiConnID);
				if (pChannel)
				{
					if (pChannel->pVideo)
					{
						CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
						pVideo->SetConnID(-1);
						pVideo->Invalidate();
					}
					if(pChannel->hItem)
					{
						m_tvDevice.SetItemImage(pChannel->hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
					}
				}
				NetClient_StopRecv(uiConnID);
			}
			break;

		case WCM_ERR_DEV_NOTACTIVATED:
			{
				unsigned int uiConnID = (unsigned int)pData->m_lParam;
				char cLog[LEN_512]={0};
				DWORD dwCurLan = CLS_LanguageManager::Instance()->GetLanguage();
				if (2052 == dwCurLan){
					sprintf_s(cLog, sizeof(cLog),"设备未激活！");
				}else{
					sprintf_s(cLog, sizeof(cLog),"Device not activated!");
				}
				AddLog(LOG_TYPE_MSG,"%d","%s WCM_ERR_DEV_NOTACTIVATED %u",pData->m_iLogonID,cLog,uiConnID);
				PCHANNEL_INFO pChannel = RemoveChannel(uiConnID);
				if (pChannel)
				{
					if (pChannel->pVideo)
					{
						CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
						pVideo->SetConnID(-1);
						pVideo->Invalidate();
					}
					if(pChannel->hItem)
					{
						m_tvDevice.SetItemImage(pChannel->hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
					}
				}
				NetClient_StopRecv(uiConnID);
			}
			break;

		case WCM_NEED_DECRYPT_KEY:
			{
				int iRealChannel = (int)pData->m_lParam;
				AddLog(LOG_TYPE_MSG,"%d","WCM_NEED_DECRYPT_KEY %d",pData->m_iLogonID,iRealChannel);
			}
			break;

		case WCM_DECRYPT_KEY_FAILED:
			{
				int iRealChannel = (int)pData->m_lParam;
				AddLog(LOG_TYPE_MSG,"%d","WCM_DECRYPT_KEY_FAILED %d",pData->m_iLogonID,iRealChannel);
			}
			break;
		case WCM_DECRYPT_SUCCESS:
			{
				int iRealChannel = (int)pData->m_lParam;
				AddLog(LOG_TYPE_MSG,"%d","WCM_DECRYPT_SUCCESS %d",pData->m_iLogonID,iRealChannel);
			}
			break;
		case WCM_NEED_DECRYPT_KEY_EX:
		case WCM_DECRYPT_KEY_FAILED_EX:
			{
				unsigned int uiConnID = (unsigned int)pData->m_lParam;
				PCHANNEL_INFO pChannel = FindChannel(uiConnID);
				if(NULL != pChannel && NULL != pChannel->pVideo)
				{
					CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
					pVideo->ShowDecrypt();
				}
			}
			break;
		case WCM_ENCRYPT_TO_NOTENCRPT:
			{
				unsigned int uiConnID = (unsigned int)pData->m_lParam;
				PCHANNEL_INFO pChannel = FindChannel(uiConnID);
				if(NULL != pChannel && NULL != pChannel->pVideo)
				{
					CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
					pVideo->ShowDecrypt(FALSE);
				}
			}
			break;
		case WCM_LOCALSTORE_LOCK_FILE:
			{
				int iRealChannel = (int)pData->m_lParam;
				AddLog(LOG_TYPE_MSG,"%d","WCM_LOCALSTORE_LOCK_FILE %d",pData->m_iLogonID,iRealChannel);
			}
			break;
		//add by zhy 2013.04.11
		case WCM_LASTERROR_INFO:
			{
				int iRealChannel = (pData->m_wParam >> 16) & 0xFFFF;
				int iErrID = (int)pData->m_lParam;
				PromptLastError(pData->m_iLogonID, iRealChannel, iErrID);
				AddLog(LOG_TYPE_MSG,"%d","WCM_LASTERROR_INFO %d",pData->m_iLogonID,iRealChannel);
			}
			break;
		//add by zhy end
		case WCM_ERR_DUAL_USERS_AUTH_FAILED:
			{
				unsigned int uiConnID = (unsigned int)pData->m_lParam;
				AddLog(LOG_TYPE_MSG,"%d","WCM_ERR_DUAL_USERS_AUTH_FAILED %u",pData->m_iLogonID,uiConnID);
				NetClient_StopRecv(uiConnID);
				PCHANNEL_INFO pChannel = RemoveChannel(uiConnID);
				if (pChannel)
				{
					if (pChannel->pVideo)
					{
						CLS_VideoView* pVideo = (CLS_VideoView*)pChannel->pVideo;
						pVideo->SetConnID(-1);
						pVideo->Invalidate();
					}
					if(pChannel->hItem)
					{
						m_tvDevice.SetItemImage(pChannel->hItem,ICON_NOT_CONNECT,ICON_NOT_CONNECT);
					}
				}
				NetClient_StopRecv(uiConnID);
			}
			break;
		default :
			break;
		}
		for (int i = 0; i < WINDOW_MAX; ++i)
		{
			if (m_pWindow[i])
			{
				m_pWindow[i]->OnMainNotify(pData->m_iLogonID,pData->m_wParam,pData->m_lParam, pData->m_iUserData);
			}
		}
	}
	__finally
	{
		FreeMsgMemory(pData);
	}

	return 0;
}

LRESULT CNetClientDemoDlg::OnParamChangeNotify( WPARAM wParam, LPARAM lParam )
{
	_PARAMCHANGE_NOTIFY_DATA* pData = (_PARAMCHANGE_NOTIFY_DATA*)wParam;
	__try
	{
		//AddLog(LOG_TYPE_MSG,"%d-CH%d","ParamChangeNotify(%d)",pData->m_iLogonID,pData->m_iChannelNo,pData->m_iParaType);
		if (m_pCurrent)
		{
			if (PARA_REPORT_QUERY == pData->m_iParaType || PARA_IRRIGATION_NOTIFY == pData->m_iParaType || PARA_GEOGRAFHY_LOCATION == pData->m_iParaType || PARA_TARGET_REALTIME_RESULT == pData->m_iParaType)
			{
				m_pCurrent->OnParamChangeNotify(pData->m_iLogonID,pData->m_iChannelNo,pData->m_iParaType,&pData->m_utParam,pData->m_iUserData);
			}
			else
			{
				m_pCurrent->OnParamChangeNotify(pData->m_iLogonID,pData->m_iChannelNo,pData->m_iParaType,&pData->m_pPara,pData->m_iUserData);
			}
		}
	}
	__finally
	{
		FreeMsgMemory(pData);
	}
	
	return 0;
}

LRESULT CNetClientDemoDlg::OnAlarmNotify( WPARAM wParam, LPARAM lParam )
{
	_ALARM_NOTIFY_DATA* pData = (_ALARM_NOTIFY_DATA*)wParam;
	__try
	{
		//AddAlarmInfo(pData->m_iLogonID, pData->m_iChannelNo, pData->m_iAlarmType, pData->m_iAlarmState, NULL, 0);

		for (int i = 0; i < WINDOW_MAX; ++i)
		{
			if (m_pWindow[i])
			{
				m_pWindow[i]->OnAlarmNotify(pData->m_iLogonID,pData->m_iChannelNo,pData->m_iAlarmState,pData->m_iAlarmType,pData->m_iUserData);
			}
		}
	}
	__finally
	{
		FreeMsgMemory(pData);
	}

	return 0;
}

LRESULT CNetClientDemoDlg::OnAlarmNotify_V5( WPARAM wParam, LPARAM lParam )
{
	_ALARM_NOTIFY_DATA_V5* pData = (_ALARM_NOTIFY_DATA_V5*)wParam;
	__try
	{
		AddAlarmInfo(pData->m_iLogonID, -1, pData->m_iAlarmType, -1, pData->m_pData, pData->m_iBufSize);
		{
			if (m_pWindow[WINDOW_CONFIG])
			{
				m_pWindow[WINDOW_CONFIG]->OnAlarmNotify_V5(pData->m_iLogonID,pData->m_iAlarmType,pData->m_pData,pData->m_iBufSize,pData->m_iUserData);
			}
		}

		//CommonAlarmNotify* ptAlarm = (CommonAlarmNotify*)pData->m_pData;
		//char cDebugInfo[1024] = {0};
		//if (6 == pData->m_iAlarmType || 9 == pData->m_iAlarmType)
		//{
		//	vca_TAlarmInfo ti = {0};
		//	int iRetAlarmInfo = NetClient_VCAGetAlarmInfo(pData->m_iLogonID, ptAlarm->iState, &ti, sizeof(ti));
		//	sprintf(cDebugInfo, "AlarmNotifyV5-->iAlarmType=%d, iChanNo=%d, iState=%d, iEventType=%d.\n"
		//		, pData->m_iAlarmType, ptAlarm->iChanNo, ptAlarm->iState, ti.iEventType);
		//	OutputDebugString(cDebugInfo);
		//}
		//else
		//{
		//	sprintf(cDebugInfo, "AlarmNotifyV5-->iAlarmType=%d, iChanNo=%d, iState=%d.\n"
		//		, pData->m_iAlarmType, ptAlarm->iChanNo, ptAlarm->iState);
		//	OutputDebugString(cDebugInfo);
		//}
	}
	__finally
	{
		if(NULL != pData->m_pData) {
			free(pData->m_pData);
			pData->m_pData = NULL;
		}
		FreeMsgMemory(pData);
	}

	return 0;
}


void CNetClientDemoDlg::SaveDevice()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\Device40.ini" ;
	CIniFile DeviceFile(szNewFile);
	
	DeviceFile.ResetFile();
	int iDeviceCount = 0;
	CString szSection = "Device";
	CString szKey1 = "IPAddress";
	CString szKey2 = "UserName";
	CString szKey3 = "Password";
	CString szKey4 = "Port";
	CString szKey5 = "Proxy";
	CString szKey6 = "ID";
	CString szKey7 = "Number";
	CString szKey8 = "PlatFeature";

	HTREEITEM hItem = NULL;
	hItem = m_tvDevice.GetChildItem(m_hTreeMainItem);
	while(hItem)
	{
		CString szSection = "Device";
		int iLogonID = (int)m_tvDevice.GetItemData(hItem);
		PDEVICE_INFO Device = FindDevice(iLogonID);
		if (Device)
		{
			CString szID ;
			szID.Format("%d", iDeviceCount);
			szSection += szID;
			DeviceFile.WriteString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey1, (char *)(LPCTSTR)Device->cIP);
			DeviceFile.WriteString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey2, (char *)(LPCTSTR)Device->cUserName);
			DeviceFile.WriteString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey3, (char *)(LPCTSTR)Device->cPassword);
			DeviceFile.WriteInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey4, Device->iPort);
			DeviceFile.WriteString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey5, (char *)(LPCTSTR)Device->cProxy);
			DeviceFile.WriteString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey6, (char *)(LPCTSTR)Device->cID);
			DeviceFile.WriteString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey8, (char *)(LPCTSTR)Device->cPlatFeature);
			iDeviceCount++;
		}
		hItem = m_tvDevice.GetNextSiblingItem(hItem);
	} 

	szSection = "TotalDevice";
	CString szCount ;
	szCount.Format("%d", iDeviceCount);
	DeviceFile.WriteString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey7, (char *)(LPCTSTR)szCount);
}

void CNetClientDemoDlg::SetSdkWorMode()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\SDKWorkMode.ini";
	CIniFile DeviceFile(szNewFile);

	CString szSection = "WorkMode";
	CString szKey = "Mode";

	m_iSDKWorkMode = DeviceFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, HEAVY_MODE);
	if (NULL == NetClient_SetSDKWorkMode)
	{
		return;
	}
	int iRet = NetClient_SetSDKWorkMode(m_iSDKWorkMode);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetSDKWorkMode(%d)",m_iSDKWorkMode);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetSDKWorkMode(%d)",m_iSDKWorkMode);
	}	
}

void CNetClientDemoDlg::SetExternDevLogonMode()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\ExternDevLogonMode.ini";
	CIniFile DeviceFile(szNewFile);

	CString szSection = "ExternDevInfo";
	CString szKey = "DevType";

	int iDevType;
	iDevType = DeviceFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, SET_EX_DEV_TYPE_NORMAL);
	if (NULL == NetClient_SetExternDevLogonInfo)
	{
		return;
	}
	int iRet = NetClient_SetExternDevLogonInfo(iDevType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetExternDevLogonInfo(%d)",iDevType);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetExternDevLogonInfo(%d)",iDevType);
	}	
}

void CNetClientDemoDlg::LoadDevice()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\Device40.ini";
	CIniFile DeviceFile(szNewFile);
	int iDeviceCount = 0;
	CString szSection = "Device";
	CString szKey1 = "IPAddress";
	CString szKey2 = "UserName";
	CString szKey3 = "Password";
	CString szKey4 = "Port";
	CString szKey5 = "Proxy";
	CString szKey6 = "ID";
	CString szKey7 = "Number";
	CString szKey8 = "PlatFeature";
	szSection = "TotalDevice";
	iDeviceCount = DeviceFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey7, 0);
	for (int i = 0; i < iDeviceCount; i++)
	{
		szSection = "Device";
		CString szNo ;
		szNo.Format("%d", i);
		szSection += szNo;
		CString szIP = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey1, "");
		CString szUserName = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey2, "");
		CString szPassword = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey3, "");
		int iPort = DeviceFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey4, 3000);
		CString szProxy = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey5, "");
		CString szID = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey6, "");
		CString szPlatFeature = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey8, "");
		DEVICE_INFO DevLogon = {0};
		memcpy_s(DevLogon.cIP, sizeof(DevLogon.cIP), szIP.GetBuffer(), szIP.GetLength());
		memcpy_s(DevLogon.cUserName, sizeof(DevLogon.cUserName), szUserName.GetBuffer(), szUserName.GetLength());
		memcpy_s(DevLogon.cPassword, sizeof(DevLogon.cPassword), szPassword.GetBuffer(), szPassword.GetLength());
		memcpy_s(DevLogon.cProxy, sizeof(DevLogon.cProxy), szProxy.GetBuffer(), szProxy.GetLength());
		memcpy_s(DevLogon.cID, sizeof(DevLogon.cID), szID.GetBuffer(), szID.GetLength());
		memcpy_s(DevLogon.cPlatFeature, sizeof(DevLogon.cPlatFeature), szPlatFeature.GetBuffer(), szPlatFeature.GetLength());
		DevLogon.iPort = iPort;
		int iLogonID = -1;
		LogonPara tLogonPara = {0};
		tLogonPara.iSize = sizeof(LogonPara);
		tLogonPara.iNvsPort = DevLogon.iPort;
		strcpy(tLogonPara.cNvsIP, DevLogon.cIP);
		strcpy(tLogonPara.cUserName, DevLogon.cUserName);
		strcpy(tLogonPara.cUserPwd, DevLogon.cPassword);
		strcpy(tLogonPara.cCharSet, "UTF-8");
		strcpy(tLogonPara.cPlatFeature, DevLogon.cPlatFeature);
		tLogonPara.pfTransCbk = CNetClientDemoDlg::TransProtocolData;
		tLogonPara.pvTransUdata = this;
		int iLogonType = SERVER_NORMAL;
		
		if (IsValidIPv6(DevLogon.cIP) >= 1)
		{
			iLogonType = SERVER_NORMAL_IPV6;
			strcpy(tLogonPara.cNvsIpV6, DevLogon.cIP);
		}
		else
		{
			if(strlen(DevLogon.cIP) > 0 && 1 != IsValidIP(DevLogon.cIP))
			{
				AddLog(LOG_TYPE_MSG,"","invalid ip(%s)", DevLogon.cIP);
				return;
			}
			if(strlen(DevLogon.cProxy) > 0 && 1 != IsValidIP(DevLogon.cIP))
			{
				AddLog(LOG_TYPE_MSG,"","invalid proxy(%s)", DevLogon.cIP);
				return;
			}
			iLogonType = SERVER_NORMAL;
			strcpy(tLogonPara.cNvsIP, DevLogon.cIP);
		}
		if (SYNC_MODE == g_iSdkUseMode)
		{
			//Synchronous blocking login device
			iLogonID = NetClient_SyncLogon(iLogonType, &tLogonPara, sizeof(LogonPara));
		}
		else
		{
			iLogonID = NetClient_Logon_V4(iLogonType, &tLogonPara, sizeof(LogonPara));
		}
		if (iLogonID < 0)
		{
			AddLog(LOG_TYPE_FAIL, szIP, "NetClient_LogonEx(%d, %s.)", iLogonID, szIP);
		}
		else
		{
			PDEVICE_INFO pDevNew = AddDevice(iLogonID);
			if (pDevNew)
			{
				memcpy_s(pDevNew,sizeof(DEVICE_INFO),&DevLogon,sizeof(DEVICE_INFO));
				pDevNew->hItem = NULL;
				pDevNew->uiInterTalkID = -1;
			}
		}
	}
}

void ComDataNotify(int _ulLogonID, char *_cData, int _iLen,int _iComNo,void* _iUser)
{
	CString szComData = Bytes2HexString((unsigned char *)_cData, _iLen);
	CString szOutput;
	szOutput.Format("Recv Com Data ,LogonID(%d), ComNo(%d), DataLen(%d), Data(%s).\n", _ulLogonID, _iComNo, _iLen, szComData);
	//OutputDebugString(szOutput);
	//AddLog(LOG_TYPE_SUCC,"","%s",szOutput);
}
int CNetClientDemoDlg::SetComDataShow()
{
	return NetClient_SetComRecvNotify_V4(ComDataNotify);
}

CTreeCtrl* CNetClientDemoDlg::GetDeviceTreeCtrl()
{
	return &m_tvDevice;
}

void CNetClientDemoDlg::OnLogoffDevice(int _iLogonID)
{
	for (int i = 0; i < WINDOW_MAX; ++i)
	{
		if (m_pWindow[i])
		{
			m_pWindow[i]->OnLogoffDevice(_iLogonID);
		}
	}
}

void CNetClientDemoDlg::PromptLastError(int _iLogonID,int _iChannel, int _iErrorID )
{
	switch (_iErrorID)
	{
	case EC_AV_SWITCHOVER_CHANNEL:MessageBox(GetTextEx(IDS_USER_Message18), _T(""));
		break;
	case EC_AV_QUERY_FILE: MessageBox(GetTextEx(IDS_USER_Message19), _T(""));
		break;
	case EC_OSD_LOGO_FORMAT:MessageBox(GetTextEx(IDS_USER_Message20), _T(""));
		break;
	case EC_OSD_LOGO_DATA:MessageBox(GetTextEx(IDS_USER_Message21), _T(""));
		break;
	case EC_SERIAL_PORT_PTZ:MessageBox(GetTextEx(IDS_USER_Message22), _T(""));
		break;
	case EC_EXCEPTION:MessageBox(GetTextEx(IDS_USER_Message23), _T(""));
		break;
	case EC_ALARM:MessageBox(GetTextEx(IDS_USER_Message24), _T(""));
		break;
	case EC_STORAGE_LOCK_FILE: MessageBox(GetTextEx(IDS_USER_Message25), _T(""));
		break;
	case EC_STORAGE_UNLOCK_FILE: MessageBox(GetTextEx(IDS_USER_Message26), _T(""));
		break;
	case EC_STORAGE_REDUNDANCY:MessageBox(GetTextEx(IDS_USER_Message27), _T(""));
		break;
	case EC_STORAGE_HOLIDAY_TIME:MessageBox(GetTextEx(IDS_USER_Message28), _T(""));
		break;
	case EC_NET_SERVICE:MessageBox(GetTextEx(IDS_USER_Message29), _T(""));
		break;
	case EC_LOGON_USERNAME:
		{
			int iCmd = COMMAND_ID_DEV_LASTERROR;
			DevLastError tErrInfo = {0};
			tErrInfo.iSize = sizeof(DevLastError);
			int iRet = NetClient_RecvCommand(_iLogonID, iCmd, _iChannel, &tErrInfo, sizeof(DevLastError));
			MessageBox(GetTextEx(IDS_USER_Message30)+":"+tErrInfo.cErrorInfo, _T(""));
		}
		break;
	case EC_LOGON_PASSWORD:
		{
			int iCmd = COMMAND_ID_DEV_LASTERROR;
			DevLastError tErrInfo = {0};
			tErrInfo.iSize = sizeof(DevLastError);
			int iRet = NetClient_RecvCommand(_iLogonID, iCmd, _iChannel, &tErrInfo, sizeof(DevLastError));
			MessageBox(GetTextEx(IDS_USER_Message31)+":"+tErrInfo.cErrorInfo, _T(""));
		}
		break;
	case EC_LOGON_DECRYPT_PWD:MessageBox(GetTextEx(IDS_USER_Message32), _T(""));
		break;
	case EC_LOGON_AUTHORITY:
		{
			AddLog(LOG_TYPE_MSG,"",GetTextEx(IDS_USER_Message33));
		}
		//MessageBox(GetTextEx(IDS_USER_Message33), _T(""));
		break;
	case EC_LOGON_AUTHORITY_GROUP:MessageBox(GetTextEx(IDS_USER_Message34), _T(""));
		break;
	case EC_PU:MessageBox(GetTextEx(IDS_USER_Message35), _T(""));
		break;
	case EC_DISK_RECORDING:MessageBox(GetTextEx(IDS_USER_Message36), _T(""));
		break;
	case EC_DISK_MEMORY:MessageBox(GetTextEx(IDS_USER_Message37), _T(""));
		break;
	case EC_DISK_TYPE:MessageBox(GetTextEx(IDS_USER_Message38), _T(""));
		break;
	case EC_DISK_NO_DISK:MessageBox(GetTextEx(IDS_USER_Message39), _T(""));
		break;
	case EC_DISK_NO_SETTING:MessageBox(GetTextEx(IDS_USER_Message40), _T(""));
		break;
	case EC_DISK_REPEAT_ADD:MessageBox(GetTextEx(IDS_USER_Message41), _T(""));
		break;
	case EC_DISK_NO_RECORD_CHANNEL:MessageBox(GetTextEx(IDS_USER_Message42), _T(""));
		break;
	case EC_DISK_RECORD_QUOTA_BIG:MessageBox(GetTextEx(IDS_USER_Message43), _T(""));
		break;
	case EC_DISK_IMAGE_QUOTA_BIG:MessageBox(GetTextEx(IDS_USER_Message44), _T(""));
		break;
	case EC_DISK_DEL_LOCK_FILE: MessageBox(GetTextEx(IDS_USER_Message45), _T(""));
		break;
	case EC_SYSTEM: MessageBox(GetTextEx(IDS_USER_Message46), _T(""));
		break;
	case EC_COM: MessageBox(GetTextEx(IDS_USER_Message47), _T(""));
		break;
	case EC_NET_CONNECTION: MessageBox(GetTextEx(IDS_USER_Message48), _T(""));
		break;
	case EC_VCA: MessageBox(GetTextEx(IDS_USER_Message49), _T(""));
		break;
	case EC_ATM: MessageBox(GetTextEx(IDS_USER_Message50), _T(""));
		break;
	case EC_ITS: MessageBox(GetTextEx(IDS_USER_Message51), _T(""));
		break;
	case EC_DZ: MessageBox(GetTextEx(IDS_USER_Message52), _T(""));
		break;
	case EC_COMMON_ENABLE: MessageBox(GetTextEx(IDS_USER_Message54), _T(""));
		break;
    case EC_NET_RTMP:
        {
            DevLastError tErrInfo = {0};
            tErrInfo.iSize = sizeof(DevLastError);
            NetClient_RecvCommand(_iLogonID, COMMAND_ID_DEV_LASTERROR, _iChannel, &tErrInfo, sizeof(DevLastError));
			MessageBox(GetTextByLan("设置RTMP参数失败", "Set RTMP param failed") + ":" +tErrInfo.cErrorInfo, _T(""));
        }
        break;
	case EC_NET_SNMP:
		{
			DevLastError tErrInfo = {0};
			tErrInfo.iSize = sizeof(DevLastError);
			NetClient_RecvCommand(_iLogonID, COMMAND_ID_DEV_LASTERROR, _iChannel, &tErrInfo, sizeof(DevLastError));
			MessageBox(GetTextByLan("设置SNMP参数失败", "Set SNMP param failed") + ":" + tErrInfo.cErrorInfo, _T(""));
		}
		break;
	case EC_BLACK_WHITE_LIST_RESTRICTED:
		{
			int iCmd = COMMAND_ID_DEV_LASTERROR;
			DevLastError tErrInfo = {0};
			tErrInfo.iSize = sizeof(DevLastError);
			int iRet = NetClient_RecvCommand(_iLogonID, iCmd, _iChannel, &tErrInfo, sizeof(DevLastError));
			if (0 == iRet)
			{
				MessageBox(GetTextEx(IDS_CONFIG_IP_FILTER)+":"+tErrInfo.cErrorInfo, _T(""));
			}
			else 
			{
				MessageBox(GetTextEx(IDS_CONFIG_IP_FILTER), _T(""));
			}
		}
		break;
	}
}

void CNetClientDemoDlg::UI_DeleteDevice(HTREEITEM _hItem, int _iLogonID)
{
	unsigned int uChannel = -1;
	int iChannelNo = -1;
	int iStreamNo = -1;
	PCHANNEL_INFO pChannel = NULL;
	CLS_VideoView* pVideo = NULL;
	HTREEITEM hSubItem = NULL;
	while((hSubItem = m_tvDevice.GetChildItem(_hItem)) != NULL)
	{
		uChannel = (int)m_tvDevice.GetItemData(hSubItem);
		AnalyzeChannel(uChannel,&iChannelNo,&iStreamNo);
		pChannel = FindChannel(_iLogonID, iChannelNo, iStreamNo);
		if (NULL != pChannel && NULL != pChannel->pVideo)
		{
			pVideo = (CLS_VideoView*)pChannel->pVideo;
			pVideo->SetConnID(-1);
		}
		m_tvDevice.DeleteItem(hSubItem);
	}
	PDEVICE_INFO pDevice = RemoveDevice(_iLogonID);
	m_tvDevice.SetItemData(_hItem, -1);
	m_tvDevice.SetItemImage(_hItem, ICON_NOT_LOGON, ICON_NOT_LOGON);
	ChangeChannel(-1,0,0);
}

void CNetClientDemoDlg::OnBnClickedButtonGetLasterror()
{
	unsigned long ulLastErr = NetClient_GetLastError();
	CString cstrLastErr;
	cstrLastErr.Format("%0x", ulLastErr);
	cstrLastErr = "0x" + cstrLastErr;
	SetDlgItemText(IDC_EDIT_GET_LASTERROR, cstrLastErr);
}

int CNetClientDemoDlg::GetDemoUseMode()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\DemoUseRule.ini";
	CIniFile DemoFile(szNewFile);

	CString szSection = "UseMode";
	CString szKey = "Mode";
	g_iSdkUseMode = DemoFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, 0);

	return g_iSdkUseMode;
}
void CNetClientDemoDlg::LoadSdkVideoMode()
{
	//Save it to the configuration file, and it will take effect when the software is restarted
	CString szNewFile = GetLocalSaveDirectory() + "\\SDKVideoMode.ini";
	CIniFile cFile(szNewFile);

	CString szSection = "VideoMode";
	int iMode = 0;
	iMode = cFile.ReadInteger((char *)(LPCTSTR)szSection, "DecodeMode", 0);//0:auto 1:d3d11va 2:dxva 3:forbid harddecode
	if (iMode < 3)
	{
		HwDecodeParam tHwDecInfo = {0};
		tHwDecInfo.iSize = sizeof(HwDecodeParam);
		tHwDecInfo.iLimitType = HWDEC_LIMIT_LOCAL;
		tHwDecInfo.iMaxCount = 100;
		tHwDecInfo.iEnableFlag = HWDEC_ENABLE_STREAM | HWDEC_ENABLE_VOD | HWDEC_ENABLE_FILE;
		tHwDecInfo.iDecodeType = iMode;
		NetClient_SetAVMode(-1,CMD_AV_DEC_HWDECODE_PARAM, &tHwDecInfo, sizeof(HwDecodeParam));
	}
	else
	{
		//First cancel the hard solution function
		HwDecodeParam tHwDecInfo = {0};	//The parameter can be cleared to zero
		tHwDecInfo.iSize = sizeof(HwDecodeParam);
		NetClient_SetAVMode(-1,CMD_AV_DEC_HWDECODE_PARAM, &tHwDecInfo, sizeof(HwDecodeParam));

		int iVideoDecLib = VIDEO_DLIB_FFMPEG;
		NetClient_SetAVMode(-1,CMD_AVMODE_VIDEO_DECLIB, &iVideoDecLib, sizeof(int));
	}

	iMode = cFile.ReadInteger((char *)(LPCTSTR)szSection, "RenderMode", 0);
	if (0 == iMode)
	{
		int iRenderType = SHOW_MAIN_MODE_D3D11;
		NetClient_SetAVMode(-1,CMD_VIDEO_SHOW_MAIN_MODE, &iRenderType, sizeof(int));
	}
	else if (1 == iMode)
	{
		int iRenderType = SHOW_MAIN_MODE_D3D;
		NetClient_SetAVMode(-1,CMD_VIDEO_SHOW_MAIN_MODE, &iRenderType, sizeof(int));
	} 
	else
	{
		int iRenderType = SHOW_MAIN_MODE_DRAW;
		NetClient_SetAVMode(-1,CMD_VIDEO_SHOW_MAIN_MODE, &iRenderType, sizeof(int));
	}
}

void CNetClientDemoDlg::GetTransProtocolCallbackFun(void** _pvThis)
{
	*_pvThis = this;
}

void CNetClientDemoDlg::ProcessDZVideoCover(int _iLogonID, unsigned int _uiConID, int _iChanNo, int _iStreamNo)
{
	char cUserName[16] = {0};
	char cPassword[16] = {0};
	int iAuthority = 0;
	RECT tRcCover[4] = {0};
	VideoCoverPara tCoverPara = {0};
	int iRet = RET_FAILED;

	iRet =  NetClient_GetCurUserInfo(_iLogonID, cUserName, cPassword, &iAuthority);
	if(RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "ProcessDZVideoCover, DEV%d-%d, CH%d-%d", "NetClient_GetCurUserInfo fail!", _iLogonID, _uiConID, _iChanNo, _iStreamNo);
		return;
	}

	if (AUT_ADMIN == iAuthority) {
		AddLog(LOG_TYPE_MSG, "ProcessDZVideoCover, DEV%d-%d, CH%d-%d", "AUT_ADMIN == iAuthority!", _iLogonID, _uiConID, _iChanNo, _iStreamNo);
		return;
	}

	iRet = NetClient_GetVideoCovArea(_iLogonID, _iChanNo, tRcCover, _iStreamNo);
	if(RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "ProcessDZVideoCover, DEV%d-%d, CH%d-%d", "NetClient_GetVideoCovArea fail1!", _iLogonID, _uiConID, _iChanNo, _iStreamNo);
		return;
	}

	tCoverPara.iEnable = ENABLE;
	tCoverPara.iCoordinateType = VIDEO_RESOLUTION_COORDINATE;
	tCoverPara.iAreaCount = 4;
	memcpy(&tCoverPara.tAreaRect, tRcCover, 4 * sizeof(RECT));
	iRet = NetClient_PlayerControl(_uiConID, CTRL_VIDEO_COVER_PARA, &tCoverPara, sizeof(VideoCoverPara), this);
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "ProcessDZVideoCover, DEV%d-%d, CH%d-%d", "NetClient_PlayerControl::CTRL_VIDEO_COVER_PARA fail!", _iLogonID, _uiConID, _iChanNo, _iStreamNo);
		return;
	}
}


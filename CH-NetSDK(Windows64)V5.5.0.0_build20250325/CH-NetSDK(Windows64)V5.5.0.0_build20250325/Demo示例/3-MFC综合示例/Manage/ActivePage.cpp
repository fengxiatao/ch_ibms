// ActivePage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "ActivePage.h"
#include "ProxyTypes.h"
#include "../LogonActive.h"
#include "../Include/PROXY_INTERFACE.h"
using namespace PROXY_INTERFACE;

#define COLUMN_FACTORYID	0
#define COLUMN_STATE		1
#define COLUMN_LANIP		2
#define COLUMN_WANIP		3
#define COLUMN_IPVERSION	4

#define MSG_UPDATE_NVSLST		(WM_USER + 1111)

HWND CLS_ActivePage::s_hWnd = NULL;

extern int g_iSdkUseMode;

const CString CONST_cstrMsg1_CH = "1, Before using the new active mode, be sure to set [local parameter configuration]. \r\n";
const CString CONST_cstrMsg2_CH = "2, The demo uses the new active mode with directory server by default. The new active mode with directory server needs to set [directory server parameter configuration] to log in to the device. All parameters of [directory server parameter configuration] are consistent with the parameter configuration of the device [registration center]. \r\n";
const CString CONST_cstrMsg3_CH = "3, If you use the new active mode without directory server, please do not select the [Directory Server] check box, and you do not need to set [Directory Server Parameter Configuration]. \r\n";
const CString CONST_cstrMsg4_CH = "4, Click the [Refresh] button, the list box can display the list of online devices. \r\n";
const CString CONST_cstrMsg5_CH = "5, Double-click an item in the list box to pop up a login dialog box, enter the correct user name and password to log in to the device. ";
const CString CONST_cstrUsrMsg_CH = CONST_cstrMsg1_CH + CONST_cstrMsg2_CH + CONST_cstrMsg3_CH + CONST_cstrMsg4_CH + CONST_cstrMsg5_CH;

const CString CONST_cstrMsg1_EN = "1. Before using the new active mode, please set the [Local Para Config] first.\r\n";
const CString CONST_cstrMsg2_EN = "2. The demo uses the new active mode with the directory server by default. The new active mode with the directory server needs to set [Directory server parameter configuration] to log in to the device,[Directory server parameter configuration] All parameters are consistent with the device [Registry Center] parameter configuration.\r\n";
const CString CONST_cstrMsg3_EN = "3. If you use the new active mode without a directory server, please do not select the [Directory Server] selection box, and you do not need to set [Directory server parameter configuration].\r\n";
const CString CONST_cstrMsg4_EN = "4. Click the [Refresh] button. The list box displays the online device list. \r\n";
const CString CONST_cstrMsg5_EN = "5. Double-click on an item in the list box to pop up the login dialog. Enter the correct user name and password to log in to the device.";
const CString CONST_cstrUsrMsg_EN = CONST_cstrMsg1_EN + CONST_cstrMsg2_EN + CONST_cstrMsg3_EN + CONST_cstrMsg4_EN + CONST_cstrMsg5_EN;

// CLS_ActivePage dialog

IMPLEMENT_DYNAMIC(CLS_ActivePage, CDialog)

CLS_ActivePage::CLS_ActivePage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ActivePage::IDD, pParent)
{
	CString strFile;
	strFile.Format("%s%s", ExtractFilePath(), "ActiveConfig.ini");
	m_iniFile.SetFileName(strFile.GetString());
}

CLS_ActivePage::~CLS_ActivePage()
{
}

void CLS_ActivePage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_REG_SERVER, m_chkRegServer);
	DDX_Control(pDX, IDC_EDIT_MNG_ACT_LOCA_IP, m_edtLocalIp);
	DDX_Control(pDX, IDC_EDIT_MNG_ACT_DSM_IP, m_edtDsmIp);
	DDX_Control(pDX, IDC_EDIT_MNG_ACT_DSM_PORT, m_edtDsmPort);
	DDX_Control(pDX, IDC_LIST_ACTIVE_REGISTER_LIST, m_lstNvsLst);
	DDX_Control(pDX, IDC_EDIT_ACTIVE_LISTEN_PORT, m_edtActivePort);
	DDX_Control(pDX, IDC_EDIT_ACCOUNT_NAME, m_edtRegAccountName);
	DDX_Control(pDX, IDC_EDIT_ACCOUNT_PWD, m_edtRegAccountPwd);
	DDX_Control(pDX, IDC_EDIT_ACTIVE_USER_MSG, m_edtUsrMsg);
	DDX_Control(pDX, IDC_EDIT_MNG_ACTIVE_LOCAL_PORT, m_edtLocalPort);
	DDX_Control(pDX, IDC_EDIT_WAN_IPV6, m_edtLocalWanIpV6);
	DDX_Control(pDX, IDC_EDIT_REGIPV6, m_edtRegisterIpV6);
	DDX_Control(pDX, IDC_CHECK_ACTIVE_WITH_REG_IPV6, m_chkIpV6WithReg);
}


BEGIN_MESSAGE_MAP(CLS_ActivePage, CDialog)
	ON_BN_CLICKED(IDC_CHECK_REG_SERVER, &CLS_ActivePage::OnBnClickedCheckRegServer)
	ON_BN_CLICKED(IDC_BUTTONMNG_ACTIVE_SET, &CLS_ActivePage::OnBnClickedButtonListenPortSet)
	ON_BN_CLICKED(IDC_BUTTON_DIRECTORY_SET, &CLS_ActivePage::OnBnClickedButtonDirectorySet)
	ON_BN_CLICKED(IDC_BUTTON_REFRESH_REGISTER_LIST, &CLS_ActivePage::OnBnClickedButtonRefreshRegisterList)
	ON_NOTIFY(NM_DBLCLK, IDC_LIST_ACTIVE_REGISTER_LIST, &CLS_ActivePage::OnNMDblclkListActiveRegisterList)
	ON_WM_SHOWWINDOW()
	ON_MESSAGE(MSG_UPDATE_NVSLST, &CLS_ActivePage::OnNvsLstMsg)
	ON_WM_DESTROY()
END_MESSAGE_MAP()


// CLS_ActivePage message handlers

BOOL CLS_ActivePage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	s_hWnd = this->GetSafeHwnd();

	m_edtActivePort.SetLimitText(LEN_16);
	m_edtLocalIp.SetLimitText(LEN_64);
	m_edtLocalPort.SetLimitText(LEN_16);
	m_edtDsmIp.SetLimitText(LEN_64);
	m_edtDsmPort.SetLimitText(LEN_32);
	m_edtRegAccountName.SetLimitText(LEN_64);
	m_edtRegAccountPwd.SetLimitText(LEN_64);
	m_edtUsrMsg.SetLimitText(LEN_1024);
	m_edtLocalWanIpV6.SetLimitText(LEN_128);
	m_edtRegisterIpV6.SetLimitText(LEN_128);
	
	ReadConfig();

	m_lstNvsLst.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	UI_UpdateDialogText();

	LoadProxySDK();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_ActivePage::ReadConfig()
{
	int iListenPort = m_iniFile.ReadInteger("LocalConfig", "ListenPort", 5004);
	SetDlgItemInt(IDC_EDIT_ACTIVE_LISTEN_PORT, iListenPort);

	int iLocalPort = m_iniFile.ReadInteger("LocalConfig", "WanPort", 5555);
	CString cstrWanIP = m_iniFile.ReadString("LocalConfig", "WanIP", "0.0.0.0");
	SetDlgItemInt(IDC_EDIT_MNG_ACTIVE_LOCAL_PORT, iLocalPort);
	SetDlgItemText(IDC_EDIT_MNG_ACT_LOCA_IP, cstrWanIP);
	CString cstrWanIpV6 = m_iniFile.ReadString("LocalConfig", "WanIpV6", "1234:5678:9abc:def::5555");
	SetDlgItemText(IDC_EDIT_WAN_IPV6, cstrWanIpV6);

	int iRegPort = m_iniFile.ReadInteger("RegisterConfig", "RegPort", 6004);
	SetDlgItemInt(IDC_EDIT_MNG_ACT_DSM_PORT, iRegPort);
	CString cstrRegIP = m_iniFile.ReadString("RegisterConfig", "RegIP", "0.0.0.0");
	SetDlgItemText(IDC_EDIT_MNG_ACT_DSM_IP, cstrRegIP);
	CString cstrRegAccount = m_iniFile.ReadString("RegisterConfig", "RegAccount", "QQ");
	SetDlgItemText(IDC_EDIT_ACCOUNT_NAME, cstrRegAccount);
	CString cstrRegRegPassword = m_iniFile.ReadString("RegisterConfig", "RegPassword", "QQQQ");
	SetDlgItemText(IDC_EDIT_ACCOUNT_PWD, cstrRegRegPassword);
	CString cstrRegIpV6 = m_iniFile.ReadString("RegisterConfig", "RegIpV6", "1234:5678:9abc:def::5555");
	SetDlgItemText(IDC_EDIT_REGIPV6, cstrRegIpV6);
}

void CLS_ActivePage::WriteConfig()
{
	m_iniFile.WriteInteger("ActiveType", "ShowType", 0);

	int iListenPort = GetDlgItemInt(IDC_EDIT_ACTIVE_LISTEN_PORT);
	m_iniFile.WriteInteger("LocalConfig", "ListenPort", iListenPort);

	int iLocalPort = GetDlgItemInt(IDC_EDIT_MNG_ACTIVE_LOCAL_PORT);
	m_iniFile.WriteInteger("LocalConfig", "WanPort", iLocalPort);
	CString cstrWanIP;
	GetDlgItemText(IDC_EDIT_MNG_ACT_LOCA_IP, cstrWanIP);
	m_iniFile.WriteString("LocalConfig", "WanIP", (char*)(LPCTSTR)cstrWanIP);
	CString cstrWanIpV6;
	GetDlgItemText(IDC_EDIT_WAN_IPV6, cstrWanIpV6);
	m_iniFile.WriteString("LocalConfig", "WanIpV6", (char*)(LPCTSTR)cstrWanIpV6);

	int iRegPort = GetDlgItemInt(IDC_EDIT_MNG_ACT_DSM_PORT);
	m_iniFile.WriteInteger("RegisterConfig", "RegPort", iRegPort);
	CString cstrRegIP;
	GetDlgItemText(IDC_EDIT_MNG_ACT_DSM_IP, cstrRegIP);
	m_iniFile.WriteString("RegisterConfig", "RegIP", (char*)(LPCTSTR)cstrRegIP);
	CString cstrRegIpV6;
	GetDlgItemText(IDC_EDIT_REGIPV6, cstrRegIpV6);
	m_iniFile.WriteString("RegisterConfig", "RegIpV6", (char*)(LPCTSTR)cstrRegIpV6);
	CString cstrRegAccount;
	GetDlgItemText(IDC_EDIT_ACCOUNT_NAME, cstrRegAccount);
	m_iniFile.WriteString("RegisterConfig", "RegAccount", (char*)(LPCTSTR)cstrRegAccount);
	CString cstrRegRegPassword;
	GetDlgItemText(IDC_EDIT_ACCOUNT_PWD, cstrRegRegPassword);
	m_iniFile.WriteString("RegisterConfig", "RegPassword", (char*)(LPCTSTR)cstrRegRegPassword);
}

void CLS_ActivePage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialogText();
	if (1 == _iLanguage)
	{
		m_edtUsrMsg.SetWindowText(CONST_cstrUsrMsg_EN);
	}
	else
	{
		m_edtUsrMsg.SetWindowText(CONST_cstrUsrMsg_CH);
	}

	OnBnClickedButtonRefreshRegisterList();
}

void CLS_ActivePage::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_ACTIVE_PORT_RANGE, GetTextByLan(_T("本地监听端口"), _T("Local Listening Port")));
	SetDlgItemTextEx(IDC_STATIC_MNG_ACTIVE_LOCAL_IP, IDS_MNG_LOCAL_IP);
	SetDlgItemText(IDC_STATIC_MNG_ACTIVE_LOCAL_PORT, GetTextByLan(_T("本地公网端口"), _T("Local Wan Port")));
	SetDlgItemTextEx(IDC_STATIC_MNG_ACT_DSM_IP, IDS_MNG_DSM_IP);
	SetDlgItemTextEx(IDC_STATIC_MNG_ACT_DSM_PORT, IDS_MNG_DSM_PORT);
	SetDlgItemTextEx(IDC_STATIC_MNG_ACT_BAK_DSM_IP, IDS_MNG_BAK_DSM_IP);
	SetDlgItemTextEx(IDC_STATIC_MNG_ACT_DSM_BAK_PORT,IDS_MNG_BAK_DSM_PORT);
	SetDlgItemTextEx(IDC_BUTTONMNG_ACTIVE_SET, IDS_SET);
	SetDlgItemTextEx(IDC_BUTTON_REFRESH_REGISTER_LIST, IDS_MNG_ADMIN_REFRESH);
	SetDlgItemText(IDC_STATIC_ACTIVE_LISTEN_PORT, GetTextByLan(_T("本地参数配置"), _T("Local Para Config")));
	SetDlgItemTextEx(IDC_STATIC_ACTIVE_USER_MSG, IDS_MNG_ACTIVE_USR_MSG);
	SetDlgItemTextEx(IDC_CHECK_REG_SERVER, IDS_MNG_ACTIVE_REGISTER);
	SetDlgItemText(IDC_STATIC_CFG_ACTIVE_PARA, GetTextByLan(_T("目录服务器参数配置"), _T("Register Para Config")));
	SetDlgItemTextEx(IDC_STATIC_ACCOUNT_NAME, IDS_MNG_ACTIVE_DSM_NAME);
	SetDlgItemTextEx(IDC_STATIC_ACCOUNT_PWD, IDS_MNG_ACTIVE_DSM_PWD);
	SetDlgItemTextEx(IDC_STATIC_ACTIVE_NVS_TOTAL_COUNT, IDS_MNG_ACTIVE_ONLINE_COUNT);
	SetDlgItemTextEx(IDC_BUTTON_DIRECTORY_SET, IDS_SET);
	SetDlgItemText(IDC_STATIC_WAN_IPV6, GetTextByLan(_T("本地公网IpV6"), _T("Local Wan IpV6")));
	SetDlgItemText(IDC_STATIC_REGIPV6, GetTextByLan(_T("目录服务器IpV6"), _T("DSM IpV6")));

	InsertColumn(m_lstNvsLst, COLUMN_FACTORYID, IDS_MNG_ADMIN_FACTORY_ID, LVCFMT_LEFT, 180);
	InsertColumn(m_lstNvsLst, COLUMN_STATE, IDS_CONFIG_WIFI_STATE, LVCFMT_CENTER, 80);
	InsertColumn(m_lstNvsLst, COLUMN_LANIP, IDS_MNG_DNS_LAN_IP, LVCFMT_CENTER, 220);
	InsertColumn(m_lstNvsLst, COLUMN_WANIP, IDS_MNG_DNS_WAN_IP, LVCFMT_CENTER, 220);
	InsertColumn(m_lstNvsLst, COLUMN_IPVERSION, GetTextByLan(_T("Ip版本"), _T("IpVersion")), LVCFMT_CENTER, 100);
	m_edtUsrMsg.SetWindowText(CONST_cstrUsrMsg_CH);
}

void CLS_ActivePage::OnBnClickedCheckRegServer()
{
	if (BST_CHECKED == m_chkRegServer.GetCheck())
	{
		GetDlgItem(IDC_STATIC_CFG_ACTIVE_PARA)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_MNG_ACT_DSM_IP)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_MNG_ACT_DSM_IP)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_MNG_ACT_DSM_PORT)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_MNG_ACT_DSM_PORT)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_ACCOUNT_NAME)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_ACCOUNT_NAME)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_ACCOUNT_PWD)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_ACCOUNT_PWD)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_BUTTON_DIRECTORY_SET)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_REGIPV6)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_REGIPV6)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_CHECK_ACTIVE_WITH_REG_IPV6)->ShowWindow(SW_SHOW);
	} 
	else
	{
		GetDlgItem(IDC_STATIC_CFG_ACTIVE_PARA)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_MNG_ACT_DSM_IP)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_MNG_ACT_DSM_IP)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_MNG_ACT_DSM_PORT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_MNG_ACT_DSM_PORT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_ACCOUNT_NAME)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_ACCOUNT_NAME)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_ACCOUNT_PWD)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_ACCOUNT_PWD)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_BUTTON_DIRECTORY_SET)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_REGIPV6)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_REGIPV6)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_CHECK_ACTIVE_WITH_REG_IPV6)->ShowWindow(SW_HIDE);
	}
}

void CLS_ActivePage::OnBnClickedButtonListenPortSet()
{
	int iActivePort = GetDlgItemInt(IDC_EDIT_ACTIVE_LISTEN_PORT);
	int iRet = NetClient_SetPort( iActivePort, iActivePort);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetPort (%d, %d)", iActivePort, iActivePort);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetPort (%d, %d)", iActivePort, iActivePort);
	}

	CString cstrLocalIp;
	CString cstrLocalWanIpV6;
	int iLocalPort = 0;
	GetDlgItemText(IDC_EDIT_MNG_ACTIVE_LOCA_IP, cstrLocalIp);
	GetDlgItemText(IDC_EDIT_WAN_IPV6, cstrLocalWanIpV6);
	iLocalPort = GetDlgItemInt(IDC_EDIT_MNG_ACTIVE_LOCAL_PORT);
	ActiveNetWanInfo tLocal = {0};
	tLocal.iSize = sizeof(ActiveNetWanInfo);
	strcpy_s(tLocal.cWanIP, sizeof(tLocal.cWanIP), cstrLocalIp.GetBuffer());
	tLocal.iWanPort = iLocalPort;
	strcpy_s(tLocal.cWanIpV6, sizeof(tLocal.cWanIpV6), cstrLocalWanIpV6.GetBuffer());
	iRet = NetClient_SetDsmConfig(DSM_CMD_SET_NET_WAN_INFO, &tLocal, sizeof(ActiveNetWanInfo));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDsmConfig::DSM_CMD_SET_NET_WAN_INFO");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDsmConfig::DSM_CMD_SET_NET_WAN_INFO");
	}

#ifdef REGISTER_CALLBACK
	ActiveNvsNotify tActiveNotify = {0};
	tActiveNotify.iSize = sizeof(ActiveNvsNotify);
	tActiveNotify.pCbk = DsmNvsRegisterNotify;
	tActiveNotify.pUser = this;
	iRet = NetClient_SetDsmConfig(DSM_CMD_SET_NVSREG_CALLBACK, &tActiveNotify, sizeof(ActiveNvsNotify));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDsmConfig::DSM_CMD_SET_NVSREG_CALLBACK");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDsmConfig::DSM_CMD_SET_NVSREG_CALLBACK");
	}
#endif

	if (NULL != NetClient_ProxyStart)
	{
		NetClient_ProxyStart(iActivePort,1);
	}

	if (NULL != NetClient_ProxySetConfig)
	{
		ProxyBindIp tProxyBindIp = {0};
		strcpy_s(tProxyBindIp.cBindIpV4, sizeof(tProxyBindIp.cBindIpV4), cstrLocalIp.GetBuffer());
		strcpy_s(tProxyBindIp.cBindIpV6, sizeof(tProxyBindIp.cBindIpV6), cstrLocalWanIpV6.GetBuffer());
		NetClient_ProxySetConfig(CMD_PROXYCMD_SET_BINDIP, &tProxyBindIp, sizeof(ProxyBindIp));
	}

	WriteConfig();
}

void CLS_ActivePage::OnBnClickedButtonDirectorySet()
{
	CString cstrDsmIp;
	int iDsmPort = 0;
	CString cstrDsmIpV6;
	GetDlgItemText(IDC_EDIT_MNG_ACT_DSM_IP, cstrDsmIp);
	GetDlgItemText(IDC_EDIT_REGIPV6, cstrDsmIpV6);
	iDsmPort = GetDlgItemInt(IDC_EDIT_MNG_ACT_DSM_PORT);
	CString cstrAccountName;
	CString cstrAccountPwd;
	GetDlgItemText(IDC_EDIT_ACCOUNT_NAME, cstrAccountName);
	GetDlgItemText(IDC_EDIT_ACCOUNT_PWD, cstrAccountPwd);
	ActiveDirectoryInfo tDirectory = {0};
	tDirectory.iSize = sizeof(ActiveNetWanInfo);
	tDirectory.iIpVer = BST_CHECKED == m_chkIpV6WithReg.GetCheck() ? IP_VERSION_6 : IP_VERSION_4;
	strcpy_s(tDirectory.cDsmIP, sizeof(tDirectory.cDsmIP), cstrDsmIp.GetBuffer());
	strcpy_s(tDirectory.cDsmIpV6, sizeof(tDirectory.cDsmIpV6), cstrDsmIpV6.GetBuffer());
	tDirectory.iDsmPort = iDsmPort;
	strcpy_s(tDirectory.cAccontName, sizeof(tDirectory.cAccontName), cstrAccountName.GetBuffer());
	strcpy_s(tDirectory.cAccontPwd, sizeof(tDirectory.cAccontPwd), cstrAccountPwd.GetBuffer());
	int iRet = NetClient_SetDsmConfig(DSM_CMD_SET_DIRECTORY_INFO, &tDirectory, sizeof(ActiveDirectoryInfo));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "OnBnClickedButtonDirectorySet:NetClient_SetDsmConfig::DSM_CMD_SET_DIRECTORY_INFO");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "OnBnClickedButtonDirectorySet:NetClient_SetDsmConfig::DSM_CMD_SET_DIRECTORY_INFO");
	}

	if (NULL != NetClient_ProxySetConfig)
	{
		ProxyDsmCfg tProxyDsmCfg = {0};
		tProxyDsmCfg.iIpVer = BST_CHECKED == m_chkIpV6WithReg.GetCheck() ? IP_VERSION_6 : IP_VERSION_4;
		strcpy_s(tProxyDsmCfg.cDsIp1V4, sizeof(tProxyDsmCfg.cDsIp1V4), cstrDsmIp.GetBuffer());
		strcpy_s(tProxyDsmCfg.cDsIp1V6, sizeof(tProxyDsmCfg.cDsIp1V6), cstrDsmIpV6.GetBuffer());
		tProxyDsmCfg.usPort1 = iDsmPort;
		strcpy_s(tProxyDsmCfg.cDsIp2V4, sizeof(tProxyDsmCfg.cDsIp2V4), cstrDsmIp.GetBuffer());
		strcpy_s(tProxyDsmCfg.cDsIp2V6, sizeof(tProxyDsmCfg.cDsIp2V6), cstrDsmIpV6.GetBuffer());
		tProxyDsmCfg.usPort2 = iDsmPort;
		strcpy_s(tProxyDsmCfg.cAccount, sizeof(tProxyDsmCfg.cAccount), cstrAccountName.GetBuffer());
		strcpy_s(tProxyDsmCfg.cPassword, sizeof(tProxyDsmCfg.cPassword), cstrAccountPwd.GetBuffer());
		NetClient_ProxySetConfig(CMD_PROXYCMD_SET_DSMCFG, &tProxyDsmCfg, sizeof(ProxyDsmCfg));
	}

	WriteConfig();
}

void CLS_ActivePage::OnBnClickedButtonRefreshRegisterList()
{
	if (BST_UNCHECKED == m_chkRegServer.GetCheck())
	{
		UpdateDevLstByLocalServer();
	}
	else
	{
		UpdateDevLstByRegisterServer();
	}
}

void CLS_ActivePage::OnNMDblclkListActiveRegisterList(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);

	if (0 == m_lstNvsLst.GetItemCount())
	{
		return;
	}

	DEVICE_INFO tDevInit = {0};
	m_lstNvsLst.GetItemText(pNMItemActivate->iItem, 0, tDevInit.cID, LEN_32);
	strcpy_s(tDevInit.cUserName,sizeof(tDevInit.cUserName),"Admin");
	strcpy_s(tDevInit.cPassword,sizeof(tDevInit.cPassword),"1111");

	CLS_LogonActive clsLogonActive(&tDevInit);
	if(IDOK != clsLogonActive.DoModal())
	{
		return;
	}

	int iOldLogonID = -1;
	PDEVICE_INFO ptDevLogon = clsLogonActive.GetDeviceInfo();
	PDEVICE_INFO ptDevFind = FindDevice(ptDevLogon->cIP,ptDevLogon->iPort, ptDevLogon->cProxy, ptDevLogon->cID, &iOldLogonID);
	if (NULL != ptDevFind)
	{
		if(LOGON_SUCCESS == NetClient_GetLogonStatus(iOldLogonID))
		{
			AddLog(LOG_TYPE_MSG,"","FindDevice(%s,%s) device already exist!"
				,ptDevFind->cIP, ptDevFind->cID);
			return;
		}
	}

	int iServerType = BST_UNCHECKED == m_chkRegServer.GetCheck() ? SERVER_ACTIVE : SERVER_REG_ACTIVE;
	int iLogonID = -1;
	LogonActiveServer tLogonPara = {0};
	tLogonPara.iSize = sizeof(LogonActiveServer);
	strcpy_s(tLogonPara.cUserName, sizeof(tLogonPara.cUserName), ptDevLogon->cUserName);
	strcpy_s(tLogonPara.cUserPwd, sizeof(tLogonPara.cUserPwd), ptDevLogon->cPassword);
	strcpy_s(tLogonPara.cProductID, sizeof(tLogonPara.cProductID), ptDevLogon->cID);
	if (SYNC_MODE == g_iSdkUseMode)
	{
		//Synchronous blocking login device
		iLogonID = NetClient_SyncLogon(iServerType, &tLogonPara, sizeof(LogonActiveServer));
	}
	else
	{

		iLogonID = NetClient_Logon_V4(iServerType, &tLogonPara, sizeof(LogonActiveServer));
	}
	if (iLogonID >= 0)
	{
		AddLog(LOG_TYPE_SUCC,ptDevLogon->cIP,"(%d)NetClient_Logon_V4",iLogonID);
		if (NULL != ptDevFind)
		{
			ptDevFind->iServerType = iServerType;
			return;
		}
		PDEVICE_INFO ptDevNew = AddDevice(iLogonID);
		if (NULL != ptDevNew)
		{
			memcpy_s(ptDevNew, sizeof(DEVICE_INFO), ptDevLogon, sizeof(DEVICE_INFO));
			ptDevNew->hItem = NULL;
			ptDevNew->uiInterTalkID  = -1;
			ptDevNew->iServerType = iServerType;
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","AddDevice(%d) Can not add device", iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,ptDevLogon->cIP,"NetClient_Logon_V4(%, %s)", iLogonID, ptDevLogon->cID);
	}

	*pResult = 0;
}

void CLS_ActivePage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
}

void CLS_ActivePage::OnDestroy()
{
	CLS_BasePage::OnDestroy();
	//if (m_iRegID >= 0)
	//{
	//	NSLook_LogoffServer(m_iRegID);
	//}
}

void CLS_ActivePage::UpdateDevLstByRegisterServer()
{
	m_lstNvsLst.DeleteAllItems();
	int iCount = 0;
	int iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_DEVCOUNT_WITHREG, &iCount, sizeof(int));
	if(iCount > 0)
	{
		ActiveRegDevListNotify tRegDevListNotify = {0};
		tRegDevListNotify.iSize = sizeof(ActiveRegDevListNotify);
		tRegDevListNotify.pCbkEx = &CLS_ActivePage::NvsLstNotify;
		tRegDevListNotify.pvUser = this;
		int iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_DEVLIST_WITHREG, &tRegDevListNotify, sizeof(ActiveRegDevListNotify));
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_DEVLIST_WITHREG fail!iRet=%d", iRet);
		}
		else
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_DEVLIST_WITHREG succ!");
		}
	}

	SetDlgItemInt(IDC_EDIT_ACTIVE_NVS_TOTAL_COUNT, iCount);
}

int CLS_ActivePage::NvsLstNotify(int _iTotalCount, int _iCurrentCount, void* _pvNvsList, int _iTotalSize, int _iSingleSize, void* _pvUsrData)
{
	__try
	{
		if (NULL != _pvNvsList)
		{
			LRESULT lRet = SendMessageTimeout(s_hWnd, MSG_UPDATE_NVSLST, (WPARAM)_pvNvsList, (LPARAM)_iCurrentCount, SMTO_NORMAL, 3000, NULL);
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{
		//AddLog(LOG_TYPE_MSG,"","NvsLstNotify exception");
	}

	return 0;
}

LRESULT CLS_ActivePage::OnNvsLstMsg(WPARAM wParam, LPARAM lParam)
{
	__try
	{
		DsmNvsRegInfoEx* ptNvs = (DsmNvsRegInfoEx*)wParam;
		int iCount = (int)lParam;
		for (int i = 0 ; i < iCount; ++i)
		{
			AddOneNvsItem(ptNvs + i);
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{
		AddLog(LOG_TYPE_MSG,"","AddOneNvsItem exception");
	}

	return 0;
}

void CLS_ActivePage::AddOneNvsItem(DsmNvsRegInfoEx* _ptNvs)
{
	int iItem = -1;
	LVFINDINFO info;
	info.flags = LVFI_STRING;
	info.psz = _ptNvs->cFactoryID;
	while ((iItem = m_lstNvsLst.FindItem(&info, iItem)) != -1)
	{
		CString strID = m_lstNvsLst.GetItemText(iItem, 0);
		if (0 == strID.CompareNoCase(_ptNvs->cFactoryID))
		{
			return;
		}
	}

	iItem = m_lstNvsLst.GetItemCount();
	m_lstNvsLst.InsertItem(iItem, _ptNvs->cFactoryID);
	m_lstNvsLst.SetItemText(iItem, COLUMN_STATE, GetTextEx(IDS_CONFIG_WIFI_ONLINE));
	if (IP_VERSION_6 == _ptNvs->iIpVer)
	{
		m_lstNvsLst.SetItemText(iItem, COLUMN_LANIP, _ptNvs->cNvsIpV6);
		m_lstNvsLst.SetItemText(iItem, COLUMN_WANIP, _ptNvs->cWanIpV6);
		m_lstNvsLst.SetItemText(iItem, COLUMN_IPVERSION, "IpV6");
	}
	else
	{
		m_lstNvsLst.SetItemText(iItem, COLUMN_LANIP, _ptNvs->cNvsIP);
		m_lstNvsLst.SetItemText(iItem, COLUMN_WANIP, _ptNvs->cWanIp);
		m_lstNvsLst.SetItemText(iItem, COLUMN_IPVERSION, "IpV4");
	}
}

void CLS_ActivePage::UpdateDevLstByLocalServer()
{
	m_lstNvsLst.DeleteAllItems();

	char* pcDevList = NULL;
	int iRegisterCount = 0;
	NetClient_GetDsmRegstierInfo(DSM_CMD_GET_REGISTER_COUNT, &iRegisterCount, sizeof(int));
	if (iRegisterCount <= 0)
	{
		AddLog(LOG_TYPE_MSG, "", "iRegisterCount(%d) <= 0", iRegisterCount);
		goto END;
	}

	pcDevList = new char[iRegisterCount * LEN_32];
	if (NULL == pcDevList)
	{
		goto END;
	}
	memset(pcDevList, 0, iRegisterCount * LEN_32);
	NetClient_GetDsmRegstierInfo(DSM_CMD_GET_REGISTER_DEVLIST, pcDevList, iRegisterCount * LEN_32);

	for (int iItem = 0; iItem < iRegisterCount; ++iItem)
	{
		char cFactoryID[LEN_32] = {0};
		strncpy_s(cFactoryID, LEN_32, pcDevList + iItem * LEN_32, LEN_32);
		m_lstNvsLst.InsertItem(iItem, cFactoryID);

		DsmOnline tOnline = {0};
		tOnline.iSize = sizeof(DsmOnline);
		strncpy_s(tOnline.cProductID, LEN_32, cFactoryID, LEN_32);
		NetClient_GetDsmRegstierInfo(DSM_CMD_GET_ONLINE_STATE, &tOnline, sizeof(DsmOnline));
		if (DSM_STATE_ONLINE == tOnline.iOnline)
		{
			m_lstNvsLst.SetItemText(iItem, COLUMN_STATE, GetTextEx(IDS_CONFIG_WIFI_ONLINE));
		}
		else
		{
			m_lstNvsLst.SetItemText(iItem, COLUMN_STATE, GetTextEx(IDS_CONFIG_WIFI_OFFLINE));
		}

		if (IP_VERSION_6 == tOnline.iIpVer)
		{
			m_lstNvsLst.SetItemText(iItem, COLUMN_LANIP, tOnline.cLanIpV6);
			m_lstNvsLst.SetItemText(iItem, COLUMN_WANIP, tOnline.cWanIpV6);
			m_lstNvsLst.SetItemText(iItem, COLUMN_IPVERSION, "IpV6");
		}
		else
		{
			m_lstNvsLst.SetItemText(iItem, COLUMN_LANIP, tOnline.cLanIP);
			m_lstNvsLst.SetItemText(iItem, COLUMN_WANIP, tOnline.cWanIP);
			m_lstNvsLst.SetItemText(iItem, COLUMN_IPVERSION, "IpV4");
		}
	}

END:
	if (NULL != pcDevList)
	{
		delete []pcDevList;
		pcDevList = NULL;
	}
	SetDlgItemInt(IDC_EDIT_ACTIVE_NVS_TOTAL_COUNT, iRegisterCount);
}

#ifdef REGISTER_CALLBACK
void CLS_ActivePage::DsmNvsRegisterNotify(DsmNvsRegInfo* _pNvsInfo, int _iSize, void* _pUser)
{
	if (NULL == _pNvsInfo)
	{
		return;
	}

	DsmNvsRegInfo tRegInfo = {0};
	int iCopySize = _iSize > sizeof(DsmNvsRegInfo) ? sizeof(DsmNvsRegInfo) : _iSize;
	memcpy(&tRegInfo, _pNvsInfo, iCopySize);
	AddLog(LOG_TYPE_MSG,"", "ID=%s, LanIp=%s, WanIp=%s.", tRegInfo.cFactoryID, tRegInfo.cNvsIP, tRegInfo.cWanIp);

	LogonActiveServer tLogonPara = {0};
	tLogonPara.iSize = sizeof(LogonActiveServer);
	strcpy_s(tLogonPara.cUserName, sizeof(tLogonPara.cUserName), "admin");
	strcpy_s(tLogonPara.cUserPwd, sizeof(tLogonPara.cUserPwd), "1111");
	strcpy_s(tLogonPara.cProductID, sizeof(tLogonPara.cProductID), tRegInfo.cFactoryID);
	int iLogonID = NetClient_Logon_V4(SERVER_ACTIVE, &tLogonPara, sizeof(LogonActiveServer));
	if (iLogonID >= 0)
	{
		AddLog(LOG_TYPE_SUCC,tRegInfo.cNvsIP,"(%d)NetClient_Logon_V4",iLogonID);
		PDEVICE_INFO ptDevNew = AddDevice(iLogonID);
		if (NULL != ptDevNew)
		{
			ptDevNew->hItem = NULL;
			ptDevNew->uiInterTalkID  = -1;
		}
		else
		{
			AddLog(LOG_TYPE_MSG,"","AddDevice(%d) Can not add device", iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,tRegInfo.cNvsIP,"NetClient_Logon_V4(%s)",tRegInfo.cFactoryID);
	}
}
#endif


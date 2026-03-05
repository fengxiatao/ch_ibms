// DNVRDigitPage.cpp : implementation file
//

#include "stdafx.h"
#include "DNVRDigitPage.h"


// CLS_DNVRDigitPage dialog
#define MSG_SEARCH_DEVICE		(WM_USER + 1117)
#define NETMODE_MUC				3

IMPLEMENT_DYNAMIC(CLS_DNVRDigitPage, CDialog)

CLS_DNVRDigitPage::CLS_DNVRDigitPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DNVRDigitPage::IDD, pParent)
	, m_IsNvssIpv6(false)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
}

CLS_DNVRDigitPage::~CLS_DNVRDigitPage()
{
}

void CLS_DNVRDigitPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_DIGIT_ENABLE, m_chkDigitEnable);
	DDX_Control(pDX, IDC_COMBO_CONNECTMODE, m_cboConnectMode);
	DDX_Control(pDX, IDC_EDIT_IPIDDDNS, m_edtIPIDDDNS);
	DDX_Control(pDX, IDC_EDIT_PROXYIP, m_edtProxyIP);
	DDX_Control(pDX, IDC_COMBO_DEVICECHAN, m_cboDeviceChan);
	DDX_Control(pDX, IDC_EDIT_DEVICEPORT, m_edtDevicePort);
	DDX_Control(pDX, IDC_COMBO_STREAMTYPE, m_cboStreamType);
	DDX_Control(pDX, IDC_COMBO_NETMODE, m_cboNetMode);
	DDX_Control(pDX, IDC_COMBO_PTZPROTOCOL, m_cboPTZProtocol);
	DDX_Control(pDX, IDC_EDIT_PTZADDR, m_edtPTZAddr);
	DDX_Control(pDX, IDC_EDIT_USERNAME, m_edtUserName);
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_edtPassword);
	DDX_Control(pDX, IDC_EDIT_ENCRYPTKEY, m_edtEncryptKey);
	DDX_Control(pDX, IDC_COMBO_SERVERTYPE, m_cboServerType);
	DDX_Control(pDX, IDC_COMBO_IPCPNP, m_cboIPCPnP);
	DDX_Control(pDX, IDC_BUTTON_DIGIT, m_btnDigit);
	DDX_Control(pDX, IDC_EDIT_COMSEND, m_edtComSend);
	DDX_Control(pDX, IDC_BUTTON_COMSEND, m_btnComSend);
	DDX_Control(pDX, IDC_LIST_DEVICE_INFO, m_lstDeviceCtrl);
	DDX_Control(pDX, IDC_EDIT_IPC_MAC, m_edtIpcMac);
	DDX_Control(pDX, IDC_COMBO_ACTIVATION, m_combo_activation);
	DDX_Control(pDX, IDC_COMBO_SYNCHRO, m_combo_synchro);
	DDX_Control(pDX, IDC_EDIT_IPIDDDNSIPV6, m_Edit_IpDdnsDsmIpv6);
	DDX_Control(pDX, IDC_EDIT_PROXYIPV6, m_Edit_ProxyIpv6);
	DDX_Control(pDX, IDC_EDIT_MUCIP, m_edit_mcuip);
	DDX_Control(pDX, IDC_EDIT_MUCPORT, m_edit_mcuport);
}


BEGIN_MESSAGE_MAP(CLS_DNVRDigitPage, CLS_BasePage)
	ON_CBN_SELCHANGE(IDC_COMBO_IPCPNP, &CLS_DNVRDigitPage::OnCbnSelchangeComboIpcpnp)
	ON_BN_CLICKED(IDC_BUTTON_DIGIT, &CLS_DNVRDigitPage::OnBnClickedButtonDigit)
	ON_BN_CLICKED(IDC_BUTTON_COMSEND, &CLS_DNVRDigitPage::OnBnClickedButtonComsend)
	ON_BN_CLICKED(IDC_BUTTON_SEARCH_DEVICE, &CLS_DNVRDigitPage::OnBnClickedButtonSearchDevice)
	ON_BN_CLICKED(IDC_BUTTON_STOP_SEARCH, &CLS_DNVRDigitPage::OnBnClickedButtonStopSearch)
	ON_MESSAGE(MSG_SEARCH_DEVICE,&CLS_DNVRDigitPage::OnSearchMsg)
	ON_NOTIFY(NM_CLICK, IDC_LIST_DEVICE_INFO, &CLS_DNVRDigitPage::OnNMClickListDeviceInfo)
	ON_BN_CLICKED(IDC_BUTTON_CHANGE, &CLS_DNVRDigitPage::OnBnClickedButtonChange)
	ON_CBN_SELCHANGE(IDC_COMBO_CONNECTMODE, &CLS_DNVRDigitPage::OnCbnSelchangeComboConnectmode)
	ON_CBN_SELCHANGE(IDC_COMBO_NETMODE, &CLS_DNVRDigitPage::OnCbnSelchangeComboNetmode)
	ON_BN_CLICKED(IDC_NVSS_IPV6, &CLS_DNVRDigitPage::OnBnClickedNvssIpv6)
	ON_BN_CLICKED(IDC_NVSS_IPV4, &CLS_DNVRDigitPage::OnBnClickedNvssIpv4)
END_MESSAGE_MAP()


// CLS_DNVRDigitPage message handlers
BOOL CLS_DNVRDigitPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();


	m_cboConnectMode.AddString("IPv4");
	m_cboConnectMode.AddString("DDNS");
	m_cboConnectMode.AddString("DSM");
	m_cboConnectMode.AddString("IPv6");
    m_cboConnectMode.AddString("EasyDDNS");

	m_cboStreamType.AddString("Main");
	m_cboStreamType.AddString("Sub");

	m_cboNetMode.AddString("TCP");
	m_cboNetMode.AddString("UDP");
	m_cboNetMode.AddString("MUC");

	m_edtIPIDDDNS.LimitText(32);
	m_edtProxyIP.LimitText(32);
	m_edtDevicePort.LimitText(5);
	m_cboStreamType.SetCurSel(0);
	m_cboNetMode.SetCurSel(0);
	m_edtPTZAddr.LimitText(5);
	m_edtUserName.LimitText(16);
	m_edtPassword.LimitText(16);
	m_edtEncryptKey.LimitText(16);
	m_edtIpcMac.SetLimitText(32);

	m_lstDeviceCtrl.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	s_hWnd = this->GetSafeHwnd();

	SetDlgItemText(IDC_EDIT_COMSEND, "00,01,02,03,04,05,06,07");

	m_combo_activation.SetItemData(m_combo_activation.AddString(_T("inactive")),0);
	m_combo_activation.SetItemData(m_combo_activation.AddString(_T("Activated")),1);
	m_combo_activation.SetCurSel(0);

	m_combo_synchro.SetItemData(m_combo_synchro.AddString(_T("not synchronized")),0);
	m_combo_synchro.SetItemData(m_combo_synchro.AddString(_T("synchronization")),1);
	m_combo_synchro.SetCurSel(0);

	m_Edit_IpDdnsDsmIpv6.EnableWindow(FALSE);
	m_Edit_ProxyIpv6.EnableWindow(FALSE);
	UI_UpdateDialog();
	
	return TRUE;
}

void CLS_DNVRDigitPage::OnBnClickedButtonSearchDevice()
{
	m_lstDeviceCtrl.DeleteAllItems();
	SetSeekNVSS stSetSeekNVSS = {0};
	stSetSeekNVSS.iSize = sizeof(SetSeekNVSS);
	stSetSeekNVSS.iSeekType = SEEK_TYPE_START;
	//stSetSeekNVSS.iSeekPara = SEEK_PARA_IP;
	stSetSeekNVSS.iSeekPara = m_IsNvssIpv6?SEEK_PARA_IPV6:SEEK_PARA_IP;
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_SEEK_NVSS, m_iChannelNO, &stSetSeekNVSS, sizeof(stSetSeekNVSS));
	if( 0 == iRet )
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand->[COMMAND_ID_SEEK_NVSS][SEEK_TYPE_START](%d,%d)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SendCommand->[COMMAND_ID_SEEK_NVSS][SEEK_TYPE_START] (%d,%d)",m_iLogonID,m_iChannelNo);
	}
}


void CLS_DNVRDigitPage::OnBnClickedButtonStopSearch()
{
	SetSeekNVSS stSetSeekNVSS = {0};
	stSetSeekNVSS.iSize = sizeof(SetSeekNVSS);
	stSetSeekNVSS.iSeekType = SEEK_TYPE_STOP;
	//stSetSeekNVSS.iSeekPara = SEEK_PARA_IP;
	stSetSeekNVSS.iSeekPara = m_IsNvssIpv6?SEEK_PARA_IPV6:SEEK_PARA_IP;
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_SEEK_NVSS, m_iChannelNO, &stSetSeekNVSS, sizeof(stSetSeekNVSS));
	if( 0 == iRet )
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand->[COMMAND_ID_SEEK_NVSS][SEEK_TYPE_STOP] (%d,%d)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SendCommand->[COMMAND_ID_SEEK_NVSS][SEEK_TYPE_STOP] (%d,%d)",m_iLogonID,m_iChannelNo);
	}

}

void CLS_DNVRDigitPage::OnMainNotify( int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	int iMsgType = LOWORD(_wParam);
	switch(iMsgType)
	{
	case WCM_SEEK_NVSS:
		{
			__try
			{
				if (NULL != _iLParam)
				{
					LRESULT lRet = SendMessageTimeout(s_hWnd, MSG_SEARCH_DEVICE, (WPARAM)_iLParam, 0, SMTO_NORMAL, 3000, NULL);
				}
			}
			__except(EXCEPTION_EXECUTE_HANDLER)
			{
				//AddLog(LOG_TYPE_MSG,"","OnMainNotify exception");
			}
		}
		break;
	default:
		break;
	}
}

LRESULT CLS_DNVRDigitPage::OnSearchMsg( WPARAM wParam, LPARAM lParam )
{
	GetSeekNVSS* pGetSeekNVSS = (GetSeekNVSS*)wParam;
	if( NULL != pGetSeekNVSS )
	{
		int iItem = m_lstDeviceCtrl.GetItemCount();
		m_lstDeviceCtrl.InsertItem(iItem, pGetSeekNVSS->cIp);
		m_lstDeviceCtrl.SetItemText(iItem,1,IntToString(pGetSeekNVSS->iServerPort));
		m_lstDeviceCtrl.SetItemText(iItem,2,IntToString(pGetSeekNVSS->iChanNum));
		m_lstDeviceCtrl.SetItemText(iItem,3,pGetSeekNVSS->cMac);
		m_lstDeviceCtrl.SetItemText(iItem,4,pGetSeekNVSS->cNewFactoryID);
		m_lstDeviceCtrl.SetItemText(iItem,5,pGetSeekNVSS->pcLocalLinkIPv6);
		m_lstDeviceCtrl.SetItemText(iItem,6,pGetSeekNVSS->pcIPv6);
		m_lstDeviceCtrl.SetItemText(iItem,7,IntToString(pGetSeekNVSS->iPrefixLenv6));
		m_lstDeviceCtrl.SetItemText(iItem,8,pGetSeekNVSS->pcGateWayv6);
		m_lstDeviceCtrl.SetItemText(iItem,9,pGetSeekNVSS->pcDNSv6);
	}
	return 0;
}

void CLS_DNVRDigitPage::OnNMClickListDeviceInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	if( NULL != pNMItemActivate) 
	{
		int nItem = pNMItemActivate->iItem;		//Row Number
		CString strIp = m_lstDeviceCtrl.GetItemText(nItem,0);
		CString strPort = m_lstDeviceCtrl.GetItemText(nItem,1);
		if( strIp.IsEmpty() || strPort.IsEmpty())
		{
			return;
		}
		SetDlgItemText(IDC_EDIT_IPIDDDNS, strIp);
		SetDlgItemText(IDC_EDIT_DEVICEPORT, strPort);
	}
	
	*pResult = 0;
}

void CLS_DNVRDigitPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_DNVRDigitPage::OnParamChangeNotify]->Invalid logon id(%d)", _iLogonID);
		return;
	}

	if (_iChannelNo == m_iChannelNo)
	{
		switch(_iParaType)
		{
		case PARA_DIGITALCHANNEL:
			{
				AddLog(LOG_TYPE_SUCC,"","[CLS_DNVRDigitPage::OnParamChangeNotify]->PARA_DIGITALCHANNEL (%d,%d)",m_iLogonID,m_iChannelNo);
				UI_UpdateDigit();
			}
			break;
		default:
			break;
		}
	}
}

void CLS_DNVRDigitPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}

	if (_iStreamNo < 0)
	{
		m_iStreamNO = 0;
	}
	else
	{
		m_iStreamNO = _iStreamNo;
	}

	UI_UpdateDigit();
	UI_UpdateIPCPnp();
}

void CLS_DNVRDigitPage::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_DNVRDigitPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_CHECK_DIGIT_ENABLE,IDS_CONFIG_FTP_SNAPSHOT_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_CONNECTMODE,IDS_CONFIG_DNVR_DIGIT_CONNECTMODE);
	SetDlgItemTextEx(IDC_STATIC_IPIDDDNS,IDS_CONFIG_DNVR_DIGIT_IPIDDDNS);
	SetDlgItemTextEx(IDC_STATIC_PROXYIP,IDS_CONFIG_DNVR_DIGIT_PROXYIP);
	SetDlgItemTextEx(IDC_STATIC_DEVICECHAN,IDS_CONFIG_DNVR_DIGIT_DEVICECHAN);
	SetDlgItemTextEx(IDC_STATIC_DEVICEPORT,IDS_CONFIG_DNVR_DIGIT_DEVICEPORT);
	SetDlgItemTextEx(IDC_STATIC_STREAMTYPE,IDS_CONFIG_DNVR_DIGIT_STREAMTYPE);
	SetDlgItemTextEx(IDC_STATIC_NETMODE,IDS_CONFIG_DNVR_DIGIT_NETMODE);
	SetDlgItemTextEx(IDC_STATIC_PTZPROTOCOL,IDS_CONFIG_DNVR_DIGIT_PTZPROTOCOL);
	SetDlgItemTextEx(IDC_STATIC_PTZADDR,IDS_CONFIG_DNVR_DIGIT_PTZADDR);
	SetDlgItemTextEx(IDC_STATIC_USERNAME,IDS_CONFIG_DNVR_DIGIT_USERNAME);
	SetDlgItemTextEx(IDC_STATIC_PASSWORD,IDS_CONFIG_DNVR_DIGIT_PASSWORD);
	SetDlgItemTextEx(IDC_STATIC_ENCRYPTKEY,IDS_CONFIG_DNVR_DIGIT_ENCRYPTKEY);
	SetDlgItemTextEx(IDC_STATIC_SERVERTYPE,IDS_CONFIG_DNVR_DIGIT_SERVERTYPE);
	SetDlgItemTextEx(IDC_STATIC_IPCPNP,IDS_CONFIG_DNVR_DIGIT_IPCPNP);
	SetDlgItemTextEx(IDC_BUTTON_DIGIT,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_COMSEND,IDS_CONFIG_DNVR_DIGIT_COMSEND);
	SetDlgItemTextEx(IDC_BUTTON_COMSEND,IDS_CONFIG_DNVR_DIGIT_SEND);

	SetDlgItemText(IDC_STATIC_DIGIT_CONF, GetTextByLan(_T("数字通道配置"), _T("Digit Config")));
	SetDlgItemText(IDC_BUTTON_SEARCH_DEVICE, GetTextByLan(_T("搜索"), _T("Search")));
	SetDlgItemText(IDC_BUTTON_STOP_SEARCH, GetTextByLan(_T("停止"), _T("Stop")));
	SetDlgItemText(IDC_STATIC_MUCIP, GetTextByLan(_T("多播IP地址"), _T("MUCIP")));
	SetDlgItemText(IDC_STATIC_MUCPORT, GetTextByLan(_T("多播端口号"), _T("MUCPORT")));
	SetDlgItemText(IDC_STATIC_ACTIVATION, GetTextByLan(_T("激活状态"), _T("ACTIVATION")));
	SetDlgItemText(IDC_STATIC_SYNCHRO, GetTextByLan(_T("同步邮件或手机号到前端"), _T("SYNCHRO")));
	
	SetDlgItemText(IDC_STATIC_CHANGE_USER, GetTextByLan(_T("账号："), _T("Username:")));
	SetDlgItemText(IDC_STATIC_CHANGE_PWD, GetTextByLan(_T("密码："), _T("Password:")));
	SetDlgItemText(IDC_STATIC_CHANGE_ADDR, GetTextByLan(_T("地址："), _T("Address:")));
	SetDlgItemText(IDC_STATIC_CHANGE_PREFIX, GetTextByLan(_T("子网："), _T("PrefixLen:")));
	SetDlgItemText(IDC_STATIC_CHANGE_GATEWAY, GetTextByLan(_T("网关："), _T("GateWay")));
	SetDlgItemText(IDC_STATIC_CHANGE_DNS, GetTextByLan(_T("DNS："), _T("DNS:")));

	SetDlgItemText(IDC_EDIT_CHANGE_USER, "Admin");
	SetDlgItemText(IDC_EDIT_CHANGE_PWD, "1111");

	m_lstDeviceCtrl.DeleteAllItems();
	InsertColumn(m_lstDeviceCtrl,0, IDS_MNG_ADMIN_IP, LVCFMT_LEFT,120);
	InsertColumn(m_lstDeviceCtrl,1, IDS_MNG_ADMIN_SERVER_PORT ,LVCFMT_LEFT,90);
	InsertColumn(m_lstDeviceCtrl,2, IDS_MNG_ADMIN_CHANNEL_NUM, LVCFMT_LEFT,80);
	InsertColumn(m_lstDeviceCtrl,3, IDS_MNG_ADMIN_MAC, LVCFMT_LEFT,160);
	InsertColumn(m_lstDeviceCtrl,4, IDS_MNG_ADMIN_FACTORY_ID, LVCFMT_LEFT,200);
	InsertColumn(m_lstDeviceCtrl,5, IDS_MNG_ADMIN_LINKV6, LVCFMT_LEFT,200);
	InsertColumn(m_lstDeviceCtrl,6, IDS_MNG_ADMIN_IPV6, LVCFMT_LEFT,200);
	InsertColumn(m_lstDeviceCtrl,7, IDS_MNG_ADMIN_MASKV6, LVCFMT_LEFT,80);
	InsertColumn(m_lstDeviceCtrl,8, IDS_MNG_ADMIN_GATEWAYV6, LVCFMT_LEFT,200);
	InsertColumn(m_lstDeviceCtrl,9, IDS_MNG_ADMIN_DNSV6, LVCFMT_LEFT,200);

	InsertString(m_cboServerType,0,IDS_CONFIG_DNVR_PRIVATE);
	InsertString(m_cboServerType,1,IDS_CONFIG_DNVR_ONVIF);
	InsertString(m_cboServerType,2,IDS_CONFIG_DNVR_PUSHSTREAM);
	InsertString(m_cboServerType,3,"RTSP");

	InsertString(m_cboIPCPnP,0,IDS_CONFIG_DNVR_DISABLE);
	InsertString(m_cboIPCPnP,1,IDS_CONFIG_DNVR_AUTO);
	InsertString(m_cboIPCPnP,2,IDS_CONFIG_DNVR_NOTICE);

	int iCurSel = m_combo_activation.GetCurSel();
	m_combo_activation.ResetContent();
	m_combo_activation.SetItemData(m_combo_activation.AddString(GetTextByLan(_T("未激活"),_T("Not Active"))), 0);
	m_combo_activation.SetItemData(m_combo_activation.AddString(GetTextByLan(_T("已激活"),_T("Activated"))), 1);
	m_combo_activation.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	iCurSel = m_combo_synchro.GetCurSel();
	m_combo_synchro.ResetContent();
	m_combo_synchro.SetItemData(m_combo_synchro.AddString(GetTextByLan(_T("不同步"),_T("Asynchrony"))), 0);
	m_combo_synchro.SetItemData(m_combo_synchro.AddString(GetTextByLan(_T("同步"),_T("Synchronization"))), 1);
	m_combo_synchro.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
}

BOOL CLS_DNVRDigitPage::UI_UpdateDigit()
{
	if (m_iLogonID < 0)
		return FALSE;

	int iChannelProperty = -1;
	int iRet = NetClient_GetChannelProperty(m_iLogonID, m_iChannelNo, GENERAL_CMD_GET_CHANNEL_TYPE, &iChannelProperty, sizeof(iChannelProperty));
	if (iChannelProperty != 2)
	{
		m_chkDigitEnable.SetCheck(BST_UNCHECKED);
		m_chkDigitEnable.EnableWindow(FALSE);
		m_cboConnectMode.SetCurSel(0);
		SetDlgItemText(IDC_EDIT_IPIDDDNS, "");
		SetDlgItemText(IDC_EDIT_PROXYIP, "");
		SetDlgItemText(IDC_EDIT_DEVICEPORT, "");
		m_cboDeviceChan.ResetContent();
		m_cboStreamType.SetCurSel(0);
		m_cboNetMode.SetCurSel(0);
		SetDlgItemText(IDC_EDIT_PTZADDR, "");
		SetDlgItemText(IDC_EDIT_USERNAME, "");
		SetDlgItemText(IDC_EDIT_PASSWORD, "");
		SetDlgItemText(IDC_EDIT_ENCRYPTKEY, "");
		m_cboServerType.SetCurSel(0);
		for (int i=0; i<32; i++)
		{
			CString strDeviceChan;
			strDeviceChan.Format("%d",i);
			m_cboDeviceChan.AddString(strDeviceChan);
		}
		return FALSE;
	}
	m_cboDeviceChan.ResetContent();
	for (int i=0; i<32; i++)
	{
		CString strDeviceChan;
		strDeviceChan.Format("%d",i);
		m_cboDeviceChan.AddString(strDeviceChan);
	}
	
	int iChanNum = 0;
	NetClient_GetChannelNum(m_iLogonID, &iChanNum);
	int iRealChan = m_iChannelNo+iChanNum*m_iStreamNO;

	TDigitalChannelParam dcp = {0};
	dcp.iChannel = iRealChan;
	iRet = NetClient_GetDigitalChannelConfig(m_iLogonID, iRealChan, DC_CMD_GET_ALL, &dcp, sizeof(dcp));
	if (0 == iRet)
	{
		m_chkDigitEnable.SetCheck(dcp.iEnable?BST_CHECKED:BST_UNCHECKED);
		m_chkDigitEnable.EnableWindow(TRUE);
		m_cboConnectMode.SetCurSel(dcp.iConnectMode);
		if (dcp.iConnectMode == 3)
		{
			m_Edit_IpDdnsDsmIpv6.EnableWindow(TRUE);
			m_Edit_ProxyIpv6.EnableWindow(TRUE);
			m_edtIPIDDDNS.EnableWindow(FALSE);
			m_edtProxyIP.EnableWindow(FALSE);
		}
		else
		{
			m_Edit_IpDdnsDsmIpv6.EnableWindow(FALSE);
			m_Edit_ProxyIpv6.EnableWindow(FALSE);
			m_edtIPIDDDNS.EnableWindow(TRUE);
			m_edtProxyIP.EnableWindow(TRUE);
		}
		if (NETMODE_MUC == dcp.iNetMode)
		{
			m_edit_mcuip.EnableWindow(TRUE);
			m_edit_mcuport.EnableWindow(TRUE);
		}
		else
		{
			m_edit_mcuip.EnableWindow(FALSE);
			m_edit_mcuport.EnableWindow(FALSE);
		}
		SetDlgItemText(IDC_EDIT_IPIDDDNS, dcp.cIP);
		SetDlgItemText(IDC_EDIT_PROXYIP, dcp.cProxyIP);
		CString strDeviceChannel;
		strDeviceChannel.Format("%d",dcp.iDevChannel);
		m_cboDeviceChan.SelectString(-1, strDeviceChannel);
		SetDlgItemInt(IDC_EDIT_DEVICEPORT, dcp.iDevPort);
		m_cboStreamType.SetCurSel(dcp.iStreamType);
		m_cboNetMode.SetCurSel(dcp.iNetMode-1);
		SetDlgItemInt(IDC_EDIT_PTZADDR, dcp.iPtzAddr);
		SetDlgItemText(IDC_EDIT_USERNAME, dcp.cUserName);
		SetDlgItemText(IDC_EDIT_PASSWORD, dcp.cPassword);
		SetDlgItemText(IDC_EDIT_ENCRYPTKEY, dcp.cEncryptKey);
		SetDlgItemText(IDC_EDIT_RTSP_URL, dcp.cRTSPUrlEx);

        if (dcp.iServerType == SERVERTYPE_ONVIF && dcp.iConnectMode == 1)
        {
            SetDlgItemText(IDC_EDIT_RTSP_URL, dcp.cOnvifUrl1);
        }

		m_cboServerType.SetCurSel(dcp.iServerType);
		SetDlgItemText(IDC_EDIT_MUCIP, dcp.cMucIp);
		SetDlgItemInt(IDC_EDIT_MUCPORT, dcp.iMucPort);
		m_combo_activation.SetCurSel(dcp.iActivation);
		m_combo_synchro.SetCurSel(dcp.iSynchro);
		SetDlgItemText(IDC_EDIT_IPC_MAC, dcp.cIpcMac);
		SetDlgItemText(IDC_EDIT_IPIDDDNSIPV6, dcp.cIPv6);
		SetDlgItemText(IDC_EDIT_PROXYIPV6, dcp.cProxyIPv6);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
	st_NVSProtocol protocol;
	m_cboPTZProtocol.ResetContent();
	m_cboPTZProtocol.AddString("PTZ_PELCO_D");
	m_cboPTZProtocol.AddString("PTZ_PELCO_P");
	m_cboPTZProtocol.AddString("PTZ_TC615_P");
	m_cboPTZProtocol.AddString("DOME_PELCO_D");
	m_cboPTZProtocol.AddString("DOME_PELCO_P");
	m_cboPTZProtocol.AddString("DOME_PLUS");
	memset(&protocol,0,sizeof(st_NVSProtocol));
	iRet = NetClient_GetProtocolList(m_iLogonID,&protocol);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetProtocolList(%d,%#x)",m_iLogonID,&protocol);
		int iProtocolIndex = -1;
		for (int i = 0; i < protocol.iCount; i++)
		{
			iProtocolIndex = m_cboPTZProtocol.FindStringExact(-1,protocol.cProtocol[i]);
			if (iProtocolIndex < 0)
			{
				m_cboPTZProtocol.AddString(protocol.cProtocol[i]);
			}
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetProtocolList(%d,%#x)",m_iLogonID,&protocol);
	}
	int iComPort = 0;
	int iDevAddress = 0;
	int iChannelNo = 0;
	char cDeviceType[64]= {0};
	iRet = NetClient_GetDeviceType(m_iLogonID,iChannelNo,&iComPort,&iDevAddress,cDeviceType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDeviceType(%d,%d,%d,%d,%s)",m_iLogonID,iChannelNo,iComPort,iDevAddress,cDeviceType);
		m_cboPTZProtocol.SelectString(-1,cDeviceType);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDeviceType(%d,%d,%d,%d,%s)",m_iLogonID,iChannelNo,iComPort,iDevAddress,cDeviceType);
	}
	return TRUE;
}

void CLS_DNVRDigitPage::OnCbnSelchangeComboIpcpnp()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iIPCPnP = m_cboIPCPnP.GetCurSel();
	int iRet = NetClient_SetDigitalChannelConfig(m_iLogonID,m_iChannelNo,DC_CMD_SET_IPCPnP,&iIPCPnP,sizeof(int));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
}

BOOL CLS_DNVRDigitPage::UI_UpdateIPCPnp()
{
	if (m_iLogonID < 0)
		return FALSE;

	int iIPCPnP = 0;
	if(0 == NetClient_GetDigitalChannelConfig(m_iLogonID,m_iChannelNo,DC_CMD_GET_IPCPnP,&iIPCPnP,sizeof(int)))
	{
		m_cboIPCPnP.SetCurSel(iIPCPnP);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
	return TRUE;
}

void CLS_DNVRDigitPage::OnBnClickedButtonDigit()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	
	int iChanNum = 0;
	NetClient_GetChannelNum(m_iLogonID, &iChanNum);

	int iRealChan = m_iChannelNo+iChanNum*m_iStreamNO;

	TDigitalChannelParam dcp = {0};
	dcp.iChannel = iRealChan;
	dcp.iEnable = (m_chkDigitEnable.GetCheck() == BST_CHECKED)?1:0;
	dcp.iConnectMode = m_cboConnectMode.GetCurSel();
	GetDlgItemText(IDC_EDIT_IPIDDDNS, dcp.cIP, 32);
	GetDlgItemText(IDC_EDIT_PROXYIP, dcp.cProxyIP, 32);
	CString strDeviceChannel;
	GetDlgItemText(IDC_COMBO_DEVICECHAN, strDeviceChannel);
	dcp.iDevChannel = atoi(strDeviceChannel);
	dcp.iDevPort = GetDlgItemInt(IDC_EDIT_DEVICEPORT);
	dcp.iStreamType = m_cboStreamType.GetCurSel();
	dcp.iNetMode = m_cboNetMode.GetCurSel() + 1;
	GetDlgItemText(IDC_COMBO_PTZPROTOCOL, dcp.cPtzProtocolName, 32);
	dcp.iPtzAddr = GetDlgItemInt(IDC_EDIT_PTZADDR);
	GetDlgItemText(IDC_EDIT_USERNAME, dcp.cUserName, 16);
	GetDlgItemText(IDC_EDIT_PASSWORD, dcp.cPassword, 16);
	GetDlgItemText(IDC_EDIT_ENCRYPTKEY, dcp.cEncryptKey, 16);
	GetDlgItemText(IDC_EDIT_MUCIP, dcp.cMucIp, 64);
	dcp.iMucPort = GetDlgItemInt(IDC_EDIT_MUCPORT);
	dcp.iActivation = m_combo_activation.GetItemData(m_combo_activation.GetCurSel());
	dcp.iSynchro = m_combo_synchro.GetItemData(m_combo_synchro.GetCurSel());
	GetDlgItemText(IDC_EDIT_IPIDDDNSIPV6, dcp.cIPv6, 64);
	GetDlgItemText(IDC_EDIT_PROXYIPV6, dcp.cProxyIPv6, 64);
	dcp.iServerType = m_cboServerType.GetCurSel();
	if (dcp.iServerType == SERVERTYPE_RTSP)
	{
		GetDlgItemText(IDC_EDIT_RTSP_URL, dcp.cRTSPUrlEx, 4*MAX_RTSPURL_LEN_EX);
	}
	else
	{
		GetDlgItemText(IDC_EDIT_RTSP_URL, dcp.cRTSPUrlEx, MAX_RTSPURL_LEN);
	}

    if (dcp.iServerType == SERVERTYPE_ONVIF && dcp.iConnectMode == 1)
    {
        GetDlgItemText(IDC_EDIT_RTSP_URL, dcp.cOnvifUrl1, LEN_256);
    }

	int iRet = NetClient_SetDigitalChannelConfig(m_iLogonID, iRealChan, DC_CMD_SET_ALL, &dcp, sizeof(dcp));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDigitalChannelConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
}

void CLS_DNVRDigitPage::OnBnClickedButtonComsend()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	CString strData;
	GetDlgItemText(IDC_EDIT_COMSEND,strData);
	strData.Replace(_T(","),_T(""));
	strData.Replace(_T(" "),_T(""));
	strData.Replace(_T("\r"),_T(""));
	strData.Replace(_T("\n"),_T(""));
	char* pucData = (LPSTR)(LPCTSTR)strData;
	char ucBuf[1024] = {0};
	int iLen = strData.GetLength()/2;
	if (iLen >= 1024)
	{
		iLen = 1023;
	}
	int i;
	for (i = 0; i < iLen; ++i)
	{
		sscanf_s(pucData+i*2,"%2x",ucBuf+i);
	}

	int iRet = NetClient_DigitalChannelSend(m_iLogonID, m_iChannelNo,(unsigned char *)ucBuf,i);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_DigitalChannelSend(%d,%#x,%d)",m_iLogonID,ucBuf,i);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_DigitalChannelSend(%d,%#x,%d)",m_iLogonID,ucBuf,i);
	}

}





void CLS_DNVRDigitPage::OnBnClickedButtonChange()
{
	// TODO: Add control notification handler code here
	if (m_IsNvssIpv6)
	{
		XChangeIpv6 tInfo = { 0 };
		tInfo.iSize = sizeof(XChangeIpv6);
		tInfo.iType = 0;
		GetDlgItemText(IDC_EDIT_CHANGE_USER, tInfo.pcUser, 16);
		GetDlgItemText(IDC_EDIT_CHANGE_PWD, tInfo.pcPwd, 16);
		GetDlgItemText(IDC_EDIT_CHANGE_ADDR, tInfo.pcIPv6, 64);
		CString strPrefixLenv6;
		GetDlgItemText(IDC_EDIT_CHANGE_PREFIX, strPrefixLenv6);
		tInfo.iPrefixLenv6 = atoi(strPrefixLenv6);
		GetDlgItemText(IDC_EDIT_CHANGE_GATEWAY, tInfo.pcGateWayv6, 64);
		GetDlgItemText(IDC_EDIT_CHANGE_DNS, tInfo.pcDNSv6, 64);

		POSITION pos = m_lstDeviceCtrl.GetFirstSelectedItemPosition();
		int iIndex = m_lstDeviceCtrl.GetNextSelectedItem(pos);

		CString strChannel = m_lstDeviceCtrl.GetItemText(iIndex,2);
		int iChannel = atoi(strChannel);
		strcpy(tInfo.pcMac,m_lstDeviceCtrl.GetItemText(iIndex,3));
		strcpy(tInfo.pcFactoryID,m_lstDeviceCtrl.GetItemText(iIndex,4));
		//tInfo.iCheckCode = 20160113;

		int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_XCHG_IPV6,iChannel,&tInfo, tInfo.iSize);
		if(0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","DigitalChangeIP Success!");
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","DigitalChangeIP Failed!");
		}
	}
	else
	{
		XChangeIp tInfo = { 0 };
		tInfo.iSize = sizeof(XChangeIp);
		tInfo.iType = 0;
		GetDlgItemText(IDC_EDIT_CHANGE_USER, tInfo.cUser, 16);
		GetDlgItemText(IDC_EDIT_CHANGE_PWD, tInfo.cPswd, 16);
		GetDlgItemText(IDC_EDIT_CHANGE_ADDR, tInfo.cIp, 16);
		GetDlgItemText(IDC_EDIT_CHANGE_PREFIX, tInfo.cMask,16);
		GetDlgItemText(IDC_EDIT_CHANGE_GATEWAY, tInfo.cGetWay, 16);
		GetDlgItemText(IDC_EDIT_CHANGE_DNS, tInfo.cDNS, 64);

		POSITION pos = m_lstDeviceCtrl.GetFirstSelectedItemPosition();
		int iIndex = m_lstDeviceCtrl.GetNextSelectedItem(pos);

		CString strChannel = m_lstDeviceCtrl.GetItemText(iIndex,2);
		int iChannel = atoi(strChannel);
		strcpy(tInfo.cMac,m_lstDeviceCtrl.GetItemText(iIndex,3));
		strcpy(tInfo.cFactryId,m_lstDeviceCtrl.GetItemText(iIndex,4));
		//tInfo.iCheckCode = 20160113;

		int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_XCHG_IP,iChannel,&tInfo, tInfo.iSize);
		if(0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","DigitalChangeIP Success!");
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","DigitalChangeIP Failed!");
		}
	}
}

void CLS_DNVRDigitPage::OnCbnSelchangeComboConnectmode()
{
	// TODO: Add control notification handler code here
	int iType = m_cboConnectMode.GetCurSel();
	if (3 == iType)
	{
		m_Edit_IpDdnsDsmIpv6.EnableWindow(TRUE);
		m_Edit_ProxyIpv6.EnableWindow(TRUE);
		m_edtIPIDDDNS.EnableWindow(FALSE);
		m_edtProxyIP.EnableWindow(FALSE);
	}
	else
	{
		m_Edit_IpDdnsDsmIpv6.EnableWindow(FALSE);
		m_Edit_ProxyIpv6.EnableWindow(FALSE);
		m_edtIPIDDDNS.EnableWindow(TRUE);
		m_edtProxyIP.EnableWindow(TRUE);
	}
}

void CLS_DNVRDigitPage::OnCbnSelchangeComboNetmode()
{
	// TODO: Add control notification handler code here
	int iTemp = m_cboNetMode.GetCurSel() + 1;
	if (NETMODE_MUC == iTemp)
	{
		m_edit_mcuip.EnableWindow(TRUE);
		m_edit_mcuport.EnableWindow(TRUE);
	}
	else
	{
		m_edit_mcuip.EnableWindow(FALSE);
		m_edit_mcuport.EnableWindow(FALSE);
	}
}

void CLS_DNVRDigitPage::OnBnClickedNvssIpv6()
{
	m_IsNvssIpv6 = true;
}

void CLS_DNVRDigitPage::OnBnClickedNvssIpv4()
{
	m_IsNvssIpv6 = false;
}

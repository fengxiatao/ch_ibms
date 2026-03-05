// WifiPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "WifiPage.h"


// CWifiPage dialog

IMPLEMENT_DYNAMIC(CWifiPage, CDialog)

CWifiPage::CWifiPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CWifiPage::IDD, pParent)
{
	m_iWifiWorkMode = -1;
}

CWifiPage::~CWifiPage()
{
}

void CWifiPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_WIFICARD, m_cboWifiCard);
	DDX_Control(pDX, IDC_COMBO_WIFISTATE, m_cboWifiState);
	DDX_Control(pDX, IDC_COMBO_WORKMODE, m_cboWifiWorkMode);
	DDX_Control(pDX, IDC_COMBO_ENCRYPTION, m_cboEncryption);
	DDX_Control(pDX, IDC_COMBO_KEYTYPE, m_cboKeyType);
	DDX_Control(pDX, IDC_COMBO_KEYNUM, m_cboKeyNum);
	DDX_Control(pDX, IDC_COMBO_PWDTYPE, m_cboPwdType);
	DDX_Control(pDX, IDC_LIST_WIFILIST, m_lstWifiList);
	DDX_Control(pDX, IDC_CHECK_DHCP, m_chkDhcp);
	DDX_Control(pDX, IDC_CHECK_APDHCP, m_chkApDhcp);
	DDX_Control(pDX, IDC_COMBO_COUNTRY, m_cboCountry);
	DDX_Control(pDX, IDC_COMBO_CHANNEL1, m_cboChannel);
	DDX_Control(pDX, IDC_COMBO_BANDWIDTH, m_cboBandWidth);
	DDX_Control(pDX, IDC_LIST_WIFILIST_AP, m_lstWifiListAP);
	DDX_Control(pDX, IDC_LIST_WIFILIST_APCLIENT, m_lstWifiListAPClient);
	DDX_Control(pDX, IDC_COMBO_WIFISTATE1, m_cboWifiState1);
	DDX_Control(pDX, IDC_EDIT_USER_NAME, m_edtUsrName);
	DDX_Control(pDX, IDC_EDIT_USR_NEW_PWD, m_edtUsrNewPwd);
}

BEGIN_MESSAGE_MAP(CWifiPage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SEARCH, &CWifiPage::OnBnClickedButtonSearch)
	ON_BN_CLICKED(IDC_BUTTON_SETWIFI, &CWifiPage::OnBnClickedButtonSetwifi)
	ON_BN_CLICKED(IDC_BUTTON_WIFIMODE, &CWifiPage::OnBnClickedButtonWifimode)
	ON_BN_CLICKED(IDC_BUTTON_SETAPDHCP, &CWifiPage::OnBnClickedButtonSetapdhcp)
	ON_NOTIFY(HDN_ITEMDBLCLICK, 0, &CWifiPage::OnHdnItemdblclickListWifilist)
	ON_BN_CLICKED(IDC_CHECK_DHCP, &CWifiPage::OnBnClickedCheckDhcp)
	ON_BN_CLICKED(IDC_CHECK_APDHCP, &CWifiPage::OnBnClickedCheckApdhcp)
    ON_BN_CLICKED(IDC_BUTTON_SEARCH_AP, &CWifiPage::OnBnClickedButtonSearchAp)
    ON_BN_CLICKED(IDC_BUTTON_SEARCH_APCLIENT, &CWifiPage::OnBnClickedButtonSearchApclient)
    ON_BN_CLICKED(IDC_BUTTON_WIFISTATE, &CWifiPage::OnBnClickedButtonWifistate)
	ON_BN_CLICKED(IDC_BUTTON_ONCE_CFG_WIFI, &CWifiPage::OnBnClickedButtonOnceCfgWifi)
END_MESSAGE_MAP()


void CWifiPage::UI_UpdateWifiState1(int _iCurSel)
{
    _iCurSel = (-1 == _iCurSel)?0:_iCurSel;
    m_cboWifiState1.ResetContent();
    m_cboWifiState1.AddString(GetTextByLan(GetTextByLan(_T("断开"), _T("offline"))));
    m_cboWifiState1.AddString(GetTextByLan(GetTextByLan(_T("连接"), _T("online"))));
    m_cboWifiState1.SetCurSel(_iCurSel);
}

// CWifiPage message handlers
void CWifiPage::UI_UpdateWifiCard(int _iCurSel)
{
	_iCurSel = (-1 == _iCurSel)?0:_iCurSel;
    m_cboWifiCard.ResetContent();
	m_cboWifiCard.AddString(GetTextEx(IDS_CONFIG_WIFI_NO));
	m_cboWifiCard.AddString(GetTextEx(IDS_CONFIG_WIFI_YES));
	m_cboWifiCard.SetCurSel(_iCurSel);
}

void CWifiPage::UI_UpdateState(int _iCurSel)
{
	_iCurSel = (-1 == _iCurSel)?0:_iCurSel;
	m_cboWifiState.ResetContent();
	m_cboWifiState.AddString(GetTextEx(IDS_CONFIG_WIFI_OFFLINE));
	m_cboWifiState.AddString(GetTextEx(IDS_CONFIG_WIFI_ONLINE));
	m_cboWifiState.SetCurSel(_iCurSel);
}

void CWifiPage::UI_UpdateWorkMode()
{
	m_cboWifiWorkMode.ResetContent();
	m_cboWifiWorkMode.AddString(GetTextEx(IDS_CONFIG_WIFI_OFFLINE));
	m_cboWifiWorkMode.AddString(GetTextEx(IDS_CONFIG_WIFT_WIFIMODE_STA));
	m_cboWifiWorkMode.AddString(GetTextEx(IDS_CONFIG_WIFT_WIFIMODE_AP));
    m_cboWifiWorkMode.AddString(GetTextByLan(_T("AP+sta"), _T("AP+sta")));

    WifiWorkMode stWorkMode = {0};
    stWorkMode.iSize = sizeof(WifiWorkMode);
    stWorkMode.iWifiMode = -1;
    stWorkMode.iChannel = m_iChannelNO;
	if(0 == NetClient_GetLanParam(m_iLogonID,LAN_CMD_GET_WIFIWORKMODE_CHN,&stWorkMode))
	{
		m_cboWifiWorkMode.SetCurSel(stWorkMode.iWifiMode);
		m_iWifiWorkMode = stWorkMode.iWifiMode;
	}
	else
	{
		m_cboWifiWorkMode.SetCurSel(0);
	}
}

void CWifiPage::UI_UpdateEncryption(int _iCurSel)
{
	_iCurSel = (-1 == _iCurSel)?0:_iCurSel;
	m_cboEncryption.ResetContent();
	m_cboEncryption.AddString("none");
	m_cboEncryption.AddString("WEP");
	m_cboEncryption.AddString("WPA-PSK");
	m_cboEncryption.AddString("WPA2-PSK");
	m_cboEncryption.SetCurSel(_iCurSel);
}

void CWifiPage::UI_UpdateKeyType(int _iCurSel)
{
	_iCurSel = (-1 == _iCurSel)?0:_iCurSel;
	m_cboKeyType.ResetContent();
	m_cboKeyType.AddString("ascii");
	m_cboKeyType.AddString("hex");
	m_cboKeyType.SetCurSel(_iCurSel);
}

void CWifiPage::UI_UpdatePwdType(int _iCurSel)
{
	_iCurSel = (-1 == _iCurSel)?0:_iCurSel;
	m_cboPwdType.ResetContent();
	m_cboPwdType.AddString("disable");
	m_cboPwdType.AddString("64bit");
	m_cboPwdType.AddString("128bit");
	m_cboPwdType.SetCurSel(_iCurSel);
}

void CWifiPage::UI_UpdateKeyNum(int _iCurSel)
{
	_iCurSel = (-1 == _iCurSel)?0:_iCurSel;
	m_cboKeyNum.ResetContent();
	m_cboKeyNum.AddString("1 -- TKIP");
	m_cboKeyNum.AddString("2 -- AES");
	m_cboKeyNum.AddString("3");
	m_cboKeyNum.AddString("4");
	m_cboKeyNum.SetCurSel(_iCurSel);
}

void CWifiPage::UI_UpdateWifiList()
{
	m_lstWifiList.DeleteAllItems();
	int nColumnCount = m_lstWifiList.GetHeaderCtrl()->GetItemCount();
	for (int i=0; i < nColumnCount; i++)
	{
		m_lstWifiList.DeleteColumn(0);
	}

	m_lstWifiList.SetExtendedStyle(m_lstWifiList.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	int iColumnIndex = 0;
	m_lstWifiList.InsertColumn( iColumnIndex++, GetTextEx(IDS_CONFIG_WIFI_ESSIDLIST), LVCFMT_LEFT, 150 );
	m_lstWifiList.InsertColumn( iColumnIndex++, GetTextEx(IDS_CONFIG_WIFI_ENCRYPTIONLIST), LVCFMT_LEFT, 90 );
}

void CWifiPage::UI_UpdateSurface()
{
	UI_UpdateWifiList();
    UI_UpdateWifiListAPClient();
    UI_UpdateWifiListAP();
	SetDlgItemTextEx(IDC_STATIC_WIFIINFO, IDS_CONFIG_WIFI_INFO);
	SetDlgItemTextEx(IDC_BUTTON_SEARCH, IDS_CONFIG_WIFI_SEARCH);
	SetDlgItemTextEx(IDC_STATIC_WIFICARD, IDS_CONFIG_WIFI_WIFICARD);
	SetDlgItemTextEx(IDC_STATIC_STATE, IDS_CONFIG_WIFI_STATE);
	SetDlgItemTextEx(IDC_STATIC_WORKMODE, IDS_CONFIG_WIFI_WORKMODE);
	SetDlgItemTextEx(IDC_BUTTON_WIFIMODE, IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_RANGE, IDS_CONFIG_WIFI_DHCPRANGE);
	SetDlgItemTextEx(IDC_STATIC_TO, IDS_CONFIG_WIFI_TO);
	SetDlgItemTextEx(IDC_STATIC_RENTTIME, IDS_CONFIG_WIFI_RENTTIME);
	SetDlgItemTextEx(IDC_STATIC_TIME, IDS_CONFIG_WIFI_MINUTE);
	SetDlgItemTextEx(IDC_BUTTON_SETAPDHCP, IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_IP,IDS_CONFIG_IP);
	SetDlgItemTextEx(IDC_STATIC_GATEWAY,IDS_CONFIG_LAN_GATEWAY);
	SetDlgItemTextEx(IDC_STATIC_MASK, IDS_CONFIG_LAN_MASK);
	SetDlgItemTextEx(IDC_STATIC_DNS, IDS_CONFIG_WIFI_DNS);
	SetDlgItemTextEx(IDC_STATIC_ESSID, IDS_CONFIG_WIFI_ESSID);
	SetDlgItemTextEx(IDC_STATIC_ENCRYPTION, IDS_CONFIG_WIFI_ENCRYPTION);
	SetDlgItemTextEx(IDC_STATIC_PASSWORD, IDS_CONFIG_WIFI_PASSWORD);
	SetDlgItemTextEx(IDC_STATIC_KEYTYPE, IDS_CONFIG_WIFI_KEYTYPE);
	SetDlgItemTextEx(IDC_STATIC_KEYNUM, IDS_CONFIG_WIFI_KEYNUM);
	SetDlgItemTextEx(IDC_STATIC_PWDTYPE, IDS_CONFIG_WIFI_PWDTYPE);
	SetDlgItemTextEx(IDC_BUTTON_SETWIFI, IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_HINT, IDS_CONFIG_WIFI_REMARK1);
	SetDlgItemTextEx(IDC_CHECK_DHCP, IDS_CONFIG_WIFI_DHCP);
	SetDlgItemTextEx(IDC_CHECK_APDHCP, IDS_CONFIG_WIFI_APDHCP);

    SetDlgItemText(IDC_STATIC_PCVERSION, GetTextByLan(_T("模块版本"), _T("PcVersion")));
    SetDlgItemText(IDC_BUTTON1, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_USER_NAME, GetTextByLan(("用户名"), ("UserName")));
	SetDlgItemText(IDC_STATIC_NEW_PWD, GetTextByLan(("新密码"), ("NewPwd")));
	SetDlgItemText(IDC_BUTTON_ONCE_CFG_WIFI, GetTextByLan(("一键配置WIFI"), ("OnceConfigWifi")));

    UI_UpdateCountry();
    UI_UpdateChannel();
    UI_UpdateBandWidth();

	UI_UpdateWifiCard(m_cboWifiCard.GetCurSel());
	UI_UpdateState(m_cboWifiState.GetCurSel());
    UI_UpdateWifiState1(m_cboWifiState1.GetCurSel());
	UI_UpdateWorkMode();
	UI_UpdateEncryption(m_cboEncryption.GetCurSel());
	UpdateDeviceInfo();
}

void CWifiPage::UI_UpdateCountry()
{
    SetDlgItemText(IDC_STATIC_COUNTRY, GetTextByLan(_T("国家"), _T("Country")));
    m_cboCountry.ResetContent();
    m_cboCountry.SetItemData(m_cboCountry.AddString(GetTextByLan(_T("我国"), _T("Our Country"))), 0);
    m_cboCountry.SetItemData(m_cboCountry.AddString(GetTextByLan(_T("北美"), _T("North Americal"))), 1);
    m_cboCountry.SetItemData(m_cboCountry.AddString(GetTextByLan(_T("日本"), _T("Japan"))), 2);
    m_cboCountry.SetItemData(m_cboCountry.AddString(GetTextByLan(_T("欧洲"), _T("Europe"))), 3);
    m_cboCountry.SetItemData(m_cboCountry.AddString(GetTextByLan(_T("其他"), _T("Other"))), 4);
    m_cboCountry.SetCurSel(0);
}

void CWifiPage::UI_UpdateChannel()
{
    SetDlgItemText(IDC_STATIC_CHANNEL, GetTextByLan(_T("信道"), _T("Channel")));
    m_cboChannel.ResetContent();
    for (int idx = 0; idx < 14; idx++)
    {
        m_cboChannel.AddString(IntToString(idx));
    }
    m_cboChannel.SetCurSel(0);
}

void CWifiPage::UI_UpdateBandWidth()
{
    SetDlgItemText(IDC_STATIC_BANDWIDTH, GetTextByLan(_T("带宽"), _T("BandWidth")));
    m_cboBandWidth.ResetContent();
    m_cboBandWidth.SetItemData(m_cboBandWidth.AddString(GetTextByLan(_T("自适应"), _T("Adaption"))), 0);
    m_cboBandWidth.SetItemData(m_cboBandWidth.AddString(GetTextByLan(_T("固定"), _T("Fixed"))), 1);
    m_cboBandWidth.SetCurSel(0);
}

void CWifiPage::UpdateDeviceInfo()
{
	NVS_WIFI_PARAM wifiPara = {0};

	
	int iCmd = LAN_CMD_GET_WIFIPARA;
	WIFIPARAM_DHCP dhcp = {0};
	dhcp.iSize = sizeof(WIFIPARAM_DHCP);
    WifiWorkMode stWorkMode = {0};
    stWorkMode.iSize = sizeof(WifiWorkMode);
    stWorkMode.iWifiMode = -1;
    stWorkMode.iChannel = m_iChannelNO;
    
	if(0 == NetClient_GetLanParam(m_iLogonID,LAN_CMD_GET_WIFIWORKMODE_CHN,&stWorkMode))
	{
        int iWifiWorkMode = stWorkMode.iWifiMode;
		iCmd = (iWifiWorkMode == 2)?LAN_CMD_GET_WIFIAPPARA:LAN_CMD_GET_WIFIPARA;
		if (iWifiWorkMode == 2)
		{
			dhcp.iWifiDHCPMode = 1;
			if(0 == NetClient_GetLanParam(m_iLogonID,LAN_CMD_GET_WIFIDHCPMODE,&dhcp))
			{
				m_chkApDhcp.SetCheck(dhcp.iEnable);
			}
			m_chkDhcp.SetCheck(BST_UNCHECKED);
		}
		else if (iWifiWorkMode == 1)
		{
			dhcp.iWifiDHCPMode = 0;
			if(0 == NetClient_GetLanParam(m_iLogonID,LAN_CMD_GET_WIFIDHCPMODE,&dhcp))
			{
				m_chkDhcp.SetCheck(dhcp.iEnable);
			}
			m_chkApDhcp.SetCheck(BST_UNCHECKED);
		}
		else
		{
			m_chkApDhcp.SetCheck(BST_UNCHECKED);
			m_chkDhcp.SetCheck(BST_UNCHECKED);
		}
	}
	if(0 == NetClient_GetLanParam(m_iLogonID, iCmd, &wifiPara))
	{
		SetDlgItemText(IDC_EDIT_IP, wifiPara.cWifiSvrIP);
		SetDlgItemText(IDC_EDIT_MASK, wifiPara.cWifiMask);
        SetDlgItemText(IDC_EDIT_GATEWAY, wifiPara.cWifiGateway);
		SetDlgItemText(IDC_EDIT_DNS, wifiPara.cWifiDNS);
           
		SetDlgItemText(IDC_EDIT_ESSID, wifiPara.cESSID);
		SetDlgItemText(IDC_EDIT_PASSWORD, wifiPara.cWifiPassword); 
        m_cboKeyType.SelectString(-1, wifiPara.cWifiKeyType);
        m_cboEncryption.SelectString(-1, wifiPara.cEncryption);
        m_cboKeyNum.SelectString(-1, wifiPara.cWifiKeyNum);
		m_cboWifiCard.SetCurSel(wifiPara.iHaveCard);
        m_cboWifiState.SetCurSel(wifiPara.iCardOnline);
        SetDlgItemText(IDC_EDIT_PCVERSION, wifiPara.cPcVersion);
        m_cboCountry.SetCurSel(wifiPara.iCountry);
        m_cboChannel.SetCurSel(wifiPara.iChannel);
        m_cboBandWidth.SetCurSel(wifiPara.iBandWidth);

		//password length
		if (strcmp(wifiPara.cWifiKeyType, "hex") == 0 && strlen(wifiPara.cWifiPassword) == 10)
		{
			m_cboPwdType.SetCurSel(1);
		}
		else if(strcmp(wifiPara.cWifiKeyType, "hex") == 0 && strlen(wifiPara.cWifiPassword) == 26)
		{
			m_cboPwdType.SetCurSel(2);
		}
		else if(strcmp(wifiPara.cWifiKeyType, "ascii") == 0 && strlen(wifiPara.cWifiPassword) == 5)
		{
			m_cboPwdType.SetCurSel(1);
		}
		else if(strcmp(wifiPara.cWifiKeyType, "ascii") == 0 && strlen(wifiPara.cWifiPassword) == 13)
		{
			m_cboPwdType.SetCurSel(2);
		}
		else
		{
			m_cboPwdType.SetCurSel(0);
		}
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetLanParam(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetLanParam(%d)", m_iLogonID);
	}
}

BOOL CWifiPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateEncryption(0);
	UI_UpdateKeyType(0);
	UI_UpdatePwdType(0);
	UI_UpdateKeyNum(0);
	m_edtUsrName.SetLimitText(LEN_16 - 1);
	m_edtUsrNewPwd.SetLimitText(LEN_16 - 1);

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CWifiPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	UI_UpdateSurface();
	UI_UpdateWifiDhcp();
}

void CWifiPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateSurface();
}

void CWifiPage::OnMainNotify( int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser  )
{

}

void CWifiPage::OnBnClickedButtonSearch()
{
	if (m_iLogonID < -1)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
	}
	m_lstWifiList.DeleteAllItems();
	if(0 == NetClient_WifiSearch(m_iLogonID))
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_WifiSearch(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_WifiSearch(%d)", m_iLogonID);
	}
}

void CWifiPage::OnBnClickedButtonSetwifi()
{
	if (m_iLogonID < -1)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
	}
	NVS_WIFI_PARAM wifiParam = {0};
	GetDlgItemText(IDC_EDIT_IP, wifiParam.cWifiSvrIP, sizeof(wifiParam.cWifiSvrIP));
	GetDlgItemText(IDC_EDIT_MASK, wifiParam.cWifiMask, sizeof(wifiParam.cWifiMask));
	GetDlgItemText(IDC_EDIT_GATEWAY, wifiParam.cWifiGateway, sizeof(wifiParam.cWifiGateway));
	GetDlgItemText(IDC_EDIT_DNS, wifiParam.cWifiDNS, sizeof(wifiParam.cWifiDNS));
	GetDlgItemText(IDC_EDIT_ESSID, wifiParam.cESSID, sizeof(wifiParam.cESSID));
	GetDlgItemText(IDC_COMBO_KEYTYPE, wifiParam.cWifiKeyType, sizeof(wifiParam.cWifiKeyType));
	GetDlgItemText(IDC_EDIT_PASSWORD, wifiParam.cWifiPassword, sizeof(wifiParam.cWifiPassword));
	GetDlgItemText(IDC_COMBO_ENCRYPTION, wifiParam.cEncryption, sizeof(wifiParam.cEncryption));
	GetDlgItemText(IDC_COMBO_KEYNUM, wifiParam.cWifiKeyNum, sizeof(wifiParam.cWifiKeyNum));

    GetDlgItemText(IDC_EDIT_PCVERSION, wifiParam.cPcVersion, sizeof(wifiParam.cPcVersion));
    wifiParam.iCountry = m_cboCountry.GetCurSel();
    wifiParam.iChannel = m_cboChannel.GetCurSel();
    wifiParam.iBandWidth = m_cboBandWidth.GetCurSel();

	//limit password length
	if (strcmp(wifiParam.cWifiKeyType, "hex") == 0)
	{
		if (m_cboPwdType.GetCurSel() == 1)
		{
			if (strlen(wifiParam.cWifiPassword) != 10)
			{
				MessageBox(GetTextEx(IDS_CONFIG_WIFI_PWD_NOT_LEAGL));;
				return;
			}
		}
		else if (m_cboPwdType.GetCurSel() == 2)
		{
			if (strlen(wifiParam.cWifiPassword) != 26)
			{
				MessageBox(GetTextEx(IDS_CONFIG_WIFI_PWD_NOT_LEAGL));;
				return;
			}
		}
		
	}
	else if(strcmp(wifiParam.cWifiKeyType, "ascii") == 0)
	{
		if (m_cboPwdType.GetCurSel() == 1)
		{
			if (strlen(wifiParam.cWifiPassword) != 5)
			{
				MessageBox(GetTextEx(IDS_CONFIG_WIFI_PWD_NOT_LEAGL));;
				return;
			}
		}
		else if (m_cboPwdType.GetCurSel() == 2)
		{
			if (strlen(wifiParam.cWifiPassword) != 13)
			{
				MessageBox(GetTextEx(IDS_CONFIG_WIFI_PWD_NOT_LEAGL));;
				return;
			}
		}
	}

	
	int iCmd = LAN_CMD_SET_WIFIPARA;
    WifiWorkMode stWorkMode = {0};
    stWorkMode.iSize = sizeof(WifiWorkMode);
    stWorkMode.iWifiMode = -1;
    stWorkMode.iChannel = m_iChannelNO;
	if(0 == NetClient_GetLanParam(m_iLogonID,LAN_CMD_GET_WIFIWORKMODE_CHN,&stWorkMode))
	{
        int iWifiWorkMode = stWorkMode.iWifiMode;
		iCmd = (iWifiWorkMode == 2)?LAN_CMD_SET_WIFIAPPARA:LAN_CMD_SET_WIFIPARA;
	}
 
	if(0 == NetClient_SetLanParam(m_iLogonID, iCmd, &wifiParam))
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
}

void CWifiPage::OnBnClickedButtonWifimode()
{
	if (m_iLogonID < -1)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
	}
	int  iWifiWorkMode = m_cboWifiWorkMode.GetCurSel();
    WifiWorkMode stWorkMode = {0};
    stWorkMode.iSize = sizeof(WifiWorkMode);
    stWorkMode.iWifiMode = iWifiWorkMode;
    stWorkMode.iChannel = m_iChannelNO;
	if(0 == NetClient_SetLanParam(m_iLogonID, LAN_CMD_SET_WIFIWORKMODE_CHN, &stWorkMode))
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
}

void CWifiPage::OnBnClickedButtonSetapdhcp()
{
	if (m_iLogonID < -1)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
	}
	WIFIAPDHCPPARA_DHCP  WifiApDHCPPara = {0};
	WifiApDHCPPara.iSize = sizeof(WIFIAPDHCPPARA_DHCP);

	
	WifiApDHCPPara.iDHCPStart = GetDlgItemInt(IDC_EDIT_IPSTART);
	WifiApDHCPPara.iDHCPEnd = GetDlgItemInt(IDC_EDIT_IPEND);
	WifiApDHCPPara.iDHCPLease = GetDlgItemInt(IDC_EDIT_RENTTIME);

	if (0 == NetClient_SetLanParam(m_iLogonID, LAN_CMD_SET_WIFIAPDHCPPARA, &WifiApDHCPPara))
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
}

void CWifiPage::OnHdnItemdblclickListWifilist(NMHDR *pNMHDR, LRESULT *pResult)
{
	//LPNMHEADER phdr = reinterpret_cast<LPNMHEADER>(pNMHDR);
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	int nItem = -1;
	if (pNMItemActivate != NULL)
	{
		nItem = pNMItemActivate->iItem;
		if (-1 != nItem)
		{
			SetDlgItemText(IDC_EDIT_ESSID,m_lstWifiList.GetItemText(nItem,0));	
		}
	}
	*pResult = 0;
}

void CWifiPage::OnBnClickedCheckDhcp()
{
	if (m_iLogonID < -1)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
	}
	WIFIPARAM_DHCP WifiParam = {0};
	WifiParam.iSize = sizeof(WIFIPARAM_DHCP);
	WifiParam.iWifiDHCPMode = 0;
	WifiParam.iEnable = (BST_CHECKED == m_chkDhcp.GetCheck())?1:0;

	if (WifiParam.iEnable)
	{
		GetDlgItem(IDC_EDIT_IPSTART)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_IPEND)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_RENTTIME)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_SETAPDHCP)->EnableWindow(FALSE);
		m_chkApDhcp.SetCheck(BST_UNCHECKED);
	}

	if(0 == NetClient_SetLanParam(m_iLogonID,LAN_CMD_SET_WIFIDHCPMODE,&WifiParam))
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
}

void CWifiPage::OnBnClickedCheckApdhcp()
{
	if (m_iLogonID < -1)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
	}
	WIFIPARAM_DHCP WifiParam = {0};
	WifiParam.iSize = sizeof(WIFIPARAM_DHCP);
	WifiParam.iWifiDHCPMode = 1;
	WifiParam.iEnable = (BST_CHECKED == m_chkApDhcp.GetCheck())?1:0;

	if (WifiParam.iEnable)
	{
		GetDlgItem(IDC_EDIT_IPSTART)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_IPEND)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_RENTTIME)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_SETAPDHCP)->EnableWindow(TRUE);
		m_chkDhcp.SetCheck(BST_UNCHECKED);
	}

	if(0 == NetClient_SetLanParam(m_iLogonID,LAN_CMD_SET_WIFIDHCPMODE,&WifiParam))
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetLanParam(%d)", m_iLogonID);
	}
}

void CWifiPage::OnParamChangeNotify( int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUser )
{
	int       iWifiCount = 0;
    switch(_iParaType)
	{
	case PARA_WIFI_SEARCH_RESULT:
		WIFI_INFO *pWifiInfo;
		
		NetClient_GetWifiSearchResult(m_iLogonID, &pWifiInfo, &iWifiCount);
		m_lstWifiList.DeleteAllItems();
		for (int i=0; i<iWifiCount; i++)
		{
			int iColumnIndex = 0;;
			m_lstWifiList.InsertItem(0, "");
			m_lstWifiList.SetItemText(0, iColumnIndex++, pWifiInfo[i].cESSID);
			m_lstWifiList.SetItemText(0, iColumnIndex++, pWifiInfo[i].cEncryption);
		}
		break;
    case PARA_WIFISTATE_CHANGED:
        {
            WifiState result = {0};
            result.iSize = sizeof(WifiState);
            result.iWifiState = -1;
            int iReturnBytes = 0;
            if(0 == NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_WIFI_STATE, m_iChannelNO, &result, result.iSize, &iReturnBytes))
            {
                AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_WIFI_STATE(%d,%d)", m_iLogonID, result.iWifiState);
            }
            else
            {
                AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_WIFI_STATE(%d,%d)", m_iLogonID, result.iWifiState);
            }
        }
        break;
	default:
		break;
	}
}

void CWifiPage::UI_UpdateWifiDhcp()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	}

	if (m_chkApDhcp.GetCheck() == BST_CHECKED)
	{
		GetDlgItem(IDC_EDIT_IPSTART)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_IPEND)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_RENTTIME)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_SETAPDHCP)->EnableWindow(TRUE);

		WIFIAPDHCPPARA_DHCP  WifiApDHCPPara = {0};
		WifiApDHCPPara.iSize = sizeof(WIFIAPDHCPPARA_DHCP);
		if (0 == NetClient_GetLanParam(m_iLogonID, LAN_CMD_GET_WIFIAPDHCPPARA, &WifiApDHCPPara))
		{
			SetDlgItemInt(IDC_EDIT_IPSTART, WifiApDHCPPara.iDHCPStart);
			SetDlgItemInt(IDC_EDIT_IPEND, WifiApDHCPPara.iDHCPEnd);
			SetDlgItemInt(IDC_EDIT_RENTTIME, WifiApDHCPPara.iDHCPLease);
			AddLog(LOG_TYPE_SUCC, "", "NetClient_SetLanParam(%d)", m_iLogonID);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetLanParam(%d)", m_iLogonID);
		}
	}
	else
	{
		GetDlgItem(IDC_EDIT_IPSTART)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_IPEND)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_RENTTIME)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_SETAPDHCP)->EnableWindow(FALSE);

	}

}

void CWifiPage::UI_UpdateWifiListAP()
{
    m_lstWifiListAP.DeleteAllItems();
    int nColumnCount = m_lstWifiListAP.GetHeaderCtrl()->GetItemCount();
    for (int i=0; i < nColumnCount; i++)
    {
        m_lstWifiListAP.DeleteColumn(0);
    }

    m_lstWifiListAP.SetExtendedStyle(m_lstWifiListAP.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
    int iColumnIndex = 0;
    m_lstWifiListAP.InsertColumn( iColumnIndex++, GetTextByLan(_T("序号"), _T("No")), LVCFMT_LEFT, 40 );
    m_lstWifiListAP.InsertColumn( iColumnIndex++, GetTextByLan(_T("路由器标识"), _T("ESSID")), LVCFMT_LEFT, 40 );
    m_lstWifiListAP.InsertColumn( iColumnIndex++, GetTextByLan(_T("加密方式"), _T("ENCRYPTION")), LVCFMT_LEFT, 40 );
    m_lstWifiListAP.InsertColumn( iColumnIndex++, GetTextByLan(_T("信道"), _T("Channnel")), LVCFMT_LEFT, 40 );
    m_lstWifiListAP.InsertColumn( iColumnIndex++, GetTextByLan(_T("信号强度"), _T("RSSL")), LVCFMT_LEFT, 40 );
    m_lstWifiListAP.InsertColumn( iColumnIndex++, GetTextByLan(_T("速度"), _T("Speed")), LVCFMT_LEFT, 40 );
}

void CWifiPage::UI_UpdateWifiListAPClient()
{
    m_lstWifiListAPClient.DeleteAllItems();
    int nColumnCount = m_lstWifiListAPClient.GetHeaderCtrl()->GetItemCount();
    for (int i=0; i < nColumnCount; i++)
    {
        m_lstWifiListAPClient.DeleteColumn(0);
    }

    m_lstWifiListAPClient.SetExtendedStyle(m_lstWifiListAPClient.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
    int iColumnIndex = 0;
    m_lstWifiListAPClient.InsertColumn( iColumnIndex++, GetTextByLan(_T("序号"), _T("No")), LVCFMT_LEFT, 40 );
    m_lstWifiListAPClient.InsertColumn( iColumnIndex++, GetTextByLan(_T("设备名称"), _T("DevName")), LVCFMT_LEFT, 40 );
    m_lstWifiListAPClient.InsertColumn( iColumnIndex++, GetTextByLan(_T("ip地址"), _T("Ip")), LVCFMT_LEFT, 40 );
    m_lstWifiListAPClient.InsertColumn( iColumnIndex++, GetTextByLan(_T("mac地址"), _T("Mac")), LVCFMT_LEFT, 40 );
    m_lstWifiListAPClient.InsertColumn( iColumnIndex++, GetTextByLan(_T("速度"), _T("Speed")), LVCFMT_LEFT, 40 );
    m_lstWifiListAPClient.InsertColumn( iColumnIndex++, GetTextByLan(_T("带宽"), _T("BandWidth")), LVCFMT_LEFT, 40 );
    m_lstWifiListAPClient.InsertColumn( iColumnIndex++, GetTextByLan(_T("连接时间"), _T("ConnectTime")), LVCFMT_LEFT, 40 );
}
void CWifiPage::OnBnClickedButtonSearchAp()
{
    if (m_iLogonID < -1)
    {
        AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
    }
    m_lstWifiListAP.DeleteAllItems();
    WIFISearchAPResultList result = {0};
    result.iSize = sizeof(WIFISearchAPResultList);
    if(0 == NetClient_CmdConfig(m_iLogonID, CMD_WIFI_SEARCHAP, m_iChannelNO, NULL, 0, &result, result.iSize))
    {
        AddLog(LOG_TYPE_SUCC, "", "CMD_WIFI_SEARCHAP(%d)", m_iLogonID);

        m_lstWifiListAP.DeleteAllItems();
        for (int i=0; i<result.tResult[0].iCount; i++)
        {
            int iColumnIndex = 0;;
            m_lstWifiListAP.InsertItem(0, "");
            WIFISearchAPResult& resultRef = result.tResult[i];
            m_lstWifiListAP.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iNo));
            m_lstWifiListAP.SetItemText(0, iColumnIndex++, resultRef.cESSID);
            m_lstWifiListAP.SetItemText(0, iColumnIndex++, resultRef.cEncryption);
            m_lstWifiListAP.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iChannel));
            m_lstWifiListAP.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iRSSL));
            m_lstWifiListAP.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iSpeed));
        }
    }
    else
    {
        AddLog(LOG_TYPE_FAIL, "", "CMD_WIFI_SEARCHAP(%d)", m_iLogonID);
    }
}

void CWifiPage::OnBnClickedButtonSearchApclient()
{
    if (m_iLogonID < -1)
    {
        AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
    }
    m_lstWifiListAPClient.DeleteAllItems();
    WifiAPClientList result = {0};
    result.iSize = sizeof(WifiAPClientList);
    if(0 == NetClient_CmdConfig(m_iLogonID, CMD_WIFI_APCLIENTLIST, m_iChannelNO, NULL, 0, &result, result.iSize))
    {
        AddLog(LOG_TYPE_SUCC, "", "CMD_WIFI_APCLIENTLIST(%d)", m_iLogonID);

        m_lstWifiListAPClient.DeleteAllItems();
        for (int i=0; i<result.tResult[0].iCount; i++)
        {
            int iColumnIndex = 0;;
            m_lstWifiListAPClient.InsertItem(0, "");
            WIFIAPClientResult& resultRef = result.tResult[i];
            m_lstWifiListAPClient.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iNo));
            m_lstWifiListAPClient.SetItemText(0, iColumnIndex++, resultRef.cDevName);
            m_lstWifiListAPClient.SetItemText(0, iColumnIndex++, resultRef.cIp);
            m_lstWifiListAPClient.SetItemText(0, iColumnIndex++, resultRef.cMac);
            m_lstWifiListAPClient.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iSpeed));
            m_lstWifiListAPClient.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iBandWidth));
            m_lstWifiListAPClient.SetItemText(0, iColumnIndex++, IntToCString(resultRef.iConnectTime));

        }
    }
    else
    {
        AddLog(LOG_TYPE_FAIL, "", "CMD_WIFI_APCLIENTLIST(%d)", m_iLogonID);
    }
}

void CWifiPage::OnBnClickedButtonWifistate()
{
    if (m_iLogonID < -1)
    {
        AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
    }

    WifiState result = {0};
    result.iSize = sizeof(WifiState);
    result.iWifiState = m_cboWifiState1.GetCurSel();
    if(0 == NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_WIFI_STATE, m_iChannelNO, &result, result.iSize))
    {
        AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_WIFI_STATE(%d)", m_iLogonID);
    }
    else
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_WIFI_STATE(%d)", m_iLogonID);
    }
}

void CWifiPage::OnBnClickedButtonOnceCfgWifi()
{
	if (m_iLogonID < -1) {
		AddLog(LOG_TYPE_FAIL, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	}

	int iRet = RET_FAILED;
	OnceCfgWifiPara tWifiPara = {0};
	OnceCfgWifiResult tWifiResult = {0};
	GetDlgItemText(IDC_EDIT_ESSID, tWifiPara.cEssId, sizeof(tWifiPara.cEssId));
	GetDlgItemText(IDC_COMBO_ENCRYPTION, tWifiPara.cEncryption, sizeof(tWifiPara.cEncryption));
	GetDlgItemText(IDC_COMBO_KEYTYPE, tWifiPara.cWifiKeyType, sizeof(tWifiPara.cWifiKeyType));
	GetDlgItemText(IDC_EDIT_PASSWORD, tWifiPara.cWifiConPwd, sizeof(tWifiPara.cWifiConPwd));
	tWifiPara.iWifiKeyNum = m_cboKeyNum.GetCurSel();
	iRet = NetClient_CmdConfig(m_iLogonID, CMD_ONCE_CFG_WIFI, 0, &tWifiPara, sizeof(tWifiPara), &tWifiResult, sizeof(tWifiResult));
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_CmdConfig]CMD_ONCE_CFG_WIFI(m_iLogonID=%d, iResult=%d)", m_iLogonID, tWifiResult.iResult);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_CmdConfig]CMD_ONCE_CFG_WIFI(%d)", m_iLogonID);
	}
}

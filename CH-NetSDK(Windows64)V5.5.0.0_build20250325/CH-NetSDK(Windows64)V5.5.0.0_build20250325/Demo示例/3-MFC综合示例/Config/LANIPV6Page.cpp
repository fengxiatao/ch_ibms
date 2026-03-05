// LANIPV6Page.cpp : implementation file
//

#include "stdafx.h"
#include "LANIPV6Page.h"

#define IPV6DNSENABLE_AUTO     0
#define IPV6DNSENABLE_MANUAL   1

#define IPV6MODE_RESERVE	   0
#define IPV6MODE_MANUAL		   1
#define IPV6MODE_DHCP		   2
#define IPV6MODE_ROUTING	   3
// CLS_LANIPV6Page dialog

IMPLEMENT_DYNAMIC(CLS_LANIPV6Page, CDialog)

CLS_LANIPV6Page::CLS_LANIPV6Page(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_LANIPV6Page::IDD, pParent)
{
	m_iLogonID = -1;
}

CLS_LANIPV6Page::~CLS_LANIPV6Page()
{
}

void CLS_LANIPV6Page::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_IPV6_LANNO, m_cboLanNo);
	DDX_Control(pDX, IDC_EDIT_IPV6_IP, m_edtIP);
	DDX_Control(pDX, IDC_EDIT_IPV6_PREFIXLEN, m_edtPreFixLen);
	DDX_Control(pDX, IDC_EDIT_IPV6_GATEWAY, m_edtGateway);
	DDX_Control(pDX, IDC_EDIT_IPV6_DNS, m_edtDNS);
	DDX_Control(pDX, IDC_EDIT_IPV6_BACKDNS, m_edtBackDNS);
	DDX_Control(pDX, IDC_EDIT_RESERVED, m_edtReserved);
	DDX_Control(pDX, IDC_BUTTON_IPV6, m_btnIPV6);
	DDX_Control(pDX, IDC_COMBO_IPV6_DNSENABLE, m_Combo_Ipv6_DnsEnable);
	DDX_Control(pDX, IDC_COMBO_IPV6_MODE, m_Combo_Ipv6_Mode);
	DDX_Control(pDX, IDC_COMBO_IPV6_LIST, m_ComboIpv6AddrList);
}


BEGIN_MESSAGE_MAP(CLS_LANIPV6Page, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_IPV6, &CLS_LANIPV6Page::OnBnClickedButtonIpv6)
	ON_CBN_SELCHANGE(IDC_COMBO_IPV6_LANNO, &CLS_LANIPV6Page::OnCbnSelchangeComboIpv6Lanno)
	ON_CBN_SELCHANGE(IDC_COMBO_IPV6_MODE, &CLS_LANIPV6Page::OnCbnSelchangeComboIpv6Mode)
	ON_CBN_SELCHANGE(IDC_COMBO_IPV6_DNSENABLE, &CLS_LANIPV6Page::OnCbnSelchangeComboIpv6Dnsenable)
	ON_BN_CLICKED(IDC_BUTTON_IPV6_REFRESH, &CLS_LANIPV6Page::OnBnClickedButtonIpv6Refresh)
END_MESSAGE_MAP()


// CLS_LANIPV6Page message handlers

BOOL CLS_LANIPV6Page::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtIP.SetLimitText(LENGTH_IPV6_V1);
	m_edtGateway.SetLimitText(LENGTH_IPV6_V1);
	m_edtDNS.SetLimitText(LENGTH_IPV6_V1);
	m_edtBackDNS.SetLimitText(LENGTH_IPV6_V1);
	m_edtReserved.SetLimitText(64);
	m_edtReserved.EnableWindow(FALSE);

	m_Combo_Ipv6_Mode.SetItemData(m_Combo_Ipv6_Mode.AddString(GetTextByLan(_T("手动"),_T("Manual"))), 1);
	m_Combo_Ipv6_Mode.SetItemData(m_Combo_Ipv6_Mode.AddString(GetTextByLan(_T("DHCP"),_T("DHCP"))), 2);
	m_Combo_Ipv6_Mode.SetItemData(m_Combo_Ipv6_Mode.AddString(GetTextByLan(_T("路由公告"),_T("Route Announcemen"))), 3);
	m_Combo_Ipv6_Mode.SetCurSel(0);

	m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("不使能 自动获取"),_T("Not Enabled,Automatically Obtained"))), 0);
	m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("使能 手动配置"),_T("Enabled,Manual Configuration"))), 1);
	m_Combo_Ipv6_DnsEnable.SetCurSel(0);

	UI_UpdateDialog();

	return TRUE;
}

void CLS_LANIPV6Page::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;

	UI_UpdateLanNum();
	UI_UpdateIPV6();
	UI_UpdateIPV6AddrList();

}

void CLS_LANIPV6Page::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_LANIPV6Page::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_IPV6_LANNO,IDS_CONFIG_LAN_LANNO);
	SetDlgItemTextEx(IDC_STATIC_IPV6_IP,IDS_CONFIG_LAN_IP);
	SetDlgItemTextEx(IDC_STATIC_IPV6_PREFIXLEN,IDS_CONFIG_LAN_PREFIXLEN);
	SetDlgItemTextEx(IDC_STATIC_IPV6_GATEWAY,IDS_CONFIG_LAN_GATEWAY);
	SetDlgItemTextEx(IDC_STATIC_IPV6_DNS,IDS_CONFIG_LAN_DNS);
	SetDlgItemTextEx(IDC_STATIC_IPV6_BACKDNS,IDS_CONFIG_LAN_BACKDNS);
	SetDlgItemTextEx(IDC_STATIC_RESERVED,IDS_CONFIG_LAN_RESERVED);
	SetDlgItemTextEx(IDC_BUTTON_IPV6,IDS_SET);
	SetDlgItemText(IDC_STATIC_IPV6_DNSENABLE, GetTextByLan(_T("手动设置使能(DNS)"), _T("Manual Setting Enable(DNS)")));
	SetDlgItemText(IDC_STATIC_IPV6_MODE, GetTextByLan(_T("IPV6模式"), _T("IPV6 Mode")));
}

BOOL CLS_LANIPV6Page::UI_UpdateLanNum()
{
	if (m_iLogonID < 0)
		return FALSE;

	m_cboLanNo.ResetContent();
	int iLanNum = 0;
	int iRet = NetClient_GetLanParam(m_iLogonID, LAN_CMD_GET_LANNUM, &iLanNum);
	if (0 == iRet)
	{
		CString strLanNo;
		for (int i=0; i<iLanNum; i++)
		{
			strLanNo.Format("Lan%d",i+1);
			m_cboLanNo.AddString(strLanNo);
		}
		m_cboLanNo.SetCurSel(0);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetLanParam(IPV6 %d,%d)",m_iLogonID,iLanNum);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetLanParam(IPV6 %d,%d)",m_iLogonID,iLanNum);
	}
	return TRUE;
}

void CLS_LANIPV6Page::OnBnClickedButtonIpv6()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	LANPARAM_IPV6_V1 lIPV6 = {0};
	lIPV6.iSize = sizeof(LANPARAM_IPV6_V1);
	lIPV6.iLanNo = m_cboLanNo.GetCurSel();
	char cIP[LENGTH_IPV6_V1] = {0};				
	char cGateway[LENGTH_IPV6_V1] = {0}; 		
	char cDNS[LENGTH_IPV6_V1] = {0};			
	char cBackDNS[LENGTH_IPV6_V1] = {0};		
	char cReserved[LENGTH_IPV6_V1] = {0};
	int  iPrefixLen = 0;
	lIPV6.cReserved[0] = '0';
	iPrefixLen = GetDlgItemInt(IDC_EDIT_IPV6_PREFIXLEN);
	lIPV6.iPrefixLen = iPrefixLen;
	GetDlgItemText(IDC_EDIT_IPV6_IP, cIP, LENGTH_IPV6_V1);
	GetDlgItemText(IDC_EDIT_IPV6_GATEWAY, cGateway, LENGTH_IPV6_V1);
	GetDlgItemText(IDC_EDIT_IPV6_DNS, cDNS, LENGTH_IPV6_V1);
	GetDlgItemText(IDC_EDIT_IPV6_BACKDNS, cBackDNS, LENGTH_IPV6_V1);
	
	int iIpv6mode = m_Combo_Ipv6_Mode.GetItemData(m_Combo_Ipv6_Mode.GetCurSel());
	
	if (iIpv6mode == 1)//manual mode
	{
		if (IsValidIPv6(cIP) != 1)
		{
			AddLog(LOG_TYPE_MSG, "", "Change IP is invalid");
			return;
		}
		if (iPrefixLen<3 || lIPV6.iPrefixLen>127)
		{
			AddLog(LOG_TYPE_MSG, "", "Change PrefixLen is invalid");
			return;
		}
		if (IsValidIPv6(cGateway) != 1 && IsValidIPv6(cGateway) != 3 && strcmp(cGateway, "") != 0)
		{
			AddLog(LOG_TYPE_MSG, "", "Change Gateway is invalid");
			return;
		}
		if (IsValidIPv6(cDNS) != 1 && strcmp(cDNS, "") != 0)
		{
			AddLog(LOG_TYPE_MSG, "", "Change DNS is invalid");
			return;
		}
		if (IsValidIPv6(cBackDNS) != 1 && strcmp(cBackDNS, "") != 0 )
		{
			AddLog(LOG_TYPE_MSG, "", "Change BackDNS is invalid");
			return;
		}
	}
 	
	memcpy(lIPV6.cIP, cIP, sizeof(cIP));
	memcpy(lIPV6.cGateway, cGateway, sizeof(cGateway));
	memcpy(lIPV6.cDNS, cDNS, sizeof(cDNS));
	memcpy(lIPV6.cBackDNS, cBackDNS, sizeof(cBackDNS));
	lIPV6.iDnsEnable = m_Combo_Ipv6_DnsEnable.GetItemData(m_Combo_Ipv6_DnsEnable.GetCurSel());
	lIPV6.iIpv6mode = iIpv6mode;
	int iRet = NetClient_SetLanParam(m_iLogonID, LAN_CMD_SET_IPV6Ex, &lIPV6);
	if (0 == iRet)
	{
		iRet = MessageBox(GetTextEx(IDS_CONFIG_LAN_MESSAGETEXT), GetTextEx(IDS_CONFIG_PROMPT),MB_OKCANCEL);
		if (IDOK == iRet)
		{
			iRet = NetClient_Reboot(m_iLogonID);
		}
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetLanParam(IPV6 %d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetLanParam(IPV6 %d)",m_iLogonID);
	}
}

BOOL CLS_LANIPV6Page::UI_UpdateIPV6()
{
	if (m_iLogonID < 0)
		return FALSE;

	LANPARAM_IPV6_V1 lIPV6 = {0};
	lIPV6.iSize = sizeof(LANPARAM_IPV6_V1);
	lIPV6.iLanNo = m_cboLanNo.GetCurSel();
	int iRet = NetClient_GetLanParam(m_iLogonID, LAN_CMD_GET_IPV6Ex, &lIPV6);
	if (0 == iRet)
	{
		if (IPV6MODE_MANUAL == lIPV6.iIpv6mode)
		{
			m_Combo_Ipv6_DnsEnable.ResetContent();
			m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("使能 手动配置"),_T("Enabled,Manual Configuration"))), 1);
			m_Combo_Ipv6_DnsEnable.SetCurSel(0);
		}
		else
		{
			int iCurSel1 = m_Combo_Ipv6_DnsEnable.GetCurSel();
			m_Combo_Ipv6_DnsEnable.ResetContent();
			m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("不使能 自动获取"),_T("Not Enabled,Automatically Obtained"))), 0);
			m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("使能 手动配置"),_T("Enabled,Manual Configuration"))), 1);
			m_Combo_Ipv6_DnsEnable.SetCurSel(((iCurSel1 < 0) ? 0 : iCurSel1));
		}

		if (IPV6MODE_DHCP == lIPV6.iIpv6mode || IPV6MODE_ROUTING == lIPV6.iIpv6mode)
		{
			m_edtIP.EnableWindow(FALSE);
			m_edtPreFixLen.EnableWindow(FALSE);
			m_edtGateway.EnableWindow(FALSE);
		}
		else if (IPV6MODE_MANUAL == lIPV6.iIpv6mode)
		{
			m_edtIP.EnableWindow(TRUE);
			m_edtPreFixLen.EnableWindow(TRUE);
			m_edtGateway.EnableWindow(TRUE);
		}
		if (IPV6DNSENABLE_AUTO == lIPV6.iDnsEnable)
		{
			m_edtDNS.EnableWindow(FALSE);
			m_edtBackDNS.EnableWindow(FALSE);
		}
		else if (IPV6DNSENABLE_MANUAL == lIPV6.iDnsEnable)
		{
			m_edtDNS.EnableWindow(TRUE);
			m_edtBackDNS.EnableWindow(TRUE);
		}
		SetDlgItemText(IDC_EDIT_IPV6_IP,lIPV6.cIP);
		SetDlgItemInt(IDC_EDIT_IPV6_PREFIXLEN,lIPV6.iPrefixLen);
		SetDlgItemText(IDC_EDIT_IPV6_GATEWAY,lIPV6.cGateway);
		SetDlgItemText(IDC_EDIT_IPV6_DNS,lIPV6.cDNS);
		SetDlgItemText(IDC_EDIT_IPV6_BACKDNS,lIPV6.cBackDNS);
		SetDlgItemText(IDC_EDIT_RESERVED,lIPV6.cReserved);
		m_Combo_Ipv6_DnsEnable.SetCurSel(lIPV6.iDnsEnable);
		if (IPV6MODE_RESERVE == lIPV6.iIpv6mode)
		{
			m_Combo_Ipv6_Mode.SetCurSel(lIPV6.iIpv6mode);
		}
		else
		{
			m_Combo_Ipv6_Mode.SetCurSel(lIPV6.iIpv6mode -1);
		}
		/*OnCbnSelchangeComboIpv6Mode();
		OnCbnSelchangeComboIpv6Dnsenable();*/
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetLanParam(IPV6 %d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetLanParam(IPV6 %d)",m_iLogonID);
	}
	return TRUE;
}

void CLS_LANIPV6Page::OnCbnSelchangeComboIpv6Lanno()
{
	UI_UpdateIPV6();
	UI_UpdateIPV6AddrList();
}

void CLS_LANIPV6Page::OnCbnSelchangeComboIpv6Mode()
{
	// TODO: Add control notification handler code here
	int iTemp = m_Combo_Ipv6_Mode.GetItemData(m_Combo_Ipv6_Mode.GetCurSel());
	if (IPV6MODE_MANUAL == iTemp)
	{
		m_Combo_Ipv6_DnsEnable.ResetContent();
		m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("使能 手动配置"),_T("Enabled,Manual Configuration"))), 1);
		m_Combo_Ipv6_DnsEnable.SetCurSel(0);
	}
	else
	{
		int iCurSel1 = m_Combo_Ipv6_DnsEnable.GetCurSel();
		m_Combo_Ipv6_DnsEnable.ResetContent();
		m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("不使能 自动获取"),_T("Not Enabled,Automatically Obtained"))), 0);
		m_Combo_Ipv6_DnsEnable.SetItemData(m_Combo_Ipv6_DnsEnable.AddString(GetTextByLan(_T("使能 手动配置"),_T("Enabled,Manual Configuration"))), 1);
		m_Combo_Ipv6_DnsEnable.SetCurSel(((iCurSel1 < 0) ? 0 : iCurSel1));
	}

	if (IPV6MODE_DHCP == iTemp || IPV6MODE_ROUTING == iTemp)
	{
		m_edtIP.EnableWindow(FALSE);
		m_edtPreFixLen.EnableWindow(FALSE);
		m_edtGateway.EnableWindow(FALSE);
	}
	else if (IPV6MODE_MANUAL == iTemp)
	{
		m_edtIP.EnableWindow(TRUE);
		m_edtPreFixLen.EnableWindow(TRUE);
		m_edtGateway.EnableWindow(TRUE);
	}
	OnCbnSelchangeComboIpv6Dnsenable();
}



void CLS_LANIPV6Page::OnCbnSelchangeComboIpv6Dnsenable()
{
	// TODO: Add control notification handler code here
	int iTemp = m_Combo_Ipv6_DnsEnable.GetItemData(m_Combo_Ipv6_DnsEnable.GetCurSel());
	if (IPV6DNSENABLE_AUTO == iTemp)
	{
		m_edtDNS.EnableWindow(FALSE);
		m_edtBackDNS.EnableWindow(FALSE);
	}
	else if (IPV6DNSENABLE_MANUAL == iTemp)
	{
		m_edtDNS.EnableWindow(TRUE);
		m_edtBackDNS.EnableWindow(TRUE);
	}
}

void CLS_LANIPV6Page::OnBnClickedButtonIpv6Refresh()
{
	UI_UpdateIPV6AddrList();
}

int CLS_LANIPV6Page::UI_UpdateIPV6AddrList()
{
	if (m_iLogonID < 0)
	{
		return RET_FAILED;
	}
	
	int iBytesReturned = -1;
	IPV6AddrAndPrefix tList[IPV6_ADDRLIST_NUM_MAX*3] = {0};
	int iCopyNum = 0;
	int iOffSetNum = 0;
	
	IPV6AddrList tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iLanNo = m_cboLanNo.GetCurSel();
	tInfo.iIPMode = IPV6MODE_MANUAL;

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IPV6_ADDRLIST, m_iChannelNO, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (RET_SUCCESS == iRet)
	{
		iCopyNum = min(IPV6_ADDRLIST_NUM_MAX, tInfo.iIPListNum);
		memcpy(&tList, &tInfo.tIPInfo, iCopyNum*sizeof(IPV6AddrAndPrefix));
		iOffSetNum = iCopyNum;

	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig NET_CLIENT_IPV6_ADDRLIST(iIPMode=%d)",tInfo.iIPMode);
	}

	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSize = sizeof(tInfo);
	tInfo.iLanNo = m_cboLanNo.GetCurSel();
	tInfo.iIPMode = IPV6MODE_DHCP;

	int iRet1 = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IPV6_ADDRLIST, m_iChannelNO, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (RET_SUCCESS == iRet1)
	{
		iCopyNum = min(IPV6_ADDRLIST_NUM_MAX, tInfo.iIPListNum);
		memcpy(&tList[iOffSetNum].cIP, &tInfo.tIPInfo, iCopyNum*sizeof(IPV6AddrAndPrefix));
		iOffSetNum += iCopyNum;

	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig NET_CLIENT_IPV6_ADDRLIST(iIPMode=%d)",tInfo.iIPMode);
	}

	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSize = sizeof(tInfo);
	tInfo.iLanNo = m_cboLanNo.GetCurSel();
	tInfo.iIPMode = IPV6MODE_ROUTING;

	int iRet2 = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IPV6_ADDRLIST, m_iChannelNO, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (RET_SUCCESS == iRet2)
	{
		iCopyNum = min(IPV6_ADDRLIST_NUM_MAX, tInfo.iIPListNum);
		memcpy(&tList[iOffSetNum].cIP, &tInfo.tIPInfo, iCopyNum*sizeof(IPV6AddrAndPrefix));
		iOffSetNum += iCopyNum;

	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig NET_CLIENT_IPV6_ADDRLIST(iIPMode=%d)",tInfo.iIPMode);
	}

	if (RET_SUCCESS == iRet || RET_SUCCESS == iRet1 || RET_SUCCESS == iRet2)
	{
		m_ComboIpv6AddrList.ResetContent();
		
		for (int i=0; i<iOffSetNum && i<IPV6_ADDRLIST_NUM_MAX*3;i++)
		{
			CString cstrIPInfo;
			cstrIPInfo.Format("%s", tList[i].cIP);
			if (tList[i].iPrefixLen >0)
			{
				cstrIPInfo.AppendFormat("/%d",tList[i].iPrefixLen);
			}

			m_ComboIpv6AddrList.AddString(cstrIPInfo);
		}

		if (m_ComboIpv6AddrList.GetCount()>0)
		{
			m_ComboIpv6AddrList.SetCurSel(0);
		}

		return RET_SUCCESS;
	}
	else
	{
		return RET_FAILED;
	}

}


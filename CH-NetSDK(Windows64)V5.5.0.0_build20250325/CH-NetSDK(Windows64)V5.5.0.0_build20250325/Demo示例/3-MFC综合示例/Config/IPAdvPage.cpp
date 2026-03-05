// IPAdvPage.cpp : implementation file
//

#include "stdafx.h"
#include "IPAdvPage.h"


// CLS_IPAdvPage dialog

IMPLEMENT_DYNAMIC(CLS_IPAdvPage, CDialog)

CLS_IPAdvPage::CLS_IPAdvPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_IPAdvPage::IDD, pParent)
{
	m_iLogonID = -1;
}

CLS_IPAdvPage::~CLS_IPAdvPage()
{
}

void CLS_IPAdvPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_FILTER_IP, m_edtFilterIP);
	DDX_Control(pDX, IDC_EDIT_FILTER_MASK, m_edtFilterMask);
	DDX_Control(pDX, IDC_COMBO_FILTER_MODE, m_cboFilterType);
	DDX_Control(pDX, IDC_EDIT_IPSTRING, m_edtIpString);
}


BEGIN_MESSAGE_MAP(CLS_IPAdvPage, public CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_FILTER_SET, &CLS_IPAdvPage::OnBnClickedButtonFilterSet)
	ON_BN_CLICKED(IDC_BUTTON1, &CLS_IPAdvPage::OnBnClickedButton1)
END_MESSAGE_MAP()


// CLS_IPAdvPage message handlers
BOOL CLS_IPAdvPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();

	m_edtFilterIP.SetLimitText(15);
	m_edtFilterMask.SetLimitText(15);
	return TRUE;
}

void CLS_IPAdvPage::OnChannelChanged( int _iLogonID,int /*_iChannelNo*/,int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;

	UI_UpdateIpInfo();
}

void CLS_IPAdvPage::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_IPAdvPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_FILTER, IDS_CONFIG_IP_FILTER);
	SetDlgItemTextEx(IDC_STATIC_FILTER_IP, IDS_CONFIG_IP_FILTER_IP);
	SetDlgItemTextEx(IDC_STATIC_FILTER_MASK, IDS_CONFIG_IP_FILTER_MASK);
	SetDlgItemTextEx(IDC_BUTTON_FILTER_SET, IDS_CONFIG_IP_FILTER_SET);

	SetDlgItemTextEx(IDC_STATIC_FILTER_TYPE, IDS_CONFIG_IPADV_FILTERTYPE);
	SetDlgItemTextEx(IDC_STATIC_FILTER_IPNO, IDS_CONFIG_IPADV_FILTERIPNO);
	SetDlgItemTextEx(IDC_STATIC_IPSTRING, IDS_CONFIG_IPADV_FILTERIPSTRING);
	InsertString(m_cboFilterType,0,IDS_CONFIG_ADV_NOFORBID);
	InsertString(m_cboFilterType, 1, IDS_CONFIG_ADV_FORBIDIP);
	InsertString(m_cboFilterType, 2, IDS_CONFIG_ADV_ALLOWIP);
}


BOOL CLS_IPAdvPage::UI_UpdateIpInfo()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return FALSE;
	}
	
	int iBytesReturned = 0;

	TIpv6Filter tIpFilter = {0};
	tIpFilter.iSize = sizeof(TIpv6Filter);
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IPV6_FILTER, 0x7FFFFFFF, &tIpFilter, sizeof(tIpFilter), &iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig faied! Logon id(%d)", m_iLogonID);
		return FALSE;
	}
	else
	{

		SetDlgItemText(IDC_EDIT_FILTER_IP, tIpFilter.pcFilterIp[0]);
		m_cboFilterType.SetCurSel(tIpFilter.iFilterType);
		SetDlgItemInt(IDC_EDIT_FILTER_IPNO, tIpFilter.iIpNum);
		CString cstrIpString;
		CString cstrTmpIp;
		for (int i = 0; i < tIpFilter.iIpNum; i++)
		{

			cstrTmpIp.Format("%s", tIpFilter.pcFilterIp[i]);
			if (0 == i)
			{
				cstrIpString += cstrTmpIp;
			}
			else
			{
				cstrIpString += (CString)"," + cstrTmpIp;
			}

		}
		SetDlgItemText(IDC_EDIT_IPSTRING, cstrIpString);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig success! Logon id(%d)", m_iLogonID);
	}

	//TIpFilter tIpFilter = {0};
	//tIpFilter.iSize = sizeof(TIpFilter);
	//int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IP_FILTER, 0x7FFFFFFF, &tIpFilter, sizeof(tIpFilter), &iBytesReturned);
	//if (iRet < 0)
	//{
	//	AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig faied! Logon id(%d)", m_iLogonID);
	//	return FALSE;
	//}
	//else
	//{

	//	SetDlgItemText(IDC_EDIT_FILTER_IP, tIpFilter.cFilterIp[0]);
	//	SetDlgItemText(IDC_EDIT_FILTER_MASK, tIpFilter.cSubMask);
	//	m_cboFilterType.SetCurSel(tIpFilter.iFilterType);
	//	SetDlgItemInt(IDC_EDIT_FILTER_IPNO, tIpFilter.iIpNum);
	//	CString cstrIpString;
	//	CString cstrTmpIp;
	//	for (int i = 0; i < tIpFilter.iIpNum; i++)
	//	{

	//		cstrTmpIp.Format("%s", tIpFilter.cFilterIp[i]);
	//		if (0 == i)
	//		{   
	//			cstrIpString += cstrTmpIp;
	//		}
	//		else
	//		{
	//			cstrIpString += (CString)"," + cstrTmpIp;
	//		}
	//		
	//	}
	//	SetDlgItemText(IDC_EDIT_IPSTRING, cstrIpString);
	//	AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig success! Logon id(%d)", m_iLogonID);
	//}
	IrrigationServerInfo tInfo = {0};
	tInfo.iSize = (int)sizeof(IrrigationServerInfo);
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONSERVERINFO,0,&tInfo, tInfo.iSize, &iBytesReturned);
	if (RET_SUCCESS == iRet)
	{
		GetDlgItem(IDC_EDIT_IP1)->SetWindowText(tInfo.tServerInfo[0].pcIrrigationIP);
		CString strPort = "";
		strPort.Format("%d", tInfo.tServerInfo[0].iIrrigationPort);
		GetDlgItem(IDC_EDIT_PORT1)->SetWindowText(strPort);
	    GetDlgItem(IDC_EDIT_ADDRESS1)->SetWindowText(tInfo.tServerInfo[0].pcStationAddress);

		GetDlgItem(IDC_EDIT_IP2)->SetWindowText(tInfo.tServerInfo[1].pcIrrigationIP);
		strPort.Format("%d", tInfo.tServerInfo[1].iIrrigationPort);
		GetDlgItem(IDC_EDIT_PORT2)->SetWindowText(strPort);
		GetDlgItem(IDC_EDIT_ADDRESS2)->SetWindowText(tInfo.tServerInfo[1].pcStationAddress);

		GetDlgItem(IDC_EDIT_IP3)->SetWindowText(tInfo.tServerInfo[2].pcIrrigationIP);
		strPort.Format("%d", tInfo.tServerInfo[2].iIrrigationPort);
		GetDlgItem(IDC_EDIT_PORT3)->SetWindowText(strPort);
		GetDlgItem(IDC_EDIT_ADDRESS3)->SetWindowText(tInfo.tServerInfo[2].pcStationAddress);

		GetDlgItem(IDC_EDIT_IP4)->SetWindowText(tInfo.tServerInfo[3].pcIrrigationIP);
		strPort.Format("%d", tInfo.tServerInfo[3].iIrrigationPort);
		GetDlgItem(IDC_EDIT_PORT4)->SetWindowText(strPort);
		GetDlgItem(IDC_EDIT_ADDRESS4)->SetWindowText(tInfo.tServerInfo[3].pcStationAddress);
	}
	else
	{
		GetDlgItem(IDC_EDIT_IP1)->SetWindowText("");
		GetDlgItem(IDC_EDIT_PORT1)->SetWindowText("");
		GetDlgItem(IDC_EDIT_ADDRESS1)->SetWindowText("");

		GetDlgItem(IDC_EDIT_IP2)->SetWindowText("");
		GetDlgItem(IDC_EDIT_PORT2)->SetWindowText("");
		GetDlgItem(IDC_EDIT_ADDRESS2)->SetWindowText("");

		GetDlgItem(IDC_EDIT_IP3)->SetWindowText("");
		GetDlgItem(IDC_EDIT_PORT3)->SetWindowText("");
		GetDlgItem(IDC_EDIT_ADDRESS3)->SetWindowText("");

		GetDlgItem(IDC_EDIT_IP4)->SetWindowText("");
		GetDlgItem(IDC_EDIT_PORT4)->SetWindowText("");
		GetDlgItem(IDC_EDIT_ADDRESS4)->SetWindowText("");
	}
	return TRUE;
}

void CLS_IPAdvPage::OnBnClickedButtonFilterSet()
{
	// TODO: Add your control notification handler code here
	//0 means no list, 1 means blacklist, 2 means whitelist
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	}
// 	CString strIP;
// 	m_edtFilterIP.GetWindowText(strIP);
// 	CString strMask;
// 	m_edtFilterMask.GetWindowText(strMask);
// 	if (IsValidIP(strIP) != 1 || IsValidIP(strMask) != 3)
// 	{
// 		MessageBox("Change IP,IP or Mask is invalid", "", MB_OK|MB_TOPMOST);
// 		return;
// 	}

	//CString cstrIpString;
	//TIpFilter tIpFilter = {0};
	//tIpFilter.iSize = sizeof(TIpFilter);
	//CString strTemp = "255.255.255.255";
	//strcpy_s(tIpFilter.cIp, (LPSTR)(LPCTSTR)strTemp);
	//strcpy_s(tIpFilter.cSubMask,(LPSTR)(LPCTSTR)strTemp);
	//tIpFilter.iFilterType = m_cboFilterType.GetCurSel();
	//tIpFilter.iIpNum = GetDlgItemInt(IDC_EDIT_FILTER_IPNO);
	//string strFilterIp[MAX_IP_FILTER_NUM];
	//m_edtIpString.GetWindowText(cstrIpString);
	//
	//string strSourceString;
	//strSourceString = cstrIpString;
	////SplitStringToArray((LPSTR)(LPCTSTR)cstrIpString, ",", strFilterIp, MAX_IP_FILTER_NUM);
	//SplitStringToArray(strSourceString, ",", strFilterIp, MAX_IP_FILTER_NUM);

	//for (int i = 0; i < tIpFilter.iIpNum; i++)
	//{
	//	memcpy(tIpFilter.cFilterIp[i], strFilterIp[i].c_str(), strFilterIp[i].length());
	//}
	//int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IP_FILTER, 0x7FFFFFFF, &tIpFilter, sizeof(tIpFilter));
	//if (iRet < 0)
	//{
	//	PDEVICE_INFO Device = FindDevice((m_iLogonID));
	//	if (Device)
	//	{
	//		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%s, %d)", Device->cIP, m_iLogonID);
	//	}
	//}

	CString cstrIpString;
	TIpv6Filter tIpFilter = {0};
	tIpFilter.iSize = sizeof(TIpv6Filter);
	tIpFilter.iFilterType = m_cboFilterType.GetCurSel();
	tIpFilter.iIpNum = GetDlgItemInt(IDC_EDIT_FILTER_IPNO);
	string strFilterIp[MAX_IP_FILTER_NUM];
	m_edtIpString.GetWindowText(cstrIpString);

	string strSourceString;
	strSourceString = cstrIpString;
	//SplitStringToArray((LPSTR)(LPCTSTR)cstrIpString, ",", strFilterIp, MAX_IP_FILTER_NUM);
	SplitStringToArray(strSourceString, ",", strFilterIp, MAX_IP_FILTER_NUM);

	for (int i = 0; i < tIpFilter.iIpNum; i++)
	{
		memcpy(tIpFilter.pcFilterIp[i], strFilterIp[i].c_str(), strFilterIp[i].length());
	}
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IPV6_FILTER, 0x7FFFFFFF, &tIpFilter, tIpFilter.iSize);
	if (iRet < 0)
	{
		PDEVICE_INFO Device = FindDevice((m_iLogonID));
		if (Device)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%s, %d)", Device->cIP, m_iLogonID);
		}
	}
}

void CLS_IPAdvPage::OnBnClickedButton1()
{
	// TODO: Add your control notification handler code here
	int iRet = RET_FAILED;
	IrrigationServerInfo tInfo = {0};
	tInfo.iSize = (int)sizeof(tInfo);
	CString strIp = "",strPort = "",strAddress = "";

	for (int i = 0; i < 4 ;i++)
	{
		tInfo.tServerInfo[i].iSize = (int)sizeof(IrriServerInfo);
	}
	GetDlgItem(IDC_EDIT_IP1)->GetWindowText(strIp);
	memcpy_s(tInfo.tServerInfo[0].pcIrrigationIP, LEN_32, (LPSTR)(LPCSTR)strIp, LEN_32);

	GetDlgItem(IDC_EDIT_PORT1)->GetWindowText(strPort);
	tInfo.tServerInfo[0].iIrrigationPort = _ttoi(strPort);

	GetDlgItem(IDC_EDIT_ADDRESS1)->GetWindowText(strAddress);
	memcpy_s(tInfo.tServerInfo[0].pcStationAddress, LEN_32, (LPSTR)(LPCSTR)strAddress, LEN_32);

	GetDlgItem(IDC_EDIT_IP2)->GetWindowText(strIp);
	memcpy_s(tInfo.tServerInfo[1].pcIrrigationIP, LEN_32, (LPSTR)(LPCSTR)strIp, LEN_32);

	GetDlgItem(IDC_EDIT_ADDRESS2)->GetWindowText(strAddress);
	memcpy_s(tInfo.tServerInfo[1].pcStationAddress, LEN_32, (LPSTR)(LPCSTR)strAddress, LEN_32);

	GetDlgItem(IDC_EDIT_PORT2)->GetWindowText(strPort);
	tInfo.tServerInfo[1].iIrrigationPort = _ttoi(strPort);

	GetDlgItem(IDC_EDIT_IP3)->GetWindowText(strIp);
	memcpy_s(tInfo.tServerInfo[2].pcIrrigationIP, LEN_32, (LPSTR)(LPCSTR)strIp, LEN_32);

	GetDlgItem(IDC_EDIT_ADDRESS3)->GetWindowText(strAddress);
	memcpy_s(tInfo.tServerInfo[2].pcStationAddress, LEN_32, (LPSTR)(LPCSTR)strAddress, LEN_32);

	GetDlgItem(IDC_EDIT_PORT3)->GetWindowText(strPort);
	tInfo.tServerInfo[2].iIrrigationPort = _ttoi(strPort);

	GetDlgItem(IDC_EDIT_IP4)->GetWindowText(strIp);
	memcpy_s(tInfo.tServerInfo[3].pcIrrigationIP, LEN_32, (LPSTR)(LPCSTR)strIp, LEN_32);

	GetDlgItem(IDC_EDIT_ADDRESS4)->GetWindowText(strAddress);
	memcpy_s(tInfo.tServerInfo[3].pcStationAddress, LEN_32, (LPSTR)(LPCSTR)strAddress, LEN_32);

	GetDlgItem(IDC_EDIT_PORT4)->GetWindowText(strPort);
	tInfo.tServerInfo[3].iIrrigationPort = _ttoi(strPort);

	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONSERVERINFO, 0, &tInfo, tInfo.iSize);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig NET_CLIENT_IRRIGATIONSERVERINFO(%d,%d)",m_iLogonID, iRet);
	}
	else
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig NET_CLIENT_IRRIGATIONSERVERINFO(%d,%d)",m_iLogonID, iRet);
}

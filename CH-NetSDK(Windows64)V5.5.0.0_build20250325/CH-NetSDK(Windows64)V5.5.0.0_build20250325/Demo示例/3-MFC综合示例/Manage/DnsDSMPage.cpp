// DnsDSMPage.cpp : implementation file
//

#include "stdafx.h"
#include "DnsDSMPage.h"
#include "../LogonView.h"

#define MSG_DNS		(WM_USER + 1103)

HWND CLS_DnsDSMPage::s_hWnd = NULL;

// CLS_DnsDSMPage dialog

IMPLEMENT_DYNAMIC(CLS_DnsDSMPage, CDialog)

CLS_DnsDSMPage::CLS_DnsDSMPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DnsDSMPage::IDD, pParent)
{
	m_blUseNslook = FALSE;
	m_iRegID = -1;
	memset(m_cRegUser,0,sizeof(m_cRegUser));
	memset(m_cRegPwd,0,sizeof(m_cRegPwd));
}

CLS_DnsDSMPage::~CLS_DnsDSMPage()
{

}

void CLS_DnsDSMPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_DSM_DNS, m_lvDNS);
	DDX_Control(pDX, IDC_CBO_DSM_DNS_QUERY_TYPE, m_cboQueryType);
	DDX_Control(pDX, IDC_CBO_DSM_DNS_PAGE, m_cboPage);
}


BEGIN_MESSAGE_MAP(CLS_DnsDSMPage, CLS_BasePage)
	ON_WM_DESTROY()
	ON_MESSAGE(MSG_DNS,&CLS_DnsDSMPage::OnDnsMsg)
	ON_BN_CLICKED(IDC_BTN_DSM_DNS_REFRESH, &CLS_DnsDSMPage::OnBnClickedBtnDsmDnsRefresh)
	ON_CBN_SELCHANGE(IDC_CBO_DSM_DNS_PAGE, &CLS_DnsDSMPage::OnCbnSelchangeCboDsmDnsPage)
	ON_NOTIFY(NM_CLICK, IDC_LIST_DSM_DNS, &CLS_DnsDSMPage::OnNMClickListDsmDns)
	ON_CBN_SELCHANGE(IDC_CBO_DSM_DNS_QUERY_TYPE, &CLS_DnsDSMPage::OnCbnSelchangeCboDsmDnsQueryType)
	ON_BN_CLICKED(IDC_BTN_DSM_DNS_QUERY, &CLS_DnsDSMPage::OnBnClickedBtnDsmDnsQuery)
	ON_NOTIFY(NM_DBLCLK, IDC_LIST_DSM_DNS, &CLS_DnsDSMPage::OnNMDblclkListDsmDns)
END_MESSAGE_MAP()


// CLS_DnsDSMPage message handlers

BOOL CLS_DnsDSMPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	s_hWnd = this->GetSafeHwnd();
	((CEdit*)GetDlgItem(IDC_EDIT_DSM_DNS_ID))->SetLimitText(31);
	m_lvDNS.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);

	UI_UpdateDialog();

	m_cboQueryType.SetCurSel(1);

	m_lvDNS.SetSortType(2,SORT_TYPE_IP);
	m_lvDNS.SetSortType(3,SORT_TYPE_IP);
	m_lvDNS.SetSortType(4,SORT_TYPE_INT);
	m_lvDNS.SetSortType(5,SORT_TYPE_INT);
	m_lvDNS.SetSortType(6,SORT_TYPE_INT);
	m_lvDNS.SetSortType(7,SORT_TYPE_INT);
	
	return TRUE; 
}

void CLS_DnsDSMPage::OnDestroy()
{
	CLS_BasePage::OnDestroy();

	// TODO: Add your message handler code here
}

void CLS_DnsDSMPage::OnChannelChanged( int _iRegID,int _iRegUser,int _iRegPwd )
{
	m_iRegID = _iRegID;
	if (0 != _iRegUser)
	{
		memcpy_s(m_cRegUser,sizeof(m_cRegUser),(char*)_iRegUser,sizeof(m_cRegUser));
	}
	if (0 != _iRegPwd)
	{
		memcpy_s(m_cRegPwd,sizeof(m_cRegPwd),(char*)_iRegPwd,sizeof(m_cRegPwd));
	}
}

void CLS_DnsDSMPage::UI_UpdateDialog()
{
	InsertColumn(m_lvDNS,0,IDS_MNG_ADMIN_FACTORY_ID,LVCFMT_LEFT,170);
	InsertColumn(m_lvDNS,1,IDS_MNG_NVS_NAME,LVCFMT_LEFT,120);	
	InsertColumn(m_lvDNS,2,IDS_MNG_DNS_LAN_IP,LVCFMT_LEFT,120);
	InsertColumn(m_lvDNS,3,IDS_MNG_DNS_WAN_IP,LVCFMT_LEFT,120);
	InsertColumn(m_lvDNS,4,IDS_MNG_ADMIN_CHANNEL_NUM,LVCFMT_LEFT,60);
	InsertColumn(m_lvDNS,5,GetTextByLan(_T("µÇÂ½¶Ë¿Ú"), _T("LogonPort")),LVCFMT_LEFT,70);
	InsertColumn(m_lvDNS,6,GetTextByLan(_T("Http¶Ë¿Ú"), _T("HttpPort")),LVCFMT_LEFT,70);
	InsertColumn(m_lvDNS,7,GetTextByLan(_T("Rtmp¶Ë¿Ú"), _T("RtmpPort")),LVCFMT_LEFT,70);
	
	InsertString(m_cboQueryType,0,IDS_MNG_ADMIN_FACTORY_ID);
	InsertString(m_cboQueryType,1,IDS_MNG_NVS_NAME);

	SetDlgItemTextEx(IDC_STATIC_DSM_DNS_COUNT,IDS_MNG_NVS_COUNT);
	SetDlgItemTextEx(IDC_STATIC_DSM_DNS_PAGE,IDS_MNG_DNS_PAGE_NO);
	SetDlgItemTextEx(IDC_BTN_DSM_DNS_REFRESH,IDS_MNG_ADMIN_REFRESH);
	SetDlgItemTextEx(IDC_BTN_DSM_DNS_QUERY,IDS_MNG_NVS_QUERY);

}

void CLS_DnsDSMPage::OnBnClickedBtnDsmDnsRefresh()
{
	m_cboPage.ResetContent();
	int iCount = 0;
	int iRet = NSLook_GetCount(m_iRegID,m_cRegUser,m_cRegPwd,&iCount,TYPE_DNS);
	if(0 == iRet)
	{
		int iPages = (iCount+REG_PAGE_SIZE-1) / REG_PAGE_SIZE;
		for (int i = 1; i <= iPages; ++i)
		{
			m_cboPage.AddString(IntToString(i));
		}
		m_cboPage.SetCurSel(0);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","(%d)NSLook_GetCount(%d,%s,%s,,TYPE_DNS)"
			,iRet,m_iRegID,m_cRegUser,m_cRegPwd);
	}
	SetDlgItemInt(IDC_EDIT_DSM_DNS_COUNT,iCount);
	Refresh();
}

void CLS_DnsDSMPage::Refresh()
{
	m_lvDNS.DeleteAllItems();
	int iPage = m_cboPage.GetCurSel();
	if (iPage < 0)
	{
		return;
	}

	int iRet = NSLook_GetList(m_iRegID,m_cRegUser,m_cRegPwd,iPage,&CLS_DnsDSMPage::DnsNotify,NULL,TYPE_DNS);
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL,"","(%d)NSLook_GetList(%d,%s,%s,%d,,,TYPE_DNS)"
			,iRet,m_iRegID,m_cRegUser,m_cRegPwd,iPage);
	}
}

int __stdcall CLS_DnsDSMPage::DnsNotify( int _iCount,REG_DNS *_pDNS )
{
	__try
	{
		if (_pDNS)
		{
			LRESULT lRet = SendMessageTimeout(s_hWnd,MSG_DNS,(WPARAM)_pDNS,(LPARAM)_iCount,SMTO_NORMAL,1000,NULL);
// 			if (0 == lRet)
// 			{
// 				AddLog(LOG_TYPE_MSG,"","DnsNotify(%#x,%d,%#x)",_pDNS,_iCount,GetLastError());
// 			}
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{
		//AddLog(LOG_TYPE_MSG,"","DnsNotify exception");
	}

	return 0;
}

void CLS_DnsDSMPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();
}

LRESULT CLS_DnsDSMPage::OnDnsMsg( WPARAM wParam, LPARAM lParam )
{
	__try
	{
		DsmNvsRegInfoEx tNvsEx = {0};
		REG_DNS* ptDnsArray = (REG_DNS*)wParam;
		REG_DNS* ptDnsSingle = NULL;
		int iCount = (int)lParam;
		for (int i = 0 ; i < iCount; ++i)
		{
			ptDnsSingle = ptDnsArray + i;
			strcpy(tNvsEx.cFactoryID, ptDnsSingle->m_stDNSInfo.stNvs.cFactoryID);
			strcpy(tNvsEx.cNvsName, ptDnsSingle->m_stDNSInfo.stNvs.cNvsName);
			strcpy(tNvsEx.cNvsIP, ptDnsSingle->m_stDNSInfo.stNvs.cNvsIP);
			strcpy(tNvsEx.cWanIp, ptDnsSingle->m_stDNSInfo.m_stReserve.m_cReserved1);
			tNvsEx.iChanNum = ptDnsSingle->m_stDNSInfo.m_iChannel;
			tNvsEx.iTcpWanPort = ptDnsSingle->m_stDNSInfo.m_iPort;
			AddOneDnsItem(&tNvsEx);
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{
		AddLog(LOG_TYPE_MSG,"","AddDns exception");
	}

	return 0;
}

void CLS_DnsDSMPage::AddOneDnsItem(DsmNvsRegInfoEx* _ptDns)
{
	int iItem = -1;
	LVFINDINFO info;
	info.flags = LVFI_STRING;
	info.psz = _ptDns->cFactoryID;
	while ((iItem = m_lvDNS.FindItem(&info,iItem)) != -1)
	{
		CString strIP = m_lvDNS.GetItemText(iItem,3);
		if (0 == strIP.CompareNoCase(_ptDns->cNvsIP))
		{
			return;
		}
	}

	iItem = m_lvDNS.GetItemCount();
	m_lvDNS.InsertItem(iItem,_ptDns->cFactoryID);
	m_lvDNS.SetItemText(iItem,1,_ptDns->cNvsName);	
	m_lvDNS.SetItemText(iItem,2,_ptDns->cNvsIP);
	m_lvDNS.SetItemText(iItem,3,_ptDns->cWanIp);
	m_lvDNS.SetItemText(iItem,4,IntToString(_ptDns->iChanNum));
	m_lvDNS.SetItemText(iItem,5,IntToString(_ptDns->iTcpWanPort));
	m_lvDNS.SetItemText(iItem,6,IntToString(_ptDns->iHttpWanPort));
	m_lvDNS.SetItemText(iItem,7,IntToString(_ptDns->iRtmpWanPort));
}

void CLS_DnsDSMPage::OnCbnSelchangeCboDsmDnsPage()
{
	Refresh();
}

void CLS_DnsDSMPage::OnNMClickListDsmDns(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItem = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	
	if (pNMItem->iItem < 0)
	{
		return;
	}
	int iSubItem = m_cboQueryType.GetCurSel();
	CString strQueryData = m_lvDNS.GetItemText(pNMItem->iItem,iSubItem);
	SetDlgItemText(IDC_EDIT_DSM_DNS_ID,strQueryData);

	*pResult = 0;
}

void CLS_DnsDSMPage::OnCbnSelchangeCboDsmDnsQueryType()
{
	int iItem = m_lvDNS.GetNextItem(-1,LVNI_SELECTED);
	int iSubItem = m_cboQueryType.GetCurSel();
	CString strQueryData = m_lvDNS.GetItemText(iItem,iSubItem);
	SetDlgItemText(IDC_EDIT_DSM_DNS_ID,strQueryData);
}

void CLS_DnsDSMPage::OnBnClickedBtnDsmDnsQuery()
{
	CString strQueryData;
	GetDlgItemText(IDC_EDIT_DSM_DNS_ID,strQueryData);
	if (strQueryData.GetLength() <= 0)
	{
		CString strQueryType;
		m_cboQueryType.GetWindowText(strQueryType);
		AddLog(LOG_TYPE_MSG,"","%s is empty",strQueryType);
		return;
	}
	
	if (m_blUseNslook) {
		NslookDsmDnsQuery(strQueryData);
	} else {
		NvssdkDsmDnsQuery(strQueryData);
	}
}

void CLS_DnsDSMPage::NslookDsmDnsQuery(CString _cstrQueryData)
{
	REG_DNS regDNS = {0};
	switch(m_cboQueryType.GetCurSel())
	{
	case 0:
		strcpy_s(regDNS.m_stDNSInfo.stNvs.cFactoryID,sizeof(regDNS.m_stDNSInfo.stNvs.cFactoryID),(LPSTR)(LPCTSTR)_cstrQueryData);
		break;
	case 1:
		strcpy_s(regDNS.m_stDNSInfo.stNvs.cNvsName,sizeof(regDNS.m_stDNSInfo.stNvs.cNvsName),(LPSTR)(LPCTSTR)_cstrQueryData);
		break;
	default:
		AddLog(LOG_TYPE_MSG,"","other query type(%d)",_cstrQueryData.GetLength());
		return;
	}

	strcpy_s(regDNS.m_stDNSInfo.m_cUserName,sizeof(m_cRegUser),m_cRegUser);
	strcpy_s(regDNS.m_stDNSInfo.m_cPwd,sizeof(m_cRegPwd),m_cRegPwd);

	m_lvDNS.DeleteAllItems();
	int iRet = NSLook_Query(m_iRegID,&regDNS,NULL,TYPE_DNS);
	if(0 == iRet)
	{
		DsmNvsRegInfoEx tNvsEx = {0};
		strcpy(tNvsEx.cFactoryID, regDNS.m_stDNSInfo.stNvs.cFactoryID);
		strcpy(tNvsEx.cNvsName, regDNS.m_stDNSInfo.stNvs.cNvsName);
		strcpy(tNvsEx.cNvsIP, regDNS.m_stDNSInfo.stNvs.cNvsIP);
		strcpy(tNvsEx.cWanIp, regDNS.m_stDNSInfo.m_stReserve.m_cReserved1);
		tNvsEx.iChanNum = regDNS.m_stDNSInfo.m_iChannel;
		tNvsEx.iTcpWanPort = regDNS.m_stDNSInfo.m_iPort;
		AddOneDnsItem(&tNvsEx);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","(%d)NSLook_Query(%d,%#x,NULL,TYPE_DNS)"
			,iRet,m_iRegID,&regDNS);
	}
}

void CLS_DnsDSMPage::NvssdkDsmDnsQuery(CString _cstrQueryData)
{
	int iRet = RET_FAILED;
	m_lvDNS.DeleteAllItems();
	DsmNvsRegInfoEx tNvsEx = {0};
	switch(m_cboQueryType.GetCurSel())
	{
	case 0:
		{
			strcpy_s(tNvsEx.cFactoryID,sizeof(tNvsEx.cFactoryID),(LPSTR)(LPCTSTR)_cstrQueryData);
			iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_REGNVSBYID_WITHREG, &tNvsEx, sizeof(DsmNvsRegInfoEx));
			if (RET_SUCCESS == iRet) {
				AddOneDnsItem(&tNvsEx);	
			} else {
				AddLog(LOG_TYPE_FAIL,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_REGNVSBYID_WITHREG fail!iRet=%d", iRet);
			}
		}
		break;
	case 1:
		{
			strcpy_s(tNvsEx.cNvsName,sizeof(tNvsEx.cNvsName),(LPSTR)(LPCTSTR)_cstrQueryData);
			iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_REGNVSBYDOMAINNAME, &tNvsEx, sizeof(DsmNvsRegInfoEx));
			if (RET_SUCCESS == iRet) {
				AddOneDnsItem(&tNvsEx);	
			} else {
				AddLog(LOG_TYPE_FAIL,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_REGNVSBYDOMAINNAME fail!iRet=%d", iRet);
			}
		}
		break;
	default:
		AddLog(LOG_TYPE_MSG,"","other query type(%d)",_cstrQueryData.GetLength());
	}
}

void CLS_DnsDSMPage::OnNMDblclkListDsmDns(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItem = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	
	DEVICE_INFO tDevInit = {0};
	m_lvDNS.GetItemText(pNMItem->iItem,0,tDevInit.cID,sizeof(tDevInit.cID));
	m_lvDNS.GetItemText(pNMItem->iItem,2,tDevInit.cIP,sizeof(tDevInit.cIP));
	m_lvDNS.GetItemText(pNMItem->iItem,3,tDevInit.cProxy,sizeof(tDevInit.cProxy));
	CString strPort = m_lvDNS.GetItemText(pNMItem->iItem,5);
	tDevInit.iPort = atoi((LPSTR)(LPCTSTR)strPort);
//#ifdef _DEBUG
		strcpy_s(tDevInit.cUserName,sizeof(tDevInit.cUserName),"Admin");
		strcpy_s(tDevInit.cPassword,sizeof(tDevInit.cPassword),"1111");
//#endif

	CLS_LogonView logon(&tDevInit);
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

	int iLogonID = NetClient_LogonEx(pDevLogon->cProxy,pDevLogon->cIP,pDevLogon->cUserName,pDevLogon->cPassword,pDevLogon->cID,pDevLogon->iPort,"UTF-8");
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
			AddLog(LOG_TYPE_MSG,"","AddDevice(%d) Can not add device",iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,pDevLogon->cIP,"NetClient_LogonEx(%s,%s,,,%s,,)",pDevLogon->cProxy,pDevLogon->cIP,pDevLogon->cID);
	}

	*pResult = 0;
}

CString CLS_DnsDSMPage::GetNvsType(int _iNvsType)
{
	CString strNvsType;
	switch(_iNvsType)
	{
	case 0:
		strNvsType.Format(_T("%s"),"T");
		break;
	case 1:
		strNvsType.Format(_T("%s"),"S");
		break;
	case 2:
		strNvsType.Format(_T("%s"),"T+");
		break;
	default:
		strNvsType.Format(_T("%d"),_iNvsType);
		break;
	}
	return strNvsType;
}

void CLS_DnsDSMPage::SetRegisterInfo(RegisterInfo* _ptInfo)
{
	if (NULL == _ptInfo) {
		return;
	}

	m_blUseNslook = _ptInfo->blUseNslook;
	strcpy_s(m_cRegUser, sizeof(m_cRegUser), _ptInfo->cRegUser);
	strcpy_s(m_cRegPwd, sizeof(m_cRegPwd), _ptInfo->cRegPwd);
}


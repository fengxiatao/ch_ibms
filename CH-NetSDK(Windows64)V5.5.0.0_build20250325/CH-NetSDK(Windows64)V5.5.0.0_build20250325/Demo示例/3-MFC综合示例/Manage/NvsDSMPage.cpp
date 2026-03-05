// NvsDSMPage.cpp : implementation file
//

#include "stdafx.h"
#include "NvsDSMPage.h"
#include "../LogonView.h"

#define NSLOOK_MSG_NVS		(WM_USER + 1102)
#define NVSSDK_MSG_NVS		(WM_USER + 1103)

HWND CLS_NvsDSMPage::s_hWnd = NULL;

// CLS_NvsDSMPage dialog

IMPLEMENT_DYNAMIC(CLS_NvsDSMPage, CDialog)

CLS_NvsDSMPage::CLS_NvsDSMPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_NvsDSMPage::IDD, pParent)
{
	m_blUseNslook = FALSE;
	m_iRegID = -1;
	memset(m_cRegIP,0,sizeof(m_cRegIP));
	m_iRegPort = 0;
	memset(m_cRegUser,0,sizeof(m_cRegUser));
	memset(m_cRegPwd,0,sizeof(m_cRegPwd));
	m_blUseIpV6 = FALSE;
}

CLS_NvsDSMPage::~CLS_NvsDSMPage()
{

}

void CLS_NvsDSMPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_DSM_NVS, m_lvNVS);
	DDX_Control(pDX, IDC_CBO_DSM_NVS_QUERY_TYPE, m_cboQueryType);
	DDX_Control(pDX, IDC_CBO_DSM_NVS_PAGE, m_cboPage);
}


BEGIN_MESSAGE_MAP(CLS_NvsDSMPage, CLS_BasePage)
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BTN_DSM_NVS_REFRESH, &CLS_NvsDSMPage::OnBnClickedBtnDsmNvsRefresh)
	ON_MESSAGE(NSLOOK_MSG_NVS,&CLS_NvsDSMPage::OnNslookRegMsg)
	ON_MESSAGE(NVSSDK_MSG_NVS,&CLS_NvsDSMPage::OnNvssdkRegMsg)
	ON_BN_CLICKED(IDC_BTN_DSM_NVS_QUERY, &CLS_NvsDSMPage::OnBnClickedBtnDsmNvsQuery)
	ON_NOTIFY(NM_DBLCLK, IDC_LIST_DSM_NVS, &CLS_NvsDSMPage::OnNMDblclkListDsmNvs)
	ON_NOTIFY(NM_CLICK, IDC_LIST_DSM_NVS, &CLS_NvsDSMPage::OnNMClickListDsmNvs)
	ON_BN_CLICKED(IDC_BTN_GET_COUNT, &CLS_NvsDSMPage::OnBnClickedBtnGetCount)
END_MESSAGE_MAP()


// CLS_NvsDSMPage message handlers

BOOL CLS_NvsDSMPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	s_hWnd = this->GetSafeHwnd();

	((CEdit*)GetDlgItem(IDC_EDIT_DSM_NVS_ID))->SetLimitText(31);

	m_lvNVS.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	
	UI_UpdateDialog();

	m_cboQueryType.SetCurSel(0);

	m_lvNVS.SetSortType(2,SORT_TYPE_IP);
	m_lvNVS.SetSortType(3,SORT_TYPE_IP);
	m_lvNVS.SetSortType(4,SORT_TYPE_INT);

	return TRUE;  
}

void CLS_NvsDSMPage::OnDestroy()
{
	CLS_BasePage::OnDestroy();

	// TODO: Add your message handler code here
}

void CLS_NvsDSMPage::OnChannelChanged(int _iRegID, int _iChannelNo, int _iStreamNo)
{
	m_iRegID = _iRegID;
}

void CLS_NvsDSMPage::OnBnClickedBtnDsmNvsRefresh()
{
	m_lvNVS.DeleteAllItems();
	m_cboPage.ResetContent();

	if (m_blUseNslook) {
		DsmNvsRefreshByNslook();
	} else {
		DsmNvsRefreshByNvssdk();
	}
}

void CLS_NvsDSMPage::DsmNvsRefreshByNslook()
{
	//Resolve refresh stuck exception
	if (m_iRegID >= 0)
	{
		NSLook_LogoffServer(m_iRegID);
	}
	m_iRegID = NSLook_LogonServer(m_cRegIP, m_iRegPort, true);

	Sleep(1000);

	int iCount = 0;
	int iRet = NSLook_GetCount(m_iRegID,m_cRegUser,m_cRegPwd,&iCount,TYPE_NVS);
	if(0 == iRet)
	{
		iRet = NSLook_GetList(m_iRegID,m_cRegUser,m_cRegPwd, 0, NULL, &CLS_NvsDSMPage::NslookRegNotify, TYPE_NVS);
		if (iRet != 0)
		{
			AddLog(LOG_TYPE_FAIL,"","(%d)NSLook_GetList(%d,%s,%s,,,TYPE_NVS)"
				,iRet,m_iRegID,m_cRegUser,m_cRegPwd);
		}
		if (iCount > 0)
		{
			m_cboPage.AddString(_T("1"));
			m_cboPage.SetCurSel(0);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","(%d)NSLook_GetCount(%d,%s,%s,,TYPE_NVS)"
			,iRet,m_iRegID,m_cRegUser,m_cRegPwd);
	}
	SetDlgItemInt(IDC_EDIT_DSM_NVS_COUNT,iCount);
}

int __stdcall CLS_NvsDSMPage::NslookRegNotify(int _iCount,st_NvsSingle *_regNVS)
{
	__try
	{
		if (_regNVS)
		{
			LRESULT lRet = SendMessageTimeout(s_hWnd,NSLOOK_MSG_NVS,(WPARAM)_regNVS,(LPARAM)_iCount,SMTO_NORMAL,3000,NULL);
// 			if (0 == lRet)
// 			{
// 				AddLog(LOG_TYPE_MSG,"","NvsNotify(%#x,%d,%#08x)",_regNVS,_iCount,GetLastError());
// 			}
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{
		//AddLog(LOG_TYPE_MSG,"","NvsNotify exception");
	}

	return 0;
}

LRESULT CLS_NvsDSMPage::OnNslookRegMsg( WPARAM wParam, LPARAM lParam )
{
	__try
	{
		DsmNvsRegInfoEx tNvsEx = {0};
		st_NvsSingle* ptNvsArray = (st_NvsSingle*)wParam;
		st_NvsSingle* ptNvsSingle = NULL;
		int iCount = (int)lParam;
		for (int i = 0 ; i < iCount; ++i)
		{
			ptNvsSingle = ptNvsArray + i;
			memset(&tNvsEx, 0, sizeof(DsmNvsRegInfoEx));
			strcpy_s(tNvsEx.cFactoryID, sizeof(tNvsEx.cFactoryID), ptNvsSingle->cFactoryID);
			strcpy_s(tNvsEx.cNvsName, sizeof(tNvsEx.cNvsName), ptNvsSingle->cNvsName);
			strcpy_s(tNvsEx.cNvsIP, sizeof(tNvsEx.cNvsIP), ptNvsSingle->cNvsIP);
			AddOneNvsItem(&tNvsEx);
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{
		AddLog(LOG_TYPE_MSG,"","AddNvs exception");
	}

	return 0;
}

void CLS_NvsDSMPage::DsmNvsRefreshByNvssdk()
{
	int iCount = 0;
	int iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_DEVCOUNT_WITHREG, &iCount, sizeof(int));
	if(iCount > 0)
	{
		ActiveRegDevListNotify tRegDevListNotify = {0};
		tRegDevListNotify.iSize = sizeof(ActiveRegDevListNotify);
		tRegDevListNotify.pCbkEx = &CLS_NvsDSMPage::NvssdkRegNotify;
		tRegDevListNotify.pvUser = this;
		int iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_DEVLIST_WITHREG, &tRegDevListNotify, sizeof(ActiveRegDevListNotify));
		if (RET_SUCCESS != iRet) {
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_DEVLIST_WITHREG fail!iRet=%d", iRet);
		} else {
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_DEVLIST_WITHREG succ!");
		}
	}
	SetDlgItemInt(IDC_EDIT_DSM_NVS_COUNT, iCount);
}

int CLS_NvsDSMPage::NvssdkRegNotify(int _iTotalCount, int _iCurrentCount, void* _pvNvsList, int _iTotalSize, int _iSingleSize, void* _pvUsrData)
{
	__try
	{
		if (NULL != _pvNvsList)
		{
			LRESULT lRet = SendMessageTimeout(s_hWnd, NVSSDK_MSG_NVS, (WPARAM)_pvNvsList, (LPARAM)_iCurrentCount, SMTO_NORMAL, 3000, NULL);
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{
		//AddLog(LOG_TYPE_MSG,"","NvsLstNotify exception");
	}

	return 0;
}

LRESULT CLS_NvsDSMPage::OnNvssdkRegMsg(WPARAM wParam, LPARAM lParam)
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


void CLS_NvsDSMPage::AddOneNvsItem(DsmNvsRegInfoEx* _ptNvsEx)
{
	int iItem = -1;
	LVFINDINFO info;
	info.flags = LVFI_STRING;
	info.psz = _ptNvsEx->cFactoryID;
	while ((iItem = m_lvNVS.FindItem(&info, iItem)) != -1)
	{
		CString strID = m_lvNVS.GetItemText(iItem, 0);
		if (0 == strID.CompareNoCase(_ptNvsEx->cFactoryID))
		{
			return;
		}
	}

	char cTmp[LEN_64] = {0};
	if (0 == strcmp("UTF-8", _ptNvsEx->cCharSet)) {
		Utf8ToGbk(_ptNvsEx->cNvsName, strlen(_ptNvsEx->cNvsName), cTmp, LEN_64);
	} else {
		strcpy(cTmp, _ptNvsEx->cNvsName);
	}

	iItem = m_lvNVS.GetItemCount();
	m_lvNVS.InsertItem(iItem, _ptNvsEx->cFactoryID);
	m_lvNVS.SetItemText(iItem, 1, cTmp);
	if (IP_VERSION_6 == _ptNvsEx->iIpVer) {
		m_lvNVS.SetItemText(iItem, 2, _ptNvsEx->cNvsIpV6);
	} else {
		m_lvNVS.SetItemText(iItem, 2, _ptNvsEx->cNvsIP);
	}
	m_lvNVS.SetItemText(iItem, 3, _T(""));
	m_lvNVS.SetItemText(iItem, 4, _T(""));
}

void CLS_NvsDSMPage::OnBnClickedBtnDsmNvsQuery()
{
	if (m_blUseNslook) {
		NslookDsmNvsQuery();
	} else {
		NvssdkDsmNvsQuery();
	}
}

void CLS_NvsDSMPage::NslookDsmNvsQuery()
{
	if (m_iRegID >= 0) {
		NSLook_LogoffServer(m_iRegID);
	}
	m_iRegID = NSLook_LogonServer(m_cRegIP, m_iRegPort, true);

	REG_NVS regNVS = {0};
	m_lvNVS.DeleteAllItems();
	GetDlgItemText(IDC_EDIT_DSM_NVS_ID,regNVS.m_stNvs.cFactoryID,sizeof(regNVS.m_stNvs.cFactoryID));
	if (strlen(regNVS.m_stNvs.cFactoryID) <= 0) {
		AddLog(LOG_TYPE_MSG,"","NVS ID is empty");
		return;
	}

	int iRet = NSLook_Query(m_iRegID,NULL,&regNVS,TYPE_NVS);
	if(RET_SUCCESS == iRet) {
		DsmNvsRegInfoEx tNvsEx = {0};
		strcpy_s(tNvsEx.cFactoryID, sizeof(tNvsEx.cFactoryID), regNVS.m_stNvs.cFactoryID);
		strcpy_s(tNvsEx.cNvsName, sizeof(tNvsEx.cNvsName), regNVS.m_stNvs.cNvsName);
		strcpy_s(tNvsEx.cNvsIP, sizeof(tNvsEx.cNvsIP), regNVS.m_stNvs.cNvsIP);
		AddOneNvsItem(&tNvsEx);
	} else {
		AddLog(LOG_TYPE_FAIL,"","(%d)NSLook_Query(%d,NULL,%#x,TYPE_NVS)", iRet, m_iRegID, &regNVS);
	}
}

void CLS_NvsDSMPage::NvssdkDsmNvsQuery()
{
	m_lvNVS.DeleteAllItems();
	DsmNvsRegInfoEx tNvsEx = {0};
	GetDlgItemText(IDC_EDIT_DSM_NVS_ID, tNvsEx.cFactoryID, sizeof(tNvsEx.cFactoryID));
	int iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_REGNVSBYID_WITHREG, &tNvsEx, sizeof(DsmNvsRegInfoEx));
	if (RET_SUCCESS == iRet) {
		AddOneNvsItem(&tNvsEx);	
	} else {
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_REGNVSBYID_WITHREG fail!iRet=%d", iRet);
	}
}

void CLS_NvsDSMPage::OnNMDblclkListDsmNvs(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItem = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	
	int iCount = m_lvNVS.GetItemCount();
	int iItem = pNMItem->iItem;
	if (iItem >= iCount || iItem < 0) {	
		//out of list
		return;
	}

	if (m_blUseNslook) {
		OnActiveLogonNvsByNslook(iItem);
	} else {
		OnActiveLogonNvsByNvssdk(iItem);
	}

	*pResult = 0;
}

void CLS_NvsDSMPage::OnActiveLogonNvsByNslook(int _iItem)
{
	if (m_iRegID >= 0) {
		NSLook_LogoffServer(m_iRegID);
	}
	m_iRegID = NSLook_LogonServer(m_cRegIP, m_iRegPort, true);

	st_LogOnInfo stLogOn = {0};
	strcpy_s(stLogOn.cUserName,sizeof(m_cRegUser),m_cRegUser);
	strcpy_s(stLogOn.cUserPwd,sizeof(m_cRegPwd),m_cRegPwd);
	m_lvNVS.GetItemText(_iItem,0,stLogOn.stNvs.cFactoryID,sizeof(stLogOn.stNvs.cFactoryID));
	m_lvNVS.GetItemText(_iItem,1,stLogOn.stNvs.cNvsName,sizeof(stLogOn.stNvs.cNvsName));
	m_lvNVS.GetItemText(_iItem,2,stLogOn.stNvs.cNvsIP,sizeof(stLogOn.stNvs.cNvsIP));

	st_ProxyInfo stProxy = {0};
	int iRet = NSLook_ConnectNVS(m_iRegID,&stLogOn,&stProxy);
	if (0 == iRet)
	{
		m_lvNVS.SetItemText(_iItem,3,stProxy.cProxyIP);
		m_lvNVS.SetItemText(_iItem,4,IntToString(stProxy.iProxyPort));
		AddLog(LOG_TYPE_SUCC,"","NSLook_ConnectNVS(%d,%#x,%#x)",m_iRegID,&stLogOn,&stProxy);

		DEVICE_INFO tDevInit = {0};
		strcpy_s(tDevInit.cIP,sizeof(tDevInit.cIP),stLogOn.stNvs.cNvsIP);
		strcpy_s(tDevInit.cID,sizeof(tDevInit.cID),stLogOn.stNvs.cFactoryID);
		strcpy_s(tDevInit.cProxy,sizeof(tDevInit.cProxy),stProxy.cProxyIP);
		tDevInit.iPort = stProxy.iProxyPort;
		strcpy_s(tDevInit.cUserName,sizeof(tDevInit.cUserName),"Admin");
		strcpy_s(tDevInit.cPassword,sizeof(tDevInit.cPassword),"1111");

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
					,pDevFind->cIP,pDevLogon->iPort,pDevFind->cProxy,pDevFind->cID);
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
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","(%d)NSLook_ConnectNVS(%d,%#x,%#x)",iRet,m_iRegID,&stLogOn,&stProxy);
	}
}

void CLS_NvsDSMPage::OnActiveLogonNvsByNvssdk(int _iItem)
{
	char cNvsLanIp[64] = {0};
	m_lvNVS.GetItemText(_iItem, 2, cNvsLanIp, sizeof(cNvsLanIp));
	AssignProxy tAssignProxy = {0};
	m_lvNVS.GetItemText(_iItem, 0, tAssignProxy.cFactoryID, sizeof(tAssignProxy.cFactoryID));
	int iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_ASSIGNPROXY_WITHREG, &tAssignProxy, sizeof(AssignProxy));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDsmRegstierInfo::DSM_CMD_GET_REGNVSBYID_WITHREG fail!iRet=%d", iRet);
		return;
	} else {
		AddLog(LOG_TYPE_SUCC,"","DSM_CMD_GET_REGNVSBYID_WITHREG111(%s,%s,%s,%d)"
			, tAssignProxy.cFactoryID, tAssignProxy.cProxyIpV4, tAssignProxy.cProxyIpV6, tAssignProxy.iProxyPort);
	}

	DEVICE_INFO tDevInit = {0};
	strcpy_s(tDevInit.cIP, sizeof(tDevInit.cIP), cNvsLanIp);
	strcpy_s(tDevInit.cID, sizeof(tDevInit.cID), tAssignProxy.cFactoryID);
	if (m_blUseIpV6 && strlen(tAssignProxy.cProxyIpV6) > 0) {
		m_lvNVS.SetItemText(_iItem, 3, tAssignProxy.cProxyIpV6);
		strcpy_s(tDevInit.cProxy,sizeof(tDevInit.cProxy), tAssignProxy.cProxyIpV6);
		AddLog(LOG_TYPE_SUCC,"","tAssignProxy.cProxyIpV6=%s", tAssignProxy.cProxyIpV6);
	} else {
		m_lvNVS.SetItemText(_iItem, 3, tAssignProxy.cProxyIpV4);
		strcpy_s(tDevInit.cProxy,sizeof(tDevInit.cProxy), tAssignProxy.cProxyIpV4);
		AddLog(LOG_TYPE_SUCC,"","tAssignProxy.cProxyIpV4=%s", tAssignProxy.cProxyIpV4);
	}
	m_lvNVS.SetItemText(_iItem, 4, IntToString(tAssignProxy.iProxyPort));
	tDevInit.iPort = tAssignProxy.iProxyPort;
	AddLog(LOG_TYPE_SUCC,"","DSM_CMD_GET_REGNVSBYID_WITHREG222(%s,%s,%s,%d)"
		, tAssignProxy.cFactoryID, tAssignProxy.cProxyIpV4, tAssignProxy.cProxyIpV6, tAssignProxy.iProxyPort);
	strcpy_s(tDevInit.cUserName, sizeof(tDevInit.cUserName), "Admin");
	strcpy_s(tDevInit.cPassword, sizeof(tDevInit.cPassword), "1111");
	CLS_LogonView logon(&tDevInit);
	if(IDOK != logon.DoModal()) {
		return;
	}

	int iOldLogonID = -1;
	PDEVICE_INFO pDevLogon = logon.GetDeviceInfo();
	PDEVICE_INFO pDevFind = FindDevice(pDevLogon->cIP,pDevLogon->iPort,pDevLogon->cProxy,pDevLogon->cID,&iOldLogonID);
	if (NULL != pDevFind) {
		if(LOGON_SUCCESS == NetClient_GetLogonStatus(iOldLogonID)) {
			AddLog(LOG_TYPE_MSG,"","FindDevice(%s,%d,%s,%s) device already exist!"
				,pDevFind->cIP,pDevLogon->iPort,pDevFind->cProxy,pDevFind->cID);
			return;
		}
	}

	LogonPara tLogonPara = {0};
	tLogonPara.iSize = sizeof(LogonPara);
	tLogonPara.iNvsPort = pDevLogon->iPort;
	strcpy(tLogonPara.cUserName, pDevLogon->cUserName);
	strcpy(tLogonPara.cUserPwd, pDevLogon->cPassword);
	strcpy(tLogonPara.cProductID, pDevLogon->cID);
	strcpy(tLogonPara.cCharSet, "UTF-8");
	int iLogonType = SERVER_NORMAL;
	if (strlen(pDevLogon->cIP) > LEN_16 || strlen(pDevLogon->cProxy) > LEN_16) {
		iLogonType = SERVER_NORMAL_IPV6;
		strcpy(tLogonPara.cProxyIpV6, pDevLogon->cProxy);
		strcpy(tLogonPara.cNvsIpV6, pDevLogon->cIP);
	} else {
		iLogonType = SERVER_NORMAL;
		strcpy(tLogonPara.cProxy, pDevLogon->cProxy);
		strcpy(tLogonPara.cNvsIP, pDevLogon->cIP);
	}
	int iLogonID = NetClient_SyncLogon(iLogonType, &tLogonPara, sizeof(LogonPara));
	if (iLogonID >= 0) {
		AddLog(LOG_TYPE_SUCC, pDevLogon->cIP, "NetClient_SyncLogon, iLogonType=%d.", iLogonType);
		PDEVICE_INFO pDevNew = AddDevice(iLogonID);
		if (NULL != pDevNew) {
			memcpy_s(pDevNew,sizeof(DEVICE_INFO),pDevLogon,sizeof(DEVICE_INFO));
			pDevNew->hItem = NULL;
			pDevNew->uiInterTalkID = -1;
		} else {
			AddLog(LOG_TYPE_MSG,"","AddDevice(%d) Can not add device",iLogonID);
		}
	} else {
		AddLog(LOG_TYPE_FAIL, pDevLogon->cIP, "NetClient_SyncLogon(%d,%s,%s,%s.)", iLogonType, pDevLogon->cProxy, pDevLogon->cIP, pDevLogon->cID);
	}
}

void CLS_NvsDSMPage::OnNMClickListDsmNvs(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItem = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	
	CString strID = m_lvNVS.GetItemText(pNMItem->iItem,0);
	SetDlgItemText(IDC_EDIT_DSM_NVS_ID,strID);

	*pResult = 0;
}

void CLS_NvsDSMPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();
}

void CLS_NvsDSMPage::UI_UpdateDialog()
{
	InsertColumn(m_lvNVS,0,IDS_MNG_ADMIN_FACTORY_ID,LVCFMT_CENTER,180);
	InsertColumn(m_lvNVS,1,IDS_MNG_NVS_NAME,LVCFMT_CENTER,120);	
	InsertColumn(m_lvNVS,2,IDS_MNG_ADMIN_IP,LVCFMT_CENTER,160);
	InsertColumn(m_lvNVS,3,IDS_MNG_NVS_PROXY_IP,LVCFMT_CENTER,160);
	InsertColumn(m_lvNVS,4,IDS_MNG_NVS_PROXY_PORT,LVCFMT_CENTER,80);

	InsertString(m_cboQueryType,0,IDS_MNG_ADMIN_FACTORY_ID);

	SetDlgItemTextEx(IDC_STATIC_DSM_NVS_COUNT,IDS_MNG_NVS_COUNT);
	SetDlgItemTextEx(IDC_STATIC_DSM_NVS_PAGE,IDS_MNG_DNS_PAGE_NO);
	SetDlgItemTextEx(IDC_BTN_DSM_NVS_REFRESH,IDS_MNG_ADMIN_REFRESH);
	SetDlgItemTextEx(IDC_BTN_DSM_NVS_QUERY,IDS_MNG_NVS_QUERY);
}

CString CLS_NvsDSMPage::GetNvsType(int _iNvsType)
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

int CLS_NvsDSMPage::GetNvsType(CString _strNvsType)
{
	int iNvsType = 0;
	if (_strNvsType.CompareNoCase(_T("T")))
	{
		iNvsType = 0;
	}
	else if (_strNvsType.CompareNoCase(_T("S")))
	{
		iNvsType = 1;
	}
	else if (_strNvsType.CompareNoCase(_T("T+")))
	{
		iNvsType = 2;
	}
	else
	{
		iNvsType = atoi(_strNvsType);
	}
	return iNvsType;
}

void CLS_NvsDSMPage::SetRegisterInfo(RegisterInfo* _ptInfo)
{
	if (NULL == _ptInfo) {
		return;
	}

	m_blUseNslook = _ptInfo->blUseNslook;
	strcpy_s(m_cRegIP, sizeof(m_cRegIP), _ptInfo->cRegIP);
	m_iRegPort = _ptInfo->iRegPort;
	strcpy_s(m_cRegUser, sizeof(m_cRegUser), _ptInfo->cRegUser);
	strcpy_s(m_cRegPwd, sizeof(m_cRegPwd), _ptInfo->cRegPwd);
	m_blUseIpV6 = _ptInfo->blUseIpV6;
}

void CLS_NvsDSMPage::OnBnClickedBtnGetCount()
{
	int iRet = RET_FAILED;
	int iCount = 0;
	if (m_blUseNslook) {
		if (m_iRegID >= 0) {
			NSLook_LogoffServer(m_iRegID);
		}
		m_iRegID = NSLook_LogonServer(m_cRegIP, m_iRegPort, true);
		iRet = NSLook_GetCount(m_iRegID, m_cRegUser, m_cRegPwd, &iCount,TYPE_NVS);
		if (RET_SUCCESS == iRet) {
			SetDlgItemInt(IDC_EDIT_DSM_NVS_COUNT,iCount);
		} else {
			AddLog(LOG_TYPE_FAIL,"","Get Nvs Count failed by Nslook.");
		}
	} else {
		iRet = NetClient_GetDsmRegstierInfo(DSM_CMD_GET_DEVCOUNT_WITHREG, &iCount, sizeof(int));
		if (RET_SUCCESS == iRet) {
			SetDlgItemInt(IDC_EDIT_DSM_NVS_COUNT, iCount);
		} else {
			AddLog(LOG_TYPE_FAIL,"","Get Nvs Count failed by Nvssdk.");
		}
	}	
}

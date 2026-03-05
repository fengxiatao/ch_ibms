#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "FaceDemoDlg.h"

#include "CLS_DlgFaceLib.h"
#include "CLS_DlgFacePic.h"
#include "CLS_DlgFaceStream.h"
#include "CLS_DlgFaceAlarm.h"
#include "CLS_DlgFaceSearch.h"
#include "CLS_DlgFaceSearchSnap.h"
#include "CLS_DlgFaceDetection.h"
#include "CLS_DlgFaceLibSync.h"

//#define USED_BY_TOOL
#ifdef  USED_BY_TOOL
#include "CLS_DlgFaceAdvance.h"
#include "CLS_DlgFaceAlarmLink.h"
#include "CLS_DlgFaceFearutre.h"
#include "CLS_DlgFaceSche.h"
#endif


#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define USER_MAIN_NOTIFY		WM_USER+1002
#define USER_PARAMCHANGE_NOTIFY	WM_USER+1003



CFaceDemoDlg::CFaceDemoDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CFaceDemoDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);

	memset(m_pPage, 0, sizeof(m_pPage));
	m_pCurPage = NULL;
	m_iPageIndex = -1;
	m_iLogonId = 0;
	m_iLogonMode = SERVER_NORMAL;
}

void CFaceDemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_CHANNO, m_cboChanNo);
	DDX_Control(pDX, IDC_TREE_CONFIG, m_treeConfig);
	DDX_Control(pDX, IDC_EDT_IPADDR, m_edtIpAddr);
	DDX_Control(pDX, IDC_EDT_USERNAME, m_edtUserName);
	DDX_Control(pDX, IDC_EDT_PASSWORD, m_edtPassword);
	DDX_Control(pDX, IDC_EDT_PORT, m_edtPort);
	DDX_Control(pDX, IDC_RADIO_NORMAL_MODE, m_rdoNormalMode);
	DDX_Control(pDX, IDC_RADIO_ACTIVE_MODE, m_rdoActiveMode);
	DDX_Control(pDX, IDC_EDIT_WAN_PORT, m_edtWanPort);
}

BEGIN_MESSAGE_MAP(CFaceDemoDlg, CDialog)
	ON_WM_PAINT()
	ON_WM_CLOSE()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_NOTIFY(TVN_SELCHANGED, IDC_TREE_CONFIG, &CFaceDemoDlg::OnTvnSelchangedTreeConfig)
	ON_BN_CLICKED(IDC_BTN_LOGON, &CFaceDemoDlg::OnBnClickedBtnLogon)
	ON_BN_CLICKED(IDC_BTN_LOGOFF, &CFaceDemoDlg::OnBnClickedBtnLogoff)
	ON_CBN_SELCHANGE(IDC_CBO_CHANNO, &CFaceDemoDlg::OnCbnSelchangeCboChanno)
	ON_MESSAGE(USER_MAIN_NOTIFY, &CFaceDemoDlg::OnMainNotify)
	ON_MESSAGE(USER_PARAMCHANGE_NOTIFY,&CFaceDemoDlg::OnParamChangeNotify)
	ON_BN_CLICKED(IDC_RADIO_NORMAL_MODE, &CFaceDemoDlg::OnBnClickedRadioNormalMode)
	ON_BN_CLICKED(IDC_RADIO_ACTIVE_MODE, &CFaceDemoDlg::OnBnClickedRadioActiveMode)
END_MESSAGE_MAP()


void CFaceDemoDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // Device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center the icon in the workspace rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

//When the user drags the minimized window, the system calls this function to get the cursor
//Display.
HCURSOR CFaceDemoDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CFaceDemoDlg::OnOK()
{
	//Solve the problem of pressing the Enter key to exit the program
}

void CFaceDemoDlg::OnCancel()
{
	//Solve the problem of exiting by ESC program
}

void CFaceDemoDlg::OnClose()
{
	//Solve the problem that the program does not exit by clicking the Close button of the dialog box
	CDialog::OnCancel();
}

BOOL CFaceDemoDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	SetIcon(m_hIcon, TRUE);			// Set large icon
	SetIcon(m_hIcon, FALSE);		// Set small icons

	m_rdoNormalMode.SetCheck(BST_CHECKED);

	SDK_Init();

	UI_Init();
	UI_InitTree(DEV_TYPE_NVR);
	UI_EnableLogon(TRUE);
	UI_EnableConfig(FALSE);

	return TRUE;
}

void CFaceDemoDlg::UI_Init()
{
	SetDlgItemText(IDC_EDT_IPADDR, "192.168.1.2");
	SetDlgItemText(IDC_EDT_USERNAME, "Admin");
	SetDlgItemText(IDC_EDT_PASSWORD, "1111");
	SetDlgItemInt(IDC_EDT_PORT, 3000);	
}

void CFaceDemoDlg::UI_InitTree(int _iDevType)
{
	m_treeConfig.DeleteAllItems();
	HTREEITEM hItem = NULL;	
	hItem = InsertItem(m_treeConfig, "Face Library", PAGE_FACE_LIB);
	hItem = InsertItem(m_treeConfig, "Face Picture", PAGE_FACE_PIC);
	hItem = InsertItem(m_treeConfig, "Face AlarmStream", PAGE_FACE_STREAM);
	hItem = InsertItem(m_treeConfig, "Face Detection", PAGE_FACE_DETECTION);
	hItem = InsertItem(m_treeConfig, "Face Recognition", PAGE_FACE_ALARM);
	if (DEV_TYPE_IPC == _iDevType)
	{						
	} 
	else if (DEV_TYPE_NVR == _iDevType)
	{	
		hItem = InsertItem(m_treeConfig, "Face Search", PAGE_FACE_SEARCH);
		hItem = InsertItem(m_treeConfig, "Face Library Sync", PAGE_FACE_LIB_SYNC);
	}
	hItem = InsertItem(m_treeConfig, "Search Capture By Picture", PAGE_FACE_SEARCH_SNAP);
	
#ifdef USED_BY_TOOL
	hItem = InsertItem(m_treeConfig, "Face Schedule", PAGE_FACE_SCHEDULE);
	hItem = InsertItem(m_treeConfig, "Face Attribute", PAGE_FACE_FEATURE);
	hItem = InsertItem(m_treeConfig, "Import and Export", PAGE_FACE_ADVANCE);
	hItem = InsertItem(m_treeConfig, "Face Alarm Linkage", PAGE_FACE_ALARM_LINK);
#endif
	
	ShowPage(PAGE_MIN);
}

void CFaceDemoDlg::SDK_Init()
{
	//Note: This demo is a static load of NVSDK.dll
	NetClient_SetSDKWorkMode(EASYX_LIGHT_MODE);
	NetClient_Startup_V4(0, 0, 0);
	NetClient_SetNotifyFunction_V4(&CFaceDemoDlg::MainNotify, NULL, &CFaceDemoDlg::ParamChangeNotify, NULL, NULL);
}

void CFaceDemoDlg::MainNotify(int _iLogonId, long _iWparam, void* _iParam, void* _iUser)
{
	CFaceDemoDlg* plcs = (CFaceDemoDlg*)_iUser;
	if (NULL == plcs || _iLogonId != plcs->m_iLogonId)
	{
		return;
	}

	::PostMessage(plcs->GetSafeHwnd(), USER_MAIN_NOTIFY, (WPARAM)_iWparam, (LPARAM)_iParam);
}

LRESULT CFaceDemoDlg::OnMainNotify(WPARAM wp, LPARAM lp)
{
	if (NULL != m_pCurPage)
	{
		m_pCurPage->OnMainNotify(m_iLogonId, (long)wp, (void*)lp);
	}	
	return 0;
}

void CFaceDemoDlg::ParamChangeNotify( int _iLogonID, int _iChan, PARATYPE _iParaType,STR_Para* _pPara,void* _iUser )
{
	CFaceDemoDlg* plcs = (CFaceDemoDlg*)_iUser;
	if (NULL == plcs || _iLogonID != plcs->m_iLogonId)
	{
		return;
	}

	int iMallocSize = sizeof(_PARAMCHANGE_NOTIFY_DATA);
	_PARAMCHANGE_NOTIFY_DATA* pData = (_PARAMCHANGE_NOTIFY_DATA*)MallocMsgMemory(iMallocSize);
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

	switch (pData->m_iParaType)
	{
	case PARA_FACE_LIB_SYNC_RESULT:
		{
			FaceLibSyncResult* ptTemp = (FaceLibSyncResult*)pData->m_pPara.m_iPara[0];
			if (NULL == ptTemp || ptTemp->iSize < 0)
			{
				break;
			}

			int iCpySize = min(ptTemp->iSize, sizeof(FaceLibSyncResult));
			memcpy(&pData->m_utParam.tFaceLibSyncResult, ptTemp, iCpySize);
		}
		break;
	default:
		break;
	}

	::PostMessage(plcs->GetSafeHwnd(), USER_PARAMCHANGE_NOTIFY, (LPARAM)pData, 0);

}

LRESULT CFaceDemoDlg::OnParamChangeNotify( WPARAM wParam, LPARAM lParam )
{
	_PARAMCHANGE_NOTIFY_DATA* pData = (_PARAMCHANGE_NOTIFY_DATA*)wParam;
	__try
	{
		if (NULL != m_pCurPage)
		{
			if (PARA_FACE_LIB_SYNC_RESULT == pData->m_iParaType)
			{
				m_pCurPage->OnParamChangeNotify(pData->m_iLogonID,pData->m_iChannelNo,pData->m_iParaType,&pData->m_utParam,pData->m_iUserData);
			}
			else
			{
				m_pCurPage->OnParamChangeNotify(pData->m_iLogonID,pData->m_iChannelNo,pData->m_iParaType,&pData->m_pPara,pData->m_iUserData);
			}
		}	
	}
	__finally
	{
		FreeMsgMemory(pData);
	}

	return 0;
}

void CFaceDemoDlg::UI_EnableLogon(BOOL _blEnbale)
{
	m_edtPort.EnableWindow(_blEnbale);
	m_edtIpAddr.EnableWindow(_blEnbale);
	m_edtUserName.EnableWindow(_blEnbale);
	m_edtPassword.EnableWindow(_blEnbale);
	m_edtWanPort.EnableWindow(_blEnbale);
	GetDlgItem(IDC_BTN_LOGON)->EnableWindow(_blEnbale);
}

void CFaceDemoDlg::UI_EnableConfig(BOOL _blEnbale)
{
	m_treeConfig.EnableWindow(_blEnbale);
	if (NULL != m_pCurPage)
	{
		m_pCurPage->EnableWindow(_blEnbale);
	}
}

void CFaceDemoDlg::OnBnClickedBtnLogon()
{
	int iRet = -1;
	if (SERVER_ACTIVE == m_iLogonMode)
	{
		//Active mode login logic
		int iLocalListenPort = GetDlgItemInt(IDC_EDT_PORT);
		iRet = NetClient_SetPort(iLocalListenPort, 0);	//Start the local listening service
		if(0 != iRet ) {
			MessageBox("Set local lan port fail!");
			return;
		}

		ActiveNetWanInfo tLocalWanInfo = {0};
		tLocalWanInfo.iSize = sizeof(ActiveNetWanInfo);

		GetDlgItemText(IDC_EDT_WANIPADDR,tLocalWanInfo.cWanIP,32);

		tLocalWanInfo.iWanPort = GetDlgItemInt(IDC_EDIT_WAN_PORT);
		//Start the local public network port (router mapping port)
		iRet = NetClient_SetDsmConfig(DSM_CMD_SET_NET_WAN_INFO, &tLocalWanInfo, sizeof(ActiveNetWanInfo));
		if(0 != iRet) {
			MessageBox("Set local wan port fail!");
			return;
		}

		char cProductID[32] = {0};
		m_edtIpAddr.GetWindowText(cProductID, sizeof(cProductID));
		DsmOnline tOnline = {0};
		tOnline.iSize = sizeof(DsmOnline);
		strncpy(tOnline.cProductID, cProductID, LEN_32);
		int iOutTime = 0;
		while (1)
		{
			//Get online status of registration
			NetClient_GetDsmRegstierInfo(DSM_CMD_GET_ONLINE_STATE, &tOnline, sizeof(DsmOnline));
			if (DSM_STATE_ONLINE == tOnline.iOnline) {
				break;
			}

			if (iOutTime >= 30) {
				MessageBox("Device not register!");
				return;
			}

			Sleep(1000);
			iOutTime++;
		}

		LogonActiveServer tActive = {0};
		tActive.iSize = sizeof(LogonActiveServer);
		m_edtUserName.GetWindowText(tActive.cUserName, sizeof(tActive.cUserName));
		m_edtPassword.GetWindowText(tActive.cUserPwd, sizeof(tActive.cUserPwd));
		strcpy(tActive.cProductID, cProductID);
		m_iLogonId = NetClient_SyncLogon(SERVER_ACTIVE, &tActive, sizeof(LogonActiveServer));
	}
	else
	{
		LogonPara tNormal = {0};
		tNormal.iSize = sizeof(tNormal);
		m_edtIpAddr.GetWindowText(tNormal.cNvsIP, sizeof(tNormal.cNvsIP));
		m_edtUserName.GetWindowText(tNormal.cUserName, sizeof(tNormal.cUserName));
		m_edtPassword.GetWindowText(tNormal.cUserPwd, sizeof(tNormal.cUserPwd));
		tNormal.iNvsPort = GetDlgItemInt(IDC_EDT_PORT);
		m_iLogonId = NetClient_SyncLogon(SERVER_NORMAL, &tNormal, tNormal.iSize);
	}

	if (m_iLogonId < 0)
	{
		return;
	}
	NetClient_SetNotifyUserData_V4(m_iLogonId, this);
	UI_EnableLogon(FALSE);
	UI_EnableConfig(TRUE);
	
	//Channel number initialization
	int iChanNum = 0;
	NetClient_GetChannelNum(m_iLogonId, &iChanNum);
	m_cboChanNo.ResetContent();
	for (int i = 0 ; i < iChanNum; ++i)
	{
		m_cboChanNo.InsertString(i, IntToStr(i+1));
	}
	m_cboChanNo.SetCurSel(0);

	UI_InitTree(iChanNum > 1 ? DEV_TYPE_NVR : DEV_TYPE_IPC);

	for(int i=0; i<PAGE_MAX;i++)
	{
		if (NULL != m_pPage[i]) 
		{
			m_pPage[i]->OnChannelChanged(m_iLogonId, m_cboChanNo.GetCurSel(), 0);
			m_pPage[i]->UI_UptateData();
		}
	}

}

int CFaceDemoDlg::GetFaceAbility()
{
	if (m_iLogonId < 0)
	{
		return -1;
	}
	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	stFuncAbilityLevel.iSubFuncType = 25;
	int iReturnByte = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonId, NET_CLIENT_GET_FUNC_ABILITY, 0, &stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (0 != iRet)
	{
		return -1;
	}
	return _ttoi(stFuncAbilityLevel.cParam);
}

void CFaceDemoDlg::OnBnClickedBtnLogoff()
{
	if (m_iLogonId >= 0)
	{
		NetClient_Logoff(m_iLogonId);
		m_iLogonId = -1;
	}

	if (NULL != m_pPage[PAGE_FACE_STREAM])
	{
		m_pPage[PAGE_FACE_STREAM]->OnLogoff();
	}
	if (NULL != m_pPage[PAGE_FACE_LIB_SYNC])
	{
		m_pPage[PAGE_FACE_LIB_SYNC]->OnLogoff();
	}

	UI_EnableLogon(TRUE);
	UI_EnableConfig(FALSE);
}

void CFaceDemoDlg::OnTvnSelchangedTreeConfig(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMTREEVIEW pNMTreeView = reinterpret_cast<LPNMTREEVIEW>(pNMHDR);
	HTREEITEM hItem = pNMTreeView->itemNew.hItem;
	int iIndex = (int)m_treeConfig.GetItemData(hItem);
	ShowPage(iIndex);
	*pResult = 0;
}

void CFaceDemoDlg::ShowPage(int _iIndex)
{
	if (_iIndex < PAGE_MIN || _iIndex >= PAGE_MAX) {
		return;
	}
	
	if (NULL == m_pPage[_iIndex]) {
		switch(_iIndex) {
		case PAGE_FACE_LIB:
			m_pPage[_iIndex] = new CLS_DlgFaceLib(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_LIB, this);
			break;
		case PAGE_FACE_PIC:
			m_pPage[_iIndex] = new CLS_DlgFacePic(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_PIC, this);
			break;
		case PAGE_FACE_STREAM:
			m_pPage[_iIndex] = new CLS_DlgFaceStream(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_STREAM, this);
			break;
		case PAGE_FACE_ALARM:
			m_pPage[_iIndex] = new CLS_DlgFaceAlarm(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_ALARM, this);
			break;
		case PAGE_FACE_SEARCH:
			m_pPage[_iIndex] = new CLS_DlgFaceSearch(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_SEARCH, this);
			break;
#ifdef USED_BY_TOOL
		case PAGE_FACE_FEATURE:
			m_pPage[_iIndex] = new CLS_DlgFaceFearutre(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_FEATURE, this);
			break;
		case PAGE_FACE_SCHEDULE:
			m_pPage[_iIndex] = new CLS_DlgFaceSchedule(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_SCHEDULE, this);
			break;
		case PAGE_FACE_ADVANCE:
			m_pPage[_iIndex] = new CLS_DlgFaceAdvance(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_ADVANCE, this);
			break;
		case PAGE_FACE_ALARM_LINK:
			m_pPage[_iIndex] = new CLS_DlgFaceAlarmLink(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_ALARM_LINK, this);
			break;
#endif
		case PAGE_FACE_SEARCH_SNAP:
			m_pPage[_iIndex] = new CLS_DlgFaceSearchSnap(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_SEARCH_SNAP, this);
			break;
		case PAGE_FACE_DETECTION:
			m_pPage[_iIndex] = new CLS_DlgFaceDetection(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_DETECTION, this);
			break;
		case PAGE_FACE_LIB_SYNC:
			m_pPage[_iIndex] = new CLS_DlgFaceLibSync(this);
			m_pPage[_iIndex]->Create(IDD_DLG_CFG_FACE_LIB_SYNC, this);
			break;
		default:
			break;
		}

		if (NULL != m_pPage[_iIndex]) {
			RECT rcShow = {0};
			GetDlgItem(IDC_GBO_CONFIG)->GetWindowRect(&rcShow);
			ScreenToClient(&rcShow);
			m_pPage[_iIndex]->MoveWindow(&rcShow);
			m_pPage[_iIndex]->OnChannelChanged(m_iLogonId, m_cboChanNo.GetCurSel(), 0);
		}
	}
	if (NULL != m_pCurPage) {
		m_pCurPage->ShowWindow(SW_HIDE);
	}
	m_pCurPage = m_pPage[_iIndex];
	if (NULL != m_pCurPage) {
		m_pCurPage->ShowWindow(SW_SHOW);
	}
}

void CFaceDemoDlg::OnCbnSelchangeCboChanno()
{
	if (NULL != m_pCurPage){
		m_pCurPage->OnChannelChanged(m_iLogonId, m_cboChanNo.GetCurSel(), 0);
	}
}

void CFaceDemoDlg::OnBnClickedRadioNormalMode()
{
	m_rdoNormalMode.SetCheck(BST_CHECKED);
	m_rdoActiveMode.SetCheck(BST_UNCHECKED);
	SetDlgItemText(IDC_EDT_IPADDR, "192.168.1.2");
	SetDlgItemText(IDC_STC_IPADDR, "IPAddr");
	SetDlgItemText(IDC_STC_PORT, "DevPort");
	SetDlgItemText(IDC_EDT_PORT, "3000");
	GetDlgItem(IDC_STATIC_WAN_PORT)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_EDIT_WAN_PORT)->ShowWindow(SW_HIDE);
	m_iLogonMode = SERVER_NORMAL;
	GetDlgItem(IDC_STC_WANIPADDR)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_EDT_WANIPADDR)->ShowWindow(SW_HIDE);
}

void CFaceDemoDlg::OnBnClickedRadioActiveMode()
{
	m_rdoNormalMode.SetCheck(BST_UNCHECKED);
	m_rdoActiveMode.SetCheck(BST_CHECKED);
	SetDlgItemText(IDC_EDT_IPADDR, "ID0000801940400160610391");
	SetDlgItemText(IDC_STC_IPADDR, "FactoryID");
	SetDlgItemText(IDC_STC_PORT, "LanPort");
	SetDlgItemText(IDC_EDT_PORT, "6004");
	GetDlgItem(IDC_STATIC_WAN_PORT)->ShowWindow(SW_SHOW);
	GetDlgItem(IDC_EDIT_WAN_PORT)->ShowWindow(SW_SHOW);
	SetDlgItemText(IDC_EDIT_WAN_PORT, "6004");
	m_iLogonMode = SERVER_ACTIVE;
	GetDlgItem(IDC_STC_WANIPADDR)->ShowWindow(SW_SHOW);
	GetDlgItem(IDC_EDT_WANIPADDR)->ShowWindow(SW_SHOW);

}

#include "stdafx.h"
#include "NetClientDemo.h"
#include "StorageHotBackup.h"

#define TIMER_REFRESH_TIMEOUT	0
#define REFRESH_TIMEOUT_TIME	5000

enum E_DevLinkState	//设备连接状态
{
	n_OffLine		= 0,	//连接失败
	n_OnLine		= 1,	//连接成功
	n_WorkOver		= 2,	//正在同步
};

enum E_DevBackState	//热备状态
{
	n_BackUpReady	= 0,	//正在热备，在线
	n_BackUping		= 1,	//正在备份，不在线
	n_BackUpOver	= 2,	//正在同步，在线
	n_BackUpOffLine	= 3,	//正在热备，不在线
	n_BackPwdWrong = 4, //用户名或密码错误
};

enum E_DevStateList
{
	n_ListNo		= 0,	//序号
	n_ListIp		= 1,	//IP
	n_ListState		= 2,	//连接状态
	n_ListWorkState = 3		//工作状态
};

IMPLEMENT_DYNAMIC(CLS_StorageHotBackup, CDialog)

CLS_StorageHotBackup::CLS_StorageHotBackup(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_StorageHotBackup::IDD, pParent)
{
	m_iLogonID = -1;
	m_iWorkMode = -1;
}

CLS_StorageHotBackup::~CLS_StorageHotBackup()
{
}

void CLS_StorageHotBackup::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_RADIO_NORMAL_WORKMODE, m_rdoNormalWorkMode);
	DDX_Control(pDX, IDC_RADIO_HOTBACKUP_MODE, m_rdoHotBackupMode);
	DDX_Control(pDX, IDC_CHECK_ENABLE_HOTBACKUP, m_chkEnableHotBackup);
	DDX_Control(pDX, IDC_EDIT_HOTBACKUP_DEVIP, m_edtHotBackupDevIp);
	DDX_Control(pDX, IDC_LIST_HOTBACKUP_DEV, m_lstHotBackupHostState);
	DDX_Control(pDX, IDC_LIST_NORMAL_DEV, m_lstWorkHost);
	DDX_Control(pDX, IDC_LIST_NORMAL_DEV_STATE, m_lstWorkHostState);
	DDX_Control(pDX, IDC_BUTTON_REFRESH_NORMAL_DEVLIST, m_btnRefreshWorkDevList);
	DDX_Control(pDX, IDC_EDIT_WORKHOST_PWD, m_edtWorkHostPwd);
}


BEGIN_MESSAGE_MAP(CLS_StorageHotBackup, CLS_BasePage)

	ON_BN_CLICKED(IDC_RADIO_NORMAL_WORKMODE, &CLS_StorageHotBackup::OnBnClickedRadioNormalWorkmode)
	ON_BN_CLICKED(IDC_RADIO_HOTBACKUP_MODE, &CLS_StorageHotBackup::OnBnClickedRadioHotBackupMode)
	ON_BN_CLICKED(IDC_BUTTON_ADD_HOTBACKUP_DEV, &CLS_StorageHotBackup::OnBnClickedButtonAddHotBackupHost)
	ON_BN_CLICKED(IDC_BUTTON_DELETE_HOTBACKUP_DEV, &CLS_StorageHotBackup::OnBnClickedButtonDeleteHotBackupHost)
	ON_BN_CLICKED(IDC_BUTTON_REFRESH_HBU_DEVLIST, &CLS_StorageHotBackup::OnBnClickedButtonRefreshHotBackupHostState)
	ON_BN_CLICKED(IDC_BUTTON_MOVEIN_WORKDEV, &CLS_StorageHotBackup::OnBnClickedButtonMoveinWorkHost)
	ON_BN_CLICKED(IDC_BUTTON_REMOVE_WORKDEV, &CLS_StorageHotBackup::OnBnClickedButtonRemoveWorkHost)
	ON_BN_CLICKED(IDC_BUTTON_REFRESH_NORMAL_DEVLIST, &CLS_StorageHotBackup::OnBnClickedButtonRefreshWorkHostList)
	ON_BN_CLICKED(IDC_BUTTON_REFRESH_NORMAL_DEVSTATE, &CLS_StorageHotBackup::OnBnClickedButtonRefreshWorkHostState)
	ON_BN_CLICKED(IDC_CHECK_ENABLE_HOTBACKUP, &CLS_StorageHotBackup::OnBnClickedCheckEnableHotBackup)
END_MESSAGE_MAP()

BOOL CLS_StorageHotBackup::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtHotBackupDevIp.SetLimitText(LEN_64);
	m_edtWorkHostPwd.SetLimitText(LEN_64);

	UI_UpdateDialogText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_StorageHotBackup::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialogText();
	UpdateNvrHostWorkMode();
}

void CLS_StorageHotBackup::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	UpdateNvrHostWorkMode();
}

void CLS_StorageHotBackup::OnMainNotify(int _iLogonID, int _iWparam, void* _pvLParam, void* _pvUser)
{
	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_BACKUP_SEEKWORKDEV:
		{
			UpdateWorkHostList();
			m_btnRefreshWorkDevList.EnableWindow(TRUE);
			KillTimer(TIMER_REFRESH_TIMEOUT);
			break;
		}
	case WCM_HOTBACK_DEV_STATUS:
		{
			UpdateWorkHostEnableHotSpare();
			UpdateHotBackupHostStateList();
			break;
		}
	case WCM_WORD_DEV_STATUS:
		{
			UpdateWorkHostStateList();
			UpdateWorkHostList();
			break;
		}
	default:
		break;
	}
}

void CLS_StorageHotBackup::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (_iLogonID < 0) {
		AddLog(LOG_TYPE_MSG,"","[CLS_StorageHotBackup::OnParamChangeNotify]->Invalid logon id(%d)", _iLogonID);
		return;
	}

	switch(_iParaType)
	{
	case PARA_BACKUPDEV_INFO:
		{
			break;
		}
	case PARA_WORKDEV_BACKUP_ENABLE:
		{
			UpdateWorkHostEnableHotSpare();
			break;
		}
	case PARA_HOTBACKUP_WORKMODE:
		{
			UpdateNvrHostWorkMode();
			break;
		}
	default:
		break;
	}
}

void CLS_StorageHotBackup::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_NVR_WORKSTATE, GetTextByLan("工作状态", "WorkState"));
	SetDlgItemText(IDC_RADIO_NORMAL_WORKMODE, GetTextByLan("普通模式", "NormalWorkMode"));
	SetDlgItemText(IDC_RADIO_HOTBACKUP_MODE, GetTextByLan("热备模式", "HotBackupMode"));
	SetDlgItemText(IDC_STATIC_NORMAL_MODE, GetTextByLan("普通模式", "NormalMode"));
	SetDlgItemText(IDC_CHECK_ENABLE_HOTBACKUP, GetTextByLan("启用热备机", "EnableHotBackupDev"));
	SetDlgItemText(IDC_STATIC_HOTBACKUP_DEVIP, GetTextByLan("热备机IP地址", "HotBackupDevIp"));
	SetDlgItemText(IDC_BUTTON_ADD_HOTBACKUP_DEV, GetTextByLan("添加", "Add"));
	SetDlgItemText(IDC_BUTTON_DELETE_HOTBACKUP_DEV, GetTextByLan("删除", "Delete"));
	SetDlgItemText(IDC_STATIC_HOTBACKUP_MODE, GetTextByLan("热备模式", "HotBackupMode"));
	SetDlgItemText(IDC_BUTTON_REFRESH_HBU_DEVLIST, GetTextByLan("刷新", "Refresh"));
	SetDlgItemText(IDC_BUTTON_MOVEIN_WORKDEV, GetTextByLan("移入>>", "Movein>>"));
	SetDlgItemText(IDC_BUTTON_REMOVE_WORKDEV, GetTextByLan("移出<<", "Remove<<"));
	SetDlgItemText(IDC_BUTTON_REFRESH_NORMAL_DEVLIST, GetTextByLan("刷新", "Refresh"));
	SetDlgItemText(IDC_BUTTON_REFRESH_NORMAL_DEVSTATE, GetTextByLan("刷新", "Refresh"));
	SetDlgItemText(IDC_STATIC_HINT_LANGUAGE, GetTextByLan("注意：启用热备功能后，必须在热备机中添加工作机，否则热备功能无效。", "Note: After enabling the hot standby function, a working machine must be added to the hot standby machine, otherwise the hot standby function will be invalid."));

	DWORD dwStyle = m_lstHotBackupHostState.GetExtendedStyle();
	dwStyle = dwStyle | LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT| LVS_EX_CHECKBOXES;  
	m_lstHotBackupHostState.SetExtendedStyle(dwStyle);
	InsertColumn(m_lstHotBackupHostState, 0, GetTextByLan("序号", "No."), LVCFMT_LEFT, 50);
	InsertColumn(m_lstHotBackupHostState, 1, GetTextByLan("IP地址", "IPAddress"), LVCFMT_LEFT, 180);
	InsertColumn(m_lstHotBackupHostState, 2, GetTextByLan("当前状态", "CurrentState"), LVCFMT_LEFT, 80);

	m_lstWorkHost.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	InsertColumn(m_lstWorkHost, 0, GetTextByLan("序号", "No."), LVCFMT_LEFT, 50);
	InsertColumn(m_lstWorkHost, 1, GetTextByLan("IP地址", "IPAddress"), LVCFMT_LEFT, 180);

	m_lstWorkHostState.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	InsertColumn(m_lstWorkHostState, 0, GetTextByLan("序号", "No."), LVCFMT_LEFT, 50);
	InsertColumn(m_lstWorkHostState, 1, GetTextByLan("IP地址", "IPAddress"), LVCFMT_LEFT, 130);
	InsertColumn(m_lstWorkHostState, 2, GetTextByLan("连接状态", "ConnectState"), LVCFMT_LEFT, 80);
	InsertColumn(m_lstWorkHostState, 3, GetTextByLan("工作状态", "WorkState"), LVCFMT_LEFT, 80);
}

void CLS_StorageHotBackup::OnTimer(UINT_PTR nIDEvent)
{
	// TODO: Add your message handler code here and/or call default
	if (TIMER_REFRESH_TIMEOUT == nIDEvent) {
		m_btnRefreshWorkDevList.EnableWindow(TRUE);
		KillTimer(TIMER_REFRESH_TIMEOUT);
	}

	CLS_BasePage::OnTimer(nIDEvent);
}

void CLS_StorageHotBackup::OnBnClickedRadioNormalWorkmode()
{
	if (NVR_WORKMODE_WORKDEV != m_iWorkMode) {
		if (BST_CHECKED == m_rdoHotBackupMode.GetCheck()) {
			m_rdoHotBackupMode.SetCheck(BST_UNCHECKED);
		}
		SetNvrHostWorkMode(NVR_WORKMODE_WORKDEV);
	}
}

void CLS_StorageHotBackup::OnBnClickedRadioHotBackupMode()
{
	if (NVR_WORKMODE_HOTBACKUP != m_iWorkMode) {
		if (BST_CHECKED == m_rdoNormalWorkMode.GetCheck()) {
			m_rdoNormalWorkMode.SetCheck(BST_UNCHECKED);
		}
		SetNvrHostWorkMode(NVR_WORKMODE_HOTBACKUP);
	}
}

void CLS_StorageHotBackup::SetNvrHostWorkMode(int _iWorkMode)
{
	int iRet = RET_FAILED;
	iRet = MessageBox(GetTextByLan(_T("切换模式，设备将会自动重启，是否继续？"), _T("Switching modes, the device will automatically restart. Do you want to continue?"))
		, GetTextByLan(_T("提示"), _T("Warn")), MB_OKCANCEL);
	if (IDOK == iRet) {
		iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_WORKMODE, 0, &_iWorkMode, sizeof(int));
		if (RET_SUCCESS == iRet) {
			AddLog(LOG_TYPE_SUCC, "", "SetDevConfig NET_CLIENT_BACKUP_WORKMODE success! (%d, %d)", m_iLogonID, _iWorkMode);	
		} else {
			AddLog(LOG_TYPE_FAIL, "", "SetDevConfig NET_CLIENT_BACKUP_WORKMODE failed! (%d, %d)", m_iLogonID, _iWorkMode);	
		}
	} else {
		UpdateNvrHostWorkMode();
	}
}

void CLS_StorageHotBackup::OnBnClickedCheckEnableHotBackup()
{
	if (NVR_WORKMODE_WORKDEV != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	int iEnableValue = m_chkEnableHotBackup.GetCheck();
	int iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_WORKDEV_BACKUP, PARAM_CHANNEL_ALL, iEnableValue);
	if (iRet < 0) {
		AddLog(LOG_TYPE_FAIL, "", "[CLS_StorageHotBackup::SetEnableHotBackup] Set EnableValue Failed  LogonID=%d EnableID=%d"
			, m_iLogonID, CI_COMMON_ID_WORKDEV_BACKUP);
	}
}

void CLS_StorageHotBackup::OnBnClickedButtonAddHotBackupHost()
{
	if (NVR_WORKMODE_WORKDEV != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	CString cstrIP;
	m_edtHotBackupDevIp.GetWindowText(cstrIP);
	if (1 != IsValidIP(cstrIP.GetBuffer())) {
		AddLog(LOG_TYPE_FAIL, "", "Hot Backup host IP is invalid");
		return;
	}

	for(int i = 0; i < m_lstHotBackupHostState.GetItemCount(); ++i)
	{
		CString strTempEx = "";
		strTempEx = m_lstHotBackupHostState.GetItemText(i,1);
		if (cstrIP == strTempEx) {
			MessageBox(GetTextByLan(_T("输入的IP已存在，请重新输入！"), _T("The IP Address you entered already exists,please input again!"))
				, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
			return ;
		}
	}

	BackupDevModify tHotBackupHostModify = {0};
	strcpy(tHotBackupHostModify.cUserName, "admin");
	strcpy(tHotBackupHostModify.cIP, cstrIP.GetBuffer());
	tHotBackupHostModify.iBufSize = sizeof(BackupDevModify);
	tHotBackupHostModify.iDevType = NVR_WORKDEV_OPT_BACKUPDEV;
	tHotBackupHostModify.iOptType = NVR_HOTBACKUP_ADD_HOST;
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_MODIFY, m_iChannelNO, &tHotBackupHostModify, sizeof(BackupDevModify));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NET_CLIENT_BACKUP_MODIFY NVR_WORKDEV_OPT_BACKUPDEV NVR_HOTBACKUP_ADD_HOST failed!");
	}
}

void CLS_StorageHotBackup::OnBnClickedButtonDeleteHotBackupHost()
{
	if (NVR_WORKMODE_WORKDEV != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	for(int i = 0; i < m_lstHotBackupHostState.GetItemCount(); ++i)
	{
		if(BST_CHECKED != m_lstHotBackupHostState.GetCheck(i)) {
			continue;
		}

		CString cstrIP = m_lstHotBackupHostState.GetItemText(i, n_ListIp);
		BackupDevModify tHotBackupHostModify = {0};
		strcpy(tHotBackupHostModify.cIP, cstrIP.GetBuffer());
		tHotBackupHostModify.iBufSize = sizeof(BackupDevModify);
		tHotBackupHostModify.iDevType = NVR_WORKDEV_OPT_BACKUPDEV;
		tHotBackupHostModify.iOptType = NVR_HOTBACKUP_DEL_HOST;
		int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_MODIFY, m_iChannelNO, &tHotBackupHostModify, sizeof(BackupDevModify));
		if (RET_SUCCESS != iRet) {
			AddLog(LOG_TYPE_FAIL, "", "NET_CLIENT_BACKUP_MODIFY NVR_WORKDEV_OPT_BACKUPDEV NVR_HOTBACKUP_DEL_HOST failed!");
		}
	}
}

void CLS_StorageHotBackup::OnBnClickedButtonRefreshHotBackupHostState()
{
	if (NVR_WORKMODE_WORKDEV != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	UpdateHotBackupHostStateList();
}

void CLS_StorageHotBackup::OnBnClickedButtonMoveinWorkHost()
{
	if (NVR_WORKMODE_HOTBACKUP != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	int iSelectIdx = m_lstWorkHost.GetSelectionMark();
	if (-1 == iSelectIdx) {
		AddLog(LOG_TYPE_FAIL, "", "CLS_StorageHotBackup::OnBnClickedButtonMoveinWorkHost -1 == iSelectIdx");
		return;
	}

	CString cstrPassword;
	m_edtWorkHostPwd.GetWindowText(cstrPassword);
	//获取选中机器的信息
	CString cstrIP = m_lstWorkHost.GetItemText(iSelectIdx, n_ListIp);
	//添加工作机
	BackupDevModify tWorkHostModify = {0};
	strcpy(tWorkHostModify.cUserName, "admin");
	strcpy(tWorkHostModify.cPassword, cstrPassword.GetBuffer());
	strcpy(tWorkHostModify.cIP, cstrIP.GetBuffer());
	tWorkHostModify.iBufSize = sizeof(BackupDevModify);
	tWorkHostModify.iDevType = NVR_BACKUPDEV_OPT_WORKDEV;
	tWorkHostModify.iOptType = NVR_HOTBACKUP_ADD_HOST;
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_MODIFY, m_iChannelNO, &tWorkHostModify, sizeof(BackupDevModify));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NET_CLIENT_BACKUP_MODIFY NVR_BACKUPDEV_OPT_WORKDEV NVR_HOTBACKUP_ADD_HOST failed!");
	}
}

void CLS_StorageHotBackup::OnBnClickedButtonRemoveWorkHost()
{
	if (NVR_WORKMODE_HOTBACKUP != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	int iSelectIdx = m_lstWorkHostState.GetSelectionMark();
	if (-1 == iSelectIdx) {
		AddLog(LOG_TYPE_FAIL, "", "CLS_StorageHotBackup::OnBnClickedButtonRemoveWorkHost -1 == iSelectIdx");
		return;
	}

	//获取选中机器的信息
	CString cstrIP = m_lstWorkHostState.GetItemText(iSelectIdx, n_ListIp);
	//删除工作机
	BackupDevModify tWorkHostModify = {0};
	strcpy(tWorkHostModify.cIP, cstrIP.GetBuffer());
	tWorkHostModify.iBufSize = sizeof(BackupDevModify);
	tWorkHostModify.iDevType = NVR_BACKUPDEV_OPT_WORKDEV;
	tWorkHostModify.iOptType = NVR_HOTBACKUP_DEL_HOST;
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_MODIFY, m_iChannelNO, &tWorkHostModify, sizeof(BackupDevModify));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NET_CLIENT_BACKUP_MODIFY NVR_BACKUPDEV_OPT_WORKDEV NVR_HOTBACKUP_DEL_HOST failed!");
	}
}

void CLS_StorageHotBackup::OnBnClickedButtonRefreshWorkHostList()
{
	if (NVR_WORKMODE_HOTBACKUP != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	//启动热备机搜索工作机
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_SEEK_WORKDEV, m_iChannelNO, NULL, 0);
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig->NET_CLIENT_BACKUP_SEEK_WORKDEV failed!");
		return;
	}

	m_btnRefreshWorkDevList.EnableWindow(FALSE);
	SetTimer(TIMER_REFRESH_TIMEOUT, REFRESH_TIMEOUT_TIME, NULL);
}

void CLS_StorageHotBackup::OnBnClickedButtonRefreshWorkHostState()
{
	if (NVR_WORKMODE_HOTBACKUP != m_iWorkMode) {
		MessageBox(GetTextByLan(_T("当前设备不支持此操作！"), _T("The current device does not support this operation!"))
			, GetTextByLan(_T("提示"), _T("Warn")), MB_OK);
		return;
	}

	UpdateWorkHostStateList();	
}

void CLS_StorageHotBackup::UpdateNvrHostWorkMode()
{
	if (m_iLogonID < 0) {
		AddLog(LOG_TYPE_MSG, "", "[CLS_StorageHotBackup::UpdateDevWorkMode]->Invalid logon id(%d)", m_iLogonID);
		return;
	}

	int iByteRet = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_WORKMODE, 0, &m_iWorkMode, sizeof(int), &iByteRet);
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "GetDevConfig NET_CLIENT_BACKUP_WORKMODE failed! (%d)", m_iLogonID);
	} else {
		if (NVR_WORKMODE_HOTBACKUP == m_iWorkMode) {
			m_rdoHotBackupMode.SetCheck(BST_CHECKED);
			m_rdoNormalWorkMode.SetCheck(BST_UNCHECKED);
			//当前设备是热备机，需要更新关联的工作机信息
			UpdateWorkHostStateList();
		} else if (NVR_WORKMODE_WORKDEV == m_iWorkMode) {
			m_rdoNormalWorkMode.SetCheck(BST_CHECKED);
			m_rdoHotBackupMode.SetCheck(BST_UNCHECKED);
			//当前设备是工作机，需要更新其关联的热备信息
			UpdateWorkHostEnableHotSpare();
			UpdateHotBackupHostStateList();
		} else {
			m_rdoNormalWorkMode.SetCheck(BST_UNCHECKED);
			m_rdoHotBackupMode.SetCheck(BST_UNCHECKED);
			AddLog(LOG_TYPE_MSG, "", "Current Device not support HotBackup, logon id(%d)", m_iLogonID);
		}
		UpdateWindowEnable();
		AddLog(LOG_TYPE_SUCC, "", "GetDevConfig NET_CLIENT_BACKUP_WORKMODE success! (%d)", m_iLogonID);
	}
}

void CLS_StorageHotBackup::UpdateWorkHostEnableHotSpare()
{
	int iEnableValue = -1;
	int iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_WORKDEV_BACKUP, PARAM_CHANNEL_ALL, &iEnableValue);
	if (iRet < 0 || iEnableValue < 0) {
		AddLog(LOG_TYPE_FAIL, "", "[CLS_StorageHotBackup::UpdateEnableHotBackup] Get EnableValue Failed  LogonID=%d EnableID=%d"
			, m_iLogonID, CI_COMMON_ID_WORKDEV_BACKUP);
	} else {
		m_chkEnableHotBackup.SetCheck(iEnableValue);
	}

	if(BST_CHECKED == m_chkEnableHotBackup.GetCheck()) {
		m_edtHotBackupDevIp.EnableWindow(TRUE);
	} else {
		m_edtHotBackupDevIp.EnableWindow(FALSE);
	}
}

void CLS_StorageHotBackup::UpdateWorkHostList()
{
	int iCount = 0;
	int iReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_GET_SEEK_COUNT, m_iChannelNO, &iCount, sizeof(int), &iReturn);
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig->NET_CLIENT_BACKUP_GET_SEEK_COUNT failed");
		return;
	}

	m_lstWorkHost.DeleteAllItems();

	WorkDevInfo tWorkDevInfo = {0};
	for (int i = 0; i<iCount; ++i)
	{
		memset(&tWorkDevInfo, 0, sizeof(WorkDevInfo));
		tWorkDevInfo.iBufSize = sizeof(WorkDevInfo);
		tWorkDevInfo.iIndex = i;

		iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_GET_SEEK_WORKDEV, m_iChannelNO, &tWorkDevInfo, sizeof(WorkDevInfo), &iReturn);
		if (RET_SUCCESS != iRet) {
			AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig->NET_CLIENT_BACKUP_GET_SEEK_WORKDEV failed\n");
			continue;
		}

		bool bIsExist = false;
		CString cstrTemp = "";
		for (int i = 0; i < m_lstWorkHost.GetItemCount(); i++)
		{
			cstrTemp = m_lstWorkHost.GetItemText(i, 1);
			if (0 == strcmp(tWorkDevInfo.cIP, cstrTemp)) {
				bIsExist = true;
			}
		}

		if (bIsExist) {
			continue;
		}

		int iListCount = m_lstWorkHost.GetItemCount();
		int iIndex = 1;
		m_lstWorkHost.InsertItem(iListCount, IntToStr(iListCount+1).c_str());
		m_lstWorkHost.SetItemText(iListCount, iIndex++, tWorkDevInfo.cIP);
	}
}

void CLS_StorageHotBackup::UpdateWorkHostStateList()
{
	//获取热备机关联的工作机个数
	int iWorkHostCount = 0;
	int iBytesRet = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_GET_WORKDEV_NUM, m_iChannelNO, &iWorkHostCount, sizeof(int), &iBytesRet);
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig->NET_CLIENT_BACKUP_GET_WORKDEV_NUM failed");
		return;
	}

	//清除设备状态列表
	m_lstWorkHostState.DeleteAllItems();

	//更新设备状态列表
	WorkDevState tWorkHostState = {0};
	for (int i = 0; i < iWorkHostCount; ++i)
	{
		memset(&tWorkHostState, 0, sizeof(BackupDevState));
		tWorkHostState.iBufSize = sizeof(BackupDevState);
		tWorkHostState.iIndex = i;
		iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_WORKDEV_STATE, m_iChannelNO, &tWorkHostState, sizeof(WorkDevState), &iBytesRet);
		if (RET_SUCCESS != iRet) {
			AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig->NET_CLIENT_BACKUP_WORKDEV_STATE failed!");
			continue;
		}

		int iListCount = m_lstWorkHostState.GetItemCount();
		int iIndex = 1;
		m_lstWorkHostState.InsertItem(iListCount, IntToStr(iListCount+1).c_str());
		m_lstWorkHostState.SetItemText(iListCount, iIndex++, tWorkHostState.cIP);
		if (n_BackUpReady == tWorkHostState.iState || n_BackUpOver == tWorkHostState.iState) {
			m_lstWorkHostState.SetItemText(iListCount, iIndex++,  GetTextByLan("On Line", "在线"));
		} else {
			m_lstWorkHostState.SetItemText(iListCount, iIndex++,  GetTextByLan("Off Line", "不在线"));
		}
		m_lstWorkHostState.SetItemText(iListCount, iIndex++, GetStringByHostState(NVR_WORKMODE_HOTBACKUP, tWorkHostState.iState));
	}
}

void CLS_StorageHotBackup::UpdateHotBackupHostStateList()
{
	int iHotBackupHostCount = 0;
	int iBytesRet = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_DEV_NUM, m_iChannelNO, &iHotBackupHostCount, sizeof(int), &iBytesRet);
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig->NET_CLIENT_BACKUP_DEV_NUM failed");
		return;
	}

	//清空列表
	m_lstHotBackupHostState.DeleteAllItems();

	//更新状态
	WorkDevState tHotBackupState = {0};
	for(int i = 0; i < iHotBackupHostCount; ++i)
	{
		memset(&tHotBackupState, 0, sizeof(BackupDevState));
		tHotBackupState.iBufSize = sizeof(BackupDevState);
		tHotBackupState.iIndex = i;
		iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BACKUP_BACKUPDEV_STATE, m_iChannelNO, &tHotBackupState, sizeof(WorkDevState), &iBytesRet);
		if (RET_SUCCESS != iRet) {
			AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig->NET_CLIENT_BACKUP_BACKUPDEV_STATE");
			continue;
		}

		int iListCount = m_lstHotBackupHostState.GetItemCount();
		int iIndex = 1;
		m_lstHotBackupHostState.InsertItem(iListCount, IntToStr(iListCount+1).c_str());
		m_lstHotBackupHostState.SetItemText(iListCount, iIndex++, tHotBackupState.cIP);
		if (n_OffLine == tHotBackupState.iState) {
			m_lstHotBackupHostState.SetItemText(iListCount, iIndex++,  GetTextByLan("连接失败", "Connection Fail"));
		} else if(n_OnLine == tHotBackupState.iState ) {
			m_lstHotBackupHostState.SetItemText(iListCount, iIndex++,  GetTextByLan("连接成功", "Connection Success"));
		} else {
			m_lstHotBackupHostState.SetItemText(iListCount, iIndex++,  GetTextByLan("正在同步", "Synchronizing")+_T("(")+IntToCString(tHotBackupState.iProgress)+_T("%)"));
		}
	}
}

CString CLS_StorageHotBackup::GetStringByHostState( int _iHostType, int _iState )
{
	CString cstrState = "";

	if (NVR_WORKMODE_WORKDEV == _iHostType) {
		switch(_iState)
		{
		case n_OnLine:
			cstrState = GetTextByLan("连接成功", "Connection Success");//连接成功
			break;
		case n_OffLine:
			cstrState = GetTextByLan("连接失败", "Connection Fail");//连接失败
			break;
		case n_WorkOver:
			cstrState = GetTextByLan("正在同步", "Synchronizing");//正在同步
			break;
		default:
			cstrState = GetTextByLan("连接失败", "Connection Fail");
			break;
		}
	} else if (NVR_WORKMODE_HOTBACKUP == _iHostType) {
		switch(_iState)
		{
		case n_BackUpReady:
			cstrState = GetTextByLan("正在热备", "Hot Sparing");//正在热备
			break;
		case n_BackUping:
			cstrState = GetTextByLan("正在备份", "Backing Up");//正在备份
			break;
		case n_BackUpOver:
			cstrState = GetTextByLan("正在同步", "Synchronizing");//正在同步
			break;
		case n_BackUpOffLine:
			cstrState = GetTextByLan("正在热备", "Hot Sparing");//正在热备
			break;
		case n_BackPwdWrong:
			cstrState = GetTextByLan("用户名或者密码错误", "Password or userName error");//用户名或密码错误
			break;
		default:
			cstrState = GetTextByLan("热备模式", "Hot Spare Mode");
			break;
		}
	}

	return cstrState;
}

void CLS_StorageHotBackup::UpdateWindowEnable()
{
	if (NVR_WORKMODE_WORKDEV == m_iWorkMode) {
		GetDlgItem(IDC_CHECK_ENABLE_HOTBACKUP)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_HOTBACKUP_DEVIP)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_ADD_HOTBACKUP_DEV)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_DELETE_HOTBACKUP_DEV)->EnableWindow(TRUE);
		GetDlgItem(IDC_LIST_HOTBACKUP_DEV)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_REFRESH_HBU_DEVLIST)->EnableWindow(TRUE);

		m_edtWorkHostPwd.SetWindowText("");
		m_lstWorkHost.DeleteAllItems();
		m_lstWorkHostState.DeleteAllItems();
		GetDlgItem(IDC_LIST_NORMAL_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_WORKHOST_PWD)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REFRESH_NORMAL_DEVLIST)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_MOVEIN_WORKDEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REMOVE_WORKDEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_LIST_NORMAL_DEV_STATE)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REFRESH_NORMAL_DEVSTATE)->EnableWindow(FALSE);
	} else if (NVR_WORKMODE_HOTBACKUP == m_iWorkMode) {
		m_edtHotBackupDevIp.SetWindowText("");
		m_lstHotBackupHostState.DeleteAllItems();
		m_chkEnableHotBackup.SetCheck(BST_UNCHECKED);
		GetDlgItem(IDC_CHECK_ENABLE_HOTBACKUP)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_HOTBACKUP_DEVIP)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_ADD_HOTBACKUP_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_DELETE_HOTBACKUP_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_LIST_HOTBACKUP_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REFRESH_HBU_DEVLIST)->EnableWindow(FALSE);

		GetDlgItem(IDC_LIST_NORMAL_DEV)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_WORKHOST_PWD)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_REFRESH_NORMAL_DEVLIST)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_MOVEIN_WORKDEV)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_REMOVE_WORKDEV)->EnableWindow(TRUE);
		GetDlgItem(IDC_LIST_NORMAL_DEV_STATE)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_REFRESH_NORMAL_DEVSTATE)->EnableWindow(TRUE);
	} else {
		m_edtWorkHostPwd.SetWindowText("");
		m_lstWorkHost.DeleteAllItems();
		m_lstWorkHostState.DeleteAllItems();
		m_edtHotBackupDevIp.SetWindowText("");
		m_lstHotBackupHostState.DeleteAllItems();
		m_chkEnableHotBackup.SetCheck(BST_UNCHECKED);
		GetDlgItem(IDC_CHECK_ENABLE_HOTBACKUP)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_HOTBACKUP_DEVIP)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_ADD_HOTBACKUP_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_DELETE_HOTBACKUP_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_LIST_HOTBACKUP_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REFRESH_HBU_DEVLIST)->EnableWindow(FALSE);
		GetDlgItem(IDC_LIST_NORMAL_DEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_WORKHOST_PWD)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REFRESH_NORMAL_DEVLIST)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_MOVEIN_WORKDEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REMOVE_WORKDEV)->EnableWindow(FALSE);
		GetDlgItem(IDC_LIST_NORMAL_DEV_STATE)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_REFRESH_NORMAL_DEVSTATE)->EnableWindow(FALSE);
	}
}


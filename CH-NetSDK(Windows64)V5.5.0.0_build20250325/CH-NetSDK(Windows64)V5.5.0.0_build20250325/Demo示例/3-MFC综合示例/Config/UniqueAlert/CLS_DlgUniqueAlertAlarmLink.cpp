#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertAlarmLink.h"

const int g_iIDS_AlertLevel_Link[MAX_UNIQUE_ALERT_ALARM_LINK_LEVEL - 1] = {IDS_ALERT_LEVEL_1, IDS_ALERT_LEVEL_2, IDS_ALERT_LEVEL_3}; 

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertAlarmLink, CDialog)

CLS_DlgUniqueAlertAlarmLink::CLS_DlgUniqueAlertAlarmLink(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertAlarmLink::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
	m_pclsChanCheck = NULL;
}

CLS_DlgUniqueAlertAlarmLink::~CLS_DlgUniqueAlertAlarmLink()
{
	if (NULL != m_pclsChanCheck)
	{
		delete m_pclsChanCheck;
		m_pclsChanCheck = NULL;
	}
}

void CLS_DlgUniqueAlertAlarmLink::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALERT_ALINK_SCENE, m_cboAlertScene);
	DDX_Control(pDX, IDC_CBO_ALERT_ALINK_ALERT_TYPE, m_cboAlertType);
	DDX_Control(pDX, IDC_CBO_ALERT_ALINK_ALERT_LEVEL, m_cboAlertLevel);
	DDX_Control(pDX, IDC_CBO_ALERT_ALINK_LINK_INFO_TYPE_1, m_cboLinkType_1);
	DDX_Control(pDX, IDC_CBO_ALERT_ALINK_LINK_INFO_TYPE_2, m_cboLinkType_2);
	DDX_Control(pDX, IDC_CBO_ALERT_ALINK_WHITE_LIGHT_MODE, m_cboLinkWhiteLightMode);
}

BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertAlarmLink, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_ALERT_ALINK_LINK_INFO_SET_1, &CLS_DlgUniqueAlertAlarmLink::OnBnClickedBtnAlertAlinkLinkInfoSet1)
	ON_BN_CLICKED(IDC_BTN_ALERT_ALINK_LINK_INFO_SET_2, &CLS_DlgUniqueAlertAlarmLink::OnBnClickedBtnAlertAlinkLinkInfoSet2)
	ON_BN_CLICKED(IDC_BTN_ALERT_ALINK_LINK_INFO_SET_3, &CLS_DlgUniqueAlertAlarmLink::OnBnClickedBtnAlertAlinkLinkInfoSet3)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_ALINK_SCENE, &CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkScene)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_ALINK_ALERT_TYPE, &CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkAlertType)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_ALINK_ALERT_LEVEL, &CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkAlertLevel)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_ALINK_LINK_INFO_TYPE_1, &CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkLinkInfoType1)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_ALINK_LINK_INFO_TYPE_2, &CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkLinkInfoType2)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertAlarmLink::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_InitDlgWidget();
	return TRUE;
}

void CLS_DlgUniqueAlertAlarmLink::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertAlarmLink::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (m_iLogonID == _iLogonID && m_iChannelNo == _iChannelNo && m_iStreamNo == _iStreamNo)
	{
		return;
	}

	m_iLogonID = _iLogonID;
	m_iChannelNo = ((_iChannelNo < 0) ? 0 : _iChannelNo);
	m_iStreamNo = _iStreamNo;

	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertAlarmLink::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertAlarmLink::UI_InitDlgItemText()
{
	// alert scene
	int iCurSel = m_cboAlertScene.GetCurSel();
	m_cboAlertScene.ResetContent();
	for (int i = 0; i < MAX_UNIQUE_ALERT_SCENE_NUM; i++)
	{
		m_cboAlertScene.SetItemData(m_cboAlertScene.AddString(GetTextEx(IDS_ALERT_SCENE) + IntToCString(i + 1)), i);
	}
	m_cboAlertScene.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	// alert type
	iCurSel = m_cboAlertType.GetCurSel();
	m_cboAlertType.ResetContent();
	m_cboAlertType.SetItemData(m_cboAlertType.AddString(GetTextByLan(_T("周界警戒"), _T("Alert Perimeter"))), UNIQUE_ALERT_TYPE_PERIMETER);
	m_cboAlertType.SetItemData(m_cboAlertType.AddString(GetTextByLan(_T("绊线警戒"), _T("Alert Tripwire"))), UNIQUE_ALERT_TYPE_TRIPWIRE);
    m_cboAlertType.SetItemData(m_cboAlertType.AddString(GetTextByLan(_T("翻墙警戒"), _T("Alert ClimbWall"))), UNIQUE_ALERT_TYPE_CLIMBWALL);
    m_cboAlertType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	// alert level
	iCurSel = m_cboAlertLevel.GetCurSel();
	m_cboAlertLevel.ResetContent();
	for (int i = 0; i < MAX_UNIQUE_ALERT_ALARM_LINK_LEVEL - 1; i++)
	{
		m_cboAlertLevel.SetItemData(m_cboAlertLevel.AddString(GetTextEx(g_iIDS_AlertLevel_Link[i])), i);
	}
	m_cboAlertLevel.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));


	//Link sound, laser, tracking
	iCurSel = m_cboLinkType_1.GetCurSel();
	m_cboLinkType_1.ResetContent();
	m_cboLinkType_1.SetItemData(m_cboLinkType_1.AddString(GetTextEx(IDS_CONFIG_VCA_AUDIO)), ALARMLINKTYPE_LINKSOUND);
	m_cboLinkType_1.SetItemData(m_cboLinkType_1.AddString(GetTextEx(IDS_LASER)), ALARMLINKTYPE_LASER);
	m_cboLinkType_1.SetItemData(m_cboLinkType_1.AddString(GetTextEx(IDS_VCA_ALARM_EVENT_TRACK)), ALARMLINKTYPE_TRACKING);
	m_cboLinkType_1.SetItemData(m_cboLinkType_1.AddString(GetTextByLan("警灯", "AlarmLamp")), ALARMLINKTYPE_ALARM_LAMP);
	m_cboLinkType_1.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	//Port output, video, snapshot
	iCurSel = m_cboLinkType_2.GetCurSel();
	m_cboLinkType_2.ResetContent();
	m_cboLinkType_2.SetItemData(m_cboLinkType_2.AddString(GetTextEx(IDS_CONFIG_VCA_OUTPORT)), ALARMLINKTYPE_LINKOUTPORT);
	m_cboLinkType_2.SetItemData(m_cboLinkType_2.AddString(GetTextEx(IDS_CONFIG_VCA_RECORD)), ALARMLINKTYPE_LINKRECORD);
	m_cboLinkType_2.SetItemData(m_cboLinkType_2.AddString(GetTextEx(IDS_CONFIG_VCA_SNAP)), ALARMLINKTYPE_LINKSNAP);
	m_cboLinkType_2.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	// white light flashes
	iCurSel = m_cboLinkWhiteLightMode.GetCurSel();
	m_cboLinkWhiteLightMode.ResetContent();
	m_cboLinkWhiteLightMode.SetItemData(m_cboLinkWhiteLightMode.AddString(GetTextByLan(_T("不使能"), _T("Disable"))), 0);
	m_cboLinkWhiteLightMode.SetItemData(m_cboLinkWhiteLightMode.AddString(GetTextByLan(_T("闪光模式"), _T("Flashing Mode"))), 1);
	m_cboLinkWhiteLightMode.SetItemData(m_cboLinkWhiteLightMode.AddString(GetTextByLan(_T("常亮"), _T("Always Light On"))), 2);
	m_cboLinkWhiteLightMode.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	SetDlgItemText(IDC_STC_ALERT_ALINK_SCENE, GetTextByLan(_T("场景"), _T("SceneId")));
	SetDlgItemText(IDC_STC_ALERT_ALINK_ALERT_TYPE, GetTextByLan(_T("警戒类型"), _T("Alert Type")));
	SetDlgItemText(IDC_STC_ALERT_ALINK_ALERT_LEVEL, GetTextByLan(_T("警戒级别"), _T("Alert level")));
	SetDlgItemText(IDC_CHK_ALERT_ALINK_NEXT_LEVEL, GetTextByLan(_T("是否进入下一级警戒"), _T("Enter the next level")));
	SetDlgItemText(IDC_STC_ALERT_ALINK_DELAY_TIME, GetTextByLan(_T("报警滞留时间"), _T("Alarm residence time")));
	SetDlgItemText(IDC_GPO_ALERT_ALINK_LINK_INFO_1, GetTextByLan(_T("联动信息（声音、激光、跟踪）"), _T("Linkage information (voice, laser, tracking)")));
	SetDlgItemText(IDC_GPO_ALERT_ALINK_LINK_INFO_2, GetTextByLan(_T("联动信息（端口输出、录像、抓拍）"), _T("Linkage information (port output, video, captured)")));
	SetDlgItemText(IDC_GPO_ALERT_ALINK_LINK_INFO_3, GetTextByLan(_T("联动信息（白光闪烁）"), _T("Linkage information (white light flashing)")));
	SetDlgItemText(IDC_STC_ALERT_ALINK_LINK_INFO_TYPE_1, GetTextByLan(_T("联动类型"), _T("Linkage type")));
	SetDlgItemText(IDC_STC_ALERT_ALINK_LINK_INFO_TYPE_2, GetTextByLan(_T("联动类型"), _T("Linkage type")));
	SetDlgItemText(IDC_CHK_ALERT_ALINK_LINK_ENABLE_1, GetTextByLan(_T("使能"), _T("Enable")));
	SetDlgItemText(IDC_BTN_ALERT_ALINK_LINK_INFO_SET_1, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BTN_ALERT_ALINK_LINK_INFO_SET_2, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STC_ALERT_ALINK_WHITE_LIGHT_MODE, GetTextByLan(_T("白光灯模式"), _T("White light pattern")));
	SetDlgItemText(IDC_STC_ALERT_ALINK_WHITE_LIGHT_INFO, GetTextByLan(_T("附加信息"), _T("Additional information")));
	SetDlgItemText(IDC_BTN_ALERT_ALINK_LINK_INFO_SET_3, GetTextByLan(_T("设置"), _T("Set")));
}

void CLS_DlgUniqueAlertAlarmLink::UI_UpdateInterfaceParam()
{
	UI_Update_AlarmLinkInfo_Group_1();
	UI_Update_AlarmLinkInfo_Group_2();
	UI_Update_AlarmLinkInfo_WhiteLight();
}

void CLS_DlgUniqueAlertAlarmLink::UI_InitDlgWidget()
{
	if (NULL != m_pclsChanCheck)
	{
		return;
	}

	RECT tTemp = {0};
	m_pclsChanCheck = new CLS_ChanCheck(this);
	if (NULL == m_pclsChanCheck)
	{
		goto EXIT_FUNC;
	}
	m_pclsChanCheck->Create(IDD_DLG_CFG_CHANNEL_CHECK, this);
	GetDlgItem(IDC_PCE_ALERT_ALINK_LINK_INFO_2)->GetClientRect(&tTemp);
	GetDlgItem(IDC_PCE_ALERT_ALINK_LINK_INFO_2)->ClientToScreen(&tTemp);
	this->ScreenToClient(&tTemp);
	m_pclsChanCheck->MoveWindow(&tTemp);
	m_pclsChanCheck->ShowWindow(TRUE);

EXIT_FUNC:
	return;
}

void CLS_DlgUniqueAlertAlarmLink::UI_Get_AlarmBaseInfo(UniqueAlertAlarmLink& _tAlarmInfo)
{
	_tAlarmInfo.iSize = sizeof(UniqueAlertAlarmLink);
	_tAlarmInfo.iSceneId = m_cboAlertScene.GetItemData(m_cboAlertScene.GetCurSel());
	_tAlarmInfo.iAlertType = m_cboAlertType.GetItemData(m_cboAlertType.GetCurSel());
	_tAlarmInfo.iLinkLevel = m_cboAlertLevel.GetItemData(m_cboAlertLevel.GetCurSel());
	_tAlarmInfo.iEventNo = 0;
	_tAlarmInfo.iRetentionTime = GetDlgItemInt(IDC_EDT_ALERT_ALINK_DELAY_TIME);
	_tAlarmInfo.iNextLevel = (BST_CHECKED == ((CButton*)GetDlgItem(IDC_CHK_ALERT_ALINK_NEXT_LEVEL))->GetCheck());
}

void CLS_DlgUniqueAlertAlarmLink::UI_Update_AlarmBaseInfo(UniqueAlertAlarmLink _tAlarmInfo)
{
	SetDlgItemInt(IDC_EDT_ALERT_ALINK_DELAY_TIME, _tAlarmInfo.iRetentionTime);
	((CButton*)GetDlgItem(IDC_CHK_ALERT_ALINK_NEXT_LEVEL))->SetCheck(_tAlarmInfo.iNextLevel ? BST_CHECKED : BST_UNCHECKED);
}

void CLS_DlgUniqueAlertAlarmLink::UI_Update_AlarmLinkInfo_Group_1()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	UniqueAlertAlarmLink tAlarmLinkInfo = {0};
	UI_Get_AlarmBaseInfo(tAlarmLinkInfo);
	tAlarmLinkInfo.iLinkType = m_cboLinkType_1.GetItemData(m_cboLinkType_1.GetCurSel());
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_LINK, m_iChannelNo, &tAlarmLinkInfo, tAlarmLinkInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		memset(&tAlarmLinkInfo, 0, sizeof(tAlarmLinkInfo));
	}
	
	((CButton*)GetDlgItem(IDC_CHK_ALERT_ALINK_LINK_ENABLE_1))->SetCheck(tAlarmLinkInfo.uLinkParam.iCommonSet[0] ? BST_CHECKED : BST_UNCHECKED);

	UI_Update_AlarmBaseInfo(tAlarmLinkInfo);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertAlarmLink::UI_Update_AlarmLinkInfo_Group_2()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iTmp = 0;
	int iChkOpreateNum = 0;
	int iLinkType = m_cboLinkType_2.GetItemData(m_cboLinkType_2.GetCurSel());
	if (ALARMLINKTYPE_LINKOUTPORT == iLinkType)
	{
		NetClient_GetAlarmPortNum(m_iLogonID, &iTmp, &iChkOpreateNum);
	}
	else
	{
		NetClient_GetChannelNum(m_iLogonID, &iChkOpreateNum);
	}

	int iRet = RET_SUCCESS;
	UniqueAlertAlarmLink tAlarmLinkInfo = {0};
	UI_Get_AlarmBaseInfo(tAlarmLinkInfo);
	tAlarmLinkInfo.iLinkType = iLinkType;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_LINK, m_iChannelNo, &tAlarmLinkInfo, tAlarmLinkInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		memset(&tAlarmLinkInfo, 0, sizeof(tAlarmLinkInfo));
	}

	if (NULL != m_pclsChanCheck)
	{
		m_pclsChanCheck->InitData(iChkOpreateNum, tAlarmLinkInfo.uLinkParam.iCommonSet);
	}
	else
	{
		OutPutLogMsg("[CLS_DlgUniqueAlertAlarmLink::UI_Update_AlarmLinkInfo_Group_2] NULL == m_pclsChanCheck!");
	}

	UI_Update_AlarmBaseInfo(tAlarmLinkInfo);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertAlarmLink::UI_Update_AlarmLinkInfo_WhiteLight()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	UniqueAlertAlarmLink tAlarmLinkInfo = {0};
	UI_Get_AlarmBaseInfo(tAlarmLinkInfo);
	tAlarmLinkInfo.iLinkType = ALARMLINKTYPE_FLASHING_WHITE;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_LINK, m_iChannelNo, &tAlarmLinkInfo, tAlarmLinkInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		memset(&tAlarmLinkInfo, 0, sizeof(tAlarmLinkInfo));
	}

	TLinkFlashingWhite_V3& tLightInfo = tAlarmLinkInfo.uLinkParam.tLinkFlashingWhite;
	m_cboLinkWhiteLightMode.SetCurSel(GetCboSel(&m_cboLinkWhiteLightMode, tLightInfo.iEnable));
	SetDlgItemInt(IDC_EDT_ALERT_ALINK_WHITE_LIGHT_INFO, tLightInfo.iExternInfo);

	UI_Update_AlarmBaseInfo(tAlarmLinkInfo);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[AlarmLink 3](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[AlarmLink 3](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertAlarmLink::OnBnClickedBtnAlertAlinkLinkInfoSet1()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	UniqueAlertAlarmLink tAlarmLinkInfo = {0};
	UI_Get_AlarmBaseInfo(tAlarmLinkInfo);
	tAlarmLinkInfo.iLinkType = m_cboLinkType_1.GetItemData(m_cboLinkType_1.GetCurSel());
	tAlarmLinkInfo.uLinkParam.iCommonSet[0] = (BST_CHECKED == ((CButton*)GetDlgItem(IDC_CHK_ALERT_ALINK_LINK_ENABLE_1))->GetCheck());
	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_LINK, m_iChannelNo, &tAlarmLinkInfo, tAlarmLinkInfo.iSize);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertAlarmLink::OnBnClickedBtnAlertAlinkLinkInfoSet2()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	UniqueAlertAlarmLink tAlarmLinkInfo = {0};
	UI_Get_AlarmBaseInfo(tAlarmLinkInfo);
	tAlarmLinkInfo.iLinkType = m_cboLinkType_2.GetItemData(m_cboLinkType_2.GetCurSel());
	if (NULL != m_pclsChanCheck)
	{
		m_pclsChanCheck->GetChanValue(tAlarmLinkInfo.uLinkParam.iCommonSet);
	}
	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_LINK, m_iChannelNo, &tAlarmLinkInfo, tAlarmLinkInfo.iSize);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertAlarmLink::OnBnClickedBtnAlertAlinkLinkInfoSet3()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	UniqueAlertAlarmLink tAlarmLinkInfo = {0};
	UI_Get_AlarmBaseInfo(tAlarmLinkInfo);
	tAlarmLinkInfo.iLinkType = ALARMLINKTYPE_FLASHING_WHITE;

	TLinkFlashingWhite_V3& tLightInfo = tAlarmLinkInfo.uLinkParam.tLinkFlashingWhite;
	tLightInfo.iEnable = m_cboLinkWhiteLightMode.GetItemData(m_cboLinkWhiteLightMode.GetCurSel());
	tLightInfo.iExternInfo = GetDlgItemInt(IDC_EDT_ALERT_ALINK_WHITE_LIGHT_INFO);

	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_LINK, m_iChannelNo, &tAlarmLinkInfo, tAlarmLinkInfo.iSize);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[AlarmLink 2](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkScene()
{
	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkAlertType()
{
	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkAlertLevel()
{
	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkLinkInfoType1()
{
	UI_Update_AlarmLinkInfo_Group_1();
}

void CLS_DlgUniqueAlertAlarmLink::OnCbnSelchangeCboAlertAlinkLinkInfoType2()
{
	UI_Update_AlarmLinkInfo_Group_2();
}

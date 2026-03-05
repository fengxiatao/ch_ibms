#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertEventTripwire.h"

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertEventTripwire, CDialog)

CLS_DlgUniqueAlertEventTripwire::CLS_DlgUniqueAlertEventTripwire(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertEventTripwire::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgUniqueAlertEventTripwire::~CLS_DlgUniqueAlertEventTripwire()
{
}

void CLS_DlgUniqueAlertEventTripwire::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALERT_TRIPWIRE_SCENE, m_cboAlertSceneNo);
	DDX_Control(pDX, IDC_CBO_ALERT_TRIPWIRE_TARGET_TYPE, m_cboTargetType);
	DDX_Control(pDX, IDC_CBO_ALERT_TRIPWIRE_COLOR, m_cboAreaColor);
	DDX_Control(pDX, IDC_CBO_ALERT_TRIPWIRE_ALARM_COLOR, m_cboAlarmAreaColor);
	DDX_Control(pDX, IDC_CBO_ALERT_TRIPWIRE_CROSS_TYPE, m_cboTripwireCrossType);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertEventTripwire, CDialog)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_TRIPWIRE_SCENE, &CLS_DlgUniqueAlertEventTripwire::OnCbnSelchangeCboAlertTripwireScene)
	ON_BN_CLICKED(IDC_BTN_ALERT_TRIPWIRE_EVENT_SET, &CLS_DlgUniqueAlertEventTripwire::OnBnClickedBtnAlertTripwireEventSet)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertEventTripwire::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	return TRUE;
}

void CLS_DlgUniqueAlertEventTripwire::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertEventTripwire::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

void CLS_DlgUniqueAlertEventTripwire::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertEventTripwire::UI_InitDlgItemText()
{
	int iCurSel = m_cboAlertSceneNo.GetCurSel();
	int iCurSelEx = 0;
	m_cboAlertSceneNo.ResetContent();
	for (int i = 0; i < MAX_UNIQUE_ALERT_SCENE_NUM; i++)
	{
		m_cboAlertSceneNo.SetItemData(m_cboAlertSceneNo.AddString(GetTextEx(IDS_ALERT_SCENE) + IntToCString(i + 1)), i);
	}
	m_cboAlertSceneNo.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	iCurSel = m_cboTargetType.GetCurSel();
	m_cboTargetType.ResetContent();
	m_cboTargetType.SetItemData(m_cboTargetType.AddString(GetTextEx(IDS_VCA_TAR_ALL)), 0);
	m_cboTargetType.SetItemData(m_cboTargetType.AddString(GetTextEx(IDS_VCA_TAR_PEOPLE)), 1);
	m_cboTargetType.SetItemData(m_cboTargetType.AddString(GetTextEx(IDS_VCA_TAR_CAR)), 2);
	m_cboTargetType.SetItemData(m_cboTargetType.AddString(GetTextEx(IDS_VCA_TAR_PEOPLE_CAR)), 3);
	m_cboTargetType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	iCurSel = m_cboTripwireCrossType.GetCurSel();
	m_cboTripwireCrossType.ResetContent();
	m_cboTripwireCrossType.SetItemData(m_cboTripwireCrossType.AddString(GetTextEx(IDS_TWO_WAY)), 1);
	m_cboTripwireCrossType.SetItemData(m_cboTripwireCrossType.AddString(GetTextEx(IDS_ONE_WAY)), 0);
	m_cboTripwireCrossType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	CStringArray cstrArrayColor;
	cstrArrayColor.SetSize(10);
	cstrArrayColor.InsertAt(1, GetTextEx(IDS_VCA_COL_RED));
	cstrArrayColor.InsertAt(2, GetTextEx(IDS_VCA_COL_GREEN));
	cstrArrayColor.InsertAt(3, GetTextEx(IDS_VCA_COL_YELLOW));
	cstrArrayColor.InsertAt(4, GetTextEx(IDS_VCA_COL_BLUE));
	cstrArrayColor.InsertAt(5, GetTextEx(IDS_VCA_COL_MAGENTA));
	cstrArrayColor.InsertAt(6, GetTextEx(IDS_VCA_COL_CYAN));
	cstrArrayColor.InsertAt(7, GetTextEx(IDS_VCA_COL_BLACK));
	cstrArrayColor.InsertAt(8, GetTextEx(IDS_VCA_COL_WHITE));
	cstrArrayColor.FreeExtra();

	iCurSel = m_cboAreaColor.GetCurSel();
	iCurSelEx = m_cboAlarmAreaColor.GetCurSel();
	m_cboAreaColor.ResetContent();
	m_cboAlarmAreaColor.ResetContent();
	for (int i = 0; i < cstrArrayColor.GetCount(); i++)
	{
		if (cstrArrayColor.GetAt(i).IsEmpty())
		{
			continue;
		}

		m_cboAreaColor.SetItemData(m_cboAreaColor.AddString(cstrArrayColor.GetAt(i)), i);
		m_cboAlarmAreaColor.SetItemData(m_cboAlarmAreaColor.AddString(cstrArrayColor.GetAt(i)), i);
	}
	m_cboAreaColor.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
	m_cboAlarmAreaColor.SetCurSel(((iCurSelEx < 0) ? 0 : iCurSelEx));

	SetDlgItemText(IDC_BTN_ALERT_TRIPWIRE_EVENT_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_RULE, GetTextByLan(_T("显示报警规则"), _T("Display rules")));
	SetDlgItemText(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_STAT, GetTextByLan(_T("显示报警计数"), _T("Display count")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_COLOR, GetTextByLan(_T("绊线颜色"), _T("Tripwires color")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_ALARM_COLOR, GetTextByLan(_T("报警绊线颜色"), _T("Alarm line color")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_TARGET_TYPE, GetTextByLan(_T("目标类型"), _T("Target type")));
	SetDlgItemText(IDC_CHK_ALERT_TRIPWIRE_VALID, GetTextByLan(_T("启用事件检测"), _T("Event detection")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_DIR_ANGLE, GetTextByLan(_T("禁止穿越方向"), _T("Ban through direction")));
	SetDlgItemText(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_TARGET, GetTextByLan(_T("显示目标"), _T("Display target")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_MIN_DISTANCE, GetTextByLan(_T("最小距离"), _T("The minimum distance")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_MIN_TIME, GetTextByLan(_T("最短时间"), _T("Shortest time")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_DIR_PROHIBITION, GetTextByLan(_T("穿越类型"), _T("Through type")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_TRACK_TIME, GetTextByLan(_T("跟踪时间"), _T("Track time")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_SCENE, GetTextByLan(_T("场景"), _T("SceneId")));
	SetDlgItemText(IDC_STC_ALERT_TRIPWIRE_SENSITIVITY, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));
}

void CLS_DlgUniqueAlertEventTripwire::UI_UpdateInterfaceParam()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertTripwire tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tInfo.iEventNo = 0;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_TRIPWIRE, m_iChannelNo, &tInfo, tInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_VALID)))->SetCheck(tInfo.iValid ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_RULE)))->SetCheck(tInfo.tDisplayInfo.iDisplayRule ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_STAT)))->SetCheck(tInfo.tDisplayInfo.iDisplayStat ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_TARGET)))->SetCheck(tInfo.iDisplayTarget ? BST_CHECKED : BST_UNCHECKED);
	m_cboTargetType.SetCurSel(GetCboSel(&m_cboTargetType, tInfo.iTargetTypeCheck));
	m_cboAreaColor.SetCurSel(GetCboSel(&m_cboAreaColor, tInfo.tDisplayInfo.iColor));
	m_cboAlarmAreaColor.SetCurSel(GetCboSel(&m_cboAlarmAreaColor, tInfo.tDisplayInfo.iAlarmColor));
	m_cboTripwireCrossType.SetCurSel(GetCboSel(&m_cboTripwireCrossType, tInfo.iTripwireType));
	SetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_DIR_ANGLE, tInfo.iTripwireDirection);
	SetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_SENSITIVITY, tInfo.iSensitivity);
	SetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_MIN_DISTANCE, tInfo.iMinDistance);
	SetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_MIN_TIME, tInfo.iMinTime);
	SetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_TRACK_TIME, tInfo.iTrackTime);

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[Tripwire](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[Tripwire](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}


void CLS_DlgUniqueAlertEventTripwire::OnCbnSelchangeCboAlertTripwireScene()
{
	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertEventTripwire::OnBnClickedBtnAlertTripwireEventSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertTripwire tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tInfo.iTargetTypeCheck = m_cboTargetType.GetItemData(m_cboTargetType.GetCurSel());
	tInfo.tDisplayInfo.iColor = m_cboAreaColor.GetItemData(m_cboAreaColor.GetCurSel());
	tInfo.tDisplayInfo.iAlarmColor = m_cboAlarmAreaColor.GetItemData(m_cboAlarmAreaColor.GetCurSel());
	tInfo.iTripwireType = m_cboTripwireCrossType.GetItemData(m_cboTripwireCrossType.GetCurSel());

	tInfo.iValid = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_VALID)))->GetCheck()) ? 1 : 0;
	tInfo.tDisplayInfo.iDisplayRule = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_RULE)))->GetCheck()) ? 1 : 0;
	tInfo.tDisplayInfo.iDisplayStat = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_STAT)))->GetCheck()) ? 1 : 0;
	tInfo.iDisplayTarget = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_TRIPWIRE_DISPLAY_TARGET)))->GetCheck()) ? 1 : 0;
	tInfo.iTripwireDirection = GetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_DIR_ANGLE);
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_SENSITIVITY);
	tInfo.iMinDistance = GetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_MIN_DISTANCE);
	tInfo.iMinTime = GetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_MIN_TIME);
	tInfo.iTrackTime = GetDlgItemInt(IDC_EDT_ALERT_TRIPWIRE_TRACK_TIME);

	tInfo.iEventNo = 0;
	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_TRIPWIRE, m_iChannelNo, &tInfo, tInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[Tripwire](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[Tripwire](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertEventPerimeter.h"

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertEventPerimeter, CDialog)

CLS_DlgUniqueAlertEventPerimeter::CLS_DlgUniqueAlertEventPerimeter(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertEventPerimeter::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgUniqueAlertEventPerimeter::~CLS_DlgUniqueAlertEventPerimeter()
{
}

void CLS_DlgUniqueAlertEventPerimeter::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALERT_PERIMETER_SCENE, m_cboAlertSceneNo);
	DDX_Control(pDX, IDC_CBO_ALERT_PERIMETER_TARGET_TYPE, m_cboTargetType);
	DDX_Control(pDX, IDC_CBO_ALERT_PERIMETER_COLOR, m_cboAreaColor);
	DDX_Control(pDX, IDC_CBO_ALERT_PERIMETER_ALARM_COLOR, m_cboAlarmAreaColor);
	DDX_Control(pDX, IDC_CBO_ALERT_PERIMETER_MODE, m_cboNoAlarmMode);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertEventPerimeter, CDialog)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_PERIMETER_SCENE, &CLS_DlgUniqueAlertEventPerimeter::OnCbnSelchangeCboAlertPerimeterScene)
	ON_BN_CLICKED(IDC_BTN_ALERT_PERIMETER_EVENT_SET, &CLS_DlgUniqueAlertEventPerimeter::OnBnClickedBtnAlertPerimeterEventSet)
	ON_BN_CLICKED(IDC_CHK_ALERT_PERIMETER_DISPLAY_ENTER, &CLS_DlgUniqueAlertEventPerimeter::OnBnClickedChkAlertPerimeterDisplayEnter)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertEventPerimeter::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	return TRUE;
}

void CLS_DlgUniqueAlertEventPerimeter::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertEventPerimeter::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

void CLS_DlgUniqueAlertEventPerimeter::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertEventPerimeter::UI_InitDlgItemText()
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

	m_cboNoAlarmMode.ResetContent();
	m_cboNoAlarmMode.SetItemData(0, m_cboNoAlarmMode.AddString(GetTextByLan(_T("不支持"), _T("UNsupport"))));
	m_cboNoAlarmMode.SetItemData(1, m_cboNoAlarmMode.AddString(GetTextByLan(_T("离开视频区域消警"), _T("leave Video area to clear Alarm"))));
	m_cboNoAlarmMode.SetItemData(2, m_cboNoAlarmMode.AddString(GetTextByLan(_T("离开检测区域消警"), _T("leave detect area to clear Alarm"))));
	m_cboNoAlarmMode.SetCurSel(1);

	SetDlgItemText(IDC_BTN_ALERT_PERIMETER_EVENT_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_CHK_ALERT_PERIMETER_DISPLAY_RULE, GetTextByLan(_T("显示报警规则"), _T("Display rules")));
	SetDlgItemText(IDC_CHK_ALERT_PERIMETER_DISPLAY_STAT, GetTextByLan(_T("显示报警计数"), _T("Display alarm count")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_COLOR, GetTextByLan(_T("区域颜色"), _T("Regional color")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_ALARM_COLOR, GetTextByLan(_T("报警区域颜色"), _T("Alarm area color")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_TARGET_TYPE, GetTextByLan(_T("目标类型"), _T("Target type")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_MIN_SIZE, GetTextByLan(_T("最小尺寸"), _T("Minimum size")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_MAX_SIZE, GetTextByLan(_T("最大尺寸"), _T("Largest size")));
	SetDlgItemText(IDC_CHK_ALERT_PERIMETER_VALID, GetTextByLan(_T("启用事件检测"), _T("Enable event detection")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_DIR_ANGLE, GetTextByLan(_T("禁止方向角度"), _T("Ban direction Angle")));
	SetDlgItemText(IDC_CHK_ALERT_PERIMETER_DISPLAY_TARGET, GetTextByLan(_T("显示目标"), _T("According to the target")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_MODE, GetTextByLan(_T("检测模式"), _T("Detect patterns")));
	SetDlgItemText(IDC_CHK_ALERT_PERIMETER_DISPLAY_ENTER, GetTextByLan(_T("入侵"), _T("Invasion")));
	SetDlgItemText(IDC_CHK_ALERT_PERIMETER_DISPLAY_LEAVE, GetTextByLan(_T("离开"), _T("Leave")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_MIN_DISTANCE, GetTextByLan(_T("最小距离"), _T("Minimum distance")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_MIN_TIME, GetTextByLan(_T("最短时间"), _T("Shortest time")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_DIR_PROHIBITION, GetTextByLan(_T("方向限制"), _T("Direction restriction")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_RESORT_TIME, GetTextByLan(_T("滞留时间"), _T("Residence time")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_TRACK_TIME, GetTextByLan(_T("跟踪时间"), _T("Track of time")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_SCENE, GetTextByLan(_T("场景"), _T("SceneId")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_SENSITIVITY, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));
	SetDlgItemText(IDC_STC_ALERT_PERIMETER_NOALARMMODE, GetTextByLan(_T("消警模式"), _T("NoAlarm Mode")));
}

void CLS_DlgUniqueAlertEventPerimeter::UI_UpdateInterfaceParam()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertPerimeter tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tInfo.iEventNo = 0;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_PERIMETER, m_iChannelNo, &tInfo, tInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_VALID)))->SetCheck(tInfo.iValid ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_RULE)))->SetCheck(tInfo.tDisplayInfo.iDisplayRule ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_STAT)))->SetCheck(tInfo.tDisplayInfo.iDisplayStat ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_TARGET)))->SetCheck(tInfo.iDisplayTarget ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_ENTER)))->SetCheck((tInfo.iCheckMode & 1) ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_LEAVE)))->SetCheck(((tInfo.iCheckMode >> 1) & 1) ? BST_CHECKED : BST_UNCHECKED);
	m_cboTargetType.SetCurSel(GetCboSel(&m_cboTargetType, tInfo.iTargetTypeCheck));
	m_cboAreaColor.SetCurSel(GetCboSel(&m_cboAreaColor, tInfo.tDisplayInfo.iColor));
	m_cboAlarmAreaColor.SetCurSel(GetCboSel(&m_cboAlarmAreaColor, tInfo.tDisplayInfo.iAlarmColor));
	m_cboNoAlarmMode.SetCurSel(GetCboSel(&m_cboNoAlarmMode, tInfo.iNoAlarmMode));
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_SENSITIVITY, tInfo.iSensitivity);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MIN_DISTANCE, tInfo.iMinDistance);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MIN_TIME, tInfo.iMinTime);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_DIR_PROHIBITION, tInfo.iDirectionCheck);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_DIR_ANGLE, tInfo.iDirectionAngle);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MIN_SIZE, tInfo.iMiniSize);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MAX_SIZE, tInfo.iMaxSize);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_RESORT_TIME, tInfo.iResortTime);
	SetDlgItemInt(IDC_EDT_ALERT_PERIMETER_TRACK_TIME, tInfo.iTrackTime);

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[Perimeter](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[Perimeter](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}


void CLS_DlgUniqueAlertEventPerimeter::OnCbnSelchangeCboAlertPerimeterScene()
{
	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertEventPerimeter::OnBnClickedBtnAlertPerimeterEventSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertPerimeter tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tInfo.iEventNo = 0;
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_SENSITIVITY);
	tInfo.iMinDistance = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MIN_DISTANCE);
	tInfo.iMinTime = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MIN_TIME);
	tInfo.iDirectionCheck = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_DIR_PROHIBITION);
	tInfo.iDirectionAngle = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_DIR_ANGLE);
	tInfo.iMiniSize = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MIN_SIZE);
	tInfo.iMaxSize = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_MAX_SIZE);
	tInfo.iResortTime = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_RESORT_TIME);
	tInfo.iTrackTime = GetDlgItemInt(IDC_EDT_ALERT_PERIMETER_TRACK_TIME);

	tInfo.iValid = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_VALID)))->GetCheck()) ? 1 : 0;
	tInfo.tDisplayInfo.iDisplayRule = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_RULE)))->GetCheck()) ? 1 : 0;
	tInfo.tDisplayInfo.iDisplayStat = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_STAT)))->GetCheck()) ? 1 : 0;
	tInfo.iDisplayTarget = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_TARGET)))->GetCheck()) ? 1 : 0;
	tInfo.iCheckMode |= (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_ENTER)))->GetCheck()) ? 1 : 0;
	tInfo.iCheckMode |= ((BST_CHECKED == ((((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_LEAVE)))->GetCheck()) ? 1 : 0)) << 1);

	tInfo.iTargetTypeCheck = m_cboTargetType.GetItemData(m_cboTargetType.GetCurSel());
	tInfo.tDisplayInfo.iColor = m_cboAreaColor.GetItemData(m_cboAreaColor.GetCurSel());
	tInfo.tDisplayInfo.iAlarmColor = m_cboAlarmAreaColor.GetItemData(m_cboAlarmAreaColor.GetCurSel());
	tInfo.iNoAlarmMode = m_cboNoAlarmMode.GetCurSel();

	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_PERIMETER, m_iChannelNo, &tInfo, tInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[Perimeter](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[Perimeter](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertEventPerimeter::OnBnClickedChkAlertPerimeterDisplayEnter()
{
	// TODO: Add control notification handler code here
	if (((CButton*)(GetDlgItem(IDC_CHK_ALERT_PERIMETER_DISPLAY_ENTER)))->GetCheck())
	{
		m_cboNoAlarmMode.EnableWindow(TRUE);
	}
	else
	{
		m_cboNoAlarmMode.EnableWindow(FALSE);
		UniqueAlertPerimeter tUpdataInfo = {0};
		tUpdataInfo.iSize = sizeof(tUpdataInfo);
		tUpdataInfo.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
		tUpdataInfo.iEventNo = 0;
		int iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_PERIMETER, m_iChannelNo, &tUpdataInfo, tUpdataInfo.iSize);
		if (iRet == RET_SUCCESS)
		{
			m_cboNoAlarmMode.SetCurSel(tUpdataInfo.iNoAlarmMode);
		}
	}
}

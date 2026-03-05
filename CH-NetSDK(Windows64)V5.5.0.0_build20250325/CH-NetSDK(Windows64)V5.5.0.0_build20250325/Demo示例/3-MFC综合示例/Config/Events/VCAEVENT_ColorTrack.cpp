#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_ColorTrack.h"


IMPLEMENT_DYNAMIC(CLS_VCAEVENT_ColorTrack, CDialog)

CLS_VCAEVENT_ColorTrack::CLS_VCAEVENT_ColorTrack(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_ColorTrack::IDD, pParent)
{
}

CLS_VCAEVENT_ColorTrack::~CLS_VCAEVENT_ColorTrack()
{
}

void CLS_VCAEVENT_ColorTrack::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_COLOR_TRACK_AREA_COLOR, m_cboAreaColor);
	DDX_Control(pDX, IDC_CBO_COLOR_TRACK_AREA_ALARM_COLOR, m_cboAlarmAreaColor);
	DDX_Control(pDX, IDC_CBO_COLOR_TRACK_TARGET_TYPE, m_cboTrackTargetColor);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_ColorTrack, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_COLOR_TRACK_EVENT_SET, &CLS_VCAEVENT_ColorTrack::OnBnClickedBtnColorTrackEventSet)
END_MESSAGE_MAP()


BOOL CLS_VCAEVENT_ColorTrack::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();
	UI_UpdateDialogText();
	UI_UpdateParam();

	return TRUE; 
}

void CLS_VCAEVENT_ColorTrack::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UI_UpdateDialogText();
		UI_UpdateParam();
	}
}

void CLS_VCAEVENT_ColorTrack::OnLanguageChanged()
{
	UI_UpdateDialogText();
	UI_UpdateParam();
}

void CLS_VCAEVENT_ColorTrack::UI_UpdateDialogText()
{
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

	int iCurSel = m_cboAreaColor.GetCurSel();
	int iCurSelEx = m_cboAlarmAreaColor.GetCurSel();
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

	iCurSel = m_cboTrackTargetColor.GetCurSel();
	m_cboTrackTargetColor.ResetContent();
	m_cboTrackTargetColor.SetItemData(m_cboTrackTargetColor.AddString(GetTextEx(IDS_NATION_OTHER)), 0);
	m_cboTrackTargetColor.SetItemData(m_cboTrackTargetColor.AddString(GetTextEx(IDS_VCA_COL_RED)), 1);
	m_cboTrackTargetColor.SetItemData(m_cboTrackTargetColor.AddString(GetTextEx(IDS_VCA_COL_GREEN)), 2);
	m_cboTrackTargetColor.SetItemData(m_cboTrackTargetColor.AddString(GetTextEx(IDS_VCA_COL_YELLOW)), 3);
	m_cboTrackTargetColor.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	SetDlgItemTextEx(IDC_CHK_COLOR_TRACK_VALID, IDS_VCAEVENT_EVENT_VALID);
	SetDlgItemTextEx(IDC_CHK_COLOR_TRACK_DISPLAY_RULE, IDS_VCAEVENT_SHOW_ALARM_RULE);
	SetDlgItemTextEx(IDC_CHK_COLOR_TRACK_DISPLAY_STAT, IDS_VCAEVENT_SHOW_ALARM_STATISTICS);
	SetDlgItemTextEx(IDC_CHK_COLOR_TRACK_DISPLAY_TARGET, IDS_VCAEVENT_SHOW_TARGET_BOX);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_AREA_COLOR, IDS_VCA_NOALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_AREA_ALARM_COLOR, IDS_VCA_ALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_TARGET_TYPE, IDS_VCA_TARGETCOLOR);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_MIN_SIZE, IDS_VCA_ABMINSIZE);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_MAX_SIZE, IDS_VCA_ABMAXSIZE);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_LIGHTNESS, IDS_PREVIEW_BRIGHTNESS);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_HUE, IDS_PREVIEW_HUE);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_SATURATION, IDS_PREVIEW_SATURATION);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_SEARCH_TIME, IDS_PRESET_SEARCH_TIME);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_SEARCH_LOOP, IDS_AUTO_SEARCH_LOOP);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_SENSITIVITY, IDS_CONFIG_ITS_ILLEGALPARK_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_CALIBRATION_SIZE, IDS_CALIBRATION_FRAME_SIZE);
	SetDlgItemTextEx(IDC_STC_COLOR_TRACK_ZOOM_RATE, IDS_TRACK_ZOOM_RATE);
	SetDlgItemTextEx(IDC_BTN_COLOR_TRACK_EVENT_SET, IDS_SET);
}

void CLS_VCAEVENT_ColorTrack::UI_UpdateParam()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	VCAColorTrack tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.tRule.iRuleID = m_iRuleID;
	tInfo.tRule.iSceneID = m_iSceneID;
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_COLOR_TRACK, m_iChannelNO, &tInfo, tInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		memset(&tInfo, 0, sizeof(tInfo));
	}

	((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_VALID)))->SetCheck(tInfo.tRule.iValid ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_DISPLAY_RULE)))->SetCheck(tInfo.tDisplayParam.iDisplayRule ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_DISPLAY_STAT)))->SetCheck(tInfo.tDisplayParam.iDisplayStat ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_DISPLAY_TARGET)))->SetCheck(tInfo.iDisplayTarget ? BST_CHECKED : BST_UNCHECKED);
	m_cboTrackTargetColor.SetCurSel(GetCboSel(&m_cboTrackTargetColor, tInfo.iTargetColor));
	m_cboAreaColor.SetCurSel(GetCboSel(&m_cboAreaColor, tInfo.tDisplayParam.iColor));
	m_cboAlarmAreaColor.SetCurSel(GetCboSel(&m_cboAlarmAreaColor, tInfo.tDisplayParam.iAlarmColor));
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_MIN_SIZE, tInfo.iMinSize);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_MAX_SIZE, tInfo.iMaxSize);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_SEARCH_TIME, tInfo.iSearchTime);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_SEARCH_LOOP, tInfo.iSearchLoop);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_ZOOM_RATE, tInfo.iZoomRate);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_SENSITIVITY, tInfo.iSensitiv);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_CALIBRATION_SIZE, tInfo.iCalibrationSize);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_HUE, tInfo.iHue);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_SATURATION, tInfo.iSaturation);
	SetDlgItemInt(IDC_EDT_COLOR_TRACK_LIGHTNESS, tInfo.iLightness);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","VCAGetConfig[ColorTrack](%d,%d) Err = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","VCAGetConfig[ColorTrack](%d,%d)", m_iLogonID, m_iChannelNO);
	}
	return;
}


void CLS_VCAEVENT_ColorTrack::OnBnClickedBtnColorTrackEventSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	VCAColorTrack tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.tRule.iRuleID = m_iRuleID;
	tInfo.tRule.iSceneID = m_iSceneID;
	
	tInfo.tRule.iValid = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_VALID)))->GetCheck()) ? 1 : 0;
	tInfo.tDisplayParam.iDisplayRule = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_DISPLAY_RULE)))->GetCheck()) ? 1 : 0;
	tInfo.tDisplayParam.iDisplayStat = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_DISPLAY_STAT)))->GetCheck()) ? 1 : 0;
	tInfo.iDisplayTarget = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_COLOR_TRACK_DISPLAY_TARGET)))->GetCheck()) ? 1 : 0;
	tInfo.iTargetColor = m_cboTrackTargetColor.GetItemData(m_cboTrackTargetColor.GetCurSel());
	tInfo.tDisplayParam.iColor = m_cboAreaColor.GetItemData(m_cboAreaColor.GetCurSel());
	tInfo.tDisplayParam.iAlarmColor = m_cboAlarmAreaColor.GetItemData(m_cboAlarmAreaColor.GetCurSel());

	tInfo.iMinSize = GetDlgItemInt(IDC_EDT_COLOR_TRACK_MIN_SIZE);
	tInfo.iMaxSize = GetDlgItemInt(IDC_EDT_COLOR_TRACK_MAX_SIZE);
	tInfo.iSearchTime = GetDlgItemInt(IDC_EDT_COLOR_TRACK_SEARCH_TIME);
	tInfo.iSearchLoop = GetDlgItemInt(IDC_EDT_COLOR_TRACK_SEARCH_LOOP);
	tInfo.iZoomRate = GetDlgItemInt(IDC_EDT_COLOR_TRACK_ZOOM_RATE);
	tInfo.iSensitiv = GetDlgItemInt(IDC_EDT_COLOR_TRACK_SENSITIVITY);
	tInfo.iCalibrationSize = GetDlgItemInt(IDC_EDT_COLOR_TRACK_CALIBRATION_SIZE);
	tInfo.iHue = GetDlgItemInt(IDC_EDT_COLOR_TRACK_HUE);
	tInfo.iSaturation = GetDlgItemInt(IDC_EDT_COLOR_TRACK_SATURATION);
	tInfo.iLightness = GetDlgItemInt(IDC_EDT_COLOR_TRACK_LIGHTNESS);

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_COLOR_TRACK, m_iChannelNO, &tInfo, tInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","VCASetConfig[ColorTrack](%d,%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","VCASetConfig[ColorTrack](%d,%d)", m_iLogonID, m_iChannelNO);
	}
	return;
}

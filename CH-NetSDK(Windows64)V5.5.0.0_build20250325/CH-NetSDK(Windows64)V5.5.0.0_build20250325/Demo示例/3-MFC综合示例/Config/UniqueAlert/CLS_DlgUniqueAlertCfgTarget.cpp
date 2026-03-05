#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertCfgTarget.h"

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertTarget, CDialog)

CLS_DlgUniqueAlertTarget::CLS_DlgUniqueAlertTarget(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertTarget::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgUniqueAlertTarget::~CLS_DlgUniqueAlertTarget()
{
}

void CLS_DlgUniqueAlertTarget::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALERT_TARGET_SCENE, m_cboAlertScene);
	DDX_Control(pDX, IDC_CBO_ALERT_TARGET_COLOR, m_cboTargetColor);
	DDX_Control(pDX, IDC_CBO_ALERT_ALARM_COLOR, m_cboAlarmTargetColor);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertTarget, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_ALERT_TARGET_SET, &CLS_DlgUniqueAlertTarget::OnBnClickedBtnAlertTargetSet)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_TARGET_SCENE, &CLS_DlgUniqueAlertTarget::OnCbnSelchangeCboAlertTargetScene)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertTarget::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	return TRUE;
}

void CLS_DlgUniqueAlertTarget::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertTarget::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

void CLS_DlgUniqueAlertTarget::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertTarget::UI_InitDlgItemText()
{
	//Alert scene
	int iCurSel = m_cboAlertScene.GetCurSel();
	m_cboAlertScene.ResetContent();
	for (int i = 0; i < MAX_UNIQUE_ALERT_SCENE_NUM; i++)
	{
		m_cboAlertScene.SetItemData(m_cboAlertScene.AddString(GetTextEx(IDS_ALERT_SCENE) + IntToCString(i + 1)), i);
	}
	m_cboAlertScene.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	CStringArray cstrArrayColor;
	cstrArrayColor.SetSize(10);
	cstrArrayColor.InsertAt(0, GetTextByLan(_T("自动调整"), _T("Auto")));
	cstrArrayColor.InsertAt(1, GetTextEx(IDS_VCA_COL_RED));
	cstrArrayColor.InsertAt(2, GetTextEx(IDS_VCA_COL_GREEN));
	cstrArrayColor.InsertAt(3, GetTextEx(IDS_VCA_COL_YELLOW));
	cstrArrayColor.InsertAt(4, GetTextEx(IDS_VCA_COL_BLUE));
	cstrArrayColor.InsertAt(5, GetTextEx(IDS_VCA_COL_MAGENTA));
	cstrArrayColor.InsertAt(6, GetTextEx(IDS_VCA_COL_CYAN));
	cstrArrayColor.InsertAt(7, GetTextEx(IDS_VCA_COL_BLACK));
	cstrArrayColor.InsertAt(8, GetTextEx(IDS_VCA_COL_WHITE));
	cstrArrayColor.FreeExtra();

	iCurSel = m_cboTargetColor.GetCurSel();
	int iCurSelEx = m_cboAlarmTargetColor.GetCurSel();
	m_cboTargetColor.ResetContent();
	m_cboAlarmTargetColor.ResetContent();
	for (int i = 0; i < cstrArrayColor.GetCount(); i++)
	{
		if (cstrArrayColor.GetAt(i).IsEmpty())
		{
			continue;
		}

		m_cboTargetColor.SetItemData(m_cboTargetColor.AddString(cstrArrayColor.GetAt(i)), i);
		if (0 != i)	//Alarm color does not support automatic
		{
			m_cboAlarmTargetColor.SetItemData(m_cboAlarmTargetColor.AddString(cstrArrayColor.GetAt(i)), i);
		}
	}
	m_cboTargetColor.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
	m_cboAlarmTargetColor.SetCurSel(((iCurSelEx < 0) ? 0 : iCurSelEx));

	SetDlgItemText(IDC_STATIC_ALERT_TARGET_SCENE, GetTextByLan(_T("场景"), _T("SceneId")));
	SetDlgItemText(IDC_GPO_ALERT_CFG_TARGET, GetTextByLan(_T("叠加参数和颜色"), _T("Stacking parameters and color")));
	SetDlgItemText(IDC_CHECK_ALERT_DISPLAY_TARGET, GetTextByLan(_T("显示目标"), _T("Display target")));
	SetDlgItemText(IDC_CHECK_ALERT_DISPLAY_TRACE, GetTextByLan(_T("显示轨迹"), _T("DisPlay trace")));
	SetDlgItemText(IDC_STATIC_ALERT_TARGET_COLOR, GetTextByLan(_T("目标颜色"), _T("Target color")));
	SetDlgItemText(IDC_STATIC_ALERT_ALARM_COLOR, GetTextByLan(_T("报警颜色"), _T("Alarm target color")));
	SetDlgItemText(IDC_STATIC_ALERT_TRACE_LEN, GetTextByLan(_T("轨迹长度"), _T("Trace length")));
	SetDlgItemText(IDC_BTN_ALERT_TARGET_SET, GetTextByLan(_T("设置"), _T("Set")));
}

void CLS_DlgUniqueAlertTarget::UI_UpdateInterfaceParam()
{
	UI_UpdateInfoAlertTargetInfo();
}

void CLS_DlgUniqueAlertTarget::UI_UpdateInfoAlertTargetInfo()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	VCATargetParam tTarget = {0};
	tTarget.iBufSize = sizeof(tTarget);
	tTarget.iSceneID = m_cboAlertScene.GetItemData(m_cboAlertScene.GetCurSel());
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_CFG_TARGET, m_iChannelNo, &tTarget, tTarget.iBufSize);
	if (iRet < RET_SUCCESS)
	{
		memset(&tTarget, 0, sizeof(tTarget));
	}

	((CButton*)(GetDlgItem(IDC_CHECK_ALERT_DISPLAY_TARGET)))->SetCheck(tTarget.iDisplayTarget ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHECK_ALERT_DISPLAY_TRACE)))->SetCheck(tTarget.iDisplayTrace ? BST_CHECKED : BST_UNCHECKED);
	m_cboTargetColor.SetCurSel(GetCboSel(&m_cboTargetColor, tTarget.iTargetColor));
	m_cboAlarmTargetColor.SetCurSel(GetCboSel(&m_cboAlarmTargetColor, tTarget.iTargetAlarmColor));
	SetDlgItemInt(IDC_EDT_ALERT_TRACE_LEN, tTarget.iTraceLength);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[Target](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[Target](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}


void CLS_DlgUniqueAlertTarget::OnBnClickedBtnAlertTargetSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	VCATargetParam tTarget = {0};
	tTarget.iBufSize = sizeof(tTarget);
	tTarget.iSceneID = m_cboAlertScene.GetItemData(m_cboAlertScene.GetCurSel());
	tTarget.iDisplayTarget = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHECK_ALERT_DISPLAY_TARGET)))->GetCheck());
	tTarget.iDisplayTrace = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHECK_ALERT_DISPLAY_TRACE)))->GetCheck());
	tTarget.iTargetColor = m_cboTargetColor.GetItemData(m_cboTargetColor.GetCurSel());
	tTarget.iTargetAlarmColor = m_cboAlarmTargetColor.GetItemData(m_cboAlarmTargetColor.GetCurSel());
	tTarget.iTraceLength = GetDlgItemInt(IDC_EDT_ALERT_TRACE_LEN);

	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_CFG_TARGET, m_iChannelNo, &tTarget, tTarget.iBufSize);
	if (iRet < RET_SUCCESS)
	{
		memset(&tTarget, 0, sizeof(tTarget));
	}

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[Target](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[Target](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertTarget::OnCbnSelchangeCboAlertTargetScene()
{
	UI_UpdateInterfaceParam();
}

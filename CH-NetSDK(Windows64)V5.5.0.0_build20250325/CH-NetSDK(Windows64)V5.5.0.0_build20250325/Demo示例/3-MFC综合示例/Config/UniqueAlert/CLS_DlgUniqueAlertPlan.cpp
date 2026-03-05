#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertPlan.h"

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertPlan, CDialog)

CLS_DlgUniqueAlertPlan::CLS_DlgUniqueAlertPlan(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertPlan::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgUniqueAlertPlan::~CLS_DlgUniqueAlertPlan()
{
}

void CLS_DlgUniqueAlertPlan::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALERT_PLAN_TYPE, m_cboAlertType);
	DDX_Control(pDX, IDC_CBO_ALERT_PLAN_SCENE_NUM, m_cboAlertSceneNo);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_0, m_chkAlertPlan[0]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_1, m_chkAlertPlan[1]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_2, m_chkAlertPlan[2]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_3, m_chkAlertPlan[3]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_4, m_chkAlertPlan[4]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_5, m_chkAlertPlan[5]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_6, m_chkAlertPlan[6]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_7, m_chkAlertPlan[7]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_8, m_chkAlertPlan[8]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_9, m_chkAlertPlan[9]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_10, m_chkAlertPlan[10]);
	DDX_Control(pDX, IDC_RADIO_ALERT_PLAN_11, m_chkAlertPlan[11]);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertPlan, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_ALERT_PLAN_SET, &CLS_DlgUniqueAlertPlan::OnBnClickedBtnAlertPlanSet)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_PLAN_TYPE, &CLS_DlgUniqueAlertPlan::OnCbnSelchangeCboAlertPlanType)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_PLAN_SCENE_NUM, &CLS_DlgUniqueAlertPlan::OnCbnSelchangeCboAlertPlanSceneNum)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertPlan::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	return TRUE;
}

void CLS_DlgUniqueAlertPlan::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertPlan::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

void CLS_DlgUniqueAlertPlan::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertPlan::UI_InitDlgItemText()
{
	//TODO
	int iCurSel = m_cboAlertSceneNo.GetCurSel();
	m_cboAlertSceneNo.ResetContent();
	for (int i = 0; i < MAX_UNIQUE_ALERT_SCENE_NUM; i++)
	{
		m_cboAlertSceneNo.SetItemData(m_cboAlertSceneNo.AddString(GetTextEx(IDS_ALERT_SCENE) + IntToCString(i + 1)), i);
	}
	m_cboAlertSceneNo.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	iCurSel = m_cboAlertType.GetCurSel();
	m_cboAlertType.ResetContent();
	m_cboAlertType.SetItemData(m_cboAlertType.AddString(GetTextByLan(_T("周界警戒"), _T("Alert Perimeter"))), UNIQUE_ALERT_TYPE_PERIMETER);
	m_cboAlertType.SetItemData(m_cboAlertType.AddString(GetTextByLan(_T("绊线警戒"), _T("Alert Tripwire"))), UNIQUE_ALERT_TYPE_TRIPWIRE);
    m_cboAlertType.SetItemData(m_cboAlertType.AddString(GetTextByLan(_T("翻墙警戒"), _T("Alert ClimbWall"))), UNIQUE_ALERT_TYPE_CLIMBWALL);
	m_cboAlertType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	SetDlgItemText(IDC_STC_ALERT_PLAN_SCENE, GetTextByLan(_T("场景"), _T("SceneId")));
	SetDlgItemText(IDC_STC_ALERT_PLAN_TYPE, GetTextByLan(_T("警戒类型"), _T("Alert Type")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_0, GetTextByLan(_T("自定义事件参数"), _T("Custom event arguments")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_1, GetTextByLan(_T("抓拍并联动白光"), _T("Snapshot and link with white light")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_2, GetTextByLan(_T("抓拍并联动白光、单次警音"), _T("Snapshot and link with white light and single alert sound")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_3, GetTextByLan(_T("抓拍并联动激光、白光、循环警音"), _T("Snapshot and link with laser, white light, and recycling alert sound")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_4, GetTextByLan(_T("抓拍并联动炸裂式多级声光"), _T("Snapshot and link with bursting multi-stage sound and light")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_5, GetTextByLan(_T("抓拍并联动渐变式多级声光"), _T("Snapshot and link with gradient multi-stage sound and light")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_6, GetTextByLan(_T("抓拍并联动白光频闪、循环警音"), _T("Capture and link with white strobe and circular alert")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_7, GetTextByLan(_T("抓拍并联动警灯"), _T("Capture and link the alarm light")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_8, GetTextByLan(_T("抓拍并联动警灯、单次警音"), _T("Capture and link the alarm light and single alarm tone")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_9, GetTextByLan(_T("抓拍并联动警灯、循环警音"), _T("Capture and link the alarm light and cycle the alarm sound")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_10, GetTextByLan(_T("抓拍并联动炸裂式多级声光(警灯)"), _T("Multi level sound and light (alarm light) with snapshot and linkage")));
	SetDlgItemText(IDC_RADIO_ALERT_PLAN_11, GetTextByLan(_T("抓拍并联动渐变式多级声光(警灯)"), _T("Capture and linkage gradual change multi-level acoustooptic (warning light)")));
	SetDlgItemText(IDC_BTN_ALERT_PLAN_SET, GetTextByLan(_T("设置"), _T("Set")));
}

void CLS_DlgUniqueAlertPlan::UI_UpdateInterfaceParam()
{
	UI_UpdateInfoAlertLinkMode();
	UI_UpdateInfoAlertSupportPlan();
}

void CLS_DlgUniqueAlertPlan::UI_UpdateInfoAlertLinkMode()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertLinkMode tLinkMode = {0};
	tLinkMode.iSize = sizeof(tLinkMode);
	tLinkMode.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tLinkMode.iAlertType = m_cboAlertType.GetItemData(m_cboAlertType.GetCurSel());
	tLinkMode.iEventNo = 0;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_LINK_MODE, m_iChannelNo, &tLinkMode, tLinkMode.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	for (int i = 0; i < INNER_MAX_UNIQUE_ALERT_PLAN_NUM; i++)
	{
		int iCheck = BST_UNCHECKED;
		if (i == tLinkMode.iLinkMode)
		{
			iCheck = BST_CHECKED;
		}
		m_chkAlertPlan[i].SetCheck(iCheck);
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[EventLinkMode](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[EventLinkMode](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertPlan::UI_UpdateInfoAlertSupportPlan()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertTemplate tPlanList = {0};
	tPlanList.iSize = sizeof(tPlanList);
	tPlanList.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tPlanList.iAlertType = m_cboAlertType.GetItemData(m_cboAlertType.GetCurSel());
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_TEMPLATE, m_iChannelNo, &tPlanList, tPlanList.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	for (int i = 0; i < INNER_MAX_UNIQUE_ALERT_PLAN_NUM; i++)
	{
		BOOL blEnableWindow = FALSE;
		for (int j = 0; j < MAX_UNIQUE_ALERT_SUPPORT_NAME_NUM; j++)
		{
			if (i == tPlanList.iSupprotNames[j])
			{
				blEnableWindow = TRUE;
			}
		}
		m_chkAlertPlan[i].EnableWindow(blEnableWindow);
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[AlertPlan](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[AlertPlan](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertPlan::OnCbnSelchangeCboAlertPlanType()
{
	UI_UpdateInfoAlertSupportPlan();
	UI_UpdateInfoAlertLinkMode();
}

void CLS_DlgUniqueAlertPlan::OnCbnSelchangeCboAlertPlanSceneNum()
{
	UI_UpdateInfoAlertSupportPlan();
	UI_UpdateInfoAlertLinkMode();
}

void CLS_DlgUniqueAlertPlan::OnBnClickedBtnAlertPlanSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertLinkMode tLinkMode = {0};
	tLinkMode.iSize = sizeof(tLinkMode);
	tLinkMode.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tLinkMode.iAlertType = m_cboAlertType.GetItemData(m_cboAlertType.GetCurSel());
	tLinkMode.iEventNo = 0;
	for (int i = 0; i < INNER_MAX_UNIQUE_ALERT_PLAN_NUM; i++)
	{
		if (BST_UNCHECKED == m_chkAlertPlan[i].GetCheck())
		{
			continue;
		}
		tLinkMode.iLinkMode = i;
	}

	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_LINK_MODE, m_iChannelNo, &tLinkMode, tLinkMode.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[EventLinkMode](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[EventLinkMode](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

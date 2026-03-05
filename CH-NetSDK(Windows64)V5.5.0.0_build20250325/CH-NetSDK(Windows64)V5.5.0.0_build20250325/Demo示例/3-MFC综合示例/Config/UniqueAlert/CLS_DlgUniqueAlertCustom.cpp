#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertCustom.h"

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertCustom, CDialog)

CLS_DlgUniqueAlertCustom::CLS_DlgUniqueAlertCustom(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertCustom::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgUniqueAlertCustom::~CLS_DlgUniqueAlertCustom()
{
}

void CLS_DlgUniqueAlertCustom::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_CUSTOM_ALERT_SCENE, m_cboAlertSceneNo);
	DDX_Control(pDX, IDC_CBO_SCHEDULE_ALERT_TYPE, m_cboEventType);
	DDX_Control(pDX, IDC_CBO_ALERT_CHN_TYPE, m_cboChnAnalyzeType);
	DDX_Control(pDX, IDC_CHK_ALERT_WEEEK_DAY_0, m_chkScheduleWeekDay[0]);
	DDX_Control(pDX, IDC_CHK_ALERT_WEEEK_DAY_1, m_chkScheduleWeekDay[1]);
	DDX_Control(pDX, IDC_CHK_ALERT_WEEEK_DAY_2, m_chkScheduleWeekDay[2]);
	DDX_Control(pDX, IDC_CHK_ALERT_WEEEK_DAY_3, m_chkScheduleWeekDay[3]);
	DDX_Control(pDX, IDC_CHK_ALERT_WEEEK_DAY_4, m_chkScheduleWeekDay[4]);
	DDX_Control(pDX, IDC_CHK_ALERT_WEEEK_DAY_5, m_chkScheduleWeekDay[5]);
	DDX_Control(pDX, IDC_CHK_ALERT_WEEEK_DAY_6, m_chkScheduleWeekDay[6]);
	DDX_Control(pDX, IDC_CHK_WEEN_DAY_SEL_ALL, m_cbkSelAll);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertCustom, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_ALERT_ALARM_SCHEDULE_SET, &CLS_DlgUniqueAlertCustom::OnBnClickedBtnAlertAlarmScheduleSet)
	ON_CONTROL_RANGE(BN_CLICKED, IDC_CHK_ALERT_WEEEK_DAY_0, IDC_CHK_ALERT_WEEEK_DAY_6, &CLS_DlgUniqueAlertCustom::OnBnClickedChkWeekDay)
	ON_BN_CLICKED(IDC_CHK_WEEN_DAY_SEL_ALL, &CLS_DlgUniqueAlertCustom::OnBnClickedChkWeenDaySelAll)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_CHN_TYPE, &CLS_DlgUniqueAlertCustom::OnCbnSelchangeCboAlertChnType)
	ON_CBN_SELCHANGE(IDC_CBO_CUSTOM_ALERT_SCENE, &CLS_DlgUniqueAlertCustom::OnCbnSelchangeCboCustomAlertScene)
	ON_CBN_SELCHANGE(IDC_CBO_SCHEDULE_ALERT_TYPE, &CLS_DlgUniqueAlertCustom::OnCbnSelchangeCboScheduleAlertType)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertCustom::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	return TRUE;
}

void CLS_DlgUniqueAlertCustom::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertCustom::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

void CLS_DlgUniqueAlertCustom::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertCustom::UI_InitDlgItemText()
{
	int iCurSel = m_cboAlertSceneNo.GetCurSel();
	m_cboAlertSceneNo.ResetContent();
	for (int i = 0; i < MAX_UNIQUE_ALERT_SCENE_NUM; i++)
	{
		m_cboAlertSceneNo.SetItemData(m_cboAlertSceneNo.AddString(GetTextEx(IDS_ALERT_SCENE) + IntToCString(i + 1)), i);
	}
	m_cboAlertSceneNo.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	iCurSel = m_cboEventType.GetCurSel();
	m_cboEventType.ResetContent();
	m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan(_T("周界警戒"), _T("Alert Perimeter"))), UNIQUE_ALERT_TYPE_PERIMETER);
	m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan(_T("绊线警戒"), _T("Alert Tripwire"))), UNIQUE_ALERT_TYPE_TRIPWIRE);
    m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan(_T("翻墙警戒"), _T("Alert ClimbWall"))), UNIQUE_ALERT_TYPE_CLIMBWALL);

    m_cboEventType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	iCurSel = m_cboChnAnalyzeType.GetCurSel();
	m_cboChnAnalyzeType.ResetContent();
	m_cboChnAnalyzeType.SetItemData(m_cboChnAnalyzeType.AddString(GetTextByLan(_T("本地智能警戒"), _T("The local intelligent alert"))), 1);
	m_cboChnAnalyzeType.SetItemData(m_cboChnAnalyzeType.AddString(GetTextByLan(_T("前端智能警戒"), _T("The front-end smart alert"))), 0);
	m_cboChnAnalyzeType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	SetDlgItemText(IDC_GPO_ALERT_ALARM_SCHEDULE, GetTextByLan(_T("报警布防模板"), _T("Alert protection template")));
	SetDlgItemText(IDC_STC_ANALYZE_CHN_TYPE, GetTextByLan(_T("分析类型"), _T("Analysis type")));
	SetDlgItemText(IDC_STC_CUSTOM_ALERT_SCENE, GetTextByLan(_T("场景"), _T("SceneId")));
	SetDlgItemText(IDC_STC_SCHEDULE_ALERT_TYPE, GetTextByLan(_T("事件类型"), _T("Event type")));
	SetDlgItemText(IDC_STC_SCHEDULE_WEEK_DAY, GetTextByLan(_T("星期设置"), _T("Week set")));
	SetDlgItemText(IDC_CHK_WEEN_DAY_SEL_ALL, GetTextByLan(_T("全选"), _T("Future generations")));
	SetDlgItemText(IDC_CHK_ALERT_WEEEK_DAY_0, GetTextByLan(_T("星期日"), _T("Sunday")));
	SetDlgItemText(IDC_CHK_ALERT_WEEEK_DAY_1, GetTextByLan(_T("星期一"), _T("Monday")));
	SetDlgItemText(IDC_CHK_ALERT_WEEEK_DAY_2, GetTextByLan(_T("星期二"), _T("Tuesday")));
	SetDlgItemText(IDC_CHK_ALERT_WEEEK_DAY_3, GetTextByLan(_T("星期三"), _T("Wednesday")));
	SetDlgItemText(IDC_CHK_ALERT_WEEEK_DAY_4, GetTextByLan(_T("星期四"), _T("Thursday")));
	SetDlgItemText(IDC_CHK_ALERT_WEEEK_DAY_5, GetTextByLan(_T("星期五"), _T("Friday")));
	SetDlgItemText(IDC_CHK_ALERT_WEEEK_DAY_6, GetTextByLan(_T("星期六"), _T("Saturday")));
	SetDlgItemText(IDC_BTN_ALERT_ALARM_SCHEDULE_SET, GetTextByLan(_T("设置"), _T("Set")));
}

void CLS_DlgUniqueAlertCustom::UI_UpdateInterfaceParam()
{
	UI_UpdateInfoAlarmSchedule();
}

void CLS_DlgUniqueAlertCustom::UI_UpdateInfoAlarmSchedule()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertAlarmSchedule tSchedule = {0};
	tSchedule.iSize = sizeof(tSchedule);
	tSchedule.iEnChnType = m_cboChnAnalyzeType.GetItemData(m_cboChnAnalyzeType.GetCurSel());
	tSchedule.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tSchedule.iAlertType = m_cboEventType.GetItemData(m_cboEventType.GetCurSel());
	tSchedule.iEventNo = 0;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_SCHEDULE, m_iChannelNo, &tSchedule, tSchedule.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	for (int i = 0; i < MAX_DAYS; i++)
	{
		m_chkScheduleWeekDay[i].SetCheck(tSchedule.iWeekEnabel[i] ? BST_CHECKED : BST_UNCHECKED);
	}
	OnBnClickedChkWeekDay(m_chkScheduleWeekDay[0].GetDlgCtrlID());

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[Schedule](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[Schedule](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertCustom::OnBnClickedBtnAlertAlarmScheduleSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertAlarmSchedule tSchedule = {0};
	tSchedule.iSize = sizeof(tSchedule);
	tSchedule.iEnChnType = m_cboChnAnalyzeType.GetItemData(m_cboChnAnalyzeType.GetCurSel());
	tSchedule.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tSchedule.iAlertType = m_cboEventType.GetItemData(m_cboEventType.GetCurSel());
	tSchedule.iEventNo = 0;
	for (int i = 0; i < MAX_DAYS; i++)
	{
		tSchedule.iWeekEnabel[i] = (BST_CHECKED == m_chkScheduleWeekDay[i].GetCheck());
	}
	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_SCHEDULE, m_iChannelNo, &tSchedule, tSchedule.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}


	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[Schedule](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[Schedule](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertCustom::OnBnClickedChkWeekDay(unsigned int _uiWigetId)
{
	BOOL blCheckAll = TRUE;
	for (int i = 0; i < MAX_DAYS; i++)
	{
		if (BST_UNCHECKED == m_chkScheduleWeekDay[i].GetCheck())
		{
			blCheckAll = FALSE;
			break;
		}
	}

	m_cbkSelAll.SetCheck(blCheckAll ? BST_CHECKED : BST_UNCHECKED);
}

void CLS_DlgUniqueAlertCustom::OnBnClickedChkWeenDaySelAll()
{
	int iChk = m_cbkSelAll.GetCheck();
	for (int i = 0; i < MAX_DAYS; i++)
	{
		m_chkScheduleWeekDay[i].SetCheck(iChk);
	}
}

void CLS_DlgUniqueAlertCustom::OnCbnSelchangeCboAlertChnType()
{
	UI_UpdateInfoAlarmSchedule();
}

void CLS_DlgUniqueAlertCustom::OnCbnSelchangeCboCustomAlertScene()
{
	UI_UpdateInfoAlarmSchedule();
}

void CLS_DlgUniqueAlertCustom::OnCbnSelchangeCboScheduleAlertType()
{
	UI_UpdateInfoAlarmSchedule();
}

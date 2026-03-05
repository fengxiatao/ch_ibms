// VCAAlarmSchedulePage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAAlarmSchedulePage.h"


// CLS_VCAAlarmSchedulePage dialog

#define VCA_MAX_RULE_NUM_NEW			10			//After adding audio and video detection, there are 10 rule IDs in total

IMPLEMENT_DYNAMIC(CLS_VCAAlarmSchedulePage, CDialog)
static vca_TVCAParam g_VcaParam = {0};
extern int g_iEventIDS[VCA_EVENT_MAX];
static int g_iEnableIDS[] = {IDS_CONFIG_VCA_DISABLE, IDS_CONFIG_VCA_ENABLE};
static int g_iWeek[] = {IDS_CONFIG_SUNDAY, IDS_CONFIG_MONDAY, IDS_CONFIG_TUESDAY, IDS_CONFIG_WEDNESDAY, IDS_CONFIG_THURSDAY, IDS_CONFIG_FRIDAY ,IDS_CONFIG_SATURDAY};
static int iVcaType = ALARM_TYPE_VCA;

CLS_VCAAlarmSchedulePage::CLS_VCAAlarmSchedulePage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCAAlarmSchedulePage::IDD, pParent)
{
	memset(m_chkEnbale, 0 , sizeof(m_chkEnbale));
	m_iLogonID = -1;
	m_iChannelNo = -1;

}

CLS_VCAAlarmSchedulePage::~CLS_VCAAlarmSchedulePage()
{
}

void CLS_VCAAlarmSchedulePage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_ALARM_SCHEDULE_RULE, m_cboRuleID);
	DDX_Control(pDX, IDC_COMBO_ALARM_SCHEDULE_EVENT, m_cboEvent);
	DDX_Control(pDX, IDC_COMBO_ALARM_SCHEDULE_ENABLE, m_cboEnbale);
	DDX_Control(pDX, IDC_COMBO_ALARM_SCHEDULE_WEEK, m_cboWeek);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME1, m_dtBeginTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME2, m_dtBeginTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME3, m_dtBeginTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME4, m_dtBeginTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME1, m_dtEndTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME2, m_dtEndTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME3, m_dtEndTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME4, m_dtEndTime[3]);
	DDX_Check(pDX, IDC_CHECK_TIME1, m_chkEnbale[0]);
	DDX_Check(pDX, IDC_CHECK_TIME2, m_chkEnbale[1]);
	DDX_Check(pDX, IDC_CHECK_TIME3, m_chkEnbale[2]);
	DDX_Check(pDX, IDC_CHECK_TIME4, m_chkEnbale[3]);
	DDX_Control(pDX, IDC_COMBO_SCHEDULE_SCENE, m_CboSceneId);
	DDX_Control(pDX, IDC_CHECK_TIME5, m_chkExtraSegmentEnable[0]);
	DDX_Control(pDX, IDC_CHECK_TIME6, m_chkExtraSegmentEnable[1]);
	DDX_Control(pDX, IDC_CHECK_TIME7, m_chkExtraSegmentEnable[2]);
	DDX_Control(pDX, IDC_CHECK_TIME8, m_chkExtraSegmentEnable[3]);
	DDX_Control(pDX, IDC_CHECK_TIME9, m_chkExtraSegmentEnable[4]);
	DDX_Control(pDX, IDC_CHECK_TIME10, m_chkExtraSegmentEnable[5]);
	DDX_Control(pDX, IDC_CHECK_TIME11, m_chkExtraSegmentEnable[6]);
	DDX_Control(pDX, IDC_CHECK_TIME12, m_chkExtraSegmentEnable[7]);
	DDX_Control(pDX, IDC_CHECK_TIME13, m_chkExtraSegmentEnable[8]);
	DDX_Control(pDX, IDC_CHECK_TIME14, m_chkExtraSegmentEnable[9]);
	DDX_Control(pDX, IDC_CHECK_TIME15, m_chkExtraSegmentEnable[10]);
	DDX_Control(pDX, IDC_CHECK_TIME16, m_chkExtraSegmentEnable[11]);
	DDX_Control(pDX, IDC_CHECK_TIME17, m_chkExtraSegmentEnable[12]);
	DDX_Control(pDX, IDC_CHECK_TIME18, m_chkExtraSegmentEnable[13]);
	DDX_Control(pDX, IDC_CHECK_TIME19, m_chkExtraSegmentEnable[14]);
	DDX_Control(pDX, IDC_CHECK_TIME20, m_chkExtraSegmentEnable[15]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME5, m_dtExtraBeginTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME6, m_dtExtraBeginTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME7, m_dtExtraBeginTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME8, m_dtExtraBeginTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME9, m_dtExtraBeginTime[4]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME10, m_dtExtraBeginTime[5]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME11, m_dtExtraBeginTime[6]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME12, m_dtExtraBeginTime[7]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME13, m_dtExtraBeginTime[8]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME14, m_dtExtraBeginTime[9]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME15, m_dtExtraBeginTime[10]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME16, m_dtExtraBeginTime[11]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME17, m_dtExtraBeginTime[12]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME18, m_dtExtraBeginTime[13]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME19, m_dtExtraBeginTime[14]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME20, m_dtExtraBeginTime[15]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME5, m_dtExtraEndTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME6, m_dtExtraEndTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME7, m_dtExtraEndTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME8, m_dtExtraEndTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME9, m_dtExtraEndTime[4]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME10, m_dtExtraEndTime[5]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME11, m_dtExtraEndTime[6]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME12, m_dtExtraEndTime[7]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME13, m_dtExtraEndTime[8]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME14, m_dtExtraEndTime[9]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME15, m_dtExtraEndTime[10]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME16, m_dtExtraEndTime[11]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME17, m_dtExtraEndTime[12]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME18, m_dtExtraEndTime[13]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME19, m_dtExtraEndTime[14]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME20, m_dtExtraEndTime[15]);
	DDX_Control(pDX, IDC_COMBO_SCHEDULE_SCENE2, m_cboVcaType);
}


BEGIN_MESSAGE_MAP(CLS_VCAAlarmSchedulePage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_ALARM_SCHEDULE_SET, &CLS_VCAAlarmSchedulePage::OnBnClickedButtonAlarmScheduleSet)
	ON_CBN_SELCHANGE(IDC_COMBO_ALARM_SCHEDULE_RULE, &CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboAlarmScheduleRule)
	ON_BN_CLICKED(IDC_BUTTON_ALARM_SCHEDULE_ALLDAY, &CLS_VCAAlarmSchedulePage::OnBnClickedButtonAlarmScheduleAllday)
	ON_CBN_SELCHANGE(IDC_COMBO_ALARM_SCHEDULE_WEEK, &CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboAlarmScheduleWeek)
	ON_BN_CLICKED(IDC_BUTTON_ALARM_SCHEDULE_ENABLE, &CLS_VCAAlarmSchedulePage::OnBnClickedButtonAlarmScheduleEnable)
	ON_CBN_SELCHANGE(IDC_COMBO_SCHEDULE_SCENE, &CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboScheduleScene)
	ON_CBN_SELCHANGE(IDC_COMBO_SDDEVICENO, &CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboSddeviceno)
	ON_CBN_SELCHANGE(IDC_COMBO_ALARM_SCHEDULE_ENABLE, &CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboAlarmScheduleEnable)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_COMBO_SCHEDULE_SCENE2, &CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboScheduleScene2)
END_MESSAGE_MAP()


// CLS_VCAAlarmSchedulePage message handlers

void CLS_VCAAlarmSchedulePage::OnBnClickedButtonAlarmScheduleSet()
{
	// TODO: Add your control notification handler code here

	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}
	CheckPTZTime();
	int iWeek = m_cboWeek.GetCurSel();
	SetAlarmSchedule(iWeek);
	SetAlarmScheduleExtra(iWeek);
}

BOOL CLS_VCAAlarmSchedulePage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	for (int i = 0; i < VCA_MAX_RULE_NUM_V2; i++)
	{
		CString szRuleID;
		szRuleID.Format("%d", i + 1);
		m_cboRuleID.AddString(szRuleID);
	}
	for(int j = 1; j <= MAX_SCENE_NUM; j++)
	{
		CString szSceneID;
		szSceneID.Format("%d", j);
		m_CboSceneId.AddString(szSceneID);
	}
	m_CboSceneId.SetCurSel(0);
	m_cboRuleID.SetCurSel(0);
	UI_Clear();
	UI_UpdateText();
	SYSTEMTIME tSysTime;
	GetLocalTime(&tSysTime);
	m_cboWeek.SetCurSel(tSysTime.wDayOfWeek);
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboAlarmScheduleRule()
{
	// TODO: Add your control notification handler code here
	if(m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}
	m_cboEvent.SetCurSel(-1);
	UI_UpdateAlarmEnable();
	UI_UpdateAlarmSchedule();
}

void CLS_VCAAlarmSchedulePage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateText();	
}

void CLS_VCAAlarmSchedulePage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iChannelNo = (_iChannelNo < 0) ? 0 : _iChannelNo;
	m_iLogonID = _iLogonID;
	if (m_iLogonID < 0)
	{
		return;
	}
	m_cboEvent.SetCurSel(-1);
	UI_UpdateAlarmEnable();
	UI_UpdateAlarmSchedule();
}

void CLS_VCAAlarmSchedulePage::UI_UpdateAlarmSchedule()
{
	UI_Clear();
	//schedule
	int iWeekDay = m_cboWeek.GetCurSel();
	TAlarmScheduleParam alarmParam = {0};
	alarmParam.iBuffSize = sizeof(TAlarmScheduleParam);
	alarmParam.iSceneID = m_CboSceneId.GetCurSel();
	alarmParam.iWeekday = iWeekDay;
	alarmParam.iParam1 = m_cboRuleID.GetCurSel();
	alarmParam.iParam2 = m_cboEvent.GetCurSel();
	if(m_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	if(1 == m_cboVcaType.GetCurSel())
	{
		iVcaType = ALARM_TYPE_ZF_VCA;
	}
	int iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, iVcaType, CMD_GET_ALARMSCHEDULE, &alarmParam);
	if (iRet >= 0)
	{
		NVS_SCHEDTIME *pTS = NULL;
		CTime timeNow = CTime::GetCurrentTime();
		CTime timeSchStart, timeSchStop;
		for (int i=0; i<MAX_TIMESEGMENT; i++)
		{
			pTS = &alarmParam.timeSeg[iWeekDay][i];
			VERIFY(pTS);

			if (pTS->iStartHour >= 0 && pTS->iStartHour < MAX_HOUR 
				&& pTS->iStartMin >= 0 && pTS->iStartMin < MAX_MINUTE)
			{
				timeSchStart = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), pTS->iStartHour, pTS->iStartMin, 0);
			} 
			else
			{
				timeSchStart = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), 0, 0, 0);
			}
			if (pTS->iStopHour >= 0 && pTS->iStopHour < MAX_HOUR 
				&& pTS->iStopMin >= 0 && pTS->iStopMin < MAX_MINUTE)
			{
				timeSchStop = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), pTS->iStopHour, pTS->iStopMin, 0);
			} 
			else
			{
				timeSchStop = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), 0, 0, 0);
			}
			m_dtBeginTime[i].SetTime(&timeSchStart);
			m_dtEndTime[i].SetTime(&timeSchStop);
			m_chkEnbale[i] = (BOOL)pTS->iRecordMode;
		}
	}
	else
	{
		m_cboEvent.SetCurSel(0);
		CTime timeNow = CTime::GetCurrentTime();
		CTime timeSchStart, timeSchStop;
		timeSchStart = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(),0, 0, 0);
		timeSchStop = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(),0, 0, 0);
		for (int i=0; i<MAX_TIMESEGMENT; i++)
		{
			m_dtBeginTime[i].SetTime(&timeSchStart);
			m_dtEndTime[i].SetTime(&timeSchStop);
			m_chkEnbale[i] = FALSE;
		}

	}
	UI_UpdateAlarmScheduleExtra();
	UpdateData(FALSE);
}

void CLS_VCAAlarmSchedulePage::UI_UpdateAlarmScheduleExtra()
{
	if(1 == m_cboVcaType.GetCurSel())
	{
		iVcaType = ALARM_TYPE_ZF_VCA;
	}
	int iWeekDay = m_cboWeek.GetCurSel();
	ExtraAlarmSchedule tSchedule = {0};
	tSchedule.iSize = sizeof(ExtraAlarmSchedule);
	tSchedule.iSceneID = m_CboSceneId.GetCurSel();
	tSchedule.iWeekday = iWeekDay;
	tSchedule.iAlarmType = iVcaType;
	tSchedule.iParam1 = m_cboRuleID.GetCurSel();
	tSchedule.iParam2 = m_cboEvent.GetCurSel();
	if(m_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	int iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, iVcaType, CMD_ALARM_EXTRA_SCHEDULE, &tSchedule);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"", "[UpdateAlarmScheduleExtra]GetAlarmConfig EXTRA_SCHEDULE Failed! Err = %d", iRet);
		memset(&tSchedule, 0, sizeof(ExtraAlarmSchedule));
	}

	CTime timeNow = CTime::GetCurrentTime();
	CTime timeSchStart, timeSchStop;
	for (int i = 0; i < MAX_EXTRA_SCHEDULE_SEGMENT_NUM; i++)
	{
		BOOL blEnableWindow = FALSE;
		int	iChkEnable = tSchedule.tTimeSegment[i].iEnable ? BST_CHECKED : BST_UNCHECKED;
		if (i >= tSchedule.iSegmentNum)
		{
			timeSchStart = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), 0, 0, 0);
			timeSchStop = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), 0, 0, 0);
		}
		else
		{
			timeSchStart = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), tSchedule.tTimeSegment[i].iStartHour, tSchedule.tTimeSegment[i].iStartMin, 0);
			timeSchStop = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), tSchedule.tTimeSegment[i].iStopHour, tSchedule.tTimeSegment[i].iStopMin, 0);
			blEnableWindow = TRUE;
		}

		m_dtExtraBeginTime[i].SetTime(&timeSchStart);
		m_dtExtraEndTime[i].SetTime(&timeSchStop);

		if (blEnableWindow)
		{
			m_chkExtraSegmentEnable[i].SetCheck(iChkEnable);
		}
		m_chkExtraSegmentEnable[i].EnableWindow(blEnableWindow);
	}

	return;
}

void CLS_VCAAlarmSchedulePage::UI_UpdateAlarmEnable()
{

	if(m_iChannelNo < 0 || m_iChannelNo >= MAX_CHANNEL_NUM)
	{
		m_iChannelNo = 0;
	}

	int iSceneID = m_CboSceneId.GetCurSel();
	int iRuleID = m_cboRuleID.GetCurSel();
	VCARuleParam  tParam = {0};
	tParam.stRule.iRuleID = iRuleID;
	tParam.stRule.iSceneID = iSceneID;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RULE_PARAM, m_iChannelNo, &tParam, sizeof(VCARuleParam));
	if (iRet < 0 || !tParam.stRule.iValid)
	{
		m_cboEvent.SetCurSel(0);
		return;
	}
	int iEventID = tParam.iEventID;
	m_cboEvent.SetCurSel(iEventID);

	TAlarmScheEnableParam st = {0};
	st.iSceneID = iSceneID;
	st.iEnable = 0;
	st.iBuffSize = sizeof(TAlarmScheEnableParam);
	st.pvReserved = NULL;
	st.iParam1 = iRuleID;
	st.iParam2 = iEventID;

	if(1 == m_cboVcaType.GetCurSel())
	{
		iVcaType = ALARM_TYPE_ZF_VCA;
	}
	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo,iVcaType, CMD_GET_ALARMSCH_ENABLE, &st);
	if(iRet < 0)
	{
		m_cboEnbale.SetCurSel(0);
	}
	else
	{
		m_cboEnbale.SetCurSel(st.iEnable);
		UI_UpdataAlarmScheduleButton(st.iEnable);
	}
}

void CLS_VCAAlarmSchedulePage::SetAlarmSchedule( int iWeek )
{
	UpdateData(TRUE);
	int iRuleID = m_cboRuleID.GetCurSel();
	int iEventID = m_cboEvent.GetCurSel();
	int iSceneID = m_CboSceneId.GetCurSel();
	int iWeekDay = iWeek;
	NVS_SCHEDTIME schedtime[MAX_TIMESEGMENT];
	vca_TVCAParam * vp = &g_VcaParam;
	memset(vp, 0, sizeof(vca_TVCAParam));

	vp->chnParam[m_iChannelNo].iRuleID = iRuleID;
	vca_TAlarmSchedule * pAS = NULL;
	pAS = &vp->chnParam[m_iChannelNo].rule[iRuleID].alarmSchedule;
	pAS->iWeekday = iWeekDay;
	CTime BeginTime;
	CTime EndTime;

	for (int i=0; i<MAX_TIMESEGMENT; i++)
	{
		m_dtBeginTime[i].GetTime(BeginTime);
		m_dtEndTime[i].GetTime(EndTime);
	
		schedtime[i].iRecordMode = m_chkEnbale[i];		
		schedtime[i].iStartHour = BeginTime.GetHour();
		schedtime[i].iStartMin = BeginTime.GetMinute();
		schedtime[i].iStopHour = EndTime.GetHour();
		schedtime[i].iStopMin = EndTime.GetMinute();
	}

	TAlarmScheduleParam alarmParam = {0};
	alarmParam.iBuffSize = sizeof(alarmParam);
	alarmParam.iWeekday = iWeekDay;
	alarmParam.iSceneID = iSceneID;
	alarmParam.iParam1 = iRuleID;
	alarmParam.iParam2 = iEventID;
	for(int i=0; i< MAX_TIMESEGMENT; i++)
	{
		alarmParam.timeSeg[iWeekDay][i] = schedtime[i];
	}

	if(1 == m_cboVcaType.GetCurSel())
	{
		iVcaType = ALARM_TYPE_ZF_VCA;
	}

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, iVcaType, CMD_SET_ALARMSCHEDULE,  &alarmParam);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetAlarmConfig(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetAlarmConfig(%d)", m_iLogonID);
	}
}

void CLS_VCAAlarmSchedulePage::SetAlarmScheduleExtra( int iWeek )
{
	if(1 == m_cboVcaType.GetCurSel())
	{
		iVcaType = ALARM_TYPE_ZF_VCA;
	}
	int iWeekDay = m_cboWeek.GetCurSel();
	ExtraAlarmSchedule tSchedule = {0};
	tSchedule.iSize = sizeof(ExtraAlarmSchedule);
	tSchedule.iSceneID = m_CboSceneId.GetCurSel();
	tSchedule.iWeekday = iWeek;
	tSchedule.iAlarmType = iVcaType;
	tSchedule.iParam1 = m_cboRuleID.GetCurSel();
	tSchedule.iParam2 = m_cboEvent.GetCurSel();
	m_iChannelNo = (m_iChannelNo < 0) ? 0 : m_iChannelNo;
	tSchedule.iSegmentNum = MAX_EXTRA_SCHEDULE_SEGMENT_NUM;

	for (int i = 0; i < MAX_EXTRA_SCHEDULE_SEGMENT_NUM; i++)
	{
		DayScheduleTime& tSegment = tSchedule.tTimeSegment[i];
		CTime BeginTime;
		CTime EndTime;
		m_dtExtraBeginTime[i].GetTime(BeginTime);
		m_dtExtraEndTime[i].GetTime(EndTime);
		tSegment.iEnable = (BST_CHECKED == m_chkExtraSegmentEnable[i].GetCheck()) ? 1 : 0;		
		tSegment.iStartHour = BeginTime.GetHour();
		tSegment.iStartMin = BeginTime.GetMinute();
		tSegment.iStopHour = EndTime.GetHour();
		tSegment.iStopMin = EndTime.GetMinute();
	}

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, iVcaType, CMD_ALARM_EXTRA_SCHEDULE, &tSchedule);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"", "[SetAlarmScheduleExtra]SetAlarmConfig EXTRA_SCHEDULE Failed! Err = %d", iRet);
	}
	return;
}

void CLS_VCAAlarmSchedulePage::OnBnClickedButtonAlarmScheduleAllday()
{
	// TODO: Add your control notification handler code here
	if (m_iChannelNo < 0 || m_iLogonID < 0)
	{
		return;
	}
	for (int i = 0; i < 7; i++)
	{
		SetAlarmSchedule(i);
		SetAlarmScheduleExtra(i);
	}
}

void CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboAlarmScheduleWeek()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return ;
	}
	UI_UpdateAlarmSchedule();
}

void CLS_VCAAlarmSchedulePage::OnBnClickedButtonAlarmScheduleEnable()
{
	// TODO: Add your control notification handler code here
	
}

void CLS_VCAAlarmSchedulePage::UI_UpdateText()
{
	SetDlgItemTextEx(IDC_STATIC_RULE_ID, IDS_VCA_RULE_ID);
	SetDlgItemTextEx(IDC_STATIC_EVENT_ID, IDS_VCA_EVENT_ID);
	SetDlgItemTextEx(IDC_STATIC_ALARM_SCHEDULE_ENABLE, IDS_VCA_SCHEDULE_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_ENABLE, IDS_VCA_ALARM_ENABLE);
	SetDlgItemTextEx(IDC_BUTTON_ALARM_SCHEDULE_ENABLE, IDS_VCA_LINK_SET);
	SetDlgItemTextEx(IDC_STATIC_SCHEDULE, IDS_VCA_SCHEDULE_SET);
	SetDlgItemTextEx(IDC_STATIC_OUT_WEEKDAY, IDS_VCA_WEEK);
	SetDlgItemTextEx(IDC_BUTTON_ALARM_SCHEDULE_SET, IDS_VCA_LINK_SET);
	SetDlgItemTextEx(IDC_BUTTON_ALARM_SCHEDULE_ALLDAY, IDS_VCA_ALL_WEEK_COPY);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME1,IDS_CONFIG_DNVR_ALMSCH_TIME1);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME2,IDS_CONFIG_DNVR_ALMSCH_TIME2);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME3,IDS_CONFIG_DNVR_ALMSCH_TIME3);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME4,IDS_CONFIG_DNVR_ALMSCH_TIME4);
	SetDlgItemTextEx(IDC_STATIC_SCHEDULE_SCENE,IDS_VCA_SCENE_ID);

	//SetDlgItemText(IDC_STATIC_OUT_TIME5, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(1));
	//SetDlgItemText(IDC_STATIC_OUT_TIME6, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(2));
	//SetDlgItemText(IDC_STATIC_OUT_TIME7, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(3));
	//SetDlgItemText(IDC_STATIC_OUT_TIME8, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(4));
	//SetDlgItemText(IDC_STATIC_OUT_TIME9, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(5));
	//SetDlgItemText(IDC_STATIC_OUT_TIME10, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(6));
	//SetDlgItemText(IDC_STATIC_OUT_TIME11, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(7));
	//SetDlgItemText(IDC_STATIC_OUT_TIME12, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(8));
	//SetDlgItemText(IDC_STATIC_OUT_TIME13, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(9));
	//SetDlgItemText(IDC_STATIC_OUT_TIME14, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(10));
	//SetDlgItemText(IDC_STATIC_OUT_TIME15, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(11));
	//SetDlgItemText(IDC_STATIC_OUT_TIME16, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(12));
	//SetDlgItemText(IDC_STATIC_OUT_TIME17, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(13));
	//SetDlgItemText(IDC_STATIC_OUT_TIME18, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(14));
	//SetDlgItemText(IDC_STATIC_OUT_TIME19, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(15));
	//SetDlgItemText(IDC_STATIC_OUT_TIME20, GetTextEx(IDS_EXTRA_TIME_SEGMENT) + IntToCString(16));
	GetDlgItem(IDC_STATIC_OUT_TIME5)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME6)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME7)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME8)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME9)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME10)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME11)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME12)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME13)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME14)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME15)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME16)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME17)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME18)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME19)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_OUT_TIME20)->ShowWindow(SW_HIDE);
	for (int i=0; i < MAX_EXTRA_SCHEDULE_SEGMENT_NUM; i++)
	{
		m_chkExtraSegmentEnable[i].ShowWindow(SW_HIDE);
		m_dtExtraBeginTime[i].ShowWindow(SW_HIDE);
		m_dtExtraEndTime[i].ShowWindow(SW_HIDE);
	}
	SetDlgItemText(IDC_STATIC_SCHEDULE_VCA_TYPE, GetTextByLan(_T("智能分析类型"), _T("Vca Type")));

	for (int i = 0; i < 7; i++)
	{
		InsertString(m_cboWeek, i, g_iWeek[i]);
	}
	for (int i = 0; i < VCA_EVENT_MAX; i++)
	{
		InsertString(m_cboEvent, i, g_iEventIDS[i]);
	}

	m_cboEvent.InsertString(37, GetTextByLan(_T("单人询问"), _T("SINGLE_INQUIRY")));
	m_cboEvent.InsertString(38, GetTextByLan(_T("攀高"), _T("CLIMB_UP")));
	m_cboEvent.InsertString(39, GetTextByLan(_T("新离岗"), _T("NET_DEPARTURE")));
	m_cboEvent.InsertString(40, GetTextByLan(_T("人数异常"), _T("ABNORMAL_NUMBER")));
	m_cboEvent.InsertString(41, GetTextByLan(_T("起身"), _T("GET_UP")));
	m_cboEvent.InsertString(42, GetTextByLan(_T("离床"), _T("LEAVE_BED")));
	m_cboEvent.InsertString(43, GetTextByLan(_T("静止检测"), _T("STATIC_DETECTION")));
	m_cboEvent.InsertString(44, GetTextByLan(_T("睡岗"), _T("SLEEP_POSTION")));
	m_cboEvent.InsertString(45, GetTextByLan(_T("摔倒"), _T("SLIP_UP")));
	m_cboEvent.InsertString(46, GetTextByLan(_T("新打架"), _T("NEW_FIGHT")));
	m_cboEvent.InsertString(47, GetTextByLan(_T("肢体接触"), _T("BODY_TOUCH")));
	m_cboEvent.InsertString(48, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(49, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(50, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(51, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(52, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(53, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(54, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(55, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(56, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(57, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(58, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(59, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(60, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(61, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(62, GetTextByLan(_T("预留"), _T("RESERVED")));
	m_cboEvent.InsertString(63, GetTextByLan(_T("温度检测"), _T("TEMP_DETECTION")));

	for (int i = 0; i < 2; i++)
	{
		InsertString(m_cboEnbale, i, g_iEnableIDS[i]);
	}

	m_cboVcaType.ResetContent();
	m_cboVcaType.InsertString(0, GetTextByLan(_T("智能分析"), _T("VCA")));
	m_cboVcaType.InsertString(1, GetTextByLan(_T("政法智能分析"), _T("ZF_VCA")));
	m_cboVcaType.SetCurSel(0);
}

void CLS_VCAAlarmSchedulePage::UI_Clear()
{
	CTime BeginTime(1971, 1, 1, 0, 0, 0);
	CTime EndTime(1971, 1, 1, 23, 59, 59);
	for (int i = 0; i < 4; i++)
	{
		m_dtBeginTime[i].SetFormat("HH:mm");
		m_dtBeginTime[i].SetTime(&BeginTime);
		m_dtEndTime[i].SetFormat("HH:mm");
		m_dtEndTime[i].SetTime(&EndTime);
		m_chkEnbale[i] = FALSE;
	}

	for (int i = 0; i < MAX_EXTRA_SCHEDULE_SEGMENT_NUM; i++)
	{
		m_dtExtraBeginTime[i].SetFormat("HH:mm");
		m_dtExtraBeginTime[i].SetTime(&BeginTime);
		m_dtExtraEndTime[i].SetFormat("HH:mm");
		m_dtExtraEndTime[i].SetTime(&EndTime);
		m_chkExtraSegmentEnable[i].SetCheck(BST_UNCHECKED);
		m_chkExtraSegmentEnable[i].EnableWindow(FALSE);
	}
	UpdateData(FALSE);
}
void CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboScheduleScene()
{
	UI_UpdateAlarmEnable();
	UI_UpdateAlarmSchedule();
}

void CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboSddeviceno()
{
	// TODO: Add your control notification handler code here
}



void CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboAlarmScheduleEnable()
{
	if (m_iChannelNo < 0 || m_iLogonID < 0)
	{
		return;
	}
	int iRuleID = m_cboRuleID.GetCurSel(); 
	int iEventID = m_cboEvent.GetCurSel();
	if(iEventID < 0)
	{
		iEventID = 0;
		m_cboEvent.SetCurSel(0);
	}
	TAlarmScheEnableParam ScheEnableParam = {0};
	ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
	ScheEnableParam.iParam1 = iRuleID;
	ScheEnableParam.iParam2 = iEventID;
	ScheEnableParam.iSceneID = m_CboSceneId.GetCurSel();
	ScheEnableParam.iEnable = m_cboEnbale.GetCurSel();
	UI_UpdataAlarmScheduleButton(ScheEnableParam.iEnable);
	int iCmd = CMD_SET_ALARMSCH_ENABLE;

	if(1 == m_cboVcaType.GetCurSel())
	{
		iVcaType = ALARM_TYPE_ZF_VCA;
		iCmd = CMD_ALARMSCH_ENABLE_EX;
	}
	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, iVcaType, iCmd, &ScheEnableParam);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"", "[frmVCAAlarmPage::OnSetScheEnable]TDSetAlarmConfig error = 0x%p", GetLastError());
	}
}

void CLS_VCAAlarmSchedulePage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UI_UpdateAlarmEnable();
		UI_UpdateAlarmSchedule();
	}
}

void CLS_VCAAlarmSchedulePage::UI_UpdataAlarmScheduleButton(int _iEnable)
{
	BOOL blEnableWindow = (DISABLE == _iEnable) ? FALSE : TRUE;
	GetDlgItem(IDC_BUTTON_ALARM_SCHEDULE_SET)->EnableWindow(blEnableWindow);
	GetDlgItem(IDC_BUTTON_ALARM_SCHEDULE_ALLDAY)->EnableWindow(blEnableWindow);
}

void CLS_VCAAlarmSchedulePage::UpdateEventRuleID()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		return;
	}

	vca_TVCAParam *vp = NULL;
	memset(vp, 0, sizeof(vca_TVCAParam));

	vp->iChannelID = m_iChannelNo;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_CHANNEL, m_iChannelNo, vp, sizeof(vca_TVCAParam));
	bool bVCAChanEnable = false;
	if (iRet >= 0)
	{
		bVCAChanEnable = (vp->chnParam[vp->iChannelID].iEnable != 0);
	}

	if (bVCAChanEnable)
	{
		VCARuleParam  pParam = {0};
		pParam.stRule.iRuleID = m_cboRuleID.GetCurSel();
		pParam.stRule.iSceneID = m_CboSceneId.GetCurSel();
		iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RULE_PARAM, m_iChannelNo, &pParam, sizeof(VCARuleParam));
		if (iRet >= 0)
		{
			for (int i = 0; i < m_cboEvent.GetCount(); ++i)
			{
				if (pParam.iEventID == m_cboEvent.GetItemData(i))
				{
					m_cboEvent.SetCurSel(i);
					break;
				}
			}
		}
		else
		{
			m_cboEvent.SetCurSel(0);
		}
	}
}

void CLS_VCAAlarmSchedulePage::OnCbnSelchangeComboScheduleScene2()
{
	UI_UpdateAlarmEnable();
	UI_UpdateAlarmSchedule();
	UI_UpdateAlarmSchedule();
}

void CLS_VCAAlarmSchedulePage::CheckPTZTime()
{
	CTime BeginTime;
	CTime EndTime;
	CTime BeginTime_before;
	CTime EndTime_before;
	for (int i = 0;i <= 3;i++)
	{
		if (!m_chkEnbale[i])
		{
			continue;
		}
		m_dtBeginTime[i].GetTime(BeginTime_before);
		m_dtEndTime[i].GetTime(EndTime_before);
		// Start and end are 0, do not participate in comparison
		if (BeginTime_before.GetHour() == 0 && BeginTime_before.GetMinute() == 0 && BeginTime_before.GetSecond() == 0 && EndTime_before.GetHour() == 0 && EndTime_before.GetMinute() ==0 && EndTime_before.GetSecond() == 0)
		{
			continue;
		}
		if (EndTime_before <= BeginTime_before)
		{
			AfxMessageBox(GetTextByLan(_T("请检查您的时间，重新输入！"),_T("Please Check your time, ant input again")));
			return;
		}

		for (int j = 0;j <= 3;j++)
		{
			if (j == i)
			{
				continue;
			}

			m_dtBeginTime[j].GetTime(BeginTime);
			m_dtEndTime[j].GetTime(EndTime);

			if (BeginTime.GetHour() == 0 && BeginTime.GetMinute() == 0 && BeginTime.GetSecond() == 0 && EndTime.GetHour() == 0 && EndTime.GetMinute() ==0 && EndTime.GetSecond() == 0)
			{
				continue;
			}
			if (EndTime <= BeginTime)
			{
				AfxMessageBox(GetTextByLan(_T("请检查您的时间，重新输入！"),_T("Please Check your time, ant input again")));
				return;
			}
			else if ((BeginTime_before <= BeginTime && BeginTime <= EndTime_before) || (BeginTime_before <= EndTime && EndTime <= EndTime_before))               
			{
				AfxMessageBox(GetTextByLan(GetTextByLan(_T("请检查您的时间，重新输入！"),_T("Please Check your time, ant input again"))));
				return;
			}
		}
	}
}

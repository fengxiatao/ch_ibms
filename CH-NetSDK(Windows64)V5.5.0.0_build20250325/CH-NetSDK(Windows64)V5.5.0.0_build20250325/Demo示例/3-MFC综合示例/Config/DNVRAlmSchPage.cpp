// DNVRAlmSchPage.cpp : implementation file
//

#include "stdafx.h"
#include "DNVRAlmSchPage.h"

#define PORT_ALARM_SCHEDULE			0
#define VIDEO_LOST_SCHEDULE			1
#define VIDEO_MOTION_SCHEDULE		2
#define VIDEO_COVER_SCHEDULE		3
#define AUDIO_LOST_SCHEDULE			4
#define WATER_RAIN_SCHEDULE			5
#define WATER_ALERT_WATER_SCHEDULE	6
#define DANGEROUS_AREA_SCHEDULE		7
#define VOLTAGE_AREA_SCHEDULE		8
#define BLACK_BODY_ERR_SCHEDULE		9
#define SNAP_ONTIME_SCHEDULE		10
#define VEHICLE_IDENTIFY_SCHEDULE	11

// CLS_DNVRAlmSchPage dialog

IMPLEMENT_DYNAMIC(CLS_DNVRAlmSchPage, CDialog)

CLS_DNVRAlmSchPage::CLS_DNVRAlmSchPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DNVRAlmSchPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
}

CLS_DNVRAlmSchPage::~CLS_DNVRAlmSchPage()
{
}

void CLS_DNVRAlmSchPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_cboType);
	DDX_Control(pDX, IDC_COMBO_INPORT, m_cboInPort);
	DDX_Control(pDX, IDC_CHECK_INPORT, m_chkInPort);
	DDX_Control(pDX, IDC_COMBO_WEEKDAY, m_cboWeekday);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME1, m_dtcStartTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME1, m_dtcEndTime[0]);
	DDX_Control(pDX, IDC_CHECK_TIME1, m_chkTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME2, m_dtcStartTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME2, m_dtcEndTime[1]);
	DDX_Control(pDX, IDC_CHECK_TIME2, m_chkTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME3, m_dtcStartTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME3, m_dtcEndTime[2]);
	DDX_Control(pDX, IDC_CHECK_TIME3, m_chkTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME4, m_dtcStartTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME4, m_dtcEndTime[3]);
	DDX_Control(pDX, IDC_CHECK_TIME4, m_chkTime[3]);
	DDX_Control(pDX, IDC_BUTTON_ALLDAY, m_btnAllDay);
	DDX_Control(pDX, IDC_BUTTON_INPORT, m_btnInPort);
	DDX_Control(pDX, IDC_COMBO_OUTPORT, m_cboOutPort);
	DDX_Control(pDX, IDC_CHECK_OUTPORT, m_chkOutPort);
	DDX_Control(pDX, IDC_COMBO_OUT_WEEKDAY, m_cboOutWeekday);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_STARTTIME1, m_dtcOutStartTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_ENDTIME1, m_dtcOutEndTime[0]);
	DDX_Control(pDX, IDC_CHECK_OUT_TIME1, m_chkOutTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_STARTTIME2, m_dtcOutStartTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_ENDTIME2, m_dtcOutEndTime[1]);
	DDX_Control(pDX, IDC_CHECK_OUT_TIME2, m_chkOutTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_STARTTIME3, m_dtcOutStartTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_ENDTIME3, m_dtcOutEndTime[2]);
	DDX_Control(pDX, IDC_CHECK_OUT_TIME3, m_chkOutTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_STARTTIME4, m_dtcOutStartTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_OUT_ENDTIME4, m_dtcOutEndTime[3]);
	DDX_Control(pDX, IDC_CHECK_OUT_TIME4, m_chkOutTime[3]);
	DDX_Control(pDX, IDC_BUTTON_OUT_ALLDAY, m_btnOutAllDay);
	DDX_Control(pDX, IDC_BUTTON_OUTPORT, m_btnOutPort);
	DDX_Control(pDX, IDC_COMBO_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_COMBO_ALARM_SUB_TYPE, m_cboAlarmSubType);
	DDX_Control(pDX, IDC_COMBO_PLATE_LIB_ID, m_cboPlateLibId);
}


BEGIN_MESSAGE_MAP(CLS_DNVRAlmSchPage, CLS_BasePage)
	ON_BN_CLICKED(IDC_CHECK_INPORT, &CLS_DNVRAlmSchPage::OnBnClickedCheckInport)
	ON_CBN_SELCHANGE(IDC_COMBO_WEEKDAY, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboWeekday)
	ON_CBN_SELCHANGE(IDC_COMBO_TYPE, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboType)
	ON_BN_CLICKED(IDC_CHECK_TIME1, &CLS_DNVRAlmSchPage::OnBnClickedCheckTime1)
	ON_BN_CLICKED(IDC_CHECK_TIME2, &CLS_DNVRAlmSchPage::OnBnClickedCheckTime2)
	ON_BN_CLICKED(IDC_CHECK_TIME3, &CLS_DNVRAlmSchPage::OnBnClickedCheckTime3)
	ON_BN_CLICKED(IDC_CHECK_TIME4, &CLS_DNVRAlmSchPage::OnBnClickedCheckTime4)
	ON_BN_CLICKED(IDC_BUTTON_ALLDAY, &CLS_DNVRAlmSchPage::OnBnClickedButtonAllday)
	ON_BN_CLICKED(IDC_BUTTON_INPORT, &CLS_DNVRAlmSchPage::OnBnClickedButtonInport)
	ON_CBN_SELCHANGE(IDC_COMBO_OUTPORT, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboOutport)
	ON_CBN_SELCHANGE(IDC_COMBO_OUT_WEEKDAY, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboOutWeekday)
	ON_BN_CLICKED(IDC_CHECK_OUTPORT, &CLS_DNVRAlmSchPage::OnBnClickedCheckOutport)
	ON_BN_CLICKED(IDC_CHECK_OUT_TIME1, &CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime1)
	ON_BN_CLICKED(IDC_CHECK_OUT_TIME2, &CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime2)
	ON_BN_CLICKED(IDC_CHECK_OUT_TIME3, &CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime3)
	ON_BN_CLICKED(IDC_CHECK_OUT_TIME4, &CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime4)
	ON_BN_CLICKED(IDC_BUTTON_OUT_ALLDAY, &CLS_DNVRAlmSchPage::OnBnClickedButtonOutAllday)
	ON_BN_CLICKED(IDC_BUTTON_OUTPORT, &CLS_DNVRAlmSchPage::OnBnClickedButtonOutport)
	ON_CBN_SELCHANGE(IDC_COMBO_INPORT, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboInport)
	ON_CBN_SELCHANGE(IDC_COMBO_DEVTYPE, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboDevtype)
	ON_CBN_SELCHANGE(IDC_COMBO_ALARM_SUB_TYPE, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboAlarmSubType)
	ON_CBN_SELCHANGE(IDC_COMBO_PLATE_LIB_ID, &CLS_DNVRAlmSchPage::OnCbnSelchangeComboPlateLibId)
END_MESSAGE_MAP()


// CLS_DNVRAlmSchPage message handlers
BOOL CLS_DNVRAlmSchPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_chkInPort.SetCheck(BST_UNCHECKED);
	m_chkOutPort.SetCheck(BST_UNCHECKED);
	for (int i=0; i<4; i++)
	{
		m_dtcStartTime[i].EnableWindow(FALSE);
		m_dtcEndTime[i].EnableWindow(FALSE);
		m_dtcOutStartTime[i].EnableWindow(FALSE);
		m_dtcOutEndTime[i].EnableWindow(FALSE);
		m_dtcStartTime[i].SetFormat(_T("HH:mm"));
		m_dtcEndTime[i].SetFormat(_T("HH:mm"));
		m_dtcOutStartTime[i].SetFormat(_T("HH:mm"));
		m_dtcOutEndTime[i].SetFormat(_T("HH:mm"));
	}

	UI_UpdateDialog();
	
	m_cboType.SetCurSel(0);
	m_cboWeekday.SetCurSel(0);
	m_cboOutWeekday.SetCurSel(0);
	m_cboAlarmSubType.SetCurSel(0);
	m_cboPlateLibId.SetCurSel(0);

	m_cboDevType.ShowWindow(FALSE);
	GetDlgItem(IDC_STATIC_DEVTYPE)->ShowWindow(FALSE);
	m_cboDevType.SetCurSel(0);

	return TRUE;
}

void CLS_DNVRAlmSchPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}

	UI_UpdateDialog();
	UI_UpdateType();
	OnCbnSelchangeComboType();
	OnCbnSelchangeComboOutport();
	OnCbnSelchangeComboWeekday();
}

void CLS_DNVRAlmSchPage::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_DNVRAlmSchPage::OnParamChangeNotify( int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUser )
{
	TAlarmScheEnableParam ScheEnableParam = {0};
	ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
	switch(_iParaType)
	{
	case PARA_VOLTAGE_HIGH_SCHEDULEENABLE:
		AddLog(LOG_TYPE_SUCC,"","OnParamChangeNotify (PARA_VOLTAGE_HIGH_SCHEDULEENABLE)");
		NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, ALARM_TYPE_VOLTAGE_HIGH, CMD_GET_ALARMSCH_ENABLE, &ScheEnableParam);
		break;
	case PARA_VOLTAGE_LOW_SCHEDULEENABLE:
		AddLog(LOG_TYPE_SUCC,"","OnParamChangeNotify (PARA_VOLTAGE_LOW_SCHEDULEENABLE)");
		break;
	default:
		break;
	}
}

void CLS_DNVRAlmSchPage::OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser)
{
	switch(_iAlarmType)
	{
	case ALARM_TYPE_VOLTAGE_HIGH:
	case ALARM_TYPE_VOLTAGE_LOW:
		AddLog(LOG_TYPE_SUCC,"","OnAlarmNotify_V5 (ALARM_TYPE_VOLTAGE_HIGH)");
		break;
	}
}

void CLS_DNVRAlmSchPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_TYPE,IDS_CONFIG_DNVR_ALMLINK_TYPE);
	SetDlgItemText(IDC_STATIC_INPUTSCHEDULE,GetTextByLan(_T("通用模板"), _T("General template")));
	SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMLINK_INPORT);
	SetDlgItemTextEx(IDC_CHECK_INPORT,IDS_CONFIG_FTP_SNAPSHOT_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_WEEKDAY,IDS_CONFIG_DNVR_ALMSCH_WEEKDAY);
	SetDlgItemTextEx(IDC_STATIC_TIME1,IDS_CONFIG_DNVR_ALMSCH_TIME1);
	SetDlgItemTextEx(IDC_STATIC_TIME2,IDS_CONFIG_DNVR_ALMSCH_TIME2);
	SetDlgItemTextEx(IDC_STATIC_TIME3,IDS_CONFIG_DNVR_ALMSCH_TIME3);
	SetDlgItemTextEx(IDC_STATIC_TIME4,IDS_CONFIG_DNVR_ALMSCH_TIME4);
	SetDlgItemTextEx(IDC_BUTTON_ALLDAY,IDS_CONFIG_DNVR_ALMSCH_ALLDAY);
	SetDlgItemTextEx(IDC_BUTTON_INPORT,IDS_SET);
	SetDlgItemText(IDC_STATIC_OUTPUTSCHEDULE,GetTextByLan(_T("报警输出模板"), _T("Alarm output template")));
	SetDlgItemTextEx(IDC_CHECK_OUTPORT,IDS_CONFIG_FTP_SNAPSHOT_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_OUTPORT,IDS_CONFIG_DNVR_ALMSCH_OUTPORT);
	SetDlgItemTextEx(IDC_STATIC_OUT_WEEKDAY,IDS_CONFIG_DNVR_ALMSCH_WEEKDAY);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME1,IDS_CONFIG_DNVR_ALMSCH_TIME1);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME2,IDS_CONFIG_DNVR_ALMSCH_TIME2);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME3,IDS_CONFIG_DNVR_ALMSCH_TIME3);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME4,IDS_CONFIG_DNVR_ALMSCH_TIME4);
	SetDlgItemTextEx(IDC_BUTTON_OUT_ALLDAY,IDS_CONFIG_DNVR_ALMSCH_ALLDAY);
	SetDlgItemTextEx(IDC_BUTTON_OUTPORT,IDS_SET);
	SetDlgItemText(IDC_STATIC_DEVTYPE,GetTextByLan(_T("设备类型"), _T("DeviceType")));
	SetDlgItemText(IDC_STATIC_ADD_PARA1,GetTextByLan(_T("附加参数1"), _T("AdditionalPara1")));
	SetDlgItemText(IDC_STATIC_ADD_PARA2,GetTextByLan(_T("附加参数2"), _T("AdditionalPara2")));


	if (1 == m_iChangeLanguage)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_INPORT);
	}
	else if (0 == m_iChangeLanguage)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_CHANNELNO);
	}
	InsertString(m_cboType, PORT_ALARM_SCHEDULE, IDS_CONFIG_DNVR_ALMSCH_PORTALARM);
	InsertString(m_cboType, VIDEO_LOST_SCHEDULE, IDS_CONFIG_DNVR_ALMSCH_VIDEOLOST);
	InsertString(m_cboType, VIDEO_MOTION_SCHEDULE, IDS_CONFIG_DNVR_ALMSCH_VIDEOMOTION);
	InsertString(m_cboType, VIDEO_COVER_SCHEDULE, IDS_CONFIG_DNVR_ALMSCH_VIDEOCOVER);
	InsertString(m_cboType, AUDIO_LOST_SCHEDULE, IDS_CONFIG_DNVR_ALMSCH_AUDIOLOST);
	InsertString(m_cboType, WATER_RAIN_SCHEDULE, IDS_CONFIG_DNVR_ALMSCH_RAINFALL);
	InsertString(m_cboType, WATER_ALERT_WATER_SCHEDULE, IDS_CONFIG_DNVR_ALMSCH_ALERTWATER);
	InsertString(m_cboType, DANGEROUS_AREA_SCHEDULE, GetTextByLan(_T("危险区域报警模板"), _T("Danger area alarm template")));
	InsertString(m_cboType, VOLTAGE_AREA_SCHEDULE, GetTextByLan(_T("电压报警模板"), _T("Voltage area alarm template")));
	InsertString(m_cboType, BLACK_BODY_ERR_SCHEDULE, GetTextByLan(_T("黑体异常报警模板"), _T("Black body err alarm template")));
	InsertString(m_cboType, SNAP_ONTIME_SCHEDULE, GetTextByLan(_T("定时抓拍模板"), _T("Snap On Time Schedule")));
	InsertString(m_cboType, VEHICLE_IDENTIFY_SCHEDULE, GetTextByLan(_T("车辆识别模板"), _T("Vehicle identify Schedule")));
	
	InsertString(m_cboWeekday,0,IDS_CONFIG_SUNDAY);
	InsertString(m_cboWeekday,1,IDS_CONFIG_MONDAY);
	InsertString(m_cboWeekday,2,IDS_CONFIG_TUESDAY);
	InsertString(m_cboWeekday,3,IDS_CONFIG_WEDNESDAY);
	InsertString(m_cboWeekday,4,IDS_CONFIG_THURSDAY);
	InsertString(m_cboWeekday,5,IDS_CONFIG_FRIDAY);
	InsertString(m_cboWeekday,6,IDS_CONFIG_SATURDAY);

	InsertString(m_cboOutWeekday,0,IDS_CONFIG_SUNDAY);
	InsertString(m_cboOutWeekday,1,IDS_CONFIG_MONDAY);
	InsertString(m_cboOutWeekday,2,IDS_CONFIG_TUESDAY);
	InsertString(m_cboOutWeekday,3,IDS_CONFIG_WEDNESDAY);
	InsertString(m_cboOutWeekday,4,IDS_CONFIG_THURSDAY);
	InsertString(m_cboOutWeekday,5,IDS_CONFIG_FRIDAY);
	InsertString(m_cboOutWeekday,6,IDS_CONFIG_SATURDAY);
	InsertString(m_cboDevType,0,_T("IPC"));
	InsertString(m_cboDevType,1,_T("NVR"));

	InsertString(m_cboAlarmSubType, 0, GetTextByLan(_T("车牌比对报警"), _T("License Plate Compare Alarm")));
	InsertString(m_cboAlarmSubType, 1, GetTextByLan(_T("陌生车牌报警"), _T("Strange License Plate Alarm")));

	FuncAbilityLevel tAbilityLevel = {0};
	tAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	tAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	tAbilityLevel.iSubFuncType = 178;
	int iRetBytes = -1;
	int iChannelNo = m_iChannelNo < 0 ? 0 : m_iChannelNo;
	int iPlateLibCount = 10;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, iChannelNo, &tAbilityLevel, sizeof(tAbilityLevel), &iRetBytes);
	if (RET_SUCCESS == iRet) {
		iPlateLibCount = atoi(tAbilityLevel.cParam);
	}
	if (iPlateLibCount <= 0) {
		iPlateLibCount = 10;
	}
	for (int i = 0; i < iPlateLibCount; ++i)
	{
		CString strLibId;
		strLibId.Format("%d", i + 1);
		InsertString(m_cboPlateLibId, i, strLibId);
	}
}

BOOL CLS_DNVRAlmSchPage::UI_UpdateType()
{
	if (m_iLogonID < 0)
		return FALSE;

	int iAlarmInPortNum = -1;
	int iAlarmOutPortNum = -1;
	int iChannelNum = -1;
	int iRet;
	if (PORT_ALARM_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iAlarmInPortNum, &iAlarmOutPortNum);
		if (0 == iRet)
		{
			m_iChangeLanguage = 1;
			if (m_cboInPort.GetCount() != iAlarmInPortNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iAlarmInPortNum; i++)
				{
					CString strInPort;
					strInPort.Format("%d",i);
					m_cboInPort.AddString(strInPort);
				}
			}
			if (m_cboOutPort.GetCount() != iAlarmOutPortNum)
			{
				m_cboOutPort.ResetContent();
				for (int i=0; i<iAlarmOutPortNum; i++)
				{
					CString strOutPort;
					strOutPort.Format("%d",i);
					m_cboOutPort.AddString(strOutPort);
				}
			}
			m_cboInPort.SetCurSel(0);
			m_cboOutPort.SetCurSel(0);
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
	}
	else
	{
		iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
		if (0 == iRet)
		{
			m_iChangeLanguage = 0;
			if (m_cboInPort.GetCount() != iAlarmInPortNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iChannelNum; i++)
				{
					CString strInPort;
					strInPort.Format("%d",i);
					m_cboInPort.AddString(strInPort);
				}
				m_cboInPort.SetCurSel(0);
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
	}
	
	SYSTEMTIME sysTime;
	GetLocalTime(&sysTime);
	m_cboWeekday.SetCurSel(sysTime.wDayOfWeek);
	m_cboOutWeekday.SetCurSel(sysTime.wDayOfWeek);

	return TRUE;
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboType()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	if(m_cboDevType.IsWindowVisible())
	{
		m_cboDevType.ShowWindow(FALSE);
		GetDlgItem(IDC_STATIC_DEVTYPE)->ShowWindow(FALSE);
	}

	GetDlgItem(IDC_COMBO_INPORT)->EnableWindow(TRUE);
	GetDlgItem(IDC_CHECK_INPORT)->EnableWindow(TRUE);

	int iAlarmInPortNum = -1;
	int iAlarmOutPortNum = -1;
	int iChannelNum = -1;
	int iRet;
	if (PORT_ALARM_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iAlarmInPortNum, &iAlarmOutPortNum);
		if (0 == iRet)
		{
			m_iChangeLanguage = 1;
			if (m_cboInPort.GetCount() != iAlarmInPortNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iAlarmInPortNum; i++)
				{
					CString strInPort;
					strInPort.Format("%d",i);
					m_cboInPort.AddString(strInPort);
				}
			}
			if (m_cboOutPort.GetCount() != iAlarmOutPortNum)
			{
				m_cboOutPort.ResetContent();
				for (int i=0; i<iAlarmOutPortNum; i++)
				{
					CString strOutPort;
					strOutPort.Format("%d",i);
					m_cboOutPort.AddString(strOutPort);
				}
			}
			m_cboInPort.SetCurSel(0);
			m_cboOutPort.SetCurSel(0);
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
	}
	else if(SNAP_ONTIME_SCHEDULE == m_cboType.GetCurSel())
	{
		if(!m_cboDevType.IsWindowVisible())
		{
			m_cboDevType.ShowWindow(TRUE);
			GetDlgItem(IDC_STATIC_DEVTYPE)->ShowWindow(TRUE);
		}

		GetDlgItem(IDC_COMBO_INPORT)->EnableWindow(FALSE);
		GetDlgItem(IDC_CHECK_INPORT)->EnableWindow(FALSE);
		LocalSnapSchedule tInfo = {0};
		tInfo.iSize = (int)sizeof(LocalSnapSchedule);
		tInfo.iWeekDay = m_cboWeekday.GetCurSel();
		tInfo.iDevType = m_cboDevType.GetCurSel();
		NVS_SCHEDTIME timeSeg[4];
		NVS_SCHEDTIME *schedule[] = {&timeSeg[0],&timeSeg[1],&timeSeg[2],&timeSeg[3]};
		int iByteReturn = -1;
		int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_LOCALSNAP_SCHEDULE, m_iChannelNo, &tInfo, tInfo.iSize, &iByteReturn);
		if (RET_SUCCESS == iRet)
		{
			SYSTEMTIME sysTime = {0};
			GetLocalTime(&sysTime);
			for (int i=0; i<4; i++)
			{
				sysTime.wHour = tInfo.tTimeSeg[i].iStartHour;
				sysTime.wMinute = tInfo.tTimeSeg[i].iStartMin;
				sysTime.wSecond = 0;
				m_dtcStartTime[i].SetTime(&sysTime);
			}
			for (int i=0; i<4; i++)
			{
				sysTime.wHour = tInfo.tTimeSeg[i].iStopHour;
				sysTime.wMinute = tInfo.tTimeSeg[i].iStopMin;
				sysTime.wSecond = 0;
				m_dtcEndTime[i].SetTime(&sysTime);
				m_chkTime[i].SetCheck(tInfo.tTimeSeg[i].iRecordMode);
				if (tInfo.tTimeSeg[i].iRecordMode)
				{
					m_dtcStartTime[i].EnableWindow(TRUE);
					m_dtcEndTime[i].EnableWindow(TRUE);
				}
				else
				{
					m_dtcStartTime[i].EnableWindow(FALSE);
					m_dtcEndTime[i].EnableWindow(FALSE );
				}
			}	
		}
		return;

	}
	else
	{
		iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
		if (0 == iRet)
		{
			m_iChangeLanguage = 0;
			if (m_cboInPort.GetCount() != iChannelNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iChannelNum; i++)
				{
					CString strInPort;
					strInPort.Format("%d",i);
					m_cboInPort.AddString(strInPort);
				}
				m_cboInPort.SetCurSel(0);
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
	}
	if (1 == m_iChangeLanguage)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_INPORT);
	}
	else if (0 == m_iChangeLanguage)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_CHANNELNO);
	}

	OnCbnSelchangeComboInport();
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckInport()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iChannelNum = 0;
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
	}
	int iPortNo = 0;
	int iEnable = 0;
	int iMaxPortCount = iChannelNum;            //  The maximum number of output ports will not exceed the number of channels
	iPortNo = m_cboInPort.GetCurSel();
	if (PORT_ALARM_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetInportEnable(m_iLogonID, iPortNo, &iEnable);
		if (0 == iRet)
		{
			if (m_chkInPort.GetCheck() != iEnable)
			{
				NetClient_SetInportEnable(m_iLogonID, iPortNo, m_chkInPort.GetCheck());
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetInportEnable (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetInportEnable (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
	}
	else if (VIDEO_LOST_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetAlarmVideoLost(m_iLogonID, iPortNo, &iEnable);
		if (0 == iRet)
		{
			if (m_chkInPort.GetCheck() != iEnable)
			{
				NetClient_SetAlarmVideoLost(m_iLogonID, iPortNo, m_chkInPort.GetCheck());
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlarmVideoLost (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmVideoLost (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
	}
	else if (VIDEO_MOTION_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetMotionDetection(m_iLogonID, iPortNo, &iEnable);
		if (0 == iRet)
		{
			if (m_chkInPort.GetCheck() != iEnable)
			{
				NetClient_SetMotionDetection(m_iLogonID, iPortNo, m_chkInPort.GetCheck());
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetMotionDetection (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetMotionDetection (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
	}
	else if (VIDEO_COVER_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetAlmVideoCov(m_iLogonID, iPortNo, &iEnable);
		if (0 == iRet)
		{
			if (m_chkInPort.GetCheck() != iEnable)
			{
				NetClient_SetAlmVideoCov(m_iLogonID, iPortNo, m_chkInPort.GetCheck());
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlmVideoCov (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlmVideoCov (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
		}
	}
	else if (AUDIO_LOST_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam ScheEnableParam = {0};
		ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		ScheEnableParam.iEnable = m_chkInPort.GetCheck();
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_AUDIOLOST, CMD_SET_ALARMSCH_ENABLE, &ScheEnableParam);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
	}
	else if (WATER_RAIN_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam ScheEnableParam = {0};
		ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		ScheEnableParam.iEnable = m_chkInPort.GetCheck();
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_RAINFALL, CMD_SET_ALARMSCH_ENABLE, &ScheEnableParam);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
	}
	else if (WATER_ALERT_WATER_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam ScheEnableParam = {0};
		ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		ScheEnableParam.iEnable = m_chkInPort.GetCheck();
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_ALERT_WATER_LEVEL, CMD_SET_ALARMSCH_ENABLE, &ScheEnableParam);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
	}
	else if (DANGEROUS_AREA_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam ScheEnableParam = {0};
		ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		ScheEnableParam.iEnable = m_chkInPort.GetCheck();
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_DANGEROUS_AREA, CMD_ALARMSCH_ENABLE_EX, &ScheEnableParam);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
	}
	else if (VOLTAGE_AREA_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam ScheEnableParam = {0};
		ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		ScheEnableParam.iEnable = m_chkInPort.GetCheck();

		iRet = NetClient_SetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_VOLTAGE_HIGH, CMD_ALARMSCH_ENABLE_EX, &ScheEnableParam);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
	}
	else if (BLACK_BODY_ERR_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam ScheEnableParam = {0};
		ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		ScheEnableParam.iEnable = m_chkInPort.GetCheck();
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_BLACK_BODY_DETECT, CMD_ALARMSCH_ENABLE_EX, &ScheEnableParam);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,iPortNo);
		}
	} else if (VEHICLE_IDENTIFY_SCHEDULE == m_cboType.GetCurSel()) {
		TAlarmScheEnableParam ScheEnableParam = {0};
		ScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		ScheEnableParam.iEnable = m_chkInPort.GetCheck();
		ScheEnableParam.iParam1 = m_cboAlarmSubType.GetCurSel() + 1;
		if (2 == ScheEnableParam.iParam1) {
			ScheEnableParam.iParam2 = 0;
		} else {
			ScheEnableParam.iParam2 = m_cboPlateLibId.GetCurSel() + 1;
		}
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_VEHICLE_IDENTIFICATION, CMD_ALARMSCH_ENABLE_EX, &ScheEnableParam);
		if (0 == iRet) {
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig:ALARM_TYPE_VEHICLE_IDENTIFICATION:CMD_ALARMSCH_ENABLE_EX (%d,%d)",m_iLogonID,iPortNo);
		} else {
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig:ALARM_TYPE_VEHICLE_IDENTIFICATION:CMD_ALARMSCH_ENABLE_EX (%d,%d)",m_iLogonID,iPortNo);
		}
	}
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboWeekday()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iChannelNo = 0;
	int iPortNo = 0;
	iChannelNo = m_cboInPort.GetCurSel();
	iPortNo = m_cboInPort.GetCurSel();
	int iWeekday = m_cboWeekday.GetCurSel();
	int iRet = 0;
	int iEnable = 0;
	NVS_SCHEDTIME timeSeg[4];
	SYSTEMTIME sysTime = {0};
	GetSystemTime(&sysTime);

	for (int i=0; i<4; i++)
	{
		memset(&timeSeg[i],0,sizeof(NVS_SCHEDTIME));
	}
	NVS_SCHEDTIME *schedule[] = {&timeSeg[0],&timeSeg[1],&timeSeg[2],&timeSeg[3]};
	if (PORT_ALARM_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetInportAlarmSchedule(m_iLogonID,iPortNo,iWeekday,schedule);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetInportAlarmSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetInportAlarmSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
	}
	else if (VIDEO_LOST_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetVideoLostSchedule(m_iLogonID,iChannelNo,iWeekday,schedule);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetVideoLostSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetVideoLostSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
	}
	else if (VIDEO_MOTION_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetMotionDetectSchedule(m_iLogonID,iChannelNo,iWeekday,schedule);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetMotionDetectSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetMotionDetectSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
	}
	else if (VIDEO_COVER_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetVideoCoverSchedule(m_iLogonID,iChannelNo,iWeekday,schedule);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetVideoCoverSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetVideoCoverSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
		}
	}
	else if (AUDIO_LOST_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheduleParam alarmParam = {0};
		alarmParam.iBuffSize = sizeof(alarmParam);
		alarmParam.iWeekday = iWeekday;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_AUDIOLOST, CMD_GET_ALARMSCHEDULE, &alarmParam);
		if(iRet >= 0)
		{
			for(int i = 0; i < MAX_TIMESEGMENT; i++)
				*schedule[i] = alarmParam.timeSeg[iWeekday][i];
		}
	}
	else if (WATER_RAIN_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheduleParam alarmParam = {0};
		alarmParam.iBuffSize = sizeof(alarmParam);
		alarmParam.iWeekday = iWeekday;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_RAINFALL, CMD_GET_ALARMSCHEDULE, &alarmParam);
		if(iRet >= 0)
		{
			for(int i = 0; i < MAX_TIMESEGMENT; i++)
				*schedule[i] = alarmParam.timeSeg[iWeekday][i];
		}
	}
	else if (WATER_ALERT_WATER_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheduleParam alarmParam = {0};
		alarmParam.iBuffSize = sizeof(alarmParam);
		alarmParam.iWeekday = iWeekday;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_ALERT_WATER_LEVEL, CMD_GET_ALARMSCHEDULE, &alarmParam);
		if(iRet >= 0)
		{
			for(int i = 0; i < MAX_TIMESEGMENT; i++)
				*schedule[i] = alarmParam.timeSeg[iWeekday][i];
		}
	}
	else if (DANGEROUS_AREA_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheduleParam alarmParam = {0};
		alarmParam.iBuffSize = sizeof(alarmParam);
		alarmParam.iWeekday = iWeekday;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_DANGEROUS_AREA, CMD_GET_ALARMSCHEDULE, &alarmParam);
		if(iRet >= 0)
		{
			for(int i = 0; i < MAX_TIMESEGMENT; i++)
				*schedule[i] = alarmParam.timeSeg[iWeekday][i];
		}
	}
	else if (BLACK_BODY_ERR_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheduleParam alarmParam = {0};
		alarmParam.iBuffSize = sizeof(alarmParam);
		alarmParam.iWeekday = iWeekday;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_BLACK_BODY_DETECT, CMD_GET_ALARMSCHEDULE, &alarmParam);
		if(iRet >= 0)
		{
			for(int i = 0; i < MAX_TIMESEGMENT; i++)
				*schedule[i] = alarmParam.timeSeg[iWeekday][i];
		}
	}
	else if (VEHICLE_IDENTIFY_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheduleParam alarmParam = {0};
		alarmParam.iBuffSize = sizeof(alarmParam);
		alarmParam.iWeekday = iWeekday;
		alarmParam.iParam1 = m_cboAlarmSubType.GetCurSel() + 1;
		alarmParam.iParam2 = m_cboPlateLibId.GetCurSel() + 1;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_VEHICLE_IDENTIFICATION, CMD_GET_ALARMSCHEDULE, &alarmParam);
		if(iRet >= 0)
		{
			for(int i = 0; i < MAX_TIMESEGMENT; i++) {
				*schedule[i] = alarmParam.timeSeg[iWeekday][i];
			}
		}
	}
	else if (SNAP_ONTIME_SCHEDULE == m_cboType.GetCurSel())
	{
		LocalSnapSchedule tInfo = {0};
		tInfo.iSize = (int)sizeof(LocalSnapSchedule);
		tInfo.iWeekDay = m_cboWeekday.GetCurSel();
		tInfo.iDevType = m_cboDevType.GetCurSel();
		NVS_SCHEDTIME timeSeg[4];
		NVS_SCHEDTIME *schedule[] = {&timeSeg[0],&timeSeg[1],&timeSeg[2],&timeSeg[3]};
		int iByteReturn = -1;
		iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_LOCALSNAP_SCHEDULE, m_iChannelNo, &tInfo, tInfo.iSize, &iByteReturn);
		if (iRet == 0)
		{
			for (int i=0; i<4; i++)
			{
				sysTime.wHour = tInfo.tTimeSeg[i].iStartHour;
				sysTime.wMinute = tInfo.tTimeSeg[i].iStartMin;
				sysTime.wSecond = 0;
				m_dtcStartTime[i].SetTime(&sysTime);
			}
			for (int i=0; i<4; i++)
			{
				sysTime.wHour = tInfo.tTimeSeg[i].iStopHour;
				sysTime.wMinute = tInfo.tTimeSeg[i].iStopMin;
				sysTime.wSecond = 0;
				m_dtcEndTime[i].SetTime(&sysTime);
				m_chkTime[i].SetCheck(tInfo.tTimeSeg[i].iRecordMode);
				if (tInfo.tTimeSeg[i].iRecordMode)
				{
					m_dtcStartTime[i].EnableWindow(TRUE);
					m_dtcEndTime[i].EnableWindow(TRUE);
				}
				else
				{
					m_dtcStartTime[i].EnableWindow(FALSE);
					m_dtcEndTime[i].EnableWindow(FALSE );
				}
			}
		}
		return;
	}
	if(iRet == 0)
	{
		for (int i=0; i<4; i++)
		{
			sysTime.wHour = timeSeg[i].iStartHour;
			sysTime.wMinute = timeSeg[i].iStartMin;
			sysTime.wSecond = 0;
			m_dtcStartTime[i].SetTime(&sysTime);
		}
		for (int i=0; i<4; i++)
		{
			sysTime.wHour = timeSeg[i].iStopHour;
			sysTime.wMinute = timeSeg[i].iStopMin;
			sysTime.wSecond = 0;
			m_dtcEndTime[i].SetTime(&sysTime);
			m_chkTime[i].SetCheck(timeSeg[i].iRecordMode);
			if (timeSeg[i].iRecordMode)
			{
				m_dtcStartTime[i].EnableWindow(TRUE);
				m_dtcEndTime[i].EnableWindow(TRUE);
			}
			else
			{
				m_dtcStartTime[i].EnableWindow(FALSE);
				m_dtcEndTime[i].EnableWindow(FALSE );
			}
		}
	}
}

void CLS_DNVRAlmSchPage::CheckSchedtime(int _iIndex)
{
	if(BST_CHECKED == m_chkTime[_iIndex].GetCheck())
	{
		m_dtcStartTime[_iIndex].EnableWindow(true);
		m_dtcEndTime[_iIndex].EnableWindow(true);
	}
	else
	{
		m_dtcStartTime[_iIndex].EnableWindow(false);
		m_dtcEndTime[_iIndex].EnableWindow(false);
		CTime timeNow = CTime::GetCurrentTime();
		timeNow = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(),0, 0, 0);
		m_dtcStartTime[_iIndex].SetTime(&timeNow);
		m_dtcEndTime[_iIndex].SetTime(&timeNow);
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckTime1()
{
	CheckSchedtime(0);
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckTime2()
{
	CheckSchedtime(1);
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckTime3()
{
	CheckSchedtime(2);
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckTime4()
{
	CheckSchedtime(3);
}

void ShowMessage( int _iMsgID)
{
	MessageBox(GetFocus(),GetTextEx(_iMsgID),_T(""),MB_TOPMOST|MB_ICONWARNING);	
}

void CLS_DNVRAlmSchPage::OnBnClickedButtonAllday()
{
	//int iCurDay = m_cboWeekday.GetCurSel();
	//for (int i=0; i<7; i++)
	//{
	//	m_cboWeekday.SetCurSel(i);
	//	OnBnClickedButtonInport();
	//}
	//m_cboWeekday.SetCurSel(iCurDay);
	// TODO: Add your control notification handler code here
	CTime timeStart[MAX_TIMESEGMENT],timeStop[MAX_TIMESEGMENT];
	int iRecordMode[MAX_TIMESEGMENT];

	for (int i = 0;i < 4;i ++)
	{
		m_dtcStartTime[i].GetTime(timeStart[i]);
		m_dtcEndTime[i].GetTime(timeStop[i]);
		iRecordMode[i] = m_chkTime[i].GetCheck();		
	}

	for (int i = 0;i < 4;i ++)
	{
		if (iRecordMode[i] && timeStart[i] >= timeStop[i])
		{
			ShowMessage(IDS_MSG_ALARM_TIME);
			return;
		}
	}

	for (int i = 0; i < MAX_TIMESEGMENT; ++i)
	{
		for (int j = i+1; j < MAX_TIMESEGMENT; ++j)
		{
			if (iRecordMode[i] && iRecordMode[j])
			{				
				if (timeStop[i] > timeStart[j] && timeStart[i] < timeStop[j]
				||timeStop[j] > timeStart[i] && timeStart[j] < timeStop[i])
				{
					ShowMessage(IDS_MSG_ALARM_TIMESPAN);
					return;
				}		
			}			
		}
	}

	//int iDayOfWeek=m_cboWeekday.GetCurSel();
	NVS_SCHEDTIME schedtime[MAX_TIMESEGMENT];	
	for (int i = 0;i < 4;i ++)
	{
		schedtime[i].iRecordMode = iRecordMode[i];		
		schedtime[i].iStartHour = timeStart[i].GetHour();
		schedtime[i].iStartMin = timeStart[i].GetMinute();
		schedtime[i].iStopHour = timeStop[i].GetHour();
		schedtime[i].iStopMin = timeStop[i].GetMinute();
	}

	PNVS_SCHEDTIME pSchedtime[MAX_TIMESEGMENT] = {&schedtime[0],&schedtime[1],&schedtime[2],&schedtime[3]};
	
	int iArlarmType = m_cboType.GetCurSel();
	int iPortNo = m_cboInPort.GetCurSel();
	int iChannelNo = m_cboInPort.GetCurSel();
	int iRet = 1;
	
	for (int iDays=0; iDays< MAX_DAYS; iDays++)
	{
		TAlarmScheduleParam alarmParam = {0};
		alarmParam.iBuffSize = sizeof(alarmParam);
		alarmParam.iWeekday = iDays;
		for(int i = 0; i < MAX_TIMESEGMENT; i++)
		{
			alarmParam.timeSeg[iDays][i] = *pSchedtime[i];
		}
		
		if (PORT_ALARM_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetInportAlarmSchedule(m_iLogonID, iPortNo, iDays, pSchedtime);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetInportAlarmSchedule(%d,%d,%d)", m_iLogonID, iPortNo, iDays);
			}
		} else if (VIDEO_LOST_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetVideoLostSchedule(m_iLogonID,iChannelNo,iDays,pSchedtime);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetVideoLostSchedule(%d,%d,%d)",m_iLogonID, iChannelNo, iDays);
			}
		} else if (VIDEO_MOTION_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetMotionDetectSchedule(m_iLogonID,iChannelNo,iDays,pSchedtime);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetMotionDetectSchedule(%d,%d,%d)",m_iLogonID, iChannelNo, iDays);
			}
		} else if (VIDEO_COVER_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetVideoCoverSchedule(m_iLogonID,iChannelNo,iDays,pSchedtime);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetVideoCoverSchedule(%d,%d,%d)",m_iLogonID, iChannelNo, iDays);
			}
		} else if (AUDIO_LOST_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_AUDIOLOST, CMD_SET_ALARMSCHEDULE, &alarmParam);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig:ALARM_TYPE_AUDIOLOST:CMD_SET_ALARMSCHEDULE(%d,%d,%d)"
					, m_iLogonID, iChannelNo, iDays);
			}
		} else if (WATER_RAIN_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_RAINFALL, CMD_SET_ALARMSCHEDULE, &alarmParam);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig:ALARM_TYPE_RAINFALL:CMD_SET_ALARMSCHEDULE(%d,%d,%d)"
					, m_iLogonID, iChannelNo, iDays);
			}
		} else if (WATER_ALERT_WATER_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_ALERT_WATER_LEVEL, CMD_SET_ALARMSCHEDULE, &alarmParam);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig:ALARM_TYPE_ALERT_WATER_LEVEL:CMD_SET_ALARMSCHEDULE(%d,%d,%d)"
					, m_iLogonID, iChannelNo, iDays);
			}
		} else if (DANGEROUS_AREA_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_DANGEROUS_AREA, CMD_SET_ALARMSCHEDULE, &alarmParam);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig:ALARM_TYPE_DANGEROUS_AREA:CMD_SET_ALARMSCHEDULE(%d,%d,%d)"
					, m_iLogonID, iChannelNo, iDays);
			}
		} else if (BLACK_BODY_ERR_SCHEDULE == iArlarmType) {
			iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_BLACK_BODY_DETECT, CMD_SET_ALARMSCHEDULE, &alarmParam);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig:ALARM_TYPE_BLACK_BODY_DETECT:CMD_SET_ALARMSCHEDULE(%d,%d,%d)"
					, m_iLogonID, iChannelNo, iDays);
			}
		} else if (VEHICLE_IDENTIFY_SCHEDULE == iArlarmType) {
			alarmParam.iParam1 = m_cboAlarmSubType.GetCurSel() + 1;
			alarmParam.iParam2 = m_cboPlateLibId.GetCurSel() + 1;
			if (2 == alarmParam.iParam1) {
				alarmParam.iParam2 = 0;
			} else {
				alarmParam.iParam2 = m_cboPlateLibId.GetCurSel() + 1;
			}
			iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_VEHICLE_IDENTIFICATION, CMD_SET_ALARMSCHEDULE, &alarmParam);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig:ALARM_TYPE_VEHICLE_IDENTIFICATION:CMD_SET_ALARMSCHEDULE(%d,%d,%d)"
					, m_iLogonID, iChannelNo, iDays);
			}
		} else if (SNAP_ONTIME_SCHEDULE == iArlarmType) {
			LocalSnapSchedule tInfo = {0};
			tInfo.iSize = (int)sizeof(LocalSnapSchedule);
			tInfo.iWeekDay = iDays;
			for(int i = 0; i < MAX_TIMESEGMENT; i++)
				tInfo.tTimeSeg[i] = *pSchedtime[i];
			tInfo.iDevType = m_cboDevType.GetCurSel();
			iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_LOCALSNAP_SCHEDULE, m_iChannelNo, &tInfo, tInfo.iSize);
			if (RET_FAILED == iRet) {
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig:NET_CLIENT_LOCALSNAP_SCHEDULE(%d,%d,%d)",m_iLogonID, iChannelNo, iDays);
			}
		}
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedButtonInport()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iChannelNo = 0;
	int iPortNo = 0;
	int iWeekday = m_cboWeekday.GetCurSel();
	int iRet = 0;
	NVS_SCHEDTIME timeSeg[4];
	SYSTEMTIME sysTime = {0};
	for (int i=0; i<4; i++)
	{
		memset(&timeSeg[i],0,sizeof(NVS_SCHEDTIME));
		m_dtcStartTime[i].GetTime(&sysTime);
		timeSeg[i].iStartHour = sysTime.wHour;
		timeSeg[i].iStartMin = sysTime.wMinute;
		
		m_dtcEndTime[i].GetTime(&sysTime);
		timeSeg[i].iStopHour = sysTime.wHour;
		timeSeg[i].iStopMin = sysTime.wMinute;
		timeSeg[i].iRecordMode = m_chkTime[i].GetCheck();
		if (FALSE == m_chkTime[i].GetCheck())
		{
			memset(&timeSeg[i],0,sizeof(NVS_SCHEDTIME));
		}
		int iStart = timeSeg[i].iStartHour *60 + timeSeg[i].iStartMin;
		int iStop = timeSeg[i].iStopHour *60 + timeSeg[i].iStopMin;
		if (iStart > iStop) {
			AddLog(LOG_TYPE_MSG,"","time range %d(%2d:%2d - %2d:%2d)Start time must be earlier than end time! "
				,i,timeSeg[i].iStartHour,timeSeg[i].iStartMin,timeSeg[i].iStopHour,timeSeg[i].iStopMin);
			return;
		}
	}

	NVS_SCHEDTIME* schedule[] = {&timeSeg[0],&timeSeg[1],&timeSeg[2],&timeSeg[3]};
	int iStarti = 0;
	int iStopi = 0;
	int iStartj = 0;
	int iStopj = 0;
	for (int i = 0; i < MAX_TIMESEGMENT; ++i)
	{
		for(int j = 0; j < MAX_TIMESEGMENT; ++j)
		{
			iStarti = schedule[i]->iStartHour*60+schedule[i]->iStartMin;
			iStopi = schedule[i]->iStopHour*60+schedule[i]->iStopMin;
			iStartj = schedule[j]->iStartHour*60+schedule[j]->iStartMin;
			iStopj = schedule[j]->iStopHour*60+schedule[j]->iStopMin;
			if (i == j || 0 == (iStarti+iStopi) || 0 == (iStartj+iStopi)) {
				continue;
			}

			if (iStarti >= iStopi 
				|| iStarti >= iStartj && iStarti < iStopj 
				|| iStopi > iStartj && iStopi <= iStopj) {
				AddLog(LOG_TYPE_MSG,"","time range %d(%2d:%2d - %2d:%2d) collide with time range %d(%2d:%2d - %2d:%2d)"
					,i,schedule[i]->iStartHour,schedule[i]->iStartMin,schedule[i]->iStopHour,schedule[i]->iStopMin
					,j,schedule[j]->iStartHour,schedule[j]->iStartMin,schedule[j]->iStopHour,schedule[j]->iStopMin);
				return;
			}
		}
	}

	iChannelNo = m_cboInPort.GetCurSel();
	iPortNo = m_cboInPort.GetCurSel();
	TAlarmScheduleParam alarmParam = {0};
	alarmParam.iBuffSize = sizeof(alarmParam);
	alarmParam.iWeekday = iWeekday;
	for(int i = 0; i < MAX_TIMESEGMENT; i++)
	{
		alarmParam.timeSeg[iWeekday][i] = *schedule[i];
	}

	if (PORT_ALARM_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetInportAlarmSchedule(m_iLogonID,iPortNo,iWeekday,schedule);
	} else if (VIDEO_LOST_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetVideoLostSchedule(m_iLogonID,iChannelNo,iWeekday,schedule);
	} else if (VIDEO_MOTION_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetMotionDetectSchedule(m_iLogonID,iChannelNo,iWeekday,schedule);
	} else if (VIDEO_COVER_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetVideoCoverSchedule(m_iLogonID,iChannelNo,iWeekday,schedule);
	} else if (SNAP_ONTIME_SCHEDULE == m_cboType.GetCurSel()) {
		LocalSnapSchedule tInfo = {0};
		tInfo.iSize = (int)sizeof(LocalSnapSchedule);
		tInfo.iWeekDay = iWeekday;
		for(int i = 0; i < MAX_TIMESEGMENT; i++)
			tInfo.tTimeSeg[i] = *schedule[i];
		tInfo.iDevType = m_cboDevType.GetCurSel();
		iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_LOCALSNAP_SCHEDULE, m_iChannelNo, &tInfo, tInfo.iSize);
	} else if (AUDIO_LOST_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_AUDIOLOST, CMD_SET_ALARMSCHEDULE, &alarmParam);
	} else if (WATER_RAIN_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_RAINFALL, CMD_SET_ALARMSCHEDULE, &alarmParam);
	} else if (WATER_ALERT_WATER_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_ALERT_WATER_LEVEL, CMD_SET_ALARMSCHEDULE, &alarmParam);
	} else if (DANGEROUS_AREA_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_DANGEROUS_AREA, CMD_SET_ALARMSCHEDULE, &alarmParam);
	} else if (BLACK_BODY_ERR_SCHEDULE == m_cboType.GetCurSel()) {
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_BLACK_BODY_DETECT, CMD_SET_ALARMSCHEDULE, &alarmParam);
	} else if (VEHICLE_IDENTIFY_SCHEDULE == m_cboType.GetCurSel()) {
		alarmParam.iParam1 = m_cboAlarmSubType.GetCurSel() + 1;
		alarmParam.iParam2 = m_cboPlateLibId.GetCurSel() + 1;
		if (2 == alarmParam.iParam1) {
			alarmParam.iParam2 = 0;
		} else {
			alarmParam.iParam2 = m_cboPlateLibId.GetCurSel() + 1;
		}
		iRet = NetClient_SetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_VEHICLE_IDENTIFICATION, CMD_SET_ALARMSCHEDULE, &alarmParam);
	}
	
	if(iRet < 0)
	{
		MessageBox(GetTextEx(IDS_CONFIG_DNVR_ALMSCH_MESSAGESET), GetTextEx(IDS_CONFIG_PROMPT), MB_OK);
		m_cboWeekday.SetCurSel(0);
	}
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboOutport()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iEnable = 0;
	int iPortNo = GetDlgItemInt(IDC_COMBO_OUTPORT);
	int iRet = NetClient_GetOutportEnable(m_iLogonID, iPortNo, &iEnable);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetOutportEnable (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetOutportEnable (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
	}
	m_chkOutPort.SetCheck(iEnable?BST_CHECKED:BST_UNCHECKED);

	OnCbnSelchangeComboOutWeekday();
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboOutWeekday()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	int iPortNo = m_cboOutPort.GetCurSel();
	int iWeekday = m_cboOutWeekday.GetCurSel();
	int iRet = 0;
	NVS_SCHEDTIME timeSeg[4];
	NVS_SCHEDTIME* schedule[] = {&timeSeg[0],&timeSeg[1],&timeSeg[2],&timeSeg[3]};
	for (int i=0; i<4; i++)
	{
		memset(&timeSeg[i],0,sizeof(NVS_SCHEDTIME));
	}
	iRet = NetClient_GetOutportAlarmSchedule(m_iLogonID,iPortNo,iWeekday,schedule);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetOutportAlarmSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetOutportAlarmSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
	}
	if(iRet == 0)
	{
		SYSTEMTIME sysTime = {0};
		GetLocalTime(&sysTime);
		for (int i=0; i<4; i++)
		{
			sysTime.wHour = timeSeg[i].iStartHour;
			sysTime.wMinute = timeSeg[i].iStartMin;
			sysTime.wSecond = 0;
			m_dtcOutStartTime[i].SetTime(&sysTime);
		}
		for (int i=0; i<4; i++)
		{
			sysTime.wHour = timeSeg[i].iStopHour;
			sysTime.wMinute = timeSeg[i].iStopMin;
			sysTime.wSecond = 0;
			m_dtcOutEndTime[i].SetTime(&sysTime);
			m_chkOutTime[i].SetCheck(timeSeg[i].iRecordMode);
			if (timeSeg[i].iRecordMode)
			{
				m_dtcOutStartTime[i].EnableWindow(TRUE);
				m_dtcOutEndTime[i].EnableWindow(TRUE);
			}
			else
			{
				m_dtcOutStartTime[i].EnableWindow(FALSE);
				m_dtcOutEndTime[i].EnableWindow(FALSE );
			}
		}
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckOutport()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iEnable = 0;
	int iPortNo = GetDlgItemInt(IDC_COMBO_OUTPORT);
	int iRet = NetClient_GetOutportEnable(m_iLogonID, iPortNo, &iEnable);
	if (0 == iRet)
	{
		if (m_chkOutPort.GetCheck() != iEnable)
		{
			iRet = 	NetClient_SetOutportEnable(m_iLogonID, iPortNo, m_chkOutPort.GetCheck());

		}
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetOutportEnable (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetOutportEnable (%d,%d,%d)",m_iLogonID,iPortNo,iEnable);
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime1()
{
	if (BST_CHECKED == m_chkOutTime[0].GetCheck())
	{
		m_dtcOutStartTime[0].EnableWindow(TRUE);
		m_dtcOutEndTime[0].EnableWindow(TRUE);
	}
	else
	{
		m_dtcOutStartTime[0].EnableWindow(FALSE);
		m_dtcOutEndTime[0].EnableWindow(FALSE);
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime2()
{
	if (BST_CHECKED == m_chkOutTime[1].GetCheck())
	{
		m_dtcOutStartTime[1].EnableWindow(TRUE);
		m_dtcOutEndTime[1].EnableWindow(TRUE);
	}
	else
	{
		m_dtcOutStartTime[1].EnableWindow(FALSE);
		m_dtcOutEndTime[1].EnableWindow(FALSE);
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime3()
{
	if (BST_CHECKED == m_chkOutTime[2].GetCheck())
	{
		m_dtcOutStartTime[2].EnableWindow(TRUE);
		m_dtcOutEndTime[2].EnableWindow(TRUE);
	}
	else
	{
		m_dtcOutStartTime[2].EnableWindow(FALSE);
		m_dtcOutEndTime[2].EnableWindow(FALSE);
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedCheckOutTime4()
{
	if (BST_CHECKED == m_chkOutTime[3].GetCheck())
	{
		m_dtcOutStartTime[3].EnableWindow(TRUE);
		m_dtcOutEndTime[3].EnableWindow(TRUE);
	}
	else
	{
		m_dtcOutStartTime[3].EnableWindow(FALSE);
		m_dtcOutEndTime[3].EnableWindow(FALSE);
	}
}

void CLS_DNVRAlmSchPage::OnBnClickedButtonOutAllday()
{
	int iCurDay = m_cboOutWeekday.GetCurSel();
	for (int i=0; i<7; i++)
	{
		m_cboOutWeekday.SetCurSel(i);
		OnBnClickedButtonOutport();
	}
	m_cboOutWeekday.SetCurSel(iCurDay);
}

void CLS_DNVRAlmSchPage::OnBnClickedButtonOutport()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iPortNo = 0;
	int iWeekday = m_cboOutWeekday.GetCurSel();
	int iRet = 0;
	NVS_SCHEDTIME timeSeg[4];
	SYSTEMTIME sysTime = {0};
	for (int i=0; i<4; i++)
	{
		memset(&timeSeg[i],0,sizeof(NVS_SCHEDTIME));
		m_dtcOutStartTime[i].GetTime(&sysTime);
		timeSeg[i].iStartHour = sysTime.wHour;
		timeSeg[i].iStartMin = sysTime.wMinute;
		
		m_dtcOutEndTime[i].GetTime(&sysTime);
		timeSeg[i].iStopHour = sysTime.wHour;
		timeSeg[i].iStopMin = sysTime.wMinute;
		timeSeg[i].iRecordMode = m_chkOutTime[i].GetCheck();
		if (FALSE == m_chkOutTime[i].GetCheck())
		{
			memset(&timeSeg[i],0,sizeof(NVS_SCHEDTIME));
		}
		int iStart = timeSeg[i].iStartHour *60 + timeSeg[i].iStartMin;
		int iStop = timeSeg[i].iStopHour *60 + timeSeg[i].iStopMin;
		if (iStart > iStop)
		{
			return;
		}
	}

	NVS_SCHEDTIME* schedule[] = {&timeSeg[0],&timeSeg[1],&timeSeg[2],&timeSeg[3]};
	int iStarti = 0;
	int iStopi = 0;
	int iStartj = 0;
	int iStopj = 0;
	for (int i = 0; i < MAX_TIMESEGMENT; ++i)
	{
		for(int j = 0; j < MAX_TIMESEGMENT; ++j)
		{
			iStarti = schedule[i]->iStartHour*60+schedule[i]->iStartMin;
			iStopi = schedule[i]->iStopHour*60+schedule[i]->iStopMin;
			iStartj = schedule[j]->iStartHour*60+schedule[j]->iStartMin;
			iStopj = schedule[j]->iStopHour*60+schedule[j]->iStopMin;
			if (i == j || 0 == (iStarti+iStopi) || 0 == (iStartj+iStopi))
			{
				continue;
			}

			if (iStarti >= iStopi
				|| iStarti >= iStartj && iStarti < iStopj 
				|| iStopi > iStartj && iStopi <= iStopj)
			{
				AddLog(LOG_TYPE_MSG,"","time range %d(%2d:%2d - %2d:%2d) collide with time range %d(%2d:%2d - %2d:%2d)"
					,i,schedule[i]->iStartHour,schedule[i]->iStartMin,schedule[i]->iStopHour,schedule[i]->iStopMin
					,j,schedule[j]->iStartHour,schedule[j]->iStartMin,schedule[j]->iStopHour,schedule[j]->iStopMin);
				return;
			}
		}
	}

	iPortNo = m_cboOutPort.GetCurSel();
	iRet = NetClient_SetOutportAlarmSchedule(m_iLogonID,iPortNo,iWeekday,schedule);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetOutportAlarmSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetOutportAlarmSchedule (%d,%d,%d)",m_iLogonID,iPortNo,iWeekday);
	}
	if(iRet < 0)
	{
		MessageBox(GetTextEx(IDS_CONFIG_DNVR_ALMSCH_MESSAGESET), GetTextEx(IDS_CONFIG_PROMPT), MB_OK);
		m_cboOutWeekday.SetCurSel(0);
	}
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboInport()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iPortNo = m_cboInPort.GetCurSel();
	int iRet = 0;
	int iEnable = 0;
	m_chkInPort.ShowWindow(SW_SHOW);
	m_cboAlarmSubType.EnableWindow(FALSE);
	m_cboPlateLibId.EnableWindow(FALSE);

	if (PORT_ALARM_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetInportEnable(m_iLogonID, iPortNo, &iEnable);
	}
	else if (VIDEO_LOST_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = 	NetClient_GetAlarmVideoLost(m_iLogonID, iPortNo, &iEnable);
	}
	else if (VIDEO_MOTION_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = 	NetClient_GetMotionDetection(m_iLogonID, iPortNo, &iEnable);
	}
	else if (VIDEO_COVER_SCHEDULE == m_cboType.GetCurSel())
	{
		iRet = 	NetClient_GetAlmVideoCov(m_iLogonID, iPortNo, &iEnable);
	}
	else if (AUDIO_LOST_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam alarmScheEnableParam = {0};
		alarmScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_AUDIOLOST, CMD_GET_ALARMSCH_ENABLE, &alarmScheEnableParam);
		iEnable = alarmScheEnableParam.iEnable;
	}
	else if(WATER_RAIN_SCHEDULE == m_cboType.GetCurSel() || WATER_ALERT_WATER_SCHEDULE == m_cboType.GetCurSel())
	{
		m_chkInPort.ShowWindow(SW_HIDE);
		TAlarmScheEnableParam alarmScheEnableParam = {0};
		alarmScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		if (WATER_RAIN_SCHEDULE == m_cboType.GetCurSel())
		{
			iRet = NetClient_GetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_RAINFALL,CMD_GET_ALARMSCH_ENABLE, &alarmScheEnableParam);
			iEnable = alarmScheEnableParam.iEnable;
		}
		else if (WATER_ALERT_WATER_SCHEDULE == m_cboType.GetCurSel())
		{
			iRet = NetClient_GetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_ALERT_WATER_LEVEL,CMD_GET_ALARMSCH_ENABLE, &alarmScheEnableParam);
		    iEnable = alarmScheEnableParam.iEnable;
		}
	}
	else if (DANGEROUS_AREA_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam alarmScheEnableParam = {0};
		alarmScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_DANGEROUS_AREA, CMD_GET_ALARMSCH_ENABLE, &alarmScheEnableParam);
		iEnable = alarmScheEnableParam.iEnable;
	}
	else if (BLACK_BODY_ERR_SCHEDULE == m_cboType.GetCurSel())
	{
		TAlarmScheEnableParam alarmScheEnableParam = {0};
		alarmScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_BLACK_BODY_DETECT, CMD_GET_ALARMSCH_ENABLE, &alarmScheEnableParam);
		iEnable = alarmScheEnableParam.iEnable;
	} else if (VEHICLE_IDENTIFY_SCHEDULE == m_cboType.GetCurSel()) {
		m_cboAlarmSubType.EnableWindow(TRUE);
		m_cboPlateLibId.EnableWindow(TRUE);
		TAlarmScheEnableParam alarmScheEnableParam = {0};
		alarmScheEnableParam.iBuffSize = sizeof(TAlarmScheEnableParam);
		alarmScheEnableParam.iParam1 = m_cboAlarmSubType.GetCurSel() + 1;
		alarmScheEnableParam.iParam2 = m_cboPlateLibId.GetCurSel() + 1;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iPortNo, ALARM_TYPE_VEHICLE_IDENTIFICATION, CMD_GET_ALARMSCH_ENABLE, &alarmScheEnableParam);
		iEnable = alarmScheEnableParam.iEnable;
	}

	m_chkInPort.SetCheck(iEnable?BST_CHECKED:BST_UNCHECKED);
	OnCbnSelchangeComboWeekday();
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboDevtype()
{
	LocalSnapSchedule tInfo = {0};
	tInfo.iSize = (int)sizeof(LocalSnapSchedule);
	tInfo.iWeekDay = m_cboWeekday.GetCurSel();
	tInfo.iDevType = m_cboDevType.GetCurSel();
	int iByteReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_LOCALSNAP_SCHEDULE, m_iChannelNo, &tInfo, tInfo.iSize, &iByteReturn);
	if (iRet == 0)
	{
		SYSTEMTIME sysTime = {0};
		GetLocalTime(&sysTime);
		for (int i=0; i<4; i++)
		{
			sysTime.wHour = tInfo.tTimeSeg[i].iStartHour;
			sysTime.wMinute = tInfo.tTimeSeg[i].iStartMin;
			sysTime.wSecond = 0;
			m_dtcStartTime[i].SetTime(&sysTime);
		}
		for (int i=0; i<4; i++)
		{
			sysTime.wHour = tInfo.tTimeSeg[i].iStopHour;
			sysTime.wMinute = tInfo.tTimeSeg[i].iStopMin;
			sysTime.wSecond = 0;
			m_dtcEndTime[i].SetTime(&sysTime);
			m_chkTime[i].SetCheck(tInfo.tTimeSeg[i].iRecordMode);
			if (tInfo.tTimeSeg[i].iRecordMode)
			{
				m_dtcStartTime[i].EnableWindow(TRUE);
				m_dtcEndTime[i].EnableWindow(TRUE);
			}
			else
			{
				m_dtcStartTime[i].EnableWindow(FALSE);
				m_dtcEndTime[i].EnableWindow(FALSE );
			}
		}
	}
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboAlarmSubType()
{
	OnCbnSelchangeComboInport();
}

void CLS_DNVRAlmSchPage::OnCbnSelchangeComboPlateLibId()
{
	OnCbnSelchangeComboInport();
}


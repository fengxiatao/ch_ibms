
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceSche.h"


IMPLEMENT_DYNAMIC(CLS_DlgFaceSchedule, CLS_PageBase)

CLS_DlgFaceSchedule::CLS_DlgFaceSchedule(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceSchedule::IDD, pParent)
{

}

CLS_DlgFaceSchedule::~CLS_DlgFaceSchedule()
{
}

void CLS_DlgFaceSchedule::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALARM_TYPE, m_cboAlgoType);
	DDX_Control(pDX, IDC_CBO_LIBKEY, m_cboLibKey);
	DDX_Control(pDX, IDC_CBO_SCHE, m_cboWeekday);
	DDX_Control(pDX, IDC_CHK_SCHE, m_chkSchEnable);
	DDX_Control(pDX, IDC_CHK_TIME1, m_chkSchTime[0]);
	DDX_Control(pDX, IDC_CHK_TIME2, m_chkSchTime[1]);
	DDX_Control(pDX, IDC_CHK_TIME3, m_chkSchTime[2]);
	DDX_Control(pDX, IDC_CHK_TIME4, m_chkSchTime[3]);
	DDX_Control(pDX, IDC_DATE_BEG1, m_dtSchTimeBeg[0]);
	DDX_Control(pDX, IDC_DATE_BEG2, m_dtSchTimeBeg[1]);
	DDX_Control(pDX, IDC_DATE_BEG3, m_dtSchTimeBeg[2]);
	DDX_Control(pDX, IDC_DATE_BEG4, m_dtSchTimeBeg[3]);
	DDX_Control(pDX, IDC_DATE_END1, m_dtSchTimeEnd[0]);
	DDX_Control(pDX, IDC_DATE_END2, m_dtSchTimeEnd[1]);
	DDX_Control(pDX, IDC_DATE_END3, m_dtSchTimeEnd[2]);
	DDX_Control(pDX, IDC_DATE_END4, m_dtSchTimeEnd[3]);
	DDX_Control(pDX, IDC_CBO_ALARM_SCHEDULE_VCATYPE, m_cboVcaType);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceSchedule, CLS_PageBase)
	ON_BN_CLICKED(IDC_BTN_LIBKEY_QUERY, &CLS_DlgFaceSchedule::OnBnClickedBtnLibkeyQuery)
	ON_BN_CLICKED(IDC_BTN_SET, &CLS_DlgFaceSchedule::OnBnClickedBtnSet)
	ON_BN_CLICKED(IDC_BTN_GET, &CLS_DlgFaceSchedule::OnBnClickedBtnGet)
	ON_BN_CLICKED(IDC_CHK_TIME1, &CLS_DlgFaceSchedule::OnBnClickedChkTime1)
	ON_BN_CLICKED(IDC_CHK_TIME1, &CLS_DlgFaceSchedule::OnBnClickedChkTime1)
	ON_BN_CLICKED(IDC_CHK_TIME2, &CLS_DlgFaceSchedule::OnBnClickedChkTime2)
	ON_BN_CLICKED(IDC_CHK_TIME3, &CLS_DlgFaceSchedule::OnBnClickedChkTime3)
	ON_BN_CLICKED(IDC_CHK_TIME4, &CLS_DlgFaceSchedule::OnBnClickedChkTime4)
	ON_CBN_SELCHANGE(IDC_CBO_ALARM_TYPE, &CLS_DlgFaceSchedule::OnCbnSelchangeCboAlarmType)
END_MESSAGE_MAP()

void CLS_DlgFaceSchedule::UI_Init()
{
	m_cboAlgoType.ResetContent();
	m_cboAlgoType.SetItemData(m_cboAlgoType.AddString("IPC Face recognition"), ALARM_TYPE_FACE_IDENT);
	m_cboAlgoType.SetItemData(m_cboAlgoType.AddString("NVR Face recognition"), ALARM_TYPE_NVR_VCA);
	m_cboAlgoType.SetCurSel(0);

	m_cboVcaType.ResetContent();
	m_cboVcaType.InsertString(0, "Blacklist");
	m_cboVcaType.InsertString(1, "Whitelist");
	m_cboVcaType.SetCurSel(0);

	m_cboWeekday.ResetContent();
	m_cboWeekday.SetItemData(m_cboWeekday.AddString("Sunday"), 0);
	m_cboWeekday.SetItemData(m_cboWeekday.AddString("Monday"), 1);
	m_cboWeekday.SetItemData(m_cboWeekday.AddString("Tuesday"), 2);
	m_cboWeekday.SetItemData(m_cboWeekday.AddString("Wednesday"), 3);
	m_cboWeekday.SetItemData(m_cboWeekday.AddString("Thursday"), 4);
	m_cboWeekday.SetItemData(m_cboWeekday.AddString("Friday"), 5);
	m_cboWeekday.SetItemData(m_cboWeekday.AddString("Saturday"), 6);
	m_cboWeekday.SetCurSel(0);

	CTime BeginTime(1971, 1, 1, 0, 0, 0);
	CTime EndTime(1971, 1, 1, 23, 59, 59);
	for (int i = 0; i < 4; i++)
	{
		m_dtSchTimeBeg[i].SetFormat("HH:mm");
		m_dtSchTimeBeg[i].SetTime(&BeginTime);
		m_dtSchTimeEnd[i].SetFormat("HH:mm");
		m_dtSchTimeEnd[i].SetTime(&EndTime);
		m_chkSchTime[i].SetCheck(FALSE);
		m_dtSchTimeBeg[i].EnableWindow(FALSE);
		m_dtSchTimeEnd[i].EnableWindow(FALSE);
	}
}

void CLS_DlgFaceSchedule::OnBnClickedBtnLibkeyQuery()
{
	QueryLibkey(m_cboLibKey);
}

void CLS_DlgFaceSchedule::OnBnClickedBtnSet()
{
	int iRet = -1;
	int iLibKeySel = -1;
	int iLibKey = 0;
	int iAlarmType = 0;
	int iAlarmSubType = 0;

	iAlarmType = GetItemCurData(m_cboAlgoType);
	iAlarmSubType = m_cboVcaType.GetCurSel();

	if ( (ALARM_TYPE_FACE_IDENT ==  iAlarmType && VCA_IPC_EVENT_FACE_IDENT_COMPARE != iAlarmSubType)
		|| (ALARM_TYPE_NVR_VCA == iAlarmType && VCA_NVR_EVENT_FACE_IDENT_COMPARE != iAlarmSubType) )
	{
		iLibKey = 0;//IPC face recognition - except for comparison alarm (blacklist), other subtypes have nothing to do with iLibKey, and the default value is 0
					//NVR face recognition - except for comparison alarm, other sub types have nothing to do with iLibKey, and the default is 0
 	}
	else
	{
		iLibKeySel = m_cboLibKey.GetCurSel();
		CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);
		iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);//Face library ID
	}
	
	//Deployment enabling
	TAlarmScheEnableParam tSchEnabel = {0};	
	tSchEnabel.iBuffSize = sizeof(tSchEnabel);
	tSchEnabel.iSceneID = 0;
	tSchEnabel.iParam1 = iLibKey;//Face library ID 
	tSchEnabel.iParam2 = iAlarmSubType;	//Algorithm Type When iAlarmType=20, 0-blacklist, 1-whitelist iAlarmType=21, 0-face detection, 1-face recognition comparison alarm, 2-face recognition stranger alarm, 3-face recognition frequency alarm, 4-face recognition detention alarm
	strncpy_s(tSchEnabel.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tSchEnabel.cLibUUID));
	tSchEnabel.iEnable = m_chkSchEnable.GetCheck();
	iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, iAlarmType, CMD_ALARMSCH_ENABLE_EX, &tSchEnabel);

	//Protection formwork
	TAlarmScheduleParam tSchParam = {0};
	tSchParam.iBuffSize = sizeof(tSchParam);
	tSchParam.iWeekday = GetItemCurData(m_cboWeekday);
	tSchParam.iSceneID = 0;	//Scene No
	tSchParam.iParam1 = iLibKey;//Face library ID 
	tSchParam.iParam2 = iAlarmSubType;	//Algorithm Type When iAlarmType=20, 0-blacklist, 1-whitelist iAlarmType=21, 0-face detection, 1-face recognition comparison alarm, 2-face recognition stranger alarm, 3-face recognition frequency alarm, 4-face recognition detention alarm
	for(int i = 0; i < MAX_TIMESEGMENT; i++)
	{
		NVS_SCHEDTIME &tSeg = tSchParam.timeSeg[GetItemCurData(m_cboWeekday)][i];
		tSeg.iRecordMode = m_chkSchTime[i].GetCheck();
		if (tSeg.iRecordMode)
		{
			CTime ctBeg;
			CTime ctEnd;
			m_dtSchTimeBeg[i].GetTime(ctBeg);
			m_dtSchTimeEnd[i].GetTime(ctEnd);			
			tSeg.iStartHour = ctBeg.GetHour();
			tSeg.iStartMin = ctBeg.GetMinute();
			tSeg.iStopHour = ctEnd.GetHour();
			tSeg.iStopMin = ctEnd.GetMinute();
		}		
	}
	strncpy_s(tSchParam.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tSchParam.cLibUUID));
	iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, iAlarmType, CMD_SET_ALARMSCHEDULE,  &tSchParam);
	if (iRet < 0)
	{
	}
}

void CLS_DlgFaceSchedule::OnBnClickedBtnGet()
{
	int iRet = -1;
	int iLibKeySel = 0;
	int iLibKey = 0;
	int iAlarmType = 0;
	int iAlarmSubType = 0;

	iAlarmType = GetItemCurData(m_cboAlgoType);
	iAlarmSubType = m_cboVcaType.GetCurSel();

	if ( (ALARM_TYPE_FACE_IDENT ==  iAlarmType && VCA_IPC_EVENT_FACE_IDENT_COMPARE != iAlarmSubType)
		|| (ALARM_TYPE_NVR_VCA == iAlarmType && VCA_NVR_EVENT_FACE_IDENT_COMPARE != iAlarmSubType) )
	{
		iLibKey = 0;//IPC face recognition - except for comparison alarm (blacklist), other subtypes have nothing to do with iLibKey, and the default value is 0
					//NVR face recognition - except for comparison alarm, other sub types have nothing to do with iLibKey, and the default is 0
	}
	else
	{
		iLibKeySel = m_cboLibKey.GetCurSel();
		CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);
		iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);//Face library ID
	}

	//Deployment enabling
	TAlarmScheEnableParam tSchEnabel = {0};	
	tSchEnabel.iBuffSize = sizeof(tSchEnabel);
	tSchEnabel.iSceneID = 0;
	tSchEnabel.iParam1 = iLibKey;//Face library ID
	tSchEnabel.iParam2 = iAlarmSubType;	//Algorithm Type When iAlarmType=20, 0-blacklist, 1-whitelist iAlarmType=21, 0-face detection, 1-face recognition comparison alarm, 2-face recognition stranger alarm, 3-face recognition frequency alarm, 4-face recognition detention alarm
	strncpy_s(tSchEnabel.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tSchEnabel.cLibUUID));
	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, iAlarmType, CMD_GET_ALARMSCH_ENABLE, &tSchEnabel);
	if (iRet >= 0)
	{
		m_chkSchEnable.SetCheck(tSchEnabel.iEnable);
	}

	//Protection formwork
	TAlarmScheduleParam tSchParam = {0};
	tSchParam.iBuffSize = sizeof(tSchParam);
	tSchParam.iWeekday = GetItemCurData(m_cboWeekday);
	tSchParam.iSceneID = 0;	//Scene No
	tSchParam.iParam1 = iLibKey;//Face library ID
	tSchParam.iParam2 = iAlarmSubType;	//Algorithm Type When iAlarmType=20, 0-blacklist, 1-whitelist iAlarmType=21, 0-face detection, 1-face recognition comparison alarm, 2-face recognition stranger alarm, 3-face recognition frequency alarm, 4-face recognition detention alarm
	strncpy_s(tSchParam.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tSchParam.cLibUUID));
	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, iAlarmType, CMD_GET_ALARMSCHEDULE,  &tSchParam);
	if (iRet >= 0)
	{
		for(int i = 0; i < MAX_TIMESEGMENT; i++)
		{
			NVS_SCHEDTIME &tSeg = tSchParam.timeSeg[GetItemCurData(m_cboWeekday)][i];
			CTime ctBeg(1971, 1, 1, 0, 0, 0);
			CTime ctEnd(1971, 1, 1, 23, 59, 59);
			m_chkSchTime[i].SetCheck(tSeg.iRecordMode);
			EnableWindowSchTime(i);
			if (tSeg.iRecordMode)
			{
				ctBeg = CTime(1971, 1, 1, tSeg.iStartHour, tSeg.iStartMin, 0);
				ctEnd = CTime(1971, 1, 1, tSeg.iStopHour, tSeg.iStopMin, 0);
			}			
			m_dtSchTimeBeg[i].SetTime(&ctBeg);
			m_dtSchTimeEnd[i].SetTime(&ctEnd);
		}
	}
}

void CLS_DlgFaceSchedule::EnableWindowSchTime(int _iIndex)
{
	if (_iIndex >=0 && _iIndex < 4)
	{
		m_dtSchTimeBeg[_iIndex].EnableWindow(m_chkSchTime[_iIndex].GetCheck());
		m_dtSchTimeEnd[_iIndex].EnableWindow(m_chkSchTime[_iIndex].GetCheck());
	}
}

void CLS_DlgFaceSchedule::OnBnClickedChkTime1()
{
	EnableWindowSchTime(0);
}

void CLS_DlgFaceSchedule::OnBnClickedChkTime2()
{
	EnableWindowSchTime(1);
}

void CLS_DlgFaceSchedule::OnBnClickedChkTime3()
{
	EnableWindowSchTime(2);
}

void CLS_DlgFaceSchedule::OnBnClickedChkTime4()
{
	EnableWindowSchTime(3);
}

void CLS_DlgFaceSchedule::OnCbnSelchangeCboAlarmType()
{
	if(ALARM_TYPE_FACE_IDENT == m_cboAlgoType.GetItemData(m_cboAlgoType.GetCurSel()))
	{
		m_cboVcaType.ResetContent();
		m_cboVcaType.InsertString(0, "Blacklist");
		m_cboVcaType.InsertString(1, "Whitelist");
		m_cboVcaType.SetCurSel(0);
	}
	else
	{
		m_cboVcaType.ResetContent();
		m_cboVcaType.InsertString(0, "Face Detection");
		m_cboVcaType.InsertString(1, "Face recognition-comparison");
		m_cboVcaType.InsertString(2, "Face recognition-stranger");
		m_cboVcaType.InsertString(3, "Face recognition-frequency");
		m_cboVcaType.InsertString(4, "Face recognition-detention");
		m_cboVcaType.SetCurSel(0);
	}	
}

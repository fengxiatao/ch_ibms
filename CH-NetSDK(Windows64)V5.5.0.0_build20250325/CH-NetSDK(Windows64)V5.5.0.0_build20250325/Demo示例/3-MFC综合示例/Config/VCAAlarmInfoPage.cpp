// VCAAlarmInfoPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAAlarmInfoPage.h"

#define RULE_ALL			0xFF		//Rule number supports all functions
#define SCENE_SUM			16			//Total number of scenes

// CLS_VCAAlarmInfoPage dialog

IMPLEMENT_DYNAMIC(CLS_VCAAlarmInfoPage, CDialog)

extern int g_iEventIDS[VCA_EVENT_MAX];

static vca_TVCAParam g_VcaParam = {0};

CLS_VCAAlarmInfoPage::CLS_VCAAlarmInfoPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCAAlarmInfoPage::IDD, pParent)
{
	 m_iLogonID = -1;
	 m_iChannelNo = -1;
	
}

CLS_VCAAlarmInfoPage::~CLS_VCAAlarmInfoPage()
{
}

void CLS_VCAAlarmInfoPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_VCA_ALARM, m_lstctAlarmInfo);
	DDX_Control(pDX, IDC_COMBO_VCA_ALARM_RULE, m_cboRuleID);
	DDX_Control(pDX, IDC_COMBO_VCA_ALARM_EVENT, m_cboEventID);
	DDX_Control(pDX, IDC_CBO_VCA_ALARMINFO_SCENE_NUM, m_cboSceneID);
	DDX_Control(pDX, IDC_EDT_VCA_ALARM_STATISTIC, m_edtAlarmTimes);
	DDX_Control(pDX, IDC_CHK_VCA_ALARMINFO_VALID, m_chkValid);
	DDX_Control(pDX, IDC_CHECK_LINK_PTZ, m_chkLinkPtz);
}


BEGIN_MESSAGE_MAP(CLS_VCAAlarmInfoPage, CDialog)
	ON_CBN_SELCHANGE(IDC_COMBO_VCA_ALARM_RULE, &CLS_VCAAlarmInfoPage::OnCbnSelchangeComboVcaAlarmRule)
	ON_BN_CLICKED(IDC_BUTTON_VCA_ALARM_STATISTIC_CLEAR, &CLS_VCAAlarmInfoPage::OnBnClickedButtonVcaAlarmStatisticClear)
	ON_BN_CLICKED(IDC_BUTTON_CLEAR_RECORD, &CLS_VCAAlarmInfoPage::OnBnClickedButtonClearRecord)
	ON_CBN_SELCHANGE(IDC_CBO_VCA_ALARMINFO_SCENE_NUM, &CLS_VCAAlarmInfoPage::OnCbnSelchangeCboVcaAlarminfoSceneNum)
	ON_CBN_SELCHANGE(IDC_COMBO_VCA_ALARM_EVENT, &CLS_VCAAlarmInfoPage::OnCbnSelchangeComboVcaAlarmEvent)
	ON_NOTIFY(NM_CLICK, IDC_LIST_VCA_ALARM, &CLS_VCAAlarmInfoPage::OnNMClickList2)
END_MESSAGE_MAP()

BOOL CLS_VCAAlarmInfoPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	m_lstctAlarmInfo.SetExtendedStyle(m_lstctAlarmInfo.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);

	UI_UpdateText();
	m_lstctAlarmInfo.DeleteAllItems();
	UI_Clear();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_VCAAlarmInfoPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateText();
}

void CLS_VCAAlarmInfoPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}
	UI_Clear();
	UI_UpdateVcaAlarmStatistic();
}

void CLS_VCAAlarmInfoPage::UI_UpdateText()
{
	SetDlgItemTextEx(IDC_STATIC_ALARM_STATISTIC, IDS_VCA_ALARM_STATISTIC);
	SetDlgItemTextEx(IDC_STATIC_VCA_ALARM_RULE, IDS_VCA_RULE_ID);
	SetDlgItemTextEx(IDC_STATIC_VCA_ALARM_EVENT, IDS_VCA_EVENT_ID);
	SetDlgItemTextEx(IDC_STATIC_VCA_ALARM_STATISTIC, IDS_VCA_ALRAM_TIMES);
	SetDlgItemTextEx(IDC_BUTTON_VCA_ALARM_STATISTIC_CLEAR, IDS_VCA_CLEAR_ALARM_STATISTIC);
	SetDlgItemTextEx(IDC_STATIC_ALARM_INFO, IDS_VCA_ALARM_INFO);
	SetDlgItemTextEx(IDC_BUTTON_CLEAR_RECORD, IDS_VCA_CLEAR_RECORD);
	SetDlgItemTextEx(IDC_STC_VCA_ALARMINFO_SCENE_NUM, IDS_AREA_NUM);

	int iColumn = 0;
	InsertColumn(m_lstctAlarmInfo, iColumn++, "IP", LVCFMT_CENTER, 100);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_CHANNEL_NO, LVCFMT_CENTER, 80);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_ALARM_STATUS, LVCFMT_CENTER, 80);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_EVENT_TYPE, LVCFMT_CENTER, 100);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_RULE_ID, LVCFMT_CENTER, 60);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_RULENAME, LVCFMT_CENTER, 80);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_TARGET_ID, LVCFMT_CENTER, 60);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_TARGET_TYPE, LVCFMT_CENTER, 80);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_TARGET_POS, LVCFMT_CENTER, 160);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_TARGET_SPEED, LVCFMT_CENTER, 80);
	InsertColumn(m_lstctAlarmInfo, iColumn++, IDS_VCA_TARGET_DIRECTION, LVCFMT_CENTER, 80);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("UUID", "UUID"), LVCFMT_CENTER, 60);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("P", "P"), LVCFMT_CENTER, 60);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("T", "T"), LVCFMT_CENTER, 60);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("Z", "Z"), LVCFMT_CENTER, 60);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("方位角", "Angel"), LVCFMT_CENTER, 100);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("水平视场角", "Horizontal field angle"), LVCFMT_CENTER, 100);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("垂直视场角", "Vertical field angle"), LVCFMT_CENTER, 100);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("报警时间", "Alarm time"), LVCFMT_CENTER, 160);
	InsertColumn(m_lstctAlarmInfo, iColumn++, GetTextByLan("警情类型", "Alarm tpe"), LVCFMT_CENTER, 60);

	m_cboRuleID.ResetContent();
	InsertString(m_cboRuleID, 0, GetTextEx(IDS_PLAYBACK_TYPE_ALL));
	m_cboRuleID.SetItemData(0, RULE_ALL);
	for (int i = 1; i < VCA_MAX_RULE_NUM + 1; i++)
	{
		m_cboRuleID.InsertString(i, IntToCString(i));
		m_cboRuleID.SetItemData(i, i-1);
	}
	m_cboRuleID.SetCurSel(0);

	m_cboSceneID.ResetContent();
	for (int i = 0; i < MAX_SCENE_NUM; i++)
	{
		CString cstSceneID;
		cstSceneID.Format("%d", i + 1);
		m_cboSceneID.InsertString(i, cstSceneID);
	}
	m_cboSceneID.SetCurSel(0);

	m_cboEventID.ResetContent();
	for (int i = 0; i < sizeof(g_iEventIDS)/sizeof(int); i++)
	{
		InsertString(m_cboEventID, i, g_iEventIDS[i]);
	}	
	m_cboEventID.SetCurSel(0);
}

void CLS_VCAAlarmInfoPage::UI_UpdateVcaAlarmStatistic()
{
	if (m_iChannelNo < 0)
	{
		return;
	}

	VCARuleParam  tParam = {0};
	int iSelIndex = m_cboRuleID.GetCurSel();
	tParam.stRule.iRuleID = m_cboRuleID.GetItemData(iSelIndex);
	tParam.stRule.iSceneID = m_cboSceneID.GetCurSel();
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RULE_PARAM, m_iChannelNo, &tParam, sizeof(VCARuleParam));
	if (iRet < 0 || !tParam.stRule.iValid)
	{
		UI_Clear();
		m_cboEventID.SetCurSel(0);
		return;
	}
 	if (tParam.iEventID == VCA_EVENT_TRIPWIRE || tParam.iEventID == VCA_EVENT_PERIMETER)
 	{
		vca_TVCAParam * vp = &g_VcaParam;
		memset(vp, 0, sizeof(vca_TVCAParam));
		int iRuleID = m_cboRuleID.GetCurSel();
		vp->chnParam[m_iChannelNo].iRuleID =  iRuleID;
		iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_ALARM_STATISTIC, m_iChannelNo, vp, sizeof(vca_TVCAParam));
		vca_TRuleParam *pRule = &(vp->chnParam[m_iChannelNo].rule[iRuleID]);
 		int iAlarmCount = pRule->iAlarmCount;
 		SetDlgItemInt(IDC_EDT_VCA_ALARM_STATISTIC, iAlarmCount);
 	}

 	m_cboEventID.SetCurSel(tParam.iEventID);
}

void CLS_VCAAlarmInfoPage::UI_UpdateVcaAlarmInfo(int _iLogonID, int _iAlarmIndex)
{
	PDEVICE_INFO Device = FindDevice(_iLogonID);
	if (Device == NULL)
	{
		return;
	}
	CString szIP = Device->cIP;
	vca_TAlarmInfo ti = {0};
	int iRetAlarmInfo = NetClient_VCAGetAlarmInfo(_iLogonID, _iAlarmIndex, &ti, sizeof(ti));

	int iCurrentRuleId = (int)m_cboRuleID.GetItemData(m_cboRuleID.GetCurSel());

	if (RULE_ALL == iCurrentRuleId || ti.iRuleID == iCurrentRuleId)
	{
		VCARuleParam stRulePara = {0};
		stRulePara.iBufSize = sizeof(stRulePara);	
		stRulePara.stRule.iRuleID = ti.iRuleID;
		stRulePara.stRule.iSceneID = m_cboSceneID.GetCurSel();

		int iRetConfig = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RULE_PARAM, m_iChannelNo, &stRulePara, sizeof(stRulePara));
		if ((iRetAlarmInfo >= 0) && (iRetConfig >= 0))
		{
			CString State[2] = {"OFF", "ON"};

			//VCA_EVENT_MIN ~ VCA_EVENT_TRIPWIRE ~ ...... VCA_EVENT_VEHICLE_IDENTIFY ~ VCA_EVENT_MAX
			CString EventType[VCA_EVENT_MAX] =
			{
				 "TRIPWIRE", "DBTRIPWIRE", "PERIMETER", "LOITER", "PARKING", "RUN", "HIGH_DENSITY", "ABANDUM", "OBJSTOLEN", "FACEREC"
				, "VIDEODETECT", "TRACK", "FLUXSTATISTIC", "CROWD", "LEAVE_DETECT", "WATER_LEVEL_DETECT", "AUDIO_DIAGNOSE", "FACE_MOSAIC", "RIVERCLEAN", "DREDGE"
				, "ILLEAGEPARK", "FIGHT", "ALERT", "PLATE_RECOGNISE", "HEAT_MAP", "SEEPER", "WINDOW_DETECTION", "STFACEADVANCE", "PARK_GUARD", "UNKNOWN"
				, "HELMET", "LINK_DOME_TRACK", "SLUICEGATE", "COLOR_TRACK", "FORMAT_TYPE", "SEDIMENT", "ALERTWATER", "SINGLE_INQUIRY", "CLIMB_UP", "NET_DEPARTURE"
				, "ABNORMAL_NUMBER", "GET_UP", "LEAVE_BED", "STATIC_DETECTION", "SLEEP_POSTION", "SLIP_UP", "NEW_FIGHT", "BODY_TOUCH", "HUMAN_DETECT", "DAM_AMARM"
				, "NET_AMARM", "PEPT", "FLOWSPEED", "BEACON_SHIP", "CHEFHAT", "STRANDED", "SINGLE_ALONE", "WINDOW_DELIVERY", "SMOKING", "WEAR_MASK"
				, "NOWEAR_MASK", "PHONE", "EVETEMDETECT", "TEMDETECT", "FIREWORKDETECT", "PLATENUMBER_BLACKLIST", "SMART_DETECT", "INUIRY_TIMEOUT", "ELECTRIC_VEHICLE", "LEAVE_SEAT"
				, "SCENE_REC", "CONTRA_BAND", "BED_REST", "ATTENDED", "DOOR_OPEN", "POSEREC", "CONVERSE", "COURTPII", "COURTELP", "BEHAVIREC"
				, "INCLINED_STATIS", "VERTICAL_STATIS", "PERSON_GATHER", "BODY_TEMPERATURE", "PERSON_DENSITY", "VEHICLE_DENSITY", "TAFFIC_JAM", "VEHICLE_STANDED", "ABNORMAL_PARKING", "CROSS_CONGESTION"
				, "JUDGE_BEHAVIOR", "VERTICALHUMAN", "AERIAL_PROJECTILE", "WATER_OUTFALL", "POLICE_UNIFORM", "SPDRESS_DRESS", "SPDRESS_QUEUE", "VEHICLE_IDENTIFY", "ACTIVE_STRATEGY","EVENT_RESERVE_99"
				, "EVENT_RESERVE_100", "EVENT_RESERVE_101", "AUDIO_LOST_ALARM", "CLIMB_WALL", "CLASSROOM_BEHAVIOR_RECOGNITION", "WATER_COLOR_DETECT", "SLEEP_ABNORMAL", "PLAY_PHONES", "HUMANIOD_DETECT","VEHICLE_DETECT"
				, "NON_MOTOR_DETECT", "PLATE_WHITELIST", "NUMBER_DETECTION", "IMAGE_ANALYSIS_DETECTION", "AUDIO_ANALYSIS", "PERSON_FAST_MOVE", "COLLISION_DETECTION", "ESCORT_ANOMALY", "MEAL_DETECTION","POLICE_UNIFORMS_MIXED"
				, "NON_STATIC_DETECTION", "NOT_WEAR_POLICE_UNIFORMS", "TARGET_PARA", "HUMAN_DETECT_MONITOR", "DISCIPLINE_INSPECTORS", "MOTION_DETECTION","GAS_DETECTION"
			};

			CString TargetType[] = {"People", "Thing", "Car"};
			CString TargetTypeTemperature[] = {"Normal", "Early", "Area compare"};
			CString AdvType[] = {"NOISE", "CLARITY", "BRIGHT_ABMNL", "COLOR", "FREEZE", "NOSIGNAL", "CHANGE", "INTERFERE", "PTZ_LOST_CTL"};
			int iItemCount = m_lstctAlarmInfo.GetItemCount();
			int iColumn = 0;
			m_lstctAlarmInfo.InsertItem(iItemCount, "");
			CString szChannelNo;
			szChannelNo.Format("%d", ti.iChannel + 1);
			CString szState = State[ti.iState];
			CString szEventType = EventType[ti.iEventType];
			CString szRuleID;

			if(VCA_EVENT_BRIGHT_KITCHEN == ti.iEventType) {
				if(0 == ti.iRuleID) {
					szRuleID = _T("Reservation");
				} else if(1 == ti.iRuleID) {
					szRuleID = _T("without chef's hat");
				} else if(2 == ti.iRuleID) {
					szRuleID = _T("No mask");
				} else if(3 == ti.iRuleID) {
					szRuleID = _T("Not wearing chef clothes");
				} else {
					szRuleID.Format("%d", ti.iRuleID + 1);
				}
			} else if(VCA_EVENT_ZHONGYI == ti.iEventType) {
				if(0 == ti.iRuleID) {
					szRuleID = _T("Reservation");
				} else if(1 == ti.iRuleID) {
					szRuleID = _T("Smoke");
				} else if(2 == ti.iRuleID) {
					szRuleID = _T("Call");
				} else{
					szRuleID.Format("%d", ti.iRuleID + 1);
				}
			} else {
				szRuleID.Format("%d", ti.iRuleID + 1);
			}
			
			CString szTargetID;
			szTargetID.Format("%u", ti.uiTargetID);
			CString szTargetType;
			if (VCA_EVENT_VIDEODETECT == ti.iEventType)
			{
				if (ti.iTargetType & VCA_AVD_NOISE)
				{
					szTargetType += "[" + AdvType[0] + "]";
				}
				if (ti.iTargetType & VCA_AVD_CLARITY)
				{
					szTargetType += "[" + AdvType[1] + "]";
				}
				if (ti.iTargetType & VCA_AVD_BRIGHT_ABMNL)
				{
					szTargetType += "[" + AdvType[2] + "]";
				}
				if (ti.iTargetType & VCA_ADV_COLOR)
				{
					szTargetType += "[" + AdvType[3] + "]";
				}
				if (ti.iTargetType & VCA_ADV_FREEZE)
				{
					szTargetType += "[" + AdvType[4] + "]";
				}
				if (ti.iTargetType & VCA_ADV_NOSIGNAL)
				{
					szTargetType += "[" + AdvType[5] + "]";
				}
				if (ti.iTargetType & VCA_ADV_CHANGE)
				{
					szTargetType += "[" + AdvType[6] + "]";
				}
				if (ti.iTargetType & VCA_ADV_INTERFERE)
				{
					szTargetType += "[" + AdvType[7] + "]";
				}
				if (ti.iTargetType & VCA_ADV_PTZ_LOST_CTL)
				{
					szTargetType += "[" + AdvType[8] + "]";
				}		
			}
			else if (VCA_EVENT_EVETEMDETECT == ti.iEventType)
			{
				szTargetType = IntToCString(ti.iTargetType);
			}
			else
			{
				szTargetType = TargetType[ti.iTargetType];
			}
			CString szRctTarget;
			szRctTarget.Format("(%d,%d,%d,%d)", ti.rctTarget.left, ti.rctTarget.top, ti.rctTarget.right, ti.rctTarget.bottom);
			CString szTargetSpeed;
			szTargetSpeed.Format("%d", ti.iTargetSpeed);
			szTargetSpeed += "pixel/s";
			CString szTargetDirection;
			szTargetDirection.Format("%d", ti.iTargetDirection);

			CString cstRuleName = stRulePara.cRuleName;

			CString csPicUUid;
			csPicUUid.Format(ti.cPicUuid);
			if(!csPicUUid.IsEmpty())
			{
				WriteUuidToFile(csPicUUid);
			}

			CString szPtzP;
			szPtzP.Format("%u", ti.iPtzP);

			CString szPtzT;
			szPtzT.Format("%u", ti.iPtzT);

			CString szPtzZ;
			szPtzZ.Format("%u", ti.iPtzZ);

			CString szAngel;
			szAngel.Format("%u", ti.iAngle);

			CString szHvision;
			szHvision.Format("%u", ti.iHView);

			CString szVvision;
			szVvision.Format("%u", ti.iVView);

			CString cstrAlarmTime = ti.cAlarmTime;
			CString cstrAlarmType = IntToCString(ti.iAlarmType);

			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szIP);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szChannelNo);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szState);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szEventType);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szRuleID);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, cstRuleName);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szTargetID);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szTargetType);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szRctTarget);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szTargetSpeed);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szTargetDirection);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, csPicUUid);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szPtzP);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szPtzT);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szPtzZ);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szAngel);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szHvision);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, szVvision);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, cstrAlarmTime);
			m_lstctAlarmInfo.SetItemText(iItemCount, iColumn++, cstrAlarmType);
		}
	}
	UI_UpdateVcaAlarmStatistic();
}

int CLS_VCAAlarmInfoPage::WriteUuidToFile(CString _cstrFileName)
{
	if(_cstrFileName.IsEmpty())
	{
		return RET_FAILED;
	}
	FILE* pFile = NULL;
	pFile = fopen(".\\NetClientDemo\\UUID.txt", "ab+");
	if(NULL == pFile)
	{
		return RET_FAILED;
	}
	fwrite(_cstrFileName.GetBuffer(), 1, _cstrFileName.GetLength(), pFile);

	char cDelim = '\n';
	fwrite(&cDelim, 1, 1, pFile);
	fflush(pFile);
	fclose(pFile);
	pFile = NULL;
	return RET_SUCCESS;
}

void CLS_VCAAlarmInfoPage::OnBnClickedButtonVcaAlarmStatisticClear()
{
 	if (m_iLogonID < 0 || m_iChannelNo < 0)
 	{
 		return;
 	}
 	int iRuleID = (int)m_cboRuleID.GetItemData(m_cboRuleID.GetCurSel());
 	vca_TVCAParam * vp = &g_VcaParam;
 	memset(vp, 0, sizeof(vca_TVCAParam));
 
 	vp->chnParam[m_iChannelNo].iRuleID = iRuleID;

	if (RULE_ALL == m_cboRuleID.GetItemData(m_cboRuleID.GetCurSel()))
	{
		for (int i = 0;i < m_cboRuleID.GetCount();i++)
		{
			vp->chnParam[m_iChannelNo].iRuleID = (int)m_cboRuleID.GetItemData(i);
			if (RULE_ALL == vp->chnParam[m_iChannelNo].iRuleID)
			{
				continue;
			}
			int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SET_ALARM_STATISTIC, m_iChannelNo, vp, sizeof(g_VcaParam));
			if (iRet != 0)
			{
				AddLog(LOG_TYPE_FAIL, "", "CLS_VCAAlarmInfoPage::NetClient_VCASetConfig[VCA_CMD_SET_ALARM_STATISTIC] (%d, %d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
			}
			else
			{
				SetDlgItemInt(IDC_EDT_VCA_ALARM_STATISTIC, 0);
				AddLog(LOG_TYPE_SUCC,"","CLS_VCAAlarmInfoPage::NetClient_VCASetConfig[VCA_CMD_SET_ALARM_STATISTIC] (%d, %d)", m_iLogonID, m_iChannelNo);
			}
		}
	}
	else
	{
		int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SET_ALARM_STATISTIC, m_iChannelNo, vp, sizeof(g_VcaParam));
		if (iRet != 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "CLS_VCAAlarmInfoPage::NetClient_VCASetConfig[VCA_CMD_SET_ALARM_STATISTIC] (%d, %d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
		}
		else
		{
			SetDlgItemInt(IDC_EDT_VCA_ALARM_STATISTIC, 0);
			AddLog(LOG_TYPE_SUCC,"","CLS_VCAAlarmInfoPage::NetClient_VCASetConfig[VCA_CMD_SET_ALARM_STATISTIC] (%d, %d)", m_iLogonID, m_iChannelNo);
		}
	}
}


void CLS_VCAAlarmInfoPage::UI_Clear()
{
	SetDlgItemTextEx(IDC_STATIC_VCA_ALARM_STATISTIC, IDS_VCA_ALRAM_TIMES);
}


void CLS_VCAAlarmInfoPage::OnBnClickedButtonClearRecord()
{
	m_lstctAlarmInfo.DeleteAllItems();
}

void CLS_VCAAlarmInfoPage::OnAlarmNotify( int _iLogonID, int _iChannelNo, int _iAlarmState,int _iAlarmType,int _iUserData )
{
	if (ALARM_VCA_INFO == _iAlarmType)
	{
		UI_UpdateVcaAlarmInfo(_iLogonID, _iAlarmState);
	}
}

void CLS_VCAAlarmInfoPage::OnCbnSelchangeCboVcaAlarminfoSceneNum()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}
	OnBnClickedButtonVcaAlarmStatisticClear();
	OnBnClickedButtonClearRecord();
	UI_UpdateVcaAlarmStatistic();
}

void CLS_VCAAlarmInfoPage::OnCbnSelchangeComboVcaAlarmEvent()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}
	OnBnClickedButtonVcaAlarmStatisticClear();
	OnBnClickedButtonClearRecord();
	UI_UpdateVcaAlarmStatistic();
}

void CLS_VCAAlarmInfoPage::OnCbnSelchangeComboVcaAlarmRule()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}
	OnBnClickedButtonVcaAlarmStatisticClear();
	OnBnClickedButtonClearRecord();
	UI_UpdateVcaAlarmStatistic();
}



void CLS_VCAAlarmInfoPage::OnNMClickList2(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	// TODO: Add your control notification handler code here
	NM_LISTVIEW* pNMListView = (NM_LISTVIEW*)pNMHDR;

	int nItem = pNMListView->iItem;		//line number
	int nSub = 	pNMListView->iSubItem;	//column number
	int iCount = m_lstctAlarmInfo.GetItemCount();
	if(nItem == -1)	//If there is no data in the row, return
	{
		return;
	}

	if(BST_UNCHECKED == m_chkLinkPtz.GetCheck())
	{
		return;
	}
	// TODO: Add control notification handler code here
	char cPtzP[64] = {0};
	char cPtzT[64] = {0};
	char cPtzZ[64] = {0};
	m_lstctAlarmInfo.GetItemText(pNMListView->iItem,12,cPtzP,sizeof(cPtzP));
	m_lstctAlarmInfo.GetItemText(pNMListView->iItem,13,cPtzT,sizeof(cPtzT));
	m_lstctAlarmInfo.GetItemText(pNMListView->iItem,14,cPtzZ,sizeof(cPtzZ));

	SetPtz tSetPtz = {0};
	tSetPtz.iSize = sizeof(SetPtz);
	tSetPtz.iType = 1;
	tSetPtz.iPan = atoi(cPtzP) / 100;
	tSetPtz.iTilt = atoi(cPtzT)/ 100;
	tSetPtz.iZoom = atoi(cPtzZ)/ 100;

	int iRet = NetClient_SendCommand(m_iLogonID,COMMAND_ID_SET_PTZ, m_iChannelNo, &tSetPtz, sizeof(SetPtz));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand(%d,%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_SET_PTZ,m_iChannelNo,&tSetPtz,sizeof(SetPtz));
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SendCommand(%d,%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_SET_PTZ,m_iChannelNo,&tSetPtz,sizeof(SetPtz));
	}


	*pResult = 0;
}

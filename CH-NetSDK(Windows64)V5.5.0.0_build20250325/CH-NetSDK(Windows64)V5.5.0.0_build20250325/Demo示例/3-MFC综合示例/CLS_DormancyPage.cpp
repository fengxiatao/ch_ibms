// CLS_DormancyPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DormancyPage.h"

#define MAX_HOUR	24
#define MAX_MINUTE	60
// CLS_DormancyPage dialog
static int g_iWeek[] = {IDS_CONFIG_SUNDAY, IDS_CONFIG_MONDAY, IDS_CONFIG_TUESDAY
, IDS_CONFIG_WEDNESDAY, IDS_CONFIG_THURSDAY, IDS_CONFIG_FRIDAY ,IDS_CONFIG_SATURDAY};

#define DORMANCY_STATE  255

IMPLEMENT_DYNAMIC(CLS_DormancyPage, CDialog)

CLS_DormancyPage::CLS_DormancyPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DormancyPage::IDD, pParent)
{

}

CLS_DormancyPage::~CLS_DormancyPage()
{
}

void CLS_DormancyPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK10, m_iEnable[0]);
	DDX_Control(pDX, IDC_CHECK11, m_iEnable[1]);
	DDX_Control(pDX, IDC_CHECK12, m_iEnable[2]);
	DDX_Control(pDX, IDC_CHECK13, m_iEnable[3]);
	DDX_Control(pDX, IDC_CHECK14, m_iEnable[4]);
	DDX_Control(pDX, IDC_CHECK15, m_iEnable[5]);
	DDX_Control(pDX, IDC_CHECK16, m_iEnable[6]);
	DDX_Control(pDX, IDC_CHECK17, m_iEnable[7]);
	DDX_Control(pDX, IDC_COMBO_WEEK, m_iWeekDay);
	DDX_Control(pDX, IDC_CHECK18, m_bChooseAll);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME1, m_dtBeginTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME2, m_dtBeginTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME3, m_dtBeginTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME4, m_dtBeginTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME5, m_dtBeginTime[4]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME6, m_dtBeginTime[5]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME7, m_dtBeginTime[6]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_STARTTIME8, m_dtBeginTime[7]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME1, m_dtEndTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME2, m_dtEndTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME3, m_dtEndTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME4, m_dtEndTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME5, m_dtEndTime[4]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME6, m_dtEndTime[5]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME7, m_dtEndTime[6]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_ENDTIME8, m_dtEndTime[7]);
	DDX_Control(pDX, IDC_CHECK_DAY0, m_bChooseDate[0]);
	DDX_Control(pDX, IDC_CHECK_DAY1, m_bChooseDate[1]);
	DDX_Control(pDX, IDC_CHECK_DAY2, m_bChooseDate[2]);
	DDX_Control(pDX, IDC_CHECK_DAY3, m_bChooseDate[3]);
	DDX_Control(pDX, IDC_CHECK_DAY4, m_bChooseDate[4]);
	DDX_Control(pDX, IDC_CHECK_DAY5, m_bChooseDate[5]);
	DDX_Control(pDX, IDC_CHECK_DAY6, m_bChooseDate[6]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE1, m_cboSegType[0]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE2, m_cboSegType[1]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE3, m_cboSegType[2]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE4, m_cboSegType[3]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE5, m_cboSegType[4]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE6, m_cboSegType[5]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE7, m_cboSegType[6]);
	DDX_Control(pDX, IDC_COMBO_SegTYPE8, m_cboSegType[7]);
	DDX_Control(pDX, IDC_STATIC_DEVSTATE, m_DevState);
}


BEGIN_MESSAGE_MAP(CLS_DormancyPage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON1, &CLS_DormancyPage::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_CHECK18, &CLS_DormancyPage::OnBnClickedCheck18)
	ON_BN_CLICKED(IDC_CHECK_DAY1, &CLS_DormancyPage::OnBnClickedCheckDay1)
	ON_CONTROL_RANGE(BN_CLICKED,IDC_CHECK_DAY0,IDC_CHECK_DAY6,&CLS_DormancyPage::OnBnClickedChkWeek)
	ON_BN_CLICKED(IDC_BUTTON_SETDORMANCY, &CLS_DormancyPage::OnBnClickedButtonSetdormancy)
	ON_BN_CLICKED(IDC_BUTTONSETAWAKE, &CLS_DormancyPage::OnBnClickedButtonsetawake)
	ON_CBN_SELCHANGE(IDC_COMBO_WEEK, &CLS_DormancyPage::OnCbnSelchangeComboWeek)
	ON_WM_CTLCOLOR()
	ON_BN_CLICKED(IDC_CHECK14, &CLS_DormancyPage::OnBnClickedCheck14)
END_MESSAGE_MAP()


// CLS_DormancyPage message handlers
BOOL CLS_DormancyPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialog();
	UI_Clear();
	CTime timeNow = CTime::GetCurrentTime();
	int iDayOfWeek = timeNow.GetDayOfWeek()-1;
	m_iWeekDay.SetCurSel(iDayOfWeek);


	//Check the copied current week information
	//m_chkWeekNum[iCurrentWeekDay].SetCheck(BST_CHECKED);
	// Select all by default
	m_bChooseAll.SetCheck(BST_CHECKED);
	for (int i = 0 ; i < MAX_DAYS; i++)  
	{
		m_bChooseDate[i].SetCheck(BST_CHECKED);
	}
	GetDlgItem(IDC_STATIC_INFO)->SetWindowText(GetTextByLan(_T("注：一个时间段内起始时间和结束时间不能小于五分钟，相邻时间段时间间隔不得小于五分钟"),_T("Note: The starting and ending time of a time period should not be less than five minutes, and the interval between adjacent time periods should not be less than five minutes. ")));
	GetDlgItem(IDC_STATIC_WARNING)->SetWindowText(GetTextByLan(_T("注:一键唤醒需等待大约35秒左右,等待过程请勿进行其他操作"),_T("Awake the Device need about 35s,please do not operate other things")));
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_DormancyPage::UI_Clear()
{
	CTime BeginTime(1971, 1, 1, 0, 0, 0);
	CTime EndTime(1971, 1, 1, 0, 0, 0);
	for (int i = 0; i < MAX_SCHEDULE; i++)
	{
		m_dtBeginTime[i].SetFormat("HH:mm");
		m_dtBeginTime[i].SetTime(&BeginTime);
		m_dtEndTime[i].SetFormat("HH:mm");
		m_dtEndTime[i].SetTime(&EndTime);
	}
}

void CLS_DormancyPage::UI_UpdateDialog()
{
	for (int i = 0; i < 7; i++)
	{
		InsertString(m_iWeekDay, i, g_iWeek[i]);
	}

	for (int i = 0; i < MAX_SCHEDULE; i++)
	{
		InsertString(m_cboSegType[i], 0, GetTextByLan(_T("休眠"), _T("Dormancy")));
		InsertString(m_cboSegType[i], 1, GetTextByLan(_T("唤醒"), _T("Wake")));
		m_cboSegType[i].ShowWindow(SW_HIDE);
	}

	SetDlgItemTextEx(IDC_STATIC_WEEKDAY, IDS_VCA_WEEK);
	SetDlgItemTextEx(IDC_BUTTON_DOME_SCHEDULE_SET, IDS_DORMANCY);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME1,IDS_CONFIG_DNVR_ALMSCH_TIME1);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME2,IDS_CONFIG_DNVR_ALMSCH_TIME2);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME3,IDS_CONFIG_DNVR_ALMSCH_TIME3);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME4,IDS_CONFIG_DNVR_ALMSCH_TIME4);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME5, IDS_CONFIG_DNVR_ALMSCH_TIME5);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME6, IDS_CONFIG_DNVR_ALMSCH_TIME6);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME7, IDS_CONFIG_DNVR_ALMSCH_TIME7);
	SetDlgItemTextEx(IDC_STATIC_OUT_TIME8, IDS_CONFIG_DNVR_ALMSCH_TIME8);
}

void CLS_DormancyPage::OnBnClickedButton1()
{
	// TODO: Add your control notification handler code here
	if (!CheckPTZTime())
	{
		return;
	}

	for(int i = 0; i < 7; i++)
	{
		if(BST_CHECKED == m_bChooseDate[i].GetCheck())
		{
			TDormancySchedule DormancySchedule = {0};
			CTime BeginTime;
			CTime EndTime;
			int j = 0;
			for (j= 0; j < MAX_SCHEDULE; j++)
			{
				m_dtBeginTime[j].GetTime(BeginTime);
				m_dtEndTime[j].GetTime(EndTime);

				DormancySchedule.tWork[j].iBeginHour = BeginTime.GetHour();
				DormancySchedule.tWork[j].iBeginMinute = BeginTime.GetMinute();
				DormancySchedule.tWork[j].iEndHour = EndTime.GetHour();
				DormancySchedule.tWork[j].iEndMinute = EndTime.GetMinute();

				DormancySchedule.tWork[j].iEnable = m_iEnable[j].GetCheck();

				if (m_iEnable[j].GetCheck() != BST_UNCHECKED)
				{
					DormancySchedule.tWork[j].iWorkType = DORMANCY_STATE;
				}
				else
					DormancySchedule.tWork[j].iWorkType = 0;
			}

			DormancySchedule.iWeekDay = i;
			int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DORMANCY_SCHEDULE, m_iChannelNO, &DormancySchedule, sizeof(DormancySchedule));
			if (iRet < 0)
			{
				AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig[NET_CLIENT_DORMANCY_SCHEDULE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
			}
			else
			{
				AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig[NET_CLIENT_DORMANCY_SCHEDULE] (%d, %d), ", m_iLogonID, m_iChannelNO);
			}
		}
	}

}

bool CLS_DormancyPage::CheckPTZTime()
{
	CTime BeginTime;
	CTime EndTime;
	CTime BeginTime_before;
	CTime EndTime_before;
	for (int i = 0;i <= 7;i++)
	{
		if (m_iEnable[i].GetCheck() == BST_UNCHECKED)//If not enabled, the time will not be judged
		{
			continue;
		}
		m_dtBeginTime[i].GetTime(BeginTime_before);
		m_dtEndTime[i].GetTime(EndTime_before);
		// //Start and end are 0, do not participate in comparison
		//if (BeginTime_before.GetHour() == 0 && BeginTime_before.GetMinute() == 0 && BeginTime_before.GetSecond() == 0 && EndTime_before.GetHour() == 0 && EndTime_before.GetMinute() ==0 && EndTime_before.GetSecond() == 0)
		//{
		//	continue;
		//}
		if (EndTime_before <= BeginTime_before || (EndTime_before - BeginTime_before) < 300)
		{
			AfxMessageBox(GetTextByLan(_T("请检查您的时间，重新输入！"),_T("Please Check your time, ant input again")));
			return false;
		}

		for (int j = 0;j <= 7;j++)
		{
			if (j == i || m_iEnable[j].GetCheck() == BST_UNCHECKED)
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
				return false;
			}
			else if ((BeginTime_before <= BeginTime && BeginTime <= EndTime_before) || (BeginTime_before <= EndTime && EndTime <= EndTime_before) || (BeginTime_before > EndTime && (BeginTime_before - EndTime < 300)) || (EndTime_before < BeginTime && (BeginTime - EndTime_before < 300)))               
			{
				AfxMessageBox(GetTextByLan(GetTextByLan(_T("请检查您的时间，重新输入！"),_T("Please Check your time, ant input again"))));
				return false;
			}
		}
	}
	return true;
}


void CLS_DormancyPage::OnBnClickedCheck18()
{
	// TODO: Add your control notification handler code here
	int iCheck = m_bChooseAll.GetCheck();
	for (int i=0; i<MAX_DAYS; i++)
	{
		m_bChooseDate[i].SetCheck(iCheck);
	}

	int iWeekDay = m_iWeekDay.GetCurSel();
	// Do not select all by default select the week selected by the drop-down box
	if (BST_UNCHECKED == m_bChooseAll.GetCheck())
	{
		if(iWeekDay >= 0 && iWeekDay < MAX_DAYS)
		{
			m_bChooseDate[iWeekDay].SetCheck(BST_CHECKED);
		}
	}
}


void CLS_DormancyPage::OnBnClickedCheckDay1()
{
	// TODO: Add your control notification handler code here

}

void CLS_DormancyPage::OnBnClickedChkWeek(UINT _uiID)
{
	int index = _uiID - IDC_CHECK_DAY0;
	if (m_bChooseDate[index].GetCheck() == BST_UNCHECKED)
	{
		if (m_bChooseAll.GetCheck() == BST_CHECKED)
		{
			m_bChooseAll.SetCheck(BST_UNCHECKED);
		}
	}
	else
	{
		int i = 0;
		for (i = 0; i < MAX_DAYS; i++)
		{
			if (m_bChooseDate[i].GetCheck() == BST_UNCHECKED)
			{
				return;
			}
		}
		m_bChooseAll.SetCheck(BST_CHECKED);
	}
}

void CLS_DormancyPage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;
	}
	else
	{
		m_iChannelNO =  _iChannelNo;
	}

	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else
	{
		m_iLogonID =  _iLogonID;
	}
	
	if (_iStreamNo < 0)
	{
		m_iStreamNO = 0;
	}
	else
	{
		m_iStreamNO = _iStreamNo;
	}
	int iAblity = -1;
	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_SYSTEM;
	stFuncAbilityLevel.iSubFuncType = 18;
	int iByteReturned = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO,\
		&stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iByteReturned);
	if (iRet < 0 || strlen(stFuncAbilityLevel.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DormancyPage::GetFuncRoi] GetDevConfig MAIN_FUNC_TYPE_SYSTEM Failed! m_iLogonID %d", m_iLogonID);
		return;
	}
	BOOL blTempChk = ((stFuncAbilityLevel.cParam[0]) == '1')?TRUE:FALSE;
	GetDlgItem(IDC_BUTTON1)->EnableWindow(blTempChk);
	GetDlgItem(IDC_BUTTON_SETDORMANCY)->EnableWindow(blTempChk);
	GetDlgItem(IDC_BUTTONSETAWAKE)->EnableWindow(blTempChk);

	TDormancySchedule DormancySchedule = {0};
	int iBytesReturned = 0;
	DormancySchedule.iWeekDay = m_iWeekDay.GetCurSel();
	iRet = NetClient_GetDevConfig(_iLogonID, NET_CLIENT_DORMANCY_SCHEDULE, m_iChannelNO, &DormancySchedule, sizeof(DormancySchedule), &iBytesReturned);
	if (iRet < 0 )
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig[NET_CLIENT_DORMANCY_SCHEDULE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
		return;
	}
	UpdateWeek(DormancySchedule.iWeekDay, &DormancySchedule);
	UpdateState();
}

void CLS_DormancyPage::UpdateState()
{
	TDormancyState DormancyState = {0};
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DORMANCY_STATE, m_iChannelNO, &DormancyState, sizeof(DormancyState),&iBytesReturned);
	
	if (iRet < 0)
	{
		m_DevState.SetWindowText(GetTextByLan(_T("未知"), _T("UnKnown")));
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig[NET_CLIENT_DORMANCY_SCHEDULE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
		return;
	}
	if (DormancyState.iEnable != 0)
	{
		m_DevState.SetWindowText(GetTextByLan(_T("休眠中"), _T("Dormancying")));
	}
	else
	{
		m_DevState.SetWindowText(GetTextByLan(_T("工作中"), _T("Working")));
	}
	
}

void CLS_DormancyPage::UpdateWeek(int iWeek, TDormancySchedule *pDormancySchedule)
{
	if (NULL == pDormancySchedule)
	{
		return;
	}

	//8 time periods with one enable

	for (int i = 0; i < MAX_SCHEDULE; i++)
	{

		CTime timeNow = CTime::GetCurrentTime();

		//Starting time
		if (pDormancySchedule->tWork[i].iBeginHour >= 0 && pDormancySchedule->tWork[i].iBeginHour < MAX_HOUR && 
			pDormancySchedule->tWork[i].iBeginMinute >= 0 && pDormancySchedule->tWork[i].iBeginMinute < MAX_MINUTE)
		{
			CTime timeOpen(timeNow.GetYear(),timeNow.GetMonth(),timeNow.GetDay(),pDormancySchedule->tWork[i].iBeginHour,pDormancySchedule->tWork[i].iBeginMinute,0);
			m_dtBeginTime[i].SetTime(&timeOpen);
		}
		else
		{
			CTime timeOpen(timeNow.GetYear(),timeNow.GetMonth(),timeNow.GetDay(), 0, 0, 0);
			m_dtBeginTime[i].SetTime(&timeOpen);
		}

		if (pDormancySchedule->tWork[i].iEndHour >= 0 && pDormancySchedule->tWork[i].iEndHour < MAX_HOUR && 
			pDormancySchedule->tWork[i].iEndMinute >= 0 && pDormancySchedule->tWork[i].iEndMinute < MAX_MINUTE)
		{
			//End Time
			CTime timeEnd(timeNow.GetYear(),timeNow.GetMonth(),timeNow.GetDay(),pDormancySchedule->tWork[i].iEndHour,pDormancySchedule->tWork[i].iEndMinute,0);
			m_dtEndTime[i].SetTime(&timeEnd);
		}
		else
		{
			//End Time
			CTime timeEnd(timeNow.GetYear(),timeNow.GetMonth(),timeNow.GetDay(),0,0,0);
			m_dtEndTime[i].SetTime(&timeEnd);
		}

		//linkage action
		if (pDormancySchedule->tWork[i].iWorkType)
		{
			m_cboSegType[i].SetCurSel(0);
		}
		else
			m_cboSegType[i].SetCurSel(1);
		m_iEnable[i].SetCheck(pDormancySchedule->tWork[i].iEnable);
	}
}
void CLS_DormancyPage::OnBnClickedButtonSetdormancy()
{
	// TODO: Add your control notification handler code here
	DormancySet tInfo = {0};
	tInfo.iSize = (int)sizeof(DormancySet);
	tInfo.iEnable = 0xff;
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_DORMANCY_SET, m_iChannelNO, &tInfo, tInfo.iSize, NULL, 0);
	if (RET_SUCCESS == iRet)
	{
		if (1 == tInfo.iResult)
		{
			AfxMessageBox(GetTextByLan(_T("设置失败"),_T("Setting Failed")));
		}
		else if (2 == tInfo.iResult)
		{
			AfxMessageBox(GetTextByLan(_T("设置动作太频繁"),_T("Setting too Fast")));
		}
		UpdateState();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_CmdConfig failed, ret(%d)", iRet);
	}
	
}

void CLS_DormancyPage::OnBnClickedButtonsetawake()
{
	// TODO: Add your control notification handler code here
	DormancySet tInfo = {0};
	tInfo.iSize = (int)sizeof(DormancySet);
	tInfo.iEnable = 0;
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_DORMANCY_SET, m_iChannelNO, &tInfo, tInfo.iSize, NULL, 0);
	if (RET_SUCCESS == iRet)
	{
		if (1 == tInfo.iResult)
		{
			AfxMessageBox(GetTextByLan(_T("设置失败"),_T("Setting Failed")));
		}
		else if (2 == tInfo.iResult)
		{
			AfxMessageBox(GetTextByLan(_T("设置动作太频繁"),_T("Setting too Fast")));
		}
		UpdateState();
	}
	else if (RET_LIGHTLOGON_GET_TIME_OUT == iRet)
	{
		AfxMessageBox(GetTextByLan(_T("回复超时"),_T("Reply Time Out")));
	}
}

void CLS_DormancyPage::OnCbnSelchangeComboWeek()
{
	// TODO: Add your control notification handler code here
	TDormancySchedule DormancySchedule = {0};
	int iBytesReturned = 0;
	DormancySchedule.iWeekDay = m_iWeekDay.GetCurSel();
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DORMANCY_SCHEDULE, m_iChannelNO, &DormancySchedule, sizeof(DormancySchedule), &iBytesReturned);
	if (iRet < 0 )
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig[NET_CLIENT_DORMANCY_SCHEDULE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
		return;
	}
	UpdateWeek(DormancySchedule.iWeekDay, &DormancySchedule);
}

HBRUSH CLS_DormancyPage::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CLS_BasePage::OnCtlColor(pDC, pWnd, nCtlColor);

	// TODO:  Change any attributes of the DC here
	if(pWnd->GetDlgCtrlID()==IDC_STATIC_INFO || pWnd->GetDlgCtrlID() == IDC_STATIC_WARNING)//If it is a static edit box
	{
		pDC->SetTextColor(RGB(255,0,0));//Change font color
	}

	// TODO:  Return a different brush if the default is not desired
	return hbr;
}

void CLS_DormancyPage::OnBnClickedCheck14()
{
	// TODO: Add your control notification handler code here
}

// WiegandPage.cpp : implementation file
//

#include "stdafx.h"
#include "WiegandPage.h"


#define WIEGAND_TYPE_RESERVED 0
#define WIEGAND_TYPE_INPUT    1
#define WIEGAND_TYPE_OUTPUT     2

#define WIEGAND_PARA_CLOSE 0
#define WIEGAND_PARA_OPEN   1
#define WIEGAND_PARA_WIEGAND26  1
#define WIEGAND_PARA_WIEGAND34  2
// CLS_Wiegand dialog

IMPLEMENT_DYNAMIC(CLS_Wiegand, CDialog)
static int g_iWeek[] = { IDS_CONFIG_SUNDAY,IDS_CONFIG_MONDAY, IDS_CONFIG_TUESDAY, IDS_CONFIG_WEDNESDAY, IDS_CONFIG_THURSDAY, IDS_CONFIG_FRIDAY ,IDS_CONFIG_SATURDAY};
CLS_Wiegand::CLS_Wiegand(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_Wiegand::IDD, pParent)
{
	m_iLogonID = -1;
    m_iChannelNO = -1;
}

CLS_Wiegand::~CLS_Wiegand()
{
}

void CLS_Wiegand::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_cboType);
	DDX_Control(pDX, IDC_COMBO_PARA, m_cboPara);
	DDX_Control(pDX, IDC_BUTTON_SET, m_btnSet);
    DDX_Control(pDX, IDC_COMBO_OUTDATA, m_cboOutData);
	DDX_Control(pDX,IDC_CHK_LowPower,m_chkLowPowerEnable);
	DDX_Control(pDX, IDC_STARTIME1,m_dtStartTime[0]);
	DDX_Control(pDX,IDC_ENDTIME1,m_dtEndTime[0]);
	DDX_Control(pDX, IDC_STARTIME2,m_dtStartTime[1]);
	DDX_Control(pDX,IDC_ENDTIME2,m_dtEndTime[1]);
	DDX_Control(pDX, IDC_STARTIME3,m_dtStartTime[2]);
	DDX_Control(pDX,IDC_ENDTIME3,m_dtEndTime[2]);
	DDX_Control(pDX, IDC_STARTIME4,m_dtStartTime[3]);
	DDX_Control(pDX,IDC_ENDTIME4,m_dtEndTime[3]);
	DDX_Control(pDX,IDC_CMBWEEKDAY,m_cboWeekDay);
	
}


BEGIN_MESSAGE_MAP(CLS_Wiegand, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_Wiegand::OnBnClickedButtonSet)
    ON_CBN_SELCHANGE(IDC_COMBO_TYPE, &CLS_Wiegand::OnCbnSelchangeComboType)
	ON_BN_CLICKED(IDC_LOWPOWERSET, &CLS_Wiegand::OnBnClickedLowpowerset)
	ON_CBN_SELCHANGE(IDC_CMBWEEKDAY, &CLS_Wiegand::OnCbnSelchangeCmbweekday)
	ON_BN_CLICKED(IDC_BTNGET, &CLS_Wiegand::OnBnClickedBtnget)
END_MESSAGE_MAP()


// CLS_Wiegand message handlers

BOOL CLS_Wiegand::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

    UI_UpdateDialog();
	InitTime();
	return TRUE;
}

void CLS_Wiegand::InitTime()
{
	CTime BeginTime(1971, 1, 1, 0, 0, 0);
	CTime EndTime(1971, 1, 1, 23, 59, 59);
	for(int i = 0;i < MAX_NUM_TIME_PERIODS ;i++){
		m_dtStartTime[i].SetFormat("HH:mm");
		m_dtStartTime[i].SetTime(&BeginTime);
		m_dtEndTime[i].SetFormat("HH:mm");
		m_dtEndTime[i].SetTime(&EndTime);
	}
	UpdateData(FALSE);
}


void CLS_Wiegand::initCboPara(int iType)
{
    m_cboPara.ResetContent();
    if (WIEGAND_TYPE_INPUT == iType)
    {
        m_cboPara.InsertString(WIEGAND_PARA_CLOSE, GetTextByLan(_T("关闭"), _T("Close")));
        m_cboPara.InsertString(WIEGAND_PARA_OPEN, GetTextByLan(_T("开启"), _T("Open")));
    }
    else if (WIEGAND_TYPE_OUTPUT == iType)
    {
        m_cboPara.InsertString(WIEGAND_PARA_CLOSE, GetTextByLan(_T("关闭"), _T("Close")));
        m_cboPara.InsertString(WIEGAND_PARA_WIEGAND26, GetTextByLan(_T("Wiegand26"), _T("Wiegand26")));
        m_cboPara.InsertString(WIEGAND_PARA_WIEGAND34, GetTextByLan(_T("Wiegand34"), _T("Wiegand34")));
    }
}

void CLS_Wiegand::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
{
// 	if (m_iLogonID == _iLogonID)
// 	{
// 		return;
// 	}
	m_iLogonID = _iLogonID;
    m_iChannelNo = _iChannelNo;
}

void CLS_Wiegand::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_Wiegand::UI_UpdateDialog()
{
    int iType = m_cboType.GetCurSel();
    if (-1 == iType)
        iType = WIEGAND_TYPE_RESERVED;
    int iPara = m_cboPara.GetCurSel();
    if (-1 == iPara)
        iPara = 0;
    int iOutData = m_cboOutData.GetCurSel();
    if (-1 == iOutData)
        iOutData = 0;

	SetDlgItemText(IDC_STATIC_TYPE, GetTextByLan(_T("类型"), _T("Type")));
	SetDlgItemText(IDC_STATIC_PARA, GetTextByLan(_T("参数"), _T("Param")));
    SetDlgItemText(IDC_BUTTON_SET, GetTextByLan(_T("设置"), _T("Set")));
    SetDlgItemText(IDC_STATIC_OUTDATA, GetTextByLan(_T("内容"), _T("Data")));

    m_cboType.ResetContent();
    m_cboType.InsertString(WIEGAND_TYPE_RESERVED, GetTextByLan(_T("保留"), _T("Reserved")));
    m_cboType.InsertString(WIEGAND_TYPE_INPUT, GetTextByLan(_T("输入"), _T("Input")));
    m_cboType.InsertString(WIEGAND_TYPE_OUTPUT, GetTextByLan(_T("输出"), _T("Output")));
    initCboPara(iType);
    m_cboType.SetCurSel(iType);

    m_cboOutData.ResetContent();
    m_cboOutData.InsertString(WIEGAND_TYPE_RESERVED, GetTextByLan(_T("保留"), _T("Reserved")));
    m_cboOutData.InsertString(WIEGAND_TYPE_INPUT, GetTextByLan(_T("编号"), _T("Number")));
    m_cboOutData.InsertString(WIEGAND_TYPE_OUTPUT, GetTextByLan(_T("卡号"), _T("CardNo")));

	for (int i = 0; i < MAX_WEEK_DAYS; i++)
	{
		InsertString(m_cboWeekDay, i, g_iWeek[i]);
	}
	m_cboWeekDay.SetCurSel(0);
	for(int i = 0;i < MAX_WEEK_DAYS;i++)
	{
		for(int k = 0; k < MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT;k++){
			m_tDevLowPowerSchedule[i][k].iStartHour = 0;
			m_tDevLowPowerSchedule[i][k].iStartMin = 0;
			m_tDevLowPowerSchedule[i][k].iStopHour = 0;
			m_tDevLowPowerSchedule[i][k].iStopMin = 0;
		}
	}

    //first set
    WIEGAND tParam = {0};
    tParam.iSize = sizeof(tParam);
    tParam.iChannelNo = m_iChannelNo;
    tParam.iType = iType;
    int iBytesReturned = 0;
    int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_WIEGAND, m_iChannelNo, &tParam, sizeof(tParam), &iBytesReturned);
    if (iRet < RET_SUCCESS)
    {
        AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig failed! Logon id(%d)", m_iLogonID);
    }
    else
    {
        AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[NET_CLIENT_WIEGAND] (%d, %d,%d,%d,%d)", m_iLogonID, m_iChannelNo, tParam.iType, tParam.iParam, tParam.iOutData);
        m_cboType.SetCurSel(tParam.iType);
        initCboPara(tParam.iType);
        m_cboPara.SetCurSel(tParam.iParam);
        m_cboOutData.SetCurSel(tParam.iOutData);
    }
	
	OnBnClickedBtnget();
	
}

void CLS_Wiegand::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    if (_iLogonID < 0)
    {
        AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)", _iLogonID);
        return;
    }

    switch (_iParaType)
    {
    case  PARA_WIEGAND:
        {
            AddLog(LOG_TYPE_MSG,"","[CLS_Wiegand][OnParamChangeNotify] logon id(%d)", _iLogonID);
            UI_UpdateDialog();
        }
        break;
    default:
        break;
    }
}

void CLS_Wiegand::OnBnClickedButtonSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

    //first set
    WIEGAND tParam = {0};
    tParam.iSize = sizeof(tParam);
    tParam.iChannelNo = m_iChannelNo;
    tParam.iType = m_cboType.GetCurSel();
    tParam.iParam = m_cboPara.GetCurSel();
    tParam.iOutData = m_cboOutData.GetCurSel();

    int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_WIEGAND, m_iChannelNo, &tParam, sizeof(WIEGAND));
    if (iRet != RET_SUCCESS)
    {
        AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig[NET_CLIENT_WIEGAND] (%d, %d), error(%d)"
            , m_iLogonID, m_iChannelNo, GetLastError());
    }
    else
    {
        UI_UpdateDialog();
    }
}

void CLS_Wiegand::OnCbnSelchangeComboType()
{
    UI_UpdateDialog();
    
}

void CLS_Wiegand::OnBnClickedLowpowerset()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	CTime BeginTime;
	CTime EndTime;
	int iWeekDay = m_cboWeekDay.GetCurSel();
	if(0 == iWeekDay){
		iWeekDay = 6;
	} else	{
		iWeekDay = iWeekDay - 1;
	}

	for(int i = 0; i< MAX_NUM_TIME_PERIODS;i++){
		m_dtStartTime[i].GetTime(BeginTime);
		m_dtEndTime[i].GetTime(EndTime);
		m_tDevLowPowerSchedule[iWeekDay][i].iStartHour = BeginTime.GetHour();
		m_tDevLowPowerSchedule[iWeekDay][i].iStartMin = BeginTime.GetMinute();
		m_tDevLowPowerSchedule[iWeekDay][i].iStopHour = EndTime.GetHour();
		m_tDevLowPowerSchedule[iWeekDay][i].iStopMin = EndTime.GetMinute();
	}
	
	if(	m_tDevLowPowerSchedule[iWeekDay][0].iStopHour >= m_tDevLowPowerSchedule[iWeekDay][1].iStartHour && 
		m_tDevLowPowerSchedule[iWeekDay][0].iStopMin  >= m_tDevLowPowerSchedule[iWeekDay][1].iStartMin){
		MessageBox(GetTextByLan("时间段重复，请重新输入","Please Input Time Period"));
		return;
	}

	if(	m_tDevLowPowerSchedule[iWeekDay][1].iStopHour >= m_tDevLowPowerSchedule[iWeekDay][2].iStartHour && 
		m_tDevLowPowerSchedule[iWeekDay][1].iStopMin  >= m_tDevLowPowerSchedule[iWeekDay][2].iStartMin){
			MessageBox(GetTextByLan("时间段重复，请重新输入","Please Input Time Period"));
			return;
	}

	if(	m_tDevLowPowerSchedule[iWeekDay][2].iStopHour >= m_tDevLowPowerSchedule[iWeekDay][3].iStartHour && 
		m_tDevLowPowerSchedule[iWeekDay][2].iStopMin  >= m_tDevLowPowerSchedule[iWeekDay][3].iStartMin){
			MessageBox(GetTextByLan("时间段重复，请重新输入","Please Input Time Period"));
			return;
	}


	DeviceLowPower tDevice = {0};
	tDevice.iEnable = m_chkLowPowerEnable.GetCheck();
	tDevice.iSize = (int)sizeof(DeviceLowPower);
	
	for(int i = 0;i < MAX_WEEK_DAYS; i++)
	{
		for(int k = 0; k <MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT; k++ )
		{
			
			tDevice.tDevLowPowerSchedule[i][k].iStartHour = m_tDevLowPowerSchedule[i][k].iStartHour;
			tDevice.tDevLowPowerSchedule[i][k].iStartMin  = m_tDevLowPowerSchedule[i][k].iStartMin;
			tDevice.tDevLowPowerSchedule[i][k].iStopHour  = m_tDevLowPowerSchedule[i][k].iStopHour;
			tDevice.tDevLowPowerSchedule[i][k].iStopMin   = m_tDevLowPowerSchedule[i][k].iStopMin;
		}
	}
	if(tDevice.iEnable)
	{
		CTime timeNow = CTime::GetCurrentTime();
		SYSTEMTIME tSysTime;
		GetLocalTime(&tSysTime);
		int iSystemWeek = tSysTime.wDayOfWeek;
		if(0 == iSystemWeek){
			iSystemWeek = 6;
		} else	{
			iSystemWeek = iSystemWeek - 1;
		}

		if(iWeekDay == iSystemWeek)
		{
			for (int i = 0;i < MAX_NUM_TIME_PERIODS; i++)
			{
				int iHour = timeNow.GetHour();
				int iMin  = timeNow.GetMinute();
				if(iHour >= tDevice.tDevLowPowerSchedule[iWeekDay][i].iStartHour && iHour <= tDevice.tDevLowPowerSchedule[iWeekDay][i].iStopHour )
				{
					if(iMin > tDevice.tDevLowPowerSchedule[iWeekDay][i].iStartMin)
					{
						MessageBox("This time period will cause the device to restart, and settings are not allowed");
						return;
					}
				}
			}	
		}
	}
	
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_LOWPOWER,m_iChannelNo,&tDevice,sizeof(DeviceLowPower));
	if(RET_SUCCESS != iRet){
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig[OnBnClickedLowpowerset] (%d, %d), error(%d)"
			, m_iLogonID, m_iChannelNo, GetLastError());
	}

}

void CLS_Wiegand::OnCbnSelchangeCmbweekday()
{
	InitTime();	
}

void CLS_Wiegand::OnBnClickedBtnget()
{
	DeviceLowPower tDevice = {0};
	tDevice.iSize = (int)sizeof(DeviceLowPower);
	tDevice.iEnable = m_chkLowPowerEnable.GetCheck();

	int iResult = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_LOWPOWER,m_iChannelNo,&tDevice,sizeof(DeviceLowPower),&iResult);
	if(RET_SUCCESS != iRet){
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig failed [OnBnClickedBtnget]! Logon id(%d)", m_iLogonID);
	}
	
	for(int i = 0;i < MAX_WEEK_DAYS;i++)
	{
		for(int k = 0; k < MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT; k++ )
		{

			m_tDevLowPowerSchedule[i][k].iStartHour = tDevice.tDevLowPowerSchedule[i][k].iStartHour;
			m_tDevLowPowerSchedule[i][k].iStartMin = tDevice.tDevLowPowerSchedule[i][k].iStartMin;
			m_tDevLowPowerSchedule[i][k].iStopHour = tDevice.tDevLowPowerSchedule[i][k].iStopHour;
			m_tDevLowPowerSchedule[i][k].iStopMin = tDevice.tDevLowPowerSchedule[i][k].iStopMin;
		}
	}
	
	CTime timeNow = CTime::GetCurrentTime();
	DayScheduleTimeEx *psTime = NULL;
	CTime timeStart, timeStop;
	int iWeekDays = m_cboWeekDay.GetCurSel();
	if(0 == iWeekDays){
		iWeekDays = 6;
	} else	{
		iWeekDays = iWeekDays - 1;
	}
	
	for(int k = 0 ;k < MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT; k++){
		psTime = &tDevice.tDevLowPowerSchedule[iWeekDays][k];
		timeStart = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), psTime->iStartHour, psTime->iStartMin, 0);
		timeStop  = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), psTime->iStopHour, psTime->iStopMin, 0);

		m_dtStartTime[k].SetTime(&timeStart);
		m_dtEndTime[k].SetTime(&timeStop);
	}

}





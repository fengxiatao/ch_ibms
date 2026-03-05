// CLS_DlgElevatorAllState.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgElevatorAllState.h"


// CLS_DlgElevatorAllState dialog
#define  MAX_ALARM_TYPE 15
char cElevatorAlarm[MAX_ALARM_TYPE][64]={"bit0:冲顶故障","bit1:蹲底故障","bit2:电梯异常急停","bit3:电梯超速","bit4:门区外停梯","bit5:到站后不开门",
"bit6:电梯晃动","bit7:开门走梯","bit8:困人","bit9:主动报警","bit10:传感器故障","bit11:电动车入梯","bit12:跌落","bit13:安全回路报警"};

//bit0:冲顶故障bit1:蹲底故障bit2:电梯异常急停bit3:电梯超速bit4:门区外停梯bit5:到站后不开门
		 //bit6:电梯晃动bit7:开门走梯bit8:困人bit9:主动报警bit10:传感器故障bit11:电动车入梯bit12:跌落bit13:安全回路报警


IMPLEMENT_DYNAMIC(CLS_DlgElevatorAllState, CDialog)

CLS_DlgElevatorAllState::CLS_DlgElevatorAllState(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgElevatorAllState::IDD, pParent)
	
	, m_iStateChannelNo(0)
	, m_iFloor(0)
	, m_iDirection(0)
	, m_iSpeed(0)
	, m_iTemprature(0)
	, m_iHumidity(0)
	, m_iRunTime(0)
	, m_iBindBrakeCn(0)
	, m_iOpenDoorCn(0)
	, m_iOperationMileage(0)
{

}

CLS_DlgElevatorAllState::~CLS_DlgElevatorAllState()
{
}

void CLS_DlgElevatorAllState::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT1, m_iStateChannelNo);
	DDX_Text(pDX, IDC_EDIT3, m_iFloor);
	DDX_Text(pDX, IDC_EDIT4, m_iDirection);
	DDX_Text(pDX, IDC_EDIT5, m_iSpeed);
	DDX_Text(pDX, IDC_EDIT6, m_iTemprature);
	DDX_Text(pDX, IDC_EDIT7, m_iHumidity);
	DDX_Control(pDX, IDC_COMBO_LEVELMODE2, m_cboBindBrake);
	DDX_Control(pDX, IDC_COMBO_OPENDOORMODE2, m_cboMaintenance);
	DDX_Control(pDX, IDC_COMBO_LEVELMODE, m_cboLeveling);
	DDX_Control(pDX, IDC_COMBO_OPENDOORMODE, m_cboOpendoor);
	DDX_Control(pDX, IDC_COMBO_CARSHSTOP, m_cboCarshStop);
	DDX_Control(pDX, IDC_COMBO_CARSHSTOPMODE, m_cboBodyInduction);
	DDX_Control(pDX, IDC_COMBO_PIRMODE, m_cboMainFloor);
	DDX_Text(pDX, IDC_EDIT21, m_iRunTime);
	DDX_Text(pDX, IDC_EDIT25, m_iBindBrakeCn);
	DDX_Text(pDX, IDC_EDIT29, m_iOpenDoorCn);
	DDX_Text(pDX, IDC_EDIT50, m_iOperationMileage);
	DDX_Text(pDX, IDC_EDIT_ACCELER, m_iAcceler);
	DDX_Text(pDX, IDC_EDIT31, m_cAlarmTime);
	DDX_Text(pDX, IDC_STATIC_ALARM, m_csAlarm);
	DDX_Text(pDX, IDC_EDIT_FLOORDISPLAY, m_csFloorDisplay);
}


BEGIN_MESSAGE_MAP(CLS_DlgElevatorAllState, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_STOREYINFO, &CLS_DlgElevatorAllState::OnBnClickedButtonStoreyinfo)
END_MESSAGE_MAP()


// CLS_DlgElevatorAllState message handlers

BOOL CLS_DlgElevatorAllState::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialogText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}



void CLS_DlgElevatorAllState::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
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
	Update_State();
}

void CLS_DlgElevatorAllState::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialogText();
}

void CLS_DlgElevatorAllState::Update_State()
{
	// TODO: Add your control notification handler code here
	ElevatorAllState tParam = {0};
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ELEVATOR_ALLSTATE, m_iChannelNo, &tParam, sizeof(tParam), &iBytesReturned);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig NET_CLIENT_ELEVATOR_ALLSTATE failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[NET_CLIENT_ELEVATOR_ALLSTATE] (%d, %d)", m_iLogonID, m_iChannelNo);
	}

	m_iStateChannelNo = tParam.iChannelNo;
	m_iFloor = tParam.iFloor;
	m_iDirection = tParam.iDirection;
	m_iSpeed = tParam.iSpeed;
	m_iTemprature = tParam.iTemperature;
	m_iHumidity = tParam.iHumidity;
	m_iAcceler = tParam.iAcceler;
	m_cboBindBrake.SetCurSel(tParam.iBindBrake);
	m_cboMaintenance.SetCurSel(tParam.iMaintenance);


	m_cboLeveling.SetCurSel(tParam.iLeveling);
	m_cboOpendoor.SetCurSel(tParam.iOpenDoor);
	m_cboCarshStop.SetCurSel(tParam.iCrashStop);
	m_cboBodyInduction.SetCurSel(tParam.iBodyInduction);
	m_cboMainFloor.SetCurSel(tParam.iMainFloor);


	m_iRunTime = tParam.iOperationTime;
	m_iBindBrakeCn = tParam.iBindBrakeCn;
	m_iOpenDoorCn = tParam.iOpenDoorCn;
	m_iOperationMileage = tParam.iOperationMileage;

	m_cAlarmTime = tParam.cAlarmTime;
	m_csFloorDisplay = tParam.cFloorDisplay;
	m_csAlarm = "";
	for (int i = 0; i < MAX_ALARM_TYPE ; i++)
	{
		CString csAlarmInfo;
		if(1 == tParam.iAlarmList[i])
		{
			csAlarmInfo.Format("%s\n",cElevatorAlarm[i]);
			m_csAlarm += csAlarmInfo;
		}

	}

	UpdateData(FALSE);

}

void CLS_DlgElevatorAllState::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_CHANNELNO,GetTextByLan(_T("通道号"), _T("ChannelNo")));
	SetDlgItemText(IDC_STATIC_FLOOR,GetTextByLan(_T("楼层码"), _T("FloorNo")));
	SetDlgItemText(IDC_STATIC_DIRCTION,GetTextByLan(_T("方向"), _T("Direction")));
	SetDlgItemText(IDC_STATIC_SPEED,GetTextByLan(_T("运行速度"), _T("Speed")));
	SetDlgItemText(IDC_STATIC_TEMPERATURE,GetTextByLan(_T("温度"), _T("Temprature")));
	SetDlgItemText(IDC_STATIC_HUMIDITY,GetTextByLan(_T("湿度"), _T("Humidity")));
	SetDlgItemText(IDC_STATIC_BINDBRAKE,GetTextByLan(_T("抱闸状态"), _T("BindBrake")));
	SetDlgItemText(IDC_STATIC_MAINTENANCE,GetTextByLan(_T("检修状态"), _T("Maintenance")));
	SetDlgItemText(IDC_STATIC_LEVELING,GetTextByLan(_T("平层状态"), _T("Leveling")));
	SetDlgItemText(IDC_STATIC_OPENDOORMODE,GetTextByLan(_T("轿门状态"), _T("Door")));
	SetDlgItemText(IDC_STATIC_MAINTENMODE,GetTextByLan(_T("急停状态"), _T("CrashStop")));
	SetDlgItemText(IDC_STATIC_BODYMODE,GetTextByLan(_T("人员检测状态"), _T("Body Induction")));
	SetDlgItemText(IDC_STATIC_MAINFLOOR,GetTextByLan(_T("基站状态"), _T("MainFloor")));
	
	SetDlgItemText(IDC_STATIC_STORY_CHANNEL,GetTextByLan(_T("运行时间"), _T("OperationTime")));
	SetDlgItemText(IDC_STATIC_STORY_BINDBRAKECN,GetTextByLan(_T("累计统计抱闸次数"), _T("BindBrakeCn")));
	SetDlgItemText(IDC_STATIC_OPENDOORCN,GetTextByLan(_T("累计统计开关门次数"), _T("OpenDoorCn")));
	SetDlgItemText(IDC_STATIC_OPERMIL,GetTextByLan(_T("运行距离"), _T("OperationMileage")));
	SetDlgItemText(IDC_BUTTON_STOREYINFO,GetTextByLan(_T("更新"), _T("Update")));
	SetDlgItemText(IDC_STATIC_ACCELER,GetTextByLan(_T("加速度"), _T("Acceler")));
	SetDlgItemText(IDC_STATIC_ALARMTIME,GetTextByLan(_T("报警时间"), _T("AlarmTime")));
	SetDlgItemText(IDC_STATIC_FLOORDISPLAY, GetTextByLan(_T("楼层信息显示"), _T("Floor infomation display")));

	InsertString(m_cboBindBrake,0,GetTextByLan(_T("0-报闸关闭"), _T("0-BindBrake Close")));
	InsertString(m_cboBindBrake,1,GetTextByLan(_T("1-报闸打开"), _T("1-BindBrake Open")));
	
	InsertString(m_cboMaintenance,0,GetTextByLan(_T("0-非检修状态"), _T("0-Maintenance")));
	InsertString(m_cboMaintenance,1,GetTextByLan(_T("1-检修状态"), _T("1-No Maintenance")));

	InsertString(m_cboLeveling,0,GetTextByLan(_T("0-不在平层"), _T("0-Not Leaving")));
	InsertString(m_cboLeveling,1,GetTextByLan(_T("1-在平层"), _T("1-On Leveling")));

	InsertString(m_cboOpendoor,0,GetTextByLan(_T("0-轿门关闭"), _T("0-Close Door")));
	InsertString(m_cboOpendoor,1,GetTextByLan(_T("1-轿门打开"), _T("1-Open Door")));
	
	InsertString(m_cboCarshStop,0,GetTextByLan(_T("0-正常"), _T("0-Normal")));
	InsertString(m_cboCarshStop,1,GetTextByLan(_T("1-急停"), _T("1-CrashStop")));

	InsertString(m_cboBodyInduction,0,GetTextByLan(_T("0-无人"), _T("0-No Body")));
	InsertString(m_cboBodyInduction,1,GetTextByLan(_T("1-有人"), _T("1-Have Body")));
	
	InsertString(m_cboMainFloor,0,GetTextByLan(_T("0-不在基站"), _T("0-Not MainFloor")));
	InsertString(m_cboMainFloor,1,GetTextByLan(_T("1-在基站"), _T("1-On MainFloor")));

}
void CLS_DlgElevatorAllState::OnBnClickedButtonStoreyinfo()
{
	Update_State();
}
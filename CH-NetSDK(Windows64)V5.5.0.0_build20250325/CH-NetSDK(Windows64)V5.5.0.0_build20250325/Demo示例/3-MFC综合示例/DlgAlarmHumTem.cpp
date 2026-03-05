
#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgAlarmHumTem.h"

#define MAX_INPUT_HUM_DATA_LEN		4
#define MAX_INPUT_TEM_DATA_LEN		5

#define TO_REAL(x)			(((x) - 1000.0)/10.0) 
#define TO_PROTOCOLS(x)		((x)*10 + 1000)

#define CONST_MIN_BRIGHT_SLIDER		25
#define CONST_MAX_BRIGHT_SLIDER		100
CString DoubleToString(double _dbPara)
{
	CString cstrString;
	cstrString.Format("%.1f", _dbPara);
	return cstrString;
}

double StringToDouble(CString _cstrPara)
{
	double dbTemp = atof(_cstrPara.GetBuffer());
	_cstrPara.ReleaseBuffer();
	return dbTemp;
}

IMPLEMENT_DYNAMIC(CLS_DlgAlarmHumTem, CDialog)

CLS_DlgAlarmHumTem::CLS_DlgAlarmHumTem(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgAlarmHumTem::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgAlarmHumTem::~CLS_DlgAlarmHumTem()
{
}

void CLS_DlgAlarmHumTem::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SENSOR_TYPE, m_choSensorType);
	DDX_Control(pDX, IDC_SLIDER_SCREEN_BRIGHT, m_ScreenBright);
	DDX_Control(pDX, IDC_CHECK_SWITCHCTRL, m_SwitchCtrl);
}


BEGIN_MESSAGE_MAP(CLS_DlgAlarmHumTem, CLS_BasePage)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_HUM_TEM_THRESHOLD_SET, &CLS_DlgAlarmHumTem::OnBnClickedBtnHumTemThresholdSet)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_DlgAlarmHumTem::OnBnClickedButtonSet)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_SCREEN_BRIGHT, &CLS_DlgAlarmHumTem::OnNMCustomdrawSliderScreenBright)
	ON_BN_CLICKED(IDC_CHECK_SWITCHCTRL, &CLS_DlgAlarmHumTem::OnBnClickedRadioSwitchctrl)
	ON_BN_CLICKED(IDC_BUTTON_STSYEM_SET, &CLS_DlgAlarmHumTem::OnBnClickedButtonStsyemSet)
	ON_CBN_SELCHANGE(IDC_COMBO_SENSOR_TYPE, &CLS_DlgAlarmHumTem::OnCbnSelchangeComboSensorType)
END_MESSAGE_MAP()

BOOL CLS_DlgAlarmHumTem::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	((CEdit*)GetDlgItem(IDC_EDT_HUM_LOW))->SetLimitText(MAX_INPUT_HUM_DATA_LEN);
	((CEdit*)GetDlgItem(IDC_EDT_HUM_UP))->SetLimitText(MAX_INPUT_HUM_DATA_LEN);
	((CEdit*)GetDlgItem(IDC_EDT_TEM_LOW))->SetLimitText(MAX_INPUT_TEM_DATA_LEN);
	((CEdit*)GetDlgItem(IDC_EDT_TEM_UP))->SetLimitText(MAX_INPUT_TEM_DATA_LEN);

	return TRUE;
}

void CLS_DlgAlarmHumTem::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		InitDialogItemText();
		UI_UpdateHumTemThreshold();
		UI_UpdateHumitureScreen();
	}
}

void CLS_DlgAlarmHumTem::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = (0 > _iChannelNo) ? 0 : _iChannelNo;
	m_iStreamNo = (0 > _iStreamNo) ? 0 : _iStreamNo;

	UI_UpdateHumTemThreshold();
}

void CLS_DlgAlarmHumTem::OnLanguageChanged( int _iLanguage )
{
	InitDialogItemText();
}

void CLS_DlgAlarmHumTem::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (_iLogonID != m_iLogonID)
	{
		return;
	}

	switch (_iParaType)
	{
	case PARA_ALARMTRIGGER_TEMPERATURE:
		UI_UpdateTemThreshold();
		break;
	case PARA_ALARMTRIGGER_HUMIDITY:
		UI_UpdateHumThreshold();
		break;
	default:
		break;
	}
}

void CLS_DlgAlarmHumTem::InitDialogItemText()
{
	SetDlgItemText(IDC_GPO_HUM_TEM_THRESHOLD, GetTextEx(IDS_HUM_TEM_THRESHOLD));
	SetDlgItemText(IDC_STC_TEM_THRESHOLD, GetTextEx(IDS_TEM_THRESHOLD));
	SetDlgItemText(IDC_STC_TEM_UP, GetTextEx(IDS_TEMP_UP) + _T("(degree)"));
	SetDlgItemText(IDC_STC_TEM_LOW, GetTextEx(IDS_TEM_LOW) + _T("(degree)"));
	SetDlgItemText(IDC_STC_HUM_THRESHOLD, GetTextEx(IDS_HUM_THRESHOLD));
	SetDlgItemText(IDC_STC_HUM_UP, GetTextEx(IDS_HUM_UP));
	SetDlgItemText(IDC_STC_HUM_LOW, GetTextEx(IDS_HUM_LOW));
	SetDlgItemText(IDC_BTN_HUM_TEM_THRESHOLD_SET, GetTextEx(IDS_SET));

	SetDlgItemText(IDC_STATIC_HUM_CORRECT, GetTextByLan("温湿度屏传感器数据校正值", "Humiture Screen Sensor Correct"));
	SetDlgItemText(IDC_STATIC_SENSOR_TYPE, GetTextByLan("传感器类型", "Sensor Type"));
	SetDlgItemText(IDC_STATIC_VALUE, GetTextByLan("校正值", "Adjusted Value"));

	m_choSensorType.ResetContent();
	m_choSensorType.InsertString(0,GetTextByLan(("温度"), ("Temperature")));
	m_choSensorType.InsertString(1,GetTextByLan(("湿度"), ("Humidity")));
	m_choSensorType.InsertString(2,GetTextByLan(("二氧化碳"), ("CO2")));
	m_choSensorType.InsertString(3,GetTextByLan(("PM2.5"), ("PM2.5")));
	m_choSensorType.InsertString(4,GetTextByLan(("TVOC"), ("TVOC")));
	m_choSensorType.InsertString(5,GetTextByLan(("甲醛"), ("Methanal")));
	m_choSensorType.SetCurSel(0);

	SetDlgItemText(IDC_STATIC_HUM_SYSYTEM, GetTextByLan("温湿度屏系统参数", "Humiture Screen System"));
	SetDlgItemText(IDC_CHECK_SWITCHCTRL, GetTextByLan("屏幕按键控制使能", "SwitchCtrl"));
	SetDlgItemText(IDC_STATIC_SCREEN_BRIGHT, GetTextByLan("屏幕亮度", "ScreenBright"));
	
	m_ScreenBright.SetRange(CONST_MIN_BRIGHT_SLIDER, CONST_MAX_BRIGHT_SLIDER);
}

void CLS_DlgAlarmHumTem::OnBnClickedBtnHumTemThresholdSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Set HumTemThreshold  LogonID < 0");
		return;
	}

	int iRet = RET_FAILED;


	CString cstrTemp;
	//Humidity Threshold
	TAlarmTriggerParam tHumThreshold = {0};
	tHumThreshold.iBuffSize = sizeof(TAlarmTriggerParam);
	tHumThreshold.iType = ALARM_TRIGGER_TYPE_HUMIDITY;
	GetDlgItemText(IDC_EDT_HUM_UP, cstrTemp);
	tHumThreshold.iValue = (int)(TO_PROTOCOLS(StringToDouble(cstrTemp)));
	cstrTemp.Empty();
	GetDlgItemText(IDC_EDT_HUM_LOW, cstrTemp);
	tHumThreshold.iValueEx = (int)(TO_PROTOCOLS(StringToDouble(cstrTemp)));
	iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_HUMIDITY, CMD_SET_ALARMTRIGGER, &tHumThreshold);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d,%d)", m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_HUMIDITY, CMD_SET_ALARMTRIGGER);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d,%d)", m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_HUMIDITY, CMD_SET_ALARMTRIGGER);
	}

	cstrTemp.Empty();

	//temperature threshold
	TAlarmTriggerParam tTempThreshold = {0};
	tTempThreshold.iBuffSize = sizeof(TAlarmTriggerParam);
	tTempThreshold.iType = ALARM_TRIGGER_TYPE_TEMPERATURE;
	GetDlgItemText(IDC_EDT_TEM_UP, cstrTemp);
	tTempThreshold.iValue = (int)(TO_PROTOCOLS(StringToDouble(cstrTemp)));
	cstrTemp.Empty();
	GetDlgItemText(IDC_EDT_TEM_LOW, cstrTemp);
	tTempThreshold.iValueEx = (int)(TO_PROTOCOLS(StringToDouble(cstrTemp)));
	iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_TEMPERATURE, CMD_SET_ALARMTRIGGER, &tTempThreshold);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d,%d)", m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_TEMPERATURE, CMD_SET_ALARMTRIGGER);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d,%d)", m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_TEMPERATURE, CMD_SET_ALARMTRIGGER);
	}

	return;

}

void CLS_DlgAlarmHumTem::UI_UpdateHumTemThreshold()
{
	UI_UpdateHumThreshold();
	UI_UpdateTemThreshold();
}

void CLS_DlgAlarmHumTem::UI_UpdateHumThreshold()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Update HumThreshold  LogonID < 0");
		return;
	}

	int iRet = RET_FAILED;

	//Humidity Threshold
	TAlarmTriggerParam tHumThreshold = {0};
	tHumThreshold.iBuffSize = sizeof(TAlarmTriggerParam);
	tHumThreshold.iType = ALARM_TRIGGER_TYPE_HUMIDITY;
	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_HUMIDITY, CMD_GET_ALARMTRIGGER, &tHumThreshold);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmConfig(%d,%d,%d,%d)", m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_HUMIDITY, CMD_GET_ALARMTRIGGER);
	}
	else
	{
		SetDlgItemText(IDC_EDT_HUM_UP, DoubleToString(TO_REAL(tHumThreshold.iValue)));
		SetDlgItemText(IDC_EDT_HUM_LOW, DoubleToString(TO_REAL(tHumThreshold.iValueEx)));
	}

	return;
}

void CLS_DlgAlarmHumTem::UI_UpdateTemThreshold()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Update TemThreshold  LogonID < 0");
		return;
	}

	int iRet = RET_FAILED;

	//temperature threshold
	TAlarmTriggerParam tTempThreshold = {0};
	tTempThreshold.iBuffSize = sizeof(TAlarmTriggerParam);
	tTempThreshold.iType = ALARM_TRIGGER_TYPE_TEMPERATURE;
	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_TEMPERATURE, CMD_GET_ALARMTRIGGER, &tTempThreshold);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmConfig(%d,%d,%d,%d)", m_iLogonID, m_iChannelNo, ALARM_TRIGGER_TYPE_TEMPERATURE, CMD_GET_ALARMTRIGGER);
	}
	else
	{
		SetDlgItemText(IDC_EDT_TEM_UP, DoubleToString(TO_REAL(tTempThreshold.iValue)));
		SetDlgItemText(IDC_EDT_TEM_LOW, DoubleToString(TO_REAL(tTempThreshold.iValueEx)));
	}

	return;
}

void CLS_DlgAlarmHumTem::UI_UpdateHumitureScreen()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Update TemThreshold  LogonID < 0");
		return;
	}

	int iRet = RET_FAILED;
	THMScreenDataCorrect tInfo = {0};
	tInfo.iType = m_choSensorType.GetCurSel();
	int iBytesReturned = -1;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_THMSCREEN_SENSORCORRECT, 0x7FFFFFFF, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (0 == iRet)
	{
		SetDlgItemInt(IDC_EDIT_VALUE, tInfo.iValue);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig NET_CLIENT_THMSCREEN_SENSORCORRECT(%d,%d)",m_iLogonID,m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig NET_CLIENT_THMSCREEN_SENSORCORRECT(%d,%d), error(%d)",m_iLogonID, m_iChannelNo,GetLastError());
	}

	THMScreenSystem tParam = {0};
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_THMSCREEN_SYSTEM, 0x7FFFFFFF, &tParam, sizeof(tParam), &iBytesReturned);
	if (0 == iRet)
	{
		m_SwitchCtrl.SetCheck(tParam.iSwitchCtrl);
		m_ScreenBright.SetPos(tParam.iScreenBright);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig NET_CLIENT_THMSCREEN_SYSTEM(%d,%d)",m_iLogonID,m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig NET_CLIENT_THMSCREEN_SYSTEM(%d,%d), error(%d)",m_iLogonID, m_iChannelNo,GetLastError());
	}

	return;
}
void CLS_DlgAlarmHumTem::OnBnClickedButtonSet()
{
	THMScreenDataCorrect tInfo = {0};
	tInfo.iType = m_choSensorType.GetCurSel();
	tInfo.iValue = GetDlgItemInt(IDC_EDIT_VALUE);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_THMSCREEN_SENSORCORRECT, 0, &tInfo, sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig NET_CLIENT_THMSCREEN_SENSORCORRECT(%d,%d,%d)",m_iLogonID, tInfo.iType,tInfo.iValue);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig NET_CLIENT_THMSCREEN_SENSORCORRECT(%d,%d,%d)",m_iLogonID, tInfo.iType,tInfo.iValue);

	}
}

void CLS_DlgAlarmHumTem::OnNMCustomdrawSliderScreenBright(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int pos = m_ScreenBright.GetPos();
	SetDlgItemInt(IDC_STATIC_BRIGHT_VALUE, pos);
	*pResult = 0;
}

void CLS_DlgAlarmHumTem::OnBnClickedRadioSwitchctrl()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d), error(%d)", m_iLogonID, GetLastError());
		return;
	}

	THMScreenSystem tInfo = {0};
	tInfo.iScreenBright = m_ScreenBright.GetPos();
	tInfo.iSwitchCtrl = m_SwitchCtrl.GetCheck();
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_THMSCREEN_SYSTEM
		, m_iChannelNo, &tInfo, sizeof(tInfo));
	if (iRet == 0)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_THMSCREEN_SYSTEM] (%d, %d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig[NET_CLIENT_THMSCREEN_SYSTEM] (%d, %d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}	
}

void CLS_DlgAlarmHumTem::OnBnClickedButtonStsyemSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d), error(%d)", m_iLogonID, GetLastError());
		return;
	}

	THMScreenSystem tInfo = {0};
	tInfo.iScreenBright = m_ScreenBright.GetPos();
	tInfo.iSwitchCtrl = m_SwitchCtrl.GetCheck();
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_THMSCREEN_SYSTEM
		, m_iChannelNo, &tInfo, sizeof(tInfo));
	if (iRet == 0)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_THMSCREEN_SYSTEM] (%d, %d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig[NET_CLIENT_THMSCREEN_SYSTEM] (%d, %d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
}

void CLS_DlgAlarmHumTem::OnCbnSelchangeComboSensorType()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Update TemThreshold  LogonID < 0");
		return;
	}

	int iRet = RET_FAILED;
	THMScreenDataCorrect tInfo = {0};
	tInfo.iType = m_choSensorType.GetCurSel();
	int iBytesReturned = -1;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_THMSCREEN_SENSORCORRECT, m_iChannelNo, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (0 == iRet)
	{
		SetDlgItemInt(IDC_EDIT_VALUE, tInfo.iValue);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig NET_CLIENT_THMSCREEN_SENSORCORRECT(%d,%d)",m_iLogonID,m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig NET_CLIENT_THMSCREEN_SENSORCORRECT(%d,%d), error(%d)",m_iLogonID, m_iChannelNo,GetLastError());
	}
}

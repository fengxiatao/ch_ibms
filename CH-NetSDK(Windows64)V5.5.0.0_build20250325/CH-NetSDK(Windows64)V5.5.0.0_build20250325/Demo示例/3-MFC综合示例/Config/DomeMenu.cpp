// DomeMenu.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DomeMenu.h"


// CLS_DomeMenu dialog

IMPLEMENT_DYNAMIC(CLS_DomeMenu, CDialog)

CLS_DomeMenu::CLS_DomeMenu(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DomeMenu::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iStreamNO = 0;
}

CLS_DomeMenu::~CLS_DomeMenu()
{
}

void CLS_DomeMenu::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_DOME_TYPE, m_cboDomeType);
	DDX_Control(pDX, IDC_EDIT_DOME_PARA1, m_edtDomePara[0]);
	DDX_Control(pDX, IDC_EDIT_DOME_PARA2, m_edtDomePara[1]);
	DDX_Control(pDX, IDC_EDIT_DOME_PARA3, m_edtDomePara[2]);
	DDX_Control(pDX, IDC_EDIT_DOME_PARA4, m_edtDomePara[3]);
	DDX_Control(pDX, IDC_CHECK_PSM_ENABLE, m_chkPsmEnable);
	DDX_Control(pDX, IDC_EDIT_Infrared_light_on_time, m_edtInfraredLightOnTime);
	DDX_Control(pDX, IDC_EDIT_Infrared_light_off_time, m_edtInfraredLightOffTime);
	DDX_Control(pDX, IDC_COMBO_MODULETYPE, m_cboModuleType);
}


BEGIN_MESSAGE_MAP(CLS_DomeMenu, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_DOME_PARA_SET, &CLS_DomeMenu::OnBnClickedButtonDomeParaSet)
	ON_CBN_SELCHANGE(IDC_COMBO_DOME_TYPE, &CLS_DomeMenu::OnCbnSelchangeComboDomeType)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_SET_PSM, &CLS_DomeMenu::OnBnClickedButtonSetPsm)
	ON_CBN_SELCHANGE(IDC_COMBO_MODULETYPE, &CLS_DomeMenu::OnCbnSelchangeComboModuletype)
END_MESSAGE_MAP()


// CLS_DomeMenu message handlers

BOOL CLS_DomeMenu::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialog();
	for (int i = 0; i < 4; i++)
	{
		m_edtDomePara[i].SetLimitText(64);
	}

	m_edtInfraredLightOnTime.SetLimitText(LEN_64);
	m_edtInfraredLightOffTime.SetLimitText(LEN_64);

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_DomeMenu::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;

	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo =  _iChannelNo;
	}

	if (_iStreamNo < 0)
	{
		m_iStreamNO = 0;
	}
	else
	{
		m_iStreamNO = _iStreamNo;
	}
	UpdataLedPowerLimit();
	OnCbnSelchangeComboModuletype();
}

void CLS_DomeMenu::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();
}

void CLS_DomeMenu::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		return;
	}

	int iParaType = m_cboDomeType.GetCurSel() + 1;
	TDomeParam DomeParam = {0};
	int iBytesReturned = 0;
	DomeParam.iType = iParaType;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DOME_MENU, m_iChannelNo, &DomeParam, sizeof(TDomeParam), &iBytesReturned);
	if (iRet < 0 )
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DomeMenu::UpdatePageU] Get fail,error = %d", GetLastError());
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_DOME_PARA1, DomeParam.iParam1);
		SetDlgItemInt(IDC_EDIT_DOME_PARA2, DomeParam.iParam2);
		SetDlgItemInt(IDC_EDIT_DOME_PARA3, DomeParam.iParam3);
		SetDlgItemInt(IDC_EDIT_DOME_PARA4, DomeParam.iParam4);
	}

	DomeParam.iType = DOME_PARA_POWER_SAVING_MODE;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DOME_MENU, m_iChannelNo, &DomeParam, sizeof(TDomeParam), &iBytesReturned);
	if (0 == iRet)
	{
		if (DomeParam.iParam1 >= 0)
		{
			m_chkPsmEnable.SetCheck(DomeParam.iParam1);
		}
		SetDlgItemInt(IDC_EDIT_Infrared_light_on_time, DomeParam.iParam2);
		SetDlgItemInt(IDC_EDIT_Infrared_light_off_time, DomeParam.iParam3);
	}
}

void CLS_DomeMenu::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	switch(_iParaType)
	{
	case PARA_DOME_MENU:
		UpdatePageUI();
		break;
	default:
		break;
	}
}

void CLS_DomeMenu::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_DOME_PARA_TYPE, IDS_CONFIG_ITS_TIMERANGE_TYPE);
	SetDlgItemTextEx(IDC_STATIC_DOME_PARA1, IDS_CONFIG_ITS_TIMERANGE_PARAM1);
	SetDlgItemTextEx(IDC_STATIC_DOME_PARA2, IDS_CONFIG_ITS_TIMERANGE_PARAM2);
	SetDlgItemTextEx(IDC_STATIC_DOME_PARA3, IDS_CONFIG_ITS_TIMERANGE_PARAM3);
	SetDlgItemTextEx(IDC_STATIC_DOME_PARA4, IDS_CONFIG_ITS_TIMERANGE_PARAM4);
	SetDlgItemTextEx(IDC_BUTTON_DOME_PARA_SET, IDS_SET);
	SetDlgItemText(IDC_STATIC_LEDPOWER_LIMIT, GetTextByLan(_T("补光灯功耗限制参数"), _T("Power consumption limit parameters of fill lamp")));
	SetDlgItemText(IDC_STATIC_NEARLEDPOWER, GetTextByLan(_T("近灯单位亮度功耗(毫瓦)"), _T("Unit brightness power consumption of near lamp(mw)")));
	SetDlgItemText(IDC_STATIC_FARLEDPOWER, GetTextByLan(_T("远灯单位亮度功耗(毫瓦)"), _T("Unit brightness power consumption of far lamp(mw)")));
	SetDlgItemText(IDC_STATIC_BASEPOWER, GetTextByLan(_T("系统基本功耗(毫瓦)"), _T("Basic power consumption of the system(mw)")));
	SetDlgItemText(IDC_STATIC_BASEPOWERRATIO, GetTextByLan(_T("系统基本功耗系数(毫瓦)"), _T("Basic power consumption coefficient of the system(mw)")));
	SetDlgItemText(IDC_STATIC_OTHERPOWER, GetTextByLan(_T("其他功耗(毫瓦)"), _T("Other power consumption(mw)")));
	SetDlgItemText(IDC_STATIC_OTHERRATIO, GetTextByLan(_T("其他系数(毫瓦)"), _T("Other coefficient(mw)")));
	SetDlgItemText(IDC_STATIC_LIMITPOWER, GetTextByLan(_T("限制功耗值(毫瓦)"), _T("Limited power consumption(mw)")));

	SetDlgItemText(IDC_STATIC_MODULEPOWER_LIMIT, GetTextByLan(_T("模块功耗限制参数"), _T("Module power limit parameters")));
	SetDlgItemText(IDC_STATIC_TOTALLIMITPOWER, GetTextByLan(_T("限制总功耗值"), _T("Limited total power consumption")));
	SetDlgItemText(IDC_STATIC_MODULETYPE, GetTextByLan(_T("模块类型"), _T("Module type")));
	SetDlgItemText(IDC_STATIC_MODULELIMIT1, GetTextByLan(_T("模块功耗系数1"), _T("Module power consumption coefficient 1")));
	SetDlgItemText(IDC_STATIC_MODULELIMIT2, GetTextByLan(_T("模块功耗系数2"), _T("Module power consumption coefficient 2")));
	SetDlgItemText(IDC_STATIC_MODULELIMIT3, GetTextByLan(_T("模块功耗系数3"), _T("Module power consumption coefficient 3")));
	SetDlgItemText(IDC_STATIC_MODULELIMIT4, GetTextByLan(_T("模块功耗系数4"), _T("Module power consumption coefficient 4")));
	SetDlgItemText(IDC_STATIC_LIGHT_iD, GetTextByLan(_T("灯组id"), _T("LightId")));
	m_cboDomeType.ResetContent();
	//1--Preset title display time; 2--Auto function title display time; 3--Region title display time; 4--Coordinate direction display time;
	//5--Tracking point title display time; 6--Title background; 7--Auto stop time; 8--Menu closing time; 9--Vertical angle adjustment; 10--Control speed level;
	m_cboDomeType.InsertString(0, "1--"+GetTextEx(IDS_PRESET_TITLE_DISPLAY_TIME));
	m_cboDomeType.InsertString(1, "2--"+GetTextEx(IDS_AUTOMATIC_FUNCTION_TITLE_DISPLAY_TIME));
	m_cboDomeType.InsertString(2, "3--"+GetTextEx(IDS_REGION_TITLE_DISPLAY_TIME));
	m_cboDomeType.InsertString(3, "4--"+GetTextEx(IDS_COORDINATE_DIRECTION_DISPLAY_TIME));
	m_cboDomeType.InsertString(4, "5--"+GetTextEx(IDS_TRACEPOINTS_DISPLAY_TIME));
	m_cboDomeType.InsertString(5, "6--"+GetTextEx(IDS_TITLE_BACKGROUND));
	m_cboDomeType.InsertString(6, "7--"+GetTextEx(IDS_AUTOMATIC_STOP_TIME));
	m_cboDomeType.InsertString(7, "8--"+GetTextEx(IDS_MENU_OFF_TIME));
	m_cboDomeType.InsertString(8, "9--"+GetTextEx(IDS_VERTICAL_ANGLE_ADJUSTMENT));
	m_cboDomeType.InsertString(9, "10--"+GetTextEx(IDS_MANIPULATION_SPEED_RATING));
	//11--Preset speed level; 12--Temperature control mode; 13--485 address setting; 14--Zero setting; 15--North setting;
	//16--control mode; 17--sensitivity threshold; 18--delay time; 19--zoom matching; 20--preset;
	m_cboDomeType.InsertString(10, "11--"+GetTextEx(IDS_PRESET_SPEED_RATING));
	m_cboDomeType.InsertString(11, "12--"+GetTextEx(IDS_TEMPERATURE_CONTROL_MODE));
	m_cboDomeType.InsertString(12, "13--"+GetTextEx(IDS_485_ADDRESS_SETTING));
	m_cboDomeType.InsertString(13, "14--"+GetTextEx(IDS_ZERO_SETTING));
	m_cboDomeType.InsertString(14, "15--"+GetTextEx(IDS_NORTH_SETTING));
	m_cboDomeType.InsertString(15, "16--"+GetTextEx(IDS_CONTROL_MODE));
	m_cboDomeType.InsertString(16, "17--"+GetTextEx(IDS_SENSITIVE_THRESHOLD));
	m_cboDomeType.InsertString(17, "18--"+GetTextEx(IDS_DELAY_TIME));
	m_cboDomeType.InsertString(18, "19--"+GetTextEx(IDS_ZOOM_MATCH));
	m_cboDomeType.InsertString(19, "20--"+GetTextEx(IDS_PRESET));
	//21--scan; 22--mode path; 23--mode path current state; 24--area indication; 25--zoom speed; 26--digital zoom;
	//27--Preset freeze 28--Laser brightness threshold; 29--Laser coaxial setting; 30--Set visible beam open time;
	m_cboDomeType.InsertString(20, "21--"+GetTextEx(IDS_SCANNING));
	m_cboDomeType.InsertString(21, "22--"+GetTextEx(IDS_SCHEMA_PATH));
	m_cboDomeType.InsertString(22, "23--"+GetTextEx(IDS_SCHEMA_PATH_CURRENT_STATE));
	m_cboDomeType.InsertString(23, "24--"+GetTextEx(IDS_REGIONAL_INDICATIVE));
	m_cboDomeType.InsertString(24, "25--"+GetTextEx(IDS_ZOOM_SPEED));
	m_cboDomeType.InsertString(25, "26--"+GetTextEx(IDS_DIGITAL_ZOOM));
	m_cboDomeType.InsertString(26, "27--"+GetTextEx(IDS_PRESET_FROZEN));
	m_cboDomeType.InsertString(27, "28--"+GetTextEx(IDS_LASER_LIGHT));
	m_cboDomeType.InsertString(28, "29--"+GetTextEx(IDS_LASER_COAXIAL));
	m_cboDomeType.InsertString(29, "30--"+GetTextEx(IDS_VISIBLE_LIGHT_OPENTIME));
	//31--keying limit setting; 32--power-down memory mode; 33-- PTZ priority; 34--keying limit enable
	m_cboDomeType.InsertString(30, "31--"+GetTextEx(IDS_KEYING_LIMIT));
	m_cboDomeType.InsertString(31, "32--"+GetTextEx(IDS_OUTAGE_MEMORY));
	m_cboDomeType.InsertString(32, "33--"+GetTextEx(IDS_PTZ_PRIOR));
	m_cboDomeType.InsertString(33, "34--"+GetTextEx(IDS_KEYING_USING));
	//35-light control mode;36-white light control mode;37-laser control mode;38-water conservancy ball power saving parameters
	m_cboDomeType.InsertString(34, "35--"+GetTextByLan(_T("灯光控制模式"), _T("Light control mode")));
	m_cboDomeType.InsertString(35, "36--"+GetTextByLan(_T("白光控制方式"), _T("White light control method")));
	m_cboDomeType.InsertString(36, "37--"+GetTextByLan(_T("激光器控制方式"), _T("Laser control method")));
	m_cboDomeType.InsertString(37, "38--"+GetTextByLan(_T("水利球省电模式"), _T("Power saving mode")));
	m_cboDomeType.InsertString(38, "39--"+GetTextByLan(_T("打点激光器控制设置"), _T("Dot laser control setting")));
	m_cboDomeType.InsertString(39, "40--"+GetTextByLan(_T("设备倾斜角设置"), _T("Equipment inclination setting")));
	m_cboDomeType.SetCurSel(0);

	SetDlgItemText(IDC_STATIC_WATER_PROJ_PSM, GetTextByLan(_T("省电模式"), _T("Power saving mode")));
	SetDlgItemText(IDC_CHECK_PSM_ENABLE, GetTextByLan(_T("使能"), _T("Enable")));
	SetDlgItemText(IDC_STATIC_Infrared_light_on_time, GetTextByLan(_T("红外灯开启时长"), _T("Infrared light on time")));
	SetDlgItemText(IDC_STATIC_Infrared_light_off_time, GetTextByLan(_T("红外灯关闭时长"), _T("Infrared light off time")));
	SetDlgItemText(IDC_BUTTON_SET_PSM, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_PSM_SECOND, GetTextByLan(_T("单位:秒，默认:600，有效值>=60"), _T("Unit:second,default:600,valid value>=60")));
	SetDlgItemText(IDC_STATIC_PSM_SECOND2, GetTextByLan(_T("单位:秒，默认:3600，有效值>=60"), _T("Unit:second,default:3600,valid value>=60")));

	int iUseRule = GetDemoUseRule();
	if (RIVER_USE == iUseRule)
	{
		GetDlgItem(IDC_STATIC_WATER_PROJ_PSM)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_CHECK_PSM_ENABLE)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_Infrared_light_on_time)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_Infrared_light_off_time)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_Infrared_light_on_time)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_Infrared_light_off_time)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_PSM_SECOND)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_PSM_SECOND2)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_BUTTON_SET_PSM)->ShowWindow(SW_SHOW);
	}
	else
	{
		GetDlgItem(IDC_STATIC_WATER_PROJ_PSM)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_CHECK_PSM_ENABLE)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_Infrared_light_on_time)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_Infrared_light_off_time)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_Infrared_light_on_time)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_Infrared_light_off_time)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_PSM_SECOND)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_PSM_SECOND2)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_BUTTON_SET_PSM)->ShowWindow(SW_HIDE);
	}
	int iCurSel = m_cboModuleType.GetCurSel();
	m_cboModuleType.ResetContent();
	m_cboModuleType.SetItemData(m_cboModuleType.AddString(GetTextByLan(_T("红外灯"),_T("Infrared lamp"))), 0);
	m_cboModuleType.SetItemData(m_cboModuleType.AddString(GetTextByLan(_T("白光灯"),_T("White light"))), 1);
	m_cboModuleType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
}

void CLS_DomeMenu::OnBnClickedButtonDomeParaSet()
{
	// TODO: Add your control notification handler code here
	int iParaTye = m_cboDomeType.GetCurSel() + 1;
	TDomeParam TDomeParam = {0};
	TDomeParam.iType = iParaTye;
	TDomeParam.iParam1 = GetDlgItemInt(IDC_EDIT_DOME_PARA1);
	TDomeParam.iParam2 = GetDlgItemInt(IDC_EDIT_DOME_PARA2);
	TDomeParam.iParam3 = GetDlgItemInt(IDC_EDIT_DOME_PARA3);
	TDomeParam.iParam4 = GetDlgItemInt(IDC_EDIT_DOME_PARA4);

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DOME_MENU, m_iChannelNo, &TDomeParam, sizeof(TDomeParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DomeMenu::OnBnClickedButtonDomeParaSet()] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_DomeMenu::OnBnClickedButtonDomeParaSet()] set success!");
	}
}

void CLS_DomeMenu::OnCbnSelchangeComboDomeType()
{
	// TODO: Add your control notification handler code here
	UpdatePageUI();
}

void CLS_DomeMenu::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	// TODO: Add your message handler code here
	UpdatePageUI();
}

void CLS_DomeMenu::OnBnClickedButtonSetPsm()
{
	TDomeParam TDomeParam = {0};
	TDomeParam.iType = DOME_PARA_POWER_SAVING_MODE;
	TDomeParam.iParam1 = m_chkPsmEnable.GetCheck();
	TDomeParam.iParam2 = GetDlgItemInt(IDC_EDIT_Infrared_light_on_time);
	TDomeParam.iParam3 = GetDlgItemInt(IDC_EDIT_Infrared_light_off_time);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DOME_MENU, m_iChannelNo, &TDomeParam, sizeof(TDomeParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DomeMenu::OnBnClickedButtonSetPsm] Set DOME_PARA_POWER_SAVING_MODE fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_DomeMenu::OnBnClickedButtonSetPsm] Set DOME_PARA_POWER_SAVING_MODE success!");
	}
}

void CLS_DomeMenu::UpdataLedPowerLimit()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iReturn = 0;
	LedPowerLimit tLedPowerLimit = {0};
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_LEDPOWER_LIMIT,m_iChannelNO, &tLedPowerLimit, sizeof(LedPowerLimit), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_LEDPOWER_LIMIT fail!");
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_NEARLEDPOWER, tLedPowerLimit.iNearLEDPower);
		SetDlgItemInt(IDC_EDIT_FARLEDPOWER, tLedPowerLimit.iFarLEDPower);
		SetDlgItemInt(IDC_EDIT_BASEPOWER, tLedPowerLimit.iBasePower);
		SetDlgItemInt(IDC_EDIT_BASEPOWERRATIO, tLedPowerLimit.iBasePowerRatio);
		SetDlgItemInt(IDC_EDIT_OTHERPOWER, tLedPowerLimit.iOtherPower);
		SetDlgItemInt(IDC_EDIT_OTHERRATIO, tLedPowerLimit.iOtherRatio);
		SetDlgItemInt(IDC_EDIT_LIMITPOWER, tLedPowerLimit.iLimitPower);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_LEDPOWER_LIMIT success!");
	}
}

void CLS_DomeMenu::OnCbnSelchangeComboModuletype()
{
	// TODO: Add control notification handler code here
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iReturn = 0;
	ModulePowerLimit tModulePowerLimit = {0};
	tModulePowerLimit.iModuleType = m_cboModuleType.GetCurSel();
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_MODULEPOWER_LIMIT,m_iChannelNO, &tModulePowerLimit, sizeof(ModulePowerLimit), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_MODULEPOWER_LIMIT fail!");
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_TOTALLIMITPOWER, tModulePowerLimit.iLimitPower);
		SetDlgItemInt(IDC_EDIT_MODULELIMIT1, tModulePowerLimit.iPowerRatio1);
		SetDlgItemInt(IDC_EDIT_MODULELIMIT2, tModulePowerLimit.iPowerRatio2);
		SetDlgItemInt(IDC_EDIT_MODULELIMIT3, tModulePowerLimit.iPowerRatio3);
		SetDlgItemInt(IDC_EDIT_MODULELIMIT4, tModulePowerLimit.iPowerRatio4);
		SetDlgItemInt(IDC_EDIT_LIGHT_ID, tModulePowerLimit.iLightID);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_MODULEPOWER_LIMIT success!");
	}
}

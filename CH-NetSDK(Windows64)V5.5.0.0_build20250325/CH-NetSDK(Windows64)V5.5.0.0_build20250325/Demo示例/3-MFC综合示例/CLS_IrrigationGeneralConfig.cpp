// CLS_GeneralConfiguration.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_IrrigationGeneralConfig.h"
#include "NetClientTypes.h"

// CLS_GeneralConfiguration dialog

#define WATERLEVELSOURCE_TYPE_MANUAL			1
#define WATERLEVELSOURCE_TYPE_NETWORK			2
#define WATERLEVELSOURCE_TYPE_PERIPHERAL		3
IMPLEMENT_DYNAMIC(CLS_IrrigationGeneralConfig, CDialog)

CLS_IrrigationGeneralConfig::CLS_IrrigationGeneralConfig(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_IrrigationGeneralConfig::IDD, pParent)
{

}

CLS_IrrigationGeneralConfig::~CLS_IrrigationGeneralConfig()
{
}

void CLS_IrrigationGeneralConfig::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_IRRIGATION_PARA_REPORT_TYPE, m_cboIrrigationParaReportType);
	DDX_Control(pDX, IDC_CHECK_OVERLY_LED, m_chkOverlyLed);
	DDX_Control(pDX, IDC_CHECK_OVERLY_VIDEO, m_chkOverlyVideo);
	DDX_Control(pDX, IDC_CHECK_RAINFALL_ALERT, m_chkRainfallAlertEnbale);
	DDX_Control(pDX, IDC_CHECK_ALERT_WATER_LEVEL, m_chkWaterLevelAlert);
	DDX_Control(pDX, IDC_CHECK_HORN_ENABLE, m_chkHornEnable);
	DDX_Control(pDX, IDC_COMBO_PORTNO, m_cboPortNo);
	DDX_Control(pDX, IDC_CHECK_IRR_OSD_QUALITY, m_cbkOSDQuality);
	DDX_Control(pDX, IDC_COMBO_WATERLEVELSOURCE_TYPE, m_combo_waterlevelsource_type);
	DDX_Control(pDX, IDC_EDIT_WATERLEVELSOURCE_VALUE, m_edit_waterlevelsource_value);
	DDX_Control(pDX, IDC_COMBO_WATER_SPEED_SENCE_ID, m_cboSenceId);
}


BEGIN_MESSAGE_MAP(CLS_IrrigationGeneralConfig, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET_IRRIGATION_PARA_OVERLY_CONF, &CLS_IrrigationGeneralConfig::OnBnClickedButtonSetIrrigationParaOverlyConf)
	ON_CBN_SELCHANGE(IDC_COMBO_IRRIGATION_PARA_REPORT_TYPE, &CLS_IrrigationGeneralConfig::OnCbnSelchangeComboIrrigationParaReportType)
	ON_BN_CLICKED(IDC_BUTTON_COMMENABLE_SET, &CLS_IrrigationGeneralConfig::OnBnClickedButtonCommenableSet)
	ON_BN_CLICKED(IDC_CHECK_HORN_ENABLE, &CLS_IrrigationGeneralConfig::OnBnClickedCheckHornEnable)
	ON_BN_CLICKED(IDC_CHECK_IRR_OSD_QUALITY, &CLS_IrrigationGeneralConfig::OnBnClickedCheckIrrOsdQuality)
	ON_CBN_SELCHANGE(IDC_COMBO_WATERLEVELSOURCE_TYPE, &CLS_IrrigationGeneralConfig::OnCbnSelchangeComboType)
	ON_BN_CLICKED(IDC_BUTTON_WATERLEVELSOURCE_SET, &CLS_IrrigationGeneralConfig::OnBnClickedButtonWaterlevelsourceSet)
	ON_BN_CLICKED(IDC_BUTTON_WATERLEVELSOURCE_ID_SET, &CLS_IrrigationGeneralConfig::OnBnClickedButtonWaterlevelsourceIdSet)
	ON_BN_CLICKED(IDC_BUTTON_WATER_SPEED_SEND, &CLS_IrrigationGeneralConfig::OnBnClickedButtonWaterSpeedSend)
END_MESSAGE_MAP()


// CLS_GeneralConfiguration message handler
BOOL CLS_IrrigationGeneralConfig::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUI();

	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_IrrigationGeneralConfig::UpdateUI()
{
	SetDlgItemText(IDC_GROUP_BOX_OVERLY_CONF, GetTextByLan(_T("叠加配置"), _T("OverlyConfig")));
	SetDlgItemText(IDC_STATIC_IRRIGATION_PARA_REPORT_TYPE, GetTextByLan(_T("水利信息上报类型"), _T("IrrigationInfoReportType")));
	SetDlgItemText(IDC_CHECK_OVERLY_LED, GetTextByLan(_T("叠加LED"), _T("Overly LED")));
	SetDlgItemText(IDC_CHECK_OVERLY_VIDEO, GetTextByLan(_T("叠加视频  (视频最多同时叠加5个数据)"), _T("Overly Video  (Video can stack up to 5 data at the same time)")));
	SetDlgItemText(IDC_BUTTON_SET_IRRIGATION_PARA_OVERLY_CONF, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_COMMON_ENABLE, GetTextByLan(_T("通用使能"), _T("CommonEnable")));
	SetDlgItemText(IDC_CHECK_RAINFALL_ALERT, GetTextByLan(_T("雨量预警"), _T("RainfallAlert")));
	SetDlgItemText(IDC_CHECK_ALERT_WATER_LEVEL, GetTextByLan(_T("警戒水位预警"), _T("WaterLevelAlert")));
	SetDlgItemText(IDC_STATIC_TIMEINTERVAL, GetTextByLan(_T("电瓶电量上传间隔(0-1440)"), _T("PowerUploadInner(0-1440)")));
	SetDlgItemText(IDC_STATIC_UNIT, GetTextByLan(_T("单位: 分钟"), _T("minutes")));
	SetDlgItemText(IDC_BUTTON_COMMENABLE_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_CHECK_HORN_ENABLE, GetTextByLan(_T("喇叭继电器端口输出端口号"),_T("Conservancy Horn Relay Port")));
	m_cboIrrigationParaReportType.ResetContent();
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("雨量"), _T("Rainfall"))), IRRIGATION_TYPE_RAINFALL);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("降雨时长"), _T("RainTime"))), IRRIGATION_TYPE_RAINDURATION);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("水位"), _T("WaterLevel"))), IRRIGATION_TYPE_WATERLEVEL);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("积水深度"), _T("RainAccumulateDepth"))), IRRIGATION_TYPE_SEDIMENT);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("超警戒水位"), _T("SuperAlertWaterLevel"))), IRRIGATION_TYPE_ALERTWATERLEVEL);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("流速"), _T("Current Speed "))), IRRIGATION_TYPE_FLOWRATEVALUE);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("电瓶剩余电量"), _T("Remaining Battery Capacity"))), IRRIGATION_TYPE_BATTERYVOLTAGE);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("气压"), _T("Air pressure"))), IRRIGATION_TYPE_AIRPRESSURE);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("风速"), _T("Wind speed"))), IRRIGATION_TYPE_WINDSPEED);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("风向"), _T("wind direction"))), IRRIGATION_TYPE_WINDDIRECTION);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("温度"), _T("Temperature"))), IRRIGATION_TYPE_TEMPERATURE);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("湿度"), _T("Humidity"))), IRRIGATION_TYPE_HUMIDITY);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("酸碱度"),_T("Acidity and alkal"))),IRRIGATION_TYPE_ACIDITYANDALKAL);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("溶解氧"),_T("Dissolve Oxygen"))),IRRIGATION_TYPE_DISSOLVEOXYGEN);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("氧化还原"),_T("Oxygen Reduction"))),IRRIGATION_TYPE_OXYREDUCTION);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("GPS"), _T("GPS"))),IRRIGATION_TYPE_GPS);	
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("高程"), _T("RTX"))),IRRIGATION_TYPE_RTX);	
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("浊度"), _T("Turbidity"))),IRRIGATION_TYPE_TURBIDITY);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("氨氮"), _T("Ammonica"))),IRRIGATION_TYPE_AMMONICA);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("水温"), _T("Water temp"))),IRRIGATION_TYPE_WATERTEMP);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("电导率"), _T("Conductivity"))),IRRIGATION_TYPE_CONDUCTIVITY);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("化学需氧量"), _T("Oxydemand"))),IRRIGATION_TYPE_OXYDEMAND);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("总氮"), _T("Nitrogen"))),IRRIGATION_TYPE_NITROGEN);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("总磷"), _T("Phosphorus"))),IRRIGATION_TYPE_PHOSPHORUS);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("压力水位"), _T("Pressure WaterLevel"))),IRRIGATION_TYPE_PRESSURE_WATERLEVEL);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("流速场"), _T("Water Speed Field"))),IRRIGATION_TYPE_WATER_SPEED_FIELD);
	m_cboIrrigationParaReportType.SetItemData(m_cboIrrigationParaReportType.AddString(GetTextByLan(_T("流量"), _T("Water Flow"))),IRRIGATION_TYPE_WATER_LEVELANDFLOW);
	m_cboIrrigationParaReportType.SetCurSel(0);

	SetDlgItemText(IDC_STATIC_WATERLEVELSOURCE, GetTextByLan(_T("设备水位数据源类型"), _T("Device WaterLevelSource Type")));
	SetDlgItemText(IDC_STATIC_WATERLEVELSOURCE_CHANNELNO, GetTextByLan(_T("通道号"), _T("ChannelNum")));
	SetDlgItemText(IDC_STATIC_WATERLEVELSOURCE_TYPE, GetTextByLan(_T("水位数据源类型"), _T("WaterLevelSource Type")));
	SetDlgItemText(IDC_STATIC_WATERLEVELSOURCE_VALUE, GetTextByLan(_T("水位值"), _T("WaterLevel Value")));

	SetDlgItemText(IDC_STATIC_WATERLEVELSOURCE_ID, GetTextByLan(_T("设备接受水位数据源ID"), _T("Device WaterLevelSource ID")));
	SetDlgItemText(IDC_STATIC_WATERLEVELSOURCE_ID_CHANNELNO, GetTextByLan(_T("通道号"), _T("ChannelNum")));
	SetDlgItemText(IDC_STATIC_WATERLEVELSOURCE_ID_FACTORYID, GetTextByLan(_T("出厂ID"), _T("FactoryID")));

	m_combo_waterlevelsource_type.ResetContent();
	m_combo_waterlevelsource_type.SetItemData(m_combo_waterlevelsource_type.AddString(GetTextByLan(_T("手动设置"), _T("Manual Setting"))), WATERLEVELSOURCE_TYPE_MANUAL);
	m_combo_waterlevelsource_type.SetItemData(m_combo_waterlevelsource_type.AddString(GetTextByLan(_T("网络获取"), _T("Network"))), WATERLEVELSOURCE_TYPE_NETWORK);
	m_combo_waterlevelsource_type.SetItemData(m_combo_waterlevelsource_type.AddString(GetTextByLan(_T("外设获取"), _T("Peripheral"))), WATERLEVELSOURCE_TYPE_PERIPHERAL);
	m_combo_waterlevelsource_type.SetCurSel(0);

	m_chkOverlyLed.SetCheck(BST_UNCHECKED);
	m_chkOverlyVideo.SetCheck(BST_UNCHECKED);
	m_chkWaterLevelAlert.SetCheck(BST_UNCHECKED);
	m_chkRainfallAlertEnbale.SetCheck(BST_UNCHECKED);
	GetDlgItem(IDC_CHECK_RAINFALL_ALERT)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_CHECK_ALERT_WATER_LEVEL)->ShowWindow(SW_HIDE);

	m_cboSenceId.ResetContent();
	for (int i = 0; i < MAX_SCENE_NUM; i++)
	{
		m_cboSenceId.InsertString(i, IntToCString(i + 1));
	}
	m_cboSenceId.SetCurSel(0);

	
}
void CLS_IrrigationGeneralConfig::OnBnClickedButtonSetIrrigationParaOverlyConf()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	IrrigationOverInfo tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iType = m_cboIrrigationParaReportType.GetItemData(m_cboIrrigationParaReportType.GetCurSel());
	tInfo.iLedEnable = m_chkOverlyLed.GetCheck();
	tInfo.iOsdEnable = m_chkOverlyVideo.GetCheck();

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_SetDevConfig[NET_CLIENT_IRRIGATIONOVER_INFO] fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_SetDevConfig[NET_CLIENT_IRRIGATIONOVER_INFO] success!");
	}
}												  

void CLS_IrrigationGeneralConfig::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	switch(_iParaType)
	{
	case PARA_IRRIGATION_OVER_PARA:
		{				
			AddLog(LOG_TYPE_SUCC,"","[CLS_IrrigationGeneralConfig::OnParamChangeNotify]->PARA_IRRIGATION_OVER_PARA (%d,%d)",m_iLogonID, m_iChannelNO);
			UpdateParameter();

			if (UpdateIrriDevStatus(IRRIGATION_TYPE_ACIDITYANDALKAL) && UpdateIrriDevStatus(IRRIGATION_TYPE_DISSOLVEOXYGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_TURBIDITY)
				&& UpdateIrriDevStatus(IRRIGATION_TYPE_AMMONICA) && UpdateIrriDevStatus(IRRIGATION_TYPE_WATERTEMP) && UpdateIrriDevStatus(IRRIGATION_TYPE_CONDUCTIVITY)
				&& UpdateIrriDevStatus(IRRIGATION_TYPE_OXYDEMAND) && UpdateIrriDevStatus(IRRIGATION_TYPE_NITROGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_PHOSPHORUS))
			{
				m_cbkOSDQuality.SetCheck(1);
			}
			else
			{
				m_cbkOSDQuality.SetCheck(0);
			}
		}
		break;
	case PARA_CE_POWER_UPLOAD_INTERVAL:
		{				
			AddLog(LOG_TYPE_SUCC,"","[CLS_IrrigationGeneralConfig::OnParamChangeNotify]->PARA_CE_POWER_UPLOAD_INTERVAL (%d,%d)",m_iLogonID, m_iChannelNO);
			UpdateParameter();

			if (UpdateIrriDevStatus(IRRIGATION_TYPE_ACIDITYANDALKAL) && UpdateIrriDevStatus(IRRIGATION_TYPE_DISSOLVEOXYGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_TURBIDITY)
				&& UpdateIrriDevStatus(IRRIGATION_TYPE_AMMONICA) && UpdateIrriDevStatus(IRRIGATION_TYPE_WATERTEMP) && UpdateIrriDevStatus(IRRIGATION_TYPE_CONDUCTIVITY)
				&& UpdateIrriDevStatus(IRRIGATION_TYPE_OXYDEMAND) && UpdateIrriDevStatus(IRRIGATION_TYPE_NITROGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_PHOSPHORUS))
			{
				m_cbkOSDQuality.SetCheck(1);
			}
			else
			{
				m_cbkOSDQuality.SetCheck(0);
			}
		}
		break;
	default:
		break;
	}
}

void CLS_IrrigationGeneralConfig::OnLanguageChanged( int _iLanguage)
{
	UpdateUI();
	UpdateParameter();

	if (UpdateIrriDevStatus(IRRIGATION_TYPE_ACIDITYANDALKAL) && UpdateIrriDevStatus(IRRIGATION_TYPE_DISSOLVEOXYGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_TURBIDITY)
		&& UpdateIrriDevStatus(IRRIGATION_TYPE_AMMONICA) && UpdateIrriDevStatus(IRRIGATION_TYPE_WATERTEMP) && UpdateIrriDevStatus(IRRIGATION_TYPE_CONDUCTIVITY)
		 && UpdateIrriDevStatus(IRRIGATION_TYPE_OXYDEMAND)  && UpdateIrriDevStatus(IRRIGATION_TYPE_NITROGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_PHOSPHORUS))
	{
		m_cbkOSDQuality.SetCheck(1);
	}
	else
	{
		m_cbkOSDQuality.SetCheck(0);
	}
}

void CLS_IrrigationGeneralConfig::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else 
		m_iLogonID = _iLogonID;

	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;    
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	UpdateParameter();

	if (UpdateIrriDevStatus(IRRIGATION_TYPE_ACIDITYANDALKAL) && UpdateIrriDevStatus(IRRIGATION_TYPE_DISSOLVEOXYGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_TURBIDITY)
		&& UpdateIrriDevStatus(IRRIGATION_TYPE_AMMONICA) && UpdateIrriDevStatus(IRRIGATION_TYPE_WATERTEMP) && UpdateIrriDevStatus(IRRIGATION_TYPE_CONDUCTIVITY)
		&& UpdateIrriDevStatus(IRRIGATION_TYPE_OXYDEMAND) && UpdateIrriDevStatus(IRRIGATION_TYPE_NITROGEN) && UpdateIrriDevStatus(IRRIGATION_TYPE_PHOSPHORUS))
	{
		m_cbkOSDQuality.SetCheck(1);
	}
	else
	{
		m_cbkOSDQuality.SetCheck(0);
	}
}

void CLS_IrrigationGeneralConfig::UpdateParameter()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}
	int iInPortNum = -1, iOutPutNo = -1;
	int iRet = -1;
	int iEnable = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_VCA_RESOURCE, m_iChannelNO, &iEnable);
	if (RET_SUCCESS == iRet)
	{
		if ((0 == (iEnable >> 4)) && (0 == (iEnable >> 3)))
		{
			GetDlgItem(IDC_BUTTON_SET_IRRIGATION_PARA_OVERLY_CONF)->EnableWindow(FALSE);
			GetDlgItem(IDC_BUTTON_COMMENABLE_SET)->EnableWindow(FALSE);
		}
		else
		{
			GetDlgItem(IDC_BUTTON_SET_IRRIGATION_PARA_OVERLY_CONF)->EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_COMMENABLE_SET)->EnableWindow(TRUE);
		}
	}
	iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iInPortNum, &iOutPutNo);
	m_cboPortNo.ResetContent();
	for (int i = 0; i < iOutPutNo; i++)
	{
		CString strPortNo = "";
		strPortNo.Format("%d", i+1);
		m_cboPortNo.InsertString(i, strPortNo);
	}
	IrrigationOverInfo tInfo = {0};
	tInfo.iType = m_cboIrrigationParaReportType.GetItemData(m_cboIrrigationParaReportType.GetCurSel());
	int iBytesReturned = 0;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_IRRIGATIONOVER_INFO] fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_IRRIGATIONOVER_INFO] success!");
		if(tInfo.iType < 1)
		{
			AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_IRRIGATIONOVER_INFO] Illegal Report Type (%d)!", tInfo.iType);
			return;
		}
		m_cboIrrigationParaReportType.SetCurSel(GetCboSel(&m_cboIrrigationParaReportType, tInfo.iType));
		m_chkOverlyLed.SetCheck(tInfo.iLedEnable);
		m_chkOverlyVideo.SetCheck(tInfo.iOsdEnable);
	}


	int iPowerUploadnable = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_POWER_UPLOAD_INTERVAL, m_iChannelNO, &iPowerUploadnable);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_POWER_UPLOAD_INTERVAL] success!");
		SetDlgItemInt(IDC_EDIT_POWER_UPLOAD_INTERNAL,iPowerUploadnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_POWER_UPLOAD_INTERVAL] fail!");
	}

	int iHornRelayPortOutPutEnable = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT, m_iChannelNO, &iHornRelayPortOutPutEnable);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT] success!");
		if (0 == iHornRelayPortOutPutEnable)
		{
			m_chkHornEnable.SetCheck(0);
			m_cboPortNo.EnableWindow(FALSE);
		}
		else
		{
			m_chkHornEnable.SetCheck(1);
			m_cboPortNo.EnableWindow(TRUE);
			m_cboPortNo.SetCurSel(iHornRelayPortOutPutEnable-1);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT] fail!");
	}

	WaterLevelSource tWaterLevelSource = {0};
	int iReturned = -1;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_WATERLEVEL_SOURCE, m_iChannelNO, &tWaterLevelSource, sizeof(tWaterLevelSource), &iReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_WATERLEVEL_SOURCE] fail!");
		SetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_CHANNELNO, -1);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_WATERLEVEL_SOURCE] success!");
		
		SetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_CHANNELNO, tWaterLevelSource.iChannelNo);
		m_combo_waterlevelsource_type.SetCurSel(tWaterLevelSource.iType - 1);
		if (WATERLEVELSOURCE_TYPE_MANUAL == tWaterLevelSource.iType)
		{
			m_edit_waterlevelsource_value.EnableWindow(TRUE);
			SetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_VALUE, tWaterLevelSource.iValue);
		}
		else
		{
			m_edit_waterlevelsource_value.EnableWindow(FALSE);
		}
	}

	WaterLevelSourceID tWaterLevelSourceID = {0};
	iReturned = -1;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_WATERLEVEL_SOURCE_ID, m_iChannelNO, &tWaterLevelSourceID, sizeof(tWaterLevelSourceID), &iReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_WATERLEVEL_SOURCE_ID] fail!");
		SetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_ID_CHANNELNO, -1);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_WATERLEVEL_SOURCE_ID] success!");
		
		SetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_ID_CHANNELNO, tWaterLevelSourceID.iChannelNo);
		SetDlgItemText(IDC_EDIT_WATERLEVELSOURCE_ID_FACTORYID,tWaterLevelSourceID.cFactoryID);
	}
}

void CLS_IrrigationGeneralConfig::OnCbnSelchangeComboIrrigationParaReportType()
{
	UpdateParameter();
	UpdateUI_OverlyConfig();
}


void CLS_IrrigationGeneralConfig::OnBnClickedButtonCommenableSet()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[OnBnClickedButtonCommenableSet]::Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}
	int iRet = -1;
	int iEnable = GetDlgItemInt(IDC_EDIT_POWER_UPLOAD_INTERNAL);
	if (iEnable < 0 || iEnable > 1440)
	{
		AddLog(LOG_TYPE_FAIL, "", "illegal Param:NetClient_SetCommonEnable[CI_COMMON_ID_POWER_UPLOAD_INTERVAL] fail!");
		MessageBox(GetTextByLan(_T("参数不合法"), _T("illegal Param")));
		return;
	}
	iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_POWER_UPLOAD_INTERVAL, m_iChannelNO, iEnable);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable[CI_COMMON_ID_POWER_UPLOAD_INTERVAL] success!");
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable[CI_COMMON_ID_POWER_UPLOAD_INTERVAL] fail!");
	}

	iEnable = m_chkHornEnable.GetCheck();
	if (0 == iEnable)
	{
		iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT, m_iChannelNO, iEnable);
	}
	else if(0 < iEnable)
	{
		iEnable = m_cboPortNo.GetCurSel();
		iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT, m_iChannelNO, iEnable+1);
	}
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable[CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT] success!");
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable[CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT] fail!");
	}
}

void CLS_IrrigationGeneralConfig::OnBnClickedCheckHornEnable()
{
	// TODO: Add your control notification handler code here
	if (m_chkHornEnable.GetCheck())
	{
		m_cboPortNo.EnableWindow(TRUE);
		m_cboPortNo.SetCurSel(0);
	}
	else
	{
		m_cboPortNo.EnableWindow(FALSE);
		m_cboPortNo.SetCurSel(-1);
	}
}

void CLS_IrrigationGeneralConfig::UpdateUI_OverlyConfig()
{
	int iType = m_cboIrrigationParaReportType.GetItemData(m_cboIrrigationParaReportType.GetCurSel());
	if(IRRIGATION_TYPE_AIRPRESSURE == iType
		|| IRRIGATION_TYPE_WINDSPEED == iType
		|| IRRIGATION_TYPE_WINDDIRECTION == iType
		|| IRRIGATION_TYPE_TEMPERATURE == iType
		|| IRRIGATION_TYPE_HUMIDITY == iType
		|| IRRIGATION_TYPE_ACIDITYANDALKAL == iType
		|| IRRIGATION_TYPE_DISSOLVEOXYGEN == iType
		|| IRRIGATION_TYPE_OXYREDUCTION == iType)//Air pressure Wind speed Wind direction Temperature Humidity
	{
		m_chkOverlyLed.EnableWindow(FALSE);
		m_chkOverlyLed.SetCheck(BST_UNCHECKED);
	}
	else
	{
		m_chkOverlyLed.EnableWindow(TRUE);
	}
}

BOOL CLS_IrrigationGeneralConfig::UpdateIrriDevStatus(int iType)
{
	BOOL _bstatus = FALSE;
	IrrigationOverInfo tInfo = {0};
	tInfo.iSize = (int)sizeof(IrrigationOverInfo);
	tInfo.iType = iType;
	int iBytesReturned = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetDevConfig[NET_CLIENT_IRRIGATIONOVER_INFO] fail!");
		return FALSE;
	}
	else
	{
		if(tInfo.iOsdEnable != 0)
		{
			return TRUE;
		}
		else
			return FALSE; 
	}
}

void CLS_IrrigationGeneralConfig::OnBnClickedCheckIrrOsdQuality()
{
	//Water Conservancy Customization 2019-11-29 Open at the same time 13, 14, 18, 19, 20, 21, 22, 23, 24
	IrrigationOverInfo tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	if (BST_CHECKED == m_cbkOSDQuality.GetCheck())
	{
		tInfo.iLedEnable = false;
		tInfo.iOsdEnable = true;
		tInfo.iType = IRRIGATION_TYPE_ACIDITYANDALKAL;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_DISSOLVEOXYGEN;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_TURBIDITY;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_AMMONICA;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_WATERTEMP;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_CONDUCTIVITY;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_OXYDEMAND;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_NITROGEN;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_PHOSPHORUS;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
	}
	else
	{
		tInfo.iLedEnable = false;
		tInfo.iOsdEnable = false;
		tInfo.iType = IRRIGATION_TYPE_ACIDITYANDALKAL;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_DISSOLVEOXYGEN;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_TURBIDITY;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_AMMONICA;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_WATERTEMP;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_CONDUCTIVITY;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_OXYDEMAND;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_NITROGEN;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
		tInfo.iType = IRRIGATION_TYPE_PHOSPHORUS;
		NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRIGATIONOVER_INFO, m_iChannelNO, &tInfo, tInfo.iSize);
	}
}

void CLS_IrrigationGeneralConfig::OnCbnSelchangeComboType()
{
	// TODO: Add control notification handler code here
	int iTemp = m_combo_waterlevelsource_type.GetCurSel() + 1;
	if (WATERLEVELSOURCE_TYPE_MANUAL == iTemp)
	{
		m_edit_waterlevelsource_value.EnableWindow(TRUE);
	}
	else
	{
		m_edit_waterlevelsource_value.EnableWindow(FALSE);
	}
}

void CLS_IrrigationGeneralConfig::OnBnClickedButtonWaterlevelsourceSet()
{
	// TODO: Add control notification handler code here
	WaterLevelSource tWaterLevelSource = {0};
	tWaterLevelSource.iSize = sizeof(tWaterLevelSource);
	tWaterLevelSource.iChannelNo = GetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_CHANNELNO);
	tWaterLevelSource.iType = m_combo_waterlevelsource_type.GetCurSel() + 1; 
	tWaterLevelSource.iValue = GetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_VALUE);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_WATERLEVEL_SOURCE, m_iChannelNO, &tWaterLevelSource, sizeof(tWaterLevelSource));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig][NET_CLIENT_WATERLEVEL_SOURCE] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig][NET_CLIENT_WATERLEVEL_SOURCE] Set Success", m_iLogonID);
	}	
}

void CLS_IrrigationGeneralConfig::OnBnClickedButtonWaterlevelsourceIdSet()
{
	// TODO: Add control notification handler code here
	WaterLevelSourceID tWaterLevelSourceID = {0};
	tWaterLevelSourceID.iSize = sizeof(WaterLevelSourceID);
	tWaterLevelSourceID.iChannelNo = GetDlgItemInt(IDC_EDIT_WATERLEVELSOURCE_ID_CHANNELNO);
	GetDlgItemText(IDC_EDIT_WATERLEVELSOURCE_ID_FACTORYID, tWaterLevelSourceID.cFactoryID, sizeof(tWaterLevelSourceID.cFactoryID));
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_WATERLEVEL_SOURCE_ID, m_iChannelNO, &tWaterLevelSourceID, sizeof(tWaterLevelSourceID));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig][NET_CLIENT_WATERLEVEL_SOURCE_ID] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig][NET_CLIENT_WATERLEVEL_SOURCE_ID] Set Success", m_iLogonID);
	}	
}

void CLS_IrrigationGeneralConfig::OnBnClickedButtonWaterSpeedSend()
{
	if(m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_IrrigationGeneralConfig::OnBnClickedButtonWaterSpeedSend (%d, %d)", m_iLogonID, m_iChannelNO);

	}

	WaterSpeedDeteUpdate  tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboSenceId.GetCurSel();
	tInfo.iDetectStep = GetDlgItemInt(IDC_EDIT_DETECT_STEP);
	int iRet = NetClient_SendCommand(m_iLogonID,  COMMAND_ID_WATER_SPEED_UPDATE,  m_iChannelNO,  &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_IrrigationGeneralConfig]NetClient_SendCommand(COMMAND_ID_WATER_SPEED_UPDATE)Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_IrrigationGeneralConfig]NetClient_SendCommand(COMMAND_ID_WATER_SPEED_UPDATE)Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

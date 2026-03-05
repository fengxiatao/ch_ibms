// Periphreal.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "Periphreal.h"


// CPeriphreal dialog

#define DEV_TEMPANDHUMIDITY               0
#define DEV_PRESSURE                      1
#define DEV_MICROMETEOROLOGICAL_STATION   2
#define DEV_RADAR                         3
#define DEV_SOLARCONTROLLOR               4
#define DEV_FLOWVELOCITY_METER            5
#define DEV_LED                           6
#define DEV_BEIDOU                        7
#define DEV_GPS                           8
#define DEV_485LIGHT                      9
#define DEV_ACIDITYANDALKAL               10
#define DEV_DISSOLVEOXYGEN                11
#define DEV_OXYREDUCTION                  12
#define DEV_TURBIDITY	                  13
#define DEV_AMMONIA_NITROGEN              14
#define DEV_SWF_03_QUALITY                15
#define DEV_PRESSURE_WATERLEVEL_GAUGE	  16 
#define DEV_INCLINATION_SENSOR			  17
#define DEV_RTU_SENSOR					  18
#define DEV_WEATHER_MESH_WEATHER_STATION  19
#define DEV_DUST_COLLECTOR				  20
#define DEV_AIRSENSOR_COMBUSTIBLE_GAS	  21
#define DEV_AIRSENSOR_OXYGEN			  22
#define DEV_AIRSENSOR_CARBON_MONOXIDE	  23
#define DEV_AIRSENSOR_HYDROGEN_SULFIDE	  24
#define DEV_FLOAT_LEVEL_GAUGE			  25
#define DEV_AIRSENSOR_FORMALDEHYDE		  26
#define DEV_AIRSENSOR_AMMONIA			  27
#define DEV_LIQUID_LEVEL_TRANSMITTER	  28
#define DEV_ECON_THERMO_HYGRO_METER		  29
#define DEV_POLIT_LEGAL_TEM_HUMI_SCREEN	  30
#define DEV_MPPT_SOLAR_CONTROLLER		  31
#define DEV_INDUCT_ELECT_WATER_GAUGE	  32
#define DEV_LS_OSD						  33
#define DEV_RADAR_FLOWMETER				  34

IMPLEMENT_DYNAMIC(CPeriphreal, CDialog)

CPeriphreal::CPeriphreal(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CPeriphreal::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
}

CPeriphreal::~CPeriphreal()
{
}

void CPeriphreal::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_COMNO, m_cboComNo);
	DDX_Control(pDX, IDC_COMBO_SUPPORTTYPE, m_cboSupportType);
	DDX_Control(pDX, IDC_EDIT_PERINUM, m_PeriNum);
	DDX_Control(pDX, IDC_EDT_SHOW, m_edtShow);
	DDX_Control(pDX, IDC_EDT_INDEX1, m_edt_Data1);
	DDX_Control(pDX, IDC_EDT_INDEX2, m_edt_Data2);
	DDX_Control(pDX, IDC_CHECK_DEVICEENABLE, m_chkDeviceEnable);
}


BEGIN_MESSAGE_MAP(CPeriphreal, CDialog)
	ON_CBN_SELCHANGE(IDC_COMBO_COMNO, &CPeriphreal::OnCbnSelchangeComboComno)
	ON_BN_CLICKED(IDC_BUTTON_SETTYPE, &CPeriphreal::OnBnClickedButtonSettype)
	ON_BN_CLICKED(IDC_BUTTON_SET2, &CPeriphreal::OnBnClickedButtonSet2)
	ON_NOTIFY(NM_DBLCLK, IDC_LIST_SHOWADDR, &CPeriphreal::OnNMDblclkListShowaddr)
	ON_EN_KILLFOCUS(IDC_EDT_SHOW, &CPeriphreal::OnEnKillfocusEdtShow)
	ON_CBN_SELCHANGE(IDC_COMBO_SUPPORTTYPE, &CPeriphreal::OnCbnSelchangeComboSupporttype)
	ON_CBN_SELCHANGE(IDC_COMBO_ADDR, &CPeriphreal::OnCbnSelchangeComboAddr)
	ON_WM_CTLCOLOR()
END_MESSAGE_MAP()


// CPeriphreal message handlers
BOOL CPeriphreal::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_edtShow.ShowWindow(SW_HIDE);
	UI_InitDialog();
	return TRUE;
	
}

void CPeriphreal::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	if (m_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	int iProductType = 0;
	int iRet = NetClient_GetProductType(m_iLogonID, &iProductType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetProductType(%d,%d)"
			,m_iLogonID, iProductType);

		iProductType &= 0xFFFF;
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetProductType(%d,%d)"
			,m_iLogonID, iProductType);
	}

	int iComPortCounts = 0;
	int iComPortStatus = 0;
	iRet = NetClient_GetComPortCounts(m_iLogonID, &iComPortCounts, &iComPortStatus);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetComPortCounts(%d,%d,%d)"
			,m_iLogonID, iComPortCounts, iComPortStatus);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetComPortCounts(%d,%d,%d)"
			,m_iLogonID, iComPortCounts, iComPortStatus);

		if (IsDVR(iProductType))
		{
			iComPortCounts = 4;
		}
		else
		{
			iComPortCounts = 2;
		}
	}

	CString strCom;
	int iComIndex = -1;
	m_cboComNo.ResetContent();
	for (int i = 0; i < iComPortCounts; ++i)
	{
		if ((iComPortStatus >> i) & 1)
		{
			strCom.Format(_T("COM%d"),i+1);
			iComIndex = m_cboComNo.AddString(strCom);
			m_cboComNo.SetItemData(iComIndex,i+1);
		}
	}
	m_cboComNo.SetCurSel(0);
	UI_UpdateData();
	GetStaticInfo();
	UpdatePeriPhrealPara();
	UpdatePeriPhrealInfo();
}

void CPeriphreal::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{

	}
}

void CPeriphreal::GetTypeNameByIndex(int _iIndex)
{
	switch(_iIndex)
	{
	case DEV_TEMPANDHUMIDITY:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("温湿度计"),_T("Temp And Hum Meter"))),_iIndex+1);
		break;
	case DEV_PRESSURE:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("压差"),_T("Thero Meter"))), _iIndex+1);
		break;
	case DEV_MICROMETEOROLOGICAL_STATION:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("激光雨量计"),_T("Miniature weather station"))), _iIndex+1);
		break;
	case DEV_RADAR:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("雷达水位计"),_T("Radar Water Level Gauge"))), _iIndex + 1);
		break;
	case DEV_SOLARCONTROLLOR:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("电瓶传感器"),_T("Solar Controller"))), _iIndex +1);
		break;
	case DEV_FLOWVELOCITY_METER:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("流速仪"),_T("Flow Meter"))), _iIndex + 1);
		break;
	case DEV_LED:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("LED屏"),_T("Led Screen"))), _iIndex + 1);
		break;
	case DEV_BEIDOU:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("北斗模块"),_T("BeiDou"))), _iIndex + 1);
		break;
	case DEV_GPS:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("GPS"),_T("GPS"))), _iIndex + 1);
		break;
	case DEV_485LIGHT:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("485独立补光灯"),_T("485 Light"))), _iIndex + 1);
		break;
	case DEV_ACIDITYANDALKAL:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("酸碱度仪"),_T("AcidityAndAlkal"))), _iIndex + 1);
		break;
	case DEV_DISSOLVEOXYGEN:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("溶解氧仪"),_T("Oxygen Dissolve"))), _iIndex + 1);
		break;
	case DEV_OXYREDUCTION:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("氧化还原仪"),_T("OxyReduction"))), _iIndex + 1);
		break;
	case DEV_TURBIDITY:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("浊度"),_T("Turbidity"))), _iIndex + 1);
		break;
	case DEV_AMMONIA_NITROGEN:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("氨氮"),_T("Ammonia_Nitrogen"))), _iIndex + 1);
		break;
	case DEV_SWF_03_QUALITY:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("SWF-03型水质仪"),_T("SWF-03Quality"))), _iIndex + 1);
		break;
	case DEV_PRESSURE_WATERLEVEL_GAUGE:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("压力式水位计"),_T("Pressure_Waterlevel_Gauge"))), _iIndex + 1);
		break;
	case DEV_INCLINATION_SENSOR:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("倾角传感器"),_T("Inclination sensor"))), _iIndex + 1);
		break;
	case DEV_RTU_SENSOR:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("RTU传感器"),_T("RTU sensor"))), _iIndex + 1);
		break;
	case DEV_WEATHER_MESH_WEATHER_STATION:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("天气网眼气象站"),_T("Weather mesh weather station"))), _iIndex + 1);
		break;
	case DEV_DUST_COLLECTOR:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("扬尘器"),_T("Dust collector"))), _iIndex + 1);
		break;
	case DEV_AIRSENSOR_COMBUSTIBLE_GAS:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("空气传感器-可燃气"),_T("Air sensor combustible gas"))), _iIndex + 1);
		break;
	case DEV_AIRSENSOR_OXYGEN:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("空气传感器-氧气"),_T("Air sensor oxygen"))), _iIndex + 1);
		break;
	case DEV_AIRSENSOR_CARBON_MONOXIDE:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("空气传感器-一氧化碳"),_T("Air sensor carbon monoxide"))), _iIndex + 1);
		break;
	case DEV_AIRSENSOR_HYDROGEN_SULFIDE:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("空气传感器-硫化氢"),_T("Air sensor hydrogen sulfide"))), _iIndex + 1);
		break;
	case DEV_FLOAT_LEVEL_GAUGE:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("浮球液位计"),_T("Float level gauge"))), _iIndex + 1);
		break;
	case DEV_AIRSENSOR_FORMALDEHYDE:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("空气传感器-甲醛"),_T("Air sensor formaldehyde"))), _iIndex + 1);
		break;
	case DEV_AIRSENSOR_AMMONIA:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("空气传感器-氨气"),_T("Air sensor ammonia"))), _iIndex + 1);
		break;
	case DEV_LIQUID_LEVEL_TRANSMITTER:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("液位变送器"),_T("Liquid level transmitter"))), _iIndex + 1);
		break;
	case DEV_ECON_THERMO_HYGRO_METER:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("经济型温湿度仪"),_T("Economical thermometer and hygrometer"))), _iIndex + 1);
		break;
	case DEV_POLIT_LEGAL_TEM_HUMI_SCREEN:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("政法温湿度屏"),_T("Political and legal temperature and humidity screen"))), _iIndex + 1);
		break;
	case DEV_MPPT_SOLAR_CONTROLLER:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("MPPT太阳能控制器"),_T("MPPT solar controller"))), _iIndex + 1);
		break;
	case DEV_INDUCT_ELECT_WATER_GAUGE:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("感应式电子水尺"),_T("Inductive electronic water gauge"))), _iIndex + 1);
		break;
	case DEV_LS_OSD:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("LS_OSD"),_T("LS_OSD"))), _iIndex + 1);
		break;
	case DEV_RADAR_FLOWMETER:
		m_cboSupportType.SetItemData(m_cboSupportType.AddString(GetTextByLan(_T("雷达流量计"),_T("Radar flowmeter"))), _iIndex + 1);
		break;
	}
}                                                                           
       
void CPeriphreal::UI_UpdateData()
{
	if (-1 == m_iLogonID)
	{
		return;
	}
	int iReturn = -1;
	//Get the number of peripherals supported by the serial port under this channel
	PeripheralList tInfo = {0};
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_PERIPHERAL_LIST, m_iChannelNo, &tInfo, sizeof(tInfo), &iReturn);

	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d)",m_iChannelNo);
		m_cboSupportType.ResetContent();
		int iCount = 0;
		for (int i = 0;i < MAX_IRRIGATION_TYPE; i++)
		{
			if ((tInfo.iSupportType >> i) & 1)
			{
				GetTypeNameByIndex(i);
				iCount++;
			}
		}
		if (iCount)
		{
			m_cboSupportType.SetCurSel(0);
		}
		else
			m_cboSupportType.SetCurSel(-1);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d)",m_iChannelNo);
	}
}

void CPeriphreal::UpdatePeriPhrealInfo()
{
	if (-1 == m_iLogonID)
	{
		return;
	}
	CString strAddr;
	m_PeriNum.GetWindowText(strAddr);
	int iReturn = -1;
	int iReturnBytes = -1;
	PeripheralPara tPara = {0};
	tPara.iSize = (int)sizeof(tPara);
	int iComIndex = m_cboComNo.GetCurSel();
	tPara.iComNo = m_cboComNo.GetItemData(iComIndex);
	//if (m_cboSupportType.GetCurSel() < 0 || m_cboAddress.GetCurSel() < 0)
	//{
	//	return;
	//}
	tPara.iPeripheralType = m_cboSupportType.GetItemData(m_cboSupportType.GetCurSel());
	tPara.iPeripheralAddr = _ttoi(strAddr);
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_PERIPHERAL_PARA, m_iChannelNo, &tPara, tPara.iSize, &iReturnBytes);

	if (0 == iRet)
	{
		CString csData1 = "",csData2 = "";
		csData1 = tPara.cPeripheralPara[0];
		csData2 = tPara.cPeripheralPara[1];
		if (PERIPHERAL_TYPE_RADAR_WATER_LEVEL_GAUGE == tPara.iPeripheralType || PERIPHREAL_TYPE_PRESSURE_WATERLEVEL_GAUGE == tPara.iPeripheralType)
		{
			int iData = _ttoi(csData1);
			float fData = (float)(iData / 1000.0);
			csData1.Format("%.3f",fData);
		}
		m_edt_Data1.SetWindowText(csData1);
		m_edt_Data2.SetWindowText(csData2);
	}
	else
	{
		m_edt_Data1.SetWindowText("");
		m_edt_Data2.SetWindowText("");
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%d) = %d",m_iLogonID, m_iChannelNo, iRet);
	}
}
void CPeriphreal::OnCbnSelchangeComboComno()
{
	// TODO: Add your control notification handler code here
	UI_UpdateData();
	UpdatePeriPhrealPara();
	UpdatePeriPhrealInfo();
}





void CPeriphreal::OnBnClickedButtonSettype()
{
	// TODO: Add your control notification handler code here
	if (-1 == m_iLogonID)
	{
		return;
	}
	PeripheralInfo tInfo = {0};
	tInfo.iSize = sizeof(PeripheralInfo);
	int iComIndex = m_cboComNo.GetCurSel();
	tInfo.iComNum = m_cboComNo.GetItemData(iComIndex);
	tInfo.iPeripheralType = m_cboSupportType.GetItemData(m_cboSupportType.GetCurSel());
	tInfo.iEnable = m_chkDeviceEnable.GetCheck();
	CString strNum = "";
	m_PeriNum.GetWindowText(strNum);
	tInfo.iPeripheralAddr[0] = _ttoi(strNum);
	tInfo.iPeripheralNum = 1;

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_PERIPHERAL_INFO, m_iChannelNo, &tInfo, (int)sizeof(tInfo));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
		UpdatePeriPhrealPara();
		UpdatePeriPhrealInfo();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
	}
	return;
}

void CPeriphreal::UpdatePeriPhrealPara()
{
	
	if (-1 == m_iLogonID)
	{
		return ;
	}
	int iReturnBytes = -1;
	PeripheralInfo tInfo = {0};
	tInfo.iComNum = m_cboComNo.GetItemData(m_cboComNo.GetCurSel());
	tInfo.iSize = (int)sizeof(tInfo);
	if (m_cboSupportType.GetCurSel() < 0)
	{
		return;
	}
	tInfo.iPeripheralType = m_cboSupportType.GetItemData(m_cboSupportType.GetCurSel());
	
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_PERIPHERAL_INFO, m_iChannelNo, &tInfo, (int)sizeof(tInfo), &iReturnBytes);

	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
		int iRealPeripheralNum = (0x7FFFFFFF == (unsigned int)tInfo.iPeripheralNum) ? 1 : tInfo.iPeripheralNum;
		CString csCount;
		csCount.Format("%d", tInfo.iPeripheralAddr[0]);
		m_PeriNum.SetWindowText(csCount);
		m_chkDeviceEnable.SetCheck(tInfo.iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
	}
	return;
}
void CPeriphreal::OnBnClickedButtonSet2()
{
	// TODO: Add your control notification handler code here
	//NET_CLIENT_PERIPHERAL_PARA
	if (-1 == m_iLogonID)
	{
		return;
	}
	CString strAddr;
	m_PeriNum.GetWindowText(strAddr);
	PeripheralPara tPara = {0};
	tPara.iSize = (int)sizeof(tPara);
	int iComIndex = m_cboComNo.GetCurSel();
	tPara.iComNo = m_cboComNo.GetItemData(iComIndex);
	tPara.iPeripheralType = m_cboSupportType.GetItemData(m_cboSupportType.GetCurSel());
    tPara.iPeripheralAddr = _ttoi(strAddr);
    CString csData1,csData2;
	if (PERIPHERAL_TYPE_RADAR_WATER_LEVEL_GAUGE == tPara.iPeripheralType || PERIPHREAL_TYPE_PRESSURE_WATERLEVEL_GAUGE == tPara.iPeripheralType)
	{
		tPara.iParaCounter = 1;
		m_edt_Data1.GetWindowText(csData1);
		//Convert string to float
		CString sTmp;
		double fValue = atof(csData1);
		fValue += 0.0005;
		int iData = (int)(fValue*1000.0);
		csData1.Format("%d",iData);
		memcpy_s(&tPara.cPeripheralPara[0], LEN_256, csData1.GetBuffer(), csData1.GetLength());
	}
	else if (PERIPHERAL_TYPE_BEIDOU_MOUDLE == tPara.iPeripheralType)
	{
		tPara.iParaCounter = 2;
		m_edt_Data1.GetWindowText(csData1);
		m_edt_Data2.GetWindowText(csData2);
		int iInterval = _ttoi(csData1);
		if (iInterval < 60)
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig:iLLegal Parameter");
			GetDlgItem(IDC_STATIC_INFO)->ShowWindow(SW_SHOW);
			return;
		}
		memcpy_s(&tPara.cPeripheralPara[0], LEN_256, csData1.GetBuffer(), csData1.GetLength());
		memcpy_s(&tPara.cPeripheralPara[1], LEN_256, csData2.GetBuffer(), csData2.GetLength());
	}
	else if (PERIPHREAL_TYPE_RADAR_FLOWMETER == tPara.iPeripheralType || PERIPHREAL_TYPE_RTU_SENSOR == tPara.iPeripheralType || PERIPHREAL_TYPE_ACIDITYANDALKAL == tPara.iPeripheralType || PERIPHREAL_TYPE_DISSOLVEOXYGEN == tPara.iPeripheralType || PERIPHREAL_TYPE_OXYREDUCTION == tPara.iPeripheralType)
	{
		tPara.iParaCounter = 1;
	    m_edt_Data1.GetWindowText(csData1);
		memcpy_s(&tPara.cPeripheralPara[0], LEN_256, csData1.GetBuffer(), csData1.GetLength());
	}

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_PERIPHERAL_PARA, m_iChannelNo, &tPara, tPara.iSize);
	GetDlgItem(IDC_STATIC_INFO)->ShowWindow(SW_HIDE);

	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
		UpdatePeriPhrealInfo();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
	}
}



void CPeriphreal::OnNMDblclkListShowaddr(NMHDR *pNMHDR, LRESULT *pResult)
{
	//LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	// TODO: Add your control notification handler code here
	NM_LISTVIEW* pNMListView = (NM_LISTVIEW*)pNMHDR;
	*pResult = 0;
}

void CPeriphreal::OnEnKillfocusEdtShow()
{
	// TODO: Add your control notification handler code here  
}




void CPeriphreal::OnCbnSelchangeComboSupporttype()
{
	// TODO: Add your control notification handler code here
	GetStaticInfo();
	UpdatePeriPhrealPara();
	UpdatePeriPhrealInfo();
}

void CPeriphreal::OnCbnSelchangeComboAddr()
{
	// TODO: Add your control notification handler code here
	UpdatePeriPhrealInfo();
}

void CPeriphreal::GetStaticInfo()
{
	int iIndex = m_cboSupportType.GetItemData(m_cboSupportType.GetCurSel());
	if (iIndex != PERIPHERAL_TYPE_BEIDOU_MOUDLE && iIndex != PERIPHERAL_TYPE_RADAR_WATER_LEVEL_GAUGE &&
		iIndex != PERIPHREAL_TYPE_ACIDITYANDALKAL && iIndex != PERIPHREAL_TYPE_DISSOLVEOXYGEN &&
		iIndex != PERIPHREAL_TYPE_OXYREDUCTION && iIndex != PERIPHREAL_TYPE_PRESSURE_WATERLEVEL_GAUGE &&
		PERIPHREAL_TYPE_RTU_SENSOR != iIndex && PERIPHREAL_TYPE_RADAR_FLOWMETER != iIndex)
	{
	    GetDlgItem(IDC_STATIC_INDEX1)->SetWindowText(GetTextByLan(_T("参数1"),_T("Index Data1")));
	    GetDlgItem(IDC_STATIC_INDEX2)->SetWindowText(GetTextByLan(_T("参数2"),_T("Index Data2")));
		m_edt_Data1.EnableWindow(FALSE);
		m_edt_Data2.EnableWindow(FALSE);
	}
	else
	{
		GetDlgItem(IDC_STATIC_INDEX1)->EnableWindow(TRUE);
		GetDlgItem(IDC_STATIC_INDEX2)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDT_INDEX1)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDT_INDEX2)->EnableWindow(TRUE);
	}
	if (iIndex == PERIPHERAL_TYPE_BEIDOU_MOUDLE)
	{
		GetDlgItem(IDC_STATIC_INDEX1)->SetWindowText(GetTextByLan(_T("传输时间间隔(:s)"),_T("Transmission Time Interval(:s)")));
		GetDlgItem(IDC_STATIC_INDEX2)->SetWindowText(GetTextByLan(_T("传输目的地址"),_T("Transmission Dest Address")));
	}
	else if (iIndex == PERIPHERAL_TYPE_RADAR_WATER_LEVEL_GAUGE || iIndex == PERIPHREAL_TYPE_PRESSURE_WATERLEVEL_GAUGE)
	{
		GetDlgItem(IDC_STATIC_INDEX1)->SetWindowText(GetTextByLan(_T("基值(:m)"),_T("Base Data(:m)")));
		GetDlgItem(IDC_STATIC_INDEX2)->SetWindowText(GetTextByLan(_T("参数2"),_T("Index Data2")));
		GetDlgItem(IDC_STATIC_INDEX2)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDT_INDEX2)->EnableWindow(FALSE);
	}
	else if (iIndex == PERIPHREAL_TYPE_ACIDITYANDALKAL || iIndex == PERIPHREAL_TYPE_DISSOLVEOXYGEN || iIndex == PERIPHREAL_TYPE_OXYREDUCTION)
	{
		GetDlgItem(IDC_STATIC_INDEX1)->SetWindowText(GetTextByLan(_T("校准值:"),_T("Base Data(:m)")));
		GetDlgItem(IDC_STATIC_INDEX2)->SetWindowText(GetTextByLan(_T("参数2"),_T("Index Data2")));
		GetDlgItem(IDC_STATIC_INDEX2)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDT_INDEX2)->EnableWindow(FALSE);
	}
	else if(PERIPHREAL_TYPE_RTU_SENSOR == iIndex || PERIPHREAL_TYPE_RADAR_FLOWMETER == iIndex)
	{
		GetDlgItem(IDC_STATIC_INDEX1)->SetWindowText(GetTextByLan(_T("发送周期(秒):"),_T("Send time(s):")));
		GetDlgItem(IDC_STATIC_INDEX2)->SetWindowText(GetTextByLan(_T("参数2"),_T("Index Data2")));
		GetDlgItem(IDC_STATIC_INDEX2)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDT_INDEX2)->EnableWindow(FALSE);
	}
}
void CPeriphreal::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (-1 == m_iLogonID || -1 == m_iChannelNo)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	if (PARA_PERIPHERAL_INFO == _iParaType)
	{
		UpdatePeriPhrealPara();
	}
	else if (PARA_PERIPHERAL_PARA == _iParaType)
	{
		UpdatePeriPhrealInfo();
	}
}

void CPeriphreal::UI_InitDialog()
{
	SetDlgItemText(IDC_STATIC_COMNO, GetTextByLan(_T("串口号"), _T("ComNo")));
	SetDlgItemText(IDC_STATIC_SUPPORTTYPE, GetTextByLan(_T("外设支持类型"), _T("Support Periphreal Type")));
	SetDlgItemText(IDC_STATIC_PERINO, GetTextByLan(_T("地址码(1-255)"), _T("Address(1-255)")));
	SetDlgItemText(IDC_BUTTON_SETTYPE, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_TYPE, GetTextByLan(_T("外设类型设置"), _T("Periphreal Type Setting")));
	SetDlgItemText(IDC_STATIC_PERITYPE, GetTextByLan(_T("外设参数设置"), _T("Periphreal Data Setting")));
	SetDlgItemText(IDC_STATIC_INFO, GetTextByLan(_T("时间间隔不得小于60s"), _T("Time InterVal can not less than 60s")));
	SetDlgItemText(IDC_CHECK_DEVICEENABLE, GetTextByLan(_T("使能"),_T("Enable")));
	SetDlgItemText(IDC_BUTTON_SET2, GetTextByLan(_T("设置"),_T("Set")));
	GetDlgItem(IDC_STATIC_INFO)->ShowWindow(SW_HIDE);
}

void CPeriphreal::OnLanguageChanged(int _iLanguage)
{
	UI_InitDialog();
}

HBRUSH CPeriphreal::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CLS_BasePage::OnCtlColor(pDC, pWnd, nCtlColor);

	// TODO:  Change any attributes of the DC here
	if (IDC_STATIC_INFO == pWnd->GetDlgCtrlID())
	{
		pDC->SetTextColor(RGB(255, 0, 0));//set text color to red
		pDC->SetBkColor(TRANSPARENT);
	}
	// TODO:  Return a different brush if the default is not desired
	return hbr;
}

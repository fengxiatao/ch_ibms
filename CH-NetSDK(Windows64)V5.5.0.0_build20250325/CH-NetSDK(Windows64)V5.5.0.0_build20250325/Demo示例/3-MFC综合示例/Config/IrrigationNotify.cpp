// IrrigationNotify.cpp : implementation file
//

#include "stdafx.h"
#include "IrrigationNotify.h"

#define COLUMN_IRRILST_IP				1
#define COLUMN_IRRILST_CHAN				2
#define COLUMN_IRRILST_TYPE				3
#define COLUMN_IRRILST_VALUE			4
#define COLUMN_IRRILST_SRC				5
#define COLUMN_IRRILST_SCENEID			6
#define COLUMN_IRRILST_RULEID			7
#define COLUMN_IRRILST_UPLOADTIME		8
#define COLUMN_IRRILST_STATIONNUM		9
#define COLUMN_IRRILST_IRRPARAM8		10
#define COLUMN_IRRILST_IRRPARAM9		11
#define COLUMN_IRRILST_TOTALPOINTNUM	12
#define COLUMN_IRRILST_CUUPOINTNUM		13
#define COLUMN_IRRILST_BASENUM			14
#define COLUMN_IRRILST_RECORDTIME		15
#define COLUMN_IRRILST_FACTORYID		16
#define COLUMN_IRRILST_IRRPARAM6		17
#define COLUMN_IRRILST_IRRPARAM13		18
#define COLUMN_IRRILST_IRRPARAM15		19
#define COLUMN_IRRILST_IRRPARAM16		20
#define COLUMN_IRRILST_IRRPARAM17		21
#define COLUMN_IRRILST_IRRPARAM18		22
#define COLUMN_IRRILST_IRRPARAM19		23
#define COLUMN_IRRILST_IRRPARAM20		24
#define COLUMN_IRRILST_IRRPARAM21		25
#define COLUMN_IRRILST_IRRPARAM22		26
#define COLUMN_IRRILST_IRRPARAM23		27
#define COLUMN_IRRILST_IRRPARAM24		28


// IrrigationNotify dialog

IMPLEMENT_DYNAMIC(IrrigationNotify, CDialog)

IrrigationNotify::IrrigationNotify(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(IrrigationNotify::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	memset(&m_tIrrigationPara, 0, sizeof(m_tIrrigationPara));
}

IrrigationNotify::~IrrigationNotify()
{
}

void IrrigationNotify::DoDataExchange(CDataExchange* pDX)
{
    CDialog::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_LIST_IRRIGATION_NOTIFY, m_lstIrrigationNotify);
    //DDX_Control(pDX, IDC_LIST_WATERFLOW, m_listWaterFlow);
    DDX_Control(pDX, IDC_LIST_WATERFLOW1, m_lstWaterFlow);
    DDX_Control(pDX, IDC_COMBO_DATA_TYPE, m_cboDataType);
}


BEGIN_MESSAGE_MAP(IrrigationNotify, CDialog)
	ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_IRRIGATION_NOTIFY, &IrrigationNotify::OnLvnItemchangedListIrrigationNotify)
    ON_BN_CLICKED(IDC_BUTTON_QUERY, &IrrigationNotify::OnBnClickedButtonQuery)
    ON_BN_CLICKED(IDC_BUTTON_QUERY_CUSTOM_TYPE, &IrrigationNotify::OnBnClickedButtonQueryCustomType)
END_MESSAGE_MAP()


// IrrigationNotify message handlers

BOOL IrrigationNotify::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_lstIrrigationNotify.SetExtendedStyle(m_lstIrrigationNotify.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstWaterFlow.SetExtendedStyle(m_lstWaterFlow.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	UI_UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void IrrigationNotify::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	if(m_tIrrigationPara.iSize > 0)
	{
		UpdateIrrigationNotify(m_iLogonID, m_iChannelNo, &m_tIrrigationPara);
	}
}

void IrrigationNotify::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
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
}

void IrrigationNotify::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	int iItemCount = m_lstIrrigationNotify.GetItemCount();
	switch(_iParaType)
	{
	case PARA_IRRIGATION_NOTIFY:
		{
			IrrigationPara* ptIrrigationPara = (IrrigationPara*)_pPara;
			memcpy(&m_tIrrigationPara, ptIrrigationPara, sizeof(m_tIrrigationPara));
			UpdateIrrigationNotify(_iLogonID, _iChannelNo, ptIrrigationPara);
		}
		break;
	case PARA_IRRIGATIONEND_NOTIFY:
		m_lstIrrigationNotify.InsertItem(iItemCount, "");
		m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IP, "");
		m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_CHAN, "");
		m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_TYPE, "");
		m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_VALUE, "");
		m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_SRC, "");
		m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_SCENEID, "");
		m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_RULEID, "");
		break;
	default:
		break;
	}
}

CString IrrigationNotify::GetStringByType(int _iType)
{
	CString cstrType;
	switch (_iType)
	{
	case IRRIGATION_TYPE_RAINFALL:
		cstrType = GetTextByLan(_T("雨量"), _T("rainfall"));
		break;
	case IRRIGATION_TYPE_RAINDURATION:
		cstrType = GetTextByLan(_T("降雨时长"), _T("rain duration"));
		break;
	case IRRIGATION_TYPE_WATERLEVEL:
		cstrType = GetTextByLan(_T("水位"), _T("water level"));
		break;
	case IRRIGATION_TYPE_SEDIMENT:
		cstrType = GetTextByLan(_T("积水深度"), _T("depth of water"));
		break;
	case IRRIGATION_TYPE_ALERTWATERLEVEL:
		cstrType = GetTextByLan(_T("超警戒水位值"), _T("alert water level"));
		break;
	case IRRIGATION_TYPE_FLOWRATEVALUE:
		cstrType = GetTextByLan(_T("流速"), _T("flow rate value"));
		break;
	case IRRIGATION_TYPE_BATTERYVOLTAGE:
		cstrType = GetTextByLan(_T("电池剩余电量"), _T("Battery Voltage"));
		break;
	case IRRIGATION_TYPE_AIRPRESSURE:
		cstrType = GetTextByLan(_T("气压"), _T("Air pressure"));
		break;
	case IRRIGATION_TYPE_WINDSPEED:
		cstrType = GetTextByLan(_T("风速"), _T("Wind speed"));
		break;
	case IRRIGATION_TYPE_WINDDIRECTION:
		cstrType = GetTextByLan(_T("风向"), _T("wind direction"));
		break;
	case IRRIGATION_TYPE_TEMPERATURE:
		cstrType = GetTextByLan(_T("温度"), _T("Temperature"));
		break;
	case IRRIGATION_TYPE_HUMIDITY:
		cstrType = GetTextByLan(_T("湿度"), _T("Humidity"));
		break;
	case IRRIGATION_TYPE_ACIDITYANDALKAL:
		cstrType = GetTextByLan(_T("酸碱度"), _T("Acidity And Alkal"));
		break;
	case IRRIGATION_TYPE_DISSOLVEOXYGEN:
		cstrType = GetTextByLan(_T("溶解氧"), _T("Dissolve Oxygen"));
		break;
	case IRRIGATION_TYPE_OXYREDUCTION:
		cstrType = GetTextByLan(_T("氧化还原"), _T("OxyReduction"));
		break;
	case IRRIGATION_TYPE_GPS:
		cstrType = GetTextByLan(_T("GPS"), _T("GPS"));
		break;
	case IRRIGATION_TYPE_RTX:
		cstrType = GetTextByLan(_T("高程"), _T("RTX"));
		break;
	case IRRIGATION_TYPE_TURBIDITY:
		cstrType = GetTextByLan(_T("浊度"), _T("Turbidity"));
		break;
	case IRRIGATION_TYPE_AMMONICA:
		cstrType = GetTextByLan(_T("氨氮"), _T("Ammonica"));
		break;
	case IRRIGATION_TYPE_WATERTEMP:
		cstrType = GetTextByLan(_T("水温"), _T("Water temp"));
		break;
	case IRRIGATION_TYPE_CONDUCTIVITY:
		cstrType = GetTextByLan(_T("电导率"), _T("Conductivity"));
		break;
	case IRRIGATION_TYPE_OXYDEMAND:
		cstrType = GetTextByLan(_T("化学需氧量"), _T("Oxydemand"));
		break;
	case IRRIGATION_TYPE_NITROGEN:
		cstrType = GetTextByLan(_T("总氮"), _T("Nitrogen"));
		break;
	case IRRIGATION_TYPE_PHOSPHORUS:
		cstrType = GetTextByLan(_T("总磷"), _T("Phosphorus"));
		break;
	case IRRIGATION_TYPE_PRESSURE_WATERLEVEL:
		cstrType = GetTextByLan(_T("压力水位"), _T("Pressure WaterLevel"));
		break;
    case IRRIGATION_TYPE_WATER_SPEED_FIELD:
        cstrType = GetTextByLan(_T("流速场"), _T("Water Speed Field"));
        break;
	case IRRIGATION_TYPE_WATER_LEVELANDFLOW:
		cstrType = GetTextByLan(_T("流量"), _T("WaterFlow"));
		break;
	case IRRIGATION_TYPE_CUMULATIVE_FLOW:
		cstrType = GetTextByLan(_T("累积流量"), _T("Total WaterFlow"));
		break;
	case IRRIGATION_TYPE_COMBUSTIBLE_GAS:
		cstrType = GetTextByLan(_T("可燃性气体"), _T("combustible gas"));
		break;
	case IRRIGATION_TYPE_OXYGEN:
		cstrType = GetTextByLan(_T("氧气"), _T("Oxygen"));
		break;
	case IRRIGATION_TYPE_CARBON_MONOXIDE:
		cstrType = GetTextByLan(_T("一氧化碳"), _T("Carbon monoxide"));
		break;
	case IRRIGATION_TYPE_HYDROGEN_SULFIDE:
		cstrType = GetTextByLan(_T("硫化氢"), _T("Hydrogen sulfide"));
		break;
	case IRRIGATION_TYPE_VOLTAGE:
		cstrType = GetTextByLan(_T("电压"), _T("Voltage"));
		break;
	case IRRIGATION_TYPE_UNDER_VOLTAGE:
		cstrType = GetTextByLan(_T("欠压报警"), _T("Under voltage alarm"));
		break;
	case IRRIGATION_TYPE_FLOAT_GAUGE_LEVEL:
		cstrType = GetTextByLan(_T("浮球液位计的液位"), _T("Liquid level of float level gauge"));
		break;
	case IRRIGATION_TYPE_FORMALDEHYDE:
		cstrType = GetTextByLan(_T("甲醛"), _T("Formaldehyde"));
		break;
	case IRRIGATION_TYPE_AMMONIA:
		cstrType = GetTextByLan(_T("氨气"), _T("Ammonia"));
		break;
	case IRRIGATION_TYPE_LIQUID_LEVEL_TRANSMITER:
		cstrType = GetTextByLan(_T("液位变送器的液位"), _T("Liquid level of level transmitter"));
		break;
	case IRRIGATION_TYPE_RAINFALL_BYTIME:
		cstrType = GetTextByLan(_T("雨量(按时间段统计)"), _T("Rainfall (calculated by time period)"));
		break;
	case IRRIGATION_TYPE_GENERATING_POWER:
		cstrType = GetTextByLan(_T("发电功率"), _T("generating power"));
		break;
	case IRRIGATION_TYPE_LOAD_POWER:
		cstrType = GetTextByLan(_T("负载功率"), _T("load power"));
		break;
	case IRRIGATION_TYPE_SEEPAGE:
		cstrType = GetTextByLan(_T("渗流水位"), _T("Seepage water level"));
		break;
	case IRRIGATION_TYPE_CHLOROPHYLL:
		cstrType = GetTextByLan(_T("叶绿素"), _T("Chlorophyll"));
		break;
	case IRRIGATION_TYPE_CHROMATICITY:
		cstrType = GetTextByLan(_T("色度"), _T("Chromaticity"));
		break;
	case IRRIGATION_TYPE_TOTAL_ORGANIC_CARBON:
		cstrType = GetTextByLan(_T("总有机碳"), _T("Total organic carbon"));
		break;
	case IRRIGATION_TYPE_BIO_OXYGENDEMAND:
		cstrType = GetTextByLan(_T("生化需氧量"), _T("Biochemical oxygen demand"));
		break;
	case IRRIGATION_TYPE_NITRITE:
		cstrType = GetTextByLan(_T("亚硝酸盐"), _T("Nitrite"));
		break;
	case IRRIGATION_TYPE_POTA_PERMAN_INDEX:
		cstrType = GetTextByLan(_T("高锰酸钾指数"), _T("Potassium permanganate index"));
		break;
	case IRRIGATION_TYPE_COLORED_DISORGANIC:
		cstrType = GetTextByLan(_T("有色溶解性有机物"), _T("Colored dissolved organic matter"));
		break;
	case IRRIGATION_TYPE_EXTINCTION_COEFFICIENT:
		cstrType = GetTextByLan(_T("消光系数"), _T("extinction coefficient"));
		break;
	case IRRIGATION_TYPE_TRANSPARENCY:
		cstrType = GetTextByLan(_T("透明度"), _T("Transparency"));
		break;
	case IRRIGATION_TYPE_SUSPENDED_SOLIDS:
		cstrType = GetTextByLan(_T("悬浮物"), _T("Suspended solids"));
		break;
	case IRRIGATION_TYPE_EUTROPHICATION_INDEX:
		cstrType = GetTextByLan(_T("富营养化指数"), _T("eutrophication index"));
		break;
    case IRRIGATION_TYPE_EVAPORATION_CAPACITY:
        cstrType = GetTextByLan(_T("蒸发量"), _T("Evaporation capicy"));
        break;
    case IRRIGATION_TYPE_OVERFLOW:
        cstrType = GetTextByLan(_T("溢水量"), _T("Overflow"));
        break;
    case IRRIGATION_TYPE_WATER_STORAGE:
        cstrType = GetTextByLan(_T("蓄水量"), _T("Water Storage"));
        break;
	default:
		break;
	}

	return cstrType;
}

CString IrrigationNotify::GetStringBySrc(int _iSrc)
{
	CString cstrSrc;
	switch (_iSrc)
	{
	case IRRIGATION_PARA_SRC_ALGO:
		cstrSrc = GetTextByLan(_T("算法获取"), _T("algorithm acquisition"));
		break;
	case IRRIGATION_PARA_SRC_EXDEV:
		cstrSrc = GetTextByLan(_T("外设获取"), _T("peripheral acquisition"));
		break;
	default:
		break;
	}

	return cstrSrc;
}

void IrrigationNotify::UpdateIrrigationNotify(int _iLogonID, int _iChannelNo, IrrigationPara* _ptIrrigationPara)
{
	if (NULL == _ptIrrigationPara)
	{
		return;
	}

	PDEVICE_INFO ptDevice = FindDevice(_iLogonID);
	if (NULL == ptDevice)
	{
		return;
	}

	CString cstrIP = ptDevice->cIP;
	CString cstrChannelNo;
	cstrChannelNo.Format("%d", _iChannelNo);
	CString cstrType;
    cstrType.Format("%s(%d)",GetStringByType(_ptIrrigationPara->iType),_ptIrrigationPara->iType);
	CString cstrValue;
	cstrValue.Format("%d", _ptIrrigationPara->iValue);
	CString cstrSrc = GetStringBySrc(_ptIrrigationPara->iSrc);
	CString cstrSceneID;
	cstrSceneID.Format("%d", _ptIrrigationPara->iSceneID);
	CString cstrRuleID;
	cstrRuleID.Format("%d", _ptIrrigationPara->iRuleID);
	CString cstrUploadTime;
	cstrUploadTime.Format("%d:%02d:%02d:%02d:%02d:%02d", _ptIrrigationPara->tUploadTime.iYear,_ptIrrigationPara->tUploadTime.iMonth,
					_ptIrrigationPara->tUploadTime.iDay, _ptIrrigationPara->tUploadTime.iHour, _ptIrrigationPara->tUploadTime.iMinute,
					_ptIrrigationPara->tUploadTime.iSecond);
	CString cstrStationNum;
	cstrStationNum.Format("%d", _ptIrrigationPara->iStationNum);
	CString cstrIrriParam8;
	cstrIrriParam8.Format("%d", _ptIrrigationPara->tIrriParam8.iCommonData);
	CString cstrIrriParam9;
	cstrIrriParam9.Format("%d", _ptIrrigationPara->tIrriParam9.iCommonData);
	CString cstrTotalPointNum;
	cstrTotalPointNum.Format("%d", _ptIrrigationPara->iTotalPointNum);
	CString cstrCurrentPointNum;
	cstrCurrentPointNum.Format("%d", _ptIrrigationPara->iCurrentPointNum);
	CString cstrBaseNum;
	cstrBaseNum.Format("%d", _ptIrrigationPara->iBaseNum);
	CString cstrRecordTime;
	cstrRecordTime.Format("%d:%d:%d:%d:%d:%d", _ptIrrigationPara->tRecordTime.iYear,_ptIrrigationPara->tRecordTime.iMonth,
		_ptIrrigationPara->tRecordTime.iDay, _ptIrrigationPara->tRecordTime.iHour, _ptIrrigationPara->tRecordTime.iMinute,
		_ptIrrigationPara->tRecordTime.iSecond);
	
	int iItemCount = m_lstIrrigationNotify.GetItemCount();
	m_lstIrrigationNotify.InsertItem(iItemCount, "");
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IP, cstrIP);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_CHAN, cstrChannelNo);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_TYPE, cstrType);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_VALUE, cstrValue);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_SRC, cstrSrc);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_SCENEID, cstrSceneID);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_RULEID, cstrRuleID);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_UPLOADTIME, cstrUploadTime);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_STATIONNUM, cstrStationNum);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM8, cstrIrriParam8);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM9, cstrIrriParam9);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_TOTALPOINTNUM, cstrTotalPointNum);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_CUUPOINTNUM, cstrCurrentPointNum);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_BASENUM, cstrBaseNum);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_RECORDTIME, cstrRecordTime);
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_FACTORYID, _ptIrrigationPara->cFactoryID);

	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM6, IntToCString(_ptIrrigationPara->iIrriParam6));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM13, IntToCString(_ptIrrigationPara->iIrriParam13));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM15, IntToCString(_ptIrrigationPara->iIrriParam15));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM16, IntToCString(_ptIrrigationPara->iIrriParam16));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM17, IntToCString(_ptIrrigationPara->iIrriParam17));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM18, IntToCString(_ptIrrigationPara->iIrriParam18));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM19, IntToCString(_ptIrrigationPara->iIrriParam19));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM20, IntToCString(_ptIrrigationPara->ulLastTimeStamp));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM21, IntToCString(_ptIrrigationPara->ulCurTimeStamp));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM22, IntToCString(_ptIrrigationPara->ulIrriParam22));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM23, IntToCString(_ptIrrigationPara->iIrriParam23));
	m_lstIrrigationNotify.SetItemText(iItemCount, COLUMN_IRRILST_IRRPARAM24, IntToCString(_ptIrrigationPara->iIrriParam24));

	if (_ptIrrigationPara->iType != IRRIGATION_TYPE_WATER_LEVELANDFLOW)
	{
		return;
	}
	iItemCount = m_lstWaterFlow.GetItemCount();
	m_lstWaterFlow.InsertItem(iItemCount, "");
	m_lstWaterFlow.SetItemText(iItemCount, 1, cstrType);
	m_lstWaterFlow.SetItemText(iItemCount, 2, cstrUploadTime);
	m_lstWaterFlow.SetItemText(iItemCount, 3, cstrValue);
	m_lstWaterFlow.SetItemText(iItemCount, 4, cstrTotalPointNum);


}


void IrrigationNotify::UI_UpdateUIText()
{
	while(m_lstIrrigationNotify.DeleteColumn(0));

	InsertColumn(m_lstIrrigationNotify, 0, "", LVCFMT_CENTER, 0);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IP, GetTextByLan(_T("IP地址"), _T("IP Address")), LVCFMT_CENTER, 120);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_CHAN, GetTextByLan(_T("通道号"), _T("ChannelNo")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_TYPE, GetTextByLan(_T("类型"), _T("Type")), LVCFMT_CENTER, 120);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_VALUE, GetTextByLan(_T("值"), _T("Value")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_SRC, GetTextByLan(_T("来源"), _T("Source")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_SCENEID, GetTextByLan(_T("场景号"), _T("SceneID")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_RULEID, GetTextByLan(_T("规则号"), _T("RuleID")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_UPLOADTIME, GetTextByLan(_T("时间"), _T("UploadTime")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_STATIONNUM, GetTextByLan(_T("站点编号"), _T("StationNum")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM8, GetTextByLan(_T("高程/水位值(mm)"), _T("Elevation/WaterLevel(mm)")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM9, GetTextByLan(_T("是否有效/组号"), _T("Effective/GroupNum")), LVCFMT_CENTER, 120);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_TOTALPOINTNUM, GetTextByLan(_T("监测点数/水尺编号"), _T("TotalPointNum/GaugeNum")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_CUUPOINTNUM, GetTextByLan(_T("当前点序号/水尺读数"), _T("CurrentPointNum/GaugeData")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_BASENUM, GetTextByLan(_T("距离基准线距离（单位mm)"), _T("BaseNum(mm)")), LVCFMT_CENTER, 170);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_RECORDTIME, GetTextByLan(_T("录像开始时间"), _T("RecordTime")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_FACTORYID, GetTextByLan(_T("设备ID"), _T("FactoryID")), LVCFMT_CENTER, 100);
	
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM6, GetTextByLan(_T("参数6"), _T("Param 6")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM13, GetTextByLan(_T("参数13"), _T("Param 13")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM15, GetTextByLan(_T("参数15"), _T("Param 15")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM16, GetTextByLan(_T("参数16"), _T("Param 16")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM17, GetTextByLan(_T("参数17"), _T("Param 17")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM18, GetTextByLan(_T("参数18"), _T("Param 18")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM19, GetTextByLan(_T("参数19"), _T("Param 19")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM20, GetTextByLan(_T("参数20"), _T("Param 20")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM21, GetTextByLan(_T("参数21"), _T("Param 21")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM22, GetTextByLan(_T("参数22"), _T("Param 22")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM23, GetTextByLan(_T("参数23"), _T("Param 23")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstIrrigationNotify, COLUMN_IRRILST_IRRPARAM24, GetTextByLan(_T("参数24"), _T("Param 24")), LVCFMT_CENTER, 60);

	m_lstIrrigationNotify.DeleteAllItems();

	while(m_lstWaterFlow.DeleteColumn(0));

	InsertColumn(m_lstWaterFlow, 0, "", LVCFMT_CENTER, 0);
	InsertColumn(m_lstWaterFlow, 1, GetTextByLan(_T("类型"), _T("Type")), LVCFMT_CENTER, 120);
	InsertColumn(m_lstWaterFlow, 2, GetTextByLan(_T("时间"), _T("UploadTime")), LVCFMT_CENTER, 240);
	InsertColumn(m_lstWaterFlow, 3, GetTextByLan(_T("水位"), _T("WaterLevel")), LVCFMT_CENTER, 100);
	InsertColumn(m_lstWaterFlow, 4, GetTextByLan(_T("流量"), _T("WaterFlow")), LVCFMT_CENTER, 180);
	m_lstWaterFlow.DeleteAllItems();

    m_cboDataType.ResetContent();
    for (int i = 1; i < MAX_IRRIGATION_TYPE  ;i++)
    {
        m_cboDataType.SetItemData(m_cboDataType.AddString(GetStringByType(i)),i);
    }
    m_cboDataType.SetCurSel(0);
    SetDlgItemText(IDC_STATIC_DATA_TYPE,GetTextByLan(_T("数据类型"),_T("DataType")));
    SetDlgItemText(IDC_STATIC_CUSTOM_DATATYPE,GetTextByLan(_T("自定义数据类型"),_T("Custom DataType")));
    SetDlgItemText(IDC_STATIC_INDEX,GetTextByLan(_T("序号"),_T("Index")));
    SetDlgItemText(IDC_STATIC_PAGE_INDEX,GetTextByLan(_T("页号"),_T("PageIndex")));
    SetDlgItemText(IDC_BUTTON_QUERY,GetTextByLan(_T("查询"),_T("Query")));
    SetDlgItemText(IDC_BUTTON_QUERY_CUSTOM_TYPE,GetTextByLan(_T("查询自定义数据类型"),_T("Query Custom DataType")));
}

void IrrigationNotify::OnLvnItemchangedListIrrigationNotify(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
	// TODO: Add your control notification handler code here
	*pResult = 0;
}


void IrrigationNotify::OnBnClickedButtonQuery()
{
    IrriGationRecord tIrriGationRecord = {0};
    IrriGationRecordResult tResult = {0};
    tIrriGationRecord.iSize = (int)sizeof(IrriGationRecord);
    CString strIndex = "";
    m_iChannelNo = 0;
    tIrriGationRecord.iPageNo = 0;
    tIrriGationRecord.iPageSize = 20; //默认每页20条
    tIrriGationRecord.iType = m_cboDataType.GetItemData(m_cboDataType.GetCurSel());

    tIrriGationRecord.iSeqStart = GetDlgItemInt(IDC_EDIT_START_INDEX);
    tIrriGationRecord.iSeqStop = GetDlgItemInt(IDC_EDIT_END_INDEX);
    tIrriGationRecord.iPageNo = GetDlgItemInt(IDC_EDIT_PAGE_INDEX);
    int iRet = NetClient_CmdConfig(m_iLogonID, CMD_IRRIGATIONRECORD, m_iChannelNo, &tIrriGationRecord, tIrriGationRecord.iSize, &tResult, (int)sizeof(tResult));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_CmdConfig]CMD_IRRIGATIONRECORD fail!");
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "[NetClient_CmdConfig]CMD_IRRIGATIONRECORD SUCCESS!");
    }
}

void IrrigationNotify::OnBnClickedButtonQueryCustomType()
{
    // TODO: Add your control notification handler code here
    IrriGationRecord tIrriGationRecord = {0};
    IrriGationRecordResult tResult = {0};
    tIrriGationRecord.iSize = (int)sizeof(IrriGationRecord);
    CString strIndex = "";
    m_iChannelNo = 0;
    tIrriGationRecord.iPageNo = 0;
    tIrriGationRecord.iPageSize = 20; //默认每页20条
    tIrriGationRecord.iType =GetDlgItemInt(IDC_EDIT_DATATYPE);

    tIrriGationRecord.iSeqStart = GetDlgItemInt(IDC_EDIT_START_INDEX);
    tIrriGationRecord.iSeqStop = GetDlgItemInt(IDC_EDIT_END_INDEX);
    tIrriGationRecord.iPageNo = GetDlgItemInt(IDC_EDIT_PAGE_INDEX);
    int iRet = NetClient_CmdConfig(m_iLogonID, CMD_IRRIGATIONRECORD, m_iChannelNo, &tIrriGationRecord, tIrriGationRecord.iSize, &tResult, (int)sizeof(tResult));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_CmdConfig]CMD_IRRIGATIONRECORD fail!");
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "[NetClient_CmdConfig]CMD_IRRIGATIONRECORD SUCCESS!");
    }
}

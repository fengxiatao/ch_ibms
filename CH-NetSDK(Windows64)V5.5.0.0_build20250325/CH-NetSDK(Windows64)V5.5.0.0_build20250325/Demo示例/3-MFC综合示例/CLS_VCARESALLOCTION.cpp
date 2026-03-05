// CLS_VCARESALLOCTION.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_VCARESALLOCTION.h"

//template
#define TEMPLATE_UNREATED       -1
#define TEMPLATE_OUTDOOR        0
#define TEMPLATE_INDOOR         1
#define TEMPLATE_TRAFFIC        2
#define TEMPLATE_WIDE_DYNAMIC   3
#define TEMPLATE_MOTION         4
#define TEMPLATE_HIGHTLIGHT     5
#define TEMPLATE_COLORFUL       6
#define TEMPLATE_CUSTOM         7
#define TEMPLATE_RUNNING        8
#define TEMPLATE_LOW_LIGHT      9
#define TEMPLATE_OUTDOOR_2       10
#define TEMPLATE_INDOOR_2        11
#define TEMPLATE_TRAFFIC_2       12
#define TEMPLATE_WIDE_DYNAMIC_2  13
#define TEMPLATE_MOTION_2        14
#define TEMPLATE_HIGHTLIGHT_2     15
#define TEMPLATE_COLORFUL_2       16
#define TEMPLATE_CUSTOM_2         17

// CLS_VCARESALLOCTION dialog

IMPLEMENT_DYNAMIC(CLS_VCARESALLOCTION, CDialog)

CLS_VCARESALLOCTION::CLS_VCARESALLOCTION(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCARESALLOCTION::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNO = -1;
}

CLS_VCARESALLOCTION::~CLS_VCARESALLOCTION()
{
}

void CLS_VCARESALLOCTION::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_RESOURCE, m_comResourceID);
    DDX_Control(pDX, IDC_COMBO_ALGORITHM_TYPE, m_comAlgorithmType);
    DDX_Control(pDX, IDC_COMBO_DAY_TEMPLATE, m_comDayTemplate);
    DDX_Control(pDX, IDC_COMBO_NIGHT_TEMPLATE, m_comNightTemplate);
}


BEGIN_MESSAGE_MAP(CLS_VCARESALLOCTION, CDialog)
	ON_BN_CLICKED(IDC_BUTTON1, &CLS_VCARESALLOCTION::OnBnClickedButton1)
    ON_CBN_SELCHANGE(IDC_COMBO_ALGORITHM_TYPE, &CLS_VCARESALLOCTION::OnCbnSelchangeComboAlgorithmType)
END_MESSAGE_MAP()


// CLS_VCARESALLOCTION message handlers
BOOL CLS_VCARESALLOCTION::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	SetDlgItemText(IDC_BUTTON1, GetTextByLan(_T("设置"),_T("Set")));
	SetDlgItemText(IDC_STATIC_RESOURCE, GetTextByLan(_T("资源ID"),_T("ResourceID")));

	SetDlgItemText(IDC_STATIC_ASSOCIATION_TEMPLATE, GetTextByLan(_T("关联模板列表"),_T("AssociatedTemplateList")));
    SetDlgItemText(IDC_STATIC_ALGORITHM_TYPE, GetTextByLan(_T("算法类型"),_T("Algorithm Type")));
    SetDlgItemText(IDC_STATIC_DAY_TEMPLATE, GetTextByLan(_T("日间模板"),_T("Day Template")));
    SetDlgItemText(IDC_STATIC_NIGHT_TEMPLATE, GetTextByLan(_T("夜间模板"),_T("Night Template")));

    m_comAlgorithmType.ResetContent();
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_BEHAVIOR)), VCA_ALG_BEHAVIRO_ANALYSIS);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_CARNUM)), VCA_ALG_PLATE_DISTINGUISH);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_FACE)), VCA_ALG_FACE_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_VIDEO)), VCA_ALG_VIDEO_DIAGNOSIS);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_SMART_TRACK )), VCA_ALG_INTELLIGENT_TRACKING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_PEOPLENUM)), VCA_ALG_PEOPLE_STATISTICS);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_PEOPLECROWD)), VCA_ALG_CROWD_GATHERED);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_DUTY)), VCA_ALG_DUTY_INSPECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_EVENT_AUDIO_DIAGNOSE_NEW)), VCA_ALG_AUDIO_DIAGNOSIS);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_CFG_FUNC_FACE_COVER)), VCA_ALG_FACE_MOSAIC);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_CONFIG_ITS_ILLEGALPARK)), VCA_ALG_WRONG_PARKING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_EVENT_FIGHT)), VCA_ALG_FIGHTING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("人脸检测ST"),_T("Face DETECT ST"))), VCA_ALG_FACE_DETECTION_ST);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_CFG_SEEPER)), VCA_ALG_STAYWATER_MONITOR);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_WINDOW_DETECTION)), VCA_ALG_OVERWINDOW_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_EVENT_FACEREC)), VCA_ALG_FACE_RECOGNITION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_CONFIG_ITS_ILLEGALPARK)), VCA_ALG_PARKING_GUARD);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("目标检出(背景建模)"),_T("Target Detection"))), VCA_ALG_TARGET_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_CFG_HELMET)), VCA_ALG_HELMET_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("联动球机跟踪"),_T("PTZ TRACKING"))), VCA_ALG_PTZ_TRACKING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_EVENT_COLOR_TRACK)), VCA_ALG_COLOR_TRACKING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_STRUCTURED)), VCA_ALG_STRUCTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("积水深度"), _T("depth of water"))), VCA_ALG_STAYWATER_DEPTH);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_CFG_ALERTWATER)), VCA_ALG_WATER_LEVEL_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("单人询问"), _T("SINGLE_INQUIRY"))), VCA_ALG_UNATTENDED);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("攀高"), _T("CLIMB_UP"))), VCA_ALG_CLIMB_HIGHER);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("新离岗"), _T("NET_DEPARTURE"))), VCA_ALG_NEW_DEPARTURE);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("人数异常"), _T("AbNormal Num"))), VCA_ALG_PEOPLE_NUMBER_ABNORMAL);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("人员起身"), _T("GetUp"))), VCA_ALG_STAND_UP);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("离床"), _T("LEAVE_BED"))), VCA_ALG_OUT_BED);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("静止检测"), _T("STATIC_DETECTION"))), VCA_ALG_STATIC_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("睡岗"), _T("SLEEP_POSTION"))), VCA_ALG_SLEEPING_STATION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("摔倒"), _T("SLIP_UP"))), VCA_ALG_FALL);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("新打架"), _T("NEW_FIGHT"))), VCA_ALG_NEW_FIGHTING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("肢体接触"), _T("BODY_TOUCH"))), VCA_ALG_BODY_TOUCH);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("人形检测"), _T("Human detect"))), VCA_ALG_HUMAN_SHAPE_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("坝前堆积物检测"), _T("Dam Deposit"))), VCA_ALG_DAM_DEPOSIT_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_RIVER_RECGMODE_BLOCK)), VCA_ALG_STATION_DEPOSIT_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("油田监控"), _T("PEPT"))), VCA_ALG_OILFIELD_MONITORING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("水流速"), _T("WaterSpeed"))), VCA_ALG_WATER_SPEED_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("航标船检测"), _T("NavigationShip Detection"))), VCA_ALG_NAVIGATION_MARK_SHIP);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("明厨亮灶算法"), "ChefHatDetect")), VCA_ALG_BRIGHT_KITCHEN);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("滞留"), _T("Stranded"))), VCA_ALG_RETENTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("单人独处"), _T("Alone"))), VCA_ALG_SIGNALE_MAN);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("隔窗递物"), _T("WINDOW_DELIVERY"))), VCA_ALG_WINDOW_DELIVERY);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_ALARM_EVENT_LOITER)), VCA_ALG_LINGER);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCA_ABANDUM)), VCA_ALG_GOODS_LEFT);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_VCAEVENT_GOODS_LOSE)), VCA_ALG_GOODS_LOST);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextEx(IDS_CFG_FEC_HEAT_MAP)), VCA_ALG_HEAT_MAP);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("吸烟"), _T("Smoke"))), VCA_ALG_SMOKING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan("打电话", "CallPhone")), VCA_ALG_PHONGING);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("温度检测"), _T("TemDetect"))), VCA_ALG_TEMPERATURE_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("烟火检测"), _T("FireDetect"))), VCA_ALG_PYROTECHNIC_DETECTION);
    m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("智能侦测"), _T("SmartMove"))), VCA_ALG_INTELLIGENT_DETECTION);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("讯问超时"), _T("InuiryTimeout"))), VCA_ALG_INUIRY_TIMEOUT);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("室内电动车检测"), _T("IndooElectricVehicle"))), VCA_ALG_ELECTRIC_VEHICLE);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("人员密度"), _T("PersonnelDensity"))), VCA_ALG_PERSON_DENSITY);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("车辆密度"), _T("VehicleDensity"))), VCA_ALG_VEHICLE_DENSITY);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("车辆拥堵"), _T("TrafficJam"))), VCA_ALG_TAFFIC_JAM);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("车辆滞留"), _T("VehicleStranded"))), VCA_ALG_VEHICLE_STANDED);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("异常停车"), _T("AbnormalParking"))), VCA_ALG_ABNORMAL_PARKING);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("交叉拥堵"), _T("CrossCongestion"))), VCA_ALG_CROSS_CONGESTION);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("法官行为分析"), _T("JudgeAnalysis"))), VCA_ALG_JUDGE_BEHAVIOR_ANALYZE);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("垂直检人"), _T("VerticalHuman"))), VCA_ALG_VERTICALHUMAN_DETECTION);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("高空抛物"), _T("AerialProjectile"))), VCA_ALG_AERIAL_PROJECTILE);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("排污口监测"), _T("SewageOutfallMonitoring"))), VCA_ALG_WATER_OUTFALL);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("民警警服检测"), _T("PoliceUniform"))), VCA_ALG_POLICE_UNIFORM_DETECTION);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("被监管人员识别服检测"), _T("SupervisedPersonnelIdentifyDress"))), VCA_ALG_SPDRESS_DETECTION);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("被监管人员队列检测"), _T("SupervisedPersonnelQueue"))), VCA_ALG_SPQUEUE_DETECTION);
	m_comAlgorithmType.SetItemData(m_comAlgorithmType.AddString(GetTextByLan(_T("主动策略"), _T("ActiveStrategy"))), VCA_ALG_ACTIVE_STRATEGY);
    m_comAlgorithmType.SetCurSel(0);

    initComboBoxTemplate(m_comDayTemplate);
    initComboBoxTemplate(m_comNightTemplate);
	return TRUE;
}

void CLS_VCARESALLOCTION::initComboBoxTemplate(CComboBox& combo)
{
    combo.ResetContent();
    combo.SetItemData(combo.AddString(GetTextByLan(_T("不关联"), _T("unreated"))), TEMPLATE_UNREATED);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("室外"), _T("out door"))), TEMPLATE_OUTDOOR);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("室内"), _T("in door"))), TEMPLATE_INDOOR);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("交通"), _T("traffic"))), TEMPLATE_TRAFFIC);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("宽动态"), _T("wide dynamic"))), TEMPLATE_WIDE_DYNAMIC);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("运动"), _T("motion"))), TEMPLATE_MOTION);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("高亮"), _T("hight light"))), TEMPLATE_HIGHTLIGHT);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("鲜艳"), _T("colorful"))), TEMPLATE_COLORFUL);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("自定义"), _T("custom"))), TEMPLATE_CUSTOM);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("运行"), _T("running"))), TEMPLATE_RUNNING);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("低照"), _T("low light"))), TEMPLATE_LOW_LIGHT);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("室外2"), _T("out door2"))), TEMPLATE_OUTDOOR_2);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("室内2"), _T("in door2"))), TEMPLATE_INDOOR_2);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("交通2"), _T("traffic2"))), TEMPLATE_TRAFFIC_2);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("宽动态2"), _T("wide dynamic2"))), TEMPLATE_WIDE_DYNAMIC_2);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("运动2"), _T("motion2"))), TEMPLATE_MOTION_2);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("高亮2"), _T("hight light2"))), TEMPLATE_HIGHTLIGHT_2);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("鲜艳2"), _T("colorful2"))), TEMPLATE_COLORFUL_2);
    combo.SetItemData(combo.AddString(GetTextByLan(_T("自定义2"), _T("custom2"))), TEMPLATE_CUSTOM_2);
    combo.SetCurSel(0);
}

void CLS_VCARESALLOCTION::OnBnClickedButton1()
{
	// TODO: Add your control notification handler code here
	int iEnable = 1;
	iEnable = m_comResourceID.GetItemData(m_comResourceID.GetCurSel());
	int iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_VCA_RESOURCE, m_iChannelNO, iEnable);
	if (iRet ==RET_SUCCESS)
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_VCARESALLOCTION]OnBnClickedButton1 Success(%d)", iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCARESALLOCTION]OnBnClickedButton1 Fail(%d)", m_iLogonID);
	}
}


void CLS_VCARESALLOCTION::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	int iRet = RET_FAILED;
	if (m_iLogonID < 0)
	{
		m_iLogonID = _iLogonID;
	}
	if (m_iChannelNO < 0)
	{
		m_iChannelNO = _iChannelNo;
	}
	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	stFuncAbilityLevel.iSubFuncType = 54;
	int iReturnByte = -1;
	int iResult = -1;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO,&stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (RET_SUCCESS == iRet && 0 < strlen(stFuncAbilityLevel.cParam))
	{
		iResult = _ttoi(stFuncAbilityLevel.cParam);	
		AddFuncToCombox(iResult);
		int iEventType = -1;
		iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_VCA_RESOURCE, m_iChannelNO, &iEventType);
		if (RET_SUCCESS == iRet && iEventType != -1)
		{
			int iFlag = 0;
			for (iFlag = 0; iFlag < m_comResourceID.GetCount(); iFlag++)
			{
				int iResourceID = m_comResourceID.GetItemData(iFlag);
				if (iResourceID == iEventType)
				{
					m_comResourceID.SetCurSel(iFlag);
					break;
				}
			}
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_VCARESALLOCTION]NetClient_GetDevConfig NET_CLIENT_GET_FUNC_ABILITY failed(%d)", m_iLogonID);
	}
}

void CLS_VCARESALLOCTION::AddFuncToCombox(int iResult)
{
	m_comResourceID.ResetContent();

	if ((iResult & 0x0001) > 0) { //bit0
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能事件"), _T("Smart Event"))), 4);
	}
	if ((iResult & 0x0002) > 0) { //bit1
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("人脸抓拍"), _T("Face Snap"))),1);
	}
	if ((iResult & 0x0004) > 0) { //bit2
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("人脸识别"), _T("Face Recog"))),2);
	}
	if ((iResult & 0x0008) > 0) { //bit3
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("水利监控"), _T("IRRIGATION"))),8);
	}
	if ((iResult & 0x0010) > 0) { //bit4
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("水利预警站"), _T("IRRIGATION STATION"))), 16);
	}
	if ((iResult & 0x0020) > 0) { //bit5
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("交通流量统计"), _T("ITS Flow"))), 32);
	}
	if ((iResult & 0x0040) > 0) { //bit6
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能监委"), _T("Smart Detection"))),64);
	}
	if ((iResult & 0x0080) > 0) { //bit7
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能公安"), _T("Smart Police"))),128);
	}
	if ((iResult & 0x0100) > 0) { //bit8
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能教育"), _T("Smart Edu"))), 256);
	}
	if ((iResult & 0x0200) > 0) { //bit9
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能管教"), _T("Smart Teach"))), 512);
	}
	if ((iResult & 0x0400) > 0) { //bit10
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("道路检测"), _T("Road Detect"))), 1024);
	}
	if ((iResult & 0x0800) > 0) { //bit11
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("混合监测"), _T("Mix Detect"))), 2048);
	}
	if ((iResult & 0x1000) > 0) { //bit12
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能交通"), _T("Smart ITS"))), 4096);
	}
	if ((iResult & 0x2000) > 0) { //bit13
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能监管"), _T("Intelligent Regulation"))), 8192);
	}
	if ((iResult & 0x4000) > 0) { //bit14
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("道路预警"), _T("Road Warning"))), 16384);
	}
	if ((iResult & 0x8000) > 0) { //bit15
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("事件检测"), _T("Event Detection"))), 32768);
	}
	if ((iResult & 0x10000) > 0) { //bit16
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("环保卡口"), _T("Environmental protection bayonet"))), 0x10000);
	}
	if ((iResult & 0x20000) > 0) { //bit17
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("视频事件检测"), _T("Video event detection"))), 0x20000);
	}
	if ((iResult & 0x40000) > 0) { //bit18
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("场所点名"), _T("Place roll call"))), 0x40000);
	}
	if ((iResult & 0x80000) > 0) { //bit19
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能定位"), _T("Intelligent positioning"))), 0x80000);
	}
	if ((iResult & 0x100000) > 0) { //bit20
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("监控人脸"), _T("Monitor faces"))), 0x100000);
	}
	if ((iResult & 0x200000) > 0) { //bit21
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("低延时"), _T("Low latency"))), 0x200000);
	}
	if ((iResult & 0x400000) > 0) { //bit22
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("监控"), _T("monitor "))), 0x400000);
	}
	if ((iResult & 0x800000) > 0) { //bit23
		m_comResourceID.SetItemData(m_comResourceID.AddString(GetTextByLan(_T("智能跟踪"), _T("Intelligent tracking"))), 0x800000);
	}
}

void CLS_VCARESALLOCTION::OnCbnSelchangeComboAlgorithmType()
{
    if (m_iLogonID == -1 || m_iChannelNO == -1)
    {
        return;
    }

    DefaultTemplateList st = {0};
    st.iSize = sizeof(st);
    st.iType = m_comAlgorithmType.GetItemData(m_comAlgorithmType.GetCurSel());
    int iCmd = VCA_CMD_DEFAULTTEMPLATELIST;
    int iRet = NetClient_VCAGetConfig(m_iLogonID, iCmd, m_iChannelNO, &st, sizeof(st));
    if (RET_SUCCESS == iRet)
    {
        int iCount = m_comDayTemplate.GetCount();
        for (int x=0; x<iCount; x++)
        {
            if (m_comDayTemplate.GetItemData(x) == st.iDaytimeDef)
            {
                m_comDayTemplate.SetCurSel(x);
                break;
            }
        }

        iCount = m_comNightTemplate.GetCount();
        for (int x=0; x<iCount; x++)
        {
            if (m_comNightTemplate.GetItemData(x) == st.iDaytimeDef)
            {
                m_comNightTemplate.SetCurSel(x);
                break;
            }
        }
    }
    else
    {
        AddLog(LOG_TYPE_FAIL, "", "NetClient_VCAGetConfig(%d,%d,%d)error=%d",m_iLogonID, iCmd,m_iChannelNO,GetLastError());
    }
}

// ItsTrafficStatistics.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "ItsTrafficStatistics.h"

char* pcRunStateCN[] = {"unknown", "stop", "slow", "no car", "unobstructed"};
char* pcRunStateEN[] = {"UNKnown", "Suspended", "LowerSpeed", "ZeroCar", "Free"};

typedef struct tagCommonRelation
{
	int		iId;
	CString cstrValue;
}CommonRelation, *pCommonRelation;

const CommonRelation g_tCatTypeCN[] =
{
	{0, "unknown"},
	{1, "Hatchback sedan"},
	{2, "Sedan"},
	{3, "Coupe"},
	{4, "Sedans"},
	{5, "minicar"},
	{6, "MPV"},
	{7, "SUV"},
	{8, "Large bus"},
	{9, "Medium bus"},
	{10, "Van"},
	{11, "minivan"},
	{12, "Heavy Goods Vehicles"},
	{13, "Medium Truck"},
	{14, "Tank Truck"},
	{15, "Crane"},
	{16, "muck truck"},
	{17, "minivan"},
	{18, "Pickup"},
	{19, "minivan"},
	{20, "Motorcycle"},
	{21, "Trailer"},
	{22, "pedestrian"},
	{23, "License plate misaligned"},
	{24, "License plate detection"},
	{25, "Front"},
	{26, "Tail"},
	{27, "Car Lights"},
	{28, "SUV/MPV"},
	{29, "Trailer"},
	{1030, "Small Car"},
	{1031, "Large Vehicle"},
	{1032, "Medium Car"},
	{1033, "Oversized Vehicle"}
};
#define  MAX_CAR_TYPE		((int)(sizeof(g_tCatTypeCN)/sizeof(CommonRelation)))

const CommonRelation g_tCatTypeEN[] =
{
	{0,	"UNKnown"},
	{1,	"HatchbackCar"},
	{2,	"Saloon"},
	{3,	"Coupe"},
	{4, "Compact"},
	{5, "Mini"},
	{6, "MPV"},
	{7, "SUV"},
	{8, "LargeBus"},
	{9, "MediumBus"},
	{10, "Van"},
	{11, "Microvan"},
	{12, "HGV"},
	{13, "MediumTruck"},
	{14, "TankTruck"},
	{15, "Crane"},
	{16, "MuckCar"},
	{17, "Buggy"},
	{18, "Pick-upTrunk"},
	{19, "MicroTrunk"},
	{20, "Motorcycle"},
	{21, "Trailer"},
	{22, "Pedestrain"},
	{23, "L	icense plate hung off-center"},
	{24, "License plate detection"},
	{25, "Locomotive"},
	{26, "Caboose"},
	{27, "Headlights"},
	{28, "SUV/MPV"},
	{29, "Trailer"},
	{1030, "Small car"},
	{1031, "Large car"},
	{1032, "Medium car"},
	{1033, "Super jumbo"}
};

// Cls_ItsTrafficStatistics dialog

IMPLEMENT_DYNAMIC(Cls_ItsTrafficStatistics, CDialog)

Cls_ItsTrafficStatistics::Cls_ItsTrafficStatistics(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(Cls_ItsTrafficStatistics::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

Cls_ItsTrafficStatistics::~Cls_ItsTrafficStatistics()
{
}

void Cls_ItsTrafficStatistics::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_GP_TRAFFIC_STATIS, m_gpTrafficStatis);
	DDX_Control(pDX, IDC_CHK_USE_TRAFFIC_STATIC, m_chkUseTrafficStatis);
	DDX_Control(pDX, IDC_STC_LABLE_OF_PART_OF_STATIS, m_stcPartOfStatis);
	DDX_Control(pDX, IDC_EDT_INPUT_PART, m_edtInputPart);
	DDX_Control(pDX, IDC_BTN_SET_PART, m_btnSetPart);
	DDX_Control(pDX, IDC_EDT_SHOW_STATIS, m_edtShowPart);
	DDX_Control(pDX, IDC_BTN_CLEAN_UP, m_btnCleanUp);
	DDX_Control(pDX, IDC_CBO_ROADID, m_cboRoadID);
	DDX_Control(pDX, IDC_LIST_FLOW_RESULT, m_LisFlowResult);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FLOW_QUERY_START, m_DtFlowStartTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FLOW_QUERY_END, m_DtFlowEndTime);
}


BEGIN_MESSAGE_MAP(Cls_ItsTrafficStatistics, CLS_BasePage)
	ON_BN_CLICKED(IDC_BTN_SET_PART, &Cls_ItsTrafficStatistics::OnBnClickedBtnSetPart)
	ON_BN_CLICKED(IDC_BTN_CLEAN_UP, &Cls_ItsTrafficStatistics::OnBnClickedBtnCleanUp)
	ON_CBN_SELCHANGE(IDC_CBO_ROADID, &Cls_ItsTrafficStatistics::OnCbnSelchangeCboRoadid)
	ON_BN_CLICKED(IDC_BUTTON_FLOW_QUERY, &Cls_ItsTrafficStatistics::OnBnClickedButtonFlowQuery)
END_MESSAGE_MAP()


// Cls_ItsTrafficStatistics message handler

CString Cls_ItsTrafficStatistics::GetValueByID(int _iIndex)
{
	CString cstrValue = "UNKnown";
	
	for(int i=0; i<MAX_CAR_TYPE; i++)
	{
		if (_iIndex == g_tCatTypeCN[i].iId)
		{
			cstrValue = GetTextByLan(g_tCatTypeCN[i].cstrValue, g_tCatTypeEN[i].cstrValue);
			break;
		}
	}

	return cstrValue;
}

BOOL Cls_ItsTrafficStatistics::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_LisFlowResult.SetExtendedStyle(LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT);

	UI_UpdateDialog();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void Cls_ItsTrafficStatistics::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if(_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
	
	UI_UpdateStatis();
}

void Cls_ItsTrafficStatistics::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void Cls_ItsTrafficStatistics::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_GP_TRAFFIC_STATIS, IDS_ITS_TRAFFIC_FROW);
	SetDlgItemTextEx(IDC_CHK_USE_TRAFFIC_STATIC, IDS_ITS_USE_TRAFFIC_FROW);
	SetDlgItemTextEx(IDC_STC_LABLE_OF_PART_OF_STATIS, IDS_ITS_STASTIC_INTERVAL);
	SetDlgItemTextEx(IDC_BTN_CLEAN_UP, IDS_ITS_CLEAR_UP);
	SetDlgItemTextEx(IDC_BTN_SET_PART, IDS_ITS_SET_TRAFFIC_LIGHT_GAIN);
	SetDlgItemTextEx(IDC_STC_ROADID, IDS_CONFIG_ITS_ITSPARAM_ROADID);
	SetDlgItemText(IDC_STATIC_FLOW_QUERY, GetTextByLan("流量查询", "FlowQuery"));
	SetDlgItemText(IDC_STATIC_PAGENO, GetTextByLan("页码", "PageNo"));
	SetDlgItemText(IDC_STATIC_PAGESIZE, GetTextByLan("每页条数", "PageSize"));
	SetDlgItemText(IDC_STATIC_TIMERANGE, GetTextByLan("时间范围", "TimeRange"));
	SetDlgItemText(IDC_BUTTON_FLOW_QUERY, GetTextByLan("查询", "Query"));

	SetDlgItemText(IDC_STATIC_TOTAL_STATIS, GetTextByLan("总记录条数", "TotalStatis"));
	SetDlgItemInt(IDC_STATIC_TOTALSTATIS_VALUE, 0);
	SetDlgItemText(IDC_STATIC_TOTAL_PAGE, GetTextByLan("总页数", "TotalPage"));
	SetDlgItemInt(IDC_STATIC_TOTAL_PAGE_VALUE, 0);

	SetDlgItemInt(IDC_EDIT_FLOW_PAGESIZE, FACE_MAX_PAGE_COUNT);
	SetDlgItemInt(IDC_EDIT_FLOW_PAGENO, 1);

	for(int i = 0; i < MAX_ROADWAY_COUNT; i++)
	{
		CString strNo;
		strNo.Format("%d", i);
		InsertString(m_cboRoadID, i, strNo);
	}
	m_cboRoadID.SetCurSel(0);
	m_edtInputPart.SetLimitText(4);

	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_INDEX, GetTextByLan(_T("序号"),_T("INDEX")), LVCFMT_CENTER, 50);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_PAGENO, GetTextByLan(_T("页码"),_T("PageNo")), LVCFMT_CENTER, 50);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_LANEID, GetTextByLan(_T("车道编号"),_T("LaneID")), LVCFMT_CENTER, 70);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_ROADNAME, GetTextByLan(_T("车道名称"),_T("RoadName")), LVCFMT_CENTER, 100);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_TIMERANGE, GetTextByLan(_T("时间范围"),_T("TimeRange")), LVCFMT_CENTER, 280);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_FLOW, GetTextByLan(_T("流量 辆/时间间隔"),_T("Flow per/TimeInterval")), LVCFMT_CENTER, 150);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_HOLDRATE, GetTextByLan(_T("时间占有率 %"),_T("HoldRate %")), LVCFMT_CENTER, 100);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_SPEED, GetTextByLan(_T("平均速度 Km/h"),_T("Speed Km/h")), LVCFMT_CENTER, 120);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_DISTANCE, GetTextByLan(_T("平均车头时距 (秒)"),_T("Distance (s)")), LVCFMT_CENTER, 120);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_CARTYPETOTAL, GetTextByLan(_T("车辆类型总数"),_T("CarTypeTotal")), LVCFMT_CENTER, 100);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_CARTYPESTR, GetTextByLan(_T("车辆类型统计"),_T("CarTypeStr")), LVCFMT_CENTER, 500);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_CARQUEUELEN, GetTextByLan(_T("车辆排队长度(毫米)"),_T("QueueLen(mm)")), LVCFMT_CENTER, 120);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_HEADDISTANCE, GetTextByLan(_T("车头间距(毫米)"),_T("HeadDistance(mm)")), LVCFMT_CENTER, 120);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_ROOMRATE, GetTextByLan(_T("空间占有率 %"),_T("RoomRate %")), LVCFMT_CENTER, 100);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_RUNSTATE, GetTextByLan(_T("通行状态"),_T("RunState")), LVCFMT_CENTER, 100);
	InsertColumn(m_LisFlowResult, n_LIST_TRAFFICFLOW_SCENEID, GetTextByLan(_T("场景编号 1~32"),_T("SceneID 1~32")), LVCFMT_CENTER, 100);
	m_LisFlowResult.DeleteAllItems();

	m_DtFlowStartTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_DtFlowEndTime.SetFormat("yyyy-MM-dd HH:mm:ss");

	CTime SystemTime; 
	m_DtFlowStartTime.GetTime(SystemTime);
	CTime BeginTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 0, 0, 0);
	m_DtFlowStartTime.SetTime(&BeginTime);

	m_btnSetPart.ShowWindow(SW_HIDE);
	
	UI_UpdateStatis();
}
//Set traffic statistics
void Cls_ItsTrafficStatistics::OnBnClickedBtnSetPart()
{
	int iRet = -1;
	for (int iRoad = 0; iRoad < MAX_ROADWAY_COUNT; iRoad++)//4 lanes
	{
		ITSTrafficFlow tSetTrafficFlow;
		memset( &tSetTrafficFlow, 0, sizeof(ITSTrafficFlow));

		tSetTrafficFlow.iBufSize = sizeof(ITSTrafficFlow);
		tSetTrafficFlow.iLaneID = m_cboRoadID.GetCurSel();
		tSetTrafficFlow.iType = 1;
		tSetTrafficFlow.iEnable = m_chkUseTrafficStatis.GetCheck();
		tSetTrafficFlow.iTimeInterval = GetDlgItemInt(IDC_EDT_INPUT_PART);
		iRet = NetClient_SetITSExtraInfo(m_iLogonID, ITS_EXTRAINFO_CMD_TRAFFIC_FLOW, m_iChannelNo, &tSetTrafficFlow, sizeof(ITSTrafficFlow));
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSExtraInfo[ITS_LIGHTINFO_CMD_GET][ITS_EXTRAINFO_CMD_TRAFFIC_FLOW] (%d, %d)",m_iLogonID, m_iChannelNo);
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSExtraInfo[ITS_LIGHTINFO_CMD_GET] (%d, %d),error(%d)",m_iLogonID, m_iChannelNo, GetLastError());
		}
	}
}

void Cls_ItsTrafficStatistics::OnMainNotify( int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser )
{
	////char cOutPut[LEN_256] = {0};
	CString cstrOutPut;
	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_ITS_TRAFFICFLOWREPORT:
		{
			ITSTrafficFlowReport strctTrafficFlow = {0};
			ITSTrafficFlowReport* ptITSTrafficFlowReport = (ITSTrafficFlowReport*)_iLParam;
			if (NULL != ptITSTrafficFlowReport)
			{
				//There is still a possibility of collapse if the bottom layer does not pass the length, to be retested
				int iCpySize = min(ptITSTrafficFlowReport->iSize, sizeof(ITSTrafficFlowReport));
				memcpy(&strctTrafficFlow, ptITSTrafficFlowReport, iCpySize);
			}
			strctTrafficFlow.iLaneID = m_cboRoadID.GetCurSel();
			CString cstrCarType;
			for (int i = 0; i < strctTrafficFlow.iCarTypeTotal && i < LEN_32; i++)
			{
				CString cstrCarInfo;
				cstrCarInfo.Format("%s:%d", GetValueByID(strctTrafficFlow.tCarTypeNum[i].iCarType), strctTrafficFlow.tCarTypeNum[i].iCarNum);
				
				cstrCarType += cstrCarInfo;
				CString SplitSection = i < strctTrafficFlow.iCarTypeTotal-1 ? ", ":".";
				cstrCarType += SplitSection;
			}
			cstrOutPut.Format(GetTextByLan("车道: %d  流量: %d  时间占有率: %0.2f  平均速度: %0.2f  车头距离: %0.2f\r\n开始时间: %d年%02d月%02d日%02d:%02d:%02d - 结束时间: %d年%02d月%02d日%02d:%02d:%02d\
							  \r\n车辆类型总数: %d  车辆类型统计:%s\r\n车辆排队长度:%d  车头间距:%d  空间占有率:%0.2f  通行状态:%s\r\n车道名称:%s  场景编号:%d  \r\n\r\n",
							  "LaneNo: %d  Flow: %d  HoldRate: %0.2f  Speed: %0.2f  Distance: %0.2f\r\nStartTime: %d-%02d-%02d-%02d:%02d:%02d - EndTime: %d-%02d-%02d-%02d:%02d:%02d\
							  \r\nCarTypeTotal: %d  rCarType: %s\r\nCarQueueLength: %d  HeadDistance: %d  RoomRate: %0.2f  RunState: %s\r\nRoadName: %s  SceneID: %d  \r\n\r\n"),
				strctTrafficFlow.iLaneID, strctTrafficFlow.iFlow, float(strctTrafficFlow.iHoldRate)/100, float(strctTrafficFlow.iSpeed)/100, float(strctTrafficFlow.iDistance)/100,
				strctTrafficFlow.stStartTime.iYear+2000, strctTrafficFlow.stStartTime.iMonth, strctTrafficFlow.stStartTime.iDay,
				strctTrafficFlow.stStartTime.iHour, strctTrafficFlow.stStartTime.iMinute, strctTrafficFlow.stStartTime.iSecond,
				strctTrafficFlow.stStopTime.iYear+2000, strctTrafficFlow.stStopTime.iMonth, strctTrafficFlow.stStopTime.iDay,
				strctTrafficFlow.stStopTime.iHour, strctTrafficFlow.stStopTime.iMinute, strctTrafficFlow.stStopTime.iSecond,
				strctTrafficFlow.iCarTypeTotal, cstrCarType, strctTrafficFlow.iCarQueueLength, strctTrafficFlow.iHeadDistance,
				float(strctTrafficFlow.iRoomRate)/100, GetTextByLan(pcRunStateCN[strctTrafficFlow.iRunState], pcRunStateEN[strctTrafficFlow.iRunState]), strctTrafficFlow.pcRoadName, strctTrafficFlow.iSceneID+1);

			CString cstrTotal;
			m_edtShowPart.GetWindowText(cstrTotal);
			if(cstrTotal.GetLength() < 10000)
			{
				cstrTotal += cstrOutPut;	
			}
			else
			{
				cstrTotal = cstrOutPut;
			}
			m_edtShowPart.SetWindowText(cstrTotal);
		}
		break;
	default:
		break;
	}
}
void Cls_ItsTrafficStatistics::OnBnClickedBtnCleanUp()
{
	m_edtShowPart.SetWindowText("");
}

BOOL Cls_ItsTrafficStatistics::UI_UpdateStatis()
{
	if(m_iLogonID < 0)
		return FALSE;
	
	ITSTrafficFlow tSetTrafficFlow = {0};
	tSetTrafficFlow.iLaneID = m_cboRoadID.GetCurSel();
	int iRet = NetClient_GetITSExtraInfo(m_iLogonID, ITS_EXTRAINFO_CMD_TRAFFIC_FLOW, m_iChannelNo, &tSetTrafficFlow, sizeof(ITSTrafficFlow));
	if(iRet == 0)
	{
		m_chkUseTrafficStatis.SetCheck(tSetTrafficFlow.iEnable);
		SetDlgItemInt(IDC_EDT_INPUT_PART, tSetTrafficFlow.iTimeInterval);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSExtraInfo[ITS_EXTRAINFO_CMD_TRAFFIC_FLOW](%d, %d)",m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetITSExtraInfo[ITS_EXTRAINFO_CMD_TRAFFIC_FLOW](%d, %d),error(%d)",m_iLogonID, m_iChannelNo,GetLastError());
		return FALSE;
	}

	return TRUE;
}

void Cls_ItsTrafficStatistics::OnCbnSelchangeCboRoadid()
{
	UI_UpdateStatis();
}

void Cls_ItsTrafficStatistics::OnBnClickedButtonFlowQuery()
{
	// TODO: Add control notification handler code here
	TrafficFlowQuery tInfo = {0};
	tInfo.iChn = m_iChannelNo < 0 ? 0:m_iChannelNo;
	tInfo.iPageNo = GetDlgItemInt(IDC_EDIT_FLOW_PAGENO)-1;
	tInfo.iPageNo = tInfo.iPageNo < 0 ? 0:tInfo.iPageNo;
	tInfo.iPageSize = GetDlgItemInt(IDC_EDIT_FLOW_PAGESIZE);
	CTime tempTime; 
	m_DtFlowStartTime.GetTime(tempTime);
	tInfo.tStartTime.iYear = tempTime.GetYear();
	tInfo.tStartTime.iMonth = tempTime.GetMonth();
	tInfo.tStartTime.iDay = tempTime.GetDay();
	tInfo.tStartTime.iHour = tempTime.GetHour();
	tInfo.tStartTime.iMinute = tempTime.GetMinute();
	tInfo.tStartTime.iSecond = tempTime.GetSecond();
	m_DtFlowEndTime.GetTime(tempTime);
	tInfo.tEndTime.iYear = tempTime.GetYear();
	tInfo.tEndTime.iMonth = tempTime.GetMonth();
	tInfo.tEndTime.iDay = tempTime.GetDay();
	tInfo.tEndTime.iHour = tempTime.GetHour();
	tInfo.tEndTime.iMinute = tempTime.GetMinute();
	tInfo.tEndTime.iSecond = tempTime.GetSecond();
	TrafficFlowInfo tParam[FACE_MAX_PAGE_COUNT] = {0};
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_TRAFFICFLOW_QUERY, tInfo.iChn, &tInfo, sizeof(TrafficFlowQuery), tParam, sizeof(TrafficFlowInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig[CMD_TRAFFICFLOW_QUERY](%d, %d)failed! iRet(%d).",m_iLogonID, m_iChannelNo, iRet);
		m_LisFlowResult.DeleteAllItems();
		SetDlgItemInt(IDC_STATIC_TOTALSTATIS_VALUE, 0);
		SetDlgItemInt(IDC_STATIC_TOTAL_PAGE_VALUE, 0);
		MessageBox(GetTextByLan(_T("查询失败！"),_T("Query Failed!")));
	}
	else
	{	m_LisFlowResult.DeleteAllItems();
		SetDlgItemInt(IDC_STATIC_TOTALSTATIS_VALUE, tParam[0].iTotalNum);
		int iMod = tParam[0].iTotalNum%tInfo.iPageSize;
		int iTotalPage = tParam[0].iTotalNum/tInfo.iPageSize;
		SetDlgItemInt(IDC_STATIC_TOTAL_PAGE_VALUE, iMod==0 ? iTotalPage : iTotalPage+1);
		for (int i = 0; i < tInfo.iPageSize && i < FACE_MAX_PAGE_COUNT; i++)
		{
			if (i > 0 && tParam[i].iIndex == 0)
			{
				//At this point, the previous one is considered to be the last one
				break;
			}
			m_LisFlowResult.InsertItem(i, "");
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_INDEX, IntToCString(i + 1));
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_PAGENO, IntToCString(tParam[i].iPageNo + 1));
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_LANEID, IntToCString(tParam[i].iLaneNo));
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_ROADNAME, tParam[i].pcRoadName);
			CString cstrTimeRange;
			cstrTimeRange.Format("%d-%02d-%02d-%02d:%02d:%02d-%d-%02d-%02d-%02d:%02d:%02d",tParam[i].tStartTime.iYear+2000, tParam[i].tStartTime.iMonth, tParam[i].tStartTime.iDay,
				tParam[i].tStartTime.iHour, tParam[i].tStartTime.iMinute, tParam[i].tStartTime.iSecond, tParam[i].tEndTime.iYear+2000, tParam[i].tEndTime.iMonth,
				tParam[i].tEndTime.iDay, tParam[i].tEndTime.iHour, tParam[i].tEndTime.iMinute, tParam[i].tEndTime.iSecond);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_TIMERANGE, cstrTimeRange);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_FLOW, IntToCString(tParam[i].iFlow));
			CString cstrHoldRate;
			cstrHoldRate.Format("%0.2f", float(tParam[i].iHoldRate)/100);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_HOLDRATE, cstrHoldRate);
			CString cstrSpeed;
			cstrSpeed.Format("%0.2f", float(tParam[i].iSpeed)/100);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_SPEED, cstrSpeed);
			CString cstrDistance;
			cstrDistance.Format("%0.2f", float(tParam[i].iDistance)/100);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_DISTANCE, cstrDistance);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_CARTYPETOTAL, IntToCString(tParam[i].iCarTypeTotal));
			CString cstrCarType;
			for (int j = 0; j < tParam[i].iCarTypeTotal && j < LEN_32; j++)
			{
				CString cstrCarInfo;
				cstrCarInfo.Format("%s:%d", GetValueByID(tParam[i].tCarTypeNum[j].iCarType), tParam[i].tCarTypeNum[j].iCarNum);

				cstrCarType += cstrCarInfo;
				CString SplitSection = j < tParam[i].iCarTypeTotal-1 ? ", ":".";
				cstrCarType += SplitSection;
			}
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_CARTYPESTR, cstrCarType);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_CARQUEUELEN, IntToCString(tParam[i].iCarQueueLength));
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_HEADDISTANCE, IntToCString(tParam[i].iHeadDistance));
			CString cstrRoomRate;
			cstrRoomRate.Format("%0.2f", float(tParam[i].iRoomRate)/100);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_ROOMRATE, cstrRoomRate);
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_RUNSTATE, GetTextByLan(pcRunStateCN[tParam[i].iRunState], pcRunStateEN[tParam[i].iRunState]));
			m_LisFlowResult.SetItemText(i, n_LIST_TRAFFICFLOW_SCENEID, IntToCString(tParam[i].iSceneID+1));
		}
	}
}

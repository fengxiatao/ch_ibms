// .\Config\CLS_ItsRadarInfo.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_ItsRadarInfo.h"

#define RADAR_LANE_NUM_MAX		8	//Number of radar lanes
#define TIMER_UPDATE_RADAR_STATUS 1	//Update radar status Timer

// CLS_ItsRadarInfo dialog

IMPLEMENT_DYNAMIC(CLS_ItsRadarInfo, CDialog)

CLS_ItsRadarInfo::CLS_ItsRadarInfo(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ItsRadarInfo::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_cstrRadarVersion = "";
	m_cstrRadarStatus = "";
}

CLS_ItsRadarInfo::~CLS_ItsRadarInfo()
{
}

void CLS_ItsRadarInfo::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_SLIDER_RADAR_ROADWIDTH, m_sldRoadWidth);
	DDX_Control(pDX, IDC_SLIDER_RADAR_MEASUREMAX, m_sldMeasureMax);
	DDX_Control(pDX, IDC_SLIDER_RADAR_MEASUREMIN, m_sldMeasureMin);
	DDX_Control(pDX, IDC_SLIDER_RADAR_CROSS_SECTION, m_sldCrossSection);
	DDX_Control(pDX, IDC_SLIDER_RADAR_HEIGHT, m_sldRadarHeight);
	DDX_Control(pDX, IDC_SLIDER_RADAR_ANGLE_DELTA, m_sldAngleDelta);
	DDX_Control(pDX, IDC_SLIDER_RADAR_COORD_DELTA, m_sldCoordDelta);
	DDX_Control(pDX, IDC_SLIDER_RADAR_JAM_STARTLINE, m_sldStartLine);
	DDX_Control(pDX, IDC_SLIDER_RADAR_JAM_STOPTLINE, m_sldStopLine);
	DDX_Control(pDX, IDC_SLIDER_RADAR_QUEUE_LENGTH, m_sldQueueLength);
	DDX_Control(pDX, IDC_SLIDER_RADAR_JAM_CARNUM, m_sldCarNumber);
	DDX_Control(pDX, IDC_SLIDER_RADAR_BELT_DRIVEWAY, m_sldBeltDriveway);
	DDX_Control(pDX, IDC_SLIDER_RADAR_LEFT_LANE_NUM, m_sldLeftLaneNum);
	DDX_Control(pDX, IDC_COMBO__RADAR_ROADNUM, m_cboRoadNum);
	DDX_Control(pDX, IDC_COMBO_RADAR_EVENT_TYPE, m_cboEventType);
	DDX_Control(pDX, IDC_EDIT_RADAR_VERSION, m_edtRadarVersion);
	DDX_Control(pDX, IDC_EDIT_RADAR_STATUS, m_edtRadarStatus);
	DDX_Control(pDX, IDC_CBO_RADAR_ID, m_cboRadarID);
	DDX_Control(pDX, IDC_EDIT_RADAR_ID_VALUE, m_cboRadarIDValue);
	DDX_Control(pDX, IDC_SLIDER__RADAR_CROSS, m_sldCross);
	DDX_Control(pDX, IDC_SLIDER_RADAR_ROAD, m_sldRoad);
	DDX_Control(pDX, IDC_SLIDER_RADAR_DISTANCE, m_sldDistace);
}


BEGIN_MESSAGE_MAP(CLS_ItsRadarInfo, CLS_BasePage)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_RADAR_DEVICE_SET, &CLS_ItsRadarInfo::OnBnClickedButtonRadarDeviceSet)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_EVENT_SET, &CLS_ItsRadarInfo::OnBnClickedButtonRadarEventSet)
	ON_CBN_SELCHANGE(IDC_COMBO_RADAR_EVENT_TYPE, &CLS_ItsRadarInfo::OnCbnSelchangeComboRadarEventType)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_ROADWIDTH, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarRoadwidth)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_MEASUREMAX, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarMeasuremax)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_MEASUREMIN, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarMeasuremin)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_CROSS_SECTION, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarCrossSection)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_BELT_DRIVEWAY, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarBeltDriveway)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_LEFT_LANE_NUM, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarLeftLaneNum)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_HEIGHT, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarHeight)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_ANGLE_DELTA, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarAngleDelta)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_COORD_DELTA, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarCoordDelta)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_JAM_STARTLINE, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarJamStartline)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_JAM_STOPTLINE, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarJamStoptline)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_QUEUE_LENGTH, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarQueueLength)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_JAM_CARNUM, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarJamCarnum)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_ID_SET, &CLS_ItsRadarInfo::OnBnClickedButtonRadarIdSet)
	ON_CBN_SELCHANGE(IDC_CBO_RADAR_ID, &CLS_ItsRadarInfo::OnCbnSelchangeCboRadarId)
	ON_WM_TIMER()
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER__RADAR_CROSS, &CLS_ItsRadarInfo::OnNMCustomdrawSlider)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_ROAD, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarRoad)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RADAR_DISTANCE, &CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarDistance)
END_MESSAGE_MAP()


// CLS_ItsRadarInfo message handler

BOOL CLS_ItsRadarInfo::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}
void CLS_ItsRadarInfo::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
		SetTimer(TIMER_UPDATE_RADAR_STATUS, 5000, NULL);
	}
	else
	{
		KillTimer(TIMER_UPDATE_RADAR_STATUS);
	}
}

void CLS_ItsRadarInfo::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo)
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

	UpdatePageUI();
}

void CLS_ItsRadarInfo::OnLanguageChanged(int _iLanguage)
{	
	UpdateUIText();
	UpdatePageUI();
}

void CLS_ItsRadarInfo::UpdateUIText()
{
	//SetDlgItemText(IDC_CHECK_HUMAN_SNAP, GetTextByLan("人形抓拍使能", "Human snap enable"));

	m_cboRoadNum.ResetContent();
	for (int i=0; i<RADAR_LANE_NUM_MAX; i++)
	{
		CString strNo;
		strNo.Format("%d",i+1);
		m_cboRoadNum.AddString(strNo);
	}
	m_cboRoadNum.SetCurSel(0);

	m_cboRadarID.ResetContent();
	for (int i=0; i<RADAR_PARAMID_MAXNUM; i++)
	{
		CString strNo;
		strNo.Format("%d",i);
		m_cboRadarID.AddString(strNo);
	}
	m_cboRadarID.SetCurSel(0);

	m_cboEventType.ResetContent();
	m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan("停车事件", "Parking events")), RADAR_EVENT_TYPE_PARK);
	m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan("拥堵事件", "Congestion events")), RADAR_EVENT_TYPE_CONGESTION);
	m_cboEventType.SetCurSel(0);

	m_sldRoadWidth.SetRange(100, 400);
	m_sldRoadWidth.SetPos(100);
	SetDlgItemInt(IDC_STATIC_RADAR_ROADWIDTH_VALUE, m_sldRoadWidth.GetPos());

	m_sldMeasureMax.SetRange(100, 28000);
	m_sldMeasureMax.SetPos(100);
	SetDlgItemInt(IDC_STATIC_RADAR_MEASUREMAX_VALUE, m_sldMeasureMax.GetPos());

	m_sldMeasureMin.SetRange(100, 25600);
	m_sldMeasureMin.SetPos(100);
	SetDlgItemInt(IDC_STATIC_RADAR_MEASUREMIN_VALUE, m_sldMeasureMin.GetPos());

	m_sldCrossSection.SetRange(0, 250);
	m_sldCrossSection.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_CROSS_SECTION_VALUE, m_sldCrossSection.GetPos());

	m_sldRadarHeight.SetRange(100, 1000);
	m_sldRadarHeight.SetPos(100);
	SetDlgItemInt(IDC_STATIC_RADAR_HEIGHT_VALUE, m_sldRadarHeight.GetPos());

	m_sldAngleDelta.SetRange(0, 20000);
	m_sldAngleDelta.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_ANGLE_DELTA_VALUE, m_sldAngleDelta.GetPos());

	m_sldCoordDelta.SetRange(0, 2000);
	m_sldCoordDelta.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_COORD_DELTA_VALUE, m_sldCoordDelta.GetPos());

	m_sldStartLine.SetRange(0, 250);
	m_sldStartLine.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_JAM_STARTLINE_VALUE, m_sldStartLine.GetPos());

	m_sldStopLine.SetRange(0, 250);
	m_sldStopLine.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_JAM_STOPTLINE_VALUE, m_sldStopLine.GetPos());

	m_sldQueueLength.SetRange(0, 8);
	m_sldQueueLength.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_QUEUE_LENGTH_VALUE, m_sldQueueLength.GetPos());

	m_sldCarNumber.SetRange(0, 128);
	m_sldCarNumber.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_JAM_CARNUM_VALUE, m_sldCarNumber.GetPos());

	m_sldBeltDriveway.SetRange(0, 12);
	m_sldBeltDriveway.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_BELT_DRIVEWAY_VALUE, m_sldBeltDriveway.GetPos());

	m_sldLeftLaneNum.SetRange(0, 8);
	m_sldLeftLaneNum.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_LEFT_LANE_NUM_VALUE, m_sldLeftLaneNum.GetPos());

	m_sldCross.SetRange(0, 20);
	m_sldCross.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_CROSS_VALUE, m_sldCross.GetPos());

	m_sldRoad.SetRange(0, 20);
	m_sldRoad.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_ROAD_VALUE, m_sldRoad.GetPos());

	m_sldDistace.SetRange(0, 250);
	m_sldDistace.SetPos(0);
	SetDlgItemInt(IDC_STATIC_RADAR_DISTANCE_VALUE, m_sldDistace.GetPos());

}

void CLS_ItsRadarInfo::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_ItsRadarInfo::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}
	
	UpdatePageUI_RadarStatus();
	UpdatePageUI_RadarDevice();
	UpdatePageUI_RadarEvent();
	UpdatePageUI_RadarAdvancedPara();

	return;
} 

int CLS_ItsRadarInfo::UpdatePageUI_RadarEvent()
{
	int iReturned = 0;
	RadarEventPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;	
	tInfo.iEventType = 	m_cboEventType.GetItemData(m_cboEventType.GetCurSel());

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RADAR_EVENT_PARA, m_iChannelNo, &tInfo, sizeof(tInfo), &iReturned);
	if(iRet >= 0)
	{
		SetDlgItemInt(IDC_EDIT_RAIN_TIME_INTERVAL, tInfo.iTime);
	}

	return iRet;
}

int CLS_ItsRadarInfo::UpdatePageUI_RadarDevice()
{
	int iReturned = 0;
	RadarDevicePara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iRadarType = 1;

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RADAR_DEVICE_PARA, m_iChannelNo, &tInfo, sizeof(tInfo), &iReturned);
	if(iRet >= 0)
	{
		m_cboRoadNum.SetCurSel(tInfo.iRoadNum-1);
		m_sldRoadWidth.SetPos(tInfo.iRadarRoadWidth);
		m_sldMeasureMax.SetPos(tInfo.iRadarMeasureMax);
		m_sldMeasureMin.SetPos(tInfo.iRadarMeasureMin);
		m_sldCrossSection.SetPos(tInfo.tCrossSection[0].iPos);
		m_sldRadarHeight.SetPos(tInfo.iRadarHeight);
		m_sldAngleDelta.SetPos(tInfo.iRadarAngleDelta);
		m_sldCoordDelta.SetPos(tInfo.iRadarCoordDelta);
		m_sldStartLine.SetPos(tInfo.iTrafficJamStartLine);
		m_sldStopLine.SetPos(tInfo.iTrafficJamTerminationLine);
		m_sldQueueLength.SetPos(tInfo.iQueueLengthThreshold);
		m_sldCarNumber.SetPos(tInfo.iTrafficJamCarNumber);
		m_sldBeltDriveway.SetPos(tInfo.iLsolationBeltDriveway);
		m_sldLeftLaneNum.SetPos(tInfo.iLeftToTheLane);
		m_sldCross.SetPos(tInfo.iNonmotorValue);
		m_sldRoad.SetPos(tInfo.iMotorValue);
		m_sldDistace.SetPos(tInfo.iShieldDistance);
	}

	return iRet;
}

int CLS_ItsRadarInfo::UpdatePageUI_RadarStatus()
{
	RadarStatusInfo tInfo = {0};
	tInfo.iSize = sizeof(RadarStatusInfo);
	tInfo.iType = 1;

	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_RADAR_INFO, m_iChannelNo, &tInfo, sizeof(tInfo));

	if (RET_FAILED == iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SendCommand[COMMAND_ID_RADAR_INFO] (%d, %d),error(%d)",m_iLogonID, m_iChannelNo, GetLastError());
	}

	return iRet;
}

void CLS_ItsRadarInfo::OnBnClickedButtonRadarDeviceSet()
{
	RadarDevicePara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iRadarType = 1;
	tInfo.iRoadNum = m_cboRoadNum.GetCurSel()+1;
	tInfo.iRadarRoadWidth = m_sldRoadWidth.GetPos();
	for (int i=0; i<tInfo.iRoadNum && i<RADAR_LANE_NUM_MAX ;i++)
	{
		tInfo.tRoadDir[i].iId = i;
		tInfo.tRoadDir[i].iDir = 0;
	}
	tInfo.iRadarMeasureMax = m_sldMeasureMax.GetPos();
	tInfo.iRadarMeasureMin = m_sldMeasureMin.GetPos();
	tInfo.iCrossSectionNum = 1; //Default is 1
	tInfo.tCrossSection[0].iId = 0;
	tInfo.tCrossSection[0].iPos = m_sldCrossSection.GetPos();

	tInfo.iRadarHeight = m_sldRadarHeight.GetPos();
	tInfo.iRadarAngleDelta = m_sldAngleDelta.GetPos();
	tInfo.iRadarCoordDelta = m_sldCoordDelta.GetPos();
	tInfo.iTrafficJamStartLine = m_sldStartLine.GetPos();
	tInfo.iTrafficJamTerminationLine = m_sldStopLine.GetPos();
	tInfo.iQueueLengthThreshold = m_sldQueueLength.GetPos();
	tInfo.iTrafficJamCarNumber = m_sldCarNumber.GetPos();
	tInfo.iLsolationBeltDriveway = m_sldBeltDriveway.GetPos();
	tInfo.iLeftToTheLane = m_sldLeftLaneNum.GetPos();
	tInfo.iNonmotorValue = m_sldCross.GetPos();
	tInfo.iMotorValue = m_sldRoad.GetPos();
	tInfo.iShieldDistance = m_sldDistace.GetPos();

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_RADAR_DEVICE_PARA, m_iChannelNo, &tInfo, sizeof(tInfo));
	if(iRet >= 0)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_RADAR_DEVICE_PARA] (%d, %d)",m_iLogonID, m_iChannelNo);
	}

	return;
}

void CLS_ItsRadarInfo::OnBnClickedButtonRadarEventSet()
{
	RadarEventPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;	
	tInfo.iEventType = 	m_cboEventType.GetItemData(m_cboEventType.GetCurSel());
	tInfo.iTime = GetDlgItemInt(IDC_EDIT_RAIN_TIME_INTERVAL);

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_RADAR_EVENT_PARA, m_iChannelNo, &tInfo, sizeof(tInfo));
	if(iRet >= 0)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_RADAR_EVENT_PARA] (%d, %d)",m_iLogonID, m_iChannelNo);
	}

	return;

}

void CLS_ItsRadarInfo::OnCbnSelchangeComboRadarEventType()
{
	UpdatePageUI_RadarEvent();

	int iEventType = m_cboEventType.GetItemData(m_cboEventType.GetCurSel());
	
	if (RADAR_EVENT_TYPE_PARK == iEventType)
	{		
		SetDlgItemText(IDC_STATIC_RADAR_TIP, GetTextByLan("时间范围：1-43200s", "Time range:1-43200s"));
	}
	else if (RADAR_EVENT_TYPE_CONGESTION == iEventType)
	{
		SetDlgItemText(IDC_STATIC_RADAR_TIP, GetTextByLan("时间范围：1-3600s", "Time range:1-3600s"));
	}
	else
	{
		SetDlgItemText(IDC_STATIC_RADAR_TIP, "");
	}
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarRoadwidth(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_ROADWIDTH_VALUE, m_sldRoadWidth.GetPos());
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarMeasuremax(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_MEASUREMAX_VALUE, m_sldMeasureMax.GetPos());
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarMeasuremin(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_MEASUREMIN_VALUE, m_sldMeasureMin.GetPos());
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarCrossSection(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_CROSS_SECTION_VALUE, m_sldCrossSection.GetPos()*100);
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarBeltDriveway(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_BELT_DRIVEWAY_VALUE, m_sldBeltDriveway.GetPos());
	*pResult = 0;
}


void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarLeftLaneNum(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_LEFT_LANE_NUM_VALUE, m_sldLeftLaneNum.GetPos());
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarHeight(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_HEIGHT_VALUE, m_sldRadarHeight.GetPos());
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarAngleDelta(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_ANGLE_DELTA_VALUE, m_sldAngleDelta.GetPos()-10000);
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarCoordDelta(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_COORD_DELTA_VALUE, m_sldCoordDelta.GetPos()-1000);
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarJamStartline(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_JAM_STARTLINE_VALUE, m_sldStartLine.GetPos()*100);
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarJamStoptline(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_JAM_STOPTLINE_VALUE, m_sldStopLine.GetPos()*100);
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarQueueLength(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_QUEUE_LENGTH_VALUE, m_sldQueueLength.GetPos());
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarJamCarnum(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_JAM_CARNUM_VALUE, m_sldCarNumber.GetPos());
	*pResult = 0;
}


void CLS_ItsRadarInfo::OnMainNotify( int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser )
{
	CString cstrOutPut;
	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_RADAR_STATUSINFO:
		{
			RadarStatusInfo tInfo = {0};
			RadarStatusInfo* ptInfo = (RadarStatusInfo*)_iLParam;
			if (NULL != ptInfo)
			{
				int iCpySize = min(ptInfo->iSize, sizeof(RadarStatusInfo));
				memcpy(&tInfo, ptInfo, iCpySize);
			}

			m_cstrRadarVersion.Format("%s", tInfo.cRadarVersion);
 			m_cstrRadarStatus = GetTextByLan("离线","Offline");
 			if (1 == tInfo.iRadarStatus)
 			{
 				m_cstrRadarStatus = GetTextByLan("在线","Online");
 			}
		}
		break;
	default:
		break;
	}
}


void CLS_ItsRadarInfo::OnBnClickedButtonRadarIdSet()
{
	RadarAdvancedPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;	
	tInfo.iParamID = 	m_cboRadarID.GetCurSel();

	CString cstrValue;
	GetDlgItemText(IDC_EDIT_RADAR_ID_VALUE, cstrValue);
	strncpy_s(tInfo.cValue, (LPSTR)(LPCTSTR)cstrValue, min(LEN_64-1, cstrValue.GetLength()));

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_RADAR_ADVANCED_PARA, m_iChannelNo, &tInfo, sizeof(tInfo));
	if(iRet >= 0)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_RADAR_ADVANCED_PARA] (%d, %d)",m_iLogonID, m_iChannelNo);
	}

	return;
}

int CLS_ItsRadarInfo::UpdatePageUI_RadarAdvancedPara()
{
	int iReturned = 0;
	RadarAdvancedPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;	
	tInfo.iParamID = m_cboRadarID.GetCurSel();

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RADAR_ADVANCED_PARA, m_iChannelNo, &tInfo, sizeof(tInfo), &iReturned);
	if(RET_SUCCESS == iRet)
	{
		SetDlgItemText(IDC_EDIT_RADAR_ID_VALUE, tInfo.cValue);
	}

	return iRet;
}
void CLS_ItsRadarInfo::OnCbnSelchangeCboRadarId()
{
	UpdatePageUI_RadarAdvancedPara();
}

void CLS_ItsRadarInfo::OnTimer(UINT_PTR nIDEvent)
{
	switch(nIDEvent)
	{
	case TIMER_UPDATE_RADAR_STATUS:
 			m_edtRadarVersion.SetWindowText(m_cstrRadarVersion);
 			m_edtRadarStatus.SetWindowText(m_cstrRadarStatus);
		break;
	}
	CLS_BasePage::OnTimer(nIDEvent);
}

void CLS_ItsRadarInfo::OnNMCustomdrawSlider(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_CROSS_VALUE, m_sldCross.GetPos()-10);
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarRoad(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_ROAD_VALUE, m_sldRoad.GetPos()-10);
	*pResult = 0;
}

void CLS_ItsRadarInfo::OnNMCustomdrawSliderRadarDistance(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RADAR_DISTANCE_VALUE, m_sldDistace.GetPos()*100);
	*pResult = 0;
}

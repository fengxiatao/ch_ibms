// CLS_GaugeConfig.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_GaugeConfig.h"
#define MAX_GAUGE_CALIB_NUM				20

// CLS_GaugeConfig dialog

IMPLEMENT_DYNAMIC(CLS_GaugeConfig, CDialog)

CLS_GaugeConfig::CLS_GaugeConfig(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_GaugeConfig::IDD, pParent)
{
	memset(&m_tPoint, 0, sizeof(m_tPoint));
	//UpdateData(FALSE); // Variables -> Controls
}

CLS_GaugeConfig::~CLS_GaugeConfig()
{
}

BOOL CLS_GaugeConfig::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	for(int i = 0; i < MAX_SCENE_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboSceneID.AddString(str);
	}
	for(int i = 0; i < MAX_RULE_NUM_EX; i++) {
		CString str;
		str.Format("%d", i);
		m_cboRuleID.AddString(str);
	}

	for(int i = 0; i <= MAX_GAUGEINFO_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboReferPointNum.AddString(str);
	}

	for(int i = 0; i < MAX_GAUGE_CALIB_NUM; i++) {
		CString str;
		str.Format("%d", i + 1);
		m_cboGaugeCalibNum.AddString(str);
	}
	SetValue();
	UI_UpdateUIText();
	OnBnClickedButtonGaugeGet();
	return TRUE;
}

void CLS_GaugeConfig::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	OnBnClickedButtonGaugeGet();
}

void CLS_GaugeConfig::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_SCENEID, GetTextByLan(_T("场景编号"), _T("Scene ID")));
	SetDlgItemText(IDC_STATIC_RULE_ID, GetTextByLan(_T("规则ID"), _T("Rule ID")));
	SetDlgItemText(IDC_STATIC_GAUGE_NO, GetTextByLan(_T("水尺编号"), _T("Gauge No")));
	SetDlgItemText(IDC_STATIC_ALTITUDE, GetTextByLan(_T("水尺高程"), _T("Gauge Altutide")));
	SetDlgItemText(IDC_STATIC_GAUGE_ID, GetTextByLan(_T("水尺序号"), _T("Uauge ID")));

	SetDlgItemText(IDC_STATIC_GAUGE_TYPE, GetTextByLan(_T("水尺类型"), _T("Gauge Type")));
	SetDlgItemText(IDC_STATIC_REFERPOINT_NUM, GetTextByLan(_T("标定点个数"), _T("Refer Point uum")));
	SetDlgItemText(IDC_STATIC_GAUGE_VALUE, GetTextByLan(_T("水尺读数"), _T("Uauge Value")));
	SetDlgItemText(IDC_STATIC_GAUGE_LENGTH, GetTextByLan(_T("水尺长度"), _T("Gauge Length")));
	SetDlgItemText(IDC_STATIC_BLC_ENABLE, GetTextByLan(_T("背光补偿使能"), _T("Backlight Compensate Enable")));

	SetDlgItemText(IDC_STATIC_GAUGE_RECT, GetTextByLan(_T("对角线坐标"), _T("Diagonal Coord")));
	SetDlgItemText(IDC_STATIC_GAUGE_BASEVALUE, GetTextByLan(_T("基准水尺读数"), _T("Gauge Base Value")));
	SetDlgItemText(IDC_STATIC_GAUGE_CALIBNUM, GetTextByLan(_T("标定预置位个数"), _T("Calib Preset Bits Num")));
	SetDlgItemText(IDC_STATIC_GAUGE_CALIBTYPE, GetTextByLan(_T("水尺标定类型"), _T("Water Gauge Calib Type")));
	SetDlgItemText(IDC_STATIC_UPSWITCH, GetTextByLan(_T("上切读数值"), _T("Up Cut Reading Value")));

	SetDlgItemText(IDC_STATIC_DOWNSWITCH, GetTextByLan(_T("水尺下切读数值"), _T("Cut Down Reading Value")));
	SetDlgItemText(IDC_STATIC_UPSWITCH_ENABLE, GetTextByLan(_T("上切设置使能"), _T("Up Cut Enable")));
	SetDlgItemText(IDC_STATIC_DOWNSWITCH_ENABLE, GetTextByLan(_T("下切设置使能"), _T("Down Cut Enable")));
	SetDlgItemText(IDC_BUTTON_GAUGE_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_GAUGE_GET, GetTextByLan(_T("获取"), _T("Get")));

	SetDlgItemText(IDC_STATIC_LEFT_UP, GetTextByLan(_T("左上"), _T("LeftUp")));
	SetDlgItemText(IDC_STATIC_RIGHT_DOWN, GetTextByLan(_T("右下"), _T("RightDown")));
	SetDlgItemText(IDC_STATIC_REFER_POINT_LIST, GetTextByLan(_T("标定点"), _T("Refer Point")));
	SetDlgItemText(IDC_STATIC_X_POINT, GetTextByLan(_T("X"), _T("X")));
	SetDlgItemText(IDC_STATIC_Y_POINT, GetTextByLan(_T("Y"), _T("Y")));

	SetDlgItemText(IDC_STATIC_SCALE_VALUE, GetTextByLan(_T("刻度值"), _T("Scale value")));
	


	m_cboGaugeType.ResetContent();
	m_cboGaugeType.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboGaugeType.AddString(GetTextByLan(_T("1-宽尺、新尺"), _T("1-wide ruler, new ruler")));
	m_cboGaugeType.AddString(GetTextByLan(_T("2-窄尺、旧尺"), _T("2-narrow ruler, old ruler")));
	m_cboGaugeType.AddString(GetTextByLan(_T("3-特制水尺,支持自标定"), _T("3-special water gauge, supporting self calibration")));
	m_cboGaugeType.AddString(GetTextByLan(_T("4-方块水尺,支持自标定"), _T("4-square water gauge, supporting self calibration")));

	m_cboGaugeType.AddString(GetTextByLan(_T("5-桩式水尺,支持自标定"), _T("5-pile water gauge, supporting self calibration")));
	m_cboGaugeType.AddString(GetTextByLan(_T("6-无水尺"), _T("6-no draft")));
	m_cboGaugeType.AddString(GetTextByLan(_T("7-虚拟水尺"), _T("7-virtual water gauge")));
	m_cboGaugeType.AddString(GetTextByLan(_T("8-桩式水尺(纵向接力)"), _T("8-pile draft (longitudinal relay)")));
	m_cboGaugeType.AddString(GetTextByLan(_T("9-无水尺(纵向接力)"), _T("9-no draft (longitudinal relay)")));

	m_cboGaugeType.AddString(GetTextByLan(_T("10-阶梯水尺(支持不同规格长度)"), _T("10 step water gauge (supporting different specifications and lengths)")));

	m_cboBlcEnable.ResetContent();
	m_cboDownSwitchEnable.ResetContent();
	m_cboUpSwitchEnable.ResetContent();
	m_cboBlcEnable.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboDownSwitchEnable.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboUpSwitchEnable.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));

	m_cboBlcEnable.AddString(GetTextByLan(_T("1-开启"), _T("1-on")));
	m_cboDownSwitchEnable.AddString(GetTextByLan(_T("1-开启"), _T("1-on")));
	m_cboUpSwitchEnable.AddString(GetTextByLan(_T("1-开启"), _T("1-on")));

	m_cboBlcEnable.AddString(GetTextByLan(_T("2-关闭"), _T("2-off")));
	m_cboDownSwitchEnable.AddString(GetTextByLan(_T("2-关闭"), _T("2-off")));
	m_cboUpSwitchEnable.AddString(GetTextByLan(_T("2-关闭"), _T("2-off")));

	m_cboGaugeCalibType.ResetContent();
	m_cboGaugeCalibType.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboGaugeCalibType.AddString(GetTextByLan(_T("1-自标定"), _T("1-self calibration")));
	m_cboGaugeCalibType.AddString(GetTextByLan(_T("2-手动标定"), _T("2-manual calibration")));
	m_cboGaugeCalibType.AddString(GetTextByLan(_T("3-倾角或垂直角度标定"), _T("3-inclination or vertical angle calibration")));
}

void CLS_GaugeConfig::SetValue()
{
	m_cboSceneID.SetCurSel(0);
	m_cboRuleID.SetCurSel(0);
	SetDlgItemInt(IDC_EDIT_GAUGE_ID, 1);
}

void CLS_GaugeConfig::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SCENE_ID, m_cboSceneID);
	DDX_Control(pDX, IDC_COMBO_RULE_ID, m_cboRuleID);
	DDX_Control(pDX, IDC_COMBO_GAUGE_TYPE, m_cboGaugeType);
	DDX_Control(pDX, IDC_COMBO_REFERPOINT_NUM, m_cboReferPointNum);
	DDX_Control(pDX, IDC_COMBO_BLC_ENABLE, m_cboBlcEnable);
	DDX_Control(pDX, IDC_COMBO_GAUGE_CALIB_NUM, m_cboGaugeCalibNum);
	DDX_Control(pDX, IDC_COMBO_GAUGE_CALIB_TYPE, m_cboGaugeCalibType);
	DDX_Control(pDX, IDC_COMBO_UPSWITCH_ENABLE, m_cboUpSwitchEnable);
	DDX_Control(pDX, IDC_COMBO_DOWNSWITCH_ENABLE, m_cboDownSwitchEnable);

	DDX_Text(pDX, IDC_EDIT1, m_tPoint[0].iPointX);
	DDX_Text(pDX, IDC_EDIT5, m_tPoint[1].iPointX);
	DDX_Text(pDX, IDC_EDIT23, m_tPoint[2].iPointX);
	DDX_Text(pDX, IDC_EDIT25, m_tPoint[3].iPointX);
	DDX_Text(pDX, IDC_EDIT31, m_tPoint[4].iPointX);
	DDX_Text(pDX, IDC_EDIT35, m_tPoint[5].iPointX);
	DDX_Text(pDX, IDC_EDIT39, m_tPoint[6].iPointX);
	DDX_Text(pDX, IDC_EDIT43, m_tPoint[7].iPointX);
	DDX_Text(pDX, IDC_EDIT47, m_tPoint[8].iPointX);
	DDX_Text(pDX, IDC_EDIT51, m_tPoint[9].iPointX);

	DDX_Text(pDX, IDC_EDIT2, m_tPoint[0].iPointY);
	DDX_Text(pDX, IDC_EDIT22, m_tPoint[1].iPointY);
	DDX_Text(pDX, IDC_EDIT24, m_tPoint[2].iPointY);
	DDX_Text(pDX, IDC_EDIT26, m_tPoint[3].iPointY);
	DDX_Text(pDX, IDC_EDIT32, m_tPoint[4].iPointY);
	DDX_Text(pDX, IDC_EDIT36, m_tPoint[5].iPointY);
	DDX_Text(pDX, IDC_EDIT40, m_tPoint[6].iPointY);
	DDX_Text(pDX, IDC_EDIT44, m_tPoint[7].iPointY);
	DDX_Text(pDX, IDC_EDIT48, m_tPoint[8].iPointY);
	DDX_Text(pDX, IDC_EDIT52, m_tPoint[9].iPointY);

	DDX_Text(pDX, IDC_EDIT3, m_tPoint[0].iValue);
	DDX_Text(pDX, IDC_EDIT6, m_tPoint[1].iValue);
	DDX_Text(pDX, IDC_EDIT13, m_tPoint[2].iValue);
	DDX_Text(pDX, IDC_EDIT29, m_tPoint[3].iValue);
	DDX_Text(pDX, IDC_EDIT33, m_tPoint[4].iValue);
	DDX_Text(pDX, IDC_EDIT37, m_tPoint[5].iValue);
	DDX_Text(pDX, IDC_EDIT41, m_tPoint[6].iValue);
	DDX_Text(pDX, IDC_EDIT45, m_tPoint[7].iValue);
	DDX_Text(pDX, IDC_EDIT49, m_tPoint[8].iValue);
	DDX_Text(pDX, IDC_EDIT53, m_tPoint[9].iValue);
}


BEGIN_MESSAGE_MAP(CLS_GaugeConfig, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_GAUGE_SET, &CLS_GaugeConfig::OnBnClickedButtonGaugeSet)
	ON_BN_CLICKED(IDC_BUTTON_GAUGE_GET, &CLS_GaugeConfig::OnBnClickedButtonGaugeGet)
END_MESSAGE_MAP()


// CLS_GaugeConfig message handler

void CLS_GaugeConfig::OnBnClickedButtonGaugeSet()
{
	// TODO: Add control notification handler code here
	UpdateData(TRUE);	// Controls -> Variables

	GaugeInfo tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iRuleID = m_cboRuleID.GetCurSel();
	tInfo.iGaugeNo = GetDlgItemInt(IDC_EDIT_GAUGE_NO);
	tInfo.iGaugeAltitude = GetDlgItemInt(IDC_EDIT_ALTITUDE);
	tInfo.iGaugeID = GetDlgItemInt(IDC_EDIT_GAUGE_ID);
	if(SIZE_MINIMIZED > tInfo.iGaugeID)
	{
		MessageBox(GetTextByLan(_T("0:无效,大于0为水尺编号(最小为1)"), _T("0: invalid, greater than 0 is the draft number (minimum 1)")));
		return;
	}
	tInfo.iGaugeType = m_cboGaugeType.GetCurSel();
	tInfo.iReferNum = m_cboReferPointNum.GetCurSel();
	for(int i = 0; i < tInfo.iReferNum && i < MAX_REFERPOINT_NUM; i++) {
		tInfo.tPoint[i].iPointx = m_tPoint[i].iPointX;
		tInfo.tPoint[i].iPointy = m_tPoint[i].iPointY;
		tInfo.tPoint[i].iGaugeValue = m_tPoint[i].iValue;
	}
	tInfo.iGaugeValue = GetDlgItemInt(IDC_EDIT_GAUGE_VALUE);
	tInfo.iGaugeLength = GetDlgItemInt(IDC_EDIT_GAUGE_LENGTH);
	tInfo.iBlcEnable = m_cboBlcEnable.GetCurSel();
	tInfo.tRect.left = GetDlgItemInt(IDC_EDIT_LEFT_X);
	tInfo.tRect.top = GetDlgItemInt(IDC_EDIT_LEFT_Y);
	tInfo.tRect.right = GetDlgItemInt(IDC_EDIT_RIGHT_X);
	tInfo.tRect.bottom = GetDlgItemInt(IDC_EDIT_RIGHT_Y);
	tInfo.iGaugeBaseValue = GetDlgItemInt(IDC_EDIT_GAUGE_BASE_VALUE);
	tInfo.iGaugeCalibNum = m_cboGaugeCalibNum.GetCurSel() + 1;
	tInfo.iGaugeCalibType = m_cboGaugeCalibType.GetCurSel();
	tInfo.iUpSwitch = GetDlgItemInt(IDC_EDIT_UP_SWITCH);
	tInfo.iDownSwitch = GetDlgItemInt(IDC_EDIT_DOWN_SWITCH);
	tInfo.iUpSwitchEnable = m_cboUpSwitchEnable.GetCurSel();
	tInfo.iDownSwitchEnable = m_cboUpSwitchEnable.GetCurSel();
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_WATERGAUGE_INFO, m_iChannelNO, &tInfo, sizeof(GaugeInfo));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_GaugeConfig::NetClient_SetDevConfig[NET_CLIENT_WATERGAUGE_INFO] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_GaugeConfig::NetClient_SetDevConfig[NET_CLIENT_WATERGAUGE_INFO] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_GaugeConfig::OnBnClickedButtonGaugeGet()
{
	// TODO: Add control notification handler code here
	//NetClient_GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, void *_lpOutBuffer, int _iOutBufferSize, int *_lpBytesReturned);
	//NET_CLIENT_WATERGAUGE_INFO;
	//pCmdChan->GetWaterGaugeInfo(_iChannel, _lpOutBuffer, _iOutBufferSize);
	GaugeInfo tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSize = sizeof(GaugeInfo);
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iGaugeID = GetDlgItemInt(IDC_EDIT_GAUGE_ID);
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_WATERGAUGE_INFO, m_iChannelNO, &tInfo, sizeof(GaugeInfo), NULL);
	if(RET_SUCCESS == iRet)
	{
		m_cboSceneID.SetCurSel(tInfo.iSceneId);
		m_cboRuleID.SetCurSel(tInfo.iRuleID);
		//SetDlgItemInt(IDC_EDIT_GAUGE_NO, tInfo.iGaugeNo);
		
		SetDlgItemInt(IDC_EDIT_ALTITUDE, tInfo.iGaugeAltitude);
		SetDlgItemInt(IDC_EDIT_GAUGE_NO, tInfo.iGaugeNo);
		m_cboGaugeType.SetCurSel(tInfo.iGaugeType);
		m_cboReferPointNum.SetCurSel(tInfo.iReferNum);
		memset(&m_tPoint, 0, sizeof(m_tPoint));
		for(int i = 0; i < tInfo.iReferNum && i < MAX_PAGE_SIZE; i++) {
			m_tPoint[i].iPointX = tInfo.tPoint[i].iPointx;
			m_tPoint[i].iPointY = tInfo.tPoint[i].iPointy;
			m_tPoint[i].iValue = tInfo.tPoint[i].iGaugeValue;
		}
		UpdateData(FALSE);	// Variables -> Controls

		SetDlgItemInt(IDC_EDIT_GAUGE_VALUE, tInfo.iGaugeValue);
		SetDlgItemInt(IDC_EDIT_GAUGE_LENGTH, tInfo.iGaugeLength);
		m_cboBlcEnable.SetCurSel(tInfo.iBlcEnable);
		SetDlgItemInt(IDC_EDIT_LEFT_X, tInfo.tRect.left);
		SetDlgItemInt(IDC_EDIT_LEFT_Y, tInfo.tRect.top);
		SetDlgItemInt(IDC_EDIT_RIGHT_X, tInfo.tRect.right);
		SetDlgItemInt(IDC_EDIT_RIGHT_Y, tInfo.tRect.bottom);
		SetDlgItemInt(IDC_EDIT_GAUGE_BASE_VALUE, tInfo.iGaugeBaseValue);
		m_cboGaugeCalibNum.SetCurSel(tInfo.iGaugeCalibNum - 1);
		m_cboGaugeCalibType.SetCurSel(tInfo.iGaugeCalibType);
		SetDlgItemInt(IDC_EDIT_UP_SWITCH, tInfo.iUpSwitch);
		SetDlgItemInt(IDC_EDIT_DOWN_SWITCH, tInfo.iDownSwitch);
		m_cboUpSwitchEnable.SetCurSel(tInfo.iUpSwitchEnable);
		m_cboDownSwitchEnable.SetCurSel(tInfo.iDownSwitchEnable);
		
		AddLog(LOG_TYPE_SUCC, "","CLS_GaugeConfig::NetClient_GetDevConfig[NET_CLIENT_WATERGAUGE_INFO] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_GaugeConfig::NetClient_GetDevConfig[NET_CLIENT_WATERGAUGE_INFO] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	return;
}

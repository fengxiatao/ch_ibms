// CLS_WaterSpeedRule.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_WaterSpeedRule.h"
#define MAX_SPEED_SENSITY						100
#define MAX_CORR_LEVEL							100
#define MAX_TRACK_FRAME_NUM						20
#define MAX_CRUISE_POINTS						30
#define MAX_POLY_VER_NUM						6
#define MAX_DEFLECT_NUM							90

// CLS_WaterSpeedRule dialog

IMPLEMENT_DYNAMIC(CLS_WaterSpeedRule, CDialog)

CLS_WaterSpeedRule::CLS_WaterSpeedRule(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_WaterSpeedRule::IDD, pParent)
	, m_iDetectAltit(0)
	, m_iPointX1(0)
	, m_iPointY1(0)
	, m_iPointX2(0)
	, m_iPointY2(0)
	, m_iHorizonWidth(0)
	, m_iVerticWidth(0)
	, m_iHypotWidth(0)
	, m_iDirPointX1(0)
	, m_iDirPointY1(0)
	, m_iDirPointX2(0)
	, m_iDirPointY2(0)
	, m_iSpeedRatio(0)
	, m_iMinSpeed(0)
	, m_iMaxSpeed(0)
	, m_iWaterLevelThres(0)
	, m_iTimeoutDura(0)
	, m_iDwellTime(0)
	, m_iDecectStep(0)
	, m_iMiddleWidth(0)
{
	memset(&m_tPolygonPoint, 0, sizeof(m_tPolygonPoint));
}

CLS_WaterSpeedRule::~CLS_WaterSpeedRule()
{
}

BOOL CLS_WaterSpeedRule::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UpdateData(FALSE);

	for(int i = 0; i < MAX_SCENE_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboSceneID.AddString(str);
	}

	for(int i = 0; i < VCA_MAX_RULE_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboRuleID.AddString(str);
	}

	for(int i = 0; i <= MAX_SPEED_SENSITY; i++) {
		CString str;
		str.Format("%d", i);
		m_cboSpeedSensity.AddString(str);
	}

	for(int i = 0; i < MAX_CRUISE_POINTS; i++) {
		CString str;
		str.Format("%d", i + 1);
		m_cboCruisePoints.AddString(str);
	}

	for(int i = 0; i < MAX_POLY_VER_NUM; i++) {
		CString str;
		str.Format("%d", i + 3);
		m_cboPolyVerNum.AddString(str);
	}

	for(int i = 0; i <= MAX_SPEED_SENSITY; i++) {
		CString str;
		str.Format("%d", i);
		m_cboFilterSens.AddString(str);
	}

	for(int i = 2; i <= MAX_TRACK_FRAME_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboTrackFrameNum.AddString(str);
	}

	for(int i = 0; i <= MAX_DEFLECT_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboDeflect.AddString(str);
	}

	UI_UpdateUIText();
	InitCombo();
	OnBnClickedButtonWaterspeedGet();
	return TRUE;
}

void CLS_WaterSpeedRule::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	InitCombo();
	OnBnClickedButtonWaterspeedGet();
}

void CLS_WaterSpeedRule::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_SCENEID, GetTextByLan(_T("场景ID"), _T("Scene ID")));
	SetDlgItemText(IDC_STATIC_RULE_ID, GetTextByLan(_T("规则ID"), _T("Rule ID")));
	SetDlgItemText(IDC_STATIC_EVENT_IS_VALID, GetTextByLan(_T("此事件检测是否有效"), _T("Event Check Valid")));
	SetDlgItemText(IDC_STATIC_RULE_IS_SHOW, GetTextByLan(_T("规则是否显示"), _T("Rule Is Show")));
	SetDlgItemText(IDC_STATIC_AREA_COLOR, GetTextByLan(_T("区域颜色"), _T("Area Color")));

	SetDlgItemText(IDC_STATIC_WATERSPEED_SENSITIVITY, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));
	SetDlgItemText(IDC_STATIC_LINKAGE_VIDEO_RECORD, GetTextByLan(_T("联动录像"), _T("Linkage Video")));
	SetDlgItemText(IDC_STATIC_CRUISE_POINT_ADD_MODE, GetTextByLan(_T("巡航点添加模式"), _T("Cruise Point Add Mode")));
	SetDlgItemText(IDC_STATIC_CRUISE_POINTS, GetTextByLan(_T("巡航点数"), _T("Cruise Points")));
	SetDlgItemText(IDC_STATIC_DWELL_TIME, GetTextByLan(_T("驻留时间"), _T("Dwell Time")));

	SetDlgItemText(IDC_STATIC_POLYGON_VERTICE_NUM, GetTextByLan(_T("多边形区域顶点个数"), _T("")));
	SetDlgItemText(IDC_STATIC_PLOYGON_POINT, GetTextByLan(_T("顶点坐标"), _T("Polygon's Vertices Num")));
	SetDlgItemText(IDC_STATIC_VERTEX_COORDX, GetTextByLan(_T("左上角"), _T("Top Left")));
	SetDlgItemText(IDC_STATIC_VERTEX_COORDY, GetTextByLan(_T("右下角"), _T("Bottom Right")));
	SetDlgItemText(IDC_STATIC_APPLY_SCENE, GetTextByLan(_T("应用场景"), _T("Apply Scene")));

	SetDlgItemText(IDC_STATIC_DECECTION_STEP, GetTextByLan(_T("检测步长"), _T("Decect Step")));
	SetDlgItemText(IDC_STATIC_VIDEO_MIDDLE_WIDTH, GetTextByLan(_T("视频中线距离"), _T("Video MidleLine Distance")));
	SetDlgItemText(IDC_STATIC_DETECT_ALTITUDE, GetTextByLan(_T("初始检测区域高程"), _T("Initial Detection Srea Elevation")));
	SetDlgItemText(IDC_STATIC_POINT_x, GetTextByLan(_T("左上角"), _T("Top Left")));
	SetDlgItemText(IDC_STATIC_POINT_Y, GetTextByLan(_T("右下角"), _T("Bottom Right")));

	SetDlgItemText(IDC_STATIC_POINT_ONE, GetTextByLan(_T("视频中线点1"), _T("Video Center Line Point1")));
	SetDlgItemText(IDC_STATIC_POINT_TWO, GetTextByLan(_T("视频中线点2"), _T("Video Center Line Point1")));
	SetDlgItemText(IDC_STATIC_HORIZON_WIDTH, GetTextByLan(_T("水平直角边长度"), _T("Horizontal Side Length")));
	SetDlgItemText(IDC_STATIC_VERTIC_WIDTH, GetTextByLan(_T("垂直直角边长度"), _T("Perpendicular Edge Length")));
	SetDlgItemText(IDC_STATIC_HYPOT_WIDTH, GetTextByLan(_T("斜边长度"), _T("Bevel Length")));

	SetDlgItemText(IDC_STATIC_ANGEL_INPUT_MODE, GetTextByLan(_T("倾角输入模式"), _T("Inclinat Onput Mode")));
	SetDlgItemText(IDC_STATIC_NIGHT_FOCUS_MODE, GetTextByLan(_T("夜晚聚焦模式"), _T("Night Focus Mode")));
	SetDlgItemText(IDC_STATIC_WATER_SPEED_DIR, GetTextByLan(_T("流速方向设置使能"), _T("Flow Direct Set Enable")));
	SetDlgItemText(IDC_STATIC_POINT_x2, GetTextByLan(_T("左上角"), _T("Top Left")));
	SetDlgItemText(IDC_STATIC_POINT_Y2, GetTextByLan(_T("右下角"), _T("Bottom Right")));

	SetDlgItemText(IDC_STATIC_SPEED_DIR_ONE, GetTextByLan(_T("流速方向点1"), _T("Velocity Direct Point1")));
	SetDlgItemText(IDC_STATIC_SPEED_DIR_TWO, GetTextByLan(_T("流速方向点2"), _T("Velocity Direct Point2")));
	SetDlgItemText(IDC_STATIC_SPEED_RATIO, GetTextByLan(_T("流速系数"), _T("Velocity Coefficient")));
	SetDlgItemText(IDC_STATIC_FILTER_SENSITY, GetTextByLan(_T("过滤灵敏度"), _T("Filter Sensitivity")));
	SetDlgItemText(IDC_STATIC_LINKAREA_TYPE, GetTextByLan(_T("小场景水位联动检测区域类型"), _T("Small Scene Water Level Linkage Detection Area Type")));

	SetDlgItemText(IDC_STATIC_MIN_SPEED, GetTextByLan(_T("最小流速"), _T("Min Flow Rate")));
	SetDlgItemText(IDC_STATIC_MAX_SPEED, GetTextByLan(_T("最大流速"), _T("Max Flow Rate")));
	SetDlgItemText(IDC_STATIC_DISPLAY_TYPE, GetTextByLan(_T("流速显示类型"), _T("Flow Rate Display Type")));
	SetDlgItemText(IDC_STATIC_WATER_LEVEL_THRES, GetTextByLan(_T("水位阈值"), _T("Water Level Threshold")));
	SetDlgItemText(IDC_STATIC_DETECT_MODE, GetTextByLan(_T("流速检测模式"), _T("Flow Rate Detect Mode")));

	SetDlgItemText(IDC_STATIC_TRACK_FRAME_NUM, GetTextByLan(_T("检测跟踪帧数"), _T("Detect Track Frame Num")));
	SetDlgItemText(IDC_STATIC_FIELD_VISION, GetTextByLan(_T("流速检测视野大小"), _T("Flow Velocity Detect Field Size")));
	SetDlgItemText(IDC_STATIC_CORR_LEVEL, GetTextByLan(_T("流速曲线校正级别"), _T("Correction Level Of Velocity Curve")));
	SetDlgItemText(IDC_STATIC_START_DIR, GetTextByLan(_T("流速检测起点方向"), _T("Start Direct Of Flow Velocity Detect")));
	SetDlgItemText(IDC_STATIC_LIGHT_CTRL, GetTextByLan(_T("检测时补光策略"), _T("Fill Light Strategy During Detect")));

	SetDlgItemText(IDC_STATIC_DEFLECT, GetTextByLan(_T("夜晚检测角度"), _T("Night Detect Angle")));
	SetDlgItemText(IDC_STATIC_TIMEROUT_ENABLE, GetTextByLan(_T("流速检测超时清0使能"), _T("Flow Rate Detect Timeout Clear 0 Enable")));
	SetDlgItemText(IDC_STATIC_TIMEROUT_DURA, GetTextByLan(_T("流速检测超时时长"), _T("Flow Rate Detect Timeout Durat")));
	SetDlgItemText(IDC_BUTTON_WATERSPEED_GET, GetTextByLan(_T("获取"), _T("Get")));
	SetDlgItemText(IDC_BUTTON_WATERSPEED_SET, GetTextByLan(_T("设置"), _T("Set")));

	m_cboEventIsValid.ResetContent();
	m_cboEventIsValid.AddString(GetTextByLan(_T("0-无效"), _T("0- invalid")));
	m_cboEventIsValid.AddString(GetTextByLan(_T("1-有效"), _T("1-valid")));

	m_cboRuleIsShow.ResetContent();
	m_cboRuleIsShow.AddString(GetTextByLan(_T("0：不显示"), _T("0: do not display")));
	m_cboRuleIsShow.AddString(GetTextByLan(_T("1：显示"), _T("1: Display")));

	m_cboAeraColor.ResetContent();
	m_cboAeraColor.AddString(GetTextByLan(_T("红色"), _T("Red")));
	m_cboAeraColor.AddString(GetTextByLan(_T("绿色"), _T("Green")));
	m_cboAeraColor.AddString(GetTextByLan(_T("黄色"), _T("Yellow")));
	m_cboAeraColor.AddString(GetTextByLan(_T("蓝色"), _T("Blue")));
	m_cboAeraColor.AddString(GetTextByLan(_T("紫色"), _T("Purple")));
	m_cboAeraColor.AddString(GetTextByLan(_T("青色"), _T("Cyan")));
	m_cboAeraColor.AddString(GetTextByLan(_T("黑色"), _T("Black")));
	m_cboAeraColor.AddString(GetTextByLan(_T("白色"), _T("White")));

	m_cboVideoRecord.ResetContent();
	m_cboVideoRecord.AddString(GetTextByLan(_T("0：不联动录像"), _T("0: no linkage recording")));
	m_cboVideoRecord.AddString(GetTextByLan(_T("1：联动录像	"), _T("1: Linkage video recording")));

	m_cboCruiseAddMode.ResetContent();
	m_cboCruiseAddMode.AddString(GetTextByLan(_T("0-自动"), _T("0-Auto")));
	m_cboCruiseAddMode.AddString(GetTextByLan(_T("1-手动"), _T("1-Manual")));

	m_cboApplyScene.ResetContent();
	m_cboApplyScene.AddString(GetTextByLan(_T("0：大场景"), _T("0: large scene")));
	m_cboApplyScene.AddString(GetTextByLan(_T("1：小场景"), _T("1: small scene")));

	m_cboAngelInputMode.ResetContent();
	m_cboAngelInputMode.AddString(GetTextByLan(_T("0:输入倾斜角"), _T("0: enter the slope angle")));
	m_cboAngelInputMode.AddString(GetTextByLan(_T("1:输入直角边斜边"), _T("1: enter the right angle side slope")));
	
	m_cboNightFocusMode.ResetContent();
	m_cboNightFocusMode.AddString(GetTextByLan(_T("0:关闭"), _T("0: off")));
	m_cboNightFocusMode.AddString(GetTextByLan(_T("1:开启"), _T("1: on")));

	m_cboSpeedDir.ResetContent();
	m_cboSpeedDir.AddString(GetTextByLan(_T("0:不使能"), _T("0: not enabled")));
	m_cboSpeedDir.AddString(GetTextByLan(_T("1:使能"), _T("1: enabled")));

	m_cboLinkAreaType.ResetContent();
	m_cboLinkAreaType.AddString(GetTextByLan(_T("0-不联动"), _T("0-no linkage")));
	m_cboLinkAreaType.AddString(GetTextByLan(_T("1-联动"), _T("1-linkage")));

	m_cboDisplayType.ResetContent();
	m_cboDisplayType.AddString(GetTextByLan(_T("0-瞬时流速"), _T("0-instantaneous flow rate")));
	m_cboDisplayType.AddString(GetTextByLan(_T("1-平均流速"), _T("1-average flow rate")));


	m_cboDetectMode.ResetContent();
	m_cboDetectMode.AddString(GetTextByLan(_T("0-保留"), _T("0- reserved")));
	m_cboDetectMode.AddString(GetTextByLan(_T("1-自动"), _T("1- automatic")));
	m_cboDetectMode.AddString(GetTextByLan(_T("2-低速(有漂浮物模式)"), _T("2- low speed (with floating objects mode)")));
	m_cboDetectMode.AddString(GetTextByLan(_T("3-高速(无漂浮物模式)"), _T("3- high speed (without floating objects mode)")));
	m_cboDetectMode.AddString(GetTextByLan(_T("4-低速多帧(使用OpenCV检测小漂浮物模式)"), _T("4- low speed multi frame (using OpenCV to detect small floating objects mode)")));

	m_cboDetectMode.AddString(GetTextByLan(_T("5-无人机自动模式"), _T("5- UAV automatic mode")));
	m_cboDetectMode.AddString(GetTextByLan(_T("6-无人机小飘浮物模式"), _T("6- UAV small floating objects mode")));


	m_cboFieldVision.ResetContent();
	m_cboFieldVision.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboFieldVision.AddString(GetTextByLan(_T("1-大"), _T("1-large")));
	m_cboFieldVision.AddString(GetTextByLan(_T("2-小"), _T("2-small")));

	m_cboCorrLevel.ResetContent();
	m_cboCorrLevel.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	for(int i = 0; i < MAX_CORR_LEVEL; i++)
	{
		CString str;
		str.Format("%d", i + 1);
		m_cboCorrLevel.AddString(str);
	}

	m_cboStartDir.ResetContent();
	m_cboStartDir.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboStartDir.AddString(GetTextByLan(_T("1-同向(面向起点方向检测)"), _T("1- in the same direction (detection towards the starting point)")));
	m_cboStartDir.AddString(GetTextByLan(_T("2-反向(背向起点方向检测)"), _T("2- reverse direction (detection away from the starting point)")));

	m_cboLightCtrl.ResetContent();
	m_cboLightCtrl.AddString(GetTextByLan(_T("0-关闭"), _T("0-off")));
	m_cboLightCtrl.AddString(GetTextByLan(_T("1-开启"), _T("1-open")));

	m_cboLightCtrl.ResetContent();
	m_cboLightCtrl.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboLightCtrl.AddString(GetTextByLan(_T("1-开启"), _T("1-open")));
	m_cboLightCtrl.AddString(GetTextByLan(_T("2-关闭"), _T("2-off")));

	m_cboTimeoutEnable.ResetContent();
	m_cboTimeoutEnable.AddString(GetTextByLan(_T("0-保留"), _T("0-reserved")));
	m_cboTimeoutEnable.AddString(GetTextByLan(_T("1-开启"), _T("1-open")));
	m_cboTimeoutEnable.AddString(GetTextByLan(_T("2-关闭"), _T("2-off")));

}

void CLS_WaterSpeedRule::InitCombo()
{
	m_cboSceneID.SetCurSel(0);
	m_cboRuleID.SetCurSel(0);
	m_cboEventIsValid.SetCurSel(0);
	m_cboRuleIsShow.SetCurSel(0);
	m_cboAeraColor.SetCurSel(0);

	m_cboSpeedSensity.SetCurSel(0);
	m_cboVideoRecord.SetCurSel(0);
	m_cboCruiseAddMode.SetCurSel(0);
	m_cboCruisePoints.SetCurSel(0);
	m_cboPolyVerNum.SetCurSel(0);

	m_cboApplyScene.SetCurSel(0);
	m_cboAngelInputMode.SetCurSel(0);
	m_cboNightFocusMode.SetCurSel(0);
	m_cboSpeedDir.SetCurSel(0);
	m_cboFilterSens.SetCurSel(0);

	m_cboLinkAreaType.SetCurSel(0);
	m_cboDisplayType.SetCurSel(0);
	m_cboDetectMode.SetCurSel(0);
	m_cboTrackFrameNum.SetCurSel(0);
	m_cboFieldVision.SetCurSel(0);

	m_cboCorrLevel.SetCurSel(0);
	m_cboStartDir.SetCurSel(0);
	m_cboLightCtrl.SetCurSel(0);
	m_cboDeflect.SetCurSel(0);
	m_cboTimeoutEnable.SetCurSel(0);
}

void CLS_WaterSpeedRule::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SCENE_ID, m_cboSceneID);
	DDX_Control(pDX, IDC_COMBO_RULE_ID, m_cboRuleID);
	DDX_Control(pDX, IDC_COMBO_EVENT_IS_VALID, m_cboEventIsValid);
	DDX_Control(pDX, IDC_COMBO_RULE_IS_SHOW, m_cboRuleIsShow);
	DDX_Control(pDX, IDC_COMBO_AREA_COLOR, m_cboAeraColor);
	DDX_Control(pDX, IDC_COMBO_WATERSPEED_SENSITIVITY, m_cboSpeedSensity);
	DDX_Control(pDX, IDC_COMBO_LINKAGE_VIDEO_RECORD, m_cboVideoRecord);
	DDX_Control(pDX, IDC_COMBO_CRUISE_POINT_ADD_MODE, m_cboCruiseAddMode);
	DDX_Control(pDX, IDC_COMBO_CRUISE_POINTS, m_cboCruisePoints);
	DDX_Control(pDX, IDC_COMBO_POLYGON_VERTICE_NUM, m_cboPolyVerNum);
	DDX_Control(pDX, IDC_COMBO_APPLY_SCENE, m_cboApplyScene);
	DDX_Control(pDX, IDC_COMBO_ANGEL_INPUT_MODE, m_cboAngelInputMode);
	DDX_Control(pDX, IDC_COMBO_NIGHT_FOCUS_MODE, m_cboNightFocusMode);
	DDX_Control(pDX, IDC_COMBO_WATER_SPEED_DIR, m_cboSpeedDir);
	DDX_Control(pDX, IDC_COMBO_FILTER_SENSITY, m_cboFilterSens);
	DDX_Control(pDX, IDC_COMBO_LINKAREA_TYPE, m_cboLinkAreaType);
	DDX_Control(pDX, IDC_COMBO_DISPLAY_TYPE, m_cboDisplayType);
	DDX_Control(pDX, IDC_COMBO_DETECT_MODE, m_cboDetectMode);
	DDX_Control(pDX, IDC_COMBO_TRACK_FRAME_NUM, m_cboTrackFrameNum);
	DDX_Control(pDX, IDC_COMBO_FIELD_VISION, m_cboFieldVision);
	DDX_Control(pDX, IDC_COMBO_CORR_LEVEL, m_cboCorrLevel);
	DDX_Control(pDX, IDC_COMBO_START_DIR, m_cboStartDir);
	DDX_Control(pDX, IDC_COMBO_LIGHT_CTRL, m_cboLightCtrl);
	DDX_Control(pDX, IDC_COMBO_DEFLECT, m_cboDeflect);
	DDX_Control(pDX, IDC_COMBO_TIMEROUT_ENABLE, m_cboTimeoutEnable);
	DDX_Text(pDX, IDC_EDIT_DETECT_ALTITUDE, m_iDetectAltit);
	DDV_MinMaxInt(pDX, m_iDetectAltit, -10000000, 10000000);
	DDX_Text(pDX, IDC_EDIT_POINTX_ONE, m_iPointX1);
	DDV_MinMaxInt(pDX, m_iPointX1, 0, 10000);
	DDX_Text(pDX, IDC_EDIT_POINTY_ONE, m_iPointY1);
	DDV_MinMaxInt(pDX, m_iPointY1, 0, 10000);
	DDX_Text(pDX, IDC_EDIT_POINTX_TWO, m_iPointX2);
	DDV_MinMaxInt(pDX, m_iPointX2, 0, 10000);
	DDX_Text(pDX, IDC_EDIT_POINTY_TWO, m_iPointY2);
	DDV_MinMaxInt(pDX, m_iPointY2, 0, 10000);
	DDX_Text(pDX, IDC_EDIT_HORIZON_WIDTH, m_iHorizonWidth);
	DDV_MinMaxInt(pDX, m_iHorizonWidth, 0, 100000);
	DDX_Text(pDX, IDC_EDIT_VERTIC_WIDTH, m_iVerticWidth);
	DDV_MinMaxInt(pDX, m_iVerticWidth, 0, 100000);
	DDX_Text(pDX, IDC_EDIT_HYPOT_WIDTH, m_iHypotWidth);
	DDV_MinMaxInt(pDX, m_iHypotWidth, 0, 141421);
	DDX_Text(pDX, IDC_EDIT_POINTX_ONE2, m_iDirPointX1);
	DDV_MinMaxInt(pDX, m_iDirPointX1, 0, 10000);
	DDX_Text(pDX, IDC_EDIT_POINTY_ONE2, m_iDirPointY1);
	DDX_Text(pDX, IDC_EDIT_POINTX_TWO2, m_iDirPointX2);
	DDV_MinMaxInt(pDX, m_iDirPointX2, 0, 10000);
	DDX_Text(pDX, IDC_EDIT_POINTY_TWO2, m_iDirPointY2);
	DDV_MinMaxInt(pDX, m_iDirPointY2, 0, 10000);
	DDX_Text(pDX, IDC_EDIT_SPEED_RATIO, m_iSpeedRatio);
	DDV_MinMaxInt(pDX, m_iSpeedRatio, 1, 1000);
	DDX_Text(pDX, IDC_EDIT_MIN_SPEED, m_iMinSpeed);
	DDV_MinMaxInt(pDX, m_iMinSpeed, -300000, 300000);
	DDX_Text(pDX, IDC_EDIT_MAX_SPEED, m_iMaxSpeed);
	DDV_MinMaxInt(pDX, m_iMaxSpeed, -300000, 300000);
	DDX_Text(pDX, IDC_EDIT_WATER_LEVEL_THRES, m_iWaterLevelThres);
	DDV_MinMaxInt(pDX, m_iWaterLevelThres, -10000000, 1000000);
	DDX_Text(pDX, IDC_EDIT_TIMEROUT_DURA, m_iTimeoutDura);
	DDV_MinMaxInt(pDX, m_iTimeoutDura, 1, 65535);
	DDX_Text(pDX, IDC_EDIT_DWELL_TIME, m_iDwellTime);
	DDV_MinMaxInt(pDX, m_iDwellTime, 0, 86400);
	DDX_Text(pDX, IDC_EDIT_DECECTION_STEP, m_iDecectStep);
	DDV_MinMaxInt(pDX, m_iDecectStep, 0, 100000);
	DDX_Text(pDX, IDC_EDIT_VIDEO_CENTER_LINE_DISTANCE, m_iMiddleWidth);
	DDV_MinMaxInt(pDX, m_iMiddleWidth, 0, 20000);

	DDX_Text(pDX, IDC_EDIT1, m_tPolygonPoint[0].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT2, m_tPolygonPoint[0].ipointRightDown);
	DDX_Text(pDX, IDC_EDIT3, m_tPolygonPoint[1].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT22, m_tPolygonPoint[1].ipointRightDown);

	DDX_Text(pDX, IDC_EDIT4, m_tPolygonPoint[2].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT30, m_tPolygonPoint[2].ipointRightDown);
	DDX_Text(pDX, IDC_EDIT5, m_tPolygonPoint[3].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT34, m_tPolygonPoint[3].ipointRightDown);

	DDX_Text(pDX, IDC_EDIT6, m_tPolygonPoint[4].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT38, m_tPolygonPoint[4].ipointRightDown);
	DDX_Text(pDX, IDC_EDIT7, m_tPolygonPoint[5].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT24, m_tPolygonPoint[5].ipointRightDown);

	DDX_Text(pDX, IDC_EDIT23, m_tPolygonPoint[6].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT26, m_tPolygonPoint[6].ipointRightDown);
	DDX_Text(pDX, IDC_EDIT13, m_tPolygonPoint[7].iPointLeftUp);
	DDX_Text(pDX, IDC_EDIT42, m_tPolygonPoint[7].ipointRightDown);
}


BEGIN_MESSAGE_MAP(CLS_WaterSpeedRule, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_WATERSPEED_GET, &CLS_WaterSpeedRule::OnBnClickedButtonWaterspeedGet)
	ON_BN_CLICKED(IDC_BUTTON_WATERSPEED_SET, &CLS_WaterSpeedRule::OnBnClickedButtonWaterspeedSet)
END_MESSAGE_MAP()


// CLS_WaterSpeedRule message handler

void CLS_WaterSpeedRule::OnBnClickedButtonWaterspeedGet()
{
	// TODO: Add control notification handler code here
	//VCA_CMD_WATER_FLOW
	//NetClient_VCAGetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen)
	//pCmdChan->GetWaterFlowSpeed(_iChannel, _lpCmdBuf, _iCmdBufLen);
	VcaFlowSpeedParam tInfo;
	memset(&tInfo,0, sizeof(VcaFlowSpeedParam));
	tInfo.tRule.iSceneID = m_cboSceneID.GetCurSel();
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_WATER_FLOW, m_iChannelNO, &tInfo, sizeof(VcaFlowSpeedParam));
	if(RET_SUCCESS == iRet)
	{
		m_iChannelNO = tInfo.iChanNo;
		m_cboSceneID.SetCurSel(tInfo.tRule.iSceneID);
		m_cboRuleID.SetCurSel(tInfo.tRule.iRuleID);
		m_cboEventIsValid.SetCurSel(tInfo.tRule.iValid);
		m_cboRuleIsShow.SetCurSel(tInfo.iDisplayRule);
		m_cboAeraColor.SetCurSel(tInfo.iColor - 1);
		m_cboSpeedSensity.SetCurSel(tInfo.iSensitive);
		m_cboVideoRecord.SetCurSel(tInfo.iLinkRecord);
		m_cboCruiseAddMode.SetCurSel(tInfo.iCruiseAddMode);
		m_cboCruisePoints.SetCurSel(tInfo.iCruiseNum - 1);
		m_iDwellTime = tInfo.iInterval;
		m_cboPolyVerNum.SetCurSel(tInfo.tPonit.iPointNum - 3);

		for(int i = 0; i < tInfo.tPonit.iPointNum && i < MAX_VAR_GAUGE_POINT_NUM; i++)
		{
			m_tPolygonPoint[i].iPointLeftUp = tInfo.tPonit.stPoints[i].iX;
			m_tPolygonPoint[i].ipointRightDown = tInfo.tPonit.stPoints[i].iY;
		}
		m_cboApplyScene.SetCurSel(tInfo.iApplyScene);
		m_iDecectStep = tInfo.iDetectStep;
		m_iMiddleWidth = tInfo.iMiddleWidth;
		m_iDetectAltit = tInfo.iDetectAltitude - 10000000;
		m_iPointX1 = tInfo.tLinePonit.stStart.iX;
		m_iPointY1 = tInfo.tLinePonit.stStart.iY;
		m_iPointX2 = tInfo.tLinePonit.stEnd.iX;
		m_iPointY2 = tInfo.tLinePonit.stEnd.iY;
		m_iHorizonWidth = tInfo.iHorizontalWidth;
		m_iVerticWidth = tInfo.iVerticalWidth;
		m_iHypotWidth = tInfo.iHypotenuseWidth;
		m_cboAngelInputMode.SetCurSel(tInfo.iAngelInputMode);
		m_cboNightFocusMode.SetCurSel(tInfo.iNightFocusMode);
		m_cboSpeedDir.SetCurSel(tInfo.iWaterSpeedDirEnable);
		m_iDirPointX1 = tInfo.tWaterSpeedDirPoint.stStart.iX;
		m_iDirPointY1 = tInfo.tWaterSpeedDirPoint.stStart.iY;
		m_iDirPointX2 = tInfo.tWaterSpeedDirPoint.stEnd.iX;
		m_iDirPointY2 = tInfo.tWaterSpeedDirPoint.stEnd.iY;
		m_iSpeedRatio = tInfo.iWaterSpeedRatio / 100;
		m_cboFilterSens.SetCurSel(tInfo.iFilterSensitivity);
		m_cboLinkAreaType.SetCurSel(tInfo.iLinkAreaType);
		m_iMinSpeed = tInfo.iMinSpeed;
		m_iMaxSpeed = tInfo.iMaxSpeed;
		m_cboDisplayType.SetCurSel(tInfo.iDisplayType);
		m_iWaterLevelThres = tInfo.iWaterLevelThreshold;
		m_cboDetectMode.SetCurSel(tInfo.iDetectMode);
		m_cboTrackFrameNum.SetCurSel(tInfo.iTrackFrameNum - 2);
		m_cboFieldVision.SetCurSel(tInfo.iFieldOfVision);
		m_cboCorrLevel.SetCurSel(tInfo.iCorrlevel);
		m_cboStartDir.SetCurSel(tInfo.iStartDir);
		m_cboLightCtrl.SetCurSel(tInfo.iLightCtrl);
		m_cboDeflect.SetCurSel(tInfo.iDeflection);
		m_cboTimeoutEnable.SetCurSel(tInfo.iTimeoutEnable);
		m_iTimeoutDura = tInfo.iTimeoutDuration;
		AddLog(LOG_TYPE_SUCC, "","CLS_WaterSpeedRule::NetClient_VCAGetConfig[VCA_CMD_WATER_FLOW] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
		UpdateData(FALSE);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_WaterSpeedRule::NetClient_VCAGetConfig[VCA_CMD_WATER_FLOW] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_WaterSpeedRule::OnBnClickedButtonWaterspeedSet()
{
	// TODO: Add control notification handler code here
	UpdateData(TRUE);
	VcaFlowSpeedParam tInfo;
	memset(&tInfo, 0, sizeof(VcaFlowSpeedParam));
	tInfo.iSize = sizeof(VcaFlowSpeedParam);
	tInfo.iChanNo = m_iChannelNO;
	tInfo.tRule.iSceneID = m_cboSceneID.GetCurSel();
	tInfo.tRule.iRuleID = m_cboRuleID.GetCurSel();
	tInfo.tRule.iValid = m_cboEventIsValid.GetCurSel();
	tInfo.iDisplayRule = m_cboRuleIsShow.GetCurSel();
	tInfo.iColor = m_cboAeraColor.GetCurSel() + 1;
	tInfo.iSensitive = m_cboSpeedSensity.GetCurSel();
	tInfo.iLinkRecord = m_cboVideoRecord.GetCurSel();
	tInfo.iCruiseAddMode = m_cboCruiseAddMode.GetCurSel();
	tInfo.iCruiseNum = m_cboCruisePoints.GetCurSel() + 1;
	tInfo.iInterval = m_iDwellTime;
	tInfo.tPonit.iPointNum = m_cboPolyVerNum.GetCurSel() + 3;

	for(int i = 0; i < tInfo.tPonit.iPointNum && i < MAX_VAR_GAUGE_POINT_NUM; i++)
	{
		tInfo.tPonit.stPoints[i].iX = m_tPolygonPoint[i].iPointLeftUp;
		tInfo.tPonit.stPoints[i].iY = m_tPolygonPoint[i].ipointRightDown;
	}
	tInfo.iApplyScene = m_cboApplyScene.GetCurSel();
	tInfo.iDetectStep = m_iDecectStep;
	tInfo.iMiddleWidth = m_iMiddleWidth;
	tInfo.iDetectAltitude = m_iDetectAltit + 10000000;
	tInfo.tLinePonit.stStart.iX = m_iPointX1;
	tInfo.tLinePonit.stStart.iY = m_iPointY1;
	tInfo.tLinePonit.stEnd.iX = m_iPointX2;
	tInfo.tLinePonit.stEnd.iY = m_iPointY2;
	tInfo.iHorizontalWidth = m_iHorizonWidth;
	tInfo.iVerticalWidth = m_iVerticWidth;
	tInfo.iHypotenuseWidth = m_iHypotWidth;
	tInfo.iAngelInputMode = m_cboAngelInputMode.GetCurSel();
	tInfo.iNightFocusMode = m_cboNightFocusMode.GetCurSel();
	tInfo.iWaterSpeedDirEnable = m_cboSpeedDir.GetCurSel();
	tInfo.tWaterSpeedDirPoint.stStart.iX = m_iDirPointX1;
	tInfo.tWaterSpeedDirPoint.stStart.iY = m_iDirPointY1;
	tInfo.tWaterSpeedDirPoint.stEnd.iX = m_iDirPointX2;
	tInfo.tWaterSpeedDirPoint.stEnd.iY = m_iDirPointY2;
	tInfo.iWaterSpeedRatio = m_iSpeedRatio * 100;
	tInfo.iFilterSensitivity = m_cboFilterSens.GetCurSel();
	tInfo.iLinkAreaType = m_cboLinkAreaType.GetCurSel();
	tInfo.iMinSpeed = m_iMinSpeed;
	tInfo.iMaxSpeed = m_iMaxSpeed;
	tInfo.iDisplayType = m_cboDisplayType.GetCurSel();
	tInfo.iWaterLevelThreshold = m_iWaterLevelThres;
	tInfo.iDetectMode = m_cboDetectMode.GetCurSel();
	tInfo.iTrackFrameNum = m_cboTrackFrameNum.GetCurSel() + 2;
	tInfo.iFieldOfVision = m_cboFieldVision.GetCurSel();
	tInfo.iCorrlevel = m_cboCorrLevel.GetCurSel();
	tInfo.iStartDir = m_cboStartDir.GetCurSel();
	tInfo.iLightCtrl = m_cboLightCtrl.GetCurSel();
	tInfo.iDeflection = m_cboDeflect.GetCurSel();
	tInfo.iTimeoutEnable = m_cboTimeoutEnable.GetCurSel();
	tInfo.iTimeoutDuration = m_iTimeoutDura;
	
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_WATER_FLOW, m_iChannelNO, &tInfo, sizeof(VcaFlowSpeedParam));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_WaterSpeedRule::NetClient_VCASetConfig[VCA_CMD_WATER_FLOW] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_WaterSpeedRule::NetClient_VCASetConfig[VCA_CMD_WATER_FLOW] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

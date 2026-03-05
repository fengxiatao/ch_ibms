// VCAEVENT_WaterLevelDetection.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_WaterLevelDetection.h"


// CLS_VCAEVENT_WaterLevelDetection dialog

const int CONST_iMinIntervel = 1;
const int CONST_iMaxIntervel = 1440;

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_WaterLevelDetection, CDialog)

CLS_VCAEVENT_WaterLevelDetection::CLS_VCAEVENT_WaterLevelDetection(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_WaterLevelDetection::IDD, pParent)
{
	memset(&m_tGetWld, 0, sizeof(m_tGetWld));
	m_iReferCount = 0;
	m_vecPresetScene.clear();
	m_iArithmeticType = VCA_EVENT_WATER_LEVEL_DETECT;
}

CLS_VCAEVENT_WaterLevelDetection::~CLS_VCAEVENT_WaterLevelDetection()
{
}

void CLS_VCAEVENT_WaterLevelDetection::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHK_WLD_RULE_DISPLAY, m_chkDisplayRule);
	DDX_Control(pDX, IDC_CHK_WLD_DISPLAY_STATE, m_chkDisplayAlarmCount);
	DDX_Control(pDX, IDC_CBO_WLD_ALARM_COLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_CBO_WLD_AREA_COLOR, m_cboAreaColor);
	DDX_Control(pDX, IDC_COMBO_WLD_GaugeType, m_cboGaugeType);
	DDX_Control(pDX, IDC_COMBO_WLD_PRESET_NO, m_cboPresetNo);
	DDX_Control(pDX, IDC_EDT_WLD_SnapIntervel, m_edtSnapIntervel); 
	DDX_Control(pDX, IDC_EDT_WLD_BaseValue, m_edtBaseValue);
	DDX_Control(pDX, IDC_EDT_WLD_CurPresetID, m_edtCurPresetID);
	DDX_Control(pDX, IDC_EDIT_WLD_PRESETRANGE_BEGIN, m_edtPresetValueLow);
	DDX_Control(pDX, IDC_EDIT_WLD_PRESETRANGE_END, m_edtPresetValueHigh);
	DDX_Control(pDX, IDC_EDT_WLD_GaugeRect, m_edtGaugeRect);
	DDX_Control(pDX, IDC_EDT_WLD_AssistRect, m_edtAssistRect);
	DDX_Control(pDX, IDC_EDT_WLD_GaugeLine, m_edtGaugeLine);
	DDX_Control(pDX, IDC_EDT_WLD_ReferPoint, m_edtReferPoints);
	DDX_Control(pDX, IDC_EDIT_VCA_WLD_GaugeLength, m_edtGaugeLength);
	DDX_Control(pDX, IDC_COMBO_DIRECTION, m_cboDirection);
	DDX_Control(pDX, IDC_COMBO_GAUGED_MODE, m_cboGaugedMode);
	DDX_Control(pDX, IDC_EDIT_GAUGED_LINE, m_edtGaugedLine);
	DDX_Control(pDX, IDC_CHECK_SNAPENABLE, m_chkSnapEnable);
	DDX_Control(pDX, IDC_COMBO_SNAPNO, m_cboSnapNo);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_WaterLevelDetection, CDialog)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_COMBO_WLD_PRESET_NO, &CLS_VCAEVENT_WaterLevelDetection::OnCbnSelchangeComboWldPresetNo)
	ON_BN_CLICKED(IDC_BTN_WLD_GaugeRect, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawGaugeRect)
	ON_BN_CLICKED(IDC_BTN_WLD_AssistRect, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawAssistRect)
	ON_BN_CLICKED(IDC_BTN_WLD_GaugeLine, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawGaugeLine)
	ON_BN_CLICKED(IDC_BTN_WLD_ReferPoint, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawReferPoints)
	ON_BN_CLICKED(IDC_BUTTON_WLD_ADD_PRESET_SCENE, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldAddPresetScene)
	ON_BN_CLICKED(IDC_BUTTON_WLD_DELETE_PRESET_SCENE, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldDeletePresetScene)
	ON_BN_CLICKED(IDC_BUTTON_WLD_SET, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldSet)
	ON_BN_CLICKED(IDC_BUTTON_WLD_CLEAR_PRESET_SCENE, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldClearPresetScene)
	ON_BN_CLICKED(IDC_BUTTON_VCA_WLD_SHOW_REGION, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonVcaWldShowRegion)
	ON_CBN_SELCHANGE(IDC_COMBO_WLD_GaugeType, &CLS_VCAEVENT_WaterLevelDetection::OnCbnSelchangeComboWldGaugetype)
	ON_STN_CLICKED(IDC_STATIC_WLD_PRESET_SCENE_TOTAL_COUNT, &CLS_VCAEVENT_WaterLevelDetection::OnStnClickedStaticWldPresetSceneTotalCount)
	ON_BN_CLICKED(IDC_BUTTON_GAUGED_LINE_DRAW, &CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonGaugedLineDraw)
END_MESSAGE_MAP()


// CLS_VCAEVENT_WaterLevelDetection message handlers

BOOL CLS_VCAEVENT_WaterLevelDetection::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	m_edtSnapIntervel.SetLimitText(LEN_16);
	m_edtBaseValue.SetLimitText(LEN_16);
	m_edtCurPresetID.SetLimitText(LEN_16);
	m_edtPresetValueLow.SetLimitText(LEN_16);
	m_edtPresetValueHigh.SetLimitText(LEN_16);
	m_edtGaugeRect.SetLimitText(LEN_256);
	m_edtAssistRect.SetLimitText(LEN_256);
	m_edtGaugeLine.SetLimitText(LEN_256);
	m_edtReferPoints.SetLimitText(LEN_256);
	m_edtGaugeLength.SetLimitText(LEN_16);
	UI_UpdateDialog();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_VCAEVENT_WaterLevelDetection::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		JudgeVCAStatus();
		UI_UpdateSdkParam();
	}
}

void CLS_VCAEVENT_WaterLevelDetection::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STC_WLD_AREA_COLOR, IDS_VCA_NOALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_WLD_ALARM_COLOR, IDS_VCA_ALARM_COLOR);
	SetDlgItemTextEx(IDC_CHK_WLD_RULE_DISPLAY, IDS_VCA_DISPLAY_RULE);
	SetDlgItemTextEx(IDC_CHK_WLD_DISPLAY_STATE, IDS_VCA_DISPLAY_ALARMSTATUS);
	SetDlgItemTextEx(IDC_BUTTON_WLD_SET, IDS_SET);
	SetDlgItemTextEx(IDC_BTN_WLD_GaugeRect, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_WLD_AssistRect, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_WLD_GaugeLine, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_WLD_ReferPoint, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_STC_WLD_INTERVAL, IDS_CFG_VCA_WLD_INTERVAL);
	SetDlgItemTextEx(IDC_STC_WLD_SnapIntervel, IDS_VCA_WLD_INTERVAL);
	SetDlgItemTextEx(IDC_STC_WLD_BaseValue, IDS_VCA_WLD_BWL);
	SetDlgItemTextEx(IDC_STC_WLD_CurPresetID, IDS_VCA_WLD_CUR_PRESET);
	SetDlgItemTextEx(IDC_STATIC_WLD_GaugeType, IDS_VCA_WLD_GAUGE_TYPE);
	SetDlgItemTextEx(IDC_STATIC_WLD_PRESET_SCENE_INFO, IDS_VCA_WLD_PRESET_SCENE);
	SetDlgItemTextEx(IDC_STATIC_WLD_PRESET_NO, IDS_VCA_WLD_PRESET_NUMBER);
	SetDlgItemTextEx(IDC_STATIC_WLD_PRESETRANGE, IDS_VCA_WLD_PRESET_RANGE);
	SetDlgItemTextEx(IDC_STC_WLD_GaugeRect, IDS_VCA_WLD_GAUGE_AREA);
	SetDlgItemTextEx(IDC_STC_WLD_AssistRect, IDS__VCA_WLD_AID_AREA);
	SetDlgItemTextEx(IDC_STC_WLD_GaugeLine, IDS_VCA_WLD_REFER_LINE);
	SetDlgItemTextEx(IDC_STC_WLD_ReferPoint, IDS_VCA_WLD_REFER_POINT);
	SetDlgItemTextEx(IDC_BUTTON_WLD_ADD_PRESET_SCENE, IDS_VCA_WLD_ADD);
	SetDlgItemTextEx(IDC_BUTTON_WLD_DELETE_PRESET_SCENE, IDS_VCA_WLD_DELETE_PRESET_SCENE);
	SetDlgItemTextEx(IDC_BUTTON_WLD_CLEAR_PRESET_SCENE, IDS_VCA_WLD_CLEAR_PRESET_SCENE);
	SetDlgItemTextEx(IDC_STC_WLD_BaseValueEx, IDS_VCA_WLD_MM);
	SetDlgItemTextEx(IDC_STATIC_WLD_PRESETRANGE2, IDS_VCA_WLD_MM);
	SetDlgItemTextEx(IDC_STC_WLD_CURPRESETID1, IDS_VCA_WLD_RANGE);
	SetDlgItemTextEx(IDC_BUTTON_VCA_WLD_SHOW_REGION, IDS_VCA_WLD_SHOW_REGION);
	SetDlgItemText(IDC_STATIC_GAUGED_MODE, GetTextByLan(_T("检测模式"),_T("Test Mode")));
	SetDlgItemText(IDC_STATIC_POINT_NUM, GetTextByLan(_T("点个数"),_T("PointNum")));
	SetDlgItemText(IDC_BUTTON_GAUGED_LINE_DRAW, GetTextByLan(_T("绘制"),_T("Draw")));
	SetDlgItemText(IDC_STATIC_GAUGELINE, GetTextByLan(_T("点坐标"),_T("Point")));
	SetDlgItemText(IDC_STATIC_CURGAUGENO, GetTextByLan(_T("当前水尺编号"),_T("Current Gauge No")));
	
	CString cstrColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), 
		GetTextEx(IDS_VCA_COL_YELLOW), GetTextEx(IDS_VCA_COL_BLUE), 
		GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), 
		GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboAlarmColor.ResetContent();
	m_cboAreaColor.ResetContent();
	for (int i = 0; i < sizeof(cstrColor)/sizeof(CString); i++)
	{
		m_cboAreaColor.InsertString(i, cstrColor[i]);
		m_cboAlarmColor.InsertString(i, cstrColor[i]);
	}
	m_cboAreaColor.SetCurSel(0);
	m_cboAlarmColor.SetCurSel(0);

	m_cboPresetNo.EnableWindow(FALSE);


	CString cstrPresetNo;
	m_cboPresetNo.ResetContent();
	for (int i = 0; i < MAX_PRESET_NUM_EX; ++i)
	{
		cstrPresetNo.Format("%d", i + 1);
		m_cboPresetNo.InsertString(i, cstrPresetNo);
	}
	m_cboPresetNo.SetCurSel(0);

	GetDlgItem(IDC_STC_WLD_CurPresetID)->ShowWindow(SW_HIDE);
	m_edtCurPresetID.ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STC_WLD_CURPRESETID1)->ShowWindow(SW_HIDE);
	//GetDlgItem(IDC_BUTTON_VCA_WLD_SHOW_REGION)->ShowWindow(SW_HIDE);

	m_cboGaugedMode.ResetContent();
	m_cboGaugedMode.InsertString(0,GetTextByLan(_T("算法"),_T("Algo")));
	m_cboGaugedMode.InsertString(1,GetTextByLan(_T("人工标定"),_T("manual")));
	m_cboGaugedMode.SetCurSel(0);

	m_cboSnapNo.ResetContent();
	for (int i = 1; i <= 500;i++)
	{
		CString strIndex;
		strIndex.Format("%d", i);
		m_cboSnapNo.AddString(strIndex);
	}
	m_cboSnapNo.SetCurSel(0);

	SetDlgItemText(IDC_CHECK_SNAPENABLE, GetTextByLan(_T("抓拍预置位"),_T("Snap PreLocation")));
	SetDlgItemText(IDC_STATIC_SNAPNO, GetTextByLan(_T("抓拍预置位"),_T("Snap PreLocation No")));
}

void CLS_VCAEVENT_WaterLevelDetection::UI_UpdateSdkParam()
{
	if (!CheckPublicPara())
	{
		return;
	}

	VCASuspendResult tParam = {0};
	tParam.iBufSize = sizeof(VCASuspendResult);
	int iRetBytes = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &tParam, sizeof(tParam), &iRetBytes);
	vca_TVCAParam* ptVcaPara = m_pVcaParam;
	memset(ptVcaPara, 0, sizeof(vca_TVCAParam));
	ptVcaPara->iChannelID = m_iChannelNO;

	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_CHANNEL, m_iChannelNO, ptVcaPara, sizeof(vca_TVCAParam));
	if (0 == ptVcaPara->chnParam[ptVcaPara->iChannelID].iEnable)
	{
		AddLog(LOG_TYPE_MSG, "", "VcaParam: iChannelNO(%d), iEnable(%d)", m_iChannelNO, ptVcaPara->chnParam[ptVcaPara->iChannelID].iEnable);
	}

	int iByteReturn = -1;
	FuncAbilityLevel stFunAbilityLevel = {0};
	stFunAbilityLevel.iSize = sizeof(stFunAbilityLevel);
	stFunAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	stFunAbilityLevel.iSubFuncType = 55;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stFunAbilityLevel, sizeof(stFunAbilityLevel), &iByteReturn);
	if (iRet < 0 || strlen(stFunAbilityLevel.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_WaterLevelDetection::GetFuncVideoTranscoding] GetDevConfig  Failed! m_iLogonId %d", m_iLogonID);
		return;
	}
	else
	{
		int iParam = _ttoi(stFunAbilityLevel.cParam);
		int iIndex = 0;
		m_cboGaugeType.ResetContent();
		if (VCA_EVENT_WATER_LEVEL_DETECT == m_iArithmeticType)
		{
			if((iParam &0x01) != 0)
			{
				iIndex = m_cboGaugeType.AddString(GetTextEx(IDS_VCA_WLD_WIDE_RULERR));
				m_cboGaugeType.SetItemData(iIndex, GAUGE_TYPE_WIDE);
			}
			if (((iParam & 0x02) >> 1) != 0)
			{
				iIndex = m_cboGaugeType.AddString(GetTextEx(IDS_VCA_WLD_NARROW_RULE));
				m_cboGaugeType.SetItemData(iIndex, GAUGE_TYPE_NARROW);
			}
			if (((iParam & 0x04) >> 2) != 0)
			{
				iIndex = m_cboGaugeType.AddString(GetTextEx(IDS_VCA_SPECILA_WATER_RULER));
				m_cboGaugeType.SetItemData(iIndex, GAUGE_TYPE_SPECIAL);
			}
			if (((iParam & 0x10) >> 4) != 0)
			{
				iIndex = m_cboGaugeType.AddString(GetTextByLan(_T("桩式水尺"),_T("Plie Type Ruler")));
				m_cboGaugeType.SetItemData(iIndex,GAUGE_TYPE_PILE);
			}
			if (((iParam & 0x20) >> 5) != 0)
			{
				iIndex = m_cboGaugeType.AddString(GetTextByLan(_T("无水尺"),_T("No Ruler")));
				m_cboGaugeType.SetItemData(iIndex,GAUGE_TYPE_NORULE);
			}
		}
		else if (VCA_EVENT_SEDIMENT == m_iArithmeticType)
		{
			if (((iParam & 0x20) >> 5) != 0)
			{
				iIndex = m_cboGaugeType.AddString(GetTextByLan(_T("无水尺"),_T("No Ruler")));
				m_cboGaugeType.SetItemData(iIndex,GAUGE_TYPE_NORULE);
			}
			if (((iParam & 0x08) >> 3) != 0)
			{
				iIndex = m_cboGaugeType.AddString(GetTextByLan(_T("方块水尺"), _T("Square water ruler")));
				m_cboGaugeType.SetItemData(iIndex, GAUGE_TYPE_SQUARE);
			}
		}
	}

	stFunAbilityLevel.iSize = sizeof(stFunAbilityLevel);
	stFunAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	stFunAbilityLevel.iSubFuncType = 114;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stFunAbilityLevel, sizeof(stFunAbilityLevel), &iByteReturn);
	if (iRet < 0 )
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_WaterLevelDetection::GetFuncVideoTranscoding] GetDevConfig  Failed! m_iLogonId %d", m_iLogonID);
		m_chkSnapEnable.SetCheck(0);
		m_chkSnapEnable.EnableWindow(FALSE);
		m_cboSnapNo.EnableWindow(FALSE);
	}
	else
	{
		int iParam = _ttoi(stFunAbilityLevel.cParam);
		if ((iParam & 0x04) > 0)
		{
			m_chkSnapEnable.EnableWindow(TRUE);
			m_cboSnapNo.EnableWindow(TRUE);
		}
		else
		{
			m_chkSnapEnable.SetCheck(0);
			m_chkSnapEnable.EnableWindow(FALSE);
			m_cboSnapNo.EnableWindow(FALSE);
		}
	}
	

	memset(&m_tGetWld, 0, sizeof(m_tGetWld));
	m_tGetWld.iSize = sizeof(WaterSamplePoint);
	m_tGetWld.stRule.iRuleID = m_iRuleID;
	m_tGetWld.stRule.iSceneID = m_iSceneID;
	int iCmd = VCA_CMD_GET_RULE15_WATER_LEVEL;
	if (VCA_EVENT_SEDIMENT == m_iArithmeticType)
	{
		iCmd = VCA_CMD_SEDIMENT;
	}
	iRet = NetClient_VCAGetConfig(m_iLogonID, iCmd, m_iChannelNO, &m_tGetWld, sizeof(WaterSamplePoint));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCAGetConfig]VCA_CMD_SET_RULE15_WATER_LEVEL fail!");
		return;
	}

	m_chkDisplayRule.SetCheck(m_tGetWld.stDisplayParam.iDisplayRule);
	m_chkDisplayAlarmCount.SetCheck(m_tGetWld.stDisplayParam.iDisplayStat);
	if(0 == m_tGetWld.stDisplayParam.iColor)
	{
		m_cboAreaColor.SetCurSel(1); //Default is green
	}
	else
		m_cboAreaColor.SetCurSel(m_tGetWld.stDisplayParam.iColor - 1);
	if (0 == m_tGetWld.stDisplayParam.iAlarmColor)
	{
		m_cboAlarmColor.SetCurSel(0);//Default is red
	}
	else
		m_cboAlarmColor.SetCurSel(m_tGetWld.stDisplayParam.iAlarmColor - 1);
	//m_cboGaugeType.SetCurSel(m_tGetWld.iGaugeType);
	//if (GAUGE_TYPE_PILE == m_tGetWld.iGaugeType || GAUGE_TYPE_NORULE == m_tGetWld.iGaugeType)
	//{
	//	m_cboGaugeType.SetCurSel(m_tGetWld.iGaugeType - 1);
	//}
	int iFlag = 0;
	for (iFlag = 0; iFlag < m_cboGaugeType.GetCount(); iFlag++)
	{
		int iGaugeType = m_cboGaugeType.GetItemData(iFlag);
		if (iGaugeType == m_tGetWld.iGaugeType)
		{
			m_cboGaugeType.SetCurSel(iFlag);
			break;
		}
	}
	if (iFlag == m_cboGaugeType.GetCount())
	{
		m_cboGaugeType.SetCurSel(-1);
	}

	SetDlgItemInt(IDC_EDT_WLD_SnapIntervel, m_tGetWld.iSnapInterval);
	float fTmp = (float)(m_tGetWld.iBaseValue/1000.0);
	CString sTmp;
	sTmp.Format(_T("%.3f"), fTmp);
	//SetDlgItemText(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD, sTmp);
	SetDlgItemText(IDC_EDT_WLD_BaseValue, sTmp);
	SetDlgItemInt(IDC_EDT_WLD_CurPresetID, m_tGetWld.iCurrentPresetId);
	if (VCA_EVENT_SEDIMENT == m_iArithmeticType)
	{
		SetDlgItemInt(IDC_EDIT_VCA_WLD_GaugeLength, m_tGetWld.iGaugeLength);
		GetDlgItem(IDC_STATIC_GAUGECOUNT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_GAUGECOUNT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_DIRECTION)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_COMBO_DIRECTION)->ShowWindow(SW_HIDE);
		if(GAUGE_TYPE_NORULE == m_tGetWld.iGaugeType)
		{
			GetDlgItem(IDC_STATIC_VCA_WLD_GaugeLength)->ShowWindow(SW_HIDE);
			GetDlgItem(IDC_EDIT_VCA_WLD_GaugeLength)->ShowWindow(SW_HIDE);
		}
	}
	else if(VCA_EVENT_WATER_LEVEL_DETECT == m_iArithmeticType )
	{
		GetDlgItem(IDC_STATIC_GAUGECOUNT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_GAUGECOUNT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_DIRECTION)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_COMBO_DIRECTION)->ShowWindow(SW_HIDE);
		if(GAUGE_TYPE_PILE == m_tGetWld.iGaugeType)
		{
			CString strCount,strDirection;
			GetDlgItem(IDC_STATIC_GAUGECOUNT)->ShowWindow(SW_SHOW);
			GetDlgItem(IDC_EDIT_GAUGECOUNT)->ShowWindow(SW_SHOW);
			GetDlgItem(IDC_STATIC_DIRECTION)->ShowWindow(SW_SHOW);
			GetDlgItem(IDC_COMBO_DIRECTION)->ShowWindow(SW_SHOW);
			strCount.Format("%d", m_tGetWld.iGaugeCount);
			GetDlgItem(IDC_EDIT_GAUGECOUNT)->SetWindowText(strCount);
			m_cboDirection.SetCurSel(m_tGetWld.iDirection);
		}
	}

	m_vecPresetScene.clear();
	for (int i = 0; i < m_tGetWld.iPresetCount; ++i)
	{
		m_vecPresetScene.push_back(m_tGetWld.stPresetInfo[i]);
	}

	m_cboGaugedMode.SetCurSel(m_tGetWld.iVirGaugeMode);
	SetDlgItemInt(IDC_EDIT_POINT_NUM, m_tGetWld.iVirGaugePointNum);
	CString szPointBuf;
	for (int i = 0; i < m_tGetWld.iVirGaugePointNum && i < MAX_VAR_GAUGE_POINT_NUM; i++)
	{
		CString tmpStr;
		tmpStr.Format("(%d,%d)", m_tGetWld.tVirGaugePoint[i].iX,  m_tGetWld.tVirGaugePoint[i].iY);
		szPointBuf += tmpStr;
	}
	m_edtGaugeLine.SetWindowText(szPointBuf);
	CString cstrCurrentGaugeNo = "";
	cstrCurrentGaugeNo.Format("%d", m_tGetWld.iCurGaugeNo);
	GetDlgItem(IDC_EDIT_CURGAUGENO)->SetWindowText(cstrCurrentGaugeNo);
	m_chkSnapEnable.SetCheck(m_tGetWld.iSnapPresetEnable);
	m_cboSnapNo.SetCurSel(m_tGetWld.iSnapPresetNo - 1);
	UI_UpdatePresetSceneText();

	UI_UpdatePresetScene();
}

void CLS_VCAEVENT_WaterLevelDetection::UI_UpdatePresetScene()
{
	int iPresetIndex = m_cboPresetNo.GetCurSel();
	SetDlgItemInt(IDC_EDIT_WLD_PRESETRANGE_BEGIN, m_tGetWld.stPresetInfo[iPresetIndex].stGaugeRange.iStart);
	SetDlgItemInt(IDC_EDIT_WLD_PRESETRANGE_END, m_tGetWld.stPresetInfo[iPresetIndex].stGaugeRange.iEnd);

	CString cstrGaugeRect;
	cstrGaugeRect.Format("(%d,%d,%d,%d)"
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeRect.left
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeRect.top
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeRect.right
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeRect.bottom);
	SetDlgItemText(IDC_EDT_WLD_GaugeRect, cstrGaugeRect);

	CString cstrAssistRect;
	cstrAssistRect.Format("(%d,%d,%d,%d)"
		, m_tGetWld.stPresetInfo[iPresetIndex].rcAssistRect.left
		, m_tGetWld.stPresetInfo[iPresetIndex].rcAssistRect.top
		, m_tGetWld.stPresetInfo[iPresetIndex].rcAssistRect.right
		, m_tGetWld.stPresetInfo[iPresetIndex].rcAssistRect.bottom);
	SetDlgItemText(IDC_EDT_WLD_AssistRect, cstrAssistRect);

	CString cstrGaugeLine;
	cstrGaugeLine.Format("(%d,%d,%d,%d)"
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeLine.left
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeLine.top
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeLine.right
		, m_tGetWld.stPresetInfo[iPresetIndex].rcGaugeLine.bottom);
	SetDlgItemText(IDC_EDT_WLD_GaugeLine, cstrGaugeLine);

	int iReferNum = m_tGetWld.stPresetInfo[iPresetIndex].iReferNum;
	CString strPointArray;
	CString strPoint[MAX_REFERPOINT_NUM];
	for(int i = 0; i < iReferNum && i < MAX_REFERPOINT_NUM; ++i)
	{
		strPoint[i].Format("(%d,%d,%d)"
			, m_tGetWld.stPresetInfo[iPresetIndex].stReferPoint[i].iPointX
			, m_tGetWld.stPresetInfo[iPresetIndex].stReferPoint[i].iPointY
			, m_tGetWld.stPresetInfo[iPresetIndex].stReferPoint[i].iValue);
		strPointArray += strPoint[i];
	}
	SetDlgItemText(IDC_EDT_WLD_ReferPoint,strPointArray);
}

void CLS_VCAEVENT_WaterLevelDetection::OnLanguageChanged()
{
	UI_UpdateDialog();
}

void CLS_VCAEVENT_WaterLevelDetection::OnCbnSelchangeComboWldPresetNo()
{
	UI_UpdatePresetScene();
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawGaugeRect()
{
	int iPointCount = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	RECT tRect = {0};
	GetInfoOnDrawVideo(&iPointCount, cPointBuf, &tRect, DrawType_Crowd);
	CString cstrGaugeRect;
	cstrGaugeRect.Format("(%d,%d,%d,%d)", tRect.left, tRect.top, tRect.right, tRect.bottom);
	m_edtGaugeRect.SetWindowText(cstrGaugeRect);
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawAssistRect()
{
	int iPointCount = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	RECT tRect = {0};
	GetInfoOnDrawVideo(&iPointCount, cPointBuf, &tRect, DrawType_Crowd);
	CString cstrAssistRect;
	cstrAssistRect.Format("(%d,%d,%d,%d)", tRect.left, tRect.top, tRect.right, tRect.bottom);
	m_edtAssistRect.SetWindowText(cstrAssistRect);
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawGaugeLine()
{
	int iPointCount = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	GetInfoOnDrawVideo(&iPointCount, cPointBuf, NULL, DrawType_tripwire);
	POINT tPoints[2] = {0} ;
	GetPointsFromString(cPointBuf, 2, tPoints);
	CString cstrGaugeLine;
	cstrGaugeLine.Format("(%d,%d,%d,%d)", tPoints[0].x, tPoints[0].y, tPoints[1].x, tPoints[1].y);
	m_edtGaugeLine.SetWindowText(cstrGaugeLine);
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedBtnWldDrawReferPoints()
{
	int iPointCount = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	GetInfoOnDrawVideo(&iPointCount, cPointBuf, NULL, DrawType_GaugePoint);
	m_iReferCount = iPointCount;
	POINT tPoints[VCA_MAX_POLYGON_POINT_NUM] = {0}; 
	GetPointsFromString(cPointBuf, m_iReferCount, tPoints);
	CString cstrMsg;
	CString cstrTmp;
	for (int i = 0; i < m_iReferCount && i < MAX_REFERPOINT_NUM; ++i)
	{
		cstrTmp.Format("(%d,%d,0)", tPoints[i].x, tPoints[i].y);
		cstrMsg += cstrTmp;
	}
	m_edtReferPoints.SetWindowText(cstrMsg);
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonGaugedLineDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(0, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_VAR_GAUGE_POINT_NUM);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 1)
		{
			m_edtGaugedLine.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_POINT_NUM, iPointNum);
		}
		else
		{
			m_edtGaugedLine.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_POINT_NUM, 0);
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_WaterLevelDetection::GetInfoOnDrawVideo(int* _piPointCount, char* _pcPointsBuf, RECT* _ptRect, int _iDrawType)
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}
	/* The following code can take out the corresponding parameters from the draw dialog box */
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetVcaWldRegion(&m_tGetWld.stPresetInfo[0]);
	m_pDlgVideoView->SetDrawType(_iDrawType);
	int iDirection = 0;
	if (NULL != _piPointCount && NULL != _pcPointsBuf)
	{
		m_pDlgVideoView->SetPointRegionParam(_pcPointsBuf, _piPointCount, &iDirection);
	}

	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (NULL != _ptRect)
		{
			m_pDlgVideoView->GetPointCoordirate((int*)&_ptRect->left, (int*)&_ptRect->top, (int*)&_ptRect->right, (int*)&_ptRect->bottom);
		}
	}

	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldAddPresetScene()
{
	bool blFind = false;
	int iIndex = -1;
	int iPresetId = m_cboPresetNo.GetCurSel() + 1;
	for (size_t i = 0; i < m_vecPresetScene.size(); ++i)
	{
		if (iPresetId == m_vecPresetScene[i].iPresetId)
		{
			blFind = true;
			iIndex = i;
			break;
		}
	}

	WaterPresetInfo tPresetScene = {0};
	tPresetScene.iSize = sizeof(WaterPresetInfo);
	tPresetScene.iPresetId = m_cboPresetNo.GetCurSel() + 1;
	tPresetScene.stGaugeRange.iStart = GetDlgItemInt(IDC_EDIT_WLD_PRESETRANGE_BEGIN);
	tPresetScene.stGaugeRange.iEnd = GetDlgItemInt(IDC_EDIT_WLD_PRESETRANGE_END);

	CString cstrGaugeRect;
	m_edtGaugeRect.GetWindowText(cstrGaugeRect);
	sscanf_s((LPSTR)(LPCTSTR)cstrGaugeRect, "(%d,%d,%d,%d)"
		, &tPresetScene.rcGaugeRect.left, &tPresetScene.rcGaugeRect.top
		, &tPresetScene.rcGaugeRect.right, &tPresetScene.rcGaugeRect.bottom);

	CString cstrAssistRect;
	m_edtAssistRect.GetWindowText(cstrAssistRect);
	sscanf_s((LPSTR)(LPCTSTR)cstrAssistRect, "(%d,%d,%d,%d)"
		, &tPresetScene.rcAssistRect.left, &tPresetScene.rcAssistRect.top
		, &tPresetScene.rcAssistRect.right, &tPresetScene.rcAssistRect.bottom);

	CString cstrGaugeLine;
	m_edtGaugeLine.GetWindowText(cstrGaugeLine);
	sscanf_s((LPSTR)(LPCTSTR)cstrGaugeLine, "(%d,%d,%d,%d)"
		, &tPresetScene.rcGaugeLine.left, &tPresetScene.rcGaugeLine.top
		, &tPresetScene.rcGaugeLine.right, &tPresetScene.rcGaugeLine.bottom);

	//if(blFind)
	//{
	//	tPresetScene.iReferNum = 0 == m_iReferCount ? m_vecPresetScene[iIndex].iReferNum : m_iReferCount;
	//}
	//else
	//{
		tPresetScene.iReferNum = m_iReferCount;
	//}
	
	WldPoint tWldPoints[VCA_MAX_POLYGON_POINT_NUM] = {0} ;
	CString cstrPointBuf;
	m_edtReferPoints.GetWindowText(cstrPointBuf);
	GetWldPointsFromString(cstrPointBuf.GetBuffer(), tPresetScene.iReferNum , tWldPoints);
	for (int i = 0; i < tPresetScene.iReferNum  && i < MAX_REFERPOINT_NUM; ++i)
	{
		tPresetScene.stReferPoint[i].iPointX = tWldPoints[i].iX;
		tPresetScene.stReferPoint[i].iPointY = tWldPoints[i].iY;
		tPresetScene.stReferPoint[i].iValue = tWldPoints[i].iValue;
	}

	if (blFind)
	{
		//update parameters
		memcpy(&m_vecPresetScene[iIndex], &tPresetScene, sizeof(WaterPresetInfo));
	}
	else
	{	//Added
		m_vecPresetScene.push_back(tPresetScene);
	}
	
	UI_UpdatePresetSceneText();
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldDeletePresetScene()
{
	int iPresetId = m_cboPresetNo.GetCurSel() + 1;
	for (size_t i = 0; i < m_vecPresetScene.size(); ++i)
	{
		if (iPresetId == m_vecPresetScene[i].iPresetId)
		{
			m_vecPresetScene.erase(m_vecPresetScene.begin() + i);
			break;
		}
	}
	UI_UpdatePresetSceneText();
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldClearPresetScene()
{
	m_vecPresetScene.clear();
	UI_UpdatePresetSceneText();
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonWldSet()
{
	if (!CheckPublicPara())
	{
		return;
	}

	int iSnapInterval = GetDlgItemInt(IDC_EDT_WLD_SnapIntervel);
	if (iSnapInterval < CONST_iMinIntervel || iSnapInterval > CONST_iMaxIntervel)
	{
		MessageBox(GetTextEx(IDS_VCAEVENT_WLD_USR_MSG), GetTextEx(IDS_CONFIG_PROMPT), MB_OK|MB_TOPMOST);
		return;
	}

	VCARuleParam  tRule = {0};
	tRule.stRule.iRuleID = m_iRuleID;
	tRule.stRule.iSceneID = m_iSceneID;
	NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RULE_PARAM, m_iChannelNO, &tRule, sizeof(VCARuleParam));

	WaterSamplePoint tWaterSample = {0};
	tWaterSample.iSize = sizeof(WaterSamplePoint);
	tWaterSample.stRule.iRuleID = m_iRuleID;
	tWaterSample.stRule.iSceneID = m_iSceneID;
	tWaterSample.stRule.iValid = tRule.stRule.iValid;
	tWaterSample.stDisplayParam.iDisplayRule = m_chkDisplayRule.GetCheck();
	tWaterSample.stDisplayParam.iDisplayStat = m_chkDisplayAlarmCount.GetCheck();
	tWaterSample.stDisplayParam.iColor = m_cboAreaColor.GetCurSel() + 1;
	tWaterSample.stDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel() + 1;
	tWaterSample.iSamplePointId = m_iSceneID;
	tWaterSample.iSnapInterval = iSnapInterval;
	//tWaterSample.iBaseValue = GetDlgItemInt(IDC_EDT_WLD_BaseValue);
	CString sBaseValue;
	GetDlgItemText(IDC_EDT_WLD_BaseValue, sBaseValue);
	//Convert string to float
	double fValue = atof(sBaseValue);
	fValue += 0.0005;
	tWaterSample.iBaseValue = (int)(fValue * 1000);
	tWaterSample.iCurrentPresetId = GetDlgItemInt(IDC_EDT_WLD_CurPresetID);
	int iIndex = m_cboGaugeType.GetCurSel();
	tWaterSample.iGaugeType = m_cboGaugeType.GetItemData(iIndex);
	if (VCA_EVENT_SEDIMENT == m_iArithmeticType && GAUGE_TYPE_NORULE != tWaterSample.iGaugeType)
	{
		tWaterSample.iGaugeLength = GetDlgItemInt(IDC_EDIT_VCA_WLD_GaugeLength);
	}
	else
	{
		if (GAUGE_TYPE_PILE == tWaterSample.iGaugeType)
		{
			tWaterSample.iGaugeCount = GetDlgItemInt(IDC_EDIT_GAUGECOUNT);
			tWaterSample.iDirection = m_cboDirection.GetCurSel();
		}
	}
	tWaterSample.iPresetCount = m_vecPresetScene.size();
	for (int i = 0; i < tWaterSample.iPresetCount && i < MAX_PRESET_NUM_EX; ++i)
	{
		memcpy(&tWaterSample.stPresetInfo[i], &m_vecPresetScene[i], sizeof(WaterPresetInfo));
	}

	//if(GAUGE_TYPE_VIRTUAL == tWaterSample.iGaugeType)//Effective under the virtual water gauge
	{
		tWaterSample.iVirGaugeMode = m_cboGaugedMode.GetCurSel();
		if(1 == tWaterSample.iVirGaugeMode)//Effective under manual calibration
		{
			tWaterSample.iVirGaugePointNum = GetDlgItemInt(IDC_EDIT_POINT_NUM);
			CString strPointStr;
			GetDlgItemText(IDC_EDIT_GAUGED_LINE, strPointStr);
			vca_TPolygon t_vp = {0};
			GetPolyFromString(strPointStr, tWaterSample.iVirGaugePointNum, t_vp);
			memcpy(&tWaterSample.tVirGaugePoint, &t_vp.stPoints, sizeof(tWaterSample.tVirGaugePoint));
		}
		
	}
	CString cstrCurrentGaugeNo = "";
	GetDlgItem(IDC_EDIT_CURGAUGENO)->GetWindowText(cstrCurrentGaugeNo);
	tWaterSample.iCurGaugeNo = _ttoi(cstrCurrentGaugeNo);

	FuncAbilityLevel tInfo = {0};
	int iByteReturn = -1;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	tInfo.iSubFuncType = 123;
	CString cstrTotalGaugeNum = "";
	int _iTotalGaugeNum = -1;
	int iRet = -1;

	GetDlgItem(IDC_EDIT_GAUGECOUNT)->GetWindowText(cstrTotalGaugeNum);
	_iTotalGaugeNum = _ttoi(cstrTotalGaugeNum);

	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&tInfo, sizeof(tInfo), &iByteReturn);

	if (iRet==0 && _iTotalGaugeNum > _ttoi(tInfo.cParam) && _ttoi(tInfo.cParam) > 0)
	{
		CString strerr = GetTextByLan(_T("水尺个数超过最大个数"),_T("Gauge Num greater than Max"));
		CString strTotal;
		strTotal.Format("%d", _ttoi(tInfo.cParam));
		strerr += strTotal;
		MessageBox(strerr);
		return;
	}
	GetDlgItem(IDC_EDIT_CURGAUGENO)->GetWindowText(cstrCurrentGaugeNo);

	if (_ttoi(tInfo.cParam) > 0 && (_ttoi(cstrCurrentGaugeNo) > _iTotalGaugeNum || _ttoi(cstrCurrentGaugeNo) < 1))
	{
		MessageBox(GetTextByLan(_T("水尺编号错误"),_T("Invalid gauge No")));
		return;
	}
	if (tWaterSample.iGaugeType != GAUGE_TYPE_PILE)
	{
		tWaterSample.iCurGaugeNo = 0;
		tWaterSample.iGaugeCount = 0;
	}
	tWaterSample.iSnapPresetEnable = m_chkSnapEnable.GetCheck();
	tWaterSample.iSnapPresetNo = m_cboSnapNo.GetCurSel() + 1;

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SET_RULE15_WATER_LEVEL, m_iChannelNO, &tWaterSample, sizeof(WaterSamplePoint));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCASetConfig]VCA_CMD_SET_RULE15_WATER_LEVEL water detect Set error = %d", GetLastError());
		return;
	}

	AddLog(LOG_TYPE_SUCC, "", "[NetClient_VCASetConfig]VCA_CMD_SET_RULE15_WATER_LEVEL water detect set success!");
}

void CLS_VCAEVENT_WaterLevelDetection::UI_UpdatePresetSceneText()
{
	int iPsCount = (int)m_vecPresetScene.size();
	if (iPsCount <= 0)
	{
		SetDlgItemText(IDC_STATIC_WLD_PRESET_SCENE_TOTAL_COUNT, "");
		return;
	}

	CString cstrTmp;
	CString cstrArr;
	CString cstrMsg;
	for (int i = 0; i < iPsCount - 1; ++i)
	{
		cstrTmp.Format("%d, ", m_vecPresetScene[i].iPresetId);
		cstrArr += cstrTmp;
	}
	cstrTmp.Format("%d", m_vecPresetScene[iPsCount - 1].iPresetId);
	cstrArr += cstrTmp;

	cstrTmp.Format("%d", iPsCount);
	cstrMsg = GetTextEx(IDS_CFG_VCA_WLD_ADDED) + cstrTmp + GetTextEx(IDS_CFG_VCA_WLD_PRESET_SCENE) + GetTextEx(IDS_CFG_VCA_WLD_ID_IS) + cstrArr;
	SetDlgItemText(IDC_STATIC_WLD_PRESET_SCENE_TOTAL_COUNT, cstrMsg);
}

void CLS_VCAEVENT_WaterLevelDetection::OnBnClickedButtonVcaWldShowRegion()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}

	int iPresetIndex = m_cboPresetNo.GetCurSel();
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetVcaWldRegion(&m_tGetWld.stPresetInfo[iPresetIndex]);
	m_pDlgVideoView->DoModal();
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_WaterLevelDetection::OnMainNotify(int _ulLogonID, int _iWparam, void* _pvLParam, void* _pvUser)
{
	if (NULL != m_pDlgVideoView)
	{
		m_pDlgVideoView->OnMainNotify(_ulLogonID, _iWparam, _pvLParam, _pvUser);
	}
}

void CLS_VCAEVENT_WaterLevelDetection::SetArithmeticType(int _iType)
{
	m_iArithmeticType = _iType;

	if (VCA_EVENT_SEDIMENT == m_iArithmeticType)
	{
		GetDlgItem(IDC_STATIC_VCA_WLD_GaugeLength)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_VCA_WLD_GaugeLength)->ShowWindow(SW_SHOW);
		SetDlgItemInt(IDC_EDIT_VCA_WLD_GaugeLength, WLD_DEFAULT_GAUGE_LENGTTH);

		//m_cboGaugeType.ResetContent();
		//int iIndex = m_cboGaugeType.AddString(GetTextByLan(_T("方块水尺"), _T("Square water ruler")));
		//m_cboGaugeType.SetItemData(iIndex, GAUGE_TYPE_SQUARE);
		//m_cboGaugeType.SetCurSel(0);
		
	}
}

void CLS_VCAEVENT_WaterLevelDetection::JudgeVCASetState(bool bIsVcaSet)
{
	GetDlgItem(IDC_BUTTON_WLD_SET)->EnableWindow(bIsVcaSet);
}


void CLS_VCAEVENT_WaterLevelDetection::OnCbnSelchangeComboWldGaugetype()
{
	// TODO: Add your control notification handler code here
	int iIndex = m_cboGaugeType.GetCurSel();
	int iGaugeType = m_cboGaugeType.GetItemData(iIndex);
	if (GAUGE_TYPE_PILE == iGaugeType)
	{
		GetDlgItem(IDC_STATIC_GAUGECOUNT)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_EDIT_GAUGECOUNT)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_DIRECTION)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_COMBO_DIRECTION)->ShowWindow(SW_SHOW);
	}
	else
	{
		GetDlgItem(IDC_STATIC_GAUGECOUNT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_GAUGECOUNT)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_DIRECTION)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_COMBO_DIRECTION)->ShowWindow(SW_HIDE);
		if (GAUGE_TYPE_NORULE == iGaugeType)
		{
			GetDlgItem(IDC_EDIT_VCA_WLD_GaugeLength)->ShowWindow(SW_HIDE);
			GetDlgItem(IDC_STATIC_VCA_WLD_GaugeLength)->ShowWindow(SW_HIDE);
		}
	}
}

void CLS_VCAEVENT_WaterLevelDetection::JudgeVCAStatus()
{
	VCASuspendResult tParam = {0};
	tParam.iBufSize = sizeof(VCASuspendResult);
	int iRetBytes = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &tParam, sizeof(tParam), &iRetBytes);
	if(0 == tParam.iStatus && 2 == tParam.iResult)
	{
		GetDlgItem(IDC_BUTTON_WLD_SET)->EnableWindow(FALSE);
	}
	else if (0 == tParam.iStatus && 1 == tParam.iResult)//Paused successfully
	{
		GetDlgItem(IDC_BUTTON_WLD_SET)->EnableWindow(TRUE);
	}
}

void CLS_VCAEVENT_WaterLevelDetection::OnStnClickedStaticWldPresetSceneTotalCount()
{
	// TODO: Add your control notification handler code here
}



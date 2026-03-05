// E:\SDK_ALL\trunk\Demo\NetClientDemo\Config\Events\CLS_VCAWaterSpeed.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_VCAWaterSpeed.h"
#include "../VCAEventPage.h"

// CLS_VCAWaterSpeed dialog
#define		MAX_WATER_SPEED_POINT		8

IMPLEMENT_DYNAMIC(CLS_VCAWaterSpeed, CDialog)

CLS_VCAWaterSpeed::CLS_VCAWaterSpeed(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAWaterSpeed::IDD, pParent)
{

}

CLS_VCAWaterSpeed::~CLS_VCAWaterSpeed()
{
}

void CLS_VCAWaterSpeed::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_WATER_SPEED_EVENT_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_CHECK_WATER_SPEED_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_COMBO_WATER_SPEED_COLOR, m_cboAreaColor);
	DDX_Control(pDX, IDC_COMBO_WATER_SPEED_LINK_RECORD, m_cboLinkRecord);
	DDX_Control(pDX, IDC_CBO_WATER_SPEED_ADD_POINT, m_cboAddPoint);
	DDX_Control(pDX, IDC_CBO_WATER_SPEED_APPLY_SCENE, m_ApplayScene);
	DDX_Control(pDX, IDC_EDIT_WATER_SPEED_REGION_POINTS, m_edtPoint);
	DDX_Control(pDX, IDC_EDIT_SENSITIVE, m_edtFileterSensitive);
	DDX_Control(pDX, IDC_COMBO_TESTTYPE, m_cboAreaType);
	DDX_Control(pDX, IDC_EDIT_LIMITSMALL, m_edtLimitSmall);
	DDX_Control(pDX, IDC_EDITLIMITBIG, m_edtLimitBig);
	DDX_Control(pDX, IDC_COMBO_SHOWTYPE, m_cboShowType);
	DDX_Control(pDX, IDC_EDIT_RATIO, m_edtSpeedRatio);
}


BEGIN_MESSAGE_MAP(CLS_VCAWaterSpeed, CDialog)
	ON_BN_CLICKED(IDC_BTN_WATER_SPEED_SET, &CLS_VCAWaterSpeed::OnBnClickedBtnWaterSpeedSet)
	ON_BN_CLICKED(IDC_BTN_WATER_SPEED_REGION_DRAW, &CLS_VCAWaterSpeed::OnBnClickedBtnWaterSpeedRegionDraw)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()

void CLS_VCAWaterSpeed::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_WATER_SPEED_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_WATER_SPEED_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STC_WATER_SPEED_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_WATER_SPEED_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_WATER_SPEED_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_WATER_SPEED_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_STC_WATERSPEED_COLOR, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_ALONE_LINK_REOCRD, GetTextByLan("联动录像", "LinkRec"));
	SetDlgItemText(IDC_STC_ADD_MODE, GetTextByLan("巡航点添加", "AddPointMode"));
	SetDlgItemText(IDC_STC_APPLY_SCENE, GetTextByLan("应用场景", "ApplyScene"));
	SetDlgItemText(IDC_STC_WATER_SPEED_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_ADD_POINT, GetTextByLan("巡航点数", "PointNum"));
	SetDlgItemText(IDC_STC_STAND_TIME, GetTextByLan("驻留时间", "StandTime"));
	SetDlgItemText(IDC_STC_DETECT_STEP, GetTextByLan("检测步长", "DetectStep"));
	SetDlgItemText(IDC_STC_MIDDLE_WIDTH, GetTextByLan("中线距离", "MiddleWidth"));
	SetDlgItemText(IDC_STATIC_SENSITIVE, GetTextByLan("过滤灵敏度", "Filter Sensitive"));
	SetDlgItemText(IDC_STATIC_TESTTYPE, GetTextByLan("检测类型", "Detect Type"));
	SetDlgItemText(IDC_STATIC_LIMITSMALL, GetTextByLan("最小流速(mm/s)", "Limit under(mm/s)"));
	SetDlgItemText(IDC_STATIC_LIMITBIG, GetTextByLan("最大流速(mm/s)", "Limit top(mm/s)"));
	SetDlgItemText(IDC_STATIC_SHOWTYPE, GetTextByLan("显示类型", "Show Type"));
	SetDlgItemText(IDC_STATIC_RATIO, GetTextByLan("流速系数[0-1000]", "Speed Ratio[0-1000]"));



	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), GetTextEx(IDS_VCA_COL_YELLOW), 
		GetTextEx(IDS_VCA_COL_BLUE), GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboAreaColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboAreaColor.InsertString(i, strColor[i]);
	}
	m_cboAreaColor.SetCurSel(0);
	
	m_cboLinkRecord.ResetContent();
	m_cboLinkRecord.InsertString(0, "No");
	m_cboLinkRecord.InsertString(1, "Yes");
	m_cboLinkRecord.SetCurSel(0);

	m_cboAddPoint.ResetContent();
	m_cboAddPoint.InsertString(0, "Auto");
	m_cboAddPoint.InsertString(1, "Manual");
	m_cboAddPoint.SetCurSel(0);


	m_ApplayScene.ResetContent();
	m_ApplayScene.InsertString(0, "Big");
	m_ApplayScene.InsertString(1, "Small");
	m_ApplayScene.SetCurSel(0);

	m_cboAreaType.ResetContent();
	m_cboAreaType.InsertString(0, GetTextByLan(_T("不联动"), _T("no Link")));
	m_cboAreaType.InsertString(1, GetTextByLan(_T("左边联动"), _T("Link to left")));
	m_cboAreaType.InsertString(2, GetTextByLan(_T("右边联动"), _T("Link to right")));
	m_cboAreaType.InsertString(3, GetTextByLan(_T("双边联动"), _T("Link to both sides")));
	m_cboAreaType.SetCurSel(0);

	m_cboShowType.ResetContent();
	m_cboShowType.InsertString(0, GetTextByLan(_T("瞬时"), _T("instantaneous")));
	m_cboShowType.InsertString(1, GetTextByLan(_T("平均"), _T("average")));
	m_cboShowType.SetCurSel(0);

	
}

void CLS_VCAWaterSpeed::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "OnBnClickedBtnWaterSpeedSet::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	VcaFlowSpeedParam tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.tRule.iSceneID = m_iSceneID;			
	tInfo.tRule.iRuleID = m_iRuleID;			
	
	int iRetValue = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_WATER_FLOW, m_iChannelNO, &tInfo, sizeof(tInfo));
	if(iRetValue<0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAWaterSpeed::NetClient_VCASetConfig[VCA_CMD_WATER_FLOW] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		 m_chkEventEnable.SetCheck(tInfo.tRule.iValid);
		 tInfo.iDisplayRule = m_chkShowRule.GetCheck();		
		 m_cboAreaColor.SetCurSel(tInfo.iColor);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_SENCIVITITY,tInfo.iSensitive);
		 m_cboLinkRecord.SetCurSel(tInfo.iLinkRecord);
		 m_cboAddPoint.SetCurSel(tInfo.iCruiseAddMode);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_ADD_POINT, tInfo.iCruiseNum);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_STAND_TIME, tInfo.iInterval);
		 m_ApplayScene.SetCurSel(tInfo.iApplyScene);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_DETECT_ALT, tInfo.iDetectAltitude);
		 SetDlgItemInt(IDC_EDIT_DETECT_STEP, tInfo.iDetectStep);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_MIDDLE_WIDTH, tInfo.iMiddleWidth);
		 SetDlgItemInt(IDC_EDIT_WATER_H_LENGTH, tInfo.iHorizontalWidth);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_V_LENGTH,tInfo.iVerticalWidth);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_HY_LENGTH, tInfo.iHypotenuseWidth);
		 SetDlgItemInt(IDC_EDIT_WATER_POINT_ONE_X, tInfo.tLinePonit.stStart.iX);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEE_POINT_ONE_Y, tInfo.tLinePonit.stStart.iY);
		 SetDlgItemInt(IDC_EDIT_WATER_POINT_TWO_X2, tInfo.tLinePonit.stEnd.iX);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEE_POINT_TWO_Y2, tInfo.tLinePonit.stEnd.iY);
		 SetDlgItemInt(IDC_EDIT_WATER_SPEED_REGION_POINTNUM, tInfo.tPonit.iPointNum);
		 CString szPointBuf;
		 for (int i = 0; i < tInfo.tPonit.iPointNum && i < MAX_WATER_SPEED_POINT; i++)
		 {
			 CString tmpStr;
			 tmpStr.Format("(%d,%d)", tInfo.tPonit.stPoints[i].iX,  tInfo.tPonit.stPoints[i].iY);
			 szPointBuf += tmpStr;
		 }
		 m_edtPoint.SetWindowText(szPointBuf);
		 m_cboAreaType.SetCurSel(tInfo.iLinkAreaType);
		 m_cboShowType.SetCurSel(tInfo.iDisplayType);

		 CString strIndex = "";
		 strIndex.Format("%d", tInfo.iFilterSensitivity);
		 m_edtFileterSensitive.SetWindowText(strIndex);

		 strIndex.Format("%d", tInfo.iMinSpeed);
		 m_edtLimitSmall.SetWindowText(strIndex);
		 strIndex.Format("%d", tInfo.iMaxSpeed);
		 m_edtLimitBig.SetWindowText(strIndex);
		 strIndex.Format("%d", tInfo.iFilterSensitivity);
		 m_edtFileterSensitive.SetWindowText(strIndex);
		 strIndex.Format("%d", tInfo.iWaterSpeedRatio);
		 m_edtSpeedRatio.SetWindowText(strIndex);
	}

}

// CLS_VCAWaterSpeed message handler

void CLS_VCAWaterSpeed::OnBnClickedBtnWaterSpeedSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "OnBnClickedBtnWaterSpeedSet::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VcaFlowSpeedParam tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.tRule.iSceneID = m_iSceneID;			
	tInfo.tRule.iRuleID = m_iRuleID;			
	tInfo.tRule.iValid = m_chkEventEnable.GetCheck();
	tInfo.iDisplayRule = m_chkShowRule.GetCheck();		
	tInfo.iColor = m_cboAreaColor.GetCurSel();
	tInfo.iSensitive = GetDlgItemInt(IDC_EDIT_WATER_SPEED_SENCIVITITY);
	tInfo.iLinkRecord = m_cboLinkRecord.GetCurSel();
	tInfo.iCruiseAddMode = m_cboAddPoint.GetCurSel();
	tInfo.iCruiseNum = GetDlgItemInt(IDC_EDIT_WATER_SPEED_ADD_POINT);
	tInfo.iInterval = GetDlgItemInt(IDC_EDIT_WATER_SPEED_STAND_TIME);
	tInfo.iApplyScene = m_ApplayScene.GetCurSel();
	tInfo.iDetectAltitude = GetDlgItemInt(IDC_EDIT_WATER_SPEED_DETECT_ALT);
	tInfo.iDetectStep = GetDlgItemInt(IDC_EDIT_DETECT_STEP);
	tInfo.iMiddleWidth = GetDlgItemInt(IDC_EDIT_WATER_SPEED_MIDDLE_WIDTH);
	tInfo.iHorizontalWidth = GetDlgItemInt(IDC_EDIT_WATER_H_LENGTH);
	tInfo.iVerticalWidth = GetDlgItemInt(IDC_EDIT_WATER_SPEED_V_LENGTH);
	tInfo.iHypotenuseWidth = GetDlgItemInt(IDC_EDIT_WATER_SPEED_HY_LENGTH);
	tInfo.tLinePonit.stStart.iX = GetDlgItemInt(IDC_EDIT_WATER_POINT_ONE_X);
	tInfo.tLinePonit.stStart.iY = GetDlgItemInt(IDC_EDIT_WATER_SPEE_POINT_ONE_Y);
	tInfo.tLinePonit.stEnd.iX = GetDlgItemInt(IDC_EDIT_WATER_POINT_TWO_X2);
	tInfo.tLinePonit.stEnd.iY = GetDlgItemInt(IDC_EDIT_WATER_SPEE_POINT_TWO_Y2);
	tInfo.tPonit.iPointNum = GetDlgItemInt(IDC_EDIT_WATER_SPEED_REGION_POINTNUM);
	CString strPointStr;
	GetDlgItemText(IDC_EDIT_WATER_SPEED_REGION_POINTS, strPointStr);
	vca_TPolygon t_vp = {0};
	GetPolyFromString(strPointStr, tInfo.tPonit.iPointNum, t_vp);
	memcpy(&tInfo.tPonit.stPoints, &t_vp.stPoints, sizeof(tInfo.tPonit.stPoints));

	m_edtFileterSensitive.GetWindowText(strPointStr);
	tInfo.iFilterSensitivity = _ttoi(strPointStr);

	tInfo.iLinkAreaType = m_cboAreaType.GetCurSel();

	m_edtLimitSmall.GetWindowText(strPointStr);
	tInfo.iMinSpeed = _ttoi(strPointStr);

	m_edtLimitBig.GetWindowText(strPointStr);
	tInfo.iMaxSpeed = _ttoi(strPointStr);

	tInfo.iDisplayType = m_cboShowType.GetCurSel();

	m_edtSpeedRatio.GetWindowText(strPointStr);
	tInfo.iWaterSpeedRatio = _ttoi(strPointStr);


	int iRetValue = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_WATER_FLOW, m_iChannelNO, &tInfo, sizeof(tInfo));
	if(iRetValue<0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAWaterSpeed::NetClient_VCASetConfig[VCA_CMD_WATER_FLOW] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}

	return;
}

void CLS_VCAWaterSpeed::OnBnClickedBtnWaterSpeedRegionDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_WATER_SPEED_POINT);
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
			m_edtPoint.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_WATER_SPEED_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_edtPoint.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_WATER_SPEED_REGION_POINTNUM, 0);
		}

		
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAWaterSpeed::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

BOOL CLS_VCAWaterSpeed::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE; 
	
}

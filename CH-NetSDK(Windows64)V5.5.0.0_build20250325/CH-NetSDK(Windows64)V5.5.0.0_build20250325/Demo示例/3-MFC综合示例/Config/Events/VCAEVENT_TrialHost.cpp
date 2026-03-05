 // DlgTrialHost.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_TrialHost.h"
//#include "afxdialogex.h"


// CDlgTrialHost dialog



IMPLEMENT_DYNAMIC(CLS_VCAEVENT_TrialHost, CDialog)

CLS_VCAEVENT_TrialHost::CLS_VCAEVENT_TrialHost(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_TrialHost::IDD, pParent)
{

}

CLS_VCAEVENT_TrialHost::CLS_VCAEVENT_TrialHost(int iType, CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_TrialHost::IDD, pParent)
{
	m_iType = iType;
	memset(m_stDectArea, 0, ((int)sizeof(vca_TPolygonEx) * VCA_MAX_REGION_NUM));
	memset(m_stInvalidDectArea, 0, ((int)sizeof(vca_TPolygonEx) * VCA_MAX_REGION_NUM));
}


CLS_VCAEVENT_TrialHost::~CLS_VCAEVENT_TrialHost()
{
}

void CLS_VCAEVENT_TrialHost::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_Color, m_Color);
	DDX_Control(pDX, IDC_EDIT_MIN_SIZE, m_SmallWidth);
	DDX_Control(pDX, IDC_EDIT_MAX_SIZE, m_MaxSize);
	DDX_Control(pDX, IDC_CBO_DEVTYPE, m_DevType);
	DDX_Control(pDX, IDC_SLIDER_SENSITIVE, m_Sensitive);
	DDX_Control(pDX, IDC_EDIT_PARAM1, m_iParam1);
	DDX_Control(pDX, IDC_EDIT_PARAM2, m_iParam2);
	DDX_Control(pDX, IDC_EDIT_PARAM3, m_iParam3);
	DDX_Control(pDX, IDC_CBO_CUR_REGIONNUM, m_curRegionNum);
	DDX_Control(pDX, IDC_EDIT_REGION_NUM, m_RegionNum);
	DDX_Control(pDX, IDC_EDIT_REGION_POINTNUM, m_Region_PointNum);
	DDX_Control(pDX, IDC_EDIT_REGION_POINTS, m_RegionPoint);
	DDX_Control(pDX, IDC_CBO_CUR_REGIONNUM2, m_CurInvalid_RegionNum);
	DDX_Control(pDX, IDC_EDIT_REGION_NUM2, m_InvalidRegionNum);
	DDX_Control(pDX, IDC_EDIT_REGION_POINTNUM2, m_InvalidRegionPointNum);
	DDX_Control(pDX, IDC_EDIT_REGION_POINTS2, m_InvalidPoint);
	DDX_Control(pDX, IDC_COMBO_GAMMASET, m_AlarmColor);
	DDX_Control(pDX, IDC_CHECK_Valid, m_chkEventValid);
	DDX_Control(pDX, IDC_CHECK_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_SHOWNUM, m_chkAlarmCount);
	DDX_Control(pDX, IDC_CHECK_SHOWTARGET, m_chkShowTarget);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_TrialHost, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_VCAEVENT_TrialHost::OnBnClickedButtonSet)
	ON_BN_CLICKED(IDC_BTN_REGION_DRAW, &CLS_VCAEVENT_TrialHost::OnBnClickedBtnRegionDraw)
	ON_BN_CLICKED(IDC_BTN_REGION_DRAW2, &CLS_VCAEVENT_TrialHost::OnBnClickedBtnRegionDraw2)
	ON_CBN_SELCHANGE(IDC_CBO_CUR_REGIONNUM, &CLS_VCAEVENT_TrialHost::OnCbnSelchangeCboCurRegionnum)
	ON_CBN_SELCHANGE(IDC_CBO_CUR_REGIONNUM2, &CLS_VCAEVENT_TrialHost::OnCbnSelchangeCboCurRegionnum2)
END_MESSAGE_MAP()


// CDlgTrialHost message handler

BOOL CLS_VCAEVENT_TrialHost::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	if (VCA_ABNORMAL == m_iType)
	{
		m_iCmd = VCA_CMD_ABNORMAL_NUMBER;
		SetDlgItemText(IDC_STATIC_PARAM1, GetTextByLan(_T("参考人数"),_T("Refer Num")));
		SetDlgItemText(IDC_STATIC_PARAM2, GetTextByLan(_T("异常模式"),_T("Abnormal mode")));
		SetDlgItemText(IDC_STATIC_PARAM3, GetTextByLan(_T("允许离开时间"),_T("Leave Time")));
	}
	else if (VCA_BODYTOUCH == m_iType || VCA_NEWFIGHT == m_iType)
	{
		m_iCmd = VCA_CMD_BODY_TOUCH;
		if (VCA_NEWFIGHT == m_iType)
		{
			m_iCmd = VCA_CMD_NEW_FIGHT;
		}
		SetDlgItemText(IDC_STATIC_PARAM1, GetTextByLan(_T("最小报警间隔"),_T("Min Alarm Interval")));
		GetDlgItem(IDC_STATIC_PARAM2)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_PARAM3)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_PARAM2)->ShowWindow(SW_HIDE);
        GetDlgItem(IDC_EDIT_PARAM3)->ShowWindow(SW_HIDE);
	}
	else if(VCA_STILLDECT == m_iType)
	{
		m_iCmd = VCA_CMD_STATIC_DETECTION;
		SetDlgItemText(IDC_STATIC_PARAM1, GetTextByLan(_T("最大静止时间"),_T("Max Still Time")));
		GetDlgItem(IDC_STATIC_PARAM2)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_PARAM3)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_PARAM2)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_EDIT_PARAM3)->ShowWindow(SW_HIDE);
	}

	UpdateUIText();
	return TRUE;  // return TRUE unless you set the focus to a control
}


void CLS_VCAEVENT_TrialHost::OnBnClickedButtonSet()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_GetUp::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VCACommonPara tInfo = {0};
	int iCmd = m_iCmd;
	int iRet = RET_FAILED;
	tInfo.iDevType = m_DevType.GetCurSel();
	tInfo.tRule.iSceneID = m_iSceneID;
	tInfo.tRule.iRuleID = m_iRuleID;
	tInfo.tRule.iValid = m_chkEventValid.GetCheck();
	tInfo.tDisplayParam.iDisplayRule = m_chkShowRule.GetCheck();
	tInfo.tDisplayParam.iDisplayStat = m_chkAlarmCount.GetCheck();
	tInfo.tDisplayParam.iColor = m_Color.GetCurSel() + 1;
	tInfo.tDisplayParam.iAlarmColor = m_AlarmColor.GetCurSel() + 1;
	tInfo.iDisplayTarget = m_chkShowTarget.GetCheck();
	tInfo.iMaxSize = GetDlgItemInt(IDC_EDIT_MAX_SIZE);
	tInfo.iMinSize = GetDlgItemInt(IDC_EDIT_MIN_SIZE);
	tInfo.iSensitivity = m_Sensitive.GetPos();
	tInfo.iRegionNum = GetDlgItemInt(IDC_EDIT_REGION_NUM);

	for(int i=0;i<MAX_DETECT_AREA_NUM && i< tInfo.iRegionNum;i++)
	{
		tInfo.stPoints[i].iPointNum = m_stDectArea[i].iPointNum;
		for (int j=0;j<tInfo.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX;j++)
		{
			tInfo.stPoints[i].stPoints[j] = m_stDectArea[i].stPoints[j];
		} 
	}

	if (VCA_ABNORMAL == m_iType)
	{
		VCAAbnormalNum tParam = {{0}};
		memcpy(&tParam, &tInfo, sizeof(tInfo));
		tParam.iReferNum = GetDlgItemInt(IDC_EDIT_PARAM1);
		tParam.iMode = GetDlgItemInt(IDC_EDIT_PARAM2);
		tParam.iLeaveTime = GetDlgItemInt(IDC_EDIT_PARAM3);
		tParam.iInvalidRegionNum = GetDlgItemInt(IDC_EDIT_REGION_NUM2);
		for(int i=0;i<MAX_DETECT_AREA_NUM && i< tParam.iInvalidRegionNum;i++)
		{
			tParam.stInvalidPoints[i].iPointNum = m_stInvalidDectArea[i].iPointNum;
			for (int j=0;j < tParam.stInvalidPoints[i].iPointNum && j < VCA_MAX_POLYGON_POINT_NUMEX;j++)
			{
				tParam.stInvalidPoints[i].stPoints[j] = m_stInvalidDectArea[i].stPoints[j];
			} 
		}
		iRet = NetClient_VCASetConfig(m_iLogonID, iCmd, m_iChannelNO, &tParam, sizeof(tParam));
	}
	else if (VCA_BODYTOUCH == m_iType)
	{
		VcaArmTouch tParam = {{0}};
		memcpy(&tParam, &tInfo, sizeof(tInfo));
		tParam.iTimeMin = GetDlgItemInt(IDC_EDIT_PARAM1);
		tParam.iInvalidRegionNum = GetDlgItemInt(IDC_EDIT_REGION_NUM2);
		for(int i=0;i<MAX_DETECT_AREA_NUM && i< tParam.iInvalidRegionNum;i++)
		{
			tParam.stInvalidPoints[i].iPointNum = m_stInvalidDectArea[i].iPointNum;
			for (int j=0;j < tParam.stInvalidPoints[i].iPointNum && j < VCA_MAX_POLYGON_POINT_NUMEX;j++)
			{
				tParam.stInvalidPoints[i].stPoints[j] = m_stInvalidDectArea[i].stPoints[j];
			} 
		}
		iRet = NetClient_VCASetConfig(m_iLogonID, iCmd, m_iChannelNO, &tParam, sizeof(tParam));
	}
	else if (VCA_NEWFIGHT == m_iType)
	{
		VcaNewFight tParam = {{0}};
		memcpy(&tParam, &tInfo, sizeof(tInfo));
		tParam.iTimeMin = GetDlgItemInt(IDC_EDIT_PARAM1);
		tParam.iInvalidRegionNum = GetDlgItemInt(IDC_EDIT_REGION_NUM2);
		for(int i=0;i<MAX_DETECT_AREA_NUM && i< tParam.iInvalidRegionNum;i++)
		{
			tParam.stInvalidPoints[i].iPointNum = m_stInvalidDectArea[i].iPointNum;
			for (int j=0;j < tParam.stInvalidPoints[i].iPointNum && j < VCA_MAX_POLYGON_POINT_NUMEX;j++)
			{
				tParam.stInvalidPoints[i].stPoints[j] = m_stInvalidDectArea[i].stPoints[j];
			} 
		}
		iRet = NetClient_VCASetConfig(m_iLogonID, iCmd, m_iChannelNO, &tParam, sizeof(tParam));
	}
	else if (VCA_STILLDECT == m_iType)
	{
		VcaStillDect tParam = {{0}};
		memcpy(&tParam, &tInfo, sizeof(tInfo));
		tParam.iStillTime = GetDlgItemInt(IDC_EDIT_PARAM1);
		tParam.iInvalidRegionNum = GetDlgItemInt(IDC_EDIT_REGION_NUM2);
		for(int i=0;i<MAX_DETECT_AREA_NUM && i< tParam.iInvalidRegionNum;i++)
		{
			tParam.stInvalidPoints[i].iPointNum = m_stInvalidDectArea[i].iPointNum;
			for (int j=0;j < tParam.stInvalidPoints[i].iPointNum && j < VCA_MAX_POLYGON_POINT_NUMEX;j++)
			{
				tParam.stInvalidPoints[i].stPoints[j] = m_stInvalidDectArea[i].stPoints[j];
			} 
		}
		iRet = NetClient_VCASetConfig(m_iLogonID, iCmd, m_iChannelNO, &tParam, sizeof(tParam));
	}
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_TRIALHOST::NetClient_VCASetConfig[%d] (%d, %d)",iCmd, m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","(%d)CLS_VCAEVENT_TRIALHOST::NetClient_VCASetConfig[%d] (%d, %d)",iRet, iCmd, m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_TrialHost::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_SHOWNUM, GetTextByLan("显示报警计数", "Alarm count"));
	SetDlgItemText(IDC_CHECK_SHOWTARGET, GetTextByLan("显示目标框", "Display target box"));
	SetDlgItemText(IDC_COMBO_Color, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_ALARMCOLOR, GetTextByLan("报警区域颜色", "Color of alarm area"));
	SetDlgItemText(IDC_STC_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_MIN_SIZE, GetTextByLan("最小宽度", "Minimum width"));
	SetDlgItemText(IDC_STC_MAX_SIZE, GetTextByLan("最大宽度", "Maximum width"));

	SetDlgItemText(IDC_STC_CUR_REGIONNUM, GetTextByLan("当前检测区域号", "Current area number"));
	SetDlgItemText(IDC_STC_REGION_NUM, GetTextByLan("已绘制区域个数", "valid area"));
	SetDlgItemText(IDC_STC_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_REGION_DRAW, GetTextByLan("绘制", "Draw"));

	SetDlgItemText(IDC_STC_CUR_REGIONNUM2, GetTextByLan("当前非法检测区域号", "Current invalid area number"));
	SetDlgItemText(IDC_STC_REGION_NUM2, GetTextByLan("已绘制非法区域个数", "invalid area"));
	SetDlgItemText(IDC_STC_REGION_POINTNUM2, GetTextByLan("检测非法区域点个数", "invalid area number"));
	SetDlgItemText(IDC_STC_REGION_POINTS2, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_REGION_DRAW2, GetTextByLan("绘制", "Draw"));

	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_Valid, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STC_GETUP_DEVTYPE, GetTextByLan("设备类型", "Device type"));

	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), GetTextEx(IDS_VCA_COL_YELLOW), 
		GetTextEx(IDS_VCA_COL_BLUE), GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_Color.ResetContent();
	m_AlarmColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_Color.InsertString(i, strColor[i]);
		m_AlarmColor.InsertString(i, strColor[i]);
	}
	m_Color.SetCurSel(0);
	m_AlarmColor.SetCurSel(0);

	m_curRegionNum.ResetContent();
	for (int i=0; i<VCA_MAX_REGION_NUM; i++)
	{
		CString cstrRegionNo;
		cstrRegionNo.Format("%d",i+1);
		m_curRegionNum.InsertString(i, cstrRegionNo);
	}
	m_curRegionNum.SetCurSel(0);

	m_CurInvalid_RegionNum.ResetContent();
	for (int i=0; i<VCA_MAX_REGION_NUM; i++)
	{
		CString cstrRegionNo;
		cstrRegionNo.Format("%d",i+1);
		m_CurInvalid_RegionNum.InsertString(i, cstrRegionNo);
	}
	m_CurInvalid_RegionNum.SetCurSel(0);

	m_DevType.ResetContent();
	m_DevType.InsertString(0, "IPC");
	m_DevType.InsertString(1, "NVR");
	m_DevType.SetCurSel(0);

	m_Sensitive.SetRange(0,100);
}

void CLS_VCAEVENT_TrialHost::OnBnClickedBtnRegionDraw()
{
	// TODO: Add your control notification handler code here
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, REGION_MAX_POINTS_NUM);
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
			m_RegionPoint.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_Region_PointNum.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_REGION_POINTNUM, 0);
		}

		int iRegionNo = m_curRegionNum.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_REGION_POINTNUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
	    m_RegionPoint.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_stDectArea[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_stDectArea[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_stDectArea[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
		}
		UpdateDrawFinishRegionNum();
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_TrialHost::UpdateDrawFinishRegionNum()
{
	int iRegionNum = 0, iInvalidRegionNum = 0;
	for (int i = 0;i<MAX_DETECT_AREA_NUM;i++)
	{
		if (m_stDectArea[i].iPointNum > 1)
		{
			iRegionNum++;
		}
	}
	SetDlgItemInt(IDC_EDIT_REGION_NUM, iRegionNum);

	for (int i = 0;i<MAX_DETECT_AREA_NUM;i++)
	{
		if (m_stInvalidDectArea[i].iPointNum > 1)
		{
			iInvalidRegionNum++;
		}
	}
	SetDlgItemInt(IDC_EDIT_REGION_NUM2, iInvalidRegionNum);
}

void CLS_VCAEVENT_TrialHost::OnBnClickedBtnRegionDraw2()
{
	// TODO: Add your control notification handler code here
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, REGION_MAX_POINTS_NUM);
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
			m_InvalidPoint.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_REGION_POINTNUM2, iPointNum);
		}
		else
		{
			m_InvalidPoint.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_REGION_POINTNUM2, 0);
		}

		int iRegionNo = m_CurInvalid_RegionNum.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_REGION_POINTNUM2);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_InvalidPoint.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_stInvalidDectArea[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_stInvalidDectArea[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_stInvalidDectArea[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
		}
		UpdateDrawFinishRegionNum();
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_TrialHost::OnCbnSelchangeCboCurRegionnum()
{
	// TODO: Add your control notification handler code here
	int iRegionNo = m_curRegionNum.GetCurSel();
	int iPointNum = m_stDectArea[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_REGION_POINTNUM, iPointNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPointNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)",m_stDectArea[iRegionNo].stPoints[i].iX, m_stDectArea[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_REGION_POINTS, cstPolygonBuf);
}

void CLS_VCAEVENT_TrialHost::OnCbnSelchangeCboCurRegionnum2()
{
	// TODO: Add your control notification handler code here
	int iRegionNo = m_CurInvalid_RegionNum.GetCurSel();
	int iPointNum = m_stInvalidDectArea[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_REGION_POINTNUM2, iPointNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPointNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)",m_stInvalidDectArea[iRegionNo].stPoints[i].iX, m_stInvalidDectArea[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_REGION_POINTS2, cstPolygonBuf);
}

void CLS_VCAEVENT_TrialHost::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}
	VCACommonPara tPara = {0};
	int iRet = RET_FAILED;
	if (VCA_ABNORMAL == m_iType)
	{
		VCAAbnormalNum tInfo = {0};
		tInfo.tCommonInfo.iDevType = m_DevType.GetCurSel();
		tInfo.tCommonInfo.tRule.iSceneID = m_iSceneID;
		tInfo.tCommonInfo.tRule.iRuleID = m_iRuleID;
		iRet = NetClient_VCAGetConfig(m_iLogonID, m_iCmd, m_iChannelNO, &tInfo, sizeof(tInfo));
		if (RET_SUCCESS != iRet)
		{
			return;
		}
		SetDlgItemInt(IDC_EDIT_PARAM1, tInfo.iReferNum);
		SetDlgItemInt(IDC_EDIT_PARAM2, tInfo.iMode);
		SetDlgItemInt(IDC_EDIT_PARAM3, tInfo.iLeaveTime);
		SetDlgItemInt(IDC_EDIT_REGION_NUM2, tInfo.iInvalidRegionNum);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, tInfo.tCommonInfo.iMinSize);

		memcpy(&tPara, &tInfo, sizeof(tPara));
		for (int i = 0; i < tInfo.iInvalidRegionNum && i < MAX_DETECT_AREA_NUM; i++)
		{
			m_stInvalidDectArea[i].iPointNum = tInfo.stInvalidPoints[i].iPointNum;
			for(int j = 0; j < tInfo.stInvalidPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_stInvalidDectArea[i].stPoints[j] = tInfo.stInvalidPoints[i].stPoints[j];
			}
		}
		m_CurInvalid_RegionNum.SetCurSel(0);
		OnCbnSelchangeCboCurRegionnum2();
	}
	else if (VCA_BODYTOUCH == m_iType)
	{
		VcaArmTouch tInfo = {0};
		tInfo.tCommonInfo.iDevType = m_DevType.GetCurSel();
		tInfo.tCommonInfo.tRule.iSceneID = m_iSceneID;
		tInfo.tCommonInfo.tRule.iRuleID = m_iRuleID;
		iRet = NetClient_VCAGetConfig(m_iLogonID, m_iCmd, m_iChannelNO, &tInfo, sizeof(tInfo));
		if (RET_SUCCESS != iRet)
		{
			return;
		}
		SetDlgItemInt(IDC_EDIT_PARAM1, tInfo.iTimeMin);
		SetDlgItemInt(IDC_EDIT_REGION_NUM2, tInfo.iInvalidRegionNum);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, tInfo.tCommonInfo.iMinSize);
		memcpy(&tPara, &tInfo, sizeof(tPara));
		for (int i = 0; i < tInfo.iInvalidRegionNum && i < MAX_DETECT_AREA_NUM; i++)
		{
			m_stInvalidDectArea[i].iPointNum = tInfo.stInvalidPoints[i].iPointNum;
			for(int j = 0; j < tInfo.stInvalidPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_stInvalidDectArea[i].stPoints[j] = tInfo.stInvalidPoints[i].stPoints[j];
			}
		}
		m_CurInvalid_RegionNum.SetCurSel(0);
		OnCbnSelchangeCboCurRegionnum2();
	}
	else if (VCA_STILLDECT == m_iType)
	{
		VcaStillDect tInfo = {0};
		tInfo.tCommonInfo.iDevType = m_DevType.GetCurSel();
		tInfo.tCommonInfo.tRule.iSceneID = m_iSceneID;
		tInfo.tCommonInfo.tRule.iRuleID = m_iRuleID;
		iRet = NetClient_VCAGetConfig(m_iLogonID, m_iCmd, m_iChannelNO, &tInfo, sizeof(tInfo));
		if (RET_SUCCESS != iRet)
		{
			return;
		}
		SetDlgItemInt(IDC_EDIT_PARAM1, tInfo.iStillTime);
		SetDlgItemInt(IDC_EDIT_REGION_NUM2, tInfo.iInvalidRegionNum);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, tInfo.tCommonInfo.iMinSize);
		memcpy(&tPara, &tInfo, sizeof(tPara));
		for (int i = 0; i < tInfo.iInvalidRegionNum && i < MAX_DETECT_AREA_NUM; i++)
		{
			m_stInvalidDectArea[i].iPointNum = tInfo.stInvalidPoints[i].iPointNum;
			for(int j = 0; j < tInfo.stInvalidPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_stInvalidDectArea[i].stPoints[j] = tInfo.stInvalidPoints[i].stPoints[j];
			}
		}
		m_CurInvalid_RegionNum.SetCurSel(0);
		OnCbnSelchangeCboCurRegionnum2();
	}
	else if (VCA_NEWFIGHT == m_iType)
	{
		VcaNewFight tInfo = {0};
		tInfo.tCommonInfo.iDevType = m_DevType.GetCurSel();
		tInfo.tCommonInfo.tRule.iSceneID = m_iSceneID;
		tInfo.tCommonInfo.tRule.iRuleID = m_iRuleID;
		iRet = NetClient_VCAGetConfig(m_iLogonID, m_iCmd, m_iChannelNO, &tInfo, sizeof(tInfo));
		if (RET_SUCCESS != iRet)
		{
			return;
		}
		SetDlgItemInt(IDC_EDIT_PARAM1, tInfo.iTimeMin);
		SetDlgItemInt(IDC_EDIT_REGION_NUM2, tInfo.iInvalidRegionNum);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, tInfo.tCommonInfo.iMinSize);
		memcpy(&tPara, &tInfo, sizeof(tPara));
		for (int i = 0; i < tInfo.iInvalidRegionNum && i < MAX_DETECT_AREA_NUM; i++)
		{
			m_stInvalidDectArea[i].iPointNum = tInfo.stInvalidPoints[i].iPointNum;
			for(int j = 0; j < tInfo.stInvalidPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_stInvalidDectArea[i].stPoints[j] = tInfo.stInvalidPoints[i].stPoints[j];
			}
		}
		m_CurInvalid_RegionNum.SetCurSel(0);
		OnCbnSelchangeCboCurRegionnum2();
	}
	UpdateCommonInfo(tPara);

}

void CLS_VCAEVENT_TrialHost::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_TrialHost::UpdateCommonInfo(VCACommonPara tInfo)
{
	m_DevType.SetCurSel(tInfo.iDevType);
	m_chkEventValid.SetCheck(tInfo.tRule.iValid);
	m_chkShowRule.SetCheck(tInfo.tDisplayParam.iDisplayRule);
	m_chkAlarmCount.SetCheck(tInfo.tDisplayParam.iDisplayStat);
	m_Color.SetCurSel(tInfo.tDisplayParam.iColor - 1);
	m_AlarmColor.SetCurSel(tInfo.tDisplayParam.iAlarmColor - 1);
	m_chkShowTarget.SetCheck(tInfo.iDisplayTarget);
	SetDlgItemInt(IDC_EDIT_MAX_SIZE, tInfo.iMaxSize);
	SetDlgItemInt(IDC_EDIT_GETUP_MIN_SIZE, tInfo.iMinSize);
	m_Sensitive.SetPos(tInfo.iSensitivity);
	SetDlgItemInt(IDC_EDIT_REGION_NUM, tInfo.iRegionNum);
	m_curRegionNum.SetCurSel(0);
	for (int i = 0; i < tInfo.iRegionNum && i<MAX_DETECT_AREA_NUM; i++)
	{
		m_stDectArea[i].iPointNum = tInfo.stPoints[i].iPointNum;
		for(int j = 0; j < tInfo.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
		{
			m_stDectArea[i].stPoints[j] = tInfo.stPoints[i].stPoints[j];
		}
	}
	OnCbnSelchangeCboCurRegionnum();
}

//..\Events\VCAEVENT_NewDuty.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "..\Events\VCAEVENT_NewDuty.h"


#define  VCA_MAX_REGION_NUM			8	//Total number of detection rule areas
#define  REGION_MAX_POINTS_NUM		10	//The maximum number of points in a detection area

// CLS_VCAEVENT_NewDuty dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_NewDuty, CDialog)

CLS_VCAEVENT_NewDuty::CLS_VCAEVENT_NewDuty(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_NewDuty::IDD, pParent)
{

}

CLS_VCAEVENT_NewDuty::~CLS_VCAEVENT_NewDuty()
{
}

void CLS_VCAEVENT_NewDuty::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_SLIDER_NEWDUTY_LEAVETIME, m_sldLeaveTime);
	DDX_Control(pDX, IDC_COMBO_NEWDUTY_COLOR, m_cboColor);
	DDX_Control(pDX, IDC_SLIDER_NEWDUTY_SENSITIVE, m_sldSensitive);
	DDX_Control(pDX, IDC_CBO_NEWDUTY_CUR_REGIONNUM, m_cboCurRegionNo);
	DDX_Control(pDX, IDC_CHECK_NEWDUTY_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_NEWDUTY_SHOWNUM, m_chkShowAlarmNum);
	DDX_Control(pDX, IDC_CHECK_NEWDUTY_SHOWTARGET, m_chkShowTargetBox);
	DDX_Control(pDX, IDC_EDIT_NEWDUTY_REGION_POINTS, m_editRegionPoins);
	DDX_Control(pDX, IDC_CHECK_NEWDUTY_EVENT_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_CBO_NEWDUTY_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_COMBO_NEWDUTY_ALARMCOLOR, m_cboAlarmColor);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_NewDuty, CDialog)
	ON_BN_CLICKED(IDC_BTN_NEWDUTY_SET, &CLS_VCAEVENT_NewDuty::OnBnClickedBtnNewdutySet)
	ON_BN_CLICKED(IDC_BTN_NEWDUTY_REGION_DRAW, &CLS_VCAEVENT_NewDuty::OnBnClickedBtnNewdutyRegionDraw)
	ON_CBN_SELCHANGE(IDC_CBO_NEWDUTY_CUR_REGIONNUM, &CLS_VCAEVENT_NewDuty::OnCbnSelchangeCboNewdutyCurRegionnum)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_NEWDUTY_SENSITIVE, &CLS_VCAEVENT_NewDuty::OnNMCustomdrawSliderNewdutySensitive)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_NEWDUTY_LEAVETIME, &CLS_VCAEVENT_NewDuty::OnNMCustomdrawSliderNewdutyLeavetime)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()



void CLS_VCAEVENT_NewDuty::OnBnClickedBtnNewdutySet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_LeaveBed::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VCAParaNewDuty tInfo = {0};
	tInfo.tVcaCommPara.iDevType = m_cboDevType.GetCurSel();
	tInfo.tVcaCommPara.tRule.iSceneID = m_iSceneID;
	tInfo.tVcaCommPara.tRule.iRuleID = m_iRuleID;
	tInfo.tVcaCommPara.tRule.iValid = m_chkEventEnable.GetCheck();
	tInfo.tVcaCommPara.tDisplayParam.iDisplayRule = m_chkShowRule.GetCheck();
	tInfo.tVcaCommPara.tDisplayParam.iDisplayStat = m_chkShowAlarmNum.GetCheck();
	tInfo.tVcaCommPara.tDisplayParam.iColor = m_cboColor.GetCurSel() + 1;
	tInfo.tVcaCommPara.tDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel() + 1;
	tInfo.tVcaCommPara.iDisplayTarget = m_chkShowTargetBox.GetCheck();
	tInfo.tVcaCommPara.iMaxSize = GetDlgItemInt(IDC_EDIT_NEWDUTY_MAX_SIZE);
	tInfo.tVcaCommPara.iMinSize = GetDlgItemInt(IDC_EDIT_NEWDUTY_MIN_SIZE);
	tInfo.tVcaCommPara.iSensitivity = m_sldSensitive.GetPos();
	tInfo.iLeaveTime = m_sldLeaveTime.GetPos();
	tInfo.iDutyNum = GetDlgItemInt(IDC_EDIT_NEWDUTY_DUTYNUM);
	tInfo.tVcaCommPara.iRegionNum = GetDlgItemInt(IDC_EDIT_NEWDUTY_REGION_NUM);

	for(int i=0;i<MAX_DETECT_AREA_NUM && i< tInfo.tVcaCommPara.iRegionNum;i++)
	{
		tInfo.tVcaCommPara.stPoints[i].iPointNum = m_tVCAParaNewDuty.tVcaCommPara.stPoints[i].iPointNum;
		for (int j=0;j<tInfo.tVcaCommPara.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX;j++)
		{
			tInfo.tVcaCommPara.stPoints[i].stPoints[j] = m_tVCAParaNewDuty.tVcaCommPara.stPoints[i].stPoints[j];
		}
	}

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_NET_DEPARTURE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_LeaveBed::NetClient_VCASetConfig[VCA_CMD_LEAVE_BED] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_LeaveBed::NetClient_VCASetConfig[VCA_CMD_LEAVE_BED] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_NewDuty::OnBnClickedBtnNewdutyRegionDraw()
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
			m_editRegionPoins.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_NEWDUTY_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_editRegionPoins.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_NEWDUTY_REGION_POINTNUM, 0);
		}

		int iRegionNo = m_cboCurRegionNo.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_NEWDUTY_REGION_POINTNUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_tVCAParaNewDuty.tVcaCommPara.stPoints[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_tVCAParaNewDuty.tVcaCommPara.stPoints[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tVCAParaNewDuty.tVcaCommPara.stPoints[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
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

void CLS_VCAEVENT_NewDuty::OnCbnSelchangeCboNewdutyCurRegionnum()
{
	int iRegionNo = m_cboCurRegionNo.GetCurSel();
	int iPiontNum = m_tVCAParaNewDuty.tVcaCommPara.stPoints[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_NEWDUTY_REGION_POINTNUM, iPiontNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPiontNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)", m_tVCAParaNewDuty.tVcaCommPara.stPoints[iRegionNo].stPoints[i].iX, m_tVCAParaNewDuty.tVcaCommPara.stPoints[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_NEWDUTY_REGION_POINTS, cstPolygonBuf);
}

void CLS_VCAEVENT_NewDuty::OnNMCustomdrawSliderNewdutySensitive(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_NEWDUTY_SENSITIVE_NUM, m_sldSensitive.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NewDuty::OnNMCustomdrawSliderNewdutyLeavetime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_NEWDUTY_LEAVETIME_NUM, m_sldLeaveTime.GetPos());
	*pResult = 0;
}

BOOL CLS_VCAEVENT_NewDuty::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();
	UpdateUIText();
	return TRUE;
}

void CLS_VCAEVENT_NewDuty::UpdateDrawFinishRegionNum()
{
	int iRegionNum = 0;
	for (int i = 0;i<MAX_DETECT_AREA_NUM;i++)
	{
		if (m_tVCAParaNewDuty.tVcaCommPara.stPoints[i].iPointNum > 1)
		{
			iRegionNum++;
		}
	}

	SetDlgItemInt(IDC_EDIT_NEWDUTY_REGION_NUM, iRegionNum);
}
void CLS_VCAEVENT_NewDuty::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_NewDuty::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_NEWDUTY_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_NEWDUTY_SHOWNUM, GetTextByLan("显示报警计数", "Alarm count"));
	SetDlgItemText(IDC_CHECK_NEWDUTY_SHOWTARGET, GetTextByLan("显示目标框", "Display target box"));
	SetDlgItemText(IDC_STC_NEWDUTY_COLOR, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_NEWDUTY_ALARMCOLOR, GetTextByLan("报警区域颜色", "Color of alarm area"));
	SetDlgItemText(IDC_STC_NEWDUTY_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_NEWDUTY_MIN_SIZE, GetTextByLan("最小宽度", "Minimum width"));
	SetDlgItemText(IDC_STC_NEWDUTY_MAX_SIZE, GetTextByLan("最大宽度", "Maximum width"));
	SetDlgItemText(IDC_STC_NEWDUTY_CUR_REGIONNUM, GetTextByLan("当前检测区域号", "Current detection area number"));
	SetDlgItemText(IDC_STC_NEWDUTY_REGION_NUM, GetTextByLan("已绘制区域个数", "The number of areas that have been drawn"));
	SetDlgItemText(IDC_STC_NEWDUTY_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_NEWDUTY_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_NEWDUTY_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_NEWDUTY_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_NEWDUTY_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STC_NEWDUTY_DEVTYPE, GetTextByLan("设备类型", "Device type"));
	SetDlgItemText(IDC_STC_NEWDUTY_LEAVETIME, GetTextByLan("最大超高时间", "Permissible time of departure"));
	SetDlgItemText(IDC_STC_NEWDUTY_DUTYNUM, GetTextByLan("值岗人数", "DutyNum"));

	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), GetTextEx(IDS_VCA_COL_YELLOW), 
		GetTextEx(IDS_VCA_COL_BLUE), GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboColor.ResetContent();
	m_cboAlarmColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboColor.InsertString(i, strColor[i]);
		m_cboAlarmColor.InsertString(i, strColor[i]);
	}
	m_cboColor.SetCurSel(0);
	m_cboAlarmColor.SetCurSel(0);

	m_cboCurRegionNo.ResetContent();
	for (int i=0; i<VCA_MAX_REGION_NUM; i++)
	{
		CString cstrRegionNo;
		cstrRegionNo.Format("%d",i+1);
		m_cboCurRegionNo.InsertString(i, cstrRegionNo);
	}
	m_cboCurRegionNo.SetCurSel(0);

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, "IPC");
	m_cboDevType.InsertString(1, "NVR");
	m_cboDevType.SetCurSel(0);

	m_sldSensitive.SetRange(0,100);
	m_sldLeaveTime.SetRange(1,1000);

}

void CLS_VCAEVENT_NewDuty::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAParaNewDuty tInfo = {0};
	tInfo.tVcaCommPara.iDevType = m_cboDevType.GetCurSel();
	tInfo.tVcaCommPara.tRule.iSceneID = m_iSceneID;
	tInfo.tVcaCommPara.tRule.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_NET_DEPARTURE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboDevType.SetCurSel(tInfo.tVcaCommPara.iDevType);
		m_chkEventEnable.SetCheck(tInfo.tVcaCommPara.tRule.iValid);
		m_chkShowRule.SetCheck(tInfo.tVcaCommPara.tDisplayParam.iDisplayRule);
		m_chkShowAlarmNum.SetCheck(tInfo.tVcaCommPara.tDisplayParam.iDisplayStat);
		m_cboColor.SetCurSel(tInfo.tVcaCommPara.tDisplayParam.iColor - 1);
		m_cboAlarmColor.SetCurSel(tInfo.tVcaCommPara.tDisplayParam.iAlarmColor - 1);
		m_chkShowTargetBox.SetCheck(tInfo.tVcaCommPara.iDisplayTarget);
		SetDlgItemInt(IDC_EDIT_NEWDUTY_MAX_SIZE, tInfo.tVcaCommPara.iMaxSize);
		SetDlgItemInt(IDC_EDIT_NEWDUTY_MIN_SIZE, tInfo.tVcaCommPara.iMinSize);
		m_sldSensitive.SetPos(tInfo.tVcaCommPara.iSensitivity);
		SetDlgItemInt(IDC_STATIC_NEWDUTY_SENSITIVE_NUM, m_sldSensitive.GetPos());
		m_sldLeaveTime.SetPos(tInfo.iLeaveTime);
		SetDlgItemInt(IDC_STC_NEWDUTY_LEAVETIME_NUM, m_sldLeaveTime.GetPos());
		SetDlgItemInt(IDC_STC_NEWDUTY_LEAVETIME_NUM, m_sldLeaveTime.GetPos());
		SetDlgItemInt(IDC_EDIT_NEWDUTY_DUTYNUM, tInfo.iDutyNum);

		memset(&m_tVCAParaNewDuty, 0, sizeof(m_tVCAParaNewDuty));
		m_tVCAParaNewDuty.tVcaCommPara.iRegionNum = tInfo.tVcaCommPara.iRegionNum;
		for (int i = 0; i < tInfo.tVcaCommPara.iRegionNum && i<MAX_DETECT_AREA_NUM; i++)
		{
			m_tVCAParaNewDuty.tVcaCommPara.stPoints[i].iPointNum = tInfo.tVcaCommPara.stPoints[i].iPointNum;
			for(int j = 0; j < tInfo.tVcaCommPara.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_tVCAParaNewDuty.tVcaCommPara.stPoints[i].stPoints[j] = tInfo.tVcaCommPara.stPoints[i].stPoints[j];
			}
		}

		m_cboCurRegionNo.SetCurSel(0);
		OnCbnSelchangeCboNewdutyCurRegionnum();
		UpdateDrawFinishRegionNum();
	}
}

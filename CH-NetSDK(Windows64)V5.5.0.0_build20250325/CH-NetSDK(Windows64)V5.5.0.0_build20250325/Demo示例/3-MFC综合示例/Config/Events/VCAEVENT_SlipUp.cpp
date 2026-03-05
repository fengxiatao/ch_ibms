// VCAEVENT_SlipUp.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_SlipUp.h"
#include "../VCAEventPage.h"

// CLS_VCAEVENT_SlipUp dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_SlipUp, CDialog)

CLS_VCAEVENT_SlipUp::CLS_VCAEVENT_SlipUp(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_SlipUp::IDD, pParent)
{

}

CLS_VCAEVENT_SlipUp::~CLS_VCAEVENT_SlipUp()
{
}

void CLS_VCAEVENT_SlipUp::DoDataExchange(CDataExchange* pDX)
{
	CLS_VCAEventBasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_SLIPUP_EVENT_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_CHECK_SLIPUP_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_SLIPUP_SHOWNUM, m_chkShowAlarmNum);
	DDX_Control(pDX, IDC_CHECK_SLIPUP_SHOWTARGET, m_chkShowTargetBox);
	DDX_Control(pDX, IDC_COMBO_SLIPUP_COLOR, m_cboColor);
	DDX_Control(pDX, IDC_COMBO_SLIPUP_ALARMCOLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_CBO_SLIPUP_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_SLIDER_SLIPUP_SENSITIVE, m_sldSensitive);
	DDX_Control(pDX, IDC_EDIT_SLIPUP_REGION_POINTS, m_editRegionPoins);
	DDX_Control(pDX, IDC_SLD_SLIPUP_HEIGHT, m_sldSlipUpHeight);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_SlipUp, CLS_VCAEventBasePage)
	ON_BN_CLICKED(IDC_BTN_SLIPUP_REGION_DRAW, &CLS_VCAEVENT_SlipUp::OnBnClickedBtnSlipupRegionDraw)
	ON_WM_SHOWWINDOW()
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_SLIPUP_SENSITIVE, &CLS_VCAEVENT_SlipUp::OnNMCustomdrawSliderSlipupSensitive)
	ON_BN_CLICKED(IDC_BTN_SLIPUP_SET, &CLS_VCAEVENT_SlipUp::OnBnClickedBtnSlipupSet)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_SLIPUP_HEIGHT, &CLS_VCAEVENT_SlipUp::OnNMCustomdrawSldSlipupHeight)
END_MESSAGE_MAP()


// CLS_VCAEVENT_SlipUp message handler

BOOL CLS_VCAEVENT_SlipUp::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VCAEVENT_SlipUp::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_SLIPUP_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_SLIPUP_SHOWNUM, GetTextByLan("显示报警计数", "Alarm count"));
	SetDlgItemText(IDC_CHECK_SLIPUP_SHOWTARGET, GetTextByLan("显示目标框", "Display target box"));
	SetDlgItemText(IDC_STC_SLIPUP_COLOR, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_SLIPUP_ALARMCOLOR, GetTextByLan("报警区域颜色", "Color of alarm area"));
	SetDlgItemText(IDC_STC_SLIPUP_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_SLIPUP_MIN_SIZE, GetTextByLan("最小宽度", "Minimum width"));
	SetDlgItemText(IDC_STC_SLIPUP_MAX_SIZE, GetTextByLan("最大宽度", "Maximum width"));
	SetDlgItemText(IDC_STC_SLIPUP_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_SLIPUP_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_SLIPUP_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_SLIPUP_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_SLIPUP_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STC_SLIPUP_DEVTYPE, GetTextByLan("设备类型", "Device type"));
	SetDlgItemText(IDC_STC_SLIPUP_HEIGHT, GetTextByLan("跌倒后最大高度", "Maximum height after fall"));

	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), GetTextEx(IDS_VCA_COL_YELLOW), 
		GetTextEx(IDS_VCA_COL_BLUE), GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboColor.ResetContent();
	m_cboAlarmColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboColor.InsertString(i, strColor[i]);
		m_cboAlarmColor.InsertString(i, strColor[i]);
	}

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, "IPC");
	m_cboDevType.InsertString(1, "NVR");

	m_sldSensitive.SetRange(0,100);

}

void CLS_VCAEVENT_SlipUp::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAParaSlipUp tInfo = {0};
	tInfo.tCommonPara.iDevType = 0;
	tInfo.tCommonPara.tRule.iSceneID = m_iSceneID;
	tInfo.tCommonPara.tRule.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SLIP_UP, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboDevType.SetCurSel(tInfo.tCommonPara.iDevType);
		m_chkEventEnable.SetCheck(tInfo.tCommonPara.tRule.iValid);
		m_chkShowRule.SetCheck(tInfo.tCommonPara.tDisplayParam.iDisplayRule);
		m_chkShowAlarmNum.SetCheck(tInfo.tCommonPara.tDisplayParam.iDisplayStat);
		m_cboColor.SetCurSel(tInfo.tCommonPara.tDisplayParam.iColor - 1);
		m_cboAlarmColor.SetCurSel(tInfo.tCommonPara.tDisplayParam.iAlarmColor - 1);
		m_chkShowTargetBox.SetCheck(tInfo.tCommonPara.iDisplayTarget);
		SetDlgItemInt(IDC_EDIT_SLIPUP_MAX_SIZE, tInfo.tCommonPara.iMaxSize);
		SetDlgItemInt(IDC_EDIT_SLIPUP_MIN_SIZE, tInfo.tCommonPara.iMinSize);
		m_sldSensitive.SetPos(tInfo.tCommonPara.iSensitivity);
		SetDlgItemInt(IDC_STATIC_SLIPUP_SENSITIVE_NUM, m_sldSensitive.GetPos());

		CString cstPolygonBuf = "";
		int iPointNum = tInfo.tCommonPara.stPoints[0].iPointNum;
		for(int j = 0; j < tInfo.tCommonPara.stPoints[0].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)",tInfo.tCommonPara.stPoints[0].stPoints[j].iX, tInfo.tCommonPara.stPoints[0].stPoints[j].iY);
		}
		SetDlgItemText(IDC_EDIT_SLIPUP_REGION_POINTS, cstPolygonBuf);

		m_sldSlipUpHeight.SetPos(tInfo.iHeightMax);
		SetDlgItemInt(IDC_STC_SLIPUP_HEIGHT_NUM, m_sldSlipUpHeight.GetPos());
	}

}

void CLS_VCAEVENT_SlipUp::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_SlipUp::OnBnClickedBtnSlipupRegionDraw()
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
			SetDlgItemInt(IDC_EDIT_SLIPUP_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_editRegionPoins.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_SLIPUP_REGION_POINTNUM, 0);
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}



void CLS_VCAEVENT_SlipUp::OnNMCustomdrawSliderSlipupSensitive(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_SLIPUP_SENSITIVE_NUM, m_sldSensitive.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_SlipUp::OnBnClickedBtnSlipupSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_GetUp::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VCAParaSlipUp tInfo = {0};
	tInfo.tCommonPara.iDevType = m_cboDevType.GetCurSel();
	tInfo.tCommonPara.tRule.iSceneID = m_iSceneID;
	tInfo.tCommonPara.tRule.iRuleID = m_iRuleID;
	tInfo.tCommonPara.tRule.iValid = m_chkEventEnable.GetCheck();
	tInfo.tCommonPara.tDisplayParam.iDisplayRule = m_chkShowRule.GetCheck();
	tInfo.tCommonPara.tDisplayParam.iDisplayStat = m_chkShowAlarmNum.GetCheck();
	tInfo.tCommonPara.tDisplayParam.iColor = m_cboColor.GetCurSel() + 1;
	tInfo.tCommonPara.tDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel() + 1;
	tInfo.tCommonPara.iDisplayTarget = m_chkShowTargetBox.GetCheck();
	tInfo.tCommonPara.iMaxSize = GetDlgItemInt(IDC_EDIT_SLIPUP_MAX_SIZE);
	tInfo.tCommonPara.iMinSize = GetDlgItemInt(IDC_EDIT_SLIPUP_MIN_SIZE);
	tInfo.tCommonPara.iSensitivity = m_sldSensitive.GetPos();
	tInfo.tCommonPara.iRegionNum = 1;
	tInfo.tCommonPara.stPoints[0].iPointNum = GetDlgItemInt(IDC_EDIT_SLIPUP_REGION_POINTNUM);
	tInfo.iHeightMax = m_sldSlipUpHeight.GetPos();

	vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
	CString cstPolygon = "";
	GetDlgItemText(IDC_EDIT_SLIPUP_REGION_POINTS, cstPolygon);
	GetPointsFromString(cstPolygon, tInfo.tCommonPara.stPoints[0].iPointNum, (POINT*)ptPolygon);
	for (int i = 0; i < tInfo.tCommonPara.stPoints[0].iPointNum && i < VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		tInfo.tCommonPara.stPoints[0].stPoints[i] = ptPolygon[i];
	}

	tInfo.tCommonPara.stPoints[0].iPointNum = GetDlgItemInt(IDC_EDIT_SLIPUP_REGION_POINTNUM);

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SLIP_UP, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_GetUp::NetClient_VCASetConfig[VCA_CMD_SLIP_UP] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_GetUp::NetClient_VCASetConfig[VCA_CMD_SLIP_UP] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_SlipUp::OnNMCustomdrawSldSlipupHeight(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_SLIPUP_HEIGHT_NUM, m_sldSlipUpHeight.GetPos());
	*pResult = 0;
}

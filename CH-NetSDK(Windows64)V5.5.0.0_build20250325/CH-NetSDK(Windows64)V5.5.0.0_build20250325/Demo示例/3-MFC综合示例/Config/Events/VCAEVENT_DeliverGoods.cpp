// E:\SDK_ALL\trunk\Demo\NetClientDemo\Config\Events\VCAEVENT_DeliverGoods.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_DeliverGoods.h"


// CLS_VcaDeliverGoods dialog

IMPLEMENT_DYNAMIC(CLS_VcaDeliverGoods, CDialog)

CLS_VcaDeliverGoods::CLS_VcaDeliverGoods(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaDeliverGoods::IDD, pParent)
{

}

CLS_VcaDeliverGoods::~CLS_VcaDeliverGoods()
{
}

void CLS_VcaDeliverGoods::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);


	DDX_Control(pDX, IDC_CHECK_DELIVERGOODS_EVENT_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_CHECK_DELIVERGOODS_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_DELIVERGOODS_SHOWNUM, m_chkShowAlarmNum);
	DDX_Control(pDX, IDC_CHECK_DELIVERGOODS_SHOWTARGET, m_chkShowTargetBox);
	DDX_Control(pDX, IDC_COMBO_DELIVERGOODS_COLOR, m_cboColor);
	DDX_Control(pDX, IDC_COMBO_DELIVERGOODS_ALARMCOLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_CBO_DELIVERGOODS_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_EDIT_DELIVERGOODS_REGION_POINTS, m_editRegionPoins);
}


BEGIN_MESSAGE_MAP(CLS_VcaDeliverGoods, CDialog)
	ON_BN_CLICKED(IDC_BTN_DELIVERGOODS_SET, &CLS_VcaDeliverGoods::OnBnClickedBtnDelivergoodsSet)
	ON_BN_CLICKED(IDC_BTN_DELIVERGOODS_REGION_DRAW, &CLS_VcaDeliverGoods::OnBnClickedBtnDelivergoodsRegionDraw)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// CLS_VcaDeliverGoods message handler

BOOL CLS_VcaDeliverGoods::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  add extra initialization here
	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaDeliverGoods::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_DELIVERGOODS_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_DELIVERGOODS_SHOWNUM, GetTextByLan("显示报警计数", "Alarm count"));
	SetDlgItemText(IDC_CHECK_DELIVERGOODS_SHOWTARGET, GetTextByLan("显示目标框", "Display target box"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_COLOR, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_ALARMCOLOR, GetTextByLan("报警区域颜色", "Color of alarm area"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_MIN_SIZE, GetTextByLan("最小宽度", "Minimum width"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_MAX_SIZE, GetTextByLan("最大宽度", "Maximum width"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_DELIVERGOODS_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_DELIVERGOODS_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_DELIVERGOODS_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STC_DELIVERGOODS_DEVTYPE, GetTextByLan("设备类型", "Device type"));

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
	

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, "IPC");
	m_cboDevType.InsertString(1, "NVR");
	m_cboDevType.SetCurSel(0);

}

void CLS_VcaDeliverGoods::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCADeliverGoods tInfo = {0};
	tInfo.iDevType = 0;
	tInfo.tRule.iSceneID = m_iSceneID;
	tInfo.tRule.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_DELIVERGOODS, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboDevType.SetCurSel(tInfo.iDevType);
		m_cboColor.SetCurSel(tInfo.tDisplayParam.iColor);
		m_cboAlarmColor.SetCurSel(tInfo.tDisplayParam.iAlarmColor);
		m_chkShowRule.SetCheck(tInfo.tDisplayParam.iDisplayRule);
		m_chkShowAlarmNum.SetCheck(tInfo.tDisplayParam.iDisplayStat);
		m_chkShowTargetBox.SetCheck(tInfo.iDisplayTarget);
		SetDlgItemInt(IDC_EDIT_DELIVERY_GOODS, tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_DELIVERGOODS_MIN_SIZE, tInfo.iMinSize);
		SetDlgItemInt(IDC_EDIT_DELIVERGOODS_MAX_SIZE, tInfo.iMaxSize);
		SetDlgItemInt(IDC_EDIT_DELIVERGOODS_REGION_POINTNUM, tInfo.iPointNum);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointNum && i<MAX_VCA_DELIVERGOODS_POINT_NUM; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.stPoints[i].iX, tInfo.stPoints[i].iY);
			m_tPoints[i].iX = tInfo.stPoints[i].iX;
			m_tPoints[i].iY = tInfo.stPoints[i].iY;
		}
		SetDlgItemText(IDC_EDIT_DELIVERGOODS_REGION_POINTS, cstPolygonBuf);
	}
}
void CLS_VcaDeliverGoods::OnBnClickedBtnDelivergoodsSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaDeliverGoods::OnBnClickedBtnDelivergoodsSet Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VCADeliverGoods tInfo = {0};
	tInfo.iDevType = m_cboDevType.GetCurSel();
	tInfo.tRule.iSceneID = m_iSceneID;
	tInfo.tRule.iRuleID = m_iRuleID;
	tInfo.tRule.iValid = m_chkEventEnable.GetCheck();
	tInfo.tDisplayParam.iColor = m_cboColor.GetCurSel();
	tInfo.tDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel();
	tInfo.tDisplayParam.iDisplayRule = m_chkShowRule.GetCheck();
	tInfo.tDisplayParam.iDisplayStat = m_chkShowAlarmNum.GetCheck();
	tInfo.iDisplayTarget = m_chkShowTargetBox.GetCheck();
	tInfo.iMinSize = GetDlgItemInt(IDC_EDIT_DELIVERGOODS_MIN_SIZE);
	tInfo.iMaxSize = GetDlgItemInt(IDC_EDIT_DELIVERGOODS_MAX_SIZE);
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDIT_DELIVERY_GOODS);
	tInfo.iPointNum =  GetDlgItemInt(IDC_EDIT_DELIVERGOODS_REGION_POINTNUM);
	if(tInfo.iPointNum < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","iPointNum = %d", tInfo.iPointNum);
		return;
	}

	for (int i = 0; i < tInfo.iPointNum && i<MAX_VCA_DELIVERGOODS_POINT_NUM ; i++)
	{
		if(m_tPoints[i].iX < 0 || m_tPoints[i].iY < 0)
		{
			AddLog(LOG_TYPE_FAIL,"","tPoints[i].iX = %d, m_tPoints[i].iY = %d", m_tPoints[i].iX, m_tPoints[i].iY);
			return;
		}
		tInfo.stPoints[i].iX = m_tPoints[i].iX;
		tInfo.stPoints[i].iY = m_tPoints[i].iY;
	}
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_DELIVERGOODS, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_DELIVERGOODS] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_DELIVERGOODS] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaDeliverGoods::OnBnClickedBtnDelivergoodsRegionDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_VCA_DELIVERGOODS_POINT_NUM);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		return;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		m_editRegionPoins.SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDIT_DELIVERGOODS_REGION_POINTNUM, iPointNum);
		vca_TPoint ptPolygon[MAX_VCA_DELIVERGOODS_POINT_NUM] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)ptPolygon);
		for (int i = 0; i < iPointNum && i< MAX_VCA_DELIVERGOODS_POINT_NUM; i++)
		{
			m_tPoints[i].iX = ptPolygon[i].iX;
			m_tPoints[i].iY = ptPolygon[i].iY;
		}
	}
	else
	{
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VcaDeliverGoods::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	// TODO: add message handler code here
	UpdatePageUI();
}

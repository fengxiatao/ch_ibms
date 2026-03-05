// E:\SDK_ALL\trunk\Demo\NetClientDemo\Config\Events\VCAEVENT_Alone.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_Alone.h"

#define  VCA_MAX_REGION_NUM  7
// CLS_VcaAlone dialog

IMPLEMENT_DYNAMIC(CLS_VcaAlone, CDialog)

CLS_VcaAlone::CLS_VcaAlone(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaAlone::IDD, pParent)
{
	memset(&m_tVCAAlone, 0, sizeof(m_tVCAAlone));
}

CLS_VcaAlone::~CLS_VcaAlone()
{
}

void CLS_VcaAlone::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);


	DDX_Control(pDX, IDC_CHECK_ALONE_EVENT_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_CHECK_ALONE_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_ALONE_SHOWNUM, m_chkShowAlarmNum);
	DDX_Control(pDX, IDC_CHECK_ALONE_SHOWTARGET, m_chkShowTargetBox);
	DDX_Control(pDX, IDC_COMBO_ALONE_COLOR, m_cboColor);
	DDX_Control(pDX, IDC_COMBO_ALONE_ALARMCOLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_CBO_ALONE_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_CBO_ALONE_CUR_REGIONNUM, m_cboCurRegionNo);
	DDX_Control(pDX, IDC_EDIT_ALONE_REGION_POINTS, m_editRegionPoins);
}


BEGIN_MESSAGE_MAP(CLS_VcaAlone, CDialog)
	ON_BN_CLICKED(IDC_BTN_ALONE_SET, &CLS_VcaAlone::OnBnClickedBtnAloneSet)
	ON_CBN_SELCHANGE(IDC_CBO_ALONE_CUR_REGIONNUM, &CLS_VcaAlone::OnCbnSelchangeCboAloneCurRegionnum)
	ON_BN_CLICKED(IDC_BTN_ALONE_REGION_DRAW, &CLS_VcaAlone::OnBnClickedBtnAloneRegionDraw)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// CLS_VcaAlone message handler

void CLS_VcaAlone::OnBnClickedBtnAloneSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaAlone::OnBnClickedBtnStrandedSet Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VCAAlone tInfo = {0};
	tInfo.tVCACommonPara.iDevType = m_cboDevType.GetCurSel();
	tInfo.tVCACommonPara.tRule.iSceneID = m_iSceneID;
	tInfo.tVCACommonPara.tRule.iRuleID = m_iRuleID;
	tInfo.tVCACommonPara.tRule.iValid = m_chkEventEnable.GetCheck();
	tInfo.tVCACommonPara.tDisplayParam.iColor = m_cboColor.GetCurSel();
	tInfo.tVCACommonPara.tDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel();
	tInfo.tVCACommonPara.tDisplayParam.iDisplayRule = m_chkShowRule.GetCheck();
	tInfo.tVCACommonPara.tDisplayParam.iDisplayStat = m_chkShowAlarmNum.GetCheck();
	tInfo.tVCACommonPara.iDisplayTarget = m_chkShowTargetBox.GetCheck();
	tInfo.tVCACommonPara.iMinSize = GetDlgItemInt(IDC_EDIT_ALONE_MIN_SIZE);
	tInfo.tVCACommonPara.iMaxSize = GetDlgItemInt(IDC_EDIT_ALONE_MAX_SIZE);
	tInfo.tVCACommonPara.iSensitivity = GetDlgItemInt(IDC_EDIT_ALONE_SENCIVITITY);
	tInfo.iAlarmTime = GetDlgItemInt(IDC_EDIT_ALONE_TIME);
	tInfo.tVCACommonPara.iRegionNum = GetDlgItemInt(IDC_EDIT_ALONE_REGION_NUM);

	for(int i=0;i<MAX_DETECT_AREA_NUM && i< tInfo.tVCACommonPara.iRegionNum;i++)
	{
		tInfo.tVCACommonPara.stPoints[i].iPointNum = m_tVCAAlone.tVCACommonPara.stPoints[i].iPointNum;
		for (int j=0;j<tInfo.tVCACommonPara.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX;j++)
		{
			tInfo.tVCACommonPara.stPoints[i].stPoints[j] = m_tVCAAlone.tVCACommonPara.stPoints[i].stPoints[j];
		}
	}

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_ALONE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_ALONE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_ALONE] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

BOOL CLS_VcaAlone::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  add extra initialization here
	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaAlone::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_LEAVEBED_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_LEAVEBED_SHOWNUM, GetTextByLan("显示报警计数", "Alarm count"));
	SetDlgItemText(IDC_CHECK_LEAVEBED_SHOWTARGET, GetTextByLan("显示目标框", "Display target box"));
	SetDlgItemText(IDC_STC_LEAVEBED_COLOR, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_LEAVEBED_ALARMCOLOR, GetTextByLan("报警区域颜色", "Color of alarm area"));
	SetDlgItemText(IDC_STC_LEAVEBED_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_LEAVEBED_MIN_SIZE, GetTextByLan("最小宽度", "Minimum width"));
	SetDlgItemText(IDC_STC_LEAVEBED_MAX_SIZE, GetTextByLan("最大宽度", "Maximum width"));
	SetDlgItemText(IDC_STC_LEAVEBED_CUR_REGIONNUM, GetTextByLan("当前检测区域号", "Current detection area number"));
	SetDlgItemText(IDC_STC_LEAVEBED_REGION_NUM, GetTextByLan("已绘制区域个数", "The number of areas that have been drawn"));
	SetDlgItemText(IDC_STC_LEAVEBED_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_LEAVEBED_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_LEAVEBED_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_LEAVEBED_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_LEAVEBED_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STC_LEAVEBED_DEVTYPE, GetTextByLan("设备类型", "Device type"));
	SetDlgItemText(IDC_STC_LEAVEBED_LEAVETIME, GetTextByLan("允许离开时间", "Permissible time of departure"));

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
	
}

void CLS_VcaAlone::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAAlone tInfo = {0};
	tInfo.tVCACommonPara.iDevType = m_cboDevType.GetCurSel();
	tInfo.tVCACommonPara.tRule.iSceneID = m_iSceneID;
	tInfo.tVCACommonPara.tRule.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_ALONE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboDevType.SetCurSel(tInfo.tVCACommonPara.iDevType);
		m_chkEventEnable.SetCheck(tInfo.tVCACommonPara.tRule.iValid);
		m_chkShowRule.SetCheck(tInfo.tVCACommonPara.tDisplayParam.iDisplayRule);
		m_chkShowAlarmNum.SetCheck(tInfo.tVCACommonPara.tDisplayParam.iDisplayStat);
		m_cboColor.SetCurSel(tInfo.tVCACommonPara.tDisplayParam.iColor - 1);
		m_cboAlarmColor.SetCurSel(tInfo.tVCACommonPara.tDisplayParam.iAlarmColor - 1);
		m_chkShowTargetBox.SetCheck(tInfo.tVCACommonPara.iDisplayTarget);
		SetDlgItemInt(IDC_EDIT_ALONE_MAX_SIZE, tInfo.tVCACommonPara.iMaxSize);
		SetDlgItemInt(IDC_EDIT_ALONE_MIN_SIZE, tInfo.tVCACommonPara.iMinSize);
		SetDlgItemInt(IDC_EDIT_ALONE_SENCIVITITY, tInfo.tVCACommonPara.iSensitivity);
		SetDlgItemInt(IDC_EDIT_ALONE_TIME, tInfo.iAlarmTime);
		SetDlgItemInt(IDC_EDIT_ALONE_REGION_NUM, tInfo.tVCACommonPara.iRegionNum);
		SetDlgItemInt(IDC_EDIT_ALONE_REGION_POINTNUM, tInfo.tVCACommonPara.stPoints->iPointNum);

		memset(&m_tVCAAlone, 0, sizeof(m_tVCAAlone));
		m_tVCAAlone.tVCACommonPara.iRegionNum = tInfo.tVCACommonPara.iRegionNum;
		for (int i = 0; i < tInfo.tVCACommonPara.iRegionNum && i<MAX_DETECT_AREA_NUM; i++)
		{
			m_tVCAAlone.tVCACommonPara.stPoints[i].iPointNum = tInfo.tVCACommonPara.stPoints[i].iPointNum;
			for(int j = 0; j < tInfo.tVCACommonPara.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_tVCAAlone.tVCACommonPara.stPoints[i].stPoints[j] = tInfo.tVCACommonPara.stPoints[i].stPoints[j];
			}
		}

		m_cboCurRegionNo.SetCurSel(0);
		OnCbnSelchangeCboAloneCurRegionnum();
	}
}


void CLS_VcaAlone::OnCbnSelchangeCboAloneCurRegionnum()
{
	int iRegionNo = m_cboCurRegionNo.GetCurSel();
	int iPiontNum = m_tVCAAlone.tVCACommonPara.stPoints[iRegionNo].iPointNum;
	CString cstPolygonBuf;
	for(int i = 0; i < iPiontNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)", m_tVCAAlone.tVCACommonPara.stPoints[iRegionNo].stPoints[i].iX, m_tVCAAlone.tVCACommonPara.stPoints[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_ALONE_REGION_POINTS, cstPolygonBuf);
}

void CLS_VcaAlone::OnBnClickedBtnAloneRegionDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_VCA_DELIVERGOODS_POINT_NUM);
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
			SetDlgItemInt(IDC_EDIT_ALONE_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_editRegionPoins.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_ALONE_REGION_POINTNUM, 0);
		}

		int iRegionNo = m_cboCurRegionNo.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_ALONE_REGION_POINTNUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_tVCAAlone.tVCACommonPara.stPoints[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<MAX_VCA_DELIVERGOODS_POINT_NUM ; i++)
		{
			m_tVCAAlone.tVCACommonPara.stPoints[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tVCAAlone.tVCACommonPara.stPoints[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
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

void CLS_VcaAlone::UpdateDrawFinishRegionNum()
{
	int iRegionNum = 0;
	for (int i = 0;i<MAX_DETECT_AREA_NUM;i++)
	{
		if (m_tVCAAlone.tVCACommonPara.stPoints[i].iPointNum > 1)
		{
			iRegionNum++;
		}
	}

	SetDlgItemInt(IDC_EDIT_ALONE_REGION_NUM, iRegionNum);
}
void CLS_VcaAlone::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	UpdatePageUI();
	// TODO: add message handler code here
}

// VCAEVENT_Sleep.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_Sleep.h"
#include "../VCAEventPage.h"


// CLS_VCAEVENT_Sleep dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_Sleep, CDialog)

CLS_VCAEVENT_Sleep::CLS_VCAEVENT_Sleep(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_Sleep::IDD, pParent)
{
	memset(&m_tVCAParaSleep, 0, sizeof(m_tVCAParaSleep));
}

CLS_VCAEVENT_Sleep::~CLS_VCAEVENT_Sleep()
{
}

void CLS_VCAEVENT_Sleep::DoDataExchange(CDataExchange* pDX)
{
	CLS_VCAEventBasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SLEEP_COLOR, m_cboColor);
	DDX_Control(pDX, IDC_COMBO_SLEEP_ALARMCOLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_SLIDER_SLEEP_SENSITIVE, m_sldSensitive);
	DDX_Control(pDX, IDC_CBO_SLEEP_CUR_REGIONNUM, m_cboCurRegionNo);
	DDX_Control(pDX, IDC_CHECK_SLEEP_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_SLEEP_SHOWNUM, m_chkShowAlarmNum);
	DDX_Control(pDX, IDC_CHECK_SLEEP_SHOWTARGET, m_chkShowTargetBox);
	DDX_Control(pDX, IDC_EDIT_SLEEP_REGION_POINTS, m_editRegionPoins);
	DDX_Control(pDX, IDC_CHECK_SLEEP_EVENT_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_CBO_SLEEP_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_SLIDER_SLEEP_SLEEPTIME, m_sldSleepTime);
	DDX_Control(pDX, IDC_CBO_SLEEP_CUR_INVALID_REGIONNUM, m_cboCurInvalidRegionNo);
	DDX_Control(pDX, IDC_EDIT_SLEEP_INVALID_REGION_POINTS, m_editInvalidRegionPoins);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_Sleep, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_SLEEP_SET, &CLS_VCAEVENT_Sleep::OnBnClickedBtnSleepSet)
	ON_CBN_SELCHANGE(IDC_CBO_SLEEP_CUR_REGIONNUM, &CLS_VCAEVENT_Sleep::OnCbnSelchangeCboSleepCurRegionnum)
	ON_CBN_SELCHANGE(IDC_CBO_SLEEP_CUR_INVALID_REGIONNUM, &CLS_VCAEVENT_Sleep::OnCbnSelchangeCboSleepCurInvalidRegionnum)
	ON_BN_CLICKED(IDC_BTN_SLEEP_REGION_DRAW, &CLS_VCAEVENT_Sleep::OnBnClickedBtnSleepRegionDraw)
	ON_BN_CLICKED(IDC_BTN_SLEEP_INVALID_REGION_DRAW, &CLS_VCAEVENT_Sleep::OnBnClickedBtnSleepInvalidRegionDraw)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_SLEEP_SENSITIVE, &CLS_VCAEVENT_Sleep::OnNMCustomdrawSliderSleepSensitive)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_SLEEP_SLEEPTIME, &CLS_VCAEVENT_Sleep::OnNMCustomdrawSliderSleepSleeptime)
END_MESSAGE_MAP()


// CLS_VCAEVENT_Sleep message handler


BOOL CLS_VCAEVENT_Sleep::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}


void CLS_VCAEVENT_Sleep::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_SLEEP_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_SLEEP_SHOWNUM, GetTextByLan("显示报警计数", "Alarm count"));
	SetDlgItemText(IDC_CHECK_SLEEP_SHOWTARGET, GetTextByLan("显示目标框", "Display target box"));
	SetDlgItemText(IDC_STC_SLEEP_COLOR, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_SLEEP_ALARMCOLOR, GetTextByLan("报警区域颜色", "Color of alarm area"));
	SetDlgItemText(IDC_STC_SLEEP_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_SLEEP_MIN_SIZE, GetTextByLan("最小宽度", "Minimum width"));
	SetDlgItemText(IDC_STC_SLEEP_MAX_SIZE, GetTextByLan("最大宽度", "Maximum width"));
	SetDlgItemText(IDC_STC_SLEEP_CUR_REGIONNUM, GetTextByLan("当前检测区域号", "Current detection area number"));
	SetDlgItemText(IDC_STC_SLEEP_REGION_NUM, GetTextByLan("已绘制区域个数", "The number of areas that have been drawn"));
	SetDlgItemText(IDC_STC_SLEEP_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_SLEEP_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_SLEEP_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_SLEEP_INVALID_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_SLEEP_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STC_SLEEP_DEVTYPE, GetTextByLan("设备类型", "Device type"));
	SetDlgItemText(IDC_STC_SLEEP_SLEEPTIME, GetTextByLan("允许睡岗时间", "Sleeping time allowed"));
	SetDlgItemText(IDC_STC_SLEEP_CUR_INVALID_REGIONNUM, GetTextByLan("当前无效检测区域号", "Invalid region number currently detected"));
	SetDlgItemText(IDC_STC_SLEEP_INVALID_REGION_NUM, GetTextByLan("已绘制无效区域个数", "Number of invalid regions drawn"));
	SetDlgItemText(IDC_STC_SLEEP_INVALID_REGION_POINTNUM, GetTextByLan("检测无效区域点个数", "Number of invalid region points detected"));

	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), GetTextEx(IDS_VCA_COL_YELLOW), 
		GetTextEx(IDS_VCA_COL_BLUE), GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboColor.ResetContent();
	m_cboAlarmColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboColor.InsertString(i, strColor[i]);
		m_cboAlarmColor.InsertString(i, strColor[i]);
	}

	m_cboCurRegionNo.ResetContent();
	m_cboCurInvalidRegionNo.ResetContent();
	for (int i=0; i<VCA_MAX_REGION_NUM; i++)
	{
		CString cstrRegionNo;
		cstrRegionNo.Format("%d",i+1);
		m_cboCurRegionNo.InsertString(i, cstrRegionNo);
		m_cboCurInvalidRegionNo.InsertString(i, cstrRegionNo);
	}

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, "IPC");
	m_cboDevType.InsertString(1, "NVR");

	m_sldSensitive.SetRange(0,100);
	m_sldSleepTime.SetRange(1,3600);

}


void CLS_VCAEVENT_Sleep::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAParaSleep tInfo = {0};
	tInfo.tCommonPara.iDevType = 0;
	tInfo.tCommonPara.tRule.iSceneID = m_iSceneID;
	tInfo.tCommonPara.tRule.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SLEEP_POSTION, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboDevType.SetCurSel(tInfo.tCommonPara.iDevType);
		m_chkEventEnable.SetCheck(tInfo.tCommonPara.tRule.iValid);
		m_chkShowRule.SetCheck(tInfo.tCommonPara.tDisplayParam.iDisplayRule);
		m_chkShowAlarmNum.SetCheck(tInfo.tCommonPara.tDisplayParam.iDisplayStat);
		m_cboColor.SetCurSel(tInfo.tCommonPara.tDisplayParam.iColor - 1);
		m_cboAlarmColor.SetCurSel(tInfo.tCommonPara.tDisplayParam.iAlarmColor - 1);
		m_chkShowTargetBox.SetCheck(tInfo.tCommonPara.iDisplayTarget);
		SetDlgItemInt(IDC_EDIT_SLEEP_MAX_SIZE, tInfo.tCommonPara.iMaxSize);
		SetDlgItemInt(IDC_EDIT_SLEEP_MIN_SIZE, tInfo.tCommonPara.iMinSize);
		m_sldSensitive.SetPos(tInfo.tCommonPara.iSensitivity);
		SetDlgItemInt(IDC_STATIC_SLEEP_SENSITIVE_NUM, m_sldSensitive.GetPos());
		m_sldSleepTime.SetPos(tInfo.iSleepTime);
		SetDlgItemInt(IDC_STC_SLEEP_SLEEPTIME_NUM, m_sldSleepTime.GetPos());
		SetDlgItemInt(IDC_EDIT_SLEEP_REGION_NUM, tInfo.tCommonPara.iRegionNum);

		memset(&m_tVCAParaSleep, 0, sizeof(m_tVCAParaSleep));
		m_tVCAParaSleep.tCommonPara.iRegionNum = tInfo.tCommonPara.iRegionNum;
		for (int i = 0; i < tInfo.tCommonPara.iRegionNum && i<MAX_DETECT_AREA_NUM; i++)
		{
			m_tVCAParaSleep.tCommonPara.stPoints[i].iPointNum = tInfo.tCommonPara.stPoints[i].iPointNum;
			for(int j = 0; j < tInfo.tCommonPara.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_tVCAParaSleep.tCommonPara.stPoints[i].stPoints[j] = tInfo.tCommonPara.stPoints[i].stPoints[j];
			}
		}

		m_cboCurRegionNo.SetCurSel(0);
		OnCbnSelchangeCboSleepCurRegionnum();

		SetDlgItemInt(IDC_EDIT_SLEEP_INVALID_REGION_NUM, tInfo.iInvalidRegionNum);
		m_tVCAParaSleep.iInvalidRegionNum = tInfo.iInvalidRegionNum;
		for (int i = 0; i < tInfo.iInvalidRegionNum && i<MAX_DETECT_AREA_NUM; i++)
		{
			m_tVCAParaSleep.stInvalidPoints[i].iPointNum = tInfo.stInvalidPoints[i].iPointNum;
			for(int j = 0; j < tInfo.stInvalidPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_tVCAParaSleep.stInvalidPoints[i].stPoints[j] = tInfo.stInvalidPoints[i].stPoints[j];
			}
		}
		
		m_cboCurInvalidRegionNo.SetCurSel(0);
		OnCbnSelchangeCboSleepCurInvalidRegionnum();
	}

}
void CLS_VCAEVENT_Sleep::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_Sleep::OnBnClickedBtnSleepSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_Sleep::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VCAParaSleep tInfo = {0};
	tInfo.tCommonPara.iDevType = m_cboDevType.GetCurSel();
	tInfo.tCommonPara.tRule.iSceneID = m_iSceneID;
	tInfo.tCommonPara.tRule.iRuleID = m_iRuleID;
	tInfo.tCommonPara.tRule.iValid = m_chkEventEnable.GetCheck();
	tInfo.tCommonPara.tDisplayParam.iDisplayRule = m_chkShowRule.GetCheck();
	tInfo.tCommonPara.tDisplayParam.iDisplayStat = m_chkShowAlarmNum.GetCheck();
	tInfo.tCommonPara.tDisplayParam.iColor = m_cboColor.GetCurSel() + 1;
	tInfo.tCommonPara.tDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel() + 1;
	tInfo.tCommonPara.iDisplayTarget = m_chkShowTargetBox.GetCheck();
	tInfo.tCommonPara.iMaxSize = GetDlgItemInt(IDC_EDIT_SLEEP_MAX_SIZE);
	tInfo.tCommonPara.iMinSize = GetDlgItemInt(IDC_EDIT_SLEEP_MIN_SIZE);
	tInfo.tCommonPara.iSensitivity = m_sldSensitive.GetPos();
	tInfo.iSleepTime = m_sldSleepTime.GetPos();
	tInfo.tCommonPara.iRegionNum = GetDlgItemInt(IDC_EDIT_SLEEP_REGION_NUM);

	for(int i=0;i<MAX_DETECT_AREA_NUM && i< tInfo.tCommonPara.iRegionNum;i++)
	{
		tInfo.tCommonPara.stPoints[i].iPointNum = m_tVCAParaSleep.tCommonPara.stPoints[i].iPointNum;
		for (int j=0;j<tInfo.tCommonPara.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX;j++)
		{
			tInfo.tCommonPara.stPoints[i].stPoints[j] = m_tVCAParaSleep.tCommonPara.stPoints[i].stPoints[j];
		}
	}

	tInfo.iInvalidRegionNum = GetDlgItemInt(IDC_EDIT_SLEEP_INVALID_REGION_NUM);
	
	for(int i=0;i<MAX_DETECT_AREA_NUM && i< tInfo.iInvalidRegionNum;i++)
	{
		tInfo.stInvalidPoints[i].iPointNum = m_tVCAParaSleep.stInvalidPoints[i].iPointNum;
		for (int j=0;j<tInfo.stInvalidPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX;j++)
		{
			tInfo.stInvalidPoints[i].stPoints[j] = m_tVCAParaSleep.stInvalidPoints[i].stPoints[j];
		}
	}

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SLEEP_POSTION, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_Sleep::NetClient_VCASetConfig[VCA_CMD_SLEEP_POSTION] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_Sleep::NetClient_VCASetConfig[VCA_CMD_GET_UP] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_Sleep::OnCbnSelchangeCboSleepCurRegionnum()
{
	int iRegionNo = m_cboCurRegionNo.GetCurSel();
	int iPiontNum = m_tVCAParaSleep.tCommonPara.stPoints[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_SLEEP_REGION_POINTNUM, iPiontNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPiontNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)", m_tVCAParaSleep.tCommonPara.stPoints[iRegionNo].stPoints[i].iX, m_tVCAParaSleep.tCommonPara.stPoints[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_SLEEP_REGION_POINTS, cstPolygonBuf);
}

void CLS_VCAEVENT_Sleep::OnCbnSelchangeCboSleepCurInvalidRegionnum()
{
	int iRegionNo = m_cboCurInvalidRegionNo.GetCurSel();
	int iPiontNum = m_tVCAParaSleep.stInvalidPoints[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_SLEEP_INVALID_REGION_POINTNUM, iPiontNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPiontNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)", m_tVCAParaSleep.stInvalidPoints[iRegionNo].stPoints[i].iX, m_tVCAParaSleep.stInvalidPoints[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_SLEEP_INVALID_REGION_POINTS, cstPolygonBuf);
}

void CLS_VCAEVENT_Sleep::OnBnClickedBtnSleepRegionDraw()
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
			SetDlgItemInt(IDC_EDIT_SLEEP_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_editRegionPoins.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_SLEEP_REGION_POINTNUM, 0);
		}

		int iRegionNo = m_cboCurRegionNo.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_SLEEP_REGION_POINTNUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_tVCAParaSleep.tCommonPara.stPoints[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_tVCAParaSleep.tCommonPara.stPoints[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tVCAParaSleep.tCommonPara.stPoints[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
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

void CLS_VCAEVENT_Sleep::OnBnClickedBtnSleepInvalidRegionDraw()
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
			m_editInvalidRegionPoins.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_SLEEP_INVALID_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_editInvalidRegionPoins.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_SLEEP_INVALID_REGION_POINTNUM, 0);
		}

		int iRegionNo = m_cboCurInvalidRegionNo.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_SLEEP_INVALID_REGION_POINTNUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_editInvalidRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_tVCAParaSleep.stInvalidPoints[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_tVCAParaSleep.stInvalidPoints[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tVCAParaSleep.stInvalidPoints[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
		}

		UpdateDrawFinishInvalidRegionNum();
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_Sleep::UpdateDrawFinishRegionNum()
{
	int iRegionNum = 0;
	for (int i = 0;i<MAX_DETECT_AREA_NUM;i++)
	{
		if (m_tVCAParaSleep.tCommonPara.stPoints[i].iPointNum > 1)
		{
			iRegionNum++;
		}
	}

	SetDlgItemInt(IDC_EDIT_SLEEP_REGION_NUM, iRegionNum);
}

void CLS_VCAEVENT_Sleep::UpdateDrawFinishInvalidRegionNum()
{
	int iRegionNum = 0;
	for (int i = 0;i<MAX_DETECT_AREA_NUM;i++)
	{
		if (m_tVCAParaSleep.stInvalidPoints[i].iPointNum > 1)
		{
			iRegionNum++;
		}
	}

	SetDlgItemInt(IDC_EDIT_SLEEP_INVALID_REGION_NUM, iRegionNum);
}
void CLS_VCAEVENT_Sleep::OnNMCustomdrawSliderSleepSensitive(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_SLEEP_SENSITIVE_NUM, m_sldSensitive.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Sleep::OnNMCustomdrawSliderSleepSleeptime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_SLEEP_SLEEPTIME_NUM, m_sldSleepTime.GetPos());
	*pResult = 0;
}

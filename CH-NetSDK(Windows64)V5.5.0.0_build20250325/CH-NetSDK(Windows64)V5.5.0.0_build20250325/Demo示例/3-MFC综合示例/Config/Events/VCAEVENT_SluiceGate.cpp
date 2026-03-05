// VCAEVENT_SluiceGate.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_SluiceGate.h"


// CLS_VCAEVENT_SluiceGate dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_SluiceGate, CDialog)

CLS_VCAEVENT_SluiceGate::CLS_VCAEVENT_SluiceGate(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_SluiceGate::IDD, pParent)
{
	memset(m_iPointCount, 0, sizeof(int) * INNER_MAX_VCA_SLUICEGATE_COUNT);
	memset(m_iPointCountLine, 0, sizeof(int) * INNER_MAX_VCA_SLUICEGATE_LINE_COUNT);
}

CLS_VCAEVENT_SluiceGate::~CLS_VCAEVENT_SluiceGate()
{
}

void CLS_VCAEVENT_SluiceGate::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHK_SLUICEGATE_ALARM_RULES, m_chkDisplayRule);
	DDX_Control(pDX, IDC_CHK_SLUICEGATE_ALARM_STATISTICS, m_chkDisplayAlarmCount);
	DDX_Control(pDX, IDC_CBO_SLUICEGATE_ALARM_COLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_CBO_SLUICEGATE_UNALARM_COLOR, m_cboAreaColor);
	DDX_Control(pDX, IDC_EDT_SLUICEGATE_TYPE, m_cboSluiceGateType);
	DDX_Control(pDX, IDC_EDT_SLUICEGATE_COUNT, m_edtSluiceGateCount);
	DDX_Control(pDX, IDC_EDT_SLUICEGATE1, m_edtSluiceGate1);
	DDX_Control(pDX, IDC_EDT_SLUICEGATE2, m_edtSluiceGate2);
	
	DDX_Control(pDX, IDC_EDT_SLUICEGATE1_LINE1, m_edtSluiceGate1Line1);
	DDX_Control(pDX, IDC_EDT_SLUICEGATE1_LINE2, m_edtSluiceGate1Line2);
	DDX_Control(pDX, IDC_EDT_SLUICEGATE2_LINE1, m_edtSluiceGate2Line1);
	DDX_Control(pDX, IDC_EDT_SLUICEGATE2_LINE2, m_edtSluiceGate2Line2);
	
	DDX_Control(pDX, IDC_SPIN_SLUICEGATE_COUNT, m_spinSluiceGateCount);
	DDX_Control(pDX, IDC_CHECK_SLUICEGATE1, m_chkSluiceGate1);
	DDX_Control(pDX, IDC_CHECK_SLUICEGATE2, m_chkSluiceGate2);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_SluiceGate, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_DRAW_SLUICEGATE1, &CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluiceGate1)
	ON_BN_CLICKED(IDC_BTN_DRAW_SLUICEGATE2, &CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluiceGate2)
	ON_BN_CLICKED(IDC_BTN_DRAW_SLUICEGATE1_LINE1, &CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate1Line1)
	ON_BN_CLICKED(IDC_BTN_DRAW_SLUICEGATE1_LINE2, &CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate1Line2)
	ON_BN_CLICKED(IDC_BTN_DRAW_SLUICEGATE2_LINE1, &CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate2Line1)
	ON_BN_CLICKED(IDC_BTN_DRAW_SLUICEGATE2_LINE2, &CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate2Line2)
	ON_BN_CLICKED(IDC_BTN_SLUICEGATE_SET, &CLS_VCAEVENT_SluiceGate::OnBnClickedBtnSluicegateSet)
	ON_EN_CHANGE(IDC_EDT_SLUICEGATE_COUNT, &CLS_VCAEVENT_SluiceGate::OnEnChangeEdtSluicegateCount)
	
END_MESSAGE_MAP()


// CLS_VCAEVENT_SluiceGate message handlers

BOOL CLS_VCAEVENT_SluiceGate::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UI_UpdateDialog();
	m_edtSluiceGateCount.SetLimitText(LEN_16);
	m_edtSluiceGate1.SetLimitText(LEN_256);
	m_edtSluiceGate2.SetLimitText(LEN_256);
	m_edtSluiceGate1Line1.SetLimitText(LEN_256);
	m_edtSluiceGate1Line2.SetLimitText(LEN_256);
	m_edtSluiceGate2Line1.SetLimitText(LEN_256);
	m_edtSluiceGate2Line2.SetLimitText(LEN_256);

	m_spinSluiceGateCount.SetRange(0, INNER_MAX_VCA_SLUICEGATE_COUNT);
	m_spinSluiceGateCount.SetBuddy(this->GetDlgItem(IDC_EDT_SLUICEGATE_COUNT));

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_VCAEVENT_SluiceGate::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		JudgeVCAStatus();
		UI_UpdateSdkParam();
	}
}

void CLS_VCAEVENT_SluiceGate::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_CHK_SLUICEGATE_ALARM_RULES, IDS_VCA_DISPLAY_RULE);
	SetDlgItemTextEx(IDC_CHK_SLUICEGATE_ALARM_STATISTICS, IDS_VCA_DISPLAY_ALARMSTATUS);
	SetDlgItemTextEx(IDC_STC_SLUICEGATE_ALARM_COLOR, IDS_VCA_ALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_SLUICEGATE_UNALARM_COLOR, IDS_VCA_NOALARM_COLOR);
	SetDlgItemTextEx(IDC_BTN_SLUICEGATE_SET, IDS_SET);
	SetDlgItemTextEx(IDC_BTN_DRAW_SLUICEGATE1, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_DRAW_SLUICEGATE2, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_DRAW_SLUICEGATE1_LINE1, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_DRAW_SLUICEGATE1_LINE2, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_DRAW_SLUICEGATE2_LINE1, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_DRAW_SLUICEGATE2_LINE2, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_STC_SLUICEGATE_TYPE, IDS_VCA_EVENT_SLUICEGATE_YPE);
	SetDlgItemTextEx(IDC_STC_SLUICEGATE_COUNT, IDS_VCA_EVENT_SLUICEGATE_COUNT);
	SetDlgItemText(IDC_STC_SLUICEGATE1, GetTextEx(IDS_VCA_EVENT_SLUICEGATE) + "1");
	SetDlgItemText(IDC_STC_SLUICEGATE2, GetTextEx(IDS_VCA_EVENT_SLUICEGATE) + "2");

	SetDlgItemTextEx(IDC_CHECK_SLUICEGATE1, IDS_CONFIG_OPEN);
	SetDlgItemTextEx(IDC_CHECK_SLUICEGATE2, IDS_CONFIG_OPEN);


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

	CString cstrSluiceGateType[] = {GetTextEx(IDS_VCA_EVENT_SLUICEGATE_INVISIBLE), GetTextEx(IDS_VCA_EVENT_SLUICEGATE_VISIBLE)};
	m_cboSluiceGateType.ResetContent();
	for (int i = 0 ; i < sizeof(cstrSluiceGateType)/sizeof(CString); i++)
	{
		m_cboSluiceGateType.InsertString(i, cstrSluiceGateType[i]);
	}
	m_cboSluiceGateType.SetCurSel(0);
}

void CLS_VCAEVENT_SluiceGate::UI_UpdateSdkParam()
{
	if (!CheckPublicPara())
	{
		return;
	}

	VcaSluiceGate tSluiceGate = {0};
	tSluiceGate.iSize = sizeof(VcaSluiceGate);
	tSluiceGate.tRule.iRuleID = m_iRuleID;
	tSluiceGate.tRule.iSceneID = m_iSceneID;
	int iCmd = VCA_CMD_SLUICEGATE;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, iCmd, m_iChannelNO, &tSluiceGate, sizeof(VcaSluiceGate));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCAGetConfig]VCA_CMD_SLUICEGATE fail!");
		return;
	}

	m_chkDisplayRule.SetCheck(tSluiceGate.tDisplayParam.iDisplayRule);
	m_chkDisplayAlarmCount.SetCheck(tSluiceGate.tDisplayParam.iDisplayStat);
	if (0 == tSluiceGate.tDisplayParam.iColor)
	{
		m_cboAreaColor.SetCurSel(1); //The variable is 0, which is considered to be the default, and is directly set to green
	}
	else 
	{
		m_cboAreaColor.SetCurSel(tSluiceGate.tDisplayParam.iColor - 1);
	}
	if (0 == tSluiceGate.tDisplayParam.iAlarmColor)
	{
		m_cboAlarmColor.SetCurSel(0);//The variable is 0, which is considered to be the default, and is directly set to green
	}
	else
	{
		m_cboAlarmColor.SetCurSel(tSluiceGate.tDisplayParam.iAlarmColor - 1);
	}
	
	m_cboSluiceGateType.SetCurSel(tSluiceGate.iSluiceGateType);
	SetDlgItemInt(IDC_EDT_SLUICEGATE_COUNT, tSluiceGate.iSluiceGateCount);
	m_chkSluiceGate1.SetCheck(tSluiceGate.tSluiceGateArr[0].iSluiceGateState);
	m_chkSluiceGate2.SetCheck(tSluiceGate.tSluiceGateArr[1].iSluiceGateState);

	CString cstrPointArray[INNER_MAX_VCA_SLUICEGATE_COUNT];
	CString cstrPoint[INNER_MAX_VCA_SLUICEGATE_COUNT][MAX_SLUICEGATE_POINT_COUNT];
	for (int i = 0;  i < INNER_MAX_VCA_SLUICEGATE_COUNT; ++i)
	{
		for (int j = 0; j < tSluiceGate.tSluiceGateArr[i].iPointCount && j < MAX_SLUICEGATE_POINT_COUNT; ++j)
		{
			cstrPoint[i][j].Format("(%d,%d)", tSluiceGate.tSluiceGateArr[i].tPointArr[j].iX, tSluiceGate.tSluiceGateArr[i].tPointArr[j].iY);
			cstrPointArray[i] += cstrPoint[i][j];
		}
		GetDlgItem(IDC_EDT_SLUICEGATE1 + i)->SetWindowText(cstrPointArray[i]);
	}

	VcaSluiceGateCeil tCeil = {0};
	tCeil.iSize = sizeof(tCeil);
	tCeil.tRule.iRuleID = m_iRuleID;
	tCeil.tRule.iSceneID = m_iSceneID;

	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SLUICEGATE_PART2, m_iChannelNO, &tCeil, sizeof(tCeil));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCAGetConfig]VCA_CMD_SLUICEGATE fail!");
		return;
	}

	for (int i = 0;  i <tCeil.iCeilLineCount && i < INNER_MAX_VCA_SLUICEGATE_LINE_COUNT; ++i)
	{
		CString szPointBuf = "";
		szPointBuf.Format("(%d,%d)(%d,%d)", tCeil.tCeilLineInfo[i].tPointStart.iX, tCeil.tCeilLineInfo[i].tPointStart.iY, tCeil.tCeilLineInfo[i].tPointEnd.iX, tCeil.tCeilLineInfo[i].tPointEnd.iY);

		GetDlgItem(IDC_EDT_SLUICEGATE1_LINE1 + i)->SetWindowText(szPointBuf);
	}
}

void CLS_VCAEVENT_SluiceGate::OnLanguageChanged()
{
	UI_UpdateDialog();
}

void CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluiceGate1()
{
	DrawOnVideo(m_edtSluiceGate1, &m_iPointCount[0]);
}

void CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluiceGate2()
{
	DrawOnVideo(m_edtSluiceGate2, &m_iPointCount[1]);
}

void CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate1Line1()
{
	DrawOnVideo(m_edtSluiceGate1Line1, &m_iPointCountLine[0], DrawType_tripwire);
}

void CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate1Line2()
{
	DrawOnVideo(m_edtSluiceGate1Line2, &m_iPointCountLine[1], DrawType_tripwire);
}

void CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate2Line1()
{
	DrawOnVideo(m_edtSluiceGate2Line1, &m_iPointCountLine[2], DrawType_tripwire);
}

void CLS_VCAEVENT_SluiceGate::OnBnClickedBtnDrawSluicegate2Line2()
{
	DrawOnVideo(m_edtSluiceGate2Line2, &m_iPointCountLine[3], DrawType_tripwire);
}

void CLS_VCAEVENT_SluiceGate::DrawOnVideo(CEdit& _edtSluiceGate, int* _piPointCount, int iDrawType)
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
	m_pDlgVideoView->SetDrawType(iDrawType);
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, _piPointCount, &iDirection);
	if (-1 == iSetRet)
	{
		return;
	}

	if (IDOK == m_pDlgVideoView->DoModal())
	{
		_edtSluiceGate.SetWindowText(cPointBuf);
	}

	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_SluiceGate::OnBnClickedBtnSluicegateSet()
{
	if (!CheckPublicPara())
	{
		return;
	}

	int iSluiceGateCount = GetDlgItemInt(IDC_EDT_SLUICEGATE_COUNT);
	if (iSluiceGateCount < 0 || iSluiceGateCount > INNER_MAX_VCA_SLUICEGATE_COUNT)
	{
		MessageBox(GetTextEx(IDS_VCAEVENT_SGW_USR_MSG), GetTextEx(IDS_CONFIG_PROMPT), MB_OK|MB_TOPMOST);
		return;
	}

	VcaSluiceGate tSluiceGate = {0};
	tSluiceGate.iSize = sizeof(VcaSluiceGate);
	tSluiceGate.tRule.iRuleID = m_iRuleID;
	tSluiceGate.tRule.iSceneID = m_iSceneID;
	tSluiceGate.tDisplayParam.iDisplayRule = m_chkDisplayRule.GetCheck();
	tSluiceGate.tDisplayParam.iDisplayStat = m_chkDisplayAlarmCount.GetCheck();
	tSluiceGate.tDisplayParam.iColor = m_cboAreaColor.GetCurSel() + 1;
	tSluiceGate.tDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel() + 1;
	tSluiceGate.iSluiceGateType = m_cboSluiceGateType.GetCurSel();
	tSluiceGate.iSluiceGateCount = iSluiceGateCount;
	tSluiceGate.tSluiceGateArr[0].iSluiceGateState = m_chkSluiceGate1.GetCheck();
	tSluiceGate.tSluiceGateArr[1].iSluiceGateState = m_chkSluiceGate2.GetCheck();
	CString cstrPointArray[INNER_MAX_VCA_SLUICEGATE_COUNT];
	vca_TPolygon tRegion[INNER_MAX_VCA_SLUICEGATE_COUNT] = {0};
	for (int i = 0;  i < tSluiceGate.iSluiceGateCount && i < INNER_MAX_VCA_SLUICEGATE_COUNT; ++i)
	{
		GetDlgItemText(IDC_EDT_SLUICEGATE1 + i, cstrPointArray[i]);
		GetPolyFromString(cstrPointArray[i], m_iPointCount[i], tRegion[i]);
		tSluiceGate.tSluiceGateArr[i].iPointCount = tRegion[i].iPointNum;
		memcpy(&tSluiceGate.tSluiceGateArr[i].tPointArr, &tRegion[i].stPoints
		, min(tRegion[i].iPointNum, MAX_SLUICEGATE_POINT_COUNT) * sizeof(vca_TPoint));
	}

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SLUICEGATE, m_iChannelNO, &tSluiceGate, sizeof(VcaSluiceGate));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCASetConfig]VCA_CMD_SLUICEGATE fail!");
		return;
	}
	AddLog(LOG_TYPE_SUCC, "", "[NetClient_VCASetConfig]VCA_CMD_SLUICEGATE success!");


	VcaSluiceGateCeil tCeil = {0};
	tCeil.iSize = sizeof(VcaSluiceGate);
	tCeil.tRule.iRuleID = m_iRuleID;
	tCeil.tRule.iSceneID = m_iSceneID;
	tCeil.iCeilLineCount = INNER_MAX_VCA_SLUICEGATE_LINE_COUNT;
	
	for (int i = 0;  i < tCeil.iCeilLineCount && i < INNER_MAX_VCA_SLUICEGATE_LINE_COUNT; ++i)
	{
		CString strPointStr = "";
		GetDlgItemText(IDC_EDT_SLUICEGATE1_LINE1 + i, strPointStr);
		vca_TLine tLine = {0};
		GetLineFromString(strPointStr, tLine);
		tCeil.tCeilLineInfo[i].tPointStart = tLine.stStart; 
		tCeil.tCeilLineInfo[i].tPointEnd = tLine.stEnd; 
	}

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SLUICEGATE_PART2, m_iChannelNO, &tCeil, sizeof(tCeil));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCASetConfig]VCA_CMD_SLUICEGATE_PART2 fail!");
		return;
	}
	AddLog(LOG_TYPE_SUCC, "", "[NetClient_VCASetConfig]VCA_CMD_SLUICEGATE_PART2 succ!");
}

void CLS_VCAEVENT_SluiceGate::OnEnChangeEdtSluicegateCount()
{
	int iCount = GetDlgItemInt(IDC_EDT_SLUICEGATE_COUNT);
	if (iCount > INNER_MAX_VCA_SLUICEGATE_COUNT)
	{
		MessageBox(GetTextEx(IDS_VCAEVENT_SGW_USR_MSG), GetTextEx(IDS_CONFIG_PROMPT), MB_OK|MB_TOPMOST);
		SetDlgItemInt(IDC_EDT_SLUICEGATE_COUNT, 0);
	}
}

void CLS_VCAEVENT_SluiceGate::JudgeVCASetState(BOOL _bIsVcaStatus)
{
	GetDlgItem(IDC_BTN_SLUICEGATE_SET)->EnableWindow(_bIsVcaStatus);
}


void CLS_VCAEVENT_SluiceGate::JudgeVCAStatus()
{
	VCASuspendResult tParam = {0};
	tParam.iBufSize = sizeof(VCASuspendResult);
	int iRetBytes = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &tParam, sizeof(tParam), &iRetBytes);
	if(0 == tParam.iStatus && 2 == tParam.iResult)
	{
		GetDlgItem(IDC_BTN_SLUICEGATE_SET)->EnableWindow(FALSE);
	}
	else if (0 == tParam.iStatus && 1 == tParam.iResult)//Paused successfully
	{
		GetDlgItem(IDC_BTN_SLUICEGATE_SET)->EnableWindow(TRUE);
	}
}

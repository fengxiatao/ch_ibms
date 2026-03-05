// VCAEVENT_CrowdNew.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_CrowdNew.h"

#define POINT_SUM			4		//Number of area vertices
#define LEN_3				3		//length
#define SENSI_MIN			0		//minimum sensitivity
#define SENSI_MAX			100		//Maximum sensitivity


// CLS_VCAEVENT_CrowdNew dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_CrowdNew, CDialog)

CLS_VCAEVENT_CrowdNew::CLS_VCAEVENT_CrowdNew(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_CrowdNew::IDD, pParent)
{

}

CLS_VCAEVENT_CrowdNew::~CLS_VCAEVENT_CrowdNew()
{
}

void CLS_VCAEVENT_CrowdNew::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHK_CROWD_NEW_EVENT_VALID, m_chkEventValid);
	DDX_Control(pDX, IDC_CHK_CROWD_NEW_SHOW_ALARM_RULE, m_chkAlarmRule);
	DDX_Control(pDX, IDC_CHK_CROWD_NEW_SHOW_ALARM_STAT, m_chkAlarmStat);
	DDX_Control(pDX, IDC_CHK_CROWD_NEW_SHOW_TARGET_BOX, m_chkTargetBox);
	DDX_Control(pDX, IDC_EDT_CROWD_NEW_SENSITIVITY, m_edtSensitivity);
	DDX_Control(pDX, IDC_EDT_CROWD_NEW_SUSTAIN_TIME, m_edtSustainTime);
	DDX_Control(pDX, IDC_CBO_CROWD_NEW_ALARM_COLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_CBO_CROWD_NEW_UNALARM_COLOR, m_cboUnalarmColor);
	DDX_Control(pDX, IDC_EDT_CROWD_NEW_AREA, m_edtArea);
	DDX_Control(pDX, IDC_EDT_CROWD_NEW_BASICVALUE, m_edtBasicValue);
	DDX_Control(pDX, IDC_CBO_CROWD_NEW_SCENEMODE, m_cboSceneMode);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_CrowdNew, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_CROWD_NEW_SET, &CLS_VCAEVENT_CrowdNew::OnBnClickedBtnCrowdNewSet)
	ON_BN_CLICKED(IDC_BTN_CROWD_NEW_DRAW, &CLS_VCAEVENT_CrowdNew::OnBnClickedBtnCrowdNewDraw)
	ON_EN_CHANGE(IDC_EDT_CROWD_NEW_SENSITIBITY, &CLS_VCAEVENT_CrowdNew::OnEnChangeEdtCrowdNewSensitibity)
END_MESSAGE_MAP()


// CLS_VCAEVENT_CrowdNew message handler

void CLS_VCAEVENT_CrowdNew::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

BOOL CLS_VCAEVENT_CrowdNew::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	GetDlgItem(IDC_CBO_CROWD_NEW_ALARM_COLOR)->EnableWindow(FALSE);
	GetDlgItem(IDC_CBO_CROWD_NEW_UNALARM_COLOR)->EnableWindow(FALSE);

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}


void CLS_VCAEVENT_CrowdNew::OnLanguageChanged()
{	
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAEVENT_CrowdNew::UpdateUIText()
{
	SetDlgItemTextEx(IDC_CHK_CROWD_NEW_EVENT_VALID, IDS_VCAEVENT_EVENT_VALID);
	SetDlgItemTextEx(IDC_CHK_CROWD_NEW_SHOW_ALARM_RULE, IDS_VCAEVENT_SHOW_ALARM_RULE);
	SetDlgItemTextEx(IDC_CHK_CROWD_NEW_SHOW_ALARM_STAT, IDS_VCAEVENT_SHOW_ALARM_STATISTICS);
	SetDlgItemTextEx(IDC_STC_CROWD_NEW_SENSITIVITY, IDS_CONFIG_ITS_ILLEGALPARK_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_CROWD_NEW_SUSTAIN_TIME, IDS_CONFIG_DH_SUSTAIN_TIME);
	SetDlgItemTextEx(IDC_STC_CROWD_NEW_ALARM_COLOR, IDS_VCA_ALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_CROWD_NEW_UNALARM_COLOR, IDS_VCA_NOALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_CROWD_NEW_AREA, IDS_CONFIG_ITS_ILLEGALPARK_AREA);
	SetDlgItemTextEx(IDC_BTN_CROWD_NEW_DRAW, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_CROWD_NEW_SET, IDS_SET);
	SetDlgItemText(IDC_STC_CROWD_NEW_BASICVALUE, GetTextByLan(_T("密度基准值"), _T("Density Reference Value")));
	SetDlgItemText(IDC_STC_CROWD_NEW_SCENEMODE, GetTextByLan(_T("大小场景值"), _T("Scene Mode Value")));

	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), 
		GetTextEx(IDS_VCA_COL_YELLOW), GetTextEx(IDS_VCA_COL_BLUE), 
		GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), 
		GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboAlarmColor.ResetContent();
	m_cboUnalarmColor.ResetContent();
	m_cboSceneMode.ResetContent();
	for (int i = 0; i < sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboAlarmColor.InsertString(i, strColor[i]);
		m_cboUnalarmColor.InsertString(i, strColor[i]);
	}
	m_cboSceneMode.SetItemData(0, m_cboSceneMode.AddString(GetTextByLan(_T("不支持"), _T("UNsupport"))));
	m_cboSceneMode.SetItemData(1, m_cboSceneMode.AddString(GetTextByLan(_T("大场景"), _T("Big scene"))));
	m_cboSceneMode.SetItemData(2, m_cboSceneMode.AddString(GetTextByLan(_T("小场景"), _T("Small scene"))));

	m_cboAlarmColor.SetCurSel(0);
	m_cboUnalarmColor.SetCurSel(0);
	m_cboSceneMode.SetCurSel(1);

	m_edtSensitivity.SetLimitText(LEN_3);
	m_edtSustainTime.SetLimitText(LEN_32 - 1);
	m_edtArea.SetLimitText(LEN_32);
}

void CLS_VCAEVENT_CrowdNew::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_FaceRecNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VCACrowd vc = {0};

	vc.stRule.iRuleID = m_iRuleID;
	vc.stRule.iSceneID = m_iSceneID;
	vc.iSceneMode = m_cboSceneMode.GetCurSel();

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CROWD, m_iChannelNO, &vc, sizeof(VCACrowd));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_CrowdNew::NetClient_VCAGetConfig[VCA_CMD_CROWD] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_chkEventValid.SetCheck(vc.stRule.iValid);
		m_chkAlarmRule.SetCheck(vc.stDisplayParam.iDisplayRule);
		m_chkAlarmStat.SetCheck(vc.stDisplayParam.iDisplayStat);

		m_cboAlarmColor.SetCurSel(vc.stDisplayParam.iAlarmColor);
		m_cboUnalarmColor.SetCurSel(vc.stDisplayParam.iColor);
		m_cboSceneMode.SetCurSel(vc.iSceneMode);

		SetDlgItemInt(IDC_EDT_CROWD_NEW_SENSITIBITY, vc.iSensitivity);
		SetDlgItemInt(IDC_EDT_CROWD_NEW_SUSTAIN_TIME, vc.iTimes);
		SetDlgItemInt(IDC_EDT_CROWD_NEW_BASICVALUE, vc.iBasicValue);

		CString cstPointBuf = "";
		for (int i = 0; i < POINT_SUM; i++)
		{
			cstPointBuf.AppendFormat("(%d,%d)", vc.stRegion.stPoints[i].iX, vc.stRegion.stPoints[i].iY);
		}
		m_edtArea.SetWindowText(cstPointBuf);

		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_CrowdNew::NetClient_VCAGetConfig[VCA_CMD_CROWD] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
} 


void CLS_VCAEVENT_CrowdNew::OnBnClickedBtnCrowdNewSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_FaceRecNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VCACrowd vc = {0};

	vc.iBufSize = sizeof(VCACrowd);
	vc.iSensitivity = GetDlgItemInt(IDC_EDT_CROWD_NEW_SENSITIBITY);
	vc.iTimes = GetDlgItemInt(IDC_EDT_CROWD_NEW_SUSTAIN_TIME);
	vc.iBasicValue = GetDlgItemInt(IDC_EDT_CROWD_NEW_BASICVALUE);

	vc.stDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel();
	vc.stDisplayParam.iColor = m_cboUnalarmColor.GetCurSel();
	vc.stDisplayParam.iDisplayRule = m_chkAlarmRule.GetCheck();
	vc.stDisplayParam.iDisplayStat = m_chkAlarmStat.GetCheck();
	vc.iSceneMode = m_cboSceneMode.GetCurSel();

	vc.stRule.iRuleID = m_iRuleID;
	vc.stRule.iSceneID = m_iSceneID;
	vc.stRule.iValid = m_chkEventValid.GetCheck();

	vc.stRegion.iPointNum = POINT_SUM;
	CString strPoint = "";
	GetDlgItemText(IDC_EDT_CROWD_NEW_AREA, strPoint);
	GetPolyFromString(strPoint, POINT_SUM, vc.stRegion);

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CROWD, m_iChannelNO, &vc, sizeof(VCACrowd));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_CrowdNew::NetClient_VCASetConfig[VCA_CMD_CROWD] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_CrowdNew::NetClient_VCASetConfig[VCA_CMD_CROWD] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_CrowdNew::OnBnClickedBtnCrowdNewDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_Crowd);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		m_edtArea.SetWindowText(cPointBuf);
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_CrowdNew::OnEnChangeEdtCrowdNewSensitibity()
{
	int iTemp = GetDlgItemInt(IDC_EDT_CROWD_NEW_SENSITIVITY);
	if (iTemp < SENSI_MIN || iTemp > SENSI_MAX)
	{
		SetDlgItemInt(IDC_EDT_CROWD_NEW_SENSITIVITY, SENSI_MAX);
	}
}

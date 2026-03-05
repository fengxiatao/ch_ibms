// CLS_VcaFireWorkDetect.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_VcaFireWorkDetect.h"


// CLS_VcaFireWorkDetect dialog

IMPLEMENT_DYNAMIC(CLS_VcaFireWorkDetect, CDialog)

CLS_VcaFireWorkDetect::CLS_VcaFireWorkDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaFireWorkDetect::IDD, pParent)
{

}

CLS_VcaFireWorkDetect::~CLS_VcaFireWorkDetect()
{
}


void CLS_VcaFireWorkDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_RULE, m_chkRule);
	DDX_Control(pDX, IDC_CHECK_ALARMCOUNT, m_chkAlarmCount);
	DDX_Control(pDX, IDC_CHECK_EVENTVALID, m_chkEvent);
	DDX_Control(pDX, IDC_CHECK_TARGET, m_chkTarget);
	DDX_Control(pDX, IDC_COMBO_AREACOLOR, m_cboAreaColor);
	DDX_Control(pDX, IDC_COMBO_AlarmColor, m_cboAlarmColor);
	DDX_Control(pDX, IDC_SLIDERSMOKESENSITIVE, m_sliSmokeSensitive);
	DDX_Control(pDX, IDC_COMBO_MODE, m_cboChkMode);
	DDX_Control(pDX, IDC_EDIT_WAITTIME, m_edtWaitTime);
	DDX_Control(pDX, IDC_SLIDER_FISENSTIVVE, m_sliFireSensitive);
}


BEGIN_MESSAGE_MAP(CLS_VcaFireWorkDetect, CDialog)
	ON_WM_SHOWWINDOW()
	ON_WM_HSCROLL()
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_VcaFireWorkDetect::OnBnClickedButtonSet)
END_MESSAGE_MAP()


// CLS_VcaFireWorkDetect message handlers

BOOL CLS_VcaFireWorkDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  add extra initialization here
	UpdateUIText();
	//UpdatePageUI();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaFireWorkDetect::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_TARGET, GetTextByLan("目标框是否显示", "Target Show"));
	SetDlgItemText(IDC_CHECK_RULE, GetTextByLan("规则是否显示", "Rule Show"));
	SetDlgItemText(IDC_CHECK_EVENTVALID, GetTextByLan("事件是否有效", "Event Valid"));
	SetDlgItemText(IDC_CHECK_ALARMCOUNT, GetTextByLan("报警计数", "Alarm Count"));
	SetDlgItemText(IDC_STATIC_AREACOLOR, GetTextByLan("区域颜色", "Area Color"));
	SetDlgItemText(IDC_STATIC_AlarmColor, GetTextByLan("报警颜色", "Alarm Color"));
	SetDlgItemText(IDC_STATIC_FIRESENSITIVE, GetTextByLan("火点检测灵敏度", "Fire Sensitive"));
	SetDlgItemText(IDC_STATIC_SMOKESENSITIVE, GetTextByLan("烟雾检测灵敏度", "Smoke Sensitive"));
	SetDlgItemText(IDC_STATIC_DETECTMODE, GetTextByLan("检测模式", "Check Mode"));
	SetDlgItemText(IDC_STATIC_WAIT_TIME, GetTextByLan("警情等待时间", "Wait Time"));

	const CString strColor[] = {GetTextByLan(_T("红色"), _T("Red")), GetTextByLan(_T("绿色"), _T("Green")), GetTextByLan(_T("黄色"), _T("Yellow")), 
		GetTextByLan(_T("蓝色"), _T("Blue")), GetTextByLan(_T("紫色"), _T("Purple")), GetTextByLan(_T("青色"), _T("Cyan")), 
		GetTextByLan(_T("黑色"), _T("Black")), GetTextByLan(_T("白色"), _T("White"))};

	m_cboAreaColor.ResetContent();
	m_cboAlarmColor.ResetContent();
	for (int i = 0; i < sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboAreaColor.InsertString(i, strColor[i]);
		m_cboAlarmColor.InsertString(i, strColor[i]);
	}
	m_cboAreaColor.SetCurSel(1);
	m_cboAlarmColor.SetCurSel(0);


	m_cboChkMode.ResetContent();
	m_cboChkMode.SetItemData(0, m_cboChkMode.AddString(GetTextByLan(_T("1-火点"), _T("Fire"))));
	m_cboChkMode.SetItemData(1, m_cboChkMode.AddString(GetTextByLan(_T("2-烟雾"), _T("Smoke"))));
	m_cboChkMode.SetItemData(2, m_cboChkMode.AddString(GetTextByLan(_T("3-火点或烟雾"), _T("Fire or Smoke"))));
	m_cboChkMode.SetItemData(3, m_cboChkMode.AddString(GetTextByLan(_T("4-火点且烟雾"), _T("Fire and Smoke"))));
	m_cboChkMode.SetCurSel(0);
	m_sliFireSensitive.SetRange(0,100);
	m_sliSmokeSensitive.SetRange(0,100);
	m_sliFireSensitive.SetPos(50);
	m_sliSmokeSensitive.SetPos(50);

}


void CLS_VcaFireWorkDetect::OnBnClickedButtonSet()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaFireWorkDetect::Invalid Logon id(%d)", m_iLogonID);
		return;
	}
	if (m_iChannelNO < 0)
	{
		m_iChannelNO = 0;
	}
	VcaFireWork vc = {0};
	vc.iSize = sizeof(vc);
	vc.iRuleID = m_iRuleID;
	vc.iSceneId = m_iSceneID;
	vc.iValid = m_chkEvent.GetCheck();
	vc.iDisplayStat = m_chkAlarmCount.GetCheck();
	vc.iDisplayRule = m_chkRule.GetCheck();
	vc.iDisplayTarget = m_chkTarget.GetCheck();
	vc.iColor = m_cboAreaColor.GetCurSel()+1;
	vc.iAlarmColor = m_cboAlarmColor.GetCurSel()+1;
	vc.iFireSensitiv = m_sliFireSensitive.GetPos();
	vc.iSmogSensitiv = m_sliSmokeSensitive.GetPos();
	vc.iModelType = m_cboChkMode.GetCurSel() + 1; 
	CString cstrTemThreshold;
	m_edtWaitTime.GetWindowText(cstrTemThreshold);
	vc.iWaitTime = _ttoi(cstrTemThreshold);

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_FIREWORKS, m_iChannelNO, &vc, sizeof(VcaFireWork));
	if (iRet < 0)
	{

		AddLog(LOG_TYPE_FAIL,"","CLS_VcaFireWorkDetect::NetClient_VCASetConfig[VCA_CMD_FIREWORKS] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VcaFireWorkDetect::NetClient_VCASetConfig[VCA_CMD_FIREWORKS] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaFireWorkDetect::UpdatePageUI()
{
	if (m_iLogonID == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "VCAEVENT_TemDetect::Invalid Logon id.(%d)", m_iLogonID);
		return;
	}
	if (m_iChannelNO < 0)
	{
		m_iChannelNO = 0;
	}
	VcaFireWork vc = {0};
	vc.iSize = sizeof(vc);
	vc.iSceneId = m_iSceneID;
	vc.iRuleID = m_iRuleID;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_FIREWORKS, m_iChannelNO, &vc, sizeof(VcaFireWork));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VcaFireWorkDetect::NetClient_VCAGetConfig[VCA_CMD_FIREWORKS] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_chkEvent.SetCheck(vc.iValid);
		m_chkAlarmCount.SetCheck(vc.iDisplayStat);
		m_chkRule.SetCheck(vc.iDisplayRule);
		m_chkEvent.SetCheck(vc.iDisplayTarget);
		m_cboChkMode.SetCurSel(vc.iModelType - 1);
		m_chkTarget.SetCheck(vc.iDisplayTarget);
		m_cboAreaColor.SetCurSel(vc.iColor - 1);
		m_cboAlarmColor.SetCurSel(vc.iAlarmColor - 1);
		m_sliFireSensitive.SetPos(vc.iFireSensitiv);
		m_sliSmokeSensitive.SetPos(vc.iSmogSensitiv);
		CString strIndex;
		strIndex.Format("%d", vc.iFireSensitiv);
		GetDlgItem(IDC_STATIC_FIRESENSITIVE2)->SetWindowText(strIndex);
		strIndex.Format("%d", vc.iSmogSensitiv);
		GetDlgItem(IDC_STATIC_SMOKESENSITIVE2)->SetWindowText(strIndex);
		strIndex.Format("%d", vc.iWaitTime);
		m_edtWaitTime.SetWindowText(strIndex);
		AddLog(LOG_TYPE_SUCC,"","CLS_VcaFireWorkDetect::NetClient_VCAGetConfig[VCA_CMD_FIREWORK] (%d, %d)", m_iLogonID, m_iChannelNO);
	}

}

void CLS_VcaFireWorkDetect::OnLanguageChanged()
{	
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VcaFireWorkDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	UpdatePageUI();
}

void CLS_VcaFireWorkDetect::OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar)
{
	//if (SB_ENDSCROLL == nSBCode)
	{
		int iCtrlID = pScrollBar->GetDlgCtrlID();
		switch(iCtrlID)
		{
		case IDC_SLIDER_FISENSTIVVE:
			{
				SetDlgItemInt(IDC_STATIC_FIRESENSITIVE2, m_sliFireSensitive.GetPos());
			}
			break;
		case IDC_SLIDERSMOKESENSITIVE:
			{
				SetDlgItemInt(IDC_STATIC_SMOKESENSITIVE2, m_sliSmokeSensitive.GetPos());
			}
			break;
		default:
			break;
		}
	}

	CDialog::OnHScroll(nSBCode, nPos, pScrollBar);
}

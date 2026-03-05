// VCAEVENT_HumanDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_HumanDetect.h"


// CLS_VCAEVENT_HumanDetect dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_HumanDetect, CDialog)

CLS_VCAEVENT_HumanDetect::CLS_VCAEVENT_HumanDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_HumanDetect::IDD, pParent)
{

}

CLS_VCAEVENT_HumanDetect::~CLS_VCAEVENT_HumanDetect()
{
}

void CLS_VCAEVENT_HumanDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_SLIDER_HUMAN_MINSIZE, m_sldMinSize);
	DDX_Control(pDX, IDC_SLIDER_HUMAN_MANSIZE, m_sldMaxSize);
	DDX_Control(pDX, IDC_CHECK_HUMAN_SNAP, m_chkEnable);
	DDX_Control(pDX, IDC_CHECK_HUMAN_DISPLAY_TARGET, m_chkTargetBox);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_HumanDetect, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_HUMAN_SET, &CLS_VCAEVENT_HumanDetect::OnBnClickedButtonHumanSet)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_HUMAN_MINSIZE, &CLS_VCAEVENT_HumanDetect::OnNMCustomdrawSliderHumanMinsize)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_HUMAN_MANSIZE, &CLS_VCAEVENT_HumanDetect::OnNMCustomdrawSliderHumanMansize)
END_MESSAGE_MAP()


// CLS_VCAEVENT_HumanDetect message handler

BOOL CLS_VCAEVENT_HumanDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VCAEVENT_HumanDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_HumanDetect::OnLanguageChanged()
{	
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAEVENT_HumanDetect::UpdateUIText()
{
	SetDlgItemTextEx(IDC_BUTTON_HUMAN_SET, IDS_SET);
	SetDlgItemTextEx(IDC_CHECK_HUMAN_DISPLAY_TARGET, IDS_VCAEVENT_SHOW_TARGET_BOX);
	SetDlgItemText(IDC_CHECK_HUMAN_SNAP, GetTextByLan("人形抓拍使能", "Human snap enable"));
	SetDlgItemText(IDC_STATIC_HUMAN_MINSIZE, GetTextByLan("最小人形尺寸", "Min size"));
	SetDlgItemText(IDC_STATIC_HUMAN_MAXSIZE, GetTextByLan("最大人形尺寸", "Max size"));


	m_sldMaxSize.SetRange(1, 10000);
	m_sldMaxSize.SetPos(1);
	SetDlgItemInt(IDC_STATIC_HUMAN_MAXSIZE_NUM, m_sldMaxSize.GetPos());

	m_sldMinSize.SetRange(0, 10000);
	m_sldMinSize.SetPos(0);
	SetDlgItemInt(IDC_STATIC_HUMAN_MINSIZE_NUM, m_sldMinSize.GetPos());	

}

void CLS_VCAEVENT_HumanDetect::UpdatePageUI()
{
	HumanDetectArithmetic tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneID = m_iSceneID;			
	tInfo.iRuleNo = m_iRuleID;			

	int iRetValue = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_HUMAN_ADVANCE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if(iRetValue >= 0)
	{
		m_chkEnable.SetCheck(tInfo.iSnapEnable);
		m_chkTargetBox.SetCheck(tInfo.iDisplayTarget);
		m_sldMinSize.SetPos(tInfo.iMinSizeEx);
		m_sldMaxSize.SetPos(tInfo.iMaxSizeEx);
	}

	return;
} 
void CLS_VCAEVENT_HumanDetect::OnBnClickedButtonHumanSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_HumanDetect::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	HumanDetectArithmetic tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneID = m_iSceneID;			
	tInfo.iRuleNo = m_iRuleID;			
	tInfo.iSnapEnable = m_chkEnable.GetCheck();		
	tInfo.iDisplayTarget = m_chkTargetBox.GetCheck();		
	tInfo.iMinSizeEx = m_sldMinSize.GetPos();			  
	tInfo.iMaxSizeEx = m_sldMaxSize.GetPos();			

	int iRetValue = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_HUMAN_ADVANCE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if(iRetValue<0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_HumanDetect::NetClient_VCASetConfig[VCA_CMD_HUMAN_ADVANCE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}

	return;
}

void CLS_VCAEVENT_HumanDetect::OnNMCustomdrawSliderHumanMinsize(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_HUMAN_MINSIZE_NUM, m_sldMinSize.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_HumanDetect::OnNMCustomdrawSliderHumanMansize(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_HUMAN_MAXSIZE_NUM, m_sldMaxSize.GetPos());
	*pResult = 0;
}

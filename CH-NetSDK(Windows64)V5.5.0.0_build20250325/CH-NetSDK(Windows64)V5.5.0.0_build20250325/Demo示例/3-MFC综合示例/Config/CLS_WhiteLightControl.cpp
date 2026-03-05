//NetClientDemo\Config\CLS_WhiteLightControl.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_WhiteLightControl.h"


// CLS_WhiteLightControl dialog

IMPLEMENT_DYNAMIC(CLS_WhiteLightControl, CDialog)

CLS_WhiteLightControl::CLS_WhiteLightControl(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_WhiteLightControl::IDD, pParent)
{

}

CLS_WhiteLightControl::~CLS_WhiteLightControl()
{
}

void CLS_WhiteLightControl::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_CONTROL_TYPE, m_cboControlType);
}


BEGIN_MESSAGE_MAP(CLS_WhiteLightControl, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_WHITE_CONTROL_SET, &CLS_WhiteLightControl::OnBnClickedButtonWhiteControlSet)
END_MESSAGE_MAP()


// CLS_WhiteLightControl message handler

void CLS_WhiteLightControl::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	// TODO: add message handler code here
	UpdatePageUI();
	UpdatePageText();

}

BOOL CLS_WhiteLightControl::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdatePageUI();
	UpdatePageText();

	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_WhiteLightControl::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;

	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo =  _iChannelNo;
	}

	if (_iStreamNo < 0)
	{
		m_iStreamNO = 0;
	}
	else
	{
		m_iStreamNO = _iStreamNo;
	}
	
}

void CLS_WhiteLightControl::OnLanguageChanged( int _iLanguage )
{
	UpdatePageText();
}

void CLS_WhiteLightControl::UpdatePageText()
{
	SetDlgItemText(IDC_STATIC_CONTROL_TYPE, GetTextByLan(_T("控制类型"), _T("ControlType")));
	SetDlgItemText(IDC_STATIC_BEGIN_HOUR, GetTextByLan(_T("开始小时"), _T("BeginHour")));
	SetDlgItemText(IDC_STATIC_BEGEIN_MINUTE, GetTextByLan(_T("开始分钟"), _T("BeginMinute")));
	SetDlgItemText(IDC_STATIC_END_HOUR, GetTextByLan(_T("结束小时"), _T("EndHour")));
	SetDlgItemText(IDC_STATIC_END_MINUTE, GetTextByLan(_T("结束分钟"), _T("EndMinute")));
	SetDlgItemText(IDC_STATIC_LIGHT_TYPE, GetTextByLan(_T("灯组编号"), _T("LightId")));
	SetDlgItemText(IDC_BUTTON_WHITE_CONTROL_SET, GetTextByLan(_T("设置"), _T("Set")));

	m_cboControlType.ResetContent();
	m_cboControlType.InsertString(0, "Auto");
	m_cboControlType.InsertString(1, "ManualOn");
	m_cboControlType.InsertString(2, "ManualOff");
	m_cboControlType.InsertString(3, "Timing");
	m_cboControlType.SetCurSel(0);
}

void CLS_WhiteLightControl::UpdatePageUI()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iReturnBytes = -1;
	WhiteLightContrl tInfo = {0};
	tInfo.iBufSize = (int)sizeof(tInfo);
	

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_WHITELIGHT_CONTRL, m_iChannelNo, &tInfo, (int)sizeof(tInfo), &iReturnBytes);

	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
		m_cboControlType.SetCurSel(tInfo.iContrlType);
		SetDlgItemInt(IDC_EDIT_BEGIN_HOUR, tInfo.u16BegainHour);
		SetDlgItemInt(IDC_EDIT_BEGIN_MINUTE, tInfo.u16BegainMinute);
		SetDlgItemInt(IDC_EDIT_END_HOUR, tInfo.u16EndHour);
		SetDlgItemInt(IDC_EDIT_END_MINUTE, tInfo.u16EndMinute);
		SetDlgItemInt(IDC_EDIT_LIGHT_VALUE, tInfo.iLightValue);
		SetDlgItemInt(IDC_EDIT_LIGHT_VALUE2, tInfo.iLightValue2);
		SetDlgItemInt(IDC_EDIT_LIGHT_VALUE3, tInfo.iLightValue3);
		SetDlgItemInt(IDC_EDIT_LIGHT_ID, tInfo.iLightID);
	}
}

void CLS_WhiteLightControl::OnBnClickedButtonWhiteControlSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	WhiteLightContrl tInfo = {0};
	tInfo.iBufSize = sizeof(WhiteLightContrl);
	tInfo.iContrlType = m_cboControlType.GetCurSel();
	tInfo.u16BegainHour = GetDlgItemInt(IDC_EDIT_BEGIN_HOUR);
	tInfo.u16BegainMinute = GetDlgItemInt(IDC_EDIT_BEGIN_MINUTE);
	tInfo.u16EndHour = GetDlgItemInt(IDC_EDIT_END_HOUR);
	tInfo.u16EndMinute = GetDlgItemInt(IDC_EDIT_END_MINUTE);
	tInfo.iLightValue = GetDlgItemInt(IDC_EDIT_LIGHT_VALUE);
	tInfo.iLightValue2 = GetDlgItemInt(IDC_EDIT_LIGHT_VALUE2);
	tInfo.iLightValue3 = GetDlgItemInt(IDC_EDIT_LIGHT_VALUE3);
	tInfo.iLightID = GetDlgItemInt(IDC_EDIT_LIGHT_ID);
	
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_WHITELIGHT_CONTRL, m_iChannelNo, &tInfo, (int)sizeof(tInfo));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig(%d,%d)",m_iChannelNo, m_iChannelNo);
	}
}

// DlgWirelessSilent.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgWirelessSilent.h"


// CDlgWirelessSilent dialog

IMPLEMENT_DYNAMIC(CDlgWirelessSilent, CDialog)

CDlgWirelessSilent::CDlgWirelessSilent(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CDlgWirelessSilent::IDD, pParent)
	, m_uiSilentTime(0)
{

}

CDlgWirelessSilent::~CDlgWirelessSilent()
{
}

void CDlgWirelessSilent::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT_SILENTTIME, m_uiSilentTime);
}


BEGIN_MESSAGE_MAP(CDlgWirelessSilent, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CDlgWirelessSilent::OnBnClickedButtonSet)
END_MESSAGE_MAP()


// CDlgWirelessSilent message handlers


BOOL CDlgWirelessSilent::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	InitPageUI();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}
void CDlgWirelessSilent::InitPageUI()
{
	SetDlgItemText(IDC_STATIC, GetTextByLan("无线电重启时间", "Wireless Silent Restart Time"));
	SetDlgItemText(IDC_STATIC_TIME, GetTextByLan("分钟", "min"));
	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan("设置", "Set"));
}

void CDlgWirelessSilent::OnBnClickedButtonSet()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CDlgWirelessSilent::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	UpdateData(TRUE);
	WirelessSilent tInfo = {0};
	tInfo.iSize =  sizeof(tInfo);

	tInfo.iRestartTime = m_uiSilentTime;


	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_WIRELESS_SILENT, m_iChannelNO, &tInfo, sizeof(WirelessSilent));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand]COMMAND_ID_WIRELESS_SILENT fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SendCommand]COMMAND_ID_WIRELESS_SILENT SUCCESS!");

	}
}

void CDlgWirelessSilent::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	if (m_iLogonID < 0)
	{
		return;
	}

	int iMsgType = LOWORD(_wParam);
	switch(iMsgType)
	{
	case WCM_WIRELESS_SILENT:
		{
			WirelessSilent *tInfo = (WirelessSilent *)_iLParam;
			AddLog(LOG_TYPE_SUCC, "", "[CDlgWirelessSilent]OnMainNotify::WirelessSilent  iRestart=%d,iResult=%d.", tInfo->iRestartTime,tInfo->iResult);

		}
		break;
	default:
		break;
	}
}

void CDlgWirelessSilent::OnChannelChanged( int _iLogonID,int _iChannelNo, int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if (m_iChannelNO < 0)
	{
		m_iChannelNO = 0;
	} 
	else
	{
		m_iChannelNO = _iChannelNo;
	}

}

void CDlgWirelessSilent::OnLanguageChanged(int _iLanguage)
{
	InitPageUI();
}

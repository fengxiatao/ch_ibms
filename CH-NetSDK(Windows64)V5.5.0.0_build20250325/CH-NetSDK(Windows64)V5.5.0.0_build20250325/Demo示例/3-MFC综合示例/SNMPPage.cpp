// SNMPPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "SNMPPage.h"


// CLS_SNMPPage dialog
#define   RECOMMUNITY              "public"
#define   WRCOMMUNITY              "private"
#define   PORT                      161
#define   TRAPPORT                  162
#define   TRAPCOMMUNITY             "public"
#define   RESECURITY                3
#define   REAUALG                   1
#define   REAUPWD                   "12345678"
#define   REPRIALG                  1
#define   REPRIPWD                  "12345678"
#define   WRSECURITY                3
#define   WRAUALG                   1
#define   WRAUPWD                   "12345678"
#define   WRPRIALG                  1
#define   WRPRIPWD                  "12345678"

IMPLEMENT_DYNAMIC(CLS_SNMPPage, CDialog)

CLS_SNMPPage::CLS_SNMPPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_SNMPPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

CLS_SNMPPage::~CLS_SNMPPage()
{
}

void CLS_SNMPPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_SNMPV1, m_CheckV1);
	DDX_Control(pDX, IDC_CHECK_SNMPV2c, m_CheckV2c);
	DDX_Control(pDX, IDC_CHECK_SNMPV3, m_CheckV3);
	DDX_Control(pDX, IDC_COMBO_RESECURITY, m_Combox_ReSecurity);
	DDX_Control(pDX, IDC_COMBO_WRSECURITY, m_Combox_WrSecurity);
	DDX_Control(pDX, IDC_COMBO_REAUALG, m_Combox_Reaualg);
	DDX_Control(pDX, IDC_COMBO_REPRIALG, m_Combox_ReprigAlg);
	DDX_Control(pDX, IDC_COMBO_WRPRIALG, m_Combox_WrPrigAlg);
	DDX_Control(pDX, IDC_COMBO_WRAUALG, m_Combox_WrAuAlg);
}


BEGIN_MESSAGE_MAP(CLS_SNMPPage, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_DEFAULT, &CLS_SNMPPage::OnBnClickedButtonDefault)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_SNMPPage::OnBnClickedButtonSet)
END_MESSAGE_MAP()


// CLS_SNMPPage message handlers

BOOL CLS_SNMPPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	SetUIText();

	return TRUE;
}

void CLS_SNMPPage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
	SetUIText();
	UpdateParam();
}

void CLS_SNMPPage::UpdateParam()
{
	SnmpPara tPara = {0};
	tPara.iSize = (int)sizeof(SnmpPara);
	int iBytesReturned = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SNMP_PARA, m_iChannelNo, &tPara, (int)sizeof(tPara), &iBytesReturned);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","Get Snmp Param Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","Get Snmp Param Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
	GetParam(tPara);
}

void CLS_SNMPPage::GetParam(SnmpPara tPara)
{
	m_CheckV1.SetCheck(tPara.iEnable[0]);
	m_CheckV2c.SetCheck(tPara.iEnable[1]);
	m_CheckV3.SetCheck(tPara.iEnable[2]);
	SetDlgItemInt(IDC_EDIT_PORT, tPara.iPort);
	SetDlgItemText(IDC_EDIT_RECOM, tPara.cReCommunity);
	SetDlgItemText(IDC_EDIT_WRCOM, tPara.cWrCommunity);
	SetDlgItemText(IDC_EDIT_TRAPADDR, tPara.cTrapAddr);
	SetDlgItemInt(IDC_EDIT_TRAPPORT, tPara.iTrapPort);
	SetDlgItemText(IDC_EDIT_TRCOM, tPara.cTrapCommunity);
	SetDlgItemText(IDC_EDIT_REUSER, tPara.cReUser);
	SetDlgItemText(IDC_EDIT_WRUSER, tPara.cWrUser);
	SetDlgItemText(IDC_EDIT_REAUPWD, tPara.cReAuthPwd);
    SetDlgItemText(IDC_EDIT_AUTH_PWD, tPara.cWrAuthPwd);
	SetDlgItemText(IDC_EDIT_REPRIPWD, tPara.cRePrivPwd);
	SetDlgItemText(IDC_EDIT_WRPRIPWD, tPara.cWrPrivPwd);
	m_Combox_ReSecurity.SetCurSel(tPara.iReSecurity - 1);
	m_Combox_WrSecurity.SetCurSel(tPara.iWrSecurity - 1);
	m_Combox_Reaualg.SetCurSel(tPara.iReAuthAlg - 1);
	m_Combox_WrAuAlg.SetCurSel(tPara.iWrAuthAlg - 1);
	m_Combox_ReprigAlg.SetCurSel(tPara.iRePrivAlg - 1);
	m_Combox_WrPrigAlg.SetCurSel(tPara.iWrPrivAlg - 1);

}

void CLS_SNMPPage::OnBnClickedButtonDefault()
{
	SetDlgItemText(IDC_EDIT_RECOM, RECOMMUNITY);
	SetDlgItemText(IDC_EDIT_WRCOM, WRCOMMUNITY);
	SetDlgItemInt(IDC_EDIT_PORT, PORT);
	SetDlgItemInt(IDC_EDIT_TRAPPORT, TRAPPORT);
	SetDlgItemText(IDC_EDIT_TRCOM, TRAPCOMMUNITY);
	SetDlgItemText(IDC_EDIT_REAUPWD, REAUPWD);
	SetDlgItemText(IDC_EDIT_REPRIPWD, REPRIPWD);
	SetDlgItemText(IDC_EDIT_AUTH_PWD, WRAUPWD);
	SetDlgItemText(IDC_EDIT_WRPRIPWD, WRPRIPWD);
	m_Combox_ReSecurity.SetCurSel(RESECURITY - 1);
	m_Combox_Reaualg.SetCurSel(REAUALG - 1);
	m_Combox_ReprigAlg.SetCurSel(REPRIALG - 1);

	m_Combox_WrSecurity.SetCurSel(RESECURITY - 1);
	m_Combox_WrAuAlg.SetCurSel(WRAUALG - 1);
	m_Combox_WrPrigAlg.SetCurSel(WRPRIALG - 1);
}

void CLS_SNMPPage::OnBnClickedButtonSet()
{
	// TODO: Add your control notification handler code here
	SnmpPara tPara = {0};

	tPara.iSize = (int)sizeof(tPara);
	tPara.iEnable[0] = m_CheckV1.GetCheck();
	tPara.iEnable[1] = m_CheckV2c.GetCheck();
	tPara.iEnable[2] = m_CheckV3.GetCheck();
	tPara.iPort = GetDlgItemInt(IDC_EDIT_PORT);
	CString strReCommunity;
	GetDlgItem(IDC_EDIT_RECOM)->GetWindowText(strReCommunity);
	memcpy_s(tPara.cReCommunity, sizeof(tPara.cReCommunity), strReCommunity, sizeof(tPara.cReCommunity));
	CString strWrCommunity;
	GetDlgItem(IDC_EDIT_WRCOM)->GetWindowText(strWrCommunity);
	memcpy_s(tPara.cWrCommunity, sizeof(tPara.cWrCommunity), strWrCommunity, sizeof(tPara.cWrCommunity));
	CString strTrapAddr;
	GetDlgItem(IDC_EDIT_TRAPADDR)->GetWindowText(strTrapAddr);
	memcpy_s(tPara.cTrapAddr, LEN_16, strTrapAddr, LEN_16);
	tPara.iTrapPort = GetDlgItemInt(IDC_EDIT_TRAPPORT);
	CString strTrapCommunity;
	GetDlgItem(IDC_EDIT_TRCOM)->GetWindowText(strTrapCommunity);
	memcpy_s(tPara.cTrapCommunity, LEN_32, strTrapCommunity, LEN_32);
	CString strReUser;
	GetDlgItem(IDC_EDIT_REUSER)->GetWindowText(strReUser);
	memcpy_s(tPara.cReUser, LEN_32, strReUser, LEN_32);

	tPara.iReSecurity = m_Combox_ReSecurity.GetCurSel() + 1;

	tPara.iReAuthAlg = m_Combox_Reaualg.GetCurSel() + 1;;

	CString strReAuPwd;
	GetDlgItem(IDC_EDIT_REAUPWD)->GetWindowText(strReAuPwd);
	memcpy_s(tPara.cReAuthPwd, LEN_16, strReAuPwd, LEN_16);

	tPara.iRePrivAlg = m_Combox_ReprigAlg.GetCurSel() + 1;

	CString strRePriPwd;
	GetDlgItem(IDC_EDIT_REPRIPWD)->GetWindowText(strRePriPwd);
	memcpy_s(tPara.cRePrivPwd, LEN_16, strRePriPwd, LEN_16);

	CString strWrUser;
	GetDlgItem(IDC_EDIT_WRUSER)->GetWindowText(strWrUser);
	memcpy_s(tPara.cWrUser, LEN_32, strWrUser, LEN_32);

	tPara.iWrSecurity = m_Combox_WrSecurity.GetCurSel() + 1;

	tPara.iWrAuthAlg = m_Combox_WrAuAlg.GetCurSel() + 1;

	CString strWrAuPwd;
	GetDlgItem(IDC_EDIT_AUTH_PWD)->GetWindowText(strWrAuPwd);
	memcpy_s(tPara.cWrAuthPwd, LEN_16, strWrAuPwd, LEN_16);

	tPara.iWrPrivAlg = m_Combox_WrPrigAlg.GetCurSel() + 1;

	CString strWrPriPwd;
	GetDlgItem(IDC_EDIT__AUPRIPWD)->GetWindowText(strWrPriPwd);
	memcpy_s(tPara.cWrPrivPwd, LEN_16, strWrPriPwd, LEN_16);

	int iRet =NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SNMP_PARA, m_iChannelNo, &tPara, (int)sizeof(tPara));

	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","Set Snmp Param Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","Set Snmp Param Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}

	
}

void CLS_SNMPPage::OnLanguageChanged(int _iLanguage)
{
	SetUIText();
}

void CLS_SNMPPage::SetUIText()
{
	SetDlgItemText(IDC_STATIC_ENABLE, GetTextByLan(_T("使能"),_T("Enable")));
	SetDlgItemText(IDC_STATIC_PORT, GetTextByLan(_T("端口"),_T("Port")));
	SetDlgItemText(IDC_STATIC_RECOM, GetTextByLan(_T("读共同体"),_T("Read Community")));
	SetDlgItemText(IDC_STATIC_WRCOM, GetTextByLan(_T("写共同体"),_T("Write Community")));
	SetDlgItemText(IDC_STATIC_TRAPADDR, GetTextByLan(_T("Trap地址"),_T("Trap Address")));
	SetDlgItemText(IDC_STATIC_TRAPPORT, GetTextByLan(_T("Trap端口"),_T("Trap Port")));
	SetDlgItemText(IDC_STATIC_TRAPCOM, GetTextByLan(_T("Trap团体名"),_T("Trap Community")));

	SetDlgItemText(IDC_STATIC_REUSER, GetTextByLan(_T("读安全名称"),_T("Read User")));
	SetDlgItemText(IDC_STATIC_RESECURITY, GetTextByLan(_T("读安全级别"),_T("Read Security")));
	SetDlgItemText(IDC_STATIC_REAUALG, GetTextByLan(_T("读认证算法"),_T("Read AuAlg")));
	SetDlgItemText(IDC_STATIC_REAUPWD, GetTextByLan(_T("读认证密码"),_T("Read AuPwd")));
	SetDlgItemText(IDC_STATIC_REPRIALG, GetTextByLan(_T("读私有算法"),_T("Read PriAlg")));
	SetDlgItemText(IDC_STATIC_REPRIPWD, GetTextByLan(_T("读私有密码"),_T("Read PriPwd")));

	SetDlgItemText(IDC_STATIC_WRUSER, GetTextByLan(_T("写安全名称"),_T("Write User")));
	SetDlgItemText(IDC_STATIC_WRSECU, GetTextByLan(_T("写安全级别"),_T("Write Security")));
	SetDlgItemText(IDC_STATIC_AUALG, GetTextByLan(_T("写认证算法"),_T("Write AuAlg")));
	SetDlgItemText(IDC_STATIC_AUPWD, GetTextByLan(_T("写认证密码"),_T("Write AuPwd")));
	SetDlgItemText(IDC_STATIC_WRPRIALG, GetTextByLan(_T("写私有算法"),_T("Write PriAlg")));
	SetDlgItemText(IDC_STATIC_WRPRIPWD, GetTextByLan(_T("写私有密码"),_T("Write PriPwd")));

	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan(_T("设置"),_T("Set")));
	SetDlgItemText(IDC_BUTTON_DEFAULT, GetTextByLan(_T("默认"),_T("Default")));

	SetDlgItemText(IDC_STATIC_READ, GetTextByLan(_T("读取SNMP参数"), _T("Read  SNMP Param")));
	SetDlgItemText(IDC_STATIC_WRITE, GetTextByLan(_T("写入SNMP参数"), _T("Write  SNMP Param")));
}

// ActiveDdnsPage.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "ActiveDdnsPage.h"


// CLS_ActiveDdnsPage 对话框

IMPLEMENT_DYNAMIC(CLS_ActiveDdnsPage, CDialog)

CLS_ActiveDdnsPage::CLS_ActiveDdnsPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ActiveDdnsPage::IDD, pParent)
{

}

CLS_ActiveDdnsPage::~CLS_ActiveDdnsPage()
{
}

void CLS_ActiveDdnsPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_DOMAIN_WANIP, m_edtDdnsSerDomainIp);
	DDX_Control(pDX, IDC_EDIT_DDNS_WANPORT, m_edtDdnsSerWanPort);
	DDX_Control(pDX, IDC_EDIT_LOCAL_WANIP, m_edtLocalLanIp);
	DDX_Control(pDX, IDC_EDIT_LOCAL_DOMAINNAME, m_edtLocalDomainName);
	DDX_Control(pDX, IDC_EDIT_LOCAL_FACTORYID, m_edtLocalFactoryId);
	DDX_Control(pDX, IDC_EDIT_LOCAL_TCP_WANPORT, m_edtLocalTcpWanPort);
	DDX_Control(pDX, IDC_EDIT_LOCAL_HTTP_WANPORT, m_edtLocalHttpPort);
	DDX_Control(pDX, IDC_EDIT_LOCAL_RTMP_WANPORT, m_edtLocalRtmpPort);
}

BEGIN_MESSAGE_MAP(CLS_ActiveDdnsPage, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_REGISTER_DDNS, &CLS_ActiveDdnsPage::OnBnClickedButtonRegisterDdns)
	ON_BN_CLICKED(IDC_BUTTON_TEST_DDNS, &CLS_ActiveDdnsPage::OnBnClickedButtonTestDdns)
END_MESSAGE_MAP()


// CLS_ActiveDdnsPage 消息处理程序

BOOL CLS_ActiveDdnsPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtDdnsSerDomainIp.SetLimitText(LEN_128);
	m_edtDdnsSerWanPort.SetLimitText(LEN_8);
	m_edtLocalLanIp.SetLimitText(LEN_64);
	m_edtLocalDomainName.SetLimitText(LEN_128);
	m_edtLocalFactoryId.SetLimitText(LEN_32);
	m_edtLocalTcpWanPort.SetLimitText(LEN_8);
	m_edtLocalHttpPort.SetLimitText(LEN_8);
	m_edtLocalRtmpPort.SetLimitText(LEN_8);

	m_edtDdnsSerDomainIp.SetWindowText("www.easyddns.tech");
	SetDlgItemInt(IDC_EDIT_DDNS_WANPORT, 6004);
	m_edtLocalLanIp.SetWindowText("192.168.188.188");
	m_edtLocalDomainName.SetWindowText("NetClientDemo");
	m_edtLocalFactoryId.SetWindowText("ID089031040194080000DEMO");
	SetDlgItemInt(IDC_EDIT_LOCAL_TCP_WANPORT, 7000);
	SetDlgItemInt(IDC_EDIT_LOCAL_HTTP_WANPORT, 80);
	SetDlgItemInt(IDC_EDIT_LOCAL_RTMP_WANPORT, 7002);

	UI_UpdateDialogText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_ActiveDdnsPage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	// TODO: 在此处添加消息处理程序代码
}

void CLS_ActiveDdnsPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialogText();
}

void CLS_ActiveDdnsPage::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_DOMAIN_WANIP, GetTextByLan(_T("服务器地址"), _T("DomainNameOrWanIp")));
	SetDlgItemText(IDC_STATIC_DDNS_WANPORT, GetTextByLan(_T("服务器公网端口"), _T("DDNSWanPort")));
	SetDlgItemText(IDC_STATIC_LOCAL_LANIP, GetTextByLan(_T("本机局域网地址"), _T("LocalLanIp")));
	SetDlgItemText(IDC_STATIC_LOCAL_DOMAINNAME, GetTextByLan(_T("本机域名地址"), _T("LocalDomainName")));
	SetDlgItemText(IDC_STATIC_LOCAL_FACTORYID, GetTextByLan(_T("本机出厂ID"), _T("LocalFactoryId")));
	SetDlgItemText(IDC_STATIC_LOCAL_TCP_WANPORT, GetTextByLan(_T("本机公网Tcp端口"), _T("LocalTcpWanPort")));
	SetDlgItemText(IDC_STATIC_LOCAL_HTTP_WANPORT, GetTextByLan(_T("本机公网Http端口"), _T("LocalHttpWanPort")));
	SetDlgItemText(IDC_STATIC_LOCAL_RTMP_WANPORT, GetTextByLan(_T("本机公网Rtmp端口"), _T("LocalRtmpWanPort")));
}

void CLS_ActiveDdnsPage::OnBnClickedButtonRegisterDdns()
{
	CString cstrDdnsSerDomainIp;
	m_edtDdnsSerDomainIp.GetWindowText(cstrDdnsSerDomainIp);
	int iDdnsWanPort = GetDlgItemInt(IDC_EDIT_DDNS_WANPORT);
	CString cstrLocalLanIp;
	m_edtLocalLanIp.GetWindowText(cstrLocalLanIp);
	CString cstrLocalDomainName;
	m_edtLocalDomainName.GetWindowText(cstrLocalDomainName);
	CString cstrLocalFactoryId;
	m_edtLocalFactoryId.GetWindowText(cstrLocalFactoryId);
	int iLocalTcpWanPort = GetDlgItemInt(IDC_EDIT_LOCAL_TCP_WANPORT);
	int iLocalHttpWanPort = GetDlgItemInt(IDC_EDIT_LOCAL_HTTP_WANPORT);
	int iLocalRtmpWanPort = GetDlgItemInt(IDC_EDIT_LOCAL_RTMP_WANPORT);
	ActiveEasyDdnsPara tRegisterDdns = {0};
	strcpy_s(tRegisterDdns.cEasyDdnsDomainIp, sizeof(tRegisterDdns.cEasyDdnsDomainIp), cstrDdnsSerDomainIp.GetBuffer());
	strcpy_s(tRegisterDdns.cLocalLanIp, sizeof(tRegisterDdns.cLocalLanIp), cstrLocalLanIp.GetBuffer());
	strcpy_s(tRegisterDdns.cLocalDomainName, sizeof(tRegisterDdns.cLocalDomainName), cstrLocalDomainName.GetBuffer());
	strcpy_s(tRegisterDdns.cLocalFactoryId, sizeof(tRegisterDdns.cLocalFactoryId), cstrLocalFactoryId.GetBuffer());
	strcpy_s(tRegisterDdns.cLocalCharSet, sizeof(tRegisterDdns.cLocalCharSet), "GB2312");
	tRegisterDdns.iEasyDdnsWanPort = iDdnsWanPort;
	tRegisterDdns.iLocalTcpWanPort = iLocalTcpWanPort;
	tRegisterDdns.iLocalHttpWanPort = iLocalHttpWanPort;
	tRegisterDdns.iLocalRtmpWanPort = iLocalRtmpWanPort;
	tRegisterDdns.iIpVer = IP_VERSION_4;
	int iRet = NetClient_SetDsmConfig(DSM_CMD_REGISTER_EASYDDNS, &tRegisterDdns, sizeof(ActiveEasyDdnsPara));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDsmConfig::DSM_CMD_REGISTER_EASYDDNS fail, iRet=%d", iRet);
	} else {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDsmConfig::DSM_CMD_REGISTER_EASYDDNS success");
	}
}

void CLS_ActiveDdnsPage::OnBnClickedButtonTestDdns()
{
	CString cstrDdnsSerDomainIp;
	m_edtDdnsSerDomainIp.GetWindowText(cstrDdnsSerDomainIp);
	int iDdnsWanPort = GetDlgItemInt(IDC_EDIT_DDNS_WANPORT);
	CString cstrLocalDomainName;
	m_edtLocalDomainName.GetWindowText(cstrLocalDomainName);
	CString cstrLocalFactoryId;
	m_edtLocalFactoryId.GetWindowText(cstrLocalFactoryId);
	ActiveEasyDdnsPara tTestDdns = {0};
	strcpy_s(tTestDdns.cEasyDdnsDomainIp, sizeof(tTestDdns.cEasyDdnsDomainIp), cstrDdnsSerDomainIp.GetBuffer());
	strcpy_s(tTestDdns.cLocalDomainName, sizeof(tTestDdns.cLocalDomainName), cstrLocalDomainName.GetBuffer());
	strcpy_s(tTestDdns.cLocalFactoryId, sizeof(tTestDdns.cLocalFactoryId), cstrLocalFactoryId.GetBuffer());
	tTestDdns.iEasyDdnsWanPort = iDdnsWanPort;
	tTestDdns.iIpVer = IP_VERSION_4;
	int iRet = NetClient_SetDsmConfig(DSM_CMD_TEST_EASYDDNS, &tTestDdns, sizeof(ActiveEasyDdnsPara));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDsmConfig::DSM_CMD_TEST_EASYDDNS fail, iRet=%d", iRet);
	} else {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDsmConfig::DSM_CMD_TEST_EASYDDNS success");
	}
}

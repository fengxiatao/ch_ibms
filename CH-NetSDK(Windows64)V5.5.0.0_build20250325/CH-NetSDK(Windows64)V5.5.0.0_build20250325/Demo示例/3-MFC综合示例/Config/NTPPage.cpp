// NTPPage.cpp : implementation file
//

#include "stdafx.h"
#include "NTPPage.h"

#define NTP_TEST_SUCCEED 1
#define NTP_TEST_FAIL 2
// CLS_NTPPage dialog

IMPLEMENT_DYNAMIC(CLS_NTPPage, CDialog)

CLS_NTPPage::CLS_NTPPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_NTPPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

CLS_NTPPage::~CLS_NTPPage()
{
}

void CLS_NTPPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_NTPIP, m_edtNTPIp);
	DDX_Control(pDX, IDC_EDIT_NTPPORT, m_edtNTPPort);
	DDX_Control(pDX, IDC_EDIT_NTPINTERVAL, m_edtInterval);
	DDX_Control(pDX, IDC_BUTTON_NTP, m_btnNTP);
}


BEGIN_MESSAGE_MAP(CLS_NTPPage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_NTP, &CLS_NTPPage::OnBnClickedButtonNtp)
	ON_BN_CLICKED(IDC_BTN_LAN_NTP_TEST, &CLS_NTPPage::OnBnClickedBtnLanNtpTest)
END_MESSAGE_MAP()


// CLS_NTPPage message handlers
BOOL CLS_NTPPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtNTPIp.SetLimitText(31);
	m_edtNTPPort.SetLimitText(5);
	m_edtInterval.SetLimitText(8);
	UI_UpdateDialog();

	return TRUE;
}

void CLS_NTPPage::OnChannelChanged(int _iLogonID,int _iChannelNo, int /*_iStreamNo*/)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = (_iChannelNo < 0) ? 0 : _iChannelNo;

	UI_UpdateNTP();
}

void CLS_NTPPage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_NTPPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_NTPIP, IDS_CONFIG_NTP_IP);
	//SetDlgItemTextEx(IDC_STATIC_INTERVAL, IDS_CONFIG_ECOP_INTERVAL);
	SetDlgItemTextEx(IDC_BUTTON_NTP, IDS_SET);
	SetDlgItemTextEx(IDC_STXT_LAN_NTP_PORT, IDS_LOGON_PORT);
	SetDlgItemTextEx(IDC_BTN_LAN_NTP_TEST, IDS_LOG_TEST);
	SetDlgItemText(IDC_STATIC_INTERVAL, GetTextByLan(_T("间隔(小时)"), _T("Interval(Hour)")));
	SetDlgItemText(IDC_STATIC_INTERVAL1, GetTextByLan(_T("间隔(秒)"), _T("Interval(Second)")));

}

void CLS_NTPPage::OnMainNotify( int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser )
{
	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_LASTERROR_INFO:
		{
			int iLParam = (int)_iLParam;
			NotifyResult(iLParam);
		}
		break;   
	default:
		break;
	}
}

void CLS_NTPPage::NotifyResult(int _iLParam)
{
	if (EC_NET_NTP_TEST_RESULT != _iLParam)
	{
		return;
	}

	DevLastError tDecLastError = {0};
	int iRet = NetClient_RecvCommand(m_iLogonID, COMMAND_ID_DEV_LASTERROR, m_iChannelNo, &tDecLastError, sizeof(tDecLastError));
	
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_RecvCommand(%d,%d)",m_iLogonID,m_iChannelNo);
		goto EXIT_FUNC;
	}

	if (EC_NET_NTP_TEST_RESULT != tDecLastError.iErrorID)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_RecvCommand(%d,%d),iErrId(%d)",m_iLogonID,m_iChannelNo, tDecLastError.iErrorID);
		goto EXIT_FUNC;
	}

	int iResult = _ttoi(tDecLastError.cErrorInfo);
	switch(iResult)
	{
	case NTP_TEST_SUCCEED:
		{
			AddLog(LOG_TYPE_SUCC,"","LogonId(%d) ErrId(%d), %s", m_iLogonID, tDecLastError.iErrorID, GetTextEx(IDS_NTP_TEST_SUCCEED));
			break;
		}
	case NTP_TEST_FAIL:
		{
			AddLog(LOG_TYPE_FAIL,"","LogonId(%d) ErrId(%d), %s", m_iLogonID, tDecLastError.iErrorID, GetTextEx(IDS_NTP_TEST_FAIL));
			break;
		}
	default:
		break;
	}

EXIT_FUNC:
	return;
}

BOOL CLS_NTPPage::UI_UpdateNTP()
{
	if (m_iLogonID < 0)
		return FALSE;

	/*char cNTPIP[32] = {0};
	unsigned short usNTPPort;
	int iInterval;*/
	//int iRet = NetClient_GetNTPInfo(m_iLogonID, cNTPIP, &usNTPPort, &iInterval);

	int iRet = RET_FAILED;
#ifdef XML_PROTOCOL
	XmlNtpServer tXmlNtpServer = {0};
	iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_NTP_SERVER, NULL, 0, &tXmlNtpServer, sizeof(XmlNtpServer));
	if (RET_SUCCESS == iRet) {
		SetDlgItemText(IDC_EDIT_NTPIP, tXmlNtpServer.cNtpServerIp);
		SetDlgItemInt(IDC_EDIT_NTPPORT, tXmlNtpServer.iNtpServerPort);
		SetDlgItemInt(IDC_EDIT_NTPINTERVAL, tXmlNtpServer.iNtpInterval);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_XmlGetDevConfig:NETXMLCFG_NTP_SERVER(%d).", m_iLogonID);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_XmlGetDevConfig:NETXMLCFG_NTP_SERVER(%d).", m_iLogonID);
	}
#else
	int iByteReturn = -1;
	NTPInfo tNTPInfo = {0};
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_NTP_INFO, m_iChannelNo, (void*)&tNTPInfo, sizeof(NTPInfo), &iByteReturn);
	if (RET_SUCCESS == iRet) {
		SetDlgItemInt(IDC_EDIT_NTPPORT, tNTPInfo.iServerPort);
		SetDlgItemInt(IDC_EDIT_NTPINTERVAL, tNTPInfo.iIntervalHour);
		SetDlgItemInt(IDC_EDIT_NTPINTERVAL1, tNTPInfo.iIntervalSec);
		if (IsValidIPv6(tNTPInfo.cServerIPv6) >= 1) {
			SetDlgItemText(IDC_EDIT_NTPIP, tNTPInfo.cServerIPv6);
		} else {
			SetDlgItemText(IDC_EDIT_NTPIP, tNTPInfo.cServerIP);
		}
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetNTPInfo(%d,%s,%d,%d)",m_iLogonID,tNTPInfo.cServerIP,tNTPInfo.iServerPort,tNTPInfo.iIntervalSec);
	} else {
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetNTPInfo(%d,%s,%d,%d)",m_iLogonID,tNTPInfo.cServerIP,tNTPInfo.iServerPort,tNTPInfo.iIntervalSec);
	}
#endif

	return TRUE;
}

void CLS_NTPPage::OnBnClickedButtonNtp()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	//char cNTPIP[32] = {0};
	//GetDlgItemText(IDC_EDIT_NTPIP, cNTPIP, 32);
	//int iNTPPort = GetDlgItemInt(IDC_EDIT_NTPPORT);
	//int iInterval = GetDlgItemInt(IDC_EDIT_NTPINTERVAL);
	//unsigned short usNTPPort = iNTPPort;
	//int iRet = NetClient_SetNTPInfo(m_iLogonID, cNTPIP, usNTPPort, iInterval);

	int iRet = RET_FAILED;
#ifdef XML_PROTOCOL
	XmlNtpServer tXmlNtpServer = {0};
	GetDlgItemText(IDC_EDIT_NTPIP, tXmlNtpServer.cNtpServerIp, MAX_IPADDRESS_LEN);
	tXmlNtpServer.iNtpServerPort = GetDlgItemInt(IDC_EDIT_NTPPORT);
	tXmlNtpServer.iNtpInterval = GetDlgItemInt(IDC_EDIT_NTPINTERVAL);
	iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_NTP_SERVER, &tXmlNtpServer, sizeof(XmlNtpServer), NULL, 0);
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_XmlSetDevConfig:NETXMLCFG_DEVICE_SYSTIME(%d).", m_iLogonID);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_XmlSetDevConfig:NETXMLCFG_DEVICE_SYSTIME(%d).", m_iLogonID);
	}
#else
	NTPInfo tNTPInfo = {0};
	tNTPInfo.iBufSize = (int)sizeof(NTPInfo);
	GetDlgItemText(IDC_EDIT_NTPIP, tNTPInfo.cServerIP, 32);
	tNTPInfo.iServerPort = GetDlgItemInt(IDC_EDIT_NTPPORT);
	tNTPInfo.iIntervalHour = GetDlgItemInt(IDC_EDIT_NTPINTERVAL);
	tNTPInfo.iIntervalSec = GetDlgItemInt(IDC_EDIT_NTPINTERVAL1);
	GetDlgItemText(IDC_EDIT_NTPIP, tNTPInfo.cServerIPv6, 64);
	if (IsValidIPv6(tNTPInfo.cServerIPv6) >= 1) {
		memset(tNTPInfo.cServerIP,0,sizeof(tNTPInfo.cServerIP));
	} else {
		memset(tNTPInfo.cServerIPv6,0,sizeof(tNTPInfo.cServerIPv6));
	}

	if (tNTPInfo.iServerPort < 0 || tNTPInfo.iServerPort > 65535) {
		AddLog(LOG_TYPE_MSG,"","Please input a valid NTP port between 0 and 65535");
		return;
	}
	if (tNTPInfo.iIntervalHour < 0 || tNTPInfo.iIntervalHour > 65535) {
		AddLog(LOG_TYPE_MSG,"","Please input a valid interval between 0 and 65535");
		return;
	}
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_NTP_INFO, m_iChannelNo, &tNTPInfo, sizeof(NTPInfo));
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetNTPInfo(%d,%s,%d,%d)",m_iLogonID,tNTPInfo.cServerIP,tNTPInfo.iServerPort,tNTPInfo.iIntervalSec);
	} else {
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetNTPInfo(%d,%s,%d,%d)",m_iLogonID,tNTPInfo.cServerIP,tNTPInfo.iServerPort,tNTPInfo.iIntervalSec);
	}
#endif
}

void CLS_NTPPage::OnBnClickedBtnLanNtpTest()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iNTPPort = GetDlgItemInt(IDC_EDIT_NTPPORT);
	if (iNTPPort < 0 || iNTPPort > 65535)
	{
		AddLog(LOG_TYPE_MSG,"","Please input a valid NTP port between 0 and 65535");
		return;
	}

	NTPTest tNTPTest = {0};
	char cNTPIP[LEN_64] = {0};
	GetDlgItemText(IDC_EDIT_NTPIP, cNTPIP, LEN_64);
	if (IsValidIPv6(cNTPIP) >= 1)
	{
		strncpy(tNTPTest.pcServerIPv6, cNTPIP, LEN_64);
	}
	else
	{
		strncpy(tNTPTest.cServerIP, cNTPIP, LEN_32);
	}
	tNTPTest.iServerPort = GetDlgItemInt(IDC_EDIT_NTPPORT);
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_NTP_TEST,  m_iChannelNo, &tNTPTest, sizeof(tNTPTest));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand(%d,%d)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetNTPInfo(%d,%d)",m_iLogonID,m_iChannelNo);
	}
}


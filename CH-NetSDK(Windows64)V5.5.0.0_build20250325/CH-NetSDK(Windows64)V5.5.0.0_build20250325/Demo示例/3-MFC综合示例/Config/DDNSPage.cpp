// DDNSPage.cpp : implementation file
//

#include "stdafx.h"
#include "DDNSPage.h"

// CLS_DDNSPage dialog

IMPLEMENT_DYNAMIC(CLS_DDNSPage, CDialog)

CLS_DDNSPage::CLS_DDNSPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DDNSPage::IDD, pParent)
{

}

CLS_DDNSPage::~CLS_DDNSPage()
{
}

void CLS_DDNSPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_USERNAME, m_edtUserName);
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_edtPassword);
	DDX_Control(pDX, IDC_EDIT_NVSNAME, m_edtNvsName);
	DDX_Control(pDX, IDC_COMBO_DDNSSERVER, m_cboDDNSServer);
    DDX_Control(pDX, IDC_COMBO_DDNSTYPE, m_cboDDNSType);
	DDX_Control(pDX, IDC_EDIT_DDNSPORT, m_edtDDNSPort);
	DDX_Control(pDX, IDC_CHECK_ENABLE, m_chkEnable);
	DDX_Control(pDX, IDC_BUTTON_DDNS, m_btnDDNS);
}


BEGIN_MESSAGE_MAP(CLS_DDNSPage, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_DDNS, &CLS_DDNSPage::OnBnClickedButtonDdns)
    ON_BN_CLICKED(IDC_BUTTON_DDNSTEST, &CLS_DDNSPage::OnBnClickedButtonDdnstest)
    ON_BN_CLICKED(IDC_BUTTON_EASYDDNS_STATE, &CLS_DDNSPage::OnBnClickedButtonEasyddnsState)
END_MESSAGE_MAP()


// CLS_DDNSPage message handlers
BOOL CLS_DDNSPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtUserName.SetLimitText(LEN_128);
	m_edtPassword.SetLimitText(LEN_128);
	m_edtNvsName.SetLimitText(LEN_128);
	m_cboDDNSServer.AddString("www.3322.org");
	m_cboDDNSServer.AddString("www.ChangeIP.org");
	m_cboDDNSServer.AddString("www.freeDns.org");
	m_cboDDNSServer.AddString("dynupdate.no-ip.com");
	m_cboDDNSServer.AddString("members.dyndns.org");
	m_cboDDNSServer.SetCurSel(0);
	m_edtDDNSPort.SetLimitText(5);

	UI_UpdateDialog();

	return TRUE;
}

void CLS_DDNSPage::OnChannelChanged(int _iLogonID,int /*_iChannelNo*/,int /*_iStreamNo*/)
{
	m_iLogonID = _iLogonID;
	
	UI_UpdateDDNS();
}

void CLS_DDNSPage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_DDNSPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_USERNAME, IDS_CONFIG_FTP_USER);
	SetDlgItemTextEx(IDC_STATIC_PASSWORD, IDS_CONFIG_FTP_PASSWORD);
	SetDlgItemTextEx(IDC_STATIC_NVSNAME, IDS_CONFIG_DDNS_NVSNAME);
	SetDlgItemTextEx(IDC_STATIC_DDNSSERVER, IDS_CONFIG_DDNS_DDNSSERVER);
	SetDlgItemTextEx(IDC_STATIC_DDNSPORT, IDS_CONFIG_DDNS_DDNSPORT);
	SetDlgItemTextEx(IDC_CHECK_ENABLE, IDS_CONFIG_FTP_SNAPSHOT_ENABLE);
	SetDlgItemTextEx(IDC_BUTTON_DDNS, IDS_SET);
	SetDlgItemText(IDC_BUTTON_DDNSTEST, GetTextByLan(_T("测试"), _T("test")));
	SetDlgItemText(IDC_BUTTON_EASYDDNS_STATE, GetTextByLan(_T("EasyDDNS连接状态"), _T("EasyDDNS connection state")));
	m_cboDDNSType.ResetContent();
	m_cboDDNSType.SetItemData(0, m_cboDDNSType.AddString(GetTextByLan(_T("通用域名"), _T("CommonDomain"))));
	m_cboDDNSType.SetItemData(1, m_cboDDNSType.AddString(GetTextByLan(_T("EasyDDNS"), _T("EasyDDNS"))));
	m_cboDDNSType.SetCurSel(0);
}

BOOL CLS_DDNSPage::UI_UpdateDDNS()
{
	if (m_iLogonID < 0)
		return FALSE;

    int iRet = -1;
    // Get DDNSEx, compatible with old DDNS interface
    {
        DDNSParaEx tParam = {0};
        tParam.iSize = sizeof(tParam);
        int lpBytesReturned = 0;
        iRet = NetClient_GetDevConfig(m_iLogonID,  NET_CLIENT_DDNS_EX, -1, &tParam, tParam.iSize, &lpBytesReturned);
        if (0 == iRet)
        {
            SetDlgItemText(IDC_EDIT_USERNAME, tParam.cUserName);
            SetDlgItemText(IDC_EDIT_PASSWORD, tParam.cPassWord);
            SetDlgItemText(IDC_EDIT_NVSNAME, tParam.cNVSName);
            SetDlgItemInt(IDC_EDIT_DDNSPORT, tParam.iPort);
            m_cboDDNSServer.SelectString(-1, tParam.cDomainName);
            m_chkEnable.SetCheck((tParam.iEnable == 1)?BST_CHECKED:BST_UNCHECKED);
            m_cboDDNSType.SetCurSel(tParam.iType);
            AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d,%s,%s,%s,%s,%d,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNVSName,tParam.cDomainName,tParam.iPort,tParam.iEnable,tParam.iType);
        }
        else
        {
            AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%s,%s,%s,%s,%d,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNVSName,tParam.cDomainName,tParam.iPort,tParam.iEnable,tParam.iType);
        }

        memset(&tParam, 0, sizeof(tParam));
        tParam.iSize = sizeof(tParam);
        tParam.iType = DDNSTYPE_EASY;
        iRet = NetClient_GetDevConfig(m_iLogonID,  NET_CLIENT_DDNS_EX, -1, &tParam, tParam.iSize, &lpBytesReturned);
        if (0 == iRet)
        {
            AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d,%s,%s,%s,%s,%d,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNVSName,tParam.cDomainName,tParam.iPort,tParam.iEnable,tParam.iType);
        }
        else
        {
            AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%s,%s,%s,%s,%d,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNVSName,tParam.cDomainName,tParam.iPort,tParam.iEnable,tParam.iType);
        }
    }
	return TRUE;
}
void CLS_DDNSPage::OnBnClickedButtonDdns()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

    int iRet = -1;

    // Compatible with old interface DDNS
    {
        DDNSParaEx tParam = {0};
        tParam.iSize = sizeof(tParam);
        GetDlgItemText(IDC_EDIT_USERNAME, tParam.cUserName, LEN_128);
        GetDlgItemText(IDC_EDIT_PASSWORD, tParam.cPassWord, LEN_128);
        GetDlgItemText(IDC_EDIT_NVSNAME, tParam.cNVSName, LEN_128);
        GetDlgItemText(IDC_COMBO_DDNSSERVER, tParam.cDomainName, LEN_128);
        tParam.iPort = GetDlgItemInt(IDC_EDIT_DDNSPORT);
        tParam.iEnable = (m_chkEnable.GetCheck() == BST_CHECKED)?1:0;
        tParam.iType = m_cboDDNSType.GetCurSel();

        iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DDNS_EX, -1, &tParam, tParam.iSize);
        if (0 == iRet)
        {
            AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig(%d,%s,%s,%s,%s,%d,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNVSName,tParam.cDomainName,tParam.iPort,tParam.iEnable,tParam.iType);
        }
        else
        {
            AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig(%d,%s,%s,%s,%s,%d,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNVSName,tParam.cDomainName,tParam.iPort,tParam.iEnable,tParam.iType);
        }
    }
}

void CLS_DDNSPage::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
    switch(_wParam)
    {
    case WCM_DDNS_TEST:
        {
            int iRetNum = (int)_iLParam;
            AddLog(LOG_TYPE_SUCC,"","[CLS_DDNSPage::OnMainNotify]->WCM_DDNS_TEST(%d,%d)",_iLogonID,iRetNum);
        }
        break;
    case WCM_EASYDDNS_LINKSTATE:
        {
            EasyDDNSLinkState *pLinkState = (EasyDDNSLinkState*)_iLParam;
            AddLog(LOG_TYPE_SUCC,"","[CLS_DDNSPage::OnMainNotify]->WCM_EASYDDNS_LINKSTATE(%d,%d)",_iLogonID,pLinkState->iLinkState);
        }
        break;
    default:
        break;
    }

}

void CLS_DDNSPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    if (_iLogonID < 0)
    {
        AddLog(LOG_TYPE_MSG,"","[CLS_DDNSPage::OnParamChangeNotify]->Invalid logon id(%d)", _iLogonID);
        return;
    }

    switch(_iParaType)
    {
    case PARA_DDNS_EX:
        {
            AddLog(LOG_TYPE_SUCC,"","[CLS_DDNSPage::OnParamChangeNotify]->PARA_DDNS_EX(%d,%d)",_iLogonID,_iChannelNo);
            UI_UpdateDDNS();
        }
        break;
    case PARA_DDNSCHANGED:
        {
            AddLog(LOG_TYPE_SUCC,"","[CLS_DDNSPage::OnParamChangeNotify]->PARA_DDNSCHANGED(%d,%d)",_iLogonID,_iChannelNo);
        }
    default:
        break;
    }
}



void CLS_DDNSPage::OnBnClickedButtonDdnstest()
{
   
    DDNSTest tParam = {0};
    tParam.iBufSize = sizeof(tParam);
    GetDlgItemText(IDC_EDIT_USERNAME, tParam.cUserName, LEN_128);
    GetDlgItemText(IDC_EDIT_PASSWORD, tParam.cPassWord, LEN_128);
    GetDlgItemText(IDC_EDIT_NVSNAME, tParam.cNvsName, LEN_128);
    GetDlgItemText(IDC_COMBO_DDNSSERVER, tParam.cDomainName, LEN_128);
    tParam.iPort = GetDlgItemInt(IDC_EDIT_DDNSPORT);
    tParam.iType = m_cboDDNSType.GetCurSel();

    int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_DDNSTEST,  -1,  &tParam,  tParam.iBufSize);
    if (0 == iRet)
    {
        AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand(%d,%s,%s,%s,%s,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNvsName,tParam.cDomainName,tParam.iPort,tParam.iType);
    }
    else
    {
        AddLog(LOG_TYPE_FAIL,"","NetClient_SendCommand(%d,%s,%s,%s,%s,%d,%d)",m_iLogonID,tParam.cUserName,tParam.cPassWord,tParam.cNvsName,tParam.cDomainName,tParam.iPort,tParam.iType);
    }
}

void CLS_DDNSPage::OnBnClickedButtonEasyddnsState()
{
    EasyDDNSLinkState tParam = {0};
    tParam.iSize = sizeof(tParam);
    tParam.iLinkState = -1;
    int iRet = NetClient_RecvCommand(m_iLogonID, COMMAND_ID_EASYDDNS_LINKSTATE,  -1,  &tParam,  tParam.iSize);
    if (0 == iRet)
    {
        AddLog(LOG_TYPE_SUCC,"","NetClient_RecvCommand(%d,%d)",m_iLogonID,tParam.iLinkState);
    }
    else
    {
        AddLog(LOG_TYPE_FAIL,"","NetClient_RecvCommand(%d,%d)",m_iLogonID,tParam.iLinkState);
    }
}

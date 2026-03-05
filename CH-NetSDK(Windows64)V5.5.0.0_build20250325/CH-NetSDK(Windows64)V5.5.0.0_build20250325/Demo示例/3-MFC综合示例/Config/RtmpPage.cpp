
#include "stdafx.h"
#include "RtmpPage.h"

#define RTMP_PUSH_MAIN_STREAM		1
#define RTMP_PUSH_SUB_STREAM		2
#define RTMP_PUSH_THREED_STREAM     3

#define TYPE_CUSTOM       1
#define TYPE_NON_CUSTOM   2

#define LIVE_ADDRESS_LIMIT_TEXT		255
#define LIVE_AUTH_KEY_LIMIT_TEXT	50


IMPLEMENT_DYNAMIC(CLS_RtmpPage, CDialog)

CLS_RtmpPage::CLS_RtmpPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_RtmpPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

CLS_RtmpPage::~CLS_RtmpPage()
{
}

void CLS_RtmpPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_STREAM_TYPE, m_cboPushStreamType);


	DDX_Control(pDX, IDC_CHECK_NET_LIVE, m_chkLiveEnable);
	DDX_Control(pDX, IDC_EDIT_LIVEADDR, m_edtLiveAddr);
	DDX_Control(pDX, IDC_EDIT_AUTH_KEY, m_edtAuthKey);
    DDX_Control(pDX, IDC_EDIT_RTMPNO, m_edtRtmpNo);
    DDX_Control(pDX, IDC_EDIT_RTMPCHN, m_edtRtmpChn);
    DDX_Control(pDX, IDC_EDIT_RTMPSNDTMOUT, m_edtRtmpSndTmOut);
    DDX_Control(pDX, IDC_EDIT_RTMPRCVTMOUT, m_edtRtmpRcvTmOut);
    DDX_Control(pDX, IDC_CBO_TYPE, m_cboType);
    DDX_Control(pDX, IDC_EDIT_PORT, m_edtPort);
    DDX_Control(pDX, IDC_EDIT_USERNAME, m_edtUserName);
    DDX_Control(pDX, IDC_EDIT_USERPASSWD, m_edtUserPassword);
}


BEGIN_MESSAGE_MAP(CLS_RtmpPage, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_RTMP_SET, &CLS_RtmpPage::OnBnClickedBtnRtmpSet)
    ON_BN_CLICKED(IDC_BTN_RTMP_CLIENT_LINKSTATE, &CLS_RtmpPage::OnBnClickedBtnRtmpClientLinkstate)
END_MESSAGE_MAP()


BOOL CLS_RtmpPage::OnInitDialog()		
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();

	return TRUE;
}


void CLS_RtmpPage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	
	UI_UpdateRtmpInfo();
}

void CLS_RtmpPage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = (_iChannelNo >= 0) ? _iChannelNo : 0;

	UI_UpdateRtmpInfo();
}

void CLS_RtmpPage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_RtmpPage::UI_UpdateDialog()
{

	int iCurIndex = m_cboPushStreamType.GetCurSel();
	m_cboPushStreamType.ResetContent();
	m_cboPushStreamType.SetItemData(m_cboPushStreamType.AddString(GetTextEx(IDS_MAJOR)), RTMP_PUSH_MAIN_STREAM);
	m_cboPushStreamType.SetItemData(m_cboPushStreamType.AddString(GetTextEx(IDS_MINOR)), RTMP_PUSH_SUB_STREAM);
    m_cboPushStreamType.SetItemData(m_cboPushStreamType.AddString(GetTextByLan(_T("三码流"), _T("3rd Stream"))), RTMP_PUSH_THREED_STREAM);
	iCurIndex = (iCurIndex < m_cboPushStreamType.GetCount() && iCurIndex >= 0) ? iCurIndex : 0;
	m_cboPushStreamType.SetCurSel(0);

    m_cboType.ResetContent();
    m_cboType.SetItemData(m_cboType.AddString(GetTextByLan(_T("自定义"), _T("Custom"))), TYPE_CUSTOM);
    m_cboType.SetItemData(m_cboType.AddString(GetTextByLan(_T("非自定义"), _T("Non-custom"))), TYPE_NON_CUSTOM);
    m_cboType.SetCurSel(0);

	SetDlgItemTextEx(IDC_BTN_RTMP_SET, IDS_SET);
 	SetDlgItemTextEx(IDC_CHECK_NET_LIVE, IDS_CONFIG_FTP_SNAPSHOT_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_LIVEADDR, IDS_LIVE_ADDR);
	SetDlgItemTextEx(IDC_STATIC_AUTH_KEY, IDS_LIVE_AUTH_KEY);
	SetDlgItemTextEx(IDC_STATIC_STREAM_TYPE, IDS_CONFIG_ADV_STREAMTYPE);
    
    SetDlgItemText(IDC_STATIC_RTMPNO, GetTextByLan(_T("编号"), _T("RtmpNo")));
    SetDlgItemText(IDC_STATIC_RTMPCHN, GetTextByLan(_T("通道号"), _T("RtmpChn")));
    SetDlgItemText(IDC_STATIC_RTMPSNDTMOUT, GetTextByLan(_T("rtmp发送超时时间"), _T("Rtmp Send Timeout")));
    SetDlgItemText(IDC_STATIC_RTMPRCVTMOUT, GetTextByLan(_T("rtmp接收超时时间"), _T("Rtmp Recv Timeout")));
    SetDlgItemText(IDC_STATIC_TYPE, GetTextByLan(_T("当前地址类型"), _T("Type")));
    SetDlgItemText(IDC_STATIC_PORT, GetTextByLan(_T("端口号"), _T("Port")));
    SetDlgItemText(IDC_STATIC_USERNAME, GetTextByLan(_T("用户名"), _T("Username")));
    SetDlgItemText(IDC_STATIC_USERPASSWD, GetTextByLan(_T("密码"), _T("Password")));

    SetDlgItemText(IDC_BTN_RTMP_CLIENT_LINKSTATE, GetTextByLan(_T("获取RTMP状态"), _T("Get Rtmp State")));
	m_edtLiveAddr.SetLimitText(LIVE_ADDRESS_LIMIT_TEXT);
	m_edtAuthKey.SetLimitText(LIVE_AUTH_KEY_LIMIT_TEXT);
    m_edtUserName.SetLimitText(LEN_64);
    m_edtUserPassword.SetLimitText(LEN_64);

}

void CLS_RtmpPage::UI_UpdateRtmpInfo(int iRtmpChn)
{
	if (0 > m_iLogonID)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_RtmpPage::UI_UpdateRtmpInfo] Invalid logon id(%d)", m_iLogonID);
		return;
	}

	int iBytesReturned = 0;
	RtmpInfo tRtmpInfo = {0};
	tRtmpInfo.iBufSize = sizeof(RtmpInfo);
    tRtmpInfo.iRtmpNo = iRtmpChn;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RTMP_URL_INFO, 0, &tRtmpInfo, sizeof(RtmpInfo), &iBytesReturned);
	if (RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig:RTMP_URL_INFO, Get fail,error = %d", GetLastError());
	}
	else
	{

		m_chkLiveEnable.SetCheck(tRtmpInfo.iRtmpEnable);
		m_cboPushStreamType.SetCurSel(GetCboSel(&m_cboPushStreamType, tRtmpInfo.iStreamType));
		m_edtLiveAddr.SetWindowText(tRtmpInfo.cRtmpUrl);
		m_edtAuthKey.SetWindowText(tRtmpInfo.cRtmpKey);

        CString cTemp;
        IntToCString(tRtmpInfo.iRtmpNo,&cTemp);
        m_edtRtmpNo.SetWindowText(cTemp);
        IntToCString(tRtmpInfo.iRtmpChnNo,&cTemp);
        m_edtRtmpChn.SetWindowText(cTemp);
        IntToCString(tRtmpInfo.iRtmpSndTmOut,&cTemp);
        m_edtRtmpSndTmOut.SetWindowText(cTemp);
        IntToCString(tRtmpInfo.iRtmpRCVTmOut,&cTemp);
        m_edtRtmpRcvTmOut.SetWindowText(cTemp);
        m_cboType.SetCurSel(GetCboSel(&m_cboType, tRtmpInfo.iType));
        IntToCString(tRtmpInfo.iPort,&cTemp);
        m_edtPort.SetWindowText(cTemp);
        m_edtUserName.SetWindowText(tRtmpInfo.cUserName);
        m_edtUserPassword.SetWindowText(tRtmpInfo.cUserPasswd);
        AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig:RTMP_URL_INFO(%d,%d,%d,%s,%s,%d,%d,%d,%d,%d,%s,%s)", tRtmpInfo.iRtmpNo, tRtmpInfo.iRtmpChnNo, tRtmpInfo.iRtmpEnable, tRtmpInfo.cRtmpUrl, tRtmpInfo.cRtmpKey, tRtmpInfo.iRtmpSndTmOut, tRtmpInfo.iRtmpRCVTmOut, tRtmpInfo.iStreamType, tRtmpInfo.iType, tRtmpInfo.iPort, tRtmpInfo.cUserName, tRtmpInfo.cUserPasswd);
	}

	return;
}


void CLS_RtmpPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    if (0 > _iLogonID)
    {
        AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)", _iLogonID);
        return;
    }

    switch (_iParaType)
    {
    case  PARA_RTMP_URL_INFO:
        {
            RtmpInfo* pTemp = (RtmpInfo*)_pPara;
            AddLog(LOG_TYPE_MSG,"","OnParamChangeNotify, logon id(%d), iRtmpChnNo(%d)", _iLogonID, pTemp->iRtmpChnNo);
            UI_UpdateRtmpInfo(pTemp->iRtmpChnNo);
        }
        break;
    default:
        break;
    }
}

void CLS_RtmpPage::OnBnClickedBtnRtmpSet()
{
	if (0 > m_iLogonID)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_RtmpPage::OnBnClickedBtnRtmpSet] Invalid logon id(%d)", m_iLogonID);
		return;
	}

	RtmpInfo tRtmpInfo = {0};
	tRtmpInfo.iBufSize = sizeof(RtmpInfo);
    tRtmpInfo.iRtmpNo = GetDlgItemInt(IDC_EDIT_RTMPNO);
	tRtmpInfo.iRtmpEnable = (BST_CHECKED == m_chkLiveEnable.GetCheck()) ? 1 : 0;
	tRtmpInfo.iRtmpChnNo = GetDlgItemInt(IDC_EDIT_RTMPCHN);
	m_edtLiveAddr.GetWindowText(tRtmpInfo.cRtmpUrl,sizeof(tRtmpInfo.cRtmpUrl));
	m_edtAuthKey.GetWindowText(tRtmpInfo.cRtmpKey,sizeof(tRtmpInfo.cRtmpKey));
    tRtmpInfo.iRtmpSndTmOut = GetDlgItemInt(IDC_EDIT_RTMPSNDTMOUT);
    tRtmpInfo.iRtmpRCVTmOut = GetDlgItemInt(IDC_EDIT_RTMPRCVTMOUT);
    tRtmpInfo.iStreamType = m_cboPushStreamType.GetItemData(m_cboPushStreamType.GetCurSel());
    tRtmpInfo.iType = m_cboType.GetItemData(m_cboType.GetCurSel());
    tRtmpInfo.iPort = GetDlgItemInt(IDC_EDIT_PORT);
    m_edtUserName.GetWindowText(tRtmpInfo.cUserName,sizeof(tRtmpInfo.cUserName));
    m_edtUserPassword.GetWindowText(tRtmpInfo.cUserPasswd,sizeof(tRtmpInfo.cUserPasswd));
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_RTMP_URL_INFO, 0, &tRtmpInfo, sizeof(RtmpInfo));
	if (RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig:RTMP_URL_INFO, Set fail,error = %d", GetLastError());
	}
	else
	{
        AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig:RTMP_URL_INFO, iRtmpNo(%d),iRtmpEnable(%d), iRtmpChnNo(%d),cRtmpUrl(%s),cRtmpKey(%s),iRtmpSndTmOut(%d),iRtmpRCVTmOut(%d),iStreamType(%d),iType(%d),iPort(%d),cUserName(%s),cUserPasswd(%s)",
            tRtmpInfo.iRtmpNo, tRtmpInfo.iRtmpEnable, tRtmpInfo.iRtmpChnNo, tRtmpInfo.cRtmpUrl, tRtmpInfo.cRtmpKey, tRtmpInfo.iRtmpSndTmOut, tRtmpInfo.iRtmpRCVTmOut, tRtmpInfo.iStreamType, tRtmpInfo.iType, tRtmpInfo.iPort, tRtmpInfo.cUserName, tRtmpInfo.cUserPasswd);
	}
	return;
}

void CLS_RtmpPage::OnBnClickedBtnRtmpClientLinkstate()
{
    if (0 > m_iLogonID)
    {
        AddLog(LOG_TYPE_FAIL,"","[CLS_RtmpPage::OnBnClickedBtnRtmpClientLinkstate] Invalid logon id(%d)", m_iLogonID);
        return;
    }
    
    RtmpClientLinkState tRtmpInfo = {0};
    tRtmpInfo.iSize = sizeof(RtmpClientLinkState);
    int lpReturnByte = 0;
    int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RTMPCLIENT_LINKSTATE, m_iChannelNo, &tRtmpInfo, sizeof(RtmpClientLinkState), &lpReturnByte);
    if (RET_SUCCESS > iRet)
    {
        AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig:NET_CLIENT_RTMPCLIENT_LINKSTATE, Set fail,error = %d", GetLastError());
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig:NET_CLIENT_RTMPCLIENT_LINKSTATE,channel(%d), state(%d)", m_iChannelNo, tRtmpInfo.iStateValue);
    }
    return;
}

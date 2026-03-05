#include "stdafx.h"
#include "NetClientDemo.h"
#include "PortMapping.h"

// CLS_LinkHttp dialog

IMPLEMENT_DYNAMIC(CLS_PortMapping, CDialog)

CLS_PortMapping::CLS_PortMapping(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_PortMapping::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

CLS_PortMapping::~CLS_PortMapping()
{
	
}

void CLS_PortMapping::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_LAN_PORT_PORTTYPE, m_cboPortType);
	DDX_Control(pDX, IDC_CHK_LAN_PORT_ENABLE, m_chkLanPortEnalbe);
	DDX_Control(pDX, IDC_BTN_LAN_PORT_SET, m_btnLanPortSet);
	DDX_Control(pDX, IDC_EDT_LAN_PORT_PORTMAPPING, m_edtLanMapPort);

    DDX_Control(pDX, IDC_BTN_LAN_HTTP_PORT_SET, m_btnLanPortSet2);
    DDX_Control(pDX, IDC_LST_CFG_LAN_PORT_LIST, m_lstNpupList);

}

BEGIN_MESSAGE_MAP(CLS_PortMapping, CDialog)
	ON_BN_CLICKED(IDC_BTN_LAN_PORT_SET, &CLS_PortMapping::OnBnClickedBtnLanPortSet)
	ON_CBN_SELCHANGE(IDC_COMBO_LAN_PORT_PORTTYPE, &CLS_PortMapping::OnCbnSelchangeComboLanPortPorttype)
    ON_BN_CLICKED(IDC_BTN_LAN_HTTP_PORT_SET, &CLS_PortMapping::OnBnClickedBtnLanHttpPortSet)
END_MESSAGE_MAP()

BOOL CLS_PortMapping::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialogText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_PortMapping::UI_UpdateDialogText()
{
	SetDlgItemTextEx(IDC_GBO_LAN_PORT_PORTMAPPING,IDS_CONFIG_LAN_PORT_CBOPORTMAPPING);
	SetDlgItemTextEx(IDC_CHK_LAN_PORT_ENABLE,IDS_CONFIG_LAN_PORT_CHKENABLE);
	SetDlgItemTextEx(IDC_STC_LAN_PORT_PORTMAPPING,IDS_CONFIG_LAN_PORT_STCPORTMAPPING);
	SetDlgItemTextEx(IDC_STC_LAN_PORT_PORTTYPE,IDS_CONFIG_LAN_PORT_STCPORTTYPE);
	SetDlgItemTextEx(IDC_BTN_LAN_PORT_SET,IDS_CONFIG_LAN_PORT_BTNSET);
	SetDlgItemTextEx(IDC_STC_LAN_PORT_SRC_PORT, IDS_CONFIG_LAN_SRC_PORT);
    SetDlgItemText(IDC_STATIC_TIP, GetTextByLan(_T("注意：修改RTMP端口需要重启设备才能生效！"), _T("Attention:reboot device for RTMP changing")));

	InsertString(m_cboPortType,0,IDS_CONFIG_LAN_PORT_PORTTYPEH);
	InsertString(m_cboPortType,1,IDS_CONFIG_LAN_PORT_PORTTYPEDATA);
	InsertString(m_cboPortType,2,IDS_CONFIG_LAN_PORT_PORTTYPEUDP);
	InsertString(m_cboPortType,3,GetTextByLan("雷达外设端口", "Radar peripheral port"));
	m_cboPortType.SetCurSel(0);

    SetDlgItemTextEx(IDC_GBO_LAN_PORT_SET, IDS_GBO_LAN_PORT_SET);
    SetDlgItemTextEx(IDC_BTN_LAN_HTTP_PORT_SET,IDS_CONFIG_LAN_PORT_BTNSET);

    UI_UpdateListInfo();
}

void CLS_PortMapping::UI_UpdateListInfo()
{
    if (m_iLogonID < 0)
    {
        //AddLog(LOG_TYPE_FAIL,"","[CLS_PortMapping::UI_UpdateListInfo]Invalid logon id(%d)", m_iLogonID);
        return;
    }

    // port config
    m_lstNpupList.SetExtendedStyle(LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT);
    m_lstNpupList.DeleteAllColumns();
    m_lstNpupList.InsertColumn(n_LIST_PORT_PORTTYPE, GetTextEx(IDS_CFG_LAN_UPNP_LIST_PORTTYPE), LVCFMT_CENTER, 100);
    m_lstNpupList.InsertColumn(n_LIST_PORT_VALUE, GetTextEx(IDS_CFG_LAN_UPNP_LIST_INPORT), LVCFMT_CENTER, 100);

    m_lstNpupList.DeleteAllItems();
    m_lstNpupList.InsertItem(n_LIST_HTTP, "HTTP");
    m_lstNpupList.InsertItem(n_LIST_RTSP, "RTSP");
    m_lstNpupList.InsertItem(n_LIST_SCHEDULE, "SCHEDULE");
    m_lstNpupList.InsertItem(n_LIST_SERVER, "SERVER");
    m_lstNpupList.InsertItem(n_LIST_HTTPS, "HTTPS");
    m_lstNpupList.InsertItem(n_LIST_RTMP, "RTMP");

    m_lstNpupList.SetItemControl(EDITBOX, n_LIST_PORT_VALUE);
    THttpPort tHttpPortParam = {0};
    memset(&tHttpPortParam, 0, sizeof(THttpPort));
    tHttpPortParam.iSize = sizeof(THttpPort);
    int iReturnByte = 0;
    int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLINET_HTTPPORT, m_iChannelNo, &tHttpPortParam, sizeof(THttpPort), &iReturnByte);
    if (RET_SUCCESS > iRet)
    {
        AddLog(LOG_TYPE_FAIL, "", "[CLS_PortMapping::UI_UpdateListInfo][NET_CLINET_HTTPPORT] get fail,error = %d", GetLastError());
    }
    else
    {
        char cPortTemp[16] = {0};
        sprintf(cPortTemp, "%d", tHttpPortParam.iPort);
        m_lstNpupList.SetItemText(n_LIST_HTTP, n_LIST_PORT_VALUE, cPortTemp);
        sprintf(cPortTemp, "%d", tHttpPortParam.iRtspPort);
        m_lstNpupList.SetItemText(n_LIST_RTSP, n_LIST_PORT_VALUE, cPortTemp);
        sprintf(cPortTemp, "%d", tHttpPortParam.iSchedulePort);
        m_lstNpupList.SetItemText(n_LIST_SCHEDULE, n_LIST_PORT_VALUE, cPortTemp);
        sprintf(cPortTemp, "%d", tHttpPortParam.iServerPort);
        m_lstNpupList.SetItemText(n_LIST_SERVER, n_LIST_PORT_VALUE, cPortTemp);
        sprintf(cPortTemp, "%d", tHttpPortParam.iHttpsport);
        m_lstNpupList.SetItemText(n_LIST_HTTPS, n_LIST_PORT_VALUE, cPortTemp);
        sprintf(cPortTemp, "%d", tHttpPortParam.iRtmpServerPort);
        m_lstNpupList.SetItemText(n_LIST_RTMP, n_LIST_PORT_VALUE, cPortTemp);
        AddLog(LOG_TYPE_SUCC, "", "[CLS_PortMapping::UI_UpdateListInfo][NET_CLINET_HTTPPORT] get success!");
    }
}

void CLS_PortMapping::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialogText();
	UI_UpdatePortMapInfo();
    UI_UpdateListInfo();
}

void CLS_PortMapping::OnBnClickedBtnLanPortSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_PortMapping::OnBnClickedBtnLanPortSet]Invalid logon id(%d)", m_iLogonID);
		return;
	}
	
	RouteNat tRouteNatInfo = {0};
	GetInfoByDialog(&tRouteNatInfo);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_ROUTE_NAT, m_iChannelNo, &tRouteNatInfo, sizeof(RouteNat));
	if (RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_PortMapping::OnBnClickedBtnLanPortSet][NET_CLIENT_ROUTE_NAT] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_PortMapping::OnBnClickedBtnLanPortSet][NET_CLIENT_ROUTE_NAT] set success!");
	}
}

void CLS_PortMapping::GetInfoByDialog(RouteNat *_pRouteNatInfo)
{
	if(NULL == _pRouteNatInfo)
	{	
		return;
	}

	int iMapPort = GetDlgItemInt(IDC_EDT_LAN_PORT_PORTMAPPING);
	int iEnable = m_chkLanPortEnalbe.GetCheck();
	int iIndex = m_cboPortType.GetCurSel();

	_pRouteNatInfo->iSize = sizeof(RouteNat);
	_pRouteNatInfo->iProxyPort = iMapPort;
	_pRouteNatInfo->iEnable = iEnable;
	_pRouteNatInfo->iPortType = iIndex + 1; //The port type starts from 1 and increments the index value by 1
	_pRouteNatInfo->iIpcSrcPort = GetDlgItemInt(IDC_EDT_LAN_PORT_SRC_PORT);
}

void CLS_PortMapping::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
{
	m_iLogonID = _iLogonID;

	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
	
	UI_UpdatePortMapInfo();
    UI_UpdateListInfo();
}

void CLS_PortMapping::UI_UpdatePortMapInfo()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_PortMapping::UI_UpdatePortMapInfo] Invalid logon id(%d)", m_iLogonID);
		return;
	}
	
	RouteNat tRouteNatParam = {0};
	tRouteNatParam.iSize = sizeof(RouteNat);
	tRouteNatParam.iPortType = m_cboPortType.GetCurSel() + 1;
	int iReturnByte = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ROUTE_NAT, m_iChannelNo, &tRouteNatParam, sizeof(RouteNat), &iReturnByte);
	if (RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_PortMapping::UI_UpdatePortMapInfo][NET_CLIENT_ROUTE_NAT] Get fail,error = %d", GetLastError());
	}
	else
	{
		m_chkLanPortEnalbe.SetCheck(tRouteNatParam.iEnable);
		SetDlgItemInt(IDC_EDT_LAN_PORT_PORTMAPPING, tRouteNatParam.iProxyPort);
		SetDlgItemInt(IDC_EDT_LAN_PORT_SRC_PORT, tRouteNatParam.iIpcSrcPort);
		AddLog(LOG_TYPE_SUCC, "", "[CLS_PortMapping::UI_UpdatePortMapInfo][NET_CLIENT_ROUTE_NAT] get success!");
	}
}

void CLS_PortMapping::OnCbnSelchangeComboLanPortPorttype()
{
	UI_UpdatePortMapInfo();
}

void CLS_PortMapping::OnBnClickedBtnLanHttpPortSet()
{
    if (m_iLogonID < 0)
    {
        AddLog(LOG_TYPE_FAIL,"","[CLS_PortMapping::OnBnClickedBtnLanPortSet2]Invalid logon id(%d)", m_iLogonID);
        return;
    }

    // TODO: Add your control notification handler code here
    m_lstNpupList.GetAndDisplayControlsData();
    m_lstNpupList.ShowControls(FALSE);


    THttpPort tPara = {0};
    tPara.iSize = sizeof(tPara);
    tPara.iPort = StrToInt(m_lstNpupList.GetItemText(n_LIST_HTTP, 1));
    tPara.iRtspPort = StrToInt(m_lstNpupList.GetItemText(n_LIST_RTSP, 1));
    tPara.iSchedulePort = StrToInt(m_lstNpupList.GetItemText(n_LIST_SCHEDULE, 1));
    tPara.iServerPort = StrToInt(m_lstNpupList.GetItemText(n_LIST_SERVER, 1));
    tPara.iHttpsport = StrToInt(m_lstNpupList.GetItemText(n_LIST_HTTPS, 1));
    tPara.iRtmpServerPort = StrToInt(m_lstNpupList.GetItemText(n_LIST_RTMP, 1));

    if(1>tPara.iPort || 65535<tPara.iPort 
        || 1>tPara.iRtspPort || 65535<tPara.iRtspPort
        || 1>tPara.iSchedulePort || 65535<tPara.iSchedulePort
        || 1>tPara.iServerPort || 65535<tPara.iServerPort
        || 1>tPara.iHttpsport || 65535<tPara.iHttpsport
        || 1>tPara.iRtmpServerPort || 65535 < tPara.iRtmpServerPort)
    {
        AddLog(LOG_TYPE_FAIL,"","[CLS_PortMapping::OnBnClickedBtnLanPortSet2]Invalid Port(%d)", m_iLogonID);
        return;
    }

    int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLINET_HTTPPORT, 0, &tPara, sizeof(tPara));
    if(TD_SUCCESS != iRet)
    {
        AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d,%d,%d,%d,%d,%d,%d)", m_iLogonID, tPara.iPort, tPara.iRtspPort, tPara.iSchedulePort, tPara.iServerPort, tPara.iHttpsport, tPara.iRtmpServerPort);
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d,%d,%d,%d,%d,%d,%d)", m_iLogonID, tPara.iPort, tPara.iRtspPort, tPara.iSchedulePort, tPara.iServerPort, tPara.iHttpsport, tPara.iRtmpServerPort);
    }
}

void CLS_PortMapping::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    if (_iLogonID < 0)
    {
        AddLog(LOG_TYPE_FAIL,"","Invalid logon id(%d)", _iLogonID);
        return;
    }

    switch (_iParaType)
    {
    case  PARA_PORTSET:
        {
            AddLog(LOG_TYPE_SUCC,"","[CLS_PortMapping][OnParamChangeNotify] logon id(%d)", _iLogonID);
            UI_UpdateListInfo();
        }
        break;
    default:
        break;
    }
}

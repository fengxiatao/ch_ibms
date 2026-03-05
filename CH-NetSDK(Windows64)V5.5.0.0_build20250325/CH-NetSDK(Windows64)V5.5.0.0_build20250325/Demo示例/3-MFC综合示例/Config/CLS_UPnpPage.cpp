//
#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_UPnpPage.h"

#define EFFECT 1
#define UPNPENABLE 0

#define COLUME_ITEM_0			1
#define COLUME_ITEM_1			2
#define COLUME_ITEM_2			3
#define COLUME_ITEM_3			4

IMPLEMENT_DYNAMIC(CLS_UPnpPage, CDialog)

CLS_UPnpPage::CLS_UPnpPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_UPnpPage::IDD, pParent)
	//, m_iEnableRadioGroup(0)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iEnableRadioGroup = -1;
}

CLS_UPnpPage::~CLS_UPnpPage()
{
}

void CLS_UPnpPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LST_CFG_LAN_UPNP_LIST, m_lstNpupList);
	DDX_Control(pDX, IDC_CBO_CFG_LAN_UPNP_MAPTYPE, m_cboUpnpMapType);
	DDX_Radio(pDX, IDC_RADIO_CFG_LAN_UPNP_NOTENABLE, m_iEnableRadioGroup);
}

BEGIN_MESSAGE_MAP(CLS_UPnpPage, CLS_BasePage)
	ON_BN_CLICKED(IDC_RADIO_CFG_LAN_UPNP_ENABLE, &CLS_UPnpPage::OnBnClickedRadioCfgLanUpnpEnable)
	ON_BN_CLICKED(IDC_RADIO_CFG_LAN_UPNP_NOTENABLE, &CLS_UPnpPage::OnBnClickedRadioCfgLanUpnpEnable)
    ON_BN_CLICKED(IDC_RADIO_CFG_LAN_UPNP_MANUAL, &CLS_UPnpPage::OnBnClickedRadioCfgLanUpnpEnable)
	ON_CBN_SELCHANGE(IDC_CBO_CFG_LAN_UPNP_MAPTYPE, &CLS_UPnpPage::OnCbnSelchangeCboCfgLanUpnpMaptype)
	ON_BN_CLICKED(IDC_BTN_CFG_LAN_UPNP_SAVE, &CLS_UPnpPage::OnBnClickedBtnCfgLanUpnpSave)
END_MESSAGE_MAP()

BOOL CLS_UPnpPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialogText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

static void GetManualPortMap(int _iLogonId, ManualPortMapResult& tResult)
{
    tResult.iSize = sizeof(tResult);
    int iByteReturn = -1;
    int iRet = NetClient_GetDevConfig(_iLogonId, NET_CLIENT_MANUAL_PORTMAP, 0, &tResult, sizeof(tResult), &iByteReturn);
    if(TD_SUCCESS != iRet)
    {
        AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig(%d,%d,%d,%d,%d,%d)", _iLogonId, tResult.iHttpWanPort, tResult.iRtspWanPort, tResult.iServerWanPort, tResult.iHttpsWanPort, tResult.iRtmpWanPort);
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig(%d,%d,%d,%d,%d,%d)", _iLogonId, tResult.iHttpWanPort, tResult.iRtspWanPort, tResult.iServerWanPort, tResult.iHttpsWanPort, tResult.iRtmpWanPort);
    }
}

void CLS_UPnpPage::UI_UpdateDialogText()
{
	m_lstNpupList.SetExtendedStyle(LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT);
	m_lstNpupList.DeleteAllColumns();
	m_lstNpupList.InsertColumn(n_LIST_UPNP_PORTTYPE, GetTextEx(IDS_CFG_LAN_UPNP_LIST_PORTTYPE), LVCFMT_CENTER, 100);
	m_lstNpupList.InsertColumn(n_LIST_UPNP_OUTERPORT, GetTextEx(IDS_CFG_LAN_UPNP_LIST_OUTPORT), LVCFMT_CENTER, 100);
	m_lstNpupList.InsertColumn(n_LIST_UPNP_OUTERPORT_IP, GetTextEx(IDS_CFG_LAN_UPNP_LIST_OUTPORTIP), LVCFMT_CENTER, 100);
	m_lstNpupList.InsertColumn(n_LIST_UPNP_INNERPORT, GetTextEx(IDS_CFG_LAN_UPNP_LIST_INPORT), LVCFMT_CENTER, 100);
	m_lstNpupList.InsertColumn(n_LIST_UPNP_STATUS, GetTextEx(IDS_CFG_LAN_UPNP_LIST_STATUS), LVCFMT_CENTER, 100);
	m_lstNpupList.InsertColumn(n_LIST_UPNP_OUTERPORT_IPV6, GetTextEx(IDS_CFG_LAN_UPNP_LIST_OUTPORTIPV6), LVCFMT_CENTER, 130);

	m_lstNpupList.DeleteAllItems();
	m_lstNpupList.InsertItem(n_LIST_UPNP_HTTP, "HTTP");
	m_lstNpupList.InsertItem(n_LIST_UPNP_RTSP, "RTSP");
	m_lstNpupList.InsertItem(n_LIST_UPNP_SERVER, GetTextEx(IDS_CFG_LAN_UPNP_LIST_SERVERPORT));
	m_lstNpupList.InsertItem(n_LIST_UPNP_HTTPS, "HTTPS");
	m_lstNpupList.InsertItem(n_LIST_UPNP_RTMP, "RTMP");

	SetDlgItemTextEx(IDC_RADIO_CFG_LAN_UPNP_ENABLE, IDS_CFG_LAN_UPNP_ENABLE);
	SetDlgItemTextEx(IDC_RADIO_CFG_LAN_UPNP_NOTENABLE, IDS_CFG_LAN_UPNP_NOTENABLE);
	SetDlgItemTextEx(IDC_STC_CFG_LAN_UPNP_MAPTYPE, IDS_CFG_LAN_UPNP_STC_PORTTYPE);
	SetDlgItemTextEx(IDC_BTN_CFG_LAN_UPNP_SAVE, IDS_CFG_LAN_UPNP_SAVE);
	SetDlgItemText(IDC_RADIO_CFG_LAN_UPNP_MANUAL, GetTextByLan(_T("使能手动设置映射端口"), _T("Enable to manually set mapping port")));


	InsertString(m_cboUpnpMapType, n_CBO_MANUAL, GetTextEx(IDS_CFG_LAN_UPNP_MAPTYPE_MANUAL));
	InsertString(m_cboUpnpMapType, n_CBO_AUTOMATIC, GetTextEx(IDS_CFG_LAN_UPNP_MAPTYPE_AUTO));
	m_cboUpnpMapType.SetCurSel(0);

	UI_UpdateEditList();
	UI_UpdateEnableSatus();
	UI_UpdateUpnpMapType();
    for(int i = UPNP_PORT_TYPE_HTTP; i < MAX_UPNP_PORT_TYPE; i++)
    {
        UI_UpdateListInfo(i);
    }
}

void CLS_UPnpPage::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
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

	UI_UpdateDialogText();
	UI_UpdateUpnpMapType();
}

void CLS_UPnpPage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialogText();
}

void CLS_UPnpPage::UI_UpdateListInfo(int _iPortType)
{
    int iPort = -1;
    char cPortTemp[16] = {0};
    int iInnerPort = -1;

    if (n_RADIOGROUP_MANUAL == m_iEnableRadioGroup)
    {
        ManualPortMapResult tResult = {0};
        GetManualPortMap(m_iLogonID, tResult);

        int wanPort[MAX_UPNP_PORT_TYPE] = {0};
        wanPort[UPNP_PORT_TYPE_HTTP] = tResult.iHttpWanPort;	
        wanPort[UPNP_PORT_TYPE_RTSP] = tResult.iRtspWanPort;	
        wanPort[UPNP_PORT_TYPE_SERVER] = tResult.iServerWanPort;
        wanPort[UPNP_PORT_TYPE_HTTPS] = tResult.iHttpsWanPort;	
        wanPort[UPNP_PORT_TYPE_RTMP] = tResult.iRtmpWanPort;	

        for(int i=UPNP_PORT_TYPE_HTTP; i<MAX_UPNP_PORT_TYPE; i++)
        {
            int iLine = i - 1;
            sprintf(cPortTemp, "%d", wanPort[i]);
            m_lstNpupList.SetItemText(iLine, n_LIST_UPNP_OUTERPORT, cPortTemp);
            m_lstNpupList.SetItemText(iLine, n_LIST_UPNP_OUTERPORT_IP, "");
            m_lstNpupList.SetItemText(iLine, n_LIST_UPNP_INNERPORT, "");
            m_lstNpupList.SetItemText(iLine, n_LIST_UPNP_STATUS, "");
            m_lstNpupList.SetItemText(iLine, n_LIST_UPNP_OUTERPORT_IPV6, "");
        }
        return;
    }

	CString str;
	PortMapInfo stPortMapInfo = {0};
	stPortMapInfo.iBufSize = sizeof(PortMapInfo);
	stPortMapInfo.iPortType = _iPortType;

	int iManualRet = NetClient_RecvCommand(m_iLogonID,  COMMAND_ID_PORT_MAP,  0,  &stPortMapInfo,  sizeof(PortMapInfo));
	if(RET_SUCCESS > iManualRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_UPnpPage][COMMAND_ID_PORT_MAP] Set fail,error = %d", GetLastError());
		return;
	}

	switch(_iPortType)
	{
	case UPNP_PORT_TYPE_HTTP:
		{
			int iWebPort = 80;
			NetClient_GetHTTPPort(m_iLogonID, (WORD*)&iWebPort);
			iInnerPort = iWebPort;
		}
		break;
	case UPNP_PORT_TYPE_RTSP:
		{
			THttpPort tHttpPort = {0};
			tHttpPort.iSize = sizeof(THttpPort);
			int iBytesReturned = -1;
			NetClient_GetDevConfig(m_iLogonID,NET_CLINET_HTTPPORT,0,&tHttpPort,sizeof(THttpPort),&iBytesReturned);
			iInnerPort = tHttpPort.iRtspPort;
		}
		break;
	case UPNP_PORT_TYPE_SERVER:
		{
			int iDevPort = -1;
			int iReturned = -1;
			NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SERVER_PORT, 0, &iDevPort, sizeof(int), &iReturned);
			iInnerPort = iDevPort - 1;
		}
		break;
	case UPNP_PORT_TYPE_HTTPS:
		{
			THttpPort tHttpPort = {0};
			tHttpPort.iSize = sizeof(THttpPort);
			int iBytesReturned = -1;
			NetClient_GetDevConfig(m_iLogonID,NET_CLINET_HTTPPORT,0,&tHttpPort,sizeof(THttpPort),&iBytesReturned);
			iInnerPort = tHttpPort.iHttpsport;
		}
		break;
	case UPNP_PORT_TYPE_RTMP:
		{
			PortNo tPortNo = {0};
			tPortNo.iSize = sizeof(PortNo);
			tPortNo.iPortType = PORT_NO_TYPE_RTMP;
			int iBytesReturn = -1;
			NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_PORT_NO,0,&tPortNo,sizeof(THttpPort),&iBytesReturn);
			iInnerPort = tPortNo.iPort;
		}
		break;
	default:
		break;
	}

	if(EFFECT == stPortMapInfo.iState)
	{
		str = GetTextByLan(_T("生效"), _T("EFFECT"));
	}
	else
	{
		str = GetTextByLan(_T("不生效"), _T("INEFFECT"));
	}

	sprintf(cPortTemp, "%d", iInnerPort);
	m_lstNpupList.SetItemText(stPortMapInfo.iPortType-1, n_LIST_UPNP_OUTERPORT_IP, stPortMapInfo.cOutsideIpaddr);
	m_lstNpupList.SetItemText(stPortMapInfo.iPortType-1, n_LIST_UPNP_INNERPORT, cPortTemp);
	m_lstNpupList.SetItemText(stPortMapInfo.iPortType-1, n_LIST_UPNP_STATUS, str);
	m_lstNpupList.SetItemText(stPortMapInfo.iPortType-1, n_LIST_UPNP_OUTERPORT_IPV6, stPortMapInfo.cIPv6);
	UI_UpdateListInfoByPortMap(_iPortType);
}

void CLS_UPnpPage::UI_UpdateListInfoByPortMap(int _iPortType)
{
	PortMap stPortMap = {0};
	char cPortTemp[16] = {0};
	int iBytesReturned = 0;
	stPortMap.iBufSize = sizeof(PortMap);
	stPortMap.iPortType = _iPortType;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_PORT_MAP, 0, &stPortMap, sizeof(PortMap), &iBytesReturned);
	if(iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_UPnpPage][NET_CLIENT_PORT_MAP] Set fail,error = %d", GetLastError());
		return;
	}
	
	sprintf(cPortTemp, "%d", stPortMap.iOutsidePort);
	m_lstNpupList.SetItemText(stPortMap.iPortType-1, n_LIST_UPNP_OUTERPORT, cPortTemp);
}

void CLS_UPnpPage::UI_UpdateEnableSatus()
{
	int _iEnableValue = -1;
	int iERet = NetClient_GetUPNPEnable(m_iLogonID, &_iEnableValue);
	if (iERet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetUPNPEnable(%d)", _iEnableValue);
		((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_ENABLE))->SetCheck(FALSE);
		((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_NOTENABLE))->SetCheck(FALSE);
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_MANUAL))->SetCheck(FALSE);
		return;
	}

    m_iEnableRadioGroup = _iEnableValue;
	if(n_RADIOGROUP_ENABLE == _iEnableValue)
	{
		((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_ENABLE))->SetCheck(TRUE);
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_NOTENABLE))->SetCheck(FALSE);
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_MANUAL))->SetCheck(FALSE);
	}
	else if(n_RADIOGROUP_NOTENABLE == _iEnableValue)
	{
		((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_NOTENABLE))->SetCheck(TRUE);
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_ENABLE))->SetCheck(FALSE);
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_MANUAL))->SetCheck(FALSE);
	}
    else if (n_RADIOGROUP_MANUAL == _iEnableValue)
    {
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_MANUAL))->SetCheck(TRUE);
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_ENABLE))->SetCheck(FALSE);
        ((CButton*)GetDlgItem(IDC_RADIO_CFG_LAN_UPNP_NOTENABLE))->SetCheck(FALSE);
        m_lstNpupList.SetItemControl(EDITBOX, n_LIST_UPNP_OUTERPORT);
        m_cboUpnpMapType.EnableWindow(false);
    }
	else
	{
		return;
	}
}

void CLS_UPnpPage::OnMainNotify(int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser)
{
    if (n_RADIOGROUP_MANUAL == m_iEnableRadioGroup)
    {
        return;
    }
	if (_ulLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d) or (MANUAL PORT MAP)", _ulLogonID);
		return;
	}
	int iMessage = LOWORD(_iWparam);
	switch (iMessage)
	{
	case  WCM_PORT_MAP:
		{
			UI_UpdateDialogText();
		}
		break;
	default:
		break;
	}
}

void CLS_UPnpPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    if (_iLogonID < 0)
    {
        AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)", _iLogonID);
        return;
    }

    switch (_iParaType)
    {
    case  PARA_MANUAL_PORTMAP:
        {
            AddLog(LOG_TYPE_MSG,"","[CLS_UPnpPage][OnParamChangeNotify] logon id(%d)", _iLogonID);
            UI_UpdateDialogText();
        }
        break;
    default:
        break;
    }
}
void CLS_UPnpPage::OnBnClickedRadioCfgLanUpnpEnable()
{
	// TODO: Add your control notification handler code here
	int iEnableValue = -1;
	UpdateData(TRUE);

	switch(m_iEnableRadioGroup)
	{
	case n_RADIOGROUP_ENABLE:
		{
			iEnableValue = 1;
            m_cboUpnpMapType.EnableWindow(true);
		}
		break;
	case n_RADIOGROUP_NOTENABLE:
		{
			iEnableValue = 0;
            m_cboUpnpMapType.EnableWindow(true);
		}
        break;
    case n_RADIOGROUP_MANUAL:
        {
            iEnableValue = 2;
            m_lstNpupList.SetItemControl(EDITBOX, n_LIST_UPNP_OUTERPORT);
            m_cboUpnpMapType.EnableWindow(false);
        }
		break;
	default:
		return;
	}

	int iRet =  NetClient_SetUPNPEnable(m_iLogonID, iEnableValue);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetUPNPEnable(%d)", iEnableValue);
	}
	else if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetUPNPEnable(%d)", iEnableValue);
	}

}

void CLS_UPnpPage::OnCbnSelchangeCboCfgLanUpnpMaptype()
{
	// TODO: Add your control notification handler code here
	int iType = 0;
	iType = m_cboUpnpMapType.GetCurSel() + 1;
	int iCRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_UPNP_STATUS, PARAM_CHANNEL_ALL, iType);
	if(TD_SUCCESS != iCRet)
	{
		AddLog(LOG_TYPE_FAIL,"", "NetClient_SetCommonEnable(d)", iCRet);
		return;
	}

	for(int i = UPNP_PORT_TYPE_HTTP; i < MAX_UPNP_PORT_TYPE; i++)
	{
		UI_UpdateListInfo(i);
	}
	UI_UpdateEditList();
}

void CLS_UPnpPage::UI_UpdateUpnpMapType()
{
	int iUpnpType = 0;
	int iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_UPNP_STATUS, PARAM_CHANNEL_ALL, &iUpnpType);
	if(RET_SUCCESS == iRet)
	{
		switch (iUpnpType)
		{
		case 1:
			m_cboUpnpMapType.SetCurSel(0);
			break;
		default:
			m_cboUpnpMapType.SetCurSel(1);
			break;
		}
	}
	else
	{
		m_cboUpnpMapType.SetCurSel(0);		
	}

	UI_UpdateEditList();
}

void CLS_UPnpPage::UI_UpdateEditList()
{
	int iIndex = m_cboUpnpMapType.GetCurSel();

	switch(iIndex)
	{
	case n_CBO_MANUAL:
		{
			m_lstNpupList.SetItemControl(EDITBOX, n_LIST_UPNP_OUTERPORT);
		}
		break;
	case n_CBO_AUTOMATIC:
		{
			m_lstNpupList.ReleaseControls();
		}
	default:
		return;
	}
}

void CLS_UPnpPage::OnBnClickedBtnCfgLanUpnpSave()
{
	// TODO: Add your control notification handler code here
	m_lstNpupList.GetAndDisplayControlsData();
	m_lstNpupList.ShowControls(FALSE);

    if (n_RADIOGROUP_MANUAL == m_iEnableRadioGroup)
    {
        ManualPortMap tPara = {0};
        tPara.iSize = sizeof(tPara);
        tPara.iHttpWanPort = StrToInt(m_lstNpupList.GetItemText(UPNP_PORT_TYPE_HTTP - 1, COLUME_ITEM_0));
        tPara.iRtspWanPort = StrToInt(m_lstNpupList.GetItemText(UPNP_PORT_TYPE_RTSP - 1, COLUME_ITEM_0));
        tPara.iServerWanPort = StrToInt(m_lstNpupList.GetItemText(UPNP_PORT_TYPE_SERVER - 1, COLUME_ITEM_0));
        tPara.iHttpsWanPort = StrToInt(m_lstNpupList.GetItemText(UPNP_PORT_TYPE_HTTPS - 1, COLUME_ITEM_0));
        tPara.iRtmpWanPort = StrToInt(m_lstNpupList.GetItemText(UPNP_PORT_TYPE_RTMP - 1, COLUME_ITEM_0));

        if(1>tPara.iHttpWanPort || 65535<tPara.iHttpWanPort 
            || 1>tPara.iRtspWanPort || 65535<tPara.iRtspWanPort
            || 1>tPara.iServerWanPort || 65535<tPara.iServerWanPort
            || 1>tPara.iHttpsWanPort || 65535<tPara.iHttpsWanPort
            || 1>tPara.iRtmpWanPort || 65535<tPara.iRtmpWanPort)
        {
            return;
        }

        int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_MANUAL_PORTMAP, 0, &tPara, sizeof(tPara));
        if(TD_SUCCESS != iRet)
        {
            AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d,%d,%d,%d,%d,%d)", m_iLogonID, tPara.iHttpWanPort, tPara.iRtspWanPort, tPara.iServerWanPort, tPara.iHttpsWanPort, tPara.iRtmpWanPort);
        }
        else
        {
            AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d,%d,%d,%d,%d,%d)", m_iLogonID, tPara.iHttpWanPort, tPara.iRtspWanPort, tPara.iServerWanPort, tPara.iHttpsWanPort, tPara.iRtmpWanPort);
        }

        ManualPortMapResult tResult = {0};
        GetManualPortMap(m_iLogonID, tResult);
        return;
    }

	int iType = 0;
	iType = m_cboUpnpMapType.GetCurSel() + 1;
	int iCRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_UPNP_STATUS, PARAM_CHANNEL_ALL, iType);
	if(TD_SUCCESS != iCRet)
	{
		AddLog(LOG_TYPE_FAIL,"", "NetClient_SetCommonEnable(d)", iCRet);
		return;
	}

	int iIndex = m_cboUpnpMapType.GetCurSel();
	if(0 == iIndex)
	{
		int iRet = TD_FAILURE;
		int iTempPort = 0;
		PortMap strPortMap = {0};
		strPortMap.iBufSize = sizeof(PortMap);
		for(int i=UPNP_PORT_TYPE_HTTP; i<MAX_UPNP_PORT_TYPE; i++)
		{
			int iLine = i - 1;
			strPortMap.iPortType = i;
			strPortMap.iOutsidePort = StrToInt(m_lstNpupList.GetItemText(iLine, COLUME_ITEM_0));
			if(1>strPortMap.iOutsidePort || 65535<strPortMap.iOutsidePort)
			{
				return;
			}
			for(int iLineIndex=0; iLineIndex<4; iLineIndex++)
			{
				if(iLineIndex != iLine)
				{
					iTempPort = StrToInt(m_lstNpupList.GetItemText(iLineIndex, COLUME_ITEM_0));
					if(strPortMap.iOutsidePort == iTempPort)
					{
						return;
					}
				}
			}
			
			iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_PORT_MAP, 0, &strPortMap, sizeof(PortMap));
			if(TD_SUCCESS != iRet)
			{
				AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d)", iRet);
				return;
			}
			iTempPort = 0;
		}
	}
}

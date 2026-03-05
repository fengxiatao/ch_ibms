// AdvVersionPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "AdvVersionPage.h"


// CLS_AdvVersionPage dialog

IMPLEMENT_DYNAMIC(CLS_AdvVersionPage, CDialog)

CLS_AdvVersionPage::CLS_AdvVersionPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_AdvVersionPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

CLS_AdvVersionPage::~CLS_AdvVersionPage()
{
}

void CLS_AdvVersionPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}


BEGIN_MESSAGE_MAP(CLS_AdvVersionPage, CDialog)
	ON_BN_CLICKED(IDC_EDIT_ADVANCE_SET, &CLS_AdvVersionPage::OnBnClickedEditAdvanceSet)
	ON_BN_CLICKED(IDC_BUTTON_MODULE_ID, &CLS_AdvVersionPage::OnBnClickedButtonModuleId)
END_MESSAGE_MAP()

void CLS_AdvVersionPage::UI_UpdateText()
{
	SetDlgItemTextEx(IDC_STATIC_ADV_VERSION_KERNEL, IDS_ADV_VERSION_KERNEL);
	SetDlgItemTextEx(IDC_STATIC_ADV_VERSION_UI, IDS_ADV_VERSION_UI);
	SetDlgItemTextEx(IDC_STATIC_ADV_VERSION_PRODUCT_ID, IDS_ADV_VERSION_ID);
	SetDlgItemTextEx(IDC_STATIC_ADV_VERSION_PLUGIN, IDS_ADV_VERSION_PLUGIN);
	SetDlgItemTextEx(IDC_STATIC_ADV_VERSION_SLAVE, IDS_ADV_VERSION_SLAVE);
	SetDlgItemTextEx(IDC_STATIC_ADVI_SER_OTHID, IDS_CFG_ADVI_VER_OTHRTID);
	SetDlgItemTextEx(IDC_STATIC_SINGLECHIP_VERSION, IDS_CFG_ADVI_VER_SINGLECHIP);
	SetDlgItemText(IDC_STATIC_METHOD_VERSION, GetTextByLan("算法版本", "MethodVer"));

	SetDlgItemText(IDC_STATIC_T2Media, GetTextByLan("T2媒体版本", "T2Media"));
	SetDlgItemText(IDC_STATIC_NETPAGE, GetTextByLan("网页SDK版本", "NetPageSdkVer"));
	SetDlgItemText(IDC_STATIC_OCXVERSION, GetTextByLan("Ocx版本", "OcxVer"));
	SetDlgItemText(IDC_STATIC_DOMEVERSION, GetTextByLan("球机版本", "DomeVer"));
	SetDlgItemText(IDC_STATIC_DIGITALVERSION, GetTextByLan("数字机芯", "DigitalVer"));
	SetDlgItemText(IDC_STATIC_SHDBVERSION, GetTextByLan("上海地标", "SHDBVer"));
	SetDlgItemText(IDC_STATIC_VCA_VERSION, GetTextByLan("智能模块", "VCAVer"));
	SetDlgItemText(IDC_STATIC_ALGO_VERSION, GetTextByLan("算法模型", "AlgoVer"));
	SetDlgItemText(IDC_STATIC_FRONT_BOARD, GetTextByLan("前面板", "FrontBoardVer"));
	SetDlgItemText(IDC_STATIC_ZFZJ_VERSION, GetTextByLan("政法主机", "ZFZJVer"));
	SetDlgItemText(IDC_STATIC_ITSVERSION, GetTextByLan("交通版本", "ITSVer"));
	SetDlgItemText(IDC_STATIC_ENCRP_VERSION, GetTextByLan("加密芯片固件", "EncryptFirmwareVer"));
	SetDlgItemText(IDC_STATIC_ALI_PAYMENT_ALGORITHM, GetTextByLan("阿里支付版本", "ALiPaymentVer"));
	SetDlgItemTextEx(IDC_STATIC_BRAND, IDS_TEXT_ADVANCE_BRAND);
}

void CLS_AdvVersionPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

	UI_UpdateParam();
}

void CLS_AdvVersionPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateText();
	UI_UpdateParam();
}

void CLS_AdvVersionPage::UI_UpdateParam()
{
	if (m_iLogonID < 0)
	{
		return;
	}
	PDEVICE_INFO Device = FindDevice(m_iLogonID);
	SERVER_VERSION version = {0};
	version.m_iStructSize = sizeof(SERVER_VERSION);
	int iRet = NetClient_GetServerVersion_V1(m_iLogonID, &version);
	if (Device && iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "","NetClient_GetServerVersion_V1(%s,%d)", Device->cIP, m_iLogonID);
	}
	SetDlgItemText(IDC_EDIT_ADV_VERSION_KERNEL, version.m_cVersion);
	SetDlgItemText(IDC_EDIT_ADV_VERSION_UI, version.m_cUIVersion);
	SetDlgItemText(IDC_EDIT_ADV_VERSION_SLAVE, version.m_cSlaveVersion);
	SetDlgItemText(IDC_EDIT_ADV_VERSION_PLUGIN, version.m_cPlugInVersion);
	SetDlgItemText(IDC_EDIT_ADV_VERSION_SINGLECHIP, version.m_cSCMVersion);
	SetDlgItemText(IDC_EDIT_ADV_VERSION_ID, Device->cID);
	SetDlgItemText(IDC_EDIT_ADV_2ND_M3_DRIVER, version.m_cSecondDomeDriverVersion);
	SetDlgItemText(IDC_EDIT_METHOD_VERSION, version.m_cMethodVersion);

	SetDlgItemText(IDC_EDIT_T2VERSION, version.m_cMediaVersion);
	SetDlgItemText(IDC_EDIT_UI2,  version.m_cSCGuiVersion);
	SetDlgItemText(IDC_EDIT_NETPAGEVERSION, version.m_cNetSdkVersion);
	SetDlgItemText(IDC_EDIT_OCXVERSION,  version.m_cNetOcxVersion);
	SetDlgItemText(IDC_EDIT_CPLD, version.m_cCpldVersion);
	SetDlgItemText(IDC_EDIT_DOME, version.m_cDomeDriveVersion);
	SetDlgItemText(IDC_EDIT_DIGITAL,version.m_cDigitalMovementVersion);
	SetDlgItemText(IDC_EDIT_P2P, version.m_cTradeP2PVersion);
	SetDlgItemText(IDC_EDIT_ONVIF,version.m_cTradeOnvifVersion);
	SetDlgItemText(IDC_EDIT_28181, version.m_cTrade28181Version);
	SetDlgItemText(IDC_EDIT_H323, version.m_cTradeH323Version);

	SetDlgItemText(IDC_EDIT_RTMP, version.m_cTradeRTMPVersion);
	SetDlgItemText(IDC_EDIT_HPDVERSION,  version.m_cHpdVersion);
	SetDlgItemText(IDC_EDIT_NETPAGEVERSION, version.m_cNetSdkVersion);
	SetDlgItemText(IDC_EDIT_OCXVERSION,  version.m_cNetOcxVersion);
	SetDlgItemText(IDC_EDIT_CPLD, version.m_cCpldVersion);
	SetDlgItemText(IDC_EDIT_SHDBVERSION, version.m_cLandmarkVersion);
	SetDlgItemText(IDC_EDIT_VCAVERSION,version.m_cIntelligentModuleVersion);
	SetDlgItemText(IDC_EDIT_ALGOVERSION, version.m_cAlgoModelVersion);
	SetDlgItemText(IDC_EDIT_FRONTBOARD,version.m_cFrontPanelVersion);
	SetDlgItemText(IDC_EDIT_RTSP, version.m_cTradeRTSPVersion);
	SetDlgItemText(IDC_EDIT_ZFZJ_VERSION, version.m_cTradeZFZJVersion);
	SetDlgItemText(IDC_EDIT_ITS_VERSION, version.m_cTradeItsVersion);
	SetDlgItemText(IDC_EDIT_ENCRYPT_VERSION, version.m_cEncrypChipSupportFirmwareVersion);
	SetDlgItemText(IDC_EDIT_ALI_PAYMENT_ALGORITHM, version.m_cAliPaymentAlogrithm);

	IndustryPlatformTagInfo tInfo = {0};
	tInfo.iGetMode = DEVICE_LOGIN_REPORT;
	tInfo.iSize = sizeof(IndustryPlatformTagInfo);
	int iBytesReturned = 0;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_INDUSTRY_PLATFORM_TAG_INFO, m_iChannelNo, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig failed! Logon id(%d)", m_iLogonID);
	}
	SetDlgItemText(IDC_EDIT_MODULE_ID, tInfo.cModuleID);

	char cOtherID[LEN_32] = {0};
	iRet = NetClient_GetOtherID(m_iLogonID, cOtherID, LEN_32);
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL, "","NetClient_GetOtherID(%s,%d)", Device->cIP, m_iLogonID);
	}
	else
	{
		SetDlgItemText(IDC_EDIT_ADVISER_OTHERID,cOtherID);
	}
//add Set device model
	int iBytesRetu = 0;
	DevModel tPCModel = {0};
	tPCModel.iSize = sizeof(tPCModel);
	int iRetPCModel = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DEV_MODEL, m_iChannelNo, &tPCModel, sizeof(tPCModel), &iBytesRetu);
	
	SetDlgItemText(IDC_EDIT_ADVANCE_PCMODEL, tPCModel.cModel);
	SetDlgItemText(IDC_EDIT_BRAND, tPCModel.cBrand);
 	SetDlgItemTextEx(IDC_EDIT_ADVANCE_SET,IDS_SET);
 	SetDlgItemTextEx(IDC_STATIC_ADVANCE_PCMODEL,IDS_CONFIG_ADVANCE_PCMODEL);
	if( RET_SUCCESS == iRetPCModel)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[DEV_MODEL] (%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig[DEV_MODEL] (%d,%d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
}
// CLS_AdvVersionPage message handlers

BOOL CLS_AdvVersionPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_AdvVersionPage::OnBnClickedEditAdvanceSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	DevModel tPCModel = {0};
	CString strPCModel;
	CString strBrand;
	GetDlgItemText(IDC_EDIT_ADVANCE_PCMODEL, strPCModel);
	GetDlgItemText(IDC_EDIT_BRAND, strBrand);
	strncpy(tPCModel.cModel, strPCModel.GetBuffer(),LEN_64);
	strncpy(tPCModel.cBrand, strBrand.GetBuffer(), min(strBrand.GetLength(), sizeof(tPCModel.cBrand) - 1));
	tPCModel.iSize = sizeof(tPCModel);

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DEV_MODEL, m_iChannelNo, &tPCModel, sizeof(tPCModel));
	if( RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[DEV_MODEL] (%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig[DEV_MODEL] (%d,%d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
}

void CLS_AdvVersionPage::OnBnClickedButtonModuleId()
{
	IndustryPlatformTagInfo tInfo = {0};
	tInfo.iGetMode = LIGHTWEIGHT_REALTIME_GET;
	tInfo.iSize = sizeof(IndustryPlatformTagInfo);
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_INDUSTRY_PLATFORM_TAG_INFO, m_iChannelNo, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig failed! Logon id(%d)", m_iLogonID);
	}
	SetDlgItemText(IDC_EDIT_MODULE_ID, tInfo.cModuleID);
}

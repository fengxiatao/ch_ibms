// Config\SipPage.cpp : implementation file
//

#include "stdafx.h"
#include "SipPage.h"


// CLS_SipPage dialog
#define GB28181 1
#define GB35114 2

IMPLEMENT_DYNAMIC(CLS_SipPage, CDialog)


CLS_SipPage::CLS_SipPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_SipPage::IDD, pParent)
{
	m_iLogonID = -1;
}

CLS_SipPage::~CLS_SipPage()
{
}

void CLS_SipPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);

	DDX_Control(pDX, IDC_EDIT_SIP_LEVEL1, m_edtSipLevel1);
	DDX_Control(pDX, IDC_EDIT_SIP_PTZTIME, m_edtSipPtztime);
	DDX_Control(pDX, IDC_EDIT_SIP_LEVEL2, m_edtSipLevel2);
	DDX_Control(pDX, IDC_EDIT_SIP_CHANNELID1, m_edtSipChannelID1);
	DDX_Control(pDX, IDC_EDIT_SIP_CHANNELID2, m_edtSipChannelID2);
	DDX_Control(pDX, IDC_COMBO_ALARM, m_cboAlarmNo);
	DDX_Control(pDX, IDC_EDIT_KEYEDIT, m_edtSipPublicKey);
	DDX_Control(pDX, IDC_COMBO_ENCRYPTMODE, m_cboSipMode);
	DDX_Control(pDX, IDC_EDIT_SIP_PCCHANNEL, m_edtSipChannel);
	DDX_Control(pDX, IDC_COMBO_SIP_VIDEOPLAT, m_cboVideoPlatId);
	DDX_Control(pDX, IDC_COMBO_SIP_ALARMPLAT, m_cboAlarmPlatId);
	DDX_Control(pDX, IDC_COMBO_AUDIO_CONNTYPE, m_cboAudioConnType);
	DDX_Control(pDX, IDC_COMBO_SIP_AUDIOPLAT, m_cboAudioPlatId);
}


BEGIN_MESSAGE_MAP(CLS_SipPage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SIP_VIDEOCHANNELSET, &CLS_SipPage::OnBnClickedButtonSipVideochannelset)
	ON_BN_CLICKED(IDC_BUTTON_SIP_ALARMCHANNELSET, &CLS_SipPage::OnBnClickedButtonSipAlarmchannelset)
	ON_CBN_SELCHANGE(IDC_COMBO_ALARM, &CLS_SipPage::OnCbnSelchangeComboAlarm)
	ON_BN_CLICKED(IDC_BTN_TEST, &CLS_SipPage::OnBnClickedBtnTest)
	ON_BN_CLICKED(IDC_BUTTON_MODESET, &CLS_SipPage::OnBnClickedButtonModeset)
	ON_BN_CLICKED(IDC_BUTTON_KEYGENERATE, &CLS_SipPage::OnBnClickedButtonKeygenerate)
	ON_BN_CLICKED(IDC_BUTTON_SIP_PCCHANNELSET, &CLS_SipPage::OnBnClickedButtonSipPcchannelset)
END_MESSAGE_MAP()


// CLS_SipPage message handlers
BOOL CLS_SipPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtSipChannelID1.SetLimitText(32);
	m_edtSipChannelID2.SetLimitText(32);
	m_edtSipLevel1.SetLimitText(10);
	m_edtSipLevel2.SetLimitText(10);
	m_edtSipPtztime.SetLimitText(10);
	m_edtSipPublicKey.SetLimitText(256);
    UI_UpdateDialog();
	UI_UpdateSipChannel();
	m_cboSipMode.SetItemData(m_cboSipMode.AddString("GB28181"), GB28181);
	m_cboSipMode.SetItemData(m_cboSipMode.AddString("GB35114"), GB35114);
	return TRUE;
}

void CLS_SipPage::OnChannelChanged(int _iLogonID, int _iChannelNo, int /*_iStreamNo*/)
{
	m_iLogonID = _iLogonID;
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon ID(%d)", m_iLogonID);
		return;
	}
	if (_iChannelNo < 0)
	{
		m_iChannelNum = 0;
	}
	else
	{
		m_iChannelNum = _iChannelNo;
	}
	int iAlarmChannelNo = 0,iAlarmOutPortNum = 0;
	int iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iAlarmChannelNo, &iAlarmOutPortNum);
	m_cboAlarmNo.ResetContent();
	for (int i = 0; i < iAlarmChannelNo; i++)
	{
		CString szChannel;
		szChannel.Format("%d", i);
		m_cboAlarmNo.AddString(szChannel);
	}
	m_cboAlarmNo.SetCurSel(0);
	GetChannelSipInfo();
	GetSipEncryptInfo();
	UI_UpdateSipChannel();
}

void CLS_SipPage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}
void CLS_SipPage::OnBnClickedButtonSipVideochannelset()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon ID(%d)", m_iLogonID);
		return;
	}

	CString strChannelID;
	m_edtSipChannelID1.GetWindowText(strChannelID);
	TSipVideoChannel svc = {0};
	svc.iChannelNo = m_iChannelNum;
	strcpy_s(svc.cChannelID, sizeof(svc.cChannelID), strChannelID);
	svc.iLevel = GetDlgItemInt(IDC_EDIT_SIP_LEVEL1);
	svc.iPtzTime = GetDlgItemInt(IDC_EDIT_SIP_PTZTIME);
	svc.iPlatId = m_cboVideoPlatId.GetCurSel();

	int iRet =  NetClient_SetChannelSipConfig(m_iLogonID, m_iChannelNum, SIP_CMD_SET_VIDEOCHANNEL, &svc, sizeof(svc));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
	else if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
}

void CLS_SipPage::OnBnClickedButtonSipAlarmchannelset()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon ID(%d)", m_iLogonID);
		return;
	}

	CString strChannelID;
	m_edtSipChannelID2.GetWindowText(strChannelID);
	TSipAlarmChannel sac = {0};
	sac.iChannelNo = m_cboAlarmNo.GetCurSel();
	strcpy_s(sac.cChannelID, sizeof(sac.cChannelID), strChannelID);
	sac.iLevel = GetDlgItemInt(IDC_EDIT_SIP_LEVEL2);
	sac.iPlatId = m_cboAlarmPlatId.GetCurSel();

	int iRet = NetClient_SetChannelSipConfig(m_iLogonID, sac.iChannelNo, SIP_CMD_SET_ALARMCHANNEL, &sac, sizeof(sac));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
	else if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
}

void CLS_SipPage::GetChannelSipInfo()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon ID(%d)", m_iLogonID);
		return;
	}

	TSipVideoChannel svc = {0};
	svc.iChannelNo = m_iChannelNum;
	int iCmd = SIP_CMD_GET_VIDEOCHANNEL;
	int iRet1 = NetClient_GetChannelSipConfig(m_iLogonID, m_iChannelNum, SIP_CMD_GET_VIDEOCHANNEL, &svc, sizeof(svc));
	if (iRet1 == 0)
	{
		m_edtSipChannelID1.SetWindowText(svc.cChannelID);
		SetDlgItemInt(IDC_EDIT_SIP_LEVEL1, svc.iLevel);
		SetDlgItemInt(IDC_EDIT_SIP_PTZTIME, svc.iPtzTime);
		m_cboVideoPlatId.SetCurSel(svc.iPlatId);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
	else if (iRet1 < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}

	TSipAlarmChannel sac = {0};
	sac.iChannelNo = m_cboAlarmNo.GetCurSel();
	iCmd = SIP_CMD_GET_ALARMCHANNEL;
	int iRet2 = NetClient_GetChannelSipConfig(m_iLogonID, sac.iChannelNo, SIP_CMD_GET_ALARMCHANNEL, &sac, sizeof(sac));
	if (iRet2 == 0)
	{
		m_edtSipChannelID2.SetWindowText(sac.cChannelID);
		SetDlgItemInt(IDC_EDIT_SIP_LEVEL2, sac.iLevel);
		m_cboAlarmPlatId.SetCurSel(sac.iPlatId);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
	else if (iRet2 < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
}


void CLS_SipPage::GetSipEncryptInfo()
{
	if (m_iLogonID < 0)
	{
		return;
	}
	SIPEncrypt svc = {0};
	SIPPublicKey spk = {0};
	int iCmd = NET_CLIENT_SIP_ENCRYPT;
	int ilpBytesReturned = 0; 
	int iRet =  NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SIP_ENCRYPT, m_iChannelNum, &svc, sizeof(svc), &ilpBytesReturned);
	if (RET_SUCCESS == 0)
	{
		m_cboSipMode.SetCurSel(svc.iMode - 1);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}
	else if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}
	iCmd = NET_CLIENT_SIP_PUBLICKEY;
	ilpBytesReturned = 0; 
	iRet =  NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SIP_PUBLICKEY, m_iChannelNum, &spk, sizeof(spk), &ilpBytesReturned);
	if (RET_SUCCESS == 0)
	{
		m_edtSipPublicKey.SetWindowText(spk.sPublicKey);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}
	else if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "","NetClient_GetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}

}
void CLS_SipPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_SIP_VIDEOCHANNEL, IDS_CONFIG_SIP_VIDEOCHANNEL);
	SetDlgItemTextEx(IDC_STATIC_SIP_CHANNELID1, IDS_CONFIG_SIP_CHANNELID1);
	SetDlgItemTextEx(IDC_STATIC_SIP_LEVEL, IDS_CONFIG_SIP_LEVEL1);
	SetDlgItemTextEx(IDC_STATIC_SIP_PTZTIME, IDS_CONFIG_SIP_PTZTIME);
	SetDlgItemTextEx(IDC_BUTTON_SIP_VIDEOCHANNELSET, IDS_CONFIG_SIP_VIDEOSET);
	SetDlgItemTextEx(IDC_STATIC_SIP_ALARMCHANNEL, IDS_CONFIG_SIP_ALARMCHANNEL);
	SetDlgItemTextEx(IDC_STATIC_SIP_CHANNELID2, IDS_CONFIG_SIP_CHANNELID2);
	SetDlgItemTextEx(IDC_STATIC_SIP_LEVEL2, IDS_CONFIG_SIP_LEVEL2);
	SetDlgItemTextEx(IDC_BUTTON_SIP_ALARMCHANNELSET, IDS_CONFIG_SIP_ALARMSET);
	SetDlgItemTextEx(IDC_STATIC_ALARM, IDS_CONFIG_ALARM_NO);
	SetDlgItemTextEx(IDC_BTN_TEST, IDS_LOG_TEST);
	SetDlgItemText(IDC_STATIC_ENCRYPTINFO, GetTextByLan(_T("加密信息"), _T("Encrypt Info")));
	SetDlgItemText(IDC_STATIC_ENCRYPTMODE, GetTextByLan(_T("加密模式"), _T("Encrypt Mode")));
	SetDlgItemText(IDC_STATIC_ENCRYPTKEY, GetTextByLan(_T("加密公钥"), _T("Encrypt Key")));
	SetDlgItemText(IDC_BUTTON_MODESET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_KEYGENERATE, GetTextByLan(_T("生成"), _T("Generate")));
	SetDlgItemText(IDC_STATIC_SIPCHANNEL, GetTextByLan(_T("Sip通道"), _T("Sip Channel")));
	SetDlgItemText(IDC_BUTTON_SIP_PCCHANNELSET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_SIP_VIDEOPLAT, GetTextByLan(_T("平台编号"), _T("Plat ID")));
	SetDlgItemText(IDC_STATIC_SIP_ALARMPLAT, GetTextByLan(_T("平台编号"), _T("Plat ID")));
	SetDlgItemText(IDC_STATIC_SIP_AUDIOPLAT, GetTextByLan(_T("平台编号"), _T("Plat ID")));
	SetDlgItemText(IDC_STATIC_SIP_AUDIO_CONNTYPE, GetTextByLan(_T("连接方式"), _T("Connect type")));
	m_cboVideoPlatId.ResetContent();
	m_cboVideoPlatId.AddString(GetTextByLan(_T("平台1"), _T("Plat1")));
	m_cboVideoPlatId.AddString(GetTextByLan(_T("平台2"), _T("Plat2")));
	m_cboAlarmPlatId.ResetContent();
	m_cboAlarmPlatId.AddString(GetTextByLan(_T("平台1"), _T("Plat1")));
	m_cboAlarmPlatId.AddString(GetTextByLan(_T("平台2"), _T("Plat2")));
	m_cboAudioPlatId.ResetContent();
	m_cboAudioPlatId.AddString(GetTextByLan(_T("平台1"), _T("Plat1")));
	m_cboAudioPlatId.AddString(GetTextByLan(_T("平台2"), _T("Plat2")));
	m_cboAudioConnType.ResetContent();
	m_cboAudioConnType.AddString(GetTextByLan(_T("保留"), _T("Retain")));
	m_cboAudioConnType.AddString(_T("TCP"));
	m_cboAudioConnType.AddString(_T("UDP"));
}
void CLS_SipPage::OnCbnSelchangeComboAlarm()
{
	// TODO: Add your control notification handler code here
	TSipAlarmChannel sac = {0};
	sac.iChannelNo = m_cboAlarmNo.GetCurSel();
	sac.iPlatId = m_cboAlarmPlatId.GetCurSel();
	int iCmd = SIP_CMD_GET_ALARMCHANNEL;
	int iRet2 = NetClient_GetChannelSipConfig(m_iLogonID, sac.iChannelNo, SIP_CMD_GET_ALARMCHANNEL, &sac, sizeof(sac));
	if (iRet2 == 0)
	{
		m_edtSipChannelID2.SetWindowText(sac.cChannelID);
		SetDlgItemInt(IDC_EDIT_SIP_LEVEL2, sac.iLevel);
		m_cboAlarmPlatId.SetCurSel(sac.iPlatId);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
	else if (iRet2 < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetChannelSipConfig ID(%d)ChannelNum(%d)", m_iLogonID, m_iChannelNum);
	}
}

void CLS_SipPage::OnBnClickedBtnTest()
{
	// TODO: Add your control notification handler code here
	
	int iCommand = NET_CLIENT_BARCODE;
	int iChannelNum = 0;
	int iTmpBufSize = sizeof(char); 
	CString cTmpBarCode;

	GetDlgItemText(IDC_EDT_TEST, cTmpBarCode);

	int iRet = NetClient_SetDevConfig(m_iLogonID, iCommand, iChannelNum, (void*)(LPSTR)(LPCTSTR)cTmpBarCode, iTmpBufSize);
	if(0 != iRet)
	{
		return;
	}
}

void CLS_SipPage::OnBnClickedButtonModeset()
{
	// TODO: Add your control notification handler code here
	SIPEncrypt svc = {0};
	int iCmd = NET_CLIENT_SIP_ENCRYPT;
	svc.iMode = m_cboSipMode.GetCurSel()+1;
	int iRet =  NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SIP_ENCRYPT, m_iChannelNum, &svc, sizeof(svc));
	if (RET_SUCCESS == 0)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}
	else if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}
}

void CLS_SipPage::OnBnClickedButtonKeygenerate()
{
	// TODO: Add your control notification handler code here
	SIPPublicKey spk = {0};
	int iCmd = NET_CLIENT_SIP_PUBLICKEY;
	int iRet =  NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SIP_PUBLICKEY, m_iChannelNum, &spk, sizeof(spk));
	if (RET_SUCCESS == 0)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}
	else if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d) ID(%d)ChannelNum(%d)", iCmd, m_iLogonID, m_iChannelNum);
	}
}

void CLS_SipPage::OnBnClickedButtonSipPcchannelset()
{
	AudioChannel tAudioChannel = {0}; 
	CString cstrTemp;
	tAudioChannel.iConnectType = m_cboAudioConnType.GetCurSel();
	tAudioChannel.iPlatId = m_cboAudioPlatId.GetCurSel();
	m_edtSipChannel.GetWindowText(cstrTemp);
	strcpy_s(tAudioChannel.pcChannel, sizeof(tAudioChannel.pcChannel), cstrTemp);
	int iRet =  NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SIPAUDIO, m_iChannelNO, &tAudioChannel, sizeof(AudioChannel));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d) ID(%d)ChannelNum(%d)", NET_CLIENT_SIPAUDIO, m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d) ID(%d)ChannelNum(%d)", NET_CLIENT_SIPAUDIO, m_iLogonID, m_iChannelNO);
	}
}

void CLS_SipPage::UI_UpdateSipChannel()
{
	AudioChannel tAudioChannel = {0};
	int iReturn = 0;
	int iRet= NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SIPAUDIO, m_iChannelNO, &tAudioChannel, sizeof(AudioChannel), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		m_edtSipChannel.SetWindowText(tAudioChannel.pcChannel);
		m_cboAudioConnType.SetCurSel(tAudioChannel.iConnectType);
		m_cboAudioPlatId.SetCurSel(tAudioChannel.iPlatId);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig ID(%d)ChannelNo(%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig ID(%d)ChannelNo(%d)", m_iLogonID, m_iChannelNO);
	}
}

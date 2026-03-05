// CLS_RTPServerInfo.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include ".\CLS_RTPServerInfo.h"
#define MAX_SERVER_TTL_COUNT						128
#define TS_TYPE										2

// CLS_RTPServerInfo dialog

IMPLEMENT_DYNAMIC(CLS_RTPServerInfo, CDialog)

CLS_RTPServerInfo::CLS_RTPServerInfo(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_RTPServerInfo::IDD, pParent)
{

}

CLS_RTPServerInfo::~CLS_RTPServerInfo()
{
}

void CLS_RTPServerInfo::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_STREAM_TYPE, m_cboStreamType);
	DDX_Control(pDX, IDC_CHECK_RTPSERVERINFO_ENABLE, m_chEnable);
	DDX_Control(pDX, IDC_EDIT_VIDEO_ADDRESS, m_editVideoAddr);
	DDX_Control(pDX, IDC_EDIT_VIDEO_PORT, m_editVideoPort);
	DDX_Control(pDX, IDC_COMBO_VIDEO_TTL, m_cboVideoTTL);
	DDX_Control(pDX, IDC_EDIT_AUDIO_ADDRESS, m_editAudioAddr);
	DDX_Control(pDX, IDC_EDIT_AUDIO_PORT, m_editAudioPort);
	DDX_Control(pDX, IDC_COMBO_AUDIO_TTL, m_cboAduioTTL);
	DDX_Control(pDX, IDC_EDIT_METADATA_ADDRESS, m_editMetaDataAddr);
	DDX_Control(pDX, IDC_EDIT_METADATA_PORT, m_editMetaDataPort);
	DDX_Control(pDX, IDC_COMBO_METADATA_TTL, m_cboMetaDataTTL);
	DDX_Control(pDX, IDC_COMBO_MULTICAST_TYPE, m_cboMultiType);
	DDX_Control(pDX, IDC_BUTTON_SET_RTPSERVER, m_btSet);
	DDX_Control(pDX, IDC_BUTTON_GET_RTPSERVER, m_btGet);
}

BOOL CLS_RTPServerInfo::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	for(int i = 0; i < MAX_SERVER_TTL_COUNT; i++)
	{
		CString strNum;
		strNum.Format(_T("%d"), i + 1);
		m_cboVideoTTL.AddString(strNum);
		m_cboAduioTTL.AddString(strNum);
		m_cboMetaDataTTL.AddString(strNum);
	}
	m_cboMultiType.AddString(_T("RTSP"));
	m_cboMultiType.AddString(_T("SRTP"));
	m_cboMultiType.AddString(_T("TS"));
	UI_UpdateUIText();
	m_cboStreamType.SetCurSel(0);
	m_cboVideoTTL.SetCurSel(0);
	m_cboAduioTTL.SetCurSel(0);
	m_cboMetaDataTTL.SetCurSel(0);
	m_cboMultiType.SetCurSel(0);
	return TRUE;
}

void CLS_RTPServerInfo::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
}

void CLS_RTPServerInfo::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_STREAM_TYPE,GetTextByLan(_T("码流类型"), _T("Bitstream type")));
	SetDlgItemText(IDC_STATIC_VIDEO_ADDRESS,GetTextByLan(_T("视频服务器地址"), _T("Video server address")));
	SetDlgItemText(IDC_STATIC_VIDEO_PORT,GetTextByLan(_T("视频服务器端口"), _T("Video server port")));
	SetDlgItemText(IDC_STATIC_VIDEO_TTL,GetTextByLan(_T("视频保活时间"), _T("Video live time")));
	SetDlgItemText(IDC_STATIC_AUDIO_ADDRESS,GetTextByLan(_T("音频服务器地址"), _T("Audio server address")));
	SetDlgItemText(IDC_STATIC_AUDIO_PORT,GetTextByLan(_T("音频服务器端口"), _T("Audio server port")));
	SetDlgItemText(IDC_STATIC_AUDIO_TTL,GetTextByLan(_T("音频保活时间"), _T("Audio live time")));
	SetDlgItemText(IDC_STATIC_METADATA_ADDRESS,GetTextByLan(_T("Medatdata服务器地址"), _T("Medatdata server address")));
	SetDlgItemText(IDC_STATIC_METADATA_PORT,GetTextByLan(_T("Medatdata服务器端口"), _T("Medatdata Server port")));
	SetDlgItemText(IDC_STATIC_METADATA_TTL,GetTextByLan(_T("Medatdata保活时间"), _T("Medatdata TTL")));
	SetDlgItemText(IDC_STATIC_MULTICAST_TYPE,GetTextByLan(_T("组播类型"), _T("Multicast type")));
	m_btGet.SetWindowText(GetTextByLan(_T("获取"), _T("Get")));
	m_btSet.SetWindowText(GetTextByLan(_T("设置"), _T("Set")));
	m_chEnable.SetWindowText(GetTextByLan(_T("使能"), _T("Enable")));
	int iIndex = m_cboStreamType.GetCurSel();
	m_cboStreamType.ResetContent();
	m_cboStreamType.AddString(GetTextByLan(_T("主码流"), _T("Main code stream")));
	m_cboStreamType.AddString(GetTextByLan(_T("副码流"), _T("Sub code stream")));
	m_cboStreamType.AddString(GetTextByLan(_T("三码流"), _T("Three bit stream")));
	m_cboStreamType.SetCurSel(iIndex);
}

void CLS_RTPServerInfo::ChangEnableState()
{
	if(TS_TYPE == m_cboMultiType.GetCurSel())
	{
		m_cboStreamType.EnableWindow(FALSE);
		m_chEnable.EnableWindow(FALSE);
		m_editVideoAddr.EnableWindow(FALSE);
		m_editVideoPort.EnableWindow(FALSE);
		m_cboVideoTTL.EnableWindow(FALSE);
		m_editAudioAddr.EnableWindow(FALSE);
		m_editAudioPort.EnableWindow(FALSE);
		m_cboAduioTTL.EnableWindow(FALSE);
		m_editMetaDataAddr.EnableWindow(FALSE);
		m_editMetaDataPort.EnableWindow(FALSE);
		m_cboMetaDataTTL.EnableWindow(FALSE);
	}
	else
	{
		m_cboStreamType.EnableWindow(TRUE);
		m_chEnable.EnableWindow(TRUE);
		m_editVideoAddr.EnableWindow(TRUE);
		m_editVideoPort.EnableWindow(TRUE);
		m_cboVideoTTL.EnableWindow(TRUE);
		m_editAudioAddr.EnableWindow(TRUE);
		m_editAudioPort.EnableWindow(TRUE);
		m_cboAduioTTL.EnableWindow(TRUE);
		m_editMetaDataAddr.EnableWindow(TRUE);
		m_editMetaDataPort.EnableWindow(TRUE);
		m_cboMetaDataTTL.EnableWindow(TRUE);
	}
}

BEGIN_MESSAGE_MAP(CLS_RTPServerInfo, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET_RTPSERVER, &CLS_RTPServerInfo::OnBnClickedButtonSetRtpserver)
	ON_BN_CLICKED(IDC_BUTTON_GET_RTPSERVER, &CLS_RTPServerInfo::OnBnClickedButtonGetRtpserver)
	ON_CBN_SELCHANGE(IDC_COMBO_MULTICAST_TYPE, &CLS_RTPServerInfo::OnCbnSelchangeComboMulticastType)
END_MESSAGE_MAP()


// CLS_RTPServerInfo message handler

void CLS_RTPServerInfo::OnBnClickedButtonSetRtpserver()
{
	RtpServerInfo tInfo;
	memset(&tInfo, 0, sizeof(RtpServerInfo));
	tInfo.iStreamTyp = m_cboStreamType.GetCurSel() + 1;
	CString strTemp;
	GetDlgItemText(IDC_EDIT_VIDEO_ADDRESS, strTemp);
	if(strTemp.GetLength() < LEN_64) {
		memcpy(tInfo.pcVideoAddress, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_AUDIO_ADDRESS, strTemp);
	if(strTemp.GetLength() < LEN_64) {
		memcpy(tInfo.pcAudioAddress, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_METADATA_ADDRESS, strTemp);
	if(strTemp.GetLength() < LEN_64) {
		memcpy(tInfo.pcMetadataAddress, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	tInfo.iVideoPort = GetDlgItemInt(IDC_EDIT_VIDEO_PORT);
	tInfo.iAudioPort = GetDlgItemInt(IDC_EDIT_AUDIO_PORT);
	tInfo.iMetadataPort = GetDlgItemInt(IDC_EDIT_METADATA_PORT);
	tInfo.iVideoTTL = m_cboVideoTTL.GetCurSel() + 1;
	tInfo.iAudioTTL = m_cboAduioTTL.GetCurSel() + 1;
	tInfo.iMetadataTTL = m_cboMetaDataTTL.GetCurSel() + 1;
	tInfo.iMulticastType = m_cboMultiType.GetCurSel();
	if(IsDlgButtonChecked(IDC_CHECK_RTPSERVERINFO_ENABLE)) {
		tInfo.iEnable = 1;
	}else {
		tInfo.iEnable = 0;
	}
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_RTSPINFO, m_iChannelNO, &tInfo, sizeof(RtpServerInfo));
	if(RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "","CLS_RTPServerInfo::NetClient_SetDevConfig[NET_CLIENT_RTSPINFO] (%d,),iResult = %d", m_iLogonID, iRet);
	} else {
		AddLog(LOG_TYPE_FAIL,"","CLS_RTPServerInfo::NetClient_SetDevConfig[NET_CLIENT_RTSPINFO] (%d,), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_RTPServerInfo::OnBnClickedButtonGetRtpserver()
{
	RtpServerInfo tInfo;
	memset(&tInfo, 0, sizeof(RtpServerInfo));
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RTSPINFO, m_iChannelNO, &tInfo, sizeof(RtpServerInfo), NULL);
	if(RET_SUCCESS == iRet) {
		if(TS_TYPE != tInfo.iMulticastType)
		{
			m_editVideoAddr.SetWindowText(_T(tInfo.pcVideoAddress));
			m_editAudioAddr.SetWindowText(_T(tInfo.pcAudioAddress));
			m_editMetaDataAddr.SetWindowText(_T(tInfo.pcMetadataAddress));
			CString strNum;
			strNum.Format(_T("%d"), tInfo.iVideoPort);
			m_editVideoPort.SetWindowText(strNum);
			strNum.Format(_T("%d"), tInfo.iAudioPort);
			m_editAudioPort.SetWindowText(strNum);
			strNum.Format(_T("%d"), tInfo.iMetadataPort);
			m_editMetaDataPort.SetWindowText(strNum);
			m_cboStreamType.SetCurSel(tInfo.iStreamTyp - 1);
			m_cboVideoTTL.SetCurSel(tInfo.iVideoTTL - 1);
			m_cboAduioTTL.SetCurSel(tInfo.iAudioTTL - 1);
			m_cboMetaDataTTL.SetCurSel(tInfo.iMetadataTTL - 1);
		}
		m_cboMultiType.SetCurSel(tInfo.iMulticastType);
		AddLog(LOG_TYPE_SUCC, "","CLS_RTPServerInfo::NetClient_GetDevConfig[NET_CLIENT_RTSPINFO] (%d,),iResult = %d", m_iLogonID, iRet);
	} else {
		AddLog(LOG_TYPE_FAIL,"","CLS_RTPServerInfo::NetClient_GetDevConfig[NET_CLIENT_RTSPINFO] (%d,), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_RTPServerInfo::OnCbnSelchangeComboMulticastType()
{
	ChangEnableState();
}

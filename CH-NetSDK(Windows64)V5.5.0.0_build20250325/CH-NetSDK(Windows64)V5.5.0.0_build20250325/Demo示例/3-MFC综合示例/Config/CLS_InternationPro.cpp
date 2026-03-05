//CLS_InternationPro.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include ".\Config\CLS_InternationPro.h"


// CLS_InternationPro dialog

IMPLEMENT_DYNAMIC(CLS_InternationPro, CDialog)

CLS_InternationPro::CLS_InternationPro(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_InternationPro::IDD, pParent)
{

}

CLS_InternationPro::~CLS_InternationPro()
{
}

void CLS_InternationPro::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_STREAM_TYPE, m_cboStreamType);
	DDX_Control(pDX, IDC_COMBO_ADDRESS_TYPE, m_cboAddressType);
	DDX_Control(pDX, IDC_CHECK_RTMP_ENABLE, m_chkRtmpEnable);
	DDX_Control(pDX, IDC_EDIT_IP_ADDRESS, m_edtIpAdress);
	DDX_Control(pDX, IDC_EDIT_PORT, m_edtPort);
	DDX_Control(pDX, IDC_EDIT_USERNAME, m_edtRtmpUserName);
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_edtRtmpPassword);
	DDX_Control(pDX, IDC_EDIT_KEY, m_edtKey);
	DDX_Control(pDX, IDC_COMBO_LAN_NUM, m_cboLanNo);
	DDX_Control(pDX, IDC_COMBO_CONNECT_TYPE, m_cboConnectType);
	DDX_Control(pDX, IDC_COMBO_EAP_TYEP, m_cboEapType);
	DDX_Control(pDX, IDC_COMBO_EAPOL_TYPE, m_cboEapolType);
	DDX_Control(pDX, IDC_CHECK_8201X_ENABLE, m_chk8021xEnable);
	DDX_Control(pDX, IDC_EDIT_8021X_USERNAME, m_edtUsername8201x);
	DDX_Control(pDX, IDC_EDIT_8021X_PASSWORD, m_edtPassoword8021x);
	DDX_Control(pDX, IDC_COMBO_CONNECTTYPE, m_cboConnectState);
	DDX_Control(pDX, IDC_COMBO_TIME_MODE, m_cboTimeMode);
	DDX_Control(pDX, IDC_EDIT_VIDEO_VOVV1_TEXT, m_edtVideoCovV1Text);
}


BEGIN_MESSAGE_MAP(CLS_InternationPro, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_RTMP_SET, &CLS_InternationPro::OnBnClickedButtonRtmpSet)
	ON_BN_CLICKED(IDC_BUTTON_8021X_SET, &CLS_InternationPro::OnBnClickedButton8021xSet)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_COMBO_ADDRESS_TYPE, &CLS_InternationPro::OnCbnSelchangeComboAddressType)
	ON_CBN_SELCHANGE(IDC_COMBO_LAN_NUM, &CLS_InternationPro::OnCbnSelchangeComboLanNum)
	ON_BN_CLICKED(IDC_BUTTON_TIME_MODE_SET, &CLS_InternationPro::OnBnClickedButtonTimeModeSet)
END_MESSAGE_MAP()

BOOL CLS_InternationPro::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  add extra initialization here
	UpdateUIText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_InternationPro::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_InternationPro::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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
	if (_iStreamNo < 0)
	{
		m_iStreamNo = 0;
	}
	else
	{
		m_iStreamNo = _iStreamNo;
	}

	UpdatePageUI();
}

void CLS_InternationPro::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();
	UpdatePageUI();
}
void CLS_InternationPro::UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_STREAM_TYPE, GetTextByLan("码流类型", "STREAMTYPE"));
	SetDlgItemText(IDC_STATIC_ADDRESS_TYPE, GetTextByLan("地址类型", "ADDRESSTYPE"));
	SetDlgItemText(IDC_CHECK_RTMP_ENABLE, GetTextByLan("使能", "enable"));
	SetDlgItemText(IDC_STATIC_USERNAME, GetTextByLan("用户名", "USERNAME"));
	SetDlgItemText(IDC_STATIC_IP_ADDRESS, GetTextByLan("地址", "IP"));
	SetDlgItemText(IDC_STATIC_PORT, GetTextByLan("端口", "PORT"));
	SetDlgItemText(IDC_STATIC_PASSWORD, GetTextByLan("密码", "PASSWORD"));
	SetDlgItemText(IDC_BUTTON_RTMP_SET, GetTextByLan("设置", "SET"));
	SetDlgItemText(IDC_STATIC_LAN_NUM, GetTextByLan("网卡编号", "LANNUM"));
	SetDlgItemText(IDC_STATIC_CONNECT_TYPE, GetTextByLan("连接方式", "CONNECTTYPE"));
	SetDlgItemText(IDC_STATIC_EAP_TYEP, GetTextByLan("校验类型", "EAPTYEP"));
	SetDlgItemText(IDC_STATIC_EAPOL_TYPE, GetTextByLan("协议类型", "EAPOLTYPE"));
	SetDlgItemText(IDC_CHECK_8201X_ENABLE, GetTextByLan("使能", "enable"));
	SetDlgItemText(IDC_STATIC_8021X_USERNAME, GetTextByLan("用户名", "USERNAME"));
	SetDlgItemText(IDC_STATIC_8021X_PASSWORD, GetTextByLan("密码", "PASSWORD"));
	SetDlgItemText(IDC_STATIC_CONNECTTYPE, GetTextByLan("连接状态", "state"));
	SetDlgItemText(IDC_BUTTON_8021X_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_STATIC_TIME_MODE, GetTextByLan("校时模式", "TMIEMODE"));

	m_cboStreamType.ResetContent();
	m_cboStreamType.InsertString(0, "Main");
	m_cboStreamType.InsertString(1, "Sub");
	m_cboStreamType.InsertString(2, "Three");
	m_cboStreamType.SetCurSel(0);
	
	m_cboAddressType.ResetContent();
	m_cboAddressType.InsertString(0, "Custom");
	m_cboAddressType.InsertString(1, "Non-custom");
	m_cboAddressType.SetCurSel(0);

	m_cboLanNo.ResetContent();
	m_cboLanNo.InsertString(0, "Lan1");
	m_cboLanNo.InsertString(1, "Lan2");
	m_cboLanNo.InsertString(2, "Lan3");
	m_cboLanNo.InsertString(3, "Lan4");
	m_cboLanNo.InsertString(4, "Lan5");
	m_cboLanNo.InsertString(5, "Lan6");
	m_cboLanNo.InsertString(6, "Lan7");
	m_cboLanNo.InsertString(7, "Lan8");
	m_cboLanNo.SetCurSel(0);

	m_cboConnectType.ResetContent();
	m_cboConnectType.InsertString(0, "Auto");
	m_cboConnectType.InsertString(1, "Manual");
	m_cboConnectType.SetCurSel(0);

	m_cboEapType.ResetContent();
	m_cboEapType.InsertString(0, "EAP-MD5");
	m_cboEapType.SetCurSel(0);

	m_cboEapolType.ResetContent();
	m_cboEapolType.InsertString(0, "X-2001");
	m_cboEapolType.InsertString(1, "X-2004");
	m_cboEapolType.SetCurSel(0);

	m_cboConnectState.ResetContent();
	m_cboConnectState.InsertString(0, "NotConnect");
	m_cboConnectState.InsertString(1, "Connected");
	m_cboConnectState.InsertString(2, "Connecting");
	m_cboConnectState.InsertString(3, "Failed");
	m_cboConnectState.SetCurSel(0);

	m_cboTimeMode.ResetContent();
	m_cboTimeMode.InsertString(0, "Unuse");
	m_cboTimeMode.InsertString(1, "NTP");
	m_cboTimeMode.InsertString(2, "GPS");
	m_cboTimeMode.InsertString(3, "MANUAL");
	m_cboTimeMode.InsertString(4, "ONVIF");
	m_cboTimeMode.SetCurSel(0);
}

void CLS_InternationPro::UpdatePageUI()
{
	OnCbnSelchangeComboAddressType();
	OnCbnSelchangeComboLanNum();
	OnBnClickedButtonVideoCovv1Get();

	int iRetValue = 0;
	ChkTimeMode tMode = {0};
	tMode.iBufSize = sizeof(tMode);
	int iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_TIME_MODE,m_iChannelNO,&tMode,tMode.iBufSize,&iRetValue);
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_InternationPro::UpdatePageUI] GetDevConfig[TIME_MODE] failed,LogonID=%d",m_iLogonID);
		m_cboTimeMode.SetCurSel(-1);
	}
	else
	{
		m_cboTimeMode.SetCurSel(tMode.iChkMode);
	}

}

void CLS_InternationPro::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if(NULL == _pPara)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_InternationPro::OnParamChangeNotify NULL == _pPara");
		return;
	}
	STR_Para* strPara;
	strPara = (STR_Para*) _pPara;

	switch(_iParaType)
	{
	case PARA_IEEE8021X_STATE:
		{
			IEEE8021XState* tInfo = (IEEE8021XState*)strPara->m_iPara[0];
			m_cboLanNo.SetCurSel(tInfo->iLanNo);
			m_cboConnectState.SetCurSel(tInfo->iConnectState);
			break;
		}
	default:
		break;
	}
}

void CLS_InternationPro::OnBnClickedButtonRtmpSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnBnClickedButtonRtmpSet::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	CommonRtmp tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iStreamType = m_cboStreamType.GetCurSel();
	tInfo.iAddressType = m_cboAddressType.GetCurSel() + 1;
	tInfo.iEnable = m_chkRtmpEnable.GetCheck();
	tInfo.iPort =  GetDlgItemInt(IDC_EDIT_PORT);
	GetDlgItemText(IDC_EDIT_IP_ADDRESS, tInfo.cIpAddress, LEN_256);
	GetDlgItemText(IDC_EDIT_USERNAME, tInfo.cUserName, LEN_64);
	GetDlgItemText(IDC_EDIT_PASSWORD, tInfo.cPassWord, LEN_64);
	GetDlgItemText(IDC_EDIT_KEY, tInfo.cKey, LEN_64);

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_COMMON_RTMP, m_iChannelNO, &tInfo, sizeof(tInfo));
	if(TD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_InternationPro::OnBnClickedButton8021xSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnBnClickedButton8021xSet::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}
	IEEE8021X tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iConnectType = m_cboConnectType.GetCurSel();
	tInfo.iLanNo = m_cboLanNo.GetCurSel();
	tInfo.iEapolVersion = m_cboEapolType.GetCurSel() + 1;
	tInfo.iEapType = m_cboEapType.GetCurSel();
	tInfo.iEnable = m_chk8021xEnable.GetCheck();
	GetDlgItemText(IDC_EDIT_8021X_USERNAME, tInfo.cUserName, LEN_64);
	GetDlgItemText(IDC_EDIT_8021X_PASSWORD, tInfo.cPassWord, LEN_64);

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IEEE8021X, 0, &tInfo, sizeof(tInfo));
	if(TD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}

}

void CLS_InternationPro::OnCbnSelchangeComboAddressType()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnCbnSelchangeComboAddressType::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	CommonRtmp tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iAddressType = m_cboAddressType.GetCurSel() + 1;
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_COMMON_RTMP, m_iChannelNO, &tInfo, sizeof(tInfo), &iBytesReturned);
	if(TD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		m_cboStreamType.SetCurSel(tInfo.iStreamType);
		m_chkRtmpEnable.SetCheck(tInfo.iEnable);
		SetDlgItemInt(IDC_EDIT_PORT,tInfo.iPort);
		SetDlgItemText(IDC_EDIT_IP_ADDRESS, tInfo.cIpAddress);
		SetDlgItemText(IDC_EDIT_USERNAME, tInfo.cUserName);
		SetDlgItemText(IDC_EDIT_PASSWORD, tInfo.cPassWord);
		SetDlgItemText(IDC_EDIT_KEY, tInfo.cKey);
	}
}

void CLS_InternationPro::OnCbnSelchangeComboLanNum()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnCbnSelchangeComboLanNum::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}
	IEEE8021X tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iLanNo = m_cboLanNo.GetCurSel();
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IEEE8021X, 0, &tInfo, sizeof(tInfo), &iBytesReturned);
	if(TD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		m_cboConnectType.SetCurSel(tInfo.iConnectType);
		m_cboEapolType.SetCurSel(tInfo.iEapolVersion - 1);
		m_cboEapType.SetCurSel(tInfo.iEapType);
		m_chk8021xEnable.SetCheck(tInfo.iEnable);
		SetDlgItemText(IDC_EDIT_8021X_USERNAME, tInfo.cUserName);
		SetDlgItemText(IDC_EDIT_8021X_PASSWORD, tInfo.cPassWord);
	}

	IEEE8021XState tInfoState = {0};
	tInfo.iSize = sizeof(tInfoState);
	tInfo.iLanNo = m_cboLanNo.GetCurSel();
	iBytesReturned = 0;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IEEE8021X, 0, &tInfoState, sizeof(tInfoState), &iBytesReturned);
	if(TD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		m_cboConnectState.SetCurSel(tInfoState.iConnectState);
		
	}
}

void CLS_InternationPro::OnBnClickedButtonTimeModeSet()
{

	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnBnClickedButtonTimeModeSet::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	ChkTimeMode tMode = {0};
	tMode.iBufSize = sizeof(tMode);
	tMode.iChkMode = m_cboTimeMode.GetCurSel();
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_TIME_MODE,m_iChannelNO,&tMode,tMode.iBufSize);
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_InternationPro::OnBnClickedButtonTimeModeSet] SetDevConfig[TIME_MODE] failed,LogonID=%d",m_iLogonID);
	}
}

void CLS_InternationPro::OnBnClickedButtonVideoCovv1Get()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnBnClickedButtonVideoCovv1Get::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	CommonRECT tInfo[8] = {0};
	int iBytesReturned = 0;

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_VIDEO_COVER, m_iChannelNO, &tInfo, sizeof(CommonRECT), &iBytesReturned);
	if(TD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig(%d,%d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		CString strContent;
		for(int i = 0; i < 8; ++i)
		{
			CString strTmp;
			strTmp.Format("(%d,%d,%d,%d)", tInfo[i].left, tInfo[i].top, tInfo[i].right, tInfo[i].bottom);
			strContent += strTmp;
		}

		SetDlgItemText(IDC_EDIT_VIDEO_VOVV1_TEXT, strContent);
	}
}

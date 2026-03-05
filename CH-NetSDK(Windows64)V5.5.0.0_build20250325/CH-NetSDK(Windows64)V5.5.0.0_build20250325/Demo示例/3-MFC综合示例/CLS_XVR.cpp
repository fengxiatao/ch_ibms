// CLS_XVR.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_XVR.h"

// CLS_XVR dialog

IMPLEMENT_DYNAMIC(CLS_XVR, CDialog)

CLS_XVR::CLS_XVR(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_XVR::IDD, pParent)
{
	memset(m_iPortEnable, 0, sizeof(m_iPortEnable));
}

CLS_XVR::~CLS_XVR()
{
}

void CLS_XVR::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHK_ALM_LOOP_DETEC_STATE, m_ChkLoopDetecState);
	DDX_Control(pDX, IDC_COMBO_LOOP_CHANNEL, m_CboLoopChannelNo);
	DDX_Control(pDX, IDC_COMBO_LOOP_PORT, m_CboLoopPortNo);
	DDX_Control(pDX, IDC_COMBO_ID_TYPE_CHANNEL, m_CboIDTypeChannelNo);
	DDX_Control(pDX, IDC_COMBO_PROTOCOL_TYPE, m_CboProtocolType);
	DDX_Control(pDX, IDC_CHK_BASIC_STATE, m_ChkBasicState);
	DDX_Control(pDX, IDC_CHK_DIGEST_STATE, m_ChkDigestState);
	DDX_Control(pDX, IDC_COMBO_DECNAME_CHANNO, m_CboDeviceNameChannNo);
	DDX_Control(pDX, IDC_EDIT_DEVICENAME, m_EditDeviceName);
	DDX_Control(pDX, IDC_COMBO_ALGOTYPE, m_cboalgoType);
	DDX_Control(pDX, IDC_CHECK_WSSE, m_chkWsse);
}


BEGIN_MESSAGE_MAP(CLS_XVR, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_LOOP_DETEC_SET, &CLS_XVR::OnBnClickedButtonLoopDetecSet)
	ON_BN_CLICKED(IDC_BUTTON_ID_TYPE_SET, &CLS_XVR::OnBnClickedButtonIdTypeSet)
	ON_CBN_SELCHANGE(IDC_COMBO_LOOP_CHANNEL, &CLS_XVR::OnCbnSelchangeComboLoopChannel)
	ON_CBN_SELCHANGE(IDC_COMBO_PROTOCOL_TYPE, &CLS_XVR::OnCbnSelchangeComboProtocolType)
	ON_CBN_SELCHANGE(IDC_COMBO_LOOP_PORT, &CLS_XVR::OnCbnSelchangeComboLoopPort)
	//ON_CBN_SELCHANGE(IDC_COMBO_ID_TYPE_CHANNEL, &CLS_XVR::OnCbnSelchangeComboIdTypeChannel)
	ON_BN_CLICKED(IDC_BUTTON_DEVICENAME_SET, &CLS_XVR::OnBnClickedButtonDevicenameSet)
END_MESSAGE_MAP()


// CLS_XVR message handler
BOOL CLS_XVR::OnInitDialog()
{
	CDialog::OnInitDialog();

	UpdateUI();

	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("0")), 0);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("1")), 1);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("2")), 2);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("3")), 3);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("4")), 4);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("5")), 5);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("6")), 6);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("7")), 7);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("8")), 8);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("9")), 9);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("10")), 10);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("11")), 11);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("12")), 12);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("13")), 13);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("14")), 14);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("15")), 15);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("16")), 16);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("17")), 17);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("18")), 18);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("19")), 19);
	m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(_T("20")), 20);
	m_CboLoopPortNo.SetCurSel(0);

	return TRUE;  
}

void CLS_XVR::UpdateUI()
{
	m_ChkLoopDetecState.SetCheck(0);
	m_ChkBasicState.SetCheck(0);
	m_ChkDigestState.SetCheck(0);

	m_CboProtocolType.ResetContent();
	m_CboProtocolType.SetItemData(m_CboProtocolType.AddString(GetTextByLan(_T("rtsp协议"), _T("rtsp Protocol"))), 0);
	m_CboProtocolType.SetItemData(m_CboProtocolType.AddString(GetTextByLan(_T("http协议"), _T("http Protocol"))), 1);
	m_CboProtocolType.SetItemData(m_CboProtocolType.AddString(GetTextByLan(_T("onvif协议"), _T("onvif Protocol"))), 2);
	m_CboProtocolType.SetCurSel(0);

	m_cboalgoType.ResetContent();
	m_cboalgoType.SetItemData(m_cboalgoType.AddString(GetTextByLan(_T("无算法"), _T("NULL"))), 0);
	m_cboalgoType.SetItemData(m_cboalgoType.AddString(GetTextByLan(_T("MD5"), _T("MD5"))), 1);
	m_cboalgoType.SetItemData(m_cboalgoType.AddString(GetTextByLan(_T("SHA256"), _T("SHA256"))), 2);
	m_cboalgoType.SetItemData(m_cboalgoType.AddString(GetTextByLan(_T("MD5/SHA256"), _T("MD5/SHA256"))), 3);
	m_cboalgoType.SetCurSel(0);
	SetDlgItemText(IDC_STATIC_LOOP_DEC, GetTextByLan(_T("报警输入环路检测状态"), _T("Set AlmLoopDetec State")));
	SetDlgItemText(IDC_STATIC_LOOP_DEC_STATE, GetTextByLan(_T("启用"), _T("Enable")));
	SetDlgItemText(IDC_STATIC_LOOP_DEC_CHANNO, GetTextByLan(_T("通道号"), _T("Channel Num")));
	SetDlgItemText(IDC_STATIC_LOOP_DEC_PORTNO, GetTextByLan(_T("端口号"), _T("Port Num")));
	SetDlgItemText(IDC_BUTTON_LOOP_DETEC_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_ID_TYPE, GetTextByLan(_T("协议认证类型"), _T("ID Type")));
	SetDlgItemText(IDC_STATIC_IDTYPE_CHANNNO, GetTextByLan(_T("通道号"), _T("Channel Num")));
	SetDlgItemText(IDC_STATIC_IDTYPE_PROTOCOLTYPE, GetTextByLan(_T("协议类型"), _T("Protocol Type")));
	SetDlgItemText(IDC_STATIC_IDTYPE_BASIC, GetTextByLan(_T("Basic认证类型"), _T("Basic Auth Config")));
	SetDlgItemText(IDC_STATIC_IDTYPE_DIGEST, GetTextByLan(_T("Digest认证类型"), _T("Digest Auth Config")));
	SetDlgItemText(IDC_BUTTON_ID_TYPE_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_ALGOTYPE, GetTextByLan(_T("摘要算法"), _T("AlgoType")));
	SetDlgItemText(IDC_STATIC_WSSE, GetTextByLan(_T("Wsse认证"), _T("Wsse iDentify")));
}

void CLS_XVR::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else 
	{
		m_iLogonID = _iLogonID;
	}
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;    
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}

	int iChannelNum = 0;
	NetClient_GetChannelNum(_iLogonID, &iChannelNum);
	m_CboLoopChannelNo.ResetContent();
	m_CboIDTypeChannelNo.ResetContent();
	m_CboDeviceNameChannNo.ResetContent();
	int i = 0;
	for(i=0; iChannelNum > i; i++)
	{
		m_CboLoopChannelNo.SetItemData(m_CboLoopChannelNo.AddString(IntToCString(i)), i);
		m_CboIDTypeChannelNo.SetItemData(m_CboIDTypeChannelNo.AddString(IntToCString(i)), i);
		m_CboDeviceNameChannNo.SetItemData(m_CboDeviceNameChannNo.AddString(IntToCString(i)),i);
	}

	m_CboLoopChannelNo.SetItemData(m_CboLoopChannelNo.AddString(_T("NVR")), 0x7fffffff);
	m_CboIDTypeChannelNo.SetItemData(m_CboIDTypeChannelNo.AddString(_T("NVR")), 0x7fffffff);
	m_CboDeviceNameChannNo.SetItemData(m_CboDeviceNameChannNo.AddString(_T("NVR")), 0x7fffffff);

	m_CboLoopChannelNo.SetCurSel(i);
	m_CboIDTypeChannelNo.SetCurSel(i);
	m_CboDeviceNameChannNo.SetCurSel(i);

	UpdatePortParameter();
	UpdateProtocolConfigParameter();
	UpdateDeviceName();
}

void CLS_XVR::OnLanguageChanged(int _iLanguage)
{
	UpdateUI();
}

void CLS_XVR::UpdatePortParameter()
{
	AlmLoopDetec tInfo[MAX_ALMLOOPDETEC_PORT_NUM] = {0};
	int iReturn = -1;
	int iChannelNo = m_CboLoopChannelNo.GetItemData(m_CboLoopChannelNo.GetCurSel());
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ALM_LOOP_DETEC, iChannelNo, tInfo, sizeof(AlmLoopDetec), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_ALM_LOOP_DETEC fail!");
	}
	else
	{
		m_CboLoopPortNo.ResetContent();
		memset(m_iPortEnable, 0, sizeof(m_iPortEnable));
		for( int i=0; MAX_ALMLOOPDETEC_PORT_NUM > i; i++)
		{
			if (NULL != tInfo[i].iSize)
			{
				m_CboLoopPortNo.SetItemData(m_CboLoopPortNo.AddString(IntToCString(tInfo[i].iPortNo)), i);
				m_iPortEnable[i] = tInfo[i].iEnable;
			}
		}
		m_ChkLoopDetecState.SetCheck(FALSE);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_ALM_LOOP_DETEC success!");
	}
}

void CLS_XVR::UpdateProtocolConfigParameter()
{
	IdentificationType tInfo = {0};
	int iReturn = -1;
	int iChannelNo = m_CboIDTypeChannelNo.GetItemData(m_CboIDTypeChannelNo.GetCurSel());
	tInfo.iProtocol = m_CboProtocolType.GetItemData(m_CboProtocolType.GetCurSel());
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IDENTIFICATION_TYPE, iChannelNo, &tInfo, sizeof(IdentificationType), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_IDENTIFICATION_TYPE fail!");
	}
	else
	{
		m_ChkBasicState.SetCheck(tInfo.ibasicConfig);
		m_ChkDigestState.SetCheck(tInfo.iDigestlConfig);
		m_chkWsse.SetCheck(tInfo.iWSSEConfig);
		m_cboalgoType.SetCurSel(tInfo.iAbstractAlgorithm);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_IDENTIFICATION_TYPE success!");
	}
}

void CLS_XVR::UpdateDeviceName()
{
	DeviceName tInfo = {0};
	int iReturn = -1;
	int iChannelNo = m_CboDeviceNameChannNo.GetItemData(m_CboDeviceNameChannNo.GetCurSel());
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DEVICENAME, iChannelNo, &tInfo, sizeof(DeviceName), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_DEVICENAME fail!");
	}
	else
	{
		m_EditDeviceName.SetWindowText((LPCTSTR)(tInfo.pcDeviceName));
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_DEVICENAME success!");
	}
}

void CLS_XVR::OnBnClickedButtonLoopDetecSet()
{
	Sleep(1000);
	AlmLoopDetec tInfo = {0};
	CString strTemp;
	m_CboLoopChannelNo.GetWindowText(strTemp);
	if (0 == strTemp.CompareNoCase("NVR"))
	{
		tInfo.iChannelNo = 0x7fffffff;
	}
	else
	{
		tInfo.iChannelNo = m_CboLoopChannelNo.GetItemData(m_CboLoopChannelNo.GetCurSel());
	}
	tInfo.iPortNo = m_CboLoopPortNo.GetItemData(m_CboLoopPortNo.GetCurSel());
	tInfo.iEnable = m_ChkLoopDetecState.GetCheck();
	tInfo.iSize = sizeof(tInfo);
	int iRet = RET_FAILED;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_ALM_LOOP_DETEC, tInfo.iChannelNo, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ALM_LOOP_DETEC fail!");
	}

	AlmLoopDetec tInfoArray[MAX_ALMLOOPDETEC_PORT_NUM] = {0};
	int iReturn = -1;
	int iChannelNo = m_CboLoopChannelNo.GetItemData(m_CboLoopChannelNo.GetCurSel());
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ALM_LOOP_DETEC, iChannelNo, tInfoArray, sizeof(AlmLoopDetec), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_ALM_LOOP_DETEC fail!");
	}
	else
	{
		memset(m_iPortEnable, 0, sizeof(m_iPortEnable));
		for( int i=0; MAX_ALMLOOPDETEC_PORT_NUM > i; i++)
		{
			if (NULL != tInfoArray[i].iSize)
			{
				m_iPortEnable[i] = tInfoArray[i].iEnable;
			}
		}
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_ALM_LOOP_DETEC success!");
	}
}

void CLS_XVR::OnBnClickedButtonIdTypeSet()
{
	Sleep(1000);
	IdentificationType tInfo = {0};
	CString strTemp;
	m_CboIDTypeChannelNo.GetWindowText(strTemp);
	if (0 == strTemp.CompareNoCase("NVR"))
	{
		tInfo.iChannelNo = 0x7fffffff;
	}
	else
	{
		tInfo.iChannelNo = m_CboIDTypeChannelNo.GetItemData(m_CboIDTypeChannelNo.GetCurSel());
	}
	tInfo.iProtocol = m_CboProtocolType.GetItemData(m_CboProtocolType.GetCurSel());
	tInfo.ibasicConfig = m_ChkBasicState.GetCheck();
	tInfo.iDigestlConfig = m_ChkDigestState.GetCheck();
	tInfo.iAbstractAlgorithm = m_cboalgoType.GetItemData(m_cboalgoType.GetCurSel());
	tInfo.iWSSEConfig = m_chkWsse.GetCheck();
	tInfo.iSize = sizeof(tInfo);
	int iRet = RET_FAILED;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IDENTIFICATION_TYPE, tInfo.iChannelNo, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_IDENTIFICATION_TYPE fail!");
	}
	UpdateProtocolConfigParameter();
}

void CLS_XVR::OnCbnSelchangeComboLoopChannel()
{
	UpdatePortParameter();
}

void CLS_XVR::OnCbnSelchangeComboProtocolType()
{
	UpdateProtocolConfigParameter();
}

void CLS_XVR::OnCbnSelchangeComboLoopPort()
{
	int iIndex = m_CboLoopPortNo.GetItemData(m_CboLoopPortNo.GetCurSel());
	if (0 <= iIndex )
	{
		m_ChkLoopDetecState.SetCheck(m_iPortEnable[iIndex]);
	}
}


void CLS_XVR::OnBnClickedButtonDevicenameSet()
{
	DeviceName tInfo = {0};
	CString strTemp;
	m_CboDeviceNameChannNo.GetWindowText(strTemp);
	if (0 == strTemp.CompareNoCase("NVR"))
	{
		tInfo.iChannelNo = 0x7fffffff;
	}
	else
	{
		tInfo.iChannelNo = m_CboDeviceNameChannNo.GetItemData(m_CboDeviceNameChannNo.GetCurSel());
	}
	CString SName;
	m_EditDeviceName.GetWindowText(SName);
	strncpy(tInfo.pcDeviceName, SName, LEN_32);
	tInfo.iSize = sizeof(tInfo);
	int iRet = RET_FAILED;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DEVICENAME, tInfo.iChannelNo, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_DEVICENAME fail!");
	}
	UpdateDeviceName();
}

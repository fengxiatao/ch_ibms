
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include ".\Config\CLS_NetManage.h"


// CLS_NetManage dialog

IMPLEMENT_DYNAMIC(CLS_NetManage, CDialog)

CLS_NetManage::CLS_NetManage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_NetManage::IDD, pParent)
{
	
}

CLS_NetManage::~CLS_NetManage()
{
}

void CLS_NetManage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_NETTEST_TYPE, m_cboTestType);
	DDX_Control(pDX, IDC_EDIT_CARDNUM, m_edtCardNum);
	DDX_Control(pDX, IDC_IPADDRESS_NETTEST_IP, m_IPAddr);
	DDX_Control(pDX, IDC_COMBO_NETTEST_REACHABLE, m_cboReachable);
	DDX_Control(pDX, IDC_COMBO_INDEX, m_cboIpIndex);
	DDX_Control(pDX, IDC_IPADDRESS_IP, m_IPAdress);
	DDX_Control(pDX, IDC_EDIT_ABILITITY, m_edtAblitity);
	DDX_Control(pDX, IDC_COMBO_ELEANTISHAKE, m_cboStatus);
}


BEGIN_MESSAGE_MAP(CLS_NetManage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_NetManage::OnBnClickedButtonSet)
	ON_BN_CLICKED(IDC_BUTTON_IP_SET, &CLS_NetManage::OnBnClickedButtonIpSet)
	ON_CBN_SELCHANGE(IDC_COMBO_INDEX, &CLS_NetManage::OnCbnSelchangeComboIndex)
	ON_BN_CLICKED(IDC_BUTTON_ELEANTISHAKE, &CLS_NetManage::OnBnClickedButtonEleantishake)
	ON_CBN_SELCHANGE(IDC_COMBO_ELEANTISHAKE, &CLS_NetManage::OnCbnSelchangeComboEleantishake)
END_MESSAGE_MAP()


// CLS_NetManage message handler
void CLS_NetManage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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
	UpdateUIText();

	UpdateData();
}

void CLS_NetManage::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();

	UpdateData();
}

void CLS_NetManage:: OnMainNotify(long _iWParam,void* _iLParam)
{
	if(WCM_NET_TEST == _iWParam)
	{
		UpdateTestResult(_iLParam);
	}

}

void CLS_NetManage::UpdateTestResult(void * _pParam)
{
	NetTestResult tInfo = *(NetTestResult *)_pParam;
	m_cboReachable.SetCurSel(tInfo.iReachable);
	SetDlgItemInt(IDC_EDIT_NET_DELAY_TIME, tInfo.iDelayTime);
	SetDlgItemInt(IDC_EDIT_LOST_RATE, tInfo.iLostRate);
}

void CLS_NetManage::OnBnClickedButtonSet()
{
	NetTestPara tInfo = {0};

	CString sIp;
	m_IPAddr.GetWindowText(sIp);
	tInfo.iTestType = m_cboTestType.GetCurSel() +1;
	tInfo.iCardNum = GetDlgItemInt(IDC_EDIT_CARDNUM);
	strcpy(tInfo.cIp, sIp);
	tInfo.iPort = GetDlgItemInt(IDC_EDIT_NETTEST_PORT);
	GetDlgItemText(IDC_IPADDRESS_NETTEST_IP, tInfo.cReserve, sizeof(tInfo.cReserve));
	if(IsValidIPv6(tInfo.cReserve) >= 1)
	{
		memset(tInfo.cIp,0,16);
	}
	else
	{
		memset(tInfo.cReserve,0,sizeof(tInfo.cReserve));
	}
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_NETTEST, m_iChannelNO, &tInfo, sizeof(tInfo));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand][COMMAND_ID_VCAFPGA_QUERYINFO] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SendCommand][COMMAND_ID_VCAFPGA_QUERYINFO] Set Success", m_iLogonID);
	}
}

void CLS_NetManage::UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_NETTEST_TYPE, GetTextByLan(_T("测试类型"), _T("Test Type")));
	SetDlgItemText(IDC_STATIC_CARDNUM, GetTextByLan(_T("网口"), _T("CardNum")));
	SetDlgItemText(IDC_STATIC_NETTEST_PORT, GetTextByLan(_T("端口"), _T("Port")));
	SetDlgItemText(IDC_STATIC_NETTEST_REACHABLE, GetTextByLan(_T("是否可达"), _T("Reachable")));
	SetDlgItemText(IDC_STATIC_NET_DELAY_TIME, GetTextByLan(_T("网络延时"), _T("DelayTime")));
	SetDlgItemText(IDC_STATIC_LOST_RATE, GetTextByLan(_T("丢包率"), _T("LostRate")));
	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_IP_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_INDEX, GetTextByLan(_T("序号"), _T("Index")));

	m_cboTestType.ResetContent();
	m_cboTestType.InsertString(0, "1-ping");

	m_cboReachable.ResetContent();
	m_cboReachable.InsertString(0,"Reachable");
	m_cboReachable.InsertString(1,"UnReachable");

	m_cboIpIndex.ResetContent();
	m_cboIpIndex.InsertString(0,"1");
	m_cboIpIndex.InsertString(1,"2");
	m_cboIpIndex.InsertString(2,"3");
	m_cboIpIndex.InsertString(3,"4");
	m_cboIpIndex.SetCurSel(0);
	m_IPAdress.ClearAddress();

	m_cboStatus.ResetContent();
	m_cboStatus.InsertString(0,"0");
	m_cboStatus.InsertString(1,"1");
	m_cboStatus.InsertString(2,"2");
	m_cboStatus.SetCurSel(0);
}


BOOL CLS_NetManage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	UpdateData();
	return TRUE;  
}

void CLS_NetManage::UpdateData()
{
	if(m_iLogonID < 0)
		return;
	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_NET;
	stFuncAbilityLevel.iSubFuncType = 14;
	int iReturnByte = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, &stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (RET_SUCCESS == iRet)
	{
		SetDlgItemInt(IDC_EDIT_ABILITITY, _ttoi(stFuncAbilityLevel.cParam) );
	}
	else
	{

	}
	OnCbnSelchangeComboIndex();
}

void CLS_NetManage::OnBnClickedButtonIpSet()
{
	RetransInfo tInfo = {0};

	CString sIp;
	m_IPAdress.GetWindowText(sIp);
	tInfo.iIndex = m_cboIpIndex.GetCurSel() ;
	strcpy(tInfo.cIp, sIp);
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_RETRANS_INFO, m_iChannelNO, &tInfo, sizeof(RetransInfo));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_VCASetConfig::VCA_CMD_RETRANS_INFO fail!");
	}
}

void CLS_NetManage::OnCbnSelchangeComboIndex()
{
	RetransInfo tInfo = {0};
	tInfo.iIndex = m_cboIpIndex.GetCurSel() ;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RETRANS_INFO, m_iChannelNO, &tInfo, sizeof(RetransInfo));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_VCAGetConfig::VCA_CMD_RETRANS_INFO fail!");
	}
	else
	{
		m_IPAdress.SetWindowText(tInfo.cIp);
	}
}

void CLS_NetManage::OnBnClickedButtonEleantishake()
{
	EleAntiShake  tEleAntiShake = {0};
	tEleAntiShake.iSize = sizeof(EleAntiShake);
	tEleAntiShake.iStatus = m_cboStatus.GetCurSel();
	tEleAntiShake.iValue = GetDlgItemInt(IDC_EDIT_ELEANTISHAKE);;

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_ELE_ANTI_SHAKE, m_iChannelNO, (void*)&tEleAntiShake, sizeof(tEleAntiShake));

	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig NET_CLIENT_ELE_ANTI_SHAKE(%d,%d)",m_iLogonID,m_iChannelNO);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig NET_CLIENT_ELE_ANTI_SHAKE(%d,%d)",m_iLogonID,m_iChannelNO);
	}
}

void CLS_NetManage::OnCbnSelchangeComboEleantishake()
{
	EleAntiShake sParam = {0};
	sParam.iSize = sizeof(EleAntiShake);
	sParam.iStatus = m_cboStatus.GetCurSel();
	int iBytesReturned = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ELE_ANTI_SHAKE, m_iChannelNO, &sParam, sizeof(sParam), &iBytesReturned);
    if(iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig::NET_CLIENT_ELE_ANTI_SHAKE fail!");
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_ELEANTISHAKE,sParam.iValue );

	}
}

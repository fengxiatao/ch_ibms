// CLS_DlgFDDFunction.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgFDDFunction.h"


// CLS_DlgFDDFunction dialog

IMPLEMENT_DYNAMIC(CLS_DlgFDDFunction, CDialog)

CLS_DlgFDDFunction::CLS_DlgFDDFunction(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgFDDFunction::IDD, pParent)
{

}

CLS_DlgFDDFunction::~CLS_DlgFDDFunction()
{
}

void CLS_DlgFDDFunction::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_FDDENABLE, m_cboFddRfEnable);
	DDX_Control(pDX, IDC_COMBO_TDDENABLE, m_cboTddRf);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_cboType);
	DDX_Control(pDX, IDC_COMBO_STATUS, m_cboRfStatus);
	DDX_Control(pDX, IDC_CHECKHEARTBEAT, m_chkHeartBeat);
	DDX_Control(pDX, IDC_COMBO_MOBILE, m_cboMobile);
	DDX_Control(pDX, IDC_COMBO_INTYPE, m_cboInType);
	DDX_Control(pDX, IDC_COMBO_STATIONTYPE, m_cboBaseStation);
	DDX_Control(pDX, IDC_COMBO_STATIONQUERY, m_cboLibQuery);
	DDX_Control(pDX, IDC_COMBO_QUERYINTIME, m_cboQueryTime);
	DDX_Control(pDX, IDC_COMBO_WARNING, m_cboWarning);
}


BEGIN_MESSAGE_MAP(CLS_DlgFDDFunction, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_FDDENABLE, &CLS_DlgFDDFunction::OnBnClickedButtonFddenable)
	ON_BN_CLICKED(IDC_BUTTON_TDDSET, &CLS_DlgFDDFunction::OnBnClickedButtonTddset)
	ON_CBN_SELCHANGE(IDC_COMBO_TYPE, &CLS_DlgFDDFunction::OnCbnSelchangeComboType)
END_MESSAGE_MAP()


// CLS_DlgFDDFunction message handlers
BOOL CLS_DlgFDDFunction::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UpdateUI();
	return TRUE;
}

void CLS_DlgFDDFunction::OnBnClickedButtonFddenable()
{
	// TODO: Add your control notification handler code here
	int iRet = RET_FAILED;
	iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_OPENFDDRF, m_iChannelNo, m_cboFddRfEnable.GetCurSel());

	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgFDDFunction::NetClient_SetCommonEnable] Set Failed ! LogonID %d EnableType %d EnableValue %d", m_iLogonID, CI_COMMON_ID_OPENFDDRF, m_cboFddRfEnable.GetCurSel());
		return;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_DlgFDDFunction::NetClient_SetCommonEnable] Set Success ! LogonID %d EnableType %d EnableValue %d", m_iLogonID, CI_COMMON_ID_OPENFDDRF, m_cboFddRfEnable.GetCurSel());
		return;
	}
}

void CLS_DlgFDDFunction::OnBnClickedButtonTddset()
{
	// TODO: Add your control notification handler code here
	int iRet = RET_FAILED;
	iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_OPENTDDRF, m_iChannelNo, m_cboTddRf.GetCurSel());

	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgFDDFunction::NetClient_SetCommonEnable] Set Failed ! LogonID %d EnableType %d EnableValue %d", m_iLogonID, CI_COMMON_ID_OPENTDDRF, m_cboTddRf.GetCurSel());
		return;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_DlgFDDFunction::NetClient_SetCommonEnable] Set Success ! LogonID %d EnableType %d EnableValue %d", m_iLogonID, CI_COMMON_ID_OPENTDDRF, m_cboTddRf.GetCurSel());
		return;
	}
}

void CLS_DlgFDDFunction::OnCbnSelchangeComboType()
{
	// TODO: Add your control notification handler code here
	UpdateParam();
}

void CLS_DlgFDDFunction::UpdateUI()
{
	SetDlgItemText(IDC_STATIC_FDDTDDENABLE, GetTextByLan(_T("FDD/TDD射频使能"), _T("FDD/TDD Enable")));
	SetDlgItemText(IDC_STATIC_FDDENABLE, GetTextByLan(_T("FDD射频使能"), _T("FDD Enable")));
	SetDlgItemText(IDC_STATIC_TDDENABLE, GetTextByLan(_T("TDD射频使能"), _T("TDD Enable")));
	SetDlgItemText(IDC_BUTTON_FDDENABLE, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_TDDSET, GetTextByLan(_T("设置"), _T("Set")));

	SetDlgItemText(IDC_STATIC_FDDTDDSTATUS, GetTextByLan(_T("FDD/TDD射频状态"), _T("FDD/TDD Status")));
	SetDlgItemText(IDC_STATIC_TYPE, GetTextByLan(_T("模块状态"), _T("Model Type")));
	SetDlgItemText(IDC_CHECKHEARTBEAT, GetTextByLan(_T("是否收到心跳"), _T("Receive HeartBeat")));
	SetDlgItemText(IDC_STATIC_STATUS, GetTextByLan(_T("状态"), _T("Status")));

	SetDlgItemText(IDC_STATIC_IMSI, GetTextByLan(_T("IMSI"), _T("IMSI")));
	SetDlgItemText(IDC_STATIC_DEVICEID, GetTextByLan(_T("设备编号"), _T("Device ID")));
	SetDlgItemText(IDC_STATIC_MSGID, GetTextByLan(_T("消息编号"), _T("Msg ID")));
	SetDlgItemText(IDC_STATIC_TIME, GetTextByLan(_T("采集时间"), _T("Time")));
	SetDlgItemText(IDC_STATIC_IMSINO, GetTextByLan(_T("IMSI号"), _T("IMSI NO")));
	SetDlgItemText(IDC_STATIC_PROVICE, GetTextByLan(_T("归属省"), _T("Province")));
	SetDlgItemText(IDC_STATIC_CITY, GetTextByLan(_T("归属市"), _T("City")));
	SetDlgItemText(IDC_STATIC_MOBILE, GetTextByLan(_T("运营商"), _T("Mobile")));
	SetDlgItemText(IDC_STATIC_INTYPE, GetTextByLan(_T("接入类型"), _T("InType")));
	SetDlgItemText(IDC_STATIC__STATIONTYPE, GetTextByLan(_T("基站类型"), _T("Station Type")));
	SetDlgItemText(IDC_STATIC_STMI, GetTextByLan(_T("STMI号"), _T("STMI No")));
	SetDlgItemText(IDC_STATIC_IMEI, GetTextByLan(_T("IMEI"), _T("IMEI")));
	SetDlgItemText(IDC_STATIC_STATIONQUERY, GetTextByLan(_T("站内查询"), _T("Station Query")));
	SetDlgItemText(IDC_STATIC_QUERYINTIME, GetTextByLan(_T("实时查询"), _T("Query Intime")));
	SetDlgItemText(IDC_STATIC_WARNING, GetTextByLan(_T("归属地警告"), _T("Warning")));

	m_cboFddRfEnable.ResetContent();
	m_cboFddRfEnable.AddString(GetTextByLan(_T("不使能"), _T("DisEnable")));
	m_cboFddRfEnable.AddString(GetTextByLan(_T("使能"), _T("Enable")));
	m_cboFddRfEnable.SetCurSel(0);

	m_cboTddRf.ResetContent();
	m_cboTddRf.AddString(GetTextByLan(_T("不使能"), _T("DisEnable")));
	m_cboTddRf.AddString(GetTextByLan(_T("使能"), _T("Enable")));
	m_cboTddRf.SetCurSel(0);

	m_cboType.ResetContent();
	m_cboType.AddString(GetTextByLan(_T("FDD"), _T("FDD")));
	m_cboType.AddString(GetTextByLan(_T("TDD"), _T("TDD")));
	m_cboType.SetCurSel(0);

	m_cboRfStatus.ResetContent();
	m_cboRfStatus.AddString(GetTextByLan(_T("关闭"), _T("Closed")));
	m_cboRfStatus.AddString(GetTextByLan(_T("开启"), _T("Open")));
	m_cboRfStatus.SetCurSel(0);

	((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->ResetContent();
	//0-Unknown 1-Mobile 2-Unicom 3-Telecom
	((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->AddString(GetTextByLan(_T("未知"),_T("UnKnown"))), 0);
	((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->AddString(GetTextByLan(_T("移动"),_T("mobile"))), 1);
	((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->AddString(GetTextByLan(_T("联通"),_T("UniCom"))), 2);
	((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->AddString(GetTextByLan(_T("电信"),_T("Telecom"))), 3);
	((CComboBox*)(GetDlgItem(IDC_COMBO_MOBILE)))->SetCurSel(0);

	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->ResetContent();
	////1-EpsAtt 2-EPSImsiAtt 6-EpsEmerAtt 16-Tau 17-TaLau 18-TaLauImsi 19-PeriU 24-TaUBr 25-TaLaUBr 26-TaLaImsiBr 27-PeriUBr
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("EpsAtt"), 1);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("EPSImsiAtt"), 2);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("EpsEmerAtt"), 6);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("Tau"), 16);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("TaLau"), 17);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("TaLauImsi"), 18);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("PeriU"), 19);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("TaUBr"), 24);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("TaLaUBr"), 25);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("TaLaImsiBr"), 26);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->AddString("PeriUBr"), 27);
	((CComboBox*)(GetDlgItem(IDC_COMBO_INTYPE)))->SetCurSel(0);


	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->ResetContent();
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->AddString("FDD"), 1);
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->AddString("TDD"), 2);
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->SetCurSel(0);

	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->ResetContent();
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->AddString("FDD"), 1);
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->AddString("TDD"), 2);
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONTYPE)))->SetCurSel(0);


	//0-under list 1-black list 2-white list
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->ResetContent();
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->AddString(GetTextByLan(_T("名单外"),_T("under list"))), 0);
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->AddString(GetTextByLan(_T("黑名单"),_T("black list"))), 1);
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->AddString(GetTextByLan(_T("白名单"),_T("white list"))), 2);
	((CComboBox*)(GetDlgItem(IDC_COMBO_STATIONQUERY)))->SetCurSel(0);

	//1-upload ontime 2-upload storage
	((CComboBox*)(GetDlgItem(IDC_COMBO_QUERYINTIME)))->ResetContent();
	((CComboBox*)(GetDlgItem(IDC_COMBO_QUERYINTIME)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_QUERYINTIME)))->AddString(GetTextByLan(_T("实时上传"),_T("upload ontime"))), 1);
	((CComboBox*)(GetDlgItem(IDC_COMBO_QUERYINTIME)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_QUERYINTIME)))->AddString(GetTextByLan(_T("存储上传"),_T("upload storage"))), 2);
	((CComboBox*)(GetDlgItem(IDC_COMBO_QUERYINTIME)))->SetCurSel(0);

	((CComboBox*)(GetDlgItem(IDC_COMBO_WARNING)))->ResetContent();
	((CComboBox*)(GetDlgItem(IDC_COMBO_WARNING)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_WARNING)))->AddString(GetTextByLan(_T("归属地查询"),_T("Place of attribution warning"))), 1);
	((CComboBox*)(GetDlgItem(IDC_COMBO_WARNING)))->SetItemData(((CComboBox*)(GetDlgItem(IDC_COMBO_WARNING)))->AddString(GetTextByLan(_T("非归属地查询"),_T("Not place of attribution warning"))), 2);
	((CComboBox*)(GetDlgItem(IDC_COMBO_WARNING)))->SetCurSel(0);
	
}

void CLS_DlgFDDFunction::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iChannelNo = _iChannelNo;
	m_iLogonID = _iLogonID;
	UpdateParam();
	return;
}

void CLS_DlgFDDFunction::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (-1 == m_iLogonID || -1 == m_iChannelNo)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	if (PARA_SMALLCELL_IMSI == _iParaType)
	{
		UpdateIMSIInfo();
	}
	return;
}

void CLS_DlgFDDFunction::UpdateParam()
{
	int iRet=RET_FAILED;
	int iEnableValue = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_OPENFDDRF, INVALID_FLAG, &iEnableValue);

	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgFDDFunction::NetClient_GetCommonEnable] Get Failed ! LogonID %d EnableType %d", m_iLogonID, CI_COMMON_ID_OPENFDDRF);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_DlgFDDFunction::NetClient_GetCommonEnable] Get Success ! LogonID %d EnableType %d", m_iLogonID, CI_COMMON_ID_OPENFDDRF);
		m_cboFddRfEnable.SetCurSel(iEnableValue);
	}

	iEnableValue = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_OPENTDDRF, INVALID_FLAG, &iEnableValue);

	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgFDDFunction::NetClient_GetCommonEnable] Get Failed ! LogonID %d EnableType %d", m_iLogonID, CI_COMMON_ID_OPENTDDRF);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_DlgFDDFunction::NetClient_GetCommonEnable] Get Success ! LogonID %d EnableType %d", m_iLogonID, CI_COMMON_ID_OPENTDDRF);
		m_cboTddRf.SetCurSel(iEnableValue);
	}

	SmallCellInfo tInfo = {0};
	iEnableValue = -1;
	tInfo.iSize = (int)sizeof(tInfo);
	tInfo.iTerminalType = m_cboType.GetCurSel();
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SMALL_CELL, m_iChannelNo, &tInfo, tInfo.iSize, &iEnableValue);
	if (iRet == RET_SUCCESS)
	{
		m_chkHeartBeat.SetCheck(tInfo.iIsDspHearbeatRecv);
		m_cboRfStatus.SetCurSel(tInfo.iRfEnable);
		AddLog(LOG_TYPE_SUCC, "", "[CLS_DlgFDDFunction::NetClient_GetDevConfig NET_CLIENT_SMALL_CELL] Get Success ! LogonID %d ChannelID %d", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgFDDFunction::NetClient_GetDevConfig NET_CLIENT_SMALL_CELL]  Get Failed ! LogonID %d ChannelID %d ret = %d", m_iLogonID, m_iChannelNo, iRet);
	}

}

void CLS_DlgFDDFunction::UpdateIMSIInfo()
{
	SmallCellImsi_Notify tInfo = {0};
	tInfo.iSize = (int)sizeof(SmallCellImsi_Notify);
	int iRet = RET_FAILED;
	int iByteReturn = -1;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SMALL_IMSI, m_iChannelNo, &tInfo, tInfo.iSize, &iByteReturn);
	if(RET_SUCCESS == iRet)
	{
		CString strIndex = "";
		strIndex.Format("%d", tInfo.iDeviceId);
		SetDlgItemText(IDC_EDIT_DEVICEID, strIndex);
		strIndex.Format("%d", tInfo.iMsgId);
		SetDlgItemText(IDC_EDIT_MSGID, strIndex);
		strIndex.Format("%d", tInfo.iTime);
		SetDlgItemText(IDC_EDIT_TIME, strIndex);

		SetDlgItemText(IDC_EDIT_IMSINO, tInfo.pcImsiNo);
		strIndex.Format("%d", tInfo.iOwnerProvince);
		SetDlgItemText(IDC_EDIT_PROVICE, strIndex);

		strIndex.Format("%d", tInfo.iOwnerCity);
		SetDlgItemText(IDC_EDIT_CITY, strIndex);

		SetDlgItemText(IDC_EDIT_IMEI, tInfo.pcImeiNo);
		SetDlgItemText(IDC_EDIT_STMI, tInfo.pcStmsiNo);
		UpdateComboxInfo((CComboBox *)GetDlgItem(IDC_COMBO_MOBILE), tInfo.iOwnerOperator);
		UpdateComboxInfo((CComboBox *)GetDlgItem(IDC_COMBO_INTYPE), tInfo.iAccessType);
		UpdateComboxInfo((CComboBox *)GetDlgItem(IDC_COMBO_STATIONTYPE), tInfo.iBaseStationType);
		UpdateComboxInfo((CComboBox *)GetDlgItem(IDC_COMBO_STATIONQUERY), tInfo.iLibCheck);
		UpdateComboxInfo((CComboBox *)GetDlgItem(IDC_COMBO_QUERYINTIME), tInfo.iIsRealTime);
		UpdateComboxInfo((CComboBox *)GetDlgItem(IDC_COMBO_WARNING), tInfo.iIsOwnerWarn);

	}
}

void CLS_DlgFDDFunction::UpdateComboxInfo(CComboBox* pComBox, int iData)
{
	bool _bChoose = false;
	for (int i = 0; i < pComBox->GetCount(); i++)
	{
		if (pComBox->GetItemData(i) == iData)
		{
			pComBox->SetCurSel(i);
			_bChoose = true;
			break;
		}
	}
	if (!_bChoose)
	{
		pComBox->SetCurSel(0);
	}
}

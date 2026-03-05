// CLS_DlgIrriDataUploadParam.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgIrriDataUploadParam.h"


// CLS_DlgIrriDataUploadParam dialog

IMPLEMENT_DYNAMIC(CLS_DlgIrriDataUploadParam, CDialog)

CLS_DlgIrriDataUploadParam::CLS_DlgIrriDataUploadParam(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgIrriDataUploadParam::IDD, pParent)
	
	, m_iTotalNum(0)
	, m_iInterval(0)
	, m_iBrightness(0)
	, m_iWiperInterval(0)
{

}

CLS_DlgIrriDataUploadParam::~CLS_DlgIrriDataUploadParam()
{
}

void CLS_DlgIrriDataUploadParam::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_COMBO_SCENEID, m_cboSenceID);
	DDX_Control(pDX, IDC_COMBO_RULEID, m_cboRuleID);
	DDX_Text(pDX, IDC_EDIT_TOLTALNUM, m_iTotalNum);
	DDX_Control(pDX, IDC_COMBO_CURRENT, m_cboCurrent);
	DDX_Control(pDX, IDC_COMBO_IRRIDATATYPE, m_cboDataType);
	DDX_Control(pDX, IDC_COMBO_IRRIDATAENABLE, m_cboEnable);
	DDX_Text(pDX, IDC_EDIT_IRRIDATAINTERVAL, m_iInterval);
	DDX_Control(pDX, IDC_COMBO_IRRIDATAENABLE2, m_cboCtrlType);
	DDX_Text(pDX, IDC_EDIT_IRRIDATAINTERVAL2, m_iBrightness);
	DDX_Control(pDX, IDC_COMBO_IRRIDATAENABLE3, m_cboTimeTurn);
	DDX_Text(pDX, IDC_EDIT_IRRIDATAINTERVAL3, m_iWiperInterval);
	DDX_Control(pDX, IDC_COMBO_DEVTYPE2, m_cboSpecLightDevType);
	DDX_Control(pDX, IDC_COMBO_SCENEID2, m_cboSpecLightSceneID);
	DDX_Control(pDX, IDC_COMBO_RULEID3, m_cboSpecLightRuleID);
	DDX_Control(pDX, IDC_COMBO_DEVTYPE3, m_cboWiperDevType);
	DDX_Control(pDX, IDC_COMBO_SCENEID3, m_cboWiperSceneID);
	DDX_Control(pDX, IDC_COMBO_RULEID4, m_cboWiperRuleID);
}


BEGIN_MESSAGE_MAP(CLS_DlgIrriDataUploadParam, CDialog)

	ON_EN_CHANGE(IDC_EDIT_TOLTALNUM, &CLS_DlgIrriDataUploadParam::OnEnChangeEditToltalnum)
	ON_BN_CLICKED(IDC_BUTTON_IRRIUPLOAD_SET, &CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadSet)
	ON_CBN_SELCHANGE(IDC_COMBO_CURRENT, &CLS_DlgIrriDataUploadParam::OnCbnSelchangeComboCurrent)
	ON_CBN_SELCHANGE(IDC_COMBO_IRRIDATATYPE, &CLS_DlgIrriDataUploadParam::OnCbnSelchangeComboIrridatatype)
	ON_CBN_SELCHANGE(IDC_COMBO_IRRIDATAENABLE, &CLS_DlgIrriDataUploadParam::OnCbnSelchangeComboIrridataenable)
	ON_EN_CHANGE(IDC_EDIT_IRRIDATAINTERVAL, &CLS_DlgIrriDataUploadParam::OnEnChangeEditIrridatainterval)
	ON_BN_CLICKED(IDC_BUTTON_IRRIUPLOAD_SET2, &CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadSet2)
	ON_BN_CLICKED(IDC_BUTTON_IRRIUPLOAD_UPDATE2, &CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadUpdate2)
	ON_BN_CLICKED(IDC_BUTTON_IRRIUPLOAD_SET3, &CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadSet3)
	ON_BN_CLICKED(IDC_BUTTON_IRRIUPLOAD_UPDATE3, &CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadUpdate3)
	ON_BN_CLICKED(IDC_BUTTON_IRRIUPLOAD_UPDATE, &CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadUpdate)
END_MESSAGE_MAP()


// CLS_DlgIrriDataUploadParam message handlers

BOOL CLS_DlgIrriDataUploadParam::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialogText();

	m_cboDevType.ResetContent();
	m_cboDevType.AddString(_T("0-IPC"));
	m_cboDevType.AddString(_T("1-NVR"));

	m_cboSpecLightDevType.ResetContent();
	m_cboSpecLightDevType.AddString(_T("0-IPC"));
	m_cboSpecLightDevType.AddString(_T("1-NVR"));

	m_cboWiperDevType.ResetContent();
	m_cboWiperDevType.AddString(_T("0-IPC"));
	m_cboWiperDevType.AddString(_T("1-NVR"));

	for (int i = 0 ; i < MAX_SCENE_NUM; i++)
	{
		CString str;
		str.Format("%d",i);
		m_cboSenceID.AddString(str);
		m_cboSpecLightSceneID.AddString(str);
		m_cboWiperSceneID.AddString(str);
	}

	for (int i = 0 ; i < MAX_RULE_NUM_EX; i++)
	{
		CString str;
		str.Format("%d",i);
		m_cboRuleID.AddString(str);
		m_cboSpecLightRuleID.AddString(str);
		m_cboWiperRuleID.AddString(str);
	}

	for (int i = 0 ; i < MAX_IRRIGATION_TYPE; i++)
	{
		CString str;
		str.Format("%d",i);
		m_cboDataType.AddString(str);
	}

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}



void CLS_DlgIrriDataUploadParam::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
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
	Update_IrriDataUploadParam();
	Update_WaterQualityWiper();
	Update_SpecLightParam();
}

void CLS_DlgIrriDataUploadParam::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialogText();
}

void CLS_DlgIrriDataUploadParam::Update_IrriDataUploadParam()
{
	// TODO: Add your control notification handler code here
	memset(&m_tParam ,0x00 ,sizeof(IrriDataUploadParam));
	m_tParam.iIrriDataParamSize = sizeof(IrriDataParam);
	int iBytesReturned = 0;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_IRRIDATA_UPLOADPARAM, m_iChannelNO, &m_tParam, sizeof(IrriDataUploadParam));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_VCAGetConfig VCA_CMD_IRRIDATA_UPLOADPARAM failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_VCAGetConfig[VCA_CMD_IRRIDATA_UPLOADPARAM] (%d, %d)", m_iLogonID, m_iChannelNo);
		m_cboDevType.SetCurSel(m_tParam.iDevType);
		m_cboRuleID.SetCurSel(m_tParam.iRuleID);
		m_cboSenceID.SetCurSel(m_tParam.iSceneID);
		m_iTotalNum = m_tParam.iTotalNum;
		UpdateData(FALSE);
		OnEnChangeEditToltalnum();
	}

	

}


void CLS_DlgIrriDataUploadParam::Update_SpecLightParam()
{
	// TODO: Add your control notification handler code here

	SpecLightParam tSpecLightParam = {0};
	int iBytesReturned = 0;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SPECLIGHTPARAM, m_iChannelNO, &tSpecLightParam, sizeof(SpecLightParam));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_VCAGetConfig VCA_CMD_SPECLIGHTPARAM failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_VCAGetConfig[VCA_CMD_SPECLIGHTPARAM] (%d, %d)", m_iLogonID, m_iChannelNo);
		m_cboCtrlType.SetCurSel(tSpecLightParam.iCtrlType);
		m_iBrightness = tSpecLightParam.iBrightness;
		m_cboSpecLightDevType.SetCurSel(tSpecLightParam.iDevType);
		m_cboSpecLightRuleID.SetCurSel(tSpecLightParam.iRuleID);
		m_cboSpecLightSceneID.SetCurSel(tSpecLightParam.iSceneID);
	}

	UpdateData(FALSE);

}

void CLS_DlgIrriDataUploadParam::Update_WaterQualityWiper()
{
	// TODO: Add your control notification handler code here

	WaterQualityWiper tWaterQualityWiper = {0};
	int iBytesReturned = 0;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_WATERQUALITY_WIPER, m_iChannelNO, &tWaterQualityWiper, sizeof(WaterQualityWiper));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_VCAGetConfig VCA_CMD_WATERQUALITY_WIPER failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_VCAGetConfig[VCA_CMD_WATERQUALITY_WIPER] (%d, %d)", m_iLogonID, m_iChannelNo);
		m_cboTimeTurn.SetCurSel(tWaterQualityWiper.iTimeTurn);
		m_iWiperInterval = tWaterQualityWiper.iInterval;
		m_cboWiperDevType.SetCurSel(tWaterQualityWiper.iDevType);
		m_cboWiperRuleID.SetCurSel(tWaterQualityWiper.iRuleID);
		m_cboWiperSceneID.SetCurSel(tWaterQualityWiper.iSceneID);
	}

	UpdateData(FALSE);

}

void CLS_DlgIrriDataUploadParam::UpdateParam()
{
	int iIndex = m_cboCurrent.GetCurSel();

	if(iIndex >= 0 && iIndex < MAX_IRRIDATAPARAM_NUM)
	{
		IrriDataParam &tIrriDataParam = m_tParam.tIrriDataParam[iIndex];
		m_cboDataType.SetCurSel(tIrriDataParam.iIrriDataType);
		m_cboEnable.SetCurSel(tIrriDataParam.iIrriDataEnable);
		m_iInterval = tIrriDataParam.iIrriDataInterval;
	}
	UpdateData(FALSE);
}

void CLS_DlgIrriDataUploadParam::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_DEVTYPE,GetTextByLan(_T("设备类型"), _T("DevType")));
	SetDlgItemText(IDC_STATIC_SCENEID,GetTextByLan(_T("场景ID"), _T("SceneID")));
	SetDlgItemText(IDC_STATIC_RULEID,GetTextByLan(_T("规则ID"), _T("RuleID")));
	SetDlgItemText(IDC_STATIC_TOTALNUM,GetTextByLan(_T("总数"), _T("Toltal Number")));
	SetDlgItemText(IDC_STATIC_CURRENT,GetTextByLan(_T("当前序号"), _T("Current Index")));
	SetDlgItemText(IDC_STATIC_IRRITYPE,GetTextByLan(_T("水利数据类型"), _T("Irri Data Type")));
	SetDlgItemText(IDC_STATIC_IRRIDATAENABLE,GetTextByLan(_T("水利数据上报使能"), _T("Irri Data Upload Enable")));
	SetDlgItemText(IDC_STATIC_IRRIDATAINTERVAL,GetTextByLan(_T("水利数据上报间隔"), _T("Irri Data Interval")));
	
	SetDlgItemText(IDC_BUTTON_IRRIUPLOAD_SET,GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_IRRIUPLOAD_UPDATE,GetTextByLan(_T("更新"), _T("Update")));

	SetDlgItemText(IDC_STATIC_DEVTYPE2,GetTextByLan(_T("设备类型"), _T("DevType")));
	SetDlgItemText(IDC_STATIC_SCENEID2,GetTextByLan(_T("场景ID"), _T("SceneID")));
	SetDlgItemText(IDC_STATIC_RULEID2,GetTextByLan(_T("规则ID"), _T("RuleID")));
	SetDlgItemText(IDC_STATIC_CTRLTYPE,GetTextByLan(_T("控制方式"), _T("Ctrl Type")));
	SetDlgItemText(IDC_STATIC_BRIGHTNESS,GetTextByLan(_T("光谱灯源亮度"), _T("Brightness")));

	SetDlgItemText(IDC_BUTTON_IRRIUPLOAD_SET2,GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_IRRIUPLOAD_UPDATE2,GetTextByLan(_T("更新"), _T("Update")));

	SetDlgItemText(IDC_STATIC_DEVTYPE3,GetTextByLan(_T("设备类型"), _T("DevType")));
	SetDlgItemText(IDC_STATIC_SCENEID3,GetTextByLan(_T("场景ID"), _T("SceneID")));
	SetDlgItemText(IDC_STATIC_RULEID3,GetTextByLan(_T("规则ID"), _T("RuleID")));
	SetDlgItemText(IDC_STATIC_WIPER,GetTextByLan(_T("定时清洁"), _T("Time Turn")));
	SetDlgItemText(IDC_STATIC_WIPER_INTERVAL,GetTextByLan(_T("清洁间隔"), _T("Wiper Interval")));

	SetDlgItemText(IDC_BUTTON_IRRIUPLOAD_SET3,GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_IRRIUPLOAD_UPDATE3,GetTextByLan(_T("更新"), _T("Update")));

	m_cboEnable.ResetContent();
	m_cboEnable.AddString(GetTextByLan(_T("0-不使能"), _T("0-UnEnable")));
	m_cboEnable.AddString(GetTextByLan(_T("1-使能"), _T("1-Enable")));

	m_cboCtrlType.ResetContent();
	m_cboCtrlType.AddString(GetTextByLan(_T("0-自动"), _T("0-Auto")));
	m_cboCtrlType.AddString(GetTextByLan(_T("1-手动"), _T("1-Manual")));

	m_cboTimeTurn.ResetContent();
	m_cboTimeTurn.AddString(GetTextByLan(_T("0-关闭"), _T("0-Close")));
	m_cboTimeTurn.AddString(GetTextByLan(_T("1-开启"), _T("1-Open")));

}

void CLS_DlgIrriDataUploadParam::OnEnChangeEditToltalnum()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CLS_BasePage::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
	UpdateData(TRUE);
	m_tParam.iTotalNum = m_iTotalNum;
	m_cboCurrent.ResetContent();
	for (int i = 0; i < m_tParam.iTotalNum; i++)
	{
		CString str;
		str.Format("%d",i);
		m_cboCurrent.AddString(str);
	}
	m_cboCurrent.SetCurSel(0);
	UpdateParam();
}


void CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadSet()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	m_tParam.iTotalNum = m_iTotalNum;
	m_tParam.iPageSize = 20;
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_IRRIDATA_UPLOADPARAM, m_iChannelNO, &m_tParam, sizeof(IrriDataUploadParam));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_DlgIrriDataUploadParam::NetClient_VCASetConfig[VCA_CMD_IRRIDATA_UPLOADPARAM] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgIrriDataUploadParam::NetClient_VCASetConfig[VCA_CMD_IRRIDATA_UPLOADPARAM] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_DlgIrriDataUploadParam::OnCbnSelchangeComboCurrent()
{
	// TODO: Add your control notification handler code here

	UpdateParam();
}

void CLS_DlgIrriDataUploadParam::OnCbnSelchangeComboIrridatatype()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	int iIndex = m_cboCurrent.GetCurSel();

	if(iIndex >= 0 && iIndex < MAX_IRRIDATAPARAM_NUM)
	{
		IrriDataParam &tIrriDataParam = m_tParam.tIrriDataParam[iIndex];
		tIrriDataParam.iIrriDataType = m_cboDataType.GetCurSel();
	}
}

void CLS_DlgIrriDataUploadParam::OnCbnSelchangeComboIrridataenable()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	int iIndex = m_cboCurrent.GetCurSel();

	if(iIndex >= 0 && iIndex < MAX_IRRIDATAPARAM_NUM)
	{
		IrriDataParam &tIrriDataParam = m_tParam.tIrriDataParam[iIndex];
		tIrriDataParam.iIrriDataEnable = m_cboEnable.GetCurSel();
	}
}

void CLS_DlgIrriDataUploadParam::OnEnChangeEditIrridatainterval()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CLS_BasePage::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
	UpdateData(TRUE);
	int iIndex = m_cboCurrent.GetCurSel();

	if(iIndex >= 0 && iIndex < MAX_IRRIDATAPARAM_NUM)
	{
		IrriDataParam &tIrriDataParam = m_tParam.tIrriDataParam[iIndex];
		tIrriDataParam.iIrriDataInterval = m_iInterval;
	}
}

void CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadSet2()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	SpecLightParam tSpecLightParam = {0};
	tSpecLightParam.iDevType = m_cboSpecLightDevType.GetCurSel();
	tSpecLightParam.iSceneID = m_cboSpecLightSceneID.GetCurSel();
	tSpecLightParam.iRuleID = m_cboSpecLightRuleID.GetCurSel();
	tSpecLightParam.iCtrlType = m_cboCtrlType.GetCurSel();
	tSpecLightParam.iBrightness = m_iBrightness;
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SPECLIGHTPARAM, m_iChannelNO, &tSpecLightParam, sizeof(tSpecLightParam));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_DlgIrriDataUploadParam::NetClient_VCASetConfig[VCA_CMD_SPECLIGHTPARAM] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgIrriDataUploadParam::NetClient_VCASetConfig[VCA_CMD_SPECLIGHTPARAM] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadUpdate2()
{
	// TODO: Add your control notification handler code here
	Update_SpecLightParam();
}

void CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadSet3()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	WaterQualityWiper tWaterQualityWiper = {0};
	tWaterQualityWiper.iDevType = m_cboWiperDevType.GetCurSel();
	tWaterQualityWiper.iSceneID = m_cboWiperSceneID.GetCurSel();
	tWaterQualityWiper.iRuleID = m_cboWiperRuleID.GetCurSel();
	tWaterQualityWiper.iTimeTurn = m_cboTimeTurn.GetCurSel();
	tWaterQualityWiper.iInterval = m_iWiperInterval;
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_WATERQUALITY_WIPER, m_iChannelNO, &tWaterQualityWiper, sizeof(tWaterQualityWiper));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_DlgIrriDataUploadParam::NetClient_VCASetConfig[VCA_CMD_WATERQUALITY_WIPER] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgIrriDataUploadParam::NetClient_VCASetConfig[VCA_CMD_WATERQUALITY_WIPER] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadUpdate3()
{
	// TODO: Add your control notification handler code here
	Update_WaterQualityWiper();
}

void CLS_DlgIrriDataUploadParam::OnBnClickedButtonIrriuploadUpdate()
{
	// TODO: Add your control notification handler code here
	Update_IrriDataUploadParam();
}

// E:\SDK\trunk\Demo\NetClientDemo\Config\Events\VCAEVENT_TemDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_TemDetect.h"


// VCAEVENT_TemDetect dialog

IMPLEMENT_DYNAMIC(VCAEVENT_TemDetect, CDialog)

VCAEVENT_TemDetect::VCAEVENT_TemDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(VCAEVENT_TemDetect::IDD, pParent)
{

}

VCAEVENT_TemDetect::~VCAEVENT_TemDetect()
{
}

void VCAEVENT_TemDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_TEMODETECT_VALID, m_ChkValid);
	DDX_Control(pDX, IDC_CHECK_DISPLAY_TEMSCALE_ENABLE, m_ChkDisplayTemscaleEnable);
	DDX_Control(pDX, IDC_COMBO_HIGHTEMCOLOR, m_CboHighTemColor);
	DDX_Control(pDX, IDC_COMBO_LOWTEM_COLOR, m_CboLowTemColor);
	DDX_Control(pDX, IDC_COMBO_MODEL_TYPE, m_CboModelType);
	DDX_Control(pDX, IDC_COMBO_TEM_UNIT, m_CboTemUnit);
	DDX_Control(pDX, IDC_EDIT_TEMTHRESHOLD, m_EdtTemThreshold);
	DDX_Control(pDX, IDC_EDIT_WAIT_TIME, m_EdtWaitTime);
	DDX_Control(pDX, IDC_COMBO_ABNORMAL_ALARM, M_CboAbnormalAlarm);
}


BEGIN_MESSAGE_MAP(VCAEVENT_TemDetect, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_TEMDETECT_SET, &VCAEVENT_TemDetect::OnBnClickedButtonTemdetectSet)
	ON_CBN_SELCHANGE(IDC_COMBO_MODEL_TYPE, &VCAEVENT_TemDetect::OnCbnSelchangeComboModelType)
END_MESSAGE_MAP()


// VCAEVENT_TemDetect message handler
BOOL VCAEVENT_TemDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  add extra initialization here
	UpdateUIText();
	//UpdatePageUI();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void VCAEVENT_TemDetect::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_TEMODETECT_VALID, GetTextByLan("算法是否有效", "Valid"));
	SetDlgItemText(IDC_CHECK_DISPLAY_TEMSCALE_ENABLE, GetTextByLan("显示温标使能", "DisplayTemScaleEnable"));
	SetDlgItemText(IDC_STATIC_HIGHTEMCOLOR, GetTextByLan("高温温标颜色", "HighTemColor"));
	SetDlgItemText(IDC_STATIC_LOWTEM_COLOR, GetTextByLan("低温温标颜色", "LowTemColor"));
	SetDlgItemText(IDC_STATIC_MODEL_TYPE, GetTextByLan("检测模式", "ModelType"));
	SetDlgItemText(IDC_STATIC_TEM_UNIT, GetTextByLan("温度单位", "TemUnit"));
	SetDlgItemText(IDC_STATIC_TEMTHRESHOLD, GetTextByLan("温度阈值", "TemThreshold"));
	SetDlgItemText(IDC_STATIC_WAIT_TIME, GetTextByLan("发现警情等待时间", "WaitTime"));
	SetDlgItemText(IDC_BUTTON_TEMDETECT_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_STATIC_ABNORMAL_ALARM, GetTextByLan("温度异常报警使能", "AbnormalAlarm"));

	const CString strColor[] = {GetTextByLan(_T("红色"), _T("Red")), GetTextByLan(_T("绿色"), _T("Green")), GetTextByLan(_T("黄色"), _T("Yellow")), 
		GetTextByLan(_T("蓝色"), _T("Blue")), GetTextByLan(_T("紫色"), _T("Purple")), GetTextByLan(_T("青色"), _T("Cyan")), 
		GetTextByLan(_T("黑色"), _T("Black")), GetTextByLan(_T("白色"), _T("White"))};

	m_CboHighTemColor.ResetContent();
	m_CboLowTemColor.ResetContent();
	for (int i = 0; i < sizeof(strColor)/sizeof(CString); i++)
	{
		m_CboHighTemColor.InsertString(i, strColor[i]);
		m_CboLowTemColor.InsertString(i, strColor[i]);
	}
	m_CboHighTemColor.SetCurSel(0);
	m_CboLowTemColor.SetCurSel(0);

	m_CboModelType.ResetContent();
	m_CboModelType.SetItemData(0, m_CboModelType.AddString(GetTextByLan(_T("1-环境温差报警"), _T("AmbientTemDiffAlarm"))));
	m_CboModelType.SetItemData(1, m_CboModelType.AddString(GetTextByLan(_T("2-环境高温报警"), _T("AmbientTemHighAlarm"))));
	m_CboModelType.SetItemData(2, m_CboModelType.AddString(GetTextByLan(_T("3-人体高温报警"), _T("HumanTemHighAlarm"))));
	m_CboModelType.SetCurSel(0);
	//m_CboTargetType.SetItemData(3, m_CboTargetType.AddString(GetTextByLan(_T("区分人车"), _T("Distinguish People&Car"))));

	m_CboTemUnit.ResetContent();
	m_CboTemUnit.SetItemData(0, m_CboTemUnit.AddString(GetTextByLan(_T("0-摄氏"), _T("Celsius"))));
	m_CboTemUnit.SetItemData(1, m_CboTemUnit.AddString(GetTextByLan(_T("1-华氏"), _T("Fahrenheit"))));
	m_CboTemUnit.SetCurSel(0);

	M_CboAbnormalAlarm.ResetContent();
	M_CboAbnormalAlarm.SetItemData(0, M_CboAbnormalAlarm.AddString(GetTextByLan(_T("不启用"), _T("DisAble"))));
	M_CboAbnormalAlarm.SetItemData(1, M_CboAbnormalAlarm.AddString(GetTextByLan(_T("启用"), _T("Enable"))));
	M_CboAbnormalAlarm.SetCurSel(0);
}

void VCAEVENT_TemDetect::UpdatePageUI()
{
	if (m_iLogonID == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "VCAEVENT_TemDetect::Invalid Logon id.(%d)", m_iLogonID);
		return;
	}
	if (m_iChannelNO < 0)
	{
		m_iChannelNO = 0;
	}
	VCATemDetect vc = {0};
	vc.iSize = sizeof(vc);
	vc.iSceneID = m_iSceneID;
	vc.iRuleID = m_iRuleID;
	vc.iModelType = m_CboModelType.GetCurSel()+ 1;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_TEMDETECT, m_iChannelNO, &vc, sizeof(VCATemDetect));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","VCAEVENT_TemDetect::NetClient_VCAGetConfig[VCA_CMD_TEMDETECT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_ChkValid.SetCheck(vc.iValid);
		m_ChkDisplayTemscaleEnable.SetCheck(vc.iDisplayTemScaleEnable);

		m_CboHighTemColor.SetCurSel(vc.iHighTemColor-1<0?0:vc.iHighTemColor-1);
		m_CboLowTemColor.SetCurSel(vc.iLowTemColor-1<0?0:vc.iLowTemColor-1);
		m_CboModelType.SetCurSel(vc.iModelType-1<0?0:vc.iModelType-1);
		m_CboTemUnit.SetCurSel(vc.iTemUnit);
		CString cstrTemThreshold;
		cstrTemThreshold.Format("%.1f", (float)vc.iTemThreshold / 100.0);
		SetDlgItemText(IDC_EDIT_TEMTHRESHOLD, cstrTemThreshold);
		SetDlgItemInt(IDC_EDIT_WAIT_TIME, vc.iWaitTime);
		M_CboAbnormalAlarm.SetCurSel(vc.iTempLoseEnable);
		AddLog(LOG_TYPE_SUCC,"","VCAEVENT_TemDetect::NetClient_VCAGetConfig[VCA_CMD_TEMDETECT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}

}

void VCAEVENT_TemDetect::OnLanguageChanged()
{	
	UpdateUIText();
	UpdatePageUI();
}

void VCAEVENT_TemDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	UpdatePageUI();
}

void VCAEVENT_TemDetect::OnBnClickedButtonTemdetectSet()
{
	if (m_iLogonID == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PermterNew::Invalid Logon id(%d)", m_iLogonID);
		return;
	}
	if (m_iChannelNO < 0)
	{
		m_iChannelNO = 0;
	}
	VCATemDetect vc = {0};
	vc.iSize = sizeof(vc);
	vc.iRuleID = m_iRuleID;
	vc.iSceneID = m_iSceneID;
	vc.iValid = m_ChkValid.GetCheck();
	vc.iDisplayTemScaleEnable = m_ChkDisplayTemscaleEnable.GetCheck();
	vc.iHighTemColor = m_CboHighTemColor.GetCurSel()+1;
	vc.iLowTemColor = m_CboLowTemColor.GetCurSel()+1;
	vc.iModelType = m_CboModelType.GetCurSel()+1;
	vc.iTemUnit = m_CboTemUnit.GetCurSel(); 
	CString cstrTemThreshold;
	m_EdtTemThreshold.GetWindowText(cstrTemThreshold);

	double fData = atof((LPSTR)(LPCTSTR)cstrTemThreshold);
	vc.iTemThreshold = (int)((fData + 0.001) * 100);
	vc.iWaitTime = GetDlgItemInt(IDC_EDIT_WAIT_TIME);
	vc.iTempLoseEnable = M_CboAbnormalAlarm.GetCurSel();

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_TEMDETECT, m_iChannelNO, &vc, sizeof(VCATemDetect));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","VCAEVENT_TemDetect::NetClient_VCASetConfig[VCA_CMD_TEMDETECT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","VCAEVENT_TemDetect::NetClient_VCASetConfig[VCA_CMD_TEMDETECT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void VCAEVENT_TemDetect::OnCbnSelchangeComboModelType()
{
	UpdatePageUI();
}

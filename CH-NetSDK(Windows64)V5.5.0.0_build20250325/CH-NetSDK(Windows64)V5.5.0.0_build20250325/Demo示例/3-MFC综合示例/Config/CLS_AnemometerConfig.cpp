// CLS_AnemometerConfig.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_AnemometerConfig.h"


// CLS_AnemometerConfig dialog

IMPLEMENT_DYNAMIC(CLS_AnemometerConfig, CDialog)

CLS_AnemometerConfig::CLS_AnemometerConfig(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_AnemometerConfig::IDD, pParent)
	, m_iStartHour(0)
	, m_iStartMin(0)
	, m_iStopHour(0)
	, m_iStopMin(0)
	, m_iDetectTimeOut(0)
	, m_iLowThreshold(0)
{

}

CLS_AnemometerConfig::~CLS_AnemometerConfig()
{
}

BOOL CLS_AnemometerConfig::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UI_UpdateUIText();
	for(int i = 0; i < MAX_SCENE_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboSceneID.AddString(str);
	}
	UpdateData(FALSE);
	m_cboSceneID.SetCurSel(0);
	OnBnClickedButtonGet();
	return TRUE;
}

void CLS_AnemometerConfig::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	OnBnClickedButtonGet();
}

void CLS_AnemometerConfig::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_SCENE_ID, GetTextByLan(_T("场景ID"), _T("Scene ID")));
	SetDlgItemText(IDC_STATIC_USE_MODE, GetTextByLan(_T("工作模式"), _T("Use Mode")));
	SetDlgItemText(IDC_STATIC_START_HOUR, GetTextByLan(_T("使用开始时间"), _T("Use Start Hour")));
	SetDlgItemText(IDC_STATIC_START_MIN, GetTextByLan(_T("使用开始分钟"), _T("Use Start Min")));
	SetDlgItemText(IDC_STATIC_STOP_HOUR, GetTextByLan(_T("使用结束时间"), _T("Use End Hour")));

	SetDlgItemText(IDC_STATIC_STOP_MIN, GetTextByLan(_T("使用结束分钟"), _T("Use End Min")));
	SetDlgItemText(IDC_STATIC_DETECT_TIME_OUT, GetTextByLan(_T("检测超过时间"), _T("Detect Time Out")));
	SetDlgItemText(IDC_STATIC_TABLE_OPT, GetTextByLan(_T("流速表来源选择"), _T("Flow Meter Source")));
	SetDlgItemText(IDC_STATIC_LOW_THRESHOLD, GetTextByLan(_T("低水位查表阈值"), _T("Low Water Level Look Threshold")));
	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_GET, GetTextByLan(_T("获取"), _T("Get")));

	m_cboUseMode.ResetContent();
	m_cboUseMode.AddString(GetTextByLan(_T("0-自动"), _T("0-automatic")));
	m_cboUseMode.AddString(GetTextByLan(_T("1-手动开启"), _T("1-manual opening")));
	m_cboUseMode.AddString(GetTextByLan(_T("2-手动关闭"), _T("2-manual closing")));
	m_cboUseMode.AddString(GetTextByLan(_T("3-定时"), _T("3-timing")));
	m_cboUseMode.AddString(GetTextByLan(_T("4-低水位查表"), _T("4-low water level check table")));

	m_cboTableOpt.ResetContent();
	m_cboTableOpt.AddString(GetTextByLan(_T("0-使用自动流速表"), _T("0-use automatic flow meter")));
	m_cboTableOpt.AddString(GetTextByLan(_T("1-使用手动流速表"), _T("1-use manual flow meter")));
}

void CLS_AnemometerConfig::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SCENE_ID, m_cboSceneID);
	DDX_Control(pDX, IDC_COMBO_USE_MODE, m_cboUseMode);
	DDX_Text(pDX, IDC_EDIT_START_HOUR, m_iStartHour);
	DDV_MinMaxInt(pDX, m_iStartHour, 0, 23);
	DDX_Text(pDX, IDC_EDIT_START_MIN, m_iStartMin);
	DDV_MinMaxInt(pDX, m_iStartMin, 0, 59);
	DDX_Text(pDX, IDC_EDIT_STOPHOUR, m_iStopHour);
	DDV_MinMaxInt(pDX, m_iStopHour, 0, 23);
	DDX_Text(pDX, IDC_EDIT_STOPMIN, m_iStopMin);
	DDV_MinMaxInt(pDX, m_iStopMin, 0, 59);
	DDX_Text(pDX, IDC_EDIT_DETECT_TIME_OUT, m_iDetectTimeOut);
	DDV_MinMaxInt(pDX, m_iDetectTimeOut, 1, 7200);
	DDX_Control(pDX, IDC_COMBO_TABLE_OPT, m_cboTableOpt);
	DDX_Text(pDX, IDC_EDIT_LOW_THRESHOLD, m_iLowThreshold);
	DDV_MinMaxInt(pDX, m_iLowThreshold, -10000000, 10000000);
}


BEGIN_MESSAGE_MAP(CLS_AnemometerConfig, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_AnemometerConfig::OnBnClickedButtonSet)
	ON_BN_CLICKED(IDC_BUTTON_GET, &CLS_AnemometerConfig::OnBnClickedButtonGet)
END_MESSAGE_MAP()


// CLS_AnemometerConfig message handler

void CLS_AnemometerConfig::OnBnClickedButtonSet()
{
	// TODO: Add control notification handler code here
	//NetClient_VCASetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen)
	UpdateData(TRUE);
	WstAbleUseMode tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iUseMode = m_cboUseMode.GetCurSel();
	tInfo.iStartHour = m_iStartHour;
	tInfo.iStartMin = m_iStartMin;
	tInfo.iStopHour = m_iStopHour;
	tInfo.iStopMin = m_iStopMin;
	tInfo.iDetectTimeOut = m_iDetectTimeOut;
	tInfo.iTableOption = m_cboTableOpt.GetCurSel();
	tInfo.iLowThreshold = m_iLowThreshold;
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_WSTABLEUSEMODE, m_iChannelNO, &tInfo, sizeof(WstAbleUseMode));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_AnemometerConfig::NetClient_VCASetConfig[VCA_CMD_WSTABLEUSEMODE] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_AnemometerConfig::NetClient_VCASetConfig[VCA_CMD_WSTABLEUSEMODE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_AnemometerConfig::OnBnClickedButtonGet()
{
	// TODO: Add control notification handler code here
	//VCA_CMD_WSTABLEUSEMODE:
	//iRet = pCmdChan->VCAGetWsTableUseMode(_iChannel, _lpCmdBuf, _iCmdBufLen);
	//NetClient_VCAGetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen)
	WstAbleUseMode tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_WSTABLEUSEMODE, m_iChannelNO, &tInfo, sizeof(WstAbleUseMode));
	if(RET_SUCCESS == iRet)
	{
		m_cboSceneID.SetCurSel(tInfo.iSceneId);
		m_cboUseMode.SetCurSel(tInfo.iUseMode);
		m_iStartHour = tInfo.iStartHour;
		m_iStartMin = tInfo.iStartMin;
		m_iStopHour = tInfo.iStopHour;
		m_iStopMin = tInfo.iStopMin;
		m_iDetectTimeOut = tInfo.iDetectTimeOut;
		m_cboTableOpt.SetCurSel(tInfo.iTableOption);
		m_iLowThreshold = tInfo.iLowThreshold;
		AddLog(LOG_TYPE_SUCC, "","CLS_AnemometerConfig::NetClient_VCAGetConfig[VCA_CMD_WSTABLEUSEMODE] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
		UpdateData(FALSE);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_AnemometerConfig::NetClient_VCAGetConfig[VCA_CMD_WSTABLEUSEMODE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

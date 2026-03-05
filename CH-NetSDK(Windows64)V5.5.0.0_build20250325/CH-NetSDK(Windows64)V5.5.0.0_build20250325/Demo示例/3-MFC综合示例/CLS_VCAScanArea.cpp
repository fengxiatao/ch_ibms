// CLS_VCAScanArea.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_VCAScanArea.h"


// CLS_VCAScanArea dialog

IMPLEMENT_DYNAMIC(CLS_VCAScanArea, CDialog)

CLS_VCAScanArea::CLS_VCAScanArea(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCAScanArea::IDD, pParent)
{

}

CLS_VCAScanArea::~CLS_VCAScanArea()
{
}

void CLS_VCAScanArea::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SCENE_ID, m_cboSceneID);
	DDX_Control(pDX, IDC_COMBO_SCENE_ID2, m_cboOrderType);
	DDX_Control(pDX, IDC_COMBO_SCENE_ID3, m_cboCmdType);
	DDX_Control(pDX, IDC_COMBO_SCAN_TYPE, m_cboScanMode);
}


BEGIN_MESSAGE_MAP(CLS_VCAScanArea, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SCAN_AREA_SET, &CLS_VCAScanArea::OnBnClickedButtonScanAreaSet)
	ON_BN_CLICKED(IDC_BUTTON_SCAN_PARA_SET, &CLS_VCAScanArea::OnBnClickedButtonScanParaSet)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_COMBO_SCENE_ID, &CLS_VCAScanArea::OnCbnSelchangeComboSceneId)
END_MESSAGE_MAP()


// CLS_VCAScanArea message handler
void CLS_VCAScanArea::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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

void CLS_VCAScanArea::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAScanArea::UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_SCENE_ID, GetTextByLan("场景号", "SceneId"));
	SetDlgItemText(IDC_STATIC_ORDER_TYPE, GetTextByLan("操作区域类型", "OrderTyp"));
	SetDlgItemText(IDC_STATIC_CMD_TYPE, GetTextByLan("操作类型", "CmdType"));
	SetDlgItemText(IDC_BUTTON_SCAN_AREA_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_STATIC_SCAN_TYPE, GetTextByLan("扫描模式", "ScanMode"));
	SetDlgItemText(IDC_STATIC_SCAN_WAIT_TIME, GetTextByLan("停留时间", "WaitTime"));
	SetDlgItemText(IDC_STATIC_SCAN_PAN_STEP, GetTextByLan("水平步进", "PanStep"));
	SetDlgItemText(IDC_STATIC_SCAN_TITLE_STEP, GetTextByLan("垂直步进", "TiltStep"));
	SetDlgItemText(IDC_BUTTON_SCAN_PARA_SET, GetTextByLan("设置", "Set"));

	m_cboSceneID.ResetContent();
	for(int i = 0; i < 16; ++i)
	{
		CString cstrSceneId;
		cstrSceneId.Format("%d",i);
		m_cboSceneID.InsertString(i, cstrSceneId);
	}
	m_cboSceneID.SetCurSel(0);

	m_cboCmdType.ResetContent();
	m_cboCmdType.InsertString(0, "set");
	m_cboCmdType.InsertString(1, "call");
	m_cboCmdType.SetCurSel(0);

	m_cboScanMode.ResetContent();
	m_cboScanMode.InsertString(0, "manual");
	m_cboScanMode.InsertString(1, "auto");
	m_cboScanMode.SetCurSel(0);

	m_cboOrderType.ResetContent();
	m_cboOrderType.InsertString(0, "top");
	m_cboOrderType.InsertString(1, "bottom");
	m_cboOrderType.InsertString(2, "left");
	m_cboOrderType.InsertString(3, "right");
	m_cboOrderType.SetCurSel(0);
	
}

BOOL CLS_VCAScanArea::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control

}

void CLS_VCAScanArea::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAScanArea::UpdatePageUI()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::UpdatePageUI (%d, %d)", m_iChannelNO);
		return;
	}

	VcaScanPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboSceneID.GetCurSel();

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_VCA_SCAN_PARA, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboScanMode.SetCurSel(tInfo.iMode);
		SetDlgItemInt(IDC_EDIT_SCAN_PARA_WAIT_TIME, tInfo.iWaitTime);
		SetDlgItemInt(IDC_EDIT_PAN_SETP, tInfo.iPanStep);
		SetDlgItemInt(IDC_EDIT_TITLE_STEP, tInfo.iTiltStep);
	}
}


void CLS_VCAScanArea::OnBnClickedButtonScanAreaSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnBnClickedButtonScanAreaSet::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VcaScanArea tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iOrderTyp = m_cboOrderType.GetCurSel();
	tInfo.iCmdType = m_cboCmdType.GetCurSel();

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_VCA_SCAN_AREA, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_VCA_SCAN_AREA] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_VCA_SCAN_AREA] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAScanArea::OnBnClickedButtonScanParaSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnBnClickedButtonScanParaSet::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VcaScanPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iMode = m_cboScanMode.GetCurSel();
	tInfo.iWaitTime = GetDlgItemInt(IDC_EDIT_SCAN_PARA_WAIT_TIME);
	tInfo.iPanStep = GetDlgItemInt(IDC_EDIT_PAN_SETP);
	tInfo.iTiltStep = GetDlgItemInt(IDC_EDIT_TITLE_STEP);

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_VCA_SCAN_PARA, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_VCA_SCAN_PARA] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_VCA_SCAN_PARA] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}



void CLS_VCAScanArea::OnCbnSelchangeComboSceneId()
{
	UpdatePageUI();
}

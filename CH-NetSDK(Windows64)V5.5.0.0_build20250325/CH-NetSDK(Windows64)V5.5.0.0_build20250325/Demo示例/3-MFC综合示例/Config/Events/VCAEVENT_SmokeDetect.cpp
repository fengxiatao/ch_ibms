// CLS_VcaSmokeDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_SmokeDetect.h"


// CLS_VcaSmokeDetect dialog

IMPLEMENT_DYNAMIC(CLS_VcaSmokeDetect, CDialog)

CLS_VcaSmokeDetect::CLS_VcaSmokeDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaSmokeDetect::IDD, pParent)
{

}

CLS_VcaSmokeDetect::~CLS_VcaSmokeDetect()
{
}

void CLS_VcaSmokeDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_SMOKEDETECT_REGION_POINTS, m_editRegionPoints);
	DDX_Control(pDX, IDC_CHECK_SMOKEDETECT, m_chkSmokeDetect);
	DDX_Control(pDX, IDC_COMBO_SMOKE_DEV_TYPE, m_cboDevType);
}


BEGIN_MESSAGE_MAP(CLS_VcaSmokeDetect, CDialog)
	ON_BN_CLICKED(IDC_BTN_SMOKEDETECT_REGION_DRAW, &CLS_VcaSmokeDetect::OnBnClickedBtnSmokedetectRegionDraw)
	ON_BN_CLICKED(IDC_BUTTON_SMOKEDETECT, &CLS_VcaSmokeDetect::OnBnClickedButtonSmokedetect)
	//ON_CBN_SELCHANGE(IDC_COMBO_SMOKE_DEV_TYPE, &CLS_VcaSmokeDetect::OnCbnSelchangeComboSmokeDevType)
	ON_BN_CLICKED(IDC_CHECK_SMOKEDETECT, &CLS_VcaSmokeDetect::OnBnClickedCheckSmokedetect)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// VcaSmokeDetect message handler

void CLS_VcaSmokeDetect::OnBnClickedButtonSmokedetect()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaPept::OnBnClickedButtonSmokedetect Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VCASmokeDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	tInfo.iValid = m_chkSmokeDetect.GetCheck();
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDIT_SMOKEDETECT_SENSITIVITY);
	tInfo.iPointNum =  GetDlgItemInt(IDC_EDIT_SMOKEDETECT_REGION_POINTNUM);
	if(tInfo.iPointNum < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","iPointNum = %d, inValid.", tInfo.iPointNum);
		return;
	}

	for (int i = 0; i < tInfo.iPointNum && i<MAX_SMOKEDETECT_POINT_NUM ; i++)
	{
		if(m_tPoints[i].iX < 0 || m_tPoints[i].iY < 0)
		{
			AddLog(LOG_TYPE_FAIL,"","m_tPoints[i].iX = %d, m_tPoints[i].iY = %d", m_tPoints[i].iX, m_tPoints[i].iY);
			return;
		}
		tInfo.stPoints[i].iX = m_tPoints[i].iX;
		tInfo.stPoints[i].iY = m_tPoints[i].iY;
	}
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SMOKEDETECT, m_iChannelNO, &tInfo, sizeof(VCASmokeDetect));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_SMOKEDETECT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_SMOKEDETECT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaSmokeDetect::OnBnClickedBtnSmokedetectRegionDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}
	/* The following code can take out the corresponding parameters from the draw dialog box */
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_SMOKEDETECT_POINT_NUM);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		return;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		m_editRegionPoints.SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDIT_SMOKEDETECT_REGION_POINTNUM, iPointNum);
		vca_TPoint ptPolygon[MAX_SMOKEDETECT_POINT_NUM] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoints.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)ptPolygon);
		for (int i = 0; i < iPointNum && i<MAX_SMOKEDETECT_POINT_NUM ; i++)
		{
			m_tPoints[i].iX = ptPolygon[i].iX;
			m_tPoints[i].iY = ptPolygon[i].iY;
		}
	}
	else
	{
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

BOOL CLS_VcaSmokeDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  add extra initialization here
	UpdateUIText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaSmokeDetect::UpdateUIText()
{
	SetDlgItemText(IDC_STC_SMOKEDETECT_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_SMOKEDETECT_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_SMOKEDETECT_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_SMOKEDETECT_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BUTTON_SMOKEDETECT, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_SMOKEDETECT, GetTextByLan("抽烟检测算法", "SmokeDetect"));

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, _T("IPC"));
	m_cboDevType.InsertString(1, _T("NVR"));
	m_cboDevType.SetCurSel(0);
}

void CLS_VcaSmokeDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	UpdatePageUI();
}

void CLS_VcaSmokeDetect::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCASmokeDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SMOKEDETECT, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		SetDlgItemInt(IDC_EDIT_SMOKEDETECT_SENSITIVITY, tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_SMOKEDETECT_REGION_POINTNUM, tInfo.iPointNum);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointNum && i<MAX_SMOKEDETECT_POINT_NUM; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.stPoints[i].iX, tInfo.stPoints[i].iY);
			m_tPoints[i].iX = tInfo.stPoints[i].iX;
			m_tPoints[i].iY = tInfo.stPoints[i].iY;
		}
		SetDlgItemText(IDC_EDIT_SMOKEDETECT_REGION_POINTS, cstPolygonBuf);
		m_chkSmokeDetect.SetCheck(tInfo.iValid);
	}

}



void CLS_VcaSmokeDetect::OnBnClickedCheckSmokedetect()
{
	// TODO: Add control notification handler code here
}

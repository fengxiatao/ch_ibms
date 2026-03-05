// CLS_PhoneDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_PhoneDetect.h"


// CLS_PhoneDetect dialog

IMPLEMENT_DYNAMIC(CLS_VcaPhoneDetect, CDialog)

CLS_VcaPhoneDetect::CLS_VcaPhoneDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaPhoneDetect::IDD, pParent)
{

}

CLS_VcaPhoneDetect::~CLS_VcaPhoneDetect()
{
}

void CLS_VcaPhoneDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_PHONEDETECT, m_chkPhoneDetect);
	DDX_Control(pDX, IDC_EDIT_PHONEDETECT_REGION_POINTS, m_editRegionPoints);
	DDX_Control(pDX, IDC_COMBO_PHONE_DEV_TYPE, m_cboDevType);
}


BEGIN_MESSAGE_MAP(CLS_VcaPhoneDetect, CDialog)
	ON_BN_CLICKED(IDC_BTN_PHONEDETECT_REGION_DRAW, &CLS_VcaPhoneDetect::OnBnClickedBtnPhonedetectRegionDraw)
	ON_BN_CLICKED(IDC_BUTTON_PHONEDETECT, &CLS_VcaPhoneDetect::OnBnClickedButtonPhonedetect)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// VCAEVENT_PhoneDetect message handler
void CLS_VcaPhoneDetect::OnBnClickedButtonPhonedetect()
{
	// TODO: Add control notification handler code here
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaPept::OnBnClickedButtonSmokedetect Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VCAPhoneDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	tInfo.iValid = m_chkPhoneDetect.GetCheck();
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDIT_PHONEDETECT_SENSITIVITY);
	tInfo.iPointNum =  GetDlgItemInt(IDC_EDIT_PHONEDETECT_REGION_POINTNUM);
	if(tInfo.iPointNum < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","iPointNum = %d, inValid.", tInfo.iPointNum);
		return;
	}

	for (int i = 0; i < tInfo.iPointNum && i<MAX_PHONEDETECT_POINT_NUM ; i++)
	{
		if(m_tPoints[i].iX < 0 || m_tPoints[i].iY < 0)
		{
			AddLog(LOG_TYPE_FAIL,"","m_tPoints[i].iX = %d, m_tPoints[i].iY = %d", m_tPoints[i].iX, m_tPoints[i].iY);
			return;
		}
		tInfo.stPoints[i].iX = m_tPoints[i].iX;
		tInfo.stPoints[i].iY = m_tPoints[i].iY;
	}
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_PHONEDETECT, m_iChannelNO, &tInfo, sizeof(VCAPhoneDetect));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_PHONEDETECT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_PHONEDETECT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaPhoneDetect::OnBnClickedBtnPhonedetectRegionDraw()
{
	// TODO: Add control notification handler code here
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
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_PHONEDETECT_POINT_NUM);
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
		SetDlgItemInt(IDC_EDIT_PHONEDETECT_REGION_POINTNUM, iPointNum);
		vca_TPoint ptPolygon[MAX_PHONEDETECT_POINT_NUM] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoints.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)ptPolygon);
		for (int i = 0; i < iPointNum && i<MAX_PHONEDETECT_POINT_NUM ; i++)
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

BOOL CLS_VcaPhoneDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  add extra initialization here
	UpdateUIText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaPhoneDetect::UpdateUIText()
{
	SetDlgItemText(IDC_STC_PHONEDETECT_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_PHONEDETECT_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_PHONEDETECT_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_PHONEDETECT_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BUTTON_PHONEDETECT, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_PHONEDETECT, GetTextByLan("打电话检测算法", "PhoneDetect"));

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, _T("IPC"));
	m_cboDevType.InsertString(1, _T("NVR"));
	m_cboDevType.SetCurSel(0);
}

void CLS_VcaPhoneDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	UpdatePageUI();
}

void CLS_VcaPhoneDetect::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAPhoneDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_PHONEDETECT, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		SetDlgItemInt(IDC_EDIT_PHONEDETECT_SENSITIVITY, tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_PHONEDETECT_REGION_POINTNUM, tInfo.iPointNum);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointNum && i<MAX_PHONEDETECT_POINT_NUM; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.stPoints[i].iX, tInfo.stPoints[i].iY);
			m_tPoints[i].iX = tInfo.stPoints[i].iX;
			m_tPoints[i].iY = tInfo.stPoints[i].iY;
		}
		SetDlgItemText(IDC_EDIT_PHONEDETECT_REGION_POINTS, cstPolygonBuf);
		m_chkPhoneDetect.SetCheck(tInfo.iValid);
	}
}

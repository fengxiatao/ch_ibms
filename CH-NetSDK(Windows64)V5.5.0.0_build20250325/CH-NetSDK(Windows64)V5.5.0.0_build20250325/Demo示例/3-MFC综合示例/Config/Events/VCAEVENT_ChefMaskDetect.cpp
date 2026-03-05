// VCAEVENT_ChefMaskDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_ChefMaskDetect.h"


// CLS_VcaChefMaskDetect dialog

IMPLEMENT_DYNAMIC(CLS_VcaChefMaskDetect, CDialog)

CLS_VcaChefMaskDetect::CLS_VcaChefMaskDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaChefMaskDetect::IDD, pParent)
{

}

CLS_VcaChefMaskDetect::~CLS_VcaChefMaskDetect()
{
}

void CLS_VcaChefMaskDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_CHEFMASKDETECT_REGION_POINTS, m_editRegionPoins);
	DDX_Control(pDX, IDC_CHECK_CHEFMASKDETECT, m_chkHatMaskDetect);
	DDX_Control(pDX, IDC_COMBO_CHEFMASK_PUSHMODE, m_cboPushMode);
	DDX_Control(pDX, IDC_COMBO_CHEFMASK_DEV_TYPE, m_cboDevType);
}


BEGIN_MESSAGE_MAP(CLS_VcaChefMaskDetect, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_CHEFMASKDETECT, &CLS_VcaChefMaskDetect::OnBnClickedButtonChefmaskdetect)
	ON_BN_CLICKED(IDC_BTN_CHEFMASKDETECT_REGION_DRAW, &CLS_VcaChefMaskDetect::OnBnClickedBtnChefmaskdetectRegionDraw)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_CHECK_CHEFMASKDETECT, &CLS_VcaChefMaskDetect::OnBnClickedCheckChefmaskdetect)
	ON_CBN_SELCHANGE(IDC_COMBO_CHEFMASK_DEV_TYPE, &CLS_VcaChefMaskDetect::OnCbnSelchangeComboChefmaskDevType)
END_MESSAGE_MAP()


// CLS_VcaChefMaskDetect message handler

void CLS_VcaChefMaskDetect::OnBnClickedButtonChefmaskdetect()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaPept::OnBnClickedButtonChefmaskdetect Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}


	VCAChefMaskDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	tInfo.iPushMode = m_cboPushMode.GetCurSel();
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDIT_CHEFHATDETECT_REGION_POINTNUM);
	tInfo.iPointNum =  GetDlgItemInt(IDC_EDIT_CHEFMASKDETECT_REGION_POINTNUM);
	if(tInfo.iPointNum < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","xxxxxxiPointNum = %d", tInfo.iPointNum);
		return;
	}

	for (int i = 0; i < tInfo.iPointNum && i<MAX_CHEFMASKDETECT_POINT_NUM ; i++)
	{
		if(m_tPoints[i].iX < 0 || m_tPoints[i].iY < 0)
		{
			AddLog(LOG_TYPE_FAIL,"","xxxxxxm_tPoints[i].iX = %d, m_tPoints[i].iY = %d", m_tPoints[i].iX, m_tPoints[i].iY);
			return;
		}
		tInfo.stPoints[i].iX = m_tPoints[i].iX;
		tInfo.stPoints[i].iY = m_tPoints[i].iY;
	}
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CHEFMASKDETECT, m_iChannelNO, &tInfo, sizeof(VCAChefMaskDetect));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_CHEFMASKDETECT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_CHEFMASKDETECT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaChefMaskDetect::OnBnClickedBtnChefmaskdetectRegionDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_CHEFMASKDETECT_POINT_NUM);
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
		m_editRegionPoins.SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDIT_CHEFMASKDETECT_REGION_POINTNUM, iPointNum);
		vca_TPoint ptPolygon[MAX_CHEFMASKDETECT_POINT_NUM] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)ptPolygon);
		for (int i = 0; i < iPointNum && i<MAX_CHEFMASKDETECT_POINT_NUM ; i++)
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

BOOL CLS_VcaChefMaskDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaChefMaskDetect::UpdateUIText()
{
	
	SetDlgItemText(IDC_STC_CHEFMASKDETECT_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_CHEFMASKDETECT_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_CHEFMASKDETECT_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_CHEFMASKDETECT_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BUTTON_CHEFMASKDETECT, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_CHEFMASKDETECT, GetTextByLan("厨师口罩算法", "ChefMaskDetect"));
	SetDlgItemText(IDC_STATICCHEFMASK_PUSHMODE, GetTextByLan("推图策略", "PushMode"));

	m_cboPushMode.ResetContent();
	m_cboPushMode.InsertString(0, GetTextByLan("预留", "Reserve") );
	m_cboPushMode.InsertString(1, GetTextByLan("最快", "Fastest"));
	m_cboPushMode.InsertString(2, GetTextByLan("最优", "Optimal"));
	m_cboPushMode.SetCurSel(0);

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, _T("IPC"));
	m_cboDevType.InsertString(1, _T("NVR"));
	m_cboDevType.SetCurSel(0);
}

void CLS_VcaChefMaskDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	UpdatePageUI();
}

void CLS_VcaChefMaskDetect::OnLanguageChanged()
{
	UpdateUIText();
}

void CLS_VcaChefMaskDetect::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAChefMaskDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CHEFMASKDETECT, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		SetDlgItemInt(IDC_EDIT_CHEFHATDETECT_REGION_POINTNUM, tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_CHEFMASKDETECT_REGION_POINTNUM, tInfo.iPointNum);
		m_cboPushMode.SetCurSel(tInfo.iPushMode);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointNum && i<MAX_CHEFMASKDETECT_POINT_NUM; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.stPoints[i].iX, tInfo.stPoints[i].iY);
			m_tPoints[i].iX = tInfo.stPoints[i].iX;
			m_tPoints[i].iY = tInfo.stPoints[i].iY;
		}
		SetDlgItemText(IDC_EDIT_CHEFMASKDETECT_REGION_POINTS, cstPolygonBuf);
	}
}

void CLS_VcaChefMaskDetect::OnBnClickedCheckChefmaskdetect()
{
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = m_iSceneID;
	tParam.iDevType = m_cboDevType.GetCurSel();
	tParam.iArithmeticEx |= m_chkHatMaskDetect.GetCheck() << 5;

	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ANYSCENE fail!");

	}
}

void CLS_VcaChefMaskDetect::OnCbnSelchangeComboChefmaskDevType()
{
	// TODO: Add control notification handler code here
	AnyScene tAnyScene = {0};
	tAnyScene.iBufSize = sizeof(AnyScene);
	tAnyScene.iSceneID = m_iSceneID;
	tAnyScene.iDevType = m_cboDevType.GetCurSel();
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tAnyScene,sizeof(tAnyScene), &iBytesReturned);
	if (iRet >= 0)
	{
		int a = tAnyScene.iArithmeticEx ;
		a = a & (1<<5);
		m_chkHatMaskDetect.SetCheck(a);
	}
}

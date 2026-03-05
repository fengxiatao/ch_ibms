// VCAEVENT_ChefHatDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_ChefHatDetect.h"


// CLS_VcaChefHatDetect dialog

IMPLEMENT_DYNAMIC(CLS_VcaChefHatDetect, CDialog)

CLS_VcaChefHatDetect::CLS_VcaChefHatDetect(CWnd* pParent /*=NULL*/)
	:CLS_VCAEventBasePage(CLS_VcaChefHatDetect::IDD, pParent)
{

}

CLS_VcaChefHatDetect::~CLS_VcaChefHatDetect()
{
}

void CLS_VcaChefHatDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_CHEFHATDETECT_REGION_POINTS, m_editRegionPoins);
	//DDX_Control(pDX, IDC_CHECK_CHEF, m_chkChef);
	DDX_Control(pDX, IDC_CHECK_CHEFHATDETECT, m_chkChefHatDetect);
	DDX_Control(pDX, IDC_COMBO_CHEF_HAT_DEV_TYPE, m_cboDevType);
	DDX_Control(pDX, IDC_COMBO_CHEF_HAT_PUSH_MODE, m_cboPushMod);
}


BEGIN_MESSAGE_MAP(CLS_VcaChefHatDetect, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_CHEFHATDETECT, &CLS_VcaChefHatDetect::OnBnClickedButtonChefhatdetect)
	ON_BN_CLICKED(IDC_BTN_CHEFHATDETECT_REGION_DRAW, &CLS_VcaChefHatDetect::OnBnClickedBtnChefhatdetectRegionDraw)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_CHECK_CHEFHATDETECT, &CLS_VcaChefHatDetect::OnBnClickedCheckChefhatdetect)
	ON_CBN_SELCHANGE(IDC_COMBO_CHEF_HAT_DEV_TYPE, &CLS_VcaChefHatDetect::OnCbnSelchangeComboChefHatDevType)
END_MESSAGE_MAP()


// CLS_VcaChefHatDetect message handler

void CLS_VcaChefHatDetect::OnBnClickedButtonChefhatdetect()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaPept::OnBnClickedButtonChefmaskdetect Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}


	VCAChefHatDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	tInfo.iPushMode = m_cboPushMod.GetCurSel();
	tInfo.iSensitivity =  GetDlgItemInt(IDC_EDIT_CHEF_HAT_DETECT);
	tInfo.iPointNum =  GetDlgItemInt(IDC_EDIT_CHEFHATDETECT_REGION_POINTNUM);
	if(tInfo.iPointNum < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","iPointNum = %d", tInfo.iPointNum);
		return;
	}

	for (int i = 0; i < tInfo.iPointNum && i<MAX_CHEFHATDETECT_POINT_NUM ; i++)
	{
		if(m_tPoints[i].iX < 0 || m_tPoints[i].iY < 0)
		{
			AddLog(LOG_TYPE_FAIL,"","tPoints[i].iX = %d, m_tPoints[i].iY = %d", m_tPoints[i].iX, m_tPoints[i].iY);
			return;
		}
		tInfo.stPoints[i].iX = m_tPoints[i].iX;
		tInfo.stPoints[i].iY = m_tPoints[i].iY;
	}
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CHEFHATDETECT, m_iChannelNO, &tInfo, sizeof(VCAChefMaskDetect));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_CHEFHATDETECT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_CHEFHATDETECT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaChefHatDetect::OnBnClickedBtnChefhatdetectRegionDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_CHEFHATDETECT_POINT_NUM);
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
		SetDlgItemInt(IDC_EDIT_CHEFHATDETECT_REGION_POINTNUM, iPointNum);
		vca_TPoint ptPolygon[MAX_CHEFHATDETECT_POINT_NUM] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)ptPolygon);
		for (int i = 0; i < iPointNum && i< MAX_CHEFHATDETECT_POINT_NUM; i++)
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
void CLS_VcaChefHatDetect::UpdateUIText()
{

	SetDlgItemText(IDC_STC_CHEFHATDETECT_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_CHEFHATDETECT_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_CHEFHATDETECT_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_CHEFHATDETECT_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BUTTON_CHEFHATDETECT, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_CHECK_CHEFHATDETECT, GetTextByLan("厨师帽算法", "ChefHatDetect"));
	SetDlgItemText(IDC_STATIC_CHEFHATDETECT, GetTextByLan("明厨亮灶算法", "ChefHatDetect"));
	SetDlgItemText(IDC_STATIC_CHEFHAT_PUSHMODE, GetTextByLan("推图策略", "PushMode"));

	m_cboPushMod.ResetContent();
	m_cboPushMod.InsertString(0, GetTextByLan("预留", "Reserve"));
	m_cboPushMod.InsertString(1, GetTextByLan("最快", "Fastest"));
	m_cboPushMod.InsertString(2, GetTextByLan("最优", "Optimal"));
	m_cboPushMod.SetCurSel(0);

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, _T("IPC"));
	m_cboDevType.InsertString(1, _T("NVR"));
	m_cboDevType.SetCurSel(0);
	//m_sldSensitivity.SetRange(0,100);
}
void CLS_VcaChefHatDetect::PreInitDialog()
{
	// TODO: add specialized code and/or call base class here
	
	CLS_VCAEventBasePage::PreInitDialog();
}



void CLS_VcaChefHatDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	UpdatePageUI();
	// TODO: add message handler code here
}

void CLS_VcaChefHatDetect::OnLanguageChanged()
{
	UpdateUIText();
}

void CLS_VcaChefHatDetect::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAChefHatDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CHEFHATDETECT, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		SetDlgItemInt(IDC_EDIT_CHEF_HAT_DETECT, tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_CHEFHATDETECT_REGION_POINTNUM, tInfo.iPointNum);
		m_cboPushMod.SetCurSel(tInfo.iPushMode);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointNum && i<MAX_CHEFHATDETECT_POINT_NUM; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.stPoints[i].iX, tInfo.stPoints[i].iY);
			m_tPoints[i].iX = tInfo.stPoints[i].iX;
			m_tPoints[i].iY = tInfo.stPoints[i].iY;
		}
		SetDlgItemText(IDC_EDIT_CHEFHATDETECT_REGION_POINTS, cstPolygonBuf);
	}

	int iByteReturn = -1;
	FuncAbilityLevel stSwitchSnapAbility = {0};
	stSwitchSnapAbility.iSize = sizeof(stSwitchSnapAbility);
	stSwitchSnapAbility.iMainFuncType = 0x9;
	stSwitchSnapAbility.iSubFuncType = 63;

	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stSwitchSnapAbility, sizeof(stSwitchSnapAbility), &iByteReturn);
	if (iRet < 0 || strlen(stSwitchSnapAbility.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "Get ablitity 0x9 Failed! m_iLogonID %d iSubFuncType = 63", m_iLogonID);
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_CHAFHATDETECT, _ttoi(stSwitchSnapAbility.cParam));
	}
}


void CLS_VcaChefHatDetect::OnBnClickedCheckChefhatdetect()
{
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = m_iSceneID;
	tParam.iDevType = m_cboDevType.GetCurSel();
	tParam.iArithmeticEx |= m_chkChefHatDetect.GetCheck() << 4;

	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ANYSCENE fail!");

	}
}

BOOL CLS_VcaChefHatDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaChefHatDetect::OnCbnSelchangeComboChefHatDevType()
{
	AnyScene tAnyScene = {0};
	tAnyScene.iBufSize = sizeof(AnyScene);
	tAnyScene.iSceneID = m_iSceneID;
	tAnyScene.iDevType =  m_cboDevType.GetCurSel();
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tAnyScene,sizeof(tAnyScene), &iBytesReturned);
	if (iRet >= 0)
	{
		int a = tAnyScene.iArithmeticEx ;
		a = a & (1<<4);
		m_chkChefHatDetect.SetCheck(a);
	}
}

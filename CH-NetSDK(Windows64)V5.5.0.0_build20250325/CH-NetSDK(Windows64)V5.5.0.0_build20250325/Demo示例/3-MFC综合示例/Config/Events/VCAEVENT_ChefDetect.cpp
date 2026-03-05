// VCAEVENT_ChefDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_ChefDetect.h"


// CLS_VcaChefDetect dialog

IMPLEMENT_DYNAMIC(CLS_VcaChefDetect, CDialog)

CLS_VcaChefDetect::CLS_VcaChefDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaChefDetect::IDD, pParent)
{

}

CLS_VcaChefDetect::~CLS_VcaChefDetect()
{
}

void CLS_VcaChefDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_CHEF_DEV_TYPE, m_cboChefDevType);
	DDX_Control(pDX, IDC_COMBO_CHEF_DETECT_TYPE, m_cboChefDetectType);
	DDX_Control(pDX, IDC_EDIT_CHEFDETECT_SENSITIVE, m_edtChefDetectSensitive);
	DDX_Control(pDX, IDC_COMBO_CHEF_PUSHMODE, m_cboChefPushPicMode);
	DDX_Control(pDX, IDC_EDIT_CHEFDETECT_REGION_POINTCOUNT, m_edtChefDetectPointCount);
	DDX_Control(pDX, IDC_EDIT_CHEFDETECT_REGION_POINTS, m_edtChefDetectPointsArray);
	DDX_Control(pDX, IDC_CHECK_ENABLE_ARITH, m_chkEnable);
	DDX_Control(pDX, IDC_CHECK_ENABLE_CHEF_HAT, m_chkChefHat);
	DDX_Control(pDX, IDC_CHECK_ENABLE_CHEF_MASK, m_chkChefMask);
	DDX_Control(pDX, IDC_CHECK_ENABLE_THREE, m_chkChef);
}


BEGIN_MESSAGE_MAP(CLS_VcaChefDetect, CDialog)
	ON_BN_CLICKED(IDC_BTN_CHEFDETECT_REGION_DRAW, &CLS_VcaChefDetect::OnBnClickedBtnChefDetectRegionDraw)
	ON_BN_CLICKED(IDC_BUTTON_CHEFDETECT, &CLS_VcaChefDetect::OnBnClickedButtonChefDetect)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_CHECK_ENABLE_ARITH, &CLS_VcaChefDetect::OnBnClickedCheckEnableArith)
	ON_BN_CLICKED(IDC_BUTTON_CHEF_SET_ENABLE, &CLS_VcaChefDetect::OnBnClickedButtonChefSetEnable)
	ON_CBN_SELCHANGE(IDC_COMBO_CHEF_DEV_TYPE, &CLS_VcaChefDetect::OnCbnSelchangeComboChefDevType)
END_MESSAGE_MAP()


// CLS_VcaChefDetect message handler

BOOL CLS_VcaChefDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaChefDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	UpdatePageUI();
}

void CLS_VcaChefDetect::OnLanguageChanged()
{
	UpdateUIText();
}

void CLS_VcaChefDetect::UpdateUIText()
{
	SetDlgItemText(IDC_STC_CHEFDETECT_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_CHEFDETECT_REGION_POINTCOUNT, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_CHEFDETECT_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_CHEFDETECT_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BUTTON_CHEFDETECT, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_STATIC_CHEF_DEV_TYPE, GetTextByLan("设备类型", "DeviceType"));
	SetDlgItemText(IDC_STATIC_CHEF_PUSHMODE, GetTextByLan("推图策略", "PushMode"));
	SetDlgItemText(IDC_CHEF_DETECT_TYPE, GetTextByLan("厨师检测类型", "Chef Detect Type"));
	SetDlgItemText(IDC_CHECK_ENABLE_ARITH, GetTextByLan("启用算法", "Enable"));
	SetDlgItemText(IDC_CHECK_ENABLE_THREE, GetTextByLan("厨师服", "Chef"));
	SetDlgItemText(IDC_CHECK_ENABLE_CHEF_HAT, GetTextByLan("厨师帽", "ChefHat"));
	SetDlgItemText(IDC_CHECK_ENABLE_CHEF_MASK, GetTextByLan("厨师口罩", "ChefMask"));

	m_cboChefPushPicMode.ResetContent();
	m_cboChefPushPicMode.InsertString(0, GetTextByLan("预留", "Reserve") );
	m_cboChefPushPicMode.InsertString(1, GetTextByLan("最快", "Fastest"));
	m_cboChefPushPicMode.InsertString(2, GetTextByLan("最优", "Optimal"));
	m_cboChefPushPicMode.SetCurSel(0);

	m_cboChefDevType.ResetContent();
	m_cboChefDevType.InsertString(0, _T("IPC"));
	m_cboChefDevType.InsertString(1, _T("NVR"));
	m_cboChefDevType.SetCurSel(0);

	m_cboChefDetectType.ResetContent();
	m_cboChefDetectType.InsertString(0, GetTextByLan("厨师服", "Chef clothes"));
	m_cboChefDetectType.SetCurSel(0);
}

void CLS_VcaChefDetect::UpdatePageUI()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		return;
	}

	VCAChefDetect tInfo = {0};
	tInfo.iType = m_cboChefDetectType.GetCurSel();
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CHEFDETECT, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		SetDlgItemInt(IDC_EDIT_CHEFDETECT_SENSITIVE, tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_CHEFDETECT_REGION_POINTCOUNT, tInfo.iPointCount);
		m_cboChefPushPicMode.SetCurSel(tInfo.iPushMode);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointCount && i < MAX_CHEFDETECT_POINT_COUNT; ++i)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.tPoints[i].iX, tInfo.tPoints[i].iY);
			m_tPointsArray[i].iX = tInfo.tPoints[i].iX;
			m_tPointsArray[i].iY = tInfo.tPoints[i].iY;
		}
		SetDlgItemText(IDC_EDIT_CHEFDETECT_REGION_POINTS, cstPolygonBuf);
	}

	
}

void CLS_VcaChefDetect::OnBnClickedBtnChefDetectRegionDraw()
{
	if (NULL == m_pDlgVideoView) {
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView) {
			return;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, MAX_CHEFDETECT_POINT_COUNT);
	int iPointCount = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointCount, &iDirection, TRUE);
	if (-1 == iSetRet) {
		return;
	}

	if (IDOK == m_pDlgVideoView->DoModal()) {
		m_edtChefDetectPointsArray.SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDIT_CHEFDETECT_REGION_POINTCOUNT, iPointCount);
		CString cstPolygon = "";
		m_edtChefDetectPointsArray.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointCount, (POINT*)m_tPointsArray);
	}

	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VcaChefDetect::OnBnClickedButtonChefDetect()
{
	int iRet = RET_FAILED;
	
	VCAChefDetect tInfo = {0};
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	tInfo.iPushMode = m_cboChefPushPicMode.GetCurSel();
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDIT_CHEFDETECT_SENSITIVE);
	tInfo.iPointCount =  GetDlgItemInt(IDC_EDIT_CHEFDETECT_REGION_POINTCOUNT);
	if(tInfo.iPointCount < 0){
		AddLog(LOG_TYPE_FAIL, "", "[OnBnClickedButtonChefDetect]iPointCount = %d", tInfo.iPointCount);
		return;
	}

	for (int i = 0; i < tInfo.iPointCount && i<MAX_CHEFDETECT_POINT_COUNT ; ++i)
	{
		if(m_tPointsArray[i].iX < 0 || m_tPointsArray[i].iY < 0){
			AddLog(LOG_TYPE_FAIL, "", "[OnBnClickedButtonChefDetect]m_tPoints[%d].iX = %d, m_tPoints[%d].iY = %d"
				, i, m_tPointsArray[i].iX, i, m_tPointsArray[i].iY);
			continue;
		}
		tInfo.tPoints[i].iX = m_tPointsArray[i].iX;
		tInfo.tPoints[i].iY = m_tPointsArray[i].iY;
	}

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CHEFDETECT, m_iChannelNO, &tInfo, sizeof(VCAChefDetect));
	if (iRet < 0) {
		AddLog(LOG_TYPE_FAIL, "", "OnBnClickedButtonChefDetect[VCA_CMD_CHEFDETECT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	} else {
		AddLog(LOG_TYPE_SUCC, "", "OnBnClickedButtonChefDetect[VCA_CMD_CHEFDETECT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}



void CLS_VcaChefDetect::OnBnClickedCheckEnableArith()
{
	// TODO: Add control notification handler code here
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = m_iSceneID;
	tParam.iDevType = m_cboChefDevType.GetCurSel();
	tParam.iArithmeticEx |= m_chkEnable.GetCheck() << 17;
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam));
	if (iRet < 0) {
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ANYSCENE fail!");
	} else {
		AddLog(LOG_TYPE_SUCC, "", "[OnBnClickedButtonChefDetect]NET_CLIENT_ANYSCENE success!");
	}
}


void CLS_VcaChefDetect::OnBnClickedButtonChefSetEnable()
{
	// TODO: Add control notification handler code here
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = m_iSceneID;
	tParam.iDevType = m_cboChefDevType.GetCurSel();
	tParam.iArithmeticEx |= (m_chkChef.GetCheck() << 17)|(m_chkChefHat.GetCheck() << 4)|(m_chkChefMask.GetCheck() << 5);
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam));
	if (iRet < 0) {
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ANYSCENE fail!");
	} else {
		AddLog(LOG_TYPE_SUCC, "", "[OnBnClickedButtonChefDetect]NET_CLIENT_ANYSCENE success!");
	}
}


void CLS_VcaChefDetect::OnCbnSelchangeComboChefDevType()
{
	AnyScene tAnyScene = {0};
	tAnyScene.iBufSize = sizeof(AnyScene);
	tAnyScene.iSceneID = m_iSceneID;
	tAnyScene.iDevType = m_cboChefDevType.GetCurSel();
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ANYSCENE, m_iChannelNO, &tAnyScene,sizeof(tAnyScene), &iBytesReturned);
	if (iRet >= 0)
	{
		m_chkEnable.SetCheck(tAnyScene.iArithmeticEx&(1 << 17));
		m_chkChef.SetCheck(tAnyScene.iArithmeticEx&(1 << 17));
		m_chkChefHat.SetCheck(tAnyScene.iArithmeticEx&(1 << 4));
		m_chkChefMask.SetCheck(tAnyScene.iArithmeticEx&(1 << 5));
	}
}

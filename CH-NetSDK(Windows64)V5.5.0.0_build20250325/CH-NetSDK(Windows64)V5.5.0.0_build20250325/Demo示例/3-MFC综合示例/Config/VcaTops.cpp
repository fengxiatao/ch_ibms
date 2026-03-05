// E:\SDK_ALL\trunk\Demo\NetClientDemo\Config\VcaTops.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VcaTops.h"


// CLS_VcaTops dialog

IMPLEMENT_DYNAMIC(CLS_VcaTops, CDialog)

CLS_VcaTops::CLS_VcaTops(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VcaTops::IDD, pParent)
{

}

CLS_VcaTops::~CLS_VcaTops()
{
}

void CLS_VcaTops::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_VCA_TYPE, m_cboVcaTyep);
	DDX_Control(pDX, IDC_COMBO_SENCE_ID, m_cboSenceId);
	DDX_Control(pDX, IDC_COMBO_DEV_TYPE, m_cboDevType);
	DDX_Control(pDX, IDC_CHECK_ABNORMAL, m_chkAbmormal);
	DDX_Control(pDX, IDC_CHECK_STRANDED, m_chkStranded);
	DDX_Control(pDX, IDC_CHECK_ALONE, m_chkAlone);
	DDX_Control(pDX, IDC_CHECK_DELIVERY_GOODS, m_chkDeliveryGoods);
	DDX_Control(pDX, IDC_CHECK_LINGER, m_chkLinger);
	DDX_Control(pDX, IDC_CHECK_GOODS_LEFT, m_chkGoodsLeft);
	DDX_Control(pDX, IDC_CHECK_GOODS_LOSE, m_chkGoodsLose);
	DDX_Control(pDX, IDC_CHECK_THERMAL_CHERT, m_chkThermalChert);
}


BEGIN_MESSAGE_MAP(CLS_VcaTops, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_FLASH, &CLS_VcaTops::OnBnClickedButtonFlash)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_TOPS_SET, &CLS_VcaTops::OnBnClickedButtonTopsSet)
END_MESSAGE_MAP()



void CLS_VcaTops::OnBnClickedButtonFlash()
{
	// TODO: Add control notification handler code here
	UpdatePageUI();

}

BOOL CLS_VcaTops::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UpdateUIText();
	// TODO:  add extra initialization here

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VcaTops::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	UpdatePageUI();
	// TODO: add message handler code here
}

void CLS_VcaTops::UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_VCA_TYPE, GetTextByLan("算法类型", "VcaType"));
	SetDlgItemText(IDC_STATIC_SENCE_ID, GetTextByLan("场景号", "SenceID"));
	SetDlgItemText(IDC_STATIC_DEV_TYPE, GetTextByLan("设备类型", "DevType"));
	SetDlgItemText(IDC_STATIC_TOPS, GetTextByLan("算力参考值", "CaclValue"));
	SetDlgItemText(IDC_STATIC_MAX_CNT, GetTextByLan("最大开启数量", "MaxCnt"));
	SetDlgItemText(IDC_STATIC_USED_CNT, GetTextByLan("已开启数量", "UsedCnt"));
	SetDlgItemText(IDC_BUTTON_FLASH, GetTextByLan("刷新", "Refresh"));
	m_cboSenceId.ResetContent();
	for (int i=0; i<MAX_SCENE_NUM; i++)
	{
		CString cstrSenceId;
		cstrSenceId.Format("%d",i+1);
		m_cboSenceId.InsertString(i, cstrSenceId);
	}
	m_cboSenceId.SetCurSel(0);

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, "IPC");
	m_cboDevType.InsertString(1, "NVR");
	m_cboDevType.SetCurSel(0);

	m_cboVcaTyep.ResetContent();
	for (int i=0; i<VCA_EVENT_MAX; i++)
	{
		CString cstrVcaTyep;
		cstrVcaTyep.Format("%d",i);
		m_cboVcaTyep.InsertString(i, cstrVcaTyep);
	}
	m_cboVcaTyep.SetCurSel(0);

}


void CLS_VcaTops::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCATops tInfo = {0};
	tInfo.iDevType = m_cboDevType.GetCurSel();
	tInfo.iSceneID = m_cboSenceId.GetCurSel();
	tInfo.iVcaType = m_cboVcaTyep.GetCurSel();

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_VCA_TOPS, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboDevType.SetCurSel(tInfo.iDevType);
		SetDlgItemInt(IDC_EDIT_TOPS, tInfo.iTops);
		SetDlgItemInt(IDC_EDIT_MAX_CNT, tInfo.iMaxCnt);
		SetDlgItemInt(IDC_EDIT_USED_CNT, tInfo.iUsedCnt);
		
	}

	VcaArithmeticList tParam = {0};
	tParam.iSize = sizeof(tParam);
	tParam.iChannelNo = m_iChannelNO;
	
	tParam.iArithmeticType = VCA_ARITHMETIC_ABNORMAL_NUMBER;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (iRet == -2 && tParam.iEnableCount > 0)
	{
		tParam.piEnableValue = new int[tParam.iEnableCount];
		iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	}
	if (0 == iRet)
	{
		m_chkAbmormal.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	tParam.iArithmeticType = VCA_ARITHMETIC_STRANDED;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (0 == iRet)
	{
		m_chkStranded.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	tParam.iArithmeticType = VCA_ARITHMETIC_ALONE;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (0 == iRet)
	{
		m_chkAlone.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	tParam.iArithmeticType = VCA_ARITHMETIC_DELIVERGOODS;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (0 == iRet)
	{
		m_chkDeliveryGoods.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	tParam.iArithmeticType = VCA_ARITHMETIC_LINGER;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (0 == iRet)
	{
		m_chkLinger.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	tParam.iArithmeticType = VCA_ARITHMETIC_GOODS_LEFT;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (0 == iRet)
	{
		m_chkGoodsLeft.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	tParam.iArithmeticType = VCA_ARITHMETIC_GOODS_LOSE;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (0 == iRet)
	{
		m_chkGoodsLose.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	tParam.iArithmeticType = VCA_ARITHMETIC_THREMAL_CHART;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (0 == iRet)
	{
		m_chkThermalChert.EnableWindow(tParam.piEnableValue[0]>0?1:0);		
	}

	AnyScene tAnyScene = {0};
	tAnyScene.iBufSize = sizeof(AnyScene);
	tAnyScene.iSceneID = m_cboSenceId.GetCurSel();
	int iBytesReturned = 0;
	iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tAnyScene,sizeof(tAnyScene), &iBytesReturned);
	if (iRet >= 0)
	{
		int a = tAnyScene.iArithmeticEx ;
		a = a & (1<<6);
		m_chkStranded.SetCheck(a);
		a = a & (1<<7);
		m_chkAlone.SetCheck(a);
		a = a & (1<<8);
		m_chkDeliveryGoods.SetCheck(a);
		a = a & (1<<9);
		m_chkLinger.SetCheck(a);
		a = a & (1<<10);
		m_chkGoodsLeft.SetCheck(a);
		a = a & (1<<11);
		m_chkGoodsLose.SetCheck(a);
		a = a & (1<<12);
		m_chkThermalChert.SetCheck(a);
		m_cboDevType.SetCurSel(tAnyScene.iDevType);
	}

}
void CLS_VcaTops::OnBnClickedButtonTopsSet()
{
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID =  m_cboSenceId.GetCurSel();;
	tParam.iDevType = m_cboDevType.GetCurSel();
	tParam.iArithmeticEx |= m_chkStranded.GetCheck() << 6;
	tParam.iArithmeticEx |= m_chkAlone.GetCheck() << 7;
	tParam.iArithmeticEx |= m_chkDeliveryGoods.GetCheck() << 8;
	tParam.iArithmeticEx |= m_chkLinger.GetCheck() << 9;
	tParam.iArithmeticEx |= m_chkGoodsLeft.GetCheck() << 10;
	tParam.iArithmeticEx |= m_chkGoodsLose.GetCheck() << 11;

	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ANYSCENE fail!");

	}
}

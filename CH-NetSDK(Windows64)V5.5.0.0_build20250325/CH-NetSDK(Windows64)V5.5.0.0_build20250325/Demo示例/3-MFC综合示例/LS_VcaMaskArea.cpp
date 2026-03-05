// LS_VcaMaskArea.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "LS_VcaMaskArea.h"

#define VCA_MAX_MASK_AREA_REGION_NUM		8
#define REGION_MAX_MASK_AREA_POINTS_NUM     8

// CLS_VcaMaskArea dialog

IMPLEMENT_DYNAMIC(CLS_VcaMaskArea, CDialog)

CLS_VcaMaskArea::CLS_VcaMaskArea(CWnd* pParent)
	: CLS_BasePage(CLS_VcaMaskArea::IDD, pParent)
{

}

CLS_VcaMaskArea::~CLS_VcaMaskArea()
{
}

void CLS_VcaMaskArea::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_MAKS_AREA_SCENE_TYPE, m_cboVcaType);
	DDX_Control(pDX, IDC_CBO__SCENE_ID, m_cboSceneId);
	DDX_Control(pDX, IDC_COMBO_MASK_AREA_RULE_ID, m_cboRuleID);
	DDX_Control(pDX, IDC_CBO_MASK_AREA_CUR_REGIONNUM, m_cboCurReg);
	DDX_Control(pDX, IDC_COMBO_MASK_AREA_COLOR, m_cboRegColor);
	DDX_Control(pDX, IDC_EDIT_MASK_AREA_REGION_POINTS, m_editRegionPoins);
	DDX_Control(pDX, IDC_CHECK_MASK_AREA_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_CHECK_MASK_AREA_SHOWRULE, m_chkShowRule);
}


BEGIN_MESSAGE_MAP(CLS_VcaMaskArea, CDialog)
	ON_BN_CLICKED(IDC_BTN_MASK_AREA_SET, &CLS_VcaMaskArea::OnBnClickedBtnMaskAreaSet)
	ON_BN_CLICKED(IDC_BTN_MASK_AREA_REGION_DRAW, &CLS_VcaMaskArea::OnBnClickedBtnMaskAreaRegionDraw)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_CBO_MASK_AREA_CUR_REGIONNUM, &CLS_VcaMaskArea::OnCbnSelchangeCboMaskAreaCurRegionnum)
	ON_CBN_SELCHANGE(IDC_CBO__SCENE_ID, &CLS_VcaMaskArea::OnCbnSelchangeCboSceneId)
	ON_CBN_SELCHANGE(IDC_COMBO_MASK_AREA_RULE_ID, &CLS_VcaMaskArea::OnCbnSelchangeCboRuleID)
	ON_CBN_SELCHANGE(IDC_CBO__SCENE_ID, &CLS_VcaMaskArea::OnCbnSelchangeCbo)
END_MESSAGE_MAP()


// CLS_VcaMaskArea message handler

void CLS_VcaMaskArea::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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

void CLS_VcaMaskArea::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VcaMaskArea::OnBnClickedBtnMaskAreaSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","OnBnClickedBtnMaskAreaSet::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VCAMaskAreaParam tInfo = {0};
	tInfo.iBufSize = sizeof(tInfo);
	if(1 == m_cboVcaType.GetCurSel())
	{
		tInfo.iSceneId = (1<<16) | (m_cboSceneId.GetCurSel()) ;
	}
	else
	{
		tInfo.iSceneId =m_cboSceneId.GetCurSel();
	}
	if(16 == m_cboRuleID.GetCurSel())
	{
		tInfo.iRuleId = 0x7fff;
	}
	else
	{
		tInfo.iRuleId = m_cboRuleID.GetCurSel();
	}
	tInfo.iEnable = m_chkEventEnable.GetCheck();
	tInfo.iDisplayRule = m_chkShowRule.GetCheck();
	tInfo.iColor = m_cboRegColor.GetCurSel() + 1;

	tInfo.iAreaNum = GetDlgItemInt(IDC_EDIT_MASK_AREA_REGION_NUM);

	for(int i=0;i<VCA_MAX_MASK_AREA_REGION_NUM && i< tInfo.iAreaNum;i++)
	{
		tInfo.tAreaInfo[i].iPointNum = m_tVCAMaskAreaParam.tAreaInfo[i].iPointNum;
		for (int j=0;j<tInfo.tAreaInfo[i].iPointNum && j<REGION_MAX_MASK_AREA_POINTS_NUM;j++)
		{
			tInfo.tAreaInfo[i].stPoints[j] = m_tVCAMaskAreaParam.tAreaInfo[i].stPoints[j];
		}
	}

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_MASK_AREA_PARAM, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Cls_VcaMaskArea::NetClient_VCASetConfig[VCA_CMD_MASK_AREA_PARAM] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","Cls_VcaMaskArea::NetClient_VCASetConfig[VCA_CMD_MASK_AREA_PARAM] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaMaskArea::OnBnClickedBtnMaskAreaRegionDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(0, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, REGION_MAX_MASK_AREA_POINTS_NUM);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 1)
		{
			m_editRegionPoins.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_MASK_AREA_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_editRegionPoins.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_MASK_AREA_REGION_POINTNUM, 0);
		}

		int iRegionNo = m_cboCurReg.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_MASK_AREA_REGION_POINTNUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_tVCAMaskAreaParam.tAreaInfo[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<REGION_MAX_MASK_AREA_POINTS_NUM ; i++)
		{
			m_tVCAMaskAreaParam.tAreaInfo[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tVCAMaskAreaParam.tAreaInfo[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
		}

		UpdateDrawFinishRegionNum();
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

BOOL CLS_VcaMaskArea::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_VcaMaskArea::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VcaMaskArea::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_MASK_AREA_SHOWRULE, GetTextByLan("显示规则", "DisplayRules"));
	SetDlgItemText(IDC_STC_MASK_AREA_SCENE_TYPE, GetTextByLan("场景类型", "SceneType"));
	SetDlgItemText(IDC_STC_MASK_AERA_SCENE_ID, GetTextByLan("场景号", "SceneId"));
	SetDlgItemText(IDC_STATIC_RULE_ID, GetTextByLan("规则号", "RuleId"));
	SetDlgItemText(IDC_CHECK_MASK_AREA_ENABLE, GetTextByLan("事件使能", "Enable"));
	SetDlgItemText(IDC_STC_MASK_AREA_COLOR, GetTextByLan("区域颜色", "AreaColor"));
	SetDlgItemText(IDC_STC_MASK_AREA_CUR_REGIONNUM, GetTextByLan("当前屏蔽区域号", "CurMaskArea"));
	SetDlgItemText(IDC_STC_MASK_AREA_REGION_NUM, GetTextByLan("已绘制区域数", "DrawedArea"));
	SetDlgItemText(IDC_STC_MASK_AREA_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_MASK_AREA_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_MASK_AREA_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_MASK_AREA_SET, GetTextByLan("设置", "Set"));


	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), GetTextEx(IDS_VCA_COL_YELLOW), 
		GetTextEx(IDS_VCA_COL_BLUE), GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboRegColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboRegColor.InsertString(i, strColor[i]);
	}
	m_cboRegColor.SetCurSel(0);

	m_cboCurReg.ResetContent();
	for (int i=0; i<VCA_MAX_MASK_AREA_REGION_NUM; i++)
	{
		CString cstrRegionNo;
		cstrRegionNo.Format("%d",i+1);
		m_cboCurReg.InsertString(i, cstrRegionNo);
	}
	m_cboCurReg.SetCurSel(0);

	m_cboVcaType.ResetContent();
	m_cboVcaType.InsertString(0, "Vca");
	m_cboVcaType.InsertString(1, "Alert");
	m_cboVcaType.SetCurSel(0);

	for(int i = 0; i < 31; ++i)
	{
		CString cstrSceneId;
		cstrSceneId.Format("%d",i);
		m_cboSceneId.InsertString(i, cstrSceneId);
	}
	m_cboSceneId.SetCurSel(0);
	for(int i = 0; i < 16; ++i)
	{
		CString cstrRuleID;
		cstrRuleID.Format("%d",i);
		m_cboRuleID.InsertString(i, cstrRuleID);
	}
	m_cboRuleID.InsertString(16, "0x7fff");
	m_cboRuleID.SetCurSel(0);
}

void CLS_VcaMaskArea::UpdatePageUI()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Cls_VcaMaskArea::UpdatePageUI (%d, %d)", m_iChannelNO);
		return;
	}

	VCAMaskAreaParam tInfo = {0};
	tInfo.iBufSize = sizeof(tInfo);
	if(1 == m_cboVcaType.GetCurSel())
	{
		tInfo.iSceneId = (m_cboSceneId.GetCurSel())| (1<<16);
	}
	else
	{
		tInfo.iSceneId =m_cboSceneId.GetCurSel();
	}
	tInfo.iRuleId = m_cboRuleID.GetCurSel();

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_MASK_AREA_PARAM, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_chkEventEnable.SetCheck(tInfo.iEnable);
		m_chkShowRule.SetCheck(tInfo.iDisplayRule);
		m_cboRegColor.SetCurSel(tInfo.iColor - 1);
		memset(&m_tVCAMaskAreaParam, 0, sizeof(m_tVCAMaskAreaParam));
		m_tVCAMaskAreaParam.iAreaNum = tInfo.iAreaNum;
		for (int i = 0; i < tInfo.iAreaNum && i<VCA_MAX_MASK_AREA_REGION_NUM; i++)
		{
			m_tVCAMaskAreaParam.tAreaInfo[i].iPointNum = tInfo.tAreaInfo[i].iPointNum;
			for(int j = 0; j < tInfo.tAreaInfo[i].iPointNum && j<REGION_MAX_MASK_AREA_POINTS_NUM; j++)
			{
				m_tVCAMaskAreaParam.tAreaInfo[i].stPoints[j] = tInfo.tAreaInfo[i].stPoints[j];
			}
		}

		m_cboCurReg.SetCurSel(0);
		OnCbnSelchangeCboMaskAreaCurRegionnum();
	}

}

void CLS_VcaMaskArea::UpdateDrawFinishRegionNum()
{
	int iRegionNum = 0;
	for (int i = 0;i<VCA_MAX_MASK_AREA_REGION_NUM;i++)
	{
		if (m_tVCAMaskAreaParam.tAreaInfo[i].iPointNum > 1)
		{
			iRegionNum++;
		}
	}

	SetDlgItemInt(IDC_EDIT_MASK_AREA_REGION_NUM, iRegionNum);
}

void CLS_VcaMaskArea::OnCbnSelchangeCboMaskAreaCurRegionnum()
{
	int iRegionNo = m_cboCurReg.GetCurSel();
	int iPiontNum = m_tVCAMaskAreaParam.tAreaInfo[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_MASK_AREA_REGION_POINTNUM, iPiontNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPiontNum && i<REGION_MAX_MASK_AREA_POINTS_NUM; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)", m_tVCAMaskAreaParam.tAreaInfo[iRegionNo].stPoints[i].iX, m_tVCAMaskAreaParam.tAreaInfo[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_MASK_AREA_REGION_POINTS, cstPolygonBuf);
}

void CLS_VcaMaskArea::OnCbnSelchangeCboSceneId()
{
	UpdatePageUI();
}

void CLS_VcaMaskArea::OnCbnSelchangeCboRuleID()
{
	UpdatePageUI();
}
void CLS_VcaMaskArea::OnCbnSelchangeCbo()
{
	UpdatePageUI();
}

//NetClientDemo\Config\Events\CLS_VcaSmartMove.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_VcaSmartMove.h"
#include "../VCAEventPage.h"

// CLS_VcaSmartMove dialog

IMPLEMENT_DYNAMIC(CLS_VcaSmartMove, CDialog)

CLS_VcaSmartMove::CLS_VcaSmartMove(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaSmartMove::IDD, pParent)
{

}

CLS_VcaSmartMove::~CLS_VcaSmartMove()
{
}

void CLS_VcaSmartMove::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SMART_MOVE_TARGET_CHECK, m_cboTargetTypeCheck);
	DDX_Control(pDX, IDC_EDIT_SMART_MOVE_POINTT_NUM, m_edtPoints);
	DDX_Control(pDX, IDC_COMBO_PARAM_TYPE, m_cboParamType);
	DDX_Control(pDX, IDC_CHECK_ISVALID, m_chkIsValid);
	DDX_Control(pDX, IDC_COMBO_CUR_DETECT_NUM, m_cboCurRegionNo);
}


BEGIN_MESSAGE_MAP(CLS_VcaSmartMove, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SMART_MOVE_SET, &CLS_VcaSmartMove::OnBnClickedButtonSmartMoveSet)
	ON_BN_CLICKED(IDC_BUTTON_SMART_MOVE_DRAW, &CLS_VcaSmartMove::OnBnClickedButtonSmartMoveDraw)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_COMBO_CUR_DETECT_NUM, &CLS_VcaSmartMove::OnCbnSelchangeComboCurDetectNum)
END_MESSAGE_MAP()


// CLS_VcaSmartMove message handler
void CLS_VcaSmartMove::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_ISVALID, GetTextByLan("是否有效", "Valid"));
	SetDlgItemText(IDC_STATIC_TARGET_CHECK, GetTextByLan("区分目标类型", "TargetTypeCheck"));
	SetDlgItemText(IDC_STATIC_PARAM_TYPE, GetTextByLan("参数类型", "ParaType"));
	SetDlgItemText(IDC_STATIC_SMART_SENCITIVITY, GetTextByLan("灵敏度", "Sencivity"));
	SetDlgItemText(IDC_STATIC_SMART_ALARM_TIME, GetTextByLan("报警时间", "alarmTime"));
	SetDlgItemText(IDC_STATIC_MIN_SIZE, GetTextByLan("最小尺寸", "MinSize"));
	SetDlgItemText(IDC_STATIC_MAX_SIZE, GetTextByLan("最大尺寸", "MaxSize"));
	SetDlgItemText(IDC_STATIC_POINT_NUM, GetTextByLan("点集", "Points"));
	SetDlgItemText(IDC_STATIC_PPOINT_NUM, GetTextByLan("点集", "Points"));
	SetDlgItemText(IDC_BUTTON_SMART_MOVE_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BUTTON_SMART_MOVE_SET, GetTextByLan("设置", "Set"));

	m_cboTargetTypeCheck.ResetContent();
	m_cboTargetTypeCheck.InsertString(0, "None");
	m_cboTargetTypeCheck.InsertString(1, "Human");
	m_cboTargetTypeCheck.InsertString(2, "Vehicle");
	m_cboTargetTypeCheck.InsertString(3, "Human\\Vehicle");
	m_cboTargetTypeCheck.SetCurSel(0);

	m_cboParamType.ResetContent();
	m_cboParamType.InsertString(0, "Custom");
	m_cboParamType.InsertString(1, "Default");
	m_cboParamType.SetCurSel(1);

	m_cboCurRegionNo.ResetContent();
	for (int i=0; i<VCA_MAX_REGION_NUM; i++)
	{
		CString cstrRegionNo;
		cstrRegionNo.Format("%d",i+1);
		m_cboCurRegionNo.InsertString(i, cstrRegionNo);
	}
	m_cboCurRegionNo.SetCurSel(0);
}

void CLS_VcaSmartMove::UpdatePageUI()
{

	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCASmartMove tInfo = {0};
	tInfo.iSize = sizeof(VCASmartMove);
	tInfo.tRule.iSceneID = m_iSceneID;
	tInfo.tRule.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SMART_MOVE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_chkIsValid.SetCheck(tInfo.tRule.iValid);
		m_cboTargetTypeCheck.SetCurSel(tInfo.iTargetTypeCheck);
		m_cboParamType.SetCurSel(tInfo.iUserDefult);
		
		SetDlgItemInt(IDC_EDIT_SMART_MOVE_SENCEVITITY, tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, tInfo.iMinSize);
		SetDlgItemInt(IDC_EDIT_SMART_MAX_SIZE, tInfo.iMaxSize);
		SetDlgItemInt(IDC_EDIT_SMART_POINT_NUM, tInfo.stPoints[0].iPointNum);
		SetDlgItemInt(IDC_EDIT_ALARM_TIME, tInfo.iAlramTime);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, tInfo.iMinSize);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, tInfo.iMinSize);

		memset(&m_tVCASmartMove, 0, sizeof(m_tVCASmartMove));
		m_tVCASmartMove.iRegionNum = tInfo.iRegionNum;
		for (int i = 0; i < tInfo.iRegionNum && i<MAX_DETECT_AREA_NUM; i++)
		{
			m_tVCASmartMove.stPoints[i].iPointNum = tInfo.stPoints[i].iPointNum;
			for(int j = 0; j < tInfo.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_tVCASmartMove.stPoints[i].stPoints[j] = tInfo.stPoints[i].stPoints[j];
			}
		}

		m_cboCurRegionNo.SetCurSel(0);
		OnCbnSelchangeComboCurDetectNum();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VcaSmartMove::NetClient_VCAGetConfig[VCA_CMD_SMART_MOVE] (%d, %d), iRet(%d)", m_iLogonID, m_iChannelNO, iRet);
	}
}

void CLS_VcaSmartMove::OnBnClickedButtonSmartMoveSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VcaSmartMove::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VCASmartMove tInfo = {0};
	tInfo.tRule.iSceneID = m_iSceneID;
	tInfo.tRule.iRuleID = m_iRuleID;
	tInfo.tRule.iValid = m_chkIsValid.GetCheck();
	tInfo.iMaxSize = GetDlgItemInt(IDC_EDIT_SMART_MAX_SIZE);
	tInfo.iMinSize = GetDlgItemInt(IDC_EDIT_MIN_SIZE);
	tInfo.iAlramTime = GetDlgItemInt(IDC_EDIT_ALARM_TIME);
	tInfo.iUserDefult = m_cboParamType.GetCurSel();
	tInfo.iTargetTypeCheck = m_cboTargetTypeCheck.GetCurSel();
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDIT_SMART_MOVE_SENCEVITITY);
	tInfo.iRegionNum = m_cboCurRegionNo.GetCurSel() + 1;

	for(int i=0;i<MAX_DETECT_AREA_NUM && i< tInfo.iRegionNum;i++)
	{
		tInfo.stPoints[i].iPointNum = m_tVCASmartMove.stPoints[i].iPointNum;
		for (int j=0;j<tInfo.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX;j++)
		{
			tInfo.stPoints[i].stPoints[j] = m_tVCASmartMove.stPoints[i].stPoints[j];
		}
	}



	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SMART_MOVE, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VcaSmartMove::NetClient_VCASetConfig[VCA_CMD_SMART_MOVE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VcaSmartMove::NetClient_VCASetConfig[VCA_CMD_SMART_MOVE] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaSmartMove::OnBnClickedButtonSmartMoveDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, REGION_MAX_POINTS_NUM);
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
			m_edtPoints.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_SMART_POINT_NUM, iPointNum);
		}
		else
		{
			m_edtPoints.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_SMART_POINT_NUM, 0);
		}

		int iRegionNo = m_cboCurRegionNo.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_SMART_POINT_NUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_edtPoints.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_tVCASmartMove.stPoints[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_tVCASmartMove.stPoints[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tVCASmartMove.stPoints[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
		}

	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VcaSmartMove::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

BOOL CLS_VcaSmartMove::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();
	UpdateUIText();
	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_VcaSmartMove::OnCbnSelchangeComboCurDetectNum()
{
	int iRegionNo = m_cboCurRegionNo.GetCurSel();
	int iPiontNum = m_tVCASmartMove.stPoints[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_SMART_POINT_NUM, iPiontNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPiontNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)", m_tVCASmartMove.stPoints[iRegionNo].stPoints[i].iX, m_tVCASmartMove.stPoints[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_SMART_MOVE_POINTT_NUM, cstPolygonBuf);
}

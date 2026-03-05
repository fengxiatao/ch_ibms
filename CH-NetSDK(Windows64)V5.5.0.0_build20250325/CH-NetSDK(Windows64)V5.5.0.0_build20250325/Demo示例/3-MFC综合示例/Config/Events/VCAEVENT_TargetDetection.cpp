// VCAEVENT_TargetDetection.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_TargetDetection.h"
#include "../VCAEventPage.h"
#define  REGION_MAX_POINTS_NUM		10	//The maximum number of points in a detection area
// VCAEVENT_TargetDetection 对话框

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_TargetDetection, CDialog)

CLS_VCAEVENT_TargetDetection::CLS_VCAEVENT_TargetDetection(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_TargetDetection::IDD, pParent)
{

}

CLS_VCAEVENT_TargetDetection::~CLS_VCAEVENT_TargetDetection()
{
}

void CLS_VCAEVENT_TargetDetection::OnLanguageChanged()
{

}

void CLS_VCAEVENT_TargetDetection::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_EVENT_ENABLE, m_chkEventEnable);
	DDX_Control(pDX, IDC_COMBO_TARGETDEV_TYPE, m_cboDevType);
	DDX_Control(pDX, IDC_SLIDER_IDC_STATIC_SENSITIVITY, m_sldSensitive);
	DDX_Control(pDX, IDC_COMBO_CURRENT_AREA, m_cboCurRegionNo);
	DDX_Control(pDX, IDC_EDIT_POINT_SET, m_editRegionPoins);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_TargetDetection, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_TARGET_SET, &CLS_VCAEVENT_TargetDetection::OnBnClickedButtonTargetSet)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_IDC_STATIC_SENSITIVITY, &CLS_VCAEVENT_TargetDetection::OnNMCustomdrawSliderIdcStaticSensitivity)
	ON_CBN_SELCHANGE(IDC_COMBO_CURRENT_AREA, &CLS_VCAEVENT_TargetDetection::OnCbnSelchangeComboCurrentArea)
	ON_BN_CLICKED(IDC_BUTTON_DRAW_SET, &CLS_VCAEVENT_TargetDetection::OnBnClickedButtonDrawSet)
END_MESSAGE_MAP()

BOOL CLS_VCAEVENT_TargetDetection::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();
	UpdateUIText();


	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_VCAEVENT_TargetDetection::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_STATIC_TARGET_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STATIC_TARGET_REGION_NUM, GetTextByLan("已绘制区域个数", "The number of areas that have been drawn"));
	SetDlgItemText(IDC_STATIC_DEVICE_TYPE, GetTextByLan("设备类型", "Device Type"));
	SetDlgItemText(IDC_STATIC_CURRENT_TIME, GetTextByLan("目标出现的时间", "Target Occurrence Time"));
	SetDlgItemText(IDC_STATIC_SENSITIVITY, GetTextByLan("灵敏度", "Sensitivity"));
	SetDlgItemText(IDC_STATIC_DEVICE_TYPE, GetTextByLan("设备类型", "Device type"));
	SetDlgItemText(IDC_STATIC_POINT_SET, GetTextByLan("点集", "Point Set"));
	SetDlgItemText(IDC_STATIC_CURRENT_AREA, GetTextByLan("当前检测区域号", "Current detection area number"));

	m_cboCurRegionNo.ResetContent();
	for (int i=0; i<MAX_DETECT_TARGET_AREA_NUM; i++)
	{
		CString cstrRegionNo;
		cstrRegionNo.Format("%d",i+1);
		m_cboCurRegionNo.InsertString(i, cstrRegionNo);
	}
	m_cboCurRegionNo.SetCurSel(0);

	m_cboDevType.ResetContent();
	m_cboDevType.InsertString(0, "IPC");
	m_cboDevType.InsertString(1, "NVR");
	m_cboDevType.SetCurSel(0);
	
	m_sldSensitive.SetRange(0,100);
}
void CLS_VCAEVENT_TargetDetection::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UI_UpdatePage();
	}

}

void CLS_VCAEVENT_TargetDetection::UI_UpdatePage()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAParaTarget tInfo = {0};
	tInfo.iDevType = m_cboDevType.GetCurSel();
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_TARGET_PARA, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboDevType.SetCurSel(tInfo.iDevType);
		m_chkEventEnable.SetCheck(tInfo.iValid);
		m_sldSensitive.SetPos(tInfo.iSensitivity);
		SetDlgItemInt(IDC_STATIC_TARGET_SENSITIVITY, m_sldSensitive.GetPos());
		SetDlgItemInt(IDC_EDIT_TARGET_REGION_NUM, tInfo.iRegionNum);

		memset(&m_tTargetDetection, 0, sizeof(m_tTargetDetection));
		m_tTargetDetection.iRegionNum = tInfo.iRegionNum;
		for (int i = 0; i < tInfo.iRegionNum && i<MAX_DETECT_AREA_NUM; i++)
		{
			m_tTargetDetection.stPoints[i].iPointNum = tInfo.stPoints[i].iPointNum;
			for(int j =0; j < tInfo.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX; j++)
			{
				m_tTargetDetection.stPoints[i].stPoints[j] = tInfo.stPoints[i].stPoints[j];
			}
		}
		m_cboCurRegionNo.SetCurSel(0);
		OnCbnSelchangeComboCurrentArea();

	}
}


void CLS_VCAEVENT_TargetDetection::OnBnClickedButtonTargetSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_TargetDetection::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VCAParaTarget tInfo = {0};
	tInfo.iDevType = m_cboDevType.GetCurSel();
	tInfo.iSceneID = m_iSceneID;
	tInfo.iRuleID = m_iRuleID;
	tInfo.iValid = m_chkEventEnable.GetCheck();
	tInfo.iRegionNum = GetDlgItemInt(IDC_EDIT_TARGET_REGION_NUM);
	tInfo.iSensitivity = m_sldSensitive.GetPos();

	for(int i=0;i<MAX_DETECT_TARGET_AREA_NUM && i< tInfo.iRegionNum;i++)
	{
		tInfo.stPoints[i].iPointNum = m_tTargetDetection.stPoints[i].iPointNum;
		for (int j=0;j<tInfo.stPoints[i].iPointNum && j<VCA_MAX_POLYGON_POINT_NUMEX;j++)
		{
			tInfo.stPoints[i].stPoints[j] = m_tTargetDetection.stPoints[i].stPoints[j];
		}
	}

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_TARGET_PARA, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_TargetDetection::NetClient_VCASetConfig[VCA_CMD_TARGET_PARA] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_TargetDetection::NetClient_VCASetConfig[VCA_CMD_TARGET_PARA] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_TargetDetection::OnNMCustomdrawSliderIdcStaticSensitivity(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_TARGET_SENSITIVITY, m_sldSensitive.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_TargetDetection::OnCbnSelchangeComboCurrentArea()
{
	int iRegionNo = m_cboCurRegionNo.GetCurSel();
	int iPiontNum = m_tTargetDetection.stPoints[iRegionNo].iPointNum;
	SetDlgItemInt(IDC_EDIT_TAR_REGION_POINTNUM, iPiontNum);
	CString cstPolygonBuf;
	for(int i = 0; i < iPiontNum && i<VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstPolygonBuf.AppendFormat("(%d, %d)", m_tTargetDetection.stPoints[iRegionNo].stPoints[i].iX, m_tTargetDetection.stPoints[iRegionNo].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDIT_POINT_SET, cstPolygonBuf);
}

void CLS_VCAEVENT_TargetDetection::UpdateDrawFinishRegionNum()
{
	int iRegionNum = 0;
	for (int i = 0;i<MAX_DETECT_TARGET_AREA_NUM;i++)
	{
		if (m_tTargetDetection.stPoints[i].iPointNum > 1)
		{
			iRegionNum++;
		}
	}

	SetDlgItemInt(IDC_EDIT_TARGET_REGION_NUM, iRegionNum);
	
}

void CLS_VCAEVENT_TargetDetection::OnBnClickedButtonDrawSet()
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
			m_editRegionPoins.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_TAR_REGION_POINTNUM, iPointNum);
		}
		else
		{
			m_editRegionPoins.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_TAR_REGION_POINTNUM, 0);
		}

		int iRegionNo = m_cboCurRegionNo.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_TAR_REGION_POINTNUM);
		vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		m_tTargetDetection.stPoints[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i<VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_tTargetDetection.stPoints[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tTargetDetection.stPoints[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
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
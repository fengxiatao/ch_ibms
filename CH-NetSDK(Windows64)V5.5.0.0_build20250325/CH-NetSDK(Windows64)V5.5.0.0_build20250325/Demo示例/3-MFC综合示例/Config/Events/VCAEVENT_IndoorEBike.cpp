// E:\01code\sdk_trunk\Demo\NetClientDemo\Config\Events\CLS_VCAEVENT_IndoorEBike.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_IndoorEBike.h"
#define  MIN_MIN_SIZE		0		//minimum size minimum

// CLS_VCAEVENT_IndoorEBike dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_IndoorEBike, CDialog)

CLS_VCAEVENT_IndoorEBike::CLS_VCAEVENT_IndoorEBike(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_IndoorEBike::IDD, pParent)
	, m_iSensitivity(0)
	, m_iMinsize(0)
	, m_iMaxsize(0)
	, m_iAlarmTime(0)
	, m_iDisplayRule(FALSE)
	, m_bDisplayTarget(FALSE)
	, m_bEventValid(FALSE)
	, m_iRegionNum(0)
{

}

CLS_VCAEVENT_IndoorEBike::~CLS_VCAEVENT_IndoorEBike()
{
}

void CLS_VCAEVENT_IndoorEBike::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_REGION, m_cboRegion);
	DDX_Text(pDX, IDC_EDT_DREDGE_SENSITIVITY, m_iSensitivity);
	DDV_MinMaxInt(pDX, m_iSensitivity, 0, 100);
	DDX_Text(pDX, IDC_EDT_DREDGE_MINSIZE, m_iMinsize);
	DDV_MinMaxInt(pDX, m_iMinsize, 0, 100);
	DDX_Text(pDX, IDC_EDT_DREDGE_MAXSIZE, m_iMaxsize);
	DDV_MinMaxInt(pDX, m_iMaxsize, 8, 100);
	DDX_Text(pDX, IDC_EDT_DREDGE_ALARMTIME, m_iAlarmTime);
	DDV_MinMaxInt(pDX, m_iAlarmTime, 2, 3600);
	DDX_Check(pDX, IDC_CHK_DREDGE_RULE_DISPLAY, m_iDisplayRule);
	DDX_Check(pDX, IDC_CHK_DREDGE_DISPLAY_TARGET, m_bDisplayTarget);
	DDX_Check(pDX, IDC_CHK_EBIKE_RULE_VALID, m_bEventValid);
	DDX_Text(pDX, IDC_EDT_EBIKE_REGIONNUM, m_iRegionNum);
	DDV_MinMaxInt(pDX, m_iRegionNum, 0, 8);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_IndoorEBike, CDialog)
	ON_BN_CLICKED(IDC_BTN_DREDGE_POINTS_DRAW, &CLS_VCAEVENT_IndoorEBike::OnBnClickedBtnDredgePointsDraw)
	ON_BN_CLICKED(IDC_BTN_EBIKE_SET, &CLS_VCAEVENT_IndoorEBike::OnBnClickedBtnEbikeSet)
	ON_CBN_SELCHANGE(IDC_COMBO_REGION, &CLS_VCAEVENT_IndoorEBike::OnCbnSelchangeComboRegion)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// CLS_VCAEVENT_IndoorEBike message handlers

BOOL CLS_VCAEVENT_IndoorEBike::OnInitDialog()
{
	CDialog::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialog();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}
void CLS_VCAEVENT_IndoorEBike::UI_UpdateDialog()
{

	SetDlgItemTextEx(IDC_CHK_DREDGE_RULE_DISPLAY, IDS_VCA_DISPLAY_RULE);
	SetDlgItemTextEx(IDC_CHK_DREDGE_DISPLAY_STATE, IDS_VCA_DISPLAY_ALARMSTATUS);
	SetDlgItemText(IDC_CHK_EBIKE_RULE_VALID, GetTextByLan("事件是否有效", "Event Valid"));
	SetDlgItemTextEx(IDC_STC_DREDGE_MINSIZE, IDS_VCA_MINSIZE);
	SetDlgItemTextEx(IDC_STC_DREDGE_MAXSIZE, IDS_VCA_MAXSIZE);
	SetDlgItemTextEx(IDC_STC_DREDGE_POINTNUM, IDS_VCA_POINTNUM);
	SetDlgItemTextEx(IDC_STC_DREDGE_POINTS, IDS_VCA_POINTS);
	SetDlgItemTextEx(IDC_STC_DREDGE_ALARMTIME, IDS_VCA_ALARMTIME);
	SetDlgItemTextEx(IDC_STC_DREDGE_SENSITIVITY, IDS_VCA_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_DREDGE_SENSITIVITY_RANGE, IDS_STC_FIGHT_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_DREDGE_MINSIZE_RANGE,IDS_VCA_MINSIZE_RANGE);
	SetDlgItemTextEx(IDC_STC_DREDGE_MAXSIZE_RANGE,IDS_VCA_MAXSIZE_RANGE);
	SetDlgItemTextEx(IDC_BTN_DREDGE_POINTS_DRAW, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_EBIKE_SET, IDS_SET);
	SetDlgItemText(IDC_STC_REGIONNUM, GetTextByLan("区域数量", "Region Number"));

	m_cboRegion.ResetContent();
	for (int i=0; i<MAX_CHECK_RULE_REGION_NUM; i++)
	{
		CString str;
		str.Format("%d",i);
		m_cboRegion.AddString(str);

	}
}
void CLS_VCAEVENT_IndoorEBike::OnLanguageChanged()
{
	UI_UpdateDialog();
}

void CLS_VCAEVENT_IndoorEBike::OnBnClickedBtnDredgePointsDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_perimeter);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection);
	if (-1 == iSetRet)
	{
		return;
	}

	if (IDOK == m_pDlgVideoView->DoModal())
	{
		GetDlgItem(IDC_EDT_DREDGE_POINTS)->SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDT_DREDGE_POINTNUM, iPointNum);

		int iCurIndex = m_cboRegion.GetCurSel();
		if(iCurIndex >=0 && iCurIndex < MAX_CHECK_RULE_REGION_NUM)
		{
			m_tIndoorEBike.tPolygon[iCurIndex].iPointNum = iPointNum;
			GetPolyFromString(cPointBuf,iPointNum, m_tIndoorEBike.tPolygon[iCurIndex]);
		}

	}

	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}
void CLS_VCAEVENT_IndoorEBike::UI_UpdatePage()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	memset(&m_tIndoorEBike,0x00,sizeof(m_tIndoorEBike));
	m_tIndoorEBike.iSize = sizeof(m_tIndoorEBike);
	m_tIndoorEBike.tRule.iRuleID = m_iRuleID;
	m_tIndoorEBike.tRule.iSceneID = m_iSceneID;
	m_tIndoorEBike.tRule.iValid = m_bEventValid;
	int iCmd = VCA_CMD_INDOOREBIKE;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, iCmd, m_iChannelNO, &m_tIndoorEBike, sizeof(m_tIndoorEBike));
	if (RET_SUCCESS == iRet)
	{
		m_iDisplayRule= m_tIndoorEBike.iDisplayRule;
		m_bDisplayTarget= m_tIndoorEBike.iDisplayTarget;
		m_iMinsize= m_tIndoorEBike.iMinSize;
		m_iMaxsize= m_tIndoorEBike.iMaxSize;
		m_iSensitivity= m_tIndoorEBike.iSensitivity;
		m_iAlarmTime= m_tIndoorEBike.iAlarmTime;
		m_iRegionNum= m_tIndoorEBike.iRegionNum;
        if(m_iRegionNum > MAX_CHECK_RULE_REGION_NUM)
		{
			m_iRegionNum = 0;
		}

		m_cboRegion.SetCurSel(0);
		OnCbnSelchangeComboRegion();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_VCAGetConfig(%d,%d,%d)error = %d",m_iLogonID, iCmd,m_iChannelNO,GetLastError());
	}

	UpdateData(FALSE);
}

void CLS_VCAEVENT_IndoorEBike::OnBnClickedBtnEbikeSet()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}
	UpdateData(TRUE);
	
	m_tIndoorEBike.iSize = sizeof(VCAIndoorEBike);

	m_tIndoorEBike.tRule.iRuleID = m_iRuleID;
	m_tIndoorEBike.tRule.iSceneID = m_iSceneID;
	m_tIndoorEBike.tRule.iValid = m_bEventValid;

	m_tIndoorEBike.iDisplayRule = m_iDisplayRule;
	m_tIndoorEBike.iDisplayTarget = m_bDisplayTarget;
	m_tIndoorEBike.iMinSize = m_iMinsize;
	m_tIndoorEBike.iMaxSize = m_iMaxsize;
	m_tIndoorEBike.iSensitivity = m_iSensitivity;
	m_tIndoorEBike.iAlarmTime = m_iAlarmTime;
	m_tIndoorEBike.iRegionNum = m_iRegionNum;

	int iCmd = VCA_CMD_INDOOREBIKE;
	int iRet = NetClient_VCASetConfig(m_iLogonID, iCmd, m_iChannelNO, &m_tIndoorEBike, sizeof(m_tIndoorEBike));
	if (iRet >= 0)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_VCASetConfig(%d,%d,%d)error = %d",m_iLogonID, iCmd,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_VCASetConfig(%d,%d,%d)error = %d",m_iLogonID, iCmd,m_iChannelNO);
	}

}

void CLS_VCAEVENT_IndoorEBike::OnCbnSelchangeComboRegion()
{
	// TODO: Add your control notification handler code here

	int iCurIndex = m_cboRegion.GetCurSel();
	if(iCurIndex >=0 && iCurIndex < MAX_CHECK_RULE_REGION_NUM)
	{
		CString strPointArray;
		CString strPoint[VCA_MAX_POLYGON_POINT_NUM];
		for(int i = 0; i < m_tIndoorEBike.tPolygon[iCurIndex].iPointNum && i < MAX_POLYGON_POINT_NUM-6; i++)
		{
			strPoint[i].Format("(%d,%d)", m_tIndoorEBike.tPolygon[iCurIndex].stPoints[i].iX, m_tIndoorEBike.tPolygon[iCurIndex].stPoints[i].iY);
			strPointArray += strPoint[i];
		}
		SetDlgItemText(IDC_EDT_DREDGE_POINTS,strPointArray);
		SetDlgItemInt(IDC_EDT_DREDGE_POINTNUM, m_tIndoorEBike.tPolygon[iCurIndex].iPointNum);
	}


}

void CLS_VCAEVENT_IndoorEBike::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	// TODO: Add your message handler code here
	if (bShow)
	{
		UI_UpdatePage();
	}
	
}

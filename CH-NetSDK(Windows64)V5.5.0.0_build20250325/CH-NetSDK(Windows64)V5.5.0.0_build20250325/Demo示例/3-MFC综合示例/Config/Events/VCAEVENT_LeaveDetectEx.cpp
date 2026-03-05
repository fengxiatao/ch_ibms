// VCAEVENT_LeaveDetect.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_LeaveDetectEx.h"


// CLS_VCAEVENT_LeaveDetectEx dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_LeaveDetectEx, CDialog)

CLS_VCAEVENT_LeaveDetectEx::CLS_VCAEVENT_LeaveDetectEx(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_LeaveDetectEx::IDD, pParent)
	, m_iDutyNum(1)
	, m_iSensitivity(2)
	, m_iMin(3)
	, m_iMax(15)
{

}

CLS_VCAEVENT_LeaveDetectEx::~CLS_VCAEVENT_LeaveDetectEx()
{
}

void CLS_VCAEVENT_LeaveDetectEx::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_EVENT_LEAVEDETECT_RULE_DISPLAY, m_chkDisplayRule);
	DDX_Control(pDX, IDC_CHECK_EVENT_LEAVEDETECT_DIAPALYSTATE, m_chkDiaplayState);
	DDX_Control(pDX, IDC_EDIT_LEAVEDETECT_AREA_NUM, m_edtAreaNum);
	DDX_Control(pDX, IDC_EDIT_LeaveAlarmTime, m_edtLeaveAlarmTime);
	DDX_Control(pDX, IDC_EDIT_RuturnClearAlarmTime, m_edtRuturnClearAlarmTime);
	DDX_Control(pDX, IDC_COMBO_LEAVEDETECT, m_cboAreaColor);
	DDX_Control(pDX, IDC_COMBO_LEAVEDETECT_ALARMCOLOR, m_cboAlarmAreaColor);
	DDX_Control(pDX, IDC_CHECK_EVENT_VALID, m_chkEventValid);
	DDX_Control(pDX, IDC_COMBO_DISPLAYTARGET, m_cboDisplayTarget);
	DDX_Text(pDX, IDC_EDIT_DUTYNUM, m_iDutyNum);
	DDV_MinMaxInt(pDX, m_iDutyNum, 1, 2);
	DDX_Text(pDX, IDC_EDT_DREDGE_SENSITIVITY, m_iSensitivity);
	DDV_MinMaxInt(pDX, m_iSensitivity, 0, 5);
	DDX_Text(pDX, IDC_EDT_DREDGE_MINSIZE, m_iMin);
	DDV_MinMaxInt(pDX, m_iMin, 1, 50);
	DDX_Text(pDX, IDC_EDT_DREDGE_MAXSIZE, m_iMax);
	DDV_MinMaxInt(pDX, m_iMax, 5, 100);
	DDX_Control(pDX, IDC_COMBO_REGION, m_cboRegionNum);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_LeaveDetectEx, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_LEAVEDETECT_SET, &CLS_VCAEVENT_LeaveDetectEx::OnBnClickedButtonLeavedetectSet)
	ON_CBN_SELCHANGE(IDC_COMBO_REGION, &CLS_VCAEVENT_LeaveDetectEx::OnCbnSelchangeComboRegion)
	ON_BN_CLICKED(IDC_BTN_DREDGE_POINTS_DRAW, &CLS_VCAEVENT_LeaveDetectEx::OnBnClickedBtnDredgePointsDraw)
END_MESSAGE_MAP()


// CLS_VCAEVENT_LeaveDetectEx message handlers

BOOL CLS_VCAEVENT_LeaveDetectEx::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	m_edtLeaveAlarmTime.SetLimitText(16);
	m_edtRuturnClearAlarmTime.SetLimitText(16);
	m_edtAreaNum.SetLimitText(16);
	SetDlgItemInt(IDC_EDIT_LEAVEDETECT_AREA_NUM, 1);
	SetDlgItemInt(IDC_EDIT_LeaveAlarmTime, 120);
	SetDlgItemInt(IDC_EDIT_RuturnClearAlarmTime, 15);
	m_cboAlarmAreaColor.SetCurSel(0);
	m_cboAreaColor.SetCurSel(0);

	UpdateUIText();

	m_cboRegionNum.ResetContent();
	for (int i=0; i<MAX_RULE_REGION_NUM; i++)
	{
		CString str;
		str.Format("%d",i);
		m_cboRegionNum.AddString(str);

	}

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_VCAEVENT_LeaveDetectEx::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	CleanText();
	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_LeaveDetectEx::OnLanguageChanged()
{
	UpdateUIText();
	UpdatePageUI();
}
void CLS_VCAEVENT_LeaveDetectEx::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","Invalid logon id(%d)", _iLogonID);
		return;
	}

	switch (_iParaType)
	{
	case  PARA_VCA_LEAVEDETECTEX:
		{
			AddLog(LOG_TYPE_SUCC,"","[CLS_VCAEVENT_LeaveDetectEx][OnParamChangeNotify] logon id(%d)", _iLogonID);
			UpdatePageUI();
		}
		break;
	default:
		break;
	}
}

void CLS_VCAEVENT_LeaveDetectEx::UpdateUIText()
{
	SetDlgItemTextEx(IDC_CHECK_EVENT_LEAVEDETECT_RULE_DISPLAY, IDS_VCA_DISPLAY_RULE);
	SetDlgItemTextEx(IDC_CHECK_EVENT_LEAVEDETECT_DIAPALYSTATE, IDS_VCA_DISPLAY_ALARMSTATUS);
	SetDlgItemTextEx(IDC_STATI_EVENT_LEAVEDETECT_REGINNUM, IDS_VCA_EVENT_LEAVEDETECT_AREANUM);
	SetDlgItemTextEx(IDC_BUTTON_LEAVEDETECT_SET, IDS_VCA_EVENT_LEAVEDETECT_SET);
	SetDlgItemTextEx(IDC_STATIC_LeaveAlarmTime, IDS_VCA_EVENT_LEAVEDETECT_LEAVEALARMTIME);
	SetDlgItemTextEx(IDC_STATIC_RuturnClearAlarmTime, IDS_VCA_EVENT_LEAVEDETECT_RETURNCLEARTIME);
	SetDlgItemTextEx(IDC_STATIC_LEAVE_TIME, IDS_VCA_EVENT_LEAVEDETECT_LEAVE_TIME);
	SetDlgItemTextEx(IDC_STATIC_RETURN_TIME, IDS_VCA_EVENT_LEAVEDETECT_RETURN_TIME);
	SetDlgItemTextEx(IDC_STATIC_LEAVEDETECT_COLOR, IDS_VCA_NOALARM_COLOR);
	SetDlgItemTextEx(IDC_STATIC_ALARM_COLOR, IDS_VCA_ALARM_COLOR);



	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), 
								GetTextEx(IDS_VCA_COL_YELLOW), GetTextEx(IDS_VCA_COL_BLUE), 
								GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), 
								GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboAlarmAreaColor.ResetContent();
	m_cboAreaColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboAlarmAreaColor.InsertString(i, strColor[i]);
		m_cboAreaColor.InsertString(i, strColor[i]);
	}
}

void CLS_VCAEVENT_LeaveDetectEx::CleanText()
{
	m_chkDisplayRule.SetCheck(0);
	m_chkDiaplayState.SetCheck(0);
	m_edtLeaveAlarmTime.Clear();
	m_edtRuturnClearAlarmTime.Clear();
	m_edtAreaNum.Clear();
	
	SetDlgItemInt(IDC_EDIT_LEAVEDETECT_AREA_NUM, 1);
	SetDlgItemText(IDC_EDIT_LeaveAlarmTime, "");
	SetDlgItemText(IDC_EDIT_RuturnClearAlarmTime, "");
	m_cboAreaColor.SetCurSel(-1);
	m_cboAlarmAreaColor.SetCurSel(-1);
}

void CLS_VCAEVENT_LeaveDetectEx::OnBnClickedButtonLeavedetectSet()
{
		// TODO: Add your control notification handler code here
		if (m_iLogonID == -1 || m_iChannelNO == -1)
		{
			return;
		}

		UpdateData(TRUE);

	    VCALeaveDetectEx &tLeaveDetect = m_tLeaveDetect;
		tLeaveDetect.tRule.iRuleID = m_iRuleID;
		tLeaveDetect.tRule.iSceneID = m_iSceneID; 
		tLeaveDetect.tRule.iValid = m_chkEventValid.GetCheck();
		
		
		tLeaveDetect.tDisplayParam.iDisplayRule = m_chkDisplayRule.GetCheck();
		tLeaveDetect.tDisplayParam.iDisplayStat = m_chkDiaplayState.GetCheck();
		tLeaveDetect.tDisplayParam.iColor = m_cboAreaColor.GetCurSel()+1;
		tLeaveDetect.tDisplayParam.iAlarmColor = m_cboAlarmAreaColor.GetCurSel()+1;

		tLeaveDetect.iDutyNum = m_iDutyNum;
		tLeaveDetect.iMinSize = m_iMin;
		tLeaveDetect.iMaxSize = m_iMax;
		tLeaveDetect.iSensitivity = m_iSensitivity;
		tLeaveDetect.iDisplayTarget = m_cboDisplayTarget.GetCurSel();

		int iLeaveAlarmTime = GetDlgItemInt(IDC_EDIT_LeaveAlarmTime);
		tLeaveDetect.iLeaveAlarmTime = iLeaveAlarmTime;
		int iRuturnClearAlarmTime = GetDlgItemInt(IDC_EDIT_RuturnClearAlarmTime);
		tLeaveDetect.iRuturnClearAlarmTime = iRuturnClearAlarmTime;
		int iAreaNum = GetDlgItemInt(IDC_EDIT_LEAVEDETECT_AREA_NUM);
		tLeaveDetect.iAreaNum = iAreaNum;


		int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_RULE14_LEAVE_DETECTEX, m_iChannelNO, &tLeaveDetect, sizeof(VCALeaveDetectEx));
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "[CVCAEventPage::OnBnClickedButtonLeavedetectSet]NetClient_VCASetConfig leave detect Set error = %d", GetLastError());
		}
		else
		{
			AddLog(LOG_TYPE_SUCC, "", "[CVCAEventPage::OnBnClickedButtonLeavedetectSet]NetClient_VCASetConfig leave detect set success!");
			
		}

}

void CLS_VCAEVENT_LeaveDetectEx::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	
	memset(&m_tLeaveDetect, 0, sizeof(VCALeaveDetectEx));

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RULE14_LEAVE_DETECTEX, m_iChannelNO, &m_tLeaveDetect, sizeof(VCALeaveDetectEx));
	if (iRet >= RET_SUCCESS)
	{

		m_chkEventValid.SetCheck(m_tLeaveDetect.tRule.iValid);
		m_cboAlarmAreaColor.SetCurSel(m_tLeaveDetect.tDisplayParam.iAlarmColor-1);
		m_cboAreaColor.SetCurSel(m_tLeaveDetect.tDisplayParam.iColor-1);
		m_chkDisplayRule.SetCheck(m_tLeaveDetect.tDisplayParam.iDisplayRule);
		m_chkDiaplayState.SetCheck(m_tLeaveDetect.tDisplayParam.iDisplayStat);

		SetDlgItemInt(IDC_EDIT_LeaveAlarmTime, m_tLeaveDetect.iLeaveAlarmTime);
		SetDlgItemInt(IDC_EDIT_RuturnClearAlarmTime, m_tLeaveDetect.iRuturnClearAlarmTime);
		SetDlgItemInt(IDC_EDIT_LEAVEDETECT_AREA_NUM, m_tLeaveDetect.iAreaNum);

		m_iDutyNum = m_tLeaveDetect.iDutyNum;
		m_iMin = m_tLeaveDetect.iMinSize;
		m_iMax = m_tLeaveDetect.iMaxSize;
		m_iSensitivity = m_tLeaveDetect.iSensitivity;
		m_cboDisplayTarget.SetCurSel(m_tLeaveDetect.iDisplayTarget);

		m_cboRegionNum.SetCurSel(0);
		OnCbnSelchangeComboRegion();
	

		UpdateData(FALSE);
	}
	else
	{
		// TODO: Nothing
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_LeaveDetectEx::UpdatePageUI]NetClient_VCAGetConfig fail,Set error = %d", GetLastError());
	}
}

void CLS_VCAEVENT_LeaveDetectEx::OnCbnSelchangeComboRegion()
{
	// TODO: Add your control notification handler code here
	int iCurIndex = m_cboRegionNum.GetCurSel();
	if(iCurIndex >=0 && iCurIndex < MAX_REGION_NUM)
	{
		CString strPointArray;
		CString strPoint[VCA_MAX_POLYGON_POINT_NUM];
		for(int i = 0; i < m_tLeaveDetect.tPolygon[iCurIndex].iPointNum && i < MAX_POLYGON_POINT_NUM-6; i++)
		{
			strPoint[i].Format("(%d,%d)", m_tLeaveDetect.tPolygon[iCurIndex].stPoints[i].iX, m_tLeaveDetect.tPolygon[iCurIndex].stPoints[i].iY);
			strPointArray += strPoint[i];
		}
		SetDlgItemText(IDC_EDT_DREDGE_POINTS,strPointArray);
		SetDlgItemInt(IDC_EDT_DREDGE_POINTNUM, m_tLeaveDetect.tPolygon[iCurIndex].iPointNum);
	}
}

void CLS_VCAEVENT_LeaveDetectEx::OnBnClickedBtnDredgePointsDraw()
{
	// TODO: Add your control notification handler code here
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
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return;
	}

	if (IDOK == m_pDlgVideoView->DoModal())
	{
		GetDlgItem(IDC_EDT_DREDGE_POINTS)->SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDT_DREDGE_POINTNUM, iPointNum);

		int iCurIndex = m_cboRegionNum.GetCurSel();
		if(iCurIndex >=0 && iCurIndex < MAX_CHECK_RULE_REGION_NUM)
		{
			m_tLeaveDetect.tPolygon[iCurIndex].iPointNum = iPointNum;
			GetPolyFromString(cPointBuf,iPointNum, m_tLeaveDetect.tPolygon[iCurIndex]);
		}

	}

	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

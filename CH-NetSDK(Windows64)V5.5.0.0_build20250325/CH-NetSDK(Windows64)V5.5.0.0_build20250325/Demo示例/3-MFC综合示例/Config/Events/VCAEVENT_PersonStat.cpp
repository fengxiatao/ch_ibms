// VCAEVENT_PersonStat.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_PersonStat.h"

#define POINT_NUM_MIN			2			//Minimum point
#define POINT_NUM_MAX			15			//Maximum number of points
#define LEN_1					1
#define LEN_2					2
#define LEN_3					3
#define MIN_SIZE_FROM			0
#define MIN_SIZE_TO				100
#define MAX_SIZE_FROM			0
#define	MAX_SIZE_TO				100


// CLS_VCAEVENT_PersonStat dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_PersonStat, CDialog)

CLS_VCAEVENT_PersonStat::CLS_VCAEVENT_PersonStat(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_PersonStat::IDD, pParent)
	, m_iTimeout(0)
{

}

CLS_VCAEVENT_PersonStat::~CLS_VCAEVENT_PersonStat()
{
}

void CLS_VCAEVENT_PersonStat::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHK_PER_STAT_EVENT_VALID, m_chkEventValid);
	DDX_Control(pDX, IDC_CHK_PER_STAT_SHOW_ALARM_RULE, m_chkAlarmRule);
	DDX_Control(pDX, IDC_CHK_PER_STAT_SHOW_ALARM_STAT, m_chkAlarmStat);
	DDX_Control(pDX, IDC_CHK_PER_STAT_SHOW_TARGET_BOX, m_chkTargetBox);
	DDX_Control(pDX, IDC_CBO_PER_STAT_DETECT_STYLE, m_cboDetectStyle);
	DDX_Control(pDX, IDC_CBO_PER_STAT_DETECT_MODE, m_cboDetectMode);
	DDX_Control(pDX, IDC_CBO_PER_STAT_ALARM_COLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_CBO_PER_STAT_UNALARM_COLOR, m_cboUnalarmColor);
	DDX_Control(pDX, IDC_CBO_PER_STAT_POINT_NUM_IN_POLYGON, m_cboPolygonPointNum);
	DDX_Control(pDX, IDC_EDT_PER_STAT_POLYGON_AREA, m_edtPolygonArea);
	DDX_Control(pDX, IDC_EDT_PER_STAT_LINE_AREA, m_edtLineArea);
	DDX_Control(pDX, IDC_EDT_PER_STAT_SENSITIVITY, m_edtSensitivity);
	DDX_Control(pDX, IDC_EDT_PER_STAT_AIM_MIN_SIZE, m_edtMinSize);
	DDX_Control(pDX, IDC_EDT_PER_STAT_AIM_MAX_SIZE, m_edtMaxSize);
	DDX_Text(pDX, IDC_EDIT1, m_iTimeout);
	DDV_MinMaxInt(pDX, m_iTimeout, 0, 3600);
	DDX_Control(pDX, IDC_COMBO_DISPLAY_RULE, m_cboDisplayRule);
	DDX_Control(pDX, IDC_COMBO_DISPLAY_INFO, m_cboDisplayInfo);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_PersonStat, CDialog)
	ON_BN_CLICKED(IDC_BTN_PER_STAT_SET, &CLS_VCAEVENT_PersonStat::OnBnClickedBtnPerStatSet)
	ON_BN_CLICKED(IDC_STC_PER_BTN_POLYGON_AREA_DRAW, &CLS_VCAEVENT_PersonStat::OnBnClickedStcPerBtnPolygonAreaDraw)
	ON_BN_CLICKED(IDC_BTN_PER_STAT_LINE_AREA_DRAW, &CLS_VCAEVENT_PersonStat::OnBnClickedBtnPerStatLineAreaDraw)
	ON_WM_SHOWWINDOW()
	ON_EN_CHANGE(IDC_EDT_PER_STAT_SENSITIVITY, &CLS_VCAEVENT_PersonStat::OnEnChangeEdtPerStatSensitivity)
	ON_EN_CHANGE(IDC_EDT_PER_STAT_AIM_MIN_SIZE, &CLS_VCAEVENT_PersonStat::OnEnChangeEdtPerStatAimMinSize)
	ON_EN_CHANGE(IDC_EDT_PER_STAT_AIM_MAX_SIZE, &CLS_VCAEVENT_PersonStat::OnEnChangeEdtPerStatAimMaxSize)
	ON_CBN_SELCHANGE(IDC_CBO_PER_STAT_DETECT_STYLE, &CLS_VCAEVENT_PersonStat::OnCbnSelchangeCboPerStatDetectStyle)
	ON_BN_CLICKED(IDC_BUTTON_CLEAR_CPCADVANCE, &CLS_VCAEVENT_PersonStat::OnBnClickedButtonClearCpcadvance)
END_MESSAGE_MAP()


// CLS_VCAEVENT_PersonStat message handler



void CLS_VCAEVENT_PersonStat::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	CleanText();
	if(bShow)
	{
		UpdatePageUI();
	}
}

BOOL CLS_VCAEVENT_PersonStat::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	m_edtSensitivity.SetLimitText(LEN_1);
	m_edtMaxSize.SetLimitText(LEN_3);
	m_edtMinSize.SetLimitText(LEN_2);

	m_iLineAreaPointNum = 0;
	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}


void CLS_VCAEVENT_PersonStat::OnLanguageChanged()
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAEVENT_PersonStat::UpdateUIText()
{
	SetDlgItemTextEx(IDC_CHK_PER_STAT_EVENT_VALID, IDS_VCAEVENT_EVENT_VALID);
	SetDlgItemTextEx(IDC_CHK_PER_STAT_SHOW_ALARM_RULE, IDS_VCAEVENT_SHOW_ALARM_RULE);
	SetDlgItemTextEx(IDC_CHK_PER_STAT_SHOW_ALARM_STAT, IDS_VCAEVENT_SHOW_ALARM_STATISTICS);
	SetDlgItemTextEx(IDC_CHK_PER_STAT_SHOW_TARGET_BOX, IDS_VCAEVENT_SHOW_TARGET_BOX);
	SetDlgItemTextEx(IDC_STC_PER_STAT_DETECT_STYLE, IDS_VCAEVENT_DETECT_STYLE);
	SetDlgItemTextEx(IDC_STC_PER_STAT_DETECT_MODE, IDS_CONFIG_ITS_TIMERANGE_DETECTMODE);
	SetDlgItemTextEx(IDC_STC_PER_STAT_SENSITIVITY, IDS_CONFIG_ITS_ILLEGALPARK_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_PER_STAT_ALARM_COLOR, IDS_VCA_ALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_PER_STAT_UNALARM_COLOR, IDS_VCA_NOALARM_COLOR);
	SetDlgItemTextEx(IDC_STC_PER_STAT_AIM_MIN_SIZE, IDS_VCA_ADV_MINSIZE);
	SetDlgItemTextEx(IDC_STC_PER_STAT_AIM_MAX_SIZE, IDS_VCA_ADV_MAXSIZE);
	SetDlgItemTextEx(IDC_STC_PER_STAT_POINT_NUM_IN_POLYGON, IDS_VCAEVENT_POLYGON_POINT_NUM);
	SetDlgItemTextEx(IDC_STC_PER_STAT_POLYGON_AREA, IDS_VCAEVENT_POLYGON);
	SetDlgItemTextEx(IDC_STC_PER_STAT_LINE_AREA, IDS_VCAEVENT_TRIP_LINE_AREA);
	SetDlgItemTextEx(IDC_STC_PER_BTN_POLYGON_AREA_DRAW, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_PER_STAT_LINE_AREA_DRAW, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_PER_STAT_SET, IDS_SET);
	SetDlgItemText(IDC_STATIC_MIN_SIZE_EX, GetTextByLan(_T("最小尺寸扩展"), _T("MinSizeEx")));
	SetDlgItemText(IDC_STATIC_CUR_NUM, GetTextByLan(_T("当前人数"), _T("CurNum")));
	SetDlgItemText(IDC_STATIC_MAX_SIZE, GetTextByLan(_T("最大人数"), _T("MaxSize")));
	SetDlgItemText(IDC_STATIC_CLEAR_MODE, GetTextByLan(_T("清除模式"), _T("ClearMode")));
	SetDlgItemText(IDC_STATIC_HOUR, GetTextByLan(_T("小时"), _T("Hour")));
	SetDlgItemText(IDC_STATIC_MINUTE, GetTextByLan(_T("分钟"), _T("Minute")));
	SetDlgItemText(IDC_STATIC_ENABLE, GetTextByLan(_T("使能"), _T("Enable")));
	SetDlgItemText(IDC_STATIC_HINT, GetTextByLan(_T("0：不支持，1：每天，2：从不"), _T("0:unsupport 1:everyday 2:never")));
	SetDlgItemText(IDC_STATIC_OVERTIME, GetTextByLan(_T("超限报警超时时间"), _T("Over limite Over Time")));
	SetDlgItemTextEx(IDC_BUTTON_CLEAR_CPCADVANCE, IDS_SET);

	SetDlgItemText(IDC_STATIC_DISPLAY_RULE, GetTextByLan(_T("规则线"), _T("Regular Line")));
	SetDlgItemText(IDC_STATIC_DISPLAY_INFO, GetTextByLan(_T("统计信息"), _T("Statistics Info")));


	m_cboDetectStyle.ResetContent();
	const CString cstDetectStyle[] = {GetTextEx(IDS_VCAEVENT_DETECT_AREA),
		GetTextEx(IDS_VCAEVENT_DETECT_LINE),GetTextByLan(_T("两者都检测"), _T("Both"))};
	for (int i = 0; i < sizeof(cstDetectStyle)/sizeof(CString); i++)
	{
		m_cboDetectStyle.InsertString(i, cstDetectStyle[i]);
	}
	m_cboDetectStyle.SetCurSel(0);

	m_cboDetectMode.ResetContent();
	const CString cstDetectMode[] = {GetTextEx(IDS_VCAEVENT_VERTICAL_PERSON_STAT),
		GetTextEx(IDS_VCAEVENT_LEVEL_PERSON_STAT),GetTextEx(IDS_VCAEVENT_CPC_RAEA),
		GetTextEx(IDS_VCAEVENT_DRAW_PEOPLE_STATISTIC),GetTextEx(IDS_VCAEVENT_QUEUE_PEOPLE_STATIC)};
	for (int i = 0; i < sizeof(cstDetectMode)/sizeof(CString); i++)
	{
		m_cboDetectMode.InsertString(i, cstDetectMode[i]);
	}
	m_cboDetectMode.SetCurSel(0);

	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), 
		GetTextEx(IDS_VCA_COL_YELLOW), GetTextEx(IDS_VCA_COL_BLUE), 
		GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), 
		GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboAlarmColor.ResetContent();
	m_cboUnalarmColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboAlarmColor.InsertString(i, strColor[i]);
		m_cboUnalarmColor.InsertString(i, strColor[i]);
	}
	m_cboAlarmColor.SetCurSel(0);
	m_cboUnalarmColor.SetCurSel(0);

	CString cstTemp = "";
	m_cboPolygonPointNum.ResetContent();
	for (int i=(POINT_NUM_MIN - 2); i <= (POINT_NUM_MAX - 2); i++)
	{	
		cstTemp.Format("%d",i + 2);
		m_cboPolygonPointNum.InsertString(i, cstTemp);
	}
	m_cboPolygonPointNum.SetCurSel(0);

	m_cboDisplayRule.InsertString(0, GetTextByLan(_T("保留"), _T("Reserve")));
	m_cboDisplayRule.InsertString(1, GetTextByLan(_T("显示"), _T("Display")));
	m_cboDisplayRule.InsertString(2, GetTextByLan(_T("不显示"), _T("Not DisPlay")));
	m_cboDisplayRule.SetCurSel(0);

	m_cboDisplayInfo.InsertString(0, GetTextByLan(_T("保留"), _T("Reserve")));
	m_cboDisplayInfo.InsertString(1, GetTextByLan(_T("显示"), _T("Display")));
	m_cboDisplayInfo.InsertString(2, GetTextByLan(_T("不显示"), _T("Not DisPlay")));
	m_cboDisplayInfo.SetCurSel(0);
}

void CLS_VCAEVENT_PersonStat::CleanText()
{
	m_edtPolygonArea.Clear();
	m_edtLineArea.Clear();
}

void CLS_VCAEVENT_PersonStat::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PersonStat::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	PersonStatisticArithmetic psa = {0};
	int iBytesReturn = 0;

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_PERSON_STATISTIC_ARITHMETIC, m_iChannelNO, &psa, sizeof(PersonStatisticArithmetic), &iBytesReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_PersonStat::NetClient_GetDevConfig[NET_CLIENT_PERSON_STATISTIC_ARITHMETIC] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_chkTargetBox.SetCheck(psa.iDisplayTarget);
		m_cboDetectStyle.SetCurSel(psa.iDetectType - 1);
		m_cboDetectMode.SetCurSel(psa.iMode - 1);
		
		SetDlgItemInt(IDC_EDT_PER_STAT_SENSITIVITY, psa.iSensitiv);
		SetDlgItemInt(IDC_EDT_PER_STAT_AIM_MIN_SIZE, psa.iTargetMinSize);
		SetDlgItemInt(IDC_EDT_PER_STAT_AIM_MAX_SIZE, psa.iTargetMaxSize);

		SetDlgItemInt(IDC_EDIT_MIN_SIZE,psa.iMinSizeEx);
		SetDlgItemInt(IDC_EDIT_STAY_NUM,psa.iStayNum);
		SetDlgItemInt(IDC_EDIT_MAX_SIZE,psa.iMaxNum);
		SetDlgItemInt(IDC_EDIT_CLEAR_HOUR,psa.iHour);
		SetDlgItemInt(IDC_EDIT_CLEAR_MINUTE,psa.iMinute);
		SetDlgItemInt(IDC_EDIT_CLEAR_MODE,psa.iClearMode);
		SetDlgItemInt(IDC_EDIT_PERSION_ENABLE,psa.iEnable);



		CString cstPolygonBuf = "";
		for(int i=0; i<psa.iPointNum; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", psa.ptArea[i].x, psa.ptArea[i].y);
		}

		SetDlgItemText(IDC_EDT_PER_STAT_LINE_AREA, cstPolygonBuf);


		CString cstLineAreaBuf = "";
		for (int i=0; i<psa.stRegion.iPointNum; i++)
		{
			cstLineAreaBuf.AppendFormat("(%d, %d)", psa.stRegion.stPoints[i].iX, psa.stRegion.stPoints[i].iY);
		}
		m_cboPolygonPointNum.SetCurSel(psa.stRegion.iPointNum - 2);

		SetDlgItemText(IDC_EDT_PER_STAT_POLYGON_AREA, cstLineAreaBuf);
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_PersonStat::NetClient_GetDevConfig[NET_CLIENT_PERSON_STATISTIC_ARITHMETIC] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	UpdateCpcAdvancePart2();
}

void CLS_VCAEVENT_PersonStat::OnBnClickedBtnPerStatSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PersonStat::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	if (GetDlgItemInt(IDC_EDT_PER_STAT_AIM_MIN_SIZE) >= GetDlgItemInt(IDC_EDT_PER_STAT_AIM_MAX_SIZE))
	{
			AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PersonStat::MAX_SIZE Value Must Greater Than MIN_SIZE Value (%d,%d)", m_iLogonID,m_iChannelNO);
	}

	PersonStatisticArithmetic psa = {0};

	psa.iDetectType = m_cboDetectStyle.GetCurSel() + 1;
	psa.iDisplayTarget = m_chkTargetBox.GetCheck();
	psa.iMode = m_cboDetectMode.GetCurSel() + 1;
	psa.iPointNum = m_iLineAreaPointNum;
	psa.iSceneID = m_iSceneID;
	psa.iSensitiv = GetDlgItemInt(IDC_EDT_PER_STAT_SENSITIVITY);
	psa.iTargetMaxSize = GetDlgItemInt(IDC_EDT_PER_STAT_AIM_MAX_SIZE);
	psa.iTargetMinSize = GetDlgItemInt(IDC_EDT_PER_STAT_AIM_MIN_SIZE);

	POINT ptPolygon[MAX_FACE_DETECT_AREA_COUNT] = {0} ;
	CString cstPolygon = "";
	GetDlgItemText(IDC_EDT_PER_STAT_LINE_AREA, cstPolygon);
	GetPointsFromString(cstPolygon, psa.iPointNum, ptPolygon);
	for (int i=0; i<psa.iPointNum; i++)
	{
		psa.ptArea[i] = ptPolygon[i];
	}

	CString cstLineArea = "";
	GetDlgItemText(IDC_EDT_PER_STAT_POLYGON_AREA, cstLineArea);
	GetPolyFromStringEx(cstLineArea,m_cboPolygonPointNum.GetCurSel() + 2, psa.stRegion);

	psa.iBufSize = sizeof(psa);

	psa.iMinSizeEx = GetDlgItemInt(IDC_EDIT_MIN_SIZE);
	psa.iStayNum = GetDlgItemInt(IDC_EDIT_STAY_NUM);
	psa.iClearMode = GetDlgItemInt(IDC_EDIT_CLEAR_MODE);
	psa.iMaxNum = GetDlgItemInt(IDC_EDIT_MAX_SIZE);
	psa.iHour = GetDlgItemInt(IDC_EDIT_CLEAR_HOUR);
	psa.iMinute = GetDlgItemInt(IDC_EDIT_CLEAR_MINUTE);
	psa.iEnable = GetDlgItemInt(IDC_EDIT_PERSION_ENABLE);

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_PERSON_STATISTIC_ARITHMETIC, m_iChannelNO, &psa, sizeof(PersonStatisticArithmetic));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_PersonStat::NetClient_SetDevConfig[NET_CLIENT_PERSON_STATISTIC_ARITHMETIC] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_PersonStat::NetClient_SetDevConfig[NET_CLIENT_PERSON_STATISTIC_ARITHMETIC] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_PersonStat::OnBnClickedStcPerBtnPolygonAreaDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_perimeter);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection);
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
			m_edtPolygonArea.SetWindowText(cPointBuf);
			m_cboPolygonPointNum.SetCurSel(iPointNum - 2);
		}
		else
		{
			m_edtPolygonArea.SetWindowText("");
			m_cboPolygonPointNum.SetCurSel(-1);
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_PersonStat::OnBnClickedBtnPerStatLineAreaDraw()
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
	m_pDlgVideoView->SetDrawType(DrawType_tripwire);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		m_edtLineArea.SetWindowText(cPointBuf);
		m_iLineAreaPointNum = iPointNum;
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}
void CLS_VCAEVENT_PersonStat::OnEnChangeEdtPerStatSensitivity()
{
	int iTemp = GetDlgItemInt(IDC_EDT_PER_STAT_SENSITIVITY);
	if (iTemp < 0 || iTemp > 5)
	{
		SetDlgItemInt(IDC_EDT_PER_STAT_SENSITIVITY, 5);
	}
}

void CLS_VCAEVENT_PersonStat::OnEnChangeEdtPerStatAimMinSize()
{
	int iTemp = GetDlgItemInt(IDC_EDT_PER_STAT_AIM_MIN_SIZE);
	if (iTemp < MIN_SIZE_FROM || iTemp > MIN_SIZE_TO - 1)
	{
		SetDlgItemInt(IDC_EDT_PER_STAT_AIM_MIN_SIZE, MIN_SIZE_TO - 1);
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PersonStat::Put Invalid Value(%d,%d)", m_iLogonID,m_iChannelNO);
	}
}

void CLS_VCAEVENT_PersonStat::OnEnChangeEdtPerStatAimMaxSize()
{
	int iTemp = GetDlgItemInt(IDC_EDT_PER_STAT_AIM_MAX_SIZE);
	if (iTemp < MAX_SIZE_FROM + 1)
	{
		SetDlgItemInt(IDC_EDT_PER_STAT_AIM_MAX_SIZE, MAX_SIZE_FROM + 1);
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PersonStat::Put Invalid Value(%d,%d)", m_iLogonID,m_iChannelNO);
	}
	if (iTemp > MAX_SIZE_TO)
	{
		SetDlgItemInt(IDC_EDT_PER_STAT_AIM_MAX_SIZE, MAX_SIZE_TO);
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PersonStat::Put Invalid Value(%d,%d)", m_iLogonID,m_iChannelNO);
	}
}

void CLS_VCAEVENT_PersonStat::OnCbnSelchangeCboPerStatDetectStyle()
{
	 //No distinction between lines and points
	 return;
	 if (0 == m_cboDetectStyle.GetCurSel())
	 {
		 GetDlgItem(IDC_EDT_PER_STAT_POLYGON_AREA)->EnableWindow(TRUE);
		 GetDlgItem(IDC_STC_PER_BTN_POLYGON_AREA_DRAW)->EnableWindow(TRUE);
		 GetDlgItem(IDC_EDT_PER_STAT_LINE_AREA)->EnableWindow(FALSE);
		 GetDlgItem(IDC_BTN_PER_STAT_LINE_AREA_DRAW)->EnableWindow(FALSE);
	 }
	 else
	 {
		 GetDlgItem(IDC_EDT_PER_STAT_POLYGON_AREA)->EnableWindow(FALSE);
		 GetDlgItem(IDC_STC_PER_BTN_POLYGON_AREA_DRAW)->EnableWindow(FALSE);
		 GetDlgItem(IDC_EDT_PER_STAT_LINE_AREA)->EnableWindow(TRUE);
		 GetDlgItem(IDC_BTN_PER_STAT_LINE_AREA_DRAW)->EnableWindow(TRUE);
	 }
}

void CLS_VCAEVENT_PersonStat::OnBnClickedButtonClearCpcadvance()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	VcaCpcAdvancePart2 tVcaCpcAdvancePart2 = {0};
	tVcaCpcAdvancePart2.iSceneID = m_iSceneID;
	tVcaCpcAdvancePart2.iAlarmTimeout = m_iTimeout;
	tVcaCpcAdvancePart2.iDisplayRule = m_cboDisplayRule.GetCurSel();
	tVcaCpcAdvancePart2.iDisplayInfo = m_cboDisplayInfo.GetCurSel();

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CPC_ADVANCE_PART2, m_iChannelNO, &tVcaCpcAdvancePart2, sizeof(tVcaCpcAdvancePart2));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_CPC_ADVANCE_PART2] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_CPC_ADVANCE_PART2] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_PersonStat::UpdateCpcAdvancePart2()
{
	VcaCpcAdvancePart2 tVcaCpcAdvancePart2 = {0};
	tVcaCpcAdvancePart2.iSceneID = m_iSceneID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CPC_ADVANCE_PART2, m_iChannelNO, &tVcaCpcAdvancePart2, sizeof(tVcaCpcAdvancePart2));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCAGetConfig[VCA_CMD_CPC_ADVANCE_PART2] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCAGetConfig[VCA_CMD_CPC_ADVANCE_PART2] (%d, %d)", m_iLogonID, m_iChannelNO);
	}

	m_iTimeout = tVcaCpcAdvancePart2.iAlarmTimeout;
	m_cboDisplayRule.SetCurSel(tVcaCpcAdvancePart2.iDisplayRule);
	m_cboDisplayInfo.SetCurSel(tVcaCpcAdvancePart2.iDisplayInfo);
	UpdateData(FALSE);

}

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertEnable.h"

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertEnable, CDialog)

void GetPolyFromStringEx( CString _strPoints, int _iPointNum, vca_TPolygonEx& _stPoly )
{
	int iLength = _strPoints.GetLength()+1;
	char* pcData = new char [iLength];
	memset(pcData, 0, iLength);
	memcpy(pcData, _strPoints.GetBuffer(), iLength-1);
	char* p1 = pcData;
	char* p2 = NULL;
	int iPointIndex = 0;
	for (int i = 0; i < iLength; ++i)
	{
		p2 = strstr(p1, ")");
		if (p2 == NULL)
			break;

		char cCell[200] = {0};
		int iX = 0, iY = 0;
		memcpy(cCell, p1, p2-p1+1);
		sscanf_s(cCell, "(%d,%d)", &iX, &iY);
		_stPoly.stPoints[iPointIndex].iX = iX;
		_stPoly.stPoints[iPointIndex].iY = iY;
		if (++iPointIndex == VCA_MAX_POLYGON_POINT_NUMEX)
			break;

		if ((p1 = p2+1) >= pcData+iLength)
			break;
	}

	_stPoly.iPointNum =  iPointIndex;
	if (iPointIndex != _iPointNum && _iPointNum != 0)
	{
		_stPoly.iPointNum = _iPointNum;
	}

	delete [] pcData;
	pcData = NULL;
}

CLS_DlgUniqueAlertEnable::CLS_DlgUniqueAlertEnable(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertEnable::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
	m_iDrawAreaPointNum = 0;
	m_pDlgDrawVideoView = NULL;
}

CLS_DlgUniqueAlertEnable::~CLS_DlgUniqueAlertEnable()
{
}

void CLS_DlgUniqueAlertEnable::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALERT_EVENT_SCENE, m_cboAlertSceneNo);
	DDX_Control(pDX, IDC_DTP_SCENE_TIME_SEG_2, m_dtpTimeSegment2);
	DDX_Control(pDX, IDC_DTP_SCENE_TIME_SEG_1, m_dtpTimeSegment1);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertEnable, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_SET_ALERT_CHN_ENABLE, &CLS_DlgUniqueAlertEnable::OnBnClickedBtnSetAlertChnEnable)
	ON_BN_CLICKED(IDC_CHK_ALERT_CHN_ENABLE, &CLS_DlgUniqueAlertEnable::OnBnClickedChkAlertChnEnable)
	ON_CBN_SELCHANGE(IDC_CBO_ALERT_EVENT_SCENE, &CLS_DlgUniqueAlertEnable::OnCbnSelchangeCboAlertEventScene)
	ON_BN_CLICKED(IDC_BTN_SET_ALERT_EVENT_ENABLE, &CLS_DlgUniqueAlertEnable::OnBnClickedBtnSetAlertEventEnable)
	ON_BN_CLICKED(IDC_BTN_ALERT_ANALYZE_AREA_DRAW, &CLS_DlgUniqueAlertEnable::OnBnClickedBtnAlertAnalyzeAreaDraw)
	ON_BN_CLICKED(IDC_RADIO_ALERT_EVENT_PERIMETER, &CLS_DlgUniqueAlertEnable::OnBnClickedRadioAlertEventPerimeter)
	ON_BN_CLICKED(IDC_RADIO_ALERT_EVENT_TRIPWIRE, &CLS_DlgUniqueAlertEnable::OnBnClickedRadioAlertEventTripwire)
    ON_BN_CLICKED(IDC_RADIO_ALERT_EVENT_CLIMBWALL, &CLS_DlgUniqueAlertEnable::OnBnClickedRadioAlertEventTripwire)
	ON_BN_CLICKED(IDC_BTN_ALERT_ANALYZE_AREA_SET, &CLS_DlgUniqueAlertEnable::OnBnClickedBtnAlertAnalyzeAreaSet)
	ON_BN_CLICKED(IDC_BTN_ALERT_SCENE_TIME_SEGMENT_SET, &CLS_DlgUniqueAlertEnable::OnBnClickedBtnAlertSceneTimeSegmentSet)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertEnable::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_InitDlgWidget();
	return TRUE;
}

void CLS_DlgUniqueAlertEnable::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertEnable::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (m_iLogonID == _iLogonID && m_iChannelNo == _iChannelNo && m_iStreamNo == _iStreamNo)
	{
		return;
	}

	m_iLogonID = _iLogonID;
	m_iChannelNo = ((_iChannelNo < 0) ? 0 : _iChannelNo);
	m_iStreamNo = _iStreamNo;

	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertEnable::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertEnable::OnMainNotify( int _iLogonID, int _iWparam, void* _pvLParam, void* _pvUser )
{
	if (NULL != m_pDlgDrawVideoView)
	{
		m_pDlgDrawVideoView->OnMainNotify(_iLogonID, _iWparam, _pvLParam, _pvUser);
	}
}

void CLS_DlgUniqueAlertEnable::UI_InitDlgWidget()
{
	m_dtpTimeSegment1.SetFormat("DD:TT");

	CTime BeginTime(1971, 1, 1, 0, 0, 0);
	CTime EndTime(1971, 1, 1, 23, 59, 59);
	m_dtpTimeSegment1.SetFormat("HH:mm");
	m_dtpTimeSegment1.SetTime(&BeginTime);
	m_dtpTimeSegment2.SetFormat("HH:mm");
	m_dtpTimeSegment2.SetTime(&EndTime);
}

void CLS_DlgUniqueAlertEnable::UI_InitDlgItemText()
{
	int iCurSel = m_cboAlertSceneNo.GetCurSel();
	m_cboAlertSceneNo.ResetContent();
	for (int i = 0; i < MAX_UNIQUE_ALERT_SCENE_NUM; i++)
	{
		m_cboAlertSceneNo.SetItemData(m_cboAlertSceneNo.AddString(GetTextEx(IDS_ALERT_SCENE) + IntToCString(i + 1)), i);
	}
	m_cboAlertSceneNo.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	SetDlgItemText(IDC_GPO_SUPPORT_ALERT_LIST, GetTextByLan(_T("支持算法类型列表"), _T("Support alert algorithm type list")));
	SetDlgItemText(IDC_CHK_PERIMETER_ENTER, GetTextByLan(_T("周界入侵"), _T("Perimeter intrusion")));
	SetDlgItemText(IDC_CHK_PERIMETER_LEAVE, GetTextByLan(_T("周界离开"), _T("Perimeter leave")));
	SetDlgItemText(IDC_CHK_TRIPWIRE, GetTextByLan(_T("绊线"), _T("Tripwires")));
	SetDlgItemText(IDC_GPO_CHN_ENABLE_STATUS, GetTextByLan(_T("通道启用状态"), _T("Channel is enabled")));
	SetDlgItemText(IDC_CHK_ALERT_CHN_ENABLE, GetTextByLan(_T("启用"), _T("Enable")));
	SetDlgItemText(IDC_RADIO_ALERT_LOCAL_ANALYZE, GetTextByLan(_T("本地智能警戒"), _T("The local intelligent alert")));
	SetDlgItemText(IDC_RADIO_ALERT_FRONT_ANALYZE, GetTextByLan(_T("前端智能警戒"), _T("The front-end smart alert")));
	SetDlgItemText(IDC_BTN_SET_ALERT_CHN_ENABLE, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_RADIO_ALERT_EVENT_PERIMETER, GetTextByLan(_T("周界警戒"), _T("Perimeter alert")));
	SetDlgItemText(IDC_RADIO_ALERT_EVENT_TRIPWIRE, GetTextByLan(_T("绊线警戒"), _T("Tripwires alert")));
    SetDlgItemText(IDC_RADIO_ALERT_EVENT_CLIMBWALL, GetTextByLan(_T("翻墙警戒"), _T("ClimbWall alert")));
	SetDlgItemText(IDC_BTN_SET_ALERT_EVENT_ENABLE, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_GPO_CHN_ENABLE_STATUS2, GetTextByLan(_T("场景分析状态"), _T("Scene analysis state")));
	SetDlgItemText(IDC_STC_ALERT_ANALYZE_AREA, GetTextByLan(_T("区域"), _T("Area")));
	SetDlgItemText(IDC_BTN_ALERT_ANALYZE_AREA_DRAW, GetTextByLan(_T("绘制"), _T("Draw")));
	SetDlgItemText(IDC_BTN_ALERT_ANALYZE_AREA_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STC_ALERT_SCENE_TIME_SEG, GetTextByLan(_T("巡航"), _T("Cruise")));
	SetDlgItemText(IDC_BTN_ALERT_SCENE_TIME_SEGMENT_SET, GetTextByLan(_T("设置"), _T("Set")));
	return;
}

void CLS_DlgUniqueAlertEnable::UI_UpdateInterfaceParam()
{
	UI_UpdateInfoAlertList();
	UI_UpdateInfoAlertChnEnable();
	UI_UpdateInfoAlertEventEnabel();
	UI_UpdateInfoAlertSceneTimeSegment();
	return;
}

void CLS_DlgUniqueAlertEnable::UI_UpdateInfoAlertList()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	BOOL blSupportEnable = FALSE;

	//Get the perimeter alert algorithm enable
	UniqueAlertList tList = {0};
	tList.iSize = sizeof(tList);
	tList.iAlertType = UNIQUE_ALERT_TYPE_PERIMETER;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_LIST, m_iChannelNo, &tList, tList.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}
	//perimeter invasion
	blSupportEnable = tList.iAlertParam & 1;
	((CButton*)(GetDlgItem(IDC_CHK_PERIMETER_ENTER)))->SetCheck(blSupportEnable ? BST_CHECKED : BST_UNCHECKED);
	// perimeter leave
	blSupportEnable = (tList.iAlertParam >> 1) & 1;
	((CButton*)(GetDlgItem(IDC_CHK_PERIMETER_LEAVE)))->SetCheck(blSupportEnable ? BST_CHECKED : BST_UNCHECKED);

	//Get tripwire alert algorithm enable
	memset(&tList, 0, sizeof(tList));
	tList.iSize = sizeof(tList);
	tList.iAlertType = UNIQUE_ALERT_TYPE_TRIPWIRE;
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_LIST, m_iChannelNo, &tList, tList.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}
	blSupportEnable = (UNIQUE_ALERT_TYPE_TRIPWIRE == tList.iAlertType);
	((CButton*)(GetDlgItem(IDC_CHK_TRIPWIRE)))->SetCheck(blSupportEnable ? BST_CHECKED : BST_UNCHECKED);

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[AlertList](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[AlertList](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertEnable::UI_UpdateInfoAlertChnEnable()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	BOOL blLocalAnalyze = FALSE;
	BOOL blRemoteAnalyze = FALSE;

	UniqueAlertCfgChn tChnEnable = {0};
	tChnEnable.iSize = sizeof(tChnEnable);
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_CFGCHN, m_iChannelNo, &tChnEnable, tChnEnable.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	((CButton*)(GetDlgItem(IDC_CHK_ALERT_CHN_ENABLE)))->SetCheck(tChnEnable.iEnable ? BST_CHECKED : BST_UNCHECKED);

	blLocalAnalyze = (0 == tChnEnable.iEnChnType) ? TRUE : FALSE;
	blRemoteAnalyze = (1 == tChnEnable.iEnChnType) ? TRUE : FALSE;
	((CButton*)(GetDlgItem(IDC_RADIO_ALERT_LOCAL_ANALYZE)))->SetCheck(blLocalAnalyze ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_RADIO_ALERT_FRONT_ANALYZE)))->SetCheck(blRemoteAnalyze ? BST_CHECKED : BST_UNCHECKED);

	UI_UpdataChnEnableWidget();
	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[ChnEnable](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[ChnEnable](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertEnable::UI_UpdateInfoAlertEventEnabel()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	UniqueAlertEventSet tEventSet = {0};
	tEventSet.iSize = sizeof(tEventSet);
	tEventSet.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_EVENTSET, m_iChannelNo, &tEventSet, tEventSet.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_PERIMETER)))->SetCheck(tEventSet.iTypeEnable[UNIQUE_ALERT_TYPE_PERIMETER] ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_TRIPWIRE)))->SetCheck(tEventSet.iTypeEnable[UNIQUE_ALERT_TYPE_TRIPWIRE] ? BST_CHECKED : BST_UNCHECKED);
    ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_CLIMBWALL)))->SetCheck(tEventSet.iTypeEnable[UNIQUE_ALERT_TYPE_CLIMBWALL] ? BST_CHECKED : BST_UNCHECKED);

	iRet = RET_SUCCESS;
	UI_UpdateInfoAlertDrawLine();

EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[EventEnabel](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[EventEnabel](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertEnable::UI_UpdateInfoAlertDrawLine()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	CString cstrPoint;
	
	UniqueAlertDrawLine tDrawInfo = {0};
	tDrawInfo.iSize = sizeof(tDrawInfo);
	tDrawInfo.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tDrawInfo.iEventNo = 0;
	if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_PERIMETER)))->GetCheck())
	{
		tDrawInfo.iAlertType = UNIQUE_ALERT_TYPE_PERIMETER;
	}
	else if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_TRIPWIRE)))->GetCheck())
	{
		tDrawInfo.iAlertType = UNIQUE_ALERT_TYPE_TRIPWIRE;
	}
    else if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_CLIMBWALL)))->GetCheck())
    {
        tDrawInfo.iAlertType = UNIQUE_ALERT_TYPE_CLIMBWALL;
    }
	else
	{
		goto EXIT_FUNC;
	}

	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_DRAW_LINE, m_iChannelNo, &tDrawInfo, tDrawInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	m_iDrawAreaPointNum = tDrawInfo.tAreaInfo[0].iPointNum;

	for(int i = 0; i < m_iDrawAreaPointNum && i < VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		cstrPoint.AppendFormat("(%d,%d)", tDrawInfo.tAreaInfo[0].stPoints[i].iX, tDrawInfo.tAreaInfo[0].stPoints[i].iY);
	}
	SetDlgItemText(IDC_EDT_ALERT_ANALYZE_AREA, cstrPoint);

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[DrawInfo](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[DrawInfo](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertEnable::UI_UpdateInfoAlertSceneTimeSegment()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	CTime timeNow = CTime::GetCurrentTime();
	CTime timeSchStart, timeSchStop;

	UniqueAlertSceneTimeSeg tTimeSegment = {0};
	tTimeSegment.iSize = sizeof(tTimeSegment);
	tTimeSegment.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_SCENE_TIMESEG, m_iChannelNo, &tTimeSegment, tTimeSegment.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}
 
	timeSchStart = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), tTimeSegment.iStartHour, tTimeSegment.iStartMinute, 0);
	timeSchStop = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(), tTimeSegment.iStopHour, tTimeSegment.iStopMinute, 0);
	m_dtpTimeSegment1.SetTime(&timeSchStart);
	m_dtpTimeSegment2.SetTime(&timeSchStop);

	((CButton*)(GetDlgItem(IDC_CHK_ALERT_SCENE_TIME_SEG)))->SetCheck(tTimeSegment.iEnable ? BST_CHECKED : BST_UNCHECKED);

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[TimeSegment](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[TimeSegment](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertEnable::UI_UpdataChnEnableWidget()
{
	BOOL blChnEnable = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_CHN_ENABLE)))->GetCheck());
	((CButton*)(GetDlgItem(IDC_RADIO_ALERT_LOCAL_ANALYZE)))->EnableWindow(blChnEnable);
	((CButton*)(GetDlgItem(IDC_RADIO_ALERT_FRONT_ANALYZE)))->EnableWindow(blChnEnable);

	if (!blChnEnable)
	{
		((CButton*)(GetDlgItem(IDC_RADIO_ALERT_LOCAL_ANALYZE)))->SetCheck(BST_UNCHECKED);
		((CButton*)(GetDlgItem(IDC_RADIO_ALERT_FRONT_ANALYZE)))->SetCheck(BST_UNCHECKED);
	}
}

void CLS_DlgUniqueAlertEnable::OnBnClickedBtnSetAlertChnEnable()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	BOOL blChnEnable = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_CHN_ENABLE)))->GetCheck());
	BOOL blRemoteAnalyze = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_FRONT_ANALYZE)))->GetCheck());

	UniqueAlertCfgChn tChnEnable = {0};
	tChnEnable.iSize = sizeof(tChnEnable);
	if (blChnEnable)
	{
		tChnEnable.iEnable = 1;
		if (blRemoteAnalyze)
		{
			tChnEnable.iEnChnType = 1;
		}
	}

	int iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_CFGCHN, m_iChannelNo, &tChnEnable, tChnEnable.iSize);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[ChnEnable](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[ChnEnable](%d,%d)", m_iLogonID, m_iChannelNo);
	}
}

void CLS_DlgUniqueAlertEnable::OnBnClickedChkAlertChnEnable()
{
	UI_UpdataChnEnableWidget();
}

void CLS_DlgUniqueAlertEnable::OnCbnSelchangeCboAlertEventScene()
{
	UI_UpdateInfoAlertEventEnabel();
	UI_UpdateInfoAlertSceneTimeSegment();
}

void CLS_DlgUniqueAlertEnable::OnBnClickedBtnSetAlertEventEnable()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	UniqueAlertEventSet tEventSet = {0};
	tEventSet.iSize = sizeof(tEventSet);
	tEventSet.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tEventSet.iTypeEnable[UNIQUE_ALERT_TYPE_PERIMETER] = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_PERIMETER)))->GetCheck());
	tEventSet.iTypeEnable[UNIQUE_ALERT_TYPE_TRIPWIRE] = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_TRIPWIRE)))->GetCheck());
    tEventSet.iTypeEnable[UNIQUE_ALERT_TYPE_CLIMBWALL] = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_CLIMBWALL)))->GetCheck());

	int iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_EVENTSET, m_iChannelNo, &tEventSet, tEventSet.iSize);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[EventEnable](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[EventEnable](%d,%d)", m_iLogonID, m_iChannelNo);
	}
}

void CLS_DlgUniqueAlertEnable::OnBnClickedBtnAlertAnalyzeAreaDraw()
{
	int iDrawLineType = 0;
	if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_PERIMETER)))->GetCheck())
	{
		iDrawLineType = DrawType_perimeter;
	}
	else if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_TRIPWIRE)))->GetCheck())
	{
		iDrawLineType = DrawType_tripwire;
	}
	else
	{
		return;
	}

	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	if (NULL == m_pDlgDrawVideoView)
	{
		m_pDlgDrawVideoView = new CLS_VideoViewForDraw();
	}

	if (NULL == m_pDlgDrawVideoView)
	{
		goto EXIT_FUNC;
	}

	m_pDlgDrawVideoView->Init(m_iLogonID, m_iChannelNo, m_iStreamNO);
	m_pDlgDrawVideoView->SetDrawType(iDrawLineType);
	int iSetRet = m_pDlgDrawVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (iSetRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	if (IDOK == m_pDlgDrawVideoView->DoModal())
	{
		SetDlgItemText(IDC_EDT_ALERT_ANALYZE_AREA, cPointBuf);
		m_iDrawAreaPointNum = iPointNum;
	}

EXIT_FUNC:
	if (NULL != m_pDlgDrawVideoView)
	{
		delete m_pDlgDrawVideoView;
		m_pDlgDrawVideoView = NULL;
	}
	return;
}

void CLS_DlgUniqueAlertEnable::OnBnClickedRadioAlertEventPerimeter()
{
	UI_UpdateInfoAlertDrawLine();
}

void CLS_DlgUniqueAlertEnable::OnBnClickedRadioAlertEventTripwire()
{
	UI_UpdateInfoAlertDrawLine();
}
void CLS_DlgUniqueAlertEnable::OnBnClickedRadioAlertEventClimbWall()
{
    UI_UpdateInfoAlertDrawLine();
}

void CLS_DlgUniqueAlertEnable::OnBnClickedBtnAlertAnalyzeAreaSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	CString cstrPoint;

	UniqueAlertDrawLine tDrawInfo = {0};
	tDrawInfo.iSize = sizeof(tDrawInfo);
	tDrawInfo.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tDrawInfo.iEventNo = 0;
	if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_PERIMETER)))->GetCheck())
	{
		tDrawInfo.iAlertType = UNIQUE_ALERT_TYPE_PERIMETER;
	}
	else if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_TRIPWIRE)))->GetCheck())
	{
		tDrawInfo.iAlertType = UNIQUE_ALERT_TYPE_TRIPWIRE;
	}
    else if (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_RADIO_ALERT_EVENT_CLIMBWALL)))->GetCheck())
    {
        tDrawInfo.iAlertType = UNIQUE_ALERT_TYPE_CLIMBWALL;
    }
	else
	{
		goto EXIT_FUNC;
	}

	//Currently there is only one area by default
	tDrawInfo.iAreaNum = 1;
	GetDlgItemText(IDC_EDT_ALERT_ANALYZE_AREA, cstrPoint);
	GetPolyFromStringEx(cstrPoint, m_iDrawAreaPointNum, tDrawInfo.tAreaInfo[0]);

	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_DRAW_LINE, m_iChannelNo, &tDrawInfo, tDrawInfo.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[DrawInfo](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[DrawInfo](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertEnable::OnBnClickedBtnAlertSceneTimeSegmentSet()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	CTime timeNow = CTime::GetCurrentTime();
	CTime timeSchStart, timeSchStop;
	m_dtpTimeSegment1.GetTime(timeSchStart);
	m_dtpTimeSegment2.GetTime(timeSchStop);

	UniqueAlertSceneTimeSeg tTimeSegment = {0};
	tTimeSegment.iSize = sizeof(tTimeSegment);
	tTimeSegment.iSceneId = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
	tTimeSegment.iEnable = ((BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_SCENE_TIME_SEG)))->GetCheck()) ? 1 : 0);
	tTimeSegment.iStartHour = timeSchStart.GetHour();
	tTimeSegment.iStartMinute = timeSchStart.GetMinute();
	tTimeSegment.iStopHour = timeSchStop.GetHour();
	tTimeSegment.iStopMinute = timeSchStop.GetMinute();

	iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_SCENE_TIMESEG, m_iChannelNo, &tTimeSegment, tTimeSegment.iSize);
	if (iRet < RET_SUCCESS)
	{
		goto EXIT_FUNC;
	}

	iRet = RET_SUCCESS;
EXIT_FUNC:
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetUnipueAlertConfig[TimeSegment](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetUnipueAlertConfig[TimeSegment](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;	
}

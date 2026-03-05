// VCASmartSearch.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCASmartSearch.h"
#include "Common/CommonFun.h"

#define STATEMENT_TYPE_HOUR         0           //time report
#define STATEMENT_TYPE_DAY			1			//daily report
#define STATEMENT_TYPE_WEEK			2			//Weekly Report
#define STATEMENT_TYPE_MONTH		3			//monthly report
#define STATEMENT_TYPE_YEAR			4			// annual report


// CLS_VCASmartSearch dialog

IMPLEMENT_DYNAMIC(CLS_VCASmartSearch, CDialog)

CLS_VCASmartSearch::CLS_VCASmartSearch(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCASmartSearch::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iStreamNo = 0;
}

CLS_VCASmartSearch::~CLS_VCASmartSearch()
{
}

void CLS_VCASmartSearch::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LST_VCA_SEARCH_STATEMENT, m_lstctrlStatement);
	DDX_Control(pDX, IDC_CBO_VCA_SEARCH_STATEMENT_TYPE, m_cboStatementType);
	DDX_Control(pDX, IDC_DTP_VCA_SEARCH_STATISTICS_TIME, m_dtpStatTime);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_LIST, m_chkChannelListEnable);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_0, m_chkChannel[0]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_1, m_chkChannel[1]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_2, m_chkChannel[2]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_3, m_chkChannel[3]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_4, m_chkChannel[4]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_5, m_chkChannel[5]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_6, m_chkChannel[6]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_7, m_chkChannel[7]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_8, m_chkChannel[8]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_9, m_chkChannel[9]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_10, m_chkChannel[10]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_11, m_chkChannel[11]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_12, m_chkChannel[12]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_13, m_chkChannel[13]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_14, m_chkChannel[14]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_15, m_chkChannel[15]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_16, m_chkChannel[16]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_17, m_chkChannel[17]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_18, m_chkChannel[18]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_19, m_chkChannel[19]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_20, m_chkChannel[20]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_21, m_chkChannel[21]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_22, m_chkChannel[22]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_23, m_chkChannel[23]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_24, m_chkChannel[24]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_25, m_chkChannel[25]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_26, m_chkChannel[26]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_27, m_chkChannel[27]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_28, m_chkChannel[28]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_29, m_chkChannel[29]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_30, m_chkChannel[30]);
	DDX_Control(pDX, IDC_CHECK_CHANNEL_INDEX_31, m_chkChannel[31]);
	DDX_Control(pDX, IDC_CHECK_QUREYAREA, m_chkQureyArea);
}


BEGIN_MESSAGE_MAP(CLS_VCASmartSearch, CDialog)
	ON_BN_CLICKED(IDC_BTN_VCA_SEARCH_EXPORT, &CLS_VCASmartSearch::OnBnClickedBtnVcaSearchExport)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_CHECK_QUREYAREA, &CLS_VCASmartSearch::OnBnClickedCheckQureyarea)
END_MESSAGE_MAP()


// CLS_VCASmartSearch message handler


BOOL CLS_VCASmartSearch::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VCASmartSearch::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	// TODO: add message handler code here
}

void CLS_VCASmartSearch::OnLanguageChanged( int _iLanguage )
{
	UpdateUIText();
}

void CLS_VCASmartSearch::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = (_iChannelNo < 0) ? FLAG_QUERY_ALL_CHANNEL : _iChannelNo;
	m_iStreamNo = _iStreamNo;
}


void CLS_VCASmartSearch::UpdateUIText()
{
	SetDlgItemTextEx(IDC_STC_VCA_SEARCH_STATEMENT_TYPE, IDS_VCA_STATEMENT_TYPE);
	SetDlgItemTextEx(IDC_STC_VCA_SEARCH_STATISTICS_TIME, IDS_VCA_STATISTICS_TIME);
	SetDlgItemTextEx(IDC_BTN_VCA_SEARCH_EXPORT, IDS_VCA_EXPORT);
	SetDlgItemText(IDC_CHECK_CHANNEL_LIST, GetTextByLan(_T("通道列表"), _T("chanList")));
	SetDlgItemText(IDC_STATIC_0_7, GetTextByLan(_T("通道0-7"), _T("ch0-7")));
	SetDlgItemText(IDC_STATIC_8_15, GetTextByLan(_T("通道8-15"), _T("ch8-15")));
	SetDlgItemText(IDC_STATIC_16_23, GetTextByLan(_T("通道16-23"), _T("ch16-23")));
	SetDlgItemText(IDC_STATIC_24_31, GetTextByLan(_T("通道24-31"), _T("ch24-31")));

	m_cboStatementType.ResetContent();
	const CString cstType[] = {
		GetTextEx(IDS_VCA_STATEMENT_TYPE_DAY),
		GetTextEx(IDS_VCA_STATEMENT_TYPE_WEEK),
		GetTextEx(IDS_VCA_STATEMENT_TYPE_MONTH),
		GetTextEx(IDS_VCA_STATEMENT_TYPE_YEAR)
	};
	int iIndex = 0;
	iIndex = m_cboStatementType.AddString(cstType[0]);
	m_cboStatementType.SetItemData(iIndex, REPORT_FORM_TYPE_HOUR);
	iIndex = m_cboStatementType.AddString(cstType[1]);
	m_cboStatementType.SetItemData(iIndex, REPORT_FORM_TYPE_DAY);
	iIndex = m_cboStatementType.AddString(cstType[2]);
	m_cboStatementType.SetItemData(iIndex, REPORT_FORM_TYPE_MONTH);
	iIndex = m_cboStatementType.AddString(cstType[3]);
	m_cboStatementType.SetItemData(iIndex, REPORT_FORM_TYPE_YEAR);
	m_cboStatementType.SetCurSel(0);

	m_lstctrlStatement.DeleteAllItems();
	for(int i = E_STATEMENT_SUM - 1; i >= 0; i--)
	{
		m_lstctrlStatement.DeleteColumn(i);
	}
	m_lstctrlStatement.InsertColumn(E_STATEMENT_FIRST, GetTextEx(IDS_CONFIG_DNVR_ALMSCH_CHANNELNO), LVCFMT_CENTER, 110);
	m_lstctrlStatement.InsertColumn(E_STATEMENT_SECOND, GetTextEx(IDS_VCA_PUSH_PERSON_NUM), LVCFMT_CENTER, 110);
	m_lstctrlStatement.InsertColumn(E_STATEMENT_THIRD, GetTextEx(IDS_VCA_POP_PERSON_NUM), LVCFMT_CENTER, 110);
	m_lstctrlStatement.InsertColumn(E_STATEMENT_FOUTH, GetTextEx(IDS_VCA_OCCUR_TIME), LVCFMT_CENTER, 130);

	m_lstctrlStatement.SetExtendedStyle(LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT|LVS_EX_CHECKBOXES);	
}

void CLS_VCASmartSearch::OnBnClickedBtnVcaSearchExport()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCASmartSearch::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNo);
	}

	//Get the current time, according to the report type, construct the start and end time
	COleDateTime time;
	m_dtpStatTime.GetTime(time);
	CTimeSpan ts(0, 0, 0, 1); //n= a certain number of days

	QueryReportForm ReportPara;
	memset(&ReportPara, 0, sizeof(QueryReportForm));
	ReportPara.iSize = sizeof(ReportPara);
	int iType = (int)m_cboStatementType.GetItemData(m_cboStatementType.GetCurSel());
    ReportPara.iFormType = iType;

    if (BST_UNCHECKED == m_chkChannelListEnable.GetCheck())
    {
        if (STATEMENT_TYPE_WEEK == iType)
            iType = STATEMENT_TYPE_DAY;
        else if (STATEMENT_TYPE_DAY == iType)
            iType = STATEMENT_TYPE_HOUR;
    }

	// annual report
	if (STATEMENT_TYPE_YEAR == iType)
	{    
		ReportPara.tBeginTime.iYear = time.GetYear();
		ReportPara.tBeginTime.iMonth = 1;
		ReportPara.tBeginTime.iDay = 1;
		ReportPara.tBeginTime.iHour = 0;
		ReportPara.tBeginTime.iMinute = 0;
		ReportPara.tBeginTime.iSecond = 0;

		ReportPara.tEndTime.iYear = time.GetYear();
		ReportPara.tEndTime.iMonth = 12;
		ReportPara.tEndTime.iDay = 31;
		ReportPara.tEndTime.iHour = 23;
		ReportPara.tEndTime.iMinute = 59;
		ReportPara.tEndTime.iSecond = 59;
	}
	// monthly report
	else if (STATEMENT_TYPE_MONTH == iType)
	{
		ReportPara.tBeginTime.iYear = time.GetYear();
		ReportPara.tBeginTime.iMonth = time.GetMonth();
		ReportPara.tBeginTime.iDay = 1;
		ReportPara.tBeginTime.iHour = 0;
		ReportPara.tBeginTime.iMinute = 0;
		ReportPara.tBeginTime.iSecond = 0;

		//Special handling for December
		int iYear = time.GetYear();
		int iMonth = time.GetMonth();
		if (time.GetMonth() < 12)
		{
			iMonth += 1; 
		}
		else
		{
			iYear += 1;
			iMonth = 1;
		}
		CTime temptime(iYear,iMonth,1,0,0,0);
		temptime = temptime - ts;

		ReportPara.tEndTime.iYear = temptime.GetYear();
		ReportPara.tEndTime.iMonth = temptime.GetMonth();
		ReportPara.tEndTime.iDay = temptime.GetDay();
		ReportPara.tEndTime.iHour = temptime.GetHour();
		ReportPara.tEndTime.iMinute = temptime.GetMinute();
		ReportPara.tEndTime.iSecond = temptime.GetSecond();
	}
	// Weekly Report
	else if (STATEMENT_TYPE_WEEK == iType)
	{
		CTime MyStartTime(time.GetYear(),time.GetMonth(),time.GetDay(),0,0,0);
		int iIndexofweek = time.GetDayOfWeek();
		//use Monday as the start of the week
		iIndexofweek = (iIndexofweek == 1) ? 6 : (iIndexofweek - 2);
		CTimeSpan wkts(iIndexofweek, 0, 0, 0);
		MyStartTime = MyStartTime - wkts; 

		ReportPara.tBeginTime.iYear   = MyStartTime.GetYear();
		ReportPara.tBeginTime.iMonth  = MyStartTime.GetMonth();
		ReportPara.tBeginTime.iDay    = MyStartTime.GetDay();
		ReportPara.tBeginTime.iHour   = 0;
		ReportPara.tBeginTime.iMinute = 0;
		ReportPara.tBeginTime.iSecond = 0;

		CTimeSpan endts(-7,0,0,1);
		MyStartTime = MyStartTime - endts;
		ReportPara.tEndTime.iYear   = MyStartTime.GetYear();
		ReportPara.tEndTime.iMonth  = MyStartTime.GetMonth();
		ReportPara.tEndTime.iDay    = MyStartTime.GetDay();
		ReportPara.tEndTime.iHour   = MyStartTime.GetHour();
		ReportPara.tEndTime.iMinute = MyStartTime.GetMinute();
		ReportPara.tEndTime.iSecond = MyStartTime.GetSecond();
	}
	// daily report
	else if (STATEMENT_TYPE_DAY == iType)
	{
		ReportPara.tBeginTime.iYear   = time.GetYear();
		ReportPara.tBeginTime.iMonth  = time.GetMonth();
		ReportPara.tBeginTime.iDay    = time.GetDay();
		ReportPara.tBeginTime.iHour   = 0;
		ReportPara.tBeginTime.iMinute = 0;
		ReportPara.tBeginTime.iSecond = 0;

		ReportPara.tEndTime.iYear   = time.GetYear();
		ReportPara.tEndTime.iMonth  = time.GetMonth();
		ReportPara.tEndTime.iDay    = time.GetDay();
		ReportPara.tEndTime.iHour   = 23;
		ReportPara.tEndTime.iMinute = 59;
		ReportPara.tEndTime.iSecond = 59;
	}
	else if (STATEMENT_TYPE_HOUR == iType)
    {
        ReportPara.tBeginTime.iYear   = time.GetYear();
        ReportPara.tBeginTime.iMonth  = time.GetMonth();
        ReportPara.tBeginTime.iDay    = time.GetDay();
        ReportPara.tBeginTime.iHour   = time.GetHour();
        ReportPara.tBeginTime.iMinute = 0;
        ReportPara.tBeginTime.iSecond = 0;

        ReportPara.tEndTime.iYear   = time.GetYear();
        ReportPara.tEndTime.iMonth  = time.GetMonth();
        ReportPara.tEndTime.iDay    = time.GetDay();
        ReportPara.tEndTime.iHour   = time.GetHour();
        ReportPara.tEndTime.iMinute = 59;
        ReportPara.tEndTime.iSecond = 59;
    }
    else
	{
		return;
	}

    if (m_chkChannelListEnable.GetCheck() == BST_CHECKED)
    {
        ReportPara.iType = 1;
        for (int i=0; i<32; i++)
        {
            if (m_chkChannel[i].GetCheck()== BST_CHECKED)
            {
                ReportPara.iChanList[ReportPara.iChanCount] = i;
                ReportPara.iChanCount++;
            }
        }
		if(m_chkQureyArea.GetCheck()==BST_CHECKED)
		{
			ReportPara.iQueryType = QUERY_TYPE_AREA;
		}
		else
		{
			ReportPara.iQueryType = QUERY_TYPE_CHANNEL;
		}
    }

	// send command, get statistics
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_QUERY_REPORT, m_iChannelNo, &ReportPara, sizeof(ReportPara));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCASmartSearch::NetClient_SendCommand[COMMAND_ID_QUERY_REPORT] (%d, %d), error (%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCASmartSearch::NetClient_SendCommand[COMMAND_ID_QUERY_REPORT] (%d, %d)", m_iLogonID, m_iChannelNo);
	}
}

void CLS_VCASmartSearch::WriteResult(ReportFormResult _ReportResult)
{
	int iCurrentNum = _ReportResult.iCurrentNum;
	if (iCurrentNum < 0 || iCurrentNum > MAX_ONCE_FORM_REPORT_NUM)
	{
		return;
	}
	else
	{
		for (int i = 0; i < iCurrentNum; i++)
		{
			int iItemCount = m_lstctrlStatement.GetItemCount();
			int iColumn = 0;

			CString cstChannelNo = "";
			cstChannelNo.Format(_T("%d"), _ReportResult.tReport[i].iChannelNo + 1);

			CString cstPushPersonNum = "";
			cstPushPersonNum.Format(_T("%d"), _ReportResult.tReport[i].iPushPersonNum);

			CString cstPopPersonNum = "";
			cstPopPersonNum.Format(_T("%d"), _ReportResult.tReport[i].iPopPersonNum);

			CString cstTime = "";
			cstTime = FormatTime(_ReportResult.iFormType, _ReportResult.tReport[i].tOccurTime);
            m_lstctrlStatement.InsertItem(iItemCount, cstChannelNo);
			m_lstctrlStatement.SetItemText(iItemCount, ++iColumn, cstPushPersonNum);
			m_lstctrlStatement.SetItemText(iItemCount, ++iColumn, cstPopPersonNum);
			m_lstctrlStatement.SetItemText(iItemCount, ++iColumn, cstTime);
		}
	}
}

CString CLS_VCASmartSearch::FormatTime(int _iType, NVS_FILE_TIME _Time)
{
	CString cstTime = "";
	if (_iType == REPORT_FORM_TYPE_MONTH)
	{
		cstTime.Format(_T("%04d%s%02d%s%02d"),_Time.iYear,GetText(IDS_VCA_YEAR),_Time.iMonth,GetText(IDS_VCA_MONTH), _Time.iDay);
	}
	else if (_iType == REPORT_FORM_TYPE_DAY)
	{
		cstTime.Format(_T("%04d/%02d/%02d"),_Time.iYear,_Time.iMonth,_Time.iDay);
	}
	else if (_iType == REPORT_FORM_TYPE_HOUR)
	{
		cstTime.Format(_T("%04d/%02d/%02d %02d:%02d:%02d"),_Time.iYear,_Time.iMonth,
			_Time.iDay,_Time.iHour + 1,_Time.iMinute,_Time.iSecond);
	}
	else
	{
		cstTime.Format(_T("%04d/%02d/%02d %02d:%02d:%02d"),_Time.iYear,_Time.iMonth,
			_Time.iDay,_Time.iHour + 1,_Time.iMinute,_Time.iSecond);
	}
	return cstTime;
}

void CLS_VCASmartSearch::OnMainNotify(int _ulLogonID, int _iWparam, void* _iLParam, void* _iUser)
{
	if (_ulLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCASmartSearch::Invalid logon id(%d)",_ulLogonID);
		return;
	}

	int iMessage = _iWparam;
	switch (iMessage)
	{
	case WCM_QUERY_REPORT_FORM:
		{
			ReportFormResult tReportResult;
			memset(&tReportResult, 0, sizeof(ReportFormResult));
            if (m_chkChannelListEnable.GetCheck() == BST_CHECKED)
            {
                tReportResult.iType = 1;
            }
			if(m_chkQureyArea.GetCheck()== BST_CHECKED)
			{
				
			}
			int iRet = NetClient_RecvCommand(m_iLogonID, COMMAND_ID_QUERY_REPORT, m_iChannelNo, &tReportResult, sizeof(tReportResult));
			if (iRet < 0)
			{
				AddLog(LOG_TYPE_FAIL,"","CLS_VCASmartSearch::NetClient_RecvCommand[COMMAND_ID_QUERY_REPORT] (%d, %d), error (%d)", m_iLogonID, m_iChannelNo, GetLastError());
			}
			else
			{
				WriteResult(tReportResult);
				AddLog(LOG_TYPE_SUCC,"","CLS_VCASmartSearch::NetClient_RecvCommand[COMMAND_ID_QUERY_REPORT] (%d, %d)", m_iLogonID, m_iChannelNo);
			}
		}
		break;
	default:
		break;
	}
}


void CLS_VCASmartSearch::OnBnClickedCheckQureyarea()
{
	// TODO: Add your control notification handler code here
	if(m_chkQureyArea.GetCheck())
	{
		m_chkChannelListEnable.SetCheck(BST_CHECKED);
		m_chkChannelListEnable.EnableWindow(FALSE);

		SetDlgItemText(IDC_CHECK_CHANNEL_LIST, GetTextByLan(_T("区域列表"), _T("AreaList")));
		SetDlgItemText(IDC_STATIC_0_7, GetTextByLan(_T("区域0-7"), _T("area0-7")));
		SetDlgItemText(IDC_STATIC_8_15, GetTextByLan(_T("区域8-15"), _T("area8-15")));
		SetDlgItemText(IDC_STATIC_16_23, GetTextByLan(_T("区域16-23"), _T("area16-23")));
		SetDlgItemText(IDC_STATIC_24_31, GetTextByLan(_T("区域24-31"), _T("area24-31")));
	}else{
		
		m_chkChannelListEnable.EnableWindow(TRUE);

		SetDlgItemText(IDC_CHECK_CHANNEL_LIST, GetTextByLan(_T("通道列表"), _T("chanList")));
		SetDlgItemText(IDC_STATIC_0_7, GetTextByLan(_T("通道0-7"), _T("ch0-7")));
		SetDlgItemText(IDC_STATIC_8_15, GetTextByLan(_T("通道8-15"), _T("ch8-15")));
		SetDlgItemText(IDC_STATIC_16_23, GetTextByLan(_T("通道16-23"), _T("ch16-23")));
		SetDlgItemText(IDC_STATIC_24_31, GetTextByLan(_T("通道24-31"), _T("ch24-31")));
	}
}

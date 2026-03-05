// DlgCfgFluxStatistic.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgCfgFluxStatistic.h"


// CLS_DlgCfgFluxStatistic dialog

IMPLEMENT_DYNAMIC(CLS_DlgCfgFluxStatistic, CDialog)

CLS_DlgCfgFluxStatistic::CLS_DlgCfgFluxStatistic(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgCfgFluxStatistic::IDD, pParent)
	, m_iTotalNum(0)
{

}

CLS_DlgCfgFluxStatistic::~CLS_DlgCfgFluxStatistic()
{
}

void CLS_DlgCfgFluxStatistic::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_FLUXSTATISTIC_INFO, m_lstFluxStatistic);
	DDX_Control(pDX, IDC_DATETIMEPICKER_StartTime, m_dtcStartTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER_EndTime, m_dtcEndTime);
	DDX_Text(pDX, IDC_STATIC_TOTAL, m_iTotalNum);
}


BEGIN_MESSAGE_MAP(CLS_DlgCfgFluxStatistic, CDialog)
	ON_NOTIFY(NM_DBLCLK, IDC_LIST_FLUXSTATISTIC_INFO, &CLS_DlgCfgFluxStatistic::OnNMDblclkListFluxstatisticInfo)
	ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_FLUXSTATISTIC_INFO, &CLS_DlgCfgFluxStatistic::OnLvnItemchangedListFluxstatisticInfo)
	ON_BN_CLICKED(IDC_BUTTON_QUERY, &CLS_DlgCfgFluxStatistic::OnBnClickedButtonQuery)
END_MESSAGE_MAP()


// CLS_DlgCfgFluxStatistic message handlers

BOOL CLS_DlgCfgFluxStatistic::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_lstFluxStatistic.SetExtendedStyle(m_lstFluxStatistic.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstFluxStatistic.DeleteAllItems();

	m_dtcStartTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_dtcEndTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	CTime SystemTime; 
	m_dtcStartTime.GetTime(SystemTime);
	CTime BeginTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 0, 0, 0);
	m_dtcStartTime.SetTime(&BeginTime);

	UI_UpdateText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_DlgCfgFluxStatistic::UI_UpdateText()
{
	int iColumn = 0;
	InsertColumn(m_lstFluxStatistic, iColumn++, "", LVCFMT_CENTER, 0);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("进入"), _T("In")), LVCFMT_CENTER, 40);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("离开"), _T("out")), LVCFMT_CENTER, 40);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("进入差值"), _T("InDiff")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("离开差值"), _T("OutDiff")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("路过"), _T("Pass")), LVCFMT_CENTER, 40);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("区域"), _T("Region")), LVCFMT_CENTER, 40);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("停留"), _T("Stay")), LVCFMT_CENTER, 40);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("报警计数"), _T("AlarmCount")), LVCFMT_CENTER, 80);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("离线数据"), _T("OfflineData")), LVCFMT_CENTER, 80);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("编号"), _T("No")), LVCFMT_CENTER, 60);
	InsertColumn(m_lstFluxStatistic, iColumn++, GetTextByLan(_T("变化时间"), _T("change time")), LVCFMT_CENTER, 160);

	SetDlgItemText(IDC_STATIC_STARTTIME, GetTextByLan(_T("起始时间"), _T("Start Time")));
	SetDlgItemText(IDC_STATIC_ENDTIME, GetTextByLan(_T("结束时间"), _T("End Time")));
	SetDlgItemText(IDC_BUTTON_QUERY, GetTextByLan(_T("查询"), _T("Query")));
	SetDlgItemText(IDC_STATIC_INFO, GetTextByLan(_T("离线数据查询"), _T("Offline Data Query")));


}

void CLS_DlgCfgFluxStatistic::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateText();
}

void CLS_DlgCfgFluxStatistic::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
}

void CLS_DlgCfgFluxStatistic::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (m_iLogonID < 0 || m_iLogonID != _iLogonID)
	{
		return;
	}

	if (_iChannelNo != m_iChannelNo)//Only refresh the channel whose parameter has changed
	{
		return;
	}

	switch(_iParaType)
	{
	case PARA_VCA_ALARMSTAT:
		{
			STR_Para* _strPara;
			_strPara = (STR_Para*) _pPara;

			StaticData *ptStaticData = new StaticData;
			if(NULL!=ptStaticData)
			{
				ptStaticData->iIn = (int)(long)_strPara->m_iPara[0];
				ptStaticData->iOut = (int)(long)_strPara->m_iPara[1];
				ptStaticData->iInDiff = (int)(long)_strPara->m_iPara[2];
				ptStaticData->iOutDiff = (int)(long)_strPara->m_iPara[3];
				ptStaticData->iPass = (int)(long)_strPara->m_iPara[4];
				ptStaticData->iRegion = (int)(long)_strPara->m_iPara[5];
				ptStaticData->iStay = (int)(long)_strPara->m_iPara[6];
				ptStaticData->iAlarmCount = (int)(long)_strPara->m_iPara[7];
				ptStaticData->iIsOfflineData = (int)(long)_strPara->m_iPara[8];
				ptStaticData->iNo = (int)(long)_strPara->m_iPara[9];
				ptStaticData->iTime = (int)(long)_strPara->m_iPara[10];
				ptStaticData->iuTime = (int)(long)_strPara->m_iPara[11];
				m_vStaticData.push_back(ptStaticData);
				//一次性收全再显示，不然容易丢失数据
				//正常数据直接显示
				if(m_iTotalNum == ptStaticData->iNo || 0 == ptStaticData->iNo)
				{
					ShowList();
				}
			}
		}
		break;
	default:
		break;
	}
}

void CLS_DlgCfgFluxStatistic::OnNMDblclkListFluxstatisticInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	// TODO: Add your control notification handler code here
	m_lstFluxStatistic.DeleteAllItems();
	*pResult = 0;
}

void CLS_DlgCfgFluxStatistic::OnLvnItemchangedListFluxstatisticInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
	// TODO: Add control notification handler code here
	*pResult = 0;
}

void CLS_DlgCfgFluxStatistic::OnBnClickedButtonQuery()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	VcaOfflineQueryData tInfo= {0};
	VcaOfflineQueryData tInfoResult= {0};

	CTime StartTime;
	CTime EndTime;
	m_dtcStartTime.GetTime(StartTime);
	m_dtcEndTime.GetTime(EndTime);

	tInfo.iBeginTime = (int)StartTime.GetTime();
	tInfo.iEndTime = (int)EndTime.GetTime();

	int iRetValue = NetClient_CmdConfig(m_iLogonID, CMD_VCAOFFLINE_QUERYDATA, m_iChannelNo, &tInfo, sizeof(tInfo), &tInfoResult, sizeof(tInfoResult));
	if(RET_SUCCESS == iRetValue)
	{
		m_iTotalNum = tInfoResult.iTotal;
		AddLog(LOG_TYPE_SUCC,"","CLS_DlgCfgFluxStatistic::NetClient_CmdConfig[CMD_VCAOFFLINE_QUERYDATA] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());

	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCfgFluxStatistic::NetClient_CmdConfig[CMD_VCAOFFLINE_QUERYDATA] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
		m_iTotalNum = 0;
	}

	UpdateData(FALSE);
}

void CLS_DlgCfgFluxStatistic::ShowList()
{
	// TODO: Add your message handler code here and/or call default
	m_lstFluxStatistic.SetRedraw(FALSE);
	std::vector<StaticData*>::iterator pos = m_vStaticData.begin();
	for ( ; pos != m_vStaticData.end(); pos++)
	{
		if(NULL == *pos)
		{
			continue;
		}

		StaticData &tStaticData = **pos;
		CString szFluxIn;
		szFluxIn.Format("%d", tStaticData.iIn);
		CString szFluxOut;
		szFluxOut.Format("%d", tStaticData.iOut);
		CString szInDiff;
		szInDiff.Format("%d", tStaticData.iInDiff);
		CString szOutDiff;
		szOutDiff.Format("%d", tStaticData.iOutDiff);
		CString szPass;
		szPass.Format("%d", tStaticData.iPass);
		CString szRegion;
		szRegion.Format("%d", tStaticData.iRegion);
		CString szStay;
		szStay.Format("%d", tStaticData.iStay);
		CString szAlarmCount;
		szAlarmCount.Format("%d", tStaticData.iAlarmCount);

		CString szIsOfflineData;
		szIsOfflineData.Format("%d",tStaticData.iIsOfflineData);
		CString szNo;
		szNo.Format("%d", tStaticData.iNo);
		CTime tTime(tStaticData.iTime);
		CString szTime;
		szTime.Format("%s.%d",tTime.Format("%Y-%m-%d %H:%M:%S").GetBuffer(0),tStaticData.iuTime);
		int iItemCount = m_lstFluxStatistic.GetItemCount();
		int iColumn = 0;
		m_lstFluxStatistic.InsertItem(iItemCount, "");
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, "");
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szFluxIn);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szFluxOut);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szInDiff);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szOutDiff);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szPass);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szRegion);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szStay);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szAlarmCount);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szIsOfflineData);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szNo);
		m_lstFluxStatistic.SetItemText(iItemCount, iColumn++, szTime);

		delete *pos;
	}
	m_vStaticData.clear();
	m_lstFluxStatistic.SetRedraw(TRUE);
}

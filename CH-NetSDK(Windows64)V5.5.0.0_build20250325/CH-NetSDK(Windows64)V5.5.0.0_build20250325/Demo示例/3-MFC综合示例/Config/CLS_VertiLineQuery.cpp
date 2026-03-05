// CLS_VertiLineQuery.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_VertiLineQuery.h"
#include "./Playback/FilePlayReviewPage.h"
#define MAX_VERTICAL_LINE_NUM		101
#define MAX_COEF_SIZE				20

// CLS_VertiLineQuery dialog

IMPLEMENT_DYNAMIC(CLS_VertiLineQuery, CDialog)

CLS_VertiLineQuery::CLS_VertiLineQuery(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VertiLineQuery::IDD, pParent)
	, m_iCurPage(0)
{
	memset(&m_tResult, 0, sizeof(CofeInfo) * MAX_COFE_NUM * MAX_QUERY_PAGE_COUNT);
	memset(&m_iCoefNumArr, 0, sizeof(m_iCoefNumArr));
}

CLS_VertiLineQuery::~CLS_VertiLineQuery()
{
}

BOOL CLS_VertiLineQuery::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	for(int i = 0; i < MAX_SCENE_NUM; i++) {
		CString str;
		str.Format("%d", i);
		m_cboSceneID.AddString(str);
	}

	for(int i = 0; i < MAX_PAGE_SIZE; i++) {
		CString str;
		str.Format("%d", i + 1);
		m_cboNumInPage.AddString(str);
	}

	for(int i = 0; i < MAX_VERTICAL_LINE_NUM; i++) {
		CString str;
		str.Format("%d", i + 1);
		m_cboStartNum.AddString(str);
	}

	m_listCatalogue.SetExtendedStyle(LVS_EX_FULLROWSELECT|LVS_EX_GRIDLINES);

	m_cboStartNum.SetCurSel(0);
	m_cboSceneID.SetCurSel(0);
	m_cboNumInPage.SetCurSel(19);
	SetDlgItemInt(IDC_EDIT_PAGE, 0);
	UI_UpdateUIText();
	OnBnClickedButtonQuery();
	return TRUE;
}

void CLS_VertiLineQuery::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	OnBnClickedButtonQuery();
}

void CLS_VertiLineQuery::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_SCENE_ID, GetTextByLan(_T("场景ID"), _T("Scene ID")));
	SetDlgItemText(IDC_STATIC_PAGE, GetTextByLan(_T("页码"), _T("Page")));
	SetDlgItemText(IDC_STATIC_NUM_INPAGE, GetTextByLan(_T("每页条数"), _T("Number Per Page")));
	SetDlgItemText(IDC_STATIC_START_NUM, GetTextByLan(_T("开始序号"), _T("Start Number")));
	SetDlgItemText(IDC_STATIC_DETAIL, GetTextByLan(_T("详细信息"), _T("Detail Info")));

	SetDlgItemText(IDC_BUTTON_QUERY, GetTextByLan(_T("获取"), _T("Get")));

	while(m_listCatalogue.DeleteColumn(0));
	m_listCatalogue.InsertColumn(0, GetTextByLan(_T("保留"), _T("Retain")), LVCFMT_CENTER, 0);
	m_listCatalogue.InsertColumn(1, GetTextByLan(_T("垂线号"), _T("Vertical line number")), LVCFMT_CENTER, 60);
	m_listCatalogue.InsertColumn(2, GetTextByLan(_T("起点距(m)"), _T("Start point distance(m)")), LVCFMT_CENTER, 90);
	m_listCatalogue.InsertColumn(3, GetTextByLan(_T("河底高程(m)"), _T("River bottom elevation(m)")), LVCFMT_CENTER, 90);
	m_listCatalogue.InsertColumn(4, GetTextByLan(_T("实测时间"), _T("Measured time")), LVCFMT_CENTER, 120);
	m_listCatalogue.InsertColumn(5, GetTextByLan(_T("系数个数"), _T("Velocity coefficient Num")), LVCFMT_CENTER, 70);
	m_listCatalogue.DeleteColumn(0);

	while(m_listDetail.DeleteColumn(0));
	m_listDetail.InsertColumn(0, GetTextByLan(_T("保留"), _T("Retain")), LVCFMT_CENTER, 0);
	m_listDetail.InsertColumn(1, GetTextByLan(_T("系数编号"), _T("Velocity coefficient No")), LVCFMT_CENTER, 60);
	m_listDetail.InsertColumn(2, GetTextByLan(_T("水位值(m)"), _T("Water level value(m)")), LVCFMT_CENTER, 100);
	m_listDetail.InsertColumn(3, GetTextByLan(_T("流速系数值(m)"), _T("Velocity coefficient value(m)")), LVCFMT_CENTER, 100);
	m_listDetail.DeleteColumn(0);
}

CString CLS_VertiLineQuery::IntToFloatStr(int _iNum)
{
	float fNumTemp = 0;
	CString strNumTemp;
	fNumTemp = static_cast<float>(_iNum);
	fNumTemp = fNumTemp / 1000.0;
	strNumTemp.Format(_T("%0.3f"), fNumTemp);
	return strNumTemp;
}

void CLS_VertiLineQuery::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_CATALOG, m_listCatalogue);
	DDX_Control(pDX, IDC_LIST_DETAIL, m_listDetail);
	DDX_Control(pDX, IDC_COMBO_SCENE_ID, m_cboSceneID);
	DDX_Text(pDX, IDC_EDIT_PAGE, m_iCurPage);
	DDX_Control(pDX, IDC_COMBO_NUM_INPAGE, m_cboNumInPage);
	DDX_Control(pDX, IDC_COMBO_START_NUM, m_cboStartNum);
}


BEGIN_MESSAGE_MAP(CLS_VertiLineQuery, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_QUERY, &CLS_VertiLineQuery::OnBnClickedButtonQuery)
ON_NOTIFY(NM_CLICK, IDC_LIST_CATALOG, &CLS_VertiLineQuery::OnNMClickListCatalog)
END_MESSAGE_MAP()


// CLS_VertiLineQuery message handler

void CLS_VertiLineQuery::OnBnClickedButtonQuery()
{
	// TODO: Add control notification handler code here
	//VerTiCallIneGet
	//VerTiCallIneResult
	//CMD_VERTICALLINE_QUERY
	//NetClient_CmdConfig(int _iLogonId, int _iCmdId, int _iChanNo, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen)
	VerTiCallIneGet tInput;
	VerTiCallIneResult tResult[MAX_QUERY_PAGE_COUNT];
	memset(&tInput, 0, sizeof(VerTiCallIneGet));
	memset(&tResult, 0, sizeof(VerTiCallIneResult) * MAX_QUERY_PAGE_COUNT);
	UpdateData(TRUE);
	tInput.iSize = sizeof(VerTiCallIneGet);
	tInput.iSceneId = m_cboSceneID.GetCurSel();
	tInput.iPageNo = m_iCurPage;
	tInput.iPageSize = m_cboNumInPage.GetCurSel() + 1;
	tInput.iVLineIdStart = m_cboStartNum.GetCurSel() + 1;
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_VERTICALLINE_QUERY, m_iChannelNO, &tInput, sizeof(VerTiCallIneGet), &tResult, sizeof(VerTiCallIneResult));
	if(RET_SUCCESS == iRet)
	{
		memset(&m_iCoefNumArr, 0, sizeof(m_iCoefNumArr));
		memset(&m_tResult, 0, sizeof(m_tResult));
		for(int i =0; i < tResult[0].iPageSize && i < MAX_PAGE_SIZE; i++)		//The maximum number of entries per page does not exceed 20
		{
			m_iCoefNumArr[i] = tResult[i].iCoefNum;
			for(int j = 0; j < tResult[i].iCoefNum && j < MAX_COEF_SIZE; j++)	//The maximum number of velocity coefficients in the vertical line area is 20
			{
				m_tResult[i][j].iWaterLevel = tResult[i].tInfo[j].iWaterLevel;
				m_tResult[i][j].iCoef = tResult[i].tInfo[j].iCoef;
			}
		}
		m_listCatalogue.DeleteAllItems();
		for(int i = 0; i < tResult[0].iPageSize && i < MAX_PAGE_SIZE; i++)
		{
			CString str;
			str.Format("%d", tResult[i].iVLineId);
			m_listCatalogue.InsertItem(i, (LPCTSTR)str);
			m_listCatalogue.SetItemText(i, 1, (LPCTSTR)IntToFloatStr(tResult[i].iStartDistance));
			m_listCatalogue.SetItemText(i, 2, (LPCTSTR)IntToFloatStr(tResult[i].iBottomAltitude));
			NVS_FILE_TIME tTime;
			memset(&tTime, 0, sizeof(tTime));
			AbsSecondsToNvsFileTime(&tTime, tResult[i].iMeasuredTime);
			str.Format("%d:%d:%d:%d:%d:%d", tTime.iYear, tTime.iMonth, tTime.iDay, tTime.iHour, tTime.iMinute, tTime.iSecond);
			m_listCatalogue.SetItemText(i, 3, (LPCTSTR)str);
			str.Format("%d", tResult[i].iCoefNum);
			m_listCatalogue.SetItemText(i, 4, (LPCTSTR)str);
		}
		AddLog(LOG_TYPE_SUCC, "","CLS_VertiLineQuery::NetClient_CmdConfig[CMD_VERTICALLINE_QUERY] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VertiLineQuery::NetClient_CmdConfig[CMD_VERTICALLINE_QUERY] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_VertiLineQuery::OnNMClickListCatalog(NMHDR *pNMHDR, LRESULT *pResult)
{
	// TODO: Add control notification handler code here
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	POSITION ps;
	int iIndex;
	ps=m_listCatalogue.GetFirstSelectedItemPosition();
	if (NULL == ps)
	{
		MessageBox("Please select at least one");
		return;
	}
	iIndex = (int)m_listCatalogue.GetNextSelectedItem(ps);	//Get the line number, convert it through POSITION
	m_listDetail.DeleteAllItems();
	for(int i = 0; i < m_iCoefNumArr[iIndex]; i++)
	{
		CString str;
		str.Format("%d", i + 1);
		m_listDetail.InsertItem(i, (LPCTSTR)str);
		m_listDetail.SetItemText(i, 1, (LPCTSTR)IntToFloatStr(m_tResult[iIndex][i].iWaterLevel));
		m_listDetail.SetItemText(i, 2, (LPCTSTR)IntToFloatStr(m_tResult[iIndex][i].iCoef));
	}
	*pResult = 0;
}

// CLS_DlgFaceSearchSnap.cpp : Implementation file
//

#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceSearchSnap.h"
#include "CommonFun.h"
#include <io.h>

// CLS_DlgFaceSearchSnap Dialog

IMPLEMENT_DYNAMIC(CLS_DlgFaceSearchSnap, CDialog)

CLS_DlgFaceSearchSnap::CLS_DlgFaceSearchSnap(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceSearchSnap::IDD, pParent)
{
	m_iTaskId = -1;
	m_iDLFacePicId = -1;
	m_iQueryProcess = 0;
	memset(&m_tQueryChan, 0, sizeof(m_tQueryChan));
	m_pImageLocal = NULL;
}

CLS_DlgFaceSearchSnap::~CLS_DlgFaceSearchSnap()
{
	SAFE_DESTORY_IMAGE(m_pImageLocal);
}

void CLS_DlgFaceSearchSnap::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDT_SEARCH_SNAP_PICPATH, m_edtPicPath);
	DDX_Control(pDX, IDC_STC_SEARCH_SNAP_PICSHOW, m_stcPicShow);
	DDX_Control(pDX, IDC_SLD_SEARCH_SNAP_SIMILAR, m_sldSimlarity);
	DDX_Control(pDX, IDC_CBO_SEARCH_SNAP_SORT_MODE, m_cboSortMode);
	DDX_Control(pDX, IDC_DATETIMEPICKER_BEGIN, m_dtBegTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER_END, m_dtEndTime);
	DDX_Control(pDX, IDC_CHECK_CHAN_ALL, m_chkChanAll);
	for (int i = 0; i < LEN_32; ++i)
	{
		DDX_Control(pDX, IDC_CHECK_CHAN_1 + i, m_chkChanNo[i]);
	}
	DDX_Control(pDX, IDC_LST_SEARCH_SNAP_INFO, m_lstSearchSnapResult);
	DDX_Control(pDX, IDC_CBO_SEARCH_SNAP_CUTRET, m_cboCutResult);
	DDX_Control(pDX, IDC_STATIC_PROCESS, m_stcProcessQuery);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceSearchSnap, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_SEARCH_SNAP_PATH, &CLS_DlgFaceSearchSnap::OnBnClickedBtnSearchSnapPath)
	ON_BN_CLICKED(IDC_BTN_SEARCH_SNAP_CUT, &CLS_DlgFaceSearchSnap::OnBnClickedBtnSearchSnapCut)
	ON_BN_CLICKED(IDC_BUTTON_QUERY, &CLS_DlgFaceSearchSnap::OnBnClickedButtonQuery)
	ON_BN_CLICKED(IDC_BUTTON_PROCESS, &CLS_DlgFaceSearchSnap::OnBnClickedButtonProcess)
	ON_BN_CLICKED(IDC_BUTTON_RESULT, &CLS_DlgFaceSearchSnap::OnBnClickedButtonResult)
	ON_BN_CLICKED(IDC_CHECK_CHAN_ALL, &CLS_DlgFaceSearchSnap::OnBnClickedCheckChanAll)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_SEARCH_SNAP_SIMILAR, &CLS_DlgFaceSearchSnap::OnNMCustomdrawSldSearchSnapSimilar)
	ON_CBN_SELCHANGE(IDC_CBO_SEARCH_SNAP_CUTRET, &CLS_DlgFaceSearchSnap::OnCbnSelchangeCboSearchSnapCutret)
	ON_WM_PAINT()
END_MESSAGE_MAP()


// CLS_DlgFaceSearchSnap Message Handler
void CLS_DlgFaceSearchSnap::UI_Init()
{
	m_sldSimlarity.SetRange(0, 100);
	m_sldSimlarity.SetPos(70);
	m_sldSimlarity.SetTicFreq(1);
	
	m_cboSortMode.InsertString(0, "Capture time");
	m_cboSortMode.InsertString(1, "Similarity");
	m_cboSortMode.SetCurSel(1);

	m_dtBegTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_dtEndTime.SetFormat("yyyy-MM-dd HH:mm:ss");

	UI_InitFaceList(m_lstSearchSnapResult );
}

afx_msg void CLS_DlgFaceSearchSnap::OnShowWindow(BOOL bShow, UINT nStatus)
{
	if (bShow)
	{
		SetDlgItemText(IDC_STATIC_CUT_RESULT, "");
		for (int i = m_iChanCount; i < LEN_32; ++i)
		{
			m_chkChanNo[i].EnableWindow(FALSE);
		}
	}
}

void CLS_DlgFaceSearchSnap::UI_InitFaceList(CListCtrl& _lst)
{
	_lst.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	_lst.InsertColumn(0, "Num", LVCFMT_LEFT, 40, -1);
	_lst.InsertColumn(1, "Channel", LVCFMT_LEFT, 60, -1);
	_lst.InsertColumn(2, "Begin time", LVCFMT_LEFT, 120, -1);	
	_lst.InsertColumn(3, "End time", LVCFMT_LEFT, 120, -1);	
	_lst.InsertColumn(4, "Age", LVCFMT_LEFT, 40, -1);	
	_lst.InsertColumn(5, "Sex", LVCFMT_LEFT, 40, -1);	
	_lst.InsertColumn(6, "Nationality", LVCFMT_LEFT, 70, -1);	
	_lst.InsertColumn(7, "Wear glasses", LVCFMT_LEFT, 80, -1);
	_lst.InsertColumn(8, "Wear mask", LVCFMT_LEFT, 70, -1);
	_lst.InsertColumn(9, "Similarity", LVCFMT_LEFT, 70, -1);
	_lst.InsertColumn(10, "Big picture name", LVCFMT_LEFT, 100, -1);
	_lst.InsertColumn(11, "Small picture name", LVCFMT_LEFT, 110, -1);
}

void CLS_DlgFaceSearchSnap::OnMainNotify(int _iLogonID, int _wParam, void* _iLParam)
{
	CLS_PageBase::OnMainNotify(_iLogonID, _wParam, _iLParam);
	int iMsgType = LOWORD(_wParam);
	if (WCM_DWONLOAD_FINISHED == iMsgType || 
		WCM_DOWNLOAD_INTERRUPT == iMsgType ||
		WCM_DWONLOAD_FAULT == iMsgType) 
	{
		unsigned int uDownloadID = (unsigned int)_iLParam;
		if (m_iDLFacePicId != uDownloadID) {
			return;
		}
		NetClient_NetFileStopDownloadFile(m_iDLFacePicId);
		m_iDLFacePicId = -1;

		if (WCM_DWONLOAD_FINISHED == iMsgType) {
		//	ShowImage(GetDlgItem(IDC_STC_SEARCH_SNAP_PICSHOW), m_cstrDLPath);
			if (!m_cstrDLPath.IsEmpty())
			{	
				m_pImageLocal = LoadAndShowImage(m_cstrDLPath, GetDlgItem(IDC_STC_SEARCH_SNAP_PICSHOW));
			}
			SetDlgItemText(IDC_STATIC_CUT_RESULT, "Picture download success.");
		} else  {
			SetDlgItemText(IDC_STATIC_CUT_RESULT, "Picture download failed.");
		}	
	}
}

void CLS_DlgFaceSearchSnap::OnBnClickedBtnSearchSnapPath()
{
	OpenPicPath(m_edtPicPath);
}

void CLS_DlgFaceSearchSnap::OnBnClickedBtnSearchSnapCut()
{
	m_iTaskId = -1;
	m_cboCutResult.ResetContent();
	SetDlgItemInt(IDC_STATIC_PROCESS, 0);

	//Matting
	FaceCutEx tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iPicType = 0;	//0-jpg, 1-png
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iPageNo = 0;
	tInfo.iPageCount = FACE_MAX_PAGE_COUNT;
	m_edtPicPath.GetWindowText(tInfo.cPicPath, sizeof(tInfo.cPicPath));
	FaceCutQueryResult tResult[FACE_MAX_PAGE_COUNT] = {0};
	int iRet = FaceConfig(FACE_CMD_CUT_EX, &tInfo, sizeof(tInfo), &tResult,sizeof(FaceCutQueryResult));
	if (0 != iRet) {
		MessageBox("Face cutout failed!", "Tips", MB_OK);
		return;
	}

	//Matting result processing
	for(int i = 0; i < tResult[0].iPageCount && i < FACE_MAX_PAGE_COUNT; ++i)
	{
		m_cboCutResult.InsertString(i, tResult[i].cFileName);
	}

	if (tResult[0].iPageCount > 0)
	{
		m_iTaskId = tResult[0].iTaskId;
		m_cboCutResult.SetCurSel(0);
		OnCbnSelchangeCboSearchSnapCutret();
	}
}

//Query by Criteria
void CLS_DlgFaceSearchSnap::OnBnClickedButtonQuery()
{
	m_iQueryProcess = 0;
	m_lstSearchSnapResult.DeleteAllItems();
	SetDlgItemInt(IDC_STATIC_PROCESS, 0);

	if (m_iTaskId <= 0)
	{
		MessageBox("Please cutout first!", "Tips", MB_OK);
		return;
	}
	//Query Criteria
	FaceSearchSnap tQuery = {0};
	tQuery.iSize = sizeof(FaceSearchSnap);
	//Channel list
	memset(&m_tQueryChan, 0, sizeof(m_tQueryChan));
	tQuery.iChanCount = GetChanList(m_tQueryChan, MAX_QUERY_LIST_COUNT);
	tQuery.iChanSize = sizeof(QueryChanNo);
	tQuery.pChanList = m_tQueryChan;
	//Start End Time
	GetNvsFileTime(&m_dtBegTime, tQuery.tBegTime);
	GetNvsFileTime(&m_dtEndTime, tQuery.tEndTime);
	m_cboCutResult.GetWindowText(tQuery.cPicturePath, sizeof(tQuery.cPicturePath));
	tQuery.iSimilarity = m_sldSimlarity.GetPos();
	tQuery.iSortMode = m_cboSortMode.GetCurSel();
	tQuery.iTaskId = m_iTaskId;

	int iRet = FaceConfig(FACE_CMD_SEARCH_SNAP, &tQuery, sizeof(tQuery), NULL, 0);
	if (0 != iRet) {
		MessageBox("Start search failed!", "Tips", MB_OK);
		return;
	}
}
//Progress query
void CLS_DlgFaceSearchSnap::OnBnClickedButtonProcess()
{
	if (m_iTaskId <= 0)
	{
		MessageBox("Please cutout first!", "Tips", MB_OK);
		return;
	}
	FaceReply tOutInfo = {0};
	FaceSearchSnapProcess tInfo = {0};
	tInfo.iSize = sizeof(FaceSearchSnapProcess);
	tInfo.iTaskId = m_iTaskId;
	int iRet = FaceConfig(FACE_CMD_SEARCH_SNAP_PROCESS, &tInfo, sizeof(FaceSearchSnapProcess), &tOutInfo, sizeof(FaceReply));
	if (0 != iRet) {
		MessageBox("Progress query failed!", "Tips", MB_OK);
		return;
	}
	if(6 == tOutInfo.iResult)
	{
		m_iQueryProcess = tOutInfo.iDelLibProgress;
		SetDlgItemInt(IDC_STATIC_PROCESS, tOutInfo.iDelLibProgress);
	}
}
//Get Results
void CLS_DlgFaceSearchSnap::OnBnClickedButtonResult()
{
	m_lstSearchSnapResult.DeleteAllItems();
	if (m_iTaskId <= 0)
	{
		MessageBox("Please cutout first!", "Tips", MB_OK);
		return;
	}

	if(100 != m_iQueryProcess)
	{
		MessageBox("Search is not complete!", "Tips", MB_OK);
		return;
	}
	
	FaceSearchSnapQuery tInfo = {0};
	tInfo.iSize = sizeof(FaceSearchSnapQuery);
	tInfo.iTaskId = m_iTaskId;
	tInfo.iPageSize = MAX_QUERY_PAGE_COUNT;
	tInfo.iPageNo = 0;

	FaceSearchSnapResult tOutInfo[MAX_QUERY_PAGE_COUNT] = {0};
	int iRet = FaceConfig(FACE_CMD_SEARCH_SNAP_RESULT, &tInfo, sizeof(tInfo), &tOutInfo, sizeof(FaceSearchSnapResult));
	if (0 != iRet) {
		MessageBox("The result query failed!", "Tips", MB_OK);
		return;
	}

	for (int i = 0; i < tOutInfo[0].iCurPageCount && i < MAX_QUERY_PAGE_COUNT; ++i)
	{
		UI_UpdateList(tOutInfo[i]);	
	}	

	int iPageCount = tOutInfo[0].iTotal/FACE_MAX_PAGE_COUNT;
	if (0 == iPageCount%FACE_MAX_PAGE_COUNT) {
		iPageCount++;
	}

	for (int iPageNo = 1; iPageNo < iPageCount; ++iPageNo) {
		tInfo.iPageNo = iPageNo;
		FaceSearchSnapResult tOutInfo[MAX_QUERY_PAGE_COUNT] = {0};
		int iRet = FaceConfig(FACE_CMD_SEARCH_SNAP_RESULT, &tInfo, sizeof(tInfo), &tOutInfo, sizeof(FaceSearchSnapResult));
		if (0 == iRet) {
			for (int i = 0; i < tOutInfo[0].iCurPageCount && i < MAX_QUERY_PAGE_COUNT; ++i){
				UI_UpdateList(tOutInfo[i]);	
			}
		}
	}
}

void CLS_DlgFaceSearchSnap::UI_UpdateList(FaceSearchSnapResult &_tResult)
{
	if (_tResult.iSize <= 0)
	{
		return;
	}

	int iItem = m_lstSearchSnapResult.GetItemCount();
	m_lstSearchSnapResult.InsertItem(iItem, "");

	CString cstrSex = "Unknown ";
	if (1 == _tResult.iSex){
		cstrSex = "Man";
	} else if (2 == _tResult.iSex){
		cstrSex = "Woman";
	}

	m_lstSearchSnapResult.SetItemText(iItem, 0, IntToStr(iItem + 1));
	m_lstSearchSnapResult.SetItemText(iItem, 1, IntToStr(_tResult.iChanNo));
	m_lstSearchSnapResult.SetItemText(iItem, 2, GetTimeStr(_tResult.tBegTime));
	m_lstSearchSnapResult.SetItemText(iItem, 3, GetTimeStr(_tResult.tEndTime));
	m_lstSearchSnapResult.SetItemText(iItem, 4, IntToStr(_tResult.iAge));
	m_lstSearchSnapResult.SetItemText(iItem, 5, cstrSex);
	m_lstSearchSnapResult.SetItemText(iItem, 6, 1 == _tResult.iNation ? "Han" : "Minority");
	m_lstSearchSnapResult.SetItemText(iItem, 7, 1 == _tResult.iWearGlasses ? "Yes" : "No");
	m_lstSearchSnapResult.SetItemText(iItem, 8, 1 == _tResult.iWearMask ? "Yes" : "No");
	m_lstSearchSnapResult.SetItemText(iItem, 9, IntToStr(_tResult.iSimilarity));
	m_lstSearchSnapResult.SetItemText(iItem, 10, _tResult.tPicSnap.cFileName);
	m_lstSearchSnapResult.SetItemText(iItem, 11, _tResult.tPicNeg.cFileName);
}

int CLS_DlgFaceSearchSnap::GetChanList(QueryChanNo* _pList, int _iMaxCount)
{
	int iCount = 0;
	if (m_chkChanAll.GetCheck())
	{
		iCount = 1;
		_pList->iChanNo = 0x7FFFFFFF;
	}
	else
	{
		for (int i = 0; i < LEN_32; ++i)
		{
			if (m_chkChanNo[i].GetCheck())
			{
				iCount++;
				_pList->iChanNo = i;
				_pList++;
			}
		}
	}
	return iCount;
}

void CLS_DlgFaceSearchSnap::OnBnClickedCheckChanAll()
{
	int iEnable = m_chkChanAll.GetCheck();
	for (int i = 0; i < LEN_32 && i < m_iChanCount; ++i)
	{
		m_chkChanNo[i].SetCheck(iEnable);
		m_chkChanNo[i].EnableWindow(!iEnable);
	}
}

void CLS_DlgFaceSearchSnap::OnNMCustomdrawSldSearchSnapSimilar(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_SIMILARITY, m_sldSimlarity.GetPos());
	*pResult = 0;
}

void CLS_DlgFaceSearchSnap::OnCbnSelchangeCboSearchSnapCutret()
{
	SAFE_DESTORY_IMAGE(m_pImageLocal);
	int iSelPicName = m_cboCutResult.GetCurSel();
	if (iSelPicName < 0){
		return;
	}
	CString cstrName;
	m_cboCutResult.GetWindowText(cstrName);
	CString cstrPath = cstrName;
	cstrPath.Replace("/", "_");
	cstrPath = GetCurModulePath() + cstrPath;

	if (-1 != m_iDLFacePicId)
	{
		MessageBox("In the picture download, try again later!", "Tips", MB_OK);
		return;
	}
	m_cstrDLPath.Empty();

	//Determine whether the local picture exists
	if (0 == _access(cstrPath, 0))
	{	//Direct display exists
		SetDlgItemText(IDC_STATIC_CUT_RESULT, "");
		if (!cstrPath.IsEmpty())
		{	
			m_pImageLocal = LoadAndShowImage(cstrPath, GetDlgItem(IDC_STC_SEARCH_SNAP_PICSHOW));
		}
	}
	else
	{	//The local image does not exist. Download it from the device
		DOWNLOAD_FILE tDL = {0};
		tDL.m_iSize = sizeof(tDL);
		strcpy_s(tDL.m_cRemoteFilename, cstrName.GetLength()+1, (char*)(LPCSTR)cstrName);
		strcpy_s(tDL.m_cLocalFilename, cstrPath.GetLength()+1, (char*)(LPCSTR)cstrPath);
		tDL.m_iReqMode = 0;		//Mandatory flow mode
		tDL.m_iSpeed = 32;		//Maximum speed download
		unsigned int uDLId = -1; 
		int iRet = NetClient_NetFileDownload(&uDLId, m_iLogonID, DOWNLOAD_CMD_FILE, &tDL, sizeof(tDL));	
		if (0 == iRet)
		{
			SetDlgItemText(IDC_STATIC_CUT_RESULT, "Picture download...");
			m_iDLFacePicId = uDLId;
			m_cstrDLPath = cstrPath;
		}
		else
		{
			SetDlgItemText(IDC_STATIC_CUT_RESULT, "Picture download failed.");
		}
	}
}

void CLS_DlgFaceSearchSnap::OnPaint()
{
	CPaintDC dc(this); // device context for painting
	if (NULL != m_pImageLocal)
	{
		ShowImage(m_pImageLocal, GetDlgItem(IDC_STC_SEARCH_SNAP_PICSHOW));
	}
}

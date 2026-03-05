
#include "stdafx.h"
#include "NetClientDemo.h"
#include "VcaQueryFile.h"

typedef enum{
	ITEM_INDEX = 0,						// serial number
	ITEM_VCA_TYPE,						//Intelligent analysis type
	ITEM_CHAN_NO,						//channel number
	ITEM_FILE_TYPE,						//file type
	ITEM_FILE1_ATTR,					//file 1 properties
	ITEM_FILE2_ATTR,					//file 2 properties
	ITEM_BEG_TIME,						//Starting time
	ITEM_END_TIME,						//End Time
	ITEM_EX_ATTR1,						//Extended property 1
	ITEM_EX_ATTR2,						//Extended property 2
	ITEM_EX_ATTR3,						//Extended property 3
	ITEM_EX_ATTR4,						//Extended property 4
	ITEM_EX_ATTR5,						//Extended property 5
	ITEM_EX_ATTR6,						//Extended property 6
	ITEM_EX_ATTR7,						//Extended property 7
	ITEM_EX_ATTR8,						//Extended property 8
	ITEM_EX_ATTR9,						//Extended property 9
	ITEM_EX_ATTR10,						//Extended property 10
	ITEM_EX_ATTR11,						//Extended property 11
	ITEM_EX_ATTR12,						//Extended property 12
	ITEM_EX_ATTR13,						//Extended property 13
}ITEM_VCAFILE;

IMPLEMENT_DYNAMIC(CLS_DlgCfgVcaQueryFile, CDialog)

CLS_DlgCfgVcaQueryFile::CLS_DlgCfgVcaQueryFile(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgCfgVcaQueryFile::IDD, pParent)
{
	m_iCurPageNo = 0;
	m_iTotalPage = 0;
	m_iCurChanPageNo = 0;
	m_iTotalChanPage = 0;
	memset(&m_tQueryChan, 0, sizeof(m_tQueryChan));
	memset(&m_tQueryInfo, 0, sizeof(m_tQueryInfo));
	m_listDownloadPic.clear();
	m_iDLConID = -1;
	m_iDownloadNum = 0;
	m_iDownloadTotalNum = 0;
	InitializeCriticalSection(&m_csDownloadList);
}

CLS_DlgCfgVcaQueryFile::~CLS_DlgCfgVcaQueryFile()
{
	DeleteCriticalSection(&m_csDownloadList);
}

void CLS_DlgCfgVcaQueryFile::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_ALL, m_chkVcaTypeAll);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_TRIP, m_chkVcaTypeTrip);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_DB_TRIP, m_chkVcaTypeDbTrip);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_BDY, m_chkVcaTypeBoundary);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_LOITER, m_chkVcaTypeLoiter);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_PARK, m_chkVcaTypePark);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_RUN, m_chkVcaTypeRun);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_STOEN, m_chkVcaTypeStolen);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_LEFT, m_chkVcaTypeLeft);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_FACE, m_chkVcaTypeFace);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_ALERT, m_chkVcaTypeAlert);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_LPR, m_chkVcaTypeLPR);
	DDX_Control(pDX, IDC_CHK_VCA_TYPE_SAFEHAT, m_chkVcaTypeHelmet);
	DDX_Control(pDX, IDC_DT_VCA_START, m_dtBeg);
	DDX_Control(pDX, IDC_DT_VCA_END, m_dtEnd);
	DDX_Control(pDX, IDC_CBO_VCA_FILE_TYPE, m_cboFileType);
	DDX_Control(pDX, IDC_LST_VCA_FILE_INFO, m_lstVcaFile);
	DDX_Control(pDX, IDC_STC_VCA_PAGE_INFO, m_stcPageInfo);
	DDX_Control(pDX, IDC_CHK_VCA_CHAN_ALL, m_chkChanAll);
	for (int i = 0; i < LEN_32; ++i)
	{
		DDX_Control(pDX, IDC_STC_CHAN_1 + i, m_stcChanNo[i]);
		DDX_Control(pDX, IDC_CHK_CHAN_1 + i, m_chkChanNo[i]);
	}
	DDX_Control(pDX, IDC_CBO_VCA_CHAN_PAGE, m_cboChanPageNo);
	DDX_Control(pDX, IDC_CHECK_VCA_TYPE_NAVIGATION, m_chkVcaTypeNavigation);
	DDX_Control(pDX, IDC_CHECK_BRIGHT_CHICKEN, m_chkBrightKitchen);
	DDX_Control(pDX, IDC_CHECK_SMOKEorPHONE, m_chkSmokeOrPhone);
	DDX_Control(pDX, IDC_COMBO_BERAD, m_cboBeard);
}


BEGIN_MESSAGE_MAP(CLS_DlgCfgVcaQueryFile, CLS_BasePage)
	ON_BN_CLICKED(IDC_CHK_VCA_TYPE_ALL, &CLS_DlgCfgVcaQueryFile::OnBnClickedChkVcaTypeAll)
	ON_BN_CLICKED(IDC_BTN_VCA_QUERY, &CLS_DlgCfgVcaQueryFile::OnBnClickedBtnQuery)
	ON_BN_CLICKED(IDC_BTN_VCA_PAGE_FIRST, &CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPageFirst)
	ON_BN_CLICKED(IDC_BTN_VCA_PAGE_PRE, &CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPagePre)
	ON_BN_CLICKED(IDC_BTN_VCA_PAGE_NEXT, &CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPageNext)
	ON_BN_CLICKED(IDC_BTN_VCA_PAGE_LAST, &CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPageLast)
	ON_BN_CLICKED(IDC_BTN_VCA_CHAN_PAGE_PRE, &CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPreChanPage)
	ON_BN_CLICKED(IDC_BTN_VCA_CHAN_PAGE_NEXT, &CLS_DlgCfgVcaQueryFile::OnBnClickedBtnNextChanPage)
	ON_CBN_SELCHANGE(IDC_CBO_VCA_CHAN_PAGE, &CLS_DlgCfgVcaQueryFile::OnCbnSelchangeCboChanPage)
	ON_BN_CLICKED(IDC_CHK_VCA_CHAN_ALL, &CLS_DlgCfgVcaQueryFile::OnBnClickedChekChanAll)
	ON_BN_CLICKED(IDC_BUTTON_VCA_FILE_DOWNLOAD, &CLS_DlgCfgVcaQueryFile::OnBnClickedButtonVcaFileDownload)
	ON_BN_CLICKED(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE, &CLS_DlgCfgVcaQueryFile::OnBnClickedButtonVcaFileDownloadPage)
END_MESSAGE_MAP()


BOOL CLS_DlgCfgVcaQueryFile::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_dtBeg.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_dtEnd.SetFormat("yyyy-MM-dd HH:mm:ss");

	InitUI();

	return TRUE; 
}

void CLS_DlgCfgVcaQueryFile::InitUI()
{
	m_cboFileType.ResetContent();
	m_cboFileType.SetItemData(m_cboFileType.AddString("All"), 0);
	m_cboFileType.SetItemData(m_cboFileType.AddString("Video"), 1);
	m_cboFileType.SetItemData(m_cboFileType.AddString("picture"), 2);
	m_cboFileType.SetCurSel(2);
	
	m_cboBeard.ResetContent();
	m_cboBeard.SetItemData(m_cboBeard.AddString("Reserved"), 0);
	m_cboBeard.SetItemData(m_cboBeard.AddString("none"), 1);
	m_cboBeard.SetItemData(m_cboBeard.AddString("Yes"), 2);
	m_cboBeard.SetItemData(m_cboBeard.AddString("All"), 0x7FFFFFFF);
	m_cboBeard.SetCurSel(2);

	//initialization list
	m_lstVcaFile.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstVcaFile.InsertColumn(ITEM_INDEX, "Number", LVCFMT_LEFT, 40, -1);
	m_lstVcaFile.InsertColumn(ITEM_VCA_TYPE, "Intelligent Analysis Type", LVCFMT_LEFT, 40, -1);
	m_lstVcaFile.InsertColumn(ITEM_CHAN_NO, "Channel Number", LVCFMT_LEFT, 40, -1);
	m_lstVcaFile.InsertColumn(ITEM_FILE_TYPE, "File Type", LVCFMT_LEFT, 40, -1);
	m_lstVcaFile.InsertColumn(ITEM_FILE1_ATTR, "File 1 attribute", LVCFMT_LEFT, 90, -1);
	m_lstVcaFile.InsertColumn(ITEM_FILE2_ATTR, "File 2 attribute", LVCFMT_LEFT, 90, -1);
	m_lstVcaFile.InsertColumn(ITEM_BEG_TIME, "Start Time", LVCFMT_LEFT, 70, -1);
	m_lstVcaFile.InsertColumn(ITEM_END_TIME, "End Time", LVCFMT_LEFT, 70, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR1, "Extended attribute 1", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR2, "Extended attribute 2", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR3, "Extended attribute 3", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR4, "Extended attribute 4", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR5, "Extended attribute 5", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR6, "Extended attribute 6", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR7, "Extended attribute 7", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR8, "Extended attribute 8", LVCFMT_LEFT, 80, -1);
	m_lstVcaFile.InsertColumn(ITEM_EX_ATTR9, "Extended attribute 9", LVCFMT_LEFT, 80, -1);
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedChkVcaTypeAll()
{
	BOOL blCheck = !m_chkVcaTypeAll.GetCheck();

	m_chkVcaTypeTrip.EnableWindow(blCheck);
	m_chkVcaTypeDbTrip.EnableWindow(blCheck);
	m_chkVcaTypeBoundary.EnableWindow(blCheck);
	m_chkVcaTypeLoiter.EnableWindow(blCheck);
	m_chkVcaTypePark.EnableWindow(blCheck);
	m_chkVcaTypeRun.EnableWindow(blCheck);
	m_chkVcaTypeStolen.EnableWindow(blCheck);
	m_chkVcaTypeLeft.EnableWindow(blCheck);
	m_chkVcaTypeFace.EnableWindow(blCheck);
	m_chkVcaTypeAlert.EnableWindow(blCheck);
	m_chkVcaTypeLPR.EnableWindow(blCheck);
	m_chkVcaTypeHelmet.EnableWindow(blCheck);
	m_chkVcaTypeNavigation.EnableWindow(blCheck);
	m_chkBrightKitchen.EnableWindow(blCheck);
	m_chkSmokeOrPhone.EnableWindow(blCheck);
}

void CLS_DlgCfgVcaQueryFile::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	int iCurLogonId = m_iLogonID;
	CLS_BasePage::OnChannelChanged(_iLogonID, _iChannelNo, _iStreamNo);
	if (iCurLogonId != _iLogonID)
	{
		int iChannelNum = 0;
		NetClient_GetChannelNum(_iLogonID, &iChannelNum);
		m_cboChanPageNo.ResetContent();
		m_iTotalChanPage = iChannelNum/LEN_32;
		if (0 != (iChannelNum%LEN_32) && iChannelNum > 0)
		{
			m_iTotalChanPage ++;
		}

		for (int i = 0; i < m_iTotalChanPage; ++i)
		{
			m_cboChanPageNo.InsertString(i, IntToCString(i+1));
		}
		m_cboChanPageNo.SetCurSel(0);
		m_iCurChanPageNo = -1;
		OnCbnSelchangeCboChanPage();
	}	
}

int CLS_DlgCfgVcaQueryFile::GetVcaTypeList(int* _piList, int _iMaxCount)
{
	int iCount = 0;
	
	if (m_chkVcaTypeAll.GetCheck())
	{
		iCount = 1;
		_piList[0] = 0x7FFFFFFF;
	}
	else
	{
		if (m_chkVcaTypeTrip.GetCheck()){
			_piList[iCount++] = VCA_EVENT_TRIPWIRE;
		}
		if (m_chkVcaTypeDbTrip.GetCheck()){
			_piList[iCount++] = VCA_EVENT_DBTRIPWIRE;
		}
		if (m_chkVcaTypeBoundary.GetCheck()){
			_piList[iCount++] = VCA_EVENT_PERIMETER;
		}
		if (m_chkVcaTypeLoiter.GetCheck()){
			_piList[iCount++] = VCA_EVENT_LOITER;
		}
		if (m_chkVcaTypePark.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_PARKING;
		}
		if (m_chkVcaTypeRun.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_RUN;
		}
		if (m_chkVcaTypeLeft.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_ABANDUM;
		}
		if (m_chkVcaTypeStolen.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_OBJSTOLEN;
		}	
		if (m_chkVcaTypeFace.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_FACEREC;
		}
		if (m_chkVcaTypeAlert.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_PROTECT;
		}
		if (m_chkVcaTypeLPR.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_PLATE_RECOGNISE;
		}
		if (m_chkVcaTypeHelmet.GetCheck()) {
			_piList[iCount++] = VCA_EVENT_HELMET;
		}
		if (m_chkVcaTypeNavigation.GetCheck()) {
			_piList[iCount++] = 53;
		}
		if (m_chkBrightKitchen.GetCheck()) {
			_piList[iCount++] = 54;
		}
		if (m_chkSmokeOrPhone.GetCheck()) {
			_piList[iCount++] = 58;
		}
	}
	return iCount;
}

int CLS_DlgCfgVcaQueryFile::GetChanList()
{
	int iCount = 0;
	if (m_chkChanAll.GetCheck())
	{
		iCount = 1;
		m_tQueryChan[0].iChanNo = 0x7FFFFFFF;
	}
	else
	{
		for (int i = 0; i < LEN_32; ++i)
		{
			if (m_chkChanNo[i].GetCheck())
			{
				m_tQueryChan[iCount].iChanNo = i + m_iCurChanPageNo*LEN_32;
				iCount++;
			}
		}
	}
	return iCount;
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedBtnQuery()
{
	if (NULL == NetClient_Query_V5)
	{
		return;
	}

	memset(&m_tQueryChan, 0, sizeof(m_tQueryChan));
	memset(&m_tQueryInfo, 0, sizeof(m_tQueryInfo));

	NetFileQueryVca &tQuery = m_tQueryInfo;
	tQuery.iSize = sizeof(tQuery);
	
	//channel list
	tQuery.iChanCount = GetChanList();
	tQuery.iChanSize = sizeof(QueryChanNo);
	tQuery.pChanList = &m_tQueryChan[0];

	//Intelligent analysis list, up to 32
	tQuery.iVcaCount = GetVcaTypeList(tQuery.iVcaList, MAX_QUERY_LIST_COUNT);

	// start end time
	GetNvsFileTime(&m_dtBeg, tQuery.tBegTime);
	GetNvsFileTime(&m_dtEnd, tQuery.tEndTime);

	//page number information
	tQuery.iPageCount = MAX_QUERY_PAGE_COUNT;
	tQuery.iPageNo = 0;

	//file type
	tQuery.iFileType = m_cboFileType.GetCurSel();

// 	if (m_chkBrightKitchen.GetCheck() || m_chkSmokeOrPhone.GetCheck()) {
// 		tQuery.iConditionCount = 1;
// 		CString strUUid;
// 		GetDlgItemText(IDC_EDIT_UUID, strUUid);
// 		strcpy(tQuery.cQueryCondition[0], strUUid.GetBuffer());
// 	}
// 	if(m_chkVcaTypeFace.GetCheck())
// 	{
// 		tQuery.iConditionCount = 9;
// 		CString cstr = IntToCString(m_cboBeard.GetItemData(m_cboBeard.GetCurSel()));
// 		strcpy(tQuery.cQueryCondition[8], cstr.GetBuffer());
// 	}
	

	ShowPage(0, &tQuery, TRUE);
}

CString GetFileTypeStr(int _iType)
{
	CString cstr;
	if (1 == _iType) {
		cstr = "Video";
	} else if (2 == _iType) {
		cstr = "picture";
	} else {
		cstr = IntToCString(_iType);
	}
	return cstr;
}

CString GetVcaTypeStr(int _iType)
{
	CString cstr;
	if (VCA_EVENT_TRIPWIRE == _iType) {
		cstr = "Single Companion Line";
	} else if (VCA_EVENT_DBTRIPWIRE == _iType) {
		cstr = "Dual Companion Line";
	} else if (VCA_EVENT_PERIMETER == _iType) {
		cstr = "perimeter detection";
	} else if (VCA_EVENT_LOITER == _iType) {
		cstr = "Wandering";
	} else if (VCA_EVENT_PARKING == _iType) {
		cstr = "parking";
	} else if (VCA_EVENT_RUN == _iType) {
		cstr = "run";
	} else if (VCA_EVENT_ABANDUM == _iType) {
		cstr = "Abandoned Objects";
	} else if (VCA_EVENT_OBJSTOLEN == _iType) {
		cstr = "Stolen Objects";
	} else if (VCA_EVENT_FACEREC == _iType) {
		cstr = "Face Recognition";
	} else if (VCA_EVENT_PROTECT == _iType) {
		cstr = "alert";
	} else if (VCA_EVENT_PLATE_RECOGNISE == _iType) {
		cstr = "License Plate Recognition";
	} else if (VCA_EVENT_HELMET == _iType) {
		cstr = "Hard hat detection";
	} else {
		cstr = IntToCString(_iType);
	}
	return cstr;
}

CString GetPicTypeStr(int _iType)
{
	CString cstr;
	if (1 == _iType) {
		cstr = "Small image";
	} else if (2 == _iType) {
		cstr = "big picture";
	} else if (0 == _iType) {
		cstr = "none";
	} else {
		cstr = IntToCString(_iType);
	}
	return cstr;
}

CString GetFileAttr(VcaFileAttr& _tInfo)
{
	CString cstr;
	cstr.Format("File number: %d; File name: %s; File size: %d; File attribute: %s", _tInfo.iFileIndex, _tInfo.cFileName,
		_tInfo.iFileSize, GetPicTypeStr(_tInfo.iFileType));
	return cstr;
}

CString GetTimeStr(NVS_FILE_TIME& _tInfo)
{
	CString cstr;
	cstr.Format("%04d-%02d-%02d %02d:%02d:%02d", _tInfo.iYear, _tInfo.iMonth, _tInfo.iDay,
		_tInfo.iHour, _tInfo.iMinute, _tInfo.iSecond);
	return cstr;
}

CString GetFieldAttr(int _iVcaType, int _iIndex, CString _cSrc)
{
	CString cstr;
	int iValue = atoi(_cSrc);

	if (VCA_EVENT_FACEREC == _iVcaType) {
		if (1 == _iIndex) {
			cstr = "age:" + _cSrc;
		} else if (2 == _iIndex) {
			if (1 == iValue) {
				cstr = "male";
			} else if (2 == iValue) {
				cstr = "female";
			} else if (3 == iValue) {
				cstr = "unknown";
			} else {
				cstr = _cSrc;
			}
		} else if (3 == _iIndex) {
			if (1 == iValue) {
				cstr = "Han";
			} else if (2 == iValue) {
				cstr = "Minority";
			} else {
				cstr = _cSrc;
			}
		} else if (7 == _iIndex || 8 == _iIndex || 9 == _iIndex) {//7-cLibUUID,8-cFaceUUID,9-name	
				cstr = _cSrc;	
		}
	} else if (VCA_EVENT_PROTECT == _iVcaType) {
		if (1 == _iIndex) {
			if (1 == iValue) {
				cstr = "people";
			} else if (2 == iValue) {
				cstr = "car";
			} else if (3 == iValue) {
				cstr = "other";
			} else {
				cstr = _cSrc;
			}
		}else if (2 == _iIndex) {
			if (1 == iValue) {
				cstr = "perimeter-invasion";
			} else if (2 == iValue) {
				cstr = "perimeter-leave";
			} else if (3 == iValue) {
				cstr = "tripwire";
			} else if (0x7FFFFFFF == iValue) {
				cstr = "all";
			}else {
				cstr = _cSrc;
			}
		}		
	} else if (VCA_EVENT_PLATE_RECOGNISE == _iVcaType) {
		cstr = _cSrc;
	} else if (VCA_EVENT_HELMET == _iVcaType) {
		if (1 == iValue) {
			cstr = "red";
		} else if (2 == iValue) {
			cstr = "yellow";
		} else if (3 == iValue) {
			cstr = "blue";
		} else if (4 == iValue) {
			cstr = "White";
		} else if (5 == iValue) {
			cstr = "other";
		} else {
			cstr = _cSrc;
		}
	} else {
		cstr = _cSrc;
	}
	return cstr;
}

void CLS_DlgCfgVcaQueryFile::ShowPage(int _iPageNo, NetFileQueryVca* _pInfo, BOOL _blFirst)
{
	m_lstVcaFile.DeleteAllItems();

	if (NULL == _pInfo || _iPageNo < 0)
	{
		return;
	}

	if (!_blFirst && m_iTotalPage <= 0)
	{
		MessageBox("Please check first!", GetTextEx(IDS_LOG_OPERATION_MSG), MB_OK);
		return;
	}

	if (!_blFirst && _iPageNo >= m_iTotalPage)
	{
		return;
	}

	m_iCurPageNo = _iPageNo;
	_pInfo->iPageNo = _iPageNo;

	NetFileQueryVcaResult tResult[MAX_QUERY_PAGE_COUNT] = {0};
	int iRet = NetClient_Query_V5(m_iLogonID, CMD_NETFILE_QUERY_VCA, m_iChannelNO, _pInfo, sizeof(NetFileQueryVca), &tResult, sizeof(NetFileQueryVcaResult));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","ShowPage(%d) failed.", _iPageNo);
		return;
	}

	//update data in list
	int iCount = tResult[0].iCurPageCount;
	for (int i = 0; i < iCount && i < MAX_QUERY_PAGE_COUNT; ++i)
	{
		int iItem = m_lstVcaFile.GetItemCount();
		m_lstVcaFile.InsertItem(iItem, "");

		NetFileQueryVcaResult tInfo = tResult[i];
		m_lstVcaFile.SetItemText(iItem, ITEM_INDEX, IntToCString(i + 1));
		m_lstVcaFile.SetItemText(iItem, ITEM_CHAN_NO, IntToCString(tInfo.iChanNo+1));
		m_lstVcaFile.SetItemText(iItem, ITEM_VCA_TYPE, GetVcaTypeStr(tInfo.iVcaType));
		m_lstVcaFile.SetItemText(iItem, ITEM_FILE_TYPE, GetFileTypeStr(tInfo.iFileType));		
		if (tInfo.iFileAttrCount > 0) {
			m_lstVcaFile.SetItemText(iItem, ITEM_FILE1_ATTR, GetFileAttr(tInfo.tFileAttr[0]));
		}
		if (tInfo.iFileAttrCount > 1) {
			m_lstVcaFile.SetItemText(iItem, ITEM_FILE2_ATTR, GetFileAttr(tInfo.tFileAttr[1]));
		}
		m_lstVcaFile.SetItemText(iItem, ITEM_BEG_TIME, GetTimeStr(tInfo.tBegTime));
		m_lstVcaFile.SetItemText(iItem, ITEM_END_TIME, GetTimeStr(tInfo.tEndTime));

		if (tInfo.iExAttrCount > 0){
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR1, GetFieldAttr(tInfo.iVcaType, 1, tInfo.cExAttr[0]));
		}
		if (tInfo.iExAttrCount > 1) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR2, GetFieldAttr(tInfo.iVcaType, 2, tInfo.cExAttr[1]));
		}
		if (tInfo.iExAttrCount > 2) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR3, GetFieldAttr(tInfo.iVcaType, 3, tInfo.cExAttr[2]));
		}
		if (tInfo.iExAttrCount > 3) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR4, GetFieldAttr(tInfo.iVcaType, 4, tInfo.cExAttr[3]));
		}
		if (tInfo.iExAttrCount > 4) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR5, GetFieldAttr(tInfo.iVcaType, 5, tInfo.cExAttr[4]));
		}
		if (tInfo.iExAttrCount > 5) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR6, GetFieldAttr(tInfo.iVcaType, 6, tInfo.cExAttr[5]));
		}
		if (tInfo.iExAttrCount > 6) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR7, GetFieldAttr(tInfo.iVcaType, 7, tInfo.cExAttr[6]));
		}
		if (tInfo.iExAttrCount > 7) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR8, GetFieldAttr(tInfo.iVcaType, 8, tInfo.cExAttr[7]));
		}
		if (tInfo.iExAttrCount > 8) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR9, GetFieldAttr(tInfo.iVcaType, 9, tInfo.cExAttr[8]));
		}
		if (tInfo.iExAttrCount > 12) {
			m_lstVcaFile.SetItemText(iItem, ITEM_EX_ATTR13, GetFieldAttr(tInfo.iVcaType, 13, tInfo.cExAttr[12]));
		}
	}	
	CString cstrPage;
	m_iTotalPage = tResult[0].iTotal/MAX_QUERY_PAGE_COUNT;
	if (0 != tResult[0].iTotal%MAX_QUERY_PAGE_COUNT)
	{
		m_iTotalPage++;
	}
	cstrPage.Format("%d/%d", (_iPageNo + 1), m_iTotalPage);
	m_stcPageInfo.SetWindowText(cstrPage);
}


void CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPageFirst()
{
	ShowPage(0, &m_tQueryInfo);
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPagePre()
{
	ShowPage(m_iCurPageNo - 1, &m_tQueryInfo);
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPageNext()
{
	ShowPage(m_iCurPageNo + 1, &m_tQueryInfo);
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPageLast()
{
	ShowPage(m_iTotalPage - 1, &m_tQueryInfo);
}

void CLS_DlgCfgVcaQueryFile::OnCbnSelchangeCboChanPage()
{
	ShowChanPage(m_cboChanPageNo.GetCurSel());
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedBtnPreChanPage()
{
	ShowChanPage(m_iCurChanPageNo - 1);
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedBtnNextChanPage()
{
	ShowChanPage(m_iCurChanPageNo + 1);
}

void CLS_DlgCfgVcaQueryFile::ShowChanPage(int _iPageNo)
{
	if (_iPageNo < 0 || _iPageNo >= m_iTotalChanPage || m_iCurChanPageNo == _iPageNo)
	{
		return;
	}

	int iChannelNum = 0;
	NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	if (iChannelNum <= 0)
	{
		return;
	}

	m_iCurChanPageNo = _iPageNo;
	m_cboChanPageNo.SetCurSel(_iPageNo);

	for (int i = 0; i < LEN_32; ++i)
	{
		int iChanNo = i + _iPageNo*LEN_32;
		m_stcChanNo[i].SetWindowText(IntToCString(iChanNo + 1));
		m_chkChanNo[i].SetCheck(FALSE);
		m_chkChanNo[i].EnableWindow(iChanNo < iChannelNum ? TRUE : FALSE);
	}
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedChekChanAll()
{
	int iChannelNum = 0;
	NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	if (iChannelNum <= 0)
	{
		return;
	}

	int iEnable = m_chkChanAll.GetCheck();
	for (int i = 0; i < LEN_32; ++i)
	{
		int iChanNo = i + m_iCurChanPageNo*LEN_32;
		if (iChanNo >= iChannelNum)
		{
			m_chkChanNo[i].EnableWindow(FALSE);
		}
		else
		{
			m_chkChanNo[i].EnableWindow(!iEnable);
		}	
	}
}

void CLS_DlgCfgVcaQueryFile::OnBnClickedButtonVcaFileDownload()
{
	GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD)->EnableWindow(FALSE);
	GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE)->EnableWindow(FALSE);

	EnterCriticalSection(&m_csDownloadList);
	m_iDownloadNum = 0;
	m_listDownloadPic.clear();
	
	POSITION pos = m_lstVcaFile.GetFirstSelectedItemPosition();
	if (pos != NULL)
	{
		int nItem = m_lstVcaFile.GetNextSelectedItem(pos);
		CString csteFileInfo1 = m_lstVcaFile.GetItemText(nItem, ITEM_FILE1_ATTR);
		CString csteFileInfo2 = m_lstVcaFile.GetItemText(nItem, ITEM_FILE2_ATTR);

		if (csteFileInfo1 != "")
		{
			CString cstrTemp = csteFileInfo1;

			int	iPosEnd = csteFileInfo1.FindOneOf(_T(".jpg"));
			if (-1 != iPosEnd)
			{
				cstrTemp = csteFileInfo1.Left(iPosEnd+4);

				int	iPosStart = cstrTemp.FindOneOf(_T("H"));
				if (-1 != iPosStart)
				{
					CString cstrFile = cstrTemp.Right(cstrTemp.GetLength() - iPosStart);

					m_listDownloadPic.push_back(cstrFile);
				}

			}

		}

		if(csteFileInfo2 != "")
		{
			CString cstrTemp = csteFileInfo2;
			int iPosEnd = csteFileInfo2.FindOneOf(_T(".jpg"));
			if (-1 != iPosEnd)
			{
				cstrTemp = csteFileInfo2.Left(iPosEnd+4);
				int iPosStart = cstrTemp.FindOneOf(_T("H"));
				if (-1 != iPosStart)
				{
					CString cstrFile = cstrTemp.Right(cstrTemp.GetLength() - iPosStart);

					m_listDownloadPic.push_back(cstrFile);
				}
			}
		}
	}
	
	CString cstrDownload;
	m_iDownloadTotalNum = m_listDownloadPic.size();
	cstrDownload.Format("/%d",m_listDownloadPic.size());
	SetDlgItemText(IDC_STC_VCA_DOWNLOAD_ALLINFO, cstrDownload);

	if (m_listDownloadPic.size() > 0)
	{
		DownloadNextPic();
	}
	else
	{
		SetDlgItemInt(IDC_STC_VCA_DOWNLOAD_INFO, 0);
		GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE)->EnableWindow(TRUE);
	}

	LeaveCriticalSection(&m_csDownloadList);
}

int CLS_DlgCfgVcaQueryFile::StartDownloadVcaFile(CString _cstrRemoteFilename,CString _cstrLocalFilename)
{
	DOWNLOAD_FILE tDL = {0};
	tDL.m_iSize = sizeof(tDL);
	strcpy_s(tDL.m_cRemoteFilename, sizeof(tDL.m_cRemoteFilename), _cstrRemoteFilename);
	strcpy_s(tDL.m_cLocalFilename, sizeof(tDL.m_cLocalFilename), _cstrLocalFilename);
	tDL.m_iReqMode = 0; //must stream mode
	tDL.m_iSpeed = 32; //Maximum download speed
	tDL.m_iPosition = -1;//Deal active mode cannot download pictures

	unsigned int uDLId = -1; 
	int iRet = NetClient_NetFileDownload(&uDLId, m_iLogonID, DOWNLOAD_CMD_FILE, &tDL, sizeof(tDL));
	if (0 == iRet)
	{
		m_iDLConID = uDLId;
	}

	return iRet;
}

void CLS_DlgCfgVcaQueryFile::OnMainNotify( int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser )
{
	if (m_iLogonID < 0 || m_iLogonID != _ulLogonID)
	{
		return;
	}

	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_ERR_ORDER://Network command broken
	case WCM_ERR_DATANET://Network data error
		{
			GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD)->EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE)->EnableWindow(TRUE);
		}
		break;
	case WCM_DWONLOAD_FAULT:
	case WCM_DOWNLOAD_INTERRUPT:
		{
			unsigned int uDownloadID = (unsigned int)_iLParam;
			if (m_iDLConID == uDownloadID)
			{
				NetClient_NetFileStopDownloadFile(uDownloadID);
				m_iDLConID = -1;
			}

			GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD)->EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE)->EnableWindow(TRUE);
		}
		break;
	case WCM_DWONLOAD_FINISHED:
		{
			unsigned int uDownloadID = (unsigned int)_iLParam;
			if (m_iDLConID == uDownloadID)
			{
				NetClient_NetFileStopDownloadFile(uDownloadID);
				m_iDLConID = -1;
			}

			m_iDownloadNum++;
			if (m_iDownloadNum >=m_iDownloadTotalNum)
			{
				GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD)->EnableWindow(TRUE);
				GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE)->EnableWindow(TRUE);
				m_iDownloadNum = m_iDownloadTotalNum;
			}
			SetDlgItemInt(IDC_STC_VCA_DOWNLOAD_INFO, m_iDownloadNum);

			DownloadNextPic();
		}
		break;
	default :
		break;
	}	
}
void CLS_DlgCfgVcaQueryFile::OnBnClickedButtonVcaFileDownloadPage()
{
	int iCount = m_lstVcaFile.GetItemCount();
	GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD)->EnableWindow(FALSE);
	GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE)->EnableWindow(FALSE);
	EnterCriticalSection(&m_csDownloadList);
	m_iDownloadNum = 0;
	m_listDownloadPic.clear();

	for (int i = 0; i < iCount && i < MAX_QUERY_PAGE_COUNT; ++i)
	{
		CString csteFileInfo1 = m_lstVcaFile.GetItemText(i, ITEM_FILE1_ATTR);
		CString csteFileInfo2 = m_lstVcaFile.GetItemText(i, ITEM_FILE2_ATTR);
		
		if (csteFileInfo1 != "")
		{
			CString cstrTemp = csteFileInfo1;

			int	iPosEnd = csteFileInfo1.FindOneOf(_T(".jpg"));
			if (-1 != iPosEnd)
			{
				cstrTemp = csteFileInfo1.Left(iPosEnd+4);

				int	iPosStart = cstrTemp.FindOneOf(_T("H"));
				if (-1 != iPosStart)
				{
					CString cstrFile = cstrTemp.Right(cstrTemp.GetLength() - iPosStart);

					m_listDownloadPic.push_back(cstrFile);
				}
				
			}
			
		}

		if(csteFileInfo2 != "")
		{
			CString cstrTemp = csteFileInfo2;
			int iPosEnd = csteFileInfo2.FindOneOf(_T(".jpg"));
			if (-1 != iPosEnd)
			{
				cstrTemp = csteFileInfo2.Left(iPosEnd+4);
				int iPosStart = cstrTemp.FindOneOf(_T("H"));
				if (-1 != iPosStart)
				{
					CString cstrFile = cstrTemp.Right(cstrTemp.GetLength() - iPosStart);

					m_listDownloadPic.push_back(cstrFile);
				}
			}
		}
	}

	CString cstrDownload;
	m_iDownloadTotalNum = m_listDownloadPic.size();
	cstrDownload.Format("/%d",m_listDownloadPic.size());
	SetDlgItemText(IDC_STC_VCA_DOWNLOAD_ALLINFO, cstrDownload);

	if (m_listDownloadPic.size() > 0)
	{
		DownloadNextPic();
	}
	else
	{
		SetDlgItemInt(IDC_STC_VCA_DOWNLOAD_INFO, 0);
		GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_VCA_FILE_DOWNLOAD_PAGE)->EnableWindow(TRUE);
	}

	LeaveCriticalSection(&m_csDownloadList);
}

void CLS_DlgCfgVcaQueryFile::DownloadNextPic()
{		
	EnterCriticalSection(&m_csDownloadList);

	if (m_listDownloadPic.size() > 0)
	{
		CString cstrPicName = m_listDownloadPic.front();
		CString cstrPath = GetLocalSaveDirectory();
		cstrPath += "\\";
		cstrPath += cstrPicName;

		StartDownloadVcaFile(cstrPicName,cstrPath);

		m_listDownloadPic.pop_front();
	}

	LeaveCriticalSection(&m_csDownloadList);
}

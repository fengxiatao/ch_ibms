
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFacePic.h"
#include "CLS_DlgCfgProcess.h"
#include "CLS_DlgFacePicEdit.h"
#include "CommonCitys.h"

#include <direct.h>
#include <shlwapi.h>
#include <process.h>

typedef enum{
	ITEM_PIC_INDEX = 0,					//Serial No
	ITEM_PIC_NAME,						//Face Name
	ITEM_PIC_SEX,						//Gender
	ITEM_PIC_NATION,					//nation
	ITEM_PIC_BIRTH,						//date of birth
	ITEM_PIC_PLACE,						//Native place
	ITEM_PIC_CARDTYPE,					//Document type 
	ITEM_PIC_CARDNO,					//Certificate No
	ITEM_PIC_MODESTATE,					//Modeling status
	ITEM_PIC_UUID,						//Picture UUID
	ITEM_PIC_PATH,						//Picture Path
	ITEM_PIC_SIMILAR,					//Similarity
}ITEM_FACE_PIC;

IMPLEMENT_DYNAMIC(CLS_DlgFacePic, CLS_PageBase)

CLS_DlgFacePic::CLS_DlgFacePic(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFacePic::IDD, pParent)
{
	m_pDlgPicEdit = NULL;
	memset(&m_tQueryInfo, 0, sizeof(m_tQueryInfo));
	memset(&m_tFaceInfo, 0, sizeof(m_tFaceInfo));
	m_iCurPage = 0;
	m_iTolalPage = 0;

	m_pDlgPreocess = NULL;
	m_blInportThread = FALSE;
}

CLS_DlgFacePic::~CLS_DlgFacePic()
{
	m_pDlgPreocess = NULL;
}

void CLS_DlgFacePic::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_PIC_LIB, m_cboLibKey);
	DDX_Control(pDX, IDC_CBO_PIC_CARDTYPE, m_cboCardType);
	DDX_Control(pDX, IDC_EDT_PIC_CARDNUM, m_edtCardNum);
	DDX_Control(pDX, IDC_EDT_PIC_NAME, m_edtFaceName);
	DDX_Control(pDX, IDC_CBO_PIC_SEX, m_cboSex);
	DDX_Control(pDX, IDC_CBO_PIC_MODEL, m_cboModelStatus);
	DDX_Control(pDX, IDC_LST_PIC_INFO, m_lstFaceInfo);
	DDX_Control(pDX, IDC_CBO_PIC_PAGE, m_cboPage);
	DDX_Control(pDX, IDC_STC_PIC_PAGE, m_stcPageShow);
	DDX_Control(pDX, IDC_COMBO1_COUNTRY, m_cboCountry);
	DDX_Control(pDX, IDC_EDIT1_COMPANY, m_edtCompany);
	DDX_Control(pDX, IDC_EDIT2, m_edtAddress);
}

BEGIN_MESSAGE_MAP(CLS_DlgFacePic, CLS_PageBase)
	ON_BN_CLICKED(IDC_BTN_PIC_QUERY, &CLS_DlgFacePic::OnBnClickedBtnPicQuery)
	ON_BN_CLICKED(IDC_BTN_PIC_ADD, &CLS_DlgFacePic::OnBnClickedBtnPicAdd)
	ON_BN_CLICKED(IDC_BTN_PIC_MODIFY, &CLS_DlgFacePic::OnBnClickedBtnPicModify)
	ON_BN_CLICKED(IDC_BTN_PIC_DELETE, &CLS_DlgFacePic::OnBnClickedBtnPicDelete)
	ON_BN_CLICKED(IDC_BTN_PIC_PAGE_FIRST, &CLS_DlgFacePic::OnBnClickedBtnPicPageFirst)
	ON_BN_CLICKED(IDC_BTN_PIC_PAGE_PRE, &CLS_DlgFacePic::OnBnClickedBtnPicPagePre)
	ON_BN_CLICKED(IDC_BTN_PIC_PAGE_NEXT, &CLS_DlgFacePic::OnBnClickedBtnPicPageNext)
	ON_BN_CLICKED(IDC_BTN_PIC_PAGE_LAST, &CLS_DlgFacePic::OnBnClickedBtnPicPageLast)	
	ON_CBN_SELCHANGE(IDC_CBO_PIC_PAGE, &CLS_DlgFacePic::OnCbnSelchangeCboPicPage)
	ON_BN_CLICKED(IDC_BTN_PIC_INPORT, &CLS_DlgFacePic::OnBnClickedBtnPicInport)
	ON_BN_CLICKED(IDC_BTN_PIC_OUTPORT_ALL, &CLS_DlgFacePic::OnBnClickedBtnPicOutportAll)
ON_NOTIFY(NM_DBLCLK, IDC_LST_PIC_INFO, &CLS_DlgFacePic::OnNMDblclkLstPicInfo)
END_MESSAGE_MAP()

void CLS_DlgFacePic::UI_Init()
{
	//Gender
	m_cboSex.ResetContent();
	for (int i = 0; i < (sizeof(CONST_CSTR_SEX)/sizeof(CString)); ++i) {
		m_cboSex.InsertString(i, CONST_CSTR_SEX[i]);
		m_cboSex.SetItemData(i, i);
	}
	m_cboSex.SetCurSel(0);

	//Document type 
	m_cboCardType.ResetContent();
	for (int i = 0; i < (sizeof(CONST_CSTR_CARD)/sizeof(CString)); ++i) {
		if (0 == i) {
			m_cboCardType.InsertString(i, "All");
		} else {
			m_cboCardType.InsertString(i, CONST_CSTR_CARD[i]);
		}
	}
	m_cboCardType.SetCurSel(0);

	//Modeling status
	m_cboModelStatus.ResetContent();
	for (int i = 0; i < (sizeof(CONST_CSTR_MODE)/sizeof(CString)); ++i) {
		m_cboModelStatus.InsertString(i, CONST_CSTR_MODE[i]);
	}
	m_cboModelStatus.SetCurSel(0);

	//list
	m_lstFaceInfo.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_INDEX, "No.", LVCFMT_LEFT, 40, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_NAME, "Name", LVCFMT_LEFT, 100, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_SEX, "Gender", LVCFMT_LEFT, 60, -1);	
	m_lstFaceInfo.InsertColumn(ITEM_PIC_NATION, "Nationality", LVCFMT_LEFT, 80, -1);	
	m_lstFaceInfo.InsertColumn(ITEM_PIC_BIRTH, "Birthday", LVCFMT_LEFT, 70, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_PLACE, "Native Place", LVCFMT_LEFT, 100, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_CARDTYPE, "Certificate Type", LVCFMT_LEFT, 115, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_CARDNO, "Certificate No.", LVCFMT_LEFT, 120, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_MODESTATE, "Modeling State", LVCFMT_LEFT, 100, -1);

	UI_InitCountry(m_cboCountry);
}

void CLS_DlgFacePic::UI_UptateData()
{
	QueryLibkey(m_cboLibKey);
	m_lstFaceInfo.DeleteAllItems();
	if (m_cboLibKey.GetCount() > 0)
	{
		OnBnClickedBtnPicQuery();
	}
	//Maximum base map value
	FuncAbilityLevel tInfo = {sizeof(FuncAbilityLevel), MAIN_FUNC_TYPE_VCA, 27};
	int iRetBytes = 0;
	NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, 0x7FFFFFFF, &tInfo, sizeof(tInfo), &iRetBytes);
	CString cstrMsg;
	cstrMsg.Format("Face PictureMax Support %d", atoi(tInfo.cParam));
	SetDlgItemText(IDC_STC_PIC_SPCOUNT, cstrMsg);
}

void CLS_DlgFacePic::UI_ShowPage(int _iPageNo)
{
	if ((m_iTolalPage > 0 && _iPageNo >= m_iTolalPage) || _iPageNo < 0) {
		return;
	}
	m_lstFaceInfo.DeleteAllItems();
	m_cboPage.ResetContent();

	if (m_tQueryInfo.iSize <= 0)
	{
		MessageBox("Please query first!", "Tips", MB_OK);	
		return;
	}

	m_tQueryInfo.iPageNo = _iPageNo;
	m_tQueryInfo.iChanNo = m_iChannelNo;
	memset(&m_tFaceInfo, 0, sizeof(m_tFaceInfo));
	int iRet = FaceConfig(FACE_CMD_QUERY, &m_tQueryInfo, sizeof(m_tQueryInfo), &m_tFaceInfo, sizeof(FaceQueryResult));
	if (0 != iRet) {
		MessageBox("Operation failed!", "Tips", MB_OK);
		return;
	}

	//Page number processing
	int iTotalPage = m_tFaceInfo[0].iTotal/FACE_MAX_PAGE_COUNT;
	if (m_tFaceInfo[0].iTotal%FACE_MAX_PAGE_COUNT > 0 && m_tFaceInfo[0].iTotal > 0) {
		iTotalPage ++;
	}
	m_iTolalPage = iTotalPage;
	for (int i = 0; i < m_iTolalPage; ++i) {
		m_cboPage.InsertString(i, IntToStr(i+1));
	}
	m_iCurPage = _iPageNo;
	m_cboPage.SetCurSel(m_iCurPage);
	if (m_tFaceInfo[0].iTotal > 0) {
		_iPageNo ++;
	}
	m_stcPageShow.SetWindowText(IntToStr(_iPageNo)+"/"+IntToStr(m_iTolalPage));

	for (int i = 0; i < m_tFaceInfo[0].iPageCount; ++i) {
		UI_UpdateFaceList(m_tFaceInfo[i].tFace);
	}
}

void CLS_DlgFacePic::UI_UpdateFaceList(FaceInfo& _tInfo, int _iLibIndex)
{
	int iIndex = _iLibIndex;
	if (-1 == iIndex) {
		iIndex = m_lstFaceInfo.GetItemCount();
		m_lstFaceInfo.InsertItem(iIndex,_T(""));
	}
	m_lstFaceInfo.SetItemData(iIndex, _tInfo.iFaceKey);

	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_INDEX, IntToStr(iIndex + 1));
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_NAME, _tInfo.cName);
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_SEX, CONST_CSTR_SEX[_tInfo.iSex]);
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_NATION, CONST_CSTR_NATION[_tInfo.iNation]);
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_BIRTH, _tInfo.cBirthTime);
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_PLACE, GetPlaceStr(_tInfo.iPlace));
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_CARDTYPE, CONST_CSTR_CARD[_tInfo.iCertType]);
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_CARDNO, _tInfo.cCertNum);
	m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_MODESTATE, CONST_CSTR_MODE[_tInfo.iModeling + 1]);
}

void CLS_DlgFacePic::OnBnClickedBtnPicQuery()
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);

	FaceQuery &tQuery = m_tQueryInfo;
	memset(&tQuery, 0, sizeof(tQuery));
	m_iCurPage = 0;
	m_iTolalPage = 0;
	
	//query criteria
	tQuery.iSize = sizeof(tQuery);
	tQuery.iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);
	tQuery.iPageCount = FACE_MAX_PAGE_COUNT;
	strcpy_s(tQuery.cBirthStart, sizeof(tQuery.cBirthStart), "1900-01-01");
	strcpy_s(tQuery.cBirthEnd, sizeof(tQuery.cBirthEnd), GetCurTimeStr());
	tQuery.iSex = (int)m_cboSex.GetItemData(m_cboSex.GetCurSel());	//Gender
	tQuery.iCertType = m_cboCardType.GetCurSel();
	tQuery.iModeling = m_cboModelStatus.GetCurSel();//Modeling status, 0 means all
	m_edtFaceName.GetWindowText(tQuery.cName, sizeof(tQuery.cName));
	m_edtCompany.GetWindowText(tQuery.cCompany, sizeof(tQuery.cCompany));
	m_edtAddress.GetWindowText(tQuery.cAddress, sizeof(tQuery.cAddress));
	tQuery.iFaceObjScoreLevel = GetDlgItemInt(IDC_EDIT_PIC_SCORELEVEL);

	if (0 != tQuery.iCertType)
	{
		m_edtCardNum.GetWindowText(tQuery.cCertNum, sizeof(tQuery.cCertNum));
	}
	if (strlen(tQuery.cName) > 0 && !CheckFaceName((CString)tQuery.cName))
	{
		MessageBox("Name contains illegal characters!", "Tips", MB_OK);
		m_edtFaceName.SetFocus();
		return;
	}
	if (!CLS_DlgFacePicEdit::IsValidCertNo(tQuery.iCertType, tQuery.cCertNum))
	{
		MessageBox("Please enter the correct Certificate No.!", "Tips", MB_OK);
		m_edtCardNum.SetFocus();
		return;
	}

	UI_ShowPage(m_iCurPage);
}

void CLS_DlgFacePic::OnBnClickedBtnPicAdd()
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);

	if(1 == m_iChanCount)
	{
		MessageBox("The intelligent analysis will be suspended!", "Tips", MB_OK);

		VCASuspend tInPara = {0};
		tInPara.iStatus = 0;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		tInPara.iDevType = 0;	//0-IPC, 1-NVR

		VCASuspendResult tSuspendRet = {0};
		tSuspendRet.iBufSize = sizeof(tSuspendRet);
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNo, SYNC_NET_CLIENT_VCA_SUSPEND, &tInPara, sizeof(tInPara), &tSuspendRet, sizeof(tSuspendRet));
		if (0 == iRet && 1==tSuspendRet.iResult)
		{

		}
		else
		{
			MessageBox("Intelligent analysis resources are being used,suspend intelligent analysis failure!", "Tips", MB_OK);
			return;
		}
	}

	CLS_DlgFacePicEdit cls;
	cls.OnChannelChanged(m_iLogonID, m_iChannelNo, m_iStreamNo);
	FaceInfo tInfo = {0};
	tInfo.iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);
	cls.SetDlgType(DLG_TYPE_ADD);
	cls.SetPicInfo(tInfo);
	cls.DoModal();
	UI_ShowPage(m_iCurPage);
	
	if(1 == m_iChanCount)
	{
		VCASuspend tInPara = {0};
		tInPara.iStatus = 1;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		tInPara.iDevType = 0;	//0-IPC, 1-NVR

		VCASuspendResult tSuspendRet = {0};
		tSuspendRet.iBufSize = sizeof(tSuspendRet);
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNo, SYNC_NET_CLIENT_VCA_SUSPEND, &tInPara, sizeof(tInPara), &tSuspendRet, sizeof(tSuspendRet));
		if (0 == iRet && 1==tSuspendRet.iResult)
		{
			//MessageBox("Intelligent analysis is reinstated successful!", "Tips", MB_OK);
		}
	}
}

void CLS_DlgFacePic::OnBnClickedBtnPicModify()
{
	//Select a record first
	POSITION pPos = m_lstFaceInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos) {
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}
	int iPos = m_lstFaceInfo.GetNextSelectedItem(pPos);
	if (iPos < 0 || iPos >= FACE_MAX_PAGE_COUNT){
		return;
	}

	CLS_DlgFacePicEdit cls;
	m_pDlgPicEdit = &cls;

	FaceInfo tInfo = m_tFaceInfo[iPos].tFace;
	CString cstrPath = GetCurModulePath();
	StartDownLoadFacePic(tInfo, cstrPath);

	cls.OnChannelChanged(m_iLogonID, m_iChannelNo, m_iStreamNo);
	cls.SetDlgType(DLG_TYPE_MODIFY);
	cls.SetPicInfo(tInfo);
	cls.DoModal();
	m_pDlgPicEdit = NULL;
	UI_ShowPage(m_iCurPage);
}

void CLS_DlgFacePic::OnBnClickedBtnPicDelete()
{
	//Select a record first
	POSITION pPos = m_lstFaceInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos) {
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}
	int iTotal = m_lstFaceInfo.GetItemCount();
	int iCount = 0;
	int iIndex = m_lstFaceInfo.GetNextSelectedItem(pPos);
	while(iIndex >= 0 && iIndex < FACE_MAX_PAGE_COUNT)
	{
		FaceDelete tInfo = {0};
		tInfo.iSize = sizeof(tInfo);
		tInfo.iChanNo = m_iChannelNo;
		tInfo.iLibKey = m_tFaceInfo[iIndex].tFace.iLibKey;
		tInfo.iFaceKey = m_tFaceInfo[iIndex].tFace.iFaceKey;

		FaceReply tReply = {0};
		int iRet = FaceConfig(FACE_CMD_DELETE, &tInfo, tInfo.iSize, &tReply, sizeof(tReply));
		if (0 != iRet || 0 != tReply.iResult)
		{
			MessageBox("Operation failed!", "Tips", MB_OK);
			break;
		}
		iIndex = m_lstFaceInfo.GetNextSelectedItem(pPos);
		iCount ++;
	}
	if (iCount >= iTotal && m_iCurPage > 0)
	{
		m_iCurPage -= 1;
	}

	UI_ShowPage(m_iCurPage);
		}

void CLS_DlgFacePic::OnNMDblclkLstPicInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	POSITION pPos = m_lstFaceInfo.GetFirstSelectedItemPosition();
	if (NULL != pPos)
	{
		OnBnClickedBtnPicModify();
	}
	*pResult = 0;
}

void CLS_DlgFacePic::OnBnClickedBtnPicPageFirst()
{
	UI_ShowPage(0);
}

void CLS_DlgFacePic::OnBnClickedBtnPicPagePre()
{
	UI_ShowPage(m_iCurPage - 1);
}

void CLS_DlgFacePic::OnBnClickedBtnPicPageNext()
{
	UI_ShowPage(m_iCurPage + 1);
}

void CLS_DlgFacePic::OnBnClickedBtnPicPageLast()
{
	UI_ShowPage(m_iTolalPage - 1);
}

void CLS_DlgFacePic::OnCbnSelchangeCboPicPage()
{
	UI_ShowPage(m_cboPage.GetCurSel());
}

UINT __stdcall ThreadInport(LPVOID lpParam)
{
	CLS_DlgFacePic* pThis = (CLS_DlgFacePic*)lpParam;
	if (FALSE == IsBadReadPtr(pThis, sizeof(CLS_DlgFacePic)) && IsWindow(pThis->GetSafeHwnd()))
	{
		pThis->OnTheadInPort();
	}
	return 0;
}

void CLS_DlgFacePic::OnTheadInPort()
{
	CLS_DlgCfgProcess* pclsDlg = (CLS_DlgCfgProcess*)m_pDlgPreocess;
	if (NULL == pclsDlg)
	{
		return;
	}

	FaceEdit tEdit = {0};
	tEdit.iSize = sizeof(FaceEdit);
	tEdit.iChanNo = m_iChannelNo;
	tEdit.tFace.iSize = sizeof(FaceInfo);
	tEdit.tFace.iModeling = 1;
	tEdit.tFace.iLibKey = (int)m_cboLibKey.GetItemData(m_cboLibKey.GetCurSel());
	tEdit.tFace.iOptType = 1;	//1 Add 2 Modify
	strcpy_s(tEdit.tFace.cBirthTime, LEN_16, GetCurTimeStr());

	while(m_blInportThread)
	{
		CString cstrPath = m_listInportFacePic.front();
		m_listInportFacePic.pop_front();
		CString cstrName = PathFindFileName(cstrPath);

		//Start Import
		tEdit.tFace.iFileType = GetFaceFileType(cstrName);
		int iPos = cstrName.Find("_FACE_");
		if (iPos > 0)
		{
			cstrName = cstrName.Left(iPos);
		}	
		if (cstrName.GetLength() >= LEN_64)
		{
			cstrName = cstrName.Left(LEN_64-1);
		}
		iPos = cstrName.Find(".");
		if (iPos > 0)
		{
			cstrName = cstrName.Left(iPos);
		}

		strcpy_s(tEdit.cFacePic, sizeof(tEdit.cFacePic), (char*)(LPCSTR)cstrPath);
		strcpy_s(tEdit.tFace.cName, sizeof(tEdit.tFace.cName), (char*)(LPCSTR)cstrName);

		BOOL blRet = FALSE;
		FaceReply tReply = {0};
		int iRet = FaceConfig(FACE_CMD_EDIT, &tEdit, sizeof(tEdit), &tReply, sizeof(FaceReply));
		if (0 == iRet && 0 == tReply.iResult)
		{
			blRet = TRUE;
		}
		int iCount = (int)m_listInportFacePic.size();
		pclsDlg->SetLeftCount(iCount);
		pclsDlg->SetResult(blRet);
		if (iCount <= 0)
		{
			break;
		}
	}
}

void CLS_DlgFacePic::OnBnClickedBtnPicInport()
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);
	m_listInportFacePic.clear();

	//Select Imported Folder
	CString cstrFilePath = BrowseFolder();
	if (cstrFilePath.IsEmpty())
	{
		return;
	}

	//Read all pictures in the folder
	CFileFind exeFinder;
	BOOL exeFinding = exeFinder.FindFile(cstrFilePath + "*.*");
	while (exeFinding)
	{
		exeFinding = exeFinder.FindNextFile();
		CString cstrPath = exeFinder.GetFilePath();
		CString title = exeFinder.GetFileTitle();
		if (title == "" || title == ".")
		{
			continue;
		}
		int ret = _chdir(CT2A(cstrPath));	
		if (ret == 0)
		{
			continue;
		}	

		CString cstrName = PathFindFileName(cstrPath);
		if (cstrName.GetLength() >= LEN_64)
		{
			continue;
		}

		int iFileType = GetFaceFileType(cstrPath);
		if (iFileType >= 0)
		{
			m_listInportFacePic.push_back(cstrPath);
		}		
	}
	exeFinder.Close();

	//Total imports
	int iCount = (int)m_listInportFacePic.size();
	if (iCount <= 0)
	{
		MessageBox("There is no picture of the conditions!", "Tips", MB_OK);
		return;
	}

	if(1 == m_iChanCount)
	{
		MessageBox("The intelligent analysis will be suspended when the face image is imported!", "Tips", MB_OK);
		
		VCASuspend tInPara = {0};
		tInPara.iStatus = 0;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		tInPara.iDevType = 0;	//0-IPC, 1-NVR

		VCASuspendResult tSuspendRet = {0};
		tSuspendRet.iBufSize = sizeof(tSuspendRet);
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNo, SYNC_NET_CLIENT_VCA_SUSPEND, &tInPara, sizeof(tInPara), &tSuspendRet, sizeof(tSuspendRet));
		if (0 == iRet && 1==tSuspendRet.iResult)
		{
			
		}
		else
		{
			MessageBox("Intelligent analysis resources are being used,suspend intelligent analysis failure!", "Tips", MB_OK);
			return;
		}
	}
	
	CLS_DlgCfgProcess clsDlg;
	m_pDlgPreocess = &clsDlg;
	clsDlg.SetTotalCount(iCount);
	clsDlg.SetLeftCount(iCount);

	//Create thread and import pictures
	m_blInportThread = TRUE;
	HANDLE hImportThread = (HANDLE)_beginthreadex(NULL,0, &ThreadInport, this, 0, NULL);
	if (NULL == hImportThread)
	{
		m_pDlgPreocess = NULL;
		
		if(1 == m_iChanCount)
		{
			VCASuspend tInPara = {0};
			tInPara.iStatus = 1;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
			tInPara.iDevType = 0;	//0-IPC, 1-NVR

			VCASuspendResult tSuspendRet = {0};
			tSuspendRet.iBufSize = sizeof(tSuspendRet);
			int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNo, SYNC_NET_CLIENT_VCA_SUSPEND, &tInPara, sizeof(tInPara), &tSuspendRet, sizeof(tSuspendRet));
		}

		MessageBox("Create the export thread failure!", "Tips", MB_OK);
		return;
	}

	clsDlg.DoModal();
	m_blInportThread = FALSE;

	//Exit thread
	if(WaitForSingleObject(hImportThread, 500) != WAIT_OBJECT_0)
	{
		SuspendThread(hImportThread);
		TerminateThread(hImportThread,0);
	}
	CloseHandle(hImportThread);
	hImportThread = NULL;

	m_pDlgPreocess = NULL;

	if(1 == m_iChanCount)
	{
		VCASuspend tInPara = {0};
		tInPara.iStatus = 1;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		tInPara.iDevType = 0;	//0-IPC, 1-NVR

		VCASuspendResult tSuspendRet = {0};
		tSuspendRet.iBufSize = sizeof(tSuspendRet);
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNo, SYNC_NET_CLIENT_VCA_SUSPEND, &tInPara, sizeof(tInPara), &tSuspendRet, sizeof(tSuspendRet));
		if (0 == iRet && 1==tSuspendRet.iResult)
		{
			//MessageBox("Intelligent analysis is reinstated successful!", "Tips", MB_OK);
		}
	}

	//Update List
	OnBnClickedBtnPicQuery();
}

void CLS_DlgFacePic::OnBnClickedBtnPicOutportAll()
{
	//Whether there is query
	if (m_lstFaceInfo.GetItemCount() <= 0)
	{
		MessageBox("Please query first!", "Tips", MB_OK);
		return;
	}

	//Select export path
	m_cstrDLPicPath = BrowseFolder();
	if (m_cstrDLPicPath.IsEmpty())
	{
		return;
	}
	
	//Clear download list
	m_listDLFacePic.clear();
	m_pDlgPreocess = NULL;

	//To query all the base maps, check the first page first
	int iPageNo = 0;
	FaceQuery tQuery = m_tQueryInfo;
	FaceQueryResult tResult[FACE_MAX_PAGE_COUNT];
	while(TRUE)
	{
		tQuery.iPageNo = iPageNo;
		tQuery.iChanNo = m_iChannelNo;
		memset(&tResult, 0, sizeof(tResult));
		int iRet = FaceConfig(FACE_CMD_QUERY, &tQuery, sizeof(tQuery), &tResult, sizeof(FaceQueryResult));
		if (0 != iRet) 
		{
			MessageBox("Face base picture Query failure, ret: "+IntToStr(iRet) + ", page:" + IntToStr(tQuery.iPageNo), "Tips", MB_OK);
			return;
		}
		for (int i = 0; i < tResult[0].iPageCount && i < FACE_MAX_PAGE_COUNT; ++i)
		{
			FaceQueryResult tRet = tResult[i];
			m_listDLFacePic.push_back(tRet.tFace);
		}
		int iPageCount = tResult[0].iTotal / FACE_MAX_PAGE_COUNT;
		if (tResult[0].iTotal % FACE_MAX_PAGE_COUNT > 0)
		{
			iPageCount += 1;
		}
		iPageNo++;
		if (iPageNo >= iPageCount) 
		{
			break;
		}
	}

	CLS_DlgCfgProcess cls;
	m_pDlgPreocess = &cls;
	cls.SetTotalCount((int)m_listDLFacePic.size());
	cls.SetLeftCount((int)m_listDLFacePic.size());
	//Start downloading the base map
	FaceInfo tFace = m_listDLFacePic.front();
	m_listDLFacePic.pop_front();
	if (0 != StartDownLoadFacePic(tFace, m_cstrDLPicPath))
	{	
		StartDownloadNextFacePic(FALSE);
	}

	//Show Progress
	cls.DoModal();

	//Exit Display
	m_pDlgPreocess = NULL;
	m_listDLFacePic.clear();
}

void CLS_DlgFacePic::StartDownloadNextFacePic(int iRet)
{
	CLS_DlgFacePicEdit* pclsEdit = (CLS_DlgFacePicEdit*)m_pDlgPicEdit;
	if (NULL != pclsEdit)
	{
		pclsEdit->ShowDownloadPic(m_cstrLocalPicPath);
	}

	CLS_DlgCfgProcess* pclsProcss = (CLS_DlgCfgProcess*)m_pDlgPreocess;
	if (NULL != pclsProcss)
	{
		pclsProcss->SetLeftCount((int)m_listDLFacePic.size());
		pclsProcss->SetResult(iRet);		
		if (m_listDLFacePic.size() > 0)
		{
			int iRet = StartDownLoadFacePic(m_listDLFacePic.front(), m_cstrDLPicPath);
			m_listDLFacePic.pop_front();
			if (0 != iRet)
			{	
				StartDownloadNextFacePic(FALSE);
			}
		}	
	}
}

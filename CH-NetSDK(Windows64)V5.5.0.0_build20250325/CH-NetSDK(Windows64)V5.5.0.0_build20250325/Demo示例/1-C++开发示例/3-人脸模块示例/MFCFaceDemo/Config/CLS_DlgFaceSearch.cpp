
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceSearch.h"
#include "CLS_DlgFacePicEdit.h"
#include "CommonCitys.h"

typedef enum{
	ITEM_PIC_INDEX = 0,					//Serial No
	ITEM_PIC_NAME,						//Face Name
	ITEM_PIC_SEX,						//Gender
	ITEM_PIC_BIRTH,						//Date of birth
	ITEM_PIC_NATION,					//Nationalities
	ITEM_PIC_PLACE,						//Native place
	ITEM_PIC_CARDTYPE,					//Certificate type
	ITEM_PIC_CARDNO,					//Certificate No
}ITEM_FACE_SEARCH;

IMPLEMENT_DYNAMIC(CLS_DlgFaceSearch, CLS_PageBase)

CLS_DlgFaceSearch::CLS_DlgFaceSearch(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceSearch::IDD, pParent)
{
	m_pImageLocal = NULL;
	m_pDlgPicEdit = NULL;
}

CLS_DlgFaceSearch::~CLS_DlgFaceSearch()
{
	SAFE_DESTORY_IMAGE(m_pImageLocal);
}

void CLS_DlgFaceSearch::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_SEARCH_LIB, m_cboLibKey);
	DDX_Control(pDX, IDC_SLD_SEARCH_SIMILAR, m_sldSimilar);
	DDX_Control(pDX, IDC_LST_SEARCH_INFO, m_lstFaceInfo);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceSearch, CLS_PageBase)
	ON_WM_PAINT()
	ON_BN_CLICKED(IDC_BTN_SEARCH_PATH, &CLS_DlgFaceSearch::OnBnClickedBtnSearchPath)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_SEARCH_SIMILAR, &CLS_DlgFaceSearch::OnNMCustomdrawSldSearchSimilar)
	ON_BN_CLICKED(IDC_BTN_SEARCH_SEARCH, &CLS_DlgFaceSearch::OnBnClickedBtnSearchSearch)
	ON_NOTIFY(NM_DBLCLK, IDC_LST_SEARCH_INFO, &CLS_DlgFaceSearch::OnNMDblclkLstSearchInfo)
END_MESSAGE_MAP()

void CLS_DlgFaceSearch::UI_Init()
{
	//Similarity
	m_sldSimilar.SetRange(0, 100);
	m_sldSimilar.SetPos(80);
	m_sldSimilar.SetTicFreq(1);	
	//List
	m_lstFaceInfo.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_INDEX, "No.", LVCFMT_LEFT, 40, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_NAME, "Name", LVCFMT_LEFT, 100, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_SEX, "Gender", LVCFMT_LEFT, 60, -1);			
	m_lstFaceInfo.InsertColumn(ITEM_PIC_BIRTH, "Birthday", LVCFMT_LEFT, 70, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_NATION, "Nationality", LVCFMT_LEFT, 80, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_PLACE, "Native Place", LVCFMT_LEFT, 100, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_CARDTYPE, "Certificate Type", LVCFMT_LEFT, 115, -1);
	m_lstFaceInfo.InsertColumn(ITEM_PIC_CARDNO, "Certificate No.", LVCFMT_LEFT, 120, -1);
}

void CLS_DlgFaceSearch::UI_UptateData()
{
	QueryLibkey(m_cboLibKey);
}

void CLS_DlgFaceSearch::OnPaint()
{
	CPaintDC dc(this);
	if (NULL != m_pImageLocal)
	{
		ShowImage(m_pImageLocal, GetDlgItem(IDC_STC_SEARCH_PICSHOW));
	}
}


void CLS_DlgFaceSearch::OnBnClickedBtnSearchPath()
{
	SAFE_DESTORY_IMAGE(m_pImageLocal);

	CString cstrPath;
	CFileDialog clsFileDlg(TRUE, NULL, NULL, OFN_FILEMUSTEXIST|OFN_HIDEREADONLY, _T("(*.jpg;*.jpeg;*.png)|*.jpg;*.jpeg;*.png||"));
	if (IDOK == clsFileDlg.DoModal())
	{
		cstrPath = clsFileDlg.GetPathName();
		if (cstrPath.GetLength() >= LEN_256)
		{
			MessageBox("The path is too long. Please choose again!", "Tips", MB_OK);
			SetDlgItemText(IDC_EDT_SEARCH_PICPATH, "");
			return;
		}
		SetDlgItemText(IDC_EDT_SEARCH_PICPATH, cstrPath);
	}

	if (!cstrPath.IsEmpty())
	{	
		m_pImageLocal = LoadAndShowImage(cstrPath, GetDlgItem(IDC_STC_SEARCH_PICSHOW));
	}
}

void CLS_DlgFaceSearch::OnNMCustomdrawSldSearchSimilar(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_SEARCH_SILIMAR, m_sldSimilar.GetPos());
	*pResult = 0;
}

void CLS_DlgFaceSearch::OnBnClickedBtnSearchSearch()
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);	
	
	//Cutout first
	FaceCutEx tCut = {0};
	tCut.iSize = sizeof(tCut);
	tCut.iPicType = 0;		//0-jpg, 1-png
	tCut.iChanNo = 0;
	tCut.iPageNo = 0;
	tCut.iPageCount = 1;	//Only one face is selected for retrieval
	GetDlgItem(IDC_EDT_SEARCH_PICPATH)->GetWindowText(tCut.cPicPath, sizeof(tCut.cPicPath));
	if (0 == strlen(tCut.cPicPath))
	{
		MessageBox("Please select a picture first!", "Tips", MB_OK);
		return;
	}

	m_lstFaceInfo.DeleteAllItems();
	m_vecFaceInfo.clear();

	FaceCutQueryResult tCutRet = {0};
	int iRet = FaceConfig(FACE_CMD_CUT_EX, &tCut, sizeof(tCut), &tCutRet,sizeof(FaceCutQueryResult));
	if (0 != iRet || 0 == strlen(tCutRet.cFileName)) 
	{
		MessageBox("Face matching failure!", "Tips", MB_OK);
		return;
	}

	//Search the matting results
	FaceSearch tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iTaskId = tCutRet.iTaskId;
	tInfo.iSimilar = m_sldSimilar.GetPos();
	tInfo.iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);
	strcpy_s(tInfo.cPicName, sizeof(tInfo.cPicName), tCutRet.cFileName);
	tInfo.iPageCount = FACE_MAX_PAGE_COUNT;

	while(TRUE)
	{
		FaceQueryResult tSearchRet[FACE_MAX_PAGE_COUNT];
		int iRet = FaceConfig(FACE_CMD_SEARCH, &tInfo, tInfo.iSize, &tSearchRet, sizeof(FaceQueryResult));
		if (0 != iRet) 
		{
			MessageBox("Face searching failure!", "Tips", MB_OK);
			return;
		}
		int iTotalCount = tSearchRet[0].iTotal;
		int iTolalPage = iTotalCount / FACE_MAX_PAGE_COUNT;
		if (iTotalCount % FACE_MAX_PAGE_COUNT > 0)
		{
			iTolalPage++;
		}
		tInfo.iPageNo++;
		
		//Add to container
		for (int i = 0; i < tSearchRet[0].iPageCount; ++i)
		{
			if (tSearchRet[i].iSize <= 0)
			{
				break;
			}
			m_vecFaceInfo.push_back(tSearchRet[i].tFace);
		}

		if (tInfo.iPageNo >= iTolalPage) 
		{
			break;
		}
	}

	//Update List
	int iVecSize = (int)m_vecFaceInfo.size();
	for (int i = 0; i < iVecSize; ++i)
	{
		FaceInfo tInfo = m_vecFaceInfo[i];
		int iIndex = m_lstFaceInfo.GetItemCount();
		m_lstFaceInfo.InsertItem(iIndex, _T(""));
		m_lstFaceInfo.SetItemData(iIndex, tInfo.iFaceKey);
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_INDEX, IntToStr(iIndex + 1));
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_NAME, tInfo.cName);
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_SEX, CONST_CSTR_SEX[tInfo.iSex]);
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_BIRTH, tInfo.cBirthTime);
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_NATION, CONST_CSTR_NATION[tInfo.iNation]);
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_PLACE, GetPlaceStr(tInfo.iPlace));
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_CARDTYPE, CONST_CSTR_CARD[tInfo.iCertType]);
		m_lstFaceInfo.SetItemText(iIndex, ITEM_PIC_CARDNO, tInfo.cCertNum);
	}
}

void CLS_DlgFaceSearch::OnNMDblclkLstSearchInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	*pResult = 0;
	POSITION pPos = m_lstFaceInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos)
	{
		return;
	}
	int iPos = m_lstFaceInfo.GetNextSelectedItem(pPos);
	if (iPos < 0 || iPos >= (int)m_vecFaceInfo.size()){
		return;
	}

	CLS_DlgFacePicEdit cls;
	m_pDlgPicEdit = &cls;

	FaceInfo tInfo = m_vecFaceInfo[iPos];
	CString cstrPath = GetCurModulePath();
	StartDownLoadFacePic(tInfo, cstrPath);

	cls.SetDlgType(DLG_TYPE_SHOWINFO);
	cls.SetPicInfo(tInfo);
	cls.DoModal();
	m_pDlgPicEdit = NULL;
}

void CLS_DlgFaceSearch::StartDownloadNextFacePic(int iRet)
{
	CLS_DlgFacePicEdit* pclsEdit = (CLS_DlgFacePicEdit*)m_pDlgPicEdit;
	if (NULL != pclsEdit)
	{
		pclsEdit->ShowDownloadPic(m_cstrLocalPicPath);
	}
}
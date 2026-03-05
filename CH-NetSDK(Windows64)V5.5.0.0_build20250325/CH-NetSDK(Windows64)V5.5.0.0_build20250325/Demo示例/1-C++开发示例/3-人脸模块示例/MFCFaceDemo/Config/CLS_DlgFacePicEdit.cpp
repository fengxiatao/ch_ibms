
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFacePicEdit.h"
#include "CommonCitys.h"

enum CERT_TYPE
{
	CERT_TYPE_ALL = 0,
	CERT_TYPE_RESIDENT_ID,
	CERT_TYPE_OFFICER_ID,
	CERT_TYPE_PASSPORT_ID,	//passport
	CERT_TYPE_STAFF_ID		//Employee No
};

#define CERT_ID_LEN 18

IMPLEMENT_DYNAMIC(CLS_DlgFacePicEdit, CLS_PageBase)

CLS_DlgFacePicEdit::CLS_DlgFacePicEdit(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFacePicEdit::IDD, pParent)
{
	memset(&m_tFaceInfo, 0, sizeof(m_tFaceInfo));
	m_pImage = NULL;
	m_iDlgType = DLG_TYPE_ADD;
}

CLS_DlgFacePicEdit::~CLS_DlgFacePicEdit()
{
	SAFE_DESTORY_IMAGE(m_pImage);

}

void CLS_DlgFacePicEdit::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDT_PICEDIT_NAME, m_edtName);
	DDX_Control(pDX, IDC_EDT_PICEDIT_CARDNUM, m_edtCardNum);
	DDX_Control(pDX, IDC_CBO_PICEDIT_SEX, m_cboSex);
	DDX_Control(pDX, IDC_CBO_PICEDIT_CARDTYPE, m_cboCardType);
	DDX_Control(pDX, IDC_DT_PICEDIT_BIRTH, m_dtBirth);
	DDX_Control(pDX, IDC_CBO_PICEDIT_PROVIENCE, m_cboProvience);
	DDX_Control(pDX, IDC_CBO_PICEDIT_CITY, m_cboCity);
	DDX_Control(pDX, IDC_CBO_PICEDIT_NATION, m_cboNation);
	DDX_Control(pDX, IDC_EDIT_COMPANY, m_edtCompany);
	DDX_Control(pDX, IDC_EDIT_ADDRESS, m_edtAddress);
	DDX_Control(pDX, IDC_COMBO_COUNTRY, m_cboCountry);
}

BEGIN_MESSAGE_MAP(CLS_DlgFacePicEdit, CLS_PageBase)
	ON_WM_CLOSE()
	ON_WM_PAINT()
	ON_BN_CLICKED(IDC_BTN_PICEDIT_UPLOAD, &CLS_DlgFacePicEdit::OnBnClickedBtnPiceditUpload)
	ON_BN_CLICKED(IDC_BTN_PICEDIT_OK, &CLS_DlgFacePicEdit::OnBnClickedBtnPiceditOk)
	ON_BN_CLICKED(IDC_BTN_PICEDIT_CANCEL, &CLS_DlgFacePicEdit::OnBnClickedBtnPiceditCancel)
	ON_CBN_SELCHANGE(IDC_CBO_PICEDIT_PROVIENCE, &CLS_DlgFacePicEdit::OnCbnSelchangeCboPiceditProvience)
	ON_CBN_SELCHANGE(IDC_CBO_PICEDIT_CARDTYPE, &CLS_DlgFacePicEdit::OnCbnSelchangeCboPiceditCardtype)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


void CLS_DlgFacePicEdit::UI_Init()
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
		m_cboCardType.InsertString(i, CONST_CSTR_CARD[i]);
	}
	m_cboCardType.SetCurSel(0);
	//nation
	m_cboNation.ResetContent();
	for (int i = 0; i < (sizeof(CONST_CSTR_NATION)/sizeof(CString)); ++i) {
		m_cboNation.InsertString(i, CONST_CSTR_NATION[i]);
	}
	m_cboNation.SetCurSel(0);

	//Province and city
	UI_InitProvience(m_cboProvience);
	UI_InitCitys(m_cboCity, GetItemCurData(m_cboProvience));

	UI_InitCountry(m_cboCountry);

	m_dtBirth.SetFormat("yyyy-MM-dd");

	m_edtName.SetCanPaste(true);
	m_edtName.SetLimitText(63);

	m_edtCompany.SetCanPaste(true);
	m_edtCompany.SetLimitText(63);

	m_edtAddress.SetCanPaste(true);
	m_edtAddress.SetLimitText(63);

	m_edtCardNum.SetCharSet(CHARSET_UTF8);
	m_edtCardNum.SetLimitText(CERT_ID_LEN);	
	m_edtCardNum.SetFilterCallBack(CertNoFilter);
	m_edtCardNum.SetCanPaste(false);
}

void CLS_DlgFacePicEdit::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_PageBase::OnShowWindow(bShow, nStatus);
	if (m_tFaceInfo.iFaceKey <= 0 || !bShow)
	{
		return;
	}
	
	if (DLG_TYPE_MODIFY == m_iDlgType || DLG_TYPE_SHOWINFO == m_iDlgType)
	{
		m_edtName.SetWindowText(m_tFaceInfo.cName);
		m_edtCompany.SetWindowText(m_tFaceInfo.cCompany);
		m_edtAddress.SetWindowText(m_tFaceInfo.cAddress);
		SetDlgItemInt(IDC_EDIT_SCORELEVEL, m_tFaceInfo.iFaceObjScoreLevel);
		SetDlgItemInt(IDC_EDIT_SCORE, m_tFaceInfo.iFaceObjScore);
		m_edtCardNum.SetWindowText(m_tFaceInfo.cCertNum);
		m_cboSex.SetCurSel(m_tFaceInfo.iSex);
		m_cboCardType.SetCurSel(m_tFaceInfo.iCertType);
		COleDateTime tmBirthday = COleDateTime::GetCurrentTime();
		if (strlen(m_tFaceInfo.cBirthTime) > 0) {
			tmBirthday.ParseDateTime(m_tFaceInfo.cBirthTime, LOCALE_NOUSEROVERRIDE);
		}
		m_dtBirth.SetTime(tmBirthday);
		GetDlgItem(IDC_BTN_PICEDIT_UPLOAD)->ShowWindow(SW_HIDE);
		m_cboNation.SetCurSel(m_tFaceInfo.iNation);
		UI_UpdataProvience(m_cboProvience, HIWORD(m_tFaceInfo.iPlace));
		UI_InitCitys(m_cboCity, HIWORD(m_tFaceInfo.iPlace));
		UI_UpdataCitys(m_cboCity, LOWORD(m_tFaceInfo.iPlace));
		UI_UpdateCountry(m_cboCountry, m_tFaceInfo.iCountry);
	}
	
	if (DLG_TYPE_SHOWINFO == m_iDlgType)
	{
		m_edtName.EnableWindow(FALSE);
		m_edtCompany.EnableWindow(FALSE);
		m_edtAddress.EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_SCORE)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_SCORELEVEL)->EnableWindow(FALSE);
		m_cboSex.EnableWindow(FALSE);
		m_cboNation.EnableWindow(FALSE);
		m_dtBirth.EnableWindow(FALSE);
		m_cboProvience.EnableWindow(FALSE);
		m_cboCity.EnableWindow(FALSE);
		m_cboCardType.EnableWindow(FALSE);
		m_edtCardNum.EnableWindow(FALSE);
		GetDlgItem(IDC_BTN_PICEDIT_OK)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_BTN_PICEDIT_CANCEL)->ShowWindow(SW_HIDE);
		m_cboCountry.EnableWindow(FALSE);
	}
}

void CLS_DlgFacePicEdit::OnPaint()
{
	CPaintDC dc(this);
	if (NULL != m_pImage)
	{	
		ShowImage(m_pImage, GetDlgItem(IDC_STC_PICEDIT_PIC));
	}	
}

void CLS_DlgFacePicEdit::OnClose()
{
	CDialog::OnCancel();
}

void CLS_DlgFacePicEdit::ShowDownloadPic(CString _cstrPath)
{
	if (!_cstrPath.IsEmpty())
	{
		m_pImage = LoadAndShowImage(_cstrPath, GetDlgItem(IDC_STC_PICEDIT_PIC));
		DeleteFile(_cstrPath);
	}
}

BOOL CLS_DlgFacePicEdit::CertNoFilter( UINT nChar, UINT nRepCnt, UINT nFlags, HWND nhwnd /*= NULL */,CString cText /*= ""*/,bool bHasChinese /*= false*/ )
{
	//The resident ID card is a national uniform number, consisting of 18 Arabic numerals, and the last digit can be X
	//The military officer certificate is uniformly coded in the form of "Jun" prefix plus 7 digits [Jun Zi No. 1501270]
	byte pChar = (byte)nChar;
	if(VK_PASTE == pChar)
	{
		return TRUE;
	}

	if (0 != isdigit(pChar) || VK_BACK == pChar ||  ('a' <= pChar && pChar <= 'z') || ('A' <= pChar && pChar <= 'Z'))
	{
	}
	else
	{
		EDITBALLOONTIP bt;
		::ZeroMemory(&bt, sizeof(EDITBALLOONTIP));

		USES_CONVERSION;
		bt.cbStruct = sizeof(EDITBALLOONTIP);
		bt.pszTitle = A2CW(_T("Unacceptable Characters"));
		bt.pszText = A2CW(_T(" "));
		bt.ttiIcon = TTI_ERROR;

		Edit_ShowBalloonTip(nhwnd, &bt);
		return TRUE; // EM_SHOWBALLOONTIP
	}
	return FALSE;
}

void CLS_DlgFacePicEdit::OnBnClickedBtnPiceditUpload()
{
	SAFE_DESTORY_IMAGE(m_pImage);
	m_cstrPicPath.Empty();

	CString cstrPath;
	CFileDialog clsFileDlg(TRUE, NULL, NULL, OFN_FILEMUSTEXIST|OFN_HIDEREADONLY, _T("(*.jpg;*.jpeg;*.png)|*.jpg;*.jpeg;*.png||"));
	if (IDOK == clsFileDlg.DoModal())
	{
		cstrPath = clsFileDlg.GetPathName();
	}

	if (!cstrPath.IsEmpty())
	{
		m_pImage = LoadAndShowImage(cstrPath, GetDlgItem(IDC_STC_PICEDIT_PIC));
	}
	if (NULL != m_pImage)
	{
		m_cstrPicPath = cstrPath;
	}
}

void CLS_DlgFacePicEdit::OnCbnSelchangeCboPiceditProvience()
{
	UI_InitCitys(m_cboCity, GetItemCurData(m_cboProvience));
}

void CLS_DlgFacePicEdit::OnBnClickedBtnPiceditOk()
{
	FaceEdit tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;	
	tInfo.tFace.iLibKey = m_tFaceInfo.iLibKey;
	tInfo.tFace.iModeling = 1;		//1 modeling, 0 not modeling	
	tInfo.tFace.iFaceKey = m_tFaceInfo.iFaceKey; //0 means adding
	m_edtName.GetWindowText(tInfo.tFace.cName, sizeof(tInfo.tFace.cName));
	m_dtBirth.GetWindowText(tInfo.tFace.cBirthTime, sizeof(tInfo.tFace.cBirthTime));
	m_edtCompany.GetWindowText(tInfo.tFace.cCompany, sizeof(tInfo.tFace.cCompany));
	m_edtAddress.GetWindowText(tInfo.tFace.cAddress, sizeof(tInfo.tFace.cAddress));
	tInfo.tFace.iFaceObjScoreLevel = GetDlgItemInt(IDC_EDIT_SCORELEVEL);
	tInfo.tFace.iFaceObjScore = GetDlgItemInt(IDC_EDIT_SCORE);
	if (m_cstrPicPath.GetLength() > LEN_256)
	{
		MessageBox("Picture path is too long!", "Tips", MB_OK);
		return;
	}
	strcpy_s(tInfo.cFacePic, sizeof(tInfo.cFacePic), m_cstrPicPath);

	tInfo.tFace.iSex = GetItemCurData(m_cboSex);
	tInfo.tFace.iCertType = m_cboCardType.GetCurSel();
	if (0 != tInfo.tFace.iCertType)
	{
		m_edtCardNum.GetWindowText(tInfo.tFace.cCertNum, sizeof(tInfo.tFace.cCertNum));
	}
	tInfo.tFace.iNation = m_cboNation.GetCurSel();	
	tInfo.tFace.iPlace = MAKELONG(GetItemCurData(m_cboCity), GetItemCurData(m_cboProvience));
	tInfo.tFace.iCountry = GetItemCurData(m_cboCountry);

	tInfo.tFace.iOptType = 2;		//1 Add, 2 Modify
	if (0 == m_tFaceInfo.iFaceKey > 0)
	{
		tInfo.tFace.iOptType = 1;
		tInfo.tFace.iFileType = GetFaceFileType(m_cstrPicPath);
	}

	if (1 == tInfo.tFace.iOptType && 0 == strlen(tInfo.cFacePic))
	{
		MessageBox("Please upload the pictures first!", "Tips", MB_OK);
		return;
	}

	if (0 == strlen(tInfo.tFace.cName))
	{	//Face name is empty, return directly
		MessageBox("Name is empty!", "Tips", MB_OK);
		m_edtName.SetFocus();
		return;
	}
	if (!CheckFaceName((CString)tInfo.tFace.cName))
	{
		MessageBox("Name contains illegal characters!", "Tips", MB_OK);
		m_edtName.SetFocus();
		return;
	}

	if (!IsValidCertNo(tInfo.tFace.iCertType, tInfo.tFace.cCertNum))
	{
		MessageBox("Please enter the correct Certificate No.!", "Tips", MB_OK);
		m_edtCardNum.SetFocus();
		return;
	}

	if (strcmp(tInfo.tFace.cBirthTime, "1900-01-01") < 0)
	{
		MessageBox("Birthday should not be less than 1900-01-01!", "Tips", MB_OK);
		return;
	}

	if (strcmp(tInfo.tFace.cBirthTime, GetCurTimeStr()) > 0)
	{
		MessageBox("Birthday should not be greater than the date of the day!", "Tips", MB_OK);
		return;
	}

	FaceReply tReply= {0};
	int iRet = FaceConfig(FACE_CMD_EDIT, &tInfo, sizeof(tInfo), &tReply, sizeof(tReply));
	if (0 != iRet || 0 != tReply.iResult)
	{
		MessageBox(GetFailedReason(tReply.iResult, tReply.iDelLibProgress), "Tips", MB_OK);
		return;
	}
	CDialog::OnOK();
}

void CLS_DlgFacePicEdit::OnBnClickedBtnPiceditCancel()
{
	CDialog::OnCancel();
}

void CLS_DlgFacePicEdit::OnCbnSelchangeCboPiceditCardtype()
{
	if (0 == m_cboCardType.GetCurSel())
	{
		m_edtCardNum.SetWindowText("");
	}
}

bool CLS_DlgFacePicEdit::IsValidCertNo( int _iType, char* _pcCertNo )
{
	bool bRet = false;
	if (NULL == _pcCertNo || strlen(_pcCertNo) <= 0)
	{
		bRet = true;
		goto EXIT;
	}

	if (CERT_TYPE_RESIDENT_ID == _iType)
	{
		if (CERT_ID_LEN != strlen(_pcCertNo))
		{
			goto EXIT;
		}

		for (int i = 0; i < CERT_ID_LEN-1; ++i)
		{
			if (0 == isdigit(_pcCertNo[i]))
			{
				goto EXIT;
			}
		}
	}
	else if (CERT_TYPE_PASSPORT_ID == _iType || CERT_TYPE_STAFF_ID == _iType)
	{
		// No need to check, the input of numbers and letters has been controlled
	}
	else
	{
		for (int i = 0; i < (int)strlen(_pcCertNo); ++i)
		{
			if (0 == isdigit(_pcCertNo[i]))
			{
				goto EXIT;
			}
		}
	}

	bRet = true;

EXIT:
	return bRet;
}


#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceLibEdit.h"

IMPLEMENT_DYNAMIC(CLS_DlgFaceLibEdit, CLS_PageBase)

CLS_DlgFaceLibEdit::CLS_DlgFaceLibEdit(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceLibEdit::IDD, pParent)
{
	memset(&m_tLibInfo, 0, sizeof(m_tLibInfo));
	m_blLocal = TRUE;
}

CLS_DlgFaceLibEdit::~CLS_DlgFaceLibEdit()
{
}

void CLS_DlgFaceLibEdit::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_SLD_FACE_LIB_EDIT_SIMILAR, m_sldSimilar);
	DDX_Control(pDX, IDC_EDT_FACE_LIB_NAME, m_edtLibName);
	DDX_Control(pDX, IDC_EDT_FACE_LIB_DESCRIP, m_edtDescrip);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceLibEdit, CDialog)
	ON_WM_CLOSE()
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_FACE_LIB_EDIT_CONFIRM, &CLS_DlgFaceLibEdit::OnBnClickedBtnFaceLibEditConfirm)
	ON_BN_CLICKED(IDC_BTN_FACE_LIB_EDIT_CANCLE, &CLS_DlgFaceLibEdit::OnBnClickedBtnFaceLibEditCancle)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_FACE_LIB_EDIT_SIMILAR, &CLS_DlgFaceLibEdit::OnNMCustomdrawSldFaceLibEditSimilar)
END_MESSAGE_MAP()

void CLS_DlgFaceLibEdit::UI_Init()
{
	m_edtLibName.SetLimitText(63);
	m_edtLibName.SetCanPaste(true);

	m_edtDescrip.SetLimitText(63);
	m_edtDescrip.SetCanPaste(true);
	
	m_sldSimilar.SetRange(0, 100);
	m_sldSimilar.SetPos(80);
	m_sldSimilar.SetTicFreq(1);
	SetDlgItemInt(IDC_STC_FACE_LIB_EDIT_SIMILAR, 80);
}

void CLS_DlgFaceLibEdit::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_PageBase::OnShowWindow(bShow, nStatus);
	if (!bShow)
	{	
		return;
	}

	SetDlgItemText(IDC_EDT_FACE_LIB_NAME, m_tLibInfo.cName);
	SetDlgItemText(IDC_EDT_FACE_LIB_DESCRIP, m_tLibInfo.cExtrInfo);
	if (m_tLibInfo.iThreshold > 0 && m_tLibInfo.iThreshold <= 100)
	{
		m_sldSimilar.SetPos(m_tLibInfo.iThreshold);
		SetDlgItemInt(IDC_STC_FACE_LIB_EDIT_SIMILAR, m_tLibInfo.iThreshold);
	}
}

void CLS_DlgFaceLibEdit::OnNMCustomdrawSldFaceLibEditSimilar(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_FACE_LIB_EDIT_SIMILAR, m_sldSimilar.GetPos());
	*pResult = 0;
}

void CLS_DlgFaceLibEdit::OnClose()
{
	CDialog::OnOK();
}

void CLS_DlgFaceLibEdit::OnBnClickedBtnFaceLibEditCancle()
{
	CDialog::OnCancel();
}

void CLS_DlgFaceLibEdit::OnBnClickedBtnFaceLibEditConfirm()
{
	FaceLibEdit tEdit = {0};
	tEdit.iSize = sizeof(FaceLibEdit);
	tEdit.iChanNo = m_iChannelNo;
	tEdit.tFaceLib = m_tLibInfo;
	tEdit.tFaceLib.iSize = sizeof(FaceLibInfo);
	tEdit.tFaceLib.iAlarmType = 1; //0-Not Upload, 1-Upload
	tEdit.tFaceLib.iThreshold = m_sldSimilar.GetPos();
	CString cstrName, cstrDescribe;
	m_edtLibName.GetWindowText(cstrName);
	m_edtDescrip.GetWindowText(cstrDescribe);
	
	if (m_blLocal)
	{
		tEdit.tFaceLib.iOptType = m_tLibInfo.iLibKey > 0 ? 2 : 1;	//1-add, 2-modify
	}
	else
	{
		tEdit.tFaceLib.iOptType = 4;	//1-add, 2-modify 3-Lock password verification 4-Modify front-end similarity through NVR
	}
	strcpy_s(tEdit.tFaceLib.cName, sizeof(tEdit.tFaceLib.cName), cstrName);
	strcpy_s(tEdit.tFaceLib.cExtrInfo, sizeof(tEdit.tFaceLib.cExtrInfo), cstrDescribe);
	if (strlen(tEdit.tFaceLib.cName) <= 0) {
		MessageBox("Library name is empty!", "Tips", MB_OK);
		return;
	}
	if (!CheckFaceName(cstrName))
	{
		MessageBox("Library name contains illegal characters!", "Tips", MB_OK);
		m_edtLibName.SetFocus();
		return;
	}
	if(IsContainSubStr(cstrDescribe, ("#\":;'\\")))
	{
		MessageBox("Description contains illegal characters!", "Tips", MB_OK);
		m_edtDescrip.SetFocus();
		return;
	}

	FaceReply tReply = {0};
	int iRet = FaceConfig(FACE_CMD_LIB_EDIT, &tEdit, sizeof(FaceLibEdit), &tReply, sizeof(FaceReply));
	if (0 != iRet || 0 != tReply.iResult)
	{
		MessageBox(GetFailedReason(tReply.iResult), "Tips", MB_OK);
		return;
	}
	CDialog::OnOK();
}






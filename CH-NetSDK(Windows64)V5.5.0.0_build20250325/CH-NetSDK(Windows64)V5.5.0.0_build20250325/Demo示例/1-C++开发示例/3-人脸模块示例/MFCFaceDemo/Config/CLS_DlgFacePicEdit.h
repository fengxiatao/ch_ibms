#pragma once
#include "CLS_PageBase.h"
#include "TDFilterEdit.h"
#include "afxwin.h"

enum DLG_TYPE
{
	DLG_TYPE_ADD = 0,
	DLG_TYPE_MODIFY,
	DLG_TYPE_SHOWINFO,
};

class CLS_DlgFacePicEdit : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFacePicEdit)

public:
	CLS_DlgFacePicEdit(CWnd* pParent = NULL);
	virtual ~CLS_DlgFacePicEdit();

	enum { IDD = IDD_DLG_CFG_FACE_PIC_EDIT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX); 

	DECLARE_MESSAGE_MAP()

private:
	FaceInfo		m_tFaceInfo;
	CString			m_cstrPicPath;
	CImage*			m_pImage;
	int				m_iDlgType;

public:
	TDFilterEdit	m_edtName;
	TDFilterEdit	m_edtCardNum;
	CComboBox		m_cboSex;
	CComboBox		m_cboCardType;
	CComboBox 		m_cboProvience;
	CComboBox 		m_cboCity;
	CComboBox 		m_cboNation;
	CDateTimeCtrl	m_dtBirth;
	TDFilterEdit    m_edtCompany;
	TDFilterEdit    m_edtAddress;
	CComboBox		m_cboCountry;

	void			SetDlgType(int _iType) {m_iDlgType=_iType;};
	void 			SetPicInfo(FaceInfo&_tInfo){m_tFaceInfo=_tInfo;};
	void 			ShowDownloadPic(CString _cstrPath);

	void 			UI_Init();

	static bool		IsValidCertNo( int _iType, char* _pcCertNo );
	static BOOL		CertNoFilter(UINT nChar, UINT nRepCnt, UINT nFlags, HWND nhwnd = NULL ,CString cText = "",bool bHasChinese = false);

	afx_msg void 	OnClose();
	afx_msg void 	OnPaint();
	afx_msg void	OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void 	OnBnClickedBtnPiceditUpload();
	afx_msg void 	OnBnClickedBtnPiceditOk();
	afx_msg void 	OnBnClickedBtnPiceditCancel();
	afx_msg void 	OnCbnSelchangeCboPiceditProvience();
	afx_msg void 	OnCbnSelchangeCboPiceditCardtype();
};

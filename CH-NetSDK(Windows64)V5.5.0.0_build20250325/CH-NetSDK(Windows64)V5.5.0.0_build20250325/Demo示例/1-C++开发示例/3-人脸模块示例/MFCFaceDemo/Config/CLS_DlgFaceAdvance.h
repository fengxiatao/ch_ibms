#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"

class CLS_DlgFaceAdvance : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceAdvance)

public:
	CLS_DlgFaceAdvance(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFaceAdvance();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_ADVANCE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	CEdit m_edtFilePath;
	CComboBox m_cboLibKey;

public:
	afx_msg void OnBnClickedBtnAdvLibkeyQuery();
	afx_msg void OnBnClickedBtnAdvFilePath();
	afx_msg void OnBnClickedBtnAdvImport();
	afx_msg void OnBnClickedBtnAdvExpor();
	
	
};

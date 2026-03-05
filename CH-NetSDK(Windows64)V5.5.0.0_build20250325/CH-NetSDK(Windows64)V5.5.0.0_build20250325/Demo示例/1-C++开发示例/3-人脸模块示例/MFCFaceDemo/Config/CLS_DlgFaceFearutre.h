#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"

class CLS_DlgFaceFearutre : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceFearutre)

public:
	CLS_DlgFaceFearutre(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFaceFearutre();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_FEATURE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	CEdit m_edtFeaturePicPath;
	CComboBox m_cboFeatureLibKey;
	CComboBox m_cboFeatureFacekey;

public:
	afx_msg void OnBnClickedBtnFeatureLibkey();
	afx_msg void OnBnClickedBtnFeatureFacekey();
	afx_msg void OnBnClickedBtnFeaturePicpath();
	afx_msg void OnBnClickedBtnFeatureQuery();
	afx_msg void OnBnClickedBtnFeatureCalc();
};

#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"


// CLS_CertificateAndAuthFile dialog

class CLS_CertificateAndAuthFile : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_CertificateAndAuthFile)

public:
	CLS_CertificateAndAuthFile(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_CertificateAndAuthFile();

// dialog data
	enum { IDD = IDD_DLG_CFG_GET_CERTIFICATE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	CString IntToCstr(int _iNum);
	CString ArrayToCstr(char *_cArray);

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonGetCertificate();
	afx_msg void OnBnClickedButtonCreateAuthfile();
	CComboBox m_cboRequestType;
	CComboBox m_cboCertificateCount;
	CEdit m_editCountryName;
	CEdit m_editPassWord;
	CEdit m_editStateName;
	CEdit m_editLocalityName;
	CEdit m_editOrgnazationName;
	CEdit m_editOrgnazationUnitName;
	CEdit m_editEmail;
	CEdit m_editSIPDeviceID;
	CListCtrl m_listCertificateInfo;
	CButton m_btGet;
	CButton m_btCreate;
};

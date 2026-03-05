#pragma once
#include "BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_HttpXmlCfg dialog

class CLS_HttpXmlCfg : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_HttpXmlCfg)

public:
	CLS_HttpXmlCfg(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_HttpXmlCfg();

// dialog data
	enum { IDD = IDD_DLG_CFG_HTTP_XML };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateText();
	afx_msg void OnBnClickedButtonSendXmlCfg();
private:
	CEdit m_edtXmlRequestUrl;
	CComboBox m_cboXmlOpt;
	CComboBox m_cboXmlNetMode;
	CEdit m_edtInputXml;
	CEdit m_edtOutputXml;
};

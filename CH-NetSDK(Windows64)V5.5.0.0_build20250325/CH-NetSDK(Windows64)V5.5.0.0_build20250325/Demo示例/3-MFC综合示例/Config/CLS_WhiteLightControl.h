#pragma once

#include "../BasePage.h"
#include "afxwin.h"
// CLS_WhiteLightControl dialog

class CLS_WhiteLightControl : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_WhiteLightControl)

public:
	CLS_WhiteLightControl(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_WhiteLightControl();
	


// dialog data
	enum { IDD = IDD_DLG_CFG_WHITE_LIGHT_CONTROL };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual BOOL OnInitDialog();
	void UpdatePageUI();
	void UpdatePageText();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNO;
public:
	CComboBox m_cboControlType;
	afx_msg void OnBnClickedButtonWhiteControlSet();
};

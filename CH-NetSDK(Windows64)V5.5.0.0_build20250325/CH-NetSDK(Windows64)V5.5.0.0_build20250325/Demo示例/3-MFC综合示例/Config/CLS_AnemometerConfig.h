#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"

// CLS_AnemometerConfig dialog

class CLS_AnemometerConfig : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_AnemometerConfig)

public:
	CLS_AnemometerConfig(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_AnemometerConfig();
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();

// dialog data
	enum { IDD = IDD_DIALOG_WSTABLE_USE_MODE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSet();
	afx_msg void OnBnClickedButtonGet();
	CComboBox m_cboSceneID;
	CComboBox m_cboUseMode;
	int m_iStartHour;
	int m_iStartMin;
	int m_iStopHour;
	int m_iStopMin;
	int m_iDetectTimeOut;
	CComboBox m_cboTableOpt;
	int m_iLowThreshold;
};

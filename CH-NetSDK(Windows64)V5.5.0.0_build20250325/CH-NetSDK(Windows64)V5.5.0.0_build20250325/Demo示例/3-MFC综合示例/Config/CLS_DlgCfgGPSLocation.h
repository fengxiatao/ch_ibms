#pragma once

#include "../BasePage.h"
#include "afxwin.h"

// CLS_DlgCfgGPSLocation dialog

class CLS_DlgCfgGPSLocation : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgCfgGPSLocation)

public:
	CLS_DlgCfgGPSLocation(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgCfgGPSLocation();

// dialog data
	enum { IDD = IDD_DLG_CFG_GPS_LOCATION };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);


	void UpdateUIText();
	void UpdatePageUI();

	CComboBox m_cboLongitude;
	CComboBox m_cboLatitude;
	CComboBox m_cboMode;
	afx_msg void OnBnClickedButtonGpsSet();
	afx_msg void OnBnClickedButtonIntervalSet();
	CButton m_chkUpdata;

	int UpdateGpsInterval();
	int UpdateGpsInfo();


	CEdit m_editInterval;
};

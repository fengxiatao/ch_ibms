#pragma once

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
// CLS_ItsRadarLedInfo dialog

class CLS_ItsRadarLedInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ItsRadarLedInfo)

public:
	CLS_ItsRadarLedInfo(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_ItsRadarLedInfo();

// dialog data
	enum { IDD = IDD_DLG_ITS_LED_INFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void UpdateUIText();
	void UpdatePageUI();

	int m_iLogonID;
	int m_iChannelNo;

	CComboBox m_cboLedType;
	CComboBox m_cboLedModel;
	CComboBox m_cboOsdHintType;
	CComboBox m_cboFountSize;
	CComboBox m_cboFountColor;
	CListCtrl m_lstLed;
	CListCtrl m_lstLedOsd;

	afx_msg void OnBnClickedButtonRadarLedQuery();
	afx_msg void OnBnClickedButtonRadarLedAdd();
	afx_msg void OnBnClickedButtonRadarLedEdit();
	afx_msg void OnBnClickedButtonRadarLedDel();
	afx_msg void OnBnClickedButtonRadarLedTest();
	afx_msg void OnNMClickListRadarLed(NMHDR *pNMHDR, LRESULT *pResult);

	int UpdateLedList();
	int UpdateLedUI(int _iIndex);
	int LedOpt(int _iOptType);

	int UpdateLedOsdList();
	int UpdateLedOsdUI(int _iIndex);
	int LedOsdOpt(int _iOptType);

	LedDevParamResult m_tLedResult[LED_DEVICE_MAX_NUM];
	LedDevOsdParamList m_tLedOsdResult[LED_DEVICE_MAX_NUM];
	
	afx_msg void OnBnClickedButtonRadarLedosdQuery();
	afx_msg void OnBnClickedButtonRadarLedosdAdd();
	afx_msg void OnBnClickedButtonRadarLedosdEdit();
	afx_msg void OnBnClickedButtonRadarLedosdDel();
	
	afx_msg void OnNMClickListRadarLedosd(NMHDR *pNMHDR, LRESULT *pResult);
	CEdit m_edtFastInsert;
};

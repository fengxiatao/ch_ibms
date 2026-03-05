#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"


// CLS_VCAEVENT_Lisence dialog

class CLS_VCAEVENT_Lisence : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_Lisence)

public:
	CLS_VCAEVENT_Lisence(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_Lisence();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_LIENCE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboFirstChar;
	CComboBox m_cboFirstLetter;
	CSliderCtrl m_sldVLoopSensitivity;

	CStringArray m_arrayProvince;//Provincial abbreviation string group

	void OnLanguageChanged();
	void UpdateUIText();
	void UpdatePageUI();

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnLisenceSet();
	afx_msg void OnBnClickedBtnLisenceDraw();
	CEdit m_edtLisenceAreaPoints;
	afx_msg void OnNMCustomdrawSlider1(NMHDR *pNMHDR, LRESULT *pResult);
};

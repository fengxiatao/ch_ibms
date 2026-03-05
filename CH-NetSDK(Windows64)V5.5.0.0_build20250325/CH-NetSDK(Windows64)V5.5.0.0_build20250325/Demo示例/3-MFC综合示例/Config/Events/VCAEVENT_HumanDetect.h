#pragma once
#include "VCAEventBasePage.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_VCAEVENT_HumanDetect dialog

class CLS_VCAEVENT_HumanDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_HumanDetect)

public:
	CLS_VCAEVENT_HumanDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_HumanDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_HUMAN_DETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void OnLanguageChanged();
	void UpdateUIText();
	void UpdatePageUI();
	CSliderCtrl m_sldMinSize;
	CSliderCtrl m_sldMaxSize;
	afx_msg void OnBnClickedButtonHumanSet();
	CButton m_chkEnable;
	CButton m_chkTargetBox;
	afx_msg void OnNMCustomdrawSliderHumanMinsize(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderHumanMansize(NMHDR *pNMHDR, LRESULT *pResult);
};

#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


// CLS_VcaFireWorkDetect dialog

class CLS_VcaFireWorkDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaFireWorkDetect)

public:
	CLS_VcaFireWorkDetect(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VcaFireWorkDetect();

// Dialog Data
	enum { IDD = IDD_DIALOG_FIREWORK };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSet();
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void OnLanguageChanged();
	void UpdatePageUI();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
	CButton m_chkRule;
	CButton m_chkAlarmCount;
	CButton m_chkEvent;
	CButton m_chkTarget;
	CComboBox m_cboAreaColor;
	CComboBox m_cboAlarmColor;
	//CSliderCtrl m_sliFireSensitive;
	CSliderCtrl m_sliSmokeSensitive;
	CComboBox m_cboChkMode;
	CEdit m_edtWaitTime;
	CSliderCtrl m_sliFireSensitive;
};

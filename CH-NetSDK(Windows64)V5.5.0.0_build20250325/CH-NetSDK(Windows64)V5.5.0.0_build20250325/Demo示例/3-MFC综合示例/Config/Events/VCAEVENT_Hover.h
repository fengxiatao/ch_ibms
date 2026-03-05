#pragma once
#include "afxwin.h"
#include "VCAEventBasePage.h"
#include "afxdtctl.h"
// CLS_VCAEVENT_Hover dialog

class CLS_VCAEVENT_Hover : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_Hover)

public:
	CLS_VCAEVENT_Hover(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_Hover();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_HOVER };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual	BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void OnLanguageChanged();
	void UpdateUIText();
	void CleanText();
	void UpdatePageUI();
	afx_msg void OnBnClickedBtnHoverSet();
	afx_msg void OnBnClickedBtnHoverDraw();
	afx_msg void OnEnChangeEdtHoverSensitivity();
	afx_msg void OnEnChangeEdtHoverMinDist();
	afx_msg void OnEnChangeEdtHoverRectangleArea();
	afx_msg void OnEnChangeEdtHoverHoverMinTime();

private:
	CComboBox m_cboAlarmColor;
	CComboBox m_cboUnalarmColor;
	CEdit m_edtSensitivity;
	CEdit m_edtHoverMinDist;
	CEdit m_edtRectArea;
	CEdit m_edtPointArea;
	CEdit m_edtHoverMinTime;
	CButton m_chkEventValid;
	CButton m_chkAlarmRule;
	CButton m_chkAlarmStat;
	CButton m_chkTargetBox;
	
};

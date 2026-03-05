#pragma once
#include "afxwin.h"
#include "VCAEventBasePage.h"

// CLS_VCAEVENT_Dredge dialog

class CLS_VCAEVENT_Dredge : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_Dredge)

public:
	CLS_VCAEVENT_Dredge(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_Dredge();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_DREDGE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
private:
	CButton m_chkDisplayRule;
	CButton m_chkDisplayAlarmNum;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboAreaColor;
	CComboBox m_cboRecgMode;

private:
	void AutoShowParam();
	void CheckValueLimits();
	void SetZoomControl();

public:
	void UI_UpdateDialog();
	void UI_UpdatePage();
	void UI_UpdateZoomControl();
	void OnLanguageChanged();
	void JudgeVCASetState(BOOL _bIsVcaStatus);
	void JudgeVCAStatus();

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnDredgeSet();
	afx_msg void OnBnClickedBtnDredgePointsDraw();
	afx_msg void OnBnClickedBtnDredgeStop();
	afx_msg void OnBnClickedBtnDredgeStart();
	afx_msg void OnBnClickedButtonDredgeAuto();
	afx_msg void OnEnChangeEdtZoomControl();

	CButton m_DredgeSet;
};

#pragma once
#include "VCAEventBasePage.h"
#include "afxcmn.h"

// CLS_VCAEVENT_LeaveBed dialog

class CLS_VCAEVENT_LeaveBed : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_LeaveBed)

public:
	CLS_VCAEVENT_LeaveBed(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_LeaveBed();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_LEAVEBED };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();

	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();
	
	CSliderCtrl m_sldLeaveTime;
	CComboBox m_cboColor;
	CComboBox m_cboAlarmColor;
	CSliderCtrl m_sldSensitive;
	CComboBox m_cboCurRegionNo;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CEdit m_editRegionPoins;
	CButton m_chkEventEnable;
	CComboBox m_cboDevType;

	VCAParaLeaveBed m_tVCAParaLeaveBed;
	
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnLeavebedSet();
	afx_msg void OnCbnSelchangeCboLeavebedCurRegionnum();
	afx_msg void OnBnClickedBtnLeavebedRegionDraw();
	afx_msg void OnNMCustomdrawSliderLeavebedSensitive(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderLeavebedLeavetime(NMHDR *pNMHDR, LRESULT *pResult);
};

#pragma once
#include "VCAEventBasePage.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_VCAEVENT_Sleep dialog

class CLS_VCAEVENT_Sleep : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_Sleep)

public:
	CLS_VCAEVENT_Sleep(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_Sleep();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_SLEEP };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	
	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();
	void UpdateDrawFinishInvalidRegionNum();

	CSliderCtrl m_sldSleepTime;
	CComboBox m_cboCurInvalidRegionNo;
	CEdit m_editInvalidRegionPoins;


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






	VCAParaSleep m_tVCAParaSleep;
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnSleepSet();
	afx_msg void OnCbnSelchangeCboSleepCurRegionnum();
	afx_msg void OnCbnSelchangeCboSleepCurInvalidRegionnum();
	afx_msg void OnBnClickedBtnSleepRegionDraw();
	afx_msg void OnBnClickedBtnSleepInvalidRegionDraw();
	afx_msg void OnNMCustomdrawSliderSleepSensitive(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderSleepSleeptime(NMHDR *pNMHDR, LRESULT *pResult);
};

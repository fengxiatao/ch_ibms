#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "VCAEventBasePage.h"


// CLS_VCAEVENT_NewDuty dialog

class CLS_VCAEVENT_NewDuty : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_NewDuty)

public:
	CLS_VCAEVENT_NewDuty(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_NewDuty();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_NEWDUTY };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();
	afx_msg void OnBnClickedBtnNewdutySet();
	afx_msg void OnBnClickedBtnNewdutyRegionDraw();
	afx_msg void OnCbnSelchangeCboNewdutyCurRegionnum();
	afx_msg void OnNMCustomdrawSliderNewdutySensitive(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderNewdutyLeavetime(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	
	CSliderCtrl m_sldLeaveTime;
	CComboBox m_cboColor;
	CSliderCtrl m_sldSensitive;
	CComboBox m_cboCurRegionNo;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CEdit m_editRegionPoins;
	CButton m_chkEventEnable;
	CComboBox m_cboDevType;
	VCAParaNewDuty m_tVCAParaNewDuty;
	CComboBox m_cboAlarmColor;
};

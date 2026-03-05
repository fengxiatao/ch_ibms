#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "VCAEventBasePage.h"

// CLS_VCAEVENT_SlipUp dialog

class CLS_VCAEVENT_SlipUp : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_SlipUp)

public:
	CLS_VCAEVENT_SlipUp(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_SlipUp();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_SLIPUP };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	
	CButton m_chkEventEnable;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CComboBox m_cboColor;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboDevType;
	CSliderCtrl m_sldSensitive;
	afx_msg void OnBnClickedBtnSlipupRegionDraw();
	CEdit m_editRegionPoins;
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnNMCustomdrawSliderSlipupSensitive(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnSlipupSet();
	CSliderCtrl m_sldSlipUpHeight;
	afx_msg void OnNMCustomdrawSldSlipupHeight(NMHDR *pNMHDR, LRESULT *pResult);
};

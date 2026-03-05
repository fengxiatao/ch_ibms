#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "VCAEventBasePage.h"



// CLS_VcaPROCURATORATEDUTY dialog

class CLS_VcaPROCURATORATEDUTY : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaPROCURATORATEDUTY)

public:
	CLS_VcaPROCURATORATEDUTY(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaPROCURATORATEDUTY();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_PROCURATORATEDUTY };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();
	void UpdateDrawFinishInvalidRegionNum();
	afx_msg void OnCbnSelchangeCboProcuratoratedutyCurRegionnum();
	afx_msg void OnCbnSelchangeCboProcuratoratedutyCurInvalidRegionnum();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnProcuratoratedutySet();
	afx_msg void OnBnClickedBtnProcuratoratedutyRegionDraw();
	afx_msg void OnBnClickedBtnProcuratoratedutyInvalidRegionDraw();
	afx_msg void OnNMCustomdrawSliderProcuratoratedutySensitive(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderProcuratoratedutySleeptime(NMHDR *pNMHDR, LRESULT *pResult);

	CComboBox m_cboColor;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboCurRegionNo;
	CComboBox m_cboDevType;
	CSliderCtrl m_sldSensitive;
	CSliderCtrl m_sldLeaveTime;
	CButton m_chkEventEnable;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CComboBox m_cboCurInvalidRegionNo;
	CButton m_chkShowTargetBox;
	CEdit m_editRegionPoins;
	CEdit m_editInvalidRegionPoins;
	VCAParaProcratorateDuty m_tVCAParaProcratorateDuty;
	CEdit m_edtPrisonerPoints;
	afx_msg void OnBnClickedBtnPrisonerRegionDraw();
};

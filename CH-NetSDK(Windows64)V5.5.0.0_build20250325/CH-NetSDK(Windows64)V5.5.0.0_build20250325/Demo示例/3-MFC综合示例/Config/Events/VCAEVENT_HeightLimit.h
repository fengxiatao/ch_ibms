#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "VCAEventBasePage.h"


// CLS_VCAEVENT_HeightLimit dialog

class CLS_VCAEVENT_HeightLimit : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_HeightLimit)

public:
	CLS_VCAEVENT_HeightLimit(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_HeightLimit();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_HEIGHTLIMIT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();
	afx_msg void OnBnClickedBtnHeightlimitSet();
	afx_msg void OnNMCustomdrawSliderHeightlimitSensitive(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderHeightlimitLeavetime(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnHeightlimitRegionDraw();
	afx_msg void OnCbnSelchangeCboHeightLimitCurRegionnum();
	
	CSliderCtrl m_sldLeaveTime;
	CComboBox m_cboColor;
	CSliderCtrl m_sldSensitive;
	CComboBox m_cboCurRegionNo;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CEdit m_editRegionPoins;
	CButton m_chkEventEnable;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboDevType;
	VCAParaHeightLimit m_tVCAParaHeightLimit;
};

#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_VcaAlone dialog

class CLS_VcaAlone : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaAlone)

public:
	CLS_VcaAlone(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaAlone();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_ALONE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	
	afx_msg void OnBnClickedBtnAloneSet();
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();

	
	VCAAlone m_tVCAAlone;
	afx_msg void OnCbnSelchangeCboAloneCurRegionnum();
	afx_msg void OnBnClickedBtnAloneRegionDraw();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	
	CButton m_chkEventEnable;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CComboBox m_cboColor;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboDevType;
	CComboBox m_cboCurRegionNo;
	CEdit m_editRegionPoins;
};

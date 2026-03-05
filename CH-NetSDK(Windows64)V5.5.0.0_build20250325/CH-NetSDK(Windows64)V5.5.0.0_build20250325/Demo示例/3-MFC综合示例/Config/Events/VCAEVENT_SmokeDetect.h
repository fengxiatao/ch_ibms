#pragma once
#include "stdafx.h"
#include "afxcmn.h"
#include "VCAEventBasePage.h"
#include "afxwin.h"

// CLS_VcaSmokeDetect dialog

class CLS_VcaSmokeDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaSmokeDetect)

public:
	CLS_VcaSmokeDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaSmokeDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_SMOKEDETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	void UpdateUIText();
	void UpdatePageUI();
	afx_msg void OnBnClickedBtnSmokedetectRegionDraw();
	afx_msg void OnBnClickedButtonSmokedetect();
	//afx_msg void OnCbnSelchangeComboSmokeDevType();
	afx_msg void OnBnClickedCheckSmokedetect();
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	CEdit m_editRegionPoints;
	CButton m_chkSmokeDetect;
	CComboBox m_cboDevType;
	vca_TPoint    m_tPoints[MAX_SMOKEDETECT_POINT_NUM];
};

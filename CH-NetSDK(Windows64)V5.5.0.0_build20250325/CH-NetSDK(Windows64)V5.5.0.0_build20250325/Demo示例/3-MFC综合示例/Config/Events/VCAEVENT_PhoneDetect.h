#pragma once
#include "stdafx.h"
#include "afxcmn.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "afxwin.h"

// CLS_PhoneDetect dialog

class CLS_VcaPhoneDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaPhoneDetect)

public:
	CLS_VcaPhoneDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaPhoneDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_PHONEDETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	void UpdateUIText();
	void UpdatePageUI();
	CButton m_chkPhoneDetect;
	afx_msg void OnBnClickedBtnPhonedetectRegionDraw();
	CEdit m_editRegionPoints;
	CComboBox m_cboDevType;
	afx_msg void OnBnClickedButtonPhonedetect();
	vca_TPoint    m_tPoints[MAX_PHONEDETECT_POINT_NUM];
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
};

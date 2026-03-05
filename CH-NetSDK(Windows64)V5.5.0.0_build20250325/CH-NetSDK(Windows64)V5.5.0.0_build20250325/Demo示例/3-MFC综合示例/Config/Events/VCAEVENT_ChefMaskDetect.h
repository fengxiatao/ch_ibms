#pragma once
#include "afxcmn.h"
#include "VCAEventBasePage.h"
#include "afxwin.h"

// CLS_VcaChefMaskDetect dialog

class CLS_VcaChefMaskDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaChefMaskDetect)

public:
	CLS_VcaChefMaskDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaChefMaskDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_CHEFMASKDETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	void UpdateUIText();
	void UpdatePageUI();
	void OnLanguageChanged();
	afx_msg void OnBnClickedButtonChefmaskdetect();
	afx_msg void OnBnClickedBtnChefmaskdetectRegionDraw();
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	vca_TPoint    m_tPoints[MAX_CHEFMASKDETECT_POINT_NUM];
	CEdit m_editRegionPoins;
	CButton m_chkHatMaskDetect;
	CComboBox m_cboPushMode;
	afx_msg void OnBnClickedCheckChefmaskdetect();
	CComboBox m_cboDevType;
	afx_msg void OnCbnSelchangeComboChefmaskDevType();
};

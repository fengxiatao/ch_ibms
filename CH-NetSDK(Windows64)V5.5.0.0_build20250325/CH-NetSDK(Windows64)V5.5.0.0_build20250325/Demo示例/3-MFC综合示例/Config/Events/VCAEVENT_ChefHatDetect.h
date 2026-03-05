#pragma once
#include "afxcmn.h"
#include "VCAEventBasePage.h"
#include "afxwin.h"

// CLS_VcaChefHatDetect dialog

class CLS_VcaChefHatDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaChefHatDetect)

public:
	CLS_VcaChefHatDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaChefHatDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_CHEFHATDETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	void UpdateUIText();
	void UpdatePageUI();
	void OnLanguageChanged();
	afx_msg void OnBnClickedButtonChefhatdetect();
	afx_msg void OnBnClickedBtnChefhatdetectRegionDraw();
protected:
	virtual void PreInitDialog();
public:
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	vca_TPoint    m_tPoints[MAX_CHEFHATDETECT_POINT_NUM];
	CEdit m_editRegionPoins;
	//CButton m_chkChef;
	CButton m_chkChefHatDetect;
	CComboBox m_cboDevType;
	afx_msg void OnBnClickedCheckChefhatdetect();
	CComboBox m_cboPushMod;
	virtual BOOL OnInitDialog();
	afx_msg void OnCbnSelchangeComboChefHatDevType();
};

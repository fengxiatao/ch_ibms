#pragma once
#include "afxcmn.h"
#include "VCAEventBasePage.h"
#include "afxwin.h"


// CLS_VcaChefDetect dialog

class CLS_VcaChefDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaChefDetect)

public:
	CLS_VcaChefDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaChefDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_CHEFDETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void OnLanguageChanged();
	void UpdateUIText();
	void UpdatePageUI();
	afx_msg void OnBnClickedBtnChefDetectRegionDraw();
	afx_msg void OnBnClickedButtonChefDetect();
private:
	vca_TPoint		m_tPointsArray[MAX_CHEFDETECT_POINT_COUNT];
	CComboBox		m_cboChefDevType;
	CComboBox		m_cboChefDetectType;
	CEdit			m_edtChefDetectSensitive;
	CComboBox		m_cboChefPushPicMode;
	CEdit			m_edtChefDetectPointCount;
	CEdit			m_edtChefDetectPointsArray;
	
public:
	afx_msg void OnBnClickedCheckEnableArith();
	CButton m_chkEnable;
	CButton m_chkChef;
	CButton m_chkChefHat;
	CButton m_chkChefMask;
	afx_msg void OnBnClickedButtonChefSetEnable();
	afx_msg void OnCbnSelchangeComboChefDevType();
};

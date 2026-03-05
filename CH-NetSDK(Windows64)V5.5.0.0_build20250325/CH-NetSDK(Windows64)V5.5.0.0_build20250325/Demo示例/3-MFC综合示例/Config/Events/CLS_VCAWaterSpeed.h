#pragma once
#include "afxwin.h"
#include "VCAEventBasePage.h"

// CLS_VCAWaterSpeed dialog

class CLS_VCAWaterSpeed : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAWaterSpeed)

public:
	CLS_VCAWaterSpeed(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAWaterSpeed();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_WATER_SPEED };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:

	void UpdateUIText();
	void UpdatePageUI();


	afx_msg void OnBnClickedBtnWaterSpeedSet();
	afx_msg void OnBnClickedBtnWaterSpeedRegionDraw();
	CButton m_chkEventEnable;
	CButton m_chkShowRule;
	CComboBox m_cboAreaColor;
	CComboBox m_cboLinkRecord;
	CComboBox m_cboAddPoint;
	CComboBox m_ApplayScene;
	CEdit m_edtPoint;


	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual BOOL OnInitDialog();
	CEdit m_edtFileterSensitive;
	CComboBox m_cboAreaType;
	CEdit m_edtLimitSmall;
	CEdit m_edtLimitBig;
	CComboBox m_cboShowType;
	CEdit m_edtSpeedRatio;
};

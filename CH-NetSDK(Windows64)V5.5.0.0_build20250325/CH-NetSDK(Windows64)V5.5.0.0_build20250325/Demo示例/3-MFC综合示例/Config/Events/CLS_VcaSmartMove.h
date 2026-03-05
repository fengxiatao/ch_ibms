#pragma once
#include "afxwin.h"
#include "VCAEventBasePage.h"


// CLS_VcaSmartMove dialog

class CLS_VcaSmartMove : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaSmartMove)

public:
	CLS_VcaSmartMove(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaSmartMove();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_SMART_MOVE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSmartMoveSet();
	afx_msg void OnBnClickedButtonSmartMoveDraw();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	CComboBox m_cboTargetTypeCheck;
	CEdit m_edtPoints;
	CComboBox m_cboParamType;
	CButton m_chkIsValid;
	vca_TPoint    m_tPoints[VCA_MAX_POLYGON_POINT_NUMEX];
	VCASmartMove m_tVCASmartMove;
	CComboBox m_cboCurRegionNo;
	afx_msg void OnCbnSelchangeComboCurDetectNum();
};

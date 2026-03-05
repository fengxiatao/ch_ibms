#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_VcaStranded dialog

class CLS_VcaStranded : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaStranded)

public:
	CLS_VcaStranded(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaStranded();

	

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_STRANDED };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedBtnStrandedSet();
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	
	vca_TPoint    m_tPoints[MAX_VCA_STRANDED_POINT_NUM];
	afx_msg void OnBnClickedBtnStrandedRegionDraw();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	
	CComboBox m_cboColor;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboDevType;
	CEdit m_editRegionPoins;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CButton m_chkEventEnable;
};

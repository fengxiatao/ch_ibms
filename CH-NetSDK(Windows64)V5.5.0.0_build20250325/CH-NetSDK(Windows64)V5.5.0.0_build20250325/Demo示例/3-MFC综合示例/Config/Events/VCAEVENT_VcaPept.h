#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

#define MAX_TARGET_TYPE		 31
#define MAX_ALARM_TYPE		 2
// CLS_VcaPept dialog

class CLS_VcaPept : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaPept)

public:
	CLS_VcaPept(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaPept();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_PEPT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();
	CButton m_cboEventEnable;
	CButton m_ckbDisplayRule;
	CButton m_ckbDisplayAlarmCount;
	CButton m_chkShowTargetBox;
	CComboBox m_cboRegionColor;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboAlarmType;
	CComboBox m_cboTargetType;
	CSliderCtrl m_sldSensitive;
	CSliderCtrl m_sldAlarmTime;
	CButton m_btnDraw;
	CButton m_ckbSnapEnable;
	afx_msg void OnBnClickedBtnPeptRegionDraw();
	afx_msg void OnNMCustomdrawSliderPeptSensitive(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderPeptAlarmTime(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnPeptSet();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	CEdit m_editRegionPoins;
	VCAPEPT m_tVcaPept;
	vca_TPoint    m_tPoints[MAX_VCA_PEPT_POINT_NUM];
	CButton	m_chkTargetType[MAX_TARGET_TYPE];
	CButton	m_chkZoneInvalid;
	afx_msg void OnBnClickedCheckSwitchSenceSnapEnable();
	CButton m_ckbUnvisualState;
	CButton m_test;
	CButton m_ckbZone;
	CButton m_ckbPept;
	CComboBox m_cboDevType;
	CButton m_ckbPet2;
	CButton m_ckbPept3;
};

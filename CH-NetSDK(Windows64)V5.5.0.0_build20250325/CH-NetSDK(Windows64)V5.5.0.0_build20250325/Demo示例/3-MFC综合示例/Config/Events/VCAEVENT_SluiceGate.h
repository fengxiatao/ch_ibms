#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


#define INNER_MAX_VCA_SLUICEGATE_COUNT	1
#define INNER_MAX_VCA_SLUICEGATE_LINE_COUNT	INNER_MAX_VCA_SLUICEGATE_COUNT*2

// CLS_VCAEVENT_SluiceGate dialog

class CLS_VCAEVENT_SluiceGate : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_SluiceGate)

public:
	CLS_VCAEVENT_SluiceGate(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VCAEVENT_SluiceGate();

// Dialog Data
	enum { IDD = IDD_DLG_VCAEVENT_SLUICEGATE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void UI_UpdateDialog();
	void UI_UpdateSdkParam();
	void OnLanguageChanged();
	afx_msg void OnBnClickedBtnDrawSluiceGate1();
	afx_msg void OnBnClickedBtnDrawSluiceGate2();
	
	afx_msg void OnBnClickedBtnDrawSluicegate1Line1();
	afx_msg void OnBnClickedBtnDrawSluicegate1Line2();
	afx_msg void OnBnClickedBtnDrawSluicegate2Line1();
	afx_msg void OnBnClickedBtnDrawSluicegate2Line2();
	void DrawOnVideo(CEdit& _edtSluiceGate, int* _piPointCount, int iDrawType=DrawType_perimeter);
	afx_msg void OnBnClickedBtnSluicegateSet();
	afx_msg void OnEnChangeEdtSluicegateCount();
	void JudgeVCASetState(BOOL _bIsVcaStatus);
	void JudgeVCAStatus();
private:
	CButton m_chkDisplayRule;
	CButton m_chkDisplayAlarmCount;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboAreaColor;
	CComboBox m_cboSluiceGateType;
	CEdit m_edtSluiceGateCount;
	CEdit m_edtSluiceGate1;
	CEdit m_edtSluiceGate2;
	CEdit m_edtSluiceGate1Line1;
	CEdit m_edtSluiceGate1Line2;
	CEdit m_edtSluiceGate2Line1;
	CEdit m_edtSluiceGate2Line2;
	CSpinButtonCtrl m_spinSluiceGateCount;	
	int		m_iPointCount[INNER_MAX_VCA_SLUICEGATE_COUNT];
	int		m_iPointCountLine[INNER_MAX_VCA_SLUICEGATE_LINE_COUNT];
	CButton m_chkSluiceGate1;
	CButton m_chkSluiceGate2;
	
};

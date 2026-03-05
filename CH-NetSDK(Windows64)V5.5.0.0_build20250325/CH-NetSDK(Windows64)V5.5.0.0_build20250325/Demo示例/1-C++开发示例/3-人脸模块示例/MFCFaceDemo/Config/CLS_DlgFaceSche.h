#pragma once

#include "CLS_PageBase.h"
#include "afxwin.h"
#include "afxdtctl.h"

class CLS_DlgFaceSchedule : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceSchedule)

public:
	CLS_DlgFaceSchedule(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFaceSchedule();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_SCHEDULE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	DECLARE_MESSAGE_MAP()

public:
	CComboBox m_cboAlgoType;
	CComboBox m_cboLibKey;
	CComboBox m_cboWeekday;
	CComboBox m_cboVcaType;
	CButton m_chkSchEnable;
	CButton m_chkSchTime[4];
	CDateTimeCtrl m_dtSchTimeBeg[4];
	CDateTimeCtrl m_dtSchTimeEnd[4];

	void UI_Init();

	void EnableWindowSchTime(int _iIndex);

public:
	afx_msg void OnBnClickedBtnLibkeyQuery();
	afx_msg void OnBnClickedBtnSet();
	afx_msg void OnBnClickedBtnGet();
	afx_msg void OnBnClickedChkTime1();
	afx_msg void OnBnClickedChkTime2();
	afx_msg void OnBnClickedChkTime3();
	afx_msg void OnBnClickedChkTime4();
	afx_msg void OnCbnSelchangeCboAlarmType();
};

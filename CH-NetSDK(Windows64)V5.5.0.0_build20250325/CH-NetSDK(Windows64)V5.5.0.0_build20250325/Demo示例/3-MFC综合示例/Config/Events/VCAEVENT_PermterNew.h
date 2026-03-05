#pragma once

#include "VCAEventBasePage.h"
#include "afxwin.h"

// CLS_VCAEVENT_PermterNew dialog

class CLS_VCAEVENT_PermterNew : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_PermterNew)

public:
	CLS_VCAEVENT_PermterNew(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_PermterNew();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_PERIMETER_NEW };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_CboNoAlarmMode;
	CButton m_ChkDisplayRule;
	CButton m_ChkDisplayState;
	CComboBox m_CboColor;
	CComboBox m_CboAlarmColor;
	CComboBox m_CboTargetType;
	CComboBox m_CboMode;
	CEdit m_EdtType;
	CEdit m_EdtDirection;
	CEdit m_EdtMinSize;
	CEdit m_EdtMaxSize;
	CEdit m_EdtPointNum;
	CButton m_ChkDisplayTarget;
	CEdit m_EdtPoint;
	afx_msg void OnBnClickedButtonDraw();
	afx_msg void OnBnClickedButtonPerimterNewSet();

	virtual BOOL OnInitDialog();
	void OnLanguageChanged();
	void UpdateUIText();
	void UpdateUIPara();
	CEdit m_EdtMinDis;
	CEdit m_EdtMinTime;
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	CButton m_ChkRuleValid;
};

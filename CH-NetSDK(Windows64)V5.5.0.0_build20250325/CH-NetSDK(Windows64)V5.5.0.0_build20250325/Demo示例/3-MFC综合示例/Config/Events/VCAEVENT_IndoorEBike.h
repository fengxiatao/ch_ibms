#pragma once
#include "afxwin.h"
#include "VCAEventBasePage.h"

// CLS_VCAEVENT_IndoorEBike dialog

class CLS_VCAEVENT_IndoorEBike :public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_IndoorEBike)

public:
	CLS_VCAEVENT_IndoorEBike(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VCAEVENT_IndoorEBike();

// Dialog Data
	enum { IDD = IDD_DLG_VCAEVENT_INDOOREBIKE };
	virtual void OnLanguageChanged();
	void UI_UpdateDialog();
	void UI_UpdatePage();

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	CComboBox m_cboRegion;
	afx_msg void OnBnClickedBtnDredgePointsDraw();
	afx_msg void OnBnClickedBtnEbikeSet();
	afx_msg void OnCbnSelchangeComboRegion();
	int m_iSensitivity;
	int m_iMinsize;
	int m_iMaxsize;
	int m_iAlarmTime;
	BOOL m_iDisplayRule;
	BOOL m_bDisplayTarget;
	BOOL m_bEventValid;
	VCAIndoorEBike m_tIndoorEBike;
	int m_iRegionNum;
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
};

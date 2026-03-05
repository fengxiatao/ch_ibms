#pragma once

#include ".\Config\Events\VCAEventBasePage.h"
#include "afxwin.h"
// VCAEVENT_TemDetect dialog

class VCAEVENT_TemDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(VCAEVENT_TemDetect)

public:
	VCAEVENT_TemDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~VCAEVENT_TemDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_TEMDETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CButton m_ChkValid;
	CButton m_ChkDisplayTemscaleEnable;
	CComboBox m_CboHighTemColor;
	CComboBox m_CboLowTemColor;
	CComboBox m_CboModelType;
	CComboBox m_CboTemUnit;
	CEdit m_EdtTemThreshold;
	CEdit m_EdtWaitTime;

	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	void OnLanguageChanged();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonTemdetectSet();
	CComboBox M_CboAbnormalAlarm;
	afx_msg void OnCbnSelchangeComboModelType();
};

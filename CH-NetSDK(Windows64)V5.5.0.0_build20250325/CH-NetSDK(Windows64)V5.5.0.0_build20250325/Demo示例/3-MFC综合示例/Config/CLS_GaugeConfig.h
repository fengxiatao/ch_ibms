#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"
#define POINT_GROUP_NUM			10
// CLS_GaugeConfig dialog

class CLS_GaugeConfig : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_GaugeConfig)

public:
	CLS_GaugeConfig(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_GaugeConfig();
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	void SetValue();

// dialog data
	enum { IDD = IDD_DIALOG_WATERGAUGE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonGaugeSet();
	afx_msg void OnBnClickedButtonGaugeGet();
	// scene number
	CComboBox m_cboSceneID;
	// Rule ID
	CComboBox m_cboRuleID;
	// Type of water gauge
	CComboBox m_cboGaugeType;
	// Number of calibration points
	CComboBox m_cboReferPointNum;
	// Backlight Compensation Enable
	CComboBox m_cboBlcEnable;
	// Number of preset positions for water gauge calibration
	CComboBox m_cboGaugeCalibNum;
	// Type of water gauge calibration
	CComboBox m_cboGaugeCalibType;
	// Water dipstick up-cut setting enable
	CComboBox m_cboUpSwitchEnable;
	// Water gauge undercut setting enable
	CComboBox m_cboDownSwitchEnable;

private:
	ReferPoint m_tPoint[POINT_GROUP_NUM];
};

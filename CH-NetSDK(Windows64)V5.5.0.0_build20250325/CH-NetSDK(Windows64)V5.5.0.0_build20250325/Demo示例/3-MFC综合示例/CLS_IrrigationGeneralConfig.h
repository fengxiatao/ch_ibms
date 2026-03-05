      #pragma once
#include "../BasePage.h"
#include "afxwin.h"


// CLS_GeneralConfiguration dialog

class CLS_IrrigationGeneralConfig : public CLS_BasePage 
{
	DECLARE_DYNAMIC(CLS_IrrigationGeneralConfig)

public:
	CLS_IrrigationGeneralConfig(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_IrrigationGeneralConfig();

// dialog data
	enum { IDD = IDD_DLG_CFG_GENERAL_CONFIGURATION };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSetIrrigationParaOverlyConf();
	afx_msg void OnCbnSelchangeComboIrrigationParaReportType();
	afx_msg void OnBnClickedButtonCommenableSet();
	virtual BOOL OnInitDialog();
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UpdateUI();
	void UpdateParameter();
	void UpdateUI_OverlyConfig();
	BOOL UpdateIrriDevStatus(int iType);
public:
	CComboBox m_cboIrrigationParaReportType;
	CButton m_chkOverlyLed;
	CButton m_chkOverlyVideo;
	CButton m_chkRainfallAlertEnbale;
	CButton m_chkWaterLevelAlert;
	CButton m_cbkOSDQuality;
	CButton m_chkHornEnable;
	CComboBox m_cboPortNo;
	
public:
	afx_msg void OnBnClickedCheckHornEnable();
	afx_msg void OnBnClickedButtonChannelSrcSet();
	afx_msg void OnBnClickedCheckIrrOsdQuality();
	
	afx_msg void OnCbnSelchangeComboType();
	afx_msg void OnBnClickedButtonWaterlevelsourceSet();
	CComboBox m_combo_waterlevelsource_type;
	CEdit m_edit_waterlevelsource_value;
	afx_msg void OnBnClickedButtonWaterlevelsourceIdSet();
	afx_msg void OnBnClickedButtonWaterSpeedSend();
	CComboBox m_cboSenceId;
};

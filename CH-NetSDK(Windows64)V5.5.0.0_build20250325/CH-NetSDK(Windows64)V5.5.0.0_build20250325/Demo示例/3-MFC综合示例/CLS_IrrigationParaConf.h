#include "../BasePage.h"
#include "afxwin.h"

// CLS_IrrigationParaConf dialog


class CLS_IrrigationParaConf : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_IrrigationParaConf)

public:
	CLS_IrrigationParaConf(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_IrrigationParaConf();

// dialog data
	enum { IDD = IDD_DLG_CFG_IRRIGATION_INFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSetRainfallPara();
	afx_msg void OnBnClickedButtonSetAlertWaterLevelPara();
	afx_msg void OnCbnSelchangeComboSenceID();
	afx_msg void OnCbnSelchangeComboRuleId();
	afx_msg void OnCbnSelchangeComboAlgoType();
	virtual BOOL OnInitDialog();
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	void UI_UpdatePara();
	void UI_UpdateAlgoType();
	BOOL CheckParaValid();
	BOOL CheckParaValidInWater();
	void SetAlertCountByAlgo(int _iAlgo);
private:
	CComboBox m_cboRainFallAlertThershold;
	CComboBox m_cboSenceID;
	CComboBox m_cboRuleID;
	CComboBox m_cboWaterLevelAlertDataSource;
	CButton m_btnIsMainChannel;
	CEdit m_edtLinkageTime;
	CEdit m_edtRainTimeInterval;
	CComboBox m_cboAlgoType;	
public:
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
	CButton m_chkRainEnable;
	CButton m_chkWaterLevelAlert;
	CEdit m_edtDefaultUploadTime;
	CEdit m_edtUploadCheckTime1;
	CEdit m_edtUploadCheckTime2;
	CEdit m_edtUploadCheckTime3;
	CEdit m_edtThreShold;
	CEdit m_edtTherShold2;
	CEdit m_edtTherShold3;
	CEdit m_edtAlertData;
	CEdit m_edtWaterLinkAge;
	CEdit m_edtWaterUploadTime;
	CEdit m_edtFirstUploadTime;
	CEdit m_edtSecondTime;
	CEdit m_edtThirdTime;
	CEdit m_edtWaterThershold1;
	CEdit m_edtWaterThershold2;
	CEdit m_edtWaterThershold3;
	afx_msg void OnBnClickedCheckRainfall();
	afx_msg void OnBnClickedCheckAlertwaterenable();
	afx_msg void OnStnClickedStaticAlgoType();
	CEdit m_edtLowWaterLevel;
	afx_msg void OnBnClickedButtonAlarmControl();
};

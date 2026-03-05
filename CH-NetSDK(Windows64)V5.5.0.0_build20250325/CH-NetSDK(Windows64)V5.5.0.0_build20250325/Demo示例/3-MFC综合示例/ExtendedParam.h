#pragma once
#include "BasePage.h"
#include "afxwin.h"

#define HOUR_LIMIT		23  //Upper limit of time hours
#define MINUTE_LIMIT	59	//Upper limit of time (minutes)


// CLS_ExtendedParam dialog

class CLS_ExtendedParam : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ExtendedParam)

public:
	CLS_ExtendedParam(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_ExtendedParam();

// dialog data
	enum { IDD = IDD_DLG_ITS_EXTENDED_PARAM1 };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButton1();
	BOOL OnInitDialog();
	void UI_UpdateDialog();
	void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	void OnLanguageChanged(int _iLanguage);
	void UI_UpdateBasicParamData();
	void UI_UpdateFillLightControlData();
	void UI_UpdateTrafficDayNightTimeData();
private:
	int m_iLogonID;
	int m_iChannel;
public:
	CStatic m_gpBasicParam;
	CStatic m_stcNightBitValue;
	CEdit m_edtNightBitValue;
	CButton m_btnNightBitValue;
	afx_msg void OnBnClickedGpFillLightControl();
	CStatic m_gpFillLightControl;
	CStatic m_stcDayThresholdValue;
	CStatic m_stcNightThresholdValue;
	CEdit m_edtDayThresholdValue;
	CEdit m_edtNightThresholdValue;
	CButton m_btnSetFillLightControl;
	CStatic m_gpTrafficDayNIghtTime;
	CStatic m_stcDayTime;
	CStatic m_stcNightTime;
	CEdit m_edtDayTimeHour;
	CEdit m_edtDayTimeMinute;
	CEdit m_edtNightTimeHour;
	CEdit m_edtNightTimeMiunte;
	CButton m_btnTrafficDayNightTime;
	afx_msg void OnBnClickedBtnSetTrafficDayNightTime();
	afx_msg void OnBnClickedBtnSetFillLightControl();
	afx_msg void OnBnClickedBtnSetBasicParam();
	afx_msg void OnBnClickedButtonPlateFilter();
	void UI_UpdatePlateFilterInfo();
	void UI_UpdateSingleCutPicFeature();
	CComboBox m_cboPlateFilterType;
	afx_msg void OnCbnSelchangeComboPlateFilterType();
	CComboBox m_cboSceneId;
	CComboBox m_cboCutEnable;
	CComboBox m_cboCutRange;
	CComboBox m_cboSourcePic;
	CComboBox m_cboOsdEnable;
	afx_msg void OnBnClickedButtonSingleSet();
	afx_msg void OnCbnSelchangeComboSingleSceneid();
};

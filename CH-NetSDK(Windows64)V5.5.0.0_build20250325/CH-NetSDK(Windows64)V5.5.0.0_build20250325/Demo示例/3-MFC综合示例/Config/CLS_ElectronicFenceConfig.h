#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"

// CLS_ElectronicFenceConfig dialog

class CLS_ElectronicFenceConfig : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ElectronicFenceConfig)

public:
	CLS_ElectronicFenceConfig(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_ElectronicFenceConfig();

	//
	virtual BOOL OnInitDialog();

	//LanguageChanged
	virtual void OnLanguageChanged(int _iLanguage);

	//ChannelChanged
	virtual void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo );

	//
	void UI_UpdateUIText();

// dialog data
	enum { IDD = IDD_DIALOG_ELECTRONIC_FENCE_CONFIG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	//ElectronicFence set
	afx_msg void OnBnClickedButtonSet();

	//ElectronicFence get
	afx_msg void OnBnClickedButtonGet();

	//Extend info set
	afx_msg void OnBnClickedButtonExtinfoSet();

	//Extend info get
	afx_msg void OnBnClickedButtonExtinfoGet();

	DECLARE_MESSAGE_MAP()
public:
	

	//IsEnbale ElectronicFence
	CComboBox m_comboIsEnbale;

	//ElectronicFence radius
	int m_iRadius;

	//Longitude
	CComboBox m_comboLongitude;

	//LongiitudeDegree
	int m_iLongiitudeDegree;

	//LongiitudeMinute
	int m_iLongiitudeMinute;

	//LongiitudeSecond
	int m_iLongiitudeSecond;

	//Latitude
	CComboBox m_comboLatitude;

	//LatitudeDegree
	int m_iLatitudeDegree;

	//LatitudeMinute
	int m_iLatitudeMinute;

	//LatitudeSecond
	int m_iLatitudeSecond;
	
	//Extend info
	CString m_cExtInfo;
};

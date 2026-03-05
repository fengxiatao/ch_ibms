#pragma once
#include "BasePage.h"
#include "afxwin.h"

// CLS_DlgThermography dialog

class CLS_DlgThermography : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgThermography)

public:
	CLS_DlgThermography(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgThermography();

// dialog data
	enum { IDD = IDD_DLG_THERMOGRAPHY };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support;

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

	void UpdateUIText();
	void UpdateParam();

	CComboBox m_CboChannelNo;
	CComboBox m_CboTemScaleType;
	CEdit m_EdtBodyTemCorrect;
	CEdit m_EdtIntellectCorrect;
	CButton m_ChkBodyTemCorrect;
	CButton m_CboIntellectCorrect;
	CButton m_ChkBDCorrect;
	CComboBox m_CboBDCorrectType;
	CComboBox m_CboBDNum;
	CEdit m_EdtBDTem1;
	CEdit m_EdtBDTem2;
	CComboBox m_CboBDUnit1;
	CComboBox m_CboBDTemUnit2;
	CEdit m_EdtBDDis1;
	CEdit m_EdtBDDis2;
	CEdit m_EdtBDPT1;
	CEdit m_EdtBDPT2;

	int m_iChannelNo;
	afx_msg void OnBnClickedButtonTiTemscaleTypeSet();
	afx_msg void OnBnClickedButtonBodytemCorrectSet();
	afx_msg void OnBnClickedButtonTiItcorrectSet();
	afx_msg void OnBnClickedButtonBdset();
	afx_msg void OnBnClickedCheckBodytemCorrect();
	afx_msg void OnBnClickedCheckItCorrect();
	afx_msg void OnBnClickedCheckCommonenableTemdetec();
	CButton m_ChkComEnableTemDetec;
	CButton m_chkBkDetectEnable;
	CSliderCtrl m_sldBkThreshold;
	CSliderCtrl m_sldBkSharkTime;
	CComboBox m_cboTemPosition;
	afx_msg void OnBnClickedButtonBkDetect();
	afx_msg void OnNMCustomdrawSliderBlackbodyDetectThreshold(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderSharkTime(NMHDR *pNMHDR, LRESULT *pResult);
	void UpdateUI_BkDetect();
	afx_msg void OnCbnSelchangeComboTempeturePosition();
};

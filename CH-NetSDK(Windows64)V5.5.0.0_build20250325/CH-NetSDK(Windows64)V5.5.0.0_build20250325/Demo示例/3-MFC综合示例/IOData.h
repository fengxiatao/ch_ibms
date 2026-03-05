#pragma once
#include "BasePage.h"
#include "afxwin.h"


// CLS_IOData dialog

class CLS_IOData : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_IOData)

public:
	CLS_IOData(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_IOData();
private:
	int m_iLogonId;
	int m_iChannelNo;
	CComboBox m_cboDevType;
	CComboBox m_cboIONo;
	CComboBox m_cboDefaultState;
	CComboBox m_cboWorkState;
// dialog data
	enum { IDD = IDD_DLG_ITS_IO_DATA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedBtnSet();
	afx_msg void OnCbnSelchangeCombo3();
	void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo);
	BOOL OnInitDialog();
	void OnLanguageChanged( int _iLanguage);
	void UI_UpdateDialog();
	afx_msg void OnBnClickedBtnSetio();
	CEdit m_edtDutyCycle;
	CEdit m_edtDuration;
	CEdit m_LeadTime;
	CEdit m_edtDouFre;
	afx_msg void OnCbnSelchangeCboIoNo();
	void UI_UpdateFillLight();
	CComboBox m_cboDNEnable;
	afx_msg void OnBnClickedBtnIoLightSet();
	CSliderCtrl m_sldLightOpen;
	CSliderCtrl m_sldLightClose;
	afx_msg void OnNMCustomdrawSliderIoLightOpen(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderIoLightClose(NMHDR *pNMHDR, LRESULT *pResult);
};

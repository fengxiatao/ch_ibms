#pragma once
#include "afxwin.h"
#include "VCAEventBasePage.h"
#include "afxdtctl.h"

#define TIME_SUM		4	//There are 4 time periods


// CLS_VCAEVENT_IllegalPark dialog

class CLS_VCAEVENT_IllegalPark : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_IllegalPark)

public:
	CLS_VCAEVENT_IllegalPark(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_IllegalPark();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_ILLEGAL_PARK };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void OnLanguageChanged();
	void UpdateUIText();
	void CleanText();
	void UpdatePageUI();
	void UpdataChangePresetAreaNum();
	
	afx_msg void OnCbnSelchangeCboIllparkPreset();
	afx_msg void OnCbnSelchangeCboIllparkAreaNum();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnIllparkSet();
	afx_msg void OnBnClickedBtnIllparkDraw();

private:
	CButton m_chkUse;
	CButton m_chkEventValid;
	CButton m_chkParkCapture;
	CComboBox m_cboPreset;
	CComboBox m_cboAreaNum;
	CComboBox m_cboSensitivity;
	CEdit m_edtAreaName;
	CEdit m_edtNoparkTime;
	CEdit m_edtParkJudgeTime;
	CEdit m_edtPoints;
	CEdit m_edtPointNum;
	CDateTimeCtrl m_dtpTimeStart[TIME_SUM];
	CDateTimeCtrl m_dtpTimeEnd[TIME_SUM];
	CButton m_chkParkWarningEnable;
	CEdit m_edtParkingWarningTime;
	CComboBox m_cboDetectionType;
	CEdit m_edtStallJudgeTime;
public:
	afx_msg void OnEnChangeEdtIllparkPointNum();
};

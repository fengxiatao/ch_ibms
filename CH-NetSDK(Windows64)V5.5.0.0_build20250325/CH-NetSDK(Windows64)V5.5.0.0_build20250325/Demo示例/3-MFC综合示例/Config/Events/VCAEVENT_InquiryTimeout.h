#pragma once

#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
// CLS_VCAEVENT_InquiryTimeout dialog

#define INQUIRY_TIMEOUT_MIN_SIZE 8
#define INQUIRY_TIMEOUT_MAX_SIZE 25
#define INQUIRY_TIMEOUT_SEN 50
#define INQUIRY_TIMEOUT_INQUIRYTIME 3600
#define INQUIRY_TIMEOUT_LEAVETIME 0
#define CHECK_AREA 0
#define NOVALID_AREA 1


class CLS_VCAEVENT_InquiryTimeout : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_InquiryTimeout)

public:
	CLS_VCAEVENT_InquiryTimeout(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VCAEVENT_InquiryTimeout();

// Dialog Data
	enum { IDD = IDD_DLG_VCAEVENT_INQUIRY_TIMEOUT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:	
	virtual BOOL OnInitDialog();
	void OnLanguageChanged();
	void UpdateUIText();
	void CleanText();
	void UpdatePageUI();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonCheckArea();
	afx_msg void OnBnClickedButtonNovalidArea();
	afx_msg void OnBnClickedButtonSet();
	afx_msg void OnCbnSelchangeComboCheckAreaNum();
	afx_msg void OnCbnSelchangeComboCheckArea();
	afx_msg void OnCbnSelchangeComboNovalidAreaNum();
	afx_msg void OnCbnSelchangeComboNovalidArea();
	afx_msg void OnBnClickedCheckTime1();
	afx_msg void OnBnClickedCheckTime2();
	afx_msg void OnBnClickedCheckTime3();
	afx_msg void OnBnClickedCheckTime4();

private:
	void CheckSchedtime(int _iIndex);
	void DrawOnVideo(CEdit& _edtSluiceGate, int* _piPointCount);

private:
	CButton m_chkEventValid;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmStat;
	CButton m_chkTargetBox;
	CEdit m_edtMinSize;
	CEdit m_edtMaxSize;
	CEdit m_edtSensitivity;
	CEdit m_edtInquryTime;
	CEdit m_edtAllowLeaveTime;
	CDateTimeCtrl m_dtcStartTime[4];
	CDateTimeCtrl m_dtcEndTime[4];
	CButton m_chkTime[4];

	CComboBox m_cboCheckAreaNum;
	CComboBox m_cboCheckArea;
	CEdit	m_edtCheckArea;
	CComboBox m_cboNovalidAreaNum;
	CComboBox m_cboNovalidArea;
	CEdit	m_edtNovalidArea;
	VcaInquiryTimeout m_vcaInquityTimeout;
};

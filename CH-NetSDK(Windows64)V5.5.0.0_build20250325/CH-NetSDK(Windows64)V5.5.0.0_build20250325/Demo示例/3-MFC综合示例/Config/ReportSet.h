#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "afxdtctl.h"
#include "Edit_OnlyNumber.h"
#include <vector>

class CLS_ReportSet : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ReportSet)

public:
	CLS_ReportSet(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_ReportSet();

// dialog data
	enum { IDD = IDD_DLG_CFG_REPORT_SET };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

//control variable
public:
	CDateTimeCtrl m_dtReportTime;
	CListCtrl m_lstReportTime;

	CComboBox m_cboSceneId;
	CEdit m_edtCapturePicSize;
	CEdit m_edtCaptureInterval;

	CEdit m_edtFtpAddr;
	CEdit m_edtFtpPort;
	CEdit_OnlyNumber m_edtFtpPath;
	CEdit m_edtFtpUser;
	CEdit m_edtFtpPswd;
	CEdit m_edtFtpUsage;

	CDateTimeCtrl m_dtCaptureBeg;
	CDateTimeCtrl m_dtCaptureEnd;

public:
	void UI_UpdateDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _ulLogonID, int _iWparam, void* _pvLParam, void* _pvUser);
	BOOL UI_UpdateCaptureInfo();
	BOOL UI_UpdateFtpInfo();
	BOOL UI_UpdataFtpReportTime();
	void UpdataTimeList();
	void SaveReportTimeInfo(int _iEnable);
	void UnloadReportTime();
public:
	int m_iLogonID;
	int m_iChannelNo;
	vector<ScenetimePoint> m_vecReptTime;
	int m_iSelItem;
public:
	virtual BOOL OnInitDialog();
	
	afx_msg void OnBnClickedBtnFtpSet();
	afx_msg void OnBnClickedBtnCaptureSet();
	afx_msg void OnBnClickedBtnReportAdd();
	afx_msg void OnBnClickedBtnReportModify();
	afx_msg void OnBnClickedBtnReportDelete();
	afx_msg void OnNMClickLstReportTime(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnStnClickedStcCaptureSceneid();
	afx_msg void OnCbnSelchangeCboCaptureSceneid();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonWldGauge();
};

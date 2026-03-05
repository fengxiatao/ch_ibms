#pragma once
#include "afxwin.h"
#include "../BasePage.h"


// CLS__Calibrate dialog

class CLS_Calibrate : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_Calibrate)

public:
	CLS_Calibrate(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_Calibrate();

// dialog data
	enum { IDD = IDD_DLG_ITS_CALIBRATE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonCalibrateDraw();
	afx_msg void OnBnClickedButton();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	void UpdatePageUI();

	int m_iLogonID;
	int m_iChannelNo;

	CEdit m_edtPoints;
};

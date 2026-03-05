#pragma once
#include "../BasePage.h"

// CLS_GBT28181SET dialog

class CLS_GBT28181Set : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_GBT28181Set)

public:
	CLS_GBT28181Set(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_GBT28181Set();

// Dialog Data
	enum { IDD = IDD_DIALOG_GBT28181SET };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonSave();
	void Init();
	bool OnCheckValue(int _iNum, int _iStart, int _iEnd, int _iTitle );
public:
	CEdit m_edtPort;
	CEdit m_edtServerID;
	CEdit m_edtDeviceID;
	CEdit m_edtAccount;
	CEdit m_edtPassword;
	CEdit m_edtRegValidity;
	CEdit m_edtKeepalive;
	CEdit m_edtHeartBeatInterval;
	CEdit m_edtHeartBeatTimes;
	CButton m_chkNeedReg;
	CButton m_chkEnable;
	CIPAddressCtrl m_edtIPAddress;
	
};

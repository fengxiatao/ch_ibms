#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_NetManage dialog

class CLS_NetManage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_NetManage)

public:
	CLS_NetManage(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_NetManage();

// dialog data
	enum { IDD = IDD_DLG_CFG_NET_TEST };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void  OnMainNotify(long _iWParam,void* _iLParam);
	void UpdateTestResult(void * _pParam);
	afx_msg void OnBnClickedButtonSet();
	void UpdateUIText();
	virtual BOOL OnInitDialog();
	CComboBox m_cboTestType;
	CEdit m_edtCardNum;
	CIPAddressCtrl m_IPAddr;
	void UpdateData();

public:
	CComboBox m_cboReachable;
	CComboBox m_cboIpIndex;
	CIPAddressCtrl m_IPAdress;
	afx_msg void OnBnClickedButtonIpSet();
	afx_msg void OnCbnSelchangeComboIndex();
	CEdit m_edtAblitity;
	afx_msg void OnBnClickedButtonEleantishake();
	CComboBox m_cboStatus;
	afx_msg void OnCbnSelchangeComboEleantishake();
};

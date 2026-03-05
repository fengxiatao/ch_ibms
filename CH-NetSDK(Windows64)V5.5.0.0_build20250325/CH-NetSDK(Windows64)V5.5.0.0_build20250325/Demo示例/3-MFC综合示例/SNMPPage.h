#pragma once
#include "BasePage.h"
#include "afxwin.h"

// CLS_SNMPPage dialog

class CLS_SNMPPage :  public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_SNMPPage)

public:
	CLS_SNMPPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_SNMPPage();

// Dialog Data
	enum { IDD = IDD_DIALOG_SNMPPARA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);

	DECLARE_MESSAGE_MAP()
public:	
	afx_msg void OnBnClickedButtonDefault();
	afx_msg void OnBnClickedButtonSet();
	void SetUIText();
	void UpdateParam();
	void GetParam(SnmpPara tPara);
public:
	int  m_iLogonID;
	int  m_iChannelNo;
	CButton m_CheckV1;
	CButton m_CheckV2c;
	CButton m_CheckV3;
	CComboBox m_Combox_ReSecurity;
	CComboBox m_Combox_WrSecurity;
	CComboBox m_Combox_Reaualg;
	CComboBox m_Combox_ReprigAlg;
	CComboBox m_Combox_WrPrigAlg;
	CComboBox m_Combox_WrAuAlg;
};

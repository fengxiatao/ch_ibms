#pragma once
#include "afxwin.h"
#include "BasePage.h"

#define MAX_ALMLOOPDETEC_PORT_NUM 128

// CLS_XVR dialog

class CLS_XVR : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_XVR)

public:
	CLS_XVR(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_XVR();
	afx_msg void OnBnClickedButtonLoopDetecSet();
	afx_msg void OnBnClickedButtonIdTypeSet();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UpdateUI();
	/*void UpdateParameter();*/
	void CLS_XVR::UpdatePortParameter();
	void CLS_XVR::UpdateProtocolConfigParameter();
	void CLS_XVR::UpdateDeviceName();

// dialog data
	enum { IDD = IDD_DIG_XVR };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
private:
	CButton m_ChkLoopDetecState;
	CComboBox m_CboLoopChannelNo;
	CComboBox m_CboLoopPortNo;
	CComboBox m_CboIDTypeChannelNo;
	CComboBox m_CboProtocolType;
	CButton m_ChkBasicState;
	CButton m_ChkDigestState;
	int m_iPortEnable[MAX_ALMLOOPDETEC_PORT_NUM];

public:
	afx_msg void OnCbnSelchangeComboLoopChannel();
	afx_msg void OnCbnSelchangeComboProtocolType();
	afx_msg void OnCbnSelchangeComboLoopPort();
private:
	CComboBox m_CboDeviceNameChannNo;
	CEdit m_EditDeviceName;
public:
	afx_msg void OnBnClickedButtonDevicenameSet();
	CComboBox m_cboalgoType;
	CButton m_chkWsse;
};

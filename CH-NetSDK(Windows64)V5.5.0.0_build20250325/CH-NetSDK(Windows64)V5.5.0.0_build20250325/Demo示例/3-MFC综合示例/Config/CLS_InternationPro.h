#pragma once
#include "BasePage.h"
#include "afxwin.h"

// CLS_InternationPro dialog

class CLS_InternationPro : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_InternationPro)

public:
	CLS_InternationPro(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_InternationPro();

// dialog data
	enum { IDD = IDD_DLG_CFG_INTERNATION_PRO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonRtmpSet();
	afx_msg void OnBnClickedButton8021xSet();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();

	CComboBox m_cboStreamType;
	CComboBox m_cboAddressType;
	CButton m_chkRtmpEnable;
	CEdit m_edtIpAdress;
	CEdit m_edtPort;
	CEdit m_edtRtmpUserName;
	CEdit m_edtRtmpPassword;
	CEdit m_edtKey;
	CComboBox m_cboLanNo;
	CComboBox m_cboConnectType;
	CComboBox m_cboEapType;
	CComboBox m_cboEapolType;
	CButton m_chk8021xEnable;
	CEdit m_edtUsername8201x;
	CEdit m_edtPassoword8021x;
	CComboBox m_cboConnectState;

private:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
public:
	afx_msg void OnCbnSelchangeComboAddressType();
	afx_msg void OnCbnSelchangeComboLanNum();
	CComboBox m_cboTimeMode;
	afx_msg void OnBnClickedButtonTimeModeSet();
	afx_msg void OnBnClickedButtonVideoCovv1Get();
	CEdit m_edtVideoCovV1Text;
};

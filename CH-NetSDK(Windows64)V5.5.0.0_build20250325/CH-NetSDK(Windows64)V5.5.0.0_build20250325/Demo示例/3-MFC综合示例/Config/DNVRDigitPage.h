#ifndef _DNVR_DIGIT_PAGE_H
#define _DNVR_DIGIT_PAGE_H

#include "../BasePage.h"
#include "afxwin.h"



#define SEEK_TYPE_STOP	0
#define SEEK_TYPE_START	1

#define SEEK_PARA_IP	0
#define SEEK_PARA_DDNS	1
#define SEEK_PARA_DSM	2
#define SEEK_PARA_IPV6	3

// CLS_DNVRDigitPage dialog

class CLS_DNVRDigitPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DNVRDigitPage)

public:
	CLS_DNVRDigitPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DNVRDigitPage();

	// Dialog Data
	enum { IDD = IDD_DLG_CFG_DNVR_DIGIT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void OnMainNotify(int _iLogonID, int _iWparam, void* _pvLParam, void* _pvUser);
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
private:
	void UI_UpdateDialog();
	BOOL UI_UpdateDigit();
	BOOL UI_UpdateIPCPnp();

private:
	CButton m_chkDigitEnable;
	CComboBox m_cboConnectMode;
	CEdit m_edtIPIDDDNS;
	CEdit m_edtProxyIP;
	CComboBox m_cboDeviceChan;
	CEdit m_edtDevicePort;
	CComboBox m_cboStreamType;
	CComboBox m_cboNetMode;
	CComboBox m_cboPTZProtocol;
	CEdit m_edtPTZAddr;
	CEdit m_edtUserName;
	CEdit m_edtPassword;
	CEdit m_edtEncryptKey;
	CComboBox m_cboServerType;
	CComboBox m_cboIPCPnP;
	CButton m_btnDigit;
	CEdit m_edtComSend;
	CButton m_btnComSend;
	int m_iLogonID;
	int m_iChannelNo;
	HWND s_hWnd;
	CListCtrl m_lstDeviceCtrl;
	
public:
	afx_msg void OnCbnSelchangeComboIpcpnp();
	afx_msg void OnBnClickedButtonDigit();
	afx_msg void OnBnClickedButtonComsend();
	afx_msg void OnStnClickedStaticIpidddns();
	afx_msg void OnBnClickedButtonSearchDevice();
	afx_msg LRESULT OnSearchMsg(WPARAM wParam, LPARAM lParam);
	afx_msg void OnBnClickedButtonStopSearch();
	afx_msg void OnNMClickListDeviceInfo(NMHDR *pNMHDR, LRESULT *pResult);
private:
	CEdit m_edtIpcMac;
public:
	afx_msg void OnBnClickedButtonChange();
	afx_msg void OnCbnSelchangeComboConnectmode();
	CEdit m_Edit_IpDdnsDsmIpv6;
	CEdit m_Edit_ProxyIpv6;
	CComboBox m_combo_activation;
	CComboBox m_combo_synchro;
	afx_msg void OnStnClickedStaticIpidddnsipv6Text();
	afx_msg void OnCbnSelchangeComboNetmode();
	CEdit m_edit_mcuip;
	CEdit m_edit_mcuport;
	bool m_IsNvssIpv6;
	afx_msg void OnBnClickedNvssIpv6();
	afx_msg void OnBnClickedNvssIpv4();
};
#endif
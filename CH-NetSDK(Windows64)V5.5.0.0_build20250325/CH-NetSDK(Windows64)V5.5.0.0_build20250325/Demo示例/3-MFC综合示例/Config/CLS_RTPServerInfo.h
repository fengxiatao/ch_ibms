#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"


// CLS_RTPServerInfo dialog

class CLS_RTPServerInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_RTPServerInfo)

public:
	CLS_RTPServerInfo(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_RTPServerInfo();

// dialog data
	enum { IDD = IDD_DLG_CFG_RTPSERVERINFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	void ChangEnableState();

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSetRtpserver();
	CComboBox m_cboStreamType;
	CButton m_chEnable;
	CEdit m_editVideoAddr;
	CEdit m_editVideoPort;
	CComboBox m_cboVideoTTL;
	CEdit m_editAudioAddr;
	CEdit m_editAudioPort;
	CComboBox m_cboAduioTTL;
	CEdit m_editMetaDataAddr;
	CEdit m_editMetaDataPort;
	CComboBox m_cboMetaDataTTL;
	CComboBox m_cboMultiType;
	afx_msg void OnBnClickedButtonGetRtpserver();
	CButton m_btSet;
	CButton m_btGet;
	afx_msg void OnCbnSelchangeComboMulticastType();
};

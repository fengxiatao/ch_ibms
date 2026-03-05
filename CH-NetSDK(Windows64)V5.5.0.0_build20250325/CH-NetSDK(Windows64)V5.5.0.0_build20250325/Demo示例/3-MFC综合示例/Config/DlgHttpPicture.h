#pragma once
#include "../BasePage.h"
#include "afxwin.h"

// CLS_HttpPicture dialog

class CLS_HttpPicture : public CLS_BasePage 
{
	DECLARE_DYNAMIC(CLS_HttpPicture)

public:
	CLS_HttpPicture(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_HttpPicture();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_HTTP_PICTURE };

	void InitPageUI();
	void GetHttpPicStreamData();
	HttpPicStreamParam m_tHttpPicStreamParam;
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSet();
	virtual BOOL OnInitDialog();

	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
	afx_msg void OnBnClickedButtonSettest();
	afx_msg void OnBnClickedButtonSetServer();
	afx_msg void OnCbnSelchangeComboHttppicServer();
	afx_msg void OnEnChangeEditHttppicServernum();
	int m_iServerNum;
	CComboBox m_cboServer;
	CString m_csIP;
	CString m_csURL;
	CString m_csUserName;
	CString m_csPsw;
	int m_iPort;

};

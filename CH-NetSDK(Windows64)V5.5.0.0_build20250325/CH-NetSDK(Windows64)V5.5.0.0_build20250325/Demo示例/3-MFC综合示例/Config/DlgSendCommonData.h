#pragma once

#include "BasePage.h"
#include "afxcmn.h"
// CLS_DlgSendCommonData dialog

class CLS_DlgSendCommonData : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgSendCommonData)

public:
	CLS_DlgSendCommonData(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgSendCommonData();

// Dialog Data
	enum { IDD = IDD_DIALOG_SEND_COMMONDATA };
	void UI_UpdateDialogText();

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	int m_iLogonID;
	int m_iChannelNo;
public:
	afx_msg void OnBnClickedButtonSend();
	CString m_csData;
};

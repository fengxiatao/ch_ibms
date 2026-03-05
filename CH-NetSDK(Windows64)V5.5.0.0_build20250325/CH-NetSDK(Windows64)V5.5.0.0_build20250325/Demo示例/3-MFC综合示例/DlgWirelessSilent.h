#pragma once
#include "../BasePage.h"
#include "afxwin.h"

// CDlgWirelessSilent dialog

class CDlgWirelessSilent : public CLS_BasePage 
{
	DECLARE_DYNAMIC(CDlgWirelessSilent)

public:
	CDlgWirelessSilent(CWnd* pParent = NULL);   // standard constructor
	virtual ~CDlgWirelessSilent();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_WIRELESS_SILENT };

	void InitPageUI();

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSet();
	virtual BOOL OnInitDialog();
	UINT m_uiSilentTime;

	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
};

#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

#define NO_CHANNEL			0		//Indicates that the number of channels is 0
// CLS_AdvProtDetectPage dialog

class CLS_AdvProtDetectPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_AdvProtDetectPage)

public:
	CLS_AdvProtDetectPage(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_AdvProtDetectPage();

	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);

// dialog data
	enum { IDD = IDD_DLG_CFG_ADVANCE_PROT_DETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	void OnLanguageChanged(int _iLanguage);

	DECLARE_MESSAGE_MAP()

private:
	void UI_UpdateText();
	void ShowListItem();
	void GetKernelVersion();
	void GetModelAndType();
public:
	CListCtrl m_listLogonSubmitProtInfo;
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
};

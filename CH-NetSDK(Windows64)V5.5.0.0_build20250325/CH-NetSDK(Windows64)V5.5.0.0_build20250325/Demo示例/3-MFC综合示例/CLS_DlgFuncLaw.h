#ifndef _FUNC_LAW_PAGE_H
#define _FUNC_LAW_PAGE_H


#include "BasePage.h"


class CLS_DlgFuncLaw : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgFuncLaw)

public:
	CLS_DlgFuncLaw(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgFuncLaw();

// dialog data
	enum { IDD = IDD_DLG_CFG_FUNC_LAW };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()


public:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void GetFuncLaw();
	void InitDialogItemText();
};

#endif
#pragma once

#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_PlateLibManage

class CLS_PlateLibManage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_PlateLibManage)

public:
	CLS_PlateLibManage(CWnd* pParent = NULL);
	virtual ~CLS_PlateLibManage();

	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();

	void GetPlateLibParas();

	enum { IDD = IDD_DIALOG_CFG_XML_PLATELIB };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonPlatelibAdd();	// Add new license plate library
	afx_msg void OnBnClickedButtonPlatelibSet();	// change / set license plate library
	afx_msg void OnBnClickedButtonPlatelibDelete(); // delete license plate library
	CListCtrl m_listPlateLib;
	afx_msg void OnLvnItemchangedListPlatelibrary(NMHDR *pNMHDR, LRESULT *pResult);	// the current line changes
};

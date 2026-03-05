
#pragma once
#include "../BasePage.h"
#include "SerialInfo.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_SerialManagePage dialog

class CLS_SerialManagePage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_SerialManagePage)

public:
	CLS_SerialManagePage(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_SerialManagePage();

// dialog data
	enum { IDD = IDD_DLG_CFG_COM_PAGE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo);
	DECLARE_MESSAGE_MAP()
private:
	int m_iLogonID;
	CLS_SerialInfo *m_pclsSerialInfo;
public:
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	CListCtrl m_lstComMng;
	BOOL UI_Update();
	void UpdateComInfo(int _iComNo);//Update serial port list information
	afx_msg void OnNMClickLstComMng(NMHDR *pNMHDR, LRESULT *pResult);
};

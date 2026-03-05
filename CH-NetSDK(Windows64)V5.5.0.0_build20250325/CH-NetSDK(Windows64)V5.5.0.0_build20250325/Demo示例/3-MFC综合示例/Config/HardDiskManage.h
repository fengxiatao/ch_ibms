#pragma once

#include "BasePage.h"
#include "afxcmn.h"
// CLS_HardDiskManage dialog

class CLS_HardDiskManage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_HardDiskManage)

public:
	CLS_HardDiskManage(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_HardDiskManage();

// dialog data
	enum { IDD = IDD_DLG_CFG_HARDDISK_MANAGE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _ulLogonID, int _iWparam, void* _pvLParam, void* _pvUser);
	void UI_UpdateHardDiskInfo();
private:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	CListCtrl m_lstHardDisk;
	CListCtrl m_ctrListDiskInfoEx;
public:
	afx_msg void OnBnClickedButtonCleardisk();
	afx_msg void OnBnClickedButtonDiskinfoQuery();
};

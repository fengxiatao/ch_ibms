
#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "LinkDevSetEdit.h"
#include "afxcmn.h"
#include <vector>
#include <map>
// CLS_LinkSet dialog

class CLS_LinkSet : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_LinkSet)

public:
	CLS_LinkSet(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_LinkSet();

// dialog data
	enum { IDD = IDD_DLG_CFG_LINK_SET };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	int m_iLogonID;
	int m_iDevType;
	int m_iDevNo;
	int m_iFreeLinkIndex;
	bool blGetFreeLink;
	CListCtrl m_lstLink;
	CComboBox m_cboLinkType;
	map<int,CString> m_mapAlarmTypeName;
	map<int,CString> m_mapLinkTypeName;
public:
	virtual BOOL OnInitDialog();
	void InitDlgItems();
	void InitMapInfo();
	void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo);
	BOOL UpdateUI();
	BOOL UpdateInputList();	
	afx_msg void OnCbnSelchangeCboNum();
	void UpdateLinkInfo(int _iID,int _iLinkNo);
	void UpdateLinkItem(int _iLinkNo, AlarmInLink _alarminLink);
	afx_msg void OnBnClickedBtnLinkAdd();
	afx_msg void OnNMClickLstLinkset(NMHDR *pNMHDR, LRESULT *pResult);
	virtual void OnLanguageChanged(int _iLanguage);
};

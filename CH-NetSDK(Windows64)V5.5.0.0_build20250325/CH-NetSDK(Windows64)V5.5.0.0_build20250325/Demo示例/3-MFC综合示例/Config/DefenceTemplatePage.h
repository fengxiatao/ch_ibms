#pragma once
#include "afxcmn.h"
#include "../BasePage.h"
#include "DefenceTemplateAdd.h"

// CLS_DefenceTemplatePage dialog

class CLS_DefenceTemplatePage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DefenceTemplatePage)

public:
	CLS_DefenceTemplatePage(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DefenceTemplatePage();

// dialog data
	enum { IDD = IDD_DLG_CFG_DONGHUAN_SCHEDULE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
private:
	int m_iLogonID;
	CListCtrl m_lstDefenceTemplate;
	CLS_DefenceTemplateAdd *m_pclsTempAdd;
public:
	virtual BOOL OnInitDialog();
	void InitWidget();
	void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo);
	BOOL UpdateUI();
	BOOL UpdateSchedule(int _iID); 
	afx_msg void OnBnClickedBtnDhSchedule();
	int GetFreeSchedule();
	
	afx_msg void OnNMClickLstDhSchedule(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg LRESULT	OnParamChangeMsg(WPARAM wParam, LPARAM lParam);
	virtual void OnLanguageChanged(int _iLanguage);
};

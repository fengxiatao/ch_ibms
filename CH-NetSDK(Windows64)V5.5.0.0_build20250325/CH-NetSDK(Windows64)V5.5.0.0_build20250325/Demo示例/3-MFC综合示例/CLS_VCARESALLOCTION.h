#pragma once
#include "../BasePage.h"
#include "afxwin.h"

// CLS_VCARESALLOCTION dialog

class CLS_VCARESALLOCTION : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VCARESALLOCTION)

public:
	CLS_VCARESALLOCTION(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VCARESALLOCTION();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual BOOL OnInitDialog();
// Dialog Data
	enum { IDD = IDD_DLG_VCA_RESOURCE_ALLOCTION };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	int m_iLogonID;
	int m_iChannelNO;
	afx_msg void OnBnClickedButton1();
	void AddFuncToCombox(int iResult);
	int m_chkVcaMonitor;
	CComboBox m_comResourceID;

    CComboBox m_comAlgorithmType;
    CComboBox m_comDayTemplate;
    CComboBox m_comNightTemplate;
    afx_msg void OnCbnSelchangeComboAlgorithmType();
    void initComboBoxTemplate(CComboBox& combo);
};

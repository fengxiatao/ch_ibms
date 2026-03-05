#pragma once
#include ".\Config\Events\VCAEventBasePage.h"

// CLS_VCAScanArea dialog

class CLS_VCAScanArea : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VCAScanArea)

public:
	CLS_VCAScanArea(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAScanArea();

// dialog data
	enum { IDD = IDD_DLG_VCA_SCAN_AREA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboSceneID;
	CComboBox m_cboOrderType;
	CComboBox m_cboCmdType;
	CComboBox m_cboScanMode;
	void UpdateUIText();
	void UpdatePageUI();
	void OnCbnSelchangeComboSceneId();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonScanAreaSet();
	afx_msg void OnBnClickedButtonScanParaSet();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	int		m_iLogonID;
	int		m_iChannelNo;
	int		m_iStreamNo;
};

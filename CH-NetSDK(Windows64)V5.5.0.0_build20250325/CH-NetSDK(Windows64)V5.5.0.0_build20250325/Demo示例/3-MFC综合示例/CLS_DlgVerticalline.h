#pragma once

#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_DlgVerticalline dialog

class CLS_DlgVerticalline : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgVerticalline)

public:
	CLS_DlgVerticalline(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgVerticalline();

// Dialog Data
	enum { IDD = IDD_DIALOG_WATERFLOWINFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	afx_msg void OnBnClickedButtonSet();
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);

	CComboBox m_cboSceneID;
	CComboBox m_cboOperateType;
	CEdit m_edtVerticalNo;
	CEdit m_edtStartDistance;
	CEdit m_edtBottomDistance;
	CEdit m_edtCount;
	CEdit m_edtWaterFlow[20];
	CEdit m_edtWaterLevel[20];
};

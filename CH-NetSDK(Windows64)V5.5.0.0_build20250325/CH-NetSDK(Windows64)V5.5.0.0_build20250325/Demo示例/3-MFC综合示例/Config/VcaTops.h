#pragma once
#include "BasePage.h"
#include "afxwin.h"


// CLS_VcaTops dialog

class CLS_VcaTops : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VcaTops)

public:
	CLS_VcaTops(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaTops();

// dialog data
	enum { IDD = IDD_DLG_VCA_TOPS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonFlash();
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonTopsSet();
	void UpdateUIText();
	void UpdatePageUI();
	CComboBox m_cboVcaTyep;
	CComboBox m_cboSenceId;
	CComboBox m_cboDevType;
	CButton m_chkAbmormal;
	CButton m_chkStranded;
	CButton m_chkAlone;
	CButton m_chkDeliveryGoods;
	CButton m_chkLinger;
	CButton m_chkGoodsLeft;
	CButton m_chkGoodsLose;
	CButton m_chkThermalChert;
};

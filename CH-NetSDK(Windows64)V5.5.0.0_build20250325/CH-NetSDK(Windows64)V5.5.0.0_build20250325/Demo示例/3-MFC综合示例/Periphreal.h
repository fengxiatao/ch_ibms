#pragma once
#include "BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CPeriphreal dialog

class CPeriphreal : public CLS_BasePage
{
	DECLARE_DYNAMIC(CPeriphreal)

public:
	CPeriphreal(CWnd* pParent = NULL);   // standard constructor
	virtual ~CPeriphreal();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_PERIPHREAL };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iRow;
	int m_iCol;
	CComboBox m_cboComNo;
	CEdit m_PeriNum;
	CComboBox m_cboSupportType;
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo );
	virtual void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnLanguageChanged(int _iLanguage);
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	void UI_UpdateData();
	void UpdatePeriPhrealPara();
	void UpdatePeriPhrealInfo();
	void UI_InitDialog();
	void GetTypeNameByIndex(int _iIndex);
	void GetStaticInfo();
	afx_msg void OnCbnSelchangeComboComno();
	afx_msg void OnBnClickedButtonSettype();
	afx_msg void OnBnClickedButtonSet2();
	afx_msg void OnNMDblclkListShowaddr(NMHDR *pNMHDR, LRESULT *pResult);	
	CEdit m_edtShow;
	afx_msg void OnEnKillfocusEdtShow();
	CEdit m_edt_Data1;
	CEdit m_edt_Data2;
	
	afx_msg void OnCbnSelchangeComboSupporttype();
	afx_msg void OnCbnSelchangeComboAddr();
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
	CButton m_chkDeviceEnable;
};

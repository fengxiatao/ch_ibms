#pragma once
#include "BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


// CLS_DlgFDDFunction dialog

class CLS_DlgFDDFunction : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgFDDFunction)

public:
	CLS_DlgFDDFunction(CWnd* pParent = NULL);   // standard constructor
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo );
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	virtual ~CLS_DlgFDDFunction();

// Dialog Data
	enum { IDD = IDD_DIALOG_FDD };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonFddenable();
	afx_msg void OnBnClickedButtonTddset();
	afx_msg void OnCbnSelchangeComboType();
	void UpdateUI();
	void UpdateParam();
	void UpdateIMSIInfo();
	void UpdateComboxInfo(CComboBox *pComBox, int iData);

	int m_iLogonID;
	int m_iChannelNo;
	CComboBox m_cboFddRfEnable;
	CComboBox m_cboTddRf;
	CComboBox m_cboType;
	CComboBox m_cboRfStatus;
	CButton m_chkHeartBeat;
	CComboBox m_cboMobile;
	CComboBox m_cboInType;
	CComboBox m_cboBaseStation;
	CComboBox m_cboLibQuery;
	CComboBox m_cboQueryTime;
	CComboBox m_cboWarning;
};

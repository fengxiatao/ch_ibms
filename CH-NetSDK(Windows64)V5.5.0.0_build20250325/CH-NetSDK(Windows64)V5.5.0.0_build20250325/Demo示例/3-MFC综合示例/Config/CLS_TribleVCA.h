#pragma once
#include "afxwin.h"
#include "BasePage.h"


// CLS_TribleVCA dialog

class CLS_TribleVCA : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_TribleVCA)

public:
	CLS_TribleVCA(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_TribleVCA();

// dialog data
	enum { IDD = IDD_DLG_TRIBLE_VCA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedCheckSmdEncode();
	afx_msg void OnBnClickedCheckSmdImage();
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UpdateUI();
	void GetAbilitity();
	void UpdateParameter();
	virtual BOOL OnInitDialog();
	CButton m_chkSmdEncode;
	CButton m_chkSmdArea;
	afx_msg void OnBnClickedCheckSmdScene();
};

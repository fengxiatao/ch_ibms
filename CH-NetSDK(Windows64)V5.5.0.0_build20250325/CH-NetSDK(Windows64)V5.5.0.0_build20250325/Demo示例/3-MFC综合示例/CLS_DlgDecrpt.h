#pragma once


// CLS_DlgDecrpt dialog

class CLS_DlgDecrpt : public CDialog
{
	DECLARE_DYNAMIC(CLS_DlgDecrpt)

public:
	CLS_DlgDecrpt(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgDecrpt();
	int	m_iLogonID;
	int	m_iChannelNO;
	int m_iStreamNO;
// Dialog Data
	enum { IDD = IDD_DIALOG_DECRYPT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CString m_csPsw;
	afx_msg void OnBnClickedButtonDecrypt();
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
};

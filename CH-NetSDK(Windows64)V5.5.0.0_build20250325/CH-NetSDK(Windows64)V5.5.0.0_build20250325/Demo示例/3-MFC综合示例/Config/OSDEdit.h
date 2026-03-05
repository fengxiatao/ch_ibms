#pragma once
#include "afxwin.h"

// CLS_OSDEdit dialog

class CLS_OSDEdit : public CDialog
{
	DECLARE_DYNAMIC(CLS_OSDEdit)

public:
	CLS_OSDEdit(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_OSDEdit();

// dialog data
	enum { IDD = IDD_DLG_CFG_OSD_EDIT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	int m_iLogonID;
	int m_iDlgType;		//0 add interface, 1 edit interface
	int m_iDevNo;			//device number, channel number
	int m_iOSDNo;		//character overlay number
	inline void InitOSDParam(int _iDlgFlag, int _iLogonID, int _iDevID, int _iOSDNo)
	{
		m_iLogonID = _iLogonID;
		m_iDlgType = _iDlgFlag;
		m_iDevNo = _iDevID;
		m_iOSDNo = _iOSDNo;
	}
	CEdit m_edtDevName;
	CEdit m_edtDevIP;
	CComboBox m_cboDevCH;
	CComboBox m_cboOSDArea;
	CComboBox m_cboOSDColor;
	virtual BOOL OnInitDialog();
	void InitOSDInfo();
	void UpdateUI();
	CString GetColorStr(int _iColor);
	afx_msg void OnBnClickedBtnOsdSet();
};

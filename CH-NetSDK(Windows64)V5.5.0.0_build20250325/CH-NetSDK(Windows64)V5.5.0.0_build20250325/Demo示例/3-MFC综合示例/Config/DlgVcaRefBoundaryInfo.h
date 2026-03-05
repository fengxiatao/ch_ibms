#pragma once
#include "../BasePage.h"
#include "shlwapi.h"
#include "afxwin.h"
#include "afxcmn.h"

#define COORDINATE_MIN		0
#define COORDINATE_MAX		20000000	//The value range of xyz coordinates

// DlgVcaRefBoundaryInfo dialog

class DlgVcaRefBoundaryInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(DlgVcaRefBoundaryInfo)

public:
	DlgVcaRefBoundaryInfo(CWnd* pParent = NULL);   // Standard constructor
	virtual ~DlgVcaRefBoundaryInfo();

	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);

// dialog data
	enum { IDD = IDD_DLG_CFG_VCA_REFBOUNDARYINFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	void OnLanguageChanged(int _iLanguage);
	void UI_UpdateText();

	CString ProtToRealCoor(int _iCoor);
	void UpdateList(int _iRowLines);
	void GetVcaRefBoundaryInfo();
	RefBoundaryInfo m_tRefBoundaryArr;
public:
	CListCtrl m_listRefBoundary;
	afx_msg void OnBnClickedCheckEnable();
	afx_msg void OnBnClickedButtonSave();
	BOOL m_blEnable;
	int m_iPointNum;
	CComboBox m_cboPointNum;
	afx_msg void OnCbnSelchangeComboPointNum();
	afx_msg void OnLvnItemchangedListRefBoundaryInfo(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedButtonSaveLine();
};

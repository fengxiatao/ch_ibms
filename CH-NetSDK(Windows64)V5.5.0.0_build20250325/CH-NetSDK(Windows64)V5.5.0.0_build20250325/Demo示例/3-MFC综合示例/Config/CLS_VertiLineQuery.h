#pragma once
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_VertiLineQuery dialog

class CLS_VertiLineQuery : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VertiLineQuery)

public:
	CLS_VertiLineQuery(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VertiLineQuery();
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();

	CString IntToFloatStr(int _iNum);
	// dialog data
	enum { IDD = IDD_DIALOG_VERTICAL_LINE_QUERY };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CListCtrl m_listCatalogue;
	CListCtrl m_listDetail;
	afx_msg void OnBnClickedButtonQuery();
	CComboBox m_cboSceneID;
	int m_iCurPage;
	int m_iCoefNumArr[MAX_COFE_NUM];
	CComboBox m_cboNumInPage;
	CComboBox m_cboStartNum;
	CofeInfo m_tResult[MAX_QUERY_PAGE_COUNT][MAX_COFE_NUM];
//	afx_msg void OnHdnItemclickListCatalog(NMHDR *pNMHDR, LRESULT *pResult);
//	afx_msg void OnHdnItemclickListCatalog(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMClickListCatalog(NMHDR *pNMHDR, LRESULT *pResult);
};

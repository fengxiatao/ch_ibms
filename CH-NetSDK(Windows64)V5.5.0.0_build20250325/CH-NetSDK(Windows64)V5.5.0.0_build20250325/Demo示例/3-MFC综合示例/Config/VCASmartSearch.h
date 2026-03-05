#pragma once

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "afxdtctl.h"

#define SUPPORT_MAX_CHANNEL			32

enum EStatementIndex	//columns of the report
{
	E_STATEMENT_FIRST = 0,	//first row
	E_STATEMENT_SECOND,
	E_STATEMENT_THIRD,		
	E_STATEMENT_FOUTH,
	E_STATEMENT_SUM
};

class CLS_VCASmartSearch : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VCASmartSearch)

public:
	CLS_VCASmartSearch(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCASmartSearch();

// dialog data
	enum { IDD = IDD_DLG_CFG_VCA_SMART_SEARCH };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedBtnVcaSearchExport();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void OnLanguageChanged( int _iLanguage );
	void WriteResult(ReportFormResult _ReportResult);
	CString FormatTime(int _iType, NVS_FILE_TIME _Time);
	virtual void OnMainNotify(int _ulLogonID, int _iWparam, void* _iLParam, void* _iUser);
	void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo );
private:
	int	m_iLogonID;
	int	m_iChannelNo;
	int m_iStreamNo;
	CButton m_chkChannel[SUPPORT_MAX_CHANNEL];
    CButton m_chkChannelListEnable;
	CListCtrl m_lstctrlStatement;
	CComboBox m_cboStatementType;
	CDateTimeCtrl m_dtpStatTime;
public:
	CButton m_chkQureyArea;
	afx_msg void OnBnClickedCheckQureyarea();
};

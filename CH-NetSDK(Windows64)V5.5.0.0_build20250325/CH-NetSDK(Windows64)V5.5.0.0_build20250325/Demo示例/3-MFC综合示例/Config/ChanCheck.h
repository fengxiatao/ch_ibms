#ifndef _CHAN_CHECK_PAGE_H_
#define _CHAN_CHECK_PAGE_H_
#include "afxwin.h"
#include "../BasePage.h"

#define MAX_CHAN_PAGE	64
#define MAX_PAGE_NUM	3

class CLS_ChanCheck : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ChanCheck)

public:
	CLS_ChanCheck(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_ChanCheck();

// dialog data
	enum { IDD = IDD_DLG_CFG_CHANNEL_CHECK };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
//control variable
public:
	CStatic m_stcChanNo[MAX_CHAN_PAGE];
	CButton m_chkChan[MAX_CHAN_PAGE];
	CButton m_chkSelALl;
	CComboBox m_cboPage;

	int m_iCurrentPage;
	int m_iChanNum;
	int	m_iEnable[MAX_BITSET_COUNT];
public:
	void InitData(int _iChanNum, int* _iEnable);
	void OnCheckAll(BOOL _blChk);
	void OnShowPage(int _iPage);//0 starts for the first page
	void OnChanCheck();
	void GetChanValue(int * _iEnable);
public:
	virtual BOOL OnInitDialog();
	void OnLanguageChanged(int _iLanguage);
	afx_msg void OnBnClickedChkSelectAll();
	afx_msg void OnCbnSelchangeCboChPage();
	afx_msg void OnBnClickedBtnFtontPage();
	afx_msg void OnBnClickedBtnNextPage();
	afx_msg void OnBnClickedChkChan(UINT _uiID);
};

#endif

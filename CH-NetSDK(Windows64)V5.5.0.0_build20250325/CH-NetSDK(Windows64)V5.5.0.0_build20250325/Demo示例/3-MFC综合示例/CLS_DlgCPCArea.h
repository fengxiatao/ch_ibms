#pragma once
#include "afxcmn.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "afxwin.h"
#include ".\Config\ChanCheck.h"
// CLS_DlgCPCArea dialog

class CLS_DlgCPCArea : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_DlgCPCArea)

public:
	CLS_DlgCPCArea(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgCPCArea();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_VCA_CPC_AREA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboDevType;
	int m_iCurAreaNo;
	CComboBox m_cboShowType;
	CLS_ChanCheck* m_pclsChanCheck;
	afx_msg void OnBnClickedButtonAreaDisplay();
	virtual BOOL OnInitDialog();
private:
	void UpdateUIText();
	void UpdatePageUI();
	void UpdateAreaDisplay();
	void UpdateAreaConfig();
	void UI_UpdateChanCheck();

	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
public:
	CComboBox m_cboAreaNo;
	int m_iEnable;
	CString m_csAreaName;
	int m_iCurPeople;
	int m_iMaxPeople;
	CComboBox m_cboClearMode;
	int m_iHour;
	int m_iMinutes;
	afx_msg void OnBnClickedButtonAreaConfig();
	afx_msg void OnBnClickedButtonClearCpcarea();
	CComboBox m_cboAreaNoClear;
	afx_msg void OnEnChangeEditAreaMaxpeople3();
	CComboBox m_cboQueryAreaNo;
	CString m_csQueryAreaName;
	int m_iQueryCurPeopleNum;
	int m_iQueryMaxPeople;
	CString m_csQueryStartTime;
	afx_msg void OnBnClickedButtonAreaQuery();
};

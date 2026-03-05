#pragma once
#include "../BasePage.h"
#include "afxcmn.h"
#include "Common/NeuListCtrl.h"

// CLS_ITSParkCarNum dialog

class CLS_ITSParkCarNum : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ITSParkCarNum)

public:
	CLS_ITSParkCarNum(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_ITSParkCarNum();

// dialog data
	enum { IDD = IDD_DLG_CFG_ITS_ILLEGALPARK_CARNUM };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void OnLanguageChanged(int _iLanguage);
	void UI_UpdateText();
	CString GetParkStateByInt(int _iParkStatus);
	void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	CListCtrl m_ListParkCarNumState;

private:
	int m_iLogonID;
	int m_iChannelNo;
public:
	afx_msg void OnBnClickedButtonParkcarnumClear();
};

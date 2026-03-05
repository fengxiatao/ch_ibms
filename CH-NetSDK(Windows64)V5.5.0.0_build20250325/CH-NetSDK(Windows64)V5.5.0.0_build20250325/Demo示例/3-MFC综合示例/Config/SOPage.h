#ifndef _SO_PAGE_H_
#define _SO_PAGE_H_

#include "../BasePage.h"

// CLS_SOPage dialog
#define ONVIFH265_ENABLE  1
#define ONVIFH265_DISABLE 0
#define  PLUGIN_SUB_TYPE_ONVIFH265 0
const int ABLIITY_PLUGIN_REBOOT	= 100;
class CLS_SOPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_SOPage)

public:
	CLS_SOPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_SOPage();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

// Dialog Data
	enum { IDD = IDD_DLG_CFG_SO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnBnClickedButtonSoRun();
	afx_msg void OnBnClickedButtonSoStop();
	afx_msg void OnBnClickedChkService1();
	afx_msg void OnBnClickedChkService2();
	afx_msg void OnBnClickedChkService3();
	afx_msg void OnBnClickedChkService4();
	afx_msg void OnBnClickedChkService5();
	afx_msg void OnBnClickedChkService6();
	afx_msg void OnBnClickedChkService7();
	afx_msg void OnBnClickedChkService8();
	afx_msg void OnBnClickedChkService9();
	afx_msg void OnBnClickedChkService10();
	
	bool UI_UpdatePlatformApp();
	void UI_UpdateDialog();
	void OnvifH265Init();
	bool SupporOnvifH265();
	void CLS_SOPage::ServerSetInit();
	void SaveOnvifH265();
	void CLS_SOPage::MulitiServiceCheck(int iIndex);
	int  CLS_SOPage::GetFuncAbility(int _iMainType, int _iSubType);

private:
	int m_iLogonID;
	CComboBox m_cboPlatformSO;
	CButton m_chkArrService[MAX_APP_SERVER_LIST_NUM];
	CButton m_chkOnvifH265Support;
	
public:
	afx_msg void OnBnClickedButtonRun();
	afx_msg void OnBnClickedCheckOnvifH265support();

};

#endif


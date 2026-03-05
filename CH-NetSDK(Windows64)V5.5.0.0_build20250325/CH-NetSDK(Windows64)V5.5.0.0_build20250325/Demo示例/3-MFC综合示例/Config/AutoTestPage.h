#ifndef _AUTO_TEST_PAGE_H_
#define _AUTO_TEST_PAGE_H_

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
// CLS_AutoTestPage dialog


#define  CBO_INDEX_TEMPERATURE_REVISE	0    //temperature correction

#define  TEMPERATURE_REVISE_DEF			100    //temperature correction

class CLS_AutoTestPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_AutoTestPage)

public:
	CLS_AutoTestPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_AutoTestPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_AUTO_TEST };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateDialogText();
	virtual void OnMainNotify(int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser);
	void InnerAutoTest(int _iCmd, strAutoTest_Para* _pvTestBuf);
	afx_msg void OnBnClickedButtonMultiLanguage();
	afx_msg void OnBnClickedButtonAutoTestAxis();
private:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNO;
	CButton m_chkEnglish;		//English
	CButton m_chkChinese;		//Chinese
	CButton m_chkTraditional;	//traditional
	CButton m_chkKorean;		//Korean
	CButton m_chkSpanish;		//Spain
	CButton m_chkItalian;		//Italy
	CButton m_chkRussian;		//Russian
	CButton m_chkTurkish;		//Turkish
	CButton m_chkThai;			//Thai
	CButton m_chkPolish;		//Polish
	CButton m_chkHebrew;		//Hebrew
	CButton m_chkFrench;		// french
	CButton m_chkGerman;		//German
	CEdit m_edtPixels_X;
	CEdit m_edtPixels_Y;
public:
	afx_msg void OnBnClickedBtnAutotest();
	CComboBox m_cboTestItem;
	CEdit m_edtParaOne;
	CEdit m_ParaTwo;
	CEdit m_edtResult;
	afx_msg void OnBnClickedButtonSetali();
};

#endif

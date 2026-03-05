#ifndef _STORAGE_PATH_SET_H_
#define _STORAGE_PATH_SET_H_

#include "../BasePage.h"
#include "afxwin.h"
// CLS_VideoEncodeSlicePage dialog

class CLS_StoragePathSetPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_StoragePathSetPage)

public:
	CLS_StoragePathSetPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_StoragePathSetPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_STORAGE_SET };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonSetPath();
	afx_msg void OnBnClickedBtnStorageCheck();
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	
private:
	BOOL UI_UpdateStoragePathType();
	void UI_UpdateDialog();
	void ShowFileCheckResult(int _iState, const char *_pFilePath, int _iProgress);

private:
	CComboBox m_cboPathType;
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	
	CEdit m_etFilePath;
	CEdit m_etFileMd5;
	CStatic m_stcResult;
	CEdit m_etFileSize;
	CEdit m_etCheckProgress;
};

#endif

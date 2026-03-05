#ifndef _NVS_DSM_PAGE_H
#define _NVS_DSM_PAGE_H

#include "../BasePage.h"
#include "../Include/NSLOOK_INTERFACE.h"
#include "afxwin.h"
#include "../Common/SortListCtrl.h"

// CLS_NvsDSMPage dialog

class CLS_NvsDSMPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_NvsDSMPage)

public:
	CLS_NvsDSMPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_NvsDSMPage();

// Dialog Data
	enum { IDD = IDD_DLG_MNG_DSM_NVS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iRegID, int _iChannelNo, int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnDestroy();

	afx_msg void OnBnClickedBtnDsmNvsRefresh();
	void DsmNvsRefreshByNslook();
	static int __stdcall NslookRegNotify(int _iCount,st_NvsSingle *_regNVS);
	afx_msg LRESULT OnNslookRegMsg(WPARAM wParam, LPARAM lParam);
	void DsmNvsRefreshByNvssdk();
	static int __stdcall NvssdkRegNotify(int _iTotalCount, int _iCurrentCount, void* _pvNvsList, int _iTotalSize, int _iSingleSize, void* _pvUsrData);
	afx_msg LRESULT OnNvssdkRegMsg(WPARAM wParam, LPARAM lParam);

	afx_msg void OnBnClickedBtnDsmNvsQuery();
	void NslookDsmNvsQuery();
	void NvssdkDsmNvsQuery();
	afx_msg void OnNMDblclkListDsmNvs(NMHDR *pNMHDR, LRESULT *pResult);
	void OnActiveLogonNvsByNslook(int _iItem);
	void OnActiveLogonNvsByNvssdk(int _iItem);
	afx_msg void OnNMClickListDsmNvs(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnGetCount();
	virtual void SetRegisterInfo(RegisterInfo* _ptInfo);
	void AddOneNvsItem(DsmNvsRegInfoEx* _ptNvsEx);
	void UI_UpdateDialog();
	CString GetNvsType(int _iNvsType);
	int GetNvsType(CString _strNvsType);

public:
	static HWND s_hWnd;
	
private:
	CSortListCtrl m_lvNVS;
	int m_iRegID;
	char m_cRegUser[32];
	char m_cRegPwd[32];	
	char m_cRegIP[64];
	int m_iRegPort;
	BOOL m_blUseNslook;
	BOOL m_blUseIpV6;
	
	CComboBox m_cboQueryType;
	CComboBox m_cboPage;
};

#endif

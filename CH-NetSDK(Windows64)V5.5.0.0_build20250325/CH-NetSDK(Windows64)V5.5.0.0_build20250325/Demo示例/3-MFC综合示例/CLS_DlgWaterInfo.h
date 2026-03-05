#pragma once


// CLS_DlgWaterInfo dialog
#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"


#define WM_RESULT_WATERINFO (WM_USER + 1000)
#define WM_RESULT_FINISHED (WM_USER + 1001)

#define RET_EXPORT_SUCCESS   0
#define RET_EXPORT_FAIL      1
#define RET_EXPORT_FINISHED  2
#define RET_FAIL_OPENFILE    3



class CLS_DlgWaterInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgWaterInfo)

public:
	CLS_DlgWaterInfo(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgWaterInfo();

// Dialog Data
	enum { IDD = IDD_DIALOG_WATERINFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
public:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	int m_iChooseSel;
	CListCtrl m_listWaterInfo;
	afx_msg void OnBnClickedButtonAdd();
	afx_msg void OnBnClickedButtonEdit();
	afx_msg void OnBnClickedButtonDel();
	CEdit m_edtWaterLevel;
	CEdit m_edtWaterFlow;
	afx_msg void OnNMClickListWaterinfo(NMHDR *pNMHDR, LRESULT *pResult);

	int GetMaxIndex();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void SetVCAStatus(BOOL _bStatus);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	afx_msg void OnBnClickedButtonSet();
	CComboBox m_cboScene;
	CComboBox m_cboRuleNo;
	CComboBox m_cboGaugeNo;
	CEdit m_edtGaugeAltitude;
	void UpdateGaugeData();
	afx_msg void OnCbnSelchangeComboScene();
	afx_msg void OnCbnSelchangeComboRule();
	afx_msg void OnCbnSelchangeComboGaugeno();
	afx_msg void OnBnClickedButtonImport();
	afx_msg LRESULT Priv_Message_process(WPARAM wParam, LPARAM lParam);

	static DWORD WINAPI BatImportData(LPVOID pParam);
	
	HANDLE hThreadImport;
	int m_iIndex;
	int m_iSuccessCount;
	int m_iFailCount;

	CString m_sFileName;

	afx_msg void OnBnClickedButtonDelbat();
	CComboBox m_cboSceneID;
	CComboBox m_cboMode;
	CComboBox m_cboAreaNo;
	CComboBox m_cboAreaType;
	afx_msg void OnBnClickedButtonWaterSet();
	afx_msg void OnBnClickedButtonCall();
	afx_msg void OnBnClickedButtonDelete();
	CComboBox m_cboGaugeID;
	afx_msg void OnCbnSelchangeComboGaugeid();
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
	CEdit m_edtWaterSpeed;
	CStatic m_stc_Notice;
	BOOL CheckDataValid(int _iWaterLevel);
	BOOL IsEmptyData(CString _strWaterLevel, CString _strWaterFlow, CString _strWaterSpeed);
	void DeleteAllData();
	BOOL JudgeFuncAble();
};

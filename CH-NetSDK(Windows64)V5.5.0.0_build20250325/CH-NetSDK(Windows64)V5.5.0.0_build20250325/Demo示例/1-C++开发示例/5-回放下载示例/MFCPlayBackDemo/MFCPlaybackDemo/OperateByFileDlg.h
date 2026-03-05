#pragma once
#include "afxdtctl.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "GlobalTypes.h"
#include "NetSdk.h"
#include "DisplayDlg.h"
#include <vector>
#include "OperateByTimeDlg.h"
#include <map>
using namespace std;


// COperateByFileDlg dialog

class CLS_OperateByFileDlg : public CDialog
{
	DECLARE_DYNAMIC(CLS_OperateByFileDlg)

public:
	CLS_OperateByFileDlg(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_OperateByFileDlg();

// dialog data
	enum { IDD = IDD_FILE_DISPLAY_DIALOG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX); // DDX/DDV support
	
	DECLARE_MESSAGE_MAP()
	
public:
	void OnLanguageChange(int _iLanguage);
	// grid selected filename
	static CString m_sDownLoadFileName;
	CListCtrl m_lstDownloadFile;
	//display file information
	void DisplayFileInfo(int _iFileTotalCount);
	static map<CString, int> m_mapConnId;
	void ClearList();
	void OnLogonSucc();

private:
	//Initialize the dialog
	void InitDlg();
	//Get query conditions
	void GetQueryCondition();
	//Get the downloaded file name
	CString GetDownloadFileName();
	virtual BOOL OnInitDialog();
	afx_msg void OnBnClickedPlay();
	//Play button message processing
	afx_msg void OnBnClickedQuery();
	// query file
	int NetFileQuery();
	//Download button message processing
	afx_msg void OnBnClickedDownload();
	afx_msg void OnTimer(UINT_PTR _nIDEvent);
	//Download location setting message processing
	afx_msg void OnBnClickedDownloadPosSet();
	//Download speed setting message processing
	afx_msg void OnBnClickedDownloadSpeedSet();
	afx_msg void OnBnClickedButtonStopDownload();
	afx_msg void OnBnClickedPauseDownload();
	afx_msg void OnBnClickedContinueDownload();
	//download file
	unsigned long DownloadFile(int _iLogonID,char* _pcFileName,int _iDownloadPos , int _iDownloadSpeed ,int _iFlag, int _iDownloadCmdID);	
	
	
	int m_iLanguage;
	CEdit m_edtDownloadPos;
	
	CDateTimeCtrl m_dtpStartTime;
	CDateTimeCtrl m_dtpStopTime;
	CComboBox m_cboChannelNum;
	CComboBox m_cboFileType;
	NVS_FILE_TIME m_nvsStartTime;
	NVS_FILE_TIME m_nvsStopTime;
	

	CString m_csChannelNum;
	CString m_csFileType;
	int m_iRowNum;
	int m_iFileCounter;
	int m_iDownloadSize;
	int m_iDownloadPos;
	CString m_csDownloadPos;
	CString m_csDownloadSize;
	CComboBox m_cboDownLoadSpeed;
	map<CString, int> m_mapRowNum;
	map<CString, int> m_mapcsPause;

public:
	CComboBox m_cboStreamNo;
	CComboBox m_cboSaveFileType;
	CComboBox m_cboPageNo;
	int m_iCurrentPage;
	afx_msg void OnCbnSelchangeComboFilePage();
};

#pragma once
#include "afxdtctl.h"
#include "afxwin.h"
#include "NetSdk.h"
#include "afxcmn.h"

// COperateByTime dialog
class CLS_OperateByTime : public CDialog
{
	DECLARE_DYNAMIC(CLS_OperateByTime)

public:
	CLS_OperateByTime(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_OperateByTime();

// dialog data
	enum { IDD = IDD_TIME_DISPLAYDIALOG };

public:
	unsigned int NvsFileTimeToAbsSeconds( NVS_FILE_TIME * _pFileTime );
	void OnLanguageChange(int _iLanguage);
	static int m_iTimeSeconds;
	static CString m_csChannelId;
	static CString m_csStreamType;
	NVS_FILE_TIME m_nvsStartTime;
	NVS_FILE_TIME m_nvsStopTime;
	void OnLogonSucc();
	static CLS_OperateByTime* GetInstance()
	{
		if ( m_sInstance == NULL)
		{
			m_sInstance = new CLS_OperateByTime();
		}
		return m_sInstance;
	}

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	void SetSpeed();
	CString IntToCString( int _iData );
	void GetQueryCondition();
	int DownloadFileByTime(int _iLogonID,  int _iSpeed );
	//unsigned int NvsFileTimeToAbsSeconds( NVS_FILE_TIME * _pFileTime );
	virtual BOOL OnInitDialog();
	afx_msg void OnBnClickedSpeedSet();
	afx_msg void OnBnClickedPlay();
	afx_msg void OnBnClickedDownload();
	afx_msg void OnBnClickedStopDownload();
	afx_msg void OnTimer(UINT_PTR nIDEvent);
	CString m_csDownloadSpeed;
	CString m_csDownloadPos;
	int m_tStart;
	int m_tStop;
	BOOL m_bLanguage;
	CStatic m_txtProcecssCtrl;
	CDateTimeCtrl m_dtcStartTime;
	CDateTimeCtrl m_dtcStopTime;
	//CEdit m_edtDownloadSpeedCtr;
	CEdit m_edtDownloadPosCtrl;
	CProgressCtrl m_pgrsProcessCtrl;
	afx_msg void OnStnClickedStaticProcess();
	static CLS_OperateByTime* m_sInstance;
	CComboBox m_cboChannelIDCtr;
	CComboBox m_cboStreamType;
	CButton m_btnStopDownload;
	CComboBox m_cboDownloadSpeed;
	class CLS_DeleteInstace
	{
	public:
		~CLS_DeleteInstace()
		{
			if( NULL != (CLS_OperateByTime::m_sInstance))
			{
				delete m_sInstance;
				m_sInstance = NULL;
			}
		}
		
	};
	static CLS_DeleteInstace m_deleteInstace;	
public:
	afx_msg void OnCbnSelchangeComboTimeModeDownloadSpeed();
	CComboBox m_cboSaveFileType;
};

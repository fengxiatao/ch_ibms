
// VideoDisplayDlg.h : head File
//

#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "OperateByFileDlg.h"
#include "OperateByTimeDlg.h"

//custom message
#define WM_MSG_NEW_DISPLAY_DIALOG						(WM_USER+100)
#define WM_MSG_LOGON_SUCCESS							(WM_USER+101)
#define WM_MSG_LOGON_FAILED 							(WM_USER+102)
#define WM_MSG_FILE_INFO 								(WM_USER+103)
#define WM_MSG_FILE_COUNT 								(WM_USER+104)
#define WM_MSG_FILE_START_DOWNLOAD 						(WM_USER+105)
#define WM_MSG_FILE_DOWNLOAD_FAILED 					(WM_USER+106)
#define WM_MSG_SPEED_SET_FAILED 						(WM_USER+107)
#define WM_MSG_STOP_SET_FAILED 							(WM_USER+108)
#define WM_MSG_POS_SET_FAILED 							(WM_USER+109)
#define WM_MSG_FILE_DOWNLOAD_FINISH 					(WM_USER+110)
#define WM_MSG_FILE_DOWNLOAD_INPURRT 					(WM_USER+111)
#define WM_MSG_FILE_DOWNLOAD_FAULT	 					(WM_USER+112)
#define WM_MSG_VIDEO_PLAY_FAILED						(WM_USER+113)
#define WM_MSG_FILE_QUERY_FAILED						(WM_USER+114)
#define WM_MSG_DOWNLOAD_BY_TIME_FAILED					(WM_USER+115)
#define WM_MSG_STOP_PLAY_FAILED							(WM_USER+116)
#define WM_MSG_Set_RAW_FRAME_CallBACK_FAILED			(WM_USER+117)
#define WM_MSG_CAPTURE_FAILED							(WM_USER+118)
#define WM_MSG_CAPTURE_SUCCESS							(WM_USER+119)
#define WM_MSG_SET_VOLUME_SUCCESS						(WM_USER+120)
#define WM_MSG_SET_VOLUME_FAILED						(WM_USER+121)
#define WM_MSG_FORWARD_STEP_FAILED						(WM_USER+122)
#define WM_MSG_FORWARD_STEP_SUCCESS						(WM_USER+123)
#define WM_MSG_VOLUME_CTRL_FAILED						(WM_USER+124)
#define WM_MSG_VI_FRAME									(WM_USER+125)
#define WM_MSG_OTHER_TYPE								(WM_USER+126)
#define WM_MSG_AUDIO_FRAME								(WM_USER+127)

// CVideoDisplayDlg dialog
class CVideoDisplayDlg : public CDialog
{
// structure
public:
	CVideoDisplayDlg(CWnd* pParent = NULL);	// Standard constructor
	//~CVideoDisplayDlg();  //destructor

// dialog data
	enum { IDD = IDD_MAIN_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support


// accomplish
protected:
	HICON m_hIcon;

	// Generated message map function
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	//Login success message response function
	afx_msg LRESULT OnLogOnSuccess( WPARAM wParam, LPARAM lParam );
	//Login failure message response function
	afx_msg LRESULT OnLogonFailed( WPARAM wParam, LPARAM lParam );
	//Get the file information message response function
	afx_msg LRESULT OnFileInfo( WPARAM wParam, LPARAM lParam );
	//Get the number of files message response function
	afx_msg LRESULT OnFileCount( WPARAM wParam, LPARAM lParam );
	//Press the file play button processing function
	afx_msg void OnBnClickedDealByFile();
	//Play button processing function by time
	afx_msg void OnBnClickedDealByTime();
	//Login button handler
	afx_msg void OnBnClickedLogon();
	afx_msg LRESULT OnFileStartDownload( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnFileDownloadFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnFileDownloadFault( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnFileDownloadInpurrt( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnFileDownloadFinish( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnPosSetFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnSpeedSetFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnStopSetFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnVideoPlayFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnFileQueryFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnDownloadByTimeFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnSetRawFrameCallbackFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnCaptureFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnCaptureSuccess( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnSetVolumeFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnSetVolumeSuccess( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnForwardStepSuccess( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnVolumeCtrlFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnViFrame( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnOtherType( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnAudioFrame( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnForwardStepFailed( WPARAM wParam, LPARAM lParam );
	afx_msg LRESULT OnStopPlayFailed( WPARAM wParam, LPARAM lParam );
	afx_msg void OnCbnSelchangeComboLanguage();
	afx_msg void OnCbnSelchangeCombo1();
	afx_msg void OnBnClickedLogOff();
	afx_msg void OnDestroy();
	DECLARE_MESSAGE_MAP()
private:
	void InitDlg();
	void AddLog( CString _strData );
	void WriteLog(CString _cstrLogInfo);
	int SetLogSaveDirectory( CString _strPath );
	CString GetLogSaveDirectory();
	CTime m_tTime;
	CString m_cstrTime;
	CString m_strLogFileName;
	CString m_strLogSaveDirectory;
	int m_iRow;//Number of rows
	int m_iColumn;//Number of columns
	FILE* m_pFile;
	int m_iFileCount;	
	CEdit m_edtPortCtrl;
	CEdit m_edtUsernameCtrl;
	CEdit m_edtPasswordCtrl;
	CEdit m_edtIpAddressCtrl;
	CListCtrl m_lstLog;
	CLS_OperateByFileDlg m_OperateByFileDlg;
	CLS_OperateByTime m_OperateByTime;
	CRect m_DialogChild;
	CComboBox m_cboLanguageSelect;
	
protected:
	virtual void OnCancel();
	virtual void OnOK();
public:
	afx_msg void OnClose();
};

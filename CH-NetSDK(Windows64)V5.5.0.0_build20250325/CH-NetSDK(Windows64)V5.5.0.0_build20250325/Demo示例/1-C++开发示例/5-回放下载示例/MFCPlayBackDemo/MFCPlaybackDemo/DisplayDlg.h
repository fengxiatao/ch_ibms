#pragma once
#include "afxwin.h"
#include "Resource.h"
#include "afxcmn.h"
#include "GlobalTypes.h"



// CDisplayDialog dialog

class CLS_CDisplayDlg : public CDialog
{
	DECLARE_DYNAMIC(CLS_CDisplayDlg)

public:
	// standard constructor
	CLS_CDisplayDlg( CWnd* pParent = NULL);
	virtual ~CLS_CDisplayDlg();	
	//Dialog data
	enum { IDD = IDD_DISPLAY_DIALOG };

protected:
	// DDX/DDV support
	virtual void DoDataExchange(CDataExchange* pDX);    

	DECLARE_MESSAGE_MAP()

private:
	//Start playback button response
	afx_msg void OnBnClickedButtonStartDisplay();
	//Stop playback button response
	afx_msg void OnBnClickedButtonPause();
	//Play speed setting response function
	afx_msg void OnBnClickedFastPlay();
	// slow down the response function
	afx_msg void OnBnClickedSlowPlay();
	//Play positioning setting response function
	afx_msg void OnBnClickedPlayPosSet();
	afx_msg void OnDestroy();
	BOOL OnInitDialog();
	CString GetCurrentPath();
	CString MakeCaptureName();
	void LanguageChange(BOOL _bLanguage);
	void StartDisplay();
	void StopPlay();
	void OnSnatch( UINT _nID);
	//Video display txt control
	CStatic m_txtDisplayVideo;
	//speed setting control
	CComboBox m_cboFastPlay;
	CComboBox m_cboSlowPlay;
	CSliderCtrl m_sliVolumeCtr;
	//Play position control
	CEdit m_edtPlayPos;
	CStatic m_txtVolumeDisplay;
	//Play position setting button
	CButton m_btnPlayPosSet;
	//Play position static display
	CStatic m_txtPlayPos;	
	BOOL m_bLanguage;
	BOOL m_bAudioStart;
	unsigned int m_uiConnID;//Connection ID
	int m_iCaptureType;
public:

	void SetConfig(BOOL _bPlayMode, BOOL _bLanguage);
	afx_msg void OnBnClickedButton4();
	afx_msg void OnBnClickedButtonStopPlay();
	afx_msg void OnBnClickedButtonCapture();
	afx_msg void OnBnClickedButtonForwardStep();
	afx_msg void OnBnClickedButtonVolumeCtrl();
	afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
	static void __stdcall GetRawNotify(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser);
	void OnGetRawNotify(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo);
	static CLS_CDisplayDlg* m_pThis;
};

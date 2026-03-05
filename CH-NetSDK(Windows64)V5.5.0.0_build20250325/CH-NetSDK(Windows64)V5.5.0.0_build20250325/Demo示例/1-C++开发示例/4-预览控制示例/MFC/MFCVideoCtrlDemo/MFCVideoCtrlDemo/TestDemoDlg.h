// TestDemoDlg.h : header file
//

#pragma once
#include "afxcmn.h"
#include "afxwin.h"
#include "DeviceControl.h"
#include "VideoView.h"


// CTestDemoDlg dialog
const DWORD g_dwLanChinese = 2052;
const DWORD g_dwLanEnglish = 1033;
CString GetTextByLan( CString _cstrTextCH, CString _cstrTextEn/* = ""*/);
class CTestDemoDlg : public CDialog
{
// construct
public:
	CTestDemoDlg(CWnd* pParent = NULL); // standard constructor

// dialog data
	enum { IDD = IDD_TESTDEMO_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX); // DDX/DDV support


// accomplish
protected:
	HICON m_hIcon;

	// Generated message map function
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	virtual BOOL PreTranslateMessage(MSG* pMsg);
	DECLARE_MESSAGE_MAP()
public:
	int            m_iWorkMode;
	int            m_iChannel;
	int			   m_iConnectID;
	int            m_LogonID;
	CLS_VideoView* m_pVideo;
	BOOL           m_bIsLogon;
	BOOL           m_bIsDisplay;
	BOOL           m_bIsOpen3DLocation;
	RECT           m_rcDrag;
	RECT           m_rcVideo;
	CEdit          m_edtUser;
	CIPAddressCtrl m_DevIP;
	CEdit          m_edtPassword;
	CEdit          m_edtPort;
	CComboBox      m_cboLanguage;
	CComboBox      m_ChannelNo;
	CComboBox      m_cboStreamNo;
public:
	int SDKInit();
	void UpdateDialogText();
	void StartPlay(unsigned int _uConID);
	void StartRecv();
	void SwitchPara();
	void OnSnatch(UINT nID);
	void OnRecord(UINT nID);
	CString MakeFileName();
	int DrawVideoArea(RECT& _rcVideo);
	int ClientToVideo(RECT& _rcScreen,OUT RECT& _rcVideo);
	BOOL SuppotNew3D();
	BOOL IsInVideoView();
	BOOL ProtocolControl( int _iAction,int _iParam1,int _iParam2,int _iEPTZ );
	BOOL TransparentControl(int _iAction,int _iAddress, int _iSpeed, int _iPreset);
	BOOL UI_UpdatePTZ();
	void SetVideoParam();
	void UI_UpdateVideoParam();

    static void MainNotify(int _ulLogonID, long _iWparam, void* _iParam,void* _iUser);
	afx_msg void OnCbnSelchangeComboLanguage();
	afx_msg void OnDestroy();
	afx_msg void OnBnClickedButtonLogon();
	afx_msg LRESULT OnMainNotify(WPARAM wParam, LPARAM lParam);
	afx_msg void OnBnClickedButtonLogoff();

	afx_msg void OnCbnSelchangeCboChannel();
	afx_msg void OnCbnSelchangeComboStream();
	afx_msg void OnBnClickedButtonSnatch();
	afx_msg void OnBnClickedButton3dlocation();
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnLButtonUp(UINT nFlags, CPoint point);

	int m_iComNo;
	int m_iAddress;
	CSliderCtrl m_sldSpeed;
	char m_cDeviceType[64];
	CLS_DeviceControl m_tDevCtrl;
	HWND m_hReverse;

	CSliderCtrl m_slider_hue;
	CSliderCtrl m_slider_bright;
	CSliderCtrl m_slider_contrast;
	CSliderCtrl m_slider_saturation;
	CComboBox m_cboPreNum;

	afx_msg void OnTimer(UINT_PTR nIDEvent);
	afx_msg void OnNMCustomdrawSlider1(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedCheckAuto();

	afx_msg void OnBnClickedButtonSetptz();
	afx_msg void OnBnClickedButtonCallptz();
	afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
	afx_msg void OnNMCustomdrawSliderHue(NMHDR *pNMHDR, LRESULT *pResult);

	afx_msg void OnNMCustomdrawSliderBrightness(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderContrast(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderSaturation(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedButtonDefault();
	CListCtrl m_listInfo;
	afx_msg void OnBnClickedCheckRecord();
	afx_msg void OnBnClickedButtonConnectvideo();
	afx_msg void OnBnClickedButtonDisconnect();
	afx_msg void OnBnClickedButtonSavelog();
	afx_msg void OnBnClickedButtonClearlog();
};

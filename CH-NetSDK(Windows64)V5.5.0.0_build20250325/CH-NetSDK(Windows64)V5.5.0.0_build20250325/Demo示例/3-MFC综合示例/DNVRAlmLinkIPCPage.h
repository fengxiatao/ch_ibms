#pragma once

#include "../BasePage.h"
#include "afxwin.h"
#include "./Config/ChanCheck.h"
// CLS_DNVRAlmLinkIPCPage dialog

class CLS_DNVRAlmLinkIPCPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DNVRAlmLinkIPCPage)

public:
	CLS_DNVRAlmLinkIPCPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DNVRAlmLinkIPCPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_DNVR_ALMLINKIPC };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
private:
	void UI_UpdateDialog();
	void UI_UpdateChannel();
	void UI_ShowLinkCP(int _iCmdShow);
	void UI_ShowLinkAV(int _iCmdShow);
	void UI_ShowLinkType();
	void OffsetWindow(int iID,int dx,int dy);
	int  ActionInit();
	void UI_UpdateChanCheck();
	void UpdateVoice();
	void UI_ShowLinkFrontEnd(int _iCmdShow);
	void MoveAudioWindow(int _iOffset);
	void MoveCPWindow(int _iOffset);
	void UpdateNVRLinkIPC(int _iAreaNo);
	void UI_UpdateCurIndex(int _iCurIndex);
	void SetNVRLinkIPC();
private:
	CButton m_btnChannelEnable;
	CButton m_chkDisplayEnable;
	CButton m_chkSoundEnable;
	CButton m_btnAVideo;
	CButton m_btnLink;
	int m_iLogonID;
	int m_iChannelNo;
	CButton m_chkChannelEnable0;
	CComboBox m_cboAreaNo;
	CComboBox m_cboNo;
	CComboBox m_iSerialNo;
	CComboBox m_cboFrontEndLinkType;
	CLS_ChanCheck* m_pclsChanCheck;
	CComboBox m_cboLinkChannel;
	bool m_bFrontChangeCPWindow;
	bool m_bFrontChangeAVWindow;

	AlarmNVRLinkIPCParam m_tAlarmNVRLinkIPCParam;
public:
	afx_msg void OnCbnSelchangeComboAreano();
	afx_msg void OnCbnSelchangeComboFrontend();
	afx_msg void OnBnClickedBtnAlarmLinkFrontend();
	afx_msg void OnCbnSelchangeComboNo();
	afx_msg void OnBnClickedButtonSetarealink();
};

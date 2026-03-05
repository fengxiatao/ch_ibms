#ifndef _DNVR_ALM_LINK_PAGE_H
#define _DNVR_ALM_LINK_PAGE_H

#include "../BasePage.h"
#include "afxwin.h"
#include "ChanCheck.h"
// CLS_DNVRAlmLinkPage dialog

class CLS_DNVRAlmLinkPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DNVRAlmLinkPage)

public:
	CLS_DNVRAlmLinkPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DNVRAlmLinkPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_DNVR_ALMLINK };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
private:
	void UI_UpdateDialog();
	BOOL UI_UpdateType();
	BOOL UI_UpdateLinkSetAVideo();
	BOOL UI_UpdateLinkSinPic();
	void UI_ShowLinkCP(int _iCmdShow);
	void UI_ShowLinkAV(int _iCmdShow);
	void UI_ShowLinkPTZ(int _iCmdShow);
	void UI_ShowLinkSinPic(int _iCmdShow);
	void UI_ShowLinkCommonEnable(int _iCmdShow);
	void UI_ShowLinkType();
	void UI_ShowWhite(int _iCmdShow);
	void OffsetWindow(int iID,int dx,int dy);
	int  ActionInit();
	void UI_UpdateChanCheck();
	void UpdateVoice();
private:
	CComboBox m_cboType;
	CComboBox m_cboLinkType;
	CComboBox m_cboInPort;
	CButton m_btnChannelEnable;
	CButton m_chkDisplayEnable;
	CButton m_chkSoundEnable;
	CButton m_btnAVideo;
	CComboBox m_cboLinkChannelNo;
	CComboBox m_cboLinkPTZType;
	CEdit m_edtLinkACTNo;
	CButton m_btnLink;
	int m_iLogonID;
	int m_iChannelNo;
	int m_iChangeInPort;
	int m_iChangeOutPort;
	CButton m_chkChannelEnable0;
	CComboBox m_cboSinglePic;
	CComboBox m_cboCommonEnable;
	CComboBox m_cboWhiteEnable;
	int m_iAlarmTypeCMD;
	CLS_ChanCheck* m_pclsChanCheck;
	CEdit m_edtLinkResidenceTime;
public:
	afx_msg void OnCbnSelchangeComboType();
	afx_msg void OnCbnSelchangeComboLinktype();
	afx_msg void OnCbnSelchangeComboInport();
	afx_msg void OnBnClickedButtonChannelenable();
	afx_msg void OnBnClickedButtonAvideo();
	afx_msg void OnCbnSelchangeComboLinkchannelno();
	afx_msg void OnBnClickedButtonLink();
	afx_msg void OnBnClickedButtonSinglepicChannel();
	afx_msg void OnBnClickedBtnAlarmLinkCommonEnableSet();
	CComboBox m_iSerialNo;
private:
	CComboBox m_cboAddPara1;
	CComboBox m_cboAddPara2;
public:
	afx_msg void OnCbnSelchangeComboAddPara1();
	afx_msg void OnCbnSelchangeComboAddPara2();
	afx_msg void OnBnClickedButtonWhiteSet();
};
#endif

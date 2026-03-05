#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"

// CLS_VCAEVENT_AudioDiagnose dialog
#define		MAX_

class CLS_VCAEVENT_AudioDiagnose : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_AudioDiagnose)

public:
	CLS_VCAEVENT_AudioDiagnose(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_AudioDiagnose();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_AUDIODIAGNOSE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboUnAlarmColor;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboEnableType;
	CComboBox m_cboLevel;
	CButton m_chkDisplayRule;
	CButton m_chkEnableType;
public:
	void OnLanguageChanged();
	void UpdateUIText();
	void UpdatePageUI();
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnVcaAudioSet();
	CButton m_chkType[VCA_AUDIO_MAX];
	afx_msg void OnBnClickedChkVcaType();
};

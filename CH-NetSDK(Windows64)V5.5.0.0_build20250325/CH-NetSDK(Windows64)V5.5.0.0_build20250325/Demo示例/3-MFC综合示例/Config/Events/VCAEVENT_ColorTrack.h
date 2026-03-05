#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "VCAEventBasePage.h"


class CLS_VCAEVENT_ColorTrack : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_ColorTrack)

public:
	CLS_VCAEVENT_ColorTrack(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_ColorTrack();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_COLOR_TRACK };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	void OnLanguageChanged();
	void UI_UpdateDialogText();
	void UI_UpdateParam();

private:
	CComboBox m_cboAreaColor;
	CComboBox m_cboAlarmAreaColor;
	CComboBox m_cboTrackTargetColor;
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnColorTrackEventSet();
};

	
	

#pragma once

#include "CLS_PageBase.h"
#include "afxwin.h"


#define ALARMLINK_OUTPORT_MAXNUM	2

// CLS_DlgFaceAlarmLink Dialog

class CLS_DlgFaceAlarmLink : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceAlarmLink)

public:
	CLS_DlgFaceAlarmLink(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFaceAlarmLink();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_ALARM_LINK };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	void UI_Init();

	CComboBox m_cboLibKey;
	CComboBox m_cboAlarmType;
	CComboBox m_cboVcaType;
	CComboBox m_cboAlarmLinkType;
	CButton m_chkAlarmOutPort[ALARMLINK_OUTPORT_MAXNUM];


	afx_msg void OnBnClickedBtnAlarmlinkSet();
	afx_msg void OnBnClickedBtnAlarmlinkGet();
	afx_msg void OnBnClickedBtnAlarmlinkLibkey();
	afx_msg void OnCbnSelchangeCboAlarmlinkAlarmType();
	
};

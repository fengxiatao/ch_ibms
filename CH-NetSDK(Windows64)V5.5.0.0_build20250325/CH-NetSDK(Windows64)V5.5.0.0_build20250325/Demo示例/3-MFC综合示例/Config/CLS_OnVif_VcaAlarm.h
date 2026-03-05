#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"

// CLS_OnVif_VcaAlarm dialog

class CLS_OnVif_VcaAlarm : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_OnVif_VcaAlarm)

public:
	CLS_OnVif_VcaAlarm(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_OnVif_VcaAlarm();
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	virtual void OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser);
	CString IntToStr(int _iNum);

// dialog data
	enum { IDD = IDD_DLG_ONVIF_VCAALARM };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboState;
	CListCtrl m_listParam;
};

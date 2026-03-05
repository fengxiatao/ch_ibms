#ifndef _FUNC_UNIQUE_ALERT_EVENT_TRIPWIRE_H
#define _FUNC_UNIQUE_ALERT_EVENT_TRIPWIRE_H

#include "BasePage.h"
#include "afxwin.h"

//Feature Alert - Event Parameters - Tripwire
class CLS_DlgUniqueAlertEventTripwire : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertEventTripwire)

public:
	CLS_DlgUniqueAlertEventTripwire(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertEventTripwire();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_EVENT_TRIPWIRE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;

private:
	void	UI_InitDlgItemText();
	void	UI_UpdateInterfaceParam();

public:
	CComboBox m_cboAlertSceneNo;
	CComboBox m_cboTargetType;
	CComboBox m_cboAreaColor;
	CComboBox m_cboAlarmAreaColor;
	CComboBox m_cboTripwireCrossType;

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnCbnSelchangeCboAlertTripwireScene();
	afx_msg void OnBnClickedBtnAlertTripwireEventSet();
};


#endif

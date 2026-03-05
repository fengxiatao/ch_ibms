#ifndef _FUNC_UNIQUE_ALERT_ALARM_MESSAGE_H
#define _FUNC_UNIQUE_ALERT_ALARM_MESSAGE_H

#include "BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


//Featured Alerts - Alert Messages
class CLS_DlgUniqueAlertAlarmMessage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertAlarmMessage)

public:
	CLS_DlgUniqueAlertAlarmMessage(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertAlarmMessage();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_ALARM_MESSAGE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;

private:
	void	UI_InitDlgItemText();

public:
	CListCtrl m_lstAlarmInfo;

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void OnAlarmNotify(int _iLogonID, int _iChannelNo, int _iAlarmIndex,int _iAlarmType,int _iUserData);
	afx_msg void OnBnClickedBtnAlertAlarmClear();
};


#endif

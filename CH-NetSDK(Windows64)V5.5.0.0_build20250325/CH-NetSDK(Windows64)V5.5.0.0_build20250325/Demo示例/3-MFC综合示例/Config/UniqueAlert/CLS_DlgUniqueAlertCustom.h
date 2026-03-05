#ifndef _FUNC_UNIQUE_ALERT_CUSTOM_H
#define _FUNC_UNIQUE_ALERT_CUSTOM_H

#include "BasePage.h"
#include "afxwin.h"

//Featured Alerts - Custom Alerts
class CLS_DlgUniqueAlertCustom : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertCustom)

public:
	CLS_DlgUniqueAlertCustom(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertCustom();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_CUSTOM };

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

	void	UI_UpdateInfoAlarmSchedule();

public:
	CButton m_cbkSelAll;
	CButton m_chkScheduleWeekDay[MAX_DAYS];
	CComboBox m_cboAlertSceneNo;
	CComboBox m_cboChnAnalyzeType;
	CComboBox m_cboEventType;

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnBnClickedBtnAlertAlarmScheduleSet();
	afx_msg void OnBnClickedChkWeekDay(unsigned int _uiWigetId);
	afx_msg void OnBnClickedChkWeenDaySelAll();
	afx_msg void OnCbnSelchangeCboAlertChnType();
	afx_msg void OnCbnSelchangeCboCustomAlertScene();
	afx_msg void OnCbnSelchangeCboScheduleAlertType();
};


#endif

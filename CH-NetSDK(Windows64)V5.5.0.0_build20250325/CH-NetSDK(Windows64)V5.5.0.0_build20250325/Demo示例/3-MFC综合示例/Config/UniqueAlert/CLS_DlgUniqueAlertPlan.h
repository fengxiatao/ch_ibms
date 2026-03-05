#ifndef _FUNC_UNIQUE_ALERT_PLAN_H
#define _FUNC_UNIQUE_ALERT_PLAN_H

#include "BasePage.h"
#include "afxwin.h"

//Number of characteristic alert plans
#define		INNER_MAX_UNIQUE_ALERT_PLAN_NUM		12

//Featured Alert - Alert Plan
class CLS_DlgUniqueAlertPlan : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertPlan)

public:
	CLS_DlgUniqueAlertPlan(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertPlan();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_PLAN };

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


	void	UI_UpdateInfoAlertLinkMode();
	void	UI_UpdateInfoAlertSupportPlan();

public:
	CComboBox m_cboAlertSceneNo;
	CComboBox m_cboAlertType;
	CButton m_chkAlertPlan[INNER_MAX_UNIQUE_ALERT_PLAN_NUM];

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnBnClickedBtnAlertPlanSet();
	afx_msg void OnCbnSelchangeCboAlertPlanType();
	afx_msg void OnCbnSelchangeCboAlertPlanSceneNum();
};


#endif

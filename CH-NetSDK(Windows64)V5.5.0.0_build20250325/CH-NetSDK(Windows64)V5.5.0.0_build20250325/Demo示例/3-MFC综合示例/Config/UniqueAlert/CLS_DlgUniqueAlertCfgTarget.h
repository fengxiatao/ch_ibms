#ifndef _FUNC_UNIQUE_ALERT_TARGET_H
#define _FUNC_UNIQUE_ALERT_TARGET_H

#include "BasePage.h"
#include "afxwin.h"

//Featured Alert - Overlay Parameters and Colors
class CLS_DlgUniqueAlertTarget : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertTarget)

public:
	CLS_DlgUniqueAlertTarget(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertTarget();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_TARGET };

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

	void	UI_UpdateInfoAlertTargetInfo();

public:
	CComboBox m_cboAlertScene;
	CComboBox m_cboTargetColor;
	CComboBox m_cboAlarmTargetColor;

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnBnClickedBtnAlertTargetSet();
	afx_msg void OnCbnSelchangeCboAlertTargetScene();
};


#endif

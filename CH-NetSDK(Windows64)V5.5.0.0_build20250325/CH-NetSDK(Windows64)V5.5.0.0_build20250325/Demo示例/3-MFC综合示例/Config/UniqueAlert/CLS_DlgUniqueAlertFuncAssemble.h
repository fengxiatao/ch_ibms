#ifndef _FUNC_UNIQUE_ALERT_FUNC_ASSEMBLE_H
#define _FUNC_UNIQUE_ALERT_FUNC_ASSEMBLE_H

#include "BasePage.h"
#include "afxwin.h"

//Featured Alert - Ability Set
class CLS_DlgUniqueAlertFuncAssemble : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertFuncAssemble)

public:
	CLS_DlgUniqueAlertFuncAssemble(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertFuncAssemble();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_FUNC_ASSEMBLE };

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

	void	UI_UpdateInfoWhiteLightMode();
	void	UI_UpdateInfoWhiteLightLevelParam();

public:
	CComboBox m_cboEventType;
	CComboBox m_cboAlertLevel;

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnCbnSelchangeCboWlightAlertType();
	afx_msg void OnCbnSelchangeCboWlightAlertLevel();
};


#endif

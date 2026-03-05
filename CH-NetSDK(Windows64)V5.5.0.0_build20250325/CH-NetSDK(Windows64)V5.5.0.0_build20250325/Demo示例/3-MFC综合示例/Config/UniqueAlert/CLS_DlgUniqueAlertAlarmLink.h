#ifndef _FUNC_ALERT_ALARM_LINK_H
#define _FUNC_ALERT_ALARM_LINK_H

#include "BasePage.h"
#include "afxwin.h"
#include "..\ChanCheck.h"

//Featured alert-alarm linkage
class CLS_DlgUniqueAlertAlarmLink : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertAlarmLink)

public:
	CLS_DlgUniqueAlertAlarmLink(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertAlarmLink();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_ALARM_LINK };

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


	void	UI_InitDlgWidget();
	void	UI_Get_AlarmBaseInfo(UniqueAlertAlarmLink& _tAlarmInfo);
	void	UI_Update_AlarmBaseInfo(UniqueAlertAlarmLink _tAlarmInfo);

	void	UI_Update_AlarmLinkInfo_Group_1();
	void	UI_Update_AlarmLinkInfo_Group_2();
	void	UI_Update_AlarmLinkInfo_WhiteLight();

public:
	CComboBox m_cboAlertScene;
	CComboBox m_cboAlertType;
	CComboBox m_cboAlertLevel;
	CComboBox m_cboLinkType_1;
	CComboBox m_cboLinkType_2;
	CComboBox m_cboLinkWhiteLightMode;
	CLS_ChanCheck* m_pclsChanCheck;

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnBnClickedBtnAlertAlinkLinkInfoSet1();
	afx_msg void OnBnClickedBtnAlertAlinkLinkInfoSet2();
	afx_msg void OnBnClickedBtnAlertAlinkLinkInfoSet3();
	afx_msg void OnCbnSelchangeCboAlertAlinkScene();
	afx_msg void OnCbnSelchangeCboAlertAlinkAlertType();
	afx_msg void OnCbnSelchangeCboAlertAlinkAlertLevel();
	afx_msg void OnCbnSelchangeCboAlertAlinkLinkInfoType1();
	afx_msg void OnCbnSelchangeCboAlertAlinkLinkInfoType2();
};


#endif

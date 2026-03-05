#ifndef _FUNC_UNIQUE_ALERT_EVENT_CLIMBWALL_H
#define _FUNC_UNIQUE_ALERT_EVENT_CLIMBWALL_H

#include "BasePage.h"
#include "afxwin.h"

//Feature Alert - Event Parameters - Tripwire
class CLS_DlgUniqueAlertEventClimbWall : public CLS_BasePage
{
    DECLARE_DYNAMIC(CLS_DlgUniqueAlertEventClimbWall)

public:
    CLS_DlgUniqueAlertEventClimbWall(CWnd* pParent = NULL);   // Standard constructor
    virtual ~CLS_DlgUniqueAlertEventClimbWall();

    // dialog data
    enum { IDD = IDD_DLG_CFG_ALERT_EVENT_CLIMBWALL };

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

public:
    virtual BOOL OnInitDialog();
    afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
    virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
    virtual void OnLanguageChanged(int _iLanguage);
    afx_msg void OnCbnSelchangeCboAlertCLIMBWALLScene();
    afx_msg void OnBnClickedBtnAlertCLIMBWALLEventSet();

};


#endif

#pragma once

#include "BasePage.h"
#include "afxcmn.h"
// CLS_DlgChannelAlarmInfo dialog

class CLS_DlgChannelAlarmInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgChannelAlarmInfo)

public:
	CLS_DlgChannelAlarmInfo(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgChannelAlarmInfo();

// Dialog Data
	enum { IDD = IDD_DIALOG_CHANNEL_ALARMINFO };

	void AddDataToLst(ChannelCurAlarmInfo &tChannelCurAlarmInfo);
    virtual void OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser);
    void GetAlarmInfo();
	CString IntToCStr(int _iNum);
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CListCtrl m_lstAlarmInfo;
	afx_msg void OnBnClickedButtonRefresh();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	int m_iLogonID;
	int m_iChannelNo;
public:
    CListCtrl m_lstAlarmNotify;
    afx_msg void OnBnClickedButtonRefreshAlarmnotify();
};

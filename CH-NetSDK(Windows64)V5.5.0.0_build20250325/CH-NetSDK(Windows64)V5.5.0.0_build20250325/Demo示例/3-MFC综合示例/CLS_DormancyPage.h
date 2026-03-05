#pragma once
#include "afxwin.h"
#include "../BasePage.h"


// CLS_DormancyPage dialog


class CLS_DormancyPage : public CLS_BasePage 
{
	DECLARE_DYNAMIC(CLS_DormancyPage)

public:
	CLS_DormancyPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DormancyPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_DORMANCY };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedCheck18();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
public:
	void ChangeAllChooseState(bool state);
	void UI_UpdateDialog();
	void UI_Clear();
	bool CheckPTZTime();
	void UpdateWeek(int iWeek, TDormancySchedule *pDormancySchedule);
	void UpdateState();
public:
	CButton m_iEnable[8];
	CComboBox m_iWeekDay;
	CDateTimeCtrl m_dtBeginTime[MAX_SCHEDULE];
	CDateTimeCtrl m_dtEndTime[MAX_SCHEDULE];
	CComboBox m_cboSegType[MAX_SCHEDULE];
	CButton m_bChooseAll;
	CButton m_bChooseDate[MAX_WEEK_DAYS];
	int m_iLogonID;
	int m_iChannelNO;
	BOOL m_bState;
	CBrush m_brush;
	afx_msg void OnBnClickedCheckDay1();
	afx_msg void OnBnClickedChkWeek(UINT _uiID);
	afx_msg void OnBnClickedButtonSetdormancy();
	CStatic m_DevState;
	afx_msg void OnBnClickedButtonsetawake();
	afx_msg void OnCbnSelchangeComboWeek();
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
	afx_msg void OnBnClickedCheck14();
};

#pragma once
#include "BasePage.h"
#include "afxwin.h"
#include "stdafx.h"
#include "Config/ChanCheck.h"
// CLS_Shdb dialog

#define CUR_GROUPNUM  2

class CLS_Shdb : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_Shdb)

public:
	CLS_Shdb(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_Shdb();

// dialog data
	enum { IDD = IDD_DIALOG_SHDB };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
private:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	ChannelList tChanList[256];
	ShdbRunState tShdbRunState;
	ShdbAlarmPic tShdbAlarmPic;
	ShdbTimeSnap tShdbTimeSnap;
	QueryShdbApprepairSys tQueryShdbApprepairSys;
	QueryShdbServiceType  tQueryShdbServiceType;
	QueryShdbTestMainTain tQueryShdbTestMainTain;
	QueryShdbCheckManage  tQueryShdbCheckManage;
	CButton m_check_alarm;
	CComboBox m_combo_pretm;
	CComboBox m_combo_delaytm;
	CComboBox m_combo_intervaltm;
	CComboBox m_combo_timesnap;
	CButton m_check_tmenable;
	CComboBox m_CComboBoxGroupSnapNum[4];
	CComboBox m_CComboBoxGroupStartTime;
	CComboBox m_CComboBoxGroupEndTime;
	CEdit m_CEditStartTime[4];
	CEdit m_CEditEndTime[4];
	CButton m_ChkGroupEnable[4];
	CComboBox  m_cboshdbrepairsysname;
	CComboBox  m_cboshdbrepairtype;
	CComboBox m_cmobo_repairsysyname;
	CComboBox m_combo_repairtype;
	CComboBox m_combo_maintaintype;
	CComboBox m_combo_mainrepairtype;
	CComboBox m_combo_iopertype;
	CLS_ChanCheck* m_pclsChanCheck;
	CButton m_chkChannelEnable[32];
	CEdit m_edit_policeid;
	CEdit m_edit_password;
public:
	afx_msg void OnBnClickedButton2();
	afx_msg void OnBnClickedButtonMaintain();
	afx_msg void OnBnClickedButtonTestmaintain();
	afx_msg void OnBnClickedCheckRunstate();
	afx_msg void OnBnClickedButtonCheckmanage();	
	void SetUIText();
	void UpdateParam();
	virtual void OnMainNotify( int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
	void UI_UpdateChanCheck();
	afx_msg void OnBnClickedButtonAlarmpic();
	afx_msg void OnBnClickedButtonTimesnap();
};

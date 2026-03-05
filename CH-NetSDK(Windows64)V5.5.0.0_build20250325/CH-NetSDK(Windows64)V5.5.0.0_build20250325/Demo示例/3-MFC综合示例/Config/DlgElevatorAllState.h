#pragma once

#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"
// CLS_DlgElevatorAllState dialog

class CLS_DlgElevatorAllState : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgElevatorAllState)

public:
	CLS_DlgElevatorAllState(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgElevatorAllState();

	void Update_State();

// Dialog Data
	enum { IDD = IDD_DIALOG_ELEVATOR_ALLSTATE };
	void UI_UpdateDialogText();

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	int m_iLogonID;
	int m_iChannelNo;

public:
	afx_msg void OnBnClickedButtonStoreyinfo();
	int m_iStateChannelNo;
	int m_iFloor;
	int m_iDirection;
	int m_iSpeed;
	int m_iTemprature;
	int m_iHumidity;
	CComboBox m_cboBindBrake;
	CComboBox m_cboMaintenance;
	CComboBox m_cboLeveling;
	CComboBox m_cboOpendoor;
	CComboBox m_cboCarshStop;
	CComboBox m_cboBodyInduction;
	CComboBox m_cboMainFloor;
	int m_iRunTime;
	int m_iBindBrakeCn;
	int m_iOpenDoorCn;
	int m_iOperationMileage;
	int m_iAcceler;
	CString m_cAlarmTime;
	CString m_csAlarm;
	CString m_csFloorDisplay;
};

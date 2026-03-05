#pragma once
#include "afxwin.h"
#include "../BasePage.h"
// CLS_IOPORTPAGE dialog


#define ALARM_PORT_SETOBJECT_LOCAL		0	//local
#define ALARM_PORT_SETOBJECT_CHANNEL	1	//IPC under NVR

#define IPC_ALARM_OUTPORT_NUM			2	//Total number of front-end alarm outputs
#define IPC_ALARM_INPORT_NUM			3	//Total number of front-end alarm inputs

class CLS_IOPORTPAGE : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_IOPORTPAGE)

public:
	CLS_IOPORTPAGE(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_IOPORTPAGE();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_IOPORT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser);
private:
	void UI_UpdateDialog();
	BOOL UI_UpdateIOPortParam();
	BOOL UI_UpdateIOPortParamEx(int _iInPortNum, int _iOutPortNum);

private:
	int m_iLogonID;
	int m_iChannelNO;
	int m_iStreamNO;
	int m_iEnable[LEN_16];
	CButton m_chkOutPortEnable[32];
public:
	afx_msg void OnBnClickedAlarminmode();
	CComboBox m_cmbAlarmInPortID;
	CComboBox m_cmbAlarmInModeID;
	afx_msg void OnBnClickedButtonalarmoutmode();
	CComboBox m_cmbAlarmOutModeID;
	//afx_msg void OnBnClickedCheckAlarmoutportEnable();
	//CButton m_chkAlarmOutPortEnable;
	afx_msg void OnBnClickedButtonAlarmlinkSet();
	afx_msg void OnBnClickedButtonStopamodeSet();
	CComboBox m_cmbAlarmOutPortID;
	CComboBox m_cmbStopModeID;
	afx_msg void OnBnClickedCheckAlarmport();
	CComboBox m_cmbInPortChnID;
	CComboBox m_cmbAlarmActiveModeOutPortID;
	CEdit m_edtClearAlarmDelayTime;
	CButton m_chkOutPortEnableID;
	CButton m_chkAlarmPortEnableID;
	CComboBox m_cmbAlarmOutPortStatusID;
	CComboBox m_cmbInPortState;
	afx_msg void OnCbnSelchangeComboAlarminport();
	afx_msg void OnCbnSelchangeComboalarmoutport();
	afx_msg void OnCbnSelchangeComboIoportNum();
	afx_msg void OnCbnSelchangeComboAlarmoutport();
	afx_msg void OnEnChangeEditDelaytime();
	afx_msg void OnBnClickedButtonAlarmoutportEnableSet();
	afx_msg void OnBnClickedChkChannel(UINT _uiID);
	void OnMsgAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize);
private:
	CComboBox m_cboAlmInPortType;
	static CLS_IOPORTPAGE* m_pThis;

public:
	CEdit m_fAccurancy;
	CComboBox m_cmbAlarmPortSetObj;
	afx_msg void OnCbnSelchangeComboAlminporType();
	afx_msg void OnCbnSelchangeComboAlmportSetobj();
	afx_msg void OnCbnSelchangeComboInportStatus();
	void  SetAlarmlinkStatus();
private:
	CEdit m_edtAlarmDelayTime;
};

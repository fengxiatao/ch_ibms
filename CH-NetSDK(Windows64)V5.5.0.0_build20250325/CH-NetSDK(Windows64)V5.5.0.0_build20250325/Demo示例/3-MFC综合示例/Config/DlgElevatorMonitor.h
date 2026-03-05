#pragma once

#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"
// CLS_DlgElevatorMonitor dialog

class CLS_DlgElevatorMonitor : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgElevatorMonitor)

public:
	CLS_DlgElevatorMonitor(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgElevatorMonitor();

	void Update_Monitor();
	void AddDataToLst(ElevatorStoreyInfo &tElevatorStoreyInfo);
	void Update_StoreyInfo();

// Dialog Data
	enum { IDD = IDD_DIALOG_ELEVATOR_MONITOR };
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
	ElevatorStoreyInfo *m_ptElevatorStoreyInfo;
	int m_iCurFloor;

public:
	int m_iStartShorkThreshold;
	int m_iMoveSpeed;
	int m_iBodyInductionMode;
	int m_iEbikeDetectEnable;
	int m_iSwaySensitivity;
	int m_iTopLimit;
	int m_iBottomLimit;
	int m_iMainFloor;
	CComboBox m_cboLevelMode;
	CComboBox m_cboOpenDoorMode;
	CComboBox m_cboMaintenMode;
	CComboBox m_cboCarshStopMode;
	CComboBox m_cboPIRNode;
	int m_iStartFloor;
	int m_iEndFloor;
	int m_iHeight;
	int m_iDefHeight;
	int m_iFloorTotalNum;
	CListCtrl m_lstFloor;
	afx_msg void OnBnClickedButtonSetMonitor();
	afx_msg void OnNMClickListFloorheight(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnEnChangeEdit29();
	afx_msg void OnEnChangeEdit21();
	afx_msg void OnEnChangeEdit25();
	afx_msg void OnBnClickedButtonStoreyinfo();
	afx_msg void OnEnChangeEditFloorinfo();
};

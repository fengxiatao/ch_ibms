#pragma once

#include "VCAEventBasePage.h"
#include "afxwin.h"

// CLS_VCAEVENT_LeaveDetectEx dialog

class CLS_VCAEVENT_LeaveDetectEx : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_LeaveDetectEx)

public:
	CLS_VCAEVENT_LeaveDetectEx(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VCAEVENT_LeaveDetectEx();

// Dialog Data
	enum { IDD = IDD_DLG_VCAEVENT_LEAVE_DETECT_EX };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
private:
	CButton m_chkDisplayRule;
	CButton m_chkDiaplayState;
	CEdit m_edtAreaNum;
	CEdit m_edtLeaveAlarmTime;
	CEdit m_edtRuturnClearAlarmTime;
	VCALeaveDetectEx m_tLeaveDetect;
public:
	virtual BOOL OnInitDialog();
	void OnLanguageChanged();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonLeavedetectSet();
	void UpdatePageUI();
	void UpdateUIText();
	void CleanText();
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);

	
private:
	CComboBox m_cboAreaColor;
	CComboBox m_cboAlarmAreaColor;
public:
	CButton m_chkEventValid;
	CComboBox m_cboDisplayTarget;
	int m_iDutyNum;
	int m_iSensitivity;
	int m_iMin;
	int m_iMax;
	CComboBox m_cboRegionNum;
	afx_msg void OnCbnSelchangeComboRegion();
	afx_msg void OnBnClickedBtnDredgePointsDraw();
};

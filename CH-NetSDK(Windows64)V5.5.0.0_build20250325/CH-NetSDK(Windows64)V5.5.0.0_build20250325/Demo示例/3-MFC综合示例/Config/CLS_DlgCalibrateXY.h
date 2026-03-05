#pragma once
#include "BasePage.h"
#include "afxwin.h"

// CLS_DlgCalibrateXY dialog

class CLS_DlgCalibrateXY : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgCalibrateXY)

public:
	CLS_DlgCalibrateXY(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgCalibrateXY();

	void UpdateParam();
	void UpdateUIText();
	void UpdataIndexCountByPosNum(int _iPosNum);

// dialog data
	enum { IDD = IDD_DLG_CALIBRATEXY };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonCalibratexySave();
	afx_msg void OnBnClickedButtonCalibratexySet();
	afx_msg void OnBnClickedButtonAutotestmultSet();
	afx_msg void OnCbnSelchangeComboCalibratexyChnum();
	afx_msg void OnCbnSelchangeComboCalibratexyPicno();
	afx_msg void OnCbnSelchangeComboCalibratexyPosnum();
	afx_msg void OnCbnSelchangeComboParanum();

	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);

	CComboBox m_CboCalibrateXYPosNum;
	CComboBox m_CboCalibrateXYPosIndex;
	CComboBox m_CboCalibrateXYChNum;
	CComboBox m_CboCalibrateXYPicNo;
	CComboBox m_CboTestItem;
	CComboBox m_CboParaNum;
	CEdit     m_EdtTestParam1;
	CEdit     m_EdtTestParam2;
	CEdit     m_EdtTestParam3;
	CEdit     m_EdtTestParam4;
	CEdit     m_EdtTestParam5;
	CEdit     m_EdtTestParam6;
	CEdit     m_EdtTestParam7;

	int       m_iChannelNo;
	SingleScreenCalParam  m_tParam[MAX_CALIBRATION_SCREEN_COUNT];

};

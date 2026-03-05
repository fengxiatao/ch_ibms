#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "VCAEventBasePage.h"

// VCAEVENT_TargetDetection dialog

class CLS_VCAEVENT_TargetDetection : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_TargetDetection)

public:
	CLS_VCAEVENT_TargetDetection(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CLS_VCAEVENT_TargetDetection();

// 对话框数据
	enum { IDD = IDD_DLG_VCAEVENT_TARGETDETECTION };


	
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()

private:
	virtual void OnLanguageChanged();
	void UI_UpdateDialog();
	void UI_UpdatePage();
	void UpdateDrawFinishRegionNum();

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void UpdateUIText();
	CButton m_chkEventEnable;
	CComboBox m_cboDevType;
	CComboBox m_cboCurRegionNo;
	CEdit m_editRegionPoins;
	CSliderCtrl m_sldSensitive;
	VCAParaTarget m_tTargetDetection;
	afx_msg void OnBnClickedButtonTargetSet();
	afx_msg void OnNMCustomdrawSliderIdcStaticSensitivity(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeComboCurrentArea();
	afx_msg void OnBnClickedButtonDrawSet();
	afx_msg void OnBnClickedButtonTargetOn();
	afx_msg void OnBnClickedButtonYtargetOff();
};
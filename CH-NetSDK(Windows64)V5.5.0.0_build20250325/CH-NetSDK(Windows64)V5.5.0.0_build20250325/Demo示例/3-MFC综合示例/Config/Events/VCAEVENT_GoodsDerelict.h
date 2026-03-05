#pragma once

#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_VCAEVENT_GoodsDerelict dialog

class CLS_VCAEVENT_GoodsDerelict : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_GoodsDerelict)

public:
	CLS_VCAEVENT_GoodsDerelict(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_GoodsDerelict();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_GOODS_DERELICT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:	
	virtual BOOL OnInitDialog();
	void OnLanguageChanged();
	void UpdateUIText();
	void CleanText();
	void UpdatePageUI();
	int ToDrawArea(int _iAreaNum);
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnDerelictMainArea();
	afx_msg void OnBnClickedBtnDerelictChildarea1();
	afx_msg void OnBnClickedBtnDerelictChildarea2();
	afx_msg void OnBnClickedBtnDerelictChildarea3();
	afx_msg void OnBnClickedBtnDerelictSet();
private:
	CButton m_chkEventValid;
	CButton m_chkAlarmRule;
	CButton m_chkAlarmStat;
	CButton m_chkTargetBox;
	CEdit m_edtMinTime;
	CEdit m_edtMinSize;
	CEdit m_chkMaxSize;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboUnalarmColor;
	CComboBox m_cboChildAreaNum;
	CEdit m_edtMainArea;
	CEdit m_edtChildArea1;
	CEdit m_edtChildArea2;
	CEdit m_edtChildArea3;

};

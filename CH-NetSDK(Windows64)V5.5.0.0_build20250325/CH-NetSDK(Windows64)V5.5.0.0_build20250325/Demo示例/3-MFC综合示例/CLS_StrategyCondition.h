#pragma once
#include "./Config/Events/VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_StrategyCondition 对话框

#define CONDITION_COLOUR_NUM_MAX (sizeof(m_cColor)/sizeof(CString))
const CString m_cColor[] = {"red","yellow","blue","green","black","white","purple","gray"};


class CLS_StrategyCondition : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_StrategyCondition)

public:
	CLS_StrategyCondition(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CLS_StrategyCondition();

// 对话框数据
	enum { IDD = IDD_DIALOG_CFG_XML_STRATEGY_CONDITION };

	virtual BOOL OnInitDialog();

	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();


	void UI_UpdateCondition();
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboConditionName;
	afx_msg void OnBnClickedBtnConditionPoint();
	CEdit m_edtArea;
	CComboBox m_cboEventType;
	CSliderCtrl m_sldSensitivity;
	CEdit m_editTime;
	CButton m_chkColor[CONDITION_COLOUR_NUM_MAX];
	afx_msg void OnNMCustomdrawSliderConditionSensitivity(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeComboConditionName();
	afx_msg void OnBnClickedBtnConditionSet();
	afx_msg void OnBnClickedBtnConditionGetColor();

	BOOL SetVCAStatus(bool _bStatus);
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);

	void UI_UpdateStrategy();
	int m_iPointNum;
	CButton m_chkCondition[MAX_STRATEGY_NUM];
	CComboBox m_cboStrategyNo;
	CButton m_cboRuleEnable;
	void ChkConditionRuleTip(int _iIndex);
	afx_msg void OnCbnSelchangeCboConditionRuleno();
	afx_msg void OnBnClickedBtnConditionRuleSet();
	afx_msg void OnBnClickedChkConditionRule1();
	afx_msg void OnBnClickedChkConditionRule2();
	afx_msg void OnBnClickedChkConditionRule3();
	afx_msg void OnBnClickedChkConditionRule4();
	afx_msg void OnBnClickedChkConditionRule5();
	afx_msg void OnBnClickedChkConditionRule6();
	afx_msg void OnBnClickedChkConditionRule7();
	afx_msg void OnBnClickedChkConditionRule8();
};

#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_FallingObjectPage 对话框

class CLS_FallingObjectPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_FallingObjectPage)

public:
	CLS_FallingObjectPage(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CLS_FallingObjectPage();

// 对话框数据
	enum { IDD = IDD_DIALOG_CFG_XML_FALLINGOBJECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持
	void UI_UpdateDialog();

	DECLARE_MESSAGE_MAP()

public:
	int m_iLogonID;
	int m_iChannelNo;
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	
	void UpdateDrawFinishRegionNum(int _iRegoinType);
	void CoordToString();

	virtual BOOL OnInitDialog();
	void DectRegInitial();
	void MaskRegInitial();
	void FirstUpdateDevInfo();
	bool UI_UpdateFallingObjInfo();
	void ShowDectInfoToUI();
	void ShowMaskInfoToUI();
	void DefaultShowRegionInfo(int _iRegionFlag);
	void ShowDetectAreaInfo(ValidRegionFall _tValidReg);
	void ShowMaskAreaInfo(vca_TPolygonEx _tMaskReg);
	void UpdateUIRegoinDectSet();
	void UpdateUIRegoinMaskSet();
	CEdit m_editDectPoint, m_editMaskPoint;
	
	CComboBox m_cbDectDevType, m_cbMaskDevType;
	CComboBox m_cbDectRuleID, m_cbMaskRuleID;
	CComboBox m_cbDectAlarmRule, m_cbMaskAlarmRule;
	CComboBox m_cbDectAlarmNum, m_cbMaskAlarmNum;
	CComboBox m_cbDectTarget, m_cbMaskTarget;
	CComboBox m_cbDectTrack, m_cbMaskTrack;
	CComboBox m_cbDetectRegNum, m_cbMaskNum;
	CComboBox m_cbOsdType;

	CButton m_checkDectEnable, m_checkMaskEnable;
	CButton m_checkInerDectEnab;
	CButton m_btDectDraw, m_btMaskDraw;
	CButton m_btMaskAdd;
	CButton m_btDectDelete, m_btMaskDelete;
	int m_iDectSensitivity, m_iMaskSensitivity;
	XmlFallingObject m_tXmlFallObjInfo;

	afx_msg void OnCbnSelchangeComboDetectRegon1();
	afx_msg void OnCbnSelchangeComboMasknum();
	afx_msg void OnBnClickedDrawMask();
	afx_msg void OnBnClickedDrawDect();
	afx_msg void OnBnClickedButtonSetDectRegion();
	afx_msg void OnBnClickedButtonSetMaskRegion();
	afx_msg void OnCbnSelchangeComboDectReluid();
	afx_msg void OnCbnSelchangeComboReluid();
	afx_msg void OnBnClickedButtonAddmaskreg();
	afx_msg void OnBnClickedButtonDeleteDectreg();
	afx_msg void OnBnClickedButtonDeletemaskreg();
};

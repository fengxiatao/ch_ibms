#pragma once

#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"
// CLS_DlgIrriDataUploadParam dialog

class CLS_DlgIrriDataUploadParam : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgIrriDataUploadParam)

public:
	CLS_DlgIrriDataUploadParam(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgIrriDataUploadParam();

	void Update_IrriDataUploadParam();
	void Update_SpecLightParam();
	void Update_WaterQualityWiper();
	void AddDataToLst(ElevatorStoreyInfo &tElevatorStoreyInfo);
	IrriDataUploadParam m_tParam;
	void UpdateParam();
// Dialog Data
	enum { IDD = IDD_DIALOG_CFG_IRRIDATA_UPLOADPARAM };
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

	CComboBox m_cboDevType;
	CComboBox m_cboSenceID;
	CComboBox m_cboRuleID;
	int m_iTotalNum;
	afx_msg void OnEnChangeEditToltalnum();
	CComboBox m_cboCurrent;
	CComboBox m_cboDataType;
	CComboBox m_cboEnable;
	int m_iInterval;
	afx_msg void OnBnClickedButtonIrriuploadSet();
	afx_msg void OnCbnSelchangeComboCurrent();
	afx_msg void OnCbnSelchangeComboIrridatatype();
	afx_msg void OnCbnSelchangeComboIrridataenable();
	afx_msg void OnEnChangeEditIrridatainterval();
	afx_msg void OnStnClickedStaticDevtype2();
	afx_msg void OnStnClickedStaticBrightness();
	afx_msg void OnStnClickedStaticIrridatainterval5();
	afx_msg void OnBnClickedButtonIrriuploadSet2();
	afx_msg void OnBnClickedButtonIrriuploadUpdate2();
	afx_msg void OnBnClickedButtonIrriuploadSet3();
	afx_msg void OnBnClickedButtonIrriuploadUpdate3();
	afx_msg void OnStnClickedStaticIrridataenable3();
	CComboBox m_cboCtrlType;
	int m_iBrightness;
	afx_msg void OnCbnSelchangeComboIrridataenable3();
	CComboBox m_cboTimeTurn;
	int m_iWiperInterval;
	afx_msg void OnBnClickedButtonIrriuploadUpdate();
	CComboBox m_cboSpecLightDevType;
	CComboBox m_cboSpecLightSceneID;
	CComboBox m_cboSpecLightRuleID;
	CComboBox m_cboWiperDevType;
	CComboBox m_cboWiperSceneID;
	CComboBox m_cboWiperRuleID;
};

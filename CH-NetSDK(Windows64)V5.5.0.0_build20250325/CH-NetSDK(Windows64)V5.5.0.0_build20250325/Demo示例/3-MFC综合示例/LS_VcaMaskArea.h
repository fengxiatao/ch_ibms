
#pragma once
#include ".\Config\Events\VCAEventBasePage.h"
#include "afxwin.h"


// CLS_VcaMaskArea dialog

class CLS_VcaMaskArea : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VcaMaskArea)

public:
	CLS_VcaMaskArea(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaMaskArea();

// dialog data
	enum { IDD = IDD_DLG_VCA_MAKS_AREA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboVcaType;
	CComboBox m_cboSceneId;
	CComboBox m_cboRuleID;
	CComboBox m_cboCurReg;
	CComboBox m_cboRegColor;
	CEdit m_editRegionPoins;
	CButton m_chkEventEnable;
	CButton m_chkShowRule;
	VCAMaskAreaParam m_tVCAMaskAreaParam;

	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();
	afx_msg void OnCbnSelchangeCbo();
	afx_msg void OnBnClickedBtnMaskAreaSet();
	afx_msg void OnBnClickedBtnMaskAreaRegionDraw();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnCbnSelchangeCboMaskAreaCurRegionnum();
	afx_msg void OnCbnSelchangeCboSceneId();
	afx_msg void OnCbnSelchangeCboRuleID();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	int		m_iLogonID;
	int		m_iChannelNo;
	int		m_iStreamNo;
public:
};

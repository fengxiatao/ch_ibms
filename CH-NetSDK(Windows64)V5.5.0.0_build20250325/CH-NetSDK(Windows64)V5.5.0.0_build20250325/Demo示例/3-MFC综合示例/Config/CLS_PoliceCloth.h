#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include <vector>


// CLS_PoliceCloth

class CLS_PoliceCloth : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_PoliceCloth)

public:
	CLS_PoliceCloth(CWnd* pParent = NULL);
	virtual ~CLS_PoliceCloth();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	void GetPoliceClothInfo();				//Obtain the detection parameters of civilian police uniforms
	void ShowValidList(int _iIndex);		//Displays detection area information
	void ShowMaskList(int _iIndex);			//Displays shielded zone information
	BOOL SetVCAStatus(bool _bStatus);		//Set the start and stop of intelligent analysis

	enum { IDD = IDD_DIALOG_CFG_XML_POLICECLOTH };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()

public:
	CComboBox m_cboValidRegNum;				//A valid area number
	CComboBox m_cboMaskRegNum;				//Shielding area number
	CComboBox m_cboRuleNum;					//Rule number
	CComboBox m_cboSceneNum;				//Scene number
	CComboBox m_cboModel;					//mode
	int m_iSensitivity;						//sensitivity
	int m_iMinSize;							//Minimum size
	int m_iMaxSize;							//Maximum size
	CButton m_chkEnabled;					//Whether it works
	CButton m_chkDisplayRule;				//Whether to display alert rules
	CButton m_chkDisplayStat;				//Whether to display the alarm count
	CButton m_chkDisplayTarget;				//Whether to display the target
	CButton m_chkRegionEnabled;				//Detect whether the area is valid
	ValidRgPoliceCloth m_tValidRegion[MAX_REGION_COUNT_CLOTH];	//Valid area coordinates
	vector<vca_TPolygonEx> m_vecMaskRegion;	//Masked area coordinates
	
	afx_msg void OnBnClickedButtonDrawMask();
	afx_msg void OnBnClickedButtonDrawNew();
	afx_msg void OnBnClickedButtonDeleteMask();
	afx_msg void OnBnClickedButtonDrawValid();
	afx_msg void OnBnClickedButtonDeleteValid();
	afx_msg void OnBnClickedButtonSet();
	afx_msg void OnCbnSelchangeComboRegionNum();
	afx_msg void OnCbnSelchangeComboMaskRegionNum();
	afx_msg void OnBnClickedCheckRegionEnabled();
	afx_msg void OnCbnSelchangeComboRulenum();
	afx_msg void OnCbnSelchangeComboPoliceScene();
	afx_msg void OnCbnSelchangeComboModel();

	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
};

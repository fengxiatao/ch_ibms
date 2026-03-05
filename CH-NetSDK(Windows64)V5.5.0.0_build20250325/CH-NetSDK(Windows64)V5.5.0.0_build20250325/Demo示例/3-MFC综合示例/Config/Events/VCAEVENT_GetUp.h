#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


#define  VCA_MAX_GETUP_REGION_NUM		9	//Total number of detection rule areas
#define  REGION_MAX_POINTS_NUM		10	//The maximum number of points in a detection area


// CLS_VCAEVENT_GetUp dialog

class CLS_VCAEVENT_GetUp : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_GetUp)

public:
	CLS_VCAEVENT_GetUp(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_GetUp();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_GETUP };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	
	afx_msg void OnBnClickedBtnGetupSet();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnCbnSelchangeCboGetupCurRegionnum();
	afx_msg void OnBnClickedBtnGetupRegionDraw();
	afx_msg void OnBnClickedBtnGetupLineDraw();

	void UpdateUIText();
	void UpdatePageUI();
	void UpdateDrawFinishRegionNum();

	CComboBox m_cboColor;
	CComboBox m_cboAlarmColor;
	CSliderCtrl m_sldSensitive;
	CComboBox m_cboCurRegionNo;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CEdit m_editRegionPoins;
	CEdit m_editLinePoins;
	CButton m_chkEventEnable;
	CComboBox m_cboDevType;

	VCAParaGetUp m_tVCAParaGetUp;
	afx_msg void OnNMCustomdrawSliderGetupSensitive(NMHDR *pNMHDR, LRESULT *pResult);
};

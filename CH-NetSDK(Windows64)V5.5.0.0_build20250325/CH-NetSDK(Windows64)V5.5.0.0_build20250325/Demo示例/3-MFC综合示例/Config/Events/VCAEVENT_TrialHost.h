#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


// CDlgTrialHost dialog

#define  VCA_ABNORMAL   0
#define  VCA_NEWFIGHT   1
#define  VCA_STILLDECT  2
#define  VCA_BODYTOUCH  3

#define  VCA_MAX_REGION_NUM		8	//Total number of detection rule areas
#define  REGION_MAX_POINTS_NUM		10	//The maximum number of points in a detection area

class CLS_VCAEVENT_TrialHost: public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_TrialHost)

public:
	CLS_VCAEVENT_TrialHost(CWnd* pParent = NULL);   // Standard constructor
	CLS_VCAEVENT_TrialHost(int iType, CWnd* pParent = NULL);
    afx_msg void OnBnClickedButtonSet();
	afx_msg void OnCbnSelchangeCboCurRegionnum();
	afx_msg void OnCbnSelchangeCboCurRegionnum2();
	afx_msg void OnBnClickedBtnRegionDraw();
	afx_msg void OnBnClickedBtnRegionDraw2();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual BOOL OnInitDialog();
	virtual ~CLS_VCAEVENT_TrialHost();

// dialog data
	enum { IDD = IDD_DLG_VCA_TRIALHOST };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_Color;
	CComboBox m_AlarmColor;
	CEdit m_SmallWidth;
	CEdit m_MaxSize;
	CComboBox m_DevType;
	CSliderCtrl m_Sensitive;
	CEdit m_iParam1;
	CEdit m_iParam2;
	CEdit m_iParam3;
	CComboBox m_curRegionNum;
	CEdit m_RegionNum;
	CEdit m_Region_PointNum;
	CEdit m_RegionPoint;
	CComboBox m_CurInvalid_RegionNum;
	CEdit m_InvalidRegionNum;
	CEdit m_InvalidRegionPointNum;
	CEdit m_InvalidPoint;

	int   m_iType;
	int   m_iCmd;
	vca_TPolygonEx m_stDectArea[VCA_MAX_REGION_NUM];
    vca_TPolygonEx m_stInvalidDectArea[VCA_MAX_REGION_NUM];
	
public:
	void  UpdateUIText();
	void  UpdateDrawFinishRegionNum();
	void  UpdatePageUI();
	void  UpdateCommonInfo(VCACommonPara tInfo);

	CButton m_chkEventValid;
	CButton m_chkShowRule;
	CButton m_chkAlarmCount;
	CButton m_chkShowTarget;

};

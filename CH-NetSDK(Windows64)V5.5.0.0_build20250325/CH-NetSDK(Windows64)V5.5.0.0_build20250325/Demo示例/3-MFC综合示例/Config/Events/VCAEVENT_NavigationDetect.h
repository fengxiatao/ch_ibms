#pragma once
#include "VCAEventBasePage.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_VCAEVENT_NavigationDetect dialog

class CLS_VCAEVENT_NavigationDetect : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_NavigationDetect)

public:
	CLS_VCAEVENT_NavigationDetect(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_NavigationDetect();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_NAVIGATION_DETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void OnLanguageChanged();
	void UpdatePageUI();
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);

	CSliderCtrl m_sldSimilar;
	CSliderCtrl m_sldDegree;
	CSliderCtrl m_sldDegree2;
	CSliderCtrl m_sldMinute;
	CSliderCtrl m_sldMinute2;
	CSliderCtrl m_sldSecond;
	CSliderCtrl m_sldSecond2;
	CSliderCtrl m_sldTime;
	CSliderCtrl m_sldMultiple;
	CComboBox m_cboAreaNum;
	CComboBox m_cboLongitude;
	CComboBox m_cboLatitude;
	CComboBox m_cboPointID;

	CButton m_chkNavigation;

	afx_msg void OnBnClickedButtonNavigationSet();
	afx_msg void OnBnClickedButtonDangerareaSet();
	afx_msg void OnBnClickedButton3dlocateSet();
	afx_msg void OnNMCustomdrawSliderNavigationSimilar(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderDangerareaDegree(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderDangerareaMinute(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderDangerareaSecond(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderDangerareaDegree2(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderDangerareaMinute2(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderDangerareaSecond2(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSlider3dlocateTime(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSlider3dlocateMultiple(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedButtonDangerareaSave();

	GpsDangerArea m_tGpsDangerArea;
	
	int GetFuncAbility();
	int GetAnyScene();
	int GetNavigationParam();
	int GetDangerAreaParam();
	int Get3DLocateParam();
	void GetPointsFromString(CString _strPoints, int _iPointNum, TPoint* _poPoint);

	afx_msg void OnBnClickedButtonNavigationDraw();
	afx_msg void OnBnClickedCheckNavigationDetect();
	afx_msg void OnCbnSelchangeComboDangerareaId();
	afx_msg void OnCbnSelchangeComboDangerareaPointId();
	afx_msg void OnBnClickedButtonDangerareaClear();
};

#pragma once

#include "../BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_ItsRadarInfo dialog

class CLS_ItsRadarInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ItsRadarInfo)

public:
	CLS_ItsRadarInfo(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_ItsRadarInfo();

// dialog data
	enum { IDD = IDD_DLG_ITS_RADAR };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void UpdateUIText();
	void UpdatePageUI();
	
	afx_msg void OnBnClickedButtonRadarDeviceSet();
	afx_msg void OnBnClickedButtonRadarEventSet();
	
	int UpdatePageUI_RadarEvent();
	int UpdatePageUI_RadarDevice();



	int UpdatePageUI_RadarStatus();
	CSliderCtrl m_sldRoadWidth;
	CSliderCtrl m_sldMeasureMax;
	CSliderCtrl m_sldMeasureMin;
	CSliderCtrl m_sldCrossSection;
	CSliderCtrl m_sldRadarHeight;
	CSliderCtrl m_sldAngleDelta;
	CSliderCtrl m_sldCoordDelta;
	CSliderCtrl m_sldStartLine;
	CSliderCtrl m_sldStopLine;
	CSliderCtrl m_sldQueueLength;
	CSliderCtrl m_sldCarNumber;
	CSliderCtrl m_sldBeltDriveway;
	CSliderCtrl m_sldLeftLaneNum;
	CSliderCtrl m_sldCross;
	CSliderCtrl m_sldRoad;
	CSliderCtrl m_sldDistace;
	CComboBox m_cboRoadNum;
	CComboBox m_cboEventType;

	afx_msg void OnCbnSelchangeComboRadarEventType();
	afx_msg void OnNMCustomdrawSliderRadarRoadwidth(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarMeasuremax(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarMeasuremin(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarCrossSection(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarBeltDriveway(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarLeftLaneNum(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarHeight(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarAngleDelta(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarCoordDelta(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarJamStartline(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarJamStoptline(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarQueueLength(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarJamCarnum(NMHDR *pNMHDR, LRESULT *pResult);
	CEdit m_edtRadarVersion;
	CEdit m_edtRadarStatus;

	int m_iLogonID;
	int m_iChannelNo;

	CComboBox m_cboRadarID;
	CEdit m_cboRadarIDValue;
	afx_msg void OnBnClickedButtonRadarIdSet();
	int UpdatePageUI_RadarAdvancedPara();
	afx_msg void OnCbnSelchangeCboRadarId();
	afx_msg void OnTimer(UINT_PTR nIDEvent);

	CString m_cstrRadarVersion;
	CString m_cstrRadarStatus;
	afx_msg void OnNMCustomdrawSlider(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarRoad(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRadarDistance(NMHDR *pNMHDR, LRESULT *pResult);

};

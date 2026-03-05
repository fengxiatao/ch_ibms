#pragma once

#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

#define CHK_FACEATTR_ALARM_CUR_NUM	4		//The current number of face attribute alarm function enable switches
// CLS_VCAEVENT_FaceRecNew dialog

class CLS_VCAEVENT_FaceRecNew : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_FaceRecNew)

public:
	CLS_VCAEVENT_FaceRecNew(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_FaceRecNew();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_FACEREC_NEW };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void OnLanguageChanged();
	void UpdateUIText();
	void UpdatePageUI();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnFacerecNewSet();
	afx_msg void OnBnClickedBtnFacerecNewPolygonAreaDraw();
	afx_msg void OnNMCustomdrawSldFacerecNewMaxSize(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSldFacerecNewMinSize(NMHDR *pNMHDR, LRESULT *pResult);

private:
	CButton m_chkEnable;
	CEdit m_edtMaxSize;
	CEdit m_edtMinSize;
	CComboBox m_cboAlgoLevel;
	CComboBox m_cboSensitivity;
	CComboBox m_cboPicScal;
	CEdit m_edtSnapSpace;
	CComboBox m_cboSnapTimes;
	CComboBox m_cboPolygonPointNum;
	CEdit m_edtPolygonArea;
	CSliderCtrl m_sldMaxSize;
	CSliderCtrl m_sldMinSize;
	CButton m_chkTargetBox;
	CComboBox m_cboDevType;

	void UpdateUIFaceDetectParam();
	void UpdateUIPicStreamUploadParam();

public:
	CComboBox m_cboSnapMode;
	CEdit m_iQpvalueBig;
	CEdit m_iQpvalueSmall;
	CSliderCtrl m_iExposureLight;
	CStatic m_dataExpoLight;
	afx_msg void OnNMCustomdrawSlider1(NMHDR *pNMHDR, LRESULT *pResult);
	CButton m_chkIdentEnable;
	CComboBox m_cboPushMode;
	CComboBox m_cboPushLevel;
	CEdit m_edtPushTime;
	CComboBox m_cboSnapType;
	CSliderCtrl m_sldSnapLevel;
	CButton m_chkRuleBox;
	CComboBox m_cboPicSendType;
	CComboBox m_cboPicOsd;
	CComboBox m_cboPicUploadEnable;
	CSliderCtrl m_sldUploadPicQuality;
	CComboBox m_cboFaceFrameEnbale;

	afx_msg void OnCbnSelchangeCboFacerecNewPushMode();
	afx_msg void OnCbnSelchangeCboFacerecNewSnapMode();
	afx_msg void OnNMCustomdrawSldFacerecNewSnapLevel(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnFacerecNewPicuploadSet();
	afx_msg void OnCbnSelchangeCboFacerecNewPicsendType();
	afx_msg void OnNMCustomdrawSldFacerecNewUploadPicquality(NMHDR *pNMHDR, LRESULT *pResult);
	CComboBox m_cboTimeInterval;
	CComboBox m_cboTimeDelay;

    CButton m_chkFaceAttrAlarmEnable[CHK_FACEATTR_ALARM_CUR_NUM];
    CComboBox m_cboDelayPushSpan;
};

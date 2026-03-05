#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include ".\Config\Events\VideoViewForDraw.h"
#include ".\Config\Events\VCAEventBasePage.h"
#define MAX_GAUGE_POINT_TYPE  2
#define MAX_GAUGE_POINT_NUM   30
#define MAX_SCENE_NUM_EX 32

// CLS_GaugeCalib dialog

class CLS_GaugeCalib : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_GaugeCalib)

public:
	CLS_GaugeCalib(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_GaugeCalib();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual BOOL OnInitDialog();
	void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);

// Dialog Data
	enum { IDD = IDD_DIALOG_GAUGECALIB };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	int m_iLogonID;
	int m_iChannelNO;
	int m_iStreamNo;
	/*CLS_VideoViewForDraw* m_pDlgVideoView;*/
	VirtualGaugeCalibResult m_tVirtualGaugeResult[MAX_SCENE_NUM_EX][MAX_GAUGE_POINT_TYPE][MAX_GAUGE_POINT_NUM];
	CComboBox m_cboSceneID;
	CComboBox m_cboNum;
	CComboBox m_cboPointType;
	CEdit m_iDistance;
	CEdit m_edtRTKHeight;
	CEdit m_edtAngle;
	CEdit m_edtWaterLevel;
	CEdit m_edtRiverWidth;
	CComboBox m_cboResult;
	CEdit m_edtPoint;
	CEdit m_edtPointY;
	int m_iBaseNum[MAX_SCENE_NUM_EX];
	int m_iOrdinary[MAX_SCENE_NUM_EX];
public:
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedButton2();
	afx_msg void OnBnClickedButton3();
	void UpdateGaugeCalibParam(VirtualGaugeCalibResult *pInfo);
	void SaveGaugeCalibParam(VirtualGaugeCalibResult *pInfo);
	void UpdateParam();
	afx_msg void OnCbnSelchangeComboScene();
	afx_msg void OnCbnSelchangeComboNum();
	afx_msg void OnCbnSelchangeComboType();
	afx_msg void OnBnClickedButton5();
	CComboBox m_iGeoType;
	CComboBox m_cboLatitude;
	CComboBox m_cboLongtitude;
	CComboBox m_cboOffsetLatitude;
	CComboBox m_cboOffsetLongtitude;
	CEdit m_edtlatiDegree;
	CEdit m_edtlatiMin;
	CEdit m_edtLatiSec;
	CEdit m_edtLongDegree;
	CEdit m_edtLongMin;
	CEdit m_edtLongSec;
	CEdit m_edtOffsetLatitudeDegree;
	CEdit m_edtOffsetLatitudeMin;
	CEdit m_edtOffsetLatitudeSec;
	CEdit m_edtOffsetLaongtitudeDegree;
	CEdit m_edtOffsetLongMin;
	CEdit m_edtOffsetLongSec;
	CEdit m_edtHeight;
	CEdit m_edtOffsetHeight;
};

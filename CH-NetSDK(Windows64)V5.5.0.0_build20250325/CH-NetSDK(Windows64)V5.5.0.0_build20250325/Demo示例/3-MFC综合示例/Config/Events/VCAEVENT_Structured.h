#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include <map>


#define DETECT_TYPE_NUM				 5	   //Number of target detection types for structured algorithms
#define GETVCA_FUNC_ABILITY_PUSHMODE 32    //Get the ability level of the push strategy
#define GETVCA_FUNC_ABILITY_DETECT_TYPE 33 //Get the capability level of the target detection type of the structured algorithm

// CLS_VCAEVENT_Structured dialog

class CLS_VCAEVENT_Structured : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VCAEVENT_Structured)

public:
	CLS_VCAEVENT_Structured(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEVENT_Structured();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_STRUCTURED };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:	
	//virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	void OnLanguageChanged();

	void UpdateUIText();
	void UpdatePageUI();

	void UpdataManCarMixInfo();

	int  ReturnParamDetectType();
	void SetParamDetectType( int _param );
	void GetFuncAbilityPushMode();
	void GetFuncAbilityDetectType();
	void GetPointsFromString(CString _strPoints, int _iPointNum, TPoint* _poPoint);



public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedRadioStructFace();
	afx_msg void OnBnClickedRadioStructCar();
	afx_msg void OnBnClickedRadioStructMancar();
	afx_msg void OnNMCustomdrawSliderStructSensit(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderStructBright(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeComboPushmode();
	afx_msg void OnCbnSelchangeComboSnapMode();

	CComboBox m_comPushMode;
	CComboBox m_comPushLevel;
	CComboBox m_comSnapMode;
	CComboBox m_comSnapTime;
    CComboBox m_comExposureType;
    CComboBox m_comExposureEnable;
    CComboBox m_comPlateAlarmType;

	CSliderCtrl m_sldSensitivity;
	CSliderCtrl m_sldBright;
	
	CEdit m_edtPushLevel;
	CEdit m_edtMinFace;
	CEdit m_edtMinPlate;
	
	CButton m_chkShowTarget;
	CButton m_chkPlateExposureBox;
	CButton m_chkShowRule;

	CButton m_chkDetectType[DETECT_TYPE_NUM];

	CSliderCtrl m_sldFace;
	CSliderCtrl m_sldPerson;
	CSliderCtrl m_sldPlate;
	CSliderCtrl m_sldMotorVehicle;
	CSliderCtrl m_sldNonMotorVehicle;
	CSliderCtrl m_sldBigImage;
	CSliderCtrl m_sldSmallImage;

	CEdit m_edtPointNum;
	CEdit m_edtAreaInfo;

	afx_msg void OnNMCustomdrawSldFaceQuality(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSldPersonQuality(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSldPlateQuality(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSldMotorvehicleQuality(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSldNonmotorvehicleQuality(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedButtonStructSave();
	afx_msg void OnBnClickedBtnStructDraw();

	afx_msg void OnNMCustomdrawSlidStructBigimgQuality(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSlidStructSmallimgQuality(NMHDR *pNMHDR, LRESULT *pResult);

private:
	//下面几个map只是用于方便进行下拉列表到值的转换
	std::map<int, int> m_IndexToDedayTime;
	std::map<int, int> m_DelayTimeToIndex;
	std::map<int, int> m_IndexToTimeSpace;
	std::map<int, int> m_TimeSpaceToIndex;
	std::map<int, int> m_IndexToProvince;
	std::map<int, int> m_ProvinceToIndex;
	
	CComboBox m_comDelayTime;
	CComboBox m_comIntervalTime;
	CComboBox m_comDefProvince;
};

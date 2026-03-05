#pragma once

#include "BasePage.h"
#define SCENE_NUM  32
#define AREA_NUM 128
class CLS_VcaDetectArea : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VcaDetectArea)

public:
	CLS_VcaDetectArea(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VcaDetectArea();

	enum { IDD = IDD_DLG_VCA_DETECT_AREA };
public:
	CComboBox m_cboScenceID;
	CComboBox m_cboAreaType;
	CComboBox m_cboAreaNum;
	CEdit m_edtDetectLoop;
	CEdit m_edtDetectTime;
	CEdit m_edtAreaPoint;
	int m_iReferCount;
	int m_iFlag;//The number of times the marker is called back
	
	DetectAreaResult m_tResult[AREA_NUM];
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()


public:
	void UI_UpdateText();
	void UI_UpdateDialog();
	void GetInfoOnDrawVideo(int* _piPointCount, char* _pcPointsBuf, RECT* _ptRect, int _iDrawType);
	afx_msg void OnBnClickedBtnDetectAreaStartreport();	
	afx_msg void OnBnClickedBtnDetectAreaDetectParamSet();
	afx_msg void OnBnClickedBtnDetectAreaParaGet();
	afx_msg void OnBnClickedBtnDetectAreaRegionDraw();
	afx_msg void OnBnClickedBtnDetectAreaSet();
	afx_msg void OnBnClickedBtnDetectAreaCall();
	afx_msg void OnBnClickedBtnDetectAreaStopreport();
	afx_msg void OnCbnSelchangeComboAreaNum();
	afx_msg void OnBnClickedBtnDetectAreaDell();
	
};

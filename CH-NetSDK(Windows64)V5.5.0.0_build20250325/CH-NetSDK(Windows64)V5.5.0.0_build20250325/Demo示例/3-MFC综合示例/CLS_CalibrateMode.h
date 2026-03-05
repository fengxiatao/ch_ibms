#pragma once
#include "../BasePage.h"
#include "afxwin.h"

// CLS_CalibrateMode dialog

class CLS_CalibrateMode : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_CalibrateMode)

public:
	CLS_CalibrateMode(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_CalibrateMode();

// dialog data
	enum { IDD = IDD_DIG_CFG_VCA_CALIBRATEMODE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
    afx_msg void OnBnClickedButtonSet();
    virtual BOOL OnInitDialog();
    virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
    virtual void OnLanguageChanged(int _iLanguage);
    virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
    virtual void OnHScroll(UINT   nSBCode,   UINT   nPos,   CScrollBar*   pScrollBar);

    void UpdatePageUI(int sceneId = 0);
    void InitPageUI();
    int m_iLogonID;
    int m_iChannelNo;

    CComboBox m_comboScene;
    CComboBox m_comboDeviceID;
    CComboBox m_comboCalibrateMode;
    CSliderCtrl m_sldPan;
    CSliderCtrl m_sldTilt;
    CSliderCtrl m_sldZoom;
    CComboBox m_comboState;
    CComboBox m_comboSceneType;

    afx_msg void OnBnClickedButtonSet3();
    afx_msg void OnCbnSelchangeComboSceneid();
};

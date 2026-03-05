#pragma once
#include "../BasePage.h"
#include "afxwin.h"

// CLS_SceneParam dialog

class CLS_SceneHDScheduleParam : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_SceneHDScheduleParam)

public:
	CLS_SceneHDScheduleParam(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_SceneHDScheduleParam();

// dialog data
	enum { IDD = IDD_DIG_CFG_VCA_SCENE_HDSCHEDULE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
    afx_msg void OnBnClickedButtonSet();
    void  OnHScroll(UINT   nSBCode,   UINT   nPos,   CScrollBar*   pScrollBar);
    virtual BOOL OnInitDialog();
    virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
    virtual void OnLanguageChanged(int _iLanguage);
    virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);

    void UpdatePageUI();
    void InitPageUI();
    int m_iLogonID;
    int m_iChannelNo;

    CComboBox m_comboHdSCheduleScene;
    CComboBox m_comboHdSCheduleType;
    CButton m_chkHdSCheduleEnable;
    CSliderCtrl m_sldHdSCheduleDayId;
    CSliderCtrl m_sldHdSCheduleNightId;

    afx_msg void OnCbnSelchangeComboSceneid();
    afx_msg void OnCbnSelchangeComboTemplatetype();
};

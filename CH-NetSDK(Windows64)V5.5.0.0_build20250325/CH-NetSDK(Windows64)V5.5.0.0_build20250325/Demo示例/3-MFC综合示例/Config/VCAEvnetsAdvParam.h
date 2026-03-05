#pragma once
#include "../BasePage.h"
#include "afxwin.h"

#include "AdvParam/VCAEventAdvParamBase.h"
#include "AdvParam/VCAAdvParam_River.h"
#include "AdvParam/VCAAdvParamWld.h"

typedef enum __tagEEventAdvSel
{
	RIVER_CLEAN_ADV_SEL = 0,
	WLD_ADV_SEL = 1,
	MAX_EVENT_ADV_NUM
}EEventAdvSel;

class CLS_VCAEvnetsAdvParam : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VCAEvnetsAdvParam)

public:
	CLS_VCAEvnetsAdvParam(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCAEvnetsAdvParam();

// dialog data
	enum { IDD = IDD_DLG_CFG_VCA_EVENTS_ADV_PARAM };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
private:
	CLS_VCAEventAdvParamBase* m_plArrEventAdvPage[MAX_EVENT_ADV_NUM];
	int		m_iLogonID;
	int		m_iChannelNo;
	int		m_iStreamNO;
	int		m_iCurrentSel;
	int     m_iScreenID;

protected:
	CComboBox m_cboAdvEventType;
	CComboBox m_cboAdvSceneID;

	//Scene Advanced Parameters
	CComboBox m_cboAdvAnySceneID;
	CComboBox m_cboAdvDevType;
	CComboBox m_cboAdvFocusType;

	CButton  m_chkArithmeticEx[9];
	int		 m_iArithmeticEx;
public:
	void UI_Updata();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);

public:
	virtual BOOL OnInitDialog();
	afx_msg void OnCbnSelchangeCboEventId();	
	afx_msg void OnCbnSelchangeCboAdvSceneid();
private:
	CButton m_chkOpenAlgoDebug;
public:
	afx_msg void OnBnClickedCheckAlgoDebug();
	void UI_UpdateAlgoDebugInfo();

	afx_msg void OnBnClickedBtnSceneadvset();
	void GetAnySceneAdvancedParam();
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	afx_msg void OnCbnSelchangeCmbSceneid();
	afx_msg void OnCbnSelchangeCmbDevtype();
	afx_msg void OnBnClickedButtonMotionDetectionCarAlarmEnable();

	void UI_UpdateMotionDetectionCarParam();

private:
	CButton m_chkMotionDetectionCarAlarmEnable;
};

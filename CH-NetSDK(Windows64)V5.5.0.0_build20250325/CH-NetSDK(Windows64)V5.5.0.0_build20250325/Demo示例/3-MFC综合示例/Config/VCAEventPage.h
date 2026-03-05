#pragma once

#include "../BasePage.h"
#include "afxwin.h"

#include "Events/VCAEventBasePage.h"
#include "Events/VCAEVENT_Abandum.h"
#include "Events/VCAEVENT_Tripwire.h"
#include "Events/VCAEVENT_Perimeter.h"
#include "Events/VCAEVENT_VideoDetect.h"
#include "Events/VCAEVENT_FaceRec.h"
#include "Events/VCAEVENT_Track.h"
#include "Events/VCAEVENT_Crowd.h"
#include "Events/VCAEVENT_Flux.h"
#include "Events/VCAEVENT_LeaveDetect.h"
#include "Events/VCAEVENT_TripwireEx.h"
#include "Events/VCAEVENT_AudioDiagnose.h"
#include "Events/VCAEVENT_MulitTrip.h"
#include "Events/VCAEVENT_Lisence.h"
#include "Events/VCAEVENT_RiverClean.h"
#include "Events/VCAEVENT_Dredge.h"
#include "Events/VCAEVENT_Fight.h"
#include "Events/VCAEVENT_OnDutyDetect.h"
#include "Events/VCAEVENT_IllegalPark.h"
#include "Events/VCAEVENT_GoodsLose.h"
#include "Events/VCAEVENT_Running.h"
#include "Events/VCAEVENT_Hover.h"
#include "Events/VCAEVENT_Parking.h"
#include "Events/VCAEVENT_GoodsDerelict.h"
#include "Events/VCAEVENT_PersonStat.h"
#include "Events/VCAEVENT_VideoDetectNew.h"
#include "Events/VCAEVENT_AudioDiagnoseNew.h"
#include "Events/VCAEVENT_FaceRecNew.h"
#include "Events/VCAEVENT_CrowdNew.h"
#include "Events/VCAEVENT_Protect.h"
#include "Events/VCAEVENT_HeatMap.h"
#include "Events/VCAEVENT_WindowDetevtion.h"
#include "Events/VCAEVENT_WaterLevelDetection.h"
#include "Events/VCAEVENT_SluiceGate.h"
#include "Events/VCAEVENT_ColorTrack.h"
#include "Events/VCAEVENT_Structured.h"
#include "Events/VCAEVENT_GetUp.h"
#include "Events/VCAEVENT_TrialHost.h"
#include "Events/VCAEVENT_SlipUp.h"
#include "Events/VCAEVENT_LeaveBed.h"
#include "Events/VCAEVENT_Sleep.h"
#include "Events/VCAEVENT_ProcuratorateDuty.h"
#include "Events/VCAEVENT_HeightLimit.h"
#include "Events/VCAEVENT_NewDuty.h"
#include "Events/VCAEVENT_HumanDetect.h" 
#include "Events/VCAEVENT_NavigationDetect.h" 
#include "Events/VCAEVENT_VcaPept.h" 
#include "Events/VCAEVENT_ChefHatDetect.h" 
#include "Events/VCAEVENT_ChefMaskDetect.h" 
#include "Events/VCAEVENT_ChefDetect.h"
#include "Events/VCAEVENT_Alone.h" 
#include "Events/VCAEVENT_DeliverGoods.h" 
#include "Events/VCAEVENT_Stranded.h" 
#include "Events/VCAEVENT_SmokeDetect.h"
#include "Events/VCAEVENT_PhoneDetect.h"
#include "Events/VCAEVENT_PermterNew.h"
#include "Events/VCAEVENT_TemDetect.h"
#include "Events/CLS_VCAWaterSpeed.h"
#include "Events/CLS_VcaFireWorkDetect.h"
#include "Events/CLS_VcaSmartMove.h"
#include "Events/VCAEVENT_InquiryTimeout.h"
#include "Events/VCAEVENT_IndoorEBike.h"
#include "Events/VCAEVENT_LeaveDetectEx.h"
// CLS_VCAEventPage dialog
//Put the behavior analysis related algorithms in the front and display them in sequence to increase the ease of use

typedef enum __tagEEventSel
{
	TRIPWIRE_SEL = 0,	//single tripwire
	MULIT_TRIP_SEL,		// double tripwire
	PERIMETER_SEL,		//perimeter detection
	HOVER_SEL,			// hover
	PARKING_SEL,		//parking
	RUNNING_SEL,		//run
	GOODS_DERELICT_SEL,	// stolen item
	GOODS_LOSE_SEL,		// abandoned
	PROTECT_SEL,		//alert
	HEATMAP_SEL,		//heat map
	WATER_LEVEL_DETECTION_SEL, //water level monitoring
	WATER_SPEED,	//water velocity
	RIVER_CLEAN_SEL,			//float detection
	DREDGE_SEL,					//Pirate river sand
	SLUICE_GATE_SEL,			//gate detection
	SEDIMENT_SEL,				// water depth
	VIDEODETECT_NEW_SEL,
	AUDIO_DIAGNOSE_NEW_SEL,
	FACEREC_NEW_SEL,
	ABANDUM_SEL,
	TRACK_SEL,
	CROWD_NEW_SEL,
	FLUX_SEL,
	LEAVE_DETECT_SEL,
	TRIPWIRE_SEL_EX,
	LISENCE_SEL,
	FIGHT_SEL,
	ONDUTY_DETECT_SEL,
	ILLEGAL_PARK_SEL,
	PERSON_STAT_SEL,
	FACEREC_SEL,
	VIDEODETECT_SEL,
	CROWD_SEL,
	AUDIO_DIAGNOSE_SEL,
	WINDOW_DETECTION_SEL,		//window detection
	COLOR_TRACK_SEL,				// color tracking
	STRUCTURED_SEL,				// structured algorithm
	GETUP_SEL,//person gets up
	SLIPUP, // fall
	LEAVEBED, // get out of bed
	SLEEP_POSITION,//Sleeping post
	PROCURATORATE_DUTY,//Single inquiry
	HEIGHT_LIMIT, //Climb high
	NEW_DUTY, //leave the post
	ABNORMALNUM_SEL,
	BODYTOUCH_SEL,
	NEWFIGHT_SEL,
	STILLDECT_SEL,
	HUMAN_DETEC_SEL,//Humanoid detection
	NAVIGATION_DETEC_SEL,//Navigation mark ship detection
	PEPT_SEL,//Oilfield monitoring
	CHEF_HAT_SEL,
	CHEF_MASK_SET,
	CHEF_DETECT_SEL,
	VCA_STRANEDE,
	VCA_ALONE,
	VCAF_DELIVERGOODS,
	SMOKE_SEL,
	PHONE_SEL,
	PERIMETER_NEW_SEL,
	TEMDETECT_SEL,
	FIREDETECT_SEL,
	SMART_MOVE,
	INQUIRY_TIMEOUT,
	INDOOREBIKE,//Indoor Electric Vehicle
	LEAVE_DETECT_SEL_EX,//New departure detection
	TARGET_DETECT,//Target detection
	MAX_EVENT_NUM
}EEventSel;

#define MAX_SCENE_ENABLE_VCA_TYPE		21

class CLS_VCAEventPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VCAEventPage)

public:
	CLS_VCAEventPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VCAEventPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_VCA_EVENTS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	void	EventPageInit();
	CLS_VCAEventBasePage* m_plArrEventPage[MAX_EVENT_NUM];
	int		m_iLogonID;
	int		m_iChannelNo;
	int		m_iStreamNO;
	int     m_iSceneID;


	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUser);


	int		RestartVCALib(int _iLogonID, int _iChannel = -1);
	void	UpdateUIText();
	void	UpdatePageUI();
	bool	IsDigistChannel(int _iChannelNO);
	int     ReturnParamVCA();
	void    SetParamVCA(int _param);
	void    SetVCAStatus(bool _bStatus);
	void    InitVcaList();
public:
	afx_msg void OnBnClickedButtonRuleSet();
//	afx_msg void OnCbnSelendcancelComboEvnetid();

public:
	CComboBox m_cboEventType;
	virtual BOOL OnInitDialog();
	CStatic m_staticEventGroup;
	afx_msg void OnCbnSelchangeComboEvnetid();
	afx_msg void OnDestroy();

	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	CComboBox m_cboRuleID;
	CButton m_chkRuleValid;
	CEdit m_edtRuleName;
	CButton m_chkVCAEnable;
	CButton m_checkVCAHuman;

	vca_TVCAParam *m_pVcaParam;
	afx_msg void OnCbnSelchangeComboRuleid();
	afx_msg void OnBnClickedCheckVcaEnable();
	afx_msg void OnCbnSelchangeComboVcaEnable();
private:
	CComboBox m_cboEnable;
	CButton m_checkVCA[MAX_SCENE_ENABLE_VCA_TYPE];
public:
	CComboBox m_cboSceneID;
	afx_msg void OnBnClickedCheckValidRule();
	afx_msg void OnBnClickedButtonSceneidset();
	afx_msg void OnBnClickedButtonCallScene();
	afx_msg void OnCbnSelchangeComboSceneid();
	CEdit m_cdtSceneName;
	afx_msg void OnBnClickedVcaCheck1();

	afx_msg void OnBnClickedVcaCheck3();
	afx_msg void OnBnClickedVcaCheck4();
	afx_msg void OnBnClickedVcaCheck6();
	afx_msg void OnBnClickedVcaCheck8();
	afx_msg void OnBnClickedVcaCheck9();
	afx_msg void OnBnClickedVcaCheck21();

	int GetSelIndexByEventID(int _iEventID);
	int GetEventIDBySelIndex(int _iSelIndex);
	void CallCurScene();
public:
	CComboBox m_cboDevType;
	afx_msg void OnEnChangeEditRulename();
	BOOL m_bIsVcaSet;
	CButton m_checkHuman;
	CButton m_chkTempDetect;
	CButton m_chkFireWork;
	CButton m_chkTargetOpen;
	CComboBox m_cboMoveType;
	afx_msg void OnCbnSelchangeCboDevtype();
};

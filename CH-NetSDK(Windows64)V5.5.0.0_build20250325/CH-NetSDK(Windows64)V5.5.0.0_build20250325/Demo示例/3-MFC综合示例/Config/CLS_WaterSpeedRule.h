#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"
#define POINTS_GROUP_NUM				8

typedef struct tagPolygonPoint
{
	int iPointLeftUp;
	int ipointRightDown;
}PolygonPoint;

// CLS_WaterSpeedRule dialog

class CLS_WaterSpeedRule : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_WaterSpeedRule)

public:
	CLS_WaterSpeedRule(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_WaterSpeedRule();
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	void InitCombo();

// dialog data
	enum { IDD = IDD_DIALOG_WATERSPEED_RULE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboSceneID;				//scene ID
	CComboBox m_cboRuleID;				//rule ID
	CComboBox m_cboEventIsValid;		// Check if this event is valid
	CComboBox m_cboRuleIsShow;			//whether the rule is displayed
	CComboBox m_cboAeraColor;			// area color
	CComboBox m_cboSpeedSensity;		//sensitivity
	CComboBox m_cboVideoRecord;			//Linked recording
	CComboBox m_cboCruiseAddMode;		// Cruise point add mode
	CComboBox m_cboCruisePoints;		//cruise points
	CComboBox m_cboPolyVerNum;			//Number of vertices in polygon area
	CComboBox m_cboApplyScene;			//application scenario
	CComboBox m_cboAngelInputMode;		//Inclination input mode
	CComboBox m_cboNightFocusMode;		// night focus mode
	CComboBox m_cboSpeedDir;			// enable flow direction setting
	CComboBox m_cboFilterSens;			// filter sensitivity
	CComboBox m_cboLinkAreaType;		// Small scene water level linkage detection area type
	CComboBox m_cboDisplayType;			//Flow rate display type
	CComboBox m_cboDetectMode;			//Flow rate detection mode
	CComboBox m_cboTrackFrameNum;		//Detect the number of tracking frames
	CComboBox m_cboFieldVision;			//The size of the flow velocity detection field of view
	CComboBox m_cboCorrLevel;			// flow rate curve correction level
	CComboBox m_cboStartDir;			// flow velocity detection starting point direction
	CComboBox m_cboLightCtrl;			// fill light strategy during detection
	CComboBox m_cboDeflect;				// detect angle at night
	CComboBox m_cboTimeoutEnable;		//The flow rate detection timeout is cleared to 0 and enabled
	int m_iDetectAltit;					//Initial detection area elevation
	int m_iPointX1;						//The first point coordinate X
	int m_iPointY1;						//The first point coordinate Y
	int m_iPointX2;						//The second point coordinate X
	int m_iPointY2;						//The second point coordinate Y
	int m_iHorizonWidth;				//Length of horizontal right angle side
	int m_iVerticWidth;					//Length of vertical right angle side
	int m_iHypotWidth;					// hypotenuse length
	int m_iDirPointX1;					//The coordinate X of the first point in the flow direction
	int m_iDirPointY1;					//The coordinate Y of the first point in the direction of flow velocity
	int m_iDirPointX2;					//The second point coordinate X in the direction of flow velocity
	int m_iDirPointY2;					//The coordinate Y of the second point in the flow direction
	int m_iSpeedRatio;					//flow coefficient
	int m_iMinSpeed;					//Minimum flow rate limit
	int m_iMaxSpeed;					//Minimum flow rate limit
	int m_iWaterLevelThres;				//water level threshold
	int m_iTimeoutDura;					//Flow rate detection timeout period
	int m_iDwellTime;					// dwell time
	int m_iDecectStep;					//check step size
	int m_iMiddleWidth;					// video centerline distance
	PolygonPoint m_tPolygonPoint[POINTS_GROUP_NUM];
	afx_msg void OnBnClickedButtonWaterspeedGet();
	afx_msg void OnBnClickedButtonWaterspeedSet();
};

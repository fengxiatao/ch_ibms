#pragma once
#include "NetClientTypes.h"
#include "VCAEventBasePage.h"
#include "Common/NeuListCtrl.h"
#include "afxwin.h"
#include "shlwapi.h"
#include "afxcmn.h"
#include "WinDef.h"
#include <map>
#include <vector>
// VCAframDomeCurise dialog
#define COLOR_SET RGB (51,153,255)
#define HeaderFontHEX 13 //The word height of the header
#define NeuRowHeigt 30 //The word width of the header

typedef struct _STRTTime//Save the time data in the list control
{
	CString   strsBengin ;
	CString   strEnd ;
}STRTTime;

enum ECruiseIndex //Column of timed cruise
{
	E_CRUISE_FIRST = 0, //first column
	E_CRUISE_SECOND,
	E_CRUISE_THIRD,		
	E_CRUISE_FOUTH		
};

enum ETimeCruiseIndex //Column cruise by time period
{
	E_TIMECRUISE_FIRST = 0, //first column
	E_TIMECRUISE_SECOND,
	E_TIMECRUISE_THIRD,		
	E_TIMECRUISE_FOUTH,	
	E_TIMECRUISE_FIFTH
};

enum ETimingCruiseByTimeRangeIndex //Timed cruise within the time period
{
	E_TIMINGCRUISEBYTIMERANGE_FIRST = 0, //first column
	E_TIMINGCRUISEBYTIMERANGE_SECOND,
	E_TIMINGCRUISEBYTIMERANGE_THIRD,		
	E_TIMINGCRUISEBYTIMERANGE_FOUTH,	
	E_TIMINGCRUISEBYTIMERANGE_FIFTH,
	E_TIMINGCRUISEBYTIMERANGE_SIXTH,
};

class VCAframDomeCurise : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(VCAframDomeCurise)

public:
	VCAframDomeCurise(CWnd* pParent = NULL);   // standard constructor
	virtual ~VCAframDomeCurise();
	virtual BOOL OnInitDialog();
public:
	CComboBox m_cboChnNo;
	CButton m_btn_Ontime; //Timed cruise
	CButton m_btn_TimeSlot; // Cruise by time period
	CNeuListCtrl m_lst_CuriseInfo; //List of timing cruise information
	CNeuListCtrl m_lst_CuriseTimeInfo;
	CNeuListCtrl m_lstTimingCruiseByTimeRange;
	vector<AnyScene> m_vecAnyScene ;
	vector<SceneTimerCruise> m_vecSceneTimerCruise ;
	vector<SceneTimeRangeCruise> m_vecSceneTimeRangeCruise ;
	vector<SceneTimeRangeCruise> m_vecTimingCruiseByTimeRange;
	CButton m_chkEveryDay;
	void OnBnClickedChkWeek(UINT _uiID);
	int m_iCruiseType;//Determine whether the cruise is timed or cruise by time period
	bool m_bTimeCurise ;//The first click on the cruise interface by time period is false, and it is true in the future to prevent the column header of the list control from being added multiple times
	bool m_bTimingCuriseByTimeRange ;//The timing cruise interface in the first click time period is false, and it is true in the future to prevent the column header of the list control from being added multiple times
	int m_iCount ;//Used to save which row is currently selected
	bool m_bSetControl;//Whether the timed cruise has executed setitmecontrol
	bool m_bTimeSetControl ;//Whether the setitmecontrol has been executed by cruising by time period
	bool m_bTimingByTimeRangeSetControl;//Whether the timed cruise within the time period has executed setitmecontrol
	map<int, STRTTime*> m_mapTimeInfo;
	map<int, STRTTime*> m_mapTimingCruiseTimeInfo;

	int m_iChannelNO;
	void MoveToNewPos(int iOldItemIndex, int iNewItemIndex) ;
	static HWND s_hVCA_CRUISE;
	enum { IDD = IDD_DLG_CFG_VCA_CRUSIE };
private:
	CDateTimeCtrl m_timeSchedStart[MAX_TIMESEGMENT];
	CDateTimeCtrl m_timeSchedStop[MAX_TIMESEGMENT];
	CButton m_chkWeekdays[MAX_DAYS];
	CButton m_chkSchedtime[MAX_TIMESEGMENT];
	void UpdateDlgItem();
	void ClearMapTimeInfo();//Clear the requested MAP memory
	void ClearTimingCruiseMapTimeInfo();//Clear the requested MAP memory
public:
	//virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	afx_msg LRESULT OnRecSelectMessage(WPARAM wParam,LPARAM lParam);

	afx_msg void OnNMRClickListCurise(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMRClickListTimeCurise(NMHDR *pNMHDR, LRESULT *pResult);
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
public:
	int m_iCuriseType;
	int m_iNum;
	int m_iSceneCuriseID;
	CString m_strSceneName;
	int m_iStopTime;
	int m_iBeginTime;
	int m_iEndTime;
private:
	int	ChooseTimeUpdateUI(int _iType);
	DECLARE_MESSAGE_MAP()
	afx_msg void OnBnClickedRadioCuriseTimer();
public:
	afx_msg void OnBnClickedRadioCuriseTimerange();
	afx_msg void OnBnClickedButtonCuriseadd();
	afx_msg void OnBnClickedButtonCurisedel();
	afx_msg void OnBnClickedButtonCuriseSave();
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedButton5();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedRadioTimingCuriseByTimerange();
	afx_msg void OnNMRClickListTimingCuriseByTimerange(NMHDR *pNMHDR, LRESULT *pResult);
	void AddTimerCruiseInfo();
	void AddTimeRangeCruiseInfo();
	void AddTimingCruiseByTimeRangeInfo();
	void DeleteTimerCruiseInfo();
	void DeleteTimeRangeCruiseInfo();
	void DeleteTimingCruiseByTimeRangeInfo();
	void SaveTimerCruiseInfo();
	void SaveTimeRangeCruiseInfo();
	void SaveTimingCruiseByTimeRangeInfo();
	afx_msg void OnHdnBegintrackListCurise(NMHDR *pNMHDR, LRESULT *pResult);
	void OnLanguageChanged( int _iLanguage );
	void UpdateUIText();
};

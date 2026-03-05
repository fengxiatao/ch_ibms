#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"


// CLS_AirLinesPlan dialog

class CLS_AirLinesPlan : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_AirLinesPlan)

public:
	CLS_AirLinesPlan(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_AirLinesPlan();

// dialog data
	enum { IDD = IDD_DLG_CFG_AIRLINESPLAN };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSetAirlines();
	afx_msg void OnBnClickedButtonQuery();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	CString UllToStr(unsigned long long _ullPosition);
	unsigned long long GetLongItudeAndLatitude(int _iIDDlgItem, CString _strNumTemp);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	CComboBox m_cboSceneID;
	CComboBox m_cboAirLineType;
	CEdit m_editPointLen;				//Distance between waypoints
	CEdit m_editStartLong;			//Route start longitude
	CEdit m_editEndLong;				//waypoint end longitude
	CEdit m_editStartLat;				//Waypoint start latitude
	CEdit m_editEndLat;				//waypoint end latitude
	CEdit m_editPage;					//page number
	CComboBox m_cboNumPerPage;		//Number of pieces per page of flight route sound generation information
	CListCtrl m_listLongitudeLatitude;
	int m_iLogonID;
	int m_iChannelNo;

};

#ifndef _WIEGAND_PAGE_H
#define _WIEGAND_PAGE_H

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

#define MAX_NUM_TIME_PERIODS  4
// CLS_Wiegand dialog

class CLS_Wiegand : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_Wiegand)

public:
	CLS_Wiegand(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_Wiegand();

	// Dialog Data
	enum { IDD = IDD_DLG_CFG_WIEGAND };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
    void UI_UpdateDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnBnClickedButtonSet();
	void InitTime();

private:
    void initCboPara(int iType);
	CComboBox m_cboType;
	CComboBox m_cboPara;
    CComboBox m_cboOutData;
	CButton m_btnSet;
	int m_iLogonID;
    int m_iChannelNo;
	CDateTimeCtrl m_dtStartTime[MAX_NUM_TIME_PERIODS];
	CDateTimeCtrl m_dtEndTime[MAX_NUM_TIME_PERIODS];
	CButton m_chkLowPowerEnable;
	CComboBox m_cboWeekDay;
	DayScheduleTimeEx	m_tDevLowPowerSchedule[MAX_WEEK_DAYS][MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT];
public:
    afx_msg void OnCbnSelchangeComboType();
    void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);

	afx_msg void OnBnClickedLowpowerset();
	afx_msg void OnDtnDatetimechangeEndtime(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeCmbweekday();
	afx_msg void OnBnClickedBtnget();
};

#endif

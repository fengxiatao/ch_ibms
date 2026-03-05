#ifndef _FUNC_UNIQUE_ALERT_ENABLE_H
#define _FUNC_UNIQUE_ALERT_ENABLE_H

#include "BasePage.h"
#include "afxwin.h"
#include "afxdtctl.h"
#include "..\Events\VideoViewForDraw.h"


//Featured Alert - Alert enabled state
class CLS_DlgUniqueAlertEnable : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgUniqueAlertEnable)

public:
	CLS_DlgUniqueAlertEnable(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgUniqueAlertEnable();

// dialog data
	enum { IDD = IDD_DLG_CFG_ALERT_ENABLE_STATUS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	int 		m_iLogonID;
	int 		m_iChannelNo;
	int 		m_iStreamNo;
	int			m_iDrawAreaPointNum;

	CLS_VideoViewForDraw* m_pDlgDrawVideoView;


	void	UI_InitDlgWidget();
	void	UI_InitDlgItemText();
	void	UI_UpdateInterfaceParam();

	void	UI_UpdateInfoAlertList();
	void	UI_UpdateInfoAlertChnEnable();
	void	UI_UpdateInfoAlertEventEnabel();
	void	UI_UpdateInfoAlertDrawLine();
	void	UI_UpdateInfoAlertSceneTimeSegment();

	void	UI_UpdataChnEnableWidget();

public:
	CComboBox		m_cboAlertSceneNo;
	CDateTimeCtrl	m_dtpTimeSegment1;
	CDateTimeCtrl	m_dtpTimeSegment2;

public:
	virtual void OnMainNotify(int _iLogonID, int _iWparam, void* _pvLParam, void* _pvUser);
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	afx_msg void OnBnClickedBtnSetAlertChnEnable();
	afx_msg void OnBnClickedChkAlertChnEnable();
	afx_msg void OnCbnSelchangeCboAlertEventScene();
	afx_msg void OnBnClickedBtnSetAlertEventEnable();
	afx_msg void OnBnClickedBtnAlertAnalyzeAreaDraw();
	afx_msg void OnBnClickedRadioAlertEventPerimeter();
	afx_msg void OnBnClickedRadioAlertEventTripwire();
    afx_msg void OnBnClickedRadioAlertEventClimbWall();
    afx_msg void OnBnClickedBtnAlertAnalyzeAreaSet();
	afx_msg void OnBnClickedBtnAlertSceneTimeSegmentSet();
};


#endif

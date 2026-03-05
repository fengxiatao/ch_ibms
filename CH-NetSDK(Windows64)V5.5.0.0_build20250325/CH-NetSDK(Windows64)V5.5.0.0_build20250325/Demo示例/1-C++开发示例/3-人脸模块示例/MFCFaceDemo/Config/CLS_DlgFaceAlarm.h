
#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"
#include "afxcmn.h"

class CLS_DlgFaceAlarm : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceAlarm)

public:
	CLS_DlgFaceAlarm(CWnd* pParent = NULL);
	virtual ~CLS_DlgFaceAlarm();

	enum { IDD = IDD_DLG_CFG_FACE_ALARM };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()

private:
	map<int, int>	m_mapLibkey;
	CComboBox m_cboGroupNumber;

	void			UI_UpdataLibkey();
	void			UI_UpdataAlarmInfo();
	void			UI_UpdataWidget();
	void			UI_UpdataUUID();
	void			UI_ShowAndMoveWidget(CWnd* _pWnd, int _iOffset);
	RECT			UI_GetWndClientRect(CWnd* _pWnd);

public:
	CButton			m_chkEnable;
	CButton			m_chkVcaAlarmEnable;
	CComboBox		m_cboAlarmType;
	CComboBox 		m_cboVcaType;
	CListCtrl		m_lstAlarmLib;
	CSliderCtrl 	m_sldFaceAlarmSimilar;

	void			UI_Init();
	void			UI_UptateData();
	afx_msg void	OnBnClickedBtnAlarmSet();
	afx_msg void	OnCbnSelchangeCboAlarmVcaType();	
	afx_msg void	OnNMCustomdrawSlider1(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeComboFreqGroup();
};

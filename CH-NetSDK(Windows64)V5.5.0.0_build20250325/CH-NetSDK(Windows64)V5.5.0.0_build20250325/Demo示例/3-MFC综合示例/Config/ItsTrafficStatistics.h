#pragma once
#include "../BasePage.h"
#include "afxwin.h"
// Cls_ItsTrafficStatistics dialog

enum EUflowInfoIndex	//Traffic flow query List column information
{
	n_LIST_TRAFFICFLOW_INDEX = 0,
	n_LIST_TRAFFICFLOW_PAGENO,	
	n_LIST_TRAFFICFLOW_LANEID,
	n_LIST_TRAFFICFLOW_ROADNAME,		
	n_LIST_TRAFFICFLOW_TIMERANGE,
	n_LIST_TRAFFICFLOW_FLOW,
	n_LIST_TRAFFICFLOW_HOLDRATE,
	n_LIST_TRAFFICFLOW_SPEED,
	n_LIST_TRAFFICFLOW_DISTANCE,
	n_LIST_TRAFFICFLOW_CARTYPETOTAL,
	n_LIST_TRAFFICFLOW_CARTYPESTR,
	n_LIST_TRAFFICFLOW_CARQUEUELEN,
	n_LIST_TRAFFICFLOW_HEADDISTANCE,
	n_LIST_TRAFFICFLOW_ROOMRATE,
	n_LIST_TRAFFICFLOW_RUNSTATE,
	n_LIST_TRAFFICFLOW_SCENEID
};

class Cls_ItsTrafficStatistics : public CLS_BasePage
{
	DECLARE_DYNAMIC(Cls_ItsTrafficStatistics)

public:
	Cls_ItsTrafficStatistics(CWnd* pParent = NULL);   // Standard constructor
	virtual ~Cls_ItsTrafficStatistics();

// dialog data
	enum { IDD = IDD_DLG_ITS_TRAFFIC_STATIS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();

	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);

private:
	int m_iLogonID;
	int m_iChannelNo;
	void UI_UpdateDialog();
	BOOL UI_UpdateStatis();
	CString GetValueByID(int _iIndex);
public:
	CStatic m_gpTrafficStatis;
	CButton m_chkUseTrafficStatis;
	CStatic m_stcPartOfStatis;
	CEdit m_edtInputPart;
	CButton m_btnSetPart;
	CEdit m_edtShowPart;
	CButton m_btnCleanUp;
	afx_msg void OnBnClickedBtnSetPart();
	afx_msg void OnBnClickedBtnCleanUp();
private:
	CComboBox m_cboRoadID;
public:
	afx_msg void OnCbnSelchangeCboRoadid();
	afx_msg void OnBnClickedButtonFlowQuery();
	CListCtrl m_LisFlowResult;
	CDateTimeCtrl m_DtFlowStartTime;
	CDateTimeCtrl m_DtFlowEndTime;
};

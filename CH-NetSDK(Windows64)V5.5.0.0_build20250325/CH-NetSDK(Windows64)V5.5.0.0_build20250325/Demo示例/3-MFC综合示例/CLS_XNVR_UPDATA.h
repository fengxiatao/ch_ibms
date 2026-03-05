#pragma once
#include "afxwin.h"
#include "../BasePage.h"
#include "afxdtctl.h"


// CLS_XNVR_UPDATA dialog

class CLS_XNVR_UPDATA : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_XNVR_UPDATA)

public:
	CLS_XNVR_UPDATA(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_XNVR_UPDATA();

	// dialog data
	enum { IDD = IDD_DIALOG_XNVR_UPDATA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	DECLARE_MESSAGE_MAP()
public:	
	void SetUIText();
	afx_msg void OnBnClickedButtonIpcautotimingSend();
	afx_msg void OnCbnSelchangeComboIpcautotimingType();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	void UpdateParameter();
	virtual void OnMainNotify(int _iLogonID, int _iWparam, void* _pvLParam, void* _pvUser);
	void UI_UpdateText();
	void OnLanguageChanged( int _iLanguage );
	void XnvrShowWindow(int _iType);

private:
	int  m_iLogonID;
	int  m_iChannelNo;
	CComboBox m_Combox_Type;
	CComboBox m_Combox_Weekday;
	CComboBox m_Combox_Month;
	CComboBox m_Combox_Day;
	CStatic m_static_weekday;
	CStatic m_static_mouth;
	CStatic m_static_day;
	CEdit m_Edit_NewVer;
	CEdit m_Edit_ReleaseData;
	CloudAutoDetect sCloudAutoDetect;
	CDateTimeCtrl m_DateTimeCtrl_hms;
	CEdit m_Edit_ChannelNo;
	CEdit m_Edit_NewVerStat;
	CRect rect;
	CRect rect1;
	CRect rect2;
	CRect rect3;
	CRect rect4;
	CRect rect5;
	CRect rect6;
public:
	CEdit m_Edit_ChnDevInfo_ChannelNo;
	CEdit m_Edit_Mac;
	CEdit m_Edit_FactoryId;
	CEdit m_Edit_BarCode;
	afx_msg void OnDtnDatetimechangeDatetimepickerHms(NMHDR *pNMHDR, LRESULT *pResult);
};

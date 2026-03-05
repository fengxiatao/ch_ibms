#pragma once

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "afxdtctl.h"
#include "Shlwapi.h"
#include "Common/NeuListCtrl.h"
// CLS_UPnpPage dialog

enum EUpnpInfoIndex	//Upnp list column information
{
	n_LIST_UPNP_PORTTYPE = 0,	
	n_LIST_UPNP_OUTERPORT,
	n_LIST_UPNP_OUTERPORT_IP,		
	n_LIST_UPNP_INNERPORT,
	n_LIST_UPNP_STATUS,
	n_LIST_UPNP_OUTERPORT_IPV6
};

enum EUpnpPortType //UpThe port type value in the np list
{
	n_LIST_UPNP_HTTP = 0,
	n_LIST_UPNP_RTSP,
	n_LIST_UPNP_SERVER,
	n_LIST_UPNP_HTTPS,
	n_LIST_UPNP_RTMP
};

enum EUpnpCboMapType //Port mapping method in combo box
{
	n_CBO_MANUAL = 0,
	n_CBO_AUTOMATIC
};

enum EUpnpRadioGroup //radio button value
{
    n_RADIOGROUP_NOTENABLE = 0,
	n_RADIOGROUP_ENABLE,
    n_RADIOGROUP_MANUAL
};

class CLS_UPnpPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_UPnpPage)

public:
	CLS_UPnpPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_UPnpPage();

	void UI_UpdateDialogText();
	void UI_UpdateListInfo(int _iPortType);
	void UI_UpdateListInfoByPortMap(int _iPortType);
	void UI_UpdateEnableSatus();
	void UI_UpdateEditList();
	void UI_UpdateUpnpMapType();
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser);
    virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
// Dialog Data
	enum { IDD = IDD_DLG_CFG_LAN_UPNP };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	virtual BOOL OnInitDialog();

private:
	CNeuListCtrl m_lstNpupList;
	CComboBox m_cboUpnpMapType;
	int m_iLogonID;
	int m_iChannelNo;
	int m_iEnableRadioGroup;

public:
	afx_msg void OnBnClickedRadioCfgLanUpnpEnable();
	afx_msg void OnCbnSelchangeCboCfgLanUpnpMaptype();
	afx_msg void OnBnClickedBtnCfgLanUpnpSave();

};

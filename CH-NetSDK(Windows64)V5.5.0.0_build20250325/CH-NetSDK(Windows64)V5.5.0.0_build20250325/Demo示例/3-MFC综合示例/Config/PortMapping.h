#ifndef _LAN_PORT_MAPPING_H_
#define _LAN_PORT_MAPPING_H_

#include "../BasePage.h"
#include "afxwin.h"
#include "Shlwapi.h"
#include "Common/NeuListCtrl.h"

// CLS_LinkHttp dialog
enum EPortType //Port type value in the port list
{
    n_LIST_HTTP = 0,
    n_LIST_RTSP,
    n_LIST_SCHEDULE,
    n_LIST_SERVER,
    n_LIST_HTTPS,
    n_LIST_RTMP,
    n_LIST_MAX
};

enum EPortInfoIndex	//Port list column information
{
    n_LIST_PORT_PORTTYPE = 0,	
    n_LIST_PORT_VALUE,
};

class CLS_PortMapping : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_PortMapping)

public:
	CLS_PortMapping(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_PortMapping();

	// Dialog Data
	enum { IDD = IDD_DLG_CFG_LAN_PORT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	DECLARE_MESSAGE_MAP()

public:
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	void GetInfoByDialog(RouteNat *_pRouteNatInfo);

private:
	void UI_UpdateDialogText();
	void UI_UpdatePortMapInfo();

public:
	afx_msg void OnBnClickedBtnLanPortSet();
    virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
private:
	int m_iLogonID;
	int m_iChannelNo;
	CComboBox m_cboPortType;
	CButton m_chkLanPortEnalbe;
	CButton m_btnLanPortSet;
	CEdit m_edtLanMapPort;

    CButton   m_btnLanPortSet2;
    CNeuListCtrl m_lstNpupList;
public:
	afx_msg void OnCbnSelchangeComboLanPortPorttype();
    afx_msg void OnBnClickedBtnLanHttpPortSet();
    afx_msg void UI_UpdateListInfo();
};

#endif

#pragma once

#include "BasePage.h"
#include "afxwin.h"

// CLS_4GNormal dialog

class CLS_4GNormal : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_4GNormal)

public:
	CLS_4GNormal(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_4GNormal();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_4G_NORMAL };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
    virtual BOOL OnInitDialog();
	virtual void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo );
	afx_msg void OnBnClickedButton4gcontrol();
	afx_msg void OnBnClickedButton4gnormalGetstatus();
	CComboBox m_cbo4GStatus;
	CComboBox m_cbo4GType;
	CComboBox m_cbo4GControl;

	CButton m_chkInfo;
	CButton m_chkPicInfo;

	int m_iLogonID;
	int m_iChannelNo;
	BOOL IsSupport4GLoad();
	void UI_UpdateDeviceStatus();
	void Update4GUpLoad();
	CEdit m_edt_IMEI;
	CEdit m_edt_ICCID;
};

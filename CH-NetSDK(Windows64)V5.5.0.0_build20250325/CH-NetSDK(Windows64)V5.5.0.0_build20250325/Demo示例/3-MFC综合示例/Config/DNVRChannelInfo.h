#pragma once

#ifndef _DNVR_CHANNEL_INFO_H
#define _DNVR_CHANNEL_INFO_H

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
// CLS_DNVRChannelInfo dialog

class CLS_DNVRChannelInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DNVRChannelInfo)

public:
	CLS_DNVRChannelInfo(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DNVRChannelInfo();
	
// dialog data
	enum { IDD = IDD_DLG_CFG_DNVR_CHANNEL_INFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);

private:
	void UI_UpdateDialog();
	void UI_UpdateConnectState(int _iChannel);


public:

	afx_msg void OnCbnSelchangeCboTextplaniid();
	afx_msg void OnBnClickedBtnTextplanSet();
private:
	CEdit m_edtTextPlancAlias;
	CComboBox m_cboTextPlaniId;
	CEdit m_edtTextPlancText;
	int m_iLogonID;
	int m_iChannel;
	int m_iChannelNo;
public:
	CListCtrl m_lstChannelState;
};

#endif

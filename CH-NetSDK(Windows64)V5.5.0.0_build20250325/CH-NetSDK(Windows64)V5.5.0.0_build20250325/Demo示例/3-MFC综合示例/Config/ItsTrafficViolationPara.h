#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "net_sdk_types.h"

// Cls_ItsTrafficViolationPara dialog
#define TRAFFIC_VIOLATION_PARA_SENSITIVITY 50

class Cls_ItsTrafficViolationPara : public CLS_BasePage
{
	DECLARE_DYNAMIC(Cls_ItsTrafficViolationPara)

public:
	Cls_ItsTrafficViolationPara(CWnd* pParent = NULL);   // standard constructor
	virtual ~Cls_ItsTrafficViolationPara();

// Dialog Data
	enum { IDD = IDD_DLG_ITS_TRAFFIC_VIOLATION_PARA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

public:
	afx_msg void OnBnClickedButtonSet();

private:
	void UI_UpdateDialog();
	void UI_UpdateData();


private:
	CComboBox m_cboChannelNum;
	CEdit	  m_edtCall;
	CEdit	  m_edtNoSeat;

    CComboBox m_cboWhistleSnapshot;
public:
    afx_msg void OnBnClickedButtonSetWhistlesnapshot();
};

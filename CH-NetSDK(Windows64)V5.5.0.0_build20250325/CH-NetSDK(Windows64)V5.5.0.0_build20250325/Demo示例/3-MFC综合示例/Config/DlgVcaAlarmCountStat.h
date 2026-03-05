#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "../BasePage.h"

// DlgVcaAlarmCountStat 对话框

class DlgVcaAlarmCountStat : public CLS_BasePage
{
	DECLARE_DYNAMIC(DlgVcaAlarmCountStat)

public:
	DlgVcaAlarmCountStat(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~DlgVcaAlarmCountStat();

// 对话框数据
	enum { IDD = IDD_DLG_CFG_VCA_ALARM_COUNTSTAT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持
	
	void UI_UpdateDialog();
	void RefreshList();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);

	DECLARE_MESSAGE_MAP()
public:
	CListCtrl m_listAlarmCount;

private:
	int m_iChannelNo;
	//void UpdatePoliceUniform();
	//void UpdateSupervisedPerson();
public:
	afx_msg void OnCbnSelchangeComboAlgType();
};

#pragma once

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "afxdtctl.h"
// CLS_DlgCfgFluxStatistic dialog

typedef struct _tagStaticData{
	int iIn ;
	int iOut ;
	int iInDiff;
	int iOutDiff;
	int	iPass;
	int	iRegion;
	int	iStay;
	int	iAlarmCount;
	int iIsOfflineData;
	int	iNo;
	int iTime;
	int iuTime;
}StaticData;

class CLS_DlgCfgFluxStatistic : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgCfgFluxStatistic)

public:
	CLS_DlgCfgFluxStatistic(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgCfgFluxStatistic();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_VCA_FLUXSTATISTIC };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void UI_UpdateText();
	void OnLanguageChanged(int _iLanguage);
	void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	void ShowList();
private:
	int m_iLogonID;
	int m_iChannelNo;
	CListCtrl m_lstFluxStatistic;
	std::vector<StaticData*> m_vStaticData;
	
public:
	afx_msg void OnNMDblclkListFluxstatisticInfo(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnLvnItemchangedListFluxstatisticInfo(NMHDR *pNMHDR, LRESULT *pResult);
	CDateTimeCtrl m_dtcStartTime;
	CDateTimeCtrl m_dtcEndTime;
	int m_iTotalNum;
	afx_msg void OnBnClickedButtonQuery();
};

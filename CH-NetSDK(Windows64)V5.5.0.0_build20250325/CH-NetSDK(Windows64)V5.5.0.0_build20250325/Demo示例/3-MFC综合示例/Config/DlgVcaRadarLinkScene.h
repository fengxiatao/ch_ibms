#pragma once
#include "../BasePage.h"
#include "../Common/NeuListCtrl.h"
#include "shlwapi.h"
#include "afxwin.h"
#include "afxcmn.h"

#define COLOR_SET RGB (51,153,255)
#define HeaderFontHEX	13		//height of the header
#define NeuRowHeigt	30		//header width

// DlgVcaRadarLinkScene dialog

class DlgVcaRadarLinkScene : public CLS_BasePage
{
	DECLARE_DYNAMIC(DlgVcaRadarLinkScene)

public:
	DlgVcaRadarLinkScene(CWnd* pParent = NULL);   // Standard constructor
	virtual ~DlgVcaRadarLinkScene();

	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);

// dialog data
	enum { IDD = IDD_DLG_CFG_VCA_RADARLINKSCENE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	void OnLanguageChanged(int _iLanguage);
	BOOL SetVCAStatus(bool _bStatus);
	void UI_UpdateText();
	BOOL CheckNumPairRange(int _iStart, int _iEnd);
	void GetVcaRadarLinkSceneParam();

	void AddNewLine();
	void DeleteSelectLine();
	void SaveAndSet();
	RadarLinkSceneArr m_tRadarLinkSceneArr;
	vector<AnyScene> m_vecAnyScene;

public:
	CNeuListCtrl m_listLinkSceneParam;
	afx_msg void OnBnClickedButtonAdd();
	afx_msg void OnBnClickedButtonDelete();
	afx_msg void OnBnClickedButtonSave();
	afx_msg void OnBnClickedButtonGet();
};

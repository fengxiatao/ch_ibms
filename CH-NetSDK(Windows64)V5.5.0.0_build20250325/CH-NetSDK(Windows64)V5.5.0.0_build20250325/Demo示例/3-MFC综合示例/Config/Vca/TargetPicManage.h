#pragma once

#include "../BasePage.h"
#include "afxwin.h"

// CLS_DlgCfgTargetPicMng dialog

class CLS_DlgCfgTargetPicMng : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgCfgTargetPicMng)

public:
	CLS_DlgCfgTargetPicMng(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DlgCfgTargetPicMng();

// dialog data
	enum { IDD = IDD_DLG_CFG_VCA_TARGET_PICTURE_MNG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	int		m_iLogonID;
	int		m_iChannelNo;
	int		m_iStreamNo;

public:
	afx_msg void OnBnClickedBtnTargetpicSave();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void	UpdateUIText();
	void	UpdatePageUI();
	void	SetEditContralDisable(int _iStatus);
	void	SetPicTypeCheck(int _iType);
	
	CEdit m_edtPicWidth;
	CEdit m_edtFaceHeight;
	CEdit m_edtBodyHeight;
	afx_msg void OnBnClickedBtnTargetpicReset();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedRadioCustom();
	afx_msg void OnBnClickedRadioHeadpic();
	afx_msg void OnBnClickedRadioMidbodypic();
	afx_msg void OnBnClickedRadioWholebodypic();
};

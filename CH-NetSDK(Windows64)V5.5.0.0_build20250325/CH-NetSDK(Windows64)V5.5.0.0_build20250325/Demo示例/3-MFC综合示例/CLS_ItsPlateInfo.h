#pragma once
#include "afxcmn.h"
#include "BasePage.h"
#include "afxwin.h"

// CLS_ItsPlateInfo 对话框

#define PLATE_NUM_TOTALCOUNT 10000

typedef enum{
	ITEM_PLATE_SEQ = 0,					//Serial No
	ITEM_PLATE_NUM,						//License plate number
	ITEM_PLATE_INFO_INDEX,				//License plate index
}ITEM_FACE_LIB;

class CLS_ItsPlateInfo : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ItsPlateInfo)

public:
	CLS_ItsPlateInfo(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CLS_ItsPlateInfo();

	virtual BOOL OnInitDialog();

	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	// 对话框数据
	enum { IDD = IDD_DLG_ITS_PLATE_INFO };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	CListCtrl m_lstPlateInfo;
	afx_msg void OnBnClickedBtnPlateSearch();
	afx_msg void OnBnClickedBtnPlateAdd();
	afx_msg void OnBnClickedBtnPlateEdit();
	afx_msg void OnBnClickedBtnPlateDelete();
	afx_msg void OnNMClickListPlateInfo(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMRClickListPlateInfo(NMHDR *pNMHDR, LRESULT *pResult);
	void UI_UpdateUIComboxPage(int _iPageSize,int _iTotalCount);
	void UI_UpdatePlateInfo(int _iPageNo,BOOL _blUpdatePage);
	CComboBox m_cboPlatePage;
	afx_msg void OnCbnSelchangeComboxPlatePage();
	void UI_UpdateUIPlateLib();
	CComboBox m_cboPlateLib;
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnCbnSelchangeComboxPlateLib();
};

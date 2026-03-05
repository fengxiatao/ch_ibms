#pragma once

#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"
#include "afxdtctl.h"
// CLS_DlgXmlThreeDimTrans dialog

class CLS_DlgXmlThreeDimTrans : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_DlgXmlThreeDimTrans)

public:
	CLS_DlgXmlThreeDimTrans(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_DlgXmlThreeDimTrans();

	void UI_UpdateDialogText();
	void UI_UpdateInfo();
	void AddDataToLst(XmlThreeDimTransResult &tXmlThreeDimTransResult);
// Dialog Data
	enum { IDD = IDD_DIALOG_CFG_XML_THREEDIM_TRANS};

	CString IntToCStr(int _iNum);
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	int m_iLogonID;
	int m_iChannelNo;

	XmlThreeDimTransCondition m_tXmlThreeDimTransCondition;
public:

	int m_iSrcChannel;
	int m_iPan;
	int m_iTitl;
	int m_iZoom;
	int m_iDesChannel;
	int m_iCount;
	CComboBox m_cboIndex;
	int m_iX;
	int m_iY;
	int m_iCountResult;
	CListCtrl m_lstTransResult;
	afx_msg void OnEnChangeEditCount();
	afx_msg void OnCbnSelchangeComboNo();
	afx_msg void OnEnChangeEditX();
	afx_msg void OnEnChangeEditY();
	afx_msg void OnBnClickedButtonQuery();
	afx_msg void OnBnClickedCheckSetPtzinfo();
	int m_iSetPtzInfo;
};

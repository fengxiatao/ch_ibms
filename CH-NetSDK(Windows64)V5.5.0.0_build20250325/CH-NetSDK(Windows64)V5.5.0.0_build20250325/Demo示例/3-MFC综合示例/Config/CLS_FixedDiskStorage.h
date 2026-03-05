#pragma once
#include "BasePage.h"
#include "afxwin.h"
#include "afxdtctl.h"
#include "afxcmn.h"

// CLS_FixedDiskStorage dialog

class CLS_FixedDiskStorage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_FixedDiskStorage)

public:
	CLS_FixedDiskStorage(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_FixedDiskStorage();

// dialog data
	enum { IDD = IDD_DLG_CFG_FIXED_DISK_STORAGE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnMainNotify(int _ulLogonID,int _iWparam, void* _iLParam, void* _pvUser);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdateDevPara();
	afx_msg void OnBnClickedButtonRecordSupplement();
	afx_msg void OnBnClickedButtonQuerySupplementProgress();
	afx_msg void OnBnClickedButtonSetRecordchnAliasname();
	void ODevRecSuppCtrlOpt(int _iOpt);
	afx_msg void OnBnClickedButtonAckctrl();
	afx_msg void OnBnClickedButtonRecSuppCtrlPause();
	afx_msg void OnBnClickedButtonRecSuppCtrlReinstate();
	afx_msg void OnBnClickedButtonRecSuppTaskQuery();
private:
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	CDateTimeCtrl m_dtRecordStartTime;
	CDateTimeCtrl m_dtRecordStopTime;
	CEdit m_edtMsIp;
	CEdit m_edtMsPort;
	CEdit m_edtMsId;
	CEdit m_edtMsSubId;
	CEdit m_edtClientMsId;
	CEdit m_edtClientMsIp;
	CEdit m_edtClientMsPort;
	CEdit m_edtHostId;
	CEdit m_edtRecordAlisaName;
	CComboBox m_cboNvrDevChanNo;
	CComboBox m_cboSvrRecordChanNo;
	CEdit m_edtTaskMissionID;
	CComboBox m_cboRecordFileType;
	CComboBox m_cboDiskCroupNo;
	CListCtrl m_lstRecSuppTaskList;
};

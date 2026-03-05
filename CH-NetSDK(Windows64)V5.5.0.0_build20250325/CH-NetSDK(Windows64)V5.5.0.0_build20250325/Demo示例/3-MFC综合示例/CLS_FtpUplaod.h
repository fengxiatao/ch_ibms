#pragma once
#include "BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "Edit_OnlyNumber.h"

// CLS_FtpUplaod dialog
#define DIRECTORY_NUM 4

class CLS_FtpUpload : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_FtpUpload)

public:
	CLS_FtpUpload(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_FtpUpload();

// dialog data
	enum { IDD = IDD_DLG_CFG_FTP_UPLOAD };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	void UpdateUI();
	void UpdateFtpInfo();	
	void UpdateEnable();
	void UpdateDirectroyName(int _iLevel);
	void UpdateDirectroyLevel();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	BOOL CheckFtpInfo(CString& _cstrIP,int& _iPort,CString& _cstrUserName,CString& _cstrPwd,CString& _cstrMsg);
	afx_msg void OnBnClickedButtonSave();
	afx_msg void OnBnClickedCheckEnabled();
	afx_msg void OnBnClickedButtonFtpTest();
	afx_msg void OnCbnSelchangeCmbRoot();
	afx_msg void OnCbnSelchangeCmbFirst();
	afx_msg void OnCbnSelchangeCmbSecond();
	afx_msg void OnCbnSelchangeCmbThird();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);

	CEdit m_edtPort;
	CEdit m_edtUser;
	CEdit m_edtPwd;
	CEdit m_edtRootDirectory;
	CButton m_chkEnable;
	CButton m_chkFaceUpload;
	CComboBox m_cboDataType;
	CComboBox m_cboDirectoryStructure;
	CStatic m_stcDirectory[DIRECTORY_NUM];
	CComboBox m_cboDirectory[DIRECTORY_NUM];	
	CStatic m_stcDirectoryName[DIRECTORY_NUM];
	CEdit_OnlyNumber m_edtDirectoryName[DIRECTORY_NUM];
	CIPAddressCtrl m_IPAddr;
	CEdit m_editPicSeparator;
	CButton m_chkIllegalVideoUpload;
	CComboBox m_cbo_NameIndex;
	CEdit_OnlyNumber m_edt_namedef[3];
	CComboBox m_cbo_NameType[3];
	afx_msg void OnCbnSelchangeCboNameindex();
	afx_msg void OnStnClickedStaticNametype1();
	afx_msg void OnCbnSelchangeCboNametype1();

	int ShowDefine(int iType, CEdit *pEdit);
	afx_msg void OnCbnSelchangeCboNametype2();
	afx_msg void OnCbnSelchangeCboNametype3();
	afx_msg void OnCbnSelchangeCmbForth();
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
	CStatic m_stc_Notify;
	
	CComboBox m_cbEncodeType;
private:
	BOOL SetCurSelByData(CComboBox *_pCombox, int _iData);
};

#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


// CLS_Storage_ANR dialog

class CLS_StorageHotBackup : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_StorageHotBackup)

public:
	CLS_StorageHotBackup(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_StorageHotBackup();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_STORAGE_HOTBACKUP };

	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	void OnMainNotify(int _iLogonID, int _iWparam, void* _pvLParam, void* _pvUser);
	void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);
	void UI_UpdateDialogText();

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	int m_iLogonID;
	int m_iWorkMode;
	CButton m_rdoNormalWorkMode;
	CButton m_rdoHotBackupMode;
	CButton m_chkEnableHotBackup;
	CEdit m_edtHotBackupDevIp;
	CListCtrl m_lstHotBackupHostState;
	CListCtrl m_lstWorkHost;
	CListCtrl m_lstWorkHostState;
	CButton m_btnRefreshWorkDevList;
	CEdit m_edtWorkHostPwd;
public:
	afx_msg void OnTimer(UINT_PTR nIDEvent);//点击刷新后设备定时器，刷新按钮置灰，超时后设置刷新按钮可用
	afx_msg void OnBnClickedRadioNormalWorkmode();
	afx_msg void OnBnClickedRadioHotBackupMode();
	void SetNvrHostWorkMode(int _iWorkMode);
	afx_msg void OnBnClickedCheckEnableHotBackup();
	afx_msg void OnBnClickedButtonAddHotBackupHost();
	afx_msg void OnBnClickedButtonDeleteHotBackupHost();
	afx_msg void OnBnClickedButtonRefreshHotBackupHostState();
	afx_msg void OnBnClickedButtonMoveinWorkHost();
	afx_msg void OnBnClickedButtonRemoveWorkHost();
	afx_msg void OnBnClickedButtonRefreshWorkHostList();
	afx_msg void OnBnClickedButtonRefreshWorkHostState();
	void UpdateNvrHostWorkMode();
	void UpdateWorkHostEnableHotSpare();		//收到参数改变消息后，更新热备机状态
	void UpdateWorkHostList();
	void UpdateWorkHostStateList();				//收到参数改变消息后，更新工作机状态列表
	void UpdateHotBackupHostStateList();		//收到消息改变参数之后，更新工作机对应的热备机列表
	CString GetStringByHostState(int _iHostType, int _iState);
	void UpdateWindowEnable();
};

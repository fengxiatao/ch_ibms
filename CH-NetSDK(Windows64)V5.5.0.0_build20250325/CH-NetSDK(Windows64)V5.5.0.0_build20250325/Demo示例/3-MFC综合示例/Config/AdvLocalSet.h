
#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "../BasePage.h"

#define VCAFPGA_RESERVR 0
#define VCAFPGA_TEMPERATURE 1
#define VCAFPGA_TIME 2

class CLS_AdvLocalSet : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_AdvLocalSet)

public:
	CLS_AdvLocalSet(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_AdvLocalSet();

// dialog data
	enum { IDD = IDD_DLG_CFG_ADVANCE_LOCAL };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
//control variable
public:
	CComboBox m_cboLogonMode;
	CComboBox m_cboDemoUseRule;
	CComboBox m_cboDemoUseMode;
	CComboBox m_cboVcaFpgaQueryInfo;
	CComboBox m_cboVcaFpga;
	CComboBox m_cboIpVersion;

	CButton m_chkVideoRenderD3D;
	CButton m_chkVideoRenderDraw;

//Member variables
private:
	int m_iLogonID;
	int m_iChannelNo;

// function function
public:
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnMainNotify(int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);

	void UI_UpdateDialog();
	void LoadSdkWorkMode();
	void LoadDemoUseRule();
	void LoadSdkVideoMode();
	void SaveSdkVideoMode(CString sKey,int sValue);

	void UI_UpdateVcaFpga();
	void UI_UpdateVcaFpgaQueryInfo();
	void SetHwDecodeParam(int _iDecodeType);
	void IsShowRenderWindow(int _iCmd);
//system information
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedBtnLogonMode();
	afx_msg void OnBnClickedButtonSetDemoUseRule();
	afx_msg void OnBnClickedBtnVcafpgaSet();
	afx_msg void OnBnClickedBtnVcafpgaQueryinfoSet();
	afx_msg void OnCbnSelchangeCboVcafpgaQueryinfo();
	afx_msg void OnCbnSelchangeCboVcafpga();
	afx_msg void OnCbnSelchangeComboIpVersion();
	afx_msg void OnBnClickedCheckPrivateSdec();
	afx_msg void OnBnClickedCheckFfmpegSdec();
	afx_msg void OnBnClickedCheckD3dRender();
	afx_msg void OnBnClickedCheckDrawRender();
private:
	CButton m_chkWriteLog;
	CComboBox m_cboLogfileLevel;
	CComboBox m_cboTerminalLevel;
	CButton m_chkVideoRenderD3D11;
public:
	afx_msg void OnBnClickedButtonSetLogLevel();
	afx_msg void OnBnClickedCheckD3d11Render();

	CComboBox m_cboHWDecodeType;
	afx_msg void OnCbnSelchangeComboHwdecodetype();
};

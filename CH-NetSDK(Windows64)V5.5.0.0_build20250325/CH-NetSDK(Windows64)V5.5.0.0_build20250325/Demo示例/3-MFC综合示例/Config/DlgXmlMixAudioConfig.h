#pragma once

#include "BasePage.h"
#include "afxcmn.h"
#include "afxwin.h"

#define OUTPUTNAME_NUM			6
#define SRCNAME_NUM				17

// DlgXmlMixAudioConfig 对话框

class DlgXmlMixAudioConfig : public CLS_BasePage
{
	DECLARE_DYNAMIC(DlgXmlMixAudioConfig)

public:
	DlgXmlMixAudioConfig(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~DlgXmlMixAudioConfig();

	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	void InitCombox();
	void WhetherSend(int _iIndex);
	void ChangEnable(int _iIndex);
	void LoadCurrentState(int _iIndex);

	void GetMixAudioConfig();

// 对话框数据
	enum { IDD = IDD_DIALOG_CFG_XML_MIXAUDIOCONFIG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	// 显示获取结果的列表
	CListCtrl m_listGetMixAudioConfig;

	// 选择混音输出名称的下拉框
	CComboBox m_cboOutputName;

	// 选择混音输入源使能状态的下拉框
	CComboBox m_cboList[SRCNAME_NUM];

	// 控制是否发送混音输入源配置的复选框
	CButton m_chkList[SRCNAME_NUM];

private:
	// BOOL数组 格式:[混音输出名称][混音输入源] 含义:是否在设置协议中发送当前组合
	BOOL m_blSendArr[OUTPUTNAME_NUM][SRCNAME_NUM];
	
	// 存储配置参数 用于设置设备参数
	XmlMixAudioConfigList m_tConfigList;
public:
	// combox（表示混音输入源“使能”）的选中项改变事件
	afx_msg void OnCbnSelchangeComboSrcLocal();
	afx_msg void OnCbnSelchangeComboSrcRemoteall();
	afx_msg void OnCbnSelchangeComboSrcRemote1();
	afx_msg void OnCbnSelchangeComboSrcRemote2();
	afx_msg void OnCbnSelchangeComboSrcRemote3();
	afx_msg void OnCbnSelchangeComboSrcRemote4();
	afx_msg void OnCbnSelchangeComboSrcMicall();
	afx_msg void OnCbnSelchangeComboSrcMic1();
	afx_msg void OnCbnSelchangeComboSrcMic3();
	afx_msg void OnCbnSelchangeComboSrcMic5();
	afx_msg void OnCbnSelchangeComboSrcMic7();
	afx_msg void OnCbnSelchangeComboSrcProof();
	afx_msg void OnCbnSelchangeComboSrcExp1();
	afx_msg void OnCbnSelchangeComboSrcExp2();
	afx_msg void OnCbnSelchangeComboSrcExp3();
	afx_msg void OnCbnSelchangeComboSrcExp4();
	afx_msg void OnCbnSelchangeComboSrcExpothr();
	afx_msg void OnBnClickedCheckSrcLocal();

	// checkBox（表示混音输入源“是否发送”）当前状态发生改变的事件
	afx_msg void OnBnClickedCheckSrcRemoteall();
	afx_msg void OnBnClickedCheckSrcRemote1();
	afx_msg void OnBnClickedCheckSrcRemote2();
	afx_msg void OnBnClickedCheckSrcRemote3();
	afx_msg void OnBnClickedCheckSrcRemote4();
	afx_msg void OnBnClickedCheckSrcMicall();
	afx_msg void OnBnClickedCheckSrcMic1();
	afx_msg void OnBnClickedCheckSrcMic3();
	afx_msg void OnBnClickedCheckSrcMic5();
	afx_msg void OnBnClickedCheckSrcMic7();
	afx_msg void OnBnClickedCheckSrcProof();
	afx_msg void OnBnClickedCheckSrcExp1();
	afx_msg void OnBnClickedCheckSrcExp2();
	afx_msg void OnBnClickedCheckSrcExp3();
	afx_msg void OnBnClickedCheckSrcExp4();
	afx_msg void OnBnClickedCheckSrcExpothr();
	afx_msg void OnBnClickedButtonSetMixaudio();
	afx_msg void OnCbnSelchangeComboOutputName();
};

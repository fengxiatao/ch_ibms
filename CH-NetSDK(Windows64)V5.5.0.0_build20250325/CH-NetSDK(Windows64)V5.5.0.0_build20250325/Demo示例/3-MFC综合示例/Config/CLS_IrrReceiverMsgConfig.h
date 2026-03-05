#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
//#include "afxcmn.h"

#define DELETE_MESSAGE_CONFIG  2

// CLS_IrrReceiverMsgConfig 对话框

class CLS_IrrReceiverMsgConfig : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_IrrReceiverMsgConfig)

public:
	CLS_IrrReceiverMsgConfig(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CLS_IrrReceiverMsgConfig();
	virtual BOOL OnInitDialog();

// 对话框数据
	enum { IDD = IDD_DIALOG_IRRRECEIVERMSG_CONFIG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持
	void AnsiToUTF8( const char* _pstrIn,CString &_strOut);
	DECLARE_MESSAGE_MAP()
public:
	CListCtrl m_lstMessage;
	CComboBox m_ControlCombox;
	CComboBox m_TypeCombox;
	afx_msg void OnBnClickedButtonSearch();
	afx_msg void OnBnClickedButtonSetaudio();
	afx_msg void OnBnClickedButtonSave();
	afx_msg void OnCbnCloseupComboControl();
};

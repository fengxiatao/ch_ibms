#ifndef _ACTIVE_DDNS_WINDOW_H
#define _ACTIVE_DDNS_WINDOW_H

#include "BasePage.h"
#include "afxwin.h"

// CLS_ActiveDdnsPage 对话框

class CLS_ActiveDdnsPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ActiveDdnsPage)

public:
	CLS_ActiveDdnsPage(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CLS_ActiveDdnsPage();

// 对话框数据
	enum { IDD = IDD_DLG_MNG_DDNS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateDialogText();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonRegisterDdns();
	afx_msg void OnBnClickedButtonTestDdns();
private:
	CEdit m_edtDdnsSerDomainIp;
	CEdit m_edtDdnsSerWanPort;
	CEdit m_edtLocalLanIp;
	CEdit m_edtLocalDomainName;
	CEdit m_edtLocalFactoryId;
	CEdit m_edtLocalTcpWanPort;
	CEdit m_edtLocalHttpPort;
	CEdit m_edtLocalRtmpPort;
};

#endif


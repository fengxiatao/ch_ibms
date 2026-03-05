#pragma once
#include "BasePage.h"


// TargetDetect 对话框

class CLS_TargetDetect : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_TargetDetect)

public:
	CLS_TargetDetect(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CLS_TargetDetect();
	void UI_UpdatePage();

// 对话框数据
	enum { IDD = IDD_DLG_CFG_TARGETDETECT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持
	
	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
	void UpdateUIText();
	CButton m_chkSwitch;
	CButton m_chkDataTransferSwitch;
	afx_msg void OnBnClickedCheckDataTranfer2();
};

#ifndef _RTMP_PAGE_H_
#define _RTMP_PAGE_H_

#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"

class CLS_RtmpPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_RtmpPage)

public:
	CLS_RtmpPage(CWnd* pParent = NULL);   
	virtual ~CLS_RtmpPage();

	enum { IDD = IDD_DLG_CFG_RTMP };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);   

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateRtmpInfo(int iRtmpChn = 0);
	void UI_UpdateDialog();
    void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
private:
	int m_iLogonID;
	int m_iChannelNo;
	CComboBox m_cboPushStreamType; //Code stream type 1 main code stream 2 secondary code stream 3 three code stream

public:
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);

	afx_msg void OnBnClickedBtnRtmpSet();
	CButton m_chkLiveEnable;
	CEdit m_edtLiveAddr;
	CEdit m_edtAuthKey;
    
    CEdit m_edtRtmpNo; // 0~15
    CEdit m_edtRtmpChn;
    CEdit m_edtRtmpSndTmOut;
    CEdit m_edtRtmpRcvTmOut;
    CComboBox m_cboType;   // 1:Custom, 2:Non-custom
    CEdit m_edtPort;       // 1935
    CEdit m_edtUserName;
    CEdit m_edtUserPassword;

    afx_msg void OnBnClickedBtnRtmpClientLinkstate();
};

#endif

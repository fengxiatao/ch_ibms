#ifndef _CLS_GATPAGE_H_
#define _CLS_GATPAGE_H_

#include "../BasePage.h"
#include "afxwin.h"
#include "Shlwapi.h"
#include "Common/NeuListCtrl.h"


enum EListIndex	//port List column information
{
    n_LIST_NO = 0,	
    n_LIST_ID,
};

class CLS_GATPage : public CLS_BasePage
{
    DECLARE_DYNAMIC(CLS_GATPage)

public:
    CLS_GATPage(CWnd* pParent = NULL);   // standard constructor
    virtual ~CLS_GATPage();

    // Dialog Data
    enum { IDD = IDD_DLG_CFG_GAT };

protected:
    virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
    DECLARE_MESSAGE_MAP()

public:
    virtual BOOL OnInitDialog();
    virtual void OnLanguageChanged(int _iLanguage);
    virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);

private:
    void UI_UpdateDialogText();

public:
    virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
private:
    int m_iLogonID;
    int m_iChannelNo;

    CButton m_chkEnalbe;
    CEdit m_edtIpAddress;
    CEdit m_edtPort;
    CEdit m_edtDeviceId;
    CEdit m_edtUserName;
    CEdit m_edtPassword;
    CEdit m_edtHeartBeatInterval;
    CEdit m_edtHeartBeatTime;
    CEdit m_edtPlaceCode;
    CEdit m_edtLongitude;
    CEdit m_edtLatitude;
    CButton m_chkTimingEnable;
    CEdit m_edtRetryTime;
    CEdit m_edtRetryInterval;
    CEdit m_edtTimingInterval;
    CEdit m_edtConfFileNo;
    CEdit m_edtChannelCount;

    CButton m_btnOnline;
    CButton m_btnSet;
    CNeuListCtrl m_lstNpupList;
public:
    afx_msg void UI_UpdateListInfo();
    afx_msg void OnBnClickedButtonSet();
    afx_msg void OnBnClickedButtonOnline();
    afx_msg void OnBnClickedButtonAddRow();
};

#endif

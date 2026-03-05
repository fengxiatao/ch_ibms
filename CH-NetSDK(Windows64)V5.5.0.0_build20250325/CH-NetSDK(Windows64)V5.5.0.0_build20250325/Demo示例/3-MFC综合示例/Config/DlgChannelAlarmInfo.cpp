// CLS_DlgChannelAlarmInfo.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgChannelAlarmInfo.h"


// CLS_DlgChannelAlarmInfo dialog

IMPLEMENT_DYNAMIC(CLS_DlgChannelAlarmInfo, CDialog)

CLS_DlgChannelAlarmInfo::CLS_DlgChannelAlarmInfo(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgChannelAlarmInfo::IDD, pParent)
{

}

CLS_DlgChannelAlarmInfo::~CLS_DlgChannelAlarmInfo()
{
}

void CLS_DlgChannelAlarmInfo::DoDataExchange(CDataExchange* pDX)
{
    CDialog::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_LIST_CHANNEL_ALARMINFO, m_lstAlarmInfo);
    DDX_Control(pDX, IDC_LIST_CHANNEL_ALARMNOTIFY, m_lstAlarmNotify);
}


BEGIN_MESSAGE_MAP(CLS_DlgChannelAlarmInfo, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_REFRESH, &CLS_DlgChannelAlarmInfo::OnBnClickedButtonRefresh)
    ON_BN_CLICKED(IDC_BUTTON_REFRESH_ALARMNOTIFY, &CLS_DlgChannelAlarmInfo::OnBnClickedButtonRefreshAlarmnotify)
END_MESSAGE_MAP()


// CLS_DlgChannelAlarmInfo message handlers

CString CLS_DlgChannelAlarmInfo::IntToCStr(int _iNum)
{
	CString strNum;
	strNum.Format(_T("%d"), _iNum);
	return strNum;
}
void CLS_DlgChannelAlarmInfo::OnBnClickedButtonRefresh()
{
	// TODO: Add your control notification handler code here
	GetAlarmInfo();
}

BOOL CLS_DlgChannelAlarmInfo::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	m_lstAlarmInfo.SetExtendedStyle(m_lstAlarmInfo.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);

	m_lstAlarmInfo.InsertColumn(0, GetTextByLan(_T("序号"), _T("Seq")), LVCFMT_CENTER, 40);
	m_lstAlarmInfo.InsertColumn(1, GetTextByLan(_T("设备类型"), _T("DeviceType")), LVCFMT_CENTER, 80);
	m_lstAlarmInfo.InsertColumn(2, GetTextByLan(_T("报警状态"), _T("AlarmState")), LVCFMT_CENTER, 80);
	m_lstAlarmInfo.InsertColumn(3, GetTextByLan(_T("报警时间"), _T("AlarmTime")), LVCFMT_CENTER, 160);
	m_lstAlarmInfo.InsertColumn(4, GetTextByLan(_T("报警类型"), _T("AlarmType")), LVCFMT_CENTER, 80);
	m_lstAlarmInfo.InsertColumn(5, GetTextByLan(_T("场景ID"), _T("SceneID")), LVCFMT_CENTER, 80);
	m_lstAlarmInfo.InsertColumn(6, GetTextByLan(_T("规则ID"), _T("RuleID")), LVCFMT_CENTER, 80);
	m_lstAlarmInfo.InsertColumn(7, GetTextByLan(_T("事件类型"), _T("EventType")), LVCFMT_CENTER, 80);
	m_lstAlarmInfo.InsertColumn(8, GetTextByLan(_T("值"), _T("Value")), LVCFMT_CENTER, 80);



    m_lstAlarmNotify.SetExtendedStyle(m_lstAlarmNotify.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);

    m_lstAlarmNotify.InsertColumn(0, GetTextByLan(_T("序号"), _T("Seq")), LVCFMT_CENTER, 40);
    m_lstAlarmNotify.InsertColumn(1, GetTextByLan(_T("通道号"), _T("ChannelNo")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(2, GetTextByLan(_T("报警类型"), _T("AlarmType")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(3, GetTextByLan(_T("报警状态"), _T("AlarmState")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(4, GetTextByLan(_T("报警时间"), _T("AlarmTime")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(5, GetTextByLan(_T("参数1"), _T("Para1")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(6, GetTextByLan(_T("参数2"), _T("Para2")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(7, GetTextByLan(_T("参数3"), _T("Para3")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(8, GetTextByLan(_T("参数4"), _T("Para4")), LVCFMT_CENTER, 80);
    m_lstAlarmNotify.InsertColumn(9, GetTextByLan(_T("参数5"), _T("Para5")), LVCFMT_CENTER, 80);

   SetDlgItemText(IDC_STATIC_ALARM_NOTIFY_TITLE,GetTextByLan(_T("实时报警消息"), _T("Alarm Notify")));

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_DlgChannelAlarmInfo::GetAlarmInfo()
{

	ChannelCurAlarmInfo *ptChannelCurAlarmInfo = new ChannelCurAlarmInfo;
	memset(ptChannelCurAlarmInfo,0x00,sizeof(ChannelCurAlarmInfo));
	ptChannelCurAlarmInfo->iCurNoVcaAlarmInfoStructSize = sizeof(ChannelNoVcaAlarmInfo);
	ptChannelCurAlarmInfo->iCurVcaAlarmInfoStructSize = sizeof(ChannelCurAlarmInfo);

	int iReturn = 0;

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GETCURALARMINFO, m_iChannelNo, ptChannelCurAlarmInfo, sizeof(ChannelCurAlarmInfo),&iReturn);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d,%d,NET_CLIENT_GETCURALARMINFO)",m_iLogonID,m_iChannelNo);
		AddDataToLst(*ptChannelCurAlarmInfo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%d,NET_CLIENT_GETCURALARMINFO)",m_iLogonID,m_iChannelNo);
	}
	SAFE_DELETE(ptChannelCurAlarmInfo);
}

void CLS_DlgChannelAlarmInfo::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
	GetAlarmInfo();
}

void CLS_DlgChannelAlarmInfo::OnLanguageChanged( int _iLanguage)
{
	
}

void CLS_DlgChannelAlarmInfo::AddDataToLst(ChannelCurAlarmInfo &tChannelCurAlarmInfo)
{
	m_lstAlarmInfo.DeleteAllItems();
	int iSeq = 0;
	for (int i = 0; i < tChannelCurAlarmInfo.tChannelNoVcaAlarmInfo.iCount; i++)
	{

		CurNoVcaAlarmInfo &tCurNoVcaAlarmInfo = tChannelCurAlarmInfo.tChannelNoVcaAlarmInfo.tCurAlarmInfo[i];
		m_lstAlarmInfo.InsertItem(iSeq, (LPCTSTR)IntToCStr(iSeq));
		m_lstAlarmInfo.SetItemText(iSeq, 1, (LPCTSTR)IntToCStr(tCurNoVcaAlarmInfo.iDeviceType));
		m_lstAlarmInfo.SetItemText(iSeq, 2, (LPCTSTR)IntToCStr(tCurNoVcaAlarmInfo.iAlarmState));
		m_lstAlarmInfo.SetItemText(iSeq, 3, (LPCTSTR)tCurNoVcaAlarmInfo.cAlarmTime);
		m_lstAlarmInfo.SetItemText(iSeq, 4, (LPCTSTR)IntToCStr(tCurNoVcaAlarmInfo.iAlarmType));
		m_lstAlarmInfo.SetItemText(iSeq, 7, (LPCTSTR)IntToCStr(tCurNoVcaAlarmInfo.iEventType));

		iSeq++;
	}
	for (int i = 0; i < tChannelCurAlarmInfo.tChannelVcaAlarmInfo.iCount; i++)
	{

		CurVcaAlarmInfo &tCurVcaAlarmInfo = tChannelCurAlarmInfo.tChannelVcaAlarmInfo.tCurAlarmInfo[i];
		m_lstAlarmInfo.InsertItem(iSeq, (LPCTSTR)IntToCStr(iSeq));
		m_lstAlarmInfo.SetItemText(iSeq, 1, (LPCTSTR)IntToCStr(tCurVcaAlarmInfo.iDeviceType));
		m_lstAlarmInfo.SetItemText(iSeq, 2, (LPCTSTR)IntToCStr(tCurVcaAlarmInfo.iAlarmState));
		m_lstAlarmInfo.SetItemText(iSeq, 3, (LPCTSTR)tCurVcaAlarmInfo.cAlarmTime);
		m_lstAlarmInfo.SetItemText(iSeq, 4, (LPCTSTR)IntToCStr(tCurVcaAlarmInfo.iAlarmType));
		m_lstAlarmInfo.SetItemText(iSeq, 5, (LPCTSTR)IntToCStr(tCurVcaAlarmInfo.iSceneID));
		m_lstAlarmInfo.SetItemText(iSeq, 6, (LPCTSTR)IntToCStr(tCurVcaAlarmInfo.iRuleID));
		m_lstAlarmInfo.SetItemText(iSeq, 7, (LPCTSTR)IntToCStr(tCurVcaAlarmInfo.iEventType));
		m_lstAlarmInfo.SetItemText(iSeq, 8, (LPCTSTR)IntToCStr(tCurVcaAlarmInfo.iJudgeBehaviorAnalysis));

		iSeq++;
	}
}

void CLS_DlgChannelAlarmInfo::OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser)
{
    //报警条数过多以后清空
    if (m_lstAlarmNotify.GetItemCount()>100000)
    {
        m_lstAlarmNotify.DeleteAllItems();
    }
    switch(_iAlarmType)
    {
    case CALLBACK_ALARMTYPE_EXCEPTION:
    case CALLBACK_ALARMTYPE_VCAINFO:
    case CALLBACK_ALARMTYPE_VCAINFOEX:
        //TODO 需要单独解析
        break;
    default:
        CommonAlarmNotify *tInfo = (CommonAlarmNotify*)_pInfo;
        if (NULL != tInfo)
        {
            int iIndex = m_lstAlarmNotify.GetItemCount();
            m_lstAlarmNotify.InsertItem(iIndex, IntToStr(iIndex).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 1, IntToStr(tInfo->iChanNo).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 2, IntToStr(_iAlarmType).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 3, IntToStr(tInfo->iState).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 4, tInfo->cAlarmTime);
            m_lstAlarmNotify.SetItemText(iIndex, 5, IntToStr(tInfo->iAddlPara1).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 6, IntToStr(tInfo->iAddlPara2).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 7, IntToStr(tInfo->iAddlPara3).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 8, IntToStr(tInfo->iAddlPara4).c_str());
            m_lstAlarmNotify.SetItemText(iIndex, 9, tInfo->cAddlPara5);
        }
        break;
    }
}
void CLS_DlgChannelAlarmInfo::OnBnClickedButtonRefreshAlarmnotify()
{
    // TODO: Add your control notification handler code here
    m_lstAlarmNotify.DeleteAllItems();
}

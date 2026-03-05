#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertAlarmMessage.h"

enum enumAlertAlarmInfoListColumn
{
	eu_Column_CHN = 0,
	eu_Column_STATE,
	eu_Column_EventType,
	eu_Column_CheckType,
	eu_Column_TargetId,
	eu_Column_TargetType,
	eu_Column_Position,
	eu_Column_Level,
};

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertAlarmMessage, CDialog)

CLS_DlgUniqueAlertAlarmMessage::CLS_DlgUniqueAlertAlarmMessage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertAlarmMessage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgUniqueAlertAlarmMessage::~CLS_DlgUniqueAlertAlarmMessage()
{
}

void CLS_DlgUniqueAlertAlarmMessage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_ALERT_ALARM_INFO, m_lstAlarmInfo);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertAlarmMessage, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_ALERT_ALARM_CLEAR, &CLS_DlgUniqueAlertAlarmMessage::OnBnClickedBtnAlertAlarmClear)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertAlarmMessage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	return TRUE;
}

void CLS_DlgUniqueAlertAlarmMessage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
	}
}

void CLS_DlgUniqueAlertAlarmMessage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (m_iLogonID == _iLogonID && m_iChannelNo == _iChannelNo && m_iStreamNo == _iStreamNo)
	{
		return;
	}

	m_iLogonID = _iLogonID;
	m_iChannelNo = ((_iChannelNo < 0) ? 0 : _iChannelNo);
	m_iStreamNo = _iStreamNo;
}

void CLS_DlgUniqueAlertAlarmMessage::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertAlarmMessage::UI_InitDlgItemText()
{
	SetDlgItemText(IDC_BTN_ALERT_ALARM_CLEAR, GetTextByLan(_T("清除列表"), _T("Clear list")));
	SetDlgItemText(IDC_STATIC_ALARM_INFO, GetTextByLan(_T("报警信息"), _T("Alarm information")));

	m_lstAlarmInfo.DeleteAllItems();
	m_lstAlarmInfo.SetExtendedStyle(m_lstAlarmInfo.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	InsertColumn(m_lstAlarmInfo, eu_Column_CHN, IDS_VCA_CHANNEL_NO, LVCFMT_LEFT, 70);
	InsertColumn(m_lstAlarmInfo, eu_Column_STATE, IDS_VCA_ALARM_STATUS, LVCFMT_LEFT, 80);
	InsertColumn(m_lstAlarmInfo, eu_Column_EventType, IDS_VCA_EVENT_TYPE, LVCFMT_LEFT, 90);
	InsertColumn(m_lstAlarmInfo, eu_Column_CheckType, GetTextByLan(_T("检测模式"), _T("Check Type")),LVCFMT_LEFT, 90);
	InsertColumn(m_lstAlarmInfo, eu_Column_TargetId, IDS_VCA_TARGET_ID, LVCFMT_LEFT, 90);
	InsertColumn(m_lstAlarmInfo, eu_Column_TargetType, IDS_VCA_TARGET_TYPE, LVCFMT_LEFT, 90);
	InsertColumn(m_lstAlarmInfo, eu_Column_Position, IDS_VCA_TARGET_POS, LVCFMT_LEFT, 75);
	InsertColumn(m_lstAlarmInfo, eu_Column_Level, GetTextByLan(_T("警戒等级"), _T("Alert Level")), LVCFMT_LEFT, 70);
	
}

void CLS_DlgUniqueAlertAlarmMessage::OnAlarmNotify(int _iLogonID, int _iChannelNo, int _iAlarmIndex, int _iAlarmType,int _iUserData)
{
	if (_iLogonID != m_iLogonID)
	{
		return;
	}

	if (ALARM_UNIQUE_ALERT_MSG != _iAlarmType)
	{
		return;
	}

  	UniqueAlertAlarmMessage tMsg = {0};
	tMsg.iSize = sizeof(tMsg);
	tMsg.iMsgIndex = _iAlarmIndex;
	int iRet =  NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_ALARM_MESSAGE, _iChannelNo, &tMsg, tMsg.iSize);
	if (iRet < RET_SUCCESS)
	{
		return;
	}

	CString cstrChn;
	CString cstrState;;
	CString cstrEventType;
	CString cstrCheckType;
	CString cstrTargetId;
	CString cstrTargetType;
	CString cstrPosition;
	CString cstrAlertLevel;
	cstrChn = IntToCString(tMsg.iChanNo);
	
	switch (tMsg.iAlarmState)
	{
	case 0:
		cstrState = GetTextByLan(_T("消警"), _T("OFF"));
		break;
	case 1:
		cstrState = GetTextByLan(_T("报警"), _T("ON"));
		break;
	}

	switch (tMsg.iAlertType)
	{
	case 0:
		cstrEventType = GetTextByLan(_T("周界警戒"), _T("Alert Perimeter"));
		break;
	case 1:
		cstrEventType = GetTextByLan(_T("绊线警戒"), _T("Alert Tripwire"));
		break;
	}

	switch (tMsg.iCheckType)
	{
		case 0:
			cstrCheckType = GetTextByLan(_T("入侵"), _T("Intrusion"));
			break;
		case 1:
			cstrCheckType = GetTextByLan(_T("离开"), _T("Leave"));
			break;
	}

	cstrTargetId = IntToCString(tMsg.iTargetId);

	switch (tMsg.iTargetType)
	{
	case 1:
		cstrTargetType = GetTextByLan(_T("人"), _T("Human"));
		break;
	case 2:
		cstrTargetType = GetTextByLan(_T("物"), _T("Other"));
		break;
	case 3:
		cstrTargetType = GetTextByLan(_T("车"), _T("Car"));
		break;
	}

	cstrPosition.Format("(%d,%d,%d,%d)", tMsg.tTargetPosition.left, tMsg.tTargetPosition.top, tMsg.tTargetPosition.right, tMsg.tTargetPosition.bottom);
	cstrAlertLevel.Format("%d",tMsg.iLevel);
	int iItemCount = m_lstAlarmInfo.GetItemCount();
	m_lstAlarmInfo.InsertItem(iItemCount, "");
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_CHN, cstrChn);
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_STATE, cstrState);
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_EventType, cstrEventType);
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_CheckType, cstrCheckType);
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_TargetId, cstrTargetId);
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_TargetType, cstrTargetType);
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_Position, cstrPosition);
	m_lstAlarmInfo.SetItemText(iItemCount, eu_Column_Level, cstrAlertLevel);

}

void CLS_DlgUniqueAlertAlarmMessage::OnBnClickedBtnAlertAlarmClear()
{
	m_lstAlarmInfo.DeleteAllItems();
}

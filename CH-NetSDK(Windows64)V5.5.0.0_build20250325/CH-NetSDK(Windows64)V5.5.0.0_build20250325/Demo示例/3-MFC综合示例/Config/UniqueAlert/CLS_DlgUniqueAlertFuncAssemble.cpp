#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertFuncAssemble.h"

const int g_iIDS_AlertLevel[MAX_UNIQUE_ALERT_ALARM_LINK_LEVEL] = {IDS_ALERT_LEVEL_0, IDS_ALERT_LEVEL_1, IDS_ALERT_LEVEL_2, IDS_ALERT_LEVEL_3}; 

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertFuncAssemble, CDialog)

CLS_DlgUniqueAlertFuncAssemble::CLS_DlgUniqueAlertFuncAssemble(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgUniqueAlertFuncAssemble::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgUniqueAlertFuncAssemble::~CLS_DlgUniqueAlertFuncAssemble()
{
}

void CLS_DlgUniqueAlertFuncAssemble::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_WLIGHT_ALERT_TYPE, m_cboEventType);
	DDX_Control(pDX, IDC_CBO_WLIGHT_ALERT_LEVEL, m_cboAlertLevel);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertFuncAssemble, CDialog)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_CBO_WLIGHT_ALERT_TYPE, &CLS_DlgUniqueAlertFuncAssemble::OnCbnSelchangeCboWlightAlertType)
	ON_CBN_SELCHANGE(IDC_CBO_WLIGHT_ALERT_LEVEL, &CLS_DlgUniqueAlertFuncAssemble::OnCbnSelchangeCboWlightAlertLevel)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertFuncAssemble::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	return TRUE;
}

void CLS_DlgUniqueAlertFuncAssemble::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UI_InitDlgItemText();
		UI_UpdateInterfaceParam();
	}
}

void CLS_DlgUniqueAlertFuncAssemble::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (m_iLogonID == _iLogonID && m_iChannelNo == _iChannelNo && m_iStreamNo == _iStreamNo)
	{
		return;
	}

	m_iLogonID = _iLogonID;
	m_iChannelNo = ((_iChannelNo < 0) ? 0 : _iChannelNo);
	m_iStreamNo = _iStreamNo;

	UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertFuncAssemble::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertFuncAssemble::UI_InitDlgItemText()
{
	int iCurSel = m_cboEventType.GetCurSel();
	m_cboEventType.ResetContent();
	m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan(_T("周界警戒"), _T("Alert Perimeter"))), UNIQUE_ALERT_TYPE_PERIMETER);
	m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan(_T("绊线警戒"), _T("Alert Tripwire"))), UNIQUE_ALERT_TYPE_TRIPWIRE);
    m_cboEventType.SetItemData(m_cboEventType.AddString(GetTextByLan(_T("翻墙警戒"), _T("Alert ClimbWall"))), UNIQUE_ALERT_TYPE_CLIMBWALL);
	m_cboEventType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

	SetDlgItemText(IDC_GPO_ALERT_WHITE_LIGHT_INFO, GetTextByLan(_T("白光灯信息"), _T("White light information")));
	SetDlgItemText(IDC_STC_WLIGHT_ALERT_TYPE, GetTextByLan(_T("警戒类型"), _T("Alert Type")));
	SetDlgItemText(IDC_STC_WLIGHT_ALERT_LEVEL, GetTextByLan(_T("警戒等级"), _T("Alert level")));
	SetDlgItemText(IDC_CHK_WLIGHT_DEFAULT_MODE, GetTextByLan(_T("默认模式"), _T("Default mode")));
	SetDlgItemText(IDC_CHK_WLIGHT_FICKER, GetTextByLan(_T("频闪"), _T("Stroboscopic")));
	SetDlgItemText(IDC_CHK_WLIGHT_ALWAYS, GetTextByLan(_T("常亮"), _T("Normally on")));
	SetDlgItemText(IDC_STC_WLIGHT_FLASH_NUM, GetTextByLan(_T("最大闪光次数"), _T("Max flash times")));
	SetDlgItemText(IDC_STC_WLIGHT_DELAY_TIME, GetTextByLan(_T("常亮延迟熄灭时间"), _T("Max normally on delay time")));
}

void CLS_DlgUniqueAlertFuncAssemble::UI_UpdateInterfaceParam()
{
	UI_UpdateInfoWhiteLightMode();
}

void CLS_DlgUniqueAlertFuncAssemble::UI_UpdateInfoWhiteLightMode()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	UniqueAlertWhiteLightMode tMode = {0};
	tMode.iSize = sizeof(tMode);
	tMode.iAlertType = m_cboEventType.GetItemData(m_cboEventType.GetCurSel());
	iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_WHITE_LIGHT_MODE, m_iChannelNo, &tMode, tMode.iSize);
	if (iRet < RET_SUCCESS)
	{
		memset(&tMode, 0, sizeof(tMode));
	}

	m_cboAlertLevel.ResetContent();
	for(int i = 0; i < MAX_UNIQUE_ALERT_ALARM_LINK_LEVEL && i <= tMode.iSupportGrade; i++)
	{
		if (tMode.iSupportGrade > 0 && 0 == i)
		{
			continue;
		}

		m_cboAlertLevel.SetItemData(m_cboAlertLevel.AddString(GetTextEx(g_iIDS_AlertLevel[i])), i);
	}
	m_cboAlertLevel.SetCurSel(0);

	UI_UpdateInfoWhiteLightLevelParam();

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[WhiteLightMode](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[WhiteLightMode](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}

void CLS_DlgUniqueAlertFuncAssemble::UI_UpdateInfoWhiteLightLevelParam()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		return;
	}

	int iRet = RET_SUCCESS;
	UniqueAlertWhiteLightPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iAlertType = m_cboEventType.GetItemData(m_cboEventType.GetCurSel());
	tInfo.iGrade = m_cboAlertLevel.GetItemData(m_cboAlertLevel.GetCurSel());
	
	if (0 != tInfo.iGrade)
	{
		iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_WHITE_LIGHT_PARA, m_iChannelNo, &tInfo, tInfo.iSize);
	}

	if (iRet < RET_SUCCESS)
	{
		memset(&tInfo, 0, sizeof(tInfo));
	}

	((CButton*)(GetDlgItem(IDC_CHK_WLIGHT_DEFAULT_MODE)))->SetCheck(tInfo.iDefaultMode ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_WLIGHT_FICKER)))->SetCheck(tInfo.iFicker ? BST_CHECKED : BST_UNCHECKED);
	((CButton*)(GetDlgItem(IDC_CHK_WLIGHT_ALWAYS)))->SetCheck(tInfo.iAlways ? BST_CHECKED : BST_UNCHECKED);

	SetDlgItemInt(IDC_EDT_WLIGHT_FLASH_NUM, tInfo.iFlashNum);
	SetDlgItemInt(IDC_EDT_WLIGHT_DELAY_TIME, tInfo.iDelayTime);

	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetUnipueAlertConfig[WhiteLightPara](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetUnipueAlertConfig[WhiteLightPara](%d,%d)", m_iLogonID, m_iChannelNo);
	}
	return;
}


void CLS_DlgUniqueAlertFuncAssemble::OnCbnSelchangeCboWlightAlertType()
{
	UI_UpdateInfoWhiteLightMode();
}

void CLS_DlgUniqueAlertFuncAssemble::OnCbnSelchangeCboWlightAlertLevel()
{
	UI_UpdateInfoWhiteLightLevelParam();
}

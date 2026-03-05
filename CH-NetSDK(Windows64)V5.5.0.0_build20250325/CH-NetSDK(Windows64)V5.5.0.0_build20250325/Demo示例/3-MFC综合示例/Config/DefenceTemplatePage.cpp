
#include "stdafx.h"
#include "NetClientDemo.h"
#include "DefenceTemplatePage.h"

#define MAX_SCHEDULE_NUM 16
// CLS_DefenceTemplatePage dialog

IMPLEMENT_DYNAMIC(CLS_DefenceTemplatePage, CDialog)

CLS_DefenceTemplatePage::CLS_DefenceTemplatePage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DefenceTemplatePage::IDD, pParent)
{
	m_iLogonID = -1;
	m_pclsTempAdd = NULL;
}

CLS_DefenceTemplatePage::~CLS_DefenceTemplatePage()
{
	if (NULL != m_pclsTempAdd)
	{
		delete m_pclsTempAdd;
		m_pclsTempAdd = NULL;
	}
}

void CLS_DefenceTemplatePage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LST_DH_SCHEDULE, m_lstDefenceTemplate);
}


BEGIN_MESSAGE_MAP(CLS_DefenceTemplatePage, CDialog)
	ON_BN_CLICKED(IDC_BTN_DH_SCHEDULE, &CLS_DefenceTemplatePage::OnBnClickedBtnDhSchedule)
	ON_NOTIFY(NM_CLICK, IDC_LST_DH_SCHEDULE, &CLS_DefenceTemplatePage::OnNMClickLstDhSchedule)
END_MESSAGE_MAP()


// CLS_DefenceTemplatePage message handler

BOOL CLS_DefenceTemplatePage::OnInitDialog()
{
	CDialog::OnInitDialog();

	SetDlgItemTextEx(IDC_STC_DH_SCHEDULE, IDS_CONFIG_DONGHUAN_SCHEDULE);
	SetDlgItemTextEx(IDC_BTN_DH_SCHEDULE, IDS_CONFIG_LINK_ADD);

	InitWidget();

	if (NULL == m_pclsTempAdd)
	{
		m_pclsTempAdd = new CLS_DefenceTemplateAdd();
	}

	UpdateUI();
	return TRUE;
}

void CLS_DefenceTemplatePage::InitWidget()
{
	int iColumn = 0;
	DWORD dwStyle = m_lstDefenceTemplate.GetExtendedStyle();
	dwStyle = dwStyle | LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT;  
	m_lstDefenceTemplate.SetExtendedStyle(dwStyle);
	InsertColumn(m_lstDefenceTemplate, iColumn++, GetTextEx(IDS_CONFIG_LINK_ACTION_NUM), LVCFMT_LEFT, 100);
	InsertColumn(m_lstDefenceTemplate, iColumn++, GetTextEx(IDS_CONFIG_HD_TEMPLATENAME), LVCFMT_LEFT, 200);
	InsertColumn(m_lstDefenceTemplate, iColumn++, GetTextEx(IDS_CONFIG_OPETATE), LVCFMT_LEFT, 100);
	InsertColumn(m_lstDefenceTemplate, iColumn++, GetTextEx(IDS_CONFIG_OPETATE), LVCFMT_LEFT, 100);
}

void CLS_DefenceTemplatePage::OnLanguageChanged(int _iLanguage)
{
	SetDlgItemTextEx(IDC_STC_DH_SCHEDULE, IDS_CONFIG_DONGHUAN_SCHEDULE);
	SetDlgItemTextEx(IDC_BTN_DH_SCHEDULE, IDS_CONFIG_LINK_ADD);

	UpdateUI();
	InitWidget();
}

void CLS_DefenceTemplatePage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	UpdateUI();
}

BOOL CLS_DefenceTemplatePage::UpdateUI()
{
	m_lstDefenceTemplate.DeleteAllItems();
	for (int i=0; i<= MAX_SCHEDULE_NUM; i++)
	{
		UpdateSchedule(i);
	}
	return TRUE;
}

BOOL CLS_DefenceTemplatePage::UpdateSchedule(int _iID)
{
	AlarmInSchedule TmpInfo = {0};
	TmpInfo.iSize = sizeof(TmpInfo);
	TmpInfo.iScheduleNo = _iID;
	int iRet = NetClient_GetAlarmConfig(m_iLogonID,-1 ,-1, CMD_ALARM_IN_SCHEDULE, &TmpInfo);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DefenceTemplatePage::UpdateSchedule] NetClient_GetAlarmConfig failed! logonID(%d),error(%d)"
			,m_iLogonID,GetLastError());
		return FALSE;
	}

	if (1 == TmpInfo.iEnable)
	{
		int iColumn = 0;
		int iCount = m_lstDefenceTemplate.GetItemCount();
		m_lstDefenceTemplate.InsertItem(iCount,"");
		m_lstDefenceTemplate.SetItemText(iCount,iColumn++,IntToString(TmpInfo.iScheduleNo));
		m_lstDefenceTemplate.SetItemText(iCount,iColumn++,TmpInfo.cName);
		m_lstDefenceTemplate.SetItemText(iCount,iColumn++,GetTextEx(IDS_CONFIG_LINK_EDIT));
		m_lstDefenceTemplate.SetItemText(iCount,iColumn++,GetTextEx(IDS_CONFIG_LINK_DELE));
	}
	return TRUE;
}

void CLS_DefenceTemplatePage::OnBnClickedBtnDhSchedule()
{
	int iFreeTmpNo = GetFreeSchedule();
	if (iFreeTmpNo >= 0)
	{
		m_pclsTempAdd->InitParam(n_Dlg_ADD, m_iLogonID, iFreeTmpNo);
		if(IDOK == m_pclsTempAdd->DoModal())
		{
			UpdateUI();
		}
	}	
}

int CLS_DefenceTemplatePage::GetFreeSchedule()
{
	int iFreeTmpNo = -1;
	for (int i = 0; i < MAX_SCHEDULE_NUM; i++)
	{
		AlarmInSchedule Tmp = {0};
		Tmp.iSize = sizeof(Tmp);
		int iRet = -1;
		Tmp.iScheduleNo = i;
		iRet = NetClient_GetAlarmConfig(m_iLogonID,-1 ,-1, CMD_ALARM_IN_SCHEDULE, &Tmp);
		if (0 == iRet && 0 == Tmp.iEnable)
		{
			iFreeTmpNo = i;
			break;
		}
	}
	return iFreeTmpNo;
}
void CLS_DefenceTemplatePage::OnNMClickLstDhSchedule(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	
	NM_LISTVIEW* pNMListView = (NM_LISTVIEW*)pNMHDR;

	int nItem = pNMListView->iItem;		//Line number
	int nSub = 	pNMListView->iSubItem;	//Column number
	int iCount = m_lstDefenceTemplate.GetItemCount();
	if(nItem  == -1)	//If there is no data in the row, return
	{
		return;
	}
	int iTmpNo = atoi(m_lstDefenceTemplate.GetItemText(nItem,0));
	//edit
	if (2 == nSub)
	{
		m_pclsTempAdd->InitParam(n_Dlg_Edit, m_iLogonID, iTmpNo);
		if(IDOK == m_pclsTempAdd->DoModal())
		{
			UpdateUI();
		}
	}
	//delete
	if (3 == nSub)
	{
		AlarmInSchedule tmp = {0};
		tmp.iSize = sizeof(tmp);
		tmp.iScheduleNo = iTmpNo;
		tmp.iEnable = 0;
		int iRet = NetClient_SetAlarmConfig(m_iLogonID,-1 ,-1, CMD_ALARM_IN_SCHEDULE, &tmp);
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL,"","[CLS_DefenceTemplatePage::UpdateSchedule] NetClient_GetAlarmConfig failed! logonID(%d),error(%d)"
				,m_iLogonID,GetLastError());
			return ;
		}
		UpdateUI();
	}
	*pResult = 0;
}

//Handling ParamChange messages
LRESULT	CLS_DefenceTemplatePage::OnParamChangeMsg(WPARAM wParam, LPARAM lParam)
{
	UpdateUI();
	return NULL;
}

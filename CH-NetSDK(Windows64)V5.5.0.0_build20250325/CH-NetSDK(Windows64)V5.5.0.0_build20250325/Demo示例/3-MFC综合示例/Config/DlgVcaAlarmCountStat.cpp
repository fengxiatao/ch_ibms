// DlgVcaAlarmCountStat.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgVcaAlarmCountStat.h"


// DlgVcaAlarmCountStat 对话框

IMPLEMENT_DYNAMIC(DlgVcaAlarmCountStat, CDialog)

DlgVcaAlarmCountStat::DlgVcaAlarmCountStat(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(DlgVcaAlarmCountStat::IDD, pParent)
{
	m_iChannelNo = -1;
}

DlgVcaAlarmCountStat::~DlgVcaAlarmCountStat()
{
}

void DlgVcaAlarmCountStat::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_ALARM_COUNT, m_listAlarmCount);
}


void DlgVcaAlarmCountStat::UI_UpdateDialog()
{
	while (m_listAlarmCount.DeleteColumn(0));
	m_listAlarmCount.InsertColumn(0, GetTextByLan(_T("预留"), _T("Reserve")), LVCFMT_CENTER, 0);
	m_listAlarmCount.InsertColumn(1, GetTextByLan(_T("算法"), _T("Algorithm")), LVCFMT_CENTER, 350);
	m_listAlarmCount.InsertColumn(2, GetTextByLan(_T("颜色"), _T("Color")), LVCFMT_CENTER, 150);
	m_listAlarmCount.InsertColumn(3, GetTextByLan(_T("数量"), _T("Number")), LVCFMT_CENTER, 150);
	m_listAlarmCount.DeleteColumn(0);
}

void DlgVcaAlarmCountStat::RefreshList()
{
	m_listAlarmCount.DeleteAllItems();
}

BOOL DlgVcaAlarmCountStat::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_listAlarmCount.SetExtendedStyle(LVS_EX_FULLROWSELECT | LVS_EX_HEADERDRAGDROP | LVS_EX_GRIDLINES);

	UI_UpdateDialog();

	return TRUE;
}

void DlgVcaAlarmCountStat::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	RefreshList();
}

void DlgVcaAlarmCountStat::OnLanguageChanged(int _iLanguage)
{
	RefreshList();
	UI_UpdateDialog();
}

void DlgVcaAlarmCountStat::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (m_iLogonID < 0 || m_iLogonID != _iLogonID)
	{
		return;
	}

	if (_iChannelNo != m_iChannelNo)//Only refresh the channel whose parameter has changed
	{
		return;
	}

	switch(_iParaType)
	{
	case PARA_VCA_ALARMSTAT_V2:
		{
			STR_Para* _strPara;
			_strPara = (STR_Para*) _pPara;

			int iAlgType = (int)(long)_strPara->m_iPara[0];
			if(VCA_EVENT_POLICE_UNIFORM_DETECTION == iAlgType) {
				int iNum = (int)(long)_strPara->m_iPara[1];
				int iCurLine = m_listAlarmCount.GetItemCount();
				CString strNum;
				strNum.Format(_T("%d"), iNum);
				m_listAlarmCount.InsertItem(iCurLine, GetTextByLan(_T("94-民警警服检测"), _T("94-Police uniform detection")));
				m_listAlarmCount.SetItemText(iCurLine, 1, GetTextByLan(_T("未知"), _T("Unknown")));
				m_listAlarmCount.SetItemText(iCurLine, 2, strNum);
			} else if(VCA_EVENT_SPDRESS_DETECTION == iAlgType) {
				CString strChn[] = { _T("橙色"), _T("黄色"), _T("绿色"), _T("红色"), _T("蓝色"), _T("未知") };
				CString strEng[] = { _T("Orange"), _T("Yellow"), _T("Green"), _T("Red"), _T("Blue"), _T("Unknown") };
				int iIndex = 0;
				for(iIndex = 0; iIndex < LEN_6; ++iIndex)
				{
					int iNum = (int)(long)_strPara->m_iPara[iIndex + 1];
					int iCurLine = m_listAlarmCount.GetItemCount();
					m_listAlarmCount.InsertItem(iCurLine, GetTextByLan(_T("95-被监管人员识别服检测"), _T("95-Supervised person identification service detection")));
					m_listAlarmCount.SetItemText(iCurLine, 1, GetTextByLan(strChn[iIndex], strEng[iIndex]));
					CString strNum;
					strNum.Format(_T("%d"), iNum);
					m_listAlarmCount.SetItemText(iCurLine, 2, strNum);
				}
			}
		}
		break;
	default:
		break;
	}
}

BEGIN_MESSAGE_MAP(DlgVcaAlarmCountStat, CDialog)
END_MESSAGE_MAP()


// DlgVcaAlarmCountStat 消息处理程序


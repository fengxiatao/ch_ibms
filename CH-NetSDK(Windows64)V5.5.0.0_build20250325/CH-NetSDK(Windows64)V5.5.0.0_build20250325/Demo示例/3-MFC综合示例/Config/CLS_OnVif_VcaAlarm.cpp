// CLS_OnVif_VcaAlarm.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_OnVif_VcaAlarm.h"

// CLS_OnVif_VcaAlarm dialog

IMPLEMENT_DYNAMIC(CLS_OnVif_VcaAlarm, CDialog)

CLS_OnVif_VcaAlarm::CLS_OnVif_VcaAlarm(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_OnVif_VcaAlarm::IDD, pParent)
{

}

CLS_OnVif_VcaAlarm::~CLS_OnVif_VcaAlarm()
{
}

BOOL CLS_OnVif_VcaAlarm::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UI_UpdateUIText();
	UpdateData(FALSE);
	m_cboState.SetCurSel(0);
	return TRUE;
}

void CLS_OnVif_VcaAlarm::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
}

void CLS_OnVif_VcaAlarm::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_CHANNO, GetTextByLan(_T("通道号"), _T("Channo Num")));
	SetDlgItemText(IDC_STATIC_STATE, GetTextByLan(_T("状态"), _T("State")));
	SetDlgItemText(IDC_STATIC_EVENT_TYPE, GetTextByLan(_T("事件类型"), _T("Event type")));
	SetDlgItemText(IDC_STATIC_GROUP_NUM, GetTextByLan(_T("参数组数量"), _T("Group num")));
	SetDlgItemText(IDC_STATIC_PARA_NUM, GetTextByLan(_T("每组参数个数"), _T("Param num in group")));

	m_cboState.ResetContent();
	m_cboState.AddString(GetTextByLan(_T("0-消警"), _T("0-alarm eliminate")));
	m_cboState.AddString(GetTextByLan(_T("1-报警"), _T("1-alarm")));
	m_cboState.SetCurSel(0);

	for(int i = 0; i < m_listParam.GetItemCount(); i++)
	{
		m_listParam.DeleteColumn(0);
	}
	m_listParam.InsertColumn(0, GetTextByLan(_T("保留"), _T("Retain")), LVCFMT_CENTER, 0);
	m_listParam.InsertColumn(1, GetTextByLan(_T("序号"), _T("No")), LVCFMT_CENTER, 60);
	m_listParam.InsertColumn(2, GetTextByLan(_T("参数类型"), _T("Param Type")), LVCFMT_CENTER, 100);
	m_listParam.InsertColumn(3, GetTextByLan(_T("参数"), _T("Param")), LVCFMT_CENTER, 100);
	m_listParam.DeleteColumn(0);
}

void CLS_OnVif_VcaAlarm::OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser)
{
	switch(_iAlarmType)
	{
	case CALLBACK_ALARMTYPE_ONVIF_VCAALARM:
		{
			OnvifVcaAlarm *tInfo = (OnvifVcaAlarm*)_pInfo;
			SetDlgItemInt(IDC_EDIT_CHANNO, tInfo->iChannelNo);
			m_cboState.SetCurSel(tInfo->iState);
			switch(tInfo->iEventType)
			{
			case VCA_EVENT_TRIPWIRE:
				{
					SetDlgItemText(IDC_EDIT_EVENT_TYPE, GetTextByLan(_T("单绊线"), _T("Single trip wire")));
					break;
				}
			case VCA_EVENT_DBTRIPWIRE:
				{
					SetDlgItemText(IDC_EDIT_EVENT_TYPE, GetTextByLan(_T("双绊线"), _T("Double trip wire")));
					break;
				}
			case VCA_EVENT_PERIMETER:
				{
					SetDlgItemText(IDC_EDIT_EVENT_TYPE, GetTextByLan(_T("周界检测"), _T("Perimeter detection")));
					break;
				}
			case VCA_EVENT_EVETEMDETECT:
				{
					SetDlgItemText(IDC_EDIT_EVENT_TYPE, GetTextByLan(_T("环境温度检测"), _T("Ambient temperature detection")));
					break;
				}
			default:
				{
					SetDlgItemInt(IDC_EDIT_EVENT_TYPE, tInfo->iEventType);
					break;
				}
			}
			
			SetDlgItemInt(IDC_EDIT_GROUP_NUM, tInfo->iArrayCount);
			SetDlgItemInt(IDC_EDIT_PARA_NUM, tInfo->iParaCount);
			for(int i = 0; i < tInfo->iArrayCount && i < MAX_ONVIFVCAALARM_ARRAY_COUNT; i++)
			{
				m_listParam.InsertItem(i, IntToStr(i));
				m_listParam.SetItemText(i, 1, IntToStr(tInfo->tOvfVcaAlarmPara[i].iParaType));
				m_listParam.SetItemText(i, 2, tInfo->tOvfVcaAlarmPara[i].cParam);
			}
		}
		break;
	default:
		break;
	}
}

CString CLS_OnVif_VcaAlarm::IntToStr(int _iNum)
{
	CString str;
	str.Format("%d", _iNum);
	return str;
}

void CLS_OnVif_VcaAlarm::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_STATE, m_cboState);
	DDX_Control(pDX, IDC_LIST_PARAM, m_listParam);
}


BEGIN_MESSAGE_MAP(CLS_OnVif_VcaAlarm, CDialog)
END_MESSAGE_MAP()


// CLS_OnVif_VcaAlarm message handler

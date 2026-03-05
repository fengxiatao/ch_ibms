// E:\SDK\trunk\Demo\NetClientDemo\Config\ITSParkCarNum.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "ITSParkCarNum.h"

// CLS_ITSParkCarNum dialog

IMPLEMENT_DYNAMIC(CLS_ITSParkCarNum, CDialog)

CLS_ITSParkCarNum::CLS_ITSParkCarNum(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ITSParkCarNum::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

CLS_ITSParkCarNum::~CLS_ITSParkCarNum()
{
}

void CLS_ITSParkCarNum::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_PARKCARNUM_STATE, m_ListParkCarNumState);
}


BEGIN_MESSAGE_MAP(CLS_ITSParkCarNum, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_PARKCARNUM_CLEAR, &CLS_ITSParkCarNum::OnBnClickedButtonParkcarnumClear)
END_MESSAGE_MAP()

BOOL CLS_ITSParkCarNum::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_ListParkCarNumState.SetExtendedStyle(m_ListParkCarNumState.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	UI_UpdateText();
	//UI_Clear();
	return TRUE;  
}

void CLS_ITSParkCarNum::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateText();
}

void CLS_ITSParkCarNum::UI_UpdateText()
{
	SetDlgItemText(IDC_STATIC_PARK_STATE, GetTextByLan(_T("违停车辆信息"), _T("ParkCarInfo")));
	SetDlgItemText(IDC_BUTTON_PARKCARNUM_CLEAR, GetTextByLan(_T("清除"), _T("clear")));
	
	int iColumn = 0;
	InsertColumn(m_ListParkCarNumState, iColumn++, GetTextByLan(_T("通道号"),_T("ChannelNo")), LVCFMT_CENTER, 80);
	InsertColumn(m_ListParkCarNumState, iColumn++, GetTextByLan(_T("车牌号"),_T("CarNum")), LVCFMT_CENTER, 200);
	InsertColumn(m_ListParkCarNumState, iColumn++, GetTextByLan(_T("违停车辆状态"),_T("ParkState")), LVCFMT_CENTER, 200);
	//
	m_ListParkCarNumState.DeleteAllItems();
}

CString CLS_ITSParkCarNum::GetParkStateByInt(int _iParkStatus)
{
	CString cstrParkStatus;
	switch(_iParkStatus)
	{
	case 0:
		cstrParkStatus = GetTextByLan(_T("没有变化"), _T("NoChanges"));
		break;
	case 1:
		cstrParkStatus = GetTextByLan(_T("开始"), _T("Start"));
		break;
	case 2:
		cstrParkStatus = GetTextByLan(_T("结束"), _T("End"));
		break;
	default:
		break;
	}
	return cstrParkStatus;
}

void CLS_ITSParkCarNum::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	int iMsgType = _wParam & 0xFFFF;
	if (WCM_ITS_PARKCARNUM == iMsgType)
	{
		ITSParkCarNum *ptInfo = (ITSParkCarNum*)_iLParam;
		ITSParkCarNum tInfo = {0};
		if (NULL != ptInfo)
		{
			int iCpySize = min(ptInfo->iSize, sizeof(ITSParkCarNum));
			memcpy(&tInfo, ptInfo, iCpySize);

		}
		int iItemCount = m_ListParkCarNumState.GetItemCount();
		m_ListParkCarNumState.InsertItem(iItemCount, "");
		int iColumn = 0;
		m_ListParkCarNumState.SetItemText(iItemCount, iColumn++, IntToCString(tInfo.iChannel));
		m_ListParkCarNumState.SetItemText(iItemCount, iColumn++, tInfo.cCarNum);
		m_ListParkCarNumState.SetItemText(iItemCount, iColumn++, GetParkStateByInt(tInfo.iParkStatus));
	}
}
void CLS_ITSParkCarNum::OnBnClickedButtonParkcarnumClear()
{
	m_ListParkCarNumState.DeleteAllItems();
}

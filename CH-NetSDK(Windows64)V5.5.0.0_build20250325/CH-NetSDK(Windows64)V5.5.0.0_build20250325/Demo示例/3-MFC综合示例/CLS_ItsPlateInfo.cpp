// CLS_ItsPlateInfo.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_ItsPlateInfo.h"


// CLS_ItsPlateInfo 对话框

IMPLEMENT_DYNAMIC(CLS_ItsPlateInfo, CDialog)

CLS_ItsPlateInfo::CLS_ItsPlateInfo(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ItsPlateInfo::IDD, pParent)
{

}

CLS_ItsPlateInfo::~CLS_ItsPlateInfo()
{
}

void CLS_ItsPlateInfo::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_PlATE_INFO, m_lstPlateInfo);
	DDX_Control(pDX, IDC_COMBOX_PlATE_PAGE, m_cboPlatePage);
	DDX_Control(pDX, IDC_COMBOX_PlATE_LIB, m_cboPlateLib);
}


BEGIN_MESSAGE_MAP(CLS_ItsPlateInfo, CDialog)
	ON_BN_CLICKED(IDC_BTN_PLATE_SEARCH, &CLS_ItsPlateInfo::OnBnClickedBtnPlateSearch)
	ON_BN_CLICKED(IDC_BTN_PLATE_ADD, &CLS_ItsPlateInfo::OnBnClickedBtnPlateAdd)
	ON_BN_CLICKED(IDC_BTN_PLATE_EDIT, &CLS_ItsPlateInfo::OnBnClickedBtnPlateEdit)
	ON_BN_CLICKED(IDC_BTN_PLATE_DELETE, &CLS_ItsPlateInfo::OnBnClickedBtnPlateDelete)
	ON_NOTIFY(NM_CLICK, IDC_LIST_PlATE_INFO, &CLS_ItsPlateInfo::OnNMClickListPlateInfo)
	ON_NOTIFY(NM_RCLICK, IDC_LIST_PlATE_INFO, &CLS_ItsPlateInfo::OnNMRClickListPlateInfo)
	ON_CBN_SELCHANGE(IDC_COMBOX_PlATE_PAGE, &CLS_ItsPlateInfo::OnCbnSelchangeComboxPlatePage)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_COMBOX_PlATE_LIB, &CLS_ItsPlateInfo::OnCbnSelchangeComboxPlateLib)
END_MESSAGE_MAP()


BOOL CLS_ItsPlateInfo::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateUIText();
	UI_UpdateUIPlateLib();

	return TRUE;
}

// 更新当前选中的通道
void CLS_ItsPlateInfo::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNO = _iChannelNo;
}

// 更新当前的显示语言
void CLS_ItsPlateInfo::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	UI_UpdateUIPlateLib();
}

// 更新界面显示文本
void CLS_ItsPlateInfo::UI_UpdateUIText()
{
	m_lstPlateInfo.DeleteAllItems();
	m_lstPlateInfo.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	InsertColumn(m_lstPlateInfo, ITEM_PLATE_SEQ, GetTextByLan(_T("序号"), _T("No.")), LVCFMT_LEFT, 100);
	InsertColumn(m_lstPlateInfo, ITEM_PLATE_NUM, GetTextByLan(_T("车牌号"), _T("License plate number")), LVCFMT_LEFT, 400);

	SetDlgItemText(IDC_STC_PlATE_LIB, GetTextByLan(_T("车牌库"), _T("License plate library")));
	SetDlgItemText(IDC_STC_PlATE_NUM, GetTextByLan(_T("车牌号"), _T("License plate number")));

	SetDlgItemText(IDC_BTN_PLATE_ADD, GetTextByLan(_T("添加"), _T("Add")));
	SetDlgItemText(IDC_BTN_PLATE_SEARCH, GetTextByLan(_T("查询"), _T("Search")));
	SetDlgItemText(IDC_BTN_PLATE_EDIT, GetTextByLan(_T("修改"), _T("Edit")));
	SetDlgItemText(IDC_BTN_PLATE_DELETE, GetTextByLan(_T("删除"), _T("Delete")));

	SetDlgItemText(IDC_STC_PlATE_PAGE, GetTextByLan(_T("跳转至"), _T("Jump To")));
}

void CLS_ItsPlateInfo::OnBnClickedBtnPlateSearch()
{
	UI_UpdatePlateInfo(0, TRUE);
}

void CLS_ItsPlateInfo::OnBnClickedBtnPlateAdd()
{
	XmlPlateInfoParas tInfo = {0};
	tInfo.iPlateLibIndex = m_cboPlateLib.GetItemData(m_cboPlateLib.GetCurSel());
	CString cstrValue;
	GetDlgItemText(IDC_EDIT_PlATE_NUM, cstrValue);
	strncpy_s(tInfo.cPlateNum, (LPSTR)(LPCTSTR)cstrValue, min(LEN_32-1, cstrValue.GetLength()));

	XmlPlateInfoResult tResult = {0};
	int iRet = NetClient_XmlCmdConfig(m_iLogonID, NETXMLCMD_PLATEINFO_ADD, &tInfo,sizeof(tInfo), &tResult, sizeof(tResult));
	if(RET_SUCCESS == iRet)
	{
		if (-1LL == tResult.llPlateInfoIndex)
		{
			MessageBox(GetTextByLan(_T("车牌号重复"), _T("Duplicate license plate.")), "Tips", MB_OK);
		}
		else if (-2LL == tResult.llPlateInfoIndex)
		{
			MessageBox(GetTextByLan(_T("车牌数量已满"), _T("The number of license plates is full.")), "Tips", MB_OK);
		}
		else
		{
			OnBnClickedBtnPlateSearch();
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlCmdConfig[NETXMLCMD_PLATEINFO_ADD] (%d)",m_iLogonID);
	}
}

void CLS_ItsPlateInfo::OnBnClickedBtnPlateEdit()
{
	POSITION pPos = m_lstPlateInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos)
	{
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}

	int iIndex = m_lstPlateInfo.GetNextSelectedItem(pPos);
	long long llIndex = (long long)m_lstPlateInfo.GetItemData(iIndex);

 	XmlPlateInfoParas tInfo = {0};
	tInfo.iPlateLibIndex = m_cboPlateLib.GetItemData(m_cboPlateLib.GetCurSel());
	tInfo.llPlateInfoIndex = llIndex;
	CString cstrValue;
	GetDlgItemText(IDC_EDIT_PlATE_NUM, cstrValue);
	strncpy_s(tInfo.cPlateNum, (LPSTR)(LPCTSTR)cstrValue, min(LEN_32-1, cstrValue.GetLength()));

 	int iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_PLATEINFO_EDIT, &tInfo, sizeof(tInfo), NULL, 0);
 	if(RET_SUCCESS == iRet)
 	{
 		AddLog(LOG_TYPE_SUCC, "","CLS_ItsPlateInfo::NetClient_XmlSetDevConfig[NETXMLCFG_PLATEINFO_EDIT] (%d)", m_iLogonID);
 	}
 	else
 	{
 		AddLog(LOG_TYPE_FAIL,"","CLS_ItsPlateInfo::NetClient_XmlSetDevConfig[NETXMLCFG_PLATEINFO_EDIT] (%d), error(%d)", m_iLogonID, GetLastError());
 	}
 
 	OnBnClickedBtnPlateSearch();
}

void CLS_ItsPlateInfo::OnBnClickedBtnPlateDelete()
{
	POSITION pPos = m_lstPlateInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos)
	{
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}

	int iIndex = m_lstPlateInfo.GetNextSelectedItem(pPos);
	long long llIndex = (long long)m_lstPlateInfo.GetItemData(iIndex);

	XmlPlateInfoParas tInfo = {0};
	tInfo.iPlateLibIndex = m_cboPlateLib.GetItemData(m_cboPlateLib.GetCurSel());
	tInfo.llPlateInfoIndex = llIndex;

	int iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_PLATEINFO_DELETE, &tInfo, sizeof(tInfo), NULL, 0);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_ItsPlateInfo::NetClient_XmlSetDevConfig[NETXMLCFG_PLATEINFO_DELETE] (%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_ItsPlateInfo::NetClient_XmlSetDevConfig[NETXMLCFG_PLATEINFO_DELETE] (%d), error(%d)", m_iLogonID, GetLastError());
	}

	OnBnClickedBtnPlateSearch();
}

void CLS_ItsPlateInfo::OnNMClickListPlateInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	if (pNMItemActivate->iItem >= 0)
	{
		CString cstrPlate = m_lstPlateInfo.GetItemText(pNMItemActivate->iItem, ITEM_PLATE_NUM);
		SetDlgItemText(IDC_EDIT_PlATE_NUM, cstrPlate);
	}
	*pResult = 0;
}

void CLS_ItsPlateInfo::OnNMRClickListPlateInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	if (pNMItemActivate->iItem >= 0)
	{
		CString cstrPlate = m_lstPlateInfo.GetItemText(pNMItemActivate->iItem, ITEM_PLATE_NUM);
		SetDlgItemText(IDC_EDIT_PlATE_NUM, cstrPlate);
	}
	*pResult = 0;
}

void CLS_ItsPlateInfo::UI_UpdateUIComboxPage(int _iPageSize,int _iTotalCount)
{
	m_cboPlatePage.ResetContent();

	int iPageNum = _iTotalCount/_iPageSize;
	
	if(0 != _iTotalCount%_iPageSize)
	{
		iPageNum++;
	}

	for (int i=0; i<PLATE_NUM_TOTALCOUNT && i<iPageNum; i++)
	{
		m_cboPlatePage.AddString(IntToString(i+1));
	}

	if (iPageNum>=0)
	{
		m_cboPlatePage.SetCurSel(0);
	}
}

void CLS_ItsPlateInfo::UI_UpdatePlateInfo(int _iPageNo,BOOL _blUpdatePage)
{
	m_lstPlateInfo.DeleteAllItems();

	XmlPlateInfoQueryCondition tInfo = {0};
	tInfo.iPageNo = _iPageNo;
	tInfo.iPageSize = MAX_PLATE_PAGESIZE;
	tInfo.iPlateLibIndex = m_cboPlateLib.GetItemData(m_cboPlateLib.GetCurSel());

	XmlPlateInfoQueryResultPage tResult = {0};

	int iRet = NetClient_XmlCmdConfig(m_iLogonID, NETXMLCMD_PLATEINFO_QUERY, &tInfo,sizeof(tInfo), &tResult, sizeof(tResult));
	if(RET_SUCCESS == iRet)
	{
		for (int i= 0; i<tResult.iPageSize && i<MAX_PLATE_PAGESIZE; i++)
		{
			if (tResult.tResultInfo[i].llPlateInfoIndex <= 0)
			{
				continue;
			}
			m_lstPlateInfo.InsertItem(i,_T(""));
			m_lstPlateInfo.SetItemData(i, tResult.tResultInfo[i].llPlateInfoIndex);
			m_lstPlateInfo.SetItemText(i, ITEM_PLATE_SEQ, IntToStr(i + 1).c_str());
			m_lstPlateInfo.SetItemText(i, ITEM_PLATE_NUM, tResult.tResultInfo[i].cPlateNum);
		}

		if (_blUpdatePage)
		{
			UI_UpdateUIComboxPage(tInfo.iPageSize, tResult.iTotalCount);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlCmdConfig[NETXMLCMD_PLATEINFO_QUERY] (%d)",m_iLogonID);
	}
}

void CLS_ItsPlateInfo::OnCbnSelchangeComboxPlatePage()
{
	int iCutSel = 0;
	iCutSel = m_cboPlatePage.GetCurSel();
	UI_UpdatePlateInfo(iCutSel, FALSE);
}

void CLS_ItsPlateInfo::UI_UpdateUIPlateLib()
{
	m_cboPlateLib.ResetContent();

	XmlPlateLibInfo tInfo = {0};
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_PLATELIBINFO, &tInfo, sizeof(tInfo), &tInfo, sizeof(tInfo));
	if(RET_SUCCESS == iRet)
	{
		for (int i=0; i<MAX_PLATE_LIB_NUM && i<tInfo.iLibUsedNum; i++)
		{
			CString strLibName;
			strLibName.Format( "%s",tInfo.tPlateLibItemList[i].cPlateLibName);
			m_cboPlateLib.InsertString(i, strLibName);
			m_cboPlateLib.SetItemData(i, tInfo.tPlateLibItemList[i].iPlateLibIndex);
		}
		m_cboPlateLib.SetCurSel(0);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlCmdConfig[NETXMLCFG_PLATELIBINFO] (%d)",m_iLogonID);
	}
}
void CLS_ItsPlateInfo::OnShowWindow(BOOL bShow, UINT nStatus)
{
	if (bShow)
	{
		UI_UpdateUIPlateLib();
		UI_UpdatePlateInfo(0,TRUE);
	}
	CLS_BasePage::OnShowWindow(bShow, nStatus);
}

void CLS_ItsPlateInfo::OnCbnSelchangeComboxPlateLib()
{
	UI_UpdatePlateInfo(0, TRUE);
}

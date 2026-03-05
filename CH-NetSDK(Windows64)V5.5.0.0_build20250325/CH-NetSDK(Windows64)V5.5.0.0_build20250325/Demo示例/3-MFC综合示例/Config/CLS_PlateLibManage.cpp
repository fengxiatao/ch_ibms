// CLS_PlateLibManage.cpp
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_PlateLibManage.h"

#define ADD_PLATE_LIBRARY			0

// CLS_PlateLibManage

IMPLEMENT_DYNAMIC(CLS_PlateLibManage, CDialog)

CLS_PlateLibManage::CLS_PlateLibManage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_PlateLibManage::IDD, pParent)
{

}

CLS_PlateLibManage::~CLS_PlateLibManage()
{
}

BOOL CLS_PlateLibManage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	// set the list style
	m_listPlateLib.SetExtendedStyle(LVS_EX_FULLROWSELECT | LVS_EX_HEADERDRAGDROP | LVS_EX_GRIDLINES);
	UI_UpdateUIText();
	return TRUE;
}

void CLS_PlateLibManage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNO = _iChannelNo;
	m_iStreamNO = _iStreamNo;
	GetPlateLibParas();
}

void CLS_PlateLibManage::OnLanguageChanged(int _iLanguage)
{
	m_listPlateLib.DeleteAllItems();
	UI_UpdateUIText();
	GetPlateLibParas();
}

void CLS_PlateLibManage::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_PLATE, GetTextByLan(_T("车牌："), _T("Plate:")));
	SetDlgItemText(IDC_STATIC_PLATELIB, GetTextByLan(_T("车牌库："), _T("PlateLibrary:")));
	SetDlgItemText(IDC_STATIC_PLATE_NUM, _T("0/0"));
	SetDlgItemText(IDC_STATIC_PLATELIB_NUM, _T("0/0"));
	SetDlgItemText(IDC_BUTTON_PLATELIB_ADD, GetTextByLan(_T("添加"), _T("Add")));
	SetDlgItemText(IDC_BUTTON_PLATELIB_SET, GetTextByLan(_T("修改"), _T("Modify")));
	SetDlgItemText(IDC_BUTTON_PLATELIB_DELETE, GetTextByLan(_T("删除"), _T("Delete")));
	SetDlgItemText(IDC_STATIC_NAME, GetTextByLan(_T("名称"), _T("Name")));
	SetDlgItemText(IDC_STATIC_REMARK, GetTextByLan(_T("备注"), _T("Remark")));
	while(m_listPlateLib.DeleteColumn(0));
	m_listPlateLib.InsertColumn(0, GetTextByLan(_T("保留"), _T("Retain")), LVCFMT_CENTER, 0);
	m_listPlateLib.InsertColumn(1, GetTextByLan(_T("序号"), _T("Serial No")), LVCFMT_CENTER, 100);
	m_listPlateLib.InsertColumn(2, GetTextByLan(_T("名称"), _T("Name")), LVCFMT_CENTER, 200);
	m_listPlateLib.InsertColumn(3, GetTextByLan(_T("备注"), _T("Remark")), LVCFMT_CENTER, 200);
	m_listPlateLib.DeleteColumn(0);		//delete column 0
}

// get Plate Library information
void CLS_PlateLibManage::GetPlateLibParas()
{
	if(m_iLogonID < 0 || m_iChannelNO < 0)
	{
		return;	// logonID or channelNo is illegal
	}
	
	XmlPlateLibInfo tInfo;
	memset(&tInfo, 0, sizeof(XmlPlateLibInfo));
	tInfo.iChannel = m_iChannelNO;
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_PLATELIBINFO, &tInfo, sizeof(XmlPlateLibInfo), &tInfo, sizeof(XmlPlateLibInfo));
	if(RET_SUCCESS == iRet)
	{
		m_listPlateLib.DeleteAllItems();
		CString strPlate, strPlateLib;
		strPlate.Format(_T("%d/%d"), tInfo.iUsedNum, tInfo.iMaxNum);
		strPlateLib.Format(_T("%d/%d"), tInfo.iLibUsedNum, tInfo.iMaxLibNum);
		SetDlgItemText(IDC_STATIC_PLATE_NUM, strPlate);
		SetDlgItemText(IDC_STATIC_PLATELIB_NUM, strPlateLib);
		for(int i = 0; i < tInfo.iMaxLibNum && i < tInfo.iLibUsedNum && i < MAX_PLATE_LIB_NUM; ++i)
		{
			CString str;
			str.Format(_T("%d"), tInfo.tPlateLibItemList[i].iPlateLibIndex);
			m_listPlateLib.InsertItem(i, str);
			m_listPlateLib.SetItemText(i, 1, tInfo.tPlateLibItemList[i].cPlateLibName);
			m_listPlateLib.SetItemText(i, 2, tInfo.tPlateLibItemList[i].cPlateLibRemark);
		}
		SetDlgItemText(IDC_EDIT_NAME, _T(""));
		SetDlgItemText(IDC_EDIT_REMARK, _T(""));
		AddLog(LOG_TYPE_SUCC, "","CLS_PlateLibManage::NetClient_XmlGetDevConfig[NETXMLCFG_PLATELIBINFO] (%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_PlateLibManage::NetClient_XmlGetDevConfig[NETXMLCFG_PLATELIBINFO] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_PlateLibManage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_PLATELIBRARY, m_listPlateLib);
}


BEGIN_MESSAGE_MAP(CLS_PlateLibManage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_PLATELIB_ADD, &CLS_PlateLibManage::OnBnClickedButtonPlatelibAdd)
	ON_BN_CLICKED(IDC_BUTTON_PLATELIB_SET, &CLS_PlateLibManage::OnBnClickedButtonPlatelibSet)
	ON_BN_CLICKED(IDC_BUTTON_PLATELIB_DELETE, &CLS_PlateLibManage::OnBnClickedButtonPlatelibDelete)
	ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_PLATELIBRARY, &CLS_PlateLibManage::OnLvnItemchangedListPlatelibrary)
END_MESSAGE_MAP()


// CLS_PlateLibManage 消息处理程序

void CLS_PlateLibManage::OnBnClickedButtonPlatelibAdd()
{
	CString strName;
	CString strRemark;
	GetDlgItemText(IDC_EDIT_NAME, strName);
	if(strName.IsEmpty())
	{
		MessageBox(GetTextByLan(_T("新增车牌库名称不能为空"), _T("The name of the new license plate library cannot be empty")));
		return;
	}
	GetDlgItemText(IDC_EDIT_REMARK, strRemark);
	XmlPlateLibParas tInfo;
	XmlPlateInfoResult tOut;
	memset(&tInfo, 0, sizeof(XmlPlateLibParas));
	memset(&tOut, 0, sizeof(XmlPlateInfoResult));
	tInfo.iChannel = m_iChannelNO;
	tInfo.iIndex = ADD_PLATE_LIBRARY;
	memcpy(tInfo.cPlateLibName, strName.GetString(), strName.GetLength());
	memcpy(tInfo.cPlateLibRemark, strRemark.GetString(), strRemark.GetLength());
	int iRet = NetClient_XmlCmdConfig(m_iLogonID, NETXMLCMD_PLATELIB_ADD, &tInfo, sizeof(XmlPlateLibParas), &tOut, sizeof(XmlPlateInfoResult));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_PlateLibManage::NetClient_XmlCmdConfig[NETXMLCMD_PLATELIB_ADD] (%d)", m_iLogonID);
		GetPlateLibParas();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_PlateLibManage::NetClient_XmlCmdConfig[NETXMLCMD_PLATELIB_ADD] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_PlateLibManage::OnBnClickedButtonPlatelibSet()
{
	int iIndex = m_listPlateLib.GetNextItem(-1, LVNI_SELECTED);
	if(iIndex < 0)
	{
		return;
	}
	XmlPlateLibParas tInfo;
	memset(&tInfo, 0, sizeof(XmlPlateLibParas));
	tInfo.iChannel = m_iChannelNO;
	CString str = m_listPlateLib.GetItemText(iIndex, 0);
	tInfo.iIndex = atoi(str.GetBuffer());
	str.ReleaseBuffer();
	GetDlgItemText(IDC_EDIT_NAME, str);
	memcpy(tInfo.cPlateLibName, str.GetBuffer(), str.GetLength());
	GetDlgItemText(IDC_EDIT_REMARK, str);
	memcpy(tInfo.cPlateLibRemark, str.GetBuffer(), str.GetLength());

	int iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_PLATELIB_SET, &tInfo, sizeof(XmlPlateLibParas), NULL, 0);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_PlateLibManage::NetClient_XmlSetDevConfig[NETXMLCFG_PLATELIB_SET] (%d)", m_iLogonID);
		GetPlateLibParas();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_PlateLibManage::NetClient_XmlSetDevConfig[NETXMLCFG_PLATELIB_SET] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_PlateLibManage::OnBnClickedButtonPlatelibDelete()
{
	int iIndex = m_listPlateLib.GetNextItem(-1, LVNI_SELECTED);
	if(iIndex < 0)
	{
		return;
	}
	XmlPlateDeleteInfo tInfo;
	memset(&tInfo, 0, sizeof(XmlPlateDeleteInfo));
	tInfo.iChannel = m_iChannelNO;
	CString str = m_listPlateLib.GetItemText(iIndex, 0);
	tInfo.iIndex = atoi(str.GetBuffer());
	int iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_PLATELIB_DELETE, &tInfo, sizeof(XmlPlateDeleteInfo), NULL, 0);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_PlateLibManage::NetClient_XmlSetDevConfig[NETXMLCFG_PLATELIB_DELETE] (%d)", m_iLogonID);
		GetPlateLibParas();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_PlateLibManage::NetClient_XmlSetDevConfig[NETXMLCFG_PLATELIB_DELETE] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}

// Click on a line to display the content in edit
void CLS_PlateLibManage::OnLvnItemchangedListPlatelibrary(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
	if(pNMLV->uChanged == LVIF_STATE)
	{
		if(pNMLV->uNewState)
		{
			int iIndex = pNMLV->iItem;
			CString str;
			str = m_listPlateLib.GetItemText(iIndex, 1);
			SetDlgItemText(IDC_EDIT_NAME, str);
			str = m_listPlateLib.GetItemText(iIndex, 2);
			SetDlgItemText(IDC_EDIT_REMARK, str);
		}
	}

	*pResult = 0;
}

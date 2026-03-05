// HardDiskManage.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "HardDiskManage.h"

typedef enum tagE_Disk_Info_Column
{
	Disk_Info_Column_No = 0, 
	Disk_Info_Column_Name, 
	Disk_Info_Column_Max, 
}E_Disk_Info_Column;

// CLS_HardDiskManage dialog

IMPLEMENT_DYNAMIC(CLS_HardDiskManage, CDialog)

CLS_HardDiskManage::CLS_HardDiskManage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_HardDiskManage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1; 
}

CLS_HardDiskManage::~CLS_HardDiskManage()
{
}

void CLS_HardDiskManage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_HARDDISK, m_lstHardDisk);
	DDX_Control(pDX, IDC_LIST_DISKINFO_EX, m_ctrListDiskInfoEx);
}


BEGIN_MESSAGE_MAP(CLS_HardDiskManage, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_CLEARDISK, &CLS_HardDiskManage::OnBnClickedButtonCleardisk)
	ON_BN_CLICKED(IDC_BUTTON_DISKINFO_QUERY, &CLS_HardDiskManage::OnBnClickedButtonDiskinfoQuery)
END_MESSAGE_MAP()


// CLS_HardDiskManage message handler

BOOL CLS_HardDiskManage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  add extra initialization here
	m_lstHardDisk.SetExtendedStyle(LVS_EX_FULLROWSELECT| LVS_EX_CHECKBOXES |LVS_EX_GRIDLINES|LVS_EX_ONECLICKACTIVATE);
	m_lstHardDisk.InsertColumn(0, GetTextByLan(_T("编号"), _T("Number")), LVCFMT_CENTER, 80);
	m_lstHardDisk.InsertColumn(1, GetTextByLan(_T("类型"), _T("Type")), LVCFMT_CENTER, 80);
	m_lstHardDisk.InsertColumn(2, GetTextByLan(_T("大小"), _T("Total Space")), LVCFMT_CENTER, 120);
	m_lstHardDisk.InsertColumn(3, GetTextByLan(_T("已用"), _T("Used Space")), LVCFMT_CENTER, 100);
	m_lstHardDisk.InsertColumn(4, GetTextByLan(_T("空余"), _T("Free Space")), LVCFMT_CENTER, 100);
	m_lstHardDisk.InsertColumn(5, GetTextByLan(_T("状态"), _T("State")), LVCFMT_CENTER, 100);
	m_lstHardDisk.InsertColumn(6, GetTextByLan(_T("用途"), _T("Usage")), LVCFMT_CENTER, 80);

	m_ctrListDiskInfoEx.SetExtendedStyle(LVS_EX_FULLROWSELECT |LVS_EX_GRIDLINES | LVS_EX_ONECLICKACTIVATE);
	m_ctrListDiskInfoEx.InsertColumn(Disk_Info_Column_No, GetTextByLan(_T("编号"), _T("No")), LVCFMT_CENTER, 80);
	m_ctrListDiskInfoEx.InsertColumn(Disk_Info_Column_Name, GetTextByLan(_T("名称"), _T("Name")), LVCFMT_CENTER, 80);

	GetDlgItem(IDC_BUTTON_DISKINFO_QUERY)->SetWindowText(GetTextByLan(_T("查询"), _T("Query")));
	GetDlgItem(IDC_STATIC_DISKINFO_EX)->SetWindowText(GetTextByLan(_T("磁盘扩展信息"), _T("Disk expension information")));
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_HardDiskManage::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	m_iStreamNo = _iStreamNo;

	UI_UpdateHardDiskInfo();
}

void CLS_HardDiskManage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateHardDiskInfo();
}

void CLS_HardDiskManage::OnMainNotify(int _ulLogonID, int _iWparam, void* _pvLParam, void* _pvUser)
{

}

CString GetStrByType(int _iType)
{
	//HDD Type: 0-Local SATA HDD, 1-ESATA HDD, 2-SD Card, 3-USB, 4-NFS HDD, 5-RAID Virtual Disk, 6-IPSAN
	CString cstrType;
	switch (_iType)
	{
	case HARDDISK_TYPE_LOCALSATA:
		cstrType = "SATA";
		break;
	case HARDDISK_TYPE_ESATA:
		cstrType = "eSATA";
		break;
	case HARDDISK_TYPE_SD:
		cstrType = "SD";
		break;
	case HARDDISK_TYPE_USB:
		cstrType = "USB";
		break;
	case HARDDISK_TYPE_NFS:
		cstrType = "NFS";
		break;
	case HARDDISK_TYPE_RAIDVD:
		cstrType = "RAID";
		break;
	case HARDDISK_TYPE_IPSAN:
		cstrType = "IPSAN";
		break;
	default:
		break;
	}

	return cstrType;
}

CString GetStrBySpace(unsigned int _iSpace)
{
	CString strSpace(_T(""));

	if (_iSpace >= 0 && _iSpace < 1024)
	{
		strSpace.Format(_T("%d%s"),_iSpace,"(M)");
	}
	else if (_iSpace >= 1024)
	{
		float fDiskSpace = (float)_iSpace/1024;
		strSpace.Format(_T("%.3f%s"), fDiskSpace, "(G)");
	}

	return strSpace;
}

CString GetStrByStatus(unsigned short _usStatus)
{
	//Disk status: 0, no disk; 1, unformatted; 2, formatted; 3, mounted; 4, reading and writing
	CString cstrType;
	switch (_usStatus)
	{
	case HARDDISK_STATUS_NODISK:
		cstrType = GetTextByLan(_T("无磁盘"), _T("NoDisk"));
		break;
	case HARDDISK_STATUS_UNFORMAT:
		cstrType = GetTextByLan(_T("未格式化"), _T("Unformat"));
		break;
	case HARDDISK_STATUS_FORMATTED:
		cstrType = GetTextByLan(_T("已格式化"), _T("Formatted"));
		break;
	case HARDDISK_STATUS_MOUNTED:
		cstrType = GetTextByLan(_T("已挂载"), _T("Mounted"));
		break;
	case HARDDISK_STATUS_READANDWRITE:
		cstrType = GetTextByLan(_T("读写中"), _T("Reading and writing"));
		break;
	default:
		break;
	}

	return cstrType;
}

CString GetStrByUsage(unsigned short _usUsage)
{
	//Disk usage: 0, recording; 1, backup; 2, redundancy; 3, disk read only
	CString cstrType;
	switch (_usUsage)
	{
	case HARDDISK_USAGE_RECORDING:
		cstrType = GetTextByLan(_T("录像"), _T("Recording"));
			break;
	case HARDDISK_USAGE_BACKUP:
		cstrType = GetTextByLan(_T("备份"), _T("Backup"));
			break;
	case HARDDISK_USAGE_REDUNDANCE:
		cstrType = GetTextByLan(_T("冗余"), _T("Redundance"));
			break;
	case HARDDISK_USAGE_ONLYREAD:
		cstrType = GetTextByLan(_T("只读"), _T("OnlyRead"));
			break;
	default:
		break;
	}

	return cstrType;
}

void CLS_HardDiskManage::UI_UpdateHardDiskInfo()
{
	if (m_iLogonID < 0) {
		return;
	}

	if (NULL == NetClient_GetDevConfig_V5) {
		AddLog(LOG_TYPE_FAIL, "", "NULL == NetClient_GetDevConfig_V5");
		return;
	}

	int iRet = RET_FAILED;
	HardDiskInfo tDisk = {0};
	iRet = NetClient_GetDevConfig_V5(m_iLogonID, NETCLIENT_GET_HARDDISKINFO_V5, NULL, 0, &tDisk, sizeof(HardDiskInfo));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NETCLIENT_V5_GET_HARDDISKINFO fail!(%d)", iRet);
		return;
	}

	m_lstHardDisk.DeleteAllItems();
	int iItem = -1;
	CString cstrTmp;
	for (int i = 0; i < tDisk.iTotalDiskCount && i < MAX_HARDDISK_COUNT; ++i)
	{
		SingleHardDisk&	tSingle = tDisk.tHardDiskArray[i];
		iItem = m_lstHardDisk.GetItemCount();

		//disk number
		cstrTmp.Format(_T("%d"), tSingle.iDiskNumber);
		m_lstHardDisk.InsertItem(iItem, cstrTmp);
		m_lstHardDisk.SetItemData(iItem, tSingle.iDiskNumber);

		//disk type
		cstrTmp = GetStrByType (tSingle.iDiskType);
		m_lstHardDisk.SetItemText(iItem, 1, cstrTmp);

		//Total disk space
		cstrTmp = GetStrBySpace(tSingle.uiTotalSpace);
		m_lstHardDisk.SetItemText(iItem, 2, cstrTmp);

		// disk space available
		cstrTmp = GetStrBySpace(tSingle.uiTotalSpace - tSingle.uiFreeSpace);
		m_lstHardDisk.SetItemText(iItem, 3, cstrTmp);

		//Disk remaining space
		cstrTmp = GetStrBySpace(tSingle.uiFreeSpace);
		m_lstHardDisk.SetItemText(iItem, 4, cstrTmp);

		//disk status
		cstrTmp = GetStrByStatus(tSingle.usStatus);
		m_lstHardDisk.SetItemText(iItem, 5, cstrTmp);

		//disk usage
		cstrTmp = GetStrByUsage(tSingle.usUsage);
		m_lstHardDisk.SetItemText(iItem, 6, cstrTmp);
	}
}


void CLS_HardDiskManage::OnBnClickedButtonCleardisk()
{
	// TODO: Add your control notification handler code here
	for (int i = 0; i < m_lstHardDisk.GetItemCount(); i++)
	{
		if (TRUE == m_lstHardDisk.GetCheck(i))
		{
			CString cstrDiskNo = m_lstHardDisk.GetItemText(i,0);
			int iDiskNo = _ttoi(cstrDiskNo);
			int iRet = NetClient_DiskPart(m_iLogonID, iDiskNo, 1, 1);
			if (RET_SUCCESS == iRet)
			{
				AddLog(LOG_TYPE_SUCC, "", "NetClient_ClearDisk(%d) success", iDiskNo);
			}
			else
				AddLog(LOG_TYPE_FAIL, "", "NetClient_ClearDisk(%d) failed", iDiskNo);
		}
	}
}

void CLS_HardDiskManage::OnBnClickedButtonDiskinfoQuery()
{
	DiskInfosRealTimeArr tDiskInfoEx = {0};
	tDiskInfoEx.iSize = sizeof(tDiskInfoEx);
	m_ctrListDiskInfoEx.DeleteAllItems();
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_PARAM_DISKINFOREALTIME, 0, &tDiskInfoEx, sizeof(tDiskInfoEx), NULL);
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig m_iLogonID:%d, cmd:%d  failed", m_iLogonID, NET_CLIENT_PARAM_DISKINFOREALTIME);
		return;
	}
	AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig cmd: NET_CLIENT_PARAM_DISKINFOREALTIME, m_iLogonID = %d, success", m_iLogonID);
	for (int i = 0; i < tDiskInfoEx.iDiskNum && i < DISK_NUM_MAX; i++)
	{
		CString strNo;
		int iItemIndex = m_ctrListDiskInfoEx.GetItemCount();
		strNo.Format(_T("%d"), tDiskInfoEx.tDiskInfos[i].iDiskNo);
		m_ctrListDiskInfoEx.InsertItem(iItemIndex, strNo);
		m_ctrListDiskInfoEx.SetItemText(iItemIndex, Disk_Info_Column_Name, CString(tDiskInfoEx.tDiskInfos[i].cDiskName));
	}
}

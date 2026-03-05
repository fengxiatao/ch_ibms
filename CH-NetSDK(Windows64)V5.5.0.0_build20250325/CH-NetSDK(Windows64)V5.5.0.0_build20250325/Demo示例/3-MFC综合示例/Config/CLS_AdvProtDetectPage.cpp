// CLS_AdvProtDetectPage.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "./CLS_AdvProtDetectPage.h"


// CLS_AdvProtDetectPage dialog

IMPLEMENT_DYNAMIC(CLS_AdvProtDetectPage, CDialog)

CLS_AdvProtDetectPage::CLS_AdvProtDetectPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_AdvProtDetectPage::IDD, pParent)
{

}

CLS_AdvProtDetectPage::~CLS_AdvProtDetectPage()
{
}

BOOL CLS_AdvProtDetectPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_listLogonSubmitProtInfo.SetExtendedStyle(LVS_EX_GRIDLINES);
	UI_UpdateText();
	
	return TRUE;
}

void CLS_AdvProtDetectPage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	ShowListItem();
	GetKernelVersion();
	GetModelAndType();
}

void CLS_AdvProtDetectPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_LOGON_SUBMIT_INFO, m_listLogonSubmitProtInfo);
}


void CLS_AdvProtDetectPage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateText();
}

void CLS_AdvProtDetectPage::UI_UpdateText()
{
	SetDlgItemText(IDC_STATIC_DEVICE_TYPE, GetTextByLan(_T("设备类别"), _T("Device Type")));
	SetDlgItemText(IDC_STATIC_DEVICE_MODEL, GetTextByLan(_T("设备型号"), _T("Device Model")));
	SetDlgItemText(IDC_STATIC_KERNEL_VERSION, GetTextByLan(_T("内核版本"), _T("Kernel Version")));
	SetDlgItemText(IDC_STATIC_LOGON_SUBMIT_PROT, GetTextByLan(_T("登录上报协议"), _T("Logon Submit Prot")));
	SetDlgItemText(IDC_STATIC_PIECE, GetTextByLan(_T("条"), _T("Piece")));
	SetDlgItemText(IDC_STATIC_TIPS, GetTextByLan(_T("设备登录成功30s以后可以查看"), _T("The device is successfully logged in and can be viewed after 30s")));

	while(m_listLogonSubmitProtInfo.DeleteColumn(0));

	m_listLogonSubmitProtInfo.InsertColumn(0, GetTextByLan(_T("预留"), _T("Reserve")), LVCFMT_CENTER, 0, 0);
	m_listLogonSubmitProtInfo.InsertColumn(1, GetTextByLan(_T("一级关键字"), _T("Primary Keyword")), LVCFMT_CENTER, 183, 1);
	m_listLogonSubmitProtInfo.InsertColumn(2, GetTextByLan(_T("二级关键字"), _T("Secondary Keyword")), LVCFMT_CENTER, 183, 2);
	m_listLogonSubmitProtInfo.InsertColumn(3, GetTextByLan(_T("协议个数"), _T("Protocol Num")), LVCFMT_CENTER, 183, 3);
	m_listLogonSubmitProtInfo.DeleteColumn(0);
	ShowListItem();
	GetKernelVersion();
	GetModelAndType();
}

void CLS_AdvProtDetectPage::ShowListItem()
{
	//First find the IP address of the currently selected channel, spell out the string, form the file name, open the file, locate the position of the statistical data, and start insertItem by line
	//1. Read the data statistics of the TXT file
	//2. Insert data into list
	if(m_iLogonID < 0)
	{
		return;
	}
	SetDlgItemText(IDC_EDIT_LOGON_SUBMIT_NUM, _T(""));
	m_listLogonSubmitProtInfo.DeleteAllItems();
	int iListLine = 0;
	CString strFilePath;							//target file path to read
	char cFilePath[MAX_PATH] = {0};
	int iSize = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
	if (iSize <= 0)
	{
		strcpy_s(cFilePath,sizeof(cFilePath),"C:\\");
	}
	strFilePath.Format(_T("%s"),cFilePath);
	int iPos = strFilePath.ReverseFind('\\');		//Find a character from the end of a larger string
	if (iPos >= 0)
	{
		strFilePath = strFilePath.Left(iPos);		//Extract the left part of a string
	}
	strFilePath += "\\NetClientDemo\\NetProtocolStatistics";

	PDEVICE_INFO pDevice = FindDevice(m_iLogonID);
	if(NULL == pDevice)
	{
		return;
	}
	if(clock() - pDevice->clockStartRecvProtTime < 30 * CLOCKS_PER_SEC)
	{
		return;
	}
	strFilePath += pDevice->cIP;
	strFilePath += ".txt";
	CStdioFile fReadFile;
	BOOL blIsExist = fReadFile.Open(strFilePath, CFile::modeReadWrite);
	if(!blIsExist)
	{
		return;
	}
	CString strLineData;
	while (fReadFile.ReadString(strLineData))
	{
		if(strLineData.IsEmpty())
		{
			continue;
		}
		else
		{
			int iIndex = 0;
			if('[' == strLineData.GetAt(iIndex++))							//offset the first'['
			{
				CString strFirstKey;
				CString strSecondKey;
				for(; iIndex < strLineData.GetLength() && ']' != strLineData.GetAt(iIndex); iIndex++)
				{
					strFirstKey += strLineData.GetAt(iIndex);				//Read the first-level keywords
				}
				if("StatisticalReport" == strFirstKey)
				{
					continue;
				}
				if("TotalProtocolCount" == strFirstKey)
				{
					iIndex += 2;
					CString strTotal;
					for(int i = 0; i < strLineData.GetLength() - iIndex; i++)
					{
						strTotal += strLineData.GetAt(iIndex + i);
					}
					SetDlgItemText(IDC_EDIT_LOGON_SUBMIT_NUM, strTotal);
					continue;
				}
				iIndex++;													//Offset to the next bit of ']'
				if(iIndex < strLineData.GetLength())
				{
					switch(strLineData[iIndex])
					{
					case '[':
						{
							iIndex++;
							for(; iIndex < strLineData.GetLength() && ']' != strLineData.GetAt(iIndex); iIndex++)
							{
								strSecondKey += strLineData.GetAt(iIndex);	//read secondary keywords
							}
							iIndex += 2;									//Offset ']' and ' ', to the beginning of the number
						}
						break;
					case ' ':
						{
							iIndex++;										//offset away' '
						}
						break;
					default:
						break;
					}
					if(iIndex < strLineData.GetLength())
					{
						CString strCount;
						for(int i = 0; i < strLineData.GetLength() - iIndex; i++)
						{
							strCount += strLineData.GetAt(iIndex + i);
						}
						m_listLogonSubmitProtInfo.InsertItem(iListLine, strFirstKey);
						m_listLogonSubmitProtInfo.SetItemText(iListLine, 1, strSecondKey);
						m_listLogonSubmitProtInfo.SetItemText(iListLine, 2, strCount);
						iListLine++;
					}
				}
			}
		}
	}
	fReadFile.Close();
}

void CLS_AdvProtDetectPage::GetKernelVersion()
{
	if (m_iLogonID < 0)
	{
		return;
	}
	SERVER_VERSION version = {0};
	version.m_iStructSize = sizeof(SERVER_VERSION);
	int iRet = NetClient_GetServerVersion_V1(m_iLogonID, &version);
	if(RET_SUCCESS != iRet)
	{
		return;
	}
	if(0 == strlen(version.m_cVersionEx)) {
		SetDlgItemText(IDC_EDIT_KERNEL_VERSION, version.m_cVersion);
	} else {
		SetDlgItemText(IDC_EDIT_KERNEL_VERSION, version.m_cVersionEx);
	}
}

void CLS_AdvProtDetectPage::GetModelAndType()
{
	if (m_iLogonID < 0)
	{
		return;
	}
	CString strFilePath;							//target file path to read
	char cFilePath[MAX_PATH] = {0};
	int iSize = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
	if (iSize <= 0)
	{
		strcpy_s(cFilePath,sizeof(cFilePath),"C:\\");
	}
	strFilePath.Format(_T("%s"),cFilePath);
	int iPos = strFilePath.ReverseFind('\\');		//Find a character from the end of a larger string
	if (iPos >= 0)
	{
		strFilePath = strFilePath.Left(iPos);		//Extract the left part of a string
	}
	strFilePath += "\\ProductModel.ini";

	int iProductMode = RET_FAILED;
	int iProductType = RET_FAILED;
	int iRet = NetClient_GetProductTypeEx(m_iLogonID, &iProductMode, &iProductType);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetProductTypeEx (%d,%d,%d)",m_iLogonID,iProductMode,iProductType);
		CString strModeNum;
		iProductMode = iProductMode << 16;
		iProductMode = iProductMode >> 16;
		strModeNum.Format("%04x", iProductMode);						//The lower 16 bits of Product Model represent the product model, which is taken out by shifting
		strModeNum = CString("0x") + strModeNum;
		SetDlgItemText(IDC_EDIT_DEVICE_MODEL_NUM, strModeNum);			//Displays the corresponding ID of the device model
		switch(iProductType)
		{
		case IPCamera_PRODUCT:
			{
				//The IPC type is IPC and then get the number of channels to determine whether it is single channel or multi-channel
				int iChannelNum = NO_CHANNEL;
				iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
				if (RET_SUCCESS == iRet && NO_CHANNEL != iChannelNum) {
					if(1 == iChannelNum) {
						SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("单通道IPC"), _T("Single Channel IPC")));
					} else {
						SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("多通道IPC"), _T("Multi Channel IPC")));
					}
					strModeNum = CString(_T("ID")) + strModeNum;
					char cModelName[LEN_64];
					memset(cModelName, 0, sizeof(cModelName));
					GetPrivateProfileString(LPCTSTR(_T("IPC")), (LPCTSTR)(strModeNum.GetBuffer()), GetTextByLan(_T("未知"), _T("UnKhown")), cModelName, LEN_64, strFilePath);
					SetDlgItemText(IDC_EDIT_DEVICE_MODEL, cModelName);
					AddLog(LOG_TYPE_SUCC, "", "NetClient_GetChannelNum (%d,%d)", m_iLogonID, iChannelNum);
				} else {
					AddLog(LOG_TYPE_FAIL, "", "NetClient_GetChannelNum (%d,%d)", m_iLogonID, iChannelNum);
				}
			}
			break;
		case NVRecord_PRODUCT:
			{
				//NVR distinguishes which type of NVR by ID value
				//Traverse ini files
				strModeNum = CString(_T("ID")) + strModeNum;
				char cModelName[LEN_64];
				memset(cModelName, 0, sizeof(cModelName));
				GetPrivateProfileString(LPCTSTR(_T("NVR_ZF")), (LPCTSTR)(strModeNum.GetBuffer()), GetTextByLan(_T("未知"), _T("UnKhown")), cModelName, LEN_64, strFilePath);
				if(0 == strlen(cModelName))
				{
					GetPrivateProfileString(LPCTSTR(_T("NVR_ITS")), (LPCTSTR)(strModeNum.GetBuffer()), GetTextByLan(_T("未知"), _T("UnKhown")), cModelName, LEN_64, strFilePath);
					if(0 == strlen(cModelName))
					{
						GetPrivateProfileString(LPCTSTR(_T("NVR_STANDARD")), (LPCTSTR)(strModeNum.GetBuffer()), GetTextByLan(_T("未知"), _T("UnKhown")), cModelName, LEN_64, strFilePath);
						if(0 == strlen(cModelName))
						{
							//If the NVR device is reported, but there is no match in the corresponding field of the NVR, it means that it is of another type, which is stored in the IPC segment of the ini
							GetPrivateProfileString(LPCTSTR(_T("IPC")), (LPCTSTR)(strModeNum.GetBuffer()), GetTextByLan(_T("未知"), _T("UnKhown")), cModelName, LEN_64, strFilePath);
							SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("其他"), _T("Other")));
						}
						else
						{
							SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("标准NVR"), _T("Standard Host")));
						}
					}
					else
					{
						SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("交通主机"), _T("Traffic Host")));
					}
				}
				else
				{
					SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("政法主机"), _T("Political and Legal Host")));
				}
				SetDlgItemText(IDC_EDIT_DEVICE_MODEL, cModelName);
			}
			break;
		default:
			{
				SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("未知"), _T("UnKnown")));
			}
			break;
		}
	}
	else
	{
		SetDlgItemText(IDC_EDIT_DEVICE_TYPE, GetTextByLan(_T("获取失败"), _T("Get Failed")));
		SetDlgItemText(IDC_EDIT_DEVICE_MODEL, GetTextByLan(_T("获取失败"), _T("Get Failed")));
		SetDlgItemText(IDC_EDIT_DEVICE_MODEL_NUM, GetTextByLan(_T("获取失败"), _T("Get Failed")));
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetProductTypeEx (%d,%d,%d)",m_iLogonID,iProductMode,iProductType);
	}
}

BEGIN_MESSAGE_MAP(CLS_AdvProtDetectPage, CDialog)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// CLS_AdvProtDetectPage message handler

void CLS_AdvProtDetectPage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	UI_UpdateText();
}

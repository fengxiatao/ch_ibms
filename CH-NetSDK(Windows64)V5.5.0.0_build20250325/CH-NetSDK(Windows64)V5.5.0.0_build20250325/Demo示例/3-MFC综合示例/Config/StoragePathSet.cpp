// CLS_StoragePathSetPage.cpp : implementation file
//

#include "stdafx.h"
#include "StoragePathSet.h"

#define PTAH_TYPE_SDUSB 1
#define PTAH_TYPE_NFS   2
#define PTAH_TYPE_FTP   3

// CLS_StoragePathSetPage dialog

IMPLEMENT_DYNAMIC(CLS_StoragePathSetPage, CDialog)

CLS_StoragePathSetPage::CLS_StoragePathSetPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_StoragePathSetPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
}

CLS_StoragePathSetPage::~CLS_StoragePathSetPage()
{
}

void CLS_StoragePathSetPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_STORAGE_PATH, m_cboPathType);
	DDX_Control(pDX, IDC_EDIT_STORAGE_FILEPATH, m_etFilePath);
	DDX_Control(pDX, IDC_EDIT_STORAGE_FILEMD5, m_etFileMd5);
	DDX_Control(pDX, IDC_STXT_STORAGE_CHECKRESULT, m_stcResult);
	DDX_Control(pDX, IDC_EDIT_STORAGE_FILESIZE, m_etFileSize);
	DDX_Control(pDX, IDC_EDIT_STORAGE_PROGRESS, m_etCheckProgress);
}

BEGIN_MESSAGE_MAP(CLS_StoragePathSetPage, CLS_BasePage)
	ON_BN_CLICKED(IDC_BTN_STORAGE_SET, &CLS_StoragePathSetPage::OnBnClickedButtonSetPath)
	ON_WM_DESTROY()
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_STORAGE_CHECK, &CLS_StoragePathSetPage::OnBnClickedBtnStorageCheck)
END_MESSAGE_MAP()

// CLS_VideoEncodeSlicePage message handlers

BOOL CLS_StoragePathSetPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();
	m_cboPathType.SetCurSel(0);

	return TRUE; 
}

void CLS_StoragePathSetPage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	UI_UpdateStoragePathType();

}

void CLS_StoragePathSetPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	if (_iStreamNo < 0)
	{
		m_iStreamNo = 0;
	}
	else
	{
		m_iStreamNo = _iStreamNo;
	}
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
	
	UI_UpdateStoragePathType();
}

void CLS_StoragePathSetPage::OnBnClickedButtonSetPath()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	if (m_iStreamNo > BASIC_STREAM_TYPES)
	{
		AddLog(LOG_TYPE_MSG,"","Illegal stream number %d", m_iStreamNo);
		return;
	}
	LocalStorePath tLocalStorePath = {0};
	tLocalStorePath.iSize = sizeof(tLocalStorePath);
	tLocalStorePath.iEnable = m_cboPathType.GetItemData(m_cboPathType.GetCurSel());
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_LOCAL_STORE_PATH, m_iChannelNo, &tLocalStorePath, sizeof(tLocalStorePath));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_LOCAL_STORE_PATH](%d,%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iStreamNo,tLocalStorePath.iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig[NET_CLIENT_LOCAL_STORE_PATH](%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iStreamNo);
	}
}

BOOL CLS_StoragePathSetPage::UI_UpdateStoragePathType()
{
	if (m_iLogonID < 0)
		return FALSE;
	if (m_iStreamNo > BASIC_STREAM_TYPES)
	{
		AddLog(LOG_TYPE_MSG,"","Illegal stream number %d", m_iStreamNo);
		return FALSE;
	}
	LocalStorePath tLocalStorePath = {0};
	tLocalStorePath.iSize = sizeof(tLocalStorePath);
	int iByteReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_LOCAL_STORE_PATH, m_iChannelNo, &tLocalStorePath, sizeof(tLocalStorePath), &iByteReturn);
	if (RET_FAILED == iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig[NET_CLIENT_LOCAL_STORE_PATH](%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iStreamNo);
	}
	else
	{
		m_cboPathType.SetCurSel(GetCboSel(&m_cboPathType, tLocalStorePath.iEnable));
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[NET_CLIENT_LOCAL_STORE_PATH](%d,%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iStreamNo,tLocalStorePath.iEnable);
	}
	return TRUE;
}

void CLS_StoragePathSetPage::UI_UpdateDialog()
{
	m_cboPathType.ResetContent();
	m_cboPathType.SetItemData(m_cboPathType.AddString("SD/USB"), PTAH_TYPE_SDUSB);
	m_cboPathType.SetItemData(m_cboPathType.AddString("NFS"), PTAH_TYPE_NFS);
	m_cboPathType.SetItemData(m_cboPathType.AddString("FTP"), PTAH_TYPE_FTP);
	m_cboPathType.SetCurSel(0);

	SetDlgItemTextEx(IDC_STXT_STORAGE_PATH, IDS_STORAGE_PATH);
	SetDlgItemTextEx(IDC_STXT_PATH_CHOOSE, IDS_PATH_CHOOSE);
	SetDlgItemTextEx(IDC_BTN_STORAGE_SET, IDS_SET);

	SetDlgItemTextEx(IDC_STXT_STORAGE_FILECHECK, IDS_STORAGE_FILECHECK);
	SetDlgItemTextEx(IDC_STXT_STORAGE_FILEPATH, IDS_STORAGE_FILEPATH);
	SetDlgItemTextEx(IDC_STXT_STORAGE_FILEMD5, IDS_STORAGE_FILEMD5);
	SetDlgItemTextEx(IDC_BTN_STORAGE_CHECK, IDS_STORAGE_CHECK);
	SetDlgItemTextEx(IDC_STXT_STORAGE_FILESIZE, IDS_STORAGE_CHECK_FILE_SIZE);
	SetDlgItemTextEx(IDC_STXT_STORAGE_PROGRESS, IDS_STXT_STORAGE_PROGRESS);
}

void CLS_StoragePathSetPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();
	UI_UpdateStoragePathType();
}

void CLS_StoragePathSetPage::OnBnClickedBtnStorageCheck()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Please First login");
		return;
	}

	CheckDiskFile stDiskFileInfo = {0};
	CheckDiskFileResponse stChekResult = {0};
	
	CString strPath;
	CString strSize;
	CString strMD5;
	CString strErrMsg;
	m_etFilePath.GetWindowTextA(strPath);
	m_etFileSize.GetWindowTextA(strSize);
	m_etFileMd5.GetWindowTextA(strMD5);
	int iCount = strPath.GetLength();
	if (LEN_256 < strPath.GetLength())
	{
		strErrMsg.Format(_T("%s%d%s"), 
			CLS_LanguageManager::Instance()->GetText(IDS_TEXT_FILE_PATH_LEN_MAX), 
			LEN_256, CLS_LanguageManager::Instance()->GetText(IDS_TEXT_BYTE));
		AfxMessageBox(strErrMsg);
		return;
	}
	//CString strErrMsg(CLS_LanguageManager::Instance()->GetText(IDS_STXT_STORAGE_PROGRESS));
	//BOOL bRet = strText.LoadString(NULL,_uIDResource,m_wLanguage);
	if (LEN_64 < strMD5.GetLength())
	{
		strErrMsg.Format(_T("%s%d%s"), 
			CLS_LanguageManager::Instance()->GetText(IDS_TEXT_MD5_LEN_MAX), 
			LEN_64, CLS_LanguageManager::Instance()->GetText(IDS_TEXT_BYTE));
		AfxMessageBox(strErrMsg);
		return;
	}
	strcpy(stDiskFileInfo.cFilePath, strPath);
	strcpy(stDiskFileInfo.cFileMD5, strMD5);
	stDiskFileInfo.ullFileSize = (unsigned long long)_atoi64(strSize);
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_CHECK_DISKFILE, 0, &stDiskFileInfo, 
		sizeof(stDiskFileInfo), &stChekResult, sizeof(stChekResult));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig[NET_CLIENT_LOCAL_STORE_PATH](%d, return iRet = %d)", m_iLogonID, iRet);
		return;
	}
	ShowFileCheckResult(stChekResult.iResult, stChekResult.cFilePath, 0);
}

void CLS_StoragePathSetPage::OnMainNotify(int _iLogonID, int _wParam, void* _iLParam, void* _iUser)
{
	if (WCM_CHECKFILE_PROGRESS != _wParam)
	{
		return;
	}
	if (NULL == _iLParam)
	{
		return;
	}
	CheckDiskFileProgress *pProgress = (CheckDiskFileProgress*)_iLParam;
	ShowFileCheckResult(pProgress->iResult, pProgress->cFilePath, pProgress->iProgress);
}

void CLS_StoragePathSetPage::ShowFileCheckResult(int _iState, const char *_pFilePath, int _iProgress)
{
	//0，正在上报校验进度；1，成功；-1，长度不正确；-2，MD5值不正确；-3，超过最大校验任务数量（需要重新开始请求）；-4，文件不存在
	CString strMsg;
	switch(_iState)
	{
	case 0:
		strMsg.Format(_T("%s:%d"), CLS_LanguageManager::Instance()->GetText(IDS_TEXT_CHECK_PROGRESS), _iProgress);
		m_etCheckProgress.SetWindowText(strMsg);
		break;
	case 1:
		m_etCheckProgress.SetWindowText(CLS_LanguageManager::Instance()->GetText(IDS_TEXT_CHECK_SUCCESS));
		break;
	case -1:
		m_etCheckProgress.SetWindowText(CLS_LanguageManager::Instance()->GetText(IDS_TEXT_FILE_LEN_ERROR));
		break;
	case -2:
		m_etCheckProgress.SetWindowText(CLS_LanguageManager::Instance()->GetText(IDS_TEXT_MD5_ERROR));
		break;
	case -3:
		m_etCheckProgress.SetWindowText(CLS_LanguageManager::Instance()->GetText(IDS_TEXT_TASKS_EXCEEDED));
		break;
	case -4:
		m_etCheckProgress.SetWindowText(CLS_LanguageManager::Instance()->GetText(IDS_TEXT_FILE_DOESNT_EXIST));
		break;
	default:
		AddLog(LOG_TYPE_FAIL,"","ShowFileCheckResult[NET_CLIENT_LOCAL_STORE_PATH](do not support state = %d)", m_iLogonID, _iState);
		break;
	};
}

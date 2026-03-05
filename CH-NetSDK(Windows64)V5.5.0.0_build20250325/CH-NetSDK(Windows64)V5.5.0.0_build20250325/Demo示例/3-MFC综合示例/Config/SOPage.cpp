// Config/SOPage.cpp : implementation file
//

#include "stdafx.h"
#include "SOPage.h"

// CLS_SOPage dialog

IMPLEMENT_DYNAMIC(CLS_SOPage, CDialog)
const int CONST_INT_SERVICE_INDEX0 = 0;
const int CONST_INT_SERVICE_INDEX1 = 1;
const int CONST_INT_SERVICE_INDEX2 = 2;
const int CONST_INT_SERVICE_INDEX3 = 3;
const int CONST_INT_SERVICE_INDEX4 = 4;
const int CONST_INT_SERVICE_INDEX5 = 5;
const int CONST_INT_SERVICE_INDEX6 = 6;
const int CONST_INT_SERVICE_INDEX7 = 7;
const int CONST_INT_SERVICE_INDEX8 = 8;
const int CONST_INT_SERVICE_INDEX9 = 9;
#define SUPPORT_MUTIPLAT 1

CLS_SOPage::CLS_SOPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_SOPage::IDD, pParent)
{
	m_iLogonID = -1;
}

CLS_SOPage::~CLS_SOPage()
{
}

void CLS_SOPage::DoDataExchange(CDataExchange* pDX)
{
	 CLS_BasePage::DoDataExchange(pDX);
	 DDX_Control(pDX, IDC_COMBO_CONFIG_SO, m_cboPlatformSO);
	 DDX_Control(pDX, IDC_CHK_SERVICE1, m_chkArrService[0]);
	 DDX_Control(pDX, IDC_CHK_SERVICE2, m_chkArrService[1]);
	 DDX_Control(pDX, IDC_CHK_SERVICE3, m_chkArrService[2]);
	 DDX_Control(pDX, IDC_CHK_SERVICE4, m_chkArrService[3]);
	 DDX_Control(pDX, IDC_CHK_SERVICE5, m_chkArrService[4]);
	 DDX_Control(pDX, IDC_CHK_SERVICE6, m_chkArrService[5]);
	 DDX_Control(pDX, IDC_CHK_SERVICE7, m_chkArrService[6]);
	 DDX_Control(pDX, IDC_CHK_SERVICE8, m_chkArrService[7]);
	 DDX_Control(pDX, IDC_CHK_SERVICE9, m_chkArrService[8]);
	 DDX_Control(pDX, IDC_CHK_SERVICE10, m_chkArrService[9]);
	 DDX_Control(pDX, IDC_CHECK_ONVIF_H265SUPPORT, m_chkOnvifH265Support);

}


BEGIN_MESSAGE_MAP(CLS_SOPage,  CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_SO_RUN, &CLS_SOPage::OnBnClickedButtonSoRun)
	ON_BN_CLICKED(IDC_BUTTON_SO_STOP, &CLS_SOPage::OnBnClickedButtonSoStop)
	ON_BN_CLICKED(IDC_CHK_SERVICE1, &CLS_SOPage::OnBnClickedChkService1)
	ON_BN_CLICKED(IDC_CHK_SERVICE2, &CLS_SOPage::OnBnClickedChkService2)
	ON_BN_CLICKED(IDC_CHK_SERVICE3, &CLS_SOPage::OnBnClickedChkService3)
	ON_BN_CLICKED(IDC_CHK_SERVICE4, &CLS_SOPage::OnBnClickedChkService4)
	ON_BN_CLICKED(IDC_CHK_SERVICE5, &CLS_SOPage::OnBnClickedChkService5)
	ON_BN_CLICKED(IDC_CHK_SERVICE6, &CLS_SOPage::OnBnClickedChkService6)
	ON_BN_CLICKED(IDC_CHK_SERVICE7, &CLS_SOPage::OnBnClickedChkService7)
	ON_BN_CLICKED(IDC_CHK_SERVICE8, &CLS_SOPage::OnBnClickedChkService8)
	ON_BN_CLICKED(IDC_CHK_SERVICE9, &CLS_SOPage::OnBnClickedChkService9)
	ON_BN_CLICKED(IDC_CHK_SERVICE10, &CLS_SOPage::OnBnClickedChkService10)
	ON_BN_CLICKED(IDC_BUTTON_RUN, &CLS_SOPage::OnBnClickedButtonRun)
	ON_BN_CLICKED(IDC_CHECK_ONVIF_H265SUPPORT, &CLS_SOPage::OnBnClickedCheckOnvifH265support)
	
END_MESSAGE_MAP()

void CLS_SOPage::OnChannelChanged( int _iLogonID,int /*_iChannelNo*/,int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;
	UI_UpdatePlatformApp();
}

void CLS_SOPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();
}
// CLS_SOPage message handlers

BOOL CLS_SOPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialog();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_SOPage::OnBnClickedButtonSoRun()
{
	// TODO: Add your control notification handler code here
	int iLogonID = m_iLogonID;
	if (iLogonID < 0)
		return;

	int iSel = m_cboPlatformSO.GetCurSel();//get select box
	if (iSel < 0 || iSel >= MAX_PLATFORM_COUNT)
	{
		return;
	}

	CString cstrSO;
	m_cboPlatformSO.GetWindowText(cstrSO);

	if (cstrSO.IsEmpty())
	{
		return;
	}

	TPlatformApp pa = {0};
	for (int j=0; j < MAX_APP_SERVER_LIST_NUM; ++j)
	{
		CString strTemp;	
		if (BST_CHECKED == m_chkArrService[j].GetCheck())
		{
			m_chkArrService[j].GetWindowText(strTemp);

			//Extra long content is not sent
			if (MAX_PALTFORM_NAME_LENGTH_EX <= cstrSO.GetLength()+strTemp.GetLength()+1)
			{
				break;
			}

			cstrSO += "," + strTemp;
		}
	}

	pa.iState[iSel] = ENABLE;
	memcpy(pa.cName[iSel], (LPCSTR)(LPCTSTR)cstrSO, sizeof(pa.cName[iSel]));
	memcpy(pa.cNameEx[iSel], (LPCSTR)(LPCTSTR)cstrSO, cstrSO.GetLength());

	int iRet = NetClient_SetPlatformApp(iLogonID, PLATFORM_CMD_SET_RUN, &pa, sizeof(pa));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetPlatformApp[RUN](%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetPlatformApp[RUN](%d)",m_iLogonID);
		Sleep(100);
		UI_UpdatePlatformApp();
	}
	return;
}

void CLS_SOPage::OnBnClickedButtonSoStop()
{
	// TODO: Add your control notification handler code here
	int iLogonID = m_iLogonID;
	if (iLogonID < 0)
		return;

	TPlatformApp pa = {0};
	CString cstrSO;
	m_cboPlatformSO.GetWindowText(cstrSO);
	strcpy_s(pa.cName[0], sizeof(pa.cName[0]), (LPCSTR)(LPCTSTR)cstrSO+3);
	if (strlen(pa.cName[0]) == 0)
		return;
	int iRet = NetClient_SetPlatformApp(iLogonID, PLATFORM_CMD_SET_RUN, &pa, sizeof(pa));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetPlatformApp[STOP](%d)",m_iLogonID);
	}    
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetPlatformApp[STOP](%d)",m_iLogonID);
		Sleep(100);
		UI_UpdatePlatformApp();
	}
}

bool CLS_SOPage::UI_UpdatePlatformApp()
{
	int iLogonID = m_iLogonID;
	if (iLogonID < 0)
		return false;

	TPlatformApp pa = {0};
	int iRet = NetClient_GetPlatformApp(iLogonID, PLATFORM_CMD_GET_LIST, &pa, sizeof(pa));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","[NetClient_GetPlatformApp] failed(%d)",m_iLogonID);
		return false;
	}

	m_cboPlatformSO.ResetContent();
	char cName[256] = {0};
	for (int i = 0; i < MAX_PLATFORM_COUNT; ++i)
	{
		if (strlen(pa.cName[i]) == 0)
			continue;

		memset(cName, 0, sizeof(cName));
		sprintf_s(cName, sizeof(cName), "%s", pa.cName[i]);
		m_cboPlatformSO.AddString(cName);
	}
	m_cboPlatformSO.SetCurSel(0);

	ServerSetInit();

	return true;
}

void CLS_SOPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_BUTTON_SO_RUN,IDS_CFG_SO_RUN);
	SetDlgItemTextEx(IDC_BUTTON_SO_STOP,IDS_CFG_SO_STOP);
	SetDlgItemTextEx(IDC_BTN_TEST,IDS_LOG_TEST);
	SetDlgItemTextEx(IDC_BUTTON_RUN,IDS_CFG_SO_RUN);
	SetDlgItemText(IDC_STATIC_RUN_PLATFORM, GetTextByLan(_T("平台启用："), _T("Run PlatFormApp:")));
	SetDlgItemText(IDC_STATIC_SET_PLATFORM_STATE, GetTextByLan(_T("开启/关闭外挂："), _T("Run/Stop hanging:")));
	m_chkOnvifH265Support.ShowWindow(SW_SHOW);
}

void CLS_SOPage::ServerSetInit()
{
	CString strOSName = "";
	CString strServerName = "";
	m_cboPlatformSO.GetWindowText(strOSName);

	int iSel = -1;
	int iReturned = 0;
	APPServerList stAPPServerList = {0};
	stAPPServerList.iBufSize = sizeof(APPServerList);
	strcpy_s(stAPPServerList.cAppName,(LPSTR)(LPCTSTR)strOSName);

	if (!strOSName.IsEmpty() 
		&& NULL != NetClient_GetDevConfig 
		&& RET_SUCCESS == NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_APP_SERVER_LIST, PARAM_CHANNEL_ALL, &stAPPServerList, sizeof(APPServerList), &iReturned))
	{
		for (int i=0; i<stAPPServerList.iServerListNum && i<MAX_APP_SERVER_LIST_NUM; i++)
		{
			if (0 == strlen(stAPPServerList.cServerListName[i]))
			{
				continue;
			}

			m_chkArrService[i].ShowWindow(SW_NORMAL);

			if (ENABLE == stAPPServerList.iState[i])
			{
				iSel = i;
				m_chkArrService[i].SetCheck(BST_CHECKED);
			}
			else
			{
				m_chkArrService[i].SetCheck(BST_UNCHECKED);
			}
			strServerName = stAPPServerList.cServerListName[i];
			m_chkArrService[i].SetWindowText(strServerName);
			if (0 == strServerName.CompareNoCase(_T("onvif")))
			{
				OnvifH265Init();
			}
		}
		for (int iNum = stAPPServerList.iServerListNum; iNum < MAX_APP_SERVER_LIST_NUM; iNum++)
		{
			if (iNum >= 0)
			{
				m_chkArrService[iNum].ShowWindow(SW_HIDE);
			}

		}
	}
}

void CLS_SOPage::MulitiServiceCheck(int iIndex)
{
	if (BST_CHECKED != m_chkArrService[iIndex].GetCheck())
	{
		goto END;
	}
	if (SUPPORT_MUTIPLAT == GetFuncAbility(MAIN_FUNC_TYPE_TRADE,0))//Support enable multi-service
	{
		CString cstrIndexName;
		m_chkArrService[iIndex].GetWindowText(cstrIndexName);
		for (int j=0; j<MAX_APP_SERVER_LIST_NUM; ++j )
		{
			CString cstrTempName;
			m_chkArrService[j].GetWindowText(cstrTempName);
			if(((_T("QQ") == cstrIndexName) && (_T("p2p") == cstrTempName))
				||((_T("p2p") == cstrIndexName) && (_T("QQ") == cstrTempName)))
			{
				m_chkArrService[j].SetCheck(BST_UNCHECKED);
			}
		}
		goto END;
	}
	for (int i = 0; i < MAX_APP_SERVER_LIST_NUM; ++i)
	{
		if (iIndex == i)
		{
			continue;
		}
		m_chkArrService[i].SetCheck(BST_UNCHECKED);
	}

END:
	return;
}

int CLS_SOPage::GetFuncAbility(int _iMainType, int _iSubType)
{
	int iResult = 0;
	if (m_iLogonID<0 || _iMainType<=MIN_MAIN_FUNC_TYPE || _iMainType>=MAX_MAIN_FUNC_TYPE)
	{
		return iResult;
	}

	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = _iMainType;
	stFuncAbilityLevel.iSubFuncType = _iSubType;
	int iReturnByte = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO,
		&stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (RET_SUCCESS == iRet && 0 < strlen(stFuncAbilityLevel.cParam))
	{
		iResult = _ttoi(stFuncAbilityLevel.cParam);		
	}
	else
	{
		AddLog(LOG_LEVEL_ERROR, "","[GetFuncAbility]NetClient_GetDevConfig failed! _iMainType(%d),_iSubType(%d)",
			_iMainType, _iSubType);
		iResult = -1;
	}

	return iResult;
}
void CLS_SOPage::OnBnClickedChkService1()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX0);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX0].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService2()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX1);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX1].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService3()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX2);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX3].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService4()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX3);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX3].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService5()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX4);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX4].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService6()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX5);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX5].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService7()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX6);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX6].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService8()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX7);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX7].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService9()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX8);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX8].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnBnClickedChkService10()
{
	MulitiServiceCheck(CONST_INT_SERVICE_INDEX9);
	CString strPlatName; 
	m_chkArrService[CONST_INT_SERVICE_INDEX9].GetWindowText(strPlatName);
	if (0 == strPlatName.CompareNoCase(_T("onvif")))
	{
		OnvifH265Init();
	}
}

void CLS_SOPage::OnvifH265Init()
{

	int iEnable = 0;
	int iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_ONVIFSUPPORTH265, INVALID_FLAG, &iEnable);
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_SOPage::OnvifH265Init][NetClient_GetCommonEnable] LogonID=%d,iRet=%d,LastError=0x%08x",
			m_iLogonID,iRet,GetLastError());
		iEnable = ONVIFH265_DISABLE;
	}
	
	m_chkOnvifH265Support.SetCheck(ONVIFH265_ENABLE==iEnable?BST_CHECKED:BST_UNCHECKED);

	if (!SupporOnvifH265())
	{
		m_chkOnvifH265Support.ShowWindow(SW_HIDE);
	}
	else
	{
		BOOL bOnvif = FALSE;
		CString	strPlatName;
		int iIndex = 0;
		for ( ; iIndex < MAX_APP_SERVER_LIST_NUM; ++iIndex)
		{
			m_chkArrService[iIndex].GetWindowText(strPlatName);
			if (0 == strPlatName.CompareNoCase(_T("onvif")))
			{
				bOnvif = TRUE;
				break;
			}
		}

		// If there is onvif in the list, and onvif is selected, it will display the allow 265 option
		if (bOnvif && (iIndex >= 0 ||iIndex < MAX_APP_SERVER_LIST_NUM))
		{

			if (BST_CHECKED == m_chkArrService[iIndex].GetCheck())
			{
				m_chkOnvifH265Support.ShowWindow(SW_SHOW);
			}
			else
			{
				m_chkOnvifH265Support.ShowWindow(SW_HIDE);
			}
		}
		else
		{
			m_chkOnvifH265Support.ShowWindow(SW_HIDE);
		}
	}

}

bool CLS_SOPage::SupporOnvifH265()
{
	bool bSupport = false;

	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_PLUGIN;
	stFuncAbilityLevel.iSubFuncType = PLUGIN_SUB_TYPE_ONVIFH265;
	int iReturnByte = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, &stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (RET_SUCCESS == iRet)
	{
		//1 supported 0 not supported
		int iResult = _ttoi(stFuncAbilityLevel.cParam);
		if (1 == iResult)
		{
			bSupport = true;
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_SOPage::SupporOnvifH265][NetClient_GetDevConfig] LogonID=%d,iRet=%d,LastError=0x%08x",
			m_iLogonID,iRet,GetLastError());
	}

	return bSupport;
}


void CLS_SOPage::OnBnClickedButtonRun()
{
	// TODO: Add your control notification handler code here
	int iLogonID = m_iLogonID;
	if (iLogonID < 0)
		return;

	TPlatformApp pa = {0};
	CString cstrSO;
	m_cboPlatformSO.GetWindowText(cstrSO);
	strcpy_s(pa.cName[0], sizeof(pa.cName[0]), (LPCSTR)(LPCTSTR)cstrSO);
	if (strlen(pa.cName[0]) == 0)
		return;
	pa.iState[0] = 1;
	int iRet = NetClient_SetPlatformApp(iLogonID, PLATFORM_CMD_SET_RUN, &pa, sizeof(pa));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetPlatformApp[RUN](%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetPlatformApp[RUN](%d)",m_iLogonID);
		Sleep(100);
		UI_UpdatePlatformApp();
	}

}

void CLS_SOPage::OnBnClickedCheckOnvifH265support()
{
	// TODO: Add your control notification handler code here
	SaveOnvifH265();
}

void CLS_SOPage::SaveOnvifH265()
{
	int iFuncRet = 0;
	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_PLUGIN;
	stFuncAbilityLevel.iSubFuncType = ABLIITY_PLUGIN_REBOOT;
	int iReturnByte = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, &stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (RET_SUCCESS == iRet)
	{
		iFuncRet = _ttoi(stFuncAbilityLevel.cParam);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_SOPage::SaveOnvifH265][NetClient_GetDevConfig] LogonID=%d,iRet=%d,LastError=0x%08x",
			m_iLogonID,iRet,GetLastError());
	}

	// Send enable command
	int iEnable = ONVIFH265_DISABLE;
	if (BST_CHECKED == m_chkOnvifH265Support.GetCheck())
	{
		iEnable = ONVIFH265_ENABLE;	
	}

	iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_ONVIFSUPPORTH265, INVALID_FLAG, iEnable);
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_SOPage::SaveOnvifH265][NetClient_SetCommonEnable] LogonID=%d,iRet=%d,LastError=0x%08x",
			m_iLogonID,iRet,GetLastError());
	}
}

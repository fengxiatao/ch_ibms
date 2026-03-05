// DlgCommonEnable.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgCommonEnable.h"
#include "Common/Ini.h"

// CLS_DlgCommonEnable dialog

#define CHANNEL_ALL 0
#define CHANNEL_SPECIAL 1

IMPLEMENT_DYNAMIC(CLS_DlgCommonEnable, CDialog)

CLS_DlgCommonEnable::CLS_DlgCommonEnable(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgCommonEnable::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
}

CLS_DlgCommonEnable::~CLS_DlgCommonEnable()
{
}

void CLS_DlgCommonEnable::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_CFG_COMENABLE_ID, m_CboCommonEnableType);
	DDX_Control(pDX, IDC_EDIT_GETNEW_COMMONENABLE, m_edtEnableValue);
}


BEGIN_MESSAGE_MAP(CLS_DlgCommonEnable, CLS_BasePage)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_CBO_CFG_COMENABLE_ID, &CLS_DlgCommonEnable::OnCbnSelchangeCboCfgComenableId)
	ON_BN_CLICKED(IDC_BTN_CFG_COMENABLE_SET, &CLS_DlgCommonEnable::OnBnClickedBtnCfgComenableSet)
	ON_BN_CLICKED(IDC_BUTTON_GET_NEW_COMMONENABLE, &CLS_DlgCommonEnable::OnBnClickedButtonGetNewCommonenable)
END_MESSAGE_MAP()

BOOL CLS_DlgCommonEnable::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	InitDialogItemText();
	GetCommonEnable();
	return TRUE;
}

void CLS_DlgCommonEnable::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
	}
}

void CLS_DlgCommonEnable::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (m_iLogonID == _iLogonID && m_iChannelNo == _iChannelNo && m_iStreamNo == _iStreamNo)
	{
		return;
	}

	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	m_iStreamNo = _iStreamNo;
	GetCommonEnable();
}

void CLS_DlgCommonEnable::OnLanguageChanged( int _iLanguage )
{
	InitDialogItemText();
}

void CLS_DlgCommonEnable::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (_iLogonID != m_iLogonID)
	{
		return;
	}

	//TODO
}

void CLS_DlgCommonEnable::InitDialogItemText()
{
    ReadFuncFromConfig();
	UpDateCommonEnableList();
}

void CLS_DlgCommonEnable::ReadFuncFromConfig()
{
    m_MapItem.clear();
    CString strSavePath;
    char cFilePath[MAX_PATH] = {0};
    int iSize = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
    if (iSize <= 0)
    {
        strcpy_s(cFilePath,sizeof(cFilePath),"C:\\");
    }
    strSavePath.Format(_T("%s"),cFilePath);
    int iPos = strSavePath.ReverseFind('\\');
    if (iPos >= 0)
    {
        strSavePath = strSavePath.Left(iPos);
    }
    strSavePath += "\\ModulesEnable.ini";

	//common enable count
	CStdioFile fEnableFile;
	//Subscripts represent modules, and values represent the number of subordinate types
	int iModulesArr[LEN_64] = {0};
	int iCount = 0;
	BOOL bRet = fEnableFile.Open(strSavePath, CFile::modeRead);
	if(bRet)
	{
		CString strLineData;
		while (fEnableFile.ReadString(strLineData))
		{
			if('[' == strLineData.GetAt(0))
			{
				++iCount;
			}
			else if("0x" == strLineData.Left(2))
			{
				if(iCount <= 0)
				{
					strLineData.Empty();
					continue;
				}
				++iModulesArr[iCount - 1];
			}
			strLineData.Empty();
		}
		fEnableFile.Close();
	}
    else
    {
        AfxMessageBox("The enable type could not be loaded, probably because the ModulesEnable.ini file is missing!");
    }

    CIniFile DeviceFile(strSavePath);

	int iCurModule = 0;
    int idx = 0;
    CString szKeyNameZh = "name_zh";
    CString szKeyNameEn = "name_en";
    CString szKeyIdStart = "id_start";
    
    do 
    {
		int iCurIndex = 0;
        CString szSection;
        szSection.Format("module%d", idx);
        CString strModuleName = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKeyNameZh, "");
        if (strModuleName.GetLength() < 2)
            break;
        StModule aModule;
        aModule.strNameZh = strModuleName;
        aModule.strNameEn = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKeyNameEn, "");
        aModule.strIdStart = DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKeyIdStart, "");

        if (aModule.strIdStart.GetLength() < 6)
            break;

        // 0xa000~0xaFFF
        CString strPrefix = aModule.strIdStart.Left(aModule.strIdStart.GetLength() - 3);
        for (int idy = 0x0; idy <= 0xFFF; idy++)
        {
			if(iCurIndex >= iModulesArr[iCurModule])
			{
				break;
			}

            CString strKey;
            strKey.Format("%s%03X", strPrefix, idy);
            char strItemValue[LEN_1024] = {0};
            DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)strKey, strItemValue, LEN_1024, "");
            if (strlen(strItemValue) > 0)
            {
                StItem item;
                OsStrArray strArray;
                os_split_str_to_arr(strItemValue, ',', &strArray);
                item.iChannelAllForGet = atoi(strArray.pcStr[0]);
                item.iChannelAllForSet = atoi(strArray.pcStr[1]);
                item.strNameZh = strArray.pcStr[2];
                item.strNameEn = strArray.pcStr[3];
                item.iModuleIndex = idx;
                unsigned int iKey = HexStringToUInt((char*)(LPCTSTR)strKey);
                m_MapItem[iKey] = item;
				++iCurIndex;
            }
        }
        m_moduleList.push_back(aModule);
        idx++;
		++iCurModule;
    } while (1);

}

void CLS_DlgCommonEnable::GetCommonEnable()
{
	SetDlgItemText(IDC_EDT_CFG_COMENABLE_NUM, "");

	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgCommonEnable::GetCommonEnable] Error  LogonID!");
		return;
	}

	int iEnableType = m_CboCommonEnableType.GetItemData(m_CboCommonEnableType.GetCurSel());
	int iEnableValue = -1;
	int iRet = -1;
    int iTempChannelNo = -1;

    if (CHANNEL_ALL == m_MapItem[iEnableType].iChannelAllForGet)
    {
        iTempChannelNo = INVALID_FLAG;
    }
    else
    {
        iTempChannelNo = m_iChannelNo;
    }

    iRet = NetClient_GetCommonEnable(m_iLogonID, iEnableType, iTempChannelNo, &iEnableValue);

	if (iRet < 0 || iEnableValue < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgCommonEnable::GetCommonEnable] Get EnableValue Failed  LogonID %d EnableType %d!", m_iLogonID, iEnableType);
		return;
	}

	SetDlgItemInt(IDC_EDT_CFG_COMENABLE_NUM, iEnableValue);
}


void CLS_DlgCommonEnable::SetCommonEnable()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgCommonEnable::SetCommonEnable] Error  LogonID!");
		return;
	}

	int iEnableType = (int)m_CboCommonEnableType.GetItemData(m_CboCommonEnableType.GetCurSel());
	int iEnableValue = -1;
	DWORD_PTR iRet = -1;
	iEnableValue = GetDlgItemInt(IDC_EDT_CFG_COMENABLE_NUM);
	
	int iTempChannelNo = -1;

    if (CHANNEL_ALL == m_MapItem[iEnableType].iChannelAllForSet)
	{
		iTempChannelNo = INVALID_FLAG;
	}
    else
    {
        iTempChannelNo = m_iChannelNo;
    }

	iRet = NetClient_SetCommonEnable(m_iLogonID, iEnableType, iTempChannelNo, iEnableValue);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_DlgCommonEnable::SetCommonEnable] Set Failed ! LogonID %d EnableType %d EnableValue %d", m_iLogonID, iEnableType, iEnableValue);
		return;
	}
}

void CLS_DlgCommonEnable::UpDateCommonEnableList()
{
	SetDlgItemTextEx(IDC_GPO_CFG_COMENABLE, IDS_CFG_COMMONENABLE);
	SetDlgItemTextEx(IDC_BTN_CFG_COMENABLE_SET, IDS_SET);


	int iTempSel = m_CboCommonEnableType.GetCurSel();
	iTempSel = iTempSel < 0 ? 0 : iTempSel;
	m_CboCommonEnableType.ResetContent();

    std::map<unsigned int, StItem>::iterator iterItem = m_MapItem.begin();
    while (iterItem != m_MapItem.end())
    {
        std::list<StModule>::iterator iter = m_moduleList.begin();
        advance(iter, iterItem->second.iModuleIndex);
        CString strNameZh;
        strNameZh.Format("%s-%s", iter->strNameZh, iterItem->second.strNameZh);
        CString strNameEn;
        strNameEn.Format("%s-%s", iter->strNameEn, iterItem->second.strNameEn);
        m_CboCommonEnableType.SetItemData(m_CboCommonEnableType.AddString(GetTextByLan(strNameZh,strNameEn)), iterItem->first);
        iterItem++;
    }
	
    if (iTempSel < m_CboCommonEnableType.GetCount())
	{
		m_CboCommonEnableType.SetCurSel(iTempSel);
	}
	else
	{
		m_CboCommonEnableType.SetCurSel(0);
	}
}

void CLS_DlgCommonEnable::OnCbnSelchangeCboCfgComenableId()
{
	GetCommonEnable();
}


void CLS_DlgCommonEnable::OnBnClickedBtnCfgComenableSet()
{
	SetCommonEnable();
}


void CLS_DlgCommonEnable::OnBnClickedButtonGetNewCommonenable()
{
	if (-1 == m_iLogonID  || -1 == m_iChannelNo )
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_DlgCommonEnable::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}

	NewCommonEnable tInofIn = {0};
	tInofIn.iEnable = (int)m_CboCommonEnableType.GetItemData(m_CboCommonEnableType.GetCurSel());


	NewCommonEnableResult tInfoOut = {0};

	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_GETNEW_COMMONENABLE, m_iChannelNo, &tInofIn, sizeof(tInofIn), &tInfoOut, sizeof(NewCommonEnableResult));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCommonEnable failed.iFuncType=%d, iEnable = %d",CMD_GETNEW_COMMONENABLE, CI_COMMON_ID_ALI_PAYMENT_ALGORITHM);
	}
	else
	{
		CString cstrEnableValue;
		cstrEnableValue.Format("%d", tInfoOut.iEnableValue);
		m_edtEnableValue.SetWindowText(cstrEnableValue);
	}
}

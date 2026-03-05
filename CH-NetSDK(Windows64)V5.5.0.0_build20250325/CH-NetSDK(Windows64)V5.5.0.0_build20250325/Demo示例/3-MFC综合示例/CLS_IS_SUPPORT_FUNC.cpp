// CLS_IS_SUPPORT_FUNC.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_IS_SUPPORT_FUNC.h"


// CLS_IS_SUPPORT_FUNC dialog

IMPLEMENT_DYNAMIC(CLS_IS_SUPPORT_FUNC, CDialog)

CLS_IS_SUPPORT_FUNC::CLS_IS_SUPPORT_FUNC(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_IS_SUPPORT_FUNC::IDD, pParent)
{

}

CLS_IS_SUPPORT_FUNC::~CLS_IS_SUPPORT_FUNC()
{
}

void CLS_IS_SUPPORT_FUNC::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_FUNC_TYPE, m_cboFuncType);
	DDX_Control(pDX, IDC_COMBO_SUBTYPE, m_cboSubtype);
	DDX_Control(pDX, IDC_EDIT_FUNC_RESULT, m_CEditFuncResult);
}


BEGIN_MESSAGE_MAP(CLS_IS_SUPPORT_FUNC, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_CHECK, &CLS_IS_SUPPORT_FUNC::OnBnClickedButtonCheck)
	ON_CBN_SELCHANGE(IDC_COMBO_FUNC_TYPE, &CLS_IS_SUPPORT_FUNC::OnCbnSelchangeComboFuncType)
	ON_CBN_SELCHANGE(IDC_COMBO_SUBTYPE, &CLS_IS_SUPPORT_FUNC::OnCbnSelchangeComboSubtype)
    ON_BN_CLICKED(IDC_BUTTON_CHECK_ALL_FUNC, &CLS_IS_SUPPORT_FUNC::OnBnClickedButtonCheckAllFunc)
END_MESSAGE_MAP()


// CLS_IS_SUPPORT_FUNC message handler

BOOL CLS_IS_SUPPORT_FUNC::OnInitDialog()
{
	CDialog::OnInitDialog();

	ReadFuncFromConfig();

	return 0;
}

void CLS_IS_SUPPORT_FUNC::ReadFuncFromConfig()
{
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
	strSavePath += "\\FuncAbilityLevel.ini";

	FILE *file = fopen(strSavePath, "rb");
	if (file)
	{
		fclose(file);
	}
	else
	{
		AfxMessageBox("The ability set type could not be loaded, probably lack of FuncAbilityLevel.ini file!");
	}

	CIniFile DeviceFile(strSavePath);
	//
	//CString szSection;
	//CString szKey;	
	int iNo = 0;
	while(true)
	{
		CString szSection = "FuncAbilityLevel";
		CString szNo ;
		szNo.Format("%d", iNo);
		szSection += szNo;
		CString szKey = "MainType";
		char cstrMainTempValue[LEN_1024] = {0};
		FunAbility tFunAblility={0};
		DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, cstrMainTempValue,LEN_1024,"");
		if (strcmp(cstrMainTempValue,"") == 0){
			break;
		}
		
		OsStrArray strArray;
		os_split_str_to_arr(cstrMainTempValue, ',', &strArray);
		CommonUse MainType;
		MainType.iValue = _tcstoul(strArray.pcStr[0],NULL,16);
		MainType.cstrCH = strArray.pcStr[1];
		MainType.cstrEN = strArray.pcStr[2];		
		tFunAblility.m_MainType = MainType;
	////read subtype
		int iIndex = 0;
		char cstrSubTmpValue[LEN_1024] = {0};
		while (true)
		{
			szKey = "SubType";
			CString cstrIndex;
			cstrIndex.Format("%d",iIndex);
			szKey += cstrIndex;
			DeviceFile.ReadString((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, cstrSubTmpValue,LEN_1024,"");
			if (0 == strcmp(cstrSubTmpValue,"")){
				break;
			}
			iIndex++;
			OsStrArray strSubArray;
			os_split_str_to_arr(cstrSubTmpValue, ',', &strSubArray);
			CommonUse tSubType;
			tSubType.iValue = _tcstoul(strSubArray.pcStr[0],NULL,10);
			tSubType.cstrCH = strSubArray.pcStr[1];
			tSubType.cstrEN = strSubArray.pcStr[2];

			//insert map			
			tFunAblility.m_MapSubType.insert(pair<int,CommonUse>(tSubType.iValue,tSubType));		
		}
		s_mapFunAbility.insert(pair<int,FunAbility>(iNo,tFunAblility));
		iNo++;
	}
		
		std::map<int, FunAbility>::iterator iter;  
		 for(iter = s_mapFunAbility.begin(); iter != s_mapFunAbility.end(); iter++)  
		{
			char cUIMainType[16] = {0};
			_itot(iter->second.m_MainType.iValue, cUIMainType, 16);
			CString strUIMainType = "";
			strUIMainType += "0x";
			strUIMainType += cUIMainType;
			strUIMainType += "------";
			strUIMainType += iter->second.m_MainType.cstrCH;
			m_cboFuncType.SetItemData(m_cboFuncType.AddString(_T(strUIMainType)), iter->second.m_MainType.iValue);
		}

		m_cboFuncType.SetCurSel(0);
		OnCbnSelchangeComboFuncType();

}

void CLS_IS_SUPPORT_FUNC::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
}

void CLS_IS_SUPPORT_FUNC::OnBnClickedButtonCheck()
{
	// TODO: Add control notification handler code here
	FuncAbilityLevel tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iMainFuncType = m_cboFuncType.GetItemData(m_cboFuncType.GetCurSel());

	if (tInfo.iMainFuncType != 0x2 && tInfo.iMainFuncType != 0x4 )
	{
		tInfo.iSubFuncType = m_cboSubtype.GetItemData(m_cboSubtype.GetCurSel());
	}

	if ( 0 > m_iChannelNo)
	{
		m_iChannelNo = 0;
	}
	int iByteReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNo, (void*)&tInfo, sizeof(tInfo), &iByteReturn);

	m_CEditFuncResult.SetWindowText("");
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_GET_FUNC_ABILITY fail! m_iChannelNo = %d, iMainFuncType = %d, iSubFuncType = %d.", m_iChannelNo, tInfo.iMainFuncType, tInfo.iSubFuncType);
	}
	else
	{
		m_CEditFuncResult.SetWindowText(tInfo.cParam);
	}

}

//Update subtype based on main type
void CLS_IS_SUPPORT_FUNC::OnCbnSelchangeComboFuncType()
{
	m_CEditFuncResult.SetWindowText("");
	int iMainType = m_cboFuncType.GetCurSel();
	
	std::map<int, FunAbility>::iterator iter; 
	iter = s_mapFunAbility.find(iMainType);
	m_cboSubtype.ResetContent();
	
	if(iter != s_mapFunAbility.end())
	{
		if(iter->second.m_MapSubType.size() == 0)
		{
			m_cboSubtype.AddString(_T("no subtype"));
			return;			
		}
		std::map<int,CommonUse>::iterator iSubItem;
		for(iSubItem = iter->second.m_MapSubType.begin();iSubItem != iter->second.m_MapSubType.end();iSubItem++)
		{
			char cUISubType[16] = {0};
			_itot(iSubItem->second.iValue, cUISubType, 10);
			CString strUISubType;
			strUISubType = cUISubType;
			strUISubType += "------";
			strUISubType += iSubItem->second.cstrCH;
			m_cboSubtype.SetItemData(m_cboSubtype.AddString(_T(strUISubType)), iSubItem->second.iValue);
		}
		
	}
	m_cboSubtype.SetCurSel(0);
}

void CLS_IS_SUPPORT_FUNC::OnCbnSelchangeComboSubtype()
{
	// TODO: Add control notification handler code here
	m_CEditFuncResult.SetWindowText("");
}

void CLS_IS_SUPPORT_FUNC::OnBnClickedButtonCheckAllFunc()
{
    // TODO: Add your control notification handler code here
    WCHAR szDir[MAX_PATH] ={0};
    BROWSEINFO bi;
    ITEMIDLIST *pidl;
    bi.hwndOwner = this->m_hWnd;
    bi.pidlRoot = NULL; //Empty to select desktop by default
    bi.pszDisplayName = (LPSTR)szDir;
    CString temp = "";
    bi.lpszTitle = (LPCSTR)temp;
    bi.ulFlags = BIF_RETURNONLYFSDIRS;
    bi.lpfn = NULL;
    bi.lParam = 0;
    bi.iImage = 0;
    pidl = SHBrowseForFolder(&bi);
    if (NULL == pidl)
    {
        return;
    }
    CString cstrPath;
    if (!SHGetPathFromIDList(pidl,(LPSTR)szDir))
    {
        return;
    }
    COleDateTime odtTime = COleDateTime::GetCurrentTime();
    CString cstrIniFilePath;
    cstrIniFilePath.Format("%s\\devfunc_%s.ini",szDir,odtTime.Format(_T("%Y%m%d_%H%M_%S")));
    // TODO: Add control notification handler code here
    FuncAbilityLevel tInfo = {0};
    tInfo.iSize = sizeof(tInfo);
    CString cstrSec;
    CString cstrKey;
    CString cstrValue;
    const int FUNC_MAIN_MAX = 50;
    const int FUNC_SUB_MAX = 500;
    for (int iMain =0; iMain<FUNC_MAIN_MAX; iMain++)
    {
        CString cstrSubSum;
        for (int iSub =0;iSub <FUNC_SUB_MAX;iSub ++)
        {
            tInfo.iMainFuncType = iMain;
            tInfo.iSubFuncType = iSub;
            memset(&tInfo.cParam,0,sizeof(tInfo.cParam));
            cstrSec.Format("mainfunc_%d",iMain);
            cstrKey.Format("subfunc_%d",iSub);

            if ( 0 > m_iChannelNo)
            {
                m_iChannelNo = 0;
            }
            int iByteReturn = -1; 
            int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNo, (void*)&tInfo, sizeof(tInfo), &iByteReturn);
            if (iRet != RET_SUCCESS)
            {
                cstrValue = "";
            }
            else
            {
                cstrValue.Format("%s",tInfo.cParam);
            }
            if (!cstrValue.IsEmpty())
            {
                cstrSubSum.AppendFormat("%d,",iSub);
                WritePrivateProfileString(cstrSec,cstrKey,cstrValue,cstrIniFilePath);
            }
        }
        if (cstrSubSum.GetLength()>0)
        {
            cstrSubSum = cstrSubSum.Left(cstrSubSum.GetLength()-1);
        }
         WritePrivateProfileString(cstrSec,"subsum",cstrSubSum,cstrIniFilePath);
    }
}

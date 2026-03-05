
#include "stdafx.h"
#include "CommonFun.h"

#include <atlimage.h>
#include <shlwapi.h>

enum IMAGE_EXT_TYPE
{
	IMAGE_EXT_TYPE_UNKNOWN	= -1,
	IMAGE_EXT_TYPE_JPG		= 0,
	IMAGE_EXT_TYPE_PNG		= 1
};

int	GetItemCurData(CComboBox &_cbo)
{
	int iData = 0;
	int iSel = _cbo.GetCurSel();
	if (iSel >= 0)
	{
		iData = (int)_cbo.GetItemData(iSel);
	}
	return iData;
}

int	SetItemCurSel(CComboBox &_cbo, int _iData)
{
	int iCount = _cbo.GetCount();
	for (int i = 0; i < iCount; ++i)
	{
		if (_cbo.GetItemData(i) == _iData)
		{
			_cbo.SetCurSel(i);
		}
	}
	return 0;
}

HTREEITEM InsertItem( CTreeCtrl& _TreeCtrl, LPCTSTR _strItem, DWORD_PTR _dwData, HTREEITEM _hParent /*= TVI_ROOT*/ )
{
	DWORD_PTR dwData = 0;
	HTREEITEM hItem = _TreeCtrl.GetChildItem(_hParent);
	while(hItem)
	{
		dwData = _TreeCtrl.GetItemData(hItem);
		if (dwData == _dwData)
		{
			_TreeCtrl.SetItemText(hItem,_strItem);
			return hItem;
		}
		hItem = _TreeCtrl.GetNextSiblingItem(hItem);
	}

	hItem = _TreeCtrl.InsertItem(_strItem,_hParent);
	_TreeCtrl.SetItemData(hItem,_dwData);
	return hItem;
}

CString IntToStr(int _iData)
{
	CString strData;
	strData.Format(_T("%d"),_iData);
	return strData;
}

CString	GetCurTimeStr(int _iType)
{
	CString cstr;
	CTime tmCur = CTime::GetCurrentTime(); //Get system date
	if (0 == _iType)
	{
		cstr.Format("%d-%02d-%02d",tmCur.GetYear(),tmCur.GetMonth(),tmCur.GetDay());
	}
	else if (6 == _iType)
	{
		cstr.Format("%d_%02d_%02d_%02d_%02d_%02d",tmCur.GetYear(),tmCur.GetMonth(),tmCur.GetDay(), tmCur.GetHour(), tmCur.GetMinute(), tmCur.GetSecond());
	}
	return cstr;
}

int CALLBACK SHBrowseForFolderCallbackProc( HWND hwnd, UINT uMsg, LPARAM lParam, LPARAM lpData ) 
{ 
	switch(uMsg) 
	{ 
	case BFFM_INITIALIZED:    //Initialization message BFFM_INITIALIZED 
		::SendMessage(hwnd, BFFM_SETSELECTION,TRUE, 0);   //Pass Default Open Path 
		break; 
	case BFFM_SELCHANGED:    //Select path change, BFFM_SELCHANGED 
		{ 
			char curr[MAX_PATH];    
			SHGetPathFromIDList((LPCITEMIDLIST)lParam,curr);    
			::SendMessage(hwnd,BFFM_SETSTATUSTEXT,0,(LPARAM)&curr[0]);   
		} 
		break; 
	default: 
		break; 
	}  
	return 0; 
} 

CString BrowseFolder()
{
	CString cstrPath; 

	BROWSEINFO br = {0}; 
	br.hwndOwner = NULL; 
	br.pidlRoot = 0; 
	br.pszDisplayName = 0; 
	br.lpszTitle = "";
	br.ulFlags = BIF_STATUSTEXT | BIF_RETURNONLYFSDIRS | BIF_USENEWUI | BIF_NEWDIALOGSTYLE; 
	br.lpfn = SHBrowseForFolderCallbackProc ;       //Set CALLBACK function 
	br.iImage = 0; 
	//br.lParam =long(&strDef);						//Set Default Path

	::OleInitialize(NULL);
	int iRet = 0;
	ITEMIDLIST* pItem = SHBrowseForFolder(&br);
	if(pItem != NULL) 
	{  
		TCHAR szPath[MAX_PATH] = {0}; 
		if(SHGetPathFromIDList(pItem, szPath) == TRUE) 
		{ 
			cstrPath = szPath; 

			char cTemp = cstrPath.GetAt(cstrPath.GetLength() - 1);
			if ('\\' != cTemp)
			{
				cstrPath += "\\";
			}

			iRet =  1;
		} 
	}	
	::OleUninitialize();
	return cstrPath;
}

CString AnsiToUTF8( const char* _pstrIn)
{
	WCHAR* strSrc    = NULL;
	TCHAR* szRes    = NULL;

	int i = MultiByteToWideChar(CP_ACP, 0,_pstrIn, -1, NULL, 0);

	strSrc = new WCHAR[i+1];
	if (strSrc == NULL){
		return "";
	}
	MultiByteToWideChar(CP_ACP, 0,_pstrIn, -1, strSrc, i);

	i = WideCharToMultiByte(CP_UTF8, 0, strSrc, -1, NULL, 0, NULL, NULL);

	szRes = new TCHAR[i+1];
	if (szRes == NULL){
		delete[] strSrc;
		return "";
	}
	WideCharToMultiByte(CP_UTF8, 0, strSrc, -1, szRes, i, NULL, NULL);

	CString strOut = szRes;

	delete[] strSrc;
	delete[] szRes;

	return strOut;
}

int GetFaceFileType(CString _cstrFileName)
{
	int iType = IMAGE_EXT_TYPE_UNKNOWN;
	CString cstrExt = PathFindExtension(_cstrFileName);
	if (0 == cstrExt.CompareNoCase(_T(".PNG")))
	{
		iType =	IMAGE_EXT_TYPE_PNG;
	}
	else if (0 == cstrExt.CompareNoCase(_T(".JPG")) || 0 == cstrExt.CompareNoCase(_T(".JPEG")))
	{
		iType = IMAGE_EXT_TYPE_JPG;
	}
	return iType;
}

CString GetCurModulePath()
{
	char cFilePath[MAX_PATH] = {0};
	BOOL bRet = GetModuleFileName(NULL, cFilePath, sizeof(cFilePath));
	CString cstrFilePath(cFilePath);
	cstrFilePath.Replace("FaceDemo.exe", "");
	return cstrFilePath;
}

int ShowImage(CWnd* _pWnd, CString _cstrPath)
{
	if (NULL == _pWnd)
	{
		return -1;
	}

	CImage clsImg;
	HRESULT hr = clsImg.Load(_cstrPath);
	if (!FAILED(hr) && !clsImg.IsNull())
	{
		CDC* pDc = _pWnd->GetDC();
		if (NULL != pDc)
		{
			//Set the scaling mode of the image DC to be displayed
			pDc->SetStretchBltMode(COLORONCOLOR);
			
			//display picture
			RECT rtWnd;
			_pWnd->GetClientRect(&rtWnd);			
			clsImg.Draw(pDc->GetSafeHdc(), rtWnd);

			//Release dc
			_pWnd->ReleaseDC(pDc);
		}
	}
	return 0;
}

CString GetTimeStr(NVS_FILE_TIME& _tInfo)
{
	CString cstr;
	cstr.Format("%04d-%02d-%02d %02d:%02d:%02d", _tInfo.iYear, _tInfo.iMonth, _tInfo.iDay,
		_tInfo.iHour, _tInfo.iMinute, _tInfo.iSecond);
	return cstr;
}

BOOL CheckFaceName( CString _str )
{
	BOOL bRet = TRUE;
	for (int i = 0; i < _str.GetLength(); i++)
	{
		if (!CheckFaceName(_str.GetAt(i)))
		{
			bRet = FALSE;
			break;
		}
	}
	return bRet;
}

BOOL CheckFaceName( byte _cChar )
{
	BOOL bRet = FALSE;
	if(TRUE == IsDBCSLeadByte(_cChar) || 0 != isalnum(_cChar) || '-' == _cChar || '_' == _cChar || VK_SPACE == _cChar || '.' == _cChar)
	{
		bRet = TRUE;
	}
	return bRet;
}

bool IsContainSubStr(const CString& _strSource, const CString& _strChar)
{
	CString strSource = _strSource;
	for (int i = 0; i < _strChar.GetLength(); ++i)
	{
		int iIndex = strSource.Find(_strChar[i]);
		if (iIndex != -1)
		{
			return true;
		}
	}
	return false;
}

bool IsDirectory(const char* strFileOrDirectoryFullPath)
{
	DWORD theFileAttribute = GetFileAttributes(strFileOrDirectoryFullPath);
	if(( theFileAttribute != -1 ) && (theFileAttribute & FILE_ATTRIBUTE_DIRECTORY) != 0)
		return TRUE;
	else return FALSE;
}

bool CreateDirectoryByPath(CString strFullPath)
{
	if (strFullPath.GetLength()<2
		||	strFullPath[1]	!=	':')
		return	false;

	CString	strTmp;
	int		iPos	=	0;
	bool	blRet	=	true;
	do 
	{
		iPos	=	strFullPath.Find('\\',	iPos);
		if (-1	==	iPos)
			strTmp	=	strFullPath;
		else
			strTmp	=	strFullPath.Left(++iPos);

		if(IsDirectory(strTmp) == false)
		{
			/*SECURITY_ATTRIBUTES lp = {0};
			lp.nLength = sizeof(SECURITY_ATTRIBUTES);
			lp.bInheritHandle = TRUE;
			lp.lpSecurityDescriptor = new LPVOID;*/
			int iRet = CreateDirectory(strTmp, NULL);
			if(iRet == 0)
			{
				blRet	=	false;
				break;
			}
		}
	} while (-1	!=	iPos);
	return	blRet;
}

void Utf8ToAnsi( const char* _pstrIn,CString &_strOut)
{
	WCHAR* strSrc    = NULL;
	TCHAR* szRes    = NULL;

	int i = MultiByteToWideChar(CP_UTF8, 0,_pstrIn, -1, NULL, 0);

	strSrc = new WCHAR[i+1];
	if (strSrc == NULL){

		return;
	}
	MultiByteToWideChar(CP_UTF8, 0,_pstrIn, -1, strSrc, i);

	i = WideCharToMultiByte(CP_ACP, 0, strSrc, -1, NULL, 0, NULL, NULL);

	szRes = new TCHAR[i+1];
	if (szRes == NULL){
		delete[] strSrc;
		return;
	}
	WideCharToMultiByte(CP_ACP, 0, strSrc, -1, szRes, i, NULL, NULL);

	_strOut = szRes;

	delete[] strSrc;
	delete[] szRes;
}

void* MallocMsgMemory(int _iSize)
{
	return CLS_MessageManager::Instance()->MallocMemory(_iSize);
}

void FreeMsgMemory( void* _pMemory )
{
	CLS_MessageManager::Instance()->FreeMemory(_pMemory);
}

void DestroyMsgMemory()
{
	CLS_MessageManager::Destroy();
}

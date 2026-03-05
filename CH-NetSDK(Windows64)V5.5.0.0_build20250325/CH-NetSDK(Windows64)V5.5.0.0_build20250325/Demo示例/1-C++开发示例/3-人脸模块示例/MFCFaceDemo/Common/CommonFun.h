
#ifndef _COMMON_FUN_H
#define _COMMON_FUN_H
#include "net_sdk_types.h"
#include "MessageManager.h"

int	GetItemCurData(CComboBox &_cbo);
int	SetItemCurSel(CComboBox &_cbo, int _iData);
HTREEITEM InsertItem(CTreeCtrl& _treeCtrl, LPCTSTR _strItem, DWORD_PTR _dwData, HTREEITEM _hParent=TVI_ROOT);

CString IntToStr(int _iData);

CString	GetCurTimeStr(int _iType=0);

CString BrowseFolder();

CString AnsiToUTF8(const char* _pstrIn);

CString GetCurModulePath();

int		ShowImage(CWnd* _pWnd, CString _cstrPath);

int		GetFaceFileType(CString _cstrFileName);
CString GetTimeStr(NVS_FILE_TIME& _tInfo);

BOOL	CheckFaceName( CString _str );
BOOL	CheckFaceName(byte _cChar);
bool	IsContainSubStr(const CString& _strSource, const CString& _strChar);

bool	CreateDirectoryByPath(CString strFullPath);

void	Utf8ToAnsi( const char* _pstrIn,CString &_strOut);

void* MallocMsgMemory(int _iSize);
void  FreeMsgMemory(void* _pMemory);
void  DestroyMsgMemory();

#endif

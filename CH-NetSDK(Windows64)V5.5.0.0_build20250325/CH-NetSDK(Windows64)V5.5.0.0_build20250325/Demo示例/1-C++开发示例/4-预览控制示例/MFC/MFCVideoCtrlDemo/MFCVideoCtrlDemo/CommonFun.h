#ifndef __COMMONFUN_H__
#define __COMMONFUN_H__

#include "string.h"
#include "LanguageManager.h"
//#include "DeviceManager.h"
#include "LogManager.h"
//#include "MessageManager.h"

enum DlgType
{
	n_Dlg_ADD,
	n_Dlg_Edit,
};

#define MAX_LIST_COLUMN_NAME_LEN 128

#define DLL_LOAD_SUCCESS         0
#define DLL_NOT_FOUND           -1
#define DLL_RELOAD              -2
#define DLL_FUN_EXPORT_FAILED   -3
#define TYPEDEF_FUNCTION(funcName)  typedef int (__stdcall* pf##funcName)
#define DECLARE_FUNCTION(funcName)	extern pf##funcName funcName
#define TYPEDEF_FUNCTION2(funcName)  typedef int (__cdecl* pf##funcName)
#define IMPLEMENT_FUNCTION(funcName)  pf##funcName funcName = NULL
//#define EXPORT_FUNCTION(hDll, funcName)	if(NULL == (funcName = (pf##funcName)::GetProcAddress(hDll, #funcName))) {return DLL_FUN_EXPORT_FAILED;}
#define EXPORT_FUNCTION(hDll, funcName)	funcName = (pf##funcName)::GetProcAddress(hDll, #funcName);
#define  TYPEDEF_FUNCTION_RET(retType, funcName) typedef retType (__stdcall* pf##funcName)
#define DH_PARAM_SEPARATOR ','


//IP check
int				IsValidIP(const char* _cIP);
int				IsValidIP(const char* _cIP,const char* _cMask,const char* _cGateway);

//log management
void SetLogCtrl(CListCtrl* _plvLog);
int SetLogFileName(char* _pcFileName,int _iLen = 0);
int SetLogCaps(unsigned int _uRemove,unsigned int _uAdd);
unsigned int GetLogCaps();
void AddLog(int _iLogType,const char* _pcDevFmt,const char* _pcLogFmt, ...);

void OutPutLogMsg(const char* _pcLogFmt, ...);

void Destroy();



//device verification
bool IsDVR(const int _iProductModel);

CString IntToString(int _iData);

int SetLocalSaveDirectory(CString _strPath);
CString GetLocalSaveDirectory();

CString Bytes2HexString(unsigned char* _pcSrc,int _iSrcLen );


bool Inner_SafeCloseFile(FILE** _ppFile);

CString IntToCString(int _iPara, CString *_pCstr = NULL);

bool IfPointInPolygon(POINT _ptCur, POINT *_ptPolygon, int _iPtSum = 4);
bool IfHaveInterSection(POINT _ptLine1Start, POINT _ptLine1End, POINT _ptLine2Start, POINT _ptLine2End);
bool IfPolygonIntersect(POINT *_ptPolygon1, int _iPoly1, POINT *_ptPolygon2, int _iPoly2);
bool IfPolygonInPolygon(POINT *_ptPolygon1, int _iPoly1, POINT *_ptPolygon2, int _iPoly2);


//Get the subscript ys according to the ComboBox ItemData
int GetCboSel(CComboBox* _pComboBox, int _iItemData);

int FillStringBufferGb2312(char* _pcStringBuffer, int _iStringLen, char* _pcSrc, int _iSrcLen);

void SetListCtrlColumn(CListCtrl& _lstListCtrl, int _iIndex, CString _cstrColumnName);

#endif //__COMMONFUN_H__

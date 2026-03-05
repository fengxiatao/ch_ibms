#ifndef _BASE_WINDOW_H
#define _BASE_WINDOW_H

#include "afxwin.h"
#include "resource.h"
#include "Common/CommonFun.h"
#include "Include/NVSSDK_INTERFACE.h"
#include "Include/MP4_INTERFACE.h"
#include "RetValue.h"
using namespace NVSSDK_INTERFACE;
using namespace MP4_INTERFACE;

#define NORMAL_USE			0
#define RIVER_USE			1

#define ASYN_MODE	0
#define SYNC_MODE	1

//#define GDYHRW_DZ

typedef struct tagRegisterInfo
{
	BOOL	blUseNslook;
	char	cRegUser[32];
	char	cRegPwd[32];
	char	cRegIP[64];
	int		iRegPort;
	BOOL	blUseIpV6;
} RegisterInfo;

class CLS_BaseWindow :
	public CDialog
{
public:
	CLS_BaseWindow(UINT nIDTemplate,CWnd* pParentWnd = NULL);//need this 
	~CLS_BaseWindow();
	virtual void OnCancel(){};
	virtual void OnOK(){};
	virtual void OnLogoffDevice(int _iLogonID){};
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo){}
	virtual void OnLanguageChanged(int _iLanguage){}
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser){}
	virtual void OnAlarmNotify(int _iLogonID, int _iChannelNo, int _iAlarmState,int _iAlarmType,int _iUserData){}
	virtual void OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser){}
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData){}
	virtual void ChangeCurrentPage();
	virtual void SetRegisterInfo(RegisterInfo* _ptInfo){};
};

#endif

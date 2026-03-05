#include "stdafx.h"
#include "DeviceControl.h"
#include <cstring>

CLS_DeviceControl::CLS_DeviceControl()
{
	m_pfGetControlCode = NULL;
	m_hInst = NULL;
	memset(m_cDeviceType,0,sizeof(m_cDeviceType));
}
//-------------------------------------------------------------------------------

CLS_DeviceControl::~CLS_DeviceControl()
{
	if(m_hInst)
	{
		FreeLibrary(m_hInst);
		m_hInst = NULL;
	}
}
//-------------------------------------------------------------------------------

int CLS_DeviceControl::Initialize(char* _pcDevictType)//Dynamic loading of the dll file corresponding to the device type
{
	if (strlen(_pcDevictType) <= 0)
	{
		return -1;
	}

	if (0 == strcmp(_pcDevictType,m_cDeviceType))
	{
		return 1;
	}

	m_cDeviceType[0] = '\0';
	if (m_hInst)//If m_hInst is in use, release it
	{
		FreeLibrary(m_hInst);
		m_hInst = NULL;
	}

	CString strDllFileName;//Get the dll file name corresponding to the device
	strDllFileName.Format(_T("DeviceDll\\%s.dll"),_pcDevictType);	
	m_hInst = LoadLibrary(strDllFileName);//Reload dll dynamic library
	if (NULL == m_hInst)//Exit if loading fails
	{
		
		return -1;
	}
	
	m_pfGetControlCode = (pfGetControlCode)GetProcAddress(m_hInst,"GetControlCode");//Get the GetControlCode function
	if(NULL == m_pfGetControlCode)
	{
		return -1;
	}
	strcpy_s(m_cDeviceType,sizeof(m_cDeviceType),_pcDevictType);
	return 0;
}

//According to the device type _DevType and action code _iActionCode, modify the control parameter _cParam
int CLS_DeviceControl::GetCtrlCode(char* _pcDevictType,int _iActionCode,CONTROL_PARAM* _cParam)
{
	int iRet = Initialize(_pcDevictType);	
	if (iRet < 0)
	{
		return iRet;
	}	
	iRet = m_pfGetControlCode(_iActionCode,_cParam);
	if ( iRet == 1)
	{
		return 0;
	}
	else
	{
		return -1;
	}	
}
#include "stdafx.h"
#include "NetVideoInterface.h"

HINSTANCE hNVSSDK = NULL;

pfNetVideo_ShowConfigWindow NetVideo_ShowConfigWindow = NULL;
pfNetVideo_GetParam NetVideo_GetParam = NULL;
pfNetVideo_SetParam NetVideo_SetParam = NULL;
pfNetVideo_GetParaChangeNotify NetVideo_GetParaChangeNotify = NULL;
pfNetVideo_GetMainNotify NetVideo_GetMainNotify = NULL;
pfNetVideo_GetAlarmNotify NetVideo_GetAlarmNotify = NULL;

int LoadNetVideoConfig()
{
	int iErr = -1;
	if (s_hInstance)
	{
		return 1;
	}

	s_hInstance = ::LoadLibrary(_T("NetVideoConfig.dll"));
	//s_hInstance = ::LoadLibrary((LPCTSTR)"NetVideoConfig.dll");
	if (s_hInstance)
	{
		NetVideo_ShowConfigWindow = (pfNetVideo_ShowConfigWindow)GetProcAddress(s_hInstance, "NetVideo_ShowConfigWindow");
		NetVideo_GetParaChangeNotify = (pfNetVideo_GetParaChangeNotify)GetProcAddress(s_hInstance, "NetVideo_GetParaChangeNotify");
		NetVideo_GetMainNotify = (pfNetVideo_GetMainNotify)GetProcAddress(s_hInstance, "NetVideo_GetMainNotify");
		NetVideo_GetAlarmNotify = (pfNetVideo_GetAlarmNotify)GetProcAddress(s_hInstance,"NetVideo_GetAlarmNotify");
		NetVideo_GetParam = (pfNetVideo_GetParam)GetProcAddress(s_hInstance, "NetVideo_GetParam");
		NetVideo_SetParam = (pfNetVideo_SetParam)GetProcAddress(s_hInstance, "NetVideo_SetParam");
		return 0;
	}
	else
	{
		iErr = GetLastError();
	}

	return iErr;
}

int FreeNetVideoConfig()
{
	if (s_hInstance)
	{
		FreeLibrary(s_hInstance);
		s_hInstance = NULL;
	}

	return 0;
}

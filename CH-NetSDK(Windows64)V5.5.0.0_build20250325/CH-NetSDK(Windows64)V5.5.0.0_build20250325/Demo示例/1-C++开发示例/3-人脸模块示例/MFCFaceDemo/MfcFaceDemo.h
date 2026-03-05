
#pragma once

#ifndef __AFXWIN_H__
	#error "Include "stdafx. h" before including this file to generate a PCH file"
#endif

#include "resource.h"		// Primary symbol


class CFaceDemoApp : public CWinApp
{
public:
	CFaceDemoApp();

// rewrite
	public:
	virtual BOOL InitInstance();

// realization

	DECLARE_MESSAGE_MAP()
};

extern CFaceDemoApp theApp;
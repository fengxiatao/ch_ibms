
// VideoDisplay.h : PROJECT_NAME Application's main header file
//

#pragma once

#ifndef __AFXWIN_H__
	#error "Include "stdafx.h" before including this file to generate PCH files"
#endif

#include "resource.h"		// main symbol


// CVideoDisplayApp:
// See VideoDisplay.cpp for an implementation of this class
//

class CVideoDisplayApp : public CWinApp
{
public:
	CVideoDisplayApp();

// rewrite
	public:
	virtual BOOL InitInstance();

// accomplish

	DECLARE_MESSAGE_MAP()
};

extern CVideoDisplayApp theApp;
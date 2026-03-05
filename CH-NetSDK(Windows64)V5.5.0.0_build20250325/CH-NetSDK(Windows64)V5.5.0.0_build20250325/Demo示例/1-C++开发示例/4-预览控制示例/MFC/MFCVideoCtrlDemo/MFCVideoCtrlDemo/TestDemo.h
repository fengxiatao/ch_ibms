// TestDemo.h : the main header file for the PROJECT_NAME application
//

#pragma once

#ifndef __AFXWIN_H__
	#error "Include "stdafx.h" before including this file to generate PCH files"
#endif

#include "resource.h" // main symbol


// CTestDemoApp:
// For the implementation of this class, see TestDemo.cpp
//

class CTestDemoApp : public CWinApp
{
public:
	CTestDemoApp();

// rewrite
	public:
	virtual BOOL InitInstance();

// accomplish

	DECLARE_MESSAGE_MAP()
};

extern CTestDemoApp theApp;
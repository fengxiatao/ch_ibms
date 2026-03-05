
// VideoDisplay.cpp : Define the class behavior of the application¡£
//

#include "stdafx.h"
#include <Windows.h>
#include "NetSdk.h"
#include "MacroDefine.h"
#include "VideoDisplay.h"
#include "ConnectServer.h"
#include "VideoDisplayDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CVideoDisplayApp

BEGIN_MESSAGE_MAP(CVideoDisplayApp, CWinApp)
	ON_COMMAND(ID_HELP, &CWinApp::OnHelp)
END_MESSAGE_MAP()


// CVideoDisplayApp structure

CVideoDisplayApp::CVideoDisplayApp()
{
	// TODO: add construction code here, 
	// Put all important initialization in InitInstance
}


// The only one CVideoDisplayApp object

CVideoDisplayApp theApp;


// CVideoDisplayApp initialization

BOOL CVideoDisplayApp::InitInstance()
{
	// If an application running on Windows XP manifests
	// Use ComCtl32.dll version 6 or higher to enable visualization,
	//InitCommonControlsEx() is required. Otherwise, the window cannot be created.
	INITCOMMONCONTROLSEX InitCtrls;
	InitCtrls.dwSize = sizeof(InitCtrls);
	// set it up to include all to be used in the application
	// Public control class.
	InitCtrls.dwICC = ICC_WIN95_CLASSES;
	InitCommonControlsEx(&InitCtrls);

	CWinApp::InitInstance();

	AfxEnableControlContainer();

	// standard initialization
	// if these functions are not used and want to reduce
	// the size of the final executable, the following should be removed
	// Unnecessary specific initialization routines
	// Change the registry key used to store settings
	// TODO: The string should be modified appropriately,
	// For example, change to company or organization name
	SetRegistryKey(_T("AppWizard Generated Local Application"));

	

	CVideoDisplayDlg dlg;
	m_pMainWnd = &dlg;
	INT_PTR nResponse = dlg.DoModal();

	
	if (nResponse == IDOK)
	{
		// TODO: when to place the handler here
		// "OK" to close the dialog's code
	}
	else if (nResponse == IDCANCEL)
	{
		// TODO: when to place the handler here
		// "Cancel" code to close the dialog
	}

	// Since the dialog has been closed, FALSE will be returned to exit the application,
	//  Instead of starting the app's message pump.
	return FALSE;
}

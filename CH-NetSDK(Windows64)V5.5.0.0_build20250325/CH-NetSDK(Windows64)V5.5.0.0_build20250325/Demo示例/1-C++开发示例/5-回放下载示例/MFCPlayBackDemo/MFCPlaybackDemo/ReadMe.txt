================================================================================
    MICROSOFT Foundation Class Library: VideoDisplay Project Overview
===============================================================================

The application wizard has created this VideoDisplay application for you. This application not only demonstrates the basics of using Microsoft Foundation Classes, but also serves as a starting point for writing applications.

This file contains a summary of the contents of the various files that make up the VideoDisplay application.

VideoDisplay.vcproj
    This is the main project file for a VC++ project generated using AppWizard.
    It contains information about the Visual C++ version of the makefile, as well as information about the platform, configuration, and project features selected using the AppWizard.

VideoDisplay.h
    This is the main header file of the application. It includes other project-specific header files (including Resource.h), and declares the CVideoDisplayApp application class.

VideoDisplay.cpp
    This is the main application source file that contains the application class CVideoDisplayApp.

VideoDisplay.rc
    This is a list of all Microsoft Windows resources used by the program. It includes icons, bitmaps, and cursors stored in the RES subdirectory. This file can be edited directly in Microsoft Visual C++. Project resources are located in 2052.

res\VideoDisplay.ico
    This is the icon file used as the application icon. This icon is included in the main resource file VideoDisplay.rc.

res\VideoDisplay.rc2
    This file contains resources not edited by Microsoft Visual C++. You should put all resources that are not editable by the resource editor in this file.


/////////////////////////////////////////////////////////////////////////////

The AppWizard creates a dialog class:

VideoDisplayDlg.h, VideoDisplayDlg.cpp - Dialogs
    These files contain the CVideoDisplayDlg class. This class defines the behavior of the application's main dialog. The dialog's template is in VideoDisplay.rc, which can be edited in Microsoft Visual C++.


/////////////////////////////////////////////////////////////////////////////

Other functions:

ActiveX controls
    The application includes support for using ActiveX controls.

Printing and print preview support
    AppWizard has generated code for handling print, print setup, and print preview commands by calling member functions in the CView class from the MFC library.

/////////////////////////////////////////////////////////////////////////////

Other standard documents:

StdAfx.h, StdAfx.cpp
    These files are used to generate a precompiled header (PCH) file named VideoDisplay.pch and a precompiled type file named StdAfx.obj.

Resource.h
    This is the standard header file that defines the new resource ID.
    Microsoft Visual C++ will read and update this file.

VideoDisplay.manifest
Windows XP uses an application manifest file to describe an application's dependencies on a specific version of a side-by-side assembly. The loader uses this information to load the appropriate assembly from the assembly cache or to load private information from the application. The application manifest may be included for redistribution as an external .manifest file installed in the same folder as the application executable, or it may be included in the executable as a resource.
/////////////////////////////////////////////////////////////////////////////

Additional Notes:

The AppWizard uses "TODO:" to indicate which sections of source code should be added or customized.

If your application uses MFC in a shared DLL, you will need to redistribute the MFC DLL. If the application uses a different locale than the operating system, the corresponding localized resource MFC90XXX.DLL will also have to be redistributed.
For more information on these two topics, see the section on redistributing Visual C++ applications in the MSDN documentation.

/////////////////////////////////////////////////////////////////////////////
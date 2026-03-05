================================================================================
MICROSOFT Basic Class Library: Overview of the TestDemo Project
===============================================================================

The AppWizard has created this TestDemo application for you. This application not only demonstrates the basic use of Microsoft Foundation Classes, but also serves as a starting point for your application.

This file provides an overview of the contents of each of the files that make up the TestDemo application.

TestDemo.vcproj
This is the main project file for a VC++ project generated using AppWizard.
It contains information about the version of Visual C++ that generated the file, as well as information about the platform, configuration, and project features selected using the AppWizard.

TestDemo.h
This is the main header file of the application. It includes other project-specific header files (including Resource.h), and declares the CTestDemoApp application class.

TestDemo.cpp
This is the main application source file that contains the application class CTestDemoApp.

TestDemo.rc
This is a list of all Microsoft Windows resources used by the program. It includes icons, bitmaps, and cursors stored in the RES subdirectory. This file can be edited directly in Microsoft Visual C++. Project resources are located in 2052.

res\TestDemo.ico
This is the icon file used as the application icon. This icon is included in the main resource file TestDemo.rc.

res\TestDemo.rc2
This file contains resources that are not edited in Microsoft Visual C++. You should put all resources that are not editable by the resource editor in this file.


/////////////////////////////////////////////////////////////////////////////

The AppWizard creates a dialog class:

TestDemoDlg.h, TestDemoDlg.cpp - Dialogs
These files contain the CTestDemoDlg class. This class defines the behavior of the application's main dialog. The dialog's template is in TestDemo.rc, which can be edited in Microsoft Visual C++.


/////////////////////////////////////////////////////////////////////////////

Other functions:

ActiveX controls
The application includes support for using ActiveX controls.

/////////////////////////////////////////////////////////////////////////////

Other standard documents:

StdAfx.h, StdAfx.cpp
These files are used to generate a precompiled header (PCH) file named TestDemo.pch and a precompiled type file named StdAfx.obj.

Resource.h
This is the standard header file that defines the new resource ID.
Microsoft Visual C++ reads and updates this file.

TestDemo.manifest
Application manifest files are used by Windows XP to describe applications
A dependency on a specific version of a side-by-side assembly. The loader uses this
information to load the appropriate assembly from the assembly cache or
Load private information from the application. The application manifest may be used for redistribution as
An external .manifest file installed in the same folder as the application executable includes,
It may also be included in the executable as a resource.
/////////////////////////////////////////////////////////////////////////////

Additional Notes:

The AppWizard uses "TODO:" to indicate which sections of source code should be added or customized.

If your application uses MFC in shared DLLs, you will need to redistribute those MFC DLLs; if your application is in a different language than the operating system's current locale, you will also need to redistribute the corresponding localized resource MFC90XXX.DLL. For more information on these two topics, see the section on Redistributing Visual C++ applications in the MSDN documentation.

/////////////////////////////////////////////////////////////////////////////

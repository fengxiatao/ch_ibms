================================================================================
    MICROSOFT Basic Class Library: Overview of FaceDemo Project
===============================================================================

The Application Wizard has created this FaceDemo application for you. This application not only demonstrates the basic knowledge of using Microsoft basic classes, but also serves as a starting point for writing applications.

This file contains a summary of the contents of the individual files that make up the FaceDemo application.

FaceDemo.vcproj
    This is the main project file of the VC++project generated using the application wizard.
    It contains information about the Visual C++version of the makefile, as well as about the platform, configuration, and project features selected using the Application Wizard.

FaceDemo.h
    This is the main header file for the application. It includes other project specific header files (including Resource. h) and declares the CFaceDemoApp application class.

FaceDemo.cpp
    This is the main application source file that contains the application class CFaceDemoApp.

FaceDemo.rc
    This is a list of all Microsoft Windows resources used by the program. It includes icons, bitmaps, and cursors stored in the RES subdirectory. This file can be edited directly in Microsoft Visual C++. Project resources are located in 2052.

res\FaceDemo.ico
    This is an icon file used as an application icon. This icon is included in the main asset file FaceDemo Rc.

res\FaceDemo.rc2
    This file contains resources that are not edited by Microsoft Visual C++. You should place all resources that cannot be edited by the Resource Editor in this file.


/////////////////////////////////////////////////////////////////////////////

The application wizard creates a dialog class:

FaceDemoDlg.h, FaceDemoDlg. Cpp - Dialog
    These files contain the CFaceDemoDlg class. This class defines the behavior of the application's main dialog box. The template for this dialog is located in FaceDemo Rc, the file can be edited in Microsoft Visual C++.


/////////////////////////////////////////////////////////////////////////////

Other functions:

ActiveX Control
    The application includes support for using ActiveX controls.

/////////////////////////////////////////////////////////////////////////////

Other standard documents:

StdAfx.h, StdAfx.cpp
    These files are used to generate a file named FaceDemo The pch precompiled header (PCH) file and the file named StdAfx OBJ precompiled type file.

Resource.h
    This is the standard header file, which defines the new resource ID.
    Microsoft Visual C++ will read and update this file.

FaceDemo.manifest
	Windows XP uses an application manifest file to describe an application's dependency on a particular version of a parallel assembly. The loader uses this information to load the corresponding assembly from the assembly cache or to load private information from the application. The application manifest may be included as an external. manifest file installed in the same folder as the application executable file for republishing, or it may be included as a resource in the executable file.
/////////////////////////////////////////////////////////////////////////////

Other notes:

The application wizard uses "TODO:" to indicate which parts of the source code should be added or customized.

If your application uses MFC in a shared DLL, you will need to republish the MFC DLL. If the application uses a different locale than the operating system, you will also have to republish the corresponding localized resource MFC90XXX.DLL.
For more information on these two topics, see the section on republishing Visual C++applications in the MSDN documentation.

/////////////////////////////////////////////////////////////////////////////
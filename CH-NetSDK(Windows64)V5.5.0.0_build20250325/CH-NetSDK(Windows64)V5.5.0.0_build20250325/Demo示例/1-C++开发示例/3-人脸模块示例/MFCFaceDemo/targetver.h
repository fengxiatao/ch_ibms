
#pragma once

// The following macros define the minimum required platform. The minimum required platform
// Windows, Internet Explorer and other products with the functions required to run applications
//The earliest version. Macros can
//Normal operation.

//If you must target a platform that is lower than the version specified below, modify the following definition.
//Refer to MSDN for the latest information on the corresponding values for different platforms.
#ifndef WINVER                          //The minimum required platform is Windows Vista.
#define WINVER 0x0600           //Change this value to the corresponding value for other versions of Windows.
#endif

#ifndef _WIN32_WINNT            //The specified minimum platform is Windows Vista.
#define _WIN32_WINNT 0x0600     //Change this value to the corresponding value for other versions of Windows.
#endif

#ifndef _WIN32_WINDOWS          //The specified minimum platform is Windows 98.
#define _WIN32_WINDOWS 0x0410	//Change this value to the appropriate value for Windows Me or later.
#endif

#ifndef _WIN32_IE                       //The minimum platform specified is Internet Explorer 7.0.
#define _WIN32_IE 0x0700        //Change this value to the corresponding value for other versions of IE.
#endif


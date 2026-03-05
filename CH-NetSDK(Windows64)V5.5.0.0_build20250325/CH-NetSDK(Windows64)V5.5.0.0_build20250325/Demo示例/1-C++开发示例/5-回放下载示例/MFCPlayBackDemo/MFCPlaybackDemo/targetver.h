
#pragma once

// The following macros define the minimum platform required. Minimum platform required
// is for products such as Windows, Internet Explorer, etc. that have the functionality required to run the application
// The earliest version. By enabling all available features on the specified version of the platform and earlier, the macro can
// normal work.

// If you must target a platform lower than the version specified below, please modify the following definitions.
// For the latest information on the corresponding values ??for different platforms, please refer to MSDN.
#ifndef WINVER // Specifies that the minimum platform required is Windows Vista.
#define WINVER 0x0600 // Change this value to the appropriate value for other versions of Windows.
#endif

#ifndef _WIN32_WINNT // Specifies that the minimum platform required is Windows Vista.
#define _WIN32_WINNT 0x0600 // Change this value to the appropriate value for other versions of Windows.
#endif

#ifndef _WIN32_WINDOWS // Specifies that the minimum platform required is Windows 98.
#define _WIN32_WINDOWS 0x0410 // Change this value to an appropriate value for Windows Me or later.
#endif

#ifndef _WIN32_IE // Specifies that the minimum required platform is Internet Explorer 7.0.
#define _WIN32_IE 0x0700 // Change this value to the appropriate value for other versions of IE.
#endif


#pragma once

#include "NetClientTypes.h"

typedef void       td_MP4FileHandle;

namespace MP4_INTERFACE
{
	extern	HINSTANCE hMP4;
	extern	HRESULT	LoadMp4Dll();

	/************************************************* 
	  Function name: MP4_ConvertSdvToMp4
	  Function description: Used to convert SDV files into standard MP4 files
	  Input parameters: _pcSrcFilePath , source SDV file path
	  Input parameters: _pcDstFilePath , the target MP4 file path to be generated
	  Output parameters:
	  Return: 0 for success -1 for failure
	*************************************************/
	typedef int (__cdecl * MP4_ConvertSdvToMp4)(const char* _pcSrcFilePath, char* _pcDstFilePath);
	extern MP4_ConvertSdvToMp4 TMP4_ConvertSdvToMp4;
}

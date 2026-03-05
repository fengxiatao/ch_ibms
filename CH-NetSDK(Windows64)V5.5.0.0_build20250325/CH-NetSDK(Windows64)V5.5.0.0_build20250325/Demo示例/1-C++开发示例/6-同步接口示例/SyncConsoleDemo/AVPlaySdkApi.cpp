#include "AVPlaySdkApi.h"

#define  CHECK_NULL_RETURN(src, ret)						if (NULL == (src)){return ret;}
#define  CHECK_NULL_RETURN_VOID(P)						if (NULL == (P)){return;}

static void* SdkApiExport(void* _pvHandle, const char* _pcSymbol)
{
#if defined(_WIN32) || defined(_WIN64)
	return GetProcAddress((HMODULE)_pvHandle, _pcSymbol);
#else
	return dlsym(_pvHandle, _pcSymbol);
#endif
}

CLS_AVPlaySdkApi* CLS_AVPlaySdkApi::m_pclsInstance = NULL;

CLS_AVPlaySdkApi::CLS_AVPlaySdkApi()
{
	m_pvAVPlaySdkHdl = NULL;

	m_pTC_CreateSystem					= NULL;
	m_pTC_DeleteSystem					= NULL;
	m_pTC_CreatePlayer					= NULL;
	m_pTC_CreatePlayer					= NULL;
	m_pTC_CreatePlayerFromFile			= NULL;
	m_pTC_CreatePlayerFromStream		= NULL;
	m_pTC_CreatePlayerFromStreamEx		= NULL;
	m_pTC_CreatePlayerFromVoD			= NULL;
	m_pTC_DeletePlayer					= NULL;
	m_pTC_DeletePlayerEx				= NULL;
	m_pTC_RegisterEventMsg				= NULL;
	m_pTC_RegisterNotifyPlayToEnd		= NULL;
	m_pTC_RegisterNotifyGetDecAV		= NULL;
	m_pTC_RegisterDrawFun				= NULL;
	m_pTC_SetPlayRect					= NULL;
	m_pTC_SetPlayRectEx					= NULL;
	m_pTC_Play							= NULL;
	m_pTC_Stop							= NULL;
	m_pTC_GoBegin						= NULL;
	m_pTC_GoEnd							= NULL;
	m_pTC_StepForward					= NULL;
	m_pTC_StepBackward					= NULL;
	m_pTC_FastForward					= NULL;
	m_pTC_SlowForward					= NULL;
	m_pTC_FastBackward					= NULL;
	m_pTC_PlayAudio						= NULL;
	m_pTC_StopAudio						= NULL;
	m_pTC_SetAudioVolume				= NULL;
	m_pTC_SetAudioVolumeEx				= NULL;
	m_pTC_GetAudioVolumeEx				= NULL;
	m_pTC_Seek							= NULL;
	m_pTC_SeekEx						= NULL;
	m_pTC_GetFrameRate					= NULL;
	m_pTC_GetPlayingFrameNum			= NULL;
	m_pTC_GetFrameCount					= NULL;
	m_pTC_GetBeginEnd					= NULL;
	m_pTC_GetFileName					= NULL;
	m_pTC_CapturePic					= NULL;
	m_pTC_CaptureBmpPic					= NULL;
	m_pTC_CaptureJpegPic				= NULL;
	m_pTC_CapturePicture				= NULL;
	m_pTC_CapturePicData				= NULL;
	m_pTC_PutStreamToPlayer				= NULL;
	m_pTC_StartMonitorCPU				= NULL;
	m_pTC_StopMonitorCPU				= NULL;
	m_pTC_GetVersion					= NULL;
	m_pTC_SetVoDPlayerOver				= NULL;
	m_pTC_SetCleanVoDBuffer				= NULL;
	m_pTC_GetPlayTime					= NULL;
	m_pTC_Pause							= NULL;
	m_pTC_GetVideoParam					= NULL;
	m_pTC_DrawRect						= NULL;
	m_pTC_ResetPlayWnd					= NULL;
	m_pTC_AddEZoom						= NULL;
	m_pTC_SetEZoom						= NULL;
	m_pTC_RemoveEZoom					= NULL;
	m_pTC_ResetEZoom					= NULL;
	m_pTC_SearchVCAInfo					= NULL;
	m_pTC_GetBeginEndTimeStamp			= NULL;
	m_pTC_SetVideoDecryptKey			= NULL;
	m_pTC_GetVideoDecryptKey			= NULL;	
	m_pTC_RegisterCommonEventCallBack	= NULL;
	m_pTC_RegisterCommonEventCallBackEx = NULL;
	m_pTC_RegisterCommonEventCallBack_V4 = NULL;
	m_pTC_SetModeRule					= NULL;
	m_pTC_DrawPolygonOnLocalVideo		= NULL;
	m_pTC_GetMarkInfo					= NULL;
	m_pTC_SetMarkInfo					= NULL;
	m_pTC_SeekMark						= NULL;
	m_pTC_StartCaptureFile				= NULL;
	m_pTC_StopCaptureFile				= NULL;
	m_pTC_SetStreamBufferSize			= NULL;
	m_pTC_SetFrameListBufferSize		= NULL;
	m_pTC_SetVerticalSync				= NULL;
	m_pTC_StartAudioCapture				= NULL;
	m_pTC_StopAudioCapture				= NULL;
	m_pTC_RegisterNotifyAudioCapture	= NULL;
	m_pTC_SetFishEyeCorrectEnable		= NULL;
	m_pTC_GetFishEyeCorrectEnable		= NULL;
	m_pTC_CreateFishEyeCorrect			= NULL;
	m_pTC_DeleteFishEyeCorrect			= NULL;
	m_pTC_SetFishEyeCorrectPara			= NULL;
	m_pTC_GetFishEyeCorrectPara			= NULL;
	m_pTC_SetFishEyeCorrectWnd			= NULL;
	m_pTC_SetFishEyeCorrectCallBack		= NULL;
	m_pTC_CapturePictureEx				= NULL;
	m_pTC_PlayerControl					= NULL;
	m_pTC_StartFECTrack					= NULL;
	m_pTC_StopFECTrack					= NULL;
	m_pTC_SetAVMode						= NULL;
	m_pTC_SetPlayerAVMode				= NULL;
	m_pTC_GetCurFrameNo					= NULL;
	m_pTC_GetFrameListCount				= NULL;
	m_pTC_SetPlayDelay					= NULL;
	m_pTC_SetDecCallBack				= NULL;

}

CLS_AVPlaySdkApi::~CLS_AVPlaySdkApi()
{
	FreeSdkLibrary();	
}

CLS_AVPlaySdkApi* CLS_AVPlaySdkApi::Instance(void)
{
	if(NULL == m_pclsInstance)
	{
		m_pclsInstance = new CLS_AVPlaySdkApi();	
	}

	return m_pclsInstance;
}

void CLS_AVPlaySdkApi::Destroy()
{
	if (NULL != m_pclsInstance)
	{
		delete m_pclsInstance;
		m_pclsInstance = NULL;
	}
}

int CLS_AVPlaySdkApi::LoadSdkLibrary()
{
	int iRet = RET_SUCCESS;
	if(NULL != m_pvAVPlaySdkHdl)
	{
		goto END;
	}

#if defined(_WIN32) || defined(_WIN64)
	m_pvAVPlaySdkHdl = LoadLibrary("PlaySdkM4.dll");
#else
	m_pvAVPlaySdkHdl = dlopen("libplaysdk.so", RTLD_LAZY);
#endif
	if(NULL == m_pvAVPlaySdkHdl)
	{
		printf( "LoadSdkLibrary failed, errno=%d.\n", errno);
		iRet = RET_FAILED;
		goto END;
	}

	m_pTC_CreateSystem						= (pTC_CreateSystem)           				SdkApiExport(m_pvAVPlaySdkHdl, "TC_CreateSystem");
	m_pTC_DeleteSystem						= (pTC_DeleteSystem)           				SdkApiExport(m_pvAVPlaySdkHdl, "TC_DeleteSystem");
	m_pTC_CreatePlayer			   			= (pTC_CreatePlayer)						SdkApiExport(m_pvAVPlaySdkHdl, "TC_CreatePlayer");
	m_pTC_CreatePlayerFromFile				= (pTC_CreatePlayerFromFile)   				SdkApiExport(m_pvAVPlaySdkHdl, "TC_CreatePlayerFromFile");
	m_pTC_CreatePlayerFromStream  			= (pTC_CreatePlayerFromStream)				SdkApiExport(m_pvAVPlaySdkHdl, "TC_CreatePlayerFromStream");
	m_pTC_CreatePlayerFromStreamEx			= (pTC_CreatePlayerFromStreamEx)			SdkApiExport(m_pvAVPlaySdkHdl, "TC_CreatePlayerFromStreamEx");
	m_pTC_CreatePlayerFromVoD     			= (pTC_CreatePlayerFromVoD)    				SdkApiExport(m_pvAVPlaySdkHdl, "TC_CreatePlayerFromVoD");
	m_pTC_DeletePlayer						= (pTC_DeletePlayer)           				SdkApiExport(m_pvAVPlaySdkHdl, "TC_DeletePlayer");
	m_pTC_DeletePlayerEx					= (pTC_DeletePlayerEx)           			SdkApiExport(m_pvAVPlaySdkHdl, "TC_DeletePlayerEx");
	m_pTC_RegisterEventMsg					= (pTC_RegisterEventMsg)       				SdkApiExport(m_pvAVPlaySdkHdl, "TC_RegisterEventMsg");
	m_pTC_RegisterNotifyPlayToEnd			= (pTC_RegisterNotifyPlayToEnd)				SdkApiExport(m_pvAVPlaySdkHdl, "TC_RegisterNotifyPlayToEnd");
	m_pTC_RegisterNotifyGetDecAV			= (pTC_RegisterNotifyGetDecAV)				SdkApiExport(m_pvAVPlaySdkHdl, "TC_RegisterNotifyGetDecAV");
	m_pTC_RegisterDrawFun					= (pTC_RegisterDrawFun)						SdkApiExport(m_pvAVPlaySdkHdl, "TC_RigisterDrawFun");
	m_pTC_SetPlayRect             			= (pTC_SetPlayRect)            				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetPlayRect");
	m_pTC_SetPlayRectEx           			= (pTC_SetPlayRectEx)          				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetPlayRectEx");
	m_pTC_Play				       			= (pTC_Play)				   				SdkApiExport(m_pvAVPlaySdkHdl, "TC_Play");
	m_pTC_Stop                    			= (pTC_Stop)                   				SdkApiExport(m_pvAVPlaySdkHdl, "TC_Stop");
	m_pTC_GoBegin                 			= (pTC_GoBegin)                				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GoBegin");
	m_pTC_GoEnd				   				= (pTC_GoEnd)                  				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GoEnd");
	m_pTC_StepForward             			= (pTC_StepForward)            				SdkApiExport(m_pvAVPlaySdkHdl, "TC_StepForward");
	m_pTC_StepBackward            			= (pTC_StepBackward)           				SdkApiExport(m_pvAVPlaySdkHdl, "TC_StepBackward");
	m_pTC_FastForward             			= (pTC_FastForward)            				SdkApiExport(m_pvAVPlaySdkHdl, "TC_FastForward");
	m_pTC_SlowForward             			= (pTC_SlowForward)            				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SlowForward");
	m_pTC_FastBackward            			= (pTC_FastBackward)           				SdkApiExport(m_pvAVPlaySdkHdl, "TC_FastBackward");
	m_pTC_PlayAudio               			= (pTC_PlayAudio)              				SdkApiExport(m_pvAVPlaySdkHdl, "TC_PlayAudio");
	m_pTC_StopAudio               			= (pTC_StopAudio)              				SdkApiExport(m_pvAVPlaySdkHdl, "TC_StopAudio");
	m_pTC_SetAudioVolume          			= (pTC_SetAudioVolume)         				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetAudioVolume");
	m_pTC_SetAudioVolumeEx        			= (pTC_SetAudioVolumeEx)       				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetAudioVolumeEx");
	m_pTC_GetAudioVolumeEx        			= (pTC_GetAudioVolumeEx)       				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetAudioVolumeEx");
	m_pTC_Seek                    			= (pTC_Seek)                   				SdkApiExport(m_pvAVPlaySdkHdl, "TC_Seek");
	m_pTC_SeekEx                  			= (pTC_SeekEx)                 				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SeekEx");
	m_pTC_GetFrameRate            			= (pTC_GetFrameRate)           				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetFrameRate");
	m_pTC_GetPlayingFrameNum      			= (pTC_GetPlayingFrameNum)     				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetPlayingFrameNum");
	m_pTC_GetFrameCount           			= (pTC_GetFrameCount)          				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetFrameCount");
	m_pTC_GetBeginEnd             			= (pTC_GetBeginEnd)            				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetBeginEnd");
	m_pTC_GetFileName             			= (pTC_GetFileName)            				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetFileName");
	m_pTC_CapturePic              			= (pTC_CapturePic)             				SdkApiExport(m_pvAVPlaySdkHdl, "TC_CapturePic");
	m_pTC_CaptureBmpPic           			= (pTC_CaptureBmpPic)          				SdkApiExport(m_pvAVPlaySdkHdl, "TC_CaptureJpegPic");
	m_pTC_CaptureJpegPic					= (pTC_CaptureJpegPic)          			SdkApiExport(m_pvAVPlaySdkHdl, "TC_CaptureBmpPic");
	m_pTC_CapturePicture					= (pTC_CapturePicture)          			SdkApiExport(m_pvAVPlaySdkHdl, "TC_CapturePicture");
	m_pTC_CapturePicData					= (pTC_CapturePicData)          			SdkApiExport(m_pvAVPlaySdkHdl, "TC_CapturePicData");
	m_pTC_PutStreamToPlayer       			= (pTC_PutStreamToPlayer)      				SdkApiExport(m_pvAVPlaySdkHdl, "TC_PutStreamToPlayer");
	m_pTC_StartMonitorCPU         			= (pTC_StartMonitorCPU)        				SdkApiExport(m_pvAVPlaySdkHdl, "TC_StartMonitorCPU");
	m_pTC_StopMonitorCPU          			= (pTC_StopMonitorCPU)         				SdkApiExport(m_pvAVPlaySdkHdl, "TC_StopMonitorCPU");
	m_pTC_GetVersion              			= (pTC_GetVersion)             				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetVersion");
	m_pTC_SetVoDPlayerOver        			= (pTC_SetVoDPlayerOver )      				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetVoDPlayerOver");
	m_pTC_SetCleanVoDBuffer       			= (pTC_SetCleanVoDBuffer)      				SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetCleanVoDBuffer");
	m_pTC_GetPlayTime             			= (pTC_GetPlayTime)							SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetPlayTime");
	m_pTC_Pause								= (pTC_Pause)               	    		SdkApiExport(m_pvAVPlaySdkHdl, "TC_Pause");
	m_pTC_GetVideoParam						= (pTC_GetVideoParam)						SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetVideoParam");
	m_pTC_DrawRect			 				= (pTC_DrawRect)							SdkApiExport(m_pvAVPlaySdkHdl, "TC_DrawRect");
	m_pTC_SetEZoom          				= (pTC_SetEZoom)							SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetEZoom");
	m_pTC_ResetEZoom        				= (pTC_ResetEZoom)							SdkApiExport(m_pvAVPlaySdkHdl, "TC_ResetEZoom");
	m_pTC_AddEZoom          				= (pTC_AddEZoom)							SdkApiExport(m_pvAVPlaySdkHdl, "TC_AddEZoom");
	m_pTC_RemoveEZoom       				= (pTC_RemoveEZoom)							SdkApiExport(m_pvAVPlaySdkHdl, "TC_RemoveEZoom");
	m_pTC_ResetPlayWnd      				= (pTC_ResetPlayWnd)						SdkApiExport(m_pvAVPlaySdkHdl, "TC_ResetPlayWnd");
	m_pTC_SearchVCAInfo						= (pTC_SearchVCAInfo)						SdkApiExport(m_pvAVPlaySdkHdl, "TC_SearchVCAInfo");
	m_pTC_GetBeginEndTimeStamp				= (pTC_GetBeginEndTimeStamp)				SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetBeginEndTimeStamp");
	m_pTC_SetVideoDecryptKey				= (pTC_SetVideoDecryptKey)					SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetVideoDecryptKey");
	m_pTC_GetVideoDecryptKey				= (pTC_GetVideoDecryptKey)					SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetVideoDecryptKey");
	m_pTC_RegisterCommonEventCallBack		= (pTC_RegisterCommonEventCallBack)			SdkApiExport(m_pvAVPlaySdkHdl, "TC_RegisterCommonEventCallBack");
	m_pTC_RegisterCommonEventCallBackEx		= (pTC_RegisterCommonEventCallBackEx)	 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_RegisterCommonEventCallBackEx");
	m_pTC_RegisterCommonEventCallBack_V4	= (pTC_RegisterCommonEventCallBack_V4)		SdkApiExport(m_pvAVPlaySdkHdl, "TC_RegisterCommonEventCallBack_V4");
	m_pTC_SetModeRule						= (pTC_SetModeRule)						 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetModeRule");
	m_pTC_DrawPolygonOnLocalVideo			= (pTC_DrawPolygonOnLocalVideo)		 		SdkApiExport(m_pvAVPlaySdkHdl, "TC_DrawPolygonOnLocalVideo");
	m_pTC_GetMarkInfo						= (pTC_GetMarkInfo)						 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetMarkInfo");
	m_pTC_SetMarkInfo						= (pTC_SetMarkInfo)						 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetMarkInfo");
	m_pTC_SeekMark							= (pTC_SeekMark)					 		SdkApiExport(m_pvAVPlaySdkHdl, "TC_SeekMark");
	m_pTC_StartCaptureFile					= (pTC_StartCaptureFile)				 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_StartCaptureFile");
	m_pTC_StopCaptureFile					= (pTC_StopCaptureFile)					 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_StopCaptureFile");
	m_pTC_SetStreamBufferSize				= (pTC_SetStreamBufferSize)				 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetStreamBufferSize");
	m_pTC_SetFrameListBufferSize			= (pTC_SetFrameListBufferSize)			 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetFrameListBufferSize");
	m_pTC_SetVerticalSync					= (pTC_SetVerticalSync)					 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetVerticalSync");
	m_pTC_StartAudioCapture					= (pTC_StartAudioCapture)				 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_StartAudioCapture");
	m_pTC_StopAudioCapture					= (pTC_StopAudioCapture)				 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_StopAudioCapture");
	m_pTC_RegisterNotifyAudioCapture		= (pTC_RegisterNotifyAudioCapture)		 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_RegisterNotifyAudioCapture");
	m_pTC_SetFishEyeCorrectEnable			= (pTC_SetFishEyeCorrectEnable)			 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetFishEyeCorrectEnable");
	m_pTC_GetFishEyeCorrectEnable			= (pTC_GetFishEyeCorrectEnable)			 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetFishEyeCorrectEnable");
	m_pTC_CreateFishEyeCorrect				= (pTC_CreateFishEyeCorrect)			 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_CreateFishEyeCorrect");
	m_pTC_DeleteFishEyeCorrect				= (pTC_DeleteFishEyeCorrect)			 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_DeleteFishEyeCorrect");
	m_pTC_SetFishEyeCorrectPara				= (pTC_SetFishEyeCorrectPara)			 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetFishEyeCorrectPara");
	m_pTC_GetFishEyeCorrectPara				= (pTC_GetFishEyeCorrectPara)			 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_GetFishEyeCorrectPara");
	m_pTC_SetFishEyeCorrectWnd				= (pTC_SetFishEyeCorrectWnd)		 		SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetFishEyeCorrectWnd");
	m_pTC_SetFishEyeCorrectCallBack			= (pTC_SetFishEyeCorrectCallBack)		 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetFishEyeCorrectCallBack");
	m_pTC_CapturePictureEx					= (pTC_CapturePictureEx)				 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_CapturePictureEx");
	m_pTC_PlayerControl						= (pTC_PlayerControl)					 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_PlayerControl");
	m_pTC_StartFECTrack						= (pTC_StartFECTrack)					 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_StartFECTrack");
	m_pTC_StopFECTrack						= (pTC_StopFECTrack)					 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_StopFECTrack");
	m_pTC_SetAVMode							= (pTC_SetAVMode)						 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetAVMode");
	m_pTC_SetPlayerAVMode					= (pTC_SetPlayerAVMode)					 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetPlayerAVMode");
	m_pTC_SetDecCallBack					= (pTC_SetDecCallBack)					 	SdkApiExport(m_pvAVPlaySdkHdl, "TC_SetDecCallBack");
						
END:
	return iRet;					  
}										  
										  
void CLS_AVPlaySdkApi::FreeSdkLibrary()	  
{										  
	if (NULL != m_pvAVPlaySdkHdl)		  
	{									  
#if defined(_WIN32) || defined(_WIN64)	  
		FreeLibrary((HMODULE)m_pvAVPlaySdkHdl);
#else									  
		dlclose(m_pvAVPlaySdkHdl);
#endif
		m_pvAVPlaySdkHdl = NULL;
	}
}

int CLS_AVPlaySdkApi::CreateSystem(PLAYHWND _hWnd)
{
	CHECK_NULL_RETURN(m_pTC_CreateSystem, RET_FAILED);
	return m_pTC_CreateSystem(_hWnd);	
}

int CLS_AVPlaySdkApi::DeleteSystem()
{
	CHECK_NULL_RETURN(m_pTC_DeleteSystem, RET_FAILED);
	return m_pTC_DeleteSystem();	
}

int	CLS_AVPlaySdkApi::CreatePlayer(int _iType, void* _pvCmdBuf, int _iBufLen, void* _pvUserData)
{
	CHECK_NULL_RETURN(m_pTC_CreatePlayer, RET_FAILED);
	return m_pTC_CreatePlayer(_iType, _pvCmdBuf, _iBufLen, _pvUserData);
}

int CLS_AVPlaySdkApi::CreatePlayerFromFile(PLAYHWND _hWnd, char* _pcFileName, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_CreatePlayerFromFile, RET_FAILED);
	return m_pTC_CreatePlayerFromFile(_hWnd, _pcFileName, _iBufSize,0, NULL, 0, NULL);
}

int CLS_AVPlaySdkApi::CreatePlayerFromStream(PLAYHWND _hWnd, unsigned char* _pucFileHeadBuf, int _iHeadSize)
{
	CHECK_NULL_RETURN(m_pTC_CreatePlayerFromStream, RET_FAILED);
#ifdef __WIN__
	return m_pTC_CreatePlayerFromStream(_hWnd, _pucFileHeadBuf, _iHeadSize);
#else
	return m_pTC_CreatePlayerFromStream(_hWnd, _pucFileHeadBuf, _iHeadSize, 0);
#endif
}

int CLS_AVPlaySdkApi::CreatePlayerFromStreamEx(PLAYHWND _hWnd, unsigned char* _pucFileHeadBuf, int _iHeadSize, int _iStreamBufferSize)
{
	CHECK_NULL_RETURN(m_pTC_CreatePlayerFromStreamEx, RET_FAILED);
	return m_pTC_CreatePlayerFromStreamEx(_hWnd, _pucFileHeadBuf, _iHeadSize, _iStreamBufferSize);
}

int CLS_AVPlaySdkApi::CreatePlayerFromVOD(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize)
{
	CHECK_NULL_RETURN(m_pTC_CreatePlayerFromVoD, RET_FAILED);
	return m_pTC_CreatePlayerFromVoD(_hWnd, _pucVideoHeadBuf, _iHeadSize);
}

int CLS_AVPlaySdkApi::DeletePlayer(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_DeletePlayer, RET_FAILED);
	return m_pTC_DeletePlayer(_iID);
}

int CLS_AVPlaySdkApi::DeletePlayerEx(int _iID, int _iParam)
{
	CHECK_NULL_RETURN(m_pTC_DeletePlayerEx, RET_FAILED);
	return m_pTC_DeletePlayerEx(_iID, _iParam);
}

void CLS_AVPlaySdkApi::RegisterEventMsg(PLAYHWND _hEventWnd, UINT _uiEventMsg)
{
	CHECK_NULL_RETURN_VOID(m_pTC_RegisterEventMsg);
	m_pTC_RegisterEventMsg(_hEventWnd, _uiEventMsg);
}

int CLS_AVPlaySdkApi::RegisterNotifyPlayToEnd(void* _PlayEndFun)
{
	CHECK_NULL_RETURN(m_pTC_RegisterNotifyPlayToEnd, RET_FAILED);
	return m_pTC_RegisterNotifyPlayToEnd(_PlayEndFun);
}

int CLS_AVPlaySdkApi::RegisterNotifyGetDecAV(int _iID, void* _GetDecAVCbk, void* _pvUserData)
{
	CHECK_NULL_RETURN(m_pTC_RegisterNotifyGetDecAV, RET_FAILED);
#ifdef __WIN__
	bool blDisplay = false;
	int iUsrData = (int)_pvUserData;
	blDisplay = 1 == iUsrData ? true :false;
	return m_pTC_RegisterNotifyGetDecAV(_iID, _GetDecAVCbk, blDisplay);
#else
	return m_pTC_RegisterNotifyGetDecAV(_iID, (pfCBGetDecAV)_GetDecAVCbk, _pvUserData);
#endif
}

int CLS_AVPlaySdkApi::SetPlayRect(int _iID, RECT* _pDrawRect)
{
	CHECK_NULL_RETURN(m_pTC_SetPlayRect, RET_FAILED);
	return m_pTC_SetPlayRect(_iID, _pDrawRect);
}

int CLS_AVPlaySdkApi::SetPlayRectEx(int _iID, RECT* _pDrawRect, DWORD _dwMask)
{
	CHECK_NULL_RETURN(m_pTC_SetPlayRectEx, RET_FAILED);
	return m_pTC_SetPlayRectEx(_iID, _pDrawRect, _dwMask);
}

int CLS_AVPlaySdkApi::Play(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_Play, RET_FAILED);
	return m_pTC_Play(_iID);
}

int CLS_AVPlaySdkApi::Stop(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_Stop, RET_FAILED);
	return m_pTC_Stop(_iID);
}

int CLS_AVPlaySdkApi::GoBegin(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GoBegin, RET_FAILED);
	return m_pTC_GoBegin(_iID);
}

int CLS_AVPlaySdkApi::GoEnd(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GoEnd, RET_FAILED);
	return m_pTC_GoEnd(_iID);
}

int CLS_AVPlaySdkApi::StepForward(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_StepForward, RET_FAILED);
	return m_pTC_StepForward(_iID);
}

int CLS_AVPlaySdkApi::StepBackward(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_StepBackward, RET_FAILED);
	return m_pTC_StepBackward(_iID);
}

int CLS_AVPlaySdkApi::FastForward(int _iID, int _iSpeed)
{
	CHECK_NULL_RETURN(m_pTC_FastForward, RET_FAILED);
	return m_pTC_FastForward(_iID, _iSpeed);
}

int CLS_AVPlaySdkApi::SlowForward(int _iID, int _iSpeed)
{
	CHECK_NULL_RETURN(m_pTC_SlowForward, RET_FAILED);
	return m_pTC_SlowForward(_iID, _iSpeed);
}

int CLS_AVPlaySdkApi::FastBackward(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_FastBackward, RET_FAILED);
	return m_pTC_FastBackward(_iID);
}

int CLS_AVPlaySdkApi::PlayAudio(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_PlayAudio, RET_FAILED);
	return m_pTC_PlayAudio(_iID);
}

int CLS_AVPlaySdkApi::StopAudio(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_StopAudio, RET_FAILED);
	return m_pTC_StopAudio(_iID);
}

int CLS_AVPlaySdkApi::SetAudioVolume(unsigned short _ustVolume)
{
	CHECK_NULL_RETURN(m_pTC_SetAudioVolume, RET_FAILED);
	return m_pTC_SetAudioVolume(_ustVolume);
}

int CLS_AVPlaySdkApi::SetAudioVolumeEx(int _iID, unsigned short _ustVolume)
{
	CHECK_NULL_RETURN(m_pTC_SetAudioVolumeEx, RET_FAILED);
	return m_pTC_SetAudioVolumeEx(_iID, _ustVolume);
}

int CLS_AVPlaySdkApi::GetAudioVolumeEx(int _iID, unsigned short &_ustVolume)
{
	CHECK_NULL_RETURN(m_pTC_GetAudioVolumeEx, RET_FAILED);
	return m_pTC_GetAudioVolumeEx(_iID, _ustVolume);
}

int CLS_AVPlaySdkApi::Seek(int _iID, int _iFrameNo)
{
	CHECK_NULL_RETURN(m_pTC_Seek, RET_FAILED);
	return m_pTC_Seek(_iID, _iFrameNo);
}
int CLS_AVPlaySdkApi::SeekEx(int _iID, int _iFrameNo)
{
	CHECK_NULL_RETURN(m_pTC_SeekEx, RET_FAILED);
	return m_pTC_SeekEx(_iID, _iFrameNo);
}

int CLS_AVPlaySdkApi::GetFrameRate(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GetFrameRate, RET_FAILED);
	return m_pTC_GetFrameRate(_iID);
}

int CLS_AVPlaySdkApi::GetPlayingFrameNum(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GetPlayingFrameNum, RET_FAILED);
	return m_pTC_GetPlayingFrameNum(_iID);
}

int CLS_AVPlaySdkApi::GetFrameCount(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GetFrameCount, RET_FAILED);
	return m_pTC_GetFrameCount(_iID);
}

int CLS_AVPlaySdkApi::GetBeginEnd(int _iID, int* _piBegin, int* _piEnd)
{
	CHECK_NULL_RETURN(m_pTC_GetBeginEnd, RET_FAILED);
	return m_pTC_GetBeginEnd(_iID, _piBegin, _piEnd);
}

int CLS_AVPlaySdkApi::GetFileName(int _iID, char* _pcFileName)
{
	CHECK_NULL_RETURN(m_pTC_GetFileName, RET_FAILED);
	return m_pTC_GetFileName(_iID, _pcFileName);
}

int CLS_AVPlaySdkApi::CapturePic(int _iID, unsigned char** _ppucYUV)
{
	CHECK_NULL_RETURN(m_pTC_CapturePic, RET_FAILED);
	return m_pTC_CapturePic(_iID, _ppucYUV);
}

int CLS_AVPlaySdkApi::CaptureBmpPic(int _iID, char* _pcSaveFile)
{
	CHECK_NULL_RETURN(m_pTC_CaptureBmpPic, RET_FAILED);
	return m_pTC_CaptureBmpPic(_iID, _pcSaveFile);
}

int CLS_AVPlaySdkApi::CaptureJpegPic(int _iID, char* _pcSaveFile)
{
	CHECK_NULL_RETURN(m_pTC_CaptureJpegPic, RET_FAILED);
	return m_pTC_CaptureJpegPic(_iID, _pcSaveFile);
}

int CLS_AVPlaySdkApi::CapturePicture(int _iID, int _iType, char* _pcSaveFile)
{
	CHECK_NULL_RETURN(m_pTC_CapturePicture, RET_FAILED);
	return m_pTC_CapturePicture(_iID, _iType, _pcSaveFile);
}

int CLS_AVPlaySdkApi::CapturePicData(int _iID, int _iPicType, char* _pPicBuf, int* _iPicSize)
{
	CHECK_NULL_RETURN(m_pTC_CapturePicData, RET_FAILED);
	return m_pTC_CapturePicData(_iID, _iPicType, _pPicBuf, _iPicSize);
}

int CLS_AVPlaySdkApi::PutStreamToPlayer(int _iID, unsigned char* _pucStreamData, int _iSize)
{
	CHECK_NULL_RETURN(m_pTC_PutStreamToPlayer, RET_FAILED);
	return m_pTC_PutStreamToPlayer(_iID, _pucStreamData, _iSize);
}

int CLS_AVPlaySdkApi::StartMonitorCPU()
{
	CHECK_NULL_RETURN(m_pTC_StartMonitorCPU, RET_FAILED);
	return m_pTC_StartMonitorCPU();
}

int CLS_AVPlaySdkApi::StopMonitorCPU()
{
	CHECK_NULL_RETURN(m_pTC_StopMonitorCPU, RET_FAILED);
	return m_pTC_StopMonitorCPU();
}

int CLS_AVPlaySdkApi::GetVersion(SDK_VERSION *_ver)
{
	CHECK_NULL_RETURN(m_pTC_GetVersion, RET_FAILED);
	return m_pTC_GetVersion(_ver);
}

int CLS_AVPlaySdkApi::SetVoDPlayerOver(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_SetVoDPlayerOver, RET_FAILED);
	return m_pTC_SetVoDPlayerOver(_iID);
}

int CLS_AVPlaySdkApi::SetCleanVoDBuffer(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_SetCleanVoDBuffer, RET_FAILED);
	return m_pTC_SetCleanVoDBuffer(_iID);
}

int CLS_AVPlaySdkApi::GetPlayTime(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GetPlayTime, RET_FAILED);
	return m_pTC_GetPlayTime(_iID);
}

int CLS_AVPlaySdkApi::RegisterDrawFun(int _iID, pfCBDrawFun _pfDrawFun, void* _pvUserData)
{
	CHECK_NULL_RETURN(m_pTC_RegisterDrawFun, RET_FAILED);
	return m_pTC_RegisterDrawFun(_iID, _pfDrawFun, _pvUserData);
}

int CLS_AVPlaySdkApi::Pause(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_Pause, RET_FAILED);
	return m_pTC_Pause(_iID);
}

int CLS_AVPlaySdkApi::SetEZoom(int _iID, int _iZoomID, RECT _pRectInVideo)
{
	CHECK_NULL_RETURN(m_pTC_SetEZoom, RET_FAILED);
	return m_pTC_SetEZoom(_iID, _iZoomID, _pRectInVideo);
}

int CLS_AVPlaySdkApi::ResetEZoom(int _iID , int _iZoomID)
{
	CHECK_NULL_RETURN(m_pTC_ResetEZoom, RET_FAILED);
	return m_pTC_ResetEZoom(_iID, _iZoomID);
}

int CLS_AVPlaySdkApi::AddEZoom(int _iID, PLAYHWND _hWnd, RECT *_rcZoom, int _iCount)
{
	CHECK_NULL_RETURN(m_pTC_AddEZoom, RET_FAILED);
	return m_pTC_AddEZoom(_iID, _hWnd, _rcZoom, _iCount);
}

int CLS_AVPlaySdkApi::RemoveEZoom(int _iID , int _iZoomID)
{
	CHECK_NULL_RETURN(m_pTC_RemoveEZoom, RET_FAILED);
	return m_pTC_RemoveEZoom(_iID, _iZoomID);
}

int CLS_AVPlaySdkApi::ResetPlayWnd(int _iID, PLAYHWND _hWnd)
{
	CHECK_NULL_RETURN(m_pTC_ResetPlayWnd, RET_FAILED);
	return m_pTC_ResetPlayWnd(_iID, _hWnd);
}

int CLS_AVPlaySdkApi::GetVideoParam(int _iID, int* _pWidth, int* _pHeight, int* _pFrameRate)
{
	CHECK_NULL_RETURN(m_pTC_GetVideoParam, RET_FAILED);
	return m_pTC_GetVideoParam(_iID, _pWidth, _pHeight, _pFrameRate);
}

int CLS_AVPlaySdkApi::DrawRect(int _iID, RECT *_rcDraw, int _iCount)
{
	CHECK_NULL_RETURN(m_pTC_DrawRect, RET_FAILED);
	return m_pTC_DrawRect(_iID, _rcDraw, _iCount);
}

int CLS_AVPlaySdkApi::SearchVCAInfo( int _iID, int _iSearchRule, int _iFrameStart, void* _pParamBuf, int _iBufSize, int _iContinue, unsigned int &_iFrame)
{
	CHECK_NULL_RETURN(m_pTC_SearchVCAInfo, RET_FAILED);
	return m_pTC_SearchVCAInfo(_iID, _iSearchRule, _iFrameStart, _pParamBuf, _iBufSize, _iContinue, &_iFrame);
}

int CLS_AVPlaySdkApi::GetBeginEndTimeStamp(int _iID, unsigned int *_puiBegin, unsigned int * _puiEnd)
{
	CHECK_NULL_RETURN(m_pTC_GetBeginEndTimeStamp, RET_FAILED);
	return m_pTC_GetBeginEndTimeStamp(_iID, _puiBegin, _puiEnd);
}

int CLS_AVPlaySdkApi::SetVideoDecryptKey(int _iID, char* _lpBuf, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_SetVideoDecryptKey, RET_FAILED);
	return m_pTC_SetVideoDecryptKey(_iID, _lpBuf, _iBufSize);
}

int CLS_AVPlaySdkApi::GetVideoDecryptKey(int _iID, char* _lpBuf, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_GetVideoDecryptKey, RET_FAILED);
	return m_pTC_GetVideoDecryptKey(_iID, _lpBuf, _iBufSize);
}

int CLS_AVPlaySdkApi::RegisterCommonEventCallBack(int _iID, pfCBCommonEventNotify _pf, void* _pUserData)
{
	CHECK_NULL_RETURN(m_pTC_RegisterCommonEventCallBack, RET_FAILED);
#ifdef __WIN__
	return m_pTC_RegisterCommonEventCallBack((pfCBCommonEventNotifyEx)_pf);
#else
	return m_pTC_RegisterCommonEventCallBack(_iID, _pf, _pUserData);
#endif
}

int CLS_AVPlaySdkApi::RegisterCommonEventCallBack_V4(int _iID, pfCBCommonEventNotifyEx _pf, void* _pvUserData)
{
	CHECK_NULL_RETURN(m_pTC_RegisterCommonEventCallBack_V4, RET_FAILED);
	return m_pTC_RegisterCommonEventCallBack_V4(_iID, _pf, _pvUserData);
}

int CLS_AVPlaySdkApi::SetModeRule(int _iID, int _iModeRule)
{
	CHECK_NULL_RETURN(m_pTC_SetModeRule, RET_FAILED);
	return m_pTC_SetModeRule(_iID, _iModeRule);
}

int CLS_AVPlaySdkApi::DrawPolygonOnLocalVideo(int _iID, POINT* _pPointArray, int _iPointCount, int _iEnableArrow)
{
	CHECK_NULL_RETURN(m_pTC_DrawPolygonOnLocalVideo, RET_FAILED);
	return m_pTC_DrawPolygonOnLocalVideo(_iID, _pPointArray, _iPointCount, _iEnableArrow);
}

int CLS_AVPlaySdkApi::GetMarkInfo(int _iID, PVOID _pData,int _iSize)
{
	CHECK_NULL_RETURN(m_pTC_GetMarkInfo, RET_FAILED);
	return m_pTC_GetMarkInfo(_iID,_pData,_iSize);
}

int CLS_AVPlaySdkApi::SetMarkInfo(int _iID,int _iFrameNO,int _iType)
{
	CHECK_NULL_RETURN(m_pTC_SetMarkInfo, RET_FAILED);
	return m_pTC_SetMarkInfo(_iID,_iFrameNO,_iType);
}

int CLS_AVPlaySdkApi::SeekMark(int _iID,int _iFrameNO)
{
	CHECK_NULL_RETURN(m_pTC_SeekMark, RET_FAILED);
	return m_pTC_SeekMark(_iID,_iFrameNO);
}

int CLS_AVPlaySdkApi::StartCaptureFile(int _iID, char* _cFileName,int _iRecFileType)
{
	CHECK_NULL_RETURN(m_pTC_StartCaptureFile, RET_FAILED);
	return m_pTC_StartCaptureFile(_iID,_cFileName, _iRecFileType);
}

int CLS_AVPlaySdkApi::StopCaptureFile(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_StopCaptureFile, RET_FAILED);
	return m_pTC_StopCaptureFile(_iID);
}
int CLS_AVPlaySdkApi::SetStreamBufferSize(int _iID, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_SetStreamBufferSize, RET_FAILED);
	return m_pTC_SetStreamBufferSize(_iID,_iBufSize);
}
int CLS_AVPlaySdkApi::SetFrameListBufferSize(int _iID, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_SetFrameListBufferSize, RET_FAILED);
	return m_pTC_SetFrameListBufferSize(_iID, _iBufSize, -1);
}

int CLS_AVPlaySdkApi::SetVerticalSync(int _iID,int _iFlag)
{
	CHECK_NULL_RETURN(m_pTC_SetVerticalSync, RET_FAILED);
	return m_pTC_SetVerticalSync(_iID,_iFlag);
}

int CLS_AVPlaySdkApi::StartAudioCapture(int _iID, int _iWavein,int _iWaveout)
{
	CHECK_NULL_RETURN(m_pTC_StartAudioCapture, RET_FAILED);
	return m_pTC_StartAudioCapture(_iID, _iWavein, _iWaveout);
}

int CLS_AVPlaySdkApi::StopAudioCapture(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_StopAudioCapture, RET_FAILED);
	return m_pTC_StopAudioCapture(_iID);
}

int CLS_AVPlaySdkApi::RegisterNotifyAudioCapture(void* _pvAudioCaptureFun, void* _pvContext)
{
	CHECK_NULL_RETURN(m_pTC_RegisterNotifyAudioCapture, RET_FAILED);
	return m_pTC_RegisterNotifyAudioCapture(_pvAudioCaptureFun, _pvContext);
}

int CLS_AVPlaySdkApi::SetFishEyeCorrectEnable(int _iID, int _iEnbale, CreateCorrectPara* _ptPara)
{
	CHECK_NULL_RETURN(m_pTC_SetFishEyeCorrectEnable, RET_FAILED);
	return m_pTC_SetFishEyeCorrectEnable(_iID, _iEnbale, _ptPara);
}

int CLS_AVPlaySdkApi::GetFishEyeCorrectEnable(int _iID, int* _piEnbale)
{
	CHECK_NULL_RETURN(m_pTC_GetFishEyeCorrectEnable, RET_FAILED);
	return m_pTC_GetFishEyeCorrectEnable(_iID, _piEnbale);
}

int CLS_AVPlaySdkApi::CreateFishEyeCorrect(int _iID, CreateCorrectPara* _ptPara, int _iParaSize)
{
	CHECK_NULL_RETURN(m_pTC_CreateFishEyeCorrect, RET_FAILED);
	return m_pTC_CreateFishEyeCorrect(_iID, _ptPara, _iParaSize);
}

int CLS_AVPlaySdkApi::DeleteFishEyeCorrect(int _iID, int _iSubID)
{
	CHECK_NULL_RETURN(m_pTC_DeleteFishEyeCorrect, RET_FAILED);
	return m_pTC_DeleteFishEyeCorrect(_iID, _iSubID);
}

int CLS_AVPlaySdkApi::SetFishEyeCorrectPara(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_SetFishEyeCorrectPara, RET_FAILED);
	return m_pTC_SetFishEyeCorrectPara(_iID, _iSubID, _iCmd, _pvPara, _iBufSize);
}

int CLS_AVPlaySdkApi::GetFishEyeCorrectPara(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_GetFishEyeCorrectPara, RET_FAILED);
	return m_pTC_GetFishEyeCorrectPara(_iID, _iSubID, _iCmd, _pvPara, _iBufSize);
}

int CLS_AVPlaySdkApi::SetFishEyeCorrectWnd(int _iID, int _iSubID, PLAYHWND _hWnd)
{
	CHECK_NULL_RETURN(m_pTC_SetFishEyeCorrectWnd, RET_FAILED);
	return m_pTC_SetFishEyeCorrectWnd(_iID, _iSubID, _hWnd);
}

int CLS_AVPlaySdkApi::SetFishEyeCorrectCallBack(int _iID, int _iSubID, FISHEYE_CallBack _cbFunc, void* _pvUsrData)
{
	CHECK_NULL_RETURN(m_pTC_SetFishEyeCorrectCallBack, RET_FAILED);
	return m_pTC_SetFishEyeCorrectCallBack(_iID, _iSubID, _cbFunc, _pvUsrData);
}

int CLS_AVPlaySdkApi::CapturePictureEx(int _iID, int _iType, int _iSubId, char* _pcSaveFile)
{
	CHECK_NULL_RETURN(m_pTC_CapturePictureEx, RET_FAILED);
	return m_pTC_CapturePictureEx(_iID, _iType, _iSubId, _pcSaveFile);
}

int CLS_AVPlaySdkApi::PlayBackControl( int _iID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUserData )
{
	CHECK_NULL_RETURN(m_pTC_PlayerControl, RET_FAILED);
	return m_pTC_PlayerControl(_iID, _iCmd, _pvCmdBuf, _iBufLen, _pvUserData);	
}

int CLS_AVPlaySdkApi::StartFECTrack(int _iID, FECTrackInfo* _ptTrackInfo, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_StartFECTrack, RET_FAILED);
	return m_pTC_StartFECTrack(_iID, _ptTrackInfo, _iBufSize);	
}

int CLS_AVPlaySdkApi::StopFECTrack(int _iID, int _iSubID)
{
	CHECK_NULL_RETURN(m_pTC_StopFECTrack, RET_FAILED);
	return m_pTC_StopFECTrack(_iID, _iSubID);	
}

int CLS_AVPlaySdkApi::SetAVMode( int _iCmd, void* _pvBuf, int _iBufSize )
{
	CHECK_NULL_RETURN(m_pTC_SetAVMode, RET_FAILED);
	return m_pTC_SetAVMode(_iCmd, _pvBuf, _iBufSize);	
}

int CLS_AVPlaySdkApi::SetPlayerAVMode(int _iID, int _iCmd, void* _pvBuf, int _iBufSize)
{
	CHECK_NULL_RETURN(m_pTC_SetPlayerAVMode, RET_FAILED);
	return m_pTC_SetPlayerAVMode(_iID, _iCmd, _pvBuf, _iBufSize);
}

int CLS_AVPlaySdkApi::GetCurFrameNo(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GetCurFrameNo, RET_FAILED);
	return m_pTC_GetCurFrameNo(_iID);
}

int CLS_AVPlaySdkApi::GetFrameListCount(int _iID)
{
	CHECK_NULL_RETURN(m_pTC_GetFrameListCount, RET_FAILED);
	return m_pTC_GetFrameListCount(_iID);
}

int CLS_AVPlaySdkApi::SetPlayDelay(int _iID, int _iDelayNum)
{
	CHECK_NULL_RETURN(m_pTC_SetPlayDelay, RET_FAILED);
	return m_pTC_SetPlayDelay(_iID, _iDelayNum);
}

int CLS_AVPlaySdkApi::SetDecCallBack(int _iID, DECYUV_NOTIFY_V4 _cbkGetYUV, void* _pvUserData)
{
	CHECK_NULL_RETURN(m_pTC_SetDecCallBack, RET_FAILED);
	return m_pTC_SetDecCallBack(_iID, _cbkGetYUV, _pvUserData);
}

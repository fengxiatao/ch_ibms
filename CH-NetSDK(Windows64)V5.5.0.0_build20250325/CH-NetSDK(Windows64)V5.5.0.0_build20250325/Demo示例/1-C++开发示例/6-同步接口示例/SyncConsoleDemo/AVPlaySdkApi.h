#ifndef _PLAYSDK_API_H_
#define _PLAYSDK_API_H_

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <iomanip>
using namespace std; 

#if defined(_WIN32) || defined(_WIN64)
#include <windows.h>
#include <windows.h>
#include <process.h >
#else
#include <sys/times.h>
#include <sys/time.h>
#include <stdint.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h> 
#endif

#include "RetValue.h"
#include "AVPlaySdkTypes.h"

typedef int (*pTC_CreateSystem)(PLAYHWND _hWnd);
typedef int (*pTC_DeleteSystem)();
typedef int (*pTC_CreatePlayer)(int _iType, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
typedef int (*pTC_CreatePlayerFromFile)(PLAYHWND  _hWnd, char* _pcFileName, int _iDownloadBufSz, int _iFileTrueSz, int* _piNowSz, int _iLastFrmNo, int* _piCompleteFlag);
#ifdef __WIN__
typedef int (*pTC_CreatePlayerFromStream)(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize);
#else
typedef int (*pTC_CreatePlayerFromStream)(PLAYHWND _iWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize, int _iStreamBufferSize);
#endif
typedef int (*pTC_CreatePlayerFromStreamEx)(PLAYHWND _hWnd, unsigned char* _pucFileHeadBuf, int _iHeadSize, int _iStreamBufferSize);
typedef int (*pTC_CreatePlayerFromVoD)(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize);
typedef int (*pTC_DeletePlayer)(int _iID);
typedef int (*pTC_DeletePlayerEx)(int _iID, int _iParam);
typedef void(*pTC_RegisterEventMsg)(PLAYHWND _hEventWnd, UINT _uiEventMsg);
typedef int (*pTC_RegisterNotifyPlayToEnd)(void* _PlayEndFun);
typedef int (*pTC_SetDecCallBack)(int _iID, DECYUV_NOTIFY_V4 _cbkGetYUV, void* _pvUserData);
#ifdef __WIN__
typedef int (*pTC_RegisterNotifyGetDecAV)(int _iID, void* _GetDecAVCbk, bool _blDisplay);
typedef int (*pTC_RegisterCommonEventCallBack)(pfCBCommonEventNotifyEx _pf);
#else
typedef int (*pTC_RegisterNotifyGetDecAV)(int _iID, pfCBGetDecAV _GetDecAVCbk, void* _pvUserData);
typedef int (*pTC_RegisterCommonEventCallBack)(int _iID, pfCBCommonEventNotify _pf, void* _pUserData);
#endif
typedef int (*pTC_RegisterCommonEventCallBackEx)(pfCBCommonEventNotifyEx _pf, void* _pvUserData);
typedef int (*pTC_RegisterCommonEventCallBack_V4)(int _iID, pfCBCommonEventNotifyEx _pf, void* _pvUserData);
typedef int (*pTC_RegisterDrawFun)(int _iID, pfCBDrawFun _pfDrawFun, void* _pvUserData);
typedef int (*pTC_SetPlayRect)(int _iID, RECT* _pDrawRect);
typedef int (*pTC_SetPlayRectEx)(int _iID, RECT* _pDrawRect, DWORD _dwMask);
typedef int (*pTC_Play)(int _iID);
typedef int (*pTC_Stop)(int _iID);
typedef int (*pTC_GoBegin)(int _iID);
typedef int (*pTC_GoEnd)(int _iID);
typedef int (*pTC_StepForward)(int _iID);
typedef int (*pTC_StepBackward)(int _iID);
typedef int (*pTC_FastForward)(int _iID, int _iSpeed);
typedef int (*pTC_SlowForward)(int _iID, int _iSpeed);
typedef int (*pTC_FastBackward)(int _iID);
typedef int (*pTC_PlayAudio)(int _iID);
typedef int (*pTC_StopAudio)(int _iID);
typedef int (*pTC_SetAudioVolume)(unsigned short _ustVolume);
typedef int (*pTC_SetAudioVolumeEx)(int _iID, unsigned short _ustVolume);
typedef int (*pTC_GetAudioVolumeEx)(int _iID, unsigned short &_ustVolume);
typedef int (*pTC_Seek)(int _iID, int _iFrameNo);
typedef int (*pTC_SeekEx)(int _iID, int _iFrameNo);
typedef int (*pTC_GetFrameRate)(int _iID);
typedef int (*pTC_GetPlayingFrameNum)(int _iID);
typedef int (*pTC_GetFrameCount)(int _iID);
typedef int (*pTC_GetBeginEnd)(int _iID, int* _piBegin, int* _piEnd);
typedef int (*pTC_GetFileName)(int _iID, char* _pcFileName);
typedef int (*pTC_CapturePic)(int _iID, unsigned char** _ppucYUV);
typedef int (*pTC_CaptureBmpPic)(int _iID, char* _pcSaveFile);
typedef int (*pTC_CaptureJpegPic)(int _iID, char* _pcSaveFile);
typedef int (*pTC_CapturePicture)(int _iID, int _iType, char* _pcSaveFile);
typedef int (*pTC_CapturePicData)(int _iID, int _iPicType, char* _pPicBuf, int* _iPicSize);
typedef int (*pTC_PutStreamToPlayer)(int _iID, unsigned char* _pucStreamData, int _iSize);
typedef int (*pTC_StartMonitorCPU)();
typedef int (*pTC_StopMonitorCPU)();
typedef int (*pTC_GetVersion)(SDK_VERSION *_ver);
typedef int (*pTC_SetVoDPlayerOver)(int _iID);
typedef int (*pTC_SetCleanVoDBuffer)(int _iID);
typedef int (*pTC_GetPlayTime)(int _iID);
typedef int (*pTC_Pause)(int _iID);
typedef int (*pTC_GetVideoParam)(int _iID, int*, int*, int*);
typedef int (*pTC_DrawRect)(int _iID, RECT *_rcDraw, int _iCount);
typedef int (*pTC_SetEZoom)(int _iID, int _iZoomID, RECT _pRectInVideo);
typedef int (*pTC_ResetEZoom)(int _iID, int _iZoomID);
typedef int (*pTC_AddEZoom)(int _iID, PLAYHWND _hWnd, RECT* _rcZoom, int _iCount);
typedef int (*pTC_RemoveEZoom)(int _iID, int _iZoomID);
typedef int (*pTC_ResetPlayWnd)(int _iID, PLAYHWND _hWnd);
typedef int (*pTC_SearchVCAInfo)( int _iID, int _iSearchRule, int _iFrameStart, void* _pParamBuf, int _iBufSize,  int _iContinuePlay, unsigned int* _puiFrameResult );
typedef int (*pTC_GetBeginEndTimeStamp)(int _iID, unsigned int*, unsigned int*);
typedef int (*pTC_SetVideoDecryptKey)(int _iID, void* _lpBuf, int _iBufSize);
typedef int (*pTC_GetVideoDecryptKey)(int _iID, void* _lpBuf, int _iBufSize);
typedef int (*pTC_SetModeRule)(int _iID, int _iModeRule);
typedef int (*pTC_DrawPolygonOnLocalVideo)(int _iID, POINT* _pPointArray, int _iPointCount, int _iEnableArrow);
typedef int (*pTC_GetMarkInfo)(int _iID, PVOID _pData,int _iSize);
typedef int (*pTC_SetMarkInfo)(int _iID,int _iFrameNO,int _iType);
typedef int (*pTC_SeekMark)(int _iID,int _iFrameNO);
typedef int (*pTC_StartCaptureFile)(int _iID, char* _cFileName,int _iRecFileType);
typedef int (*pTC_StopCaptureFile)(int _iID);
typedef int (*pTC_SetStreamBufferSize)(int _iID, int _iBufSize);
typedef int (*pTC_SetFrameListBufferSize)(int _iID, int _iBufSize, int _iMaxFrameCount/* = -1*/);
typedef int (*pTC_SetVerticalSync)(int _iID,int _iFlag);
typedef int (*pTC_StartAudioCapture)(int _iID, int _iWavein,int _iWaveout);
typedef int (*pTC_StopAudioCapture)(int _iID);
typedef int (*pTC_RegisterNotifyAudioCapture)(void* _pvAudioCaptureFun, void* _pvContext);
typedef int (*pTC_SetFishEyeCorrectEnable)(int _iID, int _iEnbale, CreateCorrectPara* _ptPara);
typedef int (*pTC_GetFishEyeCorrectEnable)(int _iID, int* _piEnbale);
typedef int (*pTC_CreateFishEyeCorrect)(int _iID, CreateCorrectPara* _ptPara, int _iParaSize);
typedef int (*pTC_DeleteFishEyeCorrect)(int _iID, int _iSubID);
typedef int (*pTC_SetFishEyeCorrectPara)(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize);
typedef int (*pTC_GetFishEyeCorrectPara)(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize);
typedef int (*pTC_SetFishEyeCorrectWnd)(int _iID, int _iSubID, PLAYHWND _hWnd);
typedef int (*pTC_SetFishEyeCorrectCallBack)(int _iID, int _iSubID, FISHEYE_CallBack _cbFunc, void* _pvUsrData);
typedef int (*pTC_CapturePictureEx)(int _iID, int _iType, int _iSubId, char* _pcSaveFile);
typedef int (*pTC_PlayerControl)(int _iID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUserData );
typedef int (*pTC_StartFECTrack)(int _iID, FECTrackInfo* _ptTrackInfo, int _iBufSize);
typedef int (*pTC_StopFECTrack)(int _iID, int _iSubID);
typedef int (*pTC_SetAVMode)(int _iCmd, void* _pvBuf, int _iBufSize);
typedef int (*pTC_SetPlayerAVMode)(int _iID, int _iCmd, void* _pvBuf, int _iBufSize);
typedef int (*pTC_GetCurFrameNo)(int _iID);
typedef int (*pTC_GetFrameListCount)(int _iID);
typedef int (*pTC_SetPlayDelay)(int _iID, int _iDelayNum);


class CLS_AVPlaySdkApi  
{
private:
	static CLS_AVPlaySdkApi* m_pclsInstance;
	void*	m_pvAVPlaySdkHdl;

	pTC_CreateSystem						m_pTC_CreateSystem;
	pTC_DeleteSystem						m_pTC_DeleteSystem;
	pTC_CreatePlayer						m_pTC_CreatePlayer;
	pTC_CreatePlayerFromFile				m_pTC_CreatePlayerFromFile;
	pTC_CreatePlayerFromStream				m_pTC_CreatePlayerFromStream;
	pTC_CreatePlayerFromStreamEx			m_pTC_CreatePlayerFromStreamEx;
	pTC_CreatePlayerFromVoD					m_pTC_CreatePlayerFromVoD;
	pTC_DeletePlayer						m_pTC_DeletePlayer;
	pTC_DeletePlayerEx						m_pTC_DeletePlayerEx;
	pTC_RegisterEventMsg					m_pTC_RegisterEventMsg;
	pTC_RegisterNotifyPlayToEnd				m_pTC_RegisterNotifyPlayToEnd;
	pTC_RegisterNotifyGetDecAV				m_pTC_RegisterNotifyGetDecAV;
	pTC_RegisterDrawFun						m_pTC_RegisterDrawFun;
	pTC_SetPlayRect							m_pTC_SetPlayRect;
	pTC_SetPlayRectEx						m_pTC_SetPlayRectEx;
	pTC_Play								m_pTC_Play;
	pTC_Stop								m_pTC_Stop;
	pTC_GoBegin								m_pTC_GoBegin;
	pTC_GoEnd								m_pTC_GoEnd;
	pTC_StepForward							m_pTC_StepForward;
	pTC_StepBackward						m_pTC_StepBackward;
	pTC_FastForward							m_pTC_FastForward;
	pTC_SlowForward							m_pTC_SlowForward;
	pTC_FastBackward						m_pTC_FastBackward;
	pTC_PlayAudio							m_pTC_PlayAudio;
	pTC_StopAudio							m_pTC_StopAudio;
	pTC_SetAudioVolume						m_pTC_SetAudioVolume;
	pTC_SetAudioVolumeEx					m_pTC_SetAudioVolumeEx;
	pTC_GetAudioVolumeEx					m_pTC_GetAudioVolumeEx;
	pTC_Seek								m_pTC_Seek;
	pTC_SeekEx								m_pTC_SeekEx;
	pTC_GetFrameRate						m_pTC_GetFrameRate;
	pTC_GetPlayingFrameNum					m_pTC_GetPlayingFrameNum;
	pTC_GetFrameCount						m_pTC_GetFrameCount;
	pTC_GetBeginEnd							m_pTC_GetBeginEnd;
	pTC_GetFileName							m_pTC_GetFileName;
	pTC_CapturePic							m_pTC_CapturePic;
	pTC_CaptureBmpPic						m_pTC_CaptureBmpPic;
	pTC_CaptureJpegPic						m_pTC_CaptureJpegPic;
	pTC_CapturePicture						m_pTC_CapturePicture;
	pTC_CapturePicData						m_pTC_CapturePicData;
	pTC_PutStreamToPlayer					m_pTC_PutStreamToPlayer;
	pTC_StartMonitorCPU						m_pTC_StartMonitorCPU;
	pTC_StopMonitorCPU						m_pTC_StopMonitorCPU;
	pTC_GetVersion							m_pTC_GetVersion;
	pTC_SetVoDPlayerOver					m_pTC_SetVoDPlayerOver;
	pTC_SetCleanVoDBuffer					m_pTC_SetCleanVoDBuffer;
	pTC_GetPlayTime							m_pTC_GetPlayTime;
	pTC_Pause								m_pTC_Pause;
	pTC_GetVideoParam						m_pTC_GetVideoParam;
	pTC_DrawRect							m_pTC_DrawRect;
	pTC_ResetPlayWnd						m_pTC_ResetPlayWnd;
	pTC_AddEZoom							m_pTC_AddEZoom;
	pTC_SetEZoom							m_pTC_SetEZoom;
	pTC_RemoveEZoom							m_pTC_RemoveEZoom;
	pTC_ResetEZoom							m_pTC_ResetEZoom;
	pTC_SearchVCAInfo						m_pTC_SearchVCAInfo;
	pTC_GetBeginEndTimeStamp				m_pTC_GetBeginEndTimeStamp;
	pTC_SetVideoDecryptKey					m_pTC_SetVideoDecryptKey;
	pTC_GetVideoDecryptKey					m_pTC_GetVideoDecryptKey;	
	pTC_RegisterCommonEventCallBack			m_pTC_RegisterCommonEventCallBack;
	pTC_RegisterCommonEventCallBackEx		m_pTC_RegisterCommonEventCallBackEx;
	pTC_RegisterCommonEventCallBack_V4		m_pTC_RegisterCommonEventCallBack_V4;
	pTC_SetModeRule							m_pTC_SetModeRule;
	pTC_DrawPolygonOnLocalVideo				m_pTC_DrawPolygonOnLocalVideo;
	pTC_GetMarkInfo							m_pTC_GetMarkInfo;
	pTC_SetMarkInfo							m_pTC_SetMarkInfo;
	pTC_SeekMark							m_pTC_SeekMark;
	pTC_StartCaptureFile					m_pTC_StartCaptureFile;
	pTC_StopCaptureFile						m_pTC_StopCaptureFile;
	pTC_SetStreamBufferSize					m_pTC_SetStreamBufferSize;
	pTC_SetFrameListBufferSize				m_pTC_SetFrameListBufferSize;
	pTC_SetVerticalSync						m_pTC_SetVerticalSync;
	pTC_StartAudioCapture					m_pTC_StartAudioCapture;
	pTC_StopAudioCapture					m_pTC_StopAudioCapture;
	pTC_RegisterNotifyAudioCapture			m_pTC_RegisterNotifyAudioCapture;
	pTC_SetFishEyeCorrectEnable				m_pTC_SetFishEyeCorrectEnable;
	pTC_GetFishEyeCorrectEnable				m_pTC_GetFishEyeCorrectEnable;
	pTC_CreateFishEyeCorrect				m_pTC_CreateFishEyeCorrect;
	pTC_DeleteFishEyeCorrect				m_pTC_DeleteFishEyeCorrect;
	pTC_SetFishEyeCorrectPara				m_pTC_SetFishEyeCorrectPara;
	pTC_GetFishEyeCorrectPara				m_pTC_GetFishEyeCorrectPara;
	pTC_SetFishEyeCorrectWnd				m_pTC_SetFishEyeCorrectWnd;
	pTC_SetFishEyeCorrectCallBack			m_pTC_SetFishEyeCorrectCallBack;
	pTC_CapturePictureEx					m_pTC_CapturePictureEx;
	pTC_PlayerControl						m_pTC_PlayerControl;
	pTC_StartFECTrack						m_pTC_StartFECTrack;
	pTC_StopFECTrack						m_pTC_StopFECTrack;
	pTC_SetAVMode							m_pTC_SetAVMode;
	pTC_SetPlayerAVMode						m_pTC_SetPlayerAVMode;
	pTC_GetCurFrameNo						m_pTC_GetCurFrameNo;
	pTC_GetFrameListCount					m_pTC_GetFrameListCount;
	pTC_SetPlayDelay						m_pTC_SetPlayDelay;
	pTC_SetDecCallBack						m_pTC_SetDecCallBack;
	
public:
	CLS_AVPlaySdkApi();
	~CLS_AVPlaySdkApi();

	static CLS_AVPlaySdkApi* Instance(void);
	static void Destroy();

	int  LoadSdkLibrary();
	void  FreeSdkLibrary();

	int CreateSystem(PLAYHWND _hWnd);
	int DeleteSystem();
	int	CreatePlayer(int _iType, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
	int CreatePlayerFromFile(PLAYHWND _hWnd, char* _pcFileName, int _iBufSize = 0);
	int CreatePlayerFromStream(PLAYHWND _hWnd, unsigned char* _pucFileHeadBuf, int _iHeadSize);
	int CreatePlayerFromStreamEx(PLAYHWND _hWnd, unsigned char* _pucFileHeadBuf, int _iHeadSize, int _iStreamBufferSize);
	int CreatePlayerFromVOD(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize);
	int DeletePlayer(int _iID);
	int DeletePlayerEx(int _iID, int _iParam);
	void RegisterEventMsg(PLAYHWND _hEventWnd, UINT _uiEventMsg = MSG_PLAYSDKM4);
	int RegisterNotifyPlayToEnd(void* _PlayEndFun);
	int RegisterNotifyGetDecAV(int _iID, void* _GetDecAVCbk, void* _pvUserData);
	int RegisterDrawFun(int _iID, pfCBDrawFun _pfDrawFun, void* _pvUserData);
	int SetPlayRect(int _iID, RECT* _pDrawRect);
	int SetPlayRectEx(int _iID, RECT* _pDrawRect, DWORD _dwMask);
	int Play(int _iID);
	int Stop(int _iID);
	int GoBegin(int _iID);
	int GoEnd(int _iID);
	int StepForward(int _iID);
	int StepBackward(int _iID);
	int FastForward(int _iID, int _iSpeed);
	int SlowForward(int _iID, int _iSpeed);
	int FastBackward(int _iID);
	int PlayAudio(int _iID);
	int StopAudio(int _iID);
	int SetAudioVolume(unsigned short _ustVolume);
	int GetAudioVolume(unsigned short &_ustVolume);
	int SetAudioVolumeEx(int _iID, unsigned short _ustVolume);
	int GetAudioVolumeEx(int _iID, unsigned short &_ustVolume);
	int Seek(int _iID, int _iFrameNo);
	int SeekEx(int _iID, int _iFrameNo);
	int GetFrameRate(int _iID);
	int GetPlayingFrameNum(int _iID);
	int GetFrameCount(int _iID);
	int GetBeginEnd(int _iID, int* _piBegin, int* _piEnd);
	int GetFileName(int _iID, char* _pcFileName);
	int CapturePic(int _iID, unsigned char** _ppucYUV);
	int CaptureBmpPic(int _iID, char* _pcSaveFile);
	int CaptureJpegPic(int _iID, char* _pcSaveFile);
	int CapturePicture(int _iID, int _iType, char* _pcSaveFile);
	int CapturePicData(int _iID, int _iPicType, char* _pPicBuf, int* _iPicSize);
	int PutStreamToPlayer(int _iID, unsigned char* _pucStreamData, int _iSize);
	int StartMonitorCPU();
	int StopMonitorCPU();
	int GetVersion(SDK_VERSION *_ver);
	int SetVoDPlayerOver(int _iID);
	int SetCleanVoDBuffer(int _iID);
	int GetPlayTime(int _iID);
	int Pause(int _iID);
	int GetVideoParam(int, int*, int*, int*);
	int DrawRect(int _iID, RECT *_rcDraw, int _iCount);
	int SetEZoom(int _iID, int _iZoomID, RECT _pRectInVideo);
	int ResetEZoom(int _iID, int _iZoomID);
	int AddEZoom(int _iID, PLAYHWND _hWnd, RECT* _rcZoom, int _iCount);
	int RemoveEZoom(int _iID, int _iZoomID);
	int ResetPlayWnd(int _iID, PLAYHWND _hWnd);
	int SearchVCAInfo( int _iID, int _iSearchRule, int _iFrameStart, void* _pParamBuf, int _iBufSize, int _iContinue, unsigned int &_iFrame );
	int GetBeginEndTimeStamp(int _iID, unsigned int *_puiBegin, unsigned int * _puiEnd);
	int SetVideoDecryptKey(int _iID, char* _lpBuf, int _iBufSize);
	int GetVideoDecryptKey(int _iID, char* _lpBuf, int _iBufSize);
	int RegisterCommonEventCallBack(int _iID, pfCBCommonEventNotify _pf, void* _pUserData);
	int RegisterCommonEventCallBack_V4(int _iID, pfCBCommonEventNotifyEx _pf, void* _pvUserData);
	int SetModeRule(int _iID, int _iModeRule);
	int DrawPolygonOnLocalVideo(int _iID, POINT* _pPointArray, int _iPointCount, int _iEnableArrow);
	int GetMarkInfo(int _iID, PVOID _pData,int _iSize);
	int SetMarkInfo(int _iID,int _iFrameNO,int _iType);
	int SeekMark(int _iID,int _iFrameNO);
	int StartCaptureFile(int _iID, char* _cFileName,int _iRecFileType);
	int StopCaptureFile(int _iID);
	int SetStreamBufferSize(int _iID, int _iBufSize);
	int SetFrameListBufferSize(int _iID, int _iBufSize);
	int SetVerticalSync(int _iID,int _iFlag);
	int StartAudioCapture(int _iID, int _iWavein,int _iWaveout);
	int StopAudioCapture(int _iID);
	int RegisterNotifyAudioCapture(void* _pvAudioCaptureFun, void* _pvContext);
	int SetFishEyeCorrectEnable(int _iID, int _iEnbale, CreateCorrectPara* _ptPara);
	int GetFishEyeCorrectEnable(int _iID, int* _piEnbale);
	int CreateFishEyeCorrect(int _iID, CreateCorrectPara* _ptPara, int _iParaSize);
	int DeleteFishEyeCorrect(int _iID, int _iSubID);
	int SetFishEyeCorrectPara(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize);
	int GetFishEyeCorrectPara(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize);
	int SetFishEyeCorrectWnd(int _iID, int _iSubID, PLAYHWND _hWnd);
	int SetFishEyeCorrectCallBack(int _iID, int _iSubID, FISHEYE_CallBack _cbFunc, void* _pvUsrData);
	int CapturePictureEx(int _iID, int _iType, int _iSubId, char* _pcSaveFile);
	int PlayBackControl(int _iID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
	int StartFECTrack(int _iID, FECTrackInfo* _ptTrackInfo, int _iBufSize);
	int StopFECTrack(int _iID, int _iSubID = -1);
	int SetAVMode(int _iCmd, void* _pvBuf, int _iBufSize);
	int SetPlayerAVMode(int _iID, int _iCmd, void* _pvBuf, int _iBufSize);
	int GetCurFrameNo(int _iID);
	int GetFrameListCount(int _iID);
	int SetPlayDelay(int _iID, int _iDelayNum);
	int SetDecCallBack(int _iID, DECYUV_NOTIFY_V4 _cbkGetYUV, void* _pvUserData);
};

#endif


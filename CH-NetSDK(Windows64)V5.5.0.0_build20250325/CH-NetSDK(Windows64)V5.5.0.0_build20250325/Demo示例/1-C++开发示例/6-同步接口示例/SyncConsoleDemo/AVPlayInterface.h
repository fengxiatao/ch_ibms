#ifndef _AVPLAY_INTERFACE_H_
#define _AVPLAY_INTERFACE_H_

#include "AVPlaySdkTypes.h"

#ifdef __cplusplus
extern "C"{
#endif

/***********  System Creation And Release  ********************************************/
int CALLING_CONVENTION TC_CreateSystem(PLAYHWND _hWnd);
int CALLING_CONVENTION TC_DeleteSystem();

/***********   Create And Close Playback Instances  **************************************/
/************************************************************************
* 
Function:				   Create Player
Parmeter:			
_iType					   Player Type  PlayModeCmd
_pvCmdBuf				   Parameter Required To Create Player
_iBufLen				   Size Of _pvCmdBuf
_pvUserData				   User Data

Return:Player ID
>=0						   Success
<0						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_CreatePlayer(int _iType, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
int CALLING_CONVENTION TC_CreatePlayerFromFile(PLAYHWND _hWnd, char* _pcFileName, int _iDownloadBufSz, int _iFileTrueSz
											   , int* _piNowSz, int _iLastFrmNo, int* _piCompleteFlag);
#ifdef __WIN__
int CALLING_CONVENTION TC_CreatePlayerFromStream(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize);
#else
int CALLING_CONVENTION TC_CreatePlayerFromStream(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize, int _iStreamBufferSize);
#endif
int CALLING_CONVENTION TC_CreatePlayerFromStreamEx(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize, int _iStreamBufferSize);
int CALLING_CONVENTION TC_CreatePlayerFromVoD(PLAYHWND _hWnd, unsigned char* _pucVideoHeadBuf, int _iHeadSize);
/************************************************************************
	* 
	Function:				   Multi_Window PlayBack
	Parameter: 
	_iID					   Player ID
	_hWnd		               Handle Of Windows
	Return: 				   
	>=0						   Success
	-1						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_CreateDuplicatePlayer(int _iID, PLAYHWND  _hWnd);
/************************************************************************
* 
Function:					Create Player According To Different Requirements In CMD
Parameter:
	_iCmd					Command Code
	_pvCmdBuf				Buffer Of Command Parameter
	_iBufLen				Size Of Buffer
	_pvUserData				User Data
Return:				   
>=0							Success
-1							Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_CreatePlayerForSpecialNeeds(int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
int CALLING_CONVENTION TC_DeletePlayer(int _iID);
int CALLING_CONVENTION TC_DeletePlayerEx(int _iID, int _iParam);		//When You Stop Playing Video, You Can Set Options, Such As Whether To Retain The Last Frame Image Or Not

/**********  Registration Message And Callback  ******************************************/
void CALLING_CONVENTION TC_RegisterEventMsg(PLAYHWND _hEventWnd, UINT _uiEventMsg/* = MSG_PLAYSDKM4*/);
int CALLING_CONVENTION TC_RegisterNotifyPlayToEnd(void* _PlayEndFun);
int CALLING_CONVENTION TC_RegisterDrawFun(int _iID, pfCBDrawFun _pfDrawFun, void* _pvUserData);
int CALLING_CONVENTION TC_SetDecCallBack(int _iID, DECYUV_NOTIFY_V4 _cbkGetYUV, void* _pvUserData);
#ifdef __WIN__
int CALLING_CONVENTION TC_RegisterNotifyGetDecAV(int _iID, void* _GetDecAVCbk, bool _blDisplay);
int CALLING_CONVENTION TC_RegisterCommonEventCallBack(pfCBCommonEventNotifyEx _pf);
#else
int CALLING_CONVENTION TC_RegisterNotifyGetDecAV(int _iID, pfCBGetDecAV _GetDecAVCbk, void* _pvUserData);
int CALLING_CONVENTION TC_RegisterCommonEventCallBack(int _iID, pfCBCommonEventNotify _pf, void* _pUserData);
#endif
/************************************************************************
Set Draw Picture Callback
_lUserData : User Data
_lpCmd     : The Current Pass-In Pointer Identifies Whether The Callback HDC Scales By Window Size 0:UnZoom; 1:Zoom ;1 by default.
_iLen      : Size Of _lpCmd
************************************************************************/
int CALLING_CONVENTION TC_RegisterDrawFunEx(int _iID, pfCBDrawFun _pfDrawFun, void* _pvUserData, void *_pCmd, int _iCmdLen);

/**********  Player Control    *********************************************/
int CALLING_CONVENTION TC_SetPlayRect(int _iID, RECT* _pDrawRect);
int CALLING_CONVENTION TC_SetPlayRectEx(int _iID, RECT* _pDrawRect, DWORD _dwMask);
int CALLING_CONVENTION TC_SetSrcRect(int _iID, RECTEX* _pSrc);
int CALLING_CONVENTION TC_Play(int _iID);
int CALLING_CONVENTION TC_Pause(int _iID);
int CALLING_CONVENTION TC_Stop(int _iID);
int CALLING_CONVENTION TC_GoBegin(int _iID);
int CALLING_CONVENTION TC_GoEnd(int _iID);
int CALLING_CONVENTION TC_GoEndEx(int _iID);
int CALLING_CONVENTION TC_StepForward(int _iID);
int CALLING_CONVENTION TC_StepBackward(int _iID);
int CALLING_CONVENTION TC_FastForward(int _iID, int _iSpeed);
int CALLING_CONVENTION TC_SlowForward(int _iID, int _iSpeed);
int CALLING_CONVENTION TC_FastBackward(int _iID);
int CALLING_CONVENTION TC_PlayAudio(int _iID);
int CALLING_CONVENTION TC_StopAudio(int _iID);
int CALLING_CONVENTION TC_SetAudioVolumn(unsigned short _ustVolume);
int CALLING_CONVENTION TC_SetAudioVolume(unsigned short _ustVolume);
int CALLING_CONVENTION TC_Seek(int _iID, int _iFrameNo);
int CALLING_CONVENTION TC_SeekEx(int _iID, int _iFrameNo);
int CALLING_CONVENTION TC_PlayerControl(int _iID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
int CALLING_CONVENTION TC_VODPlayerControl(int _iID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUserData);
int CALLING_CONVENTION TC_RefreshPlay(int _iID, int _iCmd, void* _pvCmdBuf, int _iBufLen);

/*********  Get Play Info   **********************************************/
int CALLING_CONVENTION TC_GetFrameRate(int _iID);						//	Get Current Frame Rate
int CALLING_CONVENTION TC_GetPlayingFrameNum(int _iID);					//	Get Current Playing Total Frame Number
int CALLING_CONVENTION TC_GetFrameCount(int _iID);						//	Get Total Number Of Frame
int CALLING_CONVENTION TC_GetCurFrameNo(int _iID);					//	Get The Frame Number Currently Playing
int CALLING_CONVENTION TC_GetBeginEnd(int _iID, int* _piBegin, int* _piEnd);			
int CALLING_CONVENTION TC_GetFileName(int _iID, char* _pcFileName);
int CALLING_CONVENTION TC_GetVideoParam(int _iID, OUT int* _pWidth, OUT int *_pHeight, OUT int *_pFrameRate);
int CALLING_CONVENTION TC_GetPlayTime(int _iID);
int CALLING_CONVENTION TC_GetFrameListCount(int _iID);
//Setting The Critical Value of Clearing Framelist in Flow Mode
int CALLING_CONVENTION TC_SetPlayDelay(int _iID, int _iDelayNum);

/********   Snap    ************************************************/
int CALLING_CONVENTION TC_CapturePic(int _iID, unsigned char** _ppucYUV);
int CALLING_CONVENTION TC_CaptureBmpPic(int _iID, char* _pcSaveFile);
int CALLING_CONVENTION TC_CaptureJpegPic(int _iID, char* _pcSaveFile);
int CALLING_CONVENTION TC_CapturePicture(int _iID, int _iType, char* _pcSaveFile);
int CALLING_CONVENTION TC_CapturePicData(int _iID, int _iPicType, char* _pPicBuf, int* _iPicSize);

/********   Streaming Play  ***************************************************/
int CALLING_CONVENTION TC_PutStreamToPlayer(int _iID, unsigned char* _pucStreamData, int _iSize);
/************************************************************************
* 
Function:				   Put Stream
Parameter:			
_iID					   Player ID
_pucStreamData			   Sending Data
_iSize				       Size Of Sending Data
_pUser				       User Data
_iUserLen                  Size Of User Data           

Return:
#define		RET_BUFFER_FULL					(-11)		//	Buffer Full, Data Not Put In Buffer
#define		RET_BUFFER_WILL_BE_FULL			(-18)		//	Coming Full, Reduce The Frequency of Incoming Data
#define		RET_BUFFER_WILL_BE_EMPTY		(-19)		//	Coming Empty,Improving The Frequency of Data Entry
#define     RET_BUFFER_IS_OK                (-20)       //  Normally, Data Can Be Placed                                                             
************************************************************************/
int CALLING_CONVENTION TC_PutStreamToPlayerEx(int _iID, unsigned char* _pucStreamData, int _iSize, void* _pvUserData);
int CALLING_CONVENTION TC_PutFrameToPlayer(int _iID, unsigned char* _pucStreamData, int _iSize);
int CALLING_CONVENTION TC_CleanStreamBuffer(int _iID);
int CALLING_CONVENTION TC_SetFrameListBufferSize(int _iID, int _iBufSize, int _iMaxFrameCount/* = -1*/);
int CALLING_CONVENTION TC_SetStreamBufferSize(int _iID, int _iBufSize);
int CALLING_CONVENTION TC_SetVoDPlayerOver(int _iID);
int CALLING_CONVENTION TC_SetVoDPlayerOverEx(int _iID, int _iFlag);
int CALLING_CONVENTION TC_SetCleanVoDBuffer(int _iID);
int CALLING_CONVENTION TC_EnablePlayStreamCtrl(int _iID, bool _blCtrl);
int CALLING_CONVENTION TC_GetStreamPlayBufferState(int _iID, int* _piStreamBufferState);
/*********   CPU Monitor   *******************************************/
int CALLING_CONVENTION TC_StartMonitorCPU();
int CALLING_CONVENTION TC_StopMonitorCPU();

/*********  Get Version Information   *********************************************/
int CALLING_CONVENTION TC_GetVersion(SDK_VERSION* _ver);

/*********  Video File Editing  ***************************************************/
int CALLING_CONVENTION TC_Locate(int _iID, int* _piFrameNo);
int CALLING_CONVENTION TC_RegisterVECallBack(void* _pVECallBack);
int CALLING_CONVENTION TC_RegisterVECallBackEx(pfCBVideoEdit _pVECallBack);	
int CALLING_CONVENTION TC_SegmentToFile(char* _pcSaveFile, char* _pcSourceFile, int _iBeginFrmNo, int _iEndFrmNo);
int CALLING_CONVENTION TC_Combine(char* _pcSaveFile, char* _pcSource1, int _iBeginFrmNo1, int _iEndFrmNo1, char* _pcSource2, int _iBeginFrmNo2, int _iEndFrmNo2);
int CALLING_CONVENTION TC_RemoveAudio(char* _pcSaveFile, char* _pcSourceFile, int _iBeginFrmNo, int _iEndFrmNo);
int CALLING_CONVENTION TC_GetVEState(int* _piState, int* _piProgress);
int CALLING_CONVENTION TC_VEAbort();
int CALLING_CONVENTION TC_VECleanup();

// Setting Rules For Special Situations
int CALLING_CONVENTION TC_SetModeRule(int _iID, int _iModeRule);

// Switching Display Screen
int CALLING_CONVENTION TC_ResetPlayWnd(int _iID, PLAYHWND _hWnd);
int CALLING_CONVENTION TC_DrawRect(int _iID, RECT *_rcDraw, int _iCount);
int CALLING_CONVENTION TC_AddEZoom(int _iID, PLAYHWND _hWnd, RECT* _rcZoom, int _iCount);
int CALLING_CONVENTION TC_SetEZoom(int _iID, int _iZoomID, RECT _pRectInVideo);
int CALLING_CONVENTION TC_RemoveEZoom(int _iID, int _iZoomID);
int CALLING_CONVENTION TC_ResetEZoom(int _iID, int _iZoomID);

/************************************************************************
* Relevant Interfaces for Multi-Screen Display                                                                     
************************************************************************/
int CALLING_CONVENTION TC_ChangeMonitor(int _iID, int _iMonitorIndex);
int CALLING_CONVENTION TC_GetMonitorCount();
int CALLING_CONVENTION TC_GetMonitorInfo(int _iMonitorIndex, MONITOR_INFO* _pMonitorInfo);
int CALLING_CONVENTION TC_UpdateMonitor();

/************************************************************************
*Get VCA Information In The Video                                                                     
************************************************************************/

/************************************************************************
*  Search According To Certain Rules                                                                   
*	_iID:					Player ID
	_iSearchFlag			Search Rules,  1:Perimeter 2:Stumbling Line
	_piFrameStart			Start Frame Number
	_pParamBuf				Input Parameter Buffer, 1: The Structure Of Perimeter  2:Stumbling Line Information Structure
	_iBufSize				Buffer Size,    1: Size of Perimeter Structure, 2: Size Of Stumbling Line
	_iContinuePlay          Whether To Continue Playing After Successful Positioning 
	                        0:Stop       1:Continue   
	_puiFrameResult         Successful Location of Frame Number
*	Return:
					>=0		Success
					<0		Failed
************************************************************************/
int CALLING_CONVENTION TC_SearchVCAInfo(int _iID, int _iSearchRule, int _iFrameStart, void* _pParamBuf, int _iBufSize, int _iContinuePlay, unsigned int* _puiFrameResult);

int CALLING_CONVENTION TC_GetBeginEndTimeStamp(int _iID, unsigned int * _puiBeginTimeStamp, unsigned int * _puiEndTimeStamp);

/************************************************************************
* Set And Get Decrpty Key
* Parameter:
		_iID				PlayerID
		_lpBuf				Decrypt Key Buffer
		_iBufSize			Buffer size (<=16)
*	Return:
		>=0		Success
		<0		Failed
************************************************************************/
int CALLING_CONVENTION TC_SetVideoDecryptKey(int _iID, void* _lpBuf, int _iBufSize);
int CALLING_CONVENTION TC_GetVideoDecryptKey(int _iID, void* _lpBuf, int _iBufSize);

/************************************************************************
*	A general callback function that is currently used to notify upper-level applications when password errors need to be decrypted or decrypted                                                                     
************************************************************************/
int CALLING_CONVENTION TC_RegisterCommonEventCallBackEx(pfCBCommonEventNotifyEx _pf, void* _pvUserData);
int CALLING_CONVENTION TC_RegisterCommonEventCallBack_V4(int _iID, pfCBCommonEventNotifyEx _pf, void* _pvUserData);

/************************************************************************
* Set and get the corresponding audio volume according to the playback ID
* Parameter:
		_iID				PlayerID
		_usVolume           Volume Value To Set
		_pusVolume          Storage Address To Get Volume Value
*	Return:
		>=0		Success
		<0		Failed
************************************************************************/
int CALLING_CONVENTION TC_SetAudioVolumeEx(int _iID, unsigned short _usVolume);
int CALLING_CONVENTION TC_GetAudioVolumeEx(int _iID, unsigned short * _pusVolume);

/************************************************************************
* Draw Polygons On Local Videos
* Parameter:
		_iID				PlayerID
		_pPointArray        Pointer of Polygon Vertex Array
		_iPointCount        Number of Vertices of a Polygon
		_iFlag				Byte0:Are Arrows Allowed Byte1:Whether To Add Polygons Or Not Byte2:Modify Only The Polygon Drawn Last Time
*	Return:
		>=0		Success
		<0		Failed
************************************************************************/
int CALLING_CONVENTION TC_DrawPolygonOnLocalVideo(int _iID, POINT* _pPointArray, int _iPointCount, int _iFlag);


/************************************************************************/
/* Getting Frame User Data, Include GET_USERDATA_INFO_TIME, GET_USERDATA_INFO_OSD, GET_USERDATA_INFO_GPS, GET_USERDATA_INFO_VCA 
	_iFrameNO:  -2 First I Frame Time Absolute Time ,-1 Absolute Time Of Current Playback Frame
*/
/************************************************************************/
int CALLING_CONVENTION TC_GetUserDataInfo(int _iID, int _iFrameNO, int _iFlag, void*  _Buffer, int _iSize);

/************************************************************************
* Setting Ps Flow Callback Function                                                                    
************************************************************************/
int CALLING_CONVENTION TC_RegisterPsPackCallBack( PSPACK_NOTIFY _cbkPsPackNotify, void* _pContext /*= NULL*/ );

/************************************************************************
* 
Function:				   Get Virtual Frame Information
Parameter:
_iID					   Player ID
_pData		               Dot Frame
_iSize					   Sizeof Dot Frame
Return : 				   Number Of Dot Frame		   If_puiData == NULL Or _iSize < 4 Query Number Of Dot Frame
>=0						   Success
-1						   Failed
************************************************************************/
int CALLING_CONVENTION TC_GetMarkInfo(int _iID, PVOID _pData/* = NULL*/, int _iSize/* = 0*/);



/************************************************************************
* 
Function: 				   Get Virtual Frame Information
Parameter:
_iID					   Player ID
_iType					   Tag Type: 0:unlabeled 1:labeled 2:CD Serial Number 4:Independent Audio 8: Vital Signs 
_pData		               Dot Frame
_iSize					   Sizeof Dot Frame
return value:					   Number Of Dot Frame		   If_puiData == NULL Or _iSize < 4 Query Number Of Dot Frame
>=0						   Success
-1						   Failed                                                                  
************************************************************************/
int CALLING_CONVENTION TC_GetKeyFrameInfo(int _iID, int _iType, PVOID _pData/* = NULL*/, int _iSize/* = 0*/);

/************************************************************************
* 
Function:                  Add Or Delete One Dot Information
Parameter:
_iID					   Player ID
_iFrameNO				   Dot Frame
Return : 				   Added Dot I Frame Number
>=0						   Success
-1						   Failed  
************************************************************************/
int CALLING_CONVENTION TC_SetMarkInfo(int _iID,int _iFrameNO, int _iType);

/************************************************************************
* 
Function:				   Locate To a Tag
Parameter:
_iID					   Player ID
_iFrameNO				   Dot Frame
Return : 		   
>=0						   Success
-1						   Failed  

************************************************************************/
int CALLING_CONVENTION TC_SeekMark(int _iID,int _iFrameNO);


/************************************************************************
* 
Function:				   Setting Audio Play Parameters
Parameter:
_iID					   Player ID
_iChannel                  Channel 1 Is Mono Channel, 2 Is Stereo  
_iSamplePerSec:            Sampling Frequency 8000 	   
_iBitsPerSample:           Sampling Data Bits  16>=0	

Return : 		   
>=0						   Success
-1						   Failed  
************************************************************************/
int CALLING_CONVENTION TC_SetAudioPlayParam(int _iID,int _iChannel, int _iSamplePerSec, int _iBitsPerSample);

int CALLING_CONVENTION TC_StartCaptureFile(int _iID, char* _cFileName,int _iRecFileType);
int CALLING_CONVENTION TC_StopCaptureFile(int _iID);

int CALLING_CONVENTION TC_RegisterNotifyAudioCapture(void* _pvAudioCaptureFun,void* _pvContext);
int CALLING_CONVENTION TC_StartAudioCapture(int _iID, int _iWavein,int _iWaveout);
int CALLING_CONVENTION TC_StopAudioCapture(int _iID);

int CALLING_CONVENTION TC_PackStreamToRawStream(unsigned char* _pucSrc, int _iSrcLen, unsigned char* _pucDst, int _iDstLen, int _iType, void* _pvReserved);
int CALLING_CONVENTION TC_RawStreamToPackStream(unsigned char* _pucSrc, int _iSrcLen, unsigned char* _pucDst, int _iDstLen, int _iType, void* _pvReserved);

/************************************************************************
* 
Function:				   Registering Original Stream Callback Function  
Parameter:
_iID					   Player ID
_pFun		               Callback Function
_pvParam				   User Data
Return :				   Success Register Or Not
>=0						   Success
-1						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_RegisterRawFrameCallback(int _iID, void* _pFun, void* _pvParam);
int CALLING_CONVENTION TC_SetRawCallback(int _iID, pfCBGetRaw _pCallback, void* _pUdata);

/************************************************************************
* 
Function:				   Set vertical synchronization parameters. Opening vertical synchronization prevents splitting, but it consumes a lot of Cpu
Parameter:
_iID					    
_iFlag		                
Return :				   
>=0						   Success
-1						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_SetVerticalSync(int _iID,int _iFlag);

/************************************************************************
* 
Function:				   Obtaining Vertical Synchronization Parameters
Parameter:
_iID					   Failed ID
_piFlag		               Synchronous Marking:  0: Close 1: Open
Return :				   
>=0						   Success
-1						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_GetVerticalSync(int _iID,int* _piFlag);

/************************************************************************
* 
Function:				   Clear Local Drawn Polygons
Parameter:
_iID					   Failed ID
_iPolygonIndex		       Polygon Index
Return :				   
>=0						   Success
-1						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_ClearPolyLocalVideo(int _iID, int _iPolygonIndex);

/************************************************************************
* 
Function:				   Acquisition of Audio Data
Parameter:
_iEnable				  Switching State 0:Close 1:Open
Return :				   
>=0						   Success
-1						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION TC_RecordAudio(int _iEnable);

/************************************************************************
* 
Function:				   Audio Acquisition Callback Registration Function
Parameter:
_cbkGetVoice			   Callback
Return :				   
>=0						   Success
-1						   Failed                                                                  
************************************************************************/
int CALLING_CONVENTION TC_RegisterNotifyGetVoice(RECORD_VOICE_NOTIFY _cbkGetVoice);

/************************************************************************
* 
Function:				   Drawing Srings
Parameter:
_iID					   Player ID
_iX						   X
_iY						   Y
_cText					   String To Display
_iColor					   String Color
Return :				   
>=0						  Success
-1						  Failed                                                                 
************************************************************************/
int CALLING_CONVENTION  TC_DrawText(int _iID,int _iX, int _iY, char* _cText, int _iColor);

/************************************************************************
* 
Function:				   Whether To Display A String (Frame Rate)
Parameter:
_iID					   Player ID
_iShowFlag				   Display Frame Rate Bit Rate
Return :				   
>=0						   Success
-1						   Failed                                                                
************************************************************************/
int CALLING_CONVENTION  TC_DrawTextFlag(int _iID, int _iShowFlag);

int CALLING_CONVENTION TC_SetFileHeader(int _iID, unsigned char *_pFileHeader, int _iHeaderLen);

/************************************************************************
* 
Function:				   Continuous Playback Of Videos
Parameter:
_iID					   Player ID
_pcFileName				   File Path

Return :
>=0						   Success
<0						   Failed                                                                  
************************************************************************/
int CALLING_CONVENTION TC_AttachFile(int _iID, char* _pcFileName);

int CALLING_CONVENTION TC_SycAddPlayer(int _iGroupId, int _iPlayerId);
int CALLING_CONVENTION TC_SycDelPlayer(int _iGroupId, int _iPlayerId);

int CALLING_CONVENTION TC_SetFishEyeCorrectEnable(int _iID, int _iEnbale, CreateCorrectPara* _ptPara);

int CALLING_CONVENTION TC_GetFishEyeCorrectEnable(int _iID, int* _piEnbale);

int CALLING_CONVENTION TC_CreateFishEyeCorrect(int _iID, CreateCorrectPara* _ptPara, int _iParaSize);

int CALLING_CONVENTION TC_DeleteFishEyeCorrect(int _iID, int _iSubID);

int CALLING_CONVENTION TC_SetFishEyeCorrectPara(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize);

int CALLING_CONVENTION TC_GetFishEyeCorrectPara(int _iID, int _iSubID, int _iCmd, void* _pvPara, int _iBufSize);

int CALLING_CONVENTION TC_SetFishEyeCorrectWnd(int _iID, int _iSubID, PLAYHWND _hWnd);

int CALLING_CONVENTION TC_SetFishEyeCorrectCallBack(int _iID, int _iSubID, FISHEYE_CallBack _cbFunc, void* _pvUsrData);

int CALLING_CONVENTION TC_CapturePictureEx(int _iID, int _iType, int _iSubId, char* _pcSaveFile);

int CALLING_CONVENTION TC_StartFECTrack(int _iID, FECTrackInfo* _ptTrackInfo, int _iBufSize, int _iSubID/* = -1*/);

int CALLING_CONVENTION TC_StopFECTrack(int _iID, int _iSubID/* = -1*/);	

int CALLING_CONVENTION TC_SetVideoMode(int _iCmd, void* _pvBuf, int _iBufSize);

int CALLING_CONVENTION TC_SetAVMode(int _iCmd, void* _pvBuf, int _iBufSize);

int CALLING_CONVENTION TC_SetPlayerAVMode(int _iID, int _iCmd, void* _pvBuf, int _iBufSize);
//Getting Index Frame Data
int CALLING_CONVENTION TC_GetIFrameIndexInfo(int _iID, void** _pvData, int* _piLen);
//Release Index Frame Data
int CALLING_CONVENTION TC_FreeIFrameIndexInfo(int _iID);
//Get The Frame Number According To The Location Of The File
int CALLING_CONVENTION TC_GetFrameNoByFilePos(int _iID, SDKint64 _iFilePos, OUT SDKint64* _piFrameNo);
//Register for Bare Stream Data Callback
int CALLING_CONVENTION TC_SetRawNotify(int _iID, RAWFRAME_NOTIFY _pGetRawFrmCbk, void* _pvUserData);

int CALLING_CONVENTION TC_Swscale_YUV2RGB565(SwscaleInfo* in, SwscaleInfo* out);

int CALLING_CONVENTION TC_Swscale_YUV2RGB24(SwscaleInfo* in, SwscaleInfo* out);

//clear people statistics
int CALLING_CONVENTION TC_ClearPeopleCountInfo(int _iID);

/*************************************************************************
Function:					TC_CreateQtWidget
Parameter:
	NULL
Return :
=other						Success
=NULL						Failed
************************************************************************/
void* CALLING_CONVENTION  TC_CreateQtWidget(void* _pvParaBuf, int _iBufSize);

/*************************************************************************
Function:					TC_ReleaseQtWidget
Parameter:
_pQtWidget					Released widget
Return :
>=0							Success
<0							Failed
************************************************************************/
int CALLING_CONVENTION  TC_ReleaseQtWidget(void* _pQtWidget);

#ifdef __cplusplus
};
#endif

#endif


#include "NVSSDK_INTERFACE.h"
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifndef  WIN32
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <net/if.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/times.h>
#include <sys/time.h>
#include <stdint.h>
#include <dlfcn.h>
#endif


typedef int (__stdcall *pNetClient_Startup_V4)(int _iServerPort, int _iClientPort, int _iWnd);
typedef int (__stdcall *pNetClient_SetNotifyFunction_V4)(MAIN_NOTIFY_V4        _MainNotify, 
		ALARM_NOTIFY_V4       _AlarmNotify,
		PARACHANGE_NOTIFY_V4  _ParaNotify,
		COMRECV_NOTIFY_V4     _ComNotify,
		PROXY_NOTIFY       _ProxyNotify);
typedef int (__stdcall *pNetClient_StartRecv_V4)(unsigned int* _uiRecvID, CLIENTINFO* _cltInfo, NVSDATA_NOTIFY _cbkDataArrive,void* _iUserData);
typedef int (__stdcall *pNetClient_StartRecv_V5)(unsigned int* _puiRecvID, NetClientPara* _ptPara, int _iParaSize);
typedef int (__stdcall *pNetClient_SetNotifyUserData_V4)(int _iLogonID,void* _iUserData);
typedef int (__stdcall *pNetClient_SetComRecvNotify_V4)(COMRECV_NOTIFY_V4 _comNotify);
typedef int (__stdcall *pNetClient_GetHTTPPort_V4)(int _iLogonID, int* _iPort);
typedef int (__stdcall *pNetClient_SetHTTPPort_V4)(int _iLogonID, int _iPort);
typedef int (__stdcall *pNetClient_SetDomainParsePara_V4)(int _iLogonID, int _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int _iPort1, int _iPort2);
typedef int (__stdcall *pNetClient_GetDomainParsePara_V4)(int _iLogonID, int* _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int* _iPort1, int* _iPort2);
typedef int (__stdcall *pNetClient_GetBitrateOnVideo_V4)(unsigned int _ulConID, int* _piX, int* _piY, int* _piEnabled, char* _pcInfo);
typedef int (__stdcall *pNetClient_SetDecCallBack_V4)(unsigned int _ulConID, DECYUV_NOTIFY_V4 _cbkDecYUV, void* _iUserData);
typedef int (__stdcall *pNetClient_RegisterDrawFun)(unsigned int _ulConID, CBK_DRAW_FUNC _pfDrawFun, long _lUserData, void* _pCmd, int _iCmdLen);
typedef int (__stdcall *pNetClient_SetPort)( int _iServerPort, int _iClientPort );
#ifdef WIN32
typedef int (__stdcall *pNetClient_Startup)();
typedef int (__stdcall *pNetClient_SetNotifyFunction)( LOGON_NOTIFY _LogonNotify,ALARM_NOTIFY _AlarmNotify, PARACHANGE_NOTIFY _ParaNotify);
typedef int (__stdcall *pNetClient_SetNotifyFunctionEx)( MAIN_NOTIFY _cbkMainNotify, ALARM_NOTIFY_EX _cbkAlarmNotify, PARACHANGE_NOTIFY_EX _cbkParaChangeNotify, void* _pNotifyUserData );
typedef int (__stdcall *pNetClient_SetMSGHandle)( unsigned int _uiMessage, HWND _hWnd, unsigned int _uiParaMsg, unsigned int _uiAlarmMsg );
typedef int (__stdcall *pNetClient_SetMSGHandleEx)( unsigned int _uiMessage, HWND _hWnd, unsigned int _uiParaMsg, unsigned int _uiAlarmMsg );
typedef int (__stdcall *pNetClient_StartRecv)(unsigned int* _ulConID, CLIENTINFO* _cltInfo, RECVDATA_NOTIFY _cbkDataArrive);
typedef int (__stdcall *pNetClient_StartRecvEx)(unsigned int* _ulConID,CLIENTINFO* _cltInfo,RECVDATA_NOTIFY_EX _cbkDataNotifyEx, void* _lpUserData);
typedef int (__stdcall *pNetClient_SetComRecvNotify)(COMRECV_NOTIFY _comNotify);
typedef int (__stdcall *pNetClient_GetHTTPPort)(int _iLogonID, unsigned short* _iPort);
typedef int (__stdcall *pNetClient_SetHTTPPort)(int _iLogonID, unsigned short _iPort);
typedef int (__stdcall *pNetClient_SetDomainParsePara)(int _iLogonID, int _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,unsigned short _iPort1, unsigned short _iPort2);
typedef int (__stdcall *pNetClient_GetDomainParsePara)(int _iLogonID, int* _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,unsigned short* _iPort1, unsigned short* _iPort2);
typedef int (__stdcall *pNetClient_GetBitrateOnVideo)(unsigned int _ulConID, int* _iX, int* _iY, int* _iEnabled);
typedef int (__stdcall *pNetClient_SetDecCallBack)(unsigned int _ulConID, DECYUV_NOTIFY _cbkGetYUV, void* _pContext);
typedef int (__stdcall *pNetClient_InterTalkStartEx)(unsigned int * _uiConnID, int _iLogonID, RECVDATA_NOTIFY_EX _cbkDataArrive, void* _iUserData);
#else
typedef int (__stdcall *pNetClient_Startup)(int _iServerPort/*=3000*/, int _iClientPort/*=6000*/, int _iWnd/*=0*/);
typedef int (__stdcall *pNetClient_SetNotifyFunction)(MAIN_NOTIFY_V4        _MainNotify, 
										  ALARM_NOTIFY_V4       _AlarmNotify,
										  PARACHANGE_NOTIFY_V4  _ParaNotify,
										  COMRECV_NOTIFY_V4     _ComNotify,
										  PROXY_NOTIFY       _ProxyNotify);
typedef int (__stdcall *pNetClient_StartRecv)(unsigned int* _uiRecvID, CLIENTINFO* _cltInfo, NVSDATA_NOTIFY _cbkDataArrive,void* _iUserData);
typedef int (__stdcall *pNetClient_SetNotifyUserData)(int _iLogonID,void* _iUserData);
typedef int (__stdcall *pNetClient_SetComRecvNotify)(COMRECV_NOTIFY_V4 _comNotify);
typedef int (__stdcall *pNetClient_GetHTTPPort)(int _iLogonID, int* _iPort);
typedef int (__stdcall *pNetClient_SetHTTPPort)(int _iLogonID, int _iPort);
typedef int (__stdcall *pNetClient_SetDomainParsePara)(int _iLogonID, int _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int _iPort1, int _iPort2);
typedef int (__stdcall *pNetClient_GetDomainParsePara)(int _iLogonID, int* _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int* _iPort1, int* _iPort2);
typedef int (__stdcall *pNetClient_GetBitrateOnVideo)(unsigned int _ulConID, int* _piX, int* _piY, int* _piEnabled, char* _pcInfo);
typedef int (__stdcall *pNetClient_SetDecCallBack)(unsigned int _ulID, pfCBGetDecAV _cbkGetYUV, void* _iUserData);
typedef int (__stdcall *pNetClient_SetDecCallBackEx)(unsigned int _ulID, DECYUV_NOTIFY _cbkDecYUV, void* _iUserData);
typedef int (__stdcall *pNetClient_InterTalkStartEx)(unsigned int * _uiConnID, int _iLogonID, NVSDATA_NOTIFY _cbkDataArrive, void* _iUserData);
#endif
typedef int (__stdcall *pNetClient_Cleanup)();
typedef int (__stdcall *pNetClient_GetVersion)(SDK_VERSION* _ver);
typedef int (__stdcall *pNetClient_Logon)(char* _cProxy,char* _cIP,char* _cUserName,
					char* _cPwd,char* _cProductID,int _iPort);
typedef int (__stdcall *pNetClient_LogonEx)(char* _cProxy,char* _cIP,char* _cUserName,
							  char* _cPwd,char* _cProductID,int _iPort,const char* _pcCharSet);
typedef int (__stdcall *pNetClient_Logoff)(int _iLogonID);
typedef int (__stdcall *pNetClient_GetLogonStatus)(int _iLogonID);
typedef int (__stdcall *pNetClient_ProxyGetDevInfo)(int _iLogonID, int _iCmd, void* _pvOutBuf, int _iBufLen);
typedef int (__stdcall *pNetClient_StopRecv)(unsigned int _uiRecvID);
typedef int (__stdcall *pNetClient_GetRecvID)(int _iLogonID, int _iChannel, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetInfoByConnectID)(unsigned int _iConnectID,st_ConnectInfo* _stConnectInfo);
typedef int (__stdcall *pNetClient_SetFullStreamNotify )(unsigned int _uiRecvID, FULLFRAME_NOTIFY _cbkFullStream);
typedef int (__stdcall *pNetClient_SetFullStreamNotify_V4 )(unsigned int _uiRecvID, FULLFRAME_NOTIFY_V4 _cbkFullStream, void * _iUserData);
typedef int (__stdcall *pNetClient_GetCmdString)(int _iLogonID,int _iType,int _iPara,CMDSTRING_NOTIFY _cbkCmdString,void* _pUserData);
typedef int (__stdcall *pNetClient_GetDevInfo)(int _iLogonID,ENCODERINFO* _pEncoderInfo);
typedef int (__stdcall *pNetClient_SendDataToServer)(int _iLogonID,char* _cData,int _iLen);
typedef int (__stdcall *pNetClient_IsValidUser)(int _iLogonID,char* _cUserName,char* _cPwd);
typedef int (__stdcall *pNetClient_SetInnerDataNotify)(unsigned int _uiRecvID,INNER_DATA_NOTIFY _cbkNotify,void* _iUserData);
typedef int (__stdcall *pNetClient_SetWorkMode)(int _iWorkMode);
#ifdef WIN32
typedef int (__stdcall *pNetClient_AddActiveServer)(char* _cDevIP,char* _cFactoryID,void* _iSocket, void* _pUserData);
typedef int (__stdcall *pNetClient_BindSocket)(int _iLogonID,int _iChan,void* _iSocket, void* _pUserData);
#else
typedef int (__stdcall *pNetClient_AddActiveServer)(char* _cDevIP,char* _cFactoryID,int _iSocket, void* _pUserData);
typedef int (__stdcall *pNetClient_BindSocket)(int _iLogonID,int _iChan,int _iSocket, void* _pUserData);
#endif
typedef int (__stdcall *pNetClient_PushData)(int _iLogonID,int _iChan,char* _cData,int _iLen);
typedef int (__stdcall *pNetClient_DelActiveServer)(int _iLogonID);
typedef int (__stdcall *pNetClient_StartCaptureData)(unsigned long _ulID);
typedef int (__stdcall *pNetClient_StopCaptureData)(unsigned long _ulID);
typedef int (__stdcall *pNetClient_GetVideoHeader)(unsigned long _ulID,unsigned char* _ucHeader);
typedef int (__stdcall *pNetClient_SetRawFrameCallBack)(unsigned int _ulConID, RAWFRAME_NOTIFY _cbkGetFrame, void* _pContext);
typedef int (__stdcall *pNetClient_SetRawFrameCallBackEx)(unsigned int _ulConID, RAWFRAME_NOTIFY_EX _cbkGetFrame, void* _pContext);
typedef int (__stdcall *pNetClient_StartCaptureFile)(unsigned int _uiRecvID, char* _pszFileName, int _iRecordFileType);
typedef int (__stdcall *pNetClient_StopCaptureFile)(unsigned int _uiRecvID);
typedef int (__stdcall *pNetClient_GetCaptureStatus)(unsigned int _ulConID);
typedef int (__stdcall *pNetClient_SetCaptureFileSize)(unsigned int _uiRecvID, int _iFileSize);
typedef int (__stdcall *pNetClient_StartPlay)(unsigned int _ulID, int _hWnd, RECT _rcShow, unsigned int _uiDecflag);
typedef int (__stdcall *pNetClient_StartPlayEx)(unsigned int _ulID, void* _pvBuff, int _iBuffSize);
typedef int (__stdcall *pNetClient_StartPlayEs)(unsigned int _ulID, int _hWnd);
typedef int (__stdcall *pNetClient_StopPlay)(unsigned int _ulID);
typedef int (__stdcall *pNetClient_StopPlayEx)(unsigned int _ulID, unsigned int _iParam);
typedef int (__stdcall *pNetClient_SetPlayRectEx)(unsigned int _ulID, RECT* _pDrawRect, int _dwMask);
typedef int (__stdcall *pNetClient_SetSrcRect)(unsigned int _ulID, void* _pSrcRect);
typedef int (__stdcall *pNetClient_ResetPlayerWnd)(unsigned int _ulID, int _hwnd);
typedef int (__stdcall *pNetClient_GetPlayingStatus)(unsigned int _ulID);
typedef int (__stdcall *pNetClient_AdjustVideo)(unsigned int _ulID, RECT _rctShow);
typedef int (__stdcall *pNetClient_AudioStart)(unsigned int _ulID);
typedef int (__stdcall *pNetClient_AudioStop)(unsigned int _ulID);
typedef int (__stdcall *pNetClient_SetLocalAudioVolume)(int _iVolume);
typedef int (__stdcall *pNetClient_SetBufferNum)(unsigned int _ulID, int _iNum);
typedef int (__stdcall *pNetClient_SetPlayDelay)(unsigned long _ulID, int _iNum);
typedef int (__stdcall *pNetClient_GetChannelNum)(int _iLogonID, int* _piChanNum);
typedef int (__stdcall *pNetClient_GetAlarmPortNum)(int _iLogonID, int* _iAlarmInPortNum, int* _iAlarmOutPortNum);
typedef int (__stdcall *pNetClient_GetLocalAlarmNum)(int _iLogonID, int* _iLocalAlarmInNum, int* _iLocalAlarmOutNum);
typedef int (__stdcall *pNetClient_SetVideoPara)(int _iLogonID, int _iChannelNum, STR_VideoParam* _strVideoParam);
typedef int (__stdcall *pNetClient_GetVideoPara)(int _iLogonID, int _iChannelNum, STR_VideoParam* _strVideoParam);
typedef int (__stdcall *pNetClient_SetVideoparaSchedule)(int _iLogonID, int _iChannelNum,STR_VideoParam* _strVideoParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_GetVideoparaSchedule)(int _iLogonID, int _iChannelNum,STR_VideoParam* _strVideoParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_SetVideoQuality)(int _iLogonID, int _iChannelNum, int _iVideoQuality, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetVideoQuality)(int _iLogonID,int _iChannelNum,int* _iVideoQuality, int _iStreamNO);
typedef int (__stdcall *pNetClient_SetFrameRate)(int _iLogonID, int _iChannelNum, int _iFrameRate, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetFrameRate)(int _iLogonID, int _iChannelNum, int* _iFrameRate, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetDecordFrameNum )(unsigned int _ulConID);
typedef int (__stdcall *pNetClient_SetStreamType)(int _iLogonID, int _iChannelNum, int _iStreamType, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetStreamType)(int _iLogonID, int _iChannelNum, int* _iStreamType, int _iStreamNO);
typedef int (__stdcall *pNetClient_SetMotionAreaEnable )(int _iLogonID,int _iChannelNum);
typedef int (__stdcall *pNetClient_SetMotionDetetionArea)(int _iLogonID, int _iChannelNum, int _ix, int _iy,int _iEnabled);
typedef int (__stdcall *pNetClient_GetMotionDetetionArea)(int _iLogonID, int _iChannelNum, int _ix, int _iy,int* _iEnabled);
typedef int (__stdcall *pNetClient_SetThreshold)(int _iLogonID, int _iChannelNum,int _iThreshold);
typedef int (__stdcall *pNetClient_GetThreshold)(int _iLogonID, int _iChannelNum,int* _iThreshold);
typedef int (__stdcall *pNetClient_SetMotionDetection)(int _iLogonID, int _iChannelNum,int _iEnabled);
typedef int (__stdcall *pNetClient_GetMotionDetection)(int _iLogonID, int _iChannelNum,int* _iEnabled);
typedef int (__stdcall *pNetClient_SetMotionDecLinkRec)(int _iLogonID,int _iChannelNum,int _iEnable);
typedef int (__stdcall *pNetClient_GetMotionDecLinkRec)(int _iLogonID,int _iChannelNum,int* _iEnable);
typedef int (__stdcall *pNetClient_SetMotionDecLinkSnap)(int _iLogonID,int _iChannelNum,int _iEnable);
typedef int (__stdcall *pNetClient_GetMotionDecLinkSnap)(int _iLogonID,int _iChannelNum,int* _iEnable);
typedef int (__stdcall *pNetClient_SetMotionDecLinkSoundDisplay)(int _iLogonID,int _iChannelNum,int _iSoundEnable,int _iDisplayEnable);
typedef int (__stdcall *pNetClient_GetMotionDecLinkSoundDisplay)(int _iLogonID,int _iChannelNum,int* _iSoundEnable,int* _iDisplayEnable);
typedef int (__stdcall *pNetClient_SetMotionDecLinkOutport)(int _iLogonID,int _iChannelNum,int _iOutportArray);
typedef int (__stdcall *pNetClient_GetMotionDecLinkOutport)(int _iLogonID,int _iChannelNum,int* _iOutportArray);
typedef int (__stdcall *pNetClient_SetMotionDetectSchedule)(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_GetMotionDetectSchedule)(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_SetOsdDiaphaneity)(int _iLogonID,int _iChannelNum,int _iDiaphaneity);
typedef int (__stdcall *pNetClient_GetOsdDiaphaneity)(int _iLogonID,int _iChannelNum,int* _iDiaphaneity);
typedef int (__stdcall *pNetClient_SetOsdText)(int _iLogonID, int _iChannelNum, unsigned char * _pcTxtPtr, unsigned long _ulTextColor);
typedef int (__stdcall *pNetClient_GetOsdText)(int _iLogonID, int _iChannelNum, unsigned char* _pcOSDText, unsigned long* _pulTextColor);
typedef int (__stdcall *pNetClient_SetOsdType)(int _iLogonID, int _iChannelNum,int _iPositionX,int _iPositionY,int _iOSDType,int _iEnabled);
typedef int (__stdcall *pNetClient_GetOsdType)(int _iLogonID, int _iChannelNum, int _iOSDType, int* _iPositionX ,int* _iPositionY , int* _iEnabled);
typedef int (__stdcall *pNetClient_SetDateFormat)(int _iLogonID, int _iFormat, char _cSeparate);
typedef int (__stdcall *pNetClient_GetDateFormat)(int _iLogonID, int* _iFormat, char* _cSeparate);
typedef int (__stdcall *pNetClient_SetOsdLOGO )(int _iLogonID, int _iChannelNum, unsigned char * _cLogoFile, unsigned int _ulbkColor);
typedef int (__stdcall *pNetClient_SetAudioChannel)(int _iLogonID, int _iChannelNum, int _iAudioChannel);
typedef int (__stdcall *pNetClient_GetAudioChannel)(int _iLogonID, int _iChannelNum, int* _iAudioChannel);
typedef int (__stdcall *pNetClient_SetIpFilter)(int _iLogonID, char* _cFilterIP,char* _cFilterMask);
typedef int (__stdcall *pNetClient_GetIpFilter)(int _iLogonID, char* _cFilterIP,char* _cFilterMask);
typedef int (__stdcall *pNetClient_SetAlarmOutput)(int _iLogonID, int _iAlarmInput, unsigned long _ulAlarmOutput);
typedef int (__stdcall *pNetClient_GetAlarmOutput)(int _iLogonID, int _iAlarmInput, unsigned long* _ulAlarmOutput);
typedef int (__stdcall *pNetClient_GetAlarmIPortState)(int _iLogonID, int _iInPort,int* _iState);
typedef int (__stdcall *pNetClient_SetAlarmPortEnable)(int _iLogonID,int _iInPort,int _iEnabled);
typedef int (__stdcall *pNetClient_GetAlarmPortEnable)(int _iLogonID,int _iInPort,int* _iEnabled);
typedef int (__stdcall *pNetClient_SetInportAlarmLinkRec)(int _iLogonID,int _iPortNo,int _iEnable);
typedef int (__stdcall *pNetClient_GetInportAlarmLinkRec)(int _iLogonID,int _iPortNo,int* _iEnable);
typedef int (__stdcall *pNetClient_SetInportAlarmLinkSnap)(int _iLogonID,int _iPortNo,int _iEnable);
typedef int (__stdcall *pNetClient_GetInportAlarmLinkSnap)(int _iLogonID,int _iPortNo,int* _iEnable);
typedef int (__stdcall *pNetClient_SetInportAlarmLinkPTZ)(int _iLogonID,int _iPortNo,int _iLinkChannelNum,int _iLinkType,int _iActNum);
typedef int (__stdcall *pNetClient_GetInportAlarmLinkPTZ)(int _iLogonID,int _iPortNo,int _iLinkChannelNum,int* _iLinkType,int* _iActNum);
typedef int (__stdcall *pNetClient_SetInportAlarmLinkSoundDisplay)(int _iLogonID,int _iPortNo,int _iSoundEnable,int _iDisplayEnable);
typedef int (__stdcall *pNetClient_GetInportAlarmLinkSoundDisplay)(int _iLogonID,int _iPortNo,int* _iSoundEnable,int* _iDisplayEnable);
typedef int (__stdcall *pNetClient_SetInportAlmLinkOutport)(int _iLogonID,int _iPortNo,int _iOutportArray);
typedef int (__stdcall *pNetClient_GetInportAlmLinkOutport)(int _iLogonID,int _iPortNo,int* _iOutportArray);
typedef int (__stdcall *pNetClient_SetAlarmInMode)(int _iLogonID, int _iPortNum, int _iLowOrHigh);
typedef int (__stdcall *pNetClient_GetAlarmInMode)(int _iLogonID, int _iPortNum, int* _iLowOrHigh);
typedef int (__stdcall *pNetClient_SetAlarmOutMode)(int _iLogonID, int _iPortNum, int _iLowOrHigh, int _iPulseWidth);
typedef int (__stdcall *pNetClient_GetAlarmOutMode)(int _iLogonID, int _iPortNum, int* _iLowOrHigh, int* _iPulseWidth);
typedef int (__stdcall *pNetClient_SetInportAlarmSchedule)(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_GetInportAlarmSchedule)(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_SetOutportAlarmSchedule)(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_GetOutportAlarmSchedule)(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_SetOutportAlarmDelay)(int _iLogonID,int _iPortNo,int _iClearType,int _iDelayTime);
typedef int (__stdcall *pNetClient_GetOutportAlarmDelay)(int _iLogonID,int _iPortNo,int* _iClearType,int* _iDelayTime);
typedef int (__stdcall *pNetClient_SetInportEnable)(int _iLogonID, int _iPortNo,int _iEnable);
typedef int (__stdcall *pNetClient_GetInportEnable)(int _iLogonID, int _iPortNo,int* _iEnable);
typedef int (__stdcall *pNetClient_SetOutportEnable)(int _iLogonID, int _iPortNo,int _iEnable);
typedef int (__stdcall *pNetClient_GetOutportEnable)(int _iLogonID, int _iPortNo,int* _iEnable);
typedef int (__stdcall *pNetClient_SetOutportState)(int _iLogonID, int _iPort, int _iState);
typedef int (__stdcall *pNetClient_GetOutportState)(int _iLogonID, int _iPort, int* _iState);
typedef int (__stdcall *pNetClient_SetAlmVdoCovThreshold)(int _iLogonID, int _iChannelNum, int _iThreshold);
typedef int (__stdcall *pNetClient_GetAlmVdoCovThreshold)(int _iLogonID, int _iChannelNum, int* _iThreshold);
typedef int (__stdcall *pNetClient_SetAlmVideoCov)(int _iLogonID, int _iChannelNum, int _iEnabled);
typedef int (__stdcall *pNetClient_GetAlmVideoCov)(int _iLogonID, int _iChannelNum, int* _iEnabled);
typedef int (__stdcall *pNetClient_SetDeviceType)(int _iLogonID,int _iChannelNum,int _iComNo,int _iDevAddress,const char* _pcProtocol);
typedef int (__stdcall *pNetClient_GetDeviceType)(int _iLogonID,int _iChannelNum,int* _iComPort,int* _iDevAddress ,char* _cDeviceType);
typedef int (__stdcall *pNetClient_SetComFormat)(int _iLogonID,int _iComPort,char* _cDeviceType,char* _cComFormat,int _iWorkMode);
typedef int (__stdcall *pNetClient_GetComFormat)(int _iLogonID, int _iCom, char* _cComFormat,int* _iWorkMode);
typedef int (__stdcall *pNetClient_GetAllSupportDeviceType)(int _iLogonID, int* _iSumDeviceType, char* _cDeviceType);
typedef int (__stdcall *pNetClient_DeviceCtrl)(int _iLogonID, int _iChannelNum,int _iActionType, int _iParam1,int _iParam2);
typedef int (__stdcall *pNetClient_DeviceCtrlEx)(int _iLogonID,int _iChannelNum,int _iActionType,int _iParam1,int _iParam2,int _iControlType);
typedef int (__stdcall *pNetClient_ComSend)(int _iLogonID, unsigned char* _ucBuf, int _iLength, int _iComNo);
typedef int (__stdcall *pNetClient_DevicePTZCtrl)(int _iLogonID, int _iProtocolType, int _iActionType, int _iComNo, int _iAddress, int _iSpeed, int _iPresetNO);
typedef int (__stdcall *pNetClient_GetComPortCounts)(int _iLogonID, int* _piComPortCounts, int* _piComPortEnabledStatus);
typedef int (__stdcall *pNetClient_SetAlarmVideoLost)(int _iLogonID, int _iChannelNum,int _iEnabled);
typedef int (__stdcall *pNetClient_GetAlarmVideoLost)(int _iLogonID, int _iChannelNum, int* _iEnabled);
typedef int (__stdcall *pNetClient_SetVideoLostLinkPTZ)(int _iLogonID,int _iChannelNum,int _iLinkChannelNum,int _iLinkType,int _iActNum);
typedef int (__stdcall *pNetClient_GetVideoLostLinkPTZ)(int _iLogonID,int _iChannelNum,int _iLinkChannelNum,int* _iLinkType,int* _iActNum);
typedef int (__stdcall *pNetClient_SetVideoLostLinkSoundDisplay)(int _iLogonID,int _iChannelNum,int _iSoundEnable,int _iDisplayEnable );
typedef int (__stdcall *pNetClient_GetVideoLostLinkSoundDisplay)(int _iLogonID,int _iChannelNum,int* _iSoundEnable,int* _iDisplayEnable);
typedef int (__stdcall *pNetClient_GetAlarmVLostState)(int _iLogonID, int _iChannel,int* _iState);
typedef int (__stdcall *pNetClient_SetVideoLostLinkOutport)(int _iLogonID,int _iChannelNum,int _iOutportArray);
typedef int (__stdcall *pNetClient_GetVideoLostLinkOutport)(int _iLogonID,int _iChannelNum,int* _iOutportArray);
typedef int (__stdcall *pNetClient_SetVideoLostSchedule)(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_GetVideoLostSchedule)(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_SetVideoLostLinkRec)(int _iLogonID,int _iChannelNum,int _iEnable);
typedef int (__stdcall *pNetClient_GetVideoLostLinkRec)(int _iLogonID,int _iChannelNum,int* _iEnable);
typedef int (__stdcall *pNetClient_SetVideoLostLinkSnap)(int _iLogonID,int _iChannelNum,int _iEnable);
typedef int (__stdcall *pNetClient_GetVideoLostLinkSnap)(int _iLogonID,int _iChannelNum,int* _iEnable);
typedef int (__stdcall *pNetClient_SetIFrameRate)(int _iLogonID, int _iChannelNum, int _iFrameRate, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetIFrameRate)(int _iLogonID, int _iChannelNum, int* _iFrameRate, int _iStreamNO);
typedef int (__stdcall *pNetClient_ForceIFrame)(int _iLogonID, int _iChannelNum, int _iStreamNO);
typedef int (__stdcall *pNetClient_SetTime)(int _iLogonID, int _iyy, int _imo, int _idd, int _ihh, int _imi, int _iss);
typedef int (__stdcall *pNetClient_Reboot)(int _iLogonID);
typedef int (__stdcall *pNetClient_RebootEx)(int _iLogonID, int _iChannelNo);
typedef int (__stdcall *pNetClient_DefaultPara)(int _iLogonID);
typedef int (__stdcall *pNetClient_DefaultParaEx)(int _iLogonID, int _iType);
typedef int (__stdcall *pNetClient_GetServerVersion)(int _iLogonID, char* _cVer);
typedef int (__stdcall *pNetClient_SetNVS)(int _iLogonID, int _iVideo, int _iCheck);
typedef int (__stdcall *pNetClient_UpgradeProgram)(int _iLogonID, char* _cFileName, PROUPGRADE_NOTIFY _UpgradeNotify);
typedef int (__stdcall *pNetClient_UpgradeWebPage)(int _iLogonID, char* _cFileName, WEBUPGRADE_NOTIFY _UpgradeWebNotify);
typedef int (__stdcall *pNetClient_GetUpgradePercent)(int _iLogonID);
typedef int (__stdcall *pNetClient_GetUserNum)(int _iLogonID,int* _iUserNum);
typedef int (__stdcall *pNetClient_GetUserInfo)(int _iLogonID,int _iUserSerial,char* _cUserName,char* _cPassword,int* _iAuthority);
typedef int (__stdcall *pNetClient_GetCurUserInfo)(int _iLogonID, char _cUserName[16], char _cPassword[16], int* _iAuthority);
typedef int (__stdcall *pNetClient_AddUser)(int _iLogonID, char* _cUserName, char* _cPassword, int _iAuthority);
typedef int (__stdcall *pNetClient_DelUser)(int _iLogonID, char* _cUserName);
typedef int (__stdcall *pNetClient_ModifyPwd)(int _iLogonID, char* _cUserName, char* _cNewPwd);
typedef int (__stdcall *pNetClient_SetMaxConUser)(int _iLogonID, int _iMaxConUser);
typedef int (__stdcall *pNetClient_GetMaxGetUser)(int _iLogonID,int* _iMaxConUser);
typedef int (__stdcall *pNetClient_TalkStart)(int _iLogonID, int _iUser);
typedef int (__stdcall *pNetClient_TalkEnd)(int _iLogonID);
typedef int (__stdcall *pNetClient_InputTalkingdata)(unsigned char* _ucData, unsigned int _iLen);
typedef int (__stdcall *pNetClient_GetTalkingState)(int _iLogonID,int* _iTalkState);
typedef int (__stdcall *pNetClient_CapturePic)(unsigned int _ulConID,unsigned char** _pucData);
typedef int (__stdcall *pNetClient_CaptureBmpPic)(unsigned int _ulConID, char* _pcFileName);
typedef int (__stdcall *pNetClient_ChangeSvrIP)(int _iLogonID, char* _cNewSvrIP, char* _cMask, char* _cGateway, char* _cDNS);
typedef int (__stdcall *pNetClient_GetIpProperty)(int _iLogonID, char* _cMAC, char* _cMask, char* _cGateway, char* _cDNS);
typedef int (__stdcall *pNetClient_SetDHCPParam)(int _iLogonID,int _iDHCP);
typedef int (__stdcall *pNetClient_GetDHCPParam)(int _iLogonID,int* _iDHCP);
typedef int (__stdcall *pNetClient_SetWifiDHCPParam)(int _iLogonID,int _iDHCP);
typedef int (__stdcall *pNetClient_GetWifiDHCPParam)(int _iLogonID,int* _iDHCP);
typedef int (__stdcall *pNetClient_GetVideoCovArea)(int _iLogonID, int _iChannelNum, RECT* _rect, int _iStreamNO);
typedef int (__stdcall *pNetClient_SetVideoCovArea)(int _iLogonID, int _iChannelNum, RECT* _rect, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetVideoSize)(int _iLogonID, int _iChannelNum, int* _width, int * _height, int _iStreamNO);
typedef int (__stdcall *pNetClient_SetVideoSize)(int _iLogonID, int _iChannelNum, int _iVideoSize, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetVideoSizeEx)(int _iLogonID, int _iChannelNum, int* _ivideoSize, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetMaxMinorVideoSize)(int _iLogonID, int* _iMinorVideoSize);
typedef int (__stdcall *pNetClient_BindInterface)(int _interface);
typedef char* (__stdcall *pNetClient_GetNetInterface)(int _interface);
typedef int (__stdcall *pNetClient_SetMaxKByteRate)(int _iLogonID, int _iChannelNum, int _ibitRate, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetMaxKByteRate)(int _iLogonID, int _iChannelNum, int* _ibitRate, int _iStreamNO);
typedef int (__stdcall *pNetClient_WriteUserData)(int _iLogonID,int _iOffset,unsigned char* _u8Buf,int _iLength);
typedef int (__stdcall *pNetClient_ReadUserData)(int _iLogonID,int _iOffset,unsigned char* _u8Buf,int _iLength);
typedef int (__stdcall *pNetClient_SetReducenoiseState)(int _iLogonID, int _iChannelNum,int _iState);
typedef int (__stdcall *pNetClient_GetReducenoiseState)(int _iLogonID,int _iChannelNum,int* _iState);
typedef int (__stdcall *pNetClient_DrawTextOnVideo)(int _iLogonID, int _iChannelNum, int _iX, int _iY, char* _cText, int _iStore, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetTextOnVideo)(int _iLogonID, int _iChannelNum, int* _iX, int* _iY, char* _cText,int _iStreamNO);
typedef int (__stdcall *pNetClient_SetBothStreamSame)(int _iLogonID, int _iChannelNum, int _iState);
typedef int (__stdcall *pNetClient_GetBothStreamSame)(int _iLogonID, int _iChannelNum, int* _iState);
typedef int (__stdcall *pNetClient_ShowBitrateOnVideo)(unsigned int _ulConID, int _iX, int _iY, int _iEnabled);
typedef int (__stdcall *pNetClient_SetPPPoEInfo)(int _iLogonID,char* _cAccount, char* _cPassword, int _iEnabled);
typedef int (__stdcall *pNetClient_GetPPPoEInfo)(int _iLogonID,char* _cAccount, char* _cPassword, int* _iEnabled);
typedef int (__stdcall *pNetClient_CPUCheckEnabled)(int _iEnabled, int _interval);
typedef int (__stdcall *pNetClient_SetEncodeMode)(int _iLogonID, int _iChannelNum, int _iStreamNO, int _iMode);
typedef int (__stdcall *pNetClient_GetEncodeMode)(int _iLogonID, int _iChannelNum, int _iStreamNO, int* _iMode);
typedef int (__stdcall *pNetClient_SetPreferMode)(int _iLogonID, int _iChannelNum, int _iStreamNO, int _iMode);
typedef int (__stdcall *pNetClient_GetPreferMode)(int _iLogonID, int _iChannelNum, int _iStreamNO, int* _iMode);
typedef int (__stdcall *pNetClient_LogFileSetProperty)(int _iLogonID, int _iLevel, int _iSize);
typedef int (__stdcall *pNetClient_LogFileGetProperty)(int _iLogonID, int* _iLevel, int* _iSize);
typedef int (__stdcall *pNetClient_LogFileDownload)(int _iLogonID);
typedef int (__stdcall *pNetClient_LogFileClear)(int _iLogonID);
typedef int (__stdcall *pNetClient_LogFileGetDetails)(int _iLogonID, char* _cBuf, int* _iLen);
typedef int (__stdcall *pNetClient_GetVideoNPMode)(int _iLogonID, VIDEO_NORM* _vMode);
typedef int (__stdcall *pNetClient_SetVideoNPMode)(int _iLogonID, VIDEO_NORM _vMode);
typedef int (__stdcall *pNetClient_SetAudioEncoder)(int _iLogonID, int _iChannel, AUDIO_ENCODER _aCoder);
typedef int (__stdcall *pNetClient_GetAudioEncoder)(int _iLogonID, int _iChannel, AUDIO_ENCODER* _aCoder);
typedef int (__stdcall *pNetClient_NetFileQuery)(int _iLogonID, PNVS_FILE_QUERY _ptFileQuery);
typedef int (__stdcall *pNetClient_NetFileSetRecordRule)(int _iLogonID, int _iRule, int _iTimelen, int _iFreedisk ,int _iFileSize);
typedef int (__stdcall *pNetClient_NetFileGetRecordRule)(int _iLogonID, int* _iRule, int* _iTimelen, int* _iFreedisk,int* _iFileSize);
typedef int (__stdcall *pNetClient_NetFileSetAlarmRule)(int _iLogonID, int _iPreRecordEnable, int _iPreRecordTime, int _iDelayTime ,int _iDelayEnable ,int _iChannelNum);
typedef int (__stdcall *pNetClient_NetFileGetAlarmRule)(int _iLogonID, int* _iPreEnable, int* _iPreTime, int* _iDelayTime ,int* _iDelayEnable,int _iChannelNum);
typedef int (__stdcall *pNetClient_NetFileSetAlarmState)(int _iLogonID, int _iChannel, int _iState);
typedef int (__stdcall *pNetClient_NetFileGetAlarmState)(int _iLogonID, int _iChannel, int* _iState);
typedef int (__stdcall *pNetClient_NetFileSetTaskState)(int _iLogonID, int _iChannel, int _iState);
typedef int (__stdcall *pNetClient_NetFileGetTaskState)(int _iLogonID, int _iChannel, int* _iState);
typedef int (__stdcall *pNetClient_NetFileSetTaskSchedule)(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME* _Schedule[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_NetFileGetTaskSchedule)(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME* _Schedule[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_NetFileSetTaskScheduleEx)(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME_Ex* _Schedule[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_NetFileGetTaskScheduleEx)(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME_Ex* _Schedule[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_NetFileGetFileCount)(int _iLogonID, int* _piTotalCount, int* _piCurrentCount);
typedef int (__stdcall *pNetClient_NetFileRebuildIndexFile)(int _iLogonID, int _iState);
typedef int (__stdcall *pNetClient_NetFileGetDiskInfo)(int _iLogonID, PNVS_STORAGEDEV _storage);
typedef int (__stdcall *pNetClient_NetFileGetDiskInfoEx)(int _iLogonID, PNVS_STORAGEDEV _storage, int _iSize);
typedef int (__stdcall *pNetClient_NetFileIsSupportStorage)(int _iLogonID);
typedef int (__stdcall *pNetClient_NetFileDownloadFile)(unsigned int* _ulID,
								  int _iLogonID, 
								  char* _cRemoteFilename, 
								  char* _cLocalFilename,
								  int _iFlag,
								  int _iPosition,
								  int _Speed);
typedef int (__stdcall *pNetClient_NetFileDownloadFileEx)(unsigned int* _ulConID,
									int _iLogonID, 
									char* _cRemoteFilename, 
									char* _cLocalFilename,
									int _iFlag,
									int _iPosition,
									int _Speed,
									NVSDATA_NOTIFY _cbkDataArrive,
									void* _iUserData);
typedef int (__stdcall *pNetClient_NetFileStopDownloadFile)(unsigned int _ulID);
typedef int (__stdcall *pNetClient_NetFileGetDownloadPos)(unsigned int _ulID, int* _iPos, int* _iDLSize);
typedef int (__stdcall *pNetClient_NetFileMountUSB)(int _iLogonID, int _iState);
typedef int (__stdcall *pNetClient_NetFileGetRecordState)(int _iLogonID, int _iChannel, RECORD_STATE* _piState);
typedef int (__stdcall *pNetClient_NetFileDelFile)(int _iLogonID,const char* _pcFileName);
typedef int (__stdcall *pNetClient_DiskSetUsage)(int _iLogonID,int _iDiskNo,int _iUsage);
typedef int (__stdcall *pNetClient_NetFileGetQueryfile)(int _iLogonID, int _iFileIndex, PNVS_FILE_DATA _fileInfo);
typedef int (__stdcall *pNetClient_DiskFormat)(int _iLogonID,int _iDiskNo,int _iFsType);
typedef int (__stdcall *pNetClient_DiskPart)(int _iLogonID,int _iDiskNo,int _iPartNum,int _iFormatNow);
typedef int (__stdcall *pNetClient_NetFileManualRecord)(int _iLogonID, int _iChannel, int _iState);
typedef int (__stdcall *pNetClient_NetFileMapStoreDevice)(int _iLogonID,PNVS_NFS_DEV _storeDev);
typedef int (__stdcall *pNetClient_NetFileGetMapStoreDevice)(int _iLogonID,PNVS_NFS_DEV _storeDev);
typedef int (__stdcall *pNetClient_NetFileGetUSBstate)(int _iLogonID, int* _iState);
typedef int (__stdcall *pNetClient_NetFileSetExtendname)(int _iLogonID, char* _cExtend);
typedef int (__stdcall *pNetClient_NetFileGetExtendname)(int _iLogonID, char* _cExtend);
typedef int (__stdcall *pNetClient_ClearDisk)(int _iLogonID,int _iDiskNo);
typedef int (__stdcall *pNetClient_GetDownloadFailedFileName)(int _iLogonID, int _iFileID, char* _pcFileName, int _iFileNameBufSize);
typedef int (__stdcall *pNetClient_SetMediaStreamClient)(int _iLogonID, int _iChannel, char* _cClientIP, unsigned short _iClientPort, int _iStreamType);
typedef int (__stdcall *pNetClient_GetMediaStreamClient)(int _iLogonID, int _iChannel, char* _cClientIP, unsigned short* _iClientPort, int* _iStreamType);
typedef int (__stdcall *pNetClient_SetEmailAlarm)(int _iLogonID, PSMTP_INFO _pSmtp);
typedef int (__stdcall *pNetClient_GetEmailAlarm)(int _iLogonID, PSMTP_INFO _pSmtp);
typedef int (__stdcall *pNetClient_SetEmailAlarmEnable)(int _iLogonID, int _iChannel, int _iEnable);
typedef int (__stdcall *pNetClient_GetEmailAlarmEnable)(int _iLogonID, int _iChannel, int* _iEnable);
typedef int (__stdcall *pNetClient_SetScene)(int _iLogonID, int _iChannel, int _iScene);
typedef int (__stdcall *pNetClient_GetScene)(int _iLogonID, int _iChannel, int* _iScene);
typedef int (__stdcall *pNetClient_SetSensorFlip)(int _iLogonID, int _iChannel, int _iFlip);
typedef int (__stdcall *pNetClient_GetSensorFlip)(int _iLogonID, int _iChannel, int* _iFlip);
typedef int (__stdcall *pNetClient_SetSensorMirror)(int _iLogonID, int _iChannel, int _iMirror);
typedef int (__stdcall *pNetClient_GetSensorMirror)(int _iLogonID, int _iChannel, int* _iMirror);
typedef int (__stdcall *pNetClient_Snapshot)(int _iLogonID, int _iChannel, int _iQvalue);
typedef int (__stdcall *pNetClient_GetFactoryID)(int _iLogonID, char* _cFactoryID);
typedef int (__stdcall *pNetClient_SetWifiParam)(int _iLogonID, NVS_WIFI_PARAM* _pWifiParam);
typedef int (__stdcall *pNetClient_GetWifiParam)(int _iLogonID, NVS_WIFI_PARAM* _pWifiParam);
typedef int (__stdcall *pNetClient_WifiSearch)(int _iLogonID);
typedef int (__stdcall *pNetClient_GetWifiSearchResult)(int _iLogonID, WIFI_INFO** _pWifiInfo, int* _iWifiCount);
typedef int (__stdcall *pNetClient_SetPrivacyProtect)(int _iLogonID,int _iChannelNum,int _iEnabled);
typedef int (__stdcall *pNetClient_GetPrivacyProtect)(int _iLogonID,int _iChannelNum,int* _iEnabled);
typedef int (__stdcall *pNetClient_IYUVtoYV12)(int _iWidth, int _iHeight, unsigned char* _YUV420);
typedef int (__stdcall *pNetClient_GetDevType)(int _iLogonID,int* _iDevType);
typedef int (__stdcall *pNetClient_GetProductType)(int _iLogonID,int* _iType);
typedef int (__stdcall *pNetClient_GetProductTypeEx)(int _iLogonID, int* _piProductMode, int* _piProductType);
typedef int (__stdcall *pNetClient_BackupKernel)(int _iLogonID);
typedef int (__stdcall *pNetClient_SetUPNPEnable)(int _iLogonID,int _iEnable);
typedef int (__stdcall *pNetClient_GetUPNPEnable)(int _iLogonID,int* _iEnable);
typedef int (__stdcall *pNetClient_GetSysInfo)(int _iLogonID);
typedef int (__stdcall *pNetClient_SetDDNSPara)(int _iLogonID,char* _cDUserName,char* _cDPassword,char* _cDNvsName, char* _cDomain,int _iPort,int _iDEnable);
typedef int (__stdcall *pNetClient_GetDDNSPara)(int _iLogonID,char* _cDUserName,char* _cDPassword,char* _cDNvsName, char* _cDomain,int* _iPort,int* _iDEnable);
typedef int (__stdcall *pNetClient_SetFuncListArray)(int _iLogonID,int _iEnableArray);
typedef int (__stdcall *pNetClient_GetFuncListArray)(int _iLogonID,int* _iEnableArray);
typedef int (__stdcall *pNetClient_SendStringToServer)(int _iLogonID, char* _cMsg, int _iLen);
typedef int (__stdcall *pNetClient_ReceiveString)(char* _cIpAddress,int* _iType,char* _cMsg,int* _iLen);
typedef int (__stdcall *pNetClient_SendStringToCenter)(char* _cIpAddress, int _iServerPort,char* _cMsg, int _iLen);
typedef int (__stdcall *pNetClient_SetVencType)(int _iLogonID,int _iChannelNum,int _iType);
typedef int (__stdcall *pNetClient_GetVencType)(int _iLogonID,int _iChannelNum,int* _iType);
typedef int (__stdcall *pNetClient_SetComServer)(int _iLogonID,char* _cComServer,int _iComServerPort);
typedef int (__stdcall *pNetClient_GetComServer)(int _iLogonID,char* _cComServer,int* _iComServerPort);
typedef int (__stdcall *pNetClient_Get3GDeviceStatus)(int _iLogonID, int* _i3GDeviceType,int* _iStatus,int* _iIntensity,char* _pcIP,char* _pcStarttime);
typedef int (__stdcall *pNetClient_Set3GDialog)(int _iLogonID, int _iStartType,int _iStopType,int _iDuration);
typedef int (__stdcall *pNetClient_Get3GDialog)(int _iLogonID, int* _iStartType,int* _iStopType,int* _iDuration);
typedef int (__stdcall *pNetClient_Set3GMessage)(int _iLogonID, char* _cNotify,char* _cPhoneNum1,char* _cPhoneNum2,char* _cPhoneNum3,char* _cPhoneNum4,char* _cPhoneNum5);
typedef int (__stdcall *pNetClient_Get3GMessage)(int _iLogonID, char* _cNotify,char* _cPhoneNum1,char* _cPhoneNum2,char* _cPhoneNum3,char* _cPhoneNum4,char* _cPhoneNum5);
typedef int (__stdcall *pNetClient_Set3GTaskSchedule)(int _iLogonID, int _iEnable, PNVS_SCHEDTIME _strScheduleParam);
typedef int (__stdcall *pNetClient_Get3GTaskSchedule)(int _iLogonID, int* _iEnable, PNVS_SCHEDTIME _strScheduleParam);
typedef int (__stdcall *pNetClient_Set3GNotify)(int _iLogonID, int _iType,char* _cMessage);
typedef int (__stdcall *pNetClient_Get3GNotify)(int _iLogonID, int* _iType,char* _cMessage);
typedef int (__stdcall *pNetClient_SetHDCamer)(int _iLogonID, int _iChannel,int _iFuncNum,int _iValue);
typedef int (__stdcall *pNetClient_GetHDCamer)(int _iLogonID, int _iChannel,int _iFuncNum,int* _iValue);
typedef int (__stdcall *pNetClient_SetAlarmServer)(int _iLogonID,const char* _pcAlarmServer,int _iServerPort);
typedef int (__stdcall *pNetClient_GetAlarmServer)(int _iLogonID,char* _pcAlarmServer,int* _iServerPort);
typedef int (__stdcall *pNetClient_InterTalkStart)(unsigned int * _uiConnID, int _iLogonID, int _iUserData );
typedef int (__stdcall *pNetClient_InterTalkEnd)(unsigned int _uiConnID, bool _blStopTalk);
typedef int (__stdcall *pNetClient_NetFileQueryEx)(int _iLogonID, PNVS_FILE_QUERY _pfileQuery, int _iSizeOfQuery);
typedef int (__stdcall *pNetClient_ControlDeviceRecord)(int _iLogonID, int _iChannelID, int _iRecordType, int _iAction);
typedef int (__stdcall *pNetClient_NetFileDownloadByTimeSpan)(unsigned int* _ulConID, int _iLogonID, char* _pcLocalFile, 
					int _iChannelNO, unsigned int _uiFromSecond, unsigned int _uiToSecond, 
                                                  int _iFlag, int _iPosition,int _iSpeed);
typedef int (__stdcall *pNetClient_NetFileDownloadByTimeSpanEx)( unsigned int* _ulConID, int _iLogonID, char* _pcLocalFile, 
					int _iChannelNO, NVS_FILE_TIME* _pTimeBegin, NVS_FILE_TIME* _pTimeEnd, 
                                                     int _iFlag, int _iPosition,int _iSpeed);
typedef int (__stdcall *pNetClient_NetFileDownloadByTimeSpanCallBack)( unsigned int* _ulConID, 
                                                 int _iLogonID, 
												 char* _pcLocalFile,
												 int _iChannelNO,
                                                 NVS_FILE_TIME* _pTimeBegin, 
                                                 NVS_FILE_TIME* _pTimeEnd, 
												 NVSDATA_NOTIFY _cbkDataArrive,
                                                 int _iFlag, 
                                                 int _iPosition,
                                                 int _iSpeed,
                                                 void* _iUserData);
typedef int (__stdcall *pNetClient_NetLogQuery)(int _iLogonID, PNVS_LOG_QUERY _logQuery);
typedef int (__stdcall *pNetClient_NetLogGetLogfile)(int _iLogonID, int _iLogIndex, PNVS_LOG_DATA _pLogInfo);
typedef int (__stdcall *pNetClient_NetLogGetLogCount)(int _iLogonID, int* _iTotalCount, int* _iCurrentCount);
typedef int (__stdcall *pNetClient_GetProtocolList)(int _iLogonID, st_NVSProtocol* _pstNVSProtocol);
typedef int (__stdcall *pNetClient_SetCHNPTZCRUISE)(int _iLogonID,int _iChannelNum,int _iCruiseNo,int _iEnable,int _iCruiseNum,st_PTZCruise* _stPTZCruise);
typedef int (__stdcall *pNetClient_GetCHNPTZCRUISE)(int _iLogonID,int _iChannelNum,int _iCruiseNo,int* _iEnable,int* _iCruiseNum,st_PTZCruise* _stPTZCruise);
typedef int (__stdcall *pNetClient_SetVIDEOCOVER_LINKRECORD)(int _iLogonID,int _iChannelNum,int _iEnableByBits);
typedef int (__stdcall *pNetClient_GetVIDEOCOVER_LINKRECORD)(int _iLogonID,int _iChannelNum,int* _iEnableByBits);
typedef int (__stdcall *pNetClient_SetVIDEOCOVER_LINKPTZ)(int _iLogonID,int _iChannelNum,int _iLinkChannel,int _iLinkType,int _iNo);
typedef int (__stdcall *pNetClient_GetVIDEOCOVER_LINKPTZ)(int _iLogonID,int _iChannelNum,int _iLinkChannel,int* _iLinkType,int* _iNo);
typedef int (__stdcall *pNetClient_GetAlarmVCoverState)(int _iLogonID, int _iChannel,int* _iState);
typedef int (__stdcall *pNetClient_StopCaptureDate)(unsigned long _ulID);
typedef int (__stdcall *pNetClient_SetColorToGray)(int _iLogonID,int _iChannelNum, int _iEnable);
typedef int (__stdcall *pNetClient_GetColorToGray)(int _iLogonID,int _iChannelNum, int* _iEnable);
typedef int (__stdcall *pNetClient_SetCustomChannelName)(int _iLogonID,int _iChannelNum,int _iChannelType, char* _cChannelName);
typedef int (__stdcall *pNetClient_GetCustomChannelName)(int _iLogonID,int _iChannelNum,int _iChannelType, char* _cChannelName);
typedef int (__stdcall *pNetClient_SetCustomRecType)(int _iLogonID,int _iRecType, char* _cRecTypeName);
typedef int (__stdcall *pNetClient_GetCustomRecType)(int _iLogonID,int _iRecType, char* _cRecTypeName);
typedef int (__stdcall *pNetClient_ChangeSvrIPEx)(int _iLogonID, char* _cNewSvrIP, char* _cMask, char* _cGateway, char* _cDNS, char* _cBackDNS);
typedef int (__stdcall *pNetClient_GetIpPropertyEx)(int _iLogonID,char* _cMAC, char* _cMask, char* _cGateway, char* _cDNS, char* _cBackDNS);
typedef int (__stdcall *pNetClient_SetFTPUpdate)(int _iLogonID,char* _cFtpAddr,char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword);
typedef int (__stdcall *pNetClient_GetFTPUpdate)(int _iLogonID,char* _cFtpAddr,char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword);
typedef int (__stdcall *pNetClient_SetCHNPTZFormat)(int _iLogonID,int _iChannelNum,char* _cComFormat);
typedef int (__stdcall *pNetClient_GetCHNPTZFormat)(int _iLogonID,int _iChannelNum,char* _cComFormat);
typedef int (__stdcall *pNetClient_GetServerVersionEx)(int _iLogonID, char* _cVer,char* _cUIVersion,char* _cSlaveVersion);
typedef int (__stdcall *pNetClient_GetOSDTypeColor)(int _iLogonID, int _iChannelNum, int _iOSDType, int* _iColor);
typedef int (__stdcall *pNetClient_SetOSDTypeColor)(int _iLogonID, int _iChannelNum, int _iOSDType, int _iColor);
typedef int (__stdcall *pNetClient_GetExceptionMsg)(int _iLogonID, int _iExceptionTyep, int* _iState);
typedef int (__stdcall *pNetClient_SetNTPInfo)(int _iLogonID, char* _NTPServer, unsigned short _iPort, int _iInterval);
typedef int (__stdcall *pNetClient_GetNTPInfo)(int _iLogonID, char* _NTPServer, unsigned short* _iPort, int* _iInterval);
typedef int (__stdcall *pNetClient_SetVideoEncrypt)(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetVideoEncrypt)(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetVideoDecrypt)(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetVideoDecrypt)(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetPreRecEnable)(int _iLogonID, int _iChannel, int _iEnable);
typedef int (__stdcall *pNetClient_GetPreRecEnable)(int _iLogonID, int _iChannel, int* _piEnable);
typedef int (__stdcall *pNetClient_SetVideoCombine)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetVideoCombine)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_VCASetConfig)(int _iLogonID, int _iVCACmdID, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen);
typedef int (__stdcall *pNetClient_VCAGetConfig)(int _iLogonID, int _iVCACmdID, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen);
typedef int (__stdcall *pNetClient_VCARestart)(int _iLogonID);
typedef int (__stdcall *pNetClient_VCARestartEx)(int _iLogonID, int _iChannelNO);
typedef int (__stdcall *pNetClient_VCAGetAlarmInfo)(int _iLogonID, int _iAlarmIndex, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetEmailAlarmEx)(int _iLogonID, PSMTP_INFO _pSmtp, int _iSize);
typedef int (__stdcall *pNetClient_GetEmailAlarmEx)(int _iLogonID, PSMTP_INFO _pSmtp, int _iSize);
typedef int (__stdcall *pNetClient_SetFTPUploadConfig)(int _iLogonID, int _iCmd, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetFTPUploadConfig)(int _iLogonID, int _iCmd, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_Set3GConfig)(int _iLogonID, int _iCmd, int _iChannel, void* _lpValueBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_Get3GConfig)(int _iLogonID, int _iCmd, int _iChannel, void* _lpValueBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetDigitalChannelConfig)(int _iLogonID, int _iChannel, int _iCmd, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetDigitalChannelConfig)(int _iLogonID, int _iChannel, int _iCmd, void* _lpCmdBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_DigitalChannelSend)(int _iLogonID, int _iChannel, unsigned char* _ucBuf, int _iLength);
typedef int (__stdcall *pNetClient_SendComData)(int _iLogonID, int _iCommand, void* _lpInBuffer, int _iInBufferSize);
typedef int (__stdcall *pNetClient_SetVideoNPModeEx)(int _iLogonID, int _iChannel, VIDEO_NORM _iNPMode);
typedef int (__stdcall *pNetClient_GetVideoNPModeEx)(int _iLogonID, int _iChannel, VIDEO_NORM* _piNPMode);
typedef int (__stdcall *pNetClient_GetDigitalChannelNum)(int _iLogonID, int* _piDigitChannelNum);
typedef int (__stdcall *pNetClient_GetChannelProperty)(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetDeviceTimerReboot)(int _iLogonID, int _iEnable, int _iInterval, int _iHour);
typedef int (__stdcall *pNetClient_GetDeviceTimerReboot)(int _iLogonID, int* _iEnable, int* _iInterval, int* _iHour);
typedef int (__stdcall *pNetClient_SetVideoCoverSchedule)(int _iLogonID, int _iChannel, int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_GetVideoCoverSchedule)(int _iLogonID, int _iChannel, int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT]);
typedef int (__stdcall *pNetClient_SetCPUMEMAlarmThreshold)(int _iLogonID, int _iCPUThreshold, int _iMEMThreshold);
typedef int (__stdcall *pNetClient_GetCPUMEMAlarmThreshold)(int _iLogonID, int* _iCPUThreshold, int* _iMEMThreshold);
typedef int (__stdcall *pNetClient_SetDZInfo)(int _iLogonID, DZ_INFO_PARAM* _pDZ_INFO);
typedef int (__stdcall *pNetClient_GetDZInfo)(int _iLogonID, DZ_INFO_PARAM* _pDZ_INFO);
typedef int (__stdcall *pNetClient_SetPTZAutoBack)(int _iLogonID, int _iChannel,int _iEnable,int _iPresetIndex,int _iIdleTime);
typedef int (__stdcall *pNetClient_GetPTZAutoBack)(int _iLogonID, int _iChannel,int* _iEnable,int* _iPresetIndex,int* _iIdleTime);
typedef int (__stdcall *pNetClient_Set3GVPND)(int _iLogonID, char* _cDialNumber,char* _cAccount,char* _cPassword);
typedef int (__stdcall *pNetClient_Get3GVPND)(int _iLogonID, char* _cDialNumber,char* _cAccount,char* _cPassword);
typedef int (__stdcall *pNetClient_SetHDCamerEx)(int _iLogonID, int _iChannel, int _iFuncID, void* _lpBuf, int _iSize);
typedef int (__stdcall *pNetClient_GetHDCamerEx)(int _iLogonID, int _iChannel, int _iFuncID, void* _lpBuf, int _iSize);
typedef int (__stdcall *pNetClient_SetFTPUsage)(int _iLogonID, char* _cFtpAddr, int _iPort, char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword,int _iUsage);
typedef int (__stdcall *pNetClient_GetFTPUsage)(int _iLogonID, char* _cFtpAddr, int* _piPort, char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword,int* _piUsage);
typedef int (__stdcall *pNetClient_SetChannelSipConfig)(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetChannelSipConfig)(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetMaxVideoSize)(int _iLogonID, int* _iVideoSize);
typedef int (__stdcall *pNetClient_SetBitRatePercent)(int _iLogonID, int _iChannelNO, int _iPercent);
typedef int (__stdcall *pNetClient_GetBitRatePercent)(int _iLogonID, int _iChannelNO, int* _piPercent);
typedef int (__stdcall *pNetClient_GetVideoParam)(unsigned int _uiConnID, int* _piWidth, int* _piHeight, int* _piFrameRate);
typedef int (__stdcall *pNetClient_SetOSDAlpha)(int _iLogonID, int _iChannel,int _iAlpha);
typedef int (__stdcall *pNetClient_GetOSDAlpha)(int _iLogonID, int _iChannel,int* _iAlpha);
typedef int (__stdcall *pNetClient_DeviceSetup)(int _iLogonID, int _iFlag,const char* _pcSection,const char* _pcKeyword,const char* _pcValue);
typedef int (__stdcall *pNetClient_SetPlayerShowFrameMode)(int _iLogonID,int _iChannelNum, unsigned int _uiShowFrameMode, int _iStreamNO);
typedef int (__stdcall *pNetClient_GetPlayerShowFrameMode)(int _iLogonID,int _iChannelNum,int _iStreamNO);
typedef int (__stdcall *pNetClient_DrawRectOnLocalVideo)(unsigned int _uiConID, RECT* _rcRect, int _iCount);
typedef int (__stdcall *pNetClient_DrawPolyOnLocalVideo)(unsigned int _uiConnID, POINT* _pPointArray, int _iPointCount, int _iFlag);
typedef int (__stdcall *pNetClient_SendStringToServerEx)(int _iLogonID,char* _cMsg,int _iLen, int _iFlag);
typedef int (__stdcall *pNetClient_SetNetFileDownloadFileCallBack)(unsigned int _ulConID, RECV_DOWNLOADDATA_NOTIFY _cbkDataNotify, void* _lpUserData);
typedef int (__stdcall *pNetClient_SetDataPackCallBack)(unsigned int _ulConID, int _iCBType, void* _pvCallBack, void* _pvUserData);
typedef int (__stdcall *pNetClient_AddConnectionToNetWork)(int _iSocket, void* _Connection, void* _NotifyFun);
typedef void* (__stdcall *pNetClient_MallocConnection)();
typedef int (__stdcall *pNetClient_FreeConnection)(void* _pConnect);
typedef int (__stdcall *pNetClient_NetFileSetChannelParam)(int _iLogonID,int _iChannelNo,int _iCmd, void* _lpBuf);
typedef int (__stdcall *pNetClient_NetFileGetChannelParam)(int _iLogonID,int _iChannelNo,int _iCmd, void* _lpBuf);
typedef int (__stdcall *pNetClient_ShutDownDev)(int _iLogonID, int _iFlag);
typedef int (__stdcall *pNetClient_BackupImage)(int _iLogonID , int _iType);
typedef int (__stdcall *pNetClient_SetLanParam)(int _iLogonID, int _iCmd, void* _lpData);
typedef int (__stdcall *pNetClient_GetLanParam)(int _iLogonID, int _iCmd, void* _lpData);
typedef int (__stdcall *pNetClient_GetVideoSzList)( int _iLogonID, int _iChannel, int _iStreamNo, int* _piList, int*  _piLstCount );
typedef int (__stdcall *pNetClient_SetAlarmConfig)( int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, void* _pvCmdBuf);
typedef int (__stdcall *pNetClient_GetAlarmConfig)(int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, void* _pvCmdBuf);
typedef int (__stdcall *pNetClient_SetITSBlock)(int _iLogonID, int _iBlockID, int _iX, int _iY);
typedef int (__stdcall *pNetClient_GetITSBlock)(int _iLogonID, int _iBlockID, int* _iX, int* _iY);
typedef int (__stdcall *pNetClient_SetHDTimeRangeParam)(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetHDTimeRangeParam)(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetHDTemplateName)(int _iLogonID, int _iTemplateID, char* _cTemplateName);
typedef int (__stdcall *pNetClient_GetHDTemplateName)(int _iLogonID, int _iTemplateID,char* _cTemplateName);
typedef int (__stdcall *pNetClient_SetHDTemplateMap)(int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetHDTemplateMap)(int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetITSTimeRangeEnable)(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSTimeRangeEnable)(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetITSTimeRange)(int _iLogonID, int _iIndex, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSTimeRange)(int _iLogonID, int _iIndex, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetITSDetectMode)(int _iLogonID, int _iMode);
typedef int (__stdcall *pNetClient_GetITSDetectMode)(int _iLogonID, int* _iMode);
typedef int (__stdcall *pNetClient_SetITSLoopMode)(int _iLogonID, int _iLoopMode);
typedef int (__stdcall *pNetClient_GetITSLoopMode)(int _iLogonID, int* _iLoopMode);
typedef int (__stdcall *pNetClient_SetITSDeviceType)(int _iLogonID, int _iDeviceType);
typedef int (__stdcall *pNetClient_GetITSDeviceType)(int _iLogonID, int* _iDeviceType);
typedef int (__stdcall *pNetClient_SetITSRoadwayParam)(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSRoadwayParam)(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetITSLicensePlateOptimize)(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSLicensePlateOptimize)(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetITSExtraInfo)(int _iLogonID, int _iCmd, int _iChannel, void* _pvCmdBuf, int _iCmdLen);
typedef int (__stdcall *pNetClient_GetITSExtraInfo)(int _iLogonID, int _iCmd, int _iChannel, void* _pvCmdBuf, int _iCmdLen);
typedef int (__stdcall *pNetClient_CheckDeviceState)(int _iLogonID, int _iChannelNo, int _iType);
typedef int (__stdcall *pNetClient_GetDeviceState)(int _iLogonID, int _iChannelNo, int _iType, int* _iValue);
typedef int (__stdcall *pNetClient_GetCameraCheckInfo)(int _iLogonID, int _iChannelNo, int _iType, int* _iValue);
typedef int (__stdcall *pNetClient_CheckCamera)(int _iLogonID, int _iChannelNo, int _iType, int _iEnable);
typedef int (__stdcall *pNetClient_GetCharSet)(int _iLogonID, char* _pcCharSet);
typedef int (__stdcall *pNetClient_SetTimeZone)(int _iLogonID, int _iTimeZone);
typedef int (__stdcall *pNetClient_GetTimeZone)(int _iLogonID, int* _iTimeZone);
typedef int (__stdcall *pNetClient_SetCurLanguage)(int _iLogonID, char* _pcLanguage);
typedef int (__stdcall *pNetClient_GetCurLanguage)(int _iLogonID, char* _pcLanguage);
typedef int (__stdcall *pNetClient_GetLanguageList)(int _iLogonID, st_NVSLanguageList* _pStrctLanguageList);
typedef int (__stdcall *pNetClient_SetChannelEncodeProfile)(int _iLogonID, int _iChannelNum, int _iStreamNo, int _iLevel);
typedef int (__stdcall *pNetClient_GetChannelEncodeProfile)(int _iLogonID, int _iChannelNum, int _iStreamNo, int* _piLevel);
typedef int (__stdcall *pNetClient_SetAlarmClear)(int _iLogonID, int _iChannelNo, int _iClearType);
typedef int (__stdcall *pNetClient_SetExceptionHandleParam)( int _iLogonID, int _iExceptionType, int _iFlag );
typedef int (__stdcall *pNetClient_GetExceptionHandleParam)( int _iLogonID, int _iExceptionType, int* _iFlag);
typedef int (__stdcall *pNetClient_SetAlarmLink_V1)(int _iLogonID, int _iChannelNo, int _iAlarmLinkType, void * _pParam, int _iParamSize );
typedef int (__stdcall *pNetClient_GetAlarmLink_V1)(int _iLogonID, int _iChannelNo, int _iAlarmLinkType, void * _pParam, int _iParamSize );
typedef int (__stdcall *pNetClient_SetCameraParam)(int _iLogonID, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetCameraParam)(int _iLogonID, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetColorParam)(int _iLogonID,int _iChannelNum, int _iColorToGradEnable, int _iDayRange, int _iNightRange);
typedef int (__stdcall *pNetClient_GetColorParam)(int _iLogonID,int _iChannelNum, int* _iColorToGradEnable,  int* _iDayRange, int* _iNightRange);
typedef void* (__stdcall *pNetClient_InnerMallocBlock)(int _iNeedSize);
typedef int (__stdcall *pNetClient_InnerFreeBlock)(void * _pBlock);
typedef int (__stdcall *pNetClient_InnerReferBlock)(void * _pBlock);
typedef int (__stdcall *pNetClient_InnerReleaseBlock)(void* _pBlock);
typedef int (__stdcall *pNetClient_SetJPEGQuality)(int _iLogonID, int _iJpegQuality);
typedef int (__stdcall *pNetClient_GetJPEGQuality)(int _iLogonID, int* _iJpegQuality);
typedef int (__stdcall *pNetClient_GetConnectInfo)(int _iLogonID, void* _lpBuf, int _iSize);
typedef int (__stdcall *pNetClient_SetPlatformApp)(int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetPlatformApp)(int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetManagerServersInfo)(int _iLogonID, char* _cRegSvrIP, unsigned short _wdRegPort,
            char* _cHeartSvrIP, unsigned short _wdHeartPort, char* _cAlarmSvrIP, unsigned short _wdAlarmPort);
typedef int (__stdcall *pNetClient_GetManagerServersInfo)(int _iLogonID, char* _cRegSvrIP, unsigned short* _wdRegPort,
            char* _cHeartSvrIP, unsigned short* _wdHeartPort, char* _cAlarmSvrIP, unsigned short* _wdAlarmPort);
typedef int (__stdcall *pNetClient_SetDeviceID)(int _iLogonID, char* _cDeviceID, char* _cDeviceName, unsigned short _iVspPort, char* _cAccessPass, unsigned short _iVapPort);
typedef int (__stdcall *pNetClient_GetDeviceID)(int _iLogonID, char* _cDeviceID, char* _cDeviceName, unsigned short* _iVspPort, char* _cAccessPass, unsigned short* _iVapPort);
typedef int (__stdcall *pNetClient_SetATMConfig)(int _iLogonID,int _iCmd, void* _lpBuf);
typedef int (__stdcall *pNetClient_GetATMConfig)(int _iLogonID,int _iCmd, void* _lpBuf);
typedef int (__stdcall *pNetClient_ATMQueryFile)(int _iLogonID, int _iCmd, void* _lpBuf);
typedef int (__stdcall *pNetClient_ATMGetQueryFile)(int _iLogonID, int _iFileIndex, ATM_FILE_DATA* _pFileData);
typedef int (__stdcall *pNetClient_SetAudioSample)(int _iLogonID, int _iChannel,int _iCmd, void* _lpBuf);
typedef int (__stdcall *pNetClient_GetAudioSample)(int _iLogonID, int _iChannel,int _iCmd, void* _lpBuf);
typedef int (__stdcall *pNetClient_SetSystemTypeEx)( int _iLogonID, int _iCmd, void* _pvCmdBuf);
typedef int (__stdcall *pNetClient_GetSystemTypeEx)( int _iLogonID, int _iCmd, void* _pvCmdBuf);
typedef int (__stdcall *pNetClient_SetHXListenPortInfo)(int _iLogonID,int _iVideoPort,int _iTalkPort,int _iMsgPort);
typedef int (__stdcall *pNetClient_GetHXListenPortInfo)(int _iLogonID,int* _iVideoPort,int* _iTalkPort,int* _iMsgPort);
typedef int (__stdcall *pNetClient_SetVideoModeMethod)(int _iLogonID, int _iChannel, VIDEO_METHOD  _iMethod);
typedef int (__stdcall *pNetClient_GetVideoModeMethod)(int _iLogonID, int _iChannel, VIDEO_METHOD * _piMethod);
typedef int (__stdcall *pNetClient_GetMonitorNum)();
typedef int (__stdcall *pNetClient_GetMonitorInfo)(unsigned int _uiIndex,MONITOR_INFO* _MonitorInfo);
typedef int (__stdcall *pNetClient_ChangeMonitor)(int _iLogonID,int _iChannelNum,unsigned int _uiIndex, int _iStreamNO);
typedef int (__stdcall *pNetClient_EZoomAdd)(unsigned int _uiConnID, int _hWnd, RECT _rctDisplay, unsigned int _uiMonitorIndex);
typedef int (__stdcall *pNetClient_EZoomSet)(unsigned int _uiConnID, int _iEZoomID, RECT _rctVideo);
typedef int (__stdcall *pNetClient_EZoomReset)(unsigned int _uiConnID, int _iEZoomID);
typedef int (__stdcall *pNetClient_EZoomRemove)(unsigned int _uiConnID, int _iEZoomID);
typedef int (__stdcall *pNetClient_DCardStartPlay)(unsigned int _ulConID, int _iCardChannel, int _iPos);
typedef int (__stdcall *pNetClient_DCardStopPlay)(unsigned int _ulConID, int _iFlag);
typedef int (__stdcall *pNetClient_DCardRelease)();
typedef int (__stdcall *pNetClient_DCardReInit)(int _iCardChannel);
typedef int (__stdcall *pNetClient_DCardGetState)(int _iCardChannel, int* _iState);
typedef int (__stdcall *pNetClient_DCardStartPlayEx)(int* _iId, DecoderParam* _dParam);
typedef int (__stdcall *pNetClient_DCardPutDataEx)(int _iId, char* _cBuf, int _iLen);
typedef int (__stdcall *pNetClient_DCardStopPlayEx)(int _iId, int _iFlag);
typedef int (__stdcall *pNetClient_DCardStartPlayAudio)(unsigned int _ulConID);
typedef int (__stdcall *pNetClient_SetEncryptSN)( int _iLogonID,int _iType,char* _cSN);
typedef int (__stdcall *pNetClient_GetSNReg)( int _iLogonID,int* _piLockRet);
typedef int (__stdcall *pNetClient_GetComFormat_V1)(int _iLogonID, int _iComPort,char* _cDeviceType,char* _cCommFormat,int* _iWorkMode);
typedef int (__stdcall *pNetClient_SetComFormat_V2)(int _iLogonID, COMFORMAT* _pComFormat);
typedef int (__stdcall *pNetClient_GetComFormat_V2)(int _iLogonID, COMFORMAT* _pComFormat);
typedef int (__stdcall *pNetClient_GetServerVersion_V1)(int _iLogonID, SERVER_VERSION* _pstrctServerVer);
typedef int (__stdcall *pNetClient_InputTalkingdataEx)(int _iLogonID, unsigned char* _ucData, unsigned int _iLen);
typedef int (__stdcall *pNetClient_SetVerticalSync)(unsigned int _ulConID,int _iFlag);
typedef int (__stdcall *pNetClient_GetVerticalSync)(unsigned int _ulConID,int* _piFlag);
typedef int (__stdcall *pNetClient_SetLocalAudioVolumeEx)(unsigned int _ulConID, int _iVolume);
typedef int (__stdcall *pNetClient_GetLocalAudioVolumeEx)(unsigned int _ulConID, int* _iVolume);
typedef int (__stdcall *pNetClient_ClearPolyLocalVideo)(unsigned int _uiConnID, int _iPolygonIndex);
typedef int (__stdcall *pNetClient_SetOSDTypeFontSize)(int _iLogonID,int _iChannelNum,int _iOSDType, int _iSize);
typedef int (__stdcall *pNetClient_GetOSDTypeFontSize)(int _iLogonID,int _iChannelNum,int _iOSDType, int* _iSize);
typedef int (__stdcall *pNetClient_SetImgDisposal)( int _iLogonID,void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetImgDisposal)( int _iLogonID,void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetMuted)(unsigned int _uiConID, int _iMuted);
typedef int (__stdcall *pNetClient_SetPWMValue)(int _iLogonID, int _iChannelNo, int _iValue);
typedef int (__stdcall *pNetClient_GetPWMValue)(int _iLogonID, int _iChannelNo, int* _piValue);
typedef int (__stdcall *pNetClient_SetSystemType)( int _iLogonID,int _iType);
typedef int (__stdcall *pNetClient_GetSystemType)( int _iLogonID,int* _piType);
typedef int (__stdcall *pNetClient_SetITSSwitchTime)(int _iLogonID, int _iSwitchTime, int _iDelayTime);
typedef int (__stdcall *pNetClient_GetITSSwitchTime)(int _iLogonID, int* _iSwitchTime, int* _iDelayTime);
typedef int (__stdcall *pNetClient_SetITSRecoParam)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSRecoParam)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetITSDayNight)(int _iLogonID, int _iTimeSegment);
typedef int (__stdcall *pNetClient_GetITSDayNight)(int _iLogonID, int* _iTimeSegment);
typedef int (__stdcall *pNetClient_SetITSCamLocation)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSCamLocation)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetITSWorkMode)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSWorkMode)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetWaterMarkEnable)( int _iLogonID,int _iChannelID,int _iEnable);
typedef int (__stdcall *pNetClient_GetWaterMarkEnable)( int _iLogonID,int _iChannelID,int* _piEnable);
typedef int (__stdcall *pNetClient_SetITSLightInfo)(int _iLogonID, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetITSLightInfo)(int _iLogonID, void * _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetHardWareParam)( int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetHardWareParam)( int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetDomeAdvParam)( int _iLogonID, int _iChannelNO, int _iCmd, void* _pvCmdBuf, int _iCmdBufLen);
typedef int (__stdcall *pNetClient_GetDomeAdvParam)( int _iLogonID, int _iChannelNO, int _iCmd, void* _pvCmdBuf, int _iCmdBufLen);
typedef int (__stdcall *pNetClient_SetDiskGroup)( int _iLogonID,void* _lpBuf, int _iDiskGroupNum);
typedef int (__stdcall *pNetClient_GetDiskGroup)( int _iLogonID,void* _lpBuf, int _iDiskGroupNum);
typedef int (__stdcall *pNetClient_SetDiskQuota)( int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetDiskQuotaState )( int _iLogonID, int _iChannelNo, int _iCmd, void* _lpBuf, int _iBufSize );
typedef int (__stdcall *pNetClient_ModifyUserAuthority)( int _iLogonID, char* _pcUserName, void* _lpBuf, int _iBufSize );
typedef int (__stdcall *pNetClient_GetUserAuthority)( int _iLogonID, char* _pcUserName, void* _lpBuf, int _iBufSize );
typedef int (__stdcall *pNetClient_GetGroupAuthority)( int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_NetFileGetQueryfileEx)(int _iLogonID,int _iFileIndex, PNVS_FILE_DATA_EX _pFileInfo);
typedef int (__stdcall *pNetClient_NetFileLockFile)( int _iLogonID, char* _cFileName, int _iLock);
typedef int (__stdcall *pNetClient_GetOsdTextEx)(int _iLogonID,int _iChannel,void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetHolidayPlan)(int _iLogonID,int _iCmd,void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetHolidayPlan)(int _iLogonID,int _iCmd,void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetCommonEnable)( int _iLogonID, int _iEnableID, int _iChannel, int _iEnableValue );
typedef int (__stdcall *pNetClient_GetCommonEnable)( int _iLogonID, int _iEnableID, int _iChannel, int* _iEnableValue );
typedef int (__stdcall *pNetClient_NetFileDownload)(unsigned int* _ulConID, int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_Upgrade_V4)(int _iLogonID, char* _cFileName, int _iUpgradeType, UPGRADE_NOTIFY_V4 _UpgradeNotify);
typedef int (__stdcall *pNetClient_GetAudioCoderList)( int _iLogonID, int _iChannel, int _iStreamNo, int* _piList, int* _piLstCount );
typedef int (__stdcall *pNetClient_InnerAutoTest)(int _iLogonID, int _iCmd, void* _pvTestBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetJEPGSize)(int _iLogonID, int _iChannelNo, int _iWidth, int _iHeight);
typedef int (__stdcall *pNetClient_GetJEPGSize)(int _iLogonID, int _iChannelNo, int* _iWidth, int * _iHeight);
typedef int (__stdcall *pNetClient_QueryDevStatus)(int _iLogonID, int _iType);
typedef int (__stdcall *pNetClient_GetDevStatus)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetHDTemplateIndex)(int _iLogonID, int _iChannelNo /*= 0*/, int* _iTemplateIndex);
typedef int (__stdcall *pNetClient_SetStreamInsertData)(int _iLogonID, int _iChannelNo, int _iFlag, int _iType, void* _pDataBuf, int _iDataLen);
typedef int (__stdcall *pNetClient_GetStreamInsertData)(int _iLogonID, int _iChannelNo, int _iFlag, int _iType, void* _pDataBuf, int _iDataLen);
typedef int (__stdcall *pNetClient_GetOtherID)(int _iLogonID, void* _pcBuff, int _iBufLen);
typedef int (__stdcall *pNetClient_SetDomePTZ)(int _iLogonID, int _iChannelNo, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetDomePTZ)(int _iLogonID, int _iChannelNo, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetUserDataInfo)(unsigned int _ulConID, int _iFlag, void*  _pBuffer, int _iSize);
typedef int (__stdcall *pNetClient_GetBroadcastMessage)( int _iLogonID,void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetModuleCapability)( int _iLogonID,int _iModule,unsigned int* _uCaps);
typedef int (__stdcall *pNetClient_KeyboardCtrl)(char* _pcIP, int _iAction,int _iValue);
typedef int (__stdcall *pNetClient_NetFileSetSchedule)(int _iLogonID, int _iChannel,int _iCmd,void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_NetFileGetSchedule)(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetDevConfig)(int _iLogonID, int _iCommand, int _iChannel, void* _lpInBuffer, int _iInBufferSize);
typedef int (__stdcall *pNetClient_GetDevConfig)(int _iLogonID, int _iCommand, int _iChannel, void* _lpOutBuffer, int _iOutBufferSize, int* _lpBytesReturned);
typedef int (__stdcall *pNetClient_SendCommand)(int _iLogonID,  int _iCommand,  int _iChannel,  void* _pBuffer,  int _iBufferSize);
typedef int (__stdcall *pNetClient_RecvCommand)(int _iLogonID,  int _iCommand,  int _iChannel,  void* _pBuffer,  int _iBufferSize);
typedef int (__stdcall *pNetClient_SetDevDiskConfig )(int _iLogonID, int _iCommand, int _iChannel, void* _lpInBuffer, int _iInBufferSize);
typedef int (__stdcall *pNetClient_GetDevDiskConfig )(int _iLogonID, int _iCommand, int _iChannel, void* _lpOutBuffer, int _iOutBufferSize, int* _lpBytesReturned);
typedef int (__stdcall *pNetClient_Logon_V4)(int _iLogonType, void* _pvPara, int _iInBufferSize);
typedef int (__stdcall *pNetClient_PlayBackControl)(unsigned long _ulConID, int _iControlCode, void* _pcInBuffer, int _iInLen, void* _pcOutBuffer, int* _iOutLen);
typedef int (__stdcall *pNetClient_PlayerControl)(unsigned int _uiRecvID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUsrData);
typedef int (__stdcall *pNetClient_StopPlayBack)(unsigned long _ulConID);
typedef int (__stdcall *pNetClient_PlayBack)(unsigned int* _ulConID, int _iCmd, PlayerParam* _PlayerParam, void* _hWnd);
typedef int (__stdcall *pNetClient_GetPseChInfo)(int _iLogonID, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_SetPseChProperty)(int _iLogonID, int _iPseCh, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetPseChProperty)(int _iLogonID, int _iPseCh, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_ChannelTalkStart)(int _iLogonID, int _iChannel, int _iUser);
typedef int (__stdcall *pNetClient_ChannelTalkEnd)(int _iLogonID, int _iChannel);
typedef int (__stdcall *pNetClient_InputChannelTalkingdata)(int _iLogonID, int _iChannel, unsigned char* _ucData, unsigned int _iLen);
typedef int (__stdcall *pNetClient_GetChannelTalkingState)(int _iLogonID, int _iChannel, int* _iTalkState);
typedef int (__stdcall *pNetClient_CapturePicture)(unsigned int _ulConID, int _iPicType, char* _pcFileName);
typedef int (__stdcall *pNetClient_CapturePicData)(unsigned int _ulConID, int _iPicType, char* _pcPicBuf, int* _piPicSize);
typedef int (__stdcall *pNetClient_SetSDKWorkMode)(int _iWorkMode);
typedef int (__stdcall *pNetClient_Query_V4)(int _iLogonID, int _iCmd, int _iChannel, void* _pvCmdBuf, int _iBufLen);
typedef int (__stdcall *pNetClient_GetQueryResult_V4)(int _iLogonID, int _iCmd, int _iChannel, int _iIndex, void* _pvCmdBuf, int _iBufLen);
typedef int (__stdcall *pNetClient_RebootDeviceByType)(int _iLogonID, int _iType, void* _pvCmdBuf, int _iBufLen);
typedef int (__stdcall *pNetClient_StartDownload)(int _iLogonID, int _iChannel, int _iDownloadMode, void* _lpInBuf, int _iInBufLen
									  , unsigned long* _pulDownloadFd);
typedef int (__stdcall *pNetClient_StopDownload)(unsigned long _ulDownloadFd);
typedef int (__stdcall *pNetClient_GetDownloadPos)(unsigned long _ulDownloadFd, int* _puiDownloadPos);
typedef int (__stdcall *pNetClient_ProxySend )(int _iProtocolType, bool _blSend);
typedef int (__stdcall *pNetClient_SetDevUserDataNotify)(unsigned int _uiRecvID, DEVUSERDATA_NOTIFY _cbkDevUserData, void* _pvUdata);
typedef int (__stdcall *pNetClient_SetDsmConfig)(int _iCommand, void* _pvBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetDsmRegstierInfo)(int _iCommand, void* _pvBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetRecvInfoById)(int _iRecvId, void* _pvBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetParamFromDevice)(int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_GetPlayerIndex)(unsigned int _uiConID);
typedef int (__stdcall *pNetClient_GetRealPlayerIndex)(unsigned int* _ulConID);
typedef int (__stdcall *pNetClient_StartRecvNetPicStream)(int _iLogonID, NetPicPara* _ptPara, int _iBufLen, unsigned int* _puiRecvID);
typedef int (__stdcall *pNetClient_StopRecvNetPicStream)(unsigned int _uiRecvID);
typedef int (__stdcall *pNetClient_SetProxyNotifyFunction)(MAIN_NOTIFY_V4     _ProxyMainNotify,
											 PROXY_NOTIFY       _ProxyNotify);
typedef int (__stdcall *pNetClient_SetExternDevLogonInfo)(unsigned int _uiAllowDevType);
typedef int (__stdcall *pNetClient_SetUnipueAlertConfig)(int _iLogonId, int _iCmdId, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen);
typedef int (__stdcall *pNetClient_GetUnipueAlertConfig)(int _iLogonId, int _iCmdId, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen);
typedef int (__stdcall *pNetClient_FaceConfig)(int _iLogonId, int _iCmdId, int _iChanNo, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen);
typedef int (__stdcall *pNetClient_Query_V5)(int _iLogonId, int _iCmdId, int _iChanNo, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen);
typedef int (__stdcall *pNetClient_SetAlarmNotify_V5)(ALARM_NOTIFY_V5 _pAlarm);
typedef int (__stdcall *pNetClient_Upgrade_V5)(int _iLogonId, int _iType, void* _lpBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_CmdConfig)(int _iLogonId, int _iCmdId, int _iChanNo, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen);
typedef int (__stdcall *pNetClient_GetLastError)();
typedef int (__stdcall *pNetClient_GetConncetInfo)(unsigned int _ulConID, int _iCmdId, void* _pvCmdBuf, int _iCmdBufLen);
typedef int (__stdcall *pNetClient_SyncLogon)(int _iLogonType, void* _pvPara, int _iParaSize);
typedef int (__stdcall *pNetClient_SyncRealPlay)(unsigned int* _puiRecvID, NetClientPara* _ptPara, int _iParaSize);
typedef int (__stdcall *pNetClient_StopRealPlay)(unsigned int _uiRecvID, int _iParam);
typedef int (__stdcall *pNetClient_SyncQuery)(int _iLogonID, int _iChanNo, int _iCmd, void* _pvInPara, int _iInLen, void* _pvOutPara, int _iOutTotalLen, int _iSingleLen);
typedef int (__stdcall *pNetClient_SyncSetDevCfg)(int _iLogonID, int _iChanNo, int _iCmd, void* _pvInPara, int _iInLen, void* _pvOutRet, int _iOutLen);
typedef int (__stdcall *pNetClient_CapturePicByDevice)(int _iLogonID, int _iChanNo, int _iQvalue, char* _pcPicFilePath, SnapPicData* _ptSnapPicData, int _iInSize);
typedef int (__stdcall *pNetClient_SetSDKInitConfig)(int _iCmd, void* _lpInBuffer, int _iInBufferSize);
typedef int (__stdcall *pNetClient_SetAVMode)(int _iRecvID, int _iCmd, void* _pvBuf, int _iBufSize);
typedef void* (__stdcall *pNetClient_CreateQtWidget)(void* _pvParaBuf, int _iBufSize);
typedef int (__stdcall *pNetClient_ReleaseQtWidget)(void* _pvQWidget);
typedef int (__stdcall *pNetClient_GetDevConfig_V5)(int _iLogonID, int _iCmd, void* _pvInParaBuf, int _iInBufLen, void* _pvOutParaBuf, int _iOutBufLen);
typedef int (__stdcall *pNetClient_SycVoiceTalkStart)(unsigned int* _puiTalkID, NetVoiceTalkPara* _pvPara, int _iSize);
typedef int (__stdcall *pNetClient_SycVoiceTalkStop)(unsigned int _uiTalkID, void* _pvPara, int _iSize);
typedef int (__stdcall *pNetClient_SycVoiceTalkInputData)(NetVoiceTalkInput* _pvPara, int _iSize);
typedef int (__stdcall *pNetClient_HttpXmlConfig)(int _iLogonID, XmlCfgInPara* _ptXmlInPara, int _iInSize, XmlCfgOutPara* _ptXmlOutPara, int _iOutSize);
typedef int (__stdcall *pNetClient_XmlSetDevConfig)(int _iLogonID, int _iCmd, void* _pvInPara, int _iInSize, void* _pvOutRet, int _iOutSize);
typedef int (__stdcall *pNetClient_XmlGetDevConfig)(int _iLogonID, int _iCmd, void* _pvInPara, int _iInSize, void* _pvOutRet, int _iOutSize);

pNetClient_Startup_V4 	FNetClient_Startup_V4 = NULL;
pNetClient_SetNotifyFunction_V4 	FNetClient_SetNotifyFunction_V4 = NULL;
pNetClient_StartRecv_V4 	FNetClient_StartRecv_V4 = NULL;
pNetClient_StartRecv_V5 	FNetClient_StartRecv_V5 = NULL;
pNetClient_SetNotifyUserData_V4 	FNetClient_SetNotifyUserData_V4 = NULL;
pNetClient_SetComRecvNotify_V4 	FNetClient_SetComRecvNotify_V4 = NULL;
pNetClient_GetHTTPPort_V4 	FNetClient_GetHTTPPort_V4 = NULL;
pNetClient_SetHTTPPort_V4 	FNetClient_SetHTTPPort_V4 = NULL;
pNetClient_SetDomainParsePara_V4 	FNetClient_SetDomainParsePara_V4 = NULL;
pNetClient_GetDomainParsePara_V4 	FNetClient_GetDomainParsePara_V4 = NULL;
pNetClient_GetBitrateOnVideo_V4 	FNetClient_GetBitrateOnVideo_V4 = NULL;
pNetClient_SetDecCallBack_V4 	FNetClient_SetDecCallBack_V4 = NULL;
pNetClient_RegisterDrawFun 	FNetClient_RegisterDrawFun = NULL;
pNetClient_SetPort 	FNetClient_SetPort = NULL;
#ifdef WIN32
pNetClient_Startup 	FNetClient_Startup = NULL;
pNetClient_SetNotifyFunction 	FNetClient_SetNotifyFunction = NULL;
pNetClient_SetNotifyFunctionEx 	FNetClient_SetNotifyFunctionEx = NULL;
pNetClient_SetMSGHandle 	FNetClient_SetMSGHandle = NULL;
pNetClient_SetMSGHandleEx 	FNetClient_SetMSGHandleEx = NULL;
pNetClient_StartRecv 	FNetClient_StartRecv = NULL;
pNetClient_StartRecvEx 	FNetClient_StartRecvEx = NULL;
pNetClient_SetComRecvNotify 	FNetClient_SetComRecvNotify = NULL;
pNetClient_GetHTTPPort 	FNetClient_GetHTTPPort = NULL;
pNetClient_SetHTTPPort 	FNetClient_SetHTTPPort = NULL;
pNetClient_SetDomainParsePara 	FNetClient_SetDomainParsePara = NULL;
pNetClient_GetDomainParsePara 	FNetClient_GetDomainParsePara = NULL;
pNetClient_GetBitrateOnVideo 	FNetClient_GetBitrateOnVideo = NULL;
pNetClient_SetDecCallBack 	FNetClient_SetDecCallBack = NULL;
pNetClient_InterTalkStartEx 	FNetClient_InterTalkStartEx = NULL;
#else
pNetClient_Startup 	FNetClient_Startup = NULL;
pNetClient_SetNotifyFunction 	FNetClient_SetNotifyFunction = NULL;
pNetClient_StartRecv 	FNetClient_StartRecv = NULL;
pNetClient_SetNotifyUserData 	FNetClient_SetNotifyUserData = NULL;
pNetClient_SetComRecvNotify 	FNetClient_SetComRecvNotify = NULL;
pNetClient_GetHTTPPort 	FNetClient_GetHTTPPort = NULL;
pNetClient_SetHTTPPort 	FNetClient_SetHTTPPort = NULL;
pNetClient_SetDomainParsePara 	FNetClient_SetDomainParsePara = NULL;
pNetClient_GetDomainParsePara 	FNetClient_GetDomainParsePara = NULL;
pNetClient_GetBitrateOnVideo 	FNetClient_GetBitrateOnVideo = NULL;
pNetClient_SetDecCallBack 	FNetClient_SetDecCallBack = NULL;
pNetClient_SetDecCallBackEx 	FNetClient_SetDecCallBackEx = NULL;
pNetClient_InterTalkStartEx 	FNetClient_InterTalkStartEx = NULL;
#endif
pNetClient_Cleanup 	FNetClient_Cleanup = NULL;
pNetClient_GetVersion 	FNetClient_GetVersion = NULL;
pNetClient_Logon 	FNetClient_Logon = NULL;
pNetClient_LogonEx 	FNetClient_LogonEx = NULL;
pNetClient_Logoff 	FNetClient_Logoff = NULL;
pNetClient_GetLogonStatus 	FNetClient_GetLogonStatus = NULL;
pNetClient_ProxyGetDevInfo 	FNetClient_ProxyGetDevInfo = NULL;
pNetClient_StopRecv 	FNetClient_StopRecv = NULL;
pNetClient_GetRecvID 	FNetClient_GetRecvID = NULL;
pNetClient_GetInfoByConnectID 	FNetClient_GetInfoByConnectID = NULL;
pNetClient_SetFullStreamNotify  	FNetClient_SetFullStreamNotify  = NULL;
pNetClient_SetFullStreamNotify_V4  	FNetClient_SetFullStreamNotify_V4  = NULL;
pNetClient_GetCmdString 	FNetClient_GetCmdString = NULL;
pNetClient_GetDevInfo 	FNetClient_GetDevInfo = NULL;
pNetClient_SendDataToServer 	FNetClient_SendDataToServer = NULL;
pNetClient_IsValidUser 	FNetClient_IsValidUser = NULL;
pNetClient_SetInnerDataNotify 	FNetClient_SetInnerDataNotify = NULL;
pNetClient_SetWorkMode 	FNetClient_SetWorkMode = NULL;
#ifdef WIN32
pNetClient_AddActiveServer 	FNetClient_AddActiveServer = NULL;
pNetClient_BindSocket 	FNetClient_BindSocket = NULL;
#else
pNetClient_AddActiveServer 	FNetClient_AddActiveServer = NULL;
pNetClient_BindSocket 	FNetClient_BindSocket = NULL;
#endif
pNetClient_PushData 	FNetClient_PushData = NULL;
pNetClient_DelActiveServer 	FNetClient_DelActiveServer = NULL;
pNetClient_StartCaptureData 	FNetClient_StartCaptureData = NULL;
pNetClient_StopCaptureData 	FNetClient_StopCaptureData = NULL;
pNetClient_GetVideoHeader 	FNetClient_GetVideoHeader = NULL;
pNetClient_SetRawFrameCallBack 	FNetClient_SetRawFrameCallBack = NULL;
pNetClient_SetRawFrameCallBackEx 	FNetClient_SetRawFrameCallBackEx = NULL;
pNetClient_StartCaptureFile 	FNetClient_StartCaptureFile = NULL;
pNetClient_StopCaptureFile 	FNetClient_StopCaptureFile = NULL;
pNetClient_GetCaptureStatus 	FNetClient_GetCaptureStatus = NULL;
pNetClient_SetCaptureFileSize 	FNetClient_SetCaptureFileSize = NULL;
pNetClient_StartPlay 	FNetClient_StartPlay = NULL;
pNetClient_StartPlayEx 	FNetClient_StartPlayEx = NULL;
pNetClient_StartPlayEs 	FNetClient_StartPlayEs = NULL;
pNetClient_StopPlay 	FNetClient_StopPlay = NULL;
pNetClient_StopPlayEx 	FNetClient_StopPlayEx = NULL;
pNetClient_SetPlayRectEx 	FNetClient_SetPlayRectEx = NULL;
pNetClient_SetSrcRect 	FNetClient_SetSrcRect = NULL;
pNetClient_ResetPlayerWnd 	FNetClient_ResetPlayerWnd = NULL;
pNetClient_GetPlayingStatus 	FNetClient_GetPlayingStatus = NULL;
pNetClient_AdjustVideo 	FNetClient_AdjustVideo = NULL;
pNetClient_AudioStart 	FNetClient_AudioStart = NULL;
pNetClient_AudioStop 	FNetClient_AudioStop = NULL;
pNetClient_SetLocalAudioVolume 	FNetClient_SetLocalAudioVolume = NULL;
pNetClient_SetBufferNum 	FNetClient_SetBufferNum = NULL;
pNetClient_SetPlayDelay 	FNetClient_SetPlayDelay = NULL;
pNetClient_GetChannelNum 	FNetClient_GetChannelNum = NULL;
pNetClient_GetAlarmPortNum 	FNetClient_GetAlarmPortNum = NULL;
pNetClient_GetLocalAlarmNum 	FNetClient_GetLocalAlarmNum = NULL;
pNetClient_SetVideoPara 	FNetClient_SetVideoPara = NULL;
pNetClient_GetVideoPara 	FNetClient_GetVideoPara = NULL;
pNetClient_SetVideoparaSchedule 	FNetClient_SetVideoparaSchedule = NULL;
pNetClient_GetVideoparaSchedule 	FNetClient_GetVideoparaSchedule = NULL;
pNetClient_SetVideoQuality 	FNetClient_SetVideoQuality = NULL;
pNetClient_GetVideoQuality 	FNetClient_GetVideoQuality = NULL;
pNetClient_SetFrameRate 	FNetClient_SetFrameRate = NULL;
pNetClient_GetFrameRate 	FNetClient_GetFrameRate = NULL;
pNetClient_GetDecordFrameNum  	FNetClient_GetDecordFrameNum  = NULL;
pNetClient_SetStreamType 	FNetClient_SetStreamType = NULL;
pNetClient_GetStreamType 	FNetClient_GetStreamType = NULL;
pNetClient_SetMotionAreaEnable  	FNetClient_SetMotionAreaEnable  = NULL;
pNetClient_SetMotionDetetionArea 	FNetClient_SetMotionDetetionArea = NULL;
pNetClient_GetMotionDetetionArea 	FNetClient_GetMotionDetetionArea = NULL;
pNetClient_SetThreshold 	FNetClient_SetThreshold = NULL;
pNetClient_GetThreshold 	FNetClient_GetThreshold = NULL;
pNetClient_SetMotionDetection 	FNetClient_SetMotionDetection = NULL;
pNetClient_GetMotionDetection 	FNetClient_GetMotionDetection = NULL;
pNetClient_SetMotionDecLinkRec 	FNetClient_SetMotionDecLinkRec = NULL;
pNetClient_GetMotionDecLinkRec 	FNetClient_GetMotionDecLinkRec = NULL;
pNetClient_SetMotionDecLinkSnap 	FNetClient_SetMotionDecLinkSnap = NULL;
pNetClient_GetMotionDecLinkSnap 	FNetClient_GetMotionDecLinkSnap = NULL;
pNetClient_SetMotionDecLinkSoundDisplay 	FNetClient_SetMotionDecLinkSoundDisplay = NULL;
pNetClient_GetMotionDecLinkSoundDisplay 	FNetClient_GetMotionDecLinkSoundDisplay = NULL;
pNetClient_SetMotionDecLinkOutport 	FNetClient_SetMotionDecLinkOutport = NULL;
pNetClient_GetMotionDecLinkOutport 	FNetClient_GetMotionDecLinkOutport = NULL;
pNetClient_SetMotionDetectSchedule 	FNetClient_SetMotionDetectSchedule = NULL;
pNetClient_GetMotionDetectSchedule 	FNetClient_GetMotionDetectSchedule = NULL;
pNetClient_SetOsdDiaphaneity 	FNetClient_SetOsdDiaphaneity = NULL;
pNetClient_GetOsdDiaphaneity 	FNetClient_GetOsdDiaphaneity = NULL;
pNetClient_SetOsdText 	FNetClient_SetOsdText = NULL;
pNetClient_GetOsdText 	FNetClient_GetOsdText = NULL;
pNetClient_SetOsdType 	FNetClient_SetOsdType = NULL;
pNetClient_GetOsdType 	FNetClient_GetOsdType = NULL;
pNetClient_SetDateFormat 	FNetClient_SetDateFormat = NULL;
pNetClient_GetDateFormat 	FNetClient_GetDateFormat = NULL;
pNetClient_SetOsdLOGO  	FNetClient_SetOsdLOGO  = NULL;
pNetClient_SetAudioChannel 	FNetClient_SetAudioChannel = NULL;
pNetClient_GetAudioChannel 	FNetClient_GetAudioChannel = NULL;
pNetClient_SetIpFilter 	FNetClient_SetIpFilter = NULL;
pNetClient_GetIpFilter 	FNetClient_GetIpFilter = NULL;
pNetClient_SetAlarmOutput 	FNetClient_SetAlarmOutput = NULL;
pNetClient_GetAlarmOutput 	FNetClient_GetAlarmOutput = NULL;
pNetClient_GetAlarmIPortState 	FNetClient_GetAlarmIPortState = NULL;
pNetClient_SetAlarmPortEnable 	FNetClient_SetAlarmPortEnable = NULL;
pNetClient_GetAlarmPortEnable 	FNetClient_GetAlarmPortEnable = NULL;
pNetClient_SetInportAlarmLinkRec 	FNetClient_SetInportAlarmLinkRec = NULL;
pNetClient_GetInportAlarmLinkRec 	FNetClient_GetInportAlarmLinkRec = NULL;
pNetClient_SetInportAlarmLinkSnap 	FNetClient_SetInportAlarmLinkSnap = NULL;
pNetClient_GetInportAlarmLinkSnap 	FNetClient_GetInportAlarmLinkSnap = NULL;
pNetClient_SetInportAlarmLinkPTZ 	FNetClient_SetInportAlarmLinkPTZ = NULL;
pNetClient_GetInportAlarmLinkPTZ 	FNetClient_GetInportAlarmLinkPTZ = NULL;
pNetClient_SetInportAlarmLinkSoundDisplay 	FNetClient_SetInportAlarmLinkSoundDisplay = NULL;
pNetClient_GetInportAlarmLinkSoundDisplay 	FNetClient_GetInportAlarmLinkSoundDisplay = NULL;
pNetClient_SetInportAlmLinkOutport 	FNetClient_SetInportAlmLinkOutport = NULL;
pNetClient_GetInportAlmLinkOutport 	FNetClient_GetInportAlmLinkOutport = NULL;
pNetClient_SetAlarmInMode 	FNetClient_SetAlarmInMode = NULL;
pNetClient_GetAlarmInMode 	FNetClient_GetAlarmInMode = NULL;
pNetClient_SetAlarmOutMode 	FNetClient_SetAlarmOutMode = NULL;
pNetClient_GetAlarmOutMode 	FNetClient_GetAlarmOutMode = NULL;
pNetClient_SetInportAlarmSchedule 	FNetClient_SetInportAlarmSchedule = NULL;
pNetClient_GetInportAlarmSchedule 	FNetClient_GetInportAlarmSchedule = NULL;
pNetClient_SetOutportAlarmSchedule 	FNetClient_SetOutportAlarmSchedule = NULL;
pNetClient_GetOutportAlarmSchedule 	FNetClient_GetOutportAlarmSchedule = NULL;
pNetClient_SetOutportAlarmDelay 	FNetClient_SetOutportAlarmDelay = NULL;
pNetClient_GetOutportAlarmDelay 	FNetClient_GetOutportAlarmDelay = NULL;
pNetClient_SetInportEnable 	FNetClient_SetInportEnable = NULL;
pNetClient_GetInportEnable 	FNetClient_GetInportEnable = NULL;
pNetClient_SetOutportEnable 	FNetClient_SetOutportEnable = NULL;
pNetClient_GetOutportEnable 	FNetClient_GetOutportEnable = NULL;
pNetClient_SetOutportState 	FNetClient_SetOutportState = NULL;
pNetClient_GetOutportState 	FNetClient_GetOutportState = NULL;
pNetClient_SetAlmVdoCovThreshold 	FNetClient_SetAlmVdoCovThreshold = NULL;
pNetClient_GetAlmVdoCovThreshold 	FNetClient_GetAlmVdoCovThreshold = NULL;
pNetClient_SetAlmVideoCov 	FNetClient_SetAlmVideoCov = NULL;
pNetClient_GetAlmVideoCov 	FNetClient_GetAlmVideoCov = NULL;
pNetClient_SetDeviceType 	FNetClient_SetDeviceType = NULL;
pNetClient_GetDeviceType 	FNetClient_GetDeviceType = NULL;
pNetClient_SetComFormat 	FNetClient_SetComFormat = NULL;
pNetClient_GetComFormat 	FNetClient_GetComFormat = NULL;
pNetClient_GetAllSupportDeviceType 	FNetClient_GetAllSupportDeviceType = NULL;
pNetClient_DeviceCtrl 	FNetClient_DeviceCtrl = NULL;
pNetClient_DeviceCtrlEx 	FNetClient_DeviceCtrlEx = NULL;
pNetClient_ComSend 	FNetClient_ComSend = NULL;
pNetClient_DevicePTZCtrl 	FNetClient_DevicePTZCtrl = NULL;
pNetClient_GetComPortCounts 	FNetClient_GetComPortCounts = NULL;
pNetClient_SetAlarmVideoLost 	FNetClient_SetAlarmVideoLost = NULL;
pNetClient_GetAlarmVideoLost 	FNetClient_GetAlarmVideoLost = NULL;
pNetClient_SetVideoLostLinkPTZ 	FNetClient_SetVideoLostLinkPTZ = NULL;
pNetClient_GetVideoLostLinkPTZ 	FNetClient_GetVideoLostLinkPTZ = NULL;
pNetClient_SetVideoLostLinkSoundDisplay 	FNetClient_SetVideoLostLinkSoundDisplay = NULL;
pNetClient_GetVideoLostLinkSoundDisplay 	FNetClient_GetVideoLostLinkSoundDisplay = NULL;
pNetClient_GetAlarmVLostState 	FNetClient_GetAlarmVLostState = NULL;
pNetClient_SetVideoLostLinkOutport 	FNetClient_SetVideoLostLinkOutport = NULL;
pNetClient_GetVideoLostLinkOutport 	FNetClient_GetVideoLostLinkOutport = NULL;
pNetClient_SetVideoLostSchedule 	FNetClient_SetVideoLostSchedule = NULL;
pNetClient_GetVideoLostSchedule 	FNetClient_GetVideoLostSchedule = NULL;
pNetClient_SetVideoLostLinkRec 	FNetClient_SetVideoLostLinkRec = NULL;
pNetClient_GetVideoLostLinkRec 	FNetClient_GetVideoLostLinkRec = NULL;
pNetClient_SetVideoLostLinkSnap 	FNetClient_SetVideoLostLinkSnap = NULL;
pNetClient_GetVideoLostLinkSnap 	FNetClient_GetVideoLostLinkSnap = NULL;
pNetClient_SetIFrameRate 	FNetClient_SetIFrameRate = NULL;
pNetClient_GetIFrameRate 	FNetClient_GetIFrameRate = NULL;
pNetClient_ForceIFrame 	FNetClient_ForceIFrame = NULL;
pNetClient_SetTime 	FNetClient_SetTime = NULL;
pNetClient_Reboot 	FNetClient_Reboot = NULL;
pNetClient_RebootEx 	FNetClient_RebootEx = NULL;
pNetClient_DefaultPara 	FNetClient_DefaultPara = NULL;
pNetClient_DefaultParaEx 	FNetClient_DefaultParaEx = NULL;
pNetClient_GetServerVersion 	FNetClient_GetServerVersion = NULL;
pNetClient_SetNVS 	FNetClient_SetNVS = NULL;
pNetClient_UpgradeProgram 	FNetClient_UpgradeProgram = NULL;
pNetClient_UpgradeWebPage 	FNetClient_UpgradeWebPage = NULL;
pNetClient_GetUpgradePercent 	FNetClient_GetUpgradePercent = NULL;
pNetClient_GetUserNum 	FNetClient_GetUserNum = NULL;
pNetClient_GetUserInfo 	FNetClient_GetUserInfo = NULL;
pNetClient_GetCurUserInfo 	FNetClient_GetCurUserInfo = NULL;
pNetClient_AddUser 	FNetClient_AddUser = NULL;
pNetClient_DelUser 	FNetClient_DelUser = NULL;
pNetClient_ModifyPwd 	FNetClient_ModifyPwd = NULL;
pNetClient_SetMaxConUser 	FNetClient_SetMaxConUser = NULL;
pNetClient_GetMaxGetUser 	FNetClient_GetMaxGetUser = NULL;
pNetClient_TalkStart 	FNetClient_TalkStart = NULL;
pNetClient_TalkEnd 	FNetClient_TalkEnd = NULL;
pNetClient_InputTalkingdata 	FNetClient_InputTalkingdata = NULL;
pNetClient_GetTalkingState 	FNetClient_GetTalkingState = NULL;
pNetClient_CapturePic 	FNetClient_CapturePic = NULL;
pNetClient_CaptureBmpPic 	FNetClient_CaptureBmpPic = NULL;
pNetClient_ChangeSvrIP 	FNetClient_ChangeSvrIP = NULL;
pNetClient_GetIpProperty 	FNetClient_GetIpProperty = NULL;
pNetClient_SetDHCPParam 	FNetClient_SetDHCPParam = NULL;
pNetClient_GetDHCPParam 	FNetClient_GetDHCPParam = NULL;
pNetClient_SetWifiDHCPParam 	FNetClient_SetWifiDHCPParam = NULL;
pNetClient_GetWifiDHCPParam 	FNetClient_GetWifiDHCPParam = NULL;
pNetClient_GetVideoCovArea 	FNetClient_GetVideoCovArea = NULL;
pNetClient_SetVideoCovArea 	FNetClient_SetVideoCovArea = NULL;
pNetClient_GetVideoSize 	FNetClient_GetVideoSize = NULL;
pNetClient_SetVideoSize 	FNetClient_SetVideoSize = NULL;
pNetClient_GetVideoSizeEx 	FNetClient_GetVideoSizeEx = NULL;
pNetClient_GetMaxMinorVideoSize 	FNetClient_GetMaxMinorVideoSize = NULL;
pNetClient_BindInterface 	FNetClient_BindInterface = NULL;
pNetClient_GetNetInterface 	FNetClient_GetNetInterface = NULL;
pNetClient_SetMaxKByteRate 	FNetClient_SetMaxKByteRate = NULL;
pNetClient_GetMaxKByteRate 	FNetClient_GetMaxKByteRate = NULL;
pNetClient_WriteUserData 	FNetClient_WriteUserData = NULL;
pNetClient_ReadUserData 	FNetClient_ReadUserData = NULL;
pNetClient_SetReducenoiseState 	FNetClient_SetReducenoiseState = NULL;
pNetClient_GetReducenoiseState 	FNetClient_GetReducenoiseState = NULL;
pNetClient_DrawTextOnVideo 	FNetClient_DrawTextOnVideo = NULL;
pNetClient_GetTextOnVideo 	FNetClient_GetTextOnVideo = NULL;
pNetClient_SetBothStreamSame 	FNetClient_SetBothStreamSame = NULL;
pNetClient_GetBothStreamSame 	FNetClient_GetBothStreamSame = NULL;
pNetClient_ShowBitrateOnVideo 	FNetClient_ShowBitrateOnVideo = NULL;
pNetClient_SetPPPoEInfo 	FNetClient_SetPPPoEInfo = NULL;
pNetClient_GetPPPoEInfo 	FNetClient_GetPPPoEInfo = NULL;
pNetClient_CPUCheckEnabled 	FNetClient_CPUCheckEnabled = NULL;
pNetClient_SetEncodeMode 	FNetClient_SetEncodeMode = NULL;
pNetClient_GetEncodeMode 	FNetClient_GetEncodeMode = NULL;
pNetClient_SetPreferMode 	FNetClient_SetPreferMode = NULL;
pNetClient_GetPreferMode 	FNetClient_GetPreferMode = NULL;
pNetClient_LogFileSetProperty 	FNetClient_LogFileSetProperty = NULL;
pNetClient_LogFileGetProperty 	FNetClient_LogFileGetProperty = NULL;
pNetClient_LogFileDownload 	FNetClient_LogFileDownload = NULL;
pNetClient_LogFileClear 	FNetClient_LogFileClear = NULL;
pNetClient_LogFileGetDetails 	FNetClient_LogFileGetDetails = NULL;
pNetClient_GetVideoNPMode 	FNetClient_GetVideoNPMode = NULL;
pNetClient_SetVideoNPMode 	FNetClient_SetVideoNPMode = NULL;
pNetClient_SetAudioEncoder 	FNetClient_SetAudioEncoder = NULL;
pNetClient_GetAudioEncoder 	FNetClient_GetAudioEncoder = NULL;
pNetClient_NetFileQuery 	FNetClient_NetFileQuery = NULL;
pNetClient_NetFileSetRecordRule 	FNetClient_NetFileSetRecordRule = NULL;
pNetClient_NetFileGetRecordRule 	FNetClient_NetFileGetRecordRule = NULL;
pNetClient_NetFileSetAlarmRule 	FNetClient_NetFileSetAlarmRule = NULL;
pNetClient_NetFileGetAlarmRule 	FNetClient_NetFileGetAlarmRule = NULL;
pNetClient_NetFileSetAlarmState 	FNetClient_NetFileSetAlarmState = NULL;
pNetClient_NetFileGetAlarmState 	FNetClient_NetFileGetAlarmState = NULL;
pNetClient_NetFileSetTaskState 	FNetClient_NetFileSetTaskState = NULL;
pNetClient_NetFileGetTaskState 	FNetClient_NetFileGetTaskState = NULL;
pNetClient_NetFileSetTaskSchedule 	FNetClient_NetFileSetTaskSchedule = NULL;
pNetClient_NetFileGetTaskSchedule 	FNetClient_NetFileGetTaskSchedule = NULL;
pNetClient_NetFileSetTaskScheduleEx 	FNetClient_NetFileSetTaskScheduleEx = NULL;
pNetClient_NetFileGetTaskScheduleEx 	FNetClient_NetFileGetTaskScheduleEx = NULL;
pNetClient_NetFileGetFileCount 	FNetClient_NetFileGetFileCount = NULL;
pNetClient_NetFileRebuildIndexFile 	FNetClient_NetFileRebuildIndexFile = NULL;
pNetClient_NetFileGetDiskInfo 	FNetClient_NetFileGetDiskInfo = NULL;
pNetClient_NetFileGetDiskInfoEx 	FNetClient_NetFileGetDiskInfoEx = NULL;
pNetClient_NetFileIsSupportStorage 	FNetClient_NetFileIsSupportStorage = NULL;
pNetClient_NetFileDownloadFile 	FNetClient_NetFileDownloadFile = NULL;
pNetClient_NetFileDownloadFileEx 	FNetClient_NetFileDownloadFileEx = NULL;
pNetClient_NetFileStopDownloadFile 	FNetClient_NetFileStopDownloadFile = NULL;
pNetClient_NetFileGetDownloadPos 	FNetClient_NetFileGetDownloadPos = NULL;
pNetClient_NetFileMountUSB 	FNetClient_NetFileMountUSB = NULL;
pNetClient_NetFileGetRecordState 	FNetClient_NetFileGetRecordState = NULL;
pNetClient_NetFileDelFile 	FNetClient_NetFileDelFile = NULL;
pNetClient_DiskSetUsage 	FNetClient_DiskSetUsage = NULL;
pNetClient_NetFileGetQueryfile 	FNetClient_NetFileGetQueryfile = NULL;
pNetClient_DiskFormat 	FNetClient_DiskFormat = NULL;
pNetClient_DiskPart 	FNetClient_DiskPart = NULL;
pNetClient_NetFileManualRecord 	FNetClient_NetFileManualRecord = NULL;
pNetClient_NetFileMapStoreDevice 	FNetClient_NetFileMapStoreDevice = NULL;
pNetClient_NetFileGetMapStoreDevice 	FNetClient_NetFileGetMapStoreDevice = NULL;
pNetClient_NetFileGetUSBstate 	FNetClient_NetFileGetUSBstate = NULL;
pNetClient_NetFileSetExtendname 	FNetClient_NetFileSetExtendname = NULL;
pNetClient_NetFileGetExtendname 	FNetClient_NetFileGetExtendname = NULL;
pNetClient_ClearDisk 	FNetClient_ClearDisk = NULL;
pNetClient_GetDownloadFailedFileName 	FNetClient_GetDownloadFailedFileName = NULL;
pNetClient_SetMediaStreamClient 	FNetClient_SetMediaStreamClient = NULL;
pNetClient_GetMediaStreamClient 	FNetClient_GetMediaStreamClient = NULL;
pNetClient_SetEmailAlarm 	FNetClient_SetEmailAlarm = NULL;
pNetClient_GetEmailAlarm 	FNetClient_GetEmailAlarm = NULL;
pNetClient_SetEmailAlarmEnable 	FNetClient_SetEmailAlarmEnable = NULL;
pNetClient_GetEmailAlarmEnable 	FNetClient_GetEmailAlarmEnable = NULL;
pNetClient_SetScene 	FNetClient_SetScene = NULL;
pNetClient_GetScene 	FNetClient_GetScene = NULL;
pNetClient_SetSensorFlip 	FNetClient_SetSensorFlip = NULL;
pNetClient_GetSensorFlip 	FNetClient_GetSensorFlip = NULL;
pNetClient_SetSensorMirror 	FNetClient_SetSensorMirror = NULL;
pNetClient_GetSensorMirror 	FNetClient_GetSensorMirror = NULL;
pNetClient_Snapshot 	FNetClient_Snapshot = NULL;
pNetClient_GetFactoryID 	FNetClient_GetFactoryID = NULL;
pNetClient_SetWifiParam 	FNetClient_SetWifiParam = NULL;
pNetClient_GetWifiParam 	FNetClient_GetWifiParam = NULL;
pNetClient_WifiSearch 	FNetClient_WifiSearch = NULL;
pNetClient_GetWifiSearchResult 	FNetClient_GetWifiSearchResult = NULL;
pNetClient_SetPrivacyProtect 	FNetClient_SetPrivacyProtect = NULL;
pNetClient_GetPrivacyProtect 	FNetClient_GetPrivacyProtect = NULL;
pNetClient_IYUVtoYV12 	FNetClient_IYUVtoYV12 = NULL;
pNetClient_GetDevType 	FNetClient_GetDevType = NULL;
pNetClient_GetProductType 	FNetClient_GetProductType = NULL;
pNetClient_GetProductTypeEx 	FNetClient_GetProductTypeEx = NULL;
pNetClient_BackupKernel 	FNetClient_BackupKernel = NULL;
pNetClient_SetUPNPEnable 	FNetClient_SetUPNPEnable = NULL;
pNetClient_GetUPNPEnable 	FNetClient_GetUPNPEnable = NULL;
pNetClient_GetSysInfo 	FNetClient_GetSysInfo = NULL;
pNetClient_SetDDNSPara 	FNetClient_SetDDNSPara = NULL;
pNetClient_GetDDNSPara 	FNetClient_GetDDNSPara = NULL;
pNetClient_SetFuncListArray 	FNetClient_SetFuncListArray = NULL;
pNetClient_GetFuncListArray 	FNetClient_GetFuncListArray = NULL;
pNetClient_SendStringToServer 	FNetClient_SendStringToServer = NULL;
pNetClient_ReceiveString 	FNetClient_ReceiveString = NULL;
pNetClient_SendStringToCenter 	FNetClient_SendStringToCenter = NULL;
pNetClient_SetVencType 	FNetClient_SetVencType = NULL;
pNetClient_GetVencType 	FNetClient_GetVencType = NULL;
pNetClient_SetComServer 	FNetClient_SetComServer = NULL;
pNetClient_GetComServer 	FNetClient_GetComServer = NULL;
pNetClient_Get3GDeviceStatus 	FNetClient_Get3GDeviceStatus = NULL;
pNetClient_Set3GDialog 	FNetClient_Set3GDialog = NULL;
pNetClient_Get3GDialog 	FNetClient_Get3GDialog = NULL;
pNetClient_Set3GMessage 	FNetClient_Set3GMessage = NULL;
pNetClient_Get3GMessage 	FNetClient_Get3GMessage = NULL;
pNetClient_Set3GTaskSchedule 	FNetClient_Set3GTaskSchedule = NULL;
pNetClient_Get3GTaskSchedule 	FNetClient_Get3GTaskSchedule = NULL;
pNetClient_Set3GNotify 	FNetClient_Set3GNotify = NULL;
pNetClient_Get3GNotify 	FNetClient_Get3GNotify = NULL;
pNetClient_SetHDCamer 	FNetClient_SetHDCamer = NULL;
pNetClient_GetHDCamer 	FNetClient_GetHDCamer = NULL;
pNetClient_SetAlarmServer 	FNetClient_SetAlarmServer = NULL;
pNetClient_GetAlarmServer 	FNetClient_GetAlarmServer = NULL;
pNetClient_InterTalkStart 	FNetClient_InterTalkStart = NULL;
pNetClient_InterTalkEnd 	FNetClient_InterTalkEnd = NULL;
pNetClient_NetFileQueryEx 	FNetClient_NetFileQueryEx = NULL;
pNetClient_ControlDeviceRecord 	FNetClient_ControlDeviceRecord = NULL;
pNetClient_NetFileDownloadByTimeSpan 	FNetClient_NetFileDownloadByTimeSpan = NULL;
pNetClient_NetFileDownloadByTimeSpanEx 	FNetClient_NetFileDownloadByTimeSpanEx = NULL;
pNetClient_NetFileDownloadByTimeSpanCallBack 	FNetClient_NetFileDownloadByTimeSpanCallBack = NULL;
pNetClient_NetLogQuery 	FNetClient_NetLogQuery = NULL;
pNetClient_NetLogGetLogfile 	FNetClient_NetLogGetLogfile = NULL;
pNetClient_NetLogGetLogCount 	FNetClient_NetLogGetLogCount = NULL;
pNetClient_GetProtocolList 	FNetClient_GetProtocolList = NULL;
pNetClient_SetCHNPTZCRUISE 	FNetClient_SetCHNPTZCRUISE = NULL;
pNetClient_GetCHNPTZCRUISE 	FNetClient_GetCHNPTZCRUISE = NULL;
pNetClient_SetVIDEOCOVER_LINKRECORD 	FNetClient_SetVIDEOCOVER_LINKRECORD = NULL;
pNetClient_GetVIDEOCOVER_LINKRECORD 	FNetClient_GetVIDEOCOVER_LINKRECORD = NULL;
pNetClient_SetVIDEOCOVER_LINKPTZ 	FNetClient_SetVIDEOCOVER_LINKPTZ = NULL;
pNetClient_GetVIDEOCOVER_LINKPTZ 	FNetClient_GetVIDEOCOVER_LINKPTZ = NULL;
pNetClient_GetAlarmVCoverState 	FNetClient_GetAlarmVCoverState = NULL;
pNetClient_StopCaptureDate 	FNetClient_StopCaptureDate = NULL;
pNetClient_SetColorToGray 	FNetClient_SetColorToGray = NULL;
pNetClient_GetColorToGray 	FNetClient_GetColorToGray = NULL;
pNetClient_SetCustomChannelName 	FNetClient_SetCustomChannelName = NULL;
pNetClient_GetCustomChannelName 	FNetClient_GetCustomChannelName = NULL;
pNetClient_SetCustomRecType 	FNetClient_SetCustomRecType = NULL;
pNetClient_GetCustomRecType 	FNetClient_GetCustomRecType = NULL;
pNetClient_ChangeSvrIPEx 	FNetClient_ChangeSvrIPEx = NULL;
pNetClient_GetIpPropertyEx 	FNetClient_GetIpPropertyEx = NULL;
pNetClient_SetFTPUpdate 	FNetClient_SetFTPUpdate = NULL;
pNetClient_GetFTPUpdate 	FNetClient_GetFTPUpdate = NULL;
pNetClient_SetCHNPTZFormat 	FNetClient_SetCHNPTZFormat = NULL;
pNetClient_GetCHNPTZFormat 	FNetClient_GetCHNPTZFormat = NULL;
pNetClient_GetServerVersionEx 	FNetClient_GetServerVersionEx = NULL;
pNetClient_GetOSDTypeColor 	FNetClient_GetOSDTypeColor = NULL;
pNetClient_SetOSDTypeColor 	FNetClient_SetOSDTypeColor = NULL;
pNetClient_GetExceptionMsg 	FNetClient_GetExceptionMsg = NULL;
pNetClient_SetNTPInfo 	FNetClient_SetNTPInfo = NULL;
pNetClient_GetNTPInfo 	FNetClient_GetNTPInfo = NULL;
pNetClient_SetVideoEncrypt 	FNetClient_SetVideoEncrypt = NULL;
pNetClient_GetVideoEncrypt 	FNetClient_GetVideoEncrypt = NULL;
pNetClient_SetVideoDecrypt 	FNetClient_SetVideoDecrypt = NULL;
pNetClient_GetVideoDecrypt 	FNetClient_GetVideoDecrypt = NULL;
pNetClient_SetPreRecEnable 	FNetClient_SetPreRecEnable = NULL;
pNetClient_GetPreRecEnable 	FNetClient_GetPreRecEnable = NULL;
pNetClient_SetVideoCombine 	FNetClient_SetVideoCombine = NULL;
pNetClient_GetVideoCombine 	FNetClient_GetVideoCombine = NULL;
pNetClient_VCASetConfig 	FNetClient_VCASetConfig = NULL;
pNetClient_VCAGetConfig 	FNetClient_VCAGetConfig = NULL;
pNetClient_VCARestart 	FNetClient_VCARestart = NULL;
pNetClient_VCARestartEx 	FNetClient_VCARestartEx = NULL;
pNetClient_VCAGetAlarmInfo 	FNetClient_VCAGetAlarmInfo = NULL;
pNetClient_SetEmailAlarmEx 	FNetClient_SetEmailAlarmEx = NULL;
pNetClient_GetEmailAlarmEx 	FNetClient_GetEmailAlarmEx = NULL;
pNetClient_SetFTPUploadConfig 	FNetClient_SetFTPUploadConfig = NULL;
pNetClient_GetFTPUploadConfig 	FNetClient_GetFTPUploadConfig = NULL;
pNetClient_Set3GConfig 	FNetClient_Set3GConfig = NULL;
pNetClient_Get3GConfig 	FNetClient_Get3GConfig = NULL;
pNetClient_SetDigitalChannelConfig 	FNetClient_SetDigitalChannelConfig = NULL;
pNetClient_GetDigitalChannelConfig 	FNetClient_GetDigitalChannelConfig = NULL;
pNetClient_DigitalChannelSend 	FNetClient_DigitalChannelSend = NULL;
pNetClient_SendComData 	FNetClient_SendComData = NULL;
pNetClient_SetVideoNPModeEx 	FNetClient_SetVideoNPModeEx = NULL;
pNetClient_GetVideoNPModeEx 	FNetClient_GetVideoNPModeEx = NULL;
pNetClient_GetDigitalChannelNum 	FNetClient_GetDigitalChannelNum = NULL;
pNetClient_GetChannelProperty 	FNetClient_GetChannelProperty = NULL;
pNetClient_SetDeviceTimerReboot 	FNetClient_SetDeviceTimerReboot = NULL;
pNetClient_GetDeviceTimerReboot 	FNetClient_GetDeviceTimerReboot = NULL;
pNetClient_SetVideoCoverSchedule 	FNetClient_SetVideoCoverSchedule = NULL;
pNetClient_GetVideoCoverSchedule 	FNetClient_GetVideoCoverSchedule = NULL;
pNetClient_SetCPUMEMAlarmThreshold 	FNetClient_SetCPUMEMAlarmThreshold = NULL;
pNetClient_GetCPUMEMAlarmThreshold 	FNetClient_GetCPUMEMAlarmThreshold = NULL;
pNetClient_SetDZInfo 	FNetClient_SetDZInfo = NULL;
pNetClient_GetDZInfo 	FNetClient_GetDZInfo = NULL;
pNetClient_SetPTZAutoBack 	FNetClient_SetPTZAutoBack = NULL;
pNetClient_GetPTZAutoBack 	FNetClient_GetPTZAutoBack = NULL;
pNetClient_Set3GVPND 	FNetClient_Set3GVPND = NULL;
pNetClient_Get3GVPND 	FNetClient_Get3GVPND = NULL;
pNetClient_SetHDCamerEx 	FNetClient_SetHDCamerEx = NULL;
pNetClient_GetHDCamerEx 	FNetClient_GetHDCamerEx = NULL;
pNetClient_SetFTPUsage 	FNetClient_SetFTPUsage = NULL;
pNetClient_GetFTPUsage 	FNetClient_GetFTPUsage = NULL;
pNetClient_SetChannelSipConfig 	FNetClient_SetChannelSipConfig = NULL;
pNetClient_GetChannelSipConfig 	FNetClient_GetChannelSipConfig = NULL;
pNetClient_GetMaxVideoSize 	FNetClient_GetMaxVideoSize = NULL;
pNetClient_SetBitRatePercent 	FNetClient_SetBitRatePercent = NULL;
pNetClient_GetBitRatePercent 	FNetClient_GetBitRatePercent = NULL;
pNetClient_GetVideoParam 	FNetClient_GetVideoParam = NULL;
pNetClient_SetOSDAlpha 	FNetClient_SetOSDAlpha = NULL;
pNetClient_GetOSDAlpha 	FNetClient_GetOSDAlpha = NULL;
pNetClient_DeviceSetup 	FNetClient_DeviceSetup = NULL;
pNetClient_SetPlayerShowFrameMode 	FNetClient_SetPlayerShowFrameMode = NULL;
pNetClient_GetPlayerShowFrameMode 	FNetClient_GetPlayerShowFrameMode = NULL;
pNetClient_DrawRectOnLocalVideo 	FNetClient_DrawRectOnLocalVideo = NULL;
pNetClient_DrawPolyOnLocalVideo 	FNetClient_DrawPolyOnLocalVideo = NULL;
pNetClient_SendStringToServerEx 	FNetClient_SendStringToServerEx = NULL;
pNetClient_SetNetFileDownloadFileCallBack 	FNetClient_SetNetFileDownloadFileCallBack = NULL;
pNetClient_SetDataPackCallBack 	FNetClient_SetDataPackCallBack = NULL;
pNetClient_AddConnectionToNetWork 	FNetClient_AddConnectionToNetWork = NULL;
pNetClient_MallocConnection 	FNetClient_MallocConnection = NULL;
pNetClient_FreeConnection 	FNetClient_FreeConnection = NULL;
pNetClient_NetFileSetChannelParam 	FNetClient_NetFileSetChannelParam = NULL;
pNetClient_NetFileGetChannelParam 	FNetClient_NetFileGetChannelParam = NULL;
pNetClient_ShutDownDev 	FNetClient_ShutDownDev = NULL;
pNetClient_BackupImage 	FNetClient_BackupImage = NULL;
pNetClient_SetLanParam 	FNetClient_SetLanParam = NULL;
pNetClient_GetLanParam 	FNetClient_GetLanParam = NULL;
pNetClient_GetVideoSzList 	FNetClient_GetVideoSzList = NULL;
pNetClient_SetAlarmConfig 	FNetClient_SetAlarmConfig = NULL;
pNetClient_GetAlarmConfig 	FNetClient_GetAlarmConfig = NULL;
pNetClient_SetITSBlock 	FNetClient_SetITSBlock = NULL;
pNetClient_GetITSBlock 	FNetClient_GetITSBlock = NULL;
pNetClient_SetHDTimeRangeParam 	FNetClient_SetHDTimeRangeParam = NULL;
pNetClient_GetHDTimeRangeParam 	FNetClient_GetHDTimeRangeParam = NULL;
pNetClient_SetHDTemplateName 	FNetClient_SetHDTemplateName = NULL;
pNetClient_GetHDTemplateName 	FNetClient_GetHDTemplateName = NULL;
pNetClient_SetHDTemplateMap 	FNetClient_SetHDTemplateMap = NULL;
pNetClient_GetHDTemplateMap 	FNetClient_GetHDTemplateMap = NULL;
pNetClient_SetITSTimeRangeEnable 	FNetClient_SetITSTimeRangeEnable = NULL;
pNetClient_GetITSTimeRangeEnable 	FNetClient_GetITSTimeRangeEnable = NULL;
pNetClient_SetITSTimeRange 	FNetClient_SetITSTimeRange = NULL;
pNetClient_GetITSTimeRange 	FNetClient_GetITSTimeRange = NULL;
pNetClient_SetITSDetectMode 	FNetClient_SetITSDetectMode = NULL;
pNetClient_GetITSDetectMode 	FNetClient_GetITSDetectMode = NULL;
pNetClient_SetITSLoopMode 	FNetClient_SetITSLoopMode = NULL;
pNetClient_GetITSLoopMode 	FNetClient_GetITSLoopMode = NULL;
pNetClient_SetITSDeviceType 	FNetClient_SetITSDeviceType = NULL;
pNetClient_GetITSDeviceType 	FNetClient_GetITSDeviceType = NULL;
pNetClient_SetITSRoadwayParam 	FNetClient_SetITSRoadwayParam = NULL;
pNetClient_GetITSRoadwayParam 	FNetClient_GetITSRoadwayParam = NULL;
pNetClient_SetITSLicensePlateOptimize 	FNetClient_SetITSLicensePlateOptimize = NULL;
pNetClient_GetITSLicensePlateOptimize 	FNetClient_GetITSLicensePlateOptimize = NULL;
pNetClient_SetITSExtraInfo 	FNetClient_SetITSExtraInfo = NULL;
pNetClient_GetITSExtraInfo 	FNetClient_GetITSExtraInfo = NULL;
pNetClient_CheckDeviceState 	FNetClient_CheckDeviceState = NULL;
pNetClient_GetDeviceState 	FNetClient_GetDeviceState = NULL;
pNetClient_GetCameraCheckInfo 	FNetClient_GetCameraCheckInfo = NULL;
pNetClient_CheckCamera 	FNetClient_CheckCamera = NULL;
pNetClient_GetCharSet 	FNetClient_GetCharSet = NULL;
pNetClient_SetTimeZone 	FNetClient_SetTimeZone = NULL;
pNetClient_GetTimeZone 	FNetClient_GetTimeZone = NULL;
pNetClient_SetCurLanguage 	FNetClient_SetCurLanguage = NULL;
pNetClient_GetCurLanguage 	FNetClient_GetCurLanguage = NULL;
pNetClient_GetLanguageList 	FNetClient_GetLanguageList = NULL;
pNetClient_SetChannelEncodeProfile 	FNetClient_SetChannelEncodeProfile = NULL;
pNetClient_GetChannelEncodeProfile 	FNetClient_GetChannelEncodeProfile = NULL;
pNetClient_SetAlarmClear 	FNetClient_SetAlarmClear = NULL;
pNetClient_SetExceptionHandleParam 	FNetClient_SetExceptionHandleParam = NULL;
pNetClient_GetExceptionHandleParam 	FNetClient_GetExceptionHandleParam = NULL;
pNetClient_SetAlarmLink_V1 	FNetClient_SetAlarmLink_V1 = NULL;
pNetClient_GetAlarmLink_V1 	FNetClient_GetAlarmLink_V1 = NULL;
pNetClient_SetCameraParam 	FNetClient_SetCameraParam = NULL;
pNetClient_GetCameraParam 	FNetClient_GetCameraParam = NULL;
pNetClient_SetColorParam 	FNetClient_SetColorParam = NULL;
pNetClient_GetColorParam 	FNetClient_GetColorParam = NULL;
pNetClient_InnerMallocBlock 	FNetClient_InnerMallocBlock = NULL;
pNetClient_InnerFreeBlock 	FNetClient_InnerFreeBlock = NULL;
pNetClient_InnerReferBlock 	FNetClient_InnerReferBlock = NULL;
pNetClient_InnerReleaseBlock 	FNetClient_InnerReleaseBlock = NULL;
pNetClient_SetJPEGQuality 	FNetClient_SetJPEGQuality = NULL;
pNetClient_GetJPEGQuality 	FNetClient_GetJPEGQuality = NULL;
pNetClient_GetConnectInfo 	FNetClient_GetConnectInfo = NULL;
pNetClient_SetPlatformApp 	FNetClient_SetPlatformApp = NULL;
pNetClient_GetPlatformApp 	FNetClient_GetPlatformApp = NULL;
pNetClient_SetManagerServersInfo 	FNetClient_SetManagerServersInfo = NULL;
pNetClient_GetManagerServersInfo 	FNetClient_GetManagerServersInfo = NULL;
pNetClient_SetDeviceID 	FNetClient_SetDeviceID = NULL;
pNetClient_GetDeviceID 	FNetClient_GetDeviceID = NULL;
pNetClient_SetATMConfig 	FNetClient_SetATMConfig = NULL;
pNetClient_GetATMConfig 	FNetClient_GetATMConfig = NULL;
pNetClient_ATMQueryFile 	FNetClient_ATMQueryFile = NULL;
pNetClient_ATMGetQueryFile 	FNetClient_ATMGetQueryFile = NULL;
pNetClient_SetAudioSample 	FNetClient_SetAudioSample = NULL;
pNetClient_GetAudioSample 	FNetClient_GetAudioSample = NULL;
pNetClient_SetSystemTypeEx 	FNetClient_SetSystemTypeEx = NULL;
pNetClient_GetSystemTypeEx 	FNetClient_GetSystemTypeEx = NULL;
pNetClient_SetHXListenPortInfo 	FNetClient_SetHXListenPortInfo = NULL;
pNetClient_GetHXListenPortInfo 	FNetClient_GetHXListenPortInfo = NULL;
pNetClient_SetVideoModeMethod 	FNetClient_SetVideoModeMethod = NULL;
pNetClient_GetVideoModeMethod 	FNetClient_GetVideoModeMethod = NULL;
pNetClient_GetMonitorNum 	FNetClient_GetMonitorNum = NULL;
pNetClient_GetMonitorInfo 	FNetClient_GetMonitorInfo = NULL;
pNetClient_ChangeMonitor 	FNetClient_ChangeMonitor = NULL;
pNetClient_EZoomAdd 	FNetClient_EZoomAdd = NULL;
pNetClient_EZoomSet 	FNetClient_EZoomSet = NULL;
pNetClient_EZoomReset 	FNetClient_EZoomReset = NULL;
pNetClient_EZoomRemove 	FNetClient_EZoomRemove = NULL;
pNetClient_DCardStartPlay 	FNetClient_DCardStartPlay = NULL;
pNetClient_DCardStopPlay 	FNetClient_DCardStopPlay = NULL;
pNetClient_DCardRelease 	FNetClient_DCardRelease = NULL;
pNetClient_DCardReInit 	FNetClient_DCardReInit = NULL;
pNetClient_DCardGetState 	FNetClient_DCardGetState = NULL;
pNetClient_DCardStartPlayEx 	FNetClient_DCardStartPlayEx = NULL;
pNetClient_DCardPutDataEx 	FNetClient_DCardPutDataEx = NULL;
pNetClient_DCardStopPlayEx 	FNetClient_DCardStopPlayEx = NULL;
pNetClient_DCardStartPlayAudio 	FNetClient_DCardStartPlayAudio = NULL;
pNetClient_SetEncryptSN 	FNetClient_SetEncryptSN = NULL;
pNetClient_GetSNReg 	FNetClient_GetSNReg = NULL;
pNetClient_GetComFormat_V1 	FNetClient_GetComFormat_V1 = NULL;
pNetClient_SetComFormat_V2 	FNetClient_SetComFormat_V2 = NULL;
pNetClient_GetComFormat_V2 	FNetClient_GetComFormat_V2 = NULL;
pNetClient_GetServerVersion_V1 	FNetClient_GetServerVersion_V1 = NULL;
pNetClient_InputTalkingdataEx 	FNetClient_InputTalkingdataEx = NULL;
pNetClient_SetVerticalSync 	FNetClient_SetVerticalSync = NULL;
pNetClient_GetVerticalSync 	FNetClient_GetVerticalSync = NULL;
pNetClient_SetLocalAudioVolumeEx 	FNetClient_SetLocalAudioVolumeEx = NULL;
pNetClient_GetLocalAudioVolumeEx 	FNetClient_GetLocalAudioVolumeEx = NULL;
pNetClient_ClearPolyLocalVideo 	FNetClient_ClearPolyLocalVideo = NULL;
pNetClient_SetOSDTypeFontSize 	FNetClient_SetOSDTypeFontSize = NULL;
pNetClient_GetOSDTypeFontSize 	FNetClient_GetOSDTypeFontSize = NULL;
pNetClient_SetImgDisposal 	FNetClient_SetImgDisposal = NULL;
pNetClient_GetImgDisposal 	FNetClient_GetImgDisposal = NULL;
pNetClient_SetMuted 	FNetClient_SetMuted = NULL;
pNetClient_SetPWMValue 	FNetClient_SetPWMValue = NULL;
pNetClient_GetPWMValue 	FNetClient_GetPWMValue = NULL;
pNetClient_SetSystemType 	FNetClient_SetSystemType = NULL;
pNetClient_GetSystemType 	FNetClient_GetSystemType = NULL;
pNetClient_SetITSSwitchTime 	FNetClient_SetITSSwitchTime = NULL;
pNetClient_GetITSSwitchTime 	FNetClient_GetITSSwitchTime = NULL;
pNetClient_SetITSRecoParam 	FNetClient_SetITSRecoParam = NULL;
pNetClient_GetITSRecoParam 	FNetClient_GetITSRecoParam = NULL;
pNetClient_SetITSDayNight 	FNetClient_SetITSDayNight = NULL;
pNetClient_GetITSDayNight 	FNetClient_GetITSDayNight = NULL;
pNetClient_SetITSCamLocation 	FNetClient_SetITSCamLocation = NULL;
pNetClient_GetITSCamLocation 	FNetClient_GetITSCamLocation = NULL;
pNetClient_SetITSWorkMode 	FNetClient_SetITSWorkMode = NULL;
pNetClient_GetITSWorkMode 	FNetClient_GetITSWorkMode = NULL;
pNetClient_SetWaterMarkEnable 	FNetClient_SetWaterMarkEnable = NULL;
pNetClient_GetWaterMarkEnable 	FNetClient_GetWaterMarkEnable = NULL;
pNetClient_SetITSLightInfo 	FNetClient_SetITSLightInfo = NULL;
pNetClient_GetITSLightInfo 	FNetClient_GetITSLightInfo = NULL;
pNetClient_SetHardWareParam 	FNetClient_SetHardWareParam = NULL;
pNetClient_GetHardWareParam 	FNetClient_GetHardWareParam = NULL;
pNetClient_SetDomeAdvParam 	FNetClient_SetDomeAdvParam = NULL;
pNetClient_GetDomeAdvParam 	FNetClient_GetDomeAdvParam = NULL;
pNetClient_SetDiskGroup 	FNetClient_SetDiskGroup = NULL;
pNetClient_GetDiskGroup 	FNetClient_GetDiskGroup = NULL;
pNetClient_SetDiskQuota 	FNetClient_SetDiskQuota = NULL;
pNetClient_GetDiskQuotaState  	FNetClient_GetDiskQuotaState  = NULL;
pNetClient_ModifyUserAuthority 	FNetClient_ModifyUserAuthority = NULL;
pNetClient_GetUserAuthority 	FNetClient_GetUserAuthority = NULL;
pNetClient_GetGroupAuthority 	FNetClient_GetGroupAuthority = NULL;
pNetClient_NetFileGetQueryfileEx 	FNetClient_NetFileGetQueryfileEx = NULL;
pNetClient_NetFileLockFile 	FNetClient_NetFileLockFile = NULL;
pNetClient_GetOsdTextEx 	FNetClient_GetOsdTextEx = NULL;
pNetClient_SetHolidayPlan 	FNetClient_SetHolidayPlan = NULL;
pNetClient_GetHolidayPlan 	FNetClient_GetHolidayPlan = NULL;
pNetClient_SetCommonEnable 	FNetClient_SetCommonEnable = NULL;
pNetClient_GetCommonEnable 	FNetClient_GetCommonEnable = NULL;
pNetClient_NetFileDownload 	FNetClient_NetFileDownload = NULL;
pNetClient_Upgrade_V4 	FNetClient_Upgrade_V4 = NULL;
pNetClient_GetAudioCoderList 	FNetClient_GetAudioCoderList = NULL;
pNetClient_InnerAutoTest 	FNetClient_InnerAutoTest = NULL;
pNetClient_SetJEPGSize 	FNetClient_SetJEPGSize = NULL;
pNetClient_GetJEPGSize 	FNetClient_GetJEPGSize = NULL;
pNetClient_QueryDevStatus 	FNetClient_QueryDevStatus = NULL;
pNetClient_GetDevStatus 	FNetClient_GetDevStatus = NULL;
pNetClient_GetHDTemplateIndex 	FNetClient_GetHDTemplateIndex = NULL;
pNetClient_SetStreamInsertData 	FNetClient_SetStreamInsertData = NULL;
pNetClient_GetStreamInsertData 	FNetClient_GetStreamInsertData = NULL;
pNetClient_GetOtherID 	FNetClient_GetOtherID = NULL;
pNetClient_SetDomePTZ 	FNetClient_SetDomePTZ = NULL;
pNetClient_GetDomePTZ 	FNetClient_GetDomePTZ = NULL;
pNetClient_GetUserDataInfo 	FNetClient_GetUserDataInfo = NULL;
pNetClient_GetBroadcastMessage 	FNetClient_GetBroadcastMessage = NULL;
pNetClient_GetModuleCapability 	FNetClient_GetModuleCapability = NULL;
pNetClient_KeyboardCtrl 	FNetClient_KeyboardCtrl = NULL;
pNetClient_NetFileSetSchedule 	FNetClient_NetFileSetSchedule = NULL;
pNetClient_NetFileGetSchedule 	FNetClient_NetFileGetSchedule = NULL;
pNetClient_SetDevConfig 	FNetClient_SetDevConfig = NULL;
pNetClient_GetDevConfig 	FNetClient_GetDevConfig = NULL;
pNetClient_SendCommand 	FNetClient_SendCommand = NULL;
pNetClient_RecvCommand 	FNetClient_RecvCommand = NULL;
pNetClient_SetDevDiskConfig  	FNetClient_SetDevDiskConfig  = NULL;
pNetClient_GetDevDiskConfig  	FNetClient_GetDevDiskConfig  = NULL;
pNetClient_Logon_V4 	FNetClient_Logon_V4 = NULL;
pNetClient_PlayBackControl 	FNetClient_PlayBackControl = NULL;
pNetClient_PlayerControl 	FNetClient_PlayerControl = NULL;
pNetClient_StopPlayBack 	FNetClient_StopPlayBack = NULL;
pNetClient_PlayBack 	FNetClient_PlayBack = NULL;
pNetClient_GetPseChInfo 	FNetClient_GetPseChInfo = NULL;
pNetClient_SetPseChProperty 	FNetClient_SetPseChProperty = NULL;
pNetClient_GetPseChProperty 	FNetClient_GetPseChProperty = NULL;
pNetClient_ChannelTalkStart 	FNetClient_ChannelTalkStart = NULL;
pNetClient_ChannelTalkEnd 	FNetClient_ChannelTalkEnd = NULL;
pNetClient_InputChannelTalkingdata 	FNetClient_InputChannelTalkingdata = NULL;
pNetClient_GetChannelTalkingState 	FNetClient_GetChannelTalkingState = NULL;
pNetClient_CapturePicture 	FNetClient_CapturePicture = NULL;
pNetClient_CapturePicData 	FNetClient_CapturePicData = NULL;
pNetClient_SetSDKWorkMode 	FNetClient_SetSDKWorkMode = NULL;
pNetClient_Query_V4 	FNetClient_Query_V4 = NULL;
pNetClient_GetQueryResult_V4 	FNetClient_GetQueryResult_V4 = NULL;
pNetClient_RebootDeviceByType 	FNetClient_RebootDeviceByType = NULL;
pNetClient_StartDownload 	FNetClient_StartDownload = NULL;
pNetClient_StopDownload 	FNetClient_StopDownload = NULL;
pNetClient_GetDownloadPos 	FNetClient_GetDownloadPos = NULL;
pNetClient_ProxySend  	FNetClient_ProxySend  = NULL;
pNetClient_SetDevUserDataNotify 	FNetClient_SetDevUserDataNotify = NULL;
pNetClient_SetDsmConfig 	FNetClient_SetDsmConfig = NULL;
pNetClient_GetDsmRegstierInfo 	FNetClient_GetDsmRegstierInfo = NULL;
pNetClient_GetRecvInfoById 	FNetClient_GetRecvInfoById = NULL;
pNetClient_GetParamFromDevice 	FNetClient_GetParamFromDevice = NULL;
pNetClient_GetPlayerIndex 	FNetClient_GetPlayerIndex = NULL;
pNetClient_GetRealPlayerIndex 	FNetClient_GetRealPlayerIndex = NULL;
pNetClient_StartRecvNetPicStream 	FNetClient_StartRecvNetPicStream = NULL;
pNetClient_StopRecvNetPicStream 	FNetClient_StopRecvNetPicStream = NULL;
pNetClient_SetProxyNotifyFunction 	FNetClient_SetProxyNotifyFunction = NULL;
pNetClient_SetExternDevLogonInfo 	FNetClient_SetExternDevLogonInfo = NULL;
pNetClient_SetUnipueAlertConfig 	FNetClient_SetUnipueAlertConfig = NULL;
pNetClient_GetUnipueAlertConfig 	FNetClient_GetUnipueAlertConfig = NULL;
pNetClient_FaceConfig 	FNetClient_FaceConfig = NULL;
pNetClient_Query_V5 	FNetClient_Query_V5 = NULL;
pNetClient_SetAlarmNotify_V5 	FNetClient_SetAlarmNotify_V5 = NULL;
pNetClient_Upgrade_V5 	FNetClient_Upgrade_V5 = NULL;
pNetClient_CmdConfig 	FNetClient_CmdConfig = NULL;
pNetClient_GetLastError 	FNetClient_GetLastError = NULL;
pNetClient_GetConncetInfo 	FNetClient_GetConncetInfo = NULL;
pNetClient_SyncLogon 	FNetClient_SyncLogon = NULL;
pNetClient_SyncRealPlay 	FNetClient_SyncRealPlay = NULL;
pNetClient_StopRealPlay 	FNetClient_StopRealPlay = NULL;
pNetClient_SyncQuery 	FNetClient_SyncQuery = NULL;
pNetClient_SyncSetDevCfg 	FNetClient_SyncSetDevCfg = NULL;
pNetClient_CapturePicByDevice 	FNetClient_CapturePicByDevice = NULL;
pNetClient_SetSDKInitConfig 	FNetClient_SetSDKInitConfig = NULL;
pNetClient_SetAVMode 	FNetClient_SetAVMode = NULL;
pNetClient_CreateQtWidget 	FNetClient_CreateQtWidget = NULL;
pNetClient_ReleaseQtWidget 	FNetClient_ReleaseQtWidget = NULL;
pNetClient_GetDevConfig_V5 	FNetClient_GetDevConfig_V5 = NULL;
pNetClient_SycVoiceTalkStart 	FNetClient_SycVoiceTalkStart = NULL;
pNetClient_SycVoiceTalkStop 	FNetClient_SycVoiceTalkStop = NULL;
pNetClient_SycVoiceTalkInputData 	FNetClient_SycVoiceTalkInputData = NULL;
pNetClient_HttpXmlConfig 	FNetClient_HttpXmlConfig = NULL;
pNetClient_XmlSetDevConfig 	FNetClient_XmlSetDevConfig = NULL;
pNetClient_XmlGetDevConfig 	FNetClient_XmlGetDevConfig = NULL;


void* DemoExport(void* _pvHandle, const char* _pcSymbol)
{
#ifdef  WIN32
		return GetProcAddress((HMODULE)_pvHandle, _pcSymbol);
#else
		return dlsym(_pvHandle, _pcSymbol);
#endif
	}

void* g_pvInstance = NULL;
int LoadNVSSDK()
{
	if (g_pvInstance) {
		return -2;
	} else {
#ifdef  WIN32
			g_pvInstance = LoadLibrary("NVSSDK.dll");
#else
			g_pvInstance = dlopen("libnvssdk.so", RTLD_LAZY);
#endif
		if (NULL == g_pvInstance) {
			return -1;
		}
	}

	FNetClient_Startup_V4 = (pNetClient_Startup_V4)DemoExport(g_pvInstance, "NetClient_Startup_V4");
	FNetClient_SetNotifyFunction_V4 = (pNetClient_SetNotifyFunction_V4)DemoExport(g_pvInstance, "NetClient_SetNotifyFunction_V4");
	FNetClient_StartRecv_V4 = (pNetClient_StartRecv_V4)DemoExport(g_pvInstance, "NetClient_StartRecv_V4");
	FNetClient_StartRecv_V5 = (pNetClient_StartRecv_V5)DemoExport(g_pvInstance, "NetClient_StartRecv_V5");
	FNetClient_SetNotifyUserData_V4 = (pNetClient_SetNotifyUserData_V4)DemoExport(g_pvInstance, "NetClient_SetNotifyUserData_V4");
	FNetClient_SetComRecvNotify_V4 = (pNetClient_SetComRecvNotify_V4)DemoExport(g_pvInstance, "NetClient_SetComRecvNotify_V4");
	FNetClient_GetHTTPPort_V4 = (pNetClient_GetHTTPPort_V4)DemoExport(g_pvInstance, "NetClient_GetHTTPPort_V4");
	FNetClient_SetHTTPPort_V4 = (pNetClient_SetHTTPPort_V4)DemoExport(g_pvInstance, "NetClient_SetHTTPPort_V4");
	FNetClient_SetDomainParsePara_V4 = (pNetClient_SetDomainParsePara_V4)DemoExport(g_pvInstance, "NetClient_SetDomainParsePara_V4");
	FNetClient_GetDomainParsePara_V4 = (pNetClient_GetDomainParsePara_V4)DemoExport(g_pvInstance, "NetClient_GetDomainParsePara_V4");
	FNetClient_GetBitrateOnVideo_V4 = (pNetClient_GetBitrateOnVideo_V4)DemoExport(g_pvInstance, "NetClient_GetBitrateOnVideo_V4");
	FNetClient_SetDecCallBack_V4 = (pNetClient_SetDecCallBack_V4)DemoExport(g_pvInstance, "NetClient_SetDecCallBack_V4");
	FNetClient_RegisterDrawFun = (pNetClient_RegisterDrawFun)DemoExport(g_pvInstance, "NetClient_RegisterDrawFun");
	FNetClient_SetPort = (pNetClient_SetPort)DemoExport(g_pvInstance, "NetClient_SetPort");
#ifdef WIN32
	FNetClient_Startup = (pNetClient_Startup)DemoExport(g_pvInstance, "NetClient_Startup");
	FNetClient_SetNotifyFunction = (pNetClient_SetNotifyFunction)DemoExport(g_pvInstance, "NetClient_SetNotifyFunction");
	FNetClient_SetNotifyFunctionEx = (pNetClient_SetNotifyFunctionEx)DemoExport(g_pvInstance, "NetClient_SetNotifyFunctionEx");
	FNetClient_SetMSGHandle = (pNetClient_SetMSGHandle)DemoExport(g_pvInstance, "NetClient_SetMSGHandle");
	FNetClient_SetMSGHandleEx = (pNetClient_SetMSGHandleEx)DemoExport(g_pvInstance, "NetClient_SetMSGHandleEx");
	FNetClient_StartRecv = (pNetClient_StartRecv)DemoExport(g_pvInstance, "NetClient_StartRecv");
	FNetClient_StartRecvEx = (pNetClient_StartRecvEx)DemoExport(g_pvInstance, "NetClient_StartRecvEx");
	FNetClient_SetComRecvNotify = (pNetClient_SetComRecvNotify)DemoExport(g_pvInstance, "NetClient_SetComRecvNotify");
	FNetClient_GetHTTPPort = (pNetClient_GetHTTPPort)DemoExport(g_pvInstance, "NetClient_GetHTTPPort");
	FNetClient_SetHTTPPort = (pNetClient_SetHTTPPort)DemoExport(g_pvInstance, "NetClient_SetHTTPPort");
	FNetClient_SetDomainParsePara = (pNetClient_SetDomainParsePara)DemoExport(g_pvInstance, "NetClient_SetDomainParsePara");
	FNetClient_GetDomainParsePara = (pNetClient_GetDomainParsePara)DemoExport(g_pvInstance, "NetClient_GetDomainParsePara");
	FNetClient_GetBitrateOnVideo = (pNetClient_GetBitrateOnVideo)DemoExport(g_pvInstance, "NetClient_GetBitrateOnVideo");
	FNetClient_SetDecCallBack = (pNetClient_SetDecCallBack)DemoExport(g_pvInstance, "NetClient_SetDecCallBack");
	FNetClient_InterTalkStartEx = (pNetClient_InterTalkStartEx)DemoExport(g_pvInstance, "NetClient_InterTalkStartEx");
#else
	FNetClient_Startup = (pNetClient_Startup)DemoExport(g_pvInstance, "NetClient_Startup");
	FNetClient_SetNotifyFunction = (pNetClient_SetNotifyFunction)DemoExport(g_pvInstance, "NetClient_SetNotifyFunction");
	FNetClient_StartRecv = (pNetClient_StartRecv)DemoExport(g_pvInstance, "NetClient_StartRecv");
	FNetClient_SetNotifyUserData = (pNetClient_SetNotifyUserData)DemoExport(g_pvInstance, "NetClient_SetNotifyUserData");
	FNetClient_SetComRecvNotify = (pNetClient_SetComRecvNotify)DemoExport(g_pvInstance, "NetClient_SetComRecvNotify");
	FNetClient_GetHTTPPort = (pNetClient_GetHTTPPort)DemoExport(g_pvInstance, "NetClient_GetHTTPPort");
	FNetClient_SetHTTPPort = (pNetClient_SetHTTPPort)DemoExport(g_pvInstance, "NetClient_SetHTTPPort");
	FNetClient_SetDomainParsePara = (pNetClient_SetDomainParsePara)DemoExport(g_pvInstance, "NetClient_SetDomainParsePara");
	FNetClient_GetDomainParsePara = (pNetClient_GetDomainParsePara)DemoExport(g_pvInstance, "NetClient_GetDomainParsePara");
	FNetClient_GetBitrateOnVideo = (pNetClient_GetBitrateOnVideo)DemoExport(g_pvInstance, "NetClient_GetBitrateOnVideo");
	FNetClient_SetDecCallBack = (pNetClient_SetDecCallBack)DemoExport(g_pvInstance, "NetClient_SetDecCallBack");
	FNetClient_SetDecCallBackEx = (pNetClient_SetDecCallBackEx)DemoExport(g_pvInstance, "NetClient_SetDecCallBackEx");
	FNetClient_InterTalkStartEx = (pNetClient_InterTalkStartEx)DemoExport(g_pvInstance, "NetClient_InterTalkStartEx");
#endif
	FNetClient_Cleanup = (pNetClient_Cleanup)DemoExport(g_pvInstance, "NetClient_Cleanup");
	FNetClient_GetVersion = (pNetClient_GetVersion)DemoExport(g_pvInstance, "NetClient_GetVersion");
	FNetClient_Logon = (pNetClient_Logon)DemoExport(g_pvInstance, "NetClient_Logon");
	FNetClient_LogonEx = (pNetClient_LogonEx)DemoExport(g_pvInstance, "NetClient_LogonEx");
	FNetClient_Logoff = (pNetClient_Logoff)DemoExport(g_pvInstance, "NetClient_Logoff");
	FNetClient_GetLogonStatus = (pNetClient_GetLogonStatus)DemoExport(g_pvInstance, "NetClient_GetLogonStatus");
	FNetClient_ProxyGetDevInfo = (pNetClient_ProxyGetDevInfo)DemoExport(g_pvInstance, "NetClient_ProxyGetDevInfo");
	FNetClient_StopRecv = (pNetClient_StopRecv)DemoExport(g_pvInstance, "NetClient_StopRecv");
	FNetClient_GetRecvID = (pNetClient_GetRecvID)DemoExport(g_pvInstance, "NetClient_GetRecvID");
	FNetClient_GetInfoByConnectID = (pNetClient_GetInfoByConnectID)DemoExport(g_pvInstance, "NetClient_GetInfoByConnectID");
	FNetClient_SetFullStreamNotify  = (pNetClient_SetFullStreamNotify )DemoExport(g_pvInstance, "NetClient_SetFullStreamNotify ");
	FNetClient_SetFullStreamNotify_V4  = (pNetClient_SetFullStreamNotify_V4 )DemoExport(g_pvInstance, "NetClient_SetFullStreamNotify_V4 ");
	FNetClient_GetCmdString = (pNetClient_GetCmdString)DemoExport(g_pvInstance, "NetClient_GetCmdString");
	FNetClient_GetDevInfo = (pNetClient_GetDevInfo)DemoExport(g_pvInstance, "NetClient_GetDevInfo");
	FNetClient_SendDataToServer = (pNetClient_SendDataToServer)DemoExport(g_pvInstance, "NetClient_SendDataToServer");
	FNetClient_IsValidUser = (pNetClient_IsValidUser)DemoExport(g_pvInstance, "NetClient_IsValidUser");
	FNetClient_SetInnerDataNotify = (pNetClient_SetInnerDataNotify)DemoExport(g_pvInstance, "NetClient_SetInnerDataNotify");
	FNetClient_SetWorkMode = (pNetClient_SetWorkMode)DemoExport(g_pvInstance, "NetClient_SetWorkMode");
#ifdef WIN32
	FNetClient_AddActiveServer = (pNetClient_AddActiveServer)DemoExport(g_pvInstance, "NetClient_AddActiveServer");
	FNetClient_BindSocket = (pNetClient_BindSocket)DemoExport(g_pvInstance, "NetClient_BindSocket");
#else
	FNetClient_AddActiveServer = (pNetClient_AddActiveServer)DemoExport(g_pvInstance, "NetClient_AddActiveServer");
	FNetClient_BindSocket = (pNetClient_BindSocket)DemoExport(g_pvInstance, "NetClient_BindSocket");
#endif
	FNetClient_PushData = (pNetClient_PushData)DemoExport(g_pvInstance, "NetClient_PushData");
	FNetClient_DelActiveServer = (pNetClient_DelActiveServer)DemoExport(g_pvInstance, "NetClient_DelActiveServer");
	FNetClient_StartCaptureData = (pNetClient_StartCaptureData)DemoExport(g_pvInstance, "NetClient_StartCaptureData");
	FNetClient_StopCaptureData = (pNetClient_StopCaptureData)DemoExport(g_pvInstance, "NetClient_StopCaptureData");
	FNetClient_GetVideoHeader = (pNetClient_GetVideoHeader)DemoExport(g_pvInstance, "NetClient_GetVideoHeader");
	FNetClient_SetRawFrameCallBack = (pNetClient_SetRawFrameCallBack)DemoExport(g_pvInstance, "NetClient_SetRawFrameCallBack");
	FNetClient_SetRawFrameCallBackEx = (pNetClient_SetRawFrameCallBackEx)DemoExport(g_pvInstance, "NetClient_SetRawFrameCallBackEx");
	FNetClient_StartCaptureFile = (pNetClient_StartCaptureFile)DemoExport(g_pvInstance, "NetClient_StartCaptureFile");
	FNetClient_StopCaptureFile = (pNetClient_StopCaptureFile)DemoExport(g_pvInstance, "NetClient_StopCaptureFile");
	FNetClient_GetCaptureStatus = (pNetClient_GetCaptureStatus)DemoExport(g_pvInstance, "NetClient_GetCaptureStatus");
	FNetClient_SetCaptureFileSize = (pNetClient_SetCaptureFileSize)DemoExport(g_pvInstance, "NetClient_SetCaptureFileSize");
	FNetClient_StartPlay = (pNetClient_StartPlay)DemoExport(g_pvInstance, "NetClient_StartPlay");
	FNetClient_StartPlayEx = (pNetClient_StartPlayEx)DemoExport(g_pvInstance, "NetClient_StartPlayEx");
	FNetClient_StartPlayEs = (pNetClient_StartPlayEs)DemoExport(g_pvInstance, "NetClient_StartPlayEs");
	FNetClient_StopPlay = (pNetClient_StopPlay)DemoExport(g_pvInstance, "NetClient_StopPlay");
	FNetClient_StopPlayEx = (pNetClient_StopPlayEx)DemoExport(g_pvInstance, "NetClient_StopPlayEx");
	FNetClient_SetPlayRectEx = (pNetClient_SetPlayRectEx)DemoExport(g_pvInstance, "NetClient_SetPlayRectEx");
	FNetClient_SetSrcRect = (pNetClient_SetSrcRect)DemoExport(g_pvInstance, "NetClient_SetSrcRect");
	FNetClient_ResetPlayerWnd = (pNetClient_ResetPlayerWnd)DemoExport(g_pvInstance, "NetClient_ResetPlayerWnd");
	FNetClient_GetPlayingStatus = (pNetClient_GetPlayingStatus)DemoExport(g_pvInstance, "NetClient_GetPlayingStatus");
	FNetClient_AdjustVideo = (pNetClient_AdjustVideo)DemoExport(g_pvInstance, "NetClient_AdjustVideo");
	FNetClient_AudioStart = (pNetClient_AudioStart)DemoExport(g_pvInstance, "NetClient_AudioStart");
	FNetClient_AudioStop = (pNetClient_AudioStop)DemoExport(g_pvInstance, "NetClient_AudioStop");
	FNetClient_SetLocalAudioVolume = (pNetClient_SetLocalAudioVolume)DemoExport(g_pvInstance, "NetClient_SetLocalAudioVolume");
	FNetClient_SetBufferNum = (pNetClient_SetBufferNum)DemoExport(g_pvInstance, "NetClient_SetBufferNum");
	FNetClient_SetPlayDelay = (pNetClient_SetPlayDelay)DemoExport(g_pvInstance, "NetClient_SetPlayDelay");
	FNetClient_GetChannelNum = (pNetClient_GetChannelNum)DemoExport(g_pvInstance, "NetClient_GetChannelNum");
	FNetClient_GetAlarmPortNum = (pNetClient_GetAlarmPortNum)DemoExport(g_pvInstance, "NetClient_GetAlarmPortNum");
	FNetClient_GetLocalAlarmNum = (pNetClient_GetLocalAlarmNum)DemoExport(g_pvInstance, "NetClient_GetLocalAlarmNum");
	FNetClient_SetVideoPara = (pNetClient_SetVideoPara)DemoExport(g_pvInstance, "NetClient_SetVideoPara");
	FNetClient_GetVideoPara = (pNetClient_GetVideoPara)DemoExport(g_pvInstance, "NetClient_GetVideoPara");
	FNetClient_SetVideoparaSchedule = (pNetClient_SetVideoparaSchedule)DemoExport(g_pvInstance, "NetClient_SetVideoparaSchedule");
	FNetClient_GetVideoparaSchedule = (pNetClient_GetVideoparaSchedule)DemoExport(g_pvInstance, "NetClient_GetVideoparaSchedule");
	FNetClient_SetVideoQuality = (pNetClient_SetVideoQuality)DemoExport(g_pvInstance, "NetClient_SetVideoQuality");
	FNetClient_GetVideoQuality = (pNetClient_GetVideoQuality)DemoExport(g_pvInstance, "NetClient_GetVideoQuality");
	FNetClient_SetFrameRate = (pNetClient_SetFrameRate)DemoExport(g_pvInstance, "NetClient_SetFrameRate");
	FNetClient_GetFrameRate = (pNetClient_GetFrameRate)DemoExport(g_pvInstance, "NetClient_GetFrameRate");
	FNetClient_GetDecordFrameNum  = (pNetClient_GetDecordFrameNum )DemoExport(g_pvInstance, "NetClient_GetDecordFrameNum ");
	FNetClient_SetStreamType = (pNetClient_SetStreamType)DemoExport(g_pvInstance, "NetClient_SetStreamType");
	FNetClient_GetStreamType = (pNetClient_GetStreamType)DemoExport(g_pvInstance, "NetClient_GetStreamType");
	FNetClient_SetMotionAreaEnable  = (pNetClient_SetMotionAreaEnable )DemoExport(g_pvInstance, "NetClient_SetMotionAreaEnable ");
	FNetClient_SetMotionDetetionArea = (pNetClient_SetMotionDetetionArea)DemoExport(g_pvInstance, "NetClient_SetMotionDetetionArea");
	FNetClient_GetMotionDetetionArea = (pNetClient_GetMotionDetetionArea)DemoExport(g_pvInstance, "NetClient_GetMotionDetetionArea");
	FNetClient_SetThreshold = (pNetClient_SetThreshold)DemoExport(g_pvInstance, "NetClient_SetThreshold");
	FNetClient_GetThreshold = (pNetClient_GetThreshold)DemoExport(g_pvInstance, "NetClient_GetThreshold");
	FNetClient_SetMotionDetection = (pNetClient_SetMotionDetection)DemoExport(g_pvInstance, "NetClient_SetMotionDetection");
	FNetClient_GetMotionDetection = (pNetClient_GetMotionDetection)DemoExport(g_pvInstance, "NetClient_GetMotionDetection");
	FNetClient_SetMotionDecLinkRec = (pNetClient_SetMotionDecLinkRec)DemoExport(g_pvInstance, "NetClient_SetMotionDecLinkRec");
	FNetClient_GetMotionDecLinkRec = (pNetClient_GetMotionDecLinkRec)DemoExport(g_pvInstance, "NetClient_GetMotionDecLinkRec");
	FNetClient_SetMotionDecLinkSnap = (pNetClient_SetMotionDecLinkSnap)DemoExport(g_pvInstance, "NetClient_SetMotionDecLinkSnap");
	FNetClient_GetMotionDecLinkSnap = (pNetClient_GetMotionDecLinkSnap)DemoExport(g_pvInstance, "NetClient_GetMotionDecLinkSnap");
	FNetClient_SetMotionDecLinkSoundDisplay = (pNetClient_SetMotionDecLinkSoundDisplay)DemoExport(g_pvInstance, "NetClient_SetMotionDecLinkSoundDisplay");
	FNetClient_GetMotionDecLinkSoundDisplay = (pNetClient_GetMotionDecLinkSoundDisplay)DemoExport(g_pvInstance, "NetClient_GetMotionDecLinkSoundDisplay");
	FNetClient_SetMotionDecLinkOutport = (pNetClient_SetMotionDecLinkOutport)DemoExport(g_pvInstance, "NetClient_SetMotionDecLinkOutport");
	FNetClient_GetMotionDecLinkOutport = (pNetClient_GetMotionDecLinkOutport)DemoExport(g_pvInstance, "NetClient_GetMotionDecLinkOutport");
	FNetClient_SetMotionDetectSchedule = (pNetClient_SetMotionDetectSchedule)DemoExport(g_pvInstance, "NetClient_SetMotionDetectSchedule");
	FNetClient_GetMotionDetectSchedule = (pNetClient_GetMotionDetectSchedule)DemoExport(g_pvInstance, "NetClient_GetMotionDetectSchedule");
	FNetClient_SetOsdDiaphaneity = (pNetClient_SetOsdDiaphaneity)DemoExport(g_pvInstance, "NetClient_SetOsdDiaphaneity");
	FNetClient_GetOsdDiaphaneity = (pNetClient_GetOsdDiaphaneity)DemoExport(g_pvInstance, "NetClient_GetOsdDiaphaneity");
	FNetClient_SetOsdText = (pNetClient_SetOsdText)DemoExport(g_pvInstance, "NetClient_SetOsdText");
	FNetClient_GetOsdText = (pNetClient_GetOsdText)DemoExport(g_pvInstance, "NetClient_GetOsdText");
	FNetClient_SetOsdType = (pNetClient_SetOsdType)DemoExport(g_pvInstance, "NetClient_SetOsdType");
	FNetClient_GetOsdType = (pNetClient_GetOsdType)DemoExport(g_pvInstance, "NetClient_GetOsdType");
	FNetClient_SetDateFormat = (pNetClient_SetDateFormat)DemoExport(g_pvInstance, "NetClient_SetDateFormat");
	FNetClient_GetDateFormat = (pNetClient_GetDateFormat)DemoExport(g_pvInstance, "NetClient_GetDateFormat");
	FNetClient_SetOsdLOGO  = (pNetClient_SetOsdLOGO )DemoExport(g_pvInstance, "NetClient_SetOsdLOGO ");
	FNetClient_SetAudioChannel = (pNetClient_SetAudioChannel)DemoExport(g_pvInstance, "NetClient_SetAudioChannel");
	FNetClient_GetAudioChannel = (pNetClient_GetAudioChannel)DemoExport(g_pvInstance, "NetClient_GetAudioChannel");
	FNetClient_SetIpFilter = (pNetClient_SetIpFilter)DemoExport(g_pvInstance, "NetClient_SetIpFilter");
	FNetClient_GetIpFilter = (pNetClient_GetIpFilter)DemoExport(g_pvInstance, "NetClient_GetIpFilter");
	FNetClient_SetAlarmOutput = (pNetClient_SetAlarmOutput)DemoExport(g_pvInstance, "NetClient_SetAlarmOutput");
	FNetClient_GetAlarmOutput = (pNetClient_GetAlarmOutput)DemoExport(g_pvInstance, "NetClient_GetAlarmOutput");
	FNetClient_GetAlarmIPortState = (pNetClient_GetAlarmIPortState)DemoExport(g_pvInstance, "NetClient_GetAlarmIPortState");
	FNetClient_SetAlarmPortEnable = (pNetClient_SetAlarmPortEnable)DemoExport(g_pvInstance, "NetClient_SetAlarmPortEnable");
	FNetClient_GetAlarmPortEnable = (pNetClient_GetAlarmPortEnable)DemoExport(g_pvInstance, "NetClient_GetAlarmPortEnable");
	FNetClient_SetInportAlarmLinkRec = (pNetClient_SetInportAlarmLinkRec)DemoExport(g_pvInstance, "NetClient_SetInportAlarmLinkRec");
	FNetClient_GetInportAlarmLinkRec = (pNetClient_GetInportAlarmLinkRec)DemoExport(g_pvInstance, "NetClient_GetInportAlarmLinkRec");
	FNetClient_SetInportAlarmLinkSnap = (pNetClient_SetInportAlarmLinkSnap)DemoExport(g_pvInstance, "NetClient_SetInportAlarmLinkSnap");
	FNetClient_GetInportAlarmLinkSnap = (pNetClient_GetInportAlarmLinkSnap)DemoExport(g_pvInstance, "NetClient_GetInportAlarmLinkSnap");
	FNetClient_SetInportAlarmLinkPTZ = (pNetClient_SetInportAlarmLinkPTZ)DemoExport(g_pvInstance, "NetClient_SetInportAlarmLinkPTZ");
	FNetClient_GetInportAlarmLinkPTZ = (pNetClient_GetInportAlarmLinkPTZ)DemoExport(g_pvInstance, "NetClient_GetInportAlarmLinkPTZ");
	FNetClient_SetInportAlarmLinkSoundDisplay = (pNetClient_SetInportAlarmLinkSoundDisplay)DemoExport(g_pvInstance, "NetClient_SetInportAlarmLinkSoundDisplay");
	FNetClient_GetInportAlarmLinkSoundDisplay = (pNetClient_GetInportAlarmLinkSoundDisplay)DemoExport(g_pvInstance, "NetClient_GetInportAlarmLinkSoundDisplay");
	FNetClient_SetInportAlmLinkOutport = (pNetClient_SetInportAlmLinkOutport)DemoExport(g_pvInstance, "NetClient_SetInportAlmLinkOutport");
	FNetClient_GetInportAlmLinkOutport = (pNetClient_GetInportAlmLinkOutport)DemoExport(g_pvInstance, "NetClient_GetInportAlmLinkOutport");
	FNetClient_SetAlarmInMode = (pNetClient_SetAlarmInMode)DemoExport(g_pvInstance, "NetClient_SetAlarmInMode");
	FNetClient_GetAlarmInMode = (pNetClient_GetAlarmInMode)DemoExport(g_pvInstance, "NetClient_GetAlarmInMode");
	FNetClient_SetAlarmOutMode = (pNetClient_SetAlarmOutMode)DemoExport(g_pvInstance, "NetClient_SetAlarmOutMode");
	FNetClient_GetAlarmOutMode = (pNetClient_GetAlarmOutMode)DemoExport(g_pvInstance, "NetClient_GetAlarmOutMode");
	FNetClient_SetInportAlarmSchedule = (pNetClient_SetInportAlarmSchedule)DemoExport(g_pvInstance, "NetClient_SetInportAlarmSchedule");
	FNetClient_GetInportAlarmSchedule = (pNetClient_GetInportAlarmSchedule)DemoExport(g_pvInstance, "NetClient_GetInportAlarmSchedule");
	FNetClient_SetOutportAlarmSchedule = (pNetClient_SetOutportAlarmSchedule)DemoExport(g_pvInstance, "NetClient_SetOutportAlarmSchedule");
	FNetClient_GetOutportAlarmSchedule = (pNetClient_GetOutportAlarmSchedule)DemoExport(g_pvInstance, "NetClient_GetOutportAlarmSchedule");
	FNetClient_SetOutportAlarmDelay = (pNetClient_SetOutportAlarmDelay)DemoExport(g_pvInstance, "NetClient_SetOutportAlarmDelay");
	FNetClient_GetOutportAlarmDelay = (pNetClient_GetOutportAlarmDelay)DemoExport(g_pvInstance, "NetClient_GetOutportAlarmDelay");
	FNetClient_SetInportEnable = (pNetClient_SetInportEnable)DemoExport(g_pvInstance, "NetClient_SetInportEnable");
	FNetClient_GetInportEnable = (pNetClient_GetInportEnable)DemoExport(g_pvInstance, "NetClient_GetInportEnable");
	FNetClient_SetOutportEnable = (pNetClient_SetOutportEnable)DemoExport(g_pvInstance, "NetClient_SetOutportEnable");
	FNetClient_GetOutportEnable = (pNetClient_GetOutportEnable)DemoExport(g_pvInstance, "NetClient_GetOutportEnable");
	FNetClient_SetOutportState = (pNetClient_SetOutportState)DemoExport(g_pvInstance, "NetClient_SetOutportState");
	FNetClient_GetOutportState = (pNetClient_GetOutportState)DemoExport(g_pvInstance, "NetClient_GetOutportState");
	FNetClient_SetAlmVdoCovThreshold = (pNetClient_SetAlmVdoCovThreshold)DemoExport(g_pvInstance, "NetClient_SetAlmVdoCovThreshold");
	FNetClient_GetAlmVdoCovThreshold = (pNetClient_GetAlmVdoCovThreshold)DemoExport(g_pvInstance, "NetClient_GetAlmVdoCovThreshold");
	FNetClient_SetAlmVideoCov = (pNetClient_SetAlmVideoCov)DemoExport(g_pvInstance, "NetClient_SetAlmVideoCov");
	FNetClient_GetAlmVideoCov = (pNetClient_GetAlmVideoCov)DemoExport(g_pvInstance, "NetClient_GetAlmVideoCov");
	FNetClient_SetDeviceType = (pNetClient_SetDeviceType)DemoExport(g_pvInstance, "NetClient_SetDeviceType");
	FNetClient_GetDeviceType = (pNetClient_GetDeviceType)DemoExport(g_pvInstance, "NetClient_GetDeviceType");
	FNetClient_SetComFormat = (pNetClient_SetComFormat)DemoExport(g_pvInstance, "NetClient_SetComFormat");
	FNetClient_GetComFormat = (pNetClient_GetComFormat)DemoExport(g_pvInstance, "NetClient_GetComFormat");
	FNetClient_GetAllSupportDeviceType = (pNetClient_GetAllSupportDeviceType)DemoExport(g_pvInstance, "NetClient_GetAllSupportDeviceType");
	FNetClient_DeviceCtrl = (pNetClient_DeviceCtrl)DemoExport(g_pvInstance, "NetClient_DeviceCtrl");
	FNetClient_DeviceCtrlEx = (pNetClient_DeviceCtrlEx)DemoExport(g_pvInstance, "NetClient_DeviceCtrlEx");
	FNetClient_ComSend = (pNetClient_ComSend)DemoExport(g_pvInstance, "NetClient_ComSend");
	FNetClient_DevicePTZCtrl = (pNetClient_DevicePTZCtrl)DemoExport(g_pvInstance, "NetClient_DevicePTZCtrl");
	FNetClient_GetComPortCounts = (pNetClient_GetComPortCounts)DemoExport(g_pvInstance, "NetClient_GetComPortCounts");
	FNetClient_SetAlarmVideoLost = (pNetClient_SetAlarmVideoLost)DemoExport(g_pvInstance, "NetClient_SetAlarmVideoLost");
	FNetClient_GetAlarmVideoLost = (pNetClient_GetAlarmVideoLost)DemoExport(g_pvInstance, "NetClient_GetAlarmVideoLost");
	FNetClient_SetVideoLostLinkPTZ = (pNetClient_SetVideoLostLinkPTZ)DemoExport(g_pvInstance, "NetClient_SetVideoLostLinkPTZ");
	FNetClient_GetVideoLostLinkPTZ = (pNetClient_GetVideoLostLinkPTZ)DemoExport(g_pvInstance, "NetClient_GetVideoLostLinkPTZ");
	FNetClient_SetVideoLostLinkSoundDisplay = (pNetClient_SetVideoLostLinkSoundDisplay)DemoExport(g_pvInstance, "NetClient_SetVideoLostLinkSoundDisplay");
	FNetClient_GetVideoLostLinkSoundDisplay = (pNetClient_GetVideoLostLinkSoundDisplay)DemoExport(g_pvInstance, "NetClient_GetVideoLostLinkSoundDisplay");
	FNetClient_GetAlarmVLostState = (pNetClient_GetAlarmVLostState)DemoExport(g_pvInstance, "NetClient_GetAlarmVLostState");
	FNetClient_SetVideoLostLinkOutport = (pNetClient_SetVideoLostLinkOutport)DemoExport(g_pvInstance, "NetClient_SetVideoLostLinkOutport");
	FNetClient_GetVideoLostLinkOutport = (pNetClient_GetVideoLostLinkOutport)DemoExport(g_pvInstance, "NetClient_GetVideoLostLinkOutport");
	FNetClient_SetVideoLostSchedule = (pNetClient_SetVideoLostSchedule)DemoExport(g_pvInstance, "NetClient_SetVideoLostSchedule");
	FNetClient_GetVideoLostSchedule = (pNetClient_GetVideoLostSchedule)DemoExport(g_pvInstance, "NetClient_GetVideoLostSchedule");
	FNetClient_SetVideoLostLinkRec = (pNetClient_SetVideoLostLinkRec)DemoExport(g_pvInstance, "NetClient_SetVideoLostLinkRec");
	FNetClient_GetVideoLostLinkRec = (pNetClient_GetVideoLostLinkRec)DemoExport(g_pvInstance, "NetClient_GetVideoLostLinkRec");
	FNetClient_SetVideoLostLinkSnap = (pNetClient_SetVideoLostLinkSnap)DemoExport(g_pvInstance, "NetClient_SetVideoLostLinkSnap");
	FNetClient_GetVideoLostLinkSnap = (pNetClient_GetVideoLostLinkSnap)DemoExport(g_pvInstance, "NetClient_GetVideoLostLinkSnap");
	FNetClient_SetIFrameRate = (pNetClient_SetIFrameRate)DemoExport(g_pvInstance, "NetClient_SetIFrameRate");
	FNetClient_GetIFrameRate = (pNetClient_GetIFrameRate)DemoExport(g_pvInstance, "NetClient_GetIFrameRate");
	FNetClient_ForceIFrame = (pNetClient_ForceIFrame)DemoExport(g_pvInstance, "NetClient_ForceIFrame");
	FNetClient_SetTime = (pNetClient_SetTime)DemoExport(g_pvInstance, "NetClient_SetTime");
	FNetClient_Reboot = (pNetClient_Reboot)DemoExport(g_pvInstance, "NetClient_Reboot");
	FNetClient_RebootEx = (pNetClient_RebootEx)DemoExport(g_pvInstance, "NetClient_RebootEx");
	FNetClient_DefaultPara = (pNetClient_DefaultPara)DemoExport(g_pvInstance, "NetClient_DefaultPara");
	FNetClient_DefaultParaEx = (pNetClient_DefaultParaEx)DemoExport(g_pvInstance, "NetClient_DefaultParaEx");
	FNetClient_GetServerVersion = (pNetClient_GetServerVersion)DemoExport(g_pvInstance, "NetClient_GetServerVersion");
	FNetClient_SetNVS = (pNetClient_SetNVS)DemoExport(g_pvInstance, "NetClient_SetNVS");
	FNetClient_UpgradeProgram = (pNetClient_UpgradeProgram)DemoExport(g_pvInstance, "NetClient_UpgradeProgram");
	FNetClient_UpgradeWebPage = (pNetClient_UpgradeWebPage)DemoExport(g_pvInstance, "NetClient_UpgradeWebPage");
	FNetClient_GetUpgradePercent = (pNetClient_GetUpgradePercent)DemoExport(g_pvInstance, "NetClient_GetUpgradePercent");
	FNetClient_GetUserNum = (pNetClient_GetUserNum)DemoExport(g_pvInstance, "NetClient_GetUserNum");
	FNetClient_GetUserInfo = (pNetClient_GetUserInfo)DemoExport(g_pvInstance, "NetClient_GetUserInfo");
	FNetClient_GetCurUserInfo = (pNetClient_GetCurUserInfo)DemoExport(g_pvInstance, "NetClient_GetCurUserInfo");
	FNetClient_AddUser = (pNetClient_AddUser)DemoExport(g_pvInstance, "NetClient_AddUser");
	FNetClient_DelUser = (pNetClient_DelUser)DemoExport(g_pvInstance, "NetClient_DelUser");
	FNetClient_ModifyPwd = (pNetClient_ModifyPwd)DemoExport(g_pvInstance, "NetClient_ModifyPwd");
	FNetClient_SetMaxConUser = (pNetClient_SetMaxConUser)DemoExport(g_pvInstance, "NetClient_SetMaxConUser");
	FNetClient_GetMaxGetUser = (pNetClient_GetMaxGetUser)DemoExport(g_pvInstance, "NetClient_GetMaxGetUser");
	FNetClient_TalkStart = (pNetClient_TalkStart)DemoExport(g_pvInstance, "NetClient_TalkStart");
	FNetClient_TalkEnd = (pNetClient_TalkEnd)DemoExport(g_pvInstance, "NetClient_TalkEnd");
	FNetClient_InputTalkingdata = (pNetClient_InputTalkingdata)DemoExport(g_pvInstance, "NetClient_InputTalkingdata");
	FNetClient_GetTalkingState = (pNetClient_GetTalkingState)DemoExport(g_pvInstance, "NetClient_GetTalkingState");
	FNetClient_CapturePic = (pNetClient_CapturePic)DemoExport(g_pvInstance, "NetClient_CapturePic");
	FNetClient_CaptureBmpPic = (pNetClient_CaptureBmpPic)DemoExport(g_pvInstance, "NetClient_CaptureBmpPic");
	FNetClient_ChangeSvrIP = (pNetClient_ChangeSvrIP)DemoExport(g_pvInstance, "NetClient_ChangeSvrIP");
	FNetClient_GetIpProperty = (pNetClient_GetIpProperty)DemoExport(g_pvInstance, "NetClient_GetIpProperty");
	FNetClient_SetDHCPParam = (pNetClient_SetDHCPParam)DemoExport(g_pvInstance, "NetClient_SetDHCPParam");
	FNetClient_GetDHCPParam = (pNetClient_GetDHCPParam)DemoExport(g_pvInstance, "NetClient_GetDHCPParam");
	FNetClient_SetWifiDHCPParam = (pNetClient_SetWifiDHCPParam)DemoExport(g_pvInstance, "NetClient_SetWifiDHCPParam");
	FNetClient_GetWifiDHCPParam = (pNetClient_GetWifiDHCPParam)DemoExport(g_pvInstance, "NetClient_GetWifiDHCPParam");
	FNetClient_GetVideoCovArea = (pNetClient_GetVideoCovArea)DemoExport(g_pvInstance, "NetClient_GetVideoCovArea");
	FNetClient_SetVideoCovArea = (pNetClient_SetVideoCovArea)DemoExport(g_pvInstance, "NetClient_SetVideoCovArea");
	FNetClient_GetVideoSize = (pNetClient_GetVideoSize)DemoExport(g_pvInstance, "NetClient_GetVideoSize");
	FNetClient_SetVideoSize = (pNetClient_SetVideoSize)DemoExport(g_pvInstance, "NetClient_SetVideoSize");
	FNetClient_GetVideoSizeEx = (pNetClient_GetVideoSizeEx)DemoExport(g_pvInstance, "NetClient_GetVideoSizeEx");
	FNetClient_GetMaxMinorVideoSize = (pNetClient_GetMaxMinorVideoSize)DemoExport(g_pvInstance, "NetClient_GetMaxMinorVideoSize");
	FNetClient_BindInterface = (pNetClient_BindInterface)DemoExport(g_pvInstance, "NetClient_BindInterface");
	FNetClient_GetNetInterface = (pNetClient_GetNetInterface)DemoExport(g_pvInstance, "NetClient_GetNetInterface");
	FNetClient_SetMaxKByteRate = (pNetClient_SetMaxKByteRate)DemoExport(g_pvInstance, "NetClient_SetMaxKByteRate");
	FNetClient_GetMaxKByteRate = (pNetClient_GetMaxKByteRate)DemoExport(g_pvInstance, "NetClient_GetMaxKByteRate");
	FNetClient_WriteUserData = (pNetClient_WriteUserData)DemoExport(g_pvInstance, "NetClient_WriteUserData");
	FNetClient_ReadUserData = (pNetClient_ReadUserData)DemoExport(g_pvInstance, "NetClient_ReadUserData");
	FNetClient_SetReducenoiseState = (pNetClient_SetReducenoiseState)DemoExport(g_pvInstance, "NetClient_SetReducenoiseState");
	FNetClient_GetReducenoiseState = (pNetClient_GetReducenoiseState)DemoExport(g_pvInstance, "NetClient_GetReducenoiseState");
	FNetClient_DrawTextOnVideo = (pNetClient_DrawTextOnVideo)DemoExport(g_pvInstance, "NetClient_DrawTextOnVideo");
	FNetClient_GetTextOnVideo = (pNetClient_GetTextOnVideo)DemoExport(g_pvInstance, "NetClient_GetTextOnVideo");
	FNetClient_SetBothStreamSame = (pNetClient_SetBothStreamSame)DemoExport(g_pvInstance, "NetClient_SetBothStreamSame");
	FNetClient_GetBothStreamSame = (pNetClient_GetBothStreamSame)DemoExport(g_pvInstance, "NetClient_GetBothStreamSame");
	FNetClient_ShowBitrateOnVideo = (pNetClient_ShowBitrateOnVideo)DemoExport(g_pvInstance, "NetClient_ShowBitrateOnVideo");
	FNetClient_SetPPPoEInfo = (pNetClient_SetPPPoEInfo)DemoExport(g_pvInstance, "NetClient_SetPPPoEInfo");
	FNetClient_GetPPPoEInfo = (pNetClient_GetPPPoEInfo)DemoExport(g_pvInstance, "NetClient_GetPPPoEInfo");
	FNetClient_CPUCheckEnabled = (pNetClient_CPUCheckEnabled)DemoExport(g_pvInstance, "NetClient_CPUCheckEnabled");
	FNetClient_SetEncodeMode = (pNetClient_SetEncodeMode)DemoExport(g_pvInstance, "NetClient_SetEncodeMode");
	FNetClient_GetEncodeMode = (pNetClient_GetEncodeMode)DemoExport(g_pvInstance, "NetClient_GetEncodeMode");
	FNetClient_SetPreferMode = (pNetClient_SetPreferMode)DemoExport(g_pvInstance, "NetClient_SetPreferMode");
	FNetClient_GetPreferMode = (pNetClient_GetPreferMode)DemoExport(g_pvInstance, "NetClient_GetPreferMode");
	FNetClient_LogFileSetProperty = (pNetClient_LogFileSetProperty)DemoExport(g_pvInstance, "NetClient_LogFileSetProperty");
	FNetClient_LogFileGetProperty = (pNetClient_LogFileGetProperty)DemoExport(g_pvInstance, "NetClient_LogFileGetProperty");
	FNetClient_LogFileDownload = (pNetClient_LogFileDownload)DemoExport(g_pvInstance, "NetClient_LogFileDownload");
	FNetClient_LogFileClear = (pNetClient_LogFileClear)DemoExport(g_pvInstance, "NetClient_LogFileClear");
	FNetClient_LogFileGetDetails = (pNetClient_LogFileGetDetails)DemoExport(g_pvInstance, "NetClient_LogFileGetDetails");
	FNetClient_GetVideoNPMode = (pNetClient_GetVideoNPMode)DemoExport(g_pvInstance, "NetClient_GetVideoNPMode");
	FNetClient_SetVideoNPMode = (pNetClient_SetVideoNPMode)DemoExport(g_pvInstance, "NetClient_SetVideoNPMode");
	FNetClient_SetAudioEncoder = (pNetClient_SetAudioEncoder)DemoExport(g_pvInstance, "NetClient_SetAudioEncoder");
	FNetClient_GetAudioEncoder = (pNetClient_GetAudioEncoder)DemoExport(g_pvInstance, "NetClient_GetAudioEncoder");
	FNetClient_NetFileQuery = (pNetClient_NetFileQuery)DemoExport(g_pvInstance, "NetClient_NetFileQuery");
	FNetClient_NetFileSetRecordRule = (pNetClient_NetFileSetRecordRule)DemoExport(g_pvInstance, "NetClient_NetFileSetRecordRule");
	FNetClient_NetFileGetRecordRule = (pNetClient_NetFileGetRecordRule)DemoExport(g_pvInstance, "NetClient_NetFileGetRecordRule");
	FNetClient_NetFileSetAlarmRule = (pNetClient_NetFileSetAlarmRule)DemoExport(g_pvInstance, "NetClient_NetFileSetAlarmRule");
	FNetClient_NetFileGetAlarmRule = (pNetClient_NetFileGetAlarmRule)DemoExport(g_pvInstance, "NetClient_NetFileGetAlarmRule");
	FNetClient_NetFileSetAlarmState = (pNetClient_NetFileSetAlarmState)DemoExport(g_pvInstance, "NetClient_NetFileSetAlarmState");
	FNetClient_NetFileGetAlarmState = (pNetClient_NetFileGetAlarmState)DemoExport(g_pvInstance, "NetClient_NetFileGetAlarmState");
	FNetClient_NetFileSetTaskState = (pNetClient_NetFileSetTaskState)DemoExport(g_pvInstance, "NetClient_NetFileSetTaskState");
	FNetClient_NetFileGetTaskState = (pNetClient_NetFileGetTaskState)DemoExport(g_pvInstance, "NetClient_NetFileGetTaskState");
	FNetClient_NetFileSetTaskSchedule = (pNetClient_NetFileSetTaskSchedule)DemoExport(g_pvInstance, "NetClient_NetFileSetTaskSchedule");
	FNetClient_NetFileGetTaskSchedule = (pNetClient_NetFileGetTaskSchedule)DemoExport(g_pvInstance, "NetClient_NetFileGetTaskSchedule");
	FNetClient_NetFileSetTaskScheduleEx = (pNetClient_NetFileSetTaskScheduleEx)DemoExport(g_pvInstance, "NetClient_NetFileSetTaskScheduleEx");
	FNetClient_NetFileGetTaskScheduleEx = (pNetClient_NetFileGetTaskScheduleEx)DemoExport(g_pvInstance, "NetClient_NetFileGetTaskScheduleEx");
	FNetClient_NetFileGetFileCount = (pNetClient_NetFileGetFileCount)DemoExport(g_pvInstance, "NetClient_NetFileGetFileCount");
	FNetClient_NetFileRebuildIndexFile = (pNetClient_NetFileRebuildIndexFile)DemoExport(g_pvInstance, "NetClient_NetFileRebuildIndexFile");
	FNetClient_NetFileGetDiskInfo = (pNetClient_NetFileGetDiskInfo)DemoExport(g_pvInstance, "NetClient_NetFileGetDiskInfo");
	FNetClient_NetFileGetDiskInfoEx = (pNetClient_NetFileGetDiskInfoEx)DemoExport(g_pvInstance, "NetClient_NetFileGetDiskInfoEx");
	FNetClient_NetFileIsSupportStorage = (pNetClient_NetFileIsSupportStorage)DemoExport(g_pvInstance, "NetClient_NetFileIsSupportStorage");
	FNetClient_NetFileDownloadFile = (pNetClient_NetFileDownloadFile)DemoExport(g_pvInstance, "NetClient_NetFileDownloadFile");
	FNetClient_NetFileDownloadFileEx = (pNetClient_NetFileDownloadFileEx)DemoExport(g_pvInstance, "NetClient_NetFileDownloadFileEx");
	FNetClient_NetFileStopDownloadFile = (pNetClient_NetFileStopDownloadFile)DemoExport(g_pvInstance, "NetClient_NetFileStopDownloadFile");
	FNetClient_NetFileGetDownloadPos = (pNetClient_NetFileGetDownloadPos)DemoExport(g_pvInstance, "NetClient_NetFileGetDownloadPos");
	FNetClient_NetFileMountUSB = (pNetClient_NetFileMountUSB)DemoExport(g_pvInstance, "NetClient_NetFileMountUSB");
	FNetClient_NetFileGetRecordState = (pNetClient_NetFileGetRecordState)DemoExport(g_pvInstance, "NetClient_NetFileGetRecordState");
	FNetClient_NetFileDelFile = (pNetClient_NetFileDelFile)DemoExport(g_pvInstance, "NetClient_NetFileDelFile");
	FNetClient_DiskSetUsage = (pNetClient_DiskSetUsage)DemoExport(g_pvInstance, "NetClient_DiskSetUsage");
	FNetClient_NetFileGetQueryfile = (pNetClient_NetFileGetQueryfile)DemoExport(g_pvInstance, "NetClient_NetFileGetQueryfile");
	FNetClient_DiskFormat = (pNetClient_DiskFormat)DemoExport(g_pvInstance, "NetClient_DiskFormat");
	FNetClient_DiskPart = (pNetClient_DiskPart)DemoExport(g_pvInstance, "NetClient_DiskPart");
	FNetClient_NetFileManualRecord = (pNetClient_NetFileManualRecord)DemoExport(g_pvInstance, "NetClient_NetFileManualRecord");
	FNetClient_NetFileMapStoreDevice = (pNetClient_NetFileMapStoreDevice)DemoExport(g_pvInstance, "NetClient_NetFileMapStoreDevice");
	FNetClient_NetFileGetMapStoreDevice = (pNetClient_NetFileGetMapStoreDevice)DemoExport(g_pvInstance, "NetClient_NetFileGetMapStoreDevice");
	FNetClient_NetFileGetUSBstate = (pNetClient_NetFileGetUSBstate)DemoExport(g_pvInstance, "NetClient_NetFileGetUSBstate");
	FNetClient_NetFileSetExtendname = (pNetClient_NetFileSetExtendname)DemoExport(g_pvInstance, "NetClient_NetFileSetExtendname");
	FNetClient_NetFileGetExtendname = (pNetClient_NetFileGetExtendname)DemoExport(g_pvInstance, "NetClient_NetFileGetExtendname");
	FNetClient_ClearDisk = (pNetClient_ClearDisk)DemoExport(g_pvInstance, "NetClient_ClearDisk");
	FNetClient_GetDownloadFailedFileName = (pNetClient_GetDownloadFailedFileName)DemoExport(g_pvInstance, "NetClient_GetDownloadFailedFileName");
	FNetClient_SetMediaStreamClient = (pNetClient_SetMediaStreamClient)DemoExport(g_pvInstance, "NetClient_SetMediaStreamClient");
	FNetClient_GetMediaStreamClient = (pNetClient_GetMediaStreamClient)DemoExport(g_pvInstance, "NetClient_GetMediaStreamClient");
	FNetClient_SetEmailAlarm = (pNetClient_SetEmailAlarm)DemoExport(g_pvInstance, "NetClient_SetEmailAlarm");
	FNetClient_GetEmailAlarm = (pNetClient_GetEmailAlarm)DemoExport(g_pvInstance, "NetClient_GetEmailAlarm");
	FNetClient_SetEmailAlarmEnable = (pNetClient_SetEmailAlarmEnable)DemoExport(g_pvInstance, "NetClient_SetEmailAlarmEnable");
	FNetClient_GetEmailAlarmEnable = (pNetClient_GetEmailAlarmEnable)DemoExport(g_pvInstance, "NetClient_GetEmailAlarmEnable");
	FNetClient_SetScene = (pNetClient_SetScene)DemoExport(g_pvInstance, "NetClient_SetScene");
	FNetClient_GetScene = (pNetClient_GetScene)DemoExport(g_pvInstance, "NetClient_GetScene");
	FNetClient_SetSensorFlip = (pNetClient_SetSensorFlip)DemoExport(g_pvInstance, "NetClient_SetSensorFlip");
	FNetClient_GetSensorFlip = (pNetClient_GetSensorFlip)DemoExport(g_pvInstance, "NetClient_GetSensorFlip");
	FNetClient_SetSensorMirror = (pNetClient_SetSensorMirror)DemoExport(g_pvInstance, "NetClient_SetSensorMirror");
	FNetClient_GetSensorMirror = (pNetClient_GetSensorMirror)DemoExport(g_pvInstance, "NetClient_GetSensorMirror");
	FNetClient_Snapshot = (pNetClient_Snapshot)DemoExport(g_pvInstance, "NetClient_Snapshot");
	FNetClient_GetFactoryID = (pNetClient_GetFactoryID)DemoExport(g_pvInstance, "NetClient_GetFactoryID");
	FNetClient_SetWifiParam = (pNetClient_SetWifiParam)DemoExport(g_pvInstance, "NetClient_SetWifiParam");
	FNetClient_GetWifiParam = (pNetClient_GetWifiParam)DemoExport(g_pvInstance, "NetClient_GetWifiParam");
	FNetClient_WifiSearch = (pNetClient_WifiSearch)DemoExport(g_pvInstance, "NetClient_WifiSearch");
	FNetClient_GetWifiSearchResult = (pNetClient_GetWifiSearchResult)DemoExport(g_pvInstance, "NetClient_GetWifiSearchResult");
	FNetClient_SetPrivacyProtect = (pNetClient_SetPrivacyProtect)DemoExport(g_pvInstance, "NetClient_SetPrivacyProtect");
	FNetClient_GetPrivacyProtect = (pNetClient_GetPrivacyProtect)DemoExport(g_pvInstance, "NetClient_GetPrivacyProtect");
	FNetClient_IYUVtoYV12 = (pNetClient_IYUVtoYV12)DemoExport(g_pvInstance, "NetClient_IYUVtoYV12");
	FNetClient_GetDevType = (pNetClient_GetDevType)DemoExport(g_pvInstance, "NetClient_GetDevType");
	FNetClient_GetProductType = (pNetClient_GetProductType)DemoExport(g_pvInstance, "NetClient_GetProductType");
	FNetClient_GetProductTypeEx = (pNetClient_GetProductTypeEx)DemoExport(g_pvInstance, "NetClient_GetProductTypeEx");
	FNetClient_BackupKernel = (pNetClient_BackupKernel)DemoExport(g_pvInstance, "NetClient_BackupKernel");
	FNetClient_SetUPNPEnable = (pNetClient_SetUPNPEnable)DemoExport(g_pvInstance, "NetClient_SetUPNPEnable");
	FNetClient_GetUPNPEnable = (pNetClient_GetUPNPEnable)DemoExport(g_pvInstance, "NetClient_GetUPNPEnable");
	FNetClient_GetSysInfo = (pNetClient_GetSysInfo)DemoExport(g_pvInstance, "NetClient_GetSysInfo");
	FNetClient_SetDDNSPara = (pNetClient_SetDDNSPara)DemoExport(g_pvInstance, "NetClient_SetDDNSPara");
	FNetClient_GetDDNSPara = (pNetClient_GetDDNSPara)DemoExport(g_pvInstance, "NetClient_GetDDNSPara");
	FNetClient_SetFuncListArray = (pNetClient_SetFuncListArray)DemoExport(g_pvInstance, "NetClient_SetFuncListArray");
	FNetClient_GetFuncListArray = (pNetClient_GetFuncListArray)DemoExport(g_pvInstance, "NetClient_GetFuncListArray");
	FNetClient_SendStringToServer = (pNetClient_SendStringToServer)DemoExport(g_pvInstance, "NetClient_SendStringToServer");
	FNetClient_ReceiveString = (pNetClient_ReceiveString)DemoExport(g_pvInstance, "NetClient_ReceiveString");
	FNetClient_SendStringToCenter = (pNetClient_SendStringToCenter)DemoExport(g_pvInstance, "NetClient_SendStringToCenter");
	FNetClient_SetVencType = (pNetClient_SetVencType)DemoExport(g_pvInstance, "NetClient_SetVencType");
	FNetClient_GetVencType = (pNetClient_GetVencType)DemoExport(g_pvInstance, "NetClient_GetVencType");
	FNetClient_SetComServer = (pNetClient_SetComServer)DemoExport(g_pvInstance, "NetClient_SetComServer");
	FNetClient_GetComServer = (pNetClient_GetComServer)DemoExport(g_pvInstance, "NetClient_GetComServer");
	FNetClient_Get3GDeviceStatus = (pNetClient_Get3GDeviceStatus)DemoExport(g_pvInstance, "NetClient_Get3GDeviceStatus");
	FNetClient_Set3GDialog = (pNetClient_Set3GDialog)DemoExport(g_pvInstance, "NetClient_Set3GDialog");
	FNetClient_Get3GDialog = (pNetClient_Get3GDialog)DemoExport(g_pvInstance, "NetClient_Get3GDialog");
	FNetClient_Set3GMessage = (pNetClient_Set3GMessage)DemoExport(g_pvInstance, "NetClient_Set3GMessage");
	FNetClient_Get3GMessage = (pNetClient_Get3GMessage)DemoExport(g_pvInstance, "NetClient_Get3GMessage");
	FNetClient_Set3GTaskSchedule = (pNetClient_Set3GTaskSchedule)DemoExport(g_pvInstance, "NetClient_Set3GTaskSchedule");
	FNetClient_Get3GTaskSchedule = (pNetClient_Get3GTaskSchedule)DemoExport(g_pvInstance, "NetClient_Get3GTaskSchedule");
	FNetClient_Set3GNotify = (pNetClient_Set3GNotify)DemoExport(g_pvInstance, "NetClient_Set3GNotify");
	FNetClient_Get3GNotify = (pNetClient_Get3GNotify)DemoExport(g_pvInstance, "NetClient_Get3GNotify");
	FNetClient_SetHDCamer = (pNetClient_SetHDCamer)DemoExport(g_pvInstance, "NetClient_SetHDCamer");
	FNetClient_GetHDCamer = (pNetClient_GetHDCamer)DemoExport(g_pvInstance, "NetClient_GetHDCamer");
	FNetClient_SetAlarmServer = (pNetClient_SetAlarmServer)DemoExport(g_pvInstance, "NetClient_SetAlarmServer");
	FNetClient_GetAlarmServer = (pNetClient_GetAlarmServer)DemoExport(g_pvInstance, "NetClient_GetAlarmServer");
	FNetClient_InterTalkStart = (pNetClient_InterTalkStart)DemoExport(g_pvInstance, "NetClient_InterTalkStart");
	FNetClient_InterTalkEnd = (pNetClient_InterTalkEnd)DemoExport(g_pvInstance, "NetClient_InterTalkEnd");
	FNetClient_NetFileQueryEx = (pNetClient_NetFileQueryEx)DemoExport(g_pvInstance, "NetClient_NetFileQueryEx");
	FNetClient_ControlDeviceRecord = (pNetClient_ControlDeviceRecord)DemoExport(g_pvInstance, "NetClient_ControlDeviceRecord");
	FNetClient_NetFileDownloadByTimeSpan = (pNetClient_NetFileDownloadByTimeSpan)DemoExport(g_pvInstance, "NetClient_NetFileDownloadByTimeSpan");
	FNetClient_NetFileDownloadByTimeSpanEx = (pNetClient_NetFileDownloadByTimeSpanEx)DemoExport(g_pvInstance, "NetClient_NetFileDownloadByTimeSpanEx");
	FNetClient_NetFileDownloadByTimeSpanCallBack = (pNetClient_NetFileDownloadByTimeSpanCallBack)DemoExport(g_pvInstance, "NetClient_NetFileDownloadByTimeSpanCallBack");
	FNetClient_NetLogQuery = (pNetClient_NetLogQuery)DemoExport(g_pvInstance, "NetClient_NetLogQuery");
	FNetClient_NetLogGetLogfile = (pNetClient_NetLogGetLogfile)DemoExport(g_pvInstance, "NetClient_NetLogGetLogfile");
	FNetClient_NetLogGetLogCount = (pNetClient_NetLogGetLogCount)DemoExport(g_pvInstance, "NetClient_NetLogGetLogCount");
	FNetClient_GetProtocolList = (pNetClient_GetProtocolList)DemoExport(g_pvInstance, "NetClient_GetProtocolList");
	FNetClient_SetCHNPTZCRUISE = (pNetClient_SetCHNPTZCRUISE)DemoExport(g_pvInstance, "NetClient_SetCHNPTZCRUISE");
	FNetClient_GetCHNPTZCRUISE = (pNetClient_GetCHNPTZCRUISE)DemoExport(g_pvInstance, "NetClient_GetCHNPTZCRUISE");
	FNetClient_SetVIDEOCOVER_LINKRECORD = (pNetClient_SetVIDEOCOVER_LINKRECORD)DemoExport(g_pvInstance, "NetClient_SetVIDEOCOVER_LINKRECORD");
	FNetClient_GetVIDEOCOVER_LINKRECORD = (pNetClient_GetVIDEOCOVER_LINKRECORD)DemoExport(g_pvInstance, "NetClient_GetVIDEOCOVER_LINKRECORD");
	FNetClient_SetVIDEOCOVER_LINKPTZ = (pNetClient_SetVIDEOCOVER_LINKPTZ)DemoExport(g_pvInstance, "NetClient_SetVIDEOCOVER_LINKPTZ");
	FNetClient_GetVIDEOCOVER_LINKPTZ = (pNetClient_GetVIDEOCOVER_LINKPTZ)DemoExport(g_pvInstance, "NetClient_GetVIDEOCOVER_LINKPTZ");
	FNetClient_GetAlarmVCoverState = (pNetClient_GetAlarmVCoverState)DemoExport(g_pvInstance, "NetClient_GetAlarmVCoverState");
	FNetClient_StopCaptureDate = (pNetClient_StopCaptureDate)DemoExport(g_pvInstance, "NetClient_StopCaptureDate");
	FNetClient_SetColorToGray = (pNetClient_SetColorToGray)DemoExport(g_pvInstance, "NetClient_SetColorToGray");
	FNetClient_GetColorToGray = (pNetClient_GetColorToGray)DemoExport(g_pvInstance, "NetClient_GetColorToGray");
	FNetClient_SetCustomChannelName = (pNetClient_SetCustomChannelName)DemoExport(g_pvInstance, "NetClient_SetCustomChannelName");
	FNetClient_GetCustomChannelName = (pNetClient_GetCustomChannelName)DemoExport(g_pvInstance, "NetClient_GetCustomChannelName");
	FNetClient_SetCustomRecType = (pNetClient_SetCustomRecType)DemoExport(g_pvInstance, "NetClient_SetCustomRecType");
	FNetClient_GetCustomRecType = (pNetClient_GetCustomRecType)DemoExport(g_pvInstance, "NetClient_GetCustomRecType");
	FNetClient_ChangeSvrIPEx = (pNetClient_ChangeSvrIPEx)DemoExport(g_pvInstance, "NetClient_ChangeSvrIPEx");
	FNetClient_GetIpPropertyEx = (pNetClient_GetIpPropertyEx)DemoExport(g_pvInstance, "NetClient_GetIpPropertyEx");
	FNetClient_SetFTPUpdate = (pNetClient_SetFTPUpdate)DemoExport(g_pvInstance, "NetClient_SetFTPUpdate");
	FNetClient_GetFTPUpdate = (pNetClient_GetFTPUpdate)DemoExport(g_pvInstance, "NetClient_GetFTPUpdate");
	FNetClient_SetCHNPTZFormat = (pNetClient_SetCHNPTZFormat)DemoExport(g_pvInstance, "NetClient_SetCHNPTZFormat");
	FNetClient_GetCHNPTZFormat = (pNetClient_GetCHNPTZFormat)DemoExport(g_pvInstance, "NetClient_GetCHNPTZFormat");
	FNetClient_GetServerVersionEx = (pNetClient_GetServerVersionEx)DemoExport(g_pvInstance, "NetClient_GetServerVersionEx");
	FNetClient_GetOSDTypeColor = (pNetClient_GetOSDTypeColor)DemoExport(g_pvInstance, "NetClient_GetOSDTypeColor");
	FNetClient_SetOSDTypeColor = (pNetClient_SetOSDTypeColor)DemoExport(g_pvInstance, "NetClient_SetOSDTypeColor");
	FNetClient_GetExceptionMsg = (pNetClient_GetExceptionMsg)DemoExport(g_pvInstance, "NetClient_GetExceptionMsg");
	FNetClient_SetNTPInfo = (pNetClient_SetNTPInfo)DemoExport(g_pvInstance, "NetClient_SetNTPInfo");
	FNetClient_GetNTPInfo = (pNetClient_GetNTPInfo)DemoExport(g_pvInstance, "NetClient_GetNTPInfo");
	FNetClient_SetVideoEncrypt = (pNetClient_SetVideoEncrypt)DemoExport(g_pvInstance, "NetClient_SetVideoEncrypt");
	FNetClient_GetVideoEncrypt = (pNetClient_GetVideoEncrypt)DemoExport(g_pvInstance, "NetClient_GetVideoEncrypt");
	FNetClient_SetVideoDecrypt = (pNetClient_SetVideoDecrypt)DemoExport(g_pvInstance, "NetClient_SetVideoDecrypt");
	FNetClient_GetVideoDecrypt = (pNetClient_GetVideoDecrypt)DemoExport(g_pvInstance, "NetClient_GetVideoDecrypt");
	FNetClient_SetPreRecEnable = (pNetClient_SetPreRecEnable)DemoExport(g_pvInstance, "NetClient_SetPreRecEnable");
	FNetClient_GetPreRecEnable = (pNetClient_GetPreRecEnable)DemoExport(g_pvInstance, "NetClient_GetPreRecEnable");
	FNetClient_SetVideoCombine = (pNetClient_SetVideoCombine)DemoExport(g_pvInstance, "NetClient_SetVideoCombine");
	FNetClient_GetVideoCombine = (pNetClient_GetVideoCombine)DemoExport(g_pvInstance, "NetClient_GetVideoCombine");
	FNetClient_VCASetConfig = (pNetClient_VCASetConfig)DemoExport(g_pvInstance, "NetClient_VCASetConfig");
	FNetClient_VCAGetConfig = (pNetClient_VCAGetConfig)DemoExport(g_pvInstance, "NetClient_VCAGetConfig");
	FNetClient_VCARestart = (pNetClient_VCARestart)DemoExport(g_pvInstance, "NetClient_VCARestart");
	FNetClient_VCARestartEx = (pNetClient_VCARestartEx)DemoExport(g_pvInstance, "NetClient_VCARestartEx");
	FNetClient_VCAGetAlarmInfo = (pNetClient_VCAGetAlarmInfo)DemoExport(g_pvInstance, "NetClient_VCAGetAlarmInfo");
	FNetClient_SetEmailAlarmEx = (pNetClient_SetEmailAlarmEx)DemoExport(g_pvInstance, "NetClient_SetEmailAlarmEx");
	FNetClient_GetEmailAlarmEx = (pNetClient_GetEmailAlarmEx)DemoExport(g_pvInstance, "NetClient_GetEmailAlarmEx");
	FNetClient_SetFTPUploadConfig = (pNetClient_SetFTPUploadConfig)DemoExport(g_pvInstance, "NetClient_SetFTPUploadConfig");
	FNetClient_GetFTPUploadConfig = (pNetClient_GetFTPUploadConfig)DemoExport(g_pvInstance, "NetClient_GetFTPUploadConfig");
	FNetClient_Set3GConfig = (pNetClient_Set3GConfig)DemoExport(g_pvInstance, "NetClient_Set3GConfig");
	FNetClient_Get3GConfig = (pNetClient_Get3GConfig)DemoExport(g_pvInstance, "NetClient_Get3GConfig");
	FNetClient_SetDigitalChannelConfig = (pNetClient_SetDigitalChannelConfig)DemoExport(g_pvInstance, "NetClient_SetDigitalChannelConfig");
	FNetClient_GetDigitalChannelConfig = (pNetClient_GetDigitalChannelConfig)DemoExport(g_pvInstance, "NetClient_GetDigitalChannelConfig");
	FNetClient_DigitalChannelSend = (pNetClient_DigitalChannelSend)DemoExport(g_pvInstance, "NetClient_DigitalChannelSend");
	FNetClient_SendComData = (pNetClient_SendComData)DemoExport(g_pvInstance, "NetClient_SendComData");
	FNetClient_SetVideoNPModeEx = (pNetClient_SetVideoNPModeEx)DemoExport(g_pvInstance, "NetClient_SetVideoNPModeEx");
	FNetClient_GetVideoNPModeEx = (pNetClient_GetVideoNPModeEx)DemoExport(g_pvInstance, "NetClient_GetVideoNPModeEx");
	FNetClient_GetDigitalChannelNum = (pNetClient_GetDigitalChannelNum)DemoExport(g_pvInstance, "NetClient_GetDigitalChannelNum");
	FNetClient_GetChannelProperty = (pNetClient_GetChannelProperty)DemoExport(g_pvInstance, "NetClient_GetChannelProperty");
	FNetClient_SetDeviceTimerReboot = (pNetClient_SetDeviceTimerReboot)DemoExport(g_pvInstance, "NetClient_SetDeviceTimerReboot");
	FNetClient_GetDeviceTimerReboot = (pNetClient_GetDeviceTimerReboot)DemoExport(g_pvInstance, "NetClient_GetDeviceTimerReboot");
	FNetClient_SetVideoCoverSchedule = (pNetClient_SetVideoCoverSchedule)DemoExport(g_pvInstance, "NetClient_SetVideoCoverSchedule");
	FNetClient_GetVideoCoverSchedule = (pNetClient_GetVideoCoverSchedule)DemoExport(g_pvInstance, "NetClient_GetVideoCoverSchedule");
	FNetClient_SetCPUMEMAlarmThreshold = (pNetClient_SetCPUMEMAlarmThreshold)DemoExport(g_pvInstance, "NetClient_SetCPUMEMAlarmThreshold");
	FNetClient_GetCPUMEMAlarmThreshold = (pNetClient_GetCPUMEMAlarmThreshold)DemoExport(g_pvInstance, "NetClient_GetCPUMEMAlarmThreshold");
	FNetClient_SetDZInfo = (pNetClient_SetDZInfo)DemoExport(g_pvInstance, "NetClient_SetDZInfo");
	FNetClient_GetDZInfo = (pNetClient_GetDZInfo)DemoExport(g_pvInstance, "NetClient_GetDZInfo");
	FNetClient_SetPTZAutoBack = (pNetClient_SetPTZAutoBack)DemoExport(g_pvInstance, "NetClient_SetPTZAutoBack");
	FNetClient_GetPTZAutoBack = (pNetClient_GetPTZAutoBack)DemoExport(g_pvInstance, "NetClient_GetPTZAutoBack");
	FNetClient_Set3GVPND = (pNetClient_Set3GVPND)DemoExport(g_pvInstance, "NetClient_Set3GVPND");
	FNetClient_Get3GVPND = (pNetClient_Get3GVPND)DemoExport(g_pvInstance, "NetClient_Get3GVPND");
	FNetClient_SetHDCamerEx = (pNetClient_SetHDCamerEx)DemoExport(g_pvInstance, "NetClient_SetHDCamerEx");
	FNetClient_GetHDCamerEx = (pNetClient_GetHDCamerEx)DemoExport(g_pvInstance, "NetClient_GetHDCamerEx");
	FNetClient_SetFTPUsage = (pNetClient_SetFTPUsage)DemoExport(g_pvInstance, "NetClient_SetFTPUsage");
	FNetClient_GetFTPUsage = (pNetClient_GetFTPUsage)DemoExport(g_pvInstance, "NetClient_GetFTPUsage");
	FNetClient_SetChannelSipConfig = (pNetClient_SetChannelSipConfig)DemoExport(g_pvInstance, "NetClient_SetChannelSipConfig");
	FNetClient_GetChannelSipConfig = (pNetClient_GetChannelSipConfig)DemoExport(g_pvInstance, "NetClient_GetChannelSipConfig");
	FNetClient_GetMaxVideoSize = (pNetClient_GetMaxVideoSize)DemoExport(g_pvInstance, "NetClient_GetMaxVideoSize");
	FNetClient_SetBitRatePercent = (pNetClient_SetBitRatePercent)DemoExport(g_pvInstance, "NetClient_SetBitRatePercent");
	FNetClient_GetBitRatePercent = (pNetClient_GetBitRatePercent)DemoExport(g_pvInstance, "NetClient_GetBitRatePercent");
	FNetClient_GetVideoParam = (pNetClient_GetVideoParam)DemoExport(g_pvInstance, "NetClient_GetVideoParam");
	FNetClient_SetOSDAlpha = (pNetClient_SetOSDAlpha)DemoExport(g_pvInstance, "NetClient_SetOSDAlpha");
	FNetClient_GetOSDAlpha = (pNetClient_GetOSDAlpha)DemoExport(g_pvInstance, "NetClient_GetOSDAlpha");
	FNetClient_DeviceSetup = (pNetClient_DeviceSetup)DemoExport(g_pvInstance, "NetClient_DeviceSetup");
	FNetClient_SetPlayerShowFrameMode = (pNetClient_SetPlayerShowFrameMode)DemoExport(g_pvInstance, "NetClient_SetPlayerShowFrameMode");
	FNetClient_GetPlayerShowFrameMode = (pNetClient_GetPlayerShowFrameMode)DemoExport(g_pvInstance, "NetClient_GetPlayerShowFrameMode");
	FNetClient_DrawRectOnLocalVideo = (pNetClient_DrawRectOnLocalVideo)DemoExport(g_pvInstance, "NetClient_DrawRectOnLocalVideo");
	FNetClient_DrawPolyOnLocalVideo = (pNetClient_DrawPolyOnLocalVideo)DemoExport(g_pvInstance, "NetClient_DrawPolyOnLocalVideo");
	FNetClient_SendStringToServerEx = (pNetClient_SendStringToServerEx)DemoExport(g_pvInstance, "NetClient_SendStringToServerEx");
	FNetClient_SetNetFileDownloadFileCallBack = (pNetClient_SetNetFileDownloadFileCallBack)DemoExport(g_pvInstance, "NetClient_SetNetFileDownloadFileCallBack");
	FNetClient_SetDataPackCallBack = (pNetClient_SetDataPackCallBack)DemoExport(g_pvInstance, "NetClient_SetDataPackCallBack");
	FNetClient_AddConnectionToNetWork = (pNetClient_AddConnectionToNetWork)DemoExport(g_pvInstance, "NetClient_AddConnectionToNetWork");
	FNetClient_MallocConnection = (pNetClient_MallocConnection)DemoExport(g_pvInstance, "NetClient_MallocConnection");
	FNetClient_FreeConnection = (pNetClient_FreeConnection)DemoExport(g_pvInstance, "NetClient_FreeConnection");
	FNetClient_NetFileSetChannelParam = (pNetClient_NetFileSetChannelParam)DemoExport(g_pvInstance, "NetClient_NetFileSetChannelParam");
	FNetClient_NetFileGetChannelParam = (pNetClient_NetFileGetChannelParam)DemoExport(g_pvInstance, "NetClient_NetFileGetChannelParam");
	FNetClient_ShutDownDev = (pNetClient_ShutDownDev)DemoExport(g_pvInstance, "NetClient_ShutDownDev");
	FNetClient_BackupImage = (pNetClient_BackupImage)DemoExport(g_pvInstance, "NetClient_BackupImage");
	FNetClient_SetLanParam = (pNetClient_SetLanParam)DemoExport(g_pvInstance, "NetClient_SetLanParam");
	FNetClient_GetLanParam = (pNetClient_GetLanParam)DemoExport(g_pvInstance, "NetClient_GetLanParam");
	FNetClient_GetVideoSzList = (pNetClient_GetVideoSzList)DemoExport(g_pvInstance, "NetClient_GetVideoSzList");
	FNetClient_SetAlarmConfig = (pNetClient_SetAlarmConfig)DemoExport(g_pvInstance, "NetClient_SetAlarmConfig");
	FNetClient_GetAlarmConfig = (pNetClient_GetAlarmConfig)DemoExport(g_pvInstance, "NetClient_GetAlarmConfig");
	FNetClient_SetITSBlock = (pNetClient_SetITSBlock)DemoExport(g_pvInstance, "NetClient_SetITSBlock");
	FNetClient_GetITSBlock = (pNetClient_GetITSBlock)DemoExport(g_pvInstance, "NetClient_GetITSBlock");
	FNetClient_SetHDTimeRangeParam = (pNetClient_SetHDTimeRangeParam)DemoExport(g_pvInstance, "NetClient_SetHDTimeRangeParam");
	FNetClient_GetHDTimeRangeParam = (pNetClient_GetHDTimeRangeParam)DemoExport(g_pvInstance, "NetClient_GetHDTimeRangeParam");
	FNetClient_SetHDTemplateName = (pNetClient_SetHDTemplateName)DemoExport(g_pvInstance, "NetClient_SetHDTemplateName");
	FNetClient_GetHDTemplateName = (pNetClient_GetHDTemplateName)DemoExport(g_pvInstance, "NetClient_GetHDTemplateName");
	FNetClient_SetHDTemplateMap = (pNetClient_SetHDTemplateMap)DemoExport(g_pvInstance, "NetClient_SetHDTemplateMap");
	FNetClient_GetHDTemplateMap = (pNetClient_GetHDTemplateMap)DemoExport(g_pvInstance, "NetClient_GetHDTemplateMap");
	FNetClient_SetITSTimeRangeEnable = (pNetClient_SetITSTimeRangeEnable)DemoExport(g_pvInstance, "NetClient_SetITSTimeRangeEnable");
	FNetClient_GetITSTimeRangeEnable = (pNetClient_GetITSTimeRangeEnable)DemoExport(g_pvInstance, "NetClient_GetITSTimeRangeEnable");
	FNetClient_SetITSTimeRange = (pNetClient_SetITSTimeRange)DemoExport(g_pvInstance, "NetClient_SetITSTimeRange");
	FNetClient_GetITSTimeRange = (pNetClient_GetITSTimeRange)DemoExport(g_pvInstance, "NetClient_GetITSTimeRange");
	FNetClient_SetITSDetectMode = (pNetClient_SetITSDetectMode)DemoExport(g_pvInstance, "NetClient_SetITSDetectMode");
	FNetClient_GetITSDetectMode = (pNetClient_GetITSDetectMode)DemoExport(g_pvInstance, "NetClient_GetITSDetectMode");
	FNetClient_SetITSLoopMode = (pNetClient_SetITSLoopMode)DemoExport(g_pvInstance, "NetClient_SetITSLoopMode");
	FNetClient_GetITSLoopMode = (pNetClient_GetITSLoopMode)DemoExport(g_pvInstance, "NetClient_GetITSLoopMode");
	FNetClient_SetITSDeviceType = (pNetClient_SetITSDeviceType)DemoExport(g_pvInstance, "NetClient_SetITSDeviceType");
	FNetClient_GetITSDeviceType = (pNetClient_GetITSDeviceType)DemoExport(g_pvInstance, "NetClient_GetITSDeviceType");
	FNetClient_SetITSRoadwayParam = (pNetClient_SetITSRoadwayParam)DemoExport(g_pvInstance, "NetClient_SetITSRoadwayParam");
	FNetClient_GetITSRoadwayParam = (pNetClient_GetITSRoadwayParam)DemoExport(g_pvInstance, "NetClient_GetITSRoadwayParam");
	FNetClient_SetITSLicensePlateOptimize = (pNetClient_SetITSLicensePlateOptimize)DemoExport(g_pvInstance, "NetClient_SetITSLicensePlateOptimize");
	FNetClient_GetITSLicensePlateOptimize = (pNetClient_GetITSLicensePlateOptimize)DemoExport(g_pvInstance, "NetClient_GetITSLicensePlateOptimize");
	FNetClient_SetITSExtraInfo = (pNetClient_SetITSExtraInfo)DemoExport(g_pvInstance, "NetClient_SetITSExtraInfo");
	FNetClient_GetITSExtraInfo = (pNetClient_GetITSExtraInfo)DemoExport(g_pvInstance, "NetClient_GetITSExtraInfo");
	FNetClient_CheckDeviceState = (pNetClient_CheckDeviceState)DemoExport(g_pvInstance, "NetClient_CheckDeviceState");
	FNetClient_GetDeviceState = (pNetClient_GetDeviceState)DemoExport(g_pvInstance, "NetClient_GetDeviceState");
	FNetClient_GetCameraCheckInfo = (pNetClient_GetCameraCheckInfo)DemoExport(g_pvInstance, "NetClient_GetCameraCheckInfo");
	FNetClient_CheckCamera = (pNetClient_CheckCamera)DemoExport(g_pvInstance, "NetClient_CheckCamera");
	FNetClient_GetCharSet = (pNetClient_GetCharSet)DemoExport(g_pvInstance, "NetClient_GetCharSet");
	FNetClient_SetTimeZone = (pNetClient_SetTimeZone)DemoExport(g_pvInstance, "NetClient_SetTimeZone");
	FNetClient_GetTimeZone = (pNetClient_GetTimeZone)DemoExport(g_pvInstance, "NetClient_GetTimeZone");
	FNetClient_SetCurLanguage = (pNetClient_SetCurLanguage)DemoExport(g_pvInstance, "NetClient_SetCurLanguage");
	FNetClient_GetCurLanguage = (pNetClient_GetCurLanguage)DemoExport(g_pvInstance, "NetClient_GetCurLanguage");
	FNetClient_GetLanguageList = (pNetClient_GetLanguageList)DemoExport(g_pvInstance, "NetClient_GetLanguageList");
	FNetClient_SetChannelEncodeProfile = (pNetClient_SetChannelEncodeProfile)DemoExport(g_pvInstance, "NetClient_SetChannelEncodeProfile");
	FNetClient_GetChannelEncodeProfile = (pNetClient_GetChannelEncodeProfile)DemoExport(g_pvInstance, "NetClient_GetChannelEncodeProfile");
	FNetClient_SetAlarmClear = (pNetClient_SetAlarmClear)DemoExport(g_pvInstance, "NetClient_SetAlarmClear");
	FNetClient_SetExceptionHandleParam = (pNetClient_SetExceptionHandleParam)DemoExport(g_pvInstance, "NetClient_SetExceptionHandleParam");
	FNetClient_GetExceptionHandleParam = (pNetClient_GetExceptionHandleParam)DemoExport(g_pvInstance, "NetClient_GetExceptionHandleParam");
	FNetClient_SetAlarmLink_V1 = (pNetClient_SetAlarmLink_V1)DemoExport(g_pvInstance, "NetClient_SetAlarmLink_V1");
	FNetClient_GetAlarmLink_V1 = (pNetClient_GetAlarmLink_V1)DemoExport(g_pvInstance, "NetClient_GetAlarmLink_V1");
	FNetClient_SetCameraParam = (pNetClient_SetCameraParam)DemoExport(g_pvInstance, "NetClient_SetCameraParam");
	FNetClient_GetCameraParam = (pNetClient_GetCameraParam)DemoExport(g_pvInstance, "NetClient_GetCameraParam");
	FNetClient_SetColorParam = (pNetClient_SetColorParam)DemoExport(g_pvInstance, "NetClient_SetColorParam");
	FNetClient_GetColorParam = (pNetClient_GetColorParam)DemoExport(g_pvInstance, "NetClient_GetColorParam");
	FNetClient_InnerMallocBlock = (pNetClient_InnerMallocBlock)DemoExport(g_pvInstance, "NetClient_InnerMallocBlock");
	FNetClient_InnerFreeBlock = (pNetClient_InnerFreeBlock)DemoExport(g_pvInstance, "NetClient_InnerFreeBlock");
	FNetClient_InnerReferBlock = (pNetClient_InnerReferBlock)DemoExport(g_pvInstance, "NetClient_InnerReferBlock");
	FNetClient_InnerReleaseBlock = (pNetClient_InnerReleaseBlock)DemoExport(g_pvInstance, "NetClient_InnerReleaseBlock");
	FNetClient_SetJPEGQuality = (pNetClient_SetJPEGQuality)DemoExport(g_pvInstance, "NetClient_SetJPEGQuality");
	FNetClient_GetJPEGQuality = (pNetClient_GetJPEGQuality)DemoExport(g_pvInstance, "NetClient_GetJPEGQuality");
	FNetClient_GetConnectInfo = (pNetClient_GetConnectInfo)DemoExport(g_pvInstance, "NetClient_GetConnectInfo");
	FNetClient_SetPlatformApp = (pNetClient_SetPlatformApp)DemoExport(g_pvInstance, "NetClient_SetPlatformApp");
	FNetClient_GetPlatformApp = (pNetClient_GetPlatformApp)DemoExport(g_pvInstance, "NetClient_GetPlatformApp");
	FNetClient_SetManagerServersInfo = (pNetClient_SetManagerServersInfo)DemoExport(g_pvInstance, "NetClient_SetManagerServersInfo");
	FNetClient_GetManagerServersInfo = (pNetClient_GetManagerServersInfo)DemoExport(g_pvInstance, "NetClient_GetManagerServersInfo");
	FNetClient_SetDeviceID = (pNetClient_SetDeviceID)DemoExport(g_pvInstance, "NetClient_SetDeviceID");
	FNetClient_GetDeviceID = (pNetClient_GetDeviceID)DemoExport(g_pvInstance, "NetClient_GetDeviceID");
	FNetClient_SetATMConfig = (pNetClient_SetATMConfig)DemoExport(g_pvInstance, "NetClient_SetATMConfig");
	FNetClient_GetATMConfig = (pNetClient_GetATMConfig)DemoExport(g_pvInstance, "NetClient_GetATMConfig");
	FNetClient_ATMQueryFile = (pNetClient_ATMQueryFile)DemoExport(g_pvInstance, "NetClient_ATMQueryFile");
	FNetClient_ATMGetQueryFile = (pNetClient_ATMGetQueryFile)DemoExport(g_pvInstance, "NetClient_ATMGetQueryFile");
	FNetClient_SetAudioSample = (pNetClient_SetAudioSample)DemoExport(g_pvInstance, "NetClient_SetAudioSample");
	FNetClient_GetAudioSample = (pNetClient_GetAudioSample)DemoExport(g_pvInstance, "NetClient_GetAudioSample");
	FNetClient_SetSystemTypeEx = (pNetClient_SetSystemTypeEx)DemoExport(g_pvInstance, "NetClient_SetSystemTypeEx");
	FNetClient_GetSystemTypeEx = (pNetClient_GetSystemTypeEx)DemoExport(g_pvInstance, "NetClient_GetSystemTypeEx");
	FNetClient_SetHXListenPortInfo = (pNetClient_SetHXListenPortInfo)DemoExport(g_pvInstance, "NetClient_SetHXListenPortInfo");
	FNetClient_GetHXListenPortInfo = (pNetClient_GetHXListenPortInfo)DemoExport(g_pvInstance, "NetClient_GetHXListenPortInfo");
	FNetClient_SetVideoModeMethod = (pNetClient_SetVideoModeMethod)DemoExport(g_pvInstance, "NetClient_SetVideoModeMethod");
	FNetClient_GetVideoModeMethod = (pNetClient_GetVideoModeMethod)DemoExport(g_pvInstance, "NetClient_GetVideoModeMethod");
	FNetClient_GetMonitorNum = (pNetClient_GetMonitorNum)DemoExport(g_pvInstance, "NetClient_GetMonitorNum");
	FNetClient_GetMonitorInfo = (pNetClient_GetMonitorInfo)DemoExport(g_pvInstance, "NetClient_GetMonitorInfo");
	FNetClient_ChangeMonitor = (pNetClient_ChangeMonitor)DemoExport(g_pvInstance, "NetClient_ChangeMonitor");
	FNetClient_EZoomAdd = (pNetClient_EZoomAdd)DemoExport(g_pvInstance, "NetClient_EZoomAdd");
	FNetClient_EZoomSet = (pNetClient_EZoomSet)DemoExport(g_pvInstance, "NetClient_EZoomSet");
	FNetClient_EZoomReset = (pNetClient_EZoomReset)DemoExport(g_pvInstance, "NetClient_EZoomReset");
	FNetClient_EZoomRemove = (pNetClient_EZoomRemove)DemoExport(g_pvInstance, "NetClient_EZoomRemove");
	FNetClient_DCardStartPlay = (pNetClient_DCardStartPlay)DemoExport(g_pvInstance, "NetClient_DCardStartPlay");
	FNetClient_DCardStopPlay = (pNetClient_DCardStopPlay)DemoExport(g_pvInstance, "NetClient_DCardStopPlay");
	FNetClient_DCardRelease = (pNetClient_DCardRelease)DemoExport(g_pvInstance, "NetClient_DCardRelease");
	FNetClient_DCardReInit = (pNetClient_DCardReInit)DemoExport(g_pvInstance, "NetClient_DCardReInit");
	FNetClient_DCardGetState = (pNetClient_DCardGetState)DemoExport(g_pvInstance, "NetClient_DCardGetState");
	FNetClient_DCardStartPlayEx = (pNetClient_DCardStartPlayEx)DemoExport(g_pvInstance, "NetClient_DCardStartPlayEx");
	FNetClient_DCardPutDataEx = (pNetClient_DCardPutDataEx)DemoExport(g_pvInstance, "NetClient_DCardPutDataEx");
	FNetClient_DCardStopPlayEx = (pNetClient_DCardStopPlayEx)DemoExport(g_pvInstance, "NetClient_DCardStopPlayEx");
	FNetClient_DCardStartPlayAudio = (pNetClient_DCardStartPlayAudio)DemoExport(g_pvInstance, "NetClient_DCardStartPlayAudio");
	FNetClient_SetEncryptSN = (pNetClient_SetEncryptSN)DemoExport(g_pvInstance, "NetClient_SetEncryptSN");
	FNetClient_GetSNReg = (pNetClient_GetSNReg)DemoExport(g_pvInstance, "NetClient_GetSNReg");
	FNetClient_GetComFormat_V1 = (pNetClient_GetComFormat_V1)DemoExport(g_pvInstance, "NetClient_GetComFormat_V1");
	FNetClient_SetComFormat_V2 = (pNetClient_SetComFormat_V2)DemoExport(g_pvInstance, "NetClient_SetComFormat_V2");
	FNetClient_GetComFormat_V2 = (pNetClient_GetComFormat_V2)DemoExport(g_pvInstance, "NetClient_GetComFormat_V2");
	FNetClient_GetServerVersion_V1 = (pNetClient_GetServerVersion_V1)DemoExport(g_pvInstance, "NetClient_GetServerVersion_V1");
	FNetClient_InputTalkingdataEx = (pNetClient_InputTalkingdataEx)DemoExport(g_pvInstance, "NetClient_InputTalkingdataEx");
	FNetClient_SetVerticalSync = (pNetClient_SetVerticalSync)DemoExport(g_pvInstance, "NetClient_SetVerticalSync");
	FNetClient_GetVerticalSync = (pNetClient_GetVerticalSync)DemoExport(g_pvInstance, "NetClient_GetVerticalSync");
	FNetClient_SetLocalAudioVolumeEx = (pNetClient_SetLocalAudioVolumeEx)DemoExport(g_pvInstance, "NetClient_SetLocalAudioVolumeEx");
	FNetClient_GetLocalAudioVolumeEx = (pNetClient_GetLocalAudioVolumeEx)DemoExport(g_pvInstance, "NetClient_GetLocalAudioVolumeEx");
	FNetClient_ClearPolyLocalVideo = (pNetClient_ClearPolyLocalVideo)DemoExport(g_pvInstance, "NetClient_ClearPolyLocalVideo");
	FNetClient_SetOSDTypeFontSize = (pNetClient_SetOSDTypeFontSize)DemoExport(g_pvInstance, "NetClient_SetOSDTypeFontSize");
	FNetClient_GetOSDTypeFontSize = (pNetClient_GetOSDTypeFontSize)DemoExport(g_pvInstance, "NetClient_GetOSDTypeFontSize");
	FNetClient_SetImgDisposal = (pNetClient_SetImgDisposal)DemoExport(g_pvInstance, "NetClient_SetImgDisposal");
	FNetClient_GetImgDisposal = (pNetClient_GetImgDisposal)DemoExport(g_pvInstance, "NetClient_GetImgDisposal");
	FNetClient_SetMuted = (pNetClient_SetMuted)DemoExport(g_pvInstance, "NetClient_SetMuted");
	FNetClient_SetPWMValue = (pNetClient_SetPWMValue)DemoExport(g_pvInstance, "NetClient_SetPWMValue");
	FNetClient_GetPWMValue = (pNetClient_GetPWMValue)DemoExport(g_pvInstance, "NetClient_GetPWMValue");
	FNetClient_SetSystemType = (pNetClient_SetSystemType)DemoExport(g_pvInstance, "NetClient_SetSystemType");
	FNetClient_GetSystemType = (pNetClient_GetSystemType)DemoExport(g_pvInstance, "NetClient_GetSystemType");
	FNetClient_SetITSSwitchTime = (pNetClient_SetITSSwitchTime)DemoExport(g_pvInstance, "NetClient_SetITSSwitchTime");
	FNetClient_GetITSSwitchTime = (pNetClient_GetITSSwitchTime)DemoExport(g_pvInstance, "NetClient_GetITSSwitchTime");
	FNetClient_SetITSRecoParam = (pNetClient_SetITSRecoParam)DemoExport(g_pvInstance, "NetClient_SetITSRecoParam");
	FNetClient_GetITSRecoParam = (pNetClient_GetITSRecoParam)DemoExport(g_pvInstance, "NetClient_GetITSRecoParam");
	FNetClient_SetITSDayNight = (pNetClient_SetITSDayNight)DemoExport(g_pvInstance, "NetClient_SetITSDayNight");
	FNetClient_GetITSDayNight = (pNetClient_GetITSDayNight)DemoExport(g_pvInstance, "NetClient_GetITSDayNight");
	FNetClient_SetITSCamLocation = (pNetClient_SetITSCamLocation)DemoExport(g_pvInstance, "NetClient_SetITSCamLocation");
	FNetClient_GetITSCamLocation = (pNetClient_GetITSCamLocation)DemoExport(g_pvInstance, "NetClient_GetITSCamLocation");
	FNetClient_SetITSWorkMode = (pNetClient_SetITSWorkMode)DemoExport(g_pvInstance, "NetClient_SetITSWorkMode");
	FNetClient_GetITSWorkMode = (pNetClient_GetITSWorkMode)DemoExport(g_pvInstance, "NetClient_GetITSWorkMode");
	FNetClient_SetWaterMarkEnable = (pNetClient_SetWaterMarkEnable)DemoExport(g_pvInstance, "NetClient_SetWaterMarkEnable");
	FNetClient_GetWaterMarkEnable = (pNetClient_GetWaterMarkEnable)DemoExport(g_pvInstance, "NetClient_GetWaterMarkEnable");
	FNetClient_SetITSLightInfo = (pNetClient_SetITSLightInfo)DemoExport(g_pvInstance, "NetClient_SetITSLightInfo");
	FNetClient_GetITSLightInfo = (pNetClient_GetITSLightInfo)DemoExport(g_pvInstance, "NetClient_GetITSLightInfo");
	FNetClient_SetHardWareParam = (pNetClient_SetHardWareParam)DemoExport(g_pvInstance, "NetClient_SetHardWareParam");
	FNetClient_GetHardWareParam = (pNetClient_GetHardWareParam)DemoExport(g_pvInstance, "NetClient_GetHardWareParam");
	FNetClient_SetDomeAdvParam = (pNetClient_SetDomeAdvParam)DemoExport(g_pvInstance, "NetClient_SetDomeAdvParam");
	FNetClient_GetDomeAdvParam = (pNetClient_GetDomeAdvParam)DemoExport(g_pvInstance, "NetClient_GetDomeAdvParam");
	FNetClient_SetDiskGroup = (pNetClient_SetDiskGroup)DemoExport(g_pvInstance, "NetClient_SetDiskGroup");
	FNetClient_GetDiskGroup = (pNetClient_GetDiskGroup)DemoExport(g_pvInstance, "NetClient_GetDiskGroup");
	FNetClient_SetDiskQuota = (pNetClient_SetDiskQuota)DemoExport(g_pvInstance, "NetClient_SetDiskQuota");
	FNetClient_GetDiskQuotaState  = (pNetClient_GetDiskQuotaState )DemoExport(g_pvInstance, "NetClient_GetDiskQuotaState ");
	FNetClient_ModifyUserAuthority = (pNetClient_ModifyUserAuthority)DemoExport(g_pvInstance, "NetClient_ModifyUserAuthority");
	FNetClient_GetUserAuthority = (pNetClient_GetUserAuthority)DemoExport(g_pvInstance, "NetClient_GetUserAuthority");
	FNetClient_GetGroupAuthority = (pNetClient_GetGroupAuthority)DemoExport(g_pvInstance, "NetClient_GetGroupAuthority");
	FNetClient_NetFileGetQueryfileEx = (pNetClient_NetFileGetQueryfileEx)DemoExport(g_pvInstance, "NetClient_NetFileGetQueryfileEx");
	FNetClient_NetFileLockFile = (pNetClient_NetFileLockFile)DemoExport(g_pvInstance, "NetClient_NetFileLockFile");
	FNetClient_GetOsdTextEx = (pNetClient_GetOsdTextEx)DemoExport(g_pvInstance, "NetClient_GetOsdTextEx");
	FNetClient_SetHolidayPlan = (pNetClient_SetHolidayPlan)DemoExport(g_pvInstance, "NetClient_SetHolidayPlan");
	FNetClient_GetHolidayPlan = (pNetClient_GetHolidayPlan)DemoExport(g_pvInstance, "NetClient_GetHolidayPlan");
	FNetClient_SetCommonEnable = (pNetClient_SetCommonEnable)DemoExport(g_pvInstance, "NetClient_SetCommonEnable");
	FNetClient_GetCommonEnable = (pNetClient_GetCommonEnable)DemoExport(g_pvInstance, "NetClient_GetCommonEnable");
	FNetClient_NetFileDownload = (pNetClient_NetFileDownload)DemoExport(g_pvInstance, "NetClient_NetFileDownload");
	FNetClient_Upgrade_V4 = (pNetClient_Upgrade_V4)DemoExport(g_pvInstance, "NetClient_Upgrade_V4");
	FNetClient_GetAudioCoderList = (pNetClient_GetAudioCoderList)DemoExport(g_pvInstance, "NetClient_GetAudioCoderList");
	FNetClient_InnerAutoTest = (pNetClient_InnerAutoTest)DemoExport(g_pvInstance, "NetClient_InnerAutoTest");
	FNetClient_SetJEPGSize = (pNetClient_SetJEPGSize)DemoExport(g_pvInstance, "NetClient_SetJEPGSize");
	FNetClient_GetJEPGSize = (pNetClient_GetJEPGSize)DemoExport(g_pvInstance, "NetClient_GetJEPGSize");
	FNetClient_QueryDevStatus = (pNetClient_QueryDevStatus)DemoExport(g_pvInstance, "NetClient_QueryDevStatus");
	FNetClient_GetDevStatus = (pNetClient_GetDevStatus)DemoExport(g_pvInstance, "NetClient_GetDevStatus");
	FNetClient_GetHDTemplateIndex = (pNetClient_GetHDTemplateIndex)DemoExport(g_pvInstance, "NetClient_GetHDTemplateIndex");
	FNetClient_SetStreamInsertData = (pNetClient_SetStreamInsertData)DemoExport(g_pvInstance, "NetClient_SetStreamInsertData");
	FNetClient_GetStreamInsertData = (pNetClient_GetStreamInsertData)DemoExport(g_pvInstance, "NetClient_GetStreamInsertData");
	FNetClient_GetOtherID = (pNetClient_GetOtherID)DemoExport(g_pvInstance, "NetClient_GetOtherID");
	FNetClient_SetDomePTZ = (pNetClient_SetDomePTZ)DemoExport(g_pvInstance, "NetClient_SetDomePTZ");
	FNetClient_GetDomePTZ = (pNetClient_GetDomePTZ)DemoExport(g_pvInstance, "NetClient_GetDomePTZ");
	FNetClient_GetUserDataInfo = (pNetClient_GetUserDataInfo)DemoExport(g_pvInstance, "NetClient_GetUserDataInfo");
	FNetClient_GetBroadcastMessage = (pNetClient_GetBroadcastMessage)DemoExport(g_pvInstance, "NetClient_GetBroadcastMessage");
	FNetClient_GetModuleCapability = (pNetClient_GetModuleCapability)DemoExport(g_pvInstance, "NetClient_GetModuleCapability");
	FNetClient_KeyboardCtrl = (pNetClient_KeyboardCtrl)DemoExport(g_pvInstance, "NetClient_KeyboardCtrl");
	FNetClient_NetFileSetSchedule = (pNetClient_NetFileSetSchedule)DemoExport(g_pvInstance, "NetClient_NetFileSetSchedule");
	FNetClient_NetFileGetSchedule = (pNetClient_NetFileGetSchedule)DemoExport(g_pvInstance, "NetClient_NetFileGetSchedule");
	FNetClient_SetDevConfig = (pNetClient_SetDevConfig)DemoExport(g_pvInstance, "NetClient_SetDevConfig");
	FNetClient_GetDevConfig = (pNetClient_GetDevConfig)DemoExport(g_pvInstance, "NetClient_GetDevConfig");
	FNetClient_SendCommand = (pNetClient_SendCommand)DemoExport(g_pvInstance, "NetClient_SendCommand");
	FNetClient_RecvCommand = (pNetClient_RecvCommand)DemoExport(g_pvInstance, "NetClient_RecvCommand");
	FNetClient_SetDevDiskConfig  = (pNetClient_SetDevDiskConfig )DemoExport(g_pvInstance, "NetClient_SetDevDiskConfig ");
	FNetClient_GetDevDiskConfig  = (pNetClient_GetDevDiskConfig )DemoExport(g_pvInstance, "NetClient_GetDevDiskConfig ");
	FNetClient_Logon_V4 = (pNetClient_Logon_V4)DemoExport(g_pvInstance, "NetClient_Logon_V4");
	FNetClient_PlayBackControl = (pNetClient_PlayBackControl)DemoExport(g_pvInstance, "NetClient_PlayBackControl");
	FNetClient_PlayerControl = (pNetClient_PlayerControl)DemoExport(g_pvInstance, "NetClient_PlayerControl");
	FNetClient_StopPlayBack = (pNetClient_StopPlayBack)DemoExport(g_pvInstance, "NetClient_StopPlayBack");
	FNetClient_PlayBack = (pNetClient_PlayBack)DemoExport(g_pvInstance, "NetClient_PlayBack");
	FNetClient_GetPseChInfo = (pNetClient_GetPseChInfo)DemoExport(g_pvInstance, "NetClient_GetPseChInfo");
	FNetClient_SetPseChProperty = (pNetClient_SetPseChProperty)DemoExport(g_pvInstance, "NetClient_SetPseChProperty");
	FNetClient_GetPseChProperty = (pNetClient_GetPseChProperty)DemoExport(g_pvInstance, "NetClient_GetPseChProperty");
	FNetClient_ChannelTalkStart = (pNetClient_ChannelTalkStart)DemoExport(g_pvInstance, "NetClient_ChannelTalkStart");
	FNetClient_ChannelTalkEnd = (pNetClient_ChannelTalkEnd)DemoExport(g_pvInstance, "NetClient_ChannelTalkEnd");
	FNetClient_InputChannelTalkingdata = (pNetClient_InputChannelTalkingdata)DemoExport(g_pvInstance, "NetClient_InputChannelTalkingdata");
	FNetClient_GetChannelTalkingState = (pNetClient_GetChannelTalkingState)DemoExport(g_pvInstance, "NetClient_GetChannelTalkingState");
	FNetClient_CapturePicture = (pNetClient_CapturePicture)DemoExport(g_pvInstance, "NetClient_CapturePicture");
	FNetClient_CapturePicData = (pNetClient_CapturePicData)DemoExport(g_pvInstance, "NetClient_CapturePicData");
	FNetClient_SetSDKWorkMode = (pNetClient_SetSDKWorkMode)DemoExport(g_pvInstance, "NetClient_SetSDKWorkMode");
	FNetClient_Query_V4 = (pNetClient_Query_V4)DemoExport(g_pvInstance, "NetClient_Query_V4");
	FNetClient_GetQueryResult_V4 = (pNetClient_GetQueryResult_V4)DemoExport(g_pvInstance, "NetClient_GetQueryResult_V4");
	FNetClient_RebootDeviceByType = (pNetClient_RebootDeviceByType)DemoExport(g_pvInstance, "NetClient_RebootDeviceByType");
	FNetClient_StartDownload = (pNetClient_StartDownload)DemoExport(g_pvInstance, "NetClient_StartDownload");
	FNetClient_StopDownload = (pNetClient_StopDownload)DemoExport(g_pvInstance, "NetClient_StopDownload");
	FNetClient_GetDownloadPos = (pNetClient_GetDownloadPos)DemoExport(g_pvInstance, "NetClient_GetDownloadPos");
	FNetClient_ProxySend  = (pNetClient_ProxySend )DemoExport(g_pvInstance, "NetClient_ProxySend ");
	FNetClient_SetDevUserDataNotify = (pNetClient_SetDevUserDataNotify)DemoExport(g_pvInstance, "NetClient_SetDevUserDataNotify");
	FNetClient_SetDsmConfig = (pNetClient_SetDsmConfig)DemoExport(g_pvInstance, "NetClient_SetDsmConfig");
	FNetClient_GetDsmRegstierInfo = (pNetClient_GetDsmRegstierInfo)DemoExport(g_pvInstance, "NetClient_GetDsmRegstierInfo");
	FNetClient_GetRecvInfoById = (pNetClient_GetRecvInfoById)DemoExport(g_pvInstance, "NetClient_GetRecvInfoById");
	FNetClient_GetParamFromDevice = (pNetClient_GetParamFromDevice)DemoExport(g_pvInstance, "NetClient_GetParamFromDevice");
	FNetClient_GetPlayerIndex = (pNetClient_GetPlayerIndex)DemoExport(g_pvInstance, "NetClient_GetPlayerIndex");
	FNetClient_GetRealPlayerIndex = (pNetClient_GetRealPlayerIndex)DemoExport(g_pvInstance, "NetClient_GetRealPlayerIndex");
	FNetClient_StartRecvNetPicStream = (pNetClient_StartRecvNetPicStream)DemoExport(g_pvInstance, "NetClient_StartRecvNetPicStream");
	FNetClient_StopRecvNetPicStream = (pNetClient_StopRecvNetPicStream)DemoExport(g_pvInstance, "NetClient_StopRecvNetPicStream");
	FNetClient_SetProxyNotifyFunction = (pNetClient_SetProxyNotifyFunction)DemoExport(g_pvInstance, "NetClient_SetProxyNotifyFunction");
	FNetClient_SetExternDevLogonInfo = (pNetClient_SetExternDevLogonInfo)DemoExport(g_pvInstance, "NetClient_SetExternDevLogonInfo");
	FNetClient_SetUnipueAlertConfig = (pNetClient_SetUnipueAlertConfig)DemoExport(g_pvInstance, "NetClient_SetUnipueAlertConfig");
	FNetClient_GetUnipueAlertConfig = (pNetClient_GetUnipueAlertConfig)DemoExport(g_pvInstance, "NetClient_GetUnipueAlertConfig");
	FNetClient_FaceConfig = (pNetClient_FaceConfig)DemoExport(g_pvInstance, "NetClient_FaceConfig");
	FNetClient_Query_V5 = (pNetClient_Query_V5)DemoExport(g_pvInstance, "NetClient_Query_V5");
	FNetClient_SetAlarmNotify_V5 = (pNetClient_SetAlarmNotify_V5)DemoExport(g_pvInstance, "NetClient_SetAlarmNotify_V5");
	FNetClient_Upgrade_V5 = (pNetClient_Upgrade_V5)DemoExport(g_pvInstance, "NetClient_Upgrade_V5");
	FNetClient_CmdConfig = (pNetClient_CmdConfig)DemoExport(g_pvInstance, "NetClient_CmdConfig");
	FNetClient_GetLastError = (pNetClient_GetLastError)DemoExport(g_pvInstance, "NetClient_GetLastError");
	FNetClient_GetConncetInfo = (pNetClient_GetConncetInfo)DemoExport(g_pvInstance, "NetClient_GetConncetInfo");
	FNetClient_SyncLogon = (pNetClient_SyncLogon)DemoExport(g_pvInstance, "NetClient_SyncLogon");
	FNetClient_SyncRealPlay = (pNetClient_SyncRealPlay)DemoExport(g_pvInstance, "NetClient_SyncRealPlay");
	FNetClient_StopRealPlay = (pNetClient_StopRealPlay)DemoExport(g_pvInstance, "NetClient_StopRealPlay");
	FNetClient_SyncQuery = (pNetClient_SyncQuery)DemoExport(g_pvInstance, "NetClient_SyncQuery");
	FNetClient_SyncSetDevCfg = (pNetClient_SyncSetDevCfg)DemoExport(g_pvInstance, "NetClient_SyncSetDevCfg");
	FNetClient_CapturePicByDevice = (pNetClient_CapturePicByDevice)DemoExport(g_pvInstance, "NetClient_CapturePicByDevice");
	FNetClient_SetSDKInitConfig = (pNetClient_SetSDKInitConfig)DemoExport(g_pvInstance, "NetClient_SetSDKInitConfig");
	FNetClient_SetAVMode = (pNetClient_SetAVMode)DemoExport(g_pvInstance, "NetClient_SetAVMode");
	FNetClient_CreateQtWidget = (pNetClient_CreateQtWidget)DemoExport(g_pvInstance, "NetClient_CreateQtWidget");
	FNetClient_ReleaseQtWidget = (pNetClient_ReleaseQtWidget)DemoExport(g_pvInstance, "NetClient_ReleaseQtWidget");
	FNetClient_GetDevConfig_V5 = (pNetClient_GetDevConfig_V5)DemoExport(g_pvInstance, "NetClient_GetDevConfig_V5");
	FNetClient_SycVoiceTalkStart = (pNetClient_SycVoiceTalkStart)DemoExport(g_pvInstance, "NetClient_SycVoiceTalkStart");
	FNetClient_SycVoiceTalkStop = (pNetClient_SycVoiceTalkStop)DemoExport(g_pvInstance, "NetClient_SycVoiceTalkStop");
	FNetClient_SycVoiceTalkInputData = (pNetClient_SycVoiceTalkInputData)DemoExport(g_pvInstance, "NetClient_SycVoiceTalkInputData");
	FNetClient_HttpXmlConfig = (pNetClient_HttpXmlConfig)DemoExport(g_pvInstance, "NetClient_HttpXmlConfig");
	FNetClient_XmlSetDevConfig = (pNetClient_XmlSetDevConfig)DemoExport(g_pvInstance, "NetClient_XmlSetDevConfig");
	FNetClient_XmlGetDevConfig = (pNetClient_XmlGetDevConfig)DemoExport(g_pvInstance, "NetClient_XmlGetDevConfig");
	return 0;
}

void FreeNVSSDK()
{
	if (g_pvInstance != NULL) {
#ifdef  WIN32
		FreeLibrary((HMODULE)g_pvInstance);
#else
		dlclose(g_pvInstance);
#endif
		g_pvInstance = NULL;
	}
}


int NetClient_Startup_V4(int _iServerPort, int _iClientPort, int _iWnd)
{
	if (NULL == g_pvInstance) {
		LoadNVSSDK();
	}
	if (NULL == FNetClient_Startup_V4) {
		return -1;
	}
	return FNetClient_Startup_V4( _iServerPort,  _iClientPort,  _iWnd);
}

int NetClient_SetNotifyFunction_V4(MAIN_NOTIFY_V4        _MainNotify, 
		ALARM_NOTIFY_V4       _AlarmNotify,
		PARACHANGE_NOTIFY_V4  _ParaNotify,
		COMRECV_NOTIFY_V4     _ComNotify,
		PROXY_NOTIFY       _ProxyNotify)
{
	if (NULL == FNetClient_SetNotifyFunction_V4) {
		return -1;
	}
	return FNetClient_SetNotifyFunction_V4( _MainNotify,  _AlarmNotify,  _ParaNotify,  _ComNotify,  _ProxyNotify);
}

int NetClient_StartRecv_V4(unsigned int* _uiRecvID, CLIENTINFO* _cltInfo, NVSDATA_NOTIFY _cbkDataArrive,void* _iUserData)
{
	if (NULL == FNetClient_StartRecv_V4) {
		return -1;
	}
	return FNetClient_StartRecv_V4( _uiRecvID,  _cltInfo,  _cbkDataArrive,  _iUserData);
}

int NetClient_StartRecv_V5(unsigned int* _puiRecvID, NetClientPara* _ptPara, int _iParaSize)
{
	if (NULL == FNetClient_StartRecv_V5) {
		return -1;
	}
	return FNetClient_StartRecv_V5( _puiRecvID,  _ptPara,  _iParaSize);
}

int NetClient_SetNotifyUserData_V4(int _iLogonID,void* _iUserData)
{
	if (NULL == FNetClient_SetNotifyUserData_V4) {
		return -1;
	}
	return FNetClient_SetNotifyUserData_V4( _iLogonID,  _iUserData);
}

int NetClient_SetComRecvNotify_V4(COMRECV_NOTIFY_V4 _comNotify)
{
	if (NULL == FNetClient_SetComRecvNotify_V4) {
		return -1;
	}
	return FNetClient_SetComRecvNotify_V4( _comNotify);
}

int NetClient_GetHTTPPort_V4(int _iLogonID, int* _iPort)
{
	if (NULL == FNetClient_GetHTTPPort_V4) {
		return -1;
	}
	return FNetClient_GetHTTPPort_V4( _iLogonID,  _iPort);
}

int NetClient_SetHTTPPort_V4(int _iLogonID, int _iPort)
{
	if (NULL == FNetClient_SetHTTPPort_V4) {
		return -1;
	}
	return FNetClient_SetHTTPPort_V4( _iLogonID,  _iPort);
}

int NetClient_SetDomainParsePara_V4(int _iLogonID, int _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int _iPort1, int _iPort2)
{
	if (NULL == FNetClient_SetDomainParsePara_V4) {
		return -1;
	}
	return FNetClient_SetDomainParsePara_V4( _iLogonID,  _iInterval,  _cUserID,  _cPassword,  _cHost,  _cDomain1,  _cDomain2,  _iPort1,  _iPort2);
}

int NetClient_GetDomainParsePara_V4(int _iLogonID, int* _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int* _iPort1, int* _iPort2)
{
	if (NULL == FNetClient_GetDomainParsePara_V4) {
		return -1;
	}
	return FNetClient_GetDomainParsePara_V4( _iLogonID,  _iInterval,  _cUserID,  _cPassword,  _cHost,  _cDomain1,  _cDomain2,  _iPort1,  _iPort2);
}

int NetClient_GetBitrateOnVideo_V4(unsigned int _ulConID, int* _piX, int* _piY, int* _piEnabled, char* _pcInfo)
{
	if (NULL == FNetClient_GetBitrateOnVideo_V4) {
		return -1;
	}
	return FNetClient_GetBitrateOnVideo_V4( _ulConID,  _piX,  _piY,  _piEnabled,  _pcInfo);
}

int NetClient_SetDecCallBack_V4(unsigned int _ulConID, DECYUV_NOTIFY_V4 _cbkDecYUV, void* _iUserData)
{
	if (NULL == FNetClient_SetDecCallBack_V4) {
		return -1;
	}
	return FNetClient_SetDecCallBack_V4( _ulConID,  _cbkDecYUV,  _iUserData);
}

int NetClient_RegisterDrawFun(unsigned int _ulConID, CBK_DRAW_FUNC _pfDrawFun, long _lUserData, void* _pCmd, int _iCmdLen)
{
	if (NULL == FNetClient_RegisterDrawFun) {
		return -1;
	}
	return FNetClient_RegisterDrawFun( _ulConID,  _pfDrawFun,  _lUserData,  _pCmd,  _iCmdLen);
}

int NetClient_SetPort( int _iServerPort, int _iClientPort )
{
	if (NULL == FNetClient_SetPort) {
		return -1;
	}
	return FNetClient_SetPort( _iServerPort,  _iClientPort );
}

#ifdef WIN32
int NetClient_Startup()
{
	if (NULL == g_pvInstance) {
		LoadNVSSDK();
	}
	if (NULL == FNetClient_Startup) {
		return -1;
	}
	return FNetClient_Startup();
}

int NetClient_SetNotifyFunction( LOGON_NOTIFY _LogonNotify,ALARM_NOTIFY _AlarmNotify, PARACHANGE_NOTIFY _ParaNotify)
{
	if (NULL == FNetClient_SetNotifyFunction) {
		return -1;
	}
	return FNetClient_SetNotifyFunction( _LogonNotify,  _AlarmNotify,  _ParaNotify);
}

int NetClient_SetNotifyFunctionEx( MAIN_NOTIFY _cbkMainNotify, ALARM_NOTIFY_EX _cbkAlarmNotify, PARACHANGE_NOTIFY_EX _cbkParaChangeNotify, void* _pNotifyUserData )
{
	if (NULL == FNetClient_SetNotifyFunctionEx) {
		return -1;
	}
	return FNetClient_SetNotifyFunctionEx( _cbkMainNotify,  _cbkAlarmNotify,  _cbkParaChangeNotify,  _pNotifyUserData );
}

int NetClient_SetMSGHandle( unsigned int _uiMessage, HWND _hWnd, unsigned int _uiParaMsg, unsigned int _uiAlarmMsg )
{
	if (NULL == FNetClient_SetMSGHandle) {
		return -1;
	}
	return FNetClient_SetMSGHandle( _uiMessage,  _hWnd,  _uiParaMsg,  _uiAlarmMsg );
}

int NetClient_SetMSGHandleEx( unsigned int _uiMessage, HWND _hWnd, unsigned int _uiParaMsg, unsigned int _uiAlarmMsg )
{
	if (NULL == FNetClient_SetMSGHandleEx) {
		return -1;
	}
	return FNetClient_SetMSGHandleEx( _uiMessage,  _hWnd,  _uiParaMsg,  _uiAlarmMsg );
}

int NetClient_StartRecv(unsigned int* _ulConID, CLIENTINFO* _cltInfo, RECVDATA_NOTIFY _cbkDataArrive)
{
	if (NULL == FNetClient_StartRecv) {
		return -1;
	}
	return FNetClient_StartRecv( _ulConID,  _cltInfo,  _cbkDataArrive);
}

int NetClient_StartRecvEx(unsigned int* _ulConID,CLIENTINFO* _cltInfo,RECVDATA_NOTIFY_EX _cbkDataNotifyEx, void* _lpUserData)
{
	if (NULL == FNetClient_StartRecvEx) {
		return -1;
	}
	return FNetClient_StartRecvEx( _ulConID,  _cltInfo,  _cbkDataNotifyEx,  _lpUserData);
}

int NetClient_SetComRecvNotify(COMRECV_NOTIFY _comNotify)
{
	if (NULL == FNetClient_SetComRecvNotify) {
		return -1;
	}
	return FNetClient_SetComRecvNotify( _comNotify);
}

int NetClient_GetHTTPPort(int _iLogonID, unsigned short* _iPort)
{
	if (NULL == FNetClient_GetHTTPPort) {
		return -1;
	}
	return FNetClient_GetHTTPPort( _iLogonID,  _iPort);
}

int NetClient_SetHTTPPort(int _iLogonID, unsigned short _iPort)
{
	if (NULL == FNetClient_SetHTTPPort) {
		return -1;
	}
	return FNetClient_SetHTTPPort( _iLogonID,  _iPort);
}

int NetClient_SetDomainParsePara(int _iLogonID, int _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,unsigned short _iPort1, unsigned short _iPort2)
{
	if (NULL == FNetClient_SetDomainParsePara) {
		return -1;
	}
	return FNetClient_SetDomainParsePara( _iLogonID,  _iInterval,  _cUserID,  _cPassword,  _cHost,  _cDomain1,  _cDomain2,  _iPort1,  _iPort2);
}

int NetClient_GetDomainParsePara(int _iLogonID, int* _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,unsigned short* _iPort1, unsigned short* _iPort2)
{
	if (NULL == FNetClient_GetDomainParsePara) {
		return -1;
	}
	return FNetClient_GetDomainParsePara( _iLogonID,  _iInterval,  _cUserID,  _cPassword,  _cHost,  _cDomain1,  _cDomain2,  _iPort1,  _iPort2);
}

int NetClient_GetBitrateOnVideo(unsigned int _ulConID, int* _iX, int* _iY, int* _iEnabled)
{
	if (NULL == FNetClient_GetBitrateOnVideo) {
		return -1;
	}
	return FNetClient_GetBitrateOnVideo( _ulConID,  _iX,  _iY,  _iEnabled);
}

int NetClient_SetDecCallBack(unsigned int _ulConID, DECYUV_NOTIFY _cbkGetYUV, void* _pContext)
{
	if (NULL == FNetClient_SetDecCallBack) {
		return -1;
	}
	return FNetClient_SetDecCallBack( _ulConID,  _cbkGetYUV,  _pContext);
}

int NetClient_InterTalkStartEx(unsigned int * _uiConnID, int _iLogonID, RECVDATA_NOTIFY_EX _cbkDataArrive, void* _iUserData)
{
	if (NULL == FNetClient_InterTalkStartEx) {
		return -1;
	}
	return FNetClient_InterTalkStartEx( _uiConnID,  _iLogonID,  _cbkDataArrive,  _iUserData);
}

#else
int NetClient_Startup(int _iServerPort/*=3000*/, int _iClientPort/*=6000*/, int _iWnd/*=0*/)
{
	if (NULL == g_pvInstance) {
		LoadNVSSDK();
	}
	if (NULL == FNetClient_Startup) {
		return -1;
	}
	return FNetClient_Startup( _iServerPort/*=3000*/,  _iClientPort/*=6000*/,  _iWnd/*=0*/);
}

int NetClient_SetNotifyFunction(MAIN_NOTIFY_V4        _MainNotify, 
										  ALARM_NOTIFY_V4       _AlarmNotify,
										  PARACHANGE_NOTIFY_V4  _ParaNotify,
										  COMRECV_NOTIFY_V4     _ComNotify,
										  PROXY_NOTIFY       _ProxyNotify)
{
	if (NULL == FNetClient_SetNotifyFunction) {
		return -1;
	}
	return FNetClient_SetNotifyFunction( _MainNotify,  _AlarmNotify,  _ParaNotify,  _ComNotify,  _ProxyNotify);
}

int NetClient_StartRecv(unsigned int* _uiRecvID, CLIENTINFO* _cltInfo, NVSDATA_NOTIFY _cbkDataArrive,void* _iUserData)
{
	if (NULL == FNetClient_StartRecv) {
		return -1;
	}
	return FNetClient_StartRecv( _uiRecvID,  _cltInfo,  _cbkDataArrive,  _iUserData);
}

int NetClient_SetNotifyUserData(int _iLogonID,void* _iUserData)
{
	if (NULL == FNetClient_SetNotifyUserData) {
		return -1;
	}
	return FNetClient_SetNotifyUserData( _iLogonID,  _iUserData);
}

int NetClient_SetComRecvNotify(COMRECV_NOTIFY_V4 _comNotify)
{
	if (NULL == FNetClient_SetComRecvNotify) {
		return -1;
	}
	return FNetClient_SetComRecvNotify( _comNotify);
}

int NetClient_GetHTTPPort(int _iLogonID, int* _iPort)
{
	if (NULL == FNetClient_GetHTTPPort) {
		return -1;
	}
	return FNetClient_GetHTTPPort( _iLogonID,  _iPort);
}

int NetClient_SetHTTPPort(int _iLogonID, int _iPort)
{
	if (NULL == FNetClient_SetHTTPPort) {
		return -1;
	}
	return FNetClient_SetHTTPPort( _iLogonID,  _iPort);
}

int NetClient_SetDomainParsePara(int _iLogonID, int _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int _iPort1, int _iPort2)
{
	if (NULL == FNetClient_SetDomainParsePara) {
		return -1;
	}
	return FNetClient_SetDomainParsePara( _iLogonID,  _iInterval,  _cUserID,  _cPassword,  _cHost,  _cDomain1,  _cDomain2,  _iPort1,  _iPort2);
}

int NetClient_GetDomainParsePara(int _iLogonID, int* _iInterval, char* _cUserID, char* _cPassword, char* _cHost, char* _cDomain1, char* _cDomain2,int* _iPort1, int* _iPort2)
{
	if (NULL == FNetClient_GetDomainParsePara) {
		return -1;
	}
	return FNetClient_GetDomainParsePara( _iLogonID,  _iInterval,  _cUserID,  _cPassword,  _cHost,  _cDomain1,  _cDomain2,  _iPort1,  _iPort2);
}

int NetClient_GetBitrateOnVideo(unsigned int _ulConID, int* _piX, int* _piY, int* _piEnabled, char* _pcInfo)
{
	if (NULL == FNetClient_GetBitrateOnVideo) {
		return -1;
	}
	return FNetClient_GetBitrateOnVideo( _ulConID,  _piX,  _piY,  _piEnabled,  _pcInfo);
}

int NetClient_SetDecCallBack(unsigned int _ulID, pfCBGetDecAV _cbkGetYUV, void* _iUserData)
{
	if (NULL == FNetClient_SetDecCallBack) {
		return -1;
	}
	return FNetClient_SetDecCallBack( _ulID,  _cbkGetYUV,  _iUserData);
}

int NetClient_SetDecCallBackEx(unsigned int _ulID, DECYUV_NOTIFY _cbkDecYUV, void* _iUserData)
{
	if (NULL == FNetClient_SetDecCallBackEx) {
		return -1;
	}
	return FNetClient_SetDecCallBackEx( _ulID,  _cbkDecYUV,  _iUserData);
}

int NetClient_InterTalkStartEx(unsigned int * _uiConnID, int _iLogonID, NVSDATA_NOTIFY _cbkDataArrive, void* _iUserData)
{
	if (NULL == FNetClient_InterTalkStartEx) {
		return -1;
	}
	return FNetClient_InterTalkStartEx( _uiConnID,  _iLogonID,  _cbkDataArrive,  _iUserData);
}

#endif
int NetClient_Cleanup()
{
	if (NULL == FNetClient_Cleanup) {
		return -1;
	}
	return FNetClient_Cleanup();
}

int NetClient_GetVersion(SDK_VERSION* _ver)
{
	if (NULL == FNetClient_GetVersion) {
		return -1;
	}
	return FNetClient_GetVersion( _ver);
}

int NetClient_Logon(char* _cProxy,char* _cIP,char* _cUserName,
					char* _cPwd,char* _cProductID,int _iPort)
{
	if (NULL == FNetClient_Logon) {
		return -1;
	}
	return FNetClient_Logon( _cProxy,  _cIP,  _cUserName,  _cPwd,  _cProductID,  _iPort);
}

int NetClient_LogonEx(char* _cProxy,char* _cIP,char* _cUserName,
							  char* _cPwd,char* _cProductID,int _iPort,const char* _pcCharSet)
{
	if (NULL == FNetClient_LogonEx) {
		return -1;
	}
	return FNetClient_LogonEx( _cProxy,  _cIP,  _cUserName,  _cPwd,  _cProductID,  _iPort,  _pcCharSet);
}

int NetClient_Logoff(int _iLogonID)
{
	if (NULL == FNetClient_Logoff) {
		return -1;
	}
	return FNetClient_Logoff( _iLogonID);
}

int NetClient_GetLogonStatus(int _iLogonID)
{
	if (NULL == FNetClient_GetLogonStatus) {
		return -1;
	}
	return FNetClient_GetLogonStatus( _iLogonID);
}

int NetClient_ProxyGetDevInfo(int _iLogonID, int _iCmd, void* _pvOutBuf, int _iBufLen)
{
	if (NULL == FNetClient_ProxyGetDevInfo) {
		return -1;
	}
	return FNetClient_ProxyGetDevInfo( _iLogonID,  _iCmd,  _pvOutBuf,  _iBufLen);
}

int NetClient_StopRecv(unsigned int _uiRecvID)
{
	if (NULL == FNetClient_StopRecv) {
		return -1;
	}
	return FNetClient_StopRecv( _uiRecvID);
}

int NetClient_GetRecvID(int _iLogonID, int _iChannel, int _iStreamNO)
{
	if (NULL == FNetClient_GetRecvID) {
		return -1;
	}
	return FNetClient_GetRecvID( _iLogonID,  _iChannel,  _iStreamNO);
}

int NetClient_GetInfoByConnectID(unsigned int _iConnectID,st_ConnectInfo* _stConnectInfo)
{
	if (NULL == FNetClient_GetInfoByConnectID) {
		return -1;
	}
	return FNetClient_GetInfoByConnectID( _iConnectID,  _stConnectInfo);
}

int NetClient_SetFullStreamNotify (unsigned int _uiRecvID, FULLFRAME_NOTIFY _cbkFullStream)
{
	if (NULL == FNetClient_SetFullStreamNotify ) {
		return -1;
	}
	return FNetClient_SetFullStreamNotify ( _uiRecvID,  _cbkFullStream);
}

int NetClient_SetFullStreamNotify_V4 (unsigned int _uiRecvID, FULLFRAME_NOTIFY_V4 _cbkFullStream, void * _iUserData)
{
	if (NULL == FNetClient_SetFullStreamNotify_V4 ) {
		return -1;
	}
	return FNetClient_SetFullStreamNotify_V4 ( _uiRecvID,  _cbkFullStream,  _iUserData);
}

int NetClient_GetCmdString(int _iLogonID,int _iType,int _iPara,CMDSTRING_NOTIFY _cbkCmdString,void* _pUserData)
{
	if (NULL == FNetClient_GetCmdString) {
		return -1;
	}
	return FNetClient_GetCmdString( _iLogonID,  _iType,  _iPara,  _cbkCmdString,  _pUserData);
}

int NetClient_GetDevInfo(int _iLogonID,ENCODERINFO* _pEncoderInfo)
{
	if (NULL == FNetClient_GetDevInfo) {
		return -1;
	}
	return FNetClient_GetDevInfo( _iLogonID,  _pEncoderInfo);
}

int NetClient_SendDataToServer(int _iLogonID,char* _cData,int _iLen)
{
	if (NULL == FNetClient_SendDataToServer) {
		return -1;
	}
	return FNetClient_SendDataToServer( _iLogonID,  _cData,  _iLen);
}

int NetClient_IsValidUser(int _iLogonID,char* _cUserName,char* _cPwd)
{
	if (NULL == FNetClient_IsValidUser) {
		return -1;
	}
	return FNetClient_IsValidUser( _iLogonID,  _cUserName,  _cPwd);
}

int NetClient_SetInnerDataNotify(unsigned int _uiRecvID,INNER_DATA_NOTIFY _cbkNotify,void* _iUserData)
{
	if (NULL == FNetClient_SetInnerDataNotify) {
		return -1;
	}
	return FNetClient_SetInnerDataNotify( _uiRecvID,  _cbkNotify,  _iUserData);
}

int NetClient_SetWorkMode(int _iWorkMode)
{
	if (NULL == FNetClient_SetWorkMode) {
		return -1;
	}
	return FNetClient_SetWorkMode( _iWorkMode);
}

#ifdef WIN32
int NetClient_AddActiveServer(char* _cDevIP,char* _cFactoryID,void* _iSocket, void* _pUserData)
{
	if (NULL == FNetClient_AddActiveServer) {
		return -1;
	}
	return FNetClient_AddActiveServer( _cDevIP,  _cFactoryID,  _iSocket,  _pUserData);
}

int NetClient_BindSocket(int _iLogonID,int _iChan,void* _iSocket, void* _pUserData)
{
	if (NULL == FNetClient_BindSocket) {
		return -1;
	}
	return FNetClient_BindSocket( _iLogonID,  _iChan,  _iSocket,  _pUserData);
}

#else
int NetClient_AddActiveServer(char* _cDevIP,char* _cFactoryID,int _iSocket, void* _pUserData)
{
	if (NULL == FNetClient_AddActiveServer) {
		return -1;
	}
	return FNetClient_AddActiveServer( _cDevIP,  _cFactoryID,  _iSocket,  _pUserData);
}

int NetClient_BindSocket(int _iLogonID,int _iChan,int _iSocket, void* _pUserData)
{
	if (NULL == FNetClient_BindSocket) {
		return -1;
	}
	return FNetClient_BindSocket( _iLogonID,  _iChan,  _iSocket,  _pUserData);
}

#endif
int NetClient_PushData(int _iLogonID,int _iChan,char* _cData,int _iLen)
{
	if (NULL == FNetClient_PushData) {
		return -1;
	}
	return FNetClient_PushData( _iLogonID,  _iChan,  _cData,  _iLen);
}

int NetClient_DelActiveServer(int _iLogonID)
{
	if (NULL == FNetClient_DelActiveServer) {
		return -1;
	}
	return FNetClient_DelActiveServer( _iLogonID);
}

int NetClient_StartCaptureData(unsigned long _ulID)
{
	if (NULL == FNetClient_StartCaptureData) {
		return -1;
	}
	return FNetClient_StartCaptureData( _ulID);
}

int NetClient_StopCaptureData(unsigned long _ulID)
{
	if (NULL == FNetClient_StopCaptureData) {
		return -1;
	}
	return FNetClient_StopCaptureData( _ulID);
}

int NetClient_GetVideoHeader(unsigned long _ulID,unsigned char* _ucHeader)
{
	if (NULL == FNetClient_GetVideoHeader) {
		return -1;
	}
	return FNetClient_GetVideoHeader( _ulID,  _ucHeader);
}

int NetClient_SetRawFrameCallBack(unsigned int _ulConID, RAWFRAME_NOTIFY _cbkGetFrame, void* _pContext)
{
	if (NULL == FNetClient_SetRawFrameCallBack) {
		return -1;
	}
	return FNetClient_SetRawFrameCallBack( _ulConID,  _cbkGetFrame,  _pContext);
}

int NetClient_SetRawFrameCallBackEx(unsigned int _ulConID, RAWFRAME_NOTIFY_EX _cbkGetFrame, void* _pContext)
{
	if (NULL == FNetClient_SetRawFrameCallBackEx) {
		return -1;
	}
	return FNetClient_SetRawFrameCallBackEx( _ulConID,  _cbkGetFrame,  _pContext);
}

int NetClient_StartCaptureFile(unsigned int _uiRecvID, char* _pszFileName, int _iRecordFileType)
{
	if (NULL == FNetClient_StartCaptureFile) {
		return -1;
	}
	return FNetClient_StartCaptureFile( _uiRecvID,  _pszFileName,  _iRecordFileType);
}

int NetClient_StopCaptureFile(unsigned int _uiRecvID)
{
	if (NULL == FNetClient_StopCaptureFile) {
		return -1;
	}
	return FNetClient_StopCaptureFile( _uiRecvID);
}

int NetClient_GetCaptureStatus(unsigned int _ulConID)
{
	if (NULL == FNetClient_GetCaptureStatus) {
		return -1;
	}
	return FNetClient_GetCaptureStatus( _ulConID);
}

int NetClient_SetCaptureFileSize(unsigned int _uiRecvID, int _iFileSize)
{
	if (NULL == FNetClient_SetCaptureFileSize) {
		return -1;
	}
	return FNetClient_SetCaptureFileSize( _uiRecvID,  _iFileSize);
}

int NetClient_StartPlay(unsigned int _ulID, int _hWnd, RECT _rcShow, unsigned int _uiDecflag)
{
	if (NULL == FNetClient_StartPlay) {
		return -1;
	}
	return FNetClient_StartPlay( _ulID,  _hWnd,  _rcShow,  _uiDecflag);
}

int NetClient_StartPlayEx(unsigned int _ulID, void* _pvBuff, int _iBuffSize)
{
	if (NULL == FNetClient_StartPlayEx) {
		return -1;
	}
	return FNetClient_StartPlayEx( _ulID,  _pvBuff,  _iBuffSize);
}

int NetClient_StartPlayEs(unsigned int _ulID, int _hWnd)
{
	if (NULL == FNetClient_StartPlayEs) {
		return -1;
	}
	return FNetClient_StartPlayEs( _ulID,  _hWnd);
}

int NetClient_StopPlay(unsigned int _ulID)
{
	if (NULL == FNetClient_StopPlay) {
		return -1;
	}
	return FNetClient_StopPlay( _ulID);
}

int NetClient_StopPlayEx(unsigned int _ulID, unsigned int _iParam)
{
	if (NULL == FNetClient_StopPlayEx) {
		return -1;
	}
	return FNetClient_StopPlayEx( _ulID,  _iParam);
}

int NetClient_SetPlayRectEx(unsigned int _ulID, RECT* _pDrawRect, int _dwMask)
{
	if (NULL == FNetClient_SetPlayRectEx) {
		return -1;
	}
	return FNetClient_SetPlayRectEx( _ulID,  _pDrawRect,  _dwMask);
}

int NetClient_SetSrcRect(unsigned int _ulID, void* _pSrcRect)
{
	if (NULL == FNetClient_SetSrcRect) {
		return -1;
	}
	return FNetClient_SetSrcRect( _ulID,  _pSrcRect);
}

int NetClient_ResetPlayerWnd(unsigned int _ulID, int _hwnd)
{
	if (NULL == FNetClient_ResetPlayerWnd) {
		return -1;
	}
	return FNetClient_ResetPlayerWnd( _ulID,  _hwnd);
}

int NetClient_GetPlayingStatus(unsigned int _ulID)
{
	if (NULL == FNetClient_GetPlayingStatus) {
		return -1;
	}
	return FNetClient_GetPlayingStatus( _ulID);
}

int NetClient_AdjustVideo(unsigned int _ulID, RECT _rctShow)
{
	if (NULL == FNetClient_AdjustVideo) {
		return -1;
	}
	return FNetClient_AdjustVideo( _ulID,  _rctShow);
}

int NetClient_AudioStart(unsigned int _ulID)
{
	if (NULL == FNetClient_AudioStart) {
		return -1;
	}
	return FNetClient_AudioStart( _ulID);
}

int NetClient_AudioStop(unsigned int _ulID)
{
	if (NULL == FNetClient_AudioStop) {
		return -1;
	}
	return FNetClient_AudioStop( _ulID);
}

int NetClient_SetLocalAudioVolume(int _iVolume)
{
	if (NULL == FNetClient_SetLocalAudioVolume) {
		return -1;
	}
	return FNetClient_SetLocalAudioVolume( _iVolume);
}

int NetClient_SetBufferNum(unsigned int _ulID, int _iNum)
{
	if (NULL == FNetClient_SetBufferNum) {
		return -1;
	}
	return FNetClient_SetBufferNum( _ulID,  _iNum);
}

int NetClient_SetPlayDelay(unsigned long _ulID, int _iNum)
{
	if (NULL == FNetClient_SetPlayDelay) {
		return -1;
	}
	return FNetClient_SetPlayDelay( _ulID,  _iNum);
}

int NetClient_GetChannelNum(int _iLogonID, int* _piChanNum)
{
	if (NULL == FNetClient_GetChannelNum) {
		return -1;
	}
	return FNetClient_GetChannelNum( _iLogonID,  _piChanNum);
}

int NetClient_GetAlarmPortNum(int _iLogonID, int* _iAlarmInPortNum, int* _iAlarmOutPortNum)
{
	if (NULL == FNetClient_GetAlarmPortNum) {
		return -1;
	}
	return FNetClient_GetAlarmPortNum( _iLogonID,  _iAlarmInPortNum,  _iAlarmOutPortNum);
}

int NetClient_GetLocalAlarmNum(int _iLogonID, int* _iLocalAlarmInNum, int* _iLocalAlarmOutNum)
{
	if (NULL == FNetClient_GetLocalAlarmNum) {
		return -1;
	}
	return FNetClient_GetLocalAlarmNum( _iLogonID,  _iLocalAlarmInNum,  _iLocalAlarmOutNum);
}

int NetClient_SetVideoPara(int _iLogonID, int _iChannelNum, STR_VideoParam* _strVideoParam)
{
	if (NULL == FNetClient_SetVideoPara) {
		return -1;
	}
	return FNetClient_SetVideoPara( _iLogonID,  _iChannelNum,  _strVideoParam);
}

int NetClient_GetVideoPara(int _iLogonID, int _iChannelNum, STR_VideoParam* _strVideoParam)
{
	if (NULL == FNetClient_GetVideoPara) {
		return -1;
	}
	return FNetClient_GetVideoPara( _iLogonID,  _iChannelNum,  _strVideoParam);
}

int NetClient_SetVideoparaSchedule(int _iLogonID, int _iChannelNum,STR_VideoParam* _strVideoParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_SetVideoparaSchedule) {
		return -1;
	}
	return FNetClient_SetVideoparaSchedule( _iLogonID,  _iChannelNum,  _strVideoParam);
}

int NetClient_GetVideoparaSchedule(int _iLogonID, int _iChannelNum,STR_VideoParam* _strVideoParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_GetVideoparaSchedule) {
		return -1;
	}
	return FNetClient_GetVideoparaSchedule( _iLogonID,  _iChannelNum,  _strVideoParam);
}

int NetClient_SetVideoQuality(int _iLogonID, int _iChannelNum, int _iVideoQuality, int _iStreamNO)
{
	if (NULL == FNetClient_SetVideoQuality) {
		return -1;
	}
	return FNetClient_SetVideoQuality( _iLogonID,  _iChannelNum,  _iVideoQuality,  _iStreamNO);
}

int NetClient_GetVideoQuality(int _iLogonID,int _iChannelNum,int* _iVideoQuality, int _iStreamNO)
{
	if (NULL == FNetClient_GetVideoQuality) {
		return -1;
	}
	return FNetClient_GetVideoQuality( _iLogonID,  _iChannelNum,  _iVideoQuality,  _iStreamNO);
}

int NetClient_SetFrameRate(int _iLogonID, int _iChannelNum, int _iFrameRate, int _iStreamNO)
{
	if (NULL == FNetClient_SetFrameRate) {
		return -1;
	}
	return FNetClient_SetFrameRate( _iLogonID,  _iChannelNum,  _iFrameRate,  _iStreamNO);
}

int NetClient_GetFrameRate(int _iLogonID, int _iChannelNum, int* _iFrameRate, int _iStreamNO)
{
	if (NULL == FNetClient_GetFrameRate) {
		return -1;
	}
	return FNetClient_GetFrameRate( _iLogonID,  _iChannelNum,  _iFrameRate,  _iStreamNO);
}

int NetClient_GetDecordFrameNum (unsigned int _ulConID)
{
	if (NULL == FNetClient_GetDecordFrameNum ) {
		return -1;
	}
	return FNetClient_GetDecordFrameNum ( _ulConID);
}

int NetClient_SetStreamType(int _iLogonID, int _iChannelNum, int _iStreamType, int _iStreamNO)
{
	if (NULL == FNetClient_SetStreamType) {
		return -1;
	}
	return FNetClient_SetStreamType( _iLogonID,  _iChannelNum,  _iStreamType,  _iStreamNO);
}

int NetClient_GetStreamType(int _iLogonID, int _iChannelNum, int* _iStreamType, int _iStreamNO)
{
	if (NULL == FNetClient_GetStreamType) {
		return -1;
	}
	return FNetClient_GetStreamType( _iLogonID,  _iChannelNum,  _iStreamType,  _iStreamNO);
}

int NetClient_SetMotionAreaEnable (int _iLogonID,int _iChannelNum)
{
	if (NULL == FNetClient_SetMotionAreaEnable ) {
		return -1;
	}
	return FNetClient_SetMotionAreaEnable ( _iLogonID,  _iChannelNum);
}

int NetClient_SetMotionDetetionArea(int _iLogonID, int _iChannelNum, int _ix, int _iy,int _iEnabled)
{
	if (NULL == FNetClient_SetMotionDetetionArea) {
		return -1;
	}
	return FNetClient_SetMotionDetetionArea( _iLogonID,  _iChannelNum,  _ix,  _iy,  _iEnabled);
}

int NetClient_GetMotionDetetionArea(int _iLogonID, int _iChannelNum, int _ix, int _iy,int* _iEnabled)
{
	if (NULL == FNetClient_GetMotionDetetionArea) {
		return -1;
	}
	return FNetClient_GetMotionDetetionArea( _iLogonID,  _iChannelNum,  _ix,  _iy,  _iEnabled);
}

int NetClient_SetThreshold(int _iLogonID, int _iChannelNum,int _iThreshold)
{
	if (NULL == FNetClient_SetThreshold) {
		return -1;
	}
	return FNetClient_SetThreshold( _iLogonID,  _iChannelNum,  _iThreshold);
}

int NetClient_GetThreshold(int _iLogonID, int _iChannelNum,int* _iThreshold)
{
	if (NULL == FNetClient_GetThreshold) {
		return -1;
	}
	return FNetClient_GetThreshold( _iLogonID,  _iChannelNum,  _iThreshold);
}

int NetClient_SetMotionDetection(int _iLogonID, int _iChannelNum,int _iEnabled)
{
	if (NULL == FNetClient_SetMotionDetection) {
		return -1;
	}
	return FNetClient_SetMotionDetection( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_GetMotionDetection(int _iLogonID, int _iChannelNum,int* _iEnabled)
{
	if (NULL == FNetClient_GetMotionDetection) {
		return -1;
	}
	return FNetClient_GetMotionDetection( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_SetMotionDecLinkRec(int _iLogonID,int _iChannelNum,int _iEnable)
{
	if (NULL == FNetClient_SetMotionDecLinkRec) {
		return -1;
	}
	return FNetClient_SetMotionDecLinkRec( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_GetMotionDecLinkRec(int _iLogonID,int _iChannelNum,int* _iEnable)
{
	if (NULL == FNetClient_GetMotionDecLinkRec) {
		return -1;
	}
	return FNetClient_GetMotionDecLinkRec( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_SetMotionDecLinkSnap(int _iLogonID,int _iChannelNum,int _iEnable)
{
	if (NULL == FNetClient_SetMotionDecLinkSnap) {
		return -1;
	}
	return FNetClient_SetMotionDecLinkSnap( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_GetMotionDecLinkSnap(int _iLogonID,int _iChannelNum,int* _iEnable)
{
	if (NULL == FNetClient_GetMotionDecLinkSnap) {
		return -1;
	}
	return FNetClient_GetMotionDecLinkSnap( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_SetMotionDecLinkSoundDisplay(int _iLogonID,int _iChannelNum,int _iSoundEnable,int _iDisplayEnable)
{
	if (NULL == FNetClient_SetMotionDecLinkSoundDisplay) {
		return -1;
	}
	return FNetClient_SetMotionDecLinkSoundDisplay( _iLogonID,  _iChannelNum,  _iSoundEnable,  _iDisplayEnable);
}

int NetClient_GetMotionDecLinkSoundDisplay(int _iLogonID,int _iChannelNum,int* _iSoundEnable,int* _iDisplayEnable)
{
	if (NULL == FNetClient_GetMotionDecLinkSoundDisplay) {
		return -1;
	}
	return FNetClient_GetMotionDecLinkSoundDisplay( _iLogonID,  _iChannelNum,  _iSoundEnable,  _iDisplayEnable);
}

int NetClient_SetMotionDecLinkOutport(int _iLogonID,int _iChannelNum,int _iOutportArray)
{
	if (NULL == FNetClient_SetMotionDecLinkOutport) {
		return -1;
	}
	return FNetClient_SetMotionDecLinkOutport( _iLogonID,  _iChannelNum,  _iOutportArray);
}

int NetClient_GetMotionDecLinkOutport(int _iLogonID,int _iChannelNum,int* _iOutportArray)
{
	if (NULL == FNetClient_GetMotionDecLinkOutport) {
		return -1;
	}
	return FNetClient_GetMotionDecLinkOutport( _iLogonID,  _iChannelNum,  _iOutportArray);
}

int NetClient_SetMotionDetectSchedule(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_SetMotionDetectSchedule) {
		return -1;
	}
	return FNetClient_SetMotionDetectSchedule( _iLogonID,  _iChannelNum,  _iWeekday,  _strScheduleParam);
}

int NetClient_GetMotionDetectSchedule(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_GetMotionDetectSchedule) {
		return -1;
	}
	return FNetClient_GetMotionDetectSchedule( _iLogonID,  _iChannelNum,  _iWeekday,  _strScheduleParam);
}

int NetClient_SetOsdDiaphaneity(int _iLogonID,int _iChannelNum,int _iDiaphaneity)
{
	if (NULL == FNetClient_SetOsdDiaphaneity) {
		return -1;
	}
	return FNetClient_SetOsdDiaphaneity( _iLogonID,  _iChannelNum,  _iDiaphaneity);
}

int NetClient_GetOsdDiaphaneity(int _iLogonID,int _iChannelNum,int* _iDiaphaneity)
{
	if (NULL == FNetClient_GetOsdDiaphaneity) {
		return -1;
	}
	return FNetClient_GetOsdDiaphaneity( _iLogonID,  _iChannelNum,  _iDiaphaneity);
}

int NetClient_SetOsdText(int _iLogonID, int _iChannelNum, unsigned char * _pcTxtPtr, unsigned long _ulTextColor)
{
	if (NULL == FNetClient_SetOsdText) {
		return -1;
	}
	return FNetClient_SetOsdText( _iLogonID,  _iChannelNum,  _pcTxtPtr,  _ulTextColor);
}

int NetClient_GetOsdText(int _iLogonID, int _iChannelNum, unsigned char* _pcOSDText, unsigned long* _pulTextColor)
{
	if (NULL == FNetClient_GetOsdText) {
		return -1;
	}
	return FNetClient_GetOsdText( _iLogonID,  _iChannelNum,  _pcOSDText,  _pulTextColor);
}

int NetClient_SetOsdType(int _iLogonID, int _iChannelNum,int _iPositionX,int _iPositionY,int _iOSDType,int _iEnabled)
{
	if (NULL == FNetClient_SetOsdType) {
		return -1;
	}
	return FNetClient_SetOsdType( _iLogonID,  _iChannelNum,  _iPositionX,  _iPositionY,  _iOSDType,  _iEnabled);
}

int NetClient_GetOsdType(int _iLogonID, int _iChannelNum, int _iOSDType, int* _iPositionX ,int* _iPositionY , int* _iEnabled)
{
	if (NULL == FNetClient_GetOsdType) {
		return -1;
	}
	return FNetClient_GetOsdType( _iLogonID,  _iChannelNum,  _iOSDType,  _iPositionX ,  _iPositionY ,  _iEnabled);
}

int NetClient_SetDateFormat(int _iLogonID, int _iFormat, char _cSeparate)
{
	if (NULL == FNetClient_SetDateFormat) {
		return -1;
	}
	return FNetClient_SetDateFormat( _iLogonID,  _iFormat,  _cSeparate);
}

int NetClient_GetDateFormat(int _iLogonID, int* _iFormat, char* _cSeparate)
{
	if (NULL == FNetClient_GetDateFormat) {
		return -1;
	}
	return FNetClient_GetDateFormat( _iLogonID,  _iFormat,  _cSeparate);
}

int NetClient_SetOsdLOGO (int _iLogonID, int _iChannelNum, unsigned char * _cLogoFile, unsigned int _ulbkColor)
{
	if (NULL == FNetClient_SetOsdLOGO ) {
		return -1;
	}
	return FNetClient_SetOsdLOGO ( _iLogonID,  _iChannelNum,  _cLogoFile,  _ulbkColor);
}

int NetClient_SetAudioChannel(int _iLogonID, int _iChannelNum, int _iAudioChannel)
{
	if (NULL == FNetClient_SetAudioChannel) {
		return -1;
	}
	return FNetClient_SetAudioChannel( _iLogonID,  _iChannelNum,  _iAudioChannel);
}

int NetClient_GetAudioChannel(int _iLogonID, int _iChannelNum, int* _iAudioChannel)
{
	if (NULL == FNetClient_GetAudioChannel) {
		return -1;
	}
	return FNetClient_GetAudioChannel( _iLogonID,  _iChannelNum,  _iAudioChannel);
}

int NetClient_SetIpFilter(int _iLogonID, char* _cFilterIP,char* _cFilterMask)
{
	if (NULL == FNetClient_SetIpFilter) {
		return -1;
	}
	return FNetClient_SetIpFilter( _iLogonID,  _cFilterIP,  _cFilterMask);
}

int NetClient_GetIpFilter(int _iLogonID, char* _cFilterIP,char* _cFilterMask)
{
	if (NULL == FNetClient_GetIpFilter) {
		return -1;
	}
	return FNetClient_GetIpFilter( _iLogonID,  _cFilterIP,  _cFilterMask);
}

int NetClient_SetAlarmOutput(int _iLogonID, int _iAlarmInput, unsigned long _ulAlarmOutput)
{
	if (NULL == FNetClient_SetAlarmOutput) {
		return -1;
	}
	return FNetClient_SetAlarmOutput( _iLogonID,  _iAlarmInput,  _ulAlarmOutput);
}

int NetClient_GetAlarmOutput(int _iLogonID, int _iAlarmInput, unsigned long* _ulAlarmOutput)
{
	if (NULL == FNetClient_GetAlarmOutput) {
		return -1;
	}
	return FNetClient_GetAlarmOutput( _iLogonID,  _iAlarmInput,  _ulAlarmOutput);
}

int NetClient_GetAlarmIPortState(int _iLogonID, int _iInPort,int* _iState)
{
	if (NULL == FNetClient_GetAlarmIPortState) {
		return -1;
	}
	return FNetClient_GetAlarmIPortState( _iLogonID,  _iInPort,  _iState);
}

int NetClient_SetAlarmPortEnable(int _iLogonID,int _iInPort,int _iEnabled)
{
	if (NULL == FNetClient_SetAlarmPortEnable) {
		return -1;
	}
	return FNetClient_SetAlarmPortEnable( _iLogonID,  _iInPort,  _iEnabled);
}

int NetClient_GetAlarmPortEnable(int _iLogonID,int _iInPort,int* _iEnabled)
{
	if (NULL == FNetClient_GetAlarmPortEnable) {
		return -1;
	}
	return FNetClient_GetAlarmPortEnable( _iLogonID,  _iInPort,  _iEnabled);
}

int NetClient_SetInportAlarmLinkRec(int _iLogonID,int _iPortNo,int _iEnable)
{
	if (NULL == FNetClient_SetInportAlarmLinkRec) {
		return -1;
	}
	return FNetClient_SetInportAlarmLinkRec( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_GetInportAlarmLinkRec(int _iLogonID,int _iPortNo,int* _iEnable)
{
	if (NULL == FNetClient_GetInportAlarmLinkRec) {
		return -1;
	}
	return FNetClient_GetInportAlarmLinkRec( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_SetInportAlarmLinkSnap(int _iLogonID,int _iPortNo,int _iEnable)
{
	if (NULL == FNetClient_SetInportAlarmLinkSnap) {
		return -1;
	}
	return FNetClient_SetInportAlarmLinkSnap( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_GetInportAlarmLinkSnap(int _iLogonID,int _iPortNo,int* _iEnable)
{
	if (NULL == FNetClient_GetInportAlarmLinkSnap) {
		return -1;
	}
	return FNetClient_GetInportAlarmLinkSnap( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_SetInportAlarmLinkPTZ(int _iLogonID,int _iPortNo,int _iLinkChannelNum,int _iLinkType,int _iActNum)
{
	if (NULL == FNetClient_SetInportAlarmLinkPTZ) {
		return -1;
	}
	return FNetClient_SetInportAlarmLinkPTZ( _iLogonID,  _iPortNo,  _iLinkChannelNum,  _iLinkType,  _iActNum);
}

int NetClient_GetInportAlarmLinkPTZ(int _iLogonID,int _iPortNo,int _iLinkChannelNum,int* _iLinkType,int* _iActNum)
{
	if (NULL == FNetClient_GetInportAlarmLinkPTZ) {
		return -1;
	}
	return FNetClient_GetInportAlarmLinkPTZ( _iLogonID,  _iPortNo,  _iLinkChannelNum,  _iLinkType,  _iActNum);
}

int NetClient_SetInportAlarmLinkSoundDisplay(int _iLogonID,int _iPortNo,int _iSoundEnable,int _iDisplayEnable)
{
	if (NULL == FNetClient_SetInportAlarmLinkSoundDisplay) {
		return -1;
	}
	return FNetClient_SetInportAlarmLinkSoundDisplay( _iLogonID,  _iPortNo,  _iSoundEnable,  _iDisplayEnable);
}

int NetClient_GetInportAlarmLinkSoundDisplay(int _iLogonID,int _iPortNo,int* _iSoundEnable,int* _iDisplayEnable)
{
	if (NULL == FNetClient_GetInportAlarmLinkSoundDisplay) {
		return -1;
	}
	return FNetClient_GetInportAlarmLinkSoundDisplay( _iLogonID,  _iPortNo,  _iSoundEnable,  _iDisplayEnable);
}

int NetClient_SetInportAlmLinkOutport(int _iLogonID,int _iPortNo,int _iOutportArray)
{
	if (NULL == FNetClient_SetInportAlmLinkOutport) {
		return -1;
	}
	return FNetClient_SetInportAlmLinkOutport( _iLogonID,  _iPortNo,  _iOutportArray);
}

int NetClient_GetInportAlmLinkOutport(int _iLogonID,int _iPortNo,int* _iOutportArray)
{
	if (NULL == FNetClient_GetInportAlmLinkOutport) {
		return -1;
	}
	return FNetClient_GetInportAlmLinkOutport( _iLogonID,  _iPortNo,  _iOutportArray);
}

int NetClient_SetAlarmInMode(int _iLogonID, int _iPortNum, int _iLowOrHigh)
{
	if (NULL == FNetClient_SetAlarmInMode) {
		return -1;
	}
	return FNetClient_SetAlarmInMode( _iLogonID,  _iPortNum,  _iLowOrHigh);
}

int NetClient_GetAlarmInMode(int _iLogonID, int _iPortNum, int* _iLowOrHigh)
{
	if (NULL == FNetClient_GetAlarmInMode) {
		return -1;
	}
	return FNetClient_GetAlarmInMode( _iLogonID,  _iPortNum,  _iLowOrHigh);
}

int NetClient_SetAlarmOutMode(int _iLogonID, int _iPortNum, int _iLowOrHigh, int _iPulseWidth)
{
	if (NULL == FNetClient_SetAlarmOutMode) {
		return -1;
	}
	return FNetClient_SetAlarmOutMode( _iLogonID,  _iPortNum,  _iLowOrHigh,  _iPulseWidth);
}

int NetClient_GetAlarmOutMode(int _iLogonID, int _iPortNum, int* _iLowOrHigh, int* _iPulseWidth)
{
	if (NULL == FNetClient_GetAlarmOutMode) {
		return -1;
	}
	return FNetClient_GetAlarmOutMode( _iLogonID,  _iPortNum,  _iLowOrHigh,  _iPulseWidth);
}

int NetClient_SetInportAlarmSchedule(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_SetInportAlarmSchedule) {
		return -1;
	}
	return FNetClient_SetInportAlarmSchedule( _iLogonID,  _iPortNo,  _iWeekday,  _strScheduleParam);
}

int NetClient_GetInportAlarmSchedule(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_GetInportAlarmSchedule) {
		return -1;
	}
	return FNetClient_GetInportAlarmSchedule( _iLogonID,  _iPortNo,  _iWeekday,  _strScheduleParam);
}

int NetClient_SetOutportAlarmSchedule(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_SetOutportAlarmSchedule) {
		return -1;
	}
	return FNetClient_SetOutportAlarmSchedule( _iLogonID,  _iPortNo,  _iWeekday,  _strScheduleParam);
}

int NetClient_GetOutportAlarmSchedule(int _iLogonID,int _iPortNo,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_GetOutportAlarmSchedule) {
		return -1;
	}
	return FNetClient_GetOutportAlarmSchedule( _iLogonID,  _iPortNo,  _iWeekday,  _strScheduleParam);
}

int NetClient_SetOutportAlarmDelay(int _iLogonID,int _iPortNo,int _iClearType,int _iDelayTime)
{
	if (NULL == FNetClient_SetOutportAlarmDelay) {
		return -1;
	}
	return FNetClient_SetOutportAlarmDelay( _iLogonID,  _iPortNo,  _iClearType,  _iDelayTime);
}

int NetClient_GetOutportAlarmDelay(int _iLogonID,int _iPortNo,int* _iClearType,int* _iDelayTime)
{
	if (NULL == FNetClient_GetOutportAlarmDelay) {
		return -1;
	}
	return FNetClient_GetOutportAlarmDelay( _iLogonID,  _iPortNo,  _iClearType,  _iDelayTime);
}

int NetClient_SetInportEnable(int _iLogonID, int _iPortNo,int _iEnable)
{
	if (NULL == FNetClient_SetInportEnable) {
		return -1;
	}
	return FNetClient_SetInportEnable( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_GetInportEnable(int _iLogonID, int _iPortNo,int* _iEnable)
{
	if (NULL == FNetClient_GetInportEnable) {
		return -1;
	}
	return FNetClient_GetInportEnable( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_SetOutportEnable(int _iLogonID, int _iPortNo,int _iEnable)
{
	if (NULL == FNetClient_SetOutportEnable) {
		return -1;
	}
	return FNetClient_SetOutportEnable( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_GetOutportEnable(int _iLogonID, int _iPortNo,int* _iEnable)
{
	if (NULL == FNetClient_GetOutportEnable) {
		return -1;
	}
	return FNetClient_GetOutportEnable( _iLogonID,  _iPortNo,  _iEnable);
}

int NetClient_SetOutportState(int _iLogonID, int _iPort, int _iState)
{
	if (NULL == FNetClient_SetOutportState) {
		return -1;
	}
	return FNetClient_SetOutportState( _iLogonID,  _iPort,  _iState);
}

int NetClient_GetOutportState(int _iLogonID, int _iPort, int* _iState)
{
	if (NULL == FNetClient_GetOutportState) {
		return -1;
	}
	return FNetClient_GetOutportState( _iLogonID,  _iPort,  _iState);
}

int NetClient_SetAlmVdoCovThreshold(int _iLogonID, int _iChannelNum, int _iThreshold)
{
	if (NULL == FNetClient_SetAlmVdoCovThreshold) {
		return -1;
	}
	return FNetClient_SetAlmVdoCovThreshold( _iLogonID,  _iChannelNum,  _iThreshold);
}

int NetClient_GetAlmVdoCovThreshold(int _iLogonID, int _iChannelNum, int* _iThreshold)
{
	if (NULL == FNetClient_GetAlmVdoCovThreshold) {
		return -1;
	}
	return FNetClient_GetAlmVdoCovThreshold( _iLogonID,  _iChannelNum,  _iThreshold);
}

int NetClient_SetAlmVideoCov(int _iLogonID, int _iChannelNum, int _iEnabled)
{
	if (NULL == FNetClient_SetAlmVideoCov) {
		return -1;
	}
	return FNetClient_SetAlmVideoCov( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_GetAlmVideoCov(int _iLogonID, int _iChannelNum, int* _iEnabled)
{
	if (NULL == FNetClient_GetAlmVideoCov) {
		return -1;
	}
	return FNetClient_GetAlmVideoCov( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_SetDeviceType(int _iLogonID,int _iChannelNum,int _iComNo,int _iDevAddress,const char* _pcProtocol)
{
	if (NULL == FNetClient_SetDeviceType) {
		return -1;
	}
	return FNetClient_SetDeviceType( _iLogonID,  _iChannelNum,  _iComNo,  _iDevAddress,  _pcProtocol);
}

int NetClient_GetDeviceType(int _iLogonID,int _iChannelNum,int* _iComPort,int* _iDevAddress ,char* _cDeviceType)
{
	if (NULL == FNetClient_GetDeviceType) {
		return -1;
	}
	return FNetClient_GetDeviceType( _iLogonID,  _iChannelNum,  _iComPort,  _iDevAddress ,  _cDeviceType);
}

int NetClient_SetComFormat(int _iLogonID,int _iComPort,char* _cDeviceType,char* _cComFormat,int _iWorkMode)
{
	if (NULL == FNetClient_SetComFormat) {
		return -1;
	}
	return FNetClient_SetComFormat( _iLogonID,  _iComPort,  _cDeviceType,  _cComFormat,  _iWorkMode);
}

int NetClient_GetComFormat(int _iLogonID, int _iCom, char* _cComFormat,int* _iWorkMode)
{
	if (NULL == FNetClient_GetComFormat) {
		return -1;
	}
	return FNetClient_GetComFormat( _iLogonID,  _iCom,  _cComFormat,  _iWorkMode);
}

int NetClient_GetAllSupportDeviceType(int _iLogonID, int* _iSumDeviceType, char* _cDeviceType)
{
	if (NULL == FNetClient_GetAllSupportDeviceType) {
		return -1;
	}
	return FNetClient_GetAllSupportDeviceType( _iLogonID,  _iSumDeviceType,  _cDeviceType);
}

int NetClient_DeviceCtrl(int _iLogonID, int _iChannelNum,int _iActionType, int _iParam1,int _iParam2)
{
	if (NULL == FNetClient_DeviceCtrl) {
		return -1;
	}
	return FNetClient_DeviceCtrl( _iLogonID,  _iChannelNum,  _iActionType,  _iParam1,  _iParam2);
}

int NetClient_DeviceCtrlEx(int _iLogonID,int _iChannelNum,int _iActionType,int _iParam1,int _iParam2,int _iControlType)
{
	if (NULL == FNetClient_DeviceCtrlEx) {
		return -1;
	}
	return FNetClient_DeviceCtrlEx( _iLogonID,  _iChannelNum,  _iActionType,  _iParam1,  _iParam2,  _iControlType);
}

int NetClient_ComSend(int _iLogonID, unsigned char* _ucBuf, int _iLength, int _iComNo)
{
	if (NULL == FNetClient_ComSend) {
		return -1;
	}
	return FNetClient_ComSend( _iLogonID,  _ucBuf,  _iLength,  _iComNo);
}

int NetClient_DevicePTZCtrl(int _iLogonID, int _iProtocolType, int _iActionType, int _iComNo, int _iAddress, int _iSpeed, int _iPresetNO)
{
	if (NULL == FNetClient_DevicePTZCtrl) {
		return -1;
	}
	return FNetClient_DevicePTZCtrl( _iLogonID,  _iProtocolType,  _iActionType,  _iComNo,  _iAddress,  _iSpeed,  _iPresetNO);
}

int NetClient_GetComPortCounts(int _iLogonID, int* _piComPortCounts, int* _piComPortEnabledStatus)
{
	if (NULL == FNetClient_GetComPortCounts) {
		return -1;
	}
	return FNetClient_GetComPortCounts( _iLogonID,  _piComPortCounts,  _piComPortEnabledStatus);
}

int NetClient_SetAlarmVideoLost(int _iLogonID, int _iChannelNum,int _iEnabled)
{
	if (NULL == FNetClient_SetAlarmVideoLost) {
		return -1;
	}
	return FNetClient_SetAlarmVideoLost( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_GetAlarmVideoLost(int _iLogonID, int _iChannelNum, int* _iEnabled)
{
	if (NULL == FNetClient_GetAlarmVideoLost) {
		return -1;
	}
	return FNetClient_GetAlarmVideoLost( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_SetVideoLostLinkPTZ(int _iLogonID,int _iChannelNum,int _iLinkChannelNum,int _iLinkType,int _iActNum)
{
	if (NULL == FNetClient_SetVideoLostLinkPTZ) {
		return -1;
	}
	return FNetClient_SetVideoLostLinkPTZ( _iLogonID,  _iChannelNum,  _iLinkChannelNum,  _iLinkType,  _iActNum);
}

int NetClient_GetVideoLostLinkPTZ(int _iLogonID,int _iChannelNum,int _iLinkChannelNum,int* _iLinkType,int* _iActNum)
{
	if (NULL == FNetClient_GetVideoLostLinkPTZ) {
		return -1;
	}
	return FNetClient_GetVideoLostLinkPTZ( _iLogonID,  _iChannelNum,  _iLinkChannelNum,  _iLinkType,  _iActNum);
}

int NetClient_SetVideoLostLinkSoundDisplay(int _iLogonID,int _iChannelNum,int _iSoundEnable,int _iDisplayEnable )
{
	if (NULL == FNetClient_SetVideoLostLinkSoundDisplay) {
		return -1;
	}
	return FNetClient_SetVideoLostLinkSoundDisplay( _iLogonID,  _iChannelNum,  _iSoundEnable,  _iDisplayEnable );
}

int NetClient_GetVideoLostLinkSoundDisplay(int _iLogonID,int _iChannelNum,int* _iSoundEnable,int* _iDisplayEnable)
{
	if (NULL == FNetClient_GetVideoLostLinkSoundDisplay) {
		return -1;
	}
	return FNetClient_GetVideoLostLinkSoundDisplay( _iLogonID,  _iChannelNum,  _iSoundEnable,  _iDisplayEnable);
}

int NetClient_GetAlarmVLostState(int _iLogonID, int _iChannel,int* _iState)
{
	if (NULL == FNetClient_GetAlarmVLostState) {
		return -1;
	}
	return FNetClient_GetAlarmVLostState( _iLogonID,  _iChannel,  _iState);
}

int NetClient_SetVideoLostLinkOutport(int _iLogonID,int _iChannelNum,int _iOutportArray)
{
	if (NULL == FNetClient_SetVideoLostLinkOutport) {
		return -1;
	}
	return FNetClient_SetVideoLostLinkOutport( _iLogonID,  _iChannelNum,  _iOutportArray);
}

int NetClient_GetVideoLostLinkOutport(int _iLogonID,int _iChannelNum,int* _iOutportArray)
{
	if (NULL == FNetClient_GetVideoLostLinkOutport) {
		return -1;
	}
	return FNetClient_GetVideoLostLinkOutport( _iLogonID,  _iChannelNum,  _iOutportArray);
}

int NetClient_SetVideoLostSchedule(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_SetVideoLostSchedule) {
		return -1;
	}
	return FNetClient_SetVideoLostSchedule( _iLogonID,  _iChannelNum,  _iWeekday,  _strScheduleParam);
}

int NetClient_GetVideoLostSchedule(int _iLogonID,int _iChannelNum,int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_GetVideoLostSchedule) {
		return -1;
	}
	return FNetClient_GetVideoLostSchedule( _iLogonID,  _iChannelNum,  _iWeekday,  _strScheduleParam);
}

int NetClient_SetVideoLostLinkRec(int _iLogonID,int _iChannelNum,int _iEnable)
{
	if (NULL == FNetClient_SetVideoLostLinkRec) {
		return -1;
	}
	return FNetClient_SetVideoLostLinkRec( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_GetVideoLostLinkRec(int _iLogonID,int _iChannelNum,int* _iEnable)
{
	if (NULL == FNetClient_GetVideoLostLinkRec) {
		return -1;
	}
	return FNetClient_GetVideoLostLinkRec( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_SetVideoLostLinkSnap(int _iLogonID,int _iChannelNum,int _iEnable)
{
	if (NULL == FNetClient_SetVideoLostLinkSnap) {
		return -1;
	}
	return FNetClient_SetVideoLostLinkSnap( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_GetVideoLostLinkSnap(int _iLogonID,int _iChannelNum,int* _iEnable)
{
	if (NULL == FNetClient_GetVideoLostLinkSnap) {
		return -1;
	}
	return FNetClient_GetVideoLostLinkSnap( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_SetIFrameRate(int _iLogonID, int _iChannelNum, int _iFrameRate, int _iStreamNO)
{
	if (NULL == FNetClient_SetIFrameRate) {
		return -1;
	}
	return FNetClient_SetIFrameRate( _iLogonID,  _iChannelNum,  _iFrameRate,  _iStreamNO);
}

int NetClient_GetIFrameRate(int _iLogonID, int _iChannelNum, int* _iFrameRate, int _iStreamNO)
{
	if (NULL == FNetClient_GetIFrameRate) {
		return -1;
	}
	return FNetClient_GetIFrameRate( _iLogonID,  _iChannelNum,  _iFrameRate,  _iStreamNO);
}

int NetClient_ForceIFrame(int _iLogonID, int _iChannelNum, int _iStreamNO)
{
	if (NULL == FNetClient_ForceIFrame) {
		return -1;
	}
	return FNetClient_ForceIFrame( _iLogonID,  _iChannelNum,  _iStreamNO);
}

int NetClient_SetTime(int _iLogonID, int _iyy, int _imo, int _idd, int _ihh, int _imi, int _iss)
{
	if (NULL == FNetClient_SetTime) {
		return -1;
	}
	return FNetClient_SetTime( _iLogonID,  _iyy,  _imo,  _idd,  _ihh,  _imi,  _iss);
}

int NetClient_Reboot(int _iLogonID)
{
	if (NULL == FNetClient_Reboot) {
		return -1;
	}
	return FNetClient_Reboot( _iLogonID);
}

int NetClient_RebootEx(int _iLogonID, int _iChannelNo)
{
	if (NULL == FNetClient_RebootEx) {
		return -1;
	}
	return FNetClient_RebootEx( _iLogonID,  _iChannelNo);
}

int NetClient_DefaultPara(int _iLogonID)
{
	if (NULL == FNetClient_DefaultPara) {
		return -1;
	}
	return FNetClient_DefaultPara( _iLogonID);
}

int NetClient_DefaultParaEx(int _iLogonID, int _iType)
{
	if (NULL == FNetClient_DefaultParaEx) {
		return -1;
	}
	return FNetClient_DefaultParaEx( _iLogonID,  _iType);
}

int NetClient_GetServerVersion(int _iLogonID, char* _cVer)
{
	if (NULL == FNetClient_GetServerVersion) {
		return -1;
	}
	return FNetClient_GetServerVersion( _iLogonID,  _cVer);
}

int NetClient_SetNVS(int _iLogonID, int _iVideo, int _iCheck)
{
	if (NULL == FNetClient_SetNVS) {
		return -1;
	}
	return FNetClient_SetNVS( _iLogonID,  _iVideo,  _iCheck);
}

int NetClient_UpgradeProgram(int _iLogonID, char* _cFileName, PROUPGRADE_NOTIFY _UpgradeNotify)
{
	if (NULL == FNetClient_UpgradeProgram) {
		return -1;
	}
	return FNetClient_UpgradeProgram( _iLogonID,  _cFileName,  _UpgradeNotify);
}

int NetClient_UpgradeWebPage(int _iLogonID, char* _cFileName, WEBUPGRADE_NOTIFY _UpgradeWebNotify)
{
	if (NULL == FNetClient_UpgradeWebPage) {
		return -1;
	}
	return FNetClient_UpgradeWebPage( _iLogonID,  _cFileName,  _UpgradeWebNotify);
}

int NetClient_GetUpgradePercent(int _iLogonID)
{
	if (NULL == FNetClient_GetUpgradePercent) {
		return -1;
	}
	return FNetClient_GetUpgradePercent( _iLogonID);
}

int NetClient_GetUserNum(int _iLogonID,int* _iUserNum)
{
	if (NULL == FNetClient_GetUserNum) {
		return -1;
	}
	return FNetClient_GetUserNum( _iLogonID,  _iUserNum);
}

int NetClient_GetUserInfo(int _iLogonID,int _iUserSerial,char* _cUserName,char* _cPassword,int* _iAuthority)
{
	if (NULL == FNetClient_GetUserInfo) {
		return -1;
	}
	return FNetClient_GetUserInfo( _iLogonID,  _iUserSerial,  _cUserName,  _cPassword,  _iAuthority);
}

int NetClient_GetCurUserInfo(int _iLogonID, char _cUserName[16], char _cPassword[16], int* _iAuthority)
{
	if (NULL == FNetClient_GetCurUserInfo) {
		return -1;
	}
	return FNetClient_GetCurUserInfo( _iLogonID,  _cUserName,  _cPassword,  _iAuthority);
}

int NetClient_AddUser(int _iLogonID, char* _cUserName, char* _cPassword, int _iAuthority)
{
	if (NULL == FNetClient_AddUser) {
		return -1;
	}
	return FNetClient_AddUser( _iLogonID,  _cUserName,  _cPassword,  _iAuthority);
}

int NetClient_DelUser(int _iLogonID, char* _cUserName)
{
	if (NULL == FNetClient_DelUser) {
		return -1;
	}
	return FNetClient_DelUser( _iLogonID,  _cUserName);
}

int NetClient_ModifyPwd(int _iLogonID, char* _cUserName, char* _cNewPwd)
{
	if (NULL == FNetClient_ModifyPwd) {
		return -1;
	}
	return FNetClient_ModifyPwd( _iLogonID,  _cUserName,  _cNewPwd);
}

int NetClient_SetMaxConUser(int _iLogonID, int _iMaxConUser)
{
	if (NULL == FNetClient_SetMaxConUser) {
		return -1;
	}
	return FNetClient_SetMaxConUser( _iLogonID,  _iMaxConUser);
}

int NetClient_GetMaxGetUser(int _iLogonID,int* _iMaxConUser)
{
	if (NULL == FNetClient_GetMaxGetUser) {
		return -1;
	}
	return FNetClient_GetMaxGetUser( _iLogonID,  _iMaxConUser);
}

int NetClient_TalkStart(int _iLogonID, int _iUser)
{
	if (NULL == FNetClient_TalkStart) {
		return -1;
	}
	return FNetClient_TalkStart( _iLogonID,  _iUser);
}

int NetClient_TalkEnd(int _iLogonID)
{
	if (NULL == FNetClient_TalkEnd) {
		return -1;
	}
	return FNetClient_TalkEnd( _iLogonID);
}

int NetClient_InputTalkingdata(unsigned char* _ucData, unsigned int _iLen)
{
	if (NULL == FNetClient_InputTalkingdata) {
		return -1;
	}
	return FNetClient_InputTalkingdata( _ucData,  _iLen);
}

int NetClient_GetTalkingState(int _iLogonID,int* _iTalkState)
{
	if (NULL == FNetClient_GetTalkingState) {
		return -1;
	}
	return FNetClient_GetTalkingState( _iLogonID,  _iTalkState);
}

int NetClient_CapturePic(unsigned int _ulConID,unsigned char** _pucData)
{
	if (NULL == FNetClient_CapturePic) {
		return -1;
	}
	return FNetClient_CapturePic( _ulConID,  _pucData);
}

int NetClient_CaptureBmpPic(unsigned int _ulConID, char* _pcFileName)
{
	if (NULL == FNetClient_CaptureBmpPic) {
		return -1;
	}
	return FNetClient_CaptureBmpPic( _ulConID,  _pcFileName);
}

int NetClient_ChangeSvrIP(int _iLogonID, char* _cNewSvrIP, char* _cMask, char* _cGateway, char* _cDNS)
{
	if (NULL == FNetClient_ChangeSvrIP) {
		return -1;
	}
	return FNetClient_ChangeSvrIP( _iLogonID,  _cNewSvrIP,  _cMask,  _cGateway,  _cDNS);
}

int NetClient_GetIpProperty(int _iLogonID, char* _cMAC, char* _cMask, char* _cGateway, char* _cDNS)
{
	if (NULL == FNetClient_GetIpProperty) {
		return -1;
	}
	return FNetClient_GetIpProperty( _iLogonID,  _cMAC,  _cMask,  _cGateway,  _cDNS);
}

int NetClient_SetDHCPParam(int _iLogonID,int _iDHCP)
{
	if (NULL == FNetClient_SetDHCPParam) {
		return -1;
	}
	return FNetClient_SetDHCPParam( _iLogonID,  _iDHCP);
}

int NetClient_GetDHCPParam(int _iLogonID,int* _iDHCP)
{
	if (NULL == FNetClient_GetDHCPParam) {
		return -1;
	}
	return FNetClient_GetDHCPParam( _iLogonID,  _iDHCP);
}

int NetClient_SetWifiDHCPParam(int _iLogonID,int _iDHCP)
{
	if (NULL == FNetClient_SetWifiDHCPParam) {
		return -1;
	}
	return FNetClient_SetWifiDHCPParam( _iLogonID,  _iDHCP);
}

int NetClient_GetWifiDHCPParam(int _iLogonID,int* _iDHCP)
{
	if (NULL == FNetClient_GetWifiDHCPParam) {
		return -1;
	}
	return FNetClient_GetWifiDHCPParam( _iLogonID,  _iDHCP);
}

int NetClient_GetVideoCovArea(int _iLogonID, int _iChannelNum, RECT* _rect, int _iStreamNO)
{
	if (NULL == FNetClient_GetVideoCovArea) {
		return -1;
	}
	return FNetClient_GetVideoCovArea( _iLogonID,  _iChannelNum,  _rect,  _iStreamNO);
}

int NetClient_SetVideoCovArea(int _iLogonID, int _iChannelNum, RECT* _rect, int _iStreamNO)
{
	if (NULL == FNetClient_SetVideoCovArea) {
		return -1;
	}
	return FNetClient_SetVideoCovArea( _iLogonID,  _iChannelNum,  _rect,  _iStreamNO);
}

int NetClient_GetVideoSize(int _iLogonID, int _iChannelNum, int* _width, int * _height, int _iStreamNO)
{
	if (NULL == FNetClient_GetVideoSize) {
		return -1;
	}
	return FNetClient_GetVideoSize( _iLogonID,  _iChannelNum,  _width,  _height,  _iStreamNO);
}

int NetClient_SetVideoSize(int _iLogonID, int _iChannelNum, int _iVideoSize, int _iStreamNO)
{
	if (NULL == FNetClient_SetVideoSize) {
		return -1;
	}
	return FNetClient_SetVideoSize( _iLogonID,  _iChannelNum,  _iVideoSize,  _iStreamNO);
}

int NetClient_GetVideoSizeEx(int _iLogonID, int _iChannelNum, int* _ivideoSize, int _iStreamNO)
{
	if (NULL == FNetClient_GetVideoSizeEx) {
		return -1;
	}
	return FNetClient_GetVideoSizeEx( _iLogonID,  _iChannelNum,  _ivideoSize,  _iStreamNO);
}

int NetClient_GetMaxMinorVideoSize(int _iLogonID, int* _iMinorVideoSize)
{
	if (NULL == FNetClient_GetMaxMinorVideoSize) {
		return -1;
	}
	return FNetClient_GetMaxMinorVideoSize( _iLogonID,  _iMinorVideoSize);
}

int NetClient_BindInterface(int _interface)
{
	if (NULL == FNetClient_BindInterface) {
		return -1;
	}
	return FNetClient_BindInterface( _interface);
}

char* NetClient_GetNetInterface(int _interface)
{
	if (NULL == FNetClient_GetNetInterface) {
		return NULL;
	}
	return FNetClient_GetNetInterface( _interface);
}

int NetClient_SetMaxKByteRate(int _iLogonID, int _iChannelNum, int _ibitRate, int _iStreamNO)
{
	if (NULL == FNetClient_SetMaxKByteRate) {
		return -1;
	}
	return FNetClient_SetMaxKByteRate( _iLogonID,  _iChannelNum,  _ibitRate,  _iStreamNO);
}

int NetClient_GetMaxKByteRate(int _iLogonID, int _iChannelNum, int* _ibitRate, int _iStreamNO)
{
	if (NULL == FNetClient_GetMaxKByteRate) {
		return -1;
	}
	return FNetClient_GetMaxKByteRate( _iLogonID,  _iChannelNum,  _ibitRate,  _iStreamNO);
}

int NetClient_WriteUserData(int _iLogonID,int _iOffset,unsigned char* _u8Buf,int _iLength)
{
	if (NULL == FNetClient_WriteUserData) {
		return -1;
	}
	return FNetClient_WriteUserData( _iLogonID,  _iOffset,  _u8Buf,  _iLength);
}

int NetClient_ReadUserData(int _iLogonID,int _iOffset,unsigned char* _u8Buf,int _iLength)
{
	if (NULL == FNetClient_ReadUserData) {
		return -1;
	}
	return FNetClient_ReadUserData( _iLogonID,  _iOffset,  _u8Buf,  _iLength);
}

int NetClient_SetReducenoiseState(int _iLogonID, int _iChannelNum,int _iState)
{
	if (NULL == FNetClient_SetReducenoiseState) {
		return -1;
	}
	return FNetClient_SetReducenoiseState( _iLogonID,  _iChannelNum,  _iState);
}

int NetClient_GetReducenoiseState(int _iLogonID,int _iChannelNum,int* _iState)
{
	if (NULL == FNetClient_GetReducenoiseState) {
		return -1;
	}
	return FNetClient_GetReducenoiseState( _iLogonID,  _iChannelNum,  _iState);
}

int NetClient_DrawTextOnVideo(int _iLogonID, int _iChannelNum, int _iX, int _iY, char* _cText, int _iStore, int _iStreamNO)
{
	if (NULL == FNetClient_DrawTextOnVideo) {
		return -1;
	}
	return FNetClient_DrawTextOnVideo( _iLogonID,  _iChannelNum,  _iX,  _iY,  _cText,  _iStore,  _iStreamNO);
}

int NetClient_GetTextOnVideo(int _iLogonID, int _iChannelNum, int* _iX, int* _iY, char* _cText,int _iStreamNO)
{
	if (NULL == FNetClient_GetTextOnVideo) {
		return -1;
	}
	return FNetClient_GetTextOnVideo( _iLogonID,  _iChannelNum,  _iX,  _iY,  _cText,  _iStreamNO);
}

int NetClient_SetBothStreamSame(int _iLogonID, int _iChannelNum, int _iState)
{
	if (NULL == FNetClient_SetBothStreamSame) {
		return -1;
	}
	return FNetClient_SetBothStreamSame( _iLogonID,  _iChannelNum,  _iState);
}

int NetClient_GetBothStreamSame(int _iLogonID, int _iChannelNum, int* _iState)
{
	if (NULL == FNetClient_GetBothStreamSame) {
		return -1;
	}
	return FNetClient_GetBothStreamSame( _iLogonID,  _iChannelNum,  _iState);
}

int NetClient_ShowBitrateOnVideo(unsigned int _ulConID, int _iX, int _iY, int _iEnabled)
{
	if (NULL == FNetClient_ShowBitrateOnVideo) {
		return -1;
	}
	return FNetClient_ShowBitrateOnVideo( _ulConID,  _iX,  _iY,  _iEnabled);
}

int NetClient_SetPPPoEInfo(int _iLogonID,char* _cAccount, char* _cPassword, int _iEnabled)
{
	if (NULL == FNetClient_SetPPPoEInfo) {
		return -1;
	}
	return FNetClient_SetPPPoEInfo( _iLogonID,  _cAccount,  _cPassword,  _iEnabled);
}

int NetClient_GetPPPoEInfo(int _iLogonID,char* _cAccount, char* _cPassword, int* _iEnabled)
{
	if (NULL == FNetClient_GetPPPoEInfo) {
		return -1;
	}
	return FNetClient_GetPPPoEInfo( _iLogonID,  _cAccount,  _cPassword,  _iEnabled);
}

int NetClient_CPUCheckEnabled(int _iEnabled, int _interval)
{
	if (NULL == FNetClient_CPUCheckEnabled) {
		return -1;
	}
	return FNetClient_CPUCheckEnabled( _iEnabled,  _interval);
}

int NetClient_SetEncodeMode(int _iLogonID, int _iChannelNum, int _iStreamNO, int _iMode)
{
	if (NULL == FNetClient_SetEncodeMode) {
		return -1;
	}
	return FNetClient_SetEncodeMode( _iLogonID,  _iChannelNum,  _iStreamNO,  _iMode);
}

int NetClient_GetEncodeMode(int _iLogonID, int _iChannelNum, int _iStreamNO, int* _iMode)
{
	if (NULL == FNetClient_GetEncodeMode) {
		return -1;
	}
	return FNetClient_GetEncodeMode( _iLogonID,  _iChannelNum,  _iStreamNO,  _iMode);
}

int NetClient_SetPreferMode(int _iLogonID, int _iChannelNum, int _iStreamNO, int _iMode)
{
	if (NULL == FNetClient_SetPreferMode) {
		return -1;
	}
	return FNetClient_SetPreferMode( _iLogonID,  _iChannelNum,  _iStreamNO,  _iMode);
}

int NetClient_GetPreferMode(int _iLogonID, int _iChannelNum, int _iStreamNO, int* _iMode)
{
	if (NULL == FNetClient_GetPreferMode) {
		return -1;
	}
	return FNetClient_GetPreferMode( _iLogonID,  _iChannelNum,  _iStreamNO,  _iMode);
}

int NetClient_LogFileSetProperty(int _iLogonID, int _iLevel, int _iSize)
{
	if (NULL == FNetClient_LogFileSetProperty) {
		return -1;
	}
	return FNetClient_LogFileSetProperty( _iLogonID,  _iLevel,  _iSize);
}

int NetClient_LogFileGetProperty(int _iLogonID, int* _iLevel, int* _iSize)
{
	if (NULL == FNetClient_LogFileGetProperty) {
		return -1;
	}
	return FNetClient_LogFileGetProperty( _iLogonID,  _iLevel,  _iSize);
}

int NetClient_LogFileDownload(int _iLogonID)
{
	if (NULL == FNetClient_LogFileDownload) {
		return -1;
	}
	return FNetClient_LogFileDownload( _iLogonID);
}

int NetClient_LogFileClear(int _iLogonID)
{
	if (NULL == FNetClient_LogFileClear) {
		return -1;
	}
	return FNetClient_LogFileClear( _iLogonID);
}

int NetClient_LogFileGetDetails(int _iLogonID, char* _cBuf, int* _iLen)
{
	if (NULL == FNetClient_LogFileGetDetails) {
		return -1;
	}
	return FNetClient_LogFileGetDetails( _iLogonID,  _cBuf,  _iLen);
}

int NetClient_GetVideoNPMode(int _iLogonID, VIDEO_NORM* _vMode)
{
	if (NULL == FNetClient_GetVideoNPMode) {
		return -1;
	}
	return FNetClient_GetVideoNPMode( _iLogonID,  _vMode);
}

int NetClient_SetVideoNPMode(int _iLogonID, VIDEO_NORM _vMode)
{
	if (NULL == FNetClient_SetVideoNPMode) {
		return -1;
	}
	return FNetClient_SetVideoNPMode( _iLogonID,  _vMode);
}

int NetClient_SetAudioEncoder(int _iLogonID, int _iChannel, AUDIO_ENCODER _aCoder)
{
	if (NULL == FNetClient_SetAudioEncoder) {
		return -1;
	}
	return FNetClient_SetAudioEncoder( _iLogonID,  _iChannel,  _aCoder);
}

int NetClient_GetAudioEncoder(int _iLogonID, int _iChannel, AUDIO_ENCODER* _aCoder)
{
	if (NULL == FNetClient_GetAudioEncoder) {
		return -1;
	}
	return FNetClient_GetAudioEncoder( _iLogonID,  _iChannel,  _aCoder);
}

int NetClient_NetFileQuery(int _iLogonID, PNVS_FILE_QUERY _ptFileQuery)
{
	if (NULL == FNetClient_NetFileQuery) {
		return -1;
	}
	return FNetClient_NetFileQuery( _iLogonID,  _ptFileQuery);
}

int NetClient_NetFileSetRecordRule(int _iLogonID, int _iRule, int _iTimelen, int _iFreedisk ,int _iFileSize)
{
	if (NULL == FNetClient_NetFileSetRecordRule) {
		return -1;
	}
	return FNetClient_NetFileSetRecordRule( _iLogonID,  _iRule,  _iTimelen,  _iFreedisk ,  _iFileSize);
}

int NetClient_NetFileGetRecordRule(int _iLogonID, int* _iRule, int* _iTimelen, int* _iFreedisk,int* _iFileSize)
{
	if (NULL == FNetClient_NetFileGetRecordRule) {
		return -1;
	}
	return FNetClient_NetFileGetRecordRule( _iLogonID,  _iRule,  _iTimelen,  _iFreedisk,  _iFileSize);
}

int NetClient_NetFileSetAlarmRule(int _iLogonID, int _iPreRecordEnable, int _iPreRecordTime, int _iDelayTime ,int _iDelayEnable ,int _iChannelNum)
{
	if (NULL == FNetClient_NetFileSetAlarmRule) {
		return -1;
	}
	return FNetClient_NetFileSetAlarmRule( _iLogonID,  _iPreRecordEnable,  _iPreRecordTime,  _iDelayTime ,  _iDelayEnable ,  _iChannelNum);
}

int NetClient_NetFileGetAlarmRule(int _iLogonID, int* _iPreEnable, int* _iPreTime, int* _iDelayTime ,int* _iDelayEnable,int _iChannelNum)
{
	if (NULL == FNetClient_NetFileGetAlarmRule) {
		return -1;
	}
	return FNetClient_NetFileGetAlarmRule( _iLogonID,  _iPreEnable,  _iPreTime,  _iDelayTime ,  _iDelayEnable,  _iChannelNum);
}

int NetClient_NetFileSetAlarmState(int _iLogonID, int _iChannel, int _iState)
{
	if (NULL == FNetClient_NetFileSetAlarmState) {
		return -1;
	}
	return FNetClient_NetFileSetAlarmState( _iLogonID,  _iChannel,  _iState);
}

int NetClient_NetFileGetAlarmState(int _iLogonID, int _iChannel, int* _iState)
{
	if (NULL == FNetClient_NetFileGetAlarmState) {
		return -1;
	}
	return FNetClient_NetFileGetAlarmState( _iLogonID,  _iChannel,  _iState);
}

int NetClient_NetFileSetTaskState(int _iLogonID, int _iChannel, int _iState)
{
	if (NULL == FNetClient_NetFileSetTaskState) {
		return -1;
	}
	return FNetClient_NetFileSetTaskState( _iLogonID,  _iChannel,  _iState);
}

int NetClient_NetFileGetTaskState(int _iLogonID, int _iChannel, int* _iState)
{
	if (NULL == FNetClient_NetFileGetTaskState) {
		return -1;
	}
	return FNetClient_NetFileGetTaskState( _iLogonID,  _iChannel,  _iState);
}

int NetClient_NetFileSetTaskSchedule(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME* _Schedule[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_NetFileSetTaskSchedule) {
		return -1;
	}
	return FNetClient_NetFileSetTaskSchedule( _iLogonID,  _iChannel,  _iWeekday,  _Schedule);
}

int NetClient_NetFileGetTaskSchedule(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME* _Schedule[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_NetFileGetTaskSchedule) {
		return -1;
	}
	return FNetClient_NetFileGetTaskSchedule( _iLogonID,  _iChannel,  _iWeekday,  _Schedule);
}

int NetClient_NetFileSetTaskScheduleEx(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME_Ex* _Schedule[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_NetFileSetTaskScheduleEx) {
		return -1;
	}
	return FNetClient_NetFileSetTaskScheduleEx( _iLogonID,  _iChannel,  _iWeekday,  _Schedule);
}

int NetClient_NetFileGetTaskScheduleEx(int _iLogonID, int _iChannel, int _iWeekday, NVS_SCHEDTIME_Ex* _Schedule[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_NetFileGetTaskScheduleEx) {
		return -1;
	}
	return FNetClient_NetFileGetTaskScheduleEx( _iLogonID,  _iChannel,  _iWeekday,  _Schedule);
}

int NetClient_NetFileGetFileCount(int _iLogonID, int* _piTotalCount, int* _piCurrentCount)
{
	if (NULL == FNetClient_NetFileGetFileCount) {
		return -1;
	}
	return FNetClient_NetFileGetFileCount( _iLogonID,  _piTotalCount,  _piCurrentCount);
}

int NetClient_NetFileRebuildIndexFile(int _iLogonID, int _iState)
{
	if (NULL == FNetClient_NetFileRebuildIndexFile) {
		return -1;
	}
	return FNetClient_NetFileRebuildIndexFile( _iLogonID,  _iState);
}

int NetClient_NetFileGetDiskInfo(int _iLogonID, PNVS_STORAGEDEV _storage)
{
	if (NULL == FNetClient_NetFileGetDiskInfo) {
		return -1;
	}
	return FNetClient_NetFileGetDiskInfo( _iLogonID,  _storage);
}

int NetClient_NetFileGetDiskInfoEx(int _iLogonID, PNVS_STORAGEDEV _storage, int _iSize)
{
	if (NULL == FNetClient_NetFileGetDiskInfoEx) {
		return -1;
	}
	return FNetClient_NetFileGetDiskInfoEx( _iLogonID,  _storage,  _iSize);
}

int NetClient_NetFileIsSupportStorage(int _iLogonID)
{
	if (NULL == FNetClient_NetFileIsSupportStorage) {
		return -1;
	}
	return FNetClient_NetFileIsSupportStorage( _iLogonID);
}

int NetClient_NetFileDownloadFile(unsigned int* _ulID,
								  int _iLogonID, 
								  char* _cRemoteFilename, 
								  char* _cLocalFilename,
								  int _iFlag,
								  int _iPosition,
								  int _Speed)
{
	if (NULL == FNetClient_NetFileDownloadFile) {
		return -1;
	}
	return FNetClient_NetFileDownloadFile( _ulID,  _iLogonID,  _cRemoteFilename,  _cLocalFilename,  _iFlag,  _iPosition,  _Speed);
}

int NetClient_NetFileDownloadFileEx(unsigned int* _ulConID,
									int _iLogonID, 
									char* _cRemoteFilename, 
									char* _cLocalFilename,
									int _iFlag,
									int _iPosition,
									int _Speed,
									NVSDATA_NOTIFY _cbkDataArrive,
									void* _iUserData)
{
	if (NULL == FNetClient_NetFileDownloadFileEx) {
		return -1;
	}
	return FNetClient_NetFileDownloadFileEx( _ulConID,  _iLogonID,  _cRemoteFilename,  _cLocalFilename,  _iFlag,  _iPosition,  _Speed,  _cbkDataArrive,  _iUserData);
}

int NetClient_NetFileStopDownloadFile(unsigned int _ulID)
{
	if (NULL == FNetClient_NetFileStopDownloadFile) {
		return -1;
	}
	return FNetClient_NetFileStopDownloadFile( _ulID);
}

int NetClient_NetFileGetDownloadPos(unsigned int _ulID, int* _iPos, int* _iDLSize)
{
	if (NULL == FNetClient_NetFileGetDownloadPos) {
		return -1;
	}
	return FNetClient_NetFileGetDownloadPos( _ulID,  _iPos,  _iDLSize);
}

int NetClient_NetFileMountUSB(int _iLogonID, int _iState)
{
	if (NULL == FNetClient_NetFileMountUSB) {
		return -1;
	}
	return FNetClient_NetFileMountUSB( _iLogonID,  _iState);
}

int NetClient_NetFileGetRecordState(int _iLogonID, int _iChannel, RECORD_STATE* _piState)
{
	if (NULL == FNetClient_NetFileGetRecordState) {
		return -1;
	}
	return FNetClient_NetFileGetRecordState( _iLogonID,  _iChannel,  _piState);
}

int NetClient_NetFileDelFile(int _iLogonID,const char* _pcFileName)
{
	if (NULL == FNetClient_NetFileDelFile) {
		return -1;
	}
	return FNetClient_NetFileDelFile( _iLogonID,  _pcFileName);
}

int NetClient_DiskSetUsage(int _iLogonID,int _iDiskNo,int _iUsage)
{
	if (NULL == FNetClient_DiskSetUsage) {
		return -1;
	}
	return FNetClient_DiskSetUsage( _iLogonID,  _iDiskNo,  _iUsage);
}

int NetClient_NetFileGetQueryfile(int _iLogonID, int _iFileIndex, PNVS_FILE_DATA _fileInfo)
{
	if (NULL == FNetClient_NetFileGetQueryfile) {
		return -1;
	}
	return FNetClient_NetFileGetQueryfile( _iLogonID,  _iFileIndex,  _fileInfo);
}

int NetClient_DiskFormat(int _iLogonID,int _iDiskNo,int _iFsType)
{
	if (NULL == FNetClient_DiskFormat) {
		return -1;
	}
	return FNetClient_DiskFormat( _iLogonID,  _iDiskNo,  _iFsType);
}

int NetClient_DiskPart(int _iLogonID,int _iDiskNo,int _iPartNum,int _iFormatNow)
{
	if (NULL == FNetClient_DiskPart) {
		return -1;
	}
	return FNetClient_DiskPart( _iLogonID,  _iDiskNo,  _iPartNum,  _iFormatNow);
}

int NetClient_NetFileManualRecord(int _iLogonID, int _iChannel, int _iState)
{
	if (NULL == FNetClient_NetFileManualRecord) {
		return -1;
	}
	return FNetClient_NetFileManualRecord( _iLogonID,  _iChannel,  _iState);
}

int NetClient_NetFileMapStoreDevice(int _iLogonID,PNVS_NFS_DEV _storeDev)
{
	if (NULL == FNetClient_NetFileMapStoreDevice) {
		return -1;
	}
	return FNetClient_NetFileMapStoreDevice( _iLogonID,  _storeDev);
}

int NetClient_NetFileGetMapStoreDevice(int _iLogonID,PNVS_NFS_DEV _storeDev)
{
	if (NULL == FNetClient_NetFileGetMapStoreDevice) {
		return -1;
	}
	return FNetClient_NetFileGetMapStoreDevice( _iLogonID,  _storeDev);
}

int NetClient_NetFileGetUSBstate(int _iLogonID, int* _iState)
{
	if (NULL == FNetClient_NetFileGetUSBstate) {
		return -1;
	}
	return FNetClient_NetFileGetUSBstate( _iLogonID,  _iState);
}

int NetClient_NetFileSetExtendname(int _iLogonID, char* _cExtend)
{
	if (NULL == FNetClient_NetFileSetExtendname) {
		return -1;
	}
	return FNetClient_NetFileSetExtendname( _iLogonID,  _cExtend);
}

int NetClient_NetFileGetExtendname(int _iLogonID, char* _cExtend)
{
	if (NULL == FNetClient_NetFileGetExtendname) {
		return -1;
	}
	return FNetClient_NetFileGetExtendname( _iLogonID,  _cExtend);
}

int NetClient_ClearDisk(int _iLogonID,int _iDiskNo)
{
	if (NULL == FNetClient_ClearDisk) {
		return -1;
	}
	return FNetClient_ClearDisk( _iLogonID,  _iDiskNo);
}

int NetClient_GetDownloadFailedFileName(int _iLogonID, int _iFileID, char* _pcFileName, int _iFileNameBufSize)
{
	if (NULL == FNetClient_GetDownloadFailedFileName) {
		return -1;
	}
	return FNetClient_GetDownloadFailedFileName( _iLogonID,  _iFileID,  _pcFileName,  _iFileNameBufSize);
}

int NetClient_SetMediaStreamClient(int _iLogonID, int _iChannel, char* _cClientIP, unsigned short _iClientPort, int _iStreamType)
{
	if (NULL == FNetClient_SetMediaStreamClient) {
		return -1;
	}
	return FNetClient_SetMediaStreamClient( _iLogonID,  _iChannel,  _cClientIP,  _iClientPort,  _iStreamType);
}

int NetClient_GetMediaStreamClient(int _iLogonID, int _iChannel, char* _cClientIP, unsigned short* _iClientPort, int* _iStreamType)
{
	if (NULL == FNetClient_GetMediaStreamClient) {
		return -1;
	}
	return FNetClient_GetMediaStreamClient( _iLogonID,  _iChannel,  _cClientIP,  _iClientPort,  _iStreamType);
}

int NetClient_SetEmailAlarm(int _iLogonID, PSMTP_INFO _pSmtp)
{
	if (NULL == FNetClient_SetEmailAlarm) {
		return -1;
	}
	return FNetClient_SetEmailAlarm( _iLogonID,  _pSmtp);
}

int NetClient_GetEmailAlarm(int _iLogonID, PSMTP_INFO _pSmtp)
{
	if (NULL == FNetClient_GetEmailAlarm) {
		return -1;
	}
	return FNetClient_GetEmailAlarm( _iLogonID,  _pSmtp);
}

int NetClient_SetEmailAlarmEnable(int _iLogonID, int _iChannel, int _iEnable)
{
	if (NULL == FNetClient_SetEmailAlarmEnable) {
		return -1;
	}
	return FNetClient_SetEmailAlarmEnable( _iLogonID,  _iChannel,  _iEnable);
}

int NetClient_GetEmailAlarmEnable(int _iLogonID, int _iChannel, int* _iEnable)
{
	if (NULL == FNetClient_GetEmailAlarmEnable) {
		return -1;
	}
	return FNetClient_GetEmailAlarmEnable( _iLogonID,  _iChannel,  _iEnable);
}

int NetClient_SetScene(int _iLogonID, int _iChannel, int _iScene)
{
	if (NULL == FNetClient_SetScene) {
		return -1;
	}
	return FNetClient_SetScene( _iLogonID,  _iChannel,  _iScene);
}

int NetClient_GetScene(int _iLogonID, int _iChannel, int* _iScene)
{
	if (NULL == FNetClient_GetScene) {
		return -1;
	}
	return FNetClient_GetScene( _iLogonID,  _iChannel,  _iScene);
}

int NetClient_SetSensorFlip(int _iLogonID, int _iChannel, int _iFlip)
{
	if (NULL == FNetClient_SetSensorFlip) {
		return -1;
	}
	return FNetClient_SetSensorFlip( _iLogonID,  _iChannel,  _iFlip);
}

int NetClient_GetSensorFlip(int _iLogonID, int _iChannel, int* _iFlip)
{
	if (NULL == FNetClient_GetSensorFlip) {
		return -1;
	}
	return FNetClient_GetSensorFlip( _iLogonID,  _iChannel,  _iFlip);
}

int NetClient_SetSensorMirror(int _iLogonID, int _iChannel, int _iMirror)
{
	if (NULL == FNetClient_SetSensorMirror) {
		return -1;
	}
	return FNetClient_SetSensorMirror( _iLogonID,  _iChannel,  _iMirror);
}

int NetClient_GetSensorMirror(int _iLogonID, int _iChannel, int* _iMirror)
{
	if (NULL == FNetClient_GetSensorMirror) {
		return -1;
	}
	return FNetClient_GetSensorMirror( _iLogonID,  _iChannel,  _iMirror);
}

int NetClient_Snapshot(int _iLogonID, int _iChannel, int _iQvalue)
{
	if (NULL == FNetClient_Snapshot) {
		return -1;
	}
	return FNetClient_Snapshot( _iLogonID,  _iChannel,  _iQvalue);
}

int NetClient_GetFactoryID(int _iLogonID, char* _cFactoryID)
{
	if (NULL == FNetClient_GetFactoryID) {
		return -1;
	}
	return FNetClient_GetFactoryID( _iLogonID,  _cFactoryID);
}

int NetClient_SetWifiParam(int _iLogonID, NVS_WIFI_PARAM* _pWifiParam)
{
	if (NULL == FNetClient_SetWifiParam) {
		return -1;
	}
	return FNetClient_SetWifiParam( _iLogonID,  _pWifiParam);
}

int NetClient_GetWifiParam(int _iLogonID, NVS_WIFI_PARAM* _pWifiParam)
{
	if (NULL == FNetClient_GetWifiParam) {
		return -1;
	}
	return FNetClient_GetWifiParam( _iLogonID,  _pWifiParam);
}

int NetClient_WifiSearch(int _iLogonID)
{
	if (NULL == FNetClient_WifiSearch) {
		return -1;
	}
	return FNetClient_WifiSearch( _iLogonID);
}

int NetClient_GetWifiSearchResult(int _iLogonID, WIFI_INFO** _pWifiInfo, int* _iWifiCount)
{
	if (NULL == FNetClient_GetWifiSearchResult) {
		return -1;
	}
	return FNetClient_GetWifiSearchResult( _iLogonID,  _pWifiInfo,  _iWifiCount);
}

int NetClient_SetPrivacyProtect(int _iLogonID,int _iChannelNum,int _iEnabled)
{
	if (NULL == FNetClient_SetPrivacyProtect) {
		return -1;
	}
	return FNetClient_SetPrivacyProtect( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_GetPrivacyProtect(int _iLogonID,int _iChannelNum,int* _iEnabled)
{
	if (NULL == FNetClient_GetPrivacyProtect) {
		return -1;
	}
	return FNetClient_GetPrivacyProtect( _iLogonID,  _iChannelNum,  _iEnabled);
}

int NetClient_IYUVtoYV12(int _iWidth, int _iHeight, unsigned char* _YUV420)
{
	if (NULL == FNetClient_IYUVtoYV12) {
		return -1;
	}
	return FNetClient_IYUVtoYV12( _iWidth,  _iHeight,  _YUV420);
}

int NetClient_GetDevType(int _iLogonID,int* _iDevType)
{
	if (NULL == FNetClient_GetDevType) {
		return -1;
	}
	return FNetClient_GetDevType( _iLogonID,  _iDevType);
}

int NetClient_GetProductType(int _iLogonID,int* _iType)
{
	if (NULL == FNetClient_GetProductType) {
		return -1;
	}
	return FNetClient_GetProductType( _iLogonID,  _iType);
}

int NetClient_GetProductTypeEx(int _iLogonID, int* _piProductMode, int* _piProductType)
{
	if (NULL == FNetClient_GetProductTypeEx) {
		return -1;
	}
	return FNetClient_GetProductTypeEx( _iLogonID,  _piProductMode,  _piProductType);
}

int NetClient_BackupKernel(int _iLogonID)
{
	if (NULL == FNetClient_BackupKernel) {
		return -1;
	}
	return FNetClient_BackupKernel( _iLogonID);
}

int NetClient_SetUPNPEnable(int _iLogonID,int _iEnable)
{
	if (NULL == FNetClient_SetUPNPEnable) {
		return -1;
	}
	return FNetClient_SetUPNPEnable( _iLogonID,  _iEnable);
}

int NetClient_GetUPNPEnable(int _iLogonID,int* _iEnable)
{
	if (NULL == FNetClient_GetUPNPEnable) {
		return -1;
	}
	return FNetClient_GetUPNPEnable( _iLogonID,  _iEnable);
}

int NetClient_GetSysInfo(int _iLogonID)
{
	if (NULL == FNetClient_GetSysInfo) {
		return -1;
	}
	return FNetClient_GetSysInfo( _iLogonID);
}

int NetClient_SetDDNSPara(int _iLogonID,char* _cDUserName,char* _cDPassword,char* _cDNvsName, char* _cDomain,int _iPort,int _iDEnable)
{
	if (NULL == FNetClient_SetDDNSPara) {
		return -1;
	}
	return FNetClient_SetDDNSPara( _iLogonID,  _cDUserName,  _cDPassword,  _cDNvsName,  _cDomain,  _iPort,  _iDEnable);
}

int NetClient_GetDDNSPara(int _iLogonID,char* _cDUserName,char* _cDPassword,char* _cDNvsName, char* _cDomain,int* _iPort,int* _iDEnable)
{
	if (NULL == FNetClient_GetDDNSPara) {
		return -1;
	}
	return FNetClient_GetDDNSPara( _iLogonID,  _cDUserName,  _cDPassword,  _cDNvsName,  _cDomain,  _iPort,  _iDEnable);
}

int NetClient_SetFuncListArray(int _iLogonID,int _iEnableArray)
{
	if (NULL == FNetClient_SetFuncListArray) {
		return -1;
	}
	return FNetClient_SetFuncListArray( _iLogonID,  _iEnableArray);
}

int NetClient_GetFuncListArray(int _iLogonID,int* _iEnableArray)
{
	if (NULL == FNetClient_GetFuncListArray) {
		return -1;
	}
	return FNetClient_GetFuncListArray( _iLogonID,  _iEnableArray);
}

int NetClient_SendStringToServer(int _iLogonID, char* _cMsg, int _iLen)
{
	if (NULL == FNetClient_SendStringToServer) {
		return -1;
	}
	return FNetClient_SendStringToServer( _iLogonID,  _cMsg,  _iLen);
}

int NetClient_ReceiveString(char* _cIpAddress,int* _iType,char* _cMsg,int* _iLen)
{
	if (NULL == FNetClient_ReceiveString) {
		return -1;
	}
	return FNetClient_ReceiveString( _cIpAddress,  _iType,  _cMsg,  _iLen);
}

int NetClient_SendStringToCenter(char* _cIpAddress, int _iServerPort,char* _cMsg, int _iLen)
{
	if (NULL == FNetClient_SendStringToCenter) {
		return -1;
	}
	return FNetClient_SendStringToCenter( _cIpAddress,  _iServerPort,  _cMsg,  _iLen);
}

int NetClient_SetVencType(int _iLogonID,int _iChannelNum,int _iType)
{
	if (NULL == FNetClient_SetVencType) {
		return -1;
	}
	return FNetClient_SetVencType( _iLogonID,  _iChannelNum,  _iType);
}

int NetClient_GetVencType(int _iLogonID,int _iChannelNum,int* _iType)
{
	if (NULL == FNetClient_GetVencType) {
		return -1;
	}
	return FNetClient_GetVencType( _iLogonID,  _iChannelNum,  _iType);
}

int NetClient_SetComServer(int _iLogonID,char* _cComServer,int _iComServerPort)
{
	if (NULL == FNetClient_SetComServer) {
		return -1;
	}
	return FNetClient_SetComServer( _iLogonID,  _cComServer,  _iComServerPort);
}

int NetClient_GetComServer(int _iLogonID,char* _cComServer,int* _iComServerPort)
{
	if (NULL == FNetClient_GetComServer) {
		return -1;
	}
	return FNetClient_GetComServer( _iLogonID,  _cComServer,  _iComServerPort);
}

int NetClient_Get3GDeviceStatus(int _iLogonID, int* _i3GDeviceType,int* _iStatus,int* _iIntensity,char* _pcIP,char* _pcStarttime)
{
	if (NULL == FNetClient_Get3GDeviceStatus) {
		return -1;
	}
	return FNetClient_Get3GDeviceStatus( _iLogonID,  _i3GDeviceType,  _iStatus,  _iIntensity,  _pcIP,  _pcStarttime);
}

int NetClient_Set3GDialog(int _iLogonID, int _iStartType,int _iStopType,int _iDuration)
{
	if (NULL == FNetClient_Set3GDialog) {
		return -1;
	}
	return FNetClient_Set3GDialog( _iLogonID,  _iStartType,  _iStopType,  _iDuration);
}

int NetClient_Get3GDialog(int _iLogonID, int* _iStartType,int* _iStopType,int* _iDuration)
{
	if (NULL == FNetClient_Get3GDialog) {
		return -1;
	}
	return FNetClient_Get3GDialog( _iLogonID,  _iStartType,  _iStopType,  _iDuration);
}

int NetClient_Set3GMessage(int _iLogonID, char* _cNotify,char* _cPhoneNum1,char* _cPhoneNum2,char* _cPhoneNum3,char* _cPhoneNum4,char* _cPhoneNum5)
{
	if (NULL == FNetClient_Set3GMessage) {
		return -1;
	}
	return FNetClient_Set3GMessage( _iLogonID,  _cNotify,  _cPhoneNum1,  _cPhoneNum2,  _cPhoneNum3,  _cPhoneNum4,  _cPhoneNum5);
}

int NetClient_Get3GMessage(int _iLogonID, char* _cNotify,char* _cPhoneNum1,char* _cPhoneNum2,char* _cPhoneNum3,char* _cPhoneNum4,char* _cPhoneNum5)
{
	if (NULL == FNetClient_Get3GMessage) {
		return -1;
	}
	return FNetClient_Get3GMessage( _iLogonID,  _cNotify,  _cPhoneNum1,  _cPhoneNum2,  _cPhoneNum3,  _cPhoneNum4,  _cPhoneNum5);
}

int NetClient_Set3GTaskSchedule(int _iLogonID, int _iEnable, PNVS_SCHEDTIME _strScheduleParam)
{
	if (NULL == FNetClient_Set3GTaskSchedule) {
		return -1;
	}
	return FNetClient_Set3GTaskSchedule( _iLogonID,  _iEnable,  _strScheduleParam);
}

int NetClient_Get3GTaskSchedule(int _iLogonID, int* _iEnable, PNVS_SCHEDTIME _strScheduleParam)
{
	if (NULL == FNetClient_Get3GTaskSchedule) {
		return -1;
	}
	return FNetClient_Get3GTaskSchedule( _iLogonID,  _iEnable,  _strScheduleParam);
}

int NetClient_Set3GNotify(int _iLogonID, int _iType,char* _cMessage)
{
	if (NULL == FNetClient_Set3GNotify) {
		return -1;
	}
	return FNetClient_Set3GNotify( _iLogonID,  _iType,  _cMessage);
}

int NetClient_Get3GNotify(int _iLogonID, int* _iType,char* _cMessage)
{
	if (NULL == FNetClient_Get3GNotify) {
		return -1;
	}
	return FNetClient_Get3GNotify( _iLogonID,  _iType,  _cMessage);
}

int NetClient_SetHDCamer(int _iLogonID, int _iChannel,int _iFuncNum,int _iValue)
{
	if (NULL == FNetClient_SetHDCamer) {
		return -1;
	}
	return FNetClient_SetHDCamer( _iLogonID,  _iChannel,  _iFuncNum,  _iValue);
}

int NetClient_GetHDCamer(int _iLogonID, int _iChannel,int _iFuncNum,int* _iValue)
{
	if (NULL == FNetClient_GetHDCamer) {
		return -1;
	}
	return FNetClient_GetHDCamer( _iLogonID,  _iChannel,  _iFuncNum,  _iValue);
}

int NetClient_SetAlarmServer(int _iLogonID,const char* _pcAlarmServer,int _iServerPort)
{
	if (NULL == FNetClient_SetAlarmServer) {
		return -1;
	}
	return FNetClient_SetAlarmServer( _iLogonID,  _pcAlarmServer,  _iServerPort);
}

int NetClient_GetAlarmServer(int _iLogonID,char* _pcAlarmServer,int* _iServerPort)
{
	if (NULL == FNetClient_GetAlarmServer) {
		return -1;
	}
	return FNetClient_GetAlarmServer( _iLogonID,  _pcAlarmServer,  _iServerPort);
}

int NetClient_InterTalkStart(unsigned int * _uiConnID, int _iLogonID, int _iUserData )
{
	if (NULL == FNetClient_InterTalkStart) {
		return -1;
	}
	return FNetClient_InterTalkStart( _uiConnID,  _iLogonID,  _iUserData );
}

int NetClient_InterTalkEnd(unsigned int _uiConnID, bool _blStopTalk)
{
	if (NULL == FNetClient_InterTalkEnd) {
		return -1;
	}
	return FNetClient_InterTalkEnd( _uiConnID,  _blStopTalk);
}

int NetClient_NetFileQueryEx(int _iLogonID, PNVS_FILE_QUERY _pfileQuery, int _iSizeOfQuery)
{
	if (NULL == FNetClient_NetFileQueryEx) {
		return -1;
	}
	return FNetClient_NetFileQueryEx( _iLogonID,  _pfileQuery,  _iSizeOfQuery);
}

int NetClient_ControlDeviceRecord(int _iLogonID, int _iChannelID, int _iRecordType, int _iAction)
{
	if (NULL == FNetClient_ControlDeviceRecord) {
		return -1;
	}
	return FNetClient_ControlDeviceRecord( _iLogonID,  _iChannelID,  _iRecordType,  _iAction);
}

int NetClient_NetFileDownloadByTimeSpan(unsigned int* _ulConID, int _iLogonID, char* _pcLocalFile, 
					int _iChannelNO, unsigned int _uiFromSecond, unsigned int _uiToSecond, 
                                                  int _iFlag, int _iPosition,int _iSpeed)
{
	if (NULL == FNetClient_NetFileDownloadByTimeSpan) {
		return -1;
	}
	return FNetClient_NetFileDownloadByTimeSpan( _ulConID,  _iLogonID,  _pcLocalFile,  _iChannelNO,  _uiFromSecond,  _uiToSecond,  _iFlag,  _iPosition,  _iSpeed);
}

int NetClient_NetFileDownloadByTimeSpanEx( unsigned int* _ulConID, int _iLogonID, char* _pcLocalFile, 
					int _iChannelNO, NVS_FILE_TIME* _pTimeBegin, NVS_FILE_TIME* _pTimeEnd, 
                                                     int _iFlag, int _iPosition,int _iSpeed)
{
	if (NULL == FNetClient_NetFileDownloadByTimeSpanEx) {
		return -1;
	}
	return FNetClient_NetFileDownloadByTimeSpanEx( _ulConID,  _iLogonID,  _pcLocalFile,  _iChannelNO,  _pTimeBegin,  _pTimeEnd,  _iFlag,  _iPosition,  _iSpeed);
}

int NetClient_NetFileDownloadByTimeSpanCallBack( unsigned int* _ulConID, 
                                                 int _iLogonID, 
												 char* _pcLocalFile,
												 int _iChannelNO,
                                                 NVS_FILE_TIME* _pTimeBegin, 
                                                 NVS_FILE_TIME* _pTimeEnd, 
												 NVSDATA_NOTIFY _cbkDataArrive,
                                                 int _iFlag, 
                                                 int _iPosition,
                                                 int _iSpeed,
                                                 void* _iUserData)
{
	if (NULL == FNetClient_NetFileDownloadByTimeSpanCallBack) {
		return -1;
	}
	return FNetClient_NetFileDownloadByTimeSpanCallBack( _ulConID,  _iLogonID,  _pcLocalFile,  _iChannelNO,  _pTimeBegin,  _pTimeEnd,  _cbkDataArrive,  _iFlag,  _iPosition,  _iSpeed,  _iUserData);
}

int NetClient_NetLogQuery(int _iLogonID, PNVS_LOG_QUERY _logQuery)
{
	if (NULL == FNetClient_NetLogQuery) {
		return -1;
	}
	return FNetClient_NetLogQuery( _iLogonID,  _logQuery);
}

int NetClient_NetLogGetLogfile(int _iLogonID, int _iLogIndex, PNVS_LOG_DATA _pLogInfo)
{
	if (NULL == FNetClient_NetLogGetLogfile) {
		return -1;
	}
	return FNetClient_NetLogGetLogfile( _iLogonID,  _iLogIndex,  _pLogInfo);
}

int NetClient_NetLogGetLogCount(int _iLogonID, int* _iTotalCount, int* _iCurrentCount)
{
	if (NULL == FNetClient_NetLogGetLogCount) {
		return -1;
	}
	return FNetClient_NetLogGetLogCount( _iLogonID,  _iTotalCount,  _iCurrentCount);
}

int NetClient_GetProtocolList(int _iLogonID, st_NVSProtocol* _pstNVSProtocol)
{
	if (NULL == FNetClient_GetProtocolList) {
		return -1;
	}
	return FNetClient_GetProtocolList( _iLogonID,  _pstNVSProtocol);
}

int NetClient_SetCHNPTZCRUISE(int _iLogonID,int _iChannelNum,int _iCruiseNo,int _iEnable,int _iCruiseNum,st_PTZCruise* _stPTZCruise)
{
	if (NULL == FNetClient_SetCHNPTZCRUISE) {
		return -1;
	}
	return FNetClient_SetCHNPTZCRUISE( _iLogonID,  _iChannelNum,  _iCruiseNo,  _iEnable,  _iCruiseNum,  _stPTZCruise);
}

int NetClient_GetCHNPTZCRUISE(int _iLogonID,int _iChannelNum,int _iCruiseNo,int* _iEnable,int* _iCruiseNum,st_PTZCruise* _stPTZCruise)
{
	if (NULL == FNetClient_GetCHNPTZCRUISE) {
		return -1;
	}
	return FNetClient_GetCHNPTZCRUISE( _iLogonID,  _iChannelNum,  _iCruiseNo,  _iEnable,  _iCruiseNum,  _stPTZCruise);
}

int NetClient_SetVIDEOCOVER_LINKRECORD(int _iLogonID,int _iChannelNum,int _iEnableByBits)
{
	if (NULL == FNetClient_SetVIDEOCOVER_LINKRECORD) {
		return -1;
	}
	return FNetClient_SetVIDEOCOVER_LINKRECORD( _iLogonID,  _iChannelNum,  _iEnableByBits);
}

int NetClient_GetVIDEOCOVER_LINKRECORD(int _iLogonID,int _iChannelNum,int* _iEnableByBits)
{
	if (NULL == FNetClient_GetVIDEOCOVER_LINKRECORD) {
		return -1;
	}
	return FNetClient_GetVIDEOCOVER_LINKRECORD( _iLogonID,  _iChannelNum,  _iEnableByBits);
}

int NetClient_SetVIDEOCOVER_LINKPTZ(int _iLogonID,int _iChannelNum,int _iLinkChannel,int _iLinkType,int _iNo)
{
	if (NULL == FNetClient_SetVIDEOCOVER_LINKPTZ) {
		return -1;
	}
	return FNetClient_SetVIDEOCOVER_LINKPTZ( _iLogonID,  _iChannelNum,  _iLinkChannel,  _iLinkType,  _iNo);
}

int NetClient_GetVIDEOCOVER_LINKPTZ(int _iLogonID,int _iChannelNum,int _iLinkChannel,int* _iLinkType,int* _iNo)
{
	if (NULL == FNetClient_GetVIDEOCOVER_LINKPTZ) {
		return -1;
	}
	return FNetClient_GetVIDEOCOVER_LINKPTZ( _iLogonID,  _iChannelNum,  _iLinkChannel,  _iLinkType,  _iNo);
}

int NetClient_GetAlarmVCoverState(int _iLogonID, int _iChannel,int* _iState)
{
	if (NULL == FNetClient_GetAlarmVCoverState) {
		return -1;
	}
	return FNetClient_GetAlarmVCoverState( _iLogonID,  _iChannel,  _iState);
}

int NetClient_StopCaptureDate(unsigned long _ulID)
{
	if (NULL == FNetClient_StopCaptureDate) {
		return -1;
	}
	return FNetClient_StopCaptureDate( _ulID);
}

int NetClient_SetColorToGray(int _iLogonID,int _iChannelNum, int _iEnable)
{
	if (NULL == FNetClient_SetColorToGray) {
		return -1;
	}
	return FNetClient_SetColorToGray( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_GetColorToGray(int _iLogonID,int _iChannelNum, int* _iEnable)
{
	if (NULL == FNetClient_GetColorToGray) {
		return -1;
	}
	return FNetClient_GetColorToGray( _iLogonID,  _iChannelNum,  _iEnable);
}

int NetClient_SetCustomChannelName(int _iLogonID,int _iChannelNum,int _iChannelType, char* _cChannelName)
{
	if (NULL == FNetClient_SetCustomChannelName) {
		return -1;
	}
	return FNetClient_SetCustomChannelName( _iLogonID,  _iChannelNum,  _iChannelType,  _cChannelName);
}

int NetClient_GetCustomChannelName(int _iLogonID,int _iChannelNum,int _iChannelType, char* _cChannelName)
{
	if (NULL == FNetClient_GetCustomChannelName) {
		return -1;
	}
	return FNetClient_GetCustomChannelName( _iLogonID,  _iChannelNum,  _iChannelType,  _cChannelName);
}

int NetClient_SetCustomRecType(int _iLogonID,int _iRecType, char* _cRecTypeName)
{
	if (NULL == FNetClient_SetCustomRecType) {
		return -1;
	}
	return FNetClient_SetCustomRecType( _iLogonID,  _iRecType,  _cRecTypeName);
}

int NetClient_GetCustomRecType(int _iLogonID,int _iRecType, char* _cRecTypeName)
{
	if (NULL == FNetClient_GetCustomRecType) {
		return -1;
	}
	return FNetClient_GetCustomRecType( _iLogonID,  _iRecType,  _cRecTypeName);
}

int NetClient_ChangeSvrIPEx(int _iLogonID, char* _cNewSvrIP, char* _cMask, char* _cGateway, char* _cDNS, char* _cBackDNS)
{
	if (NULL == FNetClient_ChangeSvrIPEx) {
		return -1;
	}
	return FNetClient_ChangeSvrIPEx( _iLogonID,  _cNewSvrIP,  _cMask,  _cGateway,  _cDNS,  _cBackDNS);
}

int NetClient_GetIpPropertyEx(int _iLogonID,char* _cMAC, char* _cMask, char* _cGateway, char* _cDNS, char* _cBackDNS)
{
	if (NULL == FNetClient_GetIpPropertyEx) {
		return -1;
	}
	return FNetClient_GetIpPropertyEx( _iLogonID,  _cMAC,  _cMask,  _cGateway,  _cDNS,  _cBackDNS);
}

int NetClient_SetFTPUpdate(int _iLogonID,char* _cFtpAddr,char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword)
{
	if (NULL == FNetClient_SetFTPUpdate) {
		return -1;
	}
	return FNetClient_SetFTPUpdate( _iLogonID,  _cFtpAddr,  _cFilePath,  _cFtpUserName,  _cFtpPassword);
}

int NetClient_GetFTPUpdate(int _iLogonID,char* _cFtpAddr,char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword)
{
	if (NULL == FNetClient_GetFTPUpdate) {
		return -1;
	}
	return FNetClient_GetFTPUpdate( _iLogonID,  _cFtpAddr,  _cFilePath,  _cFtpUserName,  _cFtpPassword);
}

int NetClient_SetCHNPTZFormat(int _iLogonID,int _iChannelNum,char* _cComFormat)
{
	if (NULL == FNetClient_SetCHNPTZFormat) {
		return -1;
	}
	return FNetClient_SetCHNPTZFormat( _iLogonID,  _iChannelNum,  _cComFormat);
}

int NetClient_GetCHNPTZFormat(int _iLogonID,int _iChannelNum,char* _cComFormat)
{
	if (NULL == FNetClient_GetCHNPTZFormat) {
		return -1;
	}
	return FNetClient_GetCHNPTZFormat( _iLogonID,  _iChannelNum,  _cComFormat);
}

int NetClient_GetServerVersionEx(int _iLogonID, char* _cVer,char* _cUIVersion,char* _cSlaveVersion)
{
	if (NULL == FNetClient_GetServerVersionEx) {
		return -1;
	}
	return FNetClient_GetServerVersionEx( _iLogonID,  _cVer,  _cUIVersion,  _cSlaveVersion);
}

int NetClient_GetOSDTypeColor(int _iLogonID, int _iChannelNum, int _iOSDType, int* _iColor)
{
	if (NULL == FNetClient_GetOSDTypeColor) {
		return -1;
	}
	return FNetClient_GetOSDTypeColor( _iLogonID,  _iChannelNum,  _iOSDType,  _iColor);
}

int NetClient_SetOSDTypeColor(int _iLogonID, int _iChannelNum, int _iOSDType, int _iColor)
{
	if (NULL == FNetClient_SetOSDTypeColor) {
		return -1;
	}
	return FNetClient_SetOSDTypeColor( _iLogonID,  _iChannelNum,  _iOSDType,  _iColor);
}

int NetClient_GetExceptionMsg(int _iLogonID, int _iExceptionTyep, int* _iState)
{
	if (NULL == FNetClient_GetExceptionMsg) {
		return -1;
	}
	return FNetClient_GetExceptionMsg( _iLogonID,  _iExceptionTyep,  _iState);
}

int NetClient_SetNTPInfo(int _iLogonID, char* _NTPServer, unsigned short _iPort, int _iInterval)
{
	if (NULL == FNetClient_SetNTPInfo) {
		return -1;
	}
	return FNetClient_SetNTPInfo( _iLogonID,  _NTPServer,  _iPort,  _iInterval);
}

int NetClient_GetNTPInfo(int _iLogonID, char* _NTPServer, unsigned short* _iPort, int* _iInterval)
{
	if (NULL == FNetClient_GetNTPInfo) {
		return -1;
	}
	return FNetClient_GetNTPInfo( _iLogonID,  _NTPServer,  _iPort,  _iInterval);
}

int NetClient_SetVideoEncrypt(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetVideoEncrypt) {
		return -1;
	}
	return FNetClient_SetVideoEncrypt( _iLogonID,  _iChannel,  _lpCmdBuf,  _iBufSize);
}

int NetClient_GetVideoEncrypt(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetVideoEncrypt) {
		return -1;
	}
	return FNetClient_GetVideoEncrypt( _iLogonID,  _iChannel,  _lpCmdBuf,  _iBufSize);
}

int NetClient_SetVideoDecrypt(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetVideoDecrypt) {
		return -1;
	}
	return FNetClient_SetVideoDecrypt( _iLogonID,  _iChannel,  _lpCmdBuf,  _iBufSize);
}

int NetClient_GetVideoDecrypt(int _iLogonID, int _iChannel, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetVideoDecrypt) {
		return -1;
	}
	return FNetClient_GetVideoDecrypt( _iLogonID,  _iChannel,  _lpCmdBuf,  _iBufSize);
}

int NetClient_SetPreRecEnable(int _iLogonID, int _iChannel, int _iEnable)
{
	if (NULL == FNetClient_SetPreRecEnable) {
		return -1;
	}
	return FNetClient_SetPreRecEnable( _iLogonID,  _iChannel,  _iEnable);
}

int NetClient_GetPreRecEnable(int _iLogonID, int _iChannel, int* _piEnable)
{
	if (NULL == FNetClient_GetPreRecEnable) {
		return -1;
	}
	return FNetClient_GetPreRecEnable( _iLogonID,  _iChannel,  _piEnable);
}

int NetClient_SetVideoCombine(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetVideoCombine) {
		return -1;
	}
	return FNetClient_SetVideoCombine( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetVideoCombine(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetVideoCombine) {
		return -1;
	}
	return FNetClient_GetVideoCombine( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_VCASetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen)
{
	if (NULL == FNetClient_VCASetConfig) {
		return -1;
	}
	return FNetClient_VCASetConfig( _iLogonID,  _iVCACmdID,  _iChannel,  _lpCmdBuf,  _iCmdBufLen);
}

int NetClient_VCAGetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen)
{
	if (NULL == FNetClient_VCAGetConfig) {
		return -1;
	}
	return FNetClient_VCAGetConfig( _iLogonID,  _iVCACmdID,  _iChannel,  _lpCmdBuf,  _iCmdBufLen);
}

int NetClient_VCARestart(int _iLogonID)
{
	if (NULL == FNetClient_VCARestart) {
		return -1;
	}
	return FNetClient_VCARestart( _iLogonID);
}

int NetClient_VCARestartEx(int _iLogonID, int _iChannelNO)
{
	if (NULL == FNetClient_VCARestartEx) {
		return -1;
	}
	return FNetClient_VCARestartEx( _iLogonID,  _iChannelNO);
}

int NetClient_VCAGetAlarmInfo(int _iLogonID, int _iAlarmIndex, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_VCAGetAlarmInfo) {
		return -1;
	}
	return FNetClient_VCAGetAlarmInfo( _iLogonID,  _iAlarmIndex,  _lpBuf,  _iBufSize);
}

int NetClient_SetEmailAlarmEx(int _iLogonID, PSMTP_INFO _pSmtp, int _iSize)
{
	if (NULL == FNetClient_SetEmailAlarmEx) {
		return -1;
	}
	return FNetClient_SetEmailAlarmEx( _iLogonID,  _pSmtp,  _iSize);
}

int NetClient_GetEmailAlarmEx(int _iLogonID, PSMTP_INFO _pSmtp, int _iSize)
{
	if (NULL == FNetClient_GetEmailAlarmEx) {
		return -1;
	}
	return FNetClient_GetEmailAlarmEx( _iLogonID,  _pSmtp,  _iSize);
}

int NetClient_SetFTPUploadConfig(int _iLogonID, int _iCmd, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetFTPUploadConfig) {
		return -1;
	}
	return FNetClient_SetFTPUploadConfig( _iLogonID,  _iCmd,  _lpCmdBuf,  _iBufSize);
}

int NetClient_GetFTPUploadConfig(int _iLogonID, int _iCmd, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetFTPUploadConfig) {
		return -1;
	}
	return FNetClient_GetFTPUploadConfig( _iLogonID,  _iCmd,  _lpCmdBuf,  _iBufSize);
}

int NetClient_Set3GConfig(int _iLogonID, int _iCmd, int _iChannel, void* _lpValueBuf, int _iBufSize)
{
	if (NULL == FNetClient_Set3GConfig) {
		return -1;
	}
	return FNetClient_Set3GConfig( _iLogonID,  _iCmd,  _iChannel,  _lpValueBuf,  _iBufSize);
}

int NetClient_Get3GConfig(int _iLogonID, int _iCmd, int _iChannel, void* _lpValueBuf, int _iBufSize)
{
	if (NULL == FNetClient_Get3GConfig) {
		return -1;
	}
	return FNetClient_Get3GConfig( _iLogonID,  _iCmd,  _iChannel,  _lpValueBuf,  _iBufSize);
}

int NetClient_SetDigitalChannelConfig(int _iLogonID, int _iChannel, int _iCmd, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetDigitalChannelConfig) {
		return -1;
	}
	return FNetClient_SetDigitalChannelConfig( _iLogonID,  _iChannel,  _iCmd,  _lpCmdBuf,  _iBufSize);
}

int NetClient_GetDigitalChannelConfig(int _iLogonID, int _iChannel, int _iCmd, void* _lpCmdBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetDigitalChannelConfig) {
		return -1;
	}
	return FNetClient_GetDigitalChannelConfig( _iLogonID,  _iChannel,  _iCmd,  _lpCmdBuf,  _iBufSize);
}

int NetClient_DigitalChannelSend(int _iLogonID, int _iChannel, unsigned char* _ucBuf, int _iLength)
{
	if (NULL == FNetClient_DigitalChannelSend) {
		return -1;
	}
	return FNetClient_DigitalChannelSend( _iLogonID,  _iChannel,  _ucBuf,  _iLength);
}

int NetClient_SendComData(int _iLogonID, int _iCommand, void* _lpInBuffer, int _iInBufferSize)
{
	if (NULL == FNetClient_SendComData) {
		return -1;
	}
	return FNetClient_SendComData( _iLogonID,  _iCommand,  _lpInBuffer,  _iInBufferSize);
}

int NetClient_SetVideoNPModeEx(int _iLogonID, int _iChannel, VIDEO_NORM _iNPMode)
{
	if (NULL == FNetClient_SetVideoNPModeEx) {
		return -1;
	}
	return FNetClient_SetVideoNPModeEx( _iLogonID,  _iChannel,  _iNPMode);
}

int NetClient_GetVideoNPModeEx(int _iLogonID, int _iChannel, VIDEO_NORM* _piNPMode)
{
	if (NULL == FNetClient_GetVideoNPModeEx) {
		return -1;
	}
	return FNetClient_GetVideoNPModeEx( _iLogonID,  _iChannel,  _piNPMode);
}

int NetClient_GetDigitalChannelNum(int _iLogonID, int* _piDigitChannelNum)
{
	if (NULL == FNetClient_GetDigitalChannelNum) {
		return -1;
	}
	return FNetClient_GetDigitalChannelNum( _iLogonID,  _piDigitChannelNum);
}

int NetClient_GetChannelProperty(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetChannelProperty) {
		return -1;
	}
	return FNetClient_GetChannelProperty( _iLogonID,  _iChannel,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetDeviceTimerReboot(int _iLogonID, int _iEnable, int _iInterval, int _iHour)
{
	if (NULL == FNetClient_SetDeviceTimerReboot) {
		return -1;
	}
	return FNetClient_SetDeviceTimerReboot( _iLogonID,  _iEnable,  _iInterval,  _iHour);
}

int NetClient_GetDeviceTimerReboot(int _iLogonID, int* _iEnable, int* _iInterval, int* _iHour)
{
	if (NULL == FNetClient_GetDeviceTimerReboot) {
		return -1;
	}
	return FNetClient_GetDeviceTimerReboot( _iLogonID,  _iEnable,  _iInterval,  _iHour);
}

int NetClient_SetVideoCoverSchedule(int _iLogonID, int _iChannel, int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_SetVideoCoverSchedule) {
		return -1;
	}
	return FNetClient_SetVideoCoverSchedule( _iLogonID,  _iChannel,  _iWeekday,  _strScheduleParam);
}

int NetClient_GetVideoCoverSchedule(int _iLogonID, int _iChannel, int _iWeekday,PNVS_SCHEDTIME _strScheduleParam[MAX_TIMESEGMENT])
{
	if (NULL == FNetClient_GetVideoCoverSchedule) {
		return -1;
	}
	return FNetClient_GetVideoCoverSchedule( _iLogonID,  _iChannel,  _iWeekday,  _strScheduleParam);
}

int NetClient_SetCPUMEMAlarmThreshold(int _iLogonID, int _iCPUThreshold, int _iMEMThreshold)
{
	if (NULL == FNetClient_SetCPUMEMAlarmThreshold) {
		return -1;
	}
	return FNetClient_SetCPUMEMAlarmThreshold( _iLogonID,  _iCPUThreshold,  _iMEMThreshold);
}

int NetClient_GetCPUMEMAlarmThreshold(int _iLogonID, int* _iCPUThreshold, int* _iMEMThreshold)
{
	if (NULL == FNetClient_GetCPUMEMAlarmThreshold) {
		return -1;
	}
	return FNetClient_GetCPUMEMAlarmThreshold( _iLogonID,  _iCPUThreshold,  _iMEMThreshold);
}

int NetClient_SetDZInfo(int _iLogonID, DZ_INFO_PARAM* _pDZ_INFO)
{
	if (NULL == FNetClient_SetDZInfo) {
		return -1;
	}
	return FNetClient_SetDZInfo( _iLogonID,  _pDZ_INFO);
}

int NetClient_GetDZInfo(int _iLogonID, DZ_INFO_PARAM* _pDZ_INFO)
{
	if (NULL == FNetClient_GetDZInfo) {
		return -1;
	}
	return FNetClient_GetDZInfo( _iLogonID,  _pDZ_INFO);
}

int NetClient_SetPTZAutoBack(int _iLogonID, int _iChannel,int _iEnable,int _iPresetIndex,int _iIdleTime)
{
	if (NULL == FNetClient_SetPTZAutoBack) {
		return -1;
	}
	return FNetClient_SetPTZAutoBack( _iLogonID,  _iChannel,  _iEnable,  _iPresetIndex,  _iIdleTime);
}

int NetClient_GetPTZAutoBack(int _iLogonID, int _iChannel,int* _iEnable,int* _iPresetIndex,int* _iIdleTime)
{
	if (NULL == FNetClient_GetPTZAutoBack) {
		return -1;
	}
	return FNetClient_GetPTZAutoBack( _iLogonID,  _iChannel,  _iEnable,  _iPresetIndex,  _iIdleTime);
}

int NetClient_Set3GVPND(int _iLogonID, char* _cDialNumber,char* _cAccount,char* _cPassword)
{
	if (NULL == FNetClient_Set3GVPND) {
		return -1;
	}
	return FNetClient_Set3GVPND( _iLogonID,  _cDialNumber,  _cAccount,  _cPassword);
}

int NetClient_Get3GVPND(int _iLogonID, char* _cDialNumber,char* _cAccount,char* _cPassword)
{
	if (NULL == FNetClient_Get3GVPND) {
		return -1;
	}
	return FNetClient_Get3GVPND( _iLogonID,  _cDialNumber,  _cAccount,  _cPassword);
}

int NetClient_SetHDCamerEx(int _iLogonID, int _iChannel, int _iFuncID, void* _lpBuf, int _iSize)
{
	if (NULL == FNetClient_SetHDCamerEx) {
		return -1;
	}
	return FNetClient_SetHDCamerEx( _iLogonID,  _iChannel,  _iFuncID,  _lpBuf,  _iSize);
}

int NetClient_GetHDCamerEx(int _iLogonID, int _iChannel, int _iFuncID, void* _lpBuf, int _iSize)
{
	if (NULL == FNetClient_GetHDCamerEx) {
		return -1;
	}
	return FNetClient_GetHDCamerEx( _iLogonID,  _iChannel,  _iFuncID,  _lpBuf,  _iSize);
}

int NetClient_SetFTPUsage(int _iLogonID, char* _cFtpAddr, int _iPort, char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword,int _iUsage)
{
	if (NULL == FNetClient_SetFTPUsage) {
		return -1;
	}
	return FNetClient_SetFTPUsage( _iLogonID,  _cFtpAddr,  _iPort,  _cFilePath,  _cFtpUserName,  _cFtpPassword,  _iUsage);
}

int NetClient_GetFTPUsage(int _iLogonID, char* _cFtpAddr, int* _piPort, char* _cFilePath,char* _cFtpUserName, char* _cFtpPassword,int* _piUsage)
{
	if (NULL == FNetClient_GetFTPUsage) {
		return -1;
	}
	return FNetClient_GetFTPUsage( _iLogonID,  _cFtpAddr,  _piPort,  _cFilePath,  _cFtpUserName,  _cFtpPassword,  _piUsage);
}

int NetClient_SetChannelSipConfig(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetChannelSipConfig) {
		return -1;
	}
	return FNetClient_SetChannelSipConfig( _iLogonID,  _iChannel,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetChannelSipConfig(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetChannelSipConfig) {
		return -1;
	}
	return FNetClient_GetChannelSipConfig( _iLogonID,  _iChannel,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetMaxVideoSize(int _iLogonID, int* _iVideoSize)
{
	if (NULL == FNetClient_GetMaxVideoSize) {
		return -1;
	}
	return FNetClient_GetMaxVideoSize( _iLogonID,  _iVideoSize);
}

int NetClient_SetBitRatePercent(int _iLogonID, int _iChannelNO, int _iPercent)
{
	if (NULL == FNetClient_SetBitRatePercent) {
		return -1;
	}
	return FNetClient_SetBitRatePercent( _iLogonID,  _iChannelNO,  _iPercent);
}

int NetClient_GetBitRatePercent(int _iLogonID, int _iChannelNO, int* _piPercent)
{
	if (NULL == FNetClient_GetBitRatePercent) {
		return -1;
	}
	return FNetClient_GetBitRatePercent( _iLogonID,  _iChannelNO,  _piPercent);
}

int NetClient_GetVideoParam(unsigned int _uiConnID, int* _piWidth, int* _piHeight, int* _piFrameRate)
{
	if (NULL == FNetClient_GetVideoParam) {
		return -1;
	}
	return FNetClient_GetVideoParam( _uiConnID,  _piWidth,  _piHeight,  _piFrameRate);
}

int NetClient_SetOSDAlpha(int _iLogonID, int _iChannel,int _iAlpha)
{
	if (NULL == FNetClient_SetOSDAlpha) {
		return -1;
	}
	return FNetClient_SetOSDAlpha( _iLogonID,  _iChannel,  _iAlpha);
}

int NetClient_GetOSDAlpha(int _iLogonID, int _iChannel,int* _iAlpha)
{
	if (NULL == FNetClient_GetOSDAlpha) {
		return -1;
	}
	return FNetClient_GetOSDAlpha( _iLogonID,  _iChannel,  _iAlpha);
}

int NetClient_DeviceSetup(int _iLogonID, int _iFlag,const char* _pcSection,const char* _pcKeyword,const char* _pcValue)
{
	if (NULL == FNetClient_DeviceSetup) {
		return -1;
	}
	return FNetClient_DeviceSetup( _iLogonID,  _iFlag,  _pcSection,  _pcKeyword,  _pcValue);
}

int NetClient_SetPlayerShowFrameMode(int _iLogonID,int _iChannelNum, unsigned int _uiShowFrameMode, int _iStreamNO)
{
	if (NULL == FNetClient_SetPlayerShowFrameMode) {
		return -1;
	}
	return FNetClient_SetPlayerShowFrameMode( _iLogonID,  _iChannelNum,  _uiShowFrameMode,  _iStreamNO);
}

int NetClient_GetPlayerShowFrameMode(int _iLogonID,int _iChannelNum,int _iStreamNO)
{
	if (NULL == FNetClient_GetPlayerShowFrameMode) {
		return -1;
	}
	return FNetClient_GetPlayerShowFrameMode( _iLogonID,  _iChannelNum,  _iStreamNO);
}

int NetClient_DrawRectOnLocalVideo(unsigned int _uiConID, RECT* _rcRect, int _iCount)
{
	if (NULL == FNetClient_DrawRectOnLocalVideo) {
		return -1;
	}
	return FNetClient_DrawRectOnLocalVideo( _uiConID,  _rcRect,  _iCount);
}

int NetClient_DrawPolyOnLocalVideo(unsigned int _uiConnID, POINT* _pPointArray, int _iPointCount, int _iFlag)
{
	if (NULL == FNetClient_DrawPolyOnLocalVideo) {
		return -1;
	}
	return FNetClient_DrawPolyOnLocalVideo( _uiConnID,  _pPointArray,  _iPointCount,  _iFlag);
}

int NetClient_SendStringToServerEx(int _iLogonID,char* _cMsg,int _iLen, int _iFlag)
{
	if (NULL == FNetClient_SendStringToServerEx) {
		return -1;
	}
	return FNetClient_SendStringToServerEx( _iLogonID,  _cMsg,  _iLen,  _iFlag);
}

int NetClient_SetNetFileDownloadFileCallBack(unsigned int _ulConID, RECV_DOWNLOADDATA_NOTIFY _cbkDataNotify, void* _lpUserData)
{
	if (NULL == FNetClient_SetNetFileDownloadFileCallBack) {
		return -1;
	}
	return FNetClient_SetNetFileDownloadFileCallBack( _ulConID,  _cbkDataNotify,  _lpUserData);
}

int NetClient_SetDataPackCallBack(unsigned int _ulConID, int _iCBType, void* _pvCallBack, void* _pvUserData)
{
	if (NULL == FNetClient_SetDataPackCallBack) {
		return -1;
	}
	return FNetClient_SetDataPackCallBack( _ulConID,  _iCBType,  _pvCallBack,  _pvUserData);
}

int NetClient_AddConnectionToNetWork(int _iSocket, void* _Connection, void* _NotifyFun)
{
	if (NULL == FNetClient_AddConnectionToNetWork) {
		return -1;
	}
	return FNetClient_AddConnectionToNetWork( _iSocket,  _Connection,  _NotifyFun);
}

void* NetClient_MallocConnection()
{
	if (NULL == FNetClient_MallocConnection) {
		return NULL;
	}
	return FNetClient_MallocConnection();
}

int NetClient_FreeConnection(void* _pConnect)
{
	if (NULL == FNetClient_FreeConnection) {
		return -1;
	}
	return FNetClient_FreeConnection( _pConnect);
}

int NetClient_NetFileSetChannelParam(int _iLogonID,int _iChannelNo,int _iCmd, void* _lpBuf)
{
	if (NULL == FNetClient_NetFileSetChannelParam) {
		return -1;
	}
	return FNetClient_NetFileSetChannelParam( _iLogonID,  _iChannelNo,  _iCmd,  _lpBuf);
}

int NetClient_NetFileGetChannelParam(int _iLogonID,int _iChannelNo,int _iCmd, void* _lpBuf)
{
	if (NULL == FNetClient_NetFileGetChannelParam) {
		return -1;
	}
	return FNetClient_NetFileGetChannelParam( _iLogonID,  _iChannelNo,  _iCmd,  _lpBuf);
}

int NetClient_ShutDownDev(int _iLogonID, int _iFlag)
{
	if (NULL == FNetClient_ShutDownDev) {
		return -1;
	}
	return FNetClient_ShutDownDev( _iLogonID,  _iFlag);
}

int NetClient_BackupImage(int _iLogonID , int _iType)
{
	if (NULL == FNetClient_BackupImage) {
		return -1;
	}
	return FNetClient_BackupImage( _iLogonID ,  _iType);
}

int NetClient_SetLanParam(int _iLogonID, int _iCmd, void* _lpData)
{
	if (NULL == FNetClient_SetLanParam) {
		return -1;
	}
	return FNetClient_SetLanParam( _iLogonID,  _iCmd,  _lpData);
}

int NetClient_GetLanParam(int _iLogonID, int _iCmd, void* _lpData)
{
	if (NULL == FNetClient_GetLanParam) {
		return -1;
	}
	return FNetClient_GetLanParam( _iLogonID,  _iCmd,  _lpData);
}

int NetClient_GetVideoSzList( int _iLogonID, int _iChannel, int _iStreamNo, int* _piList, int*  _piLstCount )
{
	if (NULL == FNetClient_GetVideoSzList) {
		return -1;
	}
	return FNetClient_GetVideoSzList( _iLogonID,  _iChannel,  _iStreamNo,  _piList,  _piLstCount );
}

int NetClient_SetAlarmConfig( int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, void* _pvCmdBuf)
{
	if (NULL == FNetClient_SetAlarmConfig) {
		return -1;
	}
	return FNetClient_SetAlarmConfig( _iLogonID,  _iChannel,  _iAlarmType,  _iCmd,  _pvCmdBuf);
}

int NetClient_GetAlarmConfig(int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, void* _pvCmdBuf)
{
	if (NULL == FNetClient_GetAlarmConfig) {
		return -1;
	}
	return FNetClient_GetAlarmConfig( _iLogonID,  _iChannel,  _iAlarmType,  _iCmd,  _pvCmdBuf);
}

int NetClient_SetITSBlock(int _iLogonID, int _iBlockID, int _iX, int _iY)
{
	if (NULL == FNetClient_SetITSBlock) {
		return -1;
	}
	return FNetClient_SetITSBlock( _iLogonID,  _iBlockID,  _iX,  _iY);
}

int NetClient_GetITSBlock(int _iLogonID, int _iBlockID, int* _iX, int* _iY)
{
	if (NULL == FNetClient_GetITSBlock) {
		return -1;
	}
	return FNetClient_GetITSBlock( _iLogonID,  _iBlockID,  _iX,  _iY);
}

int NetClient_SetHDTimeRangeParam(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetHDTimeRangeParam) {
		return -1;
	}
	return FNetClient_SetHDTimeRangeParam( _iLogonID,  _iChannel,  _iIndex,  _lpBuf,  _iBufSize);
}

int NetClient_GetHDTimeRangeParam(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetHDTimeRangeParam) {
		return -1;
	}
	return FNetClient_GetHDTimeRangeParam( _iLogonID,  _iChannel,  _iIndex,  _lpBuf,  _iBufSize);
}

int NetClient_SetHDTemplateName(int _iLogonID, int _iTemplateID, char* _cTemplateName)
{
	if (NULL == FNetClient_SetHDTemplateName) {
		return -1;
	}
	return FNetClient_SetHDTemplateName( _iLogonID,  _iTemplateID,  _cTemplateName);
}

int NetClient_GetHDTemplateName(int _iLogonID, int _iTemplateID,char* _cTemplateName)
{
	if (NULL == FNetClient_GetHDTemplateName) {
		return -1;
	}
	return FNetClient_GetHDTemplateName( _iLogonID,  _iTemplateID,  _cTemplateName);
}

int NetClient_SetHDTemplateMap(int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetHDTemplateMap) {
		return -1;
	}
	return FNetClient_SetHDTemplateMap( _iLogonID,  _iChannel,  _lpBuf,  _iBufSize);
}

int NetClient_GetHDTemplateMap(int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetHDTemplateMap) {
		return -1;
	}
	return FNetClient_GetHDTemplateMap( _iLogonID,  _iChannel,  _lpBuf,  _iBufSize);
}

int NetClient_SetITSTimeRangeEnable(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSTimeRangeEnable) {
		return -1;
	}
	return FNetClient_SetITSTimeRangeEnable( _iLogonID,  _iChannel,  _iIndex,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSTimeRangeEnable(int _iLogonID, int _iChannel, int _iIndex, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSTimeRangeEnable) {
		return -1;
	}
	return FNetClient_GetITSTimeRangeEnable( _iLogonID,  _iChannel,  _iIndex,  _lpBuf,  _iBufSize);
}

int NetClient_SetITSTimeRange(int _iLogonID, int _iIndex, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSTimeRange) {
		return -1;
	}
	return FNetClient_SetITSTimeRange( _iLogonID,  _iIndex,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSTimeRange(int _iLogonID, int _iIndex, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSTimeRange) {
		return -1;
	}
	return FNetClient_GetITSTimeRange( _iLogonID,  _iIndex,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetITSDetectMode(int _iLogonID, int _iMode)
{
	if (NULL == FNetClient_SetITSDetectMode) {
		return -1;
	}
	return FNetClient_SetITSDetectMode( _iLogonID,  _iMode);
}

int NetClient_GetITSDetectMode(int _iLogonID, int* _iMode)
{
	if (NULL == FNetClient_GetITSDetectMode) {
		return -1;
	}
	return FNetClient_GetITSDetectMode( _iLogonID,  _iMode);
}

int NetClient_SetITSLoopMode(int _iLogonID, int _iLoopMode)
{
	if (NULL == FNetClient_SetITSLoopMode) {
		return -1;
	}
	return FNetClient_SetITSLoopMode( _iLogonID,  _iLoopMode);
}

int NetClient_GetITSLoopMode(int _iLogonID, int* _iLoopMode)
{
	if (NULL == FNetClient_GetITSLoopMode) {
		return -1;
	}
	return FNetClient_GetITSLoopMode( _iLogonID,  _iLoopMode);
}

int NetClient_SetITSDeviceType(int _iLogonID, int _iDeviceType)
{
	if (NULL == FNetClient_SetITSDeviceType) {
		return -1;
	}
	return FNetClient_SetITSDeviceType( _iLogonID,  _iDeviceType);
}

int NetClient_GetITSDeviceType(int _iLogonID, int* _iDeviceType)
{
	if (NULL == FNetClient_GetITSDeviceType) {
		return -1;
	}
	return FNetClient_GetITSDeviceType( _iLogonID,  _iDeviceType);
}

int NetClient_SetITSRoadwayParam(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSRoadwayParam) {
		return -1;
	}
	return FNetClient_SetITSRoadwayParam( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSRoadwayParam(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSRoadwayParam) {
		return -1;
	}
	return FNetClient_GetITSRoadwayParam( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetITSLicensePlateOptimize(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSLicensePlateOptimize) {
		return -1;
	}
	return FNetClient_SetITSLicensePlateOptimize( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSLicensePlateOptimize(int _iLogonID, int _iCmd, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSLicensePlateOptimize) {
		return -1;
	}
	return FNetClient_GetITSLicensePlateOptimize( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetITSExtraInfo(int _iLogonID, int _iCmd, int _iChannel, void* _pvCmdBuf, int _iCmdLen)
{
	if (NULL == FNetClient_SetITSExtraInfo) {
		return -1;
	}
	return FNetClient_SetITSExtraInfo( _iLogonID,  _iCmd,  _iChannel,  _pvCmdBuf,  _iCmdLen);
}

int NetClient_GetITSExtraInfo(int _iLogonID, int _iCmd, int _iChannel, void* _pvCmdBuf, int _iCmdLen)
{
	if (NULL == FNetClient_GetITSExtraInfo) {
		return -1;
	}
	return FNetClient_GetITSExtraInfo( _iLogonID,  _iCmd,  _iChannel,  _pvCmdBuf,  _iCmdLen);
}

int NetClient_CheckDeviceState(int _iLogonID, int _iChannelNo, int _iType)
{
	if (NULL == FNetClient_CheckDeviceState) {
		return -1;
	}
	return FNetClient_CheckDeviceState( _iLogonID,  _iChannelNo,  _iType);
}

int NetClient_GetDeviceState(int _iLogonID, int _iChannelNo, int _iType, int* _iValue)
{
	if (NULL == FNetClient_GetDeviceState) {
		return -1;
	}
	return FNetClient_GetDeviceState( _iLogonID,  _iChannelNo,  _iType,  _iValue);
}

int NetClient_GetCameraCheckInfo(int _iLogonID, int _iChannelNo, int _iType, int* _iValue)
{
	if (NULL == FNetClient_GetCameraCheckInfo) {
		return -1;
	}
	return FNetClient_GetCameraCheckInfo( _iLogonID,  _iChannelNo,  _iType,  _iValue);
}

int NetClient_CheckCamera(int _iLogonID, int _iChannelNo, int _iType, int _iEnable)
{
	if (NULL == FNetClient_CheckCamera) {
		return -1;
	}
	return FNetClient_CheckCamera( _iLogonID,  _iChannelNo,  _iType,  _iEnable);
}

int NetClient_GetCharSet(int _iLogonID, char* _pcCharSet)
{
	if (NULL == FNetClient_GetCharSet) {
		return -1;
	}
	return FNetClient_GetCharSet( _iLogonID,  _pcCharSet);
}

int NetClient_SetTimeZone(int _iLogonID, int _iTimeZone)
{
	if (NULL == FNetClient_SetTimeZone) {
		return -1;
	}
	return FNetClient_SetTimeZone( _iLogonID,  _iTimeZone);
}

int NetClient_GetTimeZone(int _iLogonID, int* _iTimeZone)
{
	if (NULL == FNetClient_GetTimeZone) {
		return -1;
	}
	return FNetClient_GetTimeZone( _iLogonID,  _iTimeZone);
}

int NetClient_SetCurLanguage(int _iLogonID, char* _pcLanguage)
{
	if (NULL == FNetClient_SetCurLanguage) {
		return -1;
	}
	return FNetClient_SetCurLanguage( _iLogonID,  _pcLanguage);
}

int NetClient_GetCurLanguage(int _iLogonID, char* _pcLanguage)
{
	if (NULL == FNetClient_GetCurLanguage) {
		return -1;
	}
	return FNetClient_GetCurLanguage( _iLogonID,  _pcLanguage);
}

int NetClient_GetLanguageList(int _iLogonID, st_NVSLanguageList* _pStrctLanguageList)
{
	if (NULL == FNetClient_GetLanguageList) {
		return -1;
	}
	return FNetClient_GetLanguageList( _iLogonID,  _pStrctLanguageList);
}

int NetClient_SetChannelEncodeProfile(int _iLogonID, int _iChannelNum, int _iStreamNo, int _iLevel)
{
	if (NULL == FNetClient_SetChannelEncodeProfile) {
		return -1;
	}
	return FNetClient_SetChannelEncodeProfile( _iLogonID,  _iChannelNum,  _iStreamNo,  _iLevel);
}

int NetClient_GetChannelEncodeProfile(int _iLogonID, int _iChannelNum, int _iStreamNo, int* _piLevel)
{
	if (NULL == FNetClient_GetChannelEncodeProfile) {
		return -1;
	}
	return FNetClient_GetChannelEncodeProfile( _iLogonID,  _iChannelNum,  _iStreamNo,  _piLevel);
}

int NetClient_SetAlarmClear(int _iLogonID, int _iChannelNo, int _iClearType)
{
	if (NULL == FNetClient_SetAlarmClear) {
		return -1;
	}
	return FNetClient_SetAlarmClear( _iLogonID,  _iChannelNo,  _iClearType);
}

int NetClient_SetExceptionHandleParam( int _iLogonID, int _iExceptionType, int _iFlag )
{
	if (NULL == FNetClient_SetExceptionHandleParam) {
		return -1;
	}
	return FNetClient_SetExceptionHandleParam( _iLogonID,  _iExceptionType,  _iFlag );
}

int NetClient_GetExceptionHandleParam( int _iLogonID, int _iExceptionType, int* _iFlag)
{
	if (NULL == FNetClient_GetExceptionHandleParam) {
		return -1;
	}
	return FNetClient_GetExceptionHandleParam( _iLogonID,  _iExceptionType,  _iFlag);
}

int NetClient_SetAlarmLink_V1(int _iLogonID, int _iChannelNo, int _iAlarmLinkType, void * _pParam, int _iParamSize )
{
	if (NULL == FNetClient_SetAlarmLink_V1) {
		return -1;
	}
	return FNetClient_SetAlarmLink_V1( _iLogonID,  _iChannelNo,  _iAlarmLinkType,  _pParam,  _iParamSize );
}

int NetClient_GetAlarmLink_V1(int _iLogonID, int _iChannelNo, int _iAlarmLinkType, void * _pParam, int _iParamSize )
{
	if (NULL == FNetClient_GetAlarmLink_V1) {
		return -1;
	}
	return FNetClient_GetAlarmLink_V1( _iLogonID,  _iChannelNo,  _iAlarmLinkType,  _pParam,  _iParamSize );
}

int NetClient_SetCameraParam(int _iLogonID, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetCameraParam) {
		return -1;
	}
	return FNetClient_SetCameraParam( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetCameraParam(int _iLogonID, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetCameraParam) {
		return -1;
	}
	return FNetClient_GetCameraParam( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_SetColorParam(int _iLogonID,int _iChannelNum, int _iColorToGradEnable, int _iDayRange, int _iNightRange)
{
	if (NULL == FNetClient_SetColorParam) {
		return -1;
	}
	return FNetClient_SetColorParam( _iLogonID,  _iChannelNum,  _iColorToGradEnable,  _iDayRange,  _iNightRange);
}

int NetClient_GetColorParam(int _iLogonID,int _iChannelNum, int* _iColorToGradEnable,  int* _iDayRange, int* _iNightRange)
{
	if (NULL == FNetClient_GetColorParam) {
		return -1;
	}
	return FNetClient_GetColorParam( _iLogonID,  _iChannelNum,  _iColorToGradEnable,  _iDayRange,  _iNightRange);
}

void* NetClient_InnerMallocBlock(int _iNeedSize)
{
	if (NULL == FNetClient_InnerMallocBlock) {
		return NULL;
	}
	return FNetClient_InnerMallocBlock( _iNeedSize);
}

int NetClient_InnerFreeBlock(void * _pBlock)
{
	if (NULL == FNetClient_InnerFreeBlock) {
		return -1;
	}
	return FNetClient_InnerFreeBlock( _pBlock);
}

int NetClient_InnerReferBlock(void * _pBlock)
{
	if (NULL == FNetClient_InnerReferBlock) {
		return -1;
	}
	return FNetClient_InnerReferBlock( _pBlock);
}

int NetClient_InnerReleaseBlock(void* _pBlock)
{
	if (NULL == FNetClient_InnerReleaseBlock) {
		return -1;
	}
	return FNetClient_InnerReleaseBlock( _pBlock);
}

int NetClient_SetJPEGQuality(int _iLogonID, int _iJpegQuality)
{
	if (NULL == FNetClient_SetJPEGQuality) {
		return -1;
	}
	return FNetClient_SetJPEGQuality( _iLogonID,  _iJpegQuality);
}

int NetClient_GetJPEGQuality(int _iLogonID, int* _iJpegQuality)
{
	if (NULL == FNetClient_GetJPEGQuality) {
		return -1;
	}
	return FNetClient_GetJPEGQuality( _iLogonID,  _iJpegQuality);
}

int NetClient_GetConnectInfo(int _iLogonID, void* _lpBuf, int _iSize)
{
	if (NULL == FNetClient_GetConnectInfo) {
		return -1;
	}
	return FNetClient_GetConnectInfo( _iLogonID,  _lpBuf,  _iSize);
}

int NetClient_SetPlatformApp(int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetPlatformApp) {
		return -1;
	}
	return FNetClient_SetPlatformApp( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetPlatformApp(int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetPlatformApp) {
		return -1;
	}
	return FNetClient_GetPlatformApp( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetManagerServersInfo(int _iLogonID, char* _cRegSvrIP, unsigned short _wdRegPort,
            char* _cHeartSvrIP, unsigned short _wdHeartPort, char* _cAlarmSvrIP, unsigned short _wdAlarmPort)
{
	if (NULL == FNetClient_SetManagerServersInfo) {
		return -1;
	}
	return FNetClient_SetManagerServersInfo( _iLogonID,  _cRegSvrIP,  _wdRegPort,  _cHeartSvrIP,  _wdHeartPort,  _cAlarmSvrIP,  _wdAlarmPort);
}

int NetClient_GetManagerServersInfo(int _iLogonID, char* _cRegSvrIP, unsigned short* _wdRegPort,
            char* _cHeartSvrIP, unsigned short* _wdHeartPort, char* _cAlarmSvrIP, unsigned short* _wdAlarmPort)
{
	if (NULL == FNetClient_GetManagerServersInfo) {
		return -1;
	}
	return FNetClient_GetManagerServersInfo( _iLogonID,  _cRegSvrIP,  _wdRegPort,  _cHeartSvrIP,  _wdHeartPort,  _cAlarmSvrIP,  _wdAlarmPort);
}

int NetClient_SetDeviceID(int _iLogonID, char* _cDeviceID, char* _cDeviceName, unsigned short _iVspPort, char* _cAccessPass, unsigned short _iVapPort)
{
	if (NULL == FNetClient_SetDeviceID) {
		return -1;
	}
	return FNetClient_SetDeviceID( _iLogonID,  _cDeviceID,  _cDeviceName,  _iVspPort,  _cAccessPass,  _iVapPort);
}

int NetClient_GetDeviceID(int _iLogonID, char* _cDeviceID, char* _cDeviceName, unsigned short* _iVspPort, char* _cAccessPass, unsigned short* _iVapPort)
{
	if (NULL == FNetClient_GetDeviceID) {
		return -1;
	}
	return FNetClient_GetDeviceID( _iLogonID,  _cDeviceID,  _cDeviceName,  _iVspPort,  _cAccessPass,  _iVapPort);
}

int NetClient_SetATMConfig(int _iLogonID,int _iCmd, void* _lpBuf)
{
	if (NULL == FNetClient_SetATMConfig) {
		return -1;
	}
	return FNetClient_SetATMConfig( _iLogonID,  _iCmd,  _lpBuf);
}

int NetClient_GetATMConfig(int _iLogonID,int _iCmd, void* _lpBuf)
{
	if (NULL == FNetClient_GetATMConfig) {
		return -1;
	}
	return FNetClient_GetATMConfig( _iLogonID,  _iCmd,  _lpBuf);
}

int NetClient_ATMQueryFile(int _iLogonID, int _iCmd, void* _lpBuf)
{
	if (NULL == FNetClient_ATMQueryFile) {
		return -1;
	}
	return FNetClient_ATMQueryFile( _iLogonID,  _iCmd,  _lpBuf);
}

int NetClient_ATMGetQueryFile(int _iLogonID, int _iFileIndex, ATM_FILE_DATA* _pFileData)
{
	if (NULL == FNetClient_ATMGetQueryFile) {
		return -1;
	}
	return FNetClient_ATMGetQueryFile( _iLogonID,  _iFileIndex,  _pFileData);
}

int NetClient_SetAudioSample(int _iLogonID, int _iChannel,int _iCmd, void* _lpBuf)
{
	if (NULL == FNetClient_SetAudioSample) {
		return -1;
	}
	return FNetClient_SetAudioSample( _iLogonID,  _iChannel,  _iCmd,  _lpBuf);
}

int NetClient_GetAudioSample(int _iLogonID, int _iChannel,int _iCmd, void* _lpBuf)
{
	if (NULL == FNetClient_GetAudioSample) {
		return -1;
	}
	return FNetClient_GetAudioSample( _iLogonID,  _iChannel,  _iCmd,  _lpBuf);
}

int NetClient_SetSystemTypeEx( int _iLogonID, int _iCmd, void* _pvCmdBuf)
{
	if (NULL == FNetClient_SetSystemTypeEx) {
		return -1;
	}
	return FNetClient_SetSystemTypeEx( _iLogonID,  _iCmd,  _pvCmdBuf);
}

int NetClient_GetSystemTypeEx( int _iLogonID, int _iCmd, void* _pvCmdBuf)
{
	if (NULL == FNetClient_GetSystemTypeEx) {
		return -1;
	}
	return FNetClient_GetSystemTypeEx( _iLogonID,  _iCmd,  _pvCmdBuf);
}

int NetClient_SetHXListenPortInfo(int _iLogonID,int _iVideoPort,int _iTalkPort,int _iMsgPort)
{
	if (NULL == FNetClient_SetHXListenPortInfo) {
		return -1;
	}
	return FNetClient_SetHXListenPortInfo( _iLogonID,  _iVideoPort,  _iTalkPort,  _iMsgPort);
}

int NetClient_GetHXListenPortInfo(int _iLogonID,int* _iVideoPort,int* _iTalkPort,int* _iMsgPort)
{
	if (NULL == FNetClient_GetHXListenPortInfo) {
		return -1;
	}
	return FNetClient_GetHXListenPortInfo( _iLogonID,  _iVideoPort,  _iTalkPort,  _iMsgPort);
}

int NetClient_SetVideoModeMethod(int _iLogonID, int _iChannel, VIDEO_METHOD  _iMethod)
{
	if (NULL == FNetClient_SetVideoModeMethod) {
		return -1;
	}
	return FNetClient_SetVideoModeMethod( _iLogonID,  _iChannel,  _iMethod);
}

int NetClient_GetVideoModeMethod(int _iLogonID, int _iChannel, VIDEO_METHOD * _piMethod)
{
	if (NULL == FNetClient_GetVideoModeMethod) {
		return -1;
	}
	return FNetClient_GetVideoModeMethod( _iLogonID,  _iChannel,  _piMethod);
}

int NetClient_GetMonitorNum()
{
	if (NULL == FNetClient_GetMonitorNum) {
		return -1;
	}
	return FNetClient_GetMonitorNum();
}

int NetClient_GetMonitorInfo(unsigned int _uiIndex,MONITOR_INFO* _MonitorInfo)
{
	if (NULL == FNetClient_GetMonitorInfo) {
		return -1;
	}
	return FNetClient_GetMonitorInfo( _uiIndex,  _MonitorInfo);
}

int NetClient_ChangeMonitor(int _iLogonID,int _iChannelNum,unsigned int _uiIndex, int _iStreamNO)
{
	if (NULL == FNetClient_ChangeMonitor) {
		return -1;
	}
	return FNetClient_ChangeMonitor( _iLogonID,  _iChannelNum,  _uiIndex,  _iStreamNO);
}

int NetClient_EZoomAdd(unsigned int _uiConnID, int _hWnd, RECT _rctDisplay, unsigned int _uiMonitorIndex)
{
	if (NULL == FNetClient_EZoomAdd) {
		return -1;
	}
	return FNetClient_EZoomAdd( _uiConnID,  _hWnd,  _rctDisplay,  _uiMonitorIndex);
}

int NetClient_EZoomSet(unsigned int _uiConnID, int _iEZoomID, RECT _rctVideo)
{
	if (NULL == FNetClient_EZoomSet) {
		return -1;
	}
	return FNetClient_EZoomSet( _uiConnID,  _iEZoomID,  _rctVideo);
}

int NetClient_EZoomReset(unsigned int _uiConnID, int _iEZoomID)
{
	if (NULL == FNetClient_EZoomReset) {
		return -1;
	}
	return FNetClient_EZoomReset( _uiConnID,  _iEZoomID);
}

int NetClient_EZoomRemove(unsigned int _uiConnID, int _iEZoomID)
{
	if (NULL == FNetClient_EZoomRemove) {
		return -1;
	}
	return FNetClient_EZoomRemove( _uiConnID,  _iEZoomID);
}

int NetClient_DCardStartPlay(unsigned int _ulConID, int _iCardChannel, int _iPos)
{
	if (NULL == FNetClient_DCardStartPlay) {
		return -1;
	}
	return FNetClient_DCardStartPlay( _ulConID,  _iCardChannel,  _iPos);
}

int NetClient_DCardStopPlay(unsigned int _ulConID, int _iFlag)
{
	if (NULL == FNetClient_DCardStopPlay) {
		return -1;
	}
	return FNetClient_DCardStopPlay( _ulConID,  _iFlag);
}

int NetClient_DCardRelease()
{
	if (NULL == FNetClient_DCardRelease) {
		return -1;
	}
	return FNetClient_DCardRelease();
}

int NetClient_DCardReInit(int _iCardChannel)
{
	if (NULL == FNetClient_DCardReInit) {
		return -1;
	}
	return FNetClient_DCardReInit( _iCardChannel);
}

int NetClient_DCardGetState(int _iCardChannel, int* _iState)
{
	if (NULL == FNetClient_DCardGetState) {
		return -1;
	}
	return FNetClient_DCardGetState( _iCardChannel,  _iState);
}

int NetClient_DCardStartPlayEx(int* _iId, DecoderParam* _dParam)
{
	if (NULL == FNetClient_DCardStartPlayEx) {
		return -1;
	}
	return FNetClient_DCardStartPlayEx( _iId,  _dParam);
}

int NetClient_DCardPutDataEx(int _iId, char* _cBuf, int _iLen)
{
	if (NULL == FNetClient_DCardPutDataEx) {
		return -1;
	}
	return FNetClient_DCardPutDataEx( _iId,  _cBuf,  _iLen);
}

int NetClient_DCardStopPlayEx(int _iId, int _iFlag)
{
	if (NULL == FNetClient_DCardStopPlayEx) {
		return -1;
	}
	return FNetClient_DCardStopPlayEx( _iId,  _iFlag);
}

int NetClient_DCardStartPlayAudio(unsigned int _ulConID)
{
	if (NULL == FNetClient_DCardStartPlayAudio) {
		return -1;
	}
	return FNetClient_DCardStartPlayAudio( _ulConID);
}

int NetClient_SetEncryptSN( int _iLogonID,int _iType,char* _cSN)
{
	if (NULL == FNetClient_SetEncryptSN) {
		return -1;
	}
	return FNetClient_SetEncryptSN( _iLogonID,  _iType,  _cSN);
}

int NetClient_GetSNReg( int _iLogonID,int* _piLockRet)
{
	if (NULL == FNetClient_GetSNReg) {
		return -1;
	}
	return FNetClient_GetSNReg( _iLogonID,  _piLockRet);
}

int NetClient_GetComFormat_V1(int _iLogonID, int _iComPort,char* _cDeviceType,char* _cCommFormat,int* _iWorkMode)
{
	if (NULL == FNetClient_GetComFormat_V1) {
		return -1;
	}
	return FNetClient_GetComFormat_V1( _iLogonID,  _iComPort,  _cDeviceType,  _cCommFormat,  _iWorkMode);
}

int NetClient_SetComFormat_V2(int _iLogonID, COMFORMAT* _pComFormat)
{
	if (NULL == FNetClient_SetComFormat_V2) {
		return -1;
	}
	return FNetClient_SetComFormat_V2( _iLogonID,  _pComFormat);
}

int NetClient_GetComFormat_V2(int _iLogonID, COMFORMAT* _pComFormat)
{
	if (NULL == FNetClient_GetComFormat_V2) {
		return -1;
	}
	return FNetClient_GetComFormat_V2( _iLogonID,  _pComFormat);
}

int NetClient_GetServerVersion_V1(int _iLogonID, SERVER_VERSION* _pstrctServerVer)
{
	if (NULL == FNetClient_GetServerVersion_V1) {
		return -1;
	}
	return FNetClient_GetServerVersion_V1( _iLogonID,  _pstrctServerVer);
}

int NetClient_InputTalkingdataEx(int _iLogonID, unsigned char* _ucData, unsigned int _iLen)
{
	if (NULL == FNetClient_InputTalkingdataEx) {
		return -1;
	}
	return FNetClient_InputTalkingdataEx( _iLogonID,  _ucData,  _iLen);
}

int NetClient_SetVerticalSync(unsigned int _ulConID,int _iFlag)
{
	if (NULL == FNetClient_SetVerticalSync) {
		return -1;
	}
	return FNetClient_SetVerticalSync( _ulConID,  _iFlag);
}

int NetClient_GetVerticalSync(unsigned int _ulConID,int* _piFlag)
{
	if (NULL == FNetClient_GetVerticalSync) {
		return -1;
	}
	return FNetClient_GetVerticalSync( _ulConID,  _piFlag);
}

int NetClient_SetLocalAudioVolumeEx(unsigned int _ulConID, int _iVolume)
{
	if (NULL == FNetClient_SetLocalAudioVolumeEx) {
		return -1;
	}
	return FNetClient_SetLocalAudioVolumeEx( _ulConID,  _iVolume);
}

int NetClient_GetLocalAudioVolumeEx(unsigned int _ulConID, int* _iVolume)
{
	if (NULL == FNetClient_GetLocalAudioVolumeEx) {
		return -1;
	}
	return FNetClient_GetLocalAudioVolumeEx( _ulConID,  _iVolume);
}

int NetClient_ClearPolyLocalVideo(unsigned int _uiConnID, int _iPolygonIndex)
{
	if (NULL == FNetClient_ClearPolyLocalVideo) {
		return -1;
	}
	return FNetClient_ClearPolyLocalVideo( _uiConnID,  _iPolygonIndex);
}

int NetClient_SetOSDTypeFontSize(int _iLogonID,int _iChannelNum,int _iOSDType, int _iSize)
{
	if (NULL == FNetClient_SetOSDTypeFontSize) {
		return -1;
	}
	return FNetClient_SetOSDTypeFontSize( _iLogonID,  _iChannelNum,  _iOSDType,  _iSize);
}

int NetClient_GetOSDTypeFontSize(int _iLogonID,int _iChannelNum,int _iOSDType, int* _iSize)
{
	if (NULL == FNetClient_GetOSDTypeFontSize) {
		return -1;
	}
	return FNetClient_GetOSDTypeFontSize( _iLogonID,  _iChannelNum,  _iOSDType,  _iSize);
}

int NetClient_SetImgDisposal( int _iLogonID,void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetImgDisposal) {
		return -1;
	}
	return FNetClient_SetImgDisposal( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetImgDisposal( int _iLogonID,void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetImgDisposal) {
		return -1;
	}
	return FNetClient_GetImgDisposal( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_SetMuted(unsigned int _uiConID, int _iMuted)
{
	if (NULL == FNetClient_SetMuted) {
		return -1;
	}
	return FNetClient_SetMuted( _uiConID,  _iMuted);
}

int NetClient_SetPWMValue(int _iLogonID, int _iChannelNo, int _iValue)
{
	if (NULL == FNetClient_SetPWMValue) {
		return -1;
	}
	return FNetClient_SetPWMValue( _iLogonID,  _iChannelNo,  _iValue);
}

int NetClient_GetPWMValue(int _iLogonID, int _iChannelNo, int* _piValue)
{
	if (NULL == FNetClient_GetPWMValue) {
		return -1;
	}
	return FNetClient_GetPWMValue( _iLogonID,  _iChannelNo,  _piValue);
}

int NetClient_SetSystemType( int _iLogonID,int _iType)
{
	if (NULL == FNetClient_SetSystemType) {
		return -1;
	}
	return FNetClient_SetSystemType( _iLogonID,  _iType);
}

int NetClient_GetSystemType( int _iLogonID,int* _piType)
{
	if (NULL == FNetClient_GetSystemType) {
		return -1;
	}
	return FNetClient_GetSystemType( _iLogonID,  _piType);
}

int NetClient_SetITSSwitchTime(int _iLogonID, int _iSwitchTime, int _iDelayTime)
{
	if (NULL == FNetClient_SetITSSwitchTime) {
		return -1;
	}
	return FNetClient_SetITSSwitchTime( _iLogonID,  _iSwitchTime,  _iDelayTime);
}

int NetClient_GetITSSwitchTime(int _iLogonID, int* _iSwitchTime, int* _iDelayTime)
{
	if (NULL == FNetClient_GetITSSwitchTime) {
		return -1;
	}
	return FNetClient_GetITSSwitchTime( _iLogonID,  _iSwitchTime,  _iDelayTime);
}

int NetClient_SetITSRecoParam(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSRecoParam) {
		return -1;
	}
	return FNetClient_SetITSRecoParam( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSRecoParam(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSRecoParam) {
		return -1;
	}
	return FNetClient_GetITSRecoParam( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_SetITSDayNight(int _iLogonID, int _iTimeSegment)
{
	if (NULL == FNetClient_SetITSDayNight) {
		return -1;
	}
	return FNetClient_SetITSDayNight( _iLogonID,  _iTimeSegment);
}

int NetClient_GetITSDayNight(int _iLogonID, int* _iTimeSegment)
{
	if (NULL == FNetClient_GetITSDayNight) {
		return -1;
	}
	return FNetClient_GetITSDayNight( _iLogonID,  _iTimeSegment);
}

int NetClient_SetITSCamLocation(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSCamLocation) {
		return -1;
	}
	return FNetClient_SetITSCamLocation( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSCamLocation(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSCamLocation) {
		return -1;
	}
	return FNetClient_GetITSCamLocation( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_SetITSWorkMode(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSWorkMode) {
		return -1;
	}
	return FNetClient_SetITSWorkMode( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSWorkMode(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSWorkMode) {
		return -1;
	}
	return FNetClient_GetITSWorkMode( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_SetWaterMarkEnable( int _iLogonID,int _iChannelID,int _iEnable)
{
	if (NULL == FNetClient_SetWaterMarkEnable) {
		return -1;
	}
	return FNetClient_SetWaterMarkEnable( _iLogonID,  _iChannelID,  _iEnable);
}

int NetClient_GetWaterMarkEnable( int _iLogonID,int _iChannelID,int* _piEnable)
{
	if (NULL == FNetClient_GetWaterMarkEnable) {
		return -1;
	}
	return FNetClient_GetWaterMarkEnable( _iLogonID,  _iChannelID,  _piEnable);
}

int NetClient_SetITSLightInfo(int _iLogonID, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetITSLightInfo) {
		return -1;
	}
	return FNetClient_SetITSLightInfo( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetITSLightInfo(int _iLogonID, void * _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetITSLightInfo) {
		return -1;
	}
	return FNetClient_GetITSLightInfo( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_SetHardWareParam( int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetHardWareParam) {
		return -1;
	}
	return FNetClient_SetHardWareParam( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetHardWareParam( int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetHardWareParam) {
		return -1;
	}
	return FNetClient_GetHardWareParam( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetDomeAdvParam( int _iLogonID, int _iChannelNO, int _iCmd, void* _pvCmdBuf, int _iCmdBufLen)
{
	if (NULL == FNetClient_SetDomeAdvParam) {
		return -1;
	}
	return FNetClient_SetDomeAdvParam( _iLogonID,  _iChannelNO,  _iCmd,  _pvCmdBuf,  _iCmdBufLen);
}

int NetClient_GetDomeAdvParam( int _iLogonID, int _iChannelNO, int _iCmd, void* _pvCmdBuf, int _iCmdBufLen)
{
	if (NULL == FNetClient_GetDomeAdvParam) {
		return -1;
	}
	return FNetClient_GetDomeAdvParam( _iLogonID,  _iChannelNO,  _iCmd,  _pvCmdBuf,  _iCmdBufLen);
}

int NetClient_SetDiskGroup( int _iLogonID,void* _lpBuf, int _iDiskGroupNum)
{
	if (NULL == FNetClient_SetDiskGroup) {
		return -1;
	}
	return FNetClient_SetDiskGroup( _iLogonID,  _lpBuf,  _iDiskGroupNum);
}

int NetClient_GetDiskGroup( int _iLogonID,void* _lpBuf, int _iDiskGroupNum)
{
	if (NULL == FNetClient_GetDiskGroup) {
		return -1;
	}
	return FNetClient_GetDiskGroup( _iLogonID,  _lpBuf,  _iDiskGroupNum);
}

int NetClient_SetDiskQuota( int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetDiskQuota) {
		return -1;
	}
	return FNetClient_SetDiskQuota( _iLogonID,  _iChannel,  _lpBuf,  _iBufSize);
}

int NetClient_GetDiskQuotaState ( int _iLogonID, int _iChannelNo, int _iCmd, void* _lpBuf, int _iBufSize )
{
	if (NULL == FNetClient_GetDiskQuotaState ) {
		return -1;
	}
	return FNetClient_GetDiskQuotaState ( _iLogonID,  _iChannelNo,  _iCmd,  _lpBuf,  _iBufSize );
}

int NetClient_ModifyUserAuthority( int _iLogonID, char* _pcUserName, void* _lpBuf, int _iBufSize )
{
	if (NULL == FNetClient_ModifyUserAuthority) {
		return -1;
	}
	return FNetClient_ModifyUserAuthority( _iLogonID,  _pcUserName,  _lpBuf,  _iBufSize );
}

int NetClient_GetUserAuthority( int _iLogonID, char* _pcUserName, void* _lpBuf, int _iBufSize )
{
	if (NULL == FNetClient_GetUserAuthority) {
		return -1;
	}
	return FNetClient_GetUserAuthority( _iLogonID,  _pcUserName,  _lpBuf,  _iBufSize );
}

int NetClient_GetGroupAuthority( int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetGroupAuthority) {
		return -1;
	}
	return FNetClient_GetGroupAuthority( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_NetFileGetQueryfileEx(int _iLogonID,int _iFileIndex, PNVS_FILE_DATA_EX _pFileInfo)
{
	if (NULL == FNetClient_NetFileGetQueryfileEx) {
		return -1;
	}
	return FNetClient_NetFileGetQueryfileEx( _iLogonID,  _iFileIndex,  _pFileInfo);
}

int NetClient_NetFileLockFile( int _iLogonID, char* _cFileName, int _iLock)
{
	if (NULL == FNetClient_NetFileLockFile) {
		return -1;
	}
	return FNetClient_NetFileLockFile( _iLogonID,  _cFileName,  _iLock);
}

int NetClient_GetOsdTextEx(int _iLogonID,int _iChannel,void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetOsdTextEx) {
		return -1;
	}
	return FNetClient_GetOsdTextEx( _iLogonID,  _iChannel,  _lpBuf,  _iBufSize);
}

int NetClient_SetHolidayPlan(int _iLogonID,int _iCmd,void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetHolidayPlan) {
		return -1;
	}
	return FNetClient_SetHolidayPlan( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_GetHolidayPlan(int _iLogonID,int _iCmd,void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetHolidayPlan) {
		return -1;
	}
	return FNetClient_GetHolidayPlan( _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetCommonEnable( int _iLogonID, int _iEnableID, int _iChannel, int _iEnableValue )
{
	if (NULL == FNetClient_SetCommonEnable) {
		return -1;
	}
	return FNetClient_SetCommonEnable( _iLogonID,  _iEnableID,  _iChannel,  _iEnableValue );
}

int NetClient_GetCommonEnable( int _iLogonID, int _iEnableID, int _iChannel, int* _iEnableValue )
{
	if (NULL == FNetClient_GetCommonEnable) {
		return -1;
	}
	return FNetClient_GetCommonEnable( _iLogonID,  _iEnableID,  _iChannel,  _iEnableValue );
}

int NetClient_NetFileDownload(unsigned int* _ulConID, int _iLogonID, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_NetFileDownload) {
		return -1;
	}
	return FNetClient_NetFileDownload( _ulConID,  _iLogonID,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_Upgrade_V4(int _iLogonID, char* _cFileName, int _iUpgradeType, UPGRADE_NOTIFY_V4 _UpgradeNotify)
{
	if (NULL == FNetClient_Upgrade_V4) {
		return -1;
	}
	return FNetClient_Upgrade_V4( _iLogonID,  _cFileName,  _iUpgradeType,  _UpgradeNotify);
}

int NetClient_GetAudioCoderList( int _iLogonID, int _iChannel, int _iStreamNo, int* _piList, int* _piLstCount )
{
	if (NULL == FNetClient_GetAudioCoderList) {
		return -1;
	}
	return FNetClient_GetAudioCoderList( _iLogonID,  _iChannel,  _iStreamNo,  _piList,  _piLstCount );
}

int NetClient_InnerAutoTest(int _iLogonID, int _iCmd, void* _pvTestBuf, int _iBufSize)
{
	if (NULL == FNetClient_InnerAutoTest) {
		return -1;
	}
	return FNetClient_InnerAutoTest( _iLogonID,  _iCmd,  _pvTestBuf,  _iBufSize);
}

int NetClient_SetJEPGSize(int _iLogonID, int _iChannelNo, int _iWidth, int _iHeight)
{
	if (NULL == FNetClient_SetJEPGSize) {
		return -1;
	}
	return FNetClient_SetJEPGSize( _iLogonID,  _iChannelNo,  _iWidth,  _iHeight);
}

int NetClient_GetJEPGSize(int _iLogonID, int _iChannelNo, int* _iWidth, int * _iHeight)
{
	if (NULL == FNetClient_GetJEPGSize) {
		return -1;
	}
	return FNetClient_GetJEPGSize( _iLogonID,  _iChannelNo,  _iWidth,  _iHeight);
}

int NetClient_QueryDevStatus(int _iLogonID, int _iType)
{
	if (NULL == FNetClient_QueryDevStatus) {
		return -1;
	}
	return FNetClient_QueryDevStatus( _iLogonID,  _iType);
}

int NetClient_GetDevStatus(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetDevStatus) {
		return -1;
	}
	return FNetClient_GetDevStatus( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetHDTemplateIndex(int _iLogonID, int _iChannelNo /*= 0*/, int* _iTemplateIndex)
{
	if (NULL == FNetClient_GetHDTemplateIndex) {
		return -1;
	}
	return FNetClient_GetHDTemplateIndex( _iLogonID,  _iChannelNo /*= 0*/,  _iTemplateIndex);
}

int NetClient_SetStreamInsertData(int _iLogonID, int _iChannelNo, int _iFlag, int _iType, void* _pDataBuf, int _iDataLen)
{
	if (NULL == FNetClient_SetStreamInsertData) {
		return -1;
	}
	return FNetClient_SetStreamInsertData( _iLogonID,  _iChannelNo,  _iFlag,  _iType,  _pDataBuf,  _iDataLen);
}

int NetClient_GetStreamInsertData(int _iLogonID, int _iChannelNo, int _iFlag, int _iType, void* _pDataBuf, int _iDataLen)
{
	if (NULL == FNetClient_GetStreamInsertData) {
		return -1;
	}
	return FNetClient_GetStreamInsertData( _iLogonID,  _iChannelNo,  _iFlag,  _iType,  _pDataBuf,  _iDataLen);
}

int NetClient_GetOtherID(int _iLogonID, void* _pcBuff, int _iBufLen)
{
	if (NULL == FNetClient_GetOtherID) {
		return -1;
	}
	return FNetClient_GetOtherID( _iLogonID,  _pcBuff,  _iBufLen);
}

int NetClient_SetDomePTZ(int _iLogonID, int _iChannelNo, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetDomePTZ) {
		return -1;
	}
	return FNetClient_SetDomePTZ( _iLogonID,  _iChannelNo,  _lpBuf,  _iBufSize);
}

int NetClient_GetDomePTZ(int _iLogonID, int _iChannelNo, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetDomePTZ) {
		return -1;
	}
	return FNetClient_GetDomePTZ( _iLogonID,  _iChannelNo,  _lpBuf,  _iBufSize);
}

int NetClient_GetUserDataInfo(unsigned int _ulConID, int _iFlag, void*  _pBuffer, int _iSize)
{
	if (NULL == FNetClient_GetUserDataInfo) {
		return -1;
	}
	return FNetClient_GetUserDataInfo( _ulConID,  _iFlag,  _pBuffer,  _iSize);
}

int NetClient_GetBroadcastMessage( int _iLogonID,void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetBroadcastMessage) {
		return -1;
	}
	return FNetClient_GetBroadcastMessage( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_GetModuleCapability( int _iLogonID,int _iModule,unsigned int* _uCaps)
{
	if (NULL == FNetClient_GetModuleCapability) {
		return -1;
	}
	return FNetClient_GetModuleCapability( _iLogonID,  _iModule,  _uCaps);
}

int NetClient_KeyboardCtrl(char* _pcIP, int _iAction,int _iValue)
{
	if (NULL == FNetClient_KeyboardCtrl) {
		return -1;
	}
	return FNetClient_KeyboardCtrl( _pcIP,  _iAction,  _iValue);
}

int NetClient_NetFileSetSchedule(int _iLogonID, int _iChannel,int _iCmd,void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_NetFileSetSchedule) {
		return -1;
	}
	return FNetClient_NetFileSetSchedule( _iLogonID,  _iChannel,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_NetFileGetSchedule(int _iLogonID, int _iChannel, int _iCmd, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_NetFileGetSchedule) {
		return -1;
	}
	return FNetClient_NetFileGetSchedule( _iLogonID,  _iChannel,  _iCmd,  _lpBuf,  _iBufSize);
}

int NetClient_SetDevConfig(int _iLogonID, int _iCommand, int _iChannel, void* _lpInBuffer, int _iInBufferSize)
{
	if (NULL == FNetClient_SetDevConfig) {
		return -1;
	}
	return FNetClient_SetDevConfig( _iLogonID,  _iCommand,  _iChannel,  _lpInBuffer,  _iInBufferSize);
}

int NetClient_GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, void* _lpOutBuffer, int _iOutBufferSize, int* _lpBytesReturned)
{
	if (NULL == FNetClient_GetDevConfig) {
		return -1;
	}
	return FNetClient_GetDevConfig( _iLogonID,  _iCommand,  _iChannel,  _lpOutBuffer,  _iOutBufferSize,  _lpBytesReturned);
}

int NetClient_SendCommand(int _iLogonID,  int _iCommand,  int _iChannel,  void* _pBuffer,  int _iBufferSize)
{
	if (NULL == FNetClient_SendCommand) {
		return -1;
	}
	return FNetClient_SendCommand( _iLogonID,  _iCommand,  _iChannel,  _pBuffer,  _iBufferSize);
}

int NetClient_RecvCommand(int _iLogonID,  int _iCommand,  int _iChannel,  void* _pBuffer,  int _iBufferSize)
{
	if (NULL == FNetClient_RecvCommand) {
		return -1;
	}
	return FNetClient_RecvCommand( _iLogonID,  _iCommand,  _iChannel,  _pBuffer,  _iBufferSize);
}

int NetClient_SetDevDiskConfig (int _iLogonID, int _iCommand, int _iChannel, void* _lpInBuffer, int _iInBufferSize)
{
	if (NULL == FNetClient_SetDevDiskConfig ) {
		return -1;
	}
	return FNetClient_SetDevDiskConfig ( _iLogonID,  _iCommand,  _iChannel,  _lpInBuffer,  _iInBufferSize);
}

int NetClient_GetDevDiskConfig (int _iLogonID, int _iCommand, int _iChannel, void* _lpOutBuffer, int _iOutBufferSize, int* _lpBytesReturned)
{
	if (NULL == FNetClient_GetDevDiskConfig ) {
		return -1;
	}
	return FNetClient_GetDevDiskConfig ( _iLogonID,  _iCommand,  _iChannel,  _lpOutBuffer,  _iOutBufferSize,  _lpBytesReturned);
}

int NetClient_Logon_V4(int _iLogonType, void* _pvPara, int _iInBufferSize)
{
	if (NULL == FNetClient_Logon_V4) {
		return -1;
	}
	return FNetClient_Logon_V4( _iLogonType,  _pvPara,  _iInBufferSize);
}

int NetClient_PlayBackControl(unsigned long _ulConID, int _iControlCode, void* _pcInBuffer, int _iInLen, void* _pcOutBuffer, int* _iOutLen)
{
	if (NULL == FNetClient_PlayBackControl) {
		return -1;
	}
	return FNetClient_PlayBackControl( _ulConID,  _iControlCode,  _pcInBuffer,  _iInLen,  _pcOutBuffer,  _iOutLen);
}

int NetClient_PlayerControl(unsigned int _uiRecvID, int _iCmd, void* _pvCmdBuf, int _iBufLen, void* _pvUsrData)
{
	if (NULL == FNetClient_PlayerControl) {
		return -1;
	}
	return FNetClient_PlayerControl( _uiRecvID,  _iCmd,  _pvCmdBuf,  _iBufLen,  _pvUsrData);
}

int NetClient_StopPlayBack(unsigned long _ulConID)
{
	if (NULL == FNetClient_StopPlayBack) {
		return -1;
	}
	return FNetClient_StopPlayBack( _ulConID);
}

int NetClient_PlayBack(unsigned int* _ulConID, int _iCmd, PlayerParam* _PlayerParam, void* _hWnd)
{
	if (NULL == FNetClient_PlayBack) {
		return -1;
	}
	return FNetClient_PlayBack( _ulConID,  _iCmd,  _PlayerParam,  _hWnd);
}

int NetClient_GetPseChInfo(int _iLogonID, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetPseChInfo) {
		return -1;
	}
	return FNetClient_GetPseChInfo( _iLogonID,  _lpBuf,  _iBufSize);
}

int NetClient_SetPseChProperty(int _iLogonID, int _iPseCh, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetPseChProperty) {
		return -1;
	}
	return FNetClient_SetPseChProperty( _iLogonID,  _iPseCh,  _lpBuf,  _iBufSize);
}

int NetClient_GetPseChProperty(int _iLogonID, int _iPseCh, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetPseChProperty) {
		return -1;
	}
	return FNetClient_GetPseChProperty( _iLogonID,  _iPseCh,  _lpBuf,  _iBufSize);
}

int NetClient_ChannelTalkStart(int _iLogonID, int _iChannel, int _iUser)
{
	if (NULL == FNetClient_ChannelTalkStart) {
		return -1;
	}
	return FNetClient_ChannelTalkStart( _iLogonID,  _iChannel,  _iUser);
}

int NetClient_ChannelTalkEnd(int _iLogonID, int _iChannel)
{
	if (NULL == FNetClient_ChannelTalkEnd) {
		return -1;
	}
	return FNetClient_ChannelTalkEnd( _iLogonID,  _iChannel);
}

int NetClient_InputChannelTalkingdata(int _iLogonID, int _iChannel, unsigned char* _ucData, unsigned int _iLen)
{
	if (NULL == FNetClient_InputChannelTalkingdata) {
		return -1;
	}
	return FNetClient_InputChannelTalkingdata( _iLogonID,  _iChannel,  _ucData,  _iLen);
}

int NetClient_GetChannelTalkingState(int _iLogonID, int _iChannel, int* _iTalkState)
{
	if (NULL == FNetClient_GetChannelTalkingState) {
		return -1;
	}
	return FNetClient_GetChannelTalkingState( _iLogonID,  _iChannel,  _iTalkState);
}

int NetClient_CapturePicture(unsigned int _ulConID, int _iPicType, char* _pcFileName)
{
	if (NULL == FNetClient_CapturePicture) {
		return -1;
	}
	return FNetClient_CapturePicture( _ulConID,  _iPicType,  _pcFileName);
}

int NetClient_CapturePicData(unsigned int _ulConID, int _iPicType, char* _pcPicBuf, int* _piPicSize)
{
	if (NULL == FNetClient_CapturePicData) {
		return -1;
	}
	return FNetClient_CapturePicData( _ulConID,  _iPicType,  _pcPicBuf,  _piPicSize);
}

int NetClient_SetSDKWorkMode(int _iWorkMode)
{
	if (NULL == FNetClient_SetSDKWorkMode) {
		return -1;
	}
	return FNetClient_SetSDKWorkMode( _iWorkMode);
}

int NetClient_Query_V4(int _iLogonID, int _iCmd, int _iChannel, void* _pvCmdBuf, int _iBufLen)
{
	if (NULL == FNetClient_Query_V4) {
		return -1;
	}
	return FNetClient_Query_V4( _iLogonID,  _iCmd,  _iChannel,  _pvCmdBuf,  _iBufLen);
}

int NetClient_GetQueryResult_V4(int _iLogonID, int _iCmd, int _iChannel, int _iIndex, void* _pvCmdBuf, int _iBufLen)
{
	if (NULL == FNetClient_GetQueryResult_V4) {
		return -1;
	}
	return FNetClient_GetQueryResult_V4( _iLogonID,  _iCmd,  _iChannel,  _iIndex,  _pvCmdBuf,  _iBufLen);
}

int NetClient_RebootDeviceByType(int _iLogonID, int _iType, void* _pvCmdBuf, int _iBufLen)
{
	if (NULL == FNetClient_RebootDeviceByType) {
		return -1;
	}
	return FNetClient_RebootDeviceByType( _iLogonID,  _iType,  _pvCmdBuf,  _iBufLen);
}

int NetClient_StartDownload(int _iLogonID, int _iChannel, int _iDownloadMode, void* _lpInBuf, int _iInBufLen
									  , unsigned long* _pulDownloadFd)
{
	if (NULL == FNetClient_StartDownload) {
		return -1;
	}
	return FNetClient_StartDownload( _iLogonID,  _iChannel,  _iDownloadMode,  _lpInBuf,  _iInBufLen
									  ,  _pulDownloadFd);
}

int NetClient_StopDownload(unsigned long _ulDownloadFd)
{
	if (NULL == FNetClient_StopDownload) {
		return -1;
	}
	return FNetClient_StopDownload( _ulDownloadFd);
}

int NetClient_GetDownloadPos(unsigned long _ulDownloadFd, int* _puiDownloadPos)
{
	if (NULL == FNetClient_GetDownloadPos) {
		return -1;
	}
	return FNetClient_GetDownloadPos( _ulDownloadFd,  _puiDownloadPos);
}

int NetClient_ProxySend (int _iProtocolType, bool _blSend)
{
	if (NULL == FNetClient_ProxySend ) {
		return -1;
	}
	return FNetClient_ProxySend ( _iProtocolType,  _blSend);
}

int NetClient_SetDevUserDataNotify(unsigned int _uiRecvID, DEVUSERDATA_NOTIFY _cbkDevUserData, void* _pvUdata)
{
	if (NULL == FNetClient_SetDevUserDataNotify) {
		return -1;
	}
	return FNetClient_SetDevUserDataNotify( _uiRecvID,  _cbkDevUserData,  _pvUdata);
}

int NetClient_SetDsmConfig(int _iCommand, void* _pvBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetDsmConfig) {
		return -1;
	}
	return FNetClient_SetDsmConfig( _iCommand,  _pvBuf,  _iBufSize);
}

int NetClient_GetDsmRegstierInfo(int _iCommand, void* _pvBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetDsmRegstierInfo) {
		return -1;
	}
	return FNetClient_GetDsmRegstierInfo( _iCommand,  _pvBuf,  _iBufSize);
}

int NetClient_GetRecvInfoById(int _iRecvId, void* _pvBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetRecvInfoById) {
		return -1;
	}
	return FNetClient_GetRecvInfoById( _iRecvId,  _pvBuf,  _iBufSize);
}

int NetClient_GetParamFromDevice(int _iLogonID, int _iChannel, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_GetParamFromDevice) {
		return -1;
	}
	return FNetClient_GetParamFromDevice( _iLogonID,  _iChannel,  _lpBuf,  _iBufSize);
}

int NetClient_GetPlayerIndex(unsigned int _uiConID)
{
	if (NULL == FNetClient_GetPlayerIndex) {
		return -1;
	}
	return FNetClient_GetPlayerIndex( _uiConID);
}

int NetClient_GetRealPlayerIndex(unsigned int* _ulConID)
{
	if (NULL == FNetClient_GetRealPlayerIndex) {
		return -1;
	}
	return FNetClient_GetRealPlayerIndex( _ulConID);
}

int NetClient_StartRecvNetPicStream(int _iLogonID, NetPicPara* _ptPara, int _iBufLen, unsigned int* _puiRecvID)
{
	if (NULL == FNetClient_StartRecvNetPicStream) {
		return -1;
	}
	return FNetClient_StartRecvNetPicStream( _iLogonID,  _ptPara,  _iBufLen,  _puiRecvID);
}

int NetClient_StopRecvNetPicStream(unsigned int _uiRecvID)
{
	if (NULL == FNetClient_StopRecvNetPicStream) {
		return -1;
	}
	return FNetClient_StopRecvNetPicStream( _uiRecvID);
}

int NetClient_SetProxyNotifyFunction(MAIN_NOTIFY_V4     _ProxyMainNotify,
											 PROXY_NOTIFY       _ProxyNotify)
{
	if (NULL == FNetClient_SetProxyNotifyFunction) {
		return -1;
	}
	return FNetClient_SetProxyNotifyFunction( _ProxyMainNotify,  _ProxyNotify);
}

int NetClient_SetExternDevLogonInfo(unsigned int _uiAllowDevType)
{
	if (NULL == FNetClient_SetExternDevLogonInfo) {
		return -1;
	}
	return FNetClient_SetExternDevLogonInfo( _uiAllowDevType);
}

int NetClient_SetUnipueAlertConfig(int _iLogonId, int _iCmdId, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen)
{
	if (NULL == FNetClient_SetUnipueAlertConfig) {
		return -1;
	}
	return FNetClient_SetUnipueAlertConfig( _iLogonId,  _iCmdId,  _iChannel,  _lpCmdBuf,  _iCmdBufLen);
}

int NetClient_GetUnipueAlertConfig(int _iLogonId, int _iCmdId, int _iChannel, void* _lpCmdBuf, int _iCmdBufLen)
{
	if (NULL == FNetClient_GetUnipueAlertConfig) {
		return -1;
	}
	return FNetClient_GetUnipueAlertConfig( _iLogonId,  _iCmdId,  _iChannel,  _lpCmdBuf,  _iCmdBufLen);
}

int NetClient_FaceConfig(int _iLogonId, int _iCmdId, int _iChanNo, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen)
{
	if (NULL == FNetClient_FaceConfig) {
		return -1;
	}
	return FNetClient_FaceConfig( _iLogonId,  _iCmdId,  _iChanNo,  _lpIn,  _iInLen,  _lpOut,  _iOutLen);
}

int NetClient_Query_V5(int _iLogonId, int _iCmdId, int _iChanNo, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen)
{
	if (NULL == FNetClient_Query_V5) {
		return -1;
	}
	return FNetClient_Query_V5( _iLogonId,  _iCmdId,  _iChanNo,  _lpIn,  _iInLen,  _lpOut,  _iOutLen);
}

int NetClient_SetAlarmNotify_V5(ALARM_NOTIFY_V5 _pAlarm)
{
	if (NULL == FNetClient_SetAlarmNotify_V5) {
		return -1;
	}
	return FNetClient_SetAlarmNotify_V5( _pAlarm);
}

int NetClient_Upgrade_V5(int _iLogonId, int _iType, void* _lpBuf, int _iBufSize)
{
	if (NULL == FNetClient_Upgrade_V5) {
		return -1;
	}
	return FNetClient_Upgrade_V5( _iLogonId,  _iType,  _lpBuf,  _iBufSize);
}

int NetClient_CmdConfig(int _iLogonId, int _iCmdId, int _iChanNo, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen)
{
	if (NULL == FNetClient_CmdConfig) {
		return -1;
	}
	return FNetClient_CmdConfig( _iLogonId,  _iCmdId,  _iChanNo,  _lpIn,  _iInLen,  _lpOut,  _iOutLen);
}

int NetClient_GetLastError()
{
	if (NULL == FNetClient_GetLastError) {
		return -1;
	}
	return FNetClient_GetLastError();
}

int NetClient_GetConncetInfo(unsigned int _ulConID, int _iCmdId, void* _pvCmdBuf, int _iCmdBufLen)
{
	if (NULL == FNetClient_GetConncetInfo) {
		return -1;
	}
	return FNetClient_GetConncetInfo( _ulConID,  _iCmdId,  _pvCmdBuf,  _iCmdBufLen);
}

int NetClient_SyncLogon(int _iLogonType, void* _pvPara, int _iParaSize)
{
	if (NULL == FNetClient_SyncLogon) {
		return -1;
	}
	return FNetClient_SyncLogon( _iLogonType,  _pvPara,  _iParaSize);
}

int NetClient_SyncRealPlay(unsigned int* _puiRecvID, NetClientPara* _ptPara, int _iParaSize)
{
	if (NULL == FNetClient_SyncRealPlay) {
		return -1;
	}
	return FNetClient_SyncRealPlay( _puiRecvID,  _ptPara,  _iParaSize);
}

int NetClient_StopRealPlay(unsigned int _uiRecvID, int _iParam)
{
	if (NULL == FNetClient_StopRealPlay) {
		return -1;
	}
	return FNetClient_StopRealPlay( _uiRecvID,  _iParam);
}

int NetClient_SyncQuery(int _iLogonID, int _iChanNo, int _iCmd, void* _pvInPara, int _iInLen, void* _pvOutPara, int _iOutTotalLen, int _iSingleLen)
{
	if (NULL == FNetClient_SyncQuery) {
		return -1;
	}
	return FNetClient_SyncQuery( _iLogonID,  _iChanNo,  _iCmd,  _pvInPara,  _iInLen,  _pvOutPara,  _iOutTotalLen,  _iSingleLen);
}

int NetClient_SyncSetDevCfg(int _iLogonID, int _iChanNo, int _iCmd, void* _pvInPara, int _iInLen, void* _pvOutRet, int _iOutLen)
{
	if (NULL == FNetClient_SyncSetDevCfg) {
		return -1;
	}
	return FNetClient_SyncSetDevCfg( _iLogonID,  _iChanNo,  _iCmd,  _pvInPara,  _iInLen,  _pvOutRet,  _iOutLen);
}

int NetClient_CapturePicByDevice(int _iLogonID, int _iChanNo, int _iQvalue, char* _pcPicFilePath, SnapPicData* _ptSnapPicData, int _iInSize)
{
	if (NULL == FNetClient_CapturePicByDevice) {
		return -1;
	}
	return FNetClient_CapturePicByDevice( _iLogonID,  _iChanNo,  _iQvalue,  _pcPicFilePath,  _ptSnapPicData,  _iInSize);
}

int NetClient_SetSDKInitConfig(int _iCmd, void* _lpInBuffer, int _iInBufferSize)
{
	if (NULL == FNetClient_SetSDKInitConfig) {
		return -1;
	}
	return FNetClient_SetSDKInitConfig( _iCmd,  _lpInBuffer,  _iInBufferSize);
}

int NetClient_SetAVMode(int _iRecvID, int _iCmd, void* _pvBuf, int _iBufSize)
{
	if (NULL == FNetClient_SetAVMode) {
		return -1;
	}
	return FNetClient_SetAVMode( _iRecvID,  _iCmd,  _pvBuf,  _iBufSize);
}

void* NetClient_CreateQtWidget(void* _pvParaBuf, int _iBufSize)
{
	if (NULL == FNetClient_CreateQtWidget) {
		return NULL;
	}
	return FNetClient_CreateQtWidget( _pvParaBuf,  _iBufSize);
}

int NetClient_ReleaseQtWidget(void* _pvQWidget)
{
	if (NULL == FNetClient_ReleaseQtWidget) {
		return -1;
	}
	return FNetClient_ReleaseQtWidget( _pvQWidget);
}

int NetClient_GetDevConfig_V5(int _iLogonID, int _iCmd, void* _pvInParaBuf, int _iInBufLen, void* _pvOutParaBuf, int _iOutBufLen)
{
	if (NULL == FNetClient_GetDevConfig_V5) {
		return -1;
	}
	return FNetClient_GetDevConfig_V5( _iLogonID,  _iCmd,  _pvInParaBuf,  _iInBufLen,  _pvOutParaBuf,  _iOutBufLen);
}

int NetClient_SycVoiceTalkStart(unsigned int* _puiTalkID, NetVoiceTalkPara* _pvPara, int _iSize)
{
	if (NULL == FNetClient_SycVoiceTalkStart) {
		return -1;
	}
	return FNetClient_SycVoiceTalkStart( _puiTalkID,  _pvPara,  _iSize);
}

int NetClient_SycVoiceTalkStop(unsigned int _uiTalkID, void* _pvPara, int _iSize)
{
	if (NULL == FNetClient_SycVoiceTalkStop) {
		return -1;
	}
	return FNetClient_SycVoiceTalkStop( _uiTalkID,  _pvPara,  _iSize);
}

int NetClient_SycVoiceTalkInputData(NetVoiceTalkInput* _pvPara, int _iSize)
{
	if (NULL == FNetClient_SycVoiceTalkInputData) {
		return -1;
	}
	return FNetClient_SycVoiceTalkInputData( _pvPara,  _iSize);
}

int NetClient_HttpXmlConfig(int _iLogonID, XmlCfgInPara* _ptXmlInPara, int _iInSize, XmlCfgOutPara* _ptXmlOutPara, int _iOutSize)
{
	if (NULL == FNetClient_HttpXmlConfig) {
		return -1;
	}
	return FNetClient_HttpXmlConfig( _iLogonID,  _ptXmlInPara,  _iInSize,  _ptXmlOutPara,  _iOutSize);
}

int NetClient_XmlSetDevConfig(int _iLogonID, int _iCmd, void* _pvInPara, int _iInSize, void* _pvOutRet, int _iOutSize)
{
	if (NULL == FNetClient_XmlSetDevConfig) {
		return -1;
	}
	return FNetClient_XmlSetDevConfig( _iLogonID,  _iCmd,  _pvInPara,  _iInSize,  _pvOutRet,  _iOutSize);
}

int NetClient_XmlGetDevConfig(int _iLogonID, int _iCmd, void* _pvInPara, int _iInSize, void* _pvOutRet, int _iOutSize)
{
	if (NULL == FNetClient_XmlGetDevConfig) {
		return -1;
	}
	return FNetClient_XmlGetDevConfig( _iLogonID,  _iCmd,  _pvInPara,  _iInSize,  _pvOutRet,  _iOutSize);
}


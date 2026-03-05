#ifndef MACRODEFINE_H
#define MACRODEFINE_H

#include "stdafx.h"

#define DOWNLOAD_DEMO_INT_ZERO								 0
#define DOWNLOAD_DEMO_INT_ONE								 1
#define DOWNLOAD_DEMO_INT_TWO								 2
#define DOWNLOAD_DEMO_INT_THREE								 3
#define DOWNLOAD_DEMO_TYPE_AUDIO_FRAME						 5
#define DOWNLOAD_DEMO_INT_GEGATIVE_ONE						-1
#define DOWNLOAD_DEMO_CHANNEL_NUM_ALL						255
#define DOWNLOAD_DEMO_PLAY_SPEED_ONE_TIME					1		
#define DOWNLOAD_DEMO_CHANNEL_NUM_COUNTER					160
#define DOWNLOAD_DEMO_PROCESS_RANGE							90
#define DOWNLOAD_DEMO_WOWNLOAD_FINISH						100
#define DOWNLOAD_DEMO_SERVER_PORT							3000
#define DOWNLOAD_DEMO_CLIENT_PORT							6000
#define DOWNLOAD_DEMO_MAX_VALUE								65535
#define DOWNLOAD_DEMO_GEGATIVE_ONE							_T("-1")
#define DOWNLOAD_DEMO_ZERO									_T("0")
#define DOWNLOAD_DEMO_ONE									_T("1")
#define DOWNLOAD_DEMO_DOUBLE								_T("2")
#define DOWNLOAD_DEMO_TRIPLE								_T("3")
#define DOWNLOAD_DEMO_QUADRUPLE								_T("4")
#define DOWNLOAD_DEMO_EIGHT									_T("8")
#define DOWNLOAD_DEMO_NULL_CHARACTER						_T("")	

#define DOWNLOAD_DEMO_ALL									_T("所有")
#define DOWNLOAD_DEMO_FILE_TYPE_VIDOE						_T("音视频")
#define DOWNLOAD_DEMO_FILE_TYPE_PICTURE						_T("图片")
#define DOWNLOAD_DEMO_FILE_INFO_NAME						_T("文件名")
#define DOWNLOAD_DEMO_FILE_INFO_VIDEO_TYPE					_T("录像类型")
#define DOWNLOAD_DEMO_FILE_INFO_SIZE						_T("文件大小")
#define DOWNLOAD_DEMO_FILE_INFO_START_TIME					_T("开始时间")
#define DOWNLOAD_DEMO_FILE_INFO_STOP_TIME					_T("结束时间")
#define DOWNLOAD_DEMO_FILE_INFO_DOWNLOAD_PROCESS			_T("下载进度")
#define DOWNLOAD_DEMO_FILE_INFO_CHANNEL_NUL					_T("通道号")
#define DOWNLOAD_DEMO_FILE_INFO_MANNUL_RECORD				_T("手动录像")
#define DOWNLOAD_DEMO_FILE_INFO_SCHEDULE_RECORD				_T("定时录像")
#define DOWNLOAD_DEMO_FILE_INFO_ALARM_RECORD				_T("报警录像")

#define DOWNLOAD_DEMO_ALL_EN								_T("ALL")
#define DOWNLOAD_DEMO_FILE_TYPE_VIDOE_EN					_T("Video")
#define DOWNLOAD_DEMO_FILE_TYPE_PICTURE_EN					_T("Picture")
#define DOWNLOAD_DEMO_FILE_INFO_NAME_EN						_T("FileName")
#define DOWNLOAD_DEMO_FILE_INFO_VIDEO_TYPE_EN				_T("RecType")
#define DOWNLOAD_DEMO_FILE_INFO_SIZE_EN						_T("FileSize")
#define DOWNLOAD_DEMO_FILE_INFO_START_TIME_EN				_T("BeginTime")
#define DOWNLOAD_DEMO_FILE_INFO_STOP_TIME_EN				_T("EndTime")
#define DOWNLOAD_DEMO_FILE_INFO_DOWNLOAD_PROCESS_EN			_T("DownloadProcess")
#define DOWNLOAD_DEMO_FILE_INFO_CHANNEL_NUL_EN				_T("ChannelNum")
#define DOWNLOAD_DEMO_FILE_INFO_MANNUL_RECORD_EN			_T("MannulVideo")
#define DOWNLOAD_DEMO_FILE_INFO_SCHEDULE_RECORD_EN			_T("TimerVideo")
#define DOWNLOAD_DEMO_FILE_INFO_ALARM_RECORD_EN				_T("AlarmVideo")

#define DOWNLOAD_DEMO_TIME_SPACE							_T("时间范围")
#define DOWNLOAD_DEMO_SET									_T("设置")
#define DOWNLOAD_DEMO_TO									_T("至")
#define DOWNLOAD_DEMO_TO_EN									_T("TO")
#define DOWNLOAD_DEMO_DOWNLOAD								_T("下载")
#define DOWNLOAD_DEMO_QUERY									_T("查询")
#define DOWNLOAD_DEMO_QUERY									_T("查询")

#define DOWNLOAD_DEMO_LOGON_FIRST							_T("请先登录")
#define DOWNLOAD_DEMO_INPUT_TIME_SPACE						_T("请输入合理的时间范围")
#define DOWNLOAD_DEMO_PLEASE_CHOSE_ONE_FILE					_T("请选择一个文件!")
#define DOWNLOAD_DEMO_MAX_DOWNLOAD_FILE						_T("最多同时下载5个文件!")
#define DOWNLOAD_DEMO_NULL_FILE_NAME						_T("文件名为空")
#define DOWNLOAD_DEMO_MAIN_STREAM							_T("主码流")
#define DOWNLOAD_DEMO_SECONDARY_STREAM						_T("副码流")
#define DOWNLOAD_DEMO_LOG_TIME								_T("时间")
#define DOWNLOAD_DEMO_LOG_INFO								_T("日志信息")

#define DOWNLOAD_DEMO_LOGON_FIRST_EN						_T("Please Logon First!")
#define DOWNLOAD_DEMO_INPUT_TIME_SPACE_EN					_T("Please Input Time Space Correctly!")
#define DOWNLOAD_DEMO_PLEASE_CHOSE_ONE_FILE_EN				_T("Please Chose One File!")
#define DOWNLOAD_DEMO_NULL_FILE_NAME_EN						_T("File Name Is Empty!")
#define DOWNLOAD_DEMO_MAIN_STREAM_EN						_T("Major")
#define DOWNLOAD_DEMO_SECONDARY_STREAM_EN					_T("Minor")
#define DOWNLOAD_DEMO_LOG_TIME_EN							_T("Time")
#define DOWNLOAD_DEMO_LOG_INFO_EN							_T("LogInfo")


#define DOWNLOAD_DEMO_IP									_T("192.168.1.2")
#define DOWNLOAD_DEMO_PORT									_T("3000")
#define DOWNLOAD_DEMO_USERNAME								_T("Admin")
#define DOWNLOAD_DEMO_PASSWORD								_T("1111")
#define DOWNLOAD_DEMO_HIDE_PASSWORD							_T("*")
#define DOWNLOAD_DEMO_LOG_MSG_SUCCESS_LOGOFF				_T("Log : Logoff Success!")
#define DOWNLOAD_DEMO_LOG_MSG_SUCCESS_LOGON					_T("Log : Logon Success!")
#define DOWNLOAD_DEMO_LOG_MSG_FAILED_LOGON					_T("Err : Logon Failed!")
#define DOWNLOAD_DEMO_LOG_MSG_FAILED_SET_SPEED				_T("Err : Set Speed Failed!")
#define DOWNLOAD_DEMO_LOG_MSG_FAILED_PAUSE					_T("Err : Pause Failed!")
#define DOWNLOAD_DEMO_LOG_MSG_FAILED_SET_POS				_T("Err : Set Pos Failed!")
#define DOWNLOAD_DEMO_LOG_MSG_FINISH_DWONLOAD				_T("Log : Download Finish!")
#define DOWNLOAD_DEMO_LOG_MSG_DOWNLOAD_FALUT				_T("Err : Download Error!")
#define DOWNLOAD_DEMO_LOG_MSG_DOWNLOAD_INPURRT				_T("Err : DownLoad Break!")
#define DOWNLOAD_DEMO_LOG_MSG_DOWNLOAD_FAILED				_T("Err : Download Failed!")
#define DOWNLOAD_DEMO_LOG_MSG_START_DOWNLOAD				_T("Log : Start Download!")
#define DOWNLOAD_DEMO_LOG_MSG_NOT_QUERY_FILE				_T("Debug : Can Not Query File!")
#define DOWNLOAD_DEMO_LOG_MSG_PLAY_FAILED					_T("Err : File Play Failed!")
#define DOWNLOAD_DEMO_LOG_QUERY_FAILED						_T("Err : Query File Failed!")
#define DOWNLOAD_DEMO_LOG_TIME_DOWNLOAD_FIAILED				_T("Err : NetFileDownloadByTimeSpanEx Failed!")
#define DOWNLOAD_DEMO_LOG_STOP_PLAY_FIAILED					_T("Err : Stop Play Failed!")
#define DOWNLOAD_DEMO_LOG_SET_RAW_CALLBACK_FIAILED			_T("Err : Set Raw Frame Callback Failed!")
#define DOWNLOAD_DEMO_LOG_CAPTURE_FIAILED					_T("Err : Capture Picture Failed!")
#define DOWNLOAD_DEMO_LOG_CAPTURE_SUCCESS					_T("Success : Capture Picture!")
#define DOWNLOAD_DEMO_LOG_SET_VOLUME_SUCCESS				_T("Success : Set Volume!")
#define DOWNLOAD_DEMO_LOG_SET_VOLUME_FAILED					_T("Err : Set Volume Failed!")
#define DOWNLOAD_DEMO_LOG_STEP_FORWARD_FAILED				_T("Err : Step Forward Failed!")
#define DOWNLOAD_DEMO_LOG_STEP_FORWARD_SUCCESS				_T("Success : Step Forward!")
#define DOWNLOAD_DEMO_LOG_VOLUME_CTRL_FAILED				_T("Err : Volume Control Failed!")
#define DOWNLOAD_DEMO_LOG_VI_FRAME							_T("Rev : VI_FRAME!")
#define DOWNLOAD_DEMO_LOG_OTHER_TYPE						_T("Rev : Other Type!")
#define DOWNLOAD_DEMO_LOG_AUDIO_FRAME						_T("Rev : AUDIO_FRAME")

#endif //MACRODEFINE_H
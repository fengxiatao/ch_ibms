#ifndef NETCLIENTTYPES_H
#define NETCLIENTTYPES_H

#include "GlobalTypes.h"
#include "net_sdk_types.h"

#ifdef __WIN__
#else
#define __stdcall
#endif

/**************************************网络库NVSSDK工作模式详述，对应外部接口：NetClient_SetSDKWorkMode**************************************/
/*********Detailed description of nvssdk working mode of network library, corresponding external interface: NetClient_SetSDKWorkMode*********/ 

#define HEAVY_MODE			0	
//通用模式，可以设置和获取设备所有支持的参数。
//功能支持数据交互（实时流预览，录像下载，对讲和图片流等），支持所有参数设置和获取。
//In heavyweight mode, all supported parameters of the device can be obtained, but the memory consumption is large and the login speed is slow.
//The function supports data interaction (real-time flow and inside, video download, intercom and picture flow), and supports all parameter settings and acquisition. 

#define LIGHT_MODE			1
//废止的模式，SDK不再维护的模式，上层软件不能使用
//Obsolete mode, SDK no longer maintained mode, Upper software cannot be used.

#define EASYX_LIGHT_MODE	2
//废止的模式，SDK 5.2版本之后不再维护，上层软件不能使用
//Obsolete mode, no maintenance after SDK version 5.2, upper software cannot be used.

#define MOBILE_LIGHT_MODE	3
//移动端手机轻量级模式，登陆设备时长极短，可以很快完成设备登陆动作。
//功能只支持数据交互（实时流预览，录像下载，对讲和图片流），支持所有参数设置，不支持参数获取。
//Mobile phone lightweight mode, small memory occupation, the length of landing device, can quickly complete the device landing action. 
//The function only supports data interaction (real-time flow and there, video download, intercom and picture flow), All parameter settings are supported, and parameter acquisition is not supported. 

/**************************************网络库NVSSDK工作模式详述，对应外部接口：NetClient_SetSDKWorkMode**************************************/
/*********Detailed description of nvssdk working mode of network library, corresponding external interface: NetClient_SetSDKWorkMode*********/ 

#define TD_FALSE 0
#define TD_TRUE  1


#define MAX_TIMESEGMENT_TYPE 2
#define MAX_TIMESEGMENT_INDEX 8

#define TD_NULL                 0L
#define TD_NULL_PTR             TD_NULL

#define TD_SUCCESS              0

#define TD_FAILURE              (-1)

#define  TD_LITTLE_ENDIAN       1234      
#define  TD_BIG_ENDIAN          4321      

#define SET_EX_DEV_TYPE_NORMAL  0

#ifndef __cplusplus
#define bool int
#endif


#ifdef _MSC_VER
typedef __int64                 TD_S64;
typedef unsigned __int64        TD_U64;
#endif

#if defined __INTEL_COMPILER || defined __GNUC__
typedef long long               TD_S64;
typedef unsigned long long      TD_U64;
#endif


//old macro, no longer use
#define FLAG_THREE_STREAM			256				//Three streams --connect video
//end

#define IP_VERSION_4		0
#define IP_VERSION_6		1

#define MAX_LOCAL_CHANNEL_NUM		64	
#define MAX_DIGIT_CHANNEL_NUM		64
#define	MAX_CHANNEL_NUM				(MAX_LOCAL_CHANNEL_NUM + MAX_DIGIT_CHANNEL_NUM)	   //support 3511
#ifndef MAX_TOTAL_CHANNEL_NUM
#define MAX_TOTAL_CHANNEL_NUM		MAX_CHANNEL_NUM
#endif
#define MAX_VOLUME_CTRL_TYPE		5

#define MAX_CONNECTION				32
#define MAX_PORT_NUM				512
#define MAX_DHINFO_PARAM_LEN		1024
#define MAX_DOWNLOAD_NUM			5
#define MAX_COM_NUM					16		// The maximum number of serial ports
#define MAX_INTERTALK_NUM			1		// Maximum number of two-way intercom
//#define MAX_INNER_CHANNEL_NUM		32		// The maximum number of picture channels
#define MAX_CAPTURECHANNEL_NUM		5		// The maximum number of capture channels
#define MAX_TRACECHANNEL_NUM        1
#define MAX_TRANSPORTCHAN_NUM       1
#define MAX_COMMON_REAL_STREAM_NUM  1     

#define MAX_STREAM_TYPES			5		// The maximum number of codestreams
#define BASIC_STREAM_TYPES			2		// The number of primary and secondary codestream types
#define STREAM_TYPE_MAIN_TIMING		2
#define STREAM_TYPE_MAIN_EVENT		3
#define STREAM_TYPE_THREE			4
#define SNAPSHOT_STREAM				8		//SnapShot stream
#define MAX_EVENT_STREAM_TYPES		(MAX_STREAM_TYPES - BASIC_STREAM_TYPES)//Number of event stream types (including sub stream)

#define MIN_ITS_CHANNEL_NUM			5		// Traffic Camera Since channel 4 is used to set parameters for the picture stream channel, the channel parameter structure creates a minimum of 4 array sizes.

#define MAX_RECV_CHANNEL    		1024
#ifdef EMBEDED_ARM_USE
#define MAX_ENCODER         		4
#elif defined(IPHONE) || defined(ANDROID) || defined(HARMONY)
#define MAX_ENCODER					32
#else
#define MAX_ENCODER         		5000
#endif
#define LENGTH_RECV_ORDER   		1024

#define LENGTH_USER_NAME_BASE64		128
#define LENGTH_PASSWORD_BASE64		128
#define LENGTH_PLATFEATURE_BASE64   128

#define LENGTH_USER_NAME			16
#define LENGTH_PASSWORD				16
#define LENGTH_CHN_TITLE    		32
#define LENGTH_CHANNEL_TITLE		64
#define LENGTH_CHANNEL_TITLE_EX		LENGTH_CHANNEL_TITLE*(CHANNEL_TITLE_EXTEND_NUM+1)
#define LENGTH_DEVICE_NAME  		32

//max dev port
#define MAX_NET_PORT_NUM			65535

//connect mode
#define NETMODE_TCP     			1   	//Tcp connect
#define NETMODE_UDP     			2   	//Udp connect
#define NETMODE_MC      			3   	//Multicast connect
#define NVS_ACTIVE      			4   	//Positive Mode
#define NETMODE_P2P					5   	//P2P connect
#define RTP_OVER_TCP				6		//rtsp stream via RTP-over-TCP
#define RTP_OVER_UDP				7		//rtsp stream via RTP-over-UDP
#define RTP_OVER_MCAST				8		//rtsp stream via RTP-over-Multicast
#define SRTP_OVER_UDP				9		//rtsps stream via SRTP-over-UDP
#define SRTP_OVER_MCAST				10		//rtsps stream via SRTP-over-Multicast
#define OPENSSL_TCPNET				11		//use openssl net

// Video flip
#define VZ_QCIF_REVERSE				(0x100000+QCIF)
#define VZ_HCIF_REVERSE				(0x100000+HCIF)
#define VZ_FCIF_REVERSE				(0x100000+FCIF)
#define VZ_HD1_REVERSE				(0x100000+HD1)
#define VZ_FD1_REVERSE				(0x100000+FD1)
#define VZ_MD1_REVERSE				(0x100000+MD1)
#define VZ_QVGA_REVERSE				(0x100000+QVGA)
#define VZ_VGA_REVERSE				(0x100000+VGA)
#define VZ_HVGA_REVERSE				(0x100000+HVGA)
#define VZ_HD_720P_REVERSE			(0x100000+HD_720P)
#define VZ_HD_960P_REVERSE			(0x100000+HD_960P)
#define VZ_HD_200W_REVERSE			(0x100000+HD_200W)
#define VZ_HD_1080P_REVERSE			(0x100000+HD_1080P)
#define VZ_HD_QXGA_REVERSE			(0x100000+HD_QXGA)
#define VZ_QHD_REVERSE				(0x100000+QHD)
#define VZ_VZ_960H_REVERSE			(0x100000+VZ_960H)
#define VZ_VZ_5MA_REVERSE			(0x100000+VZ_5MA)
#define VZ_VZ_5M_REVERSE			(0x100000+VZ_5M)
#define VZ_VZ_5MB_REVERSE			(0x100000+VZ_5MB)
#define VZ_VZ_5MC_REVERSE			(0x100000+VZ_5MC)
#define VZ_VZ_5MD_REVERSE			(0x100000+VZ_5MD)

//Stream type
#define MIN_STREAM_TYPE				0
#define MAIN_STREAM   				0				//Basic stream(hight quality)
#define SUB_STREAM    				1				//Extended stream(low bitrate)
#define CHANNEL_INTERTALK			2				//two-way intercom
#define CHANNEL_DOWNLOAD			3				//file download
#define STREAM_DOWNLOAD				CHANNEL_DOWNLOAD
#define CHANNEL_PICTURE				4				//capture pictures  
#define CHANNEL_SIMUCAP  			5				//analog capture flow
#define CHANNEL_TRACK				6				//track flow
#define CHANNEL_AUDIO				7				//Pure Audio
#define COMMON_REAL_STREAM			8				//Common real-time streaming data
#define MAX_STREAM_TYPE				9


#define CHANNEL_THREE_STREAM		254				//Three streams-get and set param

#define DOWNLOAD_CHANNEL_TAG   		(250)
#define DISTINGUISH_FILE_CHAN		64

#define MAX_AUDIO_CODER_NUM			20

#define LAN_VIDEO MAIN_STREAM		//for old compatible
#define WAN_VIDEO SUB_STREAM    

#ifdef EMBEDED_ARM_USE
#define MAX_RECV_NUM				8
#elif defined(IPHONE) || defined(ANDROID) || defined(HARMONY)
#define MAX_RECV_NUM				32
#else 
#define MAX_RECV_NUM				10000
#endif
typedef struct tagRecvInfo
{
	int		iSize;
	int		iIndex;
	int		iLogonId;
	int		iChanNo;
	int		iStreamNo;
	int		iDrawId;
	int		iFlag;
	int		iNetMode;
}RecvInfo, *pRecvInfo;

//Device type
#define NVS_T       				0       //T serials NVS
#define NVS_S       				1       //S serials NVS
#define NVS_TPLUS   				2       //T+ serials NVS
#define NVR         				12      //NVR
//end

//Product Type
#define Reserved_PRODUCT		0
#define IPCamera_PRODUCT		1
#define NVRecord_PRODUCT		2

//Encode type
#define H263								11
#define H264								21
#define MP4									31
#define MJPEG								41
#define MODE_H265							23
#define SVAC								99

//	SendStringToServer
#define SEND_STRING_CMD						0
#define SEND_STRING_DATA					1
#define SEND_UTF8_STRING					2
#define SEND_XML_STRING						3 

//H264 Decod mode
#define H264DEC_DECTWO						0		//decode 2 field
#define H264DEC_DECTOP						1		//decode top field （low CPU cost）

//Message define
#define WCM_SDK_MSG_MIN						0
#define WCM_RECVMESSAGE						1		//Received string
#define WCM_ERR_ORDER						2		//Occur error in command socket
#define WCM_ERR_DATANET						3		//Occur error in data socket
#define WCM_TIMEOUT_DATANET					4		//Process stream timeout
#define WCM_ERR_PLAYER						5		//Occur error in player
#define WCM_ERR_USER						6		//User define error
#define WCM_LOGON_NOTIFY					7		//Logon notify
#define WCM_VIDEO_HEAD						8		//Received video head
#define WCM_VIDEO_DISCONNECT				9		//the connection were disconnect by mediacenter
#define WCM_CLIENT_CONNECT					10		//There a client connected to Mediacenter
#define WCM_CLIENT_DISCONNECT				11		//A client disconnected from Mediacenter
#define WCM_CLIENT_LOGON					12		//A client logon/logogg the Mediacenter
#define WCM_RECORD_ERR						13		//Occur when recording file
#define WCM_DSM_REGERR						14		//Error on proxy regist to Directory server
#define WCM_DSM_ENCODERADD					15		//A NVS regist on the proxy
#define WCM_DSM_ENCODERDEL					16		//A NVS's regist connection disconnected.
#define WCM_LOGFILE_FINISHED				17		//When logfile download finished
#define WCM_QUERYFILE_FINISHED				18		//Query finished record files
#define WCM_DWONLOAD_FINISHED				19		//Download finished
#define WCM_DWONLOAD_FAULT					20		//Download fault
#define WCM_REBUILDINDEX					22		//Finished of rebuild index file
#define WCM_TALK							23		//Begin to talk
#define WCM_DISK_FORMAT_PROGRESS			24
#define WCM_DISK_FORMAT_ERROR				25
#define WCM_DISK_PART_ERROR					26
#define	WCM_BUCKUP_KERNEL					27
#define	WCM_LOCALSTORE_PROGRESS				28
#define WCM_DOWNLOAD_INTERRUPT				29		//Download interrupt    
#define WCM_QUERYLOG_FINISHED				30		//Query finished log
#define WCM_INTERTALK_READY					31		//Intertalk is ready for now ...
#define WCM_CONTROL_DEVICE_RECORD			32		//Control device record (start, stop, package)
#define WCM_RECORD_OVER_MAX_LIMIT			33		//The size of record file is too big, and we should start to capture a new file.
#define WCM_ERR_DATANET_MAX_COUNT			34		//Max DataNet
#define WCM_CDBURN_STATUS					35
#define WCM_NEED_DECRYPT_KEY				36		//Need decrypt key for video
#define WCM_DECRYPT_KEY_FAILED				37		//decrypt key is not matched
#define WCM_DECRYPT_SUCCESS					38		//decrypt success
#define WCM_ERR_DIGITCHANNEL_NOT_ENABLED	39		//The digital channel is not enabled
#define WCM_ERR_DIGITCHANNEL_NOT_CONNECTED	40		//The digital channel is not connected
#define WCM_ENCRYPT_CLEARED					41		//clear encrypt
#define WCM_FTP_UPDATE_STATUS				42		//FTP_UPDATE_STATUS
#define	WCM_BUCKUP_IMAGE					43
#define WCM_REBUILDINDEX_PROGRESS			44		//rebuild index file progress
#define	WCM_ILLEGAL_RECORD					45		//record notify when there is illegal patking
#define WCM_QUERY_ATMFILE_FINISHED			46		//Query ATM record files finished
#define WCM_AUTOTEST_INFO					47		// Automated Debugging @ 130109
#define WCM_LASTERROR_INFO					48		// The error code returned by the device
#define WCM_LOCALSTORE_LOCK_FILE			49		// file plus unlock
#define WCM_DISK_QUOTA						50		// Disk quotas
#define WCM_CONNECT_INFO					51		// Connection information
#define WCM_DOWNLOAD_VOD_TRANS_FAILED		52		// Vod Transform Failed
#define WCM_AUDIO_HEAD						52		// AUDIO HEAD
#define WCM_USER_CHANGE						100		// User information changed
#define WCM_UNSUPPORT_STREAM				101		// The device does not support this codestream
#define WCM_BROADCAST_MSG					102		// General broadcast messages
#define WCM_VCA_SUSPEND						103		// Suspends the intelligent analysis notification message
#define WCM_ITS_ILLEGAL_RECORD				104		// Illegal recording operation status
#define WCM_BACKUP_SEEKWORKDEV				105		// Multicast Search Machine Search Result Notification Message
#define WCM_IPSAN_DISCOVERY					108		// IPSAN device discovery
#define WCM_RAID_STATUS						109		// Array status messages
#define WCM_HDD_STATUS						110		// Physical disk status messages
#define WCM_VIRTUALDISK_STATUS				111		// Virtual disk status messages
#define WCM_HOTBACK_DEV_STATUS				112		// Work machine broadcasts hot standby status
#define WCM_WORD_DEV_STATUS					113		// The standby machine broadcasts the status of the working machine
#define WCM_ALARM_INFORMATION				114		// Dynamic host data in real time
#define WCM_ALARM_OUT_STATUS				115		// Alarm output port status
#define WCM_ALARM_SCHEDULE_STATUS			116		// Arming state
#define WCM_EXPORT_CONFIG_FINISHED			117		// The export configuration is complete
#define WCM_ALM_OUTPORT_SET					118		// Set the state of the alarm output port (open or closed)
#define WCM_VOD_PLAY_FINISHED				119		// The vod is finished playing
#define WCM_VOD_PLAY_INTERRUPT				120		// The vod plays the interrupt
#define WCM_PSECH_STATE						121		//pse channel state
#define WCM_EMAIL_TEST_RESULT				122		//Email test result
#define WCM_DISK_SMART_INFO					123
#define WCM_ITS_TRAFFICFLOWREPORT			124
#define WCM_CHANNEL_TALK					125
#define WCM_GET_CREATEFREEVO				126
#define WCM_USER_MODIFYPSW					127		// Change the password
#define WCM_DISK_OPERATION					128		// Disk operations
#define WCM_DOWNLOAD_SUCCESS				129
#define WCM_DOWNLOAD_FAULT					130
#define WCM_GIGITAL_CHN_BATCH				131		//digital channel batch
#define WCM_SDK_MSG_132						132
#define WCM_SDK_MSG_133						133
#define WCM_PREVIEWREC_DEVAMPLITUDE			134
#define WCM_QUERYFILE_FAULT					135		//Query fault record files
#define WCM_QUERY_REPORT_FORM				136		//Query report form
#define WCM_START_PLAY						137		//inner StartPlay
#define WCM_NEED_DECRYPT_KEY_PLAYBACK		138		//LPARAM_NEED_DECRYPT_KEY
#define WCM_DECRYPT_KEY_FAILED_PLAYBACK		139		//LPARAM_DECRYPT_KEY_FAILED
#define WCM_DECRYPT_SUCCESS_PLAYBACK		140		//LPARAM_DECRYPT_SUCCESS
#define WCM_VOD_STREAM_END					141		//Vod stream end
#define WCM_HTTP_TEST_CMD					142		//http test cmd
#define WCM_HOTBACKUP_REQUESTSYNC			143		//hot banckup device request sync
#define WCM_HOTBACKUP_RECORDFILE			144		//hot banckup device record file info
#define WCM_HOTBACKUP_SYNCFINISH			145		//hot banckup device sync file finish
#define WCM_CMD_BATTERYINFO					146		//Battery Info
#define WCM_SDK_MSG_147						147
#define WCM_ITS_LOOPTRIGSTATE				148		//its loop state
#define WCM_ITS_QUERY_DATA					149		//ITS query data
#define WCM_ITS_MODIFYDATA					150		//ITS modify data
#define WCM_ITS_DELETEDATA					151		//ITS delete data
#define WCM_ITS_QUERY_TOTALCOUNT			152		//ITS query total count
#define WCM_ITS_DATARESET					153		//ITS data reset
#define WCM_ITS_GETDATA						154		//ITS get data
#define WCM_SEARCH_EXDEV					155		//search exter device
#define WCM_GET_EXDEVLIST					156		//get exter device list
#define WCM_GET_RECORDINGAUDIOLIST			157		//get reording audio list
#define WCM_SET_TRACKING_RATE				158		//set tracking rate
#define WCM_PWD_VERIFY						159		//password verify
#define WCM_SEEK_NVSS						160		//seek nvss info
#define WCM_FILE_MAP						161		//file map
#define WCM_PLATE_LIST_QUERY				162		//plate list query
#define WCM_PLATE_LIST_MODIFY				163		//plate list modify
#define WCM_USER_ADD						164		//add user
#define WCM_USER_DELETE						165		//delete user
#define WCM_BADBCLOCK_SIZE					166		//disk bad block size
#define WCM_BADBCLOCK_TEST					167		//disk bad block test
#define WCM_BADBCLOCK_TEST_STATE			168		//disk bad block test state
#define WCM_BADBCLOCK_STATE					169		//disk bad block block state
#define WCM_BADBCLOCK_ACTION				170		//disk bad block action
#define WCM_CURRENT_SCENE					171		//current scene change
#define WCM_FILE_INPUT						172		//file input
#define WCM_FILE_OUTPUT						173		//file output
#define WCM_FILE_CONVERT					174		//file convert
#define WCM_PORT_MAP						175		
#define WCM_QUERY_AUTOTEST_RESULT			176		
#define WCM_FTP_DOWNLOAD_VIDEO				177		//recv ftp download video reply
#define WCM_GET_DEV_MAC						178		//get dev mac
#define WCM_HU_TEM_VALUE					179		
#define WCM_CALIBRATE_STATUS				180	
#define WCM_VIDEO_HEAD_EX					181	
#define WCM_AUTOTEST_INFO_EX				182		//Autotest@170512
#define WCM_NEED_DECRYPT_KEY_EX				183		//Need decrypt key for video
#define WCM_DECRYPT_KEY_FAILED_EX			184		//decrypt key is not matched
#define WCM_DECRYPT_SUCCESS_EX				185		//decrypt success
#define WCM_PROXY_DELETE_DEV				186		//for proxy,when user logoff,nvssdk send this msg to proxy
#define WCM_VCAFPGA_QUERYINFO				187
#define WCM_CCM_CALIBRATE					188   
#define WCM_DDNS_TEST						189
#define WCM_CLOUD_DETECT					190
#define WCM_CLOUD_UPGRADE					191
#define WCM_CLOUD_DOWNLOAD					192    
#define WCM_CLOUD_UPGRADEPRO				193
#define WCM_USER_FINDPWD					194
#define WCM_USER_SECURITYCODE				195
#define WCM_USER_RESERVEPHONE				196
#define WCM_ITS_FOCUS						197
#define WCM_FACE_MODEING					198
#define WCM_SDK_MSG_199						199
#define WCM_SDK_MSG_200						200
#define WCM_FACE_SEARCH						201
#define WCM_DETECT_AREA                     202
#define WCM_DETECT_AREA_FINISH              203
#define WCM_ERR_CONNECTION_MISMATCH			204		//The requested connection does not match the service.
#define WCM_IPDWARNINGMSG					205		//Illegal linkage of SMS content
#define WCM_SELFTEST						206
#define WCM_REUPLOAD                        207
#define WCM_NET_TEST                        208
#define WCM_ITS_LATESTBKPLAT                209
#define WCM_ITS_ALARMINFO                   210
#define WCM_VIRTUAL_GAUGELIB                211
#define WCM_DETECT_AREAXPOS                 212
#define WCM_SCENE_HEIGHT                    213
#define WCM_CALL_SCENE                      214
#define WCM_WATER_FLOW_SPEED                215
#define WCM_MODIFY_FROPSD                   216
#define WCM_SHDB_APPREPAIRSYS				217
#define WCM_SHDB_SERVICETYPE				218
#define WCM_SHDB_TESTMAINTAIN				219
#define WCM_SHDB_CHECKMANAGE				220
#define WCM_CLOUD_AUTODETECT				221
#define WCM_EASYDDNS_LINKSTATE				222
#define WCM_NORTH_ANGLE						223
#define WCM_ITS_PARKCARNUM					224
#define WCM_WATER_SPEED_FIELD				225
#define WCM_AUTOTESTMULT      				226
#define WCM_RADAR_STATUSINFO      			227
#define WCM_ERR_CHANNEL_NOTCONNECT          228
#define WCM_VISUAL_RANGE		  			229
#define WCM_VIRTUALGAUGEDIS		  			230
#define WCM_TO_DEFAULT_PARAM				231
#define WCM_WATER_SPEED_UPDATE				232
#define WCM_CALIBRATE_CHECK		            233		                        //Set/Get Calibrate check result
#define WCM_WIRELESS_SILENT		            234		                        //Set/Get Wireless Silent
#define WCM_ALI_GETSECRET                   235
#define WCM_ALI_SETSECRET                   236
#define WCM_ENTRANCEGUARD_INFO              237
#define WCM_TALK_REQUEST                    238
#define WCM_TALK_STOP                       239
#define WCM_WATERDETECT_POS                 240
#define WCM_VCAOFFLINE_QUERYDATA            241		//回复查询人数统计离线数据
#define WCM_DOWNLOAD_COMMON_MSG				242		//录像下载通用主消息
//设备返回码_iWparam高16位值含义：4-定位失败 5-转码资源不足 6-带宽不足 7-用户权限不足 8-参数错误 9-打开录像文件失败 10-非法数据帧
#define WCM_DOWNLOAD_FORCESTOPRECV			243		//录像下载长时间没数据强制断开消息
#define WCM_CHECKFILE_PROGRESS				244		//文件完整性校验进度通用主消息
#define WCM_PTZ_CTRL_RESULT					245		//GDDZ定制专用 云台控制结果 MainMsgCallBackResult成员说明：iChannelNo-设备通道号，iType-详见PTZ命令类型
#define WCM_BITRATE_RESULT					246		//GDDZ定制专用 码率结果 MainMsgCallBackResult成员说明：iChannelNo-设备通道号，iType-未使用
#define WCM_VIDEOSIZE_RESULT				247		//GDDZ定制专用 分辨率结果 MainMsgCallBackResult成员说明：iChannelNo-设备通道号，iType-未使用
#define WCM_ENCODETYPE_RESULT				248		//GDDZ定制专用 压缩方式结果 MainMsgCallBackResult成员说明：iChannelNo-设备通道号，iType-未使用
#define WCM_CLASSIFICATION_RESULT			249		//GDDZ定制专用 设置密级结果 MainMsgCallBackResult成员说明：iChannelNo-设备通道号，iType-未使用
#define WCM_AUTH_NOTIFY						250		//GDDZ定制专用 认证状态
#define WCM_CHECKVOD_PROGRESS				251		//VOD文件完整性校验进度通用主消息
#define WCM_DZ_TRANSPARENT					252		//获取定制透传参数异步主消息，携带参数结构体DzTransparentPara是栈上的变量，不可用于消息参数传递
#define WCM_CMDID_NO_VALUE					253		//在收到LOGON*FINISH时，CMDID未赋值
#define WCM_SHORT_ERR_DATANET				254		//数据通道6秒收不到数据开始重连同时回调消息
#define WCM_IP_ADDR_RESULT					255		//GDDZ定制专用 修改IP参数结果上报消息
#define WCM_IPV4_NETCARD_RESULT				256		//GDDZ定制专用 修改IPV4网卡参数结果上报消息
#define WCM_IPV6_NETCARD_RESULT				257		//GDDZ定制专用 修改IPV6网卡参数结果上报消息
#define WCM_GDDZ_FRAMERATE_RESULT			258		//GDDZ定制专用 修改视频帧率结果上报消息
#define WCM_ERR_DEV_NOTACTIVATED		    259		//设备未激活（ZXYHDZ）
#define WCM_ERR_DEC_ABILITY_UPPER_LIMIT     260		//请求压缩流失败，解码能力上限（ZXYHDZ）
#define WCM_ERR_DUAL_USERS_AUTH_FAILED      261		//双用户认证失败（ZXYHDZ）
#define WCM_RECORD_SUPPLEMENT_PROGRESS      262		//设备定时上报回补录像文件进度异步消息
#define WCM_ENCRYPT_TO_NOTENCRPT			263     //从加密状态到未加密状态，上层根据这个消息隐藏解密框
#define WCM_ENCRYPT_TO_NOTENCRPT_EX			264     //从加密状态到未加密状态，上层根据这个消息隐藏解密框
#define WCM_ENCRYPT_TO_NOTENCRPT_PLAYBACK	265		//从加密状态到未加密状态，上层根据这个消息隐藏解密框
#define WCM_CDK_GETCKDSECRE                 266
#define WCM_CDK_SETCKDSECRE                 267
#define WCM_ACTIVEMODE_DATALINK_CREATE_ERR  268		//主动模式（设备到服务器的）数据连接创建失败
#define WCM_SDK_MSG_MAX						269

#define WCM_FOCUSSTATE_RUNSTATE				469		// 提前占位

//Number of Show Video
//1：通道名称（文本） 2：时间日期 3:logo颜色 4：附加字符 5:交通专用 6：交通合成图片 7：人脸叠加OSD
#define MAX_OSDTYPE_NUM						10
#define MAX_OSD_REALTIME_NUM				16		//ring ring host use

//User 
#define AUT_BROWSE							1		//Only can preview video
#define AUT_BROWSE_CTRL						2		//Can preview and control device
#define AUT_BROWSE_CTRL_SET					3		//Preview, control device, modify settings
#define AUT_ADMIN							4		//Super user, No.0 user is super user,
													//User name is Admin, which can not be modified,
													//Can add, delete, modify user's authority

typedef int	ALARMTYPE;

//Logon state
#define LOGON_DSMING						3		//DSM（Directory Sevices Mode）connecting
#define LOGON_RETRY							2		//Retry
#define LOGON_SUCCESS						0		//Logon successfully
#define LOGON_ING							1		//Being logon
#define LOGON_FAILED						4		//Fail to logon
#define LOGON_TIMEOUT						5		//Logon timeout
#define NOT_LOGON							6		//Not logon
#define LOGON_DSMFAILED						7		//DSM connection failure
#define LOGON_DSMTIMEOUT					8		//DSM connection timeout
#define LOGON_SUCCESS_LIGHT					9		//Light Logon successfully
#define LOGON_OPERATE_TIME_OUT				10		//operate time out
#define LOGON_TOKEN_INVALID					11		//token is invalid



//Player state

#define PLAYER_STOP							0		//Not require to play
#define PLAYER_WAITTING						0x01	//Require to play but not play, waiting for data
#define PLAYER_PLAYING						0x02	//Playing
#define PLAYER_CARDWAITTING					0x04	//Require to hardware decode
#define PLAYER_CARDPLAYING					0x08	//Being hardware decode output

typedef int VIDEO_NORM;
#define VIDEO_MODE_PAL						0x00
#define VIDEO_MODE_NTSC						0x01
#define VIDEO_MODE_AUTO						0x02

#define BRIGHTNESS							0
#define CONTRAST							1
#define SATURATION							2	
#define HUE									3

#define PRE_VDOQUALITY						0
#define PRE_FRAMERATE						2

#define NO_STREAM							0
#define ONLY_VIDEO							1
#define ONLY_AUDIO							2
#define VIDEO_AUDIO							3

#define DISABLE								0
#define ENABLE								1

#define LOW									0
#define HIGH								1

#define TALK_BEGIN_OK						0
#define TALK_BEGIN_ERROR					1
#define TALK_ERROR							2
#define TALK_END_OK							3

#define TALK_STATE_NO_TALK					0 	// No intercom
#define TALK_STATE_ASK_BEGIN				1 	// Have requested to start speaking
#define TALK_STATE_TALKING					2 	// Intercom is in progress
#define TALK_STATE_ASK_STOP					3 	// The intercom has been requested to stop
#define TALK_STATE_RESUME					4 	// 2010-4-26-16: 21 @ yys @, is automatically resuming intercom process

#define  PRO_UPGRADE_OK						0
#define  PRO_UPGRADE_ERROR					-1
#define  PRO_UPGRADE_READY					-2

#define WEB_UPGRADE_OK						0
#define WEB_UPGRADE_ERROR					-1
#define WEB_UPGRADE_READY					-2
// Compatible with IE
#define WEB_UPGRADE_REDAY WEB_UPGRADE_READY

// Video parameter and device definition
typedef int	PARATYPE;
#define PARA_SDK_MSG_MIN						0
#define PARA_VIDOEPARA							0
#define PARA_VIDEOPARA							0 
#define PARA_VIDEOQUALITY						1     
#define PARA_FRAMERATE							2     
#define PARA_IFRAMERATE							3     
#define PARA_STREAMTYPE							4     
#define PARA_MOTIONDETECTAREA					5
#define PARA_THRESHOLD          				6    
#define PARA_MOTIONDETECT						7     
#define PARA_OSDTEXT							8    
#define PARA_OSDTYPE							9     
#define PARA_ALMINMODE							10    
#define PARA_ALMOUTMODE							11    
#define PARA_ALARMINPUT							12    
#define PARA_ALARMOUTPUT						13    
#define PARA_AlMVDOCOVTHRESHOLD					14    
#define PARA_ALMVIDEOCOV						15    
#define PARA_RECORDCOVERAREA					16    
#define PARA_RECORDCOVER						17    
#define PARA_ALMVDOLOST							18    
#define PARA_DEVICETYPE							19    
#define PARA_NEWIP								20
#define PARA_AUDIOCHN							21    
#define PARA_IPFILTER							22    
#define PARA_COVERAREA							23    
#define PARA_VIDEOHEADER						24
#define PARA_VIDEOSIZE							25    
#define PARA_BITRATE							26    
#define PARA_DATACHANGED						27
#define PARA_PARSECHANGED						28   
#define PARA_APPIFRAMERATE						29    
#define PARA_APPFRAMERATE						30    
#define PARA_APPVIDEOQUALITY					31    
#define PARA_APPVIDEOSIZE						32    
#define PARA_APPSTREAMTYPE						33    
#define PARA_APPBITRATE							34    
#define PARA_APPCOVERAREA						35    
#define PARA_APPVIDEOHEADER						36    
#define PARA_REDUCENOISE						37    
#define PARA_BOTHSTREAMSAME						38    
#define PARA_PPPOE								39    
#define PARA_ENCODETYPE							40    
#define PARA_PREFERMODE							41    
#define PARA_LOGFILE							42    
#define PARA_SETLOGOOK							43    
#define PARA_STORAGE_SCHEDULE   				44   
#define PARA_STORAGE_TASKSTATE					45    
#define PARA_STORAGE_ALARMSTATE					46    
#define PARA_STORAGE_PRERECORD					47
//
#define PARA_STORAGE_RECORDRULE					49    
#define PARA_STORAGE_EXTNAME					50    
#define PARA_STORAGE_MAPDEVICE					51    
#define PARA_STORAGE_MOUNTUSB					52    
#define PARA_STORAGE_DISKSTATE					53    
#define PARA_AUDIOCODER							54    
#define PARA_NPMODE								55    
#define PARA_STORAGE_RECORDSTATE				56    
#define PARA_PU_MANAGERSVR						57    
#define PARA_PU_DEVICEID						58    
#define PARA_STREAMCLIENT						59    
#define PARA_DATEFORMAT							60    
#define PARA_HTTPPORT							61    
#define PARA_CMOS_SCENE							62    
#define PARA_SMTP_INTO							63    
#define PARA_SMTP_ENABLE						64    
#define PARA_SENSORFLIP							65    
#define PRAM_WIFI_PARAM							66
#define PARA_WIFI_SEARCH_RESULT					67   
#define PARA_PRIVACYPROTECT						68
#define PARA_NTP								69
#define PARA_ALARTHRESHOLD						70
#define PARA_OSDALPHA							71
#define PARA_WORDOVERLAY						72
#define PARA_ALARMSERVER						73
#define PARA_DHCP_BACK							74
#define PARA_WIFIDHCP							75
#define PARA_DIAPHANEITY						76
#define PARA_PTZ_PROTOCOL						77
#define PARA_3D									78
#define PARA_DISK_USAGE							79
#define PARA_VIDEOPARA_SCHEDULE					80
#define PARA_ALARM_INPORT						81
#define PARA_ALARM_OUT							82
#define PARA_INPORT_SCHEDULE					83
#define PARA_OUTPORT_SCHEDULE					84
#define PARA_MOTION_SCHEDULE					85
#define PARA_VDOLOST_SCHEDULE					86
#define PARA_VDOLOST_LINKREC					87
#define PARA_MOTIONDEC_LINKREC					88
#define PARA_ALMINPORT_LINKREC					89
#define PARA_VDOLOST_LINKSNAP					90
#define PARA_MOTIONDEC_LINKSNAP					91
#define PARA_ALMINPORT_LINKSNAP					92
#define PARA_VDOLOST_LINKPTZ					93
#define PARA_ALMINPORT_LINKPTZ					94
#define PARA_VDOLOST_LINKSNDDIS					95
#define PARA_MOTIONDEC_LNKSNDDIS				96
#define PARA_ALMINPORT_LNKSNDDIS				97
#define PARA_ALMINPORT_LNKOUT					98
#define PARA_VDOLOST_LNKOUT						99
#define PARA_MOTIONDEC_LNKOUT					100
#define PARA_OUTPORT_DELAY						101
#define PARA_UPNP_ENABLE						102
#define PARA_SYSINFO_RESULT						103
#define PARA_DDNSCHANGED 						104	   
#define PARA_FUNCLIST							105	
#define PARA_OUTPORT_STATE						106
#define PARA_ZTEINFO							107
#define PARA_ONLINESTATE						108
#define PARA_DZINFO								109
#define PARA_COMSERVER							110
#define PARA_VENCTYPE							111			// The video encoding changes the main stream
#define PARA_PTZAUTOPBACK						112
#define PARA_3GDEVICESTATUS						113
#define PARA_3GDIALOG							114
#define PARA_3GMESSAGE							115
#define PARA_HDCAMER							116
#define PARA_HXLISTENPORTINFO					117
#define PARA_3GTASKSCHEDULE						118
#define PARA_3GNOTIFY							119
#define PARA_COLORPARA							120
#define PARA_EXCEPTION							121
#define PARA_CAHNNELNAME						122
#define PARA_CUSTOMRECTYPE						123
#define PARA_FTP_UPDATE							124
#define PARA_FTP_UPDATE_STATUS					125
#define PARA_CHNPTZFORMAT						126
#define PARA_3GVPDN								127
#define PARA_VIDEOCOVER_SCHEDULE				128
#define PARA_CHNPTZCRUISE						129
#define PARA_VDOCOVER_LINKPTZ					130
#define PARA_VCA_CHANNEL						131
#define PARA_VCA_VIDEOSIZE						132
#define PARA_VCA_ADVANCED						133
#define PARA_VCA_TARGET							134
#define PARA_VCA_ALARMSTAT						135
#define PARA_VCA_RULESET						136
#define PARA_VCA_RULEEVENT0						137
#define PARA_VCA_RULEEVENT1						138
#define PARA_VCA_RULEEVENT2						139
#define PARA_VCA_ALARMSCHEDULE					140
#define PARA_VCA_SCHEDULEENABLE					141
#define PARA_VCA_ALARMLINK						142
#define PARA_FTP_SNAPSHOT						143
#define PARA_FTP_LINKSEND						144
#define PARA_FTP_TIMEDSEND						145
#define PARA_DVR3G_POWERDELAY					146
#define PARA_DVR3G_SIMNUM						147
#define PARA_DVR3G_GPSINFO						148
#define PARA_DVR3G_GPSFILTER					149
#define PARA_SDK_MSG_150						150
#define PARA_VIDEOENCRYPT						151
#define PARA_PREIVEWREOCRD						152
#define PARA_DIGITALCHANNEL						153
#define PARA_NPMODE_EX							154
#define PARA_SIP_VIDEOCHANNEL 					155 	// SIP video channel settings
#define PARA_SIP_ALARMCHANNEL 					156 	// SIP alarm channel settings
#define PARA_VIDEOCOMBINE						157		// Multi-screen composition
#define PARA_PLATFORM_RUN						158		//PlatForm Run
#define PARA_ITS_CHNLENABLE						159     //
#define PARA_ITS_CHNLLOOP						160     //
#define PARA_ITS_CHNLTIME						161     //
#define PARA_ITS_CHNLSPEED						162     //
#define PARA_ITS_CHNLRECO						163     //
#define PARA_ITS_CHNLVLOOP						164     //
#define PARA_ITS_LPOPTIM_FIRSTHZ				165     //
#define PARA_ITS_LPOPTIM_NPENABLE				166     //
#define PARA_ITS_LPOPTIM_OTHER					167     //
#define PARA_ITS_TIMERANGE						168 	// Camera time period setting
#define PARA_ITS_TIMERANGEPARAM					169 	// Time period parameter setting
#define PARA_ITS_TIMERANGE_AGCBLOCK				170 	// Time Metering Area is enabled
#define PARA_ITS_TIMERANGE_FLASHLAMP			171 	// The time period flash is enabled
#define PARA_ITS_DETECTMODE						172 	// Camera detection mode setting
#define PARA_ITS_BLOCK							173 	// Camera metering area setting
#define PARA_ITS_LOOPMODE						174 	// Coil operating mode
#define PARA_ITS_DEVICETYPE						175 	// Camera peripheral type
#define PARA_ITS_SWITCHTIME						176     //set the video and external trigger mode switching time and set the time delay capture peripheral (mainly used for radar)
#define PARA_JPEGQUALITY						177 	// Set the JPEG picture quality
#define PARA_VCA_RULEEVENT9						178 	// Intelligent analysis of face recognition parameters
#define PARA_VCA_RULEEVENT10					179 	// Intelligent analysis of video diagnostic parameters
#define PARA_ITS_TEMPLATENAME					180 	// HD camera template name setting
#define PARA_ITS_RECOPARAM						181 	// Traffic - Sets the parameters of the recognition algorithm
#define PARA_ITS_DAYNIGHT						182 	// Traffic - Day and night start time settings
#define PARA_ITS_CAMLOCATION					183 	// Traffic - camera position
#define PARA_ITS_WORKMODE						184 	// Traffic - Camera mode
#define PARA_TIMEZONE_SET						185 	// Set the time zone
#define PARA_LANGUAGE_SET						186 	// Set the language
#define PARA_CHANNELENCODEPROFILE_SET			187 	// Channel encoding
#define PARA_EXCEPTION_HANDLE					188 	// Exception handling
#define PARA_ITS_CHNLIGHT						189 	// Lights corresponding to the lane
#define PARA_ITS_CAPTURE						190		//Lane corresponding to the capture position
#define PARA_ITS_RECOGNIZE						191 	// Whether the recognition function is enabled
#define PARA_IMG_DISPOSAL						192 	// Picture effect processing function
#define PARA_WATERMARK							193 	// Whether to enable the watermark function
#define PARA_SYSTEM_TYPE						194 	// System type
#define PARA_ITS_LIGHT_INFO						195 	// The corresponding parameter of the semaphore
#define PARA_CHECKBADPIXEL_STATUS				196 	// Bad spot detection state
#define PARA_DEVICE_STATE						197 	// Device real-time status
#define PARA_CHECKIRIS_STATUS					198 	// Aperture Detection Status
#define PARA_ITS_ILLEGALPARK					199     //set illegal parking parameters
#define PARA_ITS_ILLEGALPARKPARAM				200		//set illegal parking calibration area parameters
#define PARA_LAN_IPV4							201 	// The network card IPv4 address
#define PARA_LAN_IPV6							202 	// Network card IPv6 address
#define PARA_LAN_WORKMODE						203 	// Network working mode
#define PARA_PWM_STATUS							204 	// Infrared lamp PWM control value
#define PARA_AUDIOMIX							205 	// Mixing synthesis parameters
#define PARA_AUDIOLOST_ALARMSCHEDULE			206 	// Audio loss alarm parameter template
#define PARA_AUDIOLOST_ALARMLINK				207 	// Audio loss alarm parameter template
#define PARA_AUDIOLOST_ALARMSCHENABLE			208 	// Audio loss alarm parameter template
#define PARA_ITS_SWITCH_INTERVAL				209 	// Off-camera triggering video-only automatic switching interval
#define PARA_ITS_VIDEODETECT					210 	// Smart Camera Image Detection
#define PARA_ITS_REFERENCLINES					211 	// Smart camera trip line settings
#define PARA_ITS_DETECTAREA						212 	// Smart camera detection locale
#define PARA_ITS_RECOTYPE						213 	// Smart camera algorithm type setting
#define PARA_METHODMODE							214     //set the video source standard access
#define PARA_LOCALSTORE_CHANNELPARA				215     //Channel Dependent Storage Policy
#define PARA_WIFIWORKMODE						216     //set the WIFI work mode
#define PARA_WIFIAPDHCPPARA						217     //set DHCP service configuration parameters Ip range of rental time
#define PARA_WIFIAPPARA							218 	// Set the WIFI_AP network parameter information
#define PARA_ITS_ENABLEPARAM					219 	// Set the traffic related function to enable
#define PARA_ITS_REDLIGHTDETECTAREA				220 	// Set the red light detection zone parameter
#define PARA_ATM_INFO							221 	// Set ATM information
#define PARA_AUDIO_SAMPLE						222 	// Set the audio sample rate
#define PARA_IPCPnP								223		//set the digital channel plug and play
#define PARA_WIFIDHCPAP							224 	// Set the AP mode DHCP function
#define PARA_MIXAUDIO							225 	// Mixing
#define PARA_DOME_TITLE							226 	// Dome title
#define PARA_HARDWARE_VOLTAGE					227 	// Set the 8127 camera voltage
#define PARA_ALARMTRIGGER_AUDIOLOSE				228 	// Set the audio loss alarm parameter value
#define PARA_VIDEOCOVER_ALARMSCHEDULE			229 	// Video masking alarm parameter template
#define PARA_VIDEOCOVER_ALARMLINK				230 	// Video masking alarm linkage setting
#define PARA_VIDEOCOVER_ALARMSCHENABLE			231 	// The video masking alarm template is enabled
#define PARA_HOLIDAY_PLAN_SCHEDULE				232 	// Holiday template
#define PARA_VCA_RULEEVENT12					233
#define PARA_VCA_RULEEVENT13					234
#define PARA_VDOLOST_LNKSINGLEPIC				235
#define PARA_ALMINPORT_LNKSINGLEPIC				236
#define PARA_MOTIONDEC_LNKSINGLEPIC				237
#define PARA_VDOCOVER_LNKSINGLEPIC				238
#define PARA_CHANNEL_TYPE						239 	// Channel type
#define PARA_OTHER_ALARMSCHEDULE				240 	// Other alarm parameter templates
#define PARA_OTHER_ALARMLINK					241 	// Other alarm parameter templates
#define PARA_OTHER_ALARMSCHENABLE				242 	// Other alarm parameter templates
#define PARA_PORTSET							243 	// The port setting parameter is changed
#define PARA_DISKGROUP							244
#define PARA_DISKQUOTA							245
#define PARA_NEW_IP								246
#define PARA_ITS_CROSSINFO						247 	// intersection information
#define PARA_ITS_AREAINFO						248 	// Gain Area
#define PARA_JPEGSIZEINFO						249 	// Camera capture resolution
#define PARA_DEVSTATUS							250
#define PARA_HD_TEMPLATE_INDEX					251 	// HD template number
#define PARA_STREAM_DATA						252 	// User-defined data insertion
#define PARA_VCA_RULEEVENT14_LEAVE_DETECT		253 	// Offline Detection Message
#define PARA_DOME_PTZ							254 	// Dome PTZ parameter change message
#define PARA_ITS_TEMPLATEMAP					255 	// The mapping between the time period and the HD camera template
#define PARA_GPS_CALIBRATION					256 	// Whether to enable GPS calibration
#define PARA_ALARM_THRESHOLD					257 	// Low-voltage alarm threshold
#define PARA_SHUTDOWN_THRESHOLD					258 	// Low Voltage Shutdown Threshold
#define PARA_SMART_SCHEDULE 					259 	// Smart Recording Template
#define PARA_VIDEO_SCHEDULE 					260
#define PARA_ITS_CHNCARSPEED					261 	// Vehicle speed parameter in lane
#define PARA_DOME_SYETEM						262 	// Dome system parameters
#define PARA_DOME_MENU							263		//Dome menu parameters
#define PARA_DOME_WORK_SCHEDULE					264		// dome run template parameters
#define PARA_INTERESTED_AREA					265 	// Region of Interest Parameters
#define PARA_APPEND_OSD							266 	// Additional character overlay parameters
#define PARA_AUTO_REBOOT						267 	// NVR automatically restarts the armed time
#define PARA_IP_FILTER							268 	// IP address filtering
#define PARA_DATE_FORMAT						269 	// Date Time Format
#define PARA_NETCONNECT_INFO_MTU				270 	// The MTU parameter (the maximum unit of the underlying socket send packet)
#define PARA_LANSET_INFO						271 	// NIC configuration parameters
#define PARA_DAYLIGHT_SAVING_TIME				272 	// Daylight saving time
#define PARA_NET_OFF_LINE						273
#define PARA_PICTURE_MERGE						274 	// Image composition
#define PARA_SNAP_SHOT_INFO						275 	// Before and after the capture parameters
#define PARA_IO_LINK_INFO						276
#define PARA_COMMAND_ITS_FOCUS					278 	// Focus on camera control
#define PARA_VCA_RULEEVENT7						279 	// Missing items left
#define PARA_VCA_RULEEVENT11					280 	// Trace events
#define PARA_DEV_COMMONNAME						281 	// Alias ??for the custom interface of the device (64 bytes)
#define PARA_APPVENCTYPE						282 	// The video encoding changes the subcode stream
#define PARA_THIRD_VENCTYPE						283 	// The video encoding changes the three streams
#define PARA_ITS_DEV_COMMONINFO					284 	// ITS device generic information message
#define PARA_ITS_COMPOUND_PARAM					285 	// Set the parameters related to the mix trigger
#define PARA_SDK_MSG_286						286
#define PARA_FILE_APPEND_INFO					287 	// Recording file additional information
#define	PARA_VCA_RULEEVENT3						288		// Intelligent analysis of wandering events
#define	PARA_VCA_RULEEVENT4						289		//Intelligent analysis of parking events	
#define PARA_VCA_RULEEVENT5 					290 	// Analyze the running events intelligently
#define PARA_VCA_RULEEVENT8 					291 	// Intelligent analysis of stolen events
#define PARA_ANYSCENE							292 	// Analyze the scene
#define PARA_ANYCRUISETYPE						293 	// Scene application cruise
#define PARA_ANYCRUISE_TIMER					294 	// The scene applies the timing cruise template
#define PARA_ANYCRUISE_TIMERANGE				295 	// Scene Apply time period cruise template
#define PARA_TRACK_ASTRICT_LOCATION 			296 	// Track Limit
#define PARA_FACE_DETECT_ARITHMETIC 			297 	// Face Detection Algorithm
#define PARA_PERSON_STATISTIC_ARITHMETIC		298			//the number of statistical algorithms
#define PARA_TRACK_ARITHMETIC					299 	// Intelligent Tracking Algorithm
#define PARA_TRACK_ZOOMX						300 	// Tracking magnification factor - real-time settings
#define PARA_COVER_ALARM_AREA					301 	// Video masking alarm zone parameter
#define PARA_3D_PRAVICY_INFO					302 	// The 3D privacy mask area parameter is changed
#define PARA_WORKDEV_BACKUP_ENABLE				303 	// Work Machine Hot Standby Enable
#define PARA_BACKUPDEV_INFO						305 	// Hot Standby configuration information
#define PARA_IPSAN_DISKINFO						306 	// IPSAN configuration information
#define PARA_RAID_WORKMODE						307 	// RAID operating mode information
#define PARA_RAIDARRAY_INFO						308 	// RAID list information
#define PARA_VIRTUAL_DISK_INFO					309 	// Virtual disk information
#define PARA_RAID_ENABLE						310 	// RAID function enable state
#define PARA_HOTBACKUP_WORKMODE					311 	// Hot Standby mode information
#define PARA_ITS_SECURITY_CODE					312		//set the number of security code
#define PARA_ITS_LIGHT_SUPPLY					313 	// License plate brightness compensation
#define PARA_ITS_CAPTURE_CPUNT					314 	// Number of snapshots
#define PARA_ITS_ILLEGAL_PARK_INFO				315 	// Regulate parking status information
#define PARA_ITS_LINE_PRESS_PERMILLAGE			316 	// Set the vehicle profile in the lane.
#define PARA_ITS_WORDOVERLAY					317 	// ITS character overlay
#define PARA_RTMP_URL_INFO						318 	// Send RTMP list information
#define PARA_RTSP_LIST_INFO 					319 	// RTSP list information
#define PARA_DEV_FORBID_CHN 					320 	// Number of blocked channels
#define PARA_ALARM_IN_CONFIG					321 	// Alarm channel configuration change message
#define PARA_ALARM_IN_LINK						322 	// Alarm channel linkage change message
#define PARA_ALARM_SCHEDULE						323 	// Alarm template change messages
#define PARA_ALARM_IN_OSD						324 	// Character Overlay Change Message
#define PARA_COM_DEVICE							325 	// serial device configuration change messages
#define PARA_DH_ALARM_HOST						326 	// Alarm server parameter change message
#define PARA_DH_WORK_MOD						327		//dynamic arm deployment mode
#define PARA_DH_DEV_TEST						328		//dynamic loop debugging mode
#define PARA_ALARM_IN_OFFLINE_TIME_INTERVEL 	329		// Offline scan interval change message
#define PARA_ELENET_METER						330 	// Electronic Net Meters Param
#define PARA_DH_ADD_ALARM_HOST					331 	// Add the alarm host
#define PARA_DH_DEVICE_ENABLE					332 	// The loop device is enabled
#define PARA_ALM_OUT_LHP						333		//alarm trigger mode, the output open-circuit alarm or closed-circuit alarm
#define PARA_OUTPUT_COLOR_TYPE					334		// Set the color output type
#define PARA_ITS_RADER_CFG_INFO					335		//ITS Roadway rader area config info
#define PARA_VCA_RULEEVENT15_WATER_DETECT		336		//water level monitor change message
#define PARA_CHANGE_VIDEO_INPUT_MODE			337		//change videoinput mode
#define PARA_CHANGE_WATER_STEADY_PERIOD			338		//water steady period
#define PARA_CHANGE_WATER_SNAP_INFO				339		//water steady period
#define PARA_STORAGE_ALARMPRE   				340
#define PARA_FAN_TEMP_CONTRO					341
#define PARA_MODIFYAUTHORITY					342
#define PARA_VCA_VIDEODIAGNOSE					343    	//VCA video diagnose
#define PARA_VCA_AUDIODIAGNOSE					344    	//VCA audio diagnose
#define PARA_VCA_TRIPWIRE300W					345    	//VCA Tripwire For 300W
#define PARA_PSE_CHANNEL_MODE					346
#define PARA_PSE_BARCODE						347
#define PARA_VCA_RULEEVENT14_LEAVE_DETECTEX 	348		//VCA Leave Detect For 300W
#define PARA_FAN_WHITELIGHT						349
#define PARA_APERTURE_TYPE						350
#define PARA_IDENTI_CODE						351
#define PARA_EXCEPTION_INFO						353
#define PARA_NET_CLIENT_VO_VIEW_SEGMENT			354		//Custom Picture segmentation way
#define PARA_NET_CLIENT_PREVIEWREC 				355		//Vc and VGA/HDMI1 or HDMI2 Output Homology
#define PARA_NET_CLIENT_PRE_MODE 				356		//Preview mode
#define PARA_ITS_TRAFFICFLOW					357 	//ITS Lane Traffic Statistics
#define PARA_ITS_TRAFFICFLOWREPORT 				358 	//ITS Traffic Flow Report
#define PARA_ITS_ILLEGALTYPE					359 	//ITS Illegal Type
#define PARA_ITS_PICMERGEOVERLAY				360		//ITS Pictrue Merge Over Lay
#define PARA_SDK_MSG_361						361
#define PARA_VCA_FACEMOSAIC 					362		//Face Mosaic
#define PARA_ITS_FTP_UPLOAD						363
#define PARA_ITS_SIGNAL_LIGHT					364
#define PARA_DISK_SMART							365
#define	PARA_OSD_DOT_MATRIX						366
#define	PARA_OSD_VECTOR							367
#define PARA_FRAMERATE_LIST						368
#define PARA_MAX_LANTRATE						369
#define PARA_ITS_CARLOGO_OPTIM					370
#define PARA_NET_CLIENT_CREATEFREEVO			371
#define PARA_EIS								373
#define PARA_SVC								374
#define PARA_SERVICE_SNMP						375
#define PARA_ALM_IN_LHP							376		// Alarm Trigger Mode, Input Open Alarm or Closed Alarm
#define PARA_NET_CLIENT_PTZ_LIST				377
#define PARA_ITS_CHNL_DELAY_DISTANCE			378
#define PARA_DEV_TRENDS_ROI						379
#define PARA_NET_CLIENT_COLORPARA_LIST 			380
#define PARA_NET_EXCEPTION_LINKOUTPORT 			381		// Exception handling port linkage
#define PARA_NET_WIRELESSMODULE					382 	// Get device support for wireless module
#define PARA_DEV_EVENT							383 	// Event template is enabled
#define PARA_DEV_TELNET							384 	// telnet enable
#define PARA_ILLEGAL_ALARMLINK					385
#define PARA_VDOLOST_LNKMAIL					386
#define PARA_ALMINPORT_LNKMAIL 					387
#define PARA_MOTIONDEC_LNKMAIL 					388
#define PARA_NET_CLIENT_SCENETIMEPOINT			389 	// Set the business time period
#define PARA_VCA_RULE_RIVERCLEAN				390 	// VCA Rule (River Detection)
#define PARA_VCA_RULE_DREDGE					391 	// VCA Rule (Pirates of the Pirates)
#define PARA_VCA_RIVER_ADVANCE					392 	// VCA Channel Detection Advanced Parameters
#define PARA_H323_LOCAL_Param					393		//H323 Local param
#define PARA_H323_GK_Param						394		//H323 GK param
#define PARA_COMMON_ENABLE_VIDEO_REVERSE		395		//video reverse
#define PARA_LIFEMONITOR_SET_CONFIG				396		//set config
#define PARA_OSD_EXTRA_INFO						397		//osd extra info
#define PARA_SNAPSHOT_VIDEOSIZE					398		//snapshot video size
#define PARA_ITS_IPDCAPMODEL					399		//set capture model
#define PARA_ITS_IPDCARPOSITION					400     //set manul capture car potion
#define PARA_DEV_DISKPOPSEAL					401     //disk pop & seal state
#define PARA_NETSERVER_SERVERINFO				402		//server info
#define PARA_COMMONENABLE_IllegalPark			403     //ITS Illegal Park
#define PARA_COMMONENABLE_PUBLIC_NETWORK		404		//public network state
#define PARA_DEV_VCA_CARCHECK					405     //Vca Car Check Enable
#define PARA_VCA_SCENE_RECOVERY					406     //Vca Scene recovery
#define PARA_VIDEO_PARAM						407     //video param
#define PARA_STREAM_SMOOTH						408		//stream smooth
#define PARA_VCA_FIGHT_ARITHMETIC				409		//fight arithmetic
#define PARA_LINK_HTTP_INFO						410		//link http
#define PARA_TIMINGRUISE_BY_TIMERANGE			411		//timing cruise by time range
#define PARA_SCENETEMPLATE_MAP					412		//scene template map
#define PARA_ALMINPORT_LNKHTTP					413
#define PARA_SDK_MSG_414						414
#define PARA_SDK_MSG_415						415
#define PARA_AUDIO_MUTE							416		//Audio Mute
#define PARA_ITS_LINKPANORAMACAP				417
#define PARA_COMMONENABLE_DISTORTION_REVISE		418				
#define PARA_ITS_PICREVCLIENT					420		
#define PARA_ITS_FILTERPLATE					421		
#define PARA_ITS_OSDTIMEFORMAT					422
#define PARA_ALARM_LINK_INTERVAL				423		//alarm link interval
#define PARA_PRECEDE_DELAY_SNAP					424		//precede delay snap
#define PARA_COMMONENABLE_PERIPHERAL_MANAGE 	425 	// Peripheral management
#define PARA_VOLUME_OUT							426 	// out put volume type
#define PARA_VCA_PROTECT						427 	// Alert rules parameters
#define	PARA_SEND_VIDEO_STREAM					428
#define	PARA_CONTROL_EMAIL						429
#define PARA_ITS_PIC_CUT						430
#define PARA_QQ_SERVICE							431
#define PARA_DDNS_EX							432
#define PARA_VIDEOSIZE_LIST						433
#define PARA_ITS_CHNLCAPSET						434
#define PARA_ITS_CHNLCARCAPTPYE					435
#define PARA_ITS_DELPICSTRATEGY					436		//delete strategy
#define PARA_ITS_HOSTNUMBER						437		//host number
#define PARA_ITS_DEVICENUMBER					438		//device number
#define PARA_ITS_PLATCFGINFO					439		//config platform info
#define PARA_ITS_PICUPLOADCFG					440		//config picture upload info
#define PARA_ITS_ADDCROSS						441		//ITS add cross
#define PARA_ITS_DELETECROSS					442		//ITS delete cross 
#define PARA_ITS_MODIFYCROSS					443		//ITS modify cross 
#define PARA_ITS_ADDLANE						444		//ITS add lane 
#define PARA_ITS_DELETELANE						445		//ITS delete lane 
#define PARA_ITS_MODIFYLANE						446		//ITS modify lane
#define PARA_ROUTENAT							447		//Port Mapping
#define PARA_ZOOM_CONTROL						448		//Zoom Control
#define PARA_ADD_EXDEV							449		//add exter device
#define PARA_DELETE_EXDEV						450		//delete exter device
#define PARA_MODIFY_EXDEV						451		//modify exter device
#define PARA_PLAY_RECORDING_AUDIO				452		//play recording audio
#define PARA_DELETE_RECORDING_AUDIO				453		//delete recording audio
#define PARA_MODIFY_RECORDING_AUDIO				454		//modify recording audio
#define PARA_TEMPERATURE_THRESHOLD				455		//temperature threshold
#define PARA_CLEAR_CONFIG						456		//clear config
#define PARA_ALARMLINK_FLASHING_WHITE			457		//alarm link flashing white
#define PARA_TEXT_PLAN							458		//text plan
#define PARA_DEV_MODEL							459		//device model
#define PARA_AUDIO_DETECTION					460		//Get Audio Value
#define PARA_AUDIO_THRESHOLD					461	
#define PARA_ALGORITHM_TYPE						462
#define PARA_SDK_MSG_463						463
#define PARA_ALARMLINK_TRAFFIC_TRIG				464
#define PARA_QOS_VALUE							465
#define PARA_PLATE_LIST_TYPE					466
#define PARA_SMART_CHECK						467
#define PARA_LOCALSNAP_SCHEDULE					468
#define PARA_FOCUSSTATE_RUNSTATE				469
#define PARA_ITS_MODE_SETTING					470
#define PARA_FEC_EPTZ							471
#define PARA_FEC_EPRESET						472
#define PARA_GET_PTZ							473
#define PARA_SPLUS								474
#define PARA_SMTPINFO_EX						475
#define PARA_FAR_END_ENLARGE					476
#define PARA_LOCAL_STORE_PATH					477
#define PARA_REPORT_QUERY						478
#define PARA_HEATMAP_GET						479
#define PARA_VCA_HEATMAP						480
#define PARA_VENC_SLICE_TYPE					481
#define PARA_CHANNEL_CONNECT_STATE				482
#define PARA_PORT_MAP							483
#define PARA_VCA_ALERT_TEMPLATE					484
#define PARA_VCA_SEEPER							485
#define PARA_VIDEO_LDC							486		////camera calibration
#define PARA_VODEV_OUTPUT_MODE					487
#define PARA_APP_LIST							488		////camera calibration
#define PARA_PORT_NO							489		
#define PARA_READCOM_TIMEINTERVAL				490		
#define PARA_VCA_ST_FACEADVANCE					491
#define PARA_VCA_WINDOW_DETECTION				492
#define PARA_VCA_ALERT_MULTISTAGE				493
#define PARA_ITS_PARK_WHITE_LIST				494		//ITS park white License plate list
#define PARA_ITS_PARK_GUARD						495		//park guard
#define PARA_ALARMTRIGGER_TEMPERATURE			496
#define PARA_ALARMTRIGGER_HUMIDITY				497
#define PARA_TEM_UPPER_LIMIT_ALARMSCHEDULE		498
#define PARA_TEM_LOWER_LIMIT_ALARMSCHEDULE		499
#define PARA_HUM_UPPER_LIMIT_ALARMSCHEDULE		500
#define PARA_HUM_LOWER_LIMIT_ALARMSCHEDULE		501
#define PARA_TEM_UPPER_LIMIT_SCHEDULEENABLE		502
#define PARA_TEM_LOWER_LIMIT_SCHEDULEENABLE		503
#define PARA_HUM_UPPER_LIMIT_SCHEDULEENABLE		504
#define PARA_HUM_LOWER_LIMIT_SCHEDULEENABLE		505
#define PARA_CALIBRATE_INFO						506
#define PARA_VCA_MASK_AREA_PARAM				507
#define PARA_GEOGRAFHY_LOCATION					508
#define PARA_GEOGRAFHY_TIME_MODE				509
#define PARA_GPS_TIME							510		//GPS check time interval
#define PARA_EXCEPTION_DISK_TEM					511
#define PARA_VCA_HELMET							512
#define PARA_VCA_HELMET_SIZE_RANGE				513
#define PARA_IPD_WORK_MODE						514
#define PARA_ITS_LEFTCOMITYSTRAIGHTDETECTAREA	515
#define PARA_VCAFPGA							516
#define PARA_ALARMTRIGGER_PRESSURE				517
#define PARA_PRESSURE_UPPER_LIMIT_ALARMSCHEDULE		518
#define PARA_PRESSURE_LOWER_LIMIT_ALARMSCHEDULE		519
#define PARA_PRESSURE_UPPER_LIMIT_SCHEDULEENABLE	520
#define PARA_PRESSURE_LOWER_LIMIT_SCHEDULEENABLE	521
#define PARA_ITS_STJ1RADAR_MODE					522
#define PARA_WHITELIGHT_CONTRL					523
#define PARA_DZ_COMMON_EX						524
#define PARA_VIDEO_LDCEX_INFO					525 
#define PARA_PERIPHERAL_INFO					526 
#define PARA_CALIBRATE_MODE						527 
#define PARA_VCA_AREA_FIRST						528 
#define PARA_VCA_BIND_OPERATE					529 
#define PARA_VCA_BIND_INFO						530 
#define PARA_VCA_CAMERA_TYPE					531 
#define PARA_PTZ_COMFORMAT						532 
#define PARA_ITS_IPDTESTSNAP					533
#define PARA_SET_PTZ							534
#define PARA_ITS_DOCKPLAT_UPLOADTYPE			535
#define PARA_UNIQUE_ALERT_CFGCHN				536
#define PARA_UNIQUE_ALERT_EVENTSET				537
#define PARA_UNIQUE_ALERT_SCENE_TIMESEG			538
#define PARA_UNIQUE_ALERT_DRAW_LINE				539
#define PARA_UNIQUE_ALERT_LINK_MODE				540
#define PARA_UNIQUE_ALERT_ALARM_SCHEDULE		541
#define PARA_UNIQUE_ALERT_PERIMETER				543
#define PARA_UNIQUE_ALERT_TRIPWIRE				544
#define PARA_UNIQUE_ALERT_CFG_TARGET			548 	
#define PARA_UNIQUE_ALERT_CFG_ADV				549 	
#define PARA_UNIQUE_ALERT_SCENE_REV				550 
#define PARA_DEV_RESOLUTION						551
#define PARA_DNS_ENABLE							552
#define PARA_ALARMSCHEDULE_FACE_IDENT			553
#define PARA_ALARMSCHENABLE_FACE_IDENT			554
#define PARA_ELE_ANTI_SHAKE						555
#define PARA_ITS_LIGHT_STATUS					556
#define PARA_SGW_NET_BITRATE					557
#define PARA_SGW_SIP_LOCALSERVER				558
#define PARA_SGW_MAC_FILTER						559
#define PARA_SGW_VPN_LAN						560
#define PARA_ITS_DEV_OFF_LINE					561
#define PARA_SGW_VPN_PARAM						562
#define PARA_VCA_COLOR_TRACK					563
#define PARA_ITS_SDI_CUTAREA                    564
#define PARA_VCA_EXTRA_ALARM_SCHEDULE			565
#define PARA_OSD_COLOR							566
#define PARA_ALARMSCHENABLE_NVR_VCA             567
#define PARA_ALARMSCHEDULE_NVR_VCA              568
#define PARA_ALARM_FACE_PARAM					569
#define PARA_OSD_DISPLAY						570
#define PARA_DETECT_AREA                        571
#define PARA_DETECT_SET                         572
#define PARA_IRRIGATION_NOTIFY					573
#define PARA_VCA_TARGET_PICTURE                 574
#define PARA_VCA_SLUICEGATE						575
#define PARA_VCA_MANCAR_DETECTARITHMETIC       	576
#define PARA_IRRIGATIONEND_NOTIFY				577
#define PARA_ALARM_RAINFALL                     578
#define PARA_ALARM_ALERTWATER                   579
#define PARA_ALARMSCHENABLE_RAINFALL            580
#define PARA_ALARMSCHENABLE_ALERTWATER          581
#define PARA_ALARMSCHEDULE_WATER_RAINFALL       582
#define PARA_ALARMSCHEDULE_WATER_ALERTWATER     583
#define PARA_IRRIGATION_OVER_PARA				584
#define PARA_PERIPHERAL_PARA					585
#define PARA_LOGNUM_PARA						586
#define PARA_USER_LOGONLOCK_PARA				587
#define PARA_VERIFICATION_CODE				    588
#define PARA_SNMP_PARA                          589
#define PARA_PICSTREAM_UPLOADPARAM				590
#define PARA_VCA_SINGLE_INQUIRY					591
#define PARA_VCA_CLIMB_UP						592
#define PARA_VCA_NEW_DEPARTURE					593
#define PARA_VCA_ABNORMAL_NUMBER				594
#define PARA_VCA_GET_UP							595
#define PARA_VCA_LEAVE_BED						596
#define PARA_VCA_STATIC_DETECTION				597
#define PARA_VCA_SLEEP_POSTION					598
#define PARA_VCA_SLIP_UP						599
#define PARA_VCA_NEW_FIGHT						600
#define PARA_VCA_BODY_TOUCH						601
#define PARA_ZF_VCA_ALARMSCHEDULE				602
#define PARA_ZF_VCA_SCHEDULEENABLE				603
#define PARA_SIP_ENCRYPTMODE                    604
#define PARA_SIP_ENCRYPTPUBLICKEY               605
#define PARA_HUMAN_ADVANCE						606
#define PARA_ITS_ILLEGALPARK_FILTERPLATE		607
#define PARA_NETREDUCE_CTRL                     608
#define PARA_DORMANCY_STATE                     609
#define PARA_DORMANCY_SCHEDULE                  610
#define PARA_GEOGRA_LOCATION                    611
#define PARA_GAUGE_PARAM                        612
#define PARA_WATER_SPEED                        613
#define PARA_UPLOAD_STATION                     614
#define PARA_VCA_PEPT            			    615
#define PARA_VCA_SENCE_SNAP            			616
#define PARA_PEPT_RETRANS_INOF         			617
#define PARA_GPS_DANGERAREA           			618
#define PARA_NAVIGATION_SHIP_DETECTION          619
#define PARA_3DLOCATE_PREDICTION				620
#define PARA_DANGEROUS_AREA_ALARMSCHEDULE		621
#define PARA_DANGEROUS_AREA_SCHEDULEENABLE		622
#define PARA_HOLIDAY_SCHEDULE                   623
#define PARA_HOLIDAY_PLAN                       624
#define PARA_SDK_MSG_625						625
#define PARA_GRANARY_VEHICLE_DETECTION          626
#define PARA_TENCENT_INFO                       627
#define PARA_VCA_CHEFHATDETECT		            628
#define PARA_VCA_CHEFMASKDETECT		            629
#define PARA_VCA_STRANDED			            630
#define PARA_VCA_ALONE				            631
#define PARA_VCA_DELIVERGOODS		            632
#define PARA_VCA_TOPS				            633
#define PARA_SHDB_RUNSTATE		                634
#define PARA_SHDB_ALARMPIC		                635
#define PARA_SHDB_TIMESNAP		                636
#define PARA_ALM_LOOP_DETEC                   	637
#define PARA_IDENTIFICATION_TYPE             	638
#define PARA_DEV_AUTOTIMING						639
#define PARA_DEVICENAME						    640
#define PARA_IPV6_FILTER						641 	// IPV6 address filtering
#define PARA_VOLTAGE_HIGH_SCHEDULEENABLE		642		// Set voltage upper limit linkage alarm
#define PARA_VOLTAGE_LOW_SCHEDULEENABLE			643		// Set the low voltage linkage alarm
#define PARA_SMD_SCENE_ENABLE					644		// Intelligent scene enabling settings
#define PARA_WATERLEVEL_SOURCE					645		// Type of equipment water level data source (integrated water conservancy machine)   
#define PARA_WATERLEVEL_SOURCE_ID				646		// Equipment acceptance water level data source ID (water conservancy integrated machine)
#define PARA_VCA_SMOKEDETECT					647		//
#define PARA_VCA_PHONEDETECT					648
#define PARA_VCA_SLUICEGATE_PART2				649
#define PARA_OSD_CLARITY						650		//Character overlay background transparency 
#define PARA_OSD_TIMEFORMAT                     651     //face osd time format
#define PARA_FACE_OSD_WORDOVERLAY               652     //face osd overlay
#define PARA_FACE_OSD_DEVCOMMON                 653     //face osd dev info
#define PARA_IMAGE_LOCATION                     654
#define PARA_GET_PTZEX                          655
#define PARA_MANUAL_PORTMAP                     656     //Configure mapping port protocol 配置映射端口协议
#define PARA_IEEE8021X				            657		//设置802.1X参数
#define PARA_COMMON_RTMP			            658
#define PARA_IEEE8021X_STATE		            659
#define PARA_VCA_TEMDETECT   					660
#define PARA_VCA_SCAN_AREA   					661
#define PARA_VCA_SCAN_PARA   					662
#define PARA_VCA_TEMPERATURESTANDARD  			663
#define PARA_VCA_BLACKBODYCORRECT   			664
#define PARA_VCA_BODYTEMPCORRECT  				665
#define PARA_VCA_INTELLIGENTCORRECT   			666
#define PARA_VCA_FIREWORK                       667
#define PARA_VCA_MASKAREAENABLE                 668
#define PARA_VCA_MASKAREAPARA                   669
#define PARA_RADAR_EVENT_PARA                   670
#define PARA_RADAR_DEVICE_PARA                  671
#define PARA_CROPCODING_PARA                    672
#define PARA_VCA_RULEEVENT15_WATER_DETECT2		673	//water level monitor change message
#define PARA_VCA_CHEFDETECT						674	//chef detect
#define PARA_RADAR_ADVANCED_DEVICE_PARA         675
#define PARA_RADAR_CALIBRATE		            676
#define PARA_BLACK_BODY_DETECT		            677
#define PARA_FACE_LIB_SYNC_RESULT	            678
#define PARA_WIFISTATE_CHANGED                  679
#define PARA_BLACK_BODY_ERR_ALARMSCHEDULE		680
#define PARA_BLACK_BODY_ERR_SCHEDULEENABLE		681
#define PARA_SMALLCELL_INFO                     682
#define PARA_SMALLCELL_IMSI                     683
#define PARA_VCA_HDSCHEDULE				        684 //scene HD template para 场景高清模板参数
#define PARA_VCA_FOCUSAREA                      685 //scene focus area para 场景重点区域参数
#define PARA_TEM_POSITION						686 //Get the temperature measurement part of human body
#define PARA_SMART_MOVE							687 //Smart move
#define PARA_SINGLE_PIC_FEATURE					688 //Feature matting parameter acquisition
#define PARA_WATERGAUGE_INFO                    689
#define PARA_TRAFFIC_FILL_LIGHT_INFO            690 //Traffic fill light info
#define PARA_DEFAULT_TEMPLATE_LIST              691 //template list association by Intelligent analysis type  智能分析类型关联模板列表
#define PARA_EBIKE_PLATE						692 //Electric vehicle license plate recognition parameters
#define PARA_WATERFLOW_PARAM				    693 //Set Water Flow param
#define PARA_WORDOVERLAYPOS_PARAM				694 //Set WordOverLay Position
#define PARA_IRRISERVERINFO      				695 //Set IrriserverInfo 
#define PARA_KAFKACFG                           696 //Get Kafka Config
#define PARA_CPC_AREADISPLAY      				697 //Set cpc area display
#define PARA_CPC_AREACONFIG      				698 //Set cpc area config
#define PARA_VCA_INQUIRITY_TIMEOUT      		699 //Set cpc area config
#define PARA_VCA_WSTABLEUSEMODE                 700 //Set WaterFlow Table Use Mode
#define PARA_VCA_WSTABLEAUTOGEN                 701 //Set WaterFlow Table Auto
#define PARA_HTTPPICSTREAM                      702 //Set Http Pic Stream
#define PARA_GAT1400											703 //Set gat1400 para
#define PARA_VITUALGAUGEDIS										704 //Set gat VirtualGauge Dis
#define PARA_DEVREALTIMEPARAM									705 //dev real time param
#define PARA_VCA_LEAVEDETECTEX									706 //new leave detect 
#define PARA_ALARMSCHEDULE_ALERT_WATERLEVEL_LOWER_LIMIT			707 //alarm scehdule WaterLevel Alarm
#define PARA_ALARMSCHEDULE_PREALERT_WATERLEVEL_UPPER_LIMIT		708 //alarm scehdule pre-alert water upper alarm
#define PARA_ALARMSCHEDULE_PREALERT_WATERLEVEL_LOWER_LIMIT		709 //alarm scehdule pre-alert water lower alarm
#define PARA_ALARMSCHEDULE_ENABLE_ALERT_WATERLEVEL_LOWER_LIMIT			710 //alarm scehdule enable WaterLevel Alarm
#define PARA_ALARMSCHEDULE_ENABLE_PREALERT_WATERLEVEL_UPPER_LIMIT		711 //alarm scehdule enable pre-alert water upper alarm
#define PARA_ALARMSCHEDULE_ENABLE_PREALERT_WATERLEVEL_LOWER_LIMIT		712 //alarm scehdule enable pre-alert water lower alarm
#define PARA_WIEGAND                                            		713 //face pad wiegand param change
#define PARA_VCA_INDOOREBIKE											714 //indoor ebike param change 
#define PARA_DEVICE_LOWERPOWER											715 //get device lower power
#define PARA_VCA_RULEEVENT15_WATER_DETECT3								716 //GET  DetDisturbFlag
#define PARA_LENPARA													717 //GET  LenPara
#define PARA_ITS_CHNLANEEXPARAM											718 //GET  Channel lane extern param 
#define PARA_ITS_DRIVEAWAYLINKLIGHT										719 //GEt  DRIVEAWAYLINKLIGHT
#define PARA_NAS_PARAM													720 //Get nas param
#define PARA_AUDIO_CHANNEL												721 //Get Sip Audio Channel
#define PARA_RTSP_INFO													722 //GetRtsp Info
#define PARA_GAUGECALIB_PARAM											723 //GaugeCalib Param
#define PARA_IRRIGATION_SERVER_INFO										724 //Irrigation server Info
#define PARA_IRRIGATION_UPLOAD_INFO										725 //Irrigation upload Info
#define PARA_ITS_PLATE_FILTER_PARAM										726 //
#define PARA_VCA_ANYSCENEADVANCED										727	//Vca scene advanced
#define PARA_DORMANCY_LOWPOWER											728 //Dormancy low power
#define PARA_DORMANCY_TIME												729 //Dormancy Time
#define PARA_VCA_CPC_ADVANCE_PART2										730 //
#define PARA_DISARM_PARAM_MANU											731
#define PARA_DISARM_PARAM_CYCLE											732
#define PARA_DISARM_PARAM_INPUT											733
#define PARA_CALIBRATION_REFERENCEINFO									734 //Set calibration reference system
#define PARA_ELEVATOR_MONITOR											735
#define PARA_ELEVATOR_STOREYINFO										736
#define PARA_ELEVATOR_STATISTICS										737
#define PARA_VCA_REFBOUNDARYINFO										738
#define PARA_VCA_RADARLINKSCENE											739
#define PARA_DIGITALWATERMARK											740
#define PARA_WATER_OUTFALL												741
#define PARA_VCA_ALARMSTAT_V2											742 //回调的结构体中 m_tPara.m_iPara[0]固定表示算法类型
																			//当iEventType=94时 m_tPara.m_iPara[1]代表民警警服检测的“民警警服数量”统计计数值
																			//当iEventType=95时 m_tPara.m_iPara[1~6]依次代表被监管人员识别服检测的“橙色,黄色,绿色,红色,蓝色,未穿识别服”统计计数值
#define PARA_VCA_IRRIDATA_UPLOADPARAM									743 //水利数据检测上报参数
#define PARA_VCA_SPECLIGHTPARAM											744 //高光谱灯源参数
#define PARA_VCA_WATERQUALITY_WIPER										745 //水质检测清洁刷配置
#define PARA_IRRI_ELECFENCE												746 //水利电子围栏配置参数
#define PARA_IRRI_ALGEXTINFO											747 //水利算法扩展信息配置参数
#define PARA_VEHICLE_IDENTIFY_ALARMSCHEDULE								748 //车辆识别报警模板参数
#define PARA_VEHICLE_IDENTIFY_SCHEDULEENABLE							749 //车辆识别模板使能参数
#define PARA_WATER_COLOR_DETECT_PARA									750 //水体颜色识别参数消息
#define PARA_VO_MOSAIC													751 //合成通道马赛克遮挡区域参数
#define PARA_MOTION_DETECTION_CAR_PARAM									752 //移动侦测之车形报警参数
#define PARA_GDDZ_SECURITY_CLASSIFICATION								753 //定制专用 密级参数
#define PARA_EVACAPACITY								                754 //蒸发量参数
#define PARA_DZSET_TRANSPARENT											755	//设置定制透传参数异步参数改变消息，携带参数结构体DzTransparentPara是栈上的变量，不可用于消息参数传递
#define PARA_DEVREPORT_TRANSPARENT										756	//设备主动定时上报异步消息，携带参数结构体DzTransparentPara是栈上的变量，不可用于消息参数传递
#define PARA_DEV_UTCTIME_PARA											757	//设备广播当前UTC时区和时间的异步消息，携带参数结构体STR_Para的变量m_tPara
																			//m_tPara.m_iPara[0]是当前正在使用的时区
																			//【整点时区】88～113，即在真实时区上+100，比如格林威治GMT+0时区，则发100，中国是GMT+8时区，则发108；但呈现给用户的范围应该是-12～+13
																			//【非整点时区】8800～11400，即在真实时区的hhmm表示法基础上+10000，比如GMT+05:45时区，则发10545(=10000+0545)；比如GMT-6:30时区，则发9370(=10000-0630)
																			//m_tPara.m_iPara[1]当前时间的UTC时间，从1970-1-1经过的秒数-UTC时间
																			//m_tPara.m_iPara[2]当前时间的毫秒位值，精确到毫秒，取值范围0～999
#define PARA_INDUSTRY_PLATFORM_TAG_INFO									758 //行业平台标识信息参数
#define PARA_RECORD_CHN_ALISA_NAME										759 //设置录像通道别名异步消息
#define PARA_IRRIGATION_SERVER_INFO_V2									760 //Irrigation server Info V2
#define PARA_NVR_DEV_NET_RESOURCES										761
#define PARA_IRRIGATION_REPORTDORMANCY									762	//水位加报的低功耗时间
#define PARA_DORMANCY_SCHEDULE_V2										763
#define PARA_TARGET_REALTIME_RESULT										764	//获取靶标检测算法结果
#define PARA_TARGET_PARAM_GET											765 //获取靶标检测算法参数
#define PARA_IRR_RECEIVER_COMMON_MSG									766
#define PARA_THMSCREE_SENSORCORRECT										767	//获取温湿度屏传感器数据校正值
#define PARA_THMSCREE_SYSTEM											768 //获取温湿度屏系统参数
#define PARA_GAUGECALIBCHECK_DATA										769 //获取校验使用的水尺标定参数【水利行业专用协议】			
#define PARA_MOBILEPRARAM												770 //回复设置定向流量卡测试ip结果
#define PARA_REMOTE_CONTROL_ALARM										771 //设置远程控制报警【水利行业专用协议】	
#define PARA_LANPRIORITY												772 //回复设置网卡优先级回复	
#define PARA_DiskGroupV2												773 //回复设置盘组模板	
#define PARA_SDK_MSG_MAX												774


//unique alert perimeter alarm link msg
#define	PARA_ALERT_PERIMETER_LINK_SOUND			20000
#define	PARA_ALERT_PERIMETER_LINK_OUTPORT		20002
#define	PARA_ALERT_PERIMETER_LINK_RECORD		20003
#define	PARA_ALERT_PERIMETER_LINK_SNAP			20005
#define	PARA_ALERT_PERIMETER_LINK_LASER			20009
#define	PARA_ALERT_PERIMETER_LINK_WHITE			20010
#define	PARA_ALERT_PERIMETER_LINK_TRACK			20018
#define	PARA_ALERT_PERIMETER_LINK_LAMP			20019

//unique alert tripwire alarm link msg
#define	PARA_ALERT_TRIPWIRE_LINK_SOUND			20100
#define	PARA_ALERT_TRIPWIRE_LINK_OUTPORT		20102
#define	PARA_ALERT_TRIPWIRE_LINK_RECORD			20103
#define	PARA_ALERT_TRIPWIRE_LINK_SNAP			20105
#define	PARA_ALERT_TRIPWIRE_LINK_LASER			20109
#define	PARA_ALERT_TRIPWIRE_LINK_WHITE			20110
#define	PARA_ALERT_TRIPWIRE_LINK_TRACK			20118
#define	PARA_ALERT_TRIPWIRE_LINK_LAMP			20119


#define MAX_SCENETIME_COUNT						8		//set the business time period
#define MAX_SCENETIME_TYPE_COUNT				2		//set the business time period

typedef int	IDENTIFY;
#define CLIENT									0
#define PROXY									1
#define CLIENT_PROXY							2

#define LOGONOBJ_DEVICE		0
#define LOGONOBJ_PROXY		1

#define DEFAULT_TYPE_HD_PARAM					(1 << 0)
#define DEFAULT_TYPE_VCA_ADV					(1 << 1)
#define DEFAULT_TYPE_HD_TEMPLATE				(1 << 2)
#define DEFAULT_TYPE_NET						(1 << 16)
#define DEFAULT_TYPE_STORAGE					(1 << 17)
#define DEFAULT_TYPE_SYSTEM						(1 << 18)
#define DEFAULT_TYPE_EVENT						(1 << 19)
#define DEFAULT_TYPE_CHANNEL					(1 << 20)
#define DEFAULT_TYPE_PREVIEW					(1 << 21)
#define DEFAULT_TYPE_ALL						0xFFFFFFFF

typedef int DECDATATYPE;
#define T_AUDIO8 								0
#define T_YUV420 								1
#define T_YUV422 								2

//Sensor Type
#define SENSOR_TYPE_LASER_RAIN_GAUGE                        0
#define SENSOR_TYPE_RADAR_WATER_LEVEL_GAUGE                 1
#define SENSOR_TYPE_BATTERY_SENSOR                          2
#define SENSOR_TYPE_FLOW_METER                              3
#define SENSOR_TYPE_LED_SCREEN                              4
#define SENSOR_TYPE_BEIDOU_MOUDLE                           5

//peripheral type;
#define PERIPHERAL_TYPE_THERMOMETER								1
#define PERIPHERAL_TYPE_PRESSURE_GAUGE							2
#define PERIPHERAL_TYPE_LASER_RAIN_GAUGE						3
#define PERIPHERAL_TYPE_RADAR_WATER_LEVEL_GAUGE					4
#define PERIPHERAL_TYPE_BATTERY_SENSOR							5
#define PERIPHERAL_TYPE_FLOW_METER								6
#define PERIPHERAL_TYPE_LED_SCREEN								7
#define PERIPHERAL_TYPE_BEIDOU_MOUDLE							8
#define PERIPHERAL_TYPE_GPS										9 
#define PERIPHREAL_TYPE_485LIGHT								10
#define PERIPHREAL_TYPE_ACIDITYANDALKAL							11
#define PERIPHREAL_TYPE_DISSOLVEOXYGEN							12
#define PERIPHREAL_TYPE_OXYREDUCTION							13
#define PERIPHREAL_TYPE_TURBIDITY								14
#define PERIPHREAL_TYPE_AMMONICA								15
#define PERIPHREAL_TYPE_SWF_03_QUALITY							16
#define PERIPHREAL_TYPE_PRESSURE_WATERLEVEL_GAUGE				17
#define PERIPHREAL_TYPE_TILT_SENSOR								18
#define PERIPHREAL_TYPE_RTU_SENSOR								19
#define PERIPHREAL_TYPE_WEATHER_STATION							20
#define PERIPHREAL_TYPE_DUST_COLLECTOR							21
#define PERIPHREAL_TYPE_AIR_SENSOR_COMB_GAS						22
#define PERIPHREAL_TYPE_AIR_SENSOR_OXYGEN						23
#define PERIPHREAL_TYPE_AIR_SENSOR_CARBON						24
#define PERIPHREAL_TYPE_AIR_SENSOR_HYDROGEN						25
#define PERIPHREAL_TYPE_FLOAR_GAUGE_LEVEL						26
#define PERIPHREAL_TYPE_AIR_SENSOR_FORMALDEHYDE					27
#define PERIPHREAL_TYPE_AIR_SENSOR_AMONIA						28
#define PERIPHREAL_TYPE_AIR_SENSOR_LIQUID						29
#define PERIPHREAL_TYPE_THERANDHYGEO							30
#define PERIPHREAL_TYPE_POLITICAL_TEMANDHUM_SCREEN				31
#define PERIPHREAL_TYPE_MPPT_SOLAR								32
#define PERIPHREAL_TYPE_ELEC_GAUGE								33
#define PERIPHREAL_TYPE_LS_OSD									34
#define PERIPHREAL_TYPE_RADAR_FLOWMETER							35
#define PERIPHREAL_TYPE_FLOOR_DISPLAY_SENSOR					36
#define PERIPHREAL_TYPE_WATERLEVEL_SENSOR_LM					37
#define PERIPHREAL_TYPE_SMART_ELECTRODE							38
#define PERIPHREAL_TYPE_FLOOR_DISPLAY_SENSOR_ZT					39
#define PERIPHREAL_TYPE_OSD_HYH							        40
#define PERIPHREAL_TYPE_RADAR_FLOWMETER_SX						41
#define PERIPHREAL_TYPE_TEMPERATURE_SENSOR						42
#define PERIPHREAL_TYPE_WIND_SPEED_SENSOR						43
#define PERIPHREAL_TYPE_CUSTOM_1						        44
#define PERIPHREAL_TYPE_CUSTOM_2						        45
#define PERIPHREAL_TYPE_CUSTOM_3						        46
#define PERIPHREAL_TYPE_CUSTOM_4						        47
#define PERIPHREAL_TYPE_CUSTOM_5						        48
#define PERIPHREAL_TYPE_CUSTOM_6						        49
#define PERIPHREAL_TYPE_CUSTOM_7						        50
#define PERIPHREAL_TYPE_CUSTOM_8					            51
#define PERIPHREAL_TYPE_CUSTOM_9						        52
#define PERIPHREAL_TYPE_CUSTOM_10						        53
#define PERIPHREAL_TYPE_CUSTOM_11						        54
#define PERIPHREAL_TYPE_CUSTOM_12						        55
#define PERIPHREAL_TYPE_TEMANDHUM_PRSS					        56
#define PERIPHREAL_TYPE_PLATE_SCREEN					        57
#define PERIPHREAL_TYPE_ANTI_SUS_BRACKET				        58
#define PERIPHERAL_TYPE_RADAR_WATER_LEVEL_GAUGE_BR		        59
#define PERIPHERAL_TYPE_RADAR_WATER_LEVEL_GAUGE_GD				60
#define PERIPHERAL_TYPE_BUBBLE_WATER_LEVEL_GAUGE				61
#define MAX_PERITYPE											62


//modify for x64
typedef struct
{
    void*  m_iPara[10];
    char    m_cPara[33];
}STR_Para;

#define ITS_DEVICE_TYPE_RED_LIGHT						0
#define ITS_DEVICE_TYPE_VEGICLE_INSPECTION				1
#define ITS_DEVICE_TYPE_LOOP							2
#define ITS_DEVICE_TYPE_TEMPERATURE						3
#define ITS_DEVICE_TYPE_RED_LIGHT_SIGNAL_DETECTOR		4
#define ITS_DEVICE_TYPE_IMAGE_LIGHTNESS					5
#define ITS_DEVICE_TYPE_RED_SYN							6
#define ITS_DEVICE_TYPE_MAX								7
#define MAX_DEVSTATUS_TYPE								ITS_DEVICE_TYPE_MAX
//add by wd 20130621
#define MAX_DEVSTATUS_NUM								4

#define MAX_BITSET_COUNT					16  //support 512 linkage
#define MAX_ROADWAY_COUNT					4          //Maximum number of lanes

#define LIGHT_COUNT_EX                      16                   //Signal light number  motify 20160401
#define LIGHT_COUNT							4					//Signal light number
#define LIGHT_COM_COUNT						4					//Signal light serial port number
#define CAPTURE_PLACE_COUNT					3					//The number of corresponding to the capture location

#define MAX_TIMESEGMENT						4


typedef struct TImgDisposal	//Picture effect
{
	int iChannelID;
	int iStartUp;		//Picture effect processing is enable identification, 0：Not enable 1:Enable
	int iQuality;		//Quality,	Range：0 to 100
	int iSaturation;	//Saturation ,range：0 to 255
	int iBrighten;		//Brightness , range ：0 to 255
	int iContrast;		//Contrast ratio, range ：0 to 255
	int iGamma;			//Gamma value, range：0 to 50	
	int iAcutance;		//Sharpness, range：0 to 255
	int iImgEnhanceLevel;//Image enhancement level, range：0～3					
}TImgDisposal;

//Signal lamp corresponding parameters
typedef struct TITSLightInfo
{
	int iLightID;						//Signal lamp number 0-3	
	int iTimeInterval;					//Signal lamp time interval   init s	
	int iTimeAcceptDiff;				//Signal lamp time tolerance value  init s					
}TITSLightInfo;

/**************************************以下结构体不再维护**************************************/
/************************The following structures are no longer maintained*********************/ 
//OSD param

typedef struct
{
	unsigned char   ucCovTime;
	unsigned char   ucCovWord;
	int             iCovYear;
	int             iCovMonth;
	int             iCovDay;
	int             iCovHour;
	int             iCovMinute;
	int             iCovSecond;
	unsigned char   ucPosition;
	char            cCovWord[32];
}OSD_PROPERTY;

typedef struct
{
	char    m_cDDNSUserName[LEN_32];
	char    m_cDDNSPwd[LEN_32];
	char    m_cDDNSNvsName[LEN_32];
	char    m_cDDNSDomain[LEN_32];
	int     m_iDDNSPort;
	int     m_iDDNSEnable;
}DDNS_INFO, *PDDNS_INFO;

typedef struct ITS_OSD
{
	int iX;
	int iY;
	int iEnable;
} *PITS_OSD;

typedef struct ITS_ILLEGALRECORD
{
	long m_lCarID;
	int  m_iRecordFlag;
	int  m_iIllegalType;
	int  m_iPreset;
}*pITS_ILLEGALRECORD;


typedef struct TPlatformVersion
{
	char cData[LEN_32+1];
}TPlatformVersion;

//Set the time zone for the device
typedef struct __tagTimeZone
{
	int iSize;
	int iTimeZone;			//The time zone currently in use should range from -12 to +13
	int iDSTEnable;			//Summer time enalbe,0-forbid，1-enable
}TTimeZone, *pTTimeZone;

typedef struct __tagSYSTEMTYPE
{
	int m_iSize;
	int m_iWorkMode;					//	When this parameter is not modified ,the assignment is 0x7FFFFFFF
	int m_iDevProducter;
}SYSTEMTYPE, *LPSYSTEMTYPE;

typedef struct TECOPChannelParam	//Lane relate parameters
{
	TImgDisposal tImgDisposal;
	int iRecognizeEnable;
	int iWaterMarkEnable;
}TECOPChannelParam;

typedef struct TECOPParam
{
	TECOPChannelParam tChannel[MAX_ROADWAY_COUNT];
	int iSystemType;	//System type
	//0：Debugging
	//3：Bayonet
	//4：Digital police	
	int iLockRet;		//Get register result
	//bit0：Identification dog
	//bit1：Intelligent traffic encrypt dog
	//0：Non register，1：Register
	TITSLightInfo tLightInfo[LIGHT_COUNT];
}TECOPParam;

typedef struct tagVCAFacerec
{
	int iBufSize;					//structure size
	VCARule	stRule;
	vca_TDisplayParam stDisplayParam;			
	vca_TPolygon stRegion;				//Area region
}VCAFaceRec, *pVCAFacerec;

typedef struct tagVCATrace
{
	int iBufSize;					//structure size
	VCARule	stRule;
	vca_TDisplayParam stDisplayParam;			
	int					m_iStartPreset;			//The starting trace point
	vca_TPolygon		m_TrackRegion;			//Trace Region
	int					m_iScene;				//Scene. big/middle/small
	int					m_iWeekDay;				//weekday
	NVS_SCHEDTIME		m_timeSeg[MAX_DAYS][MAX_TIMESEGMENT];		//Trace time region
}VCATrace, *pVCATrace;

typedef struct tagVideoDetection
{
	int iBufSize;					//structure size
	VCARule	stRule;
	vca_TDisplayParam stDisplayParam;			
	int	m_iCheckTime;
}VideoDetection, *pVideoDetection;

typedef struct tagITSAlarmTypeInfo
{
	int			iEventType;				//Event Type
}ITSAlarmTypeInfo,*pITSAlarmTypeInfo;

typedef struct	tagClientCharset
{
	int		iCharSetType;		//1:utf-8 2:GB2312
}ClientCharset,*pClientCharset;

/**************************************以上结构体不再维护**************************************/
/************************The up structures are no longer maintained****************************/ 

typedef struct 
{
	int     iType;
	int     iStatus[MAX_DEVSTATUS_NUM];
}STR_DevStatus;
//add end

typedef struct
{
	int		 iSize;
	int      iStartHour;
	int      iStartMin;
	int      iStopHour;
	int      iStopMin;
	int      iRecordMode; 
}NVS_SCHEDTIME_Ex,*PNVS_SCHEDTIME_Ex;

typedef struct
{
    char    cEncoder[16]; 			//NVS IP，
    char    cClientIP[16];			//Client IP，
    int     iChannel;     			//Channel Number，
    int     iStreamNO;    			//Stream type
    int     iMode;        			//Network mode，1（TCP）， 2（UDP），3（multicast）
}LINKINFO, *PLINKINFO;

typedef struct
{
    int             iCount;             //Connect Count，
    unsigned int    uiID[MAX_ENCODER];  //ID
}UIDLIST, *PUIDLIST;

typedef struct
{
    char        m_cHostName[32];    //NVS host name
    char        m_cEncoder[16];     //NVS (IP)
    int         m_iRecvMode;        //Network mode
    char        m_cProxy[16];       //Proxy (IP)
    char        m_cFactoryID[32];   //ProductID
    int         m_iPort;            //NVS port
    int         m_nvsType;          //NVS type(NVS_T or NVS_S or DVR ...eg)

	int         m_iChanNum;         //encoder channel num
    int         m_iLogonState;      //encoder logon state 090414 zyp add
    int         m_iServerType;      //is the active mode device SERVER_ACTIVE, or passive mode device SERVER_NORMAL
}ENCODERINFO,*PENCODERINFO;

typedef struct tagEncoderInfoEx
{
	char        cHostName[LEN_32];		//NVS host name
	char        cNvsIpV4[LEN_16];		//NVS (IP)
	char        cNvsIpV6[LEN_64];		//NVS (IP)
	int         iRecvMode;				//Network mode
	char        cProxyIpV4[LEN_16];     //Proxy (IpV4)
	char        cProxyIpV6[LEN_64];     //Proxy (IpV6)
	char        cFactoryID[LEN_32];		//ProductID
	int         iPort;					//NVS port
	int         iNvsType;				//NVS type(NVS_T or NVS_S or DVR ...eg)
	int         iChanCount;				//encoder channel num
	int         iLogonState;			//encoder logon state
	int         iServerType;			//is the active mode device SERVER_ACTIVE, or passive mode device SERVER_NORMAL
	int			iIpVersion;				//0--IpV4，1--IpV6
} EncoderInfoEx, *pEncoderInfoEx;

typedef struct
{
    int             m_iServerID;        //NVS ID,NetClient_Logon Return value
    int             m_iChannelNo;       //Remote host to be connected video channel number (Begin from 0)
    char            m_cNetFile[255];    //Play the file on net, not used temporarily
    char            m_cRemoteIP[16];    //IP address of remote host
    int             m_iNetMode;         //Select net mode 1--TCP  2--UDP  3--Multicast
    int             m_iTimeout;         //Timeout length for data receipt
    int             m_iTTL;             //TTL value when Multicast
    int             m_iBufferCount;     //Buffer number
    int             m_iDelayNum;        //Start to call play progress after which buffer is filled
    int             m_iDelayTime;       //Delay time(second), reserve
    int             m_iStreamNO;        //Stream type
    int             m_iFlag;            //0, the first request for the video file; 1, operating video files
    int             m_iPosition;        //// 0 ~ 100, locate the file playback position; -1, do not locate ; effect when m_iChannelNo == (4×n～5×n-1);(0:NVR and IE request,1:Private platform request)
    int             m_iSpeed;           //1,2,4,8, control the file playback speed
}CLIENTINFO;

typedef struct tagNetPlayParam
{
	int                     iSize;
	void*                   pWnd;			//传入的显示句柄
	RECT                    rcShow;
	unsigned int            uiDecflag;
	int                     iWorkMode;		//Decoder working mode：0-->Single Thread: 1-->Multithread  
	int                     iThreadCount;	//Number of worker threads in multithreading mode, default is 4
	int                     iDecodeMode;	//解码模式：0--流式解码，1--帧解码
	int						iWndFlag;		//标识Wnd对象的属性: 0--QtWidget对象（默认），1--QtLayout对象
	int						iIsForbidShow;	//video render flag: 0-allow show，1-forbid show, 2-Continuous video rendering
							//Macro definition in GlobalTypes.h: ALLOW_VIDEO_RENDERING\FORBID_VIDEO_RENDERING\CONTINUOUS_VIDEO_RENDERING
}NetPlayParam;

//Hardware decode to show external data parameter
typedef struct
{
    int     m_iChannelNum;      //Decode card hardware channel number
    int     m_iVideoWidth;      //Video width
    int     m_iVideoHeight;     //Video height
    int     m_iFrameRate;       //Frame rate
    int     m_iDisPos;          //Video display position
    int     m_iAudioOut;        //whether to output audio
}DecoderParam;
   
// Callback function for the command word
typedef void (*PROXY_NOTIFY)(int _ulLogonID,int _iCmdKey, char* _cData,  int _iLen,void* _iUser);

//Network to receive the original data, not a complete data frame can be used for proxy forwarding
typedef void (*NVSDATA_NOTIFY)(unsigned int _ulID,unsigned char *_cData,int _iLen,void* _iUser);

#define STREAM_INFO_SYSHEAD 1 // System header data
#define STREAM_INFO_STREAMDATA 2 // video stream data (including composite stream and separate video and audio streaming video and audio data)

//Complete a frame of data, can be used to write video files, or send player to play. You want to distinguish the type of _ulStreamType
typedef void (__stdcall *FULLFRAME_NOTIFY)(unsigned int _ulID,unsigned int _ulStreamType,unsigned char *_cData,int _iLen,void* _iUser);
typedef void (__stdcall *FULLFRAME_NOTIFY_V4)(unsigned int _ulID, unsigned int _ulStreamType, unsigned char *_cData, int _iLen, void* _iUser, void* _iUserData); //_iUser：文件头数据，_iUserData：用户数据

// Download the data for download and play
typedef void (__stdcall *RECV_DOWNLOADDATA_NOTIFY)(unsigned int _ulID, unsigned char* _ucData,int _iLen, int _iFlag, void* _lpUserData);

typedef struct tagNetClientPara
{
	int						iSize;
	CLIENTINFO				tCltInfo;
	int						iCryptType;			//iCryptType = 0, no encryption; iCryptType = 1, is AES encryption
	char					cCryptKey[LEN_32];
	NVSDATA_NOTIFY			cbkDataArrive;		//Network to receive the original data,
	void*					pvUserData;
	int						iPicType;			//When iStreamNO is 3, bit0:Face picture stream bit1:pedestrian bit2:plate number bit3:motor vehicles bit4:Non-motor vehicle
												//When iStreamNO is 8, bit0-Radar target trajectory information, bit1-Transmission of algorithm results 
	FULLFRAME_NOTIFY_V4		pCbkFullFrm;		//full frame callback, it's private frame 
	void*					pvCbkFullFrmUsrData;
	RAWFRAME_NOTIFY			pCbkRawFrm;			//raw frame callback
	void*					pvCbkRawFrmUsrData;
	int						iIsForbidDecode;	//Synchronized blocking interface extension para: 0----allow decode, 1----forbid decode
	void*					pvWnd;				//Synchronized blocking interface extension para: the window handle of show video, if NULL can not show video
	int						iVideoRenderFlag;	//video render flag: 0-allow show，1-forbid show, 2-Continuous video rendering
												//Macro definition in GlobalTypes.h: ALLOW_VIDEO_RENDERING\FORBID_VIDEO_RENDERING\CONTINUOUS_VIDEO_RENDERING
	int						m_iBitRateFlag;		//Request compressed stream description: The default request raw stream, only the main stream (ZXYHDZ) 0- request raw stream; 1- Request a compressed stream
} NetClientPara, *pNetClientPara;


//===========================================================================
//  storage  add 2007.6.13
//===========================================================================

#define MAX_PAGE_SIZE       20
#define MAX_PAGE_LOG_SIZE	20

#define MAX_DH_TIMESEGMENT	8	// DH host time period

typedef int RECORD_TYPE;
#define RECORD_ALL			0xFF
#define RECORD_MANUAL 		1
#define RECORD_TIME	  		2
#define RECORD_ALARM  		3
#define RECORD_OTHER  		4

typedef int RECORD_STATE;
#define REC_STOP			0
#define REC_MANUAL			1
#define REC_TIME			2
#define REC_ALARM			3

typedef int AUDIO_ENCODER;
#define   G711_A              0x01  /* 64kbps G.711 A, see RFC3551.txt  4.5.14 PCMA */
#define   G711_U              0x02  /* 64kbps G.711 U, see RFC3551.txt  4.5.14 PCMU */
#define   ADPCM_DVI4          0x03  /* 32kbps ADPCM(DVI4) */
#define   G726_16KBPS         0x04  /* 16kbps G.726, see RFC3551.txt  4.5.4 G726-16 */
#define   G726_24KBPS         0x05  /* 24kbps G.726, see RFC3551.txt  4.5.4 G726-24 */
#define   G726_32KBPS         0x06  /* 32kbps G.726, see RFC3551.txt  4.5.4 G726-32 */
#define   G726_40KBPS         0x07  /* 40kbps G.726, see RFC3551.txt  4.5.4 G726-40 */
#define   MPEG_LAYER2         0x0E  /* Mpeg layer 2*/
#define   AMR_NB			  0x15
#define	  MPEG4_AAC			  0x16	/* MPEG-4 aac HEv2 ADTS*/
#define   ADPCM_IMA           0x23  /* 32kbps ADPCM(IMA) */
#define   MEDIA_G726_16KBPS   0x24  /* G726 16kbps for ASF ... */
#define   MEDIA_G726_24KBPS   0x25  /* G726 24kbps for ASF ... */
#define   MEDIA_G726_32KBPS   0x26  /* G726 32kbps for ASF ... */
#define   MEDIA_G726_40KBPS   0x27   /* G726 40kbps for ASF ... */


//Record File Property
typedef struct
{
    int             iType;          /* Record type 1-Manual record, 2-Schedule record, 3-Alarm record 1001-ipc Manual, 1002-ipc Timing, 1003-ipc alarm*/
    int             iChannel;       /* Record channel 0~channel defined channel number*/
    char            cFileName[250]; /* File name */
    NVS_FILE_TIME   struStartTime;  /* File start time */
    NVS_FILE_TIME   struStoptime;   /* File end time */
    int             iFileSize;      /* File size */
}NVS_FILE_DATA,*PNVS_FILE_DATA;

typedef struct
{
	int				iSize;
	NVS_FILE_DATA	tFileData;		//file basic information
	int			    iLocked;		//add unlock state
	int				iFileAttr;		//File attributes:0: nvr local storage; 1: nvr local ipc data; 2: all nvr local data; 10000: ipc storage
	int				iAttrNum;
	char            cRecAliasName[LEN_256]; /* Video Alias */
}NVS_FILE_DATA_EX,*PNVS_FILE_DATA_EX;

//Reserch parameter
typedef struct
{
    int             iType;          /* Record type 0xFF-All, 1-Manual record, 2-Schedule record, 3-Alarm record*/
    int             iChannel;       /* Record channel 0xFF-All, 0~channel-defined channel number*/
    NVS_FILE_TIME   struStartTime;  /* Start time of file */
    NVS_FILE_TIME   struStopTime;   /* End time of file */
    int             iPageSize;      /* Record number returned by each research*/
    int             iPageNo;        /* From which page to research */
    int             iFiletype;      /* File type, 0:all, 1:Video(only sdv), 2:picture, 3:mp4, 4:all video record(sdv+mp4)*/
    int             iDevType;       /* device type, 0-camera 1-network video server 2-network camera 0xff-all*/
}NVS_FILE_QUERY, *PNVS_FILE_QUERY;

typedef struct  
{
	NVS_FILE_QUERY	fileQuery;
	char			cOtherQuery[65];
	int				iTriggerType;		//alarm type 3: port alarm, 4: motion alarm, 5: video loss alarm, 0x7FFFFFFF: invalid
	int				iTrigger;			//port (channel) number
}NVS_FILE_QUERY_EX, *PNVS_FILE_QUERY_EX;

//Net storage device
typedef struct
{
    char cDeviceIP[LEN_16];
    char cStorePath[LEN_250];
    char cUsername[LEN_16];
    char cPassword[LEN_16];
    int  iState;
}NVS_NFS_DEV,*PNVS_NFS_DEV;

//Diskinfo
//disk manager
#define DISK_SATA_NUM		8
#define DISK_USB_NUM		4
#define	DISK_NFS_NUM		1
#define	DISK_ESATA_NUM		1
#define	DISK_SD_NUM			1
#define DISK_SATA_EX_NUM	8
#define DISK_SATA_NUM_MAX	100

#define DISK_VD_NUM			16
#define MAX_DISK_VD_COUNT	100
#define DISK_IPSAN_NUM		8

#define DISK_SBOD_NUM		3
#define SBOD_SATA_NUM		24

#define DNO_ESATA			32		// esata disk first no
#define DNO_SD				50		// SD disk first no

#define DISK_USB			8		//USB type disk 8 ~ 11
#define DISK_SATA_EX		1008	// SATA type disks 1008 to 1015
#define DISK_IPSAN			3000	// IPSAN type 3000 ~ 3008
#define DISK_VD				2000	//virtual disk disk number 2000 ~ 2015

typedef int DISK_STATUSTYPE;
#define DISK_ZERO			0
#define DISK_NOTFORMAT 		1
#define DISK_FORMATED  		2
#define DISK_CANUSE    		3
#define DISK_READING   		4


typedef struct
{
    unsigned int	m_u32TotalSpace; 	// Total size
    unsigned int	m_u32FreeSpace;  	// free space
    unsigned short  m_u16PartNum;    	//  number of partitions 1 to 4
    unsigned short  m_u16Status;     	// state 0, no disk; 1, unformatted; 2, formatted; 3, mounted; 4, read and write
    unsigned short  m_u16Usage;      	// use 0, video; 1, backup; 2, redundancy; 3, only read; 4, file mode
    unsigned short  m_u16Reserve;    	//	Reserved
}NVS_DISKINFO, *PNVS_DISKINFO;

// Quota structure
#define  CMD_DISK_QUOTA_MAX_USED  		1
#define  CMD_DISK_QUOTA_TOTAL			2
#define  CMD_DISK_QUOTA_CURRENT_USED	3
typedef struct
{
	int  iSize;							//structure size
	int  iRecordQuota; 					//Recording quota (unit: GB)
	int  iPictureQuota;					//Image quota (unit: GB)
}DISK_QUOTA;

typedef struct
{
	int  iSize;							//structure size
	char cRecordQuota[LEN_65]; 			//Used recording quota (unit: GB)
	char cPictureQuota[LEN_65];			//Used image quota (unit: GB)
}DISK_QUOTA_USED;
// Disk group structure
#define MAX_DISK_GROUP_NUM				16
#define MAX_DISK_GROUP_NUM_FOR_PLAYBACK			17
#define DISK_GROUP_ESATA_INDEX			10000
#define DISK_GROUP_DISK_PARAM_NUM				2
#define DISK_GROUP_DISK_PARAM_NUM_EX			4
#define DISK_GROUP_CHAN_PARAM_NUM				4
#define DISK_GROUP_CHAN_PARAM_NUM_EX			12
typedef struct
{
	int		iSize;												//structure size
	int		iGroupNO;											//disk group number, 0-7
	unsigned int uiDiskNO[DISK_GROUP_DISK_PARAM_NUM];			// disk number, said by the bit, can only choose 0-7 / / 32, from small to large, the corresponding bit from low to high
	unsigned int uiChannelNO[DISK_GROUP_CHAN_PARAM_NUM];		//channel number, bit by bit, channel number from small to large, corresponding to / / bit from low to high can be set up 128 channels
	unsigned int uiDiskNoEx[DISK_GROUP_DISK_PARAM_NUM_EX];		//Add the disk number
	unsigned int uiChannelNOEx[DISK_GROUP_CHAN_PARAM_NUM_EX];	//channel number, bitwise, channel number from small to large, corresponding to / / bit from low to high can be set up to 512 channels
	unsigned int uiDiskEsata;									//10000+
}DISK_GROUP;

// A single permission information structure
#define MAX_MODIFY_AUTHORITY_NUM		27		// The maximum number of permissions
typedef struct 
{
	int iLevel;									//  Permission number
	unsigned int uiList[4];						//Channel list array
}AUTHORITY_INFO;
// Single authority structure of the expansion of information to support the large road
typedef struct 
{
	int		iSize;
	char	cUserName[LEN_32];					//User name
	int		iLevel;								//Permission number
	unsigned int uiList[MAX_BITSET_COUNT];		//Channel list array
}AUTHORITY_INFO_V1;
// User permission structure
typedef struct
{
	int iNeedSize;								//When the user, iNeedSize on behalf of a set to modify the number of permissions, the maximum not more than 10
	AUTHORITY_INFO  strAutInfo[MAX_MODIFY_AUTHORITY_NUM];
}USER_AUTHORITY;

// User permission group structure
#define MAX_AUTHORITY_GROUP_NUM			5		// The number of permission groups
typedef struct
{
	int iSize;									//structure size
	int iGroupNO;								//Permission group number, 1 normal user; 2 privileged user;
	// 3 Super users; 4 Administrators
	unsigned int uiList[2];						//Permission number corresponding to this permission group,
	//Permission number from small to large, corresponding bit bit from low to high
	char  cReserved[256];						//Reserved
}GROUP_AUTHORITY;

typedef struct
{
    NVS_DISKINFO		m_strctSATA[DISK_SATA_NUM];
    NVS_DISKINFO		m_strctUSB[DISK_USB_NUM];
    NVS_DISKINFO		m_strctNFS[DISK_NFS_NUM];
    NVS_DISKINFO		m_strctESATA[DISK_ESATA_NUM];
    NVS_DISKINFO		m_strctSD[DISK_SD_NUM];
	NVS_DISKINFO		m_strctSATA_EX[DISK_SATA_EX_NUM];
	NVS_DISKINFO		m_tVD[DISK_VD_NUM];			// Add VD number 2000 to 2015
	NVS_DISKINFO		m_tIPSAN [DISK_IPSAN_NUM];		// Add IPSAN numbers from 3000 to 3007
	NVS_DISKINFO		m_tSata[DISK_SATA_NUM_MAX];
	NVS_DISKINFO		m_tSBOD[DISK_SBOD_NUM][SBOD_SATA_NUM];
	NVS_DISKINFO		m_tVdEx[MAX_DISK_VD_COUNT];	
}NVS_STORAGEDEV, *PNVS_STORAGEDEV;

typedef int SMTP_AUTHTYPE;
#define AUTH_OFF				0
#define AUTH_PLAIN				1
#define AUTH_CRAM_MD5			2
#define AUTH_DIGEST_MD5 		3
#define AUTH_GSSAPI				4
#define AUTH_EXTERNAL			5
#define AUTH_LOGIN				6
#define AUTH_NTLM				7
#define SMTP_AUTHTYPE_LAST		8

typedef int SCENE_TYPE;
#define AUTO					0
#define SCENE1					1
#define SCENE2					2
#define SCENE3 					3
#define SCENE4					4
#define SCENE5					5
#define SCENE6					6
#define SCENE7					7
#define SCENE8					8
#define SCENE9					9
#define SCENE10					10
#define SCENE11					11
#define SCENE_TYPE_LAST			12

//SMTP alarm info
typedef struct
{
    char            cSmtpServer[LEN_32];    	//smtp server
    unsigned short  wdSmtpPort;					//smtp server port
    char            cEmailAccount[LEN_32];  	//mail account
    char            cEmailPassword[LEN_32]; 	//mail password
    int             iAuthtype;					//authtype
    char            cEmailAddress[LEN_32];  	//mailbox address
    char            cMailSubject[LEN_32];   	//mail subject
}SMTP_INFO,*PSMTP_INFO;

#define MAX_SMTP_SERVER_NUM					2
typedef struct SMTP_INFOEX
{
	SMTP_INFO	smtpInfo;
	char		cMailAddr[3][LEN_32];		//	3 other addresses
	int			iScrityType;				//	0-retain，1-not scrity，2-SSL，3-TLS
	int			iSmtpNo;					//  0-SMTP1,  1-SMTP2
}*PSMTP_INFOEX;

typedef struct tagSMTPInfoEx
{
	int				iSize;
	char			cSmtpIp[LEN_128];
	int				iSmtpPort;
	char			cAccount[LEN_128];			//发送方邮箱地址
	char            cPassword[LEN_128];
	int             iAuthtype;
	char            cEmailDstAddr[LEN_128];		//接收方邮箱地址
	char            cEmailSubject[LEN_128];
	char			cEmailAddr[3][LEN_128];
	int				iScrityType;				//0-保留，1-不加密，2-SSL，3-TLS
	char			cUsrName[LEN_64];			//用户名
}SMTPInfoEx, *pSMTPInfoEx;
//add end 
//---------------------------------------------------------------------------

typedef struct
{
    char cESSID[33];            //ESSID
    char cEncryption[16];       //wifi encrypttion flag; 0 no encryption，1 digital encryption
}WIFI_INFO, *PWIFI_INFO;

typedef struct
{
    char    cESSID[LEN_33];         	//ESSID
    char    cWifiSvrIP[LEN_16];     	//wifiIP
    char    cWifiMask[LEN_16];          //wifi mask
    char    cWifiGateway[LEN_16];       //wifi gateway
    char    cWifiDNS[LEN_16];           //wifi DNS
    char    cWifiKeyType[LEN_16];       //wifi KeyType：Hex；ASCII
    char    cWifiPassword[LEN_128];     //wifi password, @yys@, 32->128, 2010-07-05
    char    cEncryption[LEN_16];        //wifi encrypttion flag; NULL no encryption，"WEP" encryption
    char    cWifiKeyNum[LEN_2];         //wifi KeyNum (1,2,3,4)
    int     iPwdFormat;
    int     iHaveCard;
    int     iCardOnline;
    char    cPcVersion[LEN_32];         // WiFi module version 
    int     iCountry;                   // 0:OurCountry,1:North America,2:Japan,3:Europe,4:Other
    int     iChannel;                   // 0:Auto，1~13:channel 1～channel 13
    int     iBandWidth;                 // 0:20M/40M self-adaption;1:Fixed 20M bandwidth
    char    cEncryptionExtend[LEN_256]; // wifi encrypttion extend
    char	cMac[LENGTH_MAC_ADDR];      // Mac addr, just getting, Default 0
}NVS_WIFI_PARAM, *PNVS_WIFI_PARAM;

// Compatible with IE
//wifiap
//typedef struct
//{
//	char	cESSID[64];			    //ESSID!
//	char	cWifiSvrIP[16];		    //wifiapIP
//	char	cWifiMask[16];			//wifiap mask
//	char	cWifiGateway[16];		//wifiap gateway
//	char	cWifiDNS[16];			//wifiap DNS
//	char	cWifiKeyType[16];		//wifiap KeyType：Hex；ASCII
//	char	cWifiPassword[128];		//wifiap password,
//	char	cEncryption[16];		//wifiap encrypttion flag; NULL no encryption，"WEP" encryption
//	char	cWifiKeyNum[2];         //wifiap KeyNum (1,2,3,4)
//	int		iPwdFormat;
//	int		iHaveCard;
//	int		iCardOnline;
//}NVS_WIFIAP_PARAM, *PNVS_WIFIAP_PARAM;

// Compatible with SDK 4.0 and 3.3
typedef struct NVS_IPAndID
{
    char *m_pIP;
    char *m_pID;
	union
	{
		unsigned int *m_puiLogonID;
		int  *m_piLogonID;
	};
}*pNVS_IPAndID;

typedef struct MainMsgPara
{
	int iSize;
	int	iLogonId;
	int	iChanNo;
	int iStreamNo;
	int iValue;
}MainMsgPara, *pMainMsgPara;

//--------------------------------------------------------

#ifndef HIWORD
#define HIWORD(l)   ((unsigned short) (((unsigned int) (l) >> 16) & 0xFFFF))
#endif

#ifndef LOWORD
#define LOWORD(l)   ((unsigned short) (l))
#endif


#define SERVER_NORMAL  				0
#define SERVER_ACTIVE  				1
#define SERVER_DNS					2
#define SERVER_FIND_PSW				3
#define SERVER_REG_ACTIVE			4
#define SERVER_NORMAL_IPV6			5

#define DATA_RECEIVE_NOT		0 				// No reception
#define DATA_RECEIVE_WAIT		1 				// Wait for reception
#define DATA_RECEIVE_HEAD		2 				// has received the video header, but has not yet received the data
#define DATA_RECEIVE_ING		3 				// receiving ing
#define DATA_RECEIVE_DIGITALCHANNEL_FAILED	4	// The digital channel is not connected

#define INFO_USER_PARA     		0
#define INFO_CHANNEL_PARA  		1
#define INFO_ALARM_PARA    		2
#define INFO_VIDEO_HEAD    		3
#define INFO_LOGON_PARA    		4
#define INFO_FINISH_PARA   		5

typedef void (*CMDSTRING_NOTIFY)(char *_cBuf,int _iLen,void* _pUserData); 
  
#define PROTOCOL_COUNT			64				//	The maximum number of protocols supported
#define PROTOCOL_NAME_LENGTH	32				//	Maximum length of a single protocol name
#define MAX_PROTOCOL_TYPE		3
typedef struct
{
	int     iType;				//Integer, 0 represents the protocol supported by the device, and 1 represents the protocol being used
	int		iCount;				//Number of protocol types supported by the server
	char 	cProtocol[PROTOCOL_COUNT][PROTOCOL_NAME_LENGTH];	//name of each protocol, up to the protocol, the longest byte of each protocol name;
} st_NVSProtocol;  

#ifdef WIN32
typedef struct
{
	char m_cIP[32];
	char m_cDeviceID[64];
	int  m_iLogonID;
	int  m_iChannel;
	int  m_iStreamNO;
	int  m_iDrawIndex;
}st_ConnectInfo,*pst_ConnectInfo;
#else
typedef struct
{
	char m_cIP[32];
	char m_cDeviceID[64];
	int  m_iLogonID;
	int  m_iChannel;
	int  m_iStreamNO;
	int  m_iDrawIndex;
	char m_cDownLoadFile[256];
}st_ConnectInfo,*pst_ConnectInfo;
#endif


#define CLIENT_DEFAULT 0 // Connect to the agent's default client
#define CLIENT_DECODER 1 // Special handling of the decoder

// # define MAX_DATA_CHAN MAX_CHANNEL_NUM // contains the sub-stream
#define MAX_FILE_CHAN 16

#define DATA_CMD  1
#define DATA_DATA 2
typedef void (*INNER_DATA_NOTIFY)(unsigned int _ulID,void *_pBlock,int _iType,void* _iUser);

#define REC_FILE_TYPE_STOP			(-1)
#define REC_FILE_TYPE_NORMAL		0
#define REC_FILE_TYPE_AVI			1
#define REC_FILE_TYPE_ASF			2
#define REC_FILE_TYPE_AUDIO			3
#define REC_FILE_TYPE_RAWAAC		4
#define REC_FILE_TYPE_VIDEO			5
#define REC_FILE_TYPE_MP4			6	//Stop using
#define REC_FILE_TYPE_PS			8
#define REC_FILE_TYPE_TS			9
#define REC_FILE_TYPE_ZFMP4			10

typedef struct						//  2010-1-26-18:09 @yys@
{
	int			iChannelNo;			//	channel number
	int			iLogType;			//	Log type
	int			iLanguage;			//	language type
NVS_FILE_TIME	struStartTime;		//	start time
NVS_FILE_TIME	struStopTime;		//	End time
	int			iPageSize;			//	page size
	int			iPageNo;			//	page number
}NVS_LOG_QUERY, *PNVS_LOG_QUERY;

typedef struct  
{
	int				iChannel;
	int				iLogType;
	NVS_FILE_TIME	struStartTime; 	/* File start time */
	NVS_FILE_TIME	struStoptime; 	/* File end time */
	char			szLogContent[129];					
}NVS_LOG_DATA,*PNVS_LOG_DATA;

#define DOWNLOAD_TYPE_NOTHING		0
#define DOWNLOAD_TYPE_ERROR			1
#define DOWNLOAD_TYPE_PIC			2
#define DOWNLOAD_TYPE_VIDEO			3
#define DOWNLOAD_TYPE_TIMESPAN		4

//Inner Player Start
//play back control CMD
#define		PLAY_CONTROL_PLAY				1				//play start
#define		PLAY_CONTROL_PAUSE				2				//play pause
#define     PLAY_CONTROL_SEEK				3
#define		PLAY_CONTROL_FAST_FORWARD		4				//play fast
#define		PLAY_CONTROL_SLOW_FORWARD		5				//play slow
#define     PLAY_CONTROL_OSD				6				//Add OSD
#define		PLAY_CONTROL_GET_PROCESS		7				//get process
#define		PLAY_CONTROL_START_AUDIO		8				//PlayAudio
#define		PLAY_CONTROL_STOP_AUDIO			9				//Stop Audio

#define     PLAY_CONTROL_SET_VOLUME			10				//SetVolume
#define     PLAY_CONTROL_GET_VOLUME			11				//Get Volume

#define     PLAY_CONTROL_STEPFORWARD		12				//stepforward
#define		PLAY_CONTROL_START_CAPTUREFILE	13				//Capture File
#define		PLAY_CONTROL_STOP_CAPTUREFILE	14				//Capture File

#define		PLAY_CONTROL_START_CAPTUREPIC	15				//Capture Pic
#define		PLAY_CONTROL_PLAYRECT			16				//Play Rect
#define		PLAY_CONTROL_GETUSERINFO		17				//Get UserInfo
#define		PLAY_CONTROL_SETDECRYPTKEY		18				//SetVideoDecryptKey
#define		PLAY_CONTROL_GETFILEHEAD		19				//get video param
#define		PLAY_CONTROL_SETFILEHEAD		20				//set file header
#define		PLAY_CONTROL_GETFRAMERATE		21				//get frame rate
#define		PLAY_CONTROL_SYC_ADD			22				//add player to synch group 
#define		PLAY_CONTROL_SYC_DEL			23				//delete player from synch group
#define		PLAY_CONTROL_GET_PLAYER_INFO	24				//Get Player info
#define		PLAY_CONTROL_GET_BEGIN_TIME		25				//Get Playback begin time, DZ functionnot supported
#define		PLAY_CONTROL_GET_END_TIME		26				//Get Playback end time, DZ functionnot supported

//PlayBack Type
#define PLAYBACK_TYPE_FILE					1
#define PLAYBACK_TYPE_TIME					2

typedef struct PlayerParam
{
	int				iSize;
	char			strFileName[LEN_128];	//previous device file name
	int				iLogonID;				//remote file use
	int				iChannNo;				//remote time use
	NVS_FILE_TIME	tBeginTime;
	NVS_FILE_TIME	tEndTime;
	//up is first edition(lc) old struct
	int				iPlayerType;
	//down is new function 
	char			cLocalFilename[LEN_256];//local record file name
	int				iPosition;			    //file position,by 0%～100%;continue download，the pos of the file request
	int				iSpeed;					//1，2，4，8，control file play speed, 0-pause
	int				iIFrame;			    //juest send I frame 1,just play I frame;0,all play				
	int				iReqMode;				//need data mode 1,frame mode;0,stream mode,2，stream mode V2					
	int				iRemoteFileLen;		    //if local file is not NULL，this param set to NULL
	int				iVodTransEnable;		//enable
	int				iVodTransVideoSize;	    //Video resolution
	int				iVodTransFrameRate;     //frame rate
	int				iVodTransStreamRate;    //stream rate
	RawFrameNotifyInfo tRawNotifyInfo;		//raw frame notify with play control info
	int				iFileAttr;
    int             iPlaybackDirection;
	int				iVideoRenderFlag;	//video render flag: 0-allow show，1-forbid show, 2-Continuous video rendering
										//Macro definition in GlobalTypes.h: ALLOW_VIDEO_RENDERING\FORBID_VIDEO_RENDERING\CONTINUOUS_VIDEO_RENDERING
	int				m_iBitRateFlag;						//请求压缩流 说明: 默认请求原始流, 只有主码流和VOD需要处理该字段（ZXYHDZ）0-请求原始流; 1-请求压缩流 
	//2. 对于VOD，首次请求命令（DOWNLOAD_CMD_FILE 和DOWNLOAD_CMD_TIMESPAN ）生效，其他命令忽略
	int				iFilterEnable;			//0：录像类型不使能，1：录像类型使能，控制m_RecTypeStyley和m_RecTypeSub0~n.
	int				m_RecTypeStyle;			//录像类型大类，按位表示：bit0:录像类型小类使能, bit1:定时, bit2:全部报警, bit3:手动
	int				m_RecTypeSub0;			//录像类型小类, RecTypeStyle bit0=1时有效，按位表示：
	//bit0:端口报警, bit1:移动侦测, bit2:视频丢失, bit3:视频遮挡, bit4:智能分析, bit5:ANR录像, bit6:目标人报警，bit7:目标车报警，bit8:三智报警
	int				m_iDiskGroup;		//盘组编号：1~16
}PlayerParam;

typedef struct _tagPlayBackOsd
{
	int iSize;
	int iX;
	int iY;
	char cOsdText[96];
	int iColor;		//fefault -1 red
}tPlayBackOsd;

typedef struct _tagPlaybackProcess
{
	int iSize;
	int iPlayByFileOrTime;
	int iPlayedFrameNum;
	int iTotalFrame;
	int iProcess;
	unsigned int uiCurrentPlayTime;
	unsigned int uiCurrentAbsTime;
	unsigned int uiFrameNoDiff;
}tPlaybackProcess;

typedef struct _tagPlaybackVolume
{
	int iSize;
	unsigned short usVolume;
}tPlaybackVolume;

//typedef struct _tagPlaybackVlume
//{
//	int iSize;
//	int iSetOrGet;
//	int iVolume;
//}tPlaybackVlume;

typedef struct _tagPlaybackCapture
{
	int		iSize;
	char	cFileName[LEN_256];	
	int		iRecFileType;
}tPlaybackCapture;

typedef struct _tagDecryptKey
{
	int		iSize;
	char	cKey[LEN_128];
	int		ikeySize;
}tDecryptKey;

typedef struct _tagPlayBackSyc
{
	int		iSize;
	int		iGroupId;
}PlayBackSyc, *pPlayBackSyc;

//typedef struct _tagVideoParam
//{
//	int iSize;
//	int iWidth;
//	int iHeight;
//	int iFrameRate;
//}tVideoParam;

//Inner Player End

typedef struct st_PTZCruise
{
	int iPreset[32];				//Preset number range: 1-255	
	int	iStayTime[32];				//Dwell time in the range of 1-60	
	int	iSpeed[32];					//Speed	   range：0-100	
}st_PTZCruise,*Pst_PTZCruise;

/************************************************************************
* CD-Burn related settings
************************************************************************/

#define CDBURN_STATUS_FREE			0		// The disc is not used
#define CDBURN_STATUS_BUSY			1		// Burning
#define CDBURN_STATUS_BAD			2		// The disc is damaged
#define CDBURN_STATUS_FULL			3		// The disc is full
#define CDBURN_STATUS_PACK			4		// The disc is being packaged
#define CDBURN_STATUS_BEGINFAILED	102		// Failed to start recording

//Burn file mode:0-original mode,1-single file mode
#define BURN_FILE_ORIGINAL_MODE			0
#define BURN_FILE_SINGLE_FILE_MODE		1

typedef struct TCBurnStopMode
{
	int             iDeviceNum;                            //CD-ROM number
	int             iFlagByBits;                            // bit0: 1 do not bombs, 0 bombs; bit1: 1 is not closed,
}TCBurnStopMode, CDBurnCtrlPara;       

/************************************************************************
*	 Video Encrypt & Decrypt                                                                      
************************************************************************/
typedef struct TVideoEncrypt
{
	int			iChannel;					//	encrypted channel, -1: that all channels
	int			iType;						//	encryption type, - 0, not encrypted; 1, AES; 2, AES+XOR other to be extended
	int			iMode;						//	encryption algorithm or method, currently less than
	char		cPassword[17];				//	Encryption password
}TVideoEncrypt;

typedef struct TVideoDecrypt
{
	int			iChannel;							//	decrypt the channel
	char		cDecryptKey[17];					//	decryption key
	int			iStreamNo;							//	stream type
}TVideoDecrypt;

/************************************************************************
   Digital Channel Param  20110303                                                                   
************************************************************************/
#define DC_CMD_GET_MIN				0
#define DC_CMD_GET_ALL				(DC_CMD_GET_MIN+0)
#define DC_CMD_GET_IPCPnP			(DC_CMD_GET_MIN+2)
#define DC_CMD_GET_PARAM			(DC_CMD_GET_MIN+3)
#define DC_CMD_GET_MAX				(DC_CMD_GET_MIN+4)

#define DC_CMD_SET_MIN				(0)
#define DC_CMD_SET_ALL				(DC_CMD_SET_MIN+1)
#define DC_CMD_SET_IPCPnP			(DC_CMD_SET_MIN+2)
#define DC_CMD_SET_PARAM			(DC_CMD_SET_MIN+3)
#define DC_CMD_SET_MAX				(DC_CMD_SET_MIN+4)
/************************************************************************
   Digital channel plug-and-play operation
***********************************************************************/
#define DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_MIN         0
#define DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_OFF (DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_MIN + 0) // off
#define DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_AUTO_ADD (DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_MIN + 1) // Automatically add
#define DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_AUTO_FIND (DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_MIN + 2) // Autodiscovery (prompts the user to add it manually)
#define DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_MAX        (DIGITAL_CHANNEL_PLUGANDPLAY_WORKMODE_MIN + 0)

//  [12/15/2012]
#define	MAX_RTSPURL_LEN				(127)
#define MAX_RTSPURL_LEN_EX			(63)

typedef struct TDigitalChannelParam
{
	int iChannel;							//	// [digital channel number] - The value range depends on the device type. When sending the digital channel number to the client
	int	iEnable;							//	[Enable] - 0, disable the digital channel; 1, enable the digital channel, the default is enabled
	int iConnectMode;						//	[connection] - 0:IP; 1:domain name; 2:active mode; 3:IPV6; 4:EasyDDNS
	int	iDevChannel;						//	 [Front End Device Channel Number] - The value range has been set by the front end device capability, default is 0
	int iDevPort;							//	[Front End Device Port Number] - 81 ~ 65535, the default is 3000
	int iStreamType;						//	[stream type] - 0, primary stream; 1, secondary stream, the default is the main stream
	int iNetMode;							//	[network type] - 1, TCP; 2, UDP; 3, multicast, tentative for the TCP, can not be changed
	int iPtzAddr;							//	[PTZ address] - 0 ~ 256, the default is used

	char cIP[33];							//	[connection parameter] - iConnectMode = 0 when the IP address; iConnectMode = 1 when the domain name; iConnectMode = 2, the device ID, default is empty, up to 32 characters
	char cProxyIP[33];						//	[Proxy IP] - This field is invalid when iConnectMode = 2, default is empty, up to 32 characters
	char cPtzProtocolName[33];				//	[[PTZ Protocol] - When sending a PTZ protocol, the device sends a list of supported PTZ protocols to the client. When the device is empty, the default value is 32 characters
	char cUserName[17];						//	[user name] - The user name of the login front-end device, not null, up to 16 characters
	char cPassword[17];						//	[password] - Password for logging in to the frontend device, not null, up to 16 characters
	char cEncryptKey[17];					//	[Video Encryption Password] - The password of the front-end device video encryption. If it is empty, it means no encryption. Up to 16 characters
	int iServerType;						//   front-end equipment used in the network protocol (Miracle new field)
// 0: Private protocol
// 1: Onvif protocol;
// 2: Push stream
											//	3:RTSP
	union
	{
		struct 
		{
			char cRTSPUrl[MAX_RTSPURL_LEN+1];		//	RTSP url
			char cIPv6[LENGTH_IPV6_V1];				//	IPv6  address
		};
		char cRTSPUrlEx[4*MAX_RTSPURL_LEN_EX+4];
	};
	
	char cMucIp[LEN_64];	//muticast IP address
	int  iMucPort;			//muticast port
	int  iActivation;		//Active state
	int  iSynchro;			//synchronize mail or cell phone number to the front 0:no synchronization 1:synchronization
	char cIpcMac[LEN_32];
	char cProxyIPv6[LENGTH_IPV6_V1];
    char cOnvifUrl1[LEN_256];               //当iServerType=1且iConnectMode=1时，表示onvif前端的域名，至多128个字符，默认发空
}TDigitalChannelParam;

typedef struct tagDigitalChnPara
{
	int iSize;								//struct of size
	int iChannel;							//[digital channel number] - The value range depends on the device type. When sending the digital channel number to the client
	int	iEnable;							//[Enable] - 0, disable the digital channel; 1, enable the digital channel, the default is enabled
	int iConnectMode;						//[connection] - 0:IP; 1:domain name; 2:active mode; 3:IPV6; 4:EasyDDNS
	int	iDevChannel;						//[Front End Device Channel Number] - The value range has been set by the front end device capability, default is 0
	int iDevPort;							//[Front End Device Port Number] - 81 ~ 65535, the default is 3000
	int iStreamType;						//[stream type] - 0, primary stream; 1, secondary stream, the default is the main stream
	int iNetMode;							//[network type] - 1, TCP; 2, UDP; 3, multicast, tentative for the TCP, can not be changed
	int iPtzAddr;							//[PTZ address] - 0 ~ 256, the default is used
	char cIP[LEN_32 + 1];					//[connection parameter] - iConnectMode = 0 when the IP address; iConnectMode = 1 when the domain name; iConnectMode = 2, the device ID, default is empty, up to 32 characters
	char cProxyIP[LEN_32 + 1];				//[Proxy IP] - This field is invalid when iConnectMode = 2, default is empty, up to 32 characters
	char cPtzProtocolName[LEN_32 + 1];		//[[PTZ Protocol] - When sending a PTZ protocol, the device sends a list of supported PTZ protocols to the client. When the device is empty, the default value is 32 characters
	char cUserName[LEN_64 + 1];				//[user name] - The user name of the login front-end device, not null, up to 16 characters
	char cPassword[LEN_64 + 1];				//[password] - Password for logging in to the frontend device, not null, up to 16 characters
	char cEncryptKey[LEN_32 + 1];			//[Video Encryption Password] - The password of the front-end device video encryption. If it is empty, it means no encryption. Up to 16 characters
	int iServerType;						//front-end equipment used in the network protocol (Miracle new field)
											//0:Private protocol, 1:Onvif protocol, 2:Push stream, 3:RTSP
	char cRTSPUrl[LEN_256];					//RTSP url
	char cIPv6[LEN_64];						//IPv6  address
	char cMucIp[LEN_64];					//muticast IP address
	int  iMucPort;							//muticast port
	int  iActivation;						//Active state
	int  iSynchro;							//synchronize mail or cell phone number to the front 0:no synchronization 1:synchronization
	char cIpcMac[LEN_32];
	char cProxyIPv6[LENGTH_IPV6_V1];
    char cOnvifUrl1[LEN_256];               //当iServerType=1且iConnectMode=1时，表示onvif前端的域名，至多128个字符，默认发空
} DigitalChnPara, *pDigitalChnPara;

/************************************************************************
* Get the attribute parameters of the device channel
************************************************************************/
#define CHANNEL_TYPE_LOCAL					0 		// local analog channel
#define CHANNEL_TYPE_DIGITAL				2 		// Digital channel
#define CHANNEL_TYPE_COMBINE				3 		// Synthesize the channel
#define CHANNEL_TYPE_FISHEYE				4 		// fish eye channel
#define CHANNEL_TYPE_FULLVIEW				9999	// full view channel

#define CHANNEL_TYPE_MAINSTREAM				0 // Main stream
#define CHANNEL_TYPE_SUBSTREAM				1 // Secondary stream
#define CHANNEL_TYPE_THIRDSTREAM			4 // Three streams
#define	CHANNEL_TYPE_AUDIO					7 //  Audio Channel

#define CHANNEL_TYPE_VIDEO					0 // Video channel
#define CHANNEL_TYPE_ALARMIN				1 // Alarm in channel
#define CHANNEL_TYPE_ALARMOUT				2 // Alarm out channel

#define GENERAL_CMD_MIN						0
#define GENERAL_CMD_GET_CHANNEL_TYPE		(GENERAL_CMD_MIN + 1) // get the property of a channel of this device (old)
#define GENERAL_CMD_CHANNEL_TYPE			(GENERAL_CMD_MIN + 1) // get the properties of a channel of this device (new)
#define GENERAL_CMD_CHANTYPE_LIST			(GENERAL_CMD_MIN+2)			//	get the device can edit the type of each channel
#define GENERAL_CMD_ALARM_IN_CHANNEL_TYPE   (GENERAL_CMD_MIN+3)			//	get the equipment of the alarm input channel type
#define GENERAL_CMD_MAX						(GENERAL_CMD_MIN+4)

/************************************************************************
* VCA-related data structure for the upper software, the following CMDID use the best enum (sizeof (enum)),
But taking into account the CB and VC compatibility issues, the use of unified # define
************************************************************************/
#define VCA_MAX_RULE_NUM					8 // The maximum number of rules allowed per channel
#define VCA_MAX_RULE_NUM_V2			        16
#define VCA_MAX_EVENT_NUM					14 // The maximum number of events
#define VCA_MAX_TRIPWIRE_NUM				8 // The maximum number of single trip lines
#define VCA_MAX_DBTRIPWIRE_NUM				4 // The maximum number of double trip lines
//
#define VCA_MAX_OSC_REGION_NUM				3 // The maximum number of missing items

#define VCA_MAX_RULE_NAME_LEN				17 // The length of the rule name

//ALARM_TYPE_FACE_IDENT IPC face recognition, Sub alarm type
#define VCA_IPC_EVENT_MIN					0
#define VCA_IPC_EVENT_FACE_IDENT_COMPARE	(VCA_NVR_EVENT_MIN + 0)	//Compare(Blacklist)
#define VCA_IPC_EVENT_FACE_IDENT_STRANGER	(VCA_NVR_EVENT_MIN + 1)	//Stranger(Whitelist)
#define VCA_IPC_EVENT_MAX					(VCA_NVR_EVENT_MIN + 2)	

//ALARM_TYPE_NVR_VCA NVR face recognition, Sub alarm type
#define VCA_NVR_EVENT_MIN					0
#define VCA_NVR_EVENT_FACE_DETECE			(VCA_NVR_EVENT_MIN + 0)	//face detece
#define VCA_NVR_EVENT_FACE_IDENT_COMPARE	(VCA_NVR_EVENT_MIN + 1)	//face compare
#define VCA_NVR_EVENT_FACE_IDENT_STRANGER	(VCA_NVR_EVENT_MIN + 2)	//stranger
#define VCA_NVR_EVENT_FACE_IDENT_FREQUENCE	(VCA_NVR_EVENT_MIN + 3)	//frequence
#define VCA_NVR_EVENT_FACE_IDENT_RETENTION	(VCA_NVR_EVENT_MIN + 4)	//retention
#define VCA_NVR_EVENT_BEHAVIORAL_BORDER		(VCA_NVR_EVENT_MIN + 5)	//behavioral analysls Border 
#define VCA_NVR_EVENT_BEHAVIORAL_TRIPLINE	(VCA_NVR_EVENT_MIN + 6)	//behavioral analysls trip line 
#define VCA_NVR_EVENT_INTELLIGENT_DETECTION	(VCA_NVR_EVENT_MIN + 7)	//intelligent detection
#define VCA_NVR_EVENT_CPC_AREA_INFINITY		(VCA_NVR_EVENT_MIN + 8)	//area pepole count
#define VCA_NVR_EVENT_HUMANOID_DETECT		(VCA_NVR_EVENT_MIN + 9 // Humanoid detect
#define VCA_NVR_EVENT_VEHICLE_DETECT		(VCA_NVR_EVENT_MIN + 10) // Vehicle detect
#define VCA_NVR_EVENT_NON_MOTOR_DETECT		(VCA_NVR_EVENT_MIN + 11) // Non-motor vehicle detect
#define VCA_NVR_EVENT_PLATE_BLACKLIST		(VCA_NVR_EVENT_MIN + 12) // The license plate blacklist matches
#define VCA_NVR_EVENT_PLATE_WHITELIST		(VCA_NVR_EVENT_MIN + 13) // The license plate whitelist matches
#define VCA_NVR_EVENT_MAX					(VCA_NVR_EVENT_MIN + 14)

#define VCA_CMD_SET_MIN						1 // VCA sets the lower limit of the command
#define VCA_CMD_SET_CHANNEL					(VCA_CMD_SET_MIN + 0) // Set VCA channel number and enable
#define VCA_CMD_SET_VIDEOSIZE				(VCA_CMD_SET_MIN + 1) // Set the video size of the VCA channel
#define VCA_CMD_SET_ADVANCE_PARAM			(VCA_CMD_SET_MIN + 2) // Set VCA advanced parameters
#define VCA_CMD_SET_TARGET_PARAM			(VCA_CMD_SET_MIN + 3) // Set VCA overlay parameters and color settings
#define VCA_CMD_SET_ALARM_STATISTIC			(VCA_CMD_SET_MIN + 4) // Set the VCA alarm count to zero
#define VCA_CMD_SET_RULE_COMMON				(VCA_CMD_SET_MIN + 5) // Set VCA Rule General Parameters
#define VCA_CMD_SET_RULE0_TRIPWIRE			(VCA_CMD_SET_MIN + 6) // Set VCA rule (single trip line)
#define VCA_CMD_SET_RULE1_DBTRIPWIRE		(VCA_CMD_SET_MIN + 7) // Set VCA rule (double trip line)
#define VCA_CMD_SET_RULE2_PERIMETER			(VCA_CMD_SET_MIN + 8) // Set VCA rule (perimeter)
#define VCA_CMD_SET_ALARM_SCHEDULE			(VCA_CMD_SET_MIN + 9) // Set the VCA alarm template
#define VCA_CMD_SET_ALARM_LINK				(VCA_CMD_SET_MIN + 10) // Set VCA linkage output port
#define VCA_CMD_SET_SCHEDULE_ENABLE			(VCA_CMD_SET_MIN + 11) // Set VCA Arming Enable
#define VCA_CMD_SET_RULE9_FACEREC			(VCA_CMD_SET_MIN + 12) // Set VCA Rule (Face Recognition)
#define VCA_CMD_SET_RULE10_VIDEODETECT		(VCA_CMD_SET_MIN + 13) // Set VCA Rule (Video Diagnostics)
#define VCA_CMD_SET_RULE8_ABANDUM			(VCA_CMD_SET_MIN + 14) // Set VCA rule (discard)
#define VCA_CMD_SET_RULE7_OBJSTOLEN			(VCA_CMD_SET_MIN + 15) // Set VCA rule (stolen)
#define VCA_CMD_SET_RULE11_TRACK			(VCA_CMD_SET_MIN + 16) // Set the VCA rule (trace)
#define VCA_CMD_CALL_TRACK_NO				(VCA_CMD_SET_MIN + 17) // Call the start trace bit
#define VCA_CMD_SET_RULE12_FLUXSTATISTIC	(VCA_CMD_SET_MIN + 18) // Set the VCA rule (traffic statistics)
#define VCA_CMD_SET_RULE13_CROWD			(VCA_CMD_SET_MIN + 19) // Set VCA rule (crowd)
#define VCA_CMD_SET_CHANNEL_ENABLE			(VCA_CMD_SET_MIN + 20) // set VCA channel enable (0: do not enable intelligent analysis 1: enable local channel intelligent analysis 2: enable front-end channel intelligent analysis)
#define VCA_CMD_SET_RULE14_LEAVE_DETECT		(VCA_CMD_SET_MIN + 21) // Set VCA rule (out-of-bound detection)
#define VCA_CMD_SET_RULE15_WATER_LEVEL		(VCA_CMD_SET_MIN + 22) // Set VCA rule (water level detection)
#define VCA_CMD_SET_MAX						(VCA_CMD_SET_MIN + 23) // VCA Set command limit

#define VCA_CMD_GET_MIN						(16) // VCA read command lower limit, in order to be compatible with VIDEODETECT before the macro
#define VCA_CMD_GET_CHANNEL					(VCA_CMD_GET_MIN + 0) // read VCA channel number and enable
#define VCA_CMD_GET_VIDEOSIZE				(VCA_CMD_GET_MIN + 1) // Read the video size of the VCA channel
#define VCA_CMD_GET_ADVANCE_PARAM			(VCA_CMD_GET_MIN + 2) // Read VCA Advanced Parameters
#define VCA_CMD_GET_TARGET_PARAM			(VCA_CMD_GET_MIN + 3) // read VCA overlay and color settings
#define VCA_CMD_GET_ALARM_STATISTIC			(VCA_CMD_GET_MIN + 4) // read VCA alarm count
#define VCA_CMD_GET_RULE_COMMON				(VCA_CMD_GET_MIN + 5) // Read VCA Rule General Parameters
#define VCA_CMD_GET_RULE0_TRIPWIRE			(VCA_CMD_GET_MIN + 6) // read VCA rule (single trip line)
#define VCA_CMD_GET_RULE1_DBTRIPWIRE		(VCA_CMD_GET_MIN + 7) // read VCA rule (double trip line)
#define VCA_CMD_GET_RULE2_PERIMETER			(VCA_CMD_GET_MIN + 8) // read VCA rule (perimeter)
#define VCA_CMD_GET_ALARM_LINK				(VCA_CMD_GET_MIN + 9) // read VCA linkage output port
#define VCA_CMD_GET_ALARM_SCHEDULE			(VCA_CMD_GET_MIN + 10) // read VCA alarm template
#define VCA_CMD_GET_SCHEDULE_ENABLE			(VCA_CMD_GET_MIN + 11) // Read VCA Arming Enable
#define VCA_CMD_GET_RULE9_FACEREC			(VCA_CMD_GET_MIN + 12) // NonSupport!!!
#define VCA_CMD_GET_RULE10_VIDEODETECT		(VCA_CMD_GET_MIN + 13) // NonSupport!!!
#define VCA_CMD_GET_RULE8_ABANDUM			(VCA_CMD_GET_MIN + 14) // Set VCA rule (discard)
#define VCA_CMD_GET_RULE7_OBJSTOLEN			(VCA_CMD_GET_MIN + 15) // Set VCA rule (stolen)
#define VCA_CMD_GET_RULE11_TRACK			(VCA_CMD_GET_MIN + 16) // NonSupport!!!
#define VCA_CMD_GET_RULE12_FLUXSTATISTIC	(VCA_CMD_GET_MIN + 18) // NonSupport!!!
#define VCA_CMD_GET_RULE13_CROWD			(VCA_CMD_GET_MIN + 19) // Set VCA rule (crowd)
#define VCA_CMD_GET_CHANNEL_ENABLE			(VCA_CMD_GET_MIN + 20) // read VCA channel enable (0: do not enable intelligent analysis 1: enable local channel intelligence analysis 2: enable front-end channel intelligent analysis)
#define VCA_CMD_GET_STATISTIC_INFO			(VCA_CMD_GET_MIN + 21) // read the VCA channel person traffic statistics
#define VCA_CMD_GET_RULE14_LEAVE_DETECT		(VCA_CMD_GET_MIN + 22) // read VCA rule (out of position detection)
#define VCA_CMD_GET_RULE15_WATER_LEVEL		(VCA_CMD_GET_MIN + 23) // Set VCA rule (water level detection)
#define VCA_CMD_GET_VCALIST					(VCA_CMD_GET_MIN + 24) // Get VCA Type List
#define VCA_CMD_GET_MAX						(VCA_CMD_GET_MIN + 25) // VCA read command upper limit

#define VCA_CMD_FACEMOSAIC					(75) // FACEMOSAIC
#define VCA_CMD_MIN							100
#define VCA_CMD_TARGET_PARAM				(VCA_CMD_MIN + 0)   // Set the VCA overlay and color settings
#define VCA_CMD_ADVANCE_PARAM				(VCA_CMD_MIN + 1)	// Set VCA advanced parameters
#define VCA_CMD_RULE_PARAM					(VCA_CMD_MIN + 2)	// Set VCA Rule General Parameters
#define VCA_CMD_ALARM_STATISTIC				(VCA_CMD_MIN + 3)	// Set the VCA alarm count to zero
#define VCA_CMD_TRIPWIRE					(VCA_CMD_MIN + 4)	// Set VCA rule (single trip line)
#define VCA_CMD_PERIMETER					(VCA_CMD_MIN + 5)	// Set VCA rule (perimeter)
#define VCA_CMD_LINGER						(VCA_CMD_MIN + 6)	// Set VCA rule (wandering)
#define VCA_CMD_PARKING						(VCA_CMD_MIN + 7)	// Set VCA rule (stop)
#define VCA_CMD_RUNNING						(VCA_CMD_MIN + 8)	// Set VCA rule (run)
#define VCA_CMD_CROWD						(VCA_CMD_MIN + 9)	// Set VCA rule (crowd)
#define VCA_CMD_DERELICT					(VCA_CMD_MIN + 10) // Set VCA rule (discard)
#define VCA_CMD_STOLEN						(VCA_CMD_MIN + 11) // Set the VCA rule (stolen)
#define VCA_CMD_MULITTRIP					(VCA_CMD_MIN + 12) // Set the VCA rule (double trip line)
#define VCA_CMD_VIDEODIAGNOSE				(VCA_CMD_MIN + 13) // set VCA rule (video diagnose for 300W)
#define VCA_CMD_AUDIODIAGNOSE				(VCA_CMD_MIN + 14) // set VCA rule (audio diagnose for 300W)
#define VCA_CMD_TRIPWIRE_EX					(VCA_CMD_MIN + 15) // set VCA rule (Tripwire for 300W)
#define VCA_CMD_RULE14_LEAVE_DETECT			(VCA_CMD_MIN + 16) // set VCA rule (leave detect for 300w)
#define VCA_CMD_CHANNEL_ENABLE				(VCA_CMD_MIN + 17) // Channel Enable
#define VCA_CMD_FACEREC						(VCA_CMD_MIN + 18) // Set VCA Rule (Face Recognition)
#define VCA_CMD_TRACK						(VCA_CMD_MIN + 19) // Set the VCA rule (track)
#define VCA_CMD_VIDEODETECTION				(VCA_CMD_MIN + 20) // Set VCA Rule (Video Diagnostics)
#define VCA_CMD_VIDEOSIZE					(VCA_CMD_MIN + 21)                  //set the intelligent analysis of video input size
#define VCA_CMD_RIVERCLEAN					(VCA_CMD_MIN + 22) // Set the VCA rule (river detection)
#define VCA_CMD_DREDGE						(VCA_CMD_MIN + 23) // set the VCA rule
#define VCA_CMD_RIVERADV					(VCA_CMD_MIN + 24) // Set VCA channel detection advanced parameters
#define VCA_CMD_SCENEREV					(VCA_CMD_MIN + 25) //intelligent analysis scene recovery time
#define VCA_CMD_FIGHT						(VCA_CMD_MIN + 26) // Fight Arithmetic
#define VCA_CMD_PROTECT						(VCA_CMD_MIN + 27) // Set the alert rule parameters
#define VCA_CMD_HEATMAP						(VCA_CMD_MIN + 28)
#define VCA_CMD_ALERT_TEMPLATE				(VCA_CMD_MIN + 29)
#define VCA_CMD_SEEPER						(VCA_CMD_MIN + 30)
#define VCA_CMD_ST_FACEADVANCE				(VCA_CMD_MIN + 31)
#define VCA_CMD_WINDOW_DETECTION			(VCA_CMD_MIN + 32)
#define VCA_CMD_ALERT_MULTISTAGE			(VCA_CMD_MIN + 33)
#define VCA_CMD_FLASH_LEVEL_NUM				(VCA_CMD_MIN + 34)
#define VCA_CMD_FLASH_FLICKER_NUM			(VCA_CMD_MIN + 35)
#define VCA_CMD_MASK_AREA_PARAM				(VCA_CMD_MIN + 36)
#define VCA_CMD_HELMET						(VCA_CMD_MIN + 37)
#define VCA_CMD_HELMET_SIZE_RANGE			(VCA_CMD_MIN + 38)
#define VCA_CMD_AREA_FIRST					(VCA_CMD_MIN + 39)
#define VCA_CMD_BIND_INFO					(VCA_CMD_MIN + 40)
#define VCA_CMD_CAMERA_TYPE					(VCA_CMD_MIN + 41)
#define VCA_CMD_SLUICEGATE					(VCA_CMD_MIN + 42)
#define VCA_CMD_COLOR_TRACK					(VCA_CMD_MIN + 43)
#define VCA_CMD_TARGET_PICTURE				(VCA_CMD_MIN + 44)
#define VCA_CMD_SLUICEGATE_PART2			(VCA_CMD_MIN + 45)	//Set VCA_CMD_SLUICEGATE_PART2,must first set VCA_CMD_SLUICEGATE.
#define VCA_CMD_MANCAR_DETECTARITHMETIC		(VCA_CMD_MIN + 46)
#define VCA_CMD_PICSTREAM_UPLOADPARAM		(VCA_CMD_MIN + 47)
#define VCA_CMD_SEDIMENT					(VCA_CMD_MIN + 48)	//Depth of water
#define VCA_CMD_SINGLE_INQUIRY				(VCA_CMD_MIN + 49)  //Single Inquiry or Unattended
#define VCA_CMD_CLIMB_UP					(VCA_CMD_MIN + 50)  //Climb Up 
#define VCA_CMD_NET_DEPARTURE				(VCA_CMD_MIN + 51)  //New Departure
#define VCA_CMD_ABNORMAL_NUMBER				(VCA_CMD_MIN + 52)  //Abnormal Number
#define VCA_CMD_GET_UP						(VCA_CMD_MIN + 53)  //Get Up
#define VCA_CMD_LEAVE_BED					(VCA_CMD_MIN + 54)  //Leave Bed
#define VCA_CMD_STATIC_DETECTION			(VCA_CMD_MIN + 55)  //Static Detection
#define VCA_CMD_SLEEP_POSTION				(VCA_CMD_MIN + 56)  //Sleep Postion
#define VCA_CMD_SLIP_UP						(VCA_CMD_MIN + 57)  //Slip Up
#define VCA_CMD_NEW_FIGHT					(VCA_CMD_MIN + 58)  //New Fight
#define VCA_CMD_BODY_TOUCH					(VCA_CMD_MIN + 59)  //Body Touch
#define VCA_CMD_HUMAN_ADVANCE				(VCA_CMD_MIN + 60)  //Human Advance
#define VCA_CMD_WATER_FLOW                  (VCA_CMD_MIN + 61)
#define VCA_CMD_VCA_PEPT					(VCA_CMD_MIN + 62)  //Oil Field Monitor Parameter
#define VCA_CMD_SENCE_SNAP					(VCA_CMD_MIN + 63)  //Sence Snap Parameter
#define VCA_CMD_RETRANS_INFO				(VCA_CMD_MIN + 64)  //Retrans info
#define VCA_CMD_NAVIGATION_SHIP_DETECTION	(VCA_CMD_MIN + 65)  //Navigation ship detection algorithm parameters
#define VCA_CMD_GRANARY_VEHICLE_DETECTION	(VCA_CMD_MIN + 66)  //Granary vehicle detection algorithm parameters
#define VCA_CMD_CHEFHATDETECT				(VCA_CMD_MIN + 67)  // ChefHatDetect
#define VCA_CMD_CHEFMASKDETECT				(VCA_CMD_MIN + 68)  // ChefMaskDetect
#define VCA_CMD_STRANDED					(VCA_CMD_MIN + 69)
#define VCA_CMD_ALONE						(VCA_CMD_MIN + 70)
#define VCA_CMD_DELIVERGOODS				(VCA_CMD_MIN + 71)
#define VCA_CMD_VCA_TOPS					(VCA_CMD_MIN + 72)
#define VCA_CMD_SMOKEDETECT					(VCA_CMD_MIN + 73)  //smoke detect
#define VCA_CMD_PHONEDETECT					(VCA_CMD_MIN + 74)	//phone detect 
#define VCA_CMD_TEMDETECT					(VCA_CMD_MIN + 75)	//phone detect 
#define VCA_CMD_VCA_SCAN_AREA				(VCA_CMD_MIN + 76)  //vca scan area
#define VCA_CMD_VCA_SCAN_PARA				(VCA_CMD_MIN + 77)  //vca scan parameter
#define VCA_CMD_SCANLIST                    (VCA_CMD_MIN + 78)  //Scan List
#define VCA_CMD_FIREWORKS                   (VCA_CMD_MIN + 79)  //set fire work para
#define VCA_CMD_3DMASKAREAENABLE            (VCA_CMD_MIN + 80)  //set 3D mask enable
#define VCA_CMD_3DMASKAREAPARA              (VCA_CMD_MIN + 81)  //set 3D mask area para
#define VCA_CMD_CHEFDETECT					(VCA_CMD_MIN + 82)  //Chef Detect
#define VCA_CMD_HDSCHEDULE		            (VCA_CMD_MIN + 83)  //scene HD template para 场景高清模板参数
#define VCA_CMD_FOCUSAREA                   (VCA_CMD_MIN + 84)  //scene focus area para 场景重点区域参数
#define VCA_CMD_SMART_MOVE					(VCA_CMD_MIN + 85)  // Smart Vca
#define VCA_CMD_DEFAULTTEMPLATELIST         (VCA_CMD_MIN + 86)  //template list association by Intelligent analysis type  智能分析类型关联模板列表
#define VCA_CMD_CPC_AREADISPALY             (VCA_CMD_MIN + 87)  //Regional statistics show the number of parameters 区域人数统计显示参数
#define VCA_CMD_CPC_AREACONFIG              (VCA_CMD_MIN + 88)	//Regional statistics the number of configuration parameters 区域人数统计配置参数
#define VCA_CMD_INQUIRY_TIMEOUT             (VCA_CMD_MIN + 89)	//
#define VCA_CMD_WSTABLEUSEMODE              (VCA_CMD_MIN + 90)  //Setting flow meter usage mode 设置流速表使用模式
#define VCA_CMD_WSTABLEAUTOGEN              (VCA_CMD_MIN + 91)  //Setting Auto WaterFlow table Param   设置自动生成流速表参数
#define VCA_CMD_INDOOREBIKE					(VCA_CMD_MIN + 92)  //Set Indoor EBike 室内电动车检测
#define VCA_CMD_RULE14_LEAVE_DETECTEX		(VCA_CMD_MIN + 93) // Set/get VCA rule (out-of-bound detection) new
#define VCA_CMD_RULEEVENT15_WATER_LEVEL_PART3			(VCA_CMD_MIN + 94)	//Set/get VCA rule 干扰检测开启
#define VCA_CMD_ANYSENCEADCANCED			(VCA_CMD_MIN + 95)
#define VCA_CMD_CPC_ADVANCE_PART2			(VCA_CMD_MIN + 96)
#define VCA_CMD_RULESETADVANCED				(VCA_CMD_MIN + 97)
#define VCA_CMD_CALIBRATION_REFERENCE		(VCA_CMD_MIN + 98)  //Set/Get the calibration reference system	设置/获取标定参考系
#define VCA_CMD_REFBOUNDARYINFO				(VCA_CMD_MIN + 99)  // Set/Get VCA ipc world coordinate inspect boundary	设置/获取前端设备世界坐标系校验边界范围
#define VCA_CMD_RADARLINKSCENE				(VCA_CMD_MIN + 100)  //Set/Get VCA radar linkage scene parameters	//设置/获取雷达联动场景参数
#define VCA_CMD_WATER_OUTFALL				(VCA_CMD_MIN + 101)  //Set/Get VCA water out fall parameters	//设置/获取水利排污口监测参数
#define VCA_CMD_IRRIDATA_UPLOADPARAM		(VCA_CMD_MIN + 102)	 //设置/获取 水利数据检测上报参数
#define VCA_CMD_SPECLIGHTPARAM				(VCA_CMD_MIN + 103)	 //设置/获取 高光谱灯源参数
#define VCA_CMD_WATERQUALITY_WIPER			(VCA_CMD_MIN + 104)	 //设置/获取 水质检测清洁刷配置
#define VCA_CMD_WATERCOLOR_DETECT           (VCA_CMD_MIN + 105)  //设置/获取 水体颜色识别参数
#define VCA_CMD_EVACAPACITY_PARA            (VCA_CMD_MIN + 106)  //设置/获取 蒸发量参数
#define VCA_CMD_WATER_FLOW_NO_CACHE         (VCA_CMD_MIN + 107)  //不读取缓存，立刻向设备发送协议获取流速算法参数
#define VCA_CMD_TARGET_PARA					(VCA_CMD_MIN + 108)  //设置/获取 靶标参数
#define VCA_CMD_GAUGECALIBCHECK_DATA        (VCA_CMD_MIN + 109)  //设置校验使用的水尺标定参数【水利行业专用协议】			
#define VCA_CMD_MAX							(VCA_CMD_MIN + 110)  // max



/************************************************************************/
/* Intelligent Analysis Video Diagnostic Type                                      */
/************************************************************************/
#define VCA_AVD_NOISE						(1 << 0) // Noise diagnosis
#define VCA_AVD_CLARITY						(1 << 1) // clarity diagnosis
#define VCA_AVD_BRIGHT_ABMNL				(1 << 2) // Brightness exception diagnosis
#define VCA_ADV_COLOR						(1 << 3) // Color cast diagnostics
#define VCA_ADV_FREEZE						(1 << 4) // Screen freeze diagnosis
#define VCA_ADV_NOSIGNAL					(1 << 5) // Signal loss diagnosis
#define VCA_ADV_CHANGE						(1 << 6) // Scene transformation diagnostics
#define VCA_ADV_INTERFERE					(1 << 7) // Human disturbance diagnosis
#define VCA_ADV_PTZ_LOST_CTL				(1 << 8) // PTZ out of control diagnosis
#define VCA_ADV_ARTIFICIAL_OCCULUSION		(1 << 9) // Artificial occlusion
#define VCA_ADV_ABNORMAL_CONTRAST			(1 << 10) // Abnormal contrast
#define VCA_ADV_BLACKANDWHITE_IMAGE			(1 << 11) // Black and white image
#define VCA_ADV_IMAGE_SHAKE					(1 << 12) // Image shake
#define VCA_ADV_FRINGE_INTERFERENCE			(1 << 13) // Fringe interference

/************************************************************************/
/* Smart Analytics Audio Diagnostic Type                                */
/************************************************************************/
#define VCA_AUDIO_MIN						0
#define	VCA_AUDIO_DROP 						(1<<(VCA_AUDIO_MIN))	//	Voice Drop
#define VCA_AUDIO_UNNORMAL					(1<<(VCA_AUDIO_MIN + 1))//	Voice Unnormal
#define VCA_AUDIO_NOISE						(1<<(VCA_AUDIO_MIN + 2))//	Noise Restrain
#define VCA_AUDIO_ECHO						(1<<(VCA_AUDIO_MIN + 3))//	Echo Restrain
#define VCA_AUDIO_SIGNAL					(1<<(VCA_AUDIO_MIN + 4))//	Signal Unnormal
#define VCA_AUDIO_MAX						VCA_AUDIO_MIN + 5

/************************************************************************/
/* Judge VCA Type                                */
/************************************************************************/
#define JUDGE_VCA_LATE								(1 << 0) // late
#define JUDGE_VCA_LEAVE_EARLY						(1 << 1) // leave early
#define JUDGE_VCA_LEAVE_MIDWAY						(1 << 2) // leaave midway
#define JUDGE_VCA_ABSENT							(1 << 3) // absent
#define JUDGE_VCA_UNIFORM_DETECTION					(1 << 4) // uniform detection
#define JUDGE_VCA_EMBLEM_DETECTION					(1 << 5) // law emblem detection
#define JUDGE_VCA_SIGN_RECOGNITION					(1 << 6) // sign recognition
#define JUDGE_VCA_CALL_DETECTION					(1 << 7) // call detection



// Linkage
#define	MAX_BITSET_NUM						4
#define	MAX_ALARM_LINKTYPE  				6

// typedef enum __tagLinkPTZType
// {
// 	LINKPTZ_TYPE_MIN = 0,
// LINKPTZ_TYPE_NOLINK = LINKPTZ_TYPE_MIN, // not linked
// LINKPTZ_TYPE_PRESET, // Preset
// LINKPTZ_TYPE_TRACK, // track
// LINKPTZ_TYPE_PATH // path
// LINKPTZ_TYPE_SCENE  //Scene
// }ELinkPTZType, *PELinkPTZType;	
typedef int ELinkPTZType;
typedef int __tagLinkPTZType;
typedef int * PELinkPTZType ;
#define LINKPTZ_TYPE_MIN	0
#define LINKPTZ_TYPE_NOLINK LINKPTZ_TYPE_MIN
#define LINKPTZ_TYPE_PRESET 1
#define LINKPTZ_TYPE_TRACK  2
#define LINKPTZ_TYPE_PATH	3
#define LINKPTZ_TYPE_SCENE  4
#define LINKPTZ_TYPE_MAX    5

//period
typedef struct __tagAlarmScheduleParam
{
	int				iBuffSize;

	int				iWeekday;								//	 Sunday to Saturday (0-6)
	int				iParam1;
	int				iParam2;
	NVS_SCHEDTIME	timeSeg[MAX_DAYS][MAX_TIMESEGMENT];		//	time period

	void*			pvReserved;									
	int				iSceneID;//Scene Number (0 ~ 15) 20140305 Extension
	char			cLibUUID[LEN_UUID];
	int				iCount;
	int				iFreqgroup;//频次统计组号,0-无效组号 1-16组号 (仅频次报警有效，优先解析本字段，忽略iChannelNo)
}TAlarmScheduleParam, *PTAlarmScheduleParam;

// The alarm template is enabled
typedef struct __tagAlarmScheEnableParam
{
	int				iBuffSize;

	int				iEnable;	//0：不使能 1：使能

	int				iParam1;	//iAlarmType=4智能分析且iParam2为90(法官行为分析)时，0-迟到; 1-早退; 2-中途离席;3-缺席;4-制服检测 ;5-法徽检测;6-标牌识别;7-打电话检测。其他表示规则ID
								//iAlarmType=20人脸识别报警时，表示人脸库id
								//iAlarmType=21NVR本地智能分析， iParam2=0,1,3,4表示人脸库ID
								//iAlarmType=24警戒水位预警(上限)/32警戒水位报警(下限)/33预警水位报警(上限)/34预警水位报警(下限)且iParam2=1时，表示规则ID，水位算法相关警戒/预警联动类型
								//iAlarmType=38车辆识别报警时，1-车牌比对报警，2-陌生车牌报警

	int				iParam2;	//iType=4智能分析或26政法智能分析时，表示智能分析事件类型：VCA_EVENT_MIN~VCA_EVENT_MAX
								//iType=20人脸识别报警时，0-黑名单，1-白名单
								//iType=21NVR本地智能分析，NVR的智能分析
								//iType=24警戒水位预警(上限)/32警戒水位报警(下限)/33预警水位报警(上限)/34预警水位报警(下限)时，表示0-雷达水位计，1-水位算法
								//iType=38车辆识别报警时，iParam1=1时填车牌库id，iParam1=2时填0

	int				iParam3;	//Reserved

	void*			pvReserved;									
	int				iSceneID;	//Scene Number (0 ~ 15) 20140305 Extension
	char			cLibUUID[LEN_UUID];
	int				iCount;
}TAlarmScheEnableParam, *PTAlarmScheEnableParam;

#define MAX_ALARM_PORT_COUNT			1024

typedef struct __tagAlarm_InputDisArm
{
	int						iArmInCount;					//	Alarm input count
	AlarmPortInfo			tAlarmPortInfo[MAX_ALARM_PORT_COUNT];				//	Alarm port info
} Alarm_InputDisArm;

typedef struct tagvca_TAlarmTimeSegment
{
	int		iStartHour;					
	int		iStartMinute;
	int		iStopHour;
	int		iStopMinute;
	int		iEnable;
} vca_TAlarmTimeSegment; // Alarm time period

typedef struct __tagAlarmCycleDisArm
{
	int						iLinkage;
	vca_TAlarmTimeSegment	timeSeg[MAX_WEEK_DAYS][MAX_SCHEDULE];				//	time period
} AlarmCycleDisArm; // Alarm 

typedef struct __tagManualDisArm
{
	int						iLinkage;
}ManualDisArm;

typedef struct __tagVca_TAlarmSchedule
{
	int						iWeekday;					//	Sunday to Saturday (0-6)
	vca_TAlarmTimeSegment	timeSeg[7][4];				//	time period
} vca_TAlarmSchedule; // Alarm arming template

typedef struct tagvca_TLinkPTZ
{
	unsigned short	usType;								//0 Do not associate the channel, 1 preset, 2 track, 3 path
	unsigned short	usPresetNO;							//	Preset position number
	unsigned short	usTrackNO;							//	track number
	unsigned short	usPathNO;							//	path number
	unsigned short	usSceneNO;							//	Scene number
} vca_TLinkPTZ;

// Alarm linkage
typedef struct __tagVca_TAlarmLink
{
	int						iLinkType;					//	0, linkage voice prompts; 1, linkage screen display; 2, linkage output port; 3, linkage recording; 4, linkage PTZ;
	int						iLinkChannel;
	
	int						iLinkSet[6];				//	0,1,2,3,5
	vca_TLinkPTZ			ptz[MAX_CHANNEL_NUM];		//	4
} vca_TAlarmLink;					

typedef struct __tagVca_TScheduleEnable
{
	int						iEnable;					//	Whether to enable	
	int						iParam;						//	Parameters, reserved
} vca_TScheduleEnable;

// Single trip line parameter
typedef struct __tagVca_TTripwire
{
	int					iValid;					//	is valid, the original rule iEventID to determine which events can be effective,
// But if all rules are not effective iEventID will always point to an event, the upper can not determine whether it is really effective, can only add an event effective field
	int					iTargetTypeCheck;		//	target type limit
	int					iMinDistance;			//	minimum distance, the target movement distance must be greater than this threshold
	int					iMinTime;				//	minimum time, the target moving distance must be greater than this threshold	
	int					iType;					//	that through the type
	int					iDirection;				//	single trip line to prohibit the direction of the angle
	vca_TLine			stLine;					//	trip line coordinates
} vca_TTripwire; 

typedef struct __tagVca_TPerimeter
{
	int					iValid;					//	is valid, the original rule iEventID to determine which events can be effective,
// But if all rules do not take effect iEventID will always point to an event, the upper can not determine whether it is really effective, can only add an event effective field
	int					iTargetTypeCheck;		//	distinguish between people and vehicles
	int					iMode;					//	Monitor mode
	int 				iMinDistance;			//	Minimum distance
	int 				iMinTime;				//	Minimum time	
	int					iType;					//	whether to do directional restrictions
	int 				iDirection;				//	forbidden direction angle (unit: angle)
	vca_TPolygon		stRegion;				//	Range of fields
}vca_TPerimeter; // Perimeter parameters

typedef struct __tagVca_TFaceRec
{
	int					iValid;					//	is valid, the original rule iEventID to determine which events can be effective,
// But if all rules do not take effect iEventID will always point to an event, the upper can not determine whether it is really effective, can only add an event effective field
	vca_TPolygon		stRegion;				//	Range of fields
} vca_TFaceRec; // Face recognition parameters

typedef struct __tagVca_TVideoDetection
{
	int					iValid;					//	is valid, the original rule iEventID to determine which events can be effective,
// But if all rules do not take effect iEventID will always point to an event, the upper can not determine whether it is really effective, can only add an event effective field
	int					m_iCheckTime;
} vca_TVideoDetection;

typedef struct __tagVca_tOSC
{
	int                 iValid;					//	is valid, the original rule iEventID to determine which events can be effective,
	int                 iColor;					//area color
	int					iAlarmColor;			//Color of the region when alarming
	int					iMinTime;				//The minimum time that a drop exists in the region
	int 				iMinSize;				//Minimum pixel size
	int 				iMaxSize;				//Maximum pixel size
	int 				iSunRegionCount;		//number of sub-polygons
	vca_TPolygon 		stMainRegion;			//The main polygon area
	vca_TPolygon 		stSubRegion[VCA_MAX_OSC_REGION_NUM];	//Sub-polygon area
} vca_tOSC;

typedef	struct vca_tTrack
{
	int					m_iStartPreset;			//Start trace point
	vca_TPolygon		m_TrackRegion;			//trace area
	int					m_iScene;				//scene large / medium / small
	int					m_iWeekDay;				// Weeks
	NVS_SCHEDTIME		m_timeSeg[MAX_DAYS][MAX_TIMESEGMENT];		//Tracking time period
}vca_tTrack, *LPvca_tTrack;

typedef	struct vca_tFluxStatistic
{
	int iValid; // is valid, the original rule iEventID to determine which events can be effective,
// But if all rules do not take effect iEventID will always point to an event, the upper can not determine whether it is really effective, can only add an event effective field
	vca_TPolygon		stRegion;				//	Range of fields	
}vca_tFluxStatistic, *LPvca_tFluxStatistic;

typedef	struct vca_tCrowd
{
	int iValid; // is valid, the original rule iEventID to determine which events can be effective,
// But if all rules do not take effect iEventID will always point to an event, the upper can not determine whether it is really effective, can only add an event effective field
	vca_TPolygon		stRegion;				//	Range of fields	
}vca_tCrowd, *LPvca_tCrowd;


typedef union vca_UEventParam
{	
	char				cBuf [EVENT_BUF_RESERVED]; // consortium reserved space to 2048
	vca_TTripwire		tripwire;
	vca_TPerimeter		perimeter;
	vca_TFaceRec		m_stFaceRec;
	vca_TVideoDetection	m_stVideoDetection;
	vca_tOSC            m_stOSC;
	vca_tTrack			m_stTrack;
	vca_tFluxStatistic  m_stFluxStatic;
	vca_tCrowd          m_stCrowd;
	vca_tLeaveDetect	m_stLeaveDetect;
}vca_UEventParam, *LPvca_UEventParam;
	
// Rule Sets the parameter
typedef struct vca_TRuleParam
{
	char				cRuleName [VCA_MAX_RULE_NAME_LEN];	// Name of the rule
	int					iValid;								// Whether the rule takes effect

	vca_TDisplayParam	stDisplayParam;						//	Display the parameter settings

	int					iAlarmCount;						//	he number of alarms
	vca_TAlarmSchedule	alarmSchedule;						//	alarm arming template parameters
	vca_TAlarmLink		alarmLink;							//	Alarm linkage setting parameters
	vca_TScheduleEnable	scheduleEnable;						//	Arming Enable

	vca_EEventType		iEventID;							// Behavior analysis event type
	vca_UEventParam		events;								// Behavior analysis event parameters
}vca_TRuleParam;	

// intelligent analysis configuration parameters
typedef struct vca_TConfig
{
	int iVideoSize; // Video size
	int iDisplayTarget; // Whether to superimpose the target frame
	int iDisplayTrace; // Whether the trajectory is superimposed
	int iTargetColor; // Target box color
	int iTargetAlarmColor; // Color of the target frame when alarming
	int iTraceLength; // Track length
}vca_TConfig; 	  

// Advanced analysis of intelligent analysis
typedef struct vca_TAdvParam
{
	int iEnable; // whether to enable advanced parameters
	int iTargetMinSize; // target the minimum number of pixels
	int iTargetMaxSize; // target the maximum number of pixels
	int iTargetMinWidth; // Maximum width
	int iTargetMaxWidth; // minimum width
	int iTargetMinHeight; // maximum height
	int iTargetMaxHeight; // minimum height
	int iTargetMinSpeed; // target minimum pixel speed (-1 is not restricted)
	int iTargetMaxSpeed; // target maximum pixel speed (-1 is not restricted)
	int iTargetMinWHRatio; // Minimum aspect ratio
	int iTargetMaxWHRatio; // Maximum aspect ratio
	int iSensitivity; // sensitivity level
}vca_TAdvParam;	

typedef struct TStatisticInfo
{
	int iSize; // structure size
	int iFluxIn; // into the number of people
	int iFluxOut; // Leave number
	int	iFluxPass;
	int	iFluxRegion;
	int	iFluxStay;
	int	iCount;
	int iNo;	//数据序号	离线数据包内的数据序号（人数统计数据实时数据不需携带）
	int iTime;	//人数变化时间,从1970-1-1经过的秒数（人数统计数据实时数据不需携带）UTC时间
	int iuTime; //人数变化时间(毫秒)，只代表上面时间之后的毫秒数
}TStatisticInfo;

// intelligent analysis of channel parameters
typedef struct vca_TVCAChannelParam
{
	int					iEnable;					//	Whether to enable this channel intelligent analysis
	vca_TConfig			config;  					//intelligent analysis of configuration parameters
	vca_TAdvParam		advParam;					//	intelligent analysis of advanced parameters

	int					iRuleID;
	vca_TRuleParam		rule[VCA_MAX_RULE_NUM]; 		//	Rules Sets the parameters	
} vca_TVCAChannelParam;

// VCA configuration parameter set,
typedef struct vca_TVCAParam
{
	int iEnable; // whether to enable intelligent analysis
	int iChannelBit; // Bit set of the intelligent analysis channel

	int iChannelID; // VCA channel
	vca_TVCAChannelParam	chnParam[MAX_CHANNEL_NUM];				//	parameter settings for each channel
}vca_TVCAParam;					

typedef struct vca_TAlarmInfo
{
	int iID;					// alarm message ID, used to obtain specific information
	int iChannel;				// channel number
	int iState;					// alarm status
	int iEventType;				// Event type 0: single trip line 1: double trip line 2: perimeter detection 3: wandering 4: parking 5: running
								// 6: Personnel density in the area 7: Stolen objects 8: Discards 9: Face recognition 10: Video diagnosis
								// 11: intelligent tracking 12: traffic statistics 13: crowd gathering 14: leave the job detection 15: audio diagnosis
	int iRuleID;				// Rule ID, iEventType=54: 0--Reserve 1--No Chef's Cap 2--No Mask 3--No Chef's clothes

	unsigned int uiTargetID;	// Destination ID
	int iTargetType;			// target type
	RECT rctTarget;				// Destination location
	int iTargetSpeed;			// target speed
	int iTargetDirection;		// target direction
	int iPresetNo;				// Preset ID 
	unsigned int iWaterLevelNUm;// surface scale readings
	char cWaterPicName[LEN_64];	// Save the picture path
	int iPicType;				// 0: Key 1: Ordinary
	int iDataType;				// 0: real-time 1: offline
	int	iHelmetInfo;
	int	iSenceID;
	RegionInfo	tRegionPara;	//区域0，非目标0的参数（不包括目标0的参数）
	int	iRegionNum;				//后续区域个数（区域0之后的）
	RegionInfo	tRegionInfo[MAX_LOCAL_NVR_REGION_NUM];//后续区域参数（区域0之后的）
	int iAlarmType;				//iEventType =37,0:reserved 1:single inquiry 2:attended.
								//iEventType =51,0: Reserved 1: Zone Invasion 2: Unusual Residency
								//iEventType =62,0: Reserved 1: Temperature difference detection (greater than) 2: High temperature detection (greater than) 3: Human body temperature detection 4: Temperature difference detection (less than) 5: High temperature detection (less than) 6: Low temperature detection (greater than) 7: Low temperature detection (less than) 8: Average temperature detection (greater than) 9: Average temperature detection (less than) 10: Sudden alarm
								//iEventType =64,0: Reserved 1: Designated fire point 2: Designated smoke 3: Fire point or smoke 4: Fire point and smoke 5: Smoking detection
	char	cPicUuid[LEN_64];	//iEventType =54 uuid
	int				iPtzP;		//iEventType =0,1,2,62,64
	int				iPtzT;		//iEventType =0,1,2,62,64
	int				iPtzZ;		//iEventType =0,1,2,62,64
	int				iAngle;		//iEventType =0,1,2,62,64
	int				iHView;		//iEventType =0,1,2,62,64
	int				iVView;		//iEventType =0,1,2,62,64
	char 			cAlarmTime[LEN_32];	//Alarm time
	char 			cEventAliasName[LEN_256];	//事件别名
	int				iTargetValid;	//iEventType=98,区域0目标0是否有效,0-无效 1-有效
	int				iTargetBehavior;//区域0,目标0扩展参数 iEventType=98，1-入侵，2-离开，3-滞留，4-高音，5-低音
	int				iTargetColor;	//区域0,目标0扩展参数 iEventType=98，1-红色 2-绿色 3-黄色 4-蓝色 5-紫色 6-青色 7-黑色 8-白色 9-马赛克 10-灰色 31-其他
	RegionInfoEx	tRegionInfoEx[MAX_LOCAL_NVR_REGION_NUM];//后续区域扩展参数
}vca_TAlarmInfo;	  

/************************************************************************
* FTP timing updates related
************************************************************************/

// FTP-related protocols
#define FTP_CMD_SET_MIN				0
#define FTP_CMD_SET_SNAPSHOT		(FTP_CMD_SET_MIN+0)
#define FTP_CMD_SET_LINKSEND		(FTP_CMD_SET_MIN+1)
#define FTP_CMD_SET_TIMEDSEND		(FTP_CMD_SET_MIN+2)
#define FTP_CMD_SET_UPDATE			(FTP_CMD_SET_MIN+3)
#define FTP_CMD_SET_MAX				(FTP_CMD_SET_MIN+4)

// # define FTP_CMD_GET_MIN (FTP_CMD_SET_MAX) // Consider the following extended compatibility
#define FTP_CMD_GET_MIN				(4)
#define FTP_CMD_GET_SNAPSHOT		(FTP_CMD_GET_MIN+0)
#define FTP_CMD_GET_LINKSEND		(FTP_CMD_GET_MIN+1)
#define FTP_CMD_GET_TIMEDSEND		(FTP_CMD_GET_MIN+2)
#define FTP_CMD_GET_UPDATE			(FTP_CMD_GET_MIN+3)
#define FTP_CMD_GET_MAX				(FTP_CMD_GET_MIN+4)

#define FTP_COMMON_CMD_MIN			(FTP_CMD_GET_MAX)
#define FTP_COMMON_CMD_SNAPSHOT_EX	(FTP_COMMON_CMD_MIN + 0)
#define FTP_COMMON_CMD_MAX			(FTP_COMMON_CMD_MIN + 1)

struct FTP_SNAPSHOT
{
	int		 iChannel; // channel
	int		 iEnable; // Enable
	int		 iQValue; // Quality
	int		 iInterval;			//	interval
	int		 iPictureSize; // capture picture size 0x7fff: on behalf of the automatic, the remaining size of the corresponding resolution
	int		 iPicCount; // Number of captured pictures
};

#define SNAPSHOT_MODE_MIN			0
#define SNAPSHOT_MODE_EVENT			(SNAPSHOT_MODE_MIN + 0)
#define SNAPSHOT_MODE_TIMING		(SNAPSHOT_MODE_MIN + 1)
#define SNAPSHOT_MODE_MAX			(SNAPSHOT_MODE_MIN + 2)
typedef struct tagFtpSnapshot
{
	int		iSize;
	int		iChannelNo;			//	channel number 
	int		iType;			
	int		iQValue;			//	quality[0-100]
	char	cInterval[LEN_16];	//	capture time interval [0,3600]
	int		iPictureSize;       //  capture picture size	0x7fff：automatic
	int		iPicCount;			//  capture picture number
	int		iMode;			
	int		iDevType;
}FtpSnapshot, *pFtpSnapshot;

struct FTP_LINKSEND
{
	int 	iChannel;			// channel
	int 	iEnable;			// Enable
	int 	iMethod;			// method
	int 	iMode;				// Template type 0 event, 1 timed
};

struct FTP_TIMEDSEND	
{
	int 	iChannel;			// channel
	int 	iEnable;			// Enable
	int 	iFileType;			// file type
	int 	iHour;				// time (hour)
	int 	iMinute;			// time (Minute)
	int		iSecond;			// Seconds (Seconds)
};

struct FTP_UPDATE
{
	char 	cFtpAddr [LEN_32];	// Ftp address used to upgrade
	char 	cFileName [LEN_32]; // File name used for upgrade
	char 	cUserName [LEN_16]; // User name used for upgrade
	char 	cPassword [LEN_16]; // Password used for upgrade
	int		iPort;				// port
	int		iUsage;				// flag used
};

/************************************************************************
* 3G DVR related settings
************************************************************************/
#define		DVR3G_CMD_SET_MIN					0
#define DVR3G_CMD_SET_POWERDELAY (DVR3G_CMD_SET_MIN + 0) // delay switch
#define DVR3G_CMD_SET_SIMNUM (DVR3G_CMD_SET_MIN + 1) // SIM card number
#define DVR3G_CMD_SET_GPSOVERLAY (DVR3G_CMD_SET_MIN + 2) // GPS information
#define DVR3G_CMD_SET_GPSFILTER (DVR3G_CMD_SET_MIN + 3) // GPS filter
#define DVR3G_CMD_SET_FTPUPLOAD_MODE (DVR3G_CMD_SET_MIN + 4) // Ftp auto-upload method
#define DVR3G_CMD_SET_VPDN (DVR3G_CMD_GET_MIN + 5) // VPDN
#define DVR3G_CMD_SET_MAX (DVR3G_CMD_SET_MIN + 6)

// # define DVR3G_CMD_GET_MIN (DVR3G_CMD_SET_MAX) // Consider the following extended compatibility
#define DVR3G_CMD_GET_MIN (5) // Get command lower limit
#define DVR3G_CMD_GET_POWERDELAY (DVR3G_CMD_GET_MIN + 0) // delay switch
#define DVR3G_CMD_GET_SIMNUM (DVR3G_CMD_GET_MIN + 1) // SIM card number
#define DVR3G_CMD_GET_GPSOVERLAY (DVR3G_CMD_GET_MIN + 2) // GPS information
#define DVR3G_CMD_GET_GPSFILTER (DVR3G_CMD_GET_MIN + 3) // GPS filter
#define DVR3G_CMD_GET_FTPUPLOAD_MODE (DVR3G_CMD_GET_MIN + 4) // Ftp auto-upload method
#define DVR3G_CMD_GET_VPDN (DVR3G_CMD_GET_MIN + 5) // VPDN
#define DVR3G_CMD_GET_MAX (DVR3G_CMD_GET_MIN + 6)

typedef struct TDVR3GInfo
{
	int		iPowerDelayOnTime; // delay start time, number of seconds, range 0 ~ 999 seconds, 0 means that the delay boot is not enabled.
	int		iPowerDelayOffTime; // time delay in seconds, ranging from 0 to 999 seconds, 0 means that the delay shutdown is not enabled.
	int		iGpsOverlay [MAX_CHANNEL_NUM]; // Enable superimposed GPS information, channel dependent
	int		iGpsFilter;								//	GPGS-bit 1, GPRMC-bit 2, GPVTG-bit 3, GPGLL-bit 4, GPGSA-bit 5, GPGSV-bit 6, other bits are temporarily reserved .
	char	cSimNum [33]; // SIM card number, the maximum length of 32 bits.
}TDVR3GInfo;

typedef struct TDVR3GInfoEx
{
	TDVR3GInfo	dvr3GInfo;
	int			iFtpUploadMode; // Upload method
	char		cReserved [64]; // Reserved field
}TDVR3GInfoEx;

#define DVR3G_VPDN_MAX_LEN 32

typedef struct TDVR3GVPDN
{
	int iSize;
	char cDialNumber [DVR3G_VPDN_MAX_LEN]; // APN
	char cAccount [DVR3G_VPDN_MAX_LEN]; // Account number
	char cPassword [DVR3G_VPDN_MAX_LEN]; // Password
	char cAccessNumber[DVR3G_VPDN_MAX_LEN];//Access number
	int  i3GAuthenticationType;//0-自动，1-CHAP，2-PAP
	
}TDVR3GVPDN;

/************************************************************************
* Multi-screen synthesis
************************************************************************/
typedef struct TVideoCombine 
{
	int iPicNum; // the number of composite images
	int iRec; // Synthesize the position
	char	cChannelID[300];   	// Synthesize channel information
}TVideoCombine;

#define MAX_VIDEO_COMBINE_NUM 3 // Maximum number of video composites per segment
#define MAX_VIDEO_COMBINE_BLOCK 4 // Maximum number of video compositing segments
#define MAX_VO_NUM					3	   //Max Video Output Num
#define MAX_VC_NUM					3 	   //Max Video Combine Num
#define MAX_VC_NUM_EX				1	   //Max Video CombineEx Num
#define VC_FINAL_FLAG				100    //Vca Final Flag 0-res，1-combine pic1， 2-combine pic2， 100-BT1120(H265combine)

#define VGA_CHANNEL_PIC		1
#define HDMI1_CHANNEL_PIC	17
#define CVBS_CHANNEL_PIC	33
#define VC_CHANNEL_PIC		49
#define VC1_CHANNEL_PIC		50
typedef struct tagVideoCombine 
{
	int		iSize;				// the size of Struct
	int     iPicNum;     		// combine picture number
	int		iRec;  				// combine location
	char	cChannelID[LEN_300];   	// combine information
	int		iVoDevId;			// Output device number, 0-reserved，1-VGA/HDMI0, 17-HDMI1, 33-CVBS 49-VC,50-VC1（H264）,default is VC
}VideoCombine, *pVideoCombine;


typedef struct tagRectEx
{
	int 			iX;
	int				iY;					// coordinates
	int				iWidth;
	int				iHeight;			// width and height
}RectEx, *pRectEx;

typedef struct tagPicParam
{
	int				ChnNo;				// Channel No
	RectEx		RectParam;
}PicParam, *pPicParam;

#define  MAX_VIEW_NUM				128		//Max View Number

//Create Free
typedef struct tagVoViewSegment
{
	int				iSize;				// the size of Struct
	int    	 		iVoDevId;     		// output Device ID
	int				iPicCnt;  		    // Picture Count= iPicCnt/1000 - 1
	PicParam		tPicPar[MAX_VIEW_NUM];
}VoViewSegment,*pVoViewSegment;

//VC Affinal
typedef struct tagVcAffinal 
{
	int				iSize;				// the size of Struct
	int             iVcId;	            // vcid
	int    	 		iVoDevId;     		// output Device ID
}VcAffinal,*pVcAffinal;

//Preview Mode  
typedef struct tagPreviewMode  
{
	int				iSize;				// the size of Struct
	int				iType;  			// Video Input Type
}PreviewMode, *pPreviewMode;

/************************************************************************
* Mixing
************************************************************************/
#define MAX_MIX_NUM 300
typedef struct __tagAudioMix
{	
	int iEnable; // Mixing enabled
	int iChannel; // channel number of the audio composition channel
	char cMixChannels [MAX_MIX_NUM]; // Source combination
	void*	pvReserved;					//reserved parameters, but also to distinguish between the size and TVideoCombine
}TAudioMix,	*PTAudioMix;


/************************************************************************
* SIP protocol related
************************************************************************/
#define SIP_CMD_GET_MIN						0
#define SIP_CMD_GET_ALARMCHANNEL			(SIP_CMD_GET_MIN+0)
#define SIP_CMD_GET_VIDEOCHANNEL			(SIP_CMD_GET_MIN+1)
#define SIP_CMD_GET_MAX						(SIP_CMD_GET_MIN+2)


typedef struct TSipVideoChannel
{
	int		iChannelNo;				//通道号
	char	cChannelID[33];			//通道ID	32字节
	int		iLevel;					//通道级别	0-255
	int		iPtzTime;				//PTZ时间	0-65535
	int		iPlatId;				//平台编号	0-平台1，1-平台2
}TSipVideoChannel;

typedef struct TSipAlarmChannel
{
	int		iChannelNo;				//通道号
	char	cChannelID[33];			//通道ID	32字节
	int		iLevel;					//通道级别	0-255
	int		iPlatId;				//平台编号	0-平台1，1-平台
}TSipAlarmChannel;

#define CHARSET_LENGTH 32 // Indicates the character set string length
#define LANGUAGE_COUNT 255 // The maximum number of languages ??supported
#define LANGUAGE_NAME_LENGTH 32 // Maximum length of a single language name
typedef struct
{
	int    iCount;
	char   cLanguage[LANGUAGE_COUNT][LANGUAGE_NAME_LENGTH];
} st_NVSLanguageList;


//2012-04-22
/************************************************************************
* Added data callback structure related
************************************************************************/
#define 	DTYPE_MIN			0
#define 	DTYPE_H264_MP		(DTYPE_MIN + 0)
#define     DTYPE_RAW_AUDIO		(DTYPE_MIN + 1)
#define 	DTYPE_PS			(DTYPE_MIN + 2)
#define     DTYPE_TS            (DTYPE_MIN + 3)
#define 	DTYPE_MAX			(DTYPE_MIN + 3)

typedef void (*RAWH264_MP_NOTIFY)(unsigned int _ulID, unsigned char* _cData, 
								  int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser);

typedef void (*RAW_AUDIO_NOTIFY)(unsigned int _ulID, unsigned char* _cData, int _iLen, int Type, void* _pvUserData);
// PS stream callback is the same as pure audio callback
typedef RAW_AUDIO_NOTIFY	PS_STREAM_NOTIFY;
typedef RAW_AUDIO_NOTIFY	TS_STREAM_NOTIFY;


/************************************************************************/
/* Incompatible callbacks */
/************************************************************************/


//当报警类型_iAlarmType是20人脸识别报警时_pInfo对应结构体为FaceAlarmNotify；其他报警类型_pInfo使用CommonAlarmNotify结构体
typedef void (*ALARM_NOTIFY_V5)(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser);

// Alarm message callback
//当报警类型_iAlarmType是6或9智能分析报警时，_iAlarmState代表警情索引，对应实际报警状态通过NetClient_VCAGetAlarmInfo接口获取；其他0-消警，1-报警
typedef void (*ALARM_NOTIFY_V4)(int _ulLogonID, int _iChan, int _iAlarmState,ALARMTYPE _iAlarmType,void* _iUser);

// Device parameter change callback
typedef void (*PARACHANGE_NOTIFY_V4)(int _ulLogonID, int _iChan, PARATYPE _iParaType,STR_Para* _strPara,void* _iUser);

// The main callback function, responsible for informing the device on the assembly line, video data and other related information, _iWparam reference WCM_LOGON_NOTIFY message definition
typedef void (*MAIN_NOTIFY_V4)(int _ulLogonID,long _iWparam, void* _iLParam,void* _iUser); 

// Serial port callback
typedef void (*COMRECV_NOTIFY_V4)(int _ulLogonID, char *_cData, int _iLen,int _iComNo,void* _iUser); 

#if !defined(__WIN__)
#ifndef HDC
#define HDC void*
#endif
#endif
//旧版画图回调只支持32位程序，不再维护
typedef int (*CBK_DRAW_FUNC)(long _lHandle,HDC _hDc,long _lUserData);
//新版画图回调
#ifndef pfCBDrawFun
	typedef int  (*pfCBDrawFun)(long _lHandle, HDC _hDc, void* _pvUserData);
#endif


#define UPGRADE_PROGRAM 1 // program upgrade
#define UPGRADE_WEB 2 // page upgrade

typedef void (*UPGRADE_NOTIFY_V4)(int _iLogonID, int _iServerState, void* _iUserData);
typedef void (*TRANSPORT_NOTIFY)(int _iLogonID, int _iServerState, void* _iUserData);


typedef void (__stdcall * RECVDATA_NOTIFY) (unsigned long _ulID, unsigned char * _ucData, int _iLen); // deprecated using this callback function
typedef void (__stdcall *RECVDATA_NOTIFY_EX)(unsigned long _ulID, unsigned char* _ucData,int _iLen, int _iFlag, void* _lpUserData);

#ifdef WIN32
typedef void (__stdcall *LOGON_NOTIFY)(int _iLogonID, void* _iLogonState);
typedef void (__stdcall *ALARM_NOTIFY)(int _iLogonID, int _iCh, void* _vContext,ALARMTYPE _iAlarmType);
typedef void (__stdcall *PARACHANGE_NOTIFY)(int _iLogonID, int _iCh, PARATYPE _iParaType,LPVOID _strPara);
typedef void (__stdcall *MAIN_NOTIFY)(int _iLogonID,long _iWParam,void* _iLParam, void* _lpNoitfyUserData);
typedef void (__stdcall *ALARM_NOTIFY_EX)(int _iLogonID, int _iChannel, void* _vContext,ALARMTYPE _iAlarmType, void* _lpNoitfyUserData);
typedef void (__stdcall *PARACHANGE_NOTIFY_EX)(int _iLogonID, int _iChannel, PARATYPE _iParaType,LPVOID _strPara, void* _lpNoitfyUserData);
typedef void (__stdcall *COMRECV_NOTIFY)(int _iLogonID, char *_buf, int _length,int _iComNo);
typedef void (__stdcall *DECYUV_NOTIFY)(unsigned long _ulID, unsigned char* _ucData, int _iSize, FRAME_INFO *_pFrameInfo, void* _pContext);
// Upgrade the kernel
typedef void (__stdcall *PROUPGRADE_NOTIFY)(int _iLogonID,int _iServerState);
// Upgrade the page
typedef void (__stdcall *WEBUPGRADE_NOTIFY)(int _iLogonID,int _iServerState);    
#else
typedef void (*ALARM_NOTIFY)(int _ulLogonID, int _iChan, int _iAlarmState,ALARMTYPE _iAlarmType,void* _iUser);
typedef void (*PARACHANGE_NOTIFY)(int _ulLogonID, int _iChan, PARATYPE _iParaType,STR_Para* _strPara,void* _iUser);
typedef void (*MAIN_NOTIFY)(int _ulLogonID,long _iWparam, void* _iLParam,void* _iUser); 
typedef void (*COMRECV_NOTIFY)(int _ulLogonID, char *_cData, int _iLen,int _iComNo,void* _iUser);
typedef void (*DECYUV_NOTIFY)(unsigned int _ulID,unsigned char *_cData, int _iLen, const FRAME_INFO *_pFrameInfo, void* _iUser);
// Upgrade the kernel
typedef void (*PROUPGRADE_NOTIFY)(int _iLogonID,int _iServerState,void* _iUser);
// Upgrade the page
typedef void (*WEBUPGRADE_NOTIFY)(int _iLogonID,int _iServerState,void* _iUser);    
#endif

// channel-dependent storage policy
#define STORAGE_CMD_MIN						0
#define STORAGE_CMD_STORAGE_RULE			(STORAGE_CMD_MIN + 0)
#define STORAGE_CMD_MAX						(STORAGE_CMD_MIN + 1)

#define STORAGE_RULE_MIN					0
#define STORAGE_RULE_RECORD_AUDIO			(STORAGE_RULE_MIN + 0)
#define STORAGE_RULE_STORAGE_TIME			(STORAGE_RULE_MIN + 1)
#define STORAGE_RULE_EXTRACT_FRAME			(STORAGE_RULE_MIN + 2)
#define STORAGE_RULE_REDUNDANCE_RECORD		(STORAGE_RULE_MIN + 3)
#define STORAGE_RULE_SUB_RECORD				(STORAGE_RULE_MIN + 4)
#define STORAGE_RULE_SUB_STORAGE_TIME		(STORAGE_RULE_MIN + 5)
#define STORAGE_RULE_MAIN_SUB_RECORD		(STORAGE_RULE_MIN + 6)
#define STORAGE_RULE_MAX					(STORAGE_RULE_MIN + 7)

//0-blRecordAudio: 是否记录音频；
//1-iStorageTime: 过期时间（即录像多长时间后可以覆盖，单位：天）
//2-blExtractFrame: 是否开启抽帧功能；
//3-blRedundanceRecord: 是否开启冗余录像；
//4-blSubRecord: 是否开启副码流录像
//5-iSubStorageTime: 副码流录像保存时间，单位：天
//6-blMainRecord: 是否开启主码流录像

//iType == 0, iValue: 0--不记录音频, 1--记录音频
//iType == 1, iValue: 0～60，0表示不强制执行过期操作, 磁盘满后覆盖。
//iType == 2, iValue: 0--不开启抽帧功能, 1--开启抽帧
//iType == 3, iValue: 0--不开启冗余录像, 1--开启冗余录像；
//iType == 4, iValue: 0--不开启副码流录像, 1--开启副码流录像
//iType == 5, iValue: 0~180, 0表示不使能副码流保存时长功能, 默认循环覆盖
//iType == 6, iValue: 0--不开启主码流录像, 1--开启副码流录像, -1--不支持副码流录像
typedef struct STORAGE_RULE
{
	int		iSize;		//Size of the structure,must be initialized before used
	int		iType;		// parameter type 0: whether to record audio 1: expiration time (unit: days)
						//2: whether to open the pumping frame function 3: whether to open the redundant video
	int		iValue;		// parameter value iType = 0,2,3, 0: do not record or not open 1: record or open;
						// iType = 1, the range is [0,60]. 0 means that the expiration is not enforced, and the disk is overwritten.
}*PSTORAGE_RULE;


//************************************************************************/
// * NIC parameters
//************************************************************************/
#define MIN_LAN_NUM						0
#define MAX_LAN_NUM						8
#define MAX_WIFICARD_NUM				2
#define MAX_VIR_LAN_NUM					16

#define LAN_CMD_SET_MIN					0
#define LAN_CMD_SET_IPV4				(LAN_CMD_SET_MIN+0)
#define LAN_CMD_SET_IPV6				(LAN_CMD_SET_MIN+1)
#define LAN_CMD_SET_WORKMODE			(LAN_CMD_SET_MIN+2)
//#define LAN_CMD_SET_LANNUM			(LAN_CMD_SET_MIN+3)
#define LAN_CMD_SET_DHCP				(LAN_CMD_SET_MIN+4)
#define LAN_CMD_SET_WIFIDHCPMODE		(LAN_CMD_SET_MIN+5)
#define LAN_CMD_SET_WIFIWORKMODE		(LAN_CMD_SET_MIN+6)
#define LAN_CMD_SET_WIFIAPDHCPPARA		(LAN_CMD_SET_MIN+7)
#define LAN_CMD_SET_WIFIAPPARA			(LAN_CMD_SET_MIN+8)
#define LAN_CMD_SET_WIFIPARA			(LAN_CMD_SET_MIN+9)
#define	LAN_CMD_SET_IPV6Ex				(LAN_CMD_SET_MIN+10)
#define LAN_CMD_SET_MAX					(LAN_CMD_SET_MIN+11)

#define LAN_CMD_GET_MIN					0
#define LAN_CMD_GET_IPV4				(LAN_CMD_GET_MIN+0)
#define LAN_CMD_GET_IPV6				(LAN_CMD_GET_MIN+1)
#define LAN_CMD_GET_WORKMODE			(LAN_CMD_GET_MIN+2)
#define LAN_CMD_GET_LANNUM				(LAN_CMD_GET_MIN+3)
#define LAN_CMD_GET_DHCP				(LAN_CMD_GET_MIN+4)
#define LAN_CMD_GET_WIFIDHCPMODE		(LAN_CMD_GET_MIN+5)
#define LAN_CMD_GET_WIFIWORKMODE		(LAN_CMD_GET_MIN+6)
#define LAN_CMD_GET_WIFIAPDHCPPARA		(LAN_CMD_GET_MIN+7)
#define LAN_CMD_GET_WIFIAPPARA			(LAN_CMD_GET_MIN+8)
#define LAN_CMD_GET_WIFIPARA			(LAN_CMD_GET_MIN+9)
#define LAN_CMD_GET_IPV6Ex				(LAN_CMD_GET_MIN+10)
#define LAN_CMD_SET_WIFIWORKMODE_CHN    (LAN_CMD_SET_MIN+11)
#define LAN_CMD_GET_WIFIWORKMODE_CHN    (LAN_CMD_GET_MIN+12)
#define LAN_CMD_GET_MAX					(LAN_CMD_GET_MIN+13)
	

typedef struct LANPARAM_IPV4 
{
	int		iSize;						// sizeof(LANPARAM_IPV4)
	int 	iLanNo;						// card number 0: Lan1 1: Lan2
	int 	iLanType;					// card type reserved, fill in 0 can be.Default 10M / 100M / 1000M adaptive, can not be changed.
	char	cIP [LENGTH_IPV4];			// IPV4 IP address
	char 	cMask [LENGTH_IPV4];		// IPV4 subnet mask
	char 	cGateway [LENGTH_IPV4];		// IPV4 default gateway
	char 	cDNS [LENGTH_IPV4];			// IPV4 Preferred DNS server
	char 	cBackDNS [LENGTH_IPV4];		// IPV4 Alternate DNS server
	char 	cReserved[64];				// Reserved, can be filled 0.
	//For the future expansion of other network properties, such as MTU
} *PLANPARAM_IPV4;

typedef struct LANPARAM_IPV6 
{
	int 	iSize;						// sizeof(LANPARAM_IPV6)
	int 	iLanNo;						// card number 0: Lan1 1: Lan2
	int 	iPrefixLen;					// IPV6 Subnet prefix length
	char	cIP [LENGTH_IPV6];			// IPV6 The IP address
	char	cGateway [LENGTH_IPV6];		// IPV6 default gateway
	char	cDNS [LENGTH_IPV6];			// IPV6 Preferred DNS server
	char	cBackDNS [LENGTH_IPV6];		// IPV6 Alternate DNS server
	char	cReserved[LEN_64];			// Reserved, fill in 0 can be.
	// If IPV6 related basic attributes can be achieved through the field.
} *PLANPARAM_IPV6;

typedef struct LANPARAM_IPV6_V1 
{
	int 	iSize;						// sizeof(LANPARAM_IPV6)
	int 	iLanNo;						// card number 0: Lan1 1: Lan2
	int 	iPrefixLen;					// IPV6 Subnet prefix length
	char	cIP [LENGTH_IPV6_V1];		// IPV6 IP address
	char	cGateway [LENGTH_IPV6_V1];	// IPV6 default gateway
	char	cDNS [LENGTH_IPV6_V1];		// IPV6 Preferred DNS server
	char	cBackDNS [LENGTH_IPV6_V1];	// IPV6 Alternate DNS server
	char	cReserved[LEN_64];			// IPV6 Local link address . When it is an incoming parameter, this field is 0. When it is an outgoing parameter, this field is the local link address  
	int		iDnsEnable;					// Whether manual setting is enabled  0 - do not enable automatic acquisition; 1 - enable manual configuration 
	int		iIpv6mode;					// IPV6 mode， 0，Reserved；1，Manual；2，DHCP；3，Route announcement
	// If IPV6 related basic attributes can be achieved through the field.
} *PLANPARAM_IPV6_V1;

#define MAX_WORKMODE_NUM  2 
typedef struct LANPARAM_WORKMODE 
{
	int		 iSize;						// sizeof(LANPARAM_WORKMODE)
	int		 iWorkMode;					// working mode 0: load balancing 1: network fault tolerance 2: multiple access setting
	int		 iMainLanNo;				// the main card number 0: Lan1 1: Lan2
	int		 iCardNum;					// tht card number 0:main 1: io
} *PLANPARAM_WORKMODE;

typedef struct LANPARAM_DHCP 
{
	int 	iSize;						// sizeof(LANPARAM_WORKMODE)
	int 	iLanNo;						// card number 0: Lan1 1: Lan2
	int 	iEnable;					// DHCP enable 0: disable 1: start

} *PLANPARAM_DHCP;
//add wifi&wifiap
typedef struct WIFIPARAM_DHCP			// cb@120711
{
	int 	iSize;						// sizeof(WIFIPARAM_DHCP)
	int 	iWifiDHCPMode;				// network card operating mode 0: wireless network card 0 DHCP; 1: wireless network card 0 ap mode DHCP
	int 	iEnable;					// DHCP enable 0: disable 1: start
	int 	iReserved;					// to be extended parameters PS: There is only one wireless network card has two types of DHCP
} *PWIFIPARAM_DHCP;

typedef struct WIFIAPDHCPPARA_DHCP       
{
	int 	iSize;						// sizeof(WIFIAPDHCPPARA_DHCP)
	int 	iDHCPStart;					// DHCP allocation range begins
	int 	iDHCPEnd;					// End of DHCP allocation range
	int 	iDHCPLease;					// DHCP lease
} *PWIFIAPDHCPPARA_DHCP;

#define WIFIWORKMODE_NONE       0
#define WIFIWORKMODE_NORMAL     1
#define WIFIWORKMODE_AP         2
#define WIFIWORKMODE_APSTA      3

typedef struct tagWifiWorkMode 
{
    int 	iSize;						// sizeof(WifiWorkMode)
    int 	iChannel;				    // 取值范围根据设备而定
    int 	iWifiMode;					// 0：不启动(预留)  1：普通wifi模式(sta) 2：ap模式  3：AP+sta(预留)

}WifiWorkMode, *PWifiWorkMode;


// typedef	enum __tagAlarmLinkType
// {
// 	ALARM_LINKTYPE_AUDIO = 0,
// 	ALARM_LINKTYPE_VIDEO,
// 	ALARM_LINKTYPE_OUTPORT,
// 	ALARM_LINKTYPE_RECORD,
// 	ALARM_LINKTYPE_PTZ,
// 	ALARM_LINKTYPE_SNAPSHOT
// }EAlarmLinkType, *PEAlarmLinkType;
typedef int __tagAlarmLinkType;
typedef int EAlarmLinkType;
typedef int *PEAlarmLinkType;

/***********************************Old_Alarm_Link_Type_Define_Start*******************************/
#define VCA_ALARMLINK_MIN					0
#define VCA_ALARMLINK_AUDIO					(VCA_ALARMLINK_MIN)		// linkage sound prompt
#define VCA_ARARMLINK_SCREEMSHOW			(VCA_ALARMLINK_MIN + 1) // linkage screen display
#define VCA_ALARMLINK_OUTPORT				(VCA_ALARMLINK_MIN + 2) // linkage output port
#define VCA_ALARMLINK_RECODER				(VCA_ALARMLINK_MIN + 3) // linkage recording
#define VCA_ALARMLINK_PTZ					(VCA_ALARMLINK_MIN + 4) // linkage PTZ
#define VCA_ALARMLINK_CAPTUREPIC			(VCA_ALARMLINK_MIN + 5) // linkage capture
#define VCA_ALARMLINK_SINGLEPIC				(VCA_ALARMLINK_MIN + 6) // linkage single screen
#define VCA_ALARMLINK_WHITELIGHT			(VCA_ALARMLINK_MIN + 7) // White Light
#define VCA_ALARMLINK_HTTP					(VCA_ALARMLINK_MIN + 8) // link http
#define VCA_ALARMLINK_MAX					(VCA_ALARMLINK_MIN + 9)

#define VCA_ALARMTYPE_MIN					0
#define VCA_ALARMTYPE_VIDEOLOST				(VCA_ALARMTYPE_MIN) // video loss
#define VCA_ALARMTYPE_PORT					(VCA_ALARMTYPE_MIN + 1) // port
#define VCA_ALARMTYPE_MOVE					(VCA_ALARMTYPE_MIN + 2) // Motion Detection
#define VCA_ALARMTYPE_COVER					(VCA_ALARMTYPE_MIN + 3) // video mask
#define VCA_ALARMTYPE_VCA					(VCA_ALARMTYPE_MIN + 4) // Intelligent Analysis
#define VCA_ALARMTYPE_MAX					(VCA_ALARMTYPE_MIN + 5)

#define VCA_LINKPTZ_TYPE_MIN				0
#define VCA_LINKPTZ_TYPE_NOLINK				(VCA_LINKPTZ_TYPE_MIN)		// not linked
#define VCA_LINKPTZ_TYPE_PRESET				(VCA_LINKPTZ_TYPE_MIN + 1)	// Preset
#define VCA_LINKPTZ_TYPE_TRACK				(VCA_LINKPTZ_TYPE_MIN + 2)	// Track
#define VCA_LINKPTZ_TYPE_PATH				(VCA_LINKPTZ_TYPE_MIN + 3)  // Path
#define VCA_LINKPTZ_TYPE_SCENE              (VCA_LINKPTZ_TYPE_MIN + 4)	//Scene
#define VCA_LINKPTZ_TYPE_MAX                (VCA_LINKPTZ_TYPE_MIN + 5)
/***********************************Old_Alarm_Link_Type_Define_End*******************************/
/***********************************New_Alarm_Link_Type_Define_Start*******************************/
#define ALARMLINKTYPE_MIN                               0  
#define ALARMLINKTYPE_LINKSOUND			(ALARMLINKTYPE_MIN + 0) 	// Linking voice prompts
#define ALARMLINKTYPE_LINKDISPLAY		(ALARMLINKTYPE_MIN + 1) 	// linkage screen display
#define ALARMLINKTYPE_LINKOUTPORT		(ALARMLINKTYPE_MIN + 2) 	// Linkage Output Port
#define ALARMLINKTYPE_LINKRECORD		(ALARMLINKTYPE_MIN + 3) 	// Linked recording
#define ALARMLINKTYPE_LINKPTZ			(ALARMLINKTYPE_MIN + 4) 	// Link PTZ
#define ALARMLINKTYPE_LINKSNAP			(ALARMLINKTYPE_MIN + 5) 	// Linked snap
#define ALARMLINKTYPE_LINKSINGLEPIC		(ALARMLINKTYPE_MIN + 6) 	// linkage single screen
#define ALARMLINKTYPE_LINKEMAIL			(ALARMLINKTYPE_MIN + 7) 	// Linked mail
#define ALARMLINKTYPE_LINKHTTP			(ALARMLINKTYPE_MIN + 8) 	// link HTTP
#define ALARMLINKTYPE_LASER				(ALARMLINKTYPE_MIN + 9) 	// linkage laser
#define ALARMLINKTYPE_FLASHING_WHITE	(ALARMLINKTYPE_MIN + 10) 	// linkage white flashing
#define ALARMLINKTYPE_TRAFFIC_TRIG		(ALARMLINKTYPE_MIN + 11) 	// Linked traffic outside trigger
#define ALARMLINKTYPE_GUARD_SOUND		(ALARMLINKTYPE_MIN + 12) 	// linkage alert sound
#define ALARMLINKTYPE_PRE_ARRANGED		(ALARMLINKTYPE_MIN + 13) 	// Linkage plan
#define ALARMLINKTYPE_SHOUT				(ALARMLINKTYPE_MIN + 14)	//Linkage propaganda
#define ALARMLINKTYPE_EXPOSURE			(ALARMLINKTYPE_MIN + 15)	//Linkage exposure
#define ALARMLINKTYPE_ROI				(ALARMLINKTYPE_MIN + 16)	//Linkage ROI	
#define ALARMLINKTYPE_LINKDOME			(ALARMLINKTYPE_MIN + 17)	//link dome camera
#define ALARMLINKTYPE_TRACKING			(ALARMLINKTYPE_MIN + 18)	//link tracking
#define ALARMLINKTYPE_LOCALSOUND        (ALARMLINKTYPE_MIN + 19)	//联动语音播报（NVR）
#define ALARMLINKTYPE_IPC_OUTPORT       (ALARMLINKTYPE_MIN + 20)	//Linkage IPC Output Port
#define ALARMLINKTYPE_CLOSEUP_CAPTURE   (ALARMLINKTYPE_MIN + 21)    //Linkage close-up capture 
#define ALARMLINKTYPE_IMAGE_MODE	    (ALARMLINKTYPE_MIN + 22)    //Linkage Image mode  0: not enabled 1: indicates black to color (human white light)
#define ALARMLINKTYPE_DOOR_STATE	    (ALARMLINKTYPE_MIN + 23)    //Linkage door state, not support
#define ALARMLINKTYPE_ALARM_LAMP		(ALARMLINKTYPE_MIN + 30)	//Linkage alarm lamp
#define ALARMLINKTYPE_UPLOAD_ALARM		(ALARMLINKTYPE_MIN + 31)	//Upload alram center not used until 20201105
#define ALARMLINKTYPE_LINKAPP			(ALARMLINKTYPE_MIN + 32)	//联动APP
#define ALARMLINKTYPE_INTELLIGENT_ALGO	(ALARMLINKTYPE_MIN + 33)	//联动智能算法
#define ALARMLINKTYPE_LINKSNAP_FTP      (ALARMLINKTYPE_MIN + 34)    //联动抓拍上传FTP
#define ALARMLINKTYPE_MAX				(ALARMLINKTYPE_MIN + 35)

// Compatible with IE
#define ALARM_LINKTYPE_AUDIO			ALARMLINKTYPE_LINKSOUND		// Linking voice prompts
#define ALARM_LINKTYPE_VIDEO			ALARMLINKTYPE_LINKDISPLAY	// linkage screen display
#define ALARM_LINKTYPE_OUTPORT			ALARMLINKTYPE_LINKOUTPORT	// Linkage output port
#define ALARM_LINKTYPE_RECORD			ALARMLINKTYPE_LINKRECORD	// Linked Recording
#define ALARM_LINKTYPE_PTZ				ALARMLINKTYPE_LINKPTZ		// Link PTZ
#define ALARM_LINKTYPE_SNAPSHOT			ALARMLINKTYPE_LINKSNAP		// Link Snap
#define ALARM_LINKTYPE_SINGLEPIC		ALARMLINKTYPE_LINKSINGLEPIC // Link Single
#define ALARM_LINKTYPE_EMAIL			ALARMLINKTYPE_LINKEMAIL		// Linked Mail
#define ALARM_LINKTYPE_HTTP				ALARMLINKTYPE_LINKHTTP		// link HTTP
/***********************************New_Alarm_Link_Type_Define_End*******************************/

typedef struct __tagAlarmLink
{
	int				iLinkSetSize;
	int				iLinkSet[MAX_ALARM_LINKTYPE*MAX_BITSET_NUM];	//Linked video capture output
// The value is determined by the iLinkType, MAX_BITSET_NUM for the extension, such as 0 Type and 0 + MAX_ALARM_LINKTYPE for the same type of linkage parameters, which is extended
// iLinkType = 0,1, the corresponding location data in iLinkSet indicates that iEnable is enabled, 0 is disabled, and 1 is enabled.
// iLinkType = 2, 3, 5, the corresponding position data in the iLinkSet indicates the bitwise enable iEnableByBits,
// Every bit from the least significant bit to the most significant bit indicates the enable of an audio / video channel / output port.
// iLinkType = 4, iLinkSe corresponds to position data t, which indicates the linked channel number

	int				iPtzSize;										//PTZ
	vca_TLinkPTZ		ptz[MAX_CHANNEL_NUM];		//Parameters for storing the linked PTZ when // iLinkType = 4
} TAlarmLink, * PTAlarmLink; // Alarm linkage parameters

typedef struct __tagAlarmLinkParam
{
	int				iBuffSize;

	int				iAlarmTypeParam;				//	Alarm type parameters
// The value is determined by iAlarmType, if iAlarmType is intelligent, iAlarmTypeParam represents iRuleID
	int				iReserved;						//	 value determined by the iAlarmType, if iAlarmType for intelligent analysis, iAlarmTypeParam said iEventID
	int				iLinkType;						//  extension +100
	union
	{

		int iChannelSet[4];							//linkage single-screen with this structure does not support the expansion, the need to use atomic acquisition
		int iEnable;								//linkage single-screen 32-way enable				
		struct										//This structure is used in addition to a single drawing
		{
			TAlarmLink stLinkContent; // linkage parameter
			void*			pvReserved;		
		};
	};
}TAlarmLinkParam, *PTAlarmLinkParam;




typedef struct __tagAlarmLinkParam_V1
{
	int				iAlarmTypeParam;				//	Alarm type parameter
// The value is determined by iAlarmType, if iAlarmType is intelligent, iAlarmTypeParam represents iRuleID
	int				iReserved;						//	value determined by the iAlarmType, if iAlarmType for intelligent analysis, iAlarmTypeParam said iEventID
	int				iLinkType;						//  extension +100
	int				iLinkParamSize;
	union
	{
		int iEnable;
		int iChannelSet[MAX_BITSET_COUNT];						//linkage single-screen with this structure
		struct  
		{
			int				iPtzNo;				
			unsigned short	usType;					// 0 Do not associate the channel, 1 preset, 2 track, 3 path
			unsigned short	usTypeNO;				//	preset number / track number / path number
		};
	};
	int	iSceneID;		
} TAlarmLinkParam_V1, * PTAlarmLinkParam_V1; // This structure no longer maintains 20160318

// Linkage sound
typedef struct __tagLinkSoundParam_V3
{
	int				iEnable;
	int				iSerialNo;
}TLinkSoundParam_V3, *PTLinkSoundParam_V3;

//Linkage display
typedef struct __tagLinkDisplayParam_V3
{
	int				iEnable;
}TLinkDisplayParam_V3, *PTLinkDisplayParam_V3;

// Linkage port output
typedef struct __tagLinkOutPortParam_V3
{
	int iChannelSet[MAX_BITSET_COUNT];
}TLinkOutPortParam_V3, *PTLinkOutPortParam_V3;

//Linkage recording
typedef struct __tagLinkRecordParam_V3
{
	int iChannelSet[MAX_BITSET_COUNT];
}TLinkRecordParam_V3, *PTLinkRecordParam_V3;

// Link PTZ
typedef struct __tagLinkPtzParam_V3
{
	int				iPtzNo;				
	unsigned short	usType;						//	0 does not link the channel, 1 preset, 2 track, 3 path
	unsigned short	usTypeNO;					//	preset number / track number / path number
	int				iResidenceTime;				//	iResidenceTime
}TLinkPtzParam_V3, *PTLinkPtzParam_V3;

//Linkage capture
typedef struct __tagLinkSnapParam_V3
{
	int iChannelSet[MAX_BITSET_COUNT];
}TLinkSnapParam_V3, *PTLinkSnapParam_V3;

//Linkage capture_ftp
typedef struct __tagLinkSnapFtpParam_V3
{
    int iChannelSet[MAX_BITSET_COUNT];
}TLinkSnapFtpParam_V3, *PTLinkSnapFtpParam_V3;

//Linkage single screen
typedef struct __tagLinkSinglePicParam_V3
{
	int iChannelSet[MAX_BITSET_COUNT];
}TLinkSinglePicParam_V3, *PTLinkSinglePicParam_V3;

// Linked mail
typedef struct __tagLinkEmailParam_V3
{
	int				iEnable;
	int				iAttatchment;
}TLinkEmailParam_V3, *PTLinkEmailParam_V3;

//Linkage HTTP
typedef struct __tagLinkHttpParam_V3
{
	int				iEnable;
}TLinkHttpParam_V3, *PTLinkHttpParam_V3;

//Linkage Laser
typedef struct __tagLinkLaserParam_V3
{
	int				iEnable;
}TLinkLaserParam_V3, *PTLinkLaserParam_V3;

//Linkage traffic external trigger
typedef struct __tagLinkTrafficTrigParam_V3
{
	int iChannelSet[MAX_BITSET_COUNT];
}TLinkTrafficTrigParam_V3, *PLinkTrafficTrigParam_V3;

//Linkage reserve plan number
typedef struct __tagPreArranged_V3
{
	int				iEnable;
	int				iPreNo;
}TLinkPreArranged_V3, *PLinkPreArranged_V3;

typedef struct __tagAlarmParam_V3
{
	int             iSize;							
	int				iAlarmTypeParam;				//	Alarm type parameters
	int				iReserved;						//	The value is determined by the iAlarmType ,if the iAlarmType is intelligent analysis,the iAlarmTypeParam represents iEventID.
	int				iSceneID;						//  Expend +100 
	char			cText[LEN_256];
	char			cLibUUID[LEN_UUID];
	int				iFreqgroup;						//频次统计组号 0-无效组号 1-16组号 (仅频次报警有效，优先解析本字段，忽略iChannelNo)
}TAlarmParam_V3, *PTAlarmParam_V3;

//Linkage white light flashing
typedef struct __tagLinkFlashingWhite_V3
{
	int				iEnable;
	int				iExternInfo;
} TLinkFlashingWhite_V3, *PLinkFlashingWhite_V3;

//Linkage guard sound
typedef struct __tagLinkGuardSound_V3
{
	int				iEnable;
	int				iSerialNo;
} TLinkGuardSound_V3, *PLinkGuardSound_V3;

//Linkage propaganda
typedef struct __tagLinkShoutParam_V3
{
	int				iEnable;
}TLinkShoutParam_V3, *PTLinkShoutParam_V3;

//Linkage Exposure
typedef struct __tagLinkExposureParam_V3
{
	int				iEnable;
	int				iExposureTime;			//[0~3600]
}TLinkExposureParam_V3, *PTLinkExposureParam_V3;

//Linkage ROI
typedef struct __tagLinkRoiParam_V3
{
	int				iEnable;
	int				iRoiLevel;
}TLinkRoiParam_V3, *PTLinkRoiParam_V3;

typedef struct __tagLinkDomeCameras_V3
{
	int				iEnable;
}TLinkDomeCameras_V3, *PTLinkDomeCameras_V3;

typedef struct __tagLinkTracking_V3
{
	int				iEnable;
}TLinkTracking_V3, *PTLinkTracking_V3;

typedef struct _tagLinkLocalSound_V3
{
	int             iEnable;
	int             iSerialNo;
}TLinkLocalSound_V3, *PTLinkLocalSound;

typedef struct __tagLinkAlarmLamp
{
	int				iEnable;
}TLinkAlarmLamp, *PTLinkAlarmLamp;

typedef union tagULinkIPCParam
{
	TLinkSoundParam_V3			tLinkSoundParam;
	TLinkOutPortParam_V3		tLinkOutPortParam;
	int							iCommonSet[MAX_BITSET_COUNT];		//Universal memory operations for the Commonwealth.
}ULinkIPCParam, *PULinkIPCParam;

typedef union tagULinkParam_V3
{
	TLinkSoundParam_V3			tLinkSoundParam;
	TLinkDisplayParam_V3		tLinkDisplayParam;
	TLinkOutPortParam_V3		tLinkOutPortParam;
	TLinkRecordParam_V3			tLinkRecordParam;
	TLinkPtzParam_V3			tLinkPtzParam;
	TLinkSnapParam_V3			tLinkSnapParam;
	TLinkSinglePicParam_V3		tLinkSinglePicParam;
	TLinkEmailParam_V3			tLinkEmailParam;
	TLinkHttpParam_V3			tLinkHttpParam;
	TLinkLaserParam_V3			tLinkLaserParam;
	TLinkFlashingWhite_V3		tLinkFlashingWhite;
	TLinkGuardSound_V3			tLinkGuardSound;
	TLinkTrafficTrigParam_V3	tLinkTrafficTrigParam;
	TLinkPreArranged_V3			tLinkPreArrangParam;
	TLinkShoutParam_V3			tLinkShoutParam;
	TLinkExposureParam_V3		tLinkExposureParam;
	TLinkRoiParam_V3			tLinkRoiParam;
	TLinkDomeCameras_V3			tLinkDomeCameras;
	TLinkTracking_V3			tLinkTracking;
	TLinkLocalSound_V3          tLinkLocalSound;
	TLinkAlarmLamp				tTLinkAlarmLamp;
    TLinkSnapFtpParam_V3        tLinkSnapFtpParam;
	int							iCommonSet[MAX_BITSET_COUNT];		//Universal memory operations for the Commonwealth.
}ULinkParam_V3, *PULinkParam_V3;

typedef struct __tagLinkParam_V3
{
	int             iSize;
	int				iLinkType;
	ULinkParam_V3	uLinkParam;
}TLinkParam_V3, *PTLinkParam_V3;

typedef struct __tagAlarmLinkParam_V3
{
	TAlarmParam_V3		tAlarmParam;
	TLinkParam_V3		tLinkParam;
}TAlarmLinkParam_V3, *PTAlarmLinkParam_V3;

typedef struct _tagAlarmNVRLinkIPC
{
	int					iSize;
	int					iTrigChannelNo;		//触发报警通道号
	int					iLinkChannelNo;		//联动前端通道号
	int					iMajorType;			//报警类型列表
	int					iMinorType;			//报警子类型
	int					iAlarmTypeParam;	//报警类型参数 Alarm type param
	int					iLinkType;          //联动类型 IPC LinkType 
	ULinkIPCParam		tULinkIPCParam;	    //联动参数 IPC LinkParam
}AlarmNVRLinkIPC, *PAlarmNVRLinkIPC;

#define MAX_NVR_LINK_IPC_NUMBER 16
typedef struct _tagAlarmNVRLinkIPCParam
{
	int iSize;
	int iCount;
	AlarmNVRLinkIPC tAlarmNVRLinkIPC[MAX_NVR_LINK_IPC_NUMBER];
}AlarmNVRLinkIPCParam, *PAlarmNVRLinkIPCParam;


/************************************************************************/
/* General alarm parameters								    			*/
/************************************************************************/
//Command
// typedef enum __tagAlarmSetCmd
// {
// 	CMD_SET_ALARMSCHEDULE = 0,
// 	CMD_SET_ALARMLINK,
// 	CMD_SET_ALARMSCH_ENABLE
// }EAlarmSetCmd, *PEAlarmSetCmd;
typedef int __tagAlarmSetCmd;
typedef int EAlarmSetCmd;
typedef int * PEAlarmSetCmd;

// typedef enum __tagAlarmGetCmd
// {
// 	CMD_GET_ALARMSCHEDULE = 0,
// 	CMD_GET_ALARMLINK,
// 	CMD_GET_ALARMSCH_ENABLE
// }EAlarmGetCmd, *PEAlarmGetCmd;
typedef int __tagAlarmGetCmd;
typedef int EAlarmGetCmd;
typedef int * PEAlarmGetCmd;
#define CMD_GET_ALARMSCHEDULE		0
#define CMD_GET_ALARMLINK			1
#define CMD_GET_ALARMSCH_ENABLE		2
#define CMD_GET_ALARMTRIGGER		3
#define CMD_GET_ALARMLINK_V1		4
#define CMD_GET_ALARMLINK_V2		5
#define CMD_GET_ALARMLINK_V3		6
#define CMD_GET_ALARM_NVRLINKIPC	7
//#define CMD_GET_ALARMLINK_SOUNDTIME	8	//INNER*PARAGET*LINKSOUNDTIME协议暂时不用了，功能有问题，嵌入式未实现


//Alarm parameter identification
#define ATPI_AUDIO_LOST			0		//Audio loss alarm trigger parameter
#define AUDIO_LOST_PARAM_COUNT	1		


//Linkage alarm parameters change message from 0x10000 to start
//Video loss
#define	PARA_VIDEOLOST_ALARMLINK_SOUND				10000
#define	PARA_VIDEOLOST_ALARMLINK_DISPLAY			10001
#define	PARA_VIDEOLOST_ALARMLINK_OUTPORT			10002
#define	PARA_VIDEOLOST_ALARMLINK_RECORD				10003
#define	PARA_VIDEOLOST_ALARMLINK_PTZ				10004
#define	PARA_VIDEOLOST_ALARMLINK_SNAP				10005
#define	PARA_VIDEOLOST_ALARMLINK_SINGLEPIC			PARA_VDOLOST_LNKSINGLEPIC
#define	PARA_VIDEOLOST_ALARMLINK_EMAIL				PARA_VDOLOST_LNKMAIL
#define	PARA_VIDEOLOST_ALARMLINK_HTTP				PARA_ALMINPORT_LNKHTTP
#define	PARA_VIDEOLOST_ALARMLINK_LASER				10009
#define	PARA_VIDEOLOST_ALARMLINK_WHITE				PARA_ALARMLINK_FLASHING_WHITE
#define	PARA_VIDEOLOST_ALARMLINK_TRAFFICTRIG		10011
#define	PARA_VIDEOLOST_ALARMLINK_GUARDSOUND			10012
#define	PARA_VIDEOLOST_ALARMLINK_PREARRANGED		10013
#define	PARA_VIDEOLOST_ALARMLINK_SHOUT				10014
#define	PARA_VIDEOLOST_ALARMLINK_EXPOSURE			10015
#define	PARA_VIDEOLOST_ALARMLINK_ROI				10016
#define	PARA_VIDEOLOST_ALARMLINK_DOME				10017
#define PARA_VIDEOLOST_ALARMLINK_TRACKING			10018
#define PARA_VIDEOLOST_ALARMLINK_LOCALSOUND			10019
#define PARA_VIDEOLOST_ALARMLINK_IPC_OUTPORT		10020
#define PARA_VIDEOLOST_ALARMLINK_CLOSEUP_CAPTURE	10021 
#define PARA_VIDEOLOST_ALARMLINK_IMAGE_MODE			10022

//Port alarm
#define	PARA_PORT_ALARMLINK_SOUND				10100
#define	PARA_PORT_ALARMLINK_DISPLAY				10101
#define	PARA_PORT_ALARMLINK_OUTPORT				10102
#define	PARA_PORT_ALARMLINK_RECORD				10103
#define	PARA_PORT_ALARMLINK_PTZ					10104
#define	PARA_PORT_ALARMLINK_SNAP				10105
#define	PARA_PORT_ALARMLINK_SINGLEPIC			PARA_ALMINPORT_LNKSINGLEPIC
#define	PARA_PORT_ALARMLINK_EMAIL				PARA_ALMINPORT_LNKMAIL
#define	PARA_PORT_ALARMLINK_HTTP				PARA_ALMINPORT_LNKHTTP
#define	PARA_PORT_ALARMLINK_LASER				10109
#define	PARA_PORT_ALARMLINK_WHITE				PARA_ALARMLINK_FLASHING_WHITE
#define	PARA_PORT_ALARMLINK_TRAFFICTRIG			PARA_ALARMLINK_TRAFFIC_TRIG
#define	PARA_PORT_ALARMLINK_GUARDSOUND			10112
#define	PARA_PORT_ALARMLINK_PREARRANGED			10113
#define	PARA_PORT_ALARMLINK_SHOUT				10114
#define	PARA_PORT_ALARMLINK_EXPOSURE			10115
#define	PARA_PORT_ALARMLINK_ROI					10116
#define	PARA_PORT_ALARMLINK_DOME				10117
#define PARA_PORT_ALARMLINK_TRACKING			10118
#define PARA_PORT_ALARMLINK_LOCALSOUND			10119
#define PARA_PORT_ALARMLINK_IPC_OUTPORT			10120
#define PARA_PORT_ALARMLINK_CLOSEUP_CAPTURE		10121 
#define PARA_PORT_ALARMLINK_IMAGE_MODE			10122


//Motion detection
#define	PARA_MOTION_ALARMLINK_SOUND				10200
#define	PARA_MOTION_ALARMLINK_DISPLAY			10201
#define	PARA_MOTION_ALARMLINK_OUTPORT			10202
#define	PARA_MOTION_ALARMLINK_RECORD			10203
#define	PARA_MOTION_ALARMLINK_PTZ				10204
#define	PARA_MOTION_ALARMLINK_SNAP				10205
#define	PARA_MOTION_ALARMLINK_SINGLEPIC			PARA_MOTIONDEC_LNKSINGLEPIC
#define	PARA_MOTION_ALARMLINK_EMAIL				PARA_MOTIONDEC_LNKMAIL
#define	PARA_MOTION_ALARMLINK_HTTP				PARA_ALMINPORT_LNKHTTP
#define	PARA_MOTION_ALARMLINK_LASER				10209
#define	PARA_MOTION_ALARMLINK_WHITE				PARA_ALARMLINK_FLASHING_WHITE
#define	PARA_MOTION_ALARMLINK_TRAFFICTRIG		10211
#define	PARA_MOTION_ALARMLINK_GUARDSOUND		10212
#define	PARA_MOTION_ALARMLINK_PREARRANGED		10213
#define	PARA_MOTION_ALARMLINK_SHOUT				10214
#define	PARA_MOTION_ALARMLINK_EXPOSURE			10215
#define	PARA_MOTION_ALARMLINK_ROI				10216
#define	PARA_MOTION_ALARMLINK_DOME				10217
#define PARA_MOTION_ALARMLINK_TRACKING			10218
#define PARA_MOTION_ALARMLINK_LOCALSOUND		10219
#define PARA_MOTION_ALARMLINK_IPC_OUTPORT		10220
#define PARA_MOTION_ALARMLINK_CLOSEUP_CAPTURE	10221 
#define PARA_MOTION_ALARMLINK_IMAGE_MODE		10222

//Video occlusion
#define	PARA_COVER_ALARMLINK_SOUND				10300
#define	PARA_COVER_ALARMLINK_DISPLAY			10301
#define	PARA_COVER_ALARMLINK_OUTPORT			10302
#define	PARA_COVER_ALARMLINK_RECORD				10303
#define	PARA_COVER_ALARMLINK_PTZ				10304
#define	PARA_COVER_ALARMLINK_SNAP				10305
#define	PARA_COVER_ALARMLINK_SINGLEPIC			10306
#define	PARA_COVER_ALARMLINK_EMAIL				10307
#define	PARA_COVER_ALARMLINK_HTTP				10308
#define	PARA_COVER_ALARMLINK_LASER				10309
#define	PARA_COVER_ALARMLINK_WHITE				10310
#define	PARA_COVER_ALARMLINK_TRAFFICTRIG		10311
#define	PARA_COVER_ALARMLINK_GUARDSOUND			10312
#define	PARA_COVER_ALARMLINK_PREARRANGED		10313
#define	PARA_COVER_ALARMLINK_SHOUT				10314
#define	PARA_COVER_ALARMLINK_EXPOSURE			10315
#define	PARA_COVER_ALARMLINK_ROI				10316
#define	PARA_COVER_ALARMLINK_DOME				10317
#define PARA_COVER_ALARMLINK_TRACKING			10318
#define PARA_COVER_ALARMLINK_LOCALSOUND			10319
#define PARA_COVER_ALARMLINK_IPC_OUTPORT		10320
#define PARA_COVER_ALARMLINK_CLOSEUP_CAPTURE	10321 
#define PARA_COVER_ALARMLINK_IMAGE_MODE			10322


//Intelligent analysis
#define	PARA_VCA_ALARMLINK_SOUND				10400
#define	PARA_VCA_ALARMLINK_DISPLAY				10401
#define	PARA_VCA_ALARMLINK_OUTPORT				10402
#define	PARA_VCA_ALARMLINK_RECORD				10403
#define	PARA_VCA_ALARMLINK_PTZ					10404
#define	PARA_VCA_ALARMLINK_SNAP					10405
#define	PARA_VCA_ALARMLINK_SINGLEPIC			10406
#define	PARA_VCA_ALARMLINK_EMAIL				10407
#define	PARA_VCA_ALARMLINK_HTTP					10408
#define	PARA_VCA_ALARMLINK_LASER				10409
#define	PARA_VCA_ALARMLINK_WHITE				10410
#define	PARA_VCA_ALARMLINK_TRAFFICTRIG			10411
#define	PARA_VCA_ALARMLINK_GUARDSOUND			10412
#define	PARA_VCA_ALARMLINK_PREARRANGED			10413
#define	PARA_VCA_ALARMLINK_SHOUT				10414
#define	PARA_VCA_ALARMLINK_EXPOSURE				10415
#define	PARA_VCA_ALARMLINK_ROI					10416
#define	PARA_VCA_ALARMLINK_DOME					10417
#define PARA_VCA_ALARMLINK_TRACKING				10418
#define PARA_VCA_ALARMLINK_LOCALSOUND			10419
#define PARA_VCA_ALARMLINK_IPC_OUTPORT			10420
#define PARA_VCA_ALARMLINK_CLOSEUP_CAPTURE		10421 
#define PARA_VCA_ALARMLINK_IMAGE_MODE			10422
#define PARA_VCA_ALARMLINK_SNAP_FTP			    10423

//Audio loss
#define	PARA_AUDIOLOST_ALARMLINK_SOUND			10500
#define	PARA_AUDIOLOST_ALARMLINK_DISPLAY		10501
#define	PARA_AUDIOLOST_ALARMLINK_OUTPORT		10502
#define	PARA_AUDIOLOST_ALARMLINK_RECORD			10503
#define	PARA_AUDIOLOST_ALARMLINK_PTZ			10504
#define	PARA_AUDIOLOST_ALARMLINK_SNAP			10505
#define	PARA_AUDIOLOST_ALARMLINK_SINGLEPIC		10506
#define	PARA_AUDIOLOST_ALARMLINK_EMAIL			10507
#define	PARA_AUDIOLOST_ALARMLINK_HTTP			10508
#define	PARA_AUDIOLOST_ALARMLINK_LASER			10509
#define	PARA_AUDIOLOST_ALARMLINK_WHITE			10510
#define	PARA_AUDIOLOST_ALARMLINK_TRAFFICTRIG	10511
#define	PARA_AUDIOLOST_ALARMLINK_GUARDSOUND		10512
#define	PARA_AUDIOLOST_ALARMLINK_PREARRANGED	10513
#define	PARA_AUDIOLOST_ALARMLINK_SHOUT			10514
#define	PARA_AUDIOLOST_ALARMLINK_EXPOSURE		10515
#define	PARA_AUDIOLOST_ALARMLINK_ROI			10516
#define	PARA_AUDIOLOST_ALARMLINK_DOME			10517
#define PARA_AUDIOLOST_ALARMLINK_TRACKING			10518
#define PARA_AUDIOLOST_ALARMLINK_LOCALSOUND			10519
#define PARA_AUDIOLOST_ALARMLINK_IPC_OUTPORT		10520
#define PARA_AUDIOLOST_ALARMLINK_CLOSEUP_CAPTURE	10521 
#define PARA_AUDIOLOST_ALARMLINK_IMAGE_MODE			10522

//Temperature and humidity
#define	PARA_TEMPERATURE_ALARMLINK_SOUND		10600
#define	PARA_TEMPERATURE_ALARMLINK_DISPLAY		10601
#define	PARA_TEMPERATURE_ALARMLINK_OUTPORT		10602
#define	PARA_TEMPERATURE_ALARMLINK_RECORD		10603
#define	PARA_TEMPERATURE_ALARMLINK_PTZ			10604
#define	PARA_TEMPERATURE_ALARMLINK_SNAP			10605
#define	PARA_TEMPERATURE_ALARMLINK_SINGLEPIC	10606
#define	PARA_TEMPERATURE_ALARMLINK_EMAIL		10607
#define	PARA_TEMPERATURE_ALARMLINK_HTTP			10608
#define	PARA_TEMPERATURE_ALARMLINK_LASER		10609
#define	PARA_TEMPERATURE_ALARMLINK_WHITE		10610
#define	PARA_TEMPERATURE_ALARMLINK_TRAFFICTRIG	10611
#define	PARA_TEMPERATURE_ALARMLINK_GUARDSOUND	10612
#define	PARA_TEMPERATURE_ALARMLINK_PREARRANGED	10613
#define	PARA_TEMPERATURE_ALARMLINK_SHOUT		10614
#define	PARA_TEMPERATURE_ALARMLINK_EXPOSURE		10615
#define	PARA_TEMPERATURE_ALARMLINK_ROI			10616
#define	PARA_TEMPERATURE_ALARMLINK_DOME			10617
#define PARA_TEMPERATURE_ALARMLINK_TRACKING			10618
#define PARA_TEMPERATURE_ALARMLINK_LOCALSOUND		10619
#define PARA_TEMPERATURE_ALARMLINK_IPC_OUTPORT		10620
#define PARA_TEMPERATURE_ALARMLINK_CLOSEUP_CAPTURE	10621 
#define PARA_TEMPERATURE_ALARMLINK_IMAGE_MODE		10622

//Violation detection
#define	PARA_ILLEGAL_ALARMLINK_SOUND			10700
#define	PARA_ILLEGAL_ALARMLINK_DISPLAY			10701
#define	PARA_ILLEGAL_ALARMLINK_OUTPORT			10702
#define	PARA_ILLEGAL_ALARMLINK_RECORD			10703
#define	PARA_ILLEGAL_ALARMLINK_PTZ				10704
#define	PARA_ILLEGAL_ALARMLINK_SNAP				10705
#define	PARA_ILLEGAL_ALARMLINK_SINGLEPIC		10706
#define	PARA_ILLEGAL_ALARMLINK_EMAIL			10707
#define	PARA_ILLEGAL_ALARMLINK_HTTP				10708
#define	PARA_ILLEGAL_ALARMLINK_LASER			10709
#define	PARA_ILLEGAL_ALARMLINK_WHITE			10710
#define	PARA_ILLEGAL_ALARMLINK_TRAFFICTRIG		10711
#define	PARA_ILLEGAL_ALARMLINK_GUARDSOUND		10712
#define	PARA_ILLEGAL_ALARMLINK_PREARRANGED		10713
#define	PARA_ILLEGAL_ALARMLINK_SHOUT			10714
#define	PARA_ILLEGAL_ALARMLINK_EXPOSURE			10715
#define	PARA_ILLEGAL_ALARMLINK_ROI				10716
#define	PARA_ILLEGAL_ALARMLINK_DOME				10717
#define PARA_ILLEGAL_ALARMLINK_TRACKING			10718
#define PARA_ILLEGAL_ALARMLINK_LOCALSOUND		10719
#define PARA_ILLEGAL_ALARMLINK_IPC_OUTPORT		10720
#define PARA_ILLEGAL_ALARMLINK_CLOSEUP_CAPTURE	10721 
#define PARA_ILLEGAL_ALARMLINK_IMAGE_MODE		10722

//temperature upper limit
#define	PARA_TEMUPLIMIT_ALARMLINK_SOUND			11200
#define	PARA_TEMUPLIMIT_ALARMLINK_DISPLAY		11201
#define	PARA_TEMUPLIMIT_ALARMLINK_OUTPORT		11202
#define	PARA_TEMUPLIMIT_ALARMLINK_RECORD		11203
#define	PARA_TEMUPLIMIT_ALARMLINK_PTZ			11204
#define	PARA_TEMUPLIMIT_ALARMLINK_SNAP			11205
#define	PARA_TEMUPLIMIT_ALARMLINK_SINGLEPIC		11206
#define	PARA_TEMUPLIMIT_ALARMLINK_EMAIL			11207
#define	PARA_TEMUPLIMIT_ALARMLINK_HTTP			11208
#define	PARA_TEMUPLIMIT_ALARMLINK_LASER			11209
#define	PARA_TEMUPLIMIT_ALARMLINK_WHITE			11210
#define	PARA_TEMUPLIMIT_ALARMLINK_TRAFFICTRIG	11211
#define	PARA_TEMUPLIMIT_ALARMLINK_GUARDSOUND	11212
#define	PARA_TEMUPLIMIT_ALARMLINK_PREARRANGED	11213
#define	PARA_TEMUPLIMIT_ALARMLINK_SHOUT			11214
#define	PARA_TEMUPLIMIT_ALARMLINK_EXPOSURE		11215
#define	PARA_TEMUPLIMIT_ALARMLINK_ROI			11216
#define	PARA_TEMUPLIMIT_ALARMLINK_DOME			11217
#define PARA_TEMUPLIMIT_ALARMLINK_TRACKING			11218
#define PARA_TEMUPLIMIT_ALARMLINK_LOCALSOUND		11219
#define PARA_TEMUPLIMIT_ALARMLINK_IPC_OUTPORT		11220
#define PARA_TEMUPLIMIT_ALARMLINK_CLOSEUP_CAPTURE	11221 
#define PARA_TEMUPLIMIT_ALARMLINK_IMAGE_MODE		11222

//temperature lower limit
#define	PARA_TEMLOWLIMIT_ALARMLINK_SOUND		11300
#define	PARA_TEMLOWLIMIT_ALARMLINK_DISPLAY		11301
#define	PARA_TEMLOWLIMIT_ALARMLINK_OUTPORT		11302
#define	PARA_TEMLOWLIMIT_ALARMLINK_RECORD		11303
#define	PARA_TEMLOWLIMIT_ALARMLINK_PTZ			11304
#define	PARA_TEMLOWLIMIT_ALARMLINK_SNAP			11305
#define	PARA_TEMLOWLIMIT_ALARMLINK_SINGLEPIC	11306
#define	PARA_TEMLOWLIMIT_ALARMLINK_EMAIL		11307
#define	PARA_TEMLOWLIMIT_ALARMLINK_HTTP			11308
#define	PARA_TEMLOWLIMIT_ALARMLINK_LASER		11309
#define	PARA_TEMLOWLIMIT_ALARMLINK_WHITE		11310
#define	PARA_TEMLOWLIMIT_ALARMLINK_TRAFFICTRIG	11311
#define	PARA_TEMLOWLIMIT_ALARMLINK_GUARDSOUND	11312
#define	PARA_TEMLOWLIMIT_ALARMLINK_PREARRANGED	11313
#define	PARA_TEMLOWLIMIT_ALARMLINK_SHOUT		11314
#define	PARA_TEMLOWLIMIT_ALARMLINK_EXPOSURE		11315
#define	PARA_TEMLOWLIMIT_ALARMLINK_ROI			11316
#define	PARA_TEMLOWLIMIT_ALARMLINK_DOME			11317
#define PARA_TEMLOWLIMIT_ALARMLINK_TRACKING			11318
#define PARA_TEMLOWLIMIT_ALARMLINK_LOCALSOUND		11319
#define PARA_TEMLOWLIMIT_ALARMLINK_IPC_OUTPORT		11320
#define PARA_TEMLOWLIMIT_ALARMLINK_CLOSEUP_CAPTURE	11321 
#define PARA_TEMLOWLIMIT_ALARMLINK_IMAGE_MODE		11322

//humidity upper limit
#define	PARA_HUMUPLIMIT_ALARMLINK_SOUND			11400
#define	PARA_HUMUPLIMIT_ALARMLINK_DISPLAY		11401
#define	PARA_HUMUPLIMIT_ALARMLINK_OUTPORT		11402
#define	PARA_HUMUPLIMIT_ALARMLINK_RECORD		11403
#define	PARA_HUMUPLIMIT_ALARMLINK_PTZ			11404
#define	PARA_HUMUPLIMIT_ALARMLINK_SNAP			11405
#define	PARA_HUMUPLIMIT_ALARMLINK_SINGLEPIC		11406
#define	PARA_HUMUPLIMIT_ALARMLINK_EMAIL			11407
#define	PARA_HUMUPLIMIT_ALARMLINK_HTTP			11408
#define	PARA_HUMUPLIMIT_ALARMLINK_LASER			11409
#define	PARA_HUMUPLIMIT_ALARMLINK_WHITE			11410
#define	PARA_HUMUPLIMIT_ALARMLINK_TRAFFICTRIG	11411
#define	PARA_HUMUPLIMIT_ALARMLINK_GUARDSOUND	11412
#define	PARA_HUMUPLIMIT_ALARMLINK_PREARRANGED	11413
#define	PARA_HUMUPLIMIT_ALARMLINK_SHOUT			11414
#define	PARA_HUMUPLIMIT_ALARMLINK_EXPOSURE		11415
#define	PARA_HUMUPLIMIT_ALARMLINK_ROI			11416
#define	PARA_HUMUPLIMIT_ALARMLINK_DOME			11417
#define PARA_HUMUPLIMIT_ALARMLINK_TRACKING			11418
#define PARA_HUMUPLIMIT_ALARMLINK_LOCALSOUND		11419
#define PARA_HUMUPLIMIT_ALARMLINK_IPC_OUTPORT		11420
#define PARA_HUMUPLIMIT_ALARMLINK_CLOSEUP_CAPTURE	11421 
#define PARA_HUMUPLIMIT_ALARMLINK_IMAGE_MODE		11422

//humidity lower limit
#define	PARA_HUMLOWLIMIT_ALARMLINK_SOUND		11500
#define	PARA_HUMLOWLIMIT_ALARMLINK_DISPLAY		11501
#define	PARA_HUMLOWLIMIT_ALARMLINK_OUTPORT		11502
#define	PARA_HUMLOWLIMIT_ALARMLINK_RECORD		11503
#define	PARA_HUMLOWLIMIT_ALARMLINK_PTZ			11504
#define	PARA_HUMLOWLIMIT_ALARMLINK_SNAP			11505
#define	PARA_HUMLOWLIMIT_ALARMLINK_SINGLEPIC	11506
#define	PARA_HUMLOWLIMIT_ALARMLINK_EMAIL		11507
#define	PARA_HUMLOWLIMIT_ALARMLINK_HTTP			11508
#define	PARA_HUMLOWLIMIT_ALARMLINK_LASER		11509
#define	PARA_HUMLOWLIMIT_ALARMLINK_WHITE		11510
#define	PARA_HUMLOWLIMIT_ALARMLINK_TRAFFICTRIG	11511
#define	PARA_HUMLOWLIMIT_ALARMLINK_GUARDSOUND	11512
#define	PARA_HUMLOWLIMIT_ALARMLINK_PREARRANGED	11513
#define	PARA_HUMLOWLIMIT_ALARMLINK_SHOUT		11514
#define	PARA_HUMLOWLIMIT_ALARMLINK_EXPOSURE		11515
#define	PARA_HUMLOWLIMIT_ALARMLINK_ROI			11516
#define	PARA_HUMLOWLIMIT_ALARMLINK_DOME			11617
#define PARA_HUMLOWLIMIT_ALARMLINK_TRACKING			11618
#define PARA_HUMLOWLIMIT_ALARMLINK_LOCALSOUND		11619
#define PARA_HUMLOWLIMIT_ALARMLINK_IPC_OUTPORT		11620
#define PARA_HUMLOWLIMIT_ALARMLINK_CLOSEUP_CAPTURE	11621 
#define PARA_HUMLOWLIMIT_ALARMLINK_IMAGE_MODE		11622


//pressure up limit
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_SOUND			11517
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_DISPLAY		11518
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_OUTPORT		11519
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_RECORD			11520
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_PTZ			11521
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_SNAP			11522
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_SINGLEPIC		11523
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_EMAIL			11524
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_HTTP			11525
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_LASER			11526
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_WHITE			11527
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_TRAFFICTRIG	11528
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_GUARDSOUND		11529
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_PREARRANGED	11530
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_SHOUT			11531
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_EXPOSURE		11532
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_ROI			11533
#define	PARA_PRESSURE_UP_LIMIT_ALARMLINK_DOME			11717
#define PARA_PRESSURE_UP_LIMIT_ALARMLINK_TRACKING			11718
#define PARA_PRESSURE_UP_LIMIT_ALARMLINK_LOCALSOUND			11719
#define PARA_PRESSURE_UP_LIMIT_ALARMLINK_IPC_OUTPORT		11720
#define PARA_PRESSURE_UP_LIMIT_ALARMLINK_CLOSEUP_CAPTURE	11721 
#define PARA_PRESSURE_UP_LIMIT_ALARMLINK_IMAGE_MODE			11722

//pressure lower limit
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_SOUND			11534
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_DISPLAY		11535
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_OUTPORT		11536
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_RECORD		11537
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_PTZ			11538
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_SNAP			11539
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_SINGLEPIC		11540
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_EMAIL			11541
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_HTTP			11542
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_LASER			11543
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_WHITE			11544
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_TRAFFICTRIG	11545
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_GUARDSOUND	11546
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_PREARRANGED	11547
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_SHOUT			11548
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_EXPOSURE		11549
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_ROI			11550
#define	PARA_PRESSURE_LOW_LIMIT_ALARMLINK_DOME			11817
#define PARA_PRESSURE_LOW_LIMIT_ALARMLINK_TRACKING			11818
#define PARA_PRESSURE_LOW_LIMIT_ALARMLINK_LOCALSOUND		11819
#define PARA_PRESSURE_LOW_LIMIT_ALARMLINK_IPC_OUTPORT		11820
#define PARA_PRESSURE_LOW_LIMIT_ALARMLINK_CLOSEUP_CAPTURE	11821 
#define PARA_PRESSURE_LOW_LIMIT_ALARMLINK_IMAGE_MODE		11822

//face ident
#define	PARA_FACE_IDENT_ALARMLINK_SOUND					12601
#define	PARA_FACE_IDENT_ALARMLINK_DISPLAY				12602
#define	PARA_FACE_IDENT_ALARMLINK_OUTPORT				12603
#define	PARA_FACE_IDENT_ALARMLINK_RECORD				12604
#define	PARA_FACE_IDENT_ALARMLINK_PTZ					12605
#define	PARA_FACE_IDENT_ALARMLINK_SNAP					12606
#define	PARA_FACE_IDENT_ALARMLINK_SINGLEPIC				12607
#define	PARA_FACE_IDENT_ALARMLINK_EMAIL					12608
#define	PARA_FACE_IDENT_ALARMLINK_HTTP					12609
#define	PARA_FACE_IDENT_ALARMLINK_LASER					12610
#define	PARA_FACE_IDENT_ALARMLINK_WHITE					12611
#define	PARA_FACE_IDENT_ALARMLINK_TRAFFICTRIG			12612
#define	PARA_FACE_IDENT_ALARMLINK_GUARDSOUND			12613
#define	PARA_FACE_IDENT_ALARMLINK_PREARRANGED			12614
#define	PARA_FACE_IDENT_ALARMLINK_SHOUT					12615
#define	PARA_FACE_IDENT_ALARMLINK_EXPOSURE				12616
#define	PARA_FACE_IDENT_ALARMLINK_ROI					12617
#define	PARA_FACE_IDENT_ALARMLINK_DOME					12618
#define PARA_FACE_IDENT_ALARMLINK_TRACKING				12619
#define PARA_FACE_IDENT_ALARMLINK_LOCALSOUND			12620
#define PARA_FACE_IDENT_ALARMLINK_IPC_OUTPORT			12621
#define PARA_FACE_IDENT_ALARMLINK_CLOSEUP_CAPTURE		12622 
#define PARA_FACE_IDENT_ALARMLINK_IMAGE_MODE			12623

//NVR_VCA
#define	PARA_NVR_VCA_ALARMLINK_SOUND					12701
#define	PARA_NVR_VCA_ALARMLINK_DISPLAY				    12702
#define	PARA_NVR_VCA_ALARMLINK_OUTPORT				    12703
#define	PARA_NVR_VCA_ALARMLINK_RECORD					12704
#define	PARA_NVR_VCA_ALARMLINK_PTZ						12705
#define	PARA_NVR_VCA_ALARMLINK_SNAP					    12706
#define	PARA_NVR_VCA_ALARMLINK_SINGLEPIC				12707
#define	PARA_NVR_VCA_ALARMLINK_EMAIL					12708
#define	PARA_NVR_VCA_ALARMLINK_HTTP					    12709
#define	PARA_NVR_VCA_ALARMLINK_LASER					12710
#define	PARA_NVR_VCA_ALARMLINK_WHITE					12711
#define	PARA_NVR_VCA_ALARMLINK_TRAFFICTRIG			    12712
#define	PARA_NVR_VCA_ALARMLINK_GUARDSOUND			    12713
#define	PARA_NVR_VCA_ALARMLINK_PREARRANGED			    12714
#define	PARA_NVR_VCA_ALARMLINK_SHOUT					12715
#define	PARA_NVR_VCA_ALARMLINK_EXPOSURE				    12716
#define	PARA_NVR_VCA_ALARMLINK_ROI						12717
#define	PARA_NVR_VCA_ALARMLINK_DOME					    12718
#define PARA_NVR_VCA_ALARMLINK_TRACKING					12719
#define PARA_NVR_VCA_ALARMLINK_LOCALSOUND				12720
#define PARA_NVR_VCA_ALARMLINK_IPC_OUTPORT				12721
#define PARA_NVR_VCA_ALARMLINK_CLOSEUP_CAPTURE			12722 
#define PARA_NVR_VCA_ALARMLINK_IMAGE_MODE				12723

//ZF_VCA
#define	PARA_ZF_VCA_ALARMLINK_SOUND						12801
#define	PARA_ZF_VCA_ALARMLINK_DISPLAY				    12802
#define	PARA_ZF_VCA_ALARMLINK_OUTPORT				    12803
#define	PARA_ZF_VCA_ALARMLINK_RECORD					12804
#define	PARA_ZF_VCA_ALARMLINK_PTZ						12805
#define	PARA_ZF_VCA_ALARMLINK_SNAP					    12806
#define	PARA_ZF_VCA_ALARMLINK_SINGLEPIC					12807
#define	PARA_ZF_VCA_ALARMLINK_EMAIL						12808
#define	PARA_ZF_VCA_ALARMLINK_HTTP					    12809
#define	PARA_ZF_VCA_ALARMLINK_LASER						12810
#define	PARA_ZF_VCA_ALARMLINK_WHITE						12811
#define	PARA_ZF_VCA_ALARMLINK_TRAFFICTRIG			    12812
#define	PARA_ZF_VCA_ALARMLINK_GUARDSOUND			    12813
#define	PARA_ZF_VCA_ALARMLINK_PREARRANGED			    12814
#define	PARA_ZF_VCA_ALARMLINK_SHOUT						12815
#define	PARA_ZF_VCA_ALARMLINK_EXPOSURE				    12816
#define	PARA_ZF_VCA_ALARMLINK_ROI						12817
#define	PARA_ZF_VCA_ALARMLINK_DOME					    12818
#define PARA_ZF_VCA_ALARMLINK_TRACKING					12819
#define PARA_ZF_VCA_ALARMLINK_LOCALSOUND				12820
#define PARA_ZF_VCA_ALARMLINK_IPC_OUTPORT				12821
#define PARA_ZF_VCA_ALARMLINK_CLOSEUP_CAPTURE			12822 
#define PARA_ZF_VCA_ALARMLINK_IMAGE_MODE				12823

//NavigationShip DangerArea
#define	PARA_DANGER_AREA_ALARMLINK_SOUND				12901
#define	PARA_DANGER_AREA_ALARMLINK_DISPLAY				12902
#define	PARA_DANGER_AREA_ALARMLINK_OUTPORT				12903
#define	PARA_DANGER_AREA_ALARMLINK_RECORD				12904
#define	PARA_DANGER_AREA_ALARMLINK_PTZ					12905
#define	PARA_DANGER_AREA_ALARMLINK_SNAP					12906
#define	PARA_DANGER_AREA_ALARMLINK_SINGLEPIC			12907
#define	PARA_DANGER_AREA_ALARMLINK_EMAIL				12908
#define	PARA_DANGER_AREA_ALARMLINK_HTTP					12909
#define	PARA_DANGER_AREA_ALARMLINK_LASER				12910
#define	PARA_DANGER_AREA_ALARMLINK_WHITE				12911
#define	PARA_DANGER_AREA_ALARMLINK_TRAFFICTRIG			12912
#define	PARA_DANGER_AREA_ALARMLINK_GUARDSOUND			12913
#define	PARA_DANGER_AREA_ALARMLINK_PREARRANGED			12914
#define	PARA_DANGER_AREA_ALARMLINK_SHOUT				12915
#define	PARA_DANGER_AREA_ALARMLINK_EXPOSURE				12916
#define	PARA_DANGER_AREA_ALARMLINK_ROI					12917
#define	PARA_DANGER_AREA_ALARMLINK_DOME					12918
#define PARA_DANGER_AREA_ALARMLINK_TRACKING				12919
#define PARA_DANGER_AREA_ALARMLINK_LOCALSOUND			12920
#define PARA_DANGER_AREA_ALARMLINK_IPC_OUTPORT			12921
#define PARA_DANGER_AREA_ALARMLINK_CLOSEUP_CAPTURE		12922 
#define PARA_DANGER_AREA_ALARMLINK_IMAGE_MODE			12923

//NavigationShip DangerArea
#define	PARA_BLACK_BODY_ERR_ALARMLINK_SOUND				13001
#define	PARA_BLACK_BODY_ERR_ALARMLINK_DISPLAY			13002
#define	PARA_BLACK_BODY_ERR_ALARMLINK_OUTPORT			13003
#define	PARA_BLACK_BODY_ERR_ALARMLINK_RECORD			13004
#define	PARA_BLACK_BODY_ERR_ALARMLINK_PTZ				13005
#define	PARA_BLACK_BODY_ERR_ALARMLINK_SNAP				13006
#define	PARA_BLACK_BODY_ERR_ALARMLINK_SINGLEPIC			13007
#define	PARA_BLACK_BODY_ERR_ALARMLINK_EMAIL				13008
#define	PARA_BLACK_BODY_ERR_ALARMLINK_HTTP				13009
#define	PARA_BLACK_BODY_ERR_ALARMLINK_LASER				13010
#define	PARA_BLACK_BODY_ERR_ALARMLINK_WHITE				13011
#define	PARA_BLACK_BODY_ERR_ALARMLINK_TRAFFICTRIG		13012
#define	PARA_BLACK_BODY_ERR_ALARMLINK_GUARDSOUND		13013
#define	PARA_BLACK_BODY_ERR_ALARMLINK_PREARRANGED		13014
#define	PARA_BLACK_BODY_ERR_ALARMLINK_SHOUT				13015
#define	PARA_BLACK_BODY_ERR_ALARMLINK_EXPOSURE			13016
#define	PARA_BLACK_BODY_ERR_ALARMLINK_ROI				13017
#define	PARA_BLACK_BODY_ERR_ALARMLINK_DOME				13018
#define PARA_BLACK_BODY_ALARMLINK_TRACKING				13019
#define PARA_BLACK_BODY_ALARMLINK_LOCALSOUND			13020
#define PARA_BLACK_BODY_ALARMLINK_IPC_OUTPORT			13021
#define PARA_BLACK_BODY_ALARMLINK_CLOSEUP_CAPTURE		13022
#define PARA_BLACK_BODY_ALARMLINK_IMAGE_MODE			13023

//NVR LINK IPC
#define	PARA_NVRLINKIPC_ALARMLINK_SOUND				    14001
#define	PARA_NVRLINKIPC_ALARMLINK_OUTPORT			    14002

//Vehicle identification alarm
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_SOUND				15001
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_DISPLAY				15002
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_OUTPORT				15003
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_RECORD				15004
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_PTZ					15005
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_SNAP				15006
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_SINGLEPIC			15007
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_EMAIL				15008
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_HTTP				15009
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_LASER				15010
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_WHITE				15011
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_TRAFFICTRIG			15012
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_GUARDSOUND			15013
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_PREARRANGED			15014
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_SHOUT				15015
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_EXPOSURE			15016
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_ROI					15017
#define	PARA_VEHICLE_IDENTIFY_ALARMLINK_DOME				15018
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_TRACKING			15019
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_LOCALSOUND			15020
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_IPC_OUTPORT			15021
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_CLOSEUP_CAPTURE		15022
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_IMAGE_MODE			15023
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_ALARM_LAMP			15024
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_UPLOAD_ALARM		15025
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_LINKAPP				15026
#define PARA_VEHICLE_IDENTIFY_ALARMLINK_INTELLIGENT_ALGO	15027

//Alarm threshold parameter
#define ALARM_TRIGGER_TYPE_TEMPERATURE			700
#define ALARM_TRIGGER_TYPE_HUMIDITY				701
#define ALARM_TRIGGER_TYPE_PRESSURE				702
typedef struct __tagAlarmTriggerParam
{
	int				iBuffSize;
	int				iType;				//	Alarm type
	int				iID;				//	Alarm parameter identify
	int				iValue;				//	Parameter value
	int				iValueEx;			//700,701,Low threshold value
}TAlarmTriggerParam, *PTAlarmTriggerParam;

//SDK current use               //Here only to keep the notification alarm,the other are used with the agreement of the macro definition
#define ALARM_VDO_MOTION		0
#define ALARM_VDO_REC			1
#define ALARM_VDO_LOST			2
#define ALARM_VDO_INPORT		3
#define ALARM_VDO_OUTPORT		4
#define ALARM_VDO_COVER 		5
#define ALARM_VCA_INFO			6			//VCA alarm information
#define ALARM_AUDIO_LOST		7
#define ALARM_EXCEPTION		    8
#define ALARM_VCA_INFO_EX		9			//VCA alarm information
#define ALARM_UNIQUE_ALERT_MSG	10			//unique alert alarm message

//Moving ring host alarm（To agree with the agreement)
#define ALARM_NORMAL						0x7FFFFFFF //State normal no alarm		
#define ALARM_ANALOG_UPPER_LIMIT_ON			10//Analog upper limit alarm
#define ALARM_ANALOG_LOWER_LIMIT_ON			11//Analog lower limit alarm
#define ALARM_TEMPERATURE_UPPER_LIMIT_ON	12//Temperature upper limit alarm
#define ALARM_TEMPERATURE_LOWER_LIMIT_ON	13//Temperature lower limit alarm
#define ALARM_HUMIDITY_UPPER_LIMIT_ON		14//Humidity upper limit alarm
#define ALARM_HUMIDITY_LOWER_LIMIT_ON		15//Humidity lower limit alarm
#define	ALARM_VDO_INPORT_OFF			    (3+256) //Port stop alarm
#define ALARM_ANALOG_UPPER_LIMIT_OFF		(10+256)//Analog quantity upper limit alarm 
#define ALARM_ANALOG_LOWER_LIMIT_OFF		(11+256)//Analog quantity lower limit alarm
#define ALARM_TEMPERATURE_UPPER_LIMIT_OFF	(12+256)//Temperature upper limit stop alarm
#define ALARM_TEMPERATURE_LOWER_LIMIT_OFF	(13+256)//Temperature lower limit stop alarm 
#define ALARM_HUMIDITY_UPPER_LIMIT_OFF		(14+256)//Humidity upper limit stop alarm 
#define ALARM_HUMIDITY_LOWER_LIMIT_OFF		(15+256)//Humidity lower limit stop alarm 
//////////////////////////////////////ALARM CLEAR TYPE////////////////////////////////////CMD_GET_ALARMSCHEDULE

//Alarm type definition 
#define ALARM_TYPE_MIN                          0
//以下8种报警类型定义是SDK设置（获取）参数接口专用：
//The following 8 types of alarm are defined for SDK setting (obtaining) parameter interface specific: 
#define ALARM_TYPE_VIDEO_LOST						(ALARM_TYPE_MIN + 0)		//Alarm callback cannot use it 
#define ALARM_TYPE_PORT_ALARM                   	(ALARM_TYPE_MIN + 1)		//Alarm callback cannot use it           
#define ALARM_TYPE_MOTION_DETECTION					(ALARM_TYPE_MIN + 2)		//Alarm callback cannot use it 
#define ALARM_TYPE_VIDEO_COVER						(ALARM_TYPE_MIN + 3)		//Alarm callback cannot use it 
#define	ALARM_TYPE_VCA								(ALARM_TYPE_MIN + 4)		//Alarm callback cannot use it 
#define ALARM_TYPE_AUDIOLOST						(ALARM_TYPE_MIN + 5)		//Alarm callback cannot use it 
#define ALARM_TYPE_TEMPERATURE                  	(ALARM_TYPE_MIN + 6)		//Alarm callback cannot use it 
#define ALARM_TYPE_ILLEGAL_DETECT               	(ALARM_TYPE_MIN + 7)		//Alarm callback cannot use it (Violation detection)
//以下11种报警类型定义是SDK报警回调专用：
//The following 11 alarm types are defined as SDK alarm callback specific:  
#define CALLBACK_ALARMTYPE_MOTION_DETECTION			ALARM_VDO_MOTION			//0: 移动侦测报警消息, 对应网络协议ALARM*NOTIFY发送类型2
#define CALLBACK_ALARMTYPE_RECORD_LOST				ALARM_VDO_REC				//1: 录像丢失报警消息
#define CALLBACK_ALARMTYPE_VIDEO_LOST				ALARM_VDO_LOST				//2: 视频丢失报警消息, 对应网络协议ALARM*NOTIFY发送类型0
#define CALLBACK_ALARMTYPE_INPORT					ALARM_VDO_INPORT			//3: 输入端口报警消息, 对应网络协议ALARM*NOTIFY发送类型1
#define CALLBACK_ALARMTYPE_OUTPORT					ALARM_VDO_OUTPORT			//4: 输出端口报警消息	
#define CALLBACK_ALARMTYPE_VIDEO_COVER				ALARM_VDO_COVER				//5: 视频遮挡报警消息, 对应网络协议ALARM*NOTIFY发送类型3
#define CALLBACK_ALARMTYPE_VCAINFO					ALARM_VCA_INFO				//6: 智能分析报警消息, 对应网络协议ALARM*NOTIFY发送类型4
#define CALLBACK_ALARMTYPE_AUDIO_LOST				ALARM_AUDIO_LOST			//7: 音频丢失报警消息, 对应网络协议ALARM*NOTIFY发送类型5
#define CALLBACK_ALARMTYPE_EXCEPTION				ALARM_EXCEPTION				//8: 异常报警消息
#define CALLBACK_ALARMTYPE_VCAINFOEX				ALARM_VCA_INFO_EX			//9: 智能分析报警消息扩展，支持所有平台，可替代CALLBACK_ALARMTYPE_VCAINFO
#define CALLBACK_ALARMTYPE_UNIQUEALERTMSG			ALARM_UNIQUE_ALERT_MSG		//10: 特色警戒报警消息
//以下报警类型定义是SDK报警回调和设置（获取）参数接口共用：
//The following alarm type definitions are shared by SDK alarm callback and setting (getting) parameter interface: 
#define ALARM_TYPE_TEMPERATURE_UPPER_LIMIT			(ALARM_TYPE_MIN + 12)
#define ALARM_TYPE_TEMPERATURE_LOWER_LIMIT			(ALARM_TYPE_MIN + 13)
#define ALARM_TYPE_HUMIDITY_UPPER_LIMIT				(ALARM_TYPE_MIN + 14)
#define ALARM_TYPE_HUMIDITY_LOWER_LIMIT				(ALARM_TYPE_MIN + 15)
#define ALARM_TYPE_PRESSURE_UPPER_LIMIT				(ALARM_TYPE_MIN + 16)
#define ALARM_TYPE_PRESSURE_LOWER_LIMIT				(ALARM_TYPE_MIN + 17)
#define ALARM_TYPE_TEMPERATURE_HUMIDITY_FAULT		(ALARM_TYPE_MIN + 19)		//Temperature and humidity fault alarm
#define ALARM_TYPE_FACE_IDENT						(ALARM_TYPE_MIN + 20)
#define ALARM_TYPE_NVR_VCA							(ALARM_TYPE_MIN + 21)
#define	ALARM_TYPE_MOLP								(ALARM_TYPE_MIN + 22)		//malicious occlusion license plate
#define ALARM_TYPE_RAINFALL							(ALARM_TYPE_MIN + 23)		//rainfall alarm		
#define ALARM_TYPE_ALERT_WATER_LEVEL				(ALARM_TYPE_MIN + 24)		//alert water level alarm
#define ALARM_TYPE_SENSOR_ABNORMAL					(ALARM_TYPE_MIN + 25)		//Sensor abnormal alarm
#define ALARM_TYPE_ZF_VCA							(ALARM_TYPE_MIN + 26)		//zf Vca
#define ALARM_TYPE_ILLEGAL_IP						(ALARM_TYPE_MIN + 27)		//Illegal IP login
#define ALARM_TYPE_DANGEROUS_AREA					(ALARM_TYPE_MIN + 28)		//Navigation ship, dangerous area alarm
#define ALARM_TYPE_VOLTAGE_HIGH						(ALARM_TYPE_MIN + 29)		//电压上限报警
#define ALARM_TYPE_VOLTAGE_LOW						(ALARM_TYPE_MIN + 30)		//电压下限报警
#define ALARM_TYPE_BLACK_BODY_DETECT				(ALARM_TYPE_MIN + 31)		//黑体异常报警
#define ALARM_TYPE_ALERT_WATERLEVEL_LOWER_LIMIT		(ALARM_TYPE_MIN + 32)		//警戒水位报警(下限)
#define ALARM_TYPE_PREALERT_WATERLEVEL_UPPER_LIMIT	(ALARM_TYPE_MIN + 33)		//预警水位报警(上限)
#define ALARM_TYPE_PREALERT_WATERLEVEL_LOWER_LIMIT	(ALARM_TYPE_MIN + 34)		//预警水位报警(下限)
#define ALARM_TYPE_MOTHERBOARD_DISASSEMBLE      	(ALARM_TYPE_MIN + 35)		//主板拆卸报警
#define ALARM_TYPE_OUT_CARDREADER_DISASSEMBLE      	(ALARM_TYPE_MIN + 36)		//外接读卡器拆卸报警
#define ALARM_TYPE_BUTTON                       	(ALARM_TYPE_MIN + 37)		//按钮报警
#define ALARM_TYPE_VEHICLE_IDENTIFICATION       	(ALARM_TYPE_MIN + 38)		//车辆识别报警
#define ALARM_TYPE_WATER_ELECTRONIC_OVERRANGE   	(ALARM_TYPE_MIN + 39)		//水利电子围栏超范围报警
#define ALARM_TYPE_WATER_SPECTRAL_DATA_ABNORMAL     (ALARM_TYPE_MIN + 40)		//40，水利光谱数据异常报警
#define ALARM_TYPE_MOTION_DETECTION_CAR			    (ALARM_TYPE_MIN + 41)		//移动侦测之车形报警

//定制报警类型
#define ALARM_TYPE_PAYMENT_BEHAVIOR						10000					//支付行为报警
#define ALARM_TYPE_ELEVATOR_SERVICE						10001					//电梯业务报警

//SDK customization 
#define ALARM_TYPE_EXTERN_BUTTON					(ALARM_TYPE_MIN + 10000)	//external button alarm
#define ALARM_TYPE_EXCPETION						(ALARM_TYPE_MIN + 100)		//Abnormal alarm
#define ALARM_TYPE_ALL								(ALARM_TYPE_MIN + 255)		//All
#define ALARM_TYPE_MAX								(ALARM_TYPE_ALL + 1)
#define CALLBACK_ALARMTYPE_INPORT_OFF				ALARM_VDO_INPORT_OFF		//Alarm callback use it 
#define ALARM_TYPE_PORT_ALARM_OFF					(ALARM_TYPE_MIN + 1 + 256)	//Port stop alarm, inner use, Alarm callback cannot use it 
#define CALLBACK_ALARMTYPE_ONVIF_VCAALARM			(ALARM_TYPE_MIN + 20000)	//onvif vca alarm


///////////////////////////////////////ExceptionType///////////////////////////////////
#define  EXCEPTION_TYPE_MIN						0
#define  EXCEPTION_TYPE_DISK_FULL				(EXCEPTION_TYPE_MIN + 0)  //Disk full			
#define  EXCEPTION_TYPE_NO_DISK					(EXCEPTION_TYPE_MIN + 1)  //No disk
#define  EXCEPTION_TYPE_DISK_IO_ERROR			(EXCEPTION_TYPE_MIN + 2)  //Disk read and write error
#define  EXCEPTION_TYPE_NOALLOW_ACCESS			(EXCEPTION_TYPE_MIN + 3)  //Illegal access
#define  EXCEPTION_TYPE_IP_COLLISION			(EXCEPTION_TYPE_MIN + 4)  //IP conflict
#define  EXCEPTION_TYPE_NETWORK_INTERRUPT		(EXCEPTION_TYPE_MIN + 5)  //Network interrupt
#define  EXCEPTION_TYPE_SYSTEM_BUSY				(EXCEPTION_TYPE_MIN + 6)  //System busy
#define  EXCEPTION_TYPE_NO_REDUNDANCY_DISK		(EXCEPTION_TYPE_MIN + 7)  //No redundant disk
#define  EXCEPTION_TYPE_ABNORMAL_VOLTAGE		(EXCEPTION_TYPE_MIN + 8)  //Voltage abnormity
#define  EXCEPTION_TYPE_MAC_COLLISION			(EXCEPTION_TYPE_MIN + 9)  //MAC conflict
#define  EXCEPTION_TYPE_RAID                    (EXCEPTION_TYPE_MIN + 10) //Array exception
#define  EXCEPTION_TYPE_HOTBACKUP               (EXCEPTION_TYPE_MIN + 11) //Thermal anomaly
#define  EXCEPTION_TYPE_OVERLOAD				(EXCEPTION_TYPE_MIN + 12) //PSE overload
#define  EXCEPTION_TYPE_DISK_OVERLOAD			(EXCEPTION_TYPE_MIN + 13) //Disk overload
#define  EXCEPTION_TYPE_RECODE					(EXCEPTION_TYPE_MIN + 14) //recode
#define  EXCEPTION_TYPE_PASSOWRD_ULTRALIMIT		(EXCEPTION_TYPE_MIN + 15) 
#define  EXCEPTION_TYPE_SMART					(EXCEPTION_TYPE_MIN + 16) //smart
#define  EXCEPTION_TYPE_FTP_SERVER				(EXCEPTION_TYPE_MIN + 17) 
#define  EXCEPTION_TYPE_IO_CHIP					(EXCEPTION_TYPE_MIN + 18) 
#define  EXCEPTION_TYPE_DISK_TEMPERATURE		(EXCEPTION_TYPE_MIN + 19)
#define  EXCEPTION_TYPE_WATERGAUGE_CALIBRATION	(EXCEPTION_TYPE_MIN + 20) //Water gauge calibration abnormal alarm
#define	 EXCEPTION_TYPE_SHM_DISK				(EXCEPTION_TYPE_MIN + 21)
#define	 EXCEPTION_TYPE_DISK_LIFE				(EXCEPTION_TYPE_MIN + 22)
#define	 EXCEPTION_TYPE_CD_DRIVE_LIFE		    (EXCEPTION_TYPE_MIN + 23)
#define	 EXCEPTION_TYPE_BATTERY_ABNORMAL		(EXCEPTION_TYPE_MIN + 24)//Abnormal battery
#define	 EXCEPTION_TYPE_GPSINFO_TIMEOUT		    (EXCEPTION_TYPE_MIN + 25)//GPS information timeout
#define  EXCEPTION_TYPE_IPC_ABNORMAL_VOLTAGE	(EXCEPTION_TYPE_MIN + 26)//IPC Abnormal Voltage
#define  EXCEPTION_TYPE_VIDEO_LOST	            (EXCEPTION_TYPE_MIN + 27)//Video lost
#define  EXCEPTION_TYPE_SYSTEMTIME	            (EXCEPTION_TYPE_MIN + 28)//System time exception
#define  EXCEPTION_TYPE_PASSWORDCARD	        (EXCEPTION_TYPE_MIN + 29)//加密卡状态异常（add for DZ201300)
#define  EXCEPTION_TYPE_SOLARCONTROLER          (EXCEPTION_TYPE_MIN + 30)//solar controller low voltage
#define  EXCEPTION_TYPE_DISKGROUPSPACE          (EXCEPTION_TYPE_MIN + 31)//a group disk space
#define  EXCEPTION_TYPE_ALG_PROCESS_RUNNING     (EXCEPTION_TYPE_MIN + 32)//32,算法进程运行异常
#define  EXCEPTION_TYPE_DECODING_PERFORMANCE    (EXCEPTION_TYPE_MIN + 33)//33 解码性能超限
#define  EXCEPTION_TYPE_EVAP_DETECTION          (EXCEPTION_TYPE_MIN + 34)//34、蒸发量检测异常
#define  EXCEPTION_TYPE_INJECTION               (EXCEPTION_TYPE_MIN + 35)//35、蒸发池注水异常
#define  EXCEPTION_TYPE_WATERLEVEL_DETECTION    (EXCEPTION_TYPE_MIN + 36)//36、水位检测异常
#define  EXCEPTION_TYPE_EVAP_SUDDEN_CHANGE      (EXCEPTION_TYPE_MIN + 37)//37、蒸发量数据突变异常
#define  EXCEPTION_TYPE_PROOF_FILE_OVERSIZE     (EXCEPTION_TYPE_MIN + 38)//38、证据文件超大预警
#define  EXCEPTION_TYPE_FACE_IMG_LIMIT		    (EXCEPTION_TYPE_MIN + 39)//39、人脸库底图达到上限
#define  EXCEPTION_TYPE_MAX						(EXCEPTION_TYPE_MIN + 40)

#define  EXCEPTION_DZ_TYPE_MIN								1000
#define  EXCEPTION_DZ_TYPE_HEATBEAT				(EXCEPTION_DZ_TYPE_MIN + 0)  //beat heat exception		
#define  EXCEPTION_DZ_TYPE_SWITCHING			(EXCEPTION_DZ_TYPE_MIN + 1)  //switching exception
#define  EXCEPTION_DZ_TYPE_MAX					(EXCEPTION_DZ_TYPE_MIN + 2)  
//////////////////////////////////////Exception Handle////////////////////////////////////
#define  EXCEPTION_HANDLE_ENABLE                 0x01         //Enable exception handling
#define  EXCEPTION_HANDLE_DIALOG				 0x02         //Whether prompt dialog box
#define  EXCEPTION_HANDLE_LINK_BELL              0x04         //Whether the linkage buzzer
#define  EXCEPTION_HANDLE_LINK_NETCLIENT         0x08         //Whether to notify the network client
#define  EXCEPTION_HANDLE_LINK_EMAIL			 0x10         //Whether or not the linkage mail

//////////////////////////////////////AlarmLinkType////////////////////////////////////
#define ALARMLINKTYPE_MIN                               0  
#define ALARMLINKTYPE_VIDEOLOST_LINKRECORD     	(ALARMLINKTYPE_MIN + 0)   //Video loss alarm linkage video    
#define ALARMLINKTYPE_VIDEOLOST_LINKSNAP   	    (ALARMLINKTYPE_MIN + 1)   //Video loss alarm linkage capture
#define ALARMLINKTYPE_VIDEOLOST_LINKOUTPORT     (ALARMLINKTYPE_MIN + 3)   //Video loss alarm linkage output port
#define ALARMLINKTYPE_MOTIONDETECT_LINKRECORD   (ALARMLINKTYPE_MIN + 4)   //Motion detection alarm linkage video  
#define ALARMLINKTYPE_MOTIONDETECT_LINKSNAP     (ALARMLINKTYPE_MIN + 5)   //Motion detection alarm linkage capture
#define ALARMLINKTYPE_MOTIONDETECT_LINKOUTPORT 	(ALARMLINKTYPE_MIN + 6)   //Motion detection alarm linkage output port
#define ALARMLINKTYPE_PORTALARM_LINKRECORD     	(ALARMLINKTYPE_MIN + 7)   //Port alarm linkage video
#define ALARMLINKTYPE_PORTALARM_LINKSNAP       	(ALARMLINKTYPE_MIN + 8)   //Port alarm linkage capture
#define ALARMLINKTYPE_PORTALARM_LINKOUTPORT    	(ALARMLINKTYPE_MIN + 9)   //Port alarm linkage output port

/************************************************************************/
/*  Intelligent Transportation    Electronic Police*/
/************************************************************************/

#define SN_ENCRYPT_TYPE_MASK					0x01
#define SN_ENCRYPT_TYPE_RECOGNIZE_ARITHMETIC	0x00			//Recognition algorithm and encryption type
#define SN_ENCRYPT_TYPE_PROGRAM					0x01			//Program encryption type

#define SYSTEM_TYPE_MASK					0x04					
#define SYSTEM_TYPE_DEBUG					0x00				//Debugging
#define SYSTEM_TYPE_GATE					0x03				//Bayonet
#define SYSTEM_TYPE_ECOP					0x04				//Electronic Police
                             
#define SN_REG_MASK							0x02				
#define SN_REG_RECOGNIZE_DOG				0x01				//Identify the dog
#define SN_REG_ENCRYPT_DOG					0x02				//Intelligent traffic encryption dog

//IPC 3MP
#define MAX_CHANNEL_TYPE		2
#define MAX_REALTYPE_NUM        5  //Real time data type ，0：Real time brightness value of high definition camera;1：The camera real-time information sharpness



struct STRCT_REAL_STATE 
{
	int        m_iTypes[MAX_REALTYPE_NUM];  //Every type's type value
	int        m_iValues[MAX_REALTYPE_NUM]; //Real time data corresponding to each type
};

//ITScb@121010
//Smart camera extension
////CMD Highlights
///************************************************************************
//*	Hdvision camera time slot                                                                     
//************************************************************************/
//#define ITS_CMD_SET_MIN                         0
//#define ITS_CMD_SET_TIMERANGE_ENABLE			0                           //Set the time period to enable
//#define ITS_CMD_SET_TIMERANGE_AGCBLOCK			1							//Set the metering area for time period to enable
//#define ITS_CMD_SET_TIMERANGE_FLASHLAMP			2							//Set the time period to enable the flash
//
//#define ITS_CMD_GET_TIMERANGE_ENABLE			3							//Get the time period to enable
//#define ITS_CMD_GET_TIMERANGE_AGCBLOCK			4							//Get the metering area for time period to enable
//#define ITS_CMD_GET_TIMERANGE_FLASHLAMP			5							//Get the time period to enable the flash
///************************************************************************/
///*   Intelligent transportation lane parameters                                                                
///************************************************************************/
//#define ITS_ROADWAY_CMD_SET_MIN     0
//#define ITS_ROADWAY_CMD_SET_ENABLE  (ITS_ROADWAY_CMD_SET_MIN + 0)	    //Set lane enable
//#define ITS_ROADWAY_CMD_SET_LOOP    (ITS_ROADWAY_CMD_SET_MIN + 1)		//Set lane coil
//#define ITS_ROADWAY_CMD_SET_TIME    (ITS_ROADWAY_CMD_SET_MIN + 2)		//Set lane time
//#define ITS_ROADWAY_CMD_SET_SPEED   (ITS_ROADWAY_CMD_SET_MIN + 3)		//Set lane speed
//#define ITS_ROADWAY_CMD_SET_RECO    (ITS_ROADWAY_CMD_SET_MIN + 4)		//Set lane identify area
//#define ITS_ROADWAY_CMD_SET_VLOOP   (ITS_ROADWAY_CMD_SET_MIN + 5)		//Set fictitious coil
//#define ITS_ROADWAY_CMD_SET_LIGHT   (ITS_ROADWAY_CMD_SET_MIN + 6)		//Set lane signal lights
//#define ITS_ROADWAY_CMD_SET_CAPTURE (ITS_ROADWAY_CMD_SET_MIN + 7)		//Set lane capture
//#define ITS_ROADWAY_CMD_SET_REFERENCELINE	(ITS_ROADWAY_CMD_SET_MIN + 8)		//Set lane mix
//#define ITS_ROADWAY_CMD_SET_MAX     (ITS_ROADWAY_CMD_SET_MIN + 9)		//
//
//#define ITS_ROADWAY_CMD_GET_MIN     0
//#define ITS_ROADWAY_CMD_GET_ENABLE  (ITS_ROADWAY_CMD_GET_MIN + 0)	    //Get lane enable
//#define ITS_ROADWAY_CMD_GET_LOOP    (ITS_ROADWAY_CMD_GET_MIN + 1)		//Get lane coil
//#define ITS_ROADWAY_CMD_GET_TIME    (ITS_ROADWAY_CMD_GET_MIN + 2)		//Get lane time
//#define ITS_ROADWAY_CMD_GET_SPEED   (ITS_ROADWAY_CMD_GET_MIN + 3)		//Get lane speed
//#define ITS_ROADWAY_CMD_GET_RECO    (ITS_ROADWAY_CMD_GET_MIN + 4)		//Get lane identify area
//#define ITS_ROADWAY_CMD_GET_VLOOP   (ITS_ROADWAY_CMD_GET_MIN + 5)		//Get fictitious coil
//#define ITS_ROADWAY_CMD_GET_LIGHT   (ITS_ROADWAY_CMD_GET_MIN + 6)		//Get lane signal lights
//#define ITS_ROADWAY_CMD_GET_CAPTURE (ITS_ROADWAY_CMD_GET_MIN + 7)		//Get lane capture
//#define ITS_ROADWAY_CMD_GET_REFERENCELINE	(ITS_ROADWAY_CMD_GET_MIN + 8)	//Get lane mix
//#define ITS_ROADWAY_CMD_GET_MAX     (ITS_ROADWAY_CMD_GET_MIN + 9)		//
///************************************************************************/
///*	Intelligent vehicle license plate optimization
///************************************************************************/
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN                   0
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_FIRST_HZ				(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 0)	    //Set up the license plate optimization of the first GB2312 characters
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_NOPLATE_ENABLE		(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 1)		//Set whether to allow no license plate
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_OTHER					(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 2)		//Set to be retrieved and not trusted in the license plate
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MAX					(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 3)		//
//
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN                   0										   		
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_FIRST_CHARACTER       (ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 0)		//Get up the license plate optimization of the first GB2312 characters
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_NOPLATE_ENABLE		(ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 1)		//Get whether to allow no license plate
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_OTHER					(ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 2)		//Get to be retrieved and not trusted in the license plate
//#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MAX					(ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 3)		//
///************************************************************************/
///*  Traffic violation detection ball machine
///************************************************************************/
//#define ITS_EXTRAINFO_CMD_SET_MIN								 0
////Illegal parking project
//#define ITS_ILLEGALPARK_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 0)	    //Set up illegal parking parameters
//#define ITS_ILLEGALPARKPARM_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 1)		//Set up illegal parking parameters of the calibration area of illegal parking
//#define ITS_LICENSEOPTIMIZEOTHER_CMD_SET						(ITS_EXTRAINFO_CMD_SET_MIN + 2)		//Set with the license plate and the license plate can not be trusted
//#define ITS_OPTIM_CMD_SET										(ITS_EXTRAINFO_CMD_SET_MIN + 3)		//Set to identify the license plate by GB2312 characters
//#define ITS_RECOPARAM_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 4)		//Set identification algorithm parameters
//#define ITS_ROADWAYENABLE_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 5)		//Set lane
//#define ITS_CAMLOCATION_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 6)		//Set device information
//#define ITS_EXACTCONFIRM_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 7)		//Set precise location information
////Intelligent camera
//#define ITS_VIDEODECTECT_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 8)		//Image detection parameters
//#define ITS_DETECTAREA_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 10)	//Trajectory analysis area detection parameters 
//#define ITS_RECOTYPE_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 11)	//Algorithm type parameters
////Traffic integrated machine
//#define ITS_REDLIGHTDETECTAREA_CMD_SET							(ITS_EXTRAINFO_CMD_SET_MIN + 12)	//Set the red light to detect the regional parameters
//#define ITS_ENABLE_CMD_SET										(ITS_EXTRAINFO_CMD_SET_MIN + 13)	//Set traffic related function enable protocol
//
//#define ITS_EXTRAINFO_CMD_SET_MAX								(ITS_EXTRAINFO_CMD_SET_MIN + 15)	//
//
//#define ITS_EXTRAINFO_CMD_GET_MIN								 0
////Illegal parking project
//#define ITS_ILLEGALPARK_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 0)	    //Get illegal parking parameters
//#define ITS_ILLEGALPARKPARM_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 1)		//Get illegal parking parameters of the calibration area of illegal parking
//#define ITS_LICENSEOPTIMIZEOTHER_CMD_GET						(ITS_EXTRAINFO_CMD_GET_MIN + 2)		//Get with the license plate and the license plate can not be trusted
//#define ITS_OPTIM_CMD_GET										(ITS_EXTRAINFO_CMD_GET_MIN + 3)		//Get to identify the license plate by GB2312 characters
//#define ITS_RECOPARAM_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 4)		//Get identification algorithm parameters
//#define ITS_ROADWAYENABLE_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 5)		//Get lane
//#define ITS_CAMLOCATION_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 6)		//Get device information
////Intelligent camera
//#define ITS_VIDEODECTECT_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 7)		//Image detection parameters
//#define ITS_DETECTAREA_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 9)		//Trajectory analysis area detection parameters 
//#define ITS_RECOTYPE_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 10)	//Algorithm type parameters
//#define ITS_CAMRATYPE_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 11)	//Camera type
////Traffic integrated machine
//#define ITS_REDLIGHTDETECTAREA_CMD_GET							(ITS_EXTRAINFO_CMD_SET_MIN + 12)	//Get the red light to detect the regional parameters
//#define ITS_ENABLE_CMD_GET										(ITS_EXTRAINFO_CMD_SET_MIN + 13)	//Get traffic related function enable protocol
//#define ITS_VERSIONPRO_CMD_GET									(ITS_EXTRAINFO_CMD_SET_MIN + 14)	//Get camera version information
//
//#define ITS_EXTRAINFO_CMD_GET_MAX								(ITS_EXTRAINFO_CMD_GET_MIN + 15)	//
////CMD highlights END   Suggestion to retain the original location of the notes,so that the relevant parameters are in the same position,improving readability.

/************************************************************************
*	Camera photometric area coordinates                                                                  
************************************************************************/
#define MAX_ITS_BLOCK        5  

typedef struct ITS_TBlock  
{
	int iX;					
	int iY;
}ITS_TBlock;

struct ITS_TOTAL_TBlock
{
	ITS_TBlock block[MAX_ITS_BLOCK];
};
/************************************************************************
*	Hdvision camera time slot                                                                     
************************************************************************/
#define ITS_CMD_SET_TIMERANGE_ENABLE			0                           //Set the time period to enable
#define ITS_CMD_SET_TIMERANGE_AGCBLOCK			1							//Set the time segment metering area to enable
#define ITS_CMD_SET_TIMERANGE_FLASHLAMP			2							//Set the time period to enable flash

#define ITS_CMD_GET_TIMERANGE_ENABLE			3							//Get time segment enable
#define ITS_CMD_GET_TIMERANGE_AGCBLOCK			4							//Get the time segment metering area to enable
#define ITS_CMD_GET_TIMERANGE_FLASHLAMP			5							//Get the time period to enable flash

#define MAX_ITS_TIMERANGE                8

typedef struct ITS_TTimeRange 
{
	int iEnable;								//Whether to enable this time period，1-Enable，0-Not enable
	int iTimeRange;								//Time frame: bit24-bit31:Start hour
	//					bit16-bit23:Start minute
	//					bit8-bit15:Start hour
	//					bit0-bit7:End minute	
}ITS_TTimeRange;

typedef struct ITS_TOTAL_TTimeRange 
{
	ITS_TTimeRange timerange[MAX_ITS_TIMERANGE];
}ITS_TOTAL_TTimeRange;
/************************************************************************
*	Hdvision camera time slot general parameters                                                                     
************************************************************************/
#define MAX_ITS_TEMPLATE                8
#define MAX_TIME_RANGE_PARAM  			44
typedef struct ITS_TTimeRangeParam 
{
	int iType;							// 0-Set gain 1-Set exposure 2-Set white balance 3-Set brightness
	int iAutoEnable[MAX_TIME_RANGE_PARAM];	//  1.1-Automatic,0-Manual.
	//	2.Special note（When iType is 12,0-Daylight mode；1-Fluorescent lamp mode;2-No white balance).
	//	3.When iType is 25,represent:1-Automatic;0-Manual。
	//  4.When iType is 8, represent:1-Automatic（Normal mode），0-Manual 2-Automatic（Expert mode）3-ITS Camera 3D Denoise level   //add by wd @20130619
	int iParam1[MAX_TIME_RANGE_PARAM];		//0--Aperture adjustment :Manual down :represent concrete value; Automatic down:1 Openning aperture, 0 Closing aperture .
	//1--Super wide dynamic: 1 Opening ;0 Closing
	//2--Backlight opening :1 Opening; 0 Closing
	//3--Time of exposure: Manual down : concrete value ; Automatic down: expected value	//4--Shutter adjustment : Manual:concrete; Automatic down: Expected value
	//5--Gain control:Manual down: Concrete; automatic: Expected value
	//6--Gamma control: Manual down: concrete; automatic :Expected value
	//7--Sharpness adjustment : Manual down: concrete; Automatic:Expected value
	//8--Noise reduction control: Manual down: concrete; Automatic (Simple modle) down :Expected :Automatic (Expert Mode) dowm: 2D noise reduation   ITS camera 3D noise level :3-high, 2-middle,1-low,0-minimum //add by wd @20130619
	//9--Exposure area: Upper left comer X coordinates
	//10--Background light compensation area : Upper left comer X coordinates
	//11--AF Zone setting: Upper left comer X coordinates
	//12--Target brightness adjustment :Manual down: Concrete; Automatic down: Expected value
	//13--White balance adjustment : Manual down: R component of white balance:0-255; Automatic down:R component correction coefficient of white balance: Range 100-200, Accuracy 5
	//14--jpeg Image quality
	//15--lut Enable
	//16--Automatic brightness adjustment enable
	int iParam2[MAX_TIME_RANGE_PARAM];		//0--Aperture adjustment: not used
	//1--Super wide dynamic: not used
	//2--Background light opening: not used
	//3--Time of exposure: Manual down : not used ; Automatic down: Upper limit value
	//4--Shutter adjustment : Manual down: not used ;Automatic down: Upper limit value
	//5--Gain control:Manual down: not used; automatic down: Upper limit value
	//6--gamma contro:Manual down: not used; automatic down: Upper limit value
	//7--Sharpness adjustment :Manual down: not used; automatic down: Upper limit value
	//8--Noise reduction control:Manual down: not used; automatic(simple) down: not used; automatic(Expert Mode) down: 3D noise reduation expected value
	//9--Exposure area: Upper left comer Y coordinates
	//10--Background light compensation area : Upper left comer Y coordinates
	//11--AF Zone setting: Upper left comer Y coordinates
	//12--Target brightness adjustment :Manual down: Concrete; Automatic down: Expected value
	//13--White balance adjustment : Manual down: G component of white balance:0-255; Automatic down:G component correction coefficient of white balance: Range 100-200, Accuracy 5
	//14--jpeg Image quality
	//15--lut Enable
	//16--Automatic brightness adjustment enable
	int iParam3[MAX_TIME_RANGE_PARAM];		//0--Aperture adjustment:not used
	//1--Super wide dynamic: not used
	//2--Background light opening: not used
	//3--Time of exposure: Manual down : not used ; Automatic down: Lower limit value
	//4--Shutter adjustment : Manual down: not used ;Automatic down: Lower limit value
	//5--Gain control:Manual down: not used; automatic down: Lower limit value
	//6--gamma contro:Manual down: not used; automatic down: Lower limit value
	//7--Sharpness adjustment :Manual down: not used; automatic down: Lower limit value
	//8--Noise reduction control:Manual down: not used; automatic down: not used; 
	//9--Exposure area: Lower right comer X coordinates
	//10--Background light compensation area :  Lower right comer X coordinates
	//11--AF Zone setting: Lower right comer X coordinates
	//12--Target brightness adjustment :Manual down: Concrete; Automatic down: Lower limit value
	//13--White balance adjustment : Manual down: B component of white balance:0-255; Automatic down:B component correction coefficient of white balance: Range 100-200, Accuracy 5
	//14--jpeg Image quality
	//15--lut Enable
	//16--Automatic brightness adjustment enable
	int iParam4[MAX_TIME_RANGE_PARAM];		//0--Aperture adjustment:not used
	//1--Super wide dynamic: not used
	//2--Background light opening: not used
	//3--Time of exposure: 
	//4--Shutter adjustment : 
	//5--Gain control: not used;
	//6--gamma contro: not used; 
	//7--Sharpness adjustment : not used; 
	//8--Noise reduction control: not used; 
	//9--Exposure area: Lower right comer Y coordinates
	//10--Background light compensation area :  Lower right comer Y coordinates
	//11--AF Zone setting: Lower right comer Y coordinates
	//12--Target brightness adjustment :not used
	//13--White balance adjustment :not used
	//14--jpeg Image quality
	//15--lut Enable
	//16--Automatic brightness adjustment enable
}ITS_TTimeRangeParam;

typedef struct ITS_TOTAL_TTimeRangeParam 
{
	ITS_TTimeRangeParam timerangeparam[MAX_ITS_TEMPLATE +1];
}ITS_TOTAL_TTimeRangeParam;

#define MAX_EXPOSAL_TYPE	6
typedef struct tagDetailCameraParam 
{
	int iSize;
	int iChanNo;
	int iTemplateIndex;
	int iType;		 //reference CAMERA_PARA_MAX
	int iAutoEnable; //reference ITS_TTimeRangeParam iAutoEnable
	int iParam1;	 //reference ITS_TTimeRangeParam iParam1
	int iParam2;	 //reference ITS_TTimeRangeParam iParam2
	int iParam3;	 //reference ITS_TTimeRangeParam iParam3
	int iParam4;	 //reference ITS_TTimeRangeParam iParam4
	int iExposalType;//0-monitoring image (original high clear parameter logic) 1-capture image 2-analysis image
					 //3-infrared monitoring image 4-infrared capture image 5-infrared analysis image(temporarily not held)
}DetailCameraParam;

typedef struct ITS_TTemplateMap 
{
	int iTemplateID;					//Template number
	char cTemplateName[64];				//Template name
}ITS_TTemplateMap;

//interface in buf
typedef struct tagITS_TemplateName 
{
	int			iSize;
	int			iTemplateID;					
	char		cTemplateName[LEN_64];				
}ITS_TemplateName;

typedef struct ITS_TOTAL_TTemplateMap 
{
	ITS_TTemplateMap templatemap[MAX_ITS_TIMERANGE];
	int iHDTemplateType;
	int iDayTemplateID;
	int iNightTemplateID;
}ITS_TOTAL_TTemplateMap;


/************************************************************************
*	Hdvision camera working mode                                                                    
************************************************************************/
typedef struct ITS_CamWorkMode 
{
	int iWorkMode;							//Working mode  0-Bayonet mode   1- Kabuto Attack Ride Clock Up
	int iInterval;								//Time interval	
}ITS_CamWorkMode;

/************************************************************************/
//*   Intelligent traffic lane parameters                                                                 
/************************************************************************/

//From 9 begin, GET/SET use unified macro definition,note changes to ITS_ROADWAY_CMD_GET_MAX/ITS_ROADWAY_CMD_SET_MAX value
#define ITS_ROADWAY_CMD_SET_MIN				0
#define ITS_ROADWAY_CMD_SET_ENABLE			(ITS_ROADWAY_CMD_SET_MIN + 0)	    //Set lane enable
#define ITS_ROADWAY_CMD_SET_LOOP			(ITS_ROADWAY_CMD_SET_MIN + 1)		//Set lane coil
#define ITS_ROADWAY_CMD_SET_TIME			(ITS_ROADWAY_CMD_SET_MIN + 2)		//Set lane time
#define ITS_ROADWAY_CMD_SET_SPEED			(ITS_ROADWAY_CMD_SET_MIN + 3)		//Set lane speed
#define ITS_ROADWAY_CMD_SET_RECO			(ITS_ROADWAY_CMD_SET_MIN + 4)		//Set lane identify area
#define ITS_ROADWAY_CMD_SET_VLOOP			(ITS_ROADWAY_CMD_SET_MIN + 5)		//Set fictitious coil
#define ITS_ROADWAY_CMD_SET_LIGHT			(ITS_ROADWAY_CMD_SET_MIN + 6)		//Set lane signal lights
#define ITS_ROADWAY_CMD_SET_CAPTURE			(ITS_ROADWAY_CMD_SET_MIN + 7)		//Set lane capture
#define ITS_ROADWAY_CMD_SET_REFERENCELINE	(ITS_ROADWAY_CMD_SET_MIN + 8)		//Set lane mix
#define ITS_ROADWAY_CMD_CHNLCARSPEED        (ITS_ROADWAY_CMD_SET_MIN + 9)		//Set/Get Speed detection parameters for different vehicle types in the lane
#define ITS_ROADWAY_CMD_CHNLDELAYDIS        (ITS_ROADWAY_CMD_SET_MIN + 10)		//Set/Get Lane delay capture distance 
#define ITS_ROADWAY_CMD_CHNLANEEXPARAM      (ITS_ROADWAY_CMD_SET_MIN + 11)		//Set/Get Lane extern param
#define ITS_ROADWAY_CMD_SET_MAX				(ITS_ROADWAY_CMD_SET_MIN + 12)		

#define ITS_ROADWAY_CMD_GET_MIN				0
#define ITS_ROADWAY_CMD_GET_ENABLE			(ITS_ROADWAY_CMD_GET_MIN + 0)	    //Get lane enable
#define ITS_ROADWAY_CMD_GET_LOOP			(ITS_ROADWAY_CMD_GET_MIN + 1)		//Get lane coil
#define ITS_ROADWAY_CMD_GET_TIME			(ITS_ROADWAY_CMD_GET_MIN + 2)		//Get lane time
#define ITS_ROADWAY_CMD_GET_SPEED			(ITS_ROADWAY_CMD_GET_MIN + 3)		//Get lane speed
#define ITS_ROADWAY_CMD_GET_RECO			(ITS_ROADWAY_CMD_GET_MIN + 4)		//Get lane identify area
#define ITS_ROADWAY_CMD_GET_VLOOP			(ITS_ROADWAY_CMD_GET_MIN + 5)		//Get fictitious coil
#define ITS_ROADWAY_CMD_GET_LIGHT			(ITS_ROADWAY_CMD_GET_MIN + 6)		//Get lane signal lights
#define ITS_ROADWAY_CMD_GET_CAPTURE			(ITS_ROADWAY_CMD_GET_MIN + 7)		//Get lane capture
#define ITS_ROADWAY_CMD_GET_REFERENCELINE	(ITS_ROADWAY_CMD_GET_MIN + 8)		//Get lane mix
#define ITS_ROADWAY_CMD_GET_MAX				(ITS_ROADWAY_CMD_GET_MIN + 11)		

#define MAX_LOOP_COUNT						11          //Maximum number of coils in the lane
#define DAY_OR_NIGHT						2          //0---Day，1---Night

#define CAPTURE_TYPE_MASK					0x01FF				// Upgrade to the rear 8 [4/19/2012 hanyongqiang]
#define CAPTURE_TYPE_GATE					0x01				//Bayonet
#define CAPTURE_TYPE_RED_LIGHT				0x02				//Running a red light
#define CAPTURE_TYPE_REVERSE				0x04				//Retrograde
#define CAPTURE_TYPE_OVERSPEED				0x08				//Speeding

//Smart camera extension
#define	MAX_ITS_REFLINE_NUM		25
#define MAX_ITS_DETECTMODE		5


#ifdef		PAP_SDK
#define	MAX_SCENE_NUM			16
#else

#ifdef		NVSSDK_NVR
#define	MAX_SCENE_NUM			1
#else
#define	MAX_SCENE_NUM			32
#endif

#endif

#define DEV_TYPE_MIN				0
#define DEV_TYPE_IPC			(DEV_TYPE_MIN + 0)	    //IPC
#define DEV_TYPE_NVR			(DEV_TYPE_MIN + 1)		//NVR
#define DEV_TYPE_MAX			(DEV_TYPE_MIN + 2)	

typedef struct TITSRoadwayInfo 
{
	int iRoadwayID;
	int  iEnable;
	char cChannelName[32];
	char cChannelDir[32];
}TITSRoadwayInfo;

#define MAX_ROADWAY_CHANNEL_NAME 50
typedef struct TITSRoadwayInfoEx
{
	int iRoadwayID;
	int  iEnable;
	char cChannelName[MAX_ROADWAY_CHANNEL_NAME+1];
	char cChannelDir[LEN_32];
}TITSRoadwayInfoEx;

//Set lane information
#define ITS_ROADWAY_NORMAL				0		//Common lane
#define ITS_ROADWAY_NON_MOTOR			1		//Non motorized vehicle lane
#define ITS_ROADWAY_BUS_WAY				2		//Bus lane
#define ITS_ROADWAY_SMALL_CAR			3		//Small car lane
#define ITS_ROADWAY_EMERGENCY			4		//Emergency vehicle lane
#define ITS_ROADWAY_SINGAL				5		//Single lane
#define ITS_ROADWAY_FORBID_LORRY		6		//Ban truck lane
#define ITS_ROADWAY_MAX					7		//Lane max

//wd @20130531 Set lane information extention
typedef struct __tagTITSRoadwayInfo_V1
{
	int  iBufSize;					//Customized according to the length of the desired structure 
	int  iRoadwayID;				//Lane number Maximum support 4 lanes:0-3
	int  iEnable;					//Lane enable marking  1- Enable；0-Not enable
	char cChannelName[MAX_ROADWAY_CHANNEL_NAME+1];			//Lane name  No more than 50 characters in length
	char cChannelDir[LEN_32];		//Lane direction  No more than 31 characters in length
	int	 iChannelDownUp;			//Lane up and down  0-Up，1-Down
	//add by wd 20130619 Extended field as follows
	int  iUses;						//Lane use 0-Common lane，1-Non motorized vehicle lane，2-Bus lane,3-Small car lane
	int	 iSceneID;					// Scene ID(0~15) 20140203 extend
	NVS_FILE_TIME  stStartTime;	
	NVS_FILE_TIME  stStopTime;		//Time RangeS
	NVS_FILE_TIME  stStartTime2;	
	NVS_FILE_TIME  stStopTime2;
	NVS_FILE_TIME  stStartTime3;	
	NVS_FILE_TIME  stStopTime3;	
	NVS_FILE_TIME  stStartTime4;	
	NVS_FILE_TIME  stStopTime4;
	int  iBorderEnable;				//Border lane Position enable：bit0-left boundary，bit1-right boundary. Default 0
	char cNewChannelID[LEN_64];		//Roadway new channel ID, the length less than 63 bits
	int iPeopleMoveThreshold;
	int iBayonetEnable;
} TITSRoadwayInfo_V1, *PTITSRoadwayInfo_V1;
//add ended

typedef struct TITSRoadWayLoop
{
	int iRoadwayID;
	int iComportNo;
	int iSpeedLoopNo;
	int iCaptureLoopNo;
	int iSceneID;// Scene ID(0~15) 20140203Extend
	int iSecCaptureLoopNo;//Second capture coil number ,20150304 extend
	int iRadarChnNo; //radar channel no
}TITSRoadWayLoop;

typedef struct TITSRoadwayTime
{
	int iRoadwayID;
	int iLoopMaxTime;
	int iLoopMinTime;
	int iLoopDelayTime;
	int iSceneID;// Scene ID(0~15) 20140203 extend
}TITSRoadwayTime;

typedef struct TITSRoadwaySpeed
{
	int iRoadwayID;
	int iLoopDistance;
	int iSpeedLimit;
	int iSpeedModify;
	int iSceneID;// Scene ID(0~15) 20140203 extend
}TITSRoadwaySpeed;

typedef struct TITSRoadwayReco
{
	int iRoadwayID;
	int iRoadwayRange;
	int iSceneID;// Scene ID(0~15) 20140203 extend
	int iChnNo;
}TITSRoadwayReco;

typedef struct TITSRoadwayVLoop
{
	int iRoadwayID;
	int iLoopID;
	int iDayNight;
	int iPoint1[DAY_OR_NIGHT][MAX_LOOP_COUNT];
	int iPoint2[DAY_OR_NIGHT][MAX_LOOP_COUNT];
	int iPoint3[DAY_OR_NIGHT][MAX_LOOP_COUNT];
	int iPoint4[DAY_OR_NIGHT][MAX_LOOP_COUNT];
	int iSceneID;// Scene ID(0~15) 20140203 extend
	int iSensitivity;//sensibility(0~100) defult50 20141219
	int iChnNo;
}TITSRoadwayVLoop;

typedef struct TITSRoadwayLight			//Signal light
{
	int iRoadwayID;
	int iComNo;				//The number of corresponding to the signal light serial ,range 0-2（0、1 is RS232 serial，2 is RS485 serial)
	int iFloodID;			//Number of lights, by bit					
	char cLightID[32];	//Other signal lights,format4,0,1,2,3……(The first signs of a total of several lights,back for the 4 road red light detector,range0-3.)					
}TITSRoadwayLight;

//Intelligent camera extended
typedef struct __tagTITSRoadwayLight_V1
{
	TITSRoadwayLight m_stLight;
	int				 m_iCompelRedlight;		//Forced red light 
	int iSceneID;					// scene ID(0~15) 20140203extend
	int iFlashLampType;				//Flash control mode  0：associated flash; 1:Flash wheel;
}TITSRoadwayLight_V1, *PTITSRoadwayLight_V1;

//Lane capture type
#define ITS_ROAD_CAP_BAYONET_CAPTURE0	0		//Bayonet
#define ITS_ROAD_CAP_RED_LIGHT			1		//Running a red light 
#define ITS_ROAD_CAP_RETROGRADE			2		//Retrograte 
#define ITS_ROAD_CAP_OVER_SPEED			3		//Speeding
#define ITS_ROAD_CAP_FORBID_LEFT		4		//Ban left capture 
#define ITS_ROAD_CAP_FORBID_RIGHT		5		//Ban right capture 
#define ITS_ROAD_CAP_PRESS_YELLOW		6		//Vehicles for violation of traffic line capture
#define ITS_ROAD_CAP_NON_MOTOR			7		//Non-Motor Vehicle capture
#define ITS_ROAD_CAP_NOT_GUIDE			8		//Not in accordance with the provisions of the lane (Not in the direction of travel)
#define ITS_ROAD_CAP_FORBID_STRAIGHT	9		//No straight line    //add by wd @20130619 extend
#define ITS_ROAD_CAP_BUS_ROAD			10		//Bus line
#define ITS_ROAD_CAP_PRESS_ROAD_LINE	11		//violation of traffic line capture
#define ITS_ROAD_CAP_WAIT_TURN_REDLIGHT	12		//To turn the area to run a red light
#define ITS_ROAD_CAP_ILLEGAL_PARK		13		//Illegal parking  //add end
#define ITS_ROAD_CAP_CAP_MIX_SPEED		14		//Mixed trigger video capture function can make speeding
#define ITS_ROAD_CAP_REVERS				15		//Reversing
#define ITS_ROAD_CAP_TURN_AROUND		16		//Turn around
#define ITS_ROAD_CAP_BREAK_RIDE_LINE	17		//violation of traffic line
#define ITS_ROAD_CAP_BREAK_FORBID_LINE	18		//Break the forbidden line
#define ITS_ROAD_CAP_MAX				19		//Max

typedef struct TITSRoadwayCapture			//Capture position
{
	int iRoadwayID;                         //Lane number   Maximum support 4 lanes:0-3
	int iType;								//Lane capture type
	//bit0--Bayonet
	//bit1--Running a red light 
	//bit2--Retrograde 
	//bit3--Speeding
	//bit4--Ban left capture  
	//bit5--Ban right capture 
	//bit6--Vehicles for violation of traffic line capture
	//bit7--Non-Motor Vehicle capture
	//bit8--Not in accordance with the provisions of the lane (Not in the direction of travel)
	//bit9--NO straight line    //add by wd @20130619 extend
	//bit10--Bus line
	//bit11--violation of traffic line 
	//bit12--To turn the area to run a red light
	//bit13--Illegal parking  //add end
	//bit14--Mixed trigger video capture function can make speeding
	//bit15--Reversing
	//bit16--Turn around
	//bit17--violation of traffic line
	//bit18--Break the forbidden line
	char cPlace[CAPTURE_PLACE_COUNT][64];	//The vehicle through the coil capture position
	//0——Don't capture
	//1——Enter the measurement coil capture
	//2——Leave speed coil capture
	//3——To capture the coil capture
	//4——Leave the coil capture
	//5——Delay capture
	//According to type; as:1,2,3,4
	int iChannelTpye;                       //category
	//0--Left turn lane
	//1--Right turn lane
	//2--Straight lane
	//3--Turn left straight lane 
	//4--Turn right straight lane
	//5--Non-Motor Vehicle lane ---Reserve                 //modify by wd @20130619 
	//6--Turn left turn lane
	//7--Left turn lanes are added to transfer line
	//8--Straight+turn left + turn right lane
	//9--Pedestrian crossing
	int iRecognizePlace;                   //Identification strategy
	//0--Identify the first
	//1--Identification second
	//2--Recognition of the first recognition of the first second,getting the high reliability of the results
	//3--First identify the first, if not identify the results, and then identify the first
	//4--First identify the first, if not identify the results, and then identify the second
	//5—Identify the third                       
	int iSceneID;							// Scene ID(0~15) 20140203 extend
	int iRedLightCapType;					//0-According to the direction of capture（defaut）1-According to lane capture 2-According to the direction +lane
	int iTrailCapPlace;						//Trail Capture Place
}TITSRoadwayCapture;

typedef struct ITS_RecoParamEx					//Hdvision camera identify algorithm expansion function   
{
	int iMaxPlateNO;					//Maximum number of license plates
	int iMaxPlateWidth;					//Maximum license plate width
	int iMinPlateWidth;					//Minimum license plate width
	int iRoadwayID;						//Lane number
	int iEnable;						//Enable identification	0：disable idetification；1：Enable identificatin;3：Enable identification and face recognition				
	int iEngineNum;						//Recognition engine number temporarily not supported the 0 fill	
	int iAreaType;						//Recognition area type
	int iPlateType;						//Identify vehicle types
	int iSpeed;							//Vehicle speed. Note:Vehicle speed refers to the moving speed of the vehicle in the video, rather than the actual speed of the vehicle  Angle1-10					
	int iMaxSkip;						//THe maximum block number
	int iMinValid;						//Minimum license plate confidence
	int iFramePercent;					//Recognition ratio
	int iMinCapDiff;					//Capture threshold update
	char cCaliInfo[76];					//License plate calibration  20120229 extend
	//20121030 extend
	int	iPlateCharType;					//license plate recognition of each character type
	//Current license plate is 7，Character type for each license plate after using the 14 bit, Each two bit represents a character type
	//00-GB2312 characters 01-letter 10-number  11-letter or number
	int iConfid;						//Confidence threshold of vehicle license plate
}ITS_RecoParamEx;

typedef struct tagTITSRecoParam
{
	int iSize;
	ITS_RecoParamEx tRecoPara;
	int iCarNumType;
	int iAngle;	
	char cSymbol[LEN_64];
	int  iPlateCharConfid;
	int iSceneId;
}TITSRecoParam, *pTITSRecoParam;

//ITS line parameters---Intelligent camera v3.2
typedef struct __tagTITS_RefLine
{
	int			m_iLineID;
	int			m_iEnable;
	int			m_iLineType;
	vca_TLine	m_stLine1;
	vca_TLine	m_stLine2;				//reserve
	int			m_iDistance;
}TITS_RefLine, *PTITS_RefLine;

typedef struct __tagTITS_ReflineAarry
{
	int			m_iRoadwayID;
	int			m_iLineNum;
	TITS_RefLine m_stLines[MAX_ITS_REFLINE_NUM];
	int iSceneID;// Scene ID(0~15) 20140203 extend
}TITS_RefLineAarry, *PTITS_ReflineArray;


#define MAX_VEHICLE_TYPE 3//Maximum number of vehicle types
typedef struct __tagTITS_ChnCarSpeedPara
{
	int iCarWayNo;						//Lane number：Maximum support 4 lane，0-3
	int iCarType;						//Vehicle type：0-Not detect，1-car，2-truck
	int iSpeedHighLimit;				//Lane speed limit: m/s（When parsing divide 1000，convert to 3 decimal places)
	int iSpeedLowLimit;					//Lane speed lower limit: m/s（When parsing divide 1000，convert to 3 decimal places)
	int iHighExceedPercent;				//Percentage of high speed law enforcement:0-100
	int iLowExceedPercent;				//Percentage of low speed law enforcement:0-100
	int iSceneID;						// Scene ID(0~15) 20140203 extend
	float fAbnormalHighSpeed;			//Abnormal speed   Speed limit parameter: unit:m/s(3 decimal float)
	float fAbnormalLowSpeed;			//Abnormal low speed Speed limit parameter: unit:m/s(3 decimal float)
}TITS_ChnCarSpeedPara, *PTITS_ChnCarSpeedPara;


typedef struct TITSRoadwayParam
{
	int iRoadWayID;

	//modified by wd @20130531 
	//TITSRoadwayInfoEx info;
	TITSRoadwayInfo_V1 RoadWayInfo;
	//modified end

	TITSRoadWayLoop loop;
	TITSRoadwayTime time;
	TITSRoadwaySpeed speed;
	TITSRoadwayReco reco;
	TITSRoadwayVLoop vloop;
	TITSRoadwayCapture capture;
	TITSRoadwayCapture captureNew;
	ITS_RecoParamEx recopara;
	TITSRecoParam   recopara_ex;
	TITSRoadwayLight_V1	light;
	TITS_RefLineAarry   m_stLineArray;	
}TITSRoadwayParam;

//ShangHai BK  |zyb add 20150304
typedef struct tagTITSRoadwayDelayDistance
{
	int				iSize;
	int				iSceneId;	//Scene ID(0~15)
	int				iLaneId;	//Lane number, Maximum support 4 lane，0-3
	int				iSignalDelayDistance;	//To capture the signal delay capture distance(cm)or time(ms)
	int				iDelayCapDistanceS;		//Second capture delay distance(cm)or time(ms)
	int				iDelayCapDistanceT;		//Third capture delay distance(cm)or time(ms)
	int				iDelayCapDistanceFourth;//Forth capture delay distance(cm)or time(ms)
	int				iDelayCapDistanceFifth;	//Fifth capture delay distance(cm)or time(ms)
	int				iDelayCapTypeFirst;		//First capture delay type 1：time
	int				iDelayCapTypeSec;		//Second capture delay type 1：time
	int				iDelayCapTypeTrd;		//Third capture delay type 1：time
	int				iDelayCapTypeFourth;	//Forth capture delay type 1：time
	int				iDelayCapTypeFifth;		//Fifth capture delay type 1：time
}TITSRoadwayDelayDistance, *pTITSRoadwayDelayDistance;
//add end

/************************************************************************/
//*	Intelligent vehicle license plate optimization
/************************************************************************/

#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN                   0
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_FIRST_HZ				(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 0)	    //Set up the license plate optimization of the first GB2312 characters
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_NOPLATE_ENABLE		(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 1)		//Set whether to allow no license plate
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_OTHER					(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 2)		//Set to retrieve the license plate and the license plate is not credible
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MAX					(ITS_LICENSEPLATE_OPTIMIZE_CMD_SET_MIN + 3)		//

#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN                   0										   		
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_FIRST_CHARACTER       (ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 0)		//Get up the license plate optimization of the first GB2312 characters
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_NOPLATE_ENABLE		(ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 1)		//Get whether to allow no license plate
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_OTHER					(ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 2)		//Get to retrieve the license plate and the license plate is not credible
#define ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MAX					(ITS_LICENSEPLATE_OPTIMIZE_CMD_GET_MIN + 3)		//

struct TITSLicensePlateOptimizeFirstHZInfo
{
	char cModifyChar[9];
	int iMinCharConfid;
	char cModifyAlpha[10];
	int iMinAlphaConfig;
};

#define MAX_PALTE_MODIFY_CHAR 8
#define MAX_PALTE_MODIFY_CHAR_LEN 3
typedef struct TITSLicesePlateOptimizeOther    //Retrieve the license plate and the license plate is not credible
{
	int iType;                         //Type 0：To retrieve GB2312 characters ;1：Non trusted GB2312 characters
	int iCount;                        //Number of first GB2312 characters   Maximum of 8 characters
	char cModifyChar[MAX_PALTE_MODIFY_CHAR][MAX_PALTE_MODIFY_CHAR_LEN];            //First GB2312 characters     Should be set up in the adjacent province of the license plate of the first GB2312 characters or can not appear in the first GB2312 characters. No more than 4 characters
}TITSLicesePlateOptimizeOther;

#define MAX_MODIFY_CHAR_LEN 9
#define MAX_MODIFY_ALPHA_LEN 10
typedef struct _TITSLicensePlateOptimizeFirstHZInfo_V1
{
	char cModifyChar[MAX_MODIFY_CHAR_LEN];
	int iMinCharConfid;
	char cModifyAlpha[MAX_MODIFY_ALPHA_LEN];
	int iMinAlphaConfig;
	int iSize;
	int iSceneId;
	int iChnNo;
}TITSLicensePlateOptimizeFirstHZInfo_V1, *PTITSLicensePlateOptimizeFirstHZInfo_V1;

#define MAX_OPTIMIZEOTHERTYPE_NUM		 2		//0：To retrieve GB2312 characters ；1：Non trusted GB2312 characters
#define MAX_OPTIMIZEOTHERTYPE_SCENE		16		//(0~15) Max scene number
typedef struct TITSLicensePlateOptimizeParam
{
	int iEnableNoPlate;
	TITSLicensePlateOptimizeFirstHZInfo_V1  firstHZinfo[MAX_OPTIMIZEOTHERTYPE_SCENE];
	//modify by wd @20130626
	TITSLicesePlateOptimizeOther other[MAX_OPTIMIZEOTHERTYPE_NUM];
}TITSLicensePlateOptimizeParam;

/************************************************************************/
///*  Set to retrieve the license plate and the license plate is not credible//Non local license plate 
/************************************************************************/
#define  MAX_MODIFYCHAR_COUNT    8
#define  MAX_OTHER_MODIFY_CHAR_LEN 5
struct ITS_LicenseOptimizeOther
{
	int			iType;      							//0：To retrieve GB2312 characters ；1：Non trusted GB2312 characters
	int			iCount;									//Number of first GB2312 characters   Maximum of 8 characters
	char		cModifyChar[MAX_MODIFYCHAR_COUNT][MAX_OTHER_MODIFY_CHAR_LEN];	//Should be set up in the adjacent province of the license plate of the first GB2312 characters or can not appear in the first GB2312 characters. No more than 4 characters
	int			iOtherCharWeight[MAX_MODIFYCHAR_COUNT];	//The lowest confidence level of the first GB2312 characters
} ;

/************************************************************************/
///*  Traffic violation    
/************************************************************************/
#define ITS_EXTRAINFO_CMD_SET_MIN								 0
//Illegal stop project
#define ITS_ILLEGALPARK_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 0)	    //Set up illegal parking parameters
#define ITS_ILLEGALPARKPARM_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 1)		//Setting the parameters of the calibration area of illegal parking
#define ITS_LICENSEOPTIMIZEOTHER_CMD_SET						(ITS_EXTRAINFO_CMD_SET_MIN + 2)		//Set to retrieve the license plate and the license plate is not credible
#define ITS_OPTIM_CMD_SET										(ITS_EXTRAINFO_CMD_SET_MIN + 3)		//Set up the license plate optimization of the first GB2312 characters
#define ITS_RECOPARAM_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 4)		//Set identification algorithm parameters
#define ITS_ROADWAYENABLE_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 5)		//Set lane
#define ITS_CAMLOCATION_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 6)		//Set device information
#define ITS_EXACTCONFIRM_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 7)		//Set precise location information
//Smart camera
#define ITS_VIDEODECTECT_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 8)		//Image detection technology
#define ITS_DETECTAREA_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 10)	//Trajectory analysis and detection of regional parameters
#define ITS_RECOTYPE_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 11)	//Algorithm type parameters
//Traffic integrated machine
#define ITS_REDLIGHTDETECTAREA_CMD_SET							(ITS_EXTRAINFO_CMD_SET_MIN + 12)	//Set the red light to detect the regional parameters
#define ITS_ENABLE_CMD_SET										(ITS_EXTRAINFO_CMD_SET_MIN + 13)	//Set traffic related function enable protocol
#define ITS_DAYNIGHT_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 14)	//Set traffic related function enable protocol
#define ITS_WORKMODE_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 15)	//Set working mode
#define ITS_LIGHTINFO_CMD_SET                                   (ITS_EXTRAINFO_CMD_SET_MIN + 16)    //Set the parameters of the signal lamp
#define ITS_CROSSINFO_CMD_SET                                   (ITS_EXTRAINFO_CMD_SET_MIN + 17)	//Set intersection information parameters
#define ITS_AREAINFO_CMD_SET                                    (ITS_EXTRAINFO_CMD_SET_MIN + 18)    //Set gain area parameter
#define ITS_ILLEGALPARKINFO_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 19)	//Illegal parking camera
#define ITS_SECURITYCODE_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 20)	//Set security code number
#define ITS_LIGHTSUPPLY_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 21)	//Brightness compensation of vehicle license plate
#define ITS_CAPTURECOUNT_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 22)	//Capture a number
#define ITS_LINEPRESSPERMILLAGE_CMD_SET							(ITS_EXTRAINFO_CMD_SET_MIN + 23)	//Set the lane parameters of vehicle contour permillage
#define ITS_ITSWORDOVERLAY_CMD_SET								(ITS_EXTRAINFO_CMD_SET_MIN + 24)	//Traffic specific character overlay
#define ITS_VIDEODECTECT_NEW_CMD_SET							(ITS_EXTRAINFO_CMD_SET_MIN + 25)	//New image detection parameters
#define ITS_RADERINFO_CMD_SET									(ITS_EXTRAINFO_CMD_SET_MIN + 26)	//Set roadway rader information
#define ITS_PICCUT_CMD_SET										(ITS_EXTRAINFO_CMD_SET_MIN + 27)	//Picture Cut Information
#define ITS_EXTRAINFO_CMD_SET_MAX								(ITS_EXTRAINFO_CMD_SET_MIN + 28)	//Max (Has reached the maximum is no longer expanded)

#define ITS_EXTRAINFO_CMD_GET_MIN								 0
//Illegal stop project
#define ITS_ILLEGALPARK_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 0)	    //Get up illegal parking parameters
#define ITS_ILLEGALPARKPARM_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 1)		//Getting the parameters of the calibration area of illegal parking
#define ITS_LICENSEOPTIMIZEOTHER_CMD_GET						(ITS_EXTRAINFO_CMD_GET_MIN + 2)		//Get to retrieve the license plate and the license plate is not credible
#define ITS_OPTIM_CMD_GET										(ITS_EXTRAINFO_CMD_GET_MIN + 3)		//Get up the license plate optimization of the first GB2312 characters
#define ITS_RECOPARAM_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 4)		//Get identification algorithm parameters
#define ITS_ROADWAYENABLE_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 5)		//Get lane
#define ITS_CAMLOCATION_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 6)		//Get device information
//Smart camera
#define ITS_VIDEODECTECT_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 7)		//Image detection technology
#define ITS_DETECTAREA_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 9)		//Trajectory analysis and detection of regional parameters
#define ITS_RECOTYPE_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 10)	//Algorithm type parameters
#define ITS_CAMRATYPE_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 11)	//Camera type
//Traffic integrated machine
#define ITS_REDLIGHTDETECTAREA_CMD_GET							(ITS_EXTRAINFO_CMD_SET_MIN + 12)	//Get the red light to detect the regional parameters
#define ITS_ENABLE_CMD_GET										(ITS_EXTRAINFO_CMD_SET_MIN + 13)	//Get traffic related function enable protocol
#define ITS_VERSIONPRO_CMD_GET									(ITS_EXTRAINFO_CMD_SET_MIN + 14)	//Get camera version information
#define ITS_DAYNIGHT_CMD_GET									(ITS_EXTRAINFO_CMD_SET_MIN + 15)	//Get traffic related function enable protocol
#define ITS_WORKMODE_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 16)    //Get working mode
#define ITS_LIGHTINFO_CMD_GET                                   (ITS_EXTRAINFO_CMD_GET_MIN + 17)    //Get the parameters of the signal lamp
#define ITS_CROSSINFO_CMD_GET	                                (ITS_EXTRAINFO_CMD_GET_MIN + 18)	//Get intersection information parameters
#define ITS_AREAINFO_CMD_GET	                                (ITS_EXTRAINFO_CMD_GET_MIN + 19)    //Get gain area parameter
#define ITS_ILLEGALPARKINFO_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 20)	//Illegal parking camera
#define ITS_SECURITYCODE_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 21)	//Get security code number
#define ITS_LIGHTSUPPLY_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 22)	//Brightness compensation of vehicle license plate
#define ITS_CAPTURECOUNT_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 23)	//Capture a number
#define ITS_LINEPRESSPERMILLAGE_CMD_GET							(ITS_EXTRAINFO_CMD_GET_MIN + 24)	//Get the lane parameters of vehicle contour permillage
#define ITS_ITSWORDOVERLAY_CMD_GET								(ITS_EXTRAINFO_CMD_GET_MIN + 25)	//Traffic specific character overlay
#define ITS_VIDEODECTECT_NEW_CMD_GET							(ITS_EXTRAINFO_CMD_GET_MIN + 26)	//New image detection parameters
#define ITS_RADERINFO_CMD_GET									(ITS_EXTRAINFO_CMD_GET_MIN + 27)	//Get roadway rader information
#define ITS_PICCUT_CMD_GET										(ITS_EXTRAINFO_CMD_GET_MIN + 28)	//Picture Cut Information
#define ITS_EXTRAINFO_CMD_GET_MAX								(ITS_EXTRAINFO_CMD_GET_MIN + 29)	//Max (Has reached the maximum is no longer expanded)

#define ITS_EXTRAINFO_CMD_MIN									0
#define ITS_EXTRAINFO_COMMON_CMD_MIN							(ITS_EXTRAINFO_CMD_GET_MIN + 29)

//ITS 6M zyb add 14.12.05
#define ITS_EXTRAINFO_CMD_TRAFFIC_FLOW							(ITS_EXTRAINFO_COMMON_CMD_MIN + 0)  //ITS Lane Traffic Statistics
#define ITS_EXTRAINFO_CMD_TRAFFICFLOWREPORT						(ITS_EXTRAINFO_COMMON_CMD_MIN + 1)
#define ITS_EXTRAINFO_CMD_ILLEGAL								(ITS_EXTRAINFO_COMMON_CMD_MIN + 2) //ITS Illegal Type
#define ITS_EXTRAINFO_CMD_PICMERGEOVERLAY						(ITS_EXTRAINFO_COMMON_CMD_MIN + 3) //ITS Picture Merge Over Lay
#define ITS_EXTRAINFO_CMD_RECOPARAM								(ITS_EXTRAINFO_COMMON_CMD_MIN + 4)
#define ITS_EXTRAINFO_CMD_FTP_UPLOAD							(ITS_EXTRAINFO_COMMON_CMD_MIN + 5)
#define ITS_EXTRAINFO_CMD_CARLOGO_OPTIM							(ITS_EXTRAINFO_COMMON_CMD_MIN + 6)
#define ITS_EXTRAINFO_CMD_LOOP_MODE								(ITS_EXTRAINFO_COMMON_CMD_MIN + 7)
#define ITS_EXTRAINFO_CMD_IPDCAPMODEL							(ITS_EXTRAINFO_COMMON_CMD_MIN + 8)//ipd capture model
#define ITS_EXTRAINFO_CMD_IPDCARPOSITION						(ITS_EXTRAINFO_COMMON_CMD_MIN + 9)//ipd car position
#define ITS_EXTRAINFO_CMD_LINKPANORAMACAP						(ITS_EXTRAINFO_COMMON_CMD_MIN + 10)//ipd car position
#define ITS_EXTRAINFO_CMD_LOOPTRIGSTATE							(ITS_EXTRAINFO_COMMON_CMD_MIN + 11)//loop state
#define ITS_EXTRAINFO_CMD_PICREVCLIENT							(ITS_EXTRAINFO_COMMON_CMD_MIN + 12)//pic revc client
#define ITS_EXTRAINFO_CMD_FILTERPLATE							(ITS_EXTRAINFO_COMMON_CMD_MIN + 13)//filter
#define ITS_EXTRAINFO_CMD_OSDTIMEFORMAT							(ITS_EXTRAINFO_COMMON_CMD_MIN + 14)//osd formate
#define ITS_EXTRAINFO_CMD_DETECTMODE						 	(ITS_EXTRAINFO_COMMON_CMD_MIN + 15)
#define ITS_EXTRAINFO_CMD_CHNLCAPSET						 	(ITS_EXTRAINFO_COMMON_CMD_MIN + 16)
#define ITS_EXTRAINFO_CMD_CHNLCARCAPTPYE						(ITS_EXTRAINFO_COMMON_CMD_MIN + 17)
#define ITS_EXTRAINFO_CMD_DELPICSTRATEGY						(ITS_EXTRAINFO_COMMON_CMD_MIN + 18)		//delete strategy
#define ITS_EXTRAINFO_CMD_HOSTNUMBER							(ITS_EXTRAINFO_COMMON_CMD_MIN + 19)		//host number
#define ITS_EXTRAINFO_CMD_DEVICENUMBER							(ITS_EXTRAINFO_COMMON_CMD_MIN + 20)		//device number
#define ITS_EXTRAINFO_CMD_PLATCFGINFO							(ITS_EXTRAINFO_COMMON_CMD_MIN + 21)		//config platform info
#define ITS_EXTRAINFO_CMD_PICUPLOADCFG							(ITS_EXTRAINFO_COMMON_CMD_MIN + 22)		//config picture upload info
#define ITS_EXTRAINFO_CMD_GETCROSSCOUNT							(ITS_EXTRAINFO_COMMON_CMD_MIN + 23)		//ITS cross count
#define ITS_EXTRAINFO_CMD_CROSSINFO								(ITS_EXTRAINFO_COMMON_CMD_MIN + 24)		//ITS cross info
#define ITS_EXTRAINFO_CMD_GETLANECOUNT							(ITS_EXTRAINFO_COMMON_CMD_MIN + 25)		//ITS lane count
#define ITS_EXTRAINFO_CMD_LANEINFO								(ITS_EXTRAINFO_COMMON_CMD_MIN + 26)		//ITS lane info
#define ITS_EXTRAINFO_CMD_MODIFYDATA							(ITS_EXTRAINFO_COMMON_CMD_MIN + 27)		//ITS modify data
#define ITS_EXTRAINFO_CMD_DELETEDATA							(ITS_EXTRAINFO_COMMON_CMD_MIN + 28)		//ITS delete data
#define ITS_EXTRAINFO_CMD_GETDATA								(ITS_EXTRAINFO_COMMON_CMD_MIN + 29)		//ITS get data
#define ITS_EXTRAINFO_CMD_MODESETTING							(ITS_EXTRAINFO_COMMON_CMD_MIN + 30)		//ITS get data
#define ITS_EXTRAINFO_CMD_PARKWHITELIST							(ITS_EXTRAINFO_COMMON_CMD_MIN + 31)		//ITS park white License plate list
#define ITS_EXTRAINFO_CMD_PARKGUARD								(ITS_EXTRAINFO_COMMON_CMD_MIN + 32)		//ITS park guard
#define ITS_EXTRAINFO_CMD_IPDWORKMODE							(ITS_EXTRAINFO_COMMON_CMD_MIN + 33)		//
#define ITS_EXTRAINFO_CMD_LEFTCOMITYSTRAIGHTDETECTAREA			(ITS_EXTRAINFO_COMMON_CMD_MIN + 34)		//
#define ITS_EXTRAINFO_CMD_IPDTESTSNAP							(ITS_EXTRAINFO_COMMON_CMD_MIN + 35)		//
#define ITS_EXTRAINFO_CMD_DOCKPLAT_UPLOADTYPE					(ITS_EXTRAINFO_COMMON_CMD_MIN + 36)		//
#define ITS_EXTRAINFO_CMD_LIGHTSTATUS							(ITS_EXTRAINFO_COMMON_CMD_MIN + 37)	
#define ITS_EXTRAINFO_CMD_DEVOFFLINE							(ITS_EXTRAINFO_COMMON_CMD_MIN + 38)
#define ITS_EXTRAINFO_CMD_SETSDICUTAREA                         (ITS_EXTRAINFO_COMMON_CMD_MIN + 39) 
#define ITS_EXTRAINFO_CMD_SETREUPLOAD                           (ITS_EXTRAINFO_COMMON_CMD_MIN + 40)
#define ITS_EXTRAINFO_CMD_ILLEGALPARKFILTERPLATE                (ITS_EXTRAINFO_COMMON_CMD_MIN + 41)
#define ITS_EXTRAINFO_CMD_KAFKACFG                              (ITS_EXTRAINFO_COMMON_CMD_MIN + 42)
#define ITS_EXTRAINFO_CMD_TRAFFIC_VIOLATION_PARA				(ITS_EXTRAINFO_COMMON_CMD_MIN + 43)
#define ITS_EXTRAINFO_CMD_MAX									(ITS_EXTRAINFO_COMMON_CMD_MIN + 44)



#define MAX_PRESET_COUNT			16					//Maximum preset number of illegal parking
#define MAX_AREA_COUNT				16					//illegal park max point num
#define MAX_PARAM_COUNT				4					//Number of the maximum number of targets in the parking lot
#define MAX_ITS_RULE_NUM			1					//Maximum number of illegal parking rules
/************************************************************************/
///*  Set up illegal parking parameters 
/************************************************************************/
//struct ITS_IllegalPark
//{
//	int			iPresetNo;      						//Preset bit number
//	int			iAreaNo;								//Zone number
//	int		  	iIllegalParkTime;						//Illegal parking detection time
//	int			iTimeRange[MAX_TIMESEGMENT];								//Enable time range
//	POINT		arrPTArea[MAX_AREA_COUNT];				//Region four point coordinates  iX1,iY1:iX2,iY2:iX3,iY3:iX4,iY4
//} ;

typedef struct tagItsLoopTrigState
{
	int iSize;
	int iLoopID;
	int iTrigTime;
	int iTrigState;
}ItsLoopTrigState, *PItsLoopTrigState;

typedef struct tagItsPicRevcClient
{
	int iSize;
	char cIP[LENGTH_IPV4];
}ItsPicRevcClient, *PItsPicRevcClient;

#define FILTER_PALTE_TYPE_REPEAT_PLATE			0
#define FILTER_PALTE_TYPE_UNKNOW_PLATE			1
#define FILTER_PALTE_TYPE_KK_UNKNOW_PLATE		2
#define FILTER_PALTE_TYPE_ILLEGAL_UNKNOW_PLATE	3
#define MAX_FILTER_PALTE_TYPE 4

typedef struct tagItsFilterPlate
{
	int iSize;
	int iType;
	int iEnable;
	int iTimeInterval;
	int iEnableIllegal;
}ItsFilterPlate, *PItsFilterPlate;

typedef struct tagItsOsdTimeFormat
{
	int iSize;
	char cYearText[LEN_8];
	char cMonText[LEN_8];
	char cDayText[LEN_8];
	char cHourText[LEN_8];
	char cMinText[LEN_8];
	char cSecText[LEN_8];
	char cMilsecText1[LEN_8];
	char cMilsecText2[LEN_8];
	int iCapType;
}ItsOsdTimeFormat, *PItsOsdTimeFormat;

struct ITS_IllegalPark                                  //Extended stop parameter（iCheckParkTime&iSensitivity）@120905
{
	int			iPresetNo;      						//Preset bit number   Maximum 8，0-7
	int			iAreaNo;								//Zone code     Maximum 4，0-3
	int		  	iIllegalParkTime;						//Illegal parking detection time   Second for the unit
	int			iTimeRange[MAX_TIMESEGMENT];			//Enable time range
	POINT		arrPTArea[MAX_AREA_COUNT];				//Region four point coordinates  iX1,iY1:iX2,iY2:iX3,iY3:iX4,iY4
	int         iCheckParkTime;                         //Parking judgment time
	int         iSensitivity;                           //Sensitivity level
	char		cAreaName[LEN_32];						//Area name
	int			iEnable;								//Zone enable	0 Not enable  1 Enable
	int			iRuleID;								//Rule ID:0~10，0~7 General rules, 8~10:Special rules, requires special application to be used --zyb add 20150530
	int			iValid;									//Event detection is valid --zyb add 20150530
	int			iPointCounts;
	int			iCapEnable;								//capture enable, bit0:1-temp park capture enable
	int			iParkWarningEnable;						//0:disable, 1:enable
	int			iParkWarningTime;						// [0~300]seconds
	int			iDetectionType;							//0:Illegal parking, 1:Out into business, 2:Stall point about
	int			iStallJudgeTime;						//[0~43200]seconds
};

/************************************************************************/
//  Setting the parameters of the calibration area of illegal parking 
/************************************************************************/
struct ITS_IllegalParkParm
{
	int			iPresetNo;      					    //Preset bit number
	int			iAreaNo;								//Zone number
	POINT       arrPTParam[MAX_PARAM_COUNT][2];         //Region 2 point coordinates iAX1,iAY1:iAX2,iAY2;iBX1,iBY1:iBX2,BiY2;iCX1,iCY1:iCX2,iCY2;iDX1,iDY1:iDX2,iDY2
} ;

/****************************************************************************************************/
//  Set to retrieve the license plate and the license plate is not credible//Non local license plate 
/****************************************************************************************************/
//#define  MAX_MODIFYCHAR_COUNT    8
//struct ITS_LicenseOptimizeOther
//{
//	int			iType;      							//0：GB2312 characters to be searched ;1:Non trusted GB2312 characters
//	int			iCount;									//Number of first GB2312 characters   Maximum of 8 characters
//	char		cModifyChar[MAX_MODIFYCHAR_COUNT][5];	//Should be set up in the adjacent province of the license plate of the first GB2312 characters or can not appear in the first GB2312 characters. No more than 4 characters
//	int			iOtherCharWeight[MAX_MODIFYCHAR_COUNT];	//The lowest confidence level of the first GB2312 characters
//} ;


/************************************************************************/
//*  Set precise positioning parameters
/************************************************************************/
struct ITS_ExactConfirm
{
	int			iPresetNo;      						//Preset bit number
	int			iAreaNo;								//Zone number
	int         iLocalPos1;								//Calibration block number
} ;

/************************************************************************/
//*							Intelligent camera parameters				
/************************************************************************/

#define		MAX_ITS_DETECTAREA_NUM		16
#define		MAX_ITS_AREA_POINTNUM		16
//130T Algorithm private parameter
typedef struct __tagTITS_Params130T
{
	int			m_iZoomRate;
	int			m_iVehicleWidth;
	int			m_iConfidenceValve;
	int			m_iProportion;
}TITS_Params130T, *PTITS_Params130T;

//ITS Image detection parameters
typedef	struct __tagTITS_VDetect
{
	int			m_iEngineType;
	int			m_iEnable;
	int			m_iVedioType;
	int			m_iVedioWidth;
	int			m_iVedioHeight;
	int			m_iVehicleMinSize;
	int			m_iVehicleMaxSize;
	int			m_iModelType;
	int			m_iFrameRateCount;
	TITS_Params130T	m_stParmas130T;
}TITS_VDetect, *PTITS_VDetect;

typedef	struct __tagTITS_VDetect_Ex
{
	int				m_iSize;
	TITS_VDetect	m_tVDetect;
	int				m_iVDetectMotor;	//Non motor vehicle inspection
	int iSceneID;// Scene ID(0~15) 20140203 Extend
}TITS_VDetect_Ex, *PTITS_VDetect_Ex;

//ITS Image detection area parameters
#define  MAX_ITS_DETECTAREAPOINT_NUM  15
typedef struct __tagTITS_DetectArea
{ 
	int			m_iRegionID;                      //Zone number  0~11
	int			m_iEnabled;                       //Enable mark  0：Not Enable，1：Enable mark 
	int			m_iPointCount;                    //Coordinate number 4~15
	vca_TPoint	m_arrPoint[MAX_ITS_AREA_POINTNUM];//Calibration position two point coordinates（Maximum 15 points） iAX1:iAY1,iAX2:iAY2,iAX3:iAY3,iAX4:iAY4,…
	int iSceneID;// Scene ID(0~15) 20140203 Extend
}TITS_DetectArea, *LTITS_PDetectArea;

/************************************************************************/
//*  Traffic related functions enable parameters
/************************************************************************/
#define ITS_UNIVERSAL_EABLE_TYPE_POWER_SYN					0
#define ITS_UNIVERSAL_EABLE_TYPE_MANUAL						1
#define ITS_UNIVERSAL_EABLE_TYPE_CRC						2
#define ITS_UNIVERSAL_EABLE_TYPE_VIDEO_SPEED				3
#define ITS_UNIVERSAL_EABLE_TYPE_RED_LIGHT_ENHANCE			4
#define ITS_UNIVERSAL_EABLE_TYPE_FTP						5
#define ITS_UNIVERSAL_EABLE_TYPE_COMBINE					6			
#define ITS_UNIVERSAL_EABLE_TYPE_FACE						7
#define ITS_UNIVERSAL_EABLE_TYPE_LICENSE_PLATE				8
#define ITS_UNIVERSAL_EABLE_TYPE_SAFETY_BELT				9
#define ITS_UNIVERSAL_EABLE_TYPE_CAR_TYPE					10
#define ITS_UNIVERSAL_EABLE_TYPE_COPILOT_FACE				11
#define ITS_UNIVERSAL_EABLE_TYPE_OUTPUT_PICTURE				12
#define ITS_UNIVERSAL_EABLE_TYPE_LICENSE_PLATE_OUTPUT_PICTURE	13				
#define ITS_UNIVERSAL_EABLE_TYPE_DRIVER_FACE_OUTPUT_PICTURE	14				
#define ITS_UNIVERSAL_EABLE_TYPE_COPILOT_FACE_OUTPUT_PICTURE 15		
#define ITS_UNIVERSAL_EABLE_TYPE_QUICK_CAPTURE				16
#define ITS_UNIVERSAL_EABLE_TYPE_CONTINUEATION_CAPTURE		17
#define ITS_UNIVERSAL_EABLE_TYPE_CALLING					18
#define ITS_UNIVERSAL_EABLE_TYPE_SUN_VISOR 					19
#define ITS_UNIVERSAL_EABLE_TYPE_NO_COMITY_PEDESTRIAN		20 
#define ITS_UNIVERSAL_EABLE_TYPE_LEFT_COMITY_STRAIGHT		21 
#define ITS_UNIVERSAL_EABLE_TYPE_PENDANT_DETECTION			22
#define ITS_UNIVERSAL_EABLE_TYPE_TISSUE_DETECTION			23
#define ITS_UNIVERSAL_EABLE_TYPE_ANNUAL_STANDARD_DETECTION  24
#define ITS_UNIVERSAL_EABLE_TYPE_YELLOW_CAR_DETECTION       25
#define ITS_UNIVERSAL_EABLE_TYPE_SKYLIGHT_PEOPLE_DETECTION  26
#define ITS_UNIVERSAL_EABLE_TYPE_REMOTE_LIGHT_DETECTION     27
#define ITS_UNIVERSAL_EABLE_TYPE_ABNORMAL_LICENSE_DETECTION 28
#define ITS_UNIVERSAL_EABLE_TYPE_WINDOW_PARABOLIC_DETECTION 29
#define ITS_UNIVERSAL_EABLE_TYPE_DANGEROUS_VEHICLE_DETECTION 30
#define ITS_UNIVERSAL_EABLE_TYPE_FARMCAR_DETECTION			100	//-农用车识别
#define ITS_UNIVERSAL_EABLE_TYPE_ELECTROCAR_DETECTION		101	//-电动车识别
#define ITS_UNIVERSAL_EABLE_TYPE_WIDEANGLE_DETECTION		102	//-大角度识别
#define ITS_UNIVERSAL_EABLE_TYPE_NONMOTORIZED_DETECTION		103	//-非机动车行人检测
#define ITS_UNIVERSAL_EABLE_TYPE_CAR_HEADTOTAIL_DETECTION	104	//-车头车尾检测
#define ITS_UNIVERSAL_EABLE_TYPE_MOTOR_VEHICLES_DETECTION	105	//-机动车检测
#define ITS_UNIVERSAL_EABLE_TYPE_NONMOTOR_DETECTION			106	//-非机动车检测
#define ITS_UNIVERSAL_EABLE_TYPE_HUMAN_DETECTION			107	//-行人检测
#define ITS_UNIVERSAL_EABLE_TYPE_PARKING_EVENT				108	//-停车事件
#define ITS_UNIVERSAL_EABLE_TYPE_RETROGRANDE_EVENT			109	//-逆行事件
#define ITS_UNIVERSAL_EABLE_TYPE_LANECHANGE_EVENT			110	//-变道事件
#define ITS_UNIVERSAL_EABLE_TYPE_SERPENTINE_EVENT			111	//-蛇形事件
#define ITS_UNIVERSAL_EABLE_TYPE_CONGESTIONEVENT			112	//-拥堵事件
#define ITS_UNIVERSAL_EABLE_TYPE_MAX						113

#define MAX_ITS_ENABLE_NUM	ITS_UNIVERSAL_EABLE_TYPE_MAX
#define MAX_ITS_POWERINPHASEENABLE_NUM	0
#define MAX_ITS_ITSMANUALENABLE_NUM	1
struct ITS_EnalbeParam	
{
	int iType;	    			// Function type  0--Power synchroization  1--Start manual mode  2--CRC Check function  3--Video velocity measurement function  4—Red light gain enable   5—ftp Enable
								// 6--Image synthesis function  7--Face recognition function  8--Logo recognition enable 9--Identification of safety belt 10--Vehicle type identification  11--Face recognition feature
								// 12--Picture output switch 13--Vehicle image output enable 14--Main driver face image output enable 15--Deputy driving face image output enable
	int iEnable;				// 0-Non enable；1-Enable
};

#define	MAX_ITS_REDLIGHT_DETECTAREA_NUM		16

struct ITS_RedLightDetectArea
{
	int iRegionID;	//Red light area number 0-3
	int iEnabled;	//Enable mark	0：Closing，1：Openning					
	RECT rcStandardArea;	//Calibration area location
	RECT rcReviseArea;		
};

typedef struct __tagITS_RedLightDetectAreaV1
{
	int  iBufSize;                  //According to the length of the required structure(Can customize)
	int  iRegionID;	                //Red light number 8
	int  iEnabled;	                //Enable mark	0：Closing，1.Openning	
	RECT rcStandardArea;	        //Calibration area location
	RECT rcReviseArea;		
	int  iLightType;                //Lamp panel type 0-Single lamp panel，1- Three lamp panel，2-Five lamp panel red，3-Five lamp panel green 4-Strip lamp，5-Arrow lamp，6-Circular lamp，7-Digital lamp
	int  iChannelType;              //Red light area corresponding to the lane type Lane type setting ,0-Closing，1-Openning： bit0-Straight bit1-Turn left bit2-Turn right bit3-Turn around
} ITS_RedLightDetectAreaV1, *PITS_RedLightDetectAreaV1;

//add by wd @20130603 Intersection information structure
struct ITS_CrossInfo
{
	int iBufSize;                //According to the length of the required structure(Can customize)
	char pcCrossingID[LEN_64];       //Intersection number  No more than 63 characters in length
	char pcCrossingName[LEN_64];     //Intersection name  No more than 63 characters in length
	int iSceneID;// Scene ID(0~15) 20140203 Extend
	int iUseType;// Application types (0-Closing；1-Illegal parking； 2-Illegal turn around； 3-Illegal detection) 20140203 Extend
	int 					iWayProperty;		//Road attribute 0 - reserved 1 - Expressway 2 - Urban Expressway 3 - other roads
	int 					iRunSpeed;			//Traffic speed 0-200, unit km / h
};

struct ITS_IpdTestSnap
{
	int iBufSize; 
	int	iWeekday;
	int	iTime;
	int	iWeekday1;
	int	iTime1;
};
//add end

//add by wd @20130620 Gain area parameter
#define  MAX_AREAINFOPOINT_NUM 6
#define	 MAX_ITS_AREAINFOREGIONID_NUM		16     //Zone number 
#define  MAX_ITS_AREAINFO_TYPE				2		//Zone type
struct ITS_AreaInfo
{
	int iBufSize;                 //According to the length of the required structure
	int iType;                    //Zone type  0—Red light gain
	int iRegionID;               //Zone number Zone ID，0~7
	int iEnabled;                //Enable mark  0：Closing，1：Openning
	int iCount;                  //Coordinate number 4~6
	vca_TPoint  stTPoint[MAX_AREAINFOPOINT_NUM];//Calibration position two point coordinates（Maximum 6 points）
};
//add end

/************************************************************************
*	Hdvision camera parameter identification algorithm                                                                     
************************************************************************/
struct ITS_RecoParam 
{
	int iMaxPlateNO;					//Maximum number of license plates
	int iMaxPlateWidth;					//Maximum license plate width
	int iMinPlateWidth;					//Minimum license plate width
};

/************************************************************************
*	Hdvision camera position                                                                     
************************************************************************/
typedef struct ITS_CamLocation
{
	char cDeviceVode[LEN_64];								//Device number
	char cRoadName[LEN_64];									//Intersection name
}ITS_CamLocation;

//	ITS(Intelligent Transportation)Set variable configuration
#define MAX_ITS_VERSION_LEN 1024
typedef struct ITS_VariableParam
{
	int m_iAgcBlock[MAX_ITS_TIMERANGE];              //Time segment metering area enable
	int m_iFlashLamp[MAX_ITS_TIMERANGE];             //Time flash enable
	int m_iDetectMode;								 //Camera detection mode
	int m_iLoopMode;			  				     //Coil working mode ,0：Single coil ；1 Double coil; 2:Three coil
	int m_iITSDeviceType;							 //Camera peripheral type

	//Intelligent camera
	int	m_iITSCamraType;
	//Camera version information
	char m_strITSVersionPro[MAX_ITS_VERSION_LEN];	
}ITS_VariableParam;


#define CAMERA_PARAM_JPEG_QUALITY			0
#define CAMERA_PARAM_LUT					1

struct TCameraParam
{
	int iParamType;		//Parameter type  0:Picture quality  1:LUT  Other reserved					
	int iParamValue;	//Parameters value;  IParamType=0：0-100,The larger the value, the better the quality; iParamType=1：0:Closing ;1:Opening		
};

/************************************************************************
* Get device connection information                                                                     
************************************************************************/
#define MAX_CONNECT_COUNT		128

typedef struct TChannelConnectInfo
{
	int iChannelID;					//	The range is determined according to the total channel number of the equipment（Rules are the same as in NVSSDK），Data channel from 0，Command channel ignored
	char cClientIP[LEN_16];				//	Clienr IP address
	int iChannelType;				//Channel type ，0 Command channel，1 Data channel
	int iNetMode;					//1，TCP； 2，UDP； 3，Multicast； 4，Active mode
	int iConnectState;				//Connection status，0 Add connection ；  1 TO break off connection
	char cUserName[LEN_16+1];				//Username ，Maximum 16 characters
}TChannelConnectInfo;

typedef struct TTotalConnectInfo
{
	TChannelConnectInfo	connInfo[MAX_CONNECT_COUNT];
}TTotalConnectInfo;

// Add 20160804
typedef struct tagConnectInfoEx
{
	int iSize;
	int iIndex;
	TChannelConnectInfo tConnectInfo;
	int iFlag;						
	char cServerIpv6[LENGTH_IPV6_V1]; 
} ConnectInfoEx, *pConnectInfoEx;


/************************************************************************
*	Platform service program list and control                                                                     
************************************************************************/
#define MAX_PLATFORM_COUNT			10
#define MAX_PALTFORM_NAME_LENGTH	33
#define MAX_PALTFORM_NAME_LENGTH_EX	256

#define PLATFORM_CMD_SET_MIN		0
#define PLATFORM_CMD_SET_RUN		(PLATFORM_CMD_SET_MIN+0)
#define PLATFORM_CMD_SET_RUN_EX		(PLATFORM_CMD_SET_MIN+1)
#define PLATFORM_CMD_SET_MAX		(PLATFORM_CMD_SET_MIN+2)

//#define PLATFORM_CMD_GET_MIN		(PLATFORM_CMD_SET_MAX)			//	Consider the following extension compatibility			
#define PLATFORM_CMD_GET_MIN		(1)
#define PLATFORM_CMD_GET_LIST		(PLATFORM_CMD_GET_MIN+0)
#define PLATFORM_CMD_GET_MAX		(PLATFORM_CMD_GET_MIN+1)

#define PLATFORM_STOP	0
#define PLATFORM_RUN	1
typedef struct TPlatformApp
{
	char cName[MAX_PLATFORM_COUNT][MAX_PALTFORM_NAME_LENGTH];			//	Name of the platform service program，maximum 10，The length of a single length of bytes
	int iState[MAX_PLATFORM_COUNT];//	The running state of each platform service program , 0-Stop，1-Running
	char cNameEx[MAX_PLATFORM_COUNT][MAX_PALTFORM_NAME_LENGTH_EX];
}TPlatformApp;

#define PLATFORM_RUN_LIST_LEN	1024
typedef struct TPlatformAppRun
{
	char cRunList[MAX_PLATFORM_COUNT][PLATFORM_RUN_LIST_LEN];				//app run list
}TPlatformAppRun;

//ATM Related parameters
#define ATM_CONFIG_CMD_MIN					0
#define ATM_CONFIG_CMD_ATM_INFO				(ATM_CONFIG_CMD_MIN+0)
#define ATM_CONFIG_CMD_OSD_INFO				(ATM_CONFIG_CMD_MIN+1)
#define ATM_CONFIG_CMD_PROTOCOL_COUNT		(ATM_CONFIG_CMD_MIN+2)
#define ATM_CONFIG_CMD_PROTOCOL_NAME		(ATM_CONFIG_CMD_MIN+3)
#define ATM_CONFIG_CMD_MAX					(ATM_CONFIG_CMD_MIN+4)

#define ATM_PROTOCOL_NAME_LEN 31
#define ATM_PROTOCOL_NAME_COUNT 40
#define ATM_FIELD_COUNT	5
#define ATM_FIELD_LEN	64
#define ATM_CARD_ID_LEN 32

//ATM File query related parameters
#define ATM_QUERY_CMD_MIN					0
#define ATM_QUERY_CMD_FIELD					(ATM_QUERY_CMD_MIN+0)
#define ATM_QUERY_CMD_CARD					(ATM_QUERY_CMD_MIN+1)
#define ATM_QUERY_CMD_MAX					(ATM_QUERY_CMD_MIN+2)

//Front end video query
#define CMD_NETFILE_QUERY_MIN					0
#define CMD_NETFILE_QUERY_FILE					(CMD_NETFILE_QUERY_MIN + 0)
#define CMD_NETFILE_ITS_QUERY_DATA				(CMD_NETFILE_QUERY_MIN + 1)		//ITS query data
#define CMD_NETFILE_ITS_GETTOTALCOUNT			(CMD_NETFILE_QUERY_MIN + 2)		//get ITS query total count
#define CMD_NETFILE_ITS_GETCURRENTCOUNT			(CMD_NETFILE_QUERY_MIN + 3)		//get ITS query current count
#define CMD_NETFILE_ITS_GETRESULT				(CMD_NETFILE_QUERY_MIN + 4)		//get ITS query result
#define CMD_NETFILE_ITS_QUERY_TOTALCOUNT		(CMD_NETFILE_QUERY_MIN + 5)		//ITS query total count
#define CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE	(CMD_NETFILE_QUERY_MIN + 6)
#define CMD_NETFILE_QUERY_VCA					(CMD_NETFILE_QUERY_MIN + 7)
#define CMD_NETFILE_QUERY_LOG					(CMD_NETFILE_QUERY_MIN + 8)
#define CMD_NETFILE_RELEASE_QUERY				(CMD_NETFILE_QUERY_MIN + 9)
#define CMD_NETFILE_QUERY_MAX					(CMD_NETFILE_QUERY_MIN + 10)

typedef struct ATM_INFO
{
	int		iSize;									//Size of the structure,must be initialized before used
	int		iInformWay;								//Communication  0:serial port,1:UDP
	char	cProtocolName[ATM_PROTOCOL_NAME_LEN+1];	//Protocol type Protocol manufacture
	char	cSrcIP[LENGTH_IPV4];					//ATM IP address
	int		iSrcPort;								//ATM Port
	char	cDestIP[LENGTH_IPV4];					//Device IP address  Default 0
	int		iDestPort;								//Device port
}*PATM_INFO;

typedef struct ATM_OSD_INFO
{
	int				iSize;					//Size of the structure,must be initialized before used
	int 	        iX; 	    			//Horizontal coordinate
	int             iY; 	    			//Vertical coordinate
	unsigned int	uEnable;				//Enable Channel bit acquisition
}*PATM_OSD_INFO;

typedef struct ATM_PROTOCOL_NAME
{
	int		iSize;							//Size of the structure,must be initialized before used
	int		iIndex;							//Protocol index
	char	cName[ATM_PROTOCOL_NAME_LEN+1];	//Protocol name
}*PATM_PROTOCOL_NAME;

typedef struct ATM_FIELD_QUERY
{
	int				iSize;										//Size of the structure,must be initialized before used
	int          	iType; 										//Video type 33:ATM
	int     	    iChannel; 									//Channel number   From 0 start,0xff represent all
	NVS_FILE_TIME	struStartTime; 								//Start time
	NVS_FILE_TIME	struStopTime; 								//End time
	int     	    iPageSize;									//Page size
	int         	iPageNo;									//Page number
	int             iFiletype;									//File type 0:All,1:video,2:picture
	int				iDevType;									//Device type 0:Video camera,1:Network video server,2:Web camera ,0xff: all
	char			cOtherQuery[65];							//Character overlay
	int				iTriggerType;								//Alarm type 3:Port alarm,4:Mobile alarm ,5:Video loss alarm ,0x7FFFFFFF:invalid
	int				iTrigger;									//Port(channel)number
	char			cField[ATM_FIELD_COUNT][ATM_FIELD_LEN+1];	//Domain query
}ATM_FIELD_QUERY,*PATM_FIELD_QUERY;


typedef struct ATM_CARD_QUERY
{
	int				iSize;					//Size of the structure,must be initialized before used
	int          	iType; 					//Video type 33:ATM
	int     	    iChannel; 				//Channel number  From 0 begin,0xff represent all
	NVS_FILE_TIME	struStartTime; 			//Start time
	NVS_FILE_TIME	struStopTime; 			//End time
	int     	    iPageSize;				//Page size
	int         	iPageNo;				//Page number 
	int             iFiletype;				//File type 0:All,1:Video,2:Picture
	int				iDevType;				//Device type 0:Video camera ,1:Network video server,2:Web camera,0xff:all
	char			cOtherQuery[65];		//Character overlay
	int				iTriggerType;			//Alarm type 3:Port alarm,4:Mobile alarm,5:Video loss alarm,0x7FFFFFFF:Invalid
	int				iTrigger;				//Port(Channel) number
	char			cID[ATM_CARD_ID_LEN+1];	//Card number
	int				iOperate;				//Service type 0:Query,1:Withdraw money ,2:Change password ,3:Transfer accounts ,4:Deposit ,5:No card query,6:No card deposit,20:Other
	int				iQuantity;				//Amount of money:
	int				iAbnormity;				//Abnormal situation 0:Swallow notes ,1 Swallow card
}ATM_CARD_QUERY, *PATM_CARD_QUERY;

typedef struct ATM_FILE_DATA
{
	int				iSize;					//Size of the structure,must be initialized before used
	int             iType; 	        		//Video type 33:ATM
	int 	        iChannel; 	    		//Channel number From 0 begin,0xff represent all
	char 		    cFileName[250]; 		//File name
	NVS_FILE_TIME	struStartTime; 			//Start time
	NVS_FILE_TIME	struStoptime; 			//End time
	int             iFileSize; 	    		//File size
	int				iOperate;				//Operation type 1:Withdraw money ,2:Change password,3:Transfer accounts,4:Deposit,5:No card query,6:No card query,20:Other,21:Swallow money,22:Swallow card
	NVS_FILE_TIME	struOperateTime;		//Operation time
}ATM_FILE_DATA, *PATM_FILE_DATA;

#define  QUERY_MSG_COUNT	5
#define  QUERY_MSG_LEN		68
typedef struct NETFILE_QUERY_V4
{
	int				iSize;										//Size of the structure,must be initialized before used
	int          	iType; 										//Recording type: 0xFF:All, 1-Manual, 2-timing, 3-Alarm, 33-ATM, 42-ANR feedback
	int     	    iChannel; 									//channel num must less than 160 except query all channel use (255)
	NVS_FILE_TIME	struStartTime; 								//Start time
	NVS_FILE_TIME	struStopTime; 								//End time
	int     	    iPageSize;									//Page size
	int         	iPageNo;									//Page number
	int             iFiletype;									//File type 0:all, 1:Video(only sdv), 2:picture, 3:mp4, 4:all video record(sdv+mp4)
	int				iDevType;									//Device type 0:Video camera,1:Network video server,2:Web camera ,0xff: all
	char			cOtherQuery[65];							//Character overlay
	int				iTriggerType;								//Alarm type 3:Port alarm,4:Mobile alarm ,5:Video loss alarm ,0x7FFFFFFF:invalid
	int				iTrigger;									//Port(channel)number
	int				iQueryType;									//Query type 0: Basic query 1:ATM query 2：ITS query					
	int				iQueryCondition;							//Query criteria 0：Domain query  1：According to the card number query ；2：Traffic query condition：
	char			cField[QUERY_MSG_COUNT][QUERY_MSG_LEN];		//Query message
	/*Remind：forbid to extension 
	Related interfaces are not recommended for use 
	Please check the extension function in NETFILE_QUERY_V5*/
}*PNETFILE_QUERY_V4;

typedef struct tagQueryFileChannel
{
	int iChannelNo;
	int iStreamNo;
}QueryFileChannel, *PQueryFileChannel;

#define FLAG_QUERY_ALL_CHANNEL	(0x7FFFFFFF)
typedef struct NETFILE_QUERY_V5
{
	int				iBufSize;									//Size of the structure
	int     	    iQueryChannelNo; 							//query channel no, 0x7FFFFFFF means query all channel
	int				iStreamNo;									//stream no
	int          	iType; 										//Recording type: 0xFF:All, 1-Manual, 2-timing, 3-Alarm, 33-ATM, 42-ANR feedback
	NVS_FILE_TIME	tStartTime; 								//Start time
	NVS_FILE_TIME	tStopTime; 									//End time
	int     	    iPageSize;									//Page size
	int         	iPageNo;									//Page number
	int             iFiletype;									//File type 0:all, 1:Video(only sdv), 2:picture, 3:mp4, 4:all video record(sdv+mp4)
	int				iDevType;									//Device type 0:Video camera,1:Network video server,2:Web camera ,0xff: all
	char			cOtherQuery[LEN_64 + 1];					//Character overlay
	int				iTriggerType;								//Alarm type 3:Port alarm,4:Mobile alarm ,5:Video loss alarm ,0x7FFFFFFF:invalid
	int				iTrigger;									//Port(channel)number
	int				iQueryType;									//Query type 0: Basic query 1:ATM query 2：ITS query					
	int				iQueryCondition;							//Query criteria 0：Domain query  1：According to the card number query ；2：Traffic query condition：
	char			cField[QUERY_MSG_COUNT][QUERY_MSG_LEN];		//Query message
	int				iQueryChannelCount;							//if iQueryChannelCount = 0, query single channel with iQueryChannelNo
	int				iBufferSize;								// sizeof(QueryFileChannel)
	QueryFileChannel*	ptChannelList;							//buffer len = sizeof(QueryFileChannel)*iQueryChannelCount
	char			cLaneNo[LEN_64 + 1];						//lane no
	char			cVehicleType[LEN_64 + 1];					//vehicle type
	int				iFileAttr;									//File attributes:0: nvr local storage; 1: nvr local ipc data; 2: all nvr local data;10000: ipc storage		
	int				iQueryTypeValue[LEN_6];
	int				iCurQueryCount;								//output para, synchronized blocking interface use, return current query count
	int				iTotalQueryCount;							//output para, synchronized blocking interface use, return total query count
	int				iVcaDetailType;
}NETFILE_QUERY_V5, *PNETFILE_QUERY_V5;

//Audio sampling rate correlation
#define AUDIO_SAMPLE_CMD_MIN						0			
#define AUDIO_SAMPLE_CMD_SAMPLE				(AUDIO_SAMPLE_CMD_MIN+0)	//Set or get the sampling rate of a channel
#define AUDIO_SAMPLE_CMD_SAMPLE_COUNT		(AUDIO_SAMPLE_CMD_MIN+1)	//Get a channel to support the number of samples
#define AUDIO_SAMPLE_CMD_SAMPLE_LIST		(AUDIO_SAMPLE_CMD_MIN+2)	//Get the first few samples of a channel support，corresponding structure is AUDIO_SAMPLE
#define AUDIO_SAMPLE_CMD_MAX				(AUDIO_SAMPLE_CMD_MIN+3)

#define MAX_AUDIO_SAMPLE_NUM 16

typedef struct AUDIO_SAMPLE
{
	int				iSize;					//Size of the structure,must be initialized before used
	int				iIndex;					//Sampling rate index
	int				iSample;				//Audio sampling rate
}*PAUDIO_SAMPLE;

/*-------------------------------------------------------*/
//System type parameters
#define	SYSTEMTYPE_WORKMODE		0		//	Working mode	
#define SYSTEMTYPE_DEVPROD		1		//	Equipment manufacturer

//Traffic related
//#define MAX_ROADWAY_COUNT           4          //Maximum number of lanes
//#define MAX_LOOP_COUNT              4           //Maximum number of lanes in the lane
//#define DAY_OR_NIGHT                2          //0---day，1---night
//
//#define LIGHT_COUNT							4					//Signal lamp number
//#define LIGHT_COM_COUNT						4					//Signal lamp serial number
//#define CAPTURE_PLACE_COUNT					3					//To capture the lane corresponding to the position number

//Extended signal lamp parameters
//Extended signal lamp parameters
typedef	struct __tagTITSLightInfoEx
{
	int				iBufSize;
	TITSLightInfo	m_stLightInfo;			//Signal lamp number 0-7	
	int				iChannelType;			//Lane type of signal lamp  Lane type setting by bit ,0-Non enable，1-Enable ：bit0-Straight bit1-turn left bit2-Turn right bit3-Turn around
	int				iPhase;					//Red light phase   Phase value 0~360
	int				iDetectType;			//Detection of a red light or green light  0—red light 　1—green light
	int				m_iYellowLightTime;		//Yellow light continue time,  init ms：0-100000ms----->add new
	int				m_iLightEnhanceLevel;	//Red lamp gain lebel; Gain range：1~255；0-reserve；
	int				iEnhanceUseType;
	int				iEnhanceLightType;
	int				iExposureTimeLevel;
	int				iRedSaturationLevel;
	int				iSwayRange;
	int				iLightDetectLevel;
	int				iOverExposureAdjustLevel;
	int				iHalationControlLevel;
	int				iSmoothEnable;
	int				iLightPositionRules;
}TITSLightInfoEx,LPTITSLightInfoEx;


typedef int				VIDEO_METHOD;
#define VIDEO_MODE_HAND			0x00
#define VIDEO_MODE_AUT			0x01



/************************************************************************/
/*							Serial format settings                      */
/************************************************************************/
typedef struct COMFORMAT
{
	int				iSize;				//Size of the structure,must be initialized before used
	int				iComNo;				//Serial number
	char			cDeviceName[32];	//Protocol name
	char			cComFormat[32];		//Format serial 9600,n,8,1
	int				iWorkMode;			//Working mode 1:Protocol control,2:Transparent channel,3:Industry reserve ,4:7601B Serial alarm host,5:485 kerboard
	int				iComType;			//Serial type 0:485,1:232,2:422
}COMFORMAT, *PCOMFORMAT;

/*-------------------------------------------------------*/
//Advanced ball parameters
#define	MAX_DOME_PRESET_NUM			255	//	Number of maximum preset bit number
#define	MAX_DOME_SCAN_NUM			255	//	Maximum scan number
#define	MAX_DOME_CURISE_NUM			255	//	Maximum cruise number
#define	MAX_DOME_PATTERN_NUM		255	//	Maximum mode path number
#define	MAX_DOME_ZONES_NUM			255	//	Maximum number of regional tips

#define TITLETYPE_RESERVED			0	//	Reserve
#define TITLETYPE_PRESET			1	//	Preset position
#define TITLETYPE_SCAN				2	//	Scanning
#define TITLETYPE_CRUISE			3	//	Cruise
#define TITLETYPE_PATTERN			4	//	Mode path
#define TITLETYPE_ZONES				5	//	Regional cue

#define	MAX_TITLE_LEN				31

#define	CMD_DOME_ADV_SETTITLE		0	//	Set the header of the ball machine
typedef struct __tagDOME_TITLE
{
	int		m_iStructSize;				//	Structure size
	int		m_iType;					//	Title type
	int		m_iNum;						//	Title number
	char	m_cTitle[MAX_TITLE_LEN+1];	//	Title name
}DOME_TITLE, *LPDOME_TITLE;

/*-------------------------------------------------------*/
//cb@1212068124: SET GET CMD use the same name,Make code more readable and easier to use;
#define IPC_CMD_MIN 0
#define IPC_CMD_SENSOR_VOLTAGE       (IPC_CMD_MIN + 0)
#define IPC_CMD_MAX                  (IPC_CMD_MIN + 1)

typedef struct  
{
	int		iType; 
	int		iMold;
	float   fValue;
	int     iReserved;
}HardWare_Param;

#define CHANNEL_TITLE_EXTEND_NUM 3	//Character overlay extend number
typedef struct
{
	unsigned int uiTextColor;
	unsigned char ucOSDText[LENGTH_CHANNEL_TITLE_EX+1];
}OSD_TEXT;

//Holiday planning related parameters
#define HOLIDAY_PLAN_CMD_MIN						0			
#define HOLIDAY_PLAN_CMD_SCHEDULE			(HOLIDAY_PLAN_CMD_MIN+0)	//Set or get holiday plans video templates
#define HOLIDAY_PLAN_CMD_MAX				(HOLIDAY_PLAN_CMD_MIN+1)
#define MAX_HOLIDAY_PLAN_SCHEDULE					10

typedef struct
{
	unsigned short iYear;
	unsigned short iMonth;
	unsigned short iDay;
	unsigned short iWeekOfMonth;
	unsigned short iDayOfWeek;
}HOLIDAY_TIME;

typedef struct
{
	int iPlanID;		//Planning template ID,[0,9]
	int iEnable;		//Whether enable,1:Enable 0:Non enable
	char cPlanName[33]; //Planning template name
	int iMode;			//Time format,0:By date (year month day) 1:By week(month  week) 2:By month(Month day)
	HOLIDAY_TIME tStartTime;	//Start time
	HOLIDAY_TIME tEndTime;		//End time
}HOLIDAYPLAN_SCHEDULE;

//M7.6 Return error ID		EC:ERROR CODE
//Audio and video
#define EC_AV_SWITCHOVER_CHANNEL			0x1000	//(Digital channel) Switch channel type failed
#define EC_AV_QUERY_FILE					0x1010	//（Power resume）Query not to transfer file
#define EC_AV_AUDIO_ERR						0x1020	// forbid to change audio param when talk start
#define EC_AV_DISTORTION_CORRECTION			0x1021	// can not use function (distortion correction) when frame rate greater than 25/30
#define EC_AV_AUDIO_SPLUS					0x1022	// Intelligent analysis has been opened. Please close the intelligence analysis first, and then open the S+
#define EC_AV_CORRIDOR_MODE					0x1023	// can not use function (corridor mode) when frame rate greater than 25/30
#define EC_AV_ULTRA_WIDE_DYNAMIC			0x1024	// can not use function (ultra wide dynamic) when frame rate greater than 25/30
#define EC_AV_UP_DECODE_LIMIT				0x1025  //Up to the decode limit

//Character overlay
#define EC_OSD_LOGO_FORMAT					0x2001	//(Character overlay)Transfer of logo picture format is not correct
#define EC_OSD_LOGO_DATA					0x2002	//(Character overlay)Transfer of logo picture data is not correct
#define EC_OSD_VIDEO_COVER					0x2003	// can not use function (video cover) when frame rate greater than 25/30
#define EC_OSD_AlIGNMENT_OVERSIZE           0x2004  //Alignment margin exceeds the maximum setting range

//Sreial port PTZ
#define EC_SERIAL_PORT_PTZ					0x3000
#define EC_SERIAL_PTZ_RESET_ENABLE			0x3001	//PTZ reset enable

//Abnormal
#define EC_EXCEPTION						0x4000
//Alarm
#define EC_ALARM							0x5000
//Storage
#define EC_STORAGE_LOCK_FILE				0x6000	//（Plus unlock）Lock file failed
#define EC_STORAGE_UNLOCK_FILE				0x6001	//（Plus unlock）Unlock file failed
#define EC_STORAGE_REDUNDANCY				0x6010	//（Storage strategy）No boot disk policy，Unable to start redundant video
#define EC_STORAGE_HOLIDAY_TIME				0x6020	//（Holiday planning）Holiday template have time conflict
#define EC_STORAGE_RECORD_LIMIT				0x6030	
//Network service
#define EC_NET_SERVICE						0x7000	
#define EC_NET_FTP_TEST						0x7001	
#define EC_NET_SEARCH_BUSY					0x7002	 
#define EC_NET_NTP_TEST_RESULT				0x7003	
#define EC_NET_FORBID_PORT_SET				0x7004	
#define EC_NET_SET_IP_FAILED				0x7005
#define EC_NET_LAN_SAME_GATEWAY				0x7006
#define EC_NET_LIMIT_ADJUST_BD				0x7007	//Limited access and forwarding bandwidth adjustment
#define EC_NET_SERVERID_COLLISION			0x7008
#define EC_NET_XCHGIP						0x7009	//Error reason for modifying IP
#define EC_NET_XCHGIPV6						0x700A	//Error reason for modifying IPV6
#define EC_NET_RTMP                         0x700B  //Error reason for rtmp param setting
#define EC_NET_SNMP							0x700C  //Error reason for snmp param setting

//User
#define EC_LOGON_USERNAME					0x8000	//（Login）Uername does not exist
#define EC_LOGON_PASSWORD					0x8001	//（Login）Pasword error
#define EC_LOGON_DECRYPT_PWD				0x8002	//（Login）Decrytion password is not correct
#define EC_BLACK_WHITE_LIST_RESTRICTED		0x8003	// (Login)Black and white list restricted
#define EC_LOGON_AUTHORITY					0x8010	//（Operation）Insufficient user rights
#define EC_LOGON_AUTHORITY_GROUP			0x8020	//（Modify user right）User group is not enough,cannot configure high permissions
//PU Set 
#define EC_PU								0x9000
//Disk management
#define EC_DISK_RECORDING					0xA000	//(Purpose) The video cannot be modified
#define EC_DISK_MEMORY						0xA001	//(Purpose)Disk group in the video disk
#define EC_DISK_TYPE						0xA002	//(Purpose)Disk type not suppoted
#define EC_DISK_NO_DISK						0xA010	//(Disk）This disk group not have a video disc
#define EC_DISK_NO_SETTING					0xA011	//（Disk）This disk group does not set any disk
#define EC_DISK_REPEAT_ADD					0xA012	//（Disk）Repeat to add the same disk to different disk groups
#define EC_DISK_NO_RECORD_CHANNEL			0xA013	//（Disk）No video channels were added in this set
#define EC_DISK_RECORD_QUOTA_BIG			0xA020	//（Quota)Video quota is too large
#define EC_DISK_IMAGE_QUOTA_BIG				0xA021	//（Quota）Picture quota is too large
#define EC_DISK_DEL_LOCK_FILE				0xA022	//（Quota）Modify quota policy, can not remove the lock file
#define EC_DISK_RAID_NAME_SAME				0xA101	// (Quota) Build raid fail, because name of raids is same
#define EC_DISK_BUILD_RAID_FAIL				0xA101	// (Quota) Build raid fail
#define EC_DISK_BUILD_BACKUP_FAIL			0xA103	// (Quota) Build backup fail
#define EC_DISK_REBUILD_RAID_FAIL			0xA104	// (Quota) Rebuild raid fail
#define EC_DISK_DEL_RAID_FAIL				0xA105	// (Quota) Delete raid fail
#define EC_DISK_BUILD_VD_FAIL				0xA111	// (Quota) Build VD fail
#define EC_DISK_REPAIR_VD_FAIL				0xA112	// (Quota) Repair VD fail
#define EC_DISK_DEL_VD_FAIL					0xA113	// (Quota) Delete VD fail
#define EC_DISK_CONNECT_IPSAN_FAIL			0xA121	// (Quota) Connect IPSAN fail
#define EC_DISK_DICONNECT_IPSAN_FAIL		0xA122	// (Quota) Disconnect IPSAN fail
//System management
#define EC_SYSTEM							0xB000	
#define EC_SYSTEM_SET_TIMEZONE_FREQUENTLY   0xB001  //Set the time zone more than 20 times In 24 hours
//Signal communication
#define EC_COM								0xC000
//Network connections
#define EC_NET_CONNECTION					0xD000	
//Intelligent analysis
#define EC_VCA								0xE000	
#define EC_VCA_UNSUPPORT_SIZE				0xE001	//Digital channel intelligent analysis capabilities currently do not support the rate 
#define EC_VCA_VDEC_VENC_FAILED				0xE002  //Digital channel enable intellgent analysis of coding channel failure
#define EC_VCA_VCPRIOR_FAILED				0xE003  //vc Channel has been turned on,Could not start the local intelligent analysis of the digital channel
#define EC_VCA_UNSUPPORT_FAILED				0xE004  //Enable local intelligence analysis is not supported
#define EC_VCA_MAX2CHN_FAILED				0xE005  //Local intelligent analysis can only open the first 2
#define EC_VCA_UNSUPPORT_H265_FAILED		0xE006  //Local intelligence analysis does not support H265	
#define EC_VCA_PREVIEW_FAILED				0xE007  //Preview performance consreaint does not support open local intelligence analysis
#define EC_VCA_OUTSIDE_VIDEO				0xE008  //Intelligent analysis in addition to Audio: an intelligent analysis of IE pages except audio when the frame rate is greater than 25/30.
#define EC_VCA_CLIENT_TIPS					0xE009  //Client tip: S+ has been opened, please close S+ first, and then open intelligent analysis
#define EC_VCA_PLATFORM_GAUGE				0xE00A 
#define EC_VCA_UP_ALGORITHM_LIMIT			0xE00B 	//Upper bound of algorithm performance
#define EC_VCA_ONE_FIGHT_ALGORITHM			0xE00C	//Fighting Algorithms Only Open One Way
#define EC_VCA_DEV_ALG_REACH_TOP			0xE00D	//Device Algorithm Channel Reach Top
#define EC_VCA_CALIBRATEING					0xE00E  //just calibrating, don't calibrate again
#define EC_VCA_FLOW_RATE					0xE00F  //The current device has scene enabled flow rate algorithm, and it is not allowed to set flow rate algorithm in multiple scenes
#define EC_VCA_GAUGE_SCENE					0xE010  //existing scene of the current equipment enables the multi area water gauge detection algorithm, and it is not allowed to set the multi area water gauge detection algorithm in multiple scenes

//ATM
#define EC_ATM								0xF000	
//Traffic related
#define EC_ITS								0x10000	
//DZ related
#define EC_DZ								0x11000	
//Universal enable
#define EC_COMMON_ENABLE					0x12000
//Sip set
#define EC_SIP_SERVERID_COLLISION			0x13001


//M7.6 after the new equipment to enable the new ID		CI:COMMON ID
#define CI_COMMON_ID_EIS					0x1001	//Electronic anti shake
#define CI_COMMON_ID_SVC					0x1002	//svc
#define CI_COMMON_ID_VO_ENABLE				0x1003	//Video Output Control
#define CI_COMMON_ID_DENOISE				0x1004	//audio denoise	 |zyb add 20141223
#define CI_COMMON_ID_TRENDS_ROI				0x1005	//trends ROI	 |zyb add 20150304
#define CI_COMMON_ID_VIDEO_REVERSE			0x1006	//video reverse	
#define CI_COMMON_ID_DISTORTION_REVISE		0x1007	//distortion revise
#define CI_COMMON_ID_VIDEO_ENCODE			0x1009
#define CI_COMMON_ID_S_265_S_264			0x100A	//distortion revise	
#define CI_COMMON_ID_ERECT_MODE				0x100B	//Installation method	
#define CI_COMMON_ID_OBLIGATE_MODE			0x100C	//Reservation mode
#define CI_COMMON_ID_ECPOOL					0x100D	//Echo Suppression
#define CI_COMMON_ID_WALL_ANGLE				0x100E	//fish install mode wall camera incline angle
#define CI_COMMON_ID_VIDEO_LDC				0x100F	//camera calibration type (see MAX_VIDEOLDC_TYPE)
#define CI_COMMON_ID_VIDEO_SMOOTH			0x1010	//video smooth
#define CI_COMMON_ID_VIDEO_OUT_PREFER		0x1011	//video out prefer mode
#define CI_COMMON_ID_LOCAL_AUDIO_INPUT_MODE	0X1014	//Local audio input mode
#define CI_COMMON_ID_SET_MIXCOMPOSITION		0X1015	//Set the mix composition in the multi-party meeting scene
#define CI_COMMON_ID_DEV_HD_MODE			0X1016	//Whether the 4k trail device is enable in HD mode
#define CI_COMMON_ID_NVR_OUT_VOLUME			0x1017	//NVR output the volume
#define CI_COMMON_ID_FILL_MODE				0x1018	//Fill mode
#define CI_COMMON_ID_IMAGE_MODE				0x1019	//Image mode
#define CI_COMMON_ID_WATEROSD_INTERVAL		0x101A	//水利字符叠加滚动间隔，单位：秒，范围：1～86400秒
//OSD
#define CI_COMMON_ID_OSD_DOT_MATRIX			0x2001	
#define CI_COMMON_ID_OSD_VECTOR				0x2002	
#define CI_COMMON_ID_OSD_CHANNEL_ENABLE		0x2003	//OSD with channel
#define CI_COMMON_ID_NVRVIDEO_OCCLUSION		0x2004	//nvr video occlusion
#define CI_COMMON_ID_SCENE_NAME_OVERLAY		0x2005	//Scene name overlay enable switch

#define CI_COMMON_ID_NOTIFY_EXCEPTION		0x4001  //Notify the device to record a log for the user to handle the abnormality of the video

#define CI_ALARM_MOTION_DETECT				0x5000
#define CI_ALARM_VIDEO_COVER				0x5001

#define CI_HASH_CHECK						0x6002	//Hash check,0-Non enable，1-enable
#define CI_COMMON_ID_MAKE_AUDIO_FILE		0x6003	//Audio file create enable,channel  related
#define CI_COMMON_ID_BURN_AUDIO_FILE		0x6004	//Audio file burn enable
#define CI_COMMON_ID_BACK_AUDIO_FILE		0x6005	//Audio file backup copy enable
#define CI_COMMON_ID_BURNFILE_FORMAT		0x6006	//Video Record File Format
#define CI_COMMON_ID_BURN_PLAYER_OPTION		0x6007	//Burn player options
#define CI_COMMON_ID_USB_FLASH_BACKUP		0x600B	//Whether the USB flash drive backup function is enabled(ZXYHDZ)

#define CI_NET_SERVICE_SNMP					0x7000
#define CI_NET_SERVICE_SMTP					0x7001
#define CI_COMMON_ID_RTSP_CHECK				0x7002 	//RTSP checkout enable
#define CI_COMMON_ID_QQ_SERVICE				0x7003 	//QQ thing connect
#define CI_COMMON_ID_MULTICAST				0x7004	//group transmission
#define CI_COMMON_ID_UPNP_STATUS			0x7005	
#define CI_COMMON_ID_RTSP_ENCRYPT			0x7006	//rtsp encrypt
#define CI_COMMON_ID_ONVIF_SEARCH			0x7007  //onvif search
#define CI_NET_SERVICE_HTTP					0x7008	//http enable
#define CI_NET_SERVICE_HTTPS				0x7009  //https enable
#define CI_COMMON_ID_IP_ADAPTIVE			0x700A  //ip adaptive
#define CI_COMMON_ID_TARGETED_4G_CARD       0x7012  //是否是4G定向流量卡

#define CI_STORAGE_DISK_GROUP				0xA000
#define CI_STORAGE_DISK_QUOTA				0xA001
#define CI_COMMON_ID_RAID					0xA002	//RAID function enable state
#define CI_COMMON_ID_THIRD_PARTITION		0xA003	//Enable third_party partition
#define CI_COMMON_ID_DISK_DORMANCY			0xA004	//Whether the disk is enable in dormancy mode

#define CI_COMMON_ID_FAN_CONTROL			0xB000	//Fan long distance control
#define CI_COMMON_ID_FAN_TEMP_CONTROL		0xB001	//Fan temperature,actual value：Temperature value -100 to 100（After the software is reduced by 1000 and divided bu 10, accuracy 0.1）
//As:-50.5 Centigrade( -50.5×10 + 1000 = 495），Network send  495
#define CI_COMMON_ID_WHTITELIGHT_ENABLE		0xB002	//white light is enable
#define CI_COMMON_ID_ABF					0xB003	//ABF,0-reserve，1-open，1-close
#define CI_COMMON_ID_KS_FACE_DETECT			0xB004 //Ks face detect ,0-reserve 1-open 2-close
#define CI_COMMON_ID_NVR_CLOUD_AUTODETECT	0xB005	//0- hold, 1- open, 2- close
#define CI_COMMON_ID_IPC_CLOUD_AUTODETECT	0xB006	//0- hold, 1- open, 2- close
#define CI_COMMON_ID_TEMDETECT	            0xB007	//0- hold, 1- open, 2- close
#define CI_COMMON_ID_ONVIF_SLICE_TIMING     0xB008  //time correction enable
#define CI_COMMON_ID_SELF_CHECK             0xB009  //0-reserve，1-Complete self-check, 2-hardware self-check, 3-software self-check
#define CI_COMMON_ID_SOLAR_CALENDAR         0xB00A  //0- reserve, 1- open, 2- close
#define CI_COMMON_ID_REPORT_PUNCTUALLY      0xB00B  //0- reserve, 1- open, 2- close
#define CI_COMMON_ID_SELF_INIT_VOICE        0xB00C  //0- reserve, 1- open, 2- close
#define CI_COMMON_ID_PREVIEW_MODE			0xB011	//Preview mode of the device, 0-最佳预览质量，1-最大预览性能，2-自动选择
#define CI_COMMON_ID_SET_SCREEN_OUTPUTMODE	0xB012	//Set screen output mode, 1：HDMI，2：DVI，3：自适应，其他保留
#define CI_COMMON_ID_WATERBRUSH_MANUAL		0xB013	//水质清洁刷手动控制：0-停止，1-启动
#define CI_COMMON_ID_WATERQUALITY_DETECT_MANUAL		                    0xB014	//水质手动检测开启关闭	0：关闭； 1：开启
#define CI_COMMON_ID_WATERQUALITY_HYPERSPECTRAL_LAMP_BRITGHTNESS		0xB015	//水质高光谱灯手动开启及亮度值	亮度数值 0~100，0为关闭
#define CI_COMMON_ID_WATERQUALITY_UV_LAMP_BRITGHTNESS		            0xB016	//水质紫外灯手动开启及亮度值	 亮度数值 0~100，0为关闭
#define CI_COMMON_ID_ADJUST_IPC_TIME									0xB018	//是否开启nvr对ipc校时功能
#define	CI_COMMON_ID_DUAL_USERS_AUTHENTICATION							0xB019	//是否开启双用户认证（ZXYHDZ）
#define	CI_COMMON_ID_LOWPOWER_ENABLE		0xB01A	//延时低功耗设置使能，0-保留，1-启用，2-不启用
#define	CI_COMMON_ID_LOWPOWER_ENTER_TIME	0xB01B	//延时低功耗时间设置，无操作后自动进入低功耗模式的秒数

#define CI_COMMON_ID_COMMUNICATION			0xC000
#define CI_COMMON_ID_REGISTRY_STATUS		0xC001	//Registry connection status

#define CI_NET_CONNECT_INFO					0xD001  //Base socket send subpackage max unit
#define CI_NET_PUBLIC_NETWORK				0xD002	//public network state

#define CI_COMMON_ID_NVR_LOCAL_VCA			0xE000	//NVR Local VCA
#define CI_COMMON_ID_ALERT_TEMPLATE			0xE002	
#define CI_COMMON_ID_VCA_PIC_STREAM			0xE004	
#define CI_COMMON_ID_LICENSE_DISPLAY_LINE	0xE005
#define CI_COMMON_ID_VCA_RESOURCE			0xE006  //vca resource
#define CI_COMMON_ID_PIC_ATTRIBUTE			0xE007	//Upload image attribute
#define CI_COMMON_ID_TARGET_DETECTION		0xE008	//Live target detection switch
#define CI_COMMON_ID_WATER_SPEED_ENABLE		0xE00C	//water speed field realtime data enable
#define CI_COMMON_ID_WATER_SPEED_DATA_SRC	0xE00D	//water speed data source
#define CI_COMMON_ID_FLOW_DATA_NUM			0xE00E	//Water number of flow data cache
#define CI_COMMON_ID_RESET_FLOW_DATA		0xE00F	//water speed data source
#define CI_COMMON_ID_RESET_WATERLEVEL_DATA	0xE010  //reset water Level data source
#define CI_COMMON_ID_ALI_PAYMENT_ALGORITHM	0xE016	//Ali payment algorithm 
#define CI_COMMON_ID_PSEUDO_TRANSPARENCY_RATIO			0xE017	//Pseudo color function transparency half ratio
#define CI_COMMON_ID_SWITCH_ANALYZE_TARGET_TRAJECTORY	0xE018	//Start / stop intelligent analysis of target trajectory
#define CI_COMMON_ID_SWITCH_PSEUDO_COLORFUNCTION		0xE019	//Start / stop pseudo color function
#define CI_COMMON_ID_ANALYZE_TARGET_TRAJECTORY_ENABLE	0xE01A	//Intelligent analysis of target trajectory enabling
#define CI_COMMON_ID_TRACK_UNIQUE_ENABLE				0xE01B	//Track unique enable
#define CI_COMMON_ID_ORIGIN_DISTANCE_RIVER				0xE01C	//Starting point distance of equipment at river section
#define CI_COMMON_ID_JUDGE_BEHAVIOR_ANALYZE_ALG_ENABLE	0xE01D	//Judge Behavior Analysis Algorithm enable（庭审）法官行为分析算法使能 0-保留，1-开启，2-关闭，3-暂停，4-恢复
#define CI_COMMON_ID_FAST_FACE_RECOGNITION				0xE01F	//快捷人脸识别使能，0-保留，1-开启，2-关闭
#define CI_COMMON_ID_ACTIVEPOLICY_ENABLEPARA_EDIT		0xE020	//主动策略使能参数可编辑与否，按位使用 0-可编辑，1-不可编辑 每个bit位表示1个策略编号
#define CI_COMMON_ID_ACTIVEPOLICY_LINKPARA_EDIT			0xE021	//主动策略联动人形白灯参数可编辑与否，按位使用 0-可编辑，1-不可编辑 每个bit位表示1个策略编号
#define CI_COMMON_ID_TEMPERATURE_MEASUREMENT_MODE       0xE022  //测温模式	0-普通模式，1-专家模式
#define CI_COMMON_ID_RESERVE_0XE023                     0xE023  //占位不实现
#define CI_COMMON_ID_RESERVE_0XE024                     0xE024  //占位不实现
#define CI_COMMON_ID_MOSAIC_OVERLAY_FACIAL_TARGET       0xE025  //人脸目标区域马赛克叠加	0:保留 1:开启, 2:关闭
#define CI_COMMON_ID_MOSAIC_OVERLAY_HUMANOID_TARGET     0xE026  //人形目标区域马赛克叠加	0:保留 1:开启, 2:关闭
#define CI_COMMON_ID_EVAPORATION_DATA_RESET             0xE027  //蒸发量数据重置	0，保留（暂不需要参数）
#define CI_COMMON_ID_WATER_SPEED_AND_FLOW_DATA_RESET    0xE028  //重置流速、瞬时流量数据

#define CI_COMMON_ID_ITS_PICSTREAM			0xF001	//connect picture stream

//ITS 6M 14.12.05
#define CI_ITS_COMMON_ID							0x10000
#define CI_COMMON_ID_ITS_SIGNAL_LIGHT				0x10001
#define CI_COMMON_ID_ITS_BUSINESS					0x10002	//ITS Business Enable
#define CI_COMMON_ID_ITS_ILLEGAL_PARK				0x10003	//ITS Illegal parking
#define CI_COMMON_ID_ITS_STJ1_MODE					0x10004	//ITS STJ1 Radar Mode
#define CI_COMMON_ID_ITS_COU_PEDESTRAINS			0x10005 
#define CI_COMMON_ID_ITS_DIR_PEDESTRAINS			0x10006
#define CI_COMMON_ID_ITS_DETECT_TIMEOUT				0x10007
#define CI_COMMON_ID_ITS_NIGHT_MONITOR				0x10008  //Night monitoring enabling 0: Close;1: open
#define CI_COMMON_ID_ITS_ILLEGAL_VIDEO				0x10009  //Illegal video enabling 0: Close;1: open
#define CI_COMMON_ID_ITS_UPLOAD_ILLEGAL_PLATFORM	0x1000A  //Upload illegal video（Transportation platform）enabling 0: Close;1: open
#define CI_COMMON_ID_ITS_TURN_ON_LICENCE_PLATE_ENHANCEMENT 0x1000B  //Turn on license plate enhancement
#define CI_COMMON_ID_ITS_TURN_ON_LICENSE_PLATE_BINARY_MAP 0x1000C  //Turn on license plate binary map
#define CI_COMMON_ID_ITS_TRANSMIT_PICSTREAM			0x1000D		//Transmit picture stream

#define CI_VEHICLE_GPS_CALIBRATION			0x12001
#define CI_VEHICLE_ALARM_THRESHOLD			0x12002
#define CI_VEHICLE_SHUTDOWN_THRESHOLD		0x12003
#define CI_COMMON_ID_ANR					0x12010	//ANR Enable
#define CI_COMMON_ID_WORKDEV_BACKUP			0x12011	//Working device hot standby enable
#define CI_COMMON_ID_FORBIDCHN				0x12012	//Forbidden channel number
#define CI_COMMON_ID_EVENT					0x12013	//Event template enable
#define CI_COMMON_ID_TELNET					0x12014	//telnet enable
#define CI_COMMON_ID_DISK					0x12015	//The elastic sealing disc and disc state
#define CI_COMMON_ID_VCACARCHECK			0x12016 //Vehicle detection enable
#define CI_COMMON_ID_NVR_ANR				0x12017	//NVR ANR Enabale
#define CI_COMMON_ID_SOUND_PICKUP			0x12018	//Pikup switch
#define CI_COMMON_ID_LIGHT_LOGON			0x12019	//Whether the device support lightweight landing
#define CI_COMMON_ID_ONVIF_PSWCHK			0x12020 //Onvif passwork check
#define CI_COMMON_ID_POE_LONG_RETICLE		0x12021 //Whether POE devices use long distance reticle
#define CI_COMMON_ID_AUTO_S_PLUS			0x12022 //s+ auto
#define CI_COMMON_ID_NEW_LOGON				0x12023	//new light logon
#define CI_COMMON_ID_DZ_ALGO_DEBUG			0x12024	//Open the algorithm debug window
#define CI_COMMON_ID_RAINFALL_ALARM         0x12025 //RainFall Alaram
#define CI_COMMON_ID_ALERTWATER_ALARM       0x12026 //AlertWater Alarm
#define CI_COMMON_ID_NVR_BROADCAST_MSG      0x12027 //Enabling NVR to broadcast IPC port alarm message in real time
#define CI_COMMON_ID_ONVIFSUPPORTH265       0x12028 //Onvif Support H265
#define CI_COMMON_ID_IPC_ONVIF_H265         0x12029 //auto connect onvif H265 for IPC
#define CI_COMMON_ID_NVR_ONVIF_H265         0x12030 //connect onvif h265 for NVR
#define CI_COMMON_ID_FTP                    0x12031 //FTP
#define CI_COMMON_ID_AUTHORITYCONTROL       0x12032 //Enable authority control
#define CI_COMMON_ID_HOTIMAGE_BACKGROUND_AUTO    0x12033 //hot image background vertify auto
#define CI_COMMON_ID_HOTIMAGE_MANUAL        0x12034 // hot image vertify manual
#define CI_COMMON_ID_MANUALCTRL_RADARPOWER  0x12035 //Manual control of external radar power supply
#define CI_COMMON_ID_LAMP_BRIGHTNESS	    0x12036 //Brightness of middle infrared lamp
#define CI_COMMON_ID_LASER_PROPERTY			0x12037 //Laser properties
#define CI_COMMON_ID_LOCK_PTZ				0x12038 //lock ptz
#define CI_COMMON_ID_OPENFDDRF              0x12039 //open Fdd Rf
#define CI_COMMON_ID_OPENTDDRF              0x1203A //open Tdd Rf
#define CI_COMMON_ID_STREAM_TYPE            0x1203B //Stream type 0:Reserve,1-PS,2-TS
#define CI_COMMON_ID_MAX_CONNECTION         0x1203D //maximum connection,1, minimum
#define CI_COMMON_ID_EXIT_WITHOUT_OPERATION            0x1203E //Exit enable for 10 minutes without operation,0, reserved 1, enabled 2, not enabled
#define CI_COMMON_ID_INFRARED_VIDEO         0x1203F //open infrared video
#define CI_COMMON_ID_PRIMARY_NIC_NUMBER     0x12040 //primary nic number
#define CI_COMMON_ID_FACEPAD_DATA_CLEAR     0x12041 //face pad data clear
#define CI_COMMON_ID_FACEPAD_STORAGE_STRATEGY 0x12042 // face pad storagy strategy
#define CI_COMMON_ID_DEVICE_REMOTE_OPEN_DOOR     0x12043 //device remote open door
#define CI_COMMON_ID_FACEPAD_VOICE_TIPS     0x12044 //face pad voice tips
#define CI_COMMON_ID_GUARDED_DEVICE_MODE    0x12045 // guarded device mode
#define CI_COMMON_ID_FACEPAD_PUBLIC_PASSWORD 0x12046 //face pad public password
#define CI_COMMON_ID_FACEPAD_USERMODE_SWITCH 0x12047 //face pad user mode switch 0:User waiting,1:login UI,2:Config UI
#define CI_COMMON_ID_FACEPAD_CHECKMODE_SET   0x12048 //face pad check mode set 0:reserved,1:face checking,2:ercode checking
#define CI_COMMON_ID_HTTP_PROTOCOL			0x12050	//http protocol	enable			
#define CI_COMMON_ID_HIGHMODIFY_SNAP		0x12056 //suport high modifition snap
#define CI_COMMON_ID_28181NETWORKTYPE		0x12059 //28181support NetWork type
#define CI_COMMON_ID_CHANGELENSSENSOR		0X12061 //change camera Lens sensor
#define CI_COMMON_ID_VCARECORD_FIXED_LENGTH 0x12062 //Intelligent analysis server alarm video fixed length setting function
#define CI_COMMON_ID_HAWK_FOCUS_ENABLE		0x12063	//Hawk focus enable state
#define CI_COMMON_ID_HAWK_FOCUS_PTZLOCK		0x12064	//Hawk focus PTZ lock enable state
#define CI_COMMON_ID_BRIGHTNESS_LASER_FILL	0x12065	//Brightness of laser fill lamp
#define CI_COMMON_ID_CENTER_POINTING_ANGLE	0x12066	//Center pointing angle of panoramic window
#define CI_COMMON_ID_MULTI_SEARCH_SWITCH	0x12067	//Multicast search switch function
#define CI_COMMON_ID_ONE_CLICK_DISARM		0x12068	//One click disarm enable
#define CI_COMMON_ID_PREVIEW_DASH_ENABLE	0x12069	//Preview dash enable
#define CI_COMMON_ID_CAMERA_PROPERTIES		0x1206A	//Camera properties
#define CI_COMMON_ID_PUSH_TRACK_INFORMATION_INTERVAL 0x1206B //Push track information interval
#define CI_COMMON_ID_HDMI_LOW_DELAY_MODE	0x1206C	//HDMI low delay mode enable
#define CI_COMMON_ID_PREVIEW_PREFERENCE		0x13002 //preview preference
#define CI_COMMON_ID_CALIBRAT_REFER_ENABLE	0x1206D	//Calibration reference system function enable status
#define CI_COMMON_ID_REMOTE_UPHOLD_PORT		0x1206E	//Remote uphold (telnet or SSH) listening port number
#define CI_COMMON_ID_LOCK_ZOOM				0x12070	//Lock zoom enable

#define CI_COMMON_ID_PERIPHERAL_MANAGE		0x14000 //Peripheral management universal enable
#define CI_COMMON_ID_RED_OUTDOOR_LIGHT		0x14001 //Red light control
#define CI_COMMON_ID_WHITE_LIGHT			0x14002 //White light control
#define CI_COMMON_ID_LAND_LOCK				0x14003 //Ground lock control
//#define CI_COMMON_ID_ITS_CAP_TYPE			0x14004 //Capture type
#define CI_COMMON_ID_HUN_TEM_UPLOAD			0x14005 //temperature humidity upload
#define CI_COMMON_ID_RED_LIGHT_CTRL_WAY		0x14006 //red light ctrl way
#define CI_COMMON_ID_POWER_UPLOAD_INTERVAL  0x14007 //power upload interval
#define CI_COMMON_ID_HORN_RELAY_PORT_OUTPUT 0x14008
#define CI_COMMON_ID_BUZZER_CONTROL			0x14009
#define CI_COMMON_ID_POLICELIGHT_CONTROL	0x1400A //Police light control
#define CI_COMMON_ID_LASER_ANGLE_CONTROL	0x1400B //Laser angle control mode
#define CI_COMMON_ID_LIMIT_LASER_ON			0x1400C //Limit laser on

#define CI_COMMON_ID_LOW_DELAY_WHEN_SET_IRR	0x17001  //水利算法设置时视频预览低延时模式
#define CI_COMMON_ID_LED_SWITCH_INTERVAL	0x17002  //水利LED屏叠加切换间隔	0：预留，取值范围1-86400，单位：秒

//The camera capture rate structure respectively 
struct JPEGSizeInfo
{
	int iBufSize;                 //According to the length of the required structure
	int iChannelNo;              //Channel number 0~n-1
	int iWidth;                   //Capture picture width ,pixel value
	int iHeight;                 //Capture picture  high ,pixel value
};
//add end

//Load related parameters
#define DOWNLOAD_FLAG_MIN							0
#define DOWNLOAD_FLAG_FIRST_REQUEST		(DOWNLOAD_FLAG_MIN+0)
#define DOWNLOAD_FLAG_OPERATE_RECORD	(DOWNLOAD_FLAG_MIN+1)
#define DOWNLOAD_FLAG_BREAK_CONTINUE	(DOWNLOAD_FLAG_MIN+2)
#define DOWNLOAD_FLAG_MAX				(DOWNLOAD_FLAG_MIN+3)

#define DOWNLOAD_CMD_MIN							0
#define DOWNLOAD_CMD_FILE				(DOWNLOAD_CMD_MIN+0)
#define DOWNLOAD_CMD_TIMESPAN			(DOWNLOAD_CMD_MIN+1)
#define DOWNLOAD_CMD_CONTROL			(DOWNLOAD_CMD_MIN+2)
#define DOWNLOAD_CMD_FILE_CONTINUE		(DOWNLOAD_CMD_MIN+3)
#define DOWNLOAD_CMD_GET_FILE_COUNT		(DOWNLOAD_CMD_MIN+4)
#define DOWNLOAD_CMD_GET_FILE_INFO		(DOWNLOAD_CMD_MIN+5)
#define DOWNLOAD_CMD_SET_FILE_INFO		(DOWNLOAD_CMD_MIN+6)
#define DOWNLOAD_CMD_CLEAR_ALLFILECHAN	(DOWNLOAD_CMD_MIN+7)
#define DOWNLOAD_CMD_TIMESPAN_CONTINUE	(DOWNLOAD_CMD_MIN+8)
#define DOWNLOAD_CMD_MAX				(DOWNLOAD_CMD_MIN+9)

#define DOWNLOAD_FILE_FLAG_MULTI		0
#define DOWNLOAD_FILE_FLAG_SINGLE		1	

#define DOWNLOAD_FILE_TYPE_SDV			0	//record SDV stream
#define DOWNLOAD_FILE_TYPE_MP4			1	//Compatible with old software, record PS stream, function as DOWNLOAD_FILE_TYPE_PS 
#define DOWNLOAD_FILE_TYPE_PS			3	//record PS stream
#define DOWNLOAD_FILE_TYPE_ZFMP4		4	//record MP4 stream
#define DOWNLOAD_FILE_TYPE_AVI			5	//record AVI stream
#define DOWNLOAD_FILE_TYPE_TS           6   //record TS stream

typedef struct
{
	int				m_iSize;				//Structure size
	char            m_cRemoteFilename[255];	//Fornt end video file name
	char			m_cLocalFilename[255];	//Local video file name
	int				m_iPosition;			//File location by percentage 0～100;When continue send after stop send,file pointer offset request 
	int				m_iSpeed;				//1, 2, 4, 8, 16, 32, Control file download speed, 0-Suspend
	int				m_iIFrame;				//Only send I frame 1,Only play I Frame;0, All play					
	int				m_iReqMode;				//Require data mode 0-Stream mode					
	int				m_iRemoteFileLen;		//If local file is not null，the parameter set to null
	int				m_iVodTransEnable;		//Enable
	int				m_iVodTransVideoSize;	//Video pixel
	int				m_iVodTransFrameRate;   //Frame rate
	int				m_iVodTransStreamRate;  //Code rate
	int				m_iSaveFileType;		//0:SDV, 1:MP4, 3:PS-MP4, 4:std-MP4, 5:AVI
	int				m_iFileAttr;			//File attributes:0: nvr local storage; 10000: ipc storage
	int				m_iCryptType;			//iCryptType = 0, no encryption; iCryptType = 1, is AES encryption
	char			m_cCryptKey[LEN_32];
	int				m_iBitRateFlag;						//请求压缩流 说明: 默认请求原始流, 只有主码流和VOD需要处理该字段（ZXYHDZ）0-请求原始流; 1-请求压缩流 
	//2. 对于VOD，首次请求命令（DOWNLOAD_CMD_FILE 和DOWNLOAD_CMD_TIMESPAN ）生效，其他命令忽略
	int             m_iPlaybackDirection;   //播放方向 0,正向播放;1,反向播放
}DOWNLOAD_FILE;

typedef struct
{
	int				m_iSize;				//Structure size
	char			m_cLocalFilename[255];	//Local video file name
	int				m_iChannelNO;			//Channel number
	NVS_FILE_TIME	m_tTimeBegin;			//Start time
	NVS_FILE_TIME	m_tTimeEnd;				//End time
	int				m_iPosition;			//Position according to time point,>100
	int				m_iSpeed;				//1, 2, 4, 8, 16, 32, Control file download speed, 0-Suspend
	int				m_iIFrame;				//Only I frames  1,I only play; 0,Full play
	int				m_iReqMode;				//Require data mode 1-Frame mode or 2-stream mode v2,support Network interruption and file integrity verification
	int				m_iVodTransEnable;		//Enable
	int				m_iVodTransVideoSize;	//Video frequency ratio
	int				m_iVodTransFrameRate;   //Frame rate
	int				m_iVodTransStreamRate;  //Code Rate
	int				m_iFileFlag;			//0:Download multiple files  1:Download into a single file
	int				m_iSaveFileType;		//0:SDV, 1:MP4, 3:PS-MP4, 4:std-MP4, 5:AVI		
	int				m_iStreamNo;
	int				m_iFileAttr;			//File attributes:0: nvr local storage; 10000: ipc storage
	int				m_iCryptType;			//iCryptType = 0, no encryption; iCryptType = 1, is AES encryption
	char			m_cCryptKey[LEN_32];
	unsigned int    m_iDivideFileBySize;    //divide file by size,zf mp4 used ,max data = 4G
	long long		m_llPosition;           //when m_iReqMode = 2,stream mode v2 use this to present
											//The size of downloaded video data when disconnected from the network
	int				m_iBitRateFlag;						//请求压缩流 说明: 默认请求原始流, 只有主码流和VOD需要处理该字段（ZXYHDZ）0-请求原始流; 1-请求压缩流 
	//2. 对于VOD，首次请求命令（DOWNLOAD_CMD_FILE 和DOWNLOAD_CMD_TIMESPAN ）生效，其他命令忽略
	int             m_iPlaybackDirection;  //播放方向 0,正向播放;1,反向播放
	int				m_iDiskGroup;			   //盘组编号：1~16
}DOWNLOAD_TIMESPAN;

typedef struct
{
	int				m_iSize;				//Structure size
	int				m_iPosition;			//0～100，Location file playback ；-1，Does not carry on the localization
	int				m_iSpeed;				//1, 2, 4, 8, 16, 32, Control file download speed, 0-Suspend
	int				m_iIFrame;				//Only I frames 1,I only play;0,Full play
	int				m_iReqMode;				//Demand data model 1,Frame mode ;0,Flow pattern
}DOWNLOAD_CONTROL;

typedef struct
{
	int				m_iBufSize;				//Structure size
	int				m_iFileIndex;			//File index number
	char			m_cFilenNme[255];		//Local video asking price
	long long		m_lFileSize;			//File size,init:byte
	NVS_FILE_TIME	m_tTimeBeg;				//Start time
	NVS_FILE_TIME	m_tTimeEnd;				//End time
}DOWNLOAD_FILEINFO;

#define  SWITCH_AUDIO_CHAN_LINEIN		1	//line in
#define  SWITCH_AUDIO_CHAN_MICIN		2	//mic in
#define  SWITCH_AUDIO_CHAN_LINEOUT		3   //line out

#define  SWITCH_AUDIO_CHAN_LEFT			1	//left sound 
#define  SWITCH_AUDIO_CHAN_RIGHT		2	//right sound 
#define  SWITCH_AUDIO_CHAN_DIMEN		3	//dimensional sound 

// Automatically debug related protocols
#define CMD_AUTOTEST_MIN                    0
#define	CMD_AUTOTEST_RESERVED 				(CMD_AUTOTEST_MIN+0)      //Reserved
#define CMD_AUTOTEST_SETMACADDR				(CMD_AUTOTEST_MIN + 1) // set the MAC address;
#define CMD_AUTOTEST_LAN					(CMD_AUTOTEST_MIN + 2) // Debug the network card
#define CMD_AUTOTEST_AUDIOIN				(CMD_AUTOTEST_MIN + 3) // debug audio input;
#define CMD_AUTOTEST_VIDEOIN				(CMD_AUTOTEST_MIN + 4) // debug video input;
#define CMD_AUTOTEST_AUDIOOUT				(CMD_AUTOTEST_MIN + 5) // debug audio output;
#define CMD_AUTOTEST_VIDEOOUT				(CMD_AUTOTEST_MIN + 6) // debug video output;
#define CMD_AUTOTEST_MICIN					(CMD_AUTOTEST_MIN + 7) // debug MIC input;
#define CMD_AUTOTEST_SPEAKER				(CMD_AUTOTEST_MIN + 8) // debug SPEAKER output;
#define CMD_AUTOTEST_VIDEOADJUST			(CMD_AUTOTEST_MIN + 9) // debug video adjustment; protocol: AUTOTEST_ADADJUST
#define CMD_AUTOTEST_USB					(CMD_AUTOTEST_MIN + 10) // debugging USB port;
#define CMD_AUTOTEST_SATA					(CMD_AUTOTEST_MIN + 11) // Debug SATA port;
#define CMD_AUTOTEST_ALARMIN				(CMD_AUTOTEST_MIN + 12) // debug alarm input port;
#define CMD_AUTOTEST_ALARMOUT				(CMD_AUTOTEST_MIN + 13) // debug alarm output port;
#define CMD_AUTOTEST_SERIAL					(CMD_AUTOTEST_MIN + 14) // debugging serial port;
#define CMD_AUTOTEST_RTC					(CMD_AUTOTEST_MIN + 15) // debugging clock chip;
#define CMD_AUTOTEST_VGADETECTIVE			(CMD_AUTOTEST_MIN + 16) // Debug VGA signal detection;
#define CMD_AUTOTEST_HDMIDETECTIVE			(CMD_AUTOTEST_MIN + 17) // Debug HDMI signal detection;
#define CMD_AUTOTEST_RESETDETECTIVE			(CMD_AUTOTEST_MIN + 18) // debugging reset signal detection; device back
#define CMD_AUTOTEST_FORMATDISK				(CMD_AUTOTEST_MIN + 19) // format the disk;
#define CMD_AUTOTEST_BACKUPSYSTEM			(CMD_AUTOTEST_MIN + 20) // backup the system;
#define CMD_AUTOTEST_ENABLEGUIDE			(CMD_AUTOTEST_MIN + 21) // Enable the boot-up wizard;

#define CMD_AUTOTEST_IRIS					(CMD_AUTOTEST_MIN + 22) // aperture correction;
#define CMD_AUTOTEST_BADPOINTS				(CMD_AUTOTEST_MIN + 23) // Bad point calibration;
#define CMD_AUTOTEST_IRCUT					(CMD_AUTOTEST_MIN + 24) // IRCUT correction;
#define CMD_AUTOTEST_SDCARD					(CMD_AUTOTEST_MIN + 25) // SD card correction;
#define CMD_AUTOTEST_VERIFYTIME				(CMD_AUTOTEST_MIN + 26) // time correction;
#define CMD_AUTOTEST_CALIBRATEVIDEOBRIGHTNESS (CMD_AUTOTEST_MIN + 27) // brightness difference correction;
#define CMD_AUTOTEST_POWER_SYNC				(CMD_AUTOTEST_MIN + 28) // power synchronization detection;
#define AUTOTEST_AGGING_RESULT				(CMD_AUTOTEST_MIN + 29) // device aging results;
#define AUTOTEST_CREATKEYFILE				(CMD_AUTOTEST_MIN + 30) // Detection of Key documents;
#define CMD_AUTOTEST_ANALOG					(CMD_AUTOTEST_MIN + 31) // Analog test;

#define AUTOTEST_FOCUSLIGHT					(CMD_AUTOTEST_MIN + 32) // focus indicator test;
#define AUTOTEST_WHITELIGHT					(CMD_AUTOTEST_MIN + 33) // white light debugging;
#define AUTOTEST_LINEIN						(CMD_AUTOTEST_MIN + 34) // Debug LineIn;
#define AUTOTEST_DC							(CMD_AUTOTEST_MIN + 35) // debug DC aperture;
#define AUTOTEST_PIRIS						(CMD_AUTOTEST_MIN + 36) // debug P-iris;
#define AUTOTEST_CAMERARESET				(CMD_AUTOTEST_MIN + 37) // debug lens reset;
#define AUTOTEST_SWITCHLAN					(CMD_AUTOTEST_MIN + 38) // switch UI language;
#define AUTOTEST_WIFI						(CMD_AUTOTEST_MIN + 39) // debug the wireless;
#define AUTOTEST_KEYPRESS					(CMD_AUTOTEST_MIN + 40) // debugging five keys;

#define AUTOTEST_VIDEOSIZE					(CMD_AUTOTEST_MIN + 41) // maximum resolution debugging;
#define AUTOTEST_IICLINE					(CMD_AUTOTEST_MIN + 42) // I2C line test;
#define AUTOTEST_INSIDELIGHT				(CMD_AUTOTEST_MIN + 43) // built-in lamp test;
#define AUTOTEST_OUTSIDELIGHT				(CMD_AUTOTEST_MIN + 44) // fill light test;
#define AUTOTEST_ZIGBEE						(CMD_AUTOTEST_MIN + 45) // Zigbee peripheral detection;
#define AUTOTEST_TEMPERATURE				(CMD_AUTOTEST_MIN + 46) // temperature;
#define AUTOTEST_CAMERALENS					(CMD_AUTOTEST_MIN + 47) // Axis correction
#define AUTOTEST_MODIFYLANGUAGE				(CMD_AUTOTEST_MIN + 48) // modify language
#define AUTOTEST_BATTEARY					(CMD_AUTOTEST_MIN + 49) // Batteary Info
#define AUTOTEST_LASER						(CMD_AUTOTEST_MIN + 50) // laser debugging
#define AUTOTEST_WIFI_ESSIDPWD				(CMD_AUTOTEST_MIN + 51) // WIFI SSID
#define AUTOTEST_POLARIZER					(CMD_AUTOTEST_MIN + 52) // Polarizer
#define AUTOTEST_RGMII						(CMD_AUTOTEST_MIN + 53) // lens PI point correction
#define CMD_AUTOTEST_SPEEDLAN				(CMD_AUTOTEST_MIN + 54) // Gigabit network
#define CMD_AUTOTEST_CAMERACORRECT			(CMD_AUTOTEST_MIN + 55) // camera pose difference query
#define CMD_AUTOTEST_ABFCHECK				(CMD_AUTOTEST_MIN + 56) // Back focus self-check query
#define CMD_AUTOTEST_ABFCORRECT				(CMD_AUTOTEST_MIN + 57) // Back focus correction
#define CMD_AUTOTEST_TEMPERATURE_REVISE		(CMD_AUTOTEST_MIN + 58) // Temperature Correction
#define CMD_AUTOTEST_ALLLANGUAGE			(CMD_AUTOTEST_MIN + 59) // modify all language
#define CMD_AUTOTEST_LOUDER					(CMD_AUTOTEST_MIN + 60) // Debug the built-in external speakers

#define AUTOTEST_CAMCALIBRATE				(CMD_AUTOTEST_MIN+61)	  //Camera Call Brate
#define AUTOTEST_AUDIOCHANNEL				(CMD_AUTOTEST_MIN+62)	  //Switch Audio Channel
#define AUTOTEST_S6IRCUT					(CMD_AUTOTEST_MIN+63)	
#define AUTOTEST_S6IRIS						(CMD_AUTOTEST_MIN+64)	
#define AUTOTEST_VIRTUALZOOM				(CMD_AUTOTEST_MIN+65)	
#define AUTOTEST_WIPERBRUSH					(CMD_AUTOTEST_MIN+66)	
#define AUTOTEST_ALERTAUDIOTYPE				(CMD_AUTOTEST_MIN+67)
#define AUTOTEST_ENCRYPTPIN					(CMD_AUTOTEST_MIN+69)

#define AUTOTEST_ROOTPASSCONTROL			(CMD_AUTOTEST_MIN+68)	   		//root 密码控制
#define AUTOTEST_ENCRYPTPIN					(CMD_AUTOTEST_MIN+69)      		//检测加密芯片是否正常
#define AUTOTEST_FOCUSPERCENT				(CMD_AUTOTEST_MIN+70)      		//百分比变倍聚焦

//
#define AUTOTEST_LASER_COMMUNICATION		(CMD_AUTOTEST_MIN+71)			//激光器通讯检测													
#define AUTOTEST_GPS_COMMUNICATION			(CMD_AUTOTEST_MIN+72)			//GPS通讯检测													
#define AUTOTEST_COMPASS_COMMUNICATION		(CMD_AUTOTEST_MIN+73)			//指南针通讯检测														
#define AUTOTEST_LASER_ALARM				(CMD_AUTOTEST_MIN+74)			//警戒激光调试														
#define AUTOTEST_FAN						(CMD_AUTOTEST_MIN+75)			//风扇调试
#define AUTOTEST_CALIBRATESCENE				(CMD_AUTOTEST_MIN+78)			//image calibrate
#define AUTOTEST_CALIBRATELIST				(CMD_AUTOTEST_MIN+79)			//calibrate list chart
#define AUTOTEST_ACTIVATION					(CMD_AUTOTEST_MIN+80)			//active activation	
#define AUTOTEST_FRONTPANEL					(CMD_AUTOTEST_MIN+81)			//front panel
#define AUTOTEST_BACKPORT					(CMD_AUTOTEST_MIN+82)			//back port
#define AUTOTEST_QUICKLOGON					(CMD_AUTOTEST_MIN+85)			
#define AUTOTEST_GYRO						(CMD_AUTOTEST_MIN+86)			
#define AUTOTEST_MAGFB						(CMD_AUTOTEST_MIN+87)			
#define CMD_AUTOTEST_END					(CMD_AUTOTEST_MIN+100)			// End of debugging
#define MAX_AUTOTEST_ID						(CMD_AUTOTEST_MIN+101)			//老消息模式下的最大值
//
#define AUTOTEST_INNER_VERSION				(CMD_AUTOTEST_MIN+101)			//根据通道获取内部芯片的内核版本
#define AUTOTEST_PC_RAW_CHECK				(CMD_AUTOTEST_MIN+123)			//PC端坏点矫正
#define AUTOTEST_SET_CALIBRATE_RESULT		(CMD_AUTOTEST_MIN+158) 			//2022年3月21日 - 多目全景球项目V2.0，设置拼接矫正结果

#define AUTOTEST_SENSOR_FLASH_OPERATE		(CMD_AUTOTEST_MIN+182)			//sensor flash读写
#define AUTOTEST_ENABLE_VIDEO_CROP			(CMD_AUTOTEST_MIN+183) 			//视频裁剪生效使能


#define	CMD_AUTOTEST_MAX					(CMD_AUTOTEST_MIN+184)
//Need CMD_ head

#define MAX_HD_TEMPLATE_NUMBER	8
#define MAX_TEST_INDEX			255
#define MAX_RES_INDEX			127
#define MIN_RES_INDEX			-127

typedef struct _tagAutotestRecvInfo
{
	int iSize;
	int iTestItem;
	int iTestRes;
	char cTestParam[LEN_65];
	int iTestParam;
	char cTestInfo[LEN_65];//今后有新的附带信息，往后扩即可
	int iChannelNo;
}AutotestRecvInfo;

#define SWITCH_SPEAKER_TO_LOUDER 0 // Speakers
#define SWITCH_SPEAKER_TO_LINE 1 // Lead
// User-defined insert data
#define STREAMDATA_CMD_MIN		                0
#define	STREAMDATA_CMD_USER_DEFINE				STREAMDATA_CMD_MIN + 1
#define STREAMDATA_CMD_MAX						STREAMDATA_CMD_MIN + 1
#define MAX_INSERT_STREAM_LEN					64

#define INSERTDATA_FLAG_MIN						0
#define INSERTDATA_MAIN_STRAM					INSERTDATA_FLAG_MIN + 1
#define INSERTDATA_SUB_STRAM					INSERTDATA_FLAG_MIN + 2
#define INSERTDATA_MAIN_SUB						INSERTDATA_FLAG_MIN + 3
#define INSERTDATA_FLAG_MAX						INSERTDATA_FLAG_MIN + 4

#define DOME_PTZ_TYPE_MIN  1
#define DOME_PTZ_TYPE_PRESET_FREEZE_UP  		(DOME_PTZ_TYPE_MIN +0)
#define DOME_PTZ_TYPE_AUTO_FLIP 	 			(DOME_PTZ_TYPE_MIN +1)
#define DOME_PTZ_TYPE_PRESET_SPEED_LEVE			(DOME_PTZ_TYPE_MIN +2)
#define DOME_PTZ_TYPE_MANUL_SEPPD_LEVEL			(DOME_PTZ_TYPE_MIN +3)
#define DOME_PTZ_TYPE_WAIT_ACT             		(DOME_PTZ_TYPE_MIN +4)
#define DOME_PTZ_TYPE_INFRARED_MODE 			(DOME_PTZ_TYPE_MIN +5)
#define DOME_PTZ_TYPE_SCALE_ZOOM				(DOME_PTZ_TYPE_MIN +6)
#define DOME_PTZ_TYPE_LINK_DISJUNCTOR 			(DOME_PTZ_TYPE_MIN +7)
#define DOME_PTZ_TYPE_ANTI_SHAKE				(DOME_PTZ_TYPE_MIN +8)
#define DOME_PTZ_TYPE_MAX			 			(DOME_PTZ_TYPE_MIN +9)
typedef struct
{
	int iSize;
int iType; // type
int iAutoEnable; // 0 - not enabled, 1 - enabled
int iParam1; // Parameters
// 1 - Enable Preset Frozen: Not used
// 2 - Enable Auto Flip: Not used
// 3 - Preset speed level: 0 - low, 1 - medium, 2 - high
// 4 - manual speed level: 0 - low, 1 - in, 2 - high
// 5 - Enable standby action: Specific values: 30,60,300,600,1800 (unit: seconds)
// 6 - infrared light mode: 0 - manual, 1 - automatic
int iParam2; // Parameters
// 1 - Enable Preset Frozen: Not used
// 2 - Enable Auto Flip: Not used
// 3 - Preset speed level: Not used
// 4 - Manual Speed ??Level: Not used
// 5 - Enable standby action: 0 - preset, 1 - scan, 2 - cruise, 3 - mode path
// 6 - infrared light mode: "0 - manual, on behalf of infrared light brightness, the specific values: [0,10];
// 1 - Automatic, on behalf of infrared Ling \ sensitivity, specific values: 0 - minimum, 1 - low, 2 - standard,
// 3 - higher, 4 - highest;
int iParam3; // Reserved
int iParam4; // Reserved
}DOMEPTZ;

// Broadcast message ID
#define MSG_VEHICLE_GPS_CALIBRATION 0x12001 // GPS calibration iMsgValue (0: disabled, 1: enabled)
#define MSG_VEHICLE_VOLTAGE 0x12002 // iMsgValue (for voltage)
#define MSG_VEHICLE_TEMPERATURE 0x12003 // iMsgValue (for temperature)
#define MSG_VEHICLE_SATELLITE_NUM 0x12004 // iMsgValue (number of satellites)
#define MSG_VEHICLE_SIGNAL_INTENSITY 0x12005 // iMsgValue (for GPS signal strength)
#define MSG_VEHICLE_GPS_MODULE_TYPE 0x12006 // GPS Module Type iMsgValue (0: No Module, 1: GPS, 2: Compass)
#define MSG_VEHICLE_ALARM_THRESHOLD 0x12007 // low-voltage alarm threshold
#define MSG_VEHICLE_SHUTDOWN_THRESHOLD 0x12008 // Low Voltage Shutdown Threshold

#define MSG_VALUE_MAX_LEN 64
typedef struct  
{
	int iMsgID; // Message ID
	int iChannelNo; // channel number
	int iMsgValue; // Message with parameters
	char cMsgValue[MSG_VALUE_MAX_LEN];		//Message with parameters
}BROADCAST_MSG,*PBROADCAST_MSG;

// Module Capability ID
#define MODULE_CAP_MIN						(0) 
#define MODULE_CAP_VEHICLE			(MODULE_CAP_MIN+0)
#define MODULE_CAP_MAX				(MODULE_CAP_MIN+1)

// Vehicle module capacity
#define MODULE_CAP_VEHICLE_GPS_OVERLAY			0x1
#define MODULE_CAP_VEHICLE_POWER_DELAY			0x2
#define MODULE_CAP_VEHICLE_ALARM_THRESHOLD		0x4
#define MODULE_CAP_VEHICLE_SHUTDOWN_THRESHOLD	0x8

// Analog 485 keyboard control instruction
#define KEYBOARD_CTRL_MIN			0
#define KEYBOARD_CURRENT_SCREEN KEYBOARD_CTRL_MIN + 0 // set the UI selected window, Value for the UI window number
#define KEYBOARD_SINGLE_SCREEN KEYBOARD_CTRL_MIN + 1 // single screen display UI window, Value reserved
#define KEYBOARD_PRESET_CALL KEYBOARD_CTRL_MIN + 2 // Resume from single screen display, Value not used
#define KEYBOARD_SCREEN_RRSTORE KEYBOARD_CTRL_MIN + 3 // Call preset with Value as preset number
#define KEYBOARD_CTRL_MAX			KEYBOARD_CTRL_MIN + 4

#define TTEMPLATE_CMD_MIN				0
#define TTEMPLATE_CMD_SMART (TTEMPLATE_CMD_MIN + 0) // set the intelligent recording template
#define TTEMPLATE_CMD_VIDEO (TTEMPLATE_CMD_MIN + 1) // set the intelligent video parameter template
#define TTEMPLATE_CMD_MAX	(TTEMPLATE_CMD_MIN+2)

#define SMART_TTEMPLATE_MAX_NUM	4
typedef struct  
{
int iWeek; // Sunday to Saturday for ~, iWeekday = 7, on behalf of the holiday plan
int iIndex [SMART_TTEMPLATE_MAX_NUM]; // template number 1-8, - not enabled, -8 for template - template
}SMART_TEMPLATE,*PSMART_TEMPLATE;

typedef struct  
{
	int iIndex; // Template number
	int iType; // parameter type
	int iValue; // parameter value
}VIDEO_TEMPLATE,*PVIDEO_TEMPLATE;

//***** VIDEO_TEMPLATE the corresponding type and value ******/
// 1 - encoding 0, H.264, M-JPEG, MPEG4
// 2 - encoding Profile 0: baseline, 1: main, 2: high
// 3 - Resolution See the Resolution page
// 4 - Frame rate 1 ~
// 5 - compression mode 0, CBR;, VBR
// 6 - code rate unit is KBytes / s, such as kbps code rate, here should be set (divided by)
// 7 - picture quality 4 ~, the smaller the higher the quality
// 8 - I frame rate 0-100
// 9 - stream type 3, audio and video in line with the flow, audio stream, video stream
// 10 - audio coding algorithm AUDIO_CODEC_FORMAT_G711A = 1, /* G.711 A rate */
// AUDIO_CODEC_FORMAT_G711Mu = 2, /*G.711 Mu Rate */
// AUDIO_CODEC_FORMAT_ADPCM = 3, /* ADPCM */
// AUDIO_CODEC_FORMAT_G726 = 4, /*G.726 */
// AUDIO_CODEC_FORMAT_AAC_LC = 22 --- 31, /* AAC */ Reserve an interval for the AAC to be extended
// AUDIO_CODEC_FORMAT_BUTT

//add by 20131022
#define DEVICE_STATE_MIN						 0
#define DEVICE_STATE_BRIGHT						 (DEVICE_STATE_MIN) // Brightness
#define DEVICE_STATE_ACUTANCE					 (DEVICE_STATE_MIN + 1) // Sharpness
#define DEVICE_STATE_SYSTEM						 (DEVICE_STATE_MIN + 2) // System
#define DEVICE_STATE_LAMP_LIGHT_BLOCKS           (DEVICE_STATE_MIN + 3)
#define DEVICE_STATE_LAMP_BRIGHT_VALUE			 (DEVICE_STATE_MIN + 4)
#define DEVICE_STATE_MAX						 (DEVICE_STATE_MIN + 4)  

#define NVS_TIME NVS_FILE_TIME
typedef struct __tagSystemState
{
	int iSize;
int iCamera; // Camera status
int iHLimit; // Horizontal limit
int iVLimit; // Vertical limit
	int iInterface;				//interface module
int iTSensor; // Temperature sensor
int temperature; // Dome temperature
int itemperatureScale; // Temperature unit
NVS_TIME strPublishData; // release date
}TSystemState, *pTSystemState;

#define NET_CLIENT_MIN										0	
#define NET_CLIENT_DOME_MENU						(NET_CLIENT_MIN + 0) // Dome menu parameters
#define NET_CLIENT_DOME_WORK_SCHEDULE				(NET_CLIENT_MIN + 1) // Dome Run Template Parameters
#define NET_CLIENT_INTERESTED_AREA					(NET_CLIENT_MIN + 2) // Region of interest
#define NET_CLIENT_APPEND_OSD						(NET_CLIENT_MIN + 3) // additional character overlay
#define NET_CLIENT_LOGONFAILED_REASON				(NET_CLIENT_MIN + 4) // Login failed
#define NET_CLIENT_AUTOREBOOT						(NET_CLIENT_MIN + 5) // NVR automatically restarts the armed time
#define NET_CLIENT_IP_FILTER						(NET_CLIENT_MIN + 6) // IP address filtering (black and white list)
#define NET_CLIENT_DATE_FORMATE						(NET_CLIENT_MIN + 7) // superimposed character date display format
#define NET_CLINET_COLOR2GRAY						(NET_CLIENT_MIN + 8) // Video color to gray immediately
#define NET_CLINET_LANPARAM							(NET_CLIENT_MIN + 9) // NIC configuration function
#define NET_CLINET_DAYLIGHT_SAVING_TIME				(NET_CLIENT_MIN + 10) // Set the device daylight saving time
#define NET_CLINET_NETOFFLINE						(NET_CLIENT_MIN + 11) // Forcibly disconnect the user
#define NET_CLINET_HTTPPORT							(NET_CLIENT_MIN + 12) // HTTP port
#define NET_CLINET_PICTURE_MERGE					(NET_CLIENT_MIN + 13) // image composition
#define NET_CLIENT_SNAP_SHOT_INFO					(NET_CLIENT_MIN + 14) // before and after the capture parameters
#define NET_CLIENT_IO_LINK_INFO						(NET_CLIENT_MIN + 15) // IO linkage device type
#define NET_CLIENT_DEV_COMMONNAME					(NET_CLIENT_MIN + 16) // custom device common interface alias
#define NET_CLIENT_ITS_DEV_COMMONINFO				(NET_CLIENT_MIN + 17) // ITS device general information
#define NET_CLIENT_ITS_COMPOUNDPARAM				(NET_CLIENT_MIN + 18) // set the trigger parameters related to mixing
#define NET_CLIENT_DEV_VOLUME_CTRL					(NET_CLIENT_MIN + 19) // audio volume control
#define NET_CLIENT_INTIMITY_COVER					(NET_CLIENT_MIN + 20) // 3D privacy mask
#define NET_CLIENT_ANYSCENE							(NET_CLIENT_MIN + 21) // Analyze the scene
#define NET_CLIENT_CALL_ANYSCENE					(NET_CLIENT_MIN + 22) // call the ball machine to the appropriate analysis of the scene
#define NET_CLIENT_SENCE_CRUISE_TYPE				(NET_CLIENT_MIN + 23) // scene application cruise enable type
#define NET_CLIENT_SENCE_CRUISE_TIMER				(NET_CLIENT_MIN + 24) // Scene Application Timed Cruise Template
#define NET_CLIENT_SENCE_CRUISE_TIMERANGE			(NET_CLIENT_MIN + 25) // Scene application time period cruise template
#define NET_CLIENT_FACE_DETECT_ARITHMETIC			(NET_CLIENT_MIN + 26) // Face Detection Algorithm
#define NET_CLIENT_PERSON_STATISTIC_ARITHMETIC		(NET_CLIENT_MIN + 27) // The counting algorithm
#define NET_CLIENT_TRACK_ARITHMETIC					(NET_CLIENT_MIN + 28) // intelligent Tracking Algorithm
#define NET_CLIENT_TRACK_ASTRICT_LOCATION			(NET_CLIENT_MIN + 29) // trace limit
#define NET_CLIENT_TRACK_ZOOMX						(NET_CLIENT_MIN + 30) // Tracking magnification factor - real-time settings
#define NET_CLIENT_TRACK_MANUAL_LOCKED				(NET_CLIENT_MIN + 31) // Manual lock tracking
#define NET_CLIENT_VCA_SUSPEND						(NET_CLIENT_MIN + 32) // Suspend intelligence Analysis
#define NET_CLIENT_COVER_ALARM_AREA					(NET_CLIENT_MIN + 33) // video masking alarm zone parameter
#define NET_CLIENT_MANUAL_SNAPSHOT					(NET_CLIENT_MIN + 34) // Manual capture
#define NET_CLIENT_FILE_APPEND_INFO					(NET_CLIENT_MIN + 35) // Recording File Additional Information
#define NET_CLIENT_CURRENT_SENCE					(NET_CLIENT_MIN + 36) // Get the current use of dome analysis of the scene
#define NET_CLIENT_BACKUP_SEEK_WORKDEV				(NET_CLIENT_MIN + 37) // Multicast Search Worker
#define NET_CLIENT_BACKUP_GET_SEEK_COUNT			(NET_CLIENT_MIN + 38) // number of multicast search work machines
#define NET_CLIENT_BACKUP_GET_SEEK_WORKDEV			(NET_CLIENT_MIN + 39) // Get the search list of a work machine information
#define NET_CLIENT_BACKUP_WORKMODE					(NET_CLIENT_MIN + 40) // working mode
#define NET_CLIENT_BACKUP_MODIFY					(NET_CLIENT_MIN + 41) // to add the work machine to delete the hot machine
#define NET_CLIENT_BACKUP_BACKUPDEV_STATE			(NET_CLIENT_MIN + 42) // access to hot machine configuration information and connection status
#define NET_CLIENT_BACKUP_GET_WORKDEV_NUM			(NET_CLIENT_MIN + 43) // Get the number of workstations associated with the hot spare machine
#define NET_CLIENT_BACKUP_WORKDEV_STATE				(NET_CLIENT_MIN + 44) // get work machine configuration information and connection status
#define NET_CLIENT_APP_SERVER_LIST					(NET_CLIENT_MIN + 45) // Get the current platform to support the device list of services
#define NET_CLIENT_RTMP_URL_INFO					(NET_CLIENT_MIN + 46) // RTMP list information
#define NET_CLIENT_FRAME_RATE_LIST					(NET_CLIENT_MIN + 47) // Get a list of supported channel frame rate
#define NET_CLIENT_RTSP_LIST_INFO					(NET_CLIENT_MIN + 48) // Set RTSP list information
#define NET_CLIENT_DISABLE_PROC_VCA					(NET_CLIENT_MIN + 49) // Disable the VCA parameter processing, to prevent the use of excessive memory
#define NET_CLIENT_ENABLE_PROC_VCA					(NET_CLIENT_MIN + 50) // Enable the handling of VCA parameters
#define NET_CLIENT_COM_DEVICE						(NET_CLIENT_MIN + 51) // serial device configuration
#define NET_CLIENT_GAIN_LOG							(NET_CLIENT_MIN + 52) // Get offline log
#define NET_CLIENT_EXPORT_CONFIG					(NET_CLIENT_MIN + 53) // Export Configuration
#define NET_CLIENT_ELECNET_METER            		(NET_CLIENT_MIN + 54)	//electronic net meters param
#define NET_CLIENT_DEVINFO_AUTO_CONFIRM				(NET_CLIENT_MIN + 55)   //Dh DevInvo confirm
#define NET_CLIENT_COLOR_TYPE						(NET_CLIENT_MIN + 56)   //color input type
#define NET_CLIENT_CHANGE_VI_MODE					(NET_CLIENT_MIN + 57)   //change video input mode
#define NET_CLIENT_WATER_STEADY_PERIOD				(NET_CLIENT_MIN + 58)   
#define NET_CLIENT_WATER_SNAPINFO    				(NET_CLIENT_MIN + 59)   
#define NET_CLIENT_VIDEOENCODE_LIST					(NET_CLIENT_MIN + 60)   //Video Encod List
#define NET_CLIENT_BARCODE							(NET_CLIENT_MIN + 61)   //device bar code
#define NET_CLIENT_RESET_PASSWORD					(NET_CLIENT_MIN + 62)
#define NET_CLIENT_PSECH_STATE						(NET_CLIENT_MIN + 63)
#define NET_CLIENT_WHITELIGHT_MODE					(NET_CLIENT_MIN + 64)	//white light mode
#define NET_CLIENT_IRIS_TYPE						(NET_CLIENT_MIN + 65)	//aperture type
#define NET_CLIENT_IDENTI_CODE						(NET_CLIENT_MIN + 67)	//identity code
#define NET_CLIENT_EMAIL_TEST						(NET_CLIENT_MIN + 68)	//email test
#define NET_CLIENT_CREATEFREEV						(NET_CLIENT_MIN + 69)	//create freev0
#define NET_CLIENT_VCAFFINAL						(NET_CLIENT_MIN + 70)	//VCAFFINAL
#define NET_CLIENT_PREVIEW_MODE						(NET_CLIENT_MIN + 71)	//MODE
#define NET_CLIENT_DISK_SMART_ENABLE				(NET_CLIENT_MIN + 72)	//disk smart check enable
#define NET_CLIENT_SMART_INFO						(NET_CLIENT_MIN + 73)	//disk smart check info
#define NET_CLIENT_AUDIO_PONTICELLO					(NET_CLIENT_MIN + 74)	//AUDIO_PONTICELLO
#define NET_CLIENT_FACEMOSAIC						(NET_CLIENT_MIN + 75)	//FACEMOSAIC
#define NET_CLIENT_MAX_LANRATE						(NET_CLIENT_MIN + 76)	//max lan rate
#define NET_CLIENT_P2P_APP_URL						(NET_CLIENT_MIN + 77)	//P2p load app url
#define NET_CLIENT_CREATEFREEVO						(NET_CLIENT_MIN + 78)
#define NET_CLIENT_DEVAMPLITUDE						(NET_CLIENT_MIN + 79)
#define NET_CLIENT_PTZLIST							(NET_CLIENT_MIN + 80)
#define NET_CLIENT_COLORPARALIST					(NET_CLIENT_MIN + 81)
#define NET_CLIENT_EXCEPTION_LINKOUTPORT			(NET_CLIENT_MIN + 82)	//Exceptyion LinkOutPort
#define NET_CLIENT_WIRELESSMODULE					(NET_CLIENT_MIN + 83)	//WifiLessModule
#define NET_CLIENT_MODIFYAUTHORITY					(NET_CLIENT_MIN + 84)	//modify authority
#define NET_CLIENT_DISK_OPERATION           		(NET_CLIENT_MIN + 85)	//disk operate
#define NET_CLIENT_SCENETIMEPOINT	        		(NET_CLIENT_MIN + 86)	//scene time port
#define NET_CLIENT_H323_LOCALPARAS          		(NET_CLIENT_MIN + 87)	//H.323 local param
#define NET_CLIENT_H323_GKPARAS						(NET_CLIENT_MIN + 88)	//H.323 GK param
#define NET_CLIENT_ENABLE_LOGON_LIGHT				(NET_CLIENT_MIN + 89)	//Enable Light Logon Mode
#define NET_CLIENT_CHN_CONNECT_STATE				(NET_CLIENT_MIN + 90)	//channel connect state
#define NET_CLIENT_GET_PROTOCOL_ENABLE				(NET_CLIENT_MIN + 91)	//device is send protocol
#define NET_CLIENT_GET_LIFEMONITOR_HBREAL			(NET_CLIENT_MIN + 92)	//Vital signs in realtime heart rate and blood oxygen
#define NET_CLIENT_GET_LIFEMONITOR_GRAMREAl			(NET_CLIENT_MIN + 93)	//Vital signs waveform data
#define NET_CLIENT_LIFEMONITOR_SET_CONFIG			(NET_CLIENT_MIN + 94)	//Set life monitor config
#define NET_CLIENT_PASSWD_ERR_TIMES					(NET_CLIENT_MIN + 95)	//Password Error times
#define NET_CLIENT_DEV_ABSTRACT						(NET_CLIENT_MIN + 96)	//dev abstract
#define NET_CLIENT_OSD_EXTRA_INFO					(NET_CLIENT_MIN + 97)	//OSD extra info
#define NET_CLIENT_SERVERINFO						(NET_CLIENT_MIN + 98)	//Server Info
#define NET_CLIENT_GET_FUNC_ABILITY					(NET_CLIENT_MIN + 99)	//get function ability
#define NET_CLIENT_NTP_INFO							(NET_CLIENT_MIN + 100)	//NTP info
#define NET_CLIENT_VODEV_LIST						(NET_CLIENT_MIN + 101)	//VO Device list
#define NET_CLIENT_STREAM_SMOOTH					(NET_CLIENT_MIN + 102)	//stream smooth
#define NET_CLIENT_LINK_HTTP_INFO					(NET_CLIENT_MIN + 103)	//link http info
#define NET_CLIENT_TIMINGCRUISE_BY_TIMERANGE		(NET_CLIENT_MIN + 104)	//timing cruise by time range
#define NET_CLIENT_SCENETEMPLATE_MAP				(NET_CLIENT_MIN + 105)	//scene template map
#define NET_CLIENT_VENCTYPE							(NET_CLIENT_MIN + 106)	//venc typ
#define NET_CLIENT_DEV_MIXAUDIO						(NET_CLIENT_MIN + 107)	//Mix Audio
#define NET_CLIENT_DEV_AUDIO_IN						(NET_CLIENT_MIN + 108)	//Audio In
#define NET_CLIENT_DEV_AUDIO_MUTE					(NET_CLIENT_MIN + 109)  //Audio Mute
#define NET_CLIENT_MODE_RULE						(NET_CLIENT_MIN + 110)  //Mode Rule
#define NET_CLIENT_ENABLE_NET_OPTIMIZE				(NET_CLIENT_MIN + 111)  //net work optimize
#define NET_CLIENT_ALARM_LINK_INTERVAL				(NET_CLIENT_MIN + 112)  //alarm link interval
#define NET_CLIENT_PRECEDE_DELAY_SNAP				(NET_CLIENT_MIN + 113)  //precede delay snap
#define NET_CLIENT_ENABLE_VIDEO_OPTIMIZE			(NET_CLIENT_MIN + 114)  //video optimize
#define NET_CLIENT_VOLUME_OUT						(NET_CLIENT_MIN + 115)	//out put volume type
#define NET_CLIENT_SEND_VIDEO_STREAM				(NET_CLIENT_MIN + 116)	
#define NET_CLIENT_CONTROL_EMAIL					(NET_CLIENT_MIN + 117)	
#define NET_CLIENT_ALARM_AUDIO_LOST_STATE			(NET_CLIENT_MIN + 118)
#define NET_CLIENT_DDNS_EX							(NET_CLIENT_MIN + 119)
#define NET_CLIENT_ITS_CAMERA_TYPE					(NET_CLIENT_MIN + 120)
#define NET_CLIENT_ROUTE_NAT						(NET_CLIENT_MIN + 121)
#define NET_CLIENT_ZOOM_CONTROL						(NET_CLIENT_MIN + 122)
#define NET_CLIENT_MANAGE_EXDEV						(NET_CLIENT_MIN + 123)	//manage exter device
#define NET_CLIENT_MANAGE_RECORDINGAUDIO			(NET_CLIENT_MIN + 124)	//manage recording audio
#define NET_CLIENT_TMP_THRESHOLD					(NET_CLIENT_MIN + 125)	//temperature threshold
#define NET_CLIENT_AUDIO_SAMPLE_FILE_COUNT			(NET_CLIENT_MIN + 126)	//audio sample count
#define NET_CLIENT_CLEAR_CONFIG						(NET_CLIENT_MIN + 127)	//clear config
#define NET_CLIENT_TEXT_PLAN						(NET_CLIENT_MIN + 128)	//test plan
#define NET_CLIENT_DEV_MODEL						(NET_CLIENT_MIN + 129)	
#define NET_CLIENT_AUDIO_DETECTION					(NET_CLIENT_MIN + 130)	
#define NET_CLIENT_AUDIO_THRESHOLD					(NET_CLIENT_MIN + 131)	
#define NET_CLIENT_ALGORITHM_TYPE					(NET_CLIENT_MIN + 132)	
#define NET_CLIENT_BACKUP_DEV_NUM					(NET_CLIENT_MIN + 133)
#define NET_CLIENT_DATA_PARAM						(NET_CLIENT_MIN + 134)
#define NET_CLIENT_QOS_VALUE						(NET_CLIENT_MIN + 135)
#define NET_CLIENT_PLATE_LIST_TYPE					(NET_CLIENT_MIN + 136)
#define NET_CLIENT_CONNECT_INFO						(NET_CLIENT_MIN + 137)
#define NET_CLIENT_LOCALSNAP_SCHEDULE				(NET_CLIENT_MIN + 138)
#define NET_CLIENT_SMART_CHECK						(NET_CLIENT_MIN + 139)			
#define NET_CLIENT_OSD_FONTSIZE						(NET_CLIENT_MIN + 140)			
#define NET_CLIENT_OSD_COLOR						(NET_CLIENT_MIN + 141)	
#define NET_CLIENT_MULTI_PIC						(NET_CLIENT_MIN + 142)	
#define NET_CLIENT_CURRENT_TEMPLATE					(NET_CLIENT_MIN + 143)	
#define NET_CLIENT_SPLUS							(NET_CLIENT_MIN + 144)
#define NET_CLIENT_SMTPINFO_EX						(NET_CLIENT_MIN + 145)	
#define NET_CLIENT_FAR_END_ENLARGE					(NET_CLIENT_MIN + 146)	
#define NET_CLIENT_LOCAL_STORE_PATH					(NET_CLIENT_MIN + 147)	
#define NET_CLIENT_VENC_SLICE_TYPE					(NET_CLIENT_MIN + 148)	
#define NET_CLIENT_PORT_MAP							(NET_CLIENT_MIN + 149)	
#define NET_CLIENT_SERVER_PORT						(NET_CLIENT_MIN + 150)	
#define NET_CLIENT_VIDEO_LDC						(NET_CLIENT_MIN + 151)		//camera calibration
#define NET_CLIENT_VODEV_OUTPUT_MODE				(NET_CLIENT_MIN + 152)
#define NET_CLIENT_PORT_NO							(NET_CLIENT_MIN + 153)
#define NET_CLIENT_READCOM_TIMEINTERVAL				(NET_CLIENT_MIN + 154)
#define NET_CLIENT_ALERT_TEMPLATE_NAME				(NET_CLIENT_MIN + 155)
#define NET_CLIENT_HU_TEM_INTERVAL					(NET_CLIENT_MIN + 156)		//humidity, temperature time interval
#define NET_CLIENT_CALIBRATE_INFO					(NET_CLIENT_MIN + 157)		
#define NET_CLIENT_GEOGRAFHY_LOCATION				(NET_CLIENT_MIN + 158)		
#define NET_CLIENT_TIME_MODE						(NET_CLIENT_MIN + 159)
#define NET_CLIENT_SATAEX_NUM						(NET_CLIENT_MIN + 160)
#define NET_CLIENT_HDTEMPLATENAME					(NET_CLIENT_MIN + 161)
#define NET_CLIENT_SPLUS_TEMPLATE_PARA				(NET_CLIENT_MIN + 162)
#define NET_CLIENT_SMTP_ENABLE_MULTI				(NET_CLIENT_MIN + 163)
#define NET_CLIENT_PRE_TRACK_MANUAL					(NET_CLIENT_MIN + 164)
#define NET_CLIENT_GPS_TIME							(NET_CLIENT_MIN + 165)		//GPS check time interval
#define NET_CLIENT_EXCEPTION_DISK_TEM				(NET_CLIENT_MIN + 166)	
#define NET_CLIENT_VCAFPGA							(NET_CLIENT_MIN + 167)
#define NET_CLIENT_ACTIVATION						(NET_CLIENT_MIN + 168)
#define NET_CLIENT_WHITELIGHT_CONTRL				(NET_CLIENT_MIN + 169)
#define NET_CLIENT_NOTHING_WITH_CHANNEL				(NET_CLIENT_MIN + 170)
#define NET_CLIENT_DZ_COMMON_EX						(NET_CLIENT_MIN + 171)
#define NET_CLIENT_PU_COMMON_INFO					(NET_CLIENT_MIN + 172)
#define NET_CLIENT_SLOT_NUM							(NET_CLIENT_MIN + 173)
#define NET_CLIENT_VIDEO_LDCEX_INFO					(NET_CLIENT_MIN + 174)			
#define NET_CLIENT_VIDEO_LDCEX_INDEX				(NET_CLIENT_MIN + 175)
#define NET_CLIENT_VIDEO_LDCEX_MODEL				(NET_CLIENT_MIN + 176)
#define NET_CLIENT_PERIPHERAL_INFO					(NET_CLIENT_MIN + 177)
#define NET_CLIENT_PERIPHERAL_LIST					(NET_CLIENT_MIN + 178)
#define NET_CLIENT_CALIBRATE_MODE					(NET_CLIENT_MIN + 179)
#define NET_CLIENT_PPPOE_STATUS						(NET_CLIENT_MIN + 180)
#define NET_CLIENT_IPCATTR							(NET_CLIENT_MIN + 181)
#define NET_CLIENT_DNS_ENABLE						(NET_CLIENT_MIN + 182)
#define NET_CLIENT_DEV_RESOLUTION					(NET_CLIENT_MIN + 183)
#define NET_CLIENT_DEV_RESOLUTION_LIST				(NET_CLIENT_MIN + 184)
#define NET_CLIENT_GET_CAMERAINFO					(NET_CLIENT_MIN + 185)
#define NET_CLIENT_NEW_VERSION						(NET_CLIENT_MIN + 186)
#define NET_CLIENT_SENCE_CRUISE_TIMERANGE_EX		(NET_CLIENT_MIN + 187)
#define NET_CLIENT_TIMINGCRUISE_BY_TIMERANGE_EX		(NET_CLIENT_MIN + 188)
#define NET_CLIENT_IPC_DEV_ATTR						(NET_CLIENT_MIN + 189)
#define NET_CLIENT_ELE_ANTI_SHAKE					(NET_CLIENT_MIN + 190)
#define NET_CLIENT_SGW_NET_BITRATE					(NET_CLIENT_MIN + 191)
#define NET_CLIENT_SGW_SIP_LOCALSERVER				(NET_CLIENT_MIN + 192)
#define NET_CLIENT_SGW_MAC_FILTER					(NET_CLIENT_MIN + 193)
#define NET_CLIENT_SGW_VPN_PARAM					(NET_CLIENT_MIN + 194)
#define NET_CLIENT_SGW_VPN_LAN						(NET_CLIENT_MIN + 195)
#define NET_CLIENT_SGW_VPN_STATE					(NET_CLIENT_MIN + 196)
#define NET_CLIENT_CHN_ONLINE_STATE					(NET_CLIENT_MIN + 197)	//channel online state
#define NET_CLIENT_DISABLE_PROCESS_FRAME			(NET_CLIENT_MIN + 198)	//disable process frame
#define NET_CLIENT_DISABLE_LOGON_BY_NEW_CLIENT		(NET_CLIENT_MIN + 199)	//set logon client type 0(old client)
#define NET_CLIENT_DETECTPARA                       (NET_CLIENT_MIN + 200)
#define NET_CLIENT_DETECTAREA                       (NET_CLIENT_MIN + 201)
#define NET_CLIENT_FULL_LOGON						(NET_CLIENT_MIN + 203)	//wait for full logon message
#define NET_CLIENT_IRRIGATIONOVER_INFO				(NET_CLIENT_MIN + 204)
#define NET_CLIENT_PERIPHERAL_PARA					(NET_CLIENT_MIN + 205)
#define NET_CLIENT_LOGNUM							(NET_CLIENT_MIN + 206)
#define NET_CLIENT_LOGONLOCK_PARA					(NET_CLIENT_MIN + 207)
#define NET_CLIENT_SSL_ENCRYPT						(NET_CLIENT_MIN + 208)
#define NET_CLIENT_SMD_ENCODE_ENABLE				(NET_CLIENT_MIN + 209)
#define NET_CLIENT_SMD_IMAGE_ENABLE					(NET_CLIENT_MIN + 210)
#define NET_CLIENT_SMD_ALARM_AREA					(NET_CLIENT_MIN + 211)
#define NET_CLIENT_VERIFICATION_CODE				(NET_CLIENT_MIN + 212)
#define NET_CLIENT_SNMP_PARA                        (NET_CLIENT_MIN + 213)
#define NET_CLIENT_SELECT_NET						(NET_CLIENT_MIN + 214)
#define NET_CLIENT_SIP_ENCRYPT                      (NET_CLIENT_MIN + 215)
#define NET_CLIENT_SIP_PUBLICKEY			        (NET_CLIENT_MIN + 216)
#define NET_CLIENT_DORMANCY_SCHEDULE                (NET_CLIENT_MIN + 217)
#define NET_CLIENT_NET_REDUCE                       (NET_CLIENT_MIN + 218)
#define NET_CLIENT_DORMANCY_STATE                   (NET_CLIENT_MIN + 219)
#define NET_CLIENT_GAUGEPARA                        (NET_CLIENT_MIN + 220) 
#define NET_CLIENT_UPLOAD_STATION                   (NET_CLIENT_MIN + 221)
#define NET_CLIENT_ASYN_CHNONLINESTATE				(NET_CLIENT_MIN + 222)	//asynchronous get channel online state
#define NET_CLIENT_DETECTAREAPOSEX                  (NET_CLIENT_MIN + 223)
#define NET_CLIENT_GPS_DANGERAREA					(NET_CLIENT_MIN + 224)	//GPS Danger Area
#define NET_CLIENT_3DLOCATE_PREDICTION				(NET_CLIENT_MIN + 225)	//3D Locate prediction info
#define NET_CLIENT_HOLIDAY_SCHE                     (NET_CLIENT_MIN + 226)  //Holiday schedule
#define NET_CLIENT_HOLIDAY_PLAN                     (NET_CLIENT_MIN + 227)  //Holiday Plan
#define NET_CLIENT_GRANARY_VEHICLEDELAY             (NET_CLIENT_MIN + 228)  //Granary Vehicle Detect Delay
#define NET_CLIENT_TENCENT_INFO                     (NET_CLIENT_MIN + 229)  //tencent platform
#define NET_CLIENT_MODIFY_FROPSD                    (NET_CLIENT_MIN + 230)  //modify front password
#define NET_CLIENT_USERNAME_LETTER_CASE             (NET_CLIENT_MIN + 231)  //convert Username to uppercase
#define NET_CLIENT_SHDB_RUNSTATE					(NET_CLIENT_MIN + 232)  //set runstate
#define NET_CLIENT_SHDB_ALARMPIC					(NET_CLIENT_MIN + 233)  
#define NET_CLIENT_SHDB_TIMESNAP					(NET_CLIENT_MIN + 234) 
#define NET_CLIENT_ALM_LOOP_DETEC                	(NET_CLIENT_MIN + 235)  //set whether alarm input loop detection is on
#define NET_CLIENT_IDENTIFICATION_TYPE          	(NET_CLIENT_MIN + 236)  //set identification type
#define NET_CLIENT_DEV_AUTOTIMING					(NET_CLIENT_MIN + 237)
#define NET_CLIENT_DEVICENAME          	            (NET_CLIENT_MIN + 238)  //set device name
#define NET_CLIENT_IPV6_FILTER						(NET_CLIENT_MIN + 239) // IPv6 address filtering (black and white list)
#define NET_CLIENT_CHNDEVINFO						(NET_CLIENT_MIN + 240)
#define NET_CLIENT_PARSEPARA_IPV6					(NET_CLIENT_MIN + 241)
#define NET_CLIENT_LOCALSTORE_MAPDEVICE_IPV6		(NET_CLIENT_MIN + 242)
#define NET_CLIENT_GET_ENCODERINFO					(NET_CLIENT_MIN + 243)	//get encodee device info, include ipc and nvr
#define NET_CLIENT_SMD_SCENE_ENABLE					(NET_CLIENT_MIN + 244)	//Intelligent scene enabling settings
#define NET_CLIENT_WATERLEVEL_SOURCE				(NET_CLIENT_MIN + 245)	//Type of equipment water level data source (integrated water level machine) 
#define NET_CLIENT_WATERLEVEL_SOURCE_ID				(NET_CLIENT_MIN + 246)	//Equipment acceptance water level data source ID (water conservancy integrated machine) 
#define NET_CLIENT_IPV6_ADDRLIST					(NET_CLIENT_MIN + 247)	//IPV6 Address list
#define NET_CLIENT_OSD_CLARITY						(NET_CLIENT_MIN + 248)	//Character overlay background transparency 
#define NET_CLIENT_LEDPOWER_LIMIT					(NET_CLIENT_MIN + 249)  //Obtain the power consumption limit parameters of the device fill light of the channel
#define NET_CLIENT_OSD_TIMEFORMAT                   (NET_CLIENT_MIN + 250)  //set/get face osd time format
#define NET_CLIENT_OSD_WORDOVERLAY                  (NET_CLIENT_MIN + 251)   //set/get face osd wordoverlay
#define NET_CLIENT_OSD_DEVCOMMON                    (NET_CLIENT_MIN + 252)   //set/get osd dev common info
#define NET_CLIENT_IMAGE_LOCATION                   (NET_CLIENT_MIN + 253)
#define NET_CLIENT_MODULEPOWER_LIMIT				(NET_CLIENT_MIN + 254)	 //Get module power limit parameters
#define NET_CLIENT_MANUAL_PORTMAP                   (NET_CLIENT_MIN + 255)
#define NET_CLIENT_LOGONFAILED_RECONNECT			(NET_CLIENT_MIN + 256)
#define NET_CLIENT_VIDEO_COVER						(NET_CLIENT_MIN + 257)
#define NET_CLIENT_IEEE8021X						(NET_CLIENT_MIN + 258)
#define NET_CLIENT_COMMON_RTMP						(NET_CLIENT_MIN + 259)
#define NET_CLIENT_IEEE8021X_STATE					(NET_CLIENT_MIN + 260)
#define NET_CLIENT_CALIBRATEXY				        (NET_CLIENT_MIN + 261)
#define NET_CLIENT_VIDEO_COVER_EX					(NET_CLIENT_MIN + 262)
#define NET_CLIENT_TEMPERATURE_STANDARD				(NET_CLIENT_MIN + 263)
#define NET_CLIENT_BLACKBODY_CORRECT				(NET_CLIENT_MIN + 264)
#define NET_CLIENT_BODYTEMP_CORRECT					(NET_CLIENT_MIN + 265)
#define NET_CLIENT_INTELLIGENT_CORRECT				(NET_CLIENT_MIN + 266)
#define NET_CLIENT_RTMPCLIENT_LINKSTATE             (NET_CLIENT_MIN + 267)
#define NET_CLIENT_RADAR_EVENT_PARA		            (NET_CLIENT_MIN + 268)
#define NET_CLIENT_RADAR_DEVICE_PARA	            (NET_CLIENT_MIN + 269)
#define NET_CLIENT_CROPCODING                       (NET_CLIENT_MIN + 270)
#define NET_CLIENT_INTERESTED_AREA_EX				(NET_CLIENT_MIN + 271) // Region of interest 5-8
#define NET_CLIENT_RADAR_ADVANCED_PARA				(NET_CLIENT_MIN + 272) //radar advanced para
#define NET_CLIENT_RADAR_CALIBRATE					(NET_CLIENT_MIN + 273) //radar calibrate
#define NET_CLIENT_BLACK_BODY_DETECT				(NET_CLIENT_MIN + 274) //Black body detection
#define NET_CLIENT_WIFI_STATE                       (NET_CLIENT_MIN + 275) //wifi state
#define NET_CLIENT_SMALL_CELL                       (NET_CLIENT_MIN + 276)
#define NET_CLIENT_SMALL_IMSI                       (NET_CLIENT_MIN + 277)
#define NET_CLIENT_PLATE_FILTER_PARAM               (NET_CLIENT_MIN + 278) //Plate filter param
#define NET_CLIENT_TEMPETURE_POSITION				(NET_CLIENT_MIN + 279) //Setting the temperature measurement part of human body
#define NET_CLIENT_HD_TIMERANGE_PARAMEX				(NET_CLIENT_MIN + 280) //Hd camera parameters template
#define NET_CLIENT_WATERFLOW_INFO                   (NET_CLIENT_MIN + 281) //Get WaterFlow Info
#define NET_CLIENT_SINGLE_PIC_FEATURE               (NET_CLIENT_MIN + 282) //Feature matting parameter acquisition
#define NET_CLIENT_WATERGAUGE_INFO                  (NET_CLIENT_MIN + 283) //Get WaterGauge Info
#define NET_CLIENT_TRAFFIC_FILL_LIGHT_INFO			(NET_CLIENT_MIN + 284) //Traffic fill light info
#define NET_CLIENT_COM_RECVDATA                     (NET_CLIENT_MIN + 285) // com recv data
#define NET_CLIENT_EBIKE_PLATE						(NET_CLIENT_MIN + 286) //Electric vehicle license plate recognition parameters
#define NET_CLIENT_WATERFLOW_PARAM                  (NET_CLIENT_MIN + 287) //Water Flow Param
#define NET_CLIENT_WORDOVERLAYPOS                   (NET_CLIENT_MIN + 288) //Set/Get Word OverLay Position
#define NET_CLIENT_IRRIGATIONSERVERINFO             (NET_CLIENT_MIN + 289) //Set/Get IrrigationServerInfo
#define NET_CLIENT_GET4GDEVICE                      (NET_CLIENT_MIN + 290) //Get 4g Information
#define NET_CLIENT_GETDEVICEID                      (NET_CLIENT_MIN + 291) //Get DeviceID
#define NET_CLIENT_AUTODETECT                       (NET_CLIENT_MIN + 292) //Get Auto Detect
#define NET_CLIENT_TDSERVERSYSPARAM					(NET_CLIENT_MIN + 293) //Get TD Server systerm param
#define NET_CLIENT_HTTPPICSTREAM					(NET_CLIENT_MIN + 294) //Get HTTPPICSTREAM
#define NET_CLIENT_GAT1400_PARA 					(NET_CLIENT_MIN + 295) //Get GAT1400 para
#define NET_CLIENT_VIRTUALGAUGEDIS                  (NET_CLIENT_MIN + 296) //Set/Get VirtualDis
#define NET_CLIENT_IRRIFUNCASSEMBLE					(NET_CLIENT_MIN + 297) //Get Irri FuncAssemble
#define NET_CLIENT_DEVREALTIMEPARAM					(NET_CLIENT_MIN + 298) //Get Device Real Time Param
#define NET_CLIENT_GETDISKINFOEX                    (NET_CLIENT_MIN + 299) //Get Disk InfoEX
#define NET_CLIENT_WIEGAND                          (NET_CLIENT_MIN + 300) //Get/Set wiegand
#define NET_CLIENT_LOWPOWER							(NET_CLIENT_MIN + 301) //Set/Get lowpower
#define NET_CLIENT_LENPARA                          (NET_CLIENT_MIN + 302) //Get/Set lenpara
#define NET_CLIENT_NASPARAM							(NET_CLIENT_MIN + 303) //Get/Set nasparam
#define NET_CLIENT_SIPAUDIO							(NET_CLIENT_MIN + 304) //Get/Set Sip Audio
#define NET_CLIENT_RTSPINFO							(NET_CLIENT_MIN + 305) //Get/Set Rtsp Info
#define NET_CLIENT_GAUGECALIB						(NET_CLIENT_MIN + 306) //Set Gauge calib
#define NET_CLIENT_WATER_DETECTPOS					(NET_CLIENT_MIN + 307) //Get WaterDetect Pos
#define NET_CLIENT_IRRI_SERVERINFO					(NET_CLIENT_MIN + 308) //Set/Get Irrigation Server Info
#define NET_CLIENT_IRRI_UPLOADPARAM					(NET_CLIENT_MIN + 309) //Set/Get Irrigation Upload Param
#define NET_CLIENT_RTU_PARAM						(NET_CLIENT_MIN + 310) //Set/Get Rtu Param
#define NET_CLIENT_NET_SERVICEPARA					(NET_CLIENT_MIN + 311) //Set/Get NetService Para
#define NET_CLIENT_NET_CERATIFICATE					(NET_CLIENT_MIN + 312) //Get Certificate Info
#define NET_CLIENT_VIDEOLIMIT						(NET_CLIENT_MIN + 313) //Get VideoLimit
#define NET_CLIENT_SETOSDLOGO_NEW					(NET_CLIENT_MIN + 314) //Set Osd Logo
#define NET_CLIENT_DORMANCY_LOWPOWER				(NET_CLIENT_MIN + 315)	//Get Low Power
#define NET_CLIENT_DORMANCY_TIME					(NET_CLIENT_MIN + 316)	//Dormancy Time
#define NET_CLIENT_DISARM_MAUANL					(NET_CLIENT_MIN + 317)
#define NET_CLIENT_DISARM_CYCLE						(NET_CLIENT_MIN + 318)
#define NET_CLIENT_DISARM_INPUT						(NET_CLIENT_MIN + 319)
#define NET_CLIENT_FIREALARM_GEOGRAPHY_LOCATION		(NET_CLIENT_MIN + 320) //Get the GPS location of the fire（Customized special）
#define NET_CLIENT_GETCURALARMINFO					(NET_CLIENT_MIN + 321) //Get Current Alarm Info
#define NET_CLIENT_ELEVATOR_MONITOR					(NET_CLIENT_MIN + 322) //Set/Get Elevator Monitor
#define NET_CLIENT_ELEVATOR_STOREYINFO				(NET_CLIENT_MIN + 323) //Set/Get Elevator Floor
#define NET_CLIENT_ELEVATOR_STATE					(NET_CLIENT_MIN + 324) //Get Elevator State
#define NET_CLIENT_ELEVATOR_STATISTICS				(NET_CLIENT_MIN + 325) //Set/Get Elevator Statistics
#define NET_CLIENT_DIGITALWATERMARK					(NET_CLIENT_MIN + 326) //digital watermark
#define NET_CLIENT_SSL_ENCRYPT_NVR					(NET_CLIENT_MIN + 327)
#define NET_CLIENT_ELEVATOR_ALLSTATE				(NET_CLIENT_MIN + 328) //Get Elevator All State
#define NET_CLIENT_IRRI_ELECFENCE					(NET_CLIENT_MIN + 329) //水利电子围栏配置参数
#define NET_CLIENT_IRRI_ALGEXTINFO					(NET_CLIENT_MIN + 330) //水利算法扩展信息配置参数
#define NET_CLIENT_WATERQUALITY_ADJUST_INFO			(NET_CLIENT_MIN + 331) //获取水质光谱校正信息
#define NET_CLIENT_VCMOSAIC							(NET_CLIENT_MIN + 332) //获取合成通道马赛克遮挡区域
#define NET_CLIENT_STSTEMTYPE						(NET_CLIENT_MIN + 333) //获取设备制造商
#define NET_CLIENT_MOTION_DETECTTION_CAR_PARAM		(NET_CLIENT_MIN + 334) //移动侦测之车形报警使能
#define NET_CLIENT_NETFILE_FORCESTOP_PARA			(NET_CLIENT_MIN + 335) //远程下载强制断开参数，对应参数结构体NetFileForceStopPara
#define NET_CLIENT_GDDZ_SECURITY_CLASSIFICATION		(NET_CLIENT_MIN + 336) //定制专用 设置密级参数
#define NET_CLIENT_GDDZ_AUTH_RESULT					(NET_CLIENT_MIN + 337) //GDDZ获取认证结果码
#define NET_CLIENT_WATERGAUGE_INFOEX                (NET_CLIENT_MIN + 338) //Get WaterGauge Info
#define NET_CLIENT_PARAM_DISKINFOREALTIME			(NET_CLIENT_MIN + 339) //Get disk expenion information
#define NET_CLIENT_DUALUSERS_AUTH_SESSION			(NET_CLIENT_MIN + 340) //Set dual users authentication session
#define NET_CLIENT_INDUSTRY_PLATFORM_TAG_INFO		(NET_CLIENT_MIN + 341) //获取行业平台标识信息
#define NET_CLIENT_WATER_IRRIFITTING_COEF           (NET_CLIENT_MIN + 342)	//Get Water irrifitting coef
#define NET_CLIENT_RECORDCHN_ALIASNAME				(NET_CLIENT_MIN + 343) //设置/获取录像通道别名
#define NET_CLIENT_IRRI_SERVERINFO_V2				(NET_CLIENT_MIN + 344) //Set/Get Irrigation Server Info V2
#define NET_CLIENT_IRRI_REPORTDORMANCY				(NET_CLIENT_MIN + 345) //设置/获取水位加报的低功耗时间
#define NET_CLIENT_DORMANCY_SCHEDULE_V2             (NET_CLIENT_MIN + 346)
#define NET_CLIENT_IRRRECEIVERCOMMONMSG				(NET_CLIENT_MIN + 347) //设置水利设备通信联系人通用信息
#define NET_CLIENT_THMSCREEN_SENSORCORRECT			(NET_CLIENT_MIN + 348) //设置温湿度屏传感器数据校正值
#define NET_CLIENT_THMSCREEN_SYSTEM					(NET_CLIENT_MIN + 349) //设置温湿度屏系统参数
#define NET_CLIENT_MOBILEPARAM						(NET_CLIENT_MIN + 350) //设置定向流量卡测试ip
#define NET_CLIENT_LANPRIORITY						(NET_CLIENT_MIN + 351) //设置网卡优先级
#define NET_CLIENT_DISKGROUPV2 						(NET_CLIENT_MIN + 352) //设置磁盘盘组模板（设置大于15盘组的盘组）
#define NET_CLIENT_SAVE_ALARMLINK					(NET_CLIENT_MIN + 353) //开启默认保存ALARMLINK
#define NET_CLIENT_MAX								(NET_CLIENT_MIN + 354)



//FEC SRATR
#define NET_CLIENT_FEC_MIN							(NET_CLIENT_MIN + 1000)	
#define NET_CLIENT_FEC_EPTZ							(NET_CLIENT_FEC_MIN + 0)	
#define NET_CLIENT_FEC_EPRESET						(NET_CLIENT_FEC_MIN + 1)
#define NET_CLIENT_FEC_KEY							(NET_CLIENT_FEC_MIN + 2)
#define NET_CLIENT_FEC_MAX							(NET_CLIENT_FEC_MIN + 3)	

#define NET_CLIENT_NEWTOKEN							(NET_CLIENT_MIN + 10000) //暂时不支持


#define CHAN_CFG_MIN										(0)
#define CHAN_CFG_VIDEO_DECRYPT						(CHAN_CFG_MIN + 0)
#define CHAN_CFG_MAX								(CHAN_CFG_MIN + 1)

//NET_CLIENT_AUDIO_SAMPLE_COUNT
typedef struct _tagAudioSampleFileCount
{
	int iSize;
	int iTotalCount;			//total count
	int iSampleCount;			//Sample count
	int iCustomCount;
	int iTtsNum;
}AudioSampleFileCount, *PAudioSampleFileCount;

//Encrypt the externale interface
typedef struct _tagEncrypt_Info
{
	int iSize;
	int iPasswdErrTimes;			//Number of password errors
}TEncrypt_Info_Out, *PTEncrypt_Info_Out;

//pse ch zyb add
#define MAX_PSE_CHANNEL_NUM		16
typedef struct tagPseChState
{
	int				iBufSize;
	int				iPseCh;
	int				iState;			
	int				iPower;
}PseChState, *pPseChState;

typedef struct tagPseChProperty
{
	int				iBufSize;	
	int				iPseCh;			
	int				iWorkMode;
}PseChProperty, *pPseChProperty;

typedef struct tagPseChInfo
{
	int				iBufSize;	
	int				iPseChNum;		//PSE total channel number			
	int				iPsePower;		//PSE total power
}PseChInfo, *pPseChInfo;

//autotest use it to confirm test  item
typedef struct _tagDevAbstract 
{
	int		iBufSize;
	int		iSataPortNum;  //sata num
	int		iESataPortNum; //esata num
	int     iUsbPortNum;   //usb num
	int     iSDCardNum;	   //sd card num
	int     iVirLanNum;	   //VirLanNum
}TDevAbstract, *pTDevAbstract;

typedef struct tagDiskSmartEnable
{
	int				iSize;	
	int				iDiskId;			
	int				iEnable;
}DiskSmartEnable, *pDiskSmartEnable;


typedef struct tagSmartInfo
{
	int				iSize;	
	char			cId[LEN_8];			
	char			cAttributeName[LEN_32];	
	int				iFlag;
	int				iValue;
	int				iWorst;
	int				iThresh;
	char			cStatue[LEN_16];
	char			cRawValue[LEN_64];
}SmartInfo, *pSmartInfo;

#define MAX_SMART_INFO_NUM	256
#define SMART_INFO_NUM		32
typedef struct tagDiskSmartMsg
{
	int				iSize;	
	int				iDiskId;			
	int				iTotalPackageNum;		//total message number
	int				iCurrentPackageMsgNo;			//current message No
	char			cHDDModel[LEN_32];//HDD model
	char			cSericalNum[LEN_16];
	int				iTemprt;			//temperature
	int				iUsedTime;
	int				iHealth;
	int				iAllHealth;
	int				iTotalSmartInfoNum;
	pSmartInfo		pstSmartInfo;
}DiskSmartMsg, *pDiskSmartMsg;

typedef struct tagP2PAppUrl
{
	int				iBufSize;
	int				iTypeId;	//1=android，2=IOS
	char			cUrl[256];	//include "http://"
}P2PAppUrl,*pP2PAppUrl;

//S3E
typedef struct tagWhiteLight
{
	int				iBufSize;
	int				iMode;		//work mode
}WhiteLight,*pWhiteLight;

typedef struct tagApertureType
{
	int				iBufSize;
	int				iType;			//0-retain 1-DC 2-Piris
}ApertureType, *pApertureType;

typedef struct tagIdentiCode
{
	int				iBufSize;
	char			cParam[LEN_32];
}IdentiCode, *pIdentiCode;

typedef struct tagMaxLanRate
{
	int				iBufSize;
	int				iLanNo;
	int				iRate;
}MaxLanRate, *pMaxLanRate;

#define MAX_PRESET_LIST_NUM	501
typedef struct tagPTZList
{
	int				iBufSize;
	int				iChanNo;
	int             iFocusMode[MAX_PRESET_LIST_NUM]; //PTZ focus mode  0:auto   1:fixed -1:unknown
	int				iPtzState[MAX_PRESET_LIST_NUM];  //PTZ state 0:set 1:delete 2:call -1:unknown
}PTZList, *pPTZList;

typedef struct tagColorParaList
{
	int iSize;
	int iChanNo;
	int iParam;
}tColorParaList, *ptColorParaList;

//ITS 6M 2014/12/05
typedef struct tagITSTrafficFlow
{
	int				iBufSize;		//Size of struct
	int				iLaneID;		//LaneID ([0, 3])
	int				iType;			//Statistical Type ([0, 1])
	int				iEnable;		//Traffic Statistics Enable ([0, 1])
	int				iTimeInterval;	//Statistics Time Interval ([1, 1440])
}ITSTrafficFlow, *pITSTrafficFlow;

typedef struct tagITSTrafficFlowReport
{
	int				    iSize;				//Size of struct
	int				    iLaneID;			//Lane ID ([0, 3])
	int				    iFlow;				//Flow 
	int				    iHoldRate;			//Lane Hold Rate
	int				    iSpeed;				//Average Speed
	int				    iDistance;			//  	
	NVS_FILE_TIME 	    stStartTime;	
	NVS_FILE_TIME  	    stStopTime;			//Time Range
	int                 iCarTypeTotal;
	CarTypeNum          tCarTypeNum[LEN_32];
	int                 iCarQueueLength;
	int                 iHeadDistance;
	int                 iRoomRate;
	int                 iRunState;
	char                pcRoadName[LEN_64];
	int                 iChanNo;
	int                 iSceneID;
	NVS_FILE_TIME 	    tNewStartTime;	
	NVS_FILE_TIME  	    tNewStopTime;			//Time Range
}ITSTrafficFlowReport, *pITSTrafficFlowReport;

#define MAX_ITS_ILLEGALTYPE		12	    //not use after@151211
typedef struct tagITSIllegalType
{
	int				iBufSize;				//Size of struct
	int				iLaneID;				//Lane ID ([0, 3])
	int				iTypeID;				//Illegal Type ID ([1, 12 ])
	char			cMarkID[LEN_32];		//Type ID
	int				iPriority;				//Priority([0, 100] )
	char			pcName[LEN_256];			//Illegal Name
}ITSIllegalType, *pITSIllegalType;

typedef struct tagITSPicMergeOverLay
{
	int		iBufSize;				//Size of struct
	int		iLaneID;				//Lane No
	int		iOneOSD;
	int		iOsdType;				
	int		iEnable;				
	char	cOsdName[LEN_32];
	int		iPosX;				
	int		iPosY;
	int     iZoomScale;
	int		iLineCharNum;
	int		iBlankCharNum;
	int		iOsdPosNo;
	int		iCapType;
	int     iPicIndex;
	int     iFacePicType;
	int		iMergePicIllegalTime;
	int		iSecurityCodeLink;
	int		iThreeMergeSeCodeEn;
}ITSPicMergeOverLay, *pITSPicMergeOverLay;


#define MAX_LIST_COUNT	32

typedef struct tagPicDirectory
{
	int				iDirectoryId;
	char			cUserDefine[LEN_32];
}PicDirectory,*pPicDirectory;

typedef struct tagPicDirectoryEx
{
	int				iDirectoryId;
	char			cUserDefine[LEN_64];
}PicDirectoryEx,*pPicDirectoryEx;

typedef struct tagPicName
{
	int				iDirectoryId;
	char			cUserDefine[LEN_32];
}PicName,*pPicName;

#define MAX_FTP_NUM				3
#define MAX_USER_DEFINE         65535
typedef struct tagFtpUpload
{
	int				iSize;
	int				iFtpNum;		//0：retain；1：ftp1；2：ftp2
	int				iFtpEnable;		//0-unable 1-enable
	char			cFtpAddr[LEN_32];
	char			cUserName[LEN_16];
	char			cPassWord[LEN_16];
	int				iPort;			//ftp port [0,65535]
	int				iFtpType;		//0：retain； 1：bayonet； 2：break rules（two ftp mutual exclusion）
	int				iListCount;		//0：root list； 1~n：n-list
	int				iPicNameCount;	//0：none 1~n：n-list
	int				iPlateEnable;	//small picture of plate number 
	pPicDirectory   pstDirectory;	
	pPicName        pstPicName;	
	int				iIniUpload;
	int				iFilterEnable;
	int				iFaceUpload;	//small face picture upload
	int				iIllegalVideoUpload;	//Traffic violation video upload (FTP) switch 0- reserved, 1- on, 2- off
	char			cSeparator[LEN_2];//Valid values "_ + -. =" The effective length is 1 byte.
	pPicDirectoryEx  pstDirectoryEx;
	char			cEncodeType[LEN_4]; //ftp sever path of storage and name of picture GB2312=0, UTF-8=1, other=-1
}FtpUpload, *pFtpUpload;

typedef struct tagCarLogoOptim
{
	int				iSize;
	int				iOptimID;
	int				iEnable;				//0-Unable；1-Enable
	char			cFirstChar[LEN_32];
	char			cFirstLetter[LEN_16];
	char			cSecondLetter[LEN_32];
	int				iLogoType;				
}CarLogoOptim, *pCarLogoOptim;

typedef struct tagITS_LoopMode
{
	int		iBufSize;
	int		iLaneID;			//Lane No	Max:4  value：0-3	
	int		iLoopMode;			//0：One Loop；1：two Loop；2：three Loop											
}ITS_LoopMode, *pITS_LoopMode;

//add end

//HN zyb add 20150123
typedef struct tagDevAmplitude
{
	int				iSize;
	int				iChanNo;
	int				iMicNo;
	int				iValue;		
}DevAmplitude, *pDevAmplitude;
//add end

#define MAX_DOME_TYPE	25
#define MAX_DOME_PARA_GROUP_NUM		8
typedef struct __tagDomeParam
{
	int iType;
	int iParam1;
	int iParam2;
	int iParam3;
	int iParam4;
}TDomeParam, *pTDomeParam;

#define MAX_SCHEDULE 8
typedef struct __tagDomeWork
{
	int iBeginHour;
	int iBeginMinute;
	int iEndHour;
	int iEndMinute;
	int iWorkType;		//0-No action, 1-Preset position, 2-Sanning, 3-Cruise, 4-Mode, 5-Auxiliary output, 6-Low power consumption
	int iWorkValue;		//Preset position number range:1 ~ 8,
						//Scan number range: 1 ~ 4
						//Cruise number range: 1 ~ 4
						//Mode path number range: 1 ~ 4
						//Auxiliary output number range: bit0-output NO1, bit1-output NO2
						//Low power consumption: 0-not open, 1-open

	int iEnable;		//0-Not enable,1-enable
}TDomeWork, *pTDomeWork;

//Color to black type，Defined by SDK
#define COLOR_AUTO_IN				0			//Automatic internal synchronization
#define COLOR2GRAY_GRAY				1			//Black and white
#define COLOR2GRAY_COLOR			2			//Color
#define COLOR_AUTO_OUT				3			//Automatic external synchronization
#define COLOR_AUTO_ALARM			4			//Alarm synchronization
#define COLOR2GRAY_TIMED			5			//Timing
#define COLOR2GRAY_AUTO				6           //auto
#define COLOR2GRAY_FILL_LIGHT		7			//Fill mode (turn lights, video holding color)

//Color to gray
typedef struct __tagColor2GrayParam
{
	int  m_isize;         //Structure size
	int  m_iColor2Gray;   //Color to gray type
	int  m_iDayRange;     //Daylightness value range
	int  m_iNightRange;   //Night brightness value range
	int  m_iTimeRange;    //Timing range：Start time in the daytime, start time in the evening。
	int  m_iColorDelay;   //Color to black delay
	int  m_iGrayDelay;    //Black to color delay
	int	 m_iDevType;	  //0:Monitoring Products   1:ITS Products
	int  m_iSensitivity;  //0-Reserve 1-low 2-normal 3-high
}TColor2GrayParam,*pTColor2GrayParam;

//Network card parameter
typedef struct __tagLanParam
{
	int m_iSize;		  //Structure size
	int m_iNo;			  //Network card number：lan1：0； lan2:1
	int m_iMode;		  //Network card mode：0：Reserved；1：Half duplex；2：Reserved；3：Duplex。
	int m_iSpeed;		  //Rate
}TLanParam,*pTLanParam;

#define MAX_DEVCOMNAME_NO   4		//Number of channel types
#define DEVCOMNAME_VIDEO    0		//Video
#define DEVCOMNAME_ALARMIN	1       //Alarm input
#define DEVCOMNAME_ALARMOUT 2       //Alarm output
#define DEVCOMNAME_ALARMOUTPORT 3       //Alarm output port name
//Customize the alias for the device's generic interface
typedef struct __tagDevCommonName
{
	int  m_iSize;				//Structure size
	int	 m_iChannelType;		//Channel type
	char m_cChanneleName[LEN_64+1];   //Channel name
}TDevCommonName,*pTDevCommonName;

typedef struct __tagDomeWorkSchedule
{	
	int iWeekDay;
	TDomeWork tWork[MAX_SCHEDULE];
	int iTotalParaCnt;		//Total Number Of Parameter Index
	int iCurParaIndex;		//Current Parameter Index
}TDomeWorkSchedule, *pTDomeWorkSchedule;

#define MAX_APPEND_DSD_LAY_BUM	7
#ifndef MAX_OSD_LENGTH
#define	MAX_OSD_LENGTH			768
#endif
typedef struct __tagAppendOSD
{
	int iSize;
	int iLayNo;
	int iColor;
	int iDiaphaneity;
	int iFontSize;
	int iPosX;
	int iPosY;
	char pcText[MAX_OSD_LENGTH];
}TAppendOSD, *pTAppendOSD;

#define MAX_INTERESTED_AREA_NUM	9


#define MAX_NVR_AUTOREBOOT_SCHEDULE			11
typedef struct __tagAutoRebootSchedule
{
	int iSize;
	int iWeekDay;			//星期日到星期六为0～6，7-每天，8-从不，9-年周期，10-月周期
	int iHour;				//范围[0, 23]
	int iMinute;			//范围[0, 59]
	int iMonth;				//范围[1, 12]
	int iDay;				//范围[1, 31]
}TAutoRebootSchedule, *pTAutoRebootSchedule;

typedef struct __tagDateFormat
{
	int iSize;
	int iFormatType;			//The format type:case 0:/*yyyy/mm/dd*/; 1:/*mm/dd/yyyy*/; 2:/*dd/mm/yyyy*/; 3:/*M day yyyy*/; 4:/*day M yyyy*/
	char cSeparate;				//Separator, can be set to “/”，“-”
	char cReserved[3];			//Byte alignment
	int iFlagWeek;				//Indicates whether the week is displayed: 0-Not display，1-Display					
	int iTimeMode;				//Indicates the time format: 0-24 hour format，1-12 hour format
	int iMsecShow;				//Whether to display millisecond. 0-Not display，1-Display
	int iSeparateTimeCh;		//Whether the delimiter time uses ch characters, eg“2016/8/2 10:12:20” 0-Not display，1-Display
	int iSeparateDateCh;		//Whether the delimiter date uses ch characters，eg“2016/8/2 10:12:20” 0-Not display，1-Display
	int iChannelNo;				//channel num
	int iLanguage;				//The language of the datetime format It is mainly to add the linguistic meaning of M 3 and 4 in iFormat 0 - English (default English without this field) 1- Russian
}TDateFormat, *pDateFormat;

//Summer time
typedef struct __tagDaylightSavingTime
{
	int m_iSize;
	int m_iMonth;				//Month
	int m_iWeekOfMonth;			//Week number(0：last，1：first，2：second，3：third，4：forth)
	int m_iDayOfWeek;			//（Sunday to saturday is 0-6）
	int m_iHourOfDay;			//Hour（0～23）
}TDaylightSavingTime, *pTDaylightSavingTime;

typedef struct __tagDaylightSavingTimeSchedule
{
	int m_iSize;
	int m_iDSTEnable;					//Summer time enable,0-forbid，1-enabled
	int m_iOffsetTime;					//Offset time,0～120（unit：minute）
	TDaylightSavingTime m_tBeginDST;	//Starting time
	TDaylightSavingTime m_tEndDST;		//End time
}TDaylightSavingTimeSchedule, *pTDaylightSavingTimeSchedule;

//Remote forcibly disconnects a user
typedef struct __tagNetOffLine
{
	int iSize;
	char cIPAddr[LENGTH_IPV4];				//IP address、domain name
	int iOffTime;							//Off time,seconds；minimum 10 seconds
	char pcIPAddrv6[LENGTH_IPV6_V1];		//IPv6 address、domain name
}TNetOffLine, *pTNetOffLine;

#define DEFAULT_HTTP_PORT			80	
#define DEFAULT_HTTP_HTTPSPORT		443
#define DEFAULT_HTTP_RTSPPORT		554
#define DEFAULT_HTTP_SCHEDULEPORT	8005
//HTTP Port
typedef struct __tagHttpPort
{
	int iSize;
	int iPort;			//http Port
	int iHttpsport;		//https Port,Default 443
	int	iRtspPort;		//rtsp Port,Default554
	int iSchedulePort;	//schedule port num,default 8005
	int iServerPort;    //Server Port, default 3000，Get Only
    int iRtmpServerPort; // rtmp server port, default 1936
	int iSrtpPort;		 //SRTP server port, default 322; 
						//if get iSrtpPort is 0, please try to use NetClient_GetDevConfig(NET_CLIENT_NET_SERVICEPARA) 
}THttpPort, *pTHttpPort;

//NTP info
typedef struct tagNTPInfo
{
	int		iBufSize;
	char	cServerIP[LEN_32];	//NTP Server IP
	int		iServerPort;			//NTP Server Port
	int		iIntervalHour;
	int		iIntervalSec;		
	char	cServerIPv6[LENGTH_IPV6_V1];
}NTPInfo,*pNTPInfo;


//VoDev list
#define MAX_VODEV_LIST_NUM	50
typedef struct tagVoDevList
{
	int		iBufSize;
	int		iCount;
	int		iVoDevId[MAX_VODEV_LIST_NUM];	
}VoDevList,*pVoDevList;

typedef struct tagVencType
{
	int iSize;
	int iStreamType;
	int iVencType;
}VencType, *pVencType;

typedef struct tagModeRule
{
	int iSize;
	int iChanNo;
	int iStreamNo;
	int iMode;
}ModeRule, *pModeRule;


//Login failed error code
#define UNKNOW_ERROR			0
#define CIPHER_USERNAME_ERROR	1
#define CIPHER_USERPASS_ERROR	2
#define NO_SUPPORT_ALGORITHM	3
#define PSWD_ERR_TIMES_OVERRUN	4
#define ILLEGAL_PLATFORM        5
#define LOGON_NET_ERROR			0xFF01
#define LOGON_PORT_ERROR		0xFF02

//HD camera parameter template type
#define CAMERA_PARA_MIN								0
#define CAMERA_PARA_IRIS_ADJUSTMENT 				CAMERA_PARA_MIN + 0		//0--Aperture adjustment
#define CAMERA_PARA_WDR								CAMERA_PARA_MIN + 1		//1--Wide dynamtic
#define CAMERA_PARA_BLC								CAMERA_PARA_MIN + 2		//2--Backlight compensation
#define CAMERA_PARA_EXPOSURE_TIME					CAMERA_PARA_MIN + 3		//3--Backlight time
#define CAMERA_PARA_SHUTTER_ADJUSTMENT				CAMERA_PARA_MIN + 4		//4--Shutter adjustment
#define CAMERA_PARA_GAIN_ADJUSTMENT					CAMERA_PARA_MIN + 5		//5--Gain adjustment
#define CAMERA_PARA_GAMMA_ADJUSTMENT				CAMERA_PARA_MIN + 6		//6--Gamma adjustment
#define CAMERA_PARA_SHARPNESS_ADJUSTMENT			CAMERA_PARA_MIN + 7		//7--Sharpness adjustment
#define CAMERA_PARA_NOISE_REDUCTION_ADJUSTMENT		CAMERA_PARA_MIN + 8		//8--Noise regulation adjustment
#define CAMERA_PARA_EXPOSURE_REGION					CAMERA_PARA_MIN + 9		//9--Exposure area
#define CAMERA_PARA_BLC_REGION						CAMERA_PARA_MIN + 10	//10--Backlight compensation area
#define CAMERA_PARA_AF_REGION_SET					CAMERA_PARA_MIN + 11	//11--AF area settings
#define CAMERA_PARA_TARGET_BRIGHTNESS_ADJUSTMENT	CAMERA_PARA_MIN + 12	//12--Target brightness adjustment
#define CAMERA_PARA_WHITE_BALANCE_ADJUSTMENT		CAMERA_PARA_MIN + 13	//13--White balance adjustment
#define CAMERA_PARA_JPEG_IMAGE_QUALITY				CAMERA_PARA_MIN + 14	//14--JPEG Image quality
#define CAMERA_PARA_LUT_ENABLE						CAMERA_PARA_MIN + 15	//15--LUT enable
#define CAMERA_PARA_AUTOMATIC_BRIGHTNESS_ADJUST		CAMERA_PARA_MIN + 16	//16--Automatic brightness adjustment enable
#define CAMERA_PARA_HSBLC							CAMERA_PARA_MIN + 17	//17--Strong light suppression
#define CAMERA_PARA_AUTO_EXPOSURE_MODE				CAMERA_PARA_MIN + 18	//18--Automatic exposure mode
#define CAMERA_PARA_SCENE_MODE						CAMERA_PARA_MIN + 19	//19--Scene mode
#define CAMERA_PARA_FOCUS_MODE						CAMERA_PARA_MIN + 20	//20--Focus mode
#define CAMERA_PARA_MIN_FOCUSING_DISTANCE			CAMERA_PARA_MIN + 21	//21--Minimum focusing distance
#define CAMERA_PARA_DAY_AND_NIGHT					CAMERA_PARA_MIN + 22	//22--Day and night switching
#define CAMERA_PARA_RESTORE_DEFAULT					CAMERA_PARA_MIN + 23	//23--Reset default
#define CAMERA_PARA_THROUGH_FOG						CAMERA_PARA_MIN + 24	//24--Through fog
#define CAMERA_PARA_AE_SPEED						CAMERA_PARA_MIN + 25	//25--AE speed adjustment
#define CAMERA_PARA_SMARTIR							CAMERA_PARA_MIN + 26	//26-- SmartIR
#define CAMERA_PARA_EXPORE_COMPENSATION				CAMERA_PARA_MIN + 27	//27-- Exposure compensation
#define CAMERA_PARA_GAOGAN_DEGREE					CAMERA_PARA_MIN + 28	//28 - Gao Gan degrees
#define CAMERA_PARA_SLOW_EXPOSURE					CAMERA_PARA_MIN + 29	//29 - slow exposure
#define CAMERA_PARA_AUTO_CONTRAST					CAMERA_PARA_MIN + 30	//30-- Automatic contrast
#define CAMERA_PARA_IMAGE_FREEZE					CAMERA_PARA_MIN + 31	//31-- Image freeze
#define CAMERA_PARA_INFRARED_CORRECTION				CAMERA_PARA_MIN + 32	//32-- Infrared correction
#define CAMERA_PARA_COLOR_GAIN						CAMERA_PARA_MIN + 33	//33-- Color gain
#define CAMERA_PARA_COLOR_PHASE						CAMERA_PARA_MIN + 34	//34-- Color phase
#define CAMERA_PARA_LOW_ILLUMINATION				CAMERA_PARA_MIN + 35	//35-- Low illumination color suppression
#define CAMERA_PARA_FOCUS_TYPE						CAMERA_PARA_MIN + 36	//36-- Focus type
#define CAMERA_PARA_ENHANCED_NIGHTVISION			CAMERA_PARA_MIN + 37	//37-- Enhanced night vision
#define CAMERA_PARA_DARK_ANGLE_COMPENSATION			CAMERA_PARA_MIN + 38	//38-- Dark Angle compensation
#define CAMERA_PARA_VIDEO_BRIGHTNESS				CAMERA_PARA_MIN + 39	//39-- Video brightness
#define CAMERA_PARA_VIDEO_CONTRAST					CAMERA_PARA_MIN + 40	//40-- Video contrast
#define CAMERA_PARA_VIDEO_SATURATION				CAMERA_PARA_MIN + 41	//41-- Video saturation
#define CAMERA_PARA_VIDEO_CHROMA					CAMERA_PARA_MIN + 42	//42-- Video chroma
#define CAMERA_PARA_MIN_EXPOSURE_TIME				CAMERA_PARA_MIN + 43	//43-- Minimum exposure time
#define CAMERA_PARA_SCENE_ADAPTATION				CAMERA_PARA_MIN + 44	//44-- Scene adaptation
#define CAMERA_PARA_PSEUDO_COLOR					CAMERA_PARA_MIN + 45	//45-- pseudo color
#define CAMERA_PARA_GAIN_MODE						CAMERA_PARA_MIN + 46	//46-- Gain mode
#define CAMERA_PARA_FLAT_FIELD_CORRECTION			CAMERA_PARA_MIN + 47	//47-- Flat field correction
#define CAMERA_PARA_EXPOSURE						CAMERA_PARA_MIN + 48	//48-- no mean
#define CAMERA_PARA_VCA_NOISE_REDUCTION				CAMERA_PARA_MIN + 49	//49-- no mean
#define CAMERA_PARA_ANTI_FLASH						CAMERA_PARA_MIN + 50	//50-- anti-flash
#define CAMERA_PARA_MAX								CAMERA_PARA_MIN + 51

//Dome menu parameter template type
#define DOME_PARA_MIN										0
#define DOME_PARA_PRESET_TITLE_DISPLAY_TIME					(DOME_PARA_MIN + 1)		//1--Preset title display time
#define DOME_PARA_AUTOMATIC_FUNCTION_TITLE_DISPLAY_TIME		(DOME_PARA_MIN + 2)		//2--Automatic function title display time
#define DOME_PARA_REGION_TITLE_DISPLAY_TIME					(DOME_PARA_MIN + 3)		//3--Area title displays the time
#define DOME_PARA_COORDINATE_DIRECTION_DISPLAY_TIME			(DOME_PARA_MIN + 4)		//4--Coordinate direction display time
#define DOME_PARA_TRACEPOINTS_DISPLAY_TIME					(DOME_PARA_MIN + 5)		//5--Track point display time
#define DOME_PARA_TITLE_BACKGROUND							(DOME_PARA_MIN + 6)		//6--Title background
#define DOME_PARA_AUTOMATIC_STOP_TIME						(DOME_PARA_MIN + 7)		//7--Automatic stop time
#define DOME_PARA_MENU_OFF_TIME								(DOME_PARA_MIN + 8)		//8--menu off time
#define DOME_PARA_VERTICAL_ANGLE_ADJUSTMENT					(DOME_PARA_MIN + 9)		//9--Vertical angle adjustment
#define DOME_PARA_MANIPULATION_SPEED_RATING					(DOME_PARA_MIN + 10)	//10--Manipulation speed rating
#define DOME_PARA_PRESET_SPEED_RATING						(DOME_PARA_MIN + 11)	//11--Preset speed rating
#define DOME_PARA_TEMPERATURE_CONTROL_MODE					(DOME_PARA_MIN + 12)	//12--Temperature control mode
#define DOME_PARA_485_ADDRESS_SETTING						(DOME_PARA_MIN + 13)	//13--485 Address setting
#define DOME_PARA_ZERO_SETTING								(DOME_PARA_MIN + 14)	//14--Zero setting
#define DOME_PARA_NORTH_SETTING								(DOME_PARA_MIN + 15)	//15--North setting
#define DOME_PARA_CONTROL_MODE								(DOME_PARA_MIN + 16)	//16--Control mode
#define DOME_PARA_SENSITIVE_THRESHOLD						(DOME_PARA_MIN + 17)	//17--Sensitive threshold
#define DOME_PARA_DELAY_TIME								(DOME_PARA_MIN + 18)	//18--Delay time
#define DOME_PARA_ZOOM_MATCH								(DOME_PARA_MIN + 19)	//19--Zoom match
#define DOME_PARA_PRESET									(DOME_PARA_MIN + 20)	//20--Preset
#define DOME_PARA_SCANNING									(DOME_PARA_MIN + 21)	//21--Scanning
#define DOME_PARA_SCHEMA_PATH								(DOME_PARA_MIN + 22)	//22--Schema path
#define DOME_PARA_SCHEMA_PATH_CURRENT_STATE					(DOME_PARA_MIN + 23)	//23--Schema path current state
#define DOME_PARA_REGIONAL_INDICATIVE						(DOME_PARA_MIN + 24)	//24--Regional indicative
#define DOME_PARA_ZOOM_SPEED								(DOME_PARA_MIN + 25)	//25--Zoom speed
#define DOME_PARA_DIGITAL_ZOOM								(DOME_PARA_MIN + 26)	//26--Digital zoom
#define DOME_PARA_PRESET_FROZEN								(DOME_PARA_MIN + 27)	//27--Preset freeeze
#define DOME_PARA_LASER_LIGHT								(DOME_PARA_MIN + 28)	//28--Laser brightness threshold
#define DOME_PARA_LASER_COAXIAL								(DOME_PARA_MIN + 29)	//29--Laser coaxial setting
#define DOME_PARA_KEYING_LIMIT								(DOME_PARA_MIN + 31)	//31--Key limit setting
#define DOME_PARA_OUTAGE_MEMORY								(DOME_PARA_MIN + 32)	//32--Power-down memory mode
#define DOME_PARA_PTZ_PRIOR									(DOME_PARA_MIN + 33)	//33--PTZ first
#define DOME_PARA_KEYING_USING								(DOME_PARA_MIN + 34)	//34--Key limit using
#define DOME_PARA_LIGHT_CTRL								(DOME_PARA_MIN + 35)	//35--light control mode	
#define DOME_PARA_WHITE_LIGHT_CTRL							(DOME_PARA_MIN + 36)	//36--White light control mode
#define DOME_PARA_LASER_CTRL								(DOME_PARA_MIN + 37)	//37--laser control mode
#define	DOME_PARA_POWER_SAVING_MODE							(DOME_PARA_MIN + 38)	//38--Power saving mode
#define	DOME_PARA_DOT_LASTER_CTRL							(DOME_PARA_MIN + 39)	//39--Dot laser control
#define	DOME_PARA_DEV_ANGLE_SET								(DOME_PARA_MIN + 40)	//40--Equipment inclination setting
#define	DOME_PARA_AUTOMIC_WIPER_SET							(DOME_PARA_MIN + 41)	//41--Automatic wiper settings
#define	DOME_PARA_SIMILARITY_ANGLE							(DOME_PARA_MIN + 42)	//42--Similarity angle	
#define DOME_PARA_MAX										(DOME_PARA_MIN + 43)		
//
#define DOME_PARA_PRESET_CALL								2						//Preset call
//CMD
#define COMMAND_ID_MIN									0
#define COMMAND_ID_ITS_FOCUS					(COMMAND_ID_MIN + 1)		//Five-point focused camera control protocol
#define COMMAND_ID_MODIFY_CGF_FILE				(COMMAND_ID_MIN + 2)		//Set the value of any field in the configuration file 
#define COMMAND_ID_AUTO_FOCUS					(COMMAND_ID_MIN + 3)		//auto focus
#define COMMAND_ID_SAVECFG						(COMMAND_ID_MIN + 4)		//save param
#define COMMAND_ID_DEFAULT_PARA					(COMMAND_ID_MIN + 5)		//default para
#define COMMAND_ID_DIGITAL_CHANNEL				(COMMAND_ID_MIN + 6)		//digital channel
#define COMMAND_ID_DELETE_FOG					(COMMAND_ID_MIN + 7)		//delete fog
#define COMMAND_ID_QUERY_REPORT					(COMMAND_ID_MIN + 8)		//query report form
#define COMMAND_ID_DEV_LASTERROR				(COMMAND_ID_MIN + 9)		//device last error
#define COMMAND_ID_TEST_HTTP					(COMMAND_ID_MIN + 10)		//test http 
#define COMMAND_ID_HOTBACKUP_REQUESTSYNC		(COMMAND_ID_MIN + 11)		//hot banckup device request sync
#define COMMAND_ID_HOTBACKUP_RECORDFILE			(COMMAND_ID_MIN + 12)		//hot banckup device record file info
#define COMMAND_ID_HOTBACKUP_SYNCFINISH			(COMMAND_ID_MIN + 13)		//hot banckup device sync file finish
#define COMMAND_ID_BATTERYINFO					(COMMAND_ID_MIN + 14)		//Battery Info
#define COMMAND_ID_15							(COMMAND_ID_MIN + 15)
#define COMMAND_ID_PLAY_AUDIO_SAMPLE			(COMMAND_ID_MIN + 16)
#define COMMAND_ID_CANCEL_EMAIL					(COMMAND_ID_MIN + 17)
#define COMMAND_ID_BIGPACK_UPGRADE				(COMMAND_ID_MIN + 18)
#define COMMAND_ID_SEARCH_EXDEV					(COMMAND_ID_MIN + 19)		//search exter device
#define COMMAND_ID_GET_SEARCHEXDEV_COUNT		(COMMAND_ID_MIN + 20)		//search exter device count
#define COMMAND_ID_GET_EXDEVLIST				(COMMAND_ID_MIN + 21)		//get exter device list
#define COMMAND_ID_GET_EXDEVLIST_COUNT			(COMMAND_ID_MIN + 22)		//get exter device list count
#define COMMAND_ID_GET_RECORDINGAUDIOLIST		(COMMAND_ID_MIN + 23)		//get recording audio list
#define COMMAND_ID_GET_RECORDINGAUDIOLIST_COUNT	(COMMAND_ID_MIN + 24)		//get recording audio count
#define COMMAND_ID_SET_TRACKING_RATE			(COMMAND_ID_MIN + 25)		//set tracking rate
#define COMMAND_ID_TRACKING_RATE				COMMAND_ID_SET_TRACKING_RATE//tracking rate
#define COMMAND_ID_PWD_VERIFY					(COMMAND_ID_MIN + 26)		//user and password verify
#define COMMAND_ID_SEEK_NVSS					(COMMAND_ID_MIN + 27)		//seek nvss
#define COMMAND_ID_XCHG_IP						(COMMAND_ID_MIN + 28)		//chang nvss ip
#define COMMAND_ID_FILE_MAP						(COMMAND_ID_MIN + 29)		//file map
#define COMMAND_ID_GET_AUDIO					(COMMAND_ID_MIN + 30)		//Get Audio Value
#define COMMAND_ID_PLATE_LIST_QUERY				(COMMAND_ID_MIN + 31)		//plate list query
#define COMMAND_ID_PLATE_LIST_MODIFY			(COMMAND_ID_MIN + 32)		//plate list modify
#define COMMAND_ID_DISK_PART					(COMMAND_ID_MIN + 33)		//disk part
#define COMMAND_ID_BADBCLOCK_SIZE				(COMMAND_ID_MIN + 34)		//disk bad block size
#define COMMAND_ID_BADBCLOCK_TEST				(COMMAND_ID_MIN + 35)		//disk bad block test
#define COMMAND_ID_BADBCLOCK_TEST_STATE			(COMMAND_ID_MIN + 36)		//disk bad block test state
#define COMMAND_ID_BADBCLOCK_BLOCK_STATE		(COMMAND_ID_MIN + 37)		//disk bad block block state
#define COMMAND_ID_BADBCLOCK_ACTION				(COMMAND_ID_MIN + 38)		//disk bad block action
#define COMMAND_ID_FILE_INPUT					(COMMAND_ID_MIN + 39)		//file input
#define COMMAND_ID_FILE_OUTPUT					(COMMAND_ID_MIN + 40)		//file output
#define COMMAND_ID_FILE_CONVERT					(COMMAND_ID_MIN + 41)		//file convert
#define COMMAND_ID_FILE_TRANSPORT				(COMMAND_ID_MIN + 42)		//file transport
#define COMMAND_ID_GET_PTZ						(COMMAND_ID_MIN + 43)	
#define COMMAND_ID_ASENSOR_CORRECT				(COMMAND_ID_MIN + 44)	
#define COMMAND_ID_REPORT_QUERY					(COMMAND_ID_MIN + 45)	
#define COMMAND_ID_HEATMAP_GET					(COMMAND_ID_MIN + 46)	
#define COMMAND_ID_PORT_MAP						(COMMAND_ID_MIN + 47)	
#define COMMAND_ID_NTP_TEST						(COMMAND_ID_MIN + 48)	
#define COMMAND_ID_QUERY_AUTOTEST_RESULT		(COMMAND_ID_MIN + 49)	
#define COMMAND_ID_FTP_DOWNLOAD_VIDEO			(COMMAND_ID_MIN + 50)	
#define COMMAND_ID_UPGRADE_ERROR_INFO			(COMMAND_ID_MIN + 51)	
#define COMMAND_ID_GET_DEV_MAC					(COMMAND_ID_MIN + 52)	
#define COMMAND_ID_UPGRADE_FINISH_INFO			(COMMAND_ID_MIN + 53)	
#define COMMAND_ID_CALIBRATE_STATUS				(COMMAND_ID_MIN + 54)	
#define COMMAND_ID_SET_CALIBRATE				(COMMAND_ID_MIN + 55)	
#define COMMAND_ID_CALIBRATE_VCA_PARA			(COMMAND_ID_MIN + 56)	
#define COMMAND_ID_3D_POSITION					(COMMAND_ID_MIN + 57)
#define COMMAND_ID_SYSTEM_TEMPERATURE			(COMMAND_ID_MIN + 58)
#define COMMAND_ID_SYSTEM_FAN_NUM				(COMMAND_ID_MIN + 59)
#define COMMAND_ID_SYSTEM_FAN_SPEED				(COMMAND_ID_MIN + 60)
#define COMMAND_ID_VCAFPGA_QUERYINFO			(COMMAND_ID_MIN + 61) 
#define COMMAND_ID_CCM_CALIBRATE				(COMMAND_ID_MIN + 62)
#define COMMAND_ID_DDNSTEST						(COMMAND_ID_MIN + 64)
#define COMMAND_ID_VCA_BIND						(COMMAND_ID_MIN + 65)
#define COMMAND_ID_CLOUD_DETECT					(COMMAND_ID_MIN + 66)
#define COMMAND_ID_CLOUD_UPGRADE				(COMMAND_ID_MIN + 67)
#define COMMAND_ID_CLOUD_DOWNLOAD				(COMMAND_ID_MIN + 68)
#define COMMAND_ID_CLOUD_UPGRADEPRO				(COMMAND_ID_MIN + 69)  
#define COMMAND_ID_USER_FINDPSW					(COMMAND_ID_MIN + 70)  
#define COMMAND_ID_USER_SECURITYCODE			(COMMAND_ID_MIN + 71)  
#define COMMAND_ID_USER_RESERVEPHONE			(COMMAND_ID_MIN + 72)  
#define COMMAND_ID_SET_PTZ						(COMMAND_ID_MIN + 73)	
#define COMMAND_ID_VENCPARAM_REFRESH			(COMMAND_ID_MIN + 74)
#define COMMAND_ID_PLATFORM_GAUGE				(COMMAND_ID_MIN + 75)
#define COMMAND_ID_DETECT_AREA                  (COMMAND_ID_MIN + 76)
#define COMMAND_ID_SELFTEST						(COMMAND_ID_MIN + 77)
#define COMMAND_ID_NETTEST						(COMMAND_ID_MIN + 78)
#define COMMAND_ID_GAUGECALIBRATE               (COMMAND_ID_MIN + 79)
#define COMMAND_ID_AUTO_VISION                  (COMMAND_ID_MIN + 80)
#define COMMAND_ID_QUERYFLOW                    (COMMAND_ID_MIN + 81)
#define COMMAND_ID_DETECTAREAPOSEX              (COMMAND_ID_MIN + 82)
#define COMMAND_ID_SCENEHEIGHT                  (COMMAND_ID_MIN + 83)
#define COMMAND_ID_SINGLEPIC                    (COMMAND_ID_MIN + 84)
#define COMMAND_ID_SHDB_APPREPAIRSYS			(COMMAND_ID_MIN + 85)
#define COMMAND_ID_SHDB_SERVICETYPE				(COMMAND_ID_MIN + 86)
#define COMMAND_ID_SHDB_TESTMAINTAIN			(COMMAND_ID_MIN + 87)
#define COMMAND_ID_SHDB_CHECKMANAGE				(COMMAND_ID_MIN + 88)
#define COMMAND_ID_XCHG_IPV6					(COMMAND_ID_MIN + 89)	//chang nvss ipv6
#define COMMAND_ID_3D_LOCATE_V5					(COMMAND_ID_MIN + 90)	//3D Locate
#define COMMAND_ID_TRANSPARENTCHANNELCONTROL_V5	(COMMAND_ID_MIN + 91)	//Transparent Channel Control
#define COMMAND_ID_GET_PTZEX                    (COMMAND_ID_MIN + 92)
#define COMMAND_ID_EASYDDNS_LINKSTATE           (COMMAND_ID_MIN + 93)	//Get the connection status between the device and the EasyDDNS
#define COMMAND_ID_WATER_SPEED_FIELD            (COMMAND_ID_MIN + 94)	//Water speed field enable
#define COMMAND_ID_AUTOTESTMULT                 (COMMAND_ID_MIN + 95)	//Autotest mult
#define COMMAND_ID_RADAR_INFO                   (COMMAND_ID_MIN + 96)	//Get radar info
#define COMMAND_ID_VIRTUALGAUGEDIS				(COMMAND_ID_MIN + 97)
#define COMMAND_ID_TO_DEFAULT_PARA				(COMMAND_ID_MIN + 98)	//Common default para
#define COMMAND_ID_WATER_SPEED_UPDATE			(COMMAND_ID_MIN + 99)	//Water speed dete update
#define COMMAND_ID_CALIBRATE_CHECK			    (COMMAND_ID_MIN + 100)	//Set/Get Calibrate check result
#define COMMAND_ID_WIRELESS_SILENT			    (COMMAND_ID_MIN + 101)	//Set/Get Wireless Silent time
#define COMMAND_ID_GETSECRE                     (COMMAND_ID_MIN + 102)  //Get Ali Secret Info
#define COMMAND_ID_SETSECRE                     (COMMAND_ID_MIN + 103)  //Set/Get Ali Secret Info
#define COMMAND_ID_TALKREFUSE                   (COMMAND_ID_MIN + 104)  //Set talk refuse
#define COMMAND_ID_WATERDETECTPOS				(COMMAND_ID_MIN + 105)	//Set Water DetectPos
#define COMMAND_ID_SYNC_SETPTZ					(COMMAND_ID_MIN + 106)	//Synchronized blocking interface to set absolute coordinate of ball ipc
#define COMMAND_ID_SYNC_GETPTZ					(COMMAND_ID_MIN + 107)	//Synchronized blocking interface to get absolute coordinate of ball ipc
#define COMMAND_ID_SYNC_GETDEVICEPARAM			(COMMAND_ID_MIN + 108)	//Synchronized blocking interface to get device param
#define COMMAND_ID_SYNC_GETCHANNELPARAM			(COMMAND_ID_MIN + 109)	//Synchronized blocking interface to get channel param
#define COMMAND_ID_SETCKDKEY                    (COMMAND_ID_MIN + 110)  //Set CKD Secret Info
#define COMMAND_ID_GETCKDKEY                    (COMMAND_ID_MIN + 111)  //Get CKD Secret Info
#define COMMAND_ID_MAX							(COMMAND_ID_MIN + 112)   
//Used By CmdConfig
#define CMD_CONFIG_MIN							0
#define CMD_SNAP_SHOT							(CMD_CONFIG_MIN + 1)
#define CMD_DORMANCY_SET						(CMD_CONFIG_MIN + 2)
#define CMD_TRAFFICFLOW_QUERY					(CMD_CONFIG_MIN + 3)
#define CMD_NORTH_ANGLE							(CMD_CONFIG_MIN + 4)
#define CMD_VISUAL_RANGE						(CMD_CONFIG_MIN + 5)
#define CMD_WIFI_APCLIENTLIST					(CMD_CONFIG_MIN + 6)			// 查询WIFI设备列表
#define CMD_WIFI_SEARCHAP						(CMD_CONFIG_MIN + 7)			// 查询WIFI热点
#define CMD_DATA_DIC_ITEM						(CMD_CONFIG_MIN + 8)
#define CMD_LED_DEV_OSD_PARA					(CMD_CONFIG_MIN + 9)
#define CMD_LED_DEV_OSD_PARA_LIST				(CMD_CONFIG_MIN + 10)
#define CMD_LED_DEV_PARA_OPT					(CMD_CONFIG_MIN + 11)
#define CMD_LED_DEV_PARA_QUERY					(CMD_CONFIG_MIN + 12)
#define CMD_WATERLEVEL_FLOWREF					(CMD_CONFIG_MIN + 13)
#define CMD_VERTICALLINE						(CMD_CONFIG_MIN + 14)
#define CMD_VERTICALLINE_QUERY					(CMD_CONFIG_MIN + 15)
#define CMD_QUERY_CPC_AREASTATUS				(CMD_CONFIG_MIN + 16) //set/get Area status 
#define CMD_CPC_AREA_MANUALCLEAR				(CMD_CONFIG_MIN + 17) //set/get Area Manual clear result
#define CMD_SHELL_ASK                           (CMD_CONFIG_MIN + 18) //Remote command execution
#define CMD_WSTABLEQUERY                        (CMD_CONFIG_MIN + 19) //Water Speed table Query
#define CMD_WSTABLEOPT                          (CMD_CONFIG_MIN + 20) //automatically generates flow meter data adding, editing and deleting
#define CMD_WATERLEVELFLOWREFQUERY              (CMD_CONFIG_MIN + 21) //Query the corresponding data of water level, discharge and velocity (manual flow meter)
#define CMD_IRRIGATIONRECORD                    (CMD_CONFIG_MIN + 22) //water conservancy report record query
#define CMD_HTTPPICTEST							(CMD_CONFIG_MIN + 23) //http pic test
#define CMD_GAT1400_STATUS                      (CMD_CONFIG_MIN + 24) //GAT1400 status
#define CMD_WHISTLESNAPSHOT                     (CMD_CONFIG_MIN + 25) //ITS WHISTLE SNAPSHOT
#define CMD_COMMON_PROGRESS						(CMD_CONFIG_MIN + 26) //common progress
#define CMD_NAS_TEST							(CMD_CONFIG_MIN + 27) //nas test
#define CMD_QUERY_GAUCECALIB					(CMD_CONFIG_MIN + 28) //query gauge calib
#define CMD_IRRI_CLIENT_QUERY					(CMD_CONFIG_MIN + 29) //client query Irrigation record
#define CMD_IRRI_DETECTPOS						(CMD_CONFIG_MIN + 30) //set water detect pos
#define CMD_AUTO_ADJUSTVERSION					(CMD_CONFIG_MIN + 31) //set auto adjust version
#define CMD_WATER_POSQUERY						(CMD_CONFIG_MIN + 32) //Query Water Detect Pos Query
#define CMD_GETNEW_COMMONENABLE					(CMD_CONFIG_MIN + 33) //get Commonenable in new way
#define CMD_DELETE_FILE							(CMD_CONFIG_MIN + 34) //delete file
#define CMD_CREATE_AUTHENTICATEFILE				(CMD_CONFIG_MIN + 35) //创建认证文件
#define CMD_AIRLINESPLAN						(CMD_CONFIG_MIN + 36)	//设置飞行航线生成参数/结果
#define CMD_QUERY_AIRLINESPLANINFO				(CMD_CONFIG_MIN + 37)	//查询飞行航线生成的信息
//#define CMD_MINSIZELICENSEPLATE				(CMD_CONFIG_MIN + 38)	//设置车牌最小尺寸	//ITS*OPTIM_PART2设置/获取车牌最小尺寸协议废弃了
#define CMD_SET_COMMONDATA						(CMD_CONFIG_MIN + 38)	//设置通用数据
#define CMD_PTZ_OPERATION						(CMD_CONFIG_MIN + 39)	//设置云台操作
#define CMD_GET_HUTEMINTERVAL					(CMD_CONFIG_MIN + 40)	//实时获取温湿度参数
#define CMD_VCAOFFLINE_QUERYDATA				(CMD_CONFIG_MIN + 41)	//查询人数统计离线数据
#define CMD_WATERQUALITY_ADJUST				    (CMD_CONFIG_MIN + 42)	//发送水质光谱校正命令
#define CMD_RECUP_LOADPATH						(CMD_CONFIG_MIN + 43)	//获取回补录像文件上传路径
#define CMD_RECUP_LOAD_ADD_PARAM				(CMD_CONFIG_MIN + 44)	//回补录像文件上传
#define CMD_RECUP_LOAD_ADD_PROGRESS				(CMD_CONFIG_MIN + 45)	//获取回补录像文件进度
#define CMD_RECUP_LOAD_ADD_DEL					(CMD_CONFIG_MIN + 46)	//回补录像文件删除
#define CMD_CHECK_DISKFILE						(CMD_CONFIG_MIN + 47)	//文件完整性校验
#define CMD_GDDZ_SECURITY_CLASSIFICATION		(CMD_CONFIG_MIN + 48)	//定制专用 获取密级参数
#define CMD_CHECK_AUTHENICATION					(CMD_CONFIG_MIN + 49)	//定制专用 双向认证
#define CMD_CHECK_VODFILE						(CMD_CONFIG_MIN + 50)	//VOD文件完整性校验
#define CMD_CHECK_VODTIMESPAN					(CMD_CONFIG_MIN + 51)  //VOD时间段完整性校验
#define CMD_NET_CHECK                           (CMD_CONFIG_MIN + 52)  //网络检测
#define CMD_QUERYDISK_RECORDPARA				(CMD_CONFIG_MIN + 53)  //查询硬盘的录像信息
#define CMD_ONCE_CFG_WIFI						(CMD_CONFIG_MIN + 54)  //一键配置WIFI参数
#define CMD_DUAL_USERS_AUTHENTICATION			(CMD_CONFIG_MIN + 55)	//双用户认证（ZXYHDZ）
#define CMD_GET_DEVICE_TIME						(CMD_CONFIG_MIN + 56)	//获取设备时间
#define CMD_GET_ELEC_PTZF_POS					(CMD_CONFIG_MIN + 57) 	//获取当前电机位置PTZF
#define CMD_SET_ELEC_PTZF_POS					(CMD_CONFIG_MIN + 58)	//设置当前电机位置PTZF回码
#define CMD_IRRIFITTINGDISPOS					(CMD_CONFIG_MIN + 59)	//生成激光距离坐标拟合公式
#define CMD_IRRIDISPOSCOLLECTION				(CMD_CONFIG_MIN + 60)	//激光距离坐标采集数据添加、编辑、删除
#define CMD_IRRIDISPOSCOLLECTIONGET_QUERY		(CMD_CONFIG_MIN + 61)	//查询激光距离坐标采集数据
#define CMD_RECORD_SUPPLEMENT_CMDPARA			(CMD_CONFIG_MIN + 62)	//设备主动回补录像命令参数
#define CMD_RECORD_SUPPLEMENT_PROGRESS			(CMD_CONFIG_MIN + 63)	//设备主动回补录像进度查询
#define CMD_K200_START_STOP_ALARM				(CMD_CONFIG_MIN + 64)	//开始/停止报警录像（k200专用协议）
#define CMD_TARGET_START_STOP_TRANSFER			(CMD_CONFIG_MIN + 65)	//靶标检测实时数据上传开关
#define CMD_CONFIG_MAX							(CMD_CONFIG_MIN + 66)
#define CMD_REPLENISH_REC_CTRL					(CMD_CONFIG_MIN + 67)	//设备回补录像文件控制
#define CMD_IRRRECEIVERMSG						(CMD_CONFIG_MIN + 68)	//水利设备通信联系人信息的添加、编辑、删除(水利行业专用协议)
#define CMD_IRRRECEIVERMSG_GET					(CMD_CONFIG_MIN + 69)	//查询水利设备通信联系人信息(水利行业专用协议)
#define CMD_GAUGECALIBCHECK_GET					(CMD_CONFIG_MIN + 70)	//查询水尺标定参数校验结果(与PARASET*IRRI*GAUGECALIBCKDATA配合使用，发送所有待校验的标定参数后调用查询此条协议)【水利行业专用协议】
#define CMD_SUPP_REC_TASK_LIST					(CMD_CONFIG_MIN + 71)	//查询设备回补录像任务列表，查询结果参数结构体是RecSuppTaskList
#define CMD_REMOTE_CONTROL_ALARM				(CMD_CONFIG_MIN + 72)	//设置远程控制报警（水利行业专用协议）

//default para
typedef struct _tagDefaultPara
{
	int iSize;
	int iType;
}TDefaultPara, *PTDefaultPara;

//digital channel
typedef struct _tagDigitalChnBatch
{
	int iSize;
	int iType;  //0-Reserved 1-Batch removal of digital channel 2-Digital channel are disabled in bulk
	int iOpt;	//iOp:1 indicates that the digital channel batch operation is ON;iOp:0 indicates that the digital channel batch operation ends
}TDigitalChnBatch, PTDigitalChnBatch;

//Image composition mode setting
#define MAX_ITS_MERGE_TYPE	5
typedef struct __tagTITS_MergeInfo
{
	int		m_iSize;
	int		m_iMergeType;		//Composite type:1-one composite type,2-two composite type,3-three composite type
	int		m_iMergeMode;		//one composite type：0-Horizontal original image and close-up composite,1-Vertical original image and close-up composite,
								//				2-Horizontal close-up and original image composite,3-Vertical close-up and original image composite。
								//two composite type：0-Vertical composite mode,1-Horizontal composite mode
	int		m_iResizePercent;	//Image scaling
}TITS_MergeInfo, *PTITS_MergeInfo;

#define IODEVICE_NUM 3		//IO Device number
//Capture parameters before and after
#define MAX_LINK_CAMERA_ID		4
typedef struct __tagTITS_SnapShotInfo
{
	int		m_iSize;
	int		m_iRoadNo;				//Lane number
	int		m_iEnable;				//Enable
	char	m_cLinkIP[LENGTH_IPV4];	//Link camera IP
	int		m_iLinkRoadNo;			//Link camera lane ID
	int		m_iSceneID;// Scenes ID(0~15) 20140203Extended
}TITS_SnapShotInfo, *pTITS_SnapShotInfo;

//IO Link device type setting
#define MAX_IO_DEVICE_NUM	10
typedef struct __tagTITS_IOLinkInfo
{
	int		m_iSize;
	int		m_iIONo;		//IO number,0-10
	int		m_iLinkDevice;	//Device link type,0-flash,1-strobe light,2-polarizer,3- Regular light,4- frequency flash,5- red/white light control, 64- alarm output
	int		m_iDevicePulse;	//Fil light link pulse width,0us-10000us
	int		m_iAheadTime;	//output ahead time
	int		m_iWorkState;	//work state
	int		m_iDefaultState;//default state
	int		m_iFrequency;
	int		m_iDutyCycle;	// Duty cycle
	int		m_iDayNightEnable;//default:0(Night Enable) 1:Daytime enable
}TITS_IOLinkInfo, *pTITS_IOLinkInfo;

//the time peroid flash is enabled
typedef struct __tagTITS_FlashEnable
{
	int   m_iSize;
	int   m_iIndex;				//Time range number,0-7，Support up to 8 time slots
	int   m_iFlashLampEnable;	//Whether to use flash,bit0：0-Disable the flash，1-Enable the flash；bit1: 0-Disable strobe light，1-Enable strobe light；bit2: 0-Disable the polarizer，1-Enalbe the polarizer；
	int   m_iVideoLinkEnbale;	//Video link enable,Bitwise bit0:0-Not enable，1-Enalbe; bit1:0-Flash not enable，1-flash enable；bit2:0-strobe light not enalbe，1-strobe light enable；bit3:0-polarizer not enalbe，1-polarizer enable；
}TITS_FlashEnable, *pTITS_FlashEnable;

#define REBUILDINDEX_SUCCESS			0
#define REBUILDINDEX_NO_DISK			1
#define REBUILDINDEX_EXCEPTION			2

#define MAX_ITS_DEV_COMMOMINFO_TYPE		7
typedef struct tagITS_DevCommonInfo
{
	int		iSize;
	int		iType;					//Information type,0-Reserved，1-Organ code，2-Organ name，3-Capture picture string superimposed bit
	char	cCommonInfo[LEN_64];	//information content,the length dose not exceed 63 characters;when iType is 3：0-the image is superimposed，1-the picture is superimposed under the black box,2-the picture is superimposed on the black box
	int		iCapType;				//Capture type
}ITS_DevCommonInfo, *pITS_DevCommonInfo;

typedef struct tagITS_CamLocationEx
{
	int				iSize;
	ITS_CamLocation tITS_CamLocation;	//ITS_Cam information structure
	int				iDeviceType;		//Device type,Bitwise，bit0：run red light device,bit1：Tachometer device,bit2：Surveillance video,
										//bit3：Digital cameras,bit4：Bayonet monitoring,bit5：Range of speed card device；
}ITS_CamLocationEx, *pITS_CamLocationEx;

//Set the parameters of related to the hybrid trigger
#define MAX_ITS_ROADID_NUM	4
typedef struct tagITS_ComPoundParam
{
	int		iSize;
	int		iITSRoadID;					//Lane number,number range：0-3
	int		iRadarMatchTime;			//Rader speed match time,Unit：millisecond
	int		iRadarMinSpeed;				//Rader minimum speed match time value,Unit：m/h
	int		iRadarMaxSpeed;				//Rader maxmum speed value;Unit：m/h
	int		iSceneID;					//Scenes number(0~15)
}ITS_ComPoundParam, *pITS_ComPoundParam;

#define VOLUMECTRL_TYPE_MIN					0
#define VOLUMECTRL_TYPE_INPUT	(VOLUMECTRL_TYPE_MIN+1)
#define VOLUMECTRL_TYPE_OUTPUT	(VOLUMECTRL_TYPE_MIN+2)
#define VOLUMECTRL_TYPE_LINEIN	(VOLUMECTRL_TYPE_MIN+3)
#define VOLUMECTRL_TYPE_MICIN	(VOLUMECTRL_TYPE_MIN+4)
#define VOLUMECTRL_TYPE_MAX		(VOLUMECTRL_TYPE_MIN+5)

#define MAX_CRUISE_NUM	16
#define SENCE_CRUISE_TYPE_MIN						0
#define SENCE_CRUISE_TYPE_TIMER						(SENCE_CRUISE_TYPE_MIN + 1)
#define SENCE_CRUISE_TYPE_TIMERANGE					(SENCE_CRUISE_TYPE_MIN + 2)
#define SENCE_CRUISE_TYPE_TIMING_BY_TIMERANGE		(SENCE_CRUISE_TYPE_MIN + 3)
#define SENCE_CRUISE_TYPE_MAX						(SENCE_CRUISE_TYPE_MIN + 4)

#define MAX_PRAVICY_COVER_AREA_NUM	  24
#define MAX_INTIMITY_COVER_AREA_COUNT 32
typedef struct tagIntimityCover
{
	int				iBufSize;			//3D privacy shelter structure size
	int				iAreaNum;			//Area number(1~24)
	int				iMinZoom;			//Minimum zoom(1~50)
	int				iColor;				//Color(1：red 2：green 3：yellow 4：blue 5：purple 6：cyan 7：black 8：white 9:mosaic)
	int				iPointNum;			//Point amount(3~32)
	POINT			ptArea[MAX_INTIMITY_COVER_AREA_COUNT]; //Point
}IntimityCover, *pIntimityCover;

#define MAX_VCATYPE_COUNT	8
typedef struct tagAnyScene
{
	int				iBufSize;			//Scene application timed cruise template structure size
	int				iSceneID;			//Scene number(0~15)
	char			cSceneName[LEN_32];	//Scene name
	int				iArithmetic;		//enable the algorithm type( bit0: 1-action analysis algorithm enable；0-not enable
										//bit1：1-track algorithm enable；0-not enable
										//bit2：1-face detection algorithm enable；0-not enable
										//bit3：1-people amount statistics algorithm enable；0-not enable
										//bit4：1-video diagnose algorithm enable；0-not enable
	                                    //bit5：1-license plate recognition algorithm enable；0-not enable
	                                    //bit6：1-audio and video exception algorithm enable；0-not enable 
										//bit7：1-off post algorithm enable；0-not enable      
										//bit8：1-people gathering algorithm enable；0-not enable
										//bit11:1-witness protection algorithm enable；0-not enable
										//bit20:1-Structured algorithm enable；0-not enable
										//bit21: 1-procutrate duty   0-not enable
										//bit22: 1-height limit; 0-not enable
										//bit23: 1-new duty   0-not enable
										//bit24: 1-abnormal number   0-not enable
										//bit25: 1-get up   0-not enable
										//bit26: 1-leave bed   0-not enable
										//bit27: 1-hold still  0-not enable
										//bit28: 1-sleep    0-not enable
										//bit29: 1-slip up   0-not enable
										//bit30: 1-new fight  0-not enable
										//bit31: 1-arm touch   0-not enable
	int             iDevType;
	int				iArithmeticEx;		//enable the algorithm type
										//bit0：1-human detection algorithm enable 0-not enable
										//bit1 1-pept enable 0-not enable
										//bit2 1-Navigation ship detection enable 0-not enable
										//bit3 1-granary vehicles detect enable 0-not enable
										//bit4 1-chef hat detect enable 0-not enable
										//bit5 1-chef mask detect enable 0-not enable
	int             iActionType;        //0:Fixed pre-postion 1:scanlist
	int				iArithmeticArray[MAX_VCATYPE_COUNT];	
	int				iArithmeticThreeEx;
}AnyScene, *pAnyScene;

typedef struct tagSceneTimerCruise
{
	int				iBufSize;			//Scene application time cruise structure size
	int				iCruiseID;			//Cruise point number(0~15)
	int				iSceneID;			//Scene number(0~15)
	int				iEnable;			//enable(0：not enable 1：enable)
	int				iTime;				//(30~3600s)
	int				iSceneType;
	int				iDetectCarType;		//Bitwise,0-not enable 1-enable:bit0--bus,bit1--sedans,bit2--Wagon,bit3--van,bit4--heavy medium truck,bit5--light truck,bit6--Motorcycle,bit7--Pedestrian,bit8--SUV,bit9-- medium bus,bit10--Trailer,bit11--Hazardous chemicals car  
}SceneTimerCruise, *pSceneTimerCruise;

typedef struct tagTingCruiseByTimeRange
{
	int			iSceneID;			//Scene id(0~15)
	int         iRemainTime;
} TingCruiseByTimeRange, *pTingCruiseByTimeRange;

typedef struct tagSceneTimeRangeCruise
{
	int					iBufSize;			//Scene application time range cruise structure size
	int					iCruiseID;			//time range number(0~15) 
	int					iSceneID;			//scene number(0~15)
	int					iEnable;			//enable(0：not enable 1：enable)
	int					iBeginHour;			//start hours
	int					iBeginMinute;		//start minutes
	int					iEndHour;			//ends hours
	int					iEndMinute;			//ends minutes
	int					iWeekday;			//weeks(sunday to saturday :0～6）
	TingCruiseByTimeRange        tInfo[MAX_CRUISE_NUM];	//remain time 0~15
	int					iDetectCarType;		//Bitwise,0-not enable 1-enable:bit0--bus,bit1--sedans,bit2--Wagon,bit3--van,bit4--heavy medium truck,bit5--light truck,bit6--Motorcycle,bit7--Pedestrian,bit8--SUV,bit9-- medium bus,bit10--Trailer,bit11--Hazardous chemicals car  
}SceneTimeRangeCruise, *pSceneTimeRangeCruise;

typedef struct tagTingCruiseByTimeRangeEx
{
	int			iSceneType;
	int			iSceneID;			
	int         iRemainTime;
} TingCruiseByTimeRangeEx, *pTingCruiseByTimeRangeEx;

typedef struct tagSceneTimeRangeCruiseEx
{
	int							iBufSize;
	int							iCuriseType;		//0-TimerCruise 1-TimeRange 2-Cruise in TimeRange
	int							iCruiseID;			

	int							iSceneType;//0-vca 1-alert
	int							iSceneID;			
	int							iEnable;			
	int							iBeginHour;			
	int							iBeginMinute;		
	int							iEndHour;			
	int							iEndMinute;			
	int							iWeekday;		

	int							iTimeRangeNum;
	int							iTotalSize;
	TingCruiseByTimeRangeEx*    ptInfo;
	int							iDetectCarType;		//Bitwise,0-not enable 1-enable:bit0--bus,bit1--sedans,bit2--Wagon,bit3--van,bit4--heavy medium truck,bit5--light truck,bit6--Motorcycle,bit7--Pedestrian,bit8--SUV,bit9-- medium bus,bit10--Trailer,bit11--Hazardous chemicals car  
}SceneTimeRangeCruiseEx, *pSceneTimeRangeCruiseEx;

typedef struct tagTrackArithmetic
{
	int				iBufSize;			//Track algorithm structure size
	int				iSceneID;			//Scene id(0~15)
	int				iZoomRate;			//Zoom Rate(10~360,1~36times，accurate to 0.1)
	int				iMaxFallowTime;		//maximum track time(0~300 seconds)
	int				iHeight;			//Dome height
	int				iDesStopTime;		//target tatic tracking time(2~600 seconds,default 8 seconds)
	int				iEnable;			//Enable flag(bitwise identification：1-enable，0-not enable
										//0 bit：whether or not returns when face is detected
										//1 bit：whether use anti-occlusion function
										//2 bit：whether enable limits
										//3 bit：whether display the track box
	int				iMinSize;			//min size,range[0, 100],default 20
	int				iMaxSize;			//max size,range[0, 100]
	int             iTargetTypeCheck;   //distinguish huaman,car
	int             iCallSimilarVision; //Call Similar Vision:0:Not Call, 1,Call
}TrackArithmetic, *pTrackArithmetic;

typedef struct tagTrackAstrictLocation
{
	int				iBufSize;			//Track static location structure size
	int				iSceneID;			//Scene id(0~15)
	int				iType;				//Limit type(1:upper limit。2:lower limit 3:left limit 4:right limit)
}TrackAstrictLocation, *pTrackAstrictLocation;

#define MANUALCAP_TYPE_TRACK_LOCKED		 1  //Manual track lock
#define MANUALCAP_TYPE_SNAPSHOT			 2	//Manual sanppe
#define MANUALCAP_TYPE_PRE_TRACK		 3  //Manual prepare to track

#define PUSHMODE_SPEED_FIRST	1  //iPushMode Speed first
#define PUSHMODE_QUALITY_FIRST	2  //iPushMode Quality first
#define PUSHMODE_CUSTOM			3  //iPushMode custom
#define PUSHMODE_TIMING			4  //iPushMode Timing

#define MAX_FACE_DETECT_AREA_COUNT 32
#define MAX_FACE_ATTR_ALARM_COUNT  32
#define FACE_ATTR_ALARM_WEARMASK    0
#define FACE_ATTR_ALARM_NOWEARMASK  1
#define FACE_ATTR_ALARM_HELMET      2
#define FACE_ATTR_ALARM_NOHELMET    3

typedef struct tagFaceDetectArithmetic
{
	int				iBufSize;			//Face detection algorithm structure size
	int				iSceneID;			//Scene id(0~15)
	int				iMaxSize;			//maximum face size(0~100 percentage of image width,100 indicates the width of entire screen)
	int				iMinSize;			//minimum face size(0~100 percentage of image width，100 indicates the width of entire screen)
	int				iLevel;				//Algorithm run level(0~5)
	int				iSensitiv;			//Sensitivity(0~5)
	int				iPicScale;			//Picture scale(1~10)
	int				iSnapEnable;		//Face snap enable(1-enable，0-not enable)
	int				iSnapSpace;			//Snap space(space frame count)
	int				iSnapTimes;			//Snap times(1~10)
	int				iPointNum;			//polygon area vertex number(3～32)
	POINT			ptArea[MAX_FACE_DETECT_AREA_COUNT]; //polygon area vertex point
	int				iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
	int				iExposureBright;	//exposure light strength
	int				iDisplayRule;		//0-not display 1-display
	int				iMinSizeEx;			//minimum face size(0~10000 percentage of image width，10000 indicates the width of entire screen)
	int				iMaxSizeEx;			//maximum face size(0~10000 percentage of image width,10000 indicates the width of entire screen)
	int				iPushMode;			//push mode 0:reserved 1:Speed first 2:Quality first  3:custom	4:timing 5: Entrance guard(continuous) 6: reserved 7: channel
	int				iPushLevel;			//push level effect when push_mode == 3;(0:reserved,1:fast 2:normal 3:slow) effect when push_mode == 4;(value is timing time)
	int				iSnapMode;			//snap mode (0:reserved 1:snap all 2:snap high quality 3:custom)
	int				iSnapLevel;			//Snap level effect when Snap_mode == 3;(0~100)
	int				iDentification;		//Face recognition algorithm switch: 0-not supported, 1-off, 2-on
	int             iDevType;			//DevType:0-IPC，1-NVR
	int             iQpvalueBig;		//ST Customized, background image quality, range 1~100, 0 means not using this setting.			
	int             iQpvalueSmall;		//ST Customized, face thumbnail quality, range 1~100, 0 means not using this setting.						
	int          	iAlgSnapMode;		//ST Customized, algorithmic capture mode, 0-face, 1-vehicle, 2-mix.
	int             iPlateMinSize;		//ST Customized, the image width is very divided, the range is 1~10000, 10000 means the width of the whole screen. When this field is 0, it means no processing.
	int				iDelayTime;			//Valid when iPushMode == 2. 500ms,1000ms,2000ms
	int				iTimeSpace;			//Push Pic TimeSpace. 100ms,200ms,300ms,500ms,1000ms,2000ms
	int				iFaceAttrAlarm[MAX_FACE_ATTR_ALARM_COUNT];		//Bit enable: 1 - enable 0 - not enable bit0 - wear mask Bit1 - not wearing mask bit2 - wearing helmet bit3 - not wearing helmet
	int				iDelayPushSpan;		//Extend push interval 2s,5s,10s,20s,30s
}FaceDetectArithmetic, *pFaceDetectArithmetic;

typedef struct tagPersonStatisticArithmetic
{
	int				iBufSize;			//People statistic algorithm structure size
	int				iSceneID;			//Scene id(0~15)
	int				iMode;				//Mode(1-vertiacl people statistic 2-horizontal people statistic  3-cpc area  4-drawline people statistic 5-queue people statistic )
	int				iTargetMaxSize;		//target maximum size(0~100  percentage of image width，100 indicates the width of entire screen)
	int				iTargetMinSize;		//target minimum size(0~100 percentage of image width，100 indicates the width of entire screen)
	int				iSensitiv;			//Sensitivity(0~5)
	int				iDetectType;		//Detect type(1-detect area 2-detect line)
	int				iPointNum;			//Number of trip wire 1 vertices (2～32). At present, it supports up to 4 points.
	POINT			ptArea[MAX_FACE_DETECT_AREA_COUNT]; //trip wire 1 vertex point
	vca_TPolygonEx	stRegion;			//polygon area vertex number and Points. At present, it supports up to 8 points.
	int				iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
	int				iMinSizeEx;			//1-10000 image width ratio, 10000 represents the width of the whole screen. Do not process when this field is 0
	int				iStayNum;			//Current population, 0-100000
	int				iClearMode;			//Zero mode, 0: not supported, 1: daily, 2: never
	int				iMaxNum;			//Maximum number of people allowed, 1-100000
	int				iHour;				//Clear time, hours, 0-23
	int				iMinute;			//Clear time, minutes, 0-59
	int				iEnable;			//0: not enabled, 1 enabled
}PersonStatisticArithmetic, *pPersonStatisticArithmetic;

typedef struct tagTrackZoomX
{
	int				iBufSize;			//Track zoom structure size
	int				iSceneID;			//Scene id(0~15)
	int				iRate;				//Track zoom rate(10~360[accuracy 0.1])
}TrackZoomX, *pTrackZoomX;

#define MAX_COVER_ALARM_BLOCK_COUNT 8
#define MAX_COVER_ALARM_AREA_COUNT 32
typedef struct tagCoverAlarmArea
{
	int				iBufSize;			//video cover alarm area structure size
	int				iBlockNo;			//Block ID(1~8)
	int				iAlarmTime;			//Alarm time
	int				iPointNum;			//polygon area vertex number(3～32)
	POINT			ptArea[MAX_COVER_ALARM_AREA_COUNT]; //polygon area vertex point
}CoverAlarmArea, *pCoverAlarmArea;

//Record time parameter
typedef struct tagNVS_FILE_TIME_V1
{
	unsigned short iYear;   /* Year */
	unsigned short iMonth;  /* Month */
	unsigned short iDay;    /* Day */
	unsigned short iHour;   /* Hour */
	unsigned short iMinute; /* Minute */
	unsigned short iSecond; /* Second */
	unsigned short iMillisecond; /* Millisecond */
} NVS_FILE_TIME_V1,*PNVS_FILE_TIME_V1;

typedef struct tagITS_ILLEGALRECORD_V1
{
	int	 iBufSize;			//Mix trigger structure size 
	long lCarID;
	int  iRecordFlag;
	int  iIllegalType;
	int  iPreset;
	char cCrossingID[LEN_64];		 //Crossing id 64 characters
	char cLaneID[LEN_64];			 //Lane id	64 characters
	NVS_FILE_TIME_V1 m_tSnapTime;//Snap begin time(the first picture snap time)
}ITS_ILLEGALRECORD_V1,*pITS_ILLEGALRECORD_V1;

//Intelligent analysis overlays parameters and colors
typedef struct tagVCATargetParam
{
	int		iBufSize;						//Overlays parameters and color structure size
	int		iSceneID;						//Scene id(0~15)
	int		iDisplayTarget;					//whether the video overlays the target(0：not overlay 1：overlay)
	int		iDisplayTrace;					//whether the video overlays the track(0：not overlay 1：overlay)
	int		iTargetColor;					//Target and track color(0::By the program automatically set the colot, each target a color)
											//1：red 2：green 3：yellow 4：blue 5：purple 6：cyan 7：black 8：white)
	int		iTargetAlarmColor;				//The color of the target and trace at the time of the alarm(1：red 2：green 3：yellow 4：blue 5：purple 6：cyan 7：black 8：white)
	int		iTraceLength;					//Trace length(0～40)
}VCATargetParam,*pVCATargetParam;	

//Intellient analysis advanced parameters
typedef struct tagVCAAdvanceParam
{
	int		iBufSize;					//Advance parameters structure size
	int		iSceneID;					//Scene id(0~15)
	int		iTargetEnable; 				//whether enable advanced parameters
	int		iTargetMinSize; 			//Target minimum pixel size
	int		iTargetMaxSize; 			//Target maximum pixel size
	int   	iTargetMinWidth;  			//Maximum width
	int		iTargetMaxWidth;  			//Minimum width
	int		iTargetMinHeight;  			//Maximum height
	int		iTargetMaxHeight;  			//Minimum height
	int     iTargetMinSpeed;			//Target mimimum pixel speed(-1 is not restricted )
	int     iTargetMaxSpeed;			//Target maximum pixel speed(-1 is not restricted )
	int 	iTargetMinWHRatio;			//Target minimum width and height ratio ×100	（height/width）×100
	int 	iTargetMaxWHRatio;			//Target maximum width and height ratio ×100	（height/width）×100
	int		iSensitivity;				//Target output sensitivity level(0: low sensitivity 1：middle sensitivity 2：high sensitivity)
	int		iForegroundMinDiff;			//Foreground minimum difference(4~100)
	int		iForegroundMaxDiff;			//Foreground maximum difference(4~100)
	int		iBackUpdateSpeed;			//Background update speed(1~10)
	int		iRealTargetTime;			//Number of target confirmation frames(10~30，default:16)
	int		iBlendBackTime;				//Blend background time(number of frame, range)
	int		iTarMergeSensitiv;			//Target merge sensitivity(0~100)
	int		iTarCheckSensitiv;			//Target check sensitivity(1~100) default 40
}VCAAdvanceParam,*pVCAAdvanceParam;	

//Intelligent analysis rules parameters
typedef struct tagVCARuleParam
{
	int					iBufSize;							//overlay parameters and color structure size
	VCARule				stRule;								//rules general parameters
	char				cRuleName[VCA_MAX_RULE_NAME_LEN];	//rules name
	int					iEventID;							//current event ID
}VCARuleParam,*pVCARuleParam;

//Intelligent analysis alarm count statistic clear up
typedef struct tagVCAAlarmStatisticClear
{
	int		iBufSize;					//Alarm count statistic structure size
	int		iSceneID;					//Scene id(0~15)
	int		iRuleID;					//rules ID(0~7)
	int		iEventType;					//Event type(0：single trip line 1：double trip line 2：perimeter detection 3：wangdering 4：parking 5：running 6：density of people in the area 7：stolen goods 
										//8：abandoned 9：face recognition 10：video diagnosis   11：intelligent track  12：flow statistic 13：people gathering 14：detachment detecion)
}VCAAlarmStatisticClear,*pVCAAlarmStatisticClear;

typedef struct tagCallParam
{
	int		iBufSize;					//Call gengeral parameters structure size
	int		iType;						//0-Call Scene ID；1-Call privacy shelter location 2- call vca scene 3 - enable intelligent scene quickly 4-quickly start warning scene, 5-fire detection shielding area, 6-smoke detection shielding area, 7-temperature difference detection shielding area
	int		iValue;						//Scene ID range：0~15； privacy shelter location range：0~23
	int		iSceneType;					//0-vca 1-alert
}CallParam, *pCallParam;


// Anxiety test/scene change
typedef struct tagVCA_TRuleParam_VideoDiagnose
{
	int  iSize;   		  // struct size
	int  iChannelNo ;	  // channel No.
	int  iSceneId ;		  // scene ID
	int  iRuleID ;		  // rule ID
	int  iDisplayStat ;	  // display alarm number or not
	int  iType ;		  // arithmetic enable type
	int  iEnable ;		  // enable type
	int  iLevel ;		  // level
	int  iTime ; 		  // test time
}VCA_TRuleParam_VideoDiagnose, *pVCA_TRuleParam_VideoDiagnose;

//Audio Exception
typedef struct tagVCA_TRuleParam_AudioDiagnose
{
	int  iSize;   			// struct size
	int  iChannelNo ;		// channel No.
	int  iSceneId ;			// scene ID
	int  iRuleID ;		    // rule ID
	int  iDisplayStat ;	    // display alarm number or not
	int  iType ;		    // arithmetic enable type
	int  iEnable ;		    // enable type
	int  iLevel ;		    // level
}VCA_TRuleParam_AudioDiagnose, *pVCA_TRuleParam_AudioDiagnose;

//VCA Tripwire for 300W
typedef struct tagVCA_TRuleParam_Tripwire
{
	int					iBufSize;
	VCARule				stRule;
	int					iTargetTypeCheck;
	vca_TDisplayParam	stDisplayParam;			
	int					iTripwireType;			//0: unidirectional, 1: bidirectional
	int					iTripwireDirection;		//(0～359°)
	int					iMinSize;				//[0, 100] init 5
	int					iMaxSize;				//[0, 100] init 30
	vca_TPolygonEx		stRegion1;				//Line1 Point Num and Points
	int					iDisplayTarget;			//display target box, 0-not dispaly, 1-dispaly
}VCA_TRuleParam_Tripwire,*pVCA_TRuleParam_Tripwire;
//add end

//Intelligent analysis single trip line parameters
typedef struct tagVCATripwire
{
	int					iBufSize;				//Trip line structure size
	VCARule				stRule;					//Rules general parameters
	int					iTargetTypeCheck;		//Target type limit(0：not distinguish 1：distinguish between people 2：distinguish between cars 3：distinguish between people and cars)
	int					iMinDistance;			//Minimum distance(the target travel distance must be greater than this threshold)
	int					iMinTime;				//Minimum time(the target travel distance must be greater than this threshold)
	int					iTripwireType;			//Trip wire type(0: single, 1: double)
	int					iTripwireDirection;		//Forbid trip wire direction(0～359 degree)
	vca_TDisplayParam	stDisplayParam;			//Display parameters
	vca_TLine			stLine;					//Trip wire point
	int					iDisplayTarget;			//display target box, 0-not dispaly, 1-dispaly
}VCATripwire,*pVCATripwire;

//Intelligent analysisi double trip wire parameters
typedef struct tagVCADbTripwire
{
	int					iBufSize;
	VCARule				stRule;
	int					iTargetTypeCheck;
	vca_TDisplayParam	stDisplayParam;			
	int					iTripwireType;			//0: unidirectional, 1: bidirectional
	int					iDirTripwire1;			//tripwire 1 direction(0～359°)
	int					iDirTripwire2;			//tripwire 2 direction(0～359°)
	int					iMinDBTime;
	int					iMaxDBTime;
	int					iMinSize;				//[0, 100] default 5
	int					iMaxSize;				//[0, 100] default 30
	vca_TPolygonEx		stRegion1;				//Line1 Point Num and Points
	vca_TPolygonEx		stRegion2;				//Line2 Point Num and Points
	int					iDisplayTarget;			//display target box, 0-not dispaly, 1-dispaly
}VCADbTripwire,*pVCADbTripwire;

//Intelligent analysis perimeter parameters
typedef struct tagVCAPerimeter
{
	int					iBufSize;			//Perimeter structure size
	VCARule				stRule;				//Rule general parameters
	int					iTargetTypeCheck;	//Distinguish target type(0：not distinguish 1：distinguish between people 2：distinguish between cars 3：distinguish between people and cars)
	int 				iMode;				//Perimeter detection mode(0:invade 1:entrance 2:leave)
	int					iMinDistance;		//Minimum distance 
	int					iMinTime;			//Minimum time
	int					iType;				//Whether to do direction to judge
	int					iDirection;			//Forbid direction angle
	vca_TDisplayParam	stDisplayParam;		//Display parameters
	vca_TPolygon		stRegion;			//Area region	
	int  				iMiniSize;			//Min Size(range:0~100, default:5)
	int  				iMaxSize;			//Max Size(range:0~100, default:30)
	int					iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
	int                 iNoAlarmMode;       //Alarm Elimination mode(0: not supported 1: clear the alert when leaving the video area 2: clear the alert when leaving the detection. default: alert when leaving the video area) 
    int                 iClimbWall;         //翻墙模式	0：通用模式 1：翻墙模式
    int                 iClimbMiniSize;       //最小尺寸	[0, 100] 默认 2（翻墙模式使用）
}VCAPerimeter,*pVCAPerimeter;

//Intelligent analysis wandering parameters
typedef struct tagVCALinger
{
	int					iBufSize;			//Wandering structure size
	VCARule				stRule;				//Rule general parameters
	int					iMinTime;			//Wandering the minimum time(5000~600000 millisecond，default value：10000)
	int					iSensitivity;		//Sensitivity(0~100)
	int					iMinRange;			//Wandering minimum distance(0~100 the percentage of image width，100 indicates the width of entire screen)
	vca_TDisplayParam	stDisplayParam;		//Display parameters
	vca_TPolygon		stRegion;			//Area region
	int					iMinBoundy;			//circumscribed polygon area(range:0~100, default:5)
	int					iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
}VCALinger,*pVCALinger;	

//Intelligent analysis parking parameters
typedef struct tagVCAParking
{
	int					iBufSize;			//Parking structure size
	VCARule				stRule;				//Rule general parameters
	int					iMinTime;			//The sustain time of triggering the parking alarm(0~30000 milliseonds，default value：5000)
	vca_TDisplayParam	stDisplayParam;		//display parameters
	vca_TPolygon		stRegion;			//Area region
	int					iMiniSize;			//Min Size(range:0~100, default:5)
	int					iMaxSize;			//Max Size(range:0~100, default:30)
	int					fThVelocity;		//threshold value(Accurate to one decimal places, *10 before to send the network protocol)
	int					iDisplayTarget;			//display target box, 0-not dispaly, 1-dispaly
}VCAParking,*pVCAParking;	

//VCA Leave Detect   
typedef struct tagVCALeaveDetect
{
	int					iBufSize;
	VCARule				stRule;
	vca_TDisplayParam	stDisplayParam;			
	int					iLeaveAlarmTime;		
	int					iRuturnClearAlarmTime;	//Clear Alarm Time for People return
	int					iAreaNum;				//Area Num
	vca_TPolygon		stRegion1[MAX_AREA_NUM];//Area Point Num and Point	
	int					iDutyNum;				//[0, 5] init 2
	int					iMinSize;				//[0, 100] init 3
	int					iMaxSize;				//[0, 100] init 15
	int					iSensitivity;
	int					iDisplayTarget;			//display target box, 0-not dispaly, 1-dispaly
}VCALeaveDetect,*pVCALeaveDetect;

//Intelligent analysis running parameters
typedef struct tagVCARunning
{
	int					iBufSize;			//Running structure size
	VCARule				stRule;				//Rule general parameters
	int					iMaxDistance;		//the maximum distance of people running(0.01×100～1.00×100)
	int					iMinDistancePerSec;	//the minimum moving distance per second(0~100 the percentage of image width，100 indicate the width of the entire screen)
	vca_TDisplayParam	stDisplayParam;		//Display parameters
	vca_TPolygon		stRegion;			//Area region
	int  				iMiniSize;			//Min Size(range:0~100, default:5)
	int  				iMaxSize;			//Max Size(range:0~100, default:30)
	int					iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
	int					iTargetTypeCheck;	//Target type, 0：not distinguish 1：distinguish between people 2：distinguish between car 3：distinguish between people and car
}VCARunning,*pVCARunning;	

//VCA Face Mosaic
typedef struct tagVCAFaceMosaic 
{
	int					iBufSize;
	VCARule				stRule;
	vca_TDisplayParam	stDisplayParam;
	int					iMinSize;				//[0, 100] 
	int					iMaxSize;				//[0, 100] 
	int					iLevel;					//[0, 5] 
	int					iSensitiv;				//[0, 5] 
	int					iPicScal;				//[1, 10] 
	vca_TPolygonEx		stRegion1;				//[3, 32]
	int					iMosaicLevel;			//Mosaic Level
	vca_TPolygonEx		stProtectRegion;		//[3, 32]
}VCAFaceMosaic,pVCAFaceMosaic;

//Intelligent analysis people crowd parameters
typedef struct tagVCACrowd
{
	int					iBufSize;			//People crowd structure size
	VCARule				stRule;				//Rule general parameters
	int					iSensitivity;		//Sensitivity(0~100)
	int					iTimes;				//Sustain time(seconds)
	vca_TDisplayParam	stDisplayParam;		//Display parameters
	vca_TPolygon		stRegion;			//Area region
	int                 iBasicValue;        //Density reference value(0~350 Unit: person)
	int                 iSceneMode;			//Size scene value(0: not supported 1: large scene 2: small scene(algorithm default large scene))
}VCACrowd,*pVCACrowd;

//Intelligent analysis abandoned /stolen goods parameters
typedef struct tagVCADerelict
{
	int					iBufSize;			//Abandoned goods/stolen goods structure size
	VCARule				stRule;				//Rule general parameters
	int					iMinTime;			//The shortest time that abandoned goods exits in the area(0~30000 milliseconds，default value：5000)
	int					iMinSize;			//Minimum pixel size(0~100 pixels，default value：10)
	int					iMaxSize;			//Maximum pixel size(100~40000 pixels，default value：10000)
	int 				iSunRegionNum;		//Sub-polygon area number
	vca_TDisplayParam	stDisplayParam;		//Display parameters
	vca_TPolygon 		stMainRegion;		//Main-polygon area 
	vca_TPolygon 		stSubRegion[VCA_MAX_OSC_REGION_NUM];	//Sub-polygon area
	int					iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
}VCADerelict,VCAStolen,*pVCADerelict,*pVCAStolen;

//Intelligent analysis river detection parameters
typedef struct tagVCARiverClean
{
	int					iBufSize;			
	VCARule				stRule;				//Rule general parameter
	vca_TDisplayParam	stDisplayParam;		//Display parameters
	int					iType;				//Mode 0:Section、river interchange drift detection 1:Dam deposits detection 2:Stopping block deposit detection	
	int					iSensitivity;		//Sensitivity Default 2	region 0-5
	int					iMinSize;			//Suspected drift. minimum size(the percentage of image width)，default value 10	ranges[8, 100]，Unit：no
	int					iMaxSize;			//Suspected drift. maximum size(the percentage of image width)，default value30	ranges[8, 100]，Unit：no
	int 				iPercentage;		//Suspected drift. accounting for(the percentage of image width) default value 10	ranges[8, 100]，Unit：no
	vca_TPolygonEx		stPoints;			//Polygon area vertex count and point
}VCARiverClean,*pVCARiverClean;

//Intelligent analysis - pirates of the theft removal parameters
typedef struct tagVCADredge
{
	int					iBufSize;			
	VCARule				stRule;				//Rule general parameters
	vca_TDisplayParam	stDisplayParam;		//Display parameters
	int					iSensitivity;		//Sensitivity. default 2	ranges 0-5
	int					iMinSize;			//Minimum size(the percentage of image width) ，default value 5	ranges[0, 100]，unit：no
	int					iMaxSize;			//Maximum size(the percentage of image width) ，default value 30	ranges[0, 100]，unit：no
	int 				iTimeMin;			//Alarm time ，default value 3	ranges[0, 3600]，unit：seconds
	vca_TPolygonEx		stPoints;			//polygon area vertex count and point
}VCADredge,*pVCADredge;

//Intelligent analysis river detection advanced parameters
typedef struct tagVCARiverAdvance
{
	int					iBufSize;			
	int					iSceneID;			//Scene id(0~15)
	int					iFGSampleNum;		//The number of modeling samples（default value is 20）
	int					iFGDistThresh;		//Distance threshold（default value is 18）
	int					iFGCountThresh;		//Count threshold（default value is 15）
	int 				iFGLifeThresh;		//List threshold（default value is 10）
}VCARiverAdvance,*pVCARiverAdvance;

typedef struct tagVCASceneRecovery
{
	int					iBufSize;
	int					iChanNo;			//channel no
	int					iSceneId;			//scene id (0~15)
	int					iRecoveryTime;		//0~3600 second（defult:10）
}VCASceneRecovery,*pVCASceneRecovery;


typedef struct tagVCAChannelEnable
{
	int iBufSize;					//structure size
	int iEnable;
	int iDevType;
}VCAChannelEnable, *pVCAChannelEnable;

typedef struct tagVCAVideoSize
{
	int iBufSize;					//structure size
	int iVideoSize;
}VCAVideoSize, *pVCAVideoSize;

//Videotape file additional information
#define MAX_FILE_APPEND_INFO_TYPE	2
typedef struct tagFileAppendInfo
{
	int		iBufSize;				//Videotape file additional information structure size
	int		iType;					//Additional information type,0-reserved 1-hash
	char    cFileName[LEN_256];		//Videotape file name
	char	cValue[LEN_256];		//iType value is 1- represents the hash value of the videotape file(character string)
}FileAppendInfo, *pFileAppendInfo;


#define VCA_OPT_SUSPEND		0
#define VCA_OPT_OPENVCA		1

#define VCA_OPT_RESULT_SUCCESS		1 
#define VCA_OPT_RESULT_CONFIGING	2

typedef struct tagVCASuspend
{
	int		iStatus;				//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
	int     iDevType;				//0-IPC, 1-NVR
}VCASuspend, *pVCASuspend;

typedef struct tagVCASuspendResult
{
	int		iBufSize;				//structure size
	int		iStatus;				//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
	int		iResult;				//Result(1:success[indicate that parameters can be set] 2:fail[indicate that parameter is being set and can not set])
	int     iDevType;				//0-IPC, 1-NVR
}VCASuspendResult, *pVCASuspendResult;

#define NVR_WORKMODE_WORKDEV			0		//nvr工作模式为工作机状态
#define NVR_WORKMODE_HOTBACKUP			1		//nvr工作模式为热备机状态
#define NVR_WORKDEV_OPT_BACKUPDEV		0		//nvr工作机添加/删除操作热备机
#define NVR_BACKUPDEV_OPT_WORKDEV		1		//nvr热备机添加/删除操作工作机
#define NVR_HOTBACKUP_ADD_HOST			0		//nvr添加主机操作（工作机添加热备机 或 热备机添加工作机）
#define NVR_HOTBACKUP_DEL_HOST			1		//nvr删除主机操作（工作机删除热备机 或 热备机删除工作机）
typedef struct tagBackupDevModify
{
	int					iBufSize;			//structure size
	int					iDevType;			//Device type(0:Machine operating hot standby machine 1:Hot standby machine operating machine)
	int					iOptType;			//Operate order(0:ADD 1:DEL)	
	char				cIP[LEN_64];		//Hot standby machine IP
	char				cUserName[LEN_64];	//Machine login username	
	char				cPassword[LEN_64];	//Machine login password	
}BackupDevModify,*pBackupDevModify;

typedef struct tagWorkDevInfo
{
	int					iBufSize;		//structure size
	int					iIndex;			//index
	char				cIP[LEN_64];	//Machine IP address	
}WorkDevInfo,*pWorkDevInfo;

typedef struct tagVirtualDisk
{
	int					iBufSize;			//Structure size
	int 				iIndex;			//Input parameters，Specifies the index value
	int					iOptType;		//Operate type
	char				cVDName[LEN_64];	//The virtual disk name
	int					iVdSize;			//The virtual disk size(The unit is M)
	int					iInitType;	//The initialization type (1:Front desk 2:Backstage 3:Fast) [New、Repair、Delete Operate is valid]
	char				cRaidName[LEN_64];	//Array name
}VirtualDisk,*pVirtualDisk;

#define MAX_RAID_DISK_NUM 24   
typedef struct tagRAIDWorkMode
{
	int		iBufSize;		             //Structure size
	int		iDiskID;		             //Disk id(SATA1~SATA8 id～,SATA9~SATA16 id ~1015)
	char	cRaidName[LEN_64];	         //Array name
	int		iWorkMode;		             //Hot spare type(0:Free disk；1:Array hot spare；2:Global hot spare；3：RAID disk；4：Invalid RAID disk；5：Faulty disk)
}RAIDDiskWorkMode, *pRAIDDiskWorkMode;
#define DISKWORKTYPE_RAIDHOTBACK	1

#define MAX_RAID_NUM 96	//Global maximum support 96 Arrays   
typedef struct tagRAIDState
{
	int					iBufSize;			//Structure size
	int 				iIndex;				//Input parameters, specifics the index value
	char				cRaidName[LEN_64];	//Array name
	int					iRaidAllSize;		//Total size(the unit is M)
	int					iRiadUseAbleSize;	//Available space(the unit is M)
	int					iRaidState;			//Array state(1:normal 2:Downgrade 3:Off line)
	int					iRaidTask;			//Task(1:no 2:initializing 3:Rebuilding)
	int					iRaidTaskPara1;		//Task status parameters(Rate of progress x.x%)
	int					iRaidTaskPara2;	    //Task status parameters(Expected remaining time x.x hours, x.x=0 Do not display this article)
}RAIDState, *pRAIDState;              

#define MAX_VIRTUAL_DISK_NUM 16			//Globe maximum support 16 virtual disks  
typedef struct tagVirtualDiskState
{
	int					iBufSize;			//Structure size
	int 				iIndex;			    //Input parameters, specifics the index value
	char				cVDName[LEN_64];	//Virtual disk name
	int					iVdUseAbleSize;		//Virtual disk available space(the unit is M)
	int					iVdState;			//Virtual disk state(1:Normal 2:Downgrade 3:Off line)
	int					iVdTask;			//Task(1:no 2:initializing 3:Rebuilding)
	int					iVdTaskPara1;		//Task status parameters(Rate of progress x.x%)
	int					iVdTaskPara2;		//Task status parameters(Expected remaining time x.x hours, x.x=0 Do not display this article)
}VirtualDiskState, *pVirtualDiskState;     

#define MAX_HDD_NUM 24   
#define WORKDEV_BROADCAST_BACKUPDEV		0   
#define BACKUPDEV_BROADCAST_WORKDEV		1   
#define MAX_WORK_DEV_COUNT		16			//Maximum number of working machine 

typedef struct tagBackupDevState
{
	int					iBufSize;			//Structure size
	int 				iIndex;				//Input parameters, Specifics the index value
	char				cIP[LEN_64];		//Device IP
	int					iState;			    //State([DevType=0 0:Dropped 1:Online 2:Synchronizing]
	                                        //[DevType=1 0:Hot standby 1:Backup 2:Synchronize])
	int					iProgress;		    //The percentage of synchronization(0~100)
}BackupDevState,WorkDevState, *pBackupDevState,*pWorkDevState;  

typedef struct tagRAIDDiskInfo
{
	int					iBufSize;			//Structure size
	int					iDiskID;			//Disk id(SATA1~SATA8 id ～,SATA9~SATA16 id ~1015)
	int					iDiskType;			//Disk type(0:Free disk 1:Array hot spare2:Globe hot spare 3:RAID disk 4：invalid RAID disk；5：Faulty disk)
	int					iDiskState;			//Disk state(0:Normal 1:Exception 2:Health warning)
	int					iDiskSize;			//Disk size(The unit is M)
	char				cRaidName[LEN_64];	//Array name
	char				cDiskModel[LEN_64];	//Array mode
}RAIDDiskInfo,*pRAIDDiskInfo;

typedef struct tagIPSANDiskInfo
{
	int					iBufSize;			//Structure size
	int					iDevNo;				//Device number(0～)
	int					iEnable;			//Whether to enable(0:not enable 1:enable)
	char				cDeviceIP[LEN_64];	//IP address
	int					iDevicePort;		//Device port
	char				cUserName[LEN_32];	//Username
	char				cPassword[LEN_32];	//Password
	char				cPath[LEN_64];		//contents
}IPSANDiskInfo,*pIPSANDiskInfo;

typedef struct tagIPSANDiscovery
{
	int					iBufSize;			//Structure size
	int 				iDevType;			//Type(0:Reserved 1:iSCSI)
	char				cDeviceIP[LEN_64];	//IP address
	int					iDevicePort;		//Port
	char				cUserName[LEN_32];	//Username
	char				cPassword[LEN_32];	//Password
}IPSANDiscovery,*pIPSANDiscovery;


//Obtain IPSAN directory information
typedef struct tagIPSANDirectoryInfo
{
	int					iBufSize;			//Structure size
	int 				iIndex;				//Input parameters, specifics the index value
	char				cDeviceIP[LEN_64];	//IP address
	char				cPath[LEN_64];		//contents
}IPSANDirectoryInfo,*pIPSANDirectoryInfo;


#define MAX_RAID_NUM_NUM				8
#define MAX_RAID_TYPE_NUM				4
#define MAX_IPSAN_INFO_NUM				8
#define MAX_IPSAN_DISCOVERY				16
//Use the device disk-related configuration information
#define NET_CLIENT_IPSAN_DISCOVERY				(NET_CLIENT_MIN + 0)	//IPSAN device discovery
#define NET_CLIENT_IPSAN_GET_DIR_COUNT			(NET_CLIENT_MIN + 1)	//IPSAN found the number of directories
#define NET_CLIENT_IPSAN_GET_DIRECTORY			(NET_CLIENT_MIN + 2)	//Get the directory information
#define NET_CLIENT_IPSAN_DISK_INFO				(NET_CLIENT_MIN + 3)	//IPSAN device information
#define NET_CLIENT_RAID_DISK_WORKMODE			(NET_CLIENT_MIN + 4)	//RAID disk working mode
#define NET_CLIENT_RAID_DISK_INFO				(NET_CLIENT_MIN + 5)	//RAID disk information
#define NET_CLIENT_RAID_ARRAY_MANAGE			(NET_CLIENT_MIN + 6)	//New RAID array
#define NET_CLIENT_RAID_ARRAY_INFO				(NET_CLIENT_MIN + 7)	//Obtain RAID array information
#define NET_CLIENT_RAID_STATE					(NET_CLIENT_MIN + 8)	//RAID state
#define NET_CLIENT_RAID_VIRTUAL_DISK_MANAGE		(NET_CLIENT_MIN + 9)	//New RAID virtual disk
#define NET_CLIENT_RAID_VIRTUAL_DISK_INFO		(NET_CLIENT_MIN + 10)	//Obtain RAID virtual disk information
#define NET_CLIENT_RAID_VIRTUAL_DISK_STATE		(NET_CLIENT_MIN + 11)	//RAID virtual disk state

//raid Operate type 0=Reserved；1=New；2=Rebuild； 3=Delete
#define RAID_OPERATE_RESERVE		0
#define RAID_OPERATE_CREATE			1
#define RAID_OPERATE_REBUILD		2
#define RAID_OPERATE_DELETE			3

//RAID Array information
typedef struct tagRAIDArrayInfo
{
	int					iBufSize;					//Structure size
	int 				iIndex;						//Input parameters, specifics the index value
	char				cRaidName[LEN_64];			//Array name
	int					iOptType;					//Operate type,0=Reserved；1=New；2=Rebuild； 3=Delete
	int					iRaidType;					//Array level(0:RAID0 1:RAID1 5:RAID5 10:RAID10 100:JBOD)
	int					iBackDiskID;				//	-1 means no hot spare disk，greater than 0 means hot spare disk id	
	char				cDiskModel[LEN_64];			//Disk model
	int					iDiskNum;					//Disk number
	int					iDisk[MAX_RAID_DISK_NUM];	//Disk list
}RAIDArrayInfo,*pRAIDArrayInfo;

typedef struct tagAlarmLink_V1
{
	int				iBuffSize;
	int				iSceneID;			//Scene id(0~15)
	int				iAlarmTypeParam1;	//Alarm type parameters
	int				iAlarmTypeParam2;	//Alarm type parameters
	int				iLinkType;			//Link type
	int				iLinkTypeParam1;	//Link parameters
	int				iLinkTypeParam2;	//Link parameters
	int				iLinkTypeParam3;	//Link parameters
	int				iLinkTypeParam[13];	//Link parameters，Supports 512-way expansion
}AlarmLink_V1, *PAlarmLink_V1;

//Set the security code bits
typedef struct tagITS_SecurityCode
{
	int		iBufSize;	//Structure size
	int		iPos;		//Start position,the value is 0-64
	int		iCount;		//Security code, the value is 0-64
}ITS_SecurityCode, *pITS_SecurityCode;

//License plate brightness compensation structure
typedef struct tagITS_LightSupply
{
	int		iBufSize;		//Structure size
	int		iIndex;			//Time range number	0-7，Support up to 8 time slots
	int		iFrontLight;	//Front light compensation value	
	int		iBackLight;		//Back light compensation value	
	int		iEnable;		//0-unable 1-enable(tolerant)
	int		iSensitivity;	//Sensitivity(1,100)
	int		iPolarlzerValue;//Polarization threshold(0,100）
}ITS_LightSupply, *pITS_LightSupply;

#define MAX_ITS_CAR_TYPE	3
//Set the lane line parameters of vehicle contour permillage
typedef struct tagITS_LinePressPermillage
{
	int  iBufSize;			//Structure size
	int  iSceneId;			//Scene ID,0~15
	int  iRoadID;			//Road id,Maximum support 4 lanes：0-3
	int  iCarType;			//Car type,0-Reserved，1-dolly，2-cart
	int  iSpeed;			//The profile of the vehicle line permillage【1-1000】
}ITS_LinePressPermillage,*pITS_LinePressPermillage;

//Send traffic-specific character overlays

#define  MAX_ITS_OSDTYPE_NUM	       69		//Number of label types
typedef struct tagITS_WordOverLay
{
	int  	iBufSize;							//Structure size
	int  	iChannelNo;							//Channel number
	int   	iOsdType;							//The standard type of superimposed characters
	int   	iEnable;							//Enable state, Single bit represent type，the content of string is integer.0: Overlap time enable；,,,
	char  	pcOsdName[LEN_32+1];				//Label content
	int   	iPosX;							    //Horizontal coordinate
	int     iPosY;							    //Vertical coordinate
	int		iZoomScale;							//Zoom percentage	Range 0-500，100 means no change					
	int		iLineCharNum;
	int		iBlankCharNum;
	int		iOsdPosNo;
	int		iCapType;							//Capture type
	int     iFacePicType;                       //Face small map size
}ITS_WordOverLay,*pITS_WordOverLay;

#define MAX_ITS_CAPTURE_NUM			32  //Illegal type|Capture type maximum

//The maximum capture template types supported by the T2 series are 16 types, with value ranging from 0 to 15
#define MAX_CAP_TYPE_NUM	(MAX_ITS_CAPTURE_NUM)	

#define ITS_BAYONET_CAPTURE0		0	//Bayonet, traffic has been used，0 and 1 represent the bayonet
#define ITS_BAYONET_CAPTURE1		1	//Bayonet 
#define ITS_RETROGRADE				2	//Retrograde 
#define ITS_RED_LIGHT				3	//Red light 
#define ITS_NOT_GUIDE				4	//Vehicle into the non motor vehicle lane, the motor vehicle is not in the motor vehicle lane
#define ITS_CROSS_LINE				5   //Cross the line 1042 Motor vehicles do not drive in accordance with the provisions of the lane
#define ITS_BAN_INSTRUCTION			6   //Motor vehicle in violation of the prohibition of marking instructions
#define ITS_AMBLE					7	//In front of the motor vehicle parking waiting in line or slow driving, in the crosswalk, mesh line area waiting for parking
#define ITS_OVER_SPEED				8	//Over speed 
#define ITS_ILLEGAL_TURN_AROUND		9	//Turn around
#define ITS_AUTO_USE_PRIVATE		10  //Motor vehicle illegal use of dedicated lanes
#define ITS_ILLEGAL_BACKING_CAR		11  //Do not follow the provisions of reverse driving
#define ITS_ILLEGAL_CHANGE			12	//Illegal change road
#define ITS_BREAK_FORBID_LINE		13	//Break the forbid line
#define ITS_NOT_FASTEN_SAFETY_BELT	14	//Not wearing a seat belt
#define ITS_CALLING					15	//Pick up the phone
#define ITS_ROAD_PEDESTRIAN			16  //Impatient for pedestrians
#define	ITS_LEFT_NOT_AVOID_STRAIGHT	17 //Turn left does not avoid straight
#define ITS_ZEBRA_CROSS				18 //Zebra crossing
#define ITS_OVER_SPEED_LESS_10 		19 //Over Speed less than 10%
#define ITS_OVER_SPEED_10_TO_20 	20 //Speeding 10% -20%
#define ITS_OVER_SPEED_20_TO_30 	21 //Speeding 20% -30%
#define ITS_OVER_SPEED_30_TO_50 	22 //Speeding 30% -50%
#define ITS_OVER_SPEED_50_TO_70 	23 //Speeding 50% -70%
#define ITS_OVER_SPEED_70_TO_100 	24 //Speeding 70% -100%
#define ITS_OVER_SPEED_ABOVE_100 	25 //Speeding 100% and above
#define ITS_OVER_SPEED_20_TO_50 	26 //Speeding 20% -50%
#define ITS_OVER_SPEED_ABOVE_50 	27 //Speeding 50% and above
#define ITS_GASSER_ILLEGAL 			28 //Gasser illegal
#define ITS_GREEN_LIGHT_PARKING     29 //Green light parking illegal
#define ITS_ABNORMAL_LICENSE_PLATE  30 //Abnormal license plate illegal
#define ITS_DANGEROUS_VEHICLE       31 //Dangerous chemicals vehicle

//Capture count structure
typedef struct tagITS_CapCount
{
	int		iBufSize;		//Structure size
	int		iCapType;		//Capture type	0-Bayonet 1--Bayonet 2--Retrograde 3--Red light 4--Vehicle into the non motor vehicle lane, the motor vehicle is not in the motor vehicle lane
	                        //5--Cross the line 1042 Motor vehicles do not drive in accordance with the provisions of the lane
	                        //6--Motor vehicle in violation of the prohibition of marking instructions
							//7--In front of the motor vehicle parking waiting in line or slow driving, in the crosswalk, mesh line area waiting for parking
							//8--Over speed  9--Turn around 10--Motor vehicle illegal use of dedicated lanes 11--Do not follow the provisions of reverse driving 12--Illegal change road  13-Break the forbid line 58-Out of town car cant passing 
	int		iCapCount;		//Capture count. Value of 1，2, minimum 1，At present, the upper limit is controlled within 10.
}ITS_CapCount, *pITS_CapCount;

#define MAX_ILLEGAL_PARK_POINT_NUM		8
typedef struct tagITS_IVTMoveCTR
{
	int		iBufSize;		//Structure size
	int		iXMove;			//Up and down direction motion coordinate，Accurate to 0.01 *100
	int		iYMove;			//Left and right direction motion coordinate, Accurate to 0.01 *100
	int		iZAim;			//Multiple，Accurate to 0.01*100*
}ITS_IVTMoveCTR, *pITS_IVTMoveCTR;

typedef struct tagITS_IllegalParkInfo
{
	int				iBufSize;										//Structure size
	int				iPreset;										//Preset number(Scene ID),maximum of 16， 0~15
	int				iArea;											//Area number,maximum of 8，0~7 
	int				iCalibrationStaus;								//0-Not calibrate,1-Manual calibrate,2-Automatic calibrate
	ITS_IVTMoveCTR	tITS_IVTMoveCTR[MAX_PARAM_COUNT];				//Calibrate dome machine coordinates, absolute coordinate
	POINT			tPoint[MAX_ILLEGAL_PARK_POINT_NUM];				//Position two-point coordinate		
}ITS_IllegalParkInfo, *pITS_IllegalParkInfo;

typedef struct _tagITS_PicCutInfo
{
	int iSize;
	int iChannelNo;
	int iCutType;	// Picture cut types 0-base plate; 1-base car flag;2-base face
	int iCutScale;  //characteristics pictures cut range, such as 4, represent cut range is 1/4 of the size of raw picture.
}ITS_PicCutInfo, *pITS_PicCutInfo;

typedef struct tagCfgFileInfo
{
	int		iBufSize;				//Structure size
	char	cFileName[LEN_64+1];	//File name,64 bytes
	char	cSection[LEN_64+1];		//Section name,64 bytes
	char	cKey[LEN_64+1];			//Key,64bytes
	char	cValue[LEN_64+1];		//Field value, if the field is used for a variety of numeric type , for example“9600,n,7,2 ”
}CfgFileInfo, *pCfgFileInfo;

#define MAX_PLATFORM_NUM				10
#define MAX_APP_SERVER_LIST_NUM			10
//Remote read/Set temperature control value
typedef struct tagAPPServerList
{
	int		iBufSize;					//Structure size
	char	cAppName[LEN_32+1];			//Platform name，no more than 32 bytes
	int		iServerListNum;				//Server list number，Up to 10
	char	cServerListName[MAX_APP_SERVER_LIST_NUM][LEN_32+1];	//Server list name，no more than 32 bytes
	int		iState[MAX_APP_SERVER_LIST_NUM];
}APPServerList, *pAPPServerList;

#define MAX_RTMP_NUM		32
//RTMP list information
typedef struct tagRtmpInfo
{
	int		iBufSize;			//Structure size
	int		iRtmpNo;			//Number,Value 0~32,20200521 change by gaofei
	int		iRtmpChnNo;			//Video channel number
	int		iRtmpEnable;		//Enalbe：After enabling，Can push the flow；when disabled, the flow is inhibited
	char	cRtmpUrl[LEN_256];	//Rtmp URL string, string information：eg："rtmp://10.30.31.21:1935/live"
	char	cRtmpKey[LEN_256];	//Rtmp auth key
    int     iRtmpSndTmOut;      //rtmp发送超时时间	TCP属性（单位：s)
    int     iRtmpRCVTmOut;      //rtmp接收超时时间	TCP属性（单位：s）
    int     iStreamType;        //码流类型 1主码流 2副码流 3三码流
    int     iType;              //当前地址类型 	1:Custom, 2:Non-custom
    int     iPort;              //端口号	默认为1935
    char    cUserName[LEN_64];  //用户名
    char    cUserPasswd[LEN_64];//用户名密码
}RtmpInfo, *pRtmpInfo;

//A channel supports a list of frame rates
#define MAX_FRAME_RATE_NUM		20
typedef struct tagFrameRateList
{
	int		iBufSize;			//Structure size
	int		iChannelNo;			//Channel number, which depends on the actual capacity of the device, including sub-stream. For a mixed DVR of 16 analogue + 16 digits, the value range is 0-63
								//Three-stream 4n~5n-1
	int		iFrameRateNum;		//Frame rate number，the number of frame rates by the device,that is, the number of subsequent fields
	int		iFrameRate[MAX_FRAME_RATE_NUM];	//Frame rate list,stored in descending order. N：60 30 25 20 15 10 5 1 ，P：50 25 20 15 10 5 1
}FrameRateList, *pFrameRateList;

//RTSP list information
#define MAX_RTSP_LIST_NUM		64
typedef struct tagRtspListInfo
{
	int		iBufSize;			//Structure size
	int		iRtspNo;			//rtsp number，range0~63
	char	cRtspIp[LEN_32];	//rtsp connection ip。default 0.0.0.0，“0.0.0.0”delegates allow anyone to connect
}RtspListInfo, *pRtspListInfo;

/////////////////////////////////Dynamic environment add////////////////////////////////
//Alarm input channel type
#define ALARM_INTERFACE_TYPE_MIN       0
#define ALARM_INTERFACE_TYPE_SWITCH    1  //switch
#define ALARM_INTERFACE_TYPE_ANALOG    2  //analog
#define ALARM_INTERFACE_TYPE_COM       3  //Serial port
#define ALARM_INTERFACE_TYPE_MAX       4

//Interface SetAlarmConfig
#define CMD_SET_ALARMSCHEDULE		0
#define CMD_SET_ALARMLINK			1
#define CMD_SET_ALARMSCH_ENABLE		2
#define CMD_SET_ALARMTRIGGER		3
#define CMD_SET_ALARMLINK_V1		4
#define CMD_SET_ALARMLINK_V2		5
#define CMD_SET_ALARMLINK_V3		6
#define CMD_SET_ALARM_NVRLINKIPC	7	
//Dynamic environment alarm configuration NetClient_SetAlarmConfig/ NetClient_GetAlarmConfig
#define CMD_DH_ALARM_MIN					(100)//Minimum value of the interface of the Dynamic environment alarm type
#define CMD_ALARM_IN_CONFIG                 (CMD_DH_ALARM_MIN + 0)//Dynamic environment alarm configuration
#define CMD_ALARM_IN_LINK                   (CMD_DH_ALARM_MIN + 1)//Dynamic environment alarm linkage
#define CMD_ALARM_IN_SCHEDULE				(CMD_DH_ALARM_MIN + 2)//Dynamic environment configuration of the arming template
#define CMD_ALARM_IN_SCHEDULE_ENABLE        (CMD_DH_ALARM_MIN + 3)//Dynamic environment configuration of the arming enable
#define CMD_ALARM_IN_OSD                    (CMD_DH_ALARM_MIN + 4)//Dynamic environment characters overlay configuration 
#define CMD_ALARM_IN_DEBUG                  (CMD_DH_ALARM_MIN + 5)//Dynamic environment debug
#define CMD_ALARM_IN_OFFLINE_TIME_INTERVEL  (CMD_DH_ALARM_MIN + 6)//Offline interval
#define CMD_DH_ALARM_HOST  					(CMD_DH_ALARM_MIN + 7)//Alarm server parameter setting
#define CMD_DH_ADD_ALARM_HOST				(CMD_DH_ALARM_MIN + 8)//Add the alarm host
#define CMD_DH_DEVICE_ENABLE				(CMD_DH_ALARM_MIN + 9)//Set up the moving ring device enalbe
#define CMD_ALARMSCH_ENABLE_EX				(CMD_DH_ALARM_MIN + 10)
#define CMD_ALARM_EXTRA_SCHEDULE			(CMD_DH_ALARM_MIN + 11)
#define CMD_ALARM_FACE_PARAM				(CMD_DH_ALARM_MIN + 12)
#define CMD_ALARM_IN_PORT_PARA				(CMD_DH_ALARM_MIN + 13)//alarm in port para, mode,type......
#define CMD_ALARM_IN_RAINFALL_PARA          (CMD_DH_ALARM_MIN + 14)
#define CMD_ALARM_IN_ALERTWATER_PARA        (CMD_DH_ALARM_MIN + 15)
#define CMD_ALARM_OUTPORT_PARA				(CMD_DH_ALARM_MIN + 16)//alarm out port para, mode,type......
#define CMD_ALARM_INPORT_ENABLE				(CMD_DH_ALARM_MIN + 17)
#define CMD_ALARM_OUTPORT_ENABLE			(CMD_DH_ALARM_MIN + 18)
#define CMD_ALARM_INPORT_STATE				(CMD_DH_ALARM_MIN + 19)
#define CMD_ALARM_OUTPORT_STATE				(CMD_DH_ALARM_MIN + 20)
#define CMD_ALARM_OUTPORT_DELAYTIME			(CMD_DH_ALARM_MIN + 21)
#define CMD_DH_ALARM_MAX					(CMD_DH_ALARM_MIN + 22)//MAX



typedef struct __tagAlarmInConfig
{	
	int iSize;
	char cName[MAX_NAME_LEN + 1];
	int iInterfaceType; //ALARM_IN_CHANNEL_TYPE_MIN~  ALARM_IN_CHANNEL_TYPE_MAX
	int iInterfaceNo ;
	int iScheduleNo;
	int iDelay;
	char cParam[MAX_DHINFO_PARAM_LEN];
}AlarmInConfig, *PAlarmInConfig;


typedef struct __tagAlarmInLink
{	
	int iSize;
	int  iLinkIndex;
	int iLinkEnable;
	char cName[MAX_NAME_LEN + 1];
	int iAlarmType; 
	int iLinkType;
	int  iDisappearType;		//Eliminate alarm
	int  iDisappearTime;        //Eliminate alarm time
	char cParam[MAX_DHINFO_PARAM_LEN];
}AlarmInLink, *PAlarmInLink;

typedef struct __tagAlarmInSchedule
{	
	int iSize;
	int iScheduleNo;
	int iEnable;
	char cName[MAX_NAME_LEN + 1];
	int iWeekday;//0~6 Sunday~Monday   -1 Whole week setting  
	NVS_SCHEDTIME	timeSeg[MAX_DAYS][MAX_DH_TIMESEGMENT];	
}AlarmInSchedule, *PAlarmInSchedule;

#define ALARM_IN_SCHEDULE_DISENABLE 0
#define ALARM_IN_SCHEDULE_ENABLE    1 
#define ALARM_IN_SCHEDULE_AUTO      2
#define MAX_ALARM_IN_SCHEDULE		3

typedef struct __tagAlarmInOSD
{	
	int iSize;
	int iOsdIndex;
	int iEnable;
	char cIP[LENGTH_IPV4];
	int iChannelNo;
	int iBlockNo;	
	int iColor;
}AlarmInOSD, *PAlarmInOSD;

typedef struct __tagAlarmInDebug
{	
	int iSize;
	char    cName[MAX_NAME_LEN + 1];
	int iDelayTime;
	char cParam[MAX_DHINFO_PARAM_LEN];
}AlarmInDebug, *PAlarmInDebug;

#define MAX_DH_ALARMTYPE_NUM		2
#define MAX_DH_ALARMHOST_NUM		8
typedef  struct _tagAlarmHost
{
	int iSize;				
	int iDevType;  				//Alarm host type0：Network alarm host；1：Serial alarm host
	int	iDevNo;					//Alarm host number 0-7
	int iEnable;				//Enable flag 0：forbid；1：start
	char cDevAddr[LEN_64];		//Alarm host address，IP address（Maximum 64 bytes）
	int	iDevPort; 				//Alarm host port，0～65535（default 18803）
	int	iConnType;				//Connection type，1：TCP；2：UDP（default）
	int	iPortInNum; 			//Number of input ports
	int	iPortOutNum;			//Number of output ports
}tAlarmHost, *ptAlarmHost;

//Alarm host type
#define ALARM_SERVER_NET               0
#define ALARM_SERVER_COM               1

//Connection type
#define ALARM_SERVER_CONNTYPE_TCP       1
#define ALARM_SERVER_CONNTYPE_UDP       2

typedef struct __tagLogInterval
{	
	int		iSize;
	int		iDelayTime;
}tLogInterval, *PtLogInterval;

typedef struct __tagComDevice
{
	int		iSize;				//Size of the structure,must be initialized before used
	int		iComNo;				//Serial number
	char	cComFormat[LEN_32];	//Format serial 9600,n,8,1
	int     iDeviceType;        //0- 1- 2-
	char	cParam[MAX_DHINFO_PARAM_LEN];
}ComDevice,*PComDevice;


#define MAX_DH_SER_NO		100000   //Dynamic environment serial number
//Dynamic environment data report WCM_ALARM_INFORMATION
typedef struct __tagAlarmInfoData
{	
	int iSize;
	int iSerNo;
	int iDhInterfaceType;			  //Channel type
	int iStatus;
	int iChannelNo;
	int iAlarmType;
	int iAlarmState;
	int iDataType;
	unsigned long iTime;
	char cInfo[MAX_DHINFO_PARAM_LEN];	
}AlarmInfoData, *PAlarmInfoData;

//The maximum of splits to string send and receive
#define MAX_RECV_AND_SPLIT_NUM		16   //The number of strings that are dynamically receive or send for splitting 
#define MAX_PROTOCO_SPLIT_NUM		40   //The number of protocol strings that are dynamically receive or send for splitting

//Serial interface link device type
#define	DH_COM_DEV_TYPE_TEM			0	  //Temperature and humidity 	
#define	MAX_DH_DEV_TYPE				40    //The maximum number of supported device type
#define MAX_DH_COM_PROTOCOL			40	  //Maximum support protocol types(Dynamic environment device corresponds to the supported device type)

//DH Alarm linkage
#define MAX_DH_ALARMLINK_NUM		16    //Dynamic environment alarm linkage number
#define MAX_DH_ALARMSCHE_NUM		16    //Alarm arming template number

//DH Alarm linkage
#define DH_ALARM_LINK_TYPE_MIN				0
#define DH_ALARM_LINK_TYPE_RESERVE			(DH_ALARM_LINK_TYPE_MIN+0)	//Reserved 
#define DH_ALARM_LINK_TYPE_OUT				(DH_ALARM_LINK_TYPE_MIN+1)	//Alarm output
#define DH_ALARM_LINK_TYPE_OSD				(DH_ALARM_LINK_TYPE_MIN+2)	//OSD
#define DH_ALARM_LINK_TYPE_EMBATTLE			(DH_ALARM_LINK_TYPE_MIN+3)	//Arm
#define DH_ALARM_LINK_TYPE_DISMANTLE		(DH_ALARM_LINK_TYPE_MIN+4)	//Disarm
#define DH_ALARM_LINK_TYPE_RECOVERY			(DH_ALARM_LINK_TYPE_MIN+5)	//Restore self-control 
#define DH_ALARM_LINK_TYPE_MAX				(DH_ALARM_LINK_TYPE_MIN+6)

//DH eliminate alarm
#define DH_ALARM_DISAPPEAR_TYPE_MIN				0
#define DH_ALARM_DISAPPEAR_TYPE_NOT				(DH_ALARM_LINK_TYPE_MIN+0)	//Not recovery
#define DH_ALARM_DISAPPEAR_TYPE_DELAY			(DH_ALARM_LINK_TYPE_MIN+1)	//Delay recovery
#define DH_ALARM_DISAPPEAR_TYPE_DIS_RECOVERY	(DH_ALARM_LINK_TYPE_MIN+2)	//Eliminate alarm recovery
#define DH_ALARM_DISAPPEAR_TYPE_DIS_DELAY		(DH_ALARM_LINK_TYPE_MIN+3)	//Eliminate alarm delay recovery
#define DH_ALARM_DISAPPEAR_TYPE_MAX				(DH_ALARM_LINK_TYPE_MIN+4)

#define MAX_DH_ALARM_HOST_NUM		16
typedef struct tagAddAlarmHost
{
	int iSize;					//Structure size
	int iHostIndex;				//Host index，[0,15]
	int iEnable;				//0--Not enable，1--Enalbe
	char cIP[LEN_32];			//Alarm host IP
	int	iPort;					//Alarm host port
}AddAlarmHost, *pAddAlarmHost;
//////////////////////////////////Dynamic environment add///////////////////////////////

/////////////////////////////////Power grid////////////////////////////////
#define		Td_Alg_Meter_TaoGuanBiao_1					0/* Casing meter（White） */
#define		Td_Alg_Meter_ZhuBianYouBiao					1/* Main transformer oil meter */
#define		Td_Alg_Meter_BiLeiQi						2/* Lightning arrester */
#define		Td_Alg_Meter_DangWeiXianShiQi_1				3/* Gears displayer No.1（the radius is small）*/
#define		Td_Alg_Meter_WenDuBiao						4/* Thermometer */
#define		Td_Alg_Meter_KaiGuanFenHeQi					5/* Switch separate displayer*/
#define		Td_Alg_Meter_QiTiYaLiBiao					6/* Gas pressure gauge */
#define		Td_Alg_Meter_YaLiBiao						7/* Piezometer */
#define		Td_Alg_Meter_DaoZha							8/* Switch */
#define		Td_Alg_Meter_BiLeiJianCeBiao				9/* Lightning detection meter */
#define		Td_Alg_Meter_WenDuBiaoQiTiBiao				10/* Thermometer Gas meter */

#define		Td_Alg_Meter_DuoGongNengDianNengBiao		11/* Multi-function watt-hour meter */
#define		Td_Alg_Meter_XianLuBaoHuZhuangZhi			12/* Wire protection device */
#define		Td_Alg_Meter_HuaLiDuoGongNengDianNeng		13/* HuaLi Multi-function electric energy*/
#define		Td_Alg_Meter_WeiShengDuoGongNengDianNeng	14/* WeiSheng Multi-function electric energy*/
#define		Td_Alg_Meter_ZhiNengWenShiDuKongZhiQi		15/* Intelligent temperature and humidity controller */
#define		Td_Alg_Meter_KaiGuanZhiNengCaoKongZhuangZhi	16/* Switch intelligent control device */
#define		Td_Alg_Meter_SanXiangDuoGongNengDianNeng	17/* Three-phase multi-function power */
#define		Td_Alg_Meter_KaiGuan						18/* Switch */
#define		Td_Alg_Meter_KaiGuanFenHeXianShiQi			19/* witch separate displayer*/
#define		Td_Alg_Meter_TaoGuanBiao_2					20/* Casing meter No.2（Digit，large radius） */
#define		Td_Alg_Meter_MiDuJiDianQi					21/* Density relay */
#define		Td_Alg_Meter_YouWeiJiBiao					22/* Oil level meter */
#define		Td_Alg_Meter_DangWeiXianShiQi_2				23/* Gears displayer No.2（Red） */

#define MAX_ELENET_METER_PARAM_LEN 1024
typedef struct tagEleNetMeter
{
	int iSize;					//Structure size
	int iChanNo;				//Channel number
	int iMeterType;				//Meter type
	char cParam[MAX_ELENET_METER_PARAM_LEN];			//string
}EleNetMeter, *pEleNetMeter;
/////////////////////////////////Power grid/////////////////////////////////

//NvsType——0-T，1-S，2-T+
#define NVS_TYPE_T			0
#define NVS_TYPE_S			1
#define NVS_TYPE_T_OTHER	2

#define CLIENT_TYPE_OLD     0
#define CLIENT_TYPE_NEW     1
#define CLIENT_TYPE_WIFINVR 2
#define CLIENT_TYPE_TOKEN	3

#define CMD_TRANS_PROTOCOL_DATA		0
typedef void (*TransDataCbk)(int _iLogonID, int _iCmd, void* _pvData, int _iLen, void* _pvUsr);

typedef struct tagLogonPara
{
	int		iSize;					//Structure size
	char	cProxy[LEN_32];			//The ip address of the upper-level proxy to which the video is connected,not more than 32 characters, including '\0'
	char	cNvsIP[LEN_32];			//IPV4 address，not more than 32 characters, including '\0'
	char	cNvsName[LEN_32];		//Nvs name. Used for domain name resolution
	char	cUserName[LEN_16];		//Login Nvs username，not more than 16 characters, including '\0'
	char	cUserPwd[LEN_16];		//Login Nvs password，not more than 16 characters, including '\0'
	char	cProductID[LEN_32];		//Product ID，not more than 32 characters, including '\0'
	int		iNvsPort;				//The communication port used by the Nvs server, if not specificed,Use the system default port(3000)
	char	cCharSet[LEN_32];		//Character set
	char	cAccontName[LEN_16];	//The username that connects to the contents server
	char	cAccontPasswd[LEN_16];	//The password that connects to the contents server
	char	cProxyIpV6[LEN_64];		//IPV6 Proxy address
	char	cNvsIpV6[LEN_64];		//IPV6 address
    int     iClientType;            //0-老客户端，1-新客户端, 2-WIFINVR
	char    cPlatFeature[LEN_32];   //Platform Feature
	TransDataCbk	pfTransCbk;
	void*			pvTransUdata;
	int		iNeedAuthorize;			//是否需要双向认证（GDDZ定制专用）0-不需要认证，1-需要认证
}LogonPara, *pLogonPara;

//ITS Image detection parameter added structure
typedef	struct tagITS_VideoDetect
{
	int			iSize;					//Structure sieze
	int			iEngineType;
	int			iEnable;
	int			iVedioType;
	int			iVedioWidth;
	int			iVedioHeight;
	int			iVehicleMinSize;
	int			iVehicleMaxSize;
	int			iModelType;
	int			iFrameRateCount;
	int			iVDetectMotor;			//Non-motor vehicle detection enable
	int			iSceneID;				//Scene ID
	char		cParams130T[LEN_64];	//The private parameter of algorithm No.1
	char		cParams130G[LEN_64];	//The private parameter of algorithm No.2
	char		cParams110G[LEN_64];	//The private parameter of algorithm No.3
}ITS_VideoDetect, *PITS_VideoDetect;

//ITS Roadway rader area config info struct
typedef	struct tagITS_RaderCfgInfo
{
	int			iSize;					//Size of Struct
	int			iRoadwayID;				//Roadway ID,range[0,3]
	int			iRaderAreaID;			//Rader area ID,range[1,8]
	int			iRaderAreaLeftEdge;		//Rader area left edge value,range[-200-200]
	int			iRaderAreaRightEdge;	//Rader area right edge value,range[-200-200]
	int			iRaderAreaLine;			//Rader area line,range[1,100] Unit :( m)
}ITS_RaderCfgInfo, *PITS_RaderCfgInfo;


//download state
#define		VOD_DOWNLOAD_NORMAL		0
#define		VOD_DOWNLOAD_PAUSE		1


typedef struct __tagVideoEncodList
{
	int iSize;		   //struct size	
	int iChnNo;        //dev channel no
	int iVideoEncode;  //EncodType:By byte：bit0，H.264；bit1，M-JPEG；bit2，MPEG4；bit3，H.265，def:0
}tVideoEncodList,*ptVideoEncodList;


typedef struct
{
	int iSize;    //Structure size
	int iIndex;   //Sampling point
	int iTime;    //Time point
	int iCount;   //Capture count
	int iInterVal;//Capture interval
}WaterSampleSnapInfo,*pWaterSampleSnapInfo;


#define GAUGE_TYPE_RESERVED	   	          0
#define GAUGE_TYPE_WIDE			          1
#define GAUGE_TYPE_NARROW		          2
#define GAUGE_TYPE_SPECIAL		          3
#define GAUGE_TYPE_SQUARE		          4		//Square water ruler
#define GAUGE_TYPE_PILE                  5
#define GAUGE_TYPE_NORULE                6
#define GAUGE_TYPE_VIRTUAL               7
#define GAUGE_TYPE_PILE_VERTICALRELAY     8
#define GAUGE_TYPE_NORULE_VERTICALRELAY   9
#define GAUGE_TYPE_STEP                   10
#define GAUGE_TYPE_EVACAPACITY            11

#define WLD_DEFAULT_SNAP_INTERVAL	360
#define WLD_DEFAULT_GAUGE_LENGTTH	1000

typedef struct _tagExceptionLinkOut
{
	int iSize;								//Permission number
	int iExceptionType;						//Exception type
	unsigned int iExceptionEnable[LEN_16];  //Exception Enable
}tExceptionLinkOut, *ptExceptionLinkOut;

typedef struct _tagDiskOperation
{
	int iSize;
	int iAction;
	int iDiskNo;
}tDiskOperation, *pDiskOperation;
typedef struct tagScenetimePoint
{
	int					iBufSize;			
	int					iType;	
	int           		iIndex;
	int 				iEnable;			
	NVS_FILE_TIME		stStartTime;
	NVS_FILE_TIME		stEndTime;
}ScenetimePoint,*pScenetimePoint;

//reboot device by type
#define REBOOT_BY_DEVICE			0
#define REBOOT_BY_GUI				1
#define REBOOT_BY_APP_ITS			2
#define REBOOT_BY_APP_ALL			3
#define MAX_REBOOT_TYPE_NUM			4

#define REBOOT_BY_APP				REBOOT_BY_APP_ITS		//default its

#define MAX_H323_GROUP_ID	1
/*****************************H.323 Local Param*****************************/
//ID code mode，0-UTF-8， 1-Unicode
#define H323_IDCODE_UTF8		0
#define H323_IDCODE_UNICODE		1
//Responds by call：0-automatic,1-manual,2-not disturb
#define  H323_RESPONDS_AUTOMATIC		0
#define  H323_RESPONDS_MANUAL			1
#define  H323_RESPONDS_NOTDISTURB		2

typedef struct _tagH323LocalParam
{
	int iSize;
	int iH323GroupId;				//H323 number,default 0
	int iListenPort;				//Listen port
	char cLocalNo[LEN_64];			//Terminal number
	char cPassWord[LEN_64];			//Encrytion password
	int iIdCodeType;				//ID code mode，0-UTF-8， 1-Unicode
	int iAnwTypeForCall;			//Responds by call：0-automatic,1-manual,2-not disturb
	int iMainSrcChnNo;				//Encode main stream channel number
	int	iSubSrcChnNo;				//Encode sub stream channel number
	int	iMainDecChnNo;				//Decode main stream channel number
	int	iSubDecChnNo;				//Decode sub stream channel number
}H323LocalParam, *pH323LocalParam;

/*****************************H.323 GK Param*****************************/
//GK Mode:0-disable,1-designation,2-auto search
#define  H323_GK_MODE_DISABLE		0
#define  H323_GK_MODE_DESIGNATION	1
#define  H323_GK_MODE_AUTOSEARCH	2
//GK Encrytion mode,0-shut down,1-automatic,2-zhongxing,3-xinshitong,4-sike
#define  H323_GK_ENCRYTION_SHUTDOWN			0	
#define  H323_GK_ENCRYTION_AUTOMATIC		1
#define  H323_GK_ENCRYTION_ZHONGXING		2
#define  H323_GK_ENCRYTION_XINGSHITONG		3
#define  H323_GK_ENCRYTION_SIKE				4

typedef struct _tagH323GKParam
{
	int iSize;
	int iH323GroupId;				//H323 number,default 0
	int iGKMode;					//GK Mode:0-disable,1-designation,2-auto search
	char cGKAddress[LEN_64];		//GK address,64 bytes
	int iGKPort;					//GK port
	char cRegisterName[LEN_64];		//Register Name,64 bytes
	int iGKEncrytionType;			//GK Encrytion mode,0-shut down,1-automatic,2-zhongxing,3-xinshitong,4-sike
}H323GKParam, *pH323GKParam;

#define VCA_ARITHMETIC_TYPE_MIN							0						//Arithmetic Type Min
#define VCA_ARITHMETIC_BEHAVIOR_ANALYSIS		(VCA_ARITHMETIC_TYPE_MIN+0)		//Behavior Analysis
#define VCA_ARITHMETIC_LICENSE_RECOGNITION		(VCA_ARITHMETIC_TYPE_MIN+1)		//License Plate Recognition
#define VCA_ARITHMETIC_FACE_DETECT				(VCA_ARITHMETIC_TYPE_MIN+9)		//face detect
#define	VCA_ARITHMETIC_VIDEO_DETECT				(VCA_ARITHMETIC_TYPE_MIN+10)	//Video Detect
#define	VCA_ARITHMETIC_TRACK					(VCA_ARITHMETIC_TYPE_MIN+11)	//track
#define	VCA_ARITHMETIC_FLUX_STATISTIC			(VCA_ARITHMETIC_TYPE_MIN+12)	//Flux Statistics
#define	VCA_ARITHMETIC_CROWDS					(VCA_ARITHMETIC_TYPE_MIN+13)	//Crowds
#define	VCA_ARITHMETIC_LEAVE_DETECT				(VCA_ARITHMETIC_TYPE_MIN+14)	//Leave Detect
#define	VCA_ARITHMETIC_AUDIO_DIAGNOSE			(VCA_ARITHMETIC_TYPE_MIN+16)	//Audio Diagnose
#define	VCA_ARITHMETIC_FACEMOSAIC				(VCA_ARITHMETIC_TYPE_MIN+17)	//Face Mosaic
#define VCA_ARITHMETIC_ILLEAGEPARK				(VCA_ARITHMETIC_TYPE_MIN+20)	//Illeage Park
#define VCA_ARITHMETIC_ST_FACE_ADVANCE			(VCA_ARITHMETIC_TYPE_MIN+22)	//ST face advance
#define VCA_ARITHMETIC_SEEPER					(VCA_ARITHMETIC_TYPE_MIN+25)	//Seeper
#define VCA_ARITHMETIC_WINDOW_DETECTION			(VCA_ARITHMETIC_TYPE_MIN+26)	//window detection
#define VCA_ARITHMETIC_PARK_GUARD				(VCA_ARITHMETIC_TYPE_MIN+28)	//park guard
#define VCA_ARITHMETIC_HELMET					(VCA_ARITHMETIC_TYPE_MIN+30)	//Helmet
#define VCA_ARITHMETIC_LINK_DOME_TRACK			(VCA_ARITHMETIC_TYPE_MIN+31)	//Link dome track
#define VCA_ARITHMETIC_COLOR_TRACK				(VCA_ARITHMETIC_TYPE_MIN+33)	//Color Track
#define VCA_ARITHMETIC_FORMAT_TYPE				(VCA_ARITHMETIC_TYPE_MIN+34)	//Structured Arithmetic
#define VCA_ARITHMETIC_PROCURATORATE_DUTY		(VCA_ARITHMETIC_TYPE_MIN+37)	//Procturatorate Duty
#define VCA_ARITHMETIC_HEIGHT_LIMIT				(VCA_ARITHMETIC_TYPE_MIN+38)	//Height Limit
#define VCA_ARITHMETIC_NEW_DUTY					(VCA_ARITHMETIC_TYPE_MIN+39)	//New Duty
#define VCA_ARITHMETIC_ABNORMAL_NUMBER			(VCA_ARITHMETIC_TYPE_MIN+40)	//Abnormal Number
#define VCA_ARITHMETIC_GET_UP					(VCA_ARITHMETIC_TYPE_MIN+41)	//Get Up
#define VCA_ARITHMETIC_LEAVE_BED				(VCA_ARITHMETIC_TYPE_MIN+42)	//Leave Bed
#define VCA_ARITHMETIC_HOLD_STILL				(VCA_ARITHMETIC_TYPE_MIN+43)	//Hold Still
#define VCA_ARITHMETIC_SLEEP					(VCA_ARITHMETIC_TYPE_MIN+44)	//Sleep
#define VCA_ARITHMETIC_SLIP_UP					(VCA_ARITHMETIC_TYPE_MIN+45)	//Slip Up
#define VCA_ARITHMETIC_NEW_FIGHT				(VCA_ARITHMETIC_TYPE_MIN+46)	//New Fight
#define VCA_ARITHMETIC_ARM_TOUCH				(VCA_ARITHMETIC_TYPE_MIN+47)	//Arm Touch
#define VCA_ARITHMETIC_HUMAN_DETECT				(VCA_ARITHMETIC_TYPE_MIN+48)	//Human Detect
#define VCA_ARITHMETIC_PEPT						(VCA_ARITHMETIC_TYPE_MIN+51)	//pept
#define VCA_ARITHMETIC_STRANDED					(VCA_ARITHMETIC_TYPE_MIN+55)
#define VCA_ARITHMETIC_ALONE					(VCA_ARITHMETIC_TYPE_MIN+56)
#define VCA_ARITHMETIC_DELIVERGOODS				(VCA_ARITHMETIC_TYPE_MIN+57)
#define VCA_ARITHMETIC_LINGER					(VCA_ARITHMETIC_TYPE_MIN+58)
#define VCA_ARITHMETIC_GOODS_LEFT				(VCA_ARITHMETIC_TYPE_MIN+59)
#define VCA_ARITHMETIC_GOODS_LOSE				(VCA_ARITHMETIC_TYPE_MIN+60)
#define VCA_ARITHMETIC_THREMAL_CHART			(VCA_ARITHMETIC_TYPE_MIN+61)
#define VCA_ARITHMETIC_SMOKE					(VCA_ARITHMETIC_TYPE_MIN+62)
#define VCA_ARITHMETIC_TELEPHONE				(VCA_ARITHMETIC_TYPE_MIN+63)
#define VCA_ARITHMETIC_TEMDETECT      			(VCA_ARITHMETIC_TYPE_MIN+64)    //Tempreture Detect
#define VCA_ARITHMETIC_FIREWORKDETECT           (VCA_ARITHMETIC_TYPE_MIN+65)    //FireWork Detect
#define	VCA_ARITHMETIC_SMART_MOVE				(VCA_ARITHMETIC_TYPE_MIN+66)	//Smart Move
#define VCA_INTERROGATION_TIMEOUT				(VCA_ARITHMETIC_TYPE_MIN+67)	// interrogation timeout
#define VCA_INDOOR_ELEC_VEHICLE_DETECT			(VCA_ARITHMETIC_TYPE_MIN+68)	//indoor electric vehicle detection
#define VCA_OBJECT_LEAVING						(VCA_ARITHMETIC_TYPE_MIN+69)	//object leaving
#define VCA_SCENE_CLASSIFICATION				(VCA_ARITHMETIC_TYPE_MIN+70)	//scene classification
#define VCA_PROHIBITED_ITEM						(VCA_ARITHMETIC_TYPE_MIN+71)	//prohibited items
#define VCA_IRREGULAR_REST		 				(VCA_ARITHMETIC_TYPE_MIN+72)	//irregular rest 
#define VCA_UNATTENDED_TOILET					(VCA_ARITHMETIC_TYPE_MIN+73)	//unattended toilet
#define VCA_DOOR_NOT_CLOSED						(VCA_ARITHMETIC_TYPE_MIN+74)	//door not closed during conversation
#define VCA_LONG_TERM_HAND_RAISING				(VCA_ARITHMETIC_TYPE_MIN+75)	//long-term hand raising / long-term standing / long-term squatting 
#define VCA_RETROGRADE							(VCA_ARITHMETIC_TYPE_MIN+76)	//retrograde
#define VCA_INTELLIGENT_INTERROGATION			(VCA_ARITHMETIC_TYPE_MIN+77)	//Intelligent interrogation
#define VCA_LAW_ENFORMANCE_PROSECUTORS			(VCA_ARITHMETIC_TYPE_MIN+78)	//law enforcement prosecutors
#define VCA_BEHAVIOUR_RECOGNITION				(VCA_ARITHMETIC_TYPE_MIN+79)	//behavior recognition
#define VCA_INCLINEDD_STATISTICS				(VCA_ARITHMETIC_TYPE_MIN+80)	//inclined statistics
#define VCA_VERTICAL_STATISTICS 				(VCA_ARITHMETIC_TYPE_MIN+81)	//vertical statistics
#define VCA_PERSONNEL_GATHERING					(VCA_ARITHMETIC_TYPE_MIN+82)	//Personnel gathering
#define VCA_HUMAN_TEMPRATURE					(VCA_ARITHMETIC_TYPE_MIN+83)	//Human Temperature
#define VCA_PERSONNEL_DENSITY					(VCA_ARITHMETIC_TYPE_MIN+84)	//personnel density
#define VCA_VEHICLE_DENSITY						(VCA_ARITHMETIC_TYPE_MIN+85)	//vehicle density
#define VCA_VEHICLE_CONGESTION					(VCA_ARITHMETIC_TYPE_MIN+86)	//vehicle congestion
#define VCA_VCA_VEHICLE_DETENTION				(VCA_ARITHMETIC_TYPE_MIN+87)	//vehicle detention
#define VCA_ABNORMAL_PARKING					(VCA_ARITHMETIC_TYPE_MIN+88)	//abnormal parking
#define VCA_CROSS_CONGESTION					(VCA_ARITHMETIC_TYPE_MIN+89)	//Cross congestion
#define VCA_JUDGE_BEHAVIOR_ANALYZE				(VCA_ARITHMETIC_TYPE_MIN+90)	//Judge's Behavior Analysis
#define VCA_VERTICALHUMAN_DETECTION				(VCA_ARITHMETIC_TYPE_MIN+91)	//vertical human detection
#define VCA_AERIAL_PROJECTILE					(VCA_ARITHMETIC_TYPE_MIN+92)	//aerial projectile
#define VCA_WATER_OUTFALL						(VCA_ARITHMETIC_TYPE_MIN+93)	//sewage outfall monitoring

#define VCA_POLICE_UNIFORM_DETECTION			(VCA_ARITHMETIC_TYPE_MIN+94)	//Police uniform inspection

#define VCA_SPDRESS_DETECTION					(VCA_ARITHMETIC_TYPE_MIN+95)	//Supervised personnel identification clothing testing
#define VCA_SPQUEUE_DETECTION					(VCA_ARITHMETIC_TYPE_MIN+96)	//Supervised personnel queue detection
#define VCA_ACTIVE_STRATEGY						(VCA_ARITHMETIC_TYPE_MIN+97)	//Proactive Strategy
#define VCA_AUDIO_LOSS_ALARM					(VCA_ARITHMETIC_TYPE_MIN+98)	//Audio loss alarm
#define VCA_CLIMB_WALL					(VCA_ARITHMETIC_TYPE_MIN+99)	//Wall detection
#define VCA_CLASSROOM_BEHAVIOR_RECOGNITION		(VCA_ARITHMETIC_TYPE_MIN+100)	//Classroom behavior recognition
#define VCA_SLEEP_ABNORMALITY_DETECTION			(VCA_ARITHMETIC_TYPE_MIN+101)	//Sleep abnormality detection
#define VCA_PLAY_PHONE_DETECTION				(VCA_ARITHMETIC_TYPE_MIN+102)	//Play mobile phone detection
#define VCA_NUMBER_DETECTION					(VCA_ARITHMETIC_TYPE_MIN+103)	//Number detection
#define VCA_IMAGE_ANALYSIS_DETECTION			(VCA_ARITHMETIC_TYPE_MIN+104)	//Image analysis and detection
#define VCA_AUDIO_ANALYSIS						(VCA_ARITHMETIC_TYPE_MIN+105)	//Audio analysis
#define VCA_PERSON_FAST_MOVE					(VCA_ARITHMETIC_TYPE_MIN+106)	//Rapid movement of personnel
#define VCA_COLLISION_DETECTION					(VCA_ARITHMETIC_TYPE_MIN+107)	//Collision detection
#define VCA_ESCORT_ANOMALY						(VCA_ARITHMETIC_TYPE_MIN+108)	//Abnormal escort
#define VCA_MEAL_DETECTION						(VCA_ARITHMETIC_TYPE_MIN+109)	//Meal detection
#define VCA_POLICE_UNIFORMS_MIXED				(VCA_ARITHMETIC_TYPE_MIN+110)	//Mixed police uniforms
#define VCA_NON_STATIC_DETECTION				(VCA_ARITHMETIC_TYPE_MIN+111)	//Non static detection
#define VCA_NOT_WEAR_POLICE_UNIFORM				(VCA_ARITHMETIC_TYPE_MIN+112)	//Not wearing police uniform for testing
#define VCA_TARGET_DETECTION					(VCA_ARITHMETIC_TYPE_MIN+113)	//Target detection
#define	VCA_ARITHMETIC_TYPE_MAX					(VCA_ARITHMETIC_TYPE_MIN+114)	//Arithmetic Type Max

#define VCA_BEHAVIOR_TYPE_TRIPWIRE				(1<<0)  						//Tripwire 0
#define VCA_BEHAVIOR_TYPE_DBTRIPWIRE			(1<<1)  						//Double Tripwire 1
#define VCA_BEHAVIOR_TYPE_PERIMETER				(1<<2)  						//Perimeter 2
#define VCA_BEHAVIOR_TYPE_LOITER				(1<<3)  						//Loiter 3
#define VCA_BEHAVIOR_TYPE_PARKING				(1<<4)  						//Parking 4
#define VCA_BEHAVIOR_TYPE_RUN					(1<<5)  						//Running 5
#define VCA_BEHAVIOR_TYPE_OBJSTOLEN				(1<<7)  						//Stolen Objects 7
#define VCA_BEHAVIOR_TYPE_ABANDUM				(1<<8)  						//Abandum Objects 8
#define VCA_BEHAVIOR_TYPE_PROTECT				(1<<9)  						//Protect Objects 9
#define VCA_BEHAVIOR_TYPE_HEATMAP				(1<<10)  						//Heatmap Objects 10
#define VCA_BEHAVIOR_TYPE_WLD					(1<<11)  						//Water Level Detection
#define VCA_BEHAVIOR_TYPE_FLOAT					(1<<12)  						//river clean
#define VCA_BEHAVIOR_TYPE_DREDGE				(1<<13)  						//dredge
#define VCA_BEHAVIOR_TYPE_SGATE					(1<<14)  						//SluiceGate
#define VCA_BEHAVIOR_TYPE_SEDIMENT				(1<<15)							//depth of water

//Device send protocol macros, IE distinguishes between old and new device
//Independent of channel 
#define PROTOCOL_VCALIST					(1<<0)
#define PROTOCOL_DHDEVICESET				(1<<1)
#define PROTOCOL_DHALARMLINK				(1<<2)
#define PROTOCOL_DHOSDREALTIME				(1<<3)
#define PROTOCOL_DHDEVENABLE				(1<<4)
#define PROTOCOL_IPCPLUGANDPLAY				(1<<5)
//Channel-related
#define PROTOCAL_VCA						(1<<0)
#define PROTOCAL_CAMERAPARA					(1<<1)
#define PROTOCAL_DOMEPARA					(1<<2)
#define PROTOCOL_ALARMLINK					(1<<3)
#define PROTOCOL_ALARMSCHEDULE				(1<<4)
#define PROTOCOL_VIDEOROI					(1<<5)
#define PROTOCOL_AUDIOCODERLIST				(1<<6)
#define PROTOCOL_DOMEPTZ					(1<<7)
#define PROTOCOL_MULTIWORDOVERLAY			(1<<8)
#define PROTOCOL_TIMESEGMENT				(1<<9)
#define PROTOCOL_ALARMLINK_EX				(1<<10)

#define PROXY_SEND_ITS						(1<<0)
#define PROXY_SEND_CAMERAPARA				(1<<1)
#define PROXY_SEND_ALARMLINK				(1<<2)
#define PROXY_SEND_ALARMLINK_EX				(1<<3)
#define PROXY_SEND_DOMEPARAM				(1<<4)
#define PROXY_SEND_LIFEMONITOR				(1<<5)
#define PROXY_SEND_DHINFO					(1<<6)
#define PROXY_SEND_PREVIEWREC				(1<<7)
#define PROXY_SEND_ZF						(1<<8)


typedef struct _tagVcaArithmeticList
{
	int iSize;						//size of struct
	int iChannelNo;					//channel number
	int iArithmeticType;			//arithmetic type
	int iEnableCount;				//enable count
	int* piEnableValue;				//enable value
} VcaArithmeticList, *pVcaArithmeticList;

typedef struct _tagDownloadByTimeSpan
{
	int				iSize;							//size of struct
	NVS_FILE_TIME	tStartTime;						//Start Time
	NVS_FILE_TIME	tStopTime;						//Stop Time
	char			cLocalFilename[LEN_256-1];		//Local File Name
	int				iFileType;						//sdv:0; avi:1
} DownloadByTimeSpan, *pDownloadByTimeSpan;

typedef struct _tagPotocolEnable
{
	int iSize;
	int iEnable;
}PotocolEnable, *pPotocolEnable;

#define LIFE_MONITOR_TYPE_MIN					1
#define LIFE_MONITOR_HEART_RATE			LIFE_MONITOR_TYPE_MIN + 0
#define LIFE_MONITOR_OXYGEN				LIFE_MONITOR_TYPE_MIN + 1
#define LIFE_MONITOR_BLOOD_PRESSURE		LIFE_MONITOR_TYPE_MIN + 2
#define LIFE_MONITOR_TYPE_MAX			LIFE_MONITOR_TYPE_MIN + 3	
// Vital signs in real time heart rate and blood oxygen

//Vital signs waveform data
#define MAX_WAVEFORM_GRAM_ID	2
#define MAX_GRAM_SEQ_NUMBER		3

#define MAX_LIFE_MONITOR_CMD_ID	2
typedef struct _tagLifeMonitorConfig
{
	int iSize;
	int iMonType;			//Monitor type,	1:heart rate, 2:oxygen, 3:blood pressure, 0x7FFFFFFF:All(default value)
	int	iCmdID;				//Command ID,	1:Set whether to report IE, 2:Set whether OSD
	int	iCmdContent;		//Command content,	iCmdID=1:1--yes 0--no, iCmdID=2:1--yes,0--no
} LifeMonitorConfig, *pLifeMonitorConfig;

typedef struct _tagOSDExtraInfo
{
	int iSize;
	int iOsdType;		//1:channel name 2:time and date 3:logo color 4:Extra OSD 5:ITS use 6:ITS combine picture
	int	iBgColor;		//back color,"32 is taken low 24 indicates color rgb,representative digitally bgr:Other bits reserved 0x00BBGGRR high eight,do not use."					
	int	iDigitNum;		//Superimposed digits,it shows the median insufficient bit zeros				
	int iAutoLine;
	int iCharEnhance;
	int iCapType;
} OSDExtraInfo, *pOSDExtraInfo;

//Set the illegal-parking dome illegal parking capture style parameter
#define MAX_ITS_IPDCAPMODEL_NUM		8		//The maximum number of violations to capture
#define MAX_ITS_CAPTYPE				2		//The maximum capture type
#define MAX_ITS_INTERVALTIME_NUM	8		//max time interval num 
#define MAX_ITS_CAPTURE_AREA_NUM	8		//max capture model area num
#define MAX_ITS_CAPTURE_CARTYPE		16		//max capture car type

typedef  struct _tagIpdCapModel
{
	int	 iSize;
	int  iChannelNo;
	int  iPreset;
	int  iCapType;
	int  iCapNum;
	int  iCapStyle[MAX_ITS_IPDCAPMODEL_NUM];
	int  iInterval[MAX_ITS_INTERVALTIME_NUM];
	int	 iAreaNo;							//Area no [0~7]
	int	 iIllegalType;	//0: retain compatible old equipment; 1: roadside yellow line parking; 2: stop on the roadside with forbid stop sign; 3:Illegal parking.
	int	 iCapCarType;	//0--reserve,1--bus,2--sedans,3--Wagon,4--van,5--heavy medium truck,6--light truck,7--Motorcycle,8--Pedestrian,9--SUV,10-- medium bus,11--Trailer,12--Hazardous chemicals car
}tITS_IpdCapModel, *ptITS_IpdCapModel;

//Mark the location of the vehicle manually
#define  MAX_ITS_IPDCARPOSITION_NUM   15		//The maximum number of coordinate
#define MAX_REGION_NUM			      16		//The region number
typedef  struct _tagIpdCarPosition
{
	int  iSize;
	int  iChannelNo;
	int  iSceneId;
	int  iRegionID;
	int  iEnabled;
	int  iCount;
	vca_TPoint	m_arrPoint[MAX_ITS_IPDCARPOSITION_NUM];  
}tITS_IpdCarPosition, *ptITS_IpdCarPosition;

typedef  struct _tagITS_LinkPanoPamaCap
{
	int  iSize;
	int  iChannelNo;
	int  iEnable;
	char cLinkCameraIP[LEN_16];
	int  iLinkCameraPort;  
}ITS_LinkPanoPamaCap, *pITS_LinkPanoPamaCap;


#define SERVERINFO_APP_TYPE_MIN			0
#define SERVERINFO_APP_TYPE_RETAIN		(SERVERINFO_APP_TYPE_MIN + 0)
#define	SERVERINFO_APP_TYPE_MULTICAST	(SERVERINFO_APP_TYPE_MIN + 1)  
#define	SERVERINFO_APP_TYPE_MAX			(SERVERINFO_APP_TYPE_MIN + 2)  

typedef struct _tagServerInfo
{
	int		iSize;
	int		iAppID;					//apply type
	char	cAddress[LEN_16];		//server addr				
	int		iProt;					//server port			
}tServerInfo, *ptServerInfo;

#define MAX_SECOND_ALGOTYPE                     10
#define MAX_SUB_FUNC_TYPE						40
#define MAX_SUB_FUNC_TYPE_EX                    80
#define MIN_SUB_FUNC_TYPE_V2                    100
#define MIN_MAIN_FUNC_TYPE						0
#define MAIN_FUNC_TYPE_COLOR2GRAY		(MIN_MAIN_FUNC_TYPE + 0x01)	//color to gray
#define	MAIN_FUNC_TYPE_TRANSCODING		(MIN_MAIN_FUNC_TYPE + 0x02)	//video transcoding
#define	MAIN_FUNC_TYPE_DDNSSERVICE		(MIN_MAIN_FUNC_TYPE + 0x03)	//DDNS service
#define	MAIN_FUNC_TYPE_FORMREPORT		(MIN_MAIN_FUNC_TYPE + 0x04)	//form report
#define MAIN_FUNC_TYPE_COURT			(MIN_MAIN_FUNC_TYPE + 0x05)	//court
#define MAIN_FUNC_TYPE_DECODER			(MIN_MAIN_FUNC_TYPE + 0x06)	//Decoder
#define	MAIN_FUNC_TYPE_DYNAMIC_ROI		(MIN_MAIN_FUNC_TYPE + 0x07)	//dynamic ROI
#define	MAIN_FUNC_TYPE_DOME_PARA		(MIN_MAIN_FUNC_TYPE + 0x08)	//dome para
#define	MAIN_FUNC_TYPE_VCA				(MIN_MAIN_FUNC_TYPE + 0x09)	//VCA
#define MAIN_FUNC_TYPE_NET				(MIN_MAIN_FUNC_TYPE + 0x0A)	//net
#define MAIN_FUNC_TYPE_PERIPHERAL		(MIN_MAIN_FUNC_TYPE + 0x0B)	//peripheralmanage
#define MAIN_FUNC_TYPE_AUDIO_VIDEO		(MIN_MAIN_FUNC_TYPE + 0x0C)	//audio and video
#define MAIN_FUNC_TYPE_ITS				(MIN_MAIN_FUNC_TYPE + 0x0D)	//ITS business
#define MAIN_FUNC_TYPE_TRADE			(MIN_MAIN_FUNC_TYPE + 0x0E)	//trade
#define MAIN_FUNC_TYPE_ALARM			(MIN_MAIN_FUNC_TYPE + 0x0F)	//alarm set
#define MAIN_FUNC_TYPE_SYSTEM 			(MIN_MAIN_FUNC_TYPE + 0x10)	//system set
#define MAIN_FUNC_TYPE_AUTHORITY 		(MIN_MAIN_FUNC_TYPE + 0x11)	//user authority
#define MAIN_FUNC_TYPE_CHANNEL_TALK 	(MIN_MAIN_FUNC_TYPE + 0x12)	//nvr talk with ipc
#define MAIN_FUNC_TYPE_DISK_MANAGE 		(MIN_MAIN_FUNC_TYPE + 0x13)	//disk manage
#define MAIN_FUNC_TYPE_RECORD 			(MIN_MAIN_FUNC_TYPE + 0x14)	//record set
#define MAIN_FUNC_TYPE_ONVIF 			(MIN_MAIN_FUNC_TYPE + 0x15)	//onvif
#define MAIN_FUNC_TYPE_IO_EXTEND 		(MIN_MAIN_FUNC_TYPE + 0x16)	//IO extend
#define MAIN_FUNC_TYPE_DEVICE_MUTEX		(MIN_MAIN_FUNC_TYPE + 0x17)	//Device function mutex
#define MAIN_FUNC_TYPE_NEW_ALERT		(MIN_MAIN_FUNC_TYPE + 0x18)	//Device function mutex
#define MAIN_FUNC_TYPE_NVR_LOCAL_VCA	(MIN_MAIN_FUNC_TYPE + 0x19)	//Back End Vca
#define MAIN_FUNC_TYPE_IRRIGATION		(MIN_MAIN_FUNC_TYPE + 0x1A)	//irrigation func
#define MAIN_FUNC_TYPE_PLUGIN			(MIN_MAIN_FUNC_TYPE + 0x20)	//Plugin
#define MAX_MAIN_FUNC_TYPE				(MIN_MAIN_FUNC_TYPE + 0x21) //no more than 0x40, leave the number 0x40 for NVR

//new main type for NVR start from 0x40 
#define MIN_MAIN_FUNC_TYPE_NVR                  0
#define MAIN_FUNC_TYPE_NVR_SYSTEM				(MIN_MAIN_FUNC_TYPE_NVR + 0x40)   //Nvr system

#define ABILITY_SUB_TYPE_FACETARGET_SYNCDIS       109  

typedef struct _tagFuncAbilityLevel
{
	int		iSize;
	int		iMainFuncType;			//main function type
	int		iSubFuncType;			//sub function type
	char	cParam[LEN_1024];		//Capability Description
} FuncAbilityLevel, *pFuncAbilityLevel;

#define QUERY_REPORT_BY_CHANNEL_NUMBER		0
#define QUERY_REPORT_BY_CHANNEL_LIST		1
#define MAX_QUERY_REPORT_TYPE				2

#define REPORT_FORM_TYPE_DEFAULT		0
#define REPORT_FORM_TYPE_HOUR			1
#define REPORT_FORM_TYPE_DAY			2
#define REPORT_FORM_TYPE_MONTH			3
#define REPORT_FORM_TYPE_YEAR			4

#define QUERY_TYPE_CHANNEL		1
#define QUERY_TYPE_AREA		    2

typedef struct _tagQueryReportForm
{
	int					iSize;			
	int					iFormType;		//form type: 0--默认类型, 1--按小时/V2按日, 2--按日/v2按周, 3--按月, 4--按年
	NVS_FILE_TIME		tBeginTime;		//begin time
	NVS_FILE_TIME		tEndTime;		//end time
	int					iType;			//input para, 0:query by channel number; 1:query by channel list
	int					iChanCount;
	int					iChanList[MAX_TOTAL_CHANNEL_NUM];
	int                 iQueryType;     //ReportType, when iType == 1 vaild ,0: reserve， 1:query by multi channel ， 2: query by area 
} QueryReportForm, *pQueryReportForm;

#define MAX_ONCE_FORM_REPORT_NUM			80
typedef struct _tagRecordReport
{
	int				iChannelNo;				//channel number	
	int				iPushPersonNum;			//enter the number
	int				iPopPersonNum;			//leave the number
	NVS_FILE_TIME	tOccurTime;				//time of occurrence
} RecordReport, *pRecordReport;

typedef struct _tagReportFormResult
{
	int					iSize;			
	int					iFormType;							//form type
	int					iTotalNum;							//total report number
	int					iCurrentNum;						//current report number
	RecordReport		tReport[MAX_ONCE_FORM_REPORT_NUM];
	int					iType;								//input para, 0:query by channel number; 1:query by channel list		
	int                 iQueryType;							//ReportType, when iType == 1 vaild ,0: reserve， 1:query by multi channel ， 2: query by area 
} ReportFormResult, *pReportFormResult;

//===========================HD Para Type==============================================
#define HD_PARA_APERTURE						0						//0-Image Adjustment
#define HD_PARA_WIDE_DYNAMIC					1						//1-Wide dynamic policy
#define HD_PARA_BACK_LIGHT						2						//2-BackLight Compensation
#define HD_PARA_EXPOSAL_TIME					3						//3-Exposal Time
#define HD_PARA_SHUTTER							4						//4-Shutter adjustment
#define HD_PARA_GAIN							5						//5-Gain adjustment
#define HD_PARA_GAMMA							6						//6-Gamma type
#define HD_PARA_SHARPNESS						7						//7-Sharpness adjustment
#define HD_PARA_DNR								8						//8-Noise Reduction adjustment
#define HD_PARA_EXPOSAL_AREA					9						//9-Exposure area
#define HD_PARA_BACK_LIGHT_AREA					10						//10-BLC area
#define HD_PARA_AF_AREA							11						//11-AF area set
#define	HD_PARA_BRIGHTNESS_HD					12						//12-Brightness
#define HD_PARA_WHITE_BALANCE					13						//13-White Balance
#define HD_PARA_JPEG_QUALITY					14						//14-JPEG Quality
#define HD_PARA_LUT_ENABLE						15						//15-LUT enable
#define HD_PARA_AUTO_BRIGHTNESS					16						//16-Enabling automatic brightness adjustment
#define HD_PARA_STRONG_LIGHT					17						//17-Light Suppression
#define HD_PARA_EXPO_MODE						18						//18-Exposure mode
#define HD_PARA_INOUT_DOOR_MODE					19						//19-Indoor and outdoor mode
#define HD_PARA_FOCUS_MODE						20						//20-Focus Mode
#define HD_PARA_MIN_FOCUS_DISTANCE				21						//21-Minimum focusing distance
#define HD_PARA_DAY_NIGHT						22						//22-Day & Night Mode
#define HD_PARA_RESTORE_DEFAULT					23						//23-reset
#define HD_PARA_THROUGH_MIST					24						//24-Through fog
#define HD_PARA_AE_SPEED						25						//25-Automatic exposure adjustment
#define HD_PARA_SMARTIR							26						//26-smartIR
#define HD_PARA_OFFSET_INCREASE					27						//27-Exposure Compensation
#define HD_PARA_SENSETIVE						28						//28-High Sensitivity
#define	HD_PARA_AUTO_SLOW_EXPO					29						//29-Auto slow exposure
#define HD_PARA_AUTO_CONTRAST					30						//30-Auto Contrast
#define HD_PARA_VIDEO_FROST						31						//31-Image freeze
#define HD_PARA_INFRARED						32						//32-Infrared Calibration
#define HD_PARA_COLOR_INCREASE					33						//33-Color gain
#define HD_PARA_COLOR_PHASE						34						//34-Color phase
#define HD_PARA_LIGHTLESS						35						//35-Low light color suppression
#define HD_PARA_FOCUS_TYPE						36						//36-Focus Type
#define HD_PARA_NIGHT_STRENTH					37						//37-Night strengthen
#define HD_PARA_VIGNETTE						38						//38-Vignette compensation
#define HD_PARA_BRIGHTNESS_EX					39						//39-Brightness
#define HD_PARA_CONTRAST_HD						40						//40-Contrast
#define HD_PARA_SATURATION_HD					41						//41-saturation
#define HD_PARA_HUE_HD							42						//42-Chroma
#define HD_PARA_MINI_SHUTTER					43						//43-Minimun exposure time

typedef struct _tagStreamSmooth
{
	int		iSize;			
	int		iStreamNo;
	int		iValue;			//smooth value,range[0,100]
} StreamSmooth, *pStreamSmooth;

typedef struct _tagDevLastError
{
	int		iSize;			
	int		iErrorID;
	char	cErrorInfo[LEN_64]; //lenth <= 63,when iErrorID=0x8001/0x8003,cPara is source ip address
} DevLastError, *pDevLastError;

typedef struct tagFightArithmetic
{
	int	iBufSize;							//size of struct
	VCARule				stRule;				//rule para
	vca_TDisplayParam	stDisplayParam;		//show para
	int					iSensitivity;		//Sensitivity,default 4,range:[0,5]
	int					iAlarmTime;			//alarm time,defalt 3, rangge[0,3600],Unit:seconds
	vca_TPolygonEx		stPoints;			//The number of vertices of the polygon area and coordinate
} FightArithmetic, *pFightArithmetic;

#define MAX_LINK_HTTP_INDEX		16
#define MAX_LINK_HTTP_PARA_NUM	4
typedef struct tagLinkHttpInfo
{
	int	 iBufSize;									//size of struct
	int  iIndex;									//Index,0~15
	int  iEnable;									//1-enable, 0-disable
	char cRecordName[LEN_32];						//record name, less 32 bytes
	char cRecordDecription[LEN_64];					//Record Decription,less 64 bytes
	char cUserName[LEN_32];							//user name, less 32 bytes
	char cUserPassword[LEN_32];						//user password, less 32 bytes
	char cIpAddress[LEN_64];						//IP address or domine name, less 64 bytes
	int  iPort;										//port
	int  iParamNum;									//the num of link info para,most 4
	char cParam[MAX_LINK_HTTP_PARA_NUM][LEN_64];	//link info param
} LinkHttpInfo, HttpTestInfo, *pLinkHttpInfo, *pHttpTestInfo;

#define HOTBACKUP_FLAG_IDLE		0
#define HOTBACKUP_FLAG_BUSY		1
typedef struct _tagSyncTimeRange
{
	int				iBufSize;			//size of struct
	int				iFlagIdle;			//0:idle; 1:busy
	NVS_FILE_TIME	tBeginTime;			//start time
	NVS_FILE_TIME	tEndTime;			//end time
} SyncTimeRange, *pSyncTimeRange;

typedef struct _tagHotBackupFileAttr
{
	int				iBufSize;		//size of struct
	int				iChnNoType;		//0:Absolute channel number; 1:Logical Channel Number			
	NVS_FILE_TIME	tBeginTime;		//record start time						
	NVS_FILE_TIME	tEndTime;		//record end time						
	int				iFileSize;		//record file size,the unit is Byte					
} HotBackupFileAttr, *pHotBackupFileAttr;

typedef struct _tagHotBackupFtpInfo
{
	int				iBufSize;					//size of struct
	char			cFileName[LEN_64];			//reocrd file name				
	char			cFtpPath[LEN_64];			//FTP path			
	char			cFtpUserName[LEN_32];		//FTP username				
	char			cFtpPassword[LEN_32];		//FTP password				
} HotBackupFtpInfo, *pHotBackupFtpInfo;

typedef struct _tagHotBackupSYNCResult
{
	int				iBufSize;				//size of struct
	int				iResult;				//the result of work device recive file
} HotBackupSYNCResult, *pHotBackupSYNCResult;

typedef struct _tagSceneTemplateMap
{
	int				iBufSize;					//size of struct
	int				iSceneId;					//scene ID	0~15
	int				iTemplateIndex;				//TemplateIndex,range:0-7; 0x7FFFFFFF:not support 
} SceneTemplateMap, *pSceneTemplateMap;



#define MAX_SUPPORT_DEVICE_NUM 32
#define MAX_SUPPORT_DEVICE_NAME_LEN 32
#define MAX_COM_FORMATE_LEN  32
#define MAX_OVERLAY_TEXT_LEN 256
#define MAX_PPPOE_ACCOUNT_LEN 64
#define MAX_PPPOE_PASSWORD_LEN 64
#define MAX_DOMAIN_LEN 32
#define MAX_PHONE_NUM_LEN 24
//Battery Info
typedef struct tagBatteryInfo
{
	int				iSize;			
	int				iChannelNo;		//ChannelNo
	int				iChargeType;		//IfCharge
	int  			iBatteryPercent;  //Battery Percent
}tBatteryInfo,*ptBatteryInfo;

#define			MAX_AUDIO_MUTE_NUM				2	//Max mute dev num
typedef struct  tagDevAudioMute
{
	int				iSize;				//Buf Size
	int				iDevNo;				//Mute Dev No,0-Reserve，1-HDMI(4K）			
	int				iMute;				//Mute State,0-Non Mute，1-Mute
	int				iVolume;   			//Volume Value, 0-255
}tDevAudioMute, *ptDevAudioMute;

#define FUNTION_BIT_VCA						(1 << 0)
#define FUNTION_BIT_LIGHT					(1 << 1)
#define FUNTION_BIT_NET_OPTIMIZE			(1 << 2)
#define FUNTION_VIDEO_OPTIMIZE				(1 << 3)
#define FUNTION_DISABLE_PROCESS_FRAME		(1 << 4)
#define FUNTION_DISABLE_LOGON_BY_NEW_CLIENT	(1 << 5)	
#define FUNTION_FULL_LOGON					(1 << 6)	
#define FUNTION_SSL_ENCRPTY					(1 << 7)
#define FUNTION_CONVERT_USER				(1 << 8)
#define FUNTION_LOGONFAILED_RECONNECT		(1 << 9)
#define FUNTION_SSL_ENCRPTY_NVR				(1 << 10)
#define FUNTION_SAVE_ALARMLINK				(1 << 11)

#define MAX_ALARM_LINK_INTERVAL_TYPE	2			//Max alarm link interval link type
#define	ALARM_LINK_INTERVAL_ALL_TYPE	255			//Max alarm link interval link type all type
typedef struct tagAlarmLinkInterVal 
{
	int				iSize;				//Size
	int				iEnable;			//0:Disable 1:Enable
	int				iLinkType;			//1:link capture 255:no distinguish
	int    	 		iInterval;     		//0~300
}AlarmLinkInterVal ,*pAlarmLinkInterVal;

typedef struct tagPrecedeDelaySnap 
{
	int				iSize;				//Size
	int				iPrecedeNum;		//precede sanp number, 1-4
	int    	 		iDelayNum;     		//delay sanp number,1-4
}PrecedeDelaySnap  ,*pPrecedeDelaySnap;

#define MAX_VOLUME_OUT_TYPE				3
typedef struct tagVolumeOut 
{
	int				iSize;
	int				iType;
	int    	 		iValue;
}VolumeOut  ,*pVolumeOut;

typedef struct tagVCAProtect
{
	int				iSize;			
	VCARule			stRule;					//Rule general parameter
	vca_TDisplayParam	stDisplayParam;		//Display parameter
	int				iTargetTypeCheck;		//Target type,0:Not distinguish 1:Distinguish between people 2:Distinguish between car 3:Distinguish between people and car
	int				iMode;					//Detection mode，0:Invade 1:entrance 2:Leave
	int				iMinDistance;			//Minimum distance
	int				iMinTime;				//Shortest time
	int				iType;					//Whether to do directional restrictions
	int				iDirection;				//The direction angle is forbidden
	int				iMiniSize;				//Minimum size, [0, 100] Default 5
	int				iMaxSize;				//Maximum size, [0, 100] Default 30
	int				iDisplayTarget;			//Whether the target box is displayed, [0, 100] Default 5
	int				iResortTime;			//The unit is milliseconds
	vca_TPolygonEx	stPoints;				//The number of vertex in the polygon area,and the coordinates
}VCAProtect,*pVCAProtect;

typedef struct tagPlayAudioSample
{
	int				iSize;			
	int				iType;					//Audio type
	int				iSampleNo;				//Recording number
	int				iCtrl;					//Ctrl type
}PlayAudioSample,*pPlayAudioSample;

typedef struct tagSendVideoStream
{
	int				iSize;
	int				iEnable;
}SendVideoStream, *pSendVideoStream;

#ifndef MAX_PATH
#define MAX_PATH 260	/**< 最大路径长度 */
#endif

typedef struct _tagBigPackUpgrade
{
	int	  iSize;
	char  cDevIp[LEN_16];
	char  cFileName[MAX_PATH]; 
	int	  iChipIndex;			//Chip number,The protocol extension supports multi chip network burning. In order to be compatible with the old version, the main CPU upgrade does not need this field. The first value from the slice starts with 1. Each batch burn the same location of the chip
}BigPackUpgrade, *PBigPackUpgrade;


typedef struct tagControlEmail
{
	int				iSize;
	int				iEnable;
	int				iInterval;
}ControlEmail, *pControlEmail;

typedef struct tagItsDetectMode 
{
	int				iSize;						//Size
	int				iDetectMode;	
	int				iIntervalTime;	
}ItsDetectMode, *pItsDetectMode;

#define DDNSTYPE_NORMAL		0
#define DDNSTYPE_EASY		1
#define MAX_DDNSEX_TYPE     2
typedef struct tagDDNSParaEx
{
	int				iSize;
	char			cUserName[LEN_128];
	char			cPassWord[LEN_128];
	char			cNVSName[LEN_128];
	char			cDomainName[LEN_128];
	int				iPort;
	int				iEnable;
	int				iType;	//0:NormalDDNS, 1:EasyDDNS
}DDNSParaEx, *pDDNSParaEx;

#define	PARAM_CHANNEL_ALL			0x7FFFFFFF  //The parameter settings for all channels are acquired

#define	ZF_CHAN_TYPE_MIN			0
#define	ZF_CHAN_TYPE_VIDEO			(ZF_CHAN_TYPE_MIN + 0)
#define	ZF_CHAN_TYPE_PROOF_IN		(ZF_CHAN_TYPE_MIN + 1)
#define	ZF_CHAN_TYPE_MIC			(ZF_CHAN_TYPE_MIN + 2)
#define	ZF_CHAN_TYPE_PROOF_OUT		(ZF_CHAN_TYPE_MIN + 3)
#define	ZF_CHAN_TYPE_MAX			(ZF_CHAN_TYPE_MIN + 4)


//CommonEnable，Referred to CE，General enable parameter change message
//Compatible with old macros
#define	PARA_CE_EIS					PARA_EIS			//Electronic anti-shake
#define	PARA_CE_SVC					PARA_SVC			//svc
#define	PARA_CE_TRENDS_ROI			PARA_DEV_TRENDS_ROI	//Dynamtic ROI
#define	PARA_CE_VIDEO_REVERSE		PARA_COMMON_ENABLE_VIDEO_REVERSE	//Video filp
#define	PARA_CE_DISTORTION_REVISE	PARA_COMMONENABLE_DISTORTION_REVISE	//Distortion correction
#define	PARA_CE_SNMP				PARA_SERVICE_SNMP	//SNMP
#define	PARA_CE_QQ_SERVICE			PARA_QQ_SERVICE		//Tencen things enable
#define	PARA_CE_DISKGROUP			PARA_DISKGROUP		//Toggle disk group matching strategy
#define	PARA_CE_DISKQUOTA			PARA_DISKQUOTA		//Quota mode
#define	PARA_CE_RAID				PARA_RAID_ENABLE	//RAID function
#define	PARA_CE_FAN_TEMP			PARA_FAN_TEMP_CONTRO//Fan temperature control
#define	PARA_CE_WHITELIGHT			PARA_FAN_WHITELIGHT	//White light is supported
#define	PARA_CE_MTU					PARA_NETCONNECT_INFO_MTU//MTU setting
#define	PARA_CE_PUBLIC_NETWORK		PARA_COMMONENABLE_PUBLIC_NETWORK //Public network connection status
#define	PARA_CE_ITS_ILLEGAL_PARK	PARA_COMMONENABLE_IllegalPark	 //illegal parking system capture type
#define	PARA_CE_ITS_STJ1RADAR_MODE	PARA_ITS_STJ1RADAR_MODE	 //STJ1 Radar mode
#define	PARA_CE_GPS_CALIBRATION		PARA_GPS_CALIBRATION		//Whether to enable GPS calibration
#define	PARA_CE_ALARM_THRESHOLD		PARA_ALARM_THRESHOLD		//Indicates the equipment mortgage alarm threshold
#define	PARA_CE_SHUTDOWN_THRESHOLD	PARA_SHUTDOWN_THRESHOLD		//Low voltage shutdown threshold
#define	PARA_CE_WORKDEV_BACKUP		PARA_WORKDEV_BACKUP_ENABLE	//N+1Hot standby
#define	PARA_CE_FORBID_CHN			PARA_DEV_FORBID_CHN	//Disable the next N channels
#define	PARA_CE_EVENT_TEMPLATE		PARA_DEV_EVENT		//The event template is enabled
#define	PARA_CE_TELNET				PARA_DEV_TELNET		//telnet enable
#define	PARA_CE_DISKPOPSEAL			PARA_DEV_DISKPOPSEAL//CD-ROM disk pop and closing state
#define	PARA_CE_VCA_CARCHECK		PARA_DEV_VCA_CARCHECK//Vehicles pass detection enable
#define	PARA_CE_PERIPHERAL_MANAGE	PARA_COMMONENABLE_PERIPHERAL_MANAGE//Peripheral management universal enable
//Generic enalbe ID message macro definition
#define PARA_CE_BASE				3000
#define	PARA_CE_VO						(PARA_CE_BASE + 0)	//VO Control
#define	PARA_CE_DENOISE					(PARA_CE_BASE + 1)	//Audio noise reduction
#define	PARA_CE_DOT_MATRIX				(PARA_CE_BASE + 2)	//Dot matrix font
#define	PARA_CE_OSD_VECTOR				(PARA_CE_BASE + 3)	//Vector font
#define	PARA_CE_ALARM_MD				(PARA_CE_BASE + 4)	//MD Alarm
#define	PARA_CE_ALARM_OD				(PARA_CE_BASE + 5)	//OD Alarm
#define	PARA_CE_HASH					(PARA_CE_BASE + 6)	//Hash check
#define	PARA_CE_AUDIO_MAKE				(PARA_CE_BASE + 7)	//Audio file gengeration
#define	PARA_CE_AUDIO_BURN				(PARA_CE_BASE + 8)	//Audio file burn
#define	PARA_CE_AUDIO_BACK				(PARA_CE_BASE + 9)	//Audio file backup
#define	PARA_CE_BURN_FORMAT				(PARA_CE_BASE + 10)	//Real time recording file format
#define	PARA_CE_SMTP					(PARA_CE_BASE + 11)	//SMTP
#define	PARA_CE_RTSP					(PARA_CE_BASE + 12)	//RTSP
#define	PARA_CE_FAN						(PARA_CE_BASE + 13)	//Fan remote control
#define	PARA_CE_LOCAL_VCA				(PARA_CE_BASE + 14)	//Local intelligent analysis
#define	PARA_CE_ITS						(PARA_CE_BASE + 15)	//Traffic related
#define	PARA_CE_ITS_SIGNAL_LIGHT		(PARA_CE_BASE + 16)	//Signals synchronize
#define	PARA_CE_ITS_BUSINESS			(PARA_CE_BASE + 17)	//Traffic service enable
#define	PARA_CE_ANR						(PARA_CE_BASE + 18)	//ANR enable
#define	PARA_CE_ABF						(PARA_CE_BASE + 19)	//ABF
#define	PARA_CE_ITS_PICSTREAM			(PARA_CE_BASE + 20)	//picture stream
#define	PARA_CE_NVR_ANR					(PARA_CE_BASE + 21)	//NVR ANR
#define PARA_CE_RED_OUTDOOR_LIGHT		(PARA_CE_BASE + 22) //Infrared light control
#define PARA_CE_WHITE_LIGHT				(PARA_CE_BASE + 23) //White light control
#define PARA_CE_LAND_LOCK				(PARA_CE_BASE + 24) //Land lock control
#define PARA_CE_VIDEO_ENCODE			(PARA_CE_BASE + 25) //Video compression
//#define PARA_CE_ITS_CAP_TYPE			(PARA_CE_BASE + 27)
#define PARA_CE_SOUND_PICKUP			(PARA_CE_BASE + 28)	//Pickup power switch
#define PARA_CE_MULTICAST				(PARA_CE_BASE + 29)	//Multicase enable
#define PARA_CE_ECPOOL					(PARA_CE_BASE + 30)	//Echo suppression
#define PARA_CE_ERECT_MODE				(PARA_CE_BASE + 31)	//Installation mode
#define PARA_CE_OBLIGATE_MODE			(PARA_CE_BASE + 32)	//Obligate mode
#define PARA_CE_LIGHT_LOGON				(PARA_CE_BASE + 33)	//light logon
#define PARA_CE_UPNP_STATUS				(PARA_CE_BASE + 34)
#define PARA_CE_WALL_ANGLE				(PARA_CE_BASE + 35)
#define PARA_CE_ALERT_TEMPLATE			(PARA_CE_BASE + 36)
#define PARA_CE_VIDEO_LDC				(PARA_CE_BASE + 37)
#define PARA_CE_OSD_CHANNEL_ENABLE		(PARA_CE_BASE + 38)
#define PARA_HU_TEM_INTERVAL			(PARA_CE_BASE + 39)
#define PARA_CE_REGISTRY_STATUS			(PARA_CE_BASE + 40)
#define PARA_CE_HUN_TEM_UPLOAD			(PARA_CE_BASE + 41)
#define PARA_CE_RED_LIGHT_CTRL_WAY		(PARA_CE_BASE + 42)
#define PARA_CE_ONVIF_PSWCHK			(PARA_CE_BASE + 43)
#define PARA_CE_PREVIEW_PREFERENCE		(PARA_CE_BASE + 44)
#define PARA_CE_VCA_PIC_STREAM			(PARA_CE_BASE + 45)
#define PARA_CE_VIDEO_SMOOTH			(PARA_CE_BASE + 46)
#define PARA_CE_RTSP_ENCRYPT			(PARA_CE_BASE + 47)
#define PARA_CE_ONVIF_SEARCH			(PARA_CE_BASE + 48)
#define PARA_CE_KS_FACE_DETECT			(PARA_CE_BASE + 49)
#define PARA_CE_VIDEO_OUT_PREFER		(PARA_CE_BASE + 50)
#define PARA_CE_NOTIFY_EXCEPTION		(PARA_CE_BASE + 51)
#define PARA_CE_POE_LONG_RETICLE		(PARA_CE_BASE + 52)
#define PARA_CE_NEW_LOGON				(PARA_CE_BASE + 53)
#define PARA_CE_AUTO_S_PLUS				(PARA_CE_BASE + 54)
#define PARA_CE_LICENSE_DISPLAY_LINE	(PARA_CE_BASE + 55)
#define PARA_CE_ITS_COUPEDESTRAINS		(PARA_CE_BASE + 56)
#define PARA_CE_ITS_DIRPEDESTRAINS		(PARA_CE_BASE + 57)
#define PARA_CE_DZ_ALGO_DEBUG			(PARA_CE_BASE + 58)
#define PARA_CE_RAINFALL_ALARM			(PARA_CE_BASE + 59)
#define PARA_CE_ALERTWATER_ALARM		(PARA_CE_BASE + 60)
#define PARA_CE_ITS_DECTTION_TIMEOUT	(PARA_CE_BASE + 61)
#define PARA_CE_POWER_UPLOAD_INTERVAL	(PARA_CE_BASE + 62)
#define PARA_CE_HTTP					(PARA_CE_BASE + 63)
#define PARA_CE_HTTPS					(PARA_CE_BASE + 64)
#define PARA_CE_NVR_BROADCAST_MSG		(PARA_CE_BASE + 65)
#define PARA_CE_HORN					(PARA_CE_BASE + 66)
#define PARA_CE_PIC_ATTRIBUTE			(PARA_CE_BASE + 67)
#define PARA_CE_TARGET_DETECTION		(PARA_CE_BASE + 68)
#define PARA_CE_ONVIF_SUPPORTH265		(PARA_CE_BASE + 69)
#define PARA_CE_VCA_RESOURCE			(PARA_CE_BASE + 70)
#define PARA_CE_BUZZER_CONTROL			(PARA_CE_BASE + 71)
#define PARA_CE_IP_ADAPTIVE				(PARA_CE_BASE + 72)
#define PARA_CE_NVR_CLOUD_AUTODETECT	(PARA_CE_BASE + 73)
#define PARA_CE_IPC_CLOUD_AUTODETECT	(PARA_CE_BASE + 74)
#define PARA_CE_AUTHORITYCONTROL		(PARA_CE_BASE + 75)	
#define PARA_CE_TEMDETECT            	(PARA_CE_BASE + 76)
#define PARA_CE_WATER_SPEED_ENABLE		(PARA_CE_BASE + 77)
#define PARA_CE_MANUALCTRL_RADARPOWER	(PARA_CE_BASE + 78)
#define PARA_CE_LAMP_BRIGHTNESS			(PARA_CE_BASE + 79)
#define PARA_CE_LASER_PROPERTY			(PARA_CE_BASE + 80)
#define PARA_CE_LOCK_PTZ				(PARA_CE_BASE + 81)
#define PARA_CE__WATER_SPEED_DATA_SRC	(PARA_CE_BASE + 82)
#define PARA_CE_OPENFDDRF				(PARA_CE_BASE + 83)
#define PARA_CE_OPENTDDRF				(PARA_CE_BASE + 84)
#define PARA_CE_STREAM_TYPE				(PARA_CE_BASE + 85)
#define PARA_CE_MAX_CONNECTION			(PARA_CE_BASE + 86)
#define PARA_CE_EXIT_WITHOUT_OPERATION	(PARA_CE_BASE + 87)
#define PARA_CE_FLOW_DATA_NUM			(PARA_CE_BASE + 88)
#define PARA_CE_RESET_FLOW_DATA			(PARA_CE_BASE + 89)
#define PARA_CE_ONVIF_SLICE_TIMING		(PARA_CE_BASE + 90)
#define PARA_CE_SELF_CHECK				(PARA_CE_BASE + 91)
#define PARA_CE_SOLAR_CALENDAR			(PARA_CE_BASE + 92)
#define PARA_CE_REPORT_PUNCTUALLY		(PARA_CE_BASE + 93)
#define PARA_CE_SELF_INIT_VOICE			(PARA_CE_BASE + 94)
#define PARA_CE_RESET_WATERLEVEL_DATA	(PARA_CE_BASE + 95)
#define PARA_CE_HIGH_MODIFY_SNAP		(PARA_CE_BASE + 96)
#define PARA_CE_28181NETWORKTYPE		(PARA_CE_BASE + 97)
#define PARA_CE_CHANGELENSSENSOR		(PARA_CE_BASE + 98)
#define PARA_CE_ALIPAYMENT_ALGORITHM	(PARA_CE_BASE + 99)
#define PARA_CE_LOCAL_AUDIO_INPUT_MODE	(PARA_CE_BASE + 102)
#define PARA_CE_SET_MIXCOMPOSITION		(PARA_CE_BASE + 103)
#define PARA_CE_PREVIEW_MODE			(PARA_CE_BASE + 104)
#define PARA_CE_SET_SCREEN_OUTPUTMODE	(PARA_CE_BASE + 105)
#define PARA_CE_BRIGHTNESS_LASER_FILL	(PARA_CE_BASE + 106)
#define PARA_CE_CENTER_POINTING_ANGLE	(PARA_CE_BASE + 107)
#define PARA_CE_MULTI_SEARCH_SWITCH		(PARA_CE_BASE + 108)
#define PARA_CE_ONE_CLICK_DISARM		(PARA_CE_BASE + 109)
#define PARA_CE_PREVIEW_DASH_ENABLE		(PARA_CE_BASE + 110)
#define PARA_CE_CAMERA_PROPERTIES		(PARA_CE_BASE + 111)
#define PARA_CE_PUSH_TRACK_INFORMATION_INTERVAL		(PARA_CE_BASE + 112)
#define PARA_CE_HDMI_LOW_DELAY_MODE		(PARA_CE_BASE + 113)
#define PARA_CE_CALIBRAT_REFER_ENABLE	(PARA_CE_BASE + 114)
#define PARA_CE_LOCK_ZOOM				(PARA_CE_BASE + 115)
#define PARA_CE_PSEUDO_TRANSPARENCY_RATIO			(PARA_CE_BASE + 116)
#define PARA_CE_SWITCH_ANALYZE_TARGET_TRAJECTORY	(PARA_CE_BASE + 117)
#define PARA_CE_SWITCH_PSEUDO_COLORFUNCTION			(PARA_CE_BASE + 118)
#define PARA_CE_ANALYZE_TARGET_TRAJECTORY_ENABLE	(PARA_CE_BASE + 119)
#define PARA_CE_TRACK_UNIQUE_ENABLE		(PARA_CE_BASE + 120)
#define PARA_CE_ORIGIN_DISTANCE_RIVER	(PARA_CE_BASE + 121)
#define PARA_CE_ALARM_LIGHT_CONTROL		(PARA_CE_BASE + 122)
#define PARA_CE_LASER_ANGLE_CONTROL		(PARA_CE_BASE + 123)
#define PARA_CE_PTZ_RESET_ENABLE		(PARA_CE_BASE + 124)
#define PARA_CE_REMOTE_UPHOLD_PORT		(PARA_CE_BASE + 125)
#define PARA_CE_LIMIT_LASER_ON			(PARA_CE_BASE + 126)
#define PARA_CE_BURN_PLAYER_OPTION		(PARA_CE_BASE + 127)
#define PARA_CE_JUDGE_BEHAVIOR_ANALYZE_ALG_ENABLE	(PARA_CE_BASE + 128)
#define PARA_CE_DEV_HD_MODE				(PARA_CE_BASE + 129)
#define PARA_CE_ID_NVR_OUT_VOLUME		(PARA_CE_BASE + 130)
#define PARA_CE_ITS_TRANSMIT_PICSTREAM	(PARA_CE_BASE + 131)
#define PARA_CE_THIRD_PARTITION			(PARA_CE_BASE + 132)
#define	PARA_CE_DISK_DORMANCY			(PARA_CE_BASE + 133)
#define	PARA_CE_NVRVIDEO_OCCLUSION  	(PARA_CE_BASE + 134)
#define	PARA_CE_FILL_MODE				(PARA_CE_BASE + 135)
#define	PARA_CE_IMAGE_MODE				(PARA_CE_BASE + 136)
#define	PARA_CE_SCENE_NAME_OVERLAY		(PARA_CE_BASE + 137)
#define PARA_CE_REMOTE_OPEN_DOOR		(PARA_CE_BASE + 138)
#define PARA_CE_WATEROSD_INTERVAL		(PARA_CE_BASE + 139)	//水利字符叠加滚动间隔参数改变消息
#define PARA_CE_WATERBRUSH_MANUAL		(PARA_CE_BASE + 140)	//水质清洁刷手动控制参数改变消息
#define PARA_CE_FAST_FACE_RECOGNITION				(PARA_CE_BASE + 141)	//快捷人脸识别参数改变消息
#define PARA_CE_ACTIVEPOLICY_ENABLEPARA_EDIT		(PARA_CE_BASE + 142)	//主动策略使能参数可编辑参数改变消息
#define PARA_CE_ACTIVEPOLICY_LINKPARA_EDIT			(PARA_CE_BASE + 143)	//主动策略联动人形白灯参数可编辑参数改变消息
#define PARA_CE_WATERQUALITY_DETECT_MANUAL		                (PARA_CE_BASE + 144)	//水质手动检测开启关闭参数改变消息
#define PARA_CE_WATERQUALITY_HYPERSPECTRAL_LAMP_BRITGHTNESS		(PARA_CE_BASE + 145)	//水质高光谱灯手动开启及亮度值参数改变消息
#define PARA_CE_WATERQUALITY_UV_LAMP_BRITGHTNESS		        (PARA_CE_BASE + 146)	//水质紫外灯手动开启及亮度值参数改变消息
#define PARA_CE_ADJUST_IPC_TIME									(PARA_CE_BASE + 147)	//是否开启nvr对ipc校时功能，参数改变消息
#define PARA_CE_DUAL_USERS_AUTHENTICATION						(PARA_CE_BASE + 148)	//是否开启双用户认证使能（ZXYHDZ）
#define PARA_CE_USB_FLASH_BACKUP								(PARA_CE_BASE + 149)	//U盘备份功能是否使能（ZXYHDZ）
#define PARA_CE_LOWPOWER_ENABLE			(PARA_CE_BASE + 150)	//延时低功耗设置使能参数改变消息
#define PARA_CE_LOWPOWER_ENTER_TIME		(PARA_CE_BASE + 151)	//延时低功耗时间设置参数改变消息

//Lane capture type binding time period advanced configuration
typedef struct tagITSChnlCapSet
{
	int iSize;								//Structure size
	int iRoadwayID;                         //Road id   Maximum support 4 lanes：0-3
	int iSceneID;							// Scene ID(0~15) 
	int iIndex;								//Time range number,0-7，Supports up to 8 time slots
	int iTimeRange;							//Time range
	int iCapType;							//Drive capture type
	int iCapTypeV2;							//Drive capture type V2
}ITSChnlCapSet, *pITSChnlCapSet;


#define MAX_ITS_CAP_CAR_TYPE			16				//the maximum number of bound vehicle type supported by the lane vehicle type and the capture type binding

//Lane vehicle type,and capture type binding advanced configuration
typedef struct tagITSChnlCarCapTpye
{
	int iSize;								//Structure size
	int iRoadwayID;                         //Road id   Maximum support 4 lanes：0-3
	int iSceneID;							// Scene ID(0~15) 
	int iCarType;							//Car type
	int iCapType;							//Drive capture type
	int iCapTypeV2;							//Drive capture type V2
}ITSChnlCarCapType, *pITSChnlCarCapTpye;

#define ALALRM_STATE_CLEAR		0	//Eliminate alarms
#define ALALRM_STATE_REPORT		1	//Alarm

//ITS delete picture strategy
typedef struct tagITSDelPicStrategy
{
	int iSize;							//size of struct
	int iDelNum;                        //Number delete item
	int iKeepDay;						//Upload recordkeeping Days
	NVS_FILE_TIME tDelStartTime;		//Delete recording start time
	NVS_FILE_TIME tDelStopTime;			//Delete Record End Time
	int iMaxSaveCount;					//Max save count
} ITSDelPicStrategy, *pITSDelPicStrategy;

//ITS Host number
typedef struct tagITSHostNumber
{
	int iSize;							//size of struct
	char cHostNumber[LEN_32];			//Host number
} ITSHostNumber, *pITSHostNumber;

#define MAX_ITS_DEVICE_COUNT	40
//ITS Device number
typedef struct tagITSDeviceNumber
{
	int  iSize;					//size of struct
	char cDevNumber[LEN_32];	//Device number
	char cDevID[LEN_128];		//Device ID
	char cDevName[LEN_256];		//Device Name
	int	 iOrderId;				//
} ITSDeviceNumber, *pITSDeviceNumber;

typedef struct tagITSPlatformCfgInfo
{
	int  iSize;
	char cPlatID[LEN_64];		//Platform ID
	char cPlatIp[LEN_32];		//Platform IP
	int  iPlatPort;				//Platform port
} ITSPlatformCfgInfo, *pITSPlatformCfgInfo;

#define PLATFORM_VERSION_V7_0	0	//7.0 Version
#define PLATFORM_VERSION_V7_1	1	//7.1 Version
typedef struct tagITSPicUploadCfgInfo
{
	int  iSize;					//size of struct
	char cPlatIp[LEN_32];		//Platform IP
	int  iPlatPort;				//Platform port
	int  iVersion;
	int  iResponse;
	int  iCustomFileName;
	char cSeparator[LEN_16];
	char cNameSection[LEN_64];
} ITSPicUploadCfgInfo, *pITSPicUploadCfgInfo;

#define MAX_ITS_CROSS_COUNT		64	//max cross count
#define ITS_CROSS_OPT_RESERVE	0	//reserve
#define ITS_CROSS_OPT_ADD		1	//add
#define ITS_CROSS_OPT_DELETE	2	//delete
#define ITS_CROSS_OPT_MODIFY	3	//modify
typedef struct tagITSCrossInfo
{
	int  iSize;					//size of struct
	int  iOpt;					//0:reserve,1:add,2:delete,3:modify
	int  iOrderId;
	char cCrossNumber[LEN_64];	//corss number
	char cCrossName[LEN_64];	//cross name
	int  iResult;				//only get interface use,0:success,1:failed
} ITSCrossInfo, *pITSCrossInfo;

#define MAX_ITS_LANE_COUNT		64	//max lane count by one cross
#define ITS_LANE_OPT_RESERVE	0	//reserve
#define ITS_LANE_OPT_ADD		1	//add
#define ITS_LANE_OPT_DELETE		2	//delete
#define ITS_LANE_OPT_MODIFY		3	//modify
typedef struct tagITSLaneInfo
{
	int  iSize;					//size of struct
	int  iOpt;					//0:reserve,1:add,2:delete,3:modify
	int  iOrderId;
	char cCrossNumber[LEN_64];	//corss number
	int  iLaneID;				//lane ID
	char cLaneName[LEN_64];		//lane name
	int  iDirection;
	int  iType;
	int  iDevOrderId;			//device order id
	int  iRegionID;				//check region ID
	int  iResult;				//only get interface use,0:success,1:failed
} ITSLaneInfo, *pITSLaneInfo;

#define MAX_UPLOAD_COUNT 16
typedef struct tagITSQueryData
{
	int  iSize;						//size of struct
	NVS_FILE_TIME tStartTime;		//start time
	NVS_FILE_TIME tStopTime;		//End Time
	int  iBeginIndex;				//Begin index
	int  iEndIndex;					//End index
	char cCrossNumber[LEN_64];		//corss number
	int  iLaneID;					//lane ID
	int  iUpload;					//Upload Status
	int  iVehicleType;				//Vehicle Type
	int  iBrand;					//Vehicle Brands
	int  iColor;					//Vehicle color
	int  iDirection;				//Direction of travel
	int  iIllegal;					//Criminal Type, 0x7fffffff:all
	char cLicense[LEN_32];			//number plate
	int  iMaxOrderId;
	int  iTotalCount;
	int  iDeviceID;         //Device ID
	int  iMainUpload;
	int  iSuborUpload;
	int  iUploadState[MAX_UPLOAD_COUNT];
	int  iSubBrand;
	int  iAlarmType;
	int  iSessionID;				//Output para, SDK make Session Id.
	int  iQueryType;				//0:Monitoring data，1：Brakes data 2：all
	int	 iChannelNo;						//ChannelNO
} ITSQueryData, *pITSQueryData;

typedef struct tagITSQueryCountRet
{
	int			iQueryTotalCount;
	int			iQueryType;			//0:Monitoring data，1：Brakes data 2：all
	int			iChannelNo;
}ITSQueryCountRet,*pITSQueryCountRet;

#define MAX_ITS_QUERYDATA_PAGE_SIZE		40
#define MAX_ITS_QUERYDATA_PIC_COUNT		16
#define MAX_ITS_LICENCE_PIC_COUNT		4
#define MAX_ITS_FACE_PIC_COUNT			8
typedef struct tagITSQueryDataRecord
{
	int  iSize;						//size of struct
	int  iSessionID;				//Session Id
	int  iOrderId;
	int  iChanNo;					//channel number
	char cCrossNumber[LEN_64];		//corss number
	int  iLaneID;					//lane ID
	NVS_FILE_TIME  tTime;			//Bayonet alarm generation time
	int  iUpload;					//Upload Status
	int  iVehicleType;				//Vehicle Type
	int  iBrand;					//Vehicle Brands
	int  iColor;					//Vehicle color
	int  iDirection;				//Direction of travel
	int  iIllegal;					//Criminal Type, 0x7fffffff:all
	char cLicense[LEN_32];			//number plate
	int  iPicNum;
	char cPicName[MAX_ITS_QUERYDATA_PIC_COUNT][LEN_64];
	int  iLicencePicNum;
	char cLicencePicName[MAX_ITS_LICENCE_PIC_COUNT][LEN_64];
	int  iFacePicNum;
	char cFacePicName[MAX_ITS_FACE_PIC_COUNT][LEN_64];
	int  iMainUpload;
	int	 iSuborUpload;
	int	 iUploadState[LEN_16];
	int	 iLicenseCol;
	char cMainDriverSex;
	char cCopilotSex;
	float fSpeed;
	int  iDeviceID;
	int  iLimitSpeed;
	char cViolationType[LEN_16];
	NVS_FILE_TIME  tSnapTime[MAX_ITS_QUERYDATA_PIC_COUNT];
	short sSnapMsTime[MAX_ITS_QUERYDATA_PIC_COUNT];
	char  cSnapCode[MAX_ITS_QUERYDATA_PIC_COUNT][LEN_36];
	int   iSubBrand;
	int   iAlarmType;
	int   iQueryType;				//0:Monitoring data，1：Brakes data 2：all
} ITSQueryDataRecord, *pITSQueryDataRecord;

typedef struct tagITSModifyData
{
	int  iSize;						//size of struct
	int  iOrderId;
	int  iVehicleType;				//Vehicle Type
	int  iBrand;					//Vehicle Brands
	int  iColor;					//Vehicle color
	int  iIllegal;					//Criminal Type, 0x7fffffff:all
	char cLicense[LEN_32];			//number plate
	char cPicName[LEN_64];
	int  iLicenseCol;
	int  iSubBrand;
	int  iResult;					//only get interface use,0:success,1:failed
} ITSModifyData, *pITSModifyData;

#define MAX_ITS_DELETE_DATA_COUNT	40
typedef struct tagITSDeleteData
{
	int iSize;						//size of struct
	int iRecordCount;
	int iOrderId[MAX_ITS_DELETE_DATA_COUNT];
} ITSDeleteData, *pITSDeleteData;

typedef struct tagITSGetDataResult
{
	int iSize;						//size of struct
	int iResult;
} ITSGetDataResult, *pITSGetDataResult;

//Traffic camera type,reuse since the dome machine movement model
typedef struct tagITSCamraType
{
	int iSize;
	int iITSCamraType;	
	int iITSZoomRatio;
}ITSCamraType, *pITSCamraType;

//max route nat port type，0:reserve，1:http，2:private data，3：private udp
#define ROUTENAT_PORT_MIN								0
#define ROUTENAT_PORT_HTTP					(ROUTENAT_PORT_MIN + 1)
#define ROUTENAT_PORT_PRIVATE_DATA			(ROUTENAT_PORT_MIN + 2)
#define ROUTENAT_PORT_PRIVATE_UDP			(ROUTENAT_PORT_MIN + 3)
#define ROUTENAT_PORT_RADAR_PERIPHERAL		(ROUTENAT_PORT_MIN + 4)
#define ROUTENAT_PORT_MAX					(ROUTENAT_PORT_MIN + 5)
typedef struct tagRouteNat
{
	int iSize;
	int iEnable;
	int iPortType;	//1: HTTP 2: TD data port 3: TD UDP command port 4: radar peripheral port
	int iProxyPort;
	int iIpcSrcPort;
}RouteNat, *pRouteNat;

typedef struct tagLogonActiveServer
{
	int		iSize;					
	char	cUserName[LEN_16];		
	char	cUserPwd[LEN_16];		
	char	cProductID[LEN_32];	//product id
} LogonActiveServer;

typedef struct tagActiveNetWanInfo
{
	int		iSize;
	char	cWanIP[LEN_32];		//Local public network ip
	int		iWanPort;			//Local public network port
	char	cWanIpV6[LEN_64];	//Local public network ipv6
} ActiveNetWanInfo;

typedef struct tagActiveDirectoryInfo
{
	int		iSize;	
	char	cDsmIP[LEN_32];
	int		iDsmPort;
	char	cAccontName[LEN_16];
	char	cAccontPwd[LEN_16];
	int		iIpVer;				//input para, ip version: 0--IpV4，1--IpV6
	char	cDsmIpV6[LEN_64];
} ActiveDirectoryInfo;

typedef struct tagDsmNvsRegInfo
{
	int		iSize;
	char	cFactoryID[LEN_32];			//nvs factory id
	char  	cNvsIP[LEN_32];				//nvs ip address
	char  	cNvsName[LEN_32];			//nvs name
	char	cRegTime[LEN_32];			//nvs register time
	int		iChanNum;					//nvs channel count
	char 	cWanIp[LEN_32];				//public net Ip
} DsmNvsRegInfo;

typedef struct tagDsmNvsRegInfoEx
{
	char	cFactoryID[LEN_32];			//[in][out]nvs factory id, query by ID is input para 
	char  	cNvsIP[LEN_32];				//nvs ip address
	char  	cNvsName[LEN_32];			//[in][out]nvs name, query by domain name is input para 
	char	cRegTime[LEN_32];			//nvs register time
	int		iChanNum;					//nvs channel count
	char 	cWanIp[LEN_32];				//public net Ip
	int		iIpVer;						//output para, ip version: 0--IpV4，1--IpV6
	char  	cNvsIpV6[LEN_64];			//nvs ipv6 address
	char 	cWanIpV6[LEN_64];			//public net IpV6
	int		iTcpWanPort;				//Public network port of network communication(3001)
	int		iHttpWanPort;				//Public network port of http communication(80)
	int		iRtmpWanPort;				//Public network port of rtmp communication(1935)
	char	cCharSet[LEN_32];			//NvS Name charSet
} DsmNvsRegInfoEx;

typedef void (__stdcall *cbkDsmNvsNotify)(DsmNvsRegInfo* _pNvsInfo, int _iSize, void* _pUser);
typedef void (__stdcall *cbkDsmNvsNotifyEx)(DsmNvsRegInfoEx* _ptNvsInfoEx, int _iSize, void* _pvUsrData);
//[OUT]_ptNvsInfo: output register nvsinfo
//[OUT]_iSize: sizeof(DsmNvsRegInfoEx)
//[OUT]_pvUsrData: user data

typedef struct tagActiveNvsNotify
{
	int		iSize;
	cbkDsmNvsNotify pCbk;
	void*	pUser;
	cbkDsmNvsNotifyEx pCbkEx;	//Compatible with IpV6, Compatible with pCbk, pcbkEx and pCbk use just one
} ActiveNvsNotify;

typedef struct tagActiveEasyDdnsPara
{
	char	cEasyDdnsDomainIp[LEN_128];		//EasyDDNS cloud services wan ip or domain name
	char	cLocalLanIp[LEN_64];			//Local lan ip
	char	cLocalDomainName[LEN_128];		//Local public domain name
	char	cLocalFactoryId[LEN_32];		//Local factory id
	char	cLocalCharSet[LEN_32];			//Local character set: GB2312 UTF-8
	int		iEasyDdnsWanPort;				//EasyDDNS cloud services wan port
	int		iLocalTcpWanPort;				//Local public tcp port
	int		iLocalHttpWanPort;				//Local public http port
	int		iLocalRtmpWanPort;				//Local public rtmp port
	int		iIpVer;							//Ip version: 0--ipv4, 1--ipv6, default ipv4
} ActiveEasyDdnsPara, *pActiveEasyDdnsPara;

#define DSM_CMD_SET_MIN								0
#define DSM_CMD_SET_NET_WAN_INFO		(DSM_CMD_SET_MIN + 0)	//[ActiveNetWanInfo]local public network ip and port
#define DSM_CMD_SET_DIRECTORY_INFO		(DSM_CMD_SET_MIN + 1)	//[ActiveDirectoryInfo]directory public network ip, port,account,password....
#define DSM_CMD_SET_NVSREG_CALLBACK		(DSM_CMD_SET_MIN + 2)
#define DSM_CMD_SET_USER_RULE			(DSM_CMD_SET_MIN + 3)
#define DSM_CMD_REGISTER_EASYDDNS		(DSM_CMD_SET_MIN + 4)	//Register with EasyDDNS, synchronous blocking interface, timeout 10s
#define DSM_CMD_TEST_EASYDDNS			(DSM_CMD_SET_MIN + 5)	//Test EasyDDNS, synchronous blocking interface, timeout 10s
#define DSM_CMD_SET_MAX					(DSM_CMD_SET_MIN + 6)		

typedef int (__stdcall *cbkRegDevListNotify)(int _iTotalCount, int _iCurrentCount, void* _pvNvsList, int _iSize, void* _pUser);
typedef int (__stdcall *cbkRegDevListNotifyEx)(int _iTotalCount, int _iCurrentCount, void* _pvNvsList, int _iTotalSize, int _iSingleSize, void* _pvUsrData);
//[OUT]_iTotalCount: total register nvs count
//[OUT]_iCurrentCount: current register nvs count
//[OUT]_pvNvsList: output register nvsinfo, the array of DsmNvsRegInfoEx, the array size is _iTotalSize/_iSingleSize
//[OUT]_iTotalSize: The total memory size of _ptNvsArr, when register nvs count is 20, _iTotalSize = 20 * sizeof(DsmNvsRegInfoEx)
//[OUT]_iSingleSize: sizeof(DsmNvsRegInfoEx)
//[OUT]_pvUsrData: user data
typedef struct tagActiveRegDevNotify
{
	int				iSize;
	cbkRegDevListNotify pCbk;
	void*			pvUser;
	cbkRegDevListNotifyEx pCbkEx;	//Compatible with IpV6, Compatible with pCbk, pcbkEx and pCbk use just one
} ActiveRegDevListNotify;

#define DSM_CMD_GET_MIN								0
#define DSM_CMD_GET_ONLINE_STATE			(DSM_CMD_GET_MIN + 0) // DsmOnline
#define DSM_CMD_GET_DEVICE_INFO				(DSM_CMD_GET_MIN + 0) // DsmOnline
#define DSM_CMD_GET_REGISTER_COUNT			(DSM_CMD_GET_MIN + 1)
#define DSM_CMD_GET_REGISTER_DEVLIST		(DSM_CMD_GET_MIN + 2)
#define DSM_CMD_GET_DEVCOUNT_WITHREG		(DSM_CMD_GET_MIN + 3) //Blocking interface, device count by interface return value or output para 
#define DSM_CMD_GET_DEVLIST_WITHREG			(DSM_CMD_GET_MIN + 4) //Asynchronous non-blocking interface, device list by callback to the user
#define DSM_CMD_GET_ASSIGNPROXY_WITHREG		(DSM_CMD_GET_MIN + 5) //Get assign proxy info with register
#define DSM_CMD_GET_REGNVSBYID_WITHREG		(DSM_CMD_GET_MIN + 6) //Query register nvs info by factoryID with register
#define DSM_CMD_GET_REGNVSBYDOMAINNAME		(DSM_CMD_GET_MIN + 7) //Query register nvs info by domain name with register
#define DSM_CMD_GET_MAX						(DSM_CMD_GET_MIN + 8) 

#define DSM_STATE_OFFLINE	0
#define DSM_STATE_ONLINE	1
typedef struct tagDsmOnline
{
	int		iSize;				//input para, sizeof(DsmOnline)
	char	cProductID[LEN_32];	//input para, product id(FactoryID)	
	int		iOnline;			//output para, 0--Offline 1--Online
	char    cWanIP[LEN_32];		//output para, public net ip address
	char    cLanIP[LEN_32];		//output para, lan ip address
	int		iIpVer;				//output para, ip version: 0--IpV4，1--IpV6
	char    cWanIpV6[LEN_64];	//output para, public net ipv6 address
	char    cLanIpV6[LEN_64];	//output para, lan ipv6 address
} DsmOnline;

typedef struct	tagAssignProxy		//分配代理信息
{
	char			cFactoryID[LEN_32];		//input para, FactoryID	
	char			cProxyIpV4[LEN_16];		//output para, Proxy IpV4	
	char			cProxyIpV6[LEN_64];		//output para, Proxy IpV6	
	int				iProxyPort;				//output para, Proxy Port	
} AssignProxy, *pAssignProxy;

typedef struct tagZoomControl
{
	int iSize;
	int iZoomSize;
}ZoomControl, *pZoomControl;

#define MAX_EXDEV_COUNT				30		//The maximum number of peripherals the device supports
#define MAX_EXDEC_COUNT_BY_TYPE		MAX_PERITYPE		//Maximum number of peripherals per device supported
#define EXDEV_TYPE_MIN									0
#define EXDEV_TYP_PIR							EXDEV_TYPE_MIN + 1	//PIR
#define EXDEV_TYP_TEMPERATURE					EXDEV_TYPE_MIN + 2	//Temperature
#define EXDEV_TYP_MAGNETIC_DETECTORS			EXDEV_TYPE_MIN + 3	//Door magnetic induction
#define EXDEV_TYP_SMOKE_DETECTORS				EXDEV_TYPE_MIN + 4	//Smoke sensor
#define EXDEV_TYP_HUMAN_INFRARED_SENSOR			EXDEV_TYPE_MIN + 5	//Human infrared sersor
#define EXDEV_TYP_WHITE_LIGHT 					EXDEV_TYPE_MIN + 6	//White light
#define EXDEV_TYP_RED_OUTDOOR_LIGHT				EXDEV_TYPE_MIN + 7	//Infrared light
#define EXDEV_TYP_WATER_OUT						EXDEV_TYPE_MIN + 8	//Water out
#define EXDEV_TYP_SOS_BUTTON					EXDEV_TYPE_MIN + 9	//SOS Button
#define EXDEV_TYPE_MAX							EXDEV_TYPE_MIN + 9

#define EXDEV_OPT_ADD		1
#define EXDEV_OPT_DELETE	2
#define EXDEV_OPT_MODIFY	3

#define EXDEV_INPUT		0
#define EXDEV_OUTPUT	1
#define EXDEV_EXTERADD		0
#define EXDEV_DEVICEOWN		1
typedef struct tagExDevPara {
	int		iSize;
	int		iOpt;					//0,Reserved；1，Add；2，Delete；3，Modify the name
	int		iExDevType;				//1:PIR, 2:Temperature, 3:Door magnetic induction, 4:Smoke sensor, 5:Human infrared sersor, 6:White light, 7:Infrared light, 8:Water out
	int		iExDevOpt;				//0:input,1:output
	int		iExDevOwn;				//0:Externally added,1:Equipment comes with
	int		iExDevId;
	char	cExDevName[LEN_64];
	int		iResult;				//Operation result：0，Success；1，Fail
} ExDevPara, *pExDevPara;

#define MAX_RECORDINGAUDIO_COUNT	20
#define RECORDING_OPT_PLAY			1
#define RECORDING_OPT_DELETE		2
#define RECORDING_OPT_MODIFY		3

#define DEVICE_AUDIO	0
#define USER_AUDIO		1

typedef struct tagRecordingAudio {
	int		iSize;
	int		iOpt;					//0,Reserved；1，Video on demand；2，Delete；3，Modify the name
	int		iAudioType;	//0:device, 1:user
	int		iAudioId;
	char	cAudioName[LEN_64];
	int		iResult;				//Operate result：0，Success；1，Fail
} RecordingAudio, *pRecordingAudio;

typedef struct tagTemperatureThreshold {
	int		iSize;
	float	fUpperLimit;	//Temperature upper limit terperature value -100 to 100(After the value of the software minus 1000 divided by 10, the accuracy of 0.1)
	float	fLowerLimit;	//Temperature lower limit terperature value -100 to 100(After the value of the software minus 1000 divided by 10, the accuracy of 0.1)
} TemperatureThreshold, *pTemperatureThreshold;

#define  MAX_CRUISE_TRACK_COUNT	16

typedef struct tagTrackingRate
{
	int		iSize;
	int		iSceneId;
	int		iBoundary;
	int		iDeviceId;
	int		iSceneType;
} TrackingRate, *pTrackingRate;

#define REPORT_CLEAR_CFG_ITEM		0
#define CLEAR_CONFIG_COMMAND		1
#define MAX_CLEAR_CFG_TYPE_COUNT	2
typedef struct tagClearConfig
{
	int		iSize;
	int		iType;		//0：Escalation is supported；1：clear command
	int		iParam;		//Bitwise：0-Not support，1-Support。bit0-Preset position，bit1-Cruise path，bit2-Pattern scan，bit3-Limit setting，bit4-Watch
} ClearConfig, *pClearConfig;

//M7
#define MAX_TEXT_PLAN_NUM			16
typedef struct tagTextPlan
{
	int		iSize;
	int		iPlanId;
	char	cAlias[LEN_64];
	char	cText[LEN_512];
}TextPlan, *pTextPlan;

typedef struct tagDevModel
{
	int		iSize;
	char	cModel[LEN_64];
	char	cBrand[LEN_64];	//品牌
}DevModel, *pDevModel;

typedef struct tagPswdVerify
{
	int		iSize;
	char	cUser[LEN_16];
	char	cPswd[LEN_16];
}PswdVerify, *pPswdVerify;

typedef struct tagSetSeekNVSS
{
	int		iSize;
	int		iSeekType;		//0-stop searching; 1-Search IPC; 2-Search decoder
	int		iSeekPara;		//0-IP; 1-Domain name; 2-Active mode; 3-IPv6
}SetSeekNVSS, *pSetSeekNVSS;

typedef struct tagGetSeekNVSS
{
	int		iSize;
	char	cMac[LENGTH_MAC_ADDR];						//MAC地址
	char	cIp[LEN_16];								//ip地址
	char	cName[LEN_32];								//ipc域名
	char	cMask[LEN_16];								//子网掩码	若该设备IP为空则该字段为DHCP服务器的子网掩码
	char	cGateWay[LEN_16];							//网关	若该设备IP为空则该字段为DHCP服务器的网关
	char	cDNS[LEN_16];								//DNS地址
	int		iChanNum;									//通道数	1～16
	int		iServerPort;								//服务器端口
	int		iClientPort;								//客户端端口
	int		iDevType;									//设备类型				
	int		iHttpPort;									//HTTP端口	0～65535
	int		iProductModel;								//设备型号
	char	cFactoryID[LEN_32];							//设备ID
	char	cKernelVersion[LEN_32];						//内核版本
	char	cOcxVersion[LEN_32];						//OCX版本
	char	cGuiVersion[LEN_32];						//GUI版本
	char	cSlaveVersion[LEN_32];						//从片版本
	int		iLanNum;									//网卡数目	如无该字段则默认为1
	int		iUpTime;									//运行时长	单位为秒
	int		iCheckCode;									//校验码	固定值为：20160113， 说明：如果校验码检验通过后，才有回复组播，否则不处理。
	int		iActivation;								//激活状态	0：未激活 1：已激活 2： 未知
	char	cNewFactoryID[LEN_128];						
	int		iBind;										//是否支持绑定（0-保留，1-支持，2不支持）
	char	pcDevModel[LEN_32];							//设备型号名称
	char	pcChnName[LEN_32];							//通道名称	最大32字节长度
	char	pcDeviceSN[LEN_32];							//设备SN序列号，即barcode	最大32字节长度
	char	pcSysTime[LEN_32];							//系统时间	格式为：2013 - 11 - 17T11:59 : 22，最大32字节长度
	char	iSysTZ;										//系统时区	值参考【TIME* TIMEZONE】协议注释
	char	pcLocalLinkIPv6[LENGTH_IPV6_V1];			//IPv6链路本地地址
	char	pcIPv6[LENGTH_IPV6_V1];						//IPv6地址
	int		iPrefixLenv6;								//IPv6网络前缀长度
	char	pcGateWayv6[LENGTH_IPV6_V1];				//IPv6网关
	char	pcDNSv6[LENGTH_IPV6_V1];					//IPv6的DNS地址
}GetSeekNVSS, *pGetSeekNVSS;

typedef struct tagXChangeIp
{
	int		iSize;
	char	cMac[LENGTH_MAC_ADDR];	
	char	cUser[LEN_16];			
	char	cPswd[LEN_16];			
	int		iType;					
	char	cIp[LEN_16];			
	char	cMask[LEN_16];			
	char	cGetWay[LEN_16];		
	char	cDNS[LEN_16];			
	char	cAdminPsw[LEN_16];		
	int		iCheckCode;				
	char	cFactryId[LEN_128];		
}XChangeIp, *pXChangeIp;

typedef struct tagXChangeIpv6
{
	int			iSize;
	char		pcMac[LENGTH_MAC_ADDR];					//MAC地址
	char		pcUser[LEN_128];						//用户名
	char		pcPwd[LEN_128];							//密码
	int			iType;									//连接类型
	char		pcIPv6[LENGTH_IPV6_V1];					//IP地址
	int			iPrefixLenv6;							//IPV6子网前缀长度
	char		pcGateWayv6[LENGTH_IPV6_V1];			//网关
	char		pcDNSv6[LENGTH_IPV6_V1];				//DNS地址
	int			iCheckCode;								//校验码
	char		pcFactoryID[LEN_128];;					//出厂ID
}XChangeIpv6, * pXChangeIpv6;

#define MAX_DAYS_PER_MONTH	31
typedef struct tagFileMap
{
	int		iSize;
	int		iChanNo;	
	int		iYear;
	int		iMonth;
	int		iRecFile[MAX_DAYS_PER_MONTH];
	int		iStreamNo;		
}FileMap, *pFileMap;

typedef struct tagPlateListType
{
	int		iSize;
	int		iListType;
}PlateListType, *pPlateListType;

typedef struct tagPlateListQuerySet
{
	int		iSize;
	int		iSessionId;	
	int		iPageSize;
	int		iPageNo;
}PlateListQuerySet, *pPlateListQuerySet;

#define MAX_PAGE_SHOW_NUM	50
typedef struct tagPlateListQueryGet
{
	int		iSize;
	int		iSessionId;
	int		iTotal;
	int		iPageNo;
	int		iShow;
	char	cPlate[MAX_PAGE_SHOW_NUM][LEN_32];
}PlateListQueryGet, *pPlateListQueryGet;

typedef struct tagPlateListQueryModify
{
	int		iSize;
	int		iOpt;
	char	cPlate[LEN_32];
}PlateListQueryModify, *pPlateListQueryModify;

typedef struct tagDiskPartInfo
{
	int		iSize;
	int		iDiskNo;
	int		iPartNum;
	int		iFormatNow;
	int		iFSType;
}DiskPartInfo, *pDiskPartInfo;


typedef struct tagLocalSnapSchedule
{
	int		iSize;
	int		iWeekDay;
	NVS_SCHEDTIME tTimeSeg[MAX_TIMESEGMENT];
	int		iDevType;
}LocalSnapSchedule, *pLocalSnapSchedule;

typedef struct tagSmartCheck
{
	int		iSize;
	int		iDiskId;
	int		iEnable;
}SmartCheck, *pSmartCheck;

typedef struct tagBadBlockSize
{
	int		iSize;
	int		iDiskId;
	unsigned long lBlockSize;
}BadBlockSize, *pBadBlockSize;

typedef struct tagBadBlockTest
{
	int		iSize;
	int		iDiskId;
	int		iType;
	int		iResult;
}BadBlockTest, *pBadBlockTest;

typedef struct tagBadBlockTestState
{
	int		iSize;
	int		iDiskId;
	int		iState;
	int		iTestType;
	int		iProgress;
	int		iErrorCount;
	int		iCurrentLoc;
}BadBlockTestState, *pBadBlockTestState;

#define	MAX_BLOCK_NUM	64
typedef struct tagBadBlockState
{
	int		iSize;
	int		iDiskId;
	int		iStartNo;
	int		iCount;
	int		iState[MAX_BLOCK_NUM];
}BadBlockState, *pBadBlockState;

typedef struct tagBadBlockAction
{
	int		iSize;
	int		iDiskId;
	int		iAction;
	int		iResult;
}BadBlockAction, *pBadBlockAction;

typedef struct tagFileInput
{
	int		iSize;
	int		iCheckCode;
	int		iFileType;
	int		iState;
}FileInput, *pFileInput;

typedef struct tagFileOutput
{
	int		iSize;
	char	cPath[LEN_256];
	int		iFileSize;
	int		iCheckCode;
	int		iState;
}FileOutput, *pFileOutput;

typedef struct tagFileConvert
{
	int		iSize;
	int		iFileType;
	int		iConvertType;
	char	cFileName[LEN_256];
}FileConvert, *pFileConvert;

//操作选项 Operation options
#define TRANSPORT_TYPE_IMPORT	0	//0-导入 Import
#define TRANSPORT_TYPE_EXPORT	1	//1-导出 export

#define TRANSPORT_FILE_TYPE_FULLPATH 						0	//全路径 Full path
#define TRANSPORT_FILE_TYPE_BLACE_WHITE_PLATE	 			1	//黑白车牌 Black and white license plate
#define TRANSPORT_FILE_TYPE_CHANNEL_PARAM 					2	//通道参数 Channel parameters
#define TRANSPORT_FILE_TYPE_GUARD	 						3	//看车位白名单 Look at the white list of parking spaces
#define TRANSPORT_FILE_TYPE_UPGRADE_IPC	 					4	//给前端相机升级 Upgrade ip camera 
#define TRANSPORT_FILE_TYPE_CORE_CA_CERT	 				5	//核心CA证书 Core CA certificate
#define TRANSPORT_FILE_TYPE_CLIENT_CERT	 					6	//客户端证书 Client certificate
#define TRANSPORT_FILE_TYPE_CLIENT_KEY	 					7	//客户端秘钥 Client secret key
#define TRANSPORT_FILE_TYPE_ANTI_ATTACK_KEY	 				8	//防攻击key Anti-attack key
#define TRANSPORT_FILE_TYPE_PLATE_BLACKLIST_CSV	 			9	//车牌黑名单csv格式 Csv format of license plate blacklist
#define TRANSPORT_FILE_TYPE_PLATE_WHITELIST_CSV	 			10	//车牌白名单csv格式 License plate white list csv format
#define TRANSPORT_FILE_TYPE_GB35114_CERT	 				11	//GB35114证书(SIP服务器证书) GB35114 certificate (SIP server certificate)
#define TRANSPORT_FILE_TYPE_HTTPS_SRTP_CERT	 				12	//HTTPS和SRTP证书 HTTPS and SRTP certificates
#define TRANSPORT_FILE_TYPE_GB35114_SERVICE_CERT_FILE	 	13	//GB35114服务证书请求文件 GB35114 Service Certificate Request File
#define TRANSPORT_FILE_TYPE_GB35114_SERVICE_CERT	 		14	//GB35114服务证书 GB35114 Service Certificate
#define TRANSPORT_FILE_TYPE_SIP_CA_CERT	 					15	//GB35114 SIP服务器CA证书 GB35114 SIP server CA certificate
#define TRANSPORT_FILE_TYPE_PLATE_LIBRARY_CSV	 			33	//车牌库csv格式 License plate library csv format
#define TRANSPORT_FILE_TYPE_PLATE_LIBRARY_TEMPLATE_CSV	 	34	//车牌库模板csv格式 License plate library template csv format
typedef struct tagFileTransport
{
	int		iSize;
	int		iOptType; //TRANSPORT_TYPE_IMPORT ~ TRANSPORT_TYPE_EXPORT
	int		iFileType; //TRANSPORT_FILE_TYPE_FULLPATH ~ TRANSPORT_FILE_TYPE_PLATE_LIBRARY_TEMPLATE_CSV
	char	cLocalFilePath[LEN_256];
	char	cFileName[LEN_256];
	void*	pvUserData;
	TRANSPORT_NOTIFY cbNotify;	
}FileTransport, *pFileTransport;
//M7 END

#define GET_AUDIO_ENABLE_ON			1
#define GET_AUDIO_ENABLE_OFF		0
typedef struct tagGetAudio
{
	int		iSize;
	int		iSceneId;
	int		iEnable;
}GetAudio, *pGetAudio;

typedef struct tagAudioDetection
{
	int		iSize;
	int		iSceneId;
	int		iAudioValue;
}AudioDetection, *pAudioDetection;

#define MAX_THRESHOLD_VALUE			100
#define MIN_THRESHOLD_VALUE			0
typedef struct tagAudioThreshold
{
	int		iSize;
	int		iSceneId;
	int		iThresholdValue;
}AudioThreshold, *pAudioThreshold;


#define ALGORITHM_TYPE_MONITOR		1
#define ALGORITHM_TYPE_TRAFFIC		2
typedef struct tagAlgorithmType
{
	int		iSize;
	int		iAlgorithmType;
}AlgorithmType, *pAlgorithmType;

#define MAX_QOS_TYPE				3
#define QOS_TYPE_STREAM				1
#define QOS_TYPE_SIGNALLING			2
typedef struct tagQosValue
{
	int		iSize;
	int		iType;
	int		iDSCPValue;
}QosValue, *pQosValue;

typedef struct tagOSDFontSize
{
	int		iSize;
	int		iOsdType;		//Character overlay type
	int		iCapType;		//Capture type
	int		iFontSize;		//Font size
}OSDFontSize, *pOSDFontSize;


typedef struct tagOSDColor
{
	int		iSize;
	int		iOsdType;		//Character overlay type
	int		iCapType;		//Capture type
	int		iColor;			//Color
}OSDColor, *pOSDColor;

#define MAX_MODESETTING_TYPE_NUM		1
typedef struct tagITSModeSetting
{
	int		iSize;
	int		iModeType;		//Mode type
	int		iDetectMode;	//Detection type
}ITSModeSetting, *pITSModeSetting;

typedef struct tagFECEPTZ
{
	int		iSize;
	int		iSpeed;			
	int		iPan;
	int		iTilt;
	int		iHorZoom;
	int		iVerZoom;
}FECEPTZ, *pFECEPTZ;

#define MAX_FEC_EPRESENT_NUM		257
typedef struct tagFECEPreset
{
	int		iSize;
	int		iPreset;
	int		iType;
	int		iSpeed;			
	int		iPan;
	int		iTilt;
	int		iHorZoom;
	int		iVerZoom;
}FECEPreset, *pFECEPreset;

#define COMSEND_CMD_MIN					0
#define COMSEND_CMD_SERIAL_PORT			(COMSEND_CMD_MIN + 0)
#define COMSEND_CMD_DIGTAL_CHANNEL		(COMSEND_CMD_MIN + 256)
#define COMSEND_CMD_FEC_CHANNEL			(COMSEND_CMD_MIN + 100000)
typedef struct tagComSend
{
	int					iSize;
	int					iChannelNo;
	unsigned char*		pucDataBuf;
	int					iBuffLen;
}ComSend, *pComSend;

#define MAX_MIC_NUM 16

typedef struct tagGetPtz
{
	int					iSize;
	int					iPosP;
	int					iPosT;
	int					iPosZ;
}GetPtz, *pGetPtz;

typedef struct tagSetPtz
{
	int					iSize;
	int					iType;// control type 0- control speed 1- control position 2- relative control position

	int					iPan; //Itype is 0 at the level of horizontal direction (0-100)
							  //Itype is 1 for moving target level 0-36000, corresponding to 0-360 degrees, accurate to 0.01.
							  //Itype is a relative movement angle 0-36000, representing a -180~180 degree at 2.

	int					iTilt;//The vertical direction velocity (0-100) when itype is 0
							  //Itype represents the moving target vertical position 1000~19000 at 1 and 2, corresponding to the -90-90 degree, to 0.01

	int					iZoom;//When itype is 0, the speed of double times (0-100)
							  //When itype is 1, the target variable position of the moving target is 0-1000 times that of 0-1000, accurate to 0.01.
							  //Itype is a multiple 0~100000 of relative movement at 2, corresponding to -500~500 times, accurate to 0.01
}SetPtz, *pSetPtz;

typedef struct tagAsensorCorrect
{
	int					iSize;
	int					iSceneId;
	int					iRuleID;
}AsensorCorrect, *pAsensorCorrect;


//NET_CLIENT_MULTI_PIC
typedef struct tagMultiPic
{
	int iSize;
	int iVoDevId;
	int iCombineMode;
	char cChannelSet[LEN_300];
}MultiPic;

typedef struct tagCurrentTemplate
{
	int					iSize;
	int					iTemplateIndex;
}CurrentTemplate, *pCurrentTemplate;

#define FUNC_ABILITY_SHOW_HDTEMPLATE		0
#define FUNC_ABILITY_HDDE_HDTEMPLATE		1

typedef struct tagSPlus
{
	int					iSize;
	int					iStreamNo;
	int					iSPlusState;
}SPlus, *pSPlus;

//far end enlarge status
#define  FEE_STATUS_RESERVE			0
#define  FEE_STATUS_CLOSE			1
#define  FEE_STATUS_OPEN			2
//far end enlarge value
#define  FEE_LEVEL_RESERVE			0
#define  FEE_LEVEL_LOW				1
#define  FEE_LEVEL_MIDDLING			2
#define  FEE_LEVEL_HIGH				3
#define  FEE_LEVEL_CUSTOM			4
typedef struct tagFarEndEnlarge
{
	int					iSize;
	int					iStatus;
	int					iValue;
}FarEndEnlarge, *pFarEndEnlarge;

typedef struct tagLocalStorePath
{
	int					iSize;
	int					iEnable;
}LocalStorePath, *pLocalStorePath;

#define MAX_REPORT_CONTENT_LOG_LEN			128
#define MAX_REPORT_QUERY_INFO_NUM			80

#define REPORT_QUERY_TYPE_TIME				0	//Query type Time-Report
#define REPORT_QUERY_TYPE_AGE_RANGE			1	//Query type human age range
#define REPORT_QUERY_TYPE_SEX				2	//Query type man or woman
#define REPORT_QUERY_TYPE_PEOPLE_NUM		3	//Query type number of people
#define REPORT_QUERY_TYPE_TEMPERATURE		4	//Query type number of temperature
#define REPORT_QUERY_TYPE_HUMIDNESS			5	//Query type number of humidness
#define REPORT_QUERY_TYPE_NATION			6	//Query type nation
#define REPORT_QUERY_TYPE_GLASSES			7	//Query type glasses
#define REPORT_QUERY_TYPE_MASK				8	//Query type mask
#define REPORT_QUERY_TYPE_TARGET_ALARM		9	//Query type Target alarm statistics
#define REPORT_QUERY_TYPE_CHANNEL_ALARM		10	//Query type Channel alarm statistics
#define REPORT_QUERY_TYPE_TATGET_DETAILS	11	//Query type Details on the target
#define REPORT_QUERY_TYPE_PERSON_SEX		12	//Query type:Pedestrian sex	
#define REPORT_QUERY_TYPE_PERSON_NUM		13	//Query type:Number of Pedestrian	
#define REPORT_QUERY_TYPE_PERSON_DIRECTION	14	//Query type:Pedestrian - direction of movement	
#define REPORT_QUERY_TYPE_VIHICLE_TYPE		15	//Query type:Vehicle Type	
#define REPORT_QUERY_TYPE_VIHICLE_DIRECTION	16	//Query type:Vehicle - direction of movement 	
#define REPORT_QUERY_TYPE_BODY_TEMPERATURE	17	//Temperature anomalies (private protocol not effect,(UK used))
#define REPORT_QUERY_TYPE_PEOPLEA_COUNTING	18	//Query type:People counting

#define REPORT_QUERY_PRECISION_HOUR			0	//query precision hour	
#define REPORT_QUERY_PRECISION_DAY			1	//query precision day	
#define REPORT_QUERY_PRECISION_MONTH		2	//query precision mouth	
#define REPORT_QUERY_PRECISION_YEAR			3	//query precision year
#define REPORT_QUERY_PRECISION_TEN_MINUTES	4	//query precision 10 minutes
#define MAX_FACE_PAGE                       5
typedef struct _tagReportQuery
{
	int				iSize;
	int				iReportSession;		
	int				iReportType;	
	int				iReportPre;	
	NVS_FILE_TIME	tBeginTime;			
	NVS_FILE_TIME	tEndTime;	
	int				iSceneId;
	int             iSort;
	int             iParaNum;
	char            cPara[MAX_FACE_PAGE][LEN_UUID];
}ReportQuery, *pReportQuery;

typedef struct tagReportQueryInfo
{
	int					iSize;
	int					iChannelNo;
	NVS_FILE_TIME		tOccurTime;
	char				cLogContent[MAX_REPORT_CONTENT_LOG_LEN];
}ReportQueryInfo, *pReportQueryInfo;

typedef struct tagReportQueryResult
{
	int					iSize;
	int					iReportSession;
	int					iReportType;
	int					iInfoNum;
	ReportQueryInfo		tReportQueryInfo[MAX_REPORT_QUERY_INFO_NUM];
}ReportQueryResult, *pReportQueryResult;

#define MAX_HEAT_MAP_URL_LEN				128
typedef struct _tagHeatMapGet
{
	int				iSize;
	NVS_FILE_TIME	tBeginTime;			
	NVS_FILE_TIME	tEndTime;
	int				iSceneId;
	int				iSex;
	int				iAge;
}HeatMapGet, *pHeatMapGet;

typedef struct _tagHeatMapGetResult
{
	int				iSize;
	char			cUrl[MAX_HEAT_MAP_URL_LEN];	
}HeatMapGetResult, *pHeatMapGetResult;

typedef struct tagVCAHeatMap
{
	int				iSize;			
	VCARule			tRule;					//Rule general parameter
	int				iMinDistance;			//Minimum distance
	int				iMinTime;				//Shortest time
	int				iMiniSize;				//Minimum size, [0, 100] Default 5
	int				iMaxSize;				//Maximum size, [0, 100] Default 30
	vca_TPolygonEx	tPoints;				//The number of vertex in the polygon area,and the coordinates
}VCAHeatMap,*pVCAHeatMap;

//install fish eye cameras method***************************************** 
#define ERECT_MODE_RESERVER					0		
#define ERECT_MODE_CEILING					1		//on the cailing 
#define ERECT_MODE_DESKTOP					2		//on the desktop 
#define ERECT_MODE_WALL						3		//on the straight wall 
//end*********************************************************************

//preview fish eye camera mode********************************************
#define PREVIEW_MODE_RESERVER					0	
#define PREVIEW_MODE_SINGLE_CHANNEL_FISH		1	//view single channel with fish eye mode
#define PREVIEW_MODE_SINGLE_CHANNEL_PANORAMIC	2	//view single channel with panoramic view
#define PREVIEW_MODE_PTZ_FISH					3	//view four pictutes with one fish eye and three ptz mode
#define PREVIEW_MODE_ALL_PTZ					4	//view four pictutes with four ptz mode
//end*********************************************************************

#define	VENC_SLICE_TYPE_SINGLE					0	//single slice 
#define	VENC_SLICE_TYPE_BYTES					1	//slice group on bytes
#define	VENC_SLICE_TYPE_MACRO					2	//slice group on macro
typedef struct tagVencSliceType
{
	int				iBufSize;	
	int				iStreamNo;
	int 			iType;					
	int				iSlcSize;							
}VencSliceType, *pVencSliceType;

//Channel connnect states************************************************
#define CHANNEL_CONNECT_STATE_UNKNOWN			-1	//unknown
#define CHANNEL_CONNECT_STATE_CONNECT			1	//connect
#define CHANNEL_CONNECT_STATE_DISCONNECT		0	//disconnect
//end*********************************************************************

#define UPNP_PORT_TYPE_HTTP				1
#define UPNP_PORT_TYPE_RTSP				2
#define UPNP_PORT_TYPE_SERVER			3
#define UPNP_PORT_TYPE_HTTPS			4
#define UPNP_PORT_TYPE_RTMP				5
#define MAX_UPNP_PORT_TYPE				6
typedef struct tagPortMap
{
	int				iBufSize;	
	int				iPortType;
	int 			iOutsidePort;					
}PortMap, *pPortMap;				

typedef struct tagPortMapInfo
{
	int				iBufSize;	
	int				iPortType;
	int 			iState;			
	char			cOutsideIpaddr[LEN_32];
	char			cIPv6[LENGTH_IPV6_V1];
}PortMapInfo, *pPortMapInfo;

//common enable upnp status***********************************************
#define CE_UPNP_STATUS_RESERVE				0	
#define CE_UPNP_STATUS_MANUAL				1	//manual operation
#define CE_UPNP_STATUS_AUTOMATIC			2	//automatic			
//end*********************************************************************

typedef struct tagNTPTest
{
	int		iBufSize;
	char	cServerIP[LEN_32];			//NTP Server IP
	int		iServerPort;				//NTP Server Port
	char	pcServerIPv6[LENGTH_IPV6_V1];		//NTP Server IPv6
}NTPTest, *pNTPTest;

typedef struct tagQueryAutoTestResult
{
	int		iBufSize;
	int		iType;
	char	pcParam[LEN_128];	
	int		iEnable;			
	int		iStatus;
	int		iResult;
}QueryAutoTestResult, *pQueryAutoTestResult;

typedef struct tagAlertTemplate
{
	int		iBufSize;
	int		iSceneId;
	int		iRuleID;			
	int		iTemplateID;
	int		iTemplateType;
	int		iCustomSetEnable;
}AlertTemplate, *pAlertTemplate;

typedef struct tagVCASeeper
{
	int					iBufSize;				//Trip line structure size
	VCARule				tRule;					//Rules general parameters
	vca_TDisplayParam	tDisplayParam;			//Display parameters
	int					iTargetTypeCheck;		//Target type limit(0：not distinguish 1：distinguish between people 2：distinguish between cars 3：distinguish between people and cars)
	int					iMiniSize;				//Minimum size, [0, 100] Default 5
	int					iMaxSize;				//Maximum size, [0, 100] Default 30
	int					iSensitivity;			//sensibility [0, 100] Default 80
	int					iDisplayTarget;			//display target box, 0-not dispaly, 1-dispaly
	int					iDelayOffAlarmTime;		//delay off alarm time
	vca_TPolygonEx		tPoints;				//The number of vertex in the polygon area,and the coordinates
} VCASeeper,*pVCASeeper;

//use to get player info when play back
typedef struct _tagPlayBackInfo
{
	int		iSize;
	int		iPlayerId;     //player id
}PlayBackInfo, *PPlayBackInfo;

#define MAX_SHOW_VIDEO_NUM		2				//max url num per time

//forbid extend FtpDownloadVideoInfo 
typedef struct tagFtpDownloadVideoInfo
{
	int					iChannelNo;
	NVS_FILE_TIME		tBeginTime;			
	NVS_FILE_TIME		tEndTime;	
	int					iFileSize;
	char				cFtpUrl[LEN_256];
}FtpDownloadVideoInfo, *pFtpDownloadVideoInfo;

typedef struct tagFtpDownloadVideo
{
	int					m_iBufSize;
	char				cFtpUser[LEN_16];
	char				cFtpPasswd[LEN_16];
	int					iTotalPackets;				//total packets
	int					iCurPacketNo;				//current packet serial number 
	int					iTotalNum;					//total query file num	[0~500]
	int					iShowNum;					//current query file num  [0~20]
	FtpDownloadVideoInfo tVideoInfo[MAX_SHOW_VIDEO_NUM]; 
}FtpDownloadVideo, *pFtpDownloadVideo;

typedef struct tagFtpDownloadVideoReply
{
	int					iBufSize;
	int					iTotalPackets;				//total packets
	int					iCurPacketNo;				//current packet serial number 
	int					iDownloadState;				//0:success 1:need resend 
}FtpDownloadVideoReply, *pFtpDownloadVideoReply;

//camera calibration type
#define VIDEOLDC_TYPE_CLOSED		0
#define VIDEOLDC_TYPE_LDC			1	//camera calibration
#define VIDEOLDC_TYPE_FEE			2	//far end enlarge
#define MAX_VIDEOLDC_TYPE			3	

typedef struct _tagVideoLdc
{
	int		iSize;
	int		iType;     
	int		iField;
	int		iStrenght;
}VideoLdc, *pVideoLdc;

#define DEV_OUTPUT_MODE_HDMI				1
#define DEV_OUTPUT_MODE_DVI					2
#define DEV_OUTPUT_MODE_ADAPTATION			3
#define MAX_VODEV_COUNT		2
typedef struct _tagDevOutputMode
{
	int		iSize;
	int		iVoDevId;	//0x7fffffff all vo device，0:HDMI1, 1:HDMI2, other reserve
	int		iMode;		//0:HDMI 1:DVI 2:adaptation
} VoDevOutputMode, *pDevOutputMode;

typedef struct _tagAutoFoucs
{
	int		iSize;
	int		iType;     
}AutoFoucs, *pAutoFoucs;

#define PORT_NO_TYPE_RESERVE				0		//port no :port type reserve
#define PORT_NO_TYPE_RTMP					1		//port no :port type rtmp
#define MAX_PORT_NO_TYPE					2		//port no :max port type 2
typedef struct _tagPortNo
{
	int		iSize;
	int		iPortType;							//port type
	int		iPort;								//port no
}PortNo, *pPortNo;

#define MAX_COMREAD_COM_NUM					24	//max com read num 
typedef struct _tagReadComTimeInterval
{
	int		iSize;
	int		iComNo;								//com num 0~24
	int		iTimeInterval;						//com read time interval
}ReadComTimeInterval, *pReadComTimeInterval;

typedef struct tagVCASTFaceAdvance
{
	int					iBufSize;				//Trip line structure size
	int					iSceneId;				//[0~15]
	int					iDisplayRule;			///display Rule,0-display 1-Not display	
	int					iDisplayTarget;			//display target box, 0-not dispaly, 1-dispaly
	int					iColor;					//zone color,1-red 2-green 3-yellow 4-blue 5-Purple 6-cyan-blue 7-black 8-White,(defalit green)
	int					iAlarmColor;			//alarm zone color
	int					iExposureBright;		//[0~255]
	int					iMinSize;				//Minimum size The ratio 0~10000 means 0~1
	vca_TPolygonEx		tPoints;				//The number of vertex in the polygon area,and the coordinates
}VCASTFaceAdvance,*pVCASTFaceAdvance;


#define MIN_WINDOW_DOUBLE_CIRCLE_NUM		0  
#define MAX_WINDOW_DOUBLE_CIRCLE_NUM		64

typedef struct tagWindowDetectionPos
{
	int			iOutPolygonPointNum;			
	int			iInnerPolygonPointNum;			
	vca_TPoint 	stOutPolygonPoints[VCA_MAX_POLYGON_POINT_NUM];   
	vca_TPoint 	stInnerPolygonPoints[VCA_MAX_POLYGON_POINT_NUM];   
}WindowDetectionPos,*pWindowDetectionPos;

typedef struct tagVCAWindowDetection
{
	int					iBufSize;				//Trip line structure size
	VCARule				tRule;
	vca_TDisplayParam	tDisplayParam;
	int					iTargetTypeCheck;		//0:not distinguish 1:distinguish human 2:distinguish car 3:distinguish human and car
	int					iMiniSize;				//[0, 100] default 5
	int					iMaxSize;				//[0, 100] default 30
	int					iSensitivity;			//[0, 100] default 80
	int					iDisplayTarget;			//0：BLANK 1：show
	int					iDoubleCircleNum;		//0-63
	WindowDetectionPos  tWindowDetectionPos[MAX_WINDOW_DOUBLE_CIRCLE_NUM];
	int					iLinkType;				//alarm link type
}VCAWindowDetection,*pVCAWindowDetection;

#define MAX_ALERT_LINK_PARAM_NUM			3	//alert multi stage max link param num
#define MAX_ALERT_LINK_TEMPLATE_TYPE		2	//0-Illeagll warnning 1-multi stage protect
#define MAX_ALERT_LINK_ALARM_LEVEL			4	//0:default 1~3 multi protect level
#define MAX_ALERT_LINK_LINK_TYPE			11	

typedef struct tagVCAAlertMultiStage
{
	int					iBufSize;				//buf size	
	int					iSceneID;				//scene id [0~31]
	int					iRuleID;				//rule id [0~15]
	int					iTemplateID;			//template id	 5:customize template 
	int					iTemplateType;			//(if iTemplateID == 5) 0: Prohibited 1:Warning
	int					iAlarmLevel;
	int					iResidenceTime;
	int					iLinkType;
	int					iLinkParam[MAX_ALERT_LINK_PARAM_NUM];
}VCAAlertMultiStage,*pVCAAlertMultiStage;

#define MAX_LICENSE_PLATE_LEN				16	//max license plate len
#define MAX_WHITE_PLATE_PAGE_NUM			4
#define MAX_WHITE_PLATE_NUM					64	//a page white plate num
#define MAX_WHITE_PLATE_TOTAL_NUM			256	//total white plate num

typedef struct tagITSParkWhiteList
{
	int					iBufSize;				//buf size	
	int					iSceneID;				//scene id [0~31]
	int					iRuleID;				//rule id [0~15]
	int					iEnable;				//0:disable,1:enable
	int					iListNum;			    //park white License plate list
	char				cLicensePlateList[MAX_WHITE_PLATE_TOTAL_NUM][MAX_LICENSE_PLATE_LEN];
}ITSParkWhiteList,*pITSParkWhiteList;

#define MAX_ALERT_TEMPLATE_TYPE_NUM		6		//max alert type num			
typedef struct tagAlertTemplateName
{
	int					iBufSize;				//buf size	
	int					iSceneID;				//scene id [0~31]
	int					iRuleID;				//rule id [0~15]
	int					iAlertNum;
	int					iAlertType;
	int					iCustomSetEnable;		//wheather support custom set 0:unsupport 1:support
}AlertTemplateName,*pAlertTemplateName;

typedef struct tagITSParkGuard
{
	int					iBufSize;						//buf size
	VCARule				tRule;							//ruleId default 0
	int					iPreset;						//preset point no.  default 0
	int					iArea;							//area no. default 0
	int					iIllegalParkTime;				//illegal park time (unit: second)
	int					iTimeRange[MAX_TIMESEGMENT];	//bit24-bit31:start hour;bit16-bit23:start minute;bit8-bit15:end hour;bit0-bit7:end minnute
	int					iSensitivity;					//0 :low 1:normal 2:high
	int					iCheckParkTime;					//min 1
	char				cAreaName[LEN_32];
	int					iAreaEnable;
	int					iCapEnable;						//bit operate;bit0：1 capture park
	vca_TPolygon		tAreaPointInfo;					//point 【2~16】
}ITSParkGuard,*pITSParkGuard;


#define UPGRADE_ERROR_DEFAULT				0		//default
#define UPGRADE_ERROR_PACKAGE_SIZE			1		//package size too larage
#define UPGRADE_ERROR_CHIP_TYPE				2		//ERROR chip type
#define UPGRADE_ERROR_MAIN_VER				3		//ERROR main version
#define UPGRADE_ERROR_SUB_VER				4		//ERROR sub version
#define UPGRADE_ERROR_CHECK_OUT				5		//check out failed
#define UPGRADE_ERROR_PRESERVE				6		//preserve failed 
#define UPGRADE_ERROR_PACKAGE_FIRM			7		//error manufacturer info 
#define UPGRADE_ERROR_UPDATE_DATA			8		//update data error 
#define UPGRADE_ERROR_LITTLE_VER			9		//ERROR little version
#define UPGRADE_ERROR_MEDIAEXIT_BUSY		10		//media layer exit busy
#define UPGRADE_ERROR_MEDIAEXIT_TIMEOUT		11		//media layer exit timeout

typedef struct tagUpgradeError
{
	int					iBufSize;			//buf size
	int					iErrorInfo;			//reason of upgrade failed!
}UpgradeErrorInfo,*pUpgradeErrorInfo;

#define UPGRADE_FINISH_SUCESS				0
#define UPGRADE_FINISH_OFFLINE_UPGRADE		1
typedef struct tagUpgradeFinishInfo
{
	int					iBufSize;			//buf size
	int					iCurState;			//device current upgrade state
}UpgradeFinishInfo,*pUpgradeFinishInfo;

#define MAX_MAC_NUM  15
typedef struct tagGetDevMac
{
	int iSize;
	int iMacStart;
	int iMacEnd;
	char cMAC[MAX_MAC_NUM][LEN_18];
}GetDevMac;


#define  HUTEM_TYPE_TEMPERATURE					0
#define  HUTEM_TYPE_HUMIDITY					1
#define  HUTEM_TYPE_PRESSURE					2
#define  HUTEM_TYPE_CO2							3
#define  HUTEM_TYPE_FINE_PARTICULATE_MATTER		4
#define  HUTEM_TYPE_TVOC						5
#define  HUTEM_TYPE_METHANAL					6
#define  MAX_HUTEM_TYPE_NUM						7
//temperature humidity time interval
typedef struct tagHuTemInterval				
{
	int					iBufSize;			//buf size
	int					iType;						
	int					iInterval;
}HuTemInterval,*pHuTemInterval;

#define MAX_FLASH_ALERT_TYPE_NUM		1
#define FLASH_ALERT_TYPE_MULTI_PROTECT	0

#define MAX_FLASH_SUB_ALERT_TYPE_NUM	2
#define MAX_FLASH_ALART_LEVEL			4

typedef struct tagVCAFlashAlertInfo
{
	int					iSize;
	int					iMainAlertype;		//0:multi stage alarm`
	int					iSubType;			//{if mian alerTyepe=0(0: Prohibited 1:Warning)}
	int					iMaxLevel;			
}VCAFlashAlertInfo, *pVCAFlashAlertInfo;

typedef struct tagVCAFlashFlickerInfo
{
	int					iSize;
	int					iMainAlertype;	
	int					iSubType;		
	int					iLevel;
	int					iMaxFlickerTime;	//max falsh flicker time
	int					iStrobeEnable;		//stroboscopic enable
}VCAFlashFlickerInfo, *pVCAFlashFlickerInfo;

#define CALIBRATE_STATUS_QUIT				0
#define CALIBRATE_STATUS_ENTER				1
#define CALIBRATE_STATUS_UNDERWAY			2
typedef struct tagCalibrateStatus
{
	int		iBufSize;				//structure size
	int		iStatus;				//Status(0:exit calibrate status 1:enter calibrate status 2:underway)
}CalibrateStatus, *pCalibrateStatus;

#define MAX_CALIBRATE_SECNE_NUM				4
#define MAX_CALIBRATE_POINT_NUM				64
typedef struct tagCalibrateInfo
{
	int				iBufSize;				
	int				iSceneId;
	int				iCalibrateNo;
	int				iStatus;				//0:disable,1:enable	
	vca_TPoint		tPoint;					//Calibrate point
	int				iPan;					//0~36000
	int				iTilt;					//1000~19000
	int				iZoom;					//0~100000
	int				iLinkDevNo;				//0~1
}CalibrateInfo, *pCalibrateInfo;

#define MAX_CALIBRATE_VCA_PARA_NUM			32
typedef struct tagCalibrateVcaPara
{
	int				iBufSize;				
	int				iSceneId;
	int				iParamNum;				//0~32
	int				iParam[MAX_CALIBRATE_VCA_PARA_NUM];
	int				iLinkDevNo;
}CalibrateVcaPara, *pCalibrateVcaPara;

#define MAX_VCA_MASK_AREA_NUM				16
typedef struct tagVCAMaskAreaParam
{
	int				iBufSize;				
	int				iSceneId;				//iSceneId:0-31,场景ID 高16位为1时，低16位表示警戒，范围0~3
	int				iRuleId;				//iRuleId:0-15,0x7fff代表所有规则用相同的参数
	int				iEnable;
	int				iDisplayRule;
	int				iColor;
	int				iAreaNum;
	vca_TPolygonEx	tAreaInfo[MAX_VCA_MASK_AREA_NUM];
}VCAMaskAreaParam, *pVCAMaskAreaParam;

#define MAX_3D_LOCATE_POINT_NUM				2
typedef struct tagLocate3DPosition
{
	int				iBufSize;				
	int				iPointNum;								
	vca_TPoint		tPoint[MAX_3D_LOCATE_POINT_NUM];
}Locate3DPosition, *pLocate3DPosition;

//Latitude or longitude info
typedef struct tagCoordinate
{
	int				iDirection;
	int				iDegree;
	int				iMinute;
	int				iSecond;
}Coordinate, *pCoordinate;

typedef struct tagOffsetCoordinate
{
	int				iDegree;
	int				iMinute;
	int				iSecond;
}OffsetCoordinate, *pOffsetCoordinate;

typedef struct tagGeografhyLocation
{
	int				iBufSize;
	int				iType;				//0:manual operation 1:Auto
	Coordinate		tLongitudeInfo;
	Coordinate		tLatitudeInfo;
	int             iHeight;
	OffsetCoordinate tOffsetLongitudeInfo; //degree:0-180,minute:0-59,sec:0-6000;
	OffsetCoordinate tOffsetLatitudeInfo;//degree:0-180,minute:0-59,sec:0-6000;
	int             ioffSetHeight;
	int             iBankType;	//int	设备安装河岸两边类型	0-保留 1-左岸(面向下游时河流的左侧) 2-右岸(面向下游时河流的右侧)
	int             iBankDistance;//设备到河岸边的距离	范围:-100000～100000，单位:毫米(mm)
}GeografhyLocation, *pGeografhyLocation;

#define CHK_TIME_MODE_RESERVED			0
#define CHK_TIME_MODE_NTP				1		
#define CHK_TIME_MODE_GPS				2
#define CHK_TIME_MODE_MANUAL			3
typedef struct tagChkTimeMode
{
	int				iBufSize;
	int				iChkMode;
}ChkTimeMode, *pChkTimeMode;

//球机自动归位信息
typedef struct tagPtzAutoBack
{
	int 			iSize;
	char			iEnable;		
	int 			iPresetIndex;					
	int 			iIdleTime;					
}PtzAutoBack, *pPtzAutoBack;

#define SPLUSTEMPLATE_BITRATE_RATE 512			//S+ template rate multiplier
typedef struct tagSPlusTemplatePara
{
	int				iBufSize;					//IN Buffer size
	int				iSPlusStatus;				//IN 1=open 2-close
	int				iVencType;					//IN 0-h264 1-Mjpeg 2-Mpeg4 3-h265
	int				iVideoSize;					//IN
	int				iBitRate;					//OUT
	int				iEncodingMode;				//OUT
	int				iNPMode;					//OUT if mode==0x7ffff means Inoperative 
	int				iFrameRate;					//OUT if Rate==0x7ffff means Inoperative 
}SPlusTemplatePara, *pSPlusTemplatePara;

typedef struct tagEmailTest
{
	int 			iSize;
	int				iSmtpNo;
}EmailTest, *pEmailTest;

typedef struct tagEmailTestResult
{
	int				iTestResult;
	int				iSmtpNo;
}EmailTestResult, *pEmailTestResult;

typedef struct tagSmtpEnable
{
	int 			iSize;
	int				iSmtpNo;
	int				iEnable;
}SmtpEnable, *pSmtpEnable;

typedef struct tagGPSTime
{
	int 			iSize;
	int				iChkInterval;				//check time interval(units:seconds)
}GPSTime, *pGPSTime;

#define MAX_VIDEO_DECRYPT_KEY_LEN			17
typedef struct tagVideoDecryptEx
{
	int 			iSize;
	char			cKey[MAX_VIDEO_DECRYPT_KEY_LEN];
}VideoDecryptEx, *pVideoDecryptEx;


/*******************************picture stream begin*****************************************/

//Network picture stream callback command, different orders corresponding to different structures
#define NET_PICSTREAM_CMD_OLD			0	//Callback old image stream information
#define NET_PICSTREAM_CMD_VCA			1	//Callback VCA image stream information
#define NET_PICSTREAM_CMD_ITS			2	//Callback ITS image stream information
#define NET_PICSTREAM_CMD_FACE			3	//Callback face image stream information
#define NET_PICSTREAM_CMD_NORMALSNAP	4	//Callback normal snap image stream information
#define NET_PICSTREAM_CMD_CALIBRATION	5	//Callback calibration image stream information
#define NET_PICSTREAM_CMD_RADAR_TRACK	6	//Callback radar track stream information
#define NET_PICSTREAM_CMD_TRANS_ALGDATA	7	//Callback alg result data

//Register a network picture stream callback
typedef int (__stdcall *NET_PICSTREAM_NOTIFY)(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser);

typedef struct tagNetPicPara
{
	int 					iStructLen;				//Structure length
	int						iChannelNo;
	NET_PICSTREAM_NOTIFY	cbkPicStreamNotify;
	void*					pvUser;
	int						iPicType;		//When iPicStreamCmd is 3, bit0:Face picture stream bit1:pedestrian bit2:plate number bit3:motor vehicles bit4:Non-motor vehicle
											//When iPicStreamCmd is 7, iChannel between 8n～9n-1, bit0-Radar target trajectory information, bit1-Transmission of algorithm results 
	int						iPicStreamCmd;
} NetPicPara, *pNetPicPara;

#define MAX_CAPTURE_PICTURE_COUNT		3
#define MAX_CAPTURE_PICTURE_COUNT_EX	5
#define MAX_ITS_CAP_PIC_COUNT			(MAX_CAPTURE_PICTURE_COUNT + MAX_CAPTURE_PICTURE_COUNT_EX)
#define MAX_ITS_CAP_FACE_COUNT			3

typedef struct tagPicTime
{
	unsigned int uiYear;
	unsigned int uiMonth;
	unsigned int uiDay;
	unsigned int uiWeek;
	unsigned int uiHour;
	unsigned int uiMinute;
	unsigned int uiSecondsr;
	unsigned int uiMilliseconds; //The real number of milliseconds needs to be multiplied by 10 before use 
} PicTime, *pPicTime;

typedef struct tagPicData
{
	PicTime		tPicTime;
	int			iDataLen;		//Image length
	char*		pcPicData;		//Picture raw data, the upper can be directly saved as a picture
} PicData, *pPicData;

/*******************************VCA picture stream*****************************************/
typedef struct tagRecSceneSnap
{
	int     iChanNum;					//Channel number
	int		iScene;						//Scene ID
	int		iType;						//Reserved, 1 Snap After Entering The Scene, 2 Snap Before Leaving the Scene
}RecSceneSnap, *pRecSceneSnap;

typedef struct tagWaterDevReportInfo
{
	int		iChanNum;		//Channel number
	int		iIndex;			//Site label
	int		iSceneID;		//Scene id
	int		iRuleID;		//Rule ID
	int		iDataSource;	//Data source, 0-algorithm, 1-peripheral
	int		iType;			//1: rainfall,2: Rain time,3: Water level,4: water depth,5: Super warning water level value,6: flow rate,7: Battery remaining capacity,8: Air pressure,9: Wind speed,10: Wind direction,11: temperature,12: Humidity,13: pH,14: Dissolved oxygen,15: redox,16:GPS,17: elevation
	int		iValue;			//iType Corresponding unit--1: mm*10,2: minute,3: mm,4: mm,5: mm,6: mm/s,7: %,8: hpa*10,9: m/s*10,10: °,11: Celsius *10,12:%*10,13: pH*100,14: mg/L*100,15: mV,16:GPS,17: m
}WaterDevReportInfo, *pWaterDevReportInfo;

typedef struct tagPtzInfo
{
	int		iPanPosition;		//Current horizontal coordinate 0~3600000 corresponds to 0-360 degrees (accuracy * 10000)
	int		iTiltPosition;		//The current vertical coordinates 100000-1900000 correspond to -90-90 degrees (accuracy * 10000)
	int		iZoomPosition;		//Current magnification 0-10000000 corresponds to 0-1000 times (accuracy * 10000)
	int		iNorthAngle;		//angle (accuracy * 10000)
	int		iPanStep;			//Field angle horizontal step, unit degree (accuracy * 10000)
	int		iTiltStep;			//Field angle vertical step, unit degree (accuracy * 10000)
}PtzInfo, *pPtzInfo;

typedef struct tagCpcInfo
{
	int		    iMode;       // 1-垂直人数统计；2-水平人数统计；3-区域人数统计；4-画线人数统计，5-队列人数统计
	int         iInCount;    //实时统计总数in
	int         iOutConut;   //实时统计总数out
	int         iPassConut;  //实时统计总数pass
	int         iZoneConut;  //实时统计总数zone----这个是区域人数值
	int	 	    iMaxNum;	 //允许最大人数
	int	 	    iAlarmState;  //报警状态 0-无警情1-超限报警
}CpcInfo;

typedef struct tagPicAndRecord
{
	char cPicPdrKey[LEN_64];	//图片PDR key值，不存在允许发空
	char cPicName[LEN_64];		//图片名，不存在允许发空
	char cRecordName[LEN_64];	//录像名，不存在允许发空
}PicAndRecord;

typedef struct tagPicStreamCaliImgInfo
{
	int i32Rotation;  // 顺时针旋转角度（0~360°），单位 °
	int iResolved[LEN_8];  // 预留位
}PicStreamCaliImgInfo;

typedef struct tagFacePicStreamCpcInfo
{
	int			iValid;     //coordinate enable:0-invalid 1-valid
	double      f64PointX;  //世界坐标系X
	double		f64PointY;  //世界坐标系Y
	double		f64PointZ;  //世界坐标系Z
}FacePicStreamCpcInfo,*pFacePicStreamCpcInfo;

typedef struct tagCurrentPicPeopleNum
{
	int			iNum;//当前帧人员个数
}CurrentPicPeopleNum, *pCurrentPicPeopleNum;

typedef struct tagDevGPSInfo
{
	int		iLongitude;				//经度 0-E , 1-W   
	int		iLongitudeDegree;		//度0-180
	int		iLongitudeMinute;		//分0-59
	int		iLongitudeSecond;		//秒 (0.00~55.99) * 100，保留小数点后2位精度
	int		iLatitude;				//纬度 0-S , 1-N
	int		iLatitudeDegree;		//度0-90
	int		iLatitudeMinute;		//分0-59
	int		iLatitudeSecond;		//秒 (0.00~55.99) * 100，保留小数点后2位精度
}DevGPSInfo, *pDevGPSInfo;

typedef struct tagEventTargetGPSInfo
{
	int		iEventType;			// 按网络协议的事件类型
	int		iTargetId;		//目标id：
	int		iLongitude;		//经度 0-E , 1-W   
	int		iLongitudeDegree; //度0-180
	int		iLongitudeMinute; //分0-59
	int		iLongitudeSecond; //秒 (0.00~55.99) * 100，保留小数点后2位精度
	int		iLatitude;		//纬度 0-S , 1-N
	int		iLatitudeDegree; //度0-90
	int		iLatitudeMinute; //分0-59
	int		iLatitudeSecond; //秒 (0.00~55.99) * 100，保留小数点后2位精度
}EventTargetGPSInfo, *pEventTargetGPSInfo;

typedef struct tagVcaCaptureInfo
{
	int		iCaptureChnType[MAX_CAPTURE_PICTURE_COUNT];  //智能分析抓拍每张图对应的抓拍类型（顺序与附加抓拍图顺序对应）0-本通道抓拍 1-非本通道抓拍 2-特写抓拍
}VcaCaptureInfo, *pVcaCaptureInfo;

typedef struct 
{
	unsigned short	usLbX;	//left
	unsigned short	usLbY;	//top
	unsigned short	usRuX;	//right
	unsigned short	usRuY;	//bottom
}vca_TargetRECT;

typedef struct tagTStrategyAttr
{
	int				iConditionId;	//条件编号
	int				iTargetType;	//目标类型：1-人 2-物（其他/颜色） 3-车 4-声音
	int				iEventType;		//事件类型：1-入侵，2-离开，3-滞留，4-高音，5-低音
	int				iColor;			//TD_ALG_COLOR_TYPE_E 0-未知 1-白 2-灰 3-棕 4-红 5-蓝 6-黄 7-绿 8-粉 9-橙 10-青 11-紫 12-浅蓝 13-黑 14-(其他)彩色
	vca_TargetRECT	tRect;			//坐标信息
}TStrategyAttr,*pTStrategyAttr;

#define STRATEGY_TARGET_NUM_MAX 8
typedef struct tagTStrategyInfo
{
	int				iAttrSturctSize;	// sizeof(TStrategyAttr)
	int				iTargetNum;			//目标个数
	TStrategyAttr	tTargetAttr[STRATEGY_TARGET_NUM_MAX];
}TStrategyInfo,*pTStrategyInfo;

#define ALARM_RESERVE_NUM_MAX 4
typedef struct 
{
	int     m_iChn;				// 通道
	int     m_iState;			// 移动报警子功能报警状态 bit0:人形报警 0-消警 1-报警 bit1:车辆报警 0-消警 1-报警
	int     m_cReserve[ALARM_RESERVE_NUM_MAX];
}TAlarmMDMotionDetectionSubSnapType;


typedef struct tagVcaPicStream
{
	int 		iStructLen;				//Structure length
	int 		iWidth;					//Picture wide
	int 		iHeight;				//Picture high
	int   		iChannelID;				//Channel number
	int 		iEventType;				//Event type
	int 		iRuleID;				//Rule ID
	int    		iTargetID;				//Target ID
	int 		iTargetType;			//Target type:1-person,2-(other),3-car
										//3001:Car 3002:SUV 3003:Pickup truck 3004:Tanker 
										//3005:Van van 3006:Big truck 3007:Forklift 3008:Excavator 
										//3009:Engineering 3010:Two-wheeler 3011:Tricycle
	int 		iTargetSpeed;			//The target speed by the number of pixels per second (pixel/s)
	int 		iTargetDirection;		//Target movement direction,0 ~ 359 degrees
	RECT 		tTargetPosition;		//target location
	int     	iPresetNo;          	//Preset number (scene number) vca:0~32 alert:0~3
	char		cCameraIP[LEN_16];		//Camera IP
	int   		iPicCount;				//Record the number of images included,Maximum 3
	int			iSize;					//The size of strcut PicData
	PicData*	ptPicData[MAX_CAPTURE_PICTURE_COUNT];
	int			iAlarmType;				//0-vca 1-unique alarm
	int			iHelmetStatus;			//wear a helmet or not. 0: reserved 1: not wearing 2: wear
	int 		iHelmetColor;			//helmet color (0: reserved 1: red 2: yellow 3: blue 4:white 5: other )
	int 		iAlertTypeParam;		//useful when unique alert alarm (perimeter alert 0: reserved 1: perimeter invading 2: perimeter and leaving 3: tripping line)
	char		cTmp[LEN_32];			//Reserved bytes
										//iEventType = 51,[0] 1-Zone Invasion 2-Abnormal Residency
										//iEventType = 54,[0] 1-No chef's hat 2-No masks 3-No chef's hat and masks
										//iEventType = 62,cTmp[0],1: Temperature difference detection (greater than) 2: High temperature detection (greater than) 3: Human body temperature detection 4: Temperature difference detection (less than) 5: High temperature detection (less than) 5: Low temperature detection (greater than) 7: Low temperature detection (less than) 8: Average temperature detection (greater than) 9: Average temperature detection (less than) 10: Mutation detection
										//iEventType = 64,cTmp[0],0: Reserved 1: Designated fire point 2: Designated smoke 3: Fire point or smoke 4: Fire point and smoke 5: Smoking detection
										//iEventType=10 video diagnosis, m_cTmp[0]- m_cTmp[14] represent whether the diagnosis subtype is alarmed or not 0—not alarmed 1—alarmed
										//cTmp[0] - Noise diagnosis
										//cTmp[1] - Definition diagnosis
										//cTmp[2] - Brightness diagnosis
										//cTmp[3] - Color deviation diagnosis
										//cTmp[4] - Screen freeze diagnosis
										//cTmp[5] - Signal missing diagnosis
										//cTmp[6] - Scene change detection
										//cTmp[7]-  Human interference diagnosis
										//cTmp[8] - PTZ out of control diagnosis
										//cTmp[9] - Artificial occlusion
										//cTmp[10] - abnormal contrast
										//cTmp[11] - black and white image
										//cTmp[12] - image shaking
										//cTmp[13] - fringe interference*/
										//iEventType=90 judge's behavior analysis, m_cTmp[0]- m_cTmp[7] represents whether the judge's behavior analysis subtype is alarmed or not 0—not alarmed 1—alarmed
										//cTmp[0] - late
										//cTmp[1] - leave early
										//cTmp[2] - leave halfway
										//cTmp[3] - absent
										//cTmp[4] - uniform detection (including formal robes, winter clothes (suits), summer clothes)
										//cTmp[5] - Emblem detection (big and small Emblem)
										//cTmp[6] - sign identification
										//cTmp[7] - call detection
	int					 iWaterInfoLen;	//Water information report len
	WaterDevReportInfo*	 pWaterInfo;	//Water information report
	int					 iPtzInfoLen;	//ptz information len
	PtzInfo*		     pPtzInfo;		//ptz information
	char                 cAlarmTime[LEN_17];
	PicTime				 tAlarmTime;
	int					 iAlgoType; //0:vca; 1:alert
	int					 iCpcInfoLen;
	CpcInfo				 *pCpcInfo;
	int					 iPicAndRecordLen;
	PicAndRecord         *pPicAndRecord;
	int					 iDevGPSInfoLen;
	DevGPSInfo			 *pDevGps;
	int					 iAlarmGPSInfoLen;
	EventTargetGPSInfo	 *pAlarmGPSInfo;
	int					 iVcaCaptureInfoLen;
	VcaCaptureInfo		 *ptVcaCaptureInfo;
	int					 iTStrategyInfoLen;
	TStrategyInfo		 *ptTStrategyInfo;
}VcaPicStream, *pVcaPicStream;

/*******************************ITS picture stream*****************************************/
#define MAX_LP_CHAR_COUNT		12		//The maximum number of characters in the license plate
typedef struct
{
	unsigned int	uiBrandType;		//车辆品牌
	unsigned int	uiSubBrand;			//车辆子品牌
	unsigned int	uiSubBrandYearStart;//车辆品牌年代款式例:2010-2013或2010
	unsigned int	uiSubBrandYearEnd;
	RECT			stRectLogo;			//车标坐标矩形
	unsigned int	uiConfid;			//置信度
}STRCT_BRAND;

#define MAX_RECOGNIZE_PLATE_COUNT		32
//License plate information struct
typedef struct tagRecgPlateInfo
{
	char	cPlate[LEN_32];			//plate
	int		iPlateColor;			//plate color
	int		iPlateType;				//plate type
	RECT	tPlateRange;			//plate rect
	char	cReserve[LEN_8];		//reserve
} RecgPlateInfo;

typedef struct tagRecgPlateList
{
	int 			iPlateCount;	//Recognition of the number of license plates 
	RecgPlateInfo	tPlateInfo[MAX_RECOGNIZE_PLATE_COUNT];
	int 			iHornId;
} RecgPlateList;

typedef struct tagITS_TCZInfo
{
	int iCarID;			//Weighing vehicles ID
}ITS_TCZInfo;

typedef struct tagPicRefId
{
	int		iTargetType;	//Target type: 0-vehicle, 1-license plate 
	int 	iTargetId;		//Target ID: when the target is a vehicle, it is the same as m_CarRegionNum
	int		iRefType;		//Association type: 0-vehicle, 1-license plate
	int 	iRefId;			//Ref id
}PicRefId;

typedef struct tagPicType
{
	int		iPicType;	//Included picture types, bit0 background, Bit1 vehicle, bit2 license plate
}PicType;

typedef struct tagSingleFeature
{
	int		iPicLen;		//Single Feature Picture（JPG）len
	int		iReserve[4]; 
}SingleFeature, *pSingleFeature;

typedef struct tagSingleFeatureInfo
{
	SingleFeature	tFeature;
	char*			pcPicData;
}SingleFeatureInfo, *pSingleFeatureInfo;

#define RADAR_EVENT_TYPE_NOEVENT			0
#define RADAR_EVENT_TYPE_STOP				1
#define RADAR_EVENT_TYPE_REVERSE			2
#define RADAR_EVENT_TYPE_CHANGE_LANE		3
#define RADAR_EVENT_TYPE_SERPENTINE			4
#define RADAR_EVENT_TYPE_CONGEST			5
#define RADAR_EVENT_TYPE_OUTINTOBUSINESS	6
#define RADAR_EVENT_TYPE_STALL				7
#define RADAR_EVENT_TYPE_ABANDONEDOBJECT	8
#define RADAR_EVENT_TYPE_PEDESTRIAN			9
#define RADAR_EVENT_TYPE_ROADBARRIER		10
#define RADAR_EVENT_TYPE_GUARDRAIL			11
#define RADAR_EVENT_TYPE_REFLECTORCONE		12
#define RADAR_EVENT_TYPE_CROSS_CONGESTION	13
#define RADAR_EVENT_TYPE_VEHICLE_DETECTION	14

typedef struct tarRadarEventType
{
	int			iEventType;				//Event type: 0- no event 1- stop 2- reverse 3- lane change 4- serpentine 5- congestion
}RadarEventType,*pRadarEventType;

typedef struct tarItsPlateInfoEx
{
	char	    cExtraInfo;				//0- Unknown 1- No license plate
	char	 	cReserve[15];
}ItsPlateInfoEx,*pItsPlateInfoEx;

typedef struct tagAlarmEventType
{
    int			iEventType;				//Event Type
}AlarmEventType,*pAlarmEventType;


typedef struct tagCrossNameV2
{
	char		cCrossName[LEN_256];	//Cross  name
}CrossNameV2;

typedef struct tagUserInfo
{
	int iUserNo; //人员编号,0-999999999
}UserInfo;

typedef struct tagAllowCaptureDirection
{
	char			cCapDirection;  //0-双向 1-上行 2-下行
	char	 	    cReserve[LEN_15];
}AllowCaptureDirection;

//卡口外触发抓拍图片ID
typedef struct tagOutOfChekPointTriggerSnap
{	
	int iSnapID;	// Snap ID
}OutOfChekPointTriggerSnap;

typedef struct tagItsPicLocalFileNameData
{
	char    cTargetLitName[LEN_64]; //目标小图本地文件名
	char    cBackBigName[LEN_64]; //背景大图本地文件名
}ItsPicLocalFileNameData;//文件名格式：英文字母、数字、ASCII可见符号如 / . - _ ( ) 等

typedef struct tagItsPicStream
{
	int			iStructLen;							//Structure length
	int   		iChannelID;							//Lane number
	char		cPlate[LEN_32];						//License plate
	int   		iPlateColor;						//License plate color.	Search for "/* * License Plate Color * */" in "GlobalTypes.h" for details. 	
	int   		iPlateType;							//License plate type.	Search for "/* Car PLATE Type  */" in "GlobalTypes.h" for details.
	int	  		iCarColor;							//the color of car.		Search for "/* * The Color of Car * */" in "GlobalTypes.h" for details.
	RECT		tPlateRange;						//License plate range, the coordinates of the license plate rectangle in the picture. Irecordonum determines which picture the tplaterange is based on. Irecordonum starts from 0, where 0 represents the first picture. 
	int   		iCharNum;							//The number of characters in the license plate
	int   		iCharConfid[MAX_LP_CHAR_COUNT];		//Each character confidence, up to 12 characters.range:100~128
	int   		iPlateConfid;						//The entire license plate confidence. range:100~128
	int   		iRecrdoNum;							//Identify the serial number of the image to determine which image the tplaterange is based on, starting from 0, where 0 represents the first one. 
	float 		fSpeed;								//Vehicle speed. range:0~255(km/h)
	int 		iVehicleDirection;					//Vehicle direction: 0: no direction; 1: forward; 2: reverse; 3: up; 4: down; 5: left; 6: right. 
	int			iAlarmType;							/*Alarm type: 1: bayonet; 2: retrograde; 3: running red light; 4: driving into the non motor vehicle lane, the motor vehicle does not drive in the motor vehicle lane; 
													  5: crossing the line, the motor vehicle does not drive in accordance with the prescribed lane, does not drive in accordance with the guidance; 6: the motor vehicle violates the prohibition marking instruction;
													  7: when the motor vehicle in front stops in line to wait or drives slowly, it stops in the crosswalk and reticle area Waiting for cars; 8: General speeding; 8: serious speeding; 9: Illegal turning around; 
													  10: illegal use of special lanes by motor vehicles; 11: not backing up according to regulations; 12: illegal lane changing; 13: illegal driving; 14: not wearing seat belts; 15: answering phone calls; 
													  16: motor vehicles do not allow pedestrians; 17: left turning vehicles do not allow direct driving; 18: zebra crossing overtaking; 19: 0% - 10% overspeed; 20: 10% - 20% overspeed %; 21: over speed 20% - 30%;
													  22: over speed 30% - 50%; 23: over speed 50% - 70%; 24: over speed 70% - 100%; 25: over speed 100%; 26: over speed 20% - 50%; 27: over speed 50%; 28: illegal jam; 29: abnormal license plate; 30: green light parking;
													  31: dangerous chemicals are prohibited; 32: front and rear license plates do not match (deliberately blocking the motor vehicle license plate); 33: non motor vehicles do not pass according to the traffic signal regulations Yes;
													  34: pedestrians running red lights; 35: congestion; 36: non motor vehicle retrograde.*/ 
	char  		cCameraIP[LEN_16];					//Camera IP
	int			iRedBeginTime;						//Red light start time in seconds
	int			iRedEndTime;						//Red light end time in seconds
	int   		iPicCount;							//Record the number of images included
	int			iSize;								//The size of strcut PicData
	PicData*	ptPicData[MAX_ITS_CAP_PIC_COUNT];	//Capture picture data 
	int			iPlatCount;							//Number of license plate pictures 
	PicData*	ptPlatData;							//License plate picture data 
	int			iFaceCount;							//Number of face (driver's seat) pictures 
	PicData*	ptFaceData[MAX_ITS_CAP_FACE_COUNT];	//Face (driving position) picture data 
	//
	int			iPreset;							//(The Ball ) preset position number 				
    int			iArea;								//(The Ball )Area number               			
    char		cFileName[LEN_32];					//Video file name 
    int			iCarSerialNum;						//No. of parked vehicle 				
    int			iPictureNum;						//No. of pictures of vehicles in violation of parking regulations 				
	int			iPicType;							//Invalid field, no actual meaning 					
	int			iPlatePicLen;						//Data length of small license plate 					
	int			iFacePicNum;						//Number of small face pictures 				
	char 		cChannelName[LEN_64];				//Lane name 		
	char		cChannelDirection[LEN_64];			//Lane direction 
	char		cCrossingID[LEN_64];				//Intersection number 			
	char		cCrossingName[LEN_64];				//Intersection name 		
	int			iCarRegionNum;						//Vehicle contour number, which determines which image tcarregion is based on, starting from 0, where 0 represents the first one. 			
	RECT		tCarRegion;							//Vehicle contour coordinates, icarregionnum determines which image tcarregion is based on. Icarregionnum starts from 0, where 0 represents the first image. 				
	int			iTargetType;						//Target type machine is not human ,Car model （ It is not recommended to use this field ,Suggested use iNewTargetType）
													//The old TargetType corresponds to new TargetType.0:UNKOWN(NewTargetType:0),1::BUS(NewTargetType:8),2:CAR(NewTargetType:2),3:TRUCK(NewTargetType:3),4:VAN(NewTargetType:10),5:BIG_TRUCK(NewTargetType:12),6:SMALL_TRUCK(NewTargetType:17),7:NOMOTOR(NewTargetType:20),8:PERSON(NewTargetType:22),9:SUV(NewTargetType:7),10:MID_BUS(NewTargetType:9),11:PLATE_ASKEW(NewTargetType:29),
													
	int			iFacePicLen[3];						//Face small graph length 				
	int			iRecordAttr;						//Record attribute 0- General record ，1- Take the first record before and after ，2- Second records before and after capture 				
	int 		iLinkageChannelNo;					//Lane numbers for associated cameras ,Use before and after snapping 				
	int			iLinkageNo;							//Linkage number, greater than 0 random number, the same camera linkage number before and after the same capture record, valid before and after pure video capture, others are - 1  		
	char		cLinkageCameraIP[LEN_16];			//Linkage camera IP,Use before and after snapping 	
	int			iVehicleBrand;						/*Auto Logos ，0:Unknown ；1:Public ；2:Audi ；3:Toyota ；4:Honda ；5:Benz ；6:Chevrolet ；7:Chery ；8:Buick ；9:The Great Wall ；10:modern ；11:NISSAN ；12:Ford ；
													  13:BMW ；14:Citroen ；15:kIa ；16:Suzuki ；17:Mazda ；18:BYD ；20:Beautiful ；21:Changan ；22:LEXUS ；23:The ZH people ；24:SKODA ；25:A hippocampus ；26:Charade ；
													  27:Wuling ；28:East wind ；29:Hafei ；30:FAW ；31:Bao Jun ；32:The Emperor ；33:The name of the name ；34:Southeast ；35:An crown ；36:Golden cup ；37:Mitsubishi ；38:Roewe ；
													  39:Auspicious ；40:English ；41:global hawk ；42:Hafei Saibao ；43:Changfeng ；44:Wei Wang ；45:Beijing ；46:New Kay ；47:Gio ；48:Maserati ；49:rover ；50:Austen ；51:Ma's ；
													  52:dodge ；54:General Pontiac ；55:Jaguar ；56:Ozzy's dawn ；57:alpha ；58:Lamborghini ；59:Bugatti ；60:Lincoln ；61:Ferrari ；62:Changhe ；63:Fiat ；64:Fukuda ；65:Eulogize ；
													  66:Lotus flower ；67:Huapu ；68:red flag ；69:Riich ；70:Pentium ；71:Wei Lin ；72:Zhongtai ；73:Lifan ；74:Beijing jeep ；75:ZTE ；76:karry ；77:Land Rover ；78:Maybach ；
													  79:Renault ；80:Opel ；81:Wild horse ；82:Jeep ；83:Iveco ；84:Infiniti ；85:Subaru ；86:Aston Martin ；87:An Kai ；88:Porsche ；89:Bentley ；90:Fudi ；91:Fujian ；92:Jim ；93:View ；
													  94:Wide steam ；95:Shuanglong ；96:Hagrid ；97:Hummer ；98:Huatai ；99:Yellow Sea ；100:Kowloon ；101:Idea ；102：SMART；103:Land wind ；104:Na Ji Jie ；105:Oley ；106:Kai Chen ；
													  107:Haversian ；108:HOWO ；109:The crowd ；110:golden dragon ；111:Golden brigades ；112:Yangtze and Huai rivers ；113:Jiangling ；114:Cadillac ；115:Cheetah ；116:Mini ；117:Shaanxi steam ；
													  118:Shaolin Temple ；119:Volvo ；120:Isuzu ；121:Leap forward ；122:Yutong ；123:Zhong Tong ；124:Jinling ；125:Shen wo ；126:Yangzi River ；127:Tang Jun Ou Ling ；128:North Run ；129:Red Crag ；
													  130:Rolls-Royce ；131:Tesla ；132:Teng potential ；133:Field ；134:English ；135:West Yat ；136:Star ；137:Daewoo ；138:Alex ；139:Kaye ；140:chase ；141:Shi Ming ；142:Kama. */
	RECT_ITS	tFaceRegion;						//Face coordinates 				
	int			iAlgIllgalType;						//Algorithm detecting illegal types ,bit0 No seat belt ，bit1 Phone 	
	int			iAlarmCode;							//Illegal code,Old field, no longer maintained, please use cAlarmCode. 
	short		sMergePicNum;						//Picture synthesis information ,Photo synthesis number 
	short		sMergeType;							//Bit synthesis 
	short		sMergePercent;						//Synthesis ratio 
	short		sMergeOSDSize;						//Synthetic black edge height 
	short		sMergeOSDType;						//Synthetic black edge position ,0- Overlay in picture 1- Overlay in black box under picture 2- Overlay in black box on picture 
	short		sSingleOSDSize;						//Single black edge height 			
	short		sSingleOSDType;						//Single black edge position ,0- Overlay in picture 1- Overlay in black box under picture 2- Overlay in black box on picture 
	short		sOriginalImgWidth;					//Source image width 
	short		sOriginalImgHeight;					//Source image height 
	RECT_S		tVehicleRegion;						//Vehicle logo coordinates, this field is valid only when supporting and opening the vehicle logo recognition function, indicating the coordinates of the vehicle logo, the image serial number based on iRecrdoNum determines, otherwise the field is invalid.
	RECT_S		tCopilotRegion;						//Face coordinates of copilot 	
	char  		cCaptureTimeEx2[5][LEN_8];			//Capture time of each picture: year, month, day, week, hour, minute, second and millisecond. 
	int	  		iCaptureLenEx2[5];					//The length of each picture.
	char		cUserDefChannelID[LEN_64];			//Lane customization number 	
	short		sBodyOfCarLeight;					//Body length (cm)      
	char		cSecurityCode[LEN_8][LEN_36];		//Image security code 
	int			iSpeedPercent;						//Percentage of overspeed 
	char		cLinkPanoramaCapUUID[LEN_128];		//Linkage panoramic camera snapping linkage UID 
	int			iRedBeginTimeMS;					//Red light start time ( Millisecond ) 
	int			iRedEndTimeMS;						//Red End ( Millisecond )	
	int			iIPDCapType;						//Violation of stop the illegal record capture type ，0 automatic ，1 Manual 			
	char		cAlarmCode[LEN_16];					//Illegal code compatibility letter 
	int 		iCarFeatures[2];					/*Vehicle characteristic attributes, such as hazardous chemicals vehicle, tissue box, etc., by bit, 64 bit is supported. Bit0: do not wear safety belt, Bit1: make a phone call, bit2: main driver's sunshade, bit3: copilot's sunshade, 
													  bit4: dangerous chemicals vehicle, bit5: yellow label vehicle, bit6: hanging, bit7: tissue box, bit8: annual inspection label, bit9: high beam lamp, bit10: abnormal license plate, bit11: window standing person.*/ 
	STRCT_BRAND	stBrandInfo;						//Vehicle label information 
	int 		iMainDriverSex;						//Driving sex ，0:Unknown ；1:Male ；2:Female sex. 
	int  		iCopilpt;							//Co driving sex ，0:Unknown ；1:Male ；2:Female sex. 
	int			iNewTargetType;						/*Vehicle type ，0:Unknown model ；1:Hatchback car ；2:Sedan ；3:Coupe ；4:Small car ；5:Mini car ；6：MPV；7：SUV；8:Large bus ；9:Medium-sized passenger car ；10:Van ；11:Mini van ；12:Big truck ；13:Medium truck ；14:Fuel tank car ；15:crane ；
													  16:Part of the car ；17:Van ；18:Pickup ；19:Micro card ；20:dogcart ；21:Tricycle ；22:pedestrian ；23:License plate deviation ；24:License plate detection ；25:Head of a car ；26:Tail of a car ；27:Automative lighting ；28：SUV/ MPV；29:trailer;
													  100: bicycle; 101: electric vehicle; 102: motorcycle.*/ 
	int 		iRealImgWidth;						//image width ( Containing synthetic pictures )
	int 		iRealImgHeight;						//Image height ( Containing synthetic pictures )
	int			iBWPlatePicLen;						//The data length of small license plate picture after two-value 
	char*		pcBWPlateData;						//Small license plate picture data after two-value 
	//
	int				  iPlateRecognizeLen;			//License plate recognition result data segment data length 
	RecgPlateList*	  pPlateRecognize;				//License plate recognition result data (whistle scratching) 
	int				  iITS_TCZInfoLen;				//Weighing vehicle ID data length 
	ITS_TCZInfo*	  pITS_TCZInfo;					//Weighing vehicle ID 
	int				  iRefIdLen;
	PicRefId*		  pPicRefId;
	int				  iPicTypeLen;
	PicType*		  pPicType;
	int				  iFeatureInfoLen;
	SingleFeatureInfo* pFeatureInfoData;
	int				  iEventTypeLen;
	RadarEventType*	  pEventType;
	int				  iPlateInfoExLen;
	ItsPlateInfoEx*	  pPlateInfoEx;
    int				  iAlarmTypeLen;      // AlarmType length
    AlarmEventType*   pAlarmType;         // AlarmType data
	int               iCrossNameV2Len;      
	CrossNameV2*      pCrossNameV2; 
	char			  cAlarmTime[LEN_17];
	PicTime           tAlarmTime;
	int					   iAllowCaptureDirectionLen;
	AllowCaptureDirection *pAllowCaptureDirection;// Allow Capture Direction 增加允许抓拍的车辆行驶方向
	int					iOutOfTriggerSanpLen;
	OutOfChekPointTriggerSnap *ptOutOfTriggerSnap;				//外触发抓拍ID
	int				iItsPicLocalFileNameLen;					// 抓拍车辆图片名结构体长度
	ItsPicLocalFileNameData *pItsPicLocalFileNameData;	// 抓拍车辆图片名结构体指针
} ItsPicStream, *pItsPicStream;

//Direction of the vehicle macro definition.[iVehicleDirection]
#define VEHICLE_DIRECTION_NULL			0	//No direction
#define VEHICLE_DIRECTION_DIRECT		1	//Go straight
#define VEHICLE_DIRECTION_CONVERSE		2	//converse
#define VEHICLE_DIRECTION_UP			3	//up
#define VEHICLE_DIRECTION_DOWN			4	//down
#define VEHICLE_DIRECTION_LEFT			5	//left
#define VEHICLE_DIRECTION_RIGHT			6	//right

//Alarm type macro definition.[iAlarmType]
#define ITS_ALARM_TYPE_ISOK						1	//卡口
#define ITS_ALARM_TYPE_CONTRAYDIRET				2	//逆行
#define ITS_ALARM_TYPE_RED						3	//闯红灯
#define ITS_ALARM_TYPE_INNOMOTOR_VEHICLE		4	//车辆驶入非机动车道,机动车不在机动车道内行驶的
#define ITS_ALARM_TYPE_OVERLINE					5	//越线1042 机动车不按规定车道行驶的,不按导向行驶
#define ITS_ALARM_TYPE_OFFEND_STIPULATE			6	//机动车违反禁止标线指示的
#define ITS_ALARM_TYPE_STOP_NOPARKIN_GREGION	7	//遇前方机动车停车排队等候或者缓慢行驶时，在人行横道、网状线区域内停车等候
#define ITS_ALARM_TYPE_OVER_SPEED				8	//一般超速
#define ITS_ALARM_TYPE_OVER_HIGHSPEED			8	//严重超速
#define ITS_ALARM_TYPE_ILLEGAL_TURNROUND		9	//违法掉头
#define ITS_ALARM_TYPE_BUS_RUNONLY				10	//机动车违法使用专用车道
#define ITS_ALARM_TYPE_VECHILE_REVESE			11	//不按规定倒车行驶
#define ITS_ALARM_TYPE_CHANGE_ROAD				12	//违法变道
#define ITS_ALARM_TYPE_DRIVEIN_CLOSEDROAD		13	//闯禁行
#define ITS_ALARM_TYPE_SAFETY_BELT				14	//不系安全带
#define ITS_ALARM_TYPE_CALLCELL_PHONE			15	//接打电话
#define ITS_ALARM_TYPE_COMITY_PEDESTRAIN		16	//机动车不礼让行人
#define ITS_ALARM_TYPE_COMITY_STRAIGHT			17	//左转车不礼让直行车
#define ITS_ALARM_TYPE_OVERTAKEIN_ZEBRACROSSING	18	//斑马线超车
#define ITS_ALARM_TYPE_SPEED_ZEROTOTEN			19	//超速0%-10% code,	6050
#define ITS_ALARM_TYPE_SPEED_TENTOTWENTY		20	//超速10%-20%	13521
#define ITS_ALARM_TYPE_SPEED_TWENTYTOTHIRTY		21	//超速20%-30%	16366
#define ITS_ALARM_TYPE_SPEED_THIRTYTOFIFTY		22	//超速30%-50%	16367
#define ITS_ALARM_TYPE_SPEED_FIFTYTOSEVENTY		23	//超速50%-70%	17215
#define ITS_ALARM_TYPE_SPEED_SEVENTYTOHUNDRED	24	//超速70%-100%	17216
#define ITS_ALARM_TYPE_SPEED_OVERHUNDRED		25	//超速大于100%	17217
#define ITS_ALARM_TYPE_SPEED_TWENTYTOFIFTY		26	//超速20%-50%	1636
#define ITS_ALARM_TYPE_SPEED_OVERFIFTY			27	//超速大于50%	1721
#define ITS_ALARM_TYPE_INSERT_TRAFFICJAM		28	//违法加塞
#define ITS_ALARM_TYPE_ABNORMAL_PLATE			29	//异常车牌
#define ITS_ALARM_TYPE_GREENLIGHT_PARKING		30	//绿灯停车
#define ITS_ALARM_TYPE_PROHIBITION_DANGEROUSCAR	31	//禁止危化品
#define ITS_ALARM_TYPE_MISMATCH_PLATE			32  //前后车牌不匹配(故意遮挡机动车号牌的)，1718A
#define ITS_ALARM_TYPE_MOTOR_RED				33  //非机动车不按照交通信号规定通行的 ，20071
#define ITS_ALARM_TYPE_PEOPLE_RED 				34  //行人闯红灯，1
#define ITS_ALARM_TYPE_DRIVEINTO_JAMCROSS  	    35  //闯拥堵 ，2
#define ITS_ALARM_TYPE_MOTOR_REVERSE  			36  //非机动车逆行,3
#define ITS_ALARM_TYPE_CoachOtherWay0to10		37  //大客车其他道路超速0%-10% 6047
#define ITS_ALARM_TYPE_TruckOtherWay0to10	    38  //大货车其他道路超速0%-10% 6048
#define ITS_ALARM_TYPE_CoachOtherWay10to20		39  //大客车其他道路超速10%-20% 1349
#define ITS_ALARM_TYPE_TruckOtherWay10to20		40  //大货车其他道路超速10%-20% 1350
#define ITS_ALARM_TYPE_CoachOtherWay20to50		41  //大客车其他道路超速20%-50% 1632
#define ITS_ALARM_TYPE_TruckOtherWay20to50		42  //大货车其他道路超速20%-50% 1633
#define ITS_ALARM_TYPE_CoachCityWay0to20		43	//大客车城市快速路超速0%-20% 1628
#define ITS_ALARM_TYPE_TruckCityWay0to20		44	//大货车城市快速路超速0%-20% 1629
#define ITS_ALARM_TYPE_CoachCityWay20to50		45	//大客车城市快速路超速20%-50% 1722
#define ITS_ALARM_TYPE_TruckCityWay20to50		46	//大货车城市快速路超速20%-50% 1723
#define ITS_ALARM_TYPE_CoachOtherAndCityOver50  47  //大客车高速公路以外道路超速50%以上 1726
#define ITS_ALARM_TYPE_TruckOtherAndCityOver50  48  //大货车高速公路以外道路超速50%以上 1727
#define ITS_ALARM_TYPE_CoachHighWay0to20		49  //大客车高速公路超速0%-20% 4609
#define ITS_ALARM_TYPE_TruckHighWay0to20		50  //大货车高速公路超速0%-20% 4610
#define ITS_ALARM_TYPE_CoachHighWay20to50		51  //大客车高速公路超速20%-50% 4706
#define ITS_ALARM_TYPE_TruckHighWay20to50		52  //大货车高速公路超速20%-50% 4707
#define ITS_ALARM_TYPE_CoachHighWayOver50		53  //大客车高速公路超速50%以上 4710
#define ITS_ALARM_TYPE_TruckHighWayOver50		54  //大货车高速公路超速50%以上 4711
#define ITS_ALARM_TYPE_CarHorn					55  //违法鸣笛
#define ITS_ALARM_TYPE_NoAlternatePassage		56  //不交替通行违法
#define ITS_ALARM_TYPE_NoKeepSafeDis			57  //不保持安全车间距
#define ITS_ALARM_TYPE_OtherCityVehicleRestriction  58  //外地车禁行
#define ITS_ALARM_TYPE_NoHelmet					59  //不佩戴头盔
#define ITS_ALARM_TYPE_PressureConductingLine   60  //压导流线
#define ITS_ALARM_TYPE_MotorcycleProhibited		61  //摩托车禁行
#define ITS_ALARM_TYPE_DriveIntoTheBusLane		62  //驶入公交车道
#define ITS_ALARM_TYPE_DriveIntoTheEmergencyLane   63  //驶入应急车道
#define ITS_ALARM_TYPE_PressureLane				64  //压车道线
#define ITS_ALARM_TYPE_CopilotsNoSeatBelt		65 //30190-副驾驶不系安全带
#define ITS_ALARM_TYPE_MeduimSizePassangerExpresswaySpeed50to70     66		//中型客车快速路超速50%-70%
#define ITS_ALARM_TYPE_MeduimSizeTruckExpresswaySpeed50to70			67	    //中型货车快速路超速50%-70%
#define ITS_ALARM_TYPE_MeduimSizePassangerExpresswaySpeed70to100	68		//中型客车快速路超速70%-100%
#define ITS_ALARM_TYPE_MeduimSizeTruckExpresswaySpeed70to100		69		//中型货车快速路超速70%-100%
#define ITS_ALARM_TYPE_MeduimSizePassangerExpresswaySpeedOver100	70		//中型客车快速路超速100%
#define ITS_ALARM_TYPE_MeduimSizeTruckExpresswaySpeedOver100		71		//中型货车快速路超速100%
#define ITS_ALARM_TYPE_MeduimSizePassangerOtherWaySpeed50to70		72		//中型客车其他道路超速50%-70%
#define ITS_ALARM_TYPE_MeduimSizeTruckOtherWaySpeed50to70			73		//中型货车其他道路超速50%-70%
#define ITS_ALARM_TYPE_MeduimSizePassangerOtherWaySpeed70to100		74		//中型客车其他道路超速70%-100%
#define ITS_ALARM_TYPE_MeduimSizeTruckOtherWaySpeed70to100			75		//中型货车其他道路超速70%-100%
#define ITS_ALARM_TYPE_MeduimSizePassangerOtherWaySpeedOver100		76		//中型客车其他道路超速100%
#define ITS_ALARM_TYPE_MeduimSizeTruckOtherWaySpeedOver100			77		//中型货车其他道路超速100%
#define ITS_ALARM_TYPE_MeduimSizePassangerHighwaySpeed50to70		78		//中型客车高速公路超速50%-70%
#define ITS_ALARM_TYPE_MeduimSizeTruckHighwaySpeed50to70			79		//中型货车高速公路超速50%-70%
#define ITS_ALARM_TYPE_MeduimSizePassangerHighwaySpeed70to100		80		//中型客车高速公路超速70%-100%
#define ITS_ALARM_TYPE_MeduimSizeTruckHighwaySpeed70to100			81		//中型货车高速公路超速50%-70%
#define ITS_ALARM_TYPE_MeduimSizePassangerHighwaySpeedOver100		82		//中型客车高速公路超速100%
#define ITS_ALARM_TYPE_MeduimSizeTruckHighwaySpeedOver100			83		//中型货车高速公路超速100%
#define ITS_ALARM_TYPE_NONMOTOR_VEHICLE_INMOTOR_LAN					84		//非机动车在机动车道内行驶的

/*******************************Face picture stream*****************************************/
typedef struct tagFaceAttribute
{
	int iType;
	int iValue;
}FaceAttribute;

typedef struct tagPicStreamEncrptKey 
{
	int				iEncryptionMethod;				//算法类型：默认0，用户自定义
	int				iFullViewPicKeyLen;				//全景图密钥长度	
	int				iFacePicKeyLen;					//人脸小图密钥长度	
	int				iBasePicKeyLen;					//人脸底图密钥长度	
	unsigned char	ucFullViewPicKey[LEN_256];		//全景图密钥			
	unsigned char	ucFacePicKey[LEN_256];			//人脸小图密钥
	unsigned char	ucBasePicKey[LEN_256];			//人脸底图密钥		
} PicStreamEncrptKey;

#define MAX_FACE_PICTURE_COUNT		32
typedef struct tagFacePicData
{
	int			iFaceId;				//Face track Id
	int			iDrop;					//This field is discarded,reference field iTrack. Out of date meaning[The face track ends, 0 is not over, 1 ends].
	int			iFaceLevel;				//Face quality,0-100
	RECT		tFaceRect;				//Face coordinates
	int			iWidth;					//Face wide picture of the wide
	int			iHeight;				//Face small picture of the high
	int			iFaceAttrCount;			//Number of face attributes
	int			iFaceAttrStructSize;	//The size of strcut FaceAttribute
	FaceAttribute*	ptFaceAttr[LEN_256];//Face attributes,supports up to 256 attribute types,the subscript is the face attribute type:										//0-age,1-gender,2-masks,3-beard,4-eye open,5-mouth,6-glasses,7-race,8-emotion,9-smile,10-value......
	int			iDataLen;				//Face picture length
	char*		pcPicData;				//Small figure bare data, the upper can be directly saved as a picture
	unsigned long long	ullPts;			//Timestamp of small picture

	int			iAlramType;				//0-人脸检出，1-比对报警，2-陌生人报警，3-频次报警，4-时长报警，5-结构化检测
	int			iSimilatity;			//0~100
	int			iLibKey;				//library key id
	int			iFaceKey;				//face key id
	int			iNegPicLen;				//negative picture len
	char*		pcNegPicData;			//negative picture data
	int			iNegPicType;			//negative picture type
	int			iSex;
	int			iNation;
	int			iPlace;					//negative place
	int			iCertType;				//credentials type
	char		cCertNum[LEN_64];
	char		cBirthTime[LEN_16];
	char		cName[LEN_64];
	char		cLibName[LEN_64];
	char		cLibUUID[LEN_64];
	char		cFaceUUID[LEN_64];
	char		cVerifyCode[LEN_36];
	int			iFeatureLen;
	char*		pcFeatureData;
	char		cFaceJpegName[LEN_64];  //full pic file name
	char		cJpegName[LEN_64];	    //small facepic file name
	int			iTrack;					//0:reserve 1:track end 2:track not end
} FacePicData, *pFacePicData; //Face map information

//Face attribute type macro definition
#define FACEATTR_TYPE_FACEATTR_AGE				0	//age
#define FACEATTR_TYPE_GENDER					1	//gender 0:female 1:male
#define FACEATTR_TYPE_MASKS					2	//masks 0-none, 1-yes
#define FACEATTR_TYPE_BEARD					3	//beard 0-none, 1-yes
#define FACEATTR_TYPE_EYEOPEN					4	//eye open 0-none, 1-yes
#define FACEATTR_TYPE_MOUTH					5	//mouth 0-none, 1-yes
#define FACEATTR_TYPE_GLASSES					6	//glasses 0-none, 1-normal, 2-sun glasses, 3:other
#define FACEATTR_TYPE_RACE					7	//race 0-yellow, 1-black, 2-white, 3-the Uygurs
#define FACEATTR_TYPE_EMOTION					8	//emotion 0-angry, 1-calm, 2-disgust, 3-confused, 4-happy, 5-sad, 6-scared, 7-surprised, 8-squint, 9-scream
#define FACEATTR_TYPE_SMILE					9	//smile 0-none, 1-yes
#define FACEATTR_TYPE_VALUE					10	//value
#define FACEATTR_TYPE_NATION					11	//nation 0-han, 1-minority
#define FACEATTR_TYPE_FACE_PROBABILITY                          12	//face probability KS debugging information, value 0-100, more than 50 for non face
#define FACEATTR_TYPE_3D_POS1					13	//face left right angle KS debugging information, you need to subtract 1000 when parsing
#define FACEATTR_TYPE_3D_POS2					14	//face left right angle KS debugging information, you need to subtract 1000 when parsing
#define FACEATTR_TYPE_3D_POS3					15	//face left right angle KS debugging information, you need to subtract 1000 when parsing
#define FACEATTR_TYPE_BLUR					16	//blur KS debugging information
#define FACEATTR_TYPE_REFID					17	//ref id
#define FACEATTR_TYPE_OBJTYPE					18	//object type,0-face, 1-motor vehicle, 2-non motor vehicle, 3-pedestrians
#define FACEATTR_TYPE_BACKPACK					19	//backpack,0-none, 1-yes, 2-single shoulder, 3-double shoulder, 4-handle, 5-waist bag, 6-pull rod
#define FACEATTR_TYPE_DIRECTION					20	//movement direction,0-up, 1-down, 2-left, 3-right
#define FACEATTR_TYPE_COLOR_UPPERBODY                           21	//upper body color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_COLOR_LOWERBODY                           22	//lower body color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_HAIR					23	//hair,0-short hair, 1-long hair, 2-less hair, 3-others
#define FACEATTR_TYPE_LONG_SLEEVE				24	//long sleeve,0-no, 1-yes
#define FACEATTR_TYPE_SPEED					25	//speed,0-stationary, 1-moving
#define FACEATTR_TYPE_HAT					26	//hat,0-none, 1-yes, 2-brimless cap, 3-cap, 4-fisherman's cap
#define FACEATTR_TYPE_LONG_PAINTS				27	//long paints,0-none, 1-yes
#define FACEATTR_TYPE_SHORT_SKIRT				28	//short skirt,0-none, 1-yes
#define FACEATTR_TYPE_RIDE					29	//ride,0-none, 1-yes
#define FACEATTR_TYPE_FREQUENCY_ALARM_TIME                      30	//frequency alarm time,When the alarm type is frequency alarm: frequency alarm times > 0;When the alarm type is detention alarm: detention alarm duration > 0 (unit: Second)
#define FACEATTR_TYPE_SCENE					31	//scene,value-[0,65535]
#define FACEATTR_TYPE_NATIONAL_PROBABLITY                       32	//National probability,[0100], more than 50 are ethnic minorities
#define FACEATTR_TYPE_FACE_ATTR_CREDIBILITY                     33	//Face attribute credibility,0-low 1-high
#define FACEATTR_TYPE_COLOR_HAIR				34	//hair color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_COLOR_HAT					35	//hat color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_COLOR_MASK				36	//mask color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_STYLE_SHOE				37	//shoe style,0-leather shoes, 1-boot, 2-casual shoes, 3 sandals
#define FACEATTR_TYPE_COLOR_SHOE				38	//shoe color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_RESERVE					39	//reserve
#define FACEATTR_TYPE_COLOR_BAG					40	//bag color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_UMBRELLA					41	//umbrella,0-without umbrella, 1-with umbrella;
#define FACEATTR_TYPE_COLOR_UMBRELLA                            42	//umbrella color,0-unknown, 1-white, 2-gray, 3-brown, 4-red, 5-blue, 6-yellow, 7-green, 8-pink, 9-orange, 10-green,11-purple, 12-light blue, 13-black, 14-color
#define FACEATTR_TYPE_STYLE_UPPERBODY                           43	//upper body style,0-long coat; 1-jacket and jeans; 2-T-shirt; 3-sportswear; 4-down jacket; 5-shirt; 6-dress; 7-suit
#define FACEATTR_TYPE_PATTERN_UPPERBODY                         44	//upper body pattern,0-solid color; 1-stripe; 2-pattern; 3-stitching; 4-grid
#define FACEATTR_TYPE_STYLE_PAINTS				45	//paints style,0-No; 1-has; 2-trousers; 3-skirt; 4-shorts
#define FACEATTR_TYPE_PATTERN_PAINTS                            46	//paints pattern,0-solid color; 1-stripe; 2-pattern; 3-stitching; 4-grid
#define FACEATTR_TYPE_HUG_THINGS				47	//hug things,,0-none, 1-yes
#define FACEATTR_TYPE_HUMAN_ANGLE				48	//human angle,0-frontal; 1-lateral; 2-dorsal;
#define FACEATTR_TYPE_IS_TRACK_ENDS				49	//is track ends,0-reserved; 1-track ended; 2-track not ended
#define FACEATTR_TYPE_IS_LIVING					50	//is living,0-reserved; 1-living; 2-non living
#define FACEATTR_TYPE_TEM_VALUE					51  //temperature value,The value is the actual temperature value * 100 + 100000, used with 52
#define FACEATTR_TYPE_TEM_UNIT					52  //temperature unit,1-Centigrade scale 2-Fahrenheit scale
#define FACEATTR_TYPE_ABNORMAL_ALARM                            53  //temperature abnormal alarm,0-reserved, 1-high temperature alarm
#define FACEATTR_TYPE_OBJTYPE2					54  //objtype 2,0-face, 1-motor vehicle, 2-non motor vehicle, 3-pedestrian, 4-license plate
#define FACEATTR_TYPE_OBJTYPEID2				55  //objtype id 2

//Face attribute value macro definition
#define FACEATTR_GENDER_FEMALE			0	//female
#define FACEATTR_GENDER_MALE			1	//male
#define FACEATTR_MASKS_NOT				0	//not masks
#define FACEATTR_MASKS_HAVE				1	//have masks
#define FACEATTR_BEARD_NOT				0	//not beard
#define FACEATTR_BEARD_HAVE				1	//have beard
#define FACEATTR_EYEOPEN_NOT			0	//no eyes open
#define FACEATTR_EYEOPEN_HAVE			1	//eyes open
#define FACEATTR_MOUTH_NOT				0	//no mouth
#define FACEATTR_MOUTH_HAVE				1	//mouth
#define FACEATTR_GLASSES_NOT			0	//not wear glasses
#define FACEATTR_GLASSES_NORMAL			1	//ordinary glasses
#define FACEATTR_GLASSES_SUN			2	//sunglasses
#define FACEATTR_RACE_YELLOW			0	//yelloe people
#define FACEATTR_RACE_BLACK				1	//black people
#define FACEATTR_RACE_WHITE				2	//Caucasian
#define FACEATTR_RACE_UIGHUR			3	//uighur
#define FACEATTR_EMOTION_ANGRY			0	//angry
#define FACEATTR_EMOTION_CALM			1	//calm
#define FACEATTR_EMOTION_CONFUSED 		2	//confused
#define FACEATTR_EMOTION_DISGUST		3	//disgust
#define FACEATTR_EMOTION_HAPPY 			4	//happy
#define FACEATTR_EMOTION_SAD			5	//sad
#define FACEATTR_EMOTION_SCARED			6	//scared
#define FACEATTR_EMOTION_SURPRISED		7	//surprised 
#define FACEATTR_EMOTION_SQUINT			8	//Strabismus
#define FACEATTR_EMOTION_SCREAM			9	//scream 
#define FACEATTR_NATION_HAN				0	//Han nationality
#define FACEATTR_NATION_MINORITY		1	//minority
#define FACEATTR_OBJTYPE_FACE			0	//face
#define FACEATTR_OBJTYPE_VEHICLE		1	//vehicle
#define FACEATTR_OBJTYPE_NONMOTOR		2	//nonmotor
#define FACEATTR_OBJTYPE_PEDESTRIAN		3	//pedestrian
#define FACEATTR_DIRECTION_UP			0	//up
#define FACEATTR_DIRECTION_DOWN			1	//down
#define FACEATTR_DIRECTION_LEFT			2	//left
#define FACEATTR_DIRECTION_RIGHT		3	//right


#define FACE_ALARM_TYPE_CHECKOUT		0
#define FACE_ALARM_TYPE_BLACKLIST		1
#define FACE_ALARM_TYPE_WHITELIST		2

typedef struct tagFreqAlarmData
{
	int			iStartTime;  //开启时间 
	int			iEndTime;  //结束时间 
	int			iFreqCounts;  //频次次数
	int			iGroupId;  //组号
	int			iChnList[8];  //关联通道号，按位从低位开始，第0位代表通道1
	char	 	cReserve[16];
}FreqAlarmData;

typedef struct tagPicStreamStrangerAttr
{
	int				iValid;  //0-陌生人属性无效,1-陌生人属性有效
	int				iStrangerFacekey;  //人脸ID
	int				iStrangerLibkey;  //人脸库ID
	char			cStrangerLibuuid[LEN_64];  //人脸库对应平台UUID
	char			cReserve[LEN_64];   //Faceuuid NVR自动产生人脸UUID，该UUID每次唯一，入库失败后仍然不能重复
}PicStreamStrangerAttr;

typedef struct tagPicStreamStrangerType
{
	int	iStrangerAttrArryNum;//当前帧检测人脸个数，最大值为MAX_FACE_PICTURE_COUNT
	PicStreamStrangerAttr	tPicStreamStrangerAttrArry[MAX_FACE_PICTURE_COUNT];//陌生人信息结构体数组，ptPicStreamStrangerAttrArry所取小图下标对应FacePicData的小图下标。
} PicStreamStrangerData; //此结构体之后不再扩展

typedef struct tagFacePicStream
{
	int 			iStructLen;			//Structure length
	int				iSizeOfFull;		//The size of strcut PicData
	PicData*		ptFullData;			//The data of full screen
	int				iFaceCount;			//The current frame detects the number of face
	int				iSizeOfFace;		//The size of strcut FacePicData
	FacePicData*	ptFaceData[MAX_FACE_PICTURE_COUNT];
	int				iFaceFrameId;		//The face jpeg frame no
	PicTime			tNewPicTime;		//The picture capture time, ptFullData contain time is out of date
    int             iAlarmTypeLen;      // AlarmType length
    AlarmEventType* pAlarmType;         // AlarmType data
	int             iUserInfoLen;       //Userinfo length
	UserInfo*		pUserInfo;          //User ID
	int				iTargetCordinateLen;//face target world cordinate length
	FacePicStreamCpcInfo* pPicStreamCpcInfo; //face target world cordinate
	int				iCurStreamPeopleInfo;
	CurrentPicPeopleNum*  pCurrentPeoleNum;	//Current frame number
	int						iSizeOfPicStreamEncrptKey;	//size of struct PicStreamEncrptKey
	PicStreamEncrptKey*		ptEncrptKey;				//图片加密密钥参数
	int				iFreqAlarmLen;						// Alarm frequency length
	FreqAlarmData	*pFreqAlarmData;					// Alarm frequency data
	int 			iPicStreamStrangerLen;	  //picture stream stranger attr length
	PicStreamStrangerData *pPicStreamStrangerData; //	pPicStreamStrangerData最大只能访问iPicStreamStrangerLen个内存 	
} FacePicStream, *pFacePicStream;
 
/******************************Custome Snap picture stream***********************************/
#define MAX_SNAP_PICTURE_COUNT		32
typedef struct tagSnapPicData
{
	int 		iSnapType;		//Snap type
	int 		iWidth;			//Picture wide
	int 		iHeight;		//Picture high
	int			iSize;			//The size of strcut PicData
	PicData*	ptPicData;
} SnapPicData, *pSnapPicData;

typedef struct tagNavigationSnap
{
	int 		iSnapId;		
	char 		cReserve[LEN_8];
}NavigationSnap;


//Universal capture type macro definition
#define SNAP_TYPE_ALARM_NONE			0
#define SNAP_TYPE_ALARM_MANUAL			1
#define SNAP_TYPE_ALARM_TIMING			2
#define SNAP_TYPE_ALARM_PORT			3
#define SNAP_TYPE_ALARM_MOTION			4
#define SNAP_TYPE_ALARM_VIDEO_LOST		5
#define SNAP_TYPE_ALARM_VIDEO_COVER		6
#define SNAP_TYPE_ALARM_TEM_HUM			7
#define SNAP_TYPE_ALARM_VIDEO_MIXALM	8
#define SNAP_TYPE_ALARM_ELEV			9
typedef struct tagPicStreamSnapType
{
	char			m_cSnapType;  //1：预置位抓拍 
	char	 	    m_cReserve[7];
}PicStreamSnapType, *pPicStreamSnapType;

typedef struct AlarmElevatorDetectionSubType
{
	int     iChn;         // 通道
	int     iSubAlarm;       // 报警子类型0:冲顶故障, 1:蹲底故障, 2:电梯异常急停, 3:电梯超速, 4:门区外停梯, 5:到站后不开门, 6:电梯晃动, 7:开门走梯, 8:困人, 9:主动报警 10:传感器故障 11:电动车入梯 12：跌落 13： 安全回路报警 14：视频遮挡报警 15：未关门报警
	int     iReserve[4];
}AlarmElevatorDetectionSubData;

typedef struct AlarmTemHumDetectionSubType
{
	int     iChn;         // 通道
	int     iSubAlarm;       // 报警子类型0:温度上限报警；2:温度下限报警；3:湿度上限报警；4:湿度下限报警
	int     iReserve[4];
}AlarmTemHumDetectionSubData;

typedef struct tagSnapPicStream
{
	int 	iStructLen;				//Structure length
	int		iPicCount;				//Record the number of images included
	int		iSize;					//The size of strcut SnapPicData
	SnapPicData* ptSnapData[MAX_SNAP_PICTURE_COUNT];
	int		iSceneSnapLen;
	RecSceneSnap*	  pRecSceneSnap;
	char			  cAlarmTime[LEN_17];
	PicTime			  tAlarmTime;
	int				  iSnapTypeLen;
	PicStreamSnapType* ptSnapType;
	int				   iSessionLen;
	NavigationSnap *   ptSnapSessionID;
	int				   iAlarmMDMSubTypeLen;
	TAlarmMDMotionDetectionSubSnapType *ptTAlarmMotionSubSnapType; //移动报警子功能报警状态
	int 			iAlarmElevatorDetectionSubLen;
	AlarmElevatorDetectionSubData *pAlarmElevatorDetectionSubData; //梯控报警子功能报警状态
	int 			iAlarmTemHumDetectionSubLen;
	AlarmTemHumDetectionSubData *pAlarmTemHumDetectionSubData;	   //温湿度报警子功能报警状态
} SnapPicStream, *pSnapPicStream;




/******************************Calibtration picture stream***********************************/
#define MAX_CALIBRATION_PICTURE_COUNT				2

/*iCalibrationLevel*/
#define IMAGE_CALIBRATION_LEVEL_RESERRVED			0
#define IMAGE_CALIBRATION_LEVEL_ONE					1
#define IMAGE_CALIBRATION_LEVEL_TWO					2
#define IMAGE_CALIBRATION_PMF						3 //PMF 标定 补充之前漏的协议
#define IMAGE_CALIBRATION_DBP						4 //PC 端坏点矫正
#define IMAGE_CALIBRATION_SPLICING_JPEG				5 //拼接相关的JPEG图片
typedef struct tagCalibrationPicData
{
	int 		iCalibrationLevel;		//Calibration Level
	int 		iSensorNum;				//sensor no.
	int 		iWidth;					//Picture wide
	int 		iHeight;				//Picture high
	PicTime		tPicTime;
	
	int			iDataLen;				//Image length
	char*		pcPicData;				//Picture raw data, the upper can be directly saved as a picture
} CalibrationPicData, *pCalibrationPicData;

typedef struct tagRawMediaAttr
{
	int				iFrameId;						// 帧 id
	int				iFrameSeq;						// 帧序列号
	int				iIspPipe;						// isp 所在的 pipe 号
	int				iBayerFmt;						// raw 格式
	int				iBitWidth;						// 位宽
	int				iSnrMirror;						// 是否已开启 sensor 镜像功能
	int				iSnrFlip;						// 是否已开启 sensor 翻转功能
	int				iWidth;							// 宽度
	int				iHeight;						// 高度
	int				iFrameSize;						// 帧大小
	int				iStride[3];						// 跨度
	unsigned int	m_uiBufLen;						// 存放 raw 数据的内存长度，调用方提供
	unsigned long long m_ullTimeStamp;				// 时间戳
	int				m_iResv[LEN_16];				// 预留位
}RawMediaAttr, *pRawMediaAttr;

typedef struct tagCalibrationPicStream
{
	int 					iStructLen;				//Snap type
	int						iPicCount;
	int						iSize;					//The size of strcut CalibrationPicData
	CalibrationPicData*		ptCalibrationPicData[MAX_CALIBRATION_PICTURE_COUNT];
	int						iCaliImgInfoLen;
	PicStreamCaliImgInfo*	pCaliImgInfo;
	int						iRawAttrLen;
	RawMediaAttr*			pRawMediaAttrInfo;
	int						iRawDataLen;
	char*					pcRawData;
} CalibrationPicStream, *pCalibrationPicStream;


/*******************************Radar Track Stream*****************************************/

typedef struct tagRadarTrackStream
{
	int					iStructLen;					//Structure length
	//TODO:	Radar Trajectory flow to be expanded 	
} RadarTrackStream, *pRadarTrackStream;	


typedef struct tagTransAlgData
{
	int		iStructLen;			//Structure length
	int		iAlgDataLen;		//alg result data len
	void*	pvAlgData;			//alg result data, Note that the memory is applied for the internal stack of the SDK, and the memory will be released after callback. 
								//The upper software needs to copy the data in time in the callback. 
								//算法数据，注意该内存为sdk内部栈申请内存，回调完内存就会释放，上层软件需要在回调里将数据及时拷贝。
} TransAlgData, *pTransAlgData;



/*******************************picture stream end*****************************************/

typedef struct tagExceptionDiskTemp
{
	int 			iSize;
	int				iUpTemp;
	int				iLowTemp;
	int				iOffsetTemp;
}ExceptionDiskTemp, *pExceptionDiskTemp;

#define SYSTEM_TEMP_TYPE_DEFAULT			0
#define SYSTEM_TEMP_TYPE_DEVICE				1
#define SYSTEM_TEMP_TYPE_CPU				2
#define SYSTEM_TEMP_TYPE_EXTEND_MODULE		3
#define MAX_SYSTEM_TEMP_TYPE				4
typedef struct tagSystemInfoTemperature
{
	int 			iSize;
	int				iModuleType;
	int				iValue;
}SystemInfoTemperature, *pSystemInfoTemperature;

typedef struct tagSystemFanNum
{
	int 			iSize;	
	int				iTotalFanNum;
}SystemFanNum, *pSystemFanNum;

#define MAX_SYSTEM_FAN_SPEED_NUM			8
typedef struct tagSystemInfoFanSpeed
{
	int 			iSize;	
	int				iFanIndex;				//fan serial number (no range)
	int				iSpeedValue;	
}SystemInfoFanSpeed, *pSystemInfoFanSpeed;

#define MAX_VCA_HELMET_AREA_POINT_NUM		8
typedef struct tagVCAHelmet
{
	int				iSize;			
	VCARule			tRule;					//Rule general parameter
	vca_TDisplayParam	tDisplayParam;		//Display parameter
	int				iDisplayTarget;			//Whether the target box is displayed, [0, 1] Default 0
	int				iMaxSize;				//Maximum size, [0, 100] Default 15
	int				iMiniSize;				//Minimum size, [0, 100] Default 5
	int				iCalibrationSize;		//calibration size[0, 100] Default 15
	int				iSensitiv;				//Sensitivity(0~100)Default 80
	int				iProcType;				//0:inlet and outlet 1:construction region default 0
	int				iColorCheck;			//0:not Check 1:check default 0
	int				iAlarmSpecifiedColor;	//alram when find this color  bit0:non  bit1:red  bit2:blue bit3:white bit4:yellow bit31:other
	int				iPointNum;
	vca_TPoint		tPoint[MAX_VCA_HELMET_AREA_POINT_NUM];
}VCAHelmet,*pVCAHelmet;

typedef struct tagVCAHelmetSizeRange
{
	int				iSize;			
	int				iMaxSize;				//[0~100]
	int				iMinSize;				//[0~100]
}VCAHelmetSizeRange,*pVCAHelmetSizeRange;

typedef struct tagIpdWorkMode
{
	int				iSize;			
	int				iWorkMode;	
}IpdWorkMode,*pIpdWorkMode;

#define MAX_LEFT_COMITY_STRAIGHT_ROADWAY_NUM				4
#define MAX_LEFT_COMITY_STRAIGHT_DETECT_AREA_NUM			12			//max area num
#define MAX_LEFT_COMITY_STRAIGHT_DETECT_AREA_POINT_NUM		15			//max point num per area
typedef struct tagITSLeftComityStraightDetectArea
{
	int				iSize;	
	int				iRoadWayId;					//road way id
	int				iSceneId;					//reserved
	int				iRegionId;					//area id	1~12
	int				iEnabled;					//area enable 0:disable 1:enable
	int				iPointCount;				//4~15
	vca_TPoint		tPoint[MAX_LEFT_COMITY_STRAIGHT_DETECT_AREA_POINT_NUM];
}ITSLeftComityStraightDetectArea,*pITSLeftComityStraightDetectArea;

typedef struct tagVcaFpgaQueryInfo
{
	int				iSize;	
	int				iChannelNo;
	int				iType;
	int				iValue;
}VcaFpgaQueryInfo, *pVcaFpgaQueryInfo;

#define  MAX_VCAFPGA_TYPE 3
typedef struct tagVcaFpga
{
	int				iSize;	
	int				iChannelNo;
	int 			iTemplateIndex;
	int				iType;
	int			    iPARA1;
}VcaFpga, *pVcaFpga;

typedef struct tagUserActivation
{
	int iSize;
	int iActivation;
}UserActivation, *pUserActivation;

#define MAX_CCM_STAT_INFO_TYPE_NUM		4
#define MAX_CCM_STAT_INFO_NUM			24
typedef struct tagCcmCalibrate
{
	int 			iSize;
	int				iChanNo;
	int				iSensorNo;
	int				iCcmStatInfoType;
	int				iCcmStatInfo[MAX_CCM_STAT_INFO_NUM];
}CcmCalibrate, *pCcmCalibrate;

#define CMD_PROXY_LOGON_WAY			0
#define CMD_PROXY_ENCRYPTCONTENT	1



#define DDNSTEST_SUCCESS         		 0 //test success
#define DDNSTEST_IP_UPDATE_FAIL  		 1 //IP Update failed
#define DDNSTEST_ADD_HOST_FAIL	 		 2 //Add host failed
#define DDNSTEST_DELETE_HOST_FAIL		 3 //Delete host failed
#define DDNSTEST_HOST_NAME_INCORRECT     4 //Host name is incorrect
#define DDNSTEST_DOMAIN_NAME_INCORRECT   5 //Doamin name is incorrect
#define DDNSTEST_UNKNOWN_ERROR			 6 //Unknown error
#define DDNSTEST_DOMAINNAME_OCCUPIED	 7 //Domain name occupied 
#define DDNSTEST_SERVER_CONNECT_FAILED	 8 //Server Connect Failed
#define DDNSTEST_TESTING				 9 //Testing
typedef struct tagDDNSTest
{
	int					iBufSize;
	char				cUserName[LEN_128];	  //username
	char				cPassWord[LEN_128];   //password
	char				cNvsName[LEN_128];    //NVS name
	char				cDomainName[LEN_128]; //Domain name
	int					iPort;				  //Nvs port
	int					iType;					//0:NormalDDNS, 1:EasyDDNS
}DDNSTest,*pDDNSTest;

#define MAX_DZ_EX_PARAM_NUM					20
#define MAX_DZ_EX_TYPE_NUM					64
typedef struct tagDZCommonEx
{
	int					iBufSize;
	int					iDzType;	  
	int 				iParamNum;   
	char				cDzParam[MAX_DZ_EX_PARAM_NUM][LEN_128];   
}DZCommonEx,*pDZCommonEx;

typedef struct tagPuCommonInfoEx
{
	int					iSize;
	char    			cDeviceID[LEN_64];
	char    			cDeviceName[LEN_64];
	unsigned short		usVspPort;
	char    			cPassword[LEN_64];
	unsigned short  	usVapPort;
}PuCommonInfoEx,*pPuCommonInfoEx;

#define LDC_TYPE_LEVEL_VIEW									1		//Level View
#define LDC_TYPE_VERTICAL_VIEW								2		//Vertical View
#define LDC_TYPE_DISTORTION_CORRECTION_STRENGTH				3		//Distortion Correction Strength
#define LDC_TYPE_DISTAL_MAGNIFICATION_STRENGTH				4		//Distal Magnification Strength
#define MAX_LDC_TYPE_NUM									5
typedef struct tagVideoLdcExInfo
{
	int					iSize;
	int					iModelIndex;
	int					iLdcType;			//0-Level View 1-Vertical View 2-Distortion Correction Strength 3-Distal Magnification Strength
	int					iValue;				//0~100
}VideoLdcExInfo,*pVideoLdcExInfo;

typedef struct tagVideoLdcExModelIndex
{
	int					iSize;
	int					iModelIndex;
}VideoLdcExModelIndex,*pVideoLdcExModelIndex;

#define MAX_LDC_MODEL_NUM		17			//max ldc model num， 0x7FFFFFFF is a kind of ldc model
typedef struct tagVideoLdcExModelInfo
{
	int					iSize;
	int					iModelIndex;		
	int					iModelType;			//0-template 1-manual
	int					iLdcType;
	int					iValue;
}VideoLdcExModelInfo,*pVideoLdcExModelInfo;

#define MAX_SINGLE_COM_PERIPHERAL_NUM			255
#define SPECIAL_MEAN_FOR_PERIPHERAL_NUM			(0x7FFFFFFF)
typedef struct tagPeripheralInfo
{
	int					iSize;
	int					iComNum;
	int					iPeripheralType;
	int					iPeripheralNum;
	int					iPeripheralAddr[MAX_SINGLE_COM_PERIPHERAL_NUM];
	int					iEnable;
	int					iEnableEx[MAX_SINGLE_COM_PERIPHERAL_NUM];
}PeripheralInfo,*pPeripheralInfo;

typedef struct tagPeripheralList
{
	int					iSize;
	int					iSupportType;
}PeripheralList,*pPeripheralList;

#define MAX_CALIBRATE_SCENE_AREA_NUM	4		//fish link ball : fish area num
#define MAX_SINGLE_CHAN_LINK_DEV_NUM	2		//fish/camera link ball : max link ball num per channel

#define CALIBRATEMODE_CALIBRATE_YES   0
#define CALIBRATEMODE_CALIBRATE_NO    1
#define CALIBRATEMODE_CALIBRATEING    2

typedef struct tagCalibrateMode
{
	int					iSize;
	int					iSceneAreaNo;			//fish link ball : fish area no 0~3
	int					iLinkDevNo;				//fish/camera link ball :  link ball no 0~1
	int					iCalibrateMode;			//1-auto 1-manual
	int					iPan;
	int					iTilt;
	int					iZoom;
    int                 iState;                 //0：未标定，1：标定中，2：标定成功，3：标定失败
    int                 iSceneType;             //0：智能分析场景，1：警戒场景
}CalibrateMode,*pCalibrateMode;

#define MAX_PREFERRED_AREA_NUM			4		//area first: max preferred area num
typedef struct tagVcaAreaFirst
{
	int					iSize;
	int					iPreferredAreaNo;		//preferred area no 0~3
	int					iLinkDevNo;				//fish/camera link ball :  link ball no 0~1
	int					iAreaEnable;
	vca_TPolygonEx		tAreaInfo;
}VcaAreaFirst,*pVcaAreaFirst;

typedef struct tagPPPOEStatus
{
	int					iSize;
	int					iStatus;
	char				cIp[LEN_16];
	char 				cMask [LEN_16];
	char 				cGateway [LEN_16];
	char 				cDNS [LEN_16];	
}PPPOEStatus,*pPPPOEStatus;

typedef struct tagVcaBindOpreate
{
	int					iSize;
	int					iLinkDevNo;				//fish/camera link ball :  link ball no 0~1
	int					iOperate;				//1-bind 2-bind off 3-bind mandatory
	char				cDevIp[LEN_64];
	char				cMac[LENGTH_MAC_ADDR];
	int					iBindAck;				//1-suc 2-fail 3-err bind occupy no
}VcaBindOpreate,*pVcaBindOpreate;

typedef struct tagVcaBindInfo
{
	int					iSize;
	int					iLinkDevNo;				//fish/camera link ball :  link ball no 0~1
	char				cDevIp[LEN_64];
	char				cMac[LENGTH_MAC_ADDR];
}VcaBindInfo,*pVcaBindInfo;

typedef struct tagVcaCameraType
{
	int					iSize;
	int					iCameraType;			//0-fish 1-2.8mm 2-4.0mm 3-4.7mm 4-6.0mm
}VcaCameraType,*pVcaCameraType;

typedef struct tagCloudDetect
{
	int					iSize;
	int					iNewVerStat;			//0-no new ver 1-has new ver
	char				cVersion[LEN_64];
	char				cReleaseDate[LEN_32];
	int					iErrorCode;				//0-reserved，1-Cloud platform server unconnected 2-An unsupported protocol
	int					iChanNo;
}CloudDetect,*pCloudDetect;

typedef struct tagCloudUpgrade
{
	int					iSize;
	int					iUpgradeStat;
	int					iChanNo;
}CloudUpgrade,*pCloudUpgrade;

typedef struct tagCloudDownload	
{
	int					iSize;
	int					iDownloadStat;			//0-downloading 1-success 2-failed
	int					iProcess;
	int					iChanNo;
}CloudDownload,*pCloudDownload;

typedef struct tagCloudUpgradeProcess
{
	int					iSize;
	int					iUpgradeStat;			//0-downloading 1-success 2-failed
	int					iProcess;
	int					iChanNo;
}CloudUpgradeProcess,*pCloudUpgradeProcess;

typedef struct tagLogonFindPsw
{
	int					iSize;
	char				cNvsIP[LEN_32];			
	int					iNvsPort;				
}LogonFindPsw,*pLogonFindPsw;

#define USER_FIND_PWD_MODE_DEFAULT		0
#define USER_FIND_PWD_MODE_PHONE		1
#define USER_FIND_PWD_MODE_MAIL			2
#define USER_FIND_PWD_MODE_APP			3
#define MAX_USER_FIND_PWD_MODE_NUM		4
typedef struct tagUserFindPwd
{
	int					iSize;
	int					iFindMode;					//IN	 1-phone 2mail		
	char				cModeInfo[LEN_128];			//OUT phone num or mail addr
	char				cLinkAddress[LEN_128];		//OUT 
}UserFindPwd,*pUserFindPwd;

typedef struct tagUserSecurityCode
{
	int					iSize;
	char				cSecurityCode[LEN_16];		
	int					iFindRet;
	char				cUserName[LEN_16];		
	char				cUserPwd[LEN_16];		
}UserSecurityCode,*pUserSecurityCode;

typedef struct tagUserReservePhone
{
	int					iSize;
	int					iFindMode;					//IN 1-phone 2mail	
	int					iSynFlag;					//OUT 1-Need to synchronize data 0-no need synchronize data
	char				cModeInfo[LEN_128];			//OUT phone num or mail addr
}UserReservePhone,*pUserReservePhone;

#define MAX_DOCK_PLAT_UPLOAD_TYPE_NUM		11
#define MAX_DOCK_PLAT_UPLOAD_PARAM_NUM		15
typedef struct tagITSDockPlatUploadType	
{
	int					iSize;
	int					iType;	  
	int 				iParamNum;   
	char				cParam[MAX_DOCK_PLAT_UPLOAD_PARAM_NUM][LEN_64];   
}ITS_DockPlatUploadType,*pITS_DockPlatUploadType;

//unique alert type
#define UNIQUE_ALERT_TYPE_MIN						0
#define UNIQUE_ALERT_TYPE_PERIMETER					(UNIQUE_ALERT_TYPE_MIN)
#define UNIQUE_ALERT_TYPE_TRIPWIRE					(UNIQUE_ALERT_TYPE_MIN + 1)
#define UNIQUE_ALERT_TYPE_CLIMBWALL					(UNIQUE_ALERT_TYPE_MIN + 2)
#define UNIQUE_ALERT_TYPE_MAX						(UNIQUE_ALERT_TYPE_MIN + 3)

#define MAX_UNIQUE_ALERT_TYPE_EVENT_NUM				1

#define SCENE_TYPE_VCA								0
#define SCENE_TYPE_UNIQUE_ALERT						1

#define MAX_UNIQUE_ALERT_SCENE_NUM					4				

#define UNIQUE_ALERT_CMD_MIN						0
#define UNIQUE_ALERT_CMD_LIST						(UNIQUE_ALERT_CMD_MIN + 0)
#define UNIQUE_ALERT_CMD_TEMPLATE					(UNIQUE_ALERT_CMD_MIN + 1)
#define UNIQUE_ALERT_CMD_CFGCHN						(UNIQUE_ALERT_CMD_MIN + 2)
#define UNIQUE_ALERT_CMD_EVENTSET					(UNIQUE_ALERT_CMD_MIN + 3)
#define UNIQUE_ALERT_CMD_SCENE_TIMESEG				(UNIQUE_ALERT_CMD_MIN + 4)
#define UNIQUE_ALERT_CMD_DRAW_LINE					(UNIQUE_ALERT_CMD_MIN + 5)
#define UNIQUE_ALERT_CMD_LINK_MODE					(UNIQUE_ALERT_CMD_MIN + 6)
#define UNIQUE_ALERT_CMD_WHITE_LIGHT_MODE			(UNIQUE_ALERT_CMD_MIN + 7)
#define UNIQUE_ALERT_CMD_WHITE_LIGHT_PARA			(UNIQUE_ALERT_CMD_MIN + 8)
#define UNIQUE_ALERT_CMD_PERIMETER					(UNIQUE_ALERT_CMD_MIN + 9)
#define UNIQUE_ALERT_CMD_TRIPWIRE					(UNIQUE_ALERT_CMD_MIN + 10)
#define UNIQUE_ALERT_CMD_ALARM_LINK					(UNIQUE_ALERT_CMD_MIN + 11)
#define UNIQUE_ALERT_CMD_ALARM_SCHEDULE				(UNIQUE_ALERT_CMD_MIN + 12)
#define UNIQUE_ALERT_CMD_ALARM_MESSAGE				(UNIQUE_ALERT_CMD_MIN + 13)
#define UNIQUE_ALERT_CMD_CALL_ANYSCENE				(UNIQUE_ALERT_CMD_MIN + 15)
#define UNIQUE_ALERT_CMD_CFG_TARGET					(UNIQUE_ALERT_CMD_MIN + 16)
#define UNIQUE_ALERT_CMD_CFG_ADV					(UNIQUE_ALERT_CMD_MIN + 17)
#define UNIQUE_ALERT_CMD_SCENE_REV					(UNIQUE_ALERT_CMD_MIN + 18)
#define UNIQUE_ALERT_CMD_ALARM_STAT_CLR				(UNIQUE_ALERT_CMD_MIN + 19)
#define UNIQUE_ALERT_CMD_CLIMBWALLPARA				(UNIQUE_ALERT_CMD_MIN + 20)
#define UNIQUE_ALERT_CMD_MAX						(UNIQUE_ALERT_CMD_MIN + 21)

typedef struct tagUniqueAlertList
{
	int					iSize;
	int					iAlertType;	 
	int 				iAlertParam;   
}UniqueAlertList,*pUniqueAlertList;

#define MAX_UNIQUE_ALERT_SUPPORT_NAME_NUM	5
typedef struct tagUniqueAlertTemplate
{
	int					iSize;
	int					iSceneId;
	int					iAlertType;	 
	int 				iSupprotNames[MAX_UNIQUE_ALERT_SUPPORT_NAME_NUM];   
}UniqueAlertTemplate,*pUniqueAlertTemplate;

typedef struct tagUniqueAlertCfgChn
{
	int					iSize;
	int					iEnable;
	int					iEnChnType;			//Enable channel type	 
}UniqueAlertCfgChn,*pUniqueAlertCfgChn;

typedef struct tagUniqueAlertEventSet
{
	int					iSize;
	int					iSceneId;
	int					iTypeEnable[LEN_32];
}UniqueAlertEventSet,*pUniqueAlertEventSet;

typedef struct tagUniqueAlertSceneTimeSeg
{
	int					iSize;
	int					iSceneId;
	int					iEnable;
	int					iStartHour;					
	int					iStartMinute;
	int					iStopHour;
	int					iStopMinute;
}UniqueAlertSceneTimeSeg,*pUniqueAlertSceneTimeSeg;

#define MAX_UNIQUE_ALERT_DRAW_AREA_NUM		1
typedef struct tagUniqueAlertDrawLine
{
	int					iSize;
	int					iSceneId;
	int					iAlertType;
	int					iEventNo;					
	int					iAreaNum;
	vca_TPolygonEx		tAreaInfo[MAX_UNIQUE_ALERT_DRAW_AREA_NUM];
}UniqueAlertDrawLine,*pUniqueAlertDrawLine;

typedef struct tagUniqueAlertLinkMode
{
	int					iSize;
	int					iSceneId;
	int					iAlertType;
	int					iEventNo;					
	int					iLinkMode;
}UniqueAlertLinkMode,*pUniqueAlertLinkMode;

#define MAX_UNIQUE_ALERT_WHITE_LIGHT_SUPPORT_GRADE			4
typedef struct tagUniqueAlertWhiteLightMode
{
	int					iSize;
	int					iAlertType;
	int					iSupportGrade;
}UniqueAlertWhiteLightMode,*pUniqueAlertWhiteLightMode;

typedef struct tagUniqueAlertWhiteLightPara
{
	int					iSize;
	int					iAlertType;
	int					iGrade;
	int					iDefaultMode;
	int					iFlashNum;
	int					iFicker;
	int					iAlways;
	int					iDelayTime;
}UniqueAlertWhiteLightPara,*pUniqueAlertWhiteLightPara;

typedef struct tagUniqueAlertPerimeter
{
	int					iSize;
	int					iSceneId;
	int					iEventNo;	
	vca_TDisplayParam	tDisplayInfo;
	int					iSensitivity;
	int					iValid;
	int					iDisplayTarget;
	int					iTargetTypeCheck;
	int					iCheckMode;
	int					iMinDistance;
	int					iMinTime;
	int					iDirectionCheck;
	int					iDirectionAngle;
	int					iMiniSize;
	int					iMaxSize;
	int					iResortTime;
	int					iTrackTime;
	int                 iNoAlarmMode;
}UniqueAlertPerimeter,*pUniqueAlertPerimeter;

typedef struct tagUniqueAlertTripwire
{
	int					iSize;
	int					iSceneId;
	int					iEventNo;	
	vca_TDisplayParam	tDisplayInfo;
	int					iSensitivity;
	int					iValid;
	int					iDisplayTarget;
	int					iTargetTypeCheck;
	int					iMinDistance;
	int					iMinTime;
	int					iTripwireType;
	int					iTripwireDirection;
	int					iTrackTime;
}UniqueAlertTripwire,*pUniqueAlertTripwire;

#define MAX_UNIQUE_ALERT_ALARM_LINK_LEVEL	4
typedef struct tagUniqueAlertAlarmLink
{
	int					iSize;
	int					iSceneId;
	int					iAlertType;
	int					iEventNo;
	int					iLinkLevel;
	int					iNextLevel;
	int					iRetentionTime;
	int					iLinkType;
	ULinkParam_V3		uLinkParam;	
}UniqueAlertAlarmLink,*pUniqueAlertAlarmLink;

#define MAX_WEEK_DAYS						7
typedef struct tagUniqueAlertAlarmSchedule
{
	int					iSize;
	int					iSceneId;
	int					iEnChnType;			//Enable channel type	 
	int					iAlertType;
	int					iEventNo;
	int					iWeekEnabel[MAX_WEEK_DAYS];
}UniqueAlertAlarmSchedule,*pUniqueAlertAlarmSchedule;

typedef struct tagUniqueAlertAlarmMessage
{
	int					iSize;
	int					iMsgIndex;
	int					iChanNo;
	int					iAlarmState;
	int					iTargetId;							
	int					iAlertType;
	int					iCheckType;
	int					iEventNo;
	int					iTargetType;
	RECT				tTargetPosition;
	NVS_FILE_TIME_V1	tAlarmTime;
	int					iWeekDay;
	char				cAlarmTime[LEN_32];
	int					iLevel; //警戒级别	0：保留；1：一级警戒；2：二级警戒；3：三级警戒
}UniqueAlertAlarmMessage,*pUniqueAlertAlarmMessage;

typedef struct tagDnsEnable	
{
	int					iSize;
	int					iLanNo;	  
	int 				iEnable;   
}DnsEnable,*pDnsEnable;

typedef struct tagNewVersion	
{
	int					iSize;	
	char 				cVersion[LEN_64];   
}NewVersion,*pNewVersion;

#define	MAX_VO_DEV_COUNT		16
#define	MAX_RESOLUTION_NUM		32

typedef struct tagDevResolution	
{
	int					iSize;	  
	int 				iVoDevID;
	int					iAutoCheck;
	char				cResolution[LEN_32];   
}DevResolution,*pDevResolution;

typedef struct tagDevResolutionList	
{
	int					iSize;
	int					iVoDevCnt;	  
	int 				iVoDevID;
	char				cVoDevName[LEN_32];
	int					iResolutionNum;
	char				cResolution[MAX_RESOLUTION_NUM][LEN_32];   
}DevResolutionList,*pDevResolutionList;

typedef struct tagIpcATTR	
{
	int					iSize;
	int					iWebType;				//web type: 0-web6.0, 1-web5.0		  
	char 				cDzVerSion[LEN_128];	//dz version
	int					iLanguageNum;
	int					iLanguage[LANGUAGE_COUNT];  
}IpcATTR, *pIpcATTR;

#define CMRINFO_HORIZONTAL_VIEW			0	//Horizontal field of view
#define CMRINFO_VERTICAL_VIEW			1	//Vertical field of view
#define CMRINFO_MAGNIFICATION_TABLE		2	//Magnification table
#define MAX_CMRINFO_DATA_TYPE			3
#define MAX_CMRINFO_DATACOUNT	1024
typedef struct tagCameraInfo	
{
	int					iSize;
	int					iDataType;		//data type
	int					iDataCount;		//data count
	int					iDataArr[MAX_CMRINFO_DATACOUNT];		//data array
} CameraInfo, *pCameraInfo;

typedef struct tagCurrentScene
{
	int					iSceneId;
	int					iSceneType;
}CurrentScene, *pCurrentScene;

#define MAX_VCA_SLUICEGATE_COUNT	5
#define MAX_SLUICEGATE_POINT_COUNT	8
#define TYPE_INVISIBLE_SLUICEGATE	0
#define TYPE_VISIBLE_SLUICEGATE		1
typedef struct tagSluiceGatePara
{	
	int					iSize;
	int					iSluiceGateState;
	int					iPointCount;
	vca_TPoint			tPointArr[MAX_SLUICEGATE_POINT_COUNT];										
} SluiceGatePara, *pSluiceGatePara;
typedef struct tagVcaSluiceGate
{
	int					iSize;
	VCARule				tRule;
	vca_TDisplayParam	tDisplayParam;
	int					iSluiceGateType;	//0:Invisible gate 1:Visible gate					
	int					iSluiceGateCount;
	SluiceGatePara	tSluiceGateArr[MAX_VCA_SLUICEGATE_COUNT];
} VcaSluiceGate, *pVcaSluiceGate;

typedef struct tagPlatformGauge
{
	int					iSize;
	int					iSceneID;
} PlatformGauge, *pPlatformGauge;

#define  MAX_ELE_ANTI_SHAKE_STATUS_NUM			3
typedef struct tagEleAntiShake
{
	int					iSize;
	int					iStatus;		//0-close 1-open 2-close
	int					iValue;
} EleAntiShake, *pEleAntiShake;


typedef struct tagUpgradeV5
{
	int					iSize;
	char				cFilePath[LEN_256];	
	UPGRADE_NOTIFY_V4	cbUpgradeNotify;
	void*				pUser;
	int					iType;					
	char				cParam[LEN_64];					//iType=l, libkey
} UpgradeV5, *pUpgradeV5;

#define ALMINPORT_MODE_NORMALLY_OPEN	0	//normally open
#define ALMINPORT_MODE_NORMALLY_CLOSE	1	//normally close
#define ALMINPORT_TYPE_NORMAL			0	//normal port alarm
#define ALMINPORT_TYPE_BUTTON			1	//button alarm
#define ALMINPORT_TYPE_TIPPINGRAINGAUGE 2    
typedef struct tagFaceAlarmNotify
{
	int					iSize;
	int					iChanNo;
	int					iState;
	int					iType;
	int					iFaceKey;
	int					iTrackId;
	PicTime				tCapTime;
	char                cAlarmTime[LEN_32];
}FaceAlarmNotify,*pFaceAlarmNotify;



#define SYNC_NET_CLIENT_MIN								0	
#define SYNC_NET_CLIENT_VCA_SUSPEND				(SYNC_NET_CLIENT_MIN + 0)
#define SYNC_NET_CLIENT_MAX						(SYNC_NET_CLIENT_MIN + 1)	


/************************************************************************/
/*                                                                      
1000-Single Pic
2000~2003-Two Pic Big
2100~2103-Two Pic Midle
2200~2203-Two Pic Small
2300-Two Pic
3000~3005-Three Pic Big
3100~3105-Three Pic Midle
3200~3205-Three Pic Small
3300-Three Pic A
3301-Three Pic B
4000-Four Pic A
4001~4004-Four Pic Big
4100~4103-Four Pic Midle
4200~4203-Four Pic Small
4300-Four Pic A
4301-Four Pic B
5300-Five Pic
6-Six Pic
7-Seven Pic
8-eight Pic
9-nine pic

iVoId
#define VGA_CHANNEL_PIC		1
#define HDMI1_CHANNEL_PIC	17
#define CVBS_CHANNEL_PIC	33
#define VC_CHANNEL_PIC		49

cChannelSet
ChnNo0:ChnNo1：…ChnNon
*/
/************************************************************************/

#define CONNECT_INFO_STATE	0

#define CONNECT_STATUS_CONNECTED		1 
#define CONNECT_STATUS_DISCONNECT		0	

typedef struct _ConnectState
{
	int iSize;
	int iState;
}ConnectState;

/***********************************NetClient_SetSDKInitConfig Cmd*******************************/
#define INIT_CONFIG_MIN										   0	
#define INIT_CONFIG_LOCAL_LIBRARY_PATH				(INIT_CONFIG_MIN + 0)	//Local library load path
#define INIT_CONFIG_CLIENT_CHARSET					(INIT_CONFIG_MIN + 1)	//client charset
#define INIT_CONFIG_MAX_PROCESS_PICNUM_ONESECOND	(INIT_CONFIG_MIN + 2)	//max process pic number in one second
#define INIT_CONFIG_SET_LOG_LEVEL					(INIT_CONFIG_MIN + 3)	//set sdk log level
#define INIT_CONFIG_ENABLE_CALC_MSABSTIME			(INIT_CONFIG_MIN + 4)	
#define INIT_CONFIG_MAX								(INIT_CONFIG_MIN + 5)
#define INIT_CONFIG_SAVE_ALARMLINK					(INIT_CONFIG_MIN + 6)	//save alarmlink onlocal


#define LIBRARY_TYPE_MIN									0
#define LIBRARY_TYPE_LIBOSSDK				(LIBRARY_TYPE_MIN + 0)	//libossdk.so path
#define LIBRARY_TYPE_MAX					(LIBRARY_TYPE_MIN + 1)

#ifndef CHARSET_TYPE_UTF8
#define CHARSET_TYPE_UTF8					1
#endif
#ifndef CHARSET_TYPE_GB2312
#define CHARSET_TYPE_GB2312					2
#endif

typedef struct	tagLocalSDKPath
{
	int		iSize;
	int		iType;				//library type:iType=LIBRARY_TYPE_LIBOSSDK
	char	cPath[MAX_PATH];	//library path
}LocalSDKPath,*pLocalSDKPath;

#define SDK_LOG_LEVEL_FORBID		0
#define	SDK_LOG_LEVEL_ERROR			100
#define SDK_LOG_LEVEL_MSG			200
#define SDK_LOG_LEVEL_DEBUG			300
//接口控制日志文件和日志级别优先级高于检测配置文件
typedef struct tagSdkLogLevel
{
	int		iTerminalOutputLevel;		//终端日志的输出等级：0-不输出任何日志，100-错误日志，200-信息日志，300-调试日志
	int		iIsWriteFile;				//0-不写日志文件，1-写日志文件
	int		iLogFileWriteLevel;			//文件日志的输出等级：0-不输出任何日志，100-错误日志，200-信息日志，300-调试日志
} SdkLogLevel, *pSdkLogLevel;


typedef struct tagRemote3DLocate
{	
	int iStreamNo;			//码流类型：0--主码流，1--副码流
	RECT t3DLocateArea;		//3D定位区域，以Windows MFC Api为例，需要调用系统函数GetClientRect转换成客户区域
	RECT tVideoRenderArea;	//视频渲染区域，以Windows MFC Api为例，需要调用系统函数GetClientRect转换成客户区域
} Remote3DLocate, *pRemote3DLocate;

//Transparent Channel Control
#define TCC_MOVE_UP							1       //ptz up
#define TCC_MOVE_UP_STOP					2       //stop ptz up
#define TCC_MOVE_DOWN						3       //ptz down
#define TCC_MOVE_DOWN_STOP					4       //stop ptz down
#define TCC_MOVE_LEFT						5       //ptz left
#define TCC_MOVE_LEFT_STOP					6       //stop ptz left
#define TCC_MOVE_RIGHT						7       //ptz right
#define TCC_MOVE_RIGHT_STOP					8       //stop ptz right
#define TCC_MOVE_UP_LEFT					9       //ptz top left
#define TCC_MOVE_UP_LEFT_STOP				10      //stop ptz top left
#define TCC_MOVE_UP_RIGHT					11      //ptz top right
#define TCC_MOVE_UP_RIGHT_STOP				12      //stop ptz top right
#define TCC_MOVE_DOWN_LEFT					13      //ptz down left
#define TCC_MOVE_DOWN_LEFT_STOP				14      //stop ptz down left
#define TCC_MOVE_DOWN_RIGHT					15      //ptz down right
#define TCC_MOVE_DOWN_RIGHT_STOP			16      //stop ptz down right
#define TCC_HOR_AUTO						21      //horizontal auto        
#define TCC_HOR_AUTO_STOP					22		//stop horizontal auto
#define TCC_ZOOM_BIG						31      //zoom big
#define TCC_ZOOM_BIG_STOP					32      //stop zoom big
#define TCC_ZOOM_SMALL						33      //zoom small
#define TCC_ZOOM_SMALL_STOP					34      //stop zoom small
#define TCC_FOCUS_FAR						35      //focus far
#define TCC_FOCUS_FAR_STOP					36      //stop focus far
#define TCC_FOCUS_NEAR						37      //focus near
#define TCC_FOCUS_NEAR_STOP					38      //stop focus near
#define TCC_IRIS_OPEN						39      //open iris 
#define TCC_IRIS_OPEN_STOP					40      //stop open iris
#define TCC_IRIS_CLOSE						41      //close iris
#define TCC_IRIS_CLOSE_STOP					42      //stop close iris
#define TCC_LIGHT_ON						43      //light on
#define TCC_LIGHT_OFF						44      //light off
#define TCC_POWER_ON						45      //power on
#define TCC_POWER_OFF						46      //power off
#define TCC_RAIN_ON							47      //rain on
#define TCC_RAIN_OFF						48      //rain off
#define TCC_CALL_VIEW						62      //call camera
#define TCC_SET_VIEW						63      //set preset
#define TCC_DELETE_VIEW						64      //delete preset
//Control Protocol Type
#define CPT_PTZ_PELCO_D		0
#define CPT_PTZ_PELCO_P		1
#define CPT_PTZ_TC615_P		2
#define CPT_DOME_PELCO_D	3
#define CPT_DOME_PELCO_P	4
#define CPT_DOME_PLUS		5
typedef struct tagTransparentChannelControl
{
	int iControlCode;			//控制动作码: TCC_MOVE_UP ~ TCC_DELETE_VIEW
	int iSpeed;					//球机云台转动速度，控制球机云台转动时使用，其余功能该参数无效
	int iPresetNo;				//预置位编号，设置和调用预置位时使用，其余功能该参数无效
} TransparentChannelControl, *pTransparentChannelControl;

#define NETCLIENT_CMD_MIN_V5								0
#define NETCLIENT_GET_HARDDISKINFO_V5			NETCLIENT_CMD_MIN_V5 + 0	//获取设备磁盘信息
#define NETCLIENT_GET_ALARMINPORT_V5			NETCLIENT_CMD_MIN_V5 + 1
#define NETCLIENT_GET_ALARMOUTPORT_V5			NETCLIENT_CMD_MIN_V5 + 2
#define NETCLIENT_CMD_MAX_V5					NETCLIENT_CMD_MIN_V5 + 3	

#define MAX_HARDDISK_COUNT			256
//硬盘类型：0-本地SATA硬盘，1-ESATA硬盘，2-SD卡，3-USB，4-NFS硬盘，5-RAID虚拟磁盘，6-IPSAN 
#define HARDDISK_TYPE_LOCALSATA				0
#define HARDDISK_TYPE_ESATA					1
#define HARDDISK_TYPE_SD					2		
#define HARDDISK_TYPE_USB					3
#define HARDDISK_TYPE_NFS					4
#define HARDDISK_TYPE_RAIDVD				5
#define HARDDISK_TYPE_IPSAN 				6
//磁盘状态：0，无磁盘；1，未格式化；2，已格式化；3，已挂载；4，读写中
#define HARDDISK_STATUS_NODISK				0
#define HARDDISK_STATUS_UNFORMAT			1
#define HARDDISK_STATUS_FORMATTED			2
#define HARDDISK_STATUS_MOUNTED				3
#define HARDDISK_STATUS_READANDWRITE		4
//磁盘用途：0，录像；1，备份；2，冗余；3，磁盘只读
#define HARDDISK_USAGE_RECORDING			0
#define HARDDISK_USAGE_BACKUP				1
#define HARDDISK_USAGE_REDUNDANCE			2
#define HARDDISK_USAGE_ONLYREAD				3		
typedef struct tagSingleHardDisk
{
	int				iDiskType;			//硬盘类型：HARDDISK_TYPE_LOCALSATA ~ HARDDISK_TYPE_IPSAN
	int				iDiskNumber;		//硬盘编号
	unsigned int	uiTotalSpace; 		//Total size
	unsigned int	uiFreeSpace;  		//free space
	unsigned short  usPartCount;		//number of partitions 1 to 4
	unsigned short  usStatus;     		//state 0, no disk; 1, unformatted; 2, formatted; 3, mounted; 4, read and write
	unsigned short  usUsage;      		//use 0, video; 1, backup
	int				iExpCabinetNo;		// -1:不属于任何扩展柜, 大于等于0:扩展柜编号
	int				iResPara1;			//保留参数1，置为0 
	int				iResPara2;			//保留参数2，置为0
	int				iResPara3;			//保留参数3，置为0
	int				iResPara4;			//保留参数4，置为0
	char			cResPara5[LEN_64];	//保留参数5，置为0
	char			cResPara6[LEN_256];	//保留参数6，置为0
} SingleHardDisk, *pSingleHardDisk;

typedef struct tagHardDiskInfo
{
	int				iTotalDiskCount;
	int				iExpCabinetCount;	//扩展柜个数
	int				iRaidEnable;
	SingleHardDisk	tHardDiskArray[MAX_HARDDISK_COUNT];
} HardDiskInfo, *pHardDiskInfo;


#define ALARM_PORT_TYPE_REMOTEIPC		0	//前端相机报警端口
#define ALARM_PORT_TYPE_NVRLOCAL		1	//nvr本地报警端口
#define ALARM_PORT_TYPE_TRADEHOST		2	//行业主机报警端口
typedef struct tagSingleAlarmPort
{
	int		iPortType;				//端口类型
	int		iPortNo;				//端口编号
	char	cAliasName[LEN_128];	//端口别名
	int		iOnOrOff;				//常开常闭
	int		iOnline;				//在线状态：0--不在线，1--在线
} SingleAlarmPort, *pSingleAlarmPort;
typedef struct  tagAlarmPortInfo
{
	int					iAlarmPortCount;
	SingleAlarmPort		tAlarmPortArray[MAX_ALARM_PORT_COUNT];
} AlarmInPortInfo, *pAlarmInPortInfo, AlarmOutPortInfo, *pAlarmOutPortInfo;

typedef struct tagNetVoiceTalkPara
{
	int					iLogonId;		//device logon id
	int					iChannel;		//0x7FFFFFFF:talk with device，Other:channel talk
	int					iDataType;		//0:collection by SDK，1:input by user
	NVSDATA_NOTIFY		cbkDataArrive;
	void*				pvUserData;
} NetVoiceTalkPara, *pNetVoiceTalkPara;

#define AUDIODATA_FLAG_PCM		0	//PCM裸流
#define AUDIODATA_FLAG_ENCODE	1	//编码后的音频数据（不包含海思头）
typedef struct tagNetVoiceTalkInput
{
	int					iLogonId;		//device logon id
	int					iChannel;		//0x7FFFFFFF:talk with device，Other:channel talk
	unsigned char*		pucData;
	unsigned int		uiDataLen;
	int					iEncodeFlag;	//0-PCM裸流，1-已编码的音频数据（调用者自己确保编码格式与设备一致）
} NetVoiceTalkInput, *pNetVoiceTalkInput;


typedef struct tagTargetPoint
{
	int x;   //单位mm   有负值
	int y;   //单位mm   有负值
}TargetPoint,*pTargetPoint;

typedef struct tarRadarTargetInfo
{
	unsigned int     iTargetID;		 //轨迹ID
	TargetPoint		 tPoint;		 //轨迹目标坐标
	char			 cReserve[8];	 //预留
}RadarTargetInfo,*pRadarTargetInfo;

#define RADAR_TARGET_NUM_MAX	128
typedef struct tarRadarTargetList
{
	int				 iTargetTotal;		//轨迹目标总数
	RadarTargetInfo	 tTargetInfo[RADAR_TARGET_NUM_MAX];	//最多128个目标
	char			 cReserve[8];        //预留
}RadarTargetList,*pRadarTargetList;


#define XMLCFG_METHOD_GET			0
#define XMLCFG_METHOD_PUT			1
#define XMLCFG_METHOD_POST			2
#define XMLCFG_METHOD_DELETE		3

#define XML_DEFAULT_RECV_TIMEOUT	10 * 1000	//the default timeout is 10s 
typedef struct tagXmlCfgInPara
{
	int			iMethod;				//ISAPI/CGI Protocol method: 0--GET, 1--PUT, 2--POST, 3--DELETE
	char*		pcRequestUrl;			//输入参数：请求信令，字符串格式， 例如获取通道码流参数：/CGI/Streaming/channels/1/type/1
	//Input parameters: request signaling, string format, for example, obtain channel code stream parameters: /CGI/Streaming/channels/1/type/1
	int			iRequestUrlLen;			//输入参数：请求信令长度，字符串长度 Input parameters: request signaling length, string length 
	void*		pvInBuf;				//输入参数：输入参数缓冲区，Xml格式 Input parameters: input parameter buffer, XML format
	int			iInBufSize;				//输入参数：输入参数缓冲区大小 Input parameters: input parameter buffer size 
	int			iRecvTimeOut;			//输入参数：接收超时时间，单位：ms，填0则使用默认超时10s Input parameter: receiving timeout, unit: ms, if 0 is filled, the default timeout is 10s 
	int			iForceEncrpt;			//保留参数，暂不使用 Parameters reserved, not used temporarily 
	int			iNetMode;				//输入参数：iNetMode=0：HTTP模式，iNetMode=1：TCP模式（Super private protocol）
} XmlCfgInPara, *pXmlCfgInPara;

typedef struct tagXmlCfgOutPara 
{
	void*		pvOutputBuf;			//输出参数：输出参数缓冲区，Xml格式，应用层需要事先分配足够大的内存 
	//Output parameters: output parameter buffer, XML format.The application layer needs to allocate enough memory in advance 
	int			iOutBufSize;			//输入参数：输出参数缓冲区大小(内存大小) Input parameters: output parameter buffer size (memory size) 
	int			iReturnXmlSize;			//输出参数：实际输出的Xml内容大小 Output parameters: actual output XML content size 
	void*		pvStatusBuf;			//输出参数：返回的状态参数(Xml格式：ResponseStatus)，获取命令成功时不会赋值，如果不需要，可以置NULL
	//Output parameter: the returned status parameter (XML format: responsestatus). When the command is obtained successfully, it will not be assigned. If it is not needed, it can be set to null
	int			iStatusBufSize;			//输入参数：状态缓冲区大小(内存大小) Input parameter: status buffer size (memory size) 
} XmlCfgOutPara, *pXmlCfgOutPara;


//NetClient_XmlSetDevConfig & NetClient_XmlGetDevConfig Interface parameters '_iCmd' define
#define NETXMLCFG_CMD_MIN						0
#define NETXMLCFG_REGISTER_CENTER		(NETXMLCFG_CMD_MIN + 0)
#define NETXMLCFG_DEVICE_SYSTIME		(NETXMLCFG_CMD_MIN + 1)
#define NETXMLCFG_NTP_SERVER			(NETXMLCFG_CMD_MIN + 2)
#define NETXMLCFG_REGION_CLIP			(NETXMLCFG_CMD_MIN + 3)
#define NETXMLCFG_EXPOURSESYNC			(NETXMLCFG_CMD_MIN + 4)
#define NETXMLCFG_HNRENABLE				(NETXMLCFG_CMD_MIN + 5)
#define NETXMLCFG_SPLICLING_PARAM		(NETXMLCFG_CMD_MIN + 6)
#define NETXMLCFG_PERSONNEL_PARAM		(NETXMLCFG_CMD_MIN + 7)
#define NETXMLCFG_3DCALIBRATE_PARAM		(NETXMLCFG_CMD_MIN + 8)
#define NETXMLCFG_ANGELVIEW_PARAM		(NETXMLCFG_CMD_MIN + 9)
#define NETXMLCFG_CMD_10				(NETXMLCFG_CMD_MIN + 10)
#define NETXMLCFG_BESTIR_PARAM			(NETXMLCFG_CMD_MIN + 11)
#define NETXMLCFG_PREVIEWMODE_PARAM		(NETXMLCFG_CMD_MIN + 12)
#define NETXMLCFG_BESTIRCHN_PARAM		(NETXMLCFG_CMD_MIN + 13)
#define NETXMLCFG_VGASWITCHINFO			(NETXMLCFG_CMD_MIN + 14)
#define NETXMLCFG_REGIONINFO			(NETXMLCFG_CMD_MIN + 15)
#define NETXMLCFG_JUDGEANYINFO			(NETXMLCFG_CMD_MIN + 16)
#define NETXMLCFG_JUDGECASECONFIG		(NETXMLCFG_CMD_MIN + 17)
#define NETXMLCFG_COMMONPROGRESS		(NETXMLCFG_CMD_MIN + 18)
#define NETXMLCFG_SESSIONID				(NETXMLCFG_CMD_MIN + 19)
#define NETXMLCFG_COURT_COMMONENABLE	(NETXMLCFG_CMD_MIN + 20)
#define NETXMLCFG_COURTAVP_CONFIGPARAS	(NETXMLCFG_CMD_MIN + 21)
#define NETXMLCFG_COURTAVP_CALLSTATE	(NETXMLCFG_CMD_MIN + 22)
#define NETXMLCFG_COURTAVP_CALLDEVINFO	(NETXMLCFG_CMD_MIN + 23)
#define NETXMLCFG_COURTAVP_SUBSTREAM	(NETXMLCFG_CMD_MIN + 24)
#define NETXMLCFG_SCHEDULES_JUDGE		(NETXMLCFG_CMD_MIN + 25)
#define NETXMLCFG_COURT_ZFCHNINFO		(NETXMLCFG_CMD_MIN + 26)
#define NETXMLCFG_CMD_FALLINGOBJECT		(NETXMLCFG_CMD_MIN + 27)
#define NETXMLCFG_CMD_MIXAUDIOCONFIG	(NETXMLCFG_CMD_MIN + 28)
#define NETXMLCFG_POLICECLOTH			(NETXMLCFG_CMD_MIN + 29)
#define NETXMLCFG_PRISONERCLOTH			(NETXMLCFG_CMD_MIN + 30)
#define NETXMLCFG_QUEUESTAND			(NETXMLCFG_CMD_MIN + 31)
#define NETXMLCFG_PLATEINFO_EDIT		(NETXMLCFG_CMD_MIN + 32)	//修改车牌信息 Edit plate info
#define NETXMLCFG_PLATEINFO_DELETE		(NETXMLCFG_CMD_MIN + 33)	//删除车牌信息 Delete plate info
#define NETXMLCFG_PLATELIBINFO			(NETXMLCFG_CMD_MIN + 34)
#define NETXMLCFG_POSITION_COLOR		(NETXMLCFG_CMD_MIN + 35)	//获取屏幕颜色 Get screen color
#define NETXMLCFG_STRATEGY_CONDITION	(NETXMLCFG_CMD_MIN + 36)	//设置/获取主动策略条件参数 Set/Get the active policy condition parameter
#define NETXMLCFG_STRATEGY_INPUT		(NETXMLCFG_CMD_MIN + 37)	//设置/获取主动策略输入参数 Set/Get active policy input parameters
#define NETXMLCFG_PLATELIB_GET			(NETXMLCFG_CMD_MIN + 38)	//获取车牌库参数
#define NETXMLCFG_PLATELIB_DELETE		(NETXMLCFG_CMD_MIN + 39)	//删除车牌库
#define NETXMLCFG_PLATELIB_SET			(NETXMLCFG_CMD_MIN + 40)	//设置车牌库参数
#define NETXMLCFG_GET_TEMP_RANGE		(NETXMLCFG_CMD_MIN + 41)	//获取温度单位与温度范围映射关系
#define NETXMLCFG_MAINTAIN_DEMIST		(NETXMLCFG_CMD_MIN + 42)	//设置/获取系统维护除雾配置参数
#define NETXMLCFG_CMD_MAX				(NETXMLCFG_CMD_MIN + 43)




//NetClient_XmlCmdConfig  Interface parameters '_iCmd' define
#define NETXMLCMD_MIN						0
#define NETXMLCMD_SMART_PIC_QUERY		(NETXMLCMD_MIN + 0)
#define NETXMLCMD_THREEDIMTRANS			(NETXMLCMD_MIN + 1)
#define NETXMLCMD_PLATEINFO_QUERY		(NETXMLCMD_MIN + 2)		//查询车牌信息 Query plate info
#define NETXMLCMD_PLATEINFO_ADD			(NETXMLCMD_MIN + 3)		//添加车牌信息 Add plate info
#define NETXMLCMD_PLATELIB_ADD			(NETXMLCMD_MIN + 4)		//添加车牌库参数
#define NETXMLCMD_MAX					(NETXMLCMD_MIN + 5)




#define WIFISTATE_OFFLINE		0
#define WIFISTATE_ONLINE		1
#define WIFISTATE_APPCFG		2

typedef struct tagWifiState
{
    int				iSize;
    int             iWifiState; // 0：断开 1：连接 2：APP开始配置wifi
}WifiState, *pWifiState;

typedef struct tagComRecvData
{
	int								iSize;
	int								iComNo;
	int								iDataLen;
	unsigned char                   ucBuffer[LEN_1024];
}ComRecvData,*pComRecvData;

typedef struct tagGeografhyLocationFireAlarm
{
	int				iBufSize;
	Coordinate		tLongitudeInfo;
	Coordinate		tLatitudeInfo;
}GeografhyLocationFireAlarm, *pGeografhyLocationFireAlarm;


#define NETCLIENT_COMMONFUNC_CMD_MIN												   0
#define NETCLIENT_COMMONFUNC_CMD_REPAIR_MP4FILE			NETCLIENT_COMMONFUNC_CMD_MIN + 0	//修复mp4文件
#define NETCLIENT_COMMONFUNC_CMD_MP4_HEVC_CODETYPE		NETCLIENT_COMMONFUNC_CMD_MIN + 1	//mp4 hevc封装格式
#define NETCLIENT_COMMONFUNC_CMD_MAX					NETCLIENT_COMMONFUNC_CMD_MIN + 2	

#define HEVC_ENCODE_TYPE_HEV1    0   //hev1格式，macos默认播放器无法播放
#define HEVC_ENCODE_TYPE_HVC1    1	 //hvc1格式,macos兼容

typedef struct tagRepairMp4File
{
	char cFileName[LEN_1024];
	char cMoovFileName[LEN_1024];
}RepairMp4File,*pRepairMp4File;

#define TOKEN_LOGON					0
#define TOKEN_RTSP_VIDEO			1
typedef struct _tagTokenInfo
{
	char   cToken[LEN_20]; 
	int    iType; //0:logon   1:rtsp video
}TokenInfo;

typedef struct _tagNetFileForceStopPara
{
	unsigned int		uiNoDataTimeout;	//等于0表示无数据时不需要sdk内部强制断开，大于0表示超时多长时间无数据需要sdk强制断开释放下载通道资源（单位：秒）

} NetFileForceStopPara, *pNetFileForceStopPara;

#endif


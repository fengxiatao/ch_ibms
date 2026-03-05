#ifndef _AVPLAYSDK_TYPES_H_
#define _AVPLAYSDK_TYPES_H_

#include "GlobalTypes.h"

#ifdef __WIN__
#define PLAYHWND		HWND
#define PLAYHDC			HDC
#define SDKint64		__int64
#else
#define PLAYHWND		void*
#define PLAYHDC			void*
#define SDKint64		long long
#define UINT			unsigned int
#define DWORD			unsigned long
#define WM_USER			0xFFFF
#endif

#ifndef __cplusplus
#define bool			int
#endif

/*********************************************************************************************/
#define CALLING_CONVENTION			__cdecl
#define MAX_PLAYER_NUM				1024
#define MAX_SYC_GROUP_NUM			4				//Maximum Number Of Groups Played Back Synchronously
#define MAX_SYC_PLAYER_NUM			16				//Maximum Player Number for Synchronized Playback
#define MSG_PLAYSDKM4				(WM_USER+0x01FF)//Default Values For Playback Library Messages
/*********************************************************************************************/


/**********************************************************************************************
* Functional Macro Definition                                                                 
**********************************************************************************************/
#define	SAFE_CLOSE_THREAD(x) if (NULL != (x))	{WaitForSingleObject((x),INFINITE); CloseHandle((x)); (x) = NULL;}
#define	SAFE_CLOSE_HANDLE(x) if (NULL != (x))	{CloseHandle((x)); (x) = NULL;}
#define SAFE_DELETE(x)	if ((x) != NULL)	{delete (x); (x) = NULL;}
#define SAFE_FREE(x)	if ((x) != NULL)	{free((x)); (x) = NULL;}
#define SAFE_DELETE_ARRAY(x)	if ((x) != NULL)	{delete [] (x); (x) = NULL;}
#define SAFE_CLOSE_FILE(x) if ((x) != NULL)	{fclose((x)); (x) = NULL;}
/*********************************************************************************************/



/**********************************************************************************************
* Number of Show Video                                                                   
**********************************************************************************************/
#define	MAX_DRAW_NUM				8
/*********************************************************************************************/

/**********************************************************************************************
* Number of Voice Capture                                                                   
**********************************************************************************************/
#define	MAX_VOICE_CAPTURE_NUM		8
#define VOICE_CAPTURE_BASE			4096 //avoid old intrerface use id to start/stop voice
#define NVS_VOICE_CAPTURE			0 + VOICE_CAPTURE_BASE
#define ONVIF_VOICE_CAPTURE			1 + VOICE_CAPTURE_BASE
#define P2P_VOICE_CAPTURE			2 + VOICE_CAPTURE_BASE
/*********************************************************************************************/


/**********************************************************************************************
* List of LPARAM Parameter Values For Playback Library Messages                                                                     
**********************************************************************************************/
#define LPARAM_FILE_PLAY_END		0		//Play Finished
#define LPARAM_FILE_SEARCH_END		1		//Find The End Of The File
#define LPARAM_NEED_DECRYPT_KEY		2		//The Password Needs To Be Decrypted
#define LPARAM_DECRYPT_KEY_FAILED	3		//Decryption Password Error
#define LPARAM_DECRYPT_SUCCESS		4		//Decrypt Success
#define LPARAM_STREAM_SEARCH_END	5		//There Is No Data In The Stream Buffer
#define LPARAM_VOD_STREAM_END		6		//VOD File Stream Played Out
#define LPARAM_DIGITALWATERMARK_NOTMATCH	7	//digital watermark not match
#define LPARAM_VOD_SM3CHECK_FAILED	8		//HASH Check Failed
#define LPARAM_ENCRYPT_TO_NOTENCRPT 9		//From Encrypt to Not Encrypt

/*********************************************************************************************/

/**********************************************************************************************
* return value for encrypt frame                                                                      
**********************************************************************************************/
#define ENCRYPT_SUCCESS				0		// encrypt sucess
#define ENCRYPT_INVALID_PARAM		(-1)	// error param
#define ENCRYPT_KEY_NOT_READY		(-2)	// invalid key
#define ENCRYPT_FAILED				(-3)	// encrypt failed
/*********************************************************************************************/

/**********************************************************************************************
* Decrypt frame return value                                                                     
**********************************************************************************************/
#define DECRYPT_SUCCESS				0		//Decrypt Success
#define DECRYPT_INVALID_PARAM		(-1)	//Parameter Err
#define DECRYPT_KEY_NOT_READY		(-2)	//Not Set Decrypt Key
#define DECRYPT_FRAME_NOT_ENCRYPT	(-3)	//Frame Is Not Encrypt Frame
#define DECRYPT_FAILED				(-4)	//Frame Decrypt Failed
/*********************************************************************************************/


/**********************************************************************************************
* Record File                                                                       
**********************************************************************************************/
#define REC_FILE_TYPE_STOP			(-1)
#define REC_FILE_TYPE_NORMAL		0
#define REC_FILE_TYPE_AVI			1
#define REC_FILE_TYPE_ASF			2
#define REC_FILE_TYPE_AUDIO			3
#define REC_FILE_TYPE_WAV			4
#define REC_FILE_TYPE_MP3			5
#define REC_FILE_TYPE_ZFMP4         10
/*********************************************************************************************/


/**********************************************************************************************
* Player Control Code Used By TC_PlayerControl                                                             
**********************************************************************************************/
#define CTRL_VOD_SETOVERFLAG			0		//set vod play end flag
#define CTRL_VOD_CLEANBUFF				1		//clean vod buffer
#define CTRL_RESETDECODE				2		//resetdecode clean yuv
#define CTRL_RESVERSE_PLAY				3		//reverse play
#define CTRL_REGIST_USERDATA_NOTIFY		4		//regist usrdata notify
#define CTRL_REGIST_DRAW_NOTIFY			5		//regist draw notify
#define CTRL_REGIST_RAW_FRAME_NOTIFY	6		//regist rwa frame notify
#define CTRL_SEEK_BY_ABSTIME			7		//seek play by abs time
#define CTRL_SEEK_BY_RELATIVETIME		8		//seek play by Relative time
#define CTRL_REGIST_DRAW_YUV_NOTIFY		9		//regist draw yuv data notify
#define CTRL_GET_CUDA_INFO				10		//get cuda context
#define CTRL_DRAW_FACE_PARAM			11		//draw face ident rect
#define CTRL_DRAW_ON_HDC				12		//inner draw on HDC
#define CTRL_DRAW_TARGET_NOTIFY			13		//regist usrdata target notify
#define CTRL_REGIST_DRAW_V5				14		//regist drawFunc notify
#define CTRL_GET_VIDEOHEADER			15		//get video header:S_header
#define CTRL_FORBID_SHOW				16		//forbid show video
#define CTRL_GET_DRAW_PARAM				17		//get draw param
#define CTRL_SEEK_BY_TENTHOUSANDPERCENT   18    //seek play by ten thousand percent
#define CTRL_GETPLAYTIMEBYID			19		//getplay time byid
#define CTRL_GETPLAYTIMEBYFILE			20		//getplay time by file
#define CTRL_CAPTURE_IFRAME_YUV			21		//capture iframe yuv
#define CTRL_NEED_MANUAL_PLAY			22		//whether need call play by manual
#define CTRL_REGIST_PLAYER_MAIN_NOTIFY	23		//regist main notify
#define CTRL_CONFIG_CLIENT_CHARSET		24		//config local client charset
#define CTRL_VIDEO_ROTATE				25		//control video rotate
#define CTRL_VIDEO_COVER_PARA			26		//video cover param
#define CTRL_RESET_PLAYTIME				27		//reset playtime
#define CTRL_PRESET_GLOBALDECRYPTKEY	28		//Preset default global decryption key
#define CTRL_REG_VOICE_CAPTURE_CALLBACK	29		//regiser pcm  voice capture callback
#define CTRL_BLOCK_ADD_REF				30		//add ref to Dec Block
#define CTRL_BLOCK_RELEASE				31		//release ref to Dec Block
#define CTRL_BLOCK_GETBUFFER			32		//get Block buffer 
#define CTRL_REG_DEC_BLOCK_CALLBACK		33		//regiser decode block data
#define CTRL_SET_DISPLAY_REGION			34		//set display region, DZ functionnot supported
#define CTRL_GET_DISPLAY_REGION			35		//get display region, DZ functionnot supported
#define CTRL_WND_RESOLUTION_CHANGE		36		//show window resolution change, DZ functionnot supported
#define CTRL_GET_FILE_TYPE				37		//get sdv file type -1 is aov file
#define CTRL_GETPLAYTIME_RELATIVE		38		//get relative play time
#define CTRL_GET_BEGINEND_TIME			39
#define CTRL_GET_FRAMENO_DIFF			40      //get diff frame no
#define CTRL_QUERY_PLAYERID_BY_HWND		41      //query playerid by hwnd
#define CTRL_VIDEO_ROTATE_MIRROR		42		//control video rotate and mirror
#define CTRL_SET_MAX_USABLE_PLAYER_NUM	43		//set max usable players
#define CTRL_GET_MAX_USABLE_PLAYER_NUM	44		//get max usable players
#define CTRL_GET_USING_PLAYER_NUM		45      //get current have created players
/*********************************************************************************************/

/**********************************************************************************************
* Player Control Code Used By TC_GetFileInfo                                                             
**********************************************************************************************/
#define CMD_FILEINFO_HEADER			0		//get file header
#define CMD_FILEINFO_TYPE			1		//get file type

/*********************************************************************************************/

//CTRL_REGIST_USERDATA_NOTIFY
typedef struct tagUserDataNotify
{
	int iSize;
	DEVUSERDATA_NOTIFY pNotifyFun;
	void* pvUdata;
}UserDataNotify;

typedef struct tagRECTEx
{
	double		left;					//Proportion Of Original Video
	double		top;					//Proportion Of Original Video
	double		right;					//Proportion Of Original Video
	double		bottom;					//Proportion Of Original Video
}RECTEX, *PRECTEX;

typedef struct tagDrawParam
{
	PLAYHDC hDrawDc;
	unsigned int iTimeStamp;
	int iDcWidth;
	int iDcHeight;
	RECT rcShow;
	RECTEX rcSrc;
}DrawParam;
typedef void (*DRAW_NOTIFY_V4)(unsigned int _ulID, DrawParam* _ptDrawParam,  void* _iUser);
typedef int  (*DRAW_NOTIFY_V5)(unsigned int _ulID, void* _ptDraw, int _iSize, void* _pUser);
//CTRL_REGIST_DRAW_NOTIFY
typedef struct tagDrawNotify
{
	int iSize;
	DRAW_NOTIFY_V4 pNotifyFun;
	void* pvUdata;
}DrawNotify;

typedef struct tagDrawNotifyV5
{
	int iSize;
	DRAW_NOTIFY_V5  pDrawFun;
	void* pvUdata;
}DrawNotifyV5;

typedef struct tagYuvDataInfo
{
	unsigned char* pucYuvBuf;
	int iYuvBufLen;
	int iWidth;
	int iHeight;
	unsigned int uiTime;
	void* pvUsrData;
	int iYuvType;	//0-YV12,1-NV12
	int iYuvPitch;	
	int iPlayerID;
	struct tagYuvDataInfo *pNext;
	DrawGraphInfo *pDrawInfo;
	int iCount;
} YuvDataInfo;

typedef void (*DRAW_YUV_CallBack)(long _lPlayerHandle, YuvDataInfo* _ptYuv, int _iSize, void* _pvUdata);
typedef struct tagDrawYuvNotify
{
	int iSize;
	DRAW_YUV_CallBack pYuvCallBack;
	void* pvUdata;
	int iType;		//0YV12,1NV12
} DrawYuvNotify;

typedef struct tagCudaInfo
{
	int iSize;
	int iGpuId;
	void* pvCudaCtx;
} CudaInfo;

#define LANGUAGE_ENG							0
#define LANGUAGE_CHN							1

typedef struct tagDrawEnable
{
	int		iSize;			//结构体大小
	int		iType;			//播放库画线类型参数：DRAW_TYPE_FACE_TARGET ~ DRAW_TYPE_MAX
	int		iEnable;		//播放库画线使能参数：1-开启画线，2-关闭画线
	int		iMaxTrackTime;	//仅用于轨迹画线iType为9(DRAW_TYPE_VCATRACK)时有用，传-1代表高空抛物轨迹，大于0代表全景球人员目标轨迹
	float	fAlpha;			//用于伪彩图
	int		iLanguage;		//用于警服、识别服检测 控制文本中英文绘制 0-英文(默认) 1-中文
}DrawEnable;

typedef DrawEnable DrawFaceInfo;

typedef struct tagPictureData
{
	int			iDataLen;		//Image length
	char*		pcPicData;		//Picture raw data, the upper can be directly saved as a picture,no need to free
} PictureData, *pPictureData;

//主消息回调
typedef void (*PLAYER_MAIN_NOTIFY)(unsigned int _uiID, unsigned int _uiType, void* _pvData, int _iSize, void* _pvUser);
typedef struct tagPlayerMainNotify
{
	int iSize;
	PLAYER_MAIN_NOTIFY  pPlayerMainFun;
	void* pvUdata;
}PlayerMainNotify;

#define DIGITAL_WATERMARK_CORRECT		0    //校验正确
#define DIGITAL_WATERMARK_NOTCORRECT    1    //校验失败
typedef struct tagPlayerDigitalWaterMark
{
	int  iType;                        //是否篡改
	int  iCharSet;                     //0 UTF-8   1: GB2312
	char cDigitalWaterMark[LEN_256];   //水印内容
	unsigned long long	ullCurrentTime;		//当前时间，本地时间
	char				cMac[LEN_128];		//设备MAC内容
	char				cDevModel[LEN_256]; //设备型号内容
}PlayerDigitalWaterMark;


/**********************************************************************************************
* Main Notify Type                                                                      
**********************************************************************************************/
#define PLAYER_MAIN_NOTIFY_DIGITAL_WATERMARK   0  //数字水印


/**********************************************************************************************
* Draw Type                                                                      
**********************************************************************************************/
#define DRAW_TYPE_FACE_TARGET		0
#define DRAW_TYPE_FACE_ATTR			1
#define DRAW_TYPE_VCA_RULE			2
#define DRAW_TYPE_VCA_TARGET		3
#define DRAW_TYPE_VCA_ATTR			4
#define DRAW_TYPE_ILLEGAL_PARK		5
#define DRAW_TYPE_TEMP_DETEC        6
#define DRAW_TYPE_LIGHTNING         7
#define DRAW_TYPE_TARGET_CB         8
#define DRAW_TYPE_VCATRACK			9
#define DRAW_TYPE_PCOLOR			10
#define DRAW_TYPE_POLICE			11
#define DRAW_TYPE_VEST				12
#define DRAW_TYPE_TEMPERATURE		13
#define DRAW_TYPE_VCA_DEBUG_INFO    14
#define DRAW_TYPE_MAX				15

#define DRAW_FACE_TYPE_RECT			DRAW_TYPE_FACE_TARGET
#define DRAW_FACE_TYPE_TEXT			DRAW_TYPE_FACE_ATTR
/*********************************************************************************************/


/**********************************************************************************************
* Audio type                                                                      
**********************************************************************************************/
#define T_AUDIO8					0
#define T_YUV420					1
#define T_YUV422					2
/*********************************************************************************************/


/**********************************************************************************************
* Play Mode                                                                      
**********************************************************************************************/
#define	PLAYMODE_FILE				0		//File Mode Play
#define	PLAYMODE_VOD_FILE			1		//VOD Mode Play
#define	PLAYMODE_STREAM				2		//Stream Mode Play
#define PLAYMODE_UNIVERSAL_FILE     3       // universal file mode play
/*********************************************************************************************/


/**********************************************************************************************
* Sleep Time                                                                     
**********************************************************************************************/
#define	SLEEP_1						1		//1 Millisecond
#define	SLEEP_100					100		//100 Millisecond
/*********************************************************************************************/


/**********************************************************************************************
* 返回值错误码含义                                                                        
**********************************************************************************************/
#define RET_INVALID_PLAYERID		-1		//Illegal PLAYER ID
#define RET_ERR_PLAYMODE			-2		//Play Mode Err
#define RET_INVALID_PARAM			-3		//Illegal Parameter
/*********************************************************************************************/


/**********************************************************************************************
* 刷新功能控制码                                                                        
**********************************************************************************************/
#define CTRL_REFRESHPLAYER			0		//Initialize The PLAYER Window As A Black Background

#define PLAY_SPEED_VALUE_1			1		//2-Fold Velocity Value
#define PLAY_SPEED_VALUE_2			2		//4-Fold Velocity Value
#define PLAY_SPEED_VALUE_3			3		//8-Fold Velocity Value
#define PLAY_SPEED_VALUE_4			4		//16-Fold Velocity Value
#define PLAY_SPEED_VALUE_5			5		//32-Fold Velocity Value

#define PLAY_SPEED_2X_RATE			2		//2-fold rate
#define PLAY_SPEED_4X_RATE			4		//4-fold rate
#define PLAY_SPEED_8X_RATE			8		//8-fold rate
#define PLAY_SPEED_16X_RATE			16		//16-fold rate
#define PLAY_SPEED_32X_RATE			32		//32-fold rate
/*********************************************************************************************/


/**********************************************************************************************
* Mode Rule                                                                    
**********************************************************************************************/
#define MODERULE_AUTO_ADJUST_DRAW_NO				(0x00)	//Adaptive Data with Different Resolution--No
#define MODERULE_AUTO_ADJUST_DRAW_YES				(0x01)	//Adaptive Data with Different Resolution--Yes
#define MODERULE_AUTO_ADJUST_STREAM_PLAY_SPEED_YES	(0x11)	//Adaptively adjust the playback rate of streaming mode -- yes
#define MODERULE_AUTO_ADJUST_STREAM_PLAY_SPEED_NO	(0x10)	//Adaptively adjust the playback rate of streaming mode -- No
#define MODERULE_STREAM_ADAPTIVE					(0x24)	//Set to adaptive mode
#define MODERULE_STREAM_PLAY_BALANCE				(0x23)	//Set to Equilibrium Mode
#define MODERULE_STREAM_PLAY_NO_DELAY				(0x22)	//Set to no-delay mode
#define MODERULE_STREAM_PLAY_HIGH_SMOOTH			(0x21)	//Set to High Fluency Mode
#define MODERULE_STREAM_PLAY_LOW_DELAY				(0x20)	//Set to Low Delay Mode
#define MODERULE_AVIDEO								(0x30)	//Set to Audio and Video
#define MODERULE_PURE_AUDIO							(0x31)	//Set to pure audio mode
#define MODERULE_FAST_FORWARD_SKIP_I				(0x40)	//Set to Fast Forward Jump I Frame
#define MODERULE_FAST_FORWARD_NO_SKIP_I				(0x41)	//Set to Fast Forward No Jump I Frame
#define MODERULE_SINGLE_FILE						(0x50)	//Set to single file mode
#define MODERULE_MUTI_FILE							(0x51)	//Set to multi-file mode
#define MODERULE_FIX_RATIO							(0x60)	//Proportional video display
#define MODERULE_FIX_WINDOW							(0x61)	//Fill window display
#define MODERULE_VIDEO_OPTIMIZE_CLOSE				(0x70)	//Optimized closure of video effects
#define MODERULE_VIDEO_OPTIMIZE_OPEN				(0x71)	//Optimizing Opening of Video Effect
/*********************************************************************************************/


/*********************************************************************************************/
typedef void (CALLING_CONVENTION* pfCBPlayEnd)(int _iID);
#ifdef __WIN__
typedef void (CALLING_CONVENTION* pfCBGetDecAV)(int _iID, const DecAVInfo* _pDecAVInfo, void* _iUser);
#endif
typedef void (CALLING_CONVENTION* pfCBVideoEdit)(int _iNotifyCode);
#ifndef pfCBDrawFun
typedef int  (CALLING_CONVENTION* pfCBDrawFun)(long _lHandle, PLAYHDC _hDc, void* _lUserData);
#endif
typedef int  (CALLING_CONVENTION* pfCBDrawFunEx)(long _lHandle, PLAYHDC _hDc, long _lWidth, long _lHeight, void* _lUserData);
#ifdef __WIN__
typedef int  (CALLING_CONVENTION* pfCBCommonEventNotify)(int _iPlayerID, int _iEventMessage);
#else
typedef int (*pfCBCommonEventNotify)(int _iPlayerID, int _iEventMessage, void* _pUserData);
#endif
typedef int  (CALLING_CONVENTION* pfCBCommonEventNotifyEx)(int _iPlayerID, int _iEventMessage, void* _lpUserData);
typedef void (__stdcall* pfAudioCaptureCbk)(unsigned char* _pucData, int _iLen, void* _pvUserData);
//Callback function of original stream
typedef void (__stdcall* pfRawFrameCbk)(int _iPlayerID, unsigned char* _pucData, int _iLen, void* _pvUserData);
typedef void (*pfCBGetRaw)(int _iID, char* _pData, int _iSize, void* _iUser);
//Audio acquisition for intercom
typedef int (*RECORD_VOICE_NOTIFY)(unsigned char *_pucAuidoBuffer, int _iAudioLen);

//Create player type flags on demand
typedef enum __tagPlayModeCmd
{
	E_NORMAL_PLAYER				= -1,
	E_FILE_ONLYDECODE			= 0,			//File mode decoding only
	E_STREAM_ONLYDECODE			= 1,			//Stream mode decoding only
	E_YUV_PLAYER				= 2,			//YUV Player
	E_RGB_PLAYER				= 3,			//RGB Player
	E_SYC_PLAYER				= 4,			//Sync Player
	E_VOD_RAWNOTIFY				= 5,			//Front-end playback original stream callback
	E_HWDECODE_PLAYER			= 6,			//Hard Dec
	E_CUDA_STREAMPLAYER			= 7,            //CUDA
	E_NORMAL_FILEPLAYER			= 8,            //File Mode Player
	E_NORMAL_STREAMPLAYER		= 9,            //Stream Mode Player
	E_NORMAL_VODPLAYER			= 10,           //VOD Mode Player
	E_CUDA_FILE_ONLYDECODE		= 11,           //CUDA File mode decoding only
	E_CUDA_FILEPLAYER			= 12,			//CUDA File Mode Player
	E_UNIVERSAL_STREAMPLAYER	= 13,			//universal flow Player
	E_CUDA_STREAM_ONLYDECODE	= 14,            //CUDA Stream mode decoding only
    E_UNIVERSAL_FILEPLAYER      = 15,
	E_HW_VOD_PLAYER				= 16,
	E_HWDECODE_D3D11_PLAYER     = 17
}PlayModeCmd, *LPPlayModeCmd, linux_PlayModeCmd;

#define SINGLE_THREAD_WORK		0	//Single Thread Decoding Mode
#define MULTI_THREAD_WORK		1	//Multithread Decoding Mode
#define DEFAULT_THREAD_COUNT	4	//The default number of worker threads in multithreaded decoding mode is 4

#define STREAM_DECODE_MODE		0
#define FRAME_DECODE_MODE		1

typedef struct tagDecoderPara		//SDK defaults to multithreaded decoding mode
{
	int iSize;
	int iWorkMode;		//Decoder working mode：0-->Single Thread: 1-->Multithread  
	int iThreadCount;	//Number of worker threads in multithreading mode, default is 4
	int iDecodeMode;	//解码模式：0--流式解码，1--帧解码
} DecoderPara, linux_DecoderPara;

#define MAX_FILENAME_LEN	256
typedef struct tagFilePlayer
{
	int				iSize;
	PLAYHWND		hWnd;
	char			cFileName[MAX_FILENAME_LEN];
	DecoderPara		tDecoderPara;
	int				iWndFlag;			//Property identifying wnd object: 0 -- QtWidget object (default), 1 -- QtLayout object
	void*			ptFileOperate;		//file opretor function
	int             iFileOperateBufLen;
} FilePlayer, *pFilePlayer, linux_FilePlayer, *plinux_FilePlayer, UniversalFilePlayer, *PUniversalFilePlayer;

typedef struct tagStreamPlayer
{
	int				iSize;
	PLAYHWND		hWnd;
	unsigned char	ucVideoHeadBuf[PLAY_VIDEO_HEADER_LEN];
	int				iMaxStreamBufSize;
	DecoderPara		tDecoderPara;
	int				iWndFlag;			//Property identifying wnd object: 0 -- QtWidget object (default), 1 -- QtLayout object
} StreamPlayer, *pStreamPlayer, VodPlayer, *pVodPlayer, linux_StreamPlayer, *plinux_StreamPlayer, linux_VodPlayer, *plinux_VodPlayer;

typedef struct tagCudaPlayer
{
	int				iSize;
	int				iGpuID;
	PLAYHWND		hWnd;
	unsigned char	ucVideoHeadBuf[PLAY_VIDEO_HEADER_LEN];
}CudaPlayer, pCudaPlayer;

typedef struct tagCudaFilePlayer
{
	int				iSize;
	int				iGpuID;
	PLAYHWND		hWnd;	//Window handle passes NULL, decoding only and not displaying
	char			cFileName[MAX_FILENAME_LEN];
} CudaFilePlayer, *pCudaFilePlayer;

typedef struct tagUniversalStreamPlayer
{
	int				iSize;
	PLAYHWND		hWnd;
	DecoderPara		tDecoderPara;
} UniversalStreamPlayer, *pUniversalStreamPlayer;

typedef struct __tagPlayerHeader
{
	int			iSize;
	PLAYHWND	hWnd;
	int			iWidth;
	int			iHeight;
	int			iWndFlag;			//Property identifying wnd object: 0 -- QtWidget object (default), 1 -- QtLayout object
} TPlayerHeader, *PTPlayerHeader;

typedef void (CALLING_CONVENTION* FISHEYE_CallBack)(int _iID, int _iSubID, PLAYHDC _pvDC, unsigned int _uiWidth, unsigned int _uiHeight, void* _pvUsrData);

//Installation Method of Fisheye Camera
#define FEC_MOUNTING_WALL			1	//Wall mounting method (normal level)
#define FEC_MOUNTING_FLOOR			2	//Ground Installation (Normal Up)
#define FEC_MOUNTING_CEILING		3	//Top mounting mode (normal downward)

//Correction Method of Fisheye Camera
#define FEC_CORRECT_PTZ		0x100		//PTZ 
#define FEC_CORRECT_180		0x200		//180 degree correction
#define FEC_CORRECT_360		0x300		//360 Panoramic Correction
#define FEC_CORRECT_LAT		0x400		//Latitude development
#define FEC_CORRECT_FE      0x500		//鱼眼

//Setting Fisheye Correction Parameter CMD
#define CMD_CORRECT_PTZPARA					1		//PTZ Correction parameter
#define CMD_CORRECT_CYCLERADIUS				2		//Fisheye image center parameter for the center, this command only supports acquisition
#define CMD_CORRECT_WIDESCANOFFSET			3		//180 or 360 degree correction parameters
#define CMD_CORRECT_DISPlAY_PARA			4		//fish player diaplay para
#define CMD_CORRECT_SELECTEDWINDOW_PARA		5		//fish player Selected window para
#define CMD_RESERVE_LAST_FRAME				0XFF	//Whether to keep the last frame or not,0-No reservation,1-reservation. Default does not reserve, video screen display black screen.

//fish player diaplay method
#define FISHPLAYER_DISPLAY_FISH        			1		//鱼眼
#define FISHPLAYER_DISPLAY_180         			2		//180全景  
#define FISHPLAYER_DISPLAY_360         			3		//360全景
#define FISHPLAYER_DISPLAY_360_1PTZ    			4		//360全景+ptz
#define FISHPLAYER_DISPLAY_360_3PTZ    			5		//360全景+3ptz
#define FISHPLAYER_DISPLAY_360_6PTZ    			6		//360全景+6ptz
#define FISHPLAYER_DISPLAY_360_8PTZ    			7		//360全景+8ptz
#define FISHPLAYER_DISPLAY_2PTZ        			8		//2ptz
#define FISHPLAYER_DISPLAY_4PTZ        			9		//4ptz
#define FISHPLAYER_DISPLAY_FISH_3PTZ   			10		//鱼眼+3ptz
#define FISHPLAYER_DISPLAY_FISH_8PTZ   			11		//鱼眼+8ptz
#define FISHPLAYER_DISPLAY_LAT         			12		//全景-纬度展开
#define FISHPLAYER_DISPLAY_LAT_3PTZ    			13		//全景+3ptz
#define FISHPLAYER_DISPLAY_LAT_8PTZ    			14		//全景+8ptz
#define FISHPLAYER_DISPLAY_SOURCE      			15		//原图
#define FISHPLAYER_DISPLAY_FISH_4PTZ   			16		//鱼眼+4ptz
#define FISHPLAYER_DISPLAY_LAT_4PTZ   			17		//全景+4ptz
#define FISHPLAYER_DISPLAY_MAX	   				18		//显示类型数值上限

//PTZ Return to the contour point of the corresponding image type
#define PTZ_RET_TYPE_NONE	 0x0000		//Instead of returning contour points, only images are returned
#define PTZ_RET_TYPE_FISH	 0x1000		//PTZ Return to the outline of the fish eye image
#define PTZ_RET_TYPE_180	 0x2000		//PTZ Return to 180 degree correction image contour points, temporarily not supported
#define PTZ_RET_TYPE_360	 0x3000		//PTZ Return to the outline point of 360 degree corrected image
#define PTZ_RET_TYPE_LAT	 0x4000		//PTZ Return to the contour point of the latitude unwrapped corrected image
typedef struct tagCorrectPtzPara
{
	int				iSize;
	int				iPtzID;				//Ptz 窗口编号,范围0-7
	int				iPTZPositionX;		//PTZ Displayed central position X coordinates
	int				iPTZPositionY;		//PTZ Display center position Y coordinates
	int				iZoom;				//PTZ Displayed range parameters
	int				iPTZRstWidth;		//PTZ The width of the returned image
	int				iPTZRstHeight;		//PTZ Return the height of the image
	int				iPTZRetType;		//PTZ Return to the corresponding image type
} CorrectPtzPara, *pCorrectPtzPara;

typedef struct tagCorrectCyclePara
{
	int		iSize;					
	int		iRadiusLeft;		//The leftmost X-coordinate of a circle, expressed in tens of thousands of decimal ratios
	int		iRadiusRight;		//The rightmost X-coordinate of a circle, expressed in tens of thousands of decimal ratios
	int		iRadiusTop;			//Y coordinates on the top edge of a circle, expressed in tens of thousands of decimal ratios
	int		iRadiusBottom;		//Y coordinates at the bottom of a circle, expressed in decimal ratio
} CorrectCyclePara, *pCorrectCyclePara;

typedef struct tagCorrectWideScanPara
{
	int		iSize;					
	float   fWideScanOffset;        // Migration angles corrected at 180 or 360 degrees
} CorrectWideScanPara, *pCorrectWideScanPara;

typedef struct tagCorrectDisplayPara
{				
	int					iDisplayMethod;	//display method: 4--360全景+ptz
} CorrectDisplayPara, *pCorrectDisplayPara;

typedef struct tagCreateCorrectPara
{
	int					iSize;					
	int					iPlaceType;		//Installation Method of Fisheye Camera
	int					iCorrectType;	//Correction Method of Fisheye Camera
	CorrectCyclePara	tCycleRadius;	//Fisheye image center parameters
	CorrectWideScanPara	tWideScanPara;	//80 or 360 Degree Corrected Migration Angle
	CorrectPtzPara		tPtzPara;		//PTZ Correction parameter
	PLAYHWND			hWnd;
	CorrectDisplayPara	tDisplayPara;	//display para
} CreateCorrectPara, *pCreateCorrectPara;

//Snapshot type
#define CAPTURE_PICTURE_TYPE_BMP		1
#define CAPTURE_PICTURE_TYPE_JPG		2 
#define CAPTURE_PICTURE_TYPE_FEC_BMP	3
#define CAPTURE_PICTURE_TYPE_FEC_JPG	4 
#define CAPTURE_PICTURE_TYPE_IFRAME_YUV	5 
#define CAPTURE_PICTURE_TYPE_FAST_JPG	6


#define DEFAULT_CYCLE_RADIUS_LEFT		0
#define DEFAULT_CYCLE_RADIUS_RIGHT		10000
#define DEFAULT_CYCLE_RADIUS_TOP		0
#define DEFAULT_CYCLE_RADIUS_BOTTOM		10000

#define MAX_FEC_TRACK_COUNT		8
#define START_ALL_FEC_TRACK		-1
#define STOP_ALL_FEC_TRACK		-1
typedef struct tagFECTrackInfo
{
	int			iSize;
	int			iWindowCount;						//Number of windows to be tracked, currently up to 8PTZ tracing is supported
	int			iPtzSubID[MAX_FEC_TRACK_COUNT];		//Each PTZ tracks the corresponding correction ID, and the SubID returned by interface TC_CreateFishEyeCorrect during PTZ correction
} FECTrackInfo, *pFECTrackInfo;

typedef struct tagSelectedWindowPara
{				
	int			iWinIdx;		//窗口索引编号，从0开始
	int			iDrawColor;		//绘制选择框颜色
} SelectedWindowPara, *pSelectedWindowPara;

typedef struct _SwscaleInfo
{
	char* yuv;
	int width;
	int height;
} SwscaleInfo;

//播放方向
#define DIR_FORWARD						0				//正方向
#define DIR_REVERSE						1				//反方向

typedef struct tagSeekByTenThousandPercent
{
    int iSize;
    int iTenThousandPercent;                            //万分比
} SeekByTenThousandPercent, *pSeekByTenThousandPercent;

#define TENTHOUSANDTH_RATIO_COORDINATE		0		//万分比坐标
#define VIDEO_RESOLUTION_COORDINATE			1		//视频分辨率坐标

#ifndef VIDEO_CONVOER_AREA
#define VIDEO_CONVOER_AREA			8	//视频遮挡区域数
#endif
typedef struct tagVideoCoverPara
{
	int			iEnable;
	int			iCoordinateType;
	int			iAreaCount;
	RECT		tAreaRect[VIDEO_CONVOER_AREA];
} VideoCoverPara, *pVideoCoverPara;

typedef struct tagComposePlayerPara
{
	int		iPlayerId;		//合成画面PlayerId，初始值必须为-1，因为0是有效值，由TC_CreatePlayer/TC_CreatePlayerFromFile/TC_CreatePlayerFromStream……系列接口返回
	int		iRegionNumber;	//Player在合成画面上的显示区域编号，区域按行和列从左到右从上到下的顺序移次是0，1，2，3……不指定区域传入-1
	int     iScaleW;
	int     iScaleH;
} ComposePlayerPara, *pComposePlayerPara;

typedef struct tagCpsWinPara
{
	int		iPlayerId;			//子窗口播放视频的PlayerId，初始值必须为-1，因为0是有效值，由TC_CreatePlayer……系列接口返回
	int		iRegionNumber;		//预留参数，不赋值
	int     iScaleW;			//缩放后视频的宽，不指定传0由sdk内部计算
	int     iScaleH;			//缩放后视频的高，不指定传0由ssdk内部计算
	RECT	rcArea;				//Player在合成画面上的子区域显示矩形
} CpsWinPara, *pCpsWinPara;

#define DEFAULT_COMPOSE_FRMRATE			30				//合成图像默认帧率30
#define MAX_COMPOSE_ROWS				25				//最多支持25行画面合成
#define MAX_COMPOSE_COLUMNS				25				//最多支持25列画面合成
#define MAX_COMPOSE_PLAYER_COUNT		MAX_COMPOSE_ROWS * MAX_COMPOSE_COLUMNS			//最多支持625画面合成
typedef void (*ComposeDataCallBack)(int _iID, YuvDataInfo* _ptYuv, int _iCount, int _iSize, void* _pvUdata);

//动态获取合成参数结构体
typedef struct tagComposeDynaPara
{
	int					iCpsFrt;						//合成图像帧率，上层不指定传0，库内部按默认30帧处理
	int					iCpsW;							//合成图像分辨率宽，上层不指定传0，库内部按默认1920处理
	int					iCpsH;							//合成图像分辨率高，上层不指定传0，库内部按默认1080处理
	PLAYHWND			hWnd;							//合成图像显示窗口对象句柄
	RECT				rcDisplayArea;					//合成图像显示窗口区域矩形
	Tvca_RGB			tBgClr;							//合成画面背景颜色
	//合成画面子窗口Player(通道)参数
	int					iSubWinCount;					//合成画面子窗口个数，不超过MAX_COMPOSE_PLAYER_COUNT
	int					iSizeOfSubWin;					//player参数结构体大小 sizeof(CpsWinPara)
	CpsWinPara*			ptSubWinList;					//Player数组，iPlayerId传-1表示当前子窗口不需要播放视频，sdk内部刷新成背景色
} ComposeDynaPara, *pComposeDynaPara;

//动态获取合成参数回调函数，返回0：获取参数成功，返回-1：获取参数失败
typedef int (*CBFun_GetCpsPara)(int _iCpsId, ComposeDynaPara* _ptDyncPara, int _iParaSize, void* _pvUdata);

typedef struct tagCreateComposeRenderer
{
	//合成画面布局参数
	int					iRows;								//行数
	int					iColumns;							//列数
	//合成画面视频参数
	int					iFrmRate;							//合成图像帧率，上层不指定传0，库内部计算
	int					iWidth;								//合成图像分辨率宽，上层不指定传0，库内部计算
	int					iHeight;							//合成图像分辨率高，上层不指定传0，库内部计算
	PLAYHWND			hWnd;								//合成图像显示窗口对象句柄
	int					iWndFlag;							//预留字段
	int					iReserve;							//预留字段
	//合成画面player（通道）参数
	int					iPlayerCount;						//需要合成的player个数
	int					iSizeofPlayerPara;					//player参数结构体大小 sizeof(ComposePlayerPara)
	ComposePlayerPara	tComposePlayerArr[MAX_COMPOSE_PLAYER_COUNT];	//Player数组
	Tvca_RGB			tBgClr;								//合成画面背景颜色
	int					iPicCnt;							//合成画面个数。均分布局此参数可不赋值，由iRows*iColumns计算所得；异形布局此参数必须赋值
	int					iPicLayout;							//当iPicCnt为2时，0-表示1*2布局；1-表示2*1布局
															//当iPicCnt为10时，0-表示1+9布局；1-表示2+8布局
	ComposeDataCallBack	pCallBackData;						//合成画面数据回调，与内部渲染互斥，注册回调就不显示合成画面
	void*				pvUsrData;							//注册回调函数附带用户数据
	CBFun_GetCpsPara	pfCpsParaFun;						//向sdk注册动态获取合成参数的回调
	void*				pvDynaParaUData;					//动态获取合成参数附带用户数据
} CreateComposeRenderer, *pCreateComposeRenderer;

#define RETAIN_LAST_FRMIMAGE		0
#define DRAW_BLACK_BACKGROUND		1 
typedef struct tagDestroyRendererPara
{
	int				iRetainLastFrmImage;	//是否保留最后一帧图像：0-保留最后图像，1-不保留	
} DestroyRendererPara, *pDestroyRendererPara;

typedef struct tagComposeRemovePlayer
{
	int				iPlayerId;				//合成画面PlayerId
	int				iRetainLastFrmImage;	//是否保留最后图像：0-保留最后图像，1-不保留	
} ComposeRemovePlayer, *pComposeRemovePlayer;

typedef struct tagComposePlayerYuv
{
	int				iRegionNumber;	//Player在合成画面上的显示区域编号，区域从大到小 按行和列从左到右从上到下的顺序移次是0，1，2，3……不指定区域传入-1
	int				iWidth;
	int				iHeight;
	unsigned char   *pBuf;
	int				iBufLen;
	int             iYuvType;//0:IYUV(I420)  1:IYV12
} ComposePlayerYuv, *pComposePlayerYuv;

//TC_SetComposeRendererPara接口命令ID
#define CMD_RENDERER_PARA_ADD_PLAYER			0		//向合成渲染器添加1个player，对应结构体为ComposePlayerPara
#define CMD_RENDERER_PARA_REMOVE_ONE_PLAYER		1		//向合成渲染器删除1个player，对应结构体为ComposeRemovePlayer
#define CMD_RENDERER_PARA_REMOVE_ALL_PLAYER		2		//删除合成器绑定的所有player，不带参数
#define CMD_RENDERER_PARA_ADD_YUV				3		//添加YUV数据
#define CMD_RENDERER_PARA_SET_SCALE				4		//设置转换后的YUV宽高

#define CMD_COMPOSE_TYPE_NORMAL					0		//正常模式
#define CMD_COMPOSE_TYPE_SCALE					1		//回调每个缩放后的YUV数据
#define CMD_COMPOSE_TYPE_DYNA					2		//支持异形布局任意拖拽和动态更新参数的合成器

typedef struct tagPcmVoiceCapture
{
	int					iVoiceCaptureId;		//捕获ID NVS_AUDIO_CAPTURE,ONVIF_AUDIO_CAPTURE 最大支持8个
	RECORD_VOICE_NOTIFY	pVoiceCbk;				//声音捕获回调函数RECORD_VOICE_NOTIFY类型的函数指针	
} PcmVoiceCapture, *pPcmVoiceCapture;

//CTRL_REG_DEC_BLOCK_CALLBACK
typedef struct tagDecBlockNotify{
	pfCBGetDecAV pNotifyFun;
	void* pvUdata;
}DecBlockNotify;

//CTRL_BLOCK_GETBUFFER
typedef  struct tagGetBlokBuffer{

	void*			pBlock;	//传入参数，回调函数中取得的视频解码后数据指针，通过该命令注册的CTRL_REG_DEC_BLOCK_CALLBACK
	unsigned char*	pBuf;	//传出参数，返回的yuv数据指针
	int             iBufLen;//传出参数，返回的yuv数据长度
}GetBlockBuffer;

//CTRL_GET_BEGINEND_TIME
typedef  struct tagGetTimeStamp{

	unsigned int			uiFirstTimeStamp;		//第一帧时间戳
	unsigned int			uiLastTimeStamp;		//最后一帧时间戳
	unsigned int			uiFirstAbsTime;			//首个绝对时间
	unsigned int			uiLastAbsTime;			//末个绝对时间
}GetTimeStamp;

typedef struct tagGetPlayerIDByHwnd
{
	PLAYHWND			hWnd;		
	int					iPlayerID;
} GetPlayerIDByHwnd, *pGetPlayerIDByHwnd;

#endif



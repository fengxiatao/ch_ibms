package src;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.ByValue;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.Callback;
import com.sun.jna.ptr.PointerByReference;

public interface NVSSDK extends Library {
	
	public static final int RET_SUCCESS = 0;		//success
	public static final int RET_FAILED = -1;		//failed
	
	public static final int SERVER_NORMAL = 0;
	public static final int SERVER_ACTIVE = 1;
	public static final int SERVER_DNS = 2;
	public static final int SERVER_FIND_PSW	= 3;
	public static final int SERVER_REG_ACTIVE = 4;
	
	public static final int WM_USER = 0x0400; //

	public static final int WM_MAIN_MESSAGE = WM_USER + 1001; 	//system message
	public static final int WM_PARACHG = WM_USER + 1002; 		//Parameter change message
	public static final int WM_ALARM = WM_USER + 1003; 			//Alarm message
	public static final int WCM_ERR_ORDER = 2;
	public static final int WCM_ERR_DATANET = 3;
	public static final int WCM_LOGON_NOTIFY = 7;
	public static final int WCM_VIDEO_HEAD = 8;
	public static final int WCM_VIDEO_DISCONNECT = 9;
	public static final int WCM_RECORD_ERR = 13;
	public static final int WCM_QUERYFILE_FINISHED = 18;
	public static final int WCM_DWONLOAD_FINISHED = 19;
	public static final int WCM_DWONLOAD_FAULT = 20;
	public static final int WCM_DOWNLOAD_INTERRUPT = 29;

	public static final int LOGON_SUCCESS = 0;
	public static final int LOGON_ING = 1;
	public static final int LOGON_RETRY = 2;
	public static final int LOGON_DSMING = 3;
	public static final int LOGON_FAILED = 4;
	public static final int LOGON_TIMEOUT = 5;
	public static final int NOT_LOGON = 6;
	public static final int LOGON_DSMFAILED = 7;
	public static final int LOGON_DSMTIMEOUT = 8;
	public static final int PLAYER_PLAYING = 0x02;
	public static final int USER_ERROR = 0x10000000;
	
	public static final int REC_FILE_TYPE_STOP = -1;
	public static final int REC_FILE_TYPE_NORMAL = 0;
	public static final int REC_FILE_TYPE_AVI = 1;
	public static final int REC_FILE_TYPE_ASF = 2;
	public static final int REC_FILE_TYPE_AUDIO = 3;
	public static final int REC_FILE_TYPE_RAWAAC = 4;
	public static final int REC_FILE_TYPE_VIDEO = 5;
	public static final int REC_FILE_TYPE_MP4 = 6;
	public static final int REC_FILE_TYPE_PS = 8;
	public static final int REC_FILE_TYPE_TS = 9;
	public static final int REC_FILE_TYPE_ZFMP4 = 10;

	public static final int MOVE_UP = 1;
	public static final int MOVE_UP_STOP = 2;
	public static final int MOVE_DOWN = 3;
	public static final int MOVE_DOWN_STOP = 4;
	public static final int MOVE_LEFT = 5;
	public static final int MOVE_LEFT_STOP = 6;
	public static final int MOVE_RIGHT = 7;
	public static final int MOVE_RIGHT_STOP = 8;
	public static final int MOVE_UP_LEFT = 9;
	public static final int MOVE_UP_LEFT_STOP = 10;
	public static final int MOVE_UP_RIGHT = 11;
	public static final int MOVE_UP_RIGHT_STOP = 12;
	public static final int MOVE_DOWN_LEFT = 13;
	public static final int MOVE_DOWN_LEFT_STOP = 14;
	public static final int MOVE_DOWN_RIGHT = 15;
	public static final int MOVE_DOWN_RIGHT_STOP = 16;

	public static final int HOR_AUTO = 21;
	public static final int HOR_AUTO_STOP = 22;

	public static final int ZOOM_BIG = 31;
	public static final int ZOOM_BIG_STOP = 32;
	public static final int ZOOM_SMALL = 33;
	public static final int ZOOM_SMALL_STOP = 34;
	public static final int FOCUS_FAR = 35;
	public static final int FOCUS_FAR_STOP = 36;
	public static final int FOCUS_NEAR = 37;
	public static final int FOCUS_NEAR_STOP = 38;
	public static final int IRIS_OPEN = 39;
	public static final int IRIS_OPEN_STOP = 40;
	public static final int IRIS_CLOSE = 41;
	public static final int IRIS_CLOSE_STOP = 42;
	public static final int LIGHT_ON = 43;
	public static final int LIGHT_OFF = 44;
	public static final int POWER_ON = 45;
	public static final int POWER_OFF = 46;
	public static final int RAIN_ON = 47;
	public static final int RAIN_OFF = 48;

	public static final int MOVE = 60;
	public static final int MOVE_STOP = 61;
	public static final int CALL_VIEW = 62;
	public static final int SET_VIEW = 63;

	public static final int ALARM_VDO_MOTION = 0;
	public static final int ALARM_VDO_REC = 1;
	public static final int ALARM_VDO_LOST = 2;
	public static final int ALARM_VDO_INPORT = 3;
	public static final int ALARM_VDO_OUTPORT = 4;
	public static final int ALARM_VDO_COVER = 5;
	public static final int ALARM_VCA_INFO = 6; 	//Intelligent analysis of alarm information

	//decode data type
	public static final int T_AUDIO8 = 0;
	public static final int T_YUV420 = 1;
	public static final int T_YUV422 = 2;
	
	public static final int NET_PICSTREAM_CMD_VCA = 1;			//Callback VCA image stream information
	public static final int NET_PICSTREAM_CMD_ITS = 2;			//Callback ITS image stream information
	public static final int NET_PICSTREAM_CMD_FACE = 3;			//Callback face image stream information
	public static final int NET_PICSTREAM_CMD_NORMALSNAP = 4;	//Callback normal snap image stream information
	
	//Front end video query
	public static final int CMD_NETFILE_QUERY_FILE = 0;
	public static final int CMD_NETFILE_ITS_QUERY_DATA = 1;
	public static final int CMD_NETFILE_ITS_GETTOTALCOUNT = 2;
	public static final int CMD_NETFILE_ITS_GETCURRENTCOUNT = 3;
	public static final int CMD_NETFILE_ITS_GETRESULT = 4;
	public static final int CMD_NETFILE_ITS_QUERY_TOTALCOUNT = 5;
	public static final int CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE = 6;
	public static final int CMD_NETFILE_QUERY_VCA = 7;
	
	public static final int DOWNLOAD_FLAG_FIRST_REQUEST	= 0;
	public static final int DOWNLOAD_FLAG_OPERATE_RECORD = 1;
	public static final int DOWNLOAD_FLAG_BREAK_CONTINUE = 2;

	public static final int DOWNLOAD_CMD_FILE = 0;
	public static final int DOWNLOAD_CMD_TIMESPAN = 1;
	public static final int DOWNLOAD_CMD_CONTROL = 2;
	public static final int DOWNLOAD_CMD_FILE_CONTINUE = 3;
	public static final int DOWNLOAD_CMD_GET_FILE_COUNT = 4;
	public static final int DOWNLOAD_CMD_GET_FILE_INFO = 5;
	public static final int DOWNLOAD_CMD_SET_FILE_INFO = 6;
	
	//download file type
	public static final int DOWNLOAD_FILE_TYPE_SDV = 0;
	public static final int DOWNLOAD_FILE_TYPE_PS = 3;
	
	public static final int VI_FRAME = 0;
	public static final int VP_FRAME = 1;
	public static final int AUDIO_FRAME = 5;

	public static final int RAW_VIDEO_H264 = 1;
	public static final int RAW_VIDEO_MPEG4 = 2;
	public static final int RAW_VIDEO_MJPEG = 41;
	public static final int RAW_VIDEO_H265 = 23;

	public static final int RAW_AUDIO_G711_A = 0x01;
	public static final int RAW_AUDIO_G711_U = 0x02;
	public static final int RAW_AUDIO_ADPCM_A = 0x03;
	public static final int RAW_AUDIO_AAC = 0x16;
	
	public static final int PROTOCOL_MOVE_UP = 1;		//move up        
	public static final int PROTOCOL_MOVE_DOWN = 2;		//move down      
	public static final int PROTOCOL_MOVE_LEFT = 3;		//move left      
	public static final int PROTOCOL_MOVE_RIGHT = 4;	//move right
	public static final int PROTOCOL_MOVE_STOP = 9;		//stop move 
	public static final int SET_HOR_AUTO_BEGIN = 23;	//set  horizontal auto start
	public static final int SET_HOR_AUTO_END = 24;		//set  horizontal auto end
	
	//Capture type
	public static final int CAPTURE_PICTURE_TYPE_YUV = 0;
	public static final int CAPTURE_PICTURE_TYPE_BMP = 1;
	public static final int CAPTURE_PICTURE_TYPE_JPG = 2;
	public static final int CAPTURE_PICTURE_TYPE_FEC_BMP = 3;
	public static final int CAPTURE_PICTURE_TYPE_FEC_JPG = 4; 
	
	public static final int NET_CLIENT_GET_FUNC_ABILITY = 99;	//get function ability
	
	public static final int COMMAND_ID_3D_POSITION = 57;
	
	public static final int MAIN_FUNC_TYPE_DOME_PARA = 0x08;	//dome para
	
	public static final int GENERAL_CMD_GET_CHANNEL_TYPE = 1;	//et the property of a channel of this device
	
	//Get the attribute parameters of the device channel
	public static final int CHANNEL_TYPE_LOCAL = 0; 		// local analog channel
	public static final int CHANNEL_TYPE_DIGITAL = 2; 		// Digital channel
	public static final int CHANNEL_TYPE_COMBINE = 3; 		// Synthesize the channel
	public static final int CHANNEL_TYPE_FISHEYE = 4; 		// fish eye channel
	public static final int CHANNEL_TYPE_FULLVIEW = 9999;	// full view channel
	
	public static final int NET_CLIENT_ELEVATOR_MONITOR		= 322; //Set/Get Elevator Monitor
	public static final int NET_CLIENT_ELEVATOR_STOREYINFO	= 323; //Set/Get Elevator Floor
	public static final int NET_CLIENT_ELEVATOR_STATE		= 324; //Get Elevator State
	public static final int NET_CLIENT_ELEVATOR_STATISTICS	= 325; //Set/Get Elevator Statistics
	public static final int MIN_LOOR_LEVING                 = -20;
	

	public static class RECT extends Structure implements ByValue {
		public int left;
		public int top;
		public int right;
		public int bottom;
	}

	public static class SDK_VERSION extends Structure {
		public short m_ulMajorVersion;
		public short m_ulMinorVersion;
		public short m_ulBuilder;
		public String m_cVerInfo;
	}
	
	public static class LogonPara extends Structure {
		public int		iSize;							//Structure size
		public byte[] 	cProxy = new byte[32];			//The ip address of the upper-level proxy to which the video is connected,not more than 32 characters, including '\0'
		public byte[] 	cNvsIP = new byte[32];			//IP address, not more than 32 characters, including '\0'
		public byte[]	cNvsName = new byte[32];		//Nvs name. Used for domain name resolution
		public byte[]	cUserName = new byte[16];		//Login Nvs username, not more than 16 characters, including '\0'
		public byte[]	cUserPwd = new byte[16];		//Login Nvs password, not more than 16 characters, including '\0'
		public byte[]	cProductID = new byte[32];		//Product ID, not more than 32 characters, including '\0'
		public int		iNvsPort;						//The communication port used by the Nvs server, if not specificed,Use the system default port(3000)
		public byte[]	cCharSet = new byte[32];		//Character set
		public byte[]	cAccontName = new byte[16];		//The username that connects to the contents server
		public byte[]	cAccontPasswd = new byte[16];	//The password that connects to the contents server
	};

	public static class CLIENTINFO extends Structure {
		public CLIENTINFO() {
			allocateMemory();
		}
		public int m_iServerID; 
		public int m_iChannelNo; 
		public byte[] m_cNetFile = new byte[255]; 
		public byte[] m_cRemoteIP = new byte[16]; 
		public int m_iNetMode;
		public int m_iTimeout; 
		public int m_iTTL;
		public int m_iBufferCount; 
		public int m_iDelayNum; 
		public int m_iDelayTime; 
		public int m_iStreamNO; 
		public int m_iFlag; 
		public int m_iPosition; 
		public int m_iSpeed; 
	}
	

	public static class ENCODERINFO extends Structure {
		public byte[] m_cHostName = new byte[32]; 
		public byte[] m_cEncoder = new byte[16]; 
		public int m_iRecvMode;
		public byte[] m_cProxy = new byte[16]; 
		public byte[] m_cFactoryID = new byte[32]; //ProductID
		public int m_iPort;//NVS port
		public int m_nvsType; //NVS type(NVS_T or NVS_S or DVR ...eg)
		public int m_iChanNum; 
		public int m_iLogonState; 
		public int m_iServerType; 
	}
	
	public static class PicTime extends Structure {

		public int uiYear;
		public int uiMonth;
		public int uiDay;
		public int uiWeek;
		public int uiHour;
		public int uiMinute;
		public int uiSecondsr;
		public int uiMilliseconds;
	}
	
	public static class PicData extends Structure {

		public PicTime tPicTime;
		public int iDataLen;
		public Pointer pcPicData;
	}
	
	public static class VcaPicStream extends Structure {
		
		public int iStructLen;			
		public int iWidth;				
		public int iHeight;			
		public int iChannelID;			
		public int iEventType;			
		public int iRuleID;			
		public int iTargetID;			
		public int iTargetType;		
		public int iTargetSpeed;		
		public int iTargetDirection;
		public RECT tTargetPosition;	
		public int iPresetNo; 
		public byte[] m_cRemoteIP = new byte[16]; 
		public int iPicCount; 
		public PicData[] tPicData = new PicData[3]; 
	}

	public static class FaceAttribute extends Structure {
		public int			iType;			
		public int			iValue;		
	}
	
	public static class FacePicData extends Structure {

		public int			iFaceId;			
		public int			iDrop;	
		public int			iFaceLevel;	
		public RECT			tFaceRect;	
		public int			iWidth;				
		public int			iHeight;	
		public int			iFaceAttrCount;			//Number of face attributes
		public int			iFaceAttrStructSize;	//The size of strcut FaceAttribute
		public Pointer[]	ptFaceAttr = new Pointer[256];		//Face attributes,supports up to 256 attribute types,the subscript is the face attribute type://0-age,1-gender,2-masks,3-beard,4-eye open,5-mouth,6-glasses,7-race,8-emotion,9-smile,10-value......									
		public int			iDataLen;			
		public Pointer		pcPicData;			
	}
	
	public static class FacePicStream extends Structure {
		
		public int iStructLen;	
		public int iSizeOfFull;		//The size of strcut PicData
		public Pointer tFullData;				
		public int iFaceCount;	
		public int iSizeOfFace;		//The size of strcut FacePicData
		public Pointer[] tPicData = new Pointer[32]; 
	}
	
    public static class PICSTREAM_INFO extends Structure {

        public byte[] RecvBuffer = new byte[200*1024];//Here should be no less than the maximum alarm message length
    }
	

	public static interface MAIN_NOTIFY extends Callback {
		void MainNotify(int _iLogonID, int _iwParam, Pointer _ilParam,
				Pointer _pUserData);
	}

	public static interface ALARM_NOTIFY extends Callback {
		void AlarmNotify(int _iLogonID, int _iChannel,
				int _iAlarmState, int _iAlarmType, Pointer _pUserData);
	}

	public static interface PARACHANGE_NOTIFY extends Callback {
		void ParaChangeNotify(int _iLogonID, int _iChannel, int _iParaType,
				Pointer _strPara, Pointer _pUserData);
	}
	
	public static interface COMRECV_NOTIFY extends Callback {
		void ComRecvNotify(int _iLogonID, Pointer _pData, int _iLen,
				int _iComNo, Pointer _pUserData);
	}
	
	public static interface PROXY_NOTIFY extends Callback {
		void ProxyNotify(int _iLogonID, int _iCmdKey, Pointer _pData,
				int _iLen, Pointer _pUserData);
	}

	public static interface RECVDATA_NOTIFY extends StdCallCallback {
		void RecvDataNotify(int _ulID, Pointer _ucData, int _iLen, int _iFlag,
				Pointer _lpUserData);
	}
	
	public static interface NVSDATA_NOTIFY extends Callback {
		void NvsDataNotify(int _uiID, Pointer _pucData, int _iLen, Pointer _iUser);
	}
	
	public static interface NET_PICSTREAM_NOTIFY extends StdCallCallback {
		int PicDataNotify(int _ulID, int _lCommand, Pointer _tInfo, int _iLen,
				Pointer _lpUserData);
	}
	
	public static class NetPicPara extends Structure {
		public int 					iStructLen;				//Structure length
		public int						iChannelNo;
		public NET_PICSTREAM_NOTIFY	cbkPicStreamNotify;
		public Pointer					pvUser;
	}
	
	public static class PointerSize extends Structure {
		public Pointer					pPointer;
	}
	
	public static class NVS_FILE_TIME extends Structure implements ByValue {
		public short 		iYear;   				// Year
		public short 		iMonth;  				// Month
		public short 		iDay;    				// Day
		public short 		iHour;   				// Hour
		public short 		iMinute; 				// Minute
		public short 		iSecond; 				// Second
	}
	
	public static class QueryFileChannel extends Structure{
		public int 		iChannelNo;
		public int 		iStreamNo;
	}
	
	public static class ArrayQueryFileChannel extends Structure {
		public QueryFileChannel[] tArry = new QueryFileChannel[2];
	}
	
	public static class NETFILE_QUERY_V5 extends Structure {
		public int				iBufSize;						//Size of the structure
		public int     	    	iQueryChannelNo; 				//query channel no, 0x7FFFFFFF means query all channel
		public int				iStreamNo;						//stream no
		public int          	iType; 							//Video type 33:ATM
		public NVS_FILE_TIME	tStartTime; 					//Start time
		public NVS_FILE_TIME	tStopTime; 						//End time
		public int     	    	iPageSize;						//Page size
		public int         		iPageNo;						//Page number
		public int             	iFiletype;						//File type 0:all,1:Video,2:picture
		public int				iDevType;						//Device type 0:Video camera,1:Network video server,2:Web camera ,0xff: all
		public byte[]			cOtherQuery = new byte[65];		//Character overlay
		public int				iTriggerType;					//Alarm type 3:Port alarm,4:Mobile alarm ,5:Video loss alarm ,0x7FFFFFFF:invalid
		public int				iTrigger;						//Port(channel)number
		public int				iQueryType;						//Query type 0: Basic query 1:ATM query 2: ITS query					
		public int				iQueryCondition;				//Query criteria 0: Domain query  1: According to the card number query ; 2: Traffic query condition: 
		public byte[]			cField = new byte[5 * 68];		//Query message
		public int				iQueryChannelCount;				//if iQueryChannelCount = 0, query single channel with iQueryChannelNo
		public int				iBufferSize;					//sizeof(QueryFileChannel)
		public Pointer			ptChannelList;					//buffer len = sizeof(QueryFileChannel)*iQueryChannelCount
		public byte[]			cLaneNo = new byte[65];			//lane no
		public byte[]			cVehicleType = new byte[65];	//vehicle type
		public int				iFileAttr;						//File attributes:0: nvr local storage; 10000: ipc storage
	}
	
	//Record File Property
	public static class NVS_FILE_DATA extends Structure {
		public int             	iType;          				//Record type 1-Manual record, 2-Schedule record, 3-Alarm record
	    public int             	iChannel; 						//Record channel 0~channel defined channel number
	    public byte[]			cFileName = new byte[250]; 		//File name
	    public NVS_FILE_TIME    tStartTime;  					//File start time
	    public NVS_FILE_TIME   	tStopTime;   					//File end time
	    public int             	iFileSize;      				//File size
	};

	public static class NVS_FILE_DATA_EX extends Structure {
		public int				iSize;
		public NVS_FILE_DATA	tFileData;					//file basic information
		public int			    iLocked;					//add unlock state
		public int				iFileAttr;					//File attributes:0: nvr local storage; 10000: ipc storage
	};
	
	public static class DOWNLOAD_FILE extends Structure {
		public int				m_iSize;							//Structure size
		public byte[]   		m_cRemoteFilename = new byte[255];	//Fornt end video file name
		public byte[]			m_cLocalFilename = new byte[255];	//Local video file name
		public int				m_iPosition;						//File location by percentage 0~100;When continue send after stop send,file pointer offset request 
		public int				m_iSpeed;							//1, 2, 4, 8, Control file play speed, 0-Suspend
		public int				m_iIFrame;							//Only send I frame 1,Only play I Frame;0, All play					
		public int				m_iReqMode;							//Require data mode 1,Frame mode;0,Stream mode					
		public int				m_iRemoteFileLen;					//If local file is not null, the parameter set to null
		public int				m_iVodTransEnable;					//Enable
		public int				m_iVodTransVideoSize;				//Video pixel
		public int				m_iVodTransFrameRate;   			//Frame rate
		public int				m_iVodTransStreamRate;  			//Code rate
		public int				m_iSaveFileType;					//0:SDV	3:ps
		public int				m_iFileAttr;						//File attributes:0: nvr local storage; 10000: ipc storage
		public int				m_iCryptType;						//iCryptType = 0, no encryption; iCryptType = 1, is AES encryption
		public byte[]			m_cCryptKey = new byte[32];
	};
	
	public static class DOWNLOAD_TIMESPAN extends Structure {
		public int				m_iSize;							//Structure size
		public byte[]			m_cLocalFilename = new byte[255];	//Local video file name
		public int				m_iChannelNO;						//Channel number
		public NVS_FILE_TIME	m_tTimeBegin;						//Start time
		public NVS_FILE_TIME	m_tTimeEnd;							//End time
		public int				m_iPosition;						//Position according to time point,>100
		public int				m_iSpeed;							//1, 2, 4, 8, Control file playback speed, 0-Suspend
		public int				m_iIFrame;							//Only I frames  1,I only play; 0,Full play
		public int				m_iReqMode;							//Required data model 1,Frame mode;0,Flow pattern
		public int				m_iVodTransEnable;					//Enable
		public int				m_iVodTransVideoSize;				//Video frequency ratio
		public int				m_iVodTransFrameRate;   			//Frame rate
		public int				m_iVodTransStreamRate;  			//Code Rate
		public int				m_iFileFlag;						//0:Download multiple files  1:Download into a single file
		public int				m_iSaveFileType;					//0:SDV	3:ps		
		public int				m_iStreamNo;						//stream number,0-main stream, 1-sub stream
		public int				m_iFileAttr;						//File attributes:0: nvr local storage; 10000: ipc storage
		public int				m_iCryptType;						//iCryptType = 0, no encryption; iCryptType = 1, is AES encryption
		public byte[]			m_cCryptKey = new byte[32];
	};
	
	public static class DOWNLOAD_CONTROL extends Structure {
		public int			m_iSize;				//Structure size
		public int			m_iPosition;			//0~100, Location file playback ; -1, Does not carry on the localization
		public int			m_iSpeed;				//1, 2, 4, 8, Control file playback speed, 0-Suspend
		public int			m_iIFrame;				//Only I frames 1,I only play;0,Full play
		public int			m_iReqMode;				//Demand data model 1,Frame mode ;0,Flow pattern
	};
	
	public static class RAWFRAME_INFO extends Structure {
		public int 			nWidth;    			//Video width, audio data is 0
		public int 			nHeight;   			//Video height, audio data is 0
		public int 			nStamp;    			//Time stamp(ms)
		public int 			nType;     			//RAWFRAMETYPE, I Frame:0,P Frame:1,B Frame:2,Audio:5
		public int 			nEnCoder;  			//Audio or Video encoder(Video,0:H263,1:H264, 2:MP4. Audio:0,G711_A:0x01,G711_U:0x02,ADPCM_A:0x03,G726:0x04)
		public int 			nFrameRate;			//Frame rate
		public int 			nAbsStamp; 			//Absolute Time(s)
		public byte 		ucBitsPerSample;	// bit per sample [8/16/24] default 16
		public int 			uiSamplesPerSec;	// Samples Per Sec, default 8000
	};
	
	//Not decode the standard data before the pure h264 data
	public static interface RAWFRAME_NOTIFY extends StdCallCallback {
		void rawFrameNotify(int _uiID, Pointer _pcData, int _iLen, RAWFRAME_INFO _ptRawFrameInfo, Pointer _pvUsrData);
	}
	
	public static class FRAME_INFO extends Structure {
		public int		nWidth;				//Video width, audio data is 0; 
		public int		nHeight;			//Video height, audio data is 0; 
		public int		nStamp;				//Time stamp(ms).
		public int		nType;				//Audio type: 0--T_AUDIO8, 1-T_YUV420
		public int		nFrameRate;			//Frame rate
		Pointer			nReserved;			//reserve
	};
	public static interface DECYUV_NOTIFY_V4 extends Callback {
		void decYuvNotify(int _uiID, Pointer _pcData, int _iLen, FRAME_INFO _pFrameInfo, Pointer _pvUser);
	}
	
	public static class COMFORMAT extends Structure {
		public int		iSize;				//Size of the structure,must be initialized before used
		public int		iComNo;				//Serial number
		public byte[] 	cDeviceName = new byte[32];	//Protocol name
		public byte[] 	cComFormat = new byte[32];	//Format serial 9600,n,8,1
		public int		iWorkMode;			//Working mode 1:Protocol control,2:Transparent channel,3:Industry reserve ,4:7601B Serial alarm host,5:485 kerboard
		public int		iComType;			//Serial type 0:485,1:232,2:422
	}
	
	public static class FuncAbilityLevel extends Structure {
		public int		iSize;
		public int		iMainFuncType;				//main function type
		public int		iSubFuncType;				//sub function type
		public byte[]  cParam = new byte[1024];	//Capability Description
	}
	

	//Elevator business
	public static class ElevatorMonitor extends Structure 
	{
		public ElevatorMonitor() {
			allocateMemory();
		}
		public int					iChannelNo;			//channel number reserved
		public int					iStartShockThreshold;//Start shock threshold Unit: mgal Range: 0-100000
		public int					iMoveSpeed;			//Normal running speed Unit: mm/s Range: 0-3500
		public int					iBodyInductionMode;	//person detection mode 0-video detection 1-PIR 2-automatic
		public int					iEbikeDetectEnable;	//Electric vehicle entering elevator alarm 0-disable 1-enable
		public int					iSwaySensitivity;	//Shake alarm sensitivity range 0-100
		public int					iTopLimit;			//The highest floor
		public int					iBottomLimit;		//The lowest floor
		public int					iMainFloor;			//Base station layer
		public int					iLevelingMode;		//leveling signal valid type 0-low valid 1-high valid 2-upper edge (reserved) 3-lower edge (reserved)
		public int					iOpenDoorMode;		//Valid type of door switch signal 0-low valid 1-high valid 2-upper edge (reserved) 3-lower edge (reserved)
		public int					iMaintenanceMode;	//check valid type 0-low valid 1-high valid 2-upper edge (reserved) 3-lower edge (reserved)
		public int					iCrashStopMode;		//Emergency stop valid type 0-low valid 1-high valid 2-upper edge (reserved) 3-lower edge (reserved)
		public int					iPIRMode;			//PIR signal valid type 0-low valid 1-high valid 2-upper edge (reserved) 3-lower edge (reserved)
	};


	public static class  ElevatorStoreyInfo extends Structure 
	{
		public ElevatorStoreyInfo() {
			allocateMemory();
		}
		int					        iChannelNo;					//channel number public int
		public int					iStartFloor;				//Start floor
		public int					iEndFloor;					//End floor
		public int [] iFloorHeight = new int[256];		//floor height
	};


	public static class  ElevatorState extends Structure 
	{
		public ElevatorState() {
			allocateMemory();
		}
		public int		iChannelNo;		//channel number reserved
		public int		iFloor;			//floor code current floor
		public int		iDirection ;	//Direction Direction: 0-no direction 1-upward 2-downward
		public int		iSpeed;			//Running speed Current running speed
		public int		iTemperature;	//Temperature temperature value (unit: Celsius*10)
		public int		iHumidity;		//humidity relative humidity (unit: %*10)
		public int		iBindBrake;		//brake state 0-brake closed 1-brake open
		public int		iMaintenance;	//maintenance status 0-maintenance status 1-non-maintenance status
		public int		iLeveling;		//leveling status 0-no longer leveling 1-leveling
		public int		iOpenDoor;		//car door status 0-car door closed 1-car door open
		public int		iCrashStop;		//Emergency stop status 0-normal 1-emergency stop
		public int		iBodyInduction;	//person detection status 0-no one 1-someone
		public int		iMainFloor;		//Base station status 0-no longer base station 1-in base station

	};

	public static class  ElevatorStatistics extends Structure 
	{
		public ElevatorStatistics() {
			allocateMemory();
		}
		public int			iChannelNo;			//channel number reserved
		public int			iBindBrakeCn;		// Brake times Accumulated statistics of brake times
		public int			iOpenDoorCn;		//The times of opening and closing the door, the cumulative number of times of opening and closing the door
		public int			iOperationMileage;	//Running distance Cumulative statistical running distance Unit mm
	};
	
	public static class vca_TPoint extends Structure {
		public int	iX;
		public int	iY;
	};
	
	public static final int MAX_3D_LOCATE_POINT_NUM	= 2;
	public static class Locate3DPosition extends Structure {
		public int			iBufSize;				
		public int			iPointNum;								
		public vca_TPoint[] tPoint = new vca_TPoint[MAX_3D_LOCATE_POINT_NUM];
	};
	
	int NetClient_GetVersion(SDK_VERSION _ver);

	int NetClient_SetNotifyFunction_V4(MAIN_NOTIFY _cbkMainNotify,
			ALARM_NOTIFY _cbkAlarmNotify,
			PARACHANGE_NOTIFY _cbkParaChangeNotify,
			COMRECV_NOTIFY _cbkComRecv,
			PROXY_NOTIFY _cbkProxyNotify);

	int NetClient_SetPort(int _iServerPort, int _iClientPort);

	int NetClient_Startup_V4(int _iServerPort, int _iClientPort, int _iWnd);

	int NetClient_Cleanup();

	int NetClient_Logon(String _cProxy, String _cIP, String _cUserName,
			String _cPassword, String _pcProID, int _iPort);
	
	int NetClient_Logon_V4(int _iLogonType, Pointer _pvPara, int _iInBufSize);

	int NetClient_Logoff(int _iLogonID);

	int NetClient_StartRecvEx(IntByReference _ulConID, CLIENTINFO _cltInfo,
			RECVDATA_NOTIFY _cbkDataNotify, Pointer _pUserData);
	
	int NetClient_StartRecv_V4(IntByReference _uiRecvID, CLIENTINFO _cltInfo
			, NVSDATA_NOTIFY _cbkDataArrive, Pointer _iUserData);
	
	int NetClient_StartRecvNetPicStream(int _iLogonID, NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID);

	int NetClient_StopRecv(int _uiConID);

	int NetClient_StartCaptureData(int _uiConID);
	
	int NetClient_StopCaptureData(int _uiID);

	int NetClient_StartPlay(int _ulConID, int _hWnd, RECT _rcShow, int _iDecflag);

	int NetClient_StopPlay(int _ulConID);

	int NetClient_StartCaptureFile(int _uiConID, String _cFileName, int _iRecFileType);

	int NetClient_StopCaptureFile(int _uiConID);
	
	int NetClient_GetLogonStatus(int _iLogonID);
	
	int NetClient_GetDevInfo(int _iLogonID,ENCODERINFO _pEncoderInfo);
	
	int NetClient_Query_V4(int _iLogonID, int _iCmd, int _iChannel, Pointer _pvCmdBuf, int _iBufLen);
	
	int NetClient_NetFileGetFileCount(int _iLogonID, IntByReference _piTotalCount, IntByReference _piCurrentCount);
	
	int NetClient_NetFileGetQueryfileEx(int _iLogonID,int _iFileIndex, NVS_FILE_DATA_EX _pFileInfo);
	
	int NetClient_NetFileDownload(IntByReference _uiConID, int _iLogonID, int _iCmd, Pointer _pvBuf, int _iBufSize);
	
	int NetClient_NetFileStopDownloadFile(int _uiConID);
	
	int NetClient_NetFileGetDownloadPos(int _uiConID, IntByReference _piPos, IntByReference _piDLSize);
	
	int NetClient_SetRawFrameCallBack(int _uiConID, RAWFRAME_NOTIFY _cbkGetFrame, Pointer _pContext);
	
	int NetClient_SetDecCallBack_V4(int _uiConID, DECYUV_NOTIFY_V4 _cbkDecYUV, Pointer _pvUserData);
	
	int NetClient_GetDeviceType(int _iLogonID, int _iChannelNo, IntByReference _piComPort
			, IntByReference _iDevAddress, Pointer _cDeviceType);
	
	int NetClient_GetComFormat_V2(int _iLogonID, COMFORMAT _ptComFormat);
	
	int NetClient_SetComFormat_V2(int _iLogonID, COMFORMAT _ptComFormat);
	
	int NetClient_DeviceCtrlEx(int _iLogonID, int _iChannelNo, int _iActionType, int _iParam1, int _iParam2, int _iControlType);
	
	int NetClient_CapturePicture(int _uiConID, int _iPicType, String strFileName);
	
	int NetClient_CapturePic(int _uiConID, PointerByReference _pucData);
	
	int NetClient_GetSensorFlip(int _iLogonID, int _iChannel, IntByReference _piFlip);
	
	int NetClient_GetChannelProperty(int _iLogonID, int _iChannel, int _iCmd, Pointer _pvBuf, int _iBufSize);

	int NetClient_DigitalChannelSend(int _iLogonID, int _iChannel, byte[] _ucBuf, int _iLength);
	
	int NetClient_ComSend(int _iLogonID, byte[] _ucBuf, int _iLength, int _iComNo);
	
	int NetClient_SendCommand(int _iLogonID, int  _iCommand, int _iChannel, Pointer _pvBuffer, int _iBufSize);
	
	int NetClient_GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _pvOutBuffer
			, int _iOutBufferSize, IntByReference _piBytesReturned);
	
	int NetClient_SetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _lpInBuffer, int _iInBufferSize);
}

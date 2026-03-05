package src;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.ByValue;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.Callback;

public interface NVSSDK extends Library {
	
	//Synchronous blocking interface return value
	public static final int RET_SUCCESS = 0;	//success
	public static final int RET_FAILED = -1;	//fail
	public static final int RET_SYNCLOGON_TIMEOUT				= -300;	//Synchronous login times out, the network is normal, but the peer does not respond
	public static final int RET_SYNCLOGON_USENAME_ERROR			= -301;	//Synchronous login failed, username does not exist, default username: admin
	public static final int RET_SYNCLOGON_USRPWD_ERROR			= -302;	//Synchronous login failed, the password is wrong, the default password: 1111, if the user has changed the password, please enter the correct password.
	public static final int RET_SYNCLOGON_PWDERRTIMES_OVERRUN	= -303;	//Synchronous login failed, the number of incorrect passwords exceeded the limit, and the account was locked
	public static final int RET_SYNCLOGON_NET_ERROR				= -304;	//Synchronous login failed, network connection error
	public static final int RET_SYNCLOGON_PORT_ERROR			= -305;	//Synchronous login failed, the communication port input is incorrect, the default incoming port is 3000
	public static final int RET_SYNCLOGON_UNKNOW_ERROR			= -306;	//Synchronous login failed with unknown error
	public static final int RET_SYNCREALPLAY_TIMEOUT			= -307;	//The synchronous connection video timed out, and the peer did not send the video header protocol
	public static final int RET_SYNCREALPLAY_FAIL				= -308;	//Failed to play video when sync connected video
	public static final int RET_SYNCSUSPENDVCA_CONFIGING		= -309;	//Synchronous pause intelligent analysis failed, intelligent analysis parameters are being set by other clients
	public static final int RET_SYNCSUSPENDVCA_FAIL				= -310;	//Synchronization pause intelligent analysis failed, the device did not return the code
	public static final int RET_SYNCOPENVCA_CONFIGING			= -311;	//Failed to enable intelligent analysis synchronously, intelligent analysis parameters are being set by other clients
	public static final int RET_SYNCOPENVCA_FAIL				= -312;	//Failed to enable intelligent analysis synchronously, the device did not return the code
	public static final int RET_SYNCOPTVCA_TIMEOUE				= -313;	//Sync Pause/On Smart Analysis Timeout
	
	public static final int SYNC_NET_CLIENT_VCA_SUSPEND = 0;
	
	public static final int SERVER_NORMAL = 0;
	public static final int SERVER_ACTIVE = 1;
	
	public static final int DSM_CMD_SET_NET_WAN_INFO = 0;		//[ActiveNetWanInfo]local public network ip and port
	public static final int DSM_CMD_SET_DIRECTORY_INFO = 1;		//[ActiveDirectoryInfo]directory public network ip, port,account,password....
	public static final int DSM_CMD_SET_NVSREG_CALLBACK = 2;	
	
	public static final int DSM_CMD_GET_ONLINE_STATE = 0;
	public static final int DSM_CMD_GET_DEVICE_INFO = 0;
	public static final int DSM_CMD_GET_REGISTER_COUNT = 1;
	public static final int DSM_CMD_GET_REGISTER_DEVLIST = 2;
	public static final int DSM_CMD_GET_DEVCOUNT_WITHREG = 3; 
	public static final int DSM_CMD_GET_DEVLIST_WITHREG = 4;
	
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
	public static final int ALARM_VCA_INFO = 6; // Intelligent analysis of alarm information

	public static final int T_AUDIO8 = 0;
	public static final int T_YUV420 = 1;
	public static final int T_YUV422 = 2;
	
	public static final int NET_PICSTREAM_CMD_VCA = 1;	//Callback VCA image stream information
	public static final int NET_PICSTREAM_CMD_ITS = 2;	//Callback ITS image stream information
	public static final int NET_PICSTREAM_CMD_FACE = 3;	//Callback face image stream information
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
	public static final int CMD_NETFILE_QUERY_LOG = 8;
	
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

        public byte[] RecvBuffer = new byte[200*1024];//400 here should not be less than the maximum alarm message length
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
		public int[]			iQueryTypeValue = new int[6];
		public int				iCurQueryCount;								//output para, synchronized blocking interface use, return current query count
		public int				iTotalQueryCount;							//output para, synchronized blocking interface use, return total query count
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
	
	public static class QueryFileResult extends Structure {
		public NVS_FILE_DATA[] tArry = new NVS_FILE_DATA[20];
	}

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
	
	//Network to receive the original data, not a complete data frame can be used for proxy forwarding
	public static interface NVSDATA_NOTIFY extends Callback {
		void nvsDataNotify(int _iID, Pointer _pcData, int _iLen, Pointer _pvUser);
	}
	
	//Complete a frame of data, can be used to write video files, or send player to play. You want to distinguish the type of _ulStreamType
	public static interface FULLFRAME_NOTIFY_V4 extends StdCallCallback {
		void fullFrameNotifyV4(int _iID, int _iStreamType, Pointer _pcData, int _iLen, Pointer _pvHeader, Pointer _pvUserData);
	}
	
	public static class NetClientPara extends Structure {
		public int						iSize;
		public CLIENTINFO				tCltInfo;
		public int						iCryptType;			//iCryptType = 0, no encryption; iCryptType = 1, is AES encryption
		public byte[]					cCryptKey = new byte[32];
		public NVSDATA_NOTIFY			cbkDataArrive;		//Network to receive the original data,
		public Pointer					pvUserData;
		public int						iPicType;			//Client request picture stream type.Structured proprietary(This field is not sent or sent 0: indicates that the server is adaptively sent and sent based on the current configuration.)
															//bit0: Face picture stream 
															//bit1: Traffic picture stream
		public FULLFRAME_NOTIFY_V4		pCbkFullFrm;		//full frame callback, it's private frame 
		public Pointer					pvCbkFullFrmUsrData;
		public RAWFRAME_NOTIFY			pCbkRawFrm;			//raw frame callback
		public Pointer					pvCbkRawFrmUsrData;
		public int						iIsForbidDecode;	//Synchronized blocking interface extension para: 0----allow decode, 1----forbid decode
		public Pointer					pvWnd;				//Synchronized blocking interface extension para: the window handle of show video, if NULL can not show video
	};
	
	public static class NVS_LOG_QUERY extends Structure {
		public int				iChannelNo;		//channel number
		public int				iLogType;		//Log type: 0, syslog, 1, warning, 2, alarm, 3, action, 4, user, 5, other, 0xFF, all types
		public int				iLanguage;		//language type: 0, English; 1, GB2312 simplified; 2, BIG5 traditional ; 3, Korean; 4, Spain; 5, Italian,
		public NVS_FILE_TIME 	tStartTime; 	//start time
		public NVS_FILE_TIME 	tStopTime; 		//End time
		public int				iPageSize;		//page size
		public int				iPageNo;		//page number
	};

	public static class NVS_LOG_DATA extends Structure { 
		public int				iChannel;
		public int				iLogType;
		public NVS_FILE_TIME	tStartTime; 	/* File start time */
		public NVS_FILE_TIME	tStopTime; 	/* File end time */
		public byte[]			szLogContent = new byte[129];					
	};
	
	public static class QueryLogResult extends Structure {
		public NVS_LOG_DATA[] tArry = new NVS_LOG_DATA[20];
	}
	
	public static final int VCA_OPT_SUSPEND = 0;
	public static final int VCA_OPT_OPENVCA = 1;
	public static final int VCA_OPT_RESULT_SUCCESS = 1; 
	public static final int VCA_OPT_RESULT_CONFIGING = 2;
	
	public static class VCASuspend extends Structure { 
		public int		iStatus;				//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		public int		iDevType;				//0-IPC, 1-NVR
	};
	
	public static class VCASuspendResult extends Structure { 
		public int		iBufSize;				//structure size
		public int		iStatus;				//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		public int		iResult;				//Result(1:success[indicate that parameters can be set] 2:fail[indicate that parameter is being set and can not set])
		public int		iDevType;				//0-IPC, 1-NVR
	};
	
	public static class ActiveNetWanInfo extends Structure {
		int				iSize;
		public byte[]	cWanIP = new byte[32];		//Local public network ip
		int				iWanPort;							//Local public network port
	};
	
	public static final int DSM_STATE_OFFLINE = 0;
	public static final int DSM_STATE_ONLINE = 1;
	public static class DsmOnline extends Structure { 
		public int			iSize;
		public byte[]		cProductID = new byte[32];	//product id	
		public int			iOnline;					//0--Offline 1--Online
		public byte[]		cWanIP = new byte[32];
		public byte[]		cLanIP = new byte[32];
	};
	
	public static class LogonActiveServer extends Structure {
		public int		iSize;					
		public byte[]	cUserName = new byte[16];		
		public byte[]	cUserPwd = new byte[16];		
		public byte[]	cProductID = new byte[32];	//product id	
	};
	
	public static class LogonPara extends Structure {
		public int				iSize;							//Structure size
		public byte[]	cProxy = new byte[32];			//The ip address of the upper-level proxy to which the video is connected,not more than 32 characters, including '\0'
		public byte[]	cNvsIP = new byte[32];			//IPV4 address, not more than 32 characters, including '\0'
		public byte[]	cNvsName = new byte[32];		//Nvs name. Used for domain name resolution
		public byte[]	cUserName = new byte[16];		//Login Nvs username, not more than 16 characters, including '\0'
		public byte[]	cUserPwd = new byte[16];		//Login Nvs password, not more than 16 characters, including '\0'
		public byte[]	cProductID = new byte[32];		//Product ID, not more than 32 characters, including '\0'
		public int				iNvsPort;						//The communication port used by the Nvs server, if not specificed,Use the system default port(3000)
		public byte[]	cCharSet = new byte[32];		//Character set
		public byte[]	cAccontName = new byte[16];		//The username that connects to the contents server
		public byte[]	cAccontPasswd = new byte[16];	//The password that connects to the contents server
		public byte[]	cNvsIPV6 = new byte[64];		//IPV6 address
	};
	
	public static class LocalSDKPath extends Structure {
		public int		iSize;	
		public int		iType;			
		public byte[]	cPath = new byte[256];		
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
	int NetClient_SetDsmConfig(int _iCommand, Pointer _pvBuf, int _iBufSize);
	int NetClient_GetDsmRegstierInfo(int _iCommand, Pointer _pvBuf, int _iBufSize);
	int NetClient_Logon(String _cProxy, String _cIP, String _cUserName, String _cPassword, String _pcProID, int _iPort);
	int	NetClient_SyncLogon(int _iLogonType, Pointer _pvPara, int _iParaSize);
	int NetClient_Logoff(int _iLogonID);
	int NetClient_GetChannelNum(int _iLogonID, IntByReference _piChanNum);
	int NetClient_GetDigitalChannelNum(int _iLogonID, IntByReference _piDigitChannelNum);
	int NetClient_StartRecvEx(IntByReference _ulConID, CLIENTINFO _cltInfo, RECVDATA_NOTIFY _cbkDataNotify, Pointer _pUserData);
	int	NetClient_SyncRealPlay(IntByReference _puiRecvID, NetClientPara _ptPara, int _iParaSize);
	int	NetClient_StopRealPlay(int _uiRecvID, int _iParam);
	int NetClient_StartRecvNetPicStream(int _iLogonID, NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID);
	int NetClient_StopRecv(int _ulConID);
	int NetClient_StartCaptureData(int _ulConID);
	int NetClient_StartPlay(int _ulConID, int _hWnd, RECT _rcShow, int _iDecflag);
	int NetClient_StopPlay(int _ulConID);
	int NetClient_StartCaptureFile(int _ulConID, String _cFileName, int _iRecFileType);
	int NetClient_StopCaptureFile(int _ulConID);
	int NetClient_GetLogonStatus(int _iLogonID);
	int NetClient_GetDevInfo(int _iLogonID,ENCODERINFO _pEncoderInfo);
	int NetClient_Query_V4(int _iLogonID, int _iCmd, int _iChannel, Pointer _pvCmdBuf, int _iBufLen);
	int NetClient_NetFileGetFileCount(int _iLogonID, IntByReference _piTotalCount, IntByReference _piCurrentCount);
	int NetClient_NetFileGetQueryfileEx(int _iLogonID,int _iFileIndex, NVS_FILE_DATA_EX _pFileInfo);
	int NetClient_NetFileDownload(IntByReference _uiConID, int _iLogonID, int _iCmd, Pointer _pvBuf, int _iBufSize);
	int NetClient_NetFileStopDownloadFile(int _uiConID);
	int NetClient_NetFileGetDownloadPos(int _uiConID, IntByReference _piPos, IntByReference _piDLSize);
	int NetClient_SetRawFrameCallBack(int _uiConID, RAWFRAME_NOTIFY _cbkGetFrame, Pointer _pContext);
	int	NetClient_SyncQuery(int _iLogonID, int _iChanNo, int _iCmd, Pointer _pvInPara, int _iInLen, Pointer _pvOutPara, int _iOutTotalLen, int _iSingleLen);
	int NetClient_NetLogGetLogCount(int _iLogonID, IntByReference _iTotalCount, IntByReference _iCurrentCount);
	int	NetClient_SyncSetDevCfg(int _iLogonID, int _iChanNo, int _iCmd, Pointer _pvInPara, int _iInLen, Pointer _pvOutRet, int _iOutLen);
	int	NetClient_SetSDKInitConfig(int _iCmd, Pointer _lpInBuffer, int _iInLen);
}

package src;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.ByValue;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.Callback;

public interface NVSSDK extends Library {
	
	public static final int WM_USER = 0x0400; //

	public static final int WM_MAIN_MESSAGE = WM_USER + 1001; // system information
	public static final int WM_PARACHG = WM_USER + 1002; // parameter change message
	public static final int WM_ALARM = WM_USER + 1003; // alarm message
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
	public static final int DOWNLOAD_FILE_TYPE_TS = 6;
	
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

	int NetClient_Logoff(int _iLogonID);

	int NetClient_StartRecvEx(IntByReference _ulConID, CLIENTINFO _cltInfo,
			RECVDATA_NOTIFY _cbkDataNotify, Pointer _pUserData);
	
	int NetClient_StartRecvNetPicStream(int _iLogonID, NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID);

	int NetClient_StopRecv(int _ulConID);

	int NetClient_StartCaptureData(int _ulConID);

	int NetClient_StartPlay(int _ulConID, int _hWnd, RECT _rcShow, int _iDecflag);

	int NetClient_StopPlay(int _ulConID);

	int NetClient_StartCaptureFile(int _ulConID, String _cFileName,
			int _iRecFileType);

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
}

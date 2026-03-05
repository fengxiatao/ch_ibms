package src;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.Callback;
import com.sun.jna.Structure.ByValue;
import com.sun.jna.ptr.IntByReference;

public interface NVSSDK extends Library {
	
	public static final int WM_USER = 0x0400; //

	public static final int WM_MAIN_MESSAGE = WM_USER + 1001; // System messages
	public static final int WM_PARACHG = WM_USER + 1002; // Parameter change message
	public static final int WM_ALARM = WM_USER + 1003; // Alarm message
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
	public static final int WCM_VCA_SUSPEND = 103;	
	
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
	
	public static final int DSM_CMD_SET_NET_WAN_INFO = 0;		//[ActiveNetWanInfo]local public network ip and port
	public static final int DSM_CMD_SET_DIRECTORY_INFO = 1;		//[ActiveDirectoryInfo]directory public network ip, port,account,password....
	public static final int DSM_CMD_SET_NVSREG_CALLBACK = 2;	
	
	public static final int DSM_CMD_GET_ONLINE_STATE = 0;
	public static final int DSM_CMD_GET_DEVICE_INFO = 0;
	public static final int DSM_CMD_GET_REGISTER_COUNT = 1;
	public static final int DSM_CMD_GET_REGISTER_DEVLIST = 2;
	public static final int DSM_CMD_GET_DEVCOUNT_WITHREG = 3; 
	public static final int DSM_CMD_GET_DEVLIST_WITHREG = 4;
	
	public static final int SERVER_NORMAL = 0;
	public static final int SERVER_ACTIVE = 1;
	public static final int SERVER_DNS = 2;
	public static final int SERVER_FIND_PSW = 3;
	public static final int SERVER_REG_ACTIVE = 4;
	
	public static final int NET_CLIENT_MIN = 0;	
	public static final int NET_CLIENT_ANYSCENE = NET_CLIENT_MIN + 21;
	public static final int NET_CLIENT_FACE_DETECT_ARITHMETIC = NET_CLIENT_MIN + 26;
	public static final int NET_CLIENT_VCA_SUSPEND = NET_CLIENT_MIN + 32;
	public static final int NET_CLIENT_GET_FUNC_ABILITY = NET_CLIENT_MIN + 99;
	public static final int NET_CLIENT_FULL_LOGON = NET_CLIENT_MIN + 203;
	
	public static final int NET_CLIENT_TEMPERATURE_STANDARD = NET_CLIENT_MIN + 263;
	public static final int NET_CLIENT_BLACKBODY_CORRECT = NET_CLIENT_MIN + 264;
	public static final int NET_CLIENT_BODYTEMP_CORRECT = NET_CLIENT_MIN + 265;
	public static final int NET_CLIENT_INTELLIGENT_CORRECT = NET_CLIENT_MIN + 266;
	
	public static final int VCA_SUSPEND_STATUS_PAUSE = 0;		//Pause intelligent analysis
	public static final int VCA_SUSPEND_STATUS_RESUME = 1;	//Recovery intelligence analysis

	public static final int VCA_SUSPEND_RESULT_SUCCESS = 1;		//Intelligent analysis paused successfully
	public static final int VCA_SUSPEND_RESULT_CONFIGING = 2;		//Intelligent analysis pause failed, setting, can't set parameter
	
	//Face correlation
	public static final int FACE_MAX_PAGE_COUNT = 20;
	public static final int FACE_MAX_LIB_COUNT = 33;		//Maximum number of face databases
	
	public static final int FACE_CMD_MIN = 0;
	public static final int FACE_CMD_EDIT 					= (FACE_CMD_MIN + 0x00);
	public static final int FACE_CMD_DELETE 				= (FACE_CMD_MIN + 0x01);
	public static final int FACE_CMD_QUERY 					= (FACE_CMD_MIN + 0x02);
	public static final int FACE_CMD_MODEL 					= (FACE_CMD_MIN + 0x03);
	public static final int FACE_CMD_LIB_EDIT 				= (FACE_CMD_MIN + 0x04);
	public static final int FACE_CMD_LIB_DELETE 			= (FACE_CMD_MIN + 0x05);
	public static final int FACE_CMD_LIB_QUERY 				= (FACE_CMD_MIN + 0x06);
	public static final int FACE_CMD_FEATURE_QUERY 			= (FACE_CMD_MIN + 0x07);
	public static final int FACE_CMD_FEATURE_CALC 			= (FACE_CMD_MIN + 0x08);
	public static final int FACE_CMD_CUT 					= (FACE_CMD_MIN + 0x09);
	public static final int FACE_CMD_CUT_QUERY 				= (FACE_CMD_MIN + 0x0A);
	public static final int FACE_CMD_TASK_CREATE 			= (FACE_CMD_MIN + 0x0B);
	public static final int FACE_CMD_TASK_FREE 				= (FACE_CMD_MIN + 0x0C);
	public static final int FACE_CMD_LIB_IMPORT 			= (FACE_CMD_MIN + 0x0D);
	public static final int FACE_CMD_SEARCH 				= (FACE_CMD_MIN + 0x0E);
	public static final int FACE_CMD_LIB_SYNC_START 		= (FACE_CMD_MIN + 0x0F);
	public static final int FACE_CMD_LIB_SYNC_STATUS 		= (FACE_CMD_MIN + 0x10);
	public static final int FACE_CMD_CUT_EX 				= (FACE_CMD_MIN + 0x11);
	public static final int FACE_CMD_SEARCH_SNAP 			= (FACE_CMD_MIN + 0x12);
	public static final int FACE_CMD_SEARCH_SNAP_PROCESS 	= (FACE_CMD_MIN + 0x13);
	public static final int FACE_CMD_SEARCH_SNAP_RESULT 	= (FACE_CMD_MIN + 0x14);
	public static final int FACE_CMD_ALARM_PARAM 			= (FACE_CMD_MIN + 0x15);
	
	public static final int FACE_LIBSYNC_STATUS_START			= 20;
	public static final int FACE_LIBSYNC_STATUS_STOP			= 21;
	public static final int FACE_LIBSYNC_STATUS_DELETE			= 22;	//delete sync task
	public static final int FACE_LIBSYNC_STATUS_DELETE_IPCLIB	= 23;	//delete ipc lib
	
	public static final int NET_PICSTREAM_CMD_FACE = 3;
	public static final int CMD_ALARM_FACE_PARAM = 112;		//Set / get face alarm parameters
	
	//download file type
	public static final int DOWNLOAD_FILE_TYPE_SDV = 0;
	public static final int DOWNLOAD_FILE_TYPE_PS = 3;
	public static final int DOWNLOAD_FILE_TYPE_ZFMP4 = 4;
	
	
	public static final int DOWNLOAD_CMD_FILE = 0;
	public static final int DOWNLOAD_CMD_TIMESPAN = 1;
	public static final int DOWNLOAD_CMD_CONTROL = 2;
	public static final int DOWNLOAD_CMD_FILE_CONTINUE = 3;
	public static final int DOWNLOAD_CMD_GET_FILE_COUNT = 4;
	public static final int DOWNLOAD_CMD_GET_FILE_INFO = 5;
	public static final int DOWNLOAD_CMD_SET_FILE_INFO = 6;
	
	public static final int VCA_CMD_MIN = 100;
	public static final int VCA_CMD_PICSTREAM_UPLOADPARAM = (VCA_CMD_MIN + 47);
	public static final int VCA_CMD_TEMDETECT = (VCA_CMD_MIN + 75);
	
	public static final int CMD_NETFILE_QUERY_VCA = 7;
	
	public static final int CI_COMMON_ID_TEMDETECT = 0xB007;
	
	public static final int  TEM_DETECT_ENABLE = 1;		//Enable human body temperature measurement
	public static final int  TEM_DETECT_DISABLE = 2;       //Turn off human body temperature measurement enable
	
	public static interface MAIN_NOTIFY extends Callback {
		void MainNotify(int _iLogonID, int _iwParam, Pointer _ilParam, Pointer _pUserData);
	}
	
	public static interface ALARM_NOTIFY extends Callback {
		void AlarmNotify(int _iLogonID, int _iChannel,
				int _iAlarmState, int _iAlarmType, Pointer _pUserData);
	}
	
	public static interface PARACHANGE_NOTIFY extends Callback {
		void ParaChangeNotify(int _iLogonID, int _iChannel, int _iParaType,
				Pointer _strPara, Pointer _pUserData);
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
	
	public static class PointerSize extends Structure {
		public Pointer					pPointer;
	}

	public static class LogonPara extends Structure {
		public int						iSize;
		public byte[] cProxy = new byte[32];
		public byte[] cNvsIP = new byte[32];
		public byte[] cNvsName = new byte[32];
		public byte[] cUserName = new byte[16];
		public byte[] cUserPwd = new byte[16];
		public byte[] cProductID = new byte[32];
		public int						iNvsPort;
		public byte[] cCharSet = new byte[32];
		public byte[] cAccontName = new byte[16];
		public byte[] cAccontPasswd = new byte[16];
	}
	
	public static class DsmNvsRegInfo extends Structure {
		public int						iSize;
		public byte[] cFactoryID = new byte[32]; 	//Factory id, unique identification of equipment
		public byte[] cNvsIP = new byte[32]; 		//Device IP
		public byte[] cNvsName = new byte[32]; 		//Equipment name
		public byte[] cRegTime = new byte[32]; 		//Registration time to server
		public int						iChanNum;	//Number of device channels
	}
	
	public static interface DSM_NVS_REG_NOTIFY extends StdCallCallback {
		void DsmNvsRegNotify(Pointer _pInfo, int _iLen, Pointer _lpUserData);
	}
	
	public static class ActiveNvsNotify extends Structure {
		public int						iSize;
		public DSM_NVS_REG_NOTIFY		cbkNotify;
		public Pointer					pUser;
	}
	
	public static class SDK_VERSION extends Structure {
		public short					m_ulMajorVersion;
		public short 					m_ulMinorVersion;
		public short 					m_ulBuilder;
		public String 					m_cVerInfo;
	}
	
	//face related structures
	public static class FaceLibInfo extends Structure {
		public int 						iSize;
		public int 						iLibKey;
		public byte[] 					cName = new byte[64];
		public int 						iThreshold;
		public byte[] 					cExtrInfo = new byte[64];	
		public int 						iAlarmType;
		public int						iOptType;
		public byte[] 					cLibUUID = new byte[64];
		public byte[] 					cLibVersion = new byte[64];
	}
	
	public static class FaceLibQuery extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public int 						iPageNo;
		public int 						iPageCount;
	}
	
	public static class FaceLibQueryResult extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public int 						iTotal;
		public int 						iPageNo;
		public int 						iIndex;
		public int 						iPageCount;
		public FaceLibInfo 				tFaceLib;
	}
	
	public static class FaceLibQueryResultArr extends Structure {
		public FaceLibQueryResult [] tResult = new FaceLibQueryResult[20];
	}
	
	public static class FaceAlarmParamArr extends Structure {
		public FaceAlarmParam [] tResult = new FaceAlarmParam[33];
	}
	
	public static class FaceLibEdit extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public FaceLibInfo 				tFaceLib;
	}
	
	public static class FaceLibDelete extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public int 						iLibKey;
	}
	
/*	public static class FaceReply extends Structure {
		public int 						iSize;
		public int 						iLibKey;
		public int 						iFaceKey;
		public int 						iOptType;
		public int 						iResult;
	}
	*/
	public static class FaceInfo extends Structure {
		public int 						iSize;
		public int 						iLibKey;
		public int 						iFaceKey;
		public int 						iCheckCode;
		public int 						iFileType;
		public int 						iModeling;
		public byte[] 					cName = new byte[64];
		public int 						iSex;
		public byte[] 					cBirthTime = new byte[16];
		public int 						iNation;
		public int 						iPlace;
		public int 						iCertType;
		public byte[] 					cCertNum = new byte[64];
		public int						iOptType;
		public byte[] 					cLibUUID = new byte[64];
		public byte[] 					cFaceUUID = new byte[64];
	}
	
	public static class FaceQuery extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public int 						iPageNo;
		public int 						iPageCount;
		public int 						iLibKey;
		public int 						iModeling;
		public byte[] 					cName = new byte[64];
		public int 						iSex;
		public byte[] 					cBirthStart = new byte[16];
		public byte[] 					cBirthEnd = new byte[16];
		public int 						iNation;
		public int 						iPlace;
		public int 						iCertType;
		public byte[] 					cCertNum = new byte[64];
		public byte[] 					cLibUUID = new byte[64];
	}
	
	public static class FaceQueryResult extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public int 						iTotal;
		public int 						iPageNo;
		public int 						iPageCount;
		public int 						iIndex;
		public FaceInfo 				tFace;
	}
	
	public static class FaceQueryResultArr extends Structure {
		public FaceQueryResult[] tResult = new FaceQueryResult[20];
	}
	
	public static class FaceCutQueryResults extends Structure {
		public FaceCutQueryResult[] tResult = new FaceCutQueryResult[20];
	}
	
	public static class FaceEdit extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public byte[] 					cFacePic = new byte[256];
		public FaceInfo 				tFace;
	}
	
	public static class FaceDelete extends Structure {
		public int 						iSize;
		public int 						iChanNo;
		public int 						iLibKey;
		public int 						iFaceKey;
		public byte[] 					cLibUUID = new byte[64];
		public byte[] 					cFaceUUID = new byte[64];
	}
	
	public static class VcaStatue extends Structure {
		public int 						iStatus;
	}
	
	public static class VCASuspendResult extends Structure {
		public int 						iSize;
		public int 						iStatus;
		public int 						iResult;
	}
	
	public static class PICSTREAM_INFO extends Structure {
        public byte[] RecvBuffer = new byte[200*1024];
    }
	
	public static interface NET_PICSTREAM_NOTIFY extends StdCallCallback {
		int PicDataNotify(int _ulID, int _lCommand, Pointer _tInfo, int _iLen, Pointer _lpUserData);
	}
	
	public static class NetPicPara extends Structure {
		public int 						iStructLen;				//Structure length
		public int						iChannelNo;
		public NET_PICSTREAM_NOTIFY		cbkPicStreamNotify;
		public Pointer					pvUser;
	}
	
	public static class RECT extends Structure implements ByValue {
		public int 						left;
		public int 						top;
		public int 						right;
		public int 						bottom;
	}
	
	public static class FaceAttribute extends Structure{
		public int iType;
		public int iValue;
	}
	
	public static class FacePicData extends Structure {
		public int						iFaceId;			
		public int						iDrop;	
		public int						iFaceLevel;	
		public RECT						tFaceRect;	
		public int						iWidth;				
		public int						iHeight;	
		public int						iFaceAttrCount;			//Number of face attributes
		public int						iFaceAttrStructSize;	//The size of strcut FaceAttribute
		public Pointer[] ptFaceAttr = new Pointer[256];		//Face attributes,supports up to 256 attribute types,the subscript is the face attribute type://0-age,1-gender,2-masks,3-beard,4-eye open,5-mouth,6-glasses,7-race,8-emotion,9-smile,10-value......									
		public int						iDataLen;			
		public Pointer					pcPicData;
		public long						ullPts;	
		public int						iAlramType;				//
		public int						iSimilatity;			//0~100
		public int						iLibKey;				//library key id
		public int						iFaceKey;				//face key id
		public int						iNegPicLen;				//negative picture len
		public Pointer					pcNegPicData;			//negative picture data
		public int						iNegPicType;			//negative picture type
		public int						iSex;
		public int						iNation;
		public int						iPlace;					//negative place
		public int						iCertType;				//credentials type
		public byte[] 					cCertNum = new byte[64]; 
		public byte[] 					cBirthTime = new byte[16]; 
		public byte[] 					cName = new byte[64]; 
		public byte[] 					cLibName = new byte[64]; 	
		public byte[]					cLibUUID = new byte[64]; //ID of face database on platform
		public byte[]					cFaceUUID = new byte[64]; //ID of the picture in the platform
		public byte[]					cVerifyCode = new byte[36];//Picture check code, MD5 of picture file
	}
	
	public static class PicTime extends Structure {
		public int 						uiYear;
		public int 						uiMonth;
		public int 						uiDay;
		public int 						uiWeek;
		public int 						uiHour;
		public int 						uiMinute;
		public int 						uiSecondsr;
		public int 						uiMilliseconds;
	}
	
	public static class PicData extends Structure {
		public PicTime 					tPicTime;
		public int 						iDataLen;
		public Pointer 					pcPicData;
	}
	
	public static class FacePicStream extends Structure {
		
		public int 						iStructLen;	
		public int 						iSizeOfFull;		//The size of strcut PicData
		public Pointer 					tFullData;				
		public int 						iFaceCount;	
		public int 						iSizeOfFace;		//The size of strcut FacePicData
		public Pointer[] tPicData = new Pointer[32]; 
		public int						iFaceFrameId;		//The face jpeg frame no
	}

	public static class FaceAlarmParam extends Structure {
		public int					iSize;
		public int					iChanNo;					//	Channel number
		public int					iAlarmType;					//	21: face alarm type
		public int					iParam1;					//	Algorithm type 0: face detection 1: face recognition - comparison 2: face recognition - Stranger 3: face recognition - frequency 4: face recognition - detention
		public int					iParam2;					//	When the algorithm type is 0 \ 1 \ 2, it is 0. When iparam1 is 3 \ 4, it means time
		public int					iParam3;					//	When the algorithm type is 0 \ 1 \ 2, it passes 0. When iparam1 is 3, it represents the frequency
		public byte[] 				cLibKey = new byte[64];		//	Library ID
		public int					iRecognition;				//	Upload identification information, 0-not supported, 1-not uploaded, 2-uploaded
		public int					iSimilar;					//	Similarity
		public int					iDevType;					//	0-IPC, 1-NVR
		public int					iEnable;					//	0-Not Enable  1-Enable
		public byte[] 				cLibUUID = new byte[64];		
		public int					iLibEnable;
	}
	
	public static class FaceAlarmParamIn extends Structure {  
		public int					iSize;
		public int					iVcaType;					//	Algorithm type 0: face detection 1: face recognition - comparison 2: face recognition - Stranger 3: face recognition - frequency 4: face recognition - detention
	}
	
	public static class AnyScene extends Structure {
		public int					iBufSize;
		public int					iSceneID;					
		public byte[] 			cSceneName = new byte[32];		
		public int					iArithmetic;				//enable the algorithm type( bit0: 1-action analysis algorithm enable; 0-not enable
																		//bit1: 1-track algorithm enable; 0-not enable
																		//bit2: 1-face detection algorithm enable; 0-not enable
																		//bit3: 1-people amount statistics algorithm enable; 0-not enable
																		//bit4: 1-video diagnose algorithm enable; 0-not enable
																		//bit5: 1-license plate recognition algorithm enable; 0-not enable
																		//bit6: 1-audio and video exception algorithm enable; 0-not enable 
																		//bit7: 1-off post algorithm enable; 0-not enable      
																		//bit8: 1-people gathering algorithm enable; 0-not enable
																		//bit11:1-witness protection algorithm enable; 0-not enable
																		//bit20:1-Structured algorithm enable; 0-not enable
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
		public int					iDevType;					//	0-IPC, 1-NVR
	}
	
	public static class POINT extends Structure {  
		public int					x;
		public int					y;
	}
	
	public static class FaceDetectArithmetic extends Structure {  
		public int				iBufSize;			//Face detection algorithm structure size
		public int				iSceneID;			//Scene id(0~15)
		public int				iMaxSize;			//maximum face size(0~100 percentage of image width,100 indicates the width of entire screen)
		public int				iMinSize;			//minimum face size(0~100 percentage of image width, 100 indicates the width of entire screen)
		public int				iLevel;				//Algorithm run level(0~5)
		public int				iSensitiv;			//Sensitivity(0~5)
		public int				iPicScale;			//Picture scale(1~10)
		public int				iSnapEnable;		//Face snap enable(1-enable, 0-not enable)
		public int				iSnapSpace;			//Snap space(space frame count)
		public int				iSnapTimes;			//Snap times(1~10)
		public int				iPointNum;			//polygon area vertex number(3~32)
		public POINT[]	ptArea = new POINT[32]; //polygon area vertex point
		public int				iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
		public int				iExposureBright;	//exposure light strength
		public int				iDisplayRule;		//0-not display 1-display
		public int				iMinSizeEx;			//minimum face size(0~10000 percentage of image width, 10000 indicates the width of entire screen)
		public int				iMaxSizeEx;			//maximum face size(0~10000 percentage of image width,10000 indicates the width of entire screen)
		public int				iPushMode;			//push mode 0:reserved 1:Speed first 2:Quality first  3:custom	4:timing
		public int				iPushLevel;			//push level effect when push_mode == 3;(0:reserved,1:fast 2:normal 3:slow) effect when push_mode == 4;(value is timing time)
		public int				iSnapMode;			//snap mode (0:reserved 1:snap all 2:snap high quality 3:custom)
		public int				iSnapLevel;			//Snap level effect when Snap_mode == 3;(0~100)
		public int				iDentification;		//Face recognition algorithm switch: 0-not supported, 1-off, 2-on
		public int             iDevType;			//DevType:0-IPC, 1-NVR
		public int             iQpvalueBig;		//ST Customized, background image quality, range 1~100, 0 means not using this setting.			
		public int             iQpvalueSmall;		//ST Customized, face thumbnail quality, range 1~100, 0 means not using this setting.						
		public int          	iAlgSnapMode;		//ST Customized, algorithmic capture mode, 0-face, 1-vehicle, 2-mix.
		public int             iPlateMinSize;		//ST Customized, the image width is very divided, the range is 1~10000, 10000 means the width of the whole screen. When this field is 0, it means no processing.
		public int				iDelayTime;			//Valid when iPushMode == 2. 500ms,1000ms,2000ms
		public int				iTimeSpace;			//Push Pic TimeSpace. 100ms,200ms,300ms,500ms,1000ms,2000ms
	}
	
	public static class NVS_FILE_TIME extends Structure implements ByValue {
		public short 		iYear;   				// Year
		public short 		iMonth;  				// Month
		public short 		iDay;    				// Day
		public short 		iHour;   				// Hour
		public short 		iMinute; 				// Minute
		public short 		iSecond; 				// Second
	}
	
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
	
	public static class PicStreamUploadParam extends Structure {
		public int         iSize;			
		public int         iSceneId;		//range:0~15
		public int         iRuleNo;			//range:0~7
		public int         iPicType;		//Picture type: 0-Background big picture, 1-Small picture
		public int         iSnapEnable;		//Send enable:0-not upload, 1-upload
		public int         iIsOsd;			//Overlay information: 0-not stack, 1-stack.When iPicType=1,this parameter is invalid.
		public int         iQpvalue;		//Snap picture quality range:1-100
		public int		   iFaceFrameEnable;//Overlay face frame: 0-not stack, 1-stack.When iPicType=1,this parameter is invalid.
	};
	
	public static class FaceLibSyncStart extends Structure{
		public int					iSize;
		public int					iChanListArraySize;			//Number of channels to be synchronized
		public int[]				iChanList = new int[64];	//Channel list
		public int					iLibKey;					//Face database key value
		public byte[]				cLibUUID = new byte[64];	//ID of the face database on the platform, if not empty, this field shall prevail
		public int					iStatus;					//Start status: 20 start, 21 stop, 22 delete task, 23 delete front-end face database
	};
	
	public static class FaceLibSyncQuery extends Structure{
		public int					iSize;
		public int					iChanNo;
		public int					iQueryResultSize;	//when iChanNo=0x7FFFFFFF,iQueryResultSize=1;else iQueryResultSize is NVR's all channel number.
		public int					iLibKey;
		public byte[]				cLibUUID = new byte[64];
	};

	public static class FaceLibSyncQueryResult extends Structure{
		int							iSize;
		int							iChanNo;
		int							iLibKey;
		public byte[]				cLibUUID = new byte[64];
		int							iState;						//sync status:0-unsync, 1-synching, 2-sync success, 3-sync failure, 4-to be sync
		int							iProcess;					//Percentage of progress: range:0-100
		int							iSuccNum;					//sync success num		
		int							iFailNum;					//sync failed num
	};
	
	public static class FaceLibSyncQueryResultArray extends Structure {
		public FaceLibSyncQueryResult[] tArry = new FaceLibSyncQueryResult[360]; //Defined by up to 360 channels
	}
	
	public static class FuncAbilityLevel extends Structure {
		public int			iSize;
		public int			iMainFuncType;					//main function type
		public int			iSubFuncType;					//sub function type
		public byte[]		cParam = new byte[1024];		//Capability Description
	};
	
	public static class DownloadFile extends Structure {
		public int							m_iSize;				//Structure size
		public byte[]          	        	m_cRemoteFilename = new byte[255];	//Fornt end video file name
		public byte[]						m_cLocalFilename = new byte[255];	//Local video file name
		public int							m_iPosition;			//File location by percentage 0~100;When continue send after stop send,file pointer offset request 
		public int							m_iSpeed;				//1, 2, 4, 8, Control file play speed, 0-Suspend
		public int							m_iIFrame;				//Only send I frame 1,Only play I Frame;0, All play					
		public int							m_iReqMode;				//Require data mode 1,Frame mode;0,Stream mode					
		public int							m_iRemoteFileLen;		//If local file is not null, the parameter set to null
		public int							m_iVodTransEnable;		//Enable
		public int							m_iVodTransVideoSize;	//Video pixel
		public int							m_iVodTransFrameRate;   //Frame rate
		public int							m_iVodTransStreamRate;  //Code rate
		public int							m_iSaveFileType;		//0:SDV	1:MP4
		public int							m_iFileAttr;			//File attributes:0: nvr local storage; 10000: ipc storage
		public int							m_iCryptType;			//iCryptType = 0, no encryption; iCryptType = 1, is AES encryption
		public byte[]			        	m_cCryptKey = new byte[32];
	};
	
	public static class FaceCutEx extends Structure {
		public int					iSize;
		public int					iChanNo;
		public byte[]				cCheckCode = new byte[64];
		public int					iPicType;
		public byte[]				cPicPath = new byte[256];
		public int					iPageNo;
		public int					iPageCount;
	};
	
	public static class FaceCutQueryResult extends Structure {
		public int					iSize;
		public int					iChanNo;
		public int					iTaskId;
		public int					iTotal;
		public int					iPageNo;
		public int					iIndex;
		public int					iPageCount;
		public byte[]				cFileName = new byte[256];
	};
	
	public static class FaceSearch extends Structure {
		public int					iSize;
		public int					iChanNo;
		public int					iTaskId;
		public byte[]				cLibKey = new byte[64];
		public byte[]				cPicName = new byte[256];
		public int					iSimilar;
		public int					iPageNo;
		public int					iPageCount;
		public int					iLibKey;
	};
	
	public static class QueryChanNo extends Structure 
	{
		public int 				iChanNo;
		public int 				iStream;
	};	
	
	public static class FaceSearchSnapProcess extends Structure 
	{
		public int 				iSize;
		public int 				iTaskId;
	};
	
	public static class FaceSearchSnapQuery extends Structure 
	{
		public int 				iSize;
		public int 				iTaskId;
		public int   		    iPageSize;
		public int              iPageNo;
	};
	
	public static class VcaFileAttr extends Structure 
	{
		public int					iFileIndex;
		public byte[]				cFileName = new byte[256];
		public int					iFileSize;
		public int					iFileType;
		public int					iReserve;
		public byte[]				cReserve = new byte[64];
	};
	
	public static class FaceSearchSnapResults extends Structure {
		public FaceSearchSnapResult[] tResult = new FaceSearchSnapResult[20];
	}
	
	public static class FaceSearchSnapResult extends Structure 
	{
		public int					iSize;
		public int					iChanNo;
		public int					iTotal;
		public int					iCurPageCount;
		public int					iItemIndex;
		public NVS_FILE_TIME		tBegTime;
		public NVS_FILE_TIME		tEndTime;
		public int  			 	iAge;
		public int                  iSex;
		public int					iNation;
		public int					iWearGlasses;
		public int					iWearMask;
		public int					iSimilarity;
		public VcaFileAttr			tPicSnap;
		public VcaFileAttr			tPicNeg;
	};	
	
	public static class FaceSearchSnap extends Structure {
		public int  			    iSize;
		public int					iChanCount;
		public int					iChanSize;	
		public Pointer				pChanList; 
		public NVS_FILE_TIME	    tBegTime;
		public NVS_FILE_TIME	    tEndTime;
		public byte[]			    cPicturePath = new byte[256];
		public int 			     	iSimilarity;
		public int 			        iSortMode; //0-sort by time 1-sort by similarity
		public int                  iTaskId;
	};
	
	public static class FaceReply extends Structure {
		public int					iSize;					//Structure size
		public int					iLibKey;				//Face database key value, iLibKey>0	  
		public int 			     	iFaceKey;				//image key value, iFaceKey>0 
		public int					iOptType;				//0-add face 1-edit face 2-delete face 3-add face database 4-edit face database 5-delete face database 6-face modeling 7-query face database
													//8-query the face 9-query the face attribute 10-extract the face attribute 11-button the face 12-search the base image with the image 13-synchronize the face database 14 query the status of the synchronous face database
													//15 - image search and capture 16 - face database copy migration 17 - face database unlock password verification
		public int					iResult;				//0-success, 1-system ready, 2-failure, 3-out of maximum range, 4-system deleting, 5-system snapping face
													//6-the system is performing the operation of searching and capturing pictures 7-password error
		public byte[]				cLibUUID = new byte[64];	//ID of face database on platform
		public byte[]				cFaceUUID = new byte[64]; //ID of the picture corresponding to the platform
		public int					iDelLibProgress;		//Valid when iresult = 4, indicating the deletion progress of face database, value range 0-100
													//When iresult = 6, it is valid, indicating the search progress of image search and snapshot, and the value range is 0-100
	};
	
	public static class QueryChanNos extends Structure 
	{
		public QueryChanNo[] tResult = new QueryChanNo[2];
	};
	
	public static class NetFileQueryVca extends Structure {
		public int					iSize;
		public int					iChanCount;
		public int					iChanSize;
		public Pointer       		pChanList;
		public int			     	iVcaCount;
		public int[]				iVcaList = new int[32];
		public NVS_FILE_TIME		tBegTime;
		public NVS_FILE_TIME		tEndTime;
		public int					iPageCount;
		public int					iPageNo;
		public int					iFileType;
		public int					iConditionCount;
		public byte[]				cQueryCondition = new byte[32*256];
	};
	
	public static class NetFileQueryVcaResult extends Structure {
		public int					iSize;
		public int					iChanNo;
		public int					iFileAttrCount;
		public VcaFileAttr[]		tFileAttr = new VcaFileAttr[5];
		public int					iTotal;
		public int					iCurPageCount;
		public int					iItemIndex;
		public int					iFileType;
		public int					iVcaType;
		public NVS_FILE_TIME		tBegTime;
		public NVS_FILE_TIME		tEndTime;
		public int					iExAttrCount;
		public byte[]			    cExAttr = new byte[32*256];
	};
		
	public static class NetFileQueryVcaResults extends Structure {
		public NetFileQueryVcaResult[] tResult = new NetFileQueryVcaResult[20];
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
	
	public static class STR_Para extends Structure {
	    public long[] 	m_iPara = new long[10];
	    public byte[] 	m_cPara = new byte[33];
	};
	
	public static class TemperatureScaleType extends Structure {
        public int iSize;
        public int iChanNo;
        public int iTempStandard;    //temperature scale type, 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin 
    };
    
	public static class SingleBlackbodyParam extends Structure {
        public int iBlackBodyId;
        public int iBlackBodyTemp;
        public int iBlackBodyTempUnit; 
        public int iBlackBodyDistance;
        public RECT tRect;
    };
    
	public static class BlackbodyCorrection extends Structure {
        public int iSize;
        public int iChanNo;
        public int iBlackBodyCorrectEnable; 
        public int iBlackBodyCorrectType;
        public int iBlackBodyNum;
        public SingleBlackbodyParam[] tParam = new SingleBlackbodyParam[2];
    };
    
	public static class BodyTempCorrect extends Structure {
        public int iSize;
        public int iChanNo;
        public int iBodyTempCorrectEnable; 
        public int iBodyTempCorrectSensitivity;
    };
    
	public static class IntelligentCorretct extends Structure {
        public int iSize;
        public int iChanNo;
        public int iIntelligentCorrectEnable; 
        public int iIntelligentCorrectSensitivity;
    };
    
	public static class VCATemDetect extends Structure {
        public int iSize;
        public int iSceneID;
        public int iRuleID; 
        public int iValid;
        public int iDisplayTemScaleEnable;
        public int iHighTemColor;
        public int iLowTemColor; 
        public int iModelType;
        public int iTemUnit;
        public int iTemThreshold;
        public int iWaitTime; 
        public int iTempLoseEnable;
    };
    
	int NetClient_SetPort(int _iServerPort, int _iClientPort);
	int NetClient_Startup_V4(int _iServerPort, int _iClientPort, int _iWnd);
	int NetClient_Cleanup();
	int NetClient_SetNotifyFunction_V4(MAIN_NOTIFY _cbkMainNotify, ALARM_NOTIFY _cbkAlarmNotify
			, PARACHANGE_NOTIFY _cbkParaChangeNotify, Pointer _cbkCom, Pointer _cbkProxy);
	int NetClient_Logon_V4(int _iLogonType, Pointer _pBuf, int _iBufSize);
	int NetClient_Logoff(int _iLogonID);
	int NetClient_GetLogonStatus(int _iLogonID);
	int NetClient_GetVersion(SDK_VERSION _ver);
	int NetClient_SetDsmConfig(int _iCommand, Pointer _pvBuf, int _iBufSize);
	int NetClient_GetDsmRegstierInfo(int _iCommand, Pointer _pvBuf, int _iBufSize);
	int NetClient_GetDevInfo(int _iLogonID, ENCODERINFO _pEncoderInfo);
	int NetClient_SetTime(int _iLogonID, int _iyy, int _imo, int _idd, int _ihh, int _imi, int _iss);
	int NetClient_FaceConfig(int _iLogonId, int _iCmdId, int _iChanNo, Pointer _lpIn, int _iInLen, Pointer _lpOut, int _iOutLen);
	int NetClient_SetDevConfig(int _iLogonId, int _iCommand, int _iChannel, Pointer _lpInBuffer, int _iInBufferSize);
	int NetClient_GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _lpOutBuffer, int _iOutBufferSize, IntByReference _lpBytesReturned);
	
	int NetClient_StartRecvNetPicStream(int _iLogonID, NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID);
	int NetClient_StopRecvNetPicStream(int _iRecvID);

	int NetClient_SetAlarmConfig(int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, Pointer _pvCmdBuf);
	int NetClient_GetAlarmConfig(int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, Pointer _pvCmdBuf);

	int NetClient_GetOsdText(int _iLogonID, int _iChannelNum, Pointer _pcOSDText, IntByReference _pulTextColor);
	
	int NetClient_GetDigitalChannelNum(int _iLogonID, IntByReference _piDigitChannelNum);
	int NetClient_GetChannelNum(int _iLogonID, IntByReference _piChannelNum);
	
	int NetClient_NetFileDownload(IntByReference _uiConID, int _iLogonID, int _iCmd, Pointer _pvBuf, int _iBufSize);
	int NetClient_NetFileStopDownloadFile(int _uiConID);
	
	int NetClient_VCASetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, Pointer _lpCmdBuf, int _iCmdBufLen);
	int NetClient_VCAGetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, Pointer _lpCmdBuf, int _iCmdBufLen);
	
	int NetClient_Query_V5(int _iLogonId, int _iCmdId, int _iChanNo, Pointer _lpIn, int _iInLen, Pointer _lpOut, int _iOutLen);
	
	int NetClient_SetCommonEnable(int _iLogonID, int _iEnableID, int _iChannel, int _iEnableValue);
}

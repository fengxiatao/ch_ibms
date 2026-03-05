using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;

namespace COMMON_STRUCT
{
    delegate void RECVDATA_NOTIFY(UInt32 _ulID, string _strData, Int32 _iLen);
    delegate void RECVDATA_NOTIFY_EX(int _ulID, string _ucData, Int32 _iLen, Int32 _iFlag, IntPtr _lpUserData);
    delegate void FULLFRAME_NOTIFY_V4(UInt32 _ulID, UInt32 _ulStreamType, string _cData, Int32 _iLen, IntPtr _iUser, IntPtr _iUserData); //_iUser: file header data, _iUserData: User data

    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    delegate void MAIN_NOTIFY_V4(UInt32 _ulLogonID, int _iWparam, IntPtr _iLParam, IntPtr _iUser);
    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    delegate void ALARM_NOTIFY_V4(Int32 _ulLogonID, Int32 _iChan, Int32 _iAlarmState, Int32 _iAlarmType, Int32 _iUser);
    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    delegate void PARACHANGE_NOTIFY_V4(Int32 _ulLogonID, Int32 _iChan, Int32 _iParaType, ref STR_Para _strPara, Int32 _iUser);
    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    delegate void COMRECV_NOTIFY_V4(Int32 _ulLogonID, IntPtr _cData, Int32 _iLen, Int32 _iComNo, Int32 _iUser);
    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    delegate void PROXY_NOTIFY(Int32 _ulLogonID, Int32 _iCmdKey, IntPtr _cData, Int32 _iLen, Int32 _iUser);
    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    delegate int NETPICSTREAM_NOTIFY(UInt32 _uiRecvID, int _lCommand, IntPtr _pvCallBackInfo, Int32 _BufLen, IntPtr _iUser);

    [StructLayout(LayoutKind.Sequential)]
    struct STR_Para
    {

    };

    [StructLayout(LayoutKind.Sequential)]
    public struct CLIENTINFO
    {
        public int m_iServerID;         //NVS ID,NetClient_Logon return value
        public int m_iChannelNo;	    //Remote host to be connected video channel number (Begin from 0)
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 255)]
        public Char[] m_cNetFile;       //Play the file on net, not used temporarily
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public Char[] m_cRemoteIP;	    //IP address of remote host
        public int m_iNetMode;		    //Select net mode 1--TCP  2--UDP  3--Multicast
        public int m_iTimeout;		    //Timeout length for data receipt
        public int m_iTTL;			    //TTL value when Multicast
        public int m_iBufferCount;      //Buffer number
        public int m_iDelayNum;         //Start to call play progress after which buffer is filled
        public int m_iDelayTime;        //Delay time(second), reserve
        public int m_iStreamNO;         //Stream type
        public int m_iFlag;			    //0, Request the video file for the first time; 1. Operate the video file
        public int m_iPosition;		    //0~100, Locate the file playback location; - 1, do not locate
        public int m_iSpeed;			//1, 2, 4, 8, Control file playback speed       
    };

    [StructLayout(LayoutKind.Sequential)]
    public struct RECT
    {
        public int left;
        public int top;
        public int right;
        public int bottom;
    };

    [UnmanagedFunctionPointer(CallingConvention.StdCall)]
    public delegate int NET_PICSTREAM_NOTIFY(UInt32 _uiRecvID, int _lCommand, IntPtr _pvBuf, Int32 _iBufLen, IntPtr _pvUser);
    [StructLayout(LayoutKind.Sequential)]
    public class NetPicPara
    {
        public Int32            iStructLen;				//Structure length
        public Int32            iChannelNo;
        public NET_PICSTREAM_NOTIFY    cbkPicStreamNotify;
        public IntPtr			pvUser;
        public NetPicPara()
        {
            cbkPicStreamNotify = null;
            pvUser = IntPtr.Zero;
            iChannelNo = 0;
            iStructLen = 0;
        }
    };

    [StructLayout(LayoutKind.Sequential)]
    class LogonPara
    {
        public int		iSize;			//Structure size
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btProxy;			//The ip address of the upper-level proxy to which the video is connected,not more than 32 characters, including '\0'
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btNvsIP;			//IP address, not more than 32 characters, including '\0'
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btNvsName;		//Nvs name. Used for domain name resolution
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] btUserName;		//Login Nvs username, not more than 16 characters, including '\0'
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] btUserPwd;		//Login Nvs password, not more than 16 characters, including '\0'
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btProductID;		//Product ID, not more than 32 characters, including '\0'
        public int		iNvsPort;		//The communication port used by the Nvs server, if not specificed,Use the system default port(3000)
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btCharSet;		//Character set
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] btAccontName;	//The username that connects to the contents server
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] btAccontPasswd;	//The password that connects to the contents server

        public LogonPara()
        {
            btProxy = new byte[32];
            btNvsIP = new byte[32];
            btNvsName = new byte[32];
            btUserName = new byte[16];
            btUserPwd = new byte[16];
            btProductID = new byte[32];
            btCharSet = new byte[32];
            btAccontName = new byte[16];
            btAccontPasswd = new byte[16];
        }
    };

    [StructLayout(LayoutKind.Sequential)]
    class LogonActiveServer
    {
        public int		iSize;	
		[MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] btUserName;
		[MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] btUserPwd;	
	    [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btProductID;	//product id	
        public LogonActiveServer()
        {
            btUserName = new byte[16];
            btUserPwd = new byte[16];
            btProductID = new byte[32];
        }
    };

    [StructLayout(LayoutKind.Sequential)]
    class DsmOnline
    {
        public int		iSize;
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btProductID;	        //product id	
        public int		iOnline;			//0--Offline 1--Online
        public DsmOnline()
        {
            btProductID = new byte[32];
        }
    };

    [StructLayout(LayoutKind.Sequential)]
    public struct PicTime
    {
        public UInt32 uiYear;
        public UInt32 uiMonth;
        public UInt32 uiDay;
        public UInt32 uiWeek;
        public UInt32 uiHour;
        public UInt32 uiMinute;
        public UInt32 uiSecondsr;
        public UInt32 uiMilliseconds;
    };

    [StructLayout(LayoutKind.Sequential)]
    public struct PicData
    {
        [MarshalAs(UnmanagedType.Struct)]
        public PicTime		tPicTime;
        public Int32        iDataLen;		    //Image length
        public IntPtr       piPicData;		    //Picture raw data, the upper can be directly saved as a picture
    };

    [StructLayout(LayoutKind.Sequential)]
    public struct VcaPicStream
    {
        public Int32        iStructLen;				//Structure length
        public Int32        iWidth;					//Picture wide
        public Int32        iHeight;				//Picture high
        public Int32        iChannelID;				//Channel number
        public Int32        iEventType;				//Event type
        public Int32        iRuleID;				//Rule ID
        public Int32        iTargetID;				//Target ID
        public Int32        iTargetType;			//Target type:1-person,2-(other),3-car
        public Int32        iTargetSpeed;			//The target speed by the number of pixels per second (pixel/s)
        public Int32        iTargetDirection;		//Target movement direction,0 ~ 359 degrees
        [MarshalAs(UnmanagedType.Struct)]
        public RECT         tTargetPosition;		//target location
        public Int32        iPresetNo;          	//Preset number (scene number)       
        [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 16)]
        public string       strCameraIP;//Camera IP     		
        public Int32        iPicCount;				//Record the number of images included,Maximum 3
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)]
        public PicData[]    tPicData; // Structure array definition   
    };

    [StructLayout(LayoutKind.Sequential)]
    public struct ItsPicStream
    {
        public Int32 			iStructLen;				//Structure length
        public Int32    		iChannelID;				//Lane number
        [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 32)]
        public string		    strPlate;			    //License plate
        public Int32    		iPlateColor;			//License plate color		
        public Int32    		iPlateType;				//License plate type
        public Int32 	  		iCarColor;				//the color of car
        [MarshalAs(UnmanagedType.Struct)]
        RECT		            tPlateRange;			//License plate range
        public Int32    		iCharNum;				//The number of characters in the license plate
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 12)]
        public Int32[]    		iCharConfid;		    //Each character confidence, up to 12 characters
        public Int32    		iPlateConfid;			//The entire license plate confidence
        public Int32    		iRecrdoNum;				//Identify picture serial number
        public Single  		    fSpeed;					//Vehicle speed
        public Int32  		    iVehicleDirection;		//Direction of the vehicle
        public Int32 			iAlarmType;				//Alarm type
        [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 16)]
        public string  		    strCameraIP;		    //Camera IP
        public Int32 			iRedBeginTime;			//Red light start time in seconds
        public Int32 			iRedEndTime;			//Red light end time in seconds
        public Int32    		iPicCount;				//Record the number of images included
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)]
        public PicData[]    tPicData;                    
    };

    [StructLayout(LayoutKind.Sequential)]
    public struct FaceAttribute
    {
        public Int32 iType;				//Face attr type
        public Int32 iValue;			//face attr value
    }; 

    [StructLayout(LayoutKind.Sequential)]
    public struct FacePicData
    {
	    public Int32			iFaceId;				//Face track Id
	    public Int32			iDrop;					//The face track ends, 0 is not over, 1 ends
	    public Int32			iFaceLevel;				//Face quality,0-100
        [MarshalAs(UnmanagedType.Struct)]
	    RECT		            tFaceRect;				//Face coordinates
	    public Int32			iWidth;					//Face wide picture of the wide
	    public Int32			iHeight;				//Face small picture of the high
        public Int32            iFaceAttrCount;			//Number of face attributes
        public Int32            iFaceAttrStructSize;	//The size of strcut FaceAttribute
	    [MarshalAs(UnmanagedType.ByValArray, SizeConst = 256)]
        public IntPtr[]         ptFaceAttr;		        //Face attributes,supports up to 256 attribute types,the subscript is the face attribute type:
										                //0-age,1-gender,2-masks,3-beard,4-eye open,5-mouth,6-glasses,7-race,8-emotion,9-smile,10-value......
	    public Int32			iDataLen;				//Face picture length
	    public IntPtr		    pPicData;				//Small figure bare data, the upper can be directly saved as a picture
        public UInt64           ullPts;                 //Timestamp of small picture
    }; //Face map information

    [StructLayout(LayoutKind.Sequential)]
    struct FacePicStream
    {
        public Int32  			iStructLen;			//Structure length
        public Int32            iSizeOfFull;	
        public IntPtr           tFullData;
        public Int32 			iFaceCount;			//The current frame detects the number of face
        public Int32            iSizeOfFace;
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public IntPtr[]         tFaceData;
        public Int32            iFaceFrameId;       //The face jpeg frame no       
    };

    [StructLayout(LayoutKind.Sequential)]
    class AnyScene
    {
        public Int32 iBufSize;          //Scene application timed cruise template structure size
        public Int32 iSceneID;          //Scene number(0~15)
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] btSceneName;     //Scene name
        public Int32 iArithmetic;       //enable the algorithm type( bit0: 1-action analysis algorithm enable; 0-not enable
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
        public Int32 iDevType;
        public Int32 iArithmeticEx;     //enable the algorithm type
                                        //bit0: 1-human detection algorithm enable 0-not enable
                                        //bit1 1-pept enable 0-not enable
                                        //bit2 1-Navigation ship detection enable 0-not enable
                                        //bit3 1-granary vehicles detect enable 0-not enable
                                        //bit4 1-chef hat detect enable 0-not enable
                                        //bit5 1-chef mask detect enable 0-not enable
        public Int32 iActionType;       //0:Fixed pre-postion 1:scanlist
        public AnyScene()
        {
            btSceneName = new byte[32];
        }
    };

    [StructLayout(LayoutKind.Sequential)]
    struct VCASuspend
    {
        public Int32 iStatus;         //Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
        public Int32 iDevType;        //0-IPC, 1-NVR
    };

    [StructLayout(LayoutKind.Sequential)]
    struct POINT
    {
        public Int64 x;
        public Int64 y;
    };

    [StructLayout(LayoutKind.Sequential)]
    struct FaceDetectArithmetic
    {
	    public Int32 iBufSize;			//Face detection algorithm structure size
	    public Int32 iSceneID;			//Scene id(0~15)
	    public Int32 iMaxSize;			//maximum face size(0~100 percentage of image width,100 indicates the width of entire screen)
	    public Int32 iMinSize;			//minimum face size(0~100 percentage of image width, 100 indicates the width of entire screen)
	    public Int32 iLevel;			//Algorithm run level(0~5)
	    public Int32 iSensitiv;			//Sensitivity(0~5)
	    public Int32 iPicScale;			//Picture scale(1~10)
	    public Int32 iSnapEnable;		//Face snap enable(1-enable, 0-not enable)
	    public Int32 iSnapSpace;		//Snap space(space frame count)
	    public Int32 iSnapTimes;		//Snap times(1~10)
	    public Int32 iPointNum;			//polygon area vertex number(3~32)
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
	    public POINT[] ptArea;           //polygon area vertex point
	    public Int32 iDisplayTarget;	//display target box, 0-not dispaly, 1-dispaly
	    public Int32 iExposureBright;	//exposure light strength
	    public Int32 iDisplayRule;		//0-not display 1-display
	    public Int32 iMinSizeEx;		//minimum face size(0~10000 percentage of image width, 10000 indicates the width of entire screen)
	    public Int32 iMaxSizeEx;		//maximum face size(0~10000 percentage of image width,10000 indicates the width of entire screen)
	    public Int32 iPushMode;			//push mode 0:reserved 1:Speed first 2:Quality first  3:custom	4:timing 5: Entrance guard(continuous) 6: reserved 7: channel
	    public Int32 iPushLevel;		//push level effect when push_mode == 3;(0:reserved,1:fast 2:normal 3:slow) effect when push_mode == 4;(value is timing time)
	    public Int32 iSnapMode;			//snap mode (0:reserved 1:snap all 2:snap high quality 3:custom)
	    public Int32 iSnapLevel;		//Snap level effect when Snap_mode == 3;(0~100)
	    public Int32 iDentification;	//Face recognition algorithm switch: 0-not supported, 1-off, 2-on
	    public Int32 iDevType;			//DevType:0-IPC, 1-NVR
	    public Int32 iQpvalueBig;		//ST Customized, background image quality, range 1~100, 0 means not using this setting.			
	    public Int32 iQpvalueSmall;		//ST Customized, face thumbnail quality, range 1~100, 0 means not using this setting.						
	    public Int32 iAlgSnapMode;		//ST Customized, algorithmic capture mode, 0-face, 1-vehicle, 2-mix.
	    public Int32 iPlateMinSize;		//ST Customized, the image width is very divided, the range is 1~10000, 10000 means the width of the whole screen. When this field is 0, it means no processing.
	    public Int32 iDelayTime;		//Valid when iPushMode == 2. 500ms,1000ms,2000ms
	    public Int32 iTimeSpace;		//Push Pic TimeSpace. 100ms,200ms,300ms,500ms,1000ms,2000ms
    };

    [StructLayout(LayoutKind.Sequential)]
    struct VCASuspendResult
    {
        public Int32 iBufSize;         //structure size
        public Int32 iStatus;          //Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
        public Int32 iResult;          //Result(1:success[indicate that parameters can be set] 2:fail[indicate that parameter is being set and can not set])
        public Int32 iDevType;         //0-IPC, 1-NVR
    };

    [StructLayout(LayoutKind.Sequential)]
    struct PicStreamUploadParam
    {
        public Int32 iSize;            //structure size
        public Int32 iSceneId;         //range:0~15
        public Int32 iRuleNo;          //range:0~7
        public Int32 iPicType;         //Picture type: 0-Background big picture, 1-Small picture
        public Int32 iSnapEnable;      //Send enable:0-not upload, 1-upload
        public Int32 iIsOsd;           //Overlay information: 0-not stack, 1-stack.When iPicType=1,this parameter is invalid.
        public Int32 iQpvalue;         //Snap picture quality range:1-100
        public Int32 iFaceFrameEnable; //Overlay face frame: 0-not stack, 1-stack.When iPicType=1,this parameter is invalid.
    };

    [StructLayout(LayoutKind.Sequential)]
    struct TemperatureScaleType
    {
        public Int32 iSize;
        public Int32 iChanNo;
        public Int32 iTempStandard;    //temperature scale type, 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin 
    };

    [StructLayout(LayoutKind.Sequential)]
    struct SingleBlackbodyParam
    {
        public Int32 iBlackBodyId;          //black body ID
        public Int32 iBlackBodyTemp;        //temperature value * 100
        public Int32 iBlackBodyTempUnit;    //temperature unit: 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin
        public Int32 iBlackBodyDistance;    //unit: cm
        [MarshalAs(UnmanagedType.Struct)]
        public RECT tRect;
    };

    [StructLayout(LayoutKind.Sequential)]
    struct BlackbodyCorrection
    {
        public Int32 iSize;
        public Int32 iChanNo;
        public Int32 iBlackBodyCorrectEnable;    //black body correct enable: 0-disable, 1-enable    
        public Int32 iBlackBodyCorrectType;      //0-reserved, 1-single correction, 2-continuous correction 
        public Int32 iBlackBodyNum;              //black body number: 1 or 2
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        public SingleBlackbodyParam[] tParam;
    };

    [StructLayout(LayoutKind.Sequential)]
    struct BodyTempCorrect
    {
        public Int32 iSize;
        public Int32 iChanNo;
        public Int32 iBodyTempCorrectEnable;     //body temperature correction enable:  0-disable, 1-enable 
        public Int32 iBodyTempCorrectSensitivity;//body temperature correction sensitivity: 0-100
    };

    [StructLayout(LayoutKind.Sequential)]
    struct IntelligentCorretct
    {
        public Int32 iSize;
        public Int32 iChanNo;
        public Int32 iIntelligentCorrectEnable;  //intelligent correction enable: 0-disable, 1-enable
        public Int32 iIntelligentCorrectSensitivity;//intelligent correction correction sensitivity: 0-100
    };

    [StructLayout(LayoutKind.Sequential)]
    struct VCATemDetect
    {
        public Int32 iSize;
        public Int32 iSceneID;                //scene ID(0~31)
        public Int32 iRuleID;                 //rule ID(0~15)
        public Int32 iValid;                  //0:invalid; 1:valid(default:0)
        public Int32 iDisplayTemScaleEnable;  //0:not display 1:display (default:0)
        public Int32 iHighTemColor;           //1:red 2:green 3:yellow 4:blue 5:purple 6:cyan 7:black 8:white (default:1)
        public Int32 iLowTemColor;            //1:red 2:green 3:yellow 4:blue 5:purple 6:cyan 7:black 8:white (default:4)
        public Int32 iModelType;              //1:Ambient temperature difference alarm 2:Environmental high temperature alarm 3:human high temperature alarm	
        public Int32 iTemUnit;                //0:centigrade 1:Fahrenheit
        public Int32 iTemThreshold;           //The threshold value of temperature alarm is determined according to the imodeltype, and the value is the actual temperature * 100 
        public Int32 iWaitTime;               //Unit: Second	
        public Int32 iTempLoseEnable;         //Temperature abnormal alarm enable, 0-disable, 1-enable.(default:0)
    };

    public class SDKConstMsg
    {
        public const int WM_USER = 0x0400; 
        public const int WM_MAIN_MESSAGE = WM_USER + 1001;
        public const int MSG_PARACHG = WM_USER + 90910;
        public const int MSG_ALARM = WM_USER + 90911;
        public const int WCM_ERR_ORDER = 2;
        public const int WCM_ERR_DATANET = 3;
        public const int WCM_LOGON_NOTIFY = 7;
        public const int WCM_VIDEO_HEAD = 8;
        public const int WCM_VIDEO_DISCONNECT = 9;
        public const int WCM_RECORD_ERR = 13;

        public const int LOGON_SUCCESS = 0;
        public const int LOGON_ING = 1;
        public const int LOGON_RETRY = 2;
        public const int LOGON_DSMING = 3;
        public const int LOGON_FAILED = 4;
        public const int LOGON_TIMEOUT = 5;
        public const int NOT_LOGON = 6;
        public const int LOGON_DSMFAILED = 7;
        public const int LOGON_DSMTIMEOUT = 8;
        public const int PLAYER_PLAYING = 0x02;
        public const int USER_ERROR = 0x10000000;

        public const int WCM_QUERYFILE_FINISHED = 18;  
        public const int WCM_DWONLOAD_FINISHED = 19;
        public const int WCM_DWONLOAD_FAULT = 20;

        public const int DSM_CMD_GET_ONLINE_STATE = 0;

        public const int WCM_VCA_SUSPEND = 103;
    };

    public class SDKTypes
    {
        public const int SERVER_NORMAL = 0;
        public const int SERVER_ACTIVE = 1;
        public const int SERVER_DNS = 2;

        public const int DSM_STATE_OFFLINE = 0;
        public const int DSM_STATE_ONLINE = 1;

        public const int MAX_SAVE_PCTURE_COUNT = 2000;
        public const int NET_PICSTREAM_CMD_VCA = 1;
        public const int NET_PICSTREAM_CMD_ITS = 2;
        public const int NET_PICSTREAM_CMD_FACE = 3;

        public const int MAX_FACE_PICTURE_COUNT = 32;

    };

    public class NetClientTypes
    {
        public const int ANYSCENE = 21;
        public const int FACE_DETECT_ARITHMETIC = 26;
        public const int VCA_SUSPEND = 32;
        public const int TEMPERATURE_STANDARD = 263;
        public const int BLACKBODY_CORRECT = 264;
        public const int BODYTEMP_CORRECT = 265;
        public const int INTELLIGENT_CORRECT = 266;
    };

    public class VcaCmd
    {
        public const int MIN = 100;
        public const int PICSTREAM_UPLOADPARAM = 147;
        public const int TEMDETECT = 175;
    };

    public class CommonEnbaleID
    {
        public const int TEMDETECT = 0xB007;            //1-open, 2-close
    };

    public class VCASuspendStatus
    {
        public const int STATUS_PAUSE = 0;              //Pause intelligent analysis
        public const int STATUS_RESUME = 1;             //Recovery intelligence analysis

        public const int RESULT_SUCCESS = 1;            //Intelligent analysis paused successfully
        public const int RESULT_CONFIGING = 2;   		//Intelligent analysis pause failed, setting, can't set parameter
    };

    public class VCATemDetectStatus
    {
        public const int TEM_DETECT_ENABLE = 1;              //Enable human body temperature measurement
        public const int TEM_DETECT_DISABLE = 2;             //Turn off human body temperature measurement enable
    };

    [System.Flags]
    enum EnumFaceAttrInfo
    {
	    FACE_ATTR_Unknow			= -1,
	    FACE_ATTR_Age				= 0,	
        FACE_ATTR_Sex               = 1,	
        FACE_ATTR_Mask              = 2,	
        FACE_ATTR_Beard             = 3,	
        FACE_ATTR_OpenEye           = 4,	
        FACE_ATTR_OpenMouth         = 5,	
        FACE_ATTR_Glasses           = 6,	
        FACE_ATTR_Race              = 7,	
        FACE_ATTR_Expression        = 8,	
        FACE_ATTR_Smile             = 9,	
        FACE_ATTR_Value             = 10,	
        FACE_ATTR_Nation            = 11,
        FACE_ATTR_12 = 12,
        FACE_ATTR_13 = 13,
        FACE_ATTR_14 = 14,
        FACE_ATTR_15 = 15,
        FACE_ATTR_16 = 16,
        FACE_ATTR_17 = 17,
        FACE_ATTR_18 = 18,
        FACE_ATTR_19 = 19,
        FACE_ATTR_20 = 20,
        FACE_ATTR_21 = 21,
        FACE_ATTR_22 = 22,
        FACE_ATTR_23 = 23,
        FACE_ATTR_24 = 24,
        FACE_ATTR_25 = 25,
        FACE_ATTR_26 = 26,
        FACE_ATTR_27 = 27,
        FACE_ATTR_28 = 28,
        FACE_ATTR_29 = 29,
        FACE_ATTR_30 = 30,
        FACE_ATTR_31 = 31,
        FACE_ATTR_32 = 32,
        FACE_ATTR_33 = 33,
        FACE_ATTR_34 = 34,
        FACE_ATTR_35 = 35,
        FACE_ATTR_36 = 36,
        FACE_ATTR_37 = 37,
        FACE_ATTR_38 = 38,
        FACE_ATTR_39 = 39,
        FACE_ATTR_40 = 40,
        FACE_ATTR_41 = 41,
        FACE_ATTR_42 = 42,
        FACE_ATTR_43 = 43,
        FACE_ATTR_44 = 44,
        FACE_ATTR_45 = 45,
        FACE_ATTR_46 = 46,
        FACE_ATTR_47 = 47,
        FACE_ATTR_48 = 48,
        FACE_ATTR_49 = 49,
        FACE_ATTR_50 = 50,
        FACE_ATTR_TEM_VALUE         = 51,
        FACE_ATTR_TEM_UNIT          = 52,
        FACE_ATTR_ABNORMAL_ALARM    = 53,	
    };
    enum EnumFaceAttrName { Age, Sex, Mask, Beard, OpenEye, OpenMouth, Glasses, Race, Expression, Smile, Value, Nation, Type12, Type13, Type14, Type15, Type16, Type17,
    Type18, Type19, Type20, Type21, Type22, Type23, Type24, Type25, Type26, Type27, Type28, Type29, Type30, SceneFaceRollCall, Type32, FaceAttRCredibility, Type34, Type35, Type36, Type37, Type38, Type39, Type40, Type41, Type42,
    Type43, Type44, Type45, Type46, Type47, Type48, Type49, Type50, TemValue, TemUnit, AbnormalAlarm
    };

    [System.Flags]
    enum EnumAttrSex
    {
	    ATTR_SEX_Unknow				= -1,
	    ATTR_SEX_Female   			= 0,  	
	    ATTR_SEX_Male       		= 1,	
    };
    enum EnumAttrSexName {Female, Male};


    [System.Flags]
    enum EnumAttrCommon
    {
	    ATTR_COMMON_Unknow			= -1,
	    ATTR_COMMON_No       		= 0,  	
	    ATTR_COMMON_Yes     		,  		
    } ;
    enum EnumAttrCommonName {No, Yes};

    [System.Flags]
    enum EnumAttrGlasses
    {
	    ATTR_GLASSES_Unknow			= -1,
	    ATTR_GLASSES_No   			= 0,  	
	    ATTR_GLASSES_Wear       	,  		
	    ATTR_GLASSES_Sunglasses    	,  		
    } ;
    enum EnumAttrGlassesName { No, Wear, Sunglasses };

    [System.Flags]
    enum EnumAttrRace
    {
	    ATTR_RACE_Unknow			= -1,
	    ATTR_RACE_Yellow   			= 0,  	
	    ATTR_RACE_Black     		,  		
	    ATTR_RACE_White    			,  		
	    ATTR_RACE_Uygur    			,  		
    } ;
    enum EnumAttrRaceName { Yellow, Black, White, Uygur };
  
    [System.Flags]
    enum EnumAttrExpression
    {
	    ATTR_EXPRESSION_Unknow		= -1,
	    ATTR_EXPRESSION_Angry     	= 0,  	
	    ATTR_EXPRESSION_Calm      	,  		
	    ATTR_EXPRESSION_Confused  	,  		
	    ATTR_EXPRESSION_Disgust   	,  		
	    ATTR_EXPRESSION_Happy     	,  		
	    ATTR_EXPRESSION_Sad       	,  		
	    ATTR_EXPRESSION_Scared    	,  		
	    ATTR_EXPRESSION_Surprised 	,  		
	    ATTR_EXPRESSION_Squint    	,  		
	    ATTR_EXPRESSION_Scream    	,  		
    } ;
    enum EnumAttrExpressionName { Angry, Calm, Confused, Disgust, Happy, Sad, Scared, Surprised, Squint, Scream };

    [System.Flags]
    enum EnumAttrSmile
    {
	    ATTR_SMILE_Unknow			= -1,
	    ATTR_SMILE_No       		= 0,  	
	    ATTR_SMILE_Yes     			,
    } ;
    enum EnumAttrSmileName { No, Yes };

    [System.Flags]
    enum EnumAttrNation
    {
	    ATTR_NATION_Unknow			= -1,
	    ATTR_NATION_Han       		= 0,  	
	    ATTR_NATION_Other     		,  		
    } ;
    enum EnumAttrNationName { Han, Other };

    [System.Flags]
    enum EnumAttrTemUnit 
    { 
        ATTR_TEMUNIT_Unknow         = -1,
        ATTR_TEMUNIT_Preserve       = 0,
        ATTR_TEMUNIT_Centigrade     = 1,
        ATTR_TEMUNIT_Fahrenheit     = 2,
    };
    enum EnumAttrTemUnitName { Preserve,Centigrade, Fahrenheit };

    [System.Flags]
    enum EnumAttrAbnormalAlarm
    {
        ATTR_ABNORMALALARM_Unknow         = -1,
        ATTR_ABNORMALALARM_Preserve       = 0,
        ATTR_ABNORMALALARM_HighTemAlarm   = 1,
    };
    enum EnumAttrAbnormalAlarmName { Preserve, HighTemperatureAlarm };
}
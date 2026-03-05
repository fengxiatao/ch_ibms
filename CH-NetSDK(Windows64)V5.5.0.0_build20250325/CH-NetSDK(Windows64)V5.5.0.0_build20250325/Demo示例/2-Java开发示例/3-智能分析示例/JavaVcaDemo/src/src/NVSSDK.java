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
	public static final int ALARM_VCA_INFO_EX = 9;//VCA alarm information

	public static final int T_AUDIO8 = 0;
	public static final int T_YUV420 = 1;
	public static final int T_YUV422 = 2;
	
	public static final int NET_PICSTREAM_CMD_VCA = 1;	//Callback VCA image stream information
	public static final int NET_PICSTREAM_CMD_ITS = 2;	//Callback ITS image stream information
	public static final int NET_PICSTREAM_CMD_FACE = 3;	//Callback face image stream information
	public static final int NET_PICSTREAM_CMD_NORMALSNAP = 4;	//Callback normal snap image stream information

	public static final int  WCM_NORTH_ANGLE = 223;
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
	public static class WaterDevReportInfo extends Structure{
		public int		iChanNum;		//Channel number
		public int		iIndex;			//Site label
		public int		iSceneID;		//Scene id
		public int		iRuleID;		//Rule ID
		public int		iDataSource;	//Data source, 0-algorithm, 1-peripheral
		public int		iType;			//1: rainfall,2: Rain time,3: Water level,4: water depth,5: Super warning water level value,6: flow rate,7: Battery remaining capacity,8: Air pressure,9: Wind speed,10: Wind direction,11: temperature,12: Humidity,13: pH,14: Dissolved oxygen,15: redox,16:GPS,17: elevation
		public int		iValue;			//
	}
	
	
	public static class PtzInfo extends Structure{
		public int		iPanPosition;		
		public int		iTiltPosition;		
		public int		iZoomPosition;	
		public int		iNorthAngle;	
		public int		iPanStep;	
		public int		iTiltStep;			
	}
	
	public static class NorthAngle extends Structure{
		public int iSize;
		public int iAngle;
		public int iStatus;
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
		public int iSize;					//The size of strcut PicData
		public Pointer[] tPicData = new Pointer[3]; 
		public int iAlarmType;
		public int iHelmetStatus;
		public int iHelmetColor;
		public int iAlertTypeParam;
		public byte[] cTemp = new byte[32];
		public int iWaterInfoLen;
		public WaterDevReportInfo  tWaterDevReportInfo;
		public int iPtzInfoLen;
		public PtzInfo        tPtzInfo;
		
	}
	
    public static class PICSTREAM_INFO extends Structure {

        public byte[] RecvBuffer = new byte[200*1024];//400 here should not be less than the maximum alarm message length
    }
    
	public static class NVS_FILE_TIME extends Structure implements ByValue {
		public short 		iYear;   				// Year
		public short 		iMonth;  				// Month
		public short 		iDay;    				// Day
		public short 		iHour;   				// Hour
		public short 		iMinute; 				// Minute
		public short 		iSecond; 				// Second
	}
    
	public static class IrrigationPara extends Structure {
		public int				iSize;
		public int				iType;				//Irrigation type 1:rainfall;2:rainfall duration;3:water level;4:water depth;5:over-warning water level;6:flow velocity;7:remaining battery power;8:air pressure;9:wind speed;10:wind direction; 11:temperature;12:humidity;13:pH;14:dissolved oxygen;15:redox;16:GPS;17:elevation;18:turbidity;19:ammonia nitrogen;20:water temperature;21:conductivity;22: chemical oxygen demand;23:total nitrogen;24:total phosphorus;25:flow field;
		public int				iValue;				//Param2 iType(1,3,4,5)-->Unit:mm;  iType(2)-->Unit:minute ;iType(6)-->Unit:mm/s;iType(7,12)-->Unit:%;
												//		 iType(8)-->Unit:hpa; iType(9)-->Unit:m/s; iType(10)-->Unit:angle degree;iType(11)-->Unit:degree centigrade;	
		public int				iSrc;				//Param3  0:algorithm acquisition; 1:peripheral acquisition
		public int				iSceneID;			//Param4  Scene ID
		public int				iRuleID;			//Param5  Rule ID
		public NVS_FILE_TIME	tUploadTime;		//the time of data uploading
		public int				iStationNum;		//Param7  the station No.(when the type is flowSpeed or WaterLevel)
		public int 	            iCommonData1;		//Param8  when the type is flowspeed,this para represents cruise times;when the type is WaterLevel, it represents if data is valid
		public int	            iCommonData2;		//Param9  when the type is flowspeed,this para represents cruise times;when the type is WaterLevel, it represents if data is valid
		public int				iTotalPointNum;		//Param10 (the total detect numbers)
		public int				iCurrentPointNum;	//Param11 (the Current detect Number)
		public int				iBaseNum;			//Param12 the distance of baseLine(when the type is flowSpeed)
		public NVS_FILE_TIME	tRecordTime;		//start time of Record file(when the type is flowSpeed)
		public byte[]			cFactoryID = new byte[64];
		public int				iIrriParam6;		//Param6  when iType=25,means Detection area number
		public int				iIrriParam13;		//Param13 when iType=25,means Pixel end abscissa
		public int				iIrriParam15;		//Param15 when iType=25,means Pixel end ordinate
		public int				iIrriParam16;		//Param16 when iType=25,means Start abscissa of the space point
		public int				iIrriParam17;		//Param17 when iType=25,means Start ordinate of space point
		public int				iIrriParam18;		//Param18 when iType=25,means Termination abscissa of space point
		public int				iIrriParam19;		//Param19 when iType=25,means Spatial point end ordinate
		public long				ulLastTimeStamp;	//Param20 when iType=25,means Last frame timestamp
		public long				ulCurTimeStamp;		//Param21 when iType=25,means Current frame timestamp
	};
	
	public static class STR_Para extends Structure {
	    public long[] 	m_iPara = new long[10];
	    public byte[] 	m_cPara = new byte[33];
	};
	

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

	public static final int MAX_LOCAL_NVR_TARGET_NUM = 4;
	public static final int MAX_LOCAL_NVR_REGION_NUM = 8;
	
	public static class TargetResult extends Structure {
		public int				iAlarmState;
		public int				iTargetID;
		public int				iTargetType;
		public int				iLeft; 
		public int				iTop; 
		public int				iRight; 
		public int				iBottom; 	
	}

	public static class RegionInfo extends Structure {
		public int				iTargetNum;
		public TargetResult[] tTargetResult= new TargetResult[MAX_LOCAL_NVR_TARGET_NUM];
	};
	
	public static class vca_TAlarmInfo extends Structure {
		public int iID;					// alarm message ID, used to obtain specific information
		public int iChannel;				// channel number
		public int iState;					// alarm status
		public int iEventType;				// Event type 0: single trip line 1: double trip line 2: perimeter detection 3: wandering 4: parking 5: running
									// 6: Personnel density in the area 7: Stolen objects 8: Discards 9: Face recognition 10: Video diagnosis
									// 11: intelligent tracking 12: traffic statistics 13: crowd gathering 14: leave the job detection 15: audio diagnosis
		public int iRuleID;				// Rule ID, iEventType=54: 0--Reserve 1--No Chef's Cap 2--No Mask 3--No Chef's clothes

		public int uiTargetID;	// Destination ID
		public int iTargetType;			// target type
		public RECT rctTarget;				// Destination location
		public int iTargetSpeed;			// target speed
		public int iTargetDirection;		// target direction
		public int iPresetNo;				// Preset ID 
		public int iWaterLevelNUm;// surface scale readings
		public byte[] cWaterPicName = new byte[64]; // Save the picture path
		public int iPicType;				// 0: Key 1: Ordinary
		public int iDataType;				// 0: real-time 1: offline
		public int	iHelmetInfo;
		public int	iSenceID;
		public RegionInfo	tRegionPara;	//Region parameter					
		public int	iRegionNum;				//Follow up Region Number				
		public RegionInfo[] 	tRegionInfo = new RegionInfo[MAX_LOCAL_NVR_REGION_NUM];
		public int iAlarmType;				//iEventType =37,0:reserved 1:single inquiry 2:attended.
									//iEventType =51,0: Reserved 1: Zone Invasion 2: Unusual Residency
		public byte[] cPicUuid = new byte[64];	//iEventType =54 uuid
		public int				iPtzP;		//iEventType =0,1,2,62,64
		public int				iPtzT;		//iEventType =0,1,2,62,64
		public int				iPtzZ;		//iEventType =0,1,2,62,64
		public int				iAngle;		//iEventType =0,1,2,62,64
		public int				iHView;		//iEventType =0,1,2,62,64
		public int				iVView;		//iEventType =0,1,2,62,64
		public byte[] 			cAlarmTime = new byte[32];	//Alarm time
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

	int NetClient_CmdConfig(int mILogonID, int cmdNorthAngle, int iChan,
			NorthAngle tInPara, int iInSize, NorthAngle tInfo, int iOutSize);
	
	int NetClient_VCAGetAlarmInfo(int _iLogonID, int _iAlarmIndex, Pointer _lpBuf, int _iBufSize);
}

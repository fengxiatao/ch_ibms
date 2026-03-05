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
	
	public static final int MAX_CAR_COLOR = 32;
	public static final int MAX_CAR_PLATE_COLOR = 6;

	public static final int LEN_32 = 32;
	public static final int LEN_64 = 64;
	public static final int LEN_16 = 16;
	public static final int LEN_8 = 8;
	public static final int LEN_36 = 36;
	public static final int LEN_128 = 128;
	
	public static class RECT_S extends Structure implements ByValue {
			public short 			sLeft; 
			public short 			sRight; 
			public short 			sTop; 
			public short 			sBottom; 
		}
			
	public static class STRCT_BRAND extends Structure
	{
		public   int	uiBrandType;		//Vehicle Brands
		public  int	uiSubBrand;			//Vehicle sub-brand
		public  int	uiSubBrandYearStart;//Vehicle brand year model example: 2010-2013 or 2010
		public  int	uiSubBrandYearEnd;
		RECT			stRectLogo;			//Car logo coordinate rectangle
		public  int	uiConfid;			//Confidence
	}
		
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
	
	
	public static class ItsPicStream extends Structure {
			
		public int			iStructLen;				//Structure length
		public int   		iChannelID;				//Lane number
		public byte[] 		cPlate = new byte[32];			//License plate
		public int   		iPlateColor;			//License plate color		
		public int   		iPlateType;				//License plate type
		public int	  		iCarColor;				//the color of car
		public RECT			tPlateRange;			//License plate range
		public int   		iCharNum;				//The number of characters in the license plate
		public int[]   		iCharConfid = new int[12];		//Each character confidence, up to 12 characters
		public int   		iPlateConfid;						//The entire license plate confidence
		public int   		iRecrdoNum;							//Identify picture serial number
		public float 		fSpeed;					//Vehicle speed
		public int 			iVehicleDirection;		//Direction of the vehicle
		public int			iAlarmType;				//Alarm type
		public byte[]  		cCameraIP = new byte[16];		//Camera IP
		public int			iRedBeginTime;			//Red light start time in seconds
		public int			iRedEndTime;			//Red light end time in seconds
		public int   		iPicCount;				//Record the number of images included
		public int			iSize;
		public Pointer[]	tPicData = new Pointer[8];
		public int			iPlatCount;
		public Pointer		tPlatData;
		public int			iFaceCount;
		public Pointer[]	tFaceData= new Pointer[3];
	
		public int			iPreset;							//(The Ball ) preset position number 				
	    public int			iArea;								//(The Ball )Area number               			
	    public byte[]		cFileName = new byte[LEN_32];					//Video file name 
	    public int			iCarSerialNum;						//No. of parked vehicle 				
	    public int			iPictureNum;						//No. of pictures of vehicles in violation of parking regulations 				
	    public int			iPicType;							//Invalid field, no actual meaning 					
	    public int			iPlatePicLen;						//Data length of small license plate 					
	    public int			iFacePicNum;						//Number of small face pictures 				
	    public byte[] 		cChannelName = new byte[LEN_64];				//Lane name 		
		public byte[]		cChannelDirection = new byte[LEN_64];			//Lane direction 
		public byte[]		cCrossingID = new byte[LEN_64];				//Intersection number 			
		public byte[]		cCrossingName = new byte[LEN_64];				//Intersection name 		
		public int			iCarRegionNum;						//Vehicle contour number, which determines which image tcarregion is based on, starting from 0, where 0 represents the first one. 			
		public RECT		tCarRegion;							//Vehicle contour coordinates, icarregionnum determines which image tcarregion is based on. Icarregionnum starts from 0, where 0 represents the first image. 				
		public int			iTargetType;						//Target type machine is not human ,Car model ( It is not recommended to use this field ,Suggested use iNewTargetType) 					
		public int[]			iFacePicLen = new int[3];						//Face small graph length 				
		public int			iRecordAttr;						//Record attribute 0- General record ,1- Take the first record before and after ,2- Second records before and after capture 				
		public int 		iLinkageChannelNo;					//Lane numbers for associated cameras ,Use before and after snapping 				
		public int			iLinkageNo;							//Linkage number, greater than 0 random number, the same camera linkage number before and after the same capture record, valid before and after pure video capture, others are - 1  		
		public byte[]	cLinkageCameraIP = new byte[LEN_16];			//Linkage camera IP,Use before and after snapping 	
		public int			iVehicleBrand;						/*Auto Logos ,0:Unknown ;1:Public ;2:Audi ;3:Toyota ;4:Honda ;5:Benz ;6:Chevrolet ;7:Chery ;8:Buick ;9:The Great Wall ;10:modern ;11:NISSAN ;12:Ford ;
														  13:BMW ;14:Citroen ;15:kIa ;16:Suzuki ;17:Mazda ;18:BYD ;20:Beautiful ;21:Changan ;22:LEXUS ;23:The ZH people ;24:SKODA ;25:A hippocampus ;26:Charade ;
														  27:Wuling ;28:East wind ;29:Hafei ;30:FAW ;31:Bao Jun ;32:The Emperor ;33:The name of the name ;34:Southeast ;35:An crown ;36:Golden cup ;37:Mitsubishi ;38:Roewe ;
														  39:Auspicious ;40:English ;41:global hawk ;42:Hafei Saibao ;43:Changfeng ;44:Wei Wang ;45:Beijing ;46:New Kay ;47:Gio ;48:Maserati ;49:rover ;50:Austen ;51:Ma's ;
														  52:dodge ;54:General Pontiac ;55:Jaguar ;56:Ozzy's dawn ;57:alpha ;58:Lamborghini ;59:Bugatti ;60:Lincoln ;61:Ferrari ;62:Changhe ;63:Fiat ;64:Fukuda ;65:Eulogize ;
														  66:Lotus flower ;67:Huapu ;68:red flag ;69:Riich ;70:Pentium ;71:Wei Lin ;72:Zhongtai ;73:Lifan ;74:Beijing jeep ;75:ZTE ;76:karry ;77:Land Rover ;78:Maybach ;
														  79:Renault ;80:Opel ;81:Wild horse ;82:Jeep ;83:Iveco ;84:Infiniti ;85:Subaru ;86:Aston Martin ;87:An Kai ;88:Porsche ;89:Bentley ;90:Fudi ;91:Fujian ;92:Jim ;93:View ;
														  94:Wide steam ;95:Shuanglong ;96:Hagrid ;97:Hummer ;98:Huatai ;99:Yellow Sea ;100:Kowloon ;101:Idea ;102:SMART;103:Land wind ;104:Na Ji Jie ;105:Oley ;106:Kai Chen ;
														  107:Haversian ;108:HOWO ;109:The crowd ;110:golden dragon ;111:Golden brigades ;112:Yangtze and Huai rivers ;113:Jiangling ;114:Cadillac ;115:Cheetah ;116:Mini ;117:Shaanxi steam ;
														  118:Shaolin Temple ;119:Volvo ;120:Isuzu ;121:Leap forward ;122:Yutong ;123:Zhong Tong ;124:Jinling ;125:Shen wo ;126:Yangzi River ;127:Tang Jun Ou Ling ;128:North Run ;129:Red Crag ;
														  130:Rolls-Royce ;131:Tesla ;132:Teng potential ;133:Field ;134:English ;135:West Yat ;136:Star ;137:Daewoo ;138:Alex ;139:Kaye ;140:chase ;141:Shi Ming ;142:Kama. */
		public RECT_S	tFaceRegion;						//Face coordinates 				
		public int			iAlgIllgalType;						//Algorithm detecting illegal types ,bit0 No seat belt ,bit1 Phone 	
		public int			iAlarmCode;							//Illegal code,Old field, no longer maintained, please use cAlarmCode. 
		public short		sMergePicNum;						//Picture synthesis information ,Photo synthesis number 
		public short		sMergeType;							//Bit synthesis 
		public short		sMergePercent;						//Synthesis ratio 
		public short		sMergeOSDSize;						//Synthetic black edge height 
		public short		sMergeOSDType;						//Synthetic black edge position ,0- Overlay in picture 1- Overlay in black box under picture 2- Overlay in black box on picture 
		public short		sSingleOSDSize;						//Single black edge height 			
		public short		sSingleOSDType;						//Single black edge position ,0- Overlay in picture 1- Overlay in black box under picture 2- Overlay in black box on picture 
		public short		sOriginalImgWidth;					//Source image width 
		public short		sOriginalImgHeight;					//Source image height 
		public RECT_S		tVehicleRegion;						//Vehicle logo coordinates, this field is valid only when supporting and opening the vehicle logo recognition function, indicating the coordinates of the vehicle logo, the image serial number based on iRecrdoNum determines, otherwise the field is invalid.
		public RECT_S		tCopilotRegion;						//Face coordinates of copilot 	
		public byte[]  		cCaptureTimeEx2 = new byte[5*LEN_8];			//Capture time of each picture: year, month, day, week, hour, minute, second and millisecond. 
		public int[]	  		iCaptureLenEx2 = new int[5];					//The length of each picture.
		public byte[]		cUserDefChannelID = new byte[LEN_64];			//Lane customization number 	
		public short		sBodyOfCarLeight;					//Body length (cm)      
		public byte[]		cSecurityCode = new byte[LEN_8*LEN_36];		//Image security code 
		public int			iSpeedPercent;						//Percentage of overspeed 
		public byte[]		cLinkPanoramaCapUUID = new byte[LEN_128];		//Linkage panoramic camera snapping linkage UID 
		public int			iRedBeginTimeMS;					//Red light start time ( Millisecond ) 
		public int			iRedEndTimeMS;						//Red End ( Millisecond )	
		public int			iIPDCapType;						//Violation of stop the illegal record capture type ,0 automatic ,1 Manual 			
		public byte[]		cAlarmCode = new byte[LEN_16];					//Illegal code compatibility letter 
		public int[] 		iCarFeatures = new int[2];					/*Vehicle characteristic attributes, such as hazardous chemicals vehicle, tissue box, etc., by bit, 64 bit is supported. Bit0: do not wear safety belt, Bit1: make a phone call, bit2: main driver's sunshade, bit3: copilot's sunshade, 
														  bit4: dangerous chemicals vehicle, bit5: yellow label vehicle, bit6: hanging, bit7: tissue box, bit8: annual inspection label, bit9: high beam lamp, bit10: abnormal license plate, bit11: window standing person.*/ 
		public STRCT_BRAND	stBrandInfo;						//Vehicle label information 
		public int 		iMainDriverSex;						//Driving sex ,0:Unknown ;1:Male ;2:Female sex. 
		public int  		iCopilpt;							//Co driving sex ,0:Unknown ;1:Male ;2:Female sex. 
		public int			iNewTargetType;						/*Vehicle type ,0:Unknown model ;1:Hatchback car ;2:Sedan ;3:Coupe ;4:Small car ;5:Mini car ;6:MPV;7:SUV;8:Large bus ;9:Medium-sized passenger car ;10:Van ;11:Mini van ;12:Big truck ;13:Medium truck ;14:Fuel tank car ;15:crane ;
														  16:Part of the car ;17:Van ;18:Pickup ;19:Micro card ;20:dogcart ;21:Tricycle ;22:pedestrian ;23:License plate deviation ;24:License plate detection ;25:Head of a car ;26:Tail of a car ;27:Automative lighting ;28:SUV/ MPV;29:trailer;
														  100: bicycle; 101: electric vehicle; 102: motorcycle.*/ 
		public int 		iRealImgWidth;						//image width ( Containing synthetic pictures )
		public int 		iRealImgHeight;						//Image height ( Containing synthetic pictures )
		public int			iBWPlatePicLen;						//The data length of small license plate picture after two-value 
		public Pointer		pcBWPlateData;						//Small license plate picture data after two-value 
		
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
}

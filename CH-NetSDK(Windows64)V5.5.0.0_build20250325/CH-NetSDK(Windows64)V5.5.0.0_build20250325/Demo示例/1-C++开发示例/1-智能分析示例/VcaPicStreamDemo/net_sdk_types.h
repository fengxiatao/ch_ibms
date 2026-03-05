
#ifndef _NET_SDK_TYPES_H_
#define _NET_SDK_TYPES_H_

#ifndef LEN_2
#define LEN_2   2
#endif

#ifndef LEN_4
#define LEN_4   4
#endif
#ifndef LEN_6
#define LEN_6   6
#endif

#ifndef LEN_8
#define LEN_8   8
#endif


#ifndef LEN_10
#define LEN_10	10
#endif

#ifndef LEN_15
#define LEN_15	15
#endif

#ifndef LEN_16
#define LEN_16  16
#endif

#ifndef LEN_17
#define LEN_17  17
#endif

#ifndef LEN_20
#define LEN_20  20
#endif

#ifndef LEN_24
#define LEN_24  24
#endif

#ifndef LEN_31
#define LEN_31  31
#endif

#ifndef LEN_32
#define LEN_32  32
#endif

#ifndef LEN_33
#define LEN_33  33
#endif

#ifndef LEN_36
#define LEN_36  36
#endif

#ifndef LEN_64
#define LEN_64  64
#endif

#ifndef LEN_65
#define LEN_65	65
#endif

#ifndef LEN_96
#define LEN_96	96
#endif

#ifndef LEN_128
#define LEN_128 128
#endif

#ifndef LEN_250
#define LEN_250	250
#endif

#ifndef LEN_256
#define LEN_256 256
#endif


#ifndef LEN_255
#define LEN_255 255
#endif

#ifndef LEN_300
#define LEN_300 300
#endif

#ifndef LEN_512
#define LEN_512 512
#endif

#ifndef LEN_1024
#define LEN_1024 1024
#endif

#ifndef MAX_TOTAL_CHANNEL_NUM
#define MAX_TOTAL_CHANNEL_NUM	128
#endif

#ifndef LEN_UUID
#define LEN_UUID 64
#endif

#ifndef MAX_SCHEDULE
#define MAX_SCHEDULE 8
#endif

#ifndef MAX_TIMESLOTNUM
#define MAX_TIMESLOTNUM	16
#endif

#ifndef MAX_AREA_COUNT
#define MAX_AREA_COUNT				16
#endif

#ifndef MAX_WEEK_DAYS
#define MAX_WEEK_DAYS   7
#endif

#define LENGTH_IPV4					16	
#define LENGTH_IPV6					32
#define LENGTH_IPV6_V1				64
#define LENGTH_SUB_MASK				16
#define LENGTH_MAC_ADDR     		18
#define MAX_IPADDRESS_LEN			64

#define MAX_QUERY_PAGE_COUNT					20
#define MAX_QUERY_VCA_CONDITION					32
#define MAX_VCA_FILE_COUNT						5
#define MAX_VCA_ATTR_COUNT						32
#define MAX_QUERY_LIST_COUNT					32

#define MAX_IP_FILTER_NUM				48
#define MAX_IPV6_PACKAGE_FILTER_NUM		16

#ifndef MAIN_STREAM
#define MAIN_STREAM								0
#endif

#ifndef SUB_STREAM
#define SUB_STREAM								1
#endif

#define BOUNDAY_MAIN							10000
#define BOUNDAY_SUB								1000000

#define FACE_MAX_PAGE_COUNT						20
#define FACE_MAX_KEY_COUNT_OLD					33
#define FACE_MAX_KEY_COUNT						300
#define FACE_MAX_FEATURE_COUNT					10
#define FACE_MAX_RECT_POINT_COUNT				10

////1：通道名称（文本） 2：时间日期 3:logo颜色 4：附加字符 5:交通专用 6：交通合成图片 7：人脸叠加OSD
#define COMMON_OSD_TYPE_CHANNEL     1
#define COMMON_OSD_TYPE_DATETIME    2
#define COMMON_OSD_TYPE_LOGOCOLOR   3
#define COMMON_OSD_TYPE_ADDITION    4
#define COMMON_OSD_TYPE_ITS         5
#define COMMON_OSD_TYPE_ITSMIX      6
#define COMMON_OSD_TYPE_FACEOSD     7
#define MAX_COMMON_OSD_TYPE         8

typedef struct
{
	unsigned short 		iYear;   				/* Year */
	unsigned short 		iMonth;  				/* Month */
	unsigned short 		iDay;    				/* Day */
	unsigned short 		iHour;   				/* Hour */
	unsigned short 		iMinute; 				/* Minute */
	unsigned short 		iSecond; 				/* Second */
} NVS_FILE_TIME,*PNVS_FILE_TIME;

typedef struct tagQueryChanNo
{
	int 				iChanNo;
	int 				iStream;
}QueryChanNo, ChannelList,*PChannelList,*PQueryChanNo;

typedef struct tagNetFileQueryVca
{
	int					iSize;				//结构体大小				
	int					iChanCount;         //通道个数
	int					iChanSize;			//结构体QueryChanNo大小
	QueryChanNo*		pChanList;			//通道列表
	int					iVcaCount;			//智能分析算法个数
	int					iVcaList[MAX_QUERY_LIST_COUNT];		//智能分析算法列表
	NVS_FILE_TIME		tBegTime;			//开始时间
	NVS_FILE_TIME		tEndTime;			//结束时间
	int					iPageCount;			//页大小
	int					iPageNo;			//页编号
	int					iFileType;			//文件类型， 0：全部，1：录像，2：图片
	int					iConditionCount;	//查询条件个数, 最大32， 当值为 0 时，表示查询全部
	char				cQueryCondition[MAX_QUERY_VCA_CONDITION][LEN_256];	
/*
   当智能分析类型为9时: 
	cQueryCondition[0]：表示检索类型 0-按特征检索，1-按事件检索，2-按图片检索;
	cQueryCondition[1]：cQueryCondition[0]为0时 表示年龄，1-少年，2-青年，3-中年，4-老年;
	cQueryCondition[0]为1时 1-人脸检测 2-人脸比对 3-陌生人 4-频次 5-时长;
	cQueryCondition[2]：cQueryCondition[0]为0时 表示性别，1-男，2-女，3-未知; cQueryCondition[0]为2时 表示操作 1-查询 2-获取结果;
	cQueryCondition[3]：cQueryCondition[0]为0时 表示民族，1-汉族，2-少数民族; cQueryCondition[0]为2时 表示图片类型 1-指定文件 2-从人脸库中选择文件;
	cQueryCondition[4]：cQueryCondition[0]为0时 表示姓名; cQueryCondition[0]为2, cQueryCondition[2]为1时 cQueryCondition[3]为1时 表示图片名称; cQueryCondition[0]为2, cQueryCondition[2]为1时 cQueryCondition[3]为2时 人脸库图片Id;
	cQueryCondition[5]：cQueryCondition[0]为0时，表示戴眼镜 0-预留，1-佩戴，2-未佩戴 cQueryCondition[0]为2，cQueryCondition[2]为1时 表示相似度
	cQueryCondition[6]：cQueryCondition[0]为0时，表示戴口罩 0-预留，1-佩戴，2-未佩戴 cQueryCondition[0]为2，cQueryCondition[2]为1时 表示排序方式 0-按时间排序 1-按相似度排序
   当iVcaType = 22时
	cQueryCondition[0]:表示目标，0x7FFFFFFF-全部，1-人，2-其它，3-车
	cQueryCondition[1]:表示类型, 0x7FFFFFFF-全部，1-周界-入侵，2-周界-离开,3-绊线
   当iVcaType = 23时
	cQueryCondition[0]:表示车牌号码
   当iVcaType = 30时
	cQueryCondition[0]:表示安全帽颜色 0x7FFFFFFF-全部,1-红色，2-黄色，3-蓝色，4-白色，5-其它
	cQueryCondition[1]:表示安全帽未佩戴，0-预留，1-佩戴，2-未佩戴
  */
}NetFileQueryVca, *pNetFileQueryVca;

typedef struct tagVcaFileAttr
{
	int					iFileIndex;				//序号：多张图时，每个图分配一个序号，从10001开始，可对序号做校验。
	char				cFileName[LEN_256];		//图片名称
	int					iFileSize;				//图片名称
	int					iFileType;				//图片类型：1-小图，2-大图 
	int					iReserve;				//保留字段
	char				cReserve[LEN_64];		//保留字段
}VcaFileAttr, *pVcaFileAttr;

typedef struct __tagVcaTPoint
{
	int iX;
	int	iY;
} vca_TPoint;

typedef struct __tagVca_TLine
{
	vca_TPoint 	stStart;
	vca_TPoint 	stEnd;
} vca_TLine;						//	sizeof = 2*8 = 16

#ifndef VCA_MAX_POLYGON_POINT_NUM
#define VCA_MAX_POLYGON_POINT_NUM				16	//The maximum number of points allowed per polygon
#endif
typedef struct __tagVca_TPolygon
{
	int 		iPointNum;
	vca_TPoint 	stPoints[VCA_MAX_POLYGON_POINT_NUM];   
} vca_TPolygon;						//	sizeof = 16*8+4 = 132 

#ifndef VCA_MAX_POLYGON_POINT_NUMEX
#define VCA_MAX_POLYGON_POINT_NUMEX				32	//Max Tripwire Points Number
#endif
typedef struct __tagVca_TPolygonEx
{
	int 		iPointNum;
	vca_TPoint 	stPoints[VCA_MAX_POLYGON_POINT_NUMEX];   
} vca_TPolygonEx;

typedef struct __tagTPoint
{
	int iX;
	int	iY;
} TPoint;

typedef struct tagCommonRECT 
{ 
	int left; 
	int top; 
	int right; 
	int bottom; 
} CommonRECT, *pCommonRECT;

typedef struct tagNetFileQueryVcaResult
{
	int					iSize;
	int					iChanNo;
	int					iFileAttrCount;
	VcaFileAttr			tFileAttr[MAX_VCA_FILE_COUNT];

	int					iTotal;
	int					iCurPageCount;
	int					iItemIndex;
	int					iFileType;
	int					iVcaType;
	NVS_FILE_TIME		tBegTime;
	NVS_FILE_TIME		tEndTime;
	
	int					iExAttrCount;
	char				cExAttr[MAX_VCA_ATTR_COUNT][LEN_256];
}NetFileQueryVcaResult, *pNetFileQueryVcaResult;

typedef struct tagIpcDevAttr
{
	int					iSize;
	int					iType;
	int					iParam;
}IpcDevAttr, *pIpcDevAttr;


typedef struct tagUniqueAlertAlarmStatClr
{
	int					iSize;
	int					iSceneId;
	int					iTypeEnable[LEN_32];	//Whether the current alert event needs to eliminate the state of the alarm
	int					iEventNo;
}UniqueAlertAlarmStatClr,*pUniqueAlertAlarmStatClr;

typedef struct tagITS_LightStatus
{
	int					iSize;
	int					iLaneNo;
	int					iLightType;
	int					iStatus;
}ITS_LightStatus, *pITS_LightStatus;

typedef struct tagNetBitRate
{
	int					iSize;
	int					iMaxBw;					//NIC maximum capacity bandwidth
	int					iMaxRxBw;				//maximum access bandwidth
	int					iMaxTxBw;				//maximum forwarding bandwidth
} NetBitRate, *pNetBitRate;

typedef struct tagSipLocalServer
{
	int					iSize;
	int					iServerPort;			//local server port,default 5060
	char				cServerID[LEN_64];		//local server ID
	int					iHeartBeatInterval;		//heart beat interval,Unit: s
	int					iHeartBeatTimes;		//heart beat times
} SipLocalServer, *pSipLocalServer;

#define MAX_MACFILTER_COUNT			32
#define MAC_FILTERTYPE_DISAUTH		0			//Disable MAC authentication
#define MAC_FILTERTYPE_DISACCESS	1			//Disable MAC access
#define MAC_FILTERTYPE_ALLOWACCESS	2			//Allow MAC access
typedef struct tagMacAddrFilter
{
	int					iSize;
	int					iFilterType;			//filter type:0-Disable MAC authentication; 1-Disable MAC access; 2-Allow MAC access
	int					iMacCount;				//mac count,supports up to 32 different MAC addresses
	char				cMacAddr[MAX_MACFILTER_COUNT][LEN_64];	//MAC address string
} MacAddrFilter, *pMacAddrFilter;


#define VPN_VIRNETCARD_TUN			0
#define VPN_VIRNETCARD_TAP			1

#define VPN_PROTOCOL_TCP			0
#define VPN_PROTOCOL_UDP			1

#define VPN_COMPRESS_DISABLE		0
#define VPN_COMPRESS_LZO			1
#define VPN_COMPRESS_LZ4V2			2

#define VPN_CERTIFICATE_AUTH		0
#define VPN_USERNAMEPWD_AUTH		1
#define VPN_CERT_NAMEPWD_AUTH		2

#define CERT_USAGE_DEFAULT			0
#define CERT_USAGE_CUSTOME			1
typedef struct tagVpnParam
{
	int					iSize;
	int					iEnable;				//Enable switch:0-disable openvpn, 1-enable openvpn
	char				cServerIp[LEN_64];		//VPN server address
	int					iServerPort;			//VPN server port
	int					iVirNetCardType;		//Virtual network card type:0-TUN, 1-TAP
	int					iProtoType;				//Transfer Protocol:0-TCP, 1-UDP
	int					iTlsEnable;				//Whether to enable ta.key attack defense:0-disable, 1-enable
	int					iCompress;				//Compression method:0-Not enabled; 1-LZO; 2-LZ4_V2
	int					iCipherType;			//Data encryption method
	int					iIdentifyType;			//verification method:0: Certificate authentication 1: User name password authentication 2: Certificate + username password
	char				cUserName[LEN_128];		//User name, valid when the authentication method is 1 or 2 and the length is <=32
	char				cPassword[LEN_128];		//The password is valid when the authentication method is 1 or 2. The password must be encrypted using the base64 algorithm. Password length <= 64 bytes
	int					iCertUsage;				//Certificate usage:0-factory default, 1-custom imported
} VpnParam, *pVpnParam;

typedef struct tagVpnLan
{
	int					iSize;
	int					iLanNo;					//Network card number
	int					iDhcpEnable;			//0-disable; 1-enable
	char				cIP[LEN_32];			//IPV4 IP address
	char 				cMask[LEN_32];			//IPV4 subnet mask
	char 				cGateway[LEN_32];		//IPV4 default gateway
	int					iMtu;					//Mtu value in the tunnel,range:500-65535
} VpnLan, *pVpnLan;

typedef struct tagVpnState
{
	int					iSize;
	int					iLanNo;
	int					iState;					//0: Not connected, 1:Connected
} VpnState, *pVpnState;

#define CHN_STATE_OFFLINE		0
#define CHN_STATE_ONLINE		1
#define CHNOFFLINE_RSN_USRPWDERR	1
#define CHNOFFLINE_RSN_IPBLOCKED	2			//IP is blocked
#define CHNOFFLINE_RSN_ACCTLOCK		3			//Account lockout
#define CHNOFFLINE_RSN_MAXCON		4			//Maximum number of connections
#define CHNOFFLINE_RSN_NETEXCP		5			//network anomaly
#define CHNOFFLINE_RSN_MAXBD		6			//Reach the NVR maximum bandwidth
#define CHNOFFLINE_RSN_CAPUPPER		7			//Performance cap
typedef struct tagChnOnlineState 
{
	int					iSize;
	int					iState;					//OnlineState :0-offline, 1-online
	int					iOffLineReason;			//the reason when the channel is offline
} ChnOnlineState, *pChnOnlineState;

typedef struct tagRetransInfo 
{
	int					iIndex;
	char				cIp[LEN_64];	
}RetransInfo, *pRetransInfo;

typedef struct tagITS_DevOffLine
{
	int					iSize;
	int					iDevType;
	int					iIOIndex;
}ITS_DevOffLine, *pITS_DevOffLine;
#define MAX_SDI_CUTAREA_POINT 15
typedef struct tagITSSDICUTAREA
{
	int iSize;
	int iSceneId;
	int iAreaType;
	int iEnabled;
	int iCount;
    vca_TPoint pcPoint[MAX_SDI_CUTAREA_POINT];
}ITS_SDICUTAREA,*pITS_SDICUTAREA;

#define MAX_REUPLOAD_COUNT 40
typedef struct tagITSREUPLOAD
{
	int iSize;
	int iPlatetype;
	int iReuploadNum;
	int iOrderID[MAX_REUPLOAD_COUNT];
}ITS_REUPLOAD, *pITS_REUPLOAD;

typedef struct tagITSREUPLOADRESULT
{
	int iSize;
	int iFailNum;
	int iOrderID[MAX_REUPLOAD_COUNT];
}ITS_REUPLOADRESULT, *pITS_REUPLOADRESULT;

typedef struct tagITSWHISTLESNAPSHOTRESULT
{
    int iSize;
    int iChannelNo;
    int iCapFrameNo;        //取值范围：1~10
    int iResult;            //0：预留，1：成功；2：失败
}ITS_WHISTLESNAPSHOTRESULT, *pITS_WHISTLESNAPSHOTRESULT;

typedef struct tagBurnBackupStop
{
	int					iSize;
}BurnBackupStop,*pBurnBackupStop;

typedef struct tagSmdEncodeEnable
{
	int 				iEnable;
}SmdEncodeEnable, *pSmdEncodeEnable;

typedef struct tagSmdImageEnable
{
	int 				iEnable;
}SmdImageEnable, *pSmdImageEnable;

typedef struct tagSmdSceneEnable
{
	int 				iEnable;
}SmdSceneEnable, *pSmdSceneEnable;

typedef struct tagSmdAlarmArea
{
	int					iLeftMargin;
	int					iTopMargin;
	int					iRightMargin;
	int					iBottomMargin;
}SmdAlarmArea, *pSmdAlarmArea;

typedef struct tagVerificationCode
{
	char cVerificionCode[LEN_32];
}VerificationCode,*pVerificationCode;

#define MAX_VERSION_COUNT   3
typedef struct tagSnmpPara
{
	int                 iSize;
	int                 iEnable[MAX_VERSION_COUNT];
	int                 iPort;
	char                cReCommunity[LEN_33];
	char                cWrCommunity[LEN_33];
	char                cTrapAddr[LEN_16];
	int                 iTrapPort;
	char                cTrapCommunity[LEN_33];
	char                cReUser[LEN_33];
	int                 iReSecurity;
	int                 iReAuthAlg;
	char                cReAuthPwd[LEN_17];
	int                 iRePrivAlg;
	char                cRePrivPwd[LEN_17];
	char                cWrUser[LEN_33];
	int                 iWrSecurity;
	int                 iWrAuthAlg;
	char                cWrAuthPwd[LEN_17];
	int                 iWrPrivAlg;
	char                cWrPrivPwd[LEN_17];
}SnmpPara, *pSnmpPara;

//server type
#define SERVERTYPE_PRIVATE		(0)
#define SERVERTYPE_ONVIF		(1)
#define SERVERTYPE_PUSH			(2)
#define SERVERTYPE_RTSP			(3)
#define SERVERTYPE_28181GB		(4)
#define SERVERTYPE_LG			(1001)

typedef struct tagVCARule
{
	int					iSceneID;				//scene ID(0~15)
	int					iRuleID;				//rule ID(0~7)
	int					iValid;					//event valid, 0:invalid,1:valid
}VCARule,*pVCARule;

typedef struct __tagVca_TDisplayParam
{
	int					iDisplayRule;			//display Rule,0-display 1-Not display	
	int					iDisplayStat;			//alarm count display,0-display 1-Not display
	int					iColor;					//zone color,1-red 2-green 3-yellow 4-blue 5-Purple 6-cyan-blue 7-black 8-White,(defalit green)
	int					iAlarmColor;			//alarm zone color
}vca_TDisplayParam;	

typedef struct tagVCAColorTrack
{
	int					iSize;
	VCARule				tRule;
	vca_TDisplayParam	tDisplayParam;
	int					iDisplayTarget;
	int					iMaxSize;
	int					iMinSize;
	int					iCalibrationSize;
	int					iSensitiv;
	int					iTargetColor;
	int					iHue;
	int					iSaturation;
	int					iLightness;
	int					iZoomRate;
	int					iSearchTime;
	int					iSearchLoop;
}VCAColorTrack, *pVCAColorTrack;

#define MAX_DETECT_AREA_NUM			 8

typedef struct tagVCACommonPara
{
	int					iDevType;								//0-IPC，1-NVR
	VCARule				tRule;
	vca_TDisplayParam	tDisplayParam;
	int					iDisplayTarget;
	int					iMaxSize;
	int					iMinSize;
	int					iSensitivity;
	int					iRegionNum;
	vca_TPolygonEx		stPoints[MAX_DETECT_AREA_NUM];			//The number of vertices of the polygon area and coordinate
}VCACommonPara, *pVCACommonPara;

typedef struct tagVCAParaProcratorateDuty
{
	VCACommonPara		tVcaCommPara;
	int					iLeaveTime;
	int					iInvalidRegionNum;
	vca_TPolygonEx		stInvalidPoints[MAX_DETECT_AREA_NUM];
	vca_TPolygonEx		stPrisonerRegion;					//Prisoner Region
}VCAParaProcratorateDuty, *pVCAParaProcratorateDuty;

typedef struct tagVCAParaHeightLimit
{
	VCACommonPara		tVcaCommPara;
	int					iLimitTime;
}VCAParaHeightLimit, *pVCAParaHeightLimit;

typedef struct tagVCANewDuty
{
	VCACommonPara		tVcaCommPara;
	int					iLeaveTime;
	int					iDutyNum;
}VCAParaNewDuty, *pVCAParaNewDuty;

typedef struct tagVCAAbnormalNum
{
	VCACommonPara       tCommonInfo;
	int                 iReferNum;
	int                 iMode;
	int                 iLeaveTime;
	int			        iInvalidRegionNum;
	vca_TPolygonEx		stInvalidPoints[MAX_DETECT_AREA_NUM];	
}VCAAbnormalNum, *pVCAAbnormalNum;

typedef struct tagVcaArmTouch
{
	VCACommonPara       tCommonInfo;
	int                 iTimeMin;
	int			        iInvalidRegionNum;
	vca_TPolygonEx		stInvalidPoints[MAX_DETECT_AREA_NUM];	
}VcaArmTouch, *pVcaArmTouch;

typedef struct tagVcaNewFight
{
	VCACommonPara       tCommonInfo;
	int                 iTimeMin;
	int			        iInvalidRegionNum;
	vca_TPolygonEx		stInvalidPoints[MAX_DETECT_AREA_NUM];	
}VcaNewFight, *pVcaNewFight;

typedef struct tagVcaStillDect
{
	VCACommonPara       tCommonInfo;
	int                 iStillTime;
	int			        iInvalidRegionNum;
	vca_TPolygonEx		stInvalidPoints[MAX_DETECT_AREA_NUM];	
}VcaStillDect, *pVcaStillDect;

typedef struct tagVCAParaGetUp
{
	VCACommonPara		tCommonPara;
	vca_TLine			tLinePoins;
}VCAParaGetUp, *pVCAParaGetUp;

typedef struct tagVCAParaSlipUp
{
	VCACommonPara		tCommonPara;//The Slip Up algorithm has only one region.The default value iRegionNum=1.
	int     			iHeightMax;	
}VCAParaSlipUp, *pVCAParaSlipUp;

typedef struct tagVCAParaLeaveBed
{
	VCACommonPara		tCommonPara;
	int     			iLeaveTime;	
}VCAParaLeaveBed, *pVCAParaLeaveBed;

typedef struct tagVCAParaSleep
{
	VCACommonPara		tCommonPara;
	int     			iSleepTime;	
	int					iInvalidRegionNum;
	vca_TPolygonEx		stInvalidPoints[MAX_DETECT_AREA_NUM];
}VCAParaSleep, *pVCAParaSleep;

#define		MAX_LOCAL_NVR_TARGET_NUM   4			//每区域目标最多4个
#define		MAX_LOCAL_NVR_REGION_NUM   8			//区域最大8个
typedef struct tagTargetResult
{
	int				iAlarmState;
	int				iTargetID;
	int				iTargetType;//iEventType=98，1- 人 2- 物（其他） 3-车 4-声音 5-非机动车
	int				iLeft; 
	int				iTop; 
	int				iRight; 
	int				iBottom; 	
}TargetResult, *pTargetResult;

typedef struct tagRegionInfo
{
	int				iTargetNum;
	TargetResult	tTargetResult[MAX_LOCAL_NVR_TARGET_NUM];
}RegionInfo, *pRegionInfo;

typedef struct tagRegionInfoEx
{
	int				iTargetNum;
	int				iTargetBehavior[MAX_LOCAL_NVR_TARGET_NUM];//iEventType=98，1-入侵，2-离开，3-滞留，4-高音，5-低音
	int				iTargetColor[MAX_LOCAL_NVR_TARGET_NUM];//iEventType=98，1-红色 2-绿色 3-黄色 4-蓝色 5-紫色 6-青色 7-黑色 8-白色 9-马赛克 10-灰色 31-其他
}RegionInfoEx, *pRegionInfoEx;

typedef struct _tagDayScheduleTime
{
	int  				iStartHour;
	int  				iStartMin;
	int  				iStopHour;
	int  				iStopMin;
	int  				iEnable;			
}DayScheduleTime,*PDayScheduleTime;

#define MAX_DAYS								7
#define MAX_EXTRA_SCHEDULE_TIME_SEGMENT			16
typedef struct _tagExtraAlarmSchedule
{
	int					iSize;
	int					iSceneID;
	int					iAlarmType;
	int					iWeekday;						
	int					iParam1;
	int					iParam2;
	int					iSegmentNum;
	DayScheduleTime		tTimeSegment[MAX_EXTRA_SCHEDULE_TIME_SEGMENT];		
}ExtraAlarmSchedule, *PExtraAlarmSchedule;


//faceParam
#define FACE_CMD_MIN								0	
#define FACE_CMD_EDIT								(FACE_CMD_MIN + 0x00)
#define FACE_CMD_DELETE								(FACE_CMD_MIN + 0x01)
#define FACE_CMD_QUERY								(FACE_CMD_MIN + 0x02)
#define FACE_CMD_MODEL								(FACE_CMD_MIN + 0x03)
#define FACE_CMD_LIB_EDIT							(FACE_CMD_MIN + 0x04)
#define FACE_CMD_LIB_DELETE							(FACE_CMD_MIN + 0x05)
#define FACE_CMD_LIB_QUERY							(FACE_CMD_MIN + 0x06)
#define FACE_CMD_FEATURE_QUERY						(FACE_CMD_MIN + 0x07)
#define FACE_CMD_FEATURE_CALC						(FACE_CMD_MIN + 0x08)
#define FACE_CMD_CUT								(FACE_CMD_MIN + 0x09)
#define FACE_CMD_CUT_QUERY							(FACE_CMD_MIN + 0x0A)
#define FACE_CMD_TASK_CREATE						(FACE_CMD_MIN + 0x0B)
#define FACE_CMD_TASK_FREE							(FACE_CMD_MIN + 0x0C)
#define FACE_CMD_LIB_IMPORT							(FACE_CMD_MIN + 0x0D)
#define FACE_CMD_SEARCH								(FACE_CMD_MIN + 0x0E)
#define FACE_CMD_LIB_SYNC_START						(FACE_CMD_MIN + 0x0F)
#define FACE_CMD_LIB_SYNC_STATUS					(FACE_CMD_MIN + 0x10)
#define FACE_CMD_CUT_EX								(FACE_CMD_MIN + 0x11)
#define FACE_CMD_SEARCH_SNAP						(FACE_CMD_MIN + 0x12)
#define FACE_CMD_SEARCH_SNAP_PROCESS				(FACE_CMD_MIN + 0x13)
#define FACE_CMD_SEARCH_SNAP_RESULT 				(FACE_CMD_MIN + 0x14)
#define FACE_CMD_ALARM_PARAM						(FACE_CMD_MIN + 0x15)
#define FACE_CMD_LIB_CLEAR							(FACE_CMD_MIN + 0x16)
#define FACE_CMD_MAX								(FACE_CMD_MIN + 0x17)	

typedef struct tagFaceReply
{
	int					iSize;					//结构体大小
	int					iLibKey;				//人脸库key值，iLibKey>0	  
	int 				iFaceKey;				//图片key值，iFaceKey>0 
	int					iOptType;				//0-添加人脸 1-编辑人脸 2-删除人脸 3-添加人脸库 4-编辑人脸库 5-删除人脸库 6-人脸建模 7-查询人脸库
												//8-查询人脸 9-人脸属性查询 10-人脸属性提取 11-扣人脸 12-以图搜底图 13-同步人脸库 14-查询同步人脸库状态
												//15-以图搜抓拍图 16-人脸库拷贝迁移 17-人脸库解锁密码校验 18-人脸特征值比对 19-清除人脸库
	int					iResult;				//0-成功，1-系统准备好 2-失败 3-超出最大范围 4-系统正在删除 5-系统正在扣人脸 
												//6-系统正在执行以图搜抓拍图操作 7-密码错误 8-事务超时 9-算法模块启动中，请稍后重试
												//10-正在清空或删除的底库不可添加 11-系统盘存储空间不足
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID
	char				cFaceUUID[LEN_UUID];	//图片在平台对应ID
	int					iDelLibProgress;		//当iResult=4时有效，表示人脸库删除进度，取值范围0-100
												//当iResult=6时有效，表示以图搜抓拍图搜索进度，取值范围0-100
												/*当iOptType=0 && iResult=2时有效, 返回结果如下
													1：人脸底库不存在
													2：暂停智能分析失败
													3: 设备正在升级和对讲
													4: 添加数据库失败
													5：生成图片错误
													6：建模错误---建模图片不符合要求
													7：建模错误---建模图片算法没有检出人脸
													8：建模错误---建模图片人脸检出扩张之后超过1280*736
													9：建模错误---图片的格式和内容不匹配
													10：建模错误---建模图片插入到算法数据库失败
													11：建模错误---建模图片从算法数据库删除失败
													12：建模错误---从jpeg头部信息找图片宽高错误
													13: JPG 转 YUV 失败
													14: JPG 转 ARGB 失败
													15：无效的feature data
													16：算法返回错误
													17：VGS缩放失败                                                     
													18: 建模错误---获取视频帧失败  
													19: VGS局部缩放或者原图缩放失败  
												*/
}FaceReply,*pFaceReply;

typedef struct tagFaceInfo
{
	int					iSize;					//结构体大小
	int					iLibKey;				//人脸库key值，iLibKey>0（iLibKey为0，cLibUUID为空时表示查询所有库） 
	int 				iFaceKey;				//图片key值， iFaceKey>0
	int					iCheckCode;				//文件校验码，iFaceKey=0时有效，用来校验人脸图片
	int					iFileType;				//文件扩展类型，iFaceKey=0时有效，0-jpg，1-png  
	int					iModeling;				//是否建模：0-不建模，1-建模；单张导入时发1，批量导入时发0
	char				cName[LEN_64];			//姓名
	int					iSex;					//性别：0-未知，1-男，2-女
	char				cBirthTime[LEN_16];		//出生日期，格式为：2017-04-04，最大16个字节
	int					iNation;				//民族，按国家标准代码 （见民族代码表）
	int					iPlace;					//籍贯，高16位表示省，低16位表示市；（见省份城市代码表）分别按国家行政区划代码赋值，0表示未知
	int					iCertType;				//证件类型，0-未知，1-身份证，2-军官证，3-护照，4-员工编号
	char				cCertNum[LEN_64];		//证件号
	int					iOptType;				//操作类型：1=添加，2=修改，3=复制，4=迁移
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID，不为空以本字段为准
	char				cFaceUUID[LEN_UUID];	//图片在平台对应ID，不为空以本字段为准
	char				cLibVersion[LEN_64];	//人脸库版本号
	char				cVerifyCode[LEN_64];	//图片校验码，图片文件MD5
	int					iTaskId;				//事务ID，以图搜底图有效
	char				cFileName[LEN_256];		//图片名称，底库图片名
	int					iSimilar;				//相似度，0-100（以图搜底图中的相似度）
	int					iCountry;				//国家，按国家和地区ISO 3166-1代码表（见数字）
	char				cAddress[LEN_64];		//地址
	char				cCompany[LEN_64];		//公司
	int					iFaceObjScoreLevel;		//人脸目标评分等级,10-低，20-中，30高
	int					iFaceObjScore;			//人脸目标评分,0-100
}FaceInfo,*pFaceInfo;

typedef struct tagFaceEdit
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	char				cFacePic[LEN_256];		//输入要编辑的图片名称	
	FaceInfo			tFace;					//图片信息
}FaceEdit,*pFaceEdit;

typedef struct tagFaceDelete
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号从0开始
	int					iLibKey;				//人脸库key值，iLibKey>0	  
	int 				iFaceKey;				//图片key值， iFaceKey>0
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID，不为空以本字段为准
	char				cFaceUUID[LEN_UUID];	//图片在平台对应ID，不为空以本字段为准
}FaceDelete,*pFaceDelete;

typedef struct tagFaceQuery
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iPageNo;				//页码，iPageNo>=0
	int					iPageCount;				//当前页条数，iPageSize>=0
	int					iLibKey;				//人脸库key值，iLibKey>0（iLibKey为0，cLibUUID为空时表示查询所有库）
	int 				iModeling;				//建模状态， 0-建模成功，1-建模失败，2-未建模 
	char				cName[LEN_64];			//姓名
	int					iSex;					//性别，0-未知，1-男，2-女
	char				cBirthStart[LEN_16];	//出生日期查询开始时间，格式为：2017-04-04
	char				cBirthEnd[LEN_16];		//出生日期查询结束时间，格式为：2017-04-04
	int					iNation;				//民族，按国家标准代码 
	int					iPlace;					//籍贯，高16位表示省，低16位表示市；
												//分别按国家行政区划代码赋值，0表示未知  （见省份城市代码表）
	int					iCertType;				//证件类型，0-未知，1-身份证，2-军官证，3-护照，4-员工编号
	char				cCertNum[LEN_64];		//证件号
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID
	char				cLibPwd[LEN_64];		//library password
	int					iCountry;				//country
	char				cAddress[LEN_64];		//address
	char				cCompany[LEN_64];		//company
	int					iFaceObjScoreLevel;		//人脸目标评分等级，10-低，20-中，30-高
}FaceQuery,*pFaceQuery;

typedef struct tagFaceQueryResult
{
	int					iSize;					//结构体大小					
	int					iChanNo;				//通道号，从0开始
	int					iTotal;					//总条数
	int					iPageNo;				//页码，从0开始
	int					iPageCount;				//当前页条数
	int					iIndex;					//页码内序号，从0开始
	FaceInfo			tFace;					//人脸信息结构体
}FaceQueryResult,*pFaceQueryResult;

typedef struct tagFaceLibInfo
{
	int					iSize;					//结构体大小
	int					iLibKey;				//人脸库key值，iLibKey=0表示添加人脸库，>0表示修改人脸库  
	char				cName[LEN_64];			//人脸库名称
	int					iThreshold;				//人脸库阈值
	char				cExtrInfo[LEN_64];		//附加信息
	int					iAlarmType;				//是否上传识别信息，0-不上传，1-上传
	int					iOptType;				//操作类型，1--添加人脸库，2--修改人脸库，3--锁定密码校验，4--通过NVR修改前端相似度
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID，不为空以本字段为准
	char				cLibVersion[LEN_64];	//人脸库版本号
	int					iLibType;				//人脸库类型,0-普通人脸库，1-陌生人库
	int					iLibPicCnt;				//人脸库的图片数量
	int					iModelPicCnt;			//人脸库的已建模图片数量
}FaceLibInfo,*pFaceLibInfo;

typedef struct tagFaceLibEdit
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	FaceLibInfo			tFaceLib;				//人脸库信息结构体，不为空以本字段为准
}FaceLibEdit,*pFaceLibEdit;

typedef struct tagFaceLibDelete
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iLibKey;				//人脸库key值，iLibKey>0
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID
}FaceLibDelete,*pFaceLibDelete;

typedef struct tagFaceLibQuery
{
	int					iSize;			//结构体大小
	int					iChanNo;		//通道号，从0开始
	int					iPageNo;		//当前页编号，从0开始
	int					iPageCount;		//当前页大小
}FaceLibQuery,*pFaceLibQuery;

typedef struct tagFaceLibQueryResult
{
	int					iSize;			//结构体大小
	int					iChanNo;		//通道号，从0开始
	int					iTotal;			//总个数
	int					iPageNo;		//当前页编号，从0开始
	int					iIndex;			//页码内序号，从0开始
	int					iPageCount;		//当前页大小
	FaceLibInfo			tFaceLib;		//人脸库信息结构体	
}FaceLibQueryResult,*pFaceLibQueryResult;


typedef struct tagFaceModeling
{
	int					iSize;			//结构体大小
	int					iChanNo;		//通道号，从0开始
	int					iType;			//建模类型，0-单张建模，1-批量建模（未建模人脸），2-批量建模（全部人脸），3-取消建模
	int					iLibKey;		//人脸库key值，iLibKey>0
	int					iFaceNum;		//建模进度
	int					iFaceKey[FACE_MAX_PAGE_COUNT];	//图片key值
}FaceModeling,*pFaceModeling;

typedef struct tagFaceModeResult
{
	int					iSize;			//结构体大小
	int					iChanNo;		//通道号，从0开始
	int					iType;			//建模类型，0-单张建模，1-批量建模（未建模人脸），2-批量建模（全部人脸），3-取消建模
	int					iLibKey;		//人脸库key值，iLibKey>0
	int					iFaceKey;		//图片key值
	char				cName[LEN_64];	//姓名
	int					iRetsult;		//建模结果：0-成功，1-系统忙，2-图片格式错误，3-无人脸，4-多个人脸 5-质量低
	int					iTotal;			//建模总数：iTotal>=0
	int					iIndex;			//建模进度，尝试建模个数
}FaceModeResult,*pFaceModeResult;


typedef struct tagFaceFeatureQuery
{	
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iLibKey;				//人脸库key值，iLibKey>0
	int					iFaceKey;				//图片key值
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID，不为空以本字段为准
	char				cFaceUUID[LEN_UUID];	//图片在平台对应ID，不为空以本字段为准
}FaceFeatureQuery,*pFaceFeatureQuery;

typedef struct tagFaceFeatureCalc
{	
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iCheckCode;				//文件校验码，用来校验人脸图片，0表示不校验
	int					iPicType;				//文件扩展类型：0-jpg，1-png
	char				cPicPath[LEN_256];		//需要提取人脸属性的图片路径
	char				cPicUUID[LEN_UUID];		//图片序列号
}FaceFeatureCalc,*pFaceFeatureCalc;

//扣人脸
typedef struct tagFaceCut
{	
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	char				cCheckCode[LEN_64];		//文件校验码，用来校验人脸图片MD5 
	int					iTaskId;				//事务ID
	int					iPicType;				//文件扩展类型：0-jpg，1-png
	char				cPicPath[LEN_256];		//需要抠图的图片路径
}FaceCut,*pFaceCut;

typedef struct tagFaceCutQuery
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iTaskId;				//事务ID
	int					iPageNo;				//页编号
	int					iPageCount;				//页大小
}FaceCutQuery,*pFaceCutQuery;


typedef struct tagFaceCutEx
{	
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	char				cCheckCode[LEN_64];		//文件校验码，用来校验人脸图片MD5 
	int					iPicType;				//文件扩展类型：0-jpg，1-png
	char				cPicPath[LEN_256];		//需要抠图的图片路径
	int					iPageNo;				//页编号
	int					iPageCount;				//页大小
}FaceCutEx,*pFaceCutEx;

typedef struct tagFaceCutQueryResult
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iTaskId;				//事务ID
	int					iTotal;					//抠图总数
	int					iPageNo;				//页编号
	int					iIndex;					//当前第几条，最大值20
	int					iPageCount;				//当前页条数
	char				cFileName[LEN_256];		//图片名称，图片下载走之前的图片下载协议
	CommonRECT			tFaceRect;				//当前人脸区域坐标
}FaceCutQueryResult,*pFaceCutQueryResult;

//以图搜底图（回复查询人脸信息）
typedef struct tagFaceSearch
{
	int					iSize;				//结构体大小
	int					iChanNo;			//通道号，从0开始
	int					iTaskId;			//事务ID
	char				cLibKey[LEN_64];	//人脸库在平台的ID，不为空以本字段为准
	char				cPicName[LEN_256];	//图片名称
	int					iSimilar;			//相似度，0-100
	int					iPageNo;			//当前页码，从0开始
	int					iPageCount;			//每页个数，最大20
	int					iLibKey;			//库id，iLibKey>0(iLibKey为0，cLibUUID为空时表示查询所有库)
}FaceSearch,*pFaceSearch;

typedef struct tagFaceTaskFree
{
	int					iSize;				//结构体大小
	int					iTaskId;			//事务ID
}FaceTaskFree,*pFaceTaskFree;

#define MAX_FEATURE_DATA_LEN		(32*1024)

typedef struct tagFaceFeatureResult
{		
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iType;					//提取类型：1：提取单张图片的feature，2：查询图片的feature
	int					iLibKey;				//人脸库key值：提取单张图片feature时，该值为0
	int					iFaceKey;				//图片key值：提取单张图片feature时，该值为0
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID：提取单张图片feature时，该值为0
	char				cFaceUUID[LEN_UUID];	//图片在平台对应ID，或图片序列号
	int					iFaceNum;				//人脸数目
	int					iFaceRectCount[FACE_MAX_FEATURE_COUNT];		//当前人脸区域个数
	TPoint				tFaceRectPoint[FACE_MAX_FEATURE_COUNT][FACE_MAX_RECT_POINT_COUNT];	//当前人脸区域坐标
	int					iFaceDataLen[FACE_MAX_FEATURE_COUNT];	//当前人脸feature数据的总长度
	char				cFaceData[FACE_MAX_FEATURE_COUNT][MAX_FEATURE_DATA_LEN];	//feature二进制数据
}FaceFeatureResult,*pFaceFeatureResult;

#define FACE_ALARM_DETECTION				0		//Face detection
#define FACE_ALARM_RECOGNITON_DISCERN		1		//Face recognition-Comparison alarm
#define FACE_ALARM_RECOGNITON_STRANGER		2		//Face recognition-Stranger alarm
#define FACE_ALARM_RECOGNITON_RATEN			3		//Face recognition-Frequency alarm
#define FACE_ALARM_RECOGNITON_DETENTION		4		//Face recognition-Stay alarm

typedef struct tagFaceAlarmParam
{
	int					iSize;					//结构体大小
	int					iChanNo;				//通道号，从0开始
	int					iAlarmType;				//报警类型列表			
	int					iParam1;				//iType=21，NVR的智能分析列表							
	int					iParam2;				//当iType=21， iParam1=3,4时代表时间
	int					iParam3;				//iType =21，iParam2=3时，代表频次
	char				cLibkey[LEN_64];		//人脸库key值	
	int					iRecognition;			//是否上传识别信息：0-不支持，1-不上传，2-上传
	int					iSimilar;				//相似度：0--100
	int					iDevType;				//设备类型：0-IPC，1-NVR	
	int					iEnable;				//使能：0-不使能  1-使能
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID列表：UUID列表，最大10个，逗号分隔
	int					iLibEnable;				//人脸库使能列表：0-不使能  1-使能，个数同cLibUUIDList，逗号分隔  例：1,0,1
	int					iFreqGroup;				//频次统计组号, 0-无效组号 组号，范围1-16(仅频次报警有效，优先解析本字段，忽略iChannelNo)
	char				cFreqChannelList[LEN_256];	//频次关联通道号, 空-无效 逗号分隔 例：0,1,2,3(仅频次报警有效)
	char				cLibUUIDStranger[LEN_UUID];		//陌生人自动入库的UUID（平台生成）空-关闭自动陌生人入库
}FaceAlarmParam, *pFaceAlarmParam;

typedef struct tagFaceAlarmParamIn
{
	int					iSize;			//结构体大小
	int					iType;			//人脸报警类型：0--人脸检测，1--比对报警，2--陌生人报警，3--频次报警，4--滞留报警，5：行为分析--周界 6：行为分析--绊线 7：智能侦测 8: 人数统计--区域人数超限 9：视频结构化--人形检测 10：视频结构化--车辆检测 11：视频结构化--非机动车检测 12：视频结构化--车牌黑名单匹配 13：视频结构化--车牌白名单匹配
	int					iRetCount;		//输入需要获取的报警信息单元的个数
}FaceAlarmParamIn, *pFaceAlarmParamIn;

#define  MAX_DETECTAREA_NUM 128
#define  MIN_POINT_NUM  3
typedef struct _tagDetectArea
{
	int				 	iSize;
	int              	iChannelNO;
	int              	iSceneID;
	int              	iStatus;//1-开始上报检测区域 2-停止上报检测区域
	int                 iType; //0-人脸检测 1-流速检测 2-水位水尺检测
}DetectArea, *pDetectArea;

typedef struct _tagDetectAreaResult
{
	int				  	iSize;
	int               	iChannelNO;
	int               	iSceneID;//0-127
	int               	iAreaNum;//3～32
	int               	iState;   //0-未设置 1-已设置
	vca_TPolygonEx    	tPoint;
	int                 iAreaType;//0-人脸检测 1-流速检测 2-水位水尺检测
}DetectAreaResult, *pDetectAreaResult;

typedef struct _tagDetectAreaFinish
{
	int				  	iSize;
	int               	iChannelNO;
	int               	iSceneID;
	int               	iAreaNum; 
	int               	iAreaList[MAX_DETECTAREA_NUM];
	int                 iAreaType;//0-人脸检测 1-流速检测 2-水位水尺检测
}DetectAreaFinish, *pDetectAreaFinish; 

#define MAX_PACK_AREA_LIST      16
#define MAX_PACK_NUM            8
typedef struct _tagDetectAreaListInfo
{
	int                 iPackNo;
	int                 iAreaList[MAX_PACK_AREA_LIST];
}DetectAreaListInfo, *pDetectAreaListInfo;

typedef struct _tagDetectAreaFinishEx
{
	int				  	iSize;
	int               	iChannelNO;
	int               	iSceneID;
	int               	iAreaNum; 
    DetectAreaListInfo  tInfo[MAX_PACK_NUM];
	int                 iAreaType;//0-人脸检测 1-流速检测 2-水位水尺检测
}DetectAreaFinishEx, *pDetectAreaFinishEx;

typedef struct _tagDetectPara
{
	int 				iSize;						//size of struct
	int 				iChannelNo;
	int 				iSceneID;
	int 				iDetectLoop;
	int 				iDetectTime;
} DetectPara, *pDetectPara;


typedef struct _tagSetDetectArea
{
	int            		iSize;						//size of struct
	int            		iChannelNO;
	int            		iSceneID;
	int            		iAreaNum;
	int            		iOperationType; 
	vca_TPolygonEx 		tPoints;
} SetDetectArea, *pSetDetectArea; 

#define  PICTURE_TYPE_CUSTOMIZE			0
#define  PICTURE_TYPE_HEAD_PHOTO		1
#define  PICTURE_TYPE_HALFBODY_PHOTO	2
#define  PICTURE_TYPE_FULLBODY_PHOTO	3
typedef struct _tagVcaTargetPicture
{
	int					iSize;				//size of struct
	int 				iTargetPictureType; //Target image type  0-customize 1-Head Photo 2-Half-body Photo 3-Full-body Photo
	int					iPictureWidth;		//Picture Width(1~5) When iTargetPictureType=0,This param is available.
	int					iFaceHeight;		//Face Height(1~3)	 When iTargetPictureType=0,This param is available.
	int					iBodyHeight;		//Body Height(0~10)	 When iTargetPictureType=0,This param is available.
}VcaTargetPicture,*pVcaTargetPicture;

typedef struct _tagRainFallAlarmInfo
{
	int                 iSize;							//size of struct
	int                 iRainfallValue;     			//80,120,160(mm)
	int                 iAlarmTime;         			//Alarm Time 1-600s
	int                 iRainTimeInter;     			//The Interval Of Rain Times
	int                 iUploadTime;        			//Default Upload Check Time (min)  
	int                 iModerateRainUploadTime;        //Moderate Rain Upload Check Time (min)
	int                 iHeavyRainUploadTime;           //Heavy Rain Upload Check Time(min)
	int                 iTorrentialRainUploadTime;      //Torrential Rain Upload Check Time (min)
	int                 iModerateRainThershold;         //Moderate Rain Thershold (mm/min) 
	int                 iHeavyRainThershold;            //Heavy Rain Thershold mm/min
	int                 iTorrentialRainThershold;       //Torrential Rain Thershold mm/min
	int	                iUploadType;					//上报方式	0-保留 1-场次上报 2-定时上报					
	int                	iInterval;						//统计时长间隔	范围:0-当前变化的雨量、60-86400，默认:3600，单位:秒					
}RainFallAlarmInfo, *pRainFallAlarmInfo;

#define DEFAULT_CHECKSTAND								1
#define DEFAULT_ALERTWATER								0
#define DEFAULT_ALARMLINKTIME							120
#define DEFAULT_UPLOADTIME								360
#define DEFAULT_FIRSTWATERLEVELUPLOADTIME				240
#define DEFAULT_SECWATERLEVELUPLOADTIME					120
#define DEFAULT_THIRDWATERLEVELUPLOADTIME				60
#define DEFAULT_FIRSTWATERLEVELTHERSHOLD				1
#define DEFAULT_SECWATERLEVELTHERSHOLD					5
#define DEFAULT_THIRDWATERLEVELTHERSHOLD				10
#define MAX_WATER_THERSHOLD_NUM							5

typedef struct _tagAlertWaterAlarmInfo
{
	int                 iSize;							//size of struct
	int                 iAlertWaterValue;			    //Alert Water Level Alarm Thershold
	int                 iAlarmLinkTime;					//Alarm Time 1-600s
	int					iSenceID;						//The ID Of Sence
	int                 iRuleID;					    //The ID Of Rule
	int                 iIsMainChannel;					//Whether Main Channel Or Not(0:NO; 1:Yes)
	int					iCheckStand;					//The Source Of Water Level Alarm Data
	int                 iDefaultUploadTime;             //Default Upload Check Time
	int                 iFirstWaterLevelUploadTime;     //The Upload Check Time Of First Water Level Change(min)
	int                 iSecondWaterLevelUploadTime;    //The Upload Check Time Of Second Water Level Change(min)
	int                 iThirdWaterLevelUploadTime;     //The Upload Check Time Of Third Water Level Change(min)
	int                 iFirstWaterLevelThershold;      //First Water Level Thershold(cm/h)
	int                 iSecondWaterLevelThershold;		//Second Water Level Thershold(cm/h)
	int                 iThirdWaterLevelThershold;		//Third Water Level Thershold(cm/h)
	int					iAlgorithmType;					//Algorithm Type
	int                 iWaterNum;                      //Alert Water Level Alarm Thershold num；
	int                 iWaterAlarm[MAX_WATER_THERSHOLD_NUM]; //Alert Water Data;
	int                 iWaterLowValue;                 //
	int                 iWarnHighValue;                 //decline of the water level(superior limit)
	int                 iWarnLowValue;                  //decline of the water level(lower limit)
	int					iWaterFilterEnable;				//Water level removal jitter filtering Enable:0-Reverse, 1:Enable,2-unEnable
	int					iWaterFilterLevel;				//Water level removal jitter filtering Level:0-Reverse, 1-5 Level
	int					iWaterFilterTime;				//Water level removal jitter filtering Time :0-Reverse, 1-300 milliSecond
    int                 iThresholdEnable;               //低水位阈值使能	0-保留，1-使能，2-不使能					
    int                 iThresholdLevel;                //低水位阈值	当iThresholdEnable为1时有效，范围±10000000mm、单位mm					
	int					iAlarmHighAddEnable;			//水位上限报警加报时间间隔使能(0-保留，1-使能，2-不使能)
	int					iAlarmHighAddInterval;			//水位上限报警加报时间间隔(单位秒(范围60s-86400s))
	int					iThresholdAddReportEnable;		//水位超上限阈值加报使能(0-保留，1-使能，2-不使能)
	int					iThresholdAddReportValue;		//水位超上限加报的阈值(范围+-10000000mm、单位mm)
	int					iThresholdAddReportInterval;	//水位超上限阈值加报时间间隔(单位：秒，范围：5-86400)
	int					iThresholdGrowthRateEnable;		//涨率报警使能	0-保留，1-使能，2-不使能					
	int					iThresholdGrowthRate;			//涨率报警阈值	当iThresholdGrowthRateEnable为1时有效，范围±10000000、单位mm/min
	int					iWaterCorrectVal;				//水位校正值	（范围+-10000000mm、单位mm）				
	int					iWaterDotDegree;				//水位数据小数位精度 0为保留，大于0为水位小数位数 
}AlertWaterAlarmInfo, *pAlertWaterAlarmInfo;

#define MAX_PERIPHREAL_NUM       100
typedef struct tagIrrigationOverInfo
{
	int					iSize;							//Size Of Struct
	int					iType;							//Type Of Reporting Irrigation Info
	int					iLedEnable;						//Whether Overlying Led Or Not
	int 				iOsdEnable;						//Whether Overlying Video Or Not
    int               iSetFlag;                       //User Define Flag,0:Use Default name,1:Define
	char               cTypeName[LEN_32];              //User Define Content
	int               iPeripheralIndex;              //Peripheral No(1-100)
	int               iPeripheralType;               //periphreal type
}IrrigationOverInfo,*pIrrigationOverInfo;

#define   MAX_PERIPHERAL_PARA_COUNTER       4
typedef struct tagPeripheralPara
{
	int					iSize;							//Size Of Struct
	int					iComNo;							//Com Number
	int 				iPeripheralType;				//Peripheral Type
	int 				iPeripheralAddr;				//address code
	int					iParaCounter;					//Parameter Counter
	char 	         	cPeripheralPara[MAX_PERIPHERAL_PARA_COUNTER][LEN_256]; //Peripheral Parameter
}PeripheralPara,*pPeripheralPara;

#define IRRIGATION_TYPE_RAINFALL					1		//rainfall
#define IRRIGATION_TYPE_RAINDURATION				2		//rain duration
#define IRRIGATION_TYPE_WATERLEVEL					3		//water level
#define IRRIGATION_TYPE_SEDIMENT					4		//depth of water
#define IRRIGATION_TYPE_ALERTWATERLEVEL				5		//alert water level
#define IRRIGATION_TYPE_FLOWRATEVALUE				6		//Flow rate value
#define IRRIGATION_TYPE_BATTERYVOLTAGE				7		//Battery voltage value
#define IRRIGATION_TYPE_AIRPRESSURE 				8		//Air pressure
#define IRRIGATION_TYPE_WINDSPEED					9		//Wind speed
#define IRRIGATION_TYPE_WINDDIRECTION				10		//wind direction
#define IRRIGATION_TYPE_TEMPERATURE					11		//Temperature
#define IRRIGATION_TYPE_HUMIDITY					12		//Humidity
#define IRRIGATION_TYPE_ACIDITYANDALKAL				13     //AcidityAndalkal
#define IRRIGATION_TYPE_DISSOLVEOXYGEN				14     //Dissolve Oxygen
#define IRRIGATION_TYPE_OXYREDUCTION				15     //Oxygen Reduction
#define IRRIGATION_TYPE_GPS							16		//GPS
#define IRRIGATION_TYPE_RTX							17		//RTX
#define IRRIGATION_TYPE_TURBIDITY					18		//turbidity
#define IRRIGATION_TYPE_AMMONICA					19		//Ammonica
#define IRRIGATION_TYPE_WATERTEMP					20		//Water temp
#define IRRIGATION_TYPE_CONDUCTIVITY				21		//Conductivity
#define IRRIGATION_TYPE_OXYDEMAND					22		//Oxydemand
#define IRRIGATION_TYPE_NITROGEN					23		//Nitrogen
#define IRRIGATION_TYPE_PHOSPHORUS					24		//Phosphorus
#define IRRIGATION_TYPE_PRESSURE_WATERLEVEL			25     //Pressure water level gauge
#define IRRIGATION_TYPE_WATER_SPEED_FIELD			26     //Velocity field
#define IRRIGATION_TYPE_WATER_LEVELANDFLOW			27    //Flow
#define IRRIGATION_TYPE_CUMULATIVE_FLOW				28    //Cumulative flow
#define IRRIGATION_TYPE_COMBUSTIBLE_GAS				29    //Combustible gas
#define IRRIGATION_TYPE_OXYGEN						30    //Oxygen
#define IRRIGATION_TYPE_CARBON_MONOXIDE				31    //Carbon monoxide
#define IRRIGATION_TYPE_HYDROGEN_SULFIDE			32    //hydrogen sulfide
#define IRRIGATION_TYPE_VOLTAGE						33    //Voltage
#define IRRIGATION_TYPE_UNDER_VOLTAGE				34    //Under Voltage alarm status
#define IRRIGATION_TYPE_FLOAT_GAUGE_LEVEL			35    //Float level gauge
//36.甲醛；37.氨气；38.液位变送器；
#define IRRIGATION_TYPE_FORMALDEHYDE				36		//Formaldehyde
#define IRRIGATION_TYPE_AMMONIA						37		//ammonia
#define IRRIGATION_TYPE_LIQUID_LEVEL_TRANSMITER		38		//liquid level transmitter
#define IRRIGATION_TYPE_RAINFALL_BYTIME				39		//RainFall by time
#define IRRIGATION_TYPE_GENERATING_POWER			40		//Elec Power
#define IRRIGATION_TYPE_LOAD_POWER					41		//Load power
#define IRRIGATION_TYPE_SEEPAGE						42		//Seepage
#define IRRIGATION_TYPE_CHLOROPHYLL					43		//叶绿素
#define IRRIGATION_TYPE_CHROMATICITY				44		//色度
#define IRRIGATION_TYPE_TOTAL_ORGANIC_CARBON		45		//总有机碳
#define IRRIGATION_TYPE_BIO_OXYGENDEMAND			46		//生化需氧量
#define IRRIGATION_TYPE_NITRITE						47		//亚硝酸盐
#define IRRIGATION_TYPE_POTA_PERMAN_INDEX			48		//高锰酸钾指数
#define IRRIGATION_TYPE_COLORED_DISORGANIC			49		//有色溶解性有机物
#define IRRIGATION_TYPE_EXTINCTION_COEFFICIENT		50		//消光系数
#define IRRIGATION_TYPE_TRANSPARENCY				51		//透明度
#define IRRIGATION_TYPE_SUSPENDED_SOLIDS			52		//悬浮物
#define IRRIGATION_TYPE_EUTROPHICATION_INDEX		53		//富营养化指数
#define IRRIGATION_TYPE_EVAPORATION_CAPACITY        54      //54:蒸发量
#define IRRIGATION_TYPE_OVERFLOW		            55		//55:溢水量
#define IRRIGATION_TYPE_WATER_STORAGE		        56		//56:蓄水量
#define IRRIGATION_TYPE_WATER_GAUGE_DATA		    57		//57:水尺读数
#define IRRIGATION_TYPE_WATER_OVERALERT_ALARM		58		//58:超警戒水位报警状态
#define IRRIGATION_TYPE_WATER_SPEED_PARAM   		59		//59:流速检测参数
#define IRRIGATION_TYPE_RESERVE_CUSTOM_1   		    60		//60:自定义数据类型1（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_2   		    61		//61:自定义数据类型2（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_3   		    62		//62:自定义数据类型3（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_4   		    63		//63:自定义数据类型4（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_5   		    64		//64:自定义数据类型5（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_6   		    65		//65:自定义数据类型6（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_7   		    66		//66:自定义数据类型7（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_8   		    67		//67:自定义数据类型8（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_9   		    68		//68:自定义数据类型9（预留）
#define IRRIGATION_TYPE_RESERVE_CUSTOM_10   	    69		//69:自定义数据类型10（预留）
#define IRRIGATION_TYPE_WATER_ADDITION   		    70		//70:加水量（蒸发仪使用）
#define IRRIGATION_TYPE_WATER_GROWTH_RATE   	    71		//71:涨率
#define MAX_IRRIGATION_TYPE							72

#define IRRIGATION_PARA_SRC_ALGO				0		//algorithm acquisition
#define IRRIGATION_PARA_SRC_EXDEV				1		//peripheral acquisition

typedef union tagIrrigationPara8
{
	int        iWaterLevel;  //water level
	int        iRtxData;     //RTX Height
	int        iCommonData;
}IrrigationPara8, *pIrrigationPara8;

typedef union tagIrrigationPara9
{
	int        iValid;       //0:invalid; 1:valid
	int        iCruiseNum;   
	int        iCommonData;
}IrrigationPara9, *pIrrigationPara9;

typedef struct tagIrrigationPara
{
	int					iSize;
	int					iType;				//Irrigation type 1:雨量;2:降雨时长;3:水位;4:积水深度;5:超警戒水位值;6:流速;7:电瓶剩余电量;8:气压;9:风速;10:风向;11:温度;12:湿度;13:酸碱度;14:溶解氧;15:氧化还原;16:GPS;17:高程;18:浊度;19:氨氮;20:水温;21:电导率;22:化学需氧量;23:总氮;24:总磷;25:流速场;
	int					iValue;				//Param2 iType(1,3,4,5)-->Unit:mm;  iType(2)-->Unit:minute ;iType(6)-->Unit:mm/s;iType(7,12)-->Unit:%;
											//		 iType(8)-->Unit:hpa; iType(9)-->Unit:m/s; iType(10)-->Unit:angle°;iType(11)-->Unit:degree centigrade ℃;	
	int					iSrc;				//Param3  0:algorithm acquisition; 1:peripheral acquisition;iType = 26,this means waterLevel
	int					iSceneID;			//Param4  Scene ID
	int					iRuleID;			//Param5  Rule ID
	NVS_FILE_TIME		tUploadTime;		//the time of data uploading
	int					iStationNum;		//Param7  the station No.(when the type is flowSpeed or WaterLevel)
	IrrigationPara8		tIrriParam8;		//Param8  when the type is flowspeed,this para represents cruise times;when the type is WaterLevel, it represents if data is valid
	IrrigationPara9		tIrriParam9;		//Param9  when the type is flowspeed,this para represents cruise times;when the type is WaterLevel, it represents if data is valid
	int					iTotalPointNum;		//Param10 (the total detect numbers)
	int					iCurrentPointNum;	//Param11 (the Current detect Number)
	int					iBaseNum;			//Param12 the distance of baseLine(when the type is flowSpeed)
	NVS_FILE_TIME		tRecordTime;		//start time of Record file(when the type is flowSpeed),other type use param13
	char				cFactoryID[LEN_64];	//Param14 Device ID, no more than 64 bytes
	int					iIrriParam6;		//Param6  when iType=26,means 检测区域编号
	int					iIrriParam13;		//Param13 when iType=26,means 像素点终止横坐标
	int					iIrriParam15;		//Param15 when iType=26,means 像素点终止纵坐标
	int					iIrriParam16;		//Param16 when iType=26,means 空间点起始横坐标
	int					iIrriParam17;		//Param17 when iType=26,means 空间点起始纵坐标
	int					iIrriParam18;		//Param18 when iType=26,means 空间点终止横坐标
	int					iIrriParam19;		//Param19 when iType=26,means 空间点终止纵坐标
	unsigned long long	ulLastTimeStamp;	//Param20 when iType=26,means Last frame timestamp
	unsigned long long	ulCurTimeStamp;		//Param21 when iType=26,means Current frame timestamp
	unsigned long long	ulIrriParam22;		//serial number
	int					iIrriParam23;
	int					iIrriParam24;
} IrrigationPara, *pIrrigationPara;

#define MAX_PERIPHREALTYPE_NUM   8
typedef struct tagPeriphrealParam
{
	int       iSize;             //PeriphrealNum结构体大小
	int       iPeriphrealType;    //外设类型
	int       iPeriphrealIndex;   //外设序号
}PeriphrealParam, *pPeriphrealParam;

typedef struct tagIrriFuncAssemble
{
	int							iSize;            //IrriFuncAssemble 结构体大小
	int							iOsdType;         //水利字符叠加类型 IRRIGATION_TYPE_RAINFALL--IRRIGATION_TYPE_UNDER_VOLTAGE
	int							iPeripheralCount;  //外设个数，范围【1-255】,目前一个字符叠加类型最多支持8个
	PeriphrealParam				tPeriphrealNum[MAX_PERIPHREALTYPE_NUM];
}IrriFuncAssemble, *pIrriFuncAssemble;

//水位加报的低功耗休眠类型
#define IRRI_REPORT_DORMANCY_TYPE_LOW_POWER			1	//低功耗休眠
#define IRRI_REPORT_DORMANCY_TYPE_SUPER_LOW_POWER	2	//超低功耗休眠
#define IRRI_REPORT_DORMANCY_TYPE_EXTREME_LOW_POWER	3	//极低功耗休眠
#define IRRI_REPORT_DORMANCY_TYPE_MAX				4
typedef struct tagIrriReportDormancy
{
	int							iSize;          //结构体大小
	int							iDormancyType;  //低功耗休眠类型 0：不休眠  1：低功耗休眠 2：超低功耗休眠 3：极低功耗休眠
	int							iEnable;		//时间启用 0-预留，1-启用，2-关闭
	int							iWeekUpTime;	//唤醒时间：单位，默认秒，范围15-86400
	int							iDorMancyTime;	//休眠时间：单位，默认秒，范围15-86400
}IrriReportDormancy, *pIrriReportDormancy;

#define  MAX_MANCAR_DETECT_AREA_COUNT  32
#define  MAX_MANCAR_DETECT_TRIPAREA_COUNT  12

#define ARI_PUSHMODE_SPEED_FIRST	1  //iPushMode Speed first
#define ARI_PUSHMODE_QUALITY_FIRST	2  //iPushMode Quality first
#define ARI_PUSHMODE_CUSTOM			3  //iPushMode custom
#define ARI_PUSHMODE_TIMING			4  //iPushMode Timing
#define ARI_PUSHMODE_TOUCHLINE		5  //iPushMode Touch line

#define ARI_SNAPMODE_ALL			1  //iSnapMode 1:snap all
#define ARI_SNAPMODE_QUALITY		2  //iSnapMode 2:snap high quality 
#define ARI_SNAPMODE_CUSTOM			3  //iSnapMode 3:custom
typedef struct tagVcaManCarDetectArithmetic
{
 	int				iSize;				//structure size
 	int				iSceneID;			//Scene id(0~15)
	int				iType;				//Structured algorithm model bit：0-Face，1-pedestrian，2-plate，3-motor vehicles，4-Non-motor vehicle
 	int				iSensitiv;			//Sensitivity(0~5)
 	int				iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
 	int				iExposureBright;	//exposure light strength (0~255)
 	int				iDisplayRule;		//0-not display 1-display
 	int				iPushMode;			//push mode 0:reserved 1:Speed first 2:Quality first  3:custom 4:timing 5:Touch line	
 	int				iPushLevel;			//push level effect when iPushMode == 3;(0:reserved,1:fast 2:normal 3:slow)   effect when iPushMode == 4;(value is timing time. range 1~255)
 	int				iSnapMode;			//snap mode (0:reserved 1:snap all 2:snap high quality 3:custom)
 	int				iSnapTimes;			//Snap times(1~10)
 	int				iMinFaceSize;		//minimum face size(1~10000 percentage of image width，10000 indicates the width of entire screen)(This field is 0 and will not be processed.)
	int				iMinPlateSize;		//minimum plate size(1~10000 percentage of image width，10000 indicates the width of entire screen)(This field is 0 and will not be processed.)
 	int				iFaceQuality;		//Facial quality [0-100]
	int				iHumanQuality;		//Pedestrian quality  [0-100]
	int				iPlateQuality;		//Plate quality[0-100]
	int				iVehicleQuality;	//Motor vehicles quality[0-100]
	int				iCycleQuality;		//Non-motor vehicle quality[0-100]
	int				iBigbkimgQuality;	//Background image quality[0-100]
	int				iSmallimgQuality;	//Small image quality[0-100]
	int				iPointNum;			//polygon area vertex number(3～32)
 	TPoint			ptArea[MAX_MANCAR_DETECT_AREA_COUNT]; //x:y percentage of image width
 	int				iTripPointNum;		//Number of vertices of the strike line  effect when iPushMode == 5(0 or 2~12)
 	TPoint			ptTripArea[MAX_MANCAR_DETECT_TRIPAREA_COUNT]; //x:y percentage of image width
    int             iExposureType;      //曝光类型	0-保留, 1-长帧, 2-短帧
    int             iExposureEnable;    //曝光使能	0-不使能, 1-使能
	int             iPlateAlarmType;    //车牌告警类型	0-保留, 1-车牌黑名单报警
	int             iDelayPushSpan;    //首张推图延迟时间	单位：毫秒，支持的数值：500,1000,2000
	int             iTimeSpace;    //时间间隔	单位：毫秒，支持的数值：100,200,300，500,1000,2000
	int				iDefaultProvince;	//车牌默认省份（按国家行政区划代码赋值，0表示未知）
	int				iPlateExposureEnable;	//车牌曝光使能
	int				iPlateExposureBright;	//车牌曝光亮度
	int				iPlateExposureTime;		//车牌曝光时间
}VcaManCarDetectArithmetic, *pVcaManCarDetectArithmetic;

typedef struct tagItsIpdWarningMsg
{
	int		iSize;
	int		iSceneId;
	int		iAreaId;
	char	cRoadName[LEN_64];
	char	cPlate[LEN_32];
	int		iPlateType;
	int		iCarType;
	int		iCapTime;
} ItsIpdWarningMsg, *pItsIpdWarningMsg;

typedef struct tagSelfTest
{
	int				iSize;			//structure size
	int				iType;			//Selftest type 1:complete self-test; 2:hardware self-test; 3:software self-test.
	int				iTestResult;	//Test result 1:success 2:failed
}SelfTest, *pSelfTest;

typedef struct tagNetTestPara
{
	int     		iTestType;		//NetWork Test Type 1:ping
	int				iCardNum;		//Card Number 0，Lan1；1，Lan2 ….7，Lan8; 10000~10000+n:bond0~bondn
	char			cIp[LEN_16];	//Device Ip
	int             iPort;			//Port
	char            cReserve[LEN_64];
}NetTestPara, *pNetTestPara;

typedef struct tagNetTestResult
{
	int     		iTestType;	
	int				iCardNum;
	int   			iReachable;		//0:Reachable   1:unReachable
	int             iDelayTime;		//NetWork Delay Time(ms)
	int 			iLostRate;		//Lose Packet Rate[0, 100]
}NetTestResult, *pNetTestResult;



typedef struct tagLogNum
{
	int				iSize;			//structure size	
	int				iPara1;			//Maximum number of supported logs;
	int             iPara2;			//The current number of logs;
}LogNum, *pLogNum;


#define USERLOGONLOCK_TYPE_MIN			1
#define USERLOGONLOCK_TYPE_MAX			2
typedef struct tagUserLogonLockPara
{
	int				iSize;			//structure size	
	int				iEnable;		//0:reserved; 1:enabled; 2:not enabled
	int             iType;			//1:logon lock-time setting; 2, logon lock-Limit the number of logon
	int				iPara;			//iType=1,lock time;iType=2,Limit the number of logon
}UserLogonLockPara, *pUserLogonLockPara;

#define FACE_LIBSYNC_CHANLIST_COUNT			64

#define FACE_LIBSYNC_STATUS_START			20
#define FACE_LIBSYNC_STATUS_STOP			21
#define FACE_LIBSYNC_STATUS_DELETE			22	//delete sync task
#define FACE_LIBSYNC_STATUS_DELETE_IPCLIB	23	//delete ipc lib
#define FACE_LIBSYNC_STATUS_START_ADDTO		24  //start sync add to

//设置同步前端人脸库
typedef struct tagFaceLibSyncStart
{
	int					iSize;					//structure size
	int					iChanListArraySize;		//需要同步的通道列表个数
	int 				iChanList[FACE_LIBSYNC_CHANLIST_COUNT];	//通道列表
	int					iLibKey;				//人脸库key值
	char				cLibUUID[LEN_UUID];		//人脸库在平台的ID，不为空以本字段为准
	int					iStatus;				//开始状态：20开始，21停止，22删除任务，23 删除前端人脸库
	int					iSame;					//相似度	0-100
}FaceLibSyncStart,*pFaceLibSyncStart;

//获取同步前端人脸库状态
typedef struct tagFaceLibSyncQuery
{
	int					iSize;				//structure size
	int					iChanNo;			//通道号，取值范围据设备类型而定,0x7FFFFFFF代表所有通道
	int					iQueryResultSize;	//when iChanNo=0x7FFFFFFF,iQueryResultSize=1;else iQueryResultSize is NVR's all channel number.
	int					iLibKey;			//人脸库key值
	char				cLibUUID[LEN_UUID];	//人脸库在平台的ID，不为空以本字段为准
}FaceLibSyncQuery,*pFaceLibSyncQuery;

//获取同步前端人脸库状态结果
typedef struct tagFaceLibSyncQueryResult
{
	int					iSize;						//structure size
	int					iChanNo;					//通道号，取值范围据设备类型而定,0x7FFFFFFF代表所有通道
	int					iLibKey;					//人脸库key值
	char				cLibUUID[LEN_UUID];			//人脸库在平台的ID，不为空以本字段为准
	int					iState;						//sync status:0-unsync, 1-synching, 2-sync success, 3-sync failure, 4-to be sync
	int					iProcess;					//Percentage of progress: range:0-100
	int					iSuccNum;					//sync success num		
	int					iFailNum;					//sync failed num
}FaceLibSyncQueryResult,*pFaceLibSyncQueryResult;

#define SINGLE_PRTOCOL_SYNC_RESULT_MAX_NUM 10
//获取同步前端人脸库每个底图的详细结果(同步失败的结果)
typedef struct tagFaceLibSyncResult
{
	int					iSize;						//structure size
	int					iChanNo;					//通道号，取值范围据设备类型而定,0x7FFFFFFF代表所有通道
	int					iLibKey;					//人脸库key值
	char				cLibUUID[LEN_UUID];			//人脸库在平台的ID，不为空以本字段为准
	int 				iFaceKey;					//图片key值， iFaceKey>0
	char				cFaceUUID[LEN_UUID];		//图片在平台对应ID，不为空以本字段为准
	int					iSyncResult;				//Sync result:0预留，1同步成功，2同步失败，
}FaceLibSyncResult,*pFaceLibSyncResult;

typedef struct tagAlarmOutPortPara
{
	int					iSize;
	int					iMode;		//0:normally open, 1:normally close
	int				    iPulseWidth;
	int					iChanNo;	//0x7fffffff Local param
	int					iPortNo;
} AlarmOutPortPara, *pAlarmOutPortPara;

typedef struct tagAlarmInPortEnable
{
	int    iSize;
	int    iChannelNo;
	int    iPortNo;
	int    iEnable;//0-disenable;1-enable
}AlarmInPortEnable, *pAlarmInPortEnable;

typedef struct tagAlarmOutPortEnable
{
	int    iSize;
	int    iChannelNo;
	int    iPortNo;
	int    iEnable; //0-disenable;1-enable
}AlarmOutPortEnable, *pAlarmOutPortEnable;

typedef struct tagpAlarmInPortState
{
	int    iSize;
	int    iChannelNo;
	int    iPortNo;
	int    iState;//1-Alarm ;0-clear alarm    
	char   cAlarmTime[LEN_32]; //alarm time
}AlarmInPortState, *pAlarmInPortState;

typedef struct tagAlarmOutPortState
{
	int    iSize;
	int    iChannelNo;
	int    iPortNo;
	int    iState;//1-Alarm ;0-clear alarm       
}AlarmOutPortState, *pAlarmOutPortState;

typedef struct tagAlarmOutPortDelayTime
{
	int		iSize;
	int		iChannelNo;
	int		iPortNo;
	int		iClearType;			//0-Manual cancellation;1-Delay cancellation
	int		iDelayTime;			//clear alarm delay time
	int		iAlarmDelayTime;	//alarm delay time	0-5 second
}AlarmOutPortDelayTime, *pAlarmOutPortDelayTime;

typedef struct tagAlarmInPortPara
{
	int					iSize;
	int					iMode;		//0:normally open, 1:normally close
	int					iType;		//0：无类型（报警输入）normal port alarm 1：按钮报警button alarm 2：翻斗雨量计Tipping rain gauge 3：线圈输入Coil input
	float               fParam;		//Accuracy of tipping bucket rain gauge	
	int					iChanNo;	//0x7fffffff Local param
	int					iPortNo;
} AlarmInPortPara, *pAlarmInPortPara;

#define INVALID_FLAG	0x7fffffff

//以图搜抓拍图
typedef struct tagFaceSearchSnap
{
	int  			    iSize;			//structure size
	int					iChanCount;		//需要搜索的通道个数
	int					iChanSize;		//结构体QueryChanNo的大小，sizeof(QueryChanNo)
	QueryChanNo*		pChanList;		//通道列表，结构体QueryChanNo数组变量
	NVS_FILE_TIME	    tBegTime;		//搜索起始时间
	NVS_FILE_TIME	    tEndTime;		//搜索结束时间
	char			    cPicturePath[LEN_256];	//以图搜抓拍图需要传入的图片名称。（确保本地该图片存在，并且能正常打开）
	int 				iSimilarity;	//以图搜抓拍图相似度
	int 			    iSortMode;		//排序方式：0-按时间排序，1-按相似度排序(0-sort by time 1-sort by similarity)
	int                 iTaskId;		//抠图建模的事务ID，该值由抠图结果返回
}FaceSearchSnap, *pFaceSearchSnap;

//获取搜索进度传入参数结构体
typedef struct tagFaceSearchSnapProcess
{
	int    iSize;						//structure size
	int	   iTaskId;						//抠图建模的事务ID，该值由抠图结果返回
} FaceSearchSnapProcess, *pFaceSearchSnapProcess;

typedef struct tagFaceSearchSnapQuery
{
	int    iSize;			//structure size
	int    iTaskId;			//抠图建模的事务ID，该值由抠图结果返回
	int    iPageSize;		//以图搜抓拍图每次查询的页大小，建议每次20条记录
	int    iPageNo;			//查询的页编号：0，1，2,3……
}FaceSearchSnapQuery, *pFaceSearchSnapQuery;

typedef struct tagFaceSearchSnapResult{
	int					iSize;				//structure size
	int					iChanNo;			//通道号，取值范围据设备类型而定,0x7FFFFFFF代表所有通道
	int					iTotal;				//搜索到的记录的总个数
	int					iCurPageCount;		//本次获取的个数，页大小
	int					iItemIndex;			//当前页编号，根据页大小传入页编号。比如总共100条记录，每次取20条，页编号依次为0,1,2,3,4
	NVS_FILE_TIME		tBegTime;			//记录开始时间
	NVS_FILE_TIME		tEndTime;			//记录结束时间
	int  				iAge;				//记录年龄值
	int                 iSex;				//记录性别：1-男，2-女，3-未知
	int					iNation;			//记录民族：1-汉族，2-少数民族
	int					iWearGlasses;		//表示戴眼镜：0-预留，1-佩戴，2-未佩戴
	int					iWearMask;			//表示戴口罩：0-预留，1-佩戴，2-未佩戴
	int					iSimilarity;		//表示相似度，范围0-100
	VcaFileAttr			tPicSnap;			//抓拍图属性
	VcaFileAttr			tPicNeg;			//人脸底图属性
	char				cLibUUID[LEN_UUID];	//人脸库在平台的ID
	char				cFaceUUID[LEN_UUID];//图片在平台对应ID
	char				cName[LEN_64];		//姓名
}FaceSearchSnapResult, *pFaceSearchSnapResult;

#define TYPE_SNAP_AGRIBANK    20000
typedef struct tagSnapInfo
{
	int         iSize;
	int         iQValue;
	char        cLocalFileName[LEN_256];
}SnapInfo, *pSnapInfo;

typedef struct tagSnapInfoOut
{
	int         iSize;
	int         iQValue;
	char        cRemoteFileName[LEN_64];
}SnapInfoOut, *pSnapInfoOut;

#define MAX_PIC_TYPE_NUM   2
typedef struct tagPicStreamUploadParam
{
	int         iSize;			
	int         iSceneId;		//range:0~15
	int         iRuleNo;		//range:0～7
	int         iPicType;		//Picture type: 0-Background big picture, 1-Small picture
	int         iSnapEnable;	//Send enable:0-not upload, 1-upload
	int         iIsOsd;			//Overlay information: 0-not stack, 1-stack.When iPicType=1,this parameter is invalid.
	int         iQpvalue;		//Snap picture quality range:1-100
	int			iFaceFrameEnable;//Overlay face frame: 0-not stack, 1-stack.When iPicType=1,this parameter is invalid.
}PicStreamUploadParam, *pPicStreamUploadParam;

typedef struct tagITSLatestBkPlate
{
	long long lLicenseID;
	char      cLicense[LEN_32];
}ITSLatestBkPlate, *pITSLatestBkPlate;

typedef struct tagITSAlarmInfo
{
	NVS_FILE_TIME tTime;
	char          cLicense[LEN_32];
	char          cAddressName[LEN_128];
	char          cAreaName[LEN_128];
	char          cAlarmName[LEN_128];
}ITSAlarmInfo, *pITSAlarmInfo;

typedef struct tagHumanDetectArithmetic
{
	int				iSize;			
	int				iSceneID;			//Scene id(0~15)
	int				iRuleNo;			//0-7
	int				iSnapEnable;		//0-disenable,1-enabele
	int				iDisplayTarget;		//display target box, 0-not dispaly, 1-dispaly
	int				iMinSizeEx;			//minimum face size(0~10000 percentage of image width，10000 indicates the width of entire screen)
	int				iMaxSizeEx;			//maximum face size(0~10000 percentage of image width,10000 indicates the width of entire screen)
}HumanDetectArithmetic, *pHumanDetectArithmetic;

typedef struct tagSIPEncrypt
{
	int     iMode;
}SIPEncrypt,*pSIPEncrypt;

typedef struct tagSIPPublicKey
{
	char sPublicKey[LEN_256];
}SIPPublicKey,*pSIPPublicKey;

typedef struct __tagDormancyWork
{
	int iSize;
	int iBeginHour;
	int iBeginMinute;
	int iEndHour;
	int iEndMinute;
	int iWorkType;  //In bits,1-dormancy, 0-non-dormancy:Bit 0-white light, Bit 1-infrared light, Bit 2-laser, Bit 3-fan, Bit 4-speaker, Bit 5-wiper, Bit 6-motor H, Bit 7-motor V
    int iEnable;   //Whether Timing Sleep is Enabled: 0 Enabled, 1 Not Enabled
}TDormancyWork, *pTDormancyWork;

typedef struct __tagDormancyWorkV2
{
	int iSize;
	int iBeginHour;
	int iBeginMinute;
	int iBeginSecond;
	int iEndHour;
	int iEndMinute;
	int iEndSecond;
	int iWorkType;  //In bits,1-dormancy, 0-non-dormancy:Bit 0-white light, Bit 1-infrared light, Bit 2-laser, Bit 3-fan, Bit 4-speaker, Bit 5-wiper, Bit 6-motor H, Bit 7-motor V
	int iEnable;   //Whether Timing Sleep is Enabled: 0 Enabled, 1 Not Enabled
}TDormancyWorkV2, *pTDormancyWorkV2;

#define  MAX_DORMANCY_SCHEDULE 48
typedef struct __tagDormancySchedule
{	
	int iSize;
	int iWeekDay;  //Sunday - Saturday : 0-6
	TDormancyWork tWork[MAX_DORMANCY_SCHEDULE];
	int iDormancyType; //low power dormancy type
	int iSegmentNum;	//Period of time num
}TDormancySchedule, *pTDormancySchedule;

typedef struct __tagDormancyScheduleV2
{	
	int iSize;
	int iWeekDay;  //Sunday - Saturday : 0-6
	TDormancyWorkV2 tWorkV2[MAX_DORMANCY_SCHEDULE];
	int iDormancyType; //low power dormancy type
	int iSegmentNum;	//Period of time num
}TDormancyScheduleV2, *pTDormancyScheduleV2;

typedef struct __tagDormancyState
{
	int		iSize;
	int		iEnable;  //whether the device is in Dormancy state:In bits,1-dormancy, 0-non-dormancy:Bit 0-white light, Bit 1-infrared light, Bit 2-laser, Bit 3-fan, Bit 4-speaker, Bit 5-wiper, Bit 6-motor H, Bit 7-motor V
	int		iDormancyType;//0,：default，1：normal 2：low power 3 super low power 4 extreme low power
	char	cFactoryID[LEN_64];
	int     iActiveType;//0:non-active 1：active with reg 2：active without reg
	int		iWakeUpTime;
}TDormancyState, *pTDormancyState;

typedef struct __tagDormancyLowPowerParam
{
	int		iSize;
	int		iDormancyType;		//Low power sleep type 0：no sleep 1：low power sleep  2：Ultra-low power dormancy  3：Very low power  dormancy
	int		iRecoveryTime;		//range:1-7200s
	int		iPlanEnable;		//DormancyEnable 0:no use 1:Enable
	int		iPlanType;			//DormancyType  0:Period of time 1:timing
	int		iWakeUpType;		//deal by bit,bit0：mobile data bit1：phone bit2： text message
	int		iStartTime;			//When iplantype = 1, the sleep plan type is valid at fixed time, which indicates the start time of executing the fixed time plan
								//Bit24-bit31: when it is 1, it indicates that the timing start time is valid, and when it is 0, it is invalid
								//Bit16-bit23: start hour
								//Bit15-bit8: start minutes
}TDormancyLowPowerParam, *pTDormancyLowPowerParam;

typedef struct __tagDormancySetTime
{
	int		iSize;
	int	    iTime;
	int     iType;
	int     iEnable;
}TDormancySetTime, *pDormancySetTime;

typedef struct __tagDormancyTime
{
	int		iSize;
	int		iDormancyType;
	int		iTimeNum;
	TDormancySetTime tWork[MAX_TIMESLOTNUM];
}TDormancyTime, *pTDormancyTime;

typedef struct __tagNetReduceFlowCtrl
{
	int iSize;
	int iEnable;//Bit 0 - Information report, Bit1 - picture return; 0 - enabled, 1 - no enabled
}TNetReduceFlowCtrl, *pNetReduceFlowCtrl;

#define DEFAULT_STATE			0
#define NORMAL_STATE			1
#define NORMAL_LOW_POWER		2
#define SUPER_LOW_POWER			3
#define EXTREME_LOW_POWER		4

#define NO_ACTIVE			0
#define ACTIVE_WITHREG		1
#define ACTIVE_WITHOUTREG	2

typedef struct __tagDormancySet
{
	int iSize;
	int iEnable;//iDormancyType=1低功耗休眠时：按位表示，1-休眠，0-不休眠 bit0-白光灯,bit1-红外灯，bit2-激光，bit3-风扇，bit4-扬声器，bit5-雨刷 bit6-电机H，bit7-电机V
				//iDormancyType=2超低功耗休眠时：1-休眠，0-唤醒
				//iDormancyType=3极低功耗休眠时：1-休眠，0-唤醒
	int iResult;
	int iDormancyType;//0:保留；1：低功耗；2：超低功耗；3；极低功耗 (如果是唤醒设备，3不生效)
	int iActiveType;//iDormancyType = 2,3时有效，0:非主动模式 1：带目录服务器的主动模式 2：不带目录服务器的主动模式
	char cFactoryID[LEN_64];//iDormancyType = 2,3时有效
}DormancySet, *pDormancySet;

#define FILTER_CONDITION_MAX_NUM    64		//max number of filter conditions
#define FILTER_CONDITION_LEN		16		//Length of a filter condition
typedef struct tagITSIllegalParkFilterPlate
{
	int  iSize;		
	int  iSceneId;		//Scene ID,0-15
	int  iAreaNo;		//Area No,0-7
	int  iEnable;		//License plate filtering enabled,0: not enable,1:enable
	int  iFilterNum;	//number of filter conditions
	char cFilterCondition[FILTER_CONDITION_MAX_NUM][FILTER_CONDITION_LEN]; //Filter field
}ITSIllegalParkFilterPlate, *pITSIllegalParkFilterPlate;

#define PARKCARNUM_MAX_LEN          32
typedef struct tagITSParkCarNum
{
	int  iSize;
	int  iChannel;
	char cCarNum[PARKCARNUM_MAX_LEN];
	int  iParkStatus;   //0:no changes; 1:start(first report); 2:end(car leaves or record finished).
}ITSParkCarNum, *pITSParkCarNum;

typedef struct __tagVirtualGaugeCalib
{
	int iSize;       
	int iChannel;		 
	int	iScene;	       //scene num:0-15
	int	iType;	       //0:base water point, 1:Ordinary point
	int	iNum;	       //iType=0,iNum=0;iType=1,iNum=0-30
	int	iPosX;	       //per ten thousand (0-10000)
	int	iPosY;	       //per ten thousand (0-10000)
	int iMarkPosX;     //range:0-10000
	int iMarkPosY;     //range:0-10000
	int iCalcCalibPos;	//Enable calculation of laser dot coordinates
	int iCalcMarkPos;	//Enable calculation of marker point coordinates  
}VirtualGaugeCalib, *pVirtualGaugeCalib;

#define MAX_VCA_PEPT_POINT_NUM			 8
typedef struct tagVCAPEPT
{
	VCARule				tRule;					//Rules General Parameters
	vca_TDisplayParam	tDisplayParam;			//Display The Parameter Settings
	int					iDisplayTarget;			//Display Target, 0-Not Dispaly, 1-Dispaly
	int					iMaxSize;				//Max Target Size(Reserved:Percentage of image width)
	int					iMinSize;				//Min Target Size(Reserved:Percentage of image width)
	int					iSensitivity;			//Sensitivity
	int					iModelType;				//bit0: Zone Invasion bit1: Exception residency
	int					iTargetType;			//Target Type,bit1:Car bit2:SUV bit3:Pickup truck bit4:Tanker bit5:Van van bit6:Big truck bit7:Forklift bit8:Excavator bit9:Engineering bit10:Two-wheeler bit11:Tricycle bit31:Other types
	int 				iTimeThreshold;			//Resident Alarm Time
	int					iPointNum;				//Polygon Area Vertex Number
	vca_TPoint     		stPoints[MAX_VCA_PEPT_POINT_NUM];	//Vertex Coordinates		
}VCAPEPT, *pVCAPEPT;

#define MAX_CHEFHATDETECT_POINT_NUM			8
typedef struct tagVCAChefHatDetect
{
	int					iSceneID;				//scene ID(0~31)
	int					iRuleID;				//rule ID(0~15)
	int					iSensitivity;			//Sensitivity 0~100
	int					iPushMode;				//0: Reserved, 1: fastest, 2: best
	int					iPointNum;				//Polygon Area Vertex Number
	vca_TPoint     	stPoints[MAX_CHEFHATDETECT_POINT_NUM]; //Vertex Coordinates		
}VCAChefHatDetect, *pVCAChefHatDetect;

#define MAX_CHEFMASKDETECT_POINT_NUM		8
typedef struct tagVCAChefMaskDetect
{
	int					iSceneID;				//scene ID(0~31)
	int					iRuleID;				//rule ID(0~15)
	int					iSensitivity;			//Sensitivity 0~100
	int					iPushMode;				//0: Reserved, 1: fastest, 2: best
	int					iPointNum;				//Polygon Area point Number
	vca_TPoint     	stPoints[MAX_CHEFMASKDETECT_POINT_NUM]; //Vertex Coordinates		
}VCAChefMaskDetect, *pVCAChefMaskDetect;

#define CHEFDETECT_TYPE_CLOTHES			0	//chef's clothes
#define MAX_CHEFDETECT_TYPE				1	//max chef detect type
#define CHEFDETECT_PUSHPICMODE_RESERVE	0	//push graph strategy reserve
#define CHEFDETECT_PUSHPICMODE_FAST		1	//push graph strategy fast
#define CHEFDETECT_PUSHPICMODE_BEST		2	//push graph strategy best
#define MAX_CHEFDETECT_POINT_COUNT		8	//max number of vertices in polygon area 
typedef struct tagVCAChefDetect
{
	int					iType;					//input para:0: to detect chef's clothes
	int					iSceneID;				//input para:scene ID(0~31)
	int					iRuleID;				//input para:rule ID(0~15)
	int					iSensitivity;			//Sensitivity 0~100
	int					iPushMode;				//0: Reserved, 1: fastest, 2: best
	int					iPointCount;			//Polygon Area point count
	vca_TPoint     		tPoints[MAX_CHEFDETECT_POINT_COUNT]; //Vertex Coordinates		
} VCAChefDetect, *pVCAChefDetect;

typedef struct tagVCASenceSnap
{
	int		iSnapEnable;						//Scene Snap,  1-enable 0-not enable
}VCASenceSnap, *pVCASenceSnap;

typedef struct __tagVirtualGaugeCalibResult
{
	int iSize;
	int iChannel;		
	int	iScene;	       //scene num:0-15
	int	iType;	       //0:base water point, 1:Ordinary point
	int	iNum;	       //iType=0,iNum=0;iType=1,iNum=0-30
	int	iPosX;	       //per ten thousand (0-10000)
	int	iPosY;	       //per ten thousand (0-10000)
	int iMarkPosX;     //range: 0-10000
	int iMarkPosY;     //range: 0-10000
	int iDistance;
	int iAltitude;     //RTK data
	int iTiltAngle;    //Angle
	int iWaterLevel;   //Water Level,iType = 0  valid
	int iRiverWidth;   //the River of Width iType = 0  valid
	int iResult;       //Calib Result,0:success, -1:failed
	int iCalcCalibPos;	//Enable calculation of laser dot coordinates
	int iCalcMarkPos;	//Enable calculation of marker point coordinates 
}VirtualGaugeCalibResult, *pVirtualGaugeCalibResult;


typedef struct __tagVirtualGaugeParam
{
	int  iSize;
	int  iScene;            //scene num:0-15
	int  iType;             //0:base water point, 1:Ordinary point
	int  iSum;              //total num
	int  iNum;              
	int  iPosX;             //per ten thousand (0-10000)
	int  iPosY;             //per ten thousand (0-10000)
	int  iMarkPosX;         //range:0-10000
	int  iMarkPosY;         //range:0-10000
	int  iDistance;
	int  iAltitude;         //RTK data
	int  iTitleAngle;        //Angle
	int  iWaterLevel;       //Water Level,iType = 0  valid
	int	 iCalibType;		//Laser dot coordinate type
	int	 iMarkType;			//Mark point coordinate type
}VirtualGaugeParam, *pVirtualGaugeParam;


typedef struct __tagUploadStateInfo
{
	int      iSize;
	int      iStation;
}UploadStateInfo, *pUploadStateInfo;

typedef struct __tagSetQueryFlow
{
	int               iSize;
	int               iChn;
	int               iPageNo;        //page num
	int               iPageSize;      //info num in every page
    NVS_FILE_TIME     tStartTime;     //query from time
	NVS_FILE_TIME     tEndTime;       //query end time
}SetQueryFlow, *pSetQueryFlow;

typedef struct __tagSetQueryFlowEmbed
{
	int               iSize;
	int               iChn;
	int               iPageNo;
	int               iPageSize;
	int               iStartTime;
	int               iEndTime;
}SetQueryFlowEmbed, *pSetQueryFlowEmbed;

typedef struct __tagQueryFlowInfo
{
	int                iSize;
	int                iTotal;	       //total info num
	int  	           iPageNo;	       //current page no
	int                iPageSize;	       //current info num in current page
	int    	           iIndex;           //info index
	int                iPlace;           //Station number
	int                iScene;           //Scene num
	unsigned int       iGroup;           
	NVS_FILE_TIME      tRecordTime;     //Record time
	int                iWaterSpeed;	   //Water Speed
	int                iWaterLevel;	   //Water Level
	int                iDistance;	
	int                iSource;			//0 Algorithm Get; 1 Peripheral Get
	int                iRuleID;			//0~7
	int		           iDetectNum;        //Detection points  1~30
	int                iCurrentNum;		//Current nod number
	NVS_FILE_TIME      tStartTime;     //Video start time
}QueryFlowInfo, *pQueryFlowInfo;

typedef struct __tagQueryFlowInfoEmbed
{
	int                iSize;
	int                iTotal;	       //total info num
	int  	           iPageNo;	       //current page no
	int                iPageSize;	       //current info num in current page
	int    	           iIndex;           //info index
	int                iPlace;           //Station number
	int                iScene;           //Scene num
	unsigned int       iGroup;           
	int                iRecordTime;     //Record time
	int                iWaterSpeed;	   //Water Speed
	int                iWaterLevel;	   //Water Level
	int                iDistance;	
	int                iSource;			//0 Algorithm Get; 1 Peripheral Get
	int                iRuleID;			//0~7
	int		           iDetectNum;        //Detection points  1~30
	int                iCurrentNum;		//Current nod number
	int                iStartTime;     //Video start time
}QueryFlowInfoEmbed, *pQueryFlowInfoEmbed;

typedef struct __tagAutoJust
{
	int					iSize;
	int					iChanNo; //channel no
	int                 iFieldOfVision;	//0-保留 1-大 2-小
}AutoJust, *pAutoJust;

typedef struct __tagSceneHeight
{
	int                 iSize;
	int                 iChannel;
	int					iAutoZoom;// 自动调整倍率 0-保留，1-开启，2-关闭
}SceneHeight, *pSceneHeight;

typedef struct __tagSceneHeightResult
{
	int                 iSize;
	int                 iChannel;
	int                 iResult;
}SceneHeightResult, *pSceneHeightResult;

typedef struct _tagSinglePic
{
	int                 iSize;
	int                 iVoDevId; //1-VGA/HDMI0, 17-HDMI1(bt1120), 33-CVBS,49-VC,50-VC1（H264） ,Default:VC
	int                 iAct;     //1:Entry SinglePic 2:Exit SinglePic
}SinglePic, *pSinglePic;



typedef struct tagGpsCoordinate
{
	int				iDirection;
	int				iDegree;
	int				iMinute;
	int				iSecond;
}GpsCoordinate, *pGpsCoordinate;

typedef struct tagGpsPointInfo
{
	GpsCoordinate		tLongitudeInfo;	//Longitude iDirection:(0-e, 1-w), degrees (0-180), minutes (0-59), seconds (0-59.99*100)
	GpsCoordinate		tLatitudeInfo;	//Latitude iDirection:(0-s, 1-n), degrees (0-180), minutes (0-59), seconds (0-59.99*100)
}GpsPointInfo, *pGpsPointInfo;

#define MAX_GPS_AREA_NUM			 128
#define MAX_GPS_POINT_NUM			 8
typedef struct tagGpsDangerArea
{
	int					iSize;
	int					iChanNo;	//channel no
	int					iAreaId;	//Area Id,range:0-127
	int					iPointNum;	//Number of vertices in the region,0 indicates the removal of the area.
	GpsPointInfo		tPointInfo[MAX_GPS_POINT_NUM];	//Point coordinates
}GpsDangerArea, *pGpsDangerArea;

#define MAX_POLYGON_POINT_NUM		 16
typedef struct tagNavigationShipDetection
{
	int					iSize;
	int					iChanNo;		//channel no
	int					iSceneId;		//range:0~31
	int					iRuleNo;		//range:0～15
	int					iSensitiv;		//sensitive:range:0~100
	int					iPointNum;		//Number of vertices in a polygon area,rang:3～8
	TPoint				tPoint[MAX_POLYGON_POINT_NUM];	//Point coordinates
}NavigationShipDetection, *pNavigationShipDetection;

typedef struct tagLocate3DPrediction
{
	int					iSize;
	int					iChanNo;		//channel no
	int					iTime;			//Prediction time: unit/s 1-30s
	int					iMultiple;		//Area magnification 1-5
}Locate3DPrediction, *pLocate3DPrediction;



#define MAX_CRUISE_ADD_COUNT  2
#define MAX_AREA_TYPE    2
#define SET_AREA         1
#define CALL_AREA        2
#define DELETE_AREA      3
typedef struct __tagDetectAreaPoseX
{
	int                 iSize;
	int                 iSceneId;  //Scene Num:0-15
	int                 iCruiseAddMode; // 0:Auto;1:Manual
	int                 iAreaNum;  //0-127
	int                 iOperationType;//1-Set Area 2-Call Area 3-Detect Area
	int                 iAreaType;//0-流速 1-水位水尺
}DetectAreaPoseX, *pDetectAreaPoseX;

typedef struct __tagDetectAreaPoseXResult
{
	int                 iSize;
	int                 iResult;  //0:success, 1:fail
	int                 iSceneID;
	int                 iCruiseAddMode; //0:Auto;1:Manual
	int                 iOperationType;////1-Set Area 2-Call Area 3-Detect Area
	int                 iAreaNum;//0-127
    int                 iPtzP;
	int                 iPtzT;
	int                 iPtzZ;
	int                 iResultInfo;	//If the iresult field is not 0, it is valid to indicate the error information of the corresponding error code; if iresult = 3, it is the current diagonal distance in meters
    int                 iAreaType;//0-流速 1-水位水尺 2-零点预置位
	int                 iAngleValue;//倾角仪角度x10000，单位是°，范围0—360
}DetectAreaPoseXResult, *pDetectAreaPoseXResult;

typedef struct __tagDetectAreaParaX
{
	int                 iSize;
	int                 iSceneId;  //Scene Num:0-15
	int                 iCruiseAddMode; //0:Auto;1:Manual
	int                 iAreaNum;  //0-127
	int                 iState;//0:unset, 1;set
	int                 iPtzP;
	int                 iPtzT;
	int                 iPtzZ;
	int                 iAreaType;//0-流速 1-水位水尺 2-零点预置位
	int                 iAngleValue;//倾角仪角度x10000，单位是°，范围0—360
}DetectAreaParaX, *pDetectAreaParaX;

typedef struct tagCallParamResult
{
	int		iBufSize;					//Call gengeral parameters structure size
	int		iType;						//0-Call Scene ID；1-Call privacy shelter location 2- call vca scene 3 - enable intelligent scene quickly 4-quickly start warning scene, 5-fire detection shielding area, 6-smoke detection shielding area, 7-temperature difference detection shielding area
	int		iValue;						//Scene ID range：0~15； privacy shelter location range：0~23
	int		iSceneType;					//0-vca 1-alert
	int     iResult;                    //0:success;other:failed
}CallParamResult, *pCallParamResult;

typedef struct tagHolidayScheTime
{
	int     iSize;
	int     iStartHour;
	int     iStartMin;
	int     iEndHour;
	int     iEndMin;
}HolidayScheTime, *pHolidayScheTime;

#define MAX_HOLIDAY_PLAN_ID   8
#define MAX_HOLIDAY_PARAM_ID  32
typedef struct tagHolidaySche
{
	int               iSize;
	int               iPlanID; //planid:0-7
	int               iEnable; //0:not set Sche, 1:SetSchedule
	char              pcPlanName[LEN_32];
	HolidayScheTime   tScheDule;
}HolidaySche, *pHolidaySche;

typedef struct tagSetPlanByDate
{
	int              iYear;
	int              iMonth;
	int              iDay;
}SetPlanByDate, *pSetPlanByDate;

typedef struct tagSetPlanByWeek
{
	int              iMonthofYear;
	int              iWeekofMonth;
	int              iDayofWeek;
}SetPlanByWeek, *pSetPlanByWeek;

typedef struct tagSetPlanByMonth
{
	int            iMonth;
	int            iDay;
}SetPlanByMonth, *pSetPlanByMonth;

typedef union tagPlanTime
{
	SetPlanByDate   tSetByDate;
	SetPlanByWeek   tSetByWeek;
    SetPlanByMonth  tSetByMonth;
	int             iCommonInt[LEN_4];
}PlanTime, *pPlanTime;

#define SET_HOLIDAY_BYDATE    0
#define SET_HOLIDAY_BYWEEK    1
#define SET_HOLIDAY_BYMONTH   2
#define SET_MAX_HOLIDAY_MODE  3
typedef struct tagHolidayPlan
{
	int               iSize;
	int               iHolidayID;
	int               iEnable;
	int               iPlanID;
	int               iSetMode;   ////Time format,0:By date (year month day) 1:By week(month  week) 2:By month(Month day)
	char              pcHolidayName[LEN_32];
	PlanTime          tStartTime;
	PlanTime          tStopTime;
}HolidayPlan, *pHolidayPlan;

typedef struct tagTrafficFlowQuery
{
	int            iSize;
	int            iChn;
	int            iPageNo;
	int            iPageSize;
	NVS_FILE_TIME  tStartTime;
	NVS_FILE_TIME  tEndTime;
}TrafficFlowQuery, *pTrafficFlowQuery;

typedef struct tagCarTypeNum
{
	int            iSize;
	int            iCarType;
	int            iCarNum;
}CarTypeNum, *pCarTypeNum;

typedef struct tagTrafficFlowInfo
{
	int            iSize;
	int            iChn;           //chn Number
	int            iTotalNum;      //total info numbers
	int            iPageNo;        //current page No
	int            iPageSize;      //info numbers in this pageNo
	int            iIndex;         //info No
	int            iLaneNo;        //0-5
	char           pcRoadName[LEN_64]; 
	NVS_FILE_TIME  tStartTime;
	NVS_FILE_TIME  tEndTime;
	int            iFlow;
	int            iHoldRate;      //percent
	int            iSpeed;         //Km/h
	int            iDistance;      //Second
	int            iCarTypeTotal;  
	CarTypeNum     tCarTypeNum[LEN_32];//carType1:num1,......carTypen:numN
	int            iCarQueueLength;         //mm
	int            iHeadDistance;           //mm
	int            iRoomRate;               //Spacing percent
	int            iRunState;               //state:0:Unknown 1:stop 2:slow 3:No car 4:fluent
	int            iSceneID;       //0~31
}TrafficFlowInfo, *pTrafficFlowInfo;

typedef struct tagTencentInfo
{
	int   iSize;
	char  pcURL[LEN_64];         //url address
	char  pcID[LEN_64];          //ID
	char  pcKey[LEN_64];
}TencentInfo, *pTencentInfo;

#define MAX_POLYGON_AREA_POINT_NUM		 8
typedef struct tagGranaryVehicleDetect
{
	int					iSize;
	int					iChanNo;		//channel no
	int					iSceneId;		//range:0~31
	int					iRuleNo;		//range:0～15
	int					iSensitiv;		//sensitive:range:0~100
	int					iPointNum;		//Number of vertices in a polygon area,rang:3～8
	TPoint				tPoint[MAX_POLYGON_AREA_POINT_NUM];	//Point coordinates
}GranaryVehicleDetect, *pGranaryVehicleDetect;

typedef struct tagGranaryVehicleDelay
{
	int					iSize;
	int					iChanNo;		//channel no
	int					iTime;			//unit:s 3D positioning operation is carried out after the vehicle's stationary time reaches the set time
	int					iPreset;		//Preset num
}GranaryVehicleDelay, *pGranaryVehicleDelay;

typedef struct tagModifyFrontPsd
{
	int           iSize;
	char          pcUserName[LEN_16];
	char          pcOldPassword[LEN_16];
	char          pcNewPassword[LEN_16];
}ModifyFrontPsd, *pModifyFrontPsd;

#define MAX_VCA_STRANDED_POINT_NUM			 10
typedef struct tagVCAStranded
{
	int					iDevType;				//0-IPC，1-NVR
	VCARule				tRule;					//Rules General Parameters
	vca_TDisplayParam	tDisplayParam;			//Display The Parameter Settings
	int					iDisplayTarget;			//Display Target, 0-Not Dispaly, 1-Dispaly
	int					iMaxSize;				//Max Target Size(Reserved:Percentage of image width)
	int					iMinSize;				//Min Target Size(Reserved:Percentage of image width)
	int					iSensitivity;			//Sensitivity
	int					iAlarmTime;				//Alarm Time
	int					iPointNum;				//Polygon Area Vertex Number
	vca_TPoint     		stPoints[MAX_VCA_STRANDED_POINT_NUM];	//Vertex Coordinates		
}VCAStranded, *pVCAStranded;

typedef struct tagVCAAlone
{
	VCACommonPara		tVCACommonPara;
	int					iAlarmTime;				//Alarm Time	
}VCAAlone, *pVCAAlone;

#define MAX_VCA_DELIVERGOODS_POINT_NUM			 10
typedef struct tagVCADeliverGoods
{
	int					iDevType;				//0-IPC，1-NVR
	VCARule				tRule;					//Rules General Parameters
	vca_TDisplayParam	tDisplayParam;			//Display The Parameter Settings
	int					iDisplayTarget;			//Display Target, 0-Not Dispaly, 1-Dispaly
	int					iMaxSize;				//Max Target Size(Reserved:Percentage of image width)
	int					iMinSize;				//Min Target Size(Reserved:Percentage of image width)
	int					iSensitivity;			//Sensitivity
	int					iPointNum;				//Polygon Area Vertex Number
	vca_TPoint     		stPoints[MAX_VCA_DELIVERGOODS_POINT_NUM];	//Vertex Coordinates		
}VCADeliverGoods, *pVCADeliverGoods;

typedef struct tagVCATops
{
	int					iChanNo;
	int					iDevType;				//0-IPC，1-NVR
	int					iSceneID;				//
	int					iVcaType;
	int					iTops;					//Reference value of calculation force occupied by this algorithm, range 1-100
	int					iMaxCnt;				//The maximum number of this algorithm to open, range 1-100
	int					iUsedCnt;				//The number of this algorithm has been opened, range 0-100
}VCATops, *pVCATops;


typedef struct tagShdbRunState
{
	int					iSize;
	int					iRunState;				//Running state, 0: not running; 1: running 
}ShdbRunState, *pShdbRunState;


#define MAX_SHDB_CHAN_NUM   1024 
typedef struct tagShdbAlarmPic
{
	int					iSize;
	int					iEnable;				//Alarm linkage enable, 0: not enable; 1: Enable 
	int					iPreTm;					//Pre shooting time, settable: 0, 1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 60s 
	int					iDelayTm;				//Delay time, settable: 0, 1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 60s 
	int					iIntervalTm;			//Time interval, settable: 3,4,5,10,20,30,40,50,60s 
	int					iChanCount;				//channel number
	int					iChanList[MAX_SHDB_CHAN_NUM];
}ShdbAlarmPic, *pShdbAlarmPic;


#define MAX_SHDB_TIMESNAPGROUPS_NUM   4
#define CUR_SHDB_TIMESNAPGROUP_NUM    2
typedef struct tagShdbTimeSnapGroups
{
	int					iTmEnable;				//Point in time enable, 0: not enable, 1: Enable 
	int					iPicCnt;				//Capture times, temporarily fixed to 1 
	int					iBeginTm;				//Start time unit s 
	int					iEndTm;					//End time in seconds 
	int					iChanCount;				//channel number
	int					iChanList[MAX_SHDB_CHAN_NUM];
}ShdbTimeSnapGroups, *pShdbTimeSnapGroups;

typedef struct tagShdbTimeSnap
{
	int					iSize;
	int					iEnable;								//Daily Photo enabled, 0: not enabled; 1: enabled 
	int					iTmSnapGrpNum;							//Number of time point capture groups, maximum 4 groups, currently only 2 groups are open 
	ShdbTimeSnapGroups  tParam[MAX_SHDB_TIMESNAPGROUPS_NUM];	//Time point capture group number structure 
}ShdbTimeSnap, *pShdbTimeSnap;



typedef struct tagQueryShdbApprepairSys
{
	int					iSize;
	int					iRepairName;		    //Name of repair reporting system, 0: building intercom; 1: perimeter alarm; 2: networking alarm; 3: local alarm; 4: video security; 5: access control; 6: electronic patrol system; 7: other security systems; 
	int					iRepairType;		    //Repair type: 0: front end; 1: terminal; 2: system;
}QueryShdbApprepairSys, *pQueryShdbApprepairSys;

typedef struct tagShdbApprepairSysResult
{
	int					iSize;
	int					iRepairName;		   //Name of repair reporting system, 0: building intercom; 1: perimeter alarm; 2: networking alarm; 3: local alarm; 4: video security; 5: access control; 6: electronic patrol system; 7: other security systems; 
	int					iRepairType;           //Repair type: 0: front end; 1: terminal; 2: system;
	int					iResult;               //Execution result, 0: success; - 1: failure 
}ShdbApprepairSysResult, *pShdbApprepairSysResult;



typedef struct tagQueryShdbServiceType 
{
	int					iSize;
	int					iMaintainType;		  //Maintenance type, 0: normal; 1: exception repaired; 2: exception not repaired; 3: not selected 
	int					iRepairType;          //Repair type, 0: normal; 1: abnormal repaired; 2: abnormal not repaired; 3: not selected 
}QueryShdbServiceType, *pQueryShdbServiceType;

typedef struct tagShdbServiceTypeResult
{
	int					iSize;
	int					iMaintainType;		 //Maintenance type, 0: normal; 1: exception repaired; 2: exception not repaired; 3: not selected 
	int					iRepairType;		 //Repair type, 0: normal; 1: abnormal repaired; 2: abnormal not repaired; 3: not selected 
	int					iResult;			 //Execution result, 0: success; - 1: failure 
}ShdbServiceTypeResult, *pShdbServiceTypeResult;



typedef struct tagQueryShdbTestMainTain
{
	int					iSize;
	int					iOperType;			 //Operation type: 0: upload test picture; 1: upload maintenance picture
	int					iChanCount;			 //channel number
	int					iChanSize;			 //struct ChannelList size
	ChannelList*		pChanList;			 //channel list
}QueryShdbTestMainTain, *pQueryShdbTestMainTain;

typedef struct tagShdbTestMainTainResult
{
	int					iSize;
	int					iOperType;			 //Operation type: 0: upload test picture; 1: upload maintenance picture
	int					iChanCount;          //channel number
	int					iChanSize;			 //struct QueryChanNo size
	ChannelList*		pChanList;			 //channel list
	int					iResult;			 //Execution result, 0: success; - 1: failure
}ShdbTestMainTainResult, *pShdbTestMainTainResult;


#define  MAX_SHDB_POLICELD_NUM    32
#define  MAX_SHDB_PASSWD_NUM      32


typedef struct tagQueryShdbCheckManage
{
	int					iSize;
	char				cPoliceId[MAX_SHDB_POLICELD_NUM];	//Officer, 32 Max 
	char				cPasswd[MAX_SHDB_PASSWD_NUM];		//Password, up to 32 bits 
	int					iChanCount;							//channel number
	int					iChanSize;							//struct ChannelList size
	ChannelList*		pChanList;							//channel list
}QueryShdbCheckManage, *pQueryShdbCheckManage;

typedef struct tagShdbCheckManageResult
{
	int					iSize;
	char				cPoliceId[MAX_SHDB_POLICELD_NUM];   //Officer, 32 Max 
	char				cPasswd[MAX_SHDB_PASSWD_NUM];		//Password, up to 32 bits 
	int					iChanCount;							//channel number
	int					iChanSize;							//struct ChannelList size
	ChannelList*		pChanList;							//channel list
	int					iResult;							//Execution result, 0: success; - 1: failure
}ShdbCheckManageResult, *pShdbCheckManageResult;

typedef struct tagHsjjFocusstate
{
	int						iSize;
	int						iRunState;							//0：未聚焦完成；1：聚焦完成
}HsjjFocusstate, *pHsjjFocusstate;

typedef struct tagAlmLoopDetec
{
	int                 iSize;        
	int                 iChannelNo;                   //channel number，0~N-1：ipc parameters of corresponding IPC channel； 
	                                                  //0x7fffffff：NVR local parameters
	int                 iPortNo;                      //port number
	int                 iEnable;                      //whether to open：0, close；1, open
} AlmLoopDetec, *pAlmLoopDetec;

typedef struct tagIdentificationType
{
	int                 iSize;
	int                 iChannelNo;                    //channel number，0~N-1：ipc parameters of corresponding IPC channel；
	                                                   //0x7fffffff：NVR local parameters
	int                 iProtocol;		               //protocol type, 0：rtsp，1：http
	int                 ibasicConfig;	               //basic auth config, 0：forbid ，1：allow
	int                 iDigestlConfig;                //digest认证配置,  0：forbid ，1：allow
	int					iAbstractAlgorithm;			   //摘要算法；iProtocol=0或1时，有效0代表无此功能，1代表MD5，2代表SHA256,3代表MD5/SHA256
	int					iWSSEConfig;				   //WSSE认证配置	"iProtocol=2时，有效0：禁止 ，1：允许"					
}IdentificationType, *pIdentificationType;

typedef struct tagDevAutoTimingParam
{
	int					iSize;
	int					iType;				//Timing type, 0-never, 1-year cycle, 2-month cycle, 3-week, 4-day 
	int					iWeekDay;			//Starting time: 1-7 from Monday to Sunday, valid when itype is 3 
	int					iMonth;				//Month, value range is 1-12, valid when itype = 1 
	int					iDay;				//Day, value range is 1-31, valid when itype = 1,2
	int					iHour; 				//Hour,value range is 0-23,valid When itype = 1,2,3,4
	int					iMinute;			//Minute,value range is 0-23,valid When itype = 1,2,3,4
	int					iSecond;			//Second,value range is 0-23,valid When itype = 1,2,3,4
}DevAutoTimingParam, *pDevAutoTimingParam;

typedef struct __tagIpFilter
{
	int iSize;
	char cIp[LENGTH_IPV4];
	char cSubMask[LENGTH_SUB_MASK];
	int iFilterType;								//Filter typr:0,Disable IP permission qualification;1,IP access is disabled;2,Allow IP access
	int iIpNum;										//Filter IP number,the maximum support 16 different IP addresses
	char cFilterIp[MAX_IP_FILTER_NUM][LENGTH_IPV4];	//Network address string, such as：pcFilterIp[0]=“192.168.1.10”
}TIpFilter, * pTIpFilter;

typedef struct __tagIpv6Filter
{
	int iSize;
	int iIpNum;												//Filter IP number,the maximum support 16 different IP addresses
	int iFilterType;										//Filter typr:0,Disable IP permission qualification;1,IP access is disabled;2,Allow IP access
	char pcFilterIp[MAX_IP_FILTER_NUM][LENGTH_IPV6_V1];		//Network address string, such as：pcFilterIp[0]=“1234:5678:9abc:def::1”
}TIpv6Filter, * pTIpv6Filter;

typedef struct tagIpv6FilterParam
{
	int					iSize;				
	int					iFilterType;														//过滤类型(0-禁用IP权限设定,1-禁止IP访问,2-允许IP访问)
	int					iIpNum;																//过滤ip数量(iIpNum 最大支持48个不同的IP地址)
	int					iPackageNum;														//总包数
	int					iListIndex;															//列表序号(从1开始)
	int					iParamNum;															//参数列表长度(每次发送的列表数目)
	char				pcFilterIp[MAX_IPV6_PACKAGE_FILTER_NUM][LENGTH_IPV6_V1];			//IPv6地址(网络地址字符串。如：pcFilterIp1=“1234:5678:9abc:def::1”或“192.168.1.10”（最大长度为64字节）)
}Ipv6FilterParam, * pIpv6FilterParam;

typedef struct tagCloudAutoDetect
{
	int					iSize;
	int					iChannelNo;				//Channel number, 1-NVR when upgrading ipc channel number starts from 0, 2-NVR cloud upgrade channel number is 0x7FFFFFFF
	int					iNewVerStat;			//is there a new version, 0-no new version, 1-new version
	char				cNewVer[LEN_64];		//new version, 64 bytes
	char				cReleaseData[LEN_32];	//Release time, 32 bytes, YYYY-MM-DD
}CloudAutoDetect, *pCloudAutoDetect;

typedef struct tagDeviceName
{
	int                 iSize;
	int                 iChannelNo;              //channel number，0~N-1：ipc parameters of corresponding IPC channel；
	                                             //0x7fffffff：NVR local parameters
	char                pcDeviceName[LEN_32];    //device name
}DeviceName, *pDeviceName;

typedef struct tagChnDevInfo
{
	int					iSize;
	int					iChannelNo;					// output para,Channel number, iChannelNo according to the actual situation to determine iChannelNo= 0~n-1
	char				cMac[LEN_64];				// output para,Mac address, no more than 64 bytes
	char				cFactoryID[LEN_64];			// output para,Factory ID, no more than 64 bytes
	char				cBarCode[LEN_64];			// output para,Bar code, no more than 64 bytes
}ChnDevInfo, *pChnDevInfo;

typedef struct tagDomainParsePara
{
	int				iSize;
	char			cUserID[LEN_16];							//username
	char			cPassWord[LEN_16];							//password
	char			cHostName[LEN_32];							//device name
	int				iInterval;									//Registration interval
	char			cDomain1[LEN_32];							//Server 1 domain name
	char			cDomain2[LEN_32];							//Server 2 domain name
	int				iPort1;										//Server port 1
	int				iPort2;										//Server port 2
	char			cDomain1v6[LENGTH_IPV6_V1];					//Server 1 domain name
	char			cDomain2v6[LENGTH_IPV6_V1];					//Server 2 domain name
}DomainParsePara, *pDomainParsePara;

typedef struct	tagNVS_NFS_DEVEx
{
	int		iSize;
	char	cDeviceIP[LEN_16];					//NFS IP
	char	cStorePath[LEN_250];				//NFS Path
	char	cUsername[LEN_16];					//User Name
	char	cPassword[LEN_16];					//Password
	int		iState;								
	char	cDeviceIPv6[LENGTH_IPV6_V1];		//IPv6
}NVS_NFS_DEVEx,*pNVS_NFS_DEVEx;

typedef struct tagWaterLevelSource
{
	int				iSize;				//input para: Structure waterlevelsource size 
	int				iChannelNo;			//input para: Channel number 
	int				iType;				//input para: Water level data source type, 0-reserved; 1-manual setting; 2-network access; 3-peripheral access,4-water level algorithm acquisition
	int				iValue;				//input para: Water level in mm, valid when itype = 1 	
}WaterLevelSource, *pWaterLevelSource;

typedef struct tagWaterLevelSourceID
{
	int				iSize;				//input para: Structure waterlevelsourceid size 
	int				iChannelNo;			//input para: Channel number 
	char			cFactoryID[LEN_64];	//input para: Factory id, no more than 64 bytes 
}WaterLevelSourceID, *pWaterLevelSourceID;


#define IPV6_ADDRLIST_NUM_MAX				16

#define IPV6_ADDRESS_MANUAL_MODE				1	//MANUAL
#define IPV6_ADDRESS_DHCP_MODE					2	//DHCP
#define IPV6_ADDRESS_ANNOUNCEMENT_MODE			3	//Routing announcement mode
#define IPV6_ADDRESS_MODE_MAX					4	//Mode max num

typedef struct tagIPV6AddrAndPrefix
{
	char			cIP[LENGTH_IPV6_V1];	
	int				iPrefixLen;			
}IPV6AddrAndPrefix, *pIPV6AddrAndPrefix;

typedef struct tagIPV6AddrList
{
	int					iSize;									//input para: Structure waterlevelsourceid size 
	int					iChannelNo;								//input para: Channel number 
	int					iLanNo;									//input para: Lan number 0-MAX_LAN_NUM
	int					iIPMode;								//input para: 1-MANUAL mode 2-DHCP mode ,3-Routing announcement mode
	int					iIPListNum;								//output para: IP num
	IPV6AddrAndPrefix	tIPInfo[IPV6_ADDRLIST_NUM_MAX];			//ooutput para: IP address list
}IPV6AddrList, *pIPV6AddrList;


#define MAX_SMOKEDETECT_POINT_NUM			8
typedef struct tagVCASmokeDetect
{
	int					iSceneID;						     //scene ID(0~31)
	int					iRuleID;						     //rule ID(0~15)
	int                 iValid;					             //0;invalid; 1:valid(default:0)
	int					iSensitivity;					     //Sensitivity 0~100
	int					iPointNum;						     //Polygon Area point Number
	vca_TPoint     	    stPoints[MAX_SMOKEDETECT_POINT_NUM]; //Vertex Coordinates		
}VCASmokeDetect, *pVCASmokeDetect;

#define MAX_PHONEDETECT_POINT_NUM		8
typedef struct tagVCAPhoneDetect
{
	int					iSceneID;						     //scene ID(0~31)
	int					iRuleID;						     //rule ID(0~15)
	int                 iValid;					             //0;invalid; 1:valid(default:0)
	int					iSensitivity;					     //Sensitivity 0~100
	int					iPointNum;						     //Polygon Area point Number
	vca_TPoint     	    stPoints[MAX_PHONEDETECT_POINT_NUM]; //Vertex Coordinates		
}VCAPhoneDetect, *pVCAPhoneDetect;

#define MAX_SLUICEGATE_CEIL_COUNT	10	//MAX_VCA_SLUICEGATE_COUNT*2

typedef struct tagCeilLineInfo
{	
	vca_TPoint			tPointStart;			
	vca_TPoint			tPointEnd;	
}CeilLineInfo, *pCeilLineInfo;

typedef struct tagVcaSluiceGateCeil
{
	int					iSize;										//input para:Structure size
	VCARule				tRule;										//input para
	int					iCeilLineCount;								//input para:Ceil line count
	CeilLineInfo		tCeilLineInfo[MAX_SLUICEGATE_CEIL_COUNT];	//output para:Ceil line point info
}VcaSluiceGateCeil, *pVcaSluiceGateCeil;

typedef struct tagOsdClarity
{
	int				iSize;								// input para Structure size
	int				iOsdType;							// input para Character type, 0: channel name 1: Date Time 2: additional character 1 3: additional character 2 4: additional character 3 5: additional character 4 6: additional character 5 
	int				iBClarity;							// input para Background transparency, 0-100, smaller, more transparent 
	int				iFClarity;							// input para Foreground transparency, 0-100, smaller, more transparent 
}OsdClarity, *pOsdClarity;

typedef struct tagLedPowerLimit
{
	int				iSize;							// output para Structure size
	int				iNearLEDPower;					// output para The power consumption per unit brightness of the approach lamp is increased by 1000 times. For example, when the power consumption is 0.05W, the value of this field is 50, the same below 
	int				iFarLEDPower;					// output para The power consumption per unit brightness of the far lamp is increased by 1000 times 
	int				iBasePower;						// output para The basic power consumption of the system is increased by 1000 times 
	int				iBasePowerRatio;				// output para Basic power consumption coefficient of the system 
	int				iOtherPower;					// output para Other power consumption, actual power consumption increased by 1000 times 
	int				iOtherRatio;					// output para Other coefficient, multiple of other power consumption when calculating power consumption 
	int				iLimitPower;					// output para Limit the power consumption value, and increase the actual power consumption value by 1000 times 
}LedPowerLimit, *pLedPowerLimit;

typedef struct tagFaceOsdTimeFormat
{
	int             iSize;  // input para Structure size
	int             iCapType;  
	char            cYearText[LEN_8];  //Format symbol after year
	char            cMonthText[LEN_8]; //Format symbol after month
	char            cDayText[LEN_8];   //Format symbol after day
	char            cHourText[LEN_8];  //Format symbol after hour
	char            cMinText[LEN_8];   //Format symbol after min
	char            cSecText[LEN_8];   //Format symbol after sec
	char            cMilsecText1[LEN_8];//Format symbol after milsec1
	char            cMilsecText2[LEN_8];//Format symbol after milsec2
}FaceOsdTimeFormat, *pFaceOsdTimeFormat;

typedef struct tagFaceOsdWordOverlay
{
	int             iSize;        //input para Structure size
	int             iOsdDisplayIndex;   //
	int             iEnable;            //control context show
	char            pcOsdName[LEN_32];  //osd name
	int             iPosX;              //PosX
	int             iPosY;              //PosY
	int             iZoomScale;         //[0-500].100 represents no change
	int             iLineCharNum;       //Maximum characters per line
	int             iBlankCharNum;      //Number of starting spaces after line brea
	int             iOsdPosNo;          //osd Postion Number
	int             iCapType;           //capture type
	int             iFacePicType;       //small picture size:0,default;1.small;2.middle;3,big
}FaceOsdWordOverlay, *pFaceOsdWordOverlay;

typedef struct tagFaceOsdCommonDev
{
	int             iSize;  //input para Structure size
	int             iOsdType;//0-reserved, 1-mechanism code, 2-mechanism name, 3-capture image character overlay bit, 6-composite image character overlay bit, 7-face overlay OSD
	char            cCommonInfo[LEN_64];//ods content
	int             iCapType;//capture type
}FaceOsdCommonDev, *pFaceOsdCommonDev;

typedef struct tagImageLocation
{
        int            iSize; //input para Structure size
        int            iLocation; //0-normal，1-turnover，2-h_mirror，3-v_mirror，4-视频旋转
        int            iDirection; //旋转方向	0-保留，1-顺时针，2-逆时针
        int            iAngle; //旋转角度	范围0~360，表示旋转角度
}ImageLocation, *pImageLocation;

typedef struct tagPtzResult
{
	int iSize;
	int iPtzP; //0~3600000 [0-360]，accurancy：0.0001
	int iPtzT; //100000~1900000 [-90 - 90]accurancy：0.0001
	int iPtzZ; //0~100000 [0-1000]，accurancy:0.01
}PtzResult, *pPtzResult;

typedef struct
{
	int         m_iStructSize;        //Structure size
	char        m_cVersion[LEN_32];       //Server version number，内核版本，若m_cVersionEx字段不为空，优先使用m_cVersionEx
	char		m_cUIVersion[LEN_32];	  //UI Server version number
	char		m_cSlaveVersion[LEN_32];  //From film version number
	char        m_cPlugInVersion[LEN_32]; //Plug-in version number 
	char		m_cSCMVersion[LEN_32];	  //Single chip version number
	char 		m_cSCGuiVersion[LEN_32];  //UI version number
	char		m_cMediaVersion[LEN_32];  //T2 Camera Media Version
	char		m_cMethodVersion[LEN_64];						//1 Method Version
	char		m_cCpldVersion[LEN_64];							//2 Cpld Version
	char		m_cDomeDriveVersion[LEN_64];					//3 DomeDrive Version
	char		m_cDigitalMovementVersion[LEN_64];				//4 Digital Movement Version
	char		m_cTradeP2PVersion[LEN_64];						//5 p2p Version
	char		m_cTradeOnvifVersion[LEN_64];					//6 Onvif Version
	char		m_cTradeItsVersion[LEN_64];						//7 Its Version
	char		m_cTrade28181Version[LEN_64];					//8 28181 Version
	char		m_cTradeZFZJVersion[LEN_64];					//9 
	char		m_cTradeH323Version[LEN_64];					//10
	char		m_cTradeRTSPVersion[LEN_64];					//11
	char		m_cTradeRTMPVersion[LEN_64];					//12
	char		m_cHpdVersion[LEN_64];							//13
	char		m_cFrontPanelVersion[LEN_64];					//14 前面板
	char		m_cAlgoModelVersion[LEN_64];					//15
	char		m_cIntelligentModuleVersion[LEN_64];			//16 Intelligent module Version
	char		m_cLandmarkVersion[LEN_64];						//17 Landmark Version
	char        m_cNetSdkVersion[LEN_32];						//
	char        m_cNetOcxVersion[LEN_32];						//
	char		m_cSecondDomeDriverVersion[LEN_64];				//18 2nd M3  Driver
	char		m_cEncrypChipSupportFirmwareVersion[LEN_64];	//19
	char        m_cIspVersion[LEN_64];							//20
    char        m_cFacePadM3Version[LEN_64];					//21 face pad m3
	char		m_cMobileModuleVersion[LEN_64];					//22从22以后开始（包含22）支持轻量级获取 
	char		m_cLowPowerChipVersion[LEN_64];					//23
	char		m_cAliPaymentAlogrithm[LEN_64];					//24
	char		m_c4KTrialFPGAVersionFromChip[LEN_64];			//25
	char		m_c4KTrialFPGAVersionFromVideo[LEN_64];			//26
	char		m_cMCUFirmwareVersion[LEN_64];					//27
	char		m_cElevatorMCUFirmwareVersion[LEN_64];			//28梯控单片机固件版本号
	char		m_cElevatorMCUHardwareVersion[LEN_64];			//29梯控单片机硬件版本号
	char		m_cVersionExpand30[LEN_64];						//30
	char		m_cVersionExpand31[LEN_64];						//31
	char		m_cVersionExpand32[LEN_64];						//32
	char        m_cVersionEx[LEN_64];							//m_cVersion 内核版本扩展字段，兼容内核版本超过32字节情况，此字段前32字节内容与m_cVersion一样
}SERVER_VERSION;

#define BURN_BACKUP_FILE_ALL         0
#define BURN_BACKUP_FILE_ONLYPEN     1

#define RET_BURN_SUCCESS       0 //设置成功
#define RET_BURN_FAILED        1 //设置失败
#define RET_BURN_RUNING        2 //当前正在刻录，不允许修改配置


#define EASYDDNS_LINK_OFFLINE	0
#define	EASYDDNS_LINK_ONLINE	1
typedef struct tagEasyDDNSLinkState
{
	int iSize;
	int iLinkState;	//0:offline, 1:online
} EasyDDNSLinkState, *pEasyDDNSLinkState;

typedef struct tagModulePowerLimit
{
	int				iSize;				// output para 结构体大小
	int				iLimitPower;		// output para 限制总功耗值, 实际功耗值扩大1000倍
	int				iModuleType;		// output para 模块类型，0：红外灯，1：白光灯，2：激光灯，3-9：预留
	int				iPowerRatio1;		// output para 模块功耗系数1, 模块为红外或白光灯时表示近光组每单位亮度变化的功耗值，模块为激光灯时表示激光灯每单位亮度变化的功耗值，实际扩大1000倍，不支持则为0，其他模块则根据需求定义
	int				iPowerRatio2;		// output para 模块功耗系数2, 模块为红外或白光灯时表示远光组每单位亮度变化的功耗值，实际扩大1000倍，不支持则为0，其他模块则根据需求定义
	int				iPowerRatio3;		// output para 模块功耗系数3, 模块为红外灯时表示中光组每单位亮度变化的功耗值，实际扩大1000倍，不支持则为0，其他模块则根据需求定义
	int				iPowerRatio4;		// output para 模块功耗系数4, 模块为红外灯或白光灯时表示超远光组每单位亮度变化的功耗值，实际扩大1000倍，不支持则为0，其他模块则根据需求定义
	int				iLightID;			// output para 灯组编号, 0表示灯组1；1表示灯组2
}ModulePowerLimit, *pModulePowerLimit;

typedef struct tagManualPortMap
{
    int  iSize;
    int  iHttpWanPort;			// http协议外网映射端口号
    int  iRtspWanPort;			//rtsp协议外网映射端口号
    int  iServerWanPort;		//私有协议外网映射端口号
    int  iHttpsWanPort;			// https协议外网映射端口号
    int  iRtmpWanPort;		    // rtmp协议外网映射端口号
} ManualPortMap, *pManualPortMap, ManualPortMapResult, *pManualPortMapResult;

#define OSDTYPE_TIME						0x01
#define OSDTYPE_TITLE						0x02
#define OSDTYPE_LOGO						0x04
#define OSDTYPE_ITS							0x05

typedef struct tagNorthAngle
{
	int iSize;
	int iAngle; //0~3600000 [0-360]，accurancy：0.0001
	int iStatus;//校正状态:0：预留，1：校正，2，未校正
}NorthAngle, *pNorthAngle;

typedef struct tagIEEE8021X
{
	int iSize;
	int iLanNo; //0：lan1  1:lan2….
	int iEnable;	//0:not enable   1:enable
	int iConnectType;	//0:automic  1:manual
	int iEapType;		//0，EAP-MD5
	int iEapolVersion;	//1，IEEEStd 802.1X-2001  2，IEEEStd 802.1X-2004
	char cUserName[LEN_64];	//user name
	char cPassWord[LEN_64];	//password

}IEEE8021X, *pIEEE8021X;

#define    MAX_ADDRESS_TYPE			2
typedef struct tagCommonRtmp
{
	int iSize;
	int iStreamType;	//0:main 1:sub  2:three
	int iAddressType; //1Custom 2Non-custom 
	int iEnable; //	0:not enable  1:enable
	int iPort; //0~65535
	char cIpAddress[LEN_256]; //Ip Address
	char cUserName[LEN_64];	//user name
	char cPassWord[LEN_64];	//password
	char cKey[LEN_64];	//key
}CommonRtmp, *pCommonRtmp;

typedef struct tagIEEE8021XState
{
	int iSize;
	int iLanNo;			 //0：lan1  1:lan2….
	int iConnectState;	//0:not connect 1:connected  2:connecting 3:connect failed
}IEEE8021XState, *pIEEE8021XState;


typedef struct tagWaterSpeedFieldInfo
{
	int					iSize;			
	int					iChanNo;		//channel no
	int					iEnable;		//0-close;1-open
	int					iResult;		//output para:0-success;1-failed
}WaterSpeedFieldInfo, *pWaterSpeedFieldInfo;

#define MAX_AUTOTESTMULT_PARAM_NUM    128
typedef struct tagAutotestMultQuery
{
	int                 iSize;
	int					iChannelNo;			
	int					iTestItem;		//Debugging items:1-Calibration of PTG pot cover;
					                    //2-Calibration of PTG temperature; 3-Calibration of PTG distance;
	int					iParamNum;		                                
	char			    pcParam[MAX_AUTOTESTMULT_PARAM_NUM][LEN_65]; 
}AutotestMultQuery, *pAutotestMultQuery;

typedef struct tagAutotestMultResult
{
	int                 iSize;
	int					iChannelNo;			
	int					iTestItem;		//Debugging items:1-Calibration of PTG pot cover;
										//2-Calibration of PTG temperature; 3-Calibration of PTG distance;
	int					iTestResult;
	char                cTestInfo[LEN_65];
	int					iParamNum;		                                
	char			    pcParam[MAX_AUTOTESTMULT_PARAM_NUM][LEN_65]; 
}AutotestMultResult, *pAutotestMultResult;

#define MAX_TEMP_MODEL_TYPE 3
typedef struct tagVCATemDetect
{
	int                 iSize;
	int					iSceneID;						     //scene ID(0~31)
	int					iRuleID;						     //rule ID(0~15)
	int                 iValid;					             //0:invalid; 1:valid(default:0)
	int					iDisplayTemScaleEnable;			     //0:not display 1:display (default:0)
	int					iHighTemColor;						 //1:red 2:green 3:yellow 4:blue 5:purple 6:cyan 7:black 8:white (default:1）
	int					iLowTemColor;						 //1:red 2:green 3:yellow 4:blue 5:purple 6:cyan 7:black 8:white (default:1）		
	int					iModelType;						     //1:Ambient temperature difference alarm 2:Environmental high temperature alarm 3:human high temperature alarm	
	int					iTemUnit;						     //0:centigrade 1:Fahrenheit; 		
	int  				iTemThreshold;	                     //The threshold value of temperature alarm is determined according to the imodeltype, and the value is the actual temperature * 100 
	int					iWaitTime;                           //Unit: Second	
	int                 iTempLoseEnable;                     //Temperature abnormal alarm enable, 0-disable, 1-enable.(default:0)
}VCATemDetect, *pVCATemDetect;

typedef struct tagVcaScanList
{
	int                iSize;
	int                iVcaType;
	int                iSupport;                             //0-not support 1-support scan list
}VcaScanList, *pVcaScanList;

typedef struct tagVcaFireWork
{
	int               iSize;
	int               iSceneId;//range:[0-31],default:[0-15]
	int               iRuleID; //range:[0-15],default:0
	int               iValid;  //0:unvalid,1:valid
	int               iDisplayRule;//0:not display,1:display
	int               iDisplayStat;//0:not display,1:display
	int               iDisplayTarget;//0:not display,1:display
	int               iColor;      //1：red 2：green 3：yellow 4：blue 5：purple 6：Cyan 7：black 8：white  （default:2）
	int               iAlarmColor; //1：red 2：green 3：yellow 4：blue 5：purple 6：Cyan 7：black 8：white  （default:1）
	int               iFireSensitiv;//fire sensitive,range:[0-100],default:50
	int               iSmogSensitiv;//smoke sensitive,range:[0-100],default:50
	int               iModelType;//fire work test model:0:default，1:fire point detection，2:Smoke detection，3:fire point detection or Smoke detection，4:fire point detection and Smoke detection
	int               iWaitTime;//find alarm waiting time,range[0-600]s,default:0
}VcaFireWork, *pVcaFireWork;


#define  MAX_MASKAREA_TYPE  4
#define  MAX_MASKAREA_NO    48
typedef struct _tagMaskAreaEnable
{
	int             iSize;						//size of struct
    int             iMaskType;                  //0-reserved, 1-pyrotechnic detection - fire point shielding area, 2-pyrotechnic detection - Smoke shielding area, 3-temperature detection shielding area
	int             iEnable;                    //0：unenable  1：enable  (default:0)
}MaskAreaEnable, *pMaskAreaEnable;

#define SDK_MAX_MASKAREA_NUM   12
typedef struct _tagMaskAreaPara
{
	int             iSize;
    int             iMaskType; //0-reserved, 1-pyrotechnic detection - fire point shielding area, 2-pyrotechnic detection - Smoke shielding area, 3-temperature detection shielding area
    int             iAreaNo;//AreaNo:1-48
	vca_TPolygonEx  tPoint;
}MaskAreaPara, *pMaskAreaPara;


typedef struct tagSingleScreenCalParam
{
	int                 iChanNo;
	TPoint              tPoint;               
}SingleScreenCalParam, *pSingleScreenCalParam;

#define MAX_CALIBRATION_SCREEN_COUNT    32
typedef struct tagCalibrationInfo
{
	int                     iSize;           
	int                     iCalPtCount;	   //Calibration points count, max value 16.
	int                     iCalPtNo;          //Current calibration point number, 1~16.
	int                     iCalScreenCount;   //Screens count of calibration channel, max value 32.
	SingleScreenCalParam    tParam[MAX_CALIBRATION_SCREEN_COUNT]; //Every screen parameter, including channel number & points information corresponding to the each screen.
}CalibrationInfo, *pCalibrationInfo;

typedef struct tagVisualRange
{
	int iSize;
	int iRange;				//visual range
	int iHview;				//hortional view angle, 0~36000
	int	iVview;				//vertical view angle, 0~36000
	int iChannel;
}VisualRange, *pVisualRange;

#define VIDEO_CONVOER_AREA			8	//视频遮挡区域数

typedef struct tagVideoCover
{
	int			iSize;
	int			iStreamNo;
	CommonRECT tRect[VIDEO_CONVOER_AREA];
}VideoCover, *pVideoCover;

typedef struct tagRadarStatusInfo
{
	int		iSize;			
	int		iType;						//radar type 0-reserve  1-川速雷达
	char	cRadarVersion[LEN_64];		//Version
	int		iRadarStatus;				//Status 1-online, 0-offline
}RadarStatusInfo, *pRadarStatusInfo;

typedef struct tagTemperatureScaleType
{
	int			iSize;
	int         iChanNo;
	int         iTempStandard;          //temperature scale type, 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin 
}TemperatureScaleType, *pTemperatureScaleType;

typedef struct tagSingleBlackbodyParam
{
	int         iBlackBodyId;           //black body ID
	int         iBlackBodyTemp;         //temperature value * 100
	int         iBlackBodyTempUnit;     //temperature unit: 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin
	int         iBlackBodyDistance;     //unit: cm
	CommonRECT  tRect;
}SingleBlackbodyParam, *pSingleBlackbodyParam;

#define MAX_BLACKBODY_COUNT         2
typedef struct tagBlackbodyCorrection
{
	int			iSize;
	int         iChanNo;
	int         iBlackBodyCorrectEnable;//black body correct enable: 0-disable, 1-enable   
	int         iBlackBodyCorrectType;  //0-reserved, 1-single correction, 2-continuous correction 
	int         iBlackBodyNum;          //black body number: 1 or 2
	SingleBlackbodyParam tParam[MAX_BLACKBODY_COUNT];
}BlackbodyCorrection, *pBlackbodyCorrection;

typedef struct tagBodyTempCorrect
{
	int			iSize;
	int         iChanNo;
	int         iBodyTempCorrectEnable;          //body temperature correction enable:  0-disable, 1-enable 
	int         iBodyTempCorrectSensitivity;     //body temperature correction sensitivity: 0-100
}BodyTempCorrect, *pBodyTempCorrect;

typedef struct tagIntelligentCorretct
{
	int			iSize;
	int         iChanNo;
	int         iIntelligentCorrectEnable;		 //intelligent correction enable: 0-disable, 1-enable
	int         iIntelligentCorrectSensitivity;  //intelligent correction correction sensitivity: 0-100
}IntelligentCorretct, *pIntelligentCorretct;

typedef struct tagVcaScanArea
{
	int			iSize;
	int			iSceneId;		//scene id 0-15
	int			iOrderTyp;		//0:top area, 1:bottom area, 2:left area, 3:right area
	int			iCmdType;		//commond type, 0:set, 1:call
}VcaScanArea, *pVcaScanArea;

typedef struct tagVcaScanPara
{
	int			iSize;
	int			iSceneId;		//scene id 0-15
	int			iMode;			//scan type,0:manual, 1:auto
	int			iWaitTime;		//wait time (s)
	int			iPanStep;		//Pan Step [0,36000],Corresponding floating point range [0,360], unit: degree,(software use divide by 100), accuracy: 0.01
	int			iTiltStep;		//Tilt Step [0--9000],Corresponding floating point range [0,90], unit: degree,(software use divide by 100), accuracy: 0.01
}VcaScanPara, *pVcaScanPara;

typedef struct tagRtmpClientLinkState
{
    int				iSize;							// 结构体大小
    int				iStateValue;					// 连接状态值
} RtmpClientLinkState, *pRtmpClientLinkState;

#define RADAR_EVENT_TYPE_PARK			1
#define RADAR_EVENT_TYPE_CONGESTION		2
#define RADAR_EVENT_TYPE_MAX			3
typedef struct tagRadarEventPara
{
	int					iSize;
	int					iChanNo;		//channel no
	int					iEventType;		//0- reservation 1- parking event 2- congestion event
	int					iTime;			//iEventType=1 range [1,43200], Unit s
										//iEventType=2 range [1,3600], Unit s
}RadarEventPara, *pRadarEventPara;

#define RADAR_ROAD_MAXNUM		24
#define RADAR_CROSS_SECTION		 8

typedef struct tagRadarRoadDir
{
	int				iId;		//[0,11]
	int				iDir;		//0–来向车道, 1–去向车道
}RadarRoadDir, *pRadarRoadDir;

typedef struct tagRadarCrossSection
{
	int				iId;		//断面Id,	范围 [0,3]
	int				iPos;		//断面位置, 范围 [0,250], 默认值0表示未配置断面，单位m
}RadarCrossSection, *pRadarCrossSection;

typedef struct tagRadarDevicePara
{
	int					iSize;
	int					iChanNo;							//channel no
	int					iRadarType;							//1-川速雷达
	int					iRoadNum;							//车道数量 [1,12]
	int					iRadarRoadWidth;					//车道宽度 [100,400]	单位 cm
	RadarRoadDir		tRoadDir[RADAR_ROAD_MAXNUM];		//车道方向
	int					iRadarMeasureMax;					//测距上限 范围 [100,28000], 默认值 25000； 单位cm
	int					iRadarMeasureMin;					//测距下限 范围 [100,25600], 默认值 3000；单位cm
	int					iCrossSectionNum;					//断面个数
	RadarCrossSection	tCrossSection[RADAR_CROSS_SECTION];	//断面信息
	int					iRadarHeight;						//雷达安装高度 范围 [100,1000]默认值 700 单位cm 
	int					iRadarAngleDelta;					//雷达角度修正 范围 [0,20000] 默认值 10000 单位(度*100) (实际对应范围[-100度,100度])
	int					iRadarCoordDelta;					//雷达坐标修正 范围 [0,2000], 默认值 1000 单位cm (实际对应范围[-1000,1000])
	int					iTrafficJamStartLine;				//拥堵判断起始位置 范围 [0,250], 默认值0，单位m
	int					iTrafficJamTerminationLine;			//拥堵判断终止位置 范围 [0,250], 默认值0，单位m
	int					iQueueLengthThreshold;				//排队长度阈值 范围 [0,8], 默认值0
	int					iTrafficJamCarNumber;				//拥堵车辆阈值 范围 [0,128], 默认值0
	int					iLsolationBeltDriveway;				//隔离带所在车道 范围 [0,12], 默认值0
	int					iLeftToTheLane;						//左侧来向车道数目 范围 [0,8], 默认值0 代表全部为来向
	int					iNonmotorValue;						//路口非机动车过滤阈值 范围 [0,20], 默认值 0；对应范围[-10,10] ，-10不过滤，值越大过滤效果越强
	int					iMotorValue;						//行车道非机动车过滤阈值 整数, 范围 [0,20], 默认值 0；对应范围[-10,10] ，-10不过滤，值越大过滤效果越强
	int					iShieldDistance;					//反射屏蔽距离 [0,250],单位m， 默认值80，0表示此功能无效
}RadarDevicePara, *pRadarDevicePara;	

#define MAX_CROP_NUM 2
typedef struct _tagCropCoding
{
	int                iSize;
	int                iEnable;                             //0：不使能；1：使能
	TPoint             tPoint[MAX_CROP_NUM];                //使用万分比表示，取值范围0～10000。矩形框实际分辨率最小为352*288,区域顶点坐标
}CropCoding, *pCropCoding;

#define MAX_VAR_GAUGE_POINT_NUM		8

typedef struct tagVirGaugedIs
{
	int					iSize;
	int					iEnable;		//0, off; 1, on 
	int					iResult;		//0, setting succeeded; 1, setting failed
}VirGaugedIs, *pVirGaugedIs;

#define  MAX_VIDEO_COVER_EX     4
typedef struct tagVideoCoverEx
{
	int					iSize;
	int					iStreamNo;
	int					iAreaCnt;		//effictive area count
	CommonRECT			tCommonRECT[MAX_VIDEO_COVER_EX];		//area point
}VideoCoverEx, *pVideoCoverEx;

typedef struct __tagInterestedArea
{
	int iSize;
	int iIndex;
	int iEnable;
	int iAbsQp;
	int iQp;
	int iLeft;
	int iTop;
	int iRight;
	int iBottom;
	int iDstFrameRate;	//destination frame rate，1-current framerate
}TInterestedArea, *pTInterestedArea;

typedef struct __tagVcaFlowSpeedParam
{
	int					iSize;
	int					iChanNo;
	VCARule			    tRule;
	int		            iDisplayRule;	
	int                 iColor;
	int                 iSensitive;
	int                 iLinkRecord;
	int              	iCruiseAddMode;
	int              	iCruiseNum;
	int                 iInterval;
	vca_TPolygonEx      tPonit;
	int                 iApplyScene;	//0: large scene 1: small scene
	int                 iDetectStep;	//0 ~ 100000 unit mm, valid when icruiseaddmode is automatic
	int                 iMiddleWidth;	//0-20000 unit mm
	int                 iDetectAltitude;//The elevation unit of the initial detection area is mm, and the actual value range is [- 10000000 ~ 10000000]. If it is sent on the network, add 10000000, and then subtract 10000000 after the software value
	vca_TLine			tLinePonit;
	int                 iHorizontalWidth;	//Horizontal  Width
	int                 iVerticalWidth;		//Vertical  Width
	int                 iHypotenuseWidth;	//Hypotenuse  Width
	int                 iAngelInputMode;	//0: enter slope angle 1: enter right angle slope
	int                 iNightFocusMode;	//0: off 1: on
	int                 iWaterSpeedDirEnable;	//0: Not Enable 1: Enable
	vca_TLine			tWaterSpeedDirPoint;
	int                 iWaterSpeedRatio;	//Water Speed Ratio,（0,1000]
	int                 iFilterSensitivity;	//int	过滤灵敏度	0～100 
	int              	iLinkAreaType;	//int	水位联动检测区域类型	0-不联动 1-流速方向左边联动 2-流速方向右边联动 3-双边联动
	int              	iMinSpeed;	//int	最小流速限制	-300000～300000，单位：毫米/秒(mm/s)
	int              	iMaxSpeed;	//int	最大流速限制	-300000～300000，单位：毫米/秒(mm/s)
	int             	iDisplayType;	//int	流速显示类型	0-瞬时流速 1-平均流速
	int                 iWaterLevelThreshold;//int 范围:-10000000～10000000，单位:毫米(mm)
	int                 iDetectMode;          //流速检测模式	0-保留 1-自动 2-低速 3-高速
	int                 iTrackFrameNum;       //检测跟踪帧数	范围：2-10  iDetectMode=1、4、5、6模式下有效
	int                 iFieldOfVision;       //流速检测视野大小	0-保留 1-大 2-小
	int                 iCorrlevel;           //流速曲线校正级别	0-保留 范围:1-100
	int					iStartDir;            //流速检测起点方向	0-保留 1-同向(面向起点方向检测) 2-反向(背向起点方向检测)
	int					iLightCtrl;			//int	检测时补光策略	0-关闭 1-开启
	int					iDeflection;		//夜晚检测角度	当iLightCtrl=1时有效，范围：0-90度					
	int					iTimeoutEnable;		//流速检测超时清0使能 0-保留 1-开启 2-关闭
	int					iTimeoutDuration;	//流速检测超时时长 当iTimeoutEnable=1时有效，单位：分钟，范围：1-65535
    int                 iAutoDetectAreaType; //流速自动检测区域类型	"当大场景自动模式有效(iApplyScene=0 iCruiseAddMode=0)  取值说明：0-保留 1-按起始结束区域 2-按有水范围"
    int                 iWaterAreaEnable;    //有水范围限制使能	当iAutoDetectAreaType=2时有效，0-保留 1-使能 2-未使能
    int                 iWaterAreaStart;     //有水范围开始距离	当iWaterAreaEnablee=1时有效，范围:0～100000000,单位:mm
    int                 iWaterAreaEnd;       //有水范围结束距离	当iWaterAreaEnablee=1时有效，范围:0～100000000,单位:mm
    int                 iSurfaceWaterSpeedDis; //表面流速显示	0-保留，1-显示，2-不显示
    int                 iAutoCalcDetectStep;    //自动计算检测步长	0-保留，1-开启， 2-关闭
}VcaFlowSpeedParam, *pVcaFlowSpeedParam;

#define MAX_PRESET_NUM_EX			32

// (water level detection) RULEEVENT15
#define MAX_REFERPOINT_NUM			10
#define	EVENT_BUF_RESERVED			2048			//reserve size
#define MAX_WATER_SAMPLE_POINT		16


typedef struct tagWaterRange // level range
{
	int iStart;			// start range
	int iEnd;			// End range
} WaterRange;

typedef struct tagReferPoint // The water level reference
{
	int iPointX;		// abscissa
	int iPointY;		// ordinate
	int iValue;			// The reading on the ruler
}ReferPoint;

typedef struct tagWaterPresetInfo
{
	int                 iSize;
	int iPresetId;					// Preset ID
	WaterRange stGaugeRange;		// Water level range iStart: iEnd
	CommonRECT rcGaugeRect;			// Diagonal coordinate of the scale area
	CommonRECT rcAssistRect;		// auxiliary area diagonal coordinates
	CommonRECT rcGaugeLine;			// Coordinate of the ruler reference line
	int iReferNum;					// number of reference points
	ReferPoint stReferPoint [MAX_REFERPOINT_NUM]; // reference points 1, 2, 3
}WaterPresetInfo;

#define  MAX_RULE_REGION_NUM	4
typedef struct vca_tLeaveDetect
{
	int iValid;						// Whether this event detection is valid, 0: invalid 1: valid
	int iLeaveAlarmTime;			// off-duty alarm time, 60 ~ 3600, in seconds, the default: 120S
	int iRuturnClearAlarmTime;		// back to the police alarm time, 10 to 300, in seconds Default: 15S
	int iAreaNum;					// Total number of rule fields, 1 ~ 4
	vca_TPolygonEx		stRegion[MAX_RULE_REGION_NUM];			//	 Range	
}vca_tLeaveDetect, *LPvca_tLeaveDetect;

typedef  struct WaterSamplePoint
{
	int iSize;
	VCARule	stRule;
	vca_TDisplayParam	stDisplayParam;		//Parameter dispaly
	int iSamplePointId;
	int iSnapInterval;
	int iBaseValue;			//base water level value
	int iCurrentPresetId;	//current preset id
	int	iGaugeType;			//Gauge type:0-Reserved, 1-Wide gauge,New gauge, 2-Narrow gauge,Old gauge, 3-Special water ruler, self-calibration 4-gauge-type-squad 5-gauge-type-pile
	int iPresetCount;
	WaterPresetInfo stPresetInfo[MAX_PRESET_NUM_EX];
	int	iGaugeLength;
	int iGaugeCount;        //this param is valid when the gauge type is pile gauge
	int iDirection;         //0：left 1：right
	int iVirGaugeMode;		//Virtual Gauge Detect Mode 0-algorithm detection mode 1-manual calibration mode
	int iVirGaugePointNum;  //2--8,Effective in manual calibration mode 
	vca_TPoint tVirGaugePoint[MAX_VAR_GAUGE_POINT_NUM];
	int iCurGaugeNo;        //0：invalid  >0 is Gauge Num
	int iSnapPresetEnable; //Snap interval enable
	int iSnapPresetNo;     //Snap interval No
	int iGaugeMinScale;    //the minize Scale(Unit:cm，range:0-20，0:auto)
	int iGaugeCalibIntervel; //5-1440（unit:min）
	int iDetDisturbFlag; //0-default，1--close 2--detect human default：1
}WaterSamplePoint, *LPWaterSamplePoint;

#define BANDWIDTH_ADAPTING         0    // 20M/40M 自适应
#define BANDWIDTH_FIX              1    //固定20M 频宽
#define MAX_WIFI_AP_CLIENT        16


//查询/回复AP client设备列表
typedef struct tagWIFIAPClientResult
{
    int     iSize;              // 结构体大小
    int	    iCount;             // 总条数
    int     iNo;                // 当前序号从开始
    char    cDevName[LEN_64]; // 设备名称
    char    cIp[LEN_64];      // ip地址
    char    cMac[LEN_64];     // mac地址
    int     iSpeed;            // 速度Mbps
    int     iBandWidth;        // 带宽BANDWIDTH_ADAPTING/BANDWIDTH_FIX
    int     iConnectTime;       // 连接时间 s

} WIFIAPClientResult, *pWIFIAPClientResult;


typedef struct tagWifiAPClientList
{
    int				    iSize; // 结构体大小
    WIFIAPClientResult	tResult[MAX_WIFI_AP_CLIENT];      
}WifiAPClientList, *pWifiAPClientList;

#define SPEED_ADAPTING         0    // 20M/40M 自适应
#define SPEED_FIX              1    //固定20M 频宽
#define MAX_WIFI_SEARCH_AP     32

typedef struct tagWIFISearchAPResult
{
    int     iSize;               // 结构体大小
    int     iCount;              // 总条数
    int     iNo;                 // 当前序号从开始
    char    cESSID[LEN_33];      //ESSID  LEN_33
    char    cEncryption[LEN_16]; //wifi encrypttion flag; NULL no encryption，"WEP" encryption
    int     iChannel;            // 0，Auto，～，信道～信道。
    int     iRSSL;               // Mbps
    int     iSpeed;              // 速度Mbps SPEED_ADAPTING/SPEED_FIX

} WIFISearchAPResult, *pWIFISearchAPResult;

typedef struct tagWIFISearchAPResultList
{
    int		            iSize;   // 结构体大小
    WIFISearchAPResult  tResult[MAX_WIFI_SEARCH_AP];
} WIFISearchAPResultList,*pWIFISearchAPResultList;

#define LOCATION_NEM						1	//路口编号/地点名称
#define LANE_NUMBER							2	//车道编号/监区名称
#define DEVICE_NUMBER						3	//设备编号/设备名称
#define DRIVE_DIRECTION_TYPE				4	//行驶方向类型；
#define DICTIONARY_VEHICLE_TYPE				5	//车辆类型；
#define SNAP_TYPE							6	//抓拍类型；
#define VEHICLE_BRAND						7	//车辆品牌；
#define VEHICLE_PLATE						8	//车牌；
#define VEHICLE_COLOR						9	//车身颜色
#define PIC_NAME_FORMATE					10	//图片名称格式；
#define PLATFORM_VERSION					11	//平台版本；
#define VEHICLE_SUB_BRAND					12	//车辆子品牌；
#define EXPORT_PIC_ROUTE_FORMATE			13	//导出图片路径格式；
#define PLATFORM_TYPE						14	//平台类型；
#define DEVICE_MODE							15	//设备型号；
#define LED_INSERT_LABEL_ITEM				16	//Led字符叠加快速插入标签项；
#define LED_DEV_MODE						17	//Led设备类型；
#define LED_HINT_MODE						18	//Led提示模式；
#define SCH_ALRAM_MODE						19	//布控报警类型 C
#define SEPARATOR_TYPE						20	//分隔符类型；
#define FTP_UPLOAD_TYPE						21	//FTP上传文件类型；
#define VEHICLE_FORM						22	//车辆外形；
#define OSD_COLOR							23	//OSD字体颜色；
#define LANE_TYPE							24	//车道类型；
#define PLATE_COLOR							25	//车牌颜色；
#define ITS_LOG_LEVEL						26	//交通日志等级；
#define CAM_NAME							27	//相机名称；
#define LOCATION_NAME						28	//路口名称；
#define OSD_OVERLAY_TYPE					29	//Osd叠加信息类型；
#define MD_CARD_VERSION						30	//Mq卡口版本；
#define MD_ILLEGAL_VERSION					31	//Mq违法版本；
#define COMPOSTION_VIDESIZE_TYPE 			32	//合成分辨率类型

#define MAX_DATA_PIC_ITEM  10

typedef struct tagDataDicionaryItem
{
	int		            iSize;   // 结构体大小
	int					iFuncType;		//功能类型，如上
	int		            iTotal;   // 查询总数
	int		            iIndexNo;   // 字段索引0~(N-1)
	int		            iFuncTypeValueNo;   // 当前编号
	char				cParam[LEN_128];   // 字符串描述，内容格式根据具体iFuncType以及iFuncTypeValueNo自己定义
}DataDicionaryItem,*pDataDicionaryItem;


#define			MAX_HINT_INFO_TYPE_NUM   2
typedef struct tagHintInfo
{
	int		            iSize;   // 结构体大小
	int					iHintType;//提示类型 0：有车 1：无车
	char				cDisplayInfo[LEN_128];
	int					iHintTime;		//提示时间,单位：秒
} HintInfo,*pHintInfo;


typedef struct tagLedDevOsdInfo
{
	int     iSize;               // 结构体大小
	int     iIdNum;              // 不显示，内核赋值，添加时默认0，编辑和删除时根据获取值透传即可（从1开始）（后端使用）
	int     iActionType;         // 0：添加，1：编辑，2：删除 
	int		iLedTypeId;			//LED屏的应用类型编号(对应DATADICTIONARYITEM查询类型17的序号iFuncTypeValueNo)，从1开始，如：1-道路预警屏，2-拥堵屏，3-超速屏，也可以1-超速屏，2-道路预警屏，具体数值依据DATADICTIONARYITEM查询上报而定
	char    cLedDevNo[LEN_64];  //提示屏编号
	char    cledDevName[LEN_128];  //提示屏名称
	int     iAlarmTypeId;            // 提示模式,提示模式(对应DATADICTIONARYITEM查询类型18的序号iFuncTypeValueNo)，从1开始，如：1-道路预警
	int     iFontWidth;               // 字体宽度  16,24,26,28，30，32,48,64
	int     iFontHeight;              // 字体高度  16,24,26,28，30，32,48,64
	int     iFontColor;				//字体颜色LED屏的字体颜色(对应DATADICTIONARYITEM查询类型23的序号iFuncTypeValueNo)，从1开始，如：1-红色
	char    cLedSize[LEN_64];		//屏幕尺寸   比如：800*600  （Led设备外键关联）
	HintInfo tHintInfo[MAX_HINT_INFO_TYPE_NUM];//默认tHintInfo[0]有车 tHintInfo[1]无车
	int		iActionResult;		//操作返回状态  0：成功 ，-1 ：失败
} LedDevOsdInfo, *pLedDevOsdInfo;

typedef struct tagLedDevOsdParamList
{
	int		            iSize;   // 结构体大小
	int					iTotal;
	int					iIndexNo;		//提示时间,单位：秒
	LedDevOsdInfo       tLedDevOsdInfo;
} LedDevOsdParamList,*pLedDevOsdParamList;

#define RADAR_PARAMID_MAXNUM 256
typedef struct tagRadarAdvancedPara
{
	int		iSize;			
	int		iChanNo;
	int		iParamID;			//radar paramID 0-255
	char	cValue[LEN_64];		//paramID Value
}RadarAdvancedPara, *pRadarAdvancedPara;

#define LED_DEVICE_MAX_NUM		10

#define LED_ACTION_TYPE_ADD		0
#define LED_ACTION_TYPE_EDIT	1
#define LED_ACTION_TYPE_DEL		2
#define LED_ACTION_TYPE_TEST	3
#define LED_ACTION_TYPE_QUERY	4
#define LED_ACTION_TYPE_MAX		5

typedef struct tagLedDevInfo
{
	int     iSize;               
	int		iChanNo;
	int     iIdNo;					//reserved
	char    cLedDevNo[LEN_64];		//提示屏编号
	char    cLedDevName[LEN_128];   //提示屏名称
	int		iLedDevType;			//提示屏的应用类型
	int     iLedDevWidth;           //屏幕宽度  单位：像素(1-10000)
	int     iLedDevHeight;          //屏幕高度  单位：像素(1-10000)
	int     iLedDevModel;			//提示屏型号
	char    cIpAddr[LEN_64];		//IP地址
	int		iPort;					//端口号
}LedDevInfo, *pLedDevInfo;

typedef struct tagLedDevReply
{
	int		            iSize;
	int					iChanNo;
	int					iActionType;		//opt type 0：添加，1：编辑，2：删除，3：测试
	char				cLedDevNo[LEN_64];			//提示屏编号
	int					iActionResult;		//操作返回状态 0：成功，1:失败
}LedDevReply,*pLedDevReply;

typedef struct tagLedDevParamResult
{
	int		            iSize;   
	int					iTotal;
	int					iIndexNo;
	LedDevInfo			tLedDevInfo;
}LedDevParamResult,*pLedDevParamResult;

typedef struct tagLedDevParamOpt
{
	int		            iSize;   
	int					iActionType;		//opt type 0：添加，1：编辑，2：删除，3：测试， 4,查询
	LedDevInfo			tLedDevInfo;
}LedDevParamOpt,*pLedDevParamOpt;

#define MAX_RADAR_CALIBRATE_POINT_NUM		16

typedef struct tagRadarPointInfo
{	
	vca_TPoint			tVideoPosition;	//万分比坐标
	vca_TPoint			tRealPosition;	//数值[0~100000]对应[-50000cm~50000cm] 数值50000对应0cm
}RadarPointInfo, *pRadarPointInfo;

typedef struct tagItsRadarCalibrate
{
	int		            iSize;   
	int					iChanNo;
	int					iPointNum;								  //point Number
	RadarPointInfo     	tPoints[MAX_RADAR_CALIBRATE_POINT_NUM];   //point info
}ItsRadarCalibrate, *pItsRadarCalibrate;

typedef struct tagBlackBodyDetect
{
	int		iSize;   
	int		iChanNo;
	int		iEnable;		//0-disenable 1-enable
	int		iThreshold;		//[0-100]
	int		iSharkTime;		//[0-100s] unit:s
}BlackBodyDetect, *pBlackBodyDetect;

typedef struct tagDefaultDevParam
{
	int		iSize;   
	int		iChanNo;
	int		iType;			//input para 1-热成像模组恢复默认  2-4G小基站FDD模块恢复出厂 3-4G小基站TDD模块恢复出厂 4-4G小基站FDD模块重启 5-4G小基站TDD模块重启
	int		iResult;		//output para 0-success，1-failed
}DefaultDevParam, *pDefaultDevParam;


typedef struct tagWaterSpeedDeteUpdate
{
	int		iSize;   
	int		iSceneId;		//0~31 
	int		iDetectStep;	//0~100000,mm		
}WaterSpeedDeteUpdate, *pWaterSpeedDeteUpdate;

typedef struct tagWaterSpeedDeteUpdateResult
{
	int		iSize;   
	int		iSceneId;		//0~31 
	int		iDetectStep;	//0~100000,mm		
	int		iResult;	//0-succ 1-failed		
}WaterSpeedDeteUpdateResult, *pWaterSpeedDeteUpdateResult;

	
typedef struct tagSmallCellInfo
{
	int     iSize;
	int     iTerminalType;     //Module type in 4G small base station,0-FDD 1-TDD	
	int     iIsDspHearbeatRecv;//if Receive Dsp heart beat,0:no,1:yes
	int     iRfEnable;         //RF status 0 indicates RF off, 1 indicates RF on
}SmallCellInfo, *pSmallCellInfo;

typedef struct tagSmallCellImsi_Notify
{
	int      iSize;
	int      iDeviceId;       //Device ID
	int      iMsgId;          //Msg ID,prevent overlap
	int      iTime;           //timestamp
	char     pcImsiNo[LEN_32];
	int      iOwnerProvince;  //Provice Code
	int      iOwnerCity;      //City Code
	int      iOwnerOperator;  //0-Unknown 1-Mobile 2-Unicom 3-Telecom
	int      iAccessType;     //1-EpsAtt 2-EPSImsiAtt 6-EpsEmerAtt 16-Tau 17-TaLau 18-TaLauImsi 19-PeriU 24-TaUBr 25-TaLaUBr 26-TaLaImsiBr 27-PeriUBr
	int      iBaseStationType;//1-FDD 2-TDD
	char     pcImeiNo[LEN_32];
	char     pcStmsiNo[LEN_32];
	int      iLibCheck;       //0-under list 1-black list 2-white list
	int      iIsRealTime;     //1-upload ontime 2-upload storage
	int      iIsOwnerWarn;    //1- Place of attribution warning 2- Not place of attribution warning
}SmallCellImsi_Notify, *pSmallCellImsi_Notify;

typedef struct tagVcaHDSchedule
{
    int     iSize;
    int     iSceneId;               // 高16位为0时，低16位表示智能分析，范围0~31;高16位为1时，低16位表示警戒，范围0~3
    int     iType;			        //模板类型 0=保留, 1=通用, 2=短帧, 3=长帧
    int     iEnable;		        //0=不使能, 1=使能
    int     iDayId;			        // 日模板ID '-1=无效, 范围[0,31]
    int     iNightId;			    // 夜模板ID '-1=无效, 范围[0,31]
	int     iColor2Gray;			//联动彩转黑	0-保留 1-黑白 2-普通
} VcaHDSchedule, *pVcaHDSchedule, VcaHDScheduleResult, *pVcaHDScheduleResult;

#define MAX_VCA_FOCUS_AREA 17
typedef struct tagVcaFocusArea
{
    int                 iSize;
    int                 iSceneId;                   // 高16位为0时，低16位表示智能分析，范围0~31,高16位为1时，低16位表示警戒，范围0~3
    int                 iAlgType;                   // 0-保留 1-人脸 2-结构化
    int                 iEnable;                    // 0-不使能 1-使能
    int	                iAreaNum;                   //区域个数 取值范围[0-16]
    vca_TPolygonEx		stAreas[MAX_VCA_FOCUS_AREA];	
} VcaFocusArea, *pVcaFocusArea, VcaFocusAreaResult, *pVcaFocusAreaResult;

#define MAX_PLATE_FILTER_TYPE_NUM		2
#define MAX_PLATE_FILTER_NUM			32
typedef struct tagItsPlateFilterParam
{
	int		iSize;
	int		iChanNo;
	int		iFilterType;									//Filter type: 0-white list, 1:-blacklist
	int		iPlateFilterNum;								//Filter num, max 32
	char	cPlateFilterInfo[MAX_PLATE_FILTER_NUM][LEN_32];	//Plate Filter Info
}ItsPlateFilterParam, *pItsPlateFilterParam;

typedef struct tagTemperaturePosition
{
	int		iSize;
	int		iTempPosition;									//0-reserved, 1-face, 2-eye socket
}TemperaturePosition, *pTemperaturePosition;

typedef struct tagCalibrateCheck
{
    int					iSize;			
    int					iSceneId;		    // 0-32
    int					iStartPointX;		//万分比坐标0-10000
    int					iStartPointY;		//万分比坐标0-10000
    int					iEndPointX;			//万分比坐标0-10000
    int					iEndPointY;			//万分比坐标0-10000

}CalibrateCheck,*pCalibrateCheck;

typedef struct tagCalibrateCheckResult
{
    int					iSize;			
    int					iSceneId;		    // 0-32
    int					iStartPointX;		//万分比坐标0-10000
    int					iStartPointY;		//万分比坐标0-10000
    int					iEndPointX;			//万分比坐标0-10000
    int					iEndPointY;			//万分比坐标0-10000
    int                 iResult;			// 0-SUCCESS，1-FAILED
} CalibrateCheckResult, *pCalibrateCheckResult;

typedef struct tagWirelessSilent
{
	int						iSize;
	int						iRestartTime;						//重启时间
	int                     iResult;			                // 0-SUCCESS，1-FAILED
}WirelessSilent, *pWirelessSilent;
#define WATER_FLOW_CONTROL_TYPE_DEFAULT		0
#define WATER_FLOW_CONTROL_TYPE_ADD	        1
#define WATER_FLOW_CONTROL_TYPE_EDIT		2
#define WATER_FLOW_CONTROL_TYPE_DEL		    3
#define WATER_FLOW_CONTROL_TYPE_MAX		    4
typedef struct tagWaterFlowRef
{
	int  iSize;
	int  iControl; //0保留、1添加、2编辑、3删除、
	int  iNum;     //编号	1~1000
	int  iWaterLevel;//水位值	-10000000~10000000，单位毫米(mm)
	int  iWaterFlow;//水流量值	0~100000000，单位立方分米/秒(dm3/s)
	int  iWaterSpeed;//水流速	0~30000，单位：毫米/秒(mm/s)
	int	 iPondage;//蓄水量 0~100000000，实际值为浮点型，取值时乘以100，单位：万立方米（10000立方米），例如12345，代表实际值为123.45万立方米
}WaterFlowRef, *pWaterFlowRef;

typedef struct tagWaterFlowRefResult
{
	int  iSize;
	int  iControl; //0保留、1添加、2编辑、3删除、
	int  iNum;     //编号	1~1000
	int  iWaterLevel;//水位值	-10000000~10000000，单位毫米(mm)
	int  iWaterFlow;//水流量值	0~100000000，单位立方分米/秒(dm3/s)
	int  iResult;   //0-成功 1-超出最大个数限制错误 2-设置参数超出范围错误
	int  iWaterSpeed;//'0~30000，单位：毫米/秒(mm/s)
	int	 iPondage;//蓄水量 0~100000000，实际值为浮点型，取值时乘以100，单位：万立方米（10000立方米），例如12345，代表实际值为123.45万立方米
} WaterFlowRefResult, *pWaterFlowRefResult;


typedef struct tagWaterTotalInfo
{
	int iNum;
	int iWaterLevel;
	int iWaterFlow;
	int iWaterSpeed;
	int	iPondage;//蓄水量 0~100000000，实际值为浮点型，取值时乘以100，单位：万立方米（10000立方米），例如12345，代表实际值为123.45万立方米
}WaterTotalInfo, *pWaterTotalInfo;

#define SDK_MAX_DATA_NUM  64
typedef struct tagWaterFlowRefInfo
{
	int  iSize;
	int  iTotalCount;
	int  iPacketIndex;
	int  iCurPacketNum;
	WaterTotalInfo tInfo[SDK_MAX_DATA_NUM];
}WaterFlowRefInfo, *pWaterFlowRefInfo;

typedef struct tagVCA_GaugeInfoPoint
{
	int  iSize;
	int  iPointx;
	int  iPointy;
	int  iGaugeValue;
}VCA_GaugeInfoPoint, *pVCA_GaugeInfoPoint;

#define   MAX_GAUGEINFO_NUM 10
typedef struct tagGaugeInfo
{
	int    iSize;
	int    iSceneId; //场景编号	0~31  （默认使用0~15）
	int    iRuleID;  //规则ID(预留)	0~15   （默认使用 0）
	int    iGaugeNo; //int	水尺编号	0：无效  大于0为水尺编号
	int    iGaugeAltitude;//水尺高程 -10000000~10000000单位mm
	int    iGaugeID;//0：无效  大于0为水尺序号
	int    iGaugeType; //0-保留 1-宽尺、新尺 2-窄尺、旧尺（注意与算法对应转换）3-特制水尺,支持自标定  4-方块水尺,支持自标定（只用于积水深度算法） 5-桩式水尺，支持自标定 6-无水尺 7-虚拟水尺 8-桩式水尺(纵向接力) 9-无水尺(纵向接力)
	int    iReferNum;  //0~10
	VCA_GaugeInfoPoint  tPoint[MAX_GAUGEINFO_NUM];//坐标参数为万分比
	int    iGaugeValue; //水尺读数	-10000000~10000000单位mm
	int    iGaugeLength;//水尺长度（单位mm）iGaugeType=5桩式水尺横向接力时有效，注意转换，算法单位m iGaugeType=8桩式水尺纵向接力时有效，注意转换，算法单位m 
	int    iBlcEnable;//背光补偿使能	0-保留，1-开启，2-关闭
	CommonRECT tRect;//水尺矩形区域对角线坐标,坐标为万分比
	int	   iGaugeBaseValue; //水尺基准水尺读数	当前水位对应此组水尺的真实读数，范围：-10000000~10000000, 单位：mm（注意转换，算法单位m）
	int    iGaugeCalibNum; //水尺标定预置位个数	支持水尺标定预置位的个数，范围：1～20
	int    iGaugeCalibType; //水尺标定类型	0-保留 1-自标定 2-手动标定 3-倾角或垂直角度标定
	int	   iUpSwitch;	//int	水尺上切读数值	范围：-10000000~10000000, 单位：mm，当iUpSwitchEnable=1时有效
	int    iDownSwitch;	//int	水尺下切读数值	范围：-10000000~10000000, 单位：mm，当iDownSwitchEnable=1时有效
	int    iUpSwitchEnable;		//水尺上切设置使能 0-保留，1-开启，2-关闭
	int    iDownSwitchEnable;	//水尺下切设置使能 0-保留，1-开启，2-关闭
    int    iGaugePosId;     //水尺安装序号	iGaugeType=10阶梯水尺(支持不同规格)时表示为水尺安装序号，0：无效  大于0为当前检测的水尺安装序号
    CommonRECT tRectAssist;   //辅助区域对角线坐标,坐标为万分比
    CommonRECT tRectGaugeLine;   //水尺参考线坐标,坐标为万分比
    int    iGaugeMinScale;      //水尺最小刻度  仅在水尺标定类型为4-无水尺标定时有效，单位:cm，范围:0-20，0表示自适应
    int    iGaugeCorr;      //水尺校正值	范围：-10000000~10000000，单位：mm，0表示不校正
}GaugeInfo, *pGaugeInfo;

#define MAX_GAUGE_NUM 64
typedef struct tagGaugeInfoEx
{
	int		   iSceneId; //场景编号	0~31  （默认使用0~15）
	int		   iRuleID;  //规则ID(预留)	0~15   （默认使用 0）
	int    	   iGaugeInfoSize;
	GaugeInfo  tGaugeInfo[MAX_GAUGE_NUM];
}GaugeInfoEx;

typedef struct tagVcaGaugeCalib
{
	int						iSceneID;
	int						iRuleID;
	int						iGaugeGroupID;  //水尺组号	1~20 (与VCA*GAUGEINFO协议的iGaugeId对应)
	int						iGaugeCalibID;  //水尺每组标定预置位的序号	1~20
	int						iGaugeValue;    //视频中心点水尺读数	-10000000~10000000单位mm
	int						iReferNum;      //标定点个数	0~10
	VCA_GaugeInfoPoint		tPoint[MAX_GAUGEINFO_NUM];//坐标参数为万分比
    int                     iVaild;          //此标定预置位的参数是否有效	0：有效 1：无效；此功能用于删除使用

}VcaGaugeCalib, *pVcaGaugeCalib;

typedef struct tagGaugeCalibQuery
{
	int				iSceneID;
	int				iRuleID;
	int				iGaugeGroupID; //水尺组号	1~20 (与VCA*GAUGEINFO协议的iGaugeId对应)
	int				iGaugeCalibID; //水尺每组标定预置位的序号	0-可以查询此组水尺所有标定参数 1~20-查询此组水尺当前序号的标定参数
}GaugeCalibQuery, *pGaugeCalibQuery;


typedef struct tagGaugeCalibQueryResult
{
	int						iSize;
	int						iSceneID;
	int						iRuleID;
	int						iGaugeGroupID;
	int						iGaugeCalibID;
	int						iResult;				  //询操作结果	0-无数据 >0为返回查询结果条数
	int						iGaugeValue;			  //视频中心点水尺读数	-10000000~10000000单位mm
	int						iReferNum;
	VCA_GaugeInfoPoint		tPoint[MAX_GAUGEINFO_NUM];//坐标参数为万分比
}GaugeCalibQueryResult,*pGaugeCalibQueryResult;

typedef struct tagVCASmartMove
{
	int					iSize;								
	VCARule				tRule;
	int					iTargetTypeCheck;		//0: do not distinguish 1: distinguish people 2: distinguish cars 3: distinguish people and vehicles
	int					iSensitivity;			//[0, 100]
	int					iRegionNum;
	vca_TPolygonEx		stPoints[MAX_DETECT_AREA_NUM];			//The number of vertices of the polygon area and coordinate
	int					iUserDefult;			//0 - use custom parameter 1 - use default parameter, default value is 1
	int					iAlramTime;				//The default value of [0, 100] seconds is 3, which is valid when the iusedefault value is 0
	int					iMinSize;				//[0, 100] the default value of 5 is valid when the iusedefault value is 0
	int					iMaxSize;				//[0, 100] the default value is 30, which is valid when the iusedefault value is 0
}VCASmartMove, *pVCASmartMove;


typedef struct tagSingleCutPicFeature
{
	int		iSize;
	int		iChanNo;
	int		iSceneId;		//Scene ID 1-16
	int		iCutPicEnable;	//Feature cutout enabled 0-disenable 1-enable						
	int		iCutRange;		//Cutout range 4-1/4   9-1/9  16:-1/16
	int		iSourcePic;		//Cutout choice 0: Excellent 1: First sheet 2: Second sheet
	int		iOsdEnable;		//Character overlay enablement 0-disenable 1-enable
}SingleCutPicFeature, *pSingleCutPicFeature;

typedef struct tagTrafficFillLight
{
	int		iSize;
	int		iChanNo;
	int		iOpenVal;		//0-100 Open  light threshold
	int		iCloseVal;		//0-100 Close light threshold
}TrafficFillLight, *pTrafficFillLight;

#define MAX_DEFAULT_TEMPLATE_LIST_NUM   18  
typedef struct tagDefaultTemplateList
{
    int    iSize;
    int    iType;           //算法类型
    int    iDaytimeDef;     //默认日间模板
    int    iEveningDef;     //默认夜间模板
}DefaultTemplateList, *pDefaultTemplateList;


#define ITS_EBIKE_CITY_MAX_NUM	2
#define PROVINCE_TJ				0xC0000
#define CITY_GUANGXI_YULIN		0x2D0009

typedef struct tagItsEbikePlate
{
	int		iSize;
	int		iChanNo;
	int		iCodeNo;		//City code number Currently only supported 2 city
	int		iEnable;		//0- no enable, 1- enable
}ItsEbikePlate, *pItsEbikePlate;

#define MAX_COFE_NUM 20
typedef struct tagCofeInfo
{
	int     iSize;
	int     iWaterLevel;
	int     iCoef;
}CofeInfo, *pCofeInfo;

typedef struct tagVerTiCallIne
{
	int       iSize;
	int       iSceneId;
	int       iControl;
	int       iVLineId;
	int       iStartDistance;
	int       iBottomAltitude;
	int       iCoefNum;
	CofeInfo  tInfo[MAX_COFE_NUM];
	int       iResult;
}VerTiCallIne, *pVerTiCallIne;

typedef struct tagIrrifittingDisPos
{
	int       iSize;
	int       iSceneId;	//Scene number, 0-15
}IrrifittingDisPos, *pIrrifittingDisPos;

#define FITTING_COEFFICIENT_NUM	16	//The number of coefficients in the fitting formula
typedef struct tagIrrifittingDisPosResult
{
	int       iSize;
	int       iSceneId;	//Scene number, 0~31
	int		  iResult;	//0：成功 1：采集数据超出范围 2：公式生成的基础参数错误 3：采集数据缺少数据类型 4：公式输出的多项式数量错误
}IrrifittingDisPosResult, *pIrrifittingDisPosResult;

typedef struct tagIrrifittingDisPosQuery
{
	int       iSize;
	int       iSceneId;	//Scene number, 0~31
	int		  iTime;	//The time to generate the fitting formula, UTC time, in seconds
	int		  iCoefNum;	//The number of coefficients in the fitting formula,Range: 0-30
	char	  cCoefArr[FITTING_COEFFICIENT_NUM][LEN_64];	//Convert software values to double type for use
	int		  iXorY;	//坐标类型 0-X坐标 1-Y坐标
	int		  iZoom;	//变倍倍率 范围：0~100000 对应0-1000倍，精度：0.01
	int       iColorToGray;	//彩转黑状态 0-白天（彩色） 1-夜晚（黑白）
}IrrifittingDisPosQuery, *pIrrifittingDisPosQuery;

#define MAX_COEF_TOTALNUM 8 //每次收8条同样的协议
typedef struct tagIrrifittingDisPosQueryArr
{
	int iSize;	//结构体大小
	int iCount; //收到的数据条数
	IrrifittingDisPosQuery tIrrifittingCoef[MAX_COEF_TOTALNUM];
}IrrifittingDisPosQueryArr, *pIrrifittingDisPosQueryArr;

typedef struct tagIrriDisPosCollection
{
	int       iSize;
	int       iSceneId; //场景ID 0~31
	int       iControl; //操作类型 0保留、1添加、2编辑、3删除、
	int       iDataId;	//数据序号 范围：1～500
	int       iDistance;	//激光测距 范围:0～10000000,单位:mm 
	char	  cPointXY[LEN_32];	//Laser point coordinates (parameters are in tens of thousands: upper left corner of the screen (0:0), lower right corner (10000:10000), format is: pcPointXY="iPointX: iPointY" Example: pcPointXY="0:300" iPointX iPointY is an int type, with a size of less than 10000. When using, it is converted according to the width and height of the current main stream video)
	int       iZoom;	//变倍倍率 范围：0~100000 对应0-1000倍，精度：0.01
	int       iColorToGray;	//彩转黑状态 0-白天（彩色） 1-夜晚（黑白）
	int       iResult;	//操作结果 0-成功 1-超出最大个数限制错误 2-设置参数超出范围错误 3-当iControl=1添加时为数据序号重复 4-当iControl=1添加时为获取距离失败 5-当iControl=2编辑或3删除时为数据序号未找到
}IrriDisPosCollection, *pIrriDisPosCollection;

typedef struct tagIrriDisPosCollectionGet
{
	int        iSize;
	int        iSceneId;	//Scenario ID (reserved from 0 to 31)
	int        iPageNo;	    //Page number (iPageNo>=0)
	int        iPageSize;	//Number of entries per page (iPageSize>=0, maximum 20)
}IrriDisPosCollectionGet, *pIrriDisPosCollectionGet;

typedef struct tagIrriDisPosCollectionGetResult
{
	int        iSize;
	int        iSceneId;	//Scenario ID (reserved from 0 to 31)
	int        iTotal;	//Total number of entries (iTotal>=0)
	int        iPageNo;	//Page number (iPageNo>=0)
	int        iPageSize;	//Current number of pages (iPageSize>=0)
	int        iIndex;	//Number within page number (iIndex>=0)
	int        iDataId;	//Data serial number (range: 1-500)
	int        iDistance;	//Laser ranging (range: 0~10000000, unit: mm)
	char	   cPointXY[LEN_32];	//Laser point coordinates (parameters are in tens of thousands: upper left corner of the screen (0:0), lower right corner (10000:10000), format is: pcPointXY="iPointX: iPointY" Example: pcPointXY="0:300" iPointX iPointY is an int type, with a size of less than 10000. When using, it is converted according to the width and height of the current main stream video)
	int        iZoom;	//Variable magnification (range: 0-100000 corresponds to 0-1000 times, accuracy: 0.01)
	int        iColorToGray;	//Color to black state (0-day (color) 1-night (black and white))
}IrriDisPosCollectionGetResult, *pIrriDisPosCollectionGetResult;

typedef struct tagIrriDisPosCollectionGetResultArr
{
	IrriDisPosCollectionGetResult tDisPosResultArr[MAX_QUERY_PAGE_COUNT];
}IrriDisPosCollectionGetResultArr, *pIrriDisPosCollectionGetResultArr;

typedef struct tagVerTiCallIneGet
{
	int        iSize;
	int        iSceneId;	//Scene ID 0~31 Reserved
	int        iPageNo;	    //page number	iPageNo>=0
	int        iPageSize;	//int	Number of entries per page iPageSize>=0 maximum 20
	int        iVLineIdStart; //int	Query the starting sequence number of the vertical line	1~101
}VerTiCallIneGet, *pVerTiCallIneGet;

typedef struct tagVerTiCallIneResult
{
	int        iSize;
	int        iSceneId;	//int	场景ID	0～31 预留
	int        iTotal;	//int	总条数	iTotal>=0
	int        iPageNo;	//int	页码	iPageNo>=0
	int        iPageSize;	//int	当前页条数	iPageSize>=0
	int        iIndex;	//int	页码内序号	iIndex>=0
	int        iVLineId;	//int	垂线序号	1~101
	int        iStartDistance;	//int	垂线起点距	范围:0～10000000,单位:mm
	int        iBottomAltitude;	//int	垂线河底高程	范围:-10000000～10000000,单位:mm
	int        iMeasuredTime;	//int	垂线实测时间	时间(utc)
	int        iCoefNum;	//int	垂线区域流速系数个数	1～20
	CofeInfo   tInfo[MAX_COFE_NUM];
}VerTiCallIneResult, *pVerTiCallIneResult;

typedef struct tagVcaWaterFlow
{
	int   iSize;
	int   iSceneId;	//int	场景编号	0～31  （默认使用0～15）
	int   iFlowCoef;	//int	流量系数	范围:0～10000，软件取值后除以1000.0转换成float
	int   iFlowSource;  //流量来源	0-水利前端设备计算(默认) 1-查询人工表数据 2-稳定河道(公式) 3-水堰水量(公式)
	int   iWaterSlope;  //水面比降系数	"iFlowSource=2稳定河道时有效,系数乘以10000
	int   iRoughness;   //河床糙率系数	"iFlowSource=2稳定河道时有效,系数乘以10000
	int   iWeirFlow;    //堰流系数系数	"iFlowSource=3水堰水量时有效,系数乘以10000
}VcaWaterFlow, *pVcaWaterFlow;



typedef struct tagAliSecret
{
	int   iSize;
	char  pcPK[LEN_64]; //设备pk
	char  pcPS[LEN_64];	//设别ps
	char  pcDN[LEN_64];	//设备DN
	char  pcDS[LEN_64];	//设备DS
	int   iChannelNo;
	char  pcSN[LEN_32];	//SN串
}AliSecret,*pAliSecret;

typedef struct tagAliSecretResult
{
	int   iSize;
	int   iResult;  //设置结果
	int   iChannel;
}AliSecretResult,*pAliSecretResult;

//xml协议设置、获取参数使用以下结构体
typedef struct tagXmlResponseStatus
{
	int iStatusCode; //状态码：0=1-OK, 2-Device Busy, 3-Device Error, 4-Invalid Operation, 5-Invalid XML Format, 6-Invalid XML Content, 7-Reboot Required
	char cStatusString[LEN_64];
	char cSubStatusCode[LEN_64];
} XmlResponseStatus,* pXmlResponseStatus;

typedef struct tagXmlRegistrationCenter
{
	int		iStructSize;
	char	cServerName[LEN_32];
	char	cRegIp1[LEN_128];
	int		iRegPort1;
	char	cRegIp2[LEN_128];
	int		iRegPort2;
	char	cAccount[LEN_32];
	char	cPassword[LEN_32];
} XmlRegistrationCenter, *pXmlRegistrationCenter;

#define TIMING_MODE_RESERVED		0
#define TIMING_MODE_NTP				1		
#define TIMING_MODE_GPS				2
#define TIMING_MODE_MANUAL			3
#define TIMING_MODE_ONVIF			4
typedef struct tagXmlDeviceSystemTime
{
	int						iTimingMode;			//0-保留 1-NTP校时 2-GPS校时 3- 手动 4-ONVIF协议校时
	NVS_FILE_TIME			tLocalTime;
	int						iTimeZoneDiffHour;		//与国际标准时间的时差（小时），-12 ... +13 
	int						iTimeZoneDiffMinute;	//与国际标准时间的时差（分钟），0, 30, 45
	int						iTimeZoneDiffSecond;	//与国际标准时间的时差（秒数），0, 30, 45 
	//Daylight Saving Time parameter
	int						iDstEnable;				//DaylightSavingTime enable, 0-disable, 1-enable
	int						iDstOffsetTime;			//DaylightSavingTime Offset time,0～120（unit：minute）
	int						iDstStartMonth;			//Start daylight saving time: month
	int						iDstStartWeek;			//Start daylight saving time: week of month, (0:last 1:first 2:second 3:third 4:forth)
	int						iDstStartDay;			//Start daylight saving time: day Of week, sunday to saturday is 0-6
	int						iDstStartHour;			//Start daylight saving time: hour of day [0～23]
	int						iDstStopMonth;			//Stop daylight saving time: month
	int						iDstStopWeek;			//Stop daylight saving time: week of month, (0:last 1:first 2:second 3:third 4:forth)
	int						iDstStopDay;			//Stop daylight saving time: day Of week, sunday to saturday is 0-6
	int						iDstStopHour;			//Stop daylight saving time: hour of day [0～23]）

} XmlDeviceSystemTime, *pXmlDeviceSystemTime;

//Ntp Server parameter
typedef struct tagXmlNtpServer
{
	char					cNtpServerIp[MAX_IPADDRESS_LEN];	//NTP Server IP, support ipv6
	int						iNtpServerPort;						//NTP Server Port
	int						iNtpInterval;						//ntp校时间隔时间，以分钟为单位
} XmlNtpServer, *pXmlNtpServer;


#define MAX_WORDOVERLAY_POSNO  7
typedef struct tagWordOverLayPos
{
	int     iSize;
	int     iLayNo;
	int     iAlignFormat;
	int     iDistance;
}WordOverLayPos, *pWordOverLayPos;

typedef struct tagIrriServerInfo
{
	int  iSize;
	char pcIrrigationIP[LEN_32]; //水利平台服务器IP
	int  iIrrigationPort;       //水利平台服务器端口
	char pcStationAddress[LEN_32];//水利平台测站地址
}IrriServerInfo, *pIrriServerInfo;

typedef struct tagIrrigationServerInfo
{
	int  iSize;
	IrriServerInfo tServerInfo[LEN_4];
}IrrigationServerInfo, *pIrrigationServerInfo;

typedef struct tagT4GParam
{
	int      iSize; 
	int      i3GDeviceType;   //设备类型	0,DTM-移动3G; 1,EVDO-电信3G; 2,WCDMA-联通3G，3，未识别；4-TDD移动4G；5-电信4G；6-联通4G；7-移动网络
	int      iStatus;         //在线状态	0,未上线；1，已上线；2，拨号中	
	int      iIntensity;      //信号强度	数字，对应含义待测
	char     pcIP[LEN_16];    //拨号ip	ppp0的IP地址
	char     pcStarttime[LEN_64];//初始拨号时间	开始拨号的时间戳，格式为  "YYYY/MM/DD HH:MM:SS"
	char     pcIMEI[LEN_32];  //GSM模块的IMEI（国际移动设备标识）序列号
	char     pcICCID[LEN_32]; //SIM卡的ICCID标识
}T4GParam, *pT4GParam;	

typedef struct tagPuInfo
{
	int                         iSize;
	char    					cDeviceID[LEN_64];		
	char    					cDeviceName[LEN_64];
	unsigned short    			wVspPort;
	char    					cPassword[LEN_64];
	unsigned short    			wVapPort;
}PuInfo, *pPuInfo;

#define MAX_KFK_SERVER_COUNT  32
#define MAX_KFK_CAMERA_COUNT  8
typedef struct tagCpcAreaDisplay
{
	int                         iSize;
	int    						iDevType;	//设备类型0:IPC,1:NVR
	int    						iDisplay;	//显示方式:1: 显示客流量统计界面 2: 退出客流量统计界面
	int    						iAreaNo ;	//当前显示区域0-19
}CpcAreaDisplay, *pCpcAreaDisplay;

typedef struct tagITSKfkCameraCount
{
	int     iSize;
	char    pcCameraNo[LEN_16];
	char    pcItsHostNo[LEN_16];
	char    pcSectionNo[LEN_16];
}ITS_KfkCameraCount, *pITSKfkCameraCount;

typedef struct tagItsKfkServerInfo
{
	int     iSize;
	char    pcServerIP[LEN_16];
	int     iServerPort;
}Its_KfkServerInfo, *pItsKfkServerInfo;

typedef struct tagITSKafkaCfg
{
	int					iSize;
	int                 iDockingType;
	int					iKfaUploadType;	  
	char                pcKkTopic[LEN_64];
	char                pciLLegalTopic[LEN_64];
	char                pcPointNum[LEN_64];
	int                 iCameraCount;
	ITS_KfkCameraCount  tCameraNum[MAX_KFK_CAMERA_COUNT];
	int                 iKfkServerCount;
	Its_KfkServerInfo   tServerNum[MAX_KFK_SERVER_COUNT];
	char				cKafkaUser[LEN_64];
}ITS_KafkaCfg, *pITSKafkaCfg;
#define  MAX_CPC_AREA_NUM 20
typedef struct tagCpcAreaConfig
{
	int                         iSize;
	int    						iAreaNo ;								 //当前显示区域0-19
	int                         iMask[LEN_32];							 //通道掩码每个bit表示与对应通道关联，0:不关联 1:关联
	int                         iMaskCount;                              //通道掩码有效数量
	int							iEnable;								 //0: 禁能 1: 使能
	char						cName[LEN_64];							 //区域名称
	int                         iCurPeople;							     //当前人数
	int    						iMaxPeople;								 //最大允许人数
	int                         iClearMode;								 //清零方式1: 每天 2: 从不
	int							iHour;									 //清零小时 0-23
	int							iMinute;								 //清零分钟 0-59

}CpcAreaConfig, *pCpcAreaConfig;

typedef struct tagCpcAreaStatus
{
	int        iSize;
	int        iAreaNo;	//区域编号	0～19 
}CpcAreaStatus, *pCpcAreaStatus;

typedef struct tagCpcAreaStatusResult
{
	int							iSize;
	int							iAreaNo;								 //区域编号	0～19 
	char						cName[LEN_64];							 //区域名称
	int                         iCurPeople;							     //当前人数
	int    						iMaxPeople;								 //最大允许人数
	int                         iStartTime;								 //本次统计开始时间,1970年1月1日0点到本次开始统计的秒数

}CpcAreaStatusResult, *pCpcAreaStatusResult;

typedef struct tagCpcAreaManualClear
{
	int			iSize;
	int			iAreaNo;	//区域编号	0～19 
}CpcAreaManualClear, *pCpcAreaManualClear;

typedef struct tagCpcAreaManualClearResult
{
	int			iSize; 
	int			iResult;	//0: 成功 1: 失败
}CpcAreaManualClearResult, *pCpcAreaManualClearResult;

#define MAX_BAN_TIME_COUNT		4
typedef struct tagVcaInquiryTimeout
{
	int					iSize; 
	int					iDevType;											//0-IPC, 1-NVR
	int					iRuleID;											//rule Id, 0～7
	int					iSceneId;											//scene Id,0~15
	int					iValid;
	int					iDisplayRule;
	int					iDisplayStat;
	int					iDisplayTarget;
	int					iMiniSize;
	int					iMaxSize;
	int					iSensitivity;
	int					iInquiryTime;
	int					iLeaveTime;
	int					iBantimeNum;
	DayScheduleTime		tBanTime[MAX_BAN_TIME_COUNT];
	int					iDetectRegionNum;
	vca_TPolygonEx		tDetectRegions[MAX_AREA_COUNT];	
	int					iInvalidRegionNum;
	vca_TPolygonEx		tInvalidRegions[MAX_AREA_COUNT];
}VcaInquiryTimeout, *pVcaInquiryTimeout;

typedef struct tagCommonAlarmNotify
{
	int			iSize;
	int			iChanNo;
	int			iState;				//报警状态 1-报警，0-消警；当报警类型是6或9智能分析报警时代表警情索引，对应报警状态通过NetClient_VCAGetAlarmInfo接口获取

	int			iAddlPara1;			//iAlarmType=1, 端口报警子类型 0：普通端口报警 1：按钮报警
									//iType=20，0-比对成功；1-陌生人
									//iType=21, 表示NVR智能分析列表
									//iType=24, 表示场景号
									//iType=25, 表示传感器类型 0：激光雨量计 1：雷达水位计 2：电瓶传感器 3：流速仪 4：LED屏 5:北斗模块 
									//iType=10001, 表示报警子类型 0:冲顶故障 1:蹲底故障 2:电梯异常急停 3:电梯超速 4:门区外停梯 5:到站后不开门
												//6:电梯晃动 7:开门走8:困人 9:主动报警 10:传感器故障 11:电动车入梯 12:跌落 13:安全回路报警 14：视频遮挡报警 15：未关门报警
									//iType=38，1-车牌比对成功；2-陌生车牌
									//iAlarmType是视频丢失报警(sdk对应报警类型2，网络协议对应报警类型0)时，代表报警子类型：1-连接异常 2-码流异常 3-解码异常 4-音频丢失 5-输入信号异常
									//iAlarmType=8(ALARM_EXCEPTION)，代表异常报警子类型EXCEPTION_TYPE_DISK_FULL ~ EXCEPTION_TYPE_EVAP_SUDDEN_CHANGE

	int			iAddlPara2;			//iType=20/21时，代表人脸识别底图的数据库id
									//iType=38&iParam1=1时，代表车牌库ID
									//iAlarmType=8(ALARM_EXCEPTION)，代表设备类型：0-预留，1-IPC，2-NVR

	int			iAddlPara3;			//iType=21时，代表nvr智能分析列表
									//iAlarmType=8(ALARM_EXCEPTION)，代表通道号：0~n-1，0x7fffffff表示设备本身

	int			iAddlPara4;			//iType=20时，代表人脸轨迹id

	char		cAlarmTime[LEN_32];	//报警时间，格式：年-月-日-星期-时-分-秒-毫秒，例如：2011090120101056 代表 2020年11月9日星期一20点10分10秒560毫秒

	char		cAddlPara5[LEN_32];	//iType=20时，代表人脸图片抓拍时间
									//iType=27（非法IP登录）时，标识尝试登录的对端IP
									//iType=38时，代表车牌图片抓拍时间
									//iType=21时，代表频次统计组号,频次次数。中间用逗号隔开。如：1,10
}CommonAlarmNotify,*pCommonAlarmNotify;

typedef struct tagTdServerSysParam
{
	int					iSize;
	char				cSystemUuid[LEN_64];
	char				cBaseboardSerialNumber[LEN_64];
}TdServerSysParam,*pTdServerSysParam;

typedef struct tagWhiteLightContrl
{
	int					iBufSize;			
	int					iContrlType;						
	unsigned short		u16BegainHour;
	unsigned short		u16BegainMinute;
	unsigned short		u16EndHour;
	unsigned short		u16EndMinute;
	int					iLightValue;
	int					iLightValue2;
	int					iLightValue3;
	int					iLightID; //0:Light group 1；1:Light group2
}WhiteLightContrl,*pWhiteLightContrl;

typedef struct tagTrafficViolationPara
{
	int				iSize;
	int				iChanNo;
	int				iCallSensitivity;
	int				iNoSeatBeltSensitivity;		
}TrafficViolationPara, *pTrafficViolationPara;

typedef struct tagWstAbleUseMode
{
	int        iSize;         //结构体大小
	int        iSceneId;      //int	场景ID	0～31 预留
	int        iUseMode;      //使用时的工作模式	0-自动 1-手动开启 2-手动关闭 3-定时
	int        iStartHour;    //定时使用开始时间	单位:小时 范围:0-23 iMode=3定时模式有效
	int        iStartMin;     //定时使用开始分钟	单位:分钟 范围:0-59 iMode=3定时模式有效
	int        iStopHour;     //定时使用结束时间	单位:小时 范围:0-23 iMode=3定时模式有效
	int        iStopMin;      //定时使用结束分钟	单位:分钟 范围:0-59 iMode=3定时模式有效
	int        iDetectTimeOut;//检测超时时间	单位:秒 范围:1-7200 iMode=0自动模式有效
	int        iTableOption;  //流速表来源选择	0-使用自动流速表 1-使用手动流速表
	int        iLowThreshold; //低水位查表阈值	当iUseMode=4时有效，范围:-10000000～10000000，单位:毫米(mm)
    int        iWindThreshold; //有风查表阈值	"当iUseMode=5时有效，范围:0-1000，单位:米/秒(m/s) 软件取值后除以10转换成float"
}WstAbleUseMode, *pWstAbleUseMode;

typedef struct tagWsTableAutoGen
{
	int        iSize;
	int        iSceneId;       //场景ID	0～31 预留
	int        iAutoUpdate;    //自动更新流速表	0-停止更新  1-开启更新
	int        iWaterLevelMax; //水位最大值	范围:-10000000～10000000，单位:毫米(mm)
	int        iWaterLevelMin; //水位最小值	范围:-10000000～10000000，单位:毫米(mm)
	int        iStartHour;    //生成开始时间	单位:小时 范围:0-23
	int        iStartMin;     //生成开始分钟	单位:分钟 范围:0-59 
	int        iStopHour;     //生成结束时间	单位:小时 范围:0-23 
	int        iStopMin;      //生成结束分钟	单位:分钟 范围:0-59
}WsTableAutoGen, *pWsTableAutoGen;

typedef struct tagWsTableQuery
{
	int   iSize;
	int   iSceneId;		//场景ID	0～31 预留
	int   iPageNo;		//页码	iPageNo>=0
	int   iPageSize;	//每页条数	iPageSize>=0 最大20条
	int   iVlineArea;	//垂线区域号	0:代表所有垂线区域  1~100:垂线区域索引 
	int   iWaterLevelStart;	//水位区间起始值	范围:-10000000~10000000，单位:毫米(mm)
	int   iWaterLevelStop;	//水位区间结束值	范围:-10000000~10000000，单位:毫米(mm)
}WsTableQuery, *pWsTableQuery;

typedef struct tagWstableContent
{
	int      iSize;
	int      iPageIndex;	//当前页内数据的序号	iIndex>=0
	int	     iNum;	        //当前页内数据的唯一索引号	1-50000
	int	     iVlineArea;	//当前页内数据的垂线区域号	1~100:垂线区域索引 
	int	     iWaterLevel;	//当前页内数据的水位值	-10000000~10000000，单位毫米(mm)
	int	     iWaterSpeed;	//当前页内数据的水流速值	0~30000，单位：毫米/秒(mm/s)
	int	     iWaterFlow;	//当前页内数据的水流量值	0~100000000，单位立方分米/秒(dm?/s)
}WstableContent, *pWstableContent;

typedef struct tagWsTableQueryResult
{
	int      iSize;
	int      iSceneId;		//场景ID	0～31 预留
	int	     iTotal;		//总条数	iTotal>=0
	int	     iPageNo;		//页码	iPageNo>=0
	int	     iPageSize;		//当前页条数	iPageSize>=0
	int	     iPagePktNum;	//当前页本次包的数据个数	iPagePktNum >= 0
	WstableContent  tWsTableContent[LEN_20];//数据内容 最多20条；
}WsTableQueryResult, *pWsTableQueryResult;


typedef struct tagWsTableOpt
{
	int      iSize;
	int      iSceneId;		//场景ID	0～31 预留
	int  	 iControl;		//操作类型	0保留、1添加、2编辑、3删除
	int  	 iNum;		//唯一索引号	1-50000
	int 	 iVlineArea;		//垂线区域	1~100:垂线区域索引 
	int  	 iWaterLevel;		//水位值	-10000000~10000000，单位毫米(mm)
	int      iWaterSpeed;		//水流速	0~30000，单位：毫米/秒(mm/s)
	int  	 iWaterFlow;		//水流量值	0~100000000，单位立方分米/秒(dm?/s)*/
}WsTableOpt, *pWsTableOpt;


typedef struct tagWsTableOptResult
{
	int      iSize;
	int      iSceneId;		//场景ID	0～31 预留
	int  	 iControl;		//操作类型	0保留、1添加、2编辑、3删除
	int  	 iNum;		//唯一索引号	1-50000
	int 	 iVlineArea;		//垂线区域	1~100:垂线区域索引 
	int  	 iWaterLevel;		//水位值	-10000000~10000000，单位毫米(mm)
	int      iWaterSpeed;		//水流速	0~30000，单位：毫米/秒(mm/s)
	int  	 iWaterFlow;		//水流量值	0~100000000，单位立方分米/秒(dm?/s)
	int      iResult;		//操作结果	0-成功	1-超出最大个数限制错误	2-设置参数超出范围错误"
}WsTableOptResult, *pWsTableOptResult;


typedef struct tagWaterLevelFlowQuery
{
	int      iSize;
	int      iSceneId;		//场景ID	0～31 预留
	int   	 iPageNo;		//页码	iPageNo>=0
	int  	 iPageSize;		//每页条数	iPageSize>=0 最大20条
	int  	 iWaterLevelStart;		//水位区间起始值	范围:-10000000~10000000，单位:毫米(mm)
	int  	 iWaterLevelStop;		//水位区间结束值	范围:-10000000~10000000，单位:毫米(mm)*/
}WaterLevelFlowQuery, *pWaterLevelFlowQuery;

typedef struct tagWaterFlowContent
{
	int      iSize;
	int      iPageIndex;	//当前页内数据的序号	iIndex>=0
	int	     iNum;	        //当前页内数据的唯一索引号	1-1000
	int	     iWaterLevel;	//当前页内数据的水位值 -10000000~10000000，单位毫米(mm)
	int	     iWaterSpeed;	//当前页内数据的流速值 0~30000，单位：毫米/秒(mm/s)
	int	     iWaterFlow;	//当前页内数据的流量值 0~100000000，单位立方分米/秒(dm?/s)
    int      iPondage;      //当前页内数据的蓄水量 0~1000000000，实际值为浮点型，取值时乘以100，单位：万立方米（10000m3），例如12345代表实际值123.45万立方米
}WaterFlowContent, *pWaterFlowContent;

typedef struct tagWaterLevelFlowQueryResult
{
	int      iSize;
	int      iSceneId;		//场景ID	0～31 预留
	int	     iTotal;		//总条数	iTotal>=0
	int 	 iPageNo;		//页码	iPageNo>=0
	int 	 iPageSize;		//当前页条数	iPageSize>=0
	int 	 iPagePktNum;   //当前页本次包的数据个数(N)	iPagePktNum >= 0
	WaterFlowContent  tContent[LEN_20];
}WaterLevelFlowQueryResult, *pWaterLevelFlowQueryResult;


#define DATA_FROM_RADAR		0
#define DATA_FROM_ALGO		1
#define MAX_DATA_SOURCE		2
typedef struct tagIrriGationRecord
{
	int                  iSize;
	int                  iType;		     //记录分类	参考水利上报信息类型
	unsigned long long   iSeqStart;		//查询开始序号	iSeqStart>0
	unsigned long long 	 iSeqStop;		//查询结束序号	iSeqStop>0
	int 	             iPageNo;		//当前页号	iPageNo>=0
	int 	             iPageSize;		//每页条数	iPageSize>=0 最大20条*/
	int                  iQueryType;    //查询条件类型	0-按序号段查询 1-按时间段查询
	NVS_FILE_TIME        tStartTime;    //开始时间
	NVS_FILE_TIME        tEndTime;		//结束时间
}IrriGationRecord, *pIrriGationRecord;

typedef struct tagIrriGationRecordEx
{
	int                  iSize;
	int                  iType;		     //记录分类	参考水利上报信息类型
	unsigned long long   iSeqStart;		//查询开始序号	iSeqStart>0
	unsigned long long 	 iSeqStop;		//查询结束序号	iSeqStop>0
	int 	             iPageNo;		//当前页号	iPageNo>=0
	int 	             iPageSize;		//每页条数	iPageSize>=0 最大20条*/
	int                  iQueryType;    //查询条件类型	0-按序号段查询 1-按时间段查询
	int                  iStartTime;    //开始时间
	int                  iEndTime;		//结束时间
}IrriGationRecordEx, *pIrriGationRecordEx;

typedef struct tagIrriGationRecordResult
{
	int                  iSize;
	int                  iType;		     //记录分类	参考水利上报信息类型
	unsigned long long   iSeqStart;		//查询开始序号	iSeqStart>0
	unsigned long long 	 iSeqStop;		//查询结束序号	iSeqStop>0
	int 	             iPageNo;		//当前页号	iPageNo>=0
	int 	             iPageSize;		//每页条数	iPageSize>=0 最大20条*/
	unsigned long long   iTotal;	//总条数	iTotal>=0
	int                  iResult;	//查询结果	"0-成功 1-超出最大个数限制错误 2-读写错误	
	int                  iMaxSeqNo;//此类型记录的最大序号
}IrriGationRecordResult, *pIrriGationRecordResult;

//远程执行cmd命令
typedef struct tagRemoteCmdShell
{
	int       iSize;            //结构体大小
	char      cCmdString[LEN_128];  //命令字符串
	int       iRetLimit;        //	执行结果条数限制
}RemoteCmdShell, *pRemoteCmdShellCmdShell;

//远程执行Cmd命令结果
typedef struct tagRemoteCmdShellResult
{
	int      iSize;             //结构体大小
	int      iIndex;            //回执序号；
	char     cResultString[LEN_512];//回执字符串，最大512
}RemoteCmdShellResult, *pRemoteCmdShellResult;

//设置条形码
typedef struct tagBarCode
{
    char			cBarCode[LEN_64];			//条形码
    char            cAliSn[LEN_64];             //阿里云SN
} BarCode, *pBarCode;

//http推图参数/推图测试命令
typedef struct tagHttpPicStream
{
	int				iSize;
	char			cIPAdress[256];
	char			cURL[256];
	char            cUserName[64];
	char			cPassWord[64];
	int				iPort;
}HttpPicStream,*pHttpPicStream;

//http推图参数/推图测试命令
typedef struct HttpPicStreamEx
{
	int				iSize;
	char			cIPAdress[256];
	char			cURL[256];
	char            cUserName[64];
	char			cPassWord[64];
	int				iPort;
	int				iProtocalType; // 0:http,1:https
}HttpPicStreamEx,*pHttpPicStreamEx; //以后推图测试协议扩展的话扩展这个

#define MAX_HTTP_SERVER_NUM 32

typedef struct tagHttpPicStreamParam
{
	int				iServerNum;
	HttpPicStream	tHttpPicStream[MAX_HTTP_SERVER_NUM];
	int				iProtocolType[MAX_HTTP_SERVER_NUM]; //0:http;1:https
}HttpPicStreamParam,*pHttpPicStreamParam;

//推图测试命令回复
typedef struct tagHttpPicTestResult
{
	int      iSize;				     //结构体大小
	int      iResult;                //结果
}HttpPicTestResult, *pHttpPicTestResult;

#define MAX_CHECK_RULE_REGION_NUM 8
//室内电动车检测
typedef struct tagVCAIndoorEBike
{
	int                 iSize;
	int					iDevType;								//0-IPC，1-NVR
	VCARule				tRule;
	int                 iDisplayRule;                           //0-hide 1-show
	int					iDisplayTarget;                         //0-hide 1-show
	int					iMaxSize;								//[0-100] 25
	int					iMinSize;								//[0-100] 8
	int					iSensitivity;							//[0-100] 50
	int					iAlarmTime;								//[2-3600] 2s
	int                 iRegionNum;								//[0-8]
	vca_TPolygon        tPolygon[MAX_CHECK_RULE_REGION_NUM];			//The number of vertices of the polygon area and coordinate 
}VCAIndoorEBike, *pVCAIndoorEBike;

#define RET_GAT1400_STATUS_RESERVED         0
#define RET_GAT1400_STATUS_ONLINE           1

typedef struct tagGat1400StatusResult
{
    int		iSize;  //[IN]结构体大小
    int     iResult; //[OUT] RET_GAT1400_STATUS_RESERVED：预留RET_GAT1400_STATUS_ONLINE：在线 其他 ：其他不在线
}Gat1400StatusResult, *pGat1400StatusResult;

#define MAX_GAT1400_CHANNEL_COUNT 32
typedef struct tagGat1400Para
{
    int        iSize;               //结构大小
    int        iChannelNo;          //通道号	通道号
    int        iEnable;		        //是否使能	0不使能，使能
    char       cIpAddress[LEN_256];//ip地址	不超过字节
    int        iPort;               //服务器端口	1-65535
    char       cDeviceID[LEN_256]; //	设备编号	不超过字节
    char       cUserName[LEN_128]; //	账户名	不超过字节
    char       cPassWord[LEN_128]; //	密码	不超过字节
    int        iHeartBeatInterval;  //		心跳间隔时间	单位秒,范围~255秒
    int        iHeartBeatTimes; 		    //心跳次数	范围~255
    char       cPlaceCode[LEN_64]; //行政代码	不超过字节
    int        iLongitude;  		//经度	范围-36000 对应-180~+180，精确到.01   (-E +W)
    int        iLatitude;           //纬度	范围-18000 对应-90~+90，精确到.01     (-S +N)
    int        iTimingEnable;		//启用校时	0为关闭，为开启
    int        iRetryTimes;         //		重发次数	范围~3
    int        iRetryInterval;      //	重发间隔	单位秒,范围~5秒
    int        iTimingInterval;     //校时时间间隔	单位秒，范围-3600
    int        iConfFileNo;         //		配置文件编号	范围-100
    int        iChannelCount;       //		通道个数	设置支持通道个数n  n不超过32
    int        iChannelNoArr[MAX_GAT1400_CHANNEL_COUNT]; //第一个通道号0   （通道号值0~n-1）
    char 	   cChannelNumberArr[MAX_GAT1400_CHANNEL_COUNT][LEN_64]; //通道编号数组
}Gat1400Para, *pGat1400Para;

typedef struct tagDevRealTimeParam
{
	 int       iChannelNo;          //通道号
	 int	   iVoltageValue;       //电压值网络传送设备电压值*10。实际电压值=iVoltageValue/10，单位是V（伏特）
	 int       iQuantityper;        //电量剩余百分比，实际值除以10
	 int       iDeviceStatus;       //设备功耗状态 0：正常状态，1：低功耗状态
}DevRealTimeParam, *pDevRealTimeParam;

typedef struct tagVCALeaveDetectEx
{
	VCARule				tRule;
	vca_TDisplayParam   tDisplayParam;
	int					iLeaveAlarmTime;			// off-duty alarm time, 60 ~ 3600, in seconds, the default: 120S
	int					iRuturnClearAlarmTime;		// back to the police alarm time, 10 to 300, in seconds Default: 15S
	int					iDutyNum;					// [1,2] 默认1
	int					iMinSize;					// [1, 50] 默认 3
	int					iMaxSize;					// [5, 100] 默认 15
	int					iSensitivity;				// [0, 5] 默认 2	
	int					iDisplayTarget;             // 0-hide 1-show
	int					iAreaNum;					// Total number of rule fields, 1 ~ 4
	vca_TPolygon		tPolygon[MAX_RULE_REGION_NUM];	//	2~8。如果点数等于2是矩形 
	
}VCALeaveDetectEx, *pVCALeaveDetectEx;

typedef struct tagVcaRuleEvent15
{
	int iSize;
	int iChannelNo;
	int iSceneId;
	int iRuleId;
	int iDetDisturbFlag;
}VcaRuleEvent15,*pVcaRuleEvent15;

typedef struct tagCommonProgress
{
	int iSize;
	int iType;/*16-调用水利算法检测预置位*/
}CommonProgress, *pCommonProgress;

typedef struct tagCommonProgressResult
{
	int iSize;
	int iType;/*11-场景切换,16-调用水利算法检测预置位*/
	int iStatus;//进度状态
	int iPro;//进度值
}CommonProgressResult, *pCommonProgressResult;

typedef struct
{
    int iDiskNo;                        // disk No.
    int iFullFlag;                       // 0:not full, 1:full
}NVS_DISKINFOFIELD, *PNVS_DISKINFOFIELD;

#define DISK_NUM_MAX	100
typedef struct
{
    int iSize;
    int iDiskNum;                       // disk count
    int iParamNum;                      // param count
    NVS_DISKINFOFIELD stDiskInfos[DISK_NUM_MAX];    // 
}NVS_DISKINFOEX, *PNVS_DISKINFOEX;


#define PARAM_TYPE_RESERVED 0
#define PARAM_TYPE_INPUT    1
#define PARAM_TYPE_OUTPUT   2

typedef struct tagWIEGAND
{
    int				iSize;
    int             iChannelNo; //通道号
    int				iType;       //参数类型 0-保留，1-输入，2-输出
    int				iParam;     //参数当iType =1时：0-关闭，1-开启   当iType =2时：0-关闭，1- Wiegand26，2-Wiegand34 
    int             iOutData;   //韦根输出数据内容0-保留，1-编号，2-卡号
}WIEGAND, *pWIEGAND;

typedef struct tagEntranceGuardInfo
{
    int             iSize;
    int             iChannelNo;             //通道号
    int             iInfoType;              //上报信息类型:0-预留，1-门牌号，2-业主手机号
    char            cInfoData[LEN_64];      //上报信息内容
    char            cDeviceSN[LEN_64];      //设备SN号

}EntranceGuardInfo, *pEntranceGuardInfo;

typedef struct tagTalkRequest
{
    int             iSize;
    int             iChannelNo;             //通道号
    char            cDeviceSN[LEN_64];      //设备SN号

}TalkRequest, *pTalkRequest, TalkStop, *pTalkStop;

typedef struct tagTalkCoder {
    int iSize;
    int iAudioEncode;       //1 G711A，2 G711U，22 AAC
    int iSampleRate;        //实际采样率的值，如8000,16000,32000
} TalkCoder, *pTalkCoder;

#define MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT		4

typedef struct _tagDayScheduleTimeEx
{
	int  				iStartHour;
	int  				iStartMin;
	int  				iStopHour;
	int  				iStopMin;		
} DayScheduleTimeEx, *PDayScheduleTimeEx;

typedef struct tagDeviceLowPower{
	int					iSize;
	int					iEnable;					//0-未启用，1-启用
	DayScheduleTimeEx	tDevLowPowerSchedule[MAX_WEEK_DAYS][MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT];//0-6：Monday-Sunday
}DeviceLowPower, *pDeviceLowPower;

typedef struct tagLENPARA
{
	int				iSize;
	int             iChannelNo;				//通道号
	int				iFocusOffsetValue;		//聚焦补偿值 1000~21000
	int				iSceneID;				//[in]场景ID 0-15
}LENPARA, *pLENPARA;

typedef struct tagItsChnLaneExParam
{
	int             iChannelNo;				//[in]通道号
	int				iSceneID;				//[in]场景ID 0-15
	int				iLaneNumber;			//[in]车道编号0-3
	int             iChannelSnapDirection;	//[out]车道抓拍方向0-双向，1-上行，2-下行
}ItsChnLaneExParam, *pItsChnLaneExParam;


typedef struct tagNasTest
{
	int			iDiskNo;			//磁盘编号
	int			iNasType;			//网络附属存储类型 1：NFS、2：CIFS、3：FTP、4：HTTP
	char		pcIP[LEN_256];		//ip地址
	int			iPort;				//端口
	char		pcMapPath[LEN_256];	//映射路径
	char		pcUserName[LEN_128];//用户名
	char		pcPassWord[LEN_128];//密码
}NasTest, *pNasTest;


typedef struct tagNasTestResult
{
	int			iDiskNo;		//磁盘编号
	int			iNasType;		//网络附属存储类型 1：NFS、2：CIFS、3：FTP、4：HTTP
	int			iResult;		//测试结果 0成功；其他失败
}NasTestResult, *pNasTestResult;

typedef struct tagItsDriveaWayLinkLight
{
	int				iChannelNo;
	int				iSceneId;				//场景ID：0-15
	int				iLinkType;				//联动类型：0：激光,1:白光
	int				iLinkEnable;			//0：使能，1：不使能
	int				iPlayType;				//亮灯方式	0：常亮  1：闪烁
	int				iPlayFrequency;			//亮灯频率 亮灯方式为闪烁时代表闪烁频率 1~5s 可选 其他无意义
	int				iContinuedTime;			//持续时间  10S-300S
}ItsDriWayLinLight,*pItsDriWayLinLight;

typedef struct tagAudioChannel
{
	char pcChannel[LEN_32];					//通道ID	32字节，不包含\0
	int  iConnectType;						//连接方式	0-保留 1-tcp 2-udp
	int  iPlatId;							//平台编号	0-平台1，1-平台2
}AudioChannel,*pAudioChannel;

typedef struct tagRtpServerInfo
{
	int		iStreamTyp;			//码流类型 1- 主码流；2- 副码流；3- 三码流		当iMulticastType=2时，无效
	int		iEnable;			//是否使能	1-使能 0-不使能；每一路码流类型都对应一个使能控制		当iMulticastType=2时，无效	
	char	pcVideoAddress[LEN_64];		//视频服务器地址	最大长度为64字节（支持ipv6），不包含\0		当iMulticastType=2时，代表TS服务器地址
	int		iVideoPort;					//int	视频服务器端口	70-65535		当iMulticastType=2时，代表TS服务器端口
	int		iVideoTTL;			//int	视频保活时间	取值范围 1--128		当iMulticastType=2时，无效
	char	pcAudioAddress[LEN_64];		//音频服务器地址	最大长度为64字节（支持ipv6），不包含\0		当iMulticastType=2时，无效
	int		iAudioPort;			//音频服务器端口	70-65535		当iMulticastType=2时，无效
	int		iAudioTTL;			//音频保活时间	取值范围 1--128		当iMulticastType=2时，无效
	char	pcMetadataAddress[LEN_64];	 //Medatdata服务器地址	最大长度为64字节（支持ipv6），不包含\0		当iMulticastType=2时，无效
	int		iMetadataPort;	//Medatdata服务器端口	70-65535		当iMulticastType=2时，无效
	int		iMetadataTTL;	//Medatdata保活时间	取值范围 1--128		当iMulticastType=2时，无效
	int		iMulticastType;	//组播类型 0-RTP（改名为RTSP），1-SRTP，2-TS	0-RTP（改名为RTSP），1-SRTP，2-TS
}RtpServerInfo, *pRtpServerInfo;


typedef struct tagWaterDetectPos
{
	int			iSceneID;			//场景ID	0~31  （默认使用0~15）
	int			iRuleID;			//规则ID(预留)	0~15  （默认使用 0）
	int			iPosType;			//检测区域类型	0-水位检测
	int			iOperationType;		//操作类型	1-设置检测区域 2-调用检测区域 3-删除检测区域
	int			iGroundID;			//检测区域组号	0-保留 1～20为组号
	int			iPosID;				//检测预置位编号	"范围：0～32
}WaterDetectPos, *pWaterDetectPos;

typedef struct tagWaterDetectPosResult
{
	int			iSceneID;			//场景ID	0~31  （默认使用0~15）
	int			iRuleID;			//规则ID(预留)	0~15  （默认使用 0）
	int			iPosType;			//检测区域类型	0-水位检测
	int			iOperationType;		//操作类型	1-设置检测区域 2-调用检测区域 3-删除检测区域
	int			iGroundID;			//检测区域组号	0-保留 1～20为组号
	int			iPosID;				//检测预置位编号	"范围：0～32
	int			iResult;			//设置结果	"0-成功，非0表示失败 1-PTZ获取失败2-设置检测区域当前角度不合理 3-表示调用检测区域失败 4-表示删除检测区域失败 5-表示获取倾角仪角度失败"
	int			iPtzP;				//球机水平角度	p*1000
	int			iPtzT;				//球机垂直角度	t*1000
	int			iPtzZ;				//镜头变倍倍率	z*1000
	int			iAngleValue;		//倾角仪角度, 倾角仪角度乘以10000，单位是度，实际范围0-360
}WaterDetectPosResult, *pWaterDetectPosResult;


#define IRRIGATION_PLAT_SL206   0 
#define IRRIGATION_PLAT_SL651   1
#define IRRIGATION_PLAT_SCSW    2
#define IRRIGATION_PLAT_GZ_RTU  3
#define IRRIGATION_PLAT_GDXSD   4
#define IRRIGATION_PLAT_ZJSH	5
#define IRRIGATION_PLAT_ZJYR	6
#define IRRIGATION_PLAT_DFSW	7    //地方水文
#define IRRIGATION_PLAT_MAX     8

#define IRRIGATION_SERVERNUM_MAX 4

typedef struct tagIrriServerInfoEx
{
	int				iSerType;					//对接平台服务器类型	0-保留 1-规约SL651 2-四川水文 3-对接贵州RTU 4-广东小水电
	int				iSerNum;					//对接平台服务器总数	支持水利对接平台的数量, 最多4个
	int				iSerIndex;					//对接平台服务器编号	0～3
	char			pcServerIP[LEN_128];		//对接平台服务器的IP	不超过128字节
	int				iServerPort;				//对接平台服务器的端口	范围1-65535
	char			pcStationCode[LEN_16];		//对接平台服务器的遥测站码	小于16字节 当iSerType=1时为测站地址 当iSerType=2时为遥测站码 当iSerType=4时为存储桶名称(Bucket Name)
	char			pcCenterAddr[LEN_16];		//对接平台服务器的中心站地址	小于16字节,当iSerType=1时为中心站地址 当iSerType=3时为对接贵州RTU的用户名 当iSerType=4时为对接平台设备ID
	char			pcCenterPassword[LEN_16];	//对接平台服务器的中心站密码	小于16字节,当iSerType=1时为中心站密码 当iSerType=3时为对接贵州RTU的密码 当iSerType=4时为电站代码
	char			pcServerInfo1[LEN_64];		//对接平台信息1		小于64字节,当iSerType=4时为监测点编码
	char			pcServerInfo2[LEN_64];		//对接平台信息2		小于64字节,当iSerType=4时为测试方式：0-保留 1-泄流闸 2-泄流管道 3-泄流明渠
	char			pcServerInfo3[LEN_64];		//对接平台信息3		小于64字节,当iSerType=4时为数据平台访问KEY_ID(Access Key Id)
	char			pcServerInfo4[LEN_64];		//对接平台信息4		小于64字节,当iSerType=4时为存储平台上报图片参数,例:“1,60”,表示开启间隔60秒抓拍图片并上报
	char			pcServerInfo5[LEN_64];		//对接平台信息5		小于64字节,当iSerType=4时为存储平台上报录像参数,例:“1,3600”,表示开启间隔3600秒上报时长为30秒的录像
	char			pcServerInfo6[LEN_64];		//对接平台信息6		小于64字节,当iSerType=4时为存储平台端口
	char			pcServerInfo7[LEN_128];		//对接平台信息7		小于128字节,当iSerType=4时为存储平台IP
	char			pcServerInfo8[LEN_128];		//对接平台信息8		小于128字节,当iSerType=4时为存储平台访问KEY(Access Key)
	char			pcServerInfo9[LEN_128];		//对接平台信息9		小于128字节,当iSerType=4时为数据平台加密KEY(Secret Access Key)
    char			pcServerInfo10[LEN_128];	//对接平台信息10      小于128字节 当iSerType=1时 表示平台上报图片或数据的网络传输协议 0-预留，1-tcp，2-udp

}IrriServerInfoEx, *pIrriServerInfoEx;

#define IRRIGATION_PARAM_NUM_MAX 50
typedef struct tagIrriServerInfoV2
{
	int				iPlatformType;			//水利对接平台类型 	0-规约SZY206(预留) 1-规约SL651(预留) 2-四川水文(预留) 3-对接贵州RTU(预留) 4-广东小水电(预留) 5-浙江山洪
	int				iDataSum;				//支持水利对接平台的参数总数, 最多50个
	int				iDataType;				//水利对接平台的参数类型
	char			cParamPart1[LEN_256];	//水利对接平台的参数_第1部分
	char			cParamPart2[LEN_256];	//水利对接平台的参数_第2部分
	char			cParamPart3[LEN_256];	//水利对接平台的参数_第3部分
	char			cParamPart4[LEN_256];	//水利对接平台的参数_第4部分
	char			cParamPart5[LEN_256];	//水利对接平台的参数_第5部分
}IrriServerInfoV2, *pIrriServerInfoV2;
				
typedef struct tagIrriUpLoadParam
{
	int			iPlatFormType;		//水利对接平台类型	0-规约SZY206 1-规约SL651 2-四川水文 3-对接贵州RTU 4-广东小水电
	int			iMajorType;			//水利信息上报主类型
	int			iMinorType;			//水利信息上报子类型
	int			iEnable;			//上报数据使能	0：未使能 1：使能
	int			iInterval;			//上报数据时间间隔	60-86400秒
}IrriUpLoadParam, *pIrriUpLoadParam;

typedef struct tagIrriClientQuery
{		
	int					iType;				//记录分类	参考水利上报信息类型
	int					iQueryType;			//查询条件类型	0-按序号段查询 1-按时间段查询
	unsigned long long	iSeqStart;			//查询开始序号	iSeqStart>0，iQueryType=0时有效
	unsigned long long	iSeqStop;			//查询结束序号	iSeqStop>0，iQueryType=0时有效
	NVS_FILE_TIME		tStartTime;			//查询开始时间	utc时间，iQueryType=1时有效
	NVS_FILE_TIME		tEndTime;			//查询截止时间	utc时间，iQueryType=1时有效
	unsigned long long  iPageNo;			//当前页号	iPageNo>=0
	int					iPage;				//每页条数	iPageSize>=0 最大20条
}IrriClientQuery, *pIrriClientQuery;

typedef struct tagIrriClientQueryEx
{		
	int					iType;				//记录分类	参考水利上报信息类型
	int					iQueryType;			//查询条件类型	0-按序号段查询 1-按时间段查询
	unsigned long long	iSeqStart;			//查询开始序号	iSeqStart>0，iQueryType=0时有效
	unsigned long long	iSeqStop;			//查询结束序号	iSeqStop>0，iQueryType=0时有效
	int					iStartTime;			//查询开始时间	utc时间，iQueryType=1时有效
	int					iEndTime;			//查询截止时间	utc时间，iQueryType=1时有效
	unsigned long long  iPageNo;			//当前页号	iPageNo>=0
	int					iPage;				//每页条数	iPageSize>=0 最大20条
}IrriClientQueryEx, *pIrriClientQueryEx; //该结构体只用于嵌入式使用，时间直接传int

typedef struct tagIrriClientQueryResult
{
	int								iSize;
	int								iType;		//上报信息类型	详见【水利信息上报类型】页面的水利上报信息类型
	int								iQueryType;	//查询条件类型	0-按序号段查询 1-按时间段查询
	unsigned long long				uSeqStart;	//查询开始序号	iSeqStart>0，iQueryType=0时有效
	unsigned long long				uSeqStop;	//查询结束序号	iSeqStop>0，iQueryType=0时有效
	NVS_FILE_TIME					tStartTime;	//查询开始时间	utc时间，iQueryType=1时有效
	NVS_FILE_TIME					tStopTime;	//查询截止时间	utc时间，iQueryType=1时有效
	unsigned long long				uPageNo;	//当前页号	iPageNo>=0
	int								iPageSize;	//每页条数	iPageSize>=0 最大20条
	int								iResult;	//查询结果	"0-成功 1-超出最大个数限制错误 2-读写错误"
	unsigned long long				uMaxSeqNo;	//此类型记录的最大序号	iMaxSeqNo>=0
	unsigned long long				iTotal;		//总条数	iTotal>=0
	int								iPageTotal;	//int	当前页数据总条数	<=20
	int								iPageIndex;	//int	当前页数据序号	0-19
	IrrigationPara					tIrrigationPara; //上报具体内容
}IrriClientQueryResult, *pIrriClientQueryResult;//

typedef struct tagIrriClientQueryResultEx
{
	int								iSize;
	int								iType;		//上报信息类型	详见【水利信息上报类型】页面的水利上报信息类型
	int								iQueryType;	//查询条件类型	0-按序号段查询 1-按时间段查询
	unsigned long long				uSeqStart;	//查询开始序号	iSeqStart>0，iQueryType=0时有效
	unsigned long long				uSeqStop;	//查询结束序号	iSeqStop>0，iQueryType=0时有效
	int								iStartTime;	//查询开始时间	utc时间，iQueryType=1时有效
	int								iStopTime;	//查询截止时间	utc时间，iQueryType=1时有效
	unsigned long long				uPageNo;	//当前页号	iPageNo>=0
	int								iPageSize;	//每页条数	iPageSize>=0 最大20条
	int								iResult;	//查询结果	"0-成功 1-超出最大个数限制错误 2-读写错误"
	unsigned long long				uMaxSeqNo;	//此类型记录的最大序号	iMaxSeqNo>=0
	unsigned long long				iTotal;		//总条数	iTotal>=0
	int								iPageTotal;	//int	当前页数据总条数	<=20
	int								iPageIndex;	//int	当前页数据序号	0-19
	IrrigationPara					tIrrigationPara; //上报具体内容
}IrriClientQueryResultEx, *pIrriClientQueryResultEx;//该结构体只用于嵌入式使用，时间直接传int

typedef struct tagWaterDetectPosParam
{
	int			iSceneID;			//场景ID	0~31  （默认使用0~15）
	int			iRuleID;			//规则ID(预留)	0~15  （默认使用 0）
	int			iPosType;			//检测区域类型	0-水位检测
	int			iOperationType;		//操作类型	1-设置检测区域 2-调用检测区域 3-删除检测区域
	int			iGroundID;			//检测区域组号	0-保留 1～20为组号
	int			iPosID;				//检测预置位编号	"范围：0～32
	int			iStatus;			//0-未设置 1-已设置
	int			iPtzP;				//球机水平角度	p*1000
	int			iPtzT;				//球机垂直角度	t*1000
	int			iPtzZ;				//镜头变倍倍率	z*1000
	int			iAngleValue;		//倾角仪角度, 倾角仪角度乘以10000，单位是度，实际范围0-360
}WaterDetectPosParam, *pWaterDetectPosParam;

typedef struct tagAdjustVersionEx
{
	int				iSceneId;
	int				iRuleID;
	int				iFieldOfVision;
}AdjustVersionEx, *pAdjustVersionEx;

typedef struct tagAdjustVersionResult
{
	int				iSceneId;
	int				iRuleID;
	int				iFieldOfVision;
	int				iResult;
}AdjustVersionResult, *pAdjustVersionResult;

typedef struct tagRtuParam
{
	int				iAddress;
}RtuParam, *pRtuParam;


#define MAX_IRRIGATION_GAUGE_GROUP 50
#define MAX_IRRIGATION_GAUGEPRE_ID	75 
typedef struct tagWaterDetectQuery
{
	int			iSize;
	int			iSceneID;		//0~31  （默认使用0~15）
	int			iRuleID;		//0~15  （默认使用 0）
	int			iPosType;		//0-水位检测
	int			iGroupID;		//iPosType为0时，1~20-查询此组水尺当前序号的标定参数
	int			iTotalPosID;	//范围：0～32
	int			iPosID;			//当iPosType=0水位检测时:0-保留 1-水位检测预置位 2-上切预置位 3-下切预置位，10-32为标定预置位
	int			iState;			//0-未设置 1-已设置
	int			iPtzP;			//p*1000
	int			iPtzT;			//t*1000
	int			iPtzZ;			//z*1000
	int			iAngleValue;	//倾角仪角度乘以10000，单位是度，实际范围0-360 软件取值后除以10000.0转换成float
	int			iResult;		//0-查询成功，非0失败 
}WaterDetectQuery, *pWaterDetectQuery;

typedef struct tagWaterDetectCmd
{
	int			iSceneID;		//0~31  （默认使用0~15）
	int			iRuleID;		//0~15  （默认使用 0）
	int			iPosType;		//0-水位检测
	int			iGroupID;		//iPosType为0时，1~20-查询此组水尺当前序号的标定参数
}WaterDetectCmd, *pWaterDetectCmd;

typedef struct tagNewCommonEnable
{
	int		iEnable; //轻量级获取通用使能号
}NewCommonEnable,*pNewCommonEnable;

typedef struct tagNewCommonEnableResult
{
	int		iEnable; //轻量级获取通用使能号
	int		iEnableValue;
}NewCommonEnableResult,*pNewCommonEnableResult;

#define MAX_SERVICE_TYPE		2
typedef struct tagNetServicePara
{
	int			iServiceType;					//1代表HTTPS，2代表SRTP
	char		pcServerCertificateID[LEN_128]; //证书ID	最长128字节
	int			iEncryptionAlgo;				//加密算法	1代表AES128，2代表AES256
	int			iPort;							//SRTP接收服务端口号	默认322
}NetServicePara, *pNetServicePara;

#define MAX_CERTIFICATE_NUM 16
typedef struct tagCertificate
{
	int			iCount;							//证书总数:1-16
	int			iNum;							//证书序号
	char		pcServerCertificateID[LEN_128];	//证书ID
	char		pcStartTime[LEN_32];			//证书有效期开始时间
	char		pcEndTime[LEN_32];				//证书有效期结束时间
	int			iState;							//证书状态:0 异常；1 正常
	int			iUser;							//使用者:按位bit0:http  bit1:SRTP
	char		cIssuerCommonName[LEN_32];				//证书颁发者的名字
	char		cIssuerCountryName[LEN_32];				//证书颁发者的国家
	char		cIssuerStateOrProvinceName[LEN_32];		//证书颁发者的省或州
	char		cIssuerLocalityName[LEN_32];			//证书颁发者的地区
	char		cIssuerOrgnazationName[LEN_32];			//证书颁发者的组织
	char		cIssuerOrgnazationUnitName[LEN_32];		//证书颁发者的单位
	char		cIssuerEmail[LEN_64];					//证书颁发者的邮件
	char		cSubjectCommonName[LEN_32];				//证书归属人的设备标识
	char		cSubjectCountryName[LEN_32];			//证书归属人的国家
	char		cSubjectStateOrProvinceName[LEN_32];	//证书归属人的省或州
	char		cSubjectLocalityName[LEN_32];			//证书归属人的地区
	char		cSubjectOrgnazationName[LEN_32];		//证书归属人的组织
	char		cSubjectOrgnazationUnitName[LEN_32];	//证书归属人的单位
	char		cSubjectEmail[LEN_64];					//证书归属人的邮件
	char		cSerialNumber[LEN_64];					//证书序列号
}Certificate, *pCertificate;


typedef struct tagVideoParam
{
	int			iSize;
	int			iVencType;
	int			iVideoSize;
	int			iMinBitrate;
}VideoParam, *pVideoParam;

typedef struct tagVideoLimit
{
	int				iGroup;
	int				iNum;
	VideoParam		tVideoParam[LEN_128];
}VideoLimit, *pVideoLimit;

typedef struct tagCmdDelFile
{
	int			iType;
	char		cFileName[LEN_128];
}CmdDelFile, *pCmdDelFile;

typedef struct tagCmdDelFileResult
{
	int			iType;
	char		cFileName[LEN_128];
	int			iResult;
}CmdDelFileResult, *pCmdDelFileResult;

typedef struct tagVcaAnySenceAdvanced
{
	int			iSize;
	int			iChannelNo;
	int			iSceneId;
	int			iDevType;
	int			iFocusType;
}VcaAnySenceAdvanced,*pVcaAnySenceAdvanced;

#ifndef MAX_SINGLE_COM_PERIPHERAL_NUM
#define MAX_SINGLE_COM_PERIPHERAL_NUM 255
#endif
typedef struct tagPeripheralInfoEx
{
	int					iComNum;
	int					iPeripheralType;
	int					iPeripheralNum;
	int					iPeripheralAddr[MAX_SINGLE_COM_PERIPHERAL_NUM];
	int                 iEnable[MAX_SINGLE_COM_PERIPHERAL_NUM];
}PeripheralInfoEx, *pPeripheralInfoEx;

typedef struct tagOsdLogo
{
	char	cLogoFile[LEN_256];
	int		iBackColor; //Logo背景颜色
	int		iType;		//Osd Type1：政法合成通道novideo替换 2：政法示证通道全屏遮挡
}OsdLogo,*pOsdLogo;

typedef struct tagCommonVersion
{
	int			iSize;
	int			iVersionID;
	char		cVersion[LEN_32];
}CommonVersion, *pCommonVersion;

typedef struct __tagAlarmPortInfo
{
	int			iArmInPortId;				//报警输入端口号
	int			iArmInEnable;				//报警输入端口开启状态
	int			iArmInUsage;				//报警输入端口用途
	int			iArmInTigger;				//报警撤防触发方式
	int			iArmInLinkage;				//撤防联动项
} AlarmPortInfo;

#define METHOD_VERSION			 1
#define CPLD_VERSION			 2
#define DOME_DRIVE_VERSION		 3
#define DIGITAL_MOVEMENTVERSION	 4
#define TRADE_P2P_VERSION		 5
#define TRADE_ONVIF_VERSION		 6
#define TRADE_ITS_VERSION		 7
#define TRADE_28181_VERSION		 8
#define TRADE_ZFZJ_VERSION		 9
#define TRADE_H323_VERSION		 10
#define TRADE_RTSP_VERSION		 11
#define TRADE_RTMP_VERSION		 12
#define HPD_VERSION				 13
#define FRONTPANEL_VERSION		 14
#define ALGOMODEL_VERSION		 15
#define INTELLIGENT_VERSION		 16
#define LANDMARK_VERSION		 17
#define SECONDDOMEDRIVE_VERSION	 18
#define ENCRYPTCHIP_VERSION		 19
#define ISP_VERSION				 20
#define FACEPADM3VERSION		 21
#define MOBILE_MODULE_VERSION	 22
#define LOW_POWER_CHIP_VERSION   23
#define ALI_METHOD_VERSION		 24
#define MAX_EXTEND_VERSION_COUNT 32

#define DISARM_MANUAL				1
#define DISARM_CYCLE				2
#define DISARM_ALARMINPUT			3

typedef struct tagEmbed_Version
{
	int					iVersionNum;//maxnum = MAX_EXTEND_VERSION_COUNT
	CommonVersion		tCommonVersion[MAX_EXTEND_VERSION_COUNT];
}Embed_Version, *pEmbed_Version; //

typedef struct  tagServer_VersionEx
{
	int			iVersionNum;
	char		cVersion[MAX_EXTEND_VERSION_COUNT][LEN_64];
}Server_VersionEx, *pServer_VersionEx;

typedef struct tagVcaCpcAdvancePart2
{
	int	 iSceneID;
	int  iAlarmTimeout;//超限报警超时时间	0：无超时；1~3600秒
	int  iDisplayRule; //规则线是否显示		0：预留 1：显示 2：不显示
	int  iDisplayInfo; //统计信息是否显示	0：预留 1：显示 2：不显示
}VcaCpcAdvancePart2;


#define MAX_PRIORITY_LEVEL	30
typedef struct tagVcaRuleSetAdvance
{
	int		iSceneId;	//场景号 0-15
	int		iRuleID;	//规则号 0-31
	int		iPriorityLevel;//优先级 0-30
}VcaRuleSetAdvance, *pVcaRuleSetAdvance;

typedef struct tagVideoResolution
{
	int		iWidth;
	int		iHeight;
} VideoResolution, *pVideoResolution;

typedef struct tagXmlSetRegionClipPara
{
	int					iChannelNo;				//通道号从0开始 
	int					iStreamNo;				//码流编号 0:主码流，1:副码流，2:三码
	int					iEnable;				//是否启用区域剪裁，true:启用  falae:不启用
	VideoResolution		tClipResolution;		//区域裁剪分辨
	vca_TPolygon		tClipRegion;			//裁剪区域坐标参数
} XmlSetRegionClipPara, *pXmlSetRegionClipPara;

#define MAX_SUPCLIP_RESOLUTION_COUNT	16
typedef struct tagXmlGetRegionClipPara
{
	int					iChannelNo;				//通道号从0开始
	int					iStreamNo;				//码流编号 0:主码流，1:副码流，2:三码
	int					iEnable;				//是否启用区域剪裁，true:启用  falae:不启用
	VideoResolution		tCurClipResolution;		//区域裁剪当前分辨率
	int					iSupClipResolutionCount;	//设备支持裁剪分辨率个数
	VideoResolution		tSupClipResolutionArray[MAX_SUPCLIP_RESOLUTION_COUNT];	//设备支持裁剪分辨率列表
	vca_TPolygon		tClipRegion;			//裁剪区域坐标参数
} XmlGetRegionClipPara, *pXmlGetRegionClipPara;

#define  MAX_SUPCOORDINANTE_COUNT			10	
typedef struct tagXmlJudgeRegionInfo
{
	int					iChannelNo;				//通道号从0开始
	int					iScence;				//scene/<ID>：场景号，默认发0
	int					iRegionInfoID;
	int					iRegionInfoNamePlateCount;
	vca_TPolygon		tNamePlateCoordinate[MAX_SUPCOORDINANTE_COUNT];
	int					iRegionInfoSeatCount;
	vca_TPolygon		tSeatCoordinate[MAX_SUPCOORDINANTE_COUNT];
}XmlJudgeRegionInfo, *pXmlJudgeRegionInfo;

typedef struct tagXmlExpourseSync
{
	int				iChannelNo;					//通道号从0开始
	int				iTemplateID;				//模板ID
	int				iType;						//不同的多曝光类型 0-监控图像；1-抓拍图像；2-分析图像；3-红外监控图像；4-红外抓拍图像；5-红外分析图像（暂不使用）
	int				iEnable;					//0：使能 1：不使能
}XmlExpourseSync, *pXmlExpourseSync;

typedef struct tagCreateAuthenticateFile
{
	int					iRequestType;								//请求类型	1代表GB35114证书请求文件
	char					cCountryName[LEN_32];				//国家
	char					cPassword[LEN_32];						//密码
	char					cStateOrProvinceName[LEN_32];	//省或州
	char					cLocalityName[LEN_32];				//地区
	char					cOrgnazationName[LEN_32];			//组织
	char					cOrgnazationUnitName[LEN_32];	//单位
	char					cEmail[LEN_32];								//邮件
	char					cSIPDeviceID[LEN_64];					//sip配置中的设备ID
}CreateAuthenticateFile, *pCreateAuthenticateFile;

typedef struct tagCreateAuthenticateFileResult
{
	int					iRequestType;								//请求类型	1代表GB35114证书请求文件
	int					iState;											//结果	0-成功 其他失败
}CreateAuthenticateFileResult, *pCreateAuthenticateFileResult;

typedef struct tagXmlHnrEnable
{
	int				iChannelNo;					//通道号从0开始
	int				iTemplateID;				//模板ID
	int				iType;						//不同的多曝光类型 0-监控图像；1-抓拍图像；2-分析图像；3-红外监控图像；4-红外抓拍图像；5-红外分析图像（暂不使用）
	int				iEnable;					//0：使能 1：不使能
}XmlHnrEnable, *pXmlHnrEnable;

typedef struct tagXmlSplicingParam
{
	int				iChannelNo;					//通道号从0开始
	int				iSplicingSection;			//拼接截面距离 单位：米 范围：1m~10000m
}XmlSplicingParam, *pXmlSplicingParam;

#define MAX_DESINITY_NUM 3
typedef struct tagDesityLevel
{
	int				iLevelID;			//代表区域密度等级编号
	int				iCountLimit;		//代表区域密度等级人数，范围：1-999999
	int				iDutyLimit;			//代表区域密度等级占空比，范围：0-100%
}DesityLevel, *pDesityLevel;

#define MAX_REGION_COUNT 10
typedef struct tagValidRegion
{
	int				iRegionID;				//检测区域编号
	int				iRegionEnable;			//代表检测区域是否有效，1：有效、0：无效
	int				iRationEnable;			//代表是否启用占空比，1：启用、0：不启用
	char			cRegionName[LEN_64];	//区域名称
	int				iOsdType;				//代表区域字符叠加类型，0-无(不叠加)，1-人数，2-人数/等级/占空比
	int				iOsdPosX;				//区域字符叠加横坐标：万分比
	int				iOsdPosY;				//区域字符叠加纵坐标：万分比
	int				iDestylevelCount;		//区域密度等级配置个数
	DesityLevel		tDesityLevel[MAX_DESINITY_NUM]; 
	vca_TPolygon	tClipRegion;
}ValidRegion, *pValidRegion;

typedef struct tagValidRegionList
{
	int				iRegionCount;
	ValidRegion		tValidRegion[MAX_REGION_COUNT];
}ValidRegionList, *pValidRegionList;

#define MAX_VALID_REGION_NUM 8
#define COLOR_TYPE_RED			0//red,green,yellow,blue,purple,cyan,black,white
#define COLOR_TYPE_GREEN		1
#define COLOR_TYPE_YELLOW		2
#define COLOR_TYPE_BLUE			3
#define COLOR_TYPE_PURPLE		4
#define COLOR_TYPE_CYAN			5
#define COLOR_TYPE_BLACK		6
#define COLOR_TYPE_WHITE		7

typedef struct tagXmlPersonnelDestiny
{
	int					iChannelNo;								//通道号，赋值从0开始
	int					iSceneID;								//场景号,从0开始
	int					iRuleID;								//规则号,从1开始
	int					iDeviceType;							//设备类型 0 nvr 1 ipc
	int					iEnable;			 					//算法是否启用：0不启用，1启用
	int					iSensitive;								//灵敏度：0~100   (默认值:50)
	int					iDisPlayRule;							//显示规则：1-显示，0-不显示
	int					iDisPlayStat;							//显示报警计数：1-显示，0-不显示
	int					iDisPlayTarget;							//显示目标：1-显示，0-不显示
	int					iMinSize;								//最大尺寸8-100 默认25
	int					iMaxSize;								//最小尺寸1-50默认8
	int					iColor;									//颜色:0-red,1-green,2-yellow,3-blue,4-purple,5-cyan,6-black,7-white
	int					iAlarmColor;							//报警颜色,同上
	int					iValidRegionCount;						//检测区域列表，区域个数1~8
	ValidRegion			tValidRegion[MAX_REGION_COUNT];
	int					iMaskRegionCount;						//屏蔽区域列表，区域个数0~8 可以不存在
	vca_TPolygon		tMaskRegionList[MAX_VALID_REGION_NUM];
}XmlPersonnelDestiny, *pXmlPersonnelDestiny;

typedef struct tagAirLinesPlan
{
	int					iSceneId;				//场景编号	0～31（默认使用0～15），预留
	int					iPlanType;				//航线计划生成类型	0-保留，1-根据垂线生成，2-等间距生成
	int					iWayPointInterval;		//航点间距	当iPlanType=2时有效，范围:0～10000000,单位:mm
	int					iResult;				//飞行航线生成结果	0-成功，其他-失败
	unsigned long long	ullStartLongitude;		//开始航点的经度,浮点数乘以10的14次方
	unsigned long long	ullStartLatitude;		//开始航点的纬度,浮点数乘以10的14次方
	unsigned long long	ullEndLongitude;		//结束航点的经度,浮点数乘以10的14次方
	unsigned long long	ullEndLatitude;			//结束航点的纬度,浮点数乘以10的14次方
	int					iRetest;				//0-保留 1-返航复测使能 2-返航复测不使能
}AirLinesPlan, *pAirLinesPlan;

typedef struct tagQueryAirLineInfo
{
	int					iSceneId;	//场景号 0～31 预留
	int					iPageNo;	//页码 iPageNo>=0
	int					iPageSize;	//每页条数 iPageSize>=0 最大20
	int					iParamNum;	//每条信息需要的参数个数
}QueryAirLineInfo, *pQueryAirLineInfo;

typedef struct tagLocationInfo
{
	unsigned long long	ullSize;				//结构体大小
	unsigned long long	ullStartLongitude;		//开始航点的经度,浮点数乘以10的14次方
	unsigned long long	ullStartLatitude;		//开始航点的纬度,浮点数乘以10的14次方
}LocationInfo, *pLocationInfo;

#define MAX_LOCATION_COUNT	20
typedef struct tagQueryAirLineInfoResult
{
	int					iSceneId;
	int					iTotal;		//总条数
	int					iPageNo;	//页码
	int					iPageSize;	//当前页条数
	int					iParamNum;	//每组结果的参数个数
	LocationInfo		tLocationInfo[MAX_LOCATION_COUNT];
}QueryAirLineInfoResult, *pQueryAirLineInfoResult;

//ITS*OPTIM_PART2设置/获取车牌最小尺寸协议废弃了
//typedef struct tagMinSizeLicensePlate
//{
//	int iSceneId;      //场景ID
//	int iMinSize;      //车牌最小尺寸
//}MinSizeLicensePlate, *pMinSizeLicensePlate;

typedef struct tagCalibratePosition
{
	int				iLeft;
	int				iTop;
	int				iRight;
	int				iBottom;
}CalibratePosition, *pCalibratePosition;

typedef struct tagXmlGetCalibrateParam
{
	int						iChannelNo;
	CalibratePosition		tCalibratePosition;
}XmlGetCalibrateParam, *pXmlGetCalibrateParam;

typedef struct tagXmlGetCalibrateResultParam
{
	int			iPtzP;		//<pan> 单位：度，实际值x1000
	int			iPtzT;		//<tilt> 单位：度，实际值x1000
	int			iPtzZ;		//单位：倍率，实际值x1000
}XmlGetCalibrateResultParam, *ptagXmlGetCalibrateResultParam;

typedef struct tagXmlAngelViewParam
{
	int			iChannelNo;//通道号，从0开始
	int			iHorizontalAngle;//水平视角0~100（代表-50%到50%）
	int			iVerticalAngle;//垂直视角0~100（代表-50%到50%）
}XmlAngelViewParam, *pXmlAngelViewParam;

//INNER*PARAGET*LINKSOUNDTIME协议暂时不用了，功能有问题，嵌入式未实现
//typedef struct tagAlarmLinkSoundPara
//{
//	int			iStructSize;
//	int			iSoundTime;			//报警联动声音提示时长
//} AlarmLinkSoundPara, *pAlarmLinkSoundPara;

#define MAX_PIC_COUNT	9	//多画面对应的主机视频通道最大数量
#define SHOW_MODE_ONE				0x01
#define SHOW_MODE_TWO				0x02
#define SHOW_MODE_THREE_FIRST		0x03
#define SHOW_MODE_THREE_SECOND		0x13
#define SHOW_MODE_FOUR_FIRST		0x04
#define SHOW_MODE_FOUR_SECOND		0x14
#define SHOW_MODE_FOUR_THIRD		0x24
#define SHOW_MODE_FIVE				0x05
#define SHOW_MODE_SIX				0x06
#define SHOW_MODE_SEVEN				0x07
#define SHOW_MODE_EIGHT				0x08
#define SHOW_MODE_NINE				0x09
#define SHOW_MODE_CUSTOM1			0xF1
#define SHOW_MODE_CUSTOM2			0xF2
#define SHOW_MODE_CUSTOM3			0xF3
#define SHOW_MODE_CUSTOM4			0xF4
#define SHOW_MODE_CUSTOM5			0xF5
#define SHOW_MODE_CUSTOM6			0xF6
#define SHOW_MODE_CUSTOM7			0xF7
#define SHOW_MODE_CUSTOM8			0xF8
#define SHOW_MODE_CUSTOM9			0xF9

typedef struct tagPicToChan
{							//下面两个参数按列表发送，表示每个画面对应的主机视频通道，即设置每个物理输入通道在多画面中的显示位置，通道参数不能重复
	int			iPicId;		//画面编号，范围0~8，0表示画面1
	int			iChn;		//通道号，范围0~14，0表示通道1
} PicToChan, *pPicToChan;

typedef struct tagXmlPreviewModeParam
{
	int			iPreviewId;	//输出设备，0x01-主屏（VGA/HDMI1），0x02-副屏（HDMI2）
	int			iType;		//切换方式，0x00-预览设置，0x01-激励切换，0x02-激励结束切换，0x03-UI设置多画面
	int			iPicMode;	//显示模式，0x01-1画面，0x02-2画面，0x03-3画面第1种方式，0x13-3画面第2种方式，
							//0x04-4画面第1种方式，0x14-4画面第2种方式，0x24-4画面第3种方式，0x05~0x09-5画面~9画面，
							//0xf*（*=1~9）	用户自定义画面；
	int			iPicToChanSize;//多画面对应主机 结构体的大小
	PicToChan	tPicChanList[MAX_PIC_COUNT];
} XmlPreviewModeParam, *pXmlPreviewModeParam;

#define MAX_AUDIOCHAN_COUNT	128		//音频通道最大数量
#define GETALL_AUDIOCHN		0xFE	//读取全部音频通道信息
typedef struct tagXmlBestirChnParam
{
	int iStruSize;			//结构体大小
	int iAudioChn;			//音频通道，有效范围0x00~0x7f,全部通道0xfe
	int iPriority;			//优先级，有效范围为0x00~0x0f,数值越小级别越高，0x10表示通道未启用
	int iVideoChn;			//视频通道，有效范围为0x00~0x07，依次对应主机8路视频输入
	int iLinkPtzEnable;		//联动PTZ使能，有效范围为true/false
	int iPtzAddr;			//快球地址，有效范围为0x00~0xfd，0xfe表示该通道为定点
	int iPresetbit;			//预置位，有效范围为0x01~0xfe;//特殊预置位
	int iNameId;			//名称编号：1字节，默认编号为0（预留）
	int iSensitivity;		//灵敏度，音频检测阈值，范围1~255；手拉手MIC音频检测范围为2～100
} XmlBestirChnParam, *pXmlBestirChnParam;

typedef struct tagXmlBestirChnParamArr
{
	int					iCount;		//数组有效值数量
	XmlBestirChnParam	tXmlBestirChnParam[MAX_AUDIOCHAN_COUNT];
} XmlBestirChnParamArr, *pXmlBestirChnParamArr;

typedef struct tagXmlBestirStateParam
{
	int		iMode;						
	int		iAudioChn;
	int		iVideoChn;
	int		iMute;
	int		iVolume;
}XmlBestirStateParam, *pXmlBestirStateParam;

#define			SCREENSHOWCOUNT		10
#define			VGNCHNCOUNT			15
typedef struct tagXmlScreenEnable
{
	int		iScreenid;				//显示屏输出通道编号,范围:1~10，对应后面板10路输出
	int		iEnable;				//是否输出,false-不输出,ture-输出
}XmlScreenEnable, *pXmlScreenEnable;

typedef struct tagXmlProof
{
	int			iProofState;			//	多画面示证1输出状态
	int			iProofState1;			// 多画面示证2输出状态
}XmlProof, *pXmlProof;

typedef struct tagXMLVgaSwitchinfoParam
{
	int					iViChn;			//vga输入通道号,范围:1-12(硬件对应的vga数量) 13-14 (HDMI多画面) 15（HDMI无线投屏）
	int					iScreenCount;
	XmlScreenEnable		tScreenlist[SCREENSHOWCOUNT];
	XmlProof			tProofState;
	int					iMixAoChn;			//混音通道号,范围:0/1，0-不混音,1-混音
}XmlVgaSwitchinfoParam, *pXmlVgaSwitchinfoParam;



typedef struct tagReferenceCoordinate
{
	int	iX;
	int	iY;
	int	iZ;
} ReferenceCoordinate,*pReferenceCoordinate;

#define REFERENCE_POINT_NUM_MAX 20

typedef struct tagCalibrationReference
{
	int iSize;													//结构体大小
	int	iSceneId;												//场景ID 0~15
	int	iPointNum;												//参考物坐标点个数 4~10
	ReferenceCoordinate tPointImg[REFERENCE_POINT_NUM_MAX];		//像素坐标
	ReferenceCoordinate tPointWld[REFERENCE_POINT_NUM_MAX];		//世界坐标
	int iIpcHigh;												//相机安装高度
	ReferenceCoordinate tIpcPara;								//相机安装点相对参考原点坐标参数
	ReferenceCoordinate tCoordinateOPara;						//地图坐标系O点参数
	ReferenceCoordinate tCoordinateXPara;						//地图坐标系x轴上选定点的参数
	ReferenceCoordinate tCoordinateYPara;						//地图坐标系y轴上选定点的参数
} CalibrationReferenceInfo,*pCalibrationReferenceInfo;


#define JUDGE_ANALYZE_LATE					0
#define JUDGE_ANALYZE_LEAVE					1
#define JUDGE_ANALYZE_HALFWAY				2
#define JUDGE_ANALYZE_ABSENT				3
#define JUDGE_ANALYZE_CLOTHDETECT			4
#define JUDGE_ANALYZE_BADAGEDETECT			5
#define JUDGE_ANALYZE_NAMEPLATE				6
#define JUDGE_ANALYZE_CALL					7
#define JUDGE_ANALYZE_TYPE_MAX				8

typedef struct tagJudgeAnyParam
{
	int			iSize;
	int			iAnalyeType;	//法官席的分析类型，类型有：0-迟到；1-早退；2-中途离席；3-缺席；4-制服检测；5-法徽检测；6-标牌识别；7-打电话检测。
	int			iEnable;		//当前类型是否启用。
	int			iRegionListCount;//生效区域个数
	int			iRegionList[LEN_16];		// 生效区域ID列表，多个区域用‘，’分隔，如：1，2，3。
	int			iNpValidListCount;	//> 号牌遮挡区域个数,ID列表，多个区域用‘，’分隔，如：1，2，3。号牌遮挡区域列表，【号牌识别功能中有效】。
	int			iNpValidList[LEN_16];		//ID列表，多个区域用‘，’分隔，如：1，2，3。号牌遮挡区域列表，【号牌识别功能中有效】
	int			iSensitivity;	//灵敏度：0~100  默认值 50
	int			iMinSize;		//最小尺寸0~100默认s
	int			iMaxSize;		//最大尺寸0~100 默认25
	int			iLeaveTime;		//允许离开时间 单位s ，1~3600，默认值：300
}JudgeAnyParam, *pJudgeAnyParam;

typedef struct	tagXmlJudgeAnyInfo
{
	int				iChannel;		//通道号
	int				iScene;			//场景号，默认发0
	int				iDisplayRule;	//是否显示规则 0：不显示 1：显示
	int				iDisplayStat;	//是否显示状态 0：不显示 1：显示
	int				iDisplayTarget;	//是否显示目标框 0：不显示 1：显示
	int				iTypeCount;		//设置法官行为分析算法个数
	JudgeAnyParam	tParam[JUDGE_ANALYZE_TYPE_MAX];
}XmlJudgeAnyInfo, *pXmlJudgeAnyInfo;



typedef struct	tagXmlJudgeCaseConfig
{
	int			iChannel;		///通道号
	int			iSceneID;		//场景号，默认发0
	int			iPartInNum;		//法官席出席人数 最多16【仅在caseControl=开庭时有效】
	int			iCaseControl;	// 0:开庭；1闭庭；2休庭；3休庭结束；
}XmlJudgeCaseConfig, *pXmlJudgeCaseConfig;


/*简单命令码如下：只接受PUT的enable=true，不支持GET
0x0001，中控恢复出厂设置。
0x0011，恢复语音激励绑定通道设置。
0x0021，示证切换恢复默认。
0x0032，手拉手mic操作：初始化所有mic地址。
0x0033，手拉手mic操作：复位所有mic地址。
0x0034，手拉手mic操作：关闭所有mic。

复杂命令码如下：带value参数，只支持PUT不支持GET
0x0311，手动激励测试。value=MIC号，对应配置文件中的16个mic配置，范围：0~15。
0x0321，示证切换参数应用。value=vga输入通道，范围1-12(硬件对应的vga数量)。
0x0332，手拉手mic个数。value=mic个数，范围0~128。
0x0333，手拉手mic 打开/关闭。value表示mic id，范围1~128；enable的true=打开/false=关闭。
0x0334，手拉手mic 启用/禁用。value表示mic id，范围1~128；enable的true=启用/false=禁用。

参数类型码如下：支持PUT/GET
0x0101，触摸屏启动状态。true=启用，false=禁用（出厂默认）。
0x0111，激励控制方式。value=控制方式，0x00-手动音频激励、0x01-自动激励、0x02-手动视频激励。
0x0112，激励延迟开始时间。预留，默认为0，界面不可设。
0x0113，激励延迟停止时间。音频消失多长时间认为音频消失，恢复激励之前状态。value=时间值，范围：0~100，单位为100ms。

0x0114，是否启用特写切换。true=启用，false=禁用（出厂默认）
0x0115，激励结束后画面是否恢复。true=不恢复，false=恢复（出厂默认）。
0x0131，mic类型。value=mic类型，0-手拉手话筒，1-点对点话筒（默认）。
0x0132，启用手拉手mic控制器。true=启用，false=禁用（出厂默认）。

0x0161，蓝牙设备名称。value=名称字符串，长度1-20字节，不要包含不可见字符。
0x0162，蓝牙配对密码。value=密码字符串，长度固定为6字节，每个字节取值范围'0'-'9', 默认值：“000001”。
0x0163，蓝牙配对码使能状态。true=需输入配对码连接。false=不需要输入配对码即可连接（出厂默认）。

特殊参数类型码如下：只支持GET
0x0231，查询手拉手mic在线列表。value=在线mic列表，格式”1,2,5,…,16”。
0x0252，查询强电控制器状态。value=状态列表，格式”1,0,0,0” ，按控制器编号排序，1=在线/0=不在线。
0x0261，读取蓝牙设备状态。value=状态码，0-空闲状态，没广播也没有连接；1-模块正在广播；2-模块处于连接状态。
*/
typedef struct	tagXmlCommonEnable
{
	int			iType;//如上图
	int			iEnable;//代表使能，1：开启、0：关闭
	char		cValue[LEN_32];//代表参数
}XmlCommonEnable, *pXmlCommonEnable;

typedef struct tagXmlCommonProgress
{
	int			iType;			//获取进度类型；1-人脸底图导出 2-人脸检索查询总数 3-热度图异步统计 4-目标报警异步统计 5-通道报警异步统计 6-异步报表导出9-WIFI热点参数设置进度 10-标定状态查询 11—场景切换状态查询 17-人脸门禁用户数据导出box；18-人脸门禁用户数据导入box;19人脸门禁批量添加用户数据zip ，20-人脸门禁删除用户数据，21-人脸门禁脱机事件导出,25-拼接设置状态查询
	int			iState;			//状态，0-未导入/未导出/标定/未场景切换/未设置拼接，1-导入/导出/标定中/场景切换中/拼接中，2-导入/导出/标定成功/场景切换成功/拼接成功，3-导入/导出/标定失败/场景切换失败/拼接失败
	int			iProgress;		//<pro>进度值, 0-100
	int			iResultNum;		//处理结果数量
}XmlCommonProgress, *pXmlCommonProgress;

typedef struct tagXmlSessionInfo
{
	unsigned long long			iSessionID;
}XmlSessionInfo, *pXmlSessionInfo;

#ifndef EVENT_VIDEODETECT
#define EVENT_VIDEODETECT 10
#endif

#ifndef EVENT_JUDGE_BEHAVIOR_ANALYZE
#define EVENT_JUDGE_BEHAVIOR_ANALYZE 90
#endif

#ifndef EVENT_AUDIODETECT
#define EVENT_AUDIODETECT 16
#endif

#ifndef MAX_DEVICE_TYPE_COUNT 
#define MAX_DEVICE_TYPE_COUNT	2
#endif

#ifndef ALARM_TYPE_CUR_MAX
#define ALARM_TYPE_CUR_MAX		48
#endif

#ifndef MAX_SCENE_ALARM_NUM
#define MAX_SCENE_ALARM_NUM     16
#endif

#ifndef MAX_RULE_NUM_EX
#define MAX_RULE_NUM_EX			16
#endif

#ifndef ALARM_EVENT_MAX
#define ALARM_EVENT_MAX			91
#endif


#define MAX_CHANNEL_VCAALARMINFO (MAX_DEVICE_TYPE_COUNT*MAX_SCENE_ALARM_NUM*MAX_RULE_NUM_EX*ALARM_EVENT_MAX)
typedef struct tagCurVcaAlarmInfo
{

	int					iDeviceType;							//设备类型 0 nvr 1 ipc
	int                 iAlarmState;							//报警状态 1，报警；0，消警
	char				cAlarmTime[LEN_20];						//报警时间：格式：年-月-日-星期-时-分-秒-毫秒
																//例如：2011090120101056：2020年11月9日星期一20点10分10秒560毫秒
																//毫秒为实际值除以10之后的数值，如560ms实际赋值为56=560/10
	int					iAlarmType;								//报警类型

	int					iSceneID;								//场景ID：0-15（当iType=4-智能分析时有效，其他固定发0）
	int					iRuleID;								//规则ID：0-15（当iType=4-智能分析时有效，其他固定发0）
	int					iEventType;			 					//事件类型,当iType=4-智能分析时，参考（智能分析列表）	
																//当iAlarmType=10001, 表示报警子类型 0:冲顶故障, 1:蹲底故障, 2:电梯异常急停, 3:电梯超速, 4:门区外停梯, 
																//5:到站后不开门, 6:电梯晃动, 7:开门走梯, 8:困人, 9:主动报警 10:传感器故障  11:电动车入梯
																//12：跌落 13：安全回路报警 14：视频遮挡报警 15：未关门报警

	union {
		int iVideoDiagnosis;		//当iEventType=10-视频诊断，表示int型，报警子类型按位1-报警 0-消警：bit0-场景变换; bit1-信号缺失; bit2-亮度过亮; bit3-亮度过暗; bit4-视频模糊;
									//bit5-视频偏色; bit6-噪声干扰; bit7-画面冻结; bit8-人为干扰; bit9-PTZ失控; bit10-人为遮挡;  bit11-对比度异常; bit12-黑白图像; bit13-图像晃动; bit14-条纹干扰。
		int iJudgeBehaviorAnalysis;//当iEventType=90-法官行为分析，表示int型，报警子类型按位1-报警 0-消警：0-迟到; bit1-早退; bit2-中途离席; bit3-缺席; bit4-制服检测; bit5-法徽检测; bit6-标牌识别; bit7-打电话检测。
	
		int iAudioDiagnosis;        //当iEvnetType=16-音频诊断，表示int型，报警子类型按位1-报警 0-消警：bit0:音频丢失 bit1:音频异常
	};	
}CurVcaAlarmInfo;

typedef struct tagChannelVcaAlarmInfo
{
	int iCount;  //有效值数量
	CurVcaAlarmInfo tCurAlarmInfo[MAX_CHANNEL_VCAALARMINFO];
}ChannelVcaAlarmInfo;

#define MAX_CHANNEL_NOVCAALARMINFO (MAX_DEVICE_TYPE_COUNT*ALARM_TYPE_CUR_MAX)
typedef struct tagCurNoVcaAlarmInfo
{
	int					iDeviceType;							//设备类型 0 nvr 1 ipc
	int                 iAlarmState;							//报警状态 1，报警；0，消警
	char				cAlarmTime[LEN_20];						//报警时间：格式：年-月-日-星期-时-分-秒-毫秒
																//例如：2011090120101056：2020年11月9日星期一20点10分10秒560毫秒
																//毫秒为实际值除以10之后的数值，如560ms实际赋值为56=560/10
	int					iAlarmType;								//报警类型
	int					iSceneID;								//场景ID：0-15（当iType=4-智能分析时有效，其他固定发0）
	int					iRuleID;								//规则ID：0-15（当iType=4-智能分析时有效，其他固定发0）
	int					iEventType;			 					//事件类型当iAlarmType=10001, 表示报警子类型 0:冲顶故障, 1:蹲底故障, 2:电梯异常急停, 3:电梯超速, 4:门区外停梯, 5:到站后不开门, 6:电梯晃动, 7:开门走梯, 8:困人, 9:主动报警 10:传感器故障 11:电动车入梯

}CurNoVcaAlarmInfo, *pCurNoVcaAlarmInfo;

typedef struct tagChannelNoVcaAlarmInfo
{
	int iCount;  //有效值数量
	CurNoVcaAlarmInfo tCurAlarmInfo[MAX_CHANNEL_NOVCAALARMINFO];
}ChannelNoVcaAlarmInfo;

typedef struct tagChannelCurAlarmInfo
{
	//智能分析报警信息
	int iCurVcaAlarmInfoStructSize;//智能报警信息CurVcaAlarmInfo结构体大小，传入参数，不传获取不到信息
	ChannelVcaAlarmInfo tChannelVcaAlarmInfo;

	//非智能分析报警信息
	int iCurNoVcaAlarmInfoStructSize;//非智能报警信息ChannelNoVcaAlarmInfo结构体大小，传入参数，不传获取不到信息
	ChannelNoVcaAlarmInfo tChannelNoVcaAlarmInfo;
}ChannelCurAlarmInfo;


typedef struct tagXmlH323ChnNoParam
{
	int iH323GroupId;				//H323 number,default 0
	int iMainSrcChnNo;				//Encode main stream channel number
	int	iSubSrcChnNo;				//Encode sub stream channel number
	int	iMainDecChnNo;				//Decode main stream channel number
	int	iSubDecChnNo;				//Decode sub stream channel number

}XmlH323ChnNoParam;

#define MAX_H323CALLID   4
#define MAX_H323GROUPID  4

typedef struct tagXmlH323LocalParam
{
	
	int iListenPort;				//Listen port
	char cLocalNo[LEN_64];			//Terminal number
	char cPassWord[LEN_64];			//Encrytion password
	int iIdCodeType;				//ID code mode，0-UTF-8， 1-Unicode
	int iAnwTypeForCall;			//Responds by call：0-automatic,1-manual,2-not disturb

	int iGKMode;					//GK Mode:0-disable,1-designation,2-auto search
	char cGKAddress[LEN_64];		//GK address,64 bytes
	int iGKPort;					//GK port
	char cRegisterName[LEN_64];		//Register Name,64 bytes
	int iGKEncrytionType;			//GK Encrytion mode,0-shut down,1-automatic,2-zhongxing,3-xinshitong,4-sike

	int iCount;
	XmlH323ChnNoParam tXmlH323ChnNoParam[MAX_H323GROUPID];

}XmlH323LocalParam, *pXmlH323LocalParam;


typedef struct tagXmlCallState
{

	int iH323CallId;				//会话配置序号: 0~3
	int iH323GroupId;				//H323编号，默认0, 范围0~3
	char cIpAddress[LEN_64];		//IP地址
	int iPort;						//端口号
	int iCallType;					//H323呼叫类型，默认0 ，0-H323，1-SIP，2-自动
    int iSipNetLinkType;            //sip呼叫时信令连接方式 0 – UDP，1 - TCP，sip呼叫时有效。默认udp
	int iCallState;					//H323会议状态 0-空闲 1-呼叫中 2-已连接 3-对方拒绝 4-对方关闭 5-远程呼入 6-呼叫失败

}XmlCallState;



#define MAX_H323CALLSTATE  MAX_H323GROUPID*MAX_H323CALLID

typedef struct tagXmlH323CallState
{

	int iGKState;									//H323网守状态，0-不在线，1-在线;
	int iCount;										//数组有效值
	XmlCallState  tXmlCallState[MAX_H323CALLSTATE]; 

}XmlH323CallState,*pXmlH323CallState;

typedef struct tagXmlH323SingleDev
{

	int  iSetType;					//设置类型，0默认（增加/修改），1挂断，2呼叫，3接听，4拒绝，5删除 
	int  iStreamType;				//h323视频流类型， 0主流，1辅流
	int  iH323CallId;				//会话配置序号: 0~3
	int  iH323GroupId;				//H323编号，默认0, 范围0~3
	int  iCallType;					//H323呼叫类型，默认0 ，0-H323，1-SIP，2-自动
	int  iSipNetLinkType;            //sip呼叫时信令连接方式 0 – UDP，1 - TCP，sip呼叫时有效。默认udp
	int  iCallMode;					//呼叫模式，0-纯音频 1-音视频
	int  iCallRate;					//呼叫速率，64kbps-8Mbps
	char cIpAddress[LEN_64];		//IP地址
	int  iPort;						//端口号
	char cNetName[LEN_64];			//设备号码
	char cUri[LEN_64];				//URI字符
	char cCallMsg[LEN_64];			//拨号信息
}XmlH323SingleDev;

#define MAX_H323ITEMCOUNT  MAX_H323CALLSTATE
typedef struct tagXmlH323ArrDev
{								
	int iCount;										//数组有效值
	XmlH323SingleDev  tXmlDevInfo[MAX_H323ITEMCOUNT]; 

}XmlH323ArrDev,*pXmlH323ArrDev;

typedef struct tagXmlH323SingleSubVideo
{

	int  iSubVideoType;				//辅流状态，0断开，1连接
	int  iH323CallId;				//会话配置序号: 0~3
}XmlH323SingleSubVideo;

typedef struct tagXmlH323ArrSubVideo
{								
	int iCount;										//数组有效值
	XmlH323SingleSubVideo  tXmlSubVideo[MAX_H323GROUPID]; 

}XmlH323ArrSubVideo,*pXmlH323ArrSubVideo;

#define MAX_COMMONDATA_LEN		(32*1024)

typedef struct tagCommonData
{		
	int					iDataType;					//数据类型	0：执法办案区用户数据
	int					iDataLen;					//数据的总长度
	char				cData[MAX_COMMONDATA_LEN];	//二进制数据
}CommonData,*pCommonData;


typedef struct tagPtzOperation
{
	int					iSceneId;	//场景ID(预留)	0~31  （默认使用0~15）
	int					iOptType;	//云台操作类型	0-预留 1-垂直云台校正
}PtzOperation, *pPtzOperation;

typedef struct	tagXmlScheduleEnable
{
	int			iDayOfWeek;		
	int			iItemEnable;	//是否启用
}XmlScheduleEnable;

typedef struct tagPtzOperationResult
{
	int					iSceneId;		//场景ID(预留)	0~31  （默认使用0~15）
	int					iOptType;		//云台操作类型	0-预留 1-垂直云台校正
	int					iResult;		//设置结果	0-成功，非0表示失败
	char				cResultInfo[LEN_512];	//结果信息	iResult字段不为0时有效 表示对应错误码的错误信息
}PtzOperationResult, *pPtzOperationResult;

typedef struct
{
	unsigned short 		iHour;   				/* Hour */
	unsigned short 		iMinute; 				/* Minute */
	unsigned short 		iSecond; 				/* Second */
} HourMinuteSecond;

typedef struct	tagScheduleTimeRange
{
	HourMinuteSecond        tBeginTime;	//开始时间
	HourMinuteSecond		tEndTime;	//结束时间
}ScheduleTimeRange;



typedef struct	tagScheduleWeekTimeRange
{
	int						 iDayOfWeek;		
	ScheduleTimeRange        tScheduleTimeRange;	
}ScheduleWeekTimeRange;


typedef struct	tagSchedule
{
	int         iType;			//法官行为分析：【late-迟到；JUDGE_ANALYZE_LATE early-早退；JUDGE_ANALYZE_LEAVE halfWay-中途离席JUDGE_ANALYZE_HALFWAY ；absent-缺席；clothDetect-制服检测；badgeDetect-法徽检测；namePlate-标牌识别；call-打电话检测】
	int			iChannel;		//通道号
	int			iSceneID;		//场景号，默认发0
	char        cID[LEN_32];			//代表输入端口
	char        cEventType[LEN_32];		// 
	char        cInputIOPortID[LEN_32];
	char        cOutputIOPortID[LEN_32];
	char        cVideoInputChannelID[LEN_32];

	int            iScheduleEnableCount;
	XmlScheduleEnable tScheduleEnable[LEN_64];

	int				      iScheduleWeekTimeRangeCount;
	ScheduleWeekTimeRange tScheduleWeekTimeRange[LEN_64];

	int					iScheduleHolidayTimeRangeCount;
	ScheduleTimeRange	tScheduleHolidayTimeRange[LEN_64];

}XmlSchedule, *pXmlSchedule;

typedef struct tagXmlSmartPicQueryCondition
{
	char			cChnLst[LEN_64]; //通道列表，逗号分割："1,2,3,4"
	int				iPageNo;
	int				iPageSize;
	char			cType[LEN_16];
	char			cSubType[LEN_16];
	NVS_FILE_TIME	tBeginTime;
	NVS_FILE_TIME	tEndTime;

}XmlSmartPicQueryCondition,*pXmlSmartPicQueryCondition;


typedef struct tagXmlSmartPicQueryResult
{
	int		iIndex;
	char	cPath[LEN_64];
	int     iChannel;
	char	cPicTime[LEN_16];
	char    cType[LEN_16];
	char	cSubType[LEN_64];
	int     iPicSize;
}XmlSmartPicQueryResult;

#define MAX_SMART_PIC_QUERTY_RESULT_NUM LEN_64
typedef struct tagXmlSmartPicQueryResultArr
{
	int		iTotalCount;
	int		iPageSize;
	XmlSmartPicQueryResult tXmlSmartPicQueryResult[MAX_SMART_PIC_QUERTY_RESULT_NUM];

}XmlSmartPicQueryResultArr,*pXmlSmartPicQueryResultArr;


//电梯业务
typedef struct tagElevatorMonitor
{
	int					iChannelNo;			//通道号	预留
	int					iStartShockThreshold;//启动冲击阈值	单位：mgal 范围：0-100000
	int					iMoveSpeed;			//正常运行速度	单位：mm/s 范围：0-3500
	int					iBodyInductionMode;	//人员检测方式	0-视频检测 1-PIR 2-自动
	int					iEbikeDetectEnable;	//电动车入梯报警	0-不启用 1-启用
	int					iSwaySensitivity;	//晃动报警灵敏度	范围0-100
	int					iTopLimit;			//最高楼层
	int					iBottomLimit;		//最低楼层
	int					iMainFloor;			//基站层
	int					iLevelingMode;		//平层信号有效类型	0-低有效 1-高有效 2-上边沿（预留） 3-下边沿（预留）
	int					iOpenDoorMode;		//开关门信号有效类型	0-低有效 1-高有效 2-上边沿（预留） 3-下边沿（预留）
	int					iMaintenanceMode;	//检修有效类型	0-低有效 1-高有效 2-上边沿（预留） 3-下边沿（预留）
	int					iCrashStopMode;		//急停有效类型	0-低有效 1-高有效 2-上边沿（预留） 3-下边沿（预留）
	int					iPIRMode;			//PIR信号有效类型	0-低有效 1-高有效 2-上边沿（预留） 3-下边沿（预留）
	int					iDefaultHeight;		//默认楼层高度,单位: mm
	int					iStoreyNum;			//楼层总数,自动生成楼表时使用该值
}ElevatorMonitor, *pElevatorMonitor;

#define MIN_LOOR_LEVING -20
typedef struct tagElevatorStoreyInfo
{
	int					iChannelNo;					//通道号	预留
	int					iStartFloor;				//起始楼层
	int					iEndFloor;					//结束楼层
	int					iFloorHeight[LEN_256];		//楼层高度,代表从-20 到 235 层的高度，iFloorHeight[0]代表-20层高度，依次累加,
													//iFloorHeight[20]代表1层高度，依次类推，转换关系为: 楼层号 - MIN_LOOR_LEVING为高度数组下标,楼层号为0代表1层
	char				cStoreyDisplayList[LEN_256][LEN_32];	//楼层1~256显示信息,字符串 32字节限制，编码方式：UTF-8	
}ElevatorStoreyInfo,*pElevatorStoreyInfo;


typedef struct tagElevatorState
{
	int		iChannelNo;		//通道号	预留
	int		iFloor;			//楼层码	当前楼层
	int		iDirection ;	//方向	方向: 0-无方向 1-上行 2-下行
	int		iSpeed;			//运行速度	当前运行速度
	int		iTemperature;	//温度	温度值（单位：摄氏度*10）
	int		iHumidity;		//湿度	相对湿度（单位：%*10）
	int		iBindBrake;		//抱闸状态	0-报闸关闭 1-报闸打开
	int		iMaintenance;	//检修状态	0-非检修状态 1-检修状态
	int		iLeveling;		//平层状态	0-不再平层 1-在平层
	int		iOpenDoor;		//轿门状态	0-轿门关闭 1-轿门打开
	int		iCrashStop;		//急停状态	0-正常 1-急停
	int		iBodyInduction;	//人员检测状态	0-无人 1-有人
	int		iMainFloor;		//基站状态	0-不再基站 1-在基站
	int		iAcceler;		//纵向加速度,当前运行纵向加速度 值*1000，保留三位有效数据（单位m/s2）
	char	cFloorDisplay[LEN_32];		//字符串 32字节限制，编码方式：UTF-8
}ElevatorState,*pElevatorState;

typedef struct tagElevatorStatistics
{
	int			iChannelNo;			//通道号	预留
	int			iBindBrakeCn;		//抱闸次数	累计统计抱闸次数
	int			iOpenDoorCn;		//开关门次数	累计统计开关门次数
	int			iOperationMileage;	//运行距离	累计统计运行距离 单位mm
	int			iOperationTime;		//运行时间,累计统计运行时间 （单位s）
}ElevatorStatistics,*pElevatorStatistics;

#define	 SDK_BIT_COUNT_INT					32			//表示一个int有32位
typedef struct tagElevatorAllState
{
	int		iChannelNo;		//通道号	预留
	int		iFloor;			//楼层码	当前楼层
	int		iDirection ;	//方向	方向: 0-无方向 1-上行 2-下行
	int		iSpeed;			//运行速度	当前运行速度
	int		iTemperature;	//温度	温度值（单位：摄氏度*10）（软件取值后减1000之后除以10，精度0.1，3001为非法值表示未检出数据）
	int		iHumidity;		//湿度	相对湿度（单位：%*10）（软件取值后减1000之后除以10，精度0.1，3001为非法值表示未检出数据）
	int		iBindBrake;		//抱闸状态	0-报闸关闭 1-报闸打开
	int		iMaintenance;	//检修状态	0-非检修状态 1-检修状态
	int		iLeveling;		//平层状态	0-不再平层 1-在平层
	int		iOpenDoor;		//轿门状态	0-轿门关闭 1-轿门打开
	int		iCrashStop;		//急停状态	0-正常 1-急停
	int		iBodyInduction;	//人员检测状态	0-无人 1-有人
	int		iMainFloor;		//基站状态	0-不再基站 1-在基站
	int		iAcceler;		//纵向加速度,当前运行纵向加速度 值*1000，保留三位有效数据（单位m/s2）

	int		iBindBrakeCn;		//抱闸次数	累计统计抱闸次数
	int		iOpenDoorCn;		//开关门次数	累计统计开关门次数
	int		iOperationMileage;	//运行距离	累计统计运行距离 单位mm
	int		iOperationTime;		//运行时间,累计统计运行时间 （单位s）

	int		iAlarmList[SDK_BIT_COUNT_INT];//设备异常报警事件,按位使能 1-发生报警 0-无报警,bit0:冲顶故障bit1:蹲底故障bit2:电梯异常急停bit3:电梯超速bit4:门区外停梯bit5:到站后不开门
								//bit6:电梯晃动bit7:开门走梯bit8:困人bit9:主动报警bit10:传感器故障bit11:电动车入梯bit12:跌落bit13:安全回路报警bit14:遮挡报警

	char    cAlarmTime[LEN_32];			//格式：年-月-日-星期-时-分-秒-毫秒,毫秒为实际值除以10之后的数值，如560ms实际赋值为56=560/10,2011090120101056：2020年11月9日星期一20点10分10秒560毫秒
	char	cFloorDisplay[LEN_32];		//字符串 32字节限制，编码方式：UTF-8		
}ElevatorAllState,*pElevatorAllState;


#define MAX_3D_TRANSFROM_NUM 8 //32私有协议会超长

#define USE_CUR_DEV_PTZINFO 0
#define USE_SET_PTZINFO		1

typedef struct tagXmlThreeDimTransCondition
{
	int				iChannelNo;         //通道号 源数据通道号

	int				iPtzSet;			//0-使用设备通道当前ptz信息进行 1-指定使用iPan，iTile，iZoom进行转换

	int				iPan;				//实际值*1000
	int				iTilt;				//实际值*1000
	int				iZoom;				//实际值*1000

	int				iAimChannelNo;		//目标通道号

	int				iCount;
	TPoint			tTPoint[MAX_3D_TRANSFROM_NUM];		//万分比

	unsigned int    uiRecvTimeOut;//接收协议超时时间ms，建议设置3000-10000之间
	
}XmlThreeDimTransCondition,*pXmlThreeDimTransCondition;

typedef struct
{
	char cResult[LEN_8];     //处理结果success-转换成功 error-异常数据 busy-设备忙
	int iX;
	int iY;
} TransResultPoint;

typedef struct tagXmlThreeDimTransResult
{
	int              iCount;
	TransResultPoint tTransResultPoint[MAX_3D_TRANSFROM_NUM];
}XmlThreeDimTransResult;

#define MAX_ONVIFVCAALARM_ARRAY_COUNT		32
#define ONVIF_VCAALARM_SERIAL_NUMBER		1
#define ONVIF_VCAALARM_TEMPERATURE			2
typedef struct tagOvfVcaAlarmPara
{
	int					iParaType;					//参数类型,1-序号；2-温度；
	char				cParam[LEN_64];				//参数
} OvfVcaAlarmPara, *pOvfVcaAlarmPara;

typedef struct tagOnvifVcaAlarm
{
	int					iChannelNo;					//通道号		标识进行智能分析的通道
	int					iState;						//状态			1，报警；0，消警
	int					iEventType;					//事件类型
	int					iArrayCount;				//参数组数量	后面跟n组参数，最大32组
	int					iParaCount;					//每组参数个数
	int					iParaStructSize;			//sizeof(OvfVcaAlarmPara)
	OvfVcaAlarmPara		tOvfVcaAlarmPara[MAX_ONVIFVCAALARM_ARRAY_COUNT];
} OnvifVcaAlarm, *pOnvifVcaAlarm;

typedef struct tagXmlZFChnInfo
{
	int				iChnNo;         //通道号 当Type=0时候：取值范围据设备类型而定
									//当Type=1时候：取值0~32，主机的MIC输入
									//当Type=2时候：取值0~127，主机的MIC输入
									//当Type=3时候：取值0~32

	int				iChnNameType;   //当Type=0时候，选择内容：
									//1-法官席，2-原告席，3-公诉席，4-上诉席，5-被告席，6-辩护席，7-被上诉席，8-嫌疑人席，9-证人席，10-旁听席，11-全景，12-同声传译席，13-示证1，14-示证2，15-证人保护，16-证人席2， 100-合成画面，101-远程1，102-远程2，103-远程3，104-远程4，200-备用1，201-备用2
									//当Type=1时候，选择内容：
									//300-书记员电脑，301-公诉席，302-辩护席，303-证人席，304-多媒体，305-物证展台，
									//当Type=2时候，选择内容：400-审判长，401-陪审员
}XmlZFChnInfo,*pXmlZFChnInfo;

#ifndef MAX_SDK_CHANNEL_NUM
#define MAX_SDK_CHANNEL_NUM 360
#endif 

#define ZF_CHANNEL_TYPE_VIDEO		0
#define ZF_CHANNEL_TYPE_DEMOINPUT	1
#define ZF_CHANNEL_TYPE_MIC			2
#define ZF_CHANNEL_TYPE_DEMOOUTPUT	3

typedef struct tagXmlZFChnInfoArr
{
	int				 iType;//通道类型，0-视频通道，1-示证输入，2-点对点/手拉手MIC，3-示证输出
	int              iCount;
	XmlZFChnInfo	 tXmlZFChnInfo[MAX_SDK_CHANNEL_NUM];
}XmlZFChnInfoArr,*pXmlZFChnInfoArr;

typedef struct tagRefBoundaryInfo
{
	int					iSceneId;								//场景ID 0~15 暂时无实际意义、使用0值
	int					iEnable;								//使能
	int					iPointNum;								//边界点个数 4~20
	ReferenceCoordinate	tWldPointCoord[REFERENCE_POINT_NUM_MAX];//世界点坐标	x:y:z   当前处在世界坐标系中坐标
																//xyz的取值范围 0-20000000(代表-10000.000m-10000.000m)
																//10000000为无效值、最小有效值为1(0.001m)
} RefBoundaryInfo, *pRefBoundaryInfo;

#define	MAX_RADAR_LINK_SCENE_NUM		32
typedef struct tagRadarLinkSceneInfo
{
	int					iSceneId;		//场景ID-0	0~31
	int					iStarPosDis;	//检测场景开始位置	0~100000,单位：厘米
	int					iEndPosDis;		//检测场景结束位置	0~100000,单位：厘米
} RadarLinkSceneInfo, pRadarLinkSceneInfo;

typedef struct tagRadarLinkSceneArr
{
	int					iParaListNum;						//有效联动场景参数个数	0~32
	int					iParaCnt;							//每组参数的个数	当前为3，场景ID*检测场景开始位置*检测场景结束位置
	int					iRadarLinkSceneInfoSize;			//一个RadarLinkSceneInfo结构体的大小
	RadarLinkSceneInfo	tRadarLinkSceneInfo[MAX_RADAR_LINK_SCENE_NUM];	//场景参数信息
} RadarLinkSceneArr, *pRadarLinkSceneArr;

#define MAX_WATERMARK_LENGTH  256
typedef struct tagDigitalWaterMark
{
	int					iEnable;								//是否使能,0：不使能  1：使能
	char				cWatermarkData[MAX_WATERMARK_LENGTH];	//用户设置的数字水印字符串
} DigitalWaterMark, *pDigitalWaterMark;

typedef struct tagHuTemValue
{
	int					iSize;
	int					iChannelNo;
	int					iType;
	NVS_FILE_TIME		tOccurTime;
	int					iValue;
}HuTemValue, *pHuTemValue;

typedef struct tagGetHuTemValue
{
	int					iSize;
	int					iChannelNo;
	int					iType;			//0:temperature 1:humidity 2:differential pressure
}GetHuTemValue, *pGetHuTemValue;

typedef struct tagWaterOutFall
{
	int					iSize;
	int					iChannelNo;
	int					iDevType;		//0-IPC，1-NVR
	int					iSceneID;		//The ID Of Scene 0~15
	int                 iRuleID;		//The ID Of Rule 0～7
	int                 iValid;			
	int					iColor;					
	int                 iAlarmColor;             
	int                 iDisplayRule;     
	int                 iDisplayStat;   
	int                 iDisplayTarget;    
	int                 iMiniSize;      
	int                 iMaxSize;		
	int                 iSensitivity;		
	int					iRegionNum;					
	vca_TPolygonEx		stRegionPointCoordinate[MAX_DETECT_AREA_NUM];
	vca_TLine			stRegionDirectionLine[MAX_DETECT_AREA_NUM];
}WaterOutFall, *pWaterOutFall;

#define	MAX_REGIONCOORD_NUM		32
#define MAX_REGIONFALL_COUNT	8

typedef struct tagValidRegionFall
{
	int					iRegionNo;					//代表检测区域编号，区域编号范围1~8
	int					iRegionEnabled;				//代表检测区域是否有效，true：有效、false：无效
	int					iOsdType;					//代表区域字符叠加类型，0-无(不叠加)，1-高空抛物报警
	int					iOsdPosX;					//代表区域字符叠加横坐标：万分比
	int					iOsdPosY;					//代表区域字符叠加纵坐标：万分比
	vca_TPolygonEx		tRegionCoord;				//代表区域坐标列表，坐标个数3~32
} ValidRegionFall, *pValidRegionFall;

#define FALLINGOBJECT_VALID_REGION	0
#define FALLINGOBJECT_MASK_REGION	1
typedef struct tagXmlFallingObject
{
	int					iRuleId;					//规则号 0~15
	int					iChannelNo;					//通道号
	int					iSceneId;					//场景ID
	int					iModel;						//模式 0-nvr 1-ipc
	int					iEnabled;					//是否有效，true：启用、false：不启用
	int					iSensitivity;				//灵敏度：0~100   (默认值:50)
	int					iDisPlayRule;				//显示报警规则：true-显示，false-不显示
	int					iDisPlayStat;				//显示报警计数：true-显示，false-不显示
	int					iDisPlayTarget;				//显示目标：true-显示，false-不显示
	int					iDisPlayTrack;				//显示轨迹：true-显示，false-不显示
	int 				iMinSize;					//最小尺寸1-50默认8
	int 				iMaxSize;					//最大尺寸8-100 默认25
	int 				iValidRegionCount;			//代表检测区域列表，区域个数1~8
	ValidRegionFall		tValidRegion[MAX_REGIONFALL_COUNT];
	int 				iMaskRegionCount;			//代表屏蔽区域列表，区域个数0~8
	vca_TPolygonEx		tMaskRegion[MAX_REGIONFALL_COUNT];
} XmlFallingObject, *pXmlFallingObject;

#define MAX_MIXAUDIOSRC_NUM							32
#define MAX_MIXAUDIOCONFIG_NUM						8

typedef struct tagAudioSrcInfo
{
	char				cAudioSrcName[LEN_32];		//混音输入源名称 “local, remoteAll, remote1, remote2, remote3, remote4, micAll, mic1, mic3, mic5, mic7, proof, exp1, exp2, exp3, exp4, expOthr”
	int					iEnabled;					//是否使能 0-关闭 1-开启
} AudioSrcInfo, *pAudioSrcInfo;

typedef struct tagMixAudioConfig
{
	int					iAudioSrcNum;				//混音输入源数量
	char				cOutPutName[LEN_32];		//混音输出名称 “local, hard, localChn, vcChn, hdmi1, hdmi2”
	AudioSrcInfo		tAudioSrcName[MAX_MIXAUDIOSRC_NUM];
} MixAudioConfig, *pMixAudioConfig;

typedef struct tagXmlMixAudioConfigList
{
	int					iAudioConfigNum;			//列表成员个数
	MixAudioConfig		tMixAudioConfig[MAX_MIXAUDIOCONFIG_NUM];
} XmlMixAudioConfigList, *pXmlMixAudioConfigList;

#define MAX_REGION_COUNT_CLOTH	8

typedef struct tagValidRgPoliceCloth
{
	int					iRegionNo;					//代表检测区域编号，区域编号范围1~8
	int					iRegionEnabled;				//代表检测区域是否有效，true：有效、false：无效
	vca_TPolygonEx		tRegionCoord;				//代表区域坐标列表，坐标个数3~32
} ValidRgPoliceCloth, *pValidRgPoliceCloth;

typedef struct tagXmlPoliceCloth
{
	int					iRuleId;					//规则号 0~15
	int					iChannelNo;					//通道号
	int					iSceneId;					//场景ID
	int					iModel;						//模式 0-nvr 1-ipc
	int					iEnabled;					//是否有效，true：启用、false：不启用
	int					iSensitivity;				//灵敏度：0~100   (默认值:50)
	int					iDisPlayRule;				//显示报警规则：true-显示，false-不显示
	int					iDisPlayStat;				//显示报警计数：true-显示，false-不显示
	int					iDisPlayTarget;				//显示目标：true-显示，false-不显示
	int					iMinSize;					//最小尺寸1-50(默认2) 百分比
	int					iMaxSize;					//最大尺寸8-100 (默认25) 百分比
	int					iValidRegionCount;			//代表检测区域列表，区域个数1~8
	ValidRgPoliceCloth	tValidRegion[MAX_REGION_COUNT_CLOTH];
	int					iMaskRegionCount;			//代表屏蔽区域列表，区域个数0~8
	vca_TPolygonEx		tMaskRegion[MAX_REGION_COUNT_CLOTH];
} XmlPoliceCloth, *pXmlPoliceCloth;

typedef struct tagValidRgPrisonerCloth
{
	int					iRegionNo;					//代表检测区域编号，区域编号范围1~8
	int					iRegionEnabled;				//代表检测区域是否有效，true：有效、false：无效
	vca_TPolygonEx		tRegionCoord;				//代表区域坐标列表，坐标个数3~32
} ValidRgPrisonerCloth, *pValidRgPrisonerCloth;

typedef struct tagXmlPrisonerCloth
{
	int						iRuleId;				//规则号 0~15
	int						iChannelNo;				//通道号
	int						iSceneId;				//场景ID
	int						iModel;					//模式 0-nvr 1-ipc
	int						iEnabled;				//是否有效，true：启用、false：不启用
	int						iSensitivity;			//灵敏度：0~100   (默认值:50)
	int						iDisPlayRule;			//显示报警规则：true-显示，false-不显示
	int						iDisPlayStat;			//显示报警计数：true-显示，false-不显示
	int						iDisPlayTarget;			//显示目标：true-显示，false-不显示
	int						iMinSize;				//最小尺寸1-50(默认2) 百分比
	int						iMaxSize;				//最大尺寸8-100 (默认25) 百分比
	int						iValidRegionCount;		//代表检测区域列表，区域个数1~8
	ValidRgPrisonerCloth	tValidRegion[MAX_REGION_COUNT_CLOTH];
	int						iMaskRegionCount;		//代表屏蔽区域列表，区域个数0~8
	vca_TPolygonEx			tMaskRegion[MAX_REGION_COUNT_CLOTH];
} XmlPrisonerCloth, *pXmlPrisonerCloth;

#define MAX_REGION_COUNT_QUEUE_STAND	8

typedef struct tagValidRgQueueStand
{
	int					iRegionNo;					//代表检测区域编号，区域编号范围1~8
	int					iRegionEnabled;				//代表检测区域是否有效，true：有效、false：无效
	vca_TPolygonEx		tRegionCoord;				//代表区域坐标列表，坐标个数3~32
} ValidRgQueueStand, *pValidRgQueueStand;

typedef struct tagXmlQueueStand
{
	int					iRuleId;					//规则号 0~15
	int					iChannelNo;					//通道号
	int					iSceneId;					//场景ID
	int					iModel;						//模式 0-nvr 1-ipc
	int					iEnabled;					//是否有效，true：启用、false：不启用
	int					iSensitivity;				//灵敏度：0~100   (默认值:50)
	int					iDisPlayRule;				//显示报警规则：true-显示，false-不显示
	int					iDisPlayStat;				//显示报警计数：true-显示，false-不显示
	int					iDisPlayTarget;				//显示目标：true-显示，false-不显示
	int					iMinSize;					//最小尺寸1-50(默认2) 百分比
	int					iMaxSize;					//最大尺寸8-100 (默认25) 百分比
	int					iAlarmTime;					//最小报警间隔时间：0-7200秒 默认30
	int					iValidRegionCount;			//代表检测区域列表，区域个数1~8
	ValidRgQueueStand	tValidRegion[MAX_REGION_COUNT_QUEUE_STAND];
	int					iMaskRegionCount;			//代表屏蔽区域列表，区域个数0~8
	vca_TPolygonEx		tMaskRegion[MAX_REGION_COUNT_QUEUE_STAND];
} XmlQueueStand, *pXmlQueueStand;

typedef struct tagIrriDataParam
{
	int iIrriDataType;		//水利数据类型
	int	iIrriDataEnable;	//水利数据上报使能1	0-不使能，1-使能
	int	iIrriDataInterval;	//水利数据上报间隔1	单位：秒，范围：60～86400秒
    int iCorrectionValueK;  //水质数据校正值K
    int iCorrectionValueB;  //水质数据校正值B
}IrriDataParam,pIrriDataParam;

#define MAX_IRRIDATAPARAM_NUM   128
#define MAX_IRRIDATAPARAM_PAGESIZE   20
typedef struct tagIrriDataUploadParam
{
	int	iDevType;		//设备类型	0-IPC，1-NVR
	int	iSceneID;		//场景ID(预留)	0～15
	int	iRuleID;		//规则ID(预留)	0～15
	int iTotalNum;
	int iPageSize;      //分页大小，最大20
	int iIrriDataParamSize;
	IrriDataParam tIrriDataParam[MAX_IRRIDATAPARAM_NUM];
} IrriDataUploadParam, *pIrriDataUploadParam;

typedef struct tagSpecLightParam
{
	int	iDevType;		//设备类型	0-IPC，1-NVR
	int	iSceneID;		//场景ID(预留)	0～15
	int	iRuleID;		//规则ID(预留)	0～15
	int	iCtrlType;		//控制方式	0-自动，1-手动
	int	iBrightness;	//光谱灯源亮度	范围：0-100，当iCtrlType=1时有效
} SpecLightParam, *pSpecLightParam;

typedef struct tagWaterQualityWiper
{
	int	iDevType;		//设备类型	0-IPC，1-NVR
	int	iSceneID;		//场景ID(预留)	0～15
	int	iRuleID;		//规则ID(预留)	0～15
	int	iTimeTurn;		//定时清洁	0-关闭，1-开启
	int	iInterval;		//清洁间隔	单位：秒，范围：60～86400秒
} WaterQualityWiper, *pWaterQualityWiper;

typedef struct tagWaterElecFence
{
	int	iEnable;		//电子围栏	0-关闭，1-开启
	int	iRadius;		//围栏半径	单位：毫米，范围：1～10000000
	int	iLongitude;		//经度（东、西）	0-E ，1-W   
	int	iLonDegree;		//经度-度	度0-180
	int	iLonMinute;		//经度-分	分0-59
	int	iLonSecond;		//经度-秒	秒0-59.99*100
	int	iLatitude;		//纬度（南、北）	0-S ，1-N
	int	iLatDegree;		//纬度-度	度0-90
	int	iLatMinute;		//纬度-分	分0-59
	int	iLatSecond;		//纬度-秒	秒0-59.99*100
} WaterElecFence, *pWaterElecFence;

typedef struct tagWaterQualityAdjust
{
    int iAdjustType;        //光谱校正类型	0-保留 1-原厂空气校正调试 2-原厂纯净水校正调试 3-现场空气校正调试 4-现场纯净水校正调试
    int iResult;            //返回结果	0-成功(开始光谱校正)，非0表示失败
} WaterQualityAdjust, *pWaterQualityAdjust;

typedef struct tagWaterQualityAdjustInfo
{
    int iAdjustType;              //光谱校正类型	0-保留 1-原厂空气校正调试 2-原厂纯净水校正调试 3-现场空气校正调试 4-现场纯净水校正调试
    int iAdjustStatus;            //光谱校正状态	0-未校正调试 1-已校正调试
    int iAdjustTime;              //光谱校正时间	当iAdjustType不等于0时有效，UTC时间(无时区)
    int iAdjustTemperature;       //光谱校正时的温度	当iAdjustType不等于0时有效，单位：℃，范围：-10.0-60.0，取值时乘以10
} WaterQualityAdjustInfo, *pWaterQualityAdjustInfo;


typedef struct tagIrriAlgExtInfo
{
	char cExtInfo[LEN_1024]; //算法扩展信息	最大1024字节
} IrriAlgExtInfo, *pIrriAlgExtInfo;

typedef struct tagXmlPlateInfoQueryCondition
{
	int				iChannelNo;         
	int				iPageNo;			//页序号 Page number
	int				iPageSize;			//页大小 Page size 0<iPageSize<=32
	int				iPlateLibIndex;		//车牌库Id序号，从1开始, 范围1-128, 不检索-1 License plate library Id number, starting with 1, range 1-128, do not search -1
	char			cPlateNum[LEN_32];	//车牌号 license plate number
}XmlPlateInfoQueryCondition,*pXmlPlateInfoQueryCondition;

typedef struct tagXmlPlateInfoQueryResult
{
	int			iIndex;				//页码内序号 Page index
	int			iPlateLibIndex;		//车牌库Id序号 License plate library Id number
	long long 	llPlateInfoIndex;	//车牌信息Id序号 License plate information Id number
	char		cPlateNum[LEN_32];	//车牌号 license plate number
}XmlPlateInfoQueryResult,*pXmlPlateInfoQueryResult;

#define MAX_PLATE_PAGESIZE 32
typedef struct tagXmlPlateInfoQueryResultPage
{
	int						iTotalCount;	
	int						iPageSize;		
	XmlPlateInfoQueryResult tResultInfo[MAX_PLATE_PAGESIZE];
}XmlPlateInfoQueryResultPage,*pXmlPlateInfoQueryResultPage;

typedef struct tagXmlPlateInfoParas
{
	int		  iChannelNo;
	int		  iPlateLibIndex;
	long long llPlateInfoIndex;	//llPlateInfoIndex=0表示添加车牌信息，llPlateInfoIndex>0表示修改车牌信息
	char	  cPlateNum[LEN_32];
}XmlPlateInfoParas,*pXmlPlateInfoParas;

typedef struct tagXmlPlateInfoResult
{
	long long	llPlateInfoIndex;	//>=1:车牌信息Id序号License plate information Id number, -1:车牌号重复Duplicate license plate, -2:车牌数量已满The number of license plates is full
}XmlPlateInfoResult,*pXmlPlateInfoResult;

#define MAX_PLATE_LIB_NUM	128
typedef struct tagXmlPlateLibItem
{
	int					iPlateLibIndex;				// 车牌库Id序号，从1开始， 范围1-128
	char				cPlateLibName[LEN_64];		// 车牌库名称，最大63字节
	char				cPlateLibRemark[LEN_128];	// 车牌库描述信息，127个字符
} XmlPlateLibItem, *pXmlPlateLibItem;
typedef struct tagXmlPlateLibInfo
{
	int					iChannel;					// 通道号
	int					iMaxNum;					// 支持的最大车牌数
	int					iUsedNum;					// 已使用车牌数
	int					iMaxLibNum;					// 支持的最大车牌库数
	int					iLibUsedNum;				// 已使用车牌库数
	XmlPlateLibItem		tPlateLibItemList[MAX_PLATE_LIB_NUM];
} XmlPlateLibInfo, *pXmlPlateLibInfo;

typedef struct tagXmlPositionColorIn
{
	int		iChannelNo;
	int		iScreenX;	//视频图像横坐标，范围0-9999
	int		iScreenY;	//视频图像纵坐标，范围0-9999
}XmlPositionColorIn,*pXmlPositionColorIn;

typedef struct tagXmlPositionColorResult
{
	char	cColor[LEN_128];		//目标颜色,red/yellow/blue/green/black/white/gray/purple
}XmlPositionColorResult,*pXmlPositionColorResult;

#define MAX_STRATEGY_REGION_POINT	8
typedef struct tagXmlStrategyConditionRule
{
	int		iIndex;										//条件编号，范围1~8
	char	cType[LEN_32];								//people-人员，car-车辆，colour-颜色，audioLost-音频丢失，audioAbnormal-音频异常
	char	cColor[LEN_128];								//目标颜色，red/yellow/blue/green/black/white/gray/purple
	int		iEvent;										//目标事件，0-进入，1-离开，2-滞留
	int		iTime;										//滞留时间，范围1-60，单位秒
	int		iSensitivity;								//灵敏度，范围0-100
	int		iRegionNum;									//区域坐标点个数
	TPoint	tRegionPoint[MAX_STRATEGY_REGION_POINT];	//区域横坐标和纵坐标，万分比
	int		iTwoWayAlarm;								//双向报警，1-生效，0-不生效
}XmlStrategyConditionRule,*pXmlStrategyConditionRule;

#define MAX_STRATEGY_NUM	8
typedef struct tagXmlStrategyCondition
{
	int							iChannelNo;
	int							iSceneID;
	int							iStrategyNum;
	XmlStrategyConditionRule	tRule[MAX_STRATEGY_NUM];
}XmlStrategyCondition,*pXmlStrategyCondition;

typedef struct tagXmlStrategyInputCondition
{
	int		iIndex;				//策略编号，范围1~4
	int		iEnabled;			//代表是否有效，1-启用，0-不启用
	char	cRuleName[LEN_32];	//规则名
	char	cCondition[LEN_32];	//代表条件列表，当有多个条件时用“，”分隔
}XmlStrategyInputCondition,*pXmlStrategyInputCondition;

#define MAX_CONDITION_NUM	4
typedef struct tagXmlStrategyInput
{
	int							iChannelNo;
	int							iSceneID;
	int							iMaxRank;		//主动策略最大组合条件个数
	int							iStrategyNum;
	XmlStrategyInputCondition	tCondition[MAX_CONDITION_NUM];
}XmlStrategyInput,*pXmlStrategyInput;

typedef struct tagXmlPlateLibParas
{
	int					iChannel;					// 通道号
	int					iIndex;						// 车牌库Id序号，从1开始， 范围1-128
	char				cPlateLibName[LEN_64];		// 车牌库名称，最大63字节
	char				cPlateLibRemark[LEN_128];	// 车牌库描述信息，127个字符
} XmlPlateLibParas, *pXmlPlateLibParas;

typedef struct tagXmlPlateDeleteInfo
{
	int					iChannel;					// 通道号
	int					iIndex;						// 车牌库Id序号，从1开始， 范围1-128
} XmlPlateDeleteInfo, *pXmlPlateDeleteInfo;

typedef struct tagVcaOfflineQueryData
{
	int					iBeginTime;					// 开始时间	从1970-1-1经过的秒数。UTC时间
	int					iEndTime;					// 结束时间	从1970-1-1经过的秒数。UTC时间（每次最多查询一天）
	int					iTotal;						// 总条数	开始时间和结束时间内查询到的总条数
	int					iFlag;						// 开始结束标志	0开始 1结束
} VcaOfflineQueryData, *pVcaOfflineQueryData;

typedef struct tagXmlTempDetectBaseParam
{
	int		iEnable;
	int		iShowTempBar;
	int		iShowMaxTemp;
	int		iShowMinTemp;
	int		iShowAverageTemp;
	int		iShowVISTempInfo;
	int		iShowTHMTempInfo;
	int		iEnableSunReflectFilter;
	int		iEnableSunReflectFilterTip;
	int		iSunReflectFilterSensitivity;
	int		iTempUnit;
	int		iDataRefreshInterval;
	int		iTempInfoShowPosition;
	int		iTempDetectMode;
	char	cTempDetectRange[LEN_64];
	int		iAlarmTimeInterval;
}XmlTempDetectBaseParam,*pXmlTempDetectBaseParam;

typedef struct tagClimbWallAlertPara
{
    int     iChannelNo;         //通道号
    int     iSceneID;           //场景ID
    int     iEventNum;		    //翻墙警戒编号
    int     iSensitivity;        //灵敏度
    int     iValid;              //此事件检测是否启用
    int     iDisplayRule;        //规则是否显示
    int     iDisplayStat;        // 报警计数统计是否显示
    int     iDisplayTarget;      //目标框是否显示
    int     iColor;              //区域颜色
    int     iAlarmColor;         //报警时区域的颜色
    int     iMiniSize;           //最小尺寸
    int     iMaxSize;            //最大尺寸
}ClimbWallAlertPara,*pClimbWallAlertPara;

typedef struct tagTempRangeInfo
{
	int			iTempUnit;
	char		cHighGaintempRange[LEN_32];
	char		cLowGaintempRange[LEN_32];
}TempRangeInfo,*pTempRangeInfo;

#define TEMP_RANGE_NUM_MAX 4
typedef struct tagXmlTempRangeMapList
{
	int				iChannelNo;
	int				iTempUnitNum;
	TempRangeInfo	tTempRangeInfo[TEMP_RANGE_NUM_MAX];
}XmlTempRangeMapList,*pXmlTempRangeMapList;

typedef struct tagXmlFaceTrackInfo
{
	 int			iEnabled;
}XmlFaceTrackInfo,*pXmlFaceTrackInfo;

typedef struct tagWaterColorPara
{
    int     iChannelNo;             //通道号
    int     iSceneID;               //场景ID
    int     iRuleID;                //规则ID
    int     iValid;                 //此事件检测是否有效
    int     iColor;		            // 区域颜色 1：红色 2：绿色 3：黄色 4：蓝色 5：紫色 6：青色 7：黑色 8：白色   （默认：绿色）
    int     iAlarmColor;            //报警时区域的颜色 1：红色 2：绿色 3：黄色 4：蓝色 5：紫色 6：青色 7：黑色 8：白色   （默认：红色）
    int     iDisplayRule;           //规则是否显示
    int     iDisplayStat;           // 报警计数统计是否显示
    int     iAlarmColorEnable;      //使能的报警颜色
    int     iSensitivity;           //灵敏度
    int     iSizeMin;               //检测目标最小尺寸
    int     iSizeMax;               //检测目标最大尺寸
    vca_TPolygonEx     stDetectArea;        //检测区域
}WaterColorPara,*pWaterColorPara;


typedef struct tagRecupLoadPath
{	
	int		iSize;
	int     iRetCode;               //错误码 0：成功，<0:失败
	char	cPath[LEN_256];			//上传路径
}RecupLoadPath,*pRecupLoadPath;

typedef struct tagRecupLoadAddParam
{	
	int				iSize;
	int				iChannelNo;             //0~n-1,主码流录像；n~2n-1,副码流录像
	int				iFileSize;				//大小限制：0~1048576000（1000M） 单位：字节
	NVS_FILE_TIME	tStartTime; 			//录像开始时间
	NVS_FILE_TIME	tStopTime; 				//录像结束时间
	char			cFileFullPath[LEN_256];	//ftp上传完整路径 UTF-8格式
	char			cFileMd5[LEN_64];		//录像文件的MD5
}RecupLoadAddParam,*pRecupLoadAddParam;

typedef struct tagRecupLoadAddResult
{	
	int				iSize;
	int				iChannelNo;           //0~n-1,主码流录像；n~2n-1,副码流录像
	int				iRetCode;             //0：成功; -1000：转存队列繁忙，稍后重试; -1001:参数不合法; -1002：文件不存在; -1003：文件超出1G; -1004:无效文件; -1005：MD5校验失败;
}RecupLoadAddResult,*pRecupLoadAddResult;

typedef struct tagRecupLoadAddProgress
{	
	int		iSize;
	char	cFileFullPath[LEN_256];       //ftp上传完整路径 UTF-8格式
}RecupLoadAddProgress,*pRecupLoadAddProgress;

typedef struct tagRecupLoadAddProgressResult
{	
	int		iSize;
	int     iProgress;               //回补录像文件进度百分比0～100, -1000：文件上传失败
}RecupLoadAddProgressResult,*pRecupLoadAddProgressResult;

typedef struct tagRecupLoadAddDelParam
{	
	int				iSize;
	int				iChannelNo;				//0~n-1,主码流录像；n~2n-1,副码流录像
	NVS_FILE_TIME	tStartTime; 			//录像开始时间
	NVS_FILE_TIME	tStopTime; 				//录像结束时间
	char			cFileName[LEN_256];		//设备上的文件名称
}RecupLoadAddDelParam,*pRecupLoadAddDelParam;

typedef struct tagRecupLoadAddDelResult
{	
	int				iSize;
	int				iChannelNo;            //0~n-1,主码流录像；n~2n-1,副码流录像
	int				iRetCode;              //0：成功，<0:失败
}RecupLoadAddDelResult,*pRecupLoadAddDelResult;

#define VIDEO_MOSAIC_AREA			4	//马赛克隐私遮挡区域数
typedef struct tagMosaicAreaInfo 
{
	int iAreaNo;	//区域编号
	int iEnable;	//0--不使能，1--使能
	int iLevel;		//马赛克等级 0~100
	int iLeft;		//画面left起始坐标(使用万分比表示)
	int iTop;		//画面top起始坐标(使用万分比表示)
	int iWidth;		//画面宽度(使用万分比表示)
	int iHeight;	//画面高度(使用万分比表示)
}MosaicAreaInfo, *pMosaicAreaInfo;

#define MAX_VO_DEV_NUM 4
typedef struct tagVCMosaicGet
{
	int iVoDevId;//输出设备号, 0-保留（按VC处理），1-VGA/HDMI0, 2-HDMI1, 3-VC
	int iAreaNum;//区域个数, 0~4
	MosaicAreaInfo	stAreaInfo[VIDEO_MOSAIC_AREA];
}VCMosaicParaGet,*pVCMosaicParaGet;

typedef struct tagVCMosaicSet
{
	int iVoDevId;	//输出设备号, 0-保留（按VC处理），1-VGA/HDMI0, 2-HDMI1, 3-VC
	MosaicAreaInfo	stAreaInfo;
}VCMosaicParaSet,*pVCMosaicParaSet;

//移动侦测之车形报警参数结构体
typedef struct tagMotionDetectCarParam
{
	int	iEnable;				//使能
}MotionDetectCarParam, *pMotionDetectCarParam;

typedef struct tagCheckDiskFile
{
	int iSize;							//结构体大小
	char cFilePath[LEN_256];			//文件路径(utf-8格式)
	unsigned long long ullFileSize;		//文件大小(Byte)
	char cFileMD5[LEN_64];				//文件MD5字符串(不区分大小写)
}CheckDiskFile, *pDiskFileInfoCheck;

typedef struct tagCheckDiskFileResponse
{
	int iSize;							//结构体大小
	char cFilePath[LEN_256];			//文件路径(utf-8格式)
	int iResult;						//0:正在校验,进度参考iProgress;1:成功;-1:文件长度错误;-2:MD5校验码错误;-3:超过最大校验任务数量;-4:文件不存在
}CheckDiskFileResponse, *pCheckDiskFileResponse;

typedef struct tagCheckDiskFileProgress
{
	int iSize;							//结构体大小
	char cFilePath[LEN_256];			//文件路径(utf-8格式)
	int iResult;						//0:正在校验,进度参考iProgress;1:成功;-1:文件长度错误;-2:MD5校验码错误;-3:超过最大校验任务数量;-4:文件不存在
	int iProgress;						//检验进度取值范围:[0-100]
}CheckDiskFileProgress, *pCheckDiskFileProgress;

#define STATIC_MOSAIC_NUM_MAX	8
#define MOSAIC_POINTS_NUM_MAX	8
typedef struct tagStaticMosaicPara
{
	int iID;											//区域ID
	int iPointNum;										//实际的点个数
	TPoint tPoints[MOSAIC_POINTS_NUM_MAX];				//马赛克的点信息
}StaticMosaicPara;

typedef struct tagXmlStaticMosaicParaList
{
	int iMosaicNum;										//实际的马赛克个数
	StaticMosaicPara tMosaics[STATIC_MOSAIC_NUM_MAX];	//所有马赛克信息
}XmlStaticMosaicParaList;


typedef struct tagEVACapacityPara
{
    int     iCoefficient;         //折算系数
    int     iEvacylinder;           //蒸发筒口径
    int     iOverflow;		    //溢流筒口径
    int     iSnapEnable;        //抓拍使能
    int     iEvapInterval;          //蒸发量上报间隔	单位：分钟，范围：1-1440
    int     iInjectionStart;          //蒸发池开始注水的水位阈值	范围:1～10000000，单位:0.1毫米(mm)，开始注水的水位阈值要低于停止注水的水位阈值(iInjectionEnd)
    int     iInjectionEnd;          //蒸发池停止注水的水位阈值	范围:1～10000000，单位:0.1毫米(mm)，停止注水的水位阈值要高于开始注水的水位阈值(iInjectionStart)
    int     iInjectionTimeout;          //蒸发池注水超时时长	单位：分钟，范围：1-30，用于异常报警“35、蒸发池注水异常”
    int     iEvapExceptionThreshold;          //蒸发量异常阈值	范围:1～10000000，单位:0.1毫米(mm)，用于异常报警“37、蒸发量数据突变异常”
    int     iStartTime;             //蒸发量统计开始时间	"bit24-bit31:为1时,表示统计开始时间有效,为0时无效  bit16-bit23:开始小时   bit15-bit8:开始分钟   bit7-bit0:开始秒"
    int     iEvapFilterEnable;          //蒸发量消抖过滤使能	0-保留，1-使能，2-不使能
    int     iEvapFilterLevel;           //蒸发量消抖过滤等级	0-保留，1-5级
    int     iEvapFilterTime;            //蒸发量消抖过滤时间	0-保留，1-300，单位：毫秒

}EVACapacityPara,*pEVACapacityPara;

typedef struct tagCheckVodFile
{
	char			   cFileName[LEN_256];		//参数可复使用
	//1.按时间段下载时，此参数的内容为：“TM:开始时间：结束时间：频道号”时间为整数，从1970年1月1日00:00:00开始以秒为单位计算，本地时间不是UTC时间；范围从0到n-1的信道号指示主流记录；N~2N-1表示子流记录。
	//2.按文件下载时，此参数的内容为录制文件名。
	
	
	unsigned long long ullFileSize;				//文件大小(Byte)
	int                iVerifyMode;				//校验方法	0：预留；1：间隔MD5校验
	char			   cVerifyCode[LEN_256];	//校验码:最大256字符
												//iVerifyMode=1时，表示间隔MD5校验值，固定32字符，不区分大小写。
}CheckVodFile, *pCheckVodFile;

typedef struct tagCheckVodTimeSpan
{
	int				   iChannelNO;			//Channel number
	int				   iStreamNo;			//Stream NO
	NVS_FILE_TIME	   tTimeBegin;			//Start time
	NVS_FILE_TIME	   tTimeEnd;			//End time

	unsigned long long ullFileSize;				//文件大小(Byte)
	int                iVerifyMode;				//校验方法	0：预留；1：间隔MD5校验
	char			   cVerifyCode[LEN_256];	//校验码:最大256字符
	//iVerifyMode=1时，表示间隔MD5校验值，固定32字符，不区分大小写。
}CheckVodTimeSpan, *pCheckVodTimeSpan;

typedef struct tagCheckVodFileResponse
{
	int				   iType;                       //0:按文件下载时校验结果  1：按时间段下载时校验结果
	char			   cFileName[LEN_256];			//按文件下载时，该参数内容为录像文件名，按时间段下载时，该参数内容为："TM:起始时间:结束时间:通道号"

	int				   iChannelNo;				//按时间段下载时Channel number
	int				   iStreamNo;				//按时间段下载时Stream NO
	NVS_FILE_TIME	   tTimeBegin;				//按时间段下载时Start time
	NVS_FILE_TIME	   tTimeEnd;				//按时间段下载时End time
										
	int iResult;								//校验结果 0，正在校验，进度见iProgress；
												//1，成功；-1，长度不正确；-2，校验码不正确；-3，超过最大校验任务数量（设备端最大支持5个任务同时并行，需要重新开始请求）；-4，文件不存在；-5，不支持的校验方法
	int iProgress;								//检验进度取值范围:[0-100]
}CheckVodFileResponse, *pCheckVodFileResponse;

#define MAX_DZTRANS_PARA_COUNT		32
typedef struct tagDzTransparentPara
{
	int		iSize;											//输出参数：结构体大小，sizeof(DzTransparentPara)， 异步回调消息使用
	int		iChanNo;										//输入参数：通道号，[0 ~ 通道数-1]-转发给前端ipc，0x7fffffff-nvr本地处理 
	int		iParaType;										//输入参数：类型由平台软件和嵌入式设备协商制定
	int		iParaCount;										//输入输出参数：取值范围[0, MAX_DZTRANS_PARA_COUNT]
	char	cDzParam[MAX_DZTRANS_PARA_COUNT][LEN_128];		//输入输出参数：由平台软件和嵌入式设备协商制定
} DzTransparentPara, *pDzTransparentPara;

typedef struct tagFaceLibClearPara
{
	int			iSize;					//结构体大小
	int			iChan;					//通道号
	int			iLibKey;				//有脸库的key值(iLibKey>0)
	char		cLibUUID[LEN_UUID];		//人脸库在平台的ID(不为空以本字段为准)
}FaceLibClearPara, *pFaceLibClearPara;


typedef struct tagNetCheckPara
{
    char        cCheckAddr[LEN_64];     //检测地址	支持IPV4，IPV6，域名;长度限制63个字符；
    int         iCheckPackSize;         //ping包大小	64-65500
    int         iCheckPackNum;          //ping包次数	1-16
}NetCheckPara,*pNetCheckPara;

typedef struct tagNetCheckResult
{
    int         iReachable;             //网络是否可达状态	0:可达,1:不可达
    int         iDelayTime;             //延时时间	单位毫秒（ms）
    int         iLossRate;              //丢包率	0-100，若值为50表示丢包率50%；若不可达，丢包率为100；
}NetCheckResult,*pNetCheckResult;

typedef struct tagQueryDiskRecordPara
{
	int iSize; 	// 结构体大小
	int iDiskNo; // 磁盘编号
}QueryDiskRecordPara, *pQueryDiskRecordPara;

//设备每次最多上报20条给SDK，SDK接收完再返回给应用层
#define ONCE_REPORT_COUNT_MAX 20
typedef struct tagQueryDiskRecordParaResult
{
	int 	iSize;	// 结构体大小
	int 	iDiskNo;	// 磁盘编号
	int 	iTotal;	// 总条数 正确查询：iTotal>=0
	/*-1：其他错误
	-2：磁盘不在
	-3，磁盘读写错误
	-4：磁盘未格式化
	-5：磁盘号错误*/

	int 	iCurrentCount; // 本次上报个数
	char 	cDiskRecordData[LEN_512][LEN_256];  // 通道数据，最大支持1024条数据，每条数据最大256字节
	/*
	格式：“通道号,起始时间,结束时间”
	通道号：0~n-1,主码流录像查询；n~2n-1,副码流录像查询
	起始时间：UTC时间
	结束时间：UTC时间
	*/
}QueryDiskRecordParaResult, *pQueryDiskRecordParaResult;

#define WIFI_PWDSNUM_0_ADAPTATIVE		0	//密码序号自适应
#define	WIFI_PWDSNUM_1_TKIP				1	//密码序号TKIP
#define	WIFI_PWDSNUM_2_AES				2	//密码序号AES
typedef struct tagOnceCfgWifiPara
{
	char	cEssId[LEN_64];				//路由器标识，路由器热点名称, 实际设备使用限制32个字符
	char	cEncryption[LEN_32];		//加密方式，none-开放网络,WEP,WPA-PSK,WPA2-PSK,auto-自适应，实际设备使用限制16个字符
	char	cWifiKeyType[LEN_32];		//密码格式类型, hex,ascii,auto-自适应，实际设备长度限制16个字符
	char	cWifiConPwd[LEN_256];		//路由器连接密码，实际设备使用限制128个字符
	int		iWifiKeyNum;				//密码序号：0--自适应，1--TKIP，2--AES
} OnceCfgWifiPara, *pOnceCfgWifiPara;

#define ONCE_CFGWIFI_RET_RESERVE			0	//一键配置WIFI结果预留
#define ONCE_CFGWIFI_RET_SUCCESS			1	//一键配置WIFI结果成功
#define ONCE_CFGWIFI_RET_SETPARAFAIL		2	//一键配置WIFI结果设置WIFI参数失败
#define ONCE_CFGWIFI_RET_MODIFYPWDFAIL		3	//一键配置WIFI结果修改密码失败
#define ONCE_CFGWIFI_RET_STARTP2PFALI		4	//一键配置WIFI结果启动P2P失败
#define ONCE_CFGWIFI_RET_NOTSUPPORT			5	//一键配置WIFI结果不支持当前功能
typedef struct tagOnceCfgWifiResult
{
	int		iResult;					//一键配置WIFI参数结果回码：0-预留，1-成功，2-设置WIFI参数失败,3-启动P2P失败，4-不支持当前功能，小于0-协议处理失败

} OnceCfgWifiResult, *pOnceCfgWifiResult;

typedef struct tagUtcTimePara
{
	int		iSize; 		//结构体大小
	int		iTimeZone;	//当前正在使用的时区
						//【整点时区】88～113，即在真实时区上+100，比如格林威治GMT+0时区，则发100，中国是GMT+8时区，则发108；但呈现给用户的范围应该是-12～+13
						//【非整点时区】8800～11400，即在真实时区的hhmm表示法基础上+10000，比如GMT+05:45时区，则发10545(=10000+0545)；比如GMT-6:30时区，则发9370(=10000-0630)
	int		iUtcTime;	//当前时间的UTC时间，从1970-1-1经过的秒数-UTC时间
	int		iTimeMs;	//当前时间的毫秒位值，精确到毫秒，取值范围0～999
} UtcTimePara, *pUtcTimePara;

//磁盘扩展信息							
typedef struct tagDiskInfoRealTime
{
	int				iSize;
	int				iDiskNo;//磁盘1编号
	char			cDiskName[LEN_32];//磁盘1名称
}DiskInfoRealTime, *pDiskInfoRealTime;

typedef struct tagDiskInfosRealTimeArr
{
	int					iSize;
	int					iDiskNum;
	DiskInfoRealTime		tDiskInfos[DISK_NUM_MAX]; 
}DiskInfosRealTimeArr, *pDiskInfosRealTimeArr;

//双用户认证结构体（ZXYHDZ）
typedef struct tagDualUsersAuthentication
{
	char cUserName[LEN_16];//用户名
	char cPassword[LEN_16];//密码
	int iAuthCount;//认证次数
	int iSourceCode;//第一次认证成功后，回码中带的序号。当iIdentifyCount为1时，默认传0。当iIdentifyCount时，序号范围为10000~30000。
}DualUsersAuthentication, *pDualUsersAuthentication;

typedef struct tagDualUsersAuthenticationResult
{
	int iAuthCount;// 双用户认证，1：第一次认证 2：第二次认证 其他：无效值
	int iResult;//0：成功 1：用户名/密码错误 2：使用同一个用户名登录 3：没有使用admin用户登录 4：认证超时 5：未知错误
	int iSourceCode;//第一次认证成功后，回码中带的序号。当iIdentifyCount为1时，默认传0。当iIdentifyCount时，序号范围为10000~30000。
	char cSessionID[LEN_32];//VOD点播，下载录像/图片或VOD时，CONNECT使用，长度不超过32字节，包括\0
}DualUsersAuthenticationResult, *pDualUsersAuthenticationResult;

typedef struct tagDualUsersAuthSession
{
	int iSize;
	char cSessionID[LEN_32];//VOD点播，下载录像/图片或VOD时，CONNECT使用，长度不超过32字节，包括\0
}DualUsersAuthSession, *pDualUsersAuthSession;

typedef struct tagElecPTZFPosResult
{
	int		iChannelNo;		//通道号
	int		iType;			//数据设置/获取类型，0-PT按照角度，ZF按照电机步数获取	
	int		iReturn;		//操作返回值，0-成功,-1失败
} ElecPTZFPosResult, *pElecPTZFPosResult;

typedef struct tagElecPTZFPosInfo
{
	int		iChannelNo;		//通道号
	int		iType;			//获取类型,	数据获取类型，0-PT按照角度，ZF按照电机步数获取
	int		iPosP;			//左右位置,  0~36000  对应0-360度
	int		iPosT;			//上下位置,  1000~19000  对应-90-90度
	int		iPosZ;			//变倍步数,  取值范围：0~10000			
	int		iPosF;			//聚焦步数,  取值范围：0~10000
} ElecPTZFPosInfo, *pElecPTZFPosInfo;

#define DEVICE_LOGIN_REPORT			0		//设备登录上报
#define LIGHTWEIGHT_REALTIME_GET	1		//轻量级实时获取
typedef struct tagIndustryPlatformTagInfo
{
	int             				iSize;							//结构体大小
	int								iGetMode;						//获取方式，0-设备登录上报，从SDK内存获取 1-轻量级协议实时从设备端获取
	char             				cModuleID[128];				//模块ID
}IndustryPlatformTagInfo, *pIndustryPlatformTagInfo;

//设备主动回补录像错误码定义(负数)
#define DEV_RECSUPP_ERRCODE_10		-10		//cMissionID与当前任务不匹配
#define DEV_RECSUPP_ERRCODE_1000	-1000	//cMissionID为空
#define DEV_RECSUPP_ERRCODE_1001	-1001	//协议 iNvrChannelNo 字段错误
#define DEV_RECSUPP_ERRCODE_1002	-1002	//协议 llStartTime小于llStopTime 或 llStartTime与llStopTime间隔大于3600或2678400秒
#define DEV_RECSUPP_ERRCODE_1003	-1003	//无可用磁盘
#define DEV_RECSUPP_ERRCODE_1004	-1004	//刻录中，稍后重试
#define DEV_RECSUPP_ERRCODE_1005	-1005	//重建索引中，稍后重试
#define DEV_RECSUPP_ERRCODE_1006	-1006	//热备上传中，稍后重试
#define DEV_RECSUPP_ERRCODE_1007	-1007	//回补进行中，稍后重试
#define DEV_RECSUPP_ERRCODE_1008	-1008	//底图拷贝中，稍后重试
#define DEV_RECSUPP_ERRCODE_1009	-1009	//格式化中，稍后重试
#define DEV_RECSUPP_ERRCODE_1010	-1010	//热插拔中，稍后重试
#define DEV_RECSUPP_ERRCODE_1011	-1011	//存储错误
#define DEV_RECSUPP_ERRCODE_1012	-1012	//CURL资源正在使用中
#define DEV_RECSUPP_ERRCODE_1013	-1013	//CURL下载报错
#define DEV_RECSUPP_ERRCODE_1014	-1014	//CURL内部错误
#define DEV_RECSUPP_ERRCODE_1015	-1015	//CURL内部错误
#define DEV_RECSUPP_ERRCODE_1016	-1016	//CURL内部错误
#define DEV_RECSUPP_ERRCODE_1017	-1017	//获取进度错误
#define DEV_RECSUPP_ERRCODE_1018	-1018	//查询错误
#define DEV_RECSUPP_ERRCODE_1019	-1019	//MD5比较错误
//-1020~-1127：内部错误
#define DEV_RECSUPP_ERRCODE_1124	-1124	//服务器MD5长度不足
#define DEV_RECSUPP_ERRCODE_1128	-1128	//其他任务在运行
#define DEV_RECSUPP_ERRCODE_1129	-1129	//任务数量超限
#define DEV_RECSUPP_ERRCODE_1130	-1130	//录像文件大小超限（MP4文件体积超过6GB）

typedef struct tagRecordSupplementPara
{
	int			iSize;					//结构体大小
	char		cMissionID[LEN_64];		//本次回补任务ID号
	int			iHttpSvrChannelNo;		//服务器录像的通道号，0~n-1,主码流录像；n~2n-1,副码流录像
	long long	llStartTime;			//录像开始时间，UTC时间
	long long	llStopTime;				//录像结束时间，UTC时间
	int			iFileType;				//下载录像的类型：0-所有文件不区分类型 1-定时录像 3-报警录像 7-手动录像
	char		cMsIp[LEN_16];			//http vodsvr服务ipv4地址
	int			iMsPort;				//http vodsvr服务对外提供端口
	char		cCmdType[LEN_64];		//命令类型，下载录像类型为 download
	char		cMsId[LEN_256];			//登录中间件id，字符集为utf-8编码
	char		cMsSupId[LEN_256];		//主机所在中间件id，字符集为utf-8编码
	char		cClientMsId[LEN_256];	//登录端能访问的中间件id，字符集为utf-8编码
	char		cClientMsIp[LEN_16];	//登录端能访问的中间件ip
	int			iClientMsPort;			//登录端能访问的中间件端口
	char		cHostId[LEN_256];		//主机id，字符集为utf-8编码
	char		cAliasName[LEN_256];	//录像别名，字符集为utf-8编码
	int			iDiskGrpNo;				//补入盘组号，0~设备支持的最大盘组号-1
} RecordSupplementPara, *pRecordSupplementPara;

typedef struct tagRecordSupplementResult
{
	int			iSize;					//结构体大小
	char		cMissionID[LEN_64];		//本次回补任务ID号
	int			iChannelNo;				//0~n-1: 主码流录像；n~2n-1: 副码流录像
	int			iRetCode;				//0-成功，其他请查看【设备主动回补录像错误码定义】
} RecordSupplementResult, *pRecordSupplementResult;

typedef struct tagRecordSupplementProgress
{
	int			iSize;					//结构体大小
	char		cMissionID[LEN_64];		//本次回补任务ID号
	int			iProgress;				//0～100:正常执行的百分比，其他请查看【设备主动回补录像错误码定义】
	int			iState;					//任务状态:	0：预留，不使用
										//bit0：服务器录像时间不连续
										//bit1：尝试重连服务器
										//bit2：任务暂停中
	long long	llLastStopTime;			//回补成功的最后一个录像的结束时间，UTC时间，控制命令iCtrl为停止或暂停时有效
} RecordSupplementProgress, *pRecordSupplementProgress;

typedef struct tagRecordChnAliasName
{
	int			iSize;					//结构体大小
	int			iChanNo;				//通道号
	char		cAliasName[LEN_256];	//别名，字符集为utf-8编码
} RecordChnAliasName, *pRecordChnAliasName;

#define MAX_ONCE_RECORD_CHNCOUNT	5
typedef struct tagRecordChnAliasNameArr
{
	int					iSize;						//结构体大小
	int					iChnCount;					//录像通道个数，一次最多配置5个录像通道
	int					iSizeOfRecordChnAliasName;	//结构体RecordChnAliasName大小
	RecordChnAliasName	tChnArr[MAX_ONCE_RECORD_CHNCOUNT];
} RecordChnAliasNameArr, *pRecordChnAliasNameArr;

typedef struct tagGetAllRecordChnAliasName
{
	int					iSize;						//结构体大小
	int					iAllChnCount;				//录像通道总个数，最多支持MAX_TOTAL_CHANNEL_NUM=128个通道
	int					iSizeOfRecordChnAliasName;	//结构体RecordChnAliasName大小
	RecordChnAliasName	tAllChnArr[MAX_TOTAL_CHANNEL_NUM];
} GetAllRecordChnAliasName, *pGetAllRecordChnAliasName;

typedef struct tagNVRDevNetResources
{
	int					iChannelAccess;			//数字通道接入，单位 Mb
	int					iRemotePreview;			//远程预览，单位 Mb
	int					iRemoteDownload;		//远程回放或者下载，单位 Mb
	int					iReceivRemain;			//接入剩余，单位 Mb
	int					iSendRemain;			//转发剩余，单位 Mb
} NVRDevNetResources, *pNVRDevNetResources;

#define ALARM_VIDEO_STOP	0		//停止报警录像
#define ALARM_VIDEO_START	1		//开始报警录像
typedef struct tagK200StartStopAlarm
{
	int			iSize;					//结构体大小
	int			iChannelNo;				//通道号
	int			iAlarmType;				//报警类型
	int			iCMD;					//命令,1:开始,0:停止
} K200StartStopAlarm, *pK200StartStopAlarm;

typedef struct tagK200AlarmPara
{
	int			iChannelNo;				//通道号
	int			iAlarmType;				//报警类型
	int			iCMD;					//命令,1:开始,0:停止
	int			iRes;					//返回值，0:成功，1失败
} K200AlarmPara, *pK200AlarmPara;

#define MAX_DETECT_TARGET_AREA_NUM			 11

typedef struct tagVCAParaTarget
{		
	int					iChannelNo;			
	int					iDevType;				//0-IPC，1-NVR
	int					iRuleID;				//rule ID(0~7)
	int					iSceneID;				//scene ID(0~15)
	int					iValid;					//event valid, 0:invalid,1:valid
	int					iSensitivity;           //Sensitivity,value range : [0, 10]
	int					iRegionNum;				//Check the total number of rule areas,value range : [0, 10]
	vca_TPolygonEx		stPoints[MAX_DETECT_TARGET_AREA_NUM];			//The number of vertices of the polygon area and coordinate
}VCAParaTarget, *pVCAParaTarget;

typedef struct tagTargetParaSwitch
{
	int			iChannelNo;				//通道号
	int			iEnable;				//开关
}TargetParaSwitch, *pTargetParaSwitch;

typedef struct tagTargetParaSwitchResult
{
	int			iChannelNo;				//通道号
	int			iEnable;				//开关
	int			iResult;				//结果
}TargetParaSwitchResult, *pTargetParaSwitchResult;

typedef struct tagTargetQueryResult
{
	int			iSize;
	int			iChannelNo;					//通道号
	int			iGroupNO;					//组号
	int			iGroupDataCnt;				//单组总数据N个数,取值范围:[0,64]
	int			iDataNo;					//数据编号1，取值范围:[1,N]
	int			iTgtId;						//目标id,算法输出目标ID
	int			iTgtType;					//目标类型,0-未知，1-行人，2-物，3-机动车,4-靶标
	int			iTgtScore;					//目标分值,取值范围:[0,100]，算法输出实际值*100
	int			iPosX1;						//左上角X坐标
	int			iPosY1;						//左上角Y坐标
	int			iPosX2;						//右下角X坐标
	int			iPosY2;						//右下角Y坐标
	long long	llCurrentTime;				//目标出现的时间
	int			iCurrentMsTime;				//目标出现的时间-毫秒部分
}TargetQueryResult, *pTargetQueryResult;

#define REC_SUPP_CTRL_STOP			1	//录像补录停止
#define REC_SUPP_CTRL_PAUSE			2	//录像补录暂停
#define REC_SUPP_CTRL_REINSTATE		3	//录像补录起复
typedef struct tagDevReplayRecordFile
{
	char		cMissionID[LEN_64];			//任务ID,本次回补任务ID号，不超过64字节，包括\0
	int			iCtrl;						//控制命令, 1-停止 2-暂停 3-起复
	int			iRetCode;					//0-成功，其他请查看【设备主动回补录像错误码定义】
	long long	llLastStopTime;				//回补成功的最后一个录像的结束时间，UTC时间，控制命令iCtrl为停止或暂停时有效
} DevReplayRecordFile, *pDevReplayRecordFile;

typedef struct tagRecSuppTaskPara
{
	char		cMissionID[LEN_64];			//任务ID, 本次回补任务ID号，不超过64字节，包括\0
	int			iTaskState;					//控任务状态: 1-停止中 2-暂停 3-执行中
} RecSuppTaskPara, *pRecSuppTaskPara;
#define MAX_REC_SUPP_TASK_COUNT		8
typedef struct tagRecSuppTaskList
{
	int					iSize;									//[IN][OUT]结构体大小
	int					iTaskCount;								//[OUT]设备回补录像任务数量
	int					iSizeOfRecSuppTaskPara;					//[IN][OUT]sizeof(RecSuppTaskPara)		
	RecSuppTaskPara		tTaskArr[MAX_REC_SUPP_TASK_COUNT];		//[OUT]设备回补录像任务列表
} RecSuppTaskList, *pRecSuppTaskList;

typedef struct tagIrrReceiverMSG
{
	int			iControl;				//操作类型:0保留、1添加、2编辑、3删除、
	int			iPeopleId;				//联系人序号:1～101
	char		cName[LEN_64];			//联系人名:UTF-8编码，最大长度64字符
	char		cPhoneNumber[LEN_64];	//联系人电话号码:UTF-8编码，最大长度64字符
	int			iType;					//联系人可接受的操作:按位使用：0未使能，1使能    bit0：打电话   bit1：发短信
}IrrReceiverMSG, *pIrrReceiverMSG;

typedef struct tagIrrReceiverMSGResult
{
	int			iControl;				//操作类型:0保留、1添加、2编辑、3删除、
	int			iResult;				//操作结果:0-成功  1-超出最大个数限制错误  2-设置参数超出范围错误
	int			iPeopleId;				//联系人序号:1～101
	char		cName[LEN_64];			//联系人名:UTF-8编码，最大长度64字符
	char		cPhoneNumber[LEN_64];	//联系人电话号码:UTF-8编码，最大长度64字符
	int			iType;					//联系人可接受的操作:按位使用：0未使能，1使能    bit0：打电话   bit1：发短信
}IrrReceiverMSGResult, *pIrrReceiverMSGResult;

typedef struct tagIrrReceiverMSGGet
{
	int			iPageNo;				//页码:iPageNo>=0
	int			iPageSize;				//每页条数:iPageSize>=0 最大20
	int			iPeopleIdStart;			//联系人的开始序号:1～101
}IrrReceiverMSGGet, *pIrrReceiverMSGGet;

typedef struct tagIrrReceiverMSGGetResult
{
	int			iTotal;					//总条数,iTotal>=0
	int			iPageNo;				//页码:iPageNo>=0
	int			iPageSize;				//每页条数:iPageSize>=0 最大20
	int			iIndex;					//页码内序号,iIndex>=0
	int			iPeopleId;				//联系人序号,1～101
	char		cName[LEN_64];			//联系人名,UTF-8编码，最大长度64字符
	char		cPhoneNumber[LEN_64];	//联系人电话号码,UTF-8编码，最大长度64字符
	int			iType;	
}IrrReceiverMSGGetResult, *pIrrReceiverMSGGetResult; 

typedef struct tagIrrReceiverCommonMSG
{
	int iPlayAudioCount;		//语音播报次数 1～5
}IrrReceiverCommonMSG, *pIrrReceiverCommonMSG;

#define MAX_QUERY_PAGE_COUNT					20
typedef struct tagIrrReceiverMSGGetResultArr
{
	IrrReceiverMSGGetResult tIrrReceiverMSGGetResult[MAX_QUERY_PAGE_COUNT];
}IrrReceiverMSGGetResultArr, *pIrrReceiverMSGGetResultArr;

typedef struct tagTHMScreenDataCorrect
{
	int iType;		//传感器类型
	int iValue;		//校正值
}THMScreenDataCorrect, *pTHMScreenDataCorrect;

typedef struct tagTHMScreenSystem
{
	int iScreenBright;		//屏幕亮度
	int iSwitchCtrl;		//屏幕按键控制使能
}THMScreenSystem, *pTHMScreenSystem;

typedef struct tagGaugeCalibCheckData
{
    int			iSceneId;				//场景编号	0~31  （默认使用0~15）
    int			iRuleID;				//规则ID(预留)	0~15  （默认使用 0）
    int			iGaugeType;				//水尺类型	0-保留 1-宽尺、新尺 2-窄尺、旧尺（注意与算法对应转换）3-特制水尺,支持自标定  4-方块水尺,支持自标定（只用于积水深度算法） 5-桩式水尺，支持自标定 6-无水尺 7-虚拟水尺 8-桩式水尺(纵向接力) 9-无水尺(纵向接力) 10-阶梯水尺(支持不同规格长度)
    int			iGaugeGroupId;			//水尺组号	1~30 (与VCA*GAUGEINFO协议的iGaugeId对应)
    int			iGaugeCalibId;			//水尺每组标定预置位的序号	1~20
    int         iGaugeValue;            //视频中心点水尺读数	范围：-10000000~10000000，单位：mm
	int			iAngleValue;			//倾角仪角度	"倾角仪角度乘以10000，单位是度，实际范围0-360	软件取值后除以10000.0转换成float"
    int         iReferNum;              //标定点个数	0~10
    VCA_GaugeInfoPoint		tReferPoint[MAX_GAUGEINFO_NUM];//坐标参数为万分比			//联系人名,UTF-8编码，最大长度64字符
}GaugeCalibCheckData, *pGaugeCalibCheckData; 

typedef struct tagGaugeCalibCheckGet
{
    int			iSceneId;				//场景编号	0~31  （默认使用0~15）
    int			iRuleID;				//规则ID(预留)	0~15  （默认使用 0）
    int			iGaugeType;				//水尺类型	0-保留 1-宽尺、新尺 2-窄尺、旧尺（注意与算法对应转换）3-特制水尺,支持自标定  4-方块水尺,支持自标定（只用于积水深度算法） 5-桩式水尺，支持自标定 6-无水尺 7-虚拟水尺 8-桩式水尺(纵向接力) 9-无水尺(纵向接力) 10-阶梯水尺(支持不同规格长度)
    int			iGaugeGroupId;			//水尺组号	1~30 (与VCA*GAUGEINFO协议的iGaugeId对应)
}GaugeCalibCheckGet, *pGaugeCalibCheckGet; 

typedef struct tagGaugeCalibCheckResult
{
    int			iSceneId;				//场景编号	0~31  （默认使用0~15）
    int			iRuleID;				//规则ID(预留)	0~15  （默认使用 0）
    int			iGaugeType;				//水尺类型	0-保留 1-宽尺、新尺 2-窄尺、旧尺（注意与算法对应转换）3-特制水尺,支持自标定  4-方块水尺,支持自标定（只用于积水深度算法） 5-桩式水尺，支持自标定 6-无水尺 7-虚拟水尺 8-桩式水尺(纵向接力) 9-无水尺(纵向接力) 10-阶梯水尺(支持不同规格长度)
    int			iGaugeGroupId;			//水尺组号	1~30 (与VCA*GAUGEINFO协议的iGaugeId对应)
    int			iResult;			    //查询操作结果	"0为标定参数全部校验合格
                                        //-1为标定参数数据不全
                                        //>0为返回错误的水尺标定预置位序号个数"
    int         iGaugeCalibId;          //水尺每组标定预置位的序号	"1~20
                                        //当iGaugeType=9时，序号范围最大值取“0x9: 智能分析-133,水位算法支持无水尺(纵向接力)个数”
                                        //当iGaugeType=10时，序号范围最大值取“0x9: 智能分析-148,水位算法支持阶梯水尺(不同规格长度)组数"
    int         iCalibIdResult;         //此组标定预置位中心点水尺读数校验结果	"0为中心点水尺读数设置错误
                                        //1为中心点水尺读数参数过小
                                        //2为中心点水尺读数参数过大"
    int         iReferNum;              //坐标参数为万分比			//联系人名,UTF-8编码，最大长度64字符
    int         iReferPointResult[MAX_GAUGEINFO_NUM];    //标定点校验结果	"1为标定点小于2个
                                                        //1为标定点坐标在图像中偏差过大
                                                        //2为标定点偏差过大"

}GaugeCalibCheckResult, *pGaugeCalibCheckResult; 

typedef struct tagMobileParam
{
	char	cTestIP[LEN_64];	//IP地址 包括‘/0’ 最大64字节，目前只支持ipv4地址
	int		iOnoff;				//是否使能 0 不使能，1 使能
	int		iNetType;			//网络类型 无法设置，只能获取 0,无网络；1，有线网络；2，4G网络；3，WIFI网络					
	int		iRet;				//返回值 0 成功，其它失败
}MobileParam, *pMobileParam;

typedef struct tagRemoteAlarmState
{
	int			iSize;
	int			iChannelNo;
	int			iAlarmStatus;		//0-保留 1-报警
	int			iParam1;
	int			iParam2;
	int			iParam3;
	char		cParam1[LEN_256];	//最大长度256个字符，中文或多国语字符使用UTF-8
	char		cParam2[LEN_256];	//最大长度256个字符，中文或多国语字符使用UTF-8
	char		cParam3[LEN_256];	//最大长度256个字符，中文或多国语字符使用UTF-8
}RemoteAlarmState, *pRemoteAlarmState; 

typedef struct tagRemoteAlarmResult
{
	int			iResult;		//0-成功，小于0-协议处理失败
}RemoteAlarmResult, *pRemoteAlarmResult; 

typedef struct	tagEnableScheduleTimeRange
{
	int			iTimevaild;			//0-时间段无效，1-时间段有效
	char		cBeginTime[LEN_16]; //格式23:59:00,范围00:00:00-23:59:00 时:分:秒,秒赋值00
	char		cEndTime[LEN_16];	//格式23:59:00,范围00:00:00-23:59:00 时:分:秒,秒赋值00
}EnableScheduleTimeRange;//此结构体不往后扩展

typedef struct tagXmlMaintainDemist
{
	int						iChannelNo;
	int						iEnable; //0:关闭除雾 1:开启除雾
	EnableScheduleTimeRange tTimeSchedule[MAX_WEEK_DAYS+1][MAX_DEVLOWPOWER_DAYSCHEDULE_COUNT];	//MAX_WEEK_DAYS 0:预留 1:周一 2:周二 ... 6:周六 7:周日
}XmlMaintainDemist;

typedef struct tagLanPriority
{
	int		iLanName;	//网卡类型	1，有线网络；2，4G网络；3，WIFI网络					
	int		iPriority;	//优先级	1-100,1为最高优先级，本项目中只设置1，增加此字段为了方便后续扩展					
	int		iRet;		//返回值	0 成功，其它失败					
}LanPriority, *pLanPriority;

#define MAX_CKD_SNCKCODE_LEN				256
#define MAX_ENCRYPT_PARAM					4
typedef struct tagCKDSecret
{
	int   iSize;
	int   iChannelNo;
	int   iParamLen;	//加密串总长度
	char  pcParam[MAX_CKD_SNCKCODE_LEN];	//加密串
}CKDSecret,*pCKDSecret;

typedef struct tagCKDSecretResult
{
	int   iSize;
	int   iResult;  //设置结果,0-成功，其他-失败
	int   iChannel;
}CKDSecretResult,*pCKDSecretResult;

// Disk group structureV2
#define	BIT_COUNTS_INT						32			//表示一个int有32位
#define MAX_DISK_GROUPV2_NUM				16
#define DISK_GROUPV2_DISK_PARAM_NUM			2
#define DISK_GROUPV2_CHAN_PARAM_NUM			8
#define DISK_GROUP_ESATA_INDEX				10000
typedef struct
{
	unsigned int iGroupNo;										//盘组编号,16~31
	unsigned int uiDiskNO[DISK_GROUPV2_DISK_PARAM_NUM];			// 磁盘编号，按位使能，bit0代表磁盘0，bit1代表磁盘1，范围0~23
	unsigned int uiChannelNO[DISK_GROUPV2_CHAN_PARAM_NUM];		//通道编号，实际的通道数按bit位使能，通道号从小到大，对应bit位从低到高。
	unsigned int uiDiskEsata;									//10000，esata
}GROUPV2INFOARR;

typedef struct
{
	int				iGroupNum;											//盘组数量, [1-16]
	int				iSize;												//GROUPV2INFOARR 结构体大小
	GROUPV2INFOARR	GroupV2InfoArr[MAX_DISK_GROUPV2_NUM];				//盘组信息结构体数组
}DISK_GROUPV2;

#endif


#ifndef __GLOBAL_TYPES_H__
#define __GLOBAL_TYPES_H__

#if (defined(_WIN32) || defined(_WIN64)) && !defined(__WIN__)
#define __WIN__
#endif

#ifndef __WIN__
#include <stdlib.h>
#include <sys/resource.h>
#endif

#ifndef __WIN__
typedef struct _RECT 
{ 
    int left; 
    int top; 
    int right; 
    int bottom; 
}RECT, *LPRECT;

typedef struct tagPOINT
{
    int  x;
    int  y;
}POINT, *LPPOINT;

#define __stdcall
#define __cdecl

#ifndef OUT
#define OUT
#endif

#ifndef PVOID
#define PVOID void*
#endif

#else
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 
#endif
#include <windows.h>
#endif

/**********************************************************************************************
* Character length                                                                 
**********************************************************************************************/
#define OVERRIDE
#define LEN_0									0
#define LEN_2									2
#define LEN_4									4
#define LEN_5									5
#define LEN_8   								8
#define LEN_10									10
#define LEN_16  								16
#define LEN_17  								17
#define LEN_18  								18
#define LEN_20  								20
#define LEN_24  								24
#define LEN_32  								32
#define LEN_33  								33
#define LEN_36  								36
#define LEN_64  								64
#define LEN_65  								65
#define LEN_63  								63
#define LEN_96  								96
#define LEN_128 								128
#define LEN_250 								250
#define LEN_256 								256
#define LEN_255 								255
#define LEN_512 								512
#define LEN_300 								300
#define LEN_1024 								1024
#define LEN_1300 								1300 
#define LEN_2048 								2048 
/**********************************************************************************************/


/**********************************************************************************************
* Intelligent analysis of drawing area limits
**********************************************************************************************/
#define VCA_MAX_POLYGON_POINT_NUM				16	//The maximum number of points allowed per polygon
#define VCA_MAX_POLYGON_POINT_NUMEX				32	//Max Tripwire Points Number
#define VCA_MAX_POLYGON_NUM						25	//The maximum number of polygons allowed per VCA rule
#define MAX_AREA_NUM							4	//Max Area Number for 300w Leave Detect
/**********************************************************************************************/


/**********************************************************************************************
* The intelligent analysis information stored in the video file
**********************************************************************************************/
#define MAX_SAVE_TARGET_NUM						32	
#define MAX_SAVE_TARGET_NUM_NEW					128
#define MAX_VCA_TARGET_COUNT					MAX_SAVE_TARGET_NUM+MAX_SAVE_TARGET_NUM_NEW
#define VCAINFO_SEARCHRULE_MIN					0
#define VCAINFO_SEARCHRULE_FRAME				(VCAINFO_SEARCHRULE_MIN+0)
#define VCAINFO_SEARCHRULE_POLYGON				(VCAINFO_SEARCHRULE_MIN+1)
#define VCAINFO_SEARCHRULE_TRIPWIRE				(VCAINFO_SEARCHRULE_MIN+2)
#define VCAINFO_SEARCHRULE_MAX					(VCAINFO_SEARCHRULE_MIN+3)
/**********************************************************************************************/


/**********************************************************************************************
* The intelligent analysis information stored in the video file
**********************************************************************************************/
#define SIP_CMD_SET_MIN							(2)
#define SIP_CMD_SET_ALARMCHANNEL				(SIP_CMD_SET_MIN+0)
#define SIP_CMD_SET_VIDEOCHANNEL				(SIP_CMD_SET_MIN+1)
#define SIP_CMD_SET_MAX							(SIP_CMD_SET_MIN+2)
/**********************************************************************************************/


/**********************************************************************************************
* Video Size                                                                      
**********************************************************************************************/
#define QCIF									0		//QCif 	176*144
#define HCIF									1		//HCif	352*144	
#define FCIF									2		//Cif	352*288	
#define HD1										3		//HD1	720*288
#define FD1										4		//D1	720*576
#define MD1										5		//MD1	720*288
#define QVGA									6		//QVGA	320x240
#define VGA										7		//VGA	640*480
#define HVGA									8		//HVGA	640*240
#define HD_720P									9		//720p	1280*720
#define HD_960P									10		//960P	1280*960
#define HD_200W									11		//200W	1600*1200
#define HD_1080P								12		//1080P	1920*1080
#define HD_QXGA									13		//QXGA	2048*1536

#define QHD										0x100	//QHD	960*540
#define VZ_960H									0x200	//960H	960*576
#define VZ_720_720								0x201	//		720*720
#define VZ_2MP									0x202	//2MP	1280*1280
#define VZ_3MP									0x203	//3MP	3072*1152
#define VZ_1440P								0x210	//1440P	2560*1440
#define VZ_4MP									0x220	//4MP	2592*1520
#define WUXGA									0x230	//WUXGA 1920*1200
#define HK_4MP									0x240	//4MP	2688*1520
#define VZ_FEC_4MP								0x250	//4MP	2048*2048
#define VZ_5MA									0x300	//5.0MP	2448*2048
#define VZ_5M									0x400	//5M(2)	2560*1920
#define VZ_5MB									0x410	//5M(3)	2592*1944
#define VZ_5MC									0x420	//5M(4)	2592*2048
#define VZ_5MD									0x430	//5M(5)	2528*2128
#define VZ_5ME									0x500	//5M	2560*2048
#define VZ_6M									0x600	//6M	2752*2208
#define VZ_6MA									0x610	//6M	3008*2008
#define VZ_6MB									0x620	//6M	3408*2008
#define VZ_FEC_6M								0x630	//FEC6M	2560*2560
#define VZ_7M									0x700	//7M	3392*2008
#define VZ_FEC_7M								0x700	//FEC7M	3072*2304
#define VZ_8MA									0x800	//8M	3840*2160
#define VZ_8MB									0x810	//8M	3264*2448
#define VZ_8MC									0x820	//8M	329682472
#define VZ_9M									0x900	//9M	3072*3072
#define VZ_12M									0x1200	//12M	4000*3072
/*********************************************************************************************/



/**********************************************************************************************
** User data information                                                                    
**********************************************************************************************/
#define GET_USERDATA_INFO_MIN                   (0)                           
#define GET_USERDATA_INFO_TIME					(GET_USERDATA_INFO_MIN )	//Get time information in user data-0xFED3
#define GET_USERDATA_INFO_OSD					(GET_USERDATA_INFO_MIN + 1)	//Get OSD information in user data-0xFED1
#define GET_USERDATA_INFO_GPS                   (GET_USERDATA_INFO_MIN + 2)	//Get GPS information in user data-0xFED4
#define GET_USERDATA_INFO_VCA                   (GET_USERDATA_INFO_MIN + 3)	//Get VCA information in user data-0xFED2
#define GET_USERDATA_INFO_USER_DEFINE           (GET_USERDATA_INFO_MIN + 4)	//Get the custom information in the user data-0x0001
#define GET_USERDATA_INFO_VITAL_SIGN_DATA		(GET_USERDATA_INFO_MIN + 5)	//Get vital signs information in user data-0xFEDA
#define GET_USERDATA_INFO_DEMOGRAPHICS			(GET_USERDATA_INFO_MIN + 6)	//Get the count in the user data-0xFED9
#define GET_USERDATA_INFO_LPR					(GET_USERDATA_INFO_MIN + 7)	//Get license plate identification information in user data-0xFED5
#define GET_USERDATA_INFO_OSD_EX				(GET_USERDATA_INFO_MIN + 8)	//Get OSD information in user data-0xFEDC
#define GET_USERDATA_INFO_LDC					(GET_USERDATA_INFO_MIN + 9)	//Get Obtain distortion correction information in user data-0xFEDD
#define GET_USERDATA_INFO_ZF                    (GET_USERDATA_INFO_MIN + 10)//Get ZF focus mark-0xFEDE
#define GET_USERDATA_INFO_VCA_SRC_SIZE          (GET_USERDATA_INFO_MIN + 11)//Get src size VCA information in user data-0xFED8
#define GET_USERDATA_INFO_TIMESTAMP				(GET_USERDATA_INFO_MIN + 12)//Get timestamp in user data-0xFEDF 
#define GET_USERDATA_INFO_TARGET_ATTR           (GET_USERDATA_INFO_MIN + 13)//Get target attribute in user data-0xFEE0
#define GET_USERDATA_INFO_VCA_RULE				(GET_USERDATA_INFO_MIN + 14)//Get target attribute in user data-0xFEE1
#define GET_USERDATA_PTZ_DATA					(GET_USERDATA_INFO_MIN + 15)//Get Ptz Data-0xFEE2
#define GET_USERDATA_VIRGAUGE_DATA              (GET_USERDATA_INFO_MIN + 16)//Get VirtualGauge Data-0xFEE8
#define GET_USERDATA_INFO_ILLEGAL_AREA			(GET_USERDATA_INFO_MIN + 17)//Get ILLEGAL AREA Data-0xFEE9
#define GET_USERDATA_INFO_WATER_SPEED_BASE		(GET_USERDATA_INFO_MIN + 18)//Get Water Speed Base Data-0xFEE6
#define GET_USERDATA_INFO_WATER_SPEED_ALGINFO	(GET_USERDATA_INFO_MIN + 19)//Get Water Speed AlgInfo Data-0xFEE7
#define GET_USERDATA_INFO_ILLEGAL_PARK_AREA		(GET_USERDATA_INFO_MIN + 20)//Get ILLEG ALPARK AREA-0xFEEA
#define GET_USERDATA_INFO_ILLEGAL_PARK_CAR		(GET_USERDATA_INFO_MIN + 21)//Get ILLEG ALPARK CAR-0xFEEB
#define GET_USERDATA_INFO_CPC_INCREASE			(GET_USERDATA_INFO_MIN + 22)//Get CPC INCREASE-0xFEE4
#define GET_USERDATA_INFO_CPC_COUNT				(GET_USERDATA_INFO_MIN + 23)//Get CPC Count-0xFEE5
#define GET_USERDATA_INFO_TRAFFIC_TARGET		(GET_USERDATA_INFO_MIN + 24)//Get traffic tartget-0xFEEC
#define GET_USERDATA_INFO_TEMP_INFO      		(GET_USERDATA_INFO_MIN + 25)//Get temperature scale information-0xFEED
#define GET_USERDATA_INFO_COMP_DATA      		(GET_USERDATA_INFO_MIN + 26)//Get COMPDATA-0xFEEE
#define GET_USERDATA_REGION_DATA				(GET_USERDATA_INFO_MIN + 27)//Get Region Data-0xFEEF
#define GET_USERDATA_LAW_CASE_DATA				(GET_USERDATA_INFO_MIN + 28)//Get Law Case Area Data-0xFEF1
#define GET_USERDATA_WATERMARK_VIDEODATA		(GET_USERDATA_INFO_MIN + 29)//Get watermark summary data-0xFEF2
#define GET_USERDATA_WATERMARK_USERDATA			(GET_USERDATA_INFO_MIN + 30)//Get watermark user data-0xFEF3
#define GET_USERDATA_POLICECLOTH				(GET_USERDATA_INFO_MIN + 31)//Get Police Cloth Info-0xFEF4
#define GET_USERDATA_VESTCLOTH					(GET_USERDATA_INFO_MIN + 32)//Get Vest Cloth Info-0xFEF5
#define GET_USERDATA_TEMPERATURE				(GET_USERDATA_INFO_MIN + 33)//Get temperature Info-0xFEF6
#define GET_USERDATA_DZ_HMAC					(GET_USERDATA_INFO_MIN + 34)//HMAC Result-0xFEF7
#define GET_USERDATA_PTZ_POS_INFO				(GET_USERDATA_INFO_MIN + 35)//Real-time operating position-0xFEF8
#define GET_USERDATA_CLASS_BEHAVER				(GET_USERDATA_INFO_MIN + 36)//Classroom behavior identification data-0xFEF9
#define GET_USERDATA_DZ_CHECK_HMAC				(GET_USERDATA_INFO_MIN + 37)//HMAC Check Result-0xFEFA
#define GET_USERDATA_INFO_VCA_SRC_VIDEO_SIZE    (GET_USERDATA_INFO_MIN + 38)//Get src video size for VCA information in user data-0xFED7
#define GET_USERDATA_VCA_SRC_VIDEO_SIZE_ENABLE  (GET_USERDATA_INFO_MIN + 39)//Get src video size enable-0xFEFB
#define GET_USERDATA_TARGET_TRACK_STYLE			(GET_USERDATA_INFO_MIN + 40)//Get target track style para-0xFEFC
#define GET_USERDATA_MS_ABSTIME					(GET_USERDATA_INFO_MIN + 41)//Get ms absolute time-0xFEFD
#define GET_USERDATA_WATERMARK_ADD_DATA			(GET_USERDATA_INFO_MIN + 42)//Get watermark additional data-0xFEFE
#define GET_USERDATA_INFO_VCA_EX				(GET_USERDATA_INFO_MIN + 43)//Get VCA information in user data--Old version f0-0xFEE3
#define GET_USERDATA_INFO_VCA_NEW				(GET_USERDATA_INFO_MIN + 44)//Get VCA information in user data--New version f1-0xFEE3
#define GET_USERDATA_INFO_MAX                   (GET_USERDATA_INFO_MIN + 45)

#define GET_ALL_USERDATA_INFO					(0xFF)
#define GET_USERDATA_END						(0xFFFFFF) 
/**********************************************************************************************/

//High precision PTZ information
typedef struct
{
	int m_PanPosition;   //Horizontal coordinates p*10000，p：[0,360]
	int	m_TiltPosition;  //Vertical coordinates t*10000, t：[-90,90]
	int	m_ZoomPosition;  //Multiplying factor z*10000, z：[1,Actual ratio]
} TPtzInfoEx;

//Vi Wide height
typedef struct
{
	unsigned short		Width;		//device vi width
	unsigned short		Height;		//device vi height
}TWSViSize;

//The velocity meter elevation and other information
typedef struct
{
	int	altitude;		//Camera height，unit:mm
	int	waterlevel;		//Water level，unit:mm
	int	width;			//The actual scene width of the center of the camera，unit:mm
}TWSScene;

typedef struct
{
	TPtzInfoEx		ptz;
	TWSViSize		video;
	TWSScene		scene;
}TWaterSpeedBase;


typedef struct
{
	unsigned short		x;		//The matching point is the coordinate，millionth
	unsigned short		y;		//Match point ordinate，millionth
	unsigned int	time;	//Match time stamp，unit:ms
}TWSPointInfo;

#define WATER_SPEED_POINT_NUM  2		  
typedef struct
{
	TWSPointInfo		point[WATER_SPEED_POINT_NUM];			//A group Match point
} TWaterSpeedAlgInfo;


/* License plate string length */                                                        
#define LPR_PLATE_STR_LEN						11
#define MAX_OUTPUT_LPR_NUM						10

/* * License Plate Color * */  
#define LPR_PLATE_COLOR_UNKNOWN					0	// Unknown
#define LPR_PLATE_COLOR_BLUE					1	// Blue card
#define LPR_PLATE_COLOR_YELLOW					2	// Yellow card
#define LPR_PLATE_COLOR_WHITE					3	// White card
#define LPR_PLATE_COLOR_BLACK					4	// Black card
#define LPR_PLATE_COLOR_GREEN					5	// Green card
#define LPR_PLATE_COLOR_YELLOWTOGREEN			51	// Yellow to green card(New energy vehicle) 
#define LPR_PLATE_COLOR_GRADIENTGREEN			52	// Gradient green card(New energy vehicle)
#define LPR_PLATE_COLOR_RED						53	// Red card

/* * The Color of Car * */  
#define LPR_CAR_COLOR_WHITE					0	// White
#define LPR_CAR_COLOR_RED					1	// Red
#define LPR_CAR_COLOR_YELLOW				2	// Yellow
#define LPR_CAR_COLOR_YELLOW1				3	// Yellow
#define LPR_CAR_COLOR_BLUE					4	// Blue
#define LPR_CAR_COLOR_GREEN					5	// Green
#define LPR_CAR_COLOR_GREEN1				6	// Green
#define LPR_CAR_COLOR_PURPLE				7	// Purple
#define LPR_CAR_COLOR_PINK					8	// Pink
#define LPR_CAR_COLOR_BLACK					9	// Black
#define LPR_CAR_COLOR_RED1					10	// Red
#define LPR_CAR_COLOR_YELLOW2				11	// Yellow
#define LPR_CAR_COLOR_YELLOW3				12	// Yellow
#define LPR_CAR_COLOR_GRAY					13	// Gray
#define LPR_CAR_COLOR_YELLOW4				14	// Yellow
#define LPR_CAR_COLOR_BLUE1					15	// Blue
#define LPR_CAR_COLOR_BLUE2					16	// Blue
#define LPR_CAR_COLOR_GREEN2				17	// Green
#define LPR_CAR_COLOR_GREEN3				18	// Green
#define LPR_CAR_COLOR_WHITE1				19	// White
#define LPR_CAR_COLOR_GREEN4				20	// Green
#define LPR_CAR_COLOR_CYAN_BLUE				21	// cyan-blue
#define LPR_CAR_COLOR_YELLOW5				22	// Yellow
#define LPR_CAR_COLOR_RED2					23	// Red
#define LPR_CAR_COLOR_BLUE3					24	// Blue
#define LPR_CAR_COLOR_BLUE4					25	// Blue
#define LPR_CAR_COLOR_GRAY1					26	// Gray
#define LPR_CAR_COLOR_PURPLE1				27	// Purple
#define LPR_CAR_COLOR_PURPLE2				28	// Purple
#define LPR_CAR_COLOR_BROWN					29	// Brown
#define LPR_CAR_COLOR_BROWN1				30	// Brown
#define LPR_CAR_COLOR_BROWN2				31	// Brown
#define LPR_CAR_COLOR_ORANGE				32	// Orange
#define LPR_CAR_COLOR_LIGHT_BLUR		    33	// Light Blue
#define LPR_CAR_COLOR_MULTI_COLOR			34	// Multi Color
#define LPR_CAR_COLOR_UNKNOWN				99	// Unknown

/* Currently Supported Models */
#define LPR_VT_UNKOWN							0	//Unknown model
#define LPR_VT_BUS								1	//Buses and coaches
#define LPR_VT_CAR								2	//Cars
#define LPR_VT_TRUCK							3	//Trucks, including large and small goods
#define LPR_VT_VAN								4	//The van
#define LPR_VT_BIG_TRUCK						5	//large trucks
#define LPR_VT_SMALL_TRUCK						6	//A minivan
#define LPR_VT_TWO_THREE_WHEELER				7	//Two or three wheels
#define LPR_VT_PERSON							8	//Pedestrians	
#define LPR_VT_SUV								9	//SUV
#define LPR_VT_MID_BUS							10	//Medium Buses
#define LPR_VT_PLATE_ASKEW						11	//License plate hanging
#define LPR_VT_BGS								14	//Background detection detected
#define LPR_VT_OTHER							15	// Other models
/**********************************************************************************************/
/* Car PLATE Type  */                                                                  
#define LPR_PLATE_TYPE_UNKOWN			   		0			//Unknown
#define LPR_PLATE_TYPE_CAR_NORMAL		        1			//Ordinary car, private car.(Blue card,Black card)
#define LPR_PLATE_TYPE_BIG_NORMAL	            (1<< 1)		//Ordinary cart, truck.(Yellow card)
#define LPR_PLATE_TYPE_POLICE					(1<< 2)		//Police car.(White card)(separator between bit1 and bit2)
#define LPR_PLATE_YTPE_WJ          				(1<< 3)		//Armed police car.
#define LPR_PLATE_YTPE_HK						(1<< 4)		//Hong Kong and Macau car.
#define LPR_PLATE_YTPE_DOUBLELINE				(1<< 5)		//Double decker car.(Yellow card)
#define LPR_PLATE_YTPE_MILITARY					(1<< 6)		//Military
#define LPR_PLATE_TYPE_AMBASSADOR				(1<< 7)		//Embassy car.
#define LPR_PLATE_TYPE_DGREEN_TYPE2				(1<< 8)		//The second type of green car,electric bicycle.
#define LPR_PLATE_TYPE_POLICE_TYPE2				(1<< 9)		//Second class police car.(separator between bit2 and bit3)	
#define LPR_PLATE_TYPE_XUE						(1<< 10)	//Coach car.("xue")
#define LPR_PLATE_TYPE_MOTOR					(1<< 11)	//Motorcycle
#define LPR_PLATE_TYPE_LIAOTEMP					(1<< 12)	//Temporary license plate.("lin")
#define LPR_PLATE_TYPE_DGREEN					(1<< 13)	//Double decker car.Agricultural car.(Green card)
#define LPR_PLATE_TYPE_GUA						(1<< 14)	//Trailer.("gua")
#define LPR_PLATE_TYPE_NEWENERGY 				(1<< 15)	//New energy car.
#define LPR_PLATE_TYPE_TJELECTRICBICYCLE  		(1<< 16)	//electric bicycle.
#define LPR_PLATE_TYPE_GOVERNMENTPURPOSE  		(1<< 17)	//Government Purpose.
/**********************************************************************************************/
/* Car Logo  */                                                                  
#define LPR_LOGO_UNKNOWN						0	// Unknown
#define LPR_LOGO_VOLKSWAGEN						1	//Volkswagen(Volkswagen / Volkswagen)
#define LPR_LOGO_HONDA							2	//Honda
#define LPR_LOGO_TOYOTA							3	//Toyota
#define LPR_LOGO_DONGFENG						4	//DongFeng
#define LPR_LOGO_FAWCAR							5	//First Automobile Works
#define LPR_LOGO_BUICKT							6	//Buick
#define LPR_LOGO_SUZUKI							7	//Suzuki
#define LPR_LOGO_CITROEN						8	//Citroen
#define LPR_LOGO_KIA							9	//KIA
#define LPR_LOGO_XIALI							10	//Xiali
#define LPR_LOGO_AUDI							11	//Audi
#define LPR_LOGO_HYUNDAI						12	//Hyundai
#define LPR_LOGO_SGMW							13	//SGMW
#define LPR_LOGO_CHEERY							14	//Chery Automobile Corporation
#define LPR_LOGO_MAZDA							15	//Mazda 
#define LPR_LOGO_HAFEI							16	//Hafei 
#define LPR_LOGO_CCAG							17	//Chongqing Changan
#define LPR_LOGO_NISSAN							18	//Nissan
#define LPR_LOGO_GREATWALL						19	//GreatWall
#define LPR_LOGO_BUILDYD						20	//build your dreams	
#define LPR_LOGO_FORD							21	//Ford
#define LPR_LOGO_CHEVROLET          			22	//Chevrolet
#define LPR_LOGO_BAOJUN							23	//BaoJun
#define LPR_LOGO_BENZ							24	//Benz
#define LPR_LOGO_EMGRAND						25	//Emgrand
#define LPR_LOGO_LEXUS							26	//Lexus
#define LPR_LOGO_MG								27	//MingJue
#define LPR_LOGO_BMW							28	//BMW
#define LPR_LOGO_PEUGEOT						29	//Peugeot
#define LPR_LOGO_PEUGEOT2						30	//Peugeot
#define LPR_LOGO_HAIMA							31	//Haima
#define LPR_LOGO_ZHONGHUA						32	//Zhonghua
#define LPR_LOGO_SKODA							33	//Skoda
#define LPR_LOGO_SOUTHEAST						34	//Southeast
#define LPR_LOGO_CROWN							35	//Crown
#define LPR_LOGO_JINBEI							36	//he Gold Cup
#define LPR_LOGO_MITSUBISHI						37	//Mitsubishi 	
#define LPR_LOGO_ROEWE							38	// Roewe
#define LPR_LOGO_GEELY							39	//Geely
#define LPR_LOGO_ENGLONCAR						40	//England
#define LPR_LOGO_GLEAGLE						41	//Global Hawk
#define LPR_LOGO_ANKAI							42	//Ankai
#define LPR_LOGO_PORSCHE						43	//Poriche
#define LPR_LOGO_BAICBAW						44	//Beiqi BAW
#define LPR_LOGO_BAICMOTOR 						45	//Beijing car
#define LPR_LOGO_BAICWEIWANG					46	//Beiwei Wei Wang
#define LPR_LOGO_CCAG2							47	//ChongQingChangAn
#define LPR_LOGO_CHANGHE						48	//ChaneHe
#define LPR_LOGO_DODGE							49	//Dodge
#define LPR_LOGO_FIAT							50	//Fiat
#define LPR_LOGO_FOTON							51	//Fukuda
#define LPR_LOGO_QOROS							52	//Concept
#define LPR_LOGO_HIGER							53	//Hagrid
#define LPR_LOGO_HAVAL							54	//Harvard
#define LPR_LOGO_CNHTCHOWO						55	//Heavy truck ho Wo
#define LPR_LOGO_HONGQI							56	//red flag
#define LPR_LOGO_HUANGHAI						57	//Yellow Sea
#define LPR_LOGO_HUIZHONG						58	//Shanghai Huizhong
#define LPR_LOGO_JAGUAR							59	//Jaguar
#define LPR_LOGO_JAGUAR2						60	//Jaguar2
#define LPR_LOGO_JEEP							61	//Jeep
#define LPR_LOGO_KINGLONG						62	//Golden Dragon
#define LPR_LOGO_GOLDENDRAGON					63	//Xiamen Golden Brigade
#define LPR_LOGO_JAC							64	//JAC
#define LPR_LOGO_JMC							65	//Jiangling
#define LPR_LOGO_JMC2							66	//Jiangling2
#define LPR_LOGO_CADILLAC						67	//Cadillac
#define LPR_LOGO_CHRYSLER						68	//Chrysler
#define LPR_LOGO_LINCOLN						69	//Lincoln
#define LPR_LOGO_RENAULT						70	//Renault
#define LPR_LOGO_LIFAN							71	//Lifan
#define LPR_LOGO_EVERUS							72	//idea
#define LPR_LOGO_LEOPAARD						73	//Cheetah
#define LPR_LOGO_MINI							74	//Mini
#define LPR_LOGO_MASERATI						75	//Maserati
#define LPR_LOGO_ACURA							76	//Acura
#define LPR_LOGO_SHANXIAUTOMOBILE				77	//Shaanxi Auto
#define LPR_LOGO_SHAOLINBUS						78	//Shaolin
#define LPR_LOGO_SUBARU							79	//Subaru
#define LPR_LOGO_SMART							80	//Mercedes-Benz
#define LPR_LOGO_VOLVO							81	//Volvo
#define LPR_LOGO_ISUZU							82	//Isuzu
#define LPR_LOGO_INFINITI						83	//Infiniti
#define LPR_LOGO_YUEJIN							84	//Leap forward
#define LPR_LOGO_NAVECO							85	//Iveco
#define LPR_LOGO_YUTONGBUS						86	//Yutong
#define LPR_LOGO_ZHONGTONGBUS					87	//Zhongtong bus
#define LPR_LOGO_JINLING						88	//Jinling
#define LPR_LOGO_SUNWIN							89	//Shenwo bus
#define LPR_LOGO_YANGTSE						90	//Yangtze River
#define LPR_LOGO_LUXGEN							91	//Satisfied Chi Jie
#define LPR_LOGO_TKING							92	//Tang Jun Ou Ling
#define LPR_LOGO_FOTON2							93	//Fukuda2
#define LPR_LOGO_KAMA							94	//Kema
#define LPR_LOGO_FORLAND						95	//Times Fukuda
#define LPR_LOGO_BMC							96	//BMC
#define LPR_LOGO_DS								97	//DS
#define LPR_LOGO_WEY							98	//WEY
#define LPR_LOGO_SANY_HEAVY						99	//Sany Heavy Industries
#define LPR_LOGO_DF_LIUQI						100	//Dongfeng Liuqi
#define LPR_LOGO_ZTE							101	//Dongfeng demeanor
#define LPR_LOGO_ZHONGSHUN						102	//Dongfeng demeanor
#define LPR_LOGO_WUZHENG						103	//Dongfeng demeanor
#define LPR_LOGO_ZHONGTAI						104	//Dongfeng demeanor
#define LPR_LOGO_CHUANQI						105	//Dongfeng demeanor
#define LPR_LOGO_KAICHI							106	//Dongfeng demeanor
#define LPR_LOGO_BEIBEN							107	//Dongfeng demeanor
#define LPR_LOGO_BEIZHIZAO						108	//Dongfeng demeanor
#define LPR_LOGO_BEIHUANSU						109	//Dongfeng demeanor
#define LPR_LOGO_BEIQIAHENBAO					110	//Dongfeng demeanor
#define LPR_LOGO_HUAPU							111	//Dongfeng demeanor
#define LPR_LOGO_HUATAI							112	//Dongfeng demeanor
#define LPR_LOGO_HUALING						113	//Dongfeng demeanor
#define LPR_LOGO_FRIENDSHIP						114	//Dongfeng demeanor
#define LPR_LOGO_DOUBLE_RING					115	//Dongfeng demeanor
#define LPR_LOGO_SHUANGLONG						116	//Dongfeng demeanor
#define LPR_LOGO_QICHEN							117	//Dongfeng demeanor
#define LPR_LOGO_DAYU							118	//Dongfeng demeanor
#define LPR_LOGO_DAYONG							119	//Dongfeng demeanor
#define LPR_LOGO_PENTIUM						120	//Dongfeng demeanor
#define LPR_LOGO_BAOVO							121	//Dongfeng demeanor
#define LPR_LOGO_BENTLEY						122	//Dongfeng demeanor
#define LPR_LOGO_GQAOJI							123	//Dongfeng demeanor
#define LPR_LOGO_CARRAY							125	//Dongfeng demeanor
#define LPR_LOGO_HUMMER							126	//Dongfeng demeanor
#define LPR_LOGO_SIWEI							127	//Dongfeng demeanor
#define LPR_LOGO_SHIFENG						128	//Dongfeng demeanor
#define LPR_LOGO_OUBAO							129	//Dongfeng demeanor
#define LPR_LOGO_BISU							130	//Dongfeng demeanor
#define LPR_LOGO_BIYUAN							131	//Dongfeng demeanor
#define LPR_LOGO_FUDI							132	//Dongfeng demeanor
#define LPR_LOGO_HONGYAN						133	//Dongfeng demeanor
#define LPR_LOGO_LIANHUA						134	//Dongfeng demeanor
#define LPR_LOGO_XIYATE							135	//Dongfeng demeanor
#define LPR_LOGO_JIEFANG						136	//Dongfeng demeanor
#define LPR_LOGO_LAND_ROVER						137	//Dongfeng demeanor
#define LPR_LOGO_QINGQI							138	//Dongfeng demeanor
#define LPR_LOGO_MUSTANG						139	//Dongfeng demeanor
#define LPR_LOGO_CAHGNANSY						140	//Dongfeng demeanor
#define LPR_LOGO_LUFENG							141	//Dongfeng demeanor
#define LPR_LOGO_QINGIAN						142	//Dongfeng demeanor
#define LPR_LOGO_LINGYU							143	//Dongfeng demeanor
#define LPR_LOGO_FENGXING						144	//Dongfeng demeanor
#define LPR_LOGO_FENGCHI						145	//Dongfeng demeanor
#define LPR_LOGO_FEIDIE							146	//Dongfeng demeanor
#define LPR_LOGO_DF_WELL_OFF					147	//Dongfeng Well-off
#define LPR_LOGO_DF_DEMEANOR					148	//Dongfeng demeanor
#define LPR_LOGO_DF_WIND_GOD					149	//Dongfeng demeanor
#define LPR_LOGO_KAIYI							150	//Dongfeng demeanor
#define LPR_LOGO_HENGTIAN						151	//Dongfeng demeanor
#define LPR_LOGO_TESLA							152	//Dongfeng demeanor
#define LPR_LOGO_SHENLONG						153	//Dongfeng demeanor
#define LPR_LOGO_JINMA							154	//Dongfeng demeanor
#define LPR_LOGO_SMART2							155	//SMART
#define LPR_LOGO_JIAO							156	//Dongfeng demeanor
#define LPR_LOGO_JEEP2							157	//Dongfeng demeanor
#define LPR_LOGO_GMC							158	//Dongfeng demeanor
#define LPR_LOGO_TANGJUNQULING					159	//Dongfeng demeanor
#define LPR_LOGO_TIMEFUKUDA						160	//Dongfeng demeanor
#define LPR_LOGO_FIELD							161	//Dongfeng demeanor
#define LPR_LOGO_MINI2							162	//Dongfeng demeanor
#define LPR_LOGO_SHENBAO						163	//Dongfeng demeanor

/**********************************************************************************************/
/* Currently supported illegal types */
#define LPR_IT_UNKOWN			0,     //未知类型
#define LPR_IT_NOSAFETYBELT		1,    //Do not wear seat belts
#define LPR_IT_PHONE			(1<< 1)         // Call
#define LPR_IT_NOPRESSLINE		(1<< 2)
#define LPR_IT_NOILL			(1<< 3)
#define LPR_IT_NOOVERLINE		(1<< 4)
#define LPR_IT_NORED			(1<< 5)
#define LPR_IT_NOCONTRAYDIRET	(1<< 6)
#define LPR_IT_PLATEASKEW		(1<< 7)
#define LPR_IT_SUNVISORMAIN		(1<< 8)
#define LPR_IT_SUNVISORSEC		(1<< 9)
#define LPR_IT_BIGTRUCKLIMIT	(1<< 10)
/**********************************************************************************************/

/**********************************************************************************************/
/* AVPlay video and audio render types */
#define CMD_AV_MODE_MIN							0
#define CMD_VIDEO_SHOW_MAIN_MODE		CMD_AV_MODE_MIN + 0		// unsupported
#define CMD_VIDEO_SHOW_SUB_MODE			CMD_AV_MODE_MIN + 1		// unsupported
#define CMD_AV_DEC_SHOW_MODE			CMD_AV_MODE_MIN + 2		// unsupported
#define CMD_AV_DEC_HWDECODE_PARAM		CMD_AV_MODE_MIN + 3		// HwDecodeParam
#define CMD_AVMODE_VIDEO_DECLIB			CMD_AV_MODE_MIN + 4		// unsupported
#define CMD_AVMODE_AUDIOCOMMON_DECLIB	CMD_AV_MODE_MIN + 5		// unsupported
#define CMD_AVMODE_AUDIOAAC_DECLIB		CMD_AV_MODE_MIN + 6		// unsupported
#define CMD_AVMODE_AUDIO_PLAY_MODE		CMD_AV_MODE_MIN + 7		// unsupported
#define CMD_AVMODE_RENDER_PARA			CMD_AV_MODE_MIN + 8		// set render para
#define CMD_AVMODE_GET_SM3_STATE		CMD_AV_MODE_MIN + 9
#define CMD_AVMODE_SET_SM3_CLOSE		CMD_AV_MODE_MIN + 10
#define CMD_AV_MODE_MAX					CMD_AV_MODE_MIN + 11

#define CMD_AV_MODE_GET_MIN				0
#define CMD_AV_MODE_GET_HD_ENABLE		(CMD_AV_MODE_GET_MIN + 0)
#define CMD_AV_MODE_GET_SM3_STATE		(CMD_AV_MODE_GET_MIN + 1)
#define CMD_AV_MODE_GET_MAX				(CMD_AV_MODE_GET_MIN + 2)

#define SHOW_MAIN_MODE_DRAW		0	//draw Display, support by windows
#define SHOW_MAIN_MODE_D3D		1	//d3d Display, support by windows
#define SHOW_MAIN_MODE_SDL		4	//sdl display, support by linux
#define SHOW_MAIN_MODE_OPENGL	5	//opengl display, support by linux & windows
#define SHOW_MAIN_MODE_TEXTURE	6	//d3d texture display support by windows
#define SHOW_MAIN_MODE_ANDROID  7   // android
#define SHOW_MAIN_MODE_IOS      8   // apple ios(iphone/ipad)
#define SHOW_MAIN_MODE_D3D11    10  // d3d11 Display, support by windows

#define SHOW_SUB_MODE_YUV420	0	//420 Display
#define SHOW_SUB_MODE_YUV422	1	//422 Display

#define COMMON_AUDIO_DLIB_HISI				0
#define COMMON_AUDIO_DLIB_OPENSOURCE		1
#define AAC_AUDIO_DLIB_FAAD					0
#define AAC_AUDIO_DLIB_FFMPEG				1

#define HWDEC_ENABLE_STREAM	0x1
#define HWDEC_ENABLE_VOD	0x2
#define HWDEC_ENABLE_FILE	0x4

#define HWDEC_LIMIT_LOCAL	0
#define HWDEC_LIMIT_GLOBAL	1

#define HWDEC_DECODE_TYPE_AUTO			0
#define HWDEC_DECODE_TYPE_D3D11VA		1
#define HWDEC_DECODE_TYPE_DXVA			2

#define VIDEO_DLIB_HISI						0
#define VIDEO_DLIB_FFMPEG					1
#define VIDEO_DLIB_CUDA						2
#define VIDEO_DLIB_VAAPI					3
#define VIDEO_DLIB_D3D11VA					4

#define COMMON_AUDIO_DLIB_HISI				0
#define COMMON_AUDIO_DLIB_OPENSOURCE		1
#define AAC_AUDIO_DLIB_FAAD					0
#define AAC_AUDIO_DLIB_FFMPEG				1

typedef struct tagAVDecodeMode
{
	int			iSize;
	int			iVideoDecLibType;			//Video Decoder Library Types：Hisi，Ffmpeg
	int			iCommonAudioDecLibType;		//Universal Audio Decoder Library Type: Hisi, Open Source Library
	int			iAACAudioDecLibType;		//Types of AAC Audio Decoder Library：faad，Ffmpeg
	int			iVideoShowMainMode;			//Main mode of video display：Draw，D3D
	int			iVideoShowSubMode;			//Video Display Submode：Yuv420，Yuv422
	int			iVideoDecHisiLibType;		//Valid When iVideoDecLibType is Hisi, 0-StreamMode 1-FrameMode 
} AVDecodeMode, * pAVDecodeMode;

//CMD_AV_DEC_HWDECODE_PARAM
typedef struct tagHwDecodeParam
{
	int iSize;
	int iLimitType; //HWDEC_LIMIT_LOCAL or HWDEC_LIMIT_GLOBAL 
	int iMaxCount;  //if iLimitType == HWDEC_LIMIT_GLOBAL, iMaxCount should less than 2
	int iEnableFlag;//HWDEC_ENABLE_STREAM | HWDEC_ENABLE_VOD | HWDEC_ENABLE_FILE
	int iDecodeType;//HWDEC_DECODE_TYPE_AUTO or HWDEC_DECODE_TYPE_D3D11VA or HWDEC_DECODE_TYPE_DXVA
}HwDecodeParam;
/**********************************************************************************************/

/* struct of vca */                                                                 
typedef struct tagvca_TRect
{
	unsigned short	usLeft;
	unsigned short	usTop;
	unsigned short	usRight;
	unsigned short	usBottom;
} vca_TRect, *pvca_TRect;

//用户数据算法类型，比网络协议智能分析算法事件类型大1
#define ALG_TRIPWIRE						1
#define ALG_DOUBLE_TRIP						2
#define ALG_PERIMETER_DERECTION				3 
#define ALG_LINGER							4 
#define ALG_PARK							5 
#define ALG_RUN								6
#define ALG_PERSONNEL_DENSITY				7
#define ALG_DEREKUCT						8
#define ALG_STOLEN							9 
#define ALG_FACE_DETECTION					10
#define ALG_VIDEO_DIAGNOSIS					11
#define ALG_INTELLIGENT_TRACKING			12
#define ALG_FLOW_STATISTICS					13
#define ALG_CROWD							14
#define ALG_POST_DETECTION					15
#define ALG_WATER_LEVEL_MONITORING			16
#define ALG_AUDIO_DIAGNOSIS					17
#define ALG_MOSAIC							18
#define ALG_REVER_FLOATER					19
#define ALG_STEALING_UNLOADING				20
#define ALG_ILLEGAL_PARKING					21 
#define ALG_FIGHT							22
#define ALG_ALERT							23 
#define ALG_LPR	

//智能分析事件类型统一定义（智能分析算法参数及报警使用）
typedef int vca_EEventType;
#define VCA_EVENT_MIN									0
#ifndef VCA_EVENT_TRIPWIRE
#define VCA_EVENT_TRIPWIRE						(VCA_EVENT_MIN + 0)  // 绊线
#endif
#ifndef VCA_EVENT_DBTRIPWIRE
#define VCA_EVENT_DBTRIPWIRE					(VCA_EVENT_MIN + 1)  // 双绊线
#endif
#ifndef VCA_EVENT_PERIMETER
#define VCA_EVENT_PERIMETER						(VCA_EVENT_MIN + 2)  // 周界
#endif
#define VCA_EVENT_LOITER						(VCA_EVENT_MIN + 3)  // 徘徊
#define VCA_EVENT_PARKING						(VCA_EVENT_MIN + 4)  // 停车
#define VCA_EVENT_RUN							(VCA_EVENT_MIN + 5)  // 奔跑
#define VCA_EVENT_HIGH_DENSITY					(VCA_EVENT_MIN + 6)  // 人员密度
#define VCA_EVENT_ABANDUM						(VCA_EVENT_MIN + 7)  // 遗弃物事件7
#define VCA_EVENT_OBJSTOLEN						(VCA_EVENT_MIN + 8)  // 被盗物事件8
#define VCA_EVENT_FACEREC						(VCA_EVENT_MIN + 9)  // 人脸识别
#define VCA_EVENT_VIDEODETECT					(VCA_EVENT_MIN + 10) // 视频诊断
#define VCA_EVENT_TRACK							(VCA_EVENT_MIN + 11) // 目标跟踪
#define VCA_EVENT_TRACE							(VCA_EVENT_MIN + 11) // 目标跟踪
#define VCA_EVENT_FLUXSTATISTIC					(VCA_EVENT_MIN + 12) // 人流统计
#define VCA_EVENT_CROWD							(VCA_EVENT_MIN + 13) // 人员聚集
#define VCA_EVENT_LEAVE_DETECT					(VCA_EVENT_MIN + 14) // 离岗检测
#ifndef VCA_EVENT_WATER_LEVEL_DETECT
#define VCA_EVENT_WATER_LEVEL_DETECT			(VCA_EVENT_MIN + 15) // 水位监测
#endif
#define VCA_EVENT_AUDIO_DIAGNOSE				(VCA_EVENT_MIN + 16) // 音频检测
#define VCA_EVENT_FACE_MOSAIC					(VCA_EVENT_MIN + 17) // 人脸遮挡(马赛克)
#define VCA_EVENT_RIVERCLEAN					(VCA_EVENT_MIN + 18) // 河道漂浮物
#define VCA_EVENT_DREDGE						(VCA_EVENT_MIN + 19) // 盗采盗卸
#define VCA_EVENT_ILLEAGEPARK					(VCA_EVENT_MIN + 20) // 违章停车
#define VCA_EVENT_FIGHT							(VCA_EVENT_MIN + 21) // 打架
#define VCA_EVENT_PROTECT						(VCA_EVENT_MIN + 22) // 警戒
#define VCA_EVENT_PLATE_RECOGNISE				(VCA_EVENT_MIN + 23) // 车牌识别(仅NVR使用)
#define VCA_EVENT_HEAT_MAP						(VCA_EVENT_MIN + 24) // 热度图
#define VCA_EVENT_SEEPER						(VCA_EVENT_MIN + 25) // 积水监测
#define VCA_EVENT_WINDOW_DETECTION				(VCA_EVENT_MIN + 26) // 翻窗检测
#define VCA_EVENT_STFACEADVANCE					(VCA_EVENT_MIN + 27) // ST人脸识别
#define VCA_EVENT_PARK_GUARD					(VCA_EVENT_MIN + 28) // 车位看守
#define VCA_EVENT_TYPE_UNDEFINE					(VCA_EVENT_MIN + 29) // 未定义
#ifndef VCA_EVENT_HELMET
#define VCA_EVENT_HELMET						(VCA_EVENT_MIN + 30) // 安全帽检测
#endif
#define VCA_EVENT_LINK_DOME_TRACK				(VCA_EVENT_MIN + 31) // 鱼球联动跟踪
#define VCA_EVENT_SLUICEGATE					(VCA_EVENT_MIN + 32) // 闸门检测
#define VCA_EVENT_COLOR_TRACK					(VCA_EVENT_MIN + 33) // 颜色跟踪
#define VCA_EVENT_FORMAT_TYPE					(VCA_EVENT_MIN + 34) // 结构化算法
#define VCA_EVENT_SEDIMENT						(VCA_EVENT_MIN + 35) // 积水深度
#define VCA_EVENT_ALERTWATER                	(VCA_EVENT_MIN + 36) // 警戒水位检测 
#define VCA_EVENT_SINGLE_INQUIRY            	(VCA_EVENT_MIN + 37) // 单人询问/无人看管
#define VCA_EVENT_CLIMB_UP                  	(VCA_EVENT_MIN + 38) // 攀高
#define VCA_EVENT_NET_DEPARTURE             	(VCA_EVENT_MIN + 39) // 新离岗
#define VCA_EVENT_ABNORMAL_NUMBER           	(VCA_EVENT_MIN + 40) // 人数异常
#define VCA_EVENT_GET_UP						(VCA_EVENT_MIN + 41) // 人员起身
#define VCA_EVENT_LEAVE_BED                 	(VCA_EVENT_MIN + 42) // 离床
#define VCA_EVENT_STATIC_DETECTION          	(VCA_EVENT_MIN + 43) // 静止检测
#define VCA_EVENT_SLEEP_POSTION             	(VCA_EVENT_MIN + 44) // 睡岗
#define VCA_EVENT_SLIP_UP						(VCA_EVENT_MIN + 45) // 摔倒
#define VCA_EVENT_NEW_FIGHT                 	(VCA_EVENT_MIN + 46) // 新打架
#define VCA_EVENT_BODY_TOUCH                	(VCA_EVENT_MIN + 47) // 肢体接触
#define VCA_EVENT_HUMAN_DETECT              	(VCA_EVENT_MIN + 48) // 人形检测
#define VCA_EVENT_DAM_AMARM                 	(VCA_EVENT_MIN + 49) // 坝前堆积物检测
#define VCA_EVENT_NET_AMARM                 	(VCA_EVENT_MIN + 50) // 站前拦网堆积物检测
#ifndef VCA_EVENT_VCA_PEPT
#define VCA_EVENT_VCA_PEPT	                	(VCA_EVENT_MIN + 51) // 油田监控
#endif
#define VCA_EVENT_VCA_FLOWSPEED             	(VCA_EVENT_MIN + 52) // 水流速检测
#define VCA_EVENT_BEACON_SHIP					(VCA_EVENT_MIN + 53) // 航标船
#ifndef VCA_EVENT_BRIGHT_KITCHEN
#define VCA_EVENT_BRIGHT_KITCHEN				(VCA_EVENT_MIN + 54) // 明厨亮灶
#endif
#define VCA_EVENT_STRANDED						(VCA_EVENT_MIN + 55) // 滞留
#define VCA_EVENT_SINGLE_ALONE					(VCA_EVENT_MIN + 56) // 单人独处
#define VCA_EVENT_WINDOW_DELIVERY				(VCA_EVENT_MIN + 57) // 隔窗递物
#ifndef VCA_EVENT_ZHONGYI
#define VCA_EVENT_ZHONGYI   					(VCA_EVENT_MIN + 58) // 吸烟
#endif
#define VCA_EVENT_SMOKE          				(VCA_EVENT_MIN + 58) // 吸烟
#define VCA_EVENT_WEAR_MASK						(VCA_EVENT_MIN + 59) // 戴口罩
#define VCA_EVENT_NOT_WEAR_MASK					(VCA_EVENT_MIN + 60) // 未戴口罩
#define VCA_EVENT_PHONE                     	(VCA_EVENT_MIN + 61) // 打电话
#ifndef VCA_EVENT_EVETEMDETECT
#define VCA_EVENT_EVETEMDETECT              	(VCA_EVENT_MIN + 62) // 环境温度检测
#endif
#define VCA_EVENT_TEMDETECT  					(VCA_EVENT_MIN + 63) // 人体温度检测
#ifndef VCA_EVENT_FIREWORKDETECT
#define VCA_EVENT_FIREWORKDETECT            	(VCA_EVENT_MIN + 64) // 烟火检测
#endif
#define VCA_EVENT_PLATENUMBER_BLACKLIST     	(VCA_EVENT_MIN + 65) // 车牌黑名单
#define VCA_EVENT_SMART_MOVE					(VCA_EVENT_MIN + 66) // 智能侦测
#define VCA_EVENT_INUIRY_TIMEOUT				(VCA_EVENT_MIN + 67) // 讯问超时
#define VCA_EVENT_ELECTRIC_VEHICLE				(VCA_EVENT_MIN + 68) // 室内电动车检测
#define VCA_EVENT_LEAVE_SEAT					(VCA_EVENT_MIN + 69) // 对象离席
#define VCA_EVENT_SCENE_REC						(VCA_EVENT_MIN + 70) // 场景分类
#define VCA_EVENT_CONTRA_BAND					(VCA_EVENT_MIN + 71) // 违禁物品
#define VCA_EVENT_BED_REST						(VCA_EVENT_MIN + 72) // 未定时休息
#define VCA_EVENT_ATTENDED 						(VCA_EVENT_MIN + 73) // 如厕无人看护
#define VCA_EVENT_DOOR							(VCA_EVENT_MIN + 74) // 谈话时未关闭房门
#define VCA_EVENT_POSEREC						(VCA_EVENT_MIN + 75) // 长期举手/长期站立/长期下蹲
#define VCA_EVENT_CONVERSE						(VCA_EVENT_MIN + 76) // 逆行
#define VCA_EVENT_COURTPII						(VCA_EVENT_MIN + 77) // 智能审讯
#define VCA_EVENT_COURTELP						(VCA_EVENT_MIN + 78) // 执法检人
#define VCA_EVENT_BEHAVIREC						(VCA_EVENT_MIN + 79) // 行为识别（吸烟和/或打电话）
#define VCA_EVENT_INCLINED_STATIS				(VCA_EVENT_MIN + 80) // 倾斜式人数统计
#define VCA_EVENT_VERTICAL_STATIS				(VCA_EVENT_MIN + 81) // 垂直式人数统计
#define VCA_EVENT_PERSON_GATHER					(VCA_EVENT_MIN + 82) // (监管)人员聚集 
#define VCA_EVENT_NROMAL_BODY_TEMPERATURE		(VCA_EVENT_MIN + 83) // 人体温度正常
#define VCA_EVENT_PERSON_DENSITY				(VCA_EVENT_MIN + 84) // 人员密度
#define VCA_EVENT_VEHICLE_DENSITY				(VCA_EVENT_MIN + 85) // 车辆密度
#define VCA_EVENT_TAFFIC_JAM					(VCA_EVENT_MIN + 86) // 车辆拥堵
#define VCA_EVENT_VEHICLE_STANDED				(VCA_EVENT_MIN + 87) // 车辆滞留
#define VCA_EVENT_ABNORMAL_PARKING				(VCA_EVENT_MIN + 88) // 异常停车
#define VCA_EVENT_CROSS_CONGESTION				(VCA_EVENT_MIN + 89) // 交叉拥堵
#define VCA_EVENT_JUDGE_BEHAVIOR_ANALYZE		(VCA_EVENT_MIN + 90) // 法官行为分析
#define VCA_EVENT_VERTICALHUMAN_DETECTION		(VCA_EVENT_MIN + 91) // 垂直检人
#define VCA_EVENT_AERIAL_PROJECTILE				(VCA_EVENT_MIN + 92) // 高空抛物
#define VCA_EVENT_WATER_OUTFALL					(VCA_EVENT_MIN + 93) // 排污口监测
#ifndef VCA_EVENT_POLICE_UNIFORM_DETECTION
#define VCA_EVENT_POLICE_UNIFORM_DETECTION		(VCA_EVENT_MIN + 94) // 民警警服检测
#endif
#ifndef VCA_EVENT_SPDRESS_DETECTION
#define VCA_EVENT_SPDRESS_DETECTION				(VCA_EVENT_MIN + 95) // 被监管人员识别服检测
#endif
#define VCA_EVENT_SPQUEUE_DETECTION				(VCA_EVENT_MIN + 96) // 被监管人员队列检测
#define VCA_EVENT_VEHICLE_IDENTIFY				(VCA_EVENT_MIN + 97) // 车辆识别
#ifndef VCA_EVENT_ACTIVE_STRATEGY
#define VCA_EVENT_ACTIVE_STRATEGY				(VCA_EVENT_MIN + 98) // 主动策略
#endif
#define VCA_EVENT_RESERVE_99				    (VCA_EVENT_MIN + 99) // 占位
#define VCA_EVENT_RESERVE_100				    (VCA_EVENT_MIN + 100) // 占位
#define VCA_EVENT_RESERVE_101				    (VCA_EVENT_MIN + 101) // 占位
#define VCA_EVENT_AUDIO_LOST_ALARM				(VCA_EVENT_MIN + 102) // 音频丢失报警
#define VCA_EVENT_CLIMB_WALL				    (VCA_EVENT_MIN + 103) // 翻墙检测
#define VCA_EVENT_CLASSROOM_BEHAVIOR_RECOGNITION	(VCA_EVENT_MIN + 104) // 课堂行为识别
#define VCA_EVENT_WATER_COLOR_DETECT				(VCA_EVENT_MIN + 105) // 水体颜色检测
#define VCA_EVENT_SLEEP_ABNORMAL				(VCA_EVENT_MIN + 106) // 106-睡觉异常检测
#define VCA_EVENT_PLAY_PHONES					(VCA_EVENT_MIN + 107) // 107-玩手机检测
#define VCA_EVENT_HUMANIOD_DETECT				(VCA_EVENT_MIN + 108) // 108-后端结构化算法-人形
#define VCA_EVENT_VEHICLE_DETECT				(VCA_EVENT_MIN + 109) // 109-后端结构化算法-机动车辆
#define VCA_EVENT_NON_MOTOR_DETECT				(VCA_EVENT_MIN + 110) // 110-后端结构化算法-非机动车
#define VCA_EVENT_PLATE_WHITELIST				(VCA_EVENT_MIN + 111) // 111-车牌白名单
#define VCA_EVENT_NUMBER_DETECTION				(VCA_EVENT_MIN + 112) // 112-人数检测
#define VCA_EVENT_IMAGE_ANALYSIS_DETECTION		(VCA_EVENT_MIN + 113) // 113-图片分析检测，涉及车辆，非机动车，人脸人体属性等
#define VCA_EVENT_AUDIO_ANALYSIS				(VCA_EVENT_MIN + 114) // 114-音频分析
#define VCA_EVENT_PERSON_FAST_MOVE				(VCA_EVENT_MIN + 115) // 115-人员快速移动
#define VCA_EVENT_COLLISION_DETECTION			(VCA_EVENT_MIN + 116) // 116-冲撞检测
#define VCA_EVENT_ESCORT_ANOMALY				(VCA_EVENT_MIN + 117) // 117-押解异常
#define VCA_EVENT_MEAL_DETECTION				(VCA_EVENT_MIN + 118) // 118-用餐检测
#define VCA_EVENT_POLICE_UNIFORMS_MIXED			(VCA_EVENT_MIN + 119) // 119-警便服混穿
#define VCA_EVENT_NON_STATIC_DETECTION			(VCA_EVENT_MIN + 120) // 120-非静止检测
#define VCA_EVENT_NOT_WEAR_POLICE_UNIFORMS		(VCA_EVENT_MIN + 121) // 121-未穿警便服
#define VCA_EVENT_TARGET_PARA					(VCA_EVENT_MIN + 122) // 122-靶标检测
#define VCA_EVENT_HUMAN_DETECT_MONITOR			(VCA_EVENT_MIN + 123) // 123-人形检测(智能监控)
#define VCA_EVENT_DISCIPLINE_INSPECTORS 		(VCA_EVENT_MIN + 124) // 124-执纪检人
#define VCA_EVENT_MOTION_DETECTION				(VCA_EVENT_MIN + 125) // 125-智能动检
#define VCA_EVENT_GAS_DETECTION				    (VCA_EVENT_MIN + 126) // 126-煤气罐检测
#define VCA_EVENT_TEST_PAPER_COUNTING			(VCA_EVENT_MIN + 127) // 127-保密室试卷清点
#define VCA_EVENT_TEST_PAPER_COUNTING_NUMBERS_LACK		(VCA_EVENT_MIN + 128) // 128-保密室试卷清点少于指定人数
#define VCA_EVENT_USING_ILLEGAL_COMMUNICATION_TOOLS		(VCA_EVENT_MIN + 129) // 129-使用违规通讯工具
#define VCA_EVENT_CARRY_THINGS_OUT				(VCA_EVENT_MIN + 130) // 130-携物外出
#define VCA_EVENT_MAX							(VCA_EVENT_MIN + 131) // invalid event ID, maximum limit

//智能分析算法类型统一定义（智能分析能力级使用）
#define VCA_ALG_BEHAVIRO_ANALYSIS				0  // 行为分析
#define VCA_ALG_PLATE_DISTINGUISH				1  // 车牌识别
#define VCA_ALG_FACE_DETECTION          		9  // 人脸检测
#define VCA_ALG_VIDEO_DIAGNOSIS         		10 // 视频诊断
#define VCA_ALG_INTELLIGENT_TRACKING    		11 // 智能跟踪
#define VCA_ALG_PEOPLE_STATISTICS       		12 // 人数统计
#define VCA_ALG_CROWD_GATHERED          		13 // 人群聚集
#define VCA_ALG_DUTY_INSPECTION         		14 // 值岗检测
#define VCA_ALG_AUDIO_DIAGNOSIS         		16 // 音频诊断
#define VCA_ALG_FACE_MOSAIC             		17 // 人脸遮挡马赛克
#define VCA_ALG_WRONG_PARKING           		20 // 违章停车
#define VCA_ALG_FIGHTING                		21 // 打架
#define VCA_ALG_FACE_DETECTION_ST       		22 // ST人脸检测
#define VCA_ALG_STAYWATER_MONITOR       		25 // 积水监测
#define VCA_ALG_OVERWINDOW_DETECTION    		26 // 翻窗检测
#define VCA_ALG_FACE_RECOGNITION        		27 // ST人脸识别
#define VCA_ALG_PARKING_GUARD           		28 // 车位看守
#define VCA_ALG_TARGET_DETECTION        		29 // 目标检出（背景建模）
#define VCA_ALG_HELMET_DETECTION        		30 // 安全帽检测
#define VCA_ALG_PTZ_TRACKING            		31 // 联动球机跟踪
#define VCA_ALG_COLOR_TRACKING          		33 // 颜色跟踪算法
#define VCA_ALG_STRUCTION               		34 // 结构化算法
#define VCA_ALG_STAYWATER_DEPTH         		35 // 积水深度
#define VCA_ALG_WATER_LEVEL_DETECTION   		36 // 警戒水位检测
#define VCA_ALG_UNATTENDED              		37 // 单人询问/无人看管
#define VCA_ALG_CLIMB_HIGHER            		38 // 攀高
#define VCA_ALG_NEW_DEPARTURE           		39 // 新离岗
#define VCA_ALG_PEOPLE_NUMBER_ABNORMAL  		40 // 人数异常
#define VCA_ALG_STAND_UP                		41 // 人员起身
#define VCA_ALG_OUT_BED                 		42 // 离床
#define VCA_ALG_STATIC_DETECTION        		43 // 静止检测
#define VCA_ALG_SLEEPING_STATION        		44 // 睡岗
#define VCA_ALG_FALL                    		45 // 摔倒
#define VCA_ALG_NEW_FIGHTING            		46 // 新打架
#define VCA_ALG_BODY_TOUCH              		47 // 肢体接触
#define VCA_ALG_HUMAN_SHAPE_DETECTION   		48 // 人形检测
#define VCA_ALG_DAM_DEPOSIT_DETECTION   		49 // 坝前堆积物检测
#define VCA_ALG_STATION_DEPOSIT_DETECTION		50 // 站前拦网堆积物检测
#define VCA_ALG_OILFIELD_MONITORING				51 // 油田监控
#define VCA_ALG_WATER_SPEED_DETECTION			52 // 水流速检测
#define VCA_ALG_NAVIGATION_MARK_SHIP			53 // 航标船
#define VCA_ALG_BRIGHT_KITCHEN              	54 // 明厨亮灶
#define VCA_ALG_RETENTION                   	55 // 滞留
#define VCA_ALG_SIGNALE_MAN                 	56 // 单人独处
#define VCA_ALG_WINDOW_DELIVERY             	57 // 隔窗递物
#define VCA_ALG_LINGER                      	58 // 徘徊
#define VCA_ALG_GOODS_LEFT                  	59 // 物品遗留
#define VCA_ALG_GOODS_LOST                  	60 // 物品丢失
#define VCA_ALG_HEAT_MAP                    	61 // 热度图
#define VCA_ALG_SMOKING                     	62 // 吸烟
#define VCA_ALG_PHONGING                    	63 // 打电话
#define VCA_ALG_TEMPERATURE_DETECTION       	64 // 温度检测
#define VCA_ALG_PYROTECHNIC_DETECTION       	65 // 烟火检测
#define VCA_ALG_INTELLIGENT_DETECTION       	66 // 智能侦测
#define VCA_ALG_INUIRY_TIMEOUT					67 // 讯问超时
#define VCA_ALG_ELECTRIC_VEHICLE				68 // 室内电动车检测
#define VCA_ALG_PERSON_DENSITY					84 // 人员密度
#define VCA_ALG_VEHICLE_DENSITY					85 // 车辆密度
#define VCA_ALG_TAFFIC_JAM						86 // 车辆拥堵
#define VCA_ALG_VEHICLE_STANDED					87 // 车辆滞留
#define VCA_ALG_ABNORMAL_PARKING				88 // 异常停车
#define VCA_ALG_CROSS_CONGESTION				89 // 交叉拥堵
#define VCA_ALG_JUDGE_BEHAVIOR_ANALYZE			90 // 法官行为分析
#define VCA_ALG_VERTICALHUMAN_DETECTION			91 // 垂直检人
#define VCA_ALG_AERIAL_PROJECTILE				92 // 高空抛物
#define VCA_ALG_WATER_OUTFALL					93 // 排污口监测
#define VCA_ALG_POLICE_UNIFORM_DETECTION		94 // 民警警服检测
#define VCA_ALG_SPDRESS_DETECTION				95 // 被监管人员识别服检测
#define VCA_ALG_SPQUEUE_DETECTION				96 // 被监管人员队列检测
#define VCA_ALG_ACTIVE_STRATEGY					97 // 主动策略
#define VCA_ALG_AUDIO_LOST_ALARM				98 // 音频丢失报警
#define VCA_ALG_CLIMB_WALL					    99 // 占位 翻墙检测 
#define VCA_ALG_CLASSROOM_BEHAVIOR_RECOGNITION	100 // 课堂行为识别

#define RENDER_WND_QTWIDGET			0		//Qt widget flag
#define RENDER_WND_QTLAYOUT			1		//Qt layout flag

typedef struct tagvca_TTargetInfo
{
	vca_TRect		rect;
	unsigned short	usType;  //ALG_SINGLE_TRIP ~ ALG_LPR
	unsigned short	usVelocity;
	unsigned short	usDirection;
	unsigned short	id;	
} vca_TTargetInfo, *pvca_TTargetInfo;

typedef struct tagvca_TTargetSet
{
	unsigned short	usCount;
	vca_TTargetInfo	targets[MAX_SAVE_TARGET_NUM];
} vca_TTargetSet, *pvca_TTargetSet;

//Used to store the number of statistical results
typedef struct tagTvca_cpcInfo
{
	short 			sCountIn;
	short 			sCountOut;
} Tvca_cpcInfo, *pTvca_cpcInfo;

typedef struct tagTvca_cpcInfoV2
{
	unsigned short	usValue;			
	unsigned short	usCountIn;
	unsigned short	usCountOut;
	unsigned short	usCountPass;
	unsigned short	usCountZone;
	unsigned short	usReserved[4];
}Tvca_cpcInfoV2, *pTvca_cpcInfoV2;

typedef struct tagTvca_CpcData
{
	Tvca_cpcInfo 	tCpcInfo;			//Statistical result
	vca_TTargetSet	tVCATarget;			//Destination location
	Tvca_cpcInfoV2  tCpcInfoV2;			//Statistical Result Expand
} UDCpcData, Tvca_CpcData, *pTvca_CpcData;

typedef struct tagTvca_RGB
{
	unsigned char 	ucR;
	unsigned char 	ucG;
	unsigned char 	ucB;
} Tvca_RGB, *pTvca_RGB;

typedef struct tagITS_vca_TRect
{
	int				iTop;
	int				iBottom;
	int				iLeft;
	int				iRight;
} ITS_vca_TRect, *pITS_vca_TRect;

typedef struct tagTvca_LPRInfo
{
	Tvca_RGB		stCarColor;				//Body color
	int				iPlateColor;			//license plate color
	int      		iCarType;				//vehicle type
	int	      		iPlateType;				//License plate type
	int				iBrandType;				//The type of the cursor
	int				iIllgalType;			//Illegal type
	ITS_vca_TRect	stRectPlate;			//License plate position rectangle
	ITS_vca_TRect	stRectFace;				//face rectangle
	ITS_vca_TRect	stRectLogo;				//vehicle coordinates rectangular
	char			cPlateRec[LPR_PLATE_STR_LEN];// license plate number
	int				iHeadTail;				//front or rear: 0 unknown, 1 front, 2 rear
	ITS_vca_TRect	stRectCopilotFace;		//co-pilot face coordinate rectangle
	ITS_vca_TRect	stCarRegion;			//vehicle profile
} Tvca_LPRInfo, *pTvca_LPRInfo;

typedef struct tagTvca_LPRData
{
	int				iNumOutputs;			//output the number of results
	int				iSize;					//Input Parameters: Enter a single Tvca_LPRInfo structure size
	Tvca_LPRInfo	stOutputs[MAX_OUTPUT_LPR_NUM];//Can output multiple results
} Tvca_LPRData, *pTvca_LPRData;

typedef struct tagZFUserMarkPara
{
	int m_iUserMarkType;	//type, 0-no mark, 1-focus mark, 2-audio mark(the last two parameters are valid)
	int m_iVideoChan;		//video channel
	int m_iAudioTag;		//0-no audio(audio end), 1-has audio(audio start)
} ZFUserMarkPara, *pZFUserMarkPara;

typedef struct tagUDTimestamp
{
	unsigned int	iTimeStamp;	
} UDTimestamp, *pUDTimestamp;

typedef struct tagVcaTarget
{
	unsigned short	iTagetSize;				//size of TargetAttr
	unsigned short	iTargetCount;			//target count
}VcaTarget, *pVcaTarget;

typedef struct tagTargetAttrInfo
{
	unsigned short	iAttrInfoSize;			//size of TargetAttrInfo
	unsigned short	iTargetId;
	unsigned short	iAttrParamNum;			//attr num,current max num is 24
}TargetAttrInfo, *pTargetAttrInfo;

typedef struct tagTargetAttrParam
{
	int				iEnable;
	int				iType;				
	int				iValue;
/*	iType -- iValue
	0. age
	1. Gender 0 female 1 male
	2. Nationality 0: Han nationality 1: Minority Nationality
	3. Quality 0-100
	4. Target type: 0 face, 1 motor vehicle, 2 non motor vehicle, 3 pedestrian, 4 figure (outline)
	5. Alarm status 1 - alarm, 0 - no alarm
	6. Scenario by network protocol
	7. Rules according to network protocol
	8. Alarm count is inserted according to actual parameters
	9. Whether to display the target box 0-display, 1-not display
	10. Algorithm type by network protocol
	11. Wearing a hat 0-none, 1-yes
	12. Long sleeve 0-none, 1-yes
	13. Beard 0-none, 1-yes
	14. Vehicle color 0-White 1-Red 2-yellow 3-yellow 4-Blue 5-Green 6- Green 7- Purple 8- Pink 9- Black 10- red 11- yellow 12- yellow 13- gray 14- yellow 15- blue 16- Blue 17- Green 18- Green 19- white 20- Green 21- cyan 22- yellow 23- red 24- Blue 25- Blue 26- gray 27- Purple 28- Purple 29- Brown 30- Brown 31- Brown 32- orange 33- light blue 34- color
	15. Vehicle manufacturer Id
	16. License plate number: UTF-8 according to fixed character code
	17. Draft reading = actual reading * 1000 Draft alarm = 0xFFFFFF
	18. Draft reading sign bit 0 positive 1 negative value
	19. Gate status 0-closed 1-open
	20 gate movement direction 0-up state 1-down state 2-static state
	21. Value range of gate opening degree [0-100]
	22 temperature value is the actual temperature * 100 + 100000, used with 23 
	23 Temperature unit: 1. Centigrade scale; 2. Fahrenheit scale
	*/
}TargetAttrParam, *pTargetAttrParam;


#define TARGET_ATTR_TYPE_AGE						0
#define TARGET_ATTR_TYPE_GENDER						1
#define TARGET_ATTR_TYPE_NATION						2
#define TARGET_ATTR_TYPE_QUALITY					3
#define TARGET_ATTR_TYPE_OBJECT						4
#define TARGET_ATTR_TYPE_ALARM_STATUS				5
#define TARGET_ATTR_TYPE_SCENE_NO					6
#define TARGET_ATTR_TYPE_RULE_NO					7
#define TARGET_ATTR_TYPE_ALARM_COUNT				8
#define TARGET_ATTR_TYPE_HIDE_TARGET				9
#define TARGET_ATTR_TYPE_VCA_TYPE					10
#define TARGET_ATTR_TYPE_GAUGE_DATA					17
#define TARGET_ATTR_TYPE_GAUGE_POSITIVE             18
#define TARGET_ATTR_TYPE_SLUICEGATE_STATE		    19
#define TARGET_ATTR_TYPE_SLUICEGATE_DIRECTION       20
#define TARGET_ATTR_TYPE_SLUICEGATE_OPENSIZE        21
#define TARGET_ATTR_TYPE_TEMP_VALUE                 22
#define TARGET_ATTR_TYPE_TEMP_UNIT                  23
#define TARGET_ATTR_TYPE_MASK		                24



typedef struct tagTargetAttr
{
	unsigned short	iTargetId;
	RECT			rect;
	int				iAttrParam[LEN_256];
	/*	tAttrParam[i] -- iValue
	0. age
	1. Gender 0 female 1 male
	2. Nationality 0: Han nationality 1: Minority Nationality
	3. Quality 0-100
	4. Target type: 0 face, 1 motor vehicle, 2 non motor vehicle, 3 pedestrian, 4 figure (outline)
	5. Alarm status 1 - alarm, 0 - no alarm
	6. Scenario by network protocol
	7. Rules according to network protocol
	8. Alarm count is inserted according to actual parameters
	9. Whether to display the target box 0-display, 1-not display
	10. Algorithm type by network protocol
	11. Wearing a hat 0-none, 1-yes
	12. Long sleeve 0-none, 1-yes
	13. Beard 0-none, 1-yes
	14. Vehicle color 0-White 1-Red 2-yellow 3-yellow 4-Blue 5-Green 6- Green 7- Purple 8- Pink 9- Black 10- red 11- yellow 12- yellow 13- gray 14- yellow 15- blue 16- Blue 17- Green 18- Green 19- white 20- Green 21- cyan 22- yellow 23- red 24- Blue 25- Blue 26- gray 27- Purple 28- Purple 29- Brown 30- Brown 31- Brown 32- orange 33- light blue 34- color
	15. Vehicle manufacturer Id
	16. License plate number: UTF-8 according to fixed character code
	17. Draft reading = actual reading * 1000 Draft alarm = 0xFFFFFF
	18. Draft reading sign bit 0 positive 1 negative value
	19. Gate status 0-closed 1-open
	20 gate movement direction 0-up state 1-down state 2-static state
	21. Value range of gate opening degree [0-100]
	22 temperature value is the actual temperature * 100 + 100000, used with 23 ,actual temperature = (tAttrParam[22]-100000)/100
	23 Temperature unit: 1. Centigrade scale; 2. Fahrenheit scale
	*/
}TargetAttr, *pTargetAttr;

typedef struct tagVca_TargetArrInfo
{
	int					iBufferSize;   //size of struct
	int					iTargetCount;  //target count
	int					iAttrLen;		//size of TargetAttr 
	TargetAttr*         pTargetAttr;	//attr info
}Vca_TargetArrInfo, *ptagVca_TargetArrInfo;

typedef struct
{
	int 	iPanPosition;		//Current horizontal coordinates 0-36,000 correspond to 0-360 degrees
	int		iTiltPosition;		//Current vertical coordinates 1000-19000 correspond to - 90-90 degrees
	int		iZoomPosition;		//Current multiplier 0-100000 corresponds to 0-1000 times
}TPtzInfo, *pTPtzInfo;

#define MAX_ID_NUM			128
#define MAX_TRACK_DATA_NUM  16
//双精度 3D高精度坐标点
typedef struct
{
	double  dx;   //点的x坐标
	double  dy;   //点的y坐标
	double  dz;   //点的z坐标
}Vca_Point3D;


typedef struct
{
	unsigned int	uiType;						/*0-碰撞识别结果（行业应用叠加气泡）1-轨迹识别结果（行业应用人员定位）*/
	unsigned int	uiFaceId;					/*轨迹id*/
	int				iRefid;						/*关联ID默认-666*/
	unsigned int	uiTargetType;				/*目标类型0人脸,3行人,4合成图,5,纯轨迹*/
	unsigned int	uiFrameNO;              	/*帧序号*/
	unsigned int	uiReserved;					//预留
	char		    cFaceJpegTime[LEN_8];		//图片的抓拍时间:年-月-日-星期-时-分-秒-毫秒
	char		    cTargetID[MAX_ID_NUM];   	/*识别目标在平台对应的ID，包含\0*/
	unsigned int	uiValid;					//坐标是否有效 0无效，1有效
	Vca_Point3D		stPoint3D;					/*高精度坐标位置（碰撞结果，此值无效）*/
	unsigned long long ullTimeStamp;			/*时间戳*/
}Vca_TrackData;

typedef struct
{	
	unsigned short		usStructSize;				/*单个轨迹结果结构体大小*/
	unsigned short		usDataType;		    		/*数据类型（0：轨迹服务器识别结果类型）*/
	unsigned short		usNums;  					/*识别结果个数*/
	Vca_TrackData		stTrackData[MAX_TRACK_DATA_NUM];			/*识别结果数据*/
}TrackCompareData;

typedef enum
{
	ESHA1 = 0, 
	ESHA2 = 1,
	ESHA256 = 2,
	EDH = 3,	//Diffie-hellman
	EMD5 = 4,
	EMODE_MAX
}EMode;	//摘要的加密方式

#define DIGITAL_DIGEST_SHA1		0 
#define DIGITAL_DIGEST_SHA2		1 
#define DIGITAL_DIGEST_SHA256	2 
#define DIGITAL_DIGEST_DH		3 //Diffie-hellman
#define DIGITAL_DIGEST_MD5		5 


#define MAX_DIGITAL_SUMMARY_LENGTH  256
typedef struct
{
	unsigned char	ucDigestEncodeMode;	//摘要加密方案
	unsigned char	ucEnCodeMethod;	//摘要二次加密方式,如:SHA1生成加密串以后从加密串的第几位开始截取,0-不截取
	unsigned short	usTruncateLen;  // iEnCodeMethod二次加密方式下截取多长, m_uiEnCodeMethod==0时此参数无效
	unsigned short	usMGEPSize;		//MGEP(minimum group exchange prime) 最小交换质数
	unsigned short  usEncryptedDataLen;	//摘要数据的数据长度
	char			cEncryptedData[MAX_DIGITAL_SUMMARY_LENGTH];	//摘要数据
}DigitalWaterMarkVideoData;

#define USERDATA_WATERMARK_CHARSET_UTF8		0
#define USERDATA_WATERMARK_CHARSET_GB2312	1

typedef struct
{
	unsigned char   ucDigestEncodeMode;	//cEncryptedData使用的加密算法
	unsigned char   ucCharSet;			//用户数据使用的字符集
	unsigned short	usEncryptedDataLen;	//用户配置的数据的数据长度
	char			cEncryptedData[MAX_DIGITAL_SUMMARY_LENGTH];	//用户配置内容,此内容需要用数字摘要内容(0xFEF2)中得到的摘要数据作为key去解密.
}DigitalWaterMarkUserData;

#define ADD_DATA_WATERMARK_MAX_MAC_LEN (128)   //MAC数据内容最大长度
#define ADD_DATA_WATERMARK_MAX_DEVMODEL_LEN (256)   //设备型号数据内容最大长度
typedef struct
{
	int					iMacDataLen;		//MAC数据内容长度
	int					iDevModelDataLen;	//设备型号数据内容长度
	unsigned long long	ullCurrentTime;		//当前时间，本地时间
	unsigned char		ucMac[ADD_DATA_WATERMARK_MAX_MAC_LEN];				//设备MAC内容
	unsigned char		ucDevModel[ADD_DATA_WATERMARK_MAX_DEVMODEL_LEN];    //设备型号内容
}WaterMarkAddDataInfo;

typedef struct
{
	unsigned short			usType;					//(0xFEFE)
	unsigned short			usSize;					// m_usSize = sizeof(m_ucEnCodeMode)+ sizeof(m_ucEnCodeType)+ sizeof(m_usAddDataLen)+sizeof(WatermarkAddData)
	unsigned char			ucDigestEncodeMode;		//附加数据使用的加密算法
	unsigned char			ucCharSet;				//附加数据使用的字符集
	unsigned short			usEncryptedDataLen;		//用户附加数据的数据长度
	WaterMarkAddDataInfo	stWaterMarkAddDataInfo;	//用户附加数据
}DigitalWaterMarkAddData;

#define PRISONERCLOTH_TYPE_UNKNOWN			0 //其它
#define PRISONERCLOTH_TYPE_RED				1 //红色
#define PRISONERCLOTH_TYPE_ORANGE			2 //橙色
#define PRISONERCLOTH_TYPE_YELLOW			3 //黄色
#define PRISONERCLOTH_TYPE_GREEN			4 //绿色
#define PRISONERCLOTH_TYPE_BLUE				5 //蓝色
#define PRISONERCLOTH_TYPE_MAX				6

#define CLOTH_TYPE_POLICE					1 //警服
#define CLOTH_TYPE_VEST						2 //被监管人员识别服

typedef struct
{
	int								iRegionNumber;	//区域编号
	int								iClothType;		//服装类型1-警服 2-被监管人员识别服；其他待扩展
	int								iClothColor;	//着装颜色类别
	int								iNum;			//对应着装人数
} PrisonerClothInfo;

typedef struct
{
	int								iNumOutputs;	//输出结果的数目，最大16
	PrisonerClothInfo				tAnalysInfo[LEN_16];
} UserDataPrisonerCloth;

#define HMAC_RESULT_BUFLEN  (64)   //加密数据大小不超过64字节
typedef struct
{
	unsigned short m_usEncryptedDataLen;				   //HMAC加密数据的数据长度
	char		   m_cEncryptedData[HMAC_RESULT_BUFLEN];   //计算的HMAC加密数据
}HmacResultData;


/**********************************************************************************************/


typedef struct _tagCurrentFrameInfo
{
	unsigned int	uiSize;					//size of struct
	unsigned int	uiFrameNO;				//frame index
	unsigned int	uiFrameType;			//frame type:I=0, P=1,B=2
	unsigned int	uiTimeStamp;			//time stamp 
} CurrentFrameInfo, *pCurrentFrameInfo;




/* Auto Test  */                                                                 
#define AUTOTEST_PARAM_SIZE			5
#define AUTOTEST_STRING_SIZE		65
typedef struct _strAutoTest_Para
{
	int			iTestParam[AUTOTEST_PARAM_SIZE];
	char		cTestParam[AUTOTEST_STRING_SIZE];
	char		cTestParam1[AUTOTEST_STRING_SIZE];
	int         iChannelNo;
}strAutoTest_Para;
/**********************************************************************************************/

typedef struct	//custom common information
{
	char			m_cParam1[LEN_64];
	char			m_cParam2[LEN_64];
	char			m_cParam3[LEN_64];
	char			m_cParam4[LEN_64];
	char    		m_cParam5[LEN_64];
	char    		m_cParam6[LEN_64];
	char    		m_cParam7[LEN_64];
	char    		m_cParam8[LEN_64];
	char    		m_cParam9[LEN_64];
	char    		m_cParam10[LEN_64];
	char    		m_cParam11[LEN_64];
	char    		m_cParam12[LEN_64];
	char    		m_cParam13[LEN_64];
	char    		m_cParam14[LEN_64];
	char    		m_cParam15[LEN_64];
	char    		m_cParam16[LEN_64];
	char    		m_cParam17[LEN_64];
	char    		m_cParam18[LEN_64];
	char    		m_cParam19[LEN_64];
	char    		m_cParam20[LEN_64];
}DZ_INFO_PARAM, *PDZ_INFO_PARAM;

/* NVSSDK, PlaySDKM4 Common structure  */                                                                  
typedef struct
{
    unsigned short  m_ulMajorVersion;
    unsigned short  m_ulMinorVersion;
    unsigned short  m_ulBuilder;
    char*           m_cVerInfo;
}SDK_VERSION;

#define MAX_MONITOR_DESCLEN			512
typedef struct MONITOR_INFO					
{
	char			cDriverDesc[MAX_MONITOR_DESCLEN];
	char			cDriverName[MAX_MONITOR_DESCLEN];	
	RECT			rect;
}MONITOR_INFO, *PMONITOR_INFO;

//Callback YUV data
#define AV_CBK_TYPE_VIDEO			0
#define AV_CBK_TYPE_AUDIO			1
#define AV_CBK_TYPE_VIDEO_CUDA		2
typedef struct tagDecAVInfo
{
	int				iType;		//0: video 1: audio；此type跟FRAME_INFO结构体里的nType值正好相反，混用的时候需要做转换不能直接赋值
	int				iFormat;
	int				iWidth;
	int				iHeight;
	const unsigned char *pucData;
	int iDataLen;
}DecAVInfo, *PDecAVInfo;

typedef struct tagDecVideoOtherInfo
{
	unsigned int	uiTimeStamp;
	int				iFrameRate;
	int				iReserved;		//use with frameType, FRAME_I=0,  FRAME_P=1, FRAME_B=2
	void*			pvUserPutData;
	int				iPitch;
	void*			pvCudaContext;
}DecVideoOtherInfo;

typedef struct tagAudioSampleValue
{ 
	int			iChannel;				//audio channel num                
	int			iMaxSampleValue1;		//audio channel 1 max sample value range[-300~300]
	int			iMinSampleValue1;		//audio channel 1 min sample value
	int			iIsMaxBeforeMin1;		//whether audio channel 1 max sample value appear before  min sample value
	int			iMaxSampleValue2;		//audio channel 2 max sample value 
	int			iMinSampleValue2;		//audio channel 2 min sample value
	int			iIsMaxBeforeMin2;		//whether audio channel 2 max sample value appear before  min sample value
}AudioSampleValue, *PAudioSampleValue;

typedef struct tagDecAudioOtherInfo
{
	unsigned int uiTimeStamp;
	int	iFrameRate;
	AudioSampleValue* ptAudioSampleValue;//audio sample value
	int iAudioSampleRate;//audio sample rate
	int iAudioChannelNum; //audio channel num
}DecAudioOtherInfo;

typedef struct tagDecAVInfoEx
{
	DecAVInfo 		m_pInfo;
	//judge below value type with m_iOtherInfoSize 1:TimeStamp(unsigned int), 2:FrameRate(int)
	int				m_iOtherInfoSize;		
	void*	m_pvOtherInfo;  //DecVideoOtherInfo* or DecAudioOtherInfo*

}DecAVInfoEx, *PDecAVInfoEx;

typedef struct tagPSPACK_INFO
{
	unsigned long	nWidth;
	unsigned long	nHeight;
	unsigned long	nStamp;
	unsigned long	nType;
	unsigned long	nFrameRate;
	unsigned long	nReserved;
} PSPACK_INFO, *PPSPACK_INFO;

typedef struct
{
	unsigned long	nWidth;				//Video width, audio data is 0
	unsigned long	nHeight;			//Video height, audio data is 0
	unsigned long	nStamp;				//Time stamp(ms)
	unsigned long	nType;				//Audio type, T_AUDIO8, T_YUV420; 此type跟DecAVInfo结构体里的iType值正好相反，混用的时候需要做转换不能直接赋值
	unsigned long	nFrameRate;			//Frame rate
	void*	nReserved;					//reserve
}FRAME_INFO;
/**********************************************************************************************/

/*vital signs instrument information collect   */                                                               
#define VITAL_SIGN_HR		1			//heart rate
#define VITAL_SIGN_BO		2			//blood oxygen concentration
#define VITAL_SIGN_BP		3			//blood pressure0
#define MAX_POINTS_NUM		512
typedef struct __tagVitalSignData 
{
	char			cVersion;			//treaty version
	char			cType;				//vital signs instrument information type,1：heart rate 2：blood oxygen concentration 3：blood pressure
	char			cMonFlg;			//1:normal 2:Upper limit 3：Ultra-low limit 4:probe off 5:device offline    
	char			iOscilloGramSeqID;	//从1开始，心电最多三波依次传递1，2，3，血氧一波，使用1
	int				iMonVal[4];			//体征实时值, 血压3个,SYS DIA PR依次存放，精度：原值乘以100;
	int				iGramRate;			//1s有多少个点
	int				iSizeType;			//视频叠加层大小  1:大 2：中 3：小
	int				iXPosition;			//相对于视频区左上角(0,0)的相对横坐标(万分比)
	int				iYPosition;			//相对于视频区左上角(0,0)的相对纵坐标(万分比)
	int				iPointLen;
	unsigned char	cPointValue[MAX_POINTS_NUM];
} VitalSignData, *pVitalSignData;

//frame absoluteTime time parameter

typedef struct __STDAbsoluteTime
{
    unsigned short     iYear;                //Year
    unsigned short     iMonth;                //Month
    unsigned short     iDay;                //Day
    unsigned short     iHour;                //Hour
    unsigned short     iMinute;            //Minute
    unsigned short     iSecond;            //Second
    unsigned int		uiTime;
} TDAbsoluteTime, *pTDAbsoluteTime;
#if !defined(IPHONE)
#define AbsoluteTime TDAbsoluteTime
#endif

typedef struct tagMsAbsoluteTime
{
	int				   iSize;
	unsigned int	   uiReserved;		  //预留占位，保证字节对接
	unsigned long long ullCurSecond;	  /*绝对时间的整数部分(单位：秒)*/
	unsigned long long ullCurNanoSecond;  /*绝对时间的小数部分（单位：纳秒）*/
} MsAbsoluteTime;

typedef struct __tagTStreamData
{
	int				iSize;
	char			cStreamData[64];
	int				iStreamLen;
} TStreamData, *PTStreamData;

typedef struct
{
	unsigned short	 	usPanPosition;   //当前水平坐标百分比0-100
	unsigned short		usTiltPosition;   //当前垂直坐标百分比0-100 
	unsigned short		usZoomPosition; //当前倍率百分比0-100
} TPtzPosInfo;

#define OSD_TYPE_CHANNELNAME		1
#define OSD_TYPE_TIMEDATE			2
#define OSD_TYPE_LOGOCOLOR			3
#define OSD_TYPE_ADDITIONALCHAR		4
#define OSD_TYPE_ITS				5
#define OSD_TYPE_ITS_COMBINEPIC		6
#define OSD_TYPE_DEBUGINFO      7

typedef struct
{
	unsigned int		uiOSDType;			//1：Channel name（Text） 2：Time and date 3:logo color 4：append osd  characters 5:traffic ipc exclusive use  6：traffic ipc combining picture
	unsigned int		uiOSDBGColor;		// back color, total 32 bit low 24 bit means color rgb,bgr in figure：0x00BBGGRR。High 8bit useless
	unsigned int		uiOSDColor;			//text color
	unsigned int		uiOSDSize;			// 1～5,total 32 bit low 16 bit means Height ，High 16 bit means width,0-Auto
	unsigned int		uiOSDDiaphaneity;	//diaphaneity  range【0～100】
	unsigned int		uiOSDPostionX;		//abscissa or location number
	//1：if uiOSDPostionY equal to 0 ；uiOSDPostionX means location number ，range【0～15】，
	//2：if uiOSDPostionY greater t 0；uiOSDPostionX and uiOSDPostionY combine a point means osd postion，uiOSDPostionY must plus 1 ，eg: want set(16,0)，network treaty send（16，1），ipc actual set（16，0）
	unsigned int		uiOSDPostionY;		
	int					ilength;
	char				cText[LEN_512];		//osd text content
} osd_TOSDSet;

typedef struct
{
	double f64Fx;
	double f64Fy;
	double f64Cx;
	double f64Cy;
	double af64K[12];
} UdtCameraPara;

typedef struct
{
	int					iType;		//0-reserveed 1-warehouse camera 2-fish camera
	int					ilength;
	UdtCameraPara		tUdtCameraPara;
} osd_TLDCSet;

typedef struct __tagTargetTrackStyle
{
	unsigned int	uiStructSize;		// 结构体大小 sizeof(TargetTrackInfo)
	unsigned int	uiStyleId;      	// 轨迹样式id，全局样式默认使用0,目标需要指定特定样式时，目标属性指定样式id
	unsigned int	uiLineWidth;		// 轨迹线宽度（万分比）
	unsigned int	uiColor;			// 轨迹颜色rgb，DEV_ALG_COLOR_TYPE_UNKNOWN ~ DEV_ALG_COLOR_TYPE_YELLOWGREEN
	unsigned int	uiDisplayDelay; 	// 延迟显示轨迹帧数, 默认10帧
	unsigned int	uiReserved[8];  	// 预留
} TargetTrackStyle, *pTargetTrackStyle;

/**********************************************************************************************/

typedef struct __tagExtraInfo
{
	int				m_iInfoLen;
	void*			m_pvExtInfo;	
}FRAME_EXT_INFO, *LPFRAME_EXT_INFO;

typedef struct _RECT_S 
{ 
	short 			sLeft; 
	short 			sTop; 
	short 			sRight; 
	short 			sBottom; 
}RECT_S, *LPRECT_S;

//Its Face Coordinate Structure
typedef struct _RECT_ITS 
{ 
	short 			sLeft; 
	short 			sRight; 
	short 			sTop; 
	short 			sBottom; 
}RECT_ITS, *LPRECT_ITS;

//Schedle time
typedef struct
{
	unsigned short  iStartHour;
	unsigned short  iStartMin;
	unsigned short  iStopHour;
	unsigned short  iStopMin;
	unsigned short  iRecordMode;			//iEnable;
}NVS_SCHEDTIME,*PNVS_SCHEDTIME;

typedef struct
{
	unsigned short  m_u16Brightness;
	unsigned short  m_u16Hue;
	unsigned short  m_u16Contrast;
	unsigned short  m_u16Saturation;
	NVS_SCHEDTIME strctTempletTime;
}STR_VideoParam;


#define TYPE_NVS_T          0     //T NVS
#define TYPE_KEYBOARD       1     //Net keyboard
#define TYPE_CTL_ALLOTER    2     //Control code distributor
#define TYPE_ALARMHOST      3     //Network alarm host
#define TYPE_NVS_S          4     //S NVS
#define TYPE_MATRIX         5     //Matrix
#define TYPE_NET_PU         6     //Network Front-end device
#define TYPE_NET_GURAD      7     //Net Entrance Gurad
#define TYPE_PC_DVR         8     //PC DVR
#define TYPE_S_DECODER      9     //S Decoder
#define TYPE_NVS_TPLUS      10    //TPLUS NVS
#define TYPE_WIH_S          11    //Unattended host
#define TYPE_EMBED_DVR      12    //embedded DVR
#define TYPE_SMS_200        13    //Linux stream media host
#define TYPE_ALARM_HOST42   14    //network alarm host
#define TYPE_TC_T820GE      15    //external trigger intersection camera
#define TYPE_TC_T860GE      16    //pure video intersection camera
#define TYPE_TC_T890GE      17    //external trigger ptz
#define TYPE_TC_HDC         18    //HD camera   
#define TYPE_ONVIF          19    //Onvif device 
#define TYPE_NVS_LG         20    //LG device 

/**********************************************************************************************
* public notify                                                            
**********************************************************************************************/
#ifndef __WIN__
typedef void (*pfCBGetDecAV)(int _iID, const DecAVInfo* _pDecAVInfo, void* _iUser);
#endif

typedef void (__stdcall *DEVUSERDATA_NOTIFY)(unsigned int _uiID, int _iType, void* _pvData
			 , int _iDataLen, CurrentFrameInfo* _ptInfo, void* _pvUdata);

//video and audio date after decoding
typedef void (*DECYUV_NOTIFY_V4)(unsigned int _ulID,unsigned char *_cData, int _iLen, 
								 const FRAME_INFO *_pFrameInfo, void* _iUser);
/**********************************************************************************************/

#define	ENABLE_LAST_FRAME   0x13001		//keep last frame when disconnect
#define ENABLE_PREFERENCE   0x13002		//preview preference set
#define ENABLE_HD_DISPLAY   0x13003		//4k hd display
#define ENABLE_TELNET		0x13004		//telnet
#ifndef MAX_NAME_LEN
#define MAX_NAME_LEN		64
#endif
#define FILE_COUNT			16	//max support num
#define FILE_COUNT_EX		8	

//存储参数导出文件名，支持所有设备 
//Storage parameters export file name, support all devices
#define DEVPARAMFILE_STORAGE				"config_rec.dat"

//报警参数导出文件名，支持所有设备
//Alarm parameter export file name, support all devices
#define DEVPARAMFILE_ALARM					"config_alm.dat"
#define DEVPARAMFILE_ALARM_CONFIG			"config_alarm.ini"
#define DEVPARAMFILE_ALARM_SERIAL_ALARM		"config_serialalarmercnf.dat"
#define DEVPARAMFILE_ALARM_NET_ALARM		"config_netalarmercnf.dat"

//预览参数导出文件名，支持所有设备
//Preview parameter export file name, support all devices
#define DEVPARAMFILE_PREVIEW_DISPLAY		"./gui_config/display.cnf"
#define DEVPARAMFILE_PREVIEW_SWITCH			"config_switch.dat"

//智能分析参数导出文件名，支持所有设备 
//Intelligent analysis parameter export file name, support all devices
#define DEVPARAMFILE_VCA					"config_vca.dat"
#define DEVPARAMFILE_SCENE					"config_scene.dat"
//智能分析参数-交通参数导出文件名，支持存储nvr系列和交通相机系列设备
//Intelligent analysis parameter export file name, only support to store NVR series device
#define	DEVPARAMFILE_ITS					"config_its.ini"
//智能分析参数导出文件名，仅支持存储nvr系列设备
//Intelligent analysis parameter export file name, only support to store NVR series device
#define DEVPARAMFILE_VCAEX					"config_vca_ex.dat"
#define DEVPARAMFILE_ITS_DAT				"config_its.dat"

//系统参数导出文件名，支持所有设备
//System parameters export file name, support all devices
#define DEVPARAMFILE_SYSTEM_SERVER			"config_server.ini"
#define DEVPARAMFILE_SYSTEM_EXTEND			"extendword.txt"
#define DEVPARAMFILE_SYSTEM_PTZ				"config_ptz.dat"
#define DEVPARAMFILE_SYSTEM_RIGHT			"config_right.dat"
#define DEVPARAMFILE_SYSTEM_DG				"config_dg.dat"
#define DEVPARAMFILE_SYSTEM_BURN			"config_burn.dat"
//系统参数导出文件名，仅支持SmartS3E系列设备
//System parameters export file name, only support smarts3e series devices
#define DEVPARAMFILE_SYSTEM_COVER			"config_pdcover.dat"
//系统参数导出文件名，仅支持多目相机系列
//System parameters export file name, only support multi camera series
#define DEVPARAMFILE_CHN0					"config_chn0.ini"
#define DEVPARAMFILE_NET					"config_net.ini"
//系统参数导出文件名，仅支持政法球系列
//System parameters export file name, only support political and legal ball series
#define DEVPARAMFILE_SYSTEM_DOMESETTING		"dome_cfg/dome_setting.dat"
#define DEVPARAMFILE_SCENE_PATTERN			"dome_cfg/pattern.dat"

typedef struct tagExportConfig
{
	int		iSize;		    //Size of the structure,must be initialized before used
	int		iCount;		    //需要导出的文件总个数	Total number of files to be exported      
	char   	cFileList[FILE_COUNT][MAX_NAME_LEN];	//需要导出的文件名称列表 List of file names to be exported
	char    cFileOut[LEN_128];  //导出到本地生成的box文件名 Export to locally generated box file name
	char   	cFileListEx[FILE_COUNT_EX][MAX_NAME_LEN];	//附加文件名称列表，超过16个文件的赋值给该变量 Attached file name list, more than 16 files assigned to this variable 
} ExportConfig  ,*PExportConfig;

typedef int RAWFRAMETYPE;
#define VI_FRAME 0
#define VP_FRAME 1
#define AUDIO_FRAME 5

#define RAW_VIDEO_H264		1
#define RAW_VIDEO_MPEG4		2
#define RAW_VIDEO_MJPEG		41
#define RAW_VIDEO_H265		23

#define RAW_AUDIO_G711_A	0x01
#define RAW_AUDIO_G711_U	0x02
#define RAW_AUDIO_ADPCM_A	0x03
#define RAW_AUDIO_AAC		0x16

typedef struct
{
	unsigned int nWidth;    //Video width, audio data is 0
	unsigned int nHeight;   //Video height, audio data is 0
	unsigned int nStamp;    //Time stamp(ms)
	unsigned int nType;     //RAWFRAMETYPE, I Frame:0,P Frame:1,B Frame:2,Audio:5
	unsigned int nEnCoder;  //Audio or Video encoder(Video,0:H263,1:H264, 2:MP4. Audio:0,G711_A:0x01,G711_U:0x02,ADPCM_A:0x03,G726:0x04)
	unsigned int nFrameRate;//Frame rate
	unsigned int nAbsStamp; //Absolute Time(s)
	unsigned char ucBitsPerSample;// bit per sample [8/16/24] default 16
	unsigned int uiSamplesPerSec;// Samples Per Sec，default 8000
}RAWFRAME_INFO;

typedef struct
{
	unsigned int nWidth;    //Video width, audio data is 0
	unsigned int nHeight;   //Video height, audio data is 0
	unsigned int nStamp;    //Time stamp(ms)
	unsigned int nType;     //RAWFRAMETYPE, I Frame:0,P Frame:1,B Frame:2,Audio:5
	unsigned int nEnCoder;  //Audio or Video encoder(Video,0:H263,1:H264, 2:MP4. Audio:0,G711_A:0x01,G711_U:0x02,ADPCM_A:0x03,G726:0x04)
	unsigned int nFrameRate;//Frame rate
	unsigned int nAbsStamp; //Absolute Time(s) 单位秒
	unsigned char ucBitsPerSample;// bit per sample [8/16/24] default 16
	unsigned int uiSamplesPerSec;// Samples Per Sec，default 8000
	unsigned int ui32FrameNO;
	unsigned int uiResPara1;	//nAbsStamp对应的毫秒数   毫秒级绝对时间=nAbsStamp*1000+uiResPara1
	unsigned int uiResPara2;
	unsigned int uiResPara3;
	unsigned int uiResPara4;
	char		 cResPara5[LEN_64];
	char		 cResPara6[LEN_256];
}RAWFRAME_INFOEX;

//Not decode the standard data before the pure h264 data
typedef void (__stdcall *RAWFRAME_NOTIFY)(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFO *_pRawFrameInfo, void* _iUser);
typedef void (__stdcall *RAWFRAME_NOTIFY_EX)(unsigned int _ulID,unsigned char* _cData,int _iLen, RAWFRAME_INFOEX *_pRawFrameInfo, void* _iUser);

#define RAW_NOTIFY_ALLOW_DECODE		0
#define RAW_NOTIFY_FORBID_DECODE	1
typedef struct _tagRawFrameNotifyInfo
{
	RAWFRAME_NOTIFY pcbkRawFrameNotify;		//raw data notify with play control
	void*			pUserData;
	int				iForbidDecodeEnable;	//whether need to play     0-play 1-no need decode and play
}RawFrameNotifyInfo, *pRawFrameNotifyInfo;

#define PLAY_VIDEO_HEADER_LEN			88
typedef struct __structRawCbkPlayerHeader
{
	int iSize;			//buf size
	void* pWnd;
	unsigned char cHeader[PLAY_VIDEO_HEADER_LEN];
	RawFrameNotifyInfo tRawNotify;
}S_RawCbkPlayerHeader, *PS_RawCbkPlayerHeader;

#ifdef __WIN__
#define AVHDC						HDC
#else
#define AVHDC						void*
#endif

#define MAX_DRAW_POINT_COUNT        32

typedef int (__stdcall* CallBackDrawGraph)(void* _pBuf, int _iBufSize, void* _pUser);

#define DRAW_TYPE_LINE				0
#define DRAW_TYPE_POLYGON			1
#define DRAW_TYPE_RECT				2
#define DRAW_TYPE_TEXT				3
#define DRAW_TYPE_BLURRY			4
#define DRAW_TYPE_PCOLORDATA		5
#define DRAW_TYPE_TRIANGLE_STRIP	6

//视频显示比例(电子放大使用)
typedef struct __tagRectRate
{
	double							dLeft;					//原视频比例
	double							dTop;					//原视频比例
	double							dRight;					//原视频比例
	double							dBottom;				//原视频比例
} RectRate, *PRectRate;

typedef struct __tagDrawGraphBack
{
	AVHDC							hDC;                 	//画图DC
	unsigned int					iTimeStamp;				//时间戳
	int								iWidth;              	//视频宽
	int								iHeight;             	//视频高
	RECT							rcShow;					//视频显示窗口坐标
	RectRate						rcSrc;					//视频显示区域比例
	CallBackDrawGraph               pDrawFunc;				//绘图函数
	void*                           pDrawUser;				//绘图用户
} DrawGraphBack, *PDrawGraphBack;

typedef struct __tagDrawGraphInfo
{
	int                             iType;                  //0:画线，1区域,2矩形，3文字,4 阴影
	int                             iColor;                 //画线(文字)颜色	
	int                             iPitch;                 //画线宽，iType=3表示字体大小
	int                             iPtCount;               //点个数
	POINT                           tPts[MAX_DRAW_POINT_COUNT];//点坐标, iType=3表示文字叠加位置
	unsigned char					cText[LEN_128];         //叠加文字
	float                           fShadowLine;            //阴影线间隔，当Type = 4时，iShadowLine 赋值成0的时候，阴影线间隔为10像素 与老版保持一致
	int								iFontType;              //iType=3时有效，字体格式，0:默认，1水利扩展，2帧码率
	int								iCharSet;				//字符集0：默认，1，gb2312 2，utf-8
	float							fAlpha;					//iType=5伪彩透明度，iType=6 opengl画线透明度,取值范围0.0f-1.0f，默认为0，0是全透明-按照不开启透明度的逻辑处理
} DrawGraphInfo, *PDrawGraphInfo;

typedef struct __tagAvModeHdEnable{
	int iEnable;   //out 是否开启硬解
	int iVideoMode;//in 视频格式，MODE_264,MODE_265
}AvModeHdEnable;

#define ALLOW_VIDEO_RENDERING			0	//allow video show
#define FORBID_VIDEO_RENDERING			1	//forbid video show
#define CONTINUOUS_VIDEO_RENDERING		2	//continuous video rendering

#define CHARSET_TYPE_RESERVE				0
#ifndef CHARSET_TYPE_UTF8
#define CHARSET_TYPE_UTF8					1
#endif
#ifndef CHARSET_TYPE_GB2312
#define CHARSET_TYPE_GB2312					2
#endif

#define DEFCHARSET_OSAPI		0
#define WIDECHARSET_OSAPI		1

typedef struct	tagLocalClientInfo
{
	unsigned char ucCharsetCoded;		//local character set encoding: 0-reserve, 1-UTF8, 2-GB2312
	unsigned char ucWideCharsetVer;		//0-do not use wide-character version(fopen,_fsopen...), 1:use wide-character version(_wfsopen...)
} LocalClientInfo, *pLocalClientInfo;

#define ROTATE_DIRECTION_CLOCKWISE			0	//clockwise
#define ROTATE_DIRECTION_ANTICLOCKWISE		1	//anti-clockwise

#define VIDEO_ROTATE_0					0	
#define VIDEO_ROTATE_90					90
#define VIDEO_ROTATE_180				180
#define VIDEO_ROTATE_270				270
#define VIDEO_MIRROR_HORIZONTAL 1
#define VIDEO_MIRROR_VERTICAL		2
typedef struct tagVideoRotatePara
{
	int iRotationDirection;		//Direction of rotation: 0-clockwise, 1 anticlockwise
	int iRotationAngle;			//Rotation angle, current only supports 0/90/180/270
} VideoRotatePara, *pVideoRotatePara;

typedef struct tagVideoRotateMirrorPara
{
	int iRotationDirection;		//Direction of rotation: 0-clockwise, 1 anticlockwise
	int iRotationAngle;			//Rotation angle, current only supports 0/90/180/270, 
	int iMirrorType;			//1 horizontal mirror, 2 vertical mirror
} VideoRotateMirrorPara, *pVideoRotateMirrorPara;

#endif

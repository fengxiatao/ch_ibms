#ifndef _REGSERVERTYPES_H_
#define _REGSERVERTYPES_H_

#if ((defined(_WIN32) || defined(_WIN64)) && !defined(__WIN__))
#define __WIN__
#endif

#ifdef __WIN__
#else
#define __stdcall
#endif


#ifndef LEN_32
#define LEN_32						32
#endif


#ifndef LEN_64
#define LEN_64						64
#endif

#ifndef LEN_512
#define LEN_512						512
#endif

#ifndef LEN_1024
#define LEN_1024					1024
#endif

#ifndef	RET_SUCC
#define RET_SUCC					0										//success
#endif

#ifndef	RET_FAIL
#define RET_FAIL					-1										//fail
#endif


//return code
#define	ERR_SUCCESS					0		 								//success
#define	ERR_WSASTARTUP        		(-1001)  								//Windows Socket initialization failed
#define	ERR_PORTNOTAVAILABLE  		(-1002)  								//The port is already occupied
#define	ERR_WRONGPASSWORD     		(-1003)  								//wrong password
#define	ERR_ACCOUNTLIMIT      		(-1004)  								//Exceeded maximum number of accounts
#define	ERR_NVSHOSTLIMIT      		(-1005)
#define	ERR_NOTRANSFER        		(-1006)
#define	ERR_NONVSHOST         		(-1007)
#define	ERR_DNSHOSTLIMIT      		(-1008)
#define	ERR_CLIENTHOSTLIMIT   		(-1009)
#define	ERR_INVALIDUSERNAME   		(-1010)
#define	ERR_INVALIDPASSWORD   		(-1011)
#define	ERR_DSHOSTLIMIT       		(-1012)
#define	ERR_ERRPARA           		(-1013)  								//Function passed invalid parameter
#define	ERR_SYSTEM	    	  		(-1014) 								//system error
                              		 
// add by lyh 2009.12.02       
#define	ERR_NOT_INITIAL        		(-1016)  								//The server is not initialized
#define	ERR_PORT_RANGE         		(-1017)  								//The port range is incorrect
#define	ERR_USERFUNC_CALLFAIL  		(-1018)  								//User function call failed
#define	ERR_HAS_ID             		(-1019)  								//exist id
#define	ERR_NOT_SAME_NAME      		(-1020)  								//nvs name is inconsistent


#define	ERR_DATABASE	       		(-101)  								//Database operate failed
#define	ERR_INVALIDPARA	   			(-102)  								//Character contains illegal characters, cannot contain < > / ' " & ? and other characters used in HTML
#define	ERR_NOHOST		       		(-103)  								//The hostname does not exist
#define	ERR_NOACCOUNT	       		(-104)  								//this user does not exist
#define	ERR_NOPASS		       		(-105)  								//host password error
#define	ERR_TOLONGSTR	       		(-106)  								//The string exceeds the specified length, the user ID, the host ID can be up to 20 digits long, and the password can be up to 10 digits long
#define	ERR_CONNECT					107										//communication error


//Add error code
#define	ERR_CONNMIDSVR      		151   									//should reconnect the secondary registry
#define	ERR_NOPROXY         		152   									// no proxy server available
#define	ERR_NORIGHT         		153   									//No permission to create user
#define	ERR_WAITSVR         		154   									//should wait for a response from the server
#define	ERR_INVALIDDATA     		155   									// illegal data

#define	ERR_DATA               		301   									//zyp add
#define	ERR_TIMEOUT            		302
#define	ERR_USERINVALID        		303
#define	ERR_FACTORYIDINVALID   		304




/*---------------------------------------------------------------------------*/
//message definition
#define	MSG_REGCENTER              (WM_USER+101)    
/*---------------------------------------------------------------------------*/
//The upper 16 bits of the WPARAM parameter of the message are defined as follows
#define	PARA_ADDUSER              	0x101          							//Add user

#define	PARA_REGISTE_DS           	0x111          							//Directory server registration
#define	PARA_UPDATE_DS            	0x112          							// directory server update
#define	PARA_DISCONNECT_DS        	0x113          							// The directory server is disconnected

#define	PARA_REGISTE_NVS          	0x121          							//Nvs registration
#define	PARA_UPDATE_NVS           	0x122          							//Nvs update
#define	PARA_DISCONNECT_NVS       	0x123          							//Nvs disconnect

#define	PARA_REGISTE_PROXY        	0x131          							//agent registration
#define	PARA_UPDATE_PROXY         	0x134          							// proxy update
#define	PARA_DISCONNECT_PROXY     	0x135          							//Agent disconnects

#define	PARA_REGISTE_CLIENT       	0x141          							// Client registration to be connected
#define	PARA_UPDATE_CLIENT        	0x142          							// client update to be connected
#define	PARA_DISCONNECT_CLIENT    	0x143          							//The client to be connected is disconnected

#define	PARA_REGISTE_TRANSFER     	0x151          							//Video forwarding relationship registration
#define	PARA_UPDATE_TRANSFER      	0x152          							//Update the video forwarding relationship
#define	PARA_DISCONNECT_TRANSFER  	0x153          							//The video forwarding relationship is disconnected

#define	PARA_REGISTE_ASSIGN       	0x161          							//Agent assignment registration
#define	PARA_UPDATE_ASSIGN        	0x162          							//Agent assignment update
#define	PARA_DELETE_ASSIGN        	0x163          							//Agent assignment delete
#define	PARA_DELETEALL_ASSIGN     	0x164          							//delete all proxy assignments

#define	PARA_REGISTE_DNS          	0x171          							//Domain name resolution registration
#define	PARA_UPDATE_DNS           	0x172          							//Domain name resolution update
#define	PARA_DISCONNECT_DNS       	0x173          							//The domain name resolution is disconnected

#define	PARA_REGISTE_CONNCLIENT   	0x181          							// client registration
#define	PARA_UPDATE_CONNCLIENT    	0x182          							// client update
#define	PARA_DISCONNECT_CONNCLIENT	0x183          							// client disconnect

#define	TYPE_BYNAME      			0            							//According to nvs name
#define	TYPE_BYID        			1            							//According to nvs id

#define	REG_PAGE_SIZE    			20           							//page size


//type of data
#define	TYPE_NVS        			0                      					//nvs
#define	TYPE_PROXY      			1                      					//proxy server
#define	TYPE_CLIENT     			2                      					// client to connect
#define	TYPE_TRANSFER   			3                      					//Video forwarding relationship
#define	TYPE_ASSIGN     			4                      					//Agent assignment
#define	TYPE_DNS        			5                      					//DNS
#define	TYPE_DS         			6                      					//Secondary registration center
#define	TYPE_P2P_NVS    			7                      					//p2p nvs
#define	TPYE_P2P_CLIENT 			8                      					//Client using P2P connection

#define	DATA_TYPE_NVS				0
#define	DATA_TYPE_PROXY				1
#define	DATA_TYPE_CLIENT			2
#define	DATA_TYPE_ASSIGN     		4
#define	DATA_TYPE_NVS_ID			9

#define	DATA_TYPE_ALL_NVS			10
#define	DATA_TYPE_ALL_PROXY			11
#define	DATA_TYPE_ALL_CLIENT		12
#define	DATA_TYPE_ALL_ASSIGN     	14

#define	WORK_TYPE_LOCAL				0
#define	WORK_TYPE_REMOTE			1	

#define	WORK_MODE_BLOCK				0										//Set the working mode--blocking parameters
#define	WORK_MODE_TYPE				1										//Set the working mode--0 local 1 remote

#define REG_CBK_MSGTYPE_NVS_ONLINE		0									//NVS registration information callback
#define REG_CBK_MSGTYPE_NVS_OFFLINE		1									//NVS disconnection message callback

typedef int (__stdcall *cbkNvsRegNotify)(int _iMsgType, void* _pNvs, int _iSize, void* _lpUser);

typedef struct  
{
	int					m_iReserved1;
	unsigned long		m_dwReserved2;
	char  				m_cReserved1[LEN_32];
	char  				m_cReserved2[LEN_64];
}st_Reserve;

typedef struct			//The identification of NVS, whether it is the UDP registration center or the active mode of TCP, NVS relies on this identification
{
	char  				cNvsIP[LEN_32];
	char  				cNvsName[LEN_32];
	int   				iNvsType;
	char  				cFactoryID[LEN_32];
	st_Reserve			m_stReserve;
}st_NvsSingle,*pst_NvsSingle;

typedef struct tagst_NvsSingleEx			//The identification of NVS, whether it is the UDP registration center or the active mode of TCP, NVS relies on this identification
{
	char  				cNvsIpV4[LEN_32];
	char  				cNvsIpV6[LEN_64];
	char  				cNvsName[LEN_32];
	int   				iNvsType;
	char  				cFactoryID[LEN_32];
	int					iChanNum;							//number of channels
	char				cCharSet[LEN_32];					//character set
	char  				cWanIpV4[LEN_32];
	char  				cWanIpV6[LEN_64];
} st_NvsSingleEx, *pst_NvsSingleEx;

typedef struct
{
	char          		m_cUserName[LEN_32];
	char          		m_cPwd[LEN_32];
	st_NvsSingle  		stNvs;
	int           		m_iPort;
	int           		m_iChannel;
	st_Reserve			m_stReserve;
}st_DNSRegInfo,*pst_DNSRegInfo;

typedef struct			//Agent information
{
	char				cProxyIP[LEN_32];
	unsigned int		iProxyPort;
	st_Reserve			m_stReserve;
}st_ProxyInfo,*pst_ProxyInfo;

typedef struct tagst_ProxyInfoEx	//Agent information
{
	char				cProxyIpV4[LEN_32];
	char				cProxyIpV6[LEN_64];
	unsigned int		iProxyPort;
} st_ProxyInfoEx, *pst_ProxyInfoEx; 

typedef struct			//NVS registration information
{
	char            	szPrimaryDS[LEN_32];     							//Preferred directory server IP
	st_NvsSingle    	m_stNvs;
	char            	szRegTime[LEN_32];       							//update registration time
	unsigned long   	dwClientConnNum;     								//Number of client connections
	int            		bRegister;           								//Data type: true-directly registered data, false-backed up data
	st_Reserve			m_stReserve;
}REG_NVS,*REG_PNVS;


typedef struct			//proxy server information
{
	char            	szPrimaryDS[LEN_32];								//Preferred directory server IP
	st_ProxyInfo    	m_stProxy;											//Agent information
	char            	szRegTime[LEN_32];									//update registration time
	int            		bRegister;											//Data type: true-directly registered data, false-backed up data
	st_Reserve			m_stReserve;										//m_iReserved1: The number of established conversion relationships for this proxy, used for load balancing add by lyh 2009.11.27
																			//m_iReserved2: The connection port when the proxy connects to the directory server. Since the proxy does not send the proxy's listening port information after connecting to the nvs, it can only be identified by this
}REG_PROXY,*REG_PPROXY;


typedef struct			//When the client connects to a front-end device through a proxy, the parameters of the connected device
{
	unsigned long   	dwConnectID;             							//ConnectID
	unsigned long   	dwConnMode;              							// network mode
	unsigned long   	dwChannel;               							//channel number
	unsigned long   	dwStreamType;            							//code stream type
	char            	szClientIP[LEN_32];									//Client IP
	st_Reserve			m_stReserve;
}CLICONN,*PCLICONN;


typedef struct			//(to be connected) client information
{
	char				szPrimaryDS[LEN_32];									//Preferred directory server IP
	st_NvsSingle		stNvs;												//The client requests nvs to connect
	int         		iWaitTime;
	int         		iTriedNum;
	char        		szTriedProxy[3][LEN_32];
	st_Reserve			m_stReserve;
}REG_CLIENT,*REG_PCLIENT;


typedef struct			//Manually assigned agents
{
	st_NvsSingle		stNvs;
	st_ProxyInfo		m_stPorxy;
	st_Reserve			m_stReserve;
}REG_ASSIGN,*REG_PASSIGN;


typedef struct
{
	char 				cDSIP[LEN_32];
	char 				cUserName[LEN_32];
	char 				cUserPwd[LEN_32];
	st_NvsSingle		stNvs;
	st_Reserve			m_stReserve;
}st_LogOnInfo, *pst_LogOnInfo;

typedef struct
{
	int					iSize;
	char 				cUserName[LEN_32];
	char 				cUserPwd[LEN_32];
	char  				cFactoryID[LEN_32];
}InNvsId, *pInNvsId;


typedef struct
{
	int                 iSize;
	char                cAccount[LEN_32];
	char                cUserPwd[LEN_32];
	int					iPos;												//the first few
}InDataPos, *pInDataPos;


typedef struct
{
	int                 iSize;
	char                cAccount[LEN_32];
	char                cUserPwd[LEN_32];
	int					iCount;												// get the number
}InDataAll, *pInDataAll;


typedef struct			//NVS registration information
{
	int					iSize;
	char            	cDsIp[LEN_32];     									//Directory server IP
	char 				cUserName[LEN_32];
	char 				cUserPwd[LEN_32];
	char  				cFactoryID[LEN_32];									//Factory ID
	char  				cNvsIP[LEN_32];										//device Ip
	char  				cNvsName[LEN_32];									//device name
	int   				iNvsType;											//Equipment type
	char				cCharSet[LEN_32];									//character set
	int					iChanNum;											//Number of channels
	char				cRegTime[LEN_32];									//Registration time
	char 				cProxyIp[LEN_32];									//Proxy Ip
	int   				iProxyPort;											//Proxy port
	char 				cWanIp[LEN_32];										//public IP
	int   				iWanPort;											// public network port
	int					iIpVer;												//Register connection IP version: 0--IpV4, 1--IpV6
	char            	cDsIpV6[LEN_64];     								//Directory server IpV6 address
	char  				cNvsIpV6[LEN_64];									//Device IpV6 address
	char 				cProxyIpV6[LEN_64];									//Proxy IpV6 address
	char 				cWanIpV6[LEN_64];									//Public network IpV6 address
}RegNvs, *pRegNvs;


typedef struct			//Agent's registration information
{
	int					iSize;
	char            	cDsIp[LEN_32];     									//Directory server IP
	char 				cUserName[LEN_32];
	char 				cUserPwd[LEN_32];
	char 				cProxyIp[LEN_32];									//Proxy Ip
	int   				iPort;												//Proxy port
	char				cRegTime[LEN_32];									//Registration time
	int					iIpVer;												//Register connection IP version: 0--IpV4, 1--IpV6
	char            	cDsIpV6[LEN_64];     								//Directory server IpV6 address
	char 				cProxyIpV6[LEN_64];									//Proxy IpV6 address
}RegProxy, *pRegProxy;

typedef struct
{
	int					iSize;
	cbkNvsRegNotify		cbkNotify;
	void*				pUser;
}RegNvsNotify;

//DNS
typedef struct
{
	st_DNSRegInfo   m_stDNSInfo;
	char            szRegTime[32];  //Update registration time

	st_Reserve      m_stReserve;
}REG_DNS,*REG_PDNS;

typedef int (__stdcall *cbkGetDNSList)(int _iCount,REG_DNS *_regDNS);
typedef int (__stdcall *cbkGetNVSList)(int _iCount,st_NvsSingle *_stNvs);

#endif 

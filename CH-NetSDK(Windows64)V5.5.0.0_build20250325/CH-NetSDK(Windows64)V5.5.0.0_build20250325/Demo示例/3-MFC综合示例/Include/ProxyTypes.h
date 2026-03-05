#ifndef _PROXYTYPES_H
#define _PROXYTYPES_H

#include "NetClientTypes.h"
#include "DEFINE_USER_ERROR.h"

#define MAX_PROXY               1024     //Most agents added (max input)
#define MAX_CLIENT_CONNECTION   1024     //Maximum number of client connections (maximum output)

#ifndef DEV_PRIVATE
#define DEV_PRIVATE   0
#endif

#ifndef LEN_8
#define LEN_8   8
#endif

#ifndef LEN_16
#define LEN_16  16
#endif

#ifndef LEN_32
#define LEN_32  32
#endif

#ifndef LEN_64
#define LEN_64  64
#endif

#ifndef LEN_128
#define LEN_128 128
#endif

#ifndef LEN_256
#define LEN_256 256
#endif

#ifndef STRUCT_RESERVE
#define STRUCT_RESERVE
typedef struct                            //This structure is for reserve.
{
	int   m_iReserved1;
	unsigned long m_dwReserved2;
	char  m_cReserved1[32];
	char  m_cReserved2[64];
}st_Reserve;
//typedef struct st_Reserve *pst_Reserve;
#endif

struct st_EncoderProxy
{
    char         m_cDevIP[LEN_16];         //dev ip :192.168.1.2
    unsigned int m_iType;                  //dev type: PRIVATE-S
    char         m_cFactoryID[LEN_32];     //dev id: ID....
    char         m_cDevName[LEN_32];       //dev name: NVSS1  Reserved
    int          m_iPort;                  //dev port: 3000
    int          m_iNetMode;               //dev connect net mode: NETMODE_TCP
    char         m_cAccount[LEN_32];       //dev logon account: Admin
    char         m_cPwd[LEN_32];           //dev logon pwd: Admin

    char         m_cProxy[LEN_16];         //Proxy IP

    st_Reserve   m_stReserve;
};

typedef struct tagEncoderProxyEx
{
	char         m_cDevIP[LEN_64];         //dev ip :192.168.1.2
	unsigned int m_iType;                  //dev type: PRIVATE-S
	char         m_cFactoryID[LEN_32];     //dev id: ID....
	char         m_cDevName[LEN_32];       //dev name: NVSS1  Reserved
	int          m_iPort;                  //dev port: 3000
	int          m_iNetMode;               //dev connect net mode: NETMODE_TCP
	char         m_cAccount[LEN_32];       //dev logon account: Admin
	char         m_cPwd[LEN_32];           //dev logon pwd: Admin
	char         m_cProxy[LEN_64];         //Proxy IP
} EncoderProxyEx, *pEncoderProxyEx;

struct st_NetInterface
{
    char m_IP[LEN_32][LEN_16];
};

typedef int (*NOTIFY_PROXYMAIN)(int _iProxyID,int _iWparam,int _iLparam);
typedef int (*NOTIFY_CLIENTCONNECT)(char *_cClientIP,int _iPort,int *_iPass);
typedef int (*NOTIFY_USERSTRING)(char *_cClientIP,int _iType,char *_cMsg,int _iLen);

#define TYPE_PROXY_ONLY		0	
#define TYPE_PROXY_CLIENT	1

//Agent library setting configuration parameter command ID macro definition
#define CMD_PROXYCMD_MIN						0
#define CMD_PROXYCMD_SET_DSMCFG			(CMD_PROXYCMD_MIN + 0)		//Set up directory server configuration
#define CMD_PROXYCMD_SET_BINDIP			(CMD_PROXYCMD_MIN + 1)		//Set up directory server configuration
#define CMD_PROXYCMD_GET_ENCODER		(CMD_PROXYCMD_MIN + 2)		//Get encoding device information, including ipc and nvr
#define CMD_PROXYCMD_GET_LINK			(CMD_PROXYCMD_MIN + 3)		//Get proxy connection information
#define CMD_PROXYCMD_MAX				(CMD_PROXYCMD_MIN + 4)

//Set up directory server configuration information
typedef struct tagProxyDsmCfg
{
	int iIpVer;					//input para, ip version: 0--IpV4, 1--IpV6
	char cDsIp1V4[LEN_16];		//input para, main directory server ipv4
	char cDsIp1V6[LEN_64];		//input para, main directory server ipv6
	unsigned short usPort1;		//input para, main directory server port
	char cDsIp2V4[LEN_16];		//input para, sub directory server ipv4
	char cDsIp2V6[LEN_64];		//input para, sub directory server ipv6
	unsigned short usPort2;		//input para, sub directory server port
	char cAccount[LEN_16];		//input para, directory account
	char cPassword[LEN_16];		//input para, directory password
} ProxyDsmCfg;

//Bind the proxy local public IP address
typedef struct tagProxyBindIp
{
	char cBindIpV4[LEN_16];		//input para, bind ipv4, proxy local wan ip
	char cBindIpV6[LEN_64];		//input para, bind ipv6, proxy local wan ip
} ProxyBindIp;

typedef struct tagProxyLinkInfo
{
	int		iConnectID;				//input para:Enter connection ID
	int		iIpVer;					//ip version: 0--IpV4, 1--IpV6
	char    cNvsIpV4[LEN_16]; 		//NVS IpV4
	char    cNvsIpV6[LEN_64]; 		//NVS IpV6
	char    cClientIpV4[LEN_16];	//Client IpV4
	char    cClientIpV6[LEN_64];	//Client IpV6
	int     iChannel;     			//Channel Number
	int     iStreamNO;    			//Stream type
	int     iMode;        			//Network mode, 1(TCP), 2(UDP), 3(multicast)
} ProxyLinkInfo, *pProxyLinkInfo;

#endif


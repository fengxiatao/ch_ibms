package src;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner; 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.RECVDATA_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.NETFILE_QUERY_V5;
import src.NVSSDK.QueryFileChannel;
import src.NVSSDK.NVS_FILE_DATA;
import src.NVSSDK.QueryFileResult;
import src.NVSSDK.NVS_LOG_QUERY;
import src.NVSSDK.NVS_LOG_DATA;
import src.NVSSDK.QueryLogResult;
import src.NVSSDK.DOWNLOAD_FILE;
import src.NVSSDK.DOWNLOAD_TIMESPAN;
import src.NVSSDK.DOWNLOAD_CONTROL;
import src.NVSSDK.RAWFRAME_NOTIFY;
import src.NVSSDK.RAWFRAME_INFO;
import src.NVSSDK.FULLFRAME_NOTIFY_V4;
import src.NVSSDK.ArrayQueryFileChannel;
import src.NVSSDK.ActiveNetWanInfo;
import src.NVSSDK.DsmOnline;
import src.NVSSDK.LogonActiveServer;
import src.NVSSDK.LogonPara;
import src.NVSSDK.NetClientPara;
import src.NVSSDK.VCASuspend;
import src.NVSSDK.VCASuspendResult;
//import src.NVSSDK.LocalSDKPath;


public class SyncBusiness {
	// Demonstrate the SDK calling example, take a device as an example
	static int m_iLogonID = -1;
	// Demonstrate the SDK calling example, take connecting one video as an example
	static int m_iConnectID = -1;
	//Store the map of demo operation instructions
	Map<String, Integer> m_mapOptCmd = new HashMap<String, Integer>();
	
	//Define the demo operation command value
	public static final int CMD_QUIT = -1;
	public static final int CMD_SYNCLOGON = 1;
	public static final int CMD_LOGOFF = 2;
	public static final int CMD_STARTREALPALY = 3;
	public static final int CMD_STOPREALPALY = 4;
	public static final int CMD_SYNCQUERYFILE = 5;
	public static final int CMD_SYNCQUERYLOG = 6;
	public static final int CMD_SUSPENDVCA = 7;
	public static final int CMD_OPENVCA = 8;
	public static final int CMD_DOWNLOADBYFILE = 9;
	public static final int CMD_DOWNLOADBYTIME = 10;
	public static final int CMD_STOPDOWNLOAD = 11;
	
	public void makeOptCmd()
	{   
		m_mapOptCmd.put("Quit", CMD_QUIT);
		m_mapOptCmd.put("q", CMD_QUIT);
		
		m_mapOptCmd.put("SyncLogon", CMD_SYNCLOGON);
		m_mapOptCmd.put("1", CMD_SYNCLOGON);
		
		m_mapOptCmd.put("Logoff", CMD_LOGOFF);
		m_mapOptCmd.put("2", CMD_LOGOFF);
		
		m_mapOptCmd.put("StartRealPlay", CMD_STARTREALPALY);
		m_mapOptCmd.put("3", CMD_STARTREALPALY);
		
		m_mapOptCmd.put("StopRealPlay", CMD_STOPREALPALY);
		m_mapOptCmd.put("4", CMD_STOPREALPALY);
		
		m_mapOptCmd.put("SyncQueryFile", CMD_SYNCQUERYFILE);
		m_mapOptCmd.put("5", CMD_SYNCQUERYFILE);
		
		m_mapOptCmd.put("SyncQueryLog", CMD_SYNCQUERYLOG);
		m_mapOptCmd.put("6", CMD_SYNCQUERYLOG);
		
		m_mapOptCmd.put("SyncSuspendVca", CMD_SUSPENDVCA);
		m_mapOptCmd.put("7", CMD_SUSPENDVCA);
		
		m_mapOptCmd.put("SyncOpenVca", CMD_OPENVCA);
		m_mapOptCmd.put("8", CMD_OPENVCA);
		
		m_mapOptCmd.put("DownloadByFile", CMD_DOWNLOADBYFILE);
		m_mapOptCmd.put("9", CMD_DOWNLOADBYFILE);
		
		m_mapOptCmd.put("DownloadByTime", CMD_DOWNLOADBYTIME);
		m_mapOptCmd.put("10", CMD_DOWNLOADBYTIME);
		
		m_mapOptCmd.put("StopDownload", CMD_STOPDOWNLOAD);
		m_mapOptCmd.put("11", CMD_STOPDOWNLOAD);
	}
	
	public void testSdkApi()
	{
		Scanner scanInput = new Scanner(System.in);
		int iCmd = -1;
		String strCmd = new String();
		boolean blQuit = false;
		while(!blQuit)
		{
			System.out.println("***********************************************************");
			System.out.println(
				"[q]Quit											\n"
				+ "[1]SyncLogon 		[2]Logoff			\n"
				+ "[3]StartRealPlay 	[4]StopRealPlay			\n"
				+ "[5]SyncQueryFile	[6]SyncQueryLog		\n"
				+ "[7]SyncSuspendVca	[8]SyncOpenVca		\n"
				+ "[9]DownloadByFile	[10]DownloadByTime		\n"
				+ "[11]StopDownload");
			System.out.println("***********************************************************");

			System.out.print("Please input your correct command: ");
			strCmd = scanInput.next();
			try {
				iCmd = m_mapOptCmd.get(strCmd);
	        } catch(Exception e) {
	        	iCmd = 0;
	        }
			switch(iCmd)  
			{
				case CMD_QUIT:   //q
				{
					System.out.println("Goodbye, my friend!");
					blQuit = true;
					break;
				}
				case CMD_SYNCLOGON:
				{
					SyncLogon();
					break;
				}
				case CMD_LOGOFF:
				{
					Logoff();
					break;
				}
				case CMD_STARTREALPALY:
				{
					StartRealPlay();
					break;
				}
				case CMD_STOPREALPALY:
				{
					StopRealPlay();
					break;
				}
				case CMD_SYNCQUERYFILE:
				{
					SyncQueryFile();
					break;
				}
				case CMD_SYNCQUERYLOG:
				{
					SyncQueryLog();
					break;
				}
				case CMD_SUSPENDVCA:
				{
					SyncSuspendVca();
					break;
				}
				case CMD_OPENVCA:
				{
					SyncOpenVca();
					break;
				}
				case CMD_DOWNLOADBYFILE:
				{
					downloadByFileMode();
					break;
				}
				case CMD_DOWNLOADBYTIME:
				{
					downloadByTimespanMode();
					break;
				}
				case CMD_STOPDOWNLOAD:
				{
					stopDownload();
					break;
				}
				default:
				{
					System.out.println("Command \"" + strCmd + "\" not found!");
					break;
				}
			}
		}
	}
	
	private int sdkInit() {
		//LocalSDKPath tPath = new LocalSDKPath();
		//tPath.iSize = tPath.size();
		//tPath.iType = 0;
		//tPath.cPath ="/lib64".getBytes();
		//tPath.write();
		//NetClient.SetSDKInitConfig(0,tPath.getPointer(), tPath.size());
		//tPath.read();
		//System.out.println("[SetSDKInitConfig]" + iRet);

		SDK_VERSION ver = new SDK_VERSION();
		int iRet = NetClient.GetVersion(ver);
		System.out.println("[SDK_VERSION]" + ver.m_cVerInfo);

		iRet = NetClient.SetNotifyFunction(cbkMain, cbkAlarm, cbkParaChange);
		System.out.println("SetNotifyFunction(" + iRet + ")");

		iRet = NetClient.Startup();
		System.out.println("Startup(" + iRet + ")");

		return 0;
	};
	
	private void sdkUnInit() {
		int iRet = NetClient.Cleanup();
		System.out.println("Cleanup(" + iRet + ")");
	};
	
	public int SyncLogon()
	{
		Scanner scanInput = new Scanner(System.in);
		int iRet = NVSSDK.RET_FAILED;

		System.out.print("Please input LogonType(0--Normal  1--Active): ");
		int iLogonType = scanInput.nextInt();
		if (NVSSDK.SERVER_ACTIVE == iLogonType)
		{
			//Active mode login logic: The client and the device are not in the same subnet, and need to use the public network to penetrate
			System.out.print("Please input local listen port:");
			//Set the local listening internal port
			int iLocalListenPort = scanInput.nextInt();
			iRet = NetClient.SetPort(iLocalListenPort, 0);
			if(NVSSDK.RET_SUCCESS != iRet) {
				System.out.println("NetClient_SetPort fail!");
				return -1;
			}

			//Set the local listening external port (router mapping port)
			ActiveNetWanInfo tLocalWanInfo = new ActiveNetWanInfo();
			tLocalWanInfo.iSize = tLocalWanInfo.size();
			System.out.print("Please input wan IP: ");
			String strWanIP = scanInput.next();
			tLocalWanInfo.cWanIP = strWanIP.getBytes();
			System.out.print("Please input local wan port:");
			tLocalWanInfo.iWanPort = scanInput.nextInt();
			tLocalWanInfo.write();
			iRet = NetClient.SetDsmConfig(NVSSDK.DSM_CMD_SET_NET_WAN_INFO, tLocalWanInfo.getPointer(), tLocalWanInfo.size());
			if(NVSSDK.RET_SUCCESS != iRet )
			{
				System.out.println("NetClient_SetDsmConfig:DSM_CMD_SET_NET_WAN_INFO fail!\n");
				return -1;
			}
			
			System.out.print("Please enter the factory ID:");
			String strProductID = scanInput.next();
			DsmOnline tOnline  = new DsmOnline();
			tOnline.iSize = tOnline.size();
			tOnline.cProductID = strProductID.getBytes();
			tOnline.write();
			//Get the registration online status
			NetClient.GetDsmRegstierInfo(NVSSDK.DSM_CMD_GET_ONLINE_STATE, tOnline.getPointer(), tOnline.size());
			tOnline.read();
			int iOutTime = 0;
			while (NVSSDK.DSM_STATE_ONLINE != tOnline.iOnline)
			{
				if (iOutTime >= 30)
				{
					System.out.println("Device not register!");
					return -1;
				}
				
		        try {
		        	Thread.currentThread();
					Thread.sleep(1000); 
		        } catch(InterruptedException e) {
		            System.err.println("Interrupted");
		        }

				NetClient.GetDsmRegstierInfo(NVSSDK.DSM_CMD_GET_ONLINE_STATE, tOnline.getPointer(), tOnline.size());
				tOnline.read();
				iOutTime++;
			}


			System.out.print("Please input UserName: ");
			String strUserName = scanInput.next();
			System.out.print("Please input Password: ");
			String strUserPwd = scanInput.next();
			
			LogonActiveServer tActive = new LogonActiveServer();
			tActive.iSize = tActive.size();
			tActive.cUserName = strUserName.getBytes();
			tActive.cUserPwd = strUserPwd.getBytes();
			tActive.cProductID = strProductID.getBytes();
			tActive.write();
			//Active mode synchronous blocking login device
			m_iLogonID = NetClient.SyncLogon(iLogonType, tActive.getPointer(), tActive.size());
		}
		else
		{
			System.out.println("Please enter the device IP:");
			String strDevIP = scanInput.next();
			System.out.println("Please enter the device port:");
			int iDevPort = scanInput.nextInt();
			System.out.println("Please enter user name:");
			String strUserName = scanInput.next();
			System.out.println("Please enter your password:");
			String strUserPwd = scanInput.next();
			String strCharSet = "UTF-8";
			System.out.println("Logon:" + strDevIP + ", " + iDevPort + ", " +strUserName + ", " + strUserPwd);
			
			LogonPara tNormal = new LogonPara();
			tNormal.iSize = tNormal.size();
			tNormal.cNvsIP = strDevIP.getBytes();
			tNormal.iNvsPort = iDevPort;
			tNormal.cUserName = strUserName.getBytes();
			tNormal.cUserPwd = strUserPwd.getBytes();
			tNormal.cCharSet = strCharSet.getBytes();
			tNormal.write();
			//Regular mode synchronously blocks the login device
			m_iLogonID = NetClient.SyncLogon(iLogonType, tNormal.getPointer(), tNormal.iSize);
		}
		if (m_iLogonID >= 0)
		{
			System.out.println("NetClient_LogonSync success:" + "iLogonType=" + iLogonType + ", m_iLogonID=" + m_iLogonID);
			return 0;
		}
		else if (NVSSDK.RET_SYNCLOGON_TIMEOUT == m_iLogonID)
		{
			System.out.println("NetClient_LogonSync Timeout!");
			return -1;
		}
		else if (NVSSDK.RET_SYNCLOGON_USENAME_ERROR == m_iLogonID)
		{
			System.out.println("NetClient_LogonSync username error!");
			return -1;
		}
		else if (NVSSDK.RET_SYNCLOGON_USRPWD_ERROR == m_iLogonID)
		{
			System.out.println("NetClient_LogonSync password error!");
			return -1;
		}
		else if (NVSSDK.RET_SYNCLOGON_PWDERRTIMES_OVERRUN == m_iLogonID)
		{
			System.out.println("NetClient_LogonSync passwor times overrun!");
			return -1;
		}
		else if (NVSSDK.RET_SYNCLOGON_NET_ERROR == m_iLogonID)
		{
			System.out.println("NetClient_LogonSync net error!");
			return -1;
		}
		else if (NVSSDK.RET_SYNCLOGON_PORT_ERROR == m_iLogonID)
		{
			System.out.println("NetClient_LogonSync port error!");
			return -1;
		}
		else if (NVSSDK.RET_SYNCLOGON_UNKNOW_ERROR == m_iLogonID)
		{
			System.out.println("NetClient_LogonSync unknow error!");
			return -1;
		}
		else
		{
			System.out.println("NetClient_LogonSync fail! iRet=" + m_iLogonID);
			return -1;
		}
	}
	
	public void Logoff()
	{
		//logout
		if (m_iLogonID >= 0) {
			int iRet = NetClient.Logoff(m_iLogonID);
			m_iLogonID = -1;
			System.out.println("Logoff(" + iRet + ")");
		}
	}
	
	RAWFRAME_NOTIFY cbkRawFrame = new RAWFRAME_NOTIFY() {
		public void rawFrameNotify(int _iConnectID, Pointer _pcData, int _iLen, RAWFRAME_INFO _ptRawFrameInfo, Pointer _lpUserData) {
           //print data information
			System.out.println("recvRawData: _iConnectID=" + _iConnectID + ", _iLen=" + _iLen + ", nType=" + _ptRawFrameInfo.nType + ", input 11 stop download.");
            
           //The upper layer can save and process bare stream data
            if (null != _pcData && 0 != _iLen) {
            	//SaveRawData(_ptRawFrameInfo.nType, _pcData, _iLen);
            }
		}
	};
	
	FULLFRAME_NOTIFY_V4 cbkPrivateFullFrame = new FULLFRAME_NOTIFY_V4() {
		public void fullFrameNotifyV4(int _iConnectID, int _iStreamType, Pointer _pcData, int _iLen, Pointer _pvHeader, Pointer _pvUserData) {
	        //print data information
			System.out.println("recvPrivateData: _iConnectID=" + _iConnectID + ", _iStreamType=" + _iStreamType + ", _iLen=" + _iLen);
            
           //The upper layer can save and process bare stream data
            if (null != _pcData && 0 != _iLen) {
            	//TODO:
            }
		}
	};
	
	public int StartRealPlay()
	{
		Scanner scanInput = new Scanner(System.in);
		int iRet = NVSSDK.RET_FAILED;
		int iChanTotalCount = 0;
		int iDigitalChanCount = 0;
		int iVideoChan = 0;
		NetClientPara tVideoPara = new NetClientPara();

		//Get the total number of channels
		IntByReference piChanTotalCount = new IntByReference();
		iRet = NetClient.GetChannelNum(m_iLogonID, piChanTotalCount);
		if (NVSSDK.RET_SUCCESS != iRet)
		{
			System.out.println("NetClient_GetChannelNum fail iRet=" + iRet);
			return -1;
		}
		iChanTotalCount = piChanTotalCount.getValue();

		//Get the total number of nvr digital channels, only for nvr series devices
		IntByReference piDigitalChanCount = new IntByReference();
		iRet = NetClient.GetDigitalChannelNum(m_iLogonID, piDigitalChanCount);
		if (NVSSDK.RET_SUCCESS != iRet)
		{
			System.out.println("NetClient_GetDigitalChannelNum fail iRet=" + iRet);
			return -1;
		}
		iDigitalChanCount = piDigitalChanCount.getValue();
		System.out.println("iChanTotalCount=" + iChanTotalCount + ", iDigitalChanCount=" + iDigitalChanCount);
		System.out.println("Input video Channel Number[0-" + (iChanTotalCount - 1) + "](default:0):");
		iVideoChan = scanInput.nextInt();
		System.out.println("1-private tcp connect, 2-private udp connect, 3-private multicast connect, 6-rtsp stream via RTP-over-TCP, 7-rtsp stream via RTP-over-UDP");
		System.out.println("8-rtsp stream via RTP-over-Multicast, 9-rtsps stream via SRTP-over-UDP, 10-rtsps stream via SRTP-over-Multicast");
		System.out.println("Input net mode(default is 1-private tcp connect):");
		int iNetMode = scanInput.nextInt();
		tVideoPara.iSize = tVideoPara.size();
		tVideoPara.tCltInfo.m_iServerID = m_iLogonID;	//logon handle
		tVideoPara.tCltInfo.m_iChannelNo = iVideoChan;
		tVideoPara.tCltInfo.m_iStreamNO = 0; //0--main stream, 1--sub stream
		tVideoPara.tCltInfo.m_iNetMode = iNetMode;		
		tVideoPara.tCltInfo.m_iTimeout = 20;
		tVideoPara.pCbkFullFrm = cbkPrivateFullFrame;
		tVideoPara.pvCbkFullFrmUsrData = null;
		tVideoPara.pCbkRawFrm = cbkRawFrame;
		tVideoPara.pvCbkRawFrmUsrData = null;
		tVideoPara.pvWnd = null;	//console demo not show video
		tVideoPara.write();
		
		IntByReference piConnectID = new IntByReference();
		iRet = NetClient.SyncRealPlay(piConnectID, tVideoPara, tVideoPara.iSize);
		if (NVSSDK.RET_SUCCESS == iRet)
		{
			m_iConnectID = piConnectID.getValue();
			System.out.println("NetClient_StartRecvSync Success! uiRecvID=" + m_iConnectID);
		}
		else if (NVSSDK.RET_SYNCREALPLAY_TIMEOUT == iRet)
		{
			System.out.println("NetClient_StartRecvSync Timeout!");
			return -1;
		}
		else
		{
			System.out.println("NetClient_StartRecvSync fail!iRet=" + iRet);
			return -1;
		}
		
		return 0;
	}
	
	public int StopRealPlay()
	{
		if (m_iConnectID >= 0)
		{
			NetClient.StopRecv(m_iConnectID);
			m_iConnectID = -1;
		}

		return 0;
	}
	 
	public int SyncQueryFile()
	{
		//Users can modify the query time period by themselves to ensure that there is a video in the modified time period
		NETFILE_QUERY_V5 tQueryFileV5 = new NETFILE_QUERY_V5();
		tQueryFileV5.iBufSize = tQueryFileV5.size();
		tQueryFileV5.iQueryChannelNo = 0;	//query channel no, 0x7FFFFFFF means query all channel
		tQueryFileV5.iStreamNo = 0; //query stream number, 0-main stream, 1-sub stream
		tQueryFileV5.iType = 0xFF; //File type: 0-All, 1-AVstream, 2-picture
		
		
		//current time  
		Calendar cas = Calendar.getInstance();  
		int year = cas.get(Calendar.YEAR);//Get the year
		int month=cas.get(Calendar.MONTH)+1;//Get the month
		int day=cas.get(Calendar.DATE);//Get the day
		int hour=cas.get(Calendar.HOUR_OF_DAY);//hour
		int minute=cas.get(Calendar.MINUTE);//minute
		int second=cas.get(Calendar.SECOND);//second

		
		// query start time
		tQueryFileV5.tStartTime.iYear = (short)year;
		tQueryFileV5.tStartTime.iMonth = (short)month;
		tQueryFileV5.tStartTime.iDay = (short)day;
		tQueryFileV5.tStartTime.iHour = 0;
		tQueryFileV5.tStartTime.iMinute = 0;
		tQueryFileV5.tStartTime.iSecond = 0;
		// query end time
		tQueryFileV5.tStopTime.iYear = (short)year;
		tQueryFileV5.tStopTime.iMonth = (short)month;
		tQueryFileV5.tStopTime.iDay = (short)day;
		tQueryFileV5.tStopTime.iHour = (short)hour;
		tQueryFileV5.tStopTime.iMinute = (short)minute;
		tQueryFileV5.tStopTime.iSecond = (short)second;
		
		tQueryFileV5.iPageSize = 20; //The page size of each query, that is, the number of records per query
		tQueryFileV5.iPageNo = 0; //Query page number, for example, there are 100 records in total, and the page size of each query is 20, then the page number is 0, 1, 2, 3, 4
		tQueryFileV5.iFiletype = 1;	//File type 0:all,1:Video,2:picture
		tQueryFileV5.iTriggerType = 0x7FFFFFFF;
		tQueryFileV5.iTrigger = 0;
		tQueryFileV5.iQueryChannelCount = 1; //Number of channels for this query, some nvr supports batch query use, default single channel query can be used
		ArrayQueryFileChannel arrayQueryFileChannel = new ArrayQueryFileChannel(); //Batch query channel array, default query by single channel
		QueryFileChannel tQueryFileChannel0 = new QueryFileChannel();
		QueryFileChannel tQueryFileChannel1 = new QueryFileChannel();
		tQueryFileChannel0.iChannelNo = 0;
		tQueryFileChannel0.iStreamNo = 0;
		tQueryFileChannel1.iChannelNo = 0;
		tQueryFileChannel1.iStreamNo = 1;
		arrayQueryFileChannel.tArry[0] = tQueryFileChannel0;
		arrayQueryFileChannel.tArry[1] = tQueryFileChannel1;
		tQueryFileV5.ptChannelList = arrayQueryFileChannel.getPointer();
		tQueryFileV5.iBufferSize = tQueryFileChannel0.size(); //Query structure size
		tQueryFileV5.write();

		NVS_FILE_DATA tSingleData  = new NVS_FILE_DATA();
		QueryFileResult tResult = new QueryFileResult();
		
		int iOutTotalLen = 20 * tSingleData.size();
		int iSingleLen = tSingleData.size();
		
		int iRet = NetClient.SyncQuery(m_iLogonID, 0, NVSSDK.CMD_NETFILE_QUERY_FILE, tQueryFileV5.getPointer(), tQueryFileV5.size(), tResult.getPointer(), iOutTotalLen, iSingleLen);
		if (iRet < 0)
		{
			System.out.println("Err: NetClient_Query_V5");
			return -1;
		}

		tQueryFileV5.read();
		tResult.read();
		System.out.println("TotalCount=" + tQueryFileV5.iTotalQueryCount + ", CurrentCount=" + tQueryFileV5.iCurQueryCount);
		for(int i = 0; i < tQueryFileV5.iCurQueryCount; ++i)
		{          
 			String strFileName = new String(tResult.tArry[i].cFileName).trim();	
 			int iStartYear = tResult.tArry[i].tStartTime.iYear;
 			int iStartMonth = tResult.tArry[i].tStartTime.iMonth;
 			int iStartDay = tResult.tArry[i].tStartTime.iDay;
 			int iStartHour = tResult.tArry[i].tStartTime.iHour;
 			int iStartMinute = tResult.tArry[i].tStartTime.iMinute;
 			int iStartSecond = tResult.tArry[i].tStartTime.iSecond;
 			int iStopYear = tResult.tArry[i].tStopTime.iYear;
 			int iStopMonth = tResult.tArry[i].tStopTime.iMonth;
 			int iStopDay = tResult.tArry[i].tStopTime.iDay;
 			int iStopHour = tResult.tArry[i].tStopTime.iHour;
 			int iStopMinute = tResult.tArry[i].tStopTime.iMinute;
 			int iStopSecond = tResult.tArry[i].tStopTime.iSecond;
 			System.out.println("fileIdx=" + i + ", fileName=" + strFileName
 					+ ", chNo=" + tResult.tArry[i].iChannel + ", fileSize=" + tResult.tArry[i].iFileSize
 					+ ", fileStartTime=" + iStartYear + "-" + iStartMonth + "-" + iStartDay + "-"
 					+ iStartHour + "-" + iStartMinute + "-" + iStartSecond
 					+ ", fileStopTime=" + iStopYear + "-" + iStopMonth + "-" + iStopDay + "-"
 					+ iStopHour + "-" + iStopMinute + "-" + iStopSecond);
		}

		return 0;
	}
	
	public int SyncQueryLog()
	{
		
		//current time  
		Calendar cas = Calendar.getInstance();  
		int year = cas.get(Calendar.YEAR);//Get the year
		int month=cas.get(Calendar.MONTH) + 1;//Get the month
		int day=cas.get(Calendar.DATE);//Get the day
		int hour=cas.get(Calendar.HOUR_OF_DAY);//hour
		int minute=cas.get(Calendar.MINUTE);//minute
		int second=cas.get(Calendar.SECOND);//second
		
		NVS_LOG_QUERY tQueryLog = new NVS_LOG_QUERY();
		tQueryLog.iChannelNo = 0xFF; //Query all channels
		tQueryLog.iLogType = 0xFF; //0, syslog, 1, warning, 2, alarm, 3, action, 4, user, 5, other, 0xFF, all types
		tQueryLog.iLanguage = 1; //0, English; 1, GB2312 simplified; 2, BIG5 traditional; 3, Korean; 4, Spanish; 5, Italian,
		tQueryLog.iPageSize = 10; //The page size of each query is 20
		tQueryLog.iPageNo = 0; //Query the page number, for example, there are 100 records in total, and the page size of each query is 20, then the page number is 0, 1, 2, 3, 4
		tQueryLog.tStartTime.iYear = (short)year;
		tQueryLog.tStartTime.iMonth = (short)month;
		tQueryLog.tStartTime.iDay = (short)day;
		tQueryLog.tStartTime.iHour = 0;
		tQueryLog.tStartTime.iMinute = 0;
		tQueryLog.tStartTime.iSecond = 0;
		tQueryLog.tStopTime.iYear = (short)year;
		tQueryLog.tStopTime.iMonth = (short)month;
		tQueryLog.tStopTime.iDay = (short)day;
		tQueryLog.tStopTime.iHour = (short)hour;
		tQueryLog.tStopTime.iMinute = (short)minute;
		tQueryLog.tStopTime.iSecond = (short)second;
		tQueryLog.write();

		NVS_LOG_DATA tSingleData = new NVS_LOG_DATA();
		QueryLogResult tResult = new QueryLogResult();
		
		int iOutTotalLen = 20 * tSingleData.size();
		int iSingleLen = tSingleData.size();
		
		int iRet = NetClient.SyncQuery(m_iLogonID, 0, NVSSDK.CMD_NETFILE_QUERY_LOG, tQueryLog.getPointer(), tQueryLog.size(), tResult.getPointer(), iOutTotalLen, iSingleLen);
		if (iRet < 0)
		{
			System.out.println("Err: NetClient_Query_V5 fail!iRet=" + iRet);
			return -1;
		}

		tResult.read();
		int iTotalCount = 0;
		int iCurrentCount = 0;
		IntByReference piTotalCount = new IntByReference();
		IntByReference piCurrentCount = new IntByReference();
		iRet = NetClient.NetLogGetLogCount(m_iLogonID, piTotalCount, piCurrentCount);
		if (iRet < 0)
		{
			System.out.println("Err: NetClient_NetLogGetLogCount fail!iRet=" + iRet);
			return -1;
		}

		iTotalCount = piTotalCount.getValue();
		iCurrentCount = piCurrentCount.getValue();
		System.out.println("TotalCount=" + iTotalCount + ", CurrentCount=" + iCurrentCount);
		for(int i = 0; i < iCurrentCount; ++i)
		{   
			String strLogContent = new String(tResult.tArry[i].szLogContent).trim();	
			System.out.println("Log" + i + ":" +  tResult.tArry[i].iLogType + ", " + strLogContent); 
		}

		return 0;
	}
	
	public int SyncSuspendVca()
	{
		VCASuspend tInPara = new VCASuspend();
		tInPara.iStatus = 0;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		tInPara.iDevType = 0;	//0-IPC, 1-NVR
		tInPara.write();

		VCASuspendResult tOutResult = new VCASuspendResult();
		tOutResult.iBufSize = tOutResult.size();
		tOutResult.iDevType = 0;
		tOutResult.write();
		int iRet = NetClient.SyncSetDevCfg(m_iLogonID, 0, NVSSDK.SYNC_NET_CLIENT_VCA_SUSPEND, tInPara.getPointer(), tInPara.size(), tOutResult.getPointer(), tOutResult.size());
		tOutResult.read();
		if (NVSSDK.RET_SUCCESS == iRet)
		{
			System.out.println("---------- Suspend Vca success!" + "iRet="+iRet + "iResult"+ tOutResult.iResult);
		}
		else if (NVSSDK.RET_SYNCSUSPENDVCA_CONFIGING == iRet)
		{
			System.out.println("Suspend Vca fail, Vca Configing by other client!");
		}
		else if (NVSSDK.RET_SYNCSUSPENDVCA_FAIL == iRet)
		{
			System.out.println("Suspend Vca fail, device not return code!");
		}

		return 0;
	}

	public int SyncOpenVca()
	{
		VCASuspend tInPara = new VCASuspend();
		tInPara.iStatus = 1;	//Status(0:Suspend intelligent analysis 1:Open intelligent analysis)
		tInPara.iDevType = 0;	//0-IPC, 1-NVR
		tInPara.write();

		VCASuspendResult tOutResult = new VCASuspendResult();
		tOutResult.iBufSize = tOutResult.size();
		tOutResult.iDevType = 0;
		tOutResult.write();
		int iRet = NetClient.SyncSetDevCfg(m_iLogonID, 0, NVSSDK.SYNC_NET_CLIENT_VCA_SUSPEND, tInPara.getPointer(), tInPara.size(), tOutResult.getPointer(), tOutResult.size());
		if (NVSSDK.RET_SUCCESS == iRet)
		{
			System.out.println("Open Vca success!");
		}
		else if (NVSSDK.RET_SYNCOPENVCA_CONFIGING == iRet)
		{
			System.out.println("Open Vca fail, Vca Configing by other client!");
		}
		else if (NVSSDK.RET_SYNCOPENVCA_FAIL == iRet)
		{
			System.out.println("Open Vca fail, device not return code!");
		}

		return 0;
	}
	
	public int downloadByFileMode()
	{
		Scanner scanInput = new Scanner(System.in);
		int iRet = NVSSDK.RET_FAILED;
		DOWNLOAD_FILE tFileInfo = new DOWNLOAD_FILE();
		tFileInfo.m_iSize = tFileInfo.size();
		tFileInfo.m_iSaveFileType = NVSSDK.DOWNLOAD_FILE_TYPE_SDV;
		System.out.print("Please enter the name of the file you want to download: ");
		String strDownloadFileName = scanInput.next();
		tFileInfo.m_cRemoteFilename = strDownloadFileName.getBytes(); //The name of the video to be downloaded, which is the name of the video queried from the device
		tFileInfo.m_cLocalFilename = strDownloadFileName.getBytes(); //The name of the video to be saved in the local download, the default is the same as the name of the queried device video
		tFileInfo.m_iPosition = -1; //Use the positioning function
		tFileInfo.m_iSpeed = 32; //Download speed, the maximum is 32, the old device is prone to interruptions when downloading at the maximum speed, so after the download is successful, the speed can be adjusted to 16 times the speed
		tFileInfo.m_iReqMode = 1; 	//Require data mode 1,Frame mode;0,Stream mode
		tFileInfo.write();
		IntByReference iConnectID = new IntByReference();
		iRet = NetClient.NetFileDownload(iConnectID, m_iLogonID, NVSSDK.DOWNLOAD_CMD_FILE, tFileInfo.getPointer(), tFileInfo.size());
		if (NVSSDK.RET_SUCCESS == iRet) {
			//Set the naked streaming callback, receive audio and video naked streaming data in the callback
			m_iConnectID = iConnectID.getValue();
			NetClient.SetRawFrameCallBack(m_iConnectID, cbkRawFrame, null);
			//Adjust the speed
			DOWNLOAD_CONTROL tControl = new DOWNLOAD_CONTROL();
			tControl.m_iSize = tControl.size();
			tControl.m_iPosition = -1;
			tControl.m_iSpeed = 16;
			tControl.m_iReqMode = 1;
			tControl.write();
			iRet = NetClient.NetFileDownload(iConnectID, m_iLogonID, NVSSDK.DOWNLOAD_CMD_CONTROL, tControl.getPointer(), tControl.size());
		} else {
			System.err.println("NetFileDownload:DOWNLOAD_CMD_FILE fail! iRet=" + iRet);
		}
		
		
		
		return iRet;
	};
	
	public int downloadByTimespanMode()
	{
		DOWNLOAD_TIMESPAN tDownloadTimeSpan = new DOWNLOAD_TIMESPAN();
		tDownloadTimeSpan.m_iSize = tDownloadTimeSpan.size();
		tDownloadTimeSpan.m_iSaveFileType = NVSSDK.DOWNLOAD_FILE_TYPE_SDV;
		tDownloadTimeSpan.m_iFileFlag = 0;	//0:Download multiple files  1:Download into a single file
		String strLocalSaveFileName = new String("myTimespanDownload.sdv");
		tDownloadTimeSpan.m_cLocalFilename = strLocalSaveFileName.getBytes();
		tDownloadTimeSpan.m_iChannelNO = 0;	//The channel number is assigned according to the actual downloaded device channel number
		tDownloadTimeSpan.m_iStreamNo = 0;	//Stream number: 0-main stream, 1-secondary stream
		//Download start time by time period
		tDownloadTimeSpan.m_tTimeBegin.iYear = 2019;
		tDownloadTimeSpan.m_tTimeBegin.iMonth = 8;
		tDownloadTimeSpan.m_tTimeBegin.iDay = 22;
		tDownloadTimeSpan.m_tTimeBegin.iHour = 11;
		tDownloadTimeSpan.m_tTimeBegin.iMinute = 30;
		tDownloadTimeSpan.m_tTimeBegin.iSecond = 0;
		//Download end time by time period
		tDownloadTimeSpan.m_tTimeEnd.iYear = 2019;
		tDownloadTimeSpan.m_tTimeEnd.iMonth = 8;
		tDownloadTimeSpan.m_tTimeEnd.iDay = 22;
		tDownloadTimeSpan.m_tTimeEnd.iHour = 11;
		tDownloadTimeSpan.m_tTimeEnd.iMinute = 36;
		tDownloadTimeSpan.m_tTimeEnd.iSecond = 0;
		
		tDownloadTimeSpan.m_iPosition = -1;	//Using the positioning function
		tDownloadTimeSpan.m_iSpeed = 32;	//Download speed, maximum 32, old devices are prone to interruptions when downloading at the maximum speed, so the speed can be adjusted to 16 times after the download is successful
		tDownloadTimeSpan.m_iReqMode = 1;	//1:down frame mode,0= Flow pattern; if (mode == 0) Device do not send download time !
		tDownloadTimeSpan.write();
		IntByReference iConnID = new IntByReference();
		int iRet = NetClient.NetFileDownload(iConnID, m_iLogonID, NVSSDK.DOWNLOAD_CMD_TIMESPAN, tDownloadTimeSpan.getPointer(), tDownloadTimeSpan.size());
	 	if (NVSSDK.RET_SUCCESS == iRet)
	 	{
			//Set the naked streaming callback, receive audio and video naked streaming data in the callback
	 		m_iConnectID = iConnID.getValue();
			NetClient.SetRawFrameCallBack(m_iConnectID, cbkRawFrame, null);	
			//Adjust the speed
			DOWNLOAD_CONTROL tControl = new DOWNLOAD_CONTROL();
			tControl.m_iSize = tControl.size();
			tControl.m_iPosition = -1;
			tControl.m_iSpeed = 16;
			tControl.m_iReqMode = 1;
			tControl.write();
			NetClient.NetFileDownload(iConnID, m_iLogonID, NVSSDK.DOWNLOAD_CMD_CONTROL, tControl.getPointer(), tControl.size());
	 	} else {
			System.err.println("NetFileDownload:DOWNLOAD_CMD_TIMESPAN fail! iRet=" + iRet);
		}
	 	
		return iRet;
	}
	
	public void stopDownload()
	{
		if (m_iConnectID >= 0) {
			int iRet = NetClient.NetFileStopDownloadFile(m_iConnectID);
			m_iConnectID = -1;
			System.out.println("NetFileStopDownloadFile(" + iRet + ")");
		}
	}
	
	MAIN_NOTIFY cbkMain = new MAIN_NOTIFY() {
		public void MainNotify(int iLogonID, int wParam, Pointer lParam, Pointer noitfyUserData) {
			int iMsgType = wParam & 0xFFFF;
			switch (iMsgType) {
			case NVSSDK.WCM_DWONLOAD_FINISHED: {
				System.out.println("MainNotify:WCM_DWONLOAD_FINISHED!download successful!");
				if(null == lParam)
				{
					m_iConnectID = 0;
				}
				else {
					m_iConnectID = (int)Pointer.nativeValue(lParam);
				}
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;	
			} case NVSSDK.WCM_DWONLOAD_FAULT: {
				System.out.println("MainNotify:WCM_DWONLOAD_FAULT!download failed!");
				if(null == lParam)
				{
					m_iConnectID = 0;
				}
				else {
					m_iConnectID = (int)Pointer.nativeValue(lParam);
				}
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;
			} case NVSSDK.WCM_DOWNLOAD_INTERRUPT: {
				System.out.println("MainNotify:WCM_DOWNLOAD_INTERRUPT!Download interrupted!");
				if(null == lParam)
				{
					m_iConnectID = 0;
				}
				else {
					m_iConnectID = (int)Pointer.nativeValue(lParam);
				}
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;
			} default: break; 
			}
		}
	};
	
	ALARM_NOTIFY cbkAlarm = new ALARM_NOTIFY() {
		public void AlarmNotify(int _iLogonID, int _iChannel,
				int _iAlarmState, int _iAlarmType, Pointer _pUserData) {
		}
	};
	
	PARACHANGE_NOTIFY cbkParaChange = new PARACHANGE_NOTIFY() {
		public void ParaChangeNotify(int iLogonID, int iChannel, int paraType,
				Pointer para, Pointer noitfyUserData) {
		}
	};
	
	RECVDATA_NOTIFY cbkRecvData = new RECVDATA_NOTIFY() {
		public void RecvDataNotify(int _iConnectID, Pointer data, int len, int _iFlag,
				Pointer _lpUserData) {
			System.out.println("[RECVDATA_NOTIFY] ConnID(" + _iConnectID + "),DataLen("
					+ len + ")");
		}
	};
	
	public int SaveRawData(int type, Pointer data, int len)
	{
		FileOutputStream fopRawVideo = null;
		File fileRawVideo;
		FileOutputStream fopRawAudio = null;
		File fileRawAudio;
		try
        {
        	if (null == fopRawVideo)
    		{
        		fileRawVideo = new File("myVideoRawData.raw");
        		fopRawVideo = new FileOutputStream(fileRawVideo, true);

        		// if file doesnt exists, then create it
        		if (!fileRawVideo.exists())
        		{
        			fileRawVideo.createNewFile();
        		}
    		}
        	
        	if (null == fopRawAudio)
    		{
        		fileRawAudio = new File("myAudioRawData.raw");
        		fopRawAudio = new FileOutputStream(fileRawAudio, true);

        		// if file doesnt exists, then create it
        		if (!fileRawAudio.exists())
        		{
        			fileRawAudio.createNewFile();
        		}
    		}
        	
        	// get the content in bytes
        	byte[] contentInBytes = data.getByteArray(0, len);	
        	if(NVSSDK.AUDIO_FRAME == type) {
        		//Save raw streaming audio data
        		fopRawAudio.write(contentInBytes);
        		fopRawAudio.flush();
        		fopRawAudio.close();
        	} else {
        		//Save raw streaming video data
        		fopRawVideo.write(contentInBytes);
        		fopRawVideo.flush();
        		fopRawVideo.close();
        	}
        } 
        catch (IOException e)
        {
        	e.printStackTrace();
        } 
        finally
        {
        	try {
            	if (null != fopRawVideo) {
            		fopRawVideo.close();
            		fopRawVideo = null;
            	}
            	if (null != fopRawAudio) {
            		fopRawAudio.close();
            		fopRawAudio = null;
            	}
        	}
        	catch (IOException e)
        	{
        		e.printStackTrace();
        	}
        }
        
        return 0;
	}
	
	public static void main(String args[])
	{
		System.out.println("SyncJavaDemo!");
		SyncBusiness tSyncBusiness = new SyncBusiness();
		
		//Initialize the SDK
		tSyncBusiness.sdkInit(); 
		
		//Call the SDK interface to implement business functions
		tSyncBusiness.makeOptCmd();
		tSyncBusiness.testSdkApi();
		
		//Deinitialize the SDK and clean up the SDK resources
		tSyncBusiness.sdkUnInit(); 
	}
}

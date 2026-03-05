package src;

import java.util.Calendar;
import java.util.Scanner; 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.RECVDATA_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.ENCODERINFO;
import src.NVSSDK.NETFILE_QUERY_V5;
import src.NVSSDK.QueryFileChannel;
import src.NVSSDK.NVS_FILE_DATA_EX;
import src.NVSSDK.DOWNLOAD_FILE;
import src.NVSSDK.DOWNLOAD_TIMESPAN;
import src.NVSSDK.DOWNLOAD_CONTROL;
import src.NVSSDK.RAWFRAME_NOTIFY;
import src.NVSSDK.RAWFRAME_INFO;
import src.NVSSDK.ArrayQueryFileChannel;


import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;


public class Playback {
	
	int m_iLogonID = -1;
	int m_iConnectID = -1;
	int m_iTotalCount = 0;
	int m_iCurrentCount = 0;
	String[] suffix = {".sdv", ".ps", "", ".ps", ".mp4", ".avi", ".ts"};
	
	MAIN_NOTIFY cbkMain = new MAIN_NOTIFY() {
		public void MainNotify(int iLogonID, int wParam, Pointer lParam, Pointer noitfyUserData) {
			int iMsgType = wParam & 0xFFFF;
			switch (iMsgType) {
			case NVSSDK.WCM_LOGON_NOTIFY: {
				try {
					int iLogonStatus = NetClient.GetLogonStatus(iLogonID);
					ENCODERINFO tDevInfo = new ENCODERINFO();
					NetClient.GetDevInfo(iLogonID, tDevInfo);
					
					String strIP = new String(tDevInfo.m_cEncoder).trim();			
					String strID = new String(tDevInfo.m_cFactoryID).trim();			
					LogonNotify(strIP, strID, iLogonID, iLogonStatus);
				} catch(Exception e) {
					e.printStackTrace();
				}
				break;
			} case NVSSDK.WCM_QUERYFILE_FINISHED: {
				try {
					IntByReference iTotalCount = new IntByReference();
					IntByReference iCurrentCount = new IntByReference();
					int iRet = NetClient.NetFileGetFileCount(m_iLogonID, iTotalCount, iCurrentCount);
					if(0 == iRet) {
						m_iTotalCount = iTotalCount.getValue();
						m_iCurrentCount = iCurrentCount.getValue();
						System.out.println("MainNotify:WCM_QUERYFILE_FINISHED! m_iTotalCount= "
								+ m_iTotalCount + ", m_iCurrentCount=" + m_iCurrentCount);	
					} else {
						System.out.println("NetFileGetFileCount fail!iRet= " + iRet) ;
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				break;
			} case NVSSDK.WCM_DWONLOAD_FINISHED: {
				System.out.println("MainNotify:WCM_DWONLOAD_FINISHED!download successful!");
				if(null == lParam)
				{
					m_iConnectID = 0;
				}
				else {
					m_iConnectID = (int) Pointer.nativeValue(lParam);
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
					m_iConnectID = (int) Pointer.nativeValue(lParam);
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
					m_iConnectID = (int) Pointer.nativeValue(lParam);
				}
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;
			} default: break; 
			}
		}
	};
	
	public void LogonNotify(String strIP, String strID, int iLogonID, int iLogonState) {
		String strMsg = new String();
		m_iLogonID = -1;
		switch (iLogonState) {
		case NVSSDK.LOGON_SUCCESS: {
			m_iLogonID = iLogonID;
			strMsg = "LOGON_SUCCESS";
			break;
		} case NVSSDK.LOGON_FAILED: {
			strMsg = "LOGON_FAILED";
			break;
		} case NVSSDK.LOGON_TIMEOUT: {
			strMsg = "LOGON_TIMEOUT";
			break;
		} case NVSSDK.LOGON_RETRY: {
			strMsg = "LOGON_RETRY";
			break;
		} case NVSSDK.LOGON_ING: {
			strMsg = "LOGON_ING";
			break;
		} default: {
			System.out.println("[WCM_LOGON_NOTIFY][" + iLogonState + "] IP("
					+ strIP + "),ID(" + strID + "),LogonID(" + iLogonID + ")");
			break;
		}
		}
		System.out.println("[WCM_LOGON_NOTIFY][" + strMsg + "] IP(" + strIP
				+ "),ID(" + strID + "),LogonID(" + iLogonID + ")");
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
		public void RecvDataNotify(int _ulID, Pointer data, int len, int _iFlag,
				Pointer _lpUserData) {
			System.out.println("[RECVDATA_NOTIFY] ConnID(" + _ulID + "),DataLen("
					+ len + ")");
		}
	};
	
	public void showQueryFileInfo() {
		int iRet = -1;
		NVS_FILE_DATA_EX fileInfo = new NVS_FILE_DATA_EX();
		fileInfo.iSize = fileInfo.size();
		for(int i=0; i < m_iCurrentCount; i++)
		{
			iRet = NetClient.NetFileGetQueryfileEx(m_iLogonID, i, fileInfo);
	 		if(0 == iRet)
	 		{
	 			String strFileName = new String(fileInfo.tFileData.cFileName).trim();	
	 			
	 			int iStartYear = fileInfo.tFileData.tStartTime.iYear;
	 			int iStartMonth = fileInfo.tFileData.tStartTime.iMonth;
	 			int iStartDay = fileInfo.tFileData.tStartTime.iDay;
	 			int iStartHour = fileInfo.tFileData.tStartTime.iHour;
	 			int iStartMinute = fileInfo.tFileData.tStartTime.iMinute;
	 			int iStartSecond = fileInfo.tFileData.tStartTime.iSecond;
	 			
	 			int iStopYear = fileInfo.tFileData.tStopTime.iYear;
	 			int iStopMonth = fileInfo.tFileData.tStopTime.iMonth;
	 			int iStopDay = fileInfo.tFileData.tStopTime.iDay;
	 			int iStopHour = fileInfo.tFileData.tStopTime.iHour;
	 			int iStopMinute = fileInfo.tFileData.tStopTime.iMinute;
	 			int iStopSecond = fileInfo.tFileData.tStopTime.iSecond;
	 			
	 			System.out.println("fileIdx=" + i + ", fileName=" + strFileName
	 					+ ", chNo=" + fileInfo.tFileData.iChannel + ", fileSize=" + fileInfo.tFileData.iFileSize
	 					+ ", fileStartTime=" + iStartYear + "-" + iStartMonth + "-" + iStartDay + "-"
	 					+ iStartHour + "-" + iStartMinute + "-" + iStartSecond
	 					+ ", fileStopTime=" + iStopYear + "-" + iStopMonth + "-" + iStopDay + "-"
	 					+ iStopHour + "-" + iStopMinute + "-" + iStopSecond);
	 		} else {
	 			System.out.println("NetFileGetQueryfileEx fail! iRet=" + iRet);
	 		}
		}
	}
	
	private int SDKInit() {
		SDK_VERSION ver = new SDK_VERSION();
		int iRet = NetClient.GetVersion(ver);
		System.out.println("[SDK_VERSION]" + ver.m_cVerInfo);

		iRet = NetClient.SetNotifyFunction(cbkMain, cbkAlarm, cbkParaChange);
		System.out.println("SetNotifyFunction(" + iRet + ")");

		iRet = NetClient.Startup();
		System.out.println("Startup(" + iRet + ")");

		return 0;
	};
	
	
	public int LogonDevice(String strIP, int iPort, String strUserName, String strUserPwd)
	{
		System.out.println("Logon" + strIP + ":" + iPort + "-" +strUserName + "-" + strUserPwd);
		while(true){
			m_iLogonID = NetClient.Logon("", strIP, strUserName, strUserPwd, "",iPort);
			int iLogonStatus = NetClient.GetLogonStatus(m_iLogonID);
			if(iLogonStatus == NVSSDK.LOGON_SUCCESS){
				break;
			}
			
	        try {
	        	Thread.currentThread();
				Thread.sleep(1000); 
	        } catch(InterruptedException e) {
	            System.err.println("Interrupted");
	        }
		}
		
		return 0;
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
	};
	
	RAWFRAME_NOTIFY cbkRawFrame = new RAWFRAME_NOTIFY() {
		public void rawFrameNotify(int _uiID, Pointer _pcData, int _iLen, RAWFRAME_INFO _ptRawFrameInfo, Pointer _lpUserData) {
           //print data information
			System.out.println("recvRawData: _uiID=" + _uiID + ", _iLen=" + _iLen + ", nType=" + _ptRawFrameInfo.nType);
            
           //The upper layer can save and process bare stream data
            if (null != _pcData && 0 != _iLen) {
            	SaveRawData(_ptRawFrameInfo.nType, _pcData, _iLen);
            }
		}
	};
	
	public int PlaybackByFileMode()
	{
		Scanner scanInput = new Scanner(System.in);
		//Inquire
		NETFILE_QUERY_V5 tMultiChanQueryFile = new NETFILE_QUERY_V5();
		tMultiChanQueryFile.iBufSize = tMultiChanQueryFile.size();
		tMultiChanQueryFile.iQueryChannelNo = 0;	//query channel no, 0x7FFFFFFF means query all channel
		tMultiChanQueryFile.iStreamNo = 0;			//Query stream number, 0-main stream, 1-sub stream
		tMultiChanQueryFile.iType = 0xFF;
		
		//current time  
		Calendar cas = Calendar.getInstance();  
		int year = cas.get(Calendar.YEAR);//Get the year
		int month=cas.get(Calendar.MONTH)+1;//Get the month
		int day=cas.get(Calendar.DATE);//Get the day
		int hour=cas.get(Calendar.HOUR_OF_DAY);//hour
		int minute=cas.get(Calendar.MINUTE);//minute
		int second=cas.get(Calendar.SECOND);//second
		
		// query start time
		tMultiChanQueryFile.tStartTime.iYear = (short)year;
		tMultiChanQueryFile.tStartTime.iMonth = (short)month;
		tMultiChanQueryFile.tStartTime.iDay = (short)day;
		tMultiChanQueryFile.tStartTime.iHour = 0;
		tMultiChanQueryFile.tStartTime.iMinute = 0;
		tMultiChanQueryFile.tStartTime.iSecond = 0;
		// query end time
		tMultiChanQueryFile.tStopTime.iYear = (short)year;
		tMultiChanQueryFile.tStopTime.iMonth = (short)month;
		tMultiChanQueryFile.tStopTime.iDay = (short)day;
		tMultiChanQueryFile.tStopTime.iHour = (short)hour;
		tMultiChanQueryFile.tStopTime.iMinute = (short)minute;
		tMultiChanQueryFile.tStopTime.iSecond = (short)second;
		
		tMultiChanQueryFile.iPageSize = 20; //The page size of each query, that is, the number of records per query
		tMultiChanQueryFile.iPageNo = 0; //Query page number, for example, there are 100 records in total, and the page size of each query is 20, then the page number is 0, 1, 2, 3, 4
		tMultiChanQueryFile.iFiletype = 1;	//File type 0:all,1:Video,2:picture
		tMultiChanQueryFile.iTriggerType = 0x7FFFFFFF;
		tMultiChanQueryFile.iTrigger = 0;
		tMultiChanQueryFile.iQueryChannelCount = 1; //Number of channels for this query, some nvr supports batch query use, the default single channel query can be used
		ArrayQueryFileChannel arrayQueryFileChannel = new ArrayQueryFileChannel(); //Batch query channel array, default query by single channel
		QueryFileChannel tQueryFileChannel0 = new QueryFileChannel();
		QueryFileChannel tQueryFileChannel1 = new QueryFileChannel();
		tQueryFileChannel0.iChannelNo = 0;
		tQueryFileChannel0.iStreamNo = 0;
		tQueryFileChannel1.iChannelNo = 0;
		tQueryFileChannel1.iStreamNo = 1;
		arrayQueryFileChannel.tArry[0] = tQueryFileChannel0;
		arrayQueryFileChannel.tArry[1] = tQueryFileChannel1;
		tMultiChanQueryFile.ptChannelList = arrayQueryFileChannel.getPointer();
		tMultiChanQueryFile.iBufferSize = tQueryFileChannel0.size(); //Query structure size
		tMultiChanQueryFile.write();
		int iRet = NetClient.Query_V4(m_iLogonID, NVSSDK.CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE, 0, tMultiChanQueryFile.getPointer(), tMultiChanQueryFile.size());
		if (0 == iRet) {
			System.out.println("Query_V4:CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE succ! iRet=" + iRet);
		} else {
			System.err.println("Query_V4:CMD_NETFILE_MULTI_CHANNEL_QUERY_FILE fail! iRet=" + iRet);
			return -1;
		}
		
		//Wait for query result
		while(0 == m_iCurrentCount) {
	        try {
	        	Thread.currentThread();
				Thread.sleep(1000); 
	        } catch(InterruptedException e) {
	            System.err.println("Interrupted");
	        }
		}
		
		//Display query file information
		showQueryFileInfo();

		//download
		DOWNLOAD_FILE tFileInfo = new DOWNLOAD_FILE();
		tFileInfo.m_iSize = tFileInfo.size();
		System.out.print("Please enter the video file type (sdv=0;PS=3;MP4=4;AVI=5;TS=6): ");
		int iSaveFileType = scanInput.nextInt();
		tFileInfo.m_iSaveFileType = iSaveFileType;
		System.out.print("Please enter the file name to download: ");
		String strDownloadFileName = scanInput.next();
		tFileInfo.m_cRemoteFilename = strDownloadFileName.getBytes(); //The name of the video to be downloaded, which is the name of the video queried from the device
		String strLocalFileName = strDownloadFileName.substring(0, strDownloadFileName.lastIndexOf(".")) + suffix[iSaveFileType];
		tFileInfo.m_cLocalFilename = strLocalFileName.getBytes(); //The name of the video to be saved in the local download, the default is the same as the name of the queried device video
		tFileInfo.m_iPosition = -1; //Use the positioning function
		tFileInfo.m_iSpeed = 32; //Download speed, the maximum is 32, the old device is prone to interruptions when downloading at the maximum speed, so after the download is successful, the speed can be adjusted to 16 times the speed
		tFileInfo.m_iReqMode = 1; 	//Require data mode 1,Frame mode;0,Stream mode
		tFileInfo.write();
		IntByReference iConnID = new IntByReference();
		iRet = NetClient.NetFileDownload(iConnID, m_iLogonID, NVSSDK.DOWNLOAD_CMD_FILE, tFileInfo.getPointer(), tFileInfo.size());
		if (0 == iRet) {
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
			iRet = NetClient.NetFileDownload(iConnID, m_iLogonID, NVSSDK.DOWNLOAD_CMD_CONTROL, tControl.getPointer(), tControl.size());
		} else {
			System.err.println("NetFileDownload:DOWNLOAD_CMD_FILE fail! iRet=" + iRet);
		}
		
		// Get download progress regularly
		int iPregress = 0; // download progress
		int iDownLoadSize = 0; // download file size
		while(100 != iPregress) {
	        try {
	        	Thread.currentThread();
	        	IntByReference piPos = new IntByReference();
	        	IntByReference piDLSize = new IntByReference();
	        	iRet = NetClient.NetFileGetDownloadPos(m_iConnectID, piPos, piDLSize);
	        	iPregress = piPos.getValue();
	        	iDownLoadSize = piDLSize.getValue();
	        	System.out.println("Pregress=" + iPregress + ", DownloadSize=" + iDownLoadSize);
				Thread.sleep(1000); 
	        } catch(InterruptedException e) {
	            System.err.println("Interrupted");
	        }
		}
		
		iRet = NetClient.NetFileStopDownloadFile(m_iConnectID);
		
		return iRet;
	};
	
	public int PlaybackByTimespanMode()
	{
		DOWNLOAD_TIMESPAN tDownloadTimeSpan = new DOWNLOAD_TIMESPAN();
		tDownloadTimeSpan.m_iSize = tDownloadTimeSpan.size();
		Scanner scanInput = new Scanner(System.in);
		System.out.print("Please enter the video file type (sdv=0;PS=3;MP4=4;AVI=5;TS=6): ");
		int iSaveFileType = scanInput.nextInt();
		tDownloadTimeSpan.m_iSaveFileType = iSaveFileType;
		tDownloadTimeSpan.m_iFileFlag = 0;	//0:Download multiple files  1:Download into a single file

		String strLocalSaveFileName = new String("myTimespanDownload"+suffix[iSaveFileType]);
		tDownloadTimeSpan.m_cLocalFilename = strLocalSaveFileName.getBytes();
		tDownloadTimeSpan.m_iChannelNO = 0; //The channel number is assigned according to the actual downloaded device channel number
		tDownloadTimeSpan.m_iStreamNo = 0; //Stream number: 0-main stream, 1-secondary stream
		
		//current time  
		Calendar cas = Calendar.getInstance();  
		int year = cas.get(Calendar.YEAR);//Get the year
		int month=cas.get(Calendar.MONTH)+1;//Get the month
		int day=cas.get(Calendar.DATE);//Get the day
		int hour=cas.get(Calendar.HOUR_OF_DAY);//hour
		int minute=cas.get(Calendar.MINUTE);//minute
		int second=cas.get(Calendar.SECOND);//second
		
		//Download start time by time period
		tDownloadTimeSpan.m_tTimeBegin.iYear = (short)year;
		tDownloadTimeSpan.m_tTimeBegin.iMonth = (short)month;
		tDownloadTimeSpan.m_tTimeBegin.iDay = (short)day;
		tDownloadTimeSpan.m_tTimeBegin.iHour = 0;
		tDownloadTimeSpan.m_tTimeBegin.iMinute = 0;
		tDownloadTimeSpan.m_tTimeBegin.iSecond = 0;
		//Download end time by time period
		tDownloadTimeSpan.m_tTimeEnd.iYear = (short)year;
		tDownloadTimeSpan.m_tTimeEnd.iMonth = (short)month;
		tDownloadTimeSpan.m_tTimeEnd.iDay = (short)day;
		tDownloadTimeSpan.m_tTimeEnd.iHour = (short)hour;
		tDownloadTimeSpan.m_tTimeEnd.iMinute = (short)minute;
		tDownloadTimeSpan.m_tTimeEnd.iSecond = (short)second;
		
		tDownloadTimeSpan.m_iPosition = -1; //Use the positioning function
		tDownloadTimeSpan.m_iSpeed = 32; //Download speed, maximum 32, old devices are prone to interruptions when downloading at the maximum speed, so after the download is successful, the speed can be adjusted to 16 times the speed
		tDownloadTimeSpan.m_iReqMode = 1;	//1:down frame mode,0= Flow pattern; if (mode == 0) Device do not send download time !
		tDownloadTimeSpan.write();
		IntByReference iConnID = new IntByReference();
		int iRet = NetClient.NetFileDownload(iConnID, m_iLogonID, NVSSDK.DOWNLOAD_CMD_TIMESPAN, tDownloadTimeSpan.getPointer(), tDownloadTimeSpan.size());
	 	if (0 == iRet)
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
	};
	
	public static void main(String args[])
	{
		System.out.println("PlaybackDemo!");
		Playback tPlayback = new Playback();
		tPlayback.SDKInit(); //Initialize SDK
		
		Scanner scanInput = new Scanner(System.in);
		System.out.print("Please enter the device IP: ");
		String strDevIP = scanInput.next();
		System.out.print("Please enter the device port: ");
		int iDevPort = scanInput.nextInt();
		System.out.print("Please enter username: ");
		String strUserName = scanInput.next();
		System.out.print("Please enter password: ");
		String strUserPwd = scanInput.next();
		tPlayback.LogonDevice(strDevIP, iDevPort, strUserName, strUserPwd);//Logon the device
		
		System.out.print("Please enter the mode (0-file mode, 1-time period mode): ");
		int modePlayback = scanInput.nextInt();
		if (0 == modePlayback) {
			tPlayback.PlaybackByFileMode();
		} else {
			tPlayback.PlaybackByTimespanMode();
		}
        
		while(true){	
	        try {
	        	Thread.currentThread();
				Thread.sleep(1000); 
	        } catch(InterruptedException e) {
	            System.err.println("Interrupted");
	        }
		}   
	}	
}

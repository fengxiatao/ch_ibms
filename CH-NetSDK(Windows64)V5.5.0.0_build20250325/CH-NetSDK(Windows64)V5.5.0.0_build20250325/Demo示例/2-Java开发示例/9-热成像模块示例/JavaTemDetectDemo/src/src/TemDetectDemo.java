package src;

import src.NVSSDK.LogonPara;
import src.NVSSDK.ActiveNetWanInfo;
import src.NVSSDK.DsmOnline;
import src.NVSSDK.LogonActiveServer;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.ENCODERINFO;

//Face parameter correlation
import src.NVSSDK.BlackbodyCorrection;
import src.NVSSDK.BodyTempCorrect;
import src.NVSSDK.FaceAlarmParamArr;
import src.NVSSDK.FaceLibQuery;
import src.NVSSDK.FaceLibQueryResult;
import src.NVSSDK.FacePicData;
import src.NVSSDK.FacePicStream;
import src.NVSSDK.FaceReply;
import src.NVSSDK.FaceLibEdit;
import src.NVSSDK.FaceLibDelete;
import src.NVSSDK.FaceEdit;
import src.NVSSDK.FaceDelete;
import src.NVSSDK.FaceQuery;
import src.NVSSDK.FaceQueryResult;
import src.NVSSDK.FaceLibQueryResultArr;
import src.NVSSDK.FaceQueryResultArr;
import src.NVSSDK.IntelligentCorretct;
import src.NVSSDK.NET_PICSTREAM_NOTIFY;
import src.NVSSDK.NetPicPara;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.PicData;
import src.NVSSDK.TemperatureScaleType;
import src.NVSSDK.VCASuspendResult;
import src.NVSSDK.VCATemDetect;
import src.NVSSDK.VcaStatue;
import src.NVSSDK.FaceAlarmParam;
import src.NVSSDK.FaceAttribute;
import src.NVSSDK.FaceAlarmParamIn;
import src.NVSSDK.AnyScene;
import src.NVSSDK.FaceDetectArithmetic;
import src.NVSSDK.DOWNLOAD_TIMESPAN;
import src.NVSSDK.DOWNLOAD_CONTROL;
import src.NVSSDK.PicStreamUploadParam;
import src.NVSSDK.FuncAbilityLevel;
import src.NVSSDK.DownloadFile;
import src.NVSSDK.FaceCutEx;
import src.NVSSDK.FaceCutQueryResult;
import src.NVSSDK.FaceSearch;
import src.NVSSDK.FaceCutQueryResults;
import src.NVSSDK.QueryChanNo;
import src.NVSSDK.FaceSearchSnapProcess;
import src.NVSSDK.FaceSearchSnapQuery;
import src.NVSSDK.FaceSearchSnapResult;
import src.NVSSDK.FaceSearchSnap;
import src.NVSSDK.NetFileQueryVca;
import src.NVSSDK.NetFileQueryVcaResult;
import src.NVSSDK.FaceSearchSnapResults;
import src.NVSSDK.QueryChanNos;
import src.NVSSDK.NetFileQueryVcaResults;
import src.NVSSDK.FaceLibSyncStart;
import src.NVSSDK.FaceLibSyncQuery;
import src.NVSSDK.FaceLibSyncQueryResultArray;
import src.NVSSDK.STR_Para;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Scanner;

public class TemDetectDemo {
	
	int m_iLogonID = -1;
	int m_iConnectID = -1;
	int m_iLibKey = 0;		//Save the last library key value for easy modification and deletion
	int m_iFaceKey = 0;		//Save the key value of the last face for easy modification and deletion
	int m_iVcaStatus = 0;	//Intelligent energy analysis state
	int iChannelNum = 0; 
	Scanner scanIn = new Scanner(System.in);
	
	String m_strSavePath="PicStream";
	
	//Byte [] to string, java8 can use Base64 to directly transfer
	String ByteToStr(byte [] bt) {
		int len= bt.length;
		String str = new String();
		byte[] bLast = new byte[2];
		for(int i= 0; i < len; ++i) {
			if(0 == bt[i]) {
				break;
			} else if (bt[i] > 0){	//English or symbols
				byte[] b = new byte[]{bt[i]}; 
				String s = new String(b);
				str += s;
			} else {		//GB2312 characters, 2 bytes are 1 GB2312 characters
				if(0 != bLast[0]){
					bLast[1] = bt[i];
					String s = new String(bLast);
					str += s;
					bLast[0] = 0;
				} else {					
					bLast[0] = bt[i];
				}
			}			
		}
		return str;
	};
	
	//Main callback
	MAIN_NOTIFY cbkMain = new MAIN_NOTIFY() 
	{
		public void MainNotify(int iLogonID, int wParam, Pointer lParam, Pointer noitfyUserData) {
			int iMsgType = wParam & 0xFFFF;
			switch (iMsgType) {
			case NVSSDK.WCM_LOGON_NOTIFY: {
				try {
					ENCODERINFO tDevInfo = new ENCODERINFO();
					NetClient.GetDevInfo(iLogonID, tDevInfo);					
					String strIP = new String(tDevInfo.m_cEncoder).trim();			
					String strID = new String(tDevInfo.m_cFactoryID).trim();	
					//Processing device login status
					LogonNotify(strIP, strID, iLogonID, wParam >> 16);	
				} catch(Exception e) {
					e.printStackTrace();
				}
				break;
			} case NVSSDK.WCM_VCA_SUSPEND: {
				VCASuspendResult tParam = new VCASuspendResult();
				tParam.iSize = tParam.size();
				tParam.write();
				NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_VCA_SUSPEND, 0, tParam.getPointer(), tParam.size());
				tParam.read();
				m_iVcaStatus = tParam.iResult;		//result
				if (NVSSDK.VCA_SUSPEND_STATUS_PAUSE == tParam.iStatus) {
					if (NVSSDK.VCA_SUSPEND_RESULT_SUCCESS == tParam.iResult) {
						System.out.println("smart analysis pauses successfully.0");
					} else {
						System.out.println("Smart analysis pause failed.");
					}				
				}
				break;	
			} case NVSSDK.WCM_DWONLOAD_FINISHED: {
				System.out.println("MainNotify:WCM_DWONLOAD_FINISHED! Download successful!");
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;	
			} case NVSSDK.WCM_DWONLOAD_FAULT: {
				System.out.println("MainNotify:WCM_DWONLOAD_FAULT! Download failed!");
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;
			} case NVSSDK.WCM_DOWNLOAD_INTERRUPT: {
				System.out.println("MainNotify:WCM_DOWNLOAD_INTERRUPT! Download interrupted!");
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;
			} default: break; 
			}
		}
	};
	
	PARACHANGE_NOTIFY cbkParaChange = new PARACHANGE_NOTIFY(){
		public void ParaChangeNotify(int iLogonID, int iChannel, int paraType, Pointer para, Pointer noitfyUserData) {
			if (135 != paraType) {
				return;
			}
			
			STR_Para tPara = new STR_Para();
			tPara.write();
            int iSize = tPara.size();
            Pointer pParaBuffer = tPara.getPointer();
            byte[] RecvBuffer = para.getByteArray(0, iSize);
            pParaBuffer.write(0, RecvBuffer, 0, iSize);
            tPara.read();
            
            long iIn = tPara.m_iPara[0];
            long iOut = tPara.m_iPara[1];
            long iInDiff = tPara.m_iPara[2];
            long iOutDiff = tPara.m_iPara[3];
            if (iIn > 0 || iOut > 0) {
            	System.out.println("[People Counting] ID(" + iLogonID + "), in(" + iIn + "), out(" + iOut +").");
            }
		}
	};
	
	//Login status processing
	public void LogonNotify(String strIP, String strID, int iLogonID, int iLogonState) {
		String strMsg = new String();
		m_iLogonID = -1;
		iLogonState = NetClient.GetLogonStatus(iLogonID);
		if(NVSSDK.LOGON_SUCCESS == iLogonState){			//Login successful
			m_iLogonID = iLogonID;
			strMsg = "LOGON_SUCCESS";
			
			
			IntByReference piDigitChannelNum = new IntByReference();
			int iRet = NetClient.GetChannelNum(m_iLogonID, piDigitChannelNum);
			if (0 == iRet)
			{
				iChannelNum = piDigitChannelNum.getValue();
				System.out.println("GetDigitalChannelNum success! iDigitChannelNum=" + iChannelNum);
			}
			else
			{
				System.err.println("GetDigitalChannelNum failed! iRet=" + iRet);
			}
			
			//Synchronize the local time to the device after successful login and device calibration
			Calendar c = Calendar.getInstance();
			NetClient.SetTime(iLogonID, c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE), 
					c.get(Calendar.HOUR_OF_DAY),  c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
			//Turn on receiving picture stream
			StartSnap();
		} else if (NVSSDK.LOGON_FAILED == iLogonState) {	//Login failed
			strMsg = "LOGON_FAILED";
		} else if (NVSSDK.LOGON_TIMEOUT == iLogonState) {	//login timeout
			strMsg = "LOGON_TIMEOUT";
		} else if (NVSSDK.LOGON_RETRY == iLogonState) {		//Log in again
			strMsg = "LOGON_RETRY";
		} else if (NVSSDK.LOGON_ING == iLogonState) {		//Logging in
			strMsg = "LOGON_ING";
		} else {
			strMsg = "LOGON_UNKNOW" + iLogonState;
		}
		System.out.println("[WCM_LOGON_NOTIFY][" + strMsg + "] IP(" + strIP
				+ "),ID(" + strID + "),LogonID(" + iLogonID + ")");
	}
	
	//Login device
	public int LogonDevice() {
		int iRet = -1;
		int iLogonType = NVSSDK.SERVER_NORMAL;
		System.out.print("Please input LogonType(0--Normal  1--Active): ");
		iLogonType = scanIn.nextInt();
		if (NVSSDK.SERVER_ACTIVE == iLogonType) {
			//Active mode login logic	
			System.out.print("Please input local listen port:");
			int iLocalListenPort = scanIn.nextInt();
			iRet = NetClient.SetPort(iLocalListenPort, 0);
			if(0 != iRet) {
				System.out.println("NetClient_SetPort fail!");
				return -1;
			}

			ActiveNetWanInfo tLocalWanInfo = new ActiveNetWanInfo();
			tLocalWanInfo.iSize = tLocalWanInfo.size();
			System.out.print("Please input wan IP: ");
			String strWanIP = scanIn.next();
			tLocalWanInfo.cWanIP = strWanIP.getBytes();
			System.out.print("Please input local wan port:");
			tLocalWanInfo.iWanPort = scanIn.nextInt();
			tLocalWanInfo.write();
			iRet = NetClient.SetDsmConfig(NVSSDK.DSM_CMD_SET_NET_WAN_INFO, tLocalWanInfo.getPointer(), tLocalWanInfo.size());
			if(0 != iRet) {
				System.out.println("NetClient_SetDsmConfig:DSM_CMD_SET_NET_WAN_INFO fail!");
				return -1;
			}

			DsmOnline tOnline = new DsmOnline();
			System.out.print("Please input ProductID: ");
			String strProductID = scanIn.next();
			tOnline.iSize = tOnline.size();
			tOnline.cProductID = strProductID.getBytes();
			tOnline.write();
			int iOutTime = 0;
			while(true) {
				//Get registration online status
				NetClient.GetDsmRegstierInfo(NVSSDK.DSM_CMD_GET_ONLINE_STATE, tOnline.getPointer(), tOnline.size());
				tOnline.read();
				if (NVSSDK.DSM_STATE_ONLINE == tOnline.iOnline) {
					break;
				}
				if (iOutTime >= 30)
				{
					System.out.println("Device not register!\n");
					return -1;
				}
				
		        try {
		        	Thread.currentThread();
					Thread.sleep(1000); 
		        } catch(InterruptedException e) {
		            System.err.println("Interrupted");
		        }
		        
				iOutTime++;
			}

			LogonActiveServer tActive = new LogonActiveServer();
			tActive.iSize = tActive.size();
			System.out.print("Please input UserName: ");
			String strUser = scanIn.next();
			tActive.cUserName = strUser.getBytes();
			System.out.print("Please input Password: ");
			String strPwd = scanIn.next();
			tActive.cUserPwd = strPwd.getBytes();
			tActive.cProductID = strProductID.getBytes();
			tActive.write();
			m_iLogonID = NetClient.Logon(NVSSDK.SERVER_ACTIVE, tActive.getPointer(), tActive.size());
		} else {
			System.out.print("IP: " );
			String strIp = scanIn.next();
			System.out.print("UserName: " );
			String strUser = scanIn.next();
			System.out.print("Password: " );
			String strPswd = scanIn.next();
			System.out.print("Port: " );
			int iPort = scanIn.nextInt();
			LogonPara tNormal = new LogonPara();
			//Required fields
			tNormal.iSize = tNormal.size();				//Structure size
			tNormal.cNvsIP = strIp.getBytes();			//Device IP
			tNormal.cUserName = strUser.getBytes();		//user name
			tNormal.cUserPwd = strPswd.getBytes();		//password
			tNormal.iNvsPort = iPort;					//Device port
			tNormal.write();	
			m_iLogonID = NetClient.Logon(NVSSDK.SERVER_NORMAL, tNormal.getPointer(), tNormal.size());
		}
		
		int iTimeOut = 0;
		while(true) {
			int iLogonStatus = NetClient.GetLogonStatus(m_iLogonID);
			if(iLogonStatus == NVSSDK.LOGON_SUCCESS) {
				break;
			}
			
			iTimeOut = iTimeOut + 1;
			if(iTimeOut > 5) {
				System.out.println("LogonDevice time out!");
				NetClient.Logoff(m_iLogonID);
				m_iLogonID = -1;
				return -1;
			}
			
	        try {
	        	Thread.currentThread();
				Thread.sleep(1000); 
	        } catch(InterruptedException e) {
	            System.err.println("Interrupted");
	        }
		}
		
		System.out.println("[LogonDevice]" + ", logonId=" + m_iLogonID);	
		return 0;
	};
	
	//SDK initialization
	private int SDKInit() {
		//Get SDK version
		SDK_VERSION ver = new SDK_VERSION();
		int iRet = NetClient.GetVersion(ver);
		System.out.println("SDK Version is " + ver.m_ulMajorVersion + "."
				+ ver.m_ulMinorVersion + "." + ver.m_ulBuilder + " "
				+ ver.m_cVerInfo);		
		//Set main callback
		iRet = NetClient.SetNotifyFunction(cbkMain, null, cbkParaChange);
		System.out.println("SetNotifyFunction(" + iRet + ")");	
		//Start SDK
		iRet = NetClient.Startup();
		System.out.println("SDK Startup(" + iRet + ")");
		//Set the full login enable (this interface may not be called if the login message is inaccurate)
		iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_FULL_LOGON, 0, null, 0);
		
		return 0;
	};
	
	//Get face detection algorithm enable
	private int GetFaceDetectionEnable() {
		AnyScene tInfo = new AnyScene();
		tInfo.iBufSize = tInfo.size();
		tInfo.iSceneID = 0;	//Scene number 0-15
		tInfo.iDevType = 1;	//0-IPC, 1-NVR
		tInfo.write();
		
		int iChanNum = 0;	//Channel number, IPC = 0, NVR = actual channel number
		
		int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ANYSCENE, iChanNum, tInfo.getPointer(), tInfo.size());
		
		if(0 != iRet){
			System.out.println("Get face detection algorithm enabled failed:" + iRet);
		} else {
			
			tInfo.read();	
			
			String strEnable="Disenable";
			
			if(1 == (tInfo.iArithmetic>>2 & 1)){
				strEnable = "Enable";//Face detection algorithm on
			}

			System.out.println("The face detection algorithm enable:" + strEnable);
		}
		
		return iRet;
	}
	
	//Set intelligent analysis status
	public int SetVcaStatue(int _iStatus) {
		int iChanNo = 0;	//Channel number, 0 means the first channel, IPC only has 1 channel
		VcaStatue tInfo = new VcaStatue();
		tInfo.iStatus = _iStatus;
		tInfo.write();
		return NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_VCA_SUSPEND, iChanNo, tInfo.getPointer(), tInfo.size());
	}

	//Save picture
	public int SavePicture(String FileName, Pointer picData, int len) {
		if(null == picData || len <= 0) {
	        return -1;
	    }
		 
        FileOutputStream fop = null;
        File file;
       
        try {
        	file = new File(FileName);
        	fop = new FileOutputStream(file);
        	// if file doesnt exists, then create it
        	if (!file.exists()) {
        		file.createNewFile();
        	}
        	// get the content in bytes
        	byte[] contentInBytes = picData.getByteArray(0, len);
        	fop.write(contentInBytes);
        	fop.flush();
        	fop.close();
        } catch (IOException e) {
        	e.printStackTrace();
        } finally {
        	try {
        		if (fop != null) {
        			fop.close();
        		}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
		return 0;
	};
	//Picture stream callback
	NET_PICSTREAM_NOTIFY CallBack_PicStreamInfo = new NET_PICSTREAM_NOTIFY(){
		public int PicDataNotify(int _ulID, int _lCommand, Pointer _tInfo, int _iLen, Pointer  _lpUserData){
			if(_lCommand != NVSSDK.NET_PICSTREAM_CMD_FACE){
				System.out.println("PicDataNotify _lCommand != NVSSDK.NET_PICSTREAM_CMD_FACE") ;
				return -1;
			}
			
			FacePicStream tFacePicStream = new FacePicStream();
			tFacePicStream.write();
            Pointer pFaceBuffer = tFacePicStream.getPointer();
            byte[] RecvBuffer = _tInfo.getByteArray(0, _iLen);
            int iCopySize = Math.min(tFacePicStream.size(), _iLen);
            pFaceBuffer.write(0, RecvBuffer, 0, iCopySize);
            tFacePicStream.read();	
            System.out.println("PicDataNotify Snap Face count " + tFacePicStream.iFaceCount) ;
  
    		//Copy and capture panorama data	
    		PicData tFullData = new PicData();   //Get panorama data from pointer
            tFullData.write();
            Pointer pFullBuffer = tFullData.getPointer();
            Pointer pFullData = tFacePicStream.tFullData;
            byte[] bFullBuffer = pFullData.getByteArray(0, tFacePicStream.iSizeOfFull);
            int iFullPicSize = Math.min(tFullData.size(), tFacePicStream.iSizeOfFull);
            pFullBuffer.write(0, bFullBuffer, 0, iFullPicSize);
            tFullData.read();	
            
            //Copy and capture face data
    		FacePicData[] tPicData = (FacePicData[])new FacePicData().toArray(32);  
    		for(int i = 0; i < tFacePicStream.iFaceCount && i < 32; i++)
    		{
    			tPicData[i].write();
	            Pointer pPicBuffer = tPicData[i].getPointer();
	            Pointer pData = tFacePicStream.tPicData[i];
	            byte[] bBuffer = pData.getByteArray(0, tFacePicStream.iSizeOfFace);
	            int iPicSize = Math.min(tPicData[i].size(), tFacePicStream.iSizeOfFace);
	            pPicBuffer.write(0, bBuffer, 0, iPicSize);
	            tPicData[i].read();	
	            
	            //String strName = new String(tPicData[i].cName).trim();
	            //System.out.println("FaceName:" + strName);

	            //Analyze face attributes
	            FaceAttribute[] tArryAttr = (FaceAttribute[])new FaceAttribute().toArray(256);
	    		int iFaceAttrCount = Math.min(tPicData[i].iFaceAttrCount, 256);
	    		int iAttrSize = Math.min(tPicData[i].iFaceAttrStructSize, tArryAttr[0].size());
	    		for (int j = 0; j < iFaceAttrCount && j < 256; ++j)
	    		{
	    			tArryAttr[j].write();
	    			Pointer pAttrBuf = tArryAttr[j].getPointer();
	    			Pointer pTmp = tPicData[i].ptFaceAttr[j];
	    			byte[] bAttrData = pTmp.getByteArray(0, tPicData[i].iFaceAttrStructSize);
	    			pAttrBuf.write(0, bAttrData, 0, iAttrSize);
	    			tArryAttr[j].read();
	    			
	    			//System.out.println("Face attribute--type:" + tArryAttr[j].iType + ",value:" + tArryAttr[j].iValue);
	    			if(51 == tArryAttr[j].iType) {
	    				System.out.println("Temperature value:" + (float)((tArryAttr[j].iValue - 100000) / 100.0));
	    			}
	    			if(52 == tArryAttr[j].iType) {
	    				if (1 == tArryAttr[j].iValue) {
	    					System.out.println("Temperature Unit:" + "Centigrade");
	    				}
	    				else if (2 == tArryAttr[j].iValue) {
	    					System.out.println("Temperature Unit:" + "Fahrenheit");
	    				}
	    			}
	    			if(53 == tArryAttr[j].iType && 1 == tArryAttr[j].iValue) {
	    				System.out.println("High temperature alarm");
	    			}
	    		}
    		}
            
            //Capture time
        	int uiYear = tFullData.tPicTime.uiYear;
       		int uiMonth = tFullData.tPicTime.uiMonth;
    		int uiDay = tFullData.tPicTime.uiDay; 
    		int uiWeek = tFullData.tPicTime.uiWeek; 
    		int uiHour = tFullData.tPicTime.uiHour; 
    		int uiMinute = tFullData.tPicTime.uiMinute; 
    		int uiSecondsr = tFullData.tPicTime.uiSecondsr;
    		int uiMilliseconds = tFullData.tPicTime.uiMilliseconds;
    		
    		String sFileNameBase = new String();
    		sFileNameBase = m_strSavePath + "/";
    		sFileNameBase += "" + uiYear + uiMonth + uiDay + uiWeek + uiHour + uiMinute+ uiSecondsr + uiMilliseconds;
    		
    		//Save panorama
    		SavePicture(sFileNameBase + "full.jpg", tFullData.pcPicData, tFullData.iDataLen);
    		
    		//Save thumbnails and underlays
    		for(int i = 0; i < tFacePicStream.iFaceCount && i < 32; i++) {
    			//Small face image
    			SavePicture(sFileNameBase + "face" + i + ".jpg", tPicData[i].pcPicData, tPicData[i].iDataLen);
    			
    			//Face map
    			if(1 == tPicData[i].iAlramType) {	//Legal face
    				SavePicture(sFileNameBase + "neg" + i + ".jpg", tPicData[i].pcNegPicData, tPicData[i].iNegPicLen);
    			} else {
    				//No base map or illegal face
    			}
    		}
			return 0;
		}
	};
	//Open picture stream
	public int StartSnap() {
		NetPicPara tNetPicParam = new NetPicPara();
		tNetPicParam.iStructLen = tNetPicParam.size();
		tNetPicParam.iChannelNo = 0;
		tNetPicParam.cbkPicStreamNotify = CallBack_PicStreamInfo; //Snapshot callback function
		tNetPicParam.pvUser = null;
		
		IntByReference pConnectID = new IntByReference();
		int iRet = NetClient.StartRecvNetPicStream(m_iLogonID, tNetPicParam, tNetPicParam.size(), pConnectID);
		if (iRet < 0) {
			m_iConnectID = -1;
			 System.out.println("StartRecvNetPicStream Failed!");
		} else {
			m_iConnectID = pConnectID.getValue();
			System.out.println("StartRecvNetPicStream Success! ConnectID(" + m_iConnectID + ")");
		}
		
		return 0;
	};
	//sign out
	public int Exit() {
		//Stop picture flow
		NetClient.StopRecvNetPicStream(m_iConnectID);
		m_iConnectID = -1;
		
		//Log off
		NetClient.Logoff(m_iLogonID);
		m_iLogonID = -1;
		
		NetClient.Cleanup();
		return 0;
	}
	//Create a picture saving directory
	public boolean CtreatePicDir() {
		String destDirName = m_strSavePath;
		File dir = new File(destDirName);
		if (dir.exists()) {// Determine whether the directory exists
		//	System.out.println("The target directory already exists!");
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {// End with "/"
			destDirName = destDirName + File.separator;
		}
		if (dir.mkdirs()) {// Create target directory
		//	System.out.println("Create a directory successfully!" + destDirName);
			return true;
		} else {
		//	System.out.println("Failed to create directory!");
			return false;
		}
	}
	
	public void FaceDetectionEnable() {
        AnyScene tParam = new AnyScene();
        tParam.iBufSize = tParam.size();
        tParam.iSceneID = 0;	//Scene number0-15
        tParam.iDevType = 1;	//0-IPC, 1-NVR
        tParam.write();
        int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ANYSCENE, 0, tParam.getPointer(), tParam.size());
        if (0 > iRet)
        {
        	System.out.println("[NetClient_GetDevConfig] NET_CLIENT_ANYSCENE fail!");
        }
        else
        {
        	tParam.read();
            tParam.iArithmetic = 1 << 2;//Face detection algorithm on
            tParam.iDevType = 1;	//0-IPC, 1-NVR
            tParam.write();
            iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ANYSCENE, 0, tParam.getPointer(), tParam.size());
            if (iRet >= 0)
            {
            	System.out.println("Successful opening of face detection algorithm.\n");
            }
            else
            {
            	System.out.println("NetClient_SetDevConfig  NET_CLIENT_ANYSCENE failed.\n");
            }
        }
    }
    
	public void SetDetectParam()
    {
        FaceDetectArithmetic fda = new FaceDetectArithmetic();
        fda.iBufSize = fda.size();
        fda.iDevType = 0;	//0-IPC, 1-NVR
        fda.write();
        int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_FACE_DETECT_ARITHMETIC, 0, fda.getPointer(), fda.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_GetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
            return;
        }

        fda.read();
        if (fda.iMinSize >= fda.iMaxSize)
        {
            fda.iMaxSize = fda.iMinSize + 1;
        }

        fda.iPushMode = 2;
        fda.iSnapTimes = 1;
        fda.write();
        iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_FACE_DETECT_ARITHMETIC, 0, fda.getPointer(), fda.size());
        if (iRet >= 0)
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC success.\n");
        }
        else
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
        }
    }
	
	public void SetPicStreamUploadParam()
    {
        //Set background image quality and upload enable
        PicStreamUploadParam tInfo = new PicStreamUploadParam();
        tInfo.iSize = tInfo.size();
        tInfo.iSceneId = 0;
        tInfo.iPicType = 0;//0-Background map
        tInfo.write();
        int iRet = NetClient.VCAGetConfig(m_iLogonID, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, 0, tInfo.getPointer(), tInfo.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_GetDevConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
        }
        else
        {
        	tInfo.read();
            tInfo.iSnapEnable = 1;
            tInfo.iQpvalue = 80;
            tInfo.iIsOsd = 1;
            tInfo.write();
            iRet = NetClient.VCASetConfig(m_iLogonID, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, 0, tInfo.getPointer(), tInfo.size());
            if (iRet >= 0)
            {
            	System.out.println("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM success.\n");
            }
            else
            {
            	System.out.println("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
            }
        }

        //Set up close-up image quality and upload enable
        PicStreamUploadParam tParam = new PicStreamUploadParam();
        tParam.iSize = tParam.size();
        tParam.iSceneId = 0;
        tParam.iPicType = 1;//1-Close up
        tParam.write();
        iRet = NetClient.VCAGetConfig(m_iLogonID, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, 0, tParam.getPointer(), tParam.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_GetDevConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
        }
        else
        {
            tParam.read();
            tParam.iSnapEnable = 1;
            tParam.iQpvalue = 30;
            tParam.write();
            iRet = NetClient.VCASetConfig(m_iLogonID, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, 0, tParam.getPointer(), tParam.size());
            if (iRet < 0)
            {
            	System.out.println("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
            }
        }
    }
	
	//Enable / disable human body temperature measurement
	public void SetTemDetectEnable(int _iEnable)
    {
        int iRet = NetClient.SetCommonEnable(m_iLogonID, NVSSDK.CI_COMMON_ID_TEMDETECT, 0, _iEnable); //Channel 0 is the visible light channel, and the thermal imaging equipment sets this parameter according to channel 0
        if (iRet < 0)
        {
        	System.out.println("NetClient_SetCommonEnable  CI_COMMON_ID_TEMDETECT failed.\n");
        }
        else
        {
        	System.out.println("NetClient_SetCommonEnable  CI_COMMON_ID_TEMDETECT success.\n");
        }
    }

    //Set temperature scale type
    public void SetTemScaleType(int _iType)
    {
        if (1 != _iType && 2 != _iType)
        {
        	System.out.println("SetTemScaleType  inValid param!\n");
        }
        TemperatureScaleType tInfo = new TemperatureScaleType();
        tInfo.iChanNo = 0;
        tInfo.iSize = tInfo.size();
        tInfo.iTempStandard = _iType;
        tInfo.write();
        int iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_TEMPERATURE_STANDARD, 0, tInfo.getPointer(), tInfo.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_TEMPERATURE_STANDARD failed.\n");
        }
        else
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_TEMPERATURE_STANDARD success.\n");
        }
    }
    //Set blackbody correction parameters
    public void SetBlackbodyCorrection(int _iChannelNo)
    {
        BlackbodyCorrection tInfo = new BlackbodyCorrection();
        tInfo.write();
        int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_BLACKBODY_CORRECT, _iChannelNo, tInfo.getPointer(), tInfo.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_GetDevConfig  NET_CLIENT_BLACKBODY_CORRECT failed.\n");
        }
        else
        {
            tInfo.read();
            tInfo.iChanNo = 0;
            tInfo.iSize = tInfo.size();
            tInfo.iBlackBodyCorrectEnable = 1;       //Blackbody correction enable, 0-off, 1-on, default on
            tInfo.iBlackBodyCorrectType = 2;         //Blackbody correction type, 1-single correction, 2-continuous correction, default continuous correction
            	//temperature unit: 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin
			if(1 == tInfo.tParam[0].iBlackBodyTempUnit)
			{
				tInfo.tParam[0].iBlackBodyTemp = 3500;   //Temperature value * 100,  value 30-45  
			}
			else if(2 == tInfo.tParam[0].iBlackBodyTempUnit)
			{
				tInfo.tParam[0].iBlackBodyTemp = 9000;//Temperature value * 100 86 - 113
			}
            tInfo.tParam[0].iBlackBodyDistance = 100;//Distance in bold, CM
            tInfo.tParam[0].tRect.left = 2000;       //Left margin - the X coordinate of the upper left corner, which is the coordinate of ten thousandth ratio, 0-10000
            tInfo.tParam[0].tRect.top = 2000;        //Top margin - Y coordinate of the upper left corner
            tInfo.tParam[0].tRect.right = 8000;      //Right margin - bottom right X coordinate
            tInfo.tParam[0].tRect.bottom = 8000;     //Bottom margin - bottom right y coordinate
            tInfo.write();
            iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_BLACKBODY_CORRECT, _iChannelNo, tInfo.getPointer(), tInfo.size());
            if (iRet < 0)
            {
            	System.out.println("NetClient_SetDevConfig  NET_CLIENT_BLACKBODY_CORRECT failed.\n");
            }
            else
            {
            	System.out.println("NetClient_SetDevConfig  NET_CLIENT_BLACKBODY_CORRECT success.\n");
            }
        }
    }

    //Set temperature conversion parameters
    public void SetBodyTemCompensation(int _iChannelNo)
    {
        BodyTempCorrect tInfo = new BodyTempCorrect();
        tInfo.iChanNo = _iChannelNo;
        tInfo.iSize = tInfo.size();
        tInfo.iBodyTempCorrectEnable = 1;//Body temperature compensation enable, 0-not enabled, 1-enabled
        tInfo.iBodyTempCorrectSensitivity = 50;//Temperature compensation sensitivity, 0-100
        tInfo.write();
        int iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_BODYTEMP_CORRECT, _iChannelNo, tInfo.getPointer(), tInfo.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_BODYTEMP_CORRECT failed.\n");
        }
        else
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_BODYTEMP_CORRECT success.\n");
        }
    }

    //Set intelligent correction parameters
    public void SetIntelligentCorretct(int _iChannelNo)
    {
        IntelligentCorretct tInfo = new IntelligentCorretct();
        tInfo.iChanNo = _iChannelNo;
        tInfo.iSize = tInfo.size();
        tInfo.iIntelligentCorrectEnable = 1;//Intelligent correction enable, 0-not enabled, 1-enabled
        tInfo.iIntelligentCorrectSensitivity = 50;//Intelligent correction sensitivity, 0-100
        tInfo.write();
        int iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_INTELLIGENT_CORRECT, _iChannelNo, tInfo.getPointer(), tInfo.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_INTELLIGENT_CORRECT failed.\n");
        }
        else
        {
        	System.out.println("NetClient_SetDevConfig  NET_CLIENT_INTELLIGENT_CORRECT success.\n");
        }
    }
    //Set temperature abnormal alarm parameters
    public void SetVcaTemDetect()
    {
        VCATemDetect vc = new VCATemDetect();
        vc.iSize = vc.size();
        vc.iRuleID = 14;              //For temperature detection, when calling get interface, please bind 14
        vc.iSceneID = 0;              //Scene number, 0~32
		vc.iModelType = 1;
        vc.write();
        int iRet = NetClient.VCAGetConfig(m_iLogonID, NVSSDK.VCA_CMD_TEMDETECT, 0, vc.getPointer(), vc.size());
        if (iRet < 0)
        {
        	System.out.println("NetClient_VCAGetConfig  VCA_CMD_TEMDETECT failed.\n");
        }
        else
        {
            vc.read();
            vc.iValid = 1;                //Whether this event detection is valid, 0-invalid, 1-valid
            vc.iTemThreshold = 3600;      //Temperature threshold, the value is the actual temperature * 100
            vc.iTempLoseEnable = 1;       //Temperature abnormal alarm, 0-not enabled, 1-enabled
            vc.write();
            iRet = NetClient.VCASetConfig(m_iLogonID, NVSSDK.VCA_CMD_TEMDETECT, 0, vc.getPointer(), vc.size());
            System.out.println("NetClient_VCASetConfig  VCA_CMD_TEMDETECT success.\n");
        }
    }
    
	public static void main(String args[]) {
        TemDetectDemo cls = new TemDetectDemo();
        //Initialize SDK 
        cls.SDKInit();  
        //Login device
        cls.LogonDevice(); 
        if(cls.m_iLogonID < 0){
        	return;
        }
        
        cls.CtreatePicDir();//Create a snapshot directory
        
        while(true) {
        	System.out.println( "Please select: 0 exit, 1 enable face detection, 2 set face detection parameters, 3 enable body temperature measurement, 4 select temperature scale\n" + 
        						"5 blackbody correction, 6 body temperature conversion, 7 intelligent correction, 8 set temperature abnormal alarm parameters\n");
        	//System.out.print("Please select:");
        	int iOpt = cls.scanIn.nextInt();
        	if(0 == iOpt) {
        		System.out.println("The program is about to exit!");
        		break;	
        	} 
        	else if (1 == iOpt)	{//Turn on face detection
        		cls.FaceDetectionEnable();
	        }
	        else if (2 == iOpt)	{//Set face detection parameters
	        	cls.SetVcaStatue(NVSSDK.VCA_SUSPEND_STATUS_PAUSE);	//Pause intelligent analysis
	        	System.out.println("Enter any number\n");
	        	cls.scanIn.nextInt();
		        if (NVSSDK.VCA_SUSPEND_RESULT_SUCCESS != cls.m_iVcaStatus) {
		        	System.out.println("[main] Intelligent analysis pause failed!\n");
			        continue;
		        }
		        cls.SetDetectParam();//Parameters related to face detection (need to pause intelligent analysis)
		        cls.SetVcaStatue(NVSSDK.VCA_SUSPEND_STATUS_RESUME);//Intelligent analysis needs to be restored after setting

		        cls.SetPicStreamUploadParam();//Capture image quality, upload related parameters
	        }
	        else if (3 == iOpt)	{//Enable human body temperature measurement
	        	cls.SetTemDetectEnable(NVSSDK.TEM_DETECT_ENABLE);
	        }
	        else if (4 == iOpt)	{//Set temperature scale type
	        	System.out.println("1-celsius, 2-fahrenheit\n");
                int iType = cls.scanIn.nextInt();
                cls.SetTemScaleType(iType);
	        }
	        else if (5 == iOpt)	{//Blackbody correction
				System.out.println("Please Input Thermal imaging ChannelNo:");
				int iChannelNo = cls.scanIn.nextInt();
	        	cls.SetBlackbodyCorrection(iChannelNo);
	        }
	        else if (6 == iOpt)	{//Body temperature conversion
				System.out.println("Please Input Thermal imaging ChannelNo:");
				int iChannelNo = cls.scanIn.nextInt();
	        	cls.SetBodyTemCompensation(iChannelNo);
	        }
	        else if (7 == iOpt)	{//Intelligent correction
				System.out.println("Please Input Thermal imaging ChannelNo:");
				int iChannelNo = cls.scanIn.nextInt();
	        	cls.SetIntelligentCorretct(iChannelNo);
	        }
	        else if (8 == iOpt)	{//Set temperature abnormal alarm parameters
	        	cls.SetVcaTemDetect();
			} else {
        		System.out.println("Input operation instruction is illegal: " + iOpt) ;
        	}
        }
        
        //Program exit
        cls.Exit();
	}	
}

package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Scanner;  

import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.NorthAngle;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.PicData;
import src.NVSSDK.RECVDATA_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.NET_PICSTREAM_NOTIFY;
import src.NVSSDK.NetPicPara;
import src.NVSSDK.ENCODERINFO;
import src.NVSSDK.VcaPicStream;
import src.NVSSDK.STR_Para;
import src.NVSSDK.IrrigationPara;
import src.NVSSDK.vca_TAlarmInfo;


import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;


public class VcaSnap {
	
	
	private static final int CMD_NORTH_ANGLE = 4;
	int m_iLogonID = -1;
	int m_iConnectID = -1;
	
	MAIN_NOTIFY cbkMain = new MAIN_NOTIFY()
	{
		public void MainNotify(int iLogonID, int wParam, Pointer lParam,
				Pointer noitfyUserData) {

			int iMsgType = wParam & 0xFFFF;
			switch (iMsgType) {
			case NVSSDK.WCM_LOGON_NOTIFY:
			{
				try
				{
					int iLogonStatus = NetClient.GetLogonStatus(iLogonID);
					ENCODERINFO tDevInfo = new ENCODERINFO();
					NetClient.GetDevInfo(iLogonID, tDevInfo);
					
					String strIP = new String(tDevInfo.m_cEncoder).trim();			
					String strID = new String(tDevInfo.m_cFactoryID).trim();			
					LogonNotify(strIP, strID, iLogonID, iLogonStatus);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case NVSSDK.WCM_NORTH_ANGLE:
			{
				NorthAngle tNorthAngle = new NorthAngle();
				tNorthAngle.write();
				byte[] RecvBuffer = lParam.getByteArray(0, tNorthAngle.size());
				Pointer pParaBuf = tNorthAngle.getPointer();
				pParaBuf.write(0, RecvBuffer, 0, tNorthAngle.size());
				tNorthAngle.read();
				 System.out.println("tNorthAngle  iAngle"+tNorthAngle.iAngle+ 
		            		"tNorthAngle iStatus " + tNorthAngle.iStatus
		            		) ;
				 
			}
			default:
				break;
			}
		}
	};
	
	public void LogonNotify(String strIP, String strID, int iLogonID,
			int iLogonState) {
		String strMsg = new String();
		m_iLogonID = -1;
		switch (iLogonState) {
		case NVSSDK.LOGON_SUCCESS: {
			m_iLogonID = iLogonID;
			strMsg = "LOGON_SUCCESS";
			break;
		}
		case NVSSDK.LOGON_FAILED: {
			strMsg = "LOGON_FAILED";
			break;
		}
		case NVSSDK.LOGON_TIMEOUT: {
			strMsg = "LOGON_TIMEOUT";
			break;
		}
		case NVSSDK.LOGON_RETRY: {
			strMsg = "LOGON_RETRY";
			break;
		}
		case NVSSDK.LOGON_ING: {
			strMsg = "LOGON_ING";
			break;
		}
		default: {
			System.out.println("[WCM_LOGON_NOTIFY][" + iLogonState + "] IP("
					+ strIP + "),ID(" + strID + "),LogonID(" + iLogonID + ")");
		}
		}
		System.out.println("[WCM_LOGON_NOTIFY][" + strMsg + "] IP(" + strIP
				+ "),ID(" + strID + "),LogonID(" + iLogonID + ")");
	}
	
	ALARM_NOTIFY cbkAlarm = new ALARM_NOTIFY(){
		public void AlarmNotify(int _iLogonID, int _iChannel,
				int _iAlarmState, int _iAlarmType, Pointer _pUserData) {
			
			switch (_iAlarmType)
			{
			case NVSSDK.ALARM_VDO_MOTION:				//Video Motion Alarm
				{
					
					System.out.println("ALARM_VDO_MOTION _iChannel "+_iChannel);
					break;
				} 
			case NVSSDK.ALARM_VCA_INFO_EX:
				{
					int iVcaAlarmInfoIndex = _iAlarmState;
					vca_TAlarmInfo tVcaAlarmInfo = new vca_TAlarmInfo();
					tVcaAlarmInfo.write();

					//Obtain the current intelligent analysis alarm information according to the alarm index
					int iRet = NetClient.VCAGetAlarmInfo(_iLogonID, iVcaAlarmInfoIndex, tVcaAlarmInfo.getPointer(), tVcaAlarmInfo.size());
					tVcaAlarmInfo.read();
					if (iRet < 0) {
						break;
					}
					System.out.println("ALARM_VCA_INFO_EX _iChannel "+_iChannel+", EventType="+tVcaAlarmInfo.iEventType+", State="+tVcaAlarmInfo.iState);
					break;
				}
			default:
				break;
			}

		}
	};
	
	PARACHANGE_NOTIFY cbkParaChange = new PARACHANGE_NOTIFY(){
		public void ParaChangeNotify(int iLogonID, int iChannel, int paraType,
				Pointer para, Pointer noitfyUserData) {
			
			if (573 != paraType) {
				return;
			}
			
			if (null == para) {
				return;
			}
			
			STR_Para tPara = new STR_Para();
			tPara.write();
            int iSize = tPara.size();
            Pointer pParaBuf = tPara.getPointer();
            byte[] RecvBuffer = para.getByteArray(0, iSize);
            pParaBuf.write(0, RecvBuffer, 0, iSize);
            tPara.read();
            
			IrrigationPara tInfo = new IrrigationPara();
            tInfo.write();
            iSize = tInfo.size();
            Pointer pDataBuf = tInfo.getPointer();
            long iIn = tPara.m_iPara[0];
			Pointer pointerdata = new Pointer(iIn);
			byte[] RecvBuffer1 = pointerdata.getByteArray(0, iSize);
			pDataBuf.write(0, RecvBuffer1, 0, iSize);
			tInfo.read();
			System.out.println("IrrigationPara:iType=" + tInfo.iType + "iValue=" + tInfo.iValue);    
		}
	};
	
	RECVDATA_NOTIFY cbkRecvData = new RECVDATA_NOTIFY(){
		public void RecvDataNotify(int _ulID, Pointer data, int len, int _iFlag,
				Pointer _lpUserData) {
			System.out.println("[RECVDATA_NOTIFY] ConnID(" + _ulID + "),DataLen("
					+ len + ")");
		}
	};
	
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
	
	
	public int LogonDevice()
	{
		
		Scanner scanInput = new Scanner(System.in);
		System.out.println("Please enter the device IP:");
		String strIP = scanInput.next();
		System.out.println("Please enter the device port:");
		int iPort = scanInput.nextInt();
		System.out.println("Please enter user name:");
		String strUserName = scanInput.next();
		System.out.println("Please enter user password:");
		String strPasswd = scanInput.next();
		System.out.println("Logon" + strIP + ":" + 3000 + "-" +strUserName + "-" + strPasswd);
		while(true){
			m_iLogonID = NetClient.Logon("", strIP, strUserName, strPasswd, "",iPort);
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
	
	
	public boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {// Check if a directory exists
			System.out.println("Failed to create directory, target directory already exists!");
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {// Whether it ends with "/"
			destDirName = destDirName + File.separator;
		}
		if (dir.mkdirs()) {// Create target directory
			System.out.println("Create directory successfully!" + destDirName);
			return true;
		} else {
			System.out.println("Failed to create directory!");
			return false;
		}
	}
	
	public int SavePic(String FileName, Pointer pic, int len)
	{
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
         byte[] contentInBytes = pic.getByteArray(0, len);

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
	
	NET_PICSTREAM_NOTIFY cbkPicData = new NET_PICSTREAM_NOTIFY(){
		public int PicDataNotify(int _ulID, int _lCommand, Pointer _tInfo, int _iLen,
				Pointer _lpUserData){
			
			if(_lCommand == NVSSDK.NET_PICSTREAM_CMD_VCA){
				VcaPicStream tVcaPicStream = new VcaPicStream();
				tVcaPicStream.write();
	            Pointer pVcaBuffer = tVcaPicStream.getPointer();
	            byte[] RecvBuffer = _tInfo.getByteArray(0, _iLen);
	            int iCopySize = Math.min(tVcaPicStream.size(), _iLen);
	            pVcaBuffer.write(0, RecvBuffer, 0, iCopySize);
	            tVcaPicStream.read();	
	            String strIP = new String(tVcaPicStream.m_cRemoteIP).trim();
	            System.out.println("PicDataNotify Snap Pic "+strIP+ 
	            		" iChannelID " + tVcaPicStream.iChannelID+
	            		" count "+ tVcaPicStream.iPicCount  +
	            		" iEventType " + tVcaPicStream.iEventType 
	            		) ;
	            if( 0 < tVcaPicStream.iPtzInfoLen)
	            {
	            	System.out.println( "iNorthAngle" + tVcaPicStream.tPtzInfo.iNorthAngle);
	            }
	            if(tVcaPicStream.iPicCount <= 0)
	            {
	            	System.out.println("error count " + tVcaPicStream.iPicCount) ;
	            	return -1;
	            }
	            
	            PicData[] tPicData = (PicData[])new PicData().toArray(3);   
	           // PicData[] tPicData = new PicData[3];      
	    		for(int i = 0; i < tVcaPicStream.iPicCount && i < 3; i++){
	    			//tPicData[i] = new PicData();
	    			tPicData[i].write();
		            Pointer pPicBuffer = tPicData[i].getPointer();
		            Pointer pData = tVcaPicStream.tPicData[i];
		            byte[] bBuffer = pData.getByteArray(0, tVcaPicStream.iSize);
		            int iPicSize = Math.min(tPicData[i].size(), tVcaPicStream.iSize);
		            pPicBuffer.write(0, bBuffer, 0, iPicSize);
		            tPicData[i].read();	
	    		}
	           
	        	int uiYear = tPicData[0].tPicTime.uiYear;
	       		int uiMonth = tPicData[0].tPicTime.uiMonth;
	    		int uiDay = tPicData[0].tPicTime.uiDay; 
	    		int uiWeek = tPicData[0].tPicTime.uiWeek; 
	    		int uiHour = tPicData[0].tPicTime.uiHour; 
	    		int uiMinute = tPicData[0].tPicTime.uiMinute; 
	    		int uiSecondsr = tPicData[0].tPicTime.uiSecondsr;
	    		int uiMilliseconds = tPicData[0].tPicTime.uiMilliseconds;
	    		
	    		String strFileName = new String();
	    		strFileName +="VCA" +  uiYear + "-"+ uiMonth + "-"+ uiDay + "-"+ uiWeek + "-"+ uiHour + "-"+ uiMinute+ "-"+ uiSecondsr + "-"+ uiMilliseconds;
	    		for(int i = 0; i < tVcaPicStream.iPicCount && i < 3; i++){
	    			SavePic("PIC/" + strFileName + "pic" + i + ".jpg", tPicData[i].pcPicData, tPicData[i].iDataLen);
	    		}
				
			}else{
	            System.out.println("PicDataNotify other Snap type  " + _lCommand) ;
			}
			
			return 0;
		}
	};
	
	public int StartSnap()
	{
		NetPicPara tNetPicParam = new NetPicPara();
		tNetPicParam.iStructLen = tNetPicParam.size();
		tNetPicParam.iChannelNo = 0;
		tNetPicParam.cbkPicStreamNotify = cbkPicData; //Capture callback function
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
	public int CmdConfig()
	{
		NorthAngle tInfo = new NorthAngle();
		tInfo.iSize = tInfo.size();

		NorthAngle tInPara = new NorthAngle();
		tInPara.iSize = tInPara.size();
		int iChan = 0;
		int iRet = NetClient.CmdConfig(m_iLogonID, CMD_NORTH_ANGLE, iChan, tInPara, tInPara.size(), tInfo,  tInfo.size());
		if(0 > iRet)
		{
			 System.out.println("CmdConfig NorthAngle Failed!"+iRet);
		}
		else
		{
			
			int iAngle = tInfo.iAngle;
			int iStatus = tInfo.iStatus;
			System.out.println("CmdConfig NorthAngle Success!"+"iAngle"+iAngle+"iStatus"+iStatus);
		}
		return 0;
	}
	public static void main(String args[])
	{
		VcaSnap tVcaSnap = new VcaSnap();
		tVcaSnap.createDir("PIC");//Create snapshot directory
		tVcaSnap.SDKInit();  //Initialize SDK
		tVcaSnap.LogonDevice();//Log in to the device
		tVcaSnap.StartSnap();//Connect image stream channel
		tVcaSnap.CmdConfig();
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

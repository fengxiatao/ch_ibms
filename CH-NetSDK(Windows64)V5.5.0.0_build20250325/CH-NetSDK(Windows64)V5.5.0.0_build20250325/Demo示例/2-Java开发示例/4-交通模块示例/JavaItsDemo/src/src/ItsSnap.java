package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.RECVDATA_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.NET_PICSTREAM_NOTIFY;
import src.NVSSDK.NetPicPara;
import src.NVSSDK.ENCODERINFO;
import src.NVSSDK.ItsPicStream;
import src.NVSSDK.PicData;
import java.util.Scanner;


import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;


public class ItsSnap {
	
	int m_iLogonID = -1;
	int m_iConnectID = -1;
	
	String[] strCarPlateColor = new String[NVSSDK.MAX_CAR_PLATE_COLOR];
	String[] strCarColor = new String[NVSSDK.MAX_CAR_COLOR];
	
	int iCount = 0;
	
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

		}
	};
	
	PARACHANGE_NOTIFY cbkParaChange = new PARACHANGE_NOTIFY(){
		public void ParaChangeNotify(int iLogonID, int iChannel, int paraType,
				Pointer para, Pointer noitfyUserData) {
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
		Scanner scanIn = new Scanner(System.in);
		System.out.print("IP:" );
		String strIP = scanIn.next();
		System.out.print("UserName:" );
		String strUserName = scanIn.next();
		System.out.print("Password:" );
		String strPasswd = scanIn.next();
		System.out.print("Port:" );
		int iPort = 3000;
		
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
		if (dir.mkdirs()) {//Create target directory
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
					
			if(_lCommand != NVSSDK.NET_PICSTREAM_CMD_ITS){
				System.out.println("PicDataNotify other Snap type  " + _lCommand) ;
				return 0;
			}
			
			ItsPicStream tItsPicStream = new ItsPicStream();
			tItsPicStream.write();
			Pointer pItsBuffer = tItsPicStream.getPointer();
			byte[] RecvBuffer = _tInfo.getByteArray(0, _iLen);
			int iCopySize = Math.min(tItsPicStream.size(), _iLen);
			pItsBuffer.write(0, RecvBuffer, 0, iCopySize);
			tItsPicStream.read();	
			String strIP = new String(tItsPicStream.cCameraIP).trim();
			String strPlate = new String(tItsPicStream.cPlate).trim();
			
			String strOutput = "PicDataNotify Snap Pic "+strIP+ 
					" iChannelID: " + tItsPicStream.iChannelID+
					" count:"+ tItsPicStream.iPicCount  +
					" cPlate:" + strPlate;
			if(tItsPicStream.iCarColor >= 0 && tItsPicStream.iCarColor < NVSSDK.MAX_CAR_COLOR) {
				strOutput += " Car Color:" + strCarColor[tItsPicStream.iCarColor]; 
			} else {
				strOutput += " Car Color:" + "unknown "; 
			}
			
			if(tItsPicStream.iPlateColor >= 0 && tItsPicStream.iPlateColor < NVSSDK.MAX_CAR_PLATE_COLOR) {
				strOutput += " Plate Color:" + strCarPlateColor[tItsPicStream.iPlateColor]; 
			} else {
				strOutput += " Plate Color:" + "unknown "; 
			}
			
			System.out.println(strOutput) ;
			
			if(tItsPicStream.iPicCount <= 0){
				System.out.println("error count " + tItsPicStream.iPicCount) ;
				return -1;
			}
			
			PicData[] tPicData = (PicData[])new PicData().toArray(8);        
			for(int i = 0; i < tItsPicStream.iPicCount && i < 8; i++){
				tPicData[i].write();
				Pointer pPicBuffer = tPicData[i].getPointer();
				Pointer pData = tItsPicStream.tPicData[i];
				byte[] bBuffer = pData.getByteArray(0, tItsPicStream.iSize);
				int iPicSize = Math.min(tPicData[i].size(), tItsPicStream.iSize);
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
			strFileName +="ITS" +  uiYear + "-"+ uiMonth + "-"+ uiDay + "-"+ uiWeek + "-"+ uiHour + "-"+ uiMinute+ "-"+ uiSecondsr + "-"+ uiMilliseconds;
			for(int i = 0; i < tItsPicStream.iPicCount && i < 8; i++){
				if(iCount > 2000)
				{
					System.out.println("save picture over 20000!") ;
				}
				SavePic("PIC/" + strFileName + "pic" + iCount + ".jpg", tPicData[i].pcPicData, tPicData[i].iDataLen);
				iCount++;
			}	
			
			PicData tPlatData = (PicData)new PicData();  
			if(tItsPicStream.iPlatCount > 0) {
				tPlatData.write();
				Pointer pPicBuffer = tPlatData.getPointer();
				Pointer pData = tItsPicStream.tPlatData;
				byte[] bBuffer = pData.getByteArray(0, tItsPicStream.iSize);
				int iPicSize = Math.min(tPlatData.size(), tItsPicStream.iSize);
				pPicBuffer.write(0, bBuffer, 0, iPicSize);
				tPlatData.read();
				
				SavePic("PIC/" + strFileName + "plat" + "CP" + ".jpg", tPlatData.pcPicData, tPlatData.iDataLen);
			}
			
			PicData[] tFaceData = (PicData[])new PicData().toArray(3); 
			for(int i = 0; i < tItsPicStream.iFaceCount && i < 8; i++){
				tFaceData[i].write();
				Pointer pPicBuffer = tFaceData[i].getPointer();
				Pointer pData = tItsPicStream.tPicData[i];
				byte[] bBuffer = pData.getByteArray(0, tItsPicStream.iSize);
				int iPicSize = Math.min(tFaceData[i].size(), tItsPicStream.iSize);
				pPicBuffer.write(0, bBuffer, 0, iPicSize);
				tFaceData[i].read();	
				
				SavePic("PIC/" + strFileName + "plat" + "RL" + + i + ".jpg", tFaceData[i].pcPicData, tFaceData[i].iDataLen);
			}
			
			String strlog = "PicDataNotify Snap Pic" + "Pic" + tItsPicStream.iPicCount + "Plat" + tItsPicStream.iPlatCount + " Face" + tItsPicStream.iFaceCount;
			System.out.println(strlog) ;
					
			String strBWPlateData = new String();
			if (tItsPicStream.pcBWPlateData != Pointer.NULL && tItsPicStream.iBWPlatePicLen > 0)
			{
				strBWPlateData = tItsPicStream.pcBWPlateData.getString(tItsPicStream.iBWPlatePicLen);
			}
			System.out.println("iPreset:" + tItsPicStream.iPreset + ",iArea:" + tItsPicStream.iArea + ",sOriginalImgWidth:" + tItsPicStream.sOriginalImgWidth 
					+ ",pcBWPlateData:" + strBWPlateData + ",iRealImgWidth:" + tItsPicStream.iRealImgWidth + ",iRealImgHeight:" + tItsPicStream.iRealImgHeight
				 + ",iTargetType:" + tItsPicStream.iTargetType + ",iNewTargetType:" + tItsPicStream.iNewTargetType);
			
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
	
	
	
	public int InitValueString()
	{
		strCarPlateColor[0] = "unknown";
		strCarPlateColor[1] = "white on blue";
		strCarPlateColor[2] = "black on yellow";
		strCarPlateColor[3] = "Black on white";
		strCarPlateColor[4] = "black and white";
		strCarPlateColor[5] = "white on green";
		
		strCarColor[0] =	"white";
		strCarColor[1] =	"red" ;
		strCarColor[2] =	"yellow" ;
		strCarColor[3] =	"yellow" ;
		strCarColor[4] =	"blue" ;
		strCarColor[5] =	"green" ;
		strCarColor[6] =	"green" ;
		strCarColor[7] =	"purple" ;
		strCarColor[8] =	"pink" ;
		strCarColor[9] =	"black" ;
		strCarColor[10] = 	"red" ;
		strCarColor[11] = 	"yellow" ;
		strCarColor[12] = 	"yellow" ;
		strCarColor[13] = 	"grey" ;
		strCarColor[14] = 	"yellow" ;
		strCarColor[15] = 	"blue" ;
		strCarColor[16] = 	"blue" ;
		strCarColor[17] = 	"green" ;
		strCarColor[18] = 	"green" ;
		strCarColor[19] = 	"white" ;
		strCarColor[20] = 	"green" ;
		strCarColor[21] = 	"cyan" ;
		strCarColor[22] = 	"yellow" ;
		strCarColor[23] = 	"red" ;
		strCarColor[24] = 	"blue" ;
		strCarColor[25] = 	"blue" ;
		strCarColor[26] = 	"grey" ;
		strCarColor[27] = 	"purple" ;
		strCarColor[28] = 	"purple" ;
		strCarColor[29] = 	"brown" ;
		strCarColor[30] = 	"brown" ;
		strCarColor[31] = 	"brown" ;
		return 0;
	};
	
	public static void main(String args[])
	{
		ItsSnap tItsSnap = new ItsSnap();
		tItsSnap.createDir("PIC");//Create snapshot directory
		tItsSnap.InitValueString();
		tItsSnap.SDKInit();  //Initialize SDK
		tItsSnap.LogonDevice();//Log in to the device
		tItsSnap.StartSnap();//Connect image stream channel
        
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

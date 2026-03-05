package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Scanner;  
import java.util.HashMap;   
import java.util.Map;  

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Memory;

import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.CLIENTINFO;
import src.NVSSDK.ElevatorMonitor;
import src.NVSSDK.FRAME_INFO;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.RECVDATA_NOTIFY;
import src.NVSSDK.NVSDATA_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.ENCODERINFO;
import src.NVSSDK.RAWFRAME_NOTIFY;
import src.NVSSDK.RAWFRAME_INFO;
import src.NVSSDK.LogonPara;
import src.NVSSDK.DECYUV_NOTIFY_V4;
import src.NVSSDK.COMFORMAT;
import src.NVSSDK.FuncAbilityLevel;
import src.NVSSDK.Locate3DPosition;
import src.NVSSDK.vca_TPoint;
import src.NVSSDK.*;

public class VideoCtrl {	
	static int m_iLogonID = -1;
	static int m_iConnectID = -1;
	Map<String, Integer> m_mapOptCmd = new HashMap<String, Integer>();
	
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
				System.out.println("MainNotify:WCM_QUERYFILE_FINISHED!");
				break;
			} case NVSSDK.WCM_DWONLOAD_FINISHED: {
				System.out.println("MainNotify:WCM_DWONLOAD_FINISHED!");
				break;	
			} case NVSSDK.WCM_DWONLOAD_FAULT: {
				System.out.println("MainNotify:WCM_DWONLOAD_FAULT!");
				NetClient.NetFileStopDownloadFile(m_iConnectID);
				break;
			} case NVSSDK.WCM_DOWNLOAD_INTERRUPT: {
				System.out.println("MainNotify:WCM_DOWNLOAD_INTERRUPT!");
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
		public void RecvDataNotify(int _ulID, Pointer data, int len, int _iFlag, Pointer _lpUserData) {
			System.out.println("[RECVDATA_NOTIFY] ConnID(" + _ulID + "), DataLen(" + len + ")");
		}
	};
	
	//Network raw stream callback, it is not recommended that the user directly process the data
	NVSDATA_NOTIFY cbkNvsData = new NVSDATA_NOTIFY() {
		public void NvsDataNotify(int _uiID, Pointer _pucData, int _iLen, Pointer _iUser) {
			//System.out.println("[NVSDATA_NOTIFY] ConnID(" + _uiID + "), DataLen(" + _iLen + ")");
		}
	};
	
	public int SaveYuvData(Pointer data, int len)
	{
		FileOutputStream fopYuv = null;
		File fileYuv;
		try
        {
    		fileYuv = new File("mySnapShot.yuv");
    		fopYuv = new FileOutputStream(fileYuv, true);

    		// if file doesnt exists, then create it
    		if (!fileYuv.exists())
    		{
    			fileYuv.createNewFile();
    		}
        	
        	// get the content in bytes
        	byte[] contentInBytes = data.getByteArray(0, len);	

    		//save decode yuv video data
    		fopYuv.write(contentInBytes);
    		fopYuv.flush();
    		fopYuv.close();
        } 
        catch (IOException e)
        {
        	e.printStackTrace();
        } 
        finally
        {
        	try {
            	if (null != fopYuv) {
            		fopYuv.close();
            		fopYuv = null;
            	}
        	}
        	catch (IOException e)
        	{
        		e.printStackTrace();
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
        		//save raw video data
        		fopRawAudio.write(contentInBytes);
        		fopRawAudio.flush();
        		fopRawAudio.close();
        	} else {
        		//save raw audio data
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
			//System.out.println("recvRawData: _uiID=" + _uiID + ", _iLen=" + _iLen + ", nType=" + _ptRawFrameInfo.nType);
            
           //user can save and process raw stream data
            if (null != _pcData && 0 != _iLen) {
            	SaveRawData(_ptRawFrameInfo.nType, _pcData, _iLen);
            }
		}
	};
	
	DECYUV_NOTIFY_V4 cbkDecYuv = new DECYUV_NOTIFY_V4() {
		public void decYuvNotify(int _uiID, Pointer _pcData, int _iLen, FRAME_INFO _pFrameInfo, Pointer _pvUser) {
           //print data information
			//System.out.println("recvDecYuvData: _uiID=" + _uiID + ", _iLen=" + _iLen + ", nType=" + _pFrameInfo.nType);
		}
	};
	
	private void sdkInit() {
		SDK_VERSION ver = new SDK_VERSION();
		int iRet = NetClient.GetVersion(ver);
		System.out.println("[SDK_VERSION]" + ver.m_cVerInfo);

		iRet = NetClient.SetNotifyFunction(cbkMain, cbkAlarm, cbkParaChange);
		System.out.println("SetNotifyFunction(" + iRet + ")");

		iRet = NetClient.Startup();
		System.out.println("Startup(" + iRet + ")");
	};
	
	private void sdkUnInit() {
		int iRet = NetClient.Cleanup();
		System.out.println("Cleanup(" + iRet + ")");
	};
	
	public void Logon()
	{
		Scanner scanInput = new Scanner(System.in);
		System.out.println("Please enter the device IP:");
		String strDevIP = scanInput.next();
		System.out.println("Please enter the device port:");
		int iDevPort = scanInput.nextInt();
		System.out.println("Please enter user name:");
		String strUserName = scanInput.next();
		System.out.println("Please enter your password:");
		String strUserPwd = scanInput.next();
		System.out.println("Logon" + strDevIP + ":" + iDevPort + "-" +strUserName + "-" + strUserPwd);
		LogonPara tLogonPara = new LogonPara();
		tLogonPara.iSize = tLogonPara.size();
		tLogonPara.cNvsIP = strDevIP.getBytes();
		tLogonPara.iNvsPort = iDevPort;
		tLogonPara.cUserName = strUserName.getBytes();
		tLogonPara.cUserPwd = strUserPwd.getBytes();
		tLogonPara.write();
		m_iLogonID = NetClient.Logon_V4(NVSSDK.SERVER_NORMAL, tLogonPara.getPointer(), tLogonPara.size());
		if (m_iLogonID < 0) {
			System.err.println("Logon fail!(" + m_iLogonID + ")");
		}
		
		//The SDK login device is in asynchronous working mode and needs to wait for the login to succeed in the main callback
	};
	
	public void Logoff()
	{
		//logout
		if (m_iLogonID >= 0) {
			int iRet = NetClient.Logoff(m_iLogonID);
			m_iLogonID = -1;
			System.out.println("Logoff(" + iRet + ")");
		}
	};
	
	public void StartRecv()
	{
		Scanner scanInput = new Scanner(System.in);
		System.out.println("Please enter the video channel number (starting at 0):");
		int iChannelNo = scanInput.nextInt();
		System.out.println("Please enter the stream type (0-main stream, 1-sub stream):");
		int iStreamNO = scanInput.nextInt();
		System.out.println("1-private tcp connect, 2-private udp connect, 3-private multicast connect, 6-rtsp stream via RTP-over-TCP, 7-rtsp stream via RTP-over-UDP");
		System.out.println("8-rtsp stream via RTP-over-Multicast, 9-rtsps stream via SRTP-over-UDP, 10-rtsps stream via SRTP-over-Multicast");
		System.out.println("Input net mode(default is 1-private tcp connect):");
		int iNetMode = scanInput.nextInt();
		CLIENTINFO clientInfo = new CLIENTINFO();
		clientInfo.m_iChannelNo = iChannelNo;
		clientInfo.m_iNetMode = iNetMode;
		clientInfo.m_iStreamNO = iStreamNO;
		clientInfo.m_iServerID = m_iLogonID;
		clientInfo.m_iBufferCount = 20;
		clientInfo.m_iDelayNum = 1;
		clientInfo.m_iTimeout = 2000;
		clientInfo.m_iTTL = 8;
		clientInfo.write();

		IntByReference piConnectID = new IntByReference();
		int iRet = NetClient.StartRecv_V4(piConnectID, clientInfo, cbkNvsData, null);
		if (iRet < 0) {
			m_iConnectID = -1;
			System.err.println("StartRecv failed!(" + iRet + ")");
		} else {
			m_iConnectID = piConnectID.getValue();
			//Turn on network original stream callback
			NetClient.StartCaptureData(m_iConnectID);
			//Set the raw stream callback to receive audio and video bare stream data in the callback
			NetClient.SetRawFrameCallBack(m_iConnectID, cbkRawFrame, null);
			//Set the decoded data callback
			NetClient.SetDecCallBack_V4(m_iConnectID, cbkDecYuv, null);
			System.out.println("StartRecv success!(" + iRet + "), m_iConnectID(" + m_iConnectID + ")");
		}
		NetClient.StartPlay(m_iConnectID,null,0);
		
	};
	
	public void StopRecv()
	{
		if(m_iConnectID >= 0) {
			NetClient.StopCaptureData(m_iConnectID);
			int iRet = NetClient.StopRecv(m_iConnectID);
			m_iConnectID = -1;
			System.out.println("StopRecv(" + iRet + ")");
		}
	};
	
	public void StartRecord()
	{
		Scanner scanInput = new Scanner(System.in);
		System.out.println("Please enter the recording type (0-sdv, 4-aac, 8-ps, 9-ts , 10-mp4):");
		int iRecFileType = scanInput.nextInt();
		String strFileName = new String("myRecordFile");
		if (NVSSDK.REC_FILE_TYPE_AVI == iRecFileType) {
			strFileName += ".avi";
		} else if (NVSSDK.REC_FILE_TYPE_RAWAAC == iRecFileType) {
			strFileName += ".aac";
		} else if (NVSSDK.REC_FILE_TYPE_PS == iRecFileType) {
			strFileName += ".ps";
		} else if (NVSSDK.REC_FILE_TYPE_TS == iRecFileType) {
			strFileName += ".ts";
		}else if (NVSSDK.REC_FILE_TYPE_ZFMP4 == iRecFileType) {
			strFileName += ".mp4";
		}else {
			strFileName += ".sdv";
		}
		
		int iRet = NetClient.StartCaptureFile(m_iConnectID, strFileName, iRecFileType);
		if (NVSSDK.RET_SUCCESS == iRet) {
			System.out.println("StartCaptureFile success!(" + iRet + ")");
		} else {
			System.err.println("StartCaptureFile failed!(" + iRet + ")");
		}
	};
	
	public void StopRecord()
	{	
		int iRet = NetClient.StopCaptureFile(m_iConnectID);
		if (NVSSDK.RET_SUCCESS == iRet) {
			System.out.println("StopCaptureFile success!(" + iRet + ")");
		} else {
			System.err.println("StopCaptureFile failed!(" + iRet + ")");
		}
	};
	
	public void PTZCtrl()
	{
		//Get the current serial port number
		int iWorkMode = 0;
		int iChannelNo = 0;
		int iComNo = 1;
		int iAddress = 0;
		IntByReference piComNo = new IntByReference();
		IntByReference piAddress = new IntByReference();
        //Define pointers and open up memory space
   	 	Pointer pBuf = new Memory(64);
		int iRet = NetClient.GetDeviceType(m_iLogonID, iChannelNo, piComNo, piAddress, pBuf);
		if (NVSSDK.RET_SUCCESS == iRet) {	
			//Get byte array
			byte[] byteArray = pBuf.getByteArray(0, 12);
			//Convert to characters
			String strProtocol = new String(byteArray);
			iComNo = piComNo.getValue();
			iAddress = piAddress.getValue();
			System.out.println("GetDeviceType success! iChannelNo=" + iChannelNo + ", iComNo=" + iComNo
					+ ", iAddress=" + iAddress + ", strProtocol=" + strProtocol);
		} else {
			System.err.println("GetDeviceType failed!! iRet=" + iRet);
			return;
		}
		
		//Get the current serial port number working mode: 1--protocol mode, 2--transparent channel.
		//The protocol mode is recommended, and the transparent channel mode available for protocol mode is not supported for some particularly old devices.
		//Since the transparent channel involves the user group code, the logic is complicated. Here is not an example.
		//If necessary, please refer to the MFC version of NetClientDemo source code.
		COMFORMAT tComFormat = new COMFORMAT();
		tComFormat.iSize = tComFormat.size();
		tComFormat.iComNo = iComNo;
		tComFormat.write();
		iRet = NetClient.GetComFormat_V2(m_iLogonID, tComFormat);
		if (NVSSDK.RET_SUCCESS == iRet) {
			iWorkMode = tComFormat.iWorkMode;
			System.out.println("GetComFormat_V2 success! iComNo=" + iComNo + ", iWorkMode=" + iWorkMode);
		} else {
			System.err.println("GetComFormat_V2 failed!");
			return;
		}
		
		//If the working mode is not the protocol mode, first set to the protocol mode.
		if (1 != iWorkMode)
		{
			iWorkMode = 1;
			tComFormat.write();
			iRet = NetClient.SetComFormat_V2(m_iLogonID, tComFormat);
			if (NVSSDK.RET_SUCCESS == iRet) {
				System.out.println("SetComFormat_V2 success! iComNo=" + iComNo + ", iWorkMode=" + iWorkMode);
			} else {
				System.err.println("SetComFormat_V2 failed!");
				return;
			}
		}
		
		//PTZ control, detailed control code can be found in the header file provided by the SDK "ActionControl.h"
		Scanner scanInput = new Scanner(System.in);
		System.out.println("Please enter the direction of the dome rotation (up-1, down-2, left-3, right-4, stop -9, auto start -23, auto end -24):");
		int iAction = scanInput.nextInt();
		int iSpeed = 0;
		if (NVSSDK.PROTOCOL_MOVE_UP == iAction || NVSSDK.PROTOCOL_MOVE_DOWN == iAction) {
			System.out.println("Please enter the speed of the dome (0-100):");
			iSpeed = scanInput.nextInt();
			iRet = NetClient.DeviceCtrlEx(m_iLogonID, iChannelNo, iAction, 0, iSpeed, 0);
		} else if (NVSSDK.PROTOCOL_MOVE_LEFT == iAction || NVSSDK.PROTOCOL_MOVE_RIGHT == iAction) {
			System.out.println("Please enter the speed of the dome (0-100):");
			iSpeed = scanInput.nextInt();
			iRet = NetClient.DeviceCtrlEx(m_iLogonID, iChannelNo, iAction, iSpeed, 0, 0);
		} else {
			iRet = NetClient.DeviceCtrlEx(m_iLogonID, iChannelNo, iAction, 0, 0, 0);
		}
		
		if (NVSSDK.RET_SUCCESS == iRet) {
			System.out.println("DeviceCtrlEx success! iAction=" + iAction + ", iSpeed=" + iSpeed);
		} else {
			System.err.println("DeviceCtrlEx failed!");
		}
	};
	
	void SnapShot()
	{
		//Local capture needs to be decoded to capture, window handle is passed null, only decoding does not display
		int iRet = NetClient.StartPlay(m_iConnectID, null, 0);
		if (NVSSDK.RET_SUCCESS == iRet) {
			System.out.println("StartPlay success! m_iConnectID=" + m_iConnectID);
		} else {
			System.err.println("StartPlay failed!");
			return;
		}
		
		Scanner scanInput = new Scanner(System.in);
		System.out.print("Please enter the capture type (0-YUV, 1-BMP, 2-JPG):");
		int iType = scanInput.nextInt();
		if (NVSSDK.CAPTURE_PICTURE_TYPE_BMP == iType) {
			int iSize = NetClient.CapturePicture(m_iConnectID, NVSSDK.CAPTURE_PICTURE_TYPE_BMP, "mySnapShot.bmp");
			if(iSize <= 0) {
				System.err.println("CapturePicture:CAPTURE_PICTURE_TYPE_BMP failed! iSize=" + iSize);
				return;
			} else {
				System.out.println("CapturePicture:CAPTURE_PICTURE_TYPE_BMP success! iSize=" + iSize);
			}
		} else if (NVSSDK.CAPTURE_PICTURE_TYPE_JPG == iType) {
			int iSize = NetClient.CapturePicture(m_iConnectID, NVSSDK.CAPTURE_PICTURE_TYPE_JPG, "mySnapShot.jpg");
			if(iSize <= 0) {
				System.err.println("CapturePicture:CAPTURE_PICTURE_TYPE_JPG failed! iSize=" + iSize);
				return;
			}  else {
				System.out.println("CapturePicture:CAPTURE_PICTURE_TYPE_JPG success! iSize=" + iSize);
			}
		} else {
			PointerByReference dataYuv = new PointerByReference();
			int iSize = NetClient.CapturePic(m_iConnectID, dataYuv);
			if(iSize <= 0) {
				System.err.println("CapturePicYuv failed! iSize=" + iSize);
				return;
			} else {
				System.out.println("CapturePicYuv success! iSize=" + iSize);
				//Save yuv capture data
				SaveYuvData(dataYuv.getValue(), iSize);
			}
		}
		
		//Stop decoding
		iRet = NetClient.StopPlay(m_iConnectID);
		System.out.println("StopPlay(" + iRet + ")");
	};
	
	void fun3DLocate()
	{
		int iRet = NVSSDK.RET_FAILED;
		int iChannelNo = 0; //Sample code write test 0 channel, nvr device multi-channel use
		Scanner scanInput = new Scanner(System.in);
		FuncAbilityLevel tFuncAbilityLevel = new FuncAbilityLevel();
		tFuncAbilityLevel.iSize = tFuncAbilityLevel.size();
		tFuncAbilityLevel.iMainFuncType = NVSSDK.MAIN_FUNC_TYPE_DOME_PARA;
		tFuncAbilityLevel.iSubFuncType = 0;
		tFuncAbilityLevel.write();
		IntByReference piReturnByte = new IntByReference();
		iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_GET_FUNC_ABILITY, 0
				, tFuncAbilityLevel.getPointer(), tFuncAbilityLevel.size(), piReturnByte);
		if (NVSSDK.RET_SUCCESS != iRet) {
			System.err.println("GetDevConfig:NET_CLIENT_GET_FUNC_ABILITY failed! iRet=" + iRet);
			return;
		}
		
		tFuncAbilityLevel.read();	//It's important here

		//Convert to characters
		byte[] strTmp = new byte[1];
		strTmp[0] = tFuncAbilityLevel.cParam[0];
		String strParam = new String(strTmp);
		int iFuncPara = Integer.parseInt(strParam);
		int iSupportNew3D = (iFuncPara & 2) >> 1;	//Bit1 new 3D positioning protocol: 0-not supported, 1-supported
		if (1 == iSupportNew3D) {
			//Support new 3D positioning, precise positioning, new device support
			Locate3DPosition t3dInfo = new Locate3DPosition(); 
			t3dInfo.iBufSize = t3dInfo.size();
			t3dInfo.iPointNum = 2;
			
			vca_TPoint tPoint0 = new vca_TPoint();
			vca_TPoint tPoint1 = new vca_TPoint();
			System.out.println("Please enter the abscissa of the top left corner (parts ratio):");
			tPoint0.iX = scanInput.nextInt();
			System.out.println("Please enter the ordinate of the upper left corner (parts ratio):");
			tPoint0.iY = scanInput.nextInt();
			System.out.println("Please enter the abscissa of the lower right corner (parts ratio):");
			tPoint1.iX = scanInput.nextInt();
			System.out.println("Please enter the ordinate of the lower right corner (parts ratio):");
			tPoint1.iY = scanInput.nextInt();
			t3dInfo.tPoint[0] = tPoint0;
			t3dInfo.tPoint[1] = tPoint1;
			t3dInfo.write();
			iRet = NetClient.SendCommand(m_iLogonID, NVSSDK.COMMAND_ID_3D_POSITION, iChannelNo, t3dInfo.getPointer(), t3dInfo.size());
			if(NVSSDK.RET_SUCCESS != iRet) {
				System.err.println("SendCommand:COMMAND_ID_3D_POSITION failed! iRet=" + iRet);
			} else {
				System.out.println("SendCommand:COMMAND_ID_3D_POSITION success! iRet=" + iRet);
			}
		} else {
			//Old 3D positioning
			int	iDevAddress = 0;
			int iComNo = 1;
			IntByReference piComNo = new IntByReference();
			IntByReference piAddress = new IntByReference();
	        //Define pointers and open up memory space
	   	 	Pointer pBuf = new Memory(64);
			iRet = NetClient.GetDeviceType(m_iLogonID, iChannelNo, piComNo, piAddress, pBuf);
			if (NVSSDK.RET_SUCCESS != iRet)
			{
				System.err.println("GetDeviceType failed! iRet=" + iRet);
				return;
			}
			
			iComNo = piComNo.getValue();
			iDevAddress = piAddress.getValue();
			
			--iDevAddress;
			if (iDevAddress < 0)
			{
				iDevAddress = 0;
			}

			int iFlip=0;
			IntByReference piFlip = new IntByReference();
			iRet = NetClient.GetSensorFlip(m_iLogonID, iChannelNo, piFlip);
			if (NVSSDK.RET_SUCCESS != iRet)
			{
				System.err.println("GetSensorFlip failed! iRet=" + iRet);
				return;
			}

			iFlip = piFlip.getValue();
			System.out.println("Please enter the abscissa of the top left corner (parts ratio):");
			int iLeft = scanInput.nextInt();
			System.out.println("Please enter the ordinate of the upper left corner (parts ratio):");
			int iRight = scanInput.nextInt();
			System.out.println("Please enter the abscissa of the lower right corner (parts ratio):");
			int iTop = scanInput.nextInt();
			System.out.println("Please enter the ordinate of the lower right corner (parts ratio):");
			int iBottom = scanInput.nextInt();
			
			int L = 0, R = 0, U = 0, D = 0, W = 0, T = 0;
			vca_TPoint tVideo = new vca_TPoint();
			tVideo.iX = (iLeft + iRight) / 2;
			tVideo.iY = (iTop + iBottom)/2;
			if (iLeft < iRight) {
				T = 1;
				W = 0;
			} else {
				T = 0;
				W = 1;
			}

			int	ox = tVideo.iX, oy = tVideo.iY;
			if (1 == iFlip) {
				ox = iRight - ox;
				oy = iBottom - oy;
			}
			
			if(ox < iRight / 2) {
				L=1;
				R=0;
			} else {
				L=0;
				R=1;
			}

			if(oy < iBottom / 2) {
				U=1;
				D=0;
			} else {
				U=0;
				D=1;
			}

			byte[] cDecBuf = new byte[9];
			cDecBuf[0] = (byte)0xf6;
			cDecBuf[1] = (byte)8;
			cDecBuf[2] = (byte)iDevAddress;
			cDecBuf[3] = (byte)0x52;
			cDecBuf[4] = (byte)(R + L * 2 + U * 4 + D * 8 + W * 16 + T * 32);
			cDecBuf[5] = (byte)(Math.abs(2 * ox - iRight) * 63 / iRight);
			cDecBuf[6] = (byte)(Math.abs(2 * oy - iBottom) * 63/ iBottom);
			cDecBuf[7] = (byte)((iRight - iLeft) * 0x3f / iRight);
			cDecBuf[8] = (byte)((cDecBuf[1] + cDecBuf[2] + cDecBuf[3] + cDecBuf[4] + cDecBuf[5] + cDecBuf[6] + cDecBuf[7]) & 0x7f);
			
			int iChannelType = 0;
			IntByReference piChannelType = new IntByReference();
			iRet = NetClient.GetChannelProperty(m_iLogonID, iChannelNo, NVSSDK.GENERAL_CMD_GET_CHANNEL_TYPE, piChannelType.getPointer(), 4);
			if (NVSSDK.RET_SUCCESS == iRet ) {
				iChannelType = piChannelType.getValue();
				if (NVSSDK.CHANNEL_TYPE_DIGITAL == iChannelType) {
					iRet = NetClient.DigitalChannelSend(m_iLogonID, iChannelNo, cDecBuf, cDecBuf.length);
					if(NVSSDK.RET_SUCCESS == iRet) {
						System.out.println("DigitalChannelSend success! cDecBuf=" + cDecBuf);
					} else {
						System.err.println("DigitalChannelSend failed! iRet=" + iRet);
					}
					//When the channel attribute is judged to be the nvr digital channel, the command is sent to the current logic, otherwise the following logic is taken.
					return;
				} 
			} 
			
			//Non-nvr digital channels take this logic
			iRet = NetClient.ComSend(m_iLogonID, cDecBuf, cDecBuf.length, iComNo);
			if(NVSSDK.RET_SUCCESS == iRet) {
				System.out.println("ComSend success! " + Integer.toHexString(cDecBuf[0]) + "," + Integer.toHexString(cDecBuf[1])
						 + "," + Integer.toHexString(cDecBuf[2]) + "," + Integer.toHexString(cDecBuf[3])
						 + "," + Integer.toHexString(cDecBuf[4]) + "," + Integer.toHexString(cDecBuf[5])
						 + "," + Integer.toHexString(cDecBuf[6]) + "," + Integer.toHexString(cDecBuf[7])
						 + "," + Integer.toHexString(cDecBuf[8]));
			} else {
				System.err.println("DigitalChannelSend failed! iRet=" + iRet);
			}
		}
	};
	
	void Get_Set_Monitor()
	{
		// TODO: Add your control notification handler code here
		ElevatorMonitor tParam = new ElevatorMonitor();
		tParam.write();
	    
		System.out.println("NetClient.GetDevConfig NET_CLIENT_ELEVATOR_MONITOR :");
		IntByReference pRet = new IntByReference();
		int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ELEVATOR_MONITOR, 0, tParam.getPointer(), tParam.size(),pRet);
		if (iRet < 0)
		{
			System.err.println("NetClient.GetDevConfig NET_CLIENT_ELEVATOR_MONITOR failed! Logon id = " + m_iLogonID);
		}
		else
		{
			tParam.read();
			System.out.println( "iStartShockThreshold = " + tParam.iStartShockThreshold);
			System.out.println( "iMoveSpeed = " + tParam.iMoveSpeed);
			System.out.println( "iBodyInductionMode = " + tParam.iBodyInductionMode);
			System.out.println( "iEbikeDetectEnable = " + tParam.iEbikeDetectEnable);
			System.out.println( "iSwaySensitivity = " + tParam.iSwaySensitivity);
			System.out.println( "iTopLimit = " + tParam.iTopLimit);
			System.out.println( "iBottomLimit = " + tParam.iBottomLimit);
			System.out.println( "iMainFloor = " + tParam.iMainFloor);
			System.out.println( "iLevelingMode = " + tParam.iLevelingMode);
			
			System.out.println( "iOpenDoorMode = " + tParam.iOpenDoorMode);
			System.out.println( "iMaintenanceMode = " + tParam.iMaintenanceMode);
			System.out.println( "iCrashStopMode = " + tParam.iCrashStopMode);
			System.out.println( "iPIRMode = " + tParam.iPIRMode);
		}
		
		tParam.iMainFloor = 9;
		tParam.write();
		System.out.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_MONITOR :");
		iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ELEVATOR_MONITOR, 0, tParam.getPointer(), tParam.size());
		if (iRet < 0)
		{
			System.err.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_MONITOR failed! Logon id = " + m_iLogonID);
		}
		else
		{
			System.out.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_MONITOR SUCCESS");
		}
		
	}
	void Get_Set_StoreyInfo()
	{
		// TODO: Add your control notification handler code here
		ElevatorStoreyInfo tParam = new ElevatorStoreyInfo();
		tParam.write();
	    
		IntByReference pRet = new IntByReference();
		int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ELEVATOR_STOREYINFO, 0, tParam.getPointer(), tParam.size(),pRet);
		if (iRet < 0)
		{
			System.err.println("NetClient.GetDevConfig NET_CLIENT_ELEVATOR_STOREYINFO failed! Logon id = " + m_iLogonID);
		}
		else
		{
			tParam.read();
			System.out.println( "iStartFloor = " + tParam.iStartFloor);
			System.out.println( "iEndFloor = " + tParam.iEndFloor);
			
			for (int i = tParam.iStartFloor; i <  tParam.iEndFloor; i++)
			{
				System.out.println( "Floor = " + i + "  Height = " + tParam.iFloorHeight[i - NVSSDK.MIN_LOOR_LEVING]);
			}
		}
		
		tParam.iStartFloor = 2;
		tParam.write();
		System.out.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_STOREYINFO :");
		iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ELEVATOR_STOREYINFO, 0, tParam.getPointer(), tParam.size());
		if (iRet < 0)
		{
			System.err.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_STOREYINFO failed! Logon id = " + m_iLogonID);
		}
		else
		{
			System.out.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_STOREYINFO SUCCESS");
		}
	}

	void Get_Set_Statistics()
	{
		// TODO: Add your control notification handler code here
		ElevatorStatistics tParam = new ElevatorStatistics();
		tParam.write();
	    
		IntByReference pRet = new IntByReference();
		int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ELEVATOR_STATISTICS, 0, tParam.getPointer(), tParam.size(),pRet);
		if (iRet < 0)
		{
			System.err.println("NetClient.GetDevConfig NET_CLIENT_ELEVATOR_STATISTICS failed! Logon id = " + m_iLogonID);
		}
		else
		{
			tParam.read();
			System.out.println( "iChannelNo = " + tParam.iChannelNo);
			System.out.println( "iBindBrakeCn = " + tParam.iBindBrakeCn);
			System.out.println( "iOpenDoorCn = " + tParam.iOpenDoorCn);
			System.out.println( "iOperationMileage = " + tParam.iOperationMileage);
		}
		
		tParam.iBindBrakeCn = 12;
		tParam.write();
		System.out.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_STATISTICS :");
		iRet = NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ELEVATOR_STATISTICS, 0, tParam.getPointer(), tParam.size());
		if (iRet < 0)
		{
			System.err.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_STATISTICS failed! Logon id = " + m_iLogonID);
		}
		else
		{
			System.out.println("NetClient.SetDevConfig NET_CLIENT_ELEVATOR_STATISTICS SUCCESS");
		}
	}
	
	void Get_State()
	{
		// TODO: Add your control notification handler code here
		ElevatorState tParam = new ElevatorState();
		tParam.write();
	    
		IntByReference pRet = new IntByReference();
		int iRet = NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_ELEVATOR_STATE, 0, tParam.getPointer(), tParam.size(),pRet);
		if (iRet < 0)
		{
			System.err.println("NetClient.GetDevConfig NET_CLIENT_ELEVATOR_STATE failed! Logon id = " + m_iLogonID);
		}
		else
		{
			System.out.println("NetClient.GetDevConfig NET_CLIENT_ELEVATOR_STATE :");
			tParam.read();
			System.out.println( "iChannelNo = " + tParam.iChannelNo);
			System.out.println( "iFloor = " + tParam.iFloor);
			System.out.println( "iDirection = " + tParam.iDirection);
			System.out.println( "iSpeed = " + tParam.iSpeed);
			System.out.println( "iTemperature = " + tParam.iTemperature);
			System.out.println( "iHumidity = " + tParam.iHumidity);
			System.out.println( "iBindBrake = " + tParam.iBindBrake);
			System.out.println( "iMaintenance = " + tParam.iMaintenance);
			System.out.println( "iLeveling = " + tParam.iLeveling);
			
			System.out.println( "iOpenDoor = " + tParam.iOpenDoor);
			System.out.println( "iCrashStop = " + tParam.iCrashStop);
			System.out.println( "iBodyInduction = " + tParam.iBodyInduction);
			System.out.println( "iMainFloor = " + tParam.iMainFloor);
		}
	}
	
	public static final int CMD_QUIT = -1;
	public static final int CMD_LOGON = 0;
	public static final int CMD_LOGOFF = 1;
	public static final int CMD_STARTRECV = 2;
	public static final int CMD_STOPRECV = 3;
	public static final int CMD_STARTRECORD = 4;
	public static final int CMD_STOPRECORD = 5;
	public static final int CMD_PTZCTRL = 6;
	public static final int CMD_SNAPSHOT = 7;
	public static final int CMD_3DLOCATE = 8;
	public static final int CMD_MONITOR = 9;
	public static final int CMD_STOREYINFO = 10;
	public static final int CMD_STATICS = 11;
	public static final int CMD_STATE = 12;
	public void makeOptCmd()
	{   
		m_mapOptCmd.put("Quit", CMD_QUIT);
		m_mapOptCmd.put("q", CMD_QUIT);
		
		m_mapOptCmd.put("Logon", CMD_LOGON);
		m_mapOptCmd.put("0", CMD_LOGON);
		
		m_mapOptCmd.put("Logoff", CMD_LOGOFF);
		m_mapOptCmd.put("1", CMD_LOGOFF);
		
		m_mapOptCmd.put("StartRecv", CMD_STARTRECV);
		m_mapOptCmd.put("2", CMD_STARTRECV);
		
		m_mapOptCmd.put("StopRecv", CMD_STOPRECV);
		m_mapOptCmd.put("3", CMD_STOPRECV);
		
		m_mapOptCmd.put("StartRecord", CMD_STARTRECORD);
		m_mapOptCmd.put("4", CMD_STARTRECORD);
		
		m_mapOptCmd.put("StopRecord", CMD_STOPRECORD);
		m_mapOptCmd.put("5", CMD_STOPRECORD);
		
		m_mapOptCmd.put("PTZCtrl", CMD_PTZCTRL);
		m_mapOptCmd.put("6", CMD_PTZCTRL);
		
		m_mapOptCmd.put("SnapShot", CMD_SNAPSHOT);
		m_mapOptCmd.put("7", CMD_SNAPSHOT);
		
		m_mapOptCmd.put("3DLocate", CMD_3DLOCATE);
		m_mapOptCmd.put("8", CMD_3DLOCATE);
		
		m_mapOptCmd.put("Get_Set_Monitor", CMD_MONITOR);
		m_mapOptCmd.put("9", CMD_MONITOR);
		
		m_mapOptCmd.put("Get_Set_StoreyInfo", CMD_STOREYINFO);
		m_mapOptCmd.put("10", CMD_STOREYINFO);
		
		m_mapOptCmd.put("Get_Set_Statistics", CMD_STATICS);
		m_mapOptCmd.put("11", CMD_STATICS);
		
		m_mapOptCmd.put("Get_State", CMD_STATE);
		m_mapOptCmd.put("12", CMD_STATE);
	}
	
	public void doMajorWork()
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
				+ "[0]Logon 		[1]Logoff			\n"
				+ "[2]StartRecv 		[3]StopRecv			\n"
				+ "[4]StartRecord		[5]StopRecord		\n"
				+ "[6]PTZCtrl		[7]SnapShot		\n"
				+ "[8]3DLocate		[9]Monitor		\n" 
				+ "[10]StoreyInfo		[11]Statistics		\n"
				+ "[12]State		    ");
			System.out.println("***********************************************************");

			System.out.println("Please input your correct command: ");
			strCmd = scanInput.next();
			iCmd = m_mapOptCmd.get(strCmd);
			switch(iCmd)  
			{
				case CMD_QUIT:   //q
				{
					System.out.println("Goodbye, my friend!");
					blQuit = true;
					break;
				}
				case CMD_LOGON:
				{
					Logon();
					break;
				}
				case CMD_LOGOFF:
				{
					Logoff();
					break;
				}
				case CMD_STARTRECV:
				{
					StartRecv();
					break;
				}
				case CMD_STOPRECV:
				{
					StopRecv();
					break;
				}
				case CMD_STARTRECORD:
				{
					StartRecord();
					break;
				}
				case CMD_STOPRECORD:
				{
					StopRecord();
					break;
				}
				case CMD_PTZCTRL:
				{
					PTZCtrl();
					break;
				}
				case CMD_SNAPSHOT:
				{
					SnapShot();
					break;
				}
				case CMD_MONITOR:
				{
					Get_Set_Monitor();
					break;
				}
				case CMD_STOREYINFO:
				{
					Get_Set_StoreyInfo();
					break;
				}
				case CMD_STATICS:
				{
					Get_Set_Statistics();
					break;
				}
				case CMD_STATE:
				{
					Get_State();
					break;
				}
				case CMD_3DLOCATE:
				{
					fun3DLocate();
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
	
	public static void main(String args[])
	{
		System.out.println("VideoCtrlDemo!");
		VideoCtrl tVideoCtrl = new VideoCtrl();
		
		//Initialize the SDK
		tVideoCtrl.sdkInit(); 
		
		//Call the SDK interface to implement business functions
		tVideoCtrl.makeOptCmd();
		tVideoCtrl.doMajorWork();
		
		//Deinitialize the SDK and clean up the SDK resources
		tVideoCtrl.sdkUnInit(); 
	}	
}

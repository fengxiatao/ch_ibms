package src;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import com.nvs.*;
import com.nvs.sdk.*;


public class Device {
	private int m_iLogonID = -1;
	
	private ArrayList<Channel> m_MainDataChans = new ArrayList(); //Video main stream
	private ArrayList<Channel> m_SubDataChans = new ArrayList(); //Video substream
	private ArrayList<Channel> m_ThreeDataChans = new ArrayList(); //Video three streams
	private ArrayList<ArrayList<Channel> > m_VideoChans = new ArrayList();

	private ArrayList<Channel> m_NetFileChans = new ArrayList(); //download file channel
	private ArrayList<Channel> m_PictureChans = new ArrayList(); //picture stream
	private ArrayList<Channel> m_PlaybackChans = new ArrayList(); //Playback
	
	private int m_iTotalCount = 0;
	private int m_iCurrentCount = 0;
	
	public void MainNotify(int iLogonID, int wParam, Pointer lParam, Pointer noitfyUserData) {
		int iMsgType = wParam & 0xFFFF;
		switch (iMsgType) {
		case NvssdkLibrary.WCM_LOGON_NOTIFY: { //Login callback notification
			break;
		} case NvssdkLibrary.WCM_QUERYFILE_FINISHED: {
			try {
				IntBuffer iTotalCount = IntBuffer.allocate(1);
				IntBuffer iCurrentCount = IntBuffer.allocate(1);
				int iRet = NvssdkLibrary.INSTANCE.NetClient_NetFileGetFileCount(m_iLogonID, iTotalCount, iCurrentCount);
				if(0 == iRet) {
					m_iTotalCount = iTotalCount.get();
					m_iCurrentCount = iCurrentCount.get();
					System.out.println("MainNotify:WCM_QUERYFILE_FINISHED! m_iTotalCount= "
							+ m_iTotalCount + ", m_iCurrentCount=" + m_iCurrentCount);	
				} else {
					System.out.println("NetFileGetFileCount fail!iRet= " + iRet) ;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			break;
		} case NvssdkLibrary.WCM_DWONLOAD_FINISHED: {	
			int _iConnectID = 0;
			if (null != lParam)
			{
				_iConnectID = (int) lParam.nativeValue(lParam);
			}
			System.out.println("MainNotify:(ID:" + _iConnectID + "WCM_DWONLOAD_FINISHED! Download successful!");
			RemoveFileDownloadChannel(GetFileDownloadChannel(_iConnectID));
			break;	
		} case NvssdkLibrary.WCM_DWONLOAD_FAULT: {
			int _iConnectID = 0;
			if (null != lParam)
			{
				_iConnectID = (int) lParam.nativeValue(lParam);
			}
			System.out.println("MainNotify:(ID:" + _iConnectID + "WCM_DWONLOAD_FAULT!");
			RemoveFileDownloadChannel(GetFileDownloadChannel(_iConnectID));
			break;
		} case NvssdkLibrary.WCM_DOWNLOAD_INTERRUPT: {
			int _iConnectID = 0;
			if (null != lParam)
			{
				_iConnectID = (int) lParam.nativeValue(lParam);
			}
			System.out.println("MainNotify:(ID:" + _iConnectID + "WCM_DOWNLOAD_INTERRUPT!");
			RemoveFileDownloadChannel(GetFileDownloadChannel(_iConnectID));
			break;
		} default: break; 
		}
	}

	/**
	 * @param mILogonID
	 */
	public Device() {
		super();
		m_VideoChans.add(m_MainDataChans);
		m_VideoChans.add(m_SubDataChans);
		m_VideoChans.add(m_ThreeDataChans);
	}
	
	public void Destroy()
	{
		for (Channel aChan : m_MainDataChans)
		{
			RemoveVideoChannel(aChan);
		}
		for (Channel aChan : m_SubDataChans)
		{
			RemoveVideoChannel(aChan);
		}
		for (Channel aChan : m_ThreeDataChans)
		{
			RemoveVideoChannel(aChan);
		}
		
		for (Channel aChan : m_NetFileChans)
		{
			RemoveFileDownloadChannel(aChan);
		}
		for (Channel aChan : m_PictureChans)
		{
			RemovePicStreamChannel(aChan);
		}
		for (Channel aChan : m_PlaybackChans)
		{
			RemovePlaybackChannel(aChan);
		}
		
	}
	
	protected void finalize()
	{
		Destroy();
	}
	
	public void SetLogonID(int _iLogonID)
	{
		m_iLogonID = _iLogonID;
	}
	
	public int GetChannelCount()
	{
		return m_MainDataChans.size() + m_SubDataChans.size() + m_ThreeDataChans.size() + m_NetFileChans.size() + m_PictureChans.size() + m_PlaybackChans.size();
	}
	
	public int GetLogonID()
	{
		return m_iLogonID;
	}
	
	public int GetLogonStatus()
	{
		return NvssdkLibrary.INSTANCE.NetClient_GetLogonStatus(GetLogonID());

	}
	
	public String GetIP()
	{
		ENCODERINFO tDevInfo = new ENCODERINFO();
		NvssdkLibrary.INSTANCE.NetClient_GetDevInfo(GetLogonID(), tDevInfo);
		return new String(tDevInfo.m_cEncoder).trim();			
	
	}
	
	public String GetID()
	{
		ENCODERINFO tDevInfo = new ENCODERINFO();
		NvssdkLibrary.INSTANCE.NetClient_GetDevInfo(GetLogonID(), tDevInfo);
		return new String(tDevInfo.m_cFactoryID).trim();
	}
	
	public int Logon(tagLogonPara _logonPara) {
		m_iLogonID = NvssdkLibrary.INSTANCE.NetClient_Logon_V4(NvssdkLibrary.SERVER_NORMAL, _logonPara.getPointer(), _logonPara.size());
		if (m_iLogonID < 0) {
			System.err.println(this.toString() + " Logon fail!(" + m_iLogonID + ")");
		}
		return m_iLogonID;
	}
	
	public int LogOff() {
		//logout
		if (m_iLogonID >= 0) {
			int iRet = NvssdkLibrary.INSTANCE.NetClient_Logoff(m_iLogonID);
			m_iLogonID = -1;
			System.out.println("Logoff(" + iRet + ")");
		}
		return NvssdkLibrary.RET_SUCCESS;
	}
	
	//If there is no public network information, _iDevOrWanPort is the same as _iLocalListenPort; _strDevOrPublicIP is the local IP
	static public int SyncLogon(int _iLogonType, String _strDevOrPublicIP, int _iDevOrWanPort ,String _strUserName, String _strPassword, String _strProductID, int _iLocalListenPort)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		int iLogonID = -1;
		if (NvssdkLibrary.SERVER_ACTIVE == _iLogonType)
		{
			//Active mode login logic: the client and the device are not in the same subnet, and need to use the public network to penetrate
			//Set the local listening internal port
			iRet = NvssdkLibrary.INSTANCE.NetClient_SetPort(_iLocalListenPort, 0);
			if(NvssdkLibrary.RET_SUCCESS != iRet) {
				System.out.println("NetClient_SetPort fail!");
				return -1;
			}

			//Set the local listening external port (router mapping port)
			tagActiveNetWanInfo tLocalWanInfo = new tagActiveNetWanInfo();
			tLocalWanInfo.iSize = tLocalWanInfo.size();
			tLocalWanInfo.iWanPort = _iDevOrWanPort;
			tLocalWanInfo.cWanIP = _strDevOrPublicIP.getBytes();
			tLocalWanInfo.write();
			iRet = NvssdkLibrary.INSTANCE.NetClient_SetDsmConfig(NvssdkLibrary.DSM_CMD_SET_NET_WAN_INFO, tLocalWanInfo.getPointer(), tLocalWanInfo.size());
			if(NvssdkLibrary.RET_SUCCESS != iRet )
			{
				System.out.println("NetClient_SetDsmConfig:DSM_CMD_SET_NET_WAN_INFO fail!\n");
				return -1;
			}
			
			tagDsmOnline tOnline  = new tagDsmOnline();
			tOnline.iSize = tOnline.size();
			tOnline.cProductID = _strProductID.getBytes();
			tOnline.write();
			//Get the registration online status
			NvssdkLibrary.INSTANCE.NetClient_GetDsmRegstierInfo(NvssdkLibrary.DSM_CMD_GET_ONLINE_STATE, tOnline.getPointer(), tOnline.size());
			tOnline.read();
			int iOutTime = 0;
			while (NvssdkLibrary.DSM_STATE_ONLINE != tOnline.iOnline)
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

		        NvssdkLibrary.INSTANCE.NetClient_GetDsmRegstierInfo(NvssdkLibrary.DSM_CMD_GET_ONLINE_STATE, tOnline.getPointer(), tOnline.size());
				tOnline.read();
				iOutTime++;
			}
			
			tagLogonActiveServer tActive = new tagLogonActiveServer();
			tActive.iSize = tActive.size();
			tActive.cUserName = _strUserName.getBytes();
			tActive.cUserPwd = _strPassword.getBytes();
			tActive.cProductID = _strProductID.getBytes();
			tActive.write();
			//Active mode synchronous blocking login device
			iLogonID = NvssdkLibrary.INSTANCE.NetClient_SyncLogon(_iLogonType, tActive.getPointer(), tActive.size());
		}
		else
		{
			String strCharSet = "UTF-8";
			
			tagLogonPara tNormal = new tagLogonPara();
			tNormal.iSize = tNormal.size();
			tNormal.cNvsIP = _strDevOrPublicIP.getBytes();
			tNormal.iNvsPort = _iDevOrWanPort;
			tNormal.cUserName = _strUserName.getBytes();
			tNormal.cUserPwd = _strPassword.getBytes();
			tNormal.cCharSet = strCharSet.getBytes();
			tNormal.write();
			//Regular mode synchronously blocks the login device
			iLogonID = NvssdkLibrary.INSTANCE.NetClient_SyncLogon(_iLogonType, tNormal.getPointer(), tNormal.iSize);
		}
		if (iLogonID >= 0)
		{
			System.out.println("NetClient_LogonSync success:" + "iLogonType=" + _iLogonType + ", m_iLogonID=" + iLogonID);
			return iLogonID;
		}
		else if (NvssdkLibrary.RET_SYNCLOGON_TIMEOUT == iLogonID)
		{
			System.out.println("NetClient_LogonSync Timeout!");
			return -1;
		}
		else if (NvssdkLibrary.RET_SYNCLOGON_USENAME_ERROR == iLogonID)
		{
			System.out.println("NetClient_LogonSync username error!");
			return -1;
		}
		else if (NvssdkLibrary.RET_SYNCLOGON_USRPWD_ERROR == iLogonID)
		{
			System.out.println("NetClient_LogonSync password error!");
			return -1;
		}
		else if (NvssdkLibrary.RET_SYNCLOGON_PWDERRTIMES_OVERRUN == iLogonID)
		{
			System.out.println("NetClient_LogonSync passwor times overrun!");
			return -1;
		}
		else if (NvssdkLibrary.RET_SYNCLOGON_NET_ERROR == iLogonID)
		{
			System.out.println("NetClient_LogonSync net error!");
			return -1;
		}
		else if (NvssdkLibrary.RET_SYNCLOGON_PORT_ERROR == iLogonID)
		{
			System.out.println("NetClient_LogonSync port error!");
			return -1;
		}
		else if (NvssdkLibrary.RET_SYNCLOGON_UNKNOW_ERROR == iLogonID)
		{
			System.out.println("NetClient_LogonSync unknow error!");
			return -1;
		}
		else
		{
			System.out.println("NetClient_LogonSync fail! iRet=" + iLogonID);
			return -1;
		}
	}
	
	public int SyncRealPlay(int _iChannelNo, int _iStreamNo)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aVideoChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if (null != aVideoChan)
		{
			System.err.println("Video Channel(ChannelNo:" + _iChannelNo + ",StreamNo:" + _iStreamNo + ") aleardy Exists!!!");
			return iRet;
		}
		aVideoChan = new Channel();
		aVideoChan.m_iLogonID = m_iLogonID;
		aVideoChan.m_iChannelNo = _iChannelNo;
		aVideoChan.m_iStreamNo = _iStreamNo;
		aVideoChan.m_device = this;
		iRet = aVideoChan.SyncRealPlay();
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{
			AddVideoChannel(aVideoChan);
		}
		return iRet;
	}
	
	public int StopRealPlay(int _iChannelNo, int _iStreamNo)
	{
		Channel aChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if(null != aChan) 
		{
			aChan.StopRealPlay();
			RemoveVideoChannel(aChan);
			return NvssdkLibrary.RET_SUCCESS;
		}
		return NvssdkLibrary.RET_FAILED;	
	}
	
	private void AddVideoChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		_aChan.m_device = this;
		switch (_aChan.m_iStreamNo)
		{
			case 0: //Main stream
			{
				m_MainDataChans.add(_aChan);
				break;
			}
			case 1: //Substream
			{
				m_SubDataChans.add(_aChan);
				break;
			}
			case 254://triple stream
			{
				m_ThreeDataChans.add(_aChan);
				break;
			}
			default:
				break;
		}
			
	}
	
	private void RemoveVideoChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		_aChan.StopRecvVideo();
		switch (_aChan.m_iStreamNo)
		{
			case 0: //Main stream
			{
				m_MainDataChans.remove(_aChan);
				break;
			}
			case 1: //Substream
			{
				m_SubDataChans.remove(_aChan);
				break;
			}
			case 254://triple stream
			{
				m_ThreeDataChans.remove(_aChan);
				break;
			}
			default:
				break;
		}
	}
	
	private Channel GetVideoChannel(int _iChannelNo, int _iStreamNo)
	{
		for (ArrayList<Channel> aList : m_VideoChans)
		{
			for (Channel aChan : aList)
			{
				if (_iChannelNo == aChan.m_iChannelNo && _iStreamNo == aChan.m_iStreamNo)
				{	
					return aChan;
				}
			}
		}
		return null;
	}
		
	//Enable to receive preview video data and call back network stream data
	//NVSDATA_NOTIFY _pNotify If no network stream callback is required, this parameter is empty
	public int StartRecvVideo(int _iChannelNo, int _iStreamNo)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aVideoChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if (null != aVideoChan)
		{
			System.err.println("Video Channel(ChannelNo:" + _iChannelNo + ",StreamNo:" + _iStreamNo + ") aleardy Exists!!!");
			return iRet;
		}
		
		aVideoChan = new Channel();
		aVideoChan.m_iLogonID = m_iLogonID;
		aVideoChan.m_iChannelNo = _iChannelNo;
		aVideoChan.m_iStreamNo = _iStreamNo;
		aVideoChan.m_device = this;
		iRet = aVideoChan.StartRecvVideo();
		
		if (iRet < 0) 
		{
			System.err.println("StartRecv failed!(" + iRet + ")");
		} 
		else 
		{
			AddVideoChannel(aVideoChan);
			aVideoChan.StartCaptureData();
			return NvssdkLibrary.RET_SUCCESS;				
		}		
		return NvssdkLibrary.RET_FAILED;
	}
	
	//Enable to receive preview video data and call back the original stream data
	public int StartRawFrameCallback(int _iChannelNo, int _iStreamNo)
	{
		//Business logic
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aVideoChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if (null != aVideoChan)
		{
			iRet = aVideoChan.StartRawFrameCallBack();
		}		
		return iRet;
	}
	
	public int StopRawFrameCallback(int _iChannelNo, int _iStreamNo)
	{
		//Business logic
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aVideoChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if (null != aVideoChan)
		{
			iRet = aVideoChan.StopRawFrameCallBack();
		}		
		return iRet;
	}
	
	// stop receiving video data
	public int StopRecvVideo(int _iChannelNo, int _iStreamNo)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if(null != aChan) 
		{
			iRet = aChan.StopRecvVideo();
			RemoveVideoChannel(aChan);
		}
		return iRet;
	}
	
	// start recording
	public int StartRecord(int _iChannelNo, int _iStreamNo, String _cFileNameNoSuffix, int _iRecFileType)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if(null != aChan) 
		{
			iRet = aChan.StartRecord(_cFileNameNoSuffix, _iRecFileType);
		}
		return iRet;
	}
	
	// stop recording
	public int StopRecord(int _iChannelNo, int _iStreamNo)
	{	
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		if (null != aChan)
		{
			iRet = aChan.StopRecord();
		}
		return iRet;
	};
	
	public int SnapShot(int _iChannelNo, int _iStreamNo, int _iType)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		
		if (null != aChan)
		{
			iRet = aChan.SnapShot(_iType);
		}
		return iRet;
	};
	
	public int StartPsCallback(int _iChannelNo, int _iStreamNo)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		
		if (null != aChan)
		{
			iRet = aChan.StartPsCallback();
		}
		
        return iRet;
	}
	
	public int StopPsCallback(int _iChannelNo, int _iStreamNo)
	{
		Channel aChan = GetVideoChannel(_iChannelNo, _iStreamNo);
		int iRet = NvssdkLibrary.RET_FAILED;
		
		if (null != aChan)
		{
			iRet = aChan.StopPsCallback();
		}
		
		return iRet;
	}
	
	public int PTZCtrl(int _iChannelNo, int _iAction, int _iSpeed, int _iPresetNum)
	{
	    int iRet = NvssdkLibrary.RET_FAILED;

	    tagTransparentChannelControl aPara = new tagTransparentChannelControl();
	    aPara.iControlCode = _iAction;
	    aPara.iSpeed = _iSpeed;
	    aPara.iPresetNo = _iPresetNum;
	    aPara.write();
	    iRet = NvssdkLibrary.INSTANCE.NetClient_SendCommand(m_iLogonID, NvssdkLibrary.COMMAND_ID_TRANSPARENTCHANNELCONTROL_V5, _iChannelNo, aPara.getPointer(), aPara.size());
	    if(NvssdkLibrary.RET_SUCCESS == iRet)
	    {
	    	System.err.println("DevicePTZCtrl is OK!");
	    }
	    else
	    {
	    	System.err.println("DevicePTZCtrl is failed!");
	    }
	    return iRet;
	}
	
	public int Fun3DLocate(int _iChannelNo, int _iLeft, int _iRight, int _iTop, int _iBottom)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		//Sample code write test 0 channel, nvr device multi-channel use
		FuncAbilityLevel tFuncAbilityLevel = new FuncAbilityLevel();
		tFuncAbilityLevel.iSize = tFuncAbilityLevel.size();
		tFuncAbilityLevel.iMainFuncType = NvssdkLibrary.MAIN_FUNC_TYPE_DOME_PARA;
		tFuncAbilityLevel.iSubFuncType = 0;
		tFuncAbilityLevel.write();
		IntBuffer piReturnByte = IntBuffer.allocate(1);
		iRet = NvssdkLibrary.INSTANCE.NetClient_GetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_GET_FUNC_ABILITY, 0
				, tFuncAbilityLevel.getPointer(), tFuncAbilityLevel.size(), piReturnByte);
		if (NvssdkLibrary.RET_SUCCESS != iRet) {
			System.err.println("GetDevConfig:NET_CLIENT_GET_FUNC_ABILITY failed! iRet=" + iRet);
			return iRet;
		}
		
		tFuncAbilityLevel.read(); //It's important here

		//Convert to characters
		byte[] strTmp = new byte[1];
		strTmp[0] = tFuncAbilityLevel.cParam[0];
		String strParam = new String(strTmp);
		int iFuncPara = Integer.parseInt(strParam);
		int iSupportNew3D = (iFuncPara & 2) >> 1;	//Bit1 new 3D positioning protocol: 0-not supported, 1-supported
		if (1 == iSupportNew3D) {
			//Support new 3D positioning, precise positioning, new device support
			tagLocate3DPosition t3dInfo = new tagLocate3DPosition(); 
			t3dInfo.iBufSize = t3dInfo.size();
			t3dInfo.iPointNum = 2;
			
			vca_TPoint tPoint0 = new vca_TPoint();
			vca_TPoint tPoint1 = new vca_TPoint();
			tPoint0.iX = _iLeft;
			tPoint0.iY = _iRight;

			tPoint1.iX = _iTop;
			tPoint1.iY = _iBottom;
			t3dInfo.tPoint[0] = tPoint0;
			t3dInfo.tPoint[1] = tPoint1;
			t3dInfo.write();
			iRet = NvssdkLibrary.INSTANCE.NetClient_SendCommand(m_iLogonID, NvssdkLibrary.COMMAND_ID_3D_POSITION, _iChannelNo, t3dInfo.getPointer(), t3dInfo.size());
			if(NvssdkLibrary.RET_SUCCESS != iRet) {
				System.err.println("SendCommand:COMMAND_ID_3D_POSITION failed! iRet=" + iRet);
			} else {
				System.out.println("SendCommand:COMMAND_ID_3D_POSITION success! iRet=" + iRet);
			}
		} else {
			//Old 3D positioning
			int	iDevAddress = 0;
			int iComNo = 1;
			IntBuffer piComNo = IntBuffer.allocate(1);
			IntBuffer piAddress = IntBuffer.allocate(1);
	        //Define pointers and open up memory space
	   	 	ByteBuffer pBuf = ByteBuffer.allocate(64);
			iRet = NvssdkLibrary.INSTANCE.NetClient_GetDeviceType(m_iLogonID, _iChannelNo, piComNo, piAddress, pBuf);
			if (NvssdkLibrary.RET_SUCCESS != iRet)
			{
				System.err.println("GetDeviceType failed! iRet=" + iRet);
				return iRet;
			}
			
			iComNo = piComNo.get();
			iDevAddress = piAddress.get();
			
			--iDevAddress;
			if (iDevAddress < 0)
			{
				iDevAddress = 0;
			}

			int iFlip=0;
			IntBuffer piFlip = IntBuffer.allocate(1);
			iRet = NvssdkLibrary.INSTANCE.NetClient_GetSensorFlip(m_iLogonID, _iChannelNo, piFlip);
			if (NvssdkLibrary.RET_SUCCESS != iRet)
			{
				System.err.println("GetSensorFlip failed! iRet=" + iRet);
				return iRet;
			}

			iFlip = piFlip.get();
			int iLeft = _iLeft;
			int iRight = _iRight;
			int iTop = _iTop;
			int iBottom = _iBottom;
			
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
			iRet = NvssdkLibrary.INSTANCE.NetClient_GetChannelProperty(m_iLogonID, _iChannelNo, NvssdkLibrary.GENERAL_CMD_GET_CHANNEL_TYPE, piChannelType.getPointer(), 4);
			if (NvssdkLibrary.RET_SUCCESS == iRet ) {
				iChannelType = piChannelType.getValue();
				if (NvssdkLibrary.CHANNEL_TYPE_DIGITAL == iChannelType) {
					iRet = NvssdkLibrary.INSTANCE.NetClient_DigitalChannelSend(m_iLogonID, _iChannelNo, ByteBuffer.wrap(cDecBuf), cDecBuf.length);
					if(NvssdkLibrary.RET_SUCCESS == iRet) {
						System.out.println("DigitalChannelSend success! cDecBuf=" + cDecBuf);
					} else {
						System.err.println("DigitalChannelSend failed! iRet=" + iRet);
					}
					//When the channel attribute is judged to be the nvr digital channel, the command is sent to the current logic, otherwise the following logic is taken.
					return iRet;
				} 
			} 
			
			//Non-nvr digital channels take this logic
			iRet = NvssdkLibrary.INSTANCE.NetClient_ComSend(m_iLogonID, ByteBuffer.wrap(cDecBuf), cDecBuf.length, iComNo);
			if(NvssdkLibrary.RET_SUCCESS == iRet) {
				System.out.println("ComSend success! " + Integer.toHexString(cDecBuf[0]) + "," + Integer.toHexString(cDecBuf[1])
						 + "," + Integer.toHexString(cDecBuf[2]) + "," + Integer.toHexString(cDecBuf[3])
						 + "," + Integer.toHexString(cDecBuf[4]) + "," + Integer.toHexString(cDecBuf[5])
						 + "," + Integer.toHexString(cDecBuf[6]) + "," + Integer.toHexString(cDecBuf[7])
						 + "," + Integer.toHexString(cDecBuf[8]));
			} else {
				System.err.println("DigitalChannelSend failed! iRet=" + iRet);
			}
		}
		return iRet;
	};
	
	//////////////File download channel
	public int DownloadByFileMode(int _iSaveFileType, String _strDownloadFileName, String _strLocalSaveFileName)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = new Channel();
		aChan.m_iLogonID = m_iLogonID;
		aChan.m_device = this;
		iRet = aChan.DownloadByFileMode(_iSaveFileType, _strDownloadFileName, _strLocalSaveFileName);
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{
			AddFileDownloadChannel(aChan);
		}
		System.out.println("NetFileDownload:DOWNLOAD_CMD_FILE! iRet=" + iRet);
		return iRet;
	};
	
	public int DownloadByTimespanMode(int _iChannelNo, int _iStreamNo, int _iSaveFileType, NVS_FILE_TIME tBegin, NVS_FILE_TIME tEnd, String _strLocalSaveFileName)
	{
		Channel aChan = GetFileDownloadChannel(_iChannelNo, _iStreamNo);
		int iRet = NvssdkLibrary.RET_FAILED;
		
		if (null != aChan)
		{
			System.err.println("DownloadByTimespan Channel(ChannelNo:" + _iChannelNo + ",StreamNo:" + _iStreamNo + ") aleardy Exists!!!");
			return iRet;
		}
				
		aChan = new Channel();
		aChan.m_iLogonID = m_iLogonID;
		aChan.m_iChannelNo = _iChannelNo;
		aChan.m_iStreamNo = _iStreamNo;
		aChan.m_device = this;
		iRet = aChan.DownloadByTimespanMode(_iSaveFileType, tBegin, tEnd, _strLocalSaveFileName);
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{
			AddFileDownloadChannel(aChan);
		}
		return iRet;
	}
	
	public int StopDownload(int _iConnectID)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetFileDownloadChannel(_iConnectID);
		if (null != aChan) {
			aChan.StopDownload();
			RemoveFileDownloadChannel(aChan);
		}
		return iRet;
	}
	
	synchronized private void AddFileDownloadChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		_aChan.m_device = this;
		m_NetFileChans.add(_aChan);
	}
	
	synchronized private void RemoveFileDownloadChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		_aChan.StopDownload();
		m_NetFileChans.remove(_aChan);
	}
	
	//If you download by file, you only need to pass connectID
	synchronized private Channel GetFileDownloadChannel(int _iConnectID)
	{
		for (Channel aChan : m_NetFileChans)
		{
			if (_iConnectID == aChan.GetConnectID())
			{	
				return aChan;
			}
		}
		return null;
	}
	
	//If you download by file, you only need to pass connectID
	synchronized private Channel GetFileDownloadChannel(int _iChannelNo, int _iStreamNo)
	{
		for (Channel aChan : m_NetFileChans)
		{
			if (_iChannelNo == aChan.m_iChannelNo && _iStreamNo == aChan.m_iStreamNo)
			{	
				return aChan;
			}
		}
		return null;
	}
	
	
	// playback
	public int SyncQueryFile(NETFILE_QUERY_V5 tQueryFileV5, NVS_FILE_DATA[] tResult)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		if (null == tQueryFileV5 || null == tResult)
			return iRet;
		
		NVS_FILE_DATA tSingleData  = new NVS_FILE_DATA();
			
		int iOutTotalLen = tQueryFileV5.iPageSize * tSingleData.size();
		int iSingleLen = tSingleData.size();
		
		iRet = NvssdkLibrary.INSTANCE.NetClient_SyncQuery(m_iLogonID, 0, NvssdkLibrary.CMD_NETFILE_QUERY_FILE, tQueryFileV5.getPointer(), tQueryFileV5.size(), tResult[0].getPointer(), iOutTotalLen, iSingleLen);
		if (iRet < 0)
		{
			System.out.println("Err: NetClient_Query_V5");
			return iRet;
		}

		tQueryFileV5.read();
		for (NVS_FILE_DATA item : tResult)
		{
			item.read();
		}

		return iRet;
	}
	
	synchronized private void AddPlaybackChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		_aChan.m_device = this;
		m_PlaybackChans.add(_aChan);
	}
	
	synchronized private void RemovePlaybackChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		_aChan.StopPlayback();
		m_PlaybackChans.remove(_aChan);
	}
	
	synchronized private Channel GetPlaybackChannel(int _iConnectID)
	{
		for (Channel aChan : m_PlaybackChans)
		{
			if (_iConnectID == aChan.GetConnectID())
			{	
				return aChan;
			}
		}
		return null;
	}
	
	public int StartPlayback(String _strFileName)
	{
		int iRet = NvssdkLibrary.RET_FAILED;

		Channel aChan = new Channel();
		aChan.m_iLogonID = m_iLogonID;
		aChan.m_device = this;
		iRet = aChan.StartPlayback(_strFileName);
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{
			AddPlaybackChannel(aChan);
		}
		System.out.println("StartPlayback! iRet=" + iRet);
		return iRet;
	}
	
	public int StartPlayback(int _iChannelNo, NVS_FILE_TIME tBegin, NVS_FILE_TIME tEnd)
	{
		int iRet = NvssdkLibrary.RET_FAILED;

		Channel aChan = new Channel();
		aChan.m_iChannelNo = _iChannelNo;
		aChan.m_iLogonID = m_iLogonID;
		aChan.m_device = this;
		iRet = aChan.StartPlayback(tBegin, tEnd);
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{
			AddPlaybackChannel(aChan);
		}
		System.out.println("StartPlayback! iRet=" + iRet);
		return iRet;
	}
	
	public int StopPlayback(int _iConnectID)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetPlaybackChannel(_iConnectID);
		if (null != aChan)
		{
			aChan.StopPlayback();
			RemovePlaybackChannel(aChan);
			System.out.println("StopPlayback(ID:" + aChan.GetConnectID() + "!");
		}
		return iRet;
	}
	
	synchronized private void AddPicStreamChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		m_PictureChans.add(_aChan);
	}
	
	synchronized private void RemovePicStreamChannel(Channel _aChan)
	{
		if (null == _aChan)
			return;
		_aChan.StopRecvPicStream();
		m_PictureChans.remove(_aChan);
	}
	
	synchronized private Channel GetPicStreamChannel(int _iChannelNo)
	{
		for (Channel aChan : m_PictureChans)
		{
			if (_iChannelNo == aChan.m_iChannelNo)
			{	
				return aChan;
			}
		}
		return null;
	}
	
	//Image stream,//If the _cbkFacePicData callback is empty, the default callback is used
	public int StartRecvSnapFacePicStream(int _iChannelNo)
	{
		Channel aChan = GetPicStreamChannel(_iChannelNo);
		
		if (null != aChan)
		{
			System.out.println("Picture Stream(ChannelNo:" + _iChannelNo + ") already exist!!");
			return NvssdkLibrary.RET_FAILED;
		}
		
		int iRet = NvssdkLibrary.RET_FAILED;
		aChan = new Channel();
		aChan.m_iChannelNo = _iChannelNo;
		aChan.m_iLogonID = m_iLogonID;
		aChan.m_device = this;

		iRet = aChan.StartRecvSnapFacePicStream();
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{		
			AddPicStreamChannel(aChan); 	
		}
		return iRet;
	}
	
	//traffic, //use default callback if _cbkITSPicData callback is empty
	public int StartRecvSnapITSPicStream(int _iChannelNo)
	{
		Channel aChan = GetPicStreamChannel(_iChannelNo);
		
		if (null != aChan)
		{
			System.out.println("Picture Stream(ChannelNo:" + _iChannelNo + ") already exist!!");
			return NvssdkLibrary.RET_FAILED;
		}
		
		int iRet = NvssdkLibrary.RET_FAILED;
		aChan = new Channel();
		aChan.m_iChannelNo = _iChannelNo;
		aChan.m_iLogonID = m_iLogonID;
		aChan.m_device = this;

		iRet = aChan.StartRecvSnapITSPicStream();
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{		
			AddPicStreamChannel(aChan); 	
		}
		return iRet;
	}
	
	//Use the default callback if the _cbkVcaPicData callback is empty
	public int StartRecvSnapVcaPicStream(int _iChannelNo)
	{
		Channel aChan = GetPicStreamChannel(_iChannelNo);
		
		if (null != aChan)
		{
			System.out.println("Picture Stream(ChannelNo:" + _iChannelNo + ") already exist!!");
			return NvssdkLibrary.RET_FAILED;
		}
		
		int iRet = NvssdkLibrary.RET_FAILED;
		aChan = new Channel();
		aChan.m_iChannelNo = _iChannelNo;
		aChan.m_iLogonID = m_iLogonID;
		aChan.m_device = this;

		iRet = aChan.StartRecvSnapVcaPicStream();
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{		
			AddPicStreamChannel(aChan); 	
		}
		return iRet;
	}
	
	public int StopRecvPicStream(int _iChannelNo)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		Channel aChan = GetPicStreamChannel(_iChannelNo);
		if (null != aChan)
		{
			iRet = aChan.StopRecvPicStream();
		}
		RemovePicStreamChannel(aChan);
		return iRet;
	}
	
	public void PrintVideoChannels()
	{
		//
		String strDescription = null;
		int idx = 0;
		System.out.println("connected video channels(format:[idx](channelID,streamID,connectID)):");
		for (Channel aChan : m_MainDataChans)
		{
			strDescription = String.format("[%d](%d,%d,%d)   ", idx, aChan.m_iChannelNo, aChan.m_iStreamNo, aChan.GetConnectID());
			System.out.print(strDescription);
			idx++;
		}
		System.out.println("");
		for (Channel aChan : m_SubDataChans)
		{
			strDescription = String.format("[%d](%d,%d,%d)   ", idx, aChan.m_iChannelNo, aChan.m_iStreamNo, aChan.GetConnectID());
			System.out.print(strDescription);
			idx++;
		}
		System.out.println("");
		for (Channel aChan : m_ThreeDataChans)
		{
			strDescription = String.format("[%d](%d,%d,%d)   ", idx, aChan.m_iChannelNo, aChan.m_iStreamNo, aChan.GetConnectID());
			System.out.print(strDescription);
			idx++;
		}
		System.out.println("");
	}
	
	public void PrintPlaybackChannels()
	{
		String strDescription = null;
		int idx = 0;
		//playback
		System.out.println("connected playback channels(format:[idx](channelID,streamID,connectID)):");
		for (Channel aChan : m_PlaybackChans)
		{
			strDescription = String.format("[%d](%d,%d,%d)   ", idx, aChan.m_iChannelNo, aChan.m_iStreamNo, aChan.GetConnectID());
			System.out.print(strDescription);
			idx++;
		}
		System.out.println("");
			
	}
	
	public void PrintDownloadChannels()
	{
		String strDescription = null;
		int idx = 0;
		//download file channel
		System.out.println("connected download channels(format:[idx](channelID,streamID,connectID)):");
		for (Channel aChan : m_NetFileChans)
		{
			strDescription = String.format("[%d](%d,%d,%d)   ", idx, aChan.m_iChannelNo, aChan.m_iStreamNo, aChan.GetConnectID());
			System.out.print(strDescription);
			idx++;
		}
		System.out.println("");
	}
	
	public void PrintPictureStreamChannels()
	{
		String strDescription = null;
		int idx = 0;
		//image stream
		System.out.println("connected picture stream channels(format:[idx](channelID,streamID,connectID)):");
		for (Channel aChan : m_PictureChans)
		{
			strDescription = String.format("[%d](%d,%d,%d)   ", idx, aChan.m_iChannelNo, aChan.m_iStreamNo, aChan.GetConnectID());
			System.out.print(strDescription);
			idx++;
		}
		System.out.println("");	
		
	}
	
	public Channel GetVideoChannel(int index)
	{
		Channel aChan = null;
		if (index < 0)
		{
			return aChan;
		}
		if (m_MainDataChans.size() > index)
		{
			return m_MainDataChans.get(index);
		}
		
		index -= m_MainDataChans.size();
		if (m_SubDataChans.size() > index)
		{
			return m_SubDataChans.get(index);
		}
		
		index -= m_SubDataChans.size();
		if (m_ThreeDataChans.size() > index)
		{
			aChan = m_ThreeDataChans.get(index);
		}
		
		return aChan;
	}
	
	
	
}

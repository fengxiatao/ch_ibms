package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.sun.jna.CallbackReferenceHack;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import com.nvs.*;
import com.nvs.sdk.*;
import com.nvs.sdk.NvssdkLibrary.NET_PICSTREAM_NOTIFY;
import com.nvs.sdk.PlayerParam.ByReference;
import com.nvs.sdk.PlayerParam.ByValue;

public class Channel {
	public int m_iLogonID = -1; //Device login ID
	public int m_iChannelNo = -1; //channel number
	public int m_iStreamNo = -1; //video channel main sub three stream
	private int m_iConnectID = -1; //receive id, indicating a business connection
	public Device m_device = null;
	
	public static final int MAX_CAR_COLOR = 32;
	public static final int MAX_CAR_PLATE_COLOR = 6;
	private static String[] suffix = {".sdv", "", "", ".ps", "", "", ".ts"}; //Download file type
	//Capture type
	public static final int CAPTURE_PICTURE_TYPE_YUV = 0;
	public static final int CAPTURE_PICTURE_TYPE_BMP = 1;
	public static final int CAPTURE_PICTURE_TYPE_JPG = 2;
	public static final int CAPTURE_PICTURE_TYPE_FEC_BMP = 3;
	public static final int CAPTURE_PICTURE_TYPE_FEC_JPG = 4; 
	
	static String[] strCarPlateColor = null;
	static String[] strCarColor = null;
	
	public int InitValueString()
	{
		if (null != strCarPlateColor)
		{
			return NvssdkLibrary.RET_SUCCESS;
		}
		strCarPlateColor = new String[MAX_CAR_PLATE_COLOR];
		strCarColor = new String[MAX_CAR_COLOR];
		
		strCarPlateColor[0] = "Unknown";
		strCarPlateColor[1] = "White text on blue background";
		strCarPlateColor[2] = "Black text on yellow background";
		strCarPlateColor[3] = "Black on white";
		strCarPlateColor[4] = "White text on black background";
		strCarPlateColor[5] = "White text on green background";
		
		strCarColor[0] = "white";
		strCarColor[1] = "Red" ;
		strCarColor[2] = "Yellow" ;
		strCarColor[3] = "Yellow" ;
		strCarColor[4] = "Blue" ;
		strCarColor[5] = "green" ;
		strCarColor[6] = "green" ;
		strCarColor[7] = "Purple" ;
		strCarColor[8] = "Pink" ;
		strCarColor[9] = "Black" ;
		strCarColor[10] = "Red" ;
		strCarColor[11] = "Yellow" ;
		strCarColor[12] = "Yellow" ;
		strCarColor[13] = "gray" ;
		strCarColor[14] = "Yellow" ;
		strCarColor[15] = "Blue" ;
		strCarColor[16] = "Blue" ;
		strCarColor[17] = "green" ;
		strCarColor[18] = "green" ;
		strCarColor[19] = "white" ;
		strCarColor[20] = "green" ;
		strCarColor[21] = "cyan" ;
		strCarColor[22] = "Yellow" ;
		strCarColor[23] = "Red" ;
		strCarColor[24] = "Blue" ;
		strCarColor[25] = "Blue" ;
		strCarColor[26] = "gray" ;
		strCarColor[27] = "Purple" ;
		strCarColor[28] = "Purple" ;
		strCarColor[29] = "Brown";
		strCarColor[30] = "Brown" ;
		strCarColor[31] = "Brown" ;
		return 0;
	};

	public Channel() {
		InitValueString();
	}
	
	protected void finalize() {
		
	}
	
	public int GetConnectID()
	{
		return m_iConnectID;
	}
	
	static NvssdkLibrary.FULLFRAME_NOTIFY_V4 cbkPrivateFullFrame = new NvssdkLibrary.FULLFRAME_NOTIFY_V4() {
		public void apply(int _iConnectID, int _iStreamType, Pointer _pcData, int _iLen, Pointer _pvHeader, Pointer _pvUserData) {
	        // print data information
			//System.out.println("recvPrivateData: _iConnectID=" + _iConnectID + ", _iStreamType=" + _iStreamType + ", _iLen=" + _iLen);
            
           //The upper layer can save and process bare stream data
            if (null != _pcData && 0 != _iLen) {
            	//TODO:
            }
		}
	};
	
	NvssdkLibrary.RECVDATA_NOTIFY_EX cbkRecvData = new NvssdkLibrary.RECVDATA_NOTIFY_EX() {
		public void apply(NativeLong _ulID, Pointer data, int len, int _iFlag, Pointer _lpUserData) {
			//System.out.println("[RECVDATA_NOTIFY] ConnID(" + _ulID.intValue() + "), DataLen(" + len + ")");
		}
	};
	
	//original stream callback
	//Network raw stream callback, it is not recommended that the user directly process the data
	NvssdkLibrary.NVSDATA_NOTIFY cbkNvsData = new NvssdkLibrary.NVSDATA_NOTIFY() {
		public void apply(int _uiID, Pointer _pucData, int _iLen, Pointer _iUser) {
			//System.out.println("[NVSDATA_NOTIFY] ConnID(" + _uiID + "), DataLen(" + _iLen + ")");
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
        		fileRawVideo = new File(String.format("myVideoRawData_%d_%d_%s.raw", m_iLogonID, m_iConnectID, m_device.GetIP()));
        		
        		fopRawVideo = new FileOutputStream(fileRawVideo, true);

        		// if file doesnt exists, then create it
        		if (!fileRawVideo.exists())
        		{
        			System.out.println("ouput path:" + fileRawVideo.getAbsolutePath());
        			fileRawVideo.createNewFile();
        		}
    		}
        	
        	if (null == fopRawAudio)
    		{
        		fileRawAudio = new File(String.format("myAudioRawData_%d_%d_%s.raw", m_iLogonID, m_iConnectID, m_device.GetIP()));
        		
        		fopRawAudio = new FileOutputStream(fileRawAudio, true);

        		// if file doesnt exists, then create it
        		if (!fileRawAudio.exists())
        		{
        			System.out.println("ouput path:" + fileRawAudio.getAbsolutePath());
        			fileRawAudio.createNewFile();
        		}
    		}
        	
        	// get the content in bytes
        	byte[] contentInBytes = data.getByteArray(0, len);	
        	if(NvssdkLibrary.AUDIO_FRAME == type) {
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
	
	// whole frame callback
	NvssdkLibrary.RAWFRAME_NOTIFY cbkRawFrame = new NvssdkLibrary.RAWFRAME_NOTIFY() {
		public void apply(int _uiID, Pointer _pcData, int _iLen, RAWFRAME_INFO _ptRawFrameInfo, Pointer _lpUserData) {
           //print data information
			//System.out.println("recvRawData: _uiID=" + _uiID + ", _iLen=" + _iLen + ", nType=" + _ptRawFrameInfo.nType);
            
           //user can save and process raw stream data
            if (null != _pcData && 0 != _iLen) {
            	SaveRawData(_ptRawFrameInfo.nType, _pcData, _iLen);
            }
		}
	};
	
	// PS data callback
	static short iJustBeginOnce = 0;
	NvssdkLibrary.RECVDATA_NOTIFY_EX cbkPsData = new NvssdkLibrary.RECVDATA_NOTIFY_EX() {
		public void apply(NativeLong _uiID, Pointer _pucData, int _iLen, int _iType, Pointer _iUser) {
			if (0 == iJustBeginOnce)
			{
				System.out.println("[PSDataNotify] ConnID(" + _uiID.intValue() + "), DataLen(" + _iLen + ")" + ",_iType(" + _iType + ")");
				iJustBeginOnce++;
			}
			
		}
	};
	
	NvssdkLibrary.DECYUV_NOTIFY_V4 cbkDecYuv = new NvssdkLibrary.DECYUV_NOTIFY_V4() {
		public void apply(int _uiID, Pointer _pcData, int _iLen, FRAME_INFO _pFrameInfo, Pointer _pvUser) {
           //print data information
			if (0 == iJustBeginOnce)
			{
				System.out.println("recvDecYuvData: _uiID=" + _uiID + ", _iLen=" + _iLen + ", nType=" + _pFrameInfo.nType);
				iJustBeginOnce++;
			}
			
		}
	};
	
	public int SyncRealPlay()
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		int iChanTotalCount = 0;
		int iDigitalChanCount = 0;
		int iVideoChan = 0;
		tagNetClientPara tVideoPara = new tagNetClientPara();

		//Get the total number of digital channels
		IntBuffer piDigitalChanCount = IntBuffer.allocate(1);
		iRet = NvssdkLibrary.INSTANCE.NetClient_GetDigitalChannelNum(m_iLogonID, piDigitalChanCount);
		if (NvssdkLibrary.RET_SUCCESS != iRet)
		{
			System.out.println("NetClient_GetDigitalChannelNum fail iRet=" + iRet);
			return NvssdkLibrary.RET_FAILED;
		}
		iDigitalChanCount = piDigitalChanCount.get();
		
		//Get the total number of channels
		IntBuffer piChanTotalCount = IntBuffer.allocate(1);
		iRet = NvssdkLibrary.INSTANCE.NetClient_GetChannelNum(m_iLogonID, piChanTotalCount);
		if (NvssdkLibrary.RET_SUCCESS != iRet)
		{
			System.out.println("NetClient_GetChannelNum fail iRet=" + iRet);
			return NvssdkLibrary.RET_FAILED;
		}
		iChanTotalCount = piChanTotalCount.get();
		
		//If the digital channel is not 0, it is an NVR device; if it is 0, it is an IPC device
		if (0 == iDigitalChanCount)
		{
			iDigitalChanCount = iChanTotalCount;
		}

		System.out.println("iChanTotalCount=" + iChanTotalCount + ", iDigitalChanCount=" + iDigitalChanCount);

		if (m_iChannelNo < 0 || m_iChannelNo > iDigitalChanCount - 1)
		{
			System.out.println("_iChannelNo is not valid(0~" + (iDigitalChanCount - 1) + ")");
			return NvssdkLibrary.RET_FAILED;
		}

		tVideoPara.iSize = tVideoPara.size();
		tVideoPara.tCltInfo.m_iServerID = m_iLogonID;	//logon handle
		tVideoPara.tCltInfo.m_iChannelNo = m_iChannelNo;
		tVideoPara.tCltInfo.m_iStreamNO = m_iStreamNo; //0--main stream, 1--sub stream
		tVideoPara.tCltInfo.m_iNetMode = 1;		
		tVideoPara.tCltInfo.m_iTimeout = 20;
		tVideoPara.pCbkFullFrm = cbkPrivateFullFrame;
		tVideoPara.pvCbkFullFrmUsrData = null;
		tVideoPara.pCbkRawFrm = cbkRawFrame; //If the parameter is not empty, it is the original stream callback
		tVideoPara.pvCbkRawFrmUsrData = null;
		tVideoPara.iIsForbidDecode = NvssdkLibrary.RAW_NOTIFY_ALLOW_DECODE;
		tVideoPara.pvWnd = null;	//console demo not show video

		tVideoPara.write();
		
		IntBuffer piConnectID = IntBuffer.allocate(1);
		iRet = NvssdkLibrary.INSTANCE.NetClient_SyncRealPlay(piConnectID, tVideoPara, tVideoPara.iSize);
		if (NvssdkLibrary.RET_SUCCESS == iRet)
		{ 
			m_iConnectID = piConnectID.get();
			System.out.println("NetClient_StartRecvSync Success! uiRecvID=" + m_iConnectID);
			return NvssdkLibrary.RET_SUCCESS;
		}
		else if (NvssdkLibrary.RET_SYNCREALPLAY_TIMEOUT == iRet)
		{
			System.out.println("NetClient_StartRecvSync Timeout!");
			return NvssdkLibrary.RET_FAILED;
		}

		System.out.println("NetClient_StartRecvSync fail!iRet=" + iRet);
		return NvssdkLibrary.RET_FAILED;
	}

	public int StopRealPlay()
	{
		if (m_iConnectID < 0)
		{
			return NvssdkLibrary.RET_FAILED;
		}
		int iRet =  NvssdkLibrary.INSTANCE.NetClient_StopRealPlay(m_iConnectID, 1);
		m_iConnectID = -1;
		System.out.println("StopRealPlay(" + iRet + ")");
		return NvssdkLibrary.RET_SUCCESS;		
	}
	
	// Enable to accept video data
	public int	StartRecvVideo()
	{
		//Business logic
		CLIENTINFO clientInfo = new CLIENTINFO();
		clientInfo.m_iChannelNo = m_iChannelNo;
		clientInfo.m_iNetMode = 1;
		clientInfo.m_iStreamNO = m_iStreamNo;
		clientInfo.m_iServerID = m_iLogonID;
		clientInfo.m_iBufferCount = 20;
		clientInfo.m_iDelayNum = 1;
		clientInfo.m_iTimeout = 2000;
		clientInfo.m_iTTL = 8;
		clientInfo.write();

		IntBuffer piConnectID = IntBuffer.allocate(1);
		// The network original stream callback, directly callback the data from the network
		int iRet = NvssdkLibrary.INSTANCE.NetClient_StartRecv_V4(piConnectID, clientInfo, cbkNvsData, null);
		m_iConnectID = piConnectID.get();
		return m_iConnectID;
	}

	public int StartCaptureData()
	{
		//Turn on network original stream callback
		int iRet = NvssdkLibrary.RET_FAILED;
		NativeLong aVar = new NativeLong(m_iConnectID);
		iRet = NvssdkLibrary.INSTANCE.NetClient_StartCaptureData(aVar);
		System.out.println("StartRecv success!(" + iRet + "), m_iConnectID(" + m_iConnectID + ")");
		return iRet;	
	}

	public int StartRawFrameCallBack()
	{
		//Set the raw stream callback to receive audio and video bare stream data in the callback
		int iRet = NvssdkLibrary.RET_FAILED;
		iRet = NvssdkLibrary.INSTANCE.NetClient_SetRawFrameCallBack(m_iConnectID, cbkRawFrame, null);
		String strFile = String.format("%s/myVideoRawData_%d_%d_%s.raw", System.getProperty("user.dir"), m_iLogonID, m_iConnectID, m_device.GetIP());
		System.out.println("SetRawFrameCallBack(connectID:" + m_iConnectID + "),iRet:" + iRet + ",path:" + strFile);

		return iRet;
	}
	
	public int StopRawFrameCallBack()
	{
		//Set the raw stream callback to receive audio and video bare stream data in the callback
		int iRet = NvssdkLibrary.RET_FAILED;
		iRet = NvssdkLibrary.INSTANCE.NetClient_SetRawFrameCallBack(m_iConnectID, null, null);

		System.out.println("StopRawFrameCallBack(connectID:" + m_iConnectID + "),iRet:" + iRet);

		return iRet;
	}
	
	public int StopRecvVideo()
	{
		if( m_iConnectID >= 0) 
		{
			NativeLong aVar = new NativeLong(m_iConnectID);
			NvssdkLibrary.INSTANCE.NetClient_StopCaptureData(aVar);
			int iRet = NvssdkLibrary.INSTANCE.NetClient_StopRecv(m_iConnectID);
			m_iConnectID = -1;
			System.out.println("StopRecv(" + iRet + ")");
			return NvssdkLibrary.RET_SUCCESS;
		}
		return NvssdkLibrary.RET_FAILED;
	}
	
	// start recording
	public int StartRecord(String _cFileNameNoSuffix, int _iRecFileType)
	{
		int iRet = NvssdkLibrary.RET_FAILED;	
		String strFileName = _cFileNameNoSuffix;
		if (NvssdkLibrary.REC_FILE_TYPE_AVI == _iRecFileType) {
			strFileName += ".avi\0";
		} else if (NvssdkLibrary.REC_FILE_TYPE_RAWAAC == _iRecFileType) {
			strFileName += ".aac\0";
		} else if (NvssdkLibrary.REC_FILE_TYPE_PS == _iRecFileType) {
			strFileName += ".ps\0";
		} else if (NvssdkLibrary.REC_FILE_TYPE_TS == _iRecFileType) {
			strFileName += ".ts\0";
		}else {
			strFileName += ".sdv\0";
		}
		
		ByteBuffer strBuffer = ByteBuffer.wrap(strFileName.getBytes());
		iRet = NvssdkLibrary.INSTANCE.NetClient_StartCaptureFile(m_iConnectID, strBuffer, _iRecFileType);
		if (NvssdkLibrary.RET_SUCCESS == iRet) {
			System.out.println("StartCaptureFile success!(" + iRet + ")");
		} else {
			System.err.println("StartCaptureFile failed!(" + iRet + ")");
		}	
		return iRet;
	}
	
	// stop recording
	public int StopRecord()
	{	
		int iRet = NvssdkLibrary.RET_FAILED;
		iRet = NvssdkLibrary.INSTANCE.NetClient_StopCaptureFile(m_iConnectID);
		if (NvssdkLibrary.RET_SUCCESS == iRet) {
			System.out.println("StopCaptureFile success!(" + iRet + ")");
		} else {
			System.err.println("StopCaptureFile failed!(" + iRet + ")");
		}
		return iRet;
	};
	
	public int SaveYuvData(Pointer data, int len)
	{
		FileOutputStream fopYuv = null;
		File fileYuv;
		try
        {
    		fileYuv = new File("mySnapShot.yuv");
    		fopYuv = new FileOutputStream(fileYuv, true);
    		System.out.println("ouput path:" + fileYuv.getAbsolutePath());
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
	
	public int SnapShot(int _iType)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		//Local capture needs to be decoded to capture, window handle is passed null, only decoding does not display	
		if (CAPTURE_PICTURE_TYPE_BMP == _iType) {
			String strFile = "mySnapShot.bmp";
			ByteBuffer strBuffer = ByteBuffer.wrap(strFile.getBytes());
			iRet = NvssdkLibrary.INSTANCE.NetClient_CapturePicture(m_iConnectID, CAPTURE_PICTURE_TYPE_BMP, strBuffer);
			if(iRet <= 0) {
				System.err.println("CapturePicture:CAPTURE_PICTURE_TYPE_BMP failed! iSize=" + iRet);
				return iRet;
			} else {
				System.out.println("CapturePicture:CAPTURE_PICTURE_TYPE_BMP success! iSize=" + iRet + ",path=" + (System.getProperty("user.dir") + "/" + strFile));
			}
		} else if (CAPTURE_PICTURE_TYPE_JPG == _iType) {
			String strFile = "mySnapShot.jpg";
			ByteBuffer strBuffer = ByteBuffer.wrap(strFile.getBytes());
			iRet = NvssdkLibrary.INSTANCE.NetClient_CapturePicture(m_iConnectID, CAPTURE_PICTURE_TYPE_JPG, strBuffer);
			if(iRet <= 0) {
				System.err.println("CapturePicture:CAPTURE_PICTURE_TYPE_JPG failed! iSize=" + iRet);
				return iRet;
			}  else {
				System.out.println("CapturePicture:CAPTURE_PICTURE_TYPE_JPG success! iSize=" + iRet + ",path=" + (System.getProperty("user.dir") + "/" + strFile));
			}
		} else {
			//Do not apply for a memory block here, a pointer to a pointer
			PointerByReference dataYuv = new PointerByReference();
			iRet = NvssdkLibrary.INSTANCE.NetClient_CapturePic(m_iConnectID, dataYuv);
			if(iRet <= 0) {
				System.err.println("CapturePicYuv failed! iSize=" + iRet);
				return iRet;
			} else {
				System.out.println("CapturePicYuv success! iSize=" + iRet);
				//Save yuv capture data
				SaveYuvData(dataYuv.getValue(), iRet);
			}
		}
		return iRet;
	};	
	
	public int StartPsCallback()
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		
		//If you don't need to write a file, you only need to call back, you can pass the local file name empty
        iRet = NvssdkLibrary.INSTANCE.NetClient_SetDataPackCallBack(m_iConnectID, NvssdkLibrary.DTYPE_PS, CallbackReferenceHack.getFunctionPointer(cbkPsData), null);
        System.err.println("StartPsCallback! iRet=" + iRet);
        return iRet;
	}
	
	public int StopPsCallback()
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		//If you don't need to write a file, you only need to call back, you can pass the local file name empty
        iRet = NvssdkLibrary.INSTANCE.NetClient_SetDataPackCallBack(m_iConnectID, NvssdkLibrary.DTYPE_PS, Pointer.NULL, null);
        System.err.println("StopPsCallback! iRet=" + iRet);
        return iRet;
	}
	
	//////////////File download channel
	public int DownloadByFileMode(int _iSaveFileType, String _strDownloadFileName, String _strLocalSaveFileName)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		DOWNLOAD_FILE tFileInfo = new DOWNLOAD_FILE();
		tFileInfo.m_iSize = tFileInfo.size();
		tFileInfo.m_iSaveFileType = _iSaveFileType;
		tFileInfo.m_cRemoteFilename = _strDownloadFileName.getBytes(); //The name of the video to download, this video name is the video name queried from the device
		tFileInfo.m_cLocalFilename = _strLocalSaveFileName.getBytes(); //The name of the video to be saved in the local download, the default is the same as the name of the device video queried
		tFileInfo.m_iPosition = -1; //Use the positioning function
		tFileInfo.m_iSpeed = 32; //Download speed, the maximum is 32, the old device is prone to interruptions when downloading at the maximum speed, so after the download is successful, the speed can be adjusted to 16 times the speed
		tFileInfo.m_iReqMode = 1; 	//Require data mode 1,Frame mode;0,Stream mode
		tFileInfo.write();
		IntBuffer iConnectIDPtr = IntBuffer.allocate(1);
		iRet = NvssdkLibrary.INSTANCE.NetClient_NetFileDownload(iConnectIDPtr, m_iLogonID, NvssdkLibrary.DOWNLOAD_CMD_FILE, tFileInfo.getPointer(), tFileInfo.size());
		if (NvssdkLibrary.RET_SUCCESS == iRet) {
			//Set the bare stream callback, receive audio and video bare stream data in the callback
			m_iConnectID = iConnectIDPtr.get();
			//NetClient.SetRawFrameCallBack(iConnectID, _cbkRawFrame, null);
			// adjust the speed
			DOWNLOAD_CONTROL tControl = new DOWNLOAD_CONTROL();
			tControl.m_iSize = tControl.size();
			tControl.m_iPosition = -1;
			tControl.m_iSpeed = 16;
			tControl.m_iReqMode = 1;
			tControl.write();
			iConnectIDPtr.rewind();

			iRet = NvssdkLibrary.INSTANCE.NetClient_NetFileDownload(iConnectIDPtr, m_iLogonID, NvssdkLibrary.DOWNLOAD_CMD_CONTROL, tControl.getPointer(), tControl.size());
		} else {
			System.err.println("NetFileDownload:DOWNLOAD_CMD_FILE fail! iRet=" + iRet);
		}
		return iRet;
	};

	public int DownloadByTimespanMode(int _iSaveFileType, NVS_FILE_TIME tBegin, NVS_FILE_TIME tEnd, String _strLocalSaveFileName)
	{
		DOWNLOAD_TIMESPAN tDownloadTimeSpan = new DOWNLOAD_TIMESPAN();
		tDownloadTimeSpan.m_iSize = tDownloadTimeSpan.size();
		tDownloadTimeSpan.m_iSaveFileType = _iSaveFileType; //NvssdkLibrary.DOWNLOAD_FILE_TYPE_SDV;
		tDownloadTimeSpan.m_iFileFlag = 0;	//0:Download multiple files  1:Download into a single file

		tDownloadTimeSpan.m_cLocalFilename = _strLocalSaveFileName.getBytes();
		tDownloadTimeSpan.m_iChannelNO = m_iChannelNo; //The channel number is assigned according to the actual downloaded device channel number
		tDownloadTimeSpan.m_iStreamNo = m_iStreamNo; //Stream number: 0-main stream, 1-secondary stream
		//Download start time by time period
		tDownloadTimeSpan.m_tTimeBegin.iYear = tBegin.iYear;
		tDownloadTimeSpan.m_tTimeBegin.iMonth = tBegin.iMonth;
		tDownloadTimeSpan.m_tTimeBegin.iDay = tBegin.iDay;
		tDownloadTimeSpan.m_tTimeBegin.iHour = tBegin.iHour;
		tDownloadTimeSpan.m_tTimeBegin.iMinute = tBegin.iMinute;
		tDownloadTimeSpan.m_tTimeBegin.iSecond = tBegin.iSecond;
		//Download end time by time period
		tDownloadTimeSpan.m_tTimeEnd.iYear = tEnd.iYear;
		tDownloadTimeSpan.m_tTimeEnd.iMonth = tEnd.iMonth;
		tDownloadTimeSpan.m_tTimeEnd.iDay = tEnd.iDay;
		tDownloadTimeSpan.m_tTimeEnd.iHour = tEnd.iHour;
		tDownloadTimeSpan.m_tTimeEnd.iMinute = tEnd.iMinute;
		tDownloadTimeSpan.m_tTimeEnd.iSecond = tEnd.iSecond;
		
		tDownloadTimeSpan.m_iPosition = -1; //Use the positioning function
		tDownloadTimeSpan.m_iSpeed = 32; //Download speed, the maximum is 32, the old device is prone to interruptions when downloading at the maximum speed, so after the download is successful, the speed can be adjusted to 16 times the speed
		tDownloadTimeSpan.m_iReqMode = 1;	//1:down frame mode,0= Flow pattern; if (mode == 0) Device do not send download time !
		tDownloadTimeSpan.write();
		IntBuffer iConnID = IntBuffer.allocate(1);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_NetFileDownload(iConnID, m_iLogonID, NvssdkLibrary.DOWNLOAD_CMD_TIMESPAN, tDownloadTimeSpan.getPointer(), tDownloadTimeSpan.size());
	 	if (NvssdkLibrary.RET_SUCCESS == iRet)
	 	{
			//Set the bare stream callback, receive audio and video bare stream data in the callback
	 		m_iConnectID = iConnID.get();
			//NetClient.SetRawFrameCallBack(iConnectID1, JavaClientDemo.cbkRawFrame, null);	
			// adjust the speed
			DOWNLOAD_CONTROL tControl = new DOWNLOAD_CONTROL();
			tControl.m_iSize = tControl.size();
			tControl.m_iPosition = -1;
			tControl.m_iSpeed = 16;
			tControl.m_iReqMode = 1;
			tControl.write();
			NvssdkLibrary.INSTANCE.NetClient_NetFileDownload(iConnID, m_iLogonID, NvssdkLibrary.DOWNLOAD_CMD_CONTROL, tControl.getPointer(), tControl.size());
	 	} else {
			System.err.println("NetFileDownload:DOWNLOAD_CMD_TIMESPAN fail! iRet=" + iRet);
		}
	 	
		return iRet;
	}	
	
	public int StopDownload()
	{

		int iRet = NvssdkLibrary.INSTANCE.NetClient_NetFileStopDownloadFile(m_iConnectID);
		System.out.println("stopDownload(" + iRet + ")");
		m_iConnectID = -1;
		return iRet;

	}
	
	public int StartPlayback(String _strFileName)
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		PlayerParam tParam = new PlayerParam();
		tParam.iSize = tParam.size();
		tParam.iLogonID = m_iLogonID;
		tParam.tRawNotifyInfo = new RawFrameNotifyInfo();
		tParam.tRawNotifyInfo.pcbkRawFrameNotify = cbkRawFrame;
		tParam.tRawNotifyInfo.pUserData = null;
		tParam.tRawNotifyInfo.iForbidDecodeEnable = NvssdkLibrary.RAW_NOTIFY_FORBID_DECODE;
		
		tParam.strFileName = _strFileName.getBytes();
		tParam.write();
		IntBuffer iConnectIDPtr = IntBuffer.allocate(1);
		
		iRet = NvssdkLibrary.INSTANCE.NetClient_PlayBack(iConnectIDPtr, NvssdkLibrary.PLAYBACK_TYPE_FILE, tParam, null);
		if (iRet < 0)
		{
			System.err.println("StartPlayback:PLAYBACK_TYPE_FILE fail!iRet=" + iRet);
		}
		else
		{
			m_iConnectID = iConnectIDPtr.get();
		}
		return iRet;
	}
	
	public int StartPlayback(NVS_FILE_TIME tBegin, NVS_FILE_TIME tEnd)
	{
		PlayerParam tParam = new PlayerParam();
		tParam.iSize = tParam.size();
		tParam.iLogonID = m_iLogonID;
		tParam.iChannNo = m_iChannelNo;
		tParam.tBeginTime = tBegin;
		tParam.tEndTime = tEnd;
		tParam.tRawNotifyInfo = new RawFrameNotifyInfo();
		tParam.tRawNotifyInfo.pcbkRawFrameNotify = cbkRawFrame;
		tParam.tRawNotifyInfo.pUserData = null;
		tParam.tRawNotifyInfo.iForbidDecodeEnable = NvssdkLibrary.RAW_NOTIFY_FORBID_DECODE;
		tParam.write();
		IntBuffer iConnectIDPtr = IntBuffer.allocate(1);
		int iRet =	NvssdkLibrary.INSTANCE.NetClient_PlayBack(iConnectIDPtr, NvssdkLibrary.PLAYBACK_TYPE_TIME, tParam, null);
		if (iRet < 0)
		{
			System.err.println("StartPlayback:PLAYBACK_TYPE_TIME fail!\n");
		}
		else
		{
			m_iConnectID = iConnectIDPtr.get();
		}
		return iRet;
	}
	
	public int StopPlayback()
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		NativeLong iConnectID = new NativeLong(m_iConnectID);
		iRet = NvssdkLibrary.INSTANCE.NetClient_StopPlayBack(iConnectID);
		m_iConnectID = -1;
		System.out.println("StopPlayback(ID:" + m_iConnectID + "!");
		return iRet;
	}
	
	public String GetPictureDirection(int _ulID)
	{
		return String.format("PIC-%d-%s",_ulID, m_device.GetIP());
	}
	
	public int SavePic(String FileName, Pointer pic, int len)
	{
        if (len < 1)
        	return 0;
        
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
	
	//Face image stream callback
	NET_PICSTREAM_NOTIFY cbkFacePicData = new NET_PICSTREAM_NOTIFY(){
		public int apply(int _ulID, NativeLong _lCommand, Pointer _tInfo, int _iLen, Pointer _lpUserData)
		{
			int lCommand = _lCommand.intValue();
			if(lCommand == NvssdkLibrary.NET_PICSTREAM_CMD_FACE)
			{
				tagFacePicStream tFacePicStream = new tagFacePicStream(_tInfo);
	            tFacePicStream.read();	
	            System.out.println("PicDataNotify Snap Face count " + tFacePicStream.iFaceCount) ;
	    		  
	        	int uiYear = tFacePicStream.ptFullData.tPicTime.uiYear;
	       		int uiMonth = tFacePicStream.ptFullData.tPicTime.uiMonth;
	    		int uiDay = tFacePicStream.ptFullData.tPicTime.uiDay; 
	    		int uiWeek = tFacePicStream.ptFullData.tPicTime.uiWeek; 
	    		int uiHour = tFacePicStream.ptFullData.tPicTime.uiHour; 
	    		int uiMinute = tFacePicStream.ptFullData.tPicTime.uiMinute; 
	    		int uiSecondsr = tFacePicStream.ptFullData.tPicTime.uiSecondsr;
	    		int uiMilliseconds = tFacePicStream.ptFullData.tPicTime.uiMilliseconds;
	    		
	    		// face map data
	    		String strFileName = new String();
	    		String strNegName = new String();
	    		strFileName +="FACE" +  uiYear + "-"+ uiMonth + "-"+ uiDay + "-"+ uiWeek + "-"+ uiHour + "-"+ uiMinute+ "-"+ uiSecondsr + "-"+ uiMilliseconds;    		
	    		strNegName +="Neg" +  uiYear + "-"+ uiMonth + "-"+ uiDay + "-"+ uiWeek + "-"+ uiHour + "-"+ uiMinute+ "-"+ uiSecondsr + "-"+ uiMilliseconds;
	    		SavePic(GetPictureDirection(_ulID) + "/" + strFileName + "full.jpg", tFacePicStream.ptFullData.pcPicData, tFacePicStream.ptFullData.iDataLen);
	    		for(int i = 0; i < tFacePicStream.iFaceCount && i < 32; i++){
	    			SavePic(GetPictureDirection(_ulID) + "/" + strFileName + "face" + i + ".jpg", tFacePicStream.ptFaceData[i].pcPicData, tFacePicStream.ptFaceData[i].iDataLen);
	    			// face basemap data
	    			if(1 == tFacePicStream.ptFaceData[i].iAlramType) {
	    				SavePic(GetPictureDirection(_ulID) + "/" + strNegName + "neg" + i + ".jpg", tFacePicStream.ptFaceData[i].pcNegPicData, tFacePicStream.ptFaceData[i].iNegPicLen);
	    			}	    			
	    		}				
			}else{
	            System.out.println("PicDataNotify other Snap type  " + _lCommand) ;
			}
			
			return 0;
		}
	};
	
	// Traffic image stream callback
	NET_PICSTREAM_NOTIFY cbkITSPicData = new NET_PICSTREAM_NOTIFY(){
		public int apply(int _ulID, NativeLong _lCommand, Pointer _tInfo, int _iLen, Pointer _lpUserData)
		{
			int lCommand = _lCommand.intValue();
			if(lCommand != NvssdkLibrary.NET_PICSTREAM_CMD_ITS)
			{
				System.out.println("PicDataNotify other Snap type  " + _lCommand) ;
				return 0;
			}
			
			tagItsPicStream tItsPicStream = new tagItsPicStream(_tInfo);
			tItsPicStream.read();	
			String strIP = new String(tItsPicStream.cCameraIP).trim();
			String strPlate = new String(tItsPicStream.cPlate).trim();
			
			String strOutput = "PicDataNotify Snap Pic "+strIP+ 
					" iChannelID: " + tItsPicStream.iChannelID+
					" count:"+ tItsPicStream.iPicCount  +
					" cPlate:" + strPlate;
			if(tItsPicStream.iCarColor >= 0 && tItsPicStream.iCarColor < MAX_CAR_COLOR) {
				strOutput += " Car Color:" + strCarColor[tItsPicStream.iCarColor]; 
			} else {
				strOutput += "Car Color:" + "Unknown";
			}
			
			if(tItsPicStream.iPlateColor >= 0 && tItsPicStream.iPlateColor < MAX_CAR_PLATE_COLOR) {
				strOutput += " Plate Color:" + strCarPlateColor[tItsPicStream.iPlateColor]; 
			} else {
				strOutput += "Plate Color:" + "Unknown";
			}
			
			System.out.println(strOutput) ;
			
			if(tItsPicStream.iPicCount <= 0){
				System.out.println("error count " + tItsPicStream.iPicCount) ;
				return -1;
			}
			
			int uiYear = tItsPicStream.ptPicData[0].tPicTime.uiYear;
			int uiMonth = tItsPicStream.ptPicData[0].tPicTime.uiMonth;
			int uiDay = tItsPicStream.ptPicData[0].tPicTime.uiDay; 
			int uiWeek = tItsPicStream.ptPicData[0].tPicTime.uiWeek; 
			int uiHour = tItsPicStream.ptPicData[0].tPicTime.uiHour; 
			int uiMinute = tItsPicStream.ptPicData[0].tPicTime.uiMinute; 
			int uiSecondsr = tItsPicStream.ptPicData[0].tPicTime.uiSecondsr;
			int uiMilliseconds = tItsPicStream.ptPicData[0].tPicTime.uiMilliseconds;
			
			String strFileName = new String();
			strFileName +="ITS" +  uiYear + "-"+ uiMonth + "-"+ uiDay + "-"+ uiWeek + "-"+ uiHour + "-"+ uiMinute+ "-"+ uiSecondsr + "-"+ uiMilliseconds;
			int iCount = 0;
			for(int i = 0; i < tItsPicStream.iPicCount && i < 8; i++){
				if(iCount > 2000)
				{
					System.out.println("save picture over 20000!") ;
				}
				SavePic(GetPictureDirection(_ulID) + "/" + strFileName + "pic" + iCount + ".jpg", tItsPicStream.ptPicData[i].pcPicData, tItsPicStream.ptPicData[i].iDataLen);
				iCount++;
			}	
			
			if(tItsPicStream.iPlatCount > 0) {
				tItsPicStream.ptPlatData.read();			
				SavePic(GetPictureDirection(_ulID) + "/" + strFileName + "plat" + "CP" + ".jpg", tItsPicStream.ptPlatData.pcPicData, tItsPicStream.ptPlatData.iDataLen);
			}
			
			for(int i = 0; i < tItsPicStream.iFaceCount && i < 8; i++){
				tItsPicStream.ptFaceData[i].read();	
				
				SavePic(GetPictureDirection(_ulID) + "/" + strFileName + "plat" + "RL" + + i + ".jpg", tItsPicStream.ptFaceData[i].pcPicData, tItsPicStream.ptFaceData[i].iDataLen);
			}
			
			String strlog = "PicDataNotify Snap Pic" + "Pic:" + tItsPicStream.iPicCount + ",Plat:" + tItsPicStream.iPlatCount + ",Face:" + tItsPicStream.iFaceCount;
			System.out.println(strlog) ;
					
			String strBWPlateData = new String();
			if (tItsPicStream.pcBWPlateData != Pointer.NULL)
			{
				strBWPlateData = tItsPicStream.pcBWPlateData.getString(tItsPicStream.iBWPlatePicLen);
			}
			System.out.println("iPreset:" + tItsPicStream.iPreset + ",iArea:" + tItsPicStream.iArea + ",sOriginalImgWidth:" + tItsPicStream.sOriginalImgWidth 
					+ ",pcBWPlateData:" + strBWPlateData + ",iRealImgWidth:" + tItsPicStream.iRealImgWidth + ",iRealImgHeight:" + tItsPicStream.iRealImgHeight
				 + ",iTargetType:" + tItsPicStream.iTargetType + ",iNewTargetType:" + tItsPicStream.iNewTargetType);
			
			return 0;
		}
	};
	
	// Intelligent analysis picture stream callback
	NET_PICSTREAM_NOTIFY cbkVcaPicData = new NET_PICSTREAM_NOTIFY(){
		public int apply(int _ulID, NativeLong _lCommand, Pointer _tInfo, int _iLen, Pointer _lpUserData)
		{
			int lCommand = _lCommand.intValue();
			if(lCommand == NvssdkLibrary.NET_PICSTREAM_CMD_VCA){
				tagVcaPicStream tVcaPicStream = new tagVcaPicStream(_tInfo);
	            tVcaPicStream.read();	
	            String strIP = new String(tVcaPicStream.cCameraIP).trim();
	            System.out.println("PicDataNotify Snap Pic "+strIP+ 
	            		" iChannelID " + tVcaPicStream.iChannelID+
	            		" count "+ tVcaPicStream.iPicCount  +
	            		" iEventType " + tVcaPicStream.iEventType 
	            		) ;
	            if( 0 < tVcaPicStream.iPtzInfoLen)
	            {
	            	System.out.println( "iNorthAngle" + tVcaPicStream.pPtzInfo.iNorthAngle);
	            }
	            if(tVcaPicStream.iPicCount <= 0)
	            {
	            	System.out.println("error count " + tVcaPicStream.iPicCount) ;
	            	return -1;
	            }
	           
	        	int uiYear = tVcaPicStream.ptPicData[0].tPicTime.uiYear;
	       		int uiMonth = tVcaPicStream.ptPicData[0].tPicTime.uiMonth;
	    		int uiDay = tVcaPicStream.ptPicData[0].tPicTime.uiDay; 
	    		int uiWeek = tVcaPicStream.ptPicData[0].tPicTime.uiWeek; 
	    		int uiHour = tVcaPicStream.ptPicData[0].tPicTime.uiHour; 
	    		int uiMinute = tVcaPicStream.ptPicData[0].tPicTime.uiMinute; 
	    		int uiSecondsr = tVcaPicStream.ptPicData[0].tPicTime.uiSecondsr;
	    		int uiMilliseconds = tVcaPicStream.ptPicData[0].tPicTime.uiMilliseconds;
	    		
	    		String strFileName = new String();
	    		strFileName +="VCA" +  uiYear + "-"+ uiMonth + "-"+ uiDay + "-"+ uiWeek + "-"+ uiHour + "-"+ uiMinute+ "-"+ uiSecondsr + "-"+ uiMilliseconds;
	    		for(int i = 0; i < tVcaPicStream.iPicCount && i < 3; i++){
	    			SavePic(GetPictureDirection(_ulID) + "/" + strFileName + "pic" + i + ".jpg", tVcaPicStream.ptPicData[i].pcPicData, tVcaPicStream.ptPicData[i].iDataLen);
	    		}
				
			}else{
	            System.out.println("PicDataNotify other Snap type  " + _lCommand) ;
			}
			
			return 0;
		}
	};
	
	
	public boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {// Determine whether the directory exists
			System.out.println("Failed to create directory, target directory already exists!");
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {// Does the end end with "/"
			destDirName = destDirName + File.separator;
		}
		if (dir.mkdirs()) {// create the target directory
			System.out.println("Create directory successfully!" + destDirName);
			return true;
		} else {
			System.out.println("Failed to create directory!");
			return false;
		}
	}
	
	//Image stream,//If the _cbkFacePicData callback is empty, the default callback is used
	public int StartRecvSnapFacePicStream()
	{
		tagNetPicPara tNetPicParam = new tagNetPicPara();
		tNetPicParam.iStructLen = tNetPicParam.size();
		tNetPicParam.iChannelNo = m_iChannelNo;
		tNetPicParam.cbkPicStreamNotify = cbkFacePicData; //Snapshot callback function

		tNetPicParam.pvUser = null;
		tNetPicParam.write();
		
		IntBuffer pConnectID = IntBuffer.allocate(1);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_StartRecvNetPicStream(m_iLogonID, tNetPicParam, tNetPicParam.size(), pConnectID);
		if (iRet < 0)
		{
			 System.out.println("StartRecvSnapFacePicStream Failed!");
		}
		else 
		{
			m_iConnectID = pConnectID.get();
			createDir(GetPictureDirection(m_iConnectID));

			System.out.println("StartRecvSnapFacePicStream Success! ConnectID(" + m_iConnectID + ")");
		}
		return iRet;
	}
	
	//traffic, //use default callback if _cbkITSPicData callback is empty
	public int StartRecvSnapITSPicStream( )
	{
		tagNetPicPara tNetPicParam = new tagNetPicPara();
		tNetPicParam.iStructLen = tNetPicParam.size();
		tNetPicParam.iChannelNo = m_iChannelNo;
		tNetPicParam.cbkPicStreamNotify = cbkITSPicData; //snapshot callback function

		tNetPicParam.pvUser = null;
		tNetPicParam.write();
		
		IntBuffer pConnectID = IntBuffer.allocate(1);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_StartRecvNetPicStream(m_iLogonID, tNetPicParam, tNetPicParam.size(), pConnectID);
		if (iRet < 0)
		{
			 System.out.println("StartRecvSnapITSPicStream Failed!");
		}
		else 
		{
			m_iConnectID = pConnectID.get();
			createDir(GetPictureDirection(m_iConnectID));
			System.out.println("StartRecvSnapITSPicStream Success! ConnectID(" + m_iConnectID + ")");
		}
		return iRet;
	}
	
	//Use the default callback if the _cbkVcaPicData callback is empty
	public int StartRecvSnapVcaPicStream( )
	{
		tagNetPicPara tNetPicParam = new tagNetPicPara();
		tNetPicParam.iStructLen = tNetPicParam.size();
		tNetPicParam.iChannelNo = m_iChannelNo;
		tNetPicParam.cbkPicStreamNotify = cbkVcaPicData; //snapshot callback function

		tNetPicParam.pvUser = null;
		tNetPicParam.write();
		
		IntBuffer pConnectID = IntBuffer.allocate(1);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_StartRecvNetPicStream(m_iLogonID, tNetPicParam, tNetPicParam.size(), pConnectID);
		if (iRet < 0)
		{
			 System.out.println("StartRecvSnapVcaPicStream Failed!");
		}
		else 
		{
			m_iConnectID = pConnectID.get();
			createDir(GetPictureDirection(m_iConnectID));
			System.out.println("StartRecvSnapVcaPicStream Success! ConnectID(" + m_iConnectID + ")");
		}
		return iRet;
	}
	
	public int StopRecvPicStream()
	{
		int iRet = NvssdkLibrary.RET_FAILED;
		iRet = NvssdkLibrary.INSTANCE.NetClient_StopRecvNetPicStream(m_iConnectID);
		m_iConnectID = -1;
		System.out.println("StopRecvPicStream(" + iRet + ")");
		return iRet;
	}

}

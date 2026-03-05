package src;

import java.awt.Component;

import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.CLIENTINFO;
import src.NVSSDK.COMFORMAT;
import src.NVSSDK.DECYUV_NOTIFY_V4;
import src.NVSSDK.ENCODERINFO;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.NVSDATA_NOTIFY;
import src.NVSSDK.NetPicPara;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.RAWFRAME_NOTIFY;
import src.NVSSDK.RECT;
import src.NVSSDK.RECVDATA_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.NVS_FILE_DATA_EX;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class NetClient {

	private static NVSSDK nvssdk = (NVSSDK)(System.getProperty("os.name").toLowerCase().startsWith("win")?Native.loadLibrary("NVSSDK.dll",NVSSDK.class):Native.loadLibrary("libnvssdk.so",NVSSDK.class));

	public static int GetVersion(SDK_VERSION _ver) {
		return nvssdk.NetClient_GetVersion(_ver);
	}

	public static int SetNotifyFunction(MAIN_NOTIFY _cbkMainNotify,
			ALARM_NOTIFY _cbkAlarmNotify,PARACHANGE_NOTIFY _cbkParaChangeNotify) {
		return nvssdk.NetClient_SetNotifyFunction_V4(_cbkMainNotify, _cbkAlarmNotify, _cbkParaChangeNotify, null, null);
	}

	public static int SetPort(int _iServerPort, int _iClientPort) {
		return nvssdk.NetClient_SetPort(_iServerPort, _iClientPort);
	}

	public static int Startup() {
		return nvssdk.NetClient_Startup_V4(0, 0, 0);
	}

	public static int Cleanup() {
		return nvssdk.NetClient_Cleanup();
	}

	public static int Logon(String _cProxy, String _cIP, String _cUserName,
			String _cPassword, String _pcProID, int _iPort) {
		return nvssdk.NetClient_Logon(_cProxy, _cIP, _cUserName,
				_cPassword, _pcProID, _iPort);
	}
	
	public static int Logon_V4(int _iLogonType, Pointer _pvPara, int _iInBufSize) {
		return nvssdk.NetClient_Logon_V4(_iLogonType, _pvPara, _iInBufSize);
	}

	public static int Logoff(int _iLogonID) {
		return nvssdk.NetClient_Logoff(_iLogonID);
	}

	public static int StartRecv(IntByReference _ulConID, CLIENTINFO _cltInfo,
			RECVDATA_NOTIFY _cbkDataNotify) {
		return nvssdk.NetClient_StartRecvEx(_ulConID, _cltInfo,
				_cbkDataNotify, Pointer.NULL);
	}
	
	public static int StartRecv_V4(IntByReference _uiRecvID, CLIENTINFO _cltInfo
			, NVSDATA_NOTIFY _cbkDataArrive, Pointer _iUserData) {
		return nvssdk.NetClient_StartRecv_V4(_uiRecvID, _cltInfo, _cbkDataArrive, _iUserData);
	}

	public static int StopRecv(int _ulConID) {
		return nvssdk.NetClient_StopRecv(_ulConID);
	}

	public static int StartCaptureData(int _ulConID) {
		return nvssdk.NetClient_StartCaptureData(_ulConID);
	}
	
	public static int StopCaptureData(int _ulConID) {
		return nvssdk.NetClient_StopCaptureData(_ulConID);
	}

	public static int StartPlay(int _ulConID, Component _hWnd, int _iDecflag) {
		RECT rcShow = new RECT();
		return nvssdk.NetClient_StartPlay(_ulConID, 0, rcShow,
				_iDecflag);
	}

	public static int StopPlay(int _ulConID) {
		return nvssdk.NetClient_StopPlay(_ulConID);
	}

	public static int StartCaptureFile(int _ulConID, String _cFileName,
			int _iRecFileType) {
		return nvssdk.NetClient_StartCaptureFile(_ulConID,
				_cFileName, _iRecFileType);
	}

	public static int StopCaptureFile(int _ulConID) {
		return nvssdk.NetClient_StopCaptureFile(_ulConID);
	}
	
	public static int StartRecvNetPicStream(int _iLogonID, NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID) {
		return nvssdk.NetClient_StartRecvNetPicStream(_iLogonID, _ptPara, _iBufLen,  _puiRecvID);
	}
	
	public static int GetLogonStatus(int _iLogonID) {
		return nvssdk.NetClient_GetLogonStatus(_iLogonID);
	}
	
	public static int GetDevInfo(int _iLogonID ,ENCODERINFO _pEncoderInfo) {
		return nvssdk.NetClient_GetDevInfo(_iLogonID, _pEncoderInfo);
	}
	
	public static int Query_V4(int _iLogonID, int _iCmd, int _iChannel, Pointer _pvCmdBuf, int _iBufLen) {
		return nvssdk.NetClient_Query_V4(_iLogonID, _iCmd, _iChannel, _pvCmdBuf, _iBufLen);
	}
	
	public static int NetFileGetFileCount(int _iLogonID, IntByReference _piTotalCount, IntByReference _piCurrentCount) {
		return nvssdk.NetClient_NetFileGetFileCount(_iLogonID, _piTotalCount, _piCurrentCount);
	}
	
	public static int NetFileGetQueryfileEx(int _iLogonID, int _iFileIndex, NVS_FILE_DATA_EX _pFileInfo) {
		return nvssdk.NetClient_NetFileGetQueryfileEx(_iLogonID, _iFileIndex,  _pFileInfo);
	}
	
	public static int NetFileDownload(IntByReference _uiConID, int _iLogonID, int _iCmd, Pointer _pvBuf, int _iBufSize) {
		return nvssdk.NetClient_NetFileDownload(_uiConID, _iLogonID, _iCmd, _pvBuf, _iBufSize);
	}
	
	public static int  NetFileStopDownloadFile(int _uiConID) {
		return nvssdk.NetClient_NetFileStopDownloadFile(_uiConID);
	}
	
	public static int NetFileGetDownloadPos(int _uiConID, IntByReference _piPos, IntByReference _piDLSize) {
		return nvssdk.NetClient_NetFileGetDownloadPos(_uiConID, _piPos, _piDLSize);
	}
	
	public static int SetRawFrameCallBack(int _uiConID, RAWFRAME_NOTIFY _cbkGetFrame, Pointer _pContext) {
		return nvssdk.NetClient_SetRawFrameCallBack(_uiConID, _cbkGetFrame, _pContext);
	}
	
	public static int SetDecCallBack_V4(int _uiConID, DECYUV_NOTIFY_V4 _cbkDecYUV, Pointer _pvUserData) {
		return nvssdk.NetClient_SetDecCallBack_V4(_uiConID, _cbkDecYUV, _pvUserData);
	}
	
	public static int GetDeviceType(int _iLogonID, int _iChannelNo, IntByReference _piComPort
			, IntByReference _iDevAddress, Pointer _cDeviceType) {
		return nvssdk.NetClient_GetDeviceType(_iLogonID, _iChannelNo, _piComPort, _iDevAddress, _cDeviceType);
	}
	
	public static int GetComFormat_V2(int _iLogonID, COMFORMAT _ptComFormat) {
		return nvssdk.NetClient_GetComFormat_V2(_iLogonID, _ptComFormat);
	}
	
	public static int SetComFormat_V2(int _iLogonID, COMFORMAT _ptComFormat) {
		return nvssdk.NetClient_SetComFormat_V2(_iLogonID, _ptComFormat);
	}
	
	public static int DeviceCtrlEx(int _iLogonID, int _iChannelNo, int _iActionType
			, int _iParam1, int _iParam2, int _iControlType) {
		return nvssdk.NetClient_DeviceCtrlEx(_iLogonID, _iChannelNo, _iActionType, _iParam1, _iParam2, _iControlType);
	}
	
	public static int CapturePicture(int _uiConID, int _iPicType, String strFileName) {
		return nvssdk.NetClient_CapturePicture(_uiConID, _iPicType, strFileName);
	}
	
	public static int CapturePic(int _uiConID, PointerByReference _pucData) {
		return nvssdk.NetClient_CapturePic(_uiConID, _pucData);
	}
	
	public static int GetSensorFlip(int _iLogonID, int _iChannel, IntByReference _piFlip) {
		return nvssdk.NetClient_GetSensorFlip(_iLogonID, _iChannel, _piFlip);
	}
	
	public static int GetChannelProperty(int _iLogonID, int _iChannel, int _iCmd, Pointer _pvBuf, int _iBufSize) {
		return nvssdk.NetClient_GetChannelProperty(_iLogonID, _iChannel, _iCmd, _pvBuf, _iBufSize);
	}

	public static int DigitalChannelSend(int _iLogonID, int _iChannel, byte[] _ucBuf, int _iLength) {
		return nvssdk.NetClient_DigitalChannelSend(_iLogonID, _iChannel, _ucBuf, _iLength);
	}
	
	public static int ComSend(int _iLogonID, byte[] _ucBuf, int _iLength, int _iComNo) {
		return nvssdk.NetClient_ComSend(_iLogonID, _ucBuf, _iLength, _iComNo);
	}
	
	public static int SendCommand(int _iLogonID, int _iCommand, int _iChannel, Pointer _pvBuffer, int _iBufSize) {
		return nvssdk.NetClient_SendCommand(_iLogonID, _iCommand, _iChannel, _pvBuffer, _iBufSize);
	}
	
	public static int GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _pvOutBuffer
			, int _iOutBufferSize, IntByReference _piBytesReturned) {
		return nvssdk.NetClient_GetDevConfig(_iLogonID, _iCommand, _iChannel, _pvOutBuffer, _iOutBufferSize, _piBytesReturned);
	}
	
	public static int SetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _lpInBuffer, int _iInBufferSize) {
		return nvssdk.NetClient_SetDevConfig(_iLogonID, _iCommand, _iChannel, _lpInBuffer, _iInBufferSize);
	}
}

package src;

import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.NetPicPara;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.ENCODERINFO;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class NetClient {

	private static NVSSDK nvssdk = (NVSSDK)(System.getProperty("os.name").toLowerCase().startsWith("win")?Native.loadLibrary("NVSSDK.dll",NVSSDK.class):Native.loadLibrary("libnvssdk.so",NVSSDK.class));
	
	public static int SetNotifyFunction(MAIN_NOTIFY _cbkMain, ALARM_NOTIFY _cbkAlarmNotify, PARACHANGE_NOTIFY _cbkParaChange) {
		return nvssdk.NetClient_SetNotifyFunction_V4(_cbkMain, _cbkAlarmNotify, _cbkParaChange, null, null);
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
	
	public static int Logon(int _iLogonType, Pointer _pBuf, int _iBufSize) {
		return nvssdk.NetClient_Logon_V4(_iLogonType, _pBuf, _iBufSize);
	}

	public static int Logoff(int _iLogonID) {
		return nvssdk.NetClient_Logoff(_iLogonID);
	}
	
	public static int GetLogonStatus(int _iLogonID) {
		return nvssdk.NetClient_GetLogonStatus(_iLogonID);
	}
	
	public static int GetVersion(SDK_VERSION _ver) {
		return nvssdk.NetClient_GetVersion(_ver);
	}
	
	public static int SetPort(int _iServerPort) {
		return nvssdk.NetClient_SetPort(_iServerPort, _iServerPort);
	}
	
	public static int SetDsmConfig(int _iCommand, Pointer _pvBuf, int _iBufSize) {
		return nvssdk.NetClient_SetDsmConfig(_iCommand, _pvBuf, _iBufSize);
	}
	
	public static int GetDsmRegstierInfo(int _iCommand, Pointer _pvBuf, int _iBufSize) {
		return nvssdk.NetClient_GetDsmRegstierInfo(_iCommand, _pvBuf, _iBufSize);
	}
	
	public static int GetDevInfo(int _iLogonID ,ENCODERINFO _pEncoderInfo) {
		return nvssdk.NetClient_GetDevInfo(_iLogonID, _pEncoderInfo);
	}
	
	public static int SetTime(int _iLogonID, int _iyy, int _imo, int _idd, int _ihh, int _imi, int _iss) {
		return nvssdk.NetClient_SetTime(_iLogonID, _iyy, _imo, _idd, _ihh, _imi, _iss);
	}
	
	public static int FaceConfig(int _iLogonId, int _iCmdId, int _iChanNo, Pointer _lpIn, int _iInLen, Pointer _lpOut, int _iOutLen) {
		return nvssdk.NetClient_FaceConfig(_iLogonId, _iCmdId, _iChanNo, _lpIn, _iInLen, _lpOut, _iOutLen);
	}
	
	public static int SetDevConfig(int _iLogonId, int _iCommand, int _iChannel, Pointer _lpInBuffer, int _iInBufferSize) {
		return nvssdk.NetClient_SetDevConfig(_iLogonId, _iCommand, _iChannel, _lpInBuffer, _iInBufferSize);
	}
	
	public static int GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _lpOutBuffer, int _iOutBufferSize) {
		IntByReference pRet = new IntByReference();
		return nvssdk.NetClient_GetDevConfig(_iLogonID, _iCommand, _iChannel, _lpOutBuffer, _iOutBufferSize, pRet);
	}
	
	public static int StartRecvNetPicStream(int _iLogonID, NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID) {
		return nvssdk.NetClient_StartRecvNetPicStream(_iLogonID, _ptPara, _iBufLen,  _puiRecvID);
	}
	
	public static int StopRecvNetPicStream(int _iRecvID) {
		return nvssdk.NetClient_StopRecvNetPicStream(_iRecvID);
	}

	public static int SetAlarmConfig(int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, Pointer _pvCmdBuf) {
		return nvssdk.NetClient_SetAlarmConfig(_iLogonID, _iChannel, _iAlarmType,  _iCmd, _pvCmdBuf);
	}
	
	public static int GetAlarmConfig(int _iLogonID, int _iChannel, int _iAlarmType, int _iCmd, Pointer _pvCmdBuf) {
		return nvssdk.NetClient_GetAlarmConfig(_iLogonID, _iChannel, _iAlarmType,  _iCmd, _pvCmdBuf);
	}

	public static int GetOsdText(int _iLogonID, int _iChannelNum, Pointer _pcOSDText, IntByReference _pulTextColor){
		return nvssdk.NetClient_GetOsdText(_iLogonID, _iChannelNum, _pcOSDText, _pulTextColor);
	}
	
	public static int GetDigitalChannelNum(int _iLogonID, IntByReference _piDigitChannelNum) {
		return nvssdk.NetClient_GetDigitalChannelNum(_iLogonID, _piDigitChannelNum);
	}
	
	public static int GetChannelNum(int _iLogonID, IntByReference _piChannelNum) {
		return nvssdk.NetClient_GetChannelNum(_iLogonID, _piChannelNum);
	}
	
	public static int NetFileDownload(IntByReference _uiConID, int _iLogonID, int _iCmd, Pointer _pvBuf, int _iBufSize) {
		return nvssdk.NetClient_NetFileDownload(_uiConID, _iLogonID, _iCmd, _pvBuf, _iBufSize);
	}
	
	public static int  NetFileStopDownloadFile(int _uiConID) {
		return nvssdk.NetClient_NetFileStopDownloadFile(_uiConID);
	}
	
	public static int VCASetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, Pointer _lpCmdBuf, int _iCmdBufLen) {
		return nvssdk.NetClient_VCASetConfig(_iLogonID, _iVCACmdID, _iChannel, _lpCmdBuf, _iCmdBufLen);
	}
	
	public static int VCAGetConfig(int _iLogonID, int _iVCACmdID, int _iChannel, Pointer _lpCmdBuf, int _iCmdBufLen) {
		return nvssdk.NetClient_VCAGetConfig(_iLogonID, _iVCACmdID, _iChannel, _lpCmdBuf, _iCmdBufLen);
	}
	
	public static int Query_V5(int _iLogonId, int _iCmdId, int _iChanNo, Pointer _lpIn, int _iInLen, Pointer _lpOut, int _iOutLen){
		return nvssdk.NetClient_Query_V5(_iLogonId, _iCmdId, _iChanNo, _lpIn, _iInLen, _lpOut, _iOutLen);
	}
	
	public static int SetCommonEnable(int _iLogonID, int _iEnableID, int _iChannel, int _iEnableValue){
		return nvssdk.NetClient_SetCommonEnable(_iLogonID, _iEnableID, _iChannel, _iEnableValue);
	}
}

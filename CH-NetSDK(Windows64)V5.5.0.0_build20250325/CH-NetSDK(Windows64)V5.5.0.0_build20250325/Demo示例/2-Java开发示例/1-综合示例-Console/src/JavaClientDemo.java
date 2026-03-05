package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Scanner;  
import java.util.HashMap;   
import java.util.Map;  
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Memory;

import com.nvs.*;
import com.nvs.sdk.*;
import com.nvs.sdk.NvssdkLibrary.ALARM_NOTIFY;
import com.nvs.sdk.NvssdkLibrary.ALARM_NOTIFY_V4;
import com.nvs.sdk.NvssdkLibrary.COMRECV_NOTIFY_V4;
import com.nvs.sdk.NvssdkLibrary.MAIN_NOTIFY;
import com.nvs.sdk.NvssdkLibrary.MAIN_NOTIFY_V4;
import com.nvs.sdk.NvssdkLibrary.PARACHANGE_NOTIFY;
import com.nvs.sdk.NvssdkLibrary.PARACHANGE_NOTIFY_V4;
import com.nvs.sdk.NvssdkLibrary.PROXY_NOTIFY;

public class JavaClientDemo {	
	// Global device list, used to manage logged in devices
	static ArrayList<Device> m_Devices = new ArrayList();
	
	// The current device selected in the program to perform the operation
	static Device m_Device = null;
	static boolean m_bQuit = false; //Quit
	
	static Boolean IsDevExist(String strIp)
	{
		for (Device aDev : m_Devices)
		{
			if (aDev.GetIP().equals(strIp))
			{
				return true;
			}
		}
		return false;
	}
	static void AddDevice(Device aDev)
	{
		m_Devices.add(aDev);
	}
	
	public Device GetDevice(int _iLogonID)
	{
		for (Device aDev : m_Devices)
		{
			if (_iLogonID == aDev.GetLogonID())
			{
				return aDev;
			}
		}
		return null;
	}
	
	// JavaClientDemo architecture
	abstract class Operation {
		public Operation(JavaClientDemo _instance, String _description) {
			m_this = _instance;
			m_description = _description;
		}
		public JavaClientDemo m_this = null;
		public String m_description = null;
		public abstract void Operate();
	};
	// Action selection
	Map<Integer, Operation> m_mapOptCmd = new HashMap<Integer, Operation>();
	
	////////////////////////////////// global callback function, a program should have one
	MAIN_NOTIFY_V4 cbkMain = new MAIN_NOTIFY_V4() {
		public void apply(int iLogonID, NativeLong wParam, Pointer lParam, Pointer noitfyUserData) {
			int iMsgType = wParam.intValue() & 0xFFFF;
			switch (iMsgType) {
			case NvssdkLibrary.WCM_LOGON_NOTIFY: { //Login callback notification
				try {
					// Other operations can only be performed after the device is successfully logged in
					LogonNotify(iLogonID);
				} catch(Exception e) {
					e.printStackTrace();
				}
				break;
			} 
			case NvssdkLibrary.WCM_QUERYFILE_FINISHED:
			case NvssdkLibrary.WCM_DWONLOAD_FINISHED:
			case NvssdkLibrary.WCM_DWONLOAD_FAULT:
			case NvssdkLibrary.WCM_DOWNLOAD_INTERRUPT: {
				Device aDev = GetDevice(iLogonID);
				if (null != aDev)
				{
					aDev.MainNotify(iLogonID, wParam.intValue(), lParam, noitfyUserData);
				}
				break;
			} default: break; 
			}
		}
	};
	
	public void LogonNotify(int iLogonID) {
		Device aDev = GetDevice(iLogonID);
		if (null == aDev)
		{
			return;
		}
		int iLogonState = aDev.GetLogonStatus();
		String strMsg = null;
		switch (iLogonState) {
		case NvssdkLibrary.LOGON_SUCCESS: {
			strMsg = "LOGON_SUCCESS";
			break;
		} case NvssdkLibrary.LOGON_FAILED: {
			strMsg = "LOGON_FAILED";
			break;
		} case NvssdkLibrary.LOGON_TIMEOUT: {
			strMsg = "LOGON_TIMEOUT";
			break;
		} case NvssdkLibrary.LOGON_RETRY: {
			strMsg = "LOGON_RETRY";
			break;
		} case NvssdkLibrary.LOGON_ING: {
			strMsg = "LOGON_ING";
			break;
		} default: {
			System.out.println("[WCM_LOGON_NOTIFY][" + iLogonState + "] IP("
					+ aDev.GetIP() + "),ID(" + aDev.GetID() + "),LogonID(" + iLogonID + ")");
			break;
		}
		}
		System.out.println("[WCM_LOGON_NOTIFY][" + strMsg + "] IP(" + aDev.GetIP()
				+ "),ID(" +  aDev.GetID() + "),LogonID(" + iLogonID + ")");
		
		//If the login is unsuccessful, delete it from m_Devices
		if (iLogonState != NvssdkLibrary.LOGON_SUCCESS)
		{
			m_Devices.remove(aDev);
		}
		
	};
	
	
	ALARM_NOTIFY_V4 cbkAlarm = new ALARM_NOTIFY_V4() {
		public void apply(int _ulLogonID, int _iChan, int _iAlarmState, int _iAlarmType, Pointer _iUser) {
		}
	};
	
	PARACHANGE_NOTIFY_V4 cbkParaChange = new PARACHANGE_NOTIFY_V4() {
		public void apply(int _ulLogonID, int _iChan, int _iParaType, STR_Para _strPara, Pointer _iUser) {
		}
	};
	
	COMRECV_NOTIFY_V4 comNotify = new COMRECV_NOTIFY_V4() {
		public void apply(int _ulLogonID, Pointer _cData, int _iLen, int _iComNo, Pointer _iUser)
		{
			
		}
	};
	
	PROXY_NOTIFY proxyNotify = new PROXY_NOTIFY() {
		public void apply(int _ulLogonID, int _iCmdKey, Pointer _cData, int _iLen, Pointer _iUser)
		{
			
		}
	};
	
	//SDK initialization operation
	private void sdkInit() {
		SDK_VERSION ver = new SDK_VERSION();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_GetVersion(ver);
		ver.read();
		if (null != ver.m_cVerInfo)
		{
			System.out.println("[SDK_VERSION]" + ver.m_cVerInfo.getString(0));
		}

		//Global callback function, it should only be registered once in a program
		iRet = NvssdkLibrary.INSTANCE.NetClient_SetNotifyFunction_V4(cbkMain, cbkAlarm, cbkParaChange, comNotify, proxyNotify);
		System.out.println("SetNotifyFunction(" + iRet + ")");

		iRet = NvssdkLibrary.INSTANCE.NetClient_Startup_V4(0, 0, 0);
		System.out.println("Startup(" + iRet + ")");
	};
	
	//SDK deinitialization operation
	private void sdkUnInit() {
		int iRet = NvssdkLibrary.INSTANCE.NetClient_Cleanup();
		System.out.println("Cleanup(" + iRet + ")");
	};
	///////////////////////////////////////////// ///////////////// global function
	
	// main function
	public static void main(String args[])
	{
		System.out.println("JavaClientDemo!");
		JavaClientDemo tClient = new JavaClientDemo();
		
		//Initialize the SDK
		tClient.sdkInit(); 
		
		//Call the SDK interface to implement business functions
		tClient.makeOptCmd();
		tClient.doMajorWork();
		
		//Deinitialize the SDK and clean up the SDK resources
		tClient.sdkUnInit(); 
	}	
	
	//Demo loop interactive operation
	public void doMajorWork()
	{
		Scanner scanInput = new Scanner(System.in);
		// Initialize display device list page (main page)
		m_mapOptCmd.get(-2).Operate();
		while(!m_bQuit)
		{
			System.out.println("Please input your correct command: ");
			try
			{
				int iCmd = scanInput.nextInt();
				if (m_mapOptCmd.containsKey(iCmd))
				{
					m_mapOptCmd.get(iCmd).Operate();
				}
			}
			catch(Exception e)
			{
				scanInput.skip(".*");
				e.printStackTrace();
				
			}
		}
	}
	
	// Demo constructs a list of operations
	public void makeOptCmd()
	{  
		//Home navigation page, used to display the device management list and its operations
		MainPageNavigation aPage = MainPageNavigation.getInstance(this);

		//Subpage navigation page, used to display device information, connected channel list, etc.
		SubPageNavigation aPage1 = SubPageNavigation.getInstance(this);
		
		//Preview function navigation
		PreviewNavigation aInstance = PreviewNavigation.getInstance(this);
		
		// playback function navigation
		// Play back the download page
		PlaybackNavigation aPage2 = PlaybackNavigation.getInstance(this);
		
		// Image stream download page
		PictureStreamNavigation aPage3 = PictureStreamNavigation.getInstance(this);
		
	}
	
}

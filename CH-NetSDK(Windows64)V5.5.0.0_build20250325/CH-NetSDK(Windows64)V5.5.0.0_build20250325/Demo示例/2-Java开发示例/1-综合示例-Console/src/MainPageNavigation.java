package src;

import java.util.Map;
import java.util.Scanner;


import src.JavaClientDemo.Operation;
import com.nvs.sdk.*;
import com.nvs.*;

public class MainPageNavigation {
	private static MainPageNavigation instance = null;
	public static JavaClientDemo m_demo = null;
	
	public static MainPageNavigation getInstance(JavaClientDemo _demo) {
		m_demo = _demo;
		if (null == instance)
		{
			instance = new MainPageNavigation();
		}
		return instance;
	}
	
	//Operation ID cannot be repeated
	private MainPageNavigation() 
	{
		// Navigation page index is negative
		m_demo.m_mapOptCmd.put(-2, m_demo.new Operation(m_demo, "MAIN Page") {
			public void Operate() {		
				System.out.println("***************************MAIN PAGE********************************");
				String strOperation = new String();
	
				for (int idx = -2; idx < 10; idx++)
				{
					if (!m_this.m_mapOptCmd.containsKey(idx))
					{
						continue;
					}
					strOperation = String.format("[%d]%s   ", idx, m_this.m_mapOptCmd.get(idx).m_description);
					System.out.print(strOperation);
					if (0 == idx%5)
					{
						System.out.println("");
					}	
				}
				System.out.println("\n***********************************************************");
			}
		});
				
		// Operations for device list page
		m_demo.m_mapOptCmd.put(-1, m_demo.new Operation(m_demo, "quit") {
			public void Operate() {
				System.out.println("Goodbye, my friend!");
				this.m_this.m_bQuit = true;
			}
		});
		
		m_demo.m_mapOptCmd.put(0, m_demo.new Operation(m_demo, "Device List") {
			public void Operate() {
				//[longid]ip
				System.out.println("Device List(format:[logonid]ip, [2]Current Device, [1]switch device, [-2]Main Page):");
				int idx = 1;
				for (Device aDev : m_this.m_Devices)
				{
					String strDev = String.format("[%d]%s   ", aDev.GetLogonID(), aDev.GetIP());
					System.out.print(strDev);
					if (0 == idx%3)
					{
						System.out.println("");
					}
				}
				System.out.println("");
				m_demo.m_mapOptCmd.get(-2).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(1, m_demo.new Operation(m_demo, "Switch Device") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the Switch device ID[x](-1 quit):");
				int iLogonID = scanInput.nextInt();
				
				Device aDev = m_this.GetDevice(iLogonID);
				if (null != aDev)
				{
					m_this.m_Device = aDev;	
				}
				if (null != m_this.m_Device)
				{
					System.err.println("Switch Device LogonId!(" + m_this.m_Device.GetLogonID() + ")");
				}
				System.out.println("");
				m_demo.m_mapOptCmd.get(-2).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(2, m_demo.new Operation(m_demo, "Current Device") {
			public void Operate() {
				//Display the current device IP, ID
				if (null == m_this.m_Device)
				{
					System.out.println("Current Device is Null, Please Switch to Device");
				}
				else
				{
					System.out.println("IP(" + m_this.m_Device.GetIP()	+ "),ID(" +  m_this.m_Device.GetID() + "),LogonID(" 
							+ m_this.m_Device.GetLogonID() + "), LogonStatus(" + m_this.m_Device.GetLogonStatus() + ")");
					System.out.println("Current Channel Count:" + m_this.m_Device.GetChannelCount());
					m_this.m_mapOptCmd.get(-3).Operate();
				}
			}
		});
		
//		m_demo.m_mapOptCmd.put(3, m_demo.new Operation(m_demo, "Logon") {
//			public void Operate() {
//				Scanner scanInput = new Scanner(System.in);
//				System.out.println("Please enter the device IP:");
//				String strDevIP = scanInput.next();
//				if (m_this.IsDevExist(strDevIP))
//				{
//					System.out.println("current device exist!!!");
//					return;
//				}
//				System.out.println("Please enter the device port:");
//				int iDevPort = scanInput.nextInt();
//				System.out.println("Please enter user name:");
//				String strUserName = scanInput.next();
//				System.out.println("Please enter your password:");
//				String strUserPwd = scanInput.next();
//				System.out.println("Logon" + strDevIP + ":" + iDevPort + "-" +strUserName + "-" + strUserPwd);
//				
//				//Construct login parameters
//				LogonPara tLogonPara = new LogonPara();
//				tLogonPara.iSize = tLogonPara.size();
//				tLogonPara.cNvsIP = strDevIP.getBytes();
//				tLogonPara.iNvsPort = iDevPort;
//				tLogonPara.cUserName = strUserName.getBytes();
//				tLogonPara.cUserPwd = strUserPwd.getBytes();
//				tLogonPara.write();
//				
//				Device aDevice = new Device();
//				int iLogonID = aDevice.Logon(tLogonPara);
//				if (iLogonID < 0) 
//				{
//					System.err.println("Logon fail!(" + iLogonID + ")");
//				}
//				else
//				{
//					//Add to the device list for centralized management; asynchronous login needs to obtain the login success message in the login notification of the underlying callback
//					m_this.AddDevice(aDevice);
//				}
//				
//				//The SDK login device is in asynchronous working mode and needs to wait for the login to succeed in the main callback
//			}
//		});
		
		m_demo.m_mapOptCmd.put(4, m_demo.new Operation(m_demo, "Logoff") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the device ID[x](-1 quit):");
				int iLogonID = scanInput.nextInt();
				
				Device aDev = m_this.GetDevice(iLogonID);
				if (null != aDev)
				{
					aDev.LogOff();
					m_this.m_Devices.remove(aDev);
				}	
				System.out.println("");
				m_demo.m_mapOptCmd.get(-2).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(5, m_demo.new Operation(m_demo, "SyncLogon") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.print("Please input LogonType(0--Normal  1--Active): ");
				int iLogonType = scanInput.nextInt();
				int iRet = NvssdkLibrary.RET_FAILED;
				int iLogonID = -1;
				if (NvssdkLibrary.SERVER_ACTIVE == iLogonType)
				{
					//Active mode login logic: the client and the device are not in the same subnet, and need to use the public network to penetrate
					System.out.print("Please input local listen port:");
					//Set the local listening internal port
					int iLocalListenPort = scanInput.nextInt();
					
					System.out.print("Please input local wan ip:");
					String strPublicIp = scanInput.next();
					//Set the local listening external port (router mapping port)
					System.out.print("Please input local wan port:");
					int iWanPort = scanInput.nextInt();
					System.out.print("Please enter the factory ID:");
					String strProductID = scanInput.next();
					System.out.print("Please input UserName: ");
					String strUserName = scanInput.next();
					System.out.print("Please input Password: ");
					String strUserPwd = scanInput.next();
					
					//Active mode synchronous blocking login device
					iLogonID = Device.SyncLogon(iLogonType, strPublicIp, iWanPort, strUserName, strUserPwd, strProductID, iLocalListenPort);
				}
				else
				{
					System.out.println("Please enter the device IP:");
					String strDevIP = scanInput.next();
					if (m_this.IsDevExist(strDevIP))
					{
						System.out.println("current device exist!!!");
						return;
					}
					System.out.println("Please enter the device port:");
					int iDevPort = scanInput.nextInt();
					System.out.println("Please enter user name:");
					String strUserName = scanInput.next();
					System.out.println("Please enter your password:");
					String strUserPwd = scanInput.next();
					String strCharSet = "UTF-8";
					System.out.println("Logon:" + strDevIP + ", " + iDevPort + ", " +strUserName + ", " + strUserPwd);
					
					//Regular mode synchronously blocks the login device
					iLogonID = Device.SyncLogon(iLogonType, strDevIP, iDevPort, strUserName, strUserPwd, "", -1);
				}
				
				if (iLogonID >= 0)
				{
					Device aDev = new Device();
					aDev.SetLogonID(iLogonID);
					m_this.AddDevice(aDev);
				}
				System.out.println("");
				m_demo.m_mapOptCmd.get(-2).Operate();
			}
		});
		
	}

}

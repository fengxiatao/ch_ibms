package src;

import java.util.Scanner;

import src.JavaClientDemo.Operation;
import src.PreviewNavigation;

public class SubPageNavigation {
	private static SubPageNavigation instance = null;
	public static JavaClientDemo m_demo = null;
	
	public static SubPageNavigation getInstance(JavaClientDemo _demo) {
		m_demo = _demo;
		if (null == instance)
		{
			instance = new SubPageNavigation();
		}
		return instance;
	}
	
	//Action ID cannot be repeated
	private SubPageNavigation()
	{
		// Navigation page index is negative
		m_demo.m_mapOptCmd.put(-4, m_demo.new Operation(m_demo, "Preview API") {
			public void Operate() {	
				System.out.println("[10]MainPage [-1]quit [-3]SubPage");
				System.out.println("****************************preview(20<=id<100)*******************************");
				String strOperation = new String();
				for (int idx = 20; idx < 30; idx++)
				{
					if (!m_demo.m_mapOptCmd.containsKey(idx))
					{
						continue;
					}
					strOperation = String.format("[%d]%s   ", idx, m_this.m_mapOptCmd.get(idx).m_description);
					System.out.print(strOperation);
					if (idx > 20 && 0 == idx%5)
					{
						System.out.println("");
					}	
				}
				System.out.println("");
			}
		});
		
		m_demo.m_mapOptCmd.put(-5, m_demo.new Operation(m_demo, "Playback API") {
			public void Operate() {	
				String strOperation = new String();
				System.out.println("[10]MainPage [-1]quit [-3]SubPage");
				System.out.println("*************************playback(100<=id<200)**********************************");
				for (int idx = 100; idx < 200; idx++)
				{
					if (!m_demo.m_mapOptCmd.containsKey(idx))
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
				System.out.println("");
			}
		});
		
		m_demo.m_mapOptCmd.put(-6, m_demo.new Operation(m_demo, "Download API") {
			public void Operate() {	
				String strOperation = new String();
				System.out.println("[10]MainPage [-1]quit [-3]SubPage");
				System.out.println("*************************download(200<=id<300)**********************************");
				for (int idx = 200; idx < 300; idx++)
				{
					if (!m_demo.m_mapOptCmd.containsKey(idx))
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
				System.out.println("");
			}
		});
		m_demo.m_mapOptCmd.put(-7, m_demo.new Operation(m_demo, "Picture Stream API") {
			public void Operate() {	
				String strOperation = new String();
				System.out.println("[10]MainPage [-1]quit [-3]SubPage");
				System.out.println("*************************image stream(300<=id<400)**********************************");
				for (int idx = 300; idx < 400; idx++)
				{
					if (!m_demo.m_mapOptCmd.containsKey(idx))
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
				System.out.println("");
				//If there are new business modules, you can add them later. . . . . . . . . . . .
			}
		});
		
		
		// Navigation page index is negative
		m_demo.m_mapOptCmd.put(-3, m_demo.new Operation(m_demo, "Sub Page") {
			public void Operate() {	
				System.out.println("*************************Sub Page**********************************");
				String strOperation = new String();
				for (int idx = 10; idx < 20; idx++)
				{
					if (!m_this.m_mapOptCmd.containsKey(idx))
					{
						continue;
					}
					strOperation = String.format("[%d]%s   ", idx, m_this.m_mapOptCmd.get(idx).m_description);
					System.out.print(strOperation);
					if (idx > 10 && 0 == idx%5)
					{
						System.out.println("");
					}	
				}
				System.out.println("\n***********************************************************");
			}
		});
		
		// Device operation page, acting on the current device;
		//Back to Home Navigation Page
		m_demo.m_mapOptCmd.put(10, m_demo.new Operation(m_demo, "Main Page") {
			public void Operate() {
				m_this.m_mapOptCmd.get(-2).Operate();
			}
		});
		
		//
		m_demo.m_mapOptCmd.put(11, m_demo.new Operation(m_demo, "Preview API") {
			public void Operate() {
				if (null == m_this.m_Device)
				{
					System.out.println("current device is null");
				}
				else
				{
					m_this.m_Device.PrintVideoChannels();
					m_this.m_mapOptCmd.get(-4).Operate();
				}
			}
		});
		
		//
		m_demo.m_mapOptCmd.put(12, m_demo.new Operation(m_demo, "Playback API") {
			public void Operate() {
				if (null == m_this.m_Device)
				{
					System.out.println("current device is null");
				}
				else
				{
					m_this.m_Device.PrintPlaybackChannels();
					m_this.m_mapOptCmd.get(-5).Operate();
				}
			}
		});
		
		m_demo.m_mapOptCmd.put(13, m_demo.new Operation(m_demo, "Download API") {
			public void Operate() {
				if (null == m_this.m_Device)
				{
					System.out.println("current device is null");
				}
				else
				{
					m_this.m_Device.PrintDownloadChannels();
					m_this.m_mapOptCmd.get(-6).Operate();
				}
			}
		});
		
		m_demo.m_mapOptCmd.put(14, m_demo.new Operation(m_demo, "Picture Stream API") {
			public void Operate() {
				if (null == m_this.m_Device)
				{
					System.out.println("current device is null");
				}
				else
				{
					m_this.m_Device.PrintPictureStreamChannels();
					m_this.m_mapOptCmd.get(-7).Operate();
				}
			}
		});
		
		m_demo.m_mapOptCmd.put(15, m_demo.new Operation(m_demo, "PTZCtrl") {
			public void Operate() {
			    Scanner scanInput = new Scanner(System.in);
			    System.out.println("Please enter the channel number (starting at 0):");
			    int iChannel = scanInput.nextInt();

			    System.out.println("Please input speed[0-100]:");
			    int iSpeed = scanInput.nextInt();
			    if(-1 == iSpeed)
			    {
			        return;
			    }
			    
				System.out.println("please input preset number:");
				int iPresetNum = scanInput.nextInt();
				
			    System.out.print("up: 1\n" + 
			            "down: 3\n" + 
			            "left: 5\n" + 
			            "right: 7\n" + 
			            "up left: 9\n" + 
			            "up right: 11\n" + 
			            "down left: 13\n" +
			            "down right: 15\n" +
			            "stop: 61\n" +
						"zoom in: 31\n" +
						"zoom in stop: 32\n" +
						"zoom out: 33\n" +
						"zoom out stop: 34\n" +
						"focus far: 35\n" +
						"focus far stop: 36\n" +
						"focus near: 37\n" +
						"focus near stop: 38\n" +
						"iris open: 39\n" +
						"iris open stop: 40\n" +
						"iris close: 41\n" +
						"iris close stop: 42\n" +
			            "light on: 43\n" +
			            "light off: 44\n" +
			            "power on: 45\n" +
			            "power off: 46\n" +
			            "rain on: 47\n" +
			            "rain off: 48\n" +
						"call preset: 62\n" +
						"set preset: 63\n" +
						"quit:-1\n" + 
			            "Please input action:");

			    int iAction = scanInput.nextInt();
			    if(-1 == iAction)
			    {
			        return;
			    }
			    
			    int iRet = m_demo.m_Device.PTZCtrl(iChannel, iAction, iSpeed, iPresetNum);
			    if(0 == iRet)
			    {
			    	System.out.println("DevicePTZCtrl is OK!\n");
			    }
			    else
			    {
			    	System.out.println("DevicePTZCtrl is failed!\n");
			    }
			}
		});
		
		m_demo.m_mapOptCmd.put(16, m_demo.new Operation(m_demo, "fun3DLocate") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the video channel number (starting at 0):");
				int _iChannelNo = scanInput.nextInt();
				
				//(x1,y1,x2,y2)----(left,right,top,bottom)
				System.out.println("Please enter the abscissa of the top left corner (parts ratio):");
				int _iLeft = scanInput.nextInt();
				System.out.println("Please enter the ordinate of the upper left corner (parts ratio):");
				int _iRight = scanInput.nextInt();
				System.out.println("Please enter the abscissa of the lower right corner (parts ratio):");
				int _iTop= scanInput.nextInt();
				System.out.println("Please enter the ordinate of the lower right corner (parts ratio):");
				int _iBottom = scanInput.nextInt();

				m_demo.m_Device.Fun3DLocate(_iChannelNo, _iLeft, _iRight, _iTop, _iBottom);
			}
		});
			
	}
	
}

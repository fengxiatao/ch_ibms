package src;

import java.util.Scanner;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import src.JavaClientDemo.Operation;
import com.nvs.*;
import com.nvs.sdk.*;

public class PreviewNavigation {
	private static PreviewNavigation instance = null;
	public static JavaClientDemo m_demo = null;
	
	public static PreviewNavigation getInstance(JavaClientDemo _demo) {
		m_demo = _demo;
		if (null == instance)
		{
			instance = new PreviewNavigation();
		}
		return instance;
	}
	
	public int m_channelIndex = -1;
	
	//Operation ID cannot be repeated, ID range 20 <= id < 100
	private PreviewNavigation()
	{		
		m_demo.m_mapOptCmd.put(20, m_demo.new Operation(m_demo, "Select Channel") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the video channel index!!!!! (starting at 0):");
				int iIndex = scanInput.nextInt();
				
				Channel aChan = m_demo.m_Device.GetVideoChannel(iIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				m_channelIndex = iIndex;
				m_demo.m_mapOptCmd.get(21).Operate();

			}
		});
		
		
		m_demo.m_mapOptCmd.put(21, m_demo.new Operation(m_demo, "Current Channel") {
			public void Operate() {
				Channel aChan = m_demo.m_Device.GetVideoChannel(m_channelIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				System.out.println("current channelNo:" + aChan.m_iChannelNo + ",streamNo:" + aChan.m_iStreamNo);
				String strOperation = new String();
				for (int idx = 30; idx < 100; idx++)
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
		
		//channel navigation
//		m_demo.m_mapOptCmd.put(22, m_demo.new Operation(m_demo, "Start Recvive Video") {
//			public void Operate() {
//				Scanner scanInput = new Scanner(System.in);
//				System.out.println("Please enter the video channel number (starting at 0):");
//				int iChannelNo = scanInput.nextInt();
//				System.out.println("Please enter the stream type (0-main stream, 1-sub stream, 254-three stream):");
//				int iStreamNo = scanInput.nextInt();
//				m_demo.m_Device.StartRecvVideo(iChannelNo, iStreamNo);
//			}
//		});
//
//		m_demo.m_mapOptCmd.put(23, m_demo.new Operation(m_demo, "Close Receive Video") {
//			public void Operate() {
//				Scanner scanInput = new Scanner(System.in);
//				System.out.println("Please enter the video channel number (starting at 0):");
//				int iChannelNo = scanInput.nextInt();
//				System.out.println("Please enter the stream type (0-main stream, 1-sub stream, 254-three stream):");
//				int iStreamNo = scanInput.nextInt();
//				m_demo.m_Device.StopRecvVideo(iChannelNo, iStreamNo);
//
//			}
//		});
		
		m_demo.m_mapOptCmd.put(24, m_demo.new Operation(m_demo, "SyncRealPlay") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the video channel number (starting at 0):");
				int iChannelNo = scanInput.nextInt();
				System.out.println("Please enter the stream type (0-main stream, 1-sub stream, 254-three stream):");
				int iStreamNo = scanInput.nextInt();
				m_demo.m_Device.SyncRealPlay(iChannelNo, iStreamNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();

			}
		});
		
		m_demo.m_mapOptCmd.put(25, m_demo.new Operation(m_demo, "StopRealPlay") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the video channel number (starting at 0):");
				int iChannelNo = scanInput.nextInt();
				System.out.println("Please enter the stream type (0-main stream, 1-sub stream, 254-three stream):");
				int iStreamNo = scanInput.nextInt();
				m_demo.m_Device.StopRealPlay(iChannelNo, iStreamNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();
			}
		});
		

		
		
		//// Device Capabilities API Page
		m_demo.m_mapOptCmd.put(32, m_demo.new Operation(m_demo, "StartRawVideoDataCallback") {
			public void Operate() {
				Channel aChan = m_demo.m_Device.GetVideoChannel(m_channelIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				
				m_demo.m_Device.StartRawFrameCallback(aChan.m_iChannelNo, aChan.m_iStreamNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(34, m_demo.new Operation(m_demo, "StartRecord") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				Channel aChan = m_demo.m_Device.GetVideoChannel(m_channelIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				System.out.println("Please enter the recording type (0-sdv, 8-ps[mp4], 9-ts):");
				int iRecFileType = scanInput.nextInt();	
				
				String strFileName = new String("myRecordFile");
				
				m_demo.m_Device.StartRecord(aChan.m_iChannelNo, aChan.m_iStreamNo, strFileName, iRecFileType);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(35, m_demo.new Operation(m_demo, "StopRecord") {
			public void Operate() {
				Channel aChan = m_demo.m_Device.GetVideoChannel(m_channelIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				m_demo.m_Device.StopRecord(aChan.m_iChannelNo, aChan.m_iStreamNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();

			}
		});
		

		
		m_demo.m_mapOptCmd.put(38, m_demo.new Operation(m_demo, "SnapShot") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				Channel aChan = m_demo.m_Device.GetVideoChannel(m_channelIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				System.out.print("Please enter the capture type (0-YUV, 1-BMP, 2-JPG):");
				int iType = scanInput.nextInt();
				m_demo.m_Device.SnapShot(aChan.m_iChannelNo, aChan.m_iStreamNo, iType);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(39, m_demo.new Operation(m_demo, "StartPSVideoCallback") {
			public void Operate() {
				Channel aChan = m_demo.m_Device.GetVideoChannel(m_channelIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				m_demo.m_Device.StartPsCallback(aChan.m_iChannelNo, aChan.m_iStreamNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(40, m_demo.new Operation(m_demo, "StopPSVideoCallback") {
			public void Operate() {
				Channel aChan = m_demo.m_Device.GetVideoChannel(m_channelIndex);
				if (null == aChan)
				{
					System.out.println("not a valid channel");
					return;
				}
				m_demo.m_Device.StopPsCallback(aChan.m_iChannelNo, aChan.m_iStreamNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(21).Operate();
			}
		});
		

		
	}
}

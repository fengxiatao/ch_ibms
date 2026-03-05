package src;

import java.io.File;
import java.util.Scanner;

import com.nvs.sdk.NvssdkLibrary;

import src.JavaClientDemo.Operation;

public class PictureStreamNavigation {
	private static PictureStreamNavigation instance = null;
	public static JavaClientDemo m_demo = null;
	
	public static PictureStreamNavigation getInstance(JavaClientDemo _demo) {
		m_demo = _demo;
		if (null == instance)
		{
			instance = new PictureStreamNavigation();
		}
		return instance;
	}
	
	//Operation ID cannot be repeated, 200 <= id < 300
	private PictureStreamNavigation()
	{			
		//// Device function API page Intelligent analysis image stream, traffic image stream, face image stream
		m_demo.m_mapOptCmd.put(300, m_demo.new Operation(m_demo, "StopPictureStream") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the picture stream channel number(starting at 0):");
				int iConnectID = scanInput.nextInt();
				m_demo.m_Device.StopRecvPicStream(iConnectID);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-7).Operate();
			}
			
		});
		
		m_demo.m_mapOptCmd.put(301, m_demo.new Operation(m_demo, "StartFaceSnap") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;
				System.out.println("Please enter the  channel number (starting at 0):");
				int iChannelNo = scanInput.nextInt();
				m_demo.m_Device.StartRecvSnapFacePicStream(iChannelNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-7).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(302, m_demo.new Operation(m_demo, "StartITSSnap") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;
				System.out.println("Please enter the  channel number (starting at 0):");
				int iChannelNo = scanInput.nextInt();
				m_demo.m_Device.StartRecvSnapITSPicStream(iChannelNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-7).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(303, m_demo.new Operation(m_demo, "StartVcaSnap") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;
				System.out.println("Please enter the  channel number (starting at 0):");
				int iChannelNo = scanInput.nextInt();
				m_demo.m_Device.StartRecvSnapVcaPicStream(iChannelNo);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-7).Operate();
			}
		});
	}
}
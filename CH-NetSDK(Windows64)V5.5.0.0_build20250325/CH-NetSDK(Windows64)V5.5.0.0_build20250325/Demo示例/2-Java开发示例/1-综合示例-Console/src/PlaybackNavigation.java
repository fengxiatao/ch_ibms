package src;

import java.util.ArrayList;
import java.util.Scanner;

import com.sun.jna.ptr.IntByReference;

import src.JavaClientDemo.Operation;
import com.nvs.*;
import com.nvs.sdk.*;

public class PlaybackNavigation {
	private static PlaybackNavigation instance = null;
	public static JavaClientDemo m_demo = null;
	
	public static PlaybackNavigation getInstance(JavaClientDemo _demo) {
		m_demo = _demo;
		if (null == instance)
		{
			instance = new PlaybackNavigation();
		}
		return instance;
	}
	
	//Operation ID cannot be repeated, ID range 100 <= id < 200
	private PlaybackNavigation()
	{
			
		//// Device Capabilities API Page
		m_demo.m_mapOptCmd.put(100, m_demo.new Operation(m_demo, "SyncQueryFile") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the  channel number (starting at 0,query channel no, 0x7FFFFFFF means query all channel):");
				int iChannelNo = scanInput.nextInt();
				System.out.println("Please enter the stream type (0-main stream, 1-sub stream, 254-three stream):");
				int iStreamNo = scanInput.nextInt();
				System.out.println("Please enter the startTime (example(Year Month Day Hour Minute Second):2019-10-31-0-0-0):");
				String strLine = scanInput.next();
				String[] strArr = strLine.split("-");
				
				//Users can modify the query time period by themselves to ensure that there are videos in the modified time period
				NETFILE_QUERY_V5 tQueryFileV5 = new NETFILE_QUERY_V5();
				tQueryFileV5.iBufSize = tQueryFileV5.size();
				tQueryFileV5.iQueryChannelNo = iChannelNo;	//query channel no, 0x7FFFFFFF means query all channel
				tQueryFileV5.iStreamNo = iStreamNo;			//Query stream number, 0-main stream, 1-sub stream
				tQueryFileV5.iType = 0xFF;					
				//query start time
				tQueryFileV5.tStartTime.iYear = Integer.valueOf(strArr[0]).shortValue();
				tQueryFileV5.tStartTime.iMonth = Integer.valueOf(strArr[1]).shortValue();
				tQueryFileV5.tStartTime.iDay = Integer.valueOf(strArr[2]).shortValue();
				tQueryFileV5.tStartTime.iHour = Integer.valueOf(strArr[3]).shortValue();
				tQueryFileV5.tStartTime.iMinute = Integer.valueOf(strArr[4]).shortValue();
				tQueryFileV5.tStartTime.iSecond = Integer.valueOf(strArr[5]).shortValue();
				//query end time
				System.out.println("Please enter the endTime (example:2019-10-31-23-59-0):");
				strLine = scanInput.next();
				strArr = strLine.split("-");
				tQueryFileV5.tStopTime.iYear = Integer.valueOf(strArr[0]).shortValue();
				tQueryFileV5.tStopTime.iMonth = Integer.valueOf(strArr[1]).shortValue();
				tQueryFileV5.tStopTime.iDay = Integer.valueOf(strArr[2]).shortValue();
				tQueryFileV5.tStopTime.iHour = Integer.valueOf(strArr[3]).shortValue();
				tQueryFileV5.tStopTime.iMinute = Integer.valueOf(strArr[4]).shortValue();
				tQueryFileV5.tStopTime.iSecond = Integer.valueOf(strArr[5]).shortValue();
				
				tQueryFileV5.iPageSize = 20;	//The page size of each query, that is, the number of records per query
				tQueryFileV5.iPageNo = 0;	//Query the page number, for example, there are 100 records in total, and the page size of each query is 20, then the page number is 0, 1, 2, 3, 4 in sequence
				tQueryFileV5.iFiletype = 1;	//File type 0:all,1:Video,2:picture
				tQueryFileV5.iDevType = 0xFF; //0----camera; 1----network video server; 2----network camera; 0xff----all.

				tQueryFileV5.iTriggerType = 0x7FFFFFFF;
				tQueryFileV5.iTrigger = 0;
				//The number of channels that can be queried by multiple channels: When QueryChannelCount is 0, query iQueryChannelNo and the video in iStreamNo, ptChannelList does not take effect.
				tQueryFileV5.iQueryChannelCount = 0;		//The number of channels for this query, some nvr supports batch query use, the default single-channel query is
			
				//Batch query channel array, query by single channel by default	
				tagQueryFileChannel[] array = tagQueryFileChannel.newArray(2);
				array[0].iChannelNo = 0;
				array[0].iStreamNo = 0;
				array[1].iChannelNo = 0;
				array[1].iStreamNo = 1;
				
				tQueryFileV5.ptChannelList = array[0].byReference();
				tQueryFileV5.iBufferSize = array[0].size();	//query structure size
				tQueryFileV5.write();

				NVS_FILE_DATA[] tResult = NVS_FILE_DATA.newArray(tQueryFileV5.iPageSize);
				
				int iRet = m_demo.m_Device.SyncQueryFile(tQueryFileV5, tResult);
				if (iRet < 0)
				{
					System.out.println("Err: NetClient_Query_V5");
					return;
				}

				tQueryFileV5.read();

				System.out.println("TotalCount=" + tQueryFileV5.iTotalQueryCount + ", CurrentCount=" + tQueryFileV5.iCurQueryCount);
				for(int i = 0; i < tQueryFileV5.iCurQueryCount; ++i)
				{      
					tResult[i].read();
		 			String strFileName = new String(tResult[i].cFileName).trim();	
		 			int iStartYear = tResult[i].struStartTime.iYear;
		 			int iStartMonth = tResult[i].struStartTime.iMonth;
		 			int iStartDay = tResult[i].struStartTime.iDay;
		 			int iStartHour = tResult[i].struStartTime.iHour;
		 			int iStartMinute = tResult[i].struStartTime.iMinute;
		 			int iStartSecond = tResult[i].struStartTime.iSecond;
		 			int iStopYear = tResult[i].struStoptime.iYear;
		 			int iStopMonth = tResult[i].struStoptime.iMonth;
		 			int iStopDay = tResult[i].struStoptime.iDay;
		 			int iStopHour = tResult[i].struStoptime.iHour;
		 			int iStopMinute = tResult[i].struStoptime.iMinute;
		 			int iStopSecond = tResult[i].struStoptime.iSecond;
		 			System.out.println("fileIdx=" + i + ", fileName=" + strFileName
		 					+ ", chNo=" + tResult[i].iChannel + ", fileSize=" + tResult[i].iFileSize
		 					+ ", fileStartTime=" + iStartYear + "-" + iStartMonth + "-" + iStartDay + "-"
		 					+ iStartHour + "-" + iStartMinute + "-" + iStartSecond
		 					+ ", fileStopTime=" + iStopYear + "-" + iStopMonth + "-" + iStopDay + "-"
		 					+ iStopHour + "-" + iStopMinute + "-" + iStopSecond);
				}
				System.out.println("");
				m_demo.m_mapOptCmd.get(-5).Operate();

			}
			
		});
		
		m_demo.m_mapOptCmd.put(101, m_demo.new Operation(m_demo, "PlayBackByFileMode") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;
				System.out.println("Please enter the name of the file you want to playback: ");
				String strPlaybackFileName = scanInput.next();
				iRet = m_demo.m_Device.StartPlayback(strPlaybackFileName);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-5).Operate();
			}
		});
		
		//// Device Capabilities API Page
		m_demo.m_mapOptCmd.put(102, m_demo.new Operation(m_demo, "PlaybackByTimespanMode") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;
				System.out.println("Please enter the  channel number (starting at 0):");
				int iChannelNo = scanInput.nextInt();

				System.out.println("Please enter the startTime (example:2019-10-31-0-0-0):");
				NVS_FILE_TIME tBegin = new NVS_FILE_TIME();
				String strLine = scanInput.next();
				String[] strArr = strLine.split("-");
				
				tBegin.iYear = Integer.valueOf(strArr[0]).shortValue();
				tBegin.iMonth = Integer.valueOf(strArr[1]).shortValue();
				tBegin.iDay = Integer.valueOf(strArr[2]).shortValue();
				tBegin.iHour = Integer.valueOf(strArr[3]).shortValue();
				tBegin.iMinute = Integer.valueOf(strArr[4]).shortValue();
				tBegin.iSecond = Integer.valueOf(strArr[5]).shortValue();
				tBegin.write();
				
				System.out.println("Please enter the endTime (example:2019-10-31-23-59-0):");
				NVS_FILE_TIME tEnd = new NVS_FILE_TIME();
				strLine = scanInput.next();
				strArr = strLine.split("-");
				
				tEnd.iYear = Integer.valueOf(strArr[0]).shortValue();
				tEnd.iMonth = Integer.valueOf(strArr[1]).shortValue();
				tEnd.iDay = Integer.valueOf(strArr[2]).shortValue();
				tEnd.iHour = Integer.valueOf(strArr[3]).shortValue();
				tEnd.iMinute = Integer.valueOf(strArr[4]).shortValue();
				tEnd.iSecond = Integer.valueOf(strArr[5]).shortValue();
				tEnd.write();
				iRet = m_demo.m_Device.StartPlayback(iChannelNo,tBegin, tEnd);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-5).Operate();
			}
		});
		
		//// Device Capabilities API Page
		m_demo.m_mapOptCmd.put(103, m_demo.new Operation(m_demo, "StopPlayback") {
			public void Operate() {
				int iRet = NvssdkLibrary.RET_FAILED;
				Scanner scanInput = new Scanner(System.in);
				System.out.println("Please enter the playback connect ID:");
				int iConnectID = scanInput.nextInt();
				iRet = m_demo.m_Device.StopPlayback(iConnectID);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-5).Operate();
			}
		});
		
		
		m_demo.m_mapOptCmd.put(200, m_demo.new Operation(m_demo, "SyncQueryFile") {
			public void Operate() {
				m_demo.m_mapOptCmd.get(100).Operate();
			}
		});
		
		m_demo.m_mapOptCmd.put(201, m_demo.new Operation(m_demo, "DownloadByFileMode") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;

				System.out.println("Please enter the type of the file you want to download:(0-sdv) ");
				int iSaveFileType = scanInput.nextInt();
				System.out.println("Please enter the name of the file you want to download: ");
				String strDownloadFileName = scanInput.next();

				iRet = m_demo.m_Device.DownloadByFileMode(iSaveFileType, strDownloadFileName, strDownloadFileName);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-6).Operate();
			}
		});
		
		//// Device Capabilities API Page
		m_demo.m_mapOptCmd.put(202, m_demo.new Operation(m_demo, "DownloadByTimespanMode") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;
				System.out.println("Please enter the  channel number (starting at 0,query channel no, 0x7FFFFFFF means query all channel):");
				int iChannelNo = scanInput.nextInt();
				System.out.println("Please enter the stream type (0-main stream, 1-sub stream, 254-three stream):");
				int iStreamNo = scanInput.nextInt();
				System.out.println("Please enter the type of the file you want to download:(0-sdv) ");
				int iSaveFileType = scanInput.nextInt();
				System.out.println("Please enter the name of the file you want to save: ");
				String strDownloadFileName = scanInput.next();
				System.out.println("Please enter the startTime (example:2019-10-31-0-0-0):");
				NVS_FILE_TIME tBegin = new NVS_FILE_TIME();
				String strLine = scanInput.next();
				String[] strArr = strLine.split("-");
				
				tBegin.iYear = Integer.valueOf(strArr[0]).shortValue();
				tBegin.iMonth = Integer.valueOf(strArr[1]).shortValue();
				tBegin.iDay = Integer.valueOf(strArr[2]).shortValue();
				tBegin.iHour = Integer.valueOf(strArr[3]).shortValue();
				tBegin.iMinute = Integer.valueOf(strArr[4]).shortValue();
				tBegin.iSecond = Integer.valueOf(strArr[5]).shortValue();
				
				System.out.println("Please enter the endTime (example:2019-10-31-23-59-0):");
				NVS_FILE_TIME tEnd = new NVS_FILE_TIME();
				strLine = scanInput.next();
				strArr = strLine.split("-");
				tEnd.iYear = Integer.valueOf(strArr[0]).shortValue();
				tEnd.iMonth = Integer.valueOf(strArr[1]).shortValue();
				tEnd.iDay = Integer.valueOf(strArr[2]).shortValue();
				tEnd.iHour = Integer.valueOf(strArr[3]).shortValue();
				tEnd.iMinute = Integer.valueOf(strArr[4]).shortValue();
				tEnd.iSecond = Integer.valueOf(strArr[5]).shortValue();
				iRet = m_demo.m_Device.DownloadByTimespanMode(iChannelNo, iStreamNo, iSaveFileType,  tBegin,  tEnd, strDownloadFileName);
				System.out.println("");
				m_demo.m_mapOptCmd.get(-6).Operate();
			}
		});
		
		//// Device Capabilities API Page
		m_demo.m_mapOptCmd.put(203, m_demo.new Operation(m_demo, "stopDownload") {
			public void Operate() {
				Scanner scanInput = new Scanner(System.in);
				int iRet = NvssdkLibrary.RET_FAILED;
				System.out.println("Please enter the download connect ID:");
				int iConnectID = scanInput.nextInt();
				iRet = m_demo.m_Device.StopDownload(iConnectID);
				
				System.out.println("");
				m_demo.m_mapOptCmd.get(-6).Operate();
			}
		});
		
	}
}

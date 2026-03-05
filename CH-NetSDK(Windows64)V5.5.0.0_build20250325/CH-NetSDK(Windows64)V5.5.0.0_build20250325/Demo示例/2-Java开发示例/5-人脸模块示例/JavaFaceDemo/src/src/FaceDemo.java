package src;

import com.nvs.*;
import com.nvs.sdk.*;
import com.nvs.sdk.NvssdkLibrary.MAIN_NOTIFY_V4;
import com.nvs.sdk.NvssdkLibrary.NET_PICSTREAM_NOTIFY;
import com.nvs.sdk.NvssdkLibrary.PARACHANGE_NOTIFY;
import com.nvs.sdk.NvssdkLibrary.PARACHANGE_NOTIFY_V4;
import com.nvs.sdk.tagQueryChanNo.ByReference;
import com.ochafik.lang.jnaerator.runtime.Structure;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.Memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class FaceDemo {
	
	int m_iLogonID = -1;
	int m_iConnectID = -1;
	int m_iLibKey = 0;		//保存最后一个库key值，便于修改和删除操作
	int m_iFaceKey = 0;		//保存最后一个人脸key值，便于修改和删除操作
	int m_iVcaStatus = 0;	//智能能分析状态
	int iChannelNum = 0; 
	
	
	public static final int VCA_SUSPEND_STATUS_PAUSE = 0;		//暂停智能分析
	public static final int VCA_SUSPEND_STATUS_RESUME = 1;	//恢复智能分析

	public static final int VCA_SUSPEND_RESULT_SUCCESS = 1;		//智能分析暂停成功
	public static final int VCA_SUSPEND_RESULT_CONFIGING = 2;		//智能分析暂停失败，正在设置，不可设参
	
	//人脸相关
	public static final int FACE_MAX_PAGE_COUNT = 20;
	public static final int FACE_MAX_LIB_COUNT = 33;		//人脸库最大个数
	
	
	Scanner scanIn = new Scanner(System.in);
	
	String m_strSavePath="PicStream";
	
	//byte[]转String,JAVA8可以使用BASE64直接转
	String ByteToStr(byte [] bt) {
		int len= bt.length;
		String str = new String();
		byte[] bLast = new byte[2];
		for(int i= 0; i < len; ++i) {
			if(0 == bt[i]) {
				break;
			} else if (bt[i] > 0){	//英文或者符号
				byte[] b = new byte[]{bt[i]}; 
				String s = new String(b);
				str += s;
			} else {		//中文,2字节为1个汉字
				if(0 != bLast[0]){
					bLast[1] = bt[i];
					String s = new String(bLast);
					str += s;
					bLast[0] = 0;
				} else {					
					bLast[0] = bt[i];
				}
			}			
		}
		return str;
	};
//NvssdkLibrary.INSTANCE.NetClient_StopRecvNetPicStream();
	
	//主回调
	NvssdkLibrary.MAIN_NOTIFY_V4 cbkMain = new NvssdkLibrary.MAIN_NOTIFY_V4() 
	{
		public void apply(int ulLogonID, NativeLong iWparam, Pointer iLParam,
				Pointer iUser) {
			int iMsgType = iWparam.intValue() & 0xFFFF;
			switch (iMsgType) {
			case NvssdkLibrary.WCM_LOGON_NOTIFY: {
				try {
					ENCODERINFO tDevInfo = new ENCODERINFO();
					NvssdkLibrary.INSTANCE.NetClient_GetDevInfo(ulLogonID, tDevInfo);					
					String strIP = new String(tDevInfo.m_cEncoder).trim();			
					String strID = new String(tDevInfo.m_cFactoryID).trim();	
					//处理设备登录状态
					LogonNotify(strIP, strID, ulLogonID, iWparam.intValue() >> 16);	
				} catch(Exception e) {
					e.printStackTrace();
				}
				break;
			} case NvssdkLibrary.WCM_VCA_SUSPEND: {
				tagVCASuspendResult tParam = new tagVCASuspendResult();
				tParam.iBufSize = tParam.size();
				tParam.write();
				IntBuffer pRet = IntBuffer.allocate(1);
				NvssdkLibrary.INSTANCE.NetClient_GetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_VCA_SUSPEND, 0, tParam.getPointer(), tParam.size(),pRet);
				tParam.read();
				m_iVcaStatus = tParam.iResult;		//结果
				if (VCA_SUSPEND_STATUS_PAUSE == tParam.iStatus) {
					if (VCA_SUSPEND_RESULT_SUCCESS == tParam.iResult) {
						System.out.println("smart analysis pauses successfully, 1: continue to add, others: exit add");
					} else {
						System.out.println("Smart analysis pause failed, enter any number to exit add.");
					}				
				}
				break;	
			} case NvssdkLibrary.WCM_DWONLOAD_FINISHED: {
				System.out.println("MainNotify:WCM_DWONLOAD_FINISHED! Download successful!");
				NvssdkLibrary.INSTANCE.NetClient_NetFileStopDownloadFile(m_iConnectID);
				break;	
			} case NvssdkLibrary.WCM_DWONLOAD_FAULT: {
				System.out.println("MainNotify:WCM_DWONLOAD_FAULT! Download failed!");
				NvssdkLibrary.INSTANCE.NetClient_NetFileStopDownloadFile(m_iConnectID);
				break;
			} case NvssdkLibrary.WCM_DOWNLOAD_INTERRUPT: {
				System.out.println("MainNotify:WCM_DOWNLOAD_INTERRUPT! Download interrupted!");
				NvssdkLibrary.INSTANCE.NetClient_NetFileStopDownloadFile(m_iConnectID);
				break;
			} default: break; 
			}
		}
	};
	
	PARACHANGE_NOTIFY_V4 cbkParaChange = new PARACHANGE_NOTIFY_V4(){
		public void apply(int ulLogonID, int iChan, int iParaType,
				STR_Para strPara, Pointer iUser)  {
			if (135 != iParaType) {
				return;
			}
            	
			Pointer piIn = strPara.m_iPara[0];
			long iIn = Pointer.nativeValue(piIn);
			
			Pointer piOut = strPara.m_iPara[1];
			long iOut = Pointer.nativeValue(piOut);
			
			Pointer piInDiff = strPara.m_iPara[2];
			long iInDiff = Pointer.nativeValue(piInDiff);
			
            Pointer piOutDiff = strPara.m_iPara[3];
            long iOutDiff = Pointer.nativeValue(piOutDiff);
            
			Pointer piPass = strPara.m_iPara[4];
			long iPass = Pointer.nativeValue(piPass);
			
			Pointer piRegion = strPara.m_iPara[5];	//区域人数
			long iRegion = Pointer.nativeValue(piRegion);
			
			Pointer piStay = strPara.m_iPara[6];
			long iStay = Pointer.nativeValue(piStay);
			
			Pointer piAlarmCount = strPara.m_iPara[7];
			long iAlarmCount = Pointer.nativeValue(piAlarmCount);
			
            System.out.println("[人数统计] ID(" + ulLogonID + "), iIn(" + iIn + "), iOut(" + iOut +").");
			System.out.println("[人数统计] ID(" + ulLogonID + "), iInDiff(" + iInDiff + "), iOutDiff(" + iOutDiff +").");
			System.out.println("[人数统计] ID(" + ulLogonID + "), iPass(" + iPass + "), iRegion(" + iRegion +").");
			System.out.println("[人数统计] ID(" + ulLogonID + "), iStay(" + iStay + "), iAlarmCount(" + iAlarmCount +").");
		}
	};
	
	//登录状态处理
	public void LogonNotify(String strIP, String strID, int iLogonID, int iLogonState) {
		String strMsg = new String();
		m_iLogonID = -1;
		iLogonState = NvssdkLibrary.INSTANCE.NetClient_GetLogonStatus(iLogonID);
		if(NvssdkLibrary.LOGON_SUCCESS == iLogonState){			//登录成功
			m_iLogonID = iLogonID;
			strMsg = "LOGON_SUCCESS";
			
			
			IntBuffer piDigitChannelNum = IntBuffer.allocate(1);
			int iRet = NvssdkLibrary.INSTANCE.NetClient_GetChannelNum(m_iLogonID, piDigitChannelNum);
			if (0 == iRet)
			{
				iChannelNum = piDigitChannelNum.get();
				System.out.println("GetDigitalChannelNum success! iDigitChannelNum=" + iChannelNum);
			}
			else
			{
				System.err.println("GetDigitalChannelNum failed! iRet=" + iRet);
			}
			
			//登录成功后跟设备校时,将本地时间同步到设备
			Calendar c = Calendar.getInstance();
			NvssdkLibrary.INSTANCE.NetClient_SetTime(iLogonID, c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE), 
					c.get(Calendar.HOUR_OF_DAY),  c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
			//开启接收图片流
			StartSnap();
		} else if (NvssdkLibrary.LOGON_FAILED == iLogonState) {	//登录失败
			strMsg = "LOGON_FAILED";
		} else if (NvssdkLibrary.LOGON_TIMEOUT == iLogonState) {	//登录超时
			strMsg = "LOGON_TIMEOUT";
		} else if (NvssdkLibrary.LOGON_RETRY == iLogonState) {		//重新登录
			strMsg = "LOGON_RETRY";
		} else if (NvssdkLibrary.LOGON_ING == iLogonState) {		//正在登录
			strMsg = "LOGON_ING";
		} else {
			strMsg = "LOGON_UNKNOW" + iLogonState;
		}
		System.out.println("[WCM_LOGON_NOTIFY][" + strMsg + "] IP(" + strIP
				+ "),ID(" + strID + "),LogonID(" + iLogonID + ")");
	}
	
	//登录设备
	public int LogonDevice() {
		int iRet = -1;
		int iLogonType = NvssdkLibrary.SERVER_NORMAL;
		System.out.print("Please input LogonType(0--Normal  1--Active 2--Active With Directory): ");
		iLogonType = scanIn.nextInt();
		if (NvssdkLibrary.SERVER_REG_ACTIVE == (iLogonType + 2)) {
			
			//主动模式登陆逻辑
			System.out.print("Please input Directory Server IP:");
			String strIP = scanIn.next();
			System.out.print("Please input Port:");
			int iPort = scanIn.nextInt();
			
			String strUser = "QQ";
			String strPwd = "QQQQ";
			
			tagActiveDirectoryInfo tDirectory = new tagActiveDirectoryInfo();
			tDirectory.iSize = tDirectory.size();
			tDirectory.iIpVer = 0;
			tDirectory.cDsmIP = strIP.getBytes();
			tDirectory.iDsmPort = iPort;
			tDirectory.cAccontName = strUser.getBytes();
			tDirectory.cAccontPwd = strPwd.getBytes();
			tDirectory.write();
			iRet = NvssdkLibrary.INSTANCE.NetClient_SetDsmConfig(NvssdkLibrary.DSM_CMD_SET_DIRECTORY_INFO, tDirectory.getPointer(), tDirectory.size());
			if(0 != iRet) {
				System.out.println("NvssdkLibrary_SetDsmConfig:DSM_CMD_SET_DIRECTORY_INFO fail!");
				return -1;
			}
			System.out.print("Please input ProductID: ");
			String strProductID = scanIn.next();;
			
			int iOutTime = 0;
			while(true) {
				//获取注册在线状态
	
				IntByReference pCount = new IntByReference();
				pCount.setValue(0);
				
				NvssdkLibrary.INSTANCE.NetClient_GetDsmRegstierInfo(NvssdkLibrary.DSM_CMD_GET_DEVCOUNT_WITHREG, pCount.getPointer(), 4);
				
				if (pCount.getValue() > 0) {
					
					tagDsmNvsRegInfoEx tNvsEx = new tagDsmNvsRegInfoEx();
					tNvsEx.cFactoryID = strProductID.getBytes();
					tNvsEx.write();
					
					iRet = NvssdkLibrary.INSTANCE.NetClient_GetDsmRegstierInfo(NvssdkLibrary.DSM_CMD_GET_REGNVSBYID_WITHREG,tNvsEx.getPointer(),tNvsEx.size());
					if(0 != iRet) {
						System.out.println("NvssdkLibrary_GetDsmRegstierInfo:DSM_CMD_GET_REGNVSBYID_WITHREG fail!");
						return -1;
					}
					tNvsEx.read();
					tagAssignProxy tAssignProxy = new tagAssignProxy();
					tAssignProxy.cFactoryID = strProductID.getBytes();
					tAssignProxy.write();
					iRet = NvssdkLibrary.INSTANCE.NetClient_GetDsmRegstierInfo(NvssdkLibrary.DSM_CMD_GET_ASSIGNPROXY_WITHREG, tAssignProxy.getPointer(), tAssignProxy.size());
					if(0 != iRet) {
						System.out.println("NvssdkLibrary_GetDsmRegstierInfo:DSM_CMD_GET_ASSIGNPROXY_WITHREG fail!");
						return -1;
					}
					tAssignProxy.read();
					//登录上去
					
					System.out.print("UserName：" );
					strUser = scanIn.next();
					System.out.print("Password：" );
					String strPswd = scanIn.next();
					
					tagLogonPara tNormal = new tagLogonPara();
					//必须字段
					
					tNormal.iSize = tNormal.size();				//结构体大小
					tNormal.cProductID = strProductID.getBytes();
					tNormal.cNvsIP = tNvsEx.cNvsIP;		//设备ip
					tNormal.cUserName =strUser.getBytes(); //用户名
					tNormal.cUserPwd = strPswd.getBytes();//密码
					tNormal.iNvsPort = tAssignProxy.iProxyPort;//设备端口
					tNormal.cProxy = tAssignProxy.cProxyIpV4;
					tNormal.write();	
					m_iLogonID = NvssdkLibrary.INSTANCE.NetClient_Logon_V4(NvssdkLibrary.SERVER_NORMAL, tNormal.getPointer(), tNormal.size());
					
					break;
				}
				if (iOutTime >= 30)
				{
					System.out.println("Device not register!\n");
					return -1;
				}
				
		        try {
		        	Thread.currentThread();
					Thread.sleep(1000); 
		        } catch(InterruptedException e) {
		            System.err.println("Interrupted");
		        }
		        
				iOutTime++;
			}
			
		}else if(NvssdkLibrary.SERVER_ACTIVE == iLogonType){
			//主动模式登陆逻辑	
			System.out.print("Please input local listen port:");
			int iLocalListenPort = scanIn.nextInt();
			iRet = NvssdkLibrary.INSTANCE.NetClient_SetPort(iLocalListenPort, 0);
			if(0 != iRet) {
				System.out.println("NvssdkLibrary_SetPort fail!");
				return -1;
			}

			tagActiveNetWanInfo tLocalWanInfo = new tagActiveNetWanInfo();
			tLocalWanInfo.iSize = tLocalWanInfo.size();
			System.out.print("Please input wan IP: ");
			String strWanIP = scanIn.next();
			tLocalWanInfo.cWanIP = strWanIP.getBytes();
			System.out.print("Please input local wan port:");
			tLocalWanInfo.iWanPort = scanIn.nextInt();
			tLocalWanInfo.write();
			iRet = NvssdkLibrary.INSTANCE.NetClient_SetDsmConfig(NvssdkLibrary.DSM_CMD_SET_NET_WAN_INFO, tLocalWanInfo.getPointer(), tLocalWanInfo.size());
			if(0 != iRet) {
				System.out.println("NvssdkLibrary_SetDsmConfig:DSM_CMD_SET_NET_WAN_INFO fail!");
				return -1;
			}

			tagDsmOnline tOnline = new tagDsmOnline();
			System.out.print("Please input ProductID: ");
			String strProductID = scanIn.next();
			tOnline.iSize = tOnline.size();
			tOnline.cProductID = strProductID.getBytes();
			tOnline.write();
			int iOutTime = 0;
			while(true) {
				//获取注册在线状态
				NvssdkLibrary.INSTANCE.NetClient_GetDsmRegstierInfo(NvssdkLibrary.DSM_CMD_GET_ONLINE_STATE, tOnline.getPointer(), tOnline.size());
				tOnline.read();
				if (NvssdkLibrary.DSM_STATE_ONLINE == tOnline.iOnline) {
					break;
				}
				if (iOutTime >= 30)
				{
					System.out.println("Device not register!\n");
					return -1;
				}
				
		        try {
		        	Thread.currentThread();
					Thread.sleep(1000); 
		        } catch(InterruptedException e) {
		            System.err.println("Interrupted");
		        }
		        
				iOutTime++;
			}

			tagLogonActiveServer tActive = new tagLogonActiveServer();
			tActive.iSize = tActive.size();
			System.out.print("Please input UserName: ");
			String strUser = scanIn.next();
			tActive.cUserName = strUser.getBytes();
			System.out.print("Please input Password: ");
			String strPwd = scanIn.next();
			tActive.cUserPwd = strPwd.getBytes();
			tActive.cProductID = strProductID.getBytes();
			tActive.write();
			m_iLogonID = NvssdkLibrary.INSTANCE.NetClient_Logon_V4(NvssdkLibrary.SERVER_ACTIVE, tActive.getPointer(), tActive.size());
		} else {
			System.out.print("IP：" );
			String strIp = scanIn.next();
			System.out.print("UserName：" );
			String strUser = scanIn.next();
			System.out.print("Password：" );
			String strPswd = scanIn.next();
			System.out.print("Port：" );
			int iPort = scanIn.nextInt();
			tagLogonPara tNormal = new tagLogonPara();
			//必须字段
			tNormal.iSize = tNormal.size();				//结构体大小
			tNormal.cNvsIP = strIp.getBytes();			//设备ip
			tNormal.cUserName = strUser.getBytes();		//用户名
			tNormal.cUserPwd = strPswd.getBytes();		//密码
			tNormal.iNvsPort = iPort;					//设备端口
			tNormal.write();	
			m_iLogonID = NvssdkLibrary.INSTANCE.NetClient_Logon_V4(NvssdkLibrary.SERVER_NORMAL, tNormal.getPointer(), tNormal.size());
		}
		
		int iTimeOut = 0;
		while(true) {
			int iLogonStatus = NvssdkLibrary.INSTANCE.NetClient_GetLogonStatus(m_iLogonID);
			if(iLogonStatus == NvssdkLibrary.LOGON_SUCCESS) {
				break;
			}
			
			iTimeOut = iTimeOut + 1;
			if(iTimeOut > 5) {
				System.out.println("LogonDevice time out!");
				NvssdkLibrary.INSTANCE.NetClient_Logoff(m_iLogonID);
				m_iLogonID = -1;
				return -1;
			}
			
	        try {
	        	Thread.currentThread();
				Thread.sleep(1000); 
	        } catch(InterruptedException e) {
	            System.err.println("Interrupted");
	        }
		}
		
		System.out.println("[LogonDevice]" + ", logonId=" + m_iLogonID);	
		return 0;
	};
	
	//sdk初始化
	private int SDKInit() {
		//获取sdk版本
		SDK_VERSION ver = new SDK_VERSION();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_GetVersion(ver);
		System.out.println("SDK Version is " + ver.m_ulMajorVersion + "."
				+ ver.m_ulMinorVersion + "." + ver.m_ulBuilder + " "
				+ ver.m_cVerInfo);		
		//设置主回调
		iRet = NvssdkLibrary.INSTANCE.NetClient_SetNotifyFunction_V4(cbkMain, null, cbkParaChange,null, null);
		System.out.println("SetNotifyFunction(" + iRet + ")");	
		//启动sdk
		iRet = NvssdkLibrary.INSTANCE.NetClient_Startup_V4(0,0,0);
		System.out.println("SDK Startup(" + iRet + ")");
		//设置完整登录使能(若登录消息不准确，可以不调用此接口)
		iRet = NvssdkLibrary.INSTANCE.NetClient_SetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_FULL_LOGON, 0, null, 0);
		
		tagLocalSDKPath tInfo = new tagLocalSDKPath();
		tInfo.iSize = tInfo.size();
		tInfo.iType = 0;
		String strPath = new String();
		strPath = "/opt/apache-tomcat-8082/webapps/dzblService/WEB-INF/classes/sdk"; //注意该路径是实际环境放置sdk全套库的绝对路径;
		tInfo.cPath = strPath.getBytes(); //注意该路径是实际环境放置sdk全套库的绝对路径。
		tInfo.write();
		iRet = NvssdkLibrary.INSTANCE.NetClient_SetSDKInitConfig(NvssdkLibrary.INIT_CONFIG_LOCAL_LIBRARY_PATH, tInfo.getPointer(), tInfo.size());
		String strPathOut = new String(tInfo.cPath).trim();
		System.out.println("[INIT_CONFIG_LOCAL_LIBRARY_PATH]" + ", iRet=" + iRet + ", iType=" + tInfo.iType + ", cPath=" + strPathOut);
		
		return 0;
	};

	//人脸库查询
	private int FaceLibraryQuery() {
		int iRet = -1;
		int iPageCount = 20;//NVSSDK.FACE_MAX_PAGE_COUNT; //每页个数，每页最大查询20个
		int iToltalCount = 0;
		tagFaceLibQuery tQuery = new tagFaceLibQuery();
		tQuery.iSize = tQuery.size();
		tQuery.iChanNo = 0;		//通道号，0表示第一通道，IPC只有1个通道，nvr可根据实际赋值
		tQuery.iPageCount = iPageCount;	
		
		tagFaceLibQueryResult tSingle = new tagFaceLibQueryResult();
		int iSingleSize = tSingle.size();
		
		int iPageNo = 0;		//查询页码，0表示第一页，用户可根据实际情况赋值
		while (true) {
			tQuery.iPageNo = iPageNo;
			tQuery.write();
			//查询库信息
			tagFaceLibQueryResult [] tResult = tagFaceLibQueryResult.newArray(20);
			
			iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_LIB_QUERY, tQuery.iChanNo, tQuery.getPointer(), tQuery.size(), tResult[0].getPointer(), iSingleSize);
			if (0 != iRet) {
				System.out.println("Face library query failed:" + iRet);
				return iRet;
			}
			
			//查询完后，打印出来
			tResult[0].read();
			iToltalCount = tResult[0].iTotal;
			
			System.out.println("Face library information(" + iToltalCount + "):------------------------");
			for(int i= 0; i < tResult[0].iTotal && i < NvssdkLibrary.FACE_MAX_PAGE_COUNT; ++i) {
				int iIndex = i + 1 + iPageNo*NvssdkLibrary.FACE_MAX_PAGE_COUNT;
				String strType = "Upload";
				if(0 == tResult[i].tFaceLib.iAlarmType){
					strType = "Not upload";
				}
				tResult[i].read();
				String sLibName = ByteToStr(tResult[i].tFaceLib.cName);
				String sExtrInfo = ByteToStr(tResult[i].tFaceLib.cExtrInfo);
				System.out.println("Serial number:" + iIndex + ", Library key:" +  tResult[i].tFaceLib.iLibKey + ", Similarity:" + 
						 tResult[i].tFaceLib.iThreshold + ", Library name:" +  sLibName + ", Identification information:" +
						strType + ", Description:" +  sExtrInfo);
				m_iLibKey = tResult[i].tFaceLib.iLibKey;
			}
		
			//计算总页数
			int iTotalPage = iToltalCount / iPageCount;
			if (iToltalCount % iPageCount > 0) {
				iTotalPage = iTotalPage + 1;
			}
			iPageNo++;
			if (iPageNo >= iTotalPage || iPageNo > 1) {
				break;
			}
		}
		//获取人脸库能力级
		FuncAbilityLevel tFuncAbilityLevel = new FuncAbilityLevel();
		tFuncAbilityLevel.iSize = tFuncAbilityLevel.size();
		tFuncAbilityLevel.iMainFuncType = 0x09;
		tFuncAbilityLevel.iSubFuncType = 26;
		tFuncAbilityLevel.write();
		IntBuffer pRet = IntBuffer.allocate(1);
		iRet = NvssdkLibrary.INSTANCE.NetClient_GetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_GET_FUNC_ABILITY, 0, tFuncAbilityLevel.getPointer(), tFuncAbilityLevel.size(),pRet);
		if (0 != iRet) {
			System.out.println("Get Face Lib Abilitity Failed:" + iRet);
			return iRet;
		}
		else
		{
			tFuncAbilityLevel.read();
			String sAbilitiy = ByteToStr(tFuncAbilityLevel.cParam);
			System.out.println("Face Library, Current Count " + iToltalCount + ",  Max Support:" + sAbilitiy);
		}
		
		return iRet;
	};
	
	//人脸库添加
	private int FaceLibraryAdd() {
		tagFaceLibEdit tInfo = new tagFaceLibEdit();
		//必须字段
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;		//通道号，0表示第一通道，IPC只有1个通道
		tInfo.tFaceLib.iSize = tInfo.tFaceLib.size();
		tInfo.tFaceLib.iThreshold = 70;	//识别阀值，范围0~100
		tInfo.tFaceLib.iLibKey = 0;		//0表示添加
		tInfo.tFaceLib.iAlarmType = 1;	//0-不上传，1-上传
		//end
		
		//非必需字段
		tInfo.tFaceLib.cName = "libname".getBytes();
		tInfo.tFaceLib.cExtrInfo = "facelib~~~~~~add~~~~~".getBytes();
		//end
		
		//普通设备不需要此字段
		tInfo.tFaceLib.iOptType = 1;	//1添加,2修改
		//end
		
		tInfo.write();
		//添加
		tagFaceReply tReply = new tagFaceReply();
		tReply.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_LIB_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
		if(0 != iRet){
			System.out.println("Face library addition failed:" + iRet);
		} else {
			System.out.println("Face library added results:" + tReply.iResult);
		}
		
		//显示添加人脸库结果
		FaceLibraryQuery();
		return iRet;
	};
	
	//人脸库修改
	private int FaceLibraryModify() {
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0){
			System.out.println("Face library modification failed: please query or add face library first.");
			return -1;
		}
		
		tagFaceLibEdit tInfo = new tagFaceLibEdit();
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;		//通道号，0表示第一通道，IPC只有1个通道
		tInfo.tFaceLib.iSize = tInfo.tFaceLib.size();
		tInfo.tFaceLib.iThreshold = 80;	//识别阀值，范围0~100
		tInfo.tFaceLib.iLibKey = m_iLibKey;//大于0表示修改，此处默认修改第一个库
		tInfo.tFaceLib.iAlarmType = 1;	//0不上传，1上传
		tInfo.tFaceLib.cName = "libname2".getBytes();
		tInfo.tFaceLib.cExtrInfo = "facelib~~~~~~modify~~~~~".getBytes();
		tInfo.tFaceLib.iOptType = 2;	//1添加,2修改
		tInfo.write();
		
		//修改
		tagFaceReply tReply = new tagFaceReply();
		tReply.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_LIB_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
		if(0 != iRet){
			System.out.println("Face library modification failed:" + iRet);
		} else {
			System.out.println("Face library modification results:" + tReply.iResult);
		}
		
		//显示修改人脸库结果
		FaceLibraryQuery();
		return iRet;
	};
	
	//人脸库删除
	private int FaceLibraryDelete() {
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0){
			System.out.println("Face library deletion failed: please query or add face library first.");
			return -1;
		}
		
		tagFaceLibDelete tInfo = new tagFaceLibDelete();
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;		//通道号，0表示第一通道，IPC只有1个通道
		tInfo.iLibKey = m_iLibKey;//此处默认删除最后一个库;
		tInfo.write();
		
		tagFaceReply tReply = new tagFaceReply();
		tReply.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_LIB_DELETE, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());//同步接口，人脸库中底图较多时，接口返回需要等待较长时间
		if(0 != iRet){
			System.out.println("Face library deletion failed:" + iRet);
		} else {
			System.out.println("Face library deletion results:" + tReply.iResult);
		}
		
		//防止立即查询
		try {
        	Thread.currentThread();
			Thread.sleep(1000); 
        } catch(InterruptedException e) {
            System.err.println("Interrupted");
        }

		
		//显示删除人脸库结果
		FaceLibraryQuery();
		return iRet;
	};
	
	//人脸底图查询
	private int FacePictureQuery(int _iPageNo) {
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0) {
			System.out.println("Face basemap query failed: Please add or query face basemap first.");
			return -1;
		}
		
		int iCurPageSize = 0;
		//m_iLibKey = 1;
		//查询条件
		tagFaceQuery tInfo = new tagFaceQuery();
		//必须字段
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;		//通道号，0表示第一通道，IPC只有1个通道, nvr可自由选择多通道
		tInfo.iLibKey = m_iLibKey;
		tInfo.iPageNo = _iPageNo;
		tInfo.iPageCount = NvssdkLibrary.FACE_MAX_PAGE_COUNT;
		tInfo.cBirthStart = "1900-01-01".getBytes();	//开始出生日期
		tInfo.cBirthEnd = "2019-7-31".getBytes();	//结束出生日期
		//end	
		//非必需字段
		tInfo.iSex = 0;			//性别，0未知，1男，2女
		tInfo.iNation = 0;		//民族，0未知
		tInfo.iPlace = 0;		//籍贯，0未知
		tInfo.iCertType = 0;	//证件类型，0未知，1二代身份证，2军官证
		tInfo.iModeling = 0;	//建模状态，0未知, 1建模成功, 2建模失败, 3未建模
		tInfo.cCertNum = "".getBytes();	//证件号码
		tInfo.cName = "".getBytes();	//底图姓名
		//end
		tInfo.write();
		//查询1
		tagFaceQueryResult tSingle = new tagFaceQueryResult();
		tagFaceQueryResult[] tResult = tagFaceQueryResult.newArray(20);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_QUERY, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tResult[0].getPointer(), tSingle.size());
		if(0 != iRet){
			System.out.println("Face basemap query failed:" + iRet);
		} else {
			tResult[0].read();					
			iCurPageSize = tResult[0].iPageCount;//打印当前页底图信息
			System.out.println("Face basemap information(" + iCurPageSize + ")：------------------------");	
			for(int i = 0; i < iCurPageSize && i < NvssdkLibrary.FACE_MAX_PAGE_COUNT; ++i) {
				int iIndex = i + 1;
				tResult[i].read();
				System.out.println("Serial number:" + iIndex + ", Library key:" + tResult[i].tFace.iLibKey + ", Face key:" + 
						tResult[i].tFace.iFaceKey + ", Name:" + ByteToStr(tResult[i].tFace.cName) + ", Date of birth:" +
						ByteToStr(tResult[i].tFace.cBirthTime) + ", Modeling status:" + tResult[i].tFace.iModeling);
				m_iFaceKey = tResult[i].tFace.iFaceKey;
			}
			System.out.println("");
		}	
		//获取人脸底图能力级
		FuncAbilityLevel tFuncAbilityLevel = new FuncAbilityLevel();
		tFuncAbilityLevel.iSize = tFuncAbilityLevel.size();
		tFuncAbilityLevel.iMainFuncType = 0x09;
		tFuncAbilityLevel.iSubFuncType = 27;
		tFuncAbilityLevel.write();
		IntBuffer pRet = IntBuffer.allocate(1);
		iRet = NvssdkLibrary.INSTANCE.NetClient_GetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_GET_FUNC_ABILITY, 0, tFuncAbilityLevel.getPointer(), tFuncAbilityLevel.size(),pRet);
		if (0 != iRet) {
			System.out.println("Get Face Lib Abilitity Failed:" + iRet);
			return iRet;
		}
		else
		{
			tFuncAbilityLevel.read();
			String sAbilitit = ByteToStr(tFuncAbilityLevel.cParam);
			System.out.println("Face Library, Current Count " + iCurPageSize + ",  Max Support:" + sAbilitit);
			System.out.println("");
		}
		return iRet;
	}
	
	//人脸底图添加
	private int FacePictureAdd() {
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0) {
			System.out.println("Face basemap addition failed: please add or query face basemap first.");
			return -1;
		}
		
		tagFaceEdit tInfo = new tagFaceEdit(); 
		//必须字段
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;				//通道号，0表示第一通道，IPC只有1个通道，用户可根据实际需求选择
		tInfo.tFace.iLibKey = m_iLibKey;	//人脸库键值
		tInfo.tFace.iModeling = 1;		//是否建模，1建模，0不建模，用户可根据实际需求选择
		tInfo.tFace.iFaceKey = 0;		//人脸底图键值，0表示添加
		tInfo.tFace.cName = "ZhangSan".getBytes();			//底图姓名，用户可根据实际需求选择
		tInfo.tFace.cBirthTime = "2000-01-01".getBytes();//出生日期，用户可根据实际需求选择
		tInfo.cFacePic = "./face.jpg".getBytes();//底图图片全路径，用户可根据实际需求选择
		tInfo.tFace.iFileType = 0;//文件扩展类型，iFaceKey=0时有效，0-jpg，1-png
		//end
		
		//非必需字段
		tInfo.tFace.iSex = 0;			//性别，0未知，1男，2女
		tInfo.tFace.iNation = 0;		//民族，0未知	
		tInfo.tFace.iPlace = 0;			//籍贯，0未知;
		tInfo.tFace.iCertType = 1;		//证件类型，0未知，1二代身份证，2军官证;
		tInfo.tFace.cCertNum = "232321199909090909".getBytes();//证件号码
		//end
		
		tInfo.tFace.iOptType = 1;		//1=添加，2=修改，3=复制，4=迁移
		
		tInfo.write();
		
		tagFaceReply tReply = new tagFaceReply();
		tReply.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
		tReply.read();
		if(0 != iRet){
			System.out.println("Face basemap addition failed:" + iRet);
		} else {
			System.out.println("Face basemap add results:" + tReply.iResult);
		}

		//显示人脸底图添加后结果
		FacePictureQuery(0);
		return 0;
	}
	
	//人脸底图修改
	private int FacePictureModify() {
		
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0) {
			System.out.println("Face basemap query failed: Please add or query face basemap first.");
			return -1;
		}
		//此处默认修改最后一张底图，用户可根据实际需求选择
		if(m_iFaceKey <= 0) {
			System.out.println("Face basemap modification failed: please first query or add a face base map.");
			return -1;
		}	
		
		tagFaceEdit tInfo = new tagFaceEdit();
		//必须字段
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;				//通道号，0表示第一通道，IPC只有1个通道，用户可根据实际需求选择
		tInfo.tFace.iLibKey = m_iLibKey;	//人脸库键值
		tInfo.tFace.iFaceKey = m_iFaceKey;//人脸底图键值
		tInfo.tFace.cName = "LiSi".getBytes();			//底图姓名
		tInfo.tFace.cBirthTime = "2014-04-04".getBytes();//出生日期
		//end
		
		//非必需字段
		tInfo.tFace.iSex = 0;			//性别，0未知，1男，2女
		tInfo.tFace.iNation = 0;		//民族，0未知	
		tInfo.tFace.iPlace = 0;			//籍贯，0未知;
		tInfo.tFace.iCertType = 1;		//证件类型，0未知，1二代身份证，2军官证;
		tInfo.tFace.cCertNum = "232321201404040404".getBytes();//证件号码
		//end
		tInfo.tFace.iOptType = 2;		//1=添加，2=修改，3=复制，4=迁移
		tInfo.write();
		
		tagFaceReply tReply = new tagFaceReply();
		tReply.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
		tReply.read();
		if(0 != iRet){
			System.out.println("Face basemap modification failed:" + iRet);
		} else {
			System.out.println("Face basemap modification results:" + tReply.iResult);
		}	
		
		//显示人脸底图修改后结果
		FacePictureQuery(0);
		return 0;
	}
	
	//人脸底图删除
	private int FacePictureDelete() {
		
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0) {
			System.out.println("Face basemap query failed: Please add or query face basemap first.");
			return -1;
		}
		
		//此处默认删除最后一张底图，用户可根据实际情况选择
		if(m_iFaceKey <= 0) {
			System.out.println("Face basemap deletion failed: please first query or add a face map.");
			return -1;
		}
		
		tagFaceDelete tInfo = new tagFaceDelete();
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;		//通道号，0表示第一通道，IPC只有1个通道，用户可根据实际情况选择
		tInfo.iLibKey = m_iLibKey;//用户可根据实际需求选择
		tInfo.iFaceKey = m_iFaceKey;//用户可根据实际需求选择
		tInfo.write();
		
		tagFaceReply tReply = new tagFaceReply();
		tReply.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_DELETE, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
		tReply.read();
		if(0 != iRet){
			System.out.println("Face basemap deletion failed:" + iRet);
		} else {
			System.out.println("Face basemap deletion results:" + tReply.iResult);
		}
		
		//显示人脸底图删除后结果
		FacePictureQuery(0);
		return iRet;
	}

	//导出人脸图
	private int ExportFacePic()
	{
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0) {
			System.out.println("Face basemap query failed: Please add or query face basemap first.");
			return -1;
		}
		
		int iCurPageSize = 0;
		//查询条件
		tagFaceQuery tInfo = new tagFaceQuery();
		
		//必须字段
		tInfo.iSize = tInfo.size();
		tInfo.iChanNo = 0;		//通道号，0表示第一通道，IPC只有1个通道, nvr可自由选择多通道，用户可根据实际情况选择
		tInfo.iLibKey = m_iLibKey;
		tInfo.iPageNo = 0;
		tInfo.iPageCount = NvssdkLibrary.FACE_MAX_PAGE_COUNT;
		tInfo.cBirthStart = "1900-01-01".getBytes();	//开始出生日期，用户可根据实际情况选择
		tInfo.cBirthEnd = "2019-10-31".getBytes();	//结束出生日期，用户可根据实际情况选择
		//end	
		//非必需字段
		tInfo.iSex = 0;			//性别，0未知，1男，2女
		tInfo.iNation = 0;		//民族，0未知
		tInfo.iPlace = 0;		//籍贯，0未知
		tInfo.iCertType = 0;	//证件类型，0未知，1二代身份证，2军官证
		tInfo.iModeling = 0;	//建模状态，0未知, 1建模成功, 2建模失败, 3未建模
		tInfo.cCertNum = "".getBytes();	//证件号码
		tInfo.cName = "".getBytes();	//底图姓名
		//end
		tInfo.write();
		//查询1
		tagFaceQueryResult tSingle = new tagFaceQueryResult();
		tagFaceQueryResult[] tResult = tagFaceQueryResult.newArray(20);
		
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_QUERY, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tResult[0].getPointer(), tSingle.size());
		if(0 != iRet){
			System.out.println("Face basemap query failed:" + iRet);
		} else {
			tResult[0].read();					
			iCurPageSize = tResult[0].iPageCount;//打印当前页底图信息
			System.out.println("Face basemap information(" + iCurPageSize + ")：------------------------");	
			for(int i = 0; i < iCurPageSize && i < NvssdkLibrary.FACE_MAX_PAGE_COUNT; ++i) {
				IntBuffer iConnectID = IntBuffer.allocate(1);
				DOWNLOAD_FILE tDownload = new DOWNLOAD_FILE();
				tDownload.m_iSize = tDownload.size();
				tDownload.m_iReqMode = 0;
				tDownload.m_iSpeed = 32;
				String strRemoteFilename = new String();
				
				strRemoteFilename = "FACE:"+ String.valueOf(0) + ":" + String.valueOf(tResult[i].tFace.iLibKey) + ":" + String.valueOf(tResult[i].tFace.iFaceKey);
				tResult[i].read();
				tDownload.m_cRemoteFilename = strRemoteFilename.getBytes();
				
				String strLocalFilename = new String();
				if(0 == tResult[i].tFace.iFileType)//0-jpg  1-png
				{
					strLocalFilename = "C:\\Users\\lijianfei\\Desktop\\Test\\Out\\" +ByteToStr(tResult[i].tFace.cName) + "_" + tResult[i].tFace.iLibKey  + "_"  + tResult[i].tFace.iFaceKey + ".jpg";
				}
				else
				{
					strLocalFilename = "C:\\Users\\lijianfei\\Desktop\\Test\\Out\\" + ByteToStr(tResult[i].tFace.cName) + "_"  + tResult[i].tFace.iLibKey  + "_"  + tResult[i].tFace.iFaceKey + ".png";
				}
				tDownload.m_cLocalFilename = strLocalFilename.getBytes();
				tDownload.write();
				iRet = NvssdkLibrary.INSTANCE.NetClient_NetFileDownload(iConnectID, m_iLogonID, NvssdkLibrary.DOWNLOAD_CMD_FILE, tDownload.getPointer(), tDownload.size());
				if(0 != iRet)
				{
					System.out.println("Download err");
				}
				tDownload.read();
				m_iConnectID = iConnectID.get();
			}
			System.out.println("");
		}	
		return 0;
	}
	
	//检索人脸底图
	private int FaceSearch() {
	
		if(m_iLogonID < 0)
		{
			return -1;
		}
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0) {
			System.out.println("Face basemap query failed: Please add or query face basemap first.");
			return -1;
		}
		//先抠图
		tagFaceCutEx tFaceCutEx = new tagFaceCutEx();
		tFaceCutEx.iSize = tFaceCutEx.size();
		tFaceCutEx.iChanNo = 0; //本demo默认使用通道0，用户可根据实际情况选择
		tFaceCutEx.iPageNo = 0; //本demo默认查询第0页，用户可根据实际情况选择
		tFaceCutEx.iPageCount = 1;//本demo只抠1张人脸进行检索,用户可根据实际情况选择
		tFaceCutEx.iPicType = 1;  //0-jpg，1-png  用户可根据实际情况选择
		tFaceCutEx.cPicPath = "C:\\Users\\lijianfei\\Desktop\\Test\\search\\1111.png".getBytes();//此处为图片存放路径，用户需要自己设置
		tFaceCutEx.write();
		
		tagFaceCutQueryResult tFaceCutQueryResult = new tagFaceCutQueryResult();
		tFaceCutQueryResult.write();
		
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_CUT_EX, 0, tFaceCutEx.getPointer(), tFaceCutEx.size(), tFaceCutQueryResult.getPointer(), tFaceCutQueryResult.size());
		if(0 != iRet)
		{
			System.out.println("[FaceSearch] FACE_CMD_CUT_EX Failed\n");
			return -1;
		}
		
		tFaceCutQueryResult.read();
		//抠图结果进行检索
		tagFaceSearch tFaceSearch = new tagFaceSearch();
		tFaceSearch.iSize = tFaceSearch.size();
		tFaceSearch.iTaskId = tFaceCutQueryResult.iTaskId;
		tFaceSearch.iSimilar = 10;	//相似度，用户可根据情况可选择
		tFaceSearch.iLibKey = m_iLibKey;	//用户可根据情况可选择
		tFaceSearch.cPicName = tFaceCutQueryResult.cFileName;
		tFaceSearch.iPageCount = 20;
		tFaceSearch.write();
		tagFaceQueryResult tSingle = new tagFaceQueryResult();
		tagFaceQueryResult[] tResult = tagFaceQueryResult.newArray(20);
		iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID,  NvssdkLibrary.FACE_CMD_SEARCH, 0, tFaceSearch.getPointer(), tFaceSearch.size(), tResult[0].getPointer(), tSingle.size());
		if(0 != iRet)
		{
			System.out.println("[FaceSearch] FACE_CMD_SEARCH_SNAP Err");
			return -1;
		}
		tResult[0].read();
		int iCurPageSize = tResult[0].iPageCount;//打印当前页
		System.out.println("Face basemap information(" + iCurPageSize + ")：------------------------");	
		for(int i = 0; i < iCurPageSize && i < NvssdkLibrary.FACE_MAX_PAGE_COUNT; ++i) {
			int iIndex = i + 1;
			tResult[i].read();
			System.out.println("Serial number:" + iIndex + ", Library key:" + tResult[i].tFace.iLibKey + ", Face key:" + 
					tResult[i].tFace.iFaceKey + ", Name:" + ByteToStr(tResult[i].tFace.cName) + ", Date of birth:" +
					ByteToStr(tResult[i].tFace.cBirthTime) + ", Modeling status:" + tResult[i].tFace.iModeling);
		}
		return 0;
	}
	
	//按抓拍图检索
	private int SearchSnap() {
		
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0) {
			System.out.println("Face basemap query failed: Please add or query face basemap first.");
			return -1;
		}
		
		//先抠图
		tagFaceCutEx tFaceCutEx = new tagFaceCutEx();
		tFaceCutEx.iSize = tFaceCutEx.size();
		tFaceCutEx.iChanNo = 0;//本demo中使用通道0，用户可根据实际情况赋值
		tFaceCutEx.iPageNo = 0;//本demo中仅查询第0页，用户可根据实际情况赋值
		tFaceCutEx.iPageCount = 1;//本demo只抠1张人脸进行检索,用户可根据实际情况选择
		tFaceCutEx.iPicType = 1;  //0-jpg，1-png
		tFaceCutEx.cPicPath = "C:\\Users\\lijianfei\\Desktop\\Test\\search\\1111.png".getBytes();//此处为图片存放路径，用户需要自己设置
		tFaceCutEx.write();
		
		tagFaceCutQueryResult tSingle = new tagFaceCutQueryResult();
		tagFaceCutQueryResult[] tFaceCutQueryResult = tagFaceCutQueryResult.newArray(20);
		tSingle.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_CUT_EX, 0, tFaceCutEx.getPointer(), tFaceCutEx.size(), tFaceCutQueryResult[0].getPointer(), tSingle.size());
		if(0 != iRet)
		{
			System.out.println("[SearchCapture] FACE_CMD_CUT_EX Failed\n");
			return -1;
		}
		
		tFaceCutQueryResult[0].read();
		for(int i = 0; i < tFaceCutQueryResult[0].iTotal && i < NvssdkLibrary.FACE_MAX_PAGE_COUNT; ++i)
		{
			System.out.println("[SearchCapture]  Cut Result: Index = " + tFaceCutQueryResult[0].iIndex +  " FileName = " + ByteToStr(tFaceCutQueryResult[0].cFileName));
		}
		
		if(0 == tFaceCutQueryResult[0].cFileName.length)
		{
			System.out.println("[SearchCapture] Face Cut Failed!\n");
			return -1;
		}
		
		if(tFaceCutQueryResult[0].iTaskId <= 0)
		{
			System.out.println("[SearchCapture] Please cutout first!\n");
			return -1;
		}
		
		
		//按条件查询
		tagQueryChanNo tQueryChanNo = new tagQueryChanNo(); 
		tQueryChanNo.write();
		tagFaceSearchSnap tQuery = new tagFaceSearchSnap();
		tQuery.iSize = tQuery.size();
		//通道列表，本demo中使用通道0，主码流演示，用户可根据实际情况赋值
		tQuery.iChanCount = 0;
		tQuery.iChanSize = tQueryChanNo.size();
		tQuery.pChanList = (ByReference) tQueryChanNo;
        tQueryChanNo.read();	
		
		//开始结束时间，用户可根据实际情况赋值
		tQuery.tBegTime.iYear = 2019;
		tQuery.tBegTime.iMonth = 8;
		tQuery.tBegTime.iDay = 5;
		tQuery.tBegTime.iHour = 0;
		tQuery.tBegTime.iMinute = 0;
		tQuery.tBegTime.iSecond = 0;
		tQuery.tEndTime.iYear = 2019;
		tQuery.tEndTime.iMonth = 8;
		tQuery.tEndTime.iDay = 21;
		tQuery.tEndTime.iHour = 15;
		tQuery.tEndTime.iMinute = 0;
    	tQuery.tEndTime.iSecond = 0;
	    tQuery.cPicturePath = tFaceCutQueryResult[0].cFileName;
		tQuery.iSimilarity = 9;   //用户可根据实际情况赋值
		tQuery.iSortMode = 1;     //用户可根据实际情况赋值
		tQuery.iTaskId = tFaceCutQueryResult[0].iTaskId;

		tQuery.write();
		iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_SEARCH_SNAP, 0, tQuery.getPointer(), tQuery.size(), null, 0);
		if (0 != iRet) 
		{
			System.out.println("[SearchCapture] Start search failed!\n");
			return -1;
		}
		
		long iStart = System.currentTimeMillis();
		while(true)
		{
			tagFaceReply tOutInfo = new tagFaceReply();
			tagFaceSearchSnapProcess tInfo = new tagFaceSearchSnapProcess();
			tInfo.iSize = tInfo.size();
			tInfo.iTaskId =tFaceCutQueryResult[0].iTaskId;
			tInfo.write();
			tOutInfo.write();
			iRet =  NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_SEARCH_SNAP_PROCESS, 0, tInfo.getPointer(), tInfo.size(), tOutInfo.getPointer(), tOutInfo.size());
			if (0 != iRet) 
			{
				System.out.println("[SearchCapture] Progress query failed!\n");
				return -1;
			}
			
			tOutInfo.read();
			if(6 == tOutInfo.iResult)
			{
				if(100 == tOutInfo.iDelLibProgress)
				{
					System.out.println("[SearchCapture] Search Progress is 100%\n");
					break;
				}

			}
			//5秒超时
			long ulTimeSpan = System.currentTimeMillis() - iStart;
			if (ulTimeSpan >= 5000)
			{
				System.out.println("[SearchCapture] Wait TIMEOUT!\n");
				return -1;
			}
		}

		//结果查询
		tagFaceSearchSnapQuery tInfo = new tagFaceSearchSnapQuery();
		tInfo.iSize = tInfo.size();
		tInfo.iTaskId = tFaceCutQueryResult[0].iTaskId;
		tInfo.iPageSize = 20;
		tInfo.iPageNo = 0;

		tagFaceSearchSnapResult tFaceSearchSnapResult = new tagFaceSearchSnapResult();
		tagFaceSearchSnapResult[] tResult = tagFaceSearchSnapResult.newArray(20);
		
		tInfo.write();
		tFaceSearchSnapResult.write();
		
		iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_SEARCH_SNAP_RESULT, 0, tInfo.getPointer(), tInfo.size(), tResult[0].getPointer(), tFaceSearchSnapResult.size());
		if (0 != iRet)
		{
			System.out.println("[SearchCapture] The result query failed!\n");
			return -1;
		}
		tResult[0].read();
		System.out.println("[SearchCapture] Search Result,Total Number " + tResult[0].iTotal);
	    //此处最多显示20条
		for (int i = 0; i < tResult[0].iCurPageCount && i < 20; ++i)
		{
			tResult[i].read();
			System.out.println("Num: " + tResult[i].iItemIndex + " Chan: " + tResult[i].iChanNo + " SnapName:" + ByteToStr(tResult[i].tPicSnap.cFileName));
			
		}
		return 0;
	}
	
	//按事件检索
	private int SearchEvent()
	{
		tagNetFileQueryVca tQuery = new tagNetFileQueryVca();
		tQuery.iSize = tQuery.size();
		//本demo演示只查询单通道，用户可根据实际需求选择
		tQuery.iChanCount = 1;	
		tagQueryChanNo[] arrayQueryFileChannel = tagQueryChanNo.newArray(32);
		tagQueryChanNo tQueryFileChannel0 = new tagQueryChanNo();
		
		
		tQueryFileChannel0.iChanNo = 0;//表示查询第一个通道
		tQueryFileChannel0.iStream = 0;
		
		arrayQueryFileChannel[0] = tQueryFileChannel0;
		arrayQueryFileChannel[0].write();
		tagQueryChanNo.ByReference[] abc = new tagQueryChanNo.ByReference[tQueryFileChannel0.size()];
		tQuery.pChanList = abc[0];
		tQuery.iChanSize = tQueryFileChannel0.size();
		//本demo只查询一种智能分析类型，用户可根据实际需求选择
		tQuery.iVcaCount = 1;
		tQuery.iVcaList[0] = 9;   //9：人脸识别
		//开始结束时间，用户可根据实际情况赋值
		tQuery.tBegTime.iYear = 2019;
		tQuery.tBegTime.iMonth = 8;
		tQuery.tBegTime.iDay = 5;
		tQuery.tBegTime.iHour = 0;
		tQuery.tBegTime.iMinute = 0;
		tQuery.tBegTime.iSecond = 0;
		tQuery.tEndTime.iYear = 2019;
		tQuery.tEndTime.iMonth = 8;
		tQuery.tEndTime.iDay = 21;
		tQuery.tEndTime.iHour = 15;
		tQuery.tEndTime.iMinute = 0;
		tQuery.tEndTime.iSecond = 0;
		tQuery.iPageCount = 20;
		tQuery.iPageNo = 0;
		tQuery.iFileType = 2;   //查询文件类型 2-图片
		tQuery.iConditionCount = 2; //查询条件个数		
		
		byte[]  tmp = String.valueOf(((1<<16)+7)).getBytes();//检索类型 1-按事件检索
		int length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 0, length);
		
		tmp = String.valueOf(1).getBytes();//本demo使用 人脸检测演示，用户可根据实际情况选择，事件类型 1-人脸检测 2-人脸比对 3-陌生人 4-频次 5-时长
		length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 256, length);

		tagNetFileQueryVcaResult tSingle = new tagNetFileQueryVcaResult();
		tSingle.write();

		int iPageCount = NvssdkLibrary.FACE_MAX_PAGE_COUNT; //每页个数，每页最大查询20个
		int iToltalCount = 0;
		int iPageNo = 0;		//查询页码，0表示第一页，用户可根据实际情况赋值
		while (true) {
			tQuery.iPageNo = iPageNo;
			tQuery.write();
			
			tagNetFileQueryVcaResult[] tResult = tagNetFileQueryVcaResult.newArray(20);
			
			int iRet = NvssdkLibrary.INSTANCE.NetClient_Query_V5(m_iLogonID, NvssdkLibrary.CMD_NETFILE_QUERY_VCA, 0, tQuery.getPointer(), tQuery.size(), tResult[0].getPointer(), tSingle.size());
			if (iRet < 0)
			{
				System.out.println("[SearchEvent] NvssdkLibrary_Query_V5 Failed!"+ iRet + "\n");
				return -1;
			}
			
			tQuery.read();
			tResult[0].read();
			//查询完后，打印出来
			iToltalCount = tResult[0].iTotal;	
			if(0 == iPageNo)
			{
				System.out.println("[SearchEvent] Search Total Num Is " + iToltalCount);
			}

			for(int i = 0; i < tResult[0].iCurPageCount && i < NvssdkLibrary.FACE_MAX_PAGE_COUNT; ++i)
			{
				int iIndex = i + 1 + iPageNo*NvssdkLibrary.FACE_MAX_PAGE_COUNT;
				int iLen = 256;
				byte[] byteTemp = new byte[iLen];
				tResult[i].read();
				System.arraycopy(tResult[i].cExAttr, 8*iLen, byteTemp, 0, iLen);
				
				System.out.println("Num: " + iIndex + " FileName: " + ByteToStr(tResult[i].tFileAttr[0].cFileName) + " Age:"  + tResult[i].cExAttr[0] + " BeginTime: "+ tResult[i].tBegTime.iYear + "-" + tResult[i].tBegTime.iMonth + "-" + 
						tResult[i].tBegTime.iDay + " " + tResult[i].tBegTime.iHour + ":" + tResult[i].tBegTime.iMinute + ":" + tResult[i].tBegTime.iSecond + " EndTime: " + tResult[i].tEndTime.iYear + "-" + tResult[i].tEndTime.iMonth + "-" + 
						tResult[i].tEndTime.iDay + " " + tResult[i].tEndTime.iHour + ":" + tResult[i].tEndTime.iMinute + ":" + tResult[i].tEndTime.iSecond + " " +  
						ByteToStr(byteTemp) );

			}
			
			//计算总页数
			int iTotalPage = iToltalCount / iPageCount;
			if (iToltalCount % iPageCount > 0)
			{
				iTotalPage = iTotalPage + 1;
			}
			iPageNo++;
			if (iPageNo >= iTotalPage)
			{
				break;
			}
			
			
		}

	    return 0;
	}
	
	//按特征检索
	private int SearchFeature()
	{
		tagNetFileQueryVca tQuery = new tagNetFileQueryVca();
		tQuery.iSize = tQuery.size();
		//本demo演示只查询单通道，用户可根据实际需求选择
		tQuery.iChanCount = 1;	
		
		tagQueryChanNo.ByReference tQueryFileChannel0 = new tagQueryChanNo.ByReference();
		tagQueryChanNo.ByReference tQueryFileChannel1 = new tagQueryChanNo.ByReference();
		tQueryFileChannel0.iChanNo = 0;
		tQueryFileChannel0.iStream = 0;
		tQueryFileChannel1.iChanNo = 0;
		tQueryFileChannel1.iStream = 1;

		
		com.nvs.sdk.tagQueryChanNo.ByReference[] arrayQueryFileChannel = new com.nvs.sdk.tagQueryChanNo.ByReference[tQueryFileChannel0.size()];
		arrayQueryFileChannel[0] = tQueryFileChannel0;
		arrayQueryFileChannel[1] = tQueryFileChannel1;
		tQuery.pChanList = arrayQueryFileChannel[0];
		tQuery.iChanSize = tQueryFileChannel0.size();
		//本demo只查询一种智能分析类型，用户可根据实际需求选择
		tQuery.iVcaCount = 1;
		tQuery.iVcaList[0] = 9;   //9：人脸识别
		//开始结束时间，用户可根据实际情况赋值
		tQuery.tBegTime.iYear = 2019;
		tQuery.tBegTime.iMonth = 8;
		tQuery.tBegTime.iDay = 5;
		tQuery.tBegTime.iHour = 0;
		tQuery.tBegTime.iMinute = 0;
		tQuery.tBegTime.iSecond = 0;
		tQuery.tEndTime.iYear = 2019;
		tQuery.tEndTime.iMonth = 8;
		tQuery.tEndTime.iDay = 21;
		tQuery.tEndTime.iHour = 15;
		tQuery.tEndTime.iMinute = 0;
		tQuery.tEndTime.iSecond = 0;
		tQuery.iPageCount = 20;
		tQuery.iPageNo = 0;
		tQuery.iFileType = 2;   //查询文件类型 2-图片
		tQuery.iConditionCount = 7; //查询条件个数
		byte[]  tmp = String.valueOf(((0<<16)+7)).getBytes();//检索类型 0-按特征检索
		int length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 0, length);
		
		tmp = String.valueOf(1).getBytes();//本demo使用 1-少年 演示，用户可根据实际情况选择，表年龄，1-少年，2-青年，3-中年，4-老年
		length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 256, length);
		
		tmp = String.valueOf(1).getBytes();//本demo使用 1-男 演示，用户可根据实际情况选择，表示性别，1-男，2-女，3-未知
		length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 512, length);
		
		tmp = String.valueOf(1).getBytes();//本demo使用 1-汉族 演示，用户可根据实际情况选择，表示民族，1-汉族，2-少数民族
		length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 768, length);
		
		tmp = String.valueOf(2).getBytes();//本demo使用 2-未佩戴 演示，用户可根据实际情况选择，表示戴眼镜 0-预留，1-佩戴，2-未佩戴
		length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 1280, length);
		
		tmp = String.valueOf(2).getBytes();  //本demo使用 2-未佩戴 演示，用户可根据实际情况选择，表示戴口罩 0-预留，1-佩戴，2-未佩戴
		length = tmp.length;
		System.arraycopy(tmp, 0, tQuery.cQueryCondition, 1536, length);
		tQuery.write();
		
		tagNetFileQueryVcaResult tSingle = new tagNetFileQueryVcaResult();
		tSingle.write();
		
		tagNetFileQueryVcaResult[] tResult = tagNetFileQueryVcaResult.newArray(20);
		
		int iRet = NvssdkLibrary.INSTANCE.NetClient_Query_V5(m_iLogonID, NvssdkLibrary.CMD_NETFILE_QUERY_VCA, 0, tQuery.getPointer(), tQuery.size(), tResult[0].getPointer(), tSingle.size());
		if (iRet < 0)
		{
			System.out.println("[SearchFeature] NvssdkLibrary_Query_V5 Failed!\n");
			return -1;
		}
		tQuery.read();
		tResult[0].read();
		System.out.println("[SearchFeature] Search Total Num Is " + tResult[0].iTotal);
		//本demo只显示20条做演示，用户可根据实际情况选择
	/*	for(int i = 0; i < tResult.tResult[0].iCurPageCount; ++i)
		{
			System.out.println("Num: " + tResult.tResult[i].iItemIndex + "FileName: " + ByteToStr(tResult.tResult[i].tFileAttr[0].cFileName) + 
					" BeginTime:" +  tResult.tResult[i].tBegTime.iYear + "-"  + tResult.tResult[i].tBegTime.iMonth + "-" + tResult.tResult[i].tBegTime.iDay + " " + tResult.tResult[i].tBegTime.iHour + ":" + tResult.tResult[i].tBegTime.iMinute + ":" + tResult.tResult[i].tBegTime.iSecond + "EndTime: "+ 
					tResult.tResult[i].tEndTime.iYear + "-"  + tResult.tResult[i].tEndTime.iMonth + "-"  + tResult.tResult[i].tEndTime.iDay + " "  +  tResult.tResult[i].tEndTime.iHour + ":" +  tResult.tResult[i].tEndTime.iMinute + ":" +  tResult.tResult[i].tEndTime.iSecond);
		}*/
	    return 0;	
	}
	
	
	//人脸识别报警设置，nvr支持
	private int SetFaceAlarmParam() {
		tagFaceAlarmParam tAlarmParam = new tagFaceAlarmParam();
		tAlarmParam.iSize = tAlarmParam.size();
		tAlarmParam.iChanNo = 0;		//	通道号，0表示第一通道
		tAlarmParam.iAlarmType = 21; 	//	报警类型，21代表人脸识别（nvr本地智能分析）
		tAlarmParam.iParam1 = 2;		//	算法类型 iAlarmType = 21时，0：人脸检测  1：人脸识别-比对  2：人脸识别-陌生人 3：人脸识别-频次 4：人脸识别-滞留
		tAlarmParam.iEnable = 1;		//	是否启用算法 0-不使能  1-使能
		
		tAlarmParam.iParam2 = 0;		//	iParam1=0\1\2时传0, iParam1=3\4时代表时间
		tAlarmParam.iParam3 = 0;		//	iParam1=0\1\2时传0, iParam1=3时代表频次
		tAlarmParam.iRecognition = 1;	//	是否上传识别信息，0-不支持，1-不上传，2-上传
		tAlarmParam.iSimilar = 75;		//	相似度
		tAlarmParam.iDevType = 1;		//	1-NVR
			
		for(int i = 0; i < 1; ++i)		//遍历所有的库，需要设置每个库都是否使能，此处代码表示只有1个库
		{
			String strLibKey = String.valueOf(m_iLibKey);
			tAlarmParam.cLibkey = strLibKey.getBytes();	//当iParam1=2陌生人时，与库无关，此参数传0即可
			tAlarmParam.cLibUUID = "".getBytes(); //传空即可
			tAlarmParam.iLibEnable = 1; //0不使能，1使能
			tAlarmParam.write();
			
			int iRet = NvssdkLibrary.INSTANCE.NetClient_SetAlarmConfig(m_iLogonID, tAlarmParam.iChanNo
					, 21, NvssdkLibrary.CMD_ALARM_FACE_PARAM, tAlarmParam.getPointer());
			if(0 != iRet){
				System.out.println("Setting face alarm failed:" + iRet);
			} else {
				System.out.println("Set the face alarm result:" + iRet);
			}
		}
		
		return 0;
	}

	//人脸识别报警获取
	private int GetFaceAlarmParam() {
		tagFaceAlarmParamIn tAlarmParam = new tagFaceAlarmParamIn();
		tAlarmParam.iSize = tAlarmParam.size();
		tAlarmParam.iType = 3;	//算法类型 0：人脸检测  1：人脸识别-比对  2：人脸识别-陌生人 3：人脸识别-频次 4：人脸识别-滞留
		
		int iChanNo = 0;		//查询通道号。从0开始，此处默认0，需要查询其他通道按实际通道取值即可
		tagFaceAlarmParam tSingle = new tagFaceAlarmParam();
		int iSingleSize = tSingle.size();
		
		tagFaceAlarmParam [] tResult = tagFaceAlarmParam.newArray(33);
		
		System.out.println("iSingleSize：" + iSingleSize);
		System.out.println("tAlarmParam.iSize：" + tAlarmParam.iSize);
		tAlarmParam.write();
		int iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_ALARM_PARAM, iChanNo, tAlarmParam.getPointer(), tAlarmParam.size(), tResult[0].getPointer(), iSingleSize);
		if (0 != iRet) {
			System.out.println("Failed to get face alarm:" + iRet);
			return iRet;
		}
		tResult[0].read();	
		
		int iVcaEnable = tResult[0].iEnable;		//是否启用算法 0-不使能  1-使能
		int iSimilar =  tResult[0].iSimilar; //相似度
		int iInterval = tResult[0].iParam2; //时间间隔;
		int iFrequency = tResult[0].iParam3; //频次;
		int iRecognition = tResult[0].iRecognition;	//	是否上传识别信息，0-不支持，1-不上传，2-上传
		
		System.out.println("iVcaEnable：" + iVcaEnable);
		System.out.println("iSimilar：" + iSimilar);
		System.out.println("iInterval：" + iInterval);
		System.out.println("iFrequency：" + iFrequency);
		System.out.println("iRecognition：" + iRecognition);
		
		
		for(int j = 0; j < FACE_MAX_LIB_COUNT; ++j) //遍历获取结果中的库
		{
			tResult[j].read();
			if (tResult[j].iSize <= 0) //size等于0表示后面没有结果了
			{
				break;
			}
			
			int iIndex = j+1;
			String sLibKey = ByteToStr(tResult[j].cLibkey);
			
			System.out.println( iIndex +"-LibKey:" + sLibKey + "    LibEnable:" + tResult[j].iLibEnable);
		}
				
		return iRet;
	}

	//获取OSD设置
	private int GetOSDText() {
		ByteBuffer strOSD = ByteBuffer.allocate(64);
		NativeLongByReference pColor = new NativeLongByReference();

		int iRet = NvssdkLibrary.INSTANCE.NetClient_GetOsdText(m_iLogonID, 0, strOSD, pColor);
		if(0 != iRet){
			System.out.println("Failed to get OSD:" + iRet);
		} else {
			byte[] byteArray = strOSD.array(); 
			String strOsdText = ByteToStr(byteArray);
			System.out.println("Get OSD success, OSD:" + strOsdText);
		}
		
		return iRet;
	}

	//获取人脸检测算法使能
	private int GetFaceDetectionEnable() {
		tagAnyScene tInfo = new tagAnyScene();
		tInfo.iBufSize = tInfo.size();
		tInfo.iSceneID = 0;	//场景号0-15
		tInfo.iDevType = 1;	//0-IPC，1-NVR
		tInfo.write();
		
		int iChanNum = 0;	//通道号,IPC为0，NVR为实际通道号
		IntBuffer pRet = IntBuffer.allocate(1);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_GetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_ANYSCENE, iChanNum, tInfo.getPointer(), tInfo.size(),pRet);
		
		if(0 != iRet){
			System.out.println("Get face detection algorithm enabled failed:" + iRet);
		} else {
			
			tInfo.read();	
			
			String strEnable="Disenable";
			
			if(1 == (tInfo.iArithmetic>>2 & 1)){
				strEnable = "Enable";//人脸检测算法开启
			}

			System.out.println("The face detection algorithm enable:" + strEnable);
		}
		
		return iRet;
	}
	
	//获取人脸识别算法使能
	private int GetFaceRecognitionEnable() {
		tagFaceDetectArithmetic tInfo = new tagFaceDetectArithmetic();
		tInfo.iBufSize = tInfo.size();
		tInfo.iDevType = 1;	//0-IPC，1-NVR
		tInfo.write();
		
		int iChanNum = 0;	//通道号,IPC为0，NVR为实际通道号
		IntBuffer pRet = IntBuffer.allocate(1);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_GetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_FACE_DETECT_ARITHMETIC, iChanNum, tInfo.getPointer(), tInfo.size(),pRet);
		
		if(0 != iRet){
			System.out.println("Get face recognition algorithm enabled failed:" + iRet);
		} else {
			tInfo.read();	
			String strEnable="";
			
			//iDentification  0-not supported, 1-off, 2-on
			if(2 == tInfo.iDentification){
				strEnable = "Enable";//人脸检测算法开启
			}else if(1 == tInfo.iDentification){
				strEnable = "Disenable";
			}else{
				strEnable = "Not supported";
			}
			
			System.out.println("Face recognition algorithm enables:" + strEnable);
		}
		
		return iRet;
	}
	
	//按时间段下载前端录像
	private int DownloadRecorder(int _iFileType) {
		DOWNLOAD_TIMESPAN tDownloadTimeSpan = new DOWNLOAD_TIMESPAN();
		
		String strLocalSaveFileName = new String("myTimespanDownload.sdv");
		if (0 == _iFileType) {
			strLocalSaveFileName = "myTimespanDownload.sdv";
			tDownloadTimeSpan.m_iSaveFileType = NvssdkLibrary.DOWNLOAD_FILE_TYPE_SDV;
		} else if(4 == _iFileType){
			strLocalSaveFileName = "myTimespanDownload.mp4";
			tDownloadTimeSpan.m_iSaveFileType = NvssdkLibrary.DOWNLOAD_FILE_TYPE_ZFMP4;
		}
		
		tDownloadTimeSpan.m_iSize = tDownloadTimeSpan.size();
		tDownloadTimeSpan.m_iFileFlag = 1;	//0:Download multiple files  1:Download into a single file
		tDownloadTimeSpan.m_cLocalFilename = strLocalSaveFileName.getBytes();
		tDownloadTimeSpan.m_iChannelNO = 0;	//通道号按实际下载的设备通道号赋值
		tDownloadTimeSpan.m_iStreamNo = 0;	//码流号：0-主码流，1-副码流
		//按时间段下载开始时间
		tDownloadTimeSpan.m_tTimeBegin.iYear = 2019;
		tDownloadTimeSpan.m_tTimeBegin.iMonth = 8;
		tDownloadTimeSpan.m_tTimeBegin.iDay = 24;
		tDownloadTimeSpan.m_tTimeBegin.iHour = 0;
		tDownloadTimeSpan.m_tTimeBegin.iMinute = 12;
		tDownloadTimeSpan.m_tTimeBegin.iSecond = 0;
		//按时间段下载结束时间
		tDownloadTimeSpan.m_tTimeEnd.iYear = 2019;
		tDownloadTimeSpan.m_tTimeEnd.iMonth = 8;
		tDownloadTimeSpan.m_tTimeEnd.iDay = 24;
		tDownloadTimeSpan.m_tTimeEnd.iHour = 14;
		tDownloadTimeSpan.m_tTimeEnd.iMinute = 47;
		tDownloadTimeSpan.m_tTimeEnd.iSecond = 0;
		
		tDownloadTimeSpan.m_iPosition = -1;	//定位功能使用
		tDownloadTimeSpan.m_iSpeed = 32;	//下载速度，最大32，老设备按最大速度下载容易出现中断，因此下载成功后可将速度调成16倍速
		tDownloadTimeSpan.m_iReqMode = 1;	//1:down frame mode,0= Flow pattern; if (mode == 0) Device do not send download time !
		tDownloadTimeSpan.write();
		IntBuffer iConnectID = IntBuffer.allocate(2);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_NetFileDownload(iConnectID, m_iLogonID, NvssdkLibrary.DOWNLOAD_CMD_TIMESPAN, tDownloadTimeSpan.getPointer(), tDownloadTimeSpan.size());
	 	if (0 == iRet)
	 	{
			m_iConnectID = iConnectID.get();
			
			//调整速度
			DOWNLOAD_CONTROL tControl = new DOWNLOAD_CONTROL();
			tControl.m_iSize = tControl.size();
			tControl.m_iPosition = -1;
			tControl.m_iSpeed = 16;
			tControl.m_iReqMode = 1;
			tControl.write();
			
			NvssdkLibrary.INSTANCE.NetClient_NetFileDownload(iConnectID, m_iLogonID, NvssdkLibrary.DOWNLOAD_CMD_CONTROL, tControl.getPointer(), tControl.size());
	 	} else {
			System.err.println("NetFileDownload:DOWNLOAD_CMD_TIMESPAN fail! iRet=" + iRet);
		}
	 	
		return iRet;
	}
	
	//Set snap quality and upload enable
	private int SetPicStreamUploadParam(int _iPicType) {
		tagPicStreamUploadParam tInfo = new tagPicStreamUploadParam();
		tInfo.iSize		= tInfo.size();
		tInfo.iSceneId	= 0;
		tInfo.iPicType	= _iPicType;//0-Background picture，1-Small picture
		tInfo.write();

		int iRet = NvssdkLibrary.INSTANCE.NetClient_VCAGetConfig(m_iLogonID, NvssdkLibrary.VCA_CMD_PICSTREAM_UPLOADPARAM, 0, tInfo.getPointer(), tInfo.size());

		if (iRet < 0)
		{
			System.out.println("NvssdkLibrary_VCAGetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed:" + iRet);
		}
		else
		{
			tInfo.read();
			
			tInfo.iSnapEnable	= 1; //1 upload the snapshot, 0 do not upload the snapshot
			tInfo.iQpvalue		= 30;//picture quality
			tInfo.write();
			
			iRet = NvssdkLibrary.INSTANCE.NetClient_VCASetConfig(m_iLogonID, NvssdkLibrary.VCA_CMD_PICSTREAM_UPLOADPARAM, 0, tInfo.getPointer(), tInfo.size());
			if (iRet >= 0)
			{
				System.out.println("NvssdkLibrary_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM success");
			}
			else
			{
				System.out.println("NvssdkLibrary_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed:" + iRet);
			}
		}

		return iRet;	
	}
	
	private int SyncFaceLibToIpc() {
		//synchronization face library
		int iRet = -1;
		System.out.print("Please Input LibKey:" );
		m_iLibKey = scanIn.nextInt();
		if(m_iLibKey <= 0){
			System.out.println("SyncFaceLibToIpc failed: please query or add face library first.");
			return -1;
		}
		
		tagFaceLibSyncStart tLibSync = new tagFaceLibSyncStart();
		tLibSync.iSize = tLibSync.size();
		tLibSync.iLibKey = m_iLibKey;	// any key of library
		tLibSync.iStatus = NvssdkLibrary.FACE_LIBSYNC_STATUS_START;	//20-start，21-stop 22-delete task
		tLibSync.iChanListArraySize = 1;	//需要同步的通道列表个数
		tLibSync.iChanList[0] = 1;			//通道列表
		tLibSync.write();

		tagFaceReply tReply = new tagFaceReply();
		tReply.write();
		iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_LIB_SYNC_START, 0, tLibSync.getPointer(), tLibSync.size(), tReply.getPointer(), tReply.size());
		tReply.read();
		if(0 != iRet){
			System.out.println("FaceConfig:FACE_CMD_LIB_SYNC_START failed!" + iRet);
		} else {
			System.out.println("FaceConfig:FACE_CMD_LIB_SYNC_START success! results:" + tReply.iResult);
		}
		
		//get	synchronization status 
		int iTotalChanCount = 0;

		IntBuffer piChannelNum = IntBuffer.allocate(1);
		NvssdkLibrary.INSTANCE.NetClient_GetChannelNum(m_iLogonID, piChannelNum);
		iTotalChanCount = piChannelNum.get();
		if (iTotalChanCount < 1) {
			iTotalChanCount = 1;
		}
		tagFaceLibSyncQuery tSyncQuery = new tagFaceLibSyncQuery();
		tSyncQuery.iSize = tSyncQuery.size();
		tSyncQuery.iChanNo = 0x7FFFFFFF;//0x7FFFFFFF代表获取所有通道
		tSyncQuery.iQueryResultSize = iTotalChanCount;
		tSyncQuery.iLibKey = m_iLibKey;

		tagFaceLibSyncQueryResult[] tResultArr = tagFaceLibSyncQueryResult.newArray(360);
		iRet = NvssdkLibrary.INSTANCE.NetClient_FaceConfig(m_iLogonID, NvssdkLibrary.FACE_CMD_LIB_SYNC_STATUS, 0, tSyncQuery.getPointer(), tSyncQuery.size(), tResultArr[0].getPointer(), tResultArr[0].size()*360);
		if(0 != iRet){
			System.out.println("FaceConfig:FACE_CMD_LIB_SYNC_STATUS failed!" + iRet);
		} else {
			System.out.println("FaceConfig:FACE_CMD_LIB_SYNC_STATUS success!");
		}
		
		return iRet;
	}
	
	//设置智能分析状态
	public int SetVcaStatue(int _iStatus) {
		int iChanNo = 0;	//通道号，0表示第一通道，IPC只有1个通道

		IntByReference sta = new IntByReference();
		sta.setValue(_iStatus);

		return NvssdkLibrary.INSTANCE.NetClient_SetDevConfig(m_iLogonID, NvssdkLibrary.NET_CLIENT_VCA_SUSPEND, iChanNo,sta.getPointer() ,4);
	}

	//保存图片
	public int SavePicture(String FileName, Pointer picData, int len) {
		if(null == picData || len <= 0) {
	        return -1;
	    }
		 
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
        	byte[] contentInBytes = picData.getByteArray(0, len);
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
	//图片流回调
	NET_PICSTREAM_NOTIFY CallBack_PicStreamInfo = new NET_PICSTREAM_NOTIFY(){
		
		public int apply(int _iConnectID, NativeLong _lCommand, Pointer _tInfo, int _iLen, Pointer  _lpUserData){
			if(_lCommand.intValue() != NvssdkLibrary.NET_PICSTREAM_CMD_FACE){
				System.out.println("PicDataNotify _lCommand != NvssdkLibrary.NET_PICSTREAM_CMD_FACE") ;
				return -1;
			}
			
			tagFacePicStream tFacePicStream = new tagFacePicStream(_tInfo);
            tFacePicStream.read();	
            System.out.println("PicDataNotify Snap Face count " + tFacePicStream.iFaceCount) ;
  
    		for(int i = 0; i < tFacePicStream.iFaceCount && i < 32; i++)
    		{
	            if(null == tFacePicStream.ptFaceData[i].pcPicData || tFacePicStream.ptFaceData[i].iDataLen<=0)
	            {
	            	continue;
	            }
	            
	            String strName = new String(tFacePicStream.ptFaceData[i].cName).trim();
	            System.out.println("FaceName:" + strName);
	            
	            //解析人脸属性
	    		int iFaceAttrCount = Math.min(tFacePicStream.ptFaceData[i].iFaceAttrCount, 256);

	    		for (int j = 0; j < iFaceAttrCount && j < 256; ++j)
	    		{
	    			//测试人脸属性可以放开下面这条日志，但日志会刷频，因为每张图都带好几个属性
	    			System.out.println("Face attribute--type:" + tFacePicStream.ptFaceData[i].ptFaceAttr[j].iType + ",value:" + tFacePicStream.ptFaceData[i].ptFaceAttr[j].iValue);
	    			if(30 == tFacePicStream.ptFaceData[i].ptFaceAttr[j].iType)
	    			{
	    				System.out.println("Frequency alarm: number of times:" + tFacePicStream.ptFaceData[i].ptFaceAttr[j].iValue);
	    			}
	    			//人脸属性类型及含义说明
	    			//0		年龄	
	    			//1		性别	0-女，1-男
	    			//2		口罩	0-无，1-有
	    			//3		胡子	0-无，1-有
	    			//4		睁眼	0-无，1-有
	    			//5		张嘴	0-无，1-有
	    			//6		眼镜	0-无，1-普通，2-太阳镜
	    			//7		种族	0-黄，1-黑，2-白，3-维族
	    			//8		表情	0-生气，1-平静，2-厌恶，3-困惑，4-高兴，5-悲伤，6-害怕，7-惊讶，8-斜视，9-尖叫
	    			//9		微笑	0-无，1-有
	    			//10	颜值	
	    			//11	民族	0-汉族1-少数民族
	    			//17	目标关联id（ref_id）	
	    			//18	目标类型	0人脸,1机动车,2非机动车,3行人
	    			//19	背包	0-无，1-有
	    			//20	运动方向	0-向上，1-向下，2-向左，3-向右
	    			//21	上身颜色	0未知、1白、2灰、3棕、4红、5蓝、6黄、7绿、8粉、9?橙、10青、11紫 12浅蓝、13黑、14彩色
	    			//22	下身颜色	0未知、1白、2灰、3棕、4红、5蓝、6黄、7绿、8粉、9?橙、10青、11紫 12浅蓝、13黑、14彩色
	    			//23	头发	0-短发，1-长发
	    			//24	长袖	0-无，1-有
	    			//25	速度	0-静止，1-运动
	    			//26	戴帽子	0-无，1-有
	    			//27	长裤	0-无，1-有
	    			//28	短裙	0-无，1-有
	    			//29	骑车	0-无，1-有
	    			//30	频次报警次数	>0
	    			//31	场景（人脸点名球）	[0,65535]
	    			//32	民族概率	[0,100]，大于50为少数民族
	    		}
    		}
            
            //抓拍时间
        	int uiYear = tFacePicStream.ptFullData.tPicTime.uiYear;
       		int uiMonth = tFacePicStream.ptFullData.tPicTime.uiMonth;
    		int uiDay = tFacePicStream.ptFullData.tPicTime.uiDay; 
    		int uiWeek = tFacePicStream.ptFullData.tPicTime.uiWeek; 
    		int uiHour = tFacePicStream.ptFullData.tPicTime.uiHour; 
    		int uiMinute = tFacePicStream.ptFullData.tPicTime.uiMinute; 
    		int uiSecondsr = tFacePicStream.ptFullData.tPicTime.uiSecondsr;
    		int uiMilliseconds = tFacePicStream.ptFullData.tPicTime.uiMilliseconds;
    		
    		String sFileNameBase = new String();
    		sFileNameBase = m_strSavePath + "/";
    		sFileNameBase += "" + uiYear + uiMonth + uiDay + uiWeek + uiHour + uiMinute+ uiSecondsr + uiMilliseconds;
    		
    		//保存全景图片
    		SavePicture(sFileNameBase + "full.jpg", tFacePicStream.ptFullData.pcPicData, tFacePicStream.ptFullData.iDataLen);
    		
    		//保存小图和底图
    		for(int i = 0; i < tFacePicStream.iFaceCount && i < 32; i++) {
    			//人脸小图
    			SavePicture(sFileNameBase + "face" + i + ".jpg", tFacePicStream.ptFaceData[i].pcPicData, tFacePicStream.ptFaceData[i].iDataLen);
    			
    			//人脸底图
    			if(1 == tFacePicStream.ptFaceData[i].iAlramType) {	//合法人脸
    				SavePicture(sFileNameBase + "neg" + i + ".jpg", tFacePicStream.ptFaceData[i].pcNegPicData, tFacePicStream.ptFaceData[i].iNegPicLen);
    			} else {
    				//没有底图或者不合法人脸
    			}
    		}
			return 0;
		}


	};
	//开启图片流
	public int StartSnap() {
		tagNetPicPara tNetPicParam = new tagNetPicPara();
		tNetPicParam.iStructLen = tNetPicParam.size();
		tNetPicParam.iChannelNo = 0;
		tNetPicParam.cbkPicStreamNotify = CallBack_PicStreamInfo; //抓拍回调函数
		tNetPicParam.pvUser = null;
		
		IntBuffer pConnectID = IntBuffer.allocate(1);
		int iRet = NvssdkLibrary.INSTANCE.NetClient_StartRecvNetPicStream(m_iLogonID, tNetPicParam, tNetPicParam.size(), pConnectID);
		if (iRet < 0) {
			m_iConnectID = -1;
			 System.out.println("StartRecvNetPicStream Failed!");
		} else {
			m_iConnectID = pConnectID.get();
			System.out.println("StartRecvNetPicStream Success! ConnectID(" + m_iConnectID + ")");
		}
		
		return 0;
	};
	//退出
	public int Exit() {
		//停止图片流
		NvssdkLibrary.INSTANCE.NetClient_StopRecvNetPicStream(m_iConnectID);
		m_iConnectID = -1;
		
		//注销登陆
		NvssdkLibrary.INSTANCE.NetClient_Logoff(m_iLogonID);
		m_iLogonID = -1;
		
		NvssdkLibrary.INSTANCE.NetClient_Cleanup();
		return 0;
	}
	//创建图片保存目录
	public boolean CtreatePicDir() {
		String destDirName = m_strSavePath;
		File dir = new File(destDirName);
		if (dir.exists()) {// 判断目录是否存在
		//	System.out.println("The target directory already exists!");
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {// 结尾是否以"/"结束
			destDirName = destDirName + File.separator;
		}
		if (dir.mkdirs()) {// 创建目标目录
		//	System.out.println("Create a directory successfully!" + destDirName);
			return true;
		} else {
		//	System.out.println("Failed to create directory!");
			return false;
		}
	}
	
	public static void main(String args[]) {
		
        FaceDemo cls = new FaceDemo();
        //初始化SDK 
        cls.SDKInit();  
        //登录设备
        cls.LogonDevice(); 
        if(cls.m_iLogonID < 0){
        	return;
        }
        
        cls.CtreatePicDir();//创建抓拍目录
        
        while(true) {
        	System.out.println( "[0]Quit                        		[1]QueryFaceLibrary              [2]AddFaceLibrary\n" +
			        			"[3]ModifyFaceLibrary           		[4]DeleteFaceLibrary             [5]QueryFaceBasemap  \n" +
			        			"[6]AddFaceBasemap              		[7]ModifyFaceBasemap             [8]DeleteFaceBasemap\n" +
			        			"[9]SetFaceRecognitionAlarm  		[10]GetFaceRecognitionAlarm   	 [11]GetCharacterOverlay\n" +
			        			"[12]GetFaceDetectionEnabled 		[13]GetFaceRecognitionEnabled 	 [14]DownloadVideoRecording\n" +
			        			"[15]SetSnapQualityAndUploadEnable 	[16]ExportFacePic		 [17]FaceSearch\n" +
        						"[18]SearchCapture              		[19]SearchEvent                  [20]FaceFeature\n" +
			        			"[21]SyncFaceLibToIpc");
        	System.out.print("Please select:");
        	int iOptType = cls.scanIn.nextInt();
        	if(0 == iOptType) {
        		System.out.println("The program is about to exit!");
        		break;
        	} else if(1 == iOptType) {	//查询人脸库
        		cls.FaceLibraryQuery();
        	} else if(2 == iOptType) {	//添加人脸库
        		cls.FaceLibraryAdd(); 
        	} else if(3 == iOptType) {	//修改人脸库
        		cls.FaceLibraryModify();
        	} else if(4 == iOptType) {	//删除人脸库
        		cls.FaceLibraryDelete();  
        	} else if(5 == iOptType) {	//查询人脸底图
        		cls.FacePictureQuery(0);		//此处只查询第一页
        	} else if(6 == iOptType) {	//添加人脸底图
        		//添加人脸底图，需要暂停智能分析，否则会添加失败
        		System.out.println("Please wait for the smart analysis to pause the results!");
        		cls.m_iVcaStatus = 0;
        		cls.SetVcaStatue(VCA_SUSPEND_STATUS_PAUSE);
        		
        		int iOpt = cls.scanIn.nextInt();
        		if(1 == iOpt && VCA_SUSPEND_RESULT_SUCCESS == cls.m_iVcaStatus){
        			//添加人脸
            		cls.FacePictureAdd();		//暂停成功后，可以多次添加
        		}
        		
        		//添加完后恢复智能分析
        		cls.SetVcaStatue(VCA_SUSPEND_STATUS_RESUME);
        	} else if(7 == iOptType) {	//修改人脸底图
        		cls.FacePictureModify();
        	} else if(8 == iOptType) {	//删除人脸底图
        		cls.FacePictureDelete();
        	} else if(9 == iOptType){	//设置人脸报警
        		cls.SetFaceAlarmParam();
        	} else if(10 == iOptType){	//获取人脸报警
        		cls.GetFaceAlarmParam();
        	} else if(11 == iOptType){	//获取字符叠加
        		cls.GetOSDText();
        	} else if(12 == iOptType){	//获取人脸检测算法使能
        		cls.GetFaceDetectionEnable();
			} else if(13 == iOptType){	//获取人脸识别算法使能
        		cls.GetFaceRecognitionEnable();
			} else if(14 == iOptType){	//按时间段下载前端录像
				System.out.print("Please enter the type of video to download（0-sdv，4-mp4）：");
				int iFileType = cls.scanIn.nextInt();
				if (0 == iFileType) {
					iFileType = 0;
				} else {
					iFileType = 4;
				}
				
				cls.DownloadRecorder(iFileType);
			} else if(15 == iOptType){	//Set snap quality and upload enable
				System.out.print("Please enter the type of image you want to set（0-Background picture，1-Small picture）：");
				int iPicType = cls.scanIn.nextInt();
				cls.SetPicStreamUploadParam(iPicType);
			} else if(16 == iOptType) {
				cls.ExportFacePic();//导出人脸图
			} else if(17 == iOptType) {
				cls.FaceSearch();//以图搜图
			} else if(18 == iOptType) {
				cls.SearchSnap();//按抓拍图检索
			} else if(19 == iOptType) {
				cls.SearchEvent();//按事件检索
			} else if(20 == iOptType) {
				cls.SearchFeature();//按特征检索
			} else if(21 == iOptType) {
				cls.SyncFaceLibToIpc();	//同步nvr本地人脸库到前端ipc
			} else {
        		System.out.println("Input operation instruction is illegal: " + iOptType) ;
        	}
        }
        
        //程序退出
        cls.Exit();
	}	
}

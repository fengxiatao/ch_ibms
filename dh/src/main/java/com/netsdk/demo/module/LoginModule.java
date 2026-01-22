package com.netsdk.demo.module;

/**
 * 文件说明：LoginModule 登录与初始化模块
 *
 * 该模块负责 NetSDK 的初始化、日志配置、自动重连、登录与登出等流程。
 * 为解决“打开解码库失败(0x8000000b)”问题，新增在 SDK 初始化前设置本地转码库路径
 * 并显式预加载解码库（play.dll），以确保 H.265（HEVC）依赖库可正确解析。
 *
 * 注意：根据 SDK 说明，CLIENT_SetSDKLocalCfg 必须在 CLIENT_Init 之前调用。
 */

import java.io.File;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.LLong;
import com.netsdk.lib.NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY;
import com.netsdk.lib.NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY;
import com.netsdk.lib.enumeration.NET_EM_SDK_LOCAL_CFG_TYPE;
import com.netsdk.lib.structure.NET_CONFIG_STREAMCONVERTOR_INFO;
import com.netsdk.lib.structure.NET_CONFIG_OPENSSL_INFO;
import com.netsdk.lib.ToolKits;
import com.netsdk.lib.LibraryLoad;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Native;

import com.sun.jna.ptr.IntByReference;

/**
 * 登陆接口实现
 * 主要有 ：初始化、登陆、登出功能
 */
public class LoginModule {

    /**
     * 静态初始化块：
     * - 在 NetSDKLib 类加载之前，优先设置本地动态库的提取与加载目录为项目内的 libs/win64。
     * - 这样可以避免 JNA 默认解压到系统临时目录，确保加载到我们指定版本的原生库。
     */
    static {
        try {
            String nativeDir = new File("./libs/win64").getAbsolutePath();
            LibraryLoad.setExtractPath(nativeDir);
            System.out.println("已设置本地动态库提取目录：" + nativeDir);
        } catch (Throwable t) {
            System.err.println("设置动态库提取目录失败：" + t.getMessage());
        }
    }

	public static NetSDKLib netsdk 		= NetSDKLib.NETSDK_INSTANCE;
	public static NetSDKLib configsdk 	= NetSDKLib.CONFIG_INSTANCE; 
	
	// 设备信息
	public static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();
	
	// 登陆句柄
	public static LLong m_hLoginHandle = new LLong(0);   
	
	private static boolean bInit    = false;
	private static boolean bLogopen = false;

    /**
     * 私有方法：在 SDK 初始化前设置转码库路径并预加载解码库。
     *
     * - 设置 StreamConvertor.dll 的绝对路径到本地 SDK 配置，解决 H.265 解码依赖。
     * - 配置 OpenSSL 库路径（ssleay32.dll、libeay32.dll），确保解码库依赖满足。
     * - 显式 System.load 预加载必要依赖（OpenSSL/图像处理）以及 play.dll，避免从临时目录加载到不兼容版本。
     *
     * 异常处理：捕获所有异常并打印，保证不影响后续初始化流程。
     *
     * @throws 无（内部捕获异常并打印）
     */
    private static void preconfigureDecoderLibraries() {
        try {
            // 1) 确保 JNA 动态库提取目录指向项目 libs/win64
            String nativeDir = new File("./libs/win64").getAbsolutePath();
            LibraryLoad.setExtractPath(nativeDir);
            System.out.println("已确认本地动态库提取目录：" + LibraryLoad.getExtractPath());

            // 指定转码库路径（Windows 64 位），用于 H.265 依赖
            String streamConvertorPath = new File("./libs/win64/StreamConvertor.dll").getAbsolutePath();
            NET_CONFIG_STREAMCONVERTOR_INFO streamCfg = new NET_CONFIG_STREAMCONVERTOR_INFO();
            ToolKits.StringToByteArray(streamConvertorPath, streamCfg.szStreamConvertor);

            // 将结构体写入本地内存后传入 SDK，本接口需在 CLIENT_Init 之前调用
            streamCfg.write();
            Pointer pStreamCfg = streamCfg.getPointer();
            boolean setOk = netsdk.CLIENT_SetSDKLocalCfg(
                    NET_EM_SDK_LOCAL_CFG_TYPE.NET_SDK_LOCAL_CFG_TYPE_STREAMCONVERTOR.ordinal(),
                    pStreamCfg);
            if (!setOk) {
                System.err.println("设置转码库路径失败：" + streamConvertorPath);
            } else {
                System.out.println("已设置转码库路径：" + streamConvertorPath);
            }

            // 2) 配置 OpenSSL 路径（Windows 下 ssleay32.dll / libeay32.dll）
            NET_CONFIG_OPENSSL_INFO opensslCfg = new NET_CONFIG_OPENSSL_INFO();
            String sslPath = new File("./libs/win64/ssleay32.dll").getAbsolutePath();
            String cryptoPath = new File("./libs/win64/libeay32.dll").getAbsolutePath();
            ToolKits.StringToByteArray(sslPath, opensslCfg.szSsleay);
            ToolKits.StringToByteArray(cryptoPath, opensslCfg.szLibeay);
            opensslCfg.write();
            Pointer pOpenssl = opensslCfg.getPointer();
            boolean sslOk = netsdk.CLIENT_SetSDKLocalCfg(
                    NET_EM_SDK_LOCAL_CFG_TYPE.NET_SDK_LOCAL_CFG_TYPE_OPENSSL.ordinal(),
                    pOpenssl);
            if (!sslOk) {
                System.err.println("设置 OpenSSL 库路径失败：" + sslPath + " , " + cryptoPath);
            } else {
                System.out.println("已设置 OpenSSL 库路径：" + sslPath + " , " + cryptoPath);
            }

            // 3) 预加载依赖库，优先加载 OpenSSL 与图像处理相关库，最后加载 play.dll
            String[] prereqLibs = new String[] {
                new File("./libs/win64/libeay32.dll").getAbsolutePath(),
                new File("./libs/win64/ssleay32.dll").getAbsolutePath(),
                new File("./libs/win64/Infra.dll").getAbsolutePath(),
                new File("./libs/win64/ImageAlg.dll").getAbsolutePath(),
                new File("./libs/win64/IvsDrawer.dll").getAbsolutePath()
            };
            for (String lib : prereqLibs) {
                try {
                    System.load(lib);
                    System.out.println("预加载依赖库成功：" + lib);
                } catch (Throwable e) {
                    System.err.println("预加载依赖库失败(忽略继续)：" + lib + " , " + e.getMessage());
                }
            }

            // 显式预加载 play.dll，确保解码库为项目 libs/win64 版本
            String playPath = new File("./libs/win64/play.dll").getAbsolutePath();
            System.load(playPath);
            System.out.println("已预加载解码库：" + playPath);
        } catch (Throwable t) {
            // 捕获 Throwable，避免任何加载异常阻断后续初始化
            System.err.println("预配置解码相关库失败：" + t.getMessage());
        }
    }
	
    /**
     * \if ENGLISH_LANG
     * Init
     * \else
     * 初始化
     * \endif
     *
     * 函数说明：完成 SDK 初始化，并在初始化前调用本地库预配置，确保解码相关依赖可用。
     *
     * 参数说明：
     * - disConnect：设备断线回调函数指针
     * - haveReConnect：设备重连成功回调函数指针
     *
     * 返回值：
     * - true：初始化成功
     * - false：初始化失败
     *
     * 异常：无（内部打印错误日志，不抛出异常）
     */
    public static boolean init(NetSDKLib.fDisConnect disConnect, NetSDKLib.fHaveReConnect haveReConnect) {	
        // 在 SDK 初始化之前进行本地解码库预配置（H.265/依赖库）
        preconfigureDecoderLibraries();

		bInit = netsdk.CLIENT_Init(disConnect, null);
		if(!bInit) {
			System.out.println("Initialize SDK failed");
			return false;
		}

		//打开日志，可选
		NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
        File path = new File("./sdklog/");
        if (!path.exists()) {
            path.mkdir();
        }
		String logPath = path.getAbsoluteFile().getParent() + "\\sdklog\\" + ToolKits.getDate() + ".log";
		setLog.nPrintStrategy = 0;
		setLog.bSetFilePath = 1;
		System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
		System.out.println(logPath);
		setLog.bSetPrintStrategy = 1;
		bLogopen = netsdk.CLIENT_LogOpen(setLog);
		if(!bLogopen ) {
			System.err.println("Failed to open NetSDK log");
		}
		
		// 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
		// 此操作为可选操作，但建议用户进行设置
		netsdk.CLIENT_SetAutoReconnect(haveReConnect, null);
	    
		//设置登录超时时间和尝试次数，可选
		int waitTime = 5000; //登录请求响应超时时间设置为5S
		int tryTimes = 1;    //登录时尝试建立链接1次
		netsdk.CLIENT_SetConnectTime(waitTime, tryTimes);
		
		
		// 设置更多网络参数，NET_PARAM的nWaittime，nConnectTryNum成员与CLIENT_SetConnectTime 
		// 接口设置的登录设备超时时间和尝试次数意义相同,可选
		NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
		netParam.nConnectTime = 10000;      // 登录时尝试建立链接的超时时间
		netParam.nGetConnInfoTime = 3000;   // 设置子连接的超时时间
		netParam.nGetDevInfoTime = 3000;//获取设备信息超时时间，为0默认1000ms
		netsdk.CLIENT_SetNetworkParam(netParam);	
		
		return true;
	}
	
	/**
	 * \if ENGLISH_LANG
	 * CleanUp
	 * \else
	 * 清除环境
	 * \endif
	 */
	public static void cleanup() {
		if(bLogopen) {
			netsdk.CLIENT_LogClose();
		}
		
		if(bInit) {
			netsdk.CLIENT_Cleanup();
		}
	}

	/**
	 * \if ENGLISH_LANG
	 * Login Device
	 * \else
	 * 登录设备
	 * \endif
	 */
	public static boolean login(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) {	
		//IntByReference nError = new IntByReference(0);
		//入参
		NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam=new NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
		pstInParam.nPort=m_nPort;
		pstInParam.szIP=m_strIp.getBytes();
		pstInParam.szPassword=m_strPassword.getBytes();
		pstInParam.szUserName=m_strUser.getBytes();
		//出参
		NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam=new NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
		pstOutParam.stuDeviceInfo=m_stDeviceInfo;
		//m_hLoginHandle = netsdk.CLIENT_LoginEx2(m_strIp, m_nPort, m_strUser, m_strPassword, 0, null, m_stDeviceInfo, nError);		
		m_hLoginHandle=netsdk.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
		if(m_hLoginHandle.longValue() == 0) {
			System.err.printf("Login Device[%s] Port[%d]Failed. %s\n", m_strIp, m_nPort, ToolKits.getErrorCodePrint());
		} else {
			System.out.println("Login Success [ " + m_strIp + " ]");
		}	
		
		return m_hLoginHandle.longValue() == 0? false:true;
	}
	
	/**
	 * \if ENGLISH_LANG
	 * Logout Device
	 * \else
	 * 登出设备
	 * \endif
	 */
	public static boolean logout() {
		if(m_hLoginHandle.longValue() == 0) {
			return false;
		}
		
		boolean bRet = netsdk.CLIENT_Logout(m_hLoginHandle);
		if(bRet) {			
			m_hLoginHandle.setValue(0);	
		}
		
		return bRet;
	}
}

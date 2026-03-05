package src;

import src.NVSSDK.ALARM_NOTIFY;
import src.NVSSDK.CLIENTINFO;
import src.NVSSDK.MAIN_NOTIFY;
import src.NVSSDK.PARACHANGE_NOTIFY;
import src.NVSSDK.RECVDATA_NOTIFY;
import src.NVSSDK.SDK_VERSION;
import src.NVSSDK.ENCODERINFO;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * 
 * @author __USER__
 */
@SuppressWarnings("serial")
public class MainFrame extends javax.swing.JFrame{

	int m_iLogonID = -1;
	int m_iConnectID = -1;
	
	MAIN_NOTIFY cbkMain = new MAIN_NOTIFY()
	{
		public void MainNotify(int iLogonID, int wParam, Pointer lParam,
				Pointer noitfyUserData) {

			int iMsgType = wParam & 0xFFFF;
			switch (iMsgType) {
			case NVSSDK.WCM_LOGON_NOTIFY:
			{
				try
				{
					int iLogonStatus = NetClient.GetLogonStatus(iLogonID);
					ENCODERINFO tDevInfo = new ENCODERINFO();
					NetClient.GetDevInfo(iLogonID, tDevInfo);
					
					String strIP = new String(tDevInfo.m_cEncoder).trim();			
					String strID = new String(tDevInfo.m_cFactoryID).trim();			
					LogonNotify(strIP, strID, iLogonID, iLogonStatus);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case NVSSDK.WCM_VIDEO_HEAD:
				//VideoArrive(iLogonID, (wParam >> 16) & 0xFF, wParam >> 24);
				break;
			default:
				appendMsg("[MAIN_NOTIFY] Msg(" + iMsgType + "),LogonID("
						+ iLogonID + "),wParam(" + wParam + "),lParam(" + lParam
						+ ")");
			}
		}
	};
	
	ALARM_NOTIFY cbkAlarm = new ALARM_NOTIFY(){
		public void AlarmNotify(int _iLogonID, int _iChannel,
				int _iAlarmState, int _iAlarmType, Pointer _pUserData) {

		}
	};
	
	PARACHANGE_NOTIFY cbkParaChange = new PARACHANGE_NOTIFY(){
		public void ParaChangeNotify(int iLogonID, int iChannel, int paraType,
				Pointer para, Pointer noitfyUserData) {
			appendMsg("[PARACHANGE_NOTIFY] ID(" + iLogonID + "),Channel("
					+ iChannel + "),Type(" + paraType + ")");
		}
	};
	RECVDATA_NOTIFY cbkRecvData = new RECVDATA_NOTIFY(){
		public void RecvDataNotify(int _ulID, Pointer data, int len, int _iFlag,
				Pointer _lpUserData) {
			appendMsg("[RECVDATA_NOTIFY] ConnID(" + _ulID + "),DataLen("
					+ len + ")");
		}
	};

	private void appendMsg(String msg) {
		//System.out.println(msg);
		jTextArea_Msg.append(msg + "\n");
	}

	private int SDKInit() {
		SDK_VERSION ver = new SDK_VERSION();
		int iRet = NetClient.GetVersion(ver);
		appendMsg("[SDK_VERSION]" + ver.m_ulMajorVersion + "."
				+ ver.m_ulMinorVersion + "." + ver.m_ulBuilder + " "
				+ ver.m_cVerInfo);

		int iServerPort = 3000;
		int iClientPort = 6000;
		while (-2 == NetClient.SetPort(iServerPort, iClientPort)) {
			iServerPort++;
			iClientPort++;
			appendMsg("Set Port(" + iServerPort + "," + iClientPort
					+ ") Failed!");
		}
		appendMsg("Set Port(" + iServerPort + "," + iClientPort
				+ ") Success!");
		iRet = NetClient.SetNotifyFunction(cbkMain, cbkAlarm, cbkParaChange);
		appendMsg("SetNotifyFunction(" + iRet + ")");

		iRet = NetClient.Startup();
		appendMsg("Startup(" + iRet + ")");

		return 0;
	}

	public void LogonNotify(String strIP, String strID, int iLogonID,
			int iLogonState) {
		String strMsg = new String();
		m_iLogonID = -1;
		switch (iLogonState) {
		case NVSSDK.LOGON_SUCCESS: {
			m_iLogonID = iLogonID;
			strMsg = "LOGON_SUCCESS";
			break;
		}
		case NVSSDK.LOGON_FAILED: {
			strMsg = "LOGON_SUCCESS";
			break;
		}
		case NVSSDK.LOGON_TIMEOUT: {
			strMsg = "LOGON_TIMEOUT";
			break;
		}
		case NVSSDK.LOGON_RETRY: {
			strMsg = "LOGON_RETRY";
			break;
		}
		case NVSSDK.LOGON_ING: {
			strMsg = "LOGON_ING";
			break;
		}
		default: {
			appendMsg("[WCM_LOGON_NOTIFY][" + iLogonState + "] IP("
					+ strIP + "),ID(" + strID + "),LogonID(" + iLogonID + ")");
		}
		}
		appendMsg("[WCM_LOGON_NOTIFY][" + strMsg + "] IP(" + strIP
				+ "),ID(" + strID + "),LogonID(" + iLogonID + ")");
	}

	public void VideoArrive(int iLogonID, int iChannel, int iStream) {
		appendMsg("[WCM_VIDEO_HEAD] LogonID(" + iLogonID
				+ "),Channel(" + iChannel + "),Stream(" + iStream + ")");
		int iRet = NetClient.StartPlay(m_iConnectID, canvas1, 1);
		if (iRet < 0) {
			appendMsg("StartPlay Failed! Ret(" + iRet + "),ConnectID("
					+ m_iConnectID + "),Error(USER_ERROR+"
					+ (Native.getLastError() - NVSSDK.USER_ERROR) + ")");
		} else {
			appendMsg("StartPlay Success! Ret(" + iRet + "),ConnectID("
					+ m_iConnectID + ")");
		}
	}

	/** Creates new form MainFrame */
	public MainFrame() {
		initComponents();
		
		SDKInit();
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		canvas1 = new java.awt.Canvas();
		jPanel1 = new javax.swing.JPanel();
		jTextField_IP = new javax.swing.JTextField();
		jTextField_Port = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jTextField_User = new javax.swing.JTextField();
		jButton6 = new javax.swing.JButton();
		jButton7 = new javax.swing.JButton();
		jTextField_Proxy = new javax.swing.JTextField();
		jButton2 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		jTextField_Pwd = new javax.swing.JTextField();
		jComboBox_Channel = new javax.swing.JComboBox();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextArea_Msg = new javax.swing.JTextArea();
		jButton5 = new javax.swing.JButton();
		jButton8 = new javax.swing.JButton();

		setTitle("\u6d4b\u8bd5");
		setDefaultCloseOperation(3);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		canvas1.setBackground(java.awt.Color.black);

		jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jTextField_IP.setText("192.168.1.2");
		jTextField_IP.setToolTipText("IP");

		jTextField_Port.setText("3000");
		jTextField_Port.setToolTipText("Port");

		jButton1.setText("Logon");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jButton3.setText("StarRecv");
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});

		jTextField_User.setText("Admin");
		jTextField_User.setToolTipText("User Name");

		jButton6.setText("StartPlay");
		jButton6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton6ActionPerformed(evt);
			}
		});

		jButton7.setText("StopPlay");
		jButton7.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				StopPlay(evt);
			}
		});
		
		jTextField_Proxy.setToolTipText("Proxy IP");

		jButton2.setText("Logoff");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jButton4.setText("StopRecv");
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});

		jTextField_Pwd.setText("1111");
		jTextField_Pwd.setToolTipText("Password");

		jComboBox_Channel.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7",
						"CH8", "CH9", "CH10", "CH11", "CH12", "CH13", "CH14",
						"CH15", "CH16" }));
		jComboBox_Channel.setToolTipText("Channel");

		jTextArea_Msg.setColumns(20);
		jTextArea_Msg.setEditable(false);
		jTextArea_Msg.setRows(5);
		jTextArea_Msg.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTextArea_MsgMouseClicked(evt);
			}
		});
		jScrollPane1.setViewportView(jTextArea_Msg);

		jButton5.setText("Record");
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton5ActionPerformed(evt);
			}
		});
		
		jButton8.setText("StopRecord");
		jButton8.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				StopCaptureFile(evt);
			}
		});

		org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.TRAILING)
														.add(
																org.jdesktop.layout.GroupLayout.LEADING,
																jScrollPane1,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																168,
																Short.MAX_VALUE)
														.add(
																jPanel1Layout
																		.createSequentialGroup()
																		.add(
																				jPanel1Layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								jTextField_IP,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								81,
																								Short.MAX_VALUE)
																						.add(
																								jTextField_Port,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								81,
																								Short.MAX_VALUE)
																						.add(
																								org.jdesktop.layout.GroupLayout.TRAILING,
																								jButton1,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								81,
																								Short.MAX_VALUE)
																						.add(
																								org.jdesktop.layout.GroupLayout.TRAILING,
																								jButton3,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.add(
																								jTextField_User,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								81,
																								Short.MAX_VALUE)
																						.add(
																								jButton6,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								81,
																								Short.MAX_VALUE)
																						.add(
																								jButton7,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								81,
																								Short.MAX_VALUE)
																								
																							)
																								
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				jPanel1Layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING,
																								false)
																						.add(
																								jButton5,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																								
																						.add(
																								jButton8,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.add(
																								jTextField_Proxy)
																						.add(
																								jButton2,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.add(
																								jButton4,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.add(
																								jTextField_Pwd)
																						.add(
																								jComboBox_Channel,
																								0,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE))))
										.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jTextField_IP,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(jTextField_Proxy))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jTextField_Port,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jComboBox_Channel,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																20,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jTextField_Pwd,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jTextField_User,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.add(8, 8, 8)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jButton1).add(
																jButton2))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jButton3).add(
																jButton4))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jButton6).add(
																jButton5)
																
										)
										
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jButton8).add(
																jButton7)
																
										)
										
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jButton7).add(
																jButton6)
																
										)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jScrollPane1,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												184,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup().addContainerGap().add(canvas1,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 438,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								jPanel1,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup().addContainerGap().add(
						layout.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING).add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								canvas1,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								376, Short.MAX_VALUE).add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								jPanel1,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)).addContainerGap()));

		pack();
	}// </editor-fold>

	// GEN-END:initComponents

	private void jTextArea_MsgMouseClicked(java.awt.event.MouseEvent evt) {

		// NetClient.FunctionInvoke();
	}

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
		int iRet = NetClient.StartCaptureFile(m_iConnectID, "D:/test.sdv", 0);
		appendMsg("StartCaptureFile(" + iRet + ") ConnectID(" + m_iConnectID
				+ ")");
	}
	
	private void StopCaptureFile(java.awt.event.ActionEvent evt) {
		int iRet = NetClient.StopCaptureFile(m_iConnectID);
		appendMsg("StopCaptureFile(" + iRet + ") ConnectID(" + m_iConnectID + ")");
	}
	
	
	private void StopPlay(java.awt.event.ActionEvent evt) {
		int iRet = NetClient.StopPlay(m_iConnectID);
		appendMsg("StopPlay(" + iRet + ") ConnectID(" + m_iConnectID + ")");
	}
	
	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
		int iRet = NetClient.StartPlay(m_iConnectID, canvas1, 1);
		if (iRet < 0) {
			appendMsg("StartPlay Failed! Ret(" + iRet + "),ConnectID("
					+ m_iConnectID + "),Error(USER_ERROR+"
					+ (Native.getLastError() - NVSSDK.USER_ERROR) + ")");
		} else {
			appendMsg("StartPlay Success! Ret(" + iRet + "),ConnectID("
					+ m_iConnectID + ")");
		}
		
//		iRet = NetClient.StartCaptureData(m_iConnectID);
//		if (iRet < 0) {
//			appendMsg("StartCaptureData Failed! Ret(" + iRet + "),ConnectID("
//					+ m_iConnectID + "),Error(USER_ERROR+"
//					+ (Native.getLastError() - NVSSDK.USER_ERROR) + ")");
//		} else {
//			appendMsg("StartCaptureData Success! ConnectID("
//					+ m_iConnectID + ")");
//		}
	}

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
		int iRet = NetClient.StopRecv(m_iConnectID);
		appendMsg("[StopRecv] Ret(" + iRet + "),ConnectID(" + m_iConnectID
				+ ")");
	}

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
		CLIENTINFO clientInfo = new CLIENTINFO();
		String strIP = jTextField_IP.getText();
		clientInfo.m_cRemoteIP = strIP.getBytes();
		int iChannel = jComboBox_Channel.getSelectedIndex();
		clientInfo.m_iChannelNo = iChannel;
		clientInfo.m_iNetMode = 1;
		clientInfo.m_iStreamNO = 0;
		clientInfo.m_iServerID = m_iLogonID;
		clientInfo.m_iBufferCount = 20;
		clientInfo.m_iDelayNum = 1;
		clientInfo.m_iTimeout = 2000;
		clientInfo.m_iTTL = 8;
		clientInfo.write();

		IntByReference pConnectID = new IntByReference();
		int iRet = NetClient.StartRecv(pConnectID, clientInfo, cbkRecvData);
		if (iRet < 0) {
			m_iConnectID = -1;
			appendMsg("StartRecv Failed!");
		} else {
			m_iConnectID = pConnectID.getValue();
			appendMsg("StartRecv Success! ConnectID(" + m_iConnectID + ")");
//			if (iRet == 1) {
//				iRet = NetClient.StartPlay(m_iConnectID, canvas1, 1);
//				if (iRet < 0) {
//					appendMsg("StartPlay Failed! Ret(" + iRet + "),ConnectID("
//							+ m_iConnectID + "),Error(USER_ERROR+"
//							+ (Native.getLastError() - NVSSDK.USER_ERROR) + ")");
//				} else {
//					appendMsg("StartPlay Success! ConnectID("
//							+ m_iConnectID + ")");
//				}
//			}
		}
	}

	private void formWindowClosing(java.awt.event.WindowEvent evt) {
		if (m_iLogonID >= 0) {
			int iRet1 = NetClient.Logoff(m_iLogonID);
			appendMsg("Logoff(" + iRet1 + ") LogonID(" + m_iLogonID + ")");
		}

		int iRet = NetClient.Cleanup();
		appendMsg("Cleanup(" + iRet + ")");
	}

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
		int iRet = NetClient.Logoff(m_iLogonID);
		if (iRet < 0) {
			appendMsg("Logoff Failed! iRet(" + iRet + "),LogonID(" + m_iLogonID
					+ ")");
		} else {
			appendMsg("Logoff Success! LogonID(" + m_iLogonID + ")");
		}
		m_iLogonID = -1;
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		String strIP = jTextField_IP.getText();
		String strProxy = jTextField_Proxy.getText();
		String strUser = jTextField_User.getText();
		String strPwd = jTextField_Pwd.getText();
		int iPort = Integer.parseInt(jTextField_Port.getText());
		m_iLogonID = NetClient.Logon(strProxy, strIP, strUser, strPwd, "",
				iPort);
		appendMsg("Logon(" + m_iLogonID + ")");
	}


	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private java.awt.Canvas canvas1;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JButton jButton6;
	private javax.swing.JButton jButton7;
	private javax.swing.JButton jButton8;
	private javax.swing.JComboBox jComboBox_Channel;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextArea jTextArea_Msg;
	private javax.swing.JTextField jTextField_IP;
	private javax.swing.JTextField jTextField_Port;
	private javax.swing.JTextField jTextField_Proxy;
	private javax.swing.JTextField jTextField_Pwd;
	private javax.swing.JTextField jTextField_User;
}
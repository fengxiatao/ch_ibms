package com.netsdk.demo.frame;

import com.netsdk.common.*;
import com.netsdk.demo.module.DownLoadRecordModule;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.LLong;
import com.netsdk.lib.ToolKits;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

/*
 * 下载录像Demo
 */
class DownLoadByDataTypeFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	private Vector<String> chnlist = new Vector<String>(); 
	
	private DefaultTableModel model;
	private LLong m_hDownLoadByTimeHandle = new LLong(0);   // 按时间下载句柄
	
	private boolean b_downloadByTime = false;
    
	// 设备断线通知回调
	private DisConnect disConnect       = new DisConnect(); 
	
	// 网络连接恢复
	private static HaveReConnect haveReConnect = new HaveReConnect(); 
	
	// 开始时间
	private NetSDKLib.NET_TIME stTimeStart = new NetSDKLib.NET_TIME(); 
	
	// 结束时间
	private NetSDKLib.NET_TIME stTimeEnd = new NetSDKLib.NET_TIME();


	
	// 获取界面窗口
	private static JFrame frame = new JFrame();   
	
	public DownLoadByDataTypeFrame() {
	    setTitle(Res.string().getDownloadByDataType());
	    setLayout(new BorderLayout());
	    pack();
	    setSize(800, 300);
	    setResizable(false);
	    setLocationRelativeTo(null);
		LoginModule.init(disConnect, haveReConnect);   // 打开工程，初始化
		
    	try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	e.printStackTrace();
        } 
        	    
	    loginPanel = new LoginPanel();
		downloadRecordPanel = new DownLoadRecordPanel(); 
	
	    add(loginPanel, BorderLayout.NORTH);
		add(downloadRecordPanel, BorderLayout.CENTER);
		
	    loginPanel.addLoginBtnActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(loginPanel.checkLoginText()) {
					if(login()) {
						frame = ToolKits.getFrame(e);
						frame.setTitle(Res.string().getDownloadByDataType() + " : " + Res.string().getOnline());
					}	
				}
			}
		});
	    
	    loginPanel.addLogoutBtnActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setTitle(Res.string().getDownloadByDataType());
				logout();
			}
		});
	    
	    addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		DownLoadRecordModule.stopDownLoadRecordFile(m_hDownLoadByTimeHandle);
	    		LoginModule.logout();
	    		LoginModule.cleanup();   // 关闭工程，释放资源
	    		dispose();		
	    		
	    		SwingUtilities.invokeLater(new Runnable() {
	    			public void run() {
	    				FunctionList demo = new FunctionList();
	    				demo.setVisible(true);
	    			}
	    		});
	    	}
	    });
	}
	
	/////////////////面板//////////////////
	// 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
	private class DisConnect implements NetSDKLib.fDisConnect {
		public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
			// 断线提示
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.setTitle(Res.string().getDownloadByDataType() + " : " + Res.string().getDisConnectReconnecting());

					setButtonEnable(true);
					b_downloadByTime = false;
					downloadByTimeBtn.setText(Res.string().getDownload());
		    		DownLoadRecordModule.stopDownLoadRecordFile(m_hDownLoadByTimeHandle);
				}
			});	
		}
	}
	
	// 网络连接恢复，设备重连成功回调
	// 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
	private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
		public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
			
			// 重连提示
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.setTitle(Res.string().getDownloadRecord() + " : " + Res.string().getOnline());
				}
			});
		}
	}
	
	// 登录
	public boolean login() {
		Native.setCallbackThreadInitializer(m_DownLoadPosByTime, 
										    new CallbackThreadInitializer(false, false, "downloadbytime callback thread")); 
		if(LoginModule.login(loginPanel.ipTextArea.getText(), 
						Integer.parseInt(loginPanel.portTextArea.getText()), 
						loginPanel.nameTextArea.getText(), 
						new String(loginPanel.passwordTextArea.getPassword()))) {
			loginPanel.setButtonEnable(true);
			setButtonEnable(true);      		
			
			for(int i = 1; i < LoginModule.m_stDeviceInfo.byChanNum + 1; i++) {
				chnlist.add(Res.string().getChannel() + " " + String.valueOf(i));
			}

			
			// 登陆成功，将通道添加到控件
			chnComboBoxByTime.setModel(new DefaultComboBoxModel(chnlist));	  
		} else {
			JOptionPane.showMessageDialog(null, Res.string().getLoginFailed() + ", " + ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	//登出
	public void logout() {
		LoginModule.logout();
		loginPanel.setButtonEnable(false);
		setButtonEnable(false);

			
		for(int i = 0; i < LoginModule.m_stDeviceInfo.byChanNum; i++) {
			chnlist.clear();
		}

		chnComboBoxByTime.setModel(new DefaultComboBoxModel());

		b_downloadByTime = false;
		downloadByTimeBtn.setText(Res.string().getDownload());
	}
	
	/*
	 * 下载录像面板
	 */
	private class DownLoadRecordPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public DownLoadRecordPanel() {
			BorderEx.set(this, Res.string().getDownloadRecord(), 2);
			setLayout(new GridLayout(1, 2));
			
			downloadByTimePanel = new DownLoadByTimePanel(); // 按时间下载
			
			add(downloadByTimePanel);
		}
	}

	
	/*
	 * 按时间下载面板
	 */
	private class DownLoadByTimePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public DownLoadByTimePanel() {
			BorderEx.set(this, Res.string().getDownloadByTime(), 2);
			setLayout(new BorderLayout());
			
			downloadByTimeSetPanel = new JPanel(); // 设置
			downByTimePanel = new JPanel();  // 下载
		    
			add(downloadByTimeSetPanel, BorderLayout.NORTH);
			add(downByTimePanel, BorderLayout.CENTER);
			
			/******** 设置面板***********/
			JPanel startTimeByTime = new JPanel();
			JPanel endTimeByTime = new JPanel();
			JPanel chnByTime = new JPanel();
			JPanel streamByTime = new JPanel();
			
			downloadByTimeSetPanel.setLayout(new GridLayout(2, 2));
			
			downloadByTimeSetPanel.add(startTimeByTime);
			downloadByTimeSetPanel.add(endTimeByTime);
			downloadByTimeSetPanel.add(chnByTime);
			downloadByTimeSetPanel.add(streamByTime);
			
			// 开始时间设置
			startTimeByTime.setBorder(new EmptyBorder(5, 5, 5, 20));
			startTimeByTime.setLayout(new GridLayout(2, 1));
			JLabel startLabel = new JLabel(Res.string().getStartTime());
			dateChooserStartByTime = new DateChooserJButton();
			Dimension dimension = new Dimension();
			dimension.height = 20;
			dateChooserStartByTime.setPreferredSize(dimension);
			
			startTimeByTime.add(startLabel);
			startTimeByTime.add(dateChooserStartByTime);
		    
			// 结束时间设置
			endTimeByTime.setBorder(new EmptyBorder(5, 20, 5, 5));
			endTimeByTime.setLayout(new GridLayout(2, 1));
			JLabel endLabel = new JLabel(Res.string().getEndTime());
			dateChooserEndByTime = new DateChooserJButton();
			dateChooserEndByTime.setPreferredSize(dimension);
			
			endTimeByTime.add(endLabel);
			endTimeByTime.add(dateChooserEndByTime);
		    
		    // 通道设置
			chnByTime.setBorder(new EmptyBorder(5, 10, 0, 5));
			chnByTime.setLayout(new FlowLayout());
			chnlabel = new JLabel(Res.string().getChannel());
			chnComboBoxByTime = new JComboBox();	
			chnComboBoxByTime.setPreferredSize(new Dimension(115, 20));  
			chnByTime.add(chnlabel);
			chnByTime.add(chnComboBoxByTime);
			
			// 码流设置
			streamByTime.setBorder(new EmptyBorder(5, 10, 0, 5));
			streamByTime.setLayout(new FlowLayout());
			streamLabel = new JLabel(Res.string().getStreamType());
			String[] stream = Res.string().getDataType();
			streamComboBoxByTime = new JComboBox();	
			streamComboBoxByTime.setModel(new DefaultComboBoxModel(stream));
			streamComboBoxByTime.setPreferredSize(new Dimension(115, 20));  
			streamByTime.add(streamLabel);
			streamByTime.add(streamComboBoxByTime);

			/******** 下载面板***********/
			downByTimePanel.setLayout(new FlowLayout());
			downByTimePanel.setBorder(new EmptyBorder(0, 5, 0, 5));
		    
		    JPanel btnPanel2 = new JPanel();
		    downloadByTimeProgressBar = new JProgressBar(0, 100);
		    
		    downloadByTimeProgressBar.setPreferredSize(new Dimension(355, 20)); 
		    downloadByTimeProgressBar.setStringPainted(true);
		    
		    downByTimePanel.add(btnPanel2);
		    downByTimePanel.add(downloadByTimeProgressBar);
		    
		    // 下载按钮
		    downloadByTimeBtn = new JButton(Res.string().getDownload());
		    JLabel nullLabel = new JLabel();
		    nullLabel.setPreferredSize(new Dimension(180, 20)); 
		    downloadByTimeBtn.setPreferredSize(new Dimension(170, 20)); 
		    
		    btnPanel2.setLayout(new FlowLayout());
		    btnPanel2.add(downloadByTimeBtn);
		    btnPanel2.add(nullLabel);

		    downloadByTimeBtn.setEnabled(false);
		    downloadByTimeProgressBar.setEnabled(false);
		    chnComboBoxByTime.setEnabled(false);
		    streamComboBoxByTime.setEnabled(false);
		    dateChooserStartByTime.setEnabled(false);
		    dateChooserEndByTime.setEnabled(false);
		    
		    streamComboBoxByTime.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DownLoadRecordModule.setStreamType(streamComboBoxByTime.getSelectedIndex());
				}
			});
		    
		    downloadByTimeBtn.addActionListener(new ActionListener() {	
				@Override
				public void actionPerformed(ActionEvent e) {
					int time = 0;
					// 开始时间
					String[] dateStartByTime = dateChooserStartByTime.getText().split(" ");
					String[] dateStart1 = dateStartByTime[0].split("-");
					String[] dateStart2 = dateStartByTime[1].split(":");
					
					stTimeStart.dwYear = Integer.parseInt(dateStart1[0]);
					stTimeStart.dwMonth = Integer.parseInt(dateStart1[1]);
					stTimeStart.dwDay = Integer.parseInt(dateStart1[2]);
					
					stTimeStart.dwHour = Integer.parseInt(dateStart2[0]);
					stTimeStart.dwMinute = Integer.parseInt(dateStart2[1]);
					stTimeStart.dwSecond = Integer.parseInt(dateStart2[2]);
					
					// 结束时间
					String[] dateEndByTime = dateChooserEndByTime.getText().split(" ");
					String[] dateEnd1 = dateEndByTime[0].split("-");
					String[] dateEnd2 = dateEndByTime[1].split(":");
					
					stTimeEnd.dwYear = Integer.parseInt(dateEnd1[0]);
					stTimeEnd.dwMonth = Integer.parseInt(dateEnd1[1]);
					stTimeEnd.dwDay = Integer.parseInt(dateEnd1[2]);
					
					stTimeEnd.dwHour = Integer.parseInt(dateEnd2[0]);
					stTimeEnd.dwMinute = Integer.parseInt(dateEnd2[1]);
					stTimeEnd.dwSecond = Integer.parseInt(dateEnd2[2]);
					
					if(stTimeStart.dwYear != stTimeEnd.dwYear
					   || stTimeStart.dwMonth != stTimeEnd.dwMonth
					   || (stTimeEnd.dwDay - stTimeStart.dwDay) > 1) {
						JOptionPane.showMessageDialog(null, Res.string().getSelectTimeAgain(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
						return;				
					}
					
					if(stTimeEnd.dwDay - stTimeStart.dwDay == 1) {
						time = (24 + stTimeEnd.dwHour)*60*60 + stTimeEnd.dwMinute*60 + stTimeEnd.dwSecond -
								   stTimeStart.dwHour*60*60 - stTimeStart.dwMinute*60 - stTimeStart.dwSecond;
					} else {
						time = stTimeEnd.dwHour*60*60 + stTimeEnd.dwMinute*60 + stTimeEnd.dwSecond -
								   stTimeStart.dwHour*60*60 - stTimeStart.dwMinute*60 - stTimeStart.dwSecond;
					}
					System.out.println("time :" + time);
					if(time > 6 * 60 * 60 
					   || time <= 0) {
						JOptionPane.showMessageDialog(null, Res.string().getSelectTimeAgain(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
						return;	
					}
					
					if(!b_downloadByTime) {
						System.out.println("ByTime" + dateChooserStartByTime.getText() + "\n" + dateChooserEndByTime.getText());
					    SwingUtilities.invokeLater(new Runnable() {				
							@Override
							public void run() {
								downloadByTimeProgressBar.setValue(0);
							}
						});
						m_hDownLoadByTimeHandle = DownLoadRecordModule.downloadByDataType(chnComboBoxByTime.getSelectedIndex(),
																			streamComboBoxByTime.getSelectedIndex(),
																		    stTimeStart, 
																		    stTimeEnd, 
																		    SavePath.getSavePath().getSaveRecordFilePathWithOutPostfix(),
																			m_DownLoadDataCallBack,
																		    m_DownLoadPosByTime);
						if(m_hDownLoadByTimeHandle.longValue() != 0) {
							b_downloadByTime = true;
							downloadByTimeBtn.setText(Res.string().getStopDownload());
						    chnComboBoxByTime.setEnabled(false);
						    streamComboBoxByTime.setEnabled(false);
						    dateChooserStartByTime.setEnabled(false);
						    dateChooserEndByTime.setEnabled(false);
						} else {
							JOptionPane.showMessageDialog(null, ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
						}
					} else {
						DownLoadRecordModule.stopDownLoadRecordFile(m_hDownLoadByTimeHandle);
						b_downloadByTime = false;
						downloadByTimeBtn.setText(Res.string().getDownload());
					    chnComboBoxByTime.setEnabled(true);
					    streamComboBoxByTime.setEnabled(true);
					    dateChooserStartByTime.setEnabled(true);
					    dateChooserEndByTime.setEnabled(true);
					    
					    SwingUtilities.invokeLater(new Runnable() {				
							@Override
							public void run() {
								downloadByTimeProgressBar.setValue(0);
							}
						});
					}
				}
			});
		}
	}

	
	/*
	 * 按时间下载回调
	 */
	private DownLoadPosCallBackByTime m_DownLoadPosByTime = new DownLoadPosCallBackByTime(); // 录像下载进度
	private DownLoadDataCallBack m_DownLoadDataCallBack = DownLoadDataCallBack.getInstance(); // 录像下载进度
	class DownLoadPosCallBackByTime implements NetSDKLib.fTimeDownLoadPosCallBack{
		public void invoke(LLong lLoginID, final int dwTotalSize, final int dwDownLoadSize, int index, NetSDKLib.NET_RECORDFILE_INFO.ByValue recordfileinfo, Pointer dwUser) {	
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					System.out.println("DownloadPosCallBack dwTotalSize： " + dwTotalSize + " dwDownLoadSize： " + dwDownLoadSize);
					System.out.println("ByTime " + dwDownLoadSize + " / " + dwTotalSize);
					downloadByTimeProgressBar.setValue(dwDownLoadSize*100 / dwTotalSize);
					if(dwDownLoadSize == -1) {
						downloadByTimeProgressBar.setValue(100);
						DownLoadRecordModule.stopDownLoadRecordFile(m_hDownLoadByTimeHandle);
						b_downloadByTime = false;
						downloadByTimeBtn.setText(Res.string().getDownload());
					    chnComboBoxByTime.setEnabled(true);
					    streamComboBoxByTime.setEnabled(true);
					    dateChooserStartByTime.setEnabled(true);
					    dateChooserEndByTime.setEnabled(true);
						JOptionPane.showMessageDialog(null, Res.string().getDownloadCompleted(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
	}


	/**
	 * 下载数据回调，这里可以拿到原始的二进制码流数据
	 * 回调写成单例模式, 如果回调里需要处理数据，请另开线程
	 */
	public static class DownLoadDataCallBack implements NetSDKLib.fDataCallBack {

		private DownLoadDataCallBack() {
		}

		private static class DownloadDataCallBackHolder {
			private static final DownLoadDataCallBack dataCB = new DownLoadDataCallBack();
		}

		public static DownLoadDataCallBack getInstance() {
			return DownLoadDataCallBack.DownloadDataCallBackHolder.dataCB;
		}

		public int invoke(NetSDKLib.LLong lRealHandle, int dwDataType, Pointer pBuffer, int dwBufSize, Pointer dwUser) {

			// byte[] data = pBuffer.getByteArray(0, dwBufSize);   // 这是二进制码流数据, 如果有其他用途可以从这里取出来
			System.out.println("DownLoad DataCallBack [ " + dwDataType + " ]");
			// 不同的封装类型，回调里返回的 dwDataType 是不同的，它们遵循下面的逻辑
			if (dwDataType == (NetSDKLib.NET_DATA_CALL_BACK_VALUE + NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_PRIVATE)) {
				System.out.println("DownLoad DataCallBack [ " + dwDataType + " ]");
			} else if (dwDataType == (NetSDKLib.NET_DATA_CALL_BACK_VALUE + NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_GBPS)) {
				System.out.println("DownLoad DataCallBack [ " + dwDataType + " ]");
			} else if (dwDataType == (NetSDKLib.NET_DATA_CALL_BACK_VALUE + NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_TS)) {
				System.out.println("DownLoad DataCallBack [ " + dwDataType + " ]");
			} else if (dwDataType == (NetSDKLib.NET_DATA_CALL_BACK_VALUE + NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_MP4)) {
				System.out.println("DownLoad DataCallBack [ " + dwDataType + " ]");
			} else if (dwDataType == (NetSDKLib.NET_DATA_CALL_BACK_VALUE + NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_H264)) {
				System.out.println("DownLoad DataCallBack [ " + dwDataType + " ]");
			} else if (dwDataType == (NetSDKLib.NET_DATA_CALL_BACK_VALUE + NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_FLV_STREAM)) {
				System.out.println("DownLoad DataCallBack [ " + dwDataType + " ]");
			}
			return 0;
		}
	}

	private void setButtonEnable(boolean bln) {
		downloadByTimeBtn.setEnabled(bln);
		downloadByTimeProgressBar.setValue(0);
		downloadByTimeProgressBar.setEnabled(bln);
		chnComboBoxByTime.setEnabled(bln);
		streamComboBoxByTime.setEnabled(bln);
		dateChooserStartByTime.setEnabled(bln);
		dateChooserEndByTime.setEnabled(bln);	
	}
		
	//登录组件
	private LoginPanel loginPanel;
	// 下载
	private DownLoadRecordPanel downloadRecordPanel;
	
	// 按文件下载
	private DownLoadByTimePanel downloadByTimePanel;
	private JButton downloadByTimeBtn;
	private JProgressBar downloadByTimeProgressBar;
    private JLabel chnlabel;
    private JComboBox chnComboBoxByTime;	
    private JLabel streamLabel;
    private JComboBox streamComboBoxByTime;


	private JPanel downloadByTimeSetPanel;
	private JPanel downByTimePanel;
	
    private DateChooserJButton dateChooserStartByTime;
    private DateChooserJButton dateChooserEndByTime;
}

public class DownLoadByDataType {
	public static void main(String[] args) {	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DownLoadByDataTypeFrame demo = new DownLoadByDataTypeFrame();
				demo.setVisible(true);
			}
		});		
	}
};


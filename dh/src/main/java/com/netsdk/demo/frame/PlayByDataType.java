package com.netsdk.demo.frame;

import com.netsdk.common.BorderEx;
import com.netsdk.common.FunctionList;
import com.netsdk.common.LoginPanel;
import com.netsdk.common.Res;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.demo.module.RealPlayModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.LLong;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

class PlayByDataTypeFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private Vector<String> chnlist = new Vector<String>(); 

	private boolean isrealplayOne = false;

	// 设备断线通知回调
	private static DisConnect disConnect       = new DisConnect(); 
	
	// 网络连接恢复
	private static HaveReConnect haveReConnect = new HaveReConnect(); 
	
	// 预览句柄
	public static LLong m_hPlayHandleOne = new LLong(0);

	
	// 获取界面窗口
	private static JFrame frame = new JFrame();   
	
	public PlayByDataTypeFrame() {
	    setTitle(Res.string().getPlayByDataType());
	    setLayout(new BorderLayout());
	    pack();
	    setSize(800, 560);
	    setResizable(false);
	    setLocationRelativeTo(null);
		LoginModule.init(disConnect, haveReConnect);   // 打开工程，初始化
		
    	try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	e.printStackTrace();
        } 
	 
	    loginPanel = new LoginPanel();
	    realPanel = new JPanel();
	    
	    add(loginPanel, BorderLayout.NORTH);
	    add(realPanel, BorderLayout.CENTER);
	    
	    // 预览面板
	    realPanelOne = new RealPanelOne();
	    
	    realPanel.setLayout(new GridLayout(1,  2));
	    realPanel.add(realPanelOne);
	    
	    loginPanel.addLoginBtnActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	   	    
				if(loginPanel.checkLoginText()) {
					if(login()) {
						frame = ToolKits.getFrame(e);
						frame.setTitle(Res.string().getRealplay() + " : " + Res.string().getOnline());
					}
				}
			}
		});
	    
	    loginPanel.addLogoutBtnActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setTitle(Res.string().getRealplay());
				logout();	
			}
		});
        
	    addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		RealPlayModule.stopRealPlay(m_hPlayHandleOne);
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
	
	/////////////////面板///////////////////
	// 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
	private static class DisConnect implements NetSDKLib.fDisConnect {
		public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
			// 断线提示
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.setTitle(Res.string().getPlayByDataType() + " : " + Res.string().getDisConnectReconnecting());
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
					frame.setTitle(Res.string().getPlayByDataType() + " : " + Res.string().getOnline());
				}
			});
		}
	}
	
	// 登录
	public boolean login() {
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
			chnComboBoxOne.setModel(new DefaultComboBoxModel(chnlist));
			
		} else {
			JOptionPane.showMessageDialog(null, Res.string().getLoginFailed() + ", " + ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	// 登出
	public void logout() {
		RealPlayModule.stopRealPlay(m_hPlayHandleOne);
		LoginModule.logout();
		
		loginPanel.setButtonEnable(false);
		setButtonEnable(false);
		realPlayWindowOne.repaint();

		isrealplayOne = false;
		realplayBtnOne.setText(Res.string().getStartRealPlay());

		
		for(int i = 0; i < LoginModule.m_stDeviceInfo.byChanNum; i++) {
			chnlist.clear();
		}
		
		chnComboBoxOne.setModel(new DefaultComboBoxModel());
	}
	
	/*
	 * 预览界面通道、码流设置  以及抓图面板
	 */
	private class RealPanelOne extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public RealPanelOne() {
			BorderEx.set(this, Res.string().getRealplay(), 2);
			setLayout(new BorderLayout());
			
			channelPanelOne = new Panel();
			realplayPanelOne = new JPanel();
			
			add(channelPanelOne, BorderLayout.NORTH);
			add(realplayPanelOne, BorderLayout.CENTER);
			
			/************ 预览面板 **************/
			realplayPanelOne.setLayout(new BorderLayout());
			realplayPanelOne.setBorder(new EmptyBorder(5, 5, 5, 5));
			realPlayWindowOne = new Panel();
			realPlayWindowOne.setBackground(Color.GRAY);
			realplayPanelOne.add(realPlayWindowOne, BorderLayout.CENTER);
			
			/************ 通道、码流面板 **************/
			chnlabelOne = new JLabel(Res.string().getChn());
			chnComboBoxOne = new JComboBox();	 			

			streamLabelOne = new JLabel(Res.string().getStreamType());
			String[] stream = Res.string().getDataType();
			streamComboBoxOne = new JComboBox(stream);	 
			
			realplayBtnOne = new JButton(Res.string().getRealplay());
			
			channelPanelOne.setLayout(new FlowLayout());			
			channelPanelOne.add(chnlabelOne);
			channelPanelOne.add(chnComboBoxOne);
			channelPanelOne.add(streamLabelOne);
			channelPanelOne.add(streamComboBoxOne);
			channelPanelOne.add(realplayBtnOne);
			
			chnComboBoxOne.setPreferredSize(new Dimension(80, 20)); 
			streamComboBoxOne.setPreferredSize(new Dimension(95, 20)); 
			realplayBtnOne.setPreferredSize(new Dimension(115, 20)); 
		    
			realPlayWindowOne.setEnabled(false);
 			chnComboBoxOne.setEnabled(false);
			streamComboBoxOne.setEnabled(false);
			realplayBtnOne.setEnabled(false);
		    
			realplayBtnOne.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent e) {
					realplayOne();
				}
			});
		}
	}
	
	// 预览
	public void realplayOne() {
		if(!isrealplayOne) {
			m_hPlayHandleOne = RealPlayModule.realPlayByDataType(chnComboBoxOne.getSelectedIndex(),
				    streamComboBoxOne.getSelectedIndex(),
					realPlayWindowOne);
			if(m_hPlayHandleOne.longValue() != 0) {
				realPlayWindowOne.repaint();
				isrealplayOne = true;
				chnComboBoxOne.setEnabled(false);
				streamComboBoxOne.setEnabled(false);
				realplayBtnOne.setText(Res.string().getStopRealPlay());
			} else {
				JOptionPane.showMessageDialog(null, ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
			}
		} else {
			RealPlayModule.stopRealPlay(m_hPlayHandleOne);
			realPlayWindowOne.repaint();
			isrealplayOne = false;
			chnComboBoxOne.setEnabled(true);
			streamComboBoxOne.setEnabled(true);
			realplayBtnOne.setText(Res.string().getRealplay());
		}	
	}


	private void setButtonEnable(boolean bln) {
		realPlayWindowOne.setEnabled(bln);
		chnComboBoxOne.setEnabled(bln);
		streamComboBoxOne.setEnabled(bln);
		realplayBtnOne.setEnabled(bln);

	}
	
	/*
	 * 登录
	 */
	private LoginPanel loginPanel;	
	
    private JPanel realPanel;
    
	/*
	 * 预览
	 */
    private RealPanelOne realPanelOne;
    private JPanel realplayPanelOne;
    private Panel realPlayWindowOne;
    private Panel channelPanelOne;
    
    private JLabel chnlabelOne;
    private JComboBox chnComboBoxOne;	
    private JLabel streamLabelOne;
    private JComboBox streamComboBoxOne;
    private JButton realplayBtnOne;

}

public class PlayByDataType {
	public static void main(String[] args) {	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				PlayByDataTypeFrame demo = new PlayByDataTypeFrame();
				demo.setVisible(true);
			}
		});		
	}
}

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Threading;

namespace NetSDKDemo
{
    public partial class NetSDKDemo : Form
    {
        //Member variables
        int m_iLogonId = -1;
        int m_iConnectId = -1;

        int m_iConnChanNo = -1;
        int m_iConnStream = -1;
        private MAIN_NOTIFY_V4 MainNotify_V4 = null;

        public const int STREAM_1ST = 0;
        public const int STREAM_2ND = 1;
        public const int STREAM_3RD = 2;
        public const int FLAG_THREE_STREAM = 256;
        public const int CHANNEL_THREE_STREAM = 254;

        public const int RECORD_TYPE_PS = 0;
        public const int RECORD_TYPE_SDV = 1;

        public const int CAPTURE_TYPE_JPG = 0;
        public const int CAPTURE_TYPE_BMP = 1;

        public const int AUT_BROWSE = 0;
        public const int AUT_BROWSE_CTRL = 1;
        public const int AUT_BROWSE_CTRL_SET = 2;
        public const int AUT_ADMIN = 3;

        RECT m_tRectDraw = new RECT();
        bool m_blBtnDowm = false;


        //Constructor
        public NetSDKDemo()
        {
            InitializeComponent();
            SDK_Init();
            UI_Init();

            tabVideoParam.Parent = null;//Hide the video parameter interface
        }

        private void UI_Init()
        {
            //preview related
            cboRecordType.Items.Clear();
            cboRecordType.Items.Insert(RECORD_TYPE_PS, "PS(MP4)");
            cboRecordType.Items.Insert(RECORD_TYPE_SDV, "SDV");
            cboRecordType.SelectedIndex = RECORD_TYPE_PS;

            cboCapType.Items.Clear();
            cboCapType.Items.Insert(CAPTURE_TYPE_JPG, "JPG");
            cboCapType.Items.Insert(CAPTURE_TYPE_BMP, "BMP");
            cboCapType.SelectedIndex = CAPTURE_TYPE_JPG;

            //User Management
            cboAuthority.Items.Clear();
            cboAuthority.Items.Insert(AUT_BROWSE, "AUT_BROWSE");
            cboAuthority.Items.Insert(AUT_BROWSE_CTRL, "AUT_BROWSE_CTRL");
            cboAuthority.Items.Insert(AUT_BROWSE_CTRL_SET, "AUT_BROWSE_CTRL_SET");
            cboAuthority.Items.Insert(AUT_ADMIN, "AUT_ADMIN");
            cboAuthority.SelectedIndex = AUT_ADMIN;
        }

        // main callback
        private void MainNotify(Int32 _ulLogonID, Int32 _iWparam, IntPtr _iLParam, IntPtr _iUser)
        {
            int iMsg = _iWparam & 0xffff;
            switch (iMsg)
            {
            case NetSDKMsg.WCM_LOGON_NOTIFY:
                {
                    Int32 iStatus = _iLParam.ToInt32();
                    if (NetSDKMsg.LOGON_SUCCESS == iStatus)
                    {
                        m_iLogonId = _ulLogonID;
                        MessageBox.Show("logon success!");                  
                    }
                    else
                    {
                        MessageBox.Show("logon failed, reason " + iStatus);
                    }                  
                }
                break;
            default:
                break;
            }         
        }

        //Rewrite the message handler to handle custom messages
        protected override void DefWndProc(ref System.Windows.Forms.Message m)
        {
            //WM_MAIN_MESSAGE is a custom system message
            if (m.Msg == NetSDKMsg.WM_MAIN_MESSAGE)
            {
                //custom message handler
                this.OnNetSDKMsg(m.WParam, m.LParam);
            }
            //default message handler
            base.DefWndProc(ref m);
        }

        public void OnNetSDKMsg(IntPtr wParam, IntPtr lParam)
        {
            //The lower 16 bits of wParam are the type of the message;
            int iMsgType = wParam.ToInt32() & 0xFFFF;
            switch (iMsgType)
            {
                case NetSDKMsg.WCM_LOGON_NOTIFY:
                    {
                        Int32 iStatus = wParam.ToInt32() >> 16;
                        if (NetSDKMsg.LOGON_SUCCESS == iStatus)
                        {
                            MessageBox.Show("logon success!");
                            m_iLogonId = (Int32)lParam;

                            //Initialize the channel list
                            Int32 iChanNum = 0;
                            NVSSDK.NetClient_GetChannelNum(m_iLogonId, ref iChanNum);
                            cboChanList.Items.Clear();
                            for (Int32 i = 0; i < iChanNum; ++i)
                            {
                                cboChanList.Items.Add((i+1).ToString());
                            }
                            if (cboChanList.Items.Count > 0)
                            {
                                cboChanList.SelectedIndex = 0;
                            }
                            btnLogon.Text = "Logoff";

                            //Get NTP information after successful login
                            GetNtpInfo();
                        }
                        else
                        {
                            MessageBox.Show("logon failed, reason " + iStatus);
                        } 
                    }
                    break;
                case NetSDKMsg.WCM_VIDEO_HEAD_EX:
                    {
                        //The video header message is here, start playing the video
                        NVSSDK.NetClient_StopPlay(m_iConnectId);
                        RECT rect = new RECT();
                        NVSSDK.NetClient_StartPlay(m_iConnectId, panelVideoShow.Handle, rect, 0);
                    }
                    break;
                default:
                    break;
            } 
        }

        //SDK initialization
        private void SDK_Init()
        {
            NVSSDK.NetClient_Startup_V4(0, 0, 0);
            //Here SDK messages are processed through system messages
            NVSSDK.NetClient_SetMSGHandleEx(NetSDKMsg.WM_MAIN_MESSAGE, this.Handle, NetSDKMsg.MSG_PARACHG, NetSDKMsg.MSG_ALARM);

            //You can also use the back method to process sdk messages
            MainNotify_V4 = MainNotify;
            //NVSSDK.NetClient_SetNotifyFunction_V4(MainNotify_V4, null, null, null, null);
        }

        //original stream callback function
        private void Notify_RawFrame(UInt32 _ulID, IntPtr _cData, int _iLen, ref RAWFRAME_INFO _pRawFrameInfo, IntPtr _iUser)
        {
            Int32 iConnId = (Int32)_ulID;
            if (iConnId != m_iConnectId)
            {
                return;
            }

            if (null == _cData || _iLen <= 0)
            {
                return;
            }

            if (0 == _pRawFrameInfo.nType) //I frame
            {
                //do something
            }
            else if (1 == _pRawFrameInfo.nType) //P frame
            {
                //do something
            }
            else if (5 == _pRawFrameInfo.nType) //audio data
            {
                //do something
            }
        }

        //connect video
        private void ConnectVideo(Int32 _iStream)
        {
            if (m_iLogonId < 0)
            {
                MessageBox.Show("Please logon device first!");
                return;
            }
            // disconnect existing connection
            DisConnectVideo();

            CLIENTINFO tInfo = new CLIENTINFO();
            tInfo.m_iServerID = m_iLogonId;
            tInfo.m_iTimeout = 20;
            tInfo.m_iNetMode = ConnectNetMode.NETMODE_TCP; //default TCP connection
            tInfo.m_iChannelNo = cboChanList.SelectedIndex;
            tInfo.m_iStreamNO = _iStream;          
            if (STREAM_3RD == _iStream)
            {
                tInfo.m_iStreamNO = STREAM_2ND;
                tInfo.m_iFlag = FLAG_THREE_STREAM;
            }
            tInfo.m_cNetFile = new char[255];
            tInfo.m_cRemoteIP = new char[16];
            Array.Copy(textIP.Text.ToCharArray(), tInfo.m_cRemoteIP, textIP.Text.Length);

            //Start to receive one channel of video data. After receiving the data, MainNotify has a message callback, and then the video can be played.
            UInt32 uiConID = 0;
            int iRet = NVSSDK.NetClient_StartRecv(ref uiConID, ref tInfo, null);
            if (iRet < 0)
            {
                MessageBox.Show("Connect video failed, ret:" + iRet);
                return;
            }
            m_iConnectId = (Int32)uiConID;
            m_iConnChanNo = tInfo.m_iChannelNo;
            m_iConnStream = _iStream;

            //Set the original stream callback
            //NVSSDK.NetClient_SetRawFrameCallBack(m_iConnectId, Notify_RawFrame, IntPtr.Zero);
        }

        //Disconnect
        private void DisConnectVideo()
        {
            btnRecord.Text = "StartRec";
            btn3DSet.Text = "Start";
            if (m_iConnectId < 0)
            {
                return;
            }
            //stop recording
            NVSSDK.NetClient_StopCaptureFile(m_iConnectId);

            //Disconnect
            NVSSDK.NetClient_StopRecv(m_iConnectId);
            m_iConnectId = -1;
        }

        //Get the current time string
         private String GetCurTimeStr()
         {
             System.DateTime dtCutTime = new System.DateTime();
             dtCutTime = System.DateTime.Now;

             string strTime = dtCutTime.Year.ToString() + dtCutTime.Month.ToString("D2") + dtCutTime.Day.ToString("D2") + 
                              dtCutTime.Hour.ToString("D2") + dtCutTime.Second.ToString("D2") + dtCutTime.Minute.ToString("D2");
             return strTime;
         }

        //Log in to the device
        private void btnLogon_Click(object sender, EventArgs e)
        {
            if ("Logon" == btnLogon.Text) //login
            {
                LogonPara tInfo = new LogonPara();
                tInfo.iSize = Marshal.SizeOf(tInfo);
                tInfo.iNvsPort = Int32.Parse(textPort.Text);
                tInfo.cNvsIP = new char[32];
                Array.Copy(textIP.Text.ToCharArray(), tInfo.cNvsIP, textIP.Text.Length);
                tInfo.cUserName = new char[16];
                Array.Copy(textUser.Text.ToCharArray(), tInfo.cUserName, textUser.Text.Length);
                tInfo.cUserPwd = new char[16];
                Array.Copy(textPwd.Text.ToCharArray(), tInfo.cUserPwd, textPwd.Text.Length);

                IntPtr intptr = Marshal.AllocCoTaskMem(tInfo.iSize);
                Marshal.StructureToPtr(tInfo, intptr, true);//false is easy to cause memory leaks
                Int32 iLogonId = NVSSDK.NetClient_Logon_V4(0, intptr, tInfo.iSize);
                Marshal.FreeHGlobal(intptr);//Free the allocated unmanaged memory.
                if (iLogonId < 0)
                {
                    MessageBox.Show("logon failed!");
                    return;
                }
            }
            else //logout
            {
                btnLogon.Text = "Logon";
                if (m_iLogonId < 0)
                {
                    return;
                }

                // disconnect the video connection
                DisConnectVideo();

                //reply button status

                NVSSDK.NetClient_Logoff(m_iLogonId);
                m_iLogonId = -1;
            }
        }

        //connect to the main stream
        private void btnConn1st_Click(object sender, EventArgs e)
        {
            ConnectVideo(STREAM_1ST);
        }

        //connect the secondary stream
        private void btnConn2nd_Click(object sender, EventArgs e)
        {
            ConnectVideo(STREAM_2ND);
        }

        //connect three streams
        private void btnConn3rd_Click(object sender, EventArgs e)
        {
            ConnectVideo(STREAM_3RD);
        }

        // disconnect the video connection
        private void btnDisconn_Click(object sender, EventArgs e)
        {
            DisConnectVideo();
        }

        // start & stop recording
        private void btnRecord_Click(object sender, EventArgs e)
        {
            if (m_iConnectId < 0)
            {
                return; //No video connection is allowed to record
            }

            if ("StartRec" == btnRecord.Text)
            {
                //record save path
                String strFileName = "Record\\";
                if (!System.IO.Directory.Exists(strFileName))
                {
                    System.IO.Directory.CreateDirectory(strFileName);
                }
                strFileName += GetCurTimeStr();
                
                //record type
                int iType = cboRecordType.SelectedIndex;
                if (RECORD_TYPE_PS == iType)
                {
                    iType = NetSDKType.REC_FILE_TYPE_PS;
                    strFileName += ".ps";
                }
                else if (RECORD_TYPE_SDV == iType)
                {
                    iType = NetSDKType.REC_FILE_TYPE_NORMAL;
                    strFileName += ".sdv";
                }
                else
                {   
                    return;
                }
                int iRet = NVSSDK.NetClient_StartCaptureFile(m_iConnectId, strFileName, iType);
                if (iRet >= 0)
                {
                    btnRecord.Text = "StopRec";
                }
            }
            else
            {
                btnRecord.Text = "StartRec";
                NVSSDK.NetClient_StopCaptureFile(m_iConnectId);
            }
        }

        // capture picture
        private void btnCapPic_Click(object sender, EventArgs e)
        {
            if (m_iConnectId < 0)
            {
                return;
            }

            // Capture image save path
            String strFileName = "Capture\\";
            if (!System.IO.Directory.Exists(strFileName))
            {
                System.IO.Directory.CreateDirectory(strFileName);
            }
            strFileName += GetCurTimeStr();

            //image type
            int iType = cboCapType.SelectedIndex;
            if (CAPTURE_TYPE_JPG == iType)
            {
                iType = NetSDKType.CAPTURE_PICTURE_TYPE_JPG;
                strFileName += ".jpg";
            }
            else if (CAPTURE_TYPE_BMP == iType)
            {
                iType = NetSDKType.CAPTURE_PICTURE_TYPE_BMP;
                strFileName += ".bmp";
            }
            else
            {
                return;
            }

            int iRet = NVSSDK.NetClient_CapturePicture(m_iConnectId, iType, strFileName);
            if (iRet >= 0)
            {
                MessageBox.Show("Capture picture success!");
            }
            else
            {
                MessageBox.Show("Capture picture failed!");
            }
        }

        //3D positioning
        private void btn3DSet_Click(object sender, EventArgs e)
        {
            if ("Start" == btn3DSet.Text)
            {
                btn3DSet.Text = "Stop";
            }
            else
            {
                btn3DSet.Text = "Start";
            }
        }

        private void panelVideoShow_MouseDown(object sender, MouseEventArgs e)
        {
            if ("Start" == btn3DSet.Text || m_iConnectId < 0)
            {
                return;
            }
            m_tRectDraw.left = e.X;
            m_tRectDraw.top = e.Y;
            m_blBtnDowm = true;
        }

        private void panelVideoShow_MouseMove(object sender, MouseEventArgs e)
        {
            if ("Start" == btn3DSet.Text || m_iConnectId < 0 || !m_blBtnDowm)
            {
                return;
            }
            m_tRectDraw.right = e.X;
            m_tRectDraw.bottom = e.Y;

            RECT rc = new RECT();
            rc = m_tRectDraw;
            if (m_tRectDraw.left > m_tRectDraw.right)
            {
                rc.left = m_tRectDraw.right;
                rc.right = m_tRectDraw.left;
            }

            if (m_tRectDraw.top > m_tRectDraw.bottom)
            {
                rc.top = m_tRectDraw.bottom;
                rc.bottom = m_tRectDraw.top;
            }

            int iVWidth = 0, iVHeight = 0;
            int iStream = m_iConnStream;
            if (STREAM_3RD == iStream)
            {
                iStream = CHANNEL_THREE_STREAM;
            }
            NVSSDK.NetClient_GetVideoSize(m_iLogonId, m_iConnChanNo, ref iVWidth, ref iVHeight, iStream);

            rc.left = rc.left * iVWidth / panelVideoShow.Width;
            rc.right = rc.right * iVWidth / panelVideoShow.Width;
            rc.top = rc.top * iVHeight / panelVideoShow.Height;
            rc.bottom = rc.bottom * iVHeight / panelVideoShow.Height;

            NVSSDK.NetClient_DrawRectOnLocalVideo(m_iConnectId, ref rc, 1);
        }

        private void panelVideoShow_MouseUp(object sender, MouseEventArgs e)
        {
            m_blBtnDowm = false;
            if ("Start" == btn3DSet.Text || m_iConnectId < 0)
            {
                return;
            }
            //stop drawing
            RECT rc3D = new RECT();
            NVSSDK.NetClient_DrawRectOnLocalVideo(m_iConnectId, ref rc3D, 0);

            //Send 3D coordinates and convert to ten thousand points
            rc3D = m_tRectDraw;
            rc3D.left = (m_tRectDraw.left * 10000) / panelVideoShow.Width;
            rc3D.right = (m_tRectDraw.right * 10000) / panelVideoShow.Width;
            rc3D.top = (m_tRectDraw.top * 10000) / panelVideoShow.Width;
            rc3D.bottom = (m_tRectDraw.bottom * 10000) / panelVideoShow.Width;
       
            Locate3DPosition t3dInfo = new Locate3DPosition();
            t3dInfo.tPoint = new vca_TPoint[2];
            t3dInfo.iBufSize = Marshal.SizeOf(t3dInfo);

            if (rc3D.left == rc3D.right && rc3D.top == rc3D.bottom)
            { // draw 1 point
                t3dInfo.iPointNum = 1;
                t3dInfo.tPoint[0].iX = rc3D.left;
                t3dInfo.tPoint[0].iY = rc3D.top;
            }
            else // draw a rectangle
            {
                t3dInfo.iPointNum = 2;
                t3dInfo.tPoint[0].iX = rc3D.left;
                t3dInfo.tPoint[0].iY = rc3D.top;
                t3dInfo.tPoint[1].iX = rc3D.right;
                t3dInfo.tPoint[1].iY = rc3D.bottom;
            }

            IntPtr intptr = Marshal.AllocCoTaskMem(t3dInfo.iBufSize);
            Marshal.StructureToPtr(t3dInfo, intptr, true);     
            NVSSDK.NetClient_SendCommand(m_iLogonId, NetSDKCmd.COMMAND_ID_3D_POSITION, m_iConnChanNo, intptr, t3dInfo.iBufSize);
            Marshal.FreeHGlobal(intptr);
        }

        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        /*
                                                                Demarcation line, PTZ control
         */
        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        //Control the device through the protocol
        //_iAction action code
        //_iParam1 horizontal speed or preset number
        //_iParam2 vertical speed
        private int ProtocalControl(int _iAction, int _iParam1, int _iParam2)
        {
            //The parameters of the electronic PTZ, control type (Normal, e_PTZ)
            int iControlType = chkEPTZ.Checked == true ? 1 : 0;

            //Control the action of the device connected to a channel of the network video server
            int iRet = NVSSDK.NetClient_DeviceCtrlEx(m_iLogonId, cboChanList.SelectedIndex, _iAction, _iParam1, _iParam2, iControlType);
            if (iRet < 0)//Device control failed
            {
                MessageBox.Show("NetClient_DeviceCtrlEx failed!");
            }
            return iRet;
        }

        private int PtzControl(int _iAction)
        {
            int iRet = -1;
            if (m_iConnectId < 0)
            {
                MessageBox.Show("Please connect video first!");
                return iRet;
            }

            int iWorkMode = 0; //0 protocol mode, 1 transparent channel mode, default protocol mode here
            int iParam1 = 0;
            int iParam2 = 0;
            int iSpeed = trckSpeed.Value;
            switch (_iAction)
            {
            case ActionControlMsg.MOVE_UP://move up
                iParam1 = 0;
                iParam2 = iSpeed;
                //MOVE transparent channel move code
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.MOVE_DOWN://move down
                iParam1 = 0;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.MOVE_LEFT://move left
                iParam1 = iSpeed;
                iParam2 = 0;
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.MOVE_RIGHT://move right
                iParam1 = iSpeed;
                iParam2 = 0;
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.MOVE_UP_LEFT://move up and left
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.MOVE_UP_RIGHT://move up and right
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.MOVE_DOWN_LEFT://move down left
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.MOVE_DOWN_RIGHT://move down right
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                break;
            case ActionControlMsg.ZOOM_BIG://Zoom
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 31;
                break;
            case ActionControlMsg.ZOOM_SMALL://Zoom small
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 33;
                break;
            case ActionControlMsg.FOCUS_NEAR://Focus Near
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 37;
                break;
            case ActionControlMsg.FOCUS_FAR://Focus far
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 35;
                break;
            case ActionControlMsg.IRIS_OPEN://Iris open
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 39;
                break;
            case ActionControlMsg.IRIS_CLOSE://Iris close
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 41;
                break;
            case ActionControlMsg.RAIN_ON://wiper on
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 47;
                break;
            case ActionControlMsg.RAIN_OFF://wiper off
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 48;
                break;
            case ActionControlMsg.LIGHT_ON://backlight on
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 43;
                break;
            case ActionControlMsg.LIGHT_OFF://backlight off
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 44;
                break;
            case ActionControlMsg.HOR_AUTO://Auto cruise
                iParam1 = 0;
                iParam2 = 0;
                _iAction = iWorkMode == 0 ? _iAction : 21;
                break;
            case ActionControlMsg.HOR_AUTO_STOP://stop auto cruise
                iParam1 = 0;
                iParam2 = 0;
                _iAction = iWorkMode == 0 ? _iAction : 22;
                break;
            case ActionControlMsg.CALL_VIEW://call preset
                iParam1 = Int32.Parse(textPreset.Text);
                iParam2 = 0;
                _iAction = iWorkMode == 0 ? _iAction : 62;
                break;
            case ActionControlMsg.SET_VIEW://Set preset position
                iParam1 = Int32.Parse(textPreset.Text);
                iParam2 = 0;
                _iAction = iWorkMode == 0 ? _iAction : 63;
                break;
            case ActionControlMsg.POWER_ON://Power on
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 45;
                break;
            case ActionControlMsg.POWER_OFF://Power off
                iParam1 = iSpeed;
                iParam2 = iSpeed;
                _iAction = iWorkMode == 0 ? _iAction : 46;
                break;
            default:
                iParam1 = 0;
                iParam2 = 0;
                //9 Protocol control stop code
                _iAction = iWorkMode == 0 ? 9 : _iAction;
                break;
            }

            //Control the device through the protocol
            iRet = ProtocalControl(_iAction, iParam1, iParam2);
            return iRet;
        }

        //move control handler
        private void MOVE_MouseDown(object sender, MouseEventArgs e)
        {
            Button btnControl = (Button)sender;
            btnMoveAuto.Text = "Auto";

            //Get the control code in the Tag property
            int iAction = Int32.Parse(btnControl.Tag.ToString());

            //call the move handler
            PtzControl(iAction);
        }

        //stop move handler
        private void MOVE_STOP_MouseUp(object sender, MouseEventArgs e)
        {
            //stop the move operation
            PtzControl(ActionControlMsg.MOVE_STOP);
        }

        //Auto key in control
        private void btnMoveAuto_Click(object sender, EventArgs e)
        {
            //Auto cruise operation
            if (btnMoveAuto.Text == "Auto") //Auto state
            {
                int iRet = PtzControl(ActionControlMsg.HOR_AUTO);
                if (iRet == 0)
                {
                    btnMoveAuto.Text = "Stop";
                }
            }
            else // is the Stop state
            {
                int iRet = PtzControl(ActionControlMsg.HOR_AUTO_STOP);
                if (iRet == 0)
                {
                    btnMoveAuto.Text = "Auto";
                }
            }
        }

        //control the speed
        private void trckSpeed_ValueChanged(object sender, EventArgs e)
        {
            lblSpeed.Text = ((TrackBar)sender).Value.ToString();
        }

        //stop zoom operation
        private void btnZoomBig_MouseUp(object sender, MouseEventArgs e)
        {
            PtzControl(ActionControlMsg.ZOOM_BIG_STOP);
        }

        //Stop zooming small operation
        private void btnZoomSmall_MouseUp(object sender, MouseEventArgs e)
        {
            PtzControl(ActionControlMsg.ZOOM_SMALL_STOP);
        }

        //Stop opening the aperture operation
        private void btnIrisOpen_MouseUp(object sender, MouseEventArgs e)
        {
            PtzControl(ActionControlMsg.IRIS_OPEN_STOP);
        }

        //Stop closing the aperture operation
        private void btnIrisClose_MouseUp(object sender, MouseEventArgs e)
        {
            PtzControl(ActionControlMsg.IRIS_CLOSE_STOP);
        }

        //Stop focus near operation
        private void btnFocusNear_MouseUp(object sender, MouseEventArgs e)
        {
            PtzControl(ActionControlMsg.FOCUS_NEAR_STOP);
        }

        //Stop focus far operation
        private void btnFocusFar_MouseUp(object sender, MouseEventArgs e)
        {
            PtzControl(ActionControlMsg.FOCUS_FAR_STOP);
        }

        //power supply
        private void chkPower_CheckedChanged(object sender, EventArgs e)
        {
            PtzControl(chkPower.Checked ? ActionControlMsg.POWER_ON : ActionControlMsg.POWER_OFF);
        }

        //backlight
        private void chkLight_CheckedChanged(object sender, EventArgs e)
        {
            PtzControl(chkLight.Checked ? ActionControlMsg.LIGHT_ON : ActionControlMsg.LIGHT_OFF);
        }

        // wiper
        private void chkRain_CheckedChanged(object sender, EventArgs e)
        {
            PtzControl(chkRain.Checked ? ActionControlMsg.RAIN_ON : ActionControlMsg.RAIN_OFF);
        }

        //call preset
        private void btnGotoPreset_Click(object sender, EventArgs e)
        {
            int iPreset = 0;
            try
            {
                // Perform preset type conversion operation
                iPreset = Int32.Parse(textPreset.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            if (iPreset < 1 || iPreset > 255)
            {
                MessageBox.Show("Preset number should between 1 and 255.");
                return;
            }

            //Call the function DevControl to call the preset operation
            PtzControl(ActionControlMsg.CALL_VIEW);
        }

        //set preset position
        private void btnSetPreset_Click(object sender, EventArgs e)
        {
            int iPreset = 0;
            try
            {
                // Perform preset type conversion operation
                iPreset = Int32.Parse(textPreset.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }
            if (iPreset < 1 || iPreset > 255)
            {
                MessageBox.Show("Preset number should between 1 and 255.");
                return;
            }

            PtzControl(ActionControlMsg.SET_VIEW);
        }

        private void btnAssistantON_Click(object sender, EventArgs e)
        {
            int iAssistantNo = 0;
            try
            {
                iAssistantNo = Int32.Parse(textAssistant.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            //Only accept input values ??between 1--8
            if (iAssistantNo < 1 || iAssistantNo > 8)
            {
                MessageBox.Show("Assistant number should be between 1 and 8.");
                return;
            }
            ProtocalControl(33, iAssistantNo, trckSpeed.Value);
        }

        private void btnAssistantOFF_Click(object sender, EventArgs e)
        {
            int iAssistantNo = 0;
            try
            {
                iAssistantNo = Int32.Parse(textAssistant.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            //Only accept input values ??between 1--8
            if (iAssistantNo < 1 || iAssistantNo > 8)
            {
                MessageBox.Show("Assistant number should be between 1 and 8.");
                return;
            }
            ProtocalControl(34, iAssistantNo, trckSpeed.Value);
        }


        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        /*
                                                                dividing line, video parameters
         */
        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        private void SetVideoParam()
        {
            if (m_iConnectId < 0)
            {
                MessageBox.Show("Please connect video first!");
                return;
            }

            //Create a video parameter structure
            STR_VideoParam tParam = new STR_VideoParam();
            tParam.m_usHue = (UInt16)trckHue.Value;
            tParam.m_ustBrightness = (UInt16)trckBrightness.Value;
            tParam.m_ustContrast = (UInt16)trckContrast.Value;
            tParam.m_ustSaturation = (UInt16)trckSaturation.Value;

            //Set the video display parameters of a certain channel of the network video server
            int iRet = NVSSDK.NetClient_SetVideoPara(m_iLogonId, cboChanList.SelectedIndex, ref tParam);
            if (iRet < 0)
            {
                MessageBox.Show("Set video param failed!");
            }
        }

        //default parameters
        private void btnVideoParamDefult_Click(object sender, EventArgs e)
        {
            trckHue.Value = 128;
            trckBrightness.Value = 128;
            trckContrast.Value = 128;
            trckSaturation.Value = 128;
        }

        //modify the contrast
        private void trckContrast_ValueChanged(object sender, EventArgs e)
        {
            lblContrast.Text = ((TrackBar)sender).Value.ToString();
            SetVideoParam();
        }

        //modify brightness
        private void trckBrightness_ValueChanged(object sender, EventArgs e)
        {
            lblBrightness.Text = ((TrackBar)sender).Value.ToString();
            SetVideoParam();
        }
        
        //modify saturation
        private void trckSaturation_ValueChanged(object sender, EventArgs e)
        {
            lblSaturation.Text = ((TrackBar)sender).Value.ToString();
            SetVideoParam();
        }

        //modify chromaticity
        private void trckHue_ValueChanged(object sender, EventArgs e)
        {
            lblHue.Text = ((TrackBar)sender).Value.ToString();
            SetVideoParam();
        }   

        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        /*
                                                                demarcation line, user management
         */
        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        private void UserMangeQuery()
        {
            if (m_iLogonId < 0)
            {
                MessageBox.Show("Please logon device first!");
                return;
            }
            cboUserList.Items.Clear();

            // first query the total number of users
	        int iUserNum = 0;
            NVSSDK.NetClient_GetUserNum(m_iLogonId,  ref iUserNum);

            //Query user information
            int iAuthority = 0;
            IntPtr ptrUser = Marshal.AllocCoTaskMem(16);
            IntPtr ptrPswd = Marshal.AllocCoTaskMem(16);
	        for(int i = 0; i < iUserNum; i++)
            {
                if (0 == NVSSDK.NetClient_GetUserInfo(m_iLogonId, i, ptrUser, ptrPswd, ref iAuthority))
		        {
                    String strUserName = Marshal.PtrToStringAnsi(ptrUser);
                    iAuthority--;
                    if (AUT_BROWSE == iAuthority) 
                    {
                        strUserName += ";AUT_BROWSE";
                    } 
                    else if (AUT_BROWSE_CTRL == iAuthority) 
                    {
                        strUserName += ";AUT_BROWSE_CTRL";
                    }
                    else if (AUT_BROWSE_CTRL_SET == iAuthority)
                    {
                        strUserName += ";AUT_BROWSE_CTRL_SET";
                    }
                    else if (AUT_ADMIN == iAuthority)
                    {
                        strUserName += ";AUT_ADMIN";
                    }
                    cboUserList.Items.Add(strUserName);
		        }                
	        }
            Marshal.FreeHGlobal(ptrUser);
            Marshal.FreeHGlobal(ptrPswd);

            if (iUserNum > 0)
            {
                cboUserList.SelectedIndex = 0;
            }      
        }

        // query user
        private void btnUserQuery_Click(object sender, EventArgs e)
        {
            UserMangeQuery();
        }

        //Add user
        private void btnUserAdd_Click(object sender, EventArgs e)
        {
            if (m_iLogonId < 0)
            {
                MessageBox.Show("Please logon device first!");
                return;
            }

            String strUserName = textUMName.Text;
            String strPassword = textUMPswd.Text;
            String strConfirm = UMConfirmPswd.Text;
            if ("" == strUserName)
            {
                MessageBox.Show("Please input user name!");
                return;
            }

            if ("" == strPassword)
            {
                MessageBox.Show("Please input password!");
                return;
            }

            if (strPassword != strConfirm)
            {
                MessageBox.Show("Password is different from the first input!");
                return;
            }
            int iAuthority = cboAuthority.SelectedIndex + 1;
            NVSSDK.NetClient_AddUser(m_iLogonId, strUserName, strPassword, iAuthority);

            Thread.Sleep(200);

            UserMangeQuery();
        }

        //modify user
        private void btnUserModify_Click(object sender, EventArgs e)
        {
            if (m_iLogonId < 0)
            {
                MessageBox.Show("Please logon device first!");
                return;
            }

            String strUserName = cboUserList.Text;
            if ("" == strUserName)
            {
                MessageBox.Show("Please select a user!");
                return;
            }

            int iPos = strUserName.LastIndexOf(";");
            if (iPos > 0)
            {
                strUserName = strUserName.Substring(0, iPos);
            }

            String strPassword = textUMPswd.Text;
            String strConfirm = UMConfirmPswd.Text;
            String strOldPswd = textUMOldPswd.Text;       
            if ("" == strPassword)
            {
                MessageBox.Show("Please input new password!");
                return;
            }
            if (strPassword != strConfirm)
            {
                MessageBox.Show("Password is different from the first input!");
                return;
            }
            if ("" == strPassword)
            {
                MessageBox.Show("Please input old password!");
                return;
            }

            // get old password
            int iAuthority = 0;
            IntPtr ptrUser = Marshal.AllocCoTaskMem(16);
            IntPtr ptrPswd = Marshal.AllocCoTaskMem(16);         
            NVSSDK.NetClient_GetUserInfo(m_iLogonId, cboUserList.SelectedIndex, ptrUser, ptrPswd, ref iAuthority);
            String strCmpPswd = Marshal.PtrToStringAnsi(ptrPswd);

            Marshal.FreeHGlobal(ptrUser);
            Marshal.FreeHGlobal(ptrPswd);
            if (strCmpPswd != strOldPswd)
            {
                MessageBox.Show("Please input correct old password!");
                return;
            }

            NVSSDK.NetClient_ModifyPwd(m_iLogonId, strUserName, strPassword);

            UserMangeQuery();
        }

        //delete users
        private void btnUserDelete_Click(object sender, EventArgs e)
        {
            if (m_iLogonId < 0)
            {
                MessageBox.Show("Please logon device first!");
                return;
            }

            String strUserName = cboUserList.Text;
            if ("" == strUserName)
            {
                MessageBox.Show("Please select a user!");
                return;
            }

            int iPos = strUserName.LastIndexOf(";");
            if (iPos > 0)
            {
                strUserName = strUserName.Substring(0, iPos);
            }
            if ("Admin" == strUserName)
            {
                MessageBox.Show("Unbale to delete Admin user!");
                return;
            }

            NVSSDK.NetClient_DelUser(m_iLogonId, strUserName);

            UserMangeQuery();
        }


        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        /*
                                                                dividing line, NTP time
         */
        /*************************************************************************************************************************************/
        /*************************************************************************************************************************************/
        //NTP information settings
        private void btnNTPSet_Click(object sender, EventArgs e)
        {
            if (m_iLogonId < 0)
            {
                MessageBox.Show("Please logon device first!");
                return;
            }

            NTPInfo tInfo = new NTPInfo();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.cServerAddress = new char[32];
            //server address
            String strServer = textNTPServer.Text;
            if ("" == strServer)
            {
                MessageBox.Show("Please input NTP server address!");
                return;
            }
            Array.Copy(strServer.ToCharArray(), tInfo.cServerAddress, strServer.Length);
            //port
            tInfo.iServerPort = Int32.Parse(textNTPPort.Text);
            if (tInfo.iServerPort < 1 || tInfo.iServerPort > 65535)
            {
                MessageBox.Show("Please input NTP port between 1 and 65535!");
                return;
            }
            //time interval
            Int32 iInterval = Int32.Parse(textNTPInterval.Text);
            if (iInterval < 1 || iInterval > 1440)
            {
                MessageBox.Show("Please input NTP interval between 1 and 1440!");
                return;
            }
            tInfo.iIntervalHour = iInterval/60;
            if (tInfo.iIntervalHour <= 0)
            {
                tInfo.iIntervalHour = 1;
            }
            tInfo.iIntervalSec = iInterval*60;

            IntPtr intptr = Marshal.AllocCoTaskMem(tInfo.iSize);
            Marshal.StructureToPtr(tInfo, intptr, true);
            NVSSDK.NetClient_SetDevConfig(m_iLogonId, NetSDKCmd.NET_CLIENT_NTP_INFO, 0, intptr, tInfo.iSize);
            Marshal.FreeHGlobal(intptr);
        }

        public void GetNtpInfo()
        {
            if (m_iLogonId < 0)
            {
                MessageBox.Show("Please logon device first!");
                return;
            }

            //Get NTP parameters
            NTPInfo tInfo = new NTPInfo();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.cServerAddress = new char[32];

            int iOut = 0;
            IntPtr intptr = Marshal.AllocCoTaskMem(tInfo.iSize);
            Marshal.StructureToPtr(tInfo, intptr, true);
            NVSSDK.NetClient_GetDevConfig(m_iLogonId, NetSDKCmd.NET_CLIENT_NTP_INFO, 0, intptr, tInfo.iSize, ref iOut);
            tInfo = (NTPInfo)Marshal.PtrToStructure(intptr, typeof(NTPInfo));
            Marshal.FreeHGlobal(intptr);

            //
            string strServer = new string(tInfo.cServerAddress, 0, tInfo.cServerAddress.Length);
            strServer = strServer.Trim("\0".ToCharArray());
            textNTPServer.Text = strServer;
            textNTPPort.Text = tInfo.iServerPort.ToString();
            if (tInfo.iIntervalSec <= 0)
            {
                textNTPInterval.Text = (tInfo.iIntervalHour * 60).ToString();
            }
            else
            {
                textNTPInterval.Text = (tInfo.iIntervalSec / 60).ToString();
            }
        }

        //Get NTP information
        private void btnNTPGet_Click(object sender, EventArgs e)
        {
            GetNtpInfo();
        }
    }
}

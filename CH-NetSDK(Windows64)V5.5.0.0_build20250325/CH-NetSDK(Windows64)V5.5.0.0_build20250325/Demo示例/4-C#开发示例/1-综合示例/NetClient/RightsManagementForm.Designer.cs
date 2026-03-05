namespace NetClient
{
    partial class RightsManagementForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.labelUserList = new System.Windows.Forms.Label();
            this.cboUserList = new System.Windows.Forms.ComboBox();
            this.labelLocal = new System.Windows.Forms.Label();
            this.chkLocalAll = new System.Windows.Forms.CheckBox();
            this.chkManual = new System.Windows.Forms.CheckBox();
            this.chkReboot = new System.Windows.Forms.CheckBox();
            this.chkLogSearch = new System.Windows.Forms.CheckBox();
            this.chkAlarm = new System.Windows.Forms.CheckBox();
            this.chkManageChannels = new System.Windows.Forms.CheckBox();
            this.chkParaSet = new System.Windows.Forms.CheckBox();
            this.chkSystemSet = new System.Windows.Forms.CheckBox();
            this.chkUsrManage = new System.Windows.Forms.CheckBox();
            this.lableRemote = new System.Windows.Forms.Label();
            this.chkRemoteAll = new System.Windows.Forms.CheckBox();
            this.chkRemoteManual = new System.Windows.Forms.CheckBox();
            this.chkRemoteReboot = new System.Windows.Forms.CheckBox();
            this.chkRemoteLogSearch = new System.Windows.Forms.CheckBox();
            this.chkRemoteAlarm = new System.Windows.Forms.CheckBox();
            this.chkRemoteChannel = new System.Windows.Forms.CheckBox();
            this.chkRemoteParaSet = new System.Windows.Forms.CheckBox();
            this.chkRemoteSystemSet = new System.Windows.Forms.CheckBox();
            this.chkRemoteUsrManage = new System.Windows.Forms.CheckBox();
            this.chkRemoteVoice = new System.Windows.Forms.CheckBox();
            this.cboRights = new System.Windows.Forms.ComboBox();
            this.lableChannelPermission = new System.Windows.Forms.Label();
            this.chkChannel1 = new System.Windows.Forms.CheckBox();
            this.chkChannel2 = new System.Windows.Forms.CheckBox();
            this.chkChannel3 = new System.Windows.Forms.CheckBox();
            this.chkChannel4 = new System.Windows.Forms.CheckBox();
            this.chkChannel5 = new System.Windows.Forms.CheckBox();
            this.chkChannel6 = new System.Windows.Forms.CheckBox();
            this.chkChannel7 = new System.Windows.Forms.CheckBox();
            this.chkChannel8 = new System.Windows.Forms.CheckBox();
            this.chkChannel9 = new System.Windows.Forms.CheckBox();
            this.chkChannel10 = new System.Windows.Forms.CheckBox();
            this.chkChannel20 = new System.Windows.Forms.CheckBox();
            this.chkChannel19 = new System.Windows.Forms.CheckBox();
            this.chkChannel18 = new System.Windows.Forms.CheckBox();
            this.chkChannel17 = new System.Windows.Forms.CheckBox();
            this.chkChannel16 = new System.Windows.Forms.CheckBox();
            this.chkChannel15 = new System.Windows.Forms.CheckBox();
            this.chkChannel14 = new System.Windows.Forms.CheckBox();
            this.chkChannel13 = new System.Windows.Forms.CheckBox();
            this.chkChannel12 = new System.Windows.Forms.CheckBox();
            this.chkChannel11 = new System.Windows.Forms.CheckBox();
            this.btnSave = new System.Windows.Forms.Button();
            this.btnClose = new System.Windows.Forms.Button();
            this.btnPrePage = new System.Windows.Forms.Button();
            this.btnNextPage = new System.Windows.Forms.Button();
            this.cboPage = new System.Windows.Forms.ComboBox();
            this.labelRightsList = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // labelUserList
            // 
            this.labelUserList.AutoSize = true;
            this.labelUserList.Location = new System.Drawing.Point(6, 21);
            this.labelUserList.Name = "labelUserList";
            this.labelUserList.Size = new System.Drawing.Size(59, 12);
            this.labelUserList.TabIndex = 1;
            this.labelUserList.Text = "User List";
            // 
            // cboUserList
            // 
            this.cboUserList.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboUserList.FormattingEnabled = true;
            this.cboUserList.Location = new System.Drawing.Point(79, 18);
            this.cboUserList.Name = "cboUserList";
            this.cboUserList.Size = new System.Drawing.Size(203, 20);
            this.cboUserList.TabIndex = 2;
            this.cboUserList.SelectedIndexChanged += new System.EventHandler(this.cboUserList_SelectedIndexChanged);
            // 
            // labelLocal
            // 
            this.labelLocal.AutoSize = true;
            this.labelLocal.Location = new System.Drawing.Point(79, 61);
            this.labelLocal.Name = "labelLocal";
            this.labelLocal.Size = new System.Drawing.Size(137, 12);
            this.labelLocal.TabIndex = 3;
            this.labelLocal.Text = "Local Device Privilege";
            // 
            // chkLocalAll
            // 
            this.chkLocalAll.AutoSize = true;
            this.chkLocalAll.Location = new System.Drawing.Point(223, 61);
            this.chkLocalAll.Name = "chkLocalAll";
            this.chkLocalAll.Size = new System.Drawing.Size(42, 16);
            this.chkLocalAll.TabIndex = 4;
            this.chkLocalAll.Text = "All";
            this.chkLocalAll.UseVisualStyleBackColor = true;
            this.chkLocalAll.CheckedChanged += new System.EventHandler(this.chkLocalAll_CheckedChanged);
            // 
            // chkManual
            // 
            this.chkManual.AutoSize = true;
            this.chkManual.Location = new System.Drawing.Point(81, 88);
            this.chkManual.Name = "chkManual";
            this.chkManual.Size = new System.Drawing.Size(60, 16);
            this.chkManual.TabIndex = 5;
            this.chkManual.Text = "Manual";
            this.chkManual.UseVisualStyleBackColor = true;
            // 
            // chkReboot
            // 
            this.chkReboot.AutoSize = true;
            this.chkReboot.Location = new System.Drawing.Point(223, 88);
            this.chkReboot.Name = "chkReboot";
            this.chkReboot.Size = new System.Drawing.Size(114, 16);
            this.chkReboot.TabIndex = 6;
            this.chkReboot.Text = "Shutdown/Reboot";
            this.chkReboot.UseVisualStyleBackColor = true;
            // 
            // chkLogSearch
            // 
            this.chkLogSearch.AutoSize = true;
            this.chkLogSearch.Location = new System.Drawing.Point(358, 88);
            this.chkLogSearch.Name = "chkLogSearch";
            this.chkLogSearch.Size = new System.Drawing.Size(84, 16);
            this.chkLogSearch.TabIndex = 7;
            this.chkLogSearch.Text = "Log Search";
            this.chkLogSearch.UseVisualStyleBackColor = true;
            // 
            // chkAlarm
            // 
            this.chkAlarm.AutoSize = true;
            this.chkAlarm.Location = new System.Drawing.Point(471, 88);
            this.chkAlarm.Name = "chkAlarm";
            this.chkAlarm.Size = new System.Drawing.Size(54, 16);
            this.chkAlarm.TabIndex = 8;
            this.chkAlarm.Text = "Alarm";
            this.chkAlarm.UseVisualStyleBackColor = true;
            // 
            // chkManageChannels
            // 
            this.chkManageChannels.AutoSize = true;
            this.chkManageChannels.Location = new System.Drawing.Point(81, 120);
            this.chkManageChannels.Name = "chkManageChannels";
            this.chkManageChannels.Size = new System.Drawing.Size(114, 16);
            this.chkManageChannels.TabIndex = 9;
            this.chkManageChannels.Text = "Manage Channels";
            this.chkManageChannels.UseVisualStyleBackColor = true;
            // 
            // chkParaSet
            // 
            this.chkParaSet.AutoSize = true;
            this.chkParaSet.Location = new System.Drawing.Point(223, 120);
            this.chkParaSet.Name = "chkParaSet";
            this.chkParaSet.Size = new System.Drawing.Size(108, 16);
            this.chkParaSet.TabIndex = 10;
            this.chkParaSet.Text = "Paramerter Set";
            this.chkParaSet.UseVisualStyleBackColor = true;
            // 
            // chkSystemSet
            // 
            this.chkSystemSet.AutoSize = true;
            this.chkSystemSet.Location = new System.Drawing.Point(358, 120);
            this.chkSystemSet.Name = "chkSystemSet";
            this.chkSystemSet.Size = new System.Drawing.Size(84, 16);
            this.chkSystemSet.TabIndex = 11;
            this.chkSystemSet.Text = "System Set";
            this.chkSystemSet.UseVisualStyleBackColor = true;
            // 
            // chkUsrManage
            // 
            this.chkUsrManage.AutoSize = true;
            this.chkUsrManage.Location = new System.Drawing.Point(471, 120);
            this.chkUsrManage.Name = "chkUsrManage";
            this.chkUsrManage.Size = new System.Drawing.Size(114, 16);
            this.chkUsrManage.TabIndex = 12;
            this.chkUsrManage.Text = "User Management";
            this.chkUsrManage.UseVisualStyleBackColor = true;
            // 
            // lableRemote
            // 
            this.lableRemote.AutoSize = true;
            this.lableRemote.Location = new System.Drawing.Point(81, 160);
            this.lableRemote.Name = "lableRemote";
            this.lableRemote.Size = new System.Drawing.Size(137, 12);
            this.lableRemote.TabIndex = 13;
            this.lableRemote.Text = "Remote Device Priviege";
            // 
            // chkRemoteAll
            // 
            this.chkRemoteAll.AutoSize = true;
            this.chkRemoteAll.Location = new System.Drawing.Point(223, 159);
            this.chkRemoteAll.Name = "chkRemoteAll";
            this.chkRemoteAll.Size = new System.Drawing.Size(42, 16);
            this.chkRemoteAll.TabIndex = 14;
            this.chkRemoteAll.Text = "All";
            this.chkRemoteAll.UseVisualStyleBackColor = true;
            this.chkRemoteAll.CheckedChanged += new System.EventHandler(this.chkRemoteAll_CheckedChanged);
            // 
            // chkRemoteManual
            // 
            this.chkRemoteManual.AutoSize = true;
            this.chkRemoteManual.Location = new System.Drawing.Point(81, 187);
            this.chkRemoteManual.Name = "chkRemoteManual";
            this.chkRemoteManual.Size = new System.Drawing.Size(60, 16);
            this.chkRemoteManual.TabIndex = 15;
            this.chkRemoteManual.Text = "Manual";
            this.chkRemoteManual.UseVisualStyleBackColor = true;
            // 
            // chkRemoteReboot
            // 
            this.chkRemoteReboot.AutoSize = true;
            this.chkRemoteReboot.Location = new System.Drawing.Point(223, 187);
            this.chkRemoteReboot.Name = "chkRemoteReboot";
            this.chkRemoteReboot.Size = new System.Drawing.Size(114, 16);
            this.chkRemoteReboot.TabIndex = 16;
            this.chkRemoteReboot.Text = "Shutdown/Reboot";
            this.chkRemoteReboot.UseVisualStyleBackColor = true;
            // 
            // chkRemoteLogSearch
            // 
            this.chkRemoteLogSearch.AutoSize = true;
            this.chkRemoteLogSearch.Location = new System.Drawing.Point(358, 187);
            this.chkRemoteLogSearch.Name = "chkRemoteLogSearch";
            this.chkRemoteLogSearch.Size = new System.Drawing.Size(84, 16);
            this.chkRemoteLogSearch.TabIndex = 17;
            this.chkRemoteLogSearch.Text = "Log Search";
            this.chkRemoteLogSearch.UseVisualStyleBackColor = true;
            // 
            // chkRemoteAlarm
            // 
            this.chkRemoteAlarm.AutoSize = true;
            this.chkRemoteAlarm.Location = new System.Drawing.Point(471, 187);
            this.chkRemoteAlarm.Name = "chkRemoteAlarm";
            this.chkRemoteAlarm.Size = new System.Drawing.Size(54, 16);
            this.chkRemoteAlarm.TabIndex = 18;
            this.chkRemoteAlarm.Text = "Alarm";
            this.chkRemoteAlarm.UseVisualStyleBackColor = true;
            // 
            // chkRemoteChannel
            // 
            this.chkRemoteChannel.AutoSize = true;
            this.chkRemoteChannel.Location = new System.Drawing.Point(81, 220);
            this.chkRemoteChannel.Name = "chkRemoteChannel";
            this.chkRemoteChannel.Size = new System.Drawing.Size(114, 16);
            this.chkRemoteChannel.TabIndex = 19;
            this.chkRemoteChannel.Text = "Manage Channels";
            this.chkRemoteChannel.UseVisualStyleBackColor = true;
            // 
            // chkRemoteParaSet
            // 
            this.chkRemoteParaSet.AutoSize = true;
            this.chkRemoteParaSet.Location = new System.Drawing.Point(223, 220);
            this.chkRemoteParaSet.Name = "chkRemoteParaSet";
            this.chkRemoteParaSet.Size = new System.Drawing.Size(108, 16);
            this.chkRemoteParaSet.TabIndex = 20;
            this.chkRemoteParaSet.Text = "Paramerter Set";
            this.chkRemoteParaSet.UseVisualStyleBackColor = true;
            // 
            // chkRemoteSystemSet
            // 
            this.chkRemoteSystemSet.AutoSize = true;
            this.chkRemoteSystemSet.Location = new System.Drawing.Point(358, 220);
            this.chkRemoteSystemSet.Name = "chkRemoteSystemSet";
            this.chkRemoteSystemSet.Size = new System.Drawing.Size(84, 16);
            this.chkRemoteSystemSet.TabIndex = 21;
            this.chkRemoteSystemSet.Text = "System Set";
            this.chkRemoteSystemSet.UseVisualStyleBackColor = true;
            // 
            // chkRemoteUsrManage
            // 
            this.chkRemoteUsrManage.AutoSize = true;
            this.chkRemoteUsrManage.Location = new System.Drawing.Point(471, 220);
            this.chkRemoteUsrManage.Name = "chkRemoteUsrManage";
            this.chkRemoteUsrManage.Size = new System.Drawing.Size(114, 16);
            this.chkRemoteUsrManage.TabIndex = 22;
            this.chkRemoteUsrManage.Text = "User Management";
            this.chkRemoteUsrManage.UseVisualStyleBackColor = true;
            // 
            // chkRemoteVoice
            // 
            this.chkRemoteVoice.AutoSize = true;
            this.chkRemoteVoice.Location = new System.Drawing.Point(81, 256);
            this.chkRemoteVoice.Name = "chkRemoteVoice";
            this.chkRemoteVoice.Size = new System.Drawing.Size(108, 16);
            this.chkRemoteVoice.TabIndex = 23;
            this.chkRemoteVoice.Text = "Voice TalkBack";
            this.chkRemoteVoice.UseVisualStyleBackColor = true;
            // 
            // cboRights
            // 
            this.cboRights.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboRights.FormattingEnabled = true;
            this.cboRights.Location = new System.Drawing.Point(79, 295);
            this.cboRights.Name = "cboRights";
            this.cboRights.Size = new System.Drawing.Size(203, 20);
            this.cboRights.TabIndex = 24;
            this.cboRights.SelectionChangeCommitted += new System.EventHandler(this.cboRights_SelectionChangeCommitted);
            this.cboRights.SelectedIndexChanged += new System.EventHandler(this.cboRights_SelectedIndexChanged);
            // 
            // lableChannelPermission
            // 
            this.lableChannelPermission.AutoSize = true;
            this.lableChannelPermission.Location = new System.Drawing.Point(81, 331);
            this.lableChannelPermission.Name = "lableChannelPermission";
            this.lableChannelPermission.Size = new System.Drawing.Size(143, 12);
            this.lableChannelPermission.TabIndex = 25;
            this.lableChannelPermission.Text = "Channel Permission Conf";
            // 
            // chkChannel1
            // 
            this.chkChannel1.AutoSize = true;
            this.chkChannel1.Location = new System.Drawing.Point(83, 364);
            this.chkChannel1.Name = "chkChannel1";
            this.chkChannel1.Size = new System.Drawing.Size(30, 16);
            this.chkChannel1.TabIndex = 27;
            this.chkChannel1.Text = "1";
            this.chkChannel1.UseVisualStyleBackColor = true;
            // 
            // chkChannel2
            // 
            this.chkChannel2.AutoSize = true;
            this.chkChannel2.Location = new System.Drawing.Point(133, 364);
            this.chkChannel2.Name = "chkChannel2";
            this.chkChannel2.Size = new System.Drawing.Size(30, 16);
            this.chkChannel2.TabIndex = 28;
            this.chkChannel2.Text = "2";
            this.chkChannel2.UseVisualStyleBackColor = true;
            // 
            // chkChannel3
            // 
            this.chkChannel3.AutoSize = true;
            this.chkChannel3.Location = new System.Drawing.Point(183, 364);
            this.chkChannel3.Name = "chkChannel3";
            this.chkChannel3.Size = new System.Drawing.Size(30, 16);
            this.chkChannel3.TabIndex = 29;
            this.chkChannel3.Text = "3";
            this.chkChannel3.UseVisualStyleBackColor = true;
            // 
            // chkChannel4
            // 
            this.chkChannel4.AutoSize = true;
            this.chkChannel4.Location = new System.Drawing.Point(233, 364);
            this.chkChannel4.Name = "chkChannel4";
            this.chkChannel4.Size = new System.Drawing.Size(30, 16);
            this.chkChannel4.TabIndex = 30;
            this.chkChannel4.Text = "4";
            this.chkChannel4.UseVisualStyleBackColor = true;
            // 
            // chkChannel5
            // 
            this.chkChannel5.AutoSize = true;
            this.chkChannel5.Location = new System.Drawing.Point(283, 364);
            this.chkChannel5.Name = "chkChannel5";
            this.chkChannel5.Size = new System.Drawing.Size(30, 16);
            this.chkChannel5.TabIndex = 31;
            this.chkChannel5.Text = "5";
            this.chkChannel5.UseVisualStyleBackColor = true;
            // 
            // chkChannel6
            // 
            this.chkChannel6.AutoSize = true;
            this.chkChannel6.Location = new System.Drawing.Point(333, 364);
            this.chkChannel6.Name = "chkChannel6";
            this.chkChannel6.Size = new System.Drawing.Size(30, 16);
            this.chkChannel6.TabIndex = 32;
            this.chkChannel6.Text = "6";
            this.chkChannel6.UseVisualStyleBackColor = true;
            // 
            // chkChannel7
            // 
            this.chkChannel7.AutoSize = true;
            this.chkChannel7.Location = new System.Drawing.Point(383, 364);
            this.chkChannel7.Name = "chkChannel7";
            this.chkChannel7.Size = new System.Drawing.Size(30, 16);
            this.chkChannel7.TabIndex = 33;
            this.chkChannel7.Text = "7";
            this.chkChannel7.UseVisualStyleBackColor = true;
            // 
            // chkChannel8
            // 
            this.chkChannel8.AutoSize = true;
            this.chkChannel8.Location = new System.Drawing.Point(433, 364);
            this.chkChannel8.Name = "chkChannel8";
            this.chkChannel8.Size = new System.Drawing.Size(30, 16);
            this.chkChannel8.TabIndex = 34;
            this.chkChannel8.Text = "8";
            this.chkChannel8.UseVisualStyleBackColor = true;
            // 
            // chkChannel9
            // 
            this.chkChannel9.AutoSize = true;
            this.chkChannel9.Location = new System.Drawing.Point(483, 364);
            this.chkChannel9.Name = "chkChannel9";
            this.chkChannel9.Size = new System.Drawing.Size(30, 16);
            this.chkChannel9.TabIndex = 35;
            this.chkChannel9.Text = "9";
            this.chkChannel9.UseVisualStyleBackColor = true;
            // 
            // chkChannel10
            // 
            this.chkChannel10.AutoSize = true;
            this.chkChannel10.Location = new System.Drawing.Point(533, 364);
            this.chkChannel10.Name = "chkChannel10";
            this.chkChannel10.Size = new System.Drawing.Size(36, 16);
            this.chkChannel10.TabIndex = 36;
            this.chkChannel10.Text = "10";
            this.chkChannel10.UseVisualStyleBackColor = true;
            // 
            // chkChannel20
            // 
            this.chkChannel20.AutoSize = true;
            this.chkChannel20.Location = new System.Drawing.Point(533, 392);
            this.chkChannel20.Name = "chkChannel20";
            this.chkChannel20.Size = new System.Drawing.Size(36, 16);
            this.chkChannel20.TabIndex = 46;
            this.chkChannel20.Text = "20";
            this.chkChannel20.UseVisualStyleBackColor = true;
            // 
            // chkChannel19
            // 
            this.chkChannel19.AutoSize = true;
            this.chkChannel19.Location = new System.Drawing.Point(483, 392);
            this.chkChannel19.Name = "chkChannel19";
            this.chkChannel19.Size = new System.Drawing.Size(36, 16);
            this.chkChannel19.TabIndex = 45;
            this.chkChannel19.Text = "19";
            this.chkChannel19.UseVisualStyleBackColor = true;
            // 
            // chkChannel18
            // 
            this.chkChannel18.AutoSize = true;
            this.chkChannel18.Location = new System.Drawing.Point(433, 392);
            this.chkChannel18.Name = "chkChannel18";
            this.chkChannel18.Size = new System.Drawing.Size(36, 16);
            this.chkChannel18.TabIndex = 44;
            this.chkChannel18.Text = "18";
            this.chkChannel18.UseVisualStyleBackColor = true;
            // 
            // chkChannel17
            // 
            this.chkChannel17.AutoSize = true;
            this.chkChannel17.Location = new System.Drawing.Point(383, 392);
            this.chkChannel17.Name = "chkChannel17";
            this.chkChannel17.Size = new System.Drawing.Size(36, 16);
            this.chkChannel17.TabIndex = 43;
            this.chkChannel17.Text = "17";
            this.chkChannel17.UseVisualStyleBackColor = true;
            // 
            // chkChannel16
            // 
            this.chkChannel16.AutoSize = true;
            this.chkChannel16.Location = new System.Drawing.Point(333, 392);
            this.chkChannel16.Name = "chkChannel16";
            this.chkChannel16.Size = new System.Drawing.Size(36, 16);
            this.chkChannel16.TabIndex = 42;
            this.chkChannel16.Text = "16";
            this.chkChannel16.UseVisualStyleBackColor = true;
            // 
            // chkChannel15
            // 
            this.chkChannel15.AutoSize = true;
            this.chkChannel15.Location = new System.Drawing.Point(283, 392);
            this.chkChannel15.Name = "chkChannel15";
            this.chkChannel15.Size = new System.Drawing.Size(36, 16);
            this.chkChannel15.TabIndex = 41;
            this.chkChannel15.Text = "15";
            this.chkChannel15.UseVisualStyleBackColor = true;
            // 
            // chkChannel14
            // 
            this.chkChannel14.AutoSize = true;
            this.chkChannel14.Location = new System.Drawing.Point(233, 392);
            this.chkChannel14.Name = "chkChannel14";
            this.chkChannel14.Size = new System.Drawing.Size(36, 16);
            this.chkChannel14.TabIndex = 40;
            this.chkChannel14.Text = "14";
            this.chkChannel14.UseVisualStyleBackColor = true;
            // 
            // chkChannel13
            // 
            this.chkChannel13.AutoSize = true;
            this.chkChannel13.Location = new System.Drawing.Point(183, 392);
            this.chkChannel13.Name = "chkChannel13";
            this.chkChannel13.Size = new System.Drawing.Size(36, 16);
            this.chkChannel13.TabIndex = 39;
            this.chkChannel13.Text = "13";
            this.chkChannel13.UseVisualStyleBackColor = true;
            // 
            // chkChannel12
            // 
            this.chkChannel12.AutoSize = true;
            this.chkChannel12.Location = new System.Drawing.Point(133, 392);
            this.chkChannel12.Name = "chkChannel12";
            this.chkChannel12.Size = new System.Drawing.Size(36, 16);
            this.chkChannel12.TabIndex = 38;
            this.chkChannel12.Text = "12";
            this.chkChannel12.UseVisualStyleBackColor = true;
            // 
            // chkChannel11
            // 
            this.chkChannel11.AutoSize = true;
            this.chkChannel11.Location = new System.Drawing.Point(83, 392);
            this.chkChannel11.Name = "chkChannel11";
            this.chkChannel11.Size = new System.Drawing.Size(36, 16);
            this.chkChannel11.TabIndex = 37;
            this.chkChannel11.Text = "11";
            this.chkChannel11.UseVisualStyleBackColor = true;
            // 
            // btnSave
            // 
            this.btnSave.Location = new System.Drawing.Point(408, 441);
            this.btnSave.Name = "btnSave";
            this.btnSave.Size = new System.Drawing.Size(75, 23);
            this.btnSave.TabIndex = 47;
            this.btnSave.Text = "Save";
            this.btnSave.UseVisualStyleBackColor = true;
            this.btnSave.Click += new System.EventHandler(this.btnSave_Click);
            // 
            // btnClose
            // 
            this.btnClose.Location = new System.Drawing.Point(504, 441);
            this.btnClose.Name = "btnClose";
            this.btnClose.Size = new System.Drawing.Size(75, 23);
            this.btnClose.TabIndex = 48;
            this.btnClose.Text = "Cancel";
            this.btnClose.UseVisualStyleBackColor = true;
            this.btnClose.Click += new System.EventHandler(this.btnClose_Click);
            // 
            // btnPrePage
            // 
            this.btnPrePage.Location = new System.Drawing.Point(445, 327);
            this.btnPrePage.Name = "btnPrePage";
            this.btnPrePage.Size = new System.Drawing.Size(29, 23);
            this.btnPrePage.TabIndex = 49;
            this.btnPrePage.Text = "<-";
            this.btnPrePage.UseVisualStyleBackColor = true;
            this.btnPrePage.Click += new System.EventHandler(this.btnPrePage_Click);
            // 
            // btnNextPage
            // 
            this.btnNextPage.Location = new System.Drawing.Point(530, 328);
            this.btnNextPage.Name = "btnNextPage";
            this.btnNextPage.Size = new System.Drawing.Size(29, 23);
            this.btnNextPage.TabIndex = 50;
            this.btnNextPage.Text = "->";
            this.btnNextPage.UseVisualStyleBackColor = true;
            this.btnNextPage.Click += new System.EventHandler(this.btnNextPage_Click);
            // 
            // cboPage
            // 
            this.cboPage.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboPage.FormattingEnabled = true;
            this.cboPage.Location = new System.Drawing.Point(479, 328);
            this.cboPage.Name = "cboPage";
            this.cboPage.Size = new System.Drawing.Size(46, 20);
            this.cboPage.TabIndex = 51;
            this.cboPage.SelectedIndexChanged += new System.EventHandler(this.cboPage_SelectedIndexChanged);
            // 
            // labelRightsList
            // 
            this.labelRightsList.AutoSize = true;
            this.labelRightsList.Location = new System.Drawing.Point(6, 298);
            this.labelRightsList.Name = "labelRightsList";
            this.labelRightsList.Size = new System.Drawing.Size(65, 12);
            this.labelRightsList.TabIndex = 52;
            this.labelRightsList.Text = "Right List";
            // 
            // RightsManagementForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(619, 476);
            this.Controls.Add(this.labelRightsList);
            this.Controls.Add(this.cboPage);
            this.Controls.Add(this.btnNextPage);
            this.Controls.Add(this.btnPrePage);
            this.Controls.Add(this.btnClose);
            this.Controls.Add(this.btnSave);
            this.Controls.Add(this.chkChannel20);
            this.Controls.Add(this.chkChannel19);
            this.Controls.Add(this.chkChannel18);
            this.Controls.Add(this.chkChannel17);
            this.Controls.Add(this.chkChannel16);
            this.Controls.Add(this.chkChannel15);
            this.Controls.Add(this.chkChannel14);
            this.Controls.Add(this.chkChannel13);
            this.Controls.Add(this.chkChannel12);
            this.Controls.Add(this.chkChannel11);
            this.Controls.Add(this.chkChannel10);
            this.Controls.Add(this.chkChannel9);
            this.Controls.Add(this.chkChannel8);
            this.Controls.Add(this.chkChannel7);
            this.Controls.Add(this.chkChannel6);
            this.Controls.Add(this.chkChannel5);
            this.Controls.Add(this.chkChannel4);
            this.Controls.Add(this.chkChannel3);
            this.Controls.Add(this.chkChannel2);
            this.Controls.Add(this.chkChannel1);
            this.Controls.Add(this.lableChannelPermission);
            this.Controls.Add(this.cboRights);
            this.Controls.Add(this.chkRemoteVoice);
            this.Controls.Add(this.chkRemoteUsrManage);
            this.Controls.Add(this.chkRemoteSystemSet);
            this.Controls.Add(this.chkRemoteParaSet);
            this.Controls.Add(this.chkRemoteChannel);
            this.Controls.Add(this.chkRemoteAlarm);
            this.Controls.Add(this.chkRemoteLogSearch);
            this.Controls.Add(this.chkRemoteReboot);
            this.Controls.Add(this.chkRemoteManual);
            this.Controls.Add(this.chkRemoteAll);
            this.Controls.Add(this.lableRemote);
            this.Controls.Add(this.chkUsrManage);
            this.Controls.Add(this.chkSystemSet);
            this.Controls.Add(this.chkParaSet);
            this.Controls.Add(this.chkManageChannels);
            this.Controls.Add(this.chkAlarm);
            this.Controls.Add(this.chkLogSearch);
            this.Controls.Add(this.chkReboot);
            this.Controls.Add(this.chkManual);
            this.Controls.Add(this.chkLocalAll);
            this.Controls.Add(this.labelLocal);
            this.Controls.Add(this.cboUserList);
            this.Controls.Add(this.labelUserList);
            this.Name = "RightsManagementForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Rights Management";
            this.Shown += new System.EventHandler(this.RightsManagementForm_Shown);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label labelUserList;
        private System.Windows.Forms.ComboBox cboUserList;
        private System.Windows.Forms.Label labelLocal;
        private System.Windows.Forms.CheckBox chkLocalAll;
        private System.Windows.Forms.CheckBox chkManual;
        private System.Windows.Forms.CheckBox chkReboot;
        private System.Windows.Forms.CheckBox chkLogSearch;
        private System.Windows.Forms.CheckBox chkAlarm;
        private System.Windows.Forms.CheckBox chkManageChannels;
        private System.Windows.Forms.CheckBox chkParaSet;
        private System.Windows.Forms.CheckBox chkSystemSet;
        private System.Windows.Forms.CheckBox chkUsrManage;
        private System.Windows.Forms.Label lableRemote;
        private System.Windows.Forms.CheckBox chkRemoteAll;
        private System.Windows.Forms.CheckBox chkRemoteManual;
        private System.Windows.Forms.CheckBox chkRemoteReboot;
        private System.Windows.Forms.CheckBox chkRemoteLogSearch;
        private System.Windows.Forms.CheckBox chkRemoteAlarm;
        private System.Windows.Forms.CheckBox chkRemoteChannel;
        private System.Windows.Forms.CheckBox chkRemoteParaSet;
        private System.Windows.Forms.CheckBox chkRemoteSystemSet;
        private System.Windows.Forms.CheckBox chkRemoteUsrManage;
        private System.Windows.Forms.CheckBox chkRemoteVoice;
        private System.Windows.Forms.ComboBox cboRights;
        private System.Windows.Forms.Label lableChannelPermission;
        private System.Windows.Forms.CheckBox chkChannel1;
        private System.Windows.Forms.CheckBox chkChannel2;
        private System.Windows.Forms.CheckBox chkChannel3;
        private System.Windows.Forms.CheckBox chkChannel4;
        private System.Windows.Forms.CheckBox chkChannel5;
        private System.Windows.Forms.CheckBox chkChannel6;
        private System.Windows.Forms.CheckBox chkChannel7;
        private System.Windows.Forms.CheckBox chkChannel8;
        private System.Windows.Forms.CheckBox chkChannel9;
        private System.Windows.Forms.CheckBox chkChannel10;
        private System.Windows.Forms.CheckBox chkChannel20;
        private System.Windows.Forms.CheckBox chkChannel19;
        private System.Windows.Forms.CheckBox chkChannel18;
        private System.Windows.Forms.CheckBox chkChannel17;
        private System.Windows.Forms.CheckBox chkChannel16;
        private System.Windows.Forms.CheckBox chkChannel15;
        private System.Windows.Forms.CheckBox chkChannel14;
        private System.Windows.Forms.CheckBox chkChannel13;
        private System.Windows.Forms.CheckBox chkChannel12;
        private System.Windows.Forms.CheckBox chkChannel11;
        private System.Windows.Forms.Button btnSave;
        private System.Windows.Forms.Button btnClose;
        private System.Windows.Forms.Button btnPrePage;
        private System.Windows.Forms.Button btnNextPage;
        private System.Windows.Forms.ComboBox cboPage;
        private System.Windows.Forms.Label labelRightsList;
    }
}
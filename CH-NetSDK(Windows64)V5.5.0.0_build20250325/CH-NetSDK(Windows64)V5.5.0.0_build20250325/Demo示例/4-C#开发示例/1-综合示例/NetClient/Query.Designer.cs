namespace NetClient
{
    partial class Query
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
            System.Windows.Forms.Button btnQuery;
            this.gridFiles = new System.Windows.Forms.DataGridView();
            this.lblTotalFiles = new System.Windows.Forms.Label();
            this.lblTotalPages = new System.Windows.Forms.Label();
            this.lblCurrentPage = new System.Windows.Forms.Label();
            this.btnDownload = new System.Windows.Forms.Button();
            this.btnFirstPage = new System.Windows.Forms.Button();
            this.btnPrePage = new System.Windows.Forms.Button();
            this.btnNextPage = new System.Windows.Forms.Button();
            this.btnLastPage = new System.Windows.Forms.Button();
            this.gpQueryCondition = new System.Windows.Forms.GroupBox();
            this.cboRecType = new System.Windows.Forms.ComboBox();
            this.cboFileType = new System.Windows.Forms.ComboBox();
            this.label6 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.timeEnd = new System.Windows.Forms.DateTimePicker();
            this.dateBegin = new System.Windows.Forms.DateTimePicker();
            this.label4 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.dateEnd = new System.Windows.Forms.DateTimePicker();
            this.timeBegin = new System.Windows.Forms.DateTimePicker();
            this.cboChannel = new System.Windows.Forms.ComboBox();
            this.label2 = new System.Windows.Forms.Label();
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.FileName = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.Type = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.Channel = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.Size = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.Time = new System.Windows.Forms.DataGridViewTextBoxColumn();
            btnQuery = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.gridFiles)).BeginInit();
            this.gpQueryCondition.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnQuery
            // 
            btnQuery.Location = new System.Drawing.Point(35, 292);
            btnQuery.Name = "btnQuery";
            btnQuery.Size = new System.Drawing.Size(66, 24);
            btnQuery.TabIndex = 16;
            btnQuery.Text = "Query";
            btnQuery.UseVisualStyleBackColor = true;
            btnQuery.Click += new System.EventHandler(this.btnQuery_Click);
            // 
            // gridFiles
            // 
            this.gridFiles.AllowUserToAddRows = false;
            this.gridFiles.AllowUserToDeleteRows = false;
            this.gridFiles.AllowUserToOrderColumns = true;
            this.gridFiles.BackgroundColor = System.Drawing.SystemColors.Control;
            this.gridFiles.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.gridFiles.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.FileName,
            this.Type,
            this.Channel,
            this.Size,
            this.Time});
            this.gridFiles.Location = new System.Drawing.Point(14, 31);
            this.gridFiles.MultiSelect = false;
            this.gridFiles.Name = "gridFiles";
            this.gridFiles.ReadOnly = true;
            this.gridFiles.RowHeadersVisible = false;
            this.gridFiles.RowTemplate.Height = 23;
            this.gridFiles.Size = new System.Drawing.Size(585, 252);
            this.gridFiles.TabIndex = 0;
            this.gridFiles.CellClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.gridFiles_CellClick);
            // 
            // lblTotalFiles
            // 
            this.lblTotalFiles.AutoSize = true;
            this.lblTotalFiles.Location = new System.Drawing.Point(168, 15);
            this.lblTotalFiles.Name = "lblTotalFiles";
            this.lblTotalFiles.Size = new System.Drawing.Size(71, 12);
            this.lblTotalFiles.TabIndex = 2;
            this.lblTotalFiles.Text = "TotalFiles:";
            // 
            // lblTotalPages
            // 
            this.lblTotalPages.AutoSize = true;
            this.lblTotalPages.Location = new System.Drawing.Point(307, 15);
            this.lblTotalPages.Name = "lblTotalPages";
            this.lblTotalPages.Size = new System.Drawing.Size(71, 12);
            this.lblTotalPages.TabIndex = 3;
            this.lblTotalPages.Text = "TotalPages:";
            // 
            // lblCurrentPage
            // 
            this.lblCurrentPage.AutoSize = true;
            this.lblCurrentPage.Location = new System.Drawing.Point(452, 15);
            this.lblCurrentPage.Name = "lblCurrentPage";
            this.lblCurrentPage.Size = new System.Drawing.Size(77, 12);
            this.lblCurrentPage.TabIndex = 4;
            this.lblCurrentPage.Text = "CurrentPage:";
            // 
            // btnDownload
            // 
            this.btnDownload.Location = new System.Drawing.Point(107, 292);
            this.btnDownload.Name = "btnDownload";
            this.btnDownload.Size = new System.Drawing.Size(66, 24);
            this.btnDownload.TabIndex = 6;
            this.btnDownload.Text = "Download";
            this.btnDownload.UseVisualStyleBackColor = true;
            this.btnDownload.Click += new System.EventHandler(this.btnDownload_Click);
            // 
            // btnFirstPage
            // 
            this.btnFirstPage.Location = new System.Drawing.Point(297, 292);
            this.btnFirstPage.Name = "btnFirstPage";
            this.btnFirstPage.Size = new System.Drawing.Size(66, 24);
            this.btnFirstPage.TabIndex = 7;
            this.btnFirstPage.Text = "FirstPage";
            this.btnFirstPage.UseVisualStyleBackColor = true;
            this.btnFirstPage.Click += new System.EventHandler(this.btnFirstPage_Click);
            // 
            // btnPrePage
            // 
            this.btnPrePage.Location = new System.Drawing.Point(369, 292);
            this.btnPrePage.Name = "btnPrePage";
            this.btnPrePage.Size = new System.Drawing.Size(66, 24);
            this.btnPrePage.TabIndex = 8;
            this.btnPrePage.Text = "PrePage";
            this.btnPrePage.UseVisualStyleBackColor = true;
            this.btnPrePage.Click += new System.EventHandler(this.btnPrePage_Click);
            // 
            // btnNextPage
            // 
            this.btnNextPage.Location = new System.Drawing.Point(440, 292);
            this.btnNextPage.Name = "btnNextPage";
            this.btnNextPage.Size = new System.Drawing.Size(66, 24);
            this.btnNextPage.TabIndex = 9;
            this.btnNextPage.Text = "NextPage";
            this.btnNextPage.UseVisualStyleBackColor = true;
            this.btnNextPage.Click += new System.EventHandler(this.btnNextPage_Click);
            // 
            // btnLastPage
            // 
            this.btnLastPage.Location = new System.Drawing.Point(512, 292);
            this.btnLastPage.Name = "btnLastPage";
            this.btnLastPage.Size = new System.Drawing.Size(66, 24);
            this.btnLastPage.TabIndex = 10;
            this.btnLastPage.Text = "LastPage";
            this.btnLastPage.UseVisualStyleBackColor = true;
            this.btnLastPage.Click += new System.EventHandler(this.btnLastPage_Click);
            // 
            // gpQueryCondition
            // 
            this.gpQueryCondition.Controls.Add(this.cboRecType);
            this.gpQueryCondition.Controls.Add(this.cboFileType);
            this.gpQueryCondition.Controls.Add(this.label6);
            this.gpQueryCondition.Controls.Add(this.label5);
            this.gpQueryCondition.Controls.Add(this.timeEnd);
            this.gpQueryCondition.Controls.Add(this.dateBegin);
            this.gpQueryCondition.Controls.Add(this.label4);
            this.gpQueryCondition.Controls.Add(this.label3);
            this.gpQueryCondition.Controls.Add(this.dateEnd);
            this.gpQueryCondition.Controls.Add(this.timeBegin);
            this.gpQueryCondition.Controls.Add(this.cboChannel);
            this.gpQueryCondition.Controls.Add(this.label2);
            this.gpQueryCondition.Controls.Add(this.textBox1);
            this.gpQueryCondition.Controls.Add(this.label1);
            this.gpQueryCondition.Location = new System.Drawing.Point(14, 322);
            this.gpQueryCondition.Name = "gpQueryCondition";
            this.gpQueryCondition.Size = new System.Drawing.Size(585, 82);
            this.gpQueryCondition.TabIndex = 11;
            this.gpQueryCondition.TabStop = false;
            this.gpQueryCondition.Text = "Query condition";
            // 
            // cboRecType
            // 
            this.cboRecType.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboRecType.FormattingEnabled = true;
            this.cboRecType.Items.AddRange(new object[] {
            "ALL",
            "Manual",
            "Timer",
            "Alarm"});
            this.cboRecType.Location = new System.Drawing.Point(238, 52);
            this.cboRecType.Name = "cboRecType";
            this.cboRecType.Size = new System.Drawing.Size(84, 20);
            this.cboRecType.TabIndex = 17;
            // 
            // cboFileType
            // 
            this.cboFileType.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboFileType.FormattingEnabled = true;
            this.cboFileType.Items.AddRange(new object[] {
            "Video"});
            this.cboFileType.Location = new System.Drawing.Point(67, 48);
            this.cboFileType.Name = "cboFileType";
            this.cboFileType.Size = new System.Drawing.Size(96, 20);
            this.cboFileType.TabIndex = 16;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(354, 52);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(53, 12);
            this.label6.TabIndex = 15;
            this.label6.Text = "EndTime:";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(342, 25);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(65, 12);
            this.label5.TabIndex = 14;
            this.label5.Text = "BeginTime:";
            // 
            // timeEnd
            // 
            this.timeEnd.Format = System.Windows.Forms.DateTimePickerFormat.Time;
            this.timeEnd.Location = new System.Drawing.Point(496, 49);
            this.timeEnd.Name = "timeEnd";
            this.timeEnd.ShowUpDown = true;
            this.timeEnd.Size = new System.Drawing.Size(73, 21);
            this.timeEnd.TabIndex = 13;
            // 
            // dateBegin
            // 
            this.dateBegin.CustomFormat = "";
            this.dateBegin.Format = System.Windows.Forms.DateTimePickerFormat.Short;
            this.dateBegin.Location = new System.Drawing.Point(407, 21);
            this.dateBegin.Name = "dateBegin";
            this.dateBegin.ShowUpDown = true;
            this.dateBegin.Size = new System.Drawing.Size(85, 21);
            this.dateBegin.TabIndex = 12;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(187, 53);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(53, 12);
            this.label4.TabIndex = 10;
            this.label4.Text = "RecType:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(9, 52);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(59, 12);
            this.label3.TabIndex = 8;
            this.label3.Text = "FileType:";
            // 
            // dateEnd
            // 
            this.dateEnd.Format = System.Windows.Forms.DateTimePickerFormat.Short;
            this.dateEnd.Location = new System.Drawing.Point(407, 48);
            this.dateEnd.Name = "dateEnd";
            this.dateEnd.ShowUpDown = true;
            this.dateEnd.Size = new System.Drawing.Size(85, 21);
            this.dateEnd.TabIndex = 7;
            // 
            // timeBegin
            // 
            this.timeBegin.CustomFormat = "";
            this.timeBegin.Format = System.Windows.Forms.DateTimePickerFormat.Time;
            this.timeBegin.Location = new System.Drawing.Point(496, 21);
            this.timeBegin.Name = "timeBegin";
            this.timeBegin.ShowUpDown = true;
            this.timeBegin.Size = new System.Drawing.Size(73, 21);
            this.timeBegin.TabIndex = 6;
            // 
            // cboChannel
            // 
            this.cboChannel.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboChannel.FormattingEnabled = true;
            this.cboChannel.Location = new System.Drawing.Point(238, 23);
            this.cboChannel.Name = "cboChannel";
            this.cboChannel.Size = new System.Drawing.Size(84, 20);
            this.cboChannel.TabIndex = 5;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(186, 26);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(53, 12);
            this.label2.TabIndex = 4;
            this.label2.Text = "Channel:";
            // 
            // textBox1
            // 
            this.textBox1.Location = new System.Drawing.Point(67, 21);
            this.textBox1.Name = "textBox1";
            this.textBox1.ReadOnly = true;
            this.textBox1.Size = new System.Drawing.Size(96, 21);
            this.textBox1.TabIndex = 3;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(38, 24);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(29, 12);
            this.label1.TabIndex = 2;
            this.label1.Text = "NVS:";
            // 
            // FileName
            // 
            this.FileName.FillWeight = 220F;
            this.FileName.HeaderText = "FileName";
            this.FileName.Name = "FileName";
            this.FileName.ReadOnly = true;
            this.FileName.Width = 220;
            // 
            // Type
            // 
            this.Type.FillWeight = 60F;
            this.Type.HeaderText = "Type";
            this.Type.Name = "Type";
            this.Type.ReadOnly = true;
            this.Type.Width = 60;
            // 
            // Channel
            // 
            this.Channel.FillWeight = 60F;
            this.Channel.HeaderText = "Channel";
            this.Channel.Name = "Channel";
            this.Channel.ReadOnly = true;
            this.Channel.Width = 60;
            // 
            // Size
            // 
            this.Size.FillWeight = 90F;
            this.Size.HeaderText = "Size(Bytes)";
            this.Size.Name = "Size";
            this.Size.ReadOnly = true;
            this.Size.Width = 90;
            // 
            // Time
            // 
            this.Time.FillWeight = 150F;
            this.Time.HeaderText = "Time";
            this.Time.Name = "Time";
            this.Time.ReadOnly = true;
            this.Time.Width = 150;
            // 
            // Query
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(614, 420);
            this.Controls.Add(btnQuery);
            this.Controls.Add(this.gpQueryCondition);
            this.Controls.Add(this.btnLastPage);
            this.Controls.Add(this.btnNextPage);
            this.Controls.Add(this.btnPrePage);
            this.Controls.Add(this.btnFirstPage);
            this.Controls.Add(this.btnDownload);
            this.Controls.Add(this.lblCurrentPage);
            this.Controls.Add(this.lblTotalPages);
            this.Controls.Add(this.lblTotalFiles);
            this.Controls.Add(this.gridFiles);
            this.Name = "Query";
            this.Text = "Query";
            this.Load += new System.EventHandler(this.Query_Load);
            ((System.ComponentModel.ISupportInitialize)(this.gridFiles)).EndInit();
            this.gpQueryCondition.ResumeLayout(false);
            this.gpQueryCondition.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.DataGridView gridFiles;
        private System.Windows.Forms.Label lblTotalFiles;
        private System.Windows.Forms.Label lblTotalPages;
        private System.Windows.Forms.Label lblCurrentPage;
        private System.Windows.Forms.Button btnDownload;
        private System.Windows.Forms.Button btnFirstPage;
        private System.Windows.Forms.Button btnPrePage;
        private System.Windows.Forms.Button btnNextPage;
        private System.Windows.Forms.Button btnLastPage;
        private System.Windows.Forms.GroupBox gpQueryCondition;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.DateTimePicker dateEnd;
        private System.Windows.Forms.DateTimePicker timeBegin;
        private System.Windows.Forms.ComboBox cboChannel;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.DateTimePicker timeEnd;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.DateTimePicker dateBegin;
        private System.Windows.Forms.ComboBox cboRecType;
        private System.Windows.Forms.ComboBox cboFileType;
        private System.Windows.Forms.DataGridViewTextBoxColumn FileName;
        private System.Windows.Forms.DataGridViewTextBoxColumn Type;
        private System.Windows.Forms.DataGridViewTextBoxColumn Channel;
        private System.Windows.Forms.DataGridViewTextBoxColumn Size;
        private System.Windows.Forms.DataGridViewTextBoxColumn Time;
    }
}
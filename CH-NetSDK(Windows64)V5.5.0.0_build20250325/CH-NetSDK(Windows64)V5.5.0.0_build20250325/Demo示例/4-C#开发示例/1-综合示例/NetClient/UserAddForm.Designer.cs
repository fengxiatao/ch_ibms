namespace NetClient
{
    partial class UserAddForm
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
            this.tbPwdConfirm = new System.Windows.Forms.TextBox();
            this.tbPwd = new System.Windows.Forms.TextBox();
            this.cmbAuthority = new System.Windows.Forms.ComboBox();
            this.tbUserName = new System.Windows.Forms.TextBox();
            this.label5 = new System.Windows.Forms.Label();
            this.lbNewPwd = new System.Windows.Forms.Label();
            this.lbAuthority = new System.Windows.Forms.Label();
            this.lbUserName = new System.Windows.Forms.Label();
            this.btnCancel = new System.Windows.Forms.Button();
            this.btnAdd = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // tbPwdConfirm
            // 
            this.tbPwdConfirm.Location = new System.Drawing.Point(147, 137);
            this.tbPwdConfirm.Name = "tbPwdConfirm";
            this.tbPwdConfirm.PasswordChar = '*';
            this.tbPwdConfirm.Size = new System.Drawing.Size(158, 21);
            this.tbPwdConfirm.TabIndex = 21;
            this.tbPwdConfirm.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.tbPwdConfirm_KeyPress);
            // 
            // tbPwd
            // 
            this.tbPwd.Location = new System.Drawing.Point(145, 98);
            this.tbPwd.Name = "tbPwd";
            this.tbPwd.PasswordChar = '*';
            this.tbPwd.Size = new System.Drawing.Size(160, 21);
            this.tbPwd.TabIndex = 20;
            this.tbPwd.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.tbPwd_KeyPress);
            // 
            // cmbAuthority
            // 
            this.cmbAuthority.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cmbAuthority.FormattingEnabled = true;
            this.cmbAuthority.Location = new System.Drawing.Point(145, 56);
            this.cmbAuthority.Name = "cmbAuthority";
            this.cmbAuthority.Size = new System.Drawing.Size(160, 20);
            this.cmbAuthority.TabIndex = 19;
            // 
            // tbUserName
            // 
            this.tbUserName.Location = new System.Drawing.Point(145, 17);
            this.tbUserName.Name = "tbUserName";
            this.tbUserName.Size = new System.Drawing.Size(160, 21);
            this.tbUserName.TabIndex = 18;
            this.tbUserName.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.tbUserName_KeyPress);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(28, 141);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(101, 12);
            this.label5.TabIndex = 17;
            this.label5.Text = "Password Confirm";
            // 
            // lbNewPwd
            // 
            this.lbNewPwd.AutoSize = true;
            this.lbNewPwd.Location = new System.Drawing.Point(82, 104);
            this.lbNewPwd.Name = "lbNewPwd";
            this.lbNewPwd.Size = new System.Drawing.Size(47, 12);
            this.lbNewPwd.TabIndex = 16;
            this.lbNewPwd.Text = "New Pwd";
            // 
            // lbAuthority
            // 
            this.lbAuthority.AutoSize = true;
            this.lbAuthority.Location = new System.Drawing.Point(70, 60);
            this.lbAuthority.Name = "lbAuthority";
            this.lbAuthority.Size = new System.Drawing.Size(59, 12);
            this.lbAuthority.TabIndex = 15;
            this.lbAuthority.Text = "Authority";
            // 
            // lbUserName
            // 
            this.lbUserName.AutoSize = true;
            this.lbUserName.Location = new System.Drawing.Point(70, 20);
            this.lbUserName.Name = "lbUserName";
            this.lbUserName.Size = new System.Drawing.Size(59, 12);
            this.lbUserName.TabIndex = 14;
            this.lbUserName.Text = "User name";
            // 
            // btnCancel
            // 
            this.btnCancel.Location = new System.Drawing.Point(229, 197);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(75, 23);
            this.btnCancel.TabIndex = 23;
            this.btnCancel.Text = "Cancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // btnAdd
            // 
            this.btnAdd.Location = new System.Drawing.Point(144, 197);
            this.btnAdd.Name = "btnAdd";
            this.btnAdd.Size = new System.Drawing.Size(75, 23);
            this.btnAdd.TabIndex = 22;
            this.btnAdd.Text = "Add";
            this.btnAdd.UseVisualStyleBackColor = true;
            this.btnAdd.Click += new System.EventHandler(this.btnAdd_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(311, 20);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(11, 12);
            this.label1.TabIndex = 24;
            this.label1.Text = "*";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(311, 60);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(11, 12);
            this.label2.TabIndex = 25;
            this.label2.Text = "*";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(311, 101);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(11, 12);
            this.label3.TabIndex = 26;
            this.label3.Text = "*";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(311, 141);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(11, 12);
            this.label4.TabIndex = 27;
            this.label4.Text = "*";
            // 
            // UserAddForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(356, 236);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnAdd);
            this.Controls.Add(this.tbPwdConfirm);
            this.Controls.Add(this.tbPwd);
            this.Controls.Add(this.cmbAuthority);
            this.Controls.Add(this.tbUserName);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.lbNewPwd);
            this.Controls.Add(this.lbAuthority);
            this.Controls.Add(this.lbUserName);
            this.Name = "UserAddForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Add";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox tbPwdConfirm;
        private System.Windows.Forms.TextBox tbPwd;
        private System.Windows.Forms.ComboBox cmbAuthority;
        private System.Windows.Forms.TextBox tbUserName;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label lbNewPwd;
        private System.Windows.Forms.Label lbAuthority;
        private System.Windows.Forms.Label lbUserName;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.Button btnAdd;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
    }
}
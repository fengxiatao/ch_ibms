namespace NetClient
{
    partial class UserModifyForm
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
            this.lbUserName = new System.Windows.Forms.Label();
            this.lbOldPwd = new System.Windows.Forms.Label();
            this.lbNewPwd = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.btnModify = new System.Windows.Forms.Button();
            this.btnCancel = new System.Windows.Forms.Button();
            this.tbUserName = new System.Windows.Forms.TextBox();
            this.tbOldPwd = new System.Windows.Forms.TextBox();
            this.tbNewPwd = new System.Windows.Forms.TextBox();
            this.tbPwdConfirm = new System.Windows.Forms.TextBox();
            this.SuspendLayout();
            // 
            // lbUserName
            // 
            this.lbUserName.AutoSize = true;
            this.lbUserName.Location = new System.Drawing.Point(53, 15);
            this.lbUserName.Name = "lbUserName";
            this.lbUserName.Size = new System.Drawing.Size(59, 12);
            this.lbUserName.TabIndex = 0;
            this.lbUserName.Text = "User name";
            // 
            // lbOldPwd
            // 
            this.lbOldPwd.AutoSize = true;
            this.lbOldPwd.Location = new System.Drawing.Point(65, 55);
            this.lbOldPwd.Name = "lbOldPwd";
            this.lbOldPwd.Size = new System.Drawing.Size(47, 12);
            this.lbOldPwd.TabIndex = 2;
            this.lbOldPwd.Text = "Old Pwd";
            // 
            // lbNewPwd
            // 
            this.lbNewPwd.AutoSize = true;
            this.lbNewPwd.Location = new System.Drawing.Point(65, 94);
            this.lbNewPwd.Name = "lbNewPwd";
            this.lbNewPwd.Size = new System.Drawing.Size(47, 12);
            this.lbNewPwd.TabIndex = 3;
            this.lbNewPwd.Text = "New Pwd";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(11, 131);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(101, 12);
            this.label5.TabIndex = 4;
            this.label5.Text = "Password Confirm";
            // 
            // btnModify
            // 
            this.btnModify.Location = new System.Drawing.Point(128, 177);
            this.btnModify.Name = "btnModify";
            this.btnModify.Size = new System.Drawing.Size(75, 23);
            this.btnModify.TabIndex = 7;
            this.btnModify.Text = "Modify";
            this.btnModify.UseVisualStyleBackColor = true;
            this.btnModify.Click += new System.EventHandler(this.btnModify_Click);
            // 
            // btnCancel
            // 
            this.btnCancel.Location = new System.Drawing.Point(213, 177);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(75, 23);
            this.btnCancel.TabIndex = 8;
            this.btnCancel.Text = "Cancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // tbUserName
            // 
            this.tbUserName.Location = new System.Drawing.Point(128, 12);
            this.tbUserName.Name = "tbUserName";
            this.tbUserName.ReadOnly = true;
            this.tbUserName.ShortcutsEnabled = false;
            this.tbUserName.Size = new System.Drawing.Size(160, 21);
            this.tbUserName.TabIndex = 9;
            // 
            // tbOldPwd
            // 
            this.tbOldPwd.Location = new System.Drawing.Point(128, 49);
            this.tbOldPwd.Name = "tbOldPwd";
            this.tbOldPwd.PasswordChar = '*';
            this.tbOldPwd.ShortcutsEnabled = false;
            this.tbOldPwd.Size = new System.Drawing.Size(160, 21);
            this.tbOldPwd.TabIndex = 11;
            this.tbOldPwd.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.tbOldPwd_KeyPress);
            // 
            // tbNewPwd
            // 
            this.tbNewPwd.Location = new System.Drawing.Point(128, 88);
            this.tbNewPwd.Name = "tbNewPwd";
            this.tbNewPwd.PasswordChar = '*';
            this.tbNewPwd.ShortcutsEnabled = false;
            this.tbNewPwd.Size = new System.Drawing.Size(160, 21);
            this.tbNewPwd.TabIndex = 12;
            this.tbNewPwd.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.tbNewPwd_KeyPress);
            // 
            // tbPwdConfirm
            // 
            this.tbPwdConfirm.Location = new System.Drawing.Point(130, 127);
            this.tbPwdConfirm.Name = "tbPwdConfirm";
            this.tbPwdConfirm.PasswordChar = '*';
            this.tbPwdConfirm.ShortcutsEnabled = false;
            this.tbPwdConfirm.Size = new System.Drawing.Size(158, 21);
            this.tbPwdConfirm.TabIndex = 13;
            this.tbPwdConfirm.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.tbPwdConfirm_KeyPress);
            // 
            // UserModifyForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(318, 217);
            this.Controls.Add(this.tbPwdConfirm);
            this.Controls.Add(this.tbNewPwd);
            this.Controls.Add(this.tbOldPwd);
            this.Controls.Add(this.tbUserName);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnModify);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.lbNewPwd);
            this.Controls.Add(this.lbOldPwd);
            this.Controls.Add(this.lbUserName);
            this.Name = "UserModifyForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Modify";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label lbUserName;
        private System.Windows.Forms.Label lbOldPwd;
        private System.Windows.Forms.Label lbNewPwd;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Button btnModify;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.TextBox tbUserName;
        private System.Windows.Forms.TextBox tbOldPwd;
        private System.Windows.Forms.TextBox tbNewPwd;
        private System.Windows.Forms.TextBox tbPwdConfirm;
    }
}
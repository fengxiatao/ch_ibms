using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

using System.Runtime.InteropServices;

namespace NetClient
{
    public partial class UserModifyForm : Form
    {
        private int m_iLogonID = -1;
        private bool m_bIsNvr = false;
        private string m_strOldPwd = string.Empty;

        public UserModifyForm()
        {
            InitializeComponent();
        }
        public UserModifyForm(int _iLogonID, string _strUserName, string _strPwd, int _iAuthority, bool _bIsNvr)
        {
            InitializeComponent();

            // Initialize Authority Control
            tbUserName.MaxLength = 15;
            tbOldPwd.MaxLength = 15;
            tbNewPwd.MaxLength = 15;
            tbPwdConfirm.MaxLength = 15;

            m_iLogonID = _iLogonID;
            m_bIsNvr = _bIsNvr;
            m_strOldPwd = _strPwd;
            tbUserName.Text = _strUserName;
        }
       

        private void btnModify_Click(object sender, EventArgs e)
        {
            // Check Input info
            string strUserName = tbUserName.Text.Trim();
            string strOldPwd   = tbOldPwd.Text.Trim();
            string strNewPwd   = tbNewPwd.Text.Trim();
            string strPwdConfirm = tbPwdConfirm.Text.Trim();
            if (strUserName.Length <= 0 || strOldPwd.Length <= 0)
            {
                MessageBox.Show("Please enter the complete information.");
                return;
            }

            // if user is "Admin" and pwd is default 
            if (0 == strUserName.ToUpper().CompareTo("Admin".ToUpper()) && 0 == m_strOldPwd.CompareTo("Admin"))
            {
                // password is admin\Admin\1111, ok
                if (0 == strOldPwd.CompareTo("Admin") || 0 == strOldPwd.CompareTo("admin") || 0 == strOldPwd.CompareTo("1111"))
                {
                    // ok
                }
                else
                {
                    MessageBox.Show("Old Pwd entry error.");
                    return;
                }
            }
            else
            {
                if (0 != strOldPwd.CompareTo(m_strOldPwd))
                {
                    MessageBox.Show("Old Pwd entry error.");
                    return;
                }
            }

            if (0 != strNewPwd.CompareTo(strPwdConfirm))
            {
                MessageBox.Show("The New Pwd is different from the Password Confirm.");
                return;
            }

            string strIllChar = "#\":;'\\"; 
            for (int i = 0; i < strIllChar.Length; i++)
            {
                if (strNewPwd.Contains(strIllChar[i]))
                {
                    MessageBox.Show("Password contains illegal characters.");
                    return;
                }
            }

            // Modify Password
            if (strNewPwd.Length > 0)
            {
                int iRet = NVSSDK.NetClient_ModifyPwd(m_iLogonID, strUserName, strNewPwd);
                if (iRet < 0)
                {
                    Console.WriteLine("NetClient_GetUserInfo error");
                    return;
                }
            }
            
            
            this.Close();
        }
       
        private void tbOldPwd_KeyPress(object sender, KeyPressEventArgs e)
        {
            if ((e.KeyChar >= 'a' && e.KeyChar <= 'z') || (e.KeyChar >= 'A' && e.KeyChar <= 'Z')
                || (e.KeyChar >= '0' && e.KeyChar <= '9') || (e.KeyChar == 8))
            {
                e.Handled = false;
            }
            else
            {
                e.Handled = true;
            }
        }

        private void tbNewPwd_KeyPress(object sender, KeyPressEventArgs e)
        {
            if ((e.KeyChar >= 'a' && e.KeyChar <= 'z') || (e.KeyChar >= 'A' && e.KeyChar <= 'Z')
                || (e.KeyChar >= '0' && e.KeyChar <= '9') || (e.KeyChar == 8))
            {
                e.Handled = false;
            }
            else
            {
                e.Handled = true;
            }
        }

        private void tbPwdConfirm_KeyPress(object sender, KeyPressEventArgs e)
        {
            if ((e.KeyChar >= 'a' && e.KeyChar <= 'z') || (e.KeyChar >= 'A' && e.KeyChar <= 'Z')
                || (e.KeyChar >= '0' && e.KeyChar <= '9') || (e.KeyChar == 8))
            {
                e.Handled = false;
            }
            else
            {
                e.Handled = true;
            }
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }
    }
}

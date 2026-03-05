using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

using System.Collections;

namespace NetClient
{
    public partial class UserAddForm : Form
    {
        private int m_iLogonID = -1;
        private bool m_bIsNvr = false;

        public UserAddForm()
        {
            InitializeComponent();
        }
        public UserAddForm(int _iLogonId, bool _bIsNvr)
        {
            InitializeComponent();

            m_iLogonID = _iLogonId;
            m_bIsNvr = _bIsNvr;
            
            // Initialize Authority Control
            ArrayList lsAuthority = new ArrayList();
            if (m_bIsNvr)
            {
                lsAuthority.Add(new TextAndValue("Common", 1));
                lsAuthority.Add(new TextAndValue("Admin", 4));
            }
            else
            {
                lsAuthority.Add(new TextAndValue("Browse", 1));
                lsAuthority.Add(new TextAndValue("Browse+Control", 2));
                lsAuthority.Add(new TextAndValue("Browse+Control+Set", 3));
                lsAuthority.Add(new TextAndValue("Admin", 4));
            }
            cmbAuthority.DataSource = lsAuthority;
            cmbAuthority.DisplayMember = "DisplayText";
            cmbAuthority.ValueMember = "RealValue";
            cmbAuthority.SelectedIndex = 0;

            tbUserName.MaxLength = 15;
            tbPwd.MaxLength = 15;
            tbPwdConfirm.MaxLength = 15;
        }

        private void btnAdd_Click(object sender, EventArgs e)
        {
            // Check Input info
            string strUserName = tbUserName.Text.Trim();
            string strNewPwd = tbPwd.Text.Trim();
            string strPwdConfirm = tbPwdConfirm.Text.Trim();
            if (strUserName.Length <= 0 || strNewPwd.Length <= 0 || strPwdConfirm.Length <= 0)
            {
                MessageBox.Show("Please enter the complete information.");
                return;
            }

            if (0 != strNewPwd.CompareTo(strPwdConfirm))
            {
                MessageBox.Show("The Pwd is different from the Password Confirm.");
                return;
            }

            string strIllCharName = "#\":;'\\@-_";
            string strIllChar = "#\":;'\\";
            for (int i = 0; i < strIllCharName.Length; i++)
            {
                if (strUserName.Contains(strIllCharName[i]))
                {
                    MessageBox.Show("User name contains illegal characters.");
                    return;
                }
            }
            for (int i = 0; i < strIllChar.Length; i++)
            {
                if (strNewPwd.Contains(strIllChar[i]))
                {
                    MessageBox.Show("Password contains illegal characters.");
                    return;
                }
            }

            // Check for duplicate user name
            int iUserNum = 0;
            int iRet = NVSSDK.NetClient_GetUserNum(m_iLogonID, ref iUserNum);

            if (iRet < 0)
            {
                Console.WriteLine("NetClient_GetUserNum error");
                return;
            }

            int iAuthority = 0;
            byte[] btUserName = new byte[128];
            byte[] btPassword = new byte[128];

            for (int i = 0; i < iUserNum; i++)
            {
                iRet = NVSSDK.NetClient_GetUserInfo(m_iLogonID, i, btUserName, btPassword, ref iAuthority);
                if (iRet < 0)
                {
                    Console.WriteLine("NetClient_GetUserInfo error");
                    continue;
                }
                string strOldUsername = Encoding.ASCII.GetString(btUserName);

                if (0 == strUserName.CompareTo(strOldUsername))
                {
                    MessageBox.Show("The username already exists. Please re-enter it.");
                    return;
                }
            }

            iAuthority = Convert.ToInt32(cmbAuthority.SelectedValue.ToString());

            // Modify Password
            iRet = NVSSDK.NetClient_AddUser(m_iLogonID, strUserName, strNewPwd, iAuthority);
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_GetUserInfo error");
                return;
            }

            this.Close();
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void tbUserName_KeyPress(object sender, KeyPressEventArgs e)
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

        private void tbPwd_KeyPress(object sender, KeyPressEventArgs e)
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
    }
}

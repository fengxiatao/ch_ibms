using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace NetClient
{
    public partial class UserManagementForm : Form
    {
        const int STR_AUTHORITY_NUM_MAX = 5;
        public int m_iLogonID = -1;

        private bool m_bIsNvr = true;   // default is nvr
        private string[] m_lsAuthorityStr = new string[STR_AUTHORITY_NUM_MAX];
        
        public UserManagementForm()
        {
            InitializeComponent();
            
            if (m_bIsNvr)
            {
                m_lsAuthorityStr[0] = "Common";
                m_lsAuthorityStr[1] = "Privilege";
                m_lsAuthorityStr[2] = "Super";
                m_lsAuthorityStr[3] = "Admin";
                m_lsAuthorityStr[4] = "Default";
            }
            else
            {
                m_lsAuthorityStr[0] = "Browse";
                m_lsAuthorityStr[1] = "Browse+Control";
                m_lsAuthorityStr[2] = "Browse+Control+Set";
                m_lsAuthorityStr[3] = "Admin";
                m_lsAuthorityStr[4] = "Default";
            }
        }

        private void btnAdd_Click(object sender, EventArgs e)
        {
            UserAddForm frmAdd = new UserAddForm(m_iLogonID, m_bIsNvr);
            frmAdd.ShowDialog();

            // wait for 1s.  device send the Latest information
            System.Threading.Thread.Sleep(1000);
            
            // after adding user, update user's list
            UpdateUserInfo();
        }

        private void btnModify_Click(object sender, EventArgs e)
        {
            if (lvUsers.CheckedItems.Count <= 0)
            {
                return;
            }
            // Modify the first selected item
            string strUserName = lvUsers.CheckedItems[0].SubItems[1].Text;
            int iAuthority = (int)lvUsers.CheckedItems[0].SubItems[0].Tag;
            string strPwd = (string)lvUsers.CheckedItems[0].SubItems[1].Tag;
            UserModifyForm frmModify = new UserModifyForm(m_iLogonID, strUserName, strPwd, iAuthority, m_bIsNvr);
            frmModify.ShowDialog();
        }

        private void btnDelete_Click(object sender, EventArgs e)
        {
            if (lvUsers.CheckedItems.Count <= 0)
            {
                return;
            }
            DialogResult dlret = MessageBox.Show("Delete this user?", "Confirm Message", MessageBoxButtons.OKCancel);
            if (dlret != DialogResult.OK)
            {
                return;
            }

            // Get current user
            int iAuthority = 0;
            byte[] btUserName = new byte[128];
            byte[] btPassword = new byte[128];
            int iRet = NVSSDK.NetClient_GetCurUserInfo(m_iLogonID, btUserName, btPassword, ref iAuthority);
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_GetCurUserInfo");
                return;
            }
            string strCurUsername = Encoding.ASCII.GetString(btUserName).ToUpper();
            
            // Can not delete "Admin", "Default" and current user.
            foreach (ListViewItem one in lvUsers.CheckedItems)
            {
                string strdeleteUser = one.SubItems[1].Text.ToUpper();
                if (0 == strdeleteUser.CompareTo("Admin".ToUpper()) ||
                    0 == strdeleteUser.CompareTo("Default".ToUpper()) ||
                    0 == strdeleteUser.CompareTo(strCurUsername.ToUpper()))
                {
                    MessageBox.Show("Can not delete this user:" + one.SubItems[1].Text);
                    continue;
                }

                // delete selected users
                iRet = NVSSDK.NetClient_DelUser(m_iLogonID, one.SubItems[1].Text);
                if (iRet < 0)
                {
                    Console.WriteLine("NetClient_GetCurUserInfo");
                    return;
                }
            }

            // wait for 1s.  device send the Latest information
            System.Threading.Thread.Sleep(1000);

            // update user's list
            UpdateUserInfo();

        }

        private void UserManagementForm_Shown(object sender, EventArgs e)
        {
            UpdateUserInfo();
        }   
        
        private void UpdateUserInfo()
        {
            lvUsers.Items.Clear();

            // Get User Number
            int iUserNum = 0;
            int iRet = NVSSDK.NetClient_GetUserNum(m_iLogonID, ref iUserNum);

            if (iRet < 0)
            {
                Console.WriteLine("NetClient_GetUserNum error");
                return;
            }
            for (int i = 0; i < iUserNum; i++)
            {
                int iAuthority = 0;
                byte[] btUserName = new byte[128];
                byte[] btPassword = new byte[128];

                iRet = NVSSDK.NetClient_GetUserInfo(m_iLogonID, i, btUserName, btPassword, ref iAuthority);
                if (iRet < 0)
                {
                    Console.WriteLine("NetClient_GetUserInfo error");
                    continue;
                }
                string strUsername = Encoding.ASCII.GetString(btUserName);
                string strPassword = Encoding.ASCII.GetString(btPassword);
                string strAuthority = string.Empty;
                if ((iAuthority-1) >= 0 && (iAuthority-1) < STR_AUTHORITY_NUM_MAX)
                {
                    strAuthority = m_lsAuthorityStr[iAuthority-1];
                }
                
                ListViewItem one = new ListViewItem();
                one.Tag = i;
                one.SubItems.Add(strUsername);
                one.SubItems.Add(strAuthority);
                one.SubItems.Add(strPassword);
                one.SubItems[0].Tag = iAuthority;
                one.SubItems[1].Tag = strPassword;
                lvUsers.Items.Add(one);

            }  // end of for (int i = 0; i < iUserNum; i++)

        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }
        
    }
}

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.Threading;

namespace NetClient
{
    public partial class FormDSM : Form
    {
        private const int REG_PAGE_SIZE = 20;
        private int m_iServerID;
        private DNSList_NOTIFY DnsNotify = null;
        private NVSList_NOTIFY NvsNotify = null;

        //Connect to the ID number of the registry server
        public int ServerID
        {
            get { return m_iServerID; }
            set { m_iServerID = value; }
        }
        private string m_strServerIP;

        //Registration center server IP
        public string ServerIP
        {
            get { return m_strServerIP; }
            set { m_strServerIP = value; }
        }       
        private int m_iServerPort;

        //The port number of the registry server
        public int ServerPort
        {
            get { return m_iServerPort; }
            set { m_iServerPort = value; }
        }        
        private string m_strUserName;

        //Username to connect to the registry server
        public string UserName
        {
            get { return m_strUserName; }
            set { m_strUserName = value; }
        }        
        private string m_strPassword;

        //Password to connect to the registry server
        public string Password
        {
            get { return m_strPassword; }
            set { m_strPassword = value; }
        }       
        public FormDSM()
        {
            InitializeComponent();
            //Control.CheckForIllegalCrossThreadCalls = false;
        }

        //Convert byte array to string
        private string Bytes2Str(byte[] _btData)
        {
            //Get the address of byte 0 in the byte array
            int ilen = Array.IndexOf<byte>(_btData,0);
            if (ilen < 0)
            {
                ilen = _btData.Length;
            }

            // get string from byte array
            return Encoding.Default.GetString(_btData,0, ilen);
        }

        // make NVS query
        private void btnNVSQuery_Click(object sender, EventArgs e)
        {
            //When the NVS ID is empty, it cannot be queried
            if (textNVSID.Text.Trim() == "")
            {
                MessageBox.Show("NVS ID cannot be empty! ");
                return;
            }

            //create structure
            REG_NVS stRegNVS = new REG_NVS();

            //Assign the Byte array
            Array.Copy(Encoding.ASCII.GetBytes(textNVSID.Text), stRegNVS.m_stNvs.m_btFactoryID, textNVSID.Text.Length);

            //Clear DataGridView content
            dgvNVS.Rows.Clear();

            int iRet = -1;
            IntPtr pNvs = IntPtr.Zero;
            try
            {
                // Apply for the memory required by the structure REG_NVS
                pNvs = Marshal.AllocHGlobal(Marshal.SizeOf(stRegNVS));

                //Store stRegNVS in the memory just applied for
                Marshal.StructureToPtr(stRegNVS, pNvs, true);

                //Query NVS device information
                iRet = NVSSDK.NSLook_Query(ServerID, IntPtr.Zero, pNvs, NVSSDK.TYPE_NVS);

                //Read the value of stRegNVS from memory
                stRegNVS = (REG_NVS)Marshal.PtrToStructure(pNvs, typeof(REG_NVS));
            }
            catch (Exception ex)
            {
                // Output the exception to the Output window
                Debug.WriteLine(ex.Message);
            }
            finally
            {
                // release memory
                Marshal.FreeHGlobal(pNvs);
            }

            // query failed, exit
            if (iRet != 0)
            {
                MessageBox.Show("NSLook_Query NVS Error ! " + iRet);
                return;
            }
           
            //Add new row to DatagGridView
            dgvNVS.Rows.Add
            (
                new object[] 
                { 
                    Bytes2Str(stRegNVS.m_stNvs.m_btNvsIP),
                    Bytes2Str(stRegNVS.m_stNvs.m_btNvsName), 
                    stRegNVS.m_stNvs.m_iNvsType, 
                    Bytes2Str(stRegNVS.m_stNvs.m_btFactoryID)
                }
            );
        }

        //Get the list of NVS devices
        private void btnNVSRefresh_Click(object sender, EventArgs e)
        {
            int iCount = 0;

            //Get the number of NVS devices
            int iRet = NVSSDK.NSLook_GetCount
                       (
                            ServerID,
                            Encoding.ASCII.GetBytes(UserName), 
                            Encoding.ASCII.GetBytes(Password), 
                            ref iCount, NVSSDK.TYPE_NVS
                       );
            if ( iRet != 0)
            {
                MessageBox.Show("NSLook_GetCount NVS Error ! " + iRet);
                textNVSCount.Text = "0";
                return;
            }
            textNVSCount.Text = iCount.ToString();

            //Save the callback to prevent garbage collection and exceptions
            NvsNotify = NVSListNotify; 
            if (iCount > 0)
            {
                //Clear the contents of the DataGridView
                dgvNVS.Rows.Clear();

                //Get the NVS list
                iRet = NVSSDK.NSLook_GetList
                       (
                           ServerID,
                           Encoding.ASCII.GetBytes(UserName),
                           Encoding.ASCII.GetBytes(Password),
                           0,
                           null,
                           NvsNotify,
                           NVSSDK.TYPE_NVS
                       );   

                //operation failed       
                if (iRet != 0)
                {
                    MessageBox.Show("NSLook_GetList NVS Error ! " + iRet);
                }
            }
        }

        //The callback function to get the NVS list must use IntPtr, otherwise the value transfer fails
        private void NVSListNotify(Int32 _iCount, IntPtr _pNvs)
        {
            dgvNVS.Rows.Clear();
            for (int i = 0; i < _iCount; i++)
            {   
                NvsSingle stNvs = new NvsSingle();

                //Move the pointer backward, read the next NvsSingle structure
                IntPtr pNvs = (IntPtr)((UInt32)_pNvs + i * Marshal.SizeOf(stNvs));

                //Read NvsSingle structure data
                stNvs = (NvsSingle)Marshal.PtrToStructure(pNvs, typeof(NvsSingle));
                string strNvsIP = Bytes2Str(stNvs.m_btNvsIP);
                string strNvsName = Bytes2Str(stNvs.m_btNvsName);
                int iNvsType = stNvs.m_iNvsType;
                string strFactoryID = Bytes2Str(stNvs.m_btFactoryID);

                //Create an anonymous delegate to handle cross-thread modification of DataGridView control properties
                MethodInvoker notify = delegate()
                {
                    //Add a new row to the DataGridView
                    dgvNVS.Rows.Add
                    (
                         new object[]
                         { 
                            strNvsIP,
                            strNvsName,
                            iNvsType,
                            strFactoryID
                         }
                    );

                    //Refresh DataGridView
                    dgvNVS.Invalidate();
                };

                // Give the anonymous delegate to the DataGridView control for processing
                dgvNVS.Invoke(notify);              
            }            
        }

        private void rdoDNSID_CheckedChanged(object sender, EventArgs e)
        {
            textDNSDomainName.Text = "";
            textDNSID.ReadOnly = false;
            textDNSDomainName.ReadOnly = true; 
        }

        private void rdoDNSDomainName_CheckedChanged(object sender, EventArgs e)
        {
            textDNSID.Text = "";
            textDNSID.ReadOnly = true;
            textDNSDomainName.ReadOnly = false;           
        }

        // Perform DNS query operation
        private void btnDNSQuery_Click(object sender, EventArgs e)
        {
            //Create REG_DNS structure
            REG_DNS stRegDNS = new REG_DNS();

            // query by ID
            if (rdoDNSID.Checked)
            {
                if (textDNSID.Text.Trim() == "")
                {
                    MessageBox.Show("DNS ID cannot be empty! ");
                    return;
                }

                // Byte array to assign
                Array.Copy(Encoding.ASCII.GetBytes(textDNSID.Text), stRegDNS.m_stDNSInfo.m_stNvs.m_btFactoryID, textDNSID.Text.Length);
            }
            else
            {
                // Query by domain name
                if (textDNSDomainName.Text.Trim() == "")
                {
                    MessageBox.Show("DNS DomainName cannot be empty! ");
                    return;
                }

                // Byte array to assign
                Array.Copy(Encoding.ASCII.GetBytes(textDNSDomainName.Text), stRegDNS.m_stDNSInfo.m_stNvs.m_btNvsName, textDNSDomainName.Text.Length);
            }
            Array.Copy(Encoding.ASCII.GetBytes(UserName), stRegDNS.m_stDNSInfo.m_btUserName, UserName.Length);
            Array.Copy(Encoding.ASCII.GetBytes(Password), stRegDNS.m_stDNSInfo.m_btPwd, Password.Length); 
           
            //Clear DataGridView
            dgvDNS.Rows.Clear();

            int iRet = -1;
            IntPtr pDns = IntPtr.Zero;
            try
            {
                // Apply for the memory required by the structure REG_DNS
                pDns = Marshal.AllocHGlobal(Marshal.SizeOf(stRegDNS));

                //Store stRegDNS in the memory just applied for
                Marshal.StructureToPtr(stRegDNS, pDns, true);

                //Query DNS device information
                iRet = NVSSDK.NSLook_Query(ServerID, pDns, IntPtr.Zero, NVSSDK.TYPE_DNS);

                //Read the value of stRegDNS from memory
                stRegDNS = (REG_DNS)Marshal.PtrToStructure(pDns, typeof(REG_DNS));                
            }
            catch(Exception ex)
            {
                // Output the exception to the Output window
                Debug.WriteLine(ex.Message);
            }
            finally
            {
                // release memory
                Marshal.FreeHGlobal(pDns);
            }

            //operation failed
            if (iRet != 0)
            {
                MessageBox.Show("NSLook_Query DNS Error ! " + iRet);
                return;
            }
            
            //Add a new row to the DataGridView
            dgvDNS.Rows.Add
            (
                new object[] 
                { 
                    Bytes2Str(stRegDNS.m_stDNSInfo.m_stNvs.m_btNvsName),
                    Bytes2Str(stRegDNS.m_stDNSInfo.m_stNvs.m_btFactoryID),
                    Bytes2Str(stRegDNS.m_stDNSInfo.m_stNvs.m_btNvsIP),
                    Bytes2Str(stRegDNS.m_stDNSInfo.m_stReserve.m_btReserved1),
                    stRegDNS.m_stDNSInfo.m_iPort,
                    stRegDNS.m_stDNSInfo.m_stNvs.m_iNvsType,
                    stRegDNS.m_stDNSInfo.m_iChannel,
                    "",
                    "",
                    DateTime.Now
                }
            );
        }

        //update page number
        private void btnDNSRefresh_Click(object sender, EventArgs e)
        {
            cboDNSPage.SelectedIndex = -1;
            int iCount = 0;

            //Get the number of DNS devices
            int iRet = NVSSDK.NSLook_GetCount
                       (
                            ServerID,
                            Encoding.ASCII.GetBytes(UserName),
                            Encoding.ASCII.GetBytes(Password),
                            ref iCount, NVSSDK.TYPE_DNS
                       );

            // get failed and exit
            if (iRet != 0)
            {
                MessageBox.Show("NSLook_GetCount DNS Error ! " + iRet);
                textDNSCount.Text = "0";
                return;
            }
            cboDNSPage.Items.Clear();
            if (iCount <= 0)
            {
                textDNSCount.Text = "0";
                return;
            }
            textDNSCount.Text = iCount.ToString();
            int iPage = 0;

            //calculate the number of pages
            iPage = iCount / REG_PAGE_SIZE + iCount % REG_PAGE_SIZE == 0 ? 0 : 1;

            //Add sub-items to the page number drop-down menu
            for (int i = 0; i < iPage; i++)
            {
                cboDNSPage.Items.Add(i + 1);
            }

            //Select the first page by default
            cboDNSPage.SelectedIndex = 0;            
        }

        //Select the page number and update the DataGridView list
        private void cboDNSPage_SelectedIndexChanged(object sender, EventArgs e)
        {
            int iPage = cboDNSPage.SelectedIndex;
            if (iPage < 0)
            {
                return;
            }
            dgvDNS.Rows.Clear();

            //Save the callback to prevent garbage collection and exceptions
            DnsNotify = DNSListNotify;

            //Get a list of DNS devices
            int iRet = NVSSDK.NSLook_GetList
            (
                ServerID,
                Encoding.ASCII.GetBytes(UserName),
                Encoding.ASCII.GetBytes(Password),
                iPage,
                DnsNotify,
                null,
                NVSSDK.TYPE_DNS
            );

            //Refresh DataGridView
            dgvDNS.Invalidate();
            
            if (iRet != 0)
            {
                MessageBox.Show("NSLook_GetList DNS Error ! " + iRet);
            }
        }

        //Callback function to get DNS list
        private void DNSListNotify(Int32 _iCount,IntPtr _pDns)
        {
            for (int i = 0; i < _iCount; i++)
            {
                //Create structure REG_DNS
                REG_DNS stDns = new REG_DNS();

                // Move the pointer down to read the next data
                IntPtr pDns = (IntPtr)((UInt32)_pDns + i * Marshal.SizeOf(stDns));
                stDns = (REG_DNS)Marshal.PtrToStructure(pDns, typeof(REG_DNS));
                string strNvsName = Bytes2Str(stDns.m_stDNSInfo.m_stNvs.m_btNvsName);
                string strFactoryID = Bytes2Str(stDns.m_stDNSInfo.m_stNvs.m_btFactoryID);
                string strLANIP = Bytes2Str(stDns.m_stDNSInfo.m_stNvs.m_btNvsIP);
                string strWANIP = Bytes2Str(stDns.m_stDNSInfo.m_stReserve.m_btReserved1);
                int iPort = stDns.m_stDNSInfo.m_iPort;
                int iNvsType = stDns.m_stDNSInfo.m_stNvs.m_iNvsType;
                int iChannel = stDns.m_stDNSInfo.m_iChannel;
                string strAccount = "";
                string strPwd = "";

                //Create an anonymous delegate to handle cross-thread modification of DataGridView control properties
                MethodInvoker notify = delegate()
                {
                    //Create new row for DataGridView
                    dgvDNS.Rows.Add
                    (
                         new object[]
                         { 
                            strNvsName,
                            strFactoryID,
                            strLANIP,
                            strWANIP,
                            iPort,
                            iNvsType,
                            iChannel,
                            strAccount,
                            strPwd,
                            DateTime.Now
                         }
                    );  
                };

                // Give the anonymous delegate to the DataGridView control for processing
                dgvDNS.Invoke(notify);                
            }            
        }

        //Click the unit event handler of the DataGridView control dgvNVS
        private void dgvNVS_CellClick(object sender, DataGridViewCellEventArgs e)
        {
            try
            {
                //Modify the ID value of textNVSID
                textNVSID.Text = dgvNVS.Rows[e.RowIndex].Cells["ID"].Value.ToString();
            }
            catch
            {
                textNVSID.Text = "";
            }
        }

        //Double-click the unit event handler of the DataGridView control dgvNVS
        private void dgvNVS_CellDoubleClick(object sender, DataGridViewCellEventArgs e)
        {
            //Get the IP address of the double-clicked network video server
            string strIP = dgvNVS.Rows[e.RowIndex].Cells["NVSIP"].Value.ToString(); 
            if (strIP.Trim() == "")
            {
                return;
            }

            // Perform the login operation of the network video server in the main form
            ((Client)this.Owner).Logon(strIP);
        }

        //Click the unit event handler of the DataGridView control dgvDNS
        private void dgvDNS_CellClick(object sender, DataGridViewCellEventArgs e)
        {
            try
            {
                if (rdoDNSID.Checked)
                {
                    //Modify the ID value of textDNSID
                    textDNSID.Text = dgvDNS.Rows[e.RowIndex].Cells["DNSID"].Value.ToString();
                }
                else
                {
                    //Modify the domain name of textDNSDomainName
                    textDNSDomainName.Text = dgvDNS.Rows[e.RowIndex].Cells["DNSName"].Value.ToString();
                }
            }
            catch
            {
                textDNSID.Text = "";
                textDNSDomainName.Text = "";
            }
        }

        //Double-click the unit event handler of the DataGridView control dgvDNS
        private void dgvDNS_CellDoubleClick(object sender, DataGridViewCellEventArgs e)
        {
            //Get the IP address of the double-clicked network video server
            string strIP = dgvDNS.Rows[e.RowIndex].Cells["LANIP"].Value.ToString();
            if (strIP.Trim() == "")
            {
                return;
            }

            // Perform the login operation of the network video server in the main form
            ((Client)this.Owner).Logon(strIP);            
        }

        //close the form
        private void FormDSM_FormClosed(object sender, FormClosedEventArgs e)
        {
            //Modify the formDSM field of the main form to be empty
            ((Client)this.Owner).LogofServer();
        }
    }
}

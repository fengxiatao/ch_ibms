using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Diagnostics;
using System.Runtime.InteropServices;

namespace NetClient
{
    public partial class Query : Form
    {
        private Int32 m_iLogonID = -1;
        private const Int32 m_iPageSize = 10;
        private Int32 m_iTotalFiles = 0;
        private NVS_FILE_QUERY m_nvsQuery;
        private Int32 m_iPageNO = 0;
        private UInt32 m_uiConID = UInt32.MaxValue;//Identification of the downloaded file

        public Query()
        {
            InitializeComponent();
        }

        private void Query_Load(object sender, EventArgs e)
        {
            cboFileType.SelectedIndex = 0;
            cboRecType.SelectedIndex = 0;
            Int32 iChannelNum = 0;
            NVSSDK.NetClient_GetChannelNum(m_iLogonID, ref iChannelNum);
            if (iChannelNum <= 0)
            {
                throw new Exception("ChannelNum is "+iChannelNum+"!");
            }
            for (int i = 0; i < iChannelNum; ++i)
            {
                cboChannel.Items.Add(i.ToString());
            }
            cboChannel.SelectedIndex = 0;
            timeBegin.Value.AddYears(-1);            
        }

        public void Init(Int32 _iLogonID)
        {
            m_iLogonID = _iLogonID;
        }

        private void btnQuery_Click(object sender, EventArgs e)
        {
            gridFiles.Rows.Clear();
            m_iTotalFiles = 0;
            m_iPageNO = 0;

            NVS_FILE_TIME nvsFileTimeBegin;
            nvsFileTimeBegin.iYear = (UInt16)dateBegin.Value.Year;
            nvsFileTimeBegin.iMonth = (UInt16)dateBegin.Value.Month;
            nvsFileTimeBegin.iDay = (UInt16)dateBegin.Value.Day;
            nvsFileTimeBegin.iHour = (UInt16)timeBegin.Value.Hour;
            nvsFileTimeBegin.iMinute = (UInt16)timeBegin.Value.Minute;
            nvsFileTimeBegin.iSecond = (UInt16)timeBegin.Value.Second;

            NVS_FILE_TIME nvsFileTimeEnd;
            nvsFileTimeEnd.iYear = (UInt16)dateEnd.Value.Year;
            nvsFileTimeEnd.iMonth = (UInt16)dateEnd.Value.Month;
            nvsFileTimeEnd.iDay = (UInt16)dateEnd.Value.Day;
            nvsFileTimeEnd.iHour = (UInt16)timeEnd.Value.Hour;
            nvsFileTimeEnd.iMinute = (UInt16)timeEnd.Value.Minute;
            nvsFileTimeEnd.iSecond = (UInt16)timeEnd.Value.Second;

            Int32 iRecType = cboRecType.SelectedIndex;
            if (iRecType == 0)
            {
                iRecType = 0xFF;
            }
            m_nvsQuery.iChannel = cboChannel.SelectedIndex;
             m_nvsQuery.iFiletype = 1/*cboFileType.SelectedIndex*/;//Audio and video files
            m_nvsQuery.iType = iRecType;
            m_nvsQuery.iDevType = 0;            
            m_nvsQuery.struStartTime = nvsFileTimeBegin;
            m_nvsQuery.struStopTime = nvsFileTimeEnd;
            QueryOnePage(m_iPageNO, m_iPageSize);            
        }

        private void QueryOnePage(Int32 _iPageNO, Int32 _iPageSize)
        {
            m_nvsQuery.iPageSize = _iPageSize;
            m_nvsQuery.iPageNo = _iPageNO;
            Int32 iRet = NVSSDK.NetClient_NetFileQuery(m_iLogonID, ref m_nvsQuery);
            if (iRet < 0)
            {
                Trace.WriteLine("NetFileQuery Error!");
            }
        }

        public void QueryFinishedNotify(IntPtr wParam, IntPtr lParam)
        {
            NVS_IPAndID IPID = (NVS_IPAndID)Marshal.PtrToStructure(lParam, typeof(NVS_IPAndID));
            Int32 iTotalFiles = 0;
            Int32 iCurrentCnt = 0;
            if (Marshal.ReadInt32((IntPtr)IPID.m_puiLogonID) == m_iLogonID)
            {
                gridFiles.Rows.Clear();
                NVSSDK.NetClient_NetFileGetFileCount(m_iLogonID, ref iTotalFiles, ref iCurrentCnt);
                m_iTotalFiles = iTotalFiles;
                lblTotalFiles.Text = "TotalFiles:"+iTotalFiles;
                lblTotalPages.Text = "TotalPages:"+((iTotalFiles + m_iPageSize - 1) / m_iPageSize);
                lblCurrentPage.Text = "CurrentPage:" + (m_iTotalFiles > 0 ? (m_iPageNO + 1) : 0).ToString();
                if (iTotalFiles > 0)
                {
                    NVS_FILE_DATA stFileData;
                    stFileData.iType = new Int32();
                    stFileData.iChannel = new Int32();
                    stFileData.btFileName = new byte[250];
                    stFileData.struStartTime = new NVS_FILE_TIME();
                    stFileData.struStoptime = new NVS_FILE_TIME();
                    stFileData.iFileSize = new Int32();
                    
                    for (int i = 0; i < iCurrentCnt; i++)
                    {
                        Int32 iRet = NVSSDK.NetClient_NetFileGetQueryfile(m_iLogonID, i, ref stFileData);
                        if (iRet == 0)
                        {
                            DateTime timeStart = new DateTime(
                                stFileData.struStartTime.iYear,
                                stFileData.struStartTime.iMonth,
                                stFileData.struStartTime.iDay,
                                stFileData.struStartTime.iHour,
                                stFileData.struStartTime.iMinute,
                                stFileData.struStartTime.iSecond,
                                0
                            );
                             //Add a new row to the DataGridView
                            gridFiles.Rows.Add
                            (
                                 new object[]
                                 { 
                                    Bytes2Str(stFileData.btFileName),
                                    cboRecType.Items[stFileData.iType],
                                    stFileData.iChannel,
                                    stFileData.iFileSize,
                                    timeStart.ToShortDateString()+" "+timeStart.ToLongTimeString()
                                 }
                            );

                             //Refresh DataGridView
                            gridFiles.Invalidate();
                        }
                    }
                    gridFiles.Rows[0].Selected = true;
                }
            }		
        }

        private void btnFirstPage_Click(object sender, EventArgs e)
        {
            m_iPageNO = 0;
            QueryOnePage(m_iPageNO, m_iPageSize);
        }

        private void btnPrePage_Click(object sender, EventArgs e)
        {
            if (m_iPageNO > 0)
            {
                QueryOnePage(--m_iPageNO, m_iPageSize);
            }
        }

        private void btnNextPage_Click(object sender, EventArgs e)
        {
            if (m_iPageNO+1 < (m_iTotalFiles + m_iPageSize - 1) / m_iPageSize)
            {
                QueryOnePage(++m_iPageNO, m_iPageSize);
            }
        }

        private void btnLastPage_Click(object sender, EventArgs e)
        {
            m_iPageNO = (m_iTotalFiles + m_iPageSize - 1) / m_iPageSize - 1;
            if (m_iPageNO < 0)
            {
                m_iPageNO = 0;
            }
            QueryOnePage(m_iPageNO, m_iPageSize);
        }

        private void btnDownload_Click(object sender, EventArgs e)
        {
            if (btnDownload.Text.Equals("Download"))
            {
                if (gridFiles.Rows.Count <= 0 || gridFiles.SelectedRows.Count <= 0)
                {
                    return;
                }
                string strFilename = (string)gridFiles.SelectedRows[0].Cells["FileName"].Value;
                Int32 iRet = NVSSDK.NetClient_NetFileDownloadFile(ref m_uiConID, m_iLogonID, Encoding.ASCII.GetBytes(strFilename), Encoding.ASCII.GetBytes("D:\\NetVideoBrowser\\" + strFilename), 0, -1, 1);
                if (iRet == 0)
                {
                    btnDownload.Text = "Cancel";
                }
                else
                {
                    btnDownload.Text = "Download";
                    MessageBox.Show("download failed!");
                }
            }
            else
            {
                Int32 iRet = NVSSDK.NetClient_NetFileStopDownloadFile(m_uiConID);
                if (iRet == 0)
                {
                    btnDownload.Text = "Download";
                }
                else
                {
                    MessageBox.Show("stop download failed!");
                }                
            }            
        }

         //Convert byte array to string
        private string Bytes2Str(byte[] _btData)
        {
             //Get the address of byte 0 in the byte array
            int ilen = Array.IndexOf<byte>(_btData, 0);
            if (ilen < 0)
            {
                ilen = _btData.Length;
            }

             // get string from byte array
            return Encoding.Default.GetString(_btData, 0, ilen);
        }

        private void gridFiles_CellClick(object sender, DataGridViewCellEventArgs e)
        {
            gridFiles.Rows[e.RowIndex].Selected = true;
        }

        public void DownLoadFinishedNotify(IntPtr wParam, IntPtr lParam)
        {
            btnDownload.Text = "Download";
            MessageBox.Show("download finished!");
        }

        public void DownLoadInterruptNotify(IntPtr wParam, IntPtr lParam)
        {
            btnDownload.Text = "Download";
            MessageBox.Show("download interrupt!");
        }

        public void DownLoadFaultNotify(IntPtr wParam, IntPtr lParam)
        {
            btnDownload.Text = "Download";
            MessageBox.Show("download fault!");
        }
    }
}

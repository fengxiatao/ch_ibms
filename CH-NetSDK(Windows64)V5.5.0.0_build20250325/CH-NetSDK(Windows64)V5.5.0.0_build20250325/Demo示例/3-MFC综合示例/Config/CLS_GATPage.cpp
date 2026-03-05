#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_GATPage.h"

// CLS_LinkHttp dialog

IMPLEMENT_DYNAMIC(CLS_GATPage, CDialog)

CLS_GATPage::CLS_GATPage(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_GATPage::IDD, pParent)
{
    m_iLogonID = -1;
    m_iChannelNo = -1;
}

CLS_GATPage::~CLS_GATPage()
{

}

void CLS_GATPage::DoDataExchange(CDataExchange* pDX)
{
    CDialog::DoDataExchange(pDX);

    DDX_Control(pDX, IDC_CHK_GAT_ENABLE, m_chkEnalbe);
    DDX_Control(pDX, IDC_EDT_GAT_IPADDRESS, m_edtIpAddress);
    DDX_Control(pDX, IDC_EDT_GAT_PORT, m_edtPort);
    DDX_Control(pDX, IDC_EDT_GAT_DEVICEID, m_edtDeviceId);
    DDX_Control(pDX, IDC_EDT_GAT_USERNAME, m_edtUserName);
    DDX_Control(pDX, IDC_EDT_GAT_PASSWORD, m_edtPassword);
    DDX_Control(pDX, IDC_EDT_GAT_HEARTBEATINTERVAL, m_edtHeartBeatInterval);
    DDX_Control(pDX, IDC_EDT_GAT_HEARTBEATTIME, m_edtHeartBeatTime);
    DDX_Control(pDX, IDC_EDT_GAT_PLACECODE, m_edtPlaceCode);
    DDX_Control(pDX, IDC_EDT_GAT_LONGITUDE, m_edtLongitude);
    DDX_Control(pDX, IDC_EDT_GAT_LATITUDE, m_edtLatitude);
    DDX_Control(pDX, IDC_CHK_GAT_TIMINGENABLE, m_chkTimingEnable);
    DDX_Control(pDX, IDC_EDT_GAT_RETRYTIME, m_edtRetryTime);
    DDX_Control(pDX, IDC_EDT_GAT_RETRYINTEVAL, m_edtRetryInterval);
    DDX_Control(pDX, IDC_EDT_GAT_TIMINGINTERVAL, m_edtTimingInterval);
    DDX_Control(pDX, IDC_EDT_GAT_CONFFILENO, m_edtConfFileNo);
    DDX_Control(pDX, IDC_EDT_GAT_CHANNELCOUNT, m_edtChannelCount);

    DDX_Control(pDX, IDC_BUTTON_ONLINE, m_btnOnline);
    DDX_Control(pDX, IDC_BUTTON_SET, m_btnSet);
    DDX_Control(pDX, IDC_LST_CFG_GAT_LIST, m_lstNpupList);

}

BEGIN_MESSAGE_MAP(CLS_GATPage, CDialog)
    ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_GATPage::OnBnClickedButtonSet)
    ON_BN_CLICKED(IDC_BUTTON_ONLINE, &CLS_GATPage::OnBnClickedButtonOnline)
    ON_BN_CLICKED(IDC_BUTTON_ADD_ROW, &CLS_GATPage::OnBnClickedButtonAddRow)
END_MESSAGE_MAP()

BOOL CLS_GATPage::OnInitDialog()
{
    CLS_BasePage::OnInitDialog();

    // TODO:  Add extra initialization here
    UI_UpdateDialogText();
    return TRUE;  // return TRUE unless you set the focus to a control
    // EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_GATPage::UI_UpdateDialogText()
{
    UI_UpdateListInfo();
}

void CLS_GATPage::UI_UpdateListInfo()
{
    if (m_iLogonID < 0)
    {
        //AddLog(LOG_TYPE_FAIL,"","[CLS_GATPage::UI_UpdateListInfo]Invalid logon id(%d)", m_iLogonID);
        return;
    }

    // port config
    m_lstNpupList.SetExtendedStyle(LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT);
    m_lstNpupList.DeleteAllColumns();
    m_lstNpupList.InsertColumn(n_LIST_NO, GetTextByLan("通道号","ChannelNo"), LVCFMT_CENTER, 100);
    m_lstNpupList.InsertColumn(n_LIST_ID, GetTextByLan("通道编号","ChannelID"), LVCFMT_CENTER, 100);

    m_lstNpupList.DeleteAllItems();
    m_lstNpupList.SetItemControl(EDITBOX, n_LIST_NO);
    m_lstNpupList.SetItemControl(EDITBOX, n_LIST_ID);
    Gat1400Para tPara = {0};
    memset(&tPara, 0, sizeof(tPara));
    tPara.iSize = sizeof(tPara);
    int iReturnByte = 0;
    int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GAT1400_PARA, m_iChannelNo, &tPara, sizeof(tPara), &iReturnByte);
    if (RET_SUCCESS > iRet)
    {
        AddLog(LOG_TYPE_FAIL, "", "[CLS_GATPage::UI_UpdateListInfo][NET_CLIENT_GAT1400_PARA] get fail,error = %d", GetLastError());
    }
    else
    {
        m_chkEnalbe.SetCheck(tPara.iEnable);        //Whether to enable 0 not enable, enable
        m_edtIpAddress.SetWindowText(tPara.cIpAddress);//ip address no more than bytes
        m_edtPort.SetWindowText(IntToCString(tPara.iPort));            //server port	1-65535
        m_edtDeviceId.SetWindowText(tPara.cDeviceID);   //	device number no more than bytes
        m_edtUserName.SetWindowText(tPara.cUserName); //	Account name no more than bytes
        m_edtPassword.SetWindowText(tPara.cPassWord); //	password no more than bytes
        m_edtHeartBeatInterval.SetWindowText(IntToCString(tPara.iHeartBeatInterval)); // Heartbeat interval time in seconds, range ~255 seconds
        m_edtHeartBeatTime.SetWindowText(IntToCString(tPara.iHeartBeatTimes)); //Number of heartbeats Range~255
        m_edtPlaceCode.SetWindowText(tPara.cPlaceCode); //Administrative code no more than bytes
        m_edtLongitude.SetWindowText(IntToCString(tPara.iLongitude)); //Longitude range -36000 corresponds to -180~+180, accurate to .01 (-E +W)
        m_edtLatitude.SetWindowText(IntToCString(tPara.iLatitude)); //Latitude range -18000 corresponds to -90~+90, accurate to .01 (-S +N)
        m_chkTimingEnable.SetCheck((tPara.iTimingEnable));//Enable timing 0 is off, is on
        m_edtRetryTime.SetWindowText(IntToCString(tPara.iRetryTimes)); // Number of retransmissions Range~3
        m_edtRetryInterval.SetWindowText(IntToCString(tPara.iRetryInterval)); // Retransmission interval in seconds, range ~5 seconds
        m_edtTimingInterval.SetWindowText(IntToCString(tPara.iTimingInterval)); //Timing interval in seconds, range -3600
        m_edtConfFileNo.SetWindowText(IntToCString(tPara.iConfFileNo)); // configuration file number range -100
        m_edtChannelCount.SetWindowText(IntToCString(tPara.iChannelCount)); // Number of channels Set the number of supported channels n n does not exceed 32

        for (int idx = 0; idx < tPara.iChannelCount; idx++)
        {
            char cPortTemp[LEN_64] = {0};
            sprintf(cPortTemp, "%d", tPara.iChannelNoArr[idx]);
            m_lstNpupList.InsertItem(idx, cPortTemp);
            m_lstNpupList.SetItemText(idx, n_LIST_ID, tPara.cChannelNumberArr[idx]);
        }
        AddLog(LOG_TYPE_SUCC, "", "[CLS_GATPage::UI_UpdateListInfo][NET_CLINET_HTTPPORT] get success!");

    }
}

void CLS_GATPage::OnLanguageChanged(int _iLanguage)
{
    UI_UpdateDialogText();
	
	SetDlgItemText(IDC_STC_GAT1400_IPADDRESS, GetTextByLan(_T("IP地址"), _T("IP Address")));
	SetDlgItemText(IDC_STC_GAT1400_PORT, GetTextByLan(_T("服务器端口"), _T("Server Port")));
	SetDlgItemText(IDC_STC_GAT_DEVICEID, GetTextByLan(_T("设备编号"), _T("Device ID")));
	SetDlgItemText(IDC_STC_GAT_USERNAME, GetTextByLan(_T("账户名"), _T("User Name")));
	SetDlgItemText(IDC_STC_GAT_PASSWORD, GetTextByLan(_T("密码"), _T("Password")));
	SetDlgItemText(IDC_STC_GAT_HEARTBEATINTERVAL, GetTextByLan(_T("心跳间隔"), _T("Heart Beat")));
	SetDlgItemText(IDC_STC_GAT_HEARTBEATTIME, GetTextByLan(_T("心跳次数"), _T("Heart Time")));
	SetDlgItemText(IDC_STC_GAT_PLACECODE, GetTextByLan(_T("行政代码"), _T("Place Code")));
	SetDlgItemText(IDC_STC_GAT_LONGITUDE, GetTextByLan(_T("经度"), _T("Longitude")));
	SetDlgItemText(IDC_STC_GAT_PLACECODE3, GetTextByLan(_T("纬度"), _T("latitude")));
	SetDlgItemText(IDC_STC_GAT_RETRYTIME, GetTextByLan(_T("重连次数"), _T("Retry Connect Time")));
	SetDlgItemText(IDC_STC_GAT_RETRYINTEVAL, GetTextByLan(_T("重连间隔"), _T("Retry Inteval")));
	SetDlgItemText(IDC_STC_GAT_TIMINGINTERVAL, GetTextByLan(_T("校时间隔"), _T("Timing Interval")));
	SetDlgItemText(IDC_STC_GAT_CONFFILENO, GetTextByLan(_T("配置文件编号"), _T("Config File No.")));
	SetDlgItemText(IDC_STC_GAT_CHANNELCOUNT, GetTextByLan(_T("通道个数"), _T("Channel Count ")));
	SetDlgItemText(IDC_STATIC_CONFIRMCHANNEL, GetTextByLan(_T("请确认通道个数与通道列表行数一致"), _T("Please Confirm Channel Count to be Equal with Channel List row Count ")));
	SetDlgItemText(IDC_GBO_GAT, GetTextByLan(_T("通道列表"), _T("Channel List")));
	SetDlgItemText(IDC_BUTTON_ONLINE, GetTextByLan(_T("获取在线状态"), _T("Get Online Status")));
	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_CHK_GAT_TIMINGENABLE, GetTextByLan(_T("启用校时"), _T("Timing Enable")));
	SetDlgItemText(IDC_CHK_GAT_ENABLE, GetTextByLan(_T("使能"), _T("Enable")));
	

}

void CLS_GATPage::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
{
    m_iLogonID = _iLogonID;

    if (_iChannelNo < 0)
    {
        m_iChannelNo = 0;
    }
    else
    {
        m_iChannelNo = _iChannelNo;
    }
    UI_UpdateListInfo();
}

void CLS_GATPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    if (_iLogonID < 0)
    {
        AddLog(LOG_TYPE_FAIL,"","Invalid logon id(%d)", _iLogonID);
        return;
    }

    switch (_iParaType)
    {
    case  PARA_GAT1400:
        {
            AddLog(LOG_TYPE_SUCC,"","[CLS_GATPage][OnParamChangeNotify] logon id(%d)", _iLogonID);
            UI_UpdateListInfo();
        }
        break;
    default:
        break;
    }
}
void CLS_GATPage::OnBnClickedButtonSet()
{
    if (m_iLogonID < 0)
    {
        AddLog(LOG_TYPE_MSG,"","[CLS_GATPage::OnBnClickedButtonSet]Invalid logon id(%d)", m_iLogonID);
        return;
    }

    Gat1400Para tPara = {0};
    tPara.iSize = sizeof(tPara);
    tPara.iChannelNo = m_iChannelNo;

    tPara.iEnable = m_chkEnalbe.GetCheck() ? 1 : 0; //Whether to enable 0 not enable, enable
    GetDlgItemText(IDC_EDT_GAT_IPADDRESS, tPara.cIpAddress, sizeof(tPara.cIpAddress));
    tPara.iPort = GetDlgItemInt(IDC_EDT_GAT_PORT);
    GetDlgItemText(IDC_EDT_GAT_DEVICEID, tPara.cDeviceID, sizeof(tPara.cDeviceID));
    GetDlgItemText(IDC_EDT_GAT_USERNAME, tPara.cUserName, sizeof(tPara.cUserName));
    GetDlgItemText(IDC_EDT_GAT_PASSWORD, tPara.cPassWord, sizeof(tPara.cPassWord));
    tPara.iHeartBeatInterval = GetDlgItemInt(IDC_EDT_GAT_HEARTBEATINTERVAL);
    tPara.iHeartBeatTimes = GetDlgItemInt(IDC_EDT_GAT_HEARTBEATTIME);
    GetDlgItemText(IDC_EDT_GAT_PLACECODE, tPara.cPlaceCode, sizeof(tPara.cPlaceCode));

    tPara.iLongitude = GetDlgItemInt(IDC_EDT_GAT_LONGITUDE);
    tPara.iLatitude = GetDlgItemInt(IDC_EDT_GAT_LATITUDE);
    tPara.iTimingEnable = m_chkTimingEnable.GetCheck() ? 1 : 0;

    tPara.iRetryTimes = GetDlgItemInt(IDC_EDT_GAT_RETRYTIME);
    tPara.iRetryInterval = GetDlgItemInt(IDC_EDT_GAT_RETRYINTEVAL);
    tPara.iTimingInterval = GetDlgItemInt(IDC_EDT_GAT_TIMINGINTERVAL);
    tPara.iConfFileNo = GetDlgItemInt(IDC_EDT_GAT_CONFFILENO);
    tPara.iChannelCount = GetDlgItemInt(IDC_EDT_GAT_CHANNELCOUNT);
 
    for (int idx = 0; idx < tPara.iChannelCount; idx++)
    {
        tPara.iChannelNoArr[idx] = StrToInt(m_lstNpupList.GetItemText(idx, 0));
        CString strItemData = m_lstNpupList.GetItemText(idx, 1);
        strcpy(tPara.cChannelNumberArr[idx], strItemData.GetBuffer());
    }

    int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GAT1400_PARA, m_iChannelNo, &tPara, sizeof(tPara));
    if (RET_SUCCESS > iRet)
    {
        AddLog(LOG_TYPE_FAIL, "", "[CLS_GATPage::OnBnClickedButtonSet][NET_CLIENT_GAT1400_PARA] Set fail,error = %d", GetLastError());
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "[CLS_GATPage::OnBnClickedButtonSet][NET_CLIENT_GAT1400_PARA] set success!");
    }
}

void CLS_GATPage::OnBnClickedButtonOnline()
{
    Gat1400StatusResult result = {0};
    result.iSize = sizeof(result);

    int iRet = NetClient_CmdConfig(m_iLogonID, CMD_GAT1400_STATUS, m_iChannelNo, NULL, 0, &result, result.iSize);
    if (RET_SUCCESS == iRet)
    {
        if (RET_GAT1400_STATUS_ONLINE == result.iResult)
        {
            AddLog(LOG_TYPE_SUCC, "", "[CLS_GATPage::OnBnClickedButtonOnline][CMD_GAT1400_STATUS] Online!");
        }
        else
        {
            AddLog(LOG_TYPE_SUCC, "", "[CLS_GATPage::OnBnClickedButtonOnline][CMD_GAT1400_STATUS] Offline!");
        }
    }
}

void CLS_GATPage::OnBnClickedButtonAddRow()
{
    int idx = m_lstNpupList.GetItemCount();

    char cPortTemp[LEN_64] = {0};
    sprintf(cPortTemp, "%d", 0);
    m_lstNpupList.InsertItem(idx + 1, cPortTemp);
    m_lstNpupList.SetItemText(idx + 1, n_LIST_ID, 0);
}

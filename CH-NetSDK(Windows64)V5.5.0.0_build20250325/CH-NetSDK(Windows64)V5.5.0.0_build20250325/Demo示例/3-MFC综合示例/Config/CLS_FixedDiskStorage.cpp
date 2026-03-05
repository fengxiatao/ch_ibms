//CLS_FixedDiskStorage.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_FixedDiskStorage.h"


// CLS_FixedDiskStorage dialog

IMPLEMENT_DYNAMIC(CLS_FixedDiskStorage, CDialog)

CLS_FixedDiskStorage::CLS_FixedDiskStorage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_FixedDiskStorage::IDD, pParent)
{

}

CLS_FixedDiskStorage::~CLS_FixedDiskStorage()
{
}

void CLS_FixedDiskStorage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_DATETIMEPICKER_RECORD_START_TIME, m_dtRecordStartTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER_RECORD_STOP_TIME, m_dtRecordStopTime);
	DDX_Control(pDX, IDC_EDIT_MS_IP, m_edtMsIp);
	DDX_Control(pDX, IDC_EDIT_MS_PORT, m_edtMsPort);
	DDX_Control(pDX, IDC_EDIT_MS_ID, m_edtMsId);
	DDX_Control(pDX, IDC_EDIT_MS_SUB_ID, m_edtMsSubId);
	DDX_Control(pDX, IDC_EDIT_CLIENT_MS_ID, m_edtClientMsId);
	DDX_Control(pDX, IDC_EDIT_CLIENT_MS_IP, m_edtClientMsIp);
	DDX_Control(pDX, IDC_EDIT_CLIENT_MS_PORT, m_edtClientMsPort);
	DDX_Control(pDX, IDC_EDIT_HOST_ID, m_edtHostId);
	DDX_Control(pDX, IDC_EDIT_RECORD_ALISA_NAME, m_edtRecordAlisaName);
	DDX_Control(pDX, IDC_COMBO_NVR_DEV_CHANNO, m_cboNvrDevChanNo);
	DDX_Control(pDX, IDC_COMBO_HTTP_SVR_CHANNO, m_cboSvrRecordChanNo);
	DDX_Control(pDX, IDC_EDIT_TASK_MISSIONID, m_edtTaskMissionID);
	DDX_Control(pDX, IDC_COMBO_RECORD_FILE_TYPE, m_cboRecordFileType);
	DDX_Control(pDX, IDC_COMBO_DISK_GROUP_NO, m_cboDiskCroupNo);
	DDX_Control(pDX, IDC_LIST_REC_SUPP_TASK_LIST, m_lstRecSuppTaskList);
}


BEGIN_MESSAGE_MAP(CLS_FixedDiskStorage, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_RECORD_SUPPLEMENT, &CLS_FixedDiskStorage::OnBnClickedButtonRecordSupplement)
	ON_BN_CLICKED(IDC_BUTTON_QUERY_SUPPLEMENT_PROGRESS, &CLS_FixedDiskStorage::OnBnClickedButtonQuerySupplementProgress)
	ON_BN_CLICKED(IDC_BUTTON_SET_RECORDCHN_ALIASNAME, &CLS_FixedDiskStorage::OnBnClickedButtonSetRecordchnAliasname)
	ON_BN_CLICKED(IDC_BUTTON_ACKCTRL, &CLS_FixedDiskStorage::OnBnClickedButtonAckctrl)
	ON_BN_CLICKED(IDC_BUTTON_REC_SUPP_CTRL_PAUSE, &CLS_FixedDiskStorage::OnBnClickedButtonRecSuppCtrlPause)
	ON_BN_CLICKED(IDC_BUTTON_REC_SUPP_CTRL_REINSTATE, &CLS_FixedDiskStorage::OnBnClickedButtonRecSuppCtrlReinstate)
	ON_BN_CLICKED(IDC_BUTTON_REC_SUPP_TASK_QUERY, &CLS_FixedDiskStorage::OnBnClickedButtonRecSuppTaskQuery)
END_MESSAGE_MAP()

BOOL CLS_FixedDiskStorage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtMsIp.SetLimitText(LEN_16);
	m_edtMsPort.SetLimitText(LEN_8);
	m_edtMsId.SetLimitText(LEN_256);
	m_edtMsSubId.SetLimitText(LEN_256);
	m_edtClientMsId.SetLimitText(LEN_256);
	m_edtClientMsIp.SetLimitText(LEN_16);
	m_edtClientMsPort.SetLimitText(LEN_8);
	m_edtHostId.SetLimitText(LEN_256);
	m_edtRecordAlisaName.SetLimitText(LEN_256);
	m_edtTaskMissionID.SetLimitText(LEN_64);

	m_edtMsIp.SetWindowText("192.168.16.110");
	SetDlgItemInt(IDC_EDIT_MS_PORT, 8002);
	m_edtMsId.SetWindowText("8d79d7e3115f459786399fb9cdc49dc5");
	m_edtMsSubId.SetWindowText("8d79d7e3115f459786399fb9cdc49dc5");
	m_edtClientMsId.SetWindowText("8d79d7e3115f459786399fb9cdc49dc5");
	m_edtClientMsIp.SetWindowText("192.168.16.110");
	SetDlgItemInt(IDC_EDIT_CLIENT_MS_PORT, 8002);
	m_edtHostId.SetWindowText("b467b32c85a04a51b969126651cb9908");
	m_edtRecordAlisaName.SetWindowText("NetClientDemoTestRecordAlisaName");
	m_edtTaskMissionID.SetWindowText("QWERTYUIOP-NetClientDemo20240710-ASDFGHJKLzxcvbnm");

	m_dtRecordStartTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	m_dtRecordStopTime.SetFormat("yyyy-MM-dd HH:mm:ss");
	CTime SystemTime; 
	m_dtRecordStartTime.GetTime(SystemTime);
	CTime BeginTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 0, 0, 0);
	m_dtRecordStartTime.SetTime(&BeginTime);
	CTime EndTime(SystemTime.GetYear(), SystemTime.GetMonth(), SystemTime.GetDay(), 23, 59, 0);
	m_dtRecordStopTime.SetTime(&EndTime);

	CString cstrChaNo;
	m_cboSvrRecordChanNo.ResetContent();
	for (int i = 0; i < MAX_TOTAL_CHANNEL_NUM; ++i)
	{
		cstrChaNo.Format("CH-%d", i);
		m_cboSvrRecordChanNo.AddString(cstrChaNo);
	}
	m_cboSvrRecordChanNo.SetCurSel(0);

	m_cboDiskCroupNo.ResetContent();
	for (int i = 0; i< MAX_DISK_GROUP_NUM; ++i)
	{
		CString strNo;
		strNo.Format("%d",i);
		m_cboDiskCroupNo.AddString(strNo);
	}
	m_cboDiskCroupNo.SetCurSel(0);

	m_lstRecSuppTaskList.SetExtendedStyle(m_lstRecSuppTaskList.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_FixedDiskStorage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow) {
		UpdateDevPara();
	}
}

void CLS_FixedDiskStorage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;

	if (_iChannelNo < 0) {
		m_iChannelNo = 0;
	} else {
		m_iChannelNo = _iChannelNo;
	}

	if (_iStreamNo < 0) {
		m_iStreamNo = 0;
	} else {
		m_iStreamNo = _iStreamNo;
	}

	UpdateDevPara();
}

void CLS_FixedDiskStorage::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();
	UpdateDevPara();
}

void CLS_FixedDiskStorage::UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_RECORD_START_TIME, GetTextByLan(_T("录像开始时间"), _T("RecordStartTime")));
	SetDlgItemText(IDC_STATIC_RECORD_STOP_TIME, GetTextByLan(_T("录像结束时间"), _T("RecordStopTime")));
	SetDlgItemText(IDC_STATIC_MS_IP, GetTextByLan(_T("中间件Ip"), _T("MsIp")));
	SetDlgItemText(IDC_STATIC_MS_PORT, GetTextByLan(_T("中间件端口"), _T("MsPort")));
	SetDlgItemText(IDC_STATIC_MS_ID, GetTextByLan(_T("登录中间件Id"), _T("MsId")));
	SetDlgItemText(IDC_STATIC_MS_SUB_ID, GetTextByLan(_T("主机中间件Id"), _T("MsSubId")));
	SetDlgItemText(IDC_STATIC_CLIENT_MS_ID, GetTextByLan(_T("登录端中间件Id"), _T("ClientMsId")));
	SetDlgItemText(IDC_STATIC_CLIENT_MS_IP, GetTextByLan(_T("登录端中间件Ip"), _T("ClientMsIp")));
	SetDlgItemText(IDC_STATIC_CLIENT_MS_PORT, GetTextByLan(_T("登录端中间件端口"), _T("ClientMsPort")));
	SetDlgItemText(IDC_STATIC_HOST_ID, GetTextByLan(_T("主机Id"), _T("HostId")));
	SetDlgItemText(IDC_BUTTON_RECORD_SUPPLEMENT, GetTextByLan(_T("录像补录"), _T("RecordSupplement")));
	SetDlgItemText(IDC_BUTTON_QUERY_SUPPLEMENT_PROGRESS, GetTextByLan(_T("查询进度"), _T("QueryProgress")));
	SetDlgItemText(IDC_BUTTON_SET_RECORDCHN_ALIASNAME, GetTextByLan(_T("配置录像通道别名"), _T("SetRecordChanAliasName")));
	SetDlgItemText(IDC_STATIC_RECORD_ALISA_NAME, GetTextByLan(_T("录像别名"), _T("RecordAliasName")));
	SetDlgItemText(IDC_STATIC_NVR_DEV_CHANNO, GetTextByLan(_T("NVR设备通道号"), _T("NvrDevChanNo")));
	SetDlgItemText(IDC_STATIC_HTTP_SVR_CHANNO, GetTextByLan(_T("服务器录像通道号"), _T("SvrRecordChanNo")));
	SetDlgItemText(IDC_STATIC_TASK_MISSIONID, GetTextByLan(_T("回补任务ID"), _T("MissionID")));
	SetDlgItemText(IDC_STATIC_RECORD_FILE_TYPE, GetTextByLan(_T("录像类型"), _T("RecordFileType")));
	SetDlgItemText(IDC_STATIC_DISK_GROUP_NO, GetTextByLan(_T("盘组编号"), _T("DiskGroupNo")));
	SetDlgItemText(IDC_BUTTON_ACKCTRL, GetTextByLan(_T("任务停止"), _T("TaskStop")));
	SetDlgItemText(IDC_BUTTON_REC_SUPP_CTRL_PAUSE, GetTextByLan(_T("任务暂停"), _T("TaskPause")));
	SetDlgItemText(IDC_BUTTON_REC_SUPP_CTRL_REINSTATE, GetTextByLan(_T("任务起复"), _T("TaskReinstate")));
	SetDlgItemText(IDC_BUTTON_REC_SUPP_TASK_QUERY, GetTextByLan(_T("任务查询"), _T("TaskQuery")));

	m_cboRecordFileType.ResetContent();
	int iIdx = 0;
	iIdx = m_cboRecordFileType.AddString(GetTextByLan(_T("所有文件"), _T("All recording")));
	m_cboRecordFileType.SetItemData(iIdx, 0);
	iIdx = m_cboRecordFileType.AddString(GetTextByLan(_T("定时录像"), _T("Timed recording")));
	m_cboRecordFileType.SetItemData(iIdx, 1);
	iIdx = m_cboRecordFileType.AddString(GetTextByLan(_T("报警录像"), _T("Alarm recording")));
	m_cboRecordFileType.SetItemData(iIdx, 3);
	iIdx = m_cboRecordFileType.AddString(GetTextByLan(_T("手动录像"), _T("Manual recording")));
	m_cboRecordFileType.SetItemData(iIdx, 7);
	m_cboRecordFileType.SetCurSel(0);

	int iColumn = 0;
	InsertColumn(m_lstRecSuppTaskList, iColumn++, GetTextByLan(_T("索引"), _T("Index")), LVCFMT_CENTER, 50);
	InsertColumn(m_lstRecSuppTaskList, iColumn++, GetTextByLan(_T("任务编号"), _T("TaskID")), LVCFMT_CENTER, 380);
	InsertColumn(m_lstRecSuppTaskList, iColumn++, GetTextByLan(_T("任务状态"), _T("TaskState")), LVCFMT_CENTER, 120);
}

void CLS_FixedDiskStorage::UpdateDevPara()
{
	if (m_iLogonID < 0) {
		return;
	}

	int iChannelNum = 0;
	NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	if (0 == iChannelNum) {
		iChannelNum = 1;
	}
	CString cstrChaNo;
	m_cboNvrDevChanNo.ResetContent();
	for (int i = 0; i < iChannelNum; ++i)
	{
		cstrChaNo.Format("CH-%d", i);
		m_cboNvrDevChanNo.AddString(cstrChaNo);
	}
	m_cboNvrDevChanNo.SetCurSel(0);

	int iRetBytes = 0;
	int iRet = RET_FAILED;
	GetAllRecordChnAliasName tPara = {0};
	tPara.iSize = sizeof(GetAllRecordChnAliasName);
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RECORDCHN_ALIASNAME, PARAM_CHANNEL_ALL, &tPara, sizeof(tPara), &iRetBytes);
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig->NET_CLIENT_RECORDCHN_ALIASNAME Success(%d)(%d,%s)(%d,%s)(%d,%s)(%d,%s)(%d,%s)"
			, tPara.iAllChnCount, tPara.tAllChnArr[0].iChanNo, tPara.tAllChnArr[0].cAliasName, tPara.tAllChnArr[1].iChanNo
			, tPara.tAllChnArr[1].cAliasName, tPara.tAllChnArr[2].iChanNo, tPara.tAllChnArr[2].cAliasName, tPara.tAllChnArr[3].iChanNo
			, tPara.tAllChnArr[3].cAliasName, tPara.tAllChnArr[4].iChanNo, tPara.tAllChnArr[4].cAliasName);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig->NET_CLIENT_RECORDCHN_ALIASNAME failed, iRet=%d", iRet);
	}
}

void CLS_FixedDiskStorage::OnMainNotify(int _ulLogonID,int _iWparam, void* _pvLParam, void* _pvUser)
{
	if (m_iLogonID < 0 || m_iLogonID != _ulLogonID) {
		return;
	}

	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_RECORD_SUPPLEMENT_PROGRESS:
		{
			AddLog(LOG_TYPE_MSG,"","CLS_FixedDiskStorage::OnMainNotify WCM_RECORD_SUPPLEMENT_PROGRESS");
			RecordSupplementProgress* ptProgress = (RecordSupplementProgress*)_pvLParam;
			if (NULL != ptProgress) {
				AddLog(LOG_TYPE_MSG, "", "WCM_RECORD_SUPPLEMENT_PROGRESS(%d,%s)", ptProgress->iProgress, ptProgress->cMissionID);
			}
		}
		break;
	default:
		break;
	}
}

void CLS_FixedDiskStorage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	switch(_iParaType)
	{
	case PARA_RECORD_CHN_ALISA_NAME:
		{
			AddLog(LOG_TYPE_MSG,"","CLS_FixedDiskStorage::OnParamChangeNotify PARA_RECORD_CHN_ALISA_NAME");
			break;
		}
	default:
		break;
	}
}

//static const char* s_pcMissionID = "QWERTYUIOP-NetClientDemo20240620-ASDFGHJKLzxcvbnm";
void CLS_FixedDiskStorage::OnBnClickedButtonRecordSupplement()
{
	int iRet = RET_FAILED;
	RecordSupplementPara tPara = {0};
	tPara.iSize = sizeof(RecordSupplementPara);

	int iNvrDevChanNo = m_cboNvrDevChanNo.GetCurSel();
	tPara.iHttpSvrChannelNo = m_cboSvrRecordChanNo.GetCurSel();

	CString cstrMissionID;
	m_edtTaskMissionID.GetWindowText(cstrMissionID);
	strcpy_s(tPara.cMissionID, sizeof(tPara.cMissionID), cstrMissionID.GetBuffer());

	CTime BeginTime;
	NVS_FILE_TIME tSdkTimeBegin = {0};
	m_dtRecordStartTime.GetTime(BeginTime);
	tSdkTimeBegin.iYear = BeginTime.GetYear();
	tSdkTimeBegin.iMonth = BeginTime.GetMonth();
	tSdkTimeBegin.iDay = BeginTime.GetDay();
	tSdkTimeBegin.iHour = BeginTime.GetHour();
	tSdkTimeBegin.iMinute = BeginTime.GetMinute();
	tSdkTimeBegin.iSecond = BeginTime.GetSecond();
	tPara.llStartTime = NvsFileTimeToAbsSeconds(&tSdkTimeBegin);

	CTime EndTime;
	NVS_FILE_TIME tSdkTimeEnd = {0};
	m_dtRecordStopTime.GetTime(EndTime);
	tSdkTimeEnd.iYear = EndTime.GetYear();
	tSdkTimeEnd.iMonth = EndTime.GetMonth();
	tSdkTimeEnd.iDay = EndTime.GetDay();
	tSdkTimeEnd.iHour = EndTime.GetHour();
	tSdkTimeEnd.iMinute = EndTime.GetMinute();
	tSdkTimeEnd.iSecond = EndTime.GetSecond();
	tPara.llStopTime = NvsFileTimeToAbsSeconds(&tSdkTimeEnd);

	int iIdx = m_cboRecordFileType.GetCurSel();
	tPara.iFileType = m_cboRecordFileType.GetItemData(iIdx);

	CString cstrMsIp;
	m_edtMsIp.GetWindowText(cstrMsIp);
	strcpy_s(tPara.cMsIp, sizeof(tPara.cMsIp), cstrMsIp.GetBuffer());

	tPara.iMsPort = GetDlgItemInt(IDC_EDIT_MS_PORT);

	strcpy_s(tPara.cCmdType, sizeof(tPara.cCmdType), "download");

	CString cstrMsId;
	m_edtMsId.GetWindowText(cstrMsId);
	strcpy_s(tPara.cMsId, sizeof(tPara.cMsId), cstrMsId.GetBuffer());

	CString cstrMsSubId;
	m_edtMsSubId.GetWindowText(cstrMsSubId);
	strcpy_s(tPara.cMsSupId, sizeof(tPara.cMsSupId), cstrMsSubId.GetBuffer());

	CString cstrClientMsId;
	m_edtClientMsId.GetWindowText(cstrClientMsId);
	strcpy_s(tPara.cClientMsId, sizeof(tPara.cClientMsId), cstrClientMsId.GetBuffer());

	CString cstrClientMsIp;
	m_edtClientMsIp.GetWindowText(cstrClientMsIp);
	strcpy_s(tPara.cClientMsIp, sizeof(tPara.cClientMsIp), cstrClientMsIp.GetBuffer());

	tPara.iClientMsPort = GetDlgItemInt(IDC_EDIT_CLIENT_MS_PORT);

	CString cstrHostId;
	m_edtHostId.GetWindowText(cstrHostId);
	strcpy_s(tPara.cHostId, sizeof(tPara.cHostId), cstrHostId.GetBuffer());

	CString cstrAlisaName;
	m_edtRecordAlisaName.GetWindowText(cstrAlisaName);
	strcpy_s(tPara.cAliasName, sizeof(tPara.cAliasName), cstrAlisaName.GetBuffer());

	tPara.iDiskGrpNo = m_cboDiskCroupNo.GetCurSel();

	RecordSupplementResult tResult = {0};
	tResult.iSize = sizeof(RecordSupplementResult);
	iRet = NetClient_CmdConfig(m_iLogonID, CMD_RECORD_SUPPLEMENT_CMDPARA, iNvrDevChanNo, &tPara, sizeof(tPara), &tResult, sizeof(tResult));
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_CmdConfig->CMD_RECORD_SUPPLEMENT_CMDPARA (%d, %d, %s)", tResult.iChannelNo, tResult.iRetCode, tResult.cMissionID);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_CmdConfig->CMD_RECORD_SUPPLEMENT_CMDPARA failed, iRet=%d", iRet);
	}
}

void CLS_FixedDiskStorage::OnBnClickedButtonQuerySupplementProgress()
{
	int iRet = RET_FAILED;
	RecordSupplementProgress tProgress = {0};
	tProgress.iSize = sizeof(RecordSupplementProgress);
	CString cstrMissionID;
	m_edtTaskMissionID.GetWindowText(cstrMissionID);
	strcpy_s(tProgress.cMissionID, sizeof(tProgress.cMissionID), cstrMissionID.GetBuffer());
	iRet = NetClient_CmdConfig(m_iLogonID, CMD_RECORD_SUPPLEMENT_PROGRESS, 0, &tProgress, sizeof(tProgress), &tProgress, sizeof(tProgress));
	if (RET_SUCCESS == iRet) {
		SetDlgItemInt(IDC_EDIT_SUPPLEMENT_PROGRESS, tProgress.iProgress);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_CmdConfig->CMD_RECORD_SUPPLEMENT_PROGRESS (%d, %s)", tProgress.iProgress, tProgress.cMissionID);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_CmdConfig->CMD_RECORD_SUPPLEMENT_PROGRESS failed, iRet=%d", iRet);
	}
}

void CLS_FixedDiskStorage::OnBnClickedButtonSetRecordchnAliasname()
{
	int iRet = RET_FAILED;
	RecordChnAliasNameArr tPara = {0};
	tPara.iSize = sizeof(RecordChnAliasNameArr);
	tPara.iChnCount = MAX_ONCE_RECORD_CHNCOUNT;
	char cChnAliasName[LEN_256] = {0};
	for (int i = 0; i < MAX_ONCE_RECORD_CHNCOUNT; ++i)
	{
		tPara.tChnArr[i].iSize = sizeof(RecordChnAliasName);
		tPara.tChnArr[i].iChanNo = i;
		memset(cChnAliasName, 0, LEN_256);
		sprintf(cChnAliasName, "DemoAliasNameChan%d", i);
		strcpy_s(tPara.tChnArr[i].cAliasName, sizeof(tPara.tChnArr[i].cAliasName), cChnAliasName);
	}
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_RECORDCHN_ALIASNAME, 0, &tPara, sizeof(tPara));
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig->NET_CLIENT_RECORDCHN_ALIASNAME Success");
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig->NET_CLIENT_RECORDCHN_ALIASNAME failed, iRet=%d", iRet);
	}
}

void CLS_FixedDiskStorage::ODevRecSuppCtrlOpt(int _iOpt)
{
	DevReplayRecordFile tInfo = {0};
	tInfo.iCtrl = _iOpt;

	CString strpcMissionID;
	GetDlgItemText(IDC_EDIT_TASK_MISSIONID, strpcMissionID);
	if(strpcMissionID.GetLength() < sizeof(tInfo.cMissionID)) {
		strncpy_s(tInfo.cMissionID, strpcMissionID.GetBuffer(),sizeof(tInfo.cMissionID));
	} else {
		MessageBox("Tip","OverLengthId > 64", MB_OK);
		return;
	}

	DevReplayRecordFile tResult = {0};
	int iRetValue = NetClient_CmdConfig(m_iLogonID, CMD_REPLENISH_REC_CTRL, m_iChannelNo, &tInfo, sizeof(tInfo), &tResult, sizeof(tResult));
	if(RET_SUCCESS == iRetValue) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_CmdConfig[CMD_REPLENISH_REC_CTRL] (%d, %d),ret = %d", m_iLogonID, m_iChannelNo, tResult.iRetCode);
		int iRetCode = tResult.iRetCode;
		switch (iRetCode)
		{
		case 0:
			MessageBox(GetTextByLan(_T("操作成功"), _T("Operate success")), "ReturnCode", MB_OK);
			break;
		case -10:
			MessageBox(GetTextByLan(_T("cMissionID与当前任务不匹配"), _T("cMissionID does not match the current task")), "ReturnCode", MB_OK);
			break;
		case -1000:
			MessageBox(GetTextByLan(_T("协议-cMissionID为空"), _T("Protocol-cMissionID is empty")), "ReturnCode", MB_OK);
			break;
		case -1001:
			MessageBox(GetTextByLan(_T("协议-iNvrChannelNo字段错误"), _T("Protocol-iNvrChannelNo Field Error")), "ReturnCode", MB_OK);
			break;
		case -1002:
			MessageBox(GetTextByLan(_T("其他任务在运行"), _T("Other tasks are running")), "ReturnCode", MB_OK);
			break;
		}
	} else {
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig[CMD_REPLENISH_REC_CTRL] (%d, %d), ret = %d", m_iLogonID, m_iChannelNo, tResult.iRetCode);
	}
}

void CLS_FixedDiskStorage::OnBnClickedButtonAckctrl()
{
	ODevRecSuppCtrlOpt(REC_SUPP_CTRL_STOP);
}

void CLS_FixedDiskStorage::OnBnClickedButtonRecSuppCtrlPause()
{
	ODevRecSuppCtrlOpt(REC_SUPP_CTRL_PAUSE);
}

void CLS_FixedDiskStorage::OnBnClickedButtonRecSuppCtrlReinstate()
{
	ODevRecSuppCtrlOpt(REC_SUPP_CTRL_REINSTATE);
}

CString GetStringByTaskState(int _iState)
{
	CString cstrTaskState = GetTextByLan(_T("未知"), _T("Unknown"));
	switch (_iState)
	{
	case 1:
		cstrTaskState = GetTextByLan(_T("停止中"), _T("Stopping"));
		break;
	case 2:
		cstrTaskState = GetTextByLan(_T("暂停中"), _T("Pausing"));
		break;
	case 3:
		cstrTaskState = GetTextByLan(_T("执行中"), _T("Executing"));
		break;
	default:
		break;
	}

	return  cstrTaskState;
}
void CLS_FixedDiskStorage::OnBnClickedButtonRecSuppTaskQuery()
{
	m_lstRecSuppTaskList.DeleteAllItems();
	RecSuppTaskList tTaskList = {0};
	tTaskList.iSize = sizeof(RecSuppTaskList);
	tTaskList.iSizeOfRecSuppTaskPara = sizeof(RecSuppTaskPara);
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_SUPP_REC_TASK_LIST, m_iChannelNo, NULL, 0, &tTaskList, sizeof(tTaskList));
	if (RET_SUCCESS != iRet) {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_CmdConfig[CMD_SUPP_REC_TASK_LIST] (%d, %d), ret=%d", m_iLogonID, m_iChannelNo, iRet);
		return;
	}

	for (int i = 0; i < tTaskList.iTaskCount && i < MAX_REC_SUPP_TASK_COUNT; ++i)
	{
		int iColumn = 0;
		m_lstRecSuppTaskList.InsertItem(i, "");
		m_lstRecSuppTaskList.SetItemText(i, iColumn++, IntToString(i + 1));
		m_lstRecSuppTaskList.SetItemText(i, iColumn++, tTaskList.tTaskArr[i].cMissionID);
		m_lstRecSuppTaskList.SetItemText(i, iColumn++, GetStringByTaskState(tTaskList.tTaskArr[i].iTaskState));
	}
}

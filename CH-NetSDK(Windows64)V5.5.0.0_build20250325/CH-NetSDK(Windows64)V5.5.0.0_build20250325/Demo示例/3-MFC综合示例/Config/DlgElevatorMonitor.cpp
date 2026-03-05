// CLS_DlgElevatorMonitor.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgElevatorMonitor.h"


// CLS_DlgElevatorMonitor dialog

IMPLEMENT_DYNAMIC(CLS_DlgElevatorMonitor, CDialog)

CLS_DlgElevatorMonitor::CLS_DlgElevatorMonitor(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgElevatorMonitor::IDD, pParent)
	, m_ptElevatorStoreyInfo(new ElevatorStoreyInfo)
	, m_iStartShorkThreshold(0)
	, m_iMoveSpeed(0)
	, m_iBodyInductionMode(0)
	, m_iEbikeDetectEnable(0)
	, m_iSwaySensitivity(0)
	, m_iTopLimit(0)
	, m_iBottomLimit(0)
	, m_iMainFloor(0)
	, m_iStartFloor(0)
	, m_iEndFloor(0)
	, m_iHeight(0)
	, m_iDefHeight(0)
	, m_iFloorTotalNum(0)
{

}

CLS_DlgElevatorMonitor::~CLS_DlgElevatorMonitor()
{
	if (NULL != m_ptElevatorStoreyInfo)
	{
		delete m_ptElevatorStoreyInfo;
		m_ptElevatorStoreyInfo = NULL;
	}
}

void CLS_DlgElevatorMonitor::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT1, m_iStartShorkThreshold);
	DDX_Text(pDX, IDC_EDIT3, m_iMoveSpeed);
	DDX_Text(pDX, IDC_EDIT4, m_iBodyInductionMode);
	DDX_Text(pDX, IDC_EDIT5, m_iEbikeDetectEnable);
	DDX_Text(pDX, IDC_EDIT6, m_iSwaySensitivity);
	DDX_Text(pDX, IDC_EDIT7, m_iTopLimit);
	DDX_Text(pDX, IDC_EDIT23, m_iBottomLimit);
	DDX_Text(pDX, IDC_EDIT46, m_iMainFloor);
	DDX_Control(pDX, IDC_COMBO_LEVELMODE, m_cboLevelMode);
	DDX_Control(pDX, IDC_COMBO_OPENDOORMODE, m_cboOpenDoorMode);
	DDX_Control(pDX, IDC_COMBO_MAINTENMODE, m_cboMaintenMode);
	DDX_Control(pDX, IDC_COMBO_CARSHSTOPMODE, m_cboCarshStopMode);
	DDX_Control(pDX, IDC_COMBO_PIRMODE, m_cboPIRNode);
	DDX_Text(pDX, IDC_EDIT21, m_iStartFloor);
	DDX_Text(pDX, IDC_EDIT25, m_iEndFloor);
	DDX_Text(pDX, IDC_EDIT29, m_iHeight);
	DDX_Control(pDX, IDC_LIST_FLOORHEIGHT, m_lstFloor);
	DDX_Text(pDX, IDC_EDIT_DEFAULT_HEIGHT, m_iDefHeight);
	DDX_Text(pDX, IDC_EDIT_STOREY_NUM, m_iFloorTotalNum);
}


BEGIN_MESSAGE_MAP(CLS_DlgElevatorMonitor, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET_MONITOR, &CLS_DlgElevatorMonitor::OnBnClickedButtonSetMonitor)
	ON_NOTIFY(NM_CLICK, IDC_LIST_FLOORHEIGHT, &CLS_DlgElevatorMonitor::OnNMClickListFloorheight)
	ON_EN_CHANGE(IDC_EDIT21, &CLS_DlgElevatorMonitor::OnEnChangeEdit21)
	ON_EN_CHANGE(IDC_EDIT25, &CLS_DlgElevatorMonitor::OnEnChangeEdit25)
	ON_EN_CHANGE(IDC_EDIT29, &CLS_DlgElevatorMonitor::OnEnChangeEdit29)
	ON_BN_CLICKED(IDC_BUTTON_STOREYINFO, &CLS_DlgElevatorMonitor::OnBnClickedButtonStoreyinfo)
	ON_EN_CHANGE(IDC_EDIT_FLOORINFO, &CLS_DlgElevatorMonitor::OnEnChangeEditFloorinfo)
END_MESSAGE_MAP()


// CLS_DlgElevatorMonitor message handlers

BOOL CLS_DlgElevatorMonitor::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialogText();
	m_lstFloor.SetExtendedStyle(m_lstFloor.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);

	m_lstFloor.InsertColumn(0, GetTextByLan(_T("楼层"), _T("Floor")), LVCFMT_CENTER, 100);
	m_lstFloor.InsertColumn(1, GetTextByLan(_T("高度"), _T("Height")), LVCFMT_CENTER, 80);
	m_lstFloor.InsertColumn(2, GetTextByLan(_T("楼层信息"), _T("Floor information")), LVCFMT_CENTER, 100);

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}



void CLS_DlgElevatorMonitor::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
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
	Update_Monitor();
	Update_StoreyInfo();
}

void CLS_DlgElevatorMonitor::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialogText();
}

void CLS_DlgElevatorMonitor::Update_Monitor()
{
	// TODO: Add your control notification handler code here
	ElevatorMonitor tParam = {0};
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ELEVATOR_MONITOR, m_iChannelNo, &tParam, sizeof(tParam), &iBytesReturned);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig NET_CLIENT_ELEVATOR_MONITOR failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[NET_CLIENT_ELEVATOR_MONITOR] (%d, %d)", m_iLogonID, m_iChannelNo);
	}

	m_iStartShorkThreshold = tParam.iStartShockThreshold;
	m_iMoveSpeed = tParam.iMoveSpeed;
	m_iBodyInductionMode = tParam.iBodyInductionMode;
	m_iEbikeDetectEnable = tParam.iEbikeDetectEnable;
	m_iSwaySensitivity = tParam.iSwaySensitivity;
	m_iTopLimit = tParam.iTopLimit;
	m_iBottomLimit = tParam.iBottomLimit;
	m_iMainFloor = tParam.iMainFloor;
	m_iDefHeight = tParam.iDefaultHeight;
	m_iFloorTotalNum = tParam.iStoreyNum;
	m_cboLevelMode.SetCurSel(tParam.iLevelingMode);
	m_cboOpenDoorMode.SetCurSel(tParam.iOpenDoorMode);
	m_cboMaintenMode.SetCurSel(tParam.iMaintenanceMode);
	m_cboCarshStopMode.SetCurSel(tParam.iCrashStopMode);
	m_cboPIRNode.SetCurSel(tParam.iPIRMode);
	UpdateData(FALSE);

}

void CLS_DlgElevatorMonitor::Update_StoreyInfo()
{
	// TODO: Add your control notification handler code here
	
	memset(m_ptElevatorStoreyInfo,0x00,sizeof(ElevatorStoreyInfo));
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ELEVATOR_STOREYINFO, m_iChannelNo, m_ptElevatorStoreyInfo, sizeof(ElevatorStoreyInfo), &iBytesReturned);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig NET_CLIENT_ELEVATOR_STOREYINFO failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[NET_CLIENT_ELEVATOR_STOREYINFO] (%d, %d)", m_iLogonID, m_iChannelNo);
	}

	m_iStartFloor = m_ptElevatorStoreyInfo->iStartFloor;
	m_iEndFloor = m_ptElevatorStoreyInfo->iEndFloor;
    
	AddDataToLst(*m_ptElevatorStoreyInfo);
	UpdateData(FALSE);

}

void CLS_DlgElevatorMonitor::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_THRESHOLD,GetTextByLan(_T("启动冲击阈值"), _T("StartShockThreshold")));
	SetDlgItemText(IDC_STATIC_MOVESPEED,GetTextByLan(_T("正常运行速度"), _T("MoveSpeed")));
	SetDlgItemText(IDC_STATIC_BODYMODE,GetTextByLan(_T("人员检测方式"), _T("BodyInductionMode")));
	SetDlgItemText(IDC_STATIC_EBIKE_ENABLE,GetTextByLan(_T("电动车入梯报警"), _T("EbikeDetectEnable")));
	SetDlgItemText(IDC_STATIC_SWAY,GetTextByLan(_T("晃动报警灵敏度"), _T("SwaySensitivity")));
	SetDlgItemText(IDC_STATIC_TOPLIMIT,GetTextByLan(_T("最高楼层"), _T("TopLimit")));
	SetDlgItemText(IDC_STATIC_BOTTOMLIMIT,GetTextByLan(_T("最低楼层"), _T("BottomLimit")));
	SetDlgItemText(IDC_STATIC_MAINFLOOR,GetTextByLan(_T("基站层"), _T("iMainFloor")));
	SetDlgItemText(IDC_STATIC_LEVELMODE,GetTextByLan(_T("平层信号有效类型"), _T("LevelingMode")));
	SetDlgItemText(IDC_STATIC_OPENDOORMODE,GetTextByLan(_T("开关门信号有效类型"), _T("OpenDoorMode")));
	SetDlgItemText(IDC_STATIC_MAINTENMODE,GetTextByLan(_T("检修有效类型"), _T("MaintenanceMode")));
	SetDlgItemText(IDC_STATIC_CARSHSTOP,GetTextByLan(_T("急停有效类型"), _T("CarshStopMode")));
	SetDlgItemText(IDC_STATIC_PIRMODE,GetTextByLan(_T("PIR信号有效类型"), _T("PIRMode")));
	SetDlgItemText(IDC_STATIC_STARTFLOOR,GetTextByLan(_T("开始楼层"), _T("Start Floor")));
	SetDlgItemText(IDC_STATIC_ENDFLOOR,GetTextByLan(_T("结束楼层"), _T("End Floor")));
	SetDlgItemText(IDC_STATIC_HEIGHT,GetTextByLan(_T("楼层高度"), _T("Floor Height")));
	SetDlgItemText(IDC_BUTTON_SET_MONITOR,GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_STOREYINFO,GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_FLOORINFO,GetTextByLan(_T("楼层信息"), _T("Floor information")));
	SetDlgItemText(IDC_STATIC_DEFAULT_HEIGHT,GetTextByLan(_T("默认层高(mm)"), _T("Default height(mm)")));
	SetDlgItemText(IDC_STATIC_STOREY_NUM,GetTextByLan(_T("楼层总数"), _T("Storey number")));

	InsertString(m_cboLevelMode,0,GetTextByLan(_T("0-低有效"), _T("0-Low Effective")));
	InsertString(m_cboLevelMode,1,GetTextByLan(_T("1-高有效"), _T("1-High Effective")));
	InsertString(m_cboLevelMode,2,GetTextByLan(_T("2-上边沿（预留）"), _T("2-UpSide")));
	InsertString(m_cboLevelMode,3,GetTextByLan(_T("3-下边沿（预留）"), _T("3-DownSide")));

	InsertString(m_cboOpenDoorMode,0,GetTextByLan(_T("0-低有效"), _T("0-Low Effective")));
	InsertString(m_cboOpenDoorMode,1,GetTextByLan(_T("1-高有效"), _T("1-High Effective")));
	InsertString(m_cboOpenDoorMode,2,GetTextByLan(_T("2-上边沿（预留）"), _T("2-UpSide")));
	InsertString(m_cboOpenDoorMode,3,GetTextByLan(_T("3-下边沿（预留）"), _T("3-DownSide")));

	InsertString(m_cboMaintenMode,0,GetTextByLan(_T("0-低有效"), _T("0-Low Effective")));
	InsertString(m_cboMaintenMode,1,GetTextByLan(_T("1-高有效"), _T("1-High Effective")));
	InsertString(m_cboMaintenMode,2,GetTextByLan(_T("2-上边沿（预留）"), _T("2-UpSide")));
	InsertString(m_cboMaintenMode,3,GetTextByLan(_T("3-下边沿（预留）"), _T("3-DownSide")));

	InsertString(m_cboCarshStopMode,0,GetTextByLan(_T("0-低有效"), _T("0-Low Effective")));
	InsertString(m_cboCarshStopMode,1,GetTextByLan(_T("1-高有效"), _T("1-High Effective")));
	InsertString(m_cboCarshStopMode,2,GetTextByLan(_T("2-上边沿（预留）"), _T("2-UpSide")));
	InsertString(m_cboCarshStopMode,3,GetTextByLan(_T("3-下边沿（预留）"), _T("3-DownSide")));

	InsertString(m_cboPIRNode,0,GetTextByLan(_T("0-低有效"), _T("0-Low Effective")));
	InsertString(m_cboPIRNode,1,GetTextByLan(_T("1-高有效"), _T("1-High Effective")));
	InsertString(m_cboPIRNode,2,GetTextByLan(_T("2-上边沿（预留）"), _T("2-UpSide")));
	InsertString(m_cboPIRNode,3,GetTextByLan(_T("3-下边沿（预留）"), _T("3-DownSide")));

}
void CLS_DlgElevatorMonitor::OnBnClickedButtonSetMonitor()
{
	// TODO: Add your control notification handler code here

	UpdateData(TRUE);
	ElevatorMonitor tParam = {0};

	tParam.iStartShockThreshold = m_iStartShorkThreshold;
	tParam.iMoveSpeed = m_iMoveSpeed;
	tParam.iBodyInductionMode = m_iBodyInductionMode;
	tParam.iEbikeDetectEnable = m_iEbikeDetectEnable;
	tParam.iSwaySensitivity = m_iSwaySensitivity;
	tParam.iTopLimit = m_iTopLimit;
	tParam.iBottomLimit = m_iBottomLimit;
	tParam.iMainFloor = m_iMainFloor;
	tParam.iDefaultHeight = m_iDefHeight;
	tParam.iStoreyNum = m_iFloorTotalNum;
	tParam.iLevelingMode = m_cboLevelMode.GetCurSel();
	tParam.iOpenDoorMode = m_cboOpenDoorMode.GetCurSel();
	tParam.iMaintenanceMode = m_cboMaintenMode.GetCurSel();
	tParam.iCrashStopMode = m_cboCarshStopMode.GetCurSel();
	tParam.iPIRMode = m_cboPIRNode.GetCurSel();

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_ELEVATOR_MONITOR, m_iChannelNo, &tParam, sizeof(tParam));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig NET_CLIENT_ELEVATOR_MONITOR failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_ELEVATOR_MONITOR] (%d, %d)", m_iLogonID, m_iChannelNo);
	}

}

void CLS_DlgElevatorMonitor::AddDataToLst(ElevatorStoreyInfo &tElevatorStoreyInfo)
{
	m_lstFloor.DeleteAllItems();
	for (int i = 0; i <  LEN_256; i++)
	{
		m_lstFloor.InsertItem(i, (LPCTSTR)IntToStr(i + MIN_LOOR_LEVING).c_str());
		m_lstFloor.SetItemText(i, 1, (LPCTSTR)IntToStr(tElevatorStoreyInfo.iFloorHeight[i]).c_str());
		m_lstFloor.SetItemText(i, 2, tElevatorStoreyInfo.cStoreyDisplayList[i]);
	}
}

void CLS_DlgElevatorMonitor::OnNMClickListFloorheight(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	// TODO: Add your control notification handler code here
	int iItem = pNMItemActivate->iItem;
	if (iItem >= 0)
	{
		m_iHeight = m_ptElevatorStoreyInfo->iFloorHeight[iItem];
		UpdateData(FALSE);
		m_iCurFloor = iItem;
	}
	*pResult = 0;
}

void CLS_DlgElevatorMonitor::OnEnChangeEdit29()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CLS_BasePage::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
	UpdateData(TRUE);
	if (0 <= m_iCurFloor)
	{
		m_ptElevatorStoreyInfo->iFloorHeight[m_iCurFloor] = m_iHeight;
		m_lstFloor.SetItemText(m_iCurFloor, 1, (LPCTSTR)IntToStr(m_iHeight).c_str());
	}
}

void CLS_DlgElevatorMonitor::OnBnClickedButtonStoreyinfo()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);

	m_ptElevatorStoreyInfo->iStartFloor = m_iStartFloor;
	m_ptElevatorStoreyInfo->iEndFloor = m_iEndFloor;

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_ELEVATOR_STOREYINFO, m_iChannelNo, m_ptElevatorStoreyInfo, sizeof(ElevatorStoreyInfo));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig NET_CLIENT_ELEVATOR_STOREYINFO failed! Logon id(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_ELEVATOR_STOREYINFO] (%d, %d)", m_iLogonID, m_iChannelNo);
	}
}

void CLS_DlgElevatorMonitor::OnEnChangeEditFloorinfo()
{
	UpdateData(TRUE);
	if (0 > m_iCurFloor)
	{
		return;
	}
	CString strInfo;
	((CEdit*)GetDlgItem(IDC_EDIT_FLOORINFO))->GetWindowText(strInfo);
	memset(m_ptElevatorStoreyInfo->cStoreyDisplayList[m_iCurFloor], 0x0, sizeof(m_ptElevatorStoreyInfo->cStoreyDisplayList[m_iCurFloor]));
	memcpy(m_ptElevatorStoreyInfo->cStoreyDisplayList[m_iCurFloor], strInfo, min(strInfo.GetLength(), sizeof(m_ptElevatorStoreyInfo->cStoreyDisplayList[m_iCurFloor])));
	m_lstFloor.SetItemText(m_iCurFloor, 2, m_ptElevatorStoreyInfo->cStoreyDisplayList[m_iCurFloor]);
}

void CLS_DlgElevatorMonitor::OnEnChangeEdit21()
{
	UpdateData(TRUE);
}

void CLS_DlgElevatorMonitor::OnEnChangeEdit25()
{
	UpdateData(TRUE);
}

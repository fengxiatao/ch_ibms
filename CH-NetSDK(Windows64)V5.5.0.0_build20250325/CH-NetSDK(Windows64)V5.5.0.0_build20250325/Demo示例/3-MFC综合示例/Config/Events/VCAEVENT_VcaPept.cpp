// E:\SDK_ALL\trunk\Demo\NetClientDemo\Config\Events\VCAEVENT_VcaPept.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_VcaPept.h"

#define	REGION_MAX_POINTS_NUM  8
// CLS_VcaPept dialog

IMPLEMENT_DYNAMIC(CLS_VcaPept, CDialog)

CLS_VcaPept::CLS_VcaPept(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VcaPept::IDD, pParent)
{
	memset(&m_tPoints, 0, sizeof(m_tPoints));

}

CLS_VcaPept::~CLS_VcaPept()
{
}

void CLS_VcaPept::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_PEPT_EVENT_ENABLE, m_cboEventEnable);
	DDX_Control(pDX, IDC_CHECK_PEPT_SHOWRULE, m_ckbDisplayRule);
	DDX_Control(pDX, IDC_CHECK_PEPT_SHOWNUM, m_ckbDisplayAlarmCount);
	DDX_Control(pDX, IDC_CHECK_PEPT_SHOWTARGET, m_chkShowTargetBox);
	DDX_Control(pDX, IDC_COMBO_PEPT_COLOR, m_cboRegionColor);
	DDX_Control(pDX, IDC_COMBO_PEPT_ALARMCOLOR, m_cboAlarmColor);
	DDX_Control(pDX, IDC_SLIDER_PEPT_SENSITIVE, m_sldSensitive);
	DDX_Control(pDX, IDC_SLIDER_PEPT_ALARM_TIME, m_sldAlarmTime);
	DDX_Control(pDX, IDC_BTN_PEPT_REGION_DRAW, m_btnDraw);
	DDX_Control(pDX, IDC_CHECK_SWITCH_SENCE_SNAP_ENABLE, m_ckbSnapEnable);
	DDX_Control(pDX, IDC_EDIT_PEPT_REGION_POINTS, m_editRegionPoins);
	DDX_Control(pDX, IDC_CHECK_Pedestrian, m_chkTargetType[0]);
	DDX_Control(pDX, IDC_CHECK_CAR, m_chkTargetType[1]);
	DDX_Control(pDX, IDC_CHECK_SUV, m_chkTargetType[2]);
	DDX_Control(pDX, IDC_CHECK_PICKUP, m_chkTargetType[3]);
	DDX_Control(pDX, IDC_CHECK_TRNK, m_chkTargetType[4]);
	DDX_Control(pDX, IDC_CHECK_VAN_VAN, m_chkTargetType[5]);
	DDX_Control(pDX, IDC_CHECK_BIG_TRUNK, m_chkTargetType[6]);
	DDX_Control(pDX, IDC_CHECK_FORKLIFT, m_chkTargetType[7]);
	DDX_Control(pDX, IDC_CHECK_Excavator, m_chkTargetType[8]);
	DDX_Control(pDX, IDC_CHECK_Engineering, m_chkTargetType[9]);
	DDX_Control(pDX, IDC_CHECKTWO_WHEELER, m_chkTargetType[10]);
	DDX_Control(pDX, IDC_CHECK_TRICYCLE, m_chkTargetType[11]);
	DDX_Control(pDX, IDC_CHECK_BUS, m_chkTargetType[12]);
	DDX_Control(pDX, IDC_CHECK_VAN, m_chkTargetType[13]);
	DDX_Control(pDX, IDC_CHECK_OTHER, m_chkTargetType[31]);
	DDX_Control(pDX, IDC_CHECK_ERROR_STATE, m_ckbUnvisualState);
	DDX_Control(pDX, IDC_CHECK2, m_ckbZone);
	DDX_Control(pDX, IDC_CHECK_PEPT, m_ckbPept);
	DDX_Control(pDX, ID_COMBOX_PEPT_DEV_TYPE, m_cboDevType);
	DDX_Control(pDX, IDC_CHECK_PEPT2, m_ckbPet2);
	DDX_Control(pDX, IDC_CHECK_PEPT3, m_ckbPept3);
}


BEGIN_MESSAGE_MAP(CLS_VcaPept, CDialog)
	ON_BN_CLICKED(IDC_BTN_PEPT_REGION_DRAW, &CLS_VcaPept::OnBnClickedBtnPeptRegionDraw)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_PEPT_SENSITIVE, &CLS_VcaPept::OnNMCustomdrawSliderPeptSensitive)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_PEPT_ALARM_TIME, &CLS_VcaPept::OnNMCustomdrawSliderPeptAlarmTime)
	ON_BN_CLICKED(IDC_BTN_PEPT_SET, &CLS_VcaPept::OnBnClickedBtnPeptSet)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_CHECK_SWITCH_SENCE_SNAP_ENABLE, &CLS_VcaPept::OnBnClickedCheckSwitchSenceSnapEnable)
END_MESSAGE_MAP()


// CLS_VcaPept message handler

BOOL CLS_VcaPept::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	
}

void CLS_VcaPept::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_PEPT_EVENT_ENABLE, GetTextByLan("事件使能", "Event enable"));
	SetDlgItemText(IDC_CHECK_PEPT_SHOWRULE, GetTextByLan("显示规则", "According to the rules"));
	SetDlgItemText(IDC_CHECK_PEPT_SHOWNUM, GetTextByLan("显示报警计数", "Alarm count"));
	SetDlgItemText(IDC_CHECK_PEPT_SHOWTARGET, GetTextByLan("显示目标框", "Display target box"));
	SetDlgItemText(IDC_STC_PEPT_COLOR, GetTextByLan("区域颜色", "Regional color"));
	SetDlgItemText(IDC_STC_PEPT_ALARMCOLOR, GetTextByLan("报警区域颜色", "Color of alarm area"));
	SetDlgItemText(IDC_STC_PEPT_SENSITIVE, GetTextByLan("灵敏度", "The sensitivity"));
	SetDlgItemText(IDC_STC_PEPT_MIN_SIZE, GetTextByLan("最小宽度", "Minimum width"));
	SetDlgItemText(IDC_STC_PEPT_MAX_SIZE, GetTextByLan("最大宽度", "Maximum width"));
	SetDlgItemText(IDC_STC_PEPT_REGION_POINTNUM, GetTextByLan("检测区域点个数", "Detection area number"));
	SetDlgItemText(IDC_STC_PEPT_REGION_POINTS, GetTextByLan("点集", "Point set"));
	SetDlgItemText(IDC_BTN_PEPT_REGION_DRAW, GetTextByLan("绘制", "Draw"));
	SetDlgItemText(IDC_BTN_PEPT_SET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_STC_TARGET_TYPE, GetTextByLan("目标类型", "TargetType"));
	SetDlgItemText(IDC_STC_PEPT_ALARM_TYPE, GetTextByLan("警情类型", "AlarmType"));
	SetDlgItemText(IDC_CHECK_SWITCH_SENCE_SNAP_ENABLE, GetTextByLan("抓拍使能", "SnapEnable"));
	SetDlgItemText(IDC_STC_ALARM_TIME, GetTextByLan("驻留报警时间", "ResidentAlarmTime"));
	SetDlgItemText(IDC_CHECK_ERROR_STATE, GetTextByLan("异常驻留", "UnusualResidency"));
	SetDlgItemText(IDC_CHECK_Pedestrian, GetTextByLan("行人", "Pedestrian"));
	SetDlgItemText(IDC_CHECK_CAR, GetTextByLan("轿车 ", "car"));
	SetDlgItemText(IDC_CHECK_SUV, GetTextByLan("SUV", "SUV"));
	SetDlgItemText(IDC_CHECK_PICKUP, GetTextByLan("皮卡车 ", "pickup"));
	SetDlgItemText(IDC_CHECK_TRNK, GetTextByLan("罐车 ", "tank"));
	SetDlgItemText(IDC_CHECK_VAN_VAN, GetTextByLan("小货车", "Vanvan"));
	SetDlgItemText(IDC_CHECK_BIG_TRUNK, GetTextByLan("大货车", "Bigtruck"));
	SetDlgItemText(IDC_CHECK_FORKLIFT, GetTextByLan("铲车", "Forklift"));
	SetDlgItemText(IDC_CHECK_Excavator, GetTextByLan("挖掘机", "Excavator"));
	SetDlgItemText(IDC_CHECK_Engineering, GetTextByLan("工程车", "Engineering"));
	SetDlgItemText(IDC_CHECKTWO_WHEELER, GetTextByLan("二轮车", "Twowheeler"));
	SetDlgItemText(IDC_CHECK_TRICYCLE, GetTextByLan("三轮车", "Tricycle"));
	SetDlgItemText(IDC_CHECK_BUS, GetTextByLan("大货车", "Bus"));
	SetDlgItemText(IDC_CHECK_VAN, GetTextByLan("面包车", "Van"));
	SetDlgItemText(IDC_CHECK_OTHER, GetTextByLan("其他", "Other"));

	const CString strColor[] = {GetTextEx(IDS_VCA_COL_RED), GetTextEx(IDS_VCA_COL_GREEN), GetTextEx(IDS_VCA_COL_YELLOW), 
		GetTextEx(IDS_VCA_COL_BLUE), GetTextEx(IDS_VCA_COL_MAGENTA), GetTextEx(IDS_VCA_COL_CYAN), GetTextEx(IDS_VCA_COL_BLACK), GetTextEx(IDS_VCA_COL_WHITE)};
	m_cboRegionColor.ResetContent();
	m_cboAlarmColor.ResetContent();
	for (int i=0; i<sizeof(strColor)/sizeof(CString); i++)
	{
		m_cboRegionColor.InsertString(i, strColor[i]);
		m_cboAlarmColor.InsertString(i, strColor[i]);
	}
	m_sldSensitive.SetRange(0,100);
	m_sldAlarmTime.SetRange(0,60);
	const CString csDevType[] = {"IPC","NVR"};
	m_cboDevType.ResetContent();
	for (int i = 0; i < sizeof(csDevType)/sizeof(CString); i++)
	{
		m_cboDevType.InsertString(i,csDevType[i]);
	}
	m_cboDevType.SetCurSel(0);
}


void CLS_VcaPept::OnBnClickedBtnPeptRegionDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}
	/* The following code can take out the corresponding parameters from the draw dialog box */
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, REGION_MAX_POINTS_NUM);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection);
	if (-1 == iSetRet)
	{
		return;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		m_editRegionPoins.SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDIT_PEPT_REGION_POINTNUM, iPointNum);
		vca_TPoint ptPolygon[MAX_VCA_PEPT_POINT_NUM] = {0} ;
		CString cstPolygon = "";
		m_editRegionPoins.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)ptPolygon);
		for (int i = 0; i < iPointNum && i<MAX_VCA_PEPT_POINT_NUM ; i++)
		{
			m_tPoints[i].iX = ptPolygon[i].iX;
			m_tPoints[i].iY = ptPolygon[i].iY;
		}
	}
	else
	{
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VcaPept::UpdateDrawFinishRegionNum()
{
	
}


void CLS_VcaPept::OnNMCustomdrawSliderPeptSensitive(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_PROCURATORATEDUTY_SENSITIVE_NUM, m_sldSensitive.GetPos());
	*pResult = 0;
}

void CLS_VcaPept::OnNMCustomdrawSliderPeptAlarmTime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_PROCURATORATEDUTY_SLEEPTIME_NUM, m_sldAlarmTime.GetPos());
	*pResult = 0;
}

void CLS_VcaPept::OnBnClickedBtnPeptSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VcaPept::OnBnClickedBtnPeptSet Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	
	VCAPEPT tVCaPept = {0};
	tVCaPept.iTargetType |= m_chkTargetType[31].GetCheck() << 31;
	tVCaPept.tRule.iRuleID = m_iRuleID;
	tVCaPept.tRule.iSceneID = m_iSceneID;
	tVCaPept.tRule.iValid = m_cboEventEnable.GetCheck();
	tVCaPept.tDisplayParam.iAlarmColor = m_cboAlarmColor.GetCurSel();
	tVCaPept.tDisplayParam.iColor = m_cboRegionColor.GetCurSel();  
	tVCaPept.tDisplayParam.iDisplayRule = m_ckbDisplayRule.GetCheck();
	tVCaPept.tDisplayParam.iDisplayStat = m_ckbDisplayAlarmCount.GetCheck();
	tVCaPept.iDisplayTarget = m_chkShowTargetBox.GetCheck();
	tVCaPept.iMaxSize = GetDlgItemInt(IDC_EDIT_PEPT_MAX_SIZE);
	tVCaPept.iMinSize = GetDlgItemInt(IDC_EDIT_PEPT_MIN_SIZE);
	tVCaPept.iSensitivity = m_sldSensitive.GetPos();
	
	int temp = 0;
	temp = m_ckbZone.GetCheck();
	tVCaPept.iModelType |= temp;
	temp = m_ckbUnvisualState.GetCheck();
	temp = temp << 1;
	tVCaPept.iModelType |= temp;

	//tVCaPept.iModelType = 3;
	for (int i = 0; i < 14; i++)
	{
		int a = m_chkTargetType[i].GetCheck();
		a = a  << i;
		tVCaPept.iTargetType |= a;
	}

	tVCaPept.iTimeThreshold = m_sldAlarmTime.GetPos();
	tVCaPept.iPointNum = GetDlgItemInt(IDC_EDIT_PEPT_REGION_POINTNUM);
	if(tVCaPept.iPointNum <= 0)
	{
		AddLog(LOG_TYPE_FAIL,"","xxxxxxiPointNum = %d", tVCaPept.iPointNum);
		return;
	}

	for (int i = 0; i < tVCaPept.iPointNum && i<MAX_VCA_PEPT_POINT_NUM ; i++)
	{
		if(m_tPoints[i].iX < 0 || m_tPoints[i].iY < 0)
		{
			AddLog(LOG_TYPE_FAIL,"","xxxxxxm_tPoints[i].iX = %d, m_tPoints[i].iY = %d", m_tPoints[i].iX, m_tPoints[i].iY);
			return;
		}
		 tVCaPept.stPoints[i].iX = m_tPoints[i].iX;
		 tVCaPept.stPoints[i].iY = m_tPoints[i].iY;
	}
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_VCA_PEPT, m_iChannelNO, &tVCaPept, sizeof(VCAPEPT));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_VCA_PEPT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedBtnPeptSet::NetClient_VCASetConfig[VCA_CMD_VCA_PEPT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VcaPept::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VCAPEPT tInfo = {0};
	tInfo.tRule.iSceneID = m_iSceneID;
	tInfo.tRule.iRuleID = m_iRuleID;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_VCA_PEPT, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet >= 0)
	{
		m_cboEventEnable.SetCheck(tInfo.tRule.iValid);
		m_cboAlarmColor.SetCurSel(tInfo.tDisplayParam.iAlarmColor);
		m_cboRegionColor.SetCurSel(tInfo.tDisplayParam.iColor);
		m_ckbDisplayRule.SetCheck(tInfo.tDisplayParam.iDisplayRule);
		m_ckbDisplayAlarmCount.SetCheck(tInfo.tDisplayParam.iDisplayStat);
		m_chkShowTargetBox.SetCheck(tInfo.iDisplayTarget);
		SetDlgItemInt(IDC_EDIT_PEPT_MAX_SIZE, tInfo.iMaxSize);
		SetDlgItemInt(IDC_EDIT_PEPT_MIN_SIZE, tInfo.iMinSize);
		m_sldSensitive.SetPos(tInfo.iSensitivity);
		SetDlgItemInt(IDC_EDIT_PEPT_REGION_POINTNUM, tInfo.iPointNum);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointNum && i<MAX_VCA_PEPT_POINT_NUM; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.stPoints[i].iX, tInfo.stPoints[i].iY);
			m_tPoints[i].iX = tInfo.stPoints[i].iX;
			m_tPoints[i].iY = tInfo.stPoints[i].iY;
		}
		SetDlgItemText(IDC_EDIT_PEPT_REGION_POINTS, cstPolygonBuf);

		m_sldAlarmTime.SetPos(tInfo.iTimeThreshold);
		for (int i = 0; i < 14; i++)
		{
			int iChk = (0 == (tInfo.iTargetType & 1 << i)) ? BST_UNCHECKED : BST_CHECKED;
			m_chkTargetType[i].SetCheck(iChk);
		}
		int iChk = (0 == (tInfo.iTargetType & 1 << 31)) ? BST_UNCHECKED : BST_CHECKED;
		m_chkTargetType[31].SetCheck(iChk);

		
		iChk = (0 == (tInfo.iModelType & 1 << 0)) ? BST_UNCHECKED : BST_CHECKED;
		m_ckbZone.SetCheck(iChk);
		iChk = (0 == (tInfo.iModelType & 1 << 1)) ? BST_UNCHECKED : BST_CHECKED;
		m_ckbUnvisualState.SetCheck(iChk);

	}
	else
	{

	}
	VCASenceSnap tVCASenceSnap = {0};
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SENCE_SNAP, m_iChannelNO, &tVCASenceSnap, sizeof(tVCASenceSnap));
	if (iRet >= 0)
	{
		m_ckbSnapEnable.SetCheck(tVCASenceSnap.iSnapEnable);
	}
	else
	{

	}


	int iByteReturn = -1;
	FuncAbilityLevel stSwitchSnapAbility = {0};
	stSwitchSnapAbility.iSize = sizeof(stSwitchSnapAbility);
	stSwitchSnapAbility.iMainFuncType = 0x9;
	stSwitchSnapAbility.iSubFuncType = 56;

	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stSwitchSnapAbility, sizeof(stSwitchSnapAbility), &iByteReturn);
	if (iRet < 0 || strlen(stSwitchSnapAbility.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "Get ablitity 0x9 Failed! m_iLogonID %d iSubFuncType = 56", m_iLogonID);
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_SWITCH_SENCE, _ttoi(stSwitchSnapAbility.cParam) );
	}


	VcaArithmeticList tParam = {0};
	tParam.iSize = sizeof(tParam);
	tParam.iChannelNo = m_iChannelNO;
	//
	tParam.iArithmeticType = VCA_ARITHMETIC_PEPT;	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	if (iRet == -2 && tParam.iEnableCount > 0)
	{
		tParam.piEnableValue = new int[tParam.iEnableCount];
		iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_GET_VCALIST, m_iChannelNO, &tParam, sizeof(tParam));
	}
	if (0 == iRet)
	{
		int a = tParam.piEnableValue[0];
			a = a&(1 << 0);
		m_ckbPept.EnableWindow(a>0?1:0);
		m_ckbPept.SetCheck(a>0?1:0);
		a = tParam.piEnableValue[0];
		a = a & (1 << 1);
		m_ckbPet2.EnableWindow(a>0?1:0);		
		m_ckbPet2.SetCheck(a>0?1:0);

	}

	AnyScene tAnyScene = {0};
	tAnyScene.iBufSize = sizeof(AnyScene);
	tAnyScene.iSceneID = m_iSceneID;
	int iBytesReturned = 0;
	iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tAnyScene,sizeof(tAnyScene), &iBytesReturned);
	if (iRet >= 0)
	{
		int a = tAnyScene.iArithmeticEx ;
		a = a & (1<<1);
		m_ckbPept3.SetCheck(a);
		m_cboDevType.SetCurSel(tAnyScene.iDevType);
	}

}

void CLS_VcaPept::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VcaPept::OnBnClickedCheckSwitchSenceSnapEnable()
{
	VCASenceSnap tInfo = {0};
	tInfo.iSnapEnable = m_ckbSnapEnable.GetCheck();
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SENCE_SNAP, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedCheckSwitchSenceSnapEnable::NetClient_VCASetConfig[VCA_CMD_SENCE_SNAP] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","OnBnClickedCheckSwitchSenceSnapEnable::NetClient_VCASetConfig[VCA_CMD_SENCE_SNAP] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

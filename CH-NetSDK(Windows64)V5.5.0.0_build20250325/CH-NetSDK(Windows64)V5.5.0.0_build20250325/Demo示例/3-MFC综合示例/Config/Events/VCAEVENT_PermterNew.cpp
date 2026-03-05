// E:\SDK\trunk\Demo\NetClientDemo\Config\Events\VCAEVENT_PermterNew.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_PermterNew.h"


// CLS_VCAEVENT_PermterNew dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_PermterNew, CDialog)

CLS_VCAEVENT_PermterNew::CLS_VCAEVENT_PermterNew(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_PermterNew::IDD, pParent)
{

}

CLS_VCAEVENT_PermterNew::~CLS_VCAEVENT_PermterNew()
{
}

void CLS_VCAEVENT_PermterNew::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_NOALARM_MODE, m_CboNoAlarmMode);
	DDX_Control(pDX, IDC_CHECK_DISPLAY_RULE, m_ChkDisplayRule);
	DDX_Control(pDX, IDC_CHECK_DISPLAY_STATE, m_ChkDisplayState);
	DDX_Control(pDX, IDC_COMBO_Color, m_CboColor);
	DDX_Control(pDX, IDC_COMBO_ALARM_COLOR, m_CboAlarmColor);
	DDX_Control(pDX, IDC_COMBO_TARGET_TYPE, m_CboTargetType);
	DDX_Control(pDX, IDC_COMBO_MODE, m_CboMode);
	DDX_Control(pDX, IDC_EDIT_TYPE, m_EdtType);
	DDX_Control(pDX, IDC_EDIT_DIRECTION, m_EdtDirection);
	DDX_Control(pDX, IDC_EDIT_MIN_SIZE, m_EdtMinSize);
	DDX_Control(pDX, IDC_EDIT_MAX_SIZE, m_EdtMaxSize);
	DDX_Control(pDX, IDC_EDIT_POINT_NUM, m_EdtPointNum);
	DDX_Control(pDX, IDC_CHECK_DISPLAY_TARGET, m_ChkDisplayTarget);
	DDX_Control(pDX, IDC_EDIT_POINT, m_EdtPoint);
	DDX_Control(pDX, IDC_EDIT_MinDis, m_EdtMinDis);
	DDX_Control(pDX, IDC_EDIT_MinTime, m_EdtMinTime);
	DDX_Control(pDX, IDC_CHECK_RULE_VALID, m_ChkRuleValid);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_PermterNew, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_Draw, &CLS_VCAEVENT_PermterNew::OnBnClickedButtonDraw)
	ON_BN_CLICKED(IDC_BUTTON_PERIMTER_NEW_SET, &CLS_VCAEVENT_PermterNew::OnBnClickedButtonPerimterNewSet)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


BOOL CLS_VCAEVENT_PermterNew::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();
	return TRUE; 
}

void CLS_VCAEVENT_PermterNew::OnShowWindow(BOOL bShow, UINT nStatus)
{
	// TODO: add message handler code here
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdateUIPara();
	}
}

void CLS_VCAEVENT_PermterNew::OnLanguageChanged()
{	
	UpdateUIText();
	UpdateUIPara();
}

void CLS_VCAEVENT_PermterNew::UpdateUIText()
{
	m_CboNoAlarmMode.ResetContent();
	m_CboColor.ResetContent();
	m_CboAlarmColor.ResetContent();
	m_CboTargetType.ResetContent();
	m_CboMode.ResetContent();

	m_CboNoAlarmMode.SetItemData(0, m_CboNoAlarmMode.AddString(GetTextByLan(_T("不支持"), _T("UNsupport"))));
	m_CboNoAlarmMode.SetItemData(1, m_CboNoAlarmMode.AddString(GetTextByLan(_T("离开视频区域消警"), _T("Leave Video Area"))));
	m_CboNoAlarmMode.SetItemData(2, m_CboNoAlarmMode.AddString(GetTextByLan(_T("离开检测区域消警"), _T("Leave Detec Area"))));

	const CString strColor[] = {GetTextByLan(_T("预留0"), _T("NoSense")), GetTextByLan(_T("红色"), _T("Red")), GetTextByLan(_T("绿色"), _T("Green")), GetTextByLan(_T("黄色"), _T("Yellow")), 
		GetTextByLan(_T("蓝色"), _T("Blue")), GetTextByLan(_T("紫色"), _T("Purple")), GetTextByLan(_T("青色"), _T("Cyan")), 
		GetTextByLan(_T("黑色"), _T("Black")), GetTextByLan(_T("白色"), _T("White"))};

	for (int i = 0; i < sizeof(strColor)/sizeof(CString); i++)
	{
		m_CboColor.InsertString(i, strColor[i]);
		m_CboAlarmColor.InsertString(i, strColor[i]);
	}
	m_CboTargetType.SetItemData(0, m_CboTargetType.AddString(GetTextByLan(_T("不区分"), _T("Not Distinguish"))));
	m_CboTargetType.SetItemData(1, m_CboTargetType.AddString(GetTextByLan(_T("区分人"), _T("Distinguish People"))));
	m_CboTargetType.SetItemData(2, m_CboTargetType.AddString(GetTextByLan(_T("区分车"), _T("Distinguish Car"))));
	m_CboTargetType.SetItemData(3, m_CboTargetType.AddString(GetTextByLan(_T("区分人车"), _T("Distinguish People&Car"))));

	m_CboMode.SetItemData(0, m_CboMode.AddString(GetTextByLan(_T("入侵"), _T("Invade"))));
	m_CboMode.SetItemData(1, m_CboMode.AddString(GetTextByLan(_T("进入"), _T("In"))));
	m_CboMode.SetItemData(2, m_CboMode.AddString(GetTextByLan(_T("离开"), _T("Out"))));

	m_CboNoAlarmMode.SetCurSel(1);
	m_CboColor.SetCurSel(0);
	m_CboAlarmColor.SetCurSel(0);
	m_CboTargetType.SetCurSel(0);
	m_CboMode.SetCurSel(0);

	SetDlgItemInt(IDC_EDIT_MIN_SIZE, 5);
	SetDlgItemInt(IDC_EDIT_MAX_SIZE, 30);

	SetDlgItemText(IDC_STATIC_NOALARM_MODE, GetTextByLan(_T("消警类型"), _T("NoAlarmMode")));
	SetDlgItemText(IDC_CHECK_DISPLAY_RULE, GetTextByLan(_T("显示规则"), _T("DisplayRule")));
	SetDlgItemText(IDC_CHECK_DISPLAY_STATE, GetTextByLan(_T("显示报警计数统计"), _T("DisplayStatistics")));
	SetDlgItemText(IDC_CHECK_DISPLAY_TARGET, GetTextByLan(_T("显示规则框"), _T("DisplayTarget")));
	SetDlgItemText(IDC_CHECK_RULE_VALID, GetTextByLan(_T("规则有效"), _T("RuleValid")));
	SetDlgItemText(IDC_STATIC_Color, GetTextByLan(_T("区域颜色"), _T("AreaColor")));
	SetDlgItemText(IDC_STATIC_ALARM_COLOR, GetTextByLan(_T("报警颜色"), _T("AlarmColor")));
	SetDlgItemText(IDC_STATIC_TARGET_TYPE, GetTextByLan(_T("目标类型"), _T("TargetType")));
	SetDlgItemText(IDC_STATIC_Mode, GetTextByLan(_T("检测类型"), _T("DetecMode")));
	SetDlgItemText(IDC_STATIC_MinDis, GetTextByLan(_T("最小距离"), _T("MinDistance")));
	SetDlgItemText(IDC_STATIC_MinTime, GetTextByLan(_T("最短时间"), _T("MinTime")));
	SetDlgItemText(IDC_STATIC_TYPE, GetTextByLan(_T("方向限制"), _T("DirecLimit")));
	SetDlgItemText(IDC_STATIC_DIRECTION, GetTextByLan(_T("方向角度"), _T("DirecAngle")));
	SetDlgItemText(IDC_STATIC_MinSize, GetTextByLan(_T("最小尺寸"), _T("MinSize")));
	SetDlgItemText(IDC_STATIC_MaxSize, GetTextByLan(_T("最大尺寸"), _T("MaxSize")));
	SetDlgItemText(IDC_STATIC_POINT_NUM, GetTextByLan(_T("顶点个数"), _T("PointNum")));
	SetDlgItemText(IDC_STATIC_POINT, GetTextByLan(_T("坐标"), _T("Point")));
	SetDlgItemText(IDC_BUTTON_Draw, GetTextByLan(_T("绘制"), _T("Draw")));
	SetDlgItemText(IDC_BUTTON_PERIMTER_NEW_SET, GetTextByLan(_T("设置"), _T("Set")));
}

void CLS_VCAEVENT_PermterNew::UpdateUIPara()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PermterNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	VCAPerimeter vc = {0};
	vc.stRule.iRuleID = m_iRuleID;
	vc.stRule.iSceneID = m_iSceneID;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_PERIMETER, m_iChannelNO, &vc, sizeof(VCAPerimeter));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_PermterNew::NetClient_VCAGetConfig[VCA_CMD_PERIMETER] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_ChkRuleValid.SetCheck(vc.stRule.iValid);
		m_CboNoAlarmMode.SetCurSel(vc.iNoAlarmMode);
		m_ChkDisplayRule.SetCheck(vc.stDisplayParam.iDisplayRule);
		m_ChkDisplayState.SetCheck(vc.stDisplayParam.iDisplayStat);
		m_CboColor.SetCurSel(vc.stDisplayParam.iColor);
		m_CboAlarmColor.SetCurSel(vc.stDisplayParam.iAlarmColor);
		m_CboTargetType.SetCurSel(vc.iTargetTypeCheck);
		m_CboMode.SetCurSel(vc.iMode);
		SetDlgItemInt(IDC_EDIT_MinDis, vc.iMinDistance);
		SetDlgItemInt(IDC_EDIT_MinTime, vc.iMinTime);
		SetDlgItemInt(IDC_EDIT_TYPE, vc.iType);
		SetDlgItemInt(IDC_EDIT_DIRECTION, vc.iDirection);
		SetDlgItemInt(IDC_EDIT_POINT_NUM, vc.stRegion.iPointNum);
		CString cstPointBuf = "";
		for (int i = 0; i < vc.stRegion.iPointNum && i < LEN_32; i++)
		{
			cstPointBuf.AppendFormat("(%d,%d)", vc.stRegion.stPoints[i].iX, vc.stRegion.stPoints[i].iY);
		}
		m_EdtPoint.SetWindowText(cstPointBuf);
		SetDlgItemInt(IDC_EDIT_MIN_SIZE, vc.iMiniSize);
		SetDlgItemInt(IDC_EDIT_MAX_SIZE, vc.iMaxSize);
		m_ChkDisplayTarget.SetCheck(vc.iDisplayTarget);
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_PermterNew::NetClient_VCAGetConfig[VCA_CMD_PERIMETER] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

// CLS_VCAEVENT_PermterNew message handler

void CLS_VCAEVENT_PermterNew::OnBnClickedButtonDraw()
{
	// TODO: Add control notification handler code here
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		m_EdtPoint.SetWindowText(cPointBuf);
		SetDlgItemInt(IDC_EDIT_POINT_NUM, iPointNum);
		SetDlgItemInt(IDC_EDIT_DIRECTION, iDirection);

	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_PermterNew::OnBnClickedButtonPerimterNewSet()
{
	// TODO: Add control notification handler code here
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_PermterNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	VCAPerimeter vc = {0};
	vc.iBufSize = sizeof(VCAPerimeter);
	vc.stRule.iRuleID = m_iRuleID;
	vc.stRule.iSceneID = m_iSceneID;
	vc.stRule.iValid = m_ChkRuleValid.GetCheck();
	vc.iNoAlarmMode = m_CboNoAlarmMode.GetCurSel();
	vc.stDisplayParam.iDisplayRule = m_ChkDisplayRule.GetCheck();
	vc.stDisplayParam.iDisplayStat = m_ChkDisplayState.GetCheck();
	vc.stDisplayParam.iColor = m_CboColor.GetCurSel();
	vc.stDisplayParam.iAlarmColor = m_CboAlarmColor.GetCurSel();
	vc.iTargetTypeCheck = m_CboTargetType.GetCurSel();
	vc.iMode = m_CboMode.GetCurSel();
	vc.iMinDistance = GetDlgItemInt(IDC_EDIT_MinDis);
	vc.iMinTime = GetDlgItemInt(IDC_EDIT_MinTime);
	vc.iType = GetDlgItemInt(IDC_EDIT_TYPE);
	vc.iDirection = GetDlgItemInt(IDC_EDIT_DIRECTION);
	vc.stRegion.iPointNum = GetDlgItemInt(IDC_EDIT_POINT_NUM);
	CString strPoint = "";
	GetDlgItemText(IDC_EDIT_POINT, strPoint);
	GetPolyFromString(strPoint, vc.stRegion.iPointNum, vc.stRegion);
	vc.iMiniSize = GetDlgItemInt(IDC_EDIT_MIN_SIZE);
	vc.iMaxSize = GetDlgItemInt(IDC_EDIT_MAX_SIZE);
	vc.iDisplayTarget = m_ChkDisplayTarget.GetCheck();

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_PERIMETER, m_iChannelNO, &vc, sizeof(VCAPerimeter));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_PermterNew::NetClient_VCASetConfig[VCA_CMD_PERIMETER] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_PermterNew::NetClient_VCASetConfig[VCA_CMD_PERIMETER] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

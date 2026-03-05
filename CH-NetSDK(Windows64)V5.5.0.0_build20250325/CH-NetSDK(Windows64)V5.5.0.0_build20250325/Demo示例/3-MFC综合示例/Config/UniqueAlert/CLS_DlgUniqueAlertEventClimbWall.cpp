#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgUniqueAlertEventClimbWall.h"

IMPLEMENT_DYNAMIC(CLS_DlgUniqueAlertEventClimbWall, CDialog)

CLS_DlgUniqueAlertEventClimbWall::CLS_DlgUniqueAlertEventClimbWall(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_DlgUniqueAlertEventClimbWall::IDD, pParent)
{
    m_iLogonID = -1;
    m_iChannelNo = -1;
    m_iStreamNo = -1;
}

CLS_DlgUniqueAlertEventClimbWall::~CLS_DlgUniqueAlertEventClimbWall()
{

}

void CLS_DlgUniqueAlertEventClimbWall::DoDataExchange(CDataExchange* pDX)
{
    CLS_BasePage::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_CBO_ALERT_ALARM_COLOR, m_cboAlarmAreaColor);
    DDX_Control(pDX, IDC_CBO_ALERT_SCENE, m_cboAlertSceneNo);
    DDX_Control(pDX, IDC_CBO_ALERT_COLOR, m_cboAreaColor);
    DDX_Control(pDX, IDC_CBO_ALERT_ALARM_COLOR, m_cboAlarmAreaColor);
}


BEGIN_MESSAGE_MAP(CLS_DlgUniqueAlertEventClimbWall, CDialog)
    ON_WM_SHOWWINDOW()
    ON_CBN_SELCHANGE(IDC_CBO_ALERT_SCENE, &CLS_DlgUniqueAlertEventClimbWall::OnCbnSelchangeCboAlertCLIMBWALLScene)
    ON_BN_CLICKED(IDC_BTN_ALERT_EVENT_SET, &CLS_DlgUniqueAlertEventClimbWall::OnBnClickedBtnAlertCLIMBWALLEventSet)
END_MESSAGE_MAP()

BOOL CLS_DlgUniqueAlertEventClimbWall::OnInitDialog()
{
    CLS_BasePage::OnInitDialog();
    return TRUE;
}

void CLS_DlgUniqueAlertEventClimbWall::OnShowWindow(BOOL bShow, UINT nStatus)
{
    CLS_BasePage::OnShowWindow(bShow, nStatus);
    if (bShow)
    {
        UI_InitDlgItemText();
        UI_UpdateInterfaceParam();
    }
}

void CLS_DlgUniqueAlertEventClimbWall::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
    if (m_iLogonID == _iLogonID && m_iChannelNo == _iChannelNo && m_iStreamNo == _iStreamNo)
    {
        return;
    }

    m_iLogonID = _iLogonID;
    m_iChannelNo = ((_iChannelNo < 0) ? 0 : _iChannelNo);
    m_iStreamNo = _iStreamNo;
    UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertEventClimbWall::OnLanguageChanged( int _iLanguage )
{
    UI_InitDlgItemText();
}

void CLS_DlgUniqueAlertEventClimbWall::UI_InitDlgItemText()
{
    int iCurSel = m_cboAlertSceneNo.GetCurSel();
    int iCurSelEx = 0;
    m_cboAlertSceneNo.ResetContent();
    for (int i = 0; i < MAX_UNIQUE_ALERT_SCENE_NUM; i++)
    {
        m_cboAlertSceneNo.SetItemData(m_cboAlertSceneNo.AddString(GetTextEx(IDS_ALERT_SCENE) + IntToCString(i + 1)), i);
    }
    m_cboAlertSceneNo.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

    CStringArray cstrArrayColor;
    cstrArrayColor.SetSize(10);
    cstrArrayColor.InsertAt(1, GetTextEx(IDS_VCA_COL_RED));
    cstrArrayColor.InsertAt(2, GetTextEx(IDS_VCA_COL_GREEN));
    cstrArrayColor.InsertAt(3, GetTextEx(IDS_VCA_COL_YELLOW));
    cstrArrayColor.InsertAt(4, GetTextEx(IDS_VCA_COL_BLUE));
    cstrArrayColor.InsertAt(5, GetTextEx(IDS_VCA_COL_MAGENTA));
    cstrArrayColor.InsertAt(6, GetTextEx(IDS_VCA_COL_CYAN));
    cstrArrayColor.InsertAt(7, GetTextEx(IDS_VCA_COL_BLACK));
    cstrArrayColor.InsertAt(8, GetTextEx(IDS_VCA_COL_WHITE));
    cstrArrayColor.FreeExtra();

    iCurSel = m_cboAreaColor.GetCurSel();
    iCurSelEx = m_cboAlarmAreaColor.GetCurSel();
    m_cboAreaColor.ResetContent();
    m_cboAlarmAreaColor.ResetContent();
    for (int i = 0; i < cstrArrayColor.GetCount(); i++)
    {
        if (cstrArrayColor.GetAt(i).IsEmpty())
        {
            continue;
        }

        m_cboAreaColor.SetItemData(m_cboAreaColor.AddString(cstrArrayColor.GetAt(i)), i);
        m_cboAlarmAreaColor.SetItemData(m_cboAlarmAreaColor.AddString(cstrArrayColor.GetAt(i)), i);
    }
    m_cboAreaColor.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
    m_cboAlarmAreaColor.SetCurSel(((iCurSelEx < 0) ? 0 : iCurSelEx));

    SetDlgItemText(IDC_STC_ALERT_SCENE, GetTextByLan(_T("场景"), _T("SceneId")));
    SetDlgItemText(IDC_BTN_ALERT_EVENT_SET, GetTextByLan(_T("设置"), _T("Set")));
    SetDlgItemText(IDC_CHK_ALERT_DISPLAY_RULE, GetTextByLan(_T("显示报警规则"), _T("Display rules")));
    SetDlgItemText(IDC_CHK_ALERT_DISPLAY_STAT, GetTextByLan(_T("显示报警计数"), _T("Display alarm count")));
    SetDlgItemText(IDC_STC_ALERT_COLOR, GetTextByLan(_T("区域颜色"), _T("Regional color")));
    SetDlgItemText(IDC_STC_ALERT_ALARM_COLOR, GetTextByLan(_T("报警区域颜色"), _T("Alarm area color")));
    SetDlgItemText(IDC_STC_ALERT_MIN_SIZE, GetTextByLan(_T("最小尺寸"), _T("Minimum size")));
    SetDlgItemText(IDC_STC_ALERT_MAX_SIZE, GetTextByLan(_T("最大尺寸"), _T("Largest size")));
    SetDlgItemText(IDC_CHK_ALERT_VALID, GetTextByLan(_T("启用事件检测"), _T("Enable event detection")));
    SetDlgItemText(IDC_CHK_ALERT_DISPLAY_TARGET, GetTextByLan(_T("显示目标"), _T("According to the target")));
    SetDlgItemText(IDC_STC_ALERT_SENSITIVITY, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));

}

void CLS_DlgUniqueAlertEventClimbWall::UI_UpdateInterfaceParam()
{
    if (m_iLogonID < 0 || m_iChannelNo < 0)
    {
        return;
    }
    int iRet = RET_FAILED;
    ClimbWallAlertPara  tInfo = {0};
    tInfo.iSceneID = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
    tInfo.iEventNum = 0;
    iRet = NetClient_GetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_CLIMBWALLPARA, m_iChannelNo, &tInfo, sizeof(tInfo));
    if (iRet < RET_SUCCESS)
    {
        goto EXIT_FUNC;
    }

    ((CButton*)(GetDlgItem(IDC_CHK_ALERT_VALID)))->SetCheck(tInfo.iValid ? BST_CHECKED : BST_UNCHECKED);
    ((CButton*)(GetDlgItem(IDC_CHK_ALERT_DISPLAY_RULE)))->SetCheck(tInfo.iDisplayRule ? BST_CHECKED : BST_UNCHECKED);
    ((CButton*)(GetDlgItem(IDC_CHK_ALERT_DISPLAY_STAT)))->SetCheck(tInfo.iDisplayStat ? BST_CHECKED : BST_UNCHECKED);
    ((CButton*)(GetDlgItem(IDC_CHK_ALERT_DISPLAY_TARGET)))->SetCheck(tInfo.iDisplayTarget ? BST_CHECKED : BST_UNCHECKED);

    m_cboAreaColor.SetCurSel(GetCboSel(&m_cboAreaColor, tInfo.iColor));
    m_cboAlarmAreaColor.SetCurSel(GetCboSel(&m_cboAlarmAreaColor, tInfo.iAlarmColor));

    SetDlgItemInt(IDC_EDT_ALERT_SENSITIVITY, tInfo.iSensitivity);
    SetDlgItemInt(IDC_EDT_ALERT_MIN_SIZE, tInfo.iMiniSize);
    SetDlgItemInt(IDC_EDT_ALERT_MAX_SIZE, tInfo.iMaxSize);
    iRet = RET_SUCCESS;
EXIT_FUNC:
    if (iRet < RET_SUCCESS)
    {
        AddLog(LOG_TYPE_FAIL,"","NetClient_GetUnipueAlertConfig[CLIMBWALL](%d,%d)", m_iLogonID, m_iChannelNo);
    }
    else
    {
        AddLog(LOG_TYPE_SUCC,"","NetClient_GetUnipueAlertConfig[CLIMBWALL](%d,%d)", m_iLogonID, m_iChannelNo);
    }
    return;
}


void CLS_DlgUniqueAlertEventClimbWall::OnCbnSelchangeCboAlertCLIMBWALLScene()
{
    UI_UpdateInterfaceParam();
}

void CLS_DlgUniqueAlertEventClimbWall::OnBnClickedBtnAlertCLIMBWALLEventSet()
{
    if (m_iLogonID < 0 || m_iChannelNo < 0)
    {
        return;
    }

    int iRet = RET_FAILED;
    ClimbWallAlertPara tInfo = {0};
    tInfo.iChannelNo = m_iChannelNo;
    tInfo.iSceneID = m_cboAlertSceneNo.GetItemData(m_cboAlertSceneNo.GetCurSel());
    tInfo.iEventNum = 0;
    tInfo.iSensitivity = GetDlgItemInt(IDC_EDT_ALERT_SENSITIVITY);
    tInfo.iMiniSize = GetDlgItemInt(IDC_EDT_ALERT_MIN_SIZE);
    tInfo.iMaxSize = GetDlgItemInt(IDC_EDT_ALERT_MAX_SIZE);

    tInfo.iValid = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_VALID)))->GetCheck()) ? 1 : 0;
    tInfo.iDisplayRule = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_DISPLAY_RULE)))->GetCheck()) ? 1 : 0;
    tInfo.iDisplayStat = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_DISPLAY_STAT)))->GetCheck()) ? 1 : 0;
    tInfo.iDisplayTarget = (BST_CHECKED == ((CButton*)(GetDlgItem(IDC_CHK_ALERT_DISPLAY_TARGET)))->GetCheck()) ? 1 : 0;

    tInfo.iColor = m_cboAreaColor.GetItemData(m_cboAreaColor.GetCurSel());
    tInfo.iAlarmColor = m_cboAlarmAreaColor.GetItemData(m_cboAlarmAreaColor.GetCurSel());


    iRet = NetClient_SetUnipueAlertConfig(m_iLogonID, UNIQUE_ALERT_CMD_CLIMBWALLPARA, m_iChannelNo, &tInfo, sizeof(tInfo));
    if (iRet < RET_SUCCESS)
    {
        goto EXIT_FUNC;
    }

    iRet = RET_SUCCESS;
EXIT_FUNC:
    if (iRet < RET_SUCCESS)
    {
        AddLog(LOG_TYPE_FAIL,"","NetClient_SetUnipueAlertConfig[CLIMBWALL](%d,%d)", m_iLogonID, m_iChannelNo);
    }
    else
    {
        AddLog(LOG_TYPE_SUCC,"","NetClient_SetUnipueAlertConfig[CLIMBWALL](%d,%d)", m_iLogonID, m_iChannelNo);
    }
    return;
}

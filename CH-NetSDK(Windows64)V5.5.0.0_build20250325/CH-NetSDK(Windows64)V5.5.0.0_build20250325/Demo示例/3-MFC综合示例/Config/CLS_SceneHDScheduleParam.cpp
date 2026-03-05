// CLS_SceneParam.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_SceneHDScheduleParam.h"

#define MAX_TEMPLATE_TYPE    4
#define MAX_CALIBRATE_SCENE_NUM  16
// CLS_SceneParam dialog

IMPLEMENT_DYNAMIC(CLS_SceneHDScheduleParam, CDialog)

CLS_SceneHDScheduleParam::CLS_SceneHDScheduleParam(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_SceneHDScheduleParam::IDD, pParent)
{
    m_iLogonID = -1;
    m_iChannelNo = -1;
}

CLS_SceneHDScheduleParam::~CLS_SceneHDScheduleParam()
{
}

void CLS_SceneHDScheduleParam::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_COMBO_SCENEID, m_comboHdSCheduleScene);
    DDX_Control(pDX, IDC_COMBO_TEMPLATETYPE, m_comboHdSCheduleType);
    DDX_Control(pDX, IDC_CHECK_ENABLE, m_chkHdSCheduleEnable);
    DDX_Control(pDX, IDC_SLIDER_DAYID, m_sldHdSCheduleDayId);
    DDX_Control(pDX, IDC_SLIDER_NIGHTID, m_sldHdSCheduleNightId);
}

BEGIN_MESSAGE_MAP(CLS_SceneHDScheduleParam, CLS_BasePage)
    ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_SceneHDScheduleParam::OnBnClickedButtonSet)
    ON_WM_HSCROLL()
    ON_CBN_SELCHANGE(IDC_COMBO_SCENEID, &CLS_SceneHDScheduleParam::OnCbnSelchangeComboSceneid)
    ON_CBN_SELCHANGE(IDC_COMBO_TEMPLATETYPE, &CLS_SceneHDScheduleParam::OnCbnSelchangeComboTemplatetype)
END_MESSAGE_MAP()

void  CLS_SceneHDScheduleParam::OnHScroll(UINT   nSBCode,   UINT   nPos,   CScrollBar*   pScrollBar)     
{   
    switch(pScrollBar->GetDlgCtrlID())   
    {   
    case IDC_SLIDER_DAYID://ID of the CSliderCtrl control to be processed
        { 
            //deal with
            SetDlgItemInt(IDC_STATIC_DAYID_POS, m_sldHdSCheduleDayId.GetPos());
            break;   
        }  
    case IDC_SLIDER_NIGHTID://ID of the CSliderCtrl control to be processed
        { 
            //deal with
            SetDlgItemInt(IDC_STATIC_NIGHTID_POS, m_sldHdSCheduleNightId.GetPos());
            break;   
        }     
    default:break;
    }   
    CLS_BasePage::OnHScroll(nSBCode,   nPos,   pScrollBar);   
}

void CLS_SceneHDScheduleParam::OnBnClickedButtonSet()
{
    if (m_iLogonID == -1 || m_iChannelNo == -1)
    {
        AddLog(LOG_TYPE_MSG, "", "CLS_SceneHDScheduleParam::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
        return;
    }
    VcaHDSchedule tInfo = {0};
    tInfo.iSize =  sizeof(tInfo);

    tInfo.iSceneId = m_comboHdSCheduleScene.GetCurSel();
    tInfo.iType = m_comboHdSCheduleType.GetCurSel();
    tInfo.iEnable = (BST_CHECKED == m_chkHdSCheduleEnable.GetCheck()) ? 1 : 0;
    tInfo.iDayId = m_sldHdSCheduleDayId.GetPos();
    tInfo.iNightId = m_sldHdSCheduleNightId.GetPos();

    int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_HDSCHEDULE, m_iChannelNo, &tInfo, sizeof(VcaHDSchedule));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCASetConfig]VCA_CMD_HDSCHEDULE fail!");
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "[NetClient_VCASetConfig]VCA_CMD_HDSCHEDULE SUCCESS!");

    }
}

void CLS_SceneHDScheduleParam::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    //PARA_CALIBRATE_MODE
    int* piResult = (int*)_pPara; 
    VcaHDSchedule* pResult = (VcaHDSchedule*)_pPara;
    switch(_iParaType)
    {
    case PARA_VCA_HDSCHEDULE:
        {
            UpdatePageUI();
        }
        break;
    default:
        break;
    }
}

BOOL CLS_SceneHDScheduleParam::OnInitDialog()
{
    CLS_BasePage::OnInitDialog();

    InitPageUI();
    UpdatePageUI();
    return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_SceneHDScheduleParam::OnChannelChanged( int _iLogonID,int _iChannelNo, int _iStreamNo)
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

    UpdatePageUI();
}

void CLS_SceneHDScheduleParam::OnLanguageChanged(int _iLanguage)
{
    UpdatePageUI();
}

void CLS_SceneHDScheduleParam::InitPageUI()
{
    SetDlgItemText(IDC_STATIC_SCENEID, GetTextByLan("场景号", "SceneID"));
    SetDlgItemText(IDC_STATIC_TEMPLATETYPE, GetTextByLan("模板类型", "TemplateType"));
    SetDlgItemText(IDC_CHECK_ENABLE, GetTextByLan("使能", "Enable"));
    SetDlgItemText(IDC_STATIC_DAYID, GetTextByLan("日模板ID", "DayTemplateID"));
    SetDlgItemText(IDC_STATIC_NIGHTID, GetTextByLan("夜模板ID", "NightTemplateID"));
    SetDlgItemText(IDC_BUTTON_SET, GetTextByLan("设置", "Set"));

    int iCurSel = m_comboHdSCheduleScene.GetCurSel();
    m_comboHdSCheduleScene.ResetContent();
    //Indoor and outdoor scenes
    InsertString(m_comboHdSCheduleScene, 0, IDS_HD_MODE_INDOOR);
    InsertString(m_comboHdSCheduleScene, 1, IDS_HD_MODE_OUTDOOR);
    for (int idx = 2; idx < MAX_CALIBRATE_SCENE_NUM; idx++)
    {
        m_comboHdSCheduleScene.SetItemData(m_comboHdSCheduleScene.AddString(IntToCString(idx)), idx);
    }
    m_comboHdSCheduleScene.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

    iCurSel = m_comboHdSCheduleType.GetCurSel();
    m_comboHdSCheduleType.ResetContent();
    m_comboHdSCheduleType.SetItemData(m_comboHdSCheduleType.AddString(GetTextByLan("保留", "Reserve")), 0);
    m_comboHdSCheduleType.SetItemData(m_comboHdSCheduleType.AddString(GetTextByLan("通用", "Common")), 1);
    m_comboHdSCheduleType.SetItemData(m_comboHdSCheduleType.AddString(GetTextByLan("短帧", "Short Frame")), 2);
    m_comboHdSCheduleType.SetItemData(m_comboHdSCheduleType.AddString(GetTextByLan("长帧", "Long Frame")), 3);
    m_comboHdSCheduleType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

    m_chkHdSCheduleEnable.SetCheck(BST_UNCHECKED);

    m_sldHdSCheduleDayId.SetRange(-1, 31);
    m_sldHdSCheduleDayId.SetLineSize(1);
    m_sldHdSCheduleNightId.SetRange(-1, 31);
    m_sldHdSCheduleNightId.SetLineSize(1);

}

void CLS_SceneHDScheduleParam::UpdatePageUI()
{
    if (m_iLogonID == -1 || m_iChannelNo == -1)
    {
        AddLog(LOG_TYPE_MSG, "", "CLS_SceneHDScheduleParam::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
        return;
    }

    VcaHDSchedule tInfo = {0};
    tInfo.iSize =  sizeof(tInfo);
    tInfo.iSceneId = m_comboHdSCheduleScene.GetCurSel();
    tInfo.iType = m_comboHdSCheduleType.GetCurSel();
    int iReturn = -1;
    int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_HDSCHEDULE, m_iChannelNo, &tInfo, sizeof(VcaHDSchedule));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCAGetConfig]VCA_CMD_HDSCHEDULE fail!");
    }
    else
    {
        m_comboHdSCheduleScene.SetCurSel(tInfo.iSceneId);
        m_comboHdSCheduleType.SetCurSel(tInfo.iType);
        m_chkHdSCheduleEnable.SetCheck((0 == tInfo.iEnable) ? BST_UNCHECKED : BST_CHECKED);

        m_sldHdSCheduleDayId.SetPos(tInfo.iDayId);
        SetDlgItemInt(IDC_STATIC_DAYID_POS, tInfo.iDayId);
        m_sldHdSCheduleNightId.SetPos(tInfo.iNightId);
        SetDlgItemInt(IDC_STATIC_NIGHTID_POS, tInfo.iNightId);
    }

    return;
} 

void CLS_SceneHDScheduleParam::OnCbnSelchangeComboSceneid()
{
    UpdatePageUI();
}

void CLS_SceneHDScheduleParam::OnCbnSelchangeComboTemplatetype()
{
    UpdatePageUI();
}

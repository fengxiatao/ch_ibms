// CLS_CalibrateMode.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_CalibrateMode.h"


// CLS_CalibrateMode dialog
#define MAX_DEVICE_TYPE_COUNT    2
#define MAX_CALIBRATE_SCENE_NUM  32
#define EXTREMELY_RATIO          10000

IMPLEMENT_DYNAMIC(CLS_CalibrateMode, CDialog)

CLS_CalibrateMode::CLS_CalibrateMode(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_CalibrateMode::IDD, pParent)
{
    m_iLogonID = -1;
    m_iChannelNo = -1;
}

CLS_CalibrateMode::~CLS_CalibrateMode()
{
}

void CLS_CalibrateMode::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_COMBO_SCENEID, m_comboScene);
    DDX_Control(pDX, IDC_COMBO_DEVICEID, m_comboDeviceID);
    DDX_Control(pDX, IDC_COMBO_CALIBRATEMODE, m_comboCalibrateMode);
    DDX_Control(pDX, IDC_SLIDER_PAN, m_sldPan);
    DDX_Control(pDX, IDC_SLIDER_Tilt, m_sldTilt);
    DDX_Control(pDX, IDC_SLIDER_ZOOM, m_sldZoom);
    DDX_Control(pDX, IDC_COMBO_STATE, m_comboState);
    DDX_Control(pDX, IDC_COMBO_SCENETYPE, m_comboSceneType);

}


BEGIN_MESSAGE_MAP(CLS_CalibrateMode, CLS_BasePage)
    ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_CalibrateMode::OnBnClickedButtonSet)
    ON_WM_HSCROLL()
    ON_BN_CLICKED(IDC_BUTTON_SET3, &CLS_CalibrateMode::OnBnClickedButtonSet3)
    ON_CBN_SELCHANGE(IDC_COMBO_SCENEID, &CLS_CalibrateMode::OnCbnSelchangeComboSceneid)
END_MESSAGE_MAP()


// CLS_CalibrateMode message handler
void  CLS_CalibrateMode::OnHScroll(UINT   nSBCode,   UINT   nPos,   CScrollBar*   pScrollBar)     
{   
    switch(pScrollBar->GetDlgCtrlID())   
    {   
    case IDC_SLIDER_PAN://ID of the CSliderCtrl control to be processed
        { 
            //deal with
            SetDlgItemInt(IDC_STATIC_CALIBRATEMODE_PAN, m_sldPan.GetPos());
            break;   
        }  
    case IDC_SLIDER_Tilt://ID of the CSliderCtrl control to be processed
        { 
            //deal with
            SetDlgItemInt(IDC_STATIC_CALIBRATEMODE_TILT, m_sldTilt.GetPos());

            break;   
        }   
    case IDC_SLIDER_ZOOM://ID of the CSliderCtrl control to be processed
        { 
            //deal with
            SetDlgItemInt(IDC_STATIC_CALIBRATEMODE_ZOOM, m_sldZoom.GetPos());

            break;   
        }   
    default:break;
    }   
    CLS_BasePage::OnHScroll(nSBCode,   nPos,   pScrollBar);   
}

void CLS_CalibrateMode::OnBnClickedButtonSet()
{
    if (m_iLogonID == -1 || m_iChannelNo == -1)
    {
        AddLog(LOG_TYPE_MSG, "", "CLS_CalibrateMode::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
        return;
    }
    CalibrateMode tInfo = {0};
    tInfo.iSize =  sizeof(tInfo);

    tInfo.iSceneAreaNo = m_comboScene.GetCurSel();
    tInfo.iLinkDevNo = m_comboDeviceID.GetCurSel();
    tInfo.iCalibrateMode = m_comboCalibrateMode.GetCurSel();
    tInfo.iPan = m_sldPan.GetPos();
    tInfo.iTilt = m_sldTilt.GetPos();
    tInfo.iZoom = m_sldZoom.GetPos();
    tInfo.iState = m_comboState.GetCurSel();
    tInfo.iSceneType = m_comboSceneType.GetCurSel();

    int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_CALIBRATE_MODE, m_iChannelNo, &tInfo, sizeof(CalibrateMode));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_CALIBRATE_MODE fail!");
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_CALIBRATE_MODE SUCCESS!");

    }
}

void CLS_CalibrateMode::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    //PARA_CALIBRATE_MODE
    int* piResult = (int*)_pPara; 
    switch(_iParaType)
    {
    case PARA_CALIBRATE_MODE:
        {
            UpdatePageUI();
        }
        break;
    default:
        break;
    }
}

BOOL CLS_CalibrateMode::OnInitDialog()
{
    CLS_BasePage::OnInitDialog();

    InitPageUI();
    UpdatePageUI();
    return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_CalibrateMode::OnChannelChanged( int _iLogonID,int _iChannelNo, int _iStreamNo)
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

void CLS_CalibrateMode::OnLanguageChanged(int _iLanguage)
{
    InitPageUI();
    UpdatePageUI();
}

void CLS_CalibrateMode::InitPageUI()
{

    SetDlgItemText(IDC_STATIC_SCENETYPE, GetTextByLan("场景类型", "SceneType"));
    SetDlgItemText(IDC_STATIC_SCENEID, GetTextByLan("场景号", "SceneID"));
    SetDlgItemText(IDC_STATIC_DEVICEID, GetTextByLan("设备编号", "DeviceID"));
    SetDlgItemText(IDC_STATIC_CALIBRATEMODE, GetTextByLan("标定方式", "CalibrateMode"));
    SetDlgItemText(IDC_STATIC_STATE, GetTextByLan("标定状态", "CalibrateState"));
    SetDlgItemText(IDC_BUTTON_SET, GetTextByLan("设置", "Set"));

    int iCurSel = m_comboScene.GetCurSel();
    m_comboScene.ResetContent();
    //Indoor and outdoor scenes
    InsertString(m_comboScene, 0, IDS_HD_MODE_INDOOR);
    InsertString(m_comboScene, 1, IDS_HD_MODE_OUTDOOR);
    for (int idx = 2; idx < MAX_CALIBRATE_SCENE_NUM; idx++)
    {
        m_comboScene.SetItemData(m_comboScene.AddString(IntToCString(idx)), idx);
    }
    m_comboScene.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

    iCurSel = m_comboDeviceID.GetCurSel();
    m_comboDeviceID.ResetContent();
    for (int idx = 0; idx < MAX_DEVICE_TYPE_COUNT; idx++)
    {
        m_comboDeviceID.SetItemData(m_comboDeviceID.AddString(IntToCString(idx)), idx);
    }
    m_comboDeviceID.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

    iCurSel = m_comboCalibrateMode.GetCurSel();
    m_comboCalibrateMode.ResetContent();
    m_comboCalibrateMode.SetItemData(m_comboCalibrateMode.AddString(GetTextByLan("保留", "Reserve")), 0);
    m_comboCalibrateMode.SetItemData(m_comboCalibrateMode.AddString(GetTextByLan("自动", "Auto")), 1);
    //m_comboCalibrateMode.SetItemData(m_comboCalibrateMode.AddString(GetTextByLan("手动", "Manual")), 2);
    m_comboCalibrateMode.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

    m_sldPan.SetRange(0, 36000);
    m_sldTilt.SetRange(1000, 19000);
    m_sldZoom.SetRange(0, 100000);

    iCurSel = m_comboState.GetCurSel();
    m_comboState.ResetContent();
    m_comboState.SetItemData(m_comboState.AddString(GetTextByLan("未标定", "Calibrate No")), 0);
    m_comboState.SetItemData(m_comboState.AddString(GetTextByLan("标定中", "Calibrating")), 1);
    m_comboState.SetItemData(m_comboState.AddString(GetTextByLan("标定成功", "Calibrate OK")), 2);
    m_comboState.SetItemData(m_comboState.AddString(GetTextByLan("标定失败", "Calibrate failed")), 3);
    m_comboState.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

    iCurSel = m_comboSceneType.GetCurSel();
    m_comboSceneType.ResetContent();
    m_comboSceneType.SetItemData(m_comboSceneType.AddString(GetTextByLan("智能分析", "VCA")), 0);
    m_comboSceneType.SetItemData(m_comboSceneType.AddString(GetTextByLan("警戒", "Alert")), 1);
    m_comboSceneType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
}

void CLS_CalibrateMode::UpdatePageUI(int sceneId)
{
    if (m_iLogonID == -1 || m_iChannelNo == -1)
    {
        AddLog(LOG_TYPE_MSG, "", "CLS_CalibrateMode::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
        return;
    }

    CalibrateMode tInfo = {0};
    tInfo.iSize =  sizeof(tInfo);
    tInfo.iSceneAreaNo = sceneId;
    tInfo.iLinkDevNo = m_comboDeviceID.GetCurSel();

    int iReturn = -1;
    int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_CALIBRATE_MODE, m_iChannelNo, &tInfo, sizeof(CalibrateMode), &iReturn);
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_CALIBRATE_MODE fail!");
    }
    else
    {
        m_comboScene.SetCurSel(tInfo.iSceneAreaNo);
        m_comboDeviceID.SetCurSel(tInfo.iLinkDevNo);
        m_comboCalibrateMode.SetCurSel(tInfo.iCalibrateMode);
        m_comboState.SetCurSel(tInfo.iState);
        m_sldPan.SetPos(tInfo.iPan);
        m_sldTilt.SetPos(tInfo.iTilt);
        m_sldZoom.SetPos(tInfo.iZoom);
        m_comboSceneType.SetCurSel(tInfo.iSceneType);
    }

    return;
} 

void CLS_CalibrateMode::OnBnClickedButtonSet3()
{
    int iStartX = GetDlgItemInt(IDC_EDIT_StartX);
    int iStartY = GetDlgItemInt(IDC_EDIT_StartY);
    int iEndX = GetDlgItemInt(IDC_EDIT_ENDX);
    int iEndY = GetDlgItemInt(IDC_EDIT_ENDY);

    if (iStartX >= 0 && iStartY >= 0 && iEndX >=0 && iEndY >=0 && iStartX <= iStartY  && iEndX <= iEndY)
    {

    }
    else
    {
        MessageBox("Param Error!","Info",0);
        return;
    }

    CalibrateCheck tInfo = {0};
    tInfo.iSize =  sizeof(tInfo);
    tInfo.iSceneId = m_comboScene.GetCurSel();
    tInfo.iStartPointX = iStartX;
    tInfo.iStartPointY = iStartY;
    tInfo.iEndPointX = iEndX;
    tInfo.iEndPointY = iEndY;

    int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_CALIBRATE_CHECK, m_iChannelNo, &tInfo, sizeof(CalibrateCheck));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand]COMMAND_ID_CALIBRATE_CHECK fail!");
    }

}

void CLS_CalibrateMode::OnCbnSelchangeComboSceneid()
{
    UpdatePageUI(m_comboScene.GetCurSel());
}

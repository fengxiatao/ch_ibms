// CLS_SceneFocusArea.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_SceneFocusArea.h"
#include <Shlwapi.h>

#define MAX_TEMPLATE_TYPE           3
#define MAX_CALIBRATE_SCENE_NUM     32
#define MAX_FOCUS_AREA_NUM          16
#define REGION_MAX_MASK_AREA_POINTS_NUM     8
// CLS_SceneFocusArea dialog

IMPLEMENT_DYNAMIC(CLS_SceneFocusArea, CDialog)

CLS_SceneFocusArea::CLS_SceneFocusArea(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_SceneFocusArea::IDD, pParent)
{
    m_iLogonID = -1;
    m_iChannelNo = -1;
    m_pDlgVideoView = NULL;
    m_stAreasNum = 0;
    memset(&m_stAreas, 0, sizeof(m_stAreas));
}

CLS_SceneFocusArea::~CLS_SceneFocusArea()
{
    if (m_pDlgVideoView)
    {
        m_pDlgVideoView->DestroyWindow();
        delete m_pDlgVideoView;
        m_pDlgVideoView = NULL;
    }

}

void CLS_SceneFocusArea::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_EDIT_Points, m_edtDarwPoint);
    DDX_Control(pDX, IDC_LIST1, m_listPoints);
    DDX_Control(pDX, IDC_COMBO_SCENEID, m_comboScene);
    DDX_Control(pDX, IDC_COMBO_FOCUSAREANUM, m_comboFocusAreaNum);
    DDX_Control(pDX, IDC_COMBO_ALGTYPE, m_comboAlgType);
    DDX_Control(pDX, IDC_CHECK_ENABLE, m_chkEnable);
}


BEGIN_MESSAGE_MAP(CLS_SceneFocusArea, CLS_BasePage)
    ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_SceneFocusArea::OnBnClickedButtonSet)
    ON_BN_CLICKED(IDC_BUTTON_Draw, &CLS_SceneFocusArea::OnBnClickedButtonDraw)
    ON_BN_CLICKED(IDC_BUTTON_CLEARLIST, &CLS_SceneFocusArea::OnBnClickedButtonClearlist)
END_MESSAGE_MAP()

void CLS_SceneFocusArea::OnBnClickedButtonDraw()
{
    // TODO: Add your control notification handler code here
    if (NULL == m_pDlgVideoView)
    {
        m_pDlgVideoView = new CLS_VideoViewForDraw();
        if (NULL == m_pDlgVideoView)
        {
            return;
        }
    }
/* The following code can take out the corresponding parameters from the draw dialog box */
    m_pDlgVideoView->Init(m_iLogonID, m_iChannelNo, m_iStreamNO);
    m_pDlgVideoView->SetDrawType(DrawType_perimeter, REGION_MAX_MASK_AREA_POINTS_NUM);
    int iPointNum = 0;
    int iDirection = 0;
    char cPointBuf[MAX_POINTBUF_LEN] = {0};
    /***************************************************************************/
     //Solve the problem that there is no line when the video box is displayed 20161129
    RECT tTemp = {0};
    tTemp.left = m_tCurrentInterestRect.iLeft;
    tTemp.top = m_tCurrentInterestRect.iTop;
    tTemp.right = m_tCurrentInterestRect.iRight;
    tTemp.bottom = m_tCurrentInterestRect.iBottom;
    m_pDlgVideoView->InitCrowdRect(tTemp);
    /***************************************************************************/
    int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, true);
    if (-1 == iSetRet)
    {
        goto EXIT_FUNC;
    }

    if (IDOK == m_pDlgVideoView->DoModal())
    {
        m_edtDarwPoint.SetWindowText(cPointBuf);
        addAreaToList(cPointBuf);
        m_pDlgVideoView->GetPointCoordirate(&m_iLeft, &m_iTop, &m_iRight, &m_iBottom);
    }
    else
    {
        // TODO: Nothing
    }

EXIT_FUNC:
    delete m_pDlgVideoView;
    m_pDlgVideoView = NULL;
}

void CLS_SceneFocusArea::addAreaToList(char* cPointBuf)
{
    CString strBuf(cPointBuf);
    strBuf.Replace(',', ':');
    strBuf.Replace(')', ',');
    strBuf.TrimRight(',');
    strBuf.Remove('(');

    int iAreaCount = m_comboFocusAreaNum.GetCurSel();
    int iListCount = m_listPoints.GetCount();
    if (iListCount >= iAreaCount)
    {
         MessageBox("Number of regions exceeded", "Prompt", 0);
        return;
    }

    //sort

    m_stAreasNum++;
    int iStart = 0;
    int iMIndex = strBuf.Find(':', iStart);
    //123:345,123:432,3423:423423
    while (iMIndex > 0)
    {
        int iPointIndex = m_stAreas[m_stAreasNum - 1].iPointNum++;

        int iX = StrToInt(strBuf.Mid(iStart, iMIndex - iStart));
        m_stAreas[m_stAreasNum - 1].stPoints[iPointIndex].iX = iX;
        iStart = iMIndex + 1;
        int iDIndex = strBuf.Find(',', iStart);
        if (iDIndex < 0)
        {
            m_stAreas[m_stAreasNum - 1].stPoints[iPointIndex].iY = StrToInt(strBuf.Mid(iStart));
            iMIndex = -1;
        }
        else
        {
            m_stAreas[m_stAreasNum - 1].stPoints[iPointIndex].iY = StrToInt(strBuf.Mid(iStart, iDIndex - iStart));
            iStart = iDIndex + 1;
            iMIndex = strBuf.Find(':', iStart);
        }

    }
    
    m_listPoints.AddString(strBuf);

}

// CLS_SceneFocusArea message handler
void CLS_SceneFocusArea::OnBnClickedButtonSet()
{
    if (m_iLogonID == -1 || m_iChannelNo == -1)
    {
        AddLog(LOG_TYPE_MSG, "", "CLS_SceneFocusArea::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
        return;
    }

    int iAreaCount = m_comboFocusAreaNum.GetCurSel();
    int iListCount = m_listPoints.GetCount();
    if (iAreaCount != iListCount)
    {
         MessageBox("Make sure the number of areas is the same as the number in the list","Prompt",0);
        return;
    }

    VcaFocusArea tInfo = {0};
    tInfo.iSize =  sizeof(tInfo);

    tInfo.iSceneId = m_comboScene.GetCurSel();
    tInfo.iAlgType = m_comboAlgType.GetCurSel();
    tInfo.iEnable = (m_chkEnable.GetCheck() == BST_CHECKED) ? 1 : 0;
    tInfo.iAreaNum = m_comboFocusAreaNum.GetCurSel();
    memcpy(tInfo.stAreas, m_stAreas, sizeof(m_stAreas));

    int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_FOCUSAREA, m_iChannelNo, &tInfo, sizeof(VcaFocusArea));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCASetConfig]VCA_CMD_FOCUSAREA fail!");
    }
    else
    {
        AddLog(LOG_TYPE_SUCC, "", "[NetClient_VCASetConfig]%d,%d,%d", tInfo.iSceneId, tInfo.iAlgType, tInfo.iAreaNum);

    }
}

void CLS_SceneFocusArea::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
    //PARA_CALIBRATE_MODE
    int* piResult = (int*)_pPara; 
    switch(_iParaType)
    {
    case PARA_VCA_FOCUSAREA:
        {
            VcaFocusArea* pResult = (VcaFocusArea*)_pPara;
            UpdatePageUI();
        }
        break;
    default:
        break;
    }
}

BOOL CLS_SceneFocusArea::OnInitDialog()
{
    CLS_BasePage::OnInitDialog();

    InitPageUI();
    UpdatePageUI();
    return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_SceneFocusArea::OnChannelChanged( int _iLogonID,int _iChannelNo, int _iStreamNo)
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

void CLS_SceneFocusArea::OnLanguageChanged(int _iLanguage)
{
    UpdatePageUI();
}

void CLS_SceneFocusArea::InitPageUI()
{
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

    m_comboAlgType.ResetContent();
    m_comboAlgType.SetItemData(m_comboAlgType.AddString(GetTextByLan("保留", "Reserved")), 0);
    m_comboAlgType.SetItemData(m_comboAlgType.AddString(GetTextByLan("人脸", "Face")), 1);
    m_comboAlgType.SetItemData(m_comboAlgType.AddString(GetTextByLan("结构化", "Structure")), 2);

    m_comboScene.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));


    iCurSel = m_comboFocusAreaNum.GetCurSel();
    m_comboFocusAreaNum.ResetContent();
    for (int idx = 0; idx <= MAX_FOCUS_AREA_NUM; idx++)
    {
        m_comboFocusAreaNum.SetItemData(m_comboFocusAreaNum.AddString(IntToCString(idx)), idx);
    }
    m_comboFocusAreaNum.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
    OnBnClickedButtonClearlist();
}

void CLS_SceneFocusArea::UpdatePageUI()
{
    if (m_iLogonID == -1 || m_iChannelNo == -1)
    {
        AddLog(LOG_TYPE_MSG, "", "CLS_SceneFocusArea::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
        return;
    }
    OnBnClickedButtonClearlist();
    VcaFocusArea tInfo = {0};
    tInfo.iSize =  sizeof(tInfo);

    int iReturn = -1;
    int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_FOCUSAREA, m_iChannelNo, &tInfo, sizeof(VcaFocusArea));
    if (iRet < 0)
    {
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCAGetConfig]NET_CLIENT_CALIBRATE_MODE fail!");
    }
    else
    {
        m_comboScene.SetCurSel(tInfo.iSceneId);
        m_comboFocusAreaNum.SetCurSel(tInfo.iAreaNum);
        m_comboAlgType.SetCurSel(tInfo.iAlgType);

        m_chkEnable.SetCheck(((1 == tInfo.iEnable) ? BST_CHECKED : BST_UNCHECKED));
        m_stAreasNum = tInfo.iAreaNum;
        memcpy(&m_stAreas, tInfo.stAreas, sizeof(m_stAreas));

        for (int idx = 0; idx < tInfo.iAreaNum; idx++)
        {

            char cPointBuf[MAX_POINTBUF_LEN] = {0};
            char cPointItem[LEN_64] = {0};

            for (int idy = 0; idy < tInfo.stAreas[idx].iPointNum; idy++)
            {
                sprintf_s(cPointItem, "%d:%d,", tInfo.stAreas[idx].stPoints[idy].iX, tInfo.stAreas[idx].stPoints[idy].iY);
                strcat_s(cPointBuf, cPointItem);
            }
            int iLen = strlen(cPointBuf);
            cPointBuf[iLen-1] = '\0';
            m_listPoints.AddString(cPointBuf);
        }
        AddLog(LOG_TYPE_FAIL, "", "[NetClient_VCAGetConfig]%d,%d,%d", tInfo.iSceneId, tInfo.iAlgType, tInfo.iAreaNum);
    }

    return;
} 


void CLS_SceneFocusArea::OnBnClickedButtonClearlist()
{
    m_stAreasNum = 0;
    memset(&m_stAreas, 0, sizeof(m_stAreas));
    m_listPoints.ResetContent();
     // TODO: Add control notification handler code here
}

//Solve the bug that the sub-interface cannot connect to the video 20161229
void CLS_SceneFocusArea::OnMainNotify( int _iLogonID,int _wParam, void* _lParam, void*_iUserData )
{
    if (_iLogonID != m_iLogonID)
    {
        return;
    }

    if (NULL != m_pDlgVideoView)
    {
        m_pDlgVideoView->OnMainNotify(_iLogonID, _wParam, _lParam, _iUserData);
    }
}

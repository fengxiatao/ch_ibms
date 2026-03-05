
#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_VcaDetectArea.h"


// CLS_VcaDetectArea dialog

IMPLEMENT_DYNAMIC(CLS_VcaDetectArea, CDialog)

CLS_VcaDetectArea::CLS_VcaDetectArea(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VcaDetectArea::IDD, pParent)
{

}

CLS_VcaDetectArea::~CLS_VcaDetectArea()
{
}

void CLS_VcaDetectArea::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_MAKS_AREA_SCENE_ID, m_cboScenceID);
	DDX_Control(pDX, IDC_COMBO_AREA_TYPE, m_cboAreaType);
	DDX_Control(pDX, IDC_EDIT_DETECT_LOOP, m_edtDetectLoop);
	DDX_Control(pDX, IDC_EDIT_DETECT_TIME, m_edtDetectTime);
	DDX_Control(pDX, IDC_COMBO_AREA_NUM, m_cboAreaNum);	
	DDX_Control(pDX, IDC_EDIT_DETECT_AREA_REGION_POINTS, m_edtAreaPoint);

}


BEGIN_MESSAGE_MAP(CLS_VcaDetectArea, CDialog)
	
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_STARTREPORT, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStartreport)
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_DETECT_PARAM_SET, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDetectParamSet)
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_PARA_GET, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaParaGet)
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_REGION_DRAW, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaRegionDraw)
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_SET, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaSet)
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_CALL, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaCall)
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_STOPREPORT, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStopreport)
	ON_CBN_SELCHANGE(IDC_COMBO_AREA_NUM, &CLS_VcaDetectArea::OnCbnSelchangeComboAreaNum)
	ON_BN_CLICKED(IDC_BTN_DETECT_AREA_DELL, &CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDell)
END_MESSAGE_MAP()


void CLS_VcaDetectArea::UI_UpdateText()
{
	m_cboAreaType.ResetContent();
	m_cboAreaType.SetItemData(m_cboAreaType.AddString(GetTextByLan(_T("人脸检测"), _T("Face Detect"))), 0);
	m_cboAreaType.SetItemData(m_cboAreaType.AddString(GetTextByLan(_T("流速检测"), _T("Velocity Detect"))), 1);
	m_cboAreaType.SetItemData(m_cboAreaType.AddString(GetTextByLan(_T("水位水尺检测"), _T("WaterLevelAndDraftDetect"))), 2);
	m_cboAreaType.SetCurSel(0);

	m_cboScenceID.ResetContent();
	for(int i = 0;i<SCENE_NUM; i++){
		CString strSceneID;
		strSceneID.Format("%d",i);
		m_cboScenceID.AddString(strSceneID);
	}
	m_cboScenceID.SetCurSel(0);
	
	m_cboAreaNum.ResetContent();
	for(int i = 0;i<AREA_NUM; i++){
		CString strAreaNo;
		strAreaNo.Format("%d",i);
		m_cboAreaNum.AddString(strAreaNo);
	}
	m_cboAreaNum.SetCurSel(0);
	memset(&m_tResult,0,sizeof(m_tResult));
	m_iFlag = -1;

	SetDlgItemText(IDC_VCA_DETECTAREA_SENCEID, GetTextByLan(_T("场景ID"), _T("SceneID")));
	SetDlgItemText(IDC_DETECTAREA_AREATYPE, GetTextByLan(_T("区域类型"), _T("AreaType")));
	SetDlgItemText(IDC_DETECTPARAM, GetTextByLan(_T("检测参数"), _T("Detect Param")));
	SetDlgItemText(IDC_DETECTAREA_DETECTLOOP, GetTextByLan(_T("循环次数"), _T("Loop Time")));
	SetDlgItemText(IDC_DETECTAREA_TIME, GetTextByLan(_T("时间"), _T("Detect Time")));
	SetDlgItemText(IDC_DETECTAREA, GetTextByLan(_T("检测区域"), _T("Detect Area")));
}

void CLS_VcaDetectArea::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_VcaDetectArea::UI_UpdateDialog()
{
	SetDlgItemText(IDC_VCA_DETECTAREA_SENCEID, GetTextByLan(_T("场景ID"), _T("SceneID")));
	SetDlgItemText(IDC_DETECTAREA_AREATYPE, GetTextByLan(_T("区域类型"), _T("AreaType")));
	SetDlgItemText(IDC_DETECTPARAM, GetTextByLan(_T("检测参数"), _T("Detect Param")));
	SetDlgItemText(IDC_DETECTAREA_DETECTLOOP, GetTextByLan(_T("循环次数"), _T("Loop Time")));
	SetDlgItemText(IDC_DETECTAREA_TIME, GetTextByLan(_T("停留时间"), _T("Detect Time")));
	SetDlgItemText(IDC_DETECTAREA, GetTextByLan(_T("检测区域"), _T("Detect Area")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_STOPREPORT, GetTextByLan(_T("停止上报"), _T("Stop Report")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_STARTREPORT, GetTextByLan(_T("开始上报"), _T("Start Report")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_DETECT_PARAM_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_PARA_GET, GetTextByLan(_T("获取"), _T("Get")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_REGION_DRAW, GetTextByLan(_T("绘制"), _T("Paint")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_CALL, GetTextByLan(_T("调用"), _T("Call")));
	SetDlgItemText(IDC_BTN_DETECT_AREA_DELL, GetTextByLan(_T("删除"), _T("Delete")));
	SetDlgItemText(IDC_STATIC_AREA_NO, GetTextByLan(_T("区域编号"), _T("Area NO")));
	SetDlgItemText(IDC_STATIC_POINT_GROUP, GetTextByLan(_T("点集"), _T("Point Group")));
	
}

void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStartreport()
{
	DetectArea tInfo = {0};
	tInfo.iSceneID = m_cboScenceID.GetCurSel();
	tInfo.iType = m_cboAreaType.GetCurSel();
	tInfo.iChannelNO = m_iChannelNO;
	tInfo.iSize = (int)sizeof(DetectArea);
	tInfo.iStatus = 1;
	int iRet = NetClient_SendCommand(m_iLogonID,COMMAND_ID_DETECT_AREA,m_iChannelNO,&tInfo,sizeof(DetectArea));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStartreport] NetClient_SetDevConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStartreport] NetClient_SetDevConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
	m_iFlag =-1;
}



void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDetectParamSet()
{
	// TODO: Add your control notification handler code here
	CString strLoop = "";
	m_edtDetectLoop.GetWindowText(strLoop);
	CString strTime = "";
	m_edtDetectTime.GetWindowText(strTime);
	DetectPara tInfo ={0};
	tInfo.iDetectLoop = _ttoi(strLoop);
	tInfo.iDetectTime = _ttoi(strTime);
	tInfo.iChannelNo = m_iChannelNO;
	tInfo.iSize = (int)sizeof(DetectPara);
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_DETECTPARA,m_iChannelNO,&tInfo,sizeof(DetectPara));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDetectParamSet] NetClient_SetDevConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDetectParamSet] NetClient_SetDevConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
	
}

void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaParaGet()
{
	// TODO: Add your control notification handler code here
	DetectPara tInfo = {0};
	tInfo.iChannelNo = m_iChannelNO;
	tInfo.iSceneID = m_cboScenceID.GetCurSel();
	tInfo.iSize = (int)sizeof(DetectPara);
	int iReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_DETECTPARA,m_iChannelNO,&tInfo,sizeof(DetectPara),&iReturn);
	if(RET_SUCCESS == iRet)
	{
		CString strLoop = "";
		strLoop.Format("%d",tInfo.iDetectLoop);
		m_edtDetectLoop.SetWindowText(strLoop);
		CString strTime = "";
		strTime.Format("%d",tInfo.iDetectTime);
		m_edtDetectTime.SetWindowText(strTime);	
	}	
}


void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaRegionDraw()
{
	// TODO: Add your control notification handler code here
	int iPointCount = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	GetInfoOnDrawVideo(&iPointCount, cPointBuf, NULL, DrawType_perimeter);
	m_iReferCount = iPointCount;
	POINT tPoints[SDK_MAX_MASKAREA_NUM] = {0}; 
	GetPointsFromString(cPointBuf, m_iReferCount, tPoints);
	CString cstrMsg;
	CString cstrTmp;
	for (int i = 0; i < m_iReferCount && i < MAX_REFERPOINT_NUM; ++i)
	{
		cstrTmp.Format("(%d,%d)", tPoints[i].x, tPoints[i].y);
		cstrMsg += cstrTmp;
	}
	m_edtAreaPoint.SetWindowText(cstrMsg);
}

void CLS_VcaDetectArea::GetInfoOnDrawVideo(int* _piPointCount, char* _pcPointsBuf, RECT* _ptRect, int _iDrawType)
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
	m_pDlgVideoView->SetDrawType(_iDrawType);
	int iDirection = 0;
	if (NULL != _piPointCount && NULL != _pcPointsBuf)
	{
		m_pDlgVideoView->SetPointRegionParam(_pcPointsBuf, _piPointCount, &iDirection, TRUE);
	}

	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (NULL != _ptRect)
		{
			m_pDlgVideoView->GetPointCoordirate((int*)&_ptRect->left, (int*)&_ptRect->top, (int*)&_ptRect->right, (int*)&_ptRect->bottom);
		}
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VcaDetectArea::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	if (WCM_DETECT_AREA == _wParam)
	{
		DetectAreaResult* pRet = (DetectAreaResult*)_iLParam;
		if (pRet->iAreaNum >= 0 && pRet->iAreaNum < AREA_NUM)
		{
			memcpy(&m_tResult[pRet->iAreaNum], _iLParam, min(pRet->iSize, sizeof(DetectAreaResult)));
		}	
	} 
	else if (WCM_DETECT_AREA_FINISH == _wParam)
	{
		//renew
		if(-1 == m_iFlag)
		{
			MessageBox(GetTextByLan(_T("上报完成"), _T("Report Finished")));
			m_iFlag = 0;
		}
		
	}
}

void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaSet()
{
	//  NET_CLIENT_DETECTAREA,NetClient_SetDevConfig,SetDetectArea
	SetDetectArea tInfo ={0};
	tInfo.iSize = (int)sizeof(SetDetectArea);
	tInfo.iAreaNum = m_cboAreaNum.GetCurSel();
	tInfo.iSceneID = m_cboScenceID.GetCurSel();
	tInfo.iChannelNO = m_iChannelNO;
	tInfo.iOperationType = 1;//Set the detection area
	CString strPoint = "";
	m_edtAreaPoint.GetWindowText(strPoint);
	int i = 0;
	while (strPoint.GetLength() != 0)
	{
		CString strp = "";
		strp = strPoint.Mid(strPoint.Find("("), strPoint.Find(")") + 1);
		strPoint = strPoint.Mid(strp.GetLength());
		sscanf_s((LPSTR)(LPCTSTR)strp, "(%d,%d)"
			, &tInfo.tPoints.stPoints[i].iX, &tInfo.tPoints.stPoints[i].iY
			);
		i++;
	}
	tInfo.tPoints.iPointNum = i;
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_DETECTAREA,m_iChannelNO,&tInfo,sizeof(SetDetectArea));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaSet] NetClient_SetDevConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaSet] NetClient_SetDevConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
}


void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaCall()
{
	SetDetectArea tInfo ={0};
	tInfo.iSize = (int)sizeof(SetDetectArea);
	tInfo.iAreaNum = m_cboAreaNum.GetCurSel();
	tInfo.iSceneID = m_cboScenceID.GetCurSel();
	tInfo.iChannelNO = m_iChannelNO;
	tInfo.iOperationType = 2;//set callback
	CString strPoint = "";
	m_edtAreaPoint.GetWindowText(strPoint);
	int i = 0;
	while (strPoint.GetLength() != 0)
	{
		CString strp = "";
		strp = strPoint.Mid(strPoint.Find("("), strPoint.Find(")") + 1);
		strPoint = strPoint.Mid(strp.GetLength());
		i++;
	}
	tInfo.tPoints.iPointNum = i;
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_DETECTAREA,m_iChannelNO,&tInfo,sizeof(SetDetectArea));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaCall] NetClient_SetDevConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaCall] NetClient_SetDevConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
}


void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStopreport()
{
	DetectArea tInfo = {0};
	tInfo.iSceneID = m_cboScenceID.GetCurSel();
	tInfo.iType = m_cboAreaType.GetCurSel();
	tInfo.iChannelNO = m_iChannelNO;
	tInfo.iSize = sizeof(DetectArea);
	tInfo.iStatus = 2;
	int iRet = NetClient_SendCommand(m_iLogonID,COMMAND_ID_DETECT_AREA,m_iChannelNO,&tInfo,sizeof(DetectArea));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStopreport] NetClient_SetDevConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaStopreport] NetClient_SetDevConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
}

void CLS_VcaDetectArea::OnCbnSelchangeComboAreaNum()
{
	// TODO: Add your control notification handler code here
	int iIndex = m_cboAreaNum.GetCurSel();
	CString strTotal = "";
	for (int i = 0; i < m_tResult[iIndex].tPoint.iPointNum; i++)
	{
		CString strPoint = "";
		strPoint.Format("(%d,%d)", m_tResult[iIndex].tPoint.stPoints[i].iX, m_tResult[iIndex].tPoint.stPoints[i].iY);
		strTotal += strPoint;
	}
	
	m_edtAreaPoint.SetWindowText(strTotal);
}

void CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDell()
{
	SetDetectArea tInfo ={0};
	tInfo.iSize = (int)sizeof(SetDetectArea);
	tInfo.iAreaNum = m_cboAreaNum.GetCurSel();
	tInfo.iSceneID = m_cboScenceID.GetCurSel();
	tInfo.iChannelNO = m_iChannelNO;
	tInfo.iOperationType = 3;//delete
	CString strPoint = "";
	m_edtAreaPoint.GetWindowText(strPoint);
	int i = 0;
	while (strPoint.GetLength() != 0)
	{
		CString strp = "";
		strp = strPoint.Mid(strPoint.Find("("), strPoint.Find(")") + 1);
		strPoint = strPoint.Mid(strp.GetLength());
		i++;
	}
	tInfo.tPoints.iPointNum = i;
	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_DETECTAREA,m_iChannelNO,&tInfo,sizeof(SetDetectArea));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDell] NetClient_SetDevConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_VcaDetectArea::OnBnClickedBtnDetectAreaDell] NetClient_SetDevConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
}


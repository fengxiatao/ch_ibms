// VCAEVENT_NavigationDetect.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_NavigationDetect.h"

// CLS_VCAEVENT_NavigationDetect dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_NavigationDetect, CDialog)

CLS_VCAEVENT_NavigationDetect::CLS_VCAEVENT_NavigationDetect(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_NavigationDetect::IDD, pParent)
{
	memset(&m_tGpsDangerArea, 0, sizeof(m_tGpsDangerArea));
}

CLS_VCAEVENT_NavigationDetect::~CLS_VCAEVENT_NavigationDetect()
{
}

void CLS_VCAEVENT_NavigationDetect::DoDataExchange(CDataExchange* pDX)
{
	CLS_VCAEventBasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_SLIDER_NAVIGATION_SIMILAR, m_sldSimilar);
	DDX_Control(pDX, IDC_SLIDER_DANGERAREA_DEGREE, m_sldDegree);
	DDX_Control(pDX, IDC_SLIDER_DANGERAREA_DEGREE2, m_sldDegree2);
	DDX_Control(pDX, IDC_SLIDER_DANGERAREA_MINUTE, m_sldMinute);
	DDX_Control(pDX, IDC_SLIDER_DANGERAREA_MINUTE2, m_sldMinute2);
	DDX_Control(pDX, IDC_SLIDER_DANGERAREA_SECOND, m_sldSecond);
	DDX_Control(pDX, IDC_SLIDER_DANGERAREA_SECOND2, m_sldSecond2);
	DDX_Control(pDX, IDC_SLIDER_3DLOCATE_TIME, m_sldTime);
	DDX_Control(pDX, IDC_SLIDER_3DLOCATE_MULTIPLE, m_sldMultiple);
	DDX_Control(pDX, IDC_COMBO_DANGERAREA_ID, m_cboAreaNum);
	DDX_Control(pDX, IDC_COMBO_DANGERAREA_LONGITUDE, m_cboLongitude);
	DDX_Control(pDX, IDC_COMBO_DANGERAREA_LATITUDE, m_cboLatitude);
	DDX_Control(pDX, IDC_COMBO_DANGERAREA_POINT_ID, m_cboPointID);
	DDX_Control(pDX, IDC_CHECK_NAVIGATION_DETECT, m_chkNavigation);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_NavigationDetect, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_NAVIGATION_SET, &CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonNavigationSet)
	ON_BN_CLICKED(IDC_BUTTON_DANGERAREA_SET, &CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonDangerareaSet)
	ON_BN_CLICKED(IDC_BUTTON_3DLOCATE_SET, &CLS_VCAEVENT_NavigationDetect::OnBnClickedButton3dlocateSet)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_NAVIGATION_SIMILAR, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderNavigationSimilar)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_DANGERAREA_DEGREE, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaDegree)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_DANGERAREA_MINUTE, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaMinute)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_DANGERAREA_SECOND, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaSecond)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_DANGERAREA_DEGREE2, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaDegree2)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_DANGERAREA_MINUTE2, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaMinute2)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_DANGERAREA_SECOND2, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaSecond2)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_3DLOCATE_TIME, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSlider3dlocateTime)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_3DLOCATE_MULTIPLE, &CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSlider3dlocateMultiple)
	ON_BN_CLICKED(IDC_BUTTON_DANGERAREA_SAVE, &CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonDangerareaSave)
	ON_BN_CLICKED(IDC_BUTTON_NAVIGATION_DRAW, &CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonNavigationDraw)
	ON_BN_CLICKED(IDC_CHECK_NAVIGATION_DETECT, &CLS_VCAEVENT_NavigationDetect::OnBnClickedCheckNavigationDetect)
	ON_CBN_SELCHANGE(IDC_COMBO_DANGERAREA_ID, &CLS_VCAEVENT_NavigationDetect::OnCbnSelchangeComboDangerareaId)
	ON_CBN_SELCHANGE(IDC_COMBO_DANGERAREA_POINT_ID, &CLS_VCAEVENT_NavigationDetect::OnCbnSelchangeComboDangerareaPointId)
	ON_BN_CLICKED(IDC_BUTTON_DANGERAREA_CLEAR, &CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonDangerareaClear)
END_MESSAGE_MAP()


// CLS_VCAEVENT_NavigationDetect message handler

BOOL CLS_VCAEVENT_NavigationDetect::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();
	
	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VCAEVENT_NavigationDetect::UpdateUIText()
{
	SetDlgItemTextEx(IDC_BUTTON_NAVIGATION_SET, IDS_SET);
	SetDlgItemTextEx(IDC_BUTTON_DANGERAREA_SET, IDS_SET);
	SetDlgItemTextEx(IDC_BUTTON_3DLOCATE_SET, IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_DANGERAREA_DEGREE, IDS_DEGREE);
	SetDlgItemTextEx(IDC_STATIC_DANGERAREA_DEGREE2, IDS_DEGREE);
	SetDlgItemTextEx(IDC_STATIC_DANGERAREA_MINUTE, IDS_MINUTE);
	SetDlgItemTextEx(IDC_STATIC_DANGERAREA_MINUTE2, IDS_MINUTE);
	SetDlgItemTextEx(IDC_STATIC_DANGERAREA_SECOND, IDS_SECOND);
	SetDlgItemTextEx(IDC_STATIC_DANGERAREA_SECOND2, IDS_SECOND);

	SetDlgItemText(IDC_CHECK_NAVIGATION_DETECT, GetTextByLan(_T("航标船检测"), _T("Navigation ship detect")));
	SetDlgItemText(IDC_NAVIGATION_DANGERAREA, GetTextByLan(_T("危险区域报警支持的区域个数"), _T("Number of danger area alarm support areas")));
	SetDlgItemText(IDC_STATIC_NAVIGATION_PARAM, GetTextByLan(_T("航标船检测参数"), _T("Testing parameters of the marking vessel")));
	SetDlgItemText(IDC_STATIC_NAVIGATION_POINTNUM, GetTextByLan(_T("多边形顶点个数"), _T("Number of polygon vertices")));
	SetDlgItemText(IDC_STATIC_NAVIGATION_SIMILAR, GetTextByLan(_T("灵敏度"), _T("The sensitivity")));
	SetDlgItemText(IDC_STATIC_NAVIGATION_AREA, GetTextByLan(_T("多边形区域坐标"), _T("Polygonal regional coordinates")));
	SetDlgItemText(IDC_BUTTON_NAVIGATION_DRAW, GetTextByLan(_T("绘制"), _T("draw")));
	SetDlgItemText(IDC_STATIC_DANGERAREA_PARA, GetTextByLan(_T("GPS危险区域"), _T("GPS danger zone")));
	SetDlgItemText(IDC_STATIC_DANGERAREA_ID, GetTextByLan(_T("区域编号"), _T("Section number")));
	SetDlgItemText(IDC_STATIC_DANGERAREA_POINT_ID, GetTextByLan(_T("区域顶点编号"), _T("Region vertex number")));
	SetDlgItemText(IDC_STATIC_DANGERAREA_POINTNUM, GetTextByLan(_T("区域顶点个数"), _T("Number of vertices in the region")));
	SetDlgItemText(IDC_STATIC_DANGERAREA_LONGITUDE, GetTextByLan(_T("经度"), _T("longitude")));
	SetDlgItemText(IDC_STATIC_DANGERAREA_LATITUDE, GetTextByLan(_T("纬度"), _T("latitude")));
	SetDlgItemText(IDC_BUTTON_DANGERAREA_SAVE, GetTextByLan(_T("保存顶点"), _T("Save the vertices")));
	SetDlgItemText(IDC_BUTTON_DANGERAREA_CLEAR, GetTextByLan(_T("清除顶点"), _T("Remove the vertices")));
	SetDlgItemText(IDC_STATIC_3DLOCATE_PARAM, GetTextByLan(_T("3D定位预测信息"), _T("3D positioning prediction information")));
	SetDlgItemText(IDC_STATIC_3DLOCATE_TIME, GetTextByLan(_T("预测时间(s)"), _T("Predict the time(s)")));
	SetDlgItemText(IDC_STATIC_3DLOCATE_MULTIPLE, GetTextByLan(_T("航标船区域放大倍数"), _T("magnification")));
																	
	m_sldSimilar.SetRange(0,100);
	m_sldDegree.SetRange(0,180);
	m_sldDegree2.SetRange(0,180);
	m_sldMinute.SetRange(0,59);
	m_sldMinute2.SetRange(0,59);
	m_sldSecond.SetRange(0,5999);
	m_sldSecond2.SetRange(0,5999);
	m_sldTime.SetRange(1,12);
	m_sldMultiple.SetRange(1,20);

	m_cboAreaNum.ResetContent();
	for(int i=0; i<MAX_GPS_AREA_NUM; i++)
	{
		m_cboAreaNum.InsertString(i, IntToCString(i+1));
	}
	m_cboAreaNum.SetCurSel(0);
	
	m_cboPointID.ResetContent();
	for(int i=0; i<MAX_GPS_POINT_NUM; i++)
	{
		m_cboPointID.InsertString(i, IntToCString(i+1));
	}
	m_cboPointID.SetCurSel(0);

	CString cstrLongitude[] = {GetTextByLan(_T("东"), _T("East")), GetTextByLan(_T("西"), _T("West"))};
	m_cboLongitude.ResetContent();
	for(int i=0; i<sizeof(cstrLongitude)/sizeof(CString); i++)
	{
		m_cboLongitude.InsertString(i, cstrLongitude[i]);
	}
	m_cboLongitude.SetCurSel(0);

	CString cstrLatitude[] = {GetTextByLan(_T("南"), _T("South")), GetTextByLan(_T("北"), _T("North"))};
	m_cboLatitude.ResetContent();
	for(int i=0; i<sizeof(cstrLatitude)/sizeof(CString); i++)
	{
		m_cboLatitude.InsertString(i, cstrLatitude[i]);
	}
	m_cboLatitude.SetCurSel(0);
	
	
}

void CLS_VCAEVENT_NavigationDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_NavigationDetect::OnLanguageChanged()
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAEVENT_NavigationDetect::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1 || m_iLogonID < 0)
	{
		return;
	}

	GetFuncAbility();
	GetAnyScene();
	GetNavigationParam();
	GetDangerAreaParam();
	Get3DLocateParam();
}
void CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonNavigationSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_NavigationDetect::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	int iRet = -1;

	NavigationShipDetection tInfo = {0};
	tInfo.iSize = sizeof(NavigationShipDetection);
	tInfo.iChanNo = m_iChannelNO;
	tInfo.iSceneId = m_iSceneID;
	tInfo.iSensitiv = m_sldSimilar.GetPos();
	tInfo.iPointNum = GetDlgItemInt(IDC_EDIT_NAVIGATION_POINTNUM);
	
	TPoint ptPolygon[MAX_POLYGON_POINT_NUM] = {0} ;
	CString cstPolygon = "";
	GetDlgItemText(IDC_EDIT_NAVIGATION_AREA, cstPolygon);
	GetPointsFromString(cstPolygon, tInfo.iPointNum, ptPolygon);
	for (int i = 0; i < tInfo.iPointNum && i < MAX_POLYGON_POINT_NUM; i++)
	{
		tInfo.tPoint[i] = ptPolygon[i];
	}

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_NAVIGATION_SHIP_DETECTION, m_iChannelNO, &tInfo, sizeof(NavigationShipDetection));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_NavigationDetect::NetClient_VCASetConfig[VCA_CMD_NAVIGATION_SHIP_DETECTION] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_NavigationDetect::NetClient_VCASetConfig[VCA_CMD_NAVIGATION_SHIP_DETECTION] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonDangerareaSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_NavigationDetect::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	int iRet = -1;

	GpsDangerArea tInfo = {0};
	memcpy(&tInfo, &m_tGpsDangerArea, sizeof(GpsDangerArea));
 	tInfo.iSize = sizeof(GpsDangerArea);
 	tInfo.iChanNo = m_iChannelNO;
	tInfo.iAreaId = m_cboAreaNum.GetCurSel();
	tInfo.iPointNum = GetDlgItemInt(IDC_EDIT_DANGERAREA_POINTNUM);

	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GPS_DANGERAREA, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_GPS_DANGERAREA fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_GPS_DANGERAREA success!");
	}
}

void CLS_VCAEVENT_NavigationDetect::OnBnClickedButton3dlocateSet()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_NavigationDetect::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	int iRet = -1;

	Locate3DPrediction tInfo = {0};
	tInfo.iSize = sizeof(Locate3DPrediction);
	tInfo.iChanNo = m_iChannelNO;
	tInfo.iTime = m_sldTime.GetPos();
	tInfo.iMultiple = m_sldMultiple.GetPos();

	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_3DLOCATE_PREDICTION, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_3DLOCATE_PREDICTION fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_3DLOCATE_PREDICTION success!");
	}
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderNavigationSimilar(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_NAVIGATION_SIMILAR_NUM, m_sldSimilar.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaDegree(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_DANGERAREA_DEGREE_NUM, m_sldDegree.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaMinute(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_DANGERAREA_MINUTE_NUM, m_sldMinute.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaSecond(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_DANGERAREA_SECOND_NUM, m_sldSecond.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaDegree2(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_DANGERAREA_DEGREE2_NUM, m_sldDegree2.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaMinute2(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_DANGERAREA_MINUTE2_NUM, m_sldMinute2.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSliderDangerareaSecond2(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_DANGERAREA_SECOND2_NUM, m_sldSecond2.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSlider3dlocateTime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_3DLOCATE_TIME_NUM, m_sldTime.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_NavigationDetect::OnNMCustomdrawSlider3dlocateMultiple(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_3DLOCATE_MULTIPLE_NUM, m_sldMultiple.GetPos());
	*pResult = 0;
}

int CLS_VCAEVENT_NavigationDetect::GetFuncAbility()
{


	int iByteReturn = -1;
	FuncAbilityLevel stFunAbilityLevel = {0};
	stFunAbilityLevel.iSize = sizeof(stFunAbilityLevel);
	stFunAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	stFunAbilityLevel.iSubFuncType  = 57;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stFunAbilityLevel, sizeof(stFunAbilityLevel), &iByteReturn);
	if (iRet < 0 || strlen(stFunAbilityLevel.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_NavigationDetect::GetFuncAbility] GetDevConfig NET_CLIENT_GET_FUNC_ABILITY Failed! m_iLogonID %d", m_iLogonID);
	}
	else
	{
		CString cstrTempParam = stFunAbilityLevel.cParam;
		int iFuncPara = _ttoi(cstrTempParam);
		if (1 == iFuncPara)//1 support 2 not support
		{
			m_chkNavigation.EnableWindow(TRUE);
		}
		else
		{
			m_chkNavigation.EnableWindow(FALSE);
		}
	}
	
	memset(&stFunAbilityLevel, 0, sizeof(FuncAbilityLevel));
	stFunAbilityLevel.iSize = sizeof(stFunAbilityLevel);
	stFunAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_ALARM;
	stFunAbilityLevel.iSubFuncType  = 18;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stFunAbilityLevel, sizeof(stFunAbilityLevel), &iByteReturn);
	if (iRet < 0 || strlen(stFunAbilityLevel.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_NavigationDetect::GetFuncAbility] GetDevConfig NET_CLIENT_GET_FUNC_ABILITY Failed! m_iLogonID %d", m_iLogonID);
	}
	else
	{
		CString cstrTempParam = stFunAbilityLevel.cParam;
		int iFuncPara = _ttoi(cstrTempParam);
		if (iFuncPara > 0)//The number of areas supported by the dangerous area alarm
		{
			SetDlgItemInt(IDC_EDIT_NAVIGATION_DANGERAREA, iFuncPara);

			m_cboAreaNum.ResetContent();
			for(int i=0; i<iFuncPara; i++)
			{
				m_cboAreaNum.InsertString(i, IntToCString(i+1));
			}
			m_cboAreaNum.SetCurSel(0);
		}
		else
		{
			SetDlgItemInt(IDC_EDIT_NAVIGATION_DANGERAREA, 0);
		}
	}
	
	return iRet;
}

int CLS_VCAEVENT_NavigationDetect::GetAnyScene()
{
	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = m_iSceneID;
	tParam.iDevType = 1;
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam), &iBytesReturned);
	if (iRet >= 0)
	{
		m_chkNavigation.SetCheck(tParam.iArithmeticEx & 0x04);
	}
	else
	{
		m_chkNavigation.SetCheck(0);
	}

	return iRet;
}

int CLS_VCAEVENT_NavigationDetect::GetNavigationParam()
{
	int iRet = -1;

	NavigationShipDetection tInfo = {0};
	tInfo.iSize = sizeof(NavigationShipDetection);
	tInfo.iChanNo = m_iChannelNO;
	tInfo.iSceneId = m_iSceneID;
	
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_NAVIGATION_SHIP_DETECTION, 0, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_NavigationDetect::NetClient_VCAGetConfig[VCA_CMD_NAVIGATION_SHIP_DETECTION] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{		
		m_sldSimilar.SetPos(tInfo.iSensitiv);
		SetDlgItemInt(IDC_STATIC_NAVIGATION_SIMILAR_NUM, m_sldSimilar.GetPos());
		
		SetDlgItemInt(IDC_EDIT_NAVIGATION_POINTNUM, tInfo.iPointNum);
		CString cstPolygonBuf;
		for(int i = 0; i < tInfo.iPointNum; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.tPoint[i].iX, tInfo.tPoint[i].iY);
		}
		SetDlgItemText(IDC_EDIT_NAVIGATION_AREA, cstPolygonBuf);
	}

	return iRet;
}

int CLS_VCAEVENT_NavigationDetect::GetDangerAreaParam()
{
	int iByteReturn = -1;

	GpsDangerArea tInfo = {0};
	tInfo.iSize = sizeof(GpsDangerArea);
	tInfo.iChanNo = 0;
	tInfo.iAreaId = m_cboAreaNum.GetCurSel();

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GPS_DANGERAREA, 0, &tInfo, sizeof(tInfo), &iByteReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_GPS_DANGERAREA fail!");
	}
	else
	{
		memset(&m_tGpsDangerArea, 0, sizeof(m_tGpsDangerArea));
		memcpy(&m_tGpsDangerArea, &tInfo, sizeof(GpsDangerArea));
		
		SetDlgItemInt(IDC_EDIT_DANGERAREA_POINTNUM, tInfo.iPointNum);
		m_cboPointID.SetCurSel(0);

		m_cboLongitude.SetCurSel(tInfo.tPointInfo[0].tLongitudeInfo.iDirection);
		m_sldDegree.SetPos(tInfo.tPointInfo[0].tLongitudeInfo.iDegree);
		m_sldMinute.SetPos(tInfo.tPointInfo[0].tLongitudeInfo.iMinute);
		m_sldSecond.SetPos(tInfo.tPointInfo[0].tLongitudeInfo.iSecond);

		m_cboLatitude.SetCurSel(tInfo.tPointInfo[0].tLatitudeInfo.iDirection);
		m_sldDegree2.SetPos(tInfo.tPointInfo[0].tLatitudeInfo.iDegree);
		m_sldMinute2.SetPos(tInfo.tPointInfo[0].tLatitudeInfo.iMinute);
		m_sldSecond2.SetPos(tInfo.tPointInfo[0].tLatitudeInfo.iSecond);
	}

	return iRet;
}

int CLS_VCAEVENT_NavigationDetect::Get3DLocateParam()
{
	int iByteReturn = -1;

	Locate3DPrediction tInfo = {0};
	tInfo.iSize = sizeof(Locate3DPrediction);
	tInfo.iChanNo = 0;
	
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_3DLOCATE_PREDICTION, 0, &tInfo, sizeof(tInfo), &iByteReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_3DLOCATE_PREDICTION fail!");
	}
	else
	{
		m_sldTime.SetPos(tInfo.iTime);
		m_sldMultiple.SetPos(tInfo.iMultiple);
	}

	return iRet;
}

void CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonDangerareaSave()
{
	int iSel = m_cboPointID.GetCurSel();
	if (0 <= iSel && iSel < MAX_GPS_POINT_NUM)
	{
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iDirection = m_cboLongitude.GetCurSel();
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iDegree = m_sldDegree.GetPos();
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iMinute = m_sldMinute.GetPos();
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iSecond = m_sldSecond.GetPos();

		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iDirection = m_cboLatitude.GetCurSel();
		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iDegree = m_sldDegree2.GetPos();
		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iMinute = m_sldMinute2.GetPos();
		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iSecond = m_sldSecond2.GetPos();
	}

	int iPointCount = 0;
	for (int i = 0; i < MAX_GPS_POINT_NUM; i++)
	{
		if (m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iDirection == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iDegree == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iMinute == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iSecond == 0 && 

			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iDirection == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iDegree == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iMinute == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iSecond == 0)
		{
			continue;
		}
		else
		{
			iPointCount++;
		}	
	}

	SetDlgItemInt(IDC_EDIT_DANGERAREA_POINTNUM, iPointCount);
}

void CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonDangerareaClear()
{
	int iSel = m_cboPointID.GetCurSel();
	if (0 <= iSel && iSel< MAX_GPS_POINT_NUM)
	{
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iDirection = 0;
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iDegree = 0;
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iMinute = 0;
		m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iSecond = 0;

		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iDirection = 0;
		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iDegree = 0;
		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iMinute = 0;
		m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iSecond = 0;

		m_cboLongitude.SetCurSel(0);
		m_sldDegree.SetPos(0);
		m_sldMinute.SetPos(0);
		m_sldSecond.SetPos(0);

		m_cboLatitude.SetCurSel(0);
		m_sldDegree2.SetPos(0);
		m_sldMinute2.SetPos(0);
		m_sldSecond2.SetPos(0);
	}

	int iPointCount = 0;
	for (int i = 0; i < MAX_GPS_POINT_NUM; i++)
	{
		if (m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iDirection == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iDegree == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iMinute == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iSecond == 0 && 

			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iDirection == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iDegree == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iMinute == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iSecond == 0)
		{
			continue;
		}
		else
		{
			iPointCount++;
		}	
	}

	SetDlgItemInt(IDC_EDIT_DANGERAREA_POINTNUM, iPointCount);
}

void CLS_VCAEVENT_NavigationDetect::GetPointsFromString(CString _strPoints, int _iPointNum, TPoint* _poPoint)
{
	int iLength = _strPoints.GetLength()+1;
	char* pcData = new char [iLength];
	memset(pcData, 0, iLength);
	memcpy(pcData, _strPoints.GetBuffer(), iLength-1);
	char* p1 = pcData;
	char* p2 = NULL;
	int iPointIndex = 0;
	for (int i = 0; i < iLength; ++i)
	{
		p2 = strstr(p1, ")");
		if (p2 == NULL)
			break;

		char cCell[200] = {0};
		int iX = 0, iY = 0;
		memcpy(cCell, p1, p2-p1+1);
		sscanf_s(cCell, "(%d,%d)", &iX, &iY);
		_poPoint[iPointIndex].iX = iX;
		_poPoint[iPointIndex].iY = iY;
		if (++iPointIndex == _iPointNum)
			break;

		if ((p1 = p2+1) >= pcData+iLength)
			break;
	}
	delete [] pcData;
	pcData = NULL;
}
void CLS_VCAEVENT_NavigationDetect::OnBnClickedButtonNavigationDraw()
{
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
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 1)
		{
			SetDlgItemText(IDC_EDIT_NAVIGATION_AREA, cPointBuf);
			SetDlgItemInt(IDC_EDIT_NAVIGATION_POINTNUM, iPointNum);
		}
		else
		{
			SetDlgItemText(IDC_EDIT_NAVIGATION_AREA, _T(""));
			SetDlgItemInt(IDC_EDIT_NAVIGATION_POINTNUM, 0);
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_NavigationDetect::OnBnClickedCheckNavigationDetect()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_NavigationDetect::OnBnClickedCheckNavigationDetect] Error  LogonID!");
		return;
	}

	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID = m_iSceneID;
	tParam.iDevType = 1;
	tParam.iArithmeticEx |= m_chkNavigation.GetCheck() << 2;

	int iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ANYSCENE fail!");

	}

	return;
}

void CLS_VCAEVENT_NavigationDetect::OnCbnSelchangeComboDangerareaId()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1 || m_iLogonID < 0)
	{
		return;
	}

	GetDangerAreaParam();
}

void CLS_VCAEVENT_NavigationDetect::OnCbnSelchangeComboDangerareaPointId()
{
	int iSel = m_cboPointID.GetCurSel();
	
	if (iSel < 0 || iSel >= MAX_GPS_POINT_NUM)
	{
		return;
	}

	m_cboLongitude.SetCurSel(m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iDirection);
	m_sldDegree.SetPos(m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iDegree);
	m_sldMinute.SetPos(m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iMinute);
	m_sldSecond.SetPos(m_tGpsDangerArea.tPointInfo[iSel].tLongitudeInfo.iSecond);

	m_cboLatitude.SetCurSel(m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iDirection);
	m_sldDegree2.SetPos(m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iDegree);
	m_sldMinute2.SetPos(m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iMinute);
	m_sldSecond2.SetPos(m_tGpsDangerArea.tPointInfo[iSel].tLatitudeInfo.iSecond);

	int iPointCount = 0;
	for (int i = 0; i < MAX_GPS_POINT_NUM; i++)
	{
		if (m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iDirection == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iDegree == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iMinute == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLongitudeInfo.iSecond == 0 && 

			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iDirection == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iDegree == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iMinute == 0 && 
			m_tGpsDangerArea.tPointInfo[i].tLatitudeInfo.iSecond == 0)
		{
			continue;
		}
		else
		{
			iPointCount++;
		}	
	}

	SetDlgItemInt(IDC_EDIT_DANGERAREA_POINTNUM, iPointCount);

	RedrawWindow();
}


void CLS_VCAEVENT_NavigationDetect::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_VCAEVENT_NavigationDetect::OnParamChangeNotify]Invalid logon id(%d)", _iLogonID);
		return;
	}

	if (_iChannelNo == m_iChannelNO)//Only refresh the channel whose parameter has changed
	{
		switch(_iParaType)
		{
		case  PARA_ANYSCENE:
			{
				AddLog(LOG_TYPE_MSG,"","NavigationDetect Set success.iLogonID(%d) iChannelNo(%d)", _iLogonID, _iChannelNo);

			}
			break;
		default:
			break;
		}
	}
}

// D:\trunk\Demo\NetClientDemo\Config\CLS_AirLinesPlan.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include ".\CLS_AirLinesPlan.h"


// CLS_AirLinesPlan dialog

IMPLEMENT_DYNAMIC(CLS_AirLinesPlan, CDialog)

CLS_AirLinesPlan::CLS_AirLinesPlan(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_AirLinesPlan::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
}

CLS_AirLinesPlan::~CLS_AirLinesPlan()
{
}

void CLS_AirLinesPlan::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SCENEID, m_cboSceneID);
	DDX_Control(pDX, IDC_COMBO_AIRLINETYPE, m_cboAirLineType);
	DDX_Control(pDX, IDC_EDIT_AIRLINEPOINT_LEN, m_editPointLen);
	DDX_Control(pDX, IDC_EDIT_STARTLONGTITUDE, m_editStartLong);
	DDX_Control(pDX, IDC_EDIT_ENDLONGITUDE, m_editEndLong);
	DDX_Control(pDX, IDC_EDIT_STARTLATITUDE, m_editStartLat);
	DDX_Control(pDX, IDC_EDIT_ENDLATITUDE, m_editEndLat);
	DDX_Control(pDX, IDC_EDIT_PAGE, m_editPage);
	DDX_Control(pDX, IDC_LIST1, m_listLongitudeLatitude);
	DDX_Control(pDX, IDC_COMBO_NUM_PERPAGE, m_cboNumPerPage);
}


BEGIN_MESSAGE_MAP(CLS_AirLinesPlan, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_QUERY, &CLS_AirLinesPlan::OnBnClickedButtonQuery)
	ON_BN_CLICKED(IDC_BUTTON_SETAIRLINES, &CLS_AirLinesPlan::OnBnClickedButtonSetAirlines)
END_MESSAGE_MAP()


// CLS_AirLinesPlan message handler

BOOL CLS_AirLinesPlan::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_cboSceneID.SetCurSel(0);
	m_cboAirLineType.SetCurSel(2);
	m_cboNumPerPage.SetCurSel(0);
	SetDlgItemText(IDC_EDIT_AIRLINEPOINT_LEN, _T("10000"));
	SetDlgItemText(IDC_EDIT_STARTLONGTITUDE, _T("117.07967761562539"));
	SetDlgItemText(IDC_EDIT_STARTLATITUDE, _T("39.09619869239399"));
	SetDlgItemText(IDC_EDIT_ENDLONGITUDE, _T("117.07948340691185"));
	SetDlgItemText(IDC_EDIT_ENDLATITUDE, _T("39.09619047914471"));
	SetDlgItemText(IDC_EDIT_PAGE, _T("0"));
	SetDlgItemText(IDC_COMBO_NUM_PERPAGE, _T("0"));
	UI_UpdateUIText();
	return TRUE;  // return TRUE unless you set the focus to a control	
}

void CLS_AirLinesPlan::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
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
}

CString CLS_AirLinesPlan::UllToStr(unsigned long long _ullPosition)
{
	double dNumTemp = 0;
	CString strNumTemp;
	dNumTemp = static_cast<double>(_ullPosition);
	dNumTemp = dNumTemp / 100000000000000 - 180;
	strNumTemp.Format(_T("%0.14lf"), dNumTemp);
	return strNumTemp;
}

unsigned long long CLS_AirLinesPlan::GetLongItudeAndLatitude(int  _iIDDlgItem, CString _strNumTemp)
{
	GetDlgItemText(_iIDDlgItem, _strNumTemp);
	return static_cast<unsigned long long>((_tstof(_strNumTemp) + 180.0) * 100000000000000);
}

void CLS_AirLinesPlan::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
}

void CLS_AirLinesPlan::UI_UpdateUIText()
{
	m_listLongitudeLatitude.DeleteColumn(0);
	m_listLongitudeLatitude.DeleteColumn(0);
	m_listLongitudeLatitude.InsertColumn(0, GetTextByLan(_T("保留"), _T("Retain")), LVCFMT_CENTER, 0);
	m_listLongitudeLatitude.InsertColumn(1, GetTextByLan(_T("开始航点的经度"), _T("Longitude of starting waypoint")), LVCFMT_CENTER, 200);
	m_listLongitudeLatitude.InsertColumn(2, GetTextByLan(_T("开始航点的纬度"), _T("Latitude of starting waypoint")), LVCFMT_CENTER, 200);
	m_listLongitudeLatitude.DeleteColumn(0);		//delete column 0
	SetDlgItemText(IDC_STATIC_SETLINE, GetTextByLan(_T("设置飞行航线"), _T("Set flight route")));
	SetDlgItemText(IDC_STATIC_SCENEID, GetTextByLan(_T("场景号"), _T("SceneID")));
	SetDlgItemText(IDC_STATIC_LINETYPE, GetTextByLan(_T("航线计划生成类型"), _T("Route plan generation type")));
	SetDlgItemText(IDC_STATIC_WAYPOINTSPACE, GetTextByLan(_T("航点间距(范围：0~10000000)"), _T("Waypoint spacing (range:0~10000000)")));
	SetDlgItemText(IDC_STATIC_STARTLONG, GetTextByLan(_T("开始航点经度"), _T("Start waypoint longitude")));
	SetDlgItemText(IDC_STATIC_STARTLAT, GetTextByLan(_T("开始航点纬度"), _T("Starting waypoint latitude")));
	SetDlgItemText(IDC_STATIC_ENDLONG, GetTextByLan(_T("结束航点经度"), _T("End waypoint longitude")));
	SetDlgItemText(IDC_STATIC_ENDLAT, GetTextByLan(_T("结束航点纬度"), _T("End waypoint latitude")));
	SetDlgItemText(IDC_STATIC_QUERYINFO, GetTextByLan(_T("查询飞行航线生成信息"), _T("Query flight route generation information")));
	SetDlgItemText(IDC_STATIC_PAGE, GetTextByLan(_T("页码"), _T("Page number")));
	SetDlgItemText(IDC_STATIC_NUMPERPAGE, GetTextByLan(_T("每页条数"), _T("Number of per page")));
	SetDlgItemText(IDC_BUTTON_SETAIRLINES, GetTextByLan(_T("设置"), _T("Set Up")));
	SetDlgItemText(IDC_BUTTON_QUERY, GetTextByLan(_T("查询"), _T("Query")));

	m_cboAirLineType.ResetContent();
	m_cboAirLineType.InsertString(0, GetTextByLan(_T("保留"), _T("Retain")));
	m_cboAirLineType.InsertString(1, GetTextByLan(_T("根据垂线生成"), _T("Generate from vertical")));
	m_cboAirLineType.InsertString(2, GetTextByLan(_T("等间距生成"), _T("Equidistant generation")));
	m_cboAirLineType.SetCurSel(2);
}

void CLS_AirLinesPlan::OnBnClickedButtonSetAirlines()
{
	// TODO: Add control notification handler code here
	if (-1 == m_iLogonID|| -1 == m_iChannelNo)
	{
		AddLog(LOG_TYPE_MSG, "", "OnBnClickedButtonSetAirlines::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}
	CString strNumTemp;
	AirLinesPlan tInfo = {0};
	tInfo.iPlanType = m_cboAirLineType.GetCurSel();
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iWayPointInterval = (int)GetDlgItemInt(IDC_EDIT_AIRLINEPOINT_LEN);

	tInfo.ullStartLongitude = GetLongItudeAndLatitude(IDC_EDIT_STARTLONGTITUDE, strNumTemp);
	tInfo.ullStartLatitude = GetLongItudeAndLatitude(IDC_EDIT_STARTLATITUDE, strNumTemp);
	tInfo.ullEndLongitude = GetLongItudeAndLatitude(IDC_EDIT_ENDLONGITUDE, strNumTemp);
	tInfo.ullEndLatitude =  GetLongItudeAndLatitude(IDC_EDIT_ENDLATITUDE, strNumTemp);
	int iRetValue = NetClient_CmdConfig(m_iLogonID, CMD_AIRLINESPLAN, m_iChannelNo, &tInfo, sizeof(tInfo), &tInfo, sizeof(tInfo));
	if(RET_SUCCESS == iRetValue)
	{
		m_cboSceneID.SetCurSel(tInfo.iSceneId);
		m_cboAirLineType.SetCurSel(tInfo.iPlanType);
		m_editPointLen.SetWindowText((LPCTSTR)tInfo.iWayPointInterval);
		SetDlgItemText(IDC_EDIT_STARTLONGTITUDE, UllToStr(tInfo.ullStartLongitude));
		SetDlgItemText(IDC_EDIT_STARTLATITUDE, UllToStr(tInfo.ullStartLatitude));
		SetDlgItemText(IDC_EDIT_ENDLONGITUDE, UllToStr(tInfo.ullEndLongitude));
		SetDlgItemText(IDC_EDIT_ENDLATITUDE, UllToStr(tInfo.ullEndLatitude));
		AddLog(LOG_TYPE_SUCC, "","CLS_AirLinesPlan::NetClient_CmdConfig[CMD_AIRLINESPLAN] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNo, tInfo.iResult);
		MessageBox(GetTextByLan("设置飞行航线成功", "Set flight route successfully"));
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_AirLinesPlan::NetClient_CmdConfig[CMD_AIRLINESPLAN] (%d, %d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
	return;
}

void CLS_AirLinesPlan::OnBnClickedButtonQuery()
{
	// TODO: Add control notification handler code here
	if (-1 == m_iLogonID || -1 == m_iChannelNo)
	{
		AddLog(LOG_TYPE_MSG, "", "OnBnClickedButtonQuery::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}
	QueryAirLineInfo tInfo= {0};
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iParamNum = 2;						//There are currently two parameters, longitude and latitude
	tInfo.iPageNo = GetDlgItemInt(IDC_EDIT_PAGE);
	tInfo.iPageSize = GetDlgItemInt(IDC_COMBO_NUM_PERPAGE);

	QueryAirLineInfoResult tInfoResult = {0};

	int iRetValue = NetClient_CmdConfig(m_iLogonID, CMD_QUERY_AIRLINESPLANINFO, m_iChannelNo, &tInfo, sizeof(tInfo), &tInfoResult, sizeof(tInfoResult));
	if(RET_SUCCESS == iRetValue)
	{
		m_listLongitudeLatitude.DeleteAllItems();
		for(int i = 0; i < tInfoResult.iPageSize; i++)
		{
			m_listLongitudeLatitude.InsertItem(i, (LPCTSTR)UllToStr(tInfoResult.tLocationInfo[i].ullStartLongitude));
			m_listLongitudeLatitude.SetItemText(i, 1, (LPCTSTR)UllToStr(tInfoResult.tLocationInfo[i].ullStartLatitude));
		}
		AddLog(LOG_TYPE_SUCC, "","CLS_AirLinesPlan::NetClient_CmdConfig[CMD_QUERY_AIRLINESPLANINFO] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNo, iRetValue);
		MessageBox(GetTextByLan("查找飞行航线生成的信息成功", "The information generated by the flight route is found successfully"));
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_AirLinesPlan::NetClient_CmdConfig[CMD_QUERY_AIRLINESPLANINFO] (%d, %d), error(%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
	return;
}

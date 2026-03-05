// CLS_GaugeCalib.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_GaugeCalib.h"


// CLS_GaugeCalib dialog

IMPLEMENT_DYNAMIC(CLS_GaugeCalib, CDialog)

CLS_GaugeCalib::CLS_GaugeCalib(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_GaugeCalib::IDD, pParent)
{
	//m_iOrdinary[MAX_SCENE_NUM_EX] = {0};
	//m_iBaseNum[MAX_SCENE_NUM_EX] = {0};
	memset(m_iOrdinary, 0, sizeof(m_iOrdinary));
	memset(m_iBaseNum, 0, sizeof(m_iOrdinary));
}

CLS_GaugeCalib::~CLS_GaugeCalib()
{
}

void CLS_GaugeCalib::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SCENE, m_cboSceneID);
	DDX_Control(pDX, IDC_COMBO_NUM, m_cboNum);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_cboPointType);
	DDX_Control(pDX, IDC_EDIT2, m_iDistance);
	DDX_Control(pDX, IDC_EDIT3, m_edtRTKHeight);
	DDX_Control(pDX, IDC_EDIT4, m_edtAngle);
	DDX_Control(pDX, IDC_EDIT5, m_edtWaterLevel);
	DDX_Control(pDX, IDC_EDIT6, m_edtRiverWidth);
	DDX_Control(pDX, IDC_COMBO4, m_cboResult);
	DDX_Control(pDX, IDC_EDIT1, m_edtPoint);
	DDX_Control(pDX, IDC_EDIT7, m_edtPointY);
	DDX_Control(pDX, IDC_COMBO1, m_iGeoType);
	DDX_Control(pDX, IDC_COMBO7, m_cboLatitude);
	DDX_Control(pDX, IDC_COMBO9, m_cboLongtitude);
	DDX_Control(pDX, IDC_COMBO10, m_cboOffsetLatitude);
	DDX_Control(pDX, IDC_COMBO11, m_cboOffsetLongtitude);
	DDX_Control(pDX, IDC_EDIT8, m_edtlatiDegree);
	DDX_Control(pDX, IDC_EDIT9, m_edtlatiMin);
	DDX_Control(pDX, IDC_EDIT10, m_edtLatiSec);
	DDX_Control(pDX, IDC_EDIT12, m_edtLongDegree);
	DDX_Control(pDX, IDC_EDIT14, m_edtLongMin);
	DDX_Control(pDX, IDC_EDIT11, m_edtLongSec);
	DDX_Control(pDX, IDC_EDIT15, m_edtOffsetLatitudeDegree);
	DDX_Control(pDX, IDC_EDIT16, m_edtOffsetLatitudeMin);
	DDX_Control(pDX, IDC_EDIT17, m_edtOffsetLatitudeSec);
	DDX_Control(pDX, IDC_EDIT18, m_edtOffsetLaongtitudeDegree);
	DDX_Control(pDX, IDC_EDIT19, m_edtOffsetLongMin);
	DDX_Control(pDX, IDC_EDIT20, m_edtOffsetLongSec);
	DDX_Control(pDX, IDC_EDIT13, m_edtHeight);
	DDX_Control(pDX, IDC_EDIT21, m_edtOffsetHeight);
}


BEGIN_MESSAGE_MAP(CLS_GaugeCalib, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON2, &CLS_GaugeCalib::OnBnClickedButton2)
	ON_BN_CLICKED(IDC_BUTTON1, &CLS_GaugeCalib::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_BUTTON3, &CLS_GaugeCalib::OnBnClickedButton3)
	ON_CBN_SELCHANGE(IDC_COMBO_SCENE, &CLS_GaugeCalib::OnCbnSelchangeComboScene)
	ON_CBN_SELCHANGE(IDC_COMBO_NUM, &CLS_GaugeCalib::OnCbnSelchangeComboNum)
	ON_CBN_SELCHANGE(IDC_COMBO_TYPE, &CLS_GaugeCalib::OnCbnSelchangeComboType)
	ON_BN_CLICKED(IDC_BUTTON5, &CLS_GaugeCalib::OnBnClickedButton5)
END_MESSAGE_MAP()


// CLS_GaugeCalib message handlers

void CLS_GaugeCalib::OnBnClickedButton2()
{
	// TODO: Add your control notification handler code here
	VirtualGaugeCalib tInfo = {0};
	tInfo.iSize = (int)sizeof(VirtualGaugeCalib);
	tInfo.iScene = m_cboSceneID.GetCurSel();
	tInfo.iType = m_cboPointType.GetCurSel();
	if (0 == tInfo.iType)
	{
		tInfo.iNum = 0;
	}
	else
	{
		tInfo.iNum = m_cboNum.GetCurSel();
	}
	CString strPointx, strPointY;
	m_edtPoint.GetWindowText(strPointx);
	m_edtPointY.GetWindowText(strPointY);
	tInfo.iPosX = _ttoi(strPointx);
	tInfo.iPosY = _ttoi(strPointY);
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_GAUGECALIBRATE, m_iChannelNO, &tInfo, (int)sizeof(VirtualGaugeCalib));
}

BOOL CLS_GaugeCalib::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	for (int i = 0;i < MAX_SCENE_NUM/2; i++)
	{
		CString strSceneID;
		strSceneID.Format("%d", i+1);
		m_cboSceneID.AddString(strSceneID);
	}
	for (int i = 0;i < 30; i++)
	{
		CString strSceneID;
		strSceneID.Format("%d", i);
		m_cboNum.InsertString(i, strSceneID);
	}
	m_cboPointType.InsertString(0,GetTextByLan(_T("水位基准点"), _T("Base Water Point")));
	m_cboPointType.InsertString(1,GetTextByLan(_T("普通点"), _T("Ordinary Point")));
	m_cboResult.InsertString(0,GetTextByLan(_T("成功"), _T("Success")));
	m_cboResult.InsertString(1,GetTextByLan(_T("失败"), _T("Fail")));

	m_iGeoType.InsertString(0, "manual");
	m_iGeoType.InsertString(1, "automatic");

	m_cboLatitude.InsertString(0 ,"East");



	GetDlgItem(IDC_BUTTON1)->ShowWindow(SW_HIDE);
	return TRUE;
}

void CLS_GaugeCalib::OnBnClickedButton1()
{
	// TODO: Add your control notification handler code here
}




void CLS_GaugeCalib::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNO = _iChannelNo;
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	m_iStreamNo = _iStreamNo;
	UpdateParam();
}

void CLS_GaugeCalib::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	int iMsgType = _wParam & 0xFFFF;
	switch (iMsgType)
	{
	case WCM_VIRTUAL_GAUGELIB:
		{
			VirtualGaugeCalibResult *pInfo = (VirtualGaugeCalibResult *)_iLParam;
			UpdateGaugeCalibParam(pInfo);
			SaveGaugeCalibParam(pInfo);
		}
		break;
	}
}

void CLS_GaugeCalib::SaveGaugeCalibParam(VirtualGaugeCalibResult *pInfo)
{
	if (0 == pInfo->iType)  //water level base point
	{
		m_iBaseNum[pInfo->iScene] = 1;
	}
	else
	{
		m_iOrdinary[pInfo->iScene]++;
	}
	memcpy(&m_tVirtualGaugeResult[pInfo->iScene][pInfo->iType][pInfo->iNum], pInfo, (int)sizeof(VirtualGaugeCalibResult));
}

void CLS_GaugeCalib::UpdateGaugeCalibParam(VirtualGaugeCalibResult *pInfo)
{
	CString strDisatnce, strRtkHeight,strTitAngle,strWaterlevel,strRiverWidth;
	strDisatnce.Format("%d", pInfo->iDistance);
	m_iDistance.SetWindowText(strDisatnce);
	strRtkHeight.Format("%d", pInfo->iAltitude);
	m_edtRTKHeight.SetWindowText(strRtkHeight);
	strTitAngle.Format("%d", pInfo->iTiltAngle);
	m_edtAngle.SetWindowText(strTitAngle);
    strWaterlevel.Format("%d", pInfo->iWaterLevel);
	m_edtWaterLevel.SetWindowText(strWaterlevel);
	strRiverWidth.Format("%d", pInfo->iRiverWidth);
	m_edtRiverWidth.SetWindowText(strRiverWidth);
	if (RET_SUCCESS != pInfo->iResult)
	{
		m_cboResult.SetCurSel(1);
	}
	else
		m_cboResult.SetCurSel(0);
}


void CLS_GaugeCalib::OnBnClickedButton3()
{
	// TODO: Add your control notification handler code here
	int iRet = RET_FAILED;
	for (int i = 0; i < MAX_SCENE_NUM_EX; i++)
	{
		if (0 == m_iBaseNum[i])
		{
			continue;
		}
		VirtualGaugeParam tParam = {0};
		tParam.iSize = sizeof(VirtualGaugeParam);
		tParam.iSum = m_iBaseNum[i];
		tParam.iType = 0;
		tParam.iScene = i;
		memcpy(&tParam.iNum, &m_tVirtualGaugeResult[i][0][0].iNum, (int)(sizeof(VirtualGaugeParam)-16));
		iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GAUGEPARA, m_iChannelNO, &tParam, tParam.iSize);
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL,"","CLS_GaugeCalib:NetClient_SetDevConfig = %d", iRet);
		}
		else
		{
			AddLog(LOG_TYPE_SUCC,"","CLS_GaugeCalib:NetClient_SetDevConfig = %d", iRet);
		}
	}
	for (int i = 0; i < MAX_SCENE_NUM_EX; i++)
	{
		if (0 == m_iOrdinary[i])
		{
			continue;
		}
		for (int j = 0; j < m_iOrdinary[i];j++)
		{
			VirtualGaugeParam tParam = {0};
			tParam.iSize = sizeof(VirtualGaugeParam);
			tParam.iSum = m_iOrdinary[i];
			tParam.iType = 0;
			tParam.iScene = i;
			memcpy(&tParam.iNum, &m_tVirtualGaugeResult[i][0][j].iNum, (int)(sizeof(VirtualGaugeParam)-16));
			iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GAUGEPARA, m_iChannelNO, &tParam, tParam.iSize);
			if (RET_SUCCESS != iRet)
			{
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig(%d,%d)",m_iLogonID,iRet);
			}
			else
			{
				AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig(%d,%d)",m_iLogonID, iRet);
			}
		}
	}

}

void CLS_GaugeCalib::OnCbnSelchangeComboScene()
{
	// TODO: Add your control notification handler code here
	UpdateParam();

}

void CLS_GaugeCalib::UpdateParam()
{
	VirtualGaugeParam tParam = {0};
	tParam.iSize = (int)sizeof(VirtualGaugeParam);
	tParam.iScene = m_cboSceneID.GetCurSel();
	tParam.iType = m_cboPointType.GetCurSel();
	tParam.iNum = m_cboNum.GetCurSel();
	int iBytesReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GAUGEPARA, m_iChannelNO, &tParam, tParam.iSize, &iBytesReturn);
	if (RET_SUCCESS == iRet)
	{
		CString strDisatnce, strRtkHeight,strTitAngle,strWaterlevel,strRiverWidth;
		strDisatnce.Format("%d", tParam.iDistance);
		m_iDistance.SetWindowText(strDisatnce);
		strRtkHeight.Format("%d", tParam.iAltitude);
		m_edtRTKHeight.SetWindowText(strRtkHeight);
		strTitAngle.Format("%d", tParam.iTitleAngle);
		m_edtAngle.SetWindowText(strTitAngle);
		strWaterlevel.Format("%d", tParam.iWaterLevel);
		m_edtWaterLevel.SetWindowText(strWaterlevel);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_GaugeCalib:UpdateParam = %d", iRet);
	}
}

void CLS_GaugeCalib::OnCbnSelchangeComboNum()
{
	// TODO: Add your control notification handler code here
	UpdateParam();
}

void CLS_GaugeCalib::OnCbnSelchangeComboType()
{
	// TODO: Add your control notification handler code here
	UpdateParam();
}

void CLS_GaugeCalib::OnBnClickedButton5()
{
	// TODO: Add your control notification handler code here
	GeografhyLocation tParam = {0};
	tParam.iBufSize = (int)sizeof(GeografhyLocation);
	tParam.iType = 0;
	tParam.tLongitudeInfo.iDegree = 80;
	tParam.tLongitudeInfo.iDirection = 0;
	tParam.tLongitudeInfo.iMinute = 30;
	tParam.tLongitudeInfo.iSecond = 1000;
	tParam.tLatitudeInfo.iDirection = 1;
	tParam.tLatitudeInfo.iMinute = 50;
	tParam.tLatitudeInfo.iSecond = 800;
	tParam.tLatitudeInfo.iDegree = 60;
	tParam.iHeight = 10;
	tParam.tOffsetLongitudeInfo.iDegree = 80;
	tParam.tOffsetLongitudeInfo.iMinute = 30;
	tParam.tOffsetLongitudeInfo.iSecond = 900;
	tParam.tOffsetLatitudeInfo.iMinute = 30;
	tParam.tOffsetLatitudeInfo.iDegree = 30;
	tParam.tOffsetLatitudeInfo.iSecond = 5000;
	tParam.ioffSetHeight = 20;
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GEOGRAFHY_LOCATION, 0x7fffffff, &tParam, tParam.iBufSize);
}


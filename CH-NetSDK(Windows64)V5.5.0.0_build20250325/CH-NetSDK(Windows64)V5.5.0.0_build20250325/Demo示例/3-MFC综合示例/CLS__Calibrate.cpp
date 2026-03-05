// CLS__Calibrate.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS__Calibrate.h"


// CLS__Calibrate dialog

IMPLEMENT_DYNAMIC(CLS_Calibrate, CDialog)

CLS_Calibrate::CLS_Calibrate(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_Calibrate::IDD, pParent)
{

}

CLS_Calibrate::~CLS_Calibrate()
{
}

void CLS_Calibrate::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_CALIBRATE_POINTS, m_edtPoints);
}


BEGIN_MESSAGE_MAP(CLS_Calibrate, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_CALIBRATE_DRAW, &CLS_Calibrate::OnBnClickedButtonCalibrateDraw)
	ON_BN_CLICKED(IDC_BUTTON__CALIBRATE_SET, &CLS_Calibrate::OnBnClickedButton)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// CLS__Calibrate message handler

void CLS_Calibrate::OnBnClickedButtonCalibrateDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(0, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, 4);
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
			m_edtPoints.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_CALIBRATE_POINT_NUM, iPointNum);
		}
		else
		{
			m_edtPoints.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_CALIBRATE_POINT_NUM, 0);
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_Calibrate::OnBnClickedButton()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_Calibrate::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}
	ItsRadarCalibrate tInfo = {0};
	tInfo.iSize =  sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iPointNum = GetDlgItemInt(IDC_EDIT_CALIBRATE_POINT_NUM);
	vca_TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
	CString cstPolygon = "";
	m_edtPoints.GetWindowText(cstPolygon);
	GetPointsFromString(cstPolygon, tInfo.iPointNum, (POINT*)ptPolygon);
	for (int i = 0; i < tInfo.iPointNum && i< 4 ; i++)
	{
		tInfo.tPoints[i].tVideoPosition.iX = ptPolygon[i].iX;
		tInfo.tPoints[i].tVideoPosition.iY = ptPolygon[i].iY;
	}
	tInfo.tPoints[0].tRealPosition.iX = GetDlgItemInt(IDC_EDIT_CALIBRATE_X1);
	tInfo.tPoints[0].tRealPosition.iY = GetDlgItemInt(IDC_EDIT_CALIBRATE_Y1);

	tInfo.tPoints[1].tRealPosition.iX = GetDlgItemInt(IDC_EDIT_CALIBRATE_X2);
	tInfo.tPoints[1].tRealPosition.iY = GetDlgItemInt(IDC_EDIT__CALIBRATE_Y2);

	tInfo.tPoints[2].tRealPosition.iX = GetDlgItemInt(IDC_EDIT__CALIBRATE_X3);
	tInfo.tPoints[2].tRealPosition.iY = GetDlgItemInt(IDC_EDIT_CALIBRATE_Y3);

	tInfo.tPoints[3].tRealPosition.iX = GetDlgItemInt(IDC_EDIT__CALIBRATE_X4);
	tInfo.tPoints[3].tRealPosition.iY = GetDlgItemInt(IDC_EDIT__CALIBRATE_Y4);

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_RADAR_CALIBRATE, m_iChannelNo, &tInfo, sizeof(ItsRadarCalibrate));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_RADAR_CALIBRATE fail!");
	}
}

void CLS_Calibrate::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UpdatePageUI();
	}

}

BOOL CLS_Calibrate::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdatePageUI();
	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_Calibrate::OnChannelChanged( int _iLogonID,int _iChannelNo, int _iStreamNo)
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

void CLS_Calibrate::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_Calibrate::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}
	
	ItsRadarCalibrate tInfo = {0};
	tInfo.iSize =  sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	
	int iReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_RADAR_CALIBRATE, m_iChannelNo, &tInfo, sizeof(ItsRadarCalibrate), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_RADAR_CALIBRATE fail!");
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_CALIBRATE_POINT_NUM, tInfo.iPointNum);

		CString szPointBuf;
		for (int i = 0; i < tInfo.iPointNum && i < 4; i++)
		{
			CString tmpStr;
			tmpStr.Format("(%d,%d)", tInfo.tPoints[i].tVideoPosition.iX,  tInfo.tPoints[i].tVideoPosition.iY);
			szPointBuf += tmpStr;
		}
		m_edtPoints.SetWindowText(szPointBuf);

		SetDlgItemInt(IDC_EDIT_CALIBRATE_X1, tInfo.tPoints[0].tRealPosition.iX);
		SetDlgItemInt(IDC_EDIT_CALIBRATE_Y1, tInfo.tPoints[0].tRealPosition.iY);

		SetDlgItemInt(IDC_EDIT_CALIBRATE_X2, tInfo.tPoints[1].tRealPosition.iX);
		SetDlgItemInt(IDC_EDIT__CALIBRATE_Y2, tInfo.tPoints[1].tRealPosition.iY);

		SetDlgItemInt(IDC_EDIT__CALIBRATE_X3, tInfo.tPoints[2].tRealPosition.iX);
		SetDlgItemInt(IDC_EDIT_CALIBRATE_Y3, tInfo.tPoints[2].tRealPosition.iY);

		SetDlgItemInt(IDC_EDIT__CALIBRATE_X4, tInfo.tPoints[3].tRealPosition.iX);
		SetDlgItemInt(IDC_EDIT__CALIBRATE_Y4, tInfo.tPoints[3].tRealPosition.iY);

	}

	return;
} 


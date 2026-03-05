// VideoVideoForDraw.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"

#include "NetClientTypes.h"
#include "Include/NVSSDK_INTERFACE.h"
using namespace NVSSDK_INTERFACE;
#include "Common/CommonFun.h"

#include "VideoViewForDraw.h"

// CLS_VideoVideoForDraw dialog
#define	INVALID_CONNECT_ID	(0xFFFFFFFF)

IMPLEMENT_DYNAMIC(CLS_VideoViewForDraw, CDialog)

CLS_VideoViewForDraw::CLS_VideoViewForDraw(CWnd* pParent /*=NULL*/)
	: CDialog(CLS_VideoViewForDraw::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNO = -1;
	m_iStreamNO = -1;

	m_uiConnID = INVALID_CONNECT_ID;
	m_iDrawType = DrawType_invalid;

	m_pcPointBuf = NULL;
	m_piPointNum = NULL;
	m_piDirection = NULL;

	m_iDirection = 0;
	m_iPolygonPointNum = 0;
	memset(&m_ptPolygon, 0, sizeof(m_ptPolygon));
	
	m_iFluxCout = 0;
	memset(&m_ptFlux, 0, sizeof(m_ptFlux));
	memset(&m_rcCrowd, 0, sizeof(m_rcCrowd));

	m_pcMainRegoinBuf = NULL;
	m_iMainRegionPointNum = 0;
	memset(m_ptMainRegion, 0, sizeof(m_ptMainRegion));

	for (int i=0; i<MAX_AB_SUBREGOIN_NUM; i++)
	{
		m_pcSubRegoinBuf[i] = NULL;
		m_iSubRegionPointNum[i] = 0;
	}
	m_iABDrawIndex = -1;	//-1 is the main region, 0\1\2 are the three sub regions
	memset(m_ptSubRegion, 0, sizeof(m_ptSubRegion));

	m_iLeft = 0;
	m_iTop = 0;
	m_iRight = 0;
	m_iBottom = 0;

	m_iMaxPointCount = INNER_MAX_POINT_COUNT;

	memset(&m_tWldPresetInfo, 0, sizeof(WaterPresetInfo));
}

CLS_VideoViewForDraw::~CLS_VideoViewForDraw()
{
}

void CLS_VideoViewForDraw::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_STATIC_VIDEO_DRAW, m_staticVideoShow);
}


BEGIN_MESSAGE_MAP(CLS_VideoViewForDraw, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_DrawOK, &CLS_VideoViewForDraw::OnBnClickedButtonDrawok)
//	ON_WM_DESTROY()
	ON_WM_CLOSE()
	ON_WM_LBUTTONUP()
	ON_WM_LBUTTONDOWN()
	ON_WM_MOUSEMOVE()
	ON_BN_CLICKED(IDC_BUTTON_NextRegion, &CLS_VideoViewForDraw::OnBnClickedButtonNextregion)
	ON_BN_CLICKED(IDC_BUTTON_ClearRegion, &CLS_VideoViewForDraw::OnBnClickedButtonClearregion)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// CLS_VideoVideoForDraw message handlers

void CLS_VideoViewForDraw::OnBnClickedButtonDrawok()
{
	// TODO: Add your control notification handler code here
	// clear the line
	POINT ptNULL[2] = {{0}};
	NetClient_DrawPolyOnLocalVideo(m_uiConnID, ptNULL, 2, 0);

	StopPlay();

	int iVWidth = 1920;
	int iVHeight = 1080;
	if (NetClient_GetVideoSize)
	{
		NetClient_GetVideoSize(m_iLogonID, m_iChannelNO, &iVWidth, &iVHeight, m_iStreamNO);
		if (0 == iVWidth)
		{
			 iVWidth = 1920;
		}

		if (0 == iVHeight)
		{
			iVHeight = 1080;
		}
	}

	if (DrawType_invalid == m_iDrawType)
	{
	}
	else if (DrawType_abandum == m_iDrawType)
	{
		CString strPointArray;
		CString strPoint;
		for (int i=0; i< m_iMainRegionPointNum; i++)
		{
			strPoint.Format("(%d,%d)", m_ptMainRegion[i].x, m_ptMainRegion[i].y);
			strPointArray += strPoint;
		}
		strcpy_s(m_pcMainRegoinBuf, MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);

		for (int i=0; i<MAX_AB_SUBREGOIN_NUM; i++)
		{
			strPointArray = "";
			for (int j=0; j<m_iSubRegionPointNum[i]; j++)
			{
				strPoint.Format("(%d,%d)", m_ptSubRegion[i][j].x, m_ptSubRegion[i][j].y);
				strPointArray += strPoint;
			}
			strcpy_s(m_pcSubRegoinBuf[i], MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);
		}
	}
	else if (DrawType_Crowd == m_iDrawType)
	{
		CString strPointArray;
		CString strPoint[4];
		strPoint[0].Format("(%d,%d)", m_ptPolygon[0].x, m_ptPolygon[0].y);
		strPoint[1].Format("(%d,%d)", m_ptPolygon[1].x, m_ptPolygon[0].y);
		strPoint[2].Format("(%d,%d)", m_ptPolygon[1].x, m_ptPolygon[1].y);
		strPoint[3].Format("(%d,%d)", m_ptPolygon[0].x, m_ptPolygon[1].y);

		strPointArray = strPoint[0]+strPoint[1]+strPoint[2]+strPoint[3];
		strcpy_s(m_pcPointBuf, MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);
		*m_piPointNum = 4;
		*m_piDirection = m_iDirection;

		m_iLeft = m_ptPolygon[0].x;
		m_iTop = m_ptPolygon[0].y;
		m_iRight = m_ptPolygon[1].x;
		m_iBottom = m_ptPolygon[1].y;
	}
	else if (DrawType_Flux == m_iDrawType)
	{
		CString strPointArray;
		CString strPoint;
		for (int i=0; i< m_iFluxCout; i++)
		{
			strPoint.Format("(%d,%d)", m_ptFlux[i].x, m_ptFlux[i].y);
			strPointArray += strPoint;
		}
		strcpy_s(m_pcPointBuf, MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);
		*m_piPointNum = m_iFluxCout;
		*m_piDirection = m_iDirection;
	}
	else
	{
		CString strPointArray;
		CString strPoint;
		for (int i=0; i< m_iPolygonPointNum; i++)
		{
			if (m_blGetPoitByThousandPrecent)
			{
				strPoint.Format("(%d,%d)", m_ptPolygon[i].x * 10000 / iVWidth, m_ptPolygon[i].y * 10000 / iVHeight);
			}
			else
			{
				strPoint.Format("(%d,%d)", m_ptPolygon[i].x, m_ptPolygon[i].y);
			}
			strPointArray += strPoint;
		}
		strcpy_s(m_pcPointBuf, MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);
		/*	strcpy(m_pcPointBuf, (LPSTR)(LPCTSTR)strPointArray);*/
		*m_piPointNum = m_iPolygonPointNum;
		*m_piDirection = m_iDirection;
	}

	OnOK();
}

BOOL CLS_VideoViewForDraw::OnInitDialog()
{
	CDialog::OnInitDialog();
	// TODO:  Add extra initialization here
	if (NetClient_StartRecvEx)
	{
		CLIENTINFO tInfo = {0};
		tInfo.m_iServerID = m_iLogonID;
		tInfo.m_iChannelNo = m_iChannelNO;
		tInfo.m_iStreamNO = m_iStreamNO;
		tInfo.m_iNetMode = NETMODE_TCP;
		tInfo.m_iTimeout = 20;
		if (CHANNEL_THREE_STREAM == m_iStreamNO)
		{
			tInfo.m_iFlag = FLAG_THREE_STREAM;
		}

		int iRet = NetClient_StartRecvEx(&m_uiConnID, &tInfo, NULL, NULL);
		if (0 == iRet)
		{
		}
		else if (1 == iRet)
		{
			if (NetClient_StartPlay)
			{
				RECT rcShow = {0};
#ifdef GDYHRW_DZ
				iRet = 0;
#else
				iRet = NetClient_StartPlay(m_uiConnID, m_staticVideoShow.GetSafeHwnd(), rcShow, 0);
#endif
				if (iRet < 0)
				{
					AddLog(LOG_TYPE_FAIL," CH%d-%d","NetClient_StartPlay(%u,,,)",m_iChannelNO, m_iStreamNO, m_uiConnID);
				}
			}
		}
		else
		{
			AddLog(LOG_TYPE_FAIL," CH%d-%d","NetClient_StartRecvEx(%u,,,)",m_iChannelNO, m_iStreamNO, m_uiConnID);
		}
	}

	if (DrawType_abandum == m_iDrawType)
	{
		GetDlgItem(IDC_BUTTON_NextRegion)->ShowWindow(SW_SHOW);
	}
	else
	{
		GetDlgItem(IDC_BUTTON_NextRegion)->ShowWindow(SW_HIDE);
	}

	UI_UpdateDialog();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_VideoViewForDraw::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_VideoViewForDraw::OnMainNotify( int _iLogonID,int _wParam, void* _iLParam, void* _iUser )
{
	int iMsgType = _wParam & 0xFFFF;
	if (WCM_VIDEO_HEAD == iMsgType)
	{
		if (NetClient_StartPlay)
		{
			RECT rcShow = {0};
#ifdef GDYHRW_DZ
			int iRet = 0;
#else
			int iRet = NetClient_StartPlay(m_uiConnID, m_staticVideoShow.GetSafeHwnd(), rcShow, 0);
#endif
			if (iRet < 0)
			{
				AddLog(LOG_TYPE_FAIL," CH%d-%d","NetClient_StartPlay(%u,,,)",m_iChannelNO, m_iStreamNO, m_uiConnID);
			}

			if (DrawType_Crowd == m_iDrawType)
			{
				DrawRectgonOnVideo( &m_rcCrowd, 1);
			}
			else
			{
				memset(&m_rcCrowd, 0, sizeof(RECT));
				DrawRectgonOnVideo( &m_rcCrowd, 1);
			}

			ShowVcaWldRegion();
		}
	}
}

void CLS_VideoViewForDraw::Init( int _iLogonID, int _iChnnelNO, int _iStreamNO )
{
	m_iLogonID = _iLogonID;
	m_iChannelNO = _iChnnelNO;
	m_iStreamNO = _iStreamNO;

	m_iDrawType = DrawType_invalid;

// 	m_iLogonID = 0;
// 	m_iChannelNO = 0;
// 	m_iStreamNO = 0;
}

void CLS_VideoViewForDraw::OnClose()
{
	// TODO: Add your message handler code here and/or call default
	// clear the line
	POINT ptNULL[2] = {{0}};
	NetClient_DrawPolyOnLocalVideo(m_uiConnID, ptNULL, 2, 0);

	if (DrawType_invalid == m_iDrawType)
	{
	}
	else if (DrawType_abandum == m_iDrawType)
	{
		CString strPointArray;
		CString strPoint;
		for (int i=0; i< m_iMainRegionPointNum; i++)
		{
			strPoint.Format("(%d,%d)", m_ptMainRegion[i].x, m_ptMainRegion[i].y);
			strPointArray += strPoint;
		}
		strcpy_s(m_pcMainRegoinBuf, MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);

		for (int i=0; i<MAX_AB_SUBREGOIN_NUM; i++)
		{
			strPointArray = "";
			for (int j=0; j<m_iSubRegionPointNum[i]; j++)
			{
				strPoint.Format("(%d,%d)", m_ptSubRegion[i][j].x, m_ptSubRegion[i][j].y);
				strPointArray += strPoint;
			}
			strcpy_s(m_pcSubRegoinBuf[i], MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);
		}
	}
	else
	{
		CString strPointArray;
		CString strPoint;
		for (int i=0; i< m_iPolygonPointNum; i++)
		{
			strPoint.Format("(%d,%d)", m_ptPolygon[i].x, m_ptPolygon[i].y);
			strPointArray += strPoint;
		}
		strcpy_s(m_pcPointBuf, MAX_POINTBUF_LEN, (LPSTR)(LPCTSTR)strPointArray);
		/*	strcpy(m_pcPointBuf, (LPSTR)(LPCTSTR)strPointArray);*/
		*m_piPointNum = m_iPolygonPointNum;
		*m_piDirection = m_iDirection;
	}
	StopPlay();

	CDialog::OnClose();
}

void CLS_VideoViewForDraw::StopPlay()
{
	if (INVALID_CONNECT_ID != m_uiConnID)
	{
		if (NetClient_StopPlay)
		{
			RECT rcShow = {0};
			int iRet = NetClient_StopPlay(m_uiConnID);

			if (iRet < 0)
			{
				AddLog(LOG_TYPE_FAIL," CH%d-%d","NetClient_StopPlay(%d,%d,%d)",m_iChannelNO, m_iStreamNO, m_uiConnID);
			}
		}

		if (NetClient_StopRecv)
		{
			int iRet = NetClient_StopRecv(m_uiConnID);
			if (iRet < 0)
			{
				AddLog(LOG_TYPE_FAIL," CH%d-%d","NetClient_StopRecv(%d,%d,%d)",m_iChannelNO, m_iStreamNO, m_uiConnID);
			}
		}
	}
}

BOOL CLS_VideoViewForDraw::PolygonOnViedo( POINT *_ptArr, int _iPointCnt )
{
	BOOL  bRet = FALSE;
	if (_ptArr == NULL && _iPointCnt <= 0)
	{
		return bRet;
	}
	//First convert point to IDC_STATIC_VIDEO_DRAW coordinate system
	int iVHeight = 0;
	int iVWidth = 0;
	if (NetClient_GetVideoSize)
	{
		NetClient_GetVideoSize(m_iLogonID, m_iChannelNO, &iVWidth, &iVHeight, m_iStreamNO);

		CRect localRect;
		GetDlgItem(IDC_STATIC_VIDEO_DRAW)->GetClientRect(&localRect);

		const double dRateWidth = (double)iVWidth/localRect.Width();
		const double dRateHeight = (double)iVHeight/localRect.Height();

		for (int i=0; i<_iPointCnt; i++)
		{
			_ptArr[i].x = (LONG)(_ptArr[i].x * dRateWidth);
			if (_ptArr[i].x > iVWidth)
			{
				_ptArr[i].x = iVWidth;
				bRet = TRUE;
			}
			if (_ptArr[i].x < 0)
			{
				_ptArr[i].x = 0;
				bRet = TRUE;
			}
			_ptArr[i].y = (LONG)(_ptArr[i].y * dRateHeight);
			if (_ptArr[i].y > iVHeight)
			{
				_ptArr[i].y = iVHeight;
				bRet = TRUE;
			}
			if (_ptArr[i].y < 0)
			{
				_ptArr[i].y = 0;
				bRet = TRUE;
			}
		}
	}
	
	return bRet;
}

void CLS_VideoViewForDraw::OnLButtonUp(UINT nFlags, CPoint point)
{
	// TODO: Add your message handler code here and/or call default
	//point is the coordinate relative to this dialog
	if ((DrawType_tripwire == m_iDrawType)||(DrawType_Crowd == m_iDrawType) || (DrawType_GaugePoint == m_iDrawType))
	{
		if (IsInsideVideoStatic(point) && (DrawType_GaugePoint != m_iDrawType))
		{
			if (m_iPolygonPointNum == 1)
			{
				m_ptPolygon[1] = (POINT)point;
				PolygonOnViedo(&m_ptPolygon[1], 1);
				m_iPolygonPointNum=2;
				if (DrawType_tripwire == m_iDrawType)
				{
					DrawPolygonOnVideo(m_ptPolygon, 2, 0);
				}
				else if (DrawType_Crowd == m_iDrawType)
				{
					m_rcCrowd.left = m_ptPolygon[0].x;
					m_rcCrowd.top  = m_ptPolygon[0].y;
					m_rcCrowd.right = m_ptPolygon[1].x;
					m_rcCrowd.bottom = m_ptPolygon[1].y;
					DrawRectgonOnVideo(&m_rcCrowd, 1);
				}
			}
		}
		else if(DrawType_GaugePoint == m_iDrawType)
		{
			if (m_iPolygonPointNum > 10)
			{
				return;
			}
			POINT tIndex[2] = {0};
			//m_ptPolygon[0].x = point.x -30;
			//m_ptPolygon[0].y = point.y;
			PolygonOnViedo(&point, 1);
			m_ptPolygon[m_iPolygonPointNum].x = point.x;
			m_ptPolygon[m_iPolygonPointNum].y = point.y;
			tIndex[0].x = point.x - 30;
			tIndex[0].y = point.y;
			tIndex[1].x = point.x + 30;
			tIndex[1].y = point.y;
			DrawPolygonOnVideo(tIndex, 2, 2);
			m_iPolygonPointNum++;
		}
	}
	else if (DrawType_perimeter == m_iDrawType)
	{
		if (IsInsideVideoStatic(point))
		{
			//	
			// Reserve one for closing;
			if (m_iPolygonPointNum+1 <= m_iMaxPointCount)
			{
				m_iPolygonPointNum++;
				m_ptPolygon[m_iPolygonPointNum-1] = (POINT)point;
				PolygonOnViedo(&m_ptPolygon[m_iPolygonPointNum-1], 1);
				if (m_iPolygonPointNum >= 2)
				{
					/*PolygonOnViedo(m_ptPolygon, m_iPolygonPointNum);*/
					if (m_iPolygonPointNum >= 3)
					{
						m_ptPolygon[m_iPolygonPointNum] = m_ptPolygon[0];
						DrawPolygonOnVideo(m_ptPolygon, m_iPolygonPointNum+1, 0);
					}
					else
					{
						DrawPolygonOnVideo(m_ptPolygon, m_iPolygonPointNum, 0);
					}
				}
			}
		}
	}
	else if (DrawType_facerec == m_iDrawType)
	{
		if (IsInsideVideoStatic(point))
		{
			// Reserve one for closing;
			if (m_iPolygonPointNum+1 <= m_iMaxPointCount)
			{
				m_iPolygonPointNum++;
				m_ptPolygon[m_iPolygonPointNum-1] = (POINT)point;
				PolygonOnViedo(&m_ptPolygon[m_iPolygonPointNum-1], 1);
				if (m_iPolygonPointNum >= 2)
				{
					/*PolygonOnViedo(m_ptPolygon, m_iPolygonPointNum);*/
					if (m_iPolygonPointNum >= 3)
					{
						m_ptPolygon[m_iPolygonPointNum] = m_ptPolygon[0];
						DrawPolygonOnVideo(m_ptPolygon, m_iPolygonPointNum+1, 0);
					}
					else
					{
						DrawPolygonOnVideo(m_ptPolygon, m_iPolygonPointNum, 0);
					}
				}
			}
		}
	}
	else if (DrawType_abandum == m_iDrawType)
	{
		if (IsInsideVideoStatic(point))
		{
			if (m_iABDrawIndex >= 0 && m_iABDrawIndex <MAX_AB_SUBREGOIN_NUM)		//sub regoin
			{
				if (m_iMainRegionPointNum < 3) //main region is already at least a polygon
				{
					AddLog(LOG_TYPE_FAIL, "", "[CLS_VideoViewForDraw]DrawType_abandum, main region is invalid");
					return;
				}
				POINT ptTemp = point;
				PolygonOnViedo(&ptTemp, 1); //Unify the coordinate system with the main polygon
				// point not in main polygon
				if (!IfPointInPolygon(ptTemp, m_ptMainRegion, m_iMainRegionPointNum))
				{
					return;
				}

				// start drawing sub region
				int& iPointNum = m_iSubRegionPointNum[m_iABDrawIndex];
				if (iPointNum > 1)
					iPointNum--;
				m_ptSubRegion[m_iABDrawIndex][iPointNum++] = ptTemp; // use the same coordinate system
				int   iValid = 0;
				// Determine whether the currently drawn edge intersects with other edges of this polygon
				for(int i = 2; i < iPointNum - 1; i++)
				{
					for(int j = 0; j < i-1; j++)
						if(IfHaveInterSection(m_ptSubRegion[m_iABDrawIndex][j], m_ptSubRegion[m_iABDrawIndex][j+1], m_ptSubRegion[m_iABDrawIndex][i], m_ptSubRegion[m_iABDrawIndex][i+1]))
						{
							iValid = 1;
						}
				}
				// Determine whether the closed line intersects other edges of this polygon
				for(int i = 1; i < iPointNum - 2; i++)
				{
					if(IfHaveInterSection(m_ptSubRegion[m_iABDrawIndex][i], m_ptSubRegion[m_iABDrawIndex][i+1], m_ptSubRegion[m_iABDrawIndex][0], m_ptSubRegion[m_iABDrawIndex][iPointNum - 1]))
					{
						iValid = 1;
					}
				}
				if(iValid)
				{
					iPointNum--;
				}
				m_ptSubRegion[m_iABDrawIndex][iPointNum++] = m_ptSubRegion[m_iABDrawIndex][0];

				int iDrawFlag = 0;
				if (2 == iPointNum)
				{
					iDrawFlag |= 0x02;
				}
				else
				{
					iDrawFlag |= 0x04;
				}
			
				DrawPolygonOnVideo(m_ptSubRegion[m_iABDrawIndex], iPointNum, iDrawFlag);	
			}
			else if (-1 == m_iABDrawIndex)
			{
				// Reserve one for closing;
				if (m_iMainRegionPointNum+1 < VCA_MAX_POLYGON_POINT_NUM)
				{
					if (m_iMainRegionPointNum > 1)
						m_iMainRegionPointNum--;
					m_ptMainRegion[m_iMainRegionPointNum++] = point; // use the same coordinate system
					PolygonOnViedo(&m_ptMainRegion[m_iMainRegionPointNum-1], 1);

					int   iValid = 0;
					// Determine whether the currently drawn edge intersects with other edges of this polygon
					for(int i = 2; i < m_iMainRegionPointNum - 1; i++)
					{
						for(int j = 0; j < i-1; j++)
							if(IfHaveInterSection(m_ptMainRegion[j], m_ptMainRegion[j+1], m_ptMainRegion[i], m_ptMainRegion[i+1]))
							{
								iValid = 1;
							}
					}
					// Determine whether the closed line intersects other edges of this polygon
					for(int i = 1; i < m_iMainRegionPointNum - 2; i++)
					{
						if(IfHaveInterSection(m_ptMainRegion[i], m_ptMainRegion[i+1], m_ptMainRegion[0], m_ptMainRegion[m_iMainRegionPointNum - 1]))
						{
							iValid = 1;
						}
					}
					if(iValid)
					{
						m_iMainRegionPointNum--;
					}
					m_ptMainRegion[m_iMainRegionPointNum++] = m_ptMainRegion[0];

					int iDrawFlag = 0;
					if (2 == m_iMainRegionPointNum)
					{
						iDrawFlag |= 0x02;
					}
					else
					{
						iDrawFlag |= 0x04;
					}

					DrawPolygonOnVideo(m_ptMainRegion, m_iMainRegionPointNum, iDrawFlag);	
// 					if (m_iMainRegionPointNum >= 2)
// 					{
// 						/*PolygonOnViedo(m_ptPolygon, m_iMainRegionPointNum);*/
// 						if (m_iMainRegionPointNum >= 3)
// 						{
// 							m_ptMainRegion[m_iMainRegionPointNum] = m_ptMainRegion[0];
// 							DrawPolygonOnVideo(m_ptMainRegion, m_iMainRegionPointNum+1, 4);
// 						}
// 						else
// 						{
// 							DrawPolygonOnVideo(m_ptMainRegion, m_iMainRegionPointNum, 4);
// 						}
// 					}
				}
			}
		}
	}
	else if (DrawType_track == m_iDrawType)
	{
		if (IsInsideVideoStatic(point))
		{
			// Tracking currently only supports 4 points
			if (m_iPolygonPointNum+1 <= MAX_TRACK_POINT_NUM)
			{
				m_iPolygonPointNum++;
				m_ptPolygon[m_iPolygonPointNum-1] = (POINT)point;
				PolygonOnViedo(&m_ptPolygon[m_iPolygonPointNum-1], 1);
				if (m_iPolygonPointNum >= 2)
				{
					/*PolygonOnViedo(m_ptPolygon, m_iPolygonPointNum);*/
					if (m_iPolygonPointNum >= 3)
					{
						m_ptPolygon[m_iPolygonPointNum] = m_ptPolygon[0];
						DrawPolygonOnVideo(m_ptPolygon, m_iPolygonPointNum+1, 0);
					}
					else
					{
						DrawPolygonOnVideo(m_ptPolygon, m_iPolygonPointNum, 0);
					}
				}
			}
		}	
	}
	else if (DrawType_Flux == m_iDrawType)
	{
		if (IsInsideVideoStatic(point))
		{
			if (m_iFluxCout<=3)
			{
				m_iFluxCout++;
				m_ptFlux[m_iFluxCout-1] = (POINT)point;
				PolygonOnViedo(&m_ptFlux[m_iFluxCout-1], 1);
				DrawPolygonOnVideo(m_ptFlux, m_iFluxCout, 0);
			}
		}
	}
	CDialog::OnLButtonUp(nFlags, point);
}

void CLS_VideoViewForDraw::OnLButtonDown(UINT nFlags, CPoint point)
{
	// TODO: Add your message handler code here and/or call default
	if ((DrawType_tripwire == m_iDrawType)||(DrawType_Crowd == m_iDrawType))
	{
		if (IsInsideVideoStatic(point))
		{
			m_ptPolygon[0] = (POINT)point;
			PolygonOnViedo(&m_ptPolygon[0], 1);
			m_iPolygonPointNum = 1;					
		}
	}
	CDialog::OnLButtonDown(nFlags, point);
}

void CLS_VideoViewForDraw::OnMouseMove(UINT nFlags, CPoint point)
{
	// TODO: Add your message handler code here and/or call default

	if ((DrawType_tripwire == m_iDrawType)||(DrawType_Crowd == m_iDrawType))
	{
		if (IsInsideVideoStatic(point))
		{
			if (m_iPolygonPointNum == 1)
			{
				m_ptPolygon[1] = (POINT)point;
				PolygonOnViedo(&m_ptPolygon[1], 1);

				if (DrawType_tripwire == m_iDrawType)
				{					
					DrawPolygonOnVideo(m_ptPolygon, 2, 0);
				}
				else if (DrawType_Crowd == m_iDrawType)
				{
					m_rcCrowd.left = m_ptPolygon[0].x;
					m_rcCrowd.top = m_ptPolygon[0].y;
					m_rcCrowd.right = m_ptPolygon[1].x;
					m_rcCrowd.bottom = m_ptPolygon[1].y;
					DrawRectgonOnVideo(&m_rcCrowd, 1);
				}				
			}
		}
	}

	CDialog::OnMouseMove(nFlags, point);
}

void CLS_VideoViewForDraw::SetDrawType( int _iDrawType, int _iMaxPointCount )
{
	m_iDrawType = _iDrawType;
	m_iMaxPointCount = _iMaxPointCount;
}

BOOL CLS_VideoViewForDraw::IsInsideVideoStatic( POINT _pt )
{
	RECT rcVideo = {0};
	m_staticVideoShow.GetWindowRect(&rcVideo);
	ScreenToClient(&rcVideo);

	if (_pt.x <= rcVideo.right && _pt.x >= rcVideo.left)
	{
		if (_pt.y <= rcVideo.bottom && _pt.y >= rcVideo.top)
		{
			return TRUE;
		}
	}

	return FALSE;
}

void CLS_VideoViewForDraw::DrawPolygonOnVideo( POINT *_ptArr, int _iPointCnt, int _iEnableArrow /* = 0*/)
{
	if (NetClient_DrawPolyOnLocalVideo)
	{
		int iRet = NetClient_DrawPolyOnLocalVideo(m_uiConnID, _ptArr, _iPointCnt, _iEnableArrow);
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL," CH%d-%d","NetClient_DrawPolyOnLocalVideo(%u,,,)",m_iChannelNO, m_iStreamNO, m_uiConnID);
		}
	}
}

void CLS_VideoViewForDraw::DrawRectgonOnVideo(RECT *_ptArr, int _iPointCnt)
{
	if (NetClient_DrawRectOnLocalVideo)
	{
		int iRet = NetClient_DrawRectOnLocalVideo(m_uiConnID, _ptArr, _iPointCnt);
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL," CH%d-%d","NetClient_DrawRectOnLocalVideo(%u,,,)",m_iChannelNO, m_iStreamNO, m_uiConnID);
		}
		else
		{
			AddLog(LOG_TYPE_SUCC," CH%d-%d","NetClient_DrawRectOnLocalVideo(%u,,,)",m_iChannelNO, m_iStreamNO, m_uiConnID);
		}
	}
}

int CLS_VideoViewForDraw::SetPointRegionParam( char* _pcPointBuf, int* _piPointNum, int *_piDirection, BOOL _blGetPoitByThousandPrecent/* = FALSE*/ )
{
	if (_pcPointBuf && _piPointNum && _piDirection)
	{
		m_pcPointBuf = _pcPointBuf;
		m_piPointNum = _piPointNum;
		m_piDirection = _piDirection;
	}
	else
	{
		return -1;
	}

	m_blGetPoitByThousandPrecent = _blGetPoitByThousandPrecent;
	return 0;
}

int	CLS_VideoViewForDraw::GetPointCoordirate(int *_piLeft, int *_piTop, int *_piRight, int *_piBottom)
{
	if (_piLeft && _piTop && _piRight && _piBottom)
	{
		*_piLeft = m_iLeft;
		*_piTop = m_iTop;
		*_piRight = m_iRight;
		*_piBottom = m_iBottom;
	}
	else
	{
		return -1;
	}

	return 0;
}

int CLS_VideoViewForDraw::SetAbandumRegionParam( char* _pcMainRegionBuf, char* _pcSubRegoinBuf[MAX_AB_SUBREGOIN_NUM])
{
	if (_pcMainRegionBuf && _pcSubRegoinBuf)
	{
		m_pcMainRegoinBuf = _pcMainRegionBuf;
		for (int i=0; i<MAX_AB_SUBREGOIN_NUM; i++)
		{
			m_pcSubRegoinBuf[i] = _pcSubRegoinBuf[i];
		}
	}
	else
	{
		return -1;
	}

	return 0;
}

void CLS_VideoViewForDraw::OnBnClickedButtonNextregion()
{
	// TODO: Add your control notification handler code here
	if (m_iABDrawIndex <= 3)
	{
		m_iABDrawIndex++;
	}
}

void CLS_VideoViewForDraw::OnBnClickedButtonClearregion()
{
	// TODO: Add your control notification handler code here
	// Draw two empty dots to clear
	POINT ptNULL[2] = {0};
	DrawPolygonOnVideo(ptNULL, 2, 0);
	
	if (DrawType_abandum == m_iDrawType)
	{
		m_iABDrawIndex = -1; //Restore to main region
		memset(m_ptMainRegion, 0, sizeof(m_ptMainRegion));
		m_iMainRegionPointNum = 0;
		memset(m_ptSubRegion, 0, sizeof(m_ptSubRegion));
		for (int i=0; i<MAX_AB_SUBREGOIN_NUM; i++)
		{
			m_iSubRegionPointNum[i] = 0;
		}
	}
	else if (DrawType_Crowd == m_iDrawType)
	{
		RECT rcNull = {0};
		DrawRectgonOnVideo(&rcNull, 1);
	}
	else
	{
		m_iDirection = 0;
		m_iPolygonPointNum = 0;
		m_iFluxCout = 0;
		memset(&m_ptPolygon, 0, sizeof(m_ptPolygon));
	}
}

void CLS_VideoViewForDraw::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_BUTTON_DrawOK, IDS_CONFIG_VCA_DRAWING_OK);
	SetDlgItemTextEx(IDC_BUTTON_ClearRegion, IDS_CONFIG_VCA_DRAWING_CLEAR);
	SetDlgItemTextEx(IDC_BUTTON_NextRegion, IDS_CONFIG_VCA_DRAWING_NEXT);
}

//Solve the problem that the area frame is not drawn when the key area dialog is initialized 20161229
void CLS_VideoViewForDraw::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CDialog::OnShowWindow(bShow, nStatus);
	if (DrawType_Crowd == m_iDrawType)
	{
		DrawRectgonOnVideo( &m_rcCrowd, 1);
	}
	else
	{
		memset(&m_rcCrowd, 0, sizeof(RECT));
		DrawRectgonOnVideo( &m_rcCrowd, 1);
	}

	ShowVcaWldRegion();
}

//Solve the problem that the area frame is not drawn when the key area dialog is initialized 20161229
void CLS_VideoViewForDraw::InitCrowdRect(RECT _tRect)
{
	m_rcCrowd = _tRect;
}

void CLS_VideoViewForDraw::SetVcaWldRegion(WaterPresetInfo* _ptPresetInfo)
{
	if (NULL == _ptPresetInfo)
	{
		return;
	}

	memcpy(&m_tWldPresetInfo, _ptPresetInfo, sizeof(WaterPresetInfo));
}

void CLS_VideoViewForDraw::ShowVcaWldRegion()
{
	if (0 == m_tWldPresetInfo.iSize)
	{
		return;
	}

	RECT tRect[3] = {0};
	memcpy(&tRect[0], &m_tWldPresetInfo.rcGaugeRect, sizeof(RECT));
	memcpy(&tRect[1], &m_tWldPresetInfo.rcGaugeLine, sizeof(RECT));
	memcpy(&tRect[2], &m_tWldPresetInfo.rcAssistRect, sizeof(RECT));
	tRect[1].left -= 2;
	tRect[1].top += 2;
	tRect[1].right += 2;
	tRect[1].bottom -= 2;
	DrawRectgonOnVideo(tRect, 3);	
}

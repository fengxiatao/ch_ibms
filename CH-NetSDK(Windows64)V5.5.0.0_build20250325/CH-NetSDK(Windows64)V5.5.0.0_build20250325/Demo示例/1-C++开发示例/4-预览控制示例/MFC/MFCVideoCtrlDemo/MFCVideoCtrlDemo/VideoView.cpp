#include "StdAfx.h"
#include "VideoView.h"
#include "NVSSDK_INTERFACE.h"
#include "Resource.h"
#include <commctrl.h>
#include <share.h>
#include <bitset>
#include "CommonFun.h"

#define MULTIPLY_SCREEN 0
#define SINGLE_SCREEN 1
#define FULL_SCREEN 2
/*******************************/
#define DRAG_TYPE_NONE			0
#define DRAG_TYPE_3D_LOCATE		1
#define	DRAG_TYPE_EZOOM			2
#define DRAG_TYPE_VIDEO_COVER		3
#define DRAG_TYPE_MOTION_DETECT		4

#define SHOW_CAP_NONE					0x0
#define SHOW_CAP_BITRATE				0x1
#define SHOW_CAP_MOTION_DETECT			0x2
#define SHOW_CAP_ALL					0xFFFFFFFF

#define WM_INPUT_CHANNELTALKING_END WM_USER+5000           //InputChannelTalk End Okay

//The entire motion detection area is divided into 18*22 matrix
#define DETECT_ROW_NUM 18
#define DETECT_COLUMN_NUM 22

static HWND s_hViewWnd;

#define TENTHOUSAND_RATE 10000

// Basic parameters
//bit0 defogging: 0-not supported, 1-supported defogging
//bit1 New 3D positioning protocol: 0-not supported, 1-supported
//bit2 supports compass: 0-not supported, 1-supported
enum ABILITY_TYPE_BASSPARA
{
	ABILITY_TYPE_BASEPARA_WIPEFOG = 0, //The 0th bit represents whether defogging is supported
	ABILITY_TYPE_BASEPARA_NEW3D = 1, //The first digit represents whether the new 3D positioning protocol is supported
	ABILITY_TYPE_BASEPARA_COMPASS = 2 //The second digit represents whether the compass is supported
};

CLS_VideoView::CLS_VideoView(void)
{
	m_iID = -1;
	m_uColor = GetSysColor(COLOR_BTNFACE);
	m_uConnID = -1;
	m_iDragType = DRAG_TYPE_NONE;
	m_uShowCaps = SHOW_CAP_NONE;
	m_iVideoCoverIndex = -1;
	memset(&m_rcDrag,0xff,sizeof(RECT));
	m_bIsOpen3DLocation = false;
	m_iChannelNo = -1;
	m_iLogonID = -1;
	m_iStreamNo = -1;
}

CLS_VideoView::~CLS_VideoView(void)
{

}

BEGIN_MESSAGE_MAP(CLS_VideoView, CStatic)
	ON_WM_PAINT()
	ON_WM_LBUTTONDOWN()
	ON_WM_LBUTTONUP()
	ON_WM_MOUSEMOVE()
	ON_WM_MOUSELEAVE()
	ON_WM_LBUTTONDBLCLK()
END_MESSAGE_MAP()

CLS_VideoView* CLS_VideoView::CreateInstance( int _iID,CWnd* _pParent )
{
	RECT rcShow = {0};
	CLS_VideoView* pVideo = new CLS_VideoView();
	pVideo->Create(NULL,WS_CHILD | WS_VISIBLE | SS_NOTIFY /*| SS_GRAYRECT*/,rcShow,_pParent);
	if (pVideo)
	{
		pVideo->SetID(_iID); 
		pVideo->SetOwner(_pParent);
		s_hViewWnd = _pParent->GetSafeHwnd();
	}	
	return pVideo;
}

void CLS_VideoView::OnPaint()
{
	CPaintDC dc(this); 
	DrawRect(m_uColor,&dc);
}

void CLS_VideoView::SetConnID( unsigned int _uConnID, int _iChannelNo, int _iLogonID,int _iStreamType)
{
	m_uShowCaps = SHOW_CAP_NONE;
	m_iDragType = DRAG_TYPE_3D_LOCATE;
	m_iVideoCoverIndex = -1;
	m_uConnID = _uConnID;
	m_iChannelNo = _iChannelNo;
	m_iLogonID = _iLogonID;
	m_iStreamNo = _iStreamType;
}

void CLS_VideoView::DrawRect( COLORREF _uColor,CDC* _pDC)
{
	RECT rcShow = {0};
	GetClientRect(&rcShow);
	if (NULL == _pDC)
	{
 		_pDC = this->GetDC();
 		m_uColor = _uColor;
	}
	else
	{
		_pDC->FillSolidRect(&rcShow,RGB(0,0,0));
	}
	
	CBrush	brFrame(m_uColor);
 	InflateRect(&rcShow,1,1);
 	_pDC->FrameRect(&rcShow,&brFrame);
	ReleaseDC(_pDC);
}

void CLS_VideoView::On3DLocate()
{
	if (m_iDragType == DRAG_TYPE_3D_LOCATE)
	{
		m_iDragType = DRAG_TYPE_NONE;
	}
	else
	{
		m_iDragType = DRAG_TYPE_3D_LOCATE;
	}
}

void CLS_VideoView::OnLButtonDown(UINT nFlags, CPoint point)
{
	if (DRAG_TYPE_NONE != m_iDragType && m_bIsOpen3DLocation)
	{
		//AddLog(LOG_TYPE_MSG,"","OnLButtonDown(%d,%d)",point.x,point.y);
		m_rcDrag.left = point.x;
		m_rcDrag.top = point.y;
	}

	CStatic::OnLButtonDown(nFlags, point);
}

void CLS_VideoView::OnMouseMove(UINT nFlags, CPoint point)
{
	if (MK_LBUTTON == nFlags && DRAG_TYPE_NONE != m_iDragType && m_rcDrag.left >= 0 && m_bIsOpen3DLocation)
	{
		//AddLog(LOG_TYPE_MSG,"","OnMouseMove(%d,%d)",point.x,point.y);
		m_rcDrag.right = point.x;
		m_rcDrag.bottom = point.y;

		RECT rcVideo = {0};
		ClientToVideo(m_rcDrag,rcVideo);
		NetClient_DrawRectOnLocalVideo(m_uConnID,&rcVideo,1);

		TRACKMOUSEEVENT tme; 
		tme.cbSize = sizeof(TRACKMOUSEEVENT);
		tme.dwFlags = TME_LEAVE;
		tme.dwHoverTime = HOVER_DEFAULT;
		tme.hwndTrack = m_hWnd;
		TrackMouseEvent(&tme);
	}

	CStatic::OnMouseMove(nFlags, point);
}

void CLS_VideoView::OnLButtonUp(UINT nFlags, CPoint point)
{
	if (DRAG_TYPE_NONE != m_iDragType && m_rcDrag.left >= 0 && m_bIsOpen3DLocation)
	{
		//AddLog(LOG_TYPE_MSG,"","OnLButtonUp(%d,%d)",point.x,point.y);
		m_rcDrag.right = point.x;
		m_rcDrag.bottom = point.y;
		DrawVideoArea(m_rcDrag);
		memset(&m_rcDrag,0xff,sizeof(RECT));
	}

	CStatic::OnLButtonUp(nFlags, point);
}

void CLS_VideoView::OnMouseLeave()
{
	if (DRAG_TYPE_NONE != m_iDragType && m_rcDrag.left >= 0)
	{
		//AddLog(LOG_TYPE_MSG,"","OnMouseLeave(%d,%d,%d,%d)",m_rcDrag.left,m_rcDrag.top,m_rcDrag.right,m_rcDrag.bottom);
		DrawVideoArea(m_rcDrag);
		memset(&m_rcDrag,0xff,sizeof(RECT));
	}

	CStatic::OnMouseLeave();
}

int CLS_VideoView::ClientToVideo(RECT& _rcScreen,OUT RECT& _rcVideo)
{
	int iWidth = 0, iHeight = 0;
	int iRet = NetClient_GetVideoSize(m_iLogonID,m_iChannelNo,&iWidth,&iHeight,0);
	if (0 == iRet)
	{
		m_rcVideo.right = iWidth;
		m_rcVideo.bottom = iHeight;
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetVideoSize(%d,%d,%d,%d,%d)"
			,m_iLogonID, m_iChannelNo,iWidth,iHeight,0);
	}

	if (0 == m_rcVideo.right || 0 == m_rcVideo.bottom)
	{
		memset(&_rcVideo,0,sizeof(RECT));
		return -1;
	}

	_rcVideo.left = min(_rcScreen.left,_rcScreen.right);
	_rcVideo.top = min(_rcScreen.top,_rcScreen.bottom);
	_rcVideo.right = max(_rcScreen.left,_rcScreen.right);
	_rcVideo.bottom = max(_rcScreen.top,_rcScreen.bottom);

	int iSWidth = 0; 
	int iSHeight = 0;
	RECT rcClient = {0};
	GetClientRect(&rcClient);
	_rcVideo.left = (_rcVideo.left*iWidth+rcClient.right/2)/rcClient.right;
	_rcVideo.top = (_rcVideo.top*iHeight+rcClient.bottom/2)/rcClient.bottom;
	_rcVideo.right = (_rcVideo.right*iWidth+rcClient.right/2)/rcClient.right;
	_rcVideo.bottom = (_rcVideo.bottom*iHeight+rcClient.bottom/2)/rcClient.bottom;	

	return 0;
}

int CLS_VideoView::DrawVideoArea(RECT& _rcDrag)
{
	RECT rcVideo = {0};
	ClientToVideo(_rcDrag,rcVideo);
	switch(m_iDragType)
	{
	case DRAG_TYPE_3D_LOCATE:
		{
			New3DLocate(_rcDrag);
			NetClient_DrawRectOnLocalVideo(m_uConnID,NULL,0);
		}
		break;
	default:
		break;
	}

	return 0;
}


void CLS_VideoView::New3DLocate(RECT& _rcVideo)
{
	int iLogonID = m_iLogonID;
	int iChannelNo = m_iChannelNo;

	Locate3DPosition t3dInfo = {0}; 
	t3dInfo.iBufSize = sizeof(t3dInfo);

	CRect rcShow;
	GetClientRect(&rcShow);
	CRect rcDraw = _rcVideo; //The area, point or rectangle drawn on the video

	//Need to convert the coordinates to ten thousand points
	if (rcDraw.left == rcDraw.right && rcDraw.top == rcDraw.bottom)	
	{ // draw 1 point
		t3dInfo.iPointNum = 1;
		t3dInfo.tPoint[0].iX = rcDraw.left*TENTHOUSAND_RATE/rcShow.Width();
		t3dInfo.tPoint[0].iY = rcDraw.top*TENTHOUSAND_RATE/rcShow.Height();
	}
	else // draw a rectangle
	{
		t3dInfo.iPointNum = 2;
		t3dInfo.tPoint[0].iX = rcDraw.left*TENTHOUSAND_RATE/rcShow.Width();
		t3dInfo.tPoint[0].iY = rcDraw.top*TENTHOUSAND_RATE/rcShow.Height();
		t3dInfo.tPoint[1].iX = rcDraw.right*TENTHOUSAND_RATE/rcShow.Width();
		t3dInfo.tPoint[1].iY = rcDraw.bottom*TENTHOUSAND_RATE/rcShow.Height();
	}

	int iRet = NetClient_SendCommand(iLogonID, COMMAND_ID_3D_POSITION, iChannelNo, &t3dInfo, sizeof(t3dInfo));
	if(0 != iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","3D NetClient_SendCommand(%d,%d)",iLogonID, iChannelNo);
	}
}
#pragma once
#include "afxwin.h"

// CLS_VideoVideoForDraw dialog
#define		MAX_POINTBUF_LEN		1024

typedef	enum __tagEDrawType
{
	DrawType_invalid = -1,
	DrawType_tripwire = 0,
	DrawType_perimeter,
	DrawType_facerec,
	DrawType_abandum,
	DrawType_track,
	DrawType_IllegalPark,
	DrawType_Crowd,
	DrawType_Flux,
	DrawType_GaugePoint,
}EDrawType;

#define MAX_AB_SUBREGOIN_NUM	3
#define MAX_TRACK_POINT_NUM		4

#define INNER_MAX_POINT_COUNT	8

class CLS_VideoViewForDraw : public CDialog
{
	DECLARE_DYNAMIC(CLS_VideoViewForDraw)

public:
	CLS_VideoViewForDraw(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_VideoViewForDraw();
	virtual void OnLanguageChanged(int _iLanguage);
// Dialog Data
	enum { IDD = IDD_DLG_VIDEOVIEW_DRAWLINES };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

private:
	
	unsigned	int m_uiConnID;	
	int		m_iLogonID;
	int		m_iChannelNO;
	int		m_iStreamNO;
	POINT	m_ptPolygon[VCA_MAX_POLYGON_POINT_NUM];					//	Use this array of points except item leftovers
	int		m_iPolygonPointNum;
	int		m_iDirection;
	int		m_iDrawType;
	int		m_iMaxPointCount;

	RECT   m_rcCrowd;
	POINT  m_ptFlux[4];
	int    m_iFluxCout;

	//	Suitable for tripwire, perimeter, face, tracking, traffic statistics
	char*	m_pcPointBuf;
	int*	m_piPointNum;
	int*	m_piDirection;

	// item left
	char*	m_pcMainRegoinBuf;
	char*   m_pcSubRegoinBuf[MAX_AB_SUBREGOIN_NUM];
	int		m_iABDrawIndex;
	int		m_iMainRegionPointNum;
	POINT	m_ptMainRegion[VCA_MAX_POLYGON_POINT_NUM];					
	int		m_iSubRegionPointNum[MAX_AB_SUBREGOIN_NUM];
	POINT	m_ptSubRegion[MAX_AB_SUBREGOIN_NUM][VCA_MAX_POLYGON_POINT_NUM];	

	void	StartPlay();
	void	StopPlay();
	void    UI_UpdateDialog();
	//The return value is used to determine whether to draw the video window range
	BOOL    PolygonOnViedo(POINT *_ptArr, int _iPointCnt);
	BOOL	IsInsideVideoStatic(POINT _pt);

	// _iEnableArrow meaning: control the display mode by bit, 0x01: arrow, 0x02: Add mode, keep other polygons, 0x04: only modify the polygon drawn last time
	void	DrawPolygonOnVideo(POINT *_ptArr, int _iPointCnt, int _iEnableArrow = 0);
	void	DrawRectgonOnVideo( RECT *_ptArr, int _iPointCnt);
	//// Draw different event graphics [12/15/2012 hanyongqiang]
	//void	DrawTripwire(CPoint point);
	//void	DrawPrimeter(CPoint point);
	//void	DrawFaceRec(CPoint point);
	//void	DrawAbandum(CPoint point);
	//void	DrawTrack(CPoint point);
	//Video region of interest
	int		m_iLeft; //Left margin - X coordinate of upper left corner
	int		m_iTop; //Top margin - Y coordinate of upper left corner
	int		m_iRight; //Right margin - X coordinate of lower right corner
	int		m_iBottom; //Bottom margin - Y coordinate of the lower right corner
	CStatic m_staticVideoShow;

	WaterPresetInfo m_tWldPresetInfo;

	//Use ten thousandths when getting coordinates
	BOOL	m_blGetPoitByThousandPrecent;

public:

	void	InitCrowdRect(RECT _tRect);
	void	SetDrawType(int _iDrawType, int _iMaxPointCount = INNER_MAX_POINT_COUNT);
	void	Init(int _iLogonID, int _iChnnelNO, int _iStreamNO);
	int		SetPointRegionParam(char* _pcPointBuf, int* _piPointNum, int *_piDirection, BOOL _blGetPoitByThousandPrecent = FALSE);
	int		SetAbandumRegionParam(char* _pcMainRegionBuf, char* _pcSubRegoinBuf[MAX_AB_SUBREGOIN_NUM]);
	int		GetPointCoordirate(int *_piLeft, int *_piTop, int *_piRight, int *_piBottom);
	void	OnMainNotify( int _iLogonID,int _wParam, void* _lParam, void*_iUserData );

	virtual BOOL OnInitDialog();
	afx_msg void OnBnClickedButtonDrawok();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnClose();
	afx_msg void OnLButtonUp(UINT nFlags, CPoint point);
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnBnClickedButtonNextregion();
	afx_msg void OnBnClickedButtonClearregion();

	void SetVcaWldRegion(WaterPresetInfo* _ptPresetInfo);
	void ShowVcaWldRegion();
};


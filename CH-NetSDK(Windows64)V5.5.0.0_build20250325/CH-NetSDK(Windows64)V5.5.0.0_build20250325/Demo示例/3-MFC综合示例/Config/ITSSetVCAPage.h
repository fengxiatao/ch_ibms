#ifndef _ITS_SETVCA_PAGE_H
#define _ITS_SETVCA_PAGE_H

#include "../BasePage.h"
#include "afxwin.h"
#include "afxdtctl.h"
#include "../Preview/DeviceControl.h"
#include "../Preview/ActionControl.h"

typedef struct tagPolygon
{
	int iPointCount;
	int iIndex;
	POINT m_ptSet[VCA_MAX_POLYGON_POINT_NUM];
} POLYGON;
// CLS_ITSSetVCAPage dialog

//bool IfHaveInterSection(POINT _ptLine1Start, POINT _ptLine1End, POINT _ptLine2Start, POINT _ptLine2End);
//bool IfPointInPolygon(POINT _ptCur, POINT *_ptPolygon, int _iPtSum = 4);
//bool IfPolygonIntersect(POINT *_ptPolygon1, int _iPoly1, POINT *_ptPolygon2, int _iPoly2);
//bool IfPolygonInPolygon(POINT *_ptPolygon1, int _iPoly1, POINT *_ptPolygon2, int _iPoly2);

class CLS_ITSSetVCAPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ITSSetVCAPage)

public:
	CLS_ITSSetVCAPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_ITSSetVCAPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_ITS_SETVCA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnMainNotify(int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser);
	virtual void OnCancel();

public:
	void StartPlay();
	void StopPlay();

public: // The following function is from 2011 to November, the traffic ball project needs to be added
	void ShowVCADialog(void);
	void ShowITSDialog(void);
	int GotoPreset(int _iPreset, int _iDevAddress, int _iComNo);
	//Realize the quadrilateral in the video, the rectangle does not cross the control
	int AddOrDelPolygon(bool _bFlag = true); //Add or delete the previous graphic for later modification. true: increase false: delete Can only be deleted once.
	//So every time after drawing the graph, AddOrDelPolygon(true), to add a new graph to prevent the subsequent operate from modifying the graph just drawn
	int DrawITSQuadRangle(POINT *_ptPolygon, int _ptSum);//Modify the figure drawn last time, it becomes a quadrilateral wxl 2011-11-22
	int DrawRect(POINT *_ptRect); //Redraw the last drawn graphic, it becomes a rectangle wxl 2011-11-22 Note: It is redrawing, not adding, so before calling, add a graphic at will
	void ChangePoint(void);   //wxl 2011-11-22
	/*wxl Determine which area the current point is in, 0 means not inside any quadrilateral, 10 means other area inside quadrilateral 1,
	20 indicates other areas inside quadrilateral 2. . .
	11 means inside the first rectangle of quadrilateral 1, 12 means inside the second rectangle of quadrilateral 1. . .
	21 means inside the first rectangle of quadrilateral 2, and 22 means inside the second rectangle of quadrilateral 2. . .
	*/
	int WhichAreaOfCurPt(void);
	bool IfRectValidAndDraw(POINT *_ptRect);
	bool IfPolygonValidAndDraw(POINT *_ptPolygon, int _iPoly);
	//Set or get the coordinates displayed by the text box on the interface
	void SetCoorEditShow(POINT *_ptPoint, int _iN0, int flag);
	void GetCoorEditShow(POINT *_ptPoint, int _iN0, int flag);
	//The return value is used to determine whether to draw the video window range
	BOOL PolygonOnViedo(POINT *_ptArr, int _iPointCnt);
	BOOL IsInsideVideoStatic(POINT _pt);
	// _iEnableArrow meaning: control the display mode by bit, 0x01: arrow, 0x02: Add mode, keep other polygons, 0x04: only modify the polygon drawn last time

public:
	HWND m_hResetPlayerWnd;
	HWND m_hPlayWnd;
	CStatic *m_pstxtPlayWnd;
	POINT m_ptSet[VCA_MAX_POLYGON_POINT_NUM];
	int m_iPointCount;				
	int m_iCurPolyIndex;
	int m_iPolyCount;
	POINT m_ptCur;
	POLYGON m_MainPolygon;
	POLYGON m_SubPolygon[3];
	POINT m_ptAngle[2];
	int m_iAngle;
	double m_dArrowLen;					//  length of angle
	bool m_bMouseDown;
	bool m_bSetAngle;
	int m_iModifyPointIndex;            //  Modify the subscript of the point, which needs to be determined by comparing with all points
	unsigned long m_ulConnID;
	unsigned int m_uiConnID;
	int m_iWidth;
	int m_iHeight;  
	//wxl 2011-11-22 Traffic Ball Project
	int m_iCurDrawGrh;       //Drawing graphics 0 means no drawing, 1 means drawing a quadrilateral, 2 means drawing a rectangle
	CString m_strIP;
	unsigned short m_usRangeTimeStartH[MAX_PRESET_COUNT][MAX_AREA_COUNT];
	unsigned short m_usRangeTimeStartM[MAX_PRESET_COUNT][MAX_AREA_COUNT];
	unsigned short m_usRangeTimeEndH[MAX_PRESET_COUNT][MAX_AREA_COUNT];
	unsigned short m_usRangeTimeEndM[MAX_PRESET_COUNT][MAX_AREA_COUNT];

private:
	void UI_UpdateDialog();
	BOOL UI_UpdateDeviceType();
	BOOL UI_UpdateGraph();

private:
	CComboBox m_cboPreset;
	CComboBox m_cboArea;
	CEdit m_edtTime;
	CComboBox m_cboTimeRange;
	CDateTimeCtrl m_dtcFrom;
	CDateTimeCtrl m_dtcTo;
	CEdit m_edtPointx[4];
	CEdit m_edtPointy[4];
	CEdit m_edtParkTime;
	CEdit m_edtSensitivity;
	CButton m_btnReset;
	CButton m_btnSet;
	CEdit m_edtRectPointx[4][2];
	CEdit m_edtRectPointy[4][2];
	CComboBox m_cboParamArea;
	CButton m_btnParamReset;
	CButton m_btnParamSet;
	CButton m_btnExactFix;
	CStatic m_StxtPlayWnd;
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	CLS_DeviceControl m_tDevCtrl;
	char m_cDeviceType[64];
public:
	afx_msg void OnCbnSelchangeComboPreset();
	afx_msg void OnCbnSelchangeComboArea();
	afx_msg void OnCbnSelchangeComboTimerange();
	afx_msg void OnBnClickedButtonReset();
	afx_msg void OnBnClickedButtonSet();
	afx_msg void OnCbnSelchangeComboParamArea();
	afx_msg void OnBnClickedButtonParamReset();
	afx_msg void OnBnClickedButtonParamSet();
	afx_msg void OnBnClickedButtonParamExactfix();
	afx_msg void OnLButtonUp(UINT nFlags, CPoint point);
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnClose();
};
#endif

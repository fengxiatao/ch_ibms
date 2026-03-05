#ifndef _VIDEO_VIEW_H
#define _VIDEO_VIEW_H

#include "afxwin.h"
#include "Common/EZoomManager.h"
#include "CLS_DlgDecrpt.h"
class CLS_VideoView :
	public CStatic
{
public:
	static CLS_VideoView* CreateInstance(int _iID,CWnd* _pParent);
	~CLS_VideoView(void);
	DECLARE_MESSAGE_MAP()

public:
	int GetID(){return m_iID;}
	void SetID(int _iID){m_iID = _iID;}
	unsigned int GetConnID(){return m_uConnID;}
	void SetConnID(unsigned int _uConnID);
	void DrawRect(COLORREF _uColor,CDC* _pDC = NULL);
	bool SupportNew3D();
	void New3DLocate(RECT& _rcVideo);
	afx_msg void OnPaint();
	afx_msg void OnRButtonUp(UINT nFlags, CPoint point);
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnLButtonUp(UINT nFlags, CPoint point);
	afx_msg void OnMouseLeave();
	afx_msg void OnLButtonDblClk(UINT nFlags, CPoint point);
	afx_msg BOOL OnMouseWheel(UINT nFlags, short zDelta, CPoint pt);
	afx_msg void OnSize(UINT nType, int cx, int cy);

	void ChannelTalkNotify(int _iLogonID, int _iChannelNo, int _iTalkStatus);
	void SetVideoRotatePara(int _iRotationDirection, int _iRotationAngle, int _iMirrorType);
	int SetModelRule(int _iModeRule);
	int GetModelRule(){return m_iModeRule;};
	void ShowDecrypt(BOOL bShow = TRUE);

private:
	CLS_VideoView(void);	
	void On3DLocate();
	void OnNew3DLocate();
	void OnEZoom();
	void OnOriginEZoom();
	void OnVideoCoverArea();
	void OnVideoCoverAreaEx();
	void OnMotionDetectArea();
	void OnShowBitrate();
	void OnShowMotionDetect();
	void OnDisconnect();
	void OnAutoFocus();
	void OnAreaFocus();
	void OnChannelTalk();
	void OnInputTalkData();
	void OnVideoRotate180D();
	void OnClockwiseRotate90D();
	void OnAntiClockwiseRotate90D();
	void OnVideoHMirror();
	void OnVideoVMirror();
	bool StopTalk();
	int ClientToVideo(RECT& _rcScreen,OUT RECT& _rcVideo);
	int DrawVideoArea(RECT& _rcVideo);
	int _3DLocate(RECT& _rcVideo,BOOL _bDirection);
	int SetVideoCoverArea(RECT& _rcVideo);
	int SetVideoCoverAreaEx(RECT& _rcVideo);
	void SetVideoAreaFocus(RECT& _rcVideo);
	int GetMotionDetetionArea(RECT* _prcArea,int _iCount,RECT* _prcVideo = NULL);
	int SetMotionDetetionArea(RECT* _prcArea,int _iCount);
	int SetMotionDetetionArea(RECT& _rcVideo);
	int ShowMotionDetetionArea(RECT* _prcArea,int _iCount);
	static DWORD WINAPI ThreadInputChannelTalking(LPVOID pParam);
	afx_msg LRESULT OnChannelTalkEnd(WPARAM wParam, LPARAM lParam);
	int	CalcEZoomArea(CRect _tcTemp);
	int SetEZoomArea(RECT _rcArea);
	int MoveEZoom(int _iOffX, int _iOffY);
	void CenterWindow();
private:
	int m_iID;
	COLORREF m_uColor;
	unsigned int m_uConnID;
	unsigned int m_uShowCaps;
	int m_iDragType;
	RECT m_rcDrag;
	RECT m_rcVideo;
	int m_iVideoCoverIndex;
	int m_iVideoCoverIndexEx;
	CRect	m_rcEZoom;
	int     m_iModeRule;
	//绘制的状态，原窗口电子放大第一次选中一个框，第二次是拖拽鼠标
	int m_iDrawState;
	CPoint m_ptLast;
	CLS_DlgDecrpt *m_pDlgDecrypt;
	CRect m_rtDecrypt;
public:
	bool m_IsChannelTalk;
	bool m_IsInputTalkData;
};

#endif

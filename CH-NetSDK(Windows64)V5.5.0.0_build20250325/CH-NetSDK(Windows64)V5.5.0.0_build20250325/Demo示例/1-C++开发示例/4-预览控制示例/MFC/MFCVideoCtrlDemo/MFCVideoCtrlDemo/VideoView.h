#ifndef _VIDEO_VIEW_H
#define _VIDEO_VIEW_H

#include "afxwin.h"

class CLS_VideoView :public CStatic
{
public:
	static CLS_VideoView* CreateInstance(int _iID,CWnd* _pParent);
	~CLS_VideoView(void);
	DECLARE_MESSAGE_MAP()

public:
	int GetID(){return m_iID;}
	void SetID(int _iID){m_iID = _iID;}
	unsigned int GetConnID(){return m_uConnID;}
	void SetConnID(unsigned int _uConnID, int _iChannelNo, int _iLogonID, int _iStreamType);
	void DrawRect(COLORREF _uColor,CDC* _pDC = NULL);
	void New3DLocate(RECT& _rcVideo);
	afx_msg void OnPaint();
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnLButtonUp(UINT nFlags, CPoint point);
	afx_msg void OnMouseLeave();
	void ChannelTalkNotify(int _iLogonID, int _iChannelNo, int _iTalkStatus);
private:
	CLS_VideoView(void);	
	void On3DLocate();
	int ClientToVideo(RECT& _rcScreen,OUT RECT& _rcVideo);
	int DrawVideoArea(RECT& _rcVideo);
private:
	int m_iID;
	COLORREF m_uColor;
	unsigned int m_uConnID;
	unsigned int m_uShowCaps;
	int m_iChannelNo;
	int m_iLogonID;
	int m_iStreamNo;
	int m_iDragType;
	RECT m_rcDrag;
	RECT m_rcVideo;
	int m_iVideoCoverIndex;
public:
	bool m_bIsOpen3DLocation;
};

#endif

#pragma once

// CHeaderCtrlCl

class CHeaderCtrlClEX : public CHeaderCtrl
{
	DECLARE_DYNAMIC(CHeaderCtrlClEX)

public:
	CHeaderCtrlClEX();
	virtual ~CHeaderCtrlClEX();

protected:
	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnPaint();
	CStringArray m_HChar;
	CString m_Format; //Integer array of alignment type, 0 for left alignment, 1 for middle alignment, 2 for right alignment
	CBrush *m_pbrHeadBk;//List header background brush
public:
	float m_Height; //Height of the header, this is a multiple,
	int m_fontHeight; //font height
	int m_fontWith; //font width

	COLORREF m_color;//font color
	LRESULT OnLayout( WPARAM wParam, LPARAM lParam );

	/*
	Function name: DrawHeaderItemRect
	Function: Draw the rectangular area of the list header.
	Input parameters: _dc draws the DC used, _rtDraw draws the area,
	*/
	void DrawHeaderItemRect(CDC& _dc,CRect& _rtDraw);
};



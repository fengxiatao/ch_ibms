// HeaderCtrlCl.cpp : Implementation file
//

#include "stdafx.h"
#include "HeaderCtrlClEX.h"

// CHeaderCtrlCl

extern int g_iLanguage;

IMPLEMENT_DYNAMIC(CHeaderCtrlClEX, CHeaderCtrl)

CHeaderCtrlClEX::CHeaderCtrlClEX()
{
	m_Format = "";
	m_Height = 1;
	m_fontHeight = 15;
	m_fontWith = 0;
	m_pbrHeadBk = new CBrush();

}

CHeaderCtrlClEX::~CHeaderCtrlClEX()
{
	if (m_pbrHeadBk)
	{
		m_pbrHeadBk->DeleteObject();
		delete m_pbrHeadBk;
		m_pbrHeadBk = NULL; 
	}

}


BEGIN_MESSAGE_MAP(CHeaderCtrlClEX, CHeaderCtrl)
	ON_WM_PAINT()
	ON_MESSAGE(HDM_LAYOUT, OnLayout)
END_MESSAGE_MAP()

// CHeaderCtrlCl message handler

void CHeaderCtrlClEX::OnPaint()
{
	CPaintDC dc(this); // device context for painting
	// TODO: add message handler code
	// do not call CHeaderCtrl::OnPaint() for paint messages
	int nItem = GetItemCount();//Get how many units
	CString cstrTmp;
	for(int i = 0; i<nItem;i ++) 
	{ 
		CRect tRect;
		GetItemRect(i,&tRect);//Get the size of the Item
		CRect nRect(tRect);//Copy the size to the new container

		tRect =nRect;
 		nRect.left++;//The place to leave the dividing line

 		DrawHeaderItemRect(dc,nRect);

		dc.SetBkMode(TRANSPARENT);
		CFont nFont ,* nOldFont; 
		dc.SetTextColor(m_color);

		nOldFont = dc.SelectObject(&nFont);

		UINT nFormat = 1;
		if (m_Format[i]=='0')
		{
			nFormat = DT_LEFT;
			tRect.left+=3;
		}
		else if (m_Format[i]=='1')
		{
			nFormat = DT_CENTER;
		}
		else if (m_Format[i]=='2')
		{
			nFormat = DT_RIGHT;
			tRect.right-=3;
		}
		TEXTMETRIC metric;
		dc.GetTextMetrics(&metric);
		int ofst = 0;
		ofst = tRect.Height() - metric.tmHeight;
		tRect.OffsetRect(0,ofst/2);
		dc.DrawText(m_HChar[i],&tRect,nFormat);
		dc.SelectObject(nOldFont); 
		nFont.DeleteObject(); //Release the font
	} 

	CRect rtRect;
	CRect clientRect;
	GetItemRect(nItem - 1,rtRect);
	GetClientRect(clientRect);
	rtRect.left = rtRect.right+1;
	rtRect.right = clientRect.right;
	DrawHeaderItemRect(dc,rtRect);
}

void CHeaderCtrlClEX::DrawHeaderItemRect(CDC& _dc, CRect& _rtDraw )
{
	if (m_pbrHeadBk)
	{
		CRect nRect(_rtDraw);
		nRect.right--;
		nRect.bottom -= 2;
		_dc.FillRect(&nRect,m_pbrHeadBk);
	}
}

LRESULT CHeaderCtrlClEX::OnLayout( WPARAM wParam, LPARAM lParam )
{
	LRESULT lResult = CHeaderCtrlClEX::DefWindowProc(HDM_LAYOUT, 0, lParam); 
	HD_LAYOUT &hdl = *( HD_LAYOUT * )lParam; 
	RECT *prc = hdl.prc; 
	WINDOWPOS *pwpos = hdl.pwpos; 

	//The height of the header is 1.5 times the original height. If you want to dynamically modify the height of the header, set 1.5 as a global variable
	int nHeight = (int)(pwpos->cy * m_Height);
	pwpos->cy = nHeight; 
	prc->top = nHeight; 
	return lResult; 
}

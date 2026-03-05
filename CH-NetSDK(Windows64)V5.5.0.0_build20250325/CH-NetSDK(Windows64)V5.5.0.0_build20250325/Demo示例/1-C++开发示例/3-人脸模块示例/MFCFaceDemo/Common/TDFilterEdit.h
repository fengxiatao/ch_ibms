/**********************************************************************
Copyright (c) 2010
All rights reserved.
File name: TDFilterEdit. h
Abstract: Encapsulates the Edit class to implement character filtering
Version: 1.0
Author: liyunfei
Completion date: October 29, 2013
*********************************************************************/
#pragma once
#ifndef  TDEDITFILTERCHAR_H
#define  TDEDITFILTERCHAR_H

#define VK_PASTE 0x16

#define CHARSET_GB2312		0
#define CHARSET_UTF8		1

// TDDialog dialog
typedef BOOL (*WM_CHAR_NOTIFY)(UINT nChar, UINT nRepCnt, UINT nFlags, HWND nhwnd, CString cText, bool bHasChinese);

class TDFilterEdit : public CEdit
{
	DECLARE_DYNAMIC(TDFilterEdit)	

public:
	TDFilterEdit();
	virtual ~TDFilterEdit();

	DECLARE_MESSAGE_MAP()
	afx_msg void OnChar(UINT nChar, UINT nRepCnt, UINT nFlags);
	void OnRButtonDown(UINT nFlags, CPoint point);
	bool IsSpecialCharacters(UINT nChar);
public:
	//Set callback function
	void SetFilterCallBack(WM_CHAR_NOTIFY _wCharCallBack);

	WM_CHAR_NOTIFY	m_cbkCharCallBack;
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);

	int m_iCharSet;
	void SetCharSet(int _iCharSet){m_iCharSet = _iCharSet;}	//0-gb2312 1-utf-8

	bool m_blPaste;
	void SetCanPaste(bool _blPaste){m_blPaste = _blPaste;}

	//Maximum number of rows allowed
	int m_iMaxLineCOunt;
	void SetMaxLineCount(int iMaxLineCount){m_iMaxLineCOunt = iMaxLineCount;}

	//Limit the maximum number of rows
	void FormatLine();

	//Insert String
	void InsertText(CString strText);
};

#endif
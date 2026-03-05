#if !defined(AFX_NEULISTCTRL_H__B99644D0_C056_4A16_AF1C_06E2F3321668__INCLUDED_)
#define AFX_NEULISTCTRL_H__B99644D0_C056_4A16_AF1C_06E2F3321668__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// NeuListCtrl.h : header file
//

#define MESSAGE_NONE 909090
#define MESSAGE_SELECT 90
#define COLOR_CANCEL RGB(255,255,255)

#include <afxcoll.h>
#include <afxtempl.h>
#include "HeaderCtrlClEX.h"
//#include "../Config/CLS_VirtualBasePage.h"

enum enCONTROLTYPE { EDITBOX,COMBOBOX,LISTBOX,CHECKBOX,BUTTON,DATECTRL,NONE} ;

typedef void (CALLBACK *pEventHandler)(LPVOID lpVoid,int iIndex);
typedef void (CALLBACK *pClickItem)(LPVOID lpVoid, int _iIndex, int _iSubItem);//Click row event callback

struct ControlMessageInfo
{
	int m_iMessageID;
	pEventHandler fun;
	ControlMessageInfo()
	{
		m_iMessageID = MESSAGE_NONE;
	}
};
struct SubItemControlsInfo
{
	enCONTROLTYPE m_enControlType;
	int m_iItemIndex;
	CWnd * m_pWnd;
	COLORREF m_BKColor;
	SubItemControlsInfo()
	{
		m_enControlType = NONE;
		m_iItemIndex = -1;
		m_pWnd = NULL;
		m_BKColor = RGB(255,255,255);
	}
	CArray<ControlMessageInfo,ControlMessageInfo> m_arrControlMessageInfo;
};

/////////////////////////////////////////////////////////////////////////////
// CNeuListCtrl window

class CNeuListCtrl : public CListCtrl
{
// Construction
public:
	CNeuListCtrl();

// Attributes
public:

// Operations
public:
	CMap<DWORD , DWORD& , COLORREF , COLORREF&> MapItemColor;
public:
	afx_msg void OnNMCustomdraw(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeCombo();

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CNeuListCtrl)
	public:
	virtual BOOL DestroyWindow();
	virtual BOOL OnCmdMsg(UINT nID, int nCode, void* pExtra, AFX_CMDHANDLERINFO* pHandlerInfo);
	//}}AFX_VIRTUAL

// Implementation
public:
	void AttachEvent_HandlerForItemControl(int iIndex,UINT unMessage,pEventHandler fun);
	void ShowControls(BOOL bShow);//Control the display of the controls on the list ctrl is not displayed
	void EnableControls(BOOL bEnable);//The enable of controls on the control list ctrl is not enabled
	void SetControlBKColor(int index ,COLORREF col);
	virtual ~CNeuListCtrl();
	BOOL Create( DWORD dwStyle, const RECT& rect, CWnd* pParentWnd, UINT nID );
	void SetRowHeigt(int _iHeight);//Set row height
	int m_iRowHeight; //Row height
	CHeaderCtrlClEX    *m_pHeaderCtrl;
	void SetItemColor(DWORD iItem, COLORREF color);
	CString GetItemTextEx(int nItem ,int nSubItem ) const;
	// Generated message map functions
protected:
	CString m_strMinVal;
	//{{AFX_MSG(CNeuListCtrl)
	afx_msg void OnPaint();
	afx_msg BOOL OnEraseBkgnd(CDC* pDC);
	afx_msg void OnClick(NMHDR* pNMHDR, LRESULT* pResult);
	afx_msg void OnVScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);

	afx_msg void OnKeyDown(UINT nChar, UINT nRepCnt, UINT nFlags);
	//}}AFX_MSG
	afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
	CFont m_fnControl;//Control font
	CFont *m_pMainList;//Set the row height

	DECLARE_MESSAGE_MAP()
protected:
	CPtrArray *m_pControlPtrArray;
private:
	BOOL DetachControl(int index);
	int ControlIndexDuplicating(int iIndex);
	int m_iCurSelctedItemIndex;//The currently selected row
	int m_iSubItem;
	int m_iControlIDCounter;
	pClickItem m_pClickItemCallBack;
	void *m_pClickData;

	HWND m_hParent;

	BOOL OnNotify(WPARAM wParam, LPARAM lParam, LRESULT* pResult);
	/************************************************************************
	Function: StrToTime
	Description: Convert string to time.
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	CTime StrToTime(CString &cstrTime);

public:
	void SetParentHwnd(HWND _hWndParent);

	/************************************************************************
	Function: SetCurSelectedItem
	Description: Set the currently selected item
	Input: _iCurSelected currently selected item
	Output:
	Return:
	Others:
	************************************************************************/
	void SetCurSelectedItem(int _iCurSelected);
	/************************************************************************
	Function: GetCurSelectedItem
	Description: Get the currently selected item.
	Input:
	Output:
	Return: currently selected item
	Others:
	************************************************************************/
	int GetCurSelectedItem();

	/************************************************************************
	Function: SetHeaderHeight
	Description: Set header font height
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void SetHeaderHeight(float Height);

	/************************************************************************
	Function: PreSubclassWindow
	Description: Used to send messages to the listctrl table
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	virtual void PreSubclassWindow();

	/************************************************************************
	Function: AdjustControlsAccordingToHeaderWidth
	Description: Adjust the width of the control to fit the width of an item of listctrl
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void AdjustControlsAccordingToHeaderWidth(HD_NOTIFY * pNotify);

	/************************************************************************
	Function: GetItemTextEx
	Description: Get the content of an item
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	int GetItemTextEx(int nItem ,int nSubItem ,LPTSTR lpszText ,int nLen) const;

	/************************************************************************
	Function: MapControlsForSubItems
	Description: Set the value on the control to the list ctrl
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void MapControlsForSubItems();

	/************************************************************************
	Function: AddToControlsData
	Description: Add data to the control
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	BOOL AddToControlsData(int iIndex, CString strData);

	/************************************************************************
	Function: GetAndDisplayControlsData
	Description: Get the value on the control and display it on the list ctrl
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void GetAndDisplayControlsData();

	/************************************************************************
	Function: ResetContent
	Description: Empty combox content
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void ResetContent();

	/************************************************************************
	Function: ReleaseControls
	Description: release control
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void ReleaseControls();

	/************************************************************************
	Function: SetHeaderFontHW
	Description: //Set the header font size
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void SetHeaderFontHW(int nHeight,int nWith); 

	/************************************************************************
	Function: SetItemControl
	Description: Add controls such as combox to listctrl
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	void SetItemControl(enCONTROLTYPE enControlType,int iIndex);//

	/************************************************************************
	Function: DeleteColumn
	Description: delete column
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	BOOL DeleteColumn(int nCol);

	/************************************************************************
	Function: InsertColumn
	Description: Overload insert column
	Output:
	Return:
	Others:
	************************************************************************/
	int InsertColumn(_In_ int nCol, _In_ const LVCOLUMN* pColumn);
	int InsertColumn(_In_ int nCol, _In_z_ LPCTSTR lpszColumnHeading,
		_In_ int nFormat = LVCFMT_LEFT, _In_ int nWidth = -1, _In_ int nSubItem = -1);

	/************************************************************************
	Function: DeleteAllColumns
	Description: delete all columns
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	BOOL DeleteAllColumns();

	/************************************************************************
	Function: GetColumnCount
	Description: get the number of columns
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	int GetColumnCount() const;

	/************************************************************************
	Function: MoveControlToFocusItem
	Description: Move the control to be displayed to the specified row
	Input: _iIndex specifies the row
	Output:
	Return:
	Others:
	************************************************************************/
	void MoveControlToFocusItem(int _iIndex);

	/************************************************************************
	Function: OnMouseWheel
	Description: Handle scroll wheel events
	Input:
	Output:
	Return:
	Others:
	************************************************************************/
	afx_msg BOOL OnMouseWheel(UINT nFlags, short zDelta, CPoint pt);

	void SetClickItemCallBack(pClickItem _callbackFunc, void* _pData)
	{
		m_pClickItemCallBack = _callbackFunc;
		m_pClickData = _pData;
	};
	/************************************************************************
	Function: SetClickItemCallBack
	Description: Handle click table row event
	************************************************************************/

	/************************************************************************
	Function: SetLimitTextLength
	Description: Limit Edit input length
	Input: _iColumn: column number, _iLength: limit length
	Output:
	Return:
	Others:
	************************************************************************/
	void SetLimitTextLength(int _iColumn, int _iLength);
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_NEULISTCTRL_H__B99644D0_C056_4A16_AF1C_06E2F3321668__INCLUDED_)

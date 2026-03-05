#ifndef EDIT_ONLYNUMBER
#define EDIT_ONLYNUMBER

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// Edit_OnlyNumber.h : header file
//


#define ONLYNUMBER  0    // can only enter numbers
#define FILTERNUMBER 1     // filter numbers
#define FILTERCHAR 2       // filter letters
#define FILTERCH 3         // filter GB2312 characters
#define FILTERVIOCHAR 4    // filter illegal characters
#define NOFILTER    5      // do not filter
#define DECIMALNUMBER 6	//Can enter decimal point
#define NEGATIVENUMBER 7	//Can enter positive and negative numbers
#define ONLY_NUM_ADN_CHAR 8	// can enter numbers and characters
#define FILTERVIOCHAR_EX 9

#define FILTERVIOCHAR_HTTP	10 //Filter illegal characters in Http path
#define FILTERVIOCHAR_FTP_PSW 11//Filter the FTP password of the DOCKing module
#define FILTERVIOCHAR_HTTP_TRUST_WAY 12//The http path of Chengdao
#define FILTERKFK_CONFIG       13//Kafka configuration, filter letters, numbers and _

/////////////////////////////////////////////////////////////////////////////
// CEdit_OnlyNumber window

class CEdit_OnlyNumber : public CEdit
{
public:
	CEdit_OnlyNumber();
	/************************************************************************
	Function:      setFilter
	Description:   set filter type
	Input:         nChar : type code
				   _blLimitLen: Whether to limit the length of GB2312 characters and characters
	Output:        
	Return:        0 success 0 > failure
	Others:            
	************************************************************************/
	void setFilter(UINT _nChar = 0, bool _blLimitLen = false,bool _blCanCopy = false);

	UINT m_iFilterType;

	virtual ~CEdit_OnlyNumber();

protected:

	afx_msg void OnChar(UINT nChar, UINT nRepCnt, UINT nFlags);
	void OnRButtonDown(UINT nFlags, CPoint point);
	void OnContextMenu(CWnd* pWnd, CPoint pos);

	int m_iUnicodeMark;
	int m_iUnicodeMark1;
	int m_iUnicodeMark2;
	DECLARE_MESSAGE_MAP()
private:

	/************************************************************************
	Function:      FilterOverInput
	Description:   Filter too much input
	Input:         _blIsDoubleChar : Whether it is a double-byte GB2312 characters
	Output:        
	Return:        0 success 0 > failure      
	Others:            
	************************************************************************/
	int FilterOverInput(bool _blIsDoubleChar);

	void GetCopyString(CString &_fromClipboard);

	BOOL CheckString( CString str );

	BOOL CheckString_Ex( CString str );

	BOOL CheckString_Ex1( CString str );

	BOOL CheckString_Ex2( CString str );

	bool m_blLimitLen;
	bool m_blCanCopy;
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(EDIT_ONLYNUMBER)

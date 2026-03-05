
#include "stdafx.h"
#include "TDFilterEdit.h"
#include "CommonFun.h"

IMPLEMENT_DYNAMIC(TDFilterEdit, CEdit)

TDFilterEdit::TDFilterEdit()
{
	m_cbkCharCallBack = NULL;
	m_iCharSet = CHARSET_GB2312;
	m_blPaste = true;
	m_iMaxLineCOunt = 0;
}
//---------------------------------------------------------------------------
TDFilterEdit::~TDFilterEdit()
{
}
//---------------------------------------------------------------------------
BEGIN_MESSAGE_MAP(TDFilterEdit, CEdit)
	ON_WM_CHAR()
	ON_WM_RBUTTONDOWN()
	ON_WM_LBUTTONDOWN()
END_MESSAGE_MAP()

//---------------------------------------------------------------------------
void UTF8ToAnsi( const char* _pstrIn,CString &_strOut)
{
	WCHAR* strSrc    = NULL;
	TCHAR* szRes    = NULL;

	int i = MultiByteToWideChar(CP_UTF8, 0,_pstrIn, -1, NULL, 0);

	strSrc = new WCHAR[i+1];
	if (strSrc == NULL){

		return;
	}
	MultiByteToWideChar(CP_UTF8, 0,_pstrIn, -1, strSrc, i);

	i = WideCharToMultiByte(CP_ACP, 0, strSrc, -1, NULL, 0, NULL, NULL);

	szRes = new TCHAR[i+1];
	if (szRes == NULL){
		delete[] strSrc;
		return;
	}
	WideCharToMultiByte(CP_ACP, 0, strSrc, -1, szRes, i, NULL, NULL);

	_strOut = szRes;

	delete[] strSrc;
	delete[] szRes;
}

void AnsiToUTF8( const char* _pstrIn,CString &_strOut)
{
	WCHAR* strSrc    = NULL;
	TCHAR* szRes    = NULL;

	int i = MultiByteToWideChar(CP_ACP, 0,_pstrIn, -1, NULL, 0);

	strSrc = new WCHAR[i+1];
	if (strSrc == NULL){

		return;
	}
	MultiByteToWideChar(CP_ACP, 0,_pstrIn, -1, strSrc, i);

	i = WideCharToMultiByte(CP_UTF8, 0, strSrc, -1, NULL, 0, NULL, NULL);

	szRes = new TCHAR[i+1];
	if (szRes == NULL){
		delete[] strSrc;
		return;
	}
	WideCharToMultiByte(CP_UTF8, 0, strSrc, -1, szRes, i, NULL, NULL);

	_strOut = szRes;

	delete[] strSrc;
	delete[] szRes;
}

CStringW AnsiToUnicode(char* pSrc)
{
	CStringW strContent;
	if (NULL == pSrc)
	{
		return strContent;
	}

	int wcsLen = ::MultiByteToWideChar(CP_ACP, 0, pSrc, (int)strlen(pSrc), NULL, 0);
	wchar_t* wszString = new wchar_t[wcsLen + 1];
	if (NULL == wszString)
	{
		return strContent;
	}

	::MultiByteToWideChar(CP_ACP, NULL, pSrc, (int)strlen(pSrc), wszString, wcsLen);
	wszString[wcsLen] = '\0';
	strContent = wszString;
	delete [] wszString;
	wszString = NULL;

	return strContent;
}

CStringA UnicodeToAnsi(wchar_t* pSrc)
{
	CStringA strContent;
	if (NULL == pSrc)
	{
		return strContent;
	}

	int aLen = ::WideCharToMultiByte(CP_ACP, 0, pSrc, (int)wcslen(pSrc), NULL, 0, NULL, NULL);
	char* pChar = new char[aLen + 1];
	if (NULL == pChar)
	{
		return strContent;
	}
	::WideCharToMultiByte(CP_ACP, 0, pSrc, (int)wcslen(pSrc), pChar, aLen, NULL, NULL);
	pChar[aLen] = '\0';
	strContent = pChar;
	delete [] pChar;
	pChar = NULL;

	return strContent;
}

int SplitStringToIntArray(CString strStingSource,TCHAR* tcSplitChar,CStringArray& aryContent)
{
	int iArrayCount = 0;

	if (NULL == tcSplitChar)
	{
		return iArrayCount;
	}
	aryContent.RemoveAll();
	CString strTem;
	try
	{
		while(AfxExtractSubString(strTem,strStingSource,iArrayCount,tcSplitChar[0]))
		{
			aryContent.Add(strTem);
			++iArrayCount;
		}
	}
	catch (...)
	{
		iArrayCount = 0;
	}
	return iArrayCount;
}

CString GetClipboardString()
{
	CString strClipboard;
	if (OpenClipboard(NULL))
	{
		HANDLE hData = GetClipboardData(CF_TEXT);
		if (NULL != hData)
		{
			char* pBuffer = (char*)GlobalLock(hData);
			strClipboard = pBuffer;
			GlobalUnlock(hData);
		}
		CloseClipboard();
	}
	return strClipboard;
}

void SetClipboardString(CString strContent)
{
	if (OpenClipboard(NULL))
	{
		EmptyClipboard();
		HGLOBAL clipBuffer = GlobalAlloc(GMEM_DDESHARE, strContent.GetLength() + 1);
		char* pBuffer = (char*)GlobalLock(clipBuffer);
		if (NULL != pBuffer)
		{
			strcpy_s(pBuffer, strContent.GetLength() + 1, strContent.GetBuffer());
		}
		GlobalUnlock(clipBuffer);
		SetClipboardData(CF_TEXT, clipBuffer);
		// 		GlobalFree(clipBuffer);
		CloseClipboard();
	}
}

//---------------------------------------------------------------------------
void TDFilterEdit::OnChar(UINT nChar, UINT nRepCnt, UINT nFlags)
{
	if(VK_PASTE == nChar && !m_blPaste)
	{
		return;
	}

	int iMaxLen  = GetLimitText();
	CStringA strText;
	GetWindowText(strText);
	int iLen = 1;
	if (IsDBCSLeadByte(nChar))
	{
		iLen = 2;
	}
	int iStartChar,iEndChar;
	GetSel(iStartChar,iEndChar); 

	if (strText.GetLength()+iLen > iMaxLen && nChar != VK_BACK && nChar != VK_PASTE && iStartChar == iMaxLen)
	{
		return;
	}

	//Shearboard data
	CString strClipboard;
	if (m_iMaxLineCOunt > 0)
	{
		//Calculate how many more lines can be entered
		int iSurplusLine = m_iMaxLineCOunt;
		CStringW strU16Text = AnsiToUnicode(strText.GetBuffer());
		strText.ReleaseBuffer();
		//Remove the selected interval for calculation
		if (iEndChar - iStartChar > 0)
		{
			strU16Text.Delete(iStartChar, iEndChar - iStartChar);
		}
		CStringA strContent = UnicodeToAnsi(strU16Text.GetBuffer());
		strU16Text.ReleaseBuffer();
		strContent.Replace(_T("\r\n"), _T("\n"));
		CStringArray aryContent;
		int iLineCount = SplitStringToIntArray(strContent, _T("\n"), aryContent);
		iSurplusLine = m_iMaxLineCOunt - iLineCount + 1;//+ 1 is because you can continue to enter in this line when it is full, which is equivalent to 1 line
		if (iSurplusLine < 1)
		{
			iSurplusLine = 1;
		}

		if (iSurplusLine<=1&&(VK_RETURN==nChar || 0x0A==nChar))//0x0A Line feed key ctrl+enter
		{
			//If the number of lines is full and the input is Enter, return directly
			return;
		}

		if (VK_PASTE == nChar)
		{
			//For the copied content, the redundant lines in the clipboard should be processed
			strClipboard = GetClipboardString();
			CString strTempClipboard = strClipboard;
			strTempClipboard.Replace(_T("\r\n"), _T("\n"));
			iLineCount = SplitStringToIntArray(strTempClipboard, _T("\n"), aryContent);
			if (iLineCount > iSurplusLine)
			{
				CString strTargetClipboard;
				for (int iIdx = 0; iIdx < iSurplusLine; ++iIdx)
				{
					CString strTemp = aryContent.GetAt(iIdx);
					strTargetClipboard += strTemp;
					if (iIdx < iSurplusLine -1)
					{
						strTargetClipboard += _T("\r\n");
					}
				}
				SetClipboardString(strTargetClipboard);
			}
		}
	}
	if (m_cbkCharCallBack)
	{
		bool blHasChinese = false;
		if(nChar == VK_PASTE)
		{
			CEdit::OnChar(nChar, nRepCnt, nFlags);	
			CString strTextTemp;
			GetWindowText(strTextTemp);
			LPCTSTR str = strTextTemp;
			for(int i = 0; str[i]; ++i)
			{
				if(str[i] < 0)
				{
					blHasChinese = true;
					SetWindowText(strText);
					SetSel(iMaxLen,iMaxLen);
					break;
				}
			}
		}
		if(IsSpecialCharacters(nChar))
		{
			blHasChinese = true;
		}
		if(TRUE == m_cbkCharCallBack(nChar, nRepCnt, nFlags, GetSafeHwnd(),strText,blHasChinese))
		{
			return;
		}
	}

	CEdit::OnChar(nChar, nRepCnt, nFlags);
	if (!strClipboard.IsEmpty())
	{
		SetClipboardString(strClipboard);
	}
	CStringA strText1;
	GetWindowText(strText1);
	CStringA strOut = strText1;
	if (m_iCharSet == CHARSET_UTF8)
	{
		AnsiToUTF8(strText1.GetBuffer(), strOut);
	}
	if (strOut.GetLength() > iMaxLen && nChar != VK_BACK)
	{
		SetWindowText(strText);
		SetSel(iMaxLen,iMaxLen);
		return;
	}
}

void TDFilterEdit::SetFilterCallBack( WM_CHAR_NOTIFY _wCharCallBack )
{
	m_cbkCharCallBack = _wCharCallBack;
}

void TDFilterEdit::OnRButtonDown(UINT nFlags, CPoint point)
{
	return;
}

bool TDFilterEdit::IsSpecialCharacters(UINT nChar)
{
	if(nChar >= 0x2E80 && nChar <= 0xFE4F)
	{
		return true;
	}
	return false;
}

void TDFilterEdit::OnLButtonDown(UINT nFlags, CPoint point)
{
	CWnd* pParent = GetParent();
	if (pParent)
	{
		pParent->SetFocus();
	}
	SetActiveWindow();
	SetFocus();
	CEdit::OnLButtonDown(nFlags, point);
}

void TDFilterEdit::FormatLine()
{
	CString strText;
	GetWindowText(strText);
	strText.Replace(_T("\r\n"), _T("\n"));
	CStringArray aryContent;
	int iLineCount = SplitStringToIntArray(strText, _T("\n"), aryContent);
	if (m_iMaxLineCOunt > 0 && iLineCount > m_iMaxLineCOunt)
	{
		CString strTarget;
		for (int iIdx = 0; iIdx < m_iMaxLineCOunt; ++iIdx)
		{
			CString strTemp = aryContent.GetAt(iIdx);
			strTarget += strTemp;
			if (iIdx < m_iMaxLineCOunt - 1)
			{
				strTarget += _T("\r\n");
			}
		}
		SetWindowText(strTarget);
	}
}

void TDFilterEdit::InsertText(CString strText)
{
	if (strText.GetLength() > 0)
	{
		//Check the memory length first
		CStringA strAllContent;
		CStringA strSelectText;
		GetWindowText(strAllContent);
		CStringA strAllContentOut = strAllContent;
		CStringA strTextOut = strText;
		CStringA strSelectTextOut;
		//Calculate the length of the selected area
		int iStartChar = 0;
		int iEndChar = 0;
		GetSel(iStartChar,iEndChar);
		if (iEndChar - iStartChar > 0)
		{
			//Indicates that an area is selected
			CStringW strU16AllContent = AnsiToUnicode(strAllContent.GetBuffer());
			strAllContent.ReleaseBuffer();
			CStringW strU16Select = strU16AllContent.Mid(iStartChar, iEndChar - iStartChar);
			strSelectText = UnicodeToAnsi(strU16Select.GetBuffer());
			strU16Select.ReleaseBuffer();
		}
		if (CHARSET_UTF8 == m_iCharSet)
		{
			AnsiToUTF8(strAllContent.GetBuffer(), strAllContentOut);
			AnsiToUTF8(strText.GetBuffer(), strTextOut);
			if (strSelectText.GetLength() > 0)
			{
				AnsiToUTF8(strSelectText.GetBuffer(), strSelectTextOut);
				strSelectText.ReleaseBuffer();
			}
			strAllContent.ReleaseBuffer();
			strText.ReleaseBuffer();
		}

		int iAllCountentLen = strAllContentOut.GetLength();
		int iTextLen = strTextOut.GetLength();
		int iSelectLen = strSelectTextOut.GetLength();
		int iMaxLen = GetLimitText();
		if (iAllCountentLen + iTextLen - iSelectLen > iMaxLen)
		{
			return;
		}

		//Insert
		ReplaceSel(strText, TRUE);
	}
}

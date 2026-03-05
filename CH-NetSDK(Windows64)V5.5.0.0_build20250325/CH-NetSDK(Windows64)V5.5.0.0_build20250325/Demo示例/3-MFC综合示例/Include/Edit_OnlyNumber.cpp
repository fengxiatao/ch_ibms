// Edit_OnlyNumber.cpp : implementation file
//

#include "stdafx.h"
//#include "TEST_ivs_12_2_1.h"
#include "Edit_OnlyNumber.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CEdit_OnlyNumber

CEdit_OnlyNumber::CEdit_OnlyNumber()
{
	m_iFilterType = 0;
	m_iUnicodeMark = 0;
	m_iUnicodeMark1 = 0;
	m_iUnicodeMark2 = 0;
	m_blLimitLen = false;
	m_blCanCopy = false;
}

CEdit_OnlyNumber::~CEdit_OnlyNumber()
{
}


BEGIN_MESSAGE_MAP(CEdit_OnlyNumber, CEdit)
	ON_WM_CHAR()
	ON_WM_CONTEXTMENU()
	ON_WM_RBUTTONDOWN()
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CEdit_OnlyNumber message handlers

/**************************************************************************
*Virtual function: exclude English letters
*
*
*
**************************************************************************/
void CEdit_OnlyNumber::OnChar(UINT nChar, UINT nRepCnt, UINT nFlags) 
{
	if( nChar == 32 || (!m_blCanCopy && (22 ==  nChar || 3 == nChar))) //Block space and copy and paste functions
	{
		return;
	}

	if (m_blLimitLen && !(22 ==  nChar || 3 == nChar))
	{
		int iRet = FilterOverInput((nChar >= 0x80 && nChar <= 0xfe)?true:false);

		if (0 > iRet)
		{
			if (8 == nChar || 3 == nChar)	 //backspace
			{
				CEdit::OnChar(nChar, nRepCnt, nFlags);
			}
			return;
		}
	}

	if(ONLYNUMBER != m_iFilterType && 
		DECIMALNUMBER != m_iFilterType && 
		NEGATIVENUMBER != m_iFilterType && 
		ONLY_NUM_ADN_CHAR != m_iFilterType &&
		FILTERCH != m_iFilterType && FILTERVIOCHAR_FTP_PSW != m_iFilterType
		&&FILTERKFK_CONFIG != m_iFilterType)
	{
		if( nChar >= 0x81 && nChar <= 0xfe ) //Judging from the bytes it is Chinese
		{
			m_iUnicodeMark++;
			if( m_iUnicodeMark == 1 )
			{
				if( nChar == 161 || nChar == 163 )
				{
					m_iUnicodeMark1 = nChar;
					return;
				}
			}

			if( m_iUnicodeMark == 2 )
			{
				m_iUnicodeMark = 0;
				m_iUnicodeMark2 = nChar;
				if( ( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 182 ) || ( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 183 ) || 
					( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 172 ) || ( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 162 ) || 
					( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 187 ) || ( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 174 ) || 
					( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 190 ) || ( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 191 ) || 
					( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 164 ) || ( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 1 )   || 
					( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 164 ) || ( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 173 ) || 
					( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 168 ) || ( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 169 ) || 
					( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 170 ) || ( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 186 ) || 
					( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 176 ) || ( m_iUnicodeMark1 == 161 && m_iUnicodeMark2 == 177 ) ||
					( m_iUnicodeMark1 == 163 && m_iUnicodeMark2 == 191)/*add by lml  Add possibility to filter shift+? */)
				{
					m_iUnicodeMark1 = 0;
					m_iUnicodeMark2 = 0;

					return;
				}
				m_iUnicodeMark1 = 0;
				m_iUnicodeMark2 = 0;
			}

			CEdit::OnChar(nChar, nRepCnt, nFlags);
			return;
		}
	}

	switch (m_iFilterType)
	{
	case ONLYNUMBER://default number
		if ((nChar>=48 && nChar<=57)||8 == nChar ||(m_blCanCopy && (22 ==  nChar||3 == nChar)) )
		{
			if (22 ==  nChar)
			{
				CString cstrTemp="";
				GetCopyString(cstrTemp);
				if (!CheckString(cstrTemp))
				{
					return;
				}
			}
			else if(3 == nChar)
			{
				Copy();
			}
			CEdit::OnChar(nChar, nRepCnt, nFlags);
		}
		break;
	case FILTERNUMBER://filter number
		if (nChar <48 || nChar >57 )
		{
			CEdit::OnChar(nChar, nRepCnt, nFlags);
		}
		break;
	case FILTERCHAR://filter letters
		if (nChar > 'Z'&&nChar < 'a' || nChar > 'z'//small letters
			|| nChar<'A' )//uppercase letter
		{
			CEdit::OnChar(nChar, nRepCnt, nFlags);
		}
		break;
	case FILTERCH://filter Chinese characters
		if (nChar < 0x81 || nChar > 0xfe
			||(m_blCanCopy && (22 ==  nChar||3 == nChar))) //non-kanji
		{
			if (22 ==  nChar)
			{
				CString cstrTemp="";
				GetCopyString(cstrTemp);
				if (!CheckString_Ex2(cstrTemp))
				{
					return;
				}
			}
			else if(3 == nChar)
			{
				Copy();
			}

			CEdit::OnChar(nChar, nRepCnt, nFlags);
		}
		break;
	case FILTERVIOCHAR://Filter illegal characters
		if (nChar != 45&&//negative:-
			nChar < 33 
			||(nChar > 47 && nChar < 58)
			||(nChar > 64 && nChar < 91) 
			||(nChar > 96 && nChar < 123) 
			||(nChar > 126 && nChar !=161 
			&& nChar !=163 && nChar !=164 
			&& nChar !=169 && nChar !=172 
			&& nChar !=186 && nChar !=191))//uppercase letter
		{
			CEdit::OnChar(nChar, nRepCnt, nFlags);
		}
		break;
	case NOFILTER://do not filter any type
		CEdit::OnChar(nChar, nRepCnt, nFlags);
		break;
	case DECIMALNUMBER:	//decimal
		{
			CString strEdit = "";
			GetWindowText(strEdit);
			int iLength=strEdit.GetLength();
			int iPos=strEdit.Find('.');
			
			if((nChar >= 48 && nChar <= 57) || nChar == 46 || nChar == 8)
			{
				if(nChar == 8)
				{
					CEdit::OnChar(nChar, nRepCnt, nFlags);
					return;
				}
				if(((iLength-iPos) <= 2 && iPos != -1 && nChar != 46) || iPos == -1 )
				{
					CEdit::OnChar(nChar, nRepCnt, nFlags);
				}
			}
			break;
		}
	case NEGATIVENUMBER:
		{
			CString strEdit = "";
			GetWindowText(strEdit);
			int iLength=strEdit.GetLength();
			if((::isdigit(nChar)) > 0)   
			{
				CEdit::OnChar(nChar,   nRepCnt,   nFlags);   
			}
			else if(nChar == 45) 
			{   
				int  iPos = strEdit.Find(_T('-'));   
				if (iPos == 0 )
				{
					return;//Here is to prevent the input from being connected one after the other -
				}
				if ((iLength - iPos) <= 1 && iPos == -1 && strEdit.IsEmpty())
				{
					CEdit::OnChar(nChar, nRepCnt, nFlags);
				}
			}   
			else if(nChar==VK_BACK)
			{
				CEdit::OnChar(nChar,   nRepCnt,   nFlags);   
			}

			break;
		}
	case ONLY_NUM_ADN_CHAR:
		{
			if ((nChar >= 48 && nChar <= 57)        //number
				|| (8 == nChar)						//space
				|| (nChar <= 'Z' && nChar >= 'A')	//uppercase letter
				|| (nChar >= 'a' && nChar <= 'z')
				||(m_blCanCopy && (22 ==  nChar||3 == nChar)))	//Lower case letters
			{
				if (22 ==  nChar)
				{
					CString cstrTemp="";
					GetCopyString(cstrTemp);
					if (!CheckString_Ex1(cstrTemp))
					{
						return;
					}
				}
				else if(3 == nChar)
				{
					Copy();
				}
				CEdit::OnChar(nChar, nRepCnt, nFlags);
			}

			break;
		}
	case FILTERVIOCHAR_EX://Filter illegal characters (special characters that cannot create folders)
		 //" 34 * 42 / 47 : 58 < 60 > 62 ? 63 \ 92 | 124 39 '
		if ((nChar != 34 && 
			nChar != 42 && 
			nChar != 47 && 
			nChar != 58 && 
			nChar != 60 &&
			nChar != 62 &&
			nChar != 63 &&
			nChar != 92 &&
			nChar != 39 &&
			nChar != 124) ||
			(m_blCanCopy && (22 ==  nChar||3 == nChar)))
		{
			if (22 == nChar)
			{
				CString cstrTemp="";
				GetCopyString(cstrTemp);
				if (!CheckString_Ex(cstrTemp))
				{
					return;
				}
			}
			else if(3 == nChar)
			{
				Copy();
			}

			CEdit::OnChar(nChar, nRepCnt, nFlags);
		}
		break;
	case FILTERVIOCHAR_HTTP:    //Filter illegal characters of http
	case FILTERVIOCHAR_HTTP_TRUST_WAY:
		{
			if (nChar != 45 && nChar < 33		//negative:-
				||(nChar >= 46 && nChar <= 58)
				||(nChar > 64 && nChar < 91) 
				||nChar ==95
				||(nChar > 96 && nChar < 123) 
				||(nChar > 126 && nChar !=161 
				&& nChar !=163 && nChar !=164 
				&& nChar !=169 && nChar !=172 
				&& nChar !=186 && nChar !=191)
				|| (nChar == 63 && FILTERVIOCHAR_HTTP_TRUST_WAY == m_iFilterType))//uppercase letter
			{
				CEdit::OnChar(nChar, nRepCnt, nFlags);
			}
			break;
		}
	case FILTERVIOCHAR_FTP_PSW://Docking FTP password special characters only support @ - _ these three
		if (nChar == 45 || //negative:-
			nChar < 33 
			||(nChar > 47 && nChar < 58)
			||(nChar > 63 && nChar < 91) 
			||(nChar > 94 && nChar < 123 && nChar != 96) 
			||(nChar > 126 && nChar !=161 
			&& nChar !=163 && nChar !=164 
			&& nChar !=169 && nChar !=172 
			&& nChar !=186 && nChar !=191))//uppercase letter
		{
			CEdit::OnChar(nChar, nRepCnt, nFlags);
		}
		break;
	case FILTERKFK_CONFIG:
		{
			if ((nChar >= 48 && nChar <= 57)        //number
				|| (95 == nChar)//"_"
				|| (8 == nChar)
				|| (45 == nChar)
				|| (46 == nChar)
				|| (nChar <= 'Z' && nChar >= 'A')	//uppercase letter
				|| (nChar >= 'a' && nChar <= 'z')
				||(m_blCanCopy && (22 ==  nChar||3 == nChar)))	//Lower case letters
			{
				CEdit::OnChar(nChar, nRepCnt, nFlags);
			}
		}
		break;
	default:
		char ch[256];
		GetWindowText(ch,256);
		if(isalpha(nChar))
			break;
	}
}


void CEdit_OnlyNumber::setFilter(UINT _nChar, bool _blLimitLen,bool _blCanCopy)
{
	m_iFilterType = _nChar;
	m_blLimitLen = _blLimitLen;
	m_blCanCopy = _blCanCopy;
}

void CEdit_OnlyNumber::OnRButtonDown(UINT nFlags, CPoint point)
{
	return;
}

void CEdit_OnlyNumber::OnContextMenu(CWnd* pWnd, CPoint pos)
{
	return;
}

int CEdit_OnlyNumber::FilterOverInput(bool _blIsDoubleChar)
{
	WCHAR wcTemp[256]={0}; 
	CStringA cstrTemp = "";
	GetWindowText(cstrTemp);
	int iDoubleCharLen = cstrTemp.GetLength();
	int iLimitLen = GetLimitText();
	int iEndLen = iLimitLen - iDoubleCharLen;
	if (_blIsDoubleChar)
	{
		if(2 > iEndLen)
		{
			return -1;
		}
	}
	else
	{
		if(1 > iEndLen)
		{
			return -1;
		}
	}

	return 0;
}
void CEdit_OnlyNumber::GetCopyString(CString &_fromClipboard)
{
	char * buffer = NULL;
	//open the clipboard

	if (OpenClipboard()) 
	{
		HANDLE hData = GetClipboardData( CF_TEXT );
		char * buffer = (char*)GlobalLock( hData );
		_fromClipboard = buffer;
		GlobalUnlock( hData );
		CloseClipboard();
	}
}

BOOL CEdit_OnlyNumber::CheckString( CString str )
{
	CString strTmp = str;

	// As needed, decide whether to remove space, TAB or newline characters from both ends of the string
	strTmp.TrimLeft();
	strTmp.TrimRight();

	if(strTmp.IsEmpty())
	{
		return FALSE;
	}

	strTmp.TrimLeft("0123456789");
	// If the string is empty after removing the number, it means that all the strings are numbers.
	// If the first character is not allowed to be 0, add a judgment if(str.GetAt(0) != '0') return FALSE;
	if(!strTmp.IsEmpty())	
	{
		return FALSE;
	}

	return TRUE;
}

BOOL CEdit_OnlyNumber::CheckString_Ex( CString str )
{
	CString strTmp = str;
	int iLen = strTmp.GetLength();

	for (int i = 0; i < iLen; i++)
	{
		//number is 48-57 in ASCII table uppercase letter is 65-90 in ASCII table Lower case letters is 97-122 in ASCII
		if( strTmp[i] == 34 || 
			strTmp[i] == 42 || 
			strTmp[i] == 47 || 
			strTmp[i] == 58 || 
			strTmp[i] == 60 ||
			strTmp[i] == 62 ||
			strTmp[i] == 63 ||
			strTmp[i] == 92 ||
			strTmp[i] == 124)
		{
			return FALSE;
		}
	}

	return TRUE;
}

BOOL CEdit_OnlyNumber::CheckString_Ex1( CString str )
{
	CString strTmp = str;
	int iLen = strTmp.GetLength();

	for (int i = 0; i < iLen; i++)
	{
		if((strTmp[i] >= 48 && strTmp[i] <= 57)        //number
			|| (8 == strTmp[i])						//space
			|| (strTmp[i] <= 'Z' && strTmp[i] >= 'A')	//uppercase letter
			|| (strTmp[i] >= 'a' && strTmp[i] <= 'z'))
		{
			continue;
		}
		else 
		{
			return FALSE;
		}
	}

	return TRUE;
}

BOOL CEdit_OnlyNumber::CheckString_Ex2( CString str )
{
	CString strTmp = str;
	int iLen = strTmp.GetLength();

	for (int i = 0; i < iLen-1; i++)
	{
		if(strTmp[i] & 0x80)
		{
			return FALSE;
		}
	}

	return TRUE;
}

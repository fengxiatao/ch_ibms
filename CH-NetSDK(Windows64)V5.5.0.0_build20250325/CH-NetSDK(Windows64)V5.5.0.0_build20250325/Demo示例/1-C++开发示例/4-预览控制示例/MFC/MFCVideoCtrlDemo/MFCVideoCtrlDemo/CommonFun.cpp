#include "stdafx.h"
#include "CommonFun.h"
#include <stdio.h>

static char s_cChineseCharMin = (char)0xA1;
static char s_cChineseCharMax = (char)0xF7;

#define MAX_LOG_MSG_LEN		4096

CString g_strLocalSaveDirectory;

// A valid IP address returns >0, where:
// multicast address: 2
// subnet mask: 3
// normal IP: 1
int IsValidIP(const char* _cIP)
{
	if (!_cIP)
		return 0;

    int i, i1, i2, i3, i4=0;
	char ct = '\0';
    unsigned long uIP=0;
    if(sscanf_s(_cIP, "%03d.%03d.%03d.%d%c", &i1, &i2, &i3, &i4, &ct) != 4)
        return 0;
    if( ((0 <= i1) && (i1 <= 255)) &&
        ((0 <= i2) && (i2 <= 255)) &&
        ((0 <= i3) && (i3 <= 255)) &&
        ((0 <= i4) && (i4 <= 255)) )
    {
        if((224 <= i1) && (i1 <= 239))
            return 2;   //Multicast

        uIP = (i1<<24) | (i2<<16) | (i3<<8) | (i4);
        i=0;
        while(((uIP & 0x01)==0) && i<32)
        {
            uIP = uIP>>1;
            i++;
        }
        while(((uIP & 0x01)==1) && i<32)
        {
            uIP = uIP>>1;
            i++;
        }
        if(i==32)
            return 3;     //Mask
        else
            return 1;     //valid ip
    }
    return 0;
}

int IsValidIP(const char* _cIP,const char* _cMask,const char* _cGateway)
{
	unsigned long ulIP = inet_addr(_cIP);
	unsigned long ulMask = inet_addr(_cMask);
	unsigned long ulGateway = inet_addr(_cGateway);
	
	if (ulIP != 0 || ulGateway != 0)
	{
		if (INADDR_NONE == ulIP || INADDR_NONE == ulMask || INADDR_NONE == ulGateway
			||( 0 == (ulIP & 0xFF) || 0 == (ulGateway & 0xFF)))
		{
			return -1;
		}
	}

	if ((ulIP & ulMask) == (ulGateway & ulMask))
	{
		return 0;
	}
	return -1;
}

bool IsDVR(const int _iProductModel)
{
	return ((_iProductModel & 0x100) == 0x100);
}

CString IntToString( int _iData )
{
	CString strData;
	strData.Format(_T("%d"),_iData);
	return strData;
}


int SetLocalSaveDirectory( CString _strPath )
{
	CreateDirectory(_strPath,NULL);
	g_strLocalSaveDirectory = _strPath;
	return 0;
}

CString GetLocalSaveDirectory()
{
	return g_strLocalSaveDirectory;
}

CString Bytes2HexString( unsigned char* _pcSrc,int _iSrcLen )
{
	CString strHex;
	for (int i = 0; i < _iSrcLen; ++i)
	{
		strHex.AppendFormat(_T("%02X "),_pcSrc[i]);
	}
	return strHex.Trim();
}

bool Inner_SafeCloseFile(FILE** _ppFile)
{
	if (_ppFile == NULL || *_ppFile == NULL)
		return true;

	fclose(*_ppFile);
	*_ppFile = NULL;
	return true;
}

// can only be used when other functions copy this return value
//Such as intsert***(0, IntToCString(**)), cannot use CString str = IntToCString(**)
CString IntToCString(int _iPara, CString *_pCstr/* = NULL*/)
{
	if (_pCstr == NULL)
	{
		CString strPara;
		strPara.Format(_T("%d"), _iPara);
		return strPara;
	}
	else
	{
		_pCstr->Format(_T("%d"), _iPara);
		return *_pCstr;
	}
}

//Algorithm 2 Whether the point is inside the polygon Here the default is a quadrilateral _iPtSum defaults to 4
//_ptCur current point, _ptPolygon polygon point coordinates, polygon point number
// Judgment method Make a connection between the current point and the (0, 0) point, and then judge whether there is an intersection with each edge of the polygon, and count the total number of intersections, even numbers are outside, odd numbers are inside
bool IfPointInPolygon(POINT _ptCur, POINT *_ptPolygon, int _iPtSum)
{
	if(_iPtSum < 1)
		return false;
	POINT pt = {0};
	int iPtSum = 0;
	for(int i = 0; i< _iPtSum - 1; i++) //Determine how many connections there are between the current point and the point (0, 0) and the four sides of the quadrilateral
	{
		if(IfHaveInterSection(_ptCur, pt, _ptPolygon[i], _ptPolygon[i+1]))
			iPtSum++;
	}
	//Determine whether the connection between the current point and the point (0, 0) has an intersection with the last edge of the quadrilateral
	if(IfHaveInterSection(_ptCur, pt, *_ptPolygon, *(_ptPolygon+ _iPtSum - 1)))
		iPtSum++;

	if(iPtSum%2)
	{
		//trace("inside\n");
		return true;
	}
	else
	{
		//trace("Outside\n");
		return false;
	}
}

//Algorithm 1 to determine whether two lines have an intersection function
bool IfHaveInterSection(POINT _ptLine1Start, POINT _ptLine1End, POINT _ptLine2Start, POINT _ptLine2End)     //wxl 2011-11-22
{
	double iL1SX = _ptLine1Start.x;
	double iL1SY = _ptLine1Start.y;

	double iL2SX = _ptLine2Start.x;
	double iL2SY = _ptLine2Start.y;

	double iL1EX = _ptLine1End.x;
	double iL1EY = _ptLine1End.y;

	double iL2EX = _ptLine2End.x;
	double iL2EY = _ptLine2End.y;
	double fDiv = 0.0;
	double fInterSX = 0.0; //If there is focus, focus x coordinate
	double fInterSY = 0.0; //If there is focus, focus y coordinate

	fDiv = (iL1SX -iL1EX)*(iL2SY -iL2EY) -(iL2SX -iL2EX)*(iL1SY - iL1EY);
	if(fDiv == 0)
	{
		return false;
	}
	else
	{
		fInterSX = ((iL2SX-iL2EX)*(iL1SX*iL1EY -iL1EX*iL1SY)-(iL1SX-iL1EX)*(iL2SX*iL2EY - iL2EX*iL2SY))/fDiv;
		fInterSY = ((iL2EX*iL2SY -iL2SX*iL2EY)*(iL1SY-iL1EY)-(iL2SY-iL2EY)*(iL1EX*iL1SY-iL1SX*iL1EY))/fDiv;

		if (fInterSX < max(min(iL1SX,iL1EX),min(iL2SX,iL2EX)) || fInterSX > min(max(iL1SX,iL1EX),max(iL2SX,iL2EX)))
		{
			return false;
		}
		else if (fInterSY < max(min(iL1SY,iL1EY),min(iL2SY,iL2EY)) || fInterSY > min(max(iL1SY,iL1EY),max(iL2SY,iL2EY)))
		{
			return false;
		}
		else
		{
			return true; //there is an intersection
		}
	}
}

//Algorithm to judge whether two polygons intersect, this algorithm mainly judges two quadrilaterals
bool IfPolygonIntersect(POINT *_ptPolygon1, int _iPoly1, POINT *_ptPolygon2, int _iPoly2)
{
	POINT ptPolygon1[5] = {0};
	memcpy(ptPolygon1, _ptPolygon1, _iPoly1 * sizeof(POINT));
	ptPolygon1[_iPoly1] =  _ptPolygon1[0];

	POINT ptPolygon2[5] = {0};
	memcpy(ptPolygon2, _ptPolygon2, _iPoly2 * sizeof(POINT));
	ptPolygon2[_iPoly2] =  _ptPolygon2[0];

	for(int i = 0; i < _iPoly1; i++)
	{
		for(int j = 0; j < _iPoly2; j++)
		{
			if(IfHaveInterSection(*(ptPolygon1+i), *(ptPolygon1+i+1), *(ptPolygon2+j), *(ptPolygon2+j+1)))
				return true;
		}
	}
	return false;
}

//Algorithm Two disjoint polygons, determine whether they contain
bool IfPolygonInPolygon(POINT *_ptPolygon1, int _iPoly1, POINT *_ptPolygon2, int _iPoly2)
{
	if(IfPointInPolygon(_ptPolygon1[0], _ptPolygon2, _iPoly2))
	{
		return true;
	}
	if(IfPointInPolygon(_ptPolygon2[0], _ptPolygon1, _iPoly2))
	{
		return true;
	}
	return false;
}


int GetCboSel(CComboBox* _pComboBox, int _iItemData)
{
	int iResult = -1;
	if (NULL == _pComboBox)
	{
		return iResult;
	}

	int iMaxIndex = _pComboBox->GetCount();
	for (int iTempIndex = 0; iTempIndex < iMaxIndex; iTempIndex++)
	{
		if (_iItemData == _pComboBox->GetItemData(iTempIndex))
		{
			iResult = iTempIndex;
			break;
		}
	}

	return iResult;
}

int FillStringBufferGb2312(char* _pcStringBuffer, int _iStringLen, char* _pcSrc, int _iSrcLen)
{
	if (NULL == _pcStringBuffer || NULL == _pcSrc)
	{
		return -1;
	}

	int iCopyLen = 0;
	while (iCopyLen <= _iSrcLen)
	{
		int iCharLen = 1;
		if (_pcSrc[iCopyLen] >= s_cChineseCharMin && _pcSrc[iCopyLen] <= s_cChineseCharMax)
		{
			iCharLen = 2;
		}

		if (iCopyLen + iCharLen >= _iStringLen)
		{
			_pcStringBuffer[iCopyLen] = '\0';
			break;
		}

		if (iCopyLen + iCharLen > _iSrcLen)
		{
			_pcStringBuffer[iCopyLen] = '\0';
			break;
		}

		memcpy(_pcStringBuffer + iCopyLen, _pcSrc+iCopyLen, iCharLen);
		iCopyLen += iCharLen;
	}
	return  iCopyLen;
}

void SetListCtrlColumn(CListCtrl& _lstListCtrl, int _iIndex, CString _cstrColumnName)
{
	CHeaderCtrl *pHead = _lstListCtrl.GetHeaderCtrl();    
	if(NULL == pHead || NULL == pHead->GetSafeHwnd())    
	{    
		return;
	}  

	TCHAR szBuf[MAX_LIST_COLUMN_NAME_LEN + 1] = {0};    
	HDITEM hdItem ={0};    
	hdItem.mask = HDI_TEXT; //Get character mask
	hdItem.pszText = szBuf; //Character buffer
	hdItem.cchTextMax = MAX_LIST_COLUMN_NAME_LEN; //buffer size
	hdItem.pszText = _cstrColumnName.GetBuffer(0); //Set new character
	pHead->SetItem(_iIndex, &hdItem); //Set the header
}

void SetLogCtrl( CListCtrl* _plvLog )
{
	CLS_LogManager::Instance()->SetListCtrl(_plvLog);
}

int SetLogFileName( char* _pcFileName,int _iLen )
{
	if (_iLen <= 0)
	{
		_iLen = (int)strlen(_pcFileName);
	}
	return CLS_LogManager::Instance()->SetFileName(_pcFileName,_iLen);
}

int SetLogCaps( unsigned int _uRemove,unsigned int _uAdd )
{
	return CLS_LogManager::Instance()->SetLogCaps(_uRemove,_uAdd);
}

unsigned int GetLogCaps()
{
	return CLS_LogManager::Instance()->GetLogCaps();
}

void AddLog( int _iLogType,const char* _pcDevFmt,const char* _pcLogFmt, ... )
{
	int iLastError = GetLastError();
	if (NULL == _pcDevFmt)
	{
		_pcDevFmt = "";
	}
	if (NULL == _pcLogFmt)
	{
		_pcLogFmt = "";
	}

	char cDevInfo[2048] = {0};
	char cFormat[1024] = {0};
	char cSplit[5] = {28,29,30,31};

	sprintf_s(cFormat,sizeof(cFormat),"%s%s%s",_pcDevFmt,cSplit,_pcLogFmt);
	va_list vlLog;
	va_start(vlLog, _pcLogFmt);
	vsprintf_s(cDevInfo, cFormat, vlLog);
	va_end(vlLog);	
	char* pcLogInfo = strstr(cDevInfo,cSplit);
	if (pcLogInfo)
	{
		memset(pcLogInfo,0,strlen(cSplit));
		pcLogInfo += strlen(cSplit);
	}
	else
	{
		pcLogInfo = "";
	}

	CLS_LogManager::Instance()->AddLog(_iLogType,cDevInfo,pcLogInfo,iLastError);
}

void OutPutLogMsg(const char* _pcLogFmt, ...)
{
	if (NULL == _pcLogFmt)
	{
		_pcLogFmt = "";
	}
	char cLogBuf[MAX_LOG_MSG_LEN] = {0};
	va_list vlsLog;
	va_start( vlsLog, _pcLogFmt );
	_vsnprintf(cLogBuf, MAX_LOG_MSG_LEN, _pcLogFmt, vlsLog);
	va_end(vlsLog);

	//Arrays in C language do not allow overall copying
	char* pcLogInfo = strcat(cLogBuf, "\n");
	::OutputDebugString(pcLogInfo);
}

void Destroy()
{
	CLS_LogManager::Destroy();
}


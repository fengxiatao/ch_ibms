#include "stdafx.h"
#include "CommonFun.h"
#include <stdio.h>
#include <shlwapi.h>
#include <WS2tcpip.h>
#include "socket_api.h"
#include "md5.h"

static char s_cChCharMin = (char)0xA1;
static char s_cChCharMax = (char)0xF7;

CString g_strLocalSaveDirectory;

//  A valid IP address returns >0, where:
//  Multicast addresses: 2
//  Subnet Mask: 3
//  Normal IP: 1
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

/*****************************************************************************
 Function name : IsValidIPv6
 Function description: Determine the validity of the IPv6 address
 Input parameters: char* _cIP ipv6 address string
 Return value: 1-legal, 2-multicast, 3-link-local, 4-loopback, 5-unspecified
*****************************************************************************/
int IsValidIPv6(const char* _cIP)
{
	int iRet = -1;

	if(NULL == _cIP)
	{
		goto Exit;
	}

	struct in6_addr addr6;
	memset(&addr6, 0, sizeof(addr6));

	iRet = api_socket_inet_pton(AF_INET6, (char *)_cIP, &addr6);
	if(1 != iRet) //Invalid IPv6 address
	{
		iRet = -1;
		goto Exit;
	}

	if(IN6_IS_ADDR_MULTICAST(&addr6)) //Multicast IPv6 address (multicast addresses from FF01:: to FF0F:: are reserved private addresses)
	{
		iRet = 2;
		goto Exit;
	}

	if(IN6_IS_ADDR_LINKLOCAL(&addr6)) //Link local IPv6 address (fe80::/64)
	{
		iRet = 3;
		goto Exit;
	}

	if(IN6_IS_ADDR_LOOPBACK(&addr6)) //loopback IPv6 address(::1)
	{
		iRet = 4;
		goto Exit;
	}

	if(IN6_IS_ADDR_UNSPECIFIED(&addr6)) //unspecified IPv6 address (0:0:0:0:0:0:0:0 or ::)
	{
		iRet = 5;
		goto Exit;
	}

Exit:

	return iRet;
}

const DWORD g_dwLanChinese = 2052;
const DWORD g_dwLanEnglish = 1033;
CString GetTextByLan( CString _cstrTextCH, CString _cstrTextEn/* = ""*/ )
{
	DWORD dwCurLan = CLS_LanguageManager::Instance()->GetLanguage();
	if (g_dwLanChinese == dwCurLan)
	{
		return _cstrTextCH;
	}
	return _cstrTextEn;
}

CString GetFormatTextByLan( CString _cstrFormatTextCH, CString _cstrFormatTextEn, ... )
{
	CString cstrDstInfo;
	DWORD dwCurLan = CLS_LanguageManager::Instance()->GetLanguage();

	char cLogFmt[MAX_LOG_MSG_LEN] = {0};
	char cLogBuf[MAX_LOG_MSG_LEN] = {0};
	if (g_dwLanChinese == dwCurLan)
	{
		strncpy_s(cLogFmt, _cstrFormatTextCH.GetBuffer(), min(MAX_LOG_MSG_LEN, _cstrFormatTextCH.GetLength()));
		_cstrFormatTextCH.ReleaseBuffer();
	}
	else
	{
		strncpy_s(cLogFmt, _cstrFormatTextEn.GetBuffer(), min(MAX_LOG_MSG_LEN, _cstrFormatTextEn.GetLength()));
		_cstrFormatTextEn.ReleaseBuffer();
	}
	
	va_list vlsLog;
	va_start( vlsLog, cLogFmt );
	_vsnprintf(cLogBuf, MAX_LOG_MSG_LEN, cLogFmt, vlsLog);
	va_end(vlsLog);

	cstrDstInfo = cLogBuf;
	return cstrDstInfo;
}

PDEVICE_INFO FindDevice( char* _pcIP, int _iPort,char* _pcProxy,char* _pcID,int* _piLogonID)
{
	return CLS_DeviceManager::Instance()->FindDevice(_pcIP,_iPort,_pcProxy,_pcID,_piLogonID);
}

PDEVICE_INFO FindDevice( int _iLogonID )
{
	return CLS_DeviceManager::Instance()->FindDevice(_iLogonID);
}

PDEVICE_INFO AddDevice( int _iLogonID )
{
	return CLS_DeviceManager::Instance()->AddDevice(_iLogonID);
}

PDEVICE_INFO RemoveDevice( int _iLogonID )
{
	return CLS_DeviceManager::Instance()->RemoveDevice(_iLogonID);
}

PDEVICE_INFO RemoveDevice( char* _pcIP,int _iPort,char* _pcProxy,char* _pcID,int* _piLogonID /*= NULL*/ )
{
	return CLS_DeviceManager::Instance()->RemoveDevice(_pcIP,_iPort,_pcProxy,_pcID,_piLogonID);
}

PCHANNEL_INFO FindChannel( int _iLogonID,int _iChannelNo,int _iStreamNo,unsigned int* _puConnID)
{
	return CLS_DeviceManager::Instance()->FindChannel(_iLogonID,_iChannelNo,_iStreamNo,_puConnID);
}

PCHANNEL_INFO FindChannel( unsigned int _uConnID )
{
	return CLS_DeviceManager::Instance()->FindChannel(_uConnID);
}

PCHANNEL_INFO AddChannel( unsigned int _uConnID )
{
	return CLS_DeviceManager::Instance()->AddChannel(_uConnID);
}

PCHANNEL_INFO RemoveChannel( unsigned int _uConnID )
{
	return CLS_DeviceManager::Instance()->RemoveChannel(_uConnID);
}

PCHANNEL_INFO RemoveChannel( int _iLogonID,int _iChannelNo,int _iStreamNo,unsigned int* _puConnID /*= NULL*/ )
{
	return CLS_DeviceManager::Instance()->RemoveChannel(_iLogonID,_iChannelNo,_iStreamNo,_puConnID);
}

queue<char *>* GetProtocolQueue(int _iLogonID)
{
	return CLS_DeviceManager::Instance()->GetProtocolQueue(_iLogonID);
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

void DestroyManager()
{
	CLS_DeviceManager::Destroy();
	CLS_LanguageManager::Destroy();
	CLS_LogManager::Destroy();
	CLS_MessageManager::Destroy();
}


int InsertItem( CTabCtrl& _TabCtrl,int _iItem,LPCTSTR _strItem )
{
	int iCount = _TabCtrl.GetItemCount();
	if (_iItem >= 0 && _iItem < iCount)
	{
		TCITEM tcItem = {0};
		tcItem.mask = TCIF_TEXT;
		tcItem.pszText = (LPSTR)_strItem;
		_TabCtrl.SetItem(_iItem,&tcItem);
	}
	else
	{
		_TabCtrl.InsertItem(_iItem,_strItem);
	}

	return 0;
}

int InsertItem( CTabCtrl& _TabCtrl,int _iItem,unsigned int _uIDResource )
{
	return InsertItem(_TabCtrl,_iItem,GetTextEx(_uIDResource));
}


HTREEITEM InsertItem( CTreeCtrl& _TreeCtrl,LPCTSTR _strItem,DWORD_PTR _dwData,HTREEITEM _hParent /*= TVI_ROOT*/ )
{
	DWORD_PTR dwData = 0;
	HTREEITEM hItem = _TreeCtrl.GetChildItem(_hParent);
	while(hItem)
	{
		dwData = _TreeCtrl.GetItemData(hItem);
		if (dwData == _dwData)
		{
			_TreeCtrl.SetItemText(hItem,_strItem);
			return hItem;
		}
		hItem = _TreeCtrl.GetNextSiblingItem(hItem);
	}

	hItem = _TreeCtrl.InsertItem(_strItem,_hParent);
	_TreeCtrl.SetItemData(hItem,_dwData);
	return hItem;
}

HTREEITEM InsertItem( CTreeCtrl& _TreeCtrl,unsigned int _uIDResource,DWORD_PTR _dwData,HTREEITEM _hParent /*= TVI_ROOT*/ )
{
	return InsertItem(_TreeCtrl,GetTextEx(_uIDResource),_dwData,_hParent);
}

int InsertColumn( CListCtrl& _ListCtrl,int _iColumn,LPCTSTR _strColumn, int _iFormat /*= LVCFMT_LEFT*/,int _iWidth /*= -1*/,int _iSubItem /*= -1*/ )
{
	CHeaderCtrl* pHeader = _ListCtrl.GetHeaderCtrl();
	if (pHeader)
	{
		int iCount = pHeader->GetItemCount();
		if (_iColumn >= 0 && _iColumn < iCount)
		{
			HDITEM hdItem = {0};
			hdItem.mask = HDI_TEXT;
			hdItem.pszText = (LPSTR)_strColumn;
			pHeader->SetItem(_iColumn,&hdItem);
			return 0;
		}
	}

	_ListCtrl.InsertColumn(_iColumn,_strColumn,_iFormat,_iWidth,_iSubItem);
	return 0;
}

int InsertColumn( CListCtrl& _ListCtrl,int _iColumn,unsigned int _uIDResource, int _iFormat /*= LVCFMT_LEFT*/,int _iWidth /*= -1*/,int _iSubItem /*= -1*/ )
{
	return InsertColumn(_ListCtrl,_iColumn,GetTextEx(_uIDResource),_iFormat,_iWidth,_iSubItem);
}

int InsertString( CComboBox& _ComboBox,int _iIndex,LPCTSTR _strItem )
{
	int iItem = _ComboBox.GetCurSel();
	_ComboBox.DeleteString(_iIndex);
	_ComboBox.InsertString(_iIndex,_strItem);
	_ComboBox.SetCurSel(iItem);

	return 0;
}

int InsertString( CComboBox& _ComboBox,int _iIndex,unsigned int _uIDResource )
{
	return InsertString(_ComboBox,_iIndex,GetTextEx(_uIDResource));
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

string IntToStr( int _iValue )
{
	char pcValue[32] = {0};
	sprintf_s(pcValue,"%d",_iValue);
	string strTemp = pcValue;
	return strTemp;
}

string LongLongToStr( long long _llValue )
{
	char pcValue[32] = {0};
	sprintf_s(pcValue,"%lld",_llValue);
	string strTemp = pcValue;
	return strTemp;
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

void* MallocMsgMemory(int _iSize)
{
	return CLS_MessageManager::Instance()->MallocMemory(_iSize);
}

void FreeMsgMemory( void* _pMemory )
{
	CLS_MessageManager::Instance()->FreeMemory(_pMemory);
}

int SplitStringToArray(const string &_strStingSource, const string &_strSplitChar,
    string *_strArray,  int _iArraySize, const int _numberOfElements)
/* Parameters: _astrStingSource: source string;
           _astrSplitChar: String separator, can be "/", "@", etc.;
           _astrArray: Return value, string array, which stores the value of the split string.
    Function: Split strings separated by special symbols into arrays
*/
{
    int iTempCount=0;
    int iTempPos=0; //A pointer to traverse the entire string //modify by lpy;
    int iTempPos1=0; //Record the first address of a substring
    int iTempPos2=0; //Record the tail address of a substring

	int iTempStrLen = (int)_strStingSource.length();
    
    if (iTempStrLen > _numberOfElements) //wxl 2011-12-21 Prevent memory out of bounds
    {
        iTempStrLen = _numberOfElements;
    }
    while (iTempPos<=iTempStrLen && (iTempCount < _iArraySize -1))
    {
		string strTemp = _strStingSource.substr(iTempPos,1) ; 
        if (strTemp.compare(_strSplitChar) == 0)
        {
            if (iTempPos1==0)
            {
                iTempPos1=iTempPos+1;
                if(iTempPos != 0)
                {
                    _strArray[iTempCount]=_strStingSource.substr(0, iTempPos);
                    iTempCount++;
                }
            }
            else if (iTempPos2==0)
            {
                iTempPos2=iTempPos+1;
                _strArray[iTempCount]=_strStingSource.substr(iTempPos1,iTempPos2-iTempPos1-1);
                iTempCount++;
                iTempPos1=iTempPos2;
                iTempPos2=0;
            }
        }
        iTempPos++;
    }
		_strArray[iTempCount]=_strStingSource.substr(iTempPos1,iTempPos-iTempPos1);
    return iTempCount+1;
}

CString GetText(UINT _uiID)
{
	CString strBuffer;
	strBuffer.LoadString(_uiID);
	return strBuffer;
}

CString ExtractFilePath()
{
	char fileNewPath[MAX_PATH+1]= {0};
	char *p = NULL;
	GetModuleFileName(NULL, fileNewPath, sizeof(fileNewPath)); //Get the current execution file name of the program
	p=strrchr(fileNewPath, '\\');   
	*(p + 1)='\0';
	return CString(fileNewPath);
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
		if (_pcSrc[iCopyLen] >= s_cChCharMin && _pcSrc[iCopyLen] <= s_cChCharMax)
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

int SavePicture(char* _pcFileName, char* _pcData, int _iLen)
{
	if (NULL == _pcFileName || NULL == _pcData || _iLen <= 0)
	{
		return -1;
	}

	CString cstrPath = GetLocalSaveDirectory() + "\\ITSPic";
	if (!PathIsDirectory(cstrPath))
	{
		CreateDirectory(cstrPath, NULL);
	}	

	cstrPath = GetLocalSaveDirectory() + "\\VCAPic";
	if (!PathIsDirectory(cstrPath))
	{
		CreateDirectory(cstrPath, NULL);
	}

	cstrPath = GetLocalSaveDirectory() + "\\SnapPic";
	if (!PathIsDirectory(cstrPath))
	{
		CreateDirectory(cstrPath, NULL);
	}

	cstrPath = GetLocalSaveDirectory() + "\\FacePic";
	if (!PathIsDirectory(cstrPath))
	{
		CreateDirectory(cstrPath, NULL);
	}

	cstrPath = GetLocalSaveDirectory() + "\\AlgData";
	if (!PathIsDirectory(cstrPath)) {
		CreateDirectory(cstrPath, NULL);
	}

	FILE* pFile = NULL;
	fopen_s(&pFile, (LPSTR)(LPCTSTR)_pcFileName, "wb");
	if (NULL == pFile)
	{
		int iErr = GetLastError();
		return -1;
	}

	size_t iWriLen = fwrite(_pcData, 1, _iLen, pFile);
	if (iWriLen <= 0)
	{
		AddLog(LOG_TYPE_FAIL,"","SavePicture fail! iWriLen <= 0 PicName=%s", _pcFileName);
	}
	fclose(pFile);

	pFile = NULL;
	return 0;
}

void DealPicStreamVCA(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser)
{
	// parameter validity judgment
	if (NULL == _pvBuf || _iBufLen <= 0) 
	{		
		return;
	}

	VcaPicStream* ptVca = (VcaPicStream*)_pvBuf;

	PicData tPicData = {0};
	for (int i = 0; i < ptVca->iPicCount; ++i)
	{
		if (NULL == ptVca->ptPicData[i])
		{
			continue;
		}

		memset(&tPicData, 0, sizeof(PicData));
		memcpy(&tPicData, ptVca->ptPicData[i], min(ptVca->iSize, (int)sizeof(PicData)));
		char cFileName[256] = {0};
		sprintf_s(cFileName, sizeof(cFileName), ".\\NetClientDemo\\VCAPic\\VcaPic_Time(2%03d%02d%02d%02d%02d%02d%d)_%s_%d_%u.jpg"
			, tPicData.tPicTime.uiYear, tPicData.tPicTime.uiMonth,  tPicData.tPicTime.uiDay, tPicData.tPicTime.uiHour
			, tPicData.tPicTime.uiMinute, tPicData.tPicTime.uiSecondsr, tPicData.tPicTime.uiMilliseconds, ptVca->cCameraIP, i, _uiRecvID);

		SavePicture(cFileName, tPicData.pcPicData, tPicData.iDataLen);
	}
}

void DealPicStreamFace(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser)
{
	// parameter validity judgment
	if (NULL == _pvBuf || _iBufLen <= 0) 
	{		
		return;
	}

	//copy image stream data
	FacePicStream tInfo = {0};
	memcpy(&tInfo, _pvBuf, min(_iBufLen, sizeof(FacePicStream)));

	//Panorama
	if (NULL != tInfo.ptFullData)
	{		
		char cFullPathName[256] = {0};
		sprintf_s(cFullPathName, sizeof(cFullPathName), ".\\NetClientDemo\\FacePic\\FullPic_Time(2%03d%02d%02d%02d%02d%02d%d)_%u"
			, tInfo.ptFullData->tPicTime.uiYear, tInfo.ptFullData->tPicTime.uiMonth,  tInfo.ptFullData->tPicTime.uiDay, tInfo.ptFullData->tPicTime.uiHour
			, tInfo.ptFullData->tPicTime.uiMinute, tInfo.ptFullData->tPicTime.uiSecondsr, tInfo.ptFullData->tPicTime.uiMilliseconds, _uiRecvID);

		CString cstrFullPicName;
		cstrFullPicName.Format("%s.jpg",cFullPathName);
		SavePicture(cstrFullPicName.GetBuffer(), tInfo.ptFullData->pcPicData, tInfo.ptFullData->iDataLen);
	}

	//Small image and base image
	for (int i= 0; i < tInfo.iFaceCount && i < MAX_FACE_PICTURE_COUNT; ++i)
	{
		if (NULL == tInfo.ptFaceData[i]) 
		{
			continue;
		}
		if (tInfo.ptFaceData[i]->iDataLen <= 0) 
		{
			continue;
		}

		FacePicData tFace = {0};
		memcpy(&tFace, tInfo.ptFaceData[i], min(tInfo.iSizeOfFace, sizeof(FacePicData)));

		// small image
		char cFacePathName[256] = {0};
		sprintf_s(cFacePathName, sizeof(cFacePathName), ".\\NetClientDemo\\FacePic\\FacePic_Time(2%03d%02d%02d%02d%02d%02d%d)_%d_%u"
			, tInfo.ptFullData->tPicTime.uiYear, tInfo.ptFullData->tPicTime.uiMonth,  tInfo.ptFullData->tPicTime.uiDay, tInfo.ptFullData->tPicTime.uiHour
			, tInfo.ptFullData->tPicTime.uiMinute, tInfo.ptFullData->tPicTime.uiSecondsr, tInfo.ptFullData->tPicTime.uiMilliseconds, i, _uiRecvID);

		CString cstrFacePicName;
		cstrFacePicName.Format("%s.jpg",cFacePathName);
		SavePicture(cstrFacePicName.GetBuffer(), tFace.pcPicData, tFace.iDataLen);

		//There is a face basemap
		if (FACE_ALARM_TYPE_BLACKLIST == tFace.iAlramType)	
		{	
			char cNegPathName[256] = {0};
			sprintf_s(cNegPathName, sizeof(cNegPathName), ".\\NetClientDemo\\FacePic\\Negative_Time(2%03d%02d%02d%02d%02d%02d%d)_Similar_%d_%u.jpg"
				, tInfo.ptFullData->tPicTime.uiYear, tInfo.ptFullData->tPicTime.uiMonth,  tInfo.ptFullData->tPicTime.uiDay, tInfo.ptFullData->tPicTime.uiHour
				, tInfo.ptFullData->tPicTime.uiMinute, tInfo.ptFullData->tPicTime.uiSecondsr, tInfo.ptFullData->tPicTime.uiMilliseconds, tFace.iSimilatity, _uiRecvID);

			CString cstrNegPicName;
			cstrNegPicName.Format("%s.jpg",cNegPathName);
			SavePicture(cstrNegPicName.GetBuffer(), tFace.pcNegPicData, tFace.iNegPicLen);
		}	
	}

	if(NULL != tInfo.ptEncrptKey && tInfo.iSizeOfPicStreamEncrptKey > 0) {
		char cKeyPathName[256] = {0};
		sprintf_s(cKeyPathName, sizeof(cKeyPathName), ".\\NetClientDemo\\FacePic\\PicKey_Time(2%03d%02d%02d%02d%02d%02d%d)_%u"
			, tInfo.ptFullData->tPicTime.uiYear, tInfo.ptFullData->tPicTime.uiMonth,  tInfo.ptFullData->tPicTime.uiDay, tInfo.ptFullData->tPicTime.uiHour
			, tInfo.ptFullData->tPicTime.uiMinute, tInfo.ptFullData->tPicTime.uiSecondsr, tInfo.ptFullData->tPicTime.uiMilliseconds, _uiRecvID);

		if (tInfo.ptEncrptKey->iFullViewPicKeyLen > 0) {
			CString cstrFullEncrptKeyName;
			cstrFullEncrptKeyName.Format("%s_Full.txt",cKeyPathName);
			SavePicture(cstrFullEncrptKeyName.GetBuffer(),(char*)tInfo.ptEncrptKey->ucFullViewPicKey, tInfo.ptEncrptKey->iFullViewPicKeyLen);
		}

		if (tInfo.ptEncrptKey->iFacePicKeyLen > 0) {
			CString cstrFaceEncrptKeyName;
			cstrFaceEncrptKeyName.Format("%s_Face.txt",cKeyPathName);
			SavePicture(cstrFaceEncrptKeyName.GetBuffer(),(char*)tInfo.ptEncrptKey->ucFacePicKey, tInfo.ptEncrptKey->iFacePicKeyLen);
		}

		if (tInfo.ptEncrptKey->iBasePicKeyLen > 0) {
			CString cstrNegEncrptKeyName;
			cstrNegEncrptKeyName.Format("%s_Neg.txt",cKeyPathName);
			SavePicture(cstrNegEncrptKeyName.GetBuffer(),(char*)tInfo.ptEncrptKey->ucBasePicKey, tInfo.ptEncrptKey->iBasePicKeyLen);
		}
	}
}

void DealPicStreamITS(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser)
{
	// parameter validity judgment
	if (NULL == _pvBuf || _iBufLen <= 0) 
	{		
		return;
	}

	//copy image stream data
	ItsPicStream tIts = {0};
	memcpy(&tIts, _pvBuf, min(_iBufLen, (int)sizeof(ItsPicStream)));

	// capture picture
	for (int i = 0; i < tIts.iPicCount && i < MAX_ITS_CAP_PIC_COUNT; ++i)
	{
		if (NULL == tIts.ptPicData[i])
		{
			continue;
		}

		PicData tData = {0};
		memcpy(&tData, tIts.ptPicData[i], min(tIts.iSize, sizeof(PicData)));
		char cFileName[256] = {0};
		sprintf_s(cFileName, sizeof(cFileName), ".\\NetClientDemo\\ITSPic\\ItsPic_Time(2%03d%02d%02d%02d%02d%02d%d)_%s_%d_%u.jpg"
			, tData.tPicTime.uiYear, tData.tPicTime.uiMonth,  tData.tPicTime.uiDay, tData.tPicTime.uiHour
			, tData.tPicTime.uiMinute, tData.tPicTime.uiSecondsr, tData.tPicTime.uiMilliseconds, tIts.cCameraIP, i, _uiRecvID);

		SavePicture(cFileName, tData.pcPicData, tData.iDataLen);
	}

	// license plate image
	if (NULL != tIts.ptPlatData)
	{
		PicData tData = {0};
		memcpy(&tData, tIts.ptPlatData, min(tIts.iSize, sizeof(PicData)));
		char cFileName[256] = {0};
		sprintf_s(cFileName, sizeof(cFileName), ".\\NetClientDemo\\ITSPic\\ItsPlate_Time(2%03d%02d%02d%02d%02d%02d%d)_%s_%u.jpg"
			, tIts.ptPicData[0]->tPicTime.uiYear, tIts.ptPicData[0]->tPicTime.uiMonth,  tIts.ptPicData[0]->tPicTime.uiDay, tIts.ptPicData[0]->tPicTime.uiHour
			, tIts.ptPicData[0]->tPicTime.uiMinute, tIts.ptPicData[0]->tPicTime.uiSecondsr, tIts.ptPicData[0]->tPicTime.uiMilliseconds, tIts.cCameraIP, _uiRecvID);

		SavePicture(cFileName, tData.pcPicData, tData.iDataLen);
	}

	// face image
	for (int i = 0; i < tIts.iFaceCount && i < MAX_ITS_CAP_FACE_COUNT; ++i)
	{
		if (NULL == tIts.ptFaceData[i])
		{
			continue;
		}

		PicData tData = {0};
		memcpy(&tData, tIts.ptFaceData[i], min(tIts.iSize, sizeof(PicData)));

		char cFileName[256] = {0};
		sprintf_s(cFileName, sizeof(cFileName), ".\\NetClientDemo\\ITSPic\\ItsFace_Time(2%03d%02d%02d%02d%02d%02d%d)_%s_%d_%u.jpg"
			, tIts.ptPicData[0]->tPicTime.uiYear, tIts.ptPicData[0]->tPicTime.uiMonth,  tIts.ptPicData[0]->tPicTime.uiDay, tIts.ptPicData[0]->tPicTime.uiHour
			, tIts.ptPicData[0]->tPicTime.uiMinute, tIts.ptPicData[0]->tPicTime.uiSecondsr, tIts.ptPicData[0]->tPicTime.uiMilliseconds, tIts.cCameraIP, i, _uiRecvID);

		SavePicture(cFileName, tData.pcPicData, tData.iDataLen);
	}
}

void DealSnapPicStream(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser)
{
	if (NULL == _pvBuf || _iBufLen <= 0) 
	{		
		return;
	}
	//copy image stream data
	SnapPicStream tSnapPicStream = {0};
	memcpy(&tSnapPicStream, _pvBuf, min(_iBufLen, (int)sizeof(SnapPicStream)));

	// capture picture
	for (int i = 0; i < tSnapPicStream.iPicCount && i < MAX_SNAP_PICTURE_COUNT; ++i)
	{
		if (NULL == tSnapPicStream.ptSnapData)
		{
			continue;
		}

		SnapPicData tData = {0};
		memcpy(&tData, tSnapPicStream.ptSnapData[i], min(tSnapPicStream.iSize, sizeof(SnapPicData)));
		char cFileName[256] = {0};
		sprintf_s(cFileName, sizeof(cFileName), ".\\NetClientDemo\\SnapPic\\SnapPic_2%03d%02d%02d%02d%02d%02d%d_%d_%d_%u.jpg"
			, tData.ptPicData->tPicTime.uiYear,  tData.ptPicData->tPicTime.uiMonth,   tData.ptPicData->tPicTime.uiDay,  tData.ptPicData->tPicTime.uiHour
			,  tData.ptPicData->tPicTime.uiMinute,  tData.ptPicData->tPicTime.uiSecondsr, tData.ptPicData->tPicTime.uiMilliseconds, tData.iSnapType, i, _uiRecvID);
		SavePicture(cFileName, tData.ptPicData->pcPicData, tData.ptPicData->iDataLen);
	}
}

void DealTransAlgData(unsigned int _uiRecvID, void* _pvBuf, int _iBufLen, void* _pvUser)
{
	TransAlgData tTransAlg = {0};
	memcpy(&tTransAlg, _pvBuf, min(_iBufLen, sizeof(TransAlgData)));
	if (NULL == tTransAlg.pvAlgData || 0 == tTransAlg.iAlgDataLen) {
		return;
	}

	char cFileName[256] = {0};
	SYSTEMTIME systemTime;
	GetSystemTime(&systemTime);
	sprintf_s(cFileName, sizeof(cFileName), ".\\NetClientDemo\\AlgData\\AlgPic_%04d%02d%02d%02d%02d%02d%d_%u.jpg"
		, systemTime.wYear,  systemTime.wMonth, systemTime.wDay, systemTime.wHour,  systemTime.wMinute, systemTime.wSecond, systemTime.wMilliseconds, _uiRecvID);
	SavePicture(cFileName, (char*)tTransAlg.pvAlgData, tTransAlg.iAlgDataLen);
}

int os_split_str_to_arr(char* _pcStr, char _cSplit, OsStrArray* _ptArray)
{
    if (NULL == _pcStr || NULL == _ptArray)
    {
        return -1;
    }

    int i = 0;
    for (; i < OS_MAX_ARRAY_COUNT; i++)
    {
        const char* init_null = "";
        _ptArray->pcStr[i] = (char*)init_null;
        _ptArray->iCount = 0;
    }

    int iCount = 0;
    int iLen = (int)strlen(_pcStr);
    _ptArray->pcStr[iCount++] = _pcStr;
    for (i = 0 ; i < iLen; i++)
    {
        if (_pcStr[i] == _cSplit)
        {
            _pcStr[i] = '\0';
            if (i == iLen - 1)
            {
                break;
            }

            if (iCount >= OS_MAX_ARRAY_COUNT)
            {
                return -1;
            }
            _ptArray->pcStr[iCount++] = _pcStr + i + 1;
        }
    }
    _ptArray->iCount = iCount;

    return 0;
}

unsigned int HexStringToUInt(char* _pcInput)
{
    if (NULL == _pcInput)
    {
        return 0;
    }

    if (_pcInput[0] == '0' && _pcInput[1] == 'x')
    {
        _pcInput += 2;
    }

    unsigned int uiResult = 0;
    unsigned int uiTemp = 0;

    int idx = 0;
    while ('\0' != _pcInput[idx])
    {
        if (_pcInput[idx] >= 'A' && _pcInput[idx] <= 'F')
        {
            uiTemp = 10 + _pcInput[idx] - 'A';
        }
        else if (_pcInput[idx] >= '0' && _pcInput[idx] <= '9')
        {
            uiTemp = _pcInput[idx] - '0';
        }

        uiResult = uiResult << (4*idx>0?4:0) | uiTemp;
        
        idx++;
    }

    return uiResult;
}

#define MAX_WIDE_CHAR_NUM 1440
int Utf8ToGbk(char* _pcInBuf, int _iInLen, char* _pcOutBuf, int _iOutLen)
{
	// parameter validity check
	if (NULL == _pcInBuf || NULL == _pcOutBuf) {
		return -1;
	}

	wchar_t wchar[MAX_WIDE_CHAR_NUM] = {0};
	int wclen = MultiByteToWideChar(CP_UTF8, 0, _pcInBuf, _iInLen, wchar, MAX_WIDE_CHAR_NUM); 
	if (wclen <= 0) {
		AddLog(LOG_TYPE_FAIL, "", "MultiByteToWideChar failed! data(%s)", _pcInBuf);
		return -1;
	}
	memset(_pcOutBuf, 0, _iOutLen); 
	int mblen = WideCharToMultiByte(CP_ACP, 0, wchar, wclen, _pcOutBuf, _iOutLen, NULL, NULL); 
	return mblen;
}

int isupperstr(char* _pcStrIn)
{
	int iRet = 1;
	char* pcTmp = _pcStrIn;
	if (NULL == pcTmp) {
		iRet = 0;
		goto Leave;
	}

	if ((unsigned)*pcTmp > 0x7f) {
		iRet = 0;
		goto Leave;
	}

	if (!isupper(*pcTmp)) {
		iRet = 0;
		goto Leave;
	} else {
		pcTmp++;
	}

	while (*pcTmp)
	{
		if (!isupper(*pcTmp) && (*pcTmp > '9' || *pcTmp < '0') && (*pcTmp != '_')) {
			iRet = 0;
			goto Leave;
		}

		pcTmp++;
	}

Leave:
	return iRet;
}

int GetFileMd5ForVodCheck(char *_pcFileName,char *_pcOutputBuf, int _iLen,__int64 *_pi64FileSize)
{
	int iRet = -1;
	if (NULL == _pcFileName || NULL == _pcOutputBuf || _iLen < LEN_32)
	{
		return iRet;
	}

	FILE*fp = fopen(_pcFileName,"rb");
	if(NULL == fp)
	{
		return iRet;
	}
	
	//bufszie 1024*1024 = 1M
	char *pBuf = new char[LEN_1024*LEN_1024];
	if(NULL == pBuf)
	{
		return iRet;
	}

	MD5_CTX context;
	unsigned char digest[16] = {0};
	MD5Init (&context);
	while (!feof(fp))
	{
		size_t iRealSize = fread(pBuf,1,LEN_1024*LEN_1024,fp);
		if (iRealSize >= LEN_1024)
		{
			MD5Update (&context, (unsigned char *)pBuf, LEN_1024);
		}
	}
	if(NULL != pBuf)
	{
		delete []pBuf;
		pBuf = NULL;
	}

	_fseeki64(fp,0,SEEK_END);
	*_pi64FileSize = _ftelli64(fp);
	fclose(fp);
	fp = NULL;

	MD5Final (digest, &context);

	unsigned char digeststr[LEN_32+1] = {0};
	MDPrint (digest,(unsigned char *)digeststr);
	memcpy(_pcOutputBuf,digeststr,LEN_32);

	iRet = 0;
	return iRet;
}

void AbsSecondsToNvsFileTime( NVS_FILE_TIME* _fileTime,long _tTime )
{
	time_t ltime = (time_t)_tTime;
	struct tm pTime = {0};
	gmtime_s(&pTime, &ltime);
	_fileTime->iYear = pTime.tm_year + 1900;
	_fileTime->iMonth = pTime.tm_mon + 1;
	_fileTime->iDay = pTime.tm_mday;
	_fileTime->iHour = pTime.tm_hour;
	_fileTime->iMinute = pTime.tm_min;
	_fileTime->iSecond = pTime.tm_sec;
}

unsigned int NvsFileTimeToAbsSeconds( NVS_FILE_TIME * _pFileTime )
{
	tm tm_Begin = {0};
	tm_Begin.tm_year = _pFileTime->iYear - 1900;
	tm_Begin.tm_mon  = _pFileTime->iMonth - 1;
	tm_Begin.tm_mday = _pFileTime->iDay;
	tm_Begin.tm_hour = _pFileTime->iHour;
	tm_Begin.tm_min  = _pFileTime->iMinute;
	tm_Begin.tm_sec  = _pFileTime->iSecond;

	time_t timenow, timeSpace;
	tm  pTimeg= {0} ;
	time(&timenow);
	gmtime_s(&pTimeg, &timenow);
	timeSpace = timenow - mktime(&pTimeg);
	time_t begin = mktime(&tm_Begin) + timeSpace;
	unsigned int uiSeconds = (unsigned int)begin;
	return uiSeconds;
}

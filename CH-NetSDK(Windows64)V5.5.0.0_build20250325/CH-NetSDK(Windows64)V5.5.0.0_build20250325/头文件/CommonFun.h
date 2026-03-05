#ifndef __COMMONFUN_H__
#define __COMMONFUN_H__



/*****************************************************************************
 Function name		 : ToIntDef
 Function description: Convert strings to an integer value.
 Param1[input]		 : _pstrFrom:Null-terminated string to convert.
 Param2[input]		 : _iDef:Initialize the default value.
 Return value		 : returns the converted value.
*****************************************************************************/
int	ToIntDef(const char* _pstrFrom,int _iDef = 0);


/*****************************************************************************
 Function name		 : gets_s
 Function description: The gets_s function reads a line from the standard input stream stdin and stores it in buffer.
 Param1[output]		 : _pcStr:Storage location for input string.
 Param2[input]		 : _iCount:The size of the buffer.
 Return value		 : return 0 is success.
*****************************************************************************/
#ifndef WIN32
int gets_s(char *_pcStr, int _iCount);
#else
#endif


#endif //__COMMONFUN_H__

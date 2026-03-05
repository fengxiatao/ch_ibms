#ifndef CONNECTVIDOE_H
#define CONNECTVIDOE_H

#include "stdafx.h"
#include "NetSDK.h"



class CLS_ConnectServer
{
public:	
	//destructor
	~CLS_ConnectServer();
	//Log in to the video server process
	void ConnectServerProcess( CString _csIp, CString _csPort, CString _csUsername, CString _csPassword );
	// get an instance of this class
	static CLS_ConnectServer* GetInstance()
	{
		if ( m_sInstace == NULL)
		{
			m_sInstace = new CLS_ConnectServer();
		}
		return m_sInstace;
	}

public:
	int m_iLogonID;//Logon ID
	unsigned int m_uiConnID;//Connection ID
	HWND m_hMainWindow;//Main window handle
	static NVS_FILE_DATA m_sFileInfo[MAX_PATH];//In the file mode, save the file information
	static BOOL m_bLogOnFlag;//Sign whether the login is successful
private:
	//Constructor
	CLS_ConnectServer():m_iLogonID(-1), m_uiConnID(-1), m_hMainWindow(::FindWindow(NULL, "VideoDisplay")){}
	static CLS_ConnectServer *m_sInstace;//Instance of this class

};
#endif //CONNECTVIDOE_H
#include "stdafx.h"
#include "NetSdk.h"
#include "ConnectServer.h"
#include "DisplayDlg.h"
#include "VideoDisplayDlg.h"
#include "OperateByTimeDlg.h"
#include "MacroDefine.h"

//Callback function declaration
void Notify_Main(int _iLogonID, long _iWparam, void* _iLParam,void* _iUser );
CLS_ConnectServer* CLS_ConnectServer::m_sInstace = NULL;//Initialization of this class instance
NVS_FILE_DATA CLS_ConnectServer::m_sFileInfo[MAX_PATH] = {0};
BOOL CLS_ConnectServer::m_bLogOnFlag = FALSE;

void Notify_Main(int _iLogonID, long _iWparam, void* _iLParam,void* _iUser )
{
	int iMsgType = LOWORD(_iWparam);
	switch (iMsgType)
	{
	//login status message
	case WCM_LOGON_NOTIFY:
		{
			if( _iLParam == LOGON_SUCCESS )
			{
				CLS_ConnectServer::GetInstance()->m_bLogOnFlag = TRUE;
				if( CLS_ConnectServer::GetInstance()->m_hMainWindow != NULL)
				{
					//Send a login successful message to the main window
					::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_NEW_DISPLAY_DIALOG, 0, 0);
				}
			}
			else
			{
				CLS_ConnectServer::GetInstance()->m_bLogOnFlag = FALSE;
				if( CLS_ConnectServer::GetInstance()->m_hMainWindow != NULL)
				{
					//Send a login failed message to the main window
					::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_LOGON_FAILED, 0, 0);
				}
			}
		}
		break;
	//query end message
	case WCM_QUERYFILE_FINISHED:
		{
			int iTotalCount = 0;
			int iCurrentCount = 0;
			//Query the number of documents
			NetClient_NetFileGetFileCount(_iLogonID, &iTotalCount, &iCurrentCount);
			if( (0 == iTotalCount) || ( 0 == iCurrentCount ) )
			{
				//  Send a message to the target window
				::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_COUNT, 0, 0 );
				break;
			}

			NVS_FILE_DATA nvsFileInfo = {0};
			for(int i = 0; i < iCurrentCount; ++i)
			{   
				//Get file information
				int iRet = NetClient_NetFileGetQueryfile(_iLogonID, i, &nvsFileInfo);
				if(0 == iRet)
				{
					CLS_ConnectServer::m_sFileInfo[i] = nvsFileInfo;
					if( CLS_ConnectServer::GetInstance()->m_hMainWindow != NULL)
					{
						//Send a message to the main window every time the file information is obtained
						::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_INFO, iTotalCount, 0);
					}
				}
				else
				{
					::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_DOWNLOAD_FAILED, 0, 0 );
				}
			}
		}
		break;
	//download end message
	case WCM_DWONLOAD_FINISHED:
		{
			
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_DOWNLOAD_FINISH, 0, 0 );
		}
		break;	
	//download error message
	case WCM_DWONLOAD_FAULT:
		{
			map<CString, int>::iterator iter;
			for( iter = CLS_OperateByFileDlg::m_mapConnId.begin(); iter != CLS_OperateByFileDlg::m_mapConnId.end(); ++iter)
			{
				if( (iter->second) == (unsigned long)_iLParam )
				{
					CLS_OperateByFileDlg::m_mapConnId.erase(iter);
					NetClient_StopRecv( (unsigned long)_iLParam );
					::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_DOWNLOAD_FAULT, 0, 0 );
					break;
				}
			}		
		}
		break;
	//Download interruption message
	case WCM_DOWNLOAD_INTERRUPT:
		{
			NetClient_StopRecv( CLS_ConnectServer::GetInstance()->m_uiConnID );
			::PostMessage( CLS_ConnectServer::GetInstance()->m_hMainWindow, WM_MSG_FILE_DOWNLOAD_INPURRT, 0, 0 );
		}
		break;
	default:
		break;
	}
}


//CLS_ConnectServer::CLS_ConnectServer()
//{
//	m_iLogonID = -1;
//	m_uiConnID = -1;
//	m_MainWindowHand = ::FindWindow(NULL, L"VideoDisplay");
//
//}

CLS_ConnectServer::~CLS_ConnectServer()
{
	if( m_sInstace != NULL )
	{
		delete m_sInstace;
		m_sInstace = NULL;
	}
}



void CLS_ConnectServer::ConnectServerProcess( CString _csIp, CString _csPort, CString _csUsername, CString _csPassword )
{
/*	char cIp[20] = {0};
	sprintf(cIp,"%s", _csIp );
	char cUsername[20] = {0};
	sprintf( cUsername,"%s", _csUsername );
	char cPassword[20] = {0};
	sprintf( cPassword,"%s", _csPassword );

	
	tLogonPara.iSize = sizeof(LogonPara);
	strcpy(tLogonPara.cUserName, cUsername);
	strcpy(tLogonPara.cUserPwd ,cPassword);
	strcpy(tLogonPara.cNvsIP, cIp)*/;

	LogonPara tLogonPara = {0};
	sprintf(tLogonPara.cUserName, _csUsername);
	sprintf(tLogonPara.cUserPwd ,_csPassword);
	sprintf(tLogonPara.cNvsIP, _csIp);


	tLogonPara.iNvsPort = _ttoi(_csPort);

	m_iLogonID = NetClient_Logon_V4(SERVER_NORMAL, &tLogonPara, sizeof(tLogonPara));

	//Log in to the device
	//m_iLogonID = NetClient_Logon( "",cIp , cUsername, cPassword, "",  _ttoi(_csPort) );
	if(m_iLogonID < 0)
	{
		m_bLogOnFlag = FALSE;
		if(m_hMainWindow != NULL)
		{
			//Send a login failed message to the main window
			::PostMessage(m_hMainWindow, WM_MSG_LOGON_FAILED, 0, 0);
		}
	}
}

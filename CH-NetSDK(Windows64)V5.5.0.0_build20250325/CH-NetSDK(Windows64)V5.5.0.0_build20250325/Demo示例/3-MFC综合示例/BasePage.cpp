#include "StdAfx.h"
#include "BasePage.h"
#include "../Common/Ini.h"

//0: single tripwire 1: double tripwire 2: perimeter detection 3: wandering 4: parking 5: running 6: density of people in the area 7: stolen 8: abandoned 9: face recognition
//10: Video diagnosis 11: Intelligent tracking 12: Traffic statistics 13: Crowd gathering 14: Off-post detection 15: Water level monitoring 16: Audio diagnosis 17: Face occlusion (mosaic)
//18: River floating objects 19: Unauthorized mining and unloading 20: Illegal parking 21: Fighting 22: Vigilance 23: License plate recognition (only for NVR, traffic front-end uses traffic protocol)
//24-heat map 25-water monitoring 26-window detection 27-ST face recognition 28-parking guard 30-hard hat detection algorithm 31-fish ball linkage tracking 32 gate detection
//33-Color Tracking 34-Structured
//The subscript index is bound to the VCA event ID
int g_iEventIDS[VCA_EVENT_MAX] = {
	IDS_VCA_ALARM_EVENT_TRIPWIRE,		//0 - single tripwire
	IDS_VCA_ALARM_EVENT_DBTRIPWIRE,		//1 - double tripwire
	IDS_VCA_ALARM_EVENT_PERIMETER,		//2- perimeter detection
	IDS_VCA_ALARM_EVENT_LOITER,			//3 - hover
	IDS_VCA_ALARM_EVENT_PARKING,		//4 - Parking
	IDS_VCA_ALARM_EVENT_RUN,			//5-run
	IDS_VCA_ALARM_EVENT_HIGH_DENSITY,	//6- Density of people in the area
	IDS_VCA_ALARM_EVENT_OBJSTOLEN,		//7-Stolen
	IDS_VCA_ALARM_EVENT_ABANDUM,		//8-Abandoned
	IDS_VCA_ALARM_EVENT_FACEREC,		//9-Face recognition
	IDS_VCA_ALARM_EVENT_VIDEODETECT,	//10-Video diagnostics
	IDS_VCA_ALARM_EVENT_TRACK,			//11-Smart Tracking
	IDS_VCA_EVENT_FLUX,					//12-traffic statistics
	IDS_VCA_EVENT_CROWD_NEW,			//13-Crowd gathering
	IDS_VCA_EVENT_LEAVE_DETECT,			//14-Departure detection
	IDS_CFG_FUNC_WATER_LEVEL,			//15-Water level monitoring
	IDS_VCA_EVENT_AUDIO_DIAGNOSE_NEW,	//16-audio diagnostics
	IDS_CFG_FUNC_FACE_COVER,			//17-face occlusion
	IDS_VCA_EVENT_RIVER,				//18-River Floater
	IDS_VCA_EVENT_DREDGE,				//19-Pirate mining and unloading
	IDS_CONFIG_ITS_ILLEGALPARK,			//20-Illegal parking
	IDS_VCA_EVENT_FIGHT,				//21-fight
	IDS_PROTECT,						//22-Alert
	IDS_VCA_IDS_LISENCE,				//23-License plate recognition
	IDS_CFG_FEC_HEAT_MAP,				//24-heat map
	IDS_CFG_SEEPER,						//25-Water monitoring
	IDS_WINDOW_DETECTION,				//26-window detection
	IDS_VCA_EVENT_FACEREC,				//27-ST face recognition
	IDS_CONFIG_ITS_ILLEGALPARK,			//28-Parking Guard
	IDS_UNKNOW_CONNECT,					//29-unknown
	IDS_CFG_HELMET,						//30-Hard hat detection
	IDS_CFG_LINKDOMETRACK,				//31-Fish ball linkage tracking
	IDS_VCA_SLUICEGATE,					//32-gate detection
	IDS_VCA_EVENT_COLOR_TRACK,			//33 - Color Tracking
	IDS_VCA_STRUCTURED,					//34 - Structured
	IDS_CFG_SEDIMENT,                   //35-Water detection
	IDS_CFG_ALERTWATER,                  //36-warning water level
	IDS_VCA_EVENT_SINGLE_INQUIRY,					//37-Single Inquiry or Unattended    
	IDS_VCA_EVENT_CLIMB_UP,							//38-Hight limit          
	IDS_VCA_EVENT_NET_DEPARTURE,					//39-New Departure     
	IDS_VCA_EVENT_ABNORMAL_NUMBER,					//40-Abnormal Number   
	IDS_VCA_EVENT_GET_UP,							//41-Get Up			
	IDS_VCA_EVENT_LEAVE_BED,						//42-Leave Bed         
	IDS_VCA_EVENT_STATIC_DETECTION,					//43-Static Detection  
	IDS_VCA_EVENT_SLEEP_POSTION ,					//44-Sleep Postion    
	IDS_VCA_EVENT_SLIP_UP,							//45-Slip Up			
	IDS_VCA_EVENT_NEW_FIGHT,						//46-New Fight         
	IDS_VCA_EVENT_BODY_TOUCH,						//47-Body Touch        
	IDS_VCA_EVENT_HUMAN_DETECT,						//48-Human Detect      
	IDS_VCA_EVENT_DAM_AMARM,						//49-DAM_AMARM         
	IDS_VCA_EVENT_NET_AMARM,						//50-NET_AMARM,         
	IDS_VCA_EVENT_VCA_PEPT,							//51-PEPT	        
	IDS_VCA_EVENT_VCA_FLOWSPEED,					//52-Flow Speed     
	IDS_VCA_EVENT_BEACON_SHIP,						//53-Beacon ship		
	IDS_VCA_EVENT_BRIGHT_KITCHEN,					//54-bright kitchen	
	IDS_VCA_EVENT_STRANDED,							//55-stranded			
	IDS_VCA_EVENT_SINGLE_ALONE,						//56-single alone		
	IDS_VCA_EVENT_WINDOW_DELIVERY,					//57-window delivery	
	IDS_VCA_EVENT_ZHONGYI,							//58-Smoking   		
	IDS_VCA_EVENT_WEAR_MASK,						//59-wear mask			
	IDS_VCA_EVENT_NOT_WEAR_MASK,					//60-not wear mask		
	IDS_VCA_EVENT_PHONE,							//61-call phone             
	IDS_VCA_EVENT_EVETEMDETECT,						//62-environment temperature detect      
	IDS_VCA_EVENT_TEMDETECT,						//63-human temperature detect]  		
	IDS_VCA_EVENT_FIREWORKDETECT,					//64-Fire Work Detect    
	IDS_VCA_EVENT_PLATENUMBER_BLACKLIST,			//65-plate number in blacklist     
	IDS_VCA_EVENT_SMART_MOVE,						//66-Smart move		
	IDS_VCA_EVENT_INUIRY_TIMEOUT,					//67-Inuiry Timeout	
	IDS_VCA_EVENT_ELECTRIC_VEHICLE,					//68-Indoor electric vehicle detection	
	IDS_VCA_EVENT_LEAVE_SEAT,						//69-Leave seat		
	IDS_VCA_EVENT_SCENE_REC,						//70-Scene rec			
	IDS_VCA_EVENT_CONTRA_BAND,						//71-Contra band		
	IDS_VCA_EVENT_BED_REST,							//72-bed rest			
	IDS_VCA_EVENT_ATTENDED,							//73-attended 			
	IDS_VCA_EVENT_DOOR,								//74-door open				
	IDS_VCA_EVENT_POSEREC,							//75-poserec			
	IDS_VCA_EVENT_CONVERSE,							//76-converse			
	IDS_VCA_EVENT_COURTPII,							//77-courtpii			
	IDS_VCA_EVENT_COURTELP,							//78-elawperson			
	IDS_VCA_EVENT_BEHAVIREC,						//79-behaviour rec			
	IDS_VCA_EVENT_INCLINED_STATIS,					//80-inclined stats	
	IDS_VCA_EVENT_VERTICAL_STATIS,					//81-vertical inclined stats	
	IDS_VCA_EVENT_PERSON_GATHER,					//82-personal gathering		
	IDS_VCA_EVENT_NROMAL_BODY_TEMPERATURE,			//83-normal body temperature 	
	IDS_VCA_EVENT_PERSON_DENSITY,					//84-Personnel density			
	IDS_VCA_EVENT_VEHICLE_DENSITY,					//85-vehicle density			
	IDS_VCA_EVENT_TAFFIC_JAM,						//86-traffic jam				
	IDS_VCA_EVENT_VEHICLE_STANDED,					//87-vehicle stranded			
	IDS_VCA_EVENT_ABNORMAL_PARKING,					//88-abnormal parking			
	IDS_VCA_EVENT_CROSS_CONGESTION,					//89-cross congestion			
	IDS_VCA_EVENT_JUDGE_BEHAVIOR_ANALYZE,			//90-Judge's Behavior Analysis	
	IDS_VCA_EVENT_VERTICALHUMAN_DETECTION,			//91-vertical human detection	
	IDS_VCA_EVENT_AERIAL_PROJECTILE,				//92-aerial projectile			
	IDS_VCA_EVENT_WATER_OUTFALL,					//93-sewage outfall monitoring				
	IDS_VCA_EVENT_POLICE_UNIFORM_DETECTION,			//94-police uniform detection	
	IDS_VCA_EVENT_SPDRESS_DETECTION,				//95-Supervised personnel identification dress detection			
	IDS_VCA_EVENT_SPQUEUE_DETECTION,				//96-Supervised personnel queue detection			
	IDS_VCA_EVENT_VEHICLE_IDENTIFY,					//97-Vehicle identification			
	IDS_VCA_EVENT_ACTIVE_STRATEGY					//98-Active strategy			
};

CLS_BasePage::CLS_BasePage(UINT nIDTemplate,CWnd* pParentWnd)
: CLS_BaseWindow(nIDTemplate, pParentWnd)
{
	m_iLogonID = -1;
	m_iChannelNO = 0;
	m_iStreamNO = 0;
	m_pDlgVideoView = NULL;
}

CLS_BasePage::~CLS_BasePage()
{
	if (m_pDlgVideoView)
	{
		m_pDlgVideoView->DestroyWindow();
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
	}
}
void CLS_BasePage::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	if (m_pDlgVideoView)
	{
		m_pDlgVideoView->OnMainNotify(_iLogonID, _wParam, _iLParam, _iUser);
	}
}

void CLS_BasePage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	m_iChannelNO = _iChannelNo;
	m_iStreamNO = _iStreamNo;
}

void CLS_BasePage::GetPointsFromString(CString _strPoints, int _iPointNum, POINT* _poPoint)
{
	int iLength = _strPoints.GetLength()+1;
	char* pcData = new char [iLength];
	memset(pcData, 0, iLength);
	memcpy(pcData, _strPoints.GetBuffer(), iLength-1);
	char* p1 = pcData;
	char* p2 = NULL;
	int iPointIndex = 0;
	for (int i = 0; i < iLength; ++i)
	{
		p2 = strstr(p1, ")");
		if (p2 == NULL)
			break;

		char cCell[200] = {0};
		int iX = 0, iY = 0;
		memcpy(cCell, p1, p2-p1+1);
		sscanf_s(cCell, "(%d,%d)", &iX, &iY);
		_poPoint[iPointIndex].x = iX;
		_poPoint[iPointIndex].y = iY;
		if (++iPointIndex == _iPointNum)
			break;

		if ((p1 = p2+1) >= pcData+iLength)
			break;
	}
	delete [] pcData;
	pcData = NULL;
}

CString CLS_BasePage::GetHDTemplateName(char* pTemplateName)
{
	CString cstrTamplateName;
	cstrTamplateName.Format("%s",pTemplateName);
	if (cstrTamplateName == "outdoor" && !GetTextEx(IDS_HD_MODE_OUTDOOR).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_OUTDOOR);
	}
	else if(cstrTamplateName == "indoor" && !GetTextEx(IDS_HD_MODE_INDOOR).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_INDOOR);
	}
	else if(cstrTamplateName == "motion" && !GetTextEx(IDS_HD_MODE_MOTION).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_MOTION);
	}
	else if(cstrTamplateName == "wdr" && !GetTextEx(IDS_HD_MODE_WDR).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_WDR);
	}
	else if(cstrTamplateName == "dark" && !GetTextEx(IDS_HD_MODE_DARK).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_DARK);
	}
	else if(cstrTamplateName == "bright" && !GetTextEx(IDS_HD_MODE_BRIGHT).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_BRIGHT);
	}
	else if(cstrTamplateName == "colorful" && !GetTextEx(IDS_HD_MODE_COLORFUL).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_COLORFUL);
	}
	else if(cstrTamplateName == "customized" && !GetTextEx(IDS_HD_MODE_CUSTOMIZED).IsEmpty())
	{
		cstrTamplateName = GetTextEx(IDS_HD_MODE_CUSTOMIZED);
	}

	return cstrTamplateName;
}

void CLS_BasePage::GetNvsFileTime(CDateTimeCtrl* _pDt, OUT NVS_FILE_TIME &_tTime)
{
	if (NULL == _pDt)
	{
		return;
	}

	CTime ctTm;
	_pDt->GetTime(ctTm);
	_tTime.iYear = ctTm.GetYear();
	_tTime.iMonth = ctTm.GetMonth();
	_tTime.iDay = ctTm.GetDay();
	_tTime.iHour = ctTm.GetHour();
	_tTime.iMinute = ctTm.GetMinute();
	_tTime.iSecond = ctTm.GetSecond();
}

CString CLS_BasePage::GetWidgetText(int nId)
{
	CString cstrTxt;
	CWnd* pWnd = GetDlgItem(nId);
	if (NULL != pWnd)
	{
		pWnd->GetWindowText(cstrTxt);
	}
	return cstrTxt;
}

int CLS_BasePage::GetDemoUseRule()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\DemoUseRule.ini";
	CIniFile DemoFile(szNewFile);

	CString szSection = "UseRule";
	CString szKey = "Rule";

	return DemoFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, 0);
}

#ifndef _BASE_PAGE_H
#define _BASE_PAGE_H

#include "BaseWindow.h"
#include ".\Config\Events\VideoViewForDraw.h"

#define TEMP_TEMPLATE_INDEX		255
const int CONST_LENGTH_TWO = 2;
const int CONST_LENGTH_THREE = 3;
const int CONST_MIN_SLIDER = 0;												//Slider control minimum value
const int CONST_MAX_SLIDER = 255;											//slider control maximum
const int CONST_MAX_100 = 100;

#define MAX_DEV_STATE_COUNT	6		//Total number of device status types
typedef enum 
{
	n_Device_Type_RedLight = 0,				// red light status
	n_Device_Type_Senser_IsOnline,			//Car detector status
	n_Device_Type_Senser_Loop_IsOnline,		//coil state
	n_Device_Type_Cam_Temp,					// camera temperature
	n_Device_Type_Singnal_IsOnline,			// red light signal detector status
	n_Device_Type_Illumination,				//image brightness (current lighting)
}n_DEVICE_TYPE;

//#define XML_PROTOCOL

class CLS_BasePage :
	public CLS_BaseWindow
{
public:
	CLS_BasePage(UINT nIDTemplate,CWnd* pParentWnd = NULL);//need this 
	~CLS_BasePage();
	static CString GetHDTemplateName(char* pTemplateName);//Convert the template name returned by the device to the content displayed on the interface
	
	int		m_iLogonID;
	int		m_iChannelNO;
	int		m_iStreamNO;

	CLS_VideoViewForDraw* m_pDlgVideoView;

	void GetNvsFileTime(CDateTimeCtrl* _pDt, OUT NVS_FILE_TIME &_tTime);
	CString GetWidgetText(int nId);
	virtual void OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo );
	int GetDemoUseRule();
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	virtual void GetPointsFromString(CString _strPoints, int _iPointNum, POINT* _poPoint);
};

#endif

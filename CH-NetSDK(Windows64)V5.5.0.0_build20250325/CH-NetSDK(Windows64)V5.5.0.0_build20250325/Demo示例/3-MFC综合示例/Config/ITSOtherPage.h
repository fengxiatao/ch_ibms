#ifndef _ITS_OTHER_PAGE_H
#define _ITS_OTHER_PAGE_H

#include "../BasePage.h"
#include "afxwin.h"

static void __stdcall cbkRecvDataEx(unsigned long _ulID,unsigned char* _ucData,int _iLen, int _iFlag, void* _lpUserData);

#define  MAX_PICTURE_NUM 3
#define  PIC_CRC_NUM 3
#define  PACKET_HEAD_LEN 16
typedef  unsigned int uint;
typedef struct
{
	unsigned char	m_ui8PkgHead[PACKET_HEAD_LEN]; //Header //{16,14,12,10,8,6,4,2,1,3,5,7,9,11,13,15}
	uint	m_ui32DataType;						//type of data, 
	int   	m_iChannelID;						//lane number //fill with 0

	char  	m_cPlate[32];						// license plate
	//The first 16char is copied, the last 16char is 0, and the first snapshot is all 0.
	int   	m_iPlateColor;			            //License plate color, this parameter is set to 0 for the first snapshot
	int   	m_iPlateType;			            //Vehicle type, this parameter is set to 0 for the first snapshot
	int	  	m_iCarColor;			            //Body color, this parameter is set to 0 for the first snapshot
	//The camera outputs 32 colors, but the terminal converts to 10 colors according to the national standard
	RECT	m_stPlateRange;			            //License plate range, this parameter is set to 0 for the first snapshot
	int   	m_iCharConfid[12];	                //The confidence level of each character, the maximum is 12 characters, the first 8 copies and the last 4 are 0, the range of each value is 0-1024, , this parameter is set to 0 for the first snapshot
	int   	m_iCharNum;					        //The number of characters is filled with 8 --, the parameter of the first snapshot is set to 0
	int   	m_iPlateConfid;				        //Confidence of the whole license plate, the parameter is set to 0 for the first snapshot image
	int   	m_iRecoNum;					        //Identify the picture serial number //There are 3 pictures in total, which are recognized as the second picture, and the fixed fill is 2, which is used for the back-end secondary identification.
	float 	m_fSpeed;							//vehicle speed //fill with 0
	int 	m_iVehicleDirection;				//vehicle direction //fill with 0
	int		m_iAlarmType;						//alarm type
	char  	m_cCameraIP[16];					//Camera IP //Format example: 127.0.0.1
	int   	m_iCaptureInfoNum;					//Record the number of pictures included //Fixed to 1, pay attention to the modification here, from 3 to 1
	int     m_iRedBeginTime;					//Start time of red light, in seconds //fill with 0
	int     m_iRedEndTime;						//End time of red light, in seconds //fill with 0
	char  	m_stCaptureTime[MAX_PICTURE_NUM][8];//The capture time of each picture: year-month-day-week-hour-minute-second-millisecond
	//Note that according to the current practical application, there is only one picture. Here, clear stCaptureTime[1][8] and stCaptureTime[2][8] to all 0, and only copy the capture time to stCaptureTime[0][8]
	int	  	m_iCaptureLen[MAX_PICTURE_NUM];	    // length of each image
	//Note, the same as above, fill the current image length to m_iCaptureLen[0], and fill the others with 0
	//Parking field (exclusive field for this probject):
	int 		m_iPreset;					    // preset number
	int 		m_iArea;					    // area number
	char 		fileName[32];				    //recording file name
	long		m_iCarSerialNum;			    //The number of the illegally parked vehicle, the number is unique for the same device
	int			m_iPictureNum;				    //The picture number in 3 snapshots, 1, 2 or 3
	unsigned char	m_ui8PkgTail[PACKET_HEAD_LEN];		//Package end {15,13,11,9,7,5,3,1,2,4,6,8,10,12,14,16};
}STRCT_Record;

// CLS_ITSOtherPage dialog

class CLS_ITSOtherPage : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ITSOtherPage)

public:
	CLS_ITSOtherPage(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_ITSOtherPage();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_ITS_OTHER };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);

private:
	void UI_UpdateDialog();
	BOOL UI_UpdateOther();
	void UpdateEnable();
	void UI_UpdateWorkmode();
	void UI_UpdateLinkCamera();

private:
	CEdit m_edtDeviceCode;
	CEdit m_edtRoadName;
	CButton m_btnCamLocation;
	CEdit m_edtWokeMode;
	CEdit m_edtInterval;
	CButton m_btnWokeMode;
	CComboBox m_cboPicChan;
	CComboBox m_cboCapChan;
	CComboBox m_cboNetMode;
	CButton m_chkConnPic;	
	CButton m_btnConnect;
	CButton m_btnDisconnect;
	CComboBox m_cboEnableMode;
	CComboBox m_cboEnable;
	CComboBox m_cboDevSta;
	CComboBox m_cboLineNo;
	CButton m_chkLinkEnable;
	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;

public:
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnBnClickedButtonCamlocation();
	afx_msg void OnBnClickedButtonWorkmode();
	afx_msg void OnBnClickedButtonConnect();
	afx_msg void OnBnClickedButtonDisconnect();
	afx_msg void OnBnClickedButtonItsEnableSet();
	afx_msg void OnCbnSelchangeComboItsEnableMode();

	afx_msg void OnBnClickedButtonItsJpegsizeSet();
	afx_msg void OnBnClickedButtonItsJpegqualitySet();
	afx_msg void OnCbnSelchangeComboItsDevsta();
	afx_msg void OnBnClickedBtnLinkSet();
	afx_msg void OnCbnSelchangeCboLineNo();
};

#endif

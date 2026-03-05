#ifndef _ITS_PLATFORM_CONFIG_H_
#define _ITS_PLATFORM_CONFIG_H_

#include "../BasePage.h"
#include "afxwin.h"
#include "afxdtctl.h"
// CLS_PlatformConfig dialog

#define PIC_NAME_SECTION_MIN				1
#define	PIC_NAME_SECTION_HOST_ID			1 
#define	PIC_NAME_SECTION_ROAD_ID			2
#define	PIC_NAME_SECTION_DEV_ID				3
#define	PIC_NAME_SECTION_DETECT_AREA_ID		4
#define	PIC_NAME_SECTION_DIRECTION_ID		5
#define	PIC_NAME_SECTION_CAP_TIME			6    
#define	PIC_NAME_SECTION_MAX				7    		//The image name format contains up to 6 fields, numbered 1~6

typedef enum __tagSeparatorType
{
	E_SEPARATOR_LINE = 0		//Indicates that "_" is currently used to separate
}SeparatorType, *LPSeparatorType;


class CLS_ITSPlatformConfig : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_ITSPlatformConfig)

public:
	CLS_ITSPlatformConfig(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_ITSPlatformConfig();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_ITS_STAR_NVR_PLAT_CFG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData);

private:
	void UI_UpdateDialog();				//Initialize interface controls
	CString GetSeparatorType(SeparatorType _enumSeparatorType);

public:
	int m_iLogonId;
	int m_iChannelNo;
	CComboBox m_cboCustomNameEnable;
	CComboBox m_cboSeparator;
	CComboBox m_cboVersion;
	CComboBox m_cboResponseEnable;
	CButton m_chkPicNameSection[PIC_NAME_SECTION_MAX];
	CDateTimeCtrl m_timeStartTime;
	CDateTimeCtrl m_timeEndTime;

public:
	afx_msg void OnBnClickedBtnPaltformCfgSet();
	afx_msg void OnBnClickedBtnPicunloadSet();
	afx_msg void OnBnClickedBtnPicdelSet();
	int GetTimeWidgetValue(CDateTimeCtrl* _ptimeWidget, NVS_FILE_TIME* _ptNvsTime);
	int SetTimeWidgetValue(CDateTimeCtrl* _ptimeWidget, NVS_FILE_TIME _tNvsTime);
	void UI_UpdatePlatformCfg();
	void UI_UpdatePicunload();
	void UI_UpdatePicdel();
};

#endif

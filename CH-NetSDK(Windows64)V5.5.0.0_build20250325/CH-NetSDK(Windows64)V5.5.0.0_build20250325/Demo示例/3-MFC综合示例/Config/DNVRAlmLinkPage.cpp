// DNVRAlmLinkPage.cpp : implementation file
//

#include "stdafx.h"
#include "DNVRAlmLinkPage.h"

//#define PORT_ALARM				6
#define VIDEO_LOST				0
#define VIDEO_MOTION			1
#define VIDEO_COVER				2
#define AUDIO_LOST				3
#define TEMPERATURE_ALARM		4
//#define ILLEGAL_DETECT			5
#define RAINFALL_ALARM          5
#define ALERTWATER_ALARM        6
#define ZF_VCA			        7
#define DANGER_AREA_ALARM		8
#define BLACK_BODY_ERR_ALARM	9
#define PAGE_ALARM_TYPE_VCA     10
#define PAGE_ALARM_TYPE_MOTHERBOARD_DISASSEMBLE     11 //35
#define PAGE_ALARM_TYPE_OUT_CARDREADER_DISASSEMBLE  12 //36
#define PAGE_ALARM_TYPE_BUTTON                      13 //37
#define PORT_ALARM				14
#define UIIDX_VEHICLE_IDENTIFY						15
#define UIIDX_WATER_ELECTRONIC_OVERRANGE			16

#define LINK_RECORD				0
#define LINK_SNAP				1
#define LINK_OUTPORT			9
#define LINK_AUDIO_VIDEO		2
#define LINK_PTZ				3
#define LINK_SINGLEPIC			4
#define LINK_MAIL				5
#define LINK_HTTP				6
#define LINK_LASER				7
#define LINK_WHITE              8
#define UIIDX_LINK_ALARM_LAMP			10
#define UIIDX_LINK_UPLOAD_ALARM			11
#define UIIDX_LINK_LINKAPP				12
#define UIIDX_LINK_INTELLIGENT_ALGO		13

#define NO_LINK					0
#define PRESET					1
#define TRACK					2
#define CRUISEPATH				3

#define PTZ_TYPE_NONE			0
#define PTZ_TYPE_PRESET			1
#define PTZ_TYPE_TRACK			2
#define PTZ_TYPE_CRUISEPATH		3
#define PTZ_TYPE_SCENE			4


#define VCA_TYPE_PLATE_BLACKLIST 65 //license plate blacklist
// CLS_DNVRAlmLinkPage dialog

IMPLEMENT_DYNAMIC(CLS_DNVRAlmLinkPage, CDialog)

CLS_DNVRAlmLinkPage::CLS_DNVRAlmLinkPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DNVRAlmLinkPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_pclsChanCheck = NULL;
	m_iAlarmTypeCMD = 0;
}

CLS_DNVRAlmLinkPage::~CLS_DNVRAlmLinkPage()
{
	if (NULL != m_pclsChanCheck)
	{
		delete m_pclsChanCheck;
		m_pclsChanCheck = NULL; 
	}
}

void CLS_DNVRAlmLinkPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_cboType);
	DDX_Control(pDX, IDC_COMBO_LINKTYPE, m_cboLinkType);
	DDX_Control(pDX, IDC_COMBO_INPORT, m_cboInPort);
	DDX_Control(pDX, IDC_BUTTON_CHANNELENABLE, m_btnChannelEnable);
	DDX_Control(pDX, IDC_CHECK_DISPLAYENABLE, m_chkDisplayEnable);
	DDX_Control(pDX, IDC_CHECK_SOUNDENABLE, m_chkSoundEnable);
	DDX_Control(pDX, IDC_BUTTON_AVIDEO, m_btnAVideo);
	DDX_Control(pDX, IDC_COMBO_LINKCHANNELNO, m_cboLinkChannelNo);
	DDX_Control(pDX, IDC_COMBO_LINKPTZTYPE, m_cboLinkPTZType);
	DDX_Control(pDX, IDC_EDIT_LINKACTNO, m_edtLinkACTNo);
	DDX_Control(pDX, IDC_BUTTON_LINK, m_btnLink);
	DDX_Control(pDX, IDC_COMBO_SINGLEPIC_CHANNEL, m_cboSinglePic);
	DDX_Control(pDX, IDC_CBO_ALARM_LINK_COMMON_ENABLE, m_cboCommonEnable);
	DDX_Control(pDX, IDC_COMBO_WHITE, m_cboWhiteEnable);
	DDX_Control(pDX, IDC_COMBO_SOUNDNO, m_iSerialNo);
	DDX_Control(pDX, IDC_EDIT_LINKRESIDENCETIME, m_edtLinkResidenceTime);
	DDX_Control(pDX, IDC_COMBO_ADD_PARA1, m_cboAddPara1);
	DDX_Control(pDX, IDC_COMBO_ADD_PARA2, m_cboAddPara2);
}


BEGIN_MESSAGE_MAP(CLS_DNVRAlmLinkPage, CLS_BasePage)
	ON_CBN_SELCHANGE(IDC_COMBO_TYPE, &CLS_DNVRAlmLinkPage::OnCbnSelchangeComboType)
	ON_CBN_SELCHANGE(IDC_COMBO_LINKTYPE, &CLS_DNVRAlmLinkPage::OnCbnSelchangeComboLinktype)
	ON_CBN_SELCHANGE(IDC_COMBO_INPORT, &CLS_DNVRAlmLinkPage::OnCbnSelchangeComboInport)
	ON_BN_CLICKED(IDC_BUTTON_CHANNELENABLE, &CLS_DNVRAlmLinkPage::OnBnClickedButtonChannelenable)
	ON_BN_CLICKED(IDC_BUTTON_AVIDEO, &CLS_DNVRAlmLinkPage::OnBnClickedButtonAvideo)
	ON_CBN_SELCHANGE(IDC_COMBO_LINKCHANNELNO, &CLS_DNVRAlmLinkPage::OnCbnSelchangeComboLinkchannelno)
	ON_BN_CLICKED(IDC_BUTTON_LINK, &CLS_DNVRAlmLinkPage::OnBnClickedButtonLink)
	ON_BN_CLICKED(IDC_BUTTON_SINGLEPIC_CHANNEL, &CLS_DNVRAlmLinkPage::OnBnClickedButtonSinglepicChannel)
	ON_BN_CLICKED(IDC_BTN_ALARM_LINK_COMMON_ENABLE_SET, &CLS_DNVRAlmLinkPage::OnBnClickedBtnAlarmLinkCommonEnableSet)
	ON_CBN_SELCHANGE(IDC_COMBO_ADD_PARA1, &CLS_DNVRAlmLinkPage::OnCbnSelchangeComboAddPara1)
	ON_CBN_SELCHANGE(IDC_COMBO_ADD_PARA2, &CLS_DNVRAlmLinkPage::OnCbnSelchangeComboAddPara2)
	ON_BN_CLICKED(IDC_BUTTON_WHITE_SET, &CLS_DNVRAlmLinkPage::OnBnClickedButtonWhiteSet)
END_MESSAGE_MAP()


// CLS_DNVRAlmLinkPage message handlers
BOOL CLS_DNVRAlmLinkPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();
	UI_UpdateChanCheck();
	m_cboType.SetCurSel(0);
	m_cboLinkType.SetCurSel(0);
	m_cboLinkPTZType.SetCurSel(0);
	m_cboAddPara1.SetCurSel(0);
	m_cboAddPara2.SetCurSel(0);

	int iOffset = 0;
	RECT rcPC = {0};
	RECT rcTemp = {0};
	GetDlgItem(IDC_STATIC_LINKSET)->GetWindowRect(&rcPC);
	ScreenToClient(&rcPC);
	GetDlgItem(IDC_COMBO_LINKCHANNELNO)->ShowWindow(SW_HIDE);

	GetDlgItem(IDC_STATIC_LINKSETAVIDEO)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	iOffset = rcPC.top - rcTemp.top;
	OffsetWindow(IDC_STATIC_LINKSETAVIDEO,0,iOffset);
	OffsetWindow(IDC_CHECK_DISPLAYENABLE,0,iOffset);
	OffsetWindow(IDC_CHECK_SOUNDENABLE,0,iOffset);
	OffsetWindow(IDC_BUTTON_AVIDEO,0,iOffset);
	OffsetWindow(IDC_STATIC_SOUNDNUM, 0, iOffset);
	OffsetWindow(IDC_COMBO_SOUNDNO, 0, iOffset);
	
	GetDlgItem(IDC_STATIC_LINK_SET_PTZ)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	iOffset = rcPC.top - rcTemp.top;
	OffsetWindow(IDC_STATIC_LINK_SET_PTZ,0,iOffset);
	OffsetWindow(IDC_STATIC_LINKCHANNELNO,0,iOffset);
	OffsetWindow(IDC_STATIC_LINKPTZTYPE,0,iOffset);
	OffsetWindow(IDC_STATIC_LINKACTNO,0,iOffset);
	OffsetWindow(IDC_COMBO_LINKCHANNELNO,0,iOffset);
	OffsetWindow(IDC_COMBO_LINKPTZTYPE,0,iOffset);
	OffsetWindow(IDC_EDIT_LINKACTNO,0,iOffset);
	OffsetWindow(IDC_BUTTON_LINK,0,iOffset);
	OffsetWindow(IDC_EDIT_LINKRESIDENCETIME,0,iOffset);
	OffsetWindow(IDC_STATIC_LINK_RESIDENCETIME,0,iOffset);

	GetDlgItem(IDC_STATIC_LINK_SET_PTZ2)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	iOffset = rcPC.top - rcTemp.top;
	OffsetWindow(IDC_STATIC_LINK_SET_PTZ2,0,iOffset);
	OffsetWindow(IDC_STATIC_SINGLEPIC_CHANNEL,0,iOffset);
	OffsetWindow(IDC_COMBO_SINGLEPIC_CHANNEL,0,iOffset);
	OffsetWindow(IDC_BUTTON_SINGLEPIC_CHANNEL,0,iOffset);

	GetDlgItem(IDC_GBO_ALARM_LINK_COMMON_ENABLE)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	iOffset = rcPC.top - rcTemp.top;
	OffsetWindow(IDC_GBO_ALARM_LINK_COMMON_ENABLE,0,iOffset);
	OffsetWindow(IDC_CBO_ALARM_LINK_COMMON_ENABLE,0,iOffset);
	OffsetWindow(IDC_BTN_ALARM_LINK_COMMON_ENABLE_SET,0,iOffset);
	
	GetDlgItem(IDC_STATIC_WHITE)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	iOffset = rcPC.top - rcTemp.top;
	OffsetWindow(IDC_STATIC_WHITE,0,iOffset);
	OffsetWindow(IDC_COMBO_WHITE,0,iOffset);
	OffsetWindow(IDC_BUTTON_WHITE_SET,0,iOffset);
	return TRUE;
}

void CLS_DNVRAlmLinkPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}

	UI_UpdateDialog();
	UI_ShowLinkType();
	UI_UpdateType();
	UI_UpdateLinkSetAVideo();
	UI_UpdateLinkSinPic();
	OnCbnSelchangeComboInport();
}

void CLS_DNVRAlmLinkPage::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_DNVRAlmLinkPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_TYPE,IDS_CONFIG_DNVR_ALMLINK_TYPE);
	SetDlgItemTextEx(IDC_STATIC_LINKTYPE,IDS_CONFIG_DNVR_ALMLINK_LINKTYPE);
	SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMLINK_INPORT);
	//SetDlgItemTextEx(IDC_STATIC_LINKSET,IDS_CONFIG_DNVR_ALMLINK_LINKSET);
	SetDlgItemTextEx(IDC_STATIC_CHANNELENABLE,IDS_CONFIG_DNVR_ALMLINK_CHANNELENABLE);
	SetDlgItemTextEx(IDC_BUTTON_CHANNELENABLE,IDS_SET);
	//SetDlgItemTextEx(IDC_STATIC_LINKSETAVIDEO,IDS_CONFIG_DNVR_ALMLINK_LINKSETAVIDEO);
	SetDlgItemTextEx(IDC_CHECK_DISPLAYENABLE,IDS_CONFIG_DNVR_ALMLINK_DISPLAYENABLE);
	SetDlgItemTextEx(IDC_CHECK_SOUNDENABLE,IDS_CONFIG_DNVR_ALMLINK_SOUNDENABLE);
	SetDlgItemTextEx(IDC_BUTTON_AVIDEO,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_LINKCHANNELNO,IDS_CONFIG_DNVR_ALMLINK_LINKCHANNELNO);
	SetDlgItemTextEx(IDC_STATIC_LINKPTZTYPE,IDS_CONFIG_DNVR_ALMLINK_LINKPTZTYPE);
	SetDlgItemTextEx(IDC_STATIC_LINKACTNO,IDS_CONFIG_DNVR_ALMLINK_LINKACTNO);
	SetDlgItemTextEx(IDC_BUTTON_LINK,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_SINGLEPIC_CHANNEL,IDS_ALARM_LINK_SINGLEPIC_CHANNEL);
	SetDlgItemTextEx(IDC_BUTTON_SINGLEPIC_CHANNEL,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_SOUNDNUM, IDS_SOUNDNO);

	SetDlgItemTextEx(IDC_CHECK_DOUBLE_LIGHT_DAYNIGHT,IDS_DAYNIGHT_MODEL);
	SetDlgItemTextEx(IDC_CHECK_DOUBLE_LIGHT_COLOR,IDS_COLOR_MODEL);
	SetDlgItemTextEx(IDC_BUTTON_DOUBLE_LIGHT,IDS_SET);

	if (1 == m_iChangeInPort)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_INPORT);
	}
	else if (0 == m_iChangeInPort)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_CHANNELNO);
	}
	if (1 == m_iChangeOutPort)
	{
		SetDlgItemTextEx(IDC_STATIC_CHANNELENABLE,IDS_CONFIG_DNVR_ALMLINK_OUTPORT);
	}
	else if (0 == m_iChangeOutPort)
	{
		SetDlgItemTextEx(IDC_STATIC_CHANNELENABLE,IDS_CONFIG_DNVR_ALMLINK_CHANNELENABLE);
	}

	//The input port alarm is linked to the output port alarm. The port alarm is linked to the port output in the IO linkage, and the function is repeated.
	//InsertString(m_cboType,0,IDS_CONFIG_DNVR_PORTALARM);
	InsertString(m_cboType,0,IDS_CONFIG_DNVR_VIDEOLOST);
	InsertString(m_cboType,1,IDS_CONFIG_DNVR_VIDEOMOTION);
	InsertString(m_cboType,2,IDS_CONFIG_DNVR_VIDEOCOVER);
	InsertString(m_cboType,3,IDS_CONFIG_DNVR_AUDIOLOST);
	InsertString(m_cboType,4,IDS_CONFIG_DVR_TEMPERATURE);
	InsertString(m_cboType,5,IDS_CONFIG_DVR_RAINFALL);
	InsertString(m_cboType,6,IDS_CONFIG_DVR_ALERTWATER);
	InsertString(m_cboType,7,GetTextByLan(_T("政法智能分析"), _T("ZF VCA")));
	InsertString(m_cboType,DANGER_AREA_ALARM,GetTextByLan(_T("危险区域报警"), _T("Danger area alarm")));
    InsertString(m_cboType,BLACK_BODY_ERR_ALARM,GetTextByLan(_T("黑体异常报警"), _T("Black body err alarm")));
    InsertString(m_cboType,PAGE_ALARM_TYPE_VCA,GetTextByLan(_T("智能分析"), _T("Intelligent analysis")));

    InsertString(m_cboType,PAGE_ALARM_TYPE_MOTHERBOARD_DISASSEMBLE,GetTextByLan(_T("主板拆卸报警"), _T("Intelligent analysis")));
    InsertString(m_cboType,PAGE_ALARM_TYPE_OUT_CARDREADER_DISASSEMBLE,GetTextByLan(_T("外接读卡器拆卸报警"), _T("Intelligent analysis")));
    InsertString(m_cboType,PAGE_ALARM_TYPE_BUTTON,GetTextByLan(_T("按钮报警"), _T("Intelligent analysis")));
	InsertString(m_cboType,PORT_ALARM, GetTextByLan(_T("端口报警"), _T("Port alarm")));
	InsertString(m_cboType, UIIDX_VEHICLE_IDENTIFY, GetTextByLan(_T("车辆识别报警"), _T("Vehicle identify alarm")));
	InsertString(m_cboType, UIIDX_WATER_ELECTRONIC_OVERRANGE, GetTextByLan(_T("水利电子围栏超范围报警"), _T("Water electronic overrange")));

	InsertString(m_cboLinkType, LINK_RECORD, IDS_CONFIG_DNVR_LINKRECORD);
	InsertString(m_cboLinkType, LINK_SNAP, IDS_CONFIG_DNVR_LINKSNAP);
	InsertString(m_cboLinkType, LINK_AUDIO_VIDEO, IDS_CONFIG_DNVR_LINKAUDIO);
	InsertString(m_cboLinkType, LINK_PTZ, IDS_CONFIG_DNVR_LINKPTZ);
	InsertString(m_cboLinkType, LINK_SINGLEPIC, IDS_CONFIG_DNVR_LINKSINGLEPIC);
	InsertString(m_cboLinkType, LINK_MAIL, IDS_CONFIG_CMOS_MAILENABLE);
	InsertString(m_cboLinkType, LINK_HTTP, IDS_HTTP);
	InsertString(m_cboLinkType, LINK_LASER, IDS_LASER);
	InsertString(m_cboLinkType, LINK_WHITE, IDS_WHITE);
    InsertString(m_cboLinkType, LINK_OUTPORT, IDS_CONFIG_DNVR_LINKOUTPORT);
    InsertString(m_cboLinkType, UIIDX_LINK_ALARM_LAMP, GetTextByLan(_T("联动警灯"), _T("Linkage alarm light")));
	InsertString(m_cboLinkType, UIIDX_LINK_UPLOAD_ALARM, GetTextByLan(_T("上传报警中心"), _T("Upload alarm center")));
	InsertString(m_cboLinkType, UIIDX_LINK_LINKAPP, GetTextByLan(_T("联动APP"), _T("Link APP")));
	InsertString(m_cboLinkType, UIIDX_LINK_INTELLIGENT_ALGO, GetTextByLan(_T("联动智能算法"), _T("Linkage intelligent algorithm")));
	
	InsertString(m_cboLinkPTZType,0,IDS_CONFIG_DNVR_NOLINK);
	InsertString(m_cboLinkPTZType,1,IDS_CONFIG_DNVR_PRESET);
	InsertString(m_cboLinkPTZType,2,IDS_CONFIG_DNVR_TRACK);
	InsertString(m_cboLinkPTZType,3,IDS_CONFIG_DNVR_CRUISEPATH);
	InsertString(m_cboLinkPTZType,4,IDS_CONFIG_VCA_INTELLIGENT_SCENE);
	SetDlgItemText(IDC_STATIC_LINK_RESIDENCETIME, GetTextByLan(_T("滞留时间"), _T("Residence Time")));
	int iTempIndex = 0;
	iTempIndex = m_cboCommonEnable.GetCurSel();
	iTempIndex = (iTempIndex < 0) ? 0 : iTempIndex; 
	m_cboCommonEnable.ResetContent();
	m_cboCommonEnable.SetItemData(m_cboCommonEnable.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_DISABLE)), 0);
	m_cboCommonEnable.SetItemData(m_cboCommonEnable.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_ENABLE)), 1);
	iTempIndex = (iTempIndex < m_cboCommonEnable.GetCount()) ? iTempIndex : 0;
	m_cboCommonEnable.SetCurSel(iTempIndex);

	int iWhiteIndex = 0;
	iWhiteIndex = m_cboWhiteEnable.GetCurSel();
	iWhiteIndex = (iWhiteIndex < 0) ? 0 : iWhiteIndex; 
	m_cboWhiteEnable.ResetContent();
	m_cboWhiteEnable.SetItemData(m_cboWhiteEnable.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_DISABLE)), 0);
	m_cboWhiteEnable.SetItemData(m_cboWhiteEnable.AddString(GetTextEx(IDS_NVR_ALARMLINK_FLASH)), 1);
	m_cboWhiteEnable.SetItemData(m_cboWhiteEnable.AddString(GetTextEx(IDS_NVR_ALARMLINK_LIGHTON)), 2);
	iWhiteIndex = (iWhiteIndex < m_cboWhiteEnable.GetCount()) ? iWhiteIndex : 0;
	m_cboWhiteEnable.SetCurSel(iWhiteIndex);

	SetDlgItemText(IDC_STATIC_ADD_PARA1,GetTextByLan(_T("附加参数1"), _T("AdditionalPara1")));
	SetDlgItemText(IDC_STATIC_ADD_PARA2,GetTextByLan(_T("附加参数2"), _T("AdditionalPara2")));
	InsertString(m_cboAddPara1, 0, GetTextByLan(_T("车牌比对报警"), _T("License Plate Compare Alarm")));
	InsertString(m_cboAddPara1, 1, GetTextByLan(_T("陌生车牌报警"), _T("Strange License Plate Alarm")));
	FuncAbilityLevel tAbilityLevel = {0};
	tAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	tAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	tAbilityLevel.iSubFuncType = 178;
	int iRetBytes = -1;
	int iChannelNo = m_iChannelNo < 0 ? 0 : m_iChannelNo;
	int iPlateLibCount = 10;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, iChannelNo, &tAbilityLevel, sizeof(tAbilityLevel), &iRetBytes);
	if (RET_SUCCESS == iRet) {
		iPlateLibCount = atoi(tAbilityLevel.cParam);
	}
	if (iPlateLibCount <= 0) {
		iPlateLibCount = 10;
	}
	for (int i = 0; i < iPlateLibCount; ++i)
	{
		CString strLibId;
		strLibId.Format("%d", i + 1);
		InsertString(m_cboAddPara2, i, strLibId);
	}
}

BOOL CLS_DNVRAlmLinkPage::UI_UpdateType()
{
	if (m_iLogonID < 0)
		return FALSE;

	int iAlarmInPortNum = -1;
	int iAlarmOutPortNum = -1;
	int iChannelNum = -1;
	int iRet;

	if (PORT_ALARM == m_cboType.GetCurSel())
	{
		iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iAlarmInPortNum, &iAlarmOutPortNum);
		if (0 == iRet)
		{
			m_iChangeInPort = 1;
			if (m_cboInPort.GetCount() != iAlarmInPortNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iAlarmInPortNum; i++)
				{
					m_cboInPort.InsertString(i, IntToStr(i).c_str());
				}
			}
			InsertString(m_cboInPort,iAlarmInPortNum,IDS_CONFIG_DNVR_ALL);
			m_cboInPort.SetCurSel(0);
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
	}
	else
	{
		iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
		if (0 == iRet)
		{
			m_iChangeInPort = 0;
			if (m_cboInPort.GetCount() != iChannelNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iChannelNum && i < 32; i++)
				{
					m_cboInPort.InsertString(i, IntToStr(i).c_str());
				}
				InsertString(m_cboInPort,iChannelNum,IDS_CONFIG_DNVR_ALL);
				m_cboInPort.SetCurSel(0);
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
	}

	if (LINK_OUTPORT == m_cboLinkType.GetCurSel())
	{
		m_iChangeOutPort = 1;
	}
	else
	{
		m_iChangeOutPort = 0;
	}
	return TRUE;
}

void CLS_DNVRAlmLinkPage::OnCbnSelchangeComboType()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	int iAlarmType = m_cboType.GetCurSel();

	int iAlarmInPortNum = -1;
	int iAlarmOutPortNum = -1;
	int iChannelNum = -1;
	int iRet;

	if (PORT_ALARM == iAlarmType)
	{
		iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iAlarmInPortNum, &iAlarmOutPortNum);
		if (0 == iRet)
		{
			m_iChangeInPort = 1;
			if (m_cboInPort.GetCount() != iAlarmInPortNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iAlarmInPortNum; i++)
				{
					CString strInPort;
					strInPort.Format("%d",i);
					m_cboInPort.AddString(strInPort);
				}
			}
			InsertString(m_cboInPort,iAlarmInPortNum,IDS_CONFIG_DNVR_ALL);
			m_cboInPort.SetCurSel(0);
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmPortNum (%d,%d,%d)",m_iLogonID,iAlarmInPortNum,iAlarmOutPortNum);
		}
	}
	else
	{
		iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
		if (0 == iRet)
		{
			m_iChangeInPort = 0;
			if (m_cboInPort.GetCount() != iChannelNum)
			{
				m_cboInPort.ResetContent();
				for (int i=0; i<iChannelNum && i < 32; i++)
				{
					CString strInPort;
					strInPort.Format("%d",i);
					m_cboInPort.AddString(strInPort);
				}
				InsertString(m_cboInPort,iAlarmInPortNum,IDS_CONFIG_DNVR_ALL);
				m_cboInPort.SetCurSel(0);
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_GetChannelNum (%d,%d)",m_iLogonID,iChannelNum);
		}
	}
	if (1 == m_iChangeInPort)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_INPORT);
	}
	else if (0 == m_iChangeInPort)
	{
		SetDlgItemTextEx(IDC_STATIC_INPORT,IDS_CONFIG_DNVR_ALMSCH_CHANNELNO);
	}

	OnCbnSelchangeComboInport();
}

void CLS_DNVRAlmLinkPage::OnCbnSelchangeComboLinktype()
{
	UI_ShowLinkType();
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	if (LINK_OUTPORT == m_cboLinkType.GetCurSel())
	{
		m_iChangeOutPort = 1;
	}
	else
	{
		m_iChangeOutPort = 0;
	}
	if (LINK_AUDIO_VIDEO == m_cboLinkType.GetCurSel())
	{
		UpdateVoice();
	}
	if (1 == m_iChangeOutPort)
	{
		SetDlgItemTextEx(IDC_STATIC_CHANNELENABLE,IDS_CONFIG_DNVR_ALMLINK_OUTPORT);
	}
	else if (0 == m_iChangeOutPort)
	{
		SetDlgItemTextEx(IDC_STATIC_CHANNELENABLE,IDS_CONFIG_DNVR_ALMLINK_CHANNELENABLE);
	}
	OnCbnSelchangeComboInport();
}

void CLS_DNVRAlmLinkPage::OnCbnSelchangeComboInport()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iChannelNum = 0; //Total number of channels
	NetClient_GetChannelNum(m_iLogonID, &iChannelNum);

	int iChanEnable[LEN_16] = {0};
	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);

	int iRet = -1;
	int iAlarmType = m_cboType.GetCurSel(); //Alarm type
	int iLinkType = m_cboLinkType.GetCurSel(); //Linkage type

	m_cboAddPara1.EnableWindow(FALSE);
	m_cboAddPara2.EnableWindow(FALSE);

	switch(iAlarmType)
	{
	case PORT_ALARM:
		m_iAlarmTypeCMD = ALARM_TYPE_PORT_ALARM;
		break;
	case VIDEO_LOST:
		m_iAlarmTypeCMD = ALARM_TYPE_VIDEO_LOST;
		break;
	case VIDEO_MOTION:
		m_iAlarmTypeCMD = ALARM_TYPE_MOTION_DETECTION;
		break;
	case VIDEO_COVER:
		m_iAlarmTypeCMD = ALARM_TYPE_VIDEO_COVER;
		break;
	case AUDIO_LOST:
		m_iAlarmTypeCMD = ALARM_TYPE_AUDIOLOST;
		break;
	case TEMPERATURE_ALARM:
		m_iAlarmTypeCMD = ALARM_TYPE_TEMPERATURE;	
		break;
	case RAINFALL_ALARM:
		m_iAlarmTypeCMD = ALARM_TYPE_RAINFALL;			
		break;
	case ALERTWATER_ALARM:
		m_iAlarmTypeCMD = ALARM_TYPE_ALERT_WATER_LEVEL;
		break;
	case ZF_VCA:
		m_iAlarmTypeCMD = ALARM_TYPE_ZF_VCA;
		break;
	case DANGER_AREA_ALARM:
		m_iAlarmTypeCMD = ALARM_TYPE_DANGEROUS_AREA;
		break;
	case BLACK_BODY_ERR_ALARM:
		m_iAlarmTypeCMD = ALARM_TYPE_BLACK_BODY_DETECT;
		break;
    case PAGE_ALARM_TYPE_VCA:
        m_iAlarmTypeCMD = ALARM_TYPE_VCA;
        break;
    case PAGE_ALARM_TYPE_MOTHERBOARD_DISASSEMBLE:
        m_iAlarmTypeCMD = ALARM_TYPE_MOTHERBOARD_DISASSEMBLE;
        break;
    case PAGE_ALARM_TYPE_OUT_CARDREADER_DISASSEMBLE:
        m_iAlarmTypeCMD = ALARM_TYPE_OUT_CARDREADER_DISASSEMBLE;
        break;
    case PAGE_ALARM_TYPE_BUTTON:
        m_iAlarmTypeCMD = ALARM_TYPE_BUTTON;
        break;
	case UIIDX_VEHICLE_IDENTIFY:
		m_iAlarmTypeCMD = ALARM_TYPE_VEHICLE_IDENTIFICATION;
		m_cboAddPara1.EnableWindow(TRUE);
		m_cboAddPara2.EnableWindow(TRUE);
		tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = m_cboAddPara1.GetCurSel() + 1;
		tAlarmLinkPara.tAlarmParam.iReserved = m_cboAddPara2.GetCurSel() + 1;
		break;
	case UIIDX_WATER_ELECTRONIC_OVERRANGE:
		m_iAlarmTypeCMD = ALARM_TYPE_WATER_ELECTRONIC_OVERRANGE;
		break;
	default:
		break;
	}

	int iInportNum=0, iOutportNum=0; 
	switch (iLinkType)
	{
	case LINK_RECORD: //Link video
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKRECORD;
		break;
	case LINK_SNAP: //Linked snapshot
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKSNAP;
		break;
	case LINK_OUTPORT: //Link output port
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKOUTPORT;
		iRet = NetClient_GetAlarmPortNum(m_iLogonID, &iInportNum, &iOutportNum);
		iChannelNum = iOutportNum;
		break;
	case LINK_HTTP:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKHTTP;
		iChannelNum = MAX_LINK_HTTP_INDEX;
		break;
	case LINK_AUDIO_VIDEO: //Link audio and video
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKSOUND;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		m_chkSoundEnable.SetCheck(tAlarmLinkPara.tLinkParam.uLinkParam.tLinkSoundParam.iEnable ? BST_CHECKED : BST_UNCHECKED);
		m_iSerialNo.SetCurSel(tAlarmLinkPara.tLinkParam.uLinkParam.tLinkSoundParam.iSerialNo);

		//tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKDISPLAY;
		//iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		//m_chkDisplayEnable.SetCheck(tAlarmLinkPara.tLinkParam.uLinkParam.tLinkDisplayParam.iEnable ? BST_CHECKED : BST_UNCHECKED);
		return;
	case LINK_PTZ:	
		{//Link PTZ
		tAlarmLinkPara.tLinkParam.iLinkType = ALARM_LINKTYPE_PTZ;
		tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.iPtzNo = 0;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, 0, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		m_cboLinkPTZType.SetCurSel(tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usType);
		int iLinkPTZ_Type = tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usType;
		if(4 == iLinkPTZ_Type)
		{
			SetDlgItemInt(IDC_EDIT_LINKRESIDENCETIME,tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.iResidenceTime);
		}
		SetDlgItemInt(IDC_EDIT_LINKACTNO, tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usTypeNO);
		return;
		}
	case LINK_SINGLEPIC://link single screen
		tAlarmLinkPara.tLinkParam.iLinkType = ALARM_LINKTYPE_SINGLEPIC;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		for (int i=0; i<LEN_16; i++)
		{
			iChanEnable[i] = tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[i];
		}
		for (int i=0; i<iChannelNum; i++)
		{
			if (iChanEnable[i/LEN_32]>>(i%32)&1)
			{
				m_cboSinglePic.SetCurSel(i+1);
				break;
			}
		}
		return;
	case LINK_MAIL:	
	case LINK_LASER:
	case LINK_WHITE:
		tAlarmLinkPara.tLinkParam.iLinkType = iLinkType + 2;
		NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		m_cboCommonEnable.SetCurSel(GetCboSel(&m_cboCommonEnable, tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[0]));
		return;
	case UIIDX_LINK_ALARM_LAMP:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_ALARM_LAMP;
		NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		m_cboCommonEnable.SetCurSel(GetCboSel(&m_cboCommonEnable, tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[0]));
		return;
	case UIIDX_LINK_UPLOAD_ALARM:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_UPLOAD_ALARM;
		NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		m_cboCommonEnable.SetCurSel(GetCboSel(&m_cboCommonEnable, tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[0]));
		return;
	case UIIDX_LINK_LINKAPP:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKAPP;
		NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		m_cboCommonEnable.SetCurSel(GetCboSel(&m_cboCommonEnable, tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[0]));
		return;
	case UIIDX_LINK_INTELLIGENT_ALGO:
        tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_INTELLIGENT_ALGO;
        NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
        m_cboCommonEnable.SetCurSel(GetCboSel(&m_cboCommonEnable, tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[0]));
        return;
	default:
		break;
	}

	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
	for (int i=0; i < (sizeof(iChanEnable)/sizeof(int)); i++)
	{
		iChanEnable[i] = tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[i]; 
	}

	m_pclsChanCheck->InitData(iChannelNum, iChanEnable);
}

// This function is the real setup function
void CLS_DNVRAlmLinkPage::OnBnClickedButtonChannelenable()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);

	switch (m_cboLinkType.GetCurSel())
	{
	case LINK_RECORD:	//Linked recording
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKRECORD;
		break;
	case LINK_SNAP:		//Linked capture
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKSNAP;
		break;
	case LINK_OUTPORT:	// linkage output port
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKOUTPORT;
		break;
	case LINK_HTTP:	
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKHTTP;
		break;	
	case LINK_LASER:
		tAlarmLinkPara.tLinkParam.iLinkType = m_cboLinkType.GetCurSel() + 2;
		break;	
	default:
		break;
	}

	m_pclsChanCheck->GetChanValue(tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet);

    //if it is intelligent analysis
    if (ALARM_TYPE_VCA == m_iAlarmTypeCMD) {
		tAlarmLinkPara.tAlarmParam.iReserved = VCA_TYPE_PLATE_BLACKLIST; //License plate blacklist
    } else if (ALARM_TYPE_VEHICLE_IDENTIFICATION == m_iAlarmTypeCMD) {
		tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = m_cboAddPara1.GetCurSel() + 1;
		if (2 == tAlarmLinkPara.tAlarmParam.iAlarmTypeParam) {
			tAlarmLinkPara.tAlarmParam.iReserved = 0;
		} else {
			tAlarmLinkPara.tAlarmParam.iReserved = m_cboAddPara2.GetCurSel() + 1;
		}	
    }

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
}

BOOL CLS_DNVRAlmLinkPage::UI_UpdateLinkSetAVideo()
{
	if (m_iLogonID < 0)
		return FALSE;

	int iChannelNum = 0;
	m_cboLinkChannelNo.ResetContent();
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	for (int i=0; i<iChannelNum; i++)
	{
		CString strChannelNum;
		strChannelNum.Format("%d",i);
		m_cboLinkChannelNo.AddString(strChannelNum);
	}
	m_cboLinkChannelNo.SetCurSel(0);
	return TRUE;
}

void CLS_DNVRAlmLinkPage::OnBnClickedButtonAvideo()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	//Link sound prompt
	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);
	if (ALARM_TYPE_VEHICLE_IDENTIFICATION == m_iAlarmTypeCMD) {
		tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = m_cboAddPara1.GetCurSel() + 1;
		if (2 == tAlarmLinkPara.tAlarmParam.iAlarmTypeParam) {
			tAlarmLinkPara.tAlarmParam.iReserved = 0;
		} else {
			tAlarmLinkPara.tAlarmParam.iReserved = m_cboAddPara2.GetCurSel() + 1;
		}	
	}

	tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKSOUND;
	tAlarmLinkPara.tLinkParam.uLinkParam.tLinkSoundParam.iEnable = (BST_CHECKED == m_chkSoundEnable.GetCheck()) ? 1 : 0;
	tAlarmLinkPara.tLinkParam.uLinkParam.tLinkSoundParam.iSerialNo = m_iSerialNo.GetCurSel();
	int iRetSound = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);
	/////There may be a problem with the continuous setting. The SDK has not changed when the last protocol on the device has not been replied. If you set it again immediately, it may be wrong.
	Sleep(1500);
	//Link video display
	/*tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKDISPLAY;
	tAlarmLinkPara.tLinkParam.uLinkParam.tLinkDisplayParam.iEnable = (BST_CHECKED == m_chkDisplayEnable.GetCheck()) ? 1 : 0;*/
	//int iRetDisPlay = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);
	if (0 == iRetSound)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
}

void CLS_DNVRAlmLinkPage::OnCbnSelchangeComboLinkchannelno()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	int iRet = 0;
	int iPortNo = 0;
	int iChannelNo = 0;
	if (m_cboInPort.GetCount() - 1 == m_cboInPort.GetCurSel())
	{
		return;
	}
	else
	{
		iPortNo = GetDlgItemInt(IDC_COMBO_INPORT);
		iChannelNo = GetDlgItemInt(IDC_COMBO_INPORT);
	}
	int iLinkChannelNo = m_cboLinkChannelNo.GetCurSel();
	int iPTZLinkType = -1;
	
	if (m_cboType.GetCurSel() == AUDIO_LOST)
	{
		TAlarmLinkParam_V3 tAlarmLinkPara = {0};
		tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
		tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);


		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKPTZ;
		tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.iPtzNo = iLinkChannelNo;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iChannelNo, ALARM_TYPE_AUDIOLOST, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);

		iPTZLinkType = tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usType;
		m_cboLinkPTZType.SetCurSel(iPTZLinkType);	
		switch (iPTZLinkType)
		{
		case PTZ_TYPE_PRESET:
		case PTZ_TYPE_TRACK:
		case PTZ_TYPE_CRUISEPATH:
			SetDlgItemInt(IDC_EDIT_LINKACTNO,tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usTypeNO);
			break;
		default:
			SetDlgItemInt(IDC_EDIT_LINKACTNO, 0);
			break;
		}
		return;
	}
	else
	{
		int iAlarmType = m_cboType.GetCurSel();
		TAlarmLinkParam_V3 tAlarmLinkPara = {0};
		tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
		tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);

		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKPTZ;
		tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.iPtzNo = iLinkChannelNo;
		iRet = NetClient_GetAlarmConfig(m_iLogonID, iPortNo, iAlarmType, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);

		if (0 == iRet)
		{
			m_cboLinkPTZType.SetCurSel(iPTZLinkType);
			int iActNum = tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usTypeNO;
			SetDlgItemInt(IDC_EDIT_LINKACTNO, iActNum);
		}
	}

}

void CLS_DNVRAlmLinkPage::OnBnClickedBtnAlarmLinkCommonEnableSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);
	if (ALARM_TYPE_VEHICLE_IDENTIFICATION == m_iAlarmTypeCMD) {
		tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = m_cboAddPara1.GetCurSel() + 1;
		if (2 == tAlarmLinkPara.tAlarmParam.iAlarmTypeParam) {
			tAlarmLinkPara.tAlarmParam.iReserved = 0;
		} else {
			tAlarmLinkPara.tAlarmParam.iReserved = m_cboAddPara2.GetCurSel() + 1;
		}	
	}

	switch (m_cboLinkType.GetCurSel())
	{
	case LINK_MAIL:	
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKEMAIL;
		break;
	case LINK_LASER:	
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LASER;
		break;
	case LINK_WHITE:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_FLASHING_WHITE;
		break;
	case UIIDX_LINK_ALARM_LAMP:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_ALARM_LAMP;
		break;
	case UIIDX_LINK_UPLOAD_ALARM:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_UPLOAD_ALARM;
		break;
	case UIIDX_LINK_LINKAPP:
		tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKAPP;
		break;
	case UIIDX_LINK_INTELLIGENT_ALGO:
        tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_INTELLIGENT_ALGO;
        break;
	default:
		{
			AddLog(LOG_TYPE_MSG,"","Err LinkType(%d)",m_cboLinkType.GetCurSel());
			return;
		}
		break;
	}

	tAlarmLinkPara.tLinkParam.uLinkParam.tLinkDisplayParam.iEnable = (int)m_cboCommonEnable.GetItemData(m_cboCommonEnable.GetCurSel());
	
	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
}

void CLS_DNVRAlmLinkPage::OnBnClickedButtonLink()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	if (m_cboType.GetCurSel() == VIDEO_MOTION)
	{
		AddLog(LOG_TYPE_MSG,"","Video motion can not link");
		return;
	}

	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);
	if (ALARM_TYPE_VEHICLE_IDENTIFICATION == m_iAlarmTypeCMD) {
		tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = m_cboAddPara1.GetCurSel() + 1;
		if (2 == tAlarmLinkPara.tAlarmParam.iAlarmTypeParam) {
			tAlarmLinkPara.tAlarmParam.iReserved = 0;
		} else {
			tAlarmLinkPara.tAlarmParam.iReserved = m_cboAddPara2.GetCurSel() + 1;
		}	
	}

	tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKPTZ;

	int iLinkChannelNo = m_cboLinkChannelNo.GetCurSel(); //Linkage channel

	//There may be a problem with the assignment before here, you need to follow it later
	tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usType = m_cboLinkPTZType.GetCurSel(); //Link PTZ type

	//Determine whether the number of PTZ of the linkage channel exceeds 64
	int iPtzCount = 0; //The channel that has been linked
	bool bSelfLink = false; //Whether the currently set channel has been linked
	int iChannelNum = 0;
	NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	for (int i=0; i<iChannelNum; i++)
	{
		tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.iPtzNo = i;
		NetClient_GetAlarmConfig(m_iLogonID, i, m_iAlarmTypeCMD, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);
		if (LINKPTZ_TYPE_NOLINK != tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam.usType)
		{
			if (iLinkChannelNo == i)
			{
				bSelfLink = true;
			}
			iPtzCount++;
		}
	}

	//Add manual input data valid range judgment
	int iActNum = GetDlgItemInt(IDC_EDIT_LINKACTNO);
	int iCount = ActionInit();
	int iIndexMin = iCount > 0 ? 0 : -1;
	int iIndexMax = iCount;
	iActNum = iActNum < iIndexMin ? iIndexMin : iActNum;
	iActNum = iActNum > iIndexMax ? iIndexMax : iActNum;

	TLinkPtzParam_V3 &tLinkPtzParam = tAlarmLinkPara.tLinkParam.uLinkParam.tLinkPtzParam;
	tLinkPtzParam.iPtzNo = iLinkChannelNo;
	if(0 == m_cboLinkPTZType.GetCurSel())
	{
		tLinkPtzParam.usType = LINKPTZ_TYPE_NOLINK;
		tLinkPtzParam.usTypeNO = 0;
	}
	else if(1 == m_cboLinkPTZType.GetCurSel())
	{
		tLinkPtzParam.usType = LINKPTZ_TYPE_PRESET;
		tLinkPtzParam.usTypeNO = iActNum;
	}
	else if(2 == m_cboLinkPTZType.GetCurSel())
	{
		tLinkPtzParam.usType = LINKPTZ_TYPE_TRACK;
		tLinkPtzParam.usTypeNO = iActNum;
	}
	else if(3 == m_cboLinkPTZType.GetCurSel())
	{
		tLinkPtzParam.usType = LINKPTZ_TYPE_PATH;
		tLinkPtzParam.usTypeNO = iActNum;
	}
	else if(4 == m_cboLinkPTZType.GetCurSel())
	{
		tLinkPtzParam.usType = LINKPTZ_TYPE_SCENE;

		int iResidenceTime;
		CString cstrResiTime;
		GetDlgItem(IDC_EDIT_LINKRESIDENCETIME)->GetWindowText(cstrResiTime);
		iResidenceTime = _ttoi(cstrResiTime);
		tLinkPtzParam.iResidenceTime = iResidenceTime;
		tLinkPtzParam.usTypeNO = iActNum;
	}

	if (iPtzCount >= LEN_64 && !bSelfLink 
		&& LINKPTZ_TYPE_NOLINK != tLinkPtzParam.usType)
	{	
		//Alarm linkage PTZ can link up to 64 channels
		return;
	}

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
}

void CLS_DNVRAlmLinkPage::UI_ShowLinkCP(int _iCmdShow)
{
	GetDlgItem(IDC_STATIC_LINKSET)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_CHANNELENABLE)->ShowWindow(FALSE);
	GetDlgItem(IDC_BUTTON_CHANNELENABLE)->ShowWindow(_iCmdShow);
	m_pclsChanCheck->ShowWindow(_iCmdShow?SW_SHOW:SW_HIDE);
}

void CLS_DNVRAlmLinkPage::UI_ShowLinkAV(int _iCmdShow)
{
	GetDlgItem(IDC_STATIC_LINKSETAVIDEO)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_CHECK_DISPLAYENABLE)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_CHECK_SOUNDENABLE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_BUTTON_AVIDEO)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_SOUNDNUM)->ShowWindow(_iCmdShow);
	m_iSerialNo.ShowWindow(_iCmdShow);
}

int CLS_DNVRAlmLinkPage::ActionInit()
{
	int iMaxPtzNo = 0;
	switch(m_cboLinkPTZType.GetCurSel())
	{
	case PTZ_TYPE_PRESET:
		{
			//Preset IDS_CONFIG_DNVR_PRESET
			iMaxPtzNo = 255;
			break;
		}
	case PTZ_TYPE_TRACK:
		{
			//Track IDS_CONFIG_DNVR_TRACK
			iMaxPtzNo = 1;
			break;
		}
	case PTZ_TYPE_CRUISEPATH:
		{
			//Path IDS_CONFIG_DNVR_CRUISEPATH
			iMaxPtzNo = 8;
			break;
		}
	case PTZ_TYPE_SCENE:
		{
			iMaxPtzNo = 16;
			break;
		}
	default :
		break;
	}
	return iMaxPtzNo; 
}

void CLS_DNVRAlmLinkPage::UI_ShowLinkPTZ(int _iCmdShow)
{
	GetDlgItem(IDC_STATIC_LINK_SET_PTZ)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_LINKCHANNELNO)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_LINKPTZTYPE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_LINKACTNO)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_COMBO_LINKCHANNELNO)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_COMBO_LINKPTZTYPE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_EDIT_LINKACTNO)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_BUTTON_LINK)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_EDIT_LINKRESIDENCETIME)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_LINK_RESIDENCETIME)->ShowWindow(_iCmdShow);
}

void CLS_DNVRAlmLinkPage::UI_ShowLinkType()
{
	UI_ShowLinkCP(SW_HIDE);
	UI_ShowLinkAV(SW_HIDE);
	UI_ShowLinkPTZ(SW_HIDE);
	UI_ShowLinkSinPic(SW_HIDE);
	UI_ShowLinkCommonEnable(SW_HIDE);
	UI_ShowWhite(SW_HIDE);
	switch(m_cboLinkType.GetCurSel())
	{
	case LINK_RECORD:
	case LINK_SNAP:
	case LINK_OUTPORT:
	case LINK_HTTP:	
		UI_ShowLinkCP(SW_SHOW);
		break;
	case LINK_AUDIO_VIDEO:
		UI_ShowLinkAV(SW_SHOW);
		break;
	case LINK_PTZ:
		UI_ShowLinkPTZ(SW_SHOW);
		break;
	case LINK_SINGLEPIC:
		UI_ShowLinkSinPic(SW_SHOW);
		break;
	case LINK_WHITE:
		UI_ShowWhite(SW_SHOW);
		break;
	case LINK_MAIL:	
	case LINK_LASER:
    case UIIDX_LINK_ALARM_LAMP:
	case UIIDX_LINK_UPLOAD_ALARM:
	case UIIDX_LINK_LINKAPP:
	case UIIDX_LINK_INTELLIGENT_ALGO:
		UI_ShowLinkCommonEnable(SW_SHOW);
		break;
	default: 
		break;

	}
}

void CLS_DNVRAlmLinkPage::UI_ShowWhite(int _iCmdShow)
{
	GetDlgItem(IDC_STATIC_WHITE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_COMBO_WHITE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_BUTTON_WHITE_SET)->ShowWindow(_iCmdShow);
	int iWhiteIndex = 0;
	iWhiteIndex = m_cboWhiteEnable.GetCurSel();
	iWhiteIndex = (iWhiteIndex < 0) ? 0 : iWhiteIndex; 
	m_cboWhiteEnable.ResetContent();
	m_cboWhiteEnable.SetItemData(m_cboWhiteEnable.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_DISABLE)), 0);
	m_cboWhiteEnable.SetItemData(m_cboWhiteEnable.AddString(GetTextEx(IDS_NVR_ALARMLINK_FLASH)), 1);
	m_cboWhiteEnable.SetItemData(m_cboWhiteEnable.AddString(GetTextEx(IDS_NVR_ALARMLINK_LIGHTON)), 2);
	iWhiteIndex = (iWhiteIndex < m_cboWhiteEnable.GetCount()) ? iWhiteIndex : 0;
	m_cboWhiteEnable.SetCurSel(iWhiteIndex);
}

void CLS_DNVRAlmLinkPage::OffsetWindow(int iID,int dx,int dy)
{
	RECT rcTemp = {0};
	GetDlgItem(iID)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	OffsetRect(&rcTemp,dx,dy);
	GetDlgItem(iID)->MoveWindow(&rcTemp);
}

void CLS_DNVRAlmLinkPage::UI_ShowLinkSinPic( int _iCmdShow )
{
	GetDlgItem(IDC_STATIC_LINK_SET_PTZ2)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_SINGLEPIC_CHANNEL)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_COMBO_SINGLEPIC_CHANNEL)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_BUTTON_SINGLEPIC_CHANNEL)->ShowWindow(_iCmdShow);
}

void CLS_DNVRAlmLinkPage::UI_ShowLinkCommonEnable(int _iCmdShow)
{
	GetDlgItem(IDC_GBO_ALARM_LINK_COMMON_ENABLE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_CBO_ALARM_LINK_COMMON_ENABLE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_BTN_ALARM_LINK_COMMON_ENABLE_SET)->ShowWindow(_iCmdShow);
	int iTempIndex = 0;
	iTempIndex = m_cboCommonEnable.GetCurSel();
	iTempIndex = (iTempIndex < 0) ? 0 : iTempIndex; 
	m_cboCommonEnable.ResetContent();
	m_cboCommonEnable.SetItemData(m_cboCommonEnable.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_DISABLE)), 0);
	m_cboCommonEnable.SetItemData(m_cboCommonEnable.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_ENABLE)), 1);
	iTempIndex = (iTempIndex < m_cboCommonEnable.GetCount()) ? iTempIndex : 0;
	m_cboCommonEnable.SetCurSel(iTempIndex);
}
//add by zhy 2013.04.03
BOOL CLS_DNVRAlmLinkPage::UI_UpdateLinkSinPic()
{
	if (m_iLogonID < 0)
		return FALSE;

	int iChannelNum = 0;
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);

	m_cboSinglePic.ResetContent();
	m_cboSinglePic.AddString("--");
	for (int i=1; i<=iChannelNum; i++)
	{
		CString strNo;
		strNo.Format("Channel%d", i);
		m_cboSinglePic.AddString(strNo);
	}
	m_cboSinglePic.SetCurSel(0);

	return TRUE;
}

void CLS_DNVRAlmLinkPage::OnBnClickedButtonSinglepicChannel()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);
	if (ALARM_TYPE_VEHICLE_IDENTIFICATION == m_iAlarmTypeCMD) {
		tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = m_cboAddPara1.GetCurSel() + 1;
		if (2 == tAlarmLinkPara.tAlarmParam.iAlarmTypeParam) {
			tAlarmLinkPara.tAlarmParam.iReserved = 0;
		} else {
			tAlarmLinkPara.tAlarmParam.iReserved = m_cboAddPara2.GetCurSel() + 1;
		}	
	}

	tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_LINKSINGLEPIC;
	
	int iChanNo = m_cboSinglePic.GetCurSel()-1;
	tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[iChanNo/LEN_32] |= 1<<iChanNo%LEN_32;

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
}
//add by zhy end

void CLS_DNVRAlmLinkPage::UI_UpdateChanCheck()
{
	if (m_pclsChanCheck == NULL)
	{
		m_pclsChanCheck = new CLS_ChanCheck();
		m_pclsChanCheck->Create(IDD_DLG_CFG_CHANNEL_CHECK, this);
	}

	if (m_pclsChanCheck == NULL)
	{
		return;
	}

	RECT rc = {0};
	GetDlgItem(IDC_STATIC_LINKSET)->GetWindowRect(&rc);
	ScreenToClient(&rc);
	rc.top += 15;
	rc.bottom -= 10;
	rc.left += 5;
	rc.right -= 5;
	m_pclsChanCheck->MoveWindow(&rc);
	m_pclsChanCheck->ShowWindow(SW_HIDE);
}

void CLS_DNVRAlmLinkPage::UpdateVoice()
{
	m_iSerialNo.ResetContent();
	AudioSampleFileCount tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	int iReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_AUDIO_SAMPLE_FILE_COUNT, m_iChannelNO, &tInfo, sizeof(tInfo), &iReturn);
	if(RET_SUCCESS == iRet)
	{
		if(tInfo.iTotalCount > 100 || tInfo.iTotalCount <= 0)
		{
			tInfo.iTotalCount = 3;
		}

		CString cstrTemp;
		int iItem = 0;
		for(int i = 0; i < tInfo.iSampleCount; i++)
		{
			cstrTemp.Format("%d", i+1);
			m_iSerialNo.InsertString(iItem++,GetText(IDS_LINK_GUARD_SOUND) + cstrTemp);
		}

		for(int j = 0; j < tInfo.iCustomCount; j++)
		{
			cstrTemp.Format("%d", j+1);
			m_iSerialNo.InsertString(iItem++,GetText(IDS_HD_MODE_CUSTOMIZED) + cstrTemp);
		}
		m_iSerialNo.SetCurSel(0);
	}
}



void CLS_DNVRAlmLinkPage::OnCbnSelchangeComboAddPara1()
{
	OnCbnSelchangeComboInport();
}

void CLS_DNVRAlmLinkPage::OnCbnSelchangeComboAddPara2()
{
	OnCbnSelchangeComboInport();
}

void CLS_DNVRAlmLinkPage::OnBnClickedButtonWhiteSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);
	if (ALARM_TYPE_VEHICLE_IDENTIFICATION == m_iAlarmTypeCMD) {
		tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = m_cboAddPara1.GetCurSel() + 1;
		if (2 == tAlarmLinkPara.tAlarmParam.iAlarmTypeParam) {
			tAlarmLinkPara.tAlarmParam.iReserved = 0;
		} else {
			tAlarmLinkPara.tAlarmParam.iReserved = m_cboAddPara2.GetCurSel() + 1;
		}	
	}
	tAlarmLinkPara.tLinkParam.iLinkType = ALARMLINKTYPE_FLASHING_WHITE;
	tAlarmLinkPara.tLinkParam.uLinkParam.tLinkDisplayParam.iEnable = (int)m_cboWhiteEnable.GetItemData(m_cboWhiteEnable.GetCurSel());

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, m_iAlarmTypeCMD, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,%d)",m_iLogonID,m_iChannelNo,m_iAlarmTypeCMD);
	}
}

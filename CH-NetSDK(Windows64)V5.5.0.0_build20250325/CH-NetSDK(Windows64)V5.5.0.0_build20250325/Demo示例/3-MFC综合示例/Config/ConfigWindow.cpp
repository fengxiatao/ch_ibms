// ConfigWindow.cpp : implementation file
//

#include "stdafx.h"
#include "ConfigWindow.h"
#include "VideoParamPage.h"
#include "AudioParamPage.h"
#include "VideoEncryptPage.h"
#include "AlarmPage.h"
#include "OSDPage.h"
#include "VideoPage.h"
#include "SOPage.h"
#include "DZPage.h"
#include "LanguagePage.h"
#include "IPPage.h"
#include "IPAdvPage.h"
#include "UserPage.h"
#include "SipPage.h"
#include "CmosPage.h"
#include "DDNSPage.h"
#include "UDPPage.h"
#include "NTPPage.h"
#include "PUPage.h"
#include "ITSRoadwayPage.h"

#include "ITSOSDPage.h"
#include "ITSOtherPage.h"
#include "LANIPV4Page.h"
#include "LANIPV6Page.h"
#include "LANWorkModePage.h"
#include "FTPPage.h"
#include "8D1Page.h"
#include "HardDiskManage.h"
#include "StoragePage.h"
#include "StorageStrategyPage.h"
#include "StorageSmartPage.h"
#include "StorageHotBackup.h"

#include "DeviceDiskInfo.h"
#include "HDPage.h"
#include "DNVRAlmSchPage.h"
#include "DNVRAlmLinkPage.h"
#include "DNVRDigitPage.h"
#include "DNVRVideoCombinePage.h"
#include "DNVRExceptionPage.h"
#include "CLS_XVR.h"
#include "3GAdvPage.h"
#include "3GDVRPage.h"
#include "3GNormalPage.h"
#include "4GNormalPage.h"
#include "NVSLogPage.h"
#include "DVRLogPage.h"
#include "VideoParamSchedulePage.h"
#include "PTZPage.h"
#include "WifiPage.h"
#include "AdvPage.h"
#include "ATMPage.h"
#include "VCATargetPage.h"
#include "VCAAdvanceParam.h"
#include "VCASmartSearch.h"
#include "VCAEventPage.h"
#include "AdvParam\VcaQueryFile.h"
#include "Vca\TargetPicManage.h"
#include "ComPage.h"
#include "IOPORTPAGE.h"
#include "AdvVersionPage.h"
#include "AdvSystemInfoPage.h"
#include "AdvUpgrade.h"
#include "AdvChannelParamPage.h"
#include "VCAAlarmInfoPage.h"
#include "VCAAlarmSchedulePage.h"
#include "VCAAlarmLinkPage.h"
#include "AuthorityChannelPage.h"
#include "AuthorityLocalPage.h"
#include "AuthorityRemotePage.h"
#include "HolidayPlanPage.h"
#include "DomeInfrared.h"
#include "VehiclePage.h"
#include "VideoInterested.h"
#include "AppendOSDPage.h"
#include "DomeState.h"
#include "DomeSchedule.h"
#include "DomeMenu.h"
#include "DonghuanSet.h"
#include "LinkSet.h"
#include "SerialManagePage.h"
#include "OSDManagePage.h"
#include "PowerEnvParamPage.h"
#include "DefenceTemplatePage.h"
#include "DNVRCreateFree.h"
#include "ReportSet.h"
#include "VCAEvnetsAdvParam.h"
#include "VCAEvnetsSmartTrack.h"
#include "Events/VCAframDomeCurise.h"
#include "AdvLocalSet.h"
#include "../Common/Ini.h"
#include "DomeBasic1.h"
#include "DomeBasic2.h"
#include "DomeRun1.h"
#include "DomeRun2.h"
#include "DomeHDParam1.h"
#include "DomeHDParam2.h"
#include "DomeHDParam3.h"
#include "HDSchedule.h"

#include "ItsPictureCommon1.h"
#include "ItsPictureCommon2.h"
#include "ItsPictureCommon3.h"
#include "ItsSignalCheck.h"
#include "ItsSignalOther.h"
#include "ITSRoadWayAdvanced2.h"
#include "ItsRoadwayCommon2.h"
#include "ItsRoadwayCommon3.h"
#include "ItsRoadwayCommon4.h"
#include "ItsRecognitionParam.h"
#include "ItsSystemParam1.h"
#include "ItsSystemParam2.h"
#include "ItsSystemParam3.h"
#include "ItsCompoPic.h"
#include "ItsTrafficStatistics.h"

#include "ItsIllegalType.h"
#include "ItsStateQuery.h"
#include "ItsFocusAid.h"
#include "ItsRecognitionParam2.h"
#include "ITSRoadWayAdvanced1.h"
#include "LinkHttp.h"
#include "PortMapping.h"
#include "ITSRoadWayAdvanced3.h"
#include "ExtendedParam.h"
#include "ItsAdvanceConf2.h"
#include "AutoTestPage.h"
#include "IOData.h"
#include "CLS_DLG_FUNC_COLORTOGRAY.h"
#include "CLS_DLG_CFG_FUNC_VideoTranceCoding.h"
#include "CLS_DlgFuncSmarta.h"
#include "CLS_DlgFuncDdns.h"
#include "CLS_DlgFuncNetwork.h"
#include "CLS_DlgFuncDecoder.h"
#include "CLS_DlgFuncStatisPeopleNum.h"
#include "CLS_DlgFuncRoi.h"
#include "CLS_DlgFuncLaw.h"
#include "CLS_DlgFuncDome.h"
#include "DlgCommonEnable.h"
#include "ITSOSDPageEx.h"
#include "OSDBackGroundPage.h"
#include "ITSAlarmLink.h"
#include "ITSRoadwayCaptureCfg.h"
#include "ITSPlatformConfig.h"
#include "ITSDevManage.h"
#include "ITSLaneManage.h"
#include "ITSBayonetData.h"
#include "DNVRTextPlan.h"
#include "QosPage.h"
#include "BAWLicencePlate.h"
#include "StorageDiskManagePage.h"
#include "DNVRChannelInfo.h"
#include "FECHeatMap.h"
#include "CLS_UPnpPage.h"
#include "VideoEncodeSlicePage.h"
#include "StoragePathSet.h"
#include "StorageANR.h"
#include "DlgAlarmHumTem.h"
#include "RtmpPage.h"
#include "IrrigationNotify.h"
#include "CLS_IrrigationParaConf.h"
#include "CLS_IrrigationGeneralConfig.h"
#include "CLS_DormancyPage.h"
#include "Periphreal.h"
#include "SNMPPage.h"
#include "CLS_FtpUplaod.h"

#include ".\UniqueAlert\CLS_DlgUniqueAlertEnable.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertCustom.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertPlan.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertAlarmLink.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertEventTripwire.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertEventPerimeter.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertFuncAssemble.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertAlarmMessage.h"
#include ".\UniqueAlert\CLS_DlgUniqueAlertCfgTarget.h"
#include "CLS_NetManage.h"
#include "CLS_TribleVCA.h"
#include "CLS_VCARESALLOCTION.h"
#include ".\CLS_GaugeCalib.h"
#include "CLS_DlgCfgGPSLocation.h"
#include "DlgCfgFluxStatistic.h"
#include "VcaTops.h"
#include "CLS_Shdb.h"
#include "CLS_IS_SUPPORT_FUNC.h"
#include "CLS_XNVR_UPDATA.h"
#include "LS_VcaMaskArea.h"
#include "ITSParkCarNum.h"
#include "CLS_InternationPro.h"
#include "CLS_DlgCalibrateXY.h"
#include "CLS_DlgThermography.h"
#include "HttpXmlCfg.h"
#include "CLS_ItsRadarInfo.h"
#include "CLS_ItsRadarLedInfo.h"
#include "CLS_VCAScanArea.h"
#include "CLS__Calibrate.h"
#include "CLS_3DMaskArea.h"
#include "CLS_WhiteLightControl.h"
#include "CLS_CalibrateMode.h"
#include "../CLS_DlgFDDFunction.h"
#include "CLS_SceneFocusArea.h"
#include "CLS_SceneHDScheduleParam.h"
#include "DlgWirelessSilent.h"
#include "CLS_DlgWaterInfo.h"
#include "CLS_DlgVerticalline.h"
#include "CLS_DlgCPCArea.h"
#include "DNVRAlmLinkIPCPage.h"
#include "ItsTrafficViolationPara.h"
#include "DlgHttpPicture.h"
#include "CLS_GATPage.h"
#include "CLS_AirLinesPlan.h"

#include "vca\CLS_VcaDetectArea.h"
#include "CLS_GBT28181SET.h"
#include "WiegandPage.h"

#include "CLS_VCA_Reference.h"
#include "CLS_RTPServerInfo.h"
#include "CLS_CertificateAndAuthFile.h"
#include "DlgChannelAlarmInfo.h"
#include "DlgSendCommonData.h"
#include "CLS_GaugeConfig.h"
#include "CLS_WaterSpeedRule.h"
#include "CLS_AnemometerConfig.h"
#include "CLS_VertiLineQuery.h"
#include "DlgXmlThreeDimTrans.h"
#include "CLS_OnVif_VcaAlarm.h"
#include "DlgElevatorMonitor.h"
#include "DlgElevatorState.h"
#include "CLS_AdvProtDetectPage.h"
#include "DlgVcaRadarLinkScene.h"
#include "DlgVcaRefBoundaryInfo.h"
#include "CLS_FallingObjectPage.h"
#include "DlgElevatorAllState.h"
#include "DlgVcaAlarmCountStat.h"
#include "DlgXmlMixAudioConfig.h"
#include "CLS_ItsPlateInfo.h"
#include "CLS_PoliceCloth.h"
#include "CLS_PrisonerCloth.h"
#include "CLS_QueueStand.h"
#include "DlgIrriDataUploadParam.h"
#include "CLS_ElectronicFenceConfig.h"
#include "CLS_PlateLibManage.h"
#include "CLS_StrategyCondition.h"
#include "UniqueAlert\CLS_DlgUniqueAlertEventClimbWall.h"
#include "CLS_FixedDiskStorage.h"
#include "TargetDetect.h"
#include "CLS_IrrReceiverMsgConfig.h"


// CLS_ConfigWindow dialog
#define WAIT_PAUSE_TIME 300
IMPLEMENT_DYNAMIC(CLS_ConfigWindow, CDialog)

CLS_ConfigWindow::CLS_ConfigWindow(CWnd* pParent /*=NULL*/)
	: CLS_BaseWindow(CLS_ConfigWindow::IDD, pParent)
{
	for (int i = 0; i < CONFIG_MAX; ++i)
	{
		m_pPage[i] = NULL;
	}
	m_pCurrent = NULL;
	m_pVideo = NULL;
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iStreamNo = 0;
	m_bLocked = FALSE;
	m_iCurrentPage = -1;
}

CLS_ConfigWindow::~CLS_ConfigWindow()
{
	for (int i = 0; i < CONFIG_MAX; ++i)
	{
		if (NULL == m_pPage[i])
		{
			continue;
		}

		if (IsWindow(m_pPage[i]->GetSafeHwnd()))
		{
			m_pPage[i]->DestroyWindow();
		}

		delete m_pPage[i];
		m_pPage[i] = NULL;
	}
}

void CLS_ConfigWindow::DoDataExchange(CDataExchange* pDX)
{
	CLS_BaseWindow::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_TREE_CONFIG, m_tvConfig);
}

BEGIN_MESSAGE_MAP(CLS_ConfigWindow, CLS_BaseWindow)
	ON_WM_DESTROY()
	ON_WM_SHOWWINDOW()
	ON_NOTIFY(TVN_SELCHANGED, IDC_TREE_CONFIG, &CLS_ConfigWindow::OnTvnSelchangedTreeConfig)
	ON_NOTIFY(NM_DBLCLK, IDC_TREE_CONFIG, &CLS_ConfigWindow::OnNMDblclkTreeConfig)
	ON_STN_CLICKED(IDC_STATIC_CENTER_BG, &CLS_ConfigWindow::OnStnClickedStaticCenterBg)
END_MESSAGE_MAP()


// CLS_ConfigWindow message handlers

BOOL CLS_ConfigWindow::OnInitDialog()
{
	CLS_BaseWindow::OnInitDialog();

	UI_UpdateDialog();

	return TRUE;  
}

int CLS_ConfigWindow::ShowBasePage( int _iIndex )
{
	if (_iIndex < CONFIG_MIN || _iIndex >= CONFIG_MAX)
	{
		return -1;
	}
	if(_iIndex == CONFIG_VCA_EVENTS && NULL != m_pPage[_iIndex])
	{
		delete m_pPage[_iIndex];
		m_pPage[_iIndex] = NULL;
	}
	m_iCurrentPage = _iIndex;
	if (NULL == m_pPage[_iIndex])
	{
		switch(_iIndex)
		{
		case CONFIG_VIDEO_PARAM:
			{
				m_pPage[_iIndex] = new CLS_VideoParamPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VIDEO_PARAM,this);
			}
			break;
		case CONFIG_VIDEO_PARAM_SCHEDULE:
			{
				m_pPage[_iIndex] = new CLS_VideoParamSchedulePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VIDEO_PARAM_SCHEDULE,this);
			}
			break;
		case CONFIG_AUDIO_PARAM:
			{
				m_pPage[_iIndex] = new CLS_AudioParamPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_AUDIO_PARAM,this);
			}
			break;
		case CONFIG_VIDEO_ENCRYPT:
			{
				m_pPage[_iIndex] = new CLS_VideoEncryptPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VIDEO_ENCRYPT,this);
			}
			break;
		case CONFIG_VIDEO_ENCODE_SLICE:
			{
				m_pPage[_iIndex] = new CLS_VideoEncodeSlicePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VIDEO_ENCODE_SLICE,this);
			}
			break;
		case CONFIG_ALARM:
			{
				m_pPage[_iIndex] = new CLS_AlarmPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALARM,this);
			}
			break;
		case CONFIG_LANGUAGE:
			{
				m_pPage[_iIndex] = new CLS_LanguagePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LANGUAGE,this);
			}
			break;
        case CONFIG_WIEGAND:
            {
                m_pPage[_iIndex] = new CLS_Wiegand(this);
                m_pPage[_iIndex]->Create(IDD_DLG_CFG_WIEGAND,this);
            }
            break;
		case CONFIG_OSD:
			{
				m_pPage[_iIndex] = new CLS_OSDPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_OSD,this);
			}
			break;
		case CONFIG_IP:
			{
				m_pPage[_iIndex] = new CLS_IPPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_IP,this);
			}
			break;
		case CONFIG_IPADV:
			{
				m_pPage[_iIndex] = new CLS_IPAdvPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_IPADV, this);
			}
			break;
		case CONFIG_USER:
			{
				m_pPage[_iIndex] = new CLS_UserPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_USER,this);
			}
			break;
		case CONFIG_AUTHORITY_LOCAL:
			{
				m_pPage[_iIndex] = new CLS_AuthorityLocalPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_AUTHORITY_LOCAL,this);
			}
			break;
		case CONFIG_AUTHORITY_REMOTE:
			{
				m_pPage[_iIndex] = new CLS_AuthorityRemotePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_AUTHORITY_REMOTE,this);
			}
			break;
		case CONFIG_AUTHORITY_CHANNEL:
			{
				m_pPage[_iIndex] = new CLS_AuthorityChannelPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_AUTHORITY_CHANNEL,this);
			}
			break;
		case CONFIG_SO:
			{
				m_pPage[_iIndex] = new CLS_SOPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_SO,this);
			}
			break;
        case CONFIG_GAT:
            {
                m_pPage[_iIndex] = new CLS_GATPage(this);
                m_pPage[_iIndex]->Create(IDD_DLG_CFG_GAT,this);
            }
            break;
		case CONFIG_DZ:
			{
				m_pPage[_iIndex] = new CLS_DZPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DZ,this);
			}
			break;
		case CONFIG_SIP:
			{
				m_pPage[_iIndex] = new CLS_SipPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_SIP,this);
			}
			break;
		case CONFIG_CMOS:
			{
				m_pPage[_iIndex] = new CLS_CmosPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_CMOS,this);
			}
			break;
		case CONFIG_DDNS:
			{
				m_pPage[_iIndex] = new CLS_DDNSPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DDNS,this);
			}
			break;
		case CONFIG_UDP:
			{
				m_pPage[_iIndex] = new CLS_UDPPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_UDP,this);
			}
			break;
		case CONFIG_NTP:
			{
				m_pPage[_iIndex] = new CLS_NTPPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_NTP,this);
			}
			break;
		case CONFIG_OPTION_PU:
			{
				m_pPage[_iIndex] = new CLS_PUPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_OPTION_PU,this);
			}
			break;
		case CONFIG_ITS_ROADWAY_COMMON1:
			{
				m_pPage[_iIndex] = new CLS_ITSRoadwayPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_COMMON1,this);
			}
			break;
		case CONFIG_ITS_PICTURE_COMMON1:
			{
				m_pPage[_iIndex] = new Cls_ItsPictureCommon1(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_PICTURE_COMMON1,this);
			}
			break;
		case CONFIG_ITS_OSD:
			{
				m_pPage[_iIndex] = new CLS_ITSOSDPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_OSD,this);
			}
			break;
		case CONFIG_ITS_OSD_EX:
			{
				m_pPage[_iIndex] = new CLS_ITSOSDPageEx(this);
 				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_OSD_EX,this);
			}
			break;
		case CONFIG_ITS_ALARM_LINK:
			{
				m_pPage[_iIndex] = new CLS_ITSAlarmLinkPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ALARM_LINK,this);
			}
			break;
		case CONFIG_ITS_ROADWAY_ADVANCED2:
			{
				m_pPage[_iIndex] = new CLS_ITSRoadWayAdvanced2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_ADVANCED2,this);
			}
			break;
		case CONFIG_ITS_OTHER:
			{
				m_pPage[_iIndex] = new CLS_ITSOtherPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_OTHER,this);
			}
			break;
		case CONFIG_LAN_IPV4:
			{
				m_pPage[_iIndex] = new CLS_LANIPV4Page(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LAN_IPV4,this);
			}
			break;
		case CONFIG_LAN_IPV6:
			{
				m_pPage[_iIndex] = new CLS_LANIPV6Page(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LAN_IPV6,this);
			}
			break;	
		case CONFIG_LAN_WOKEMODE:
			{
				m_pPage[_iIndex] = new CLS_LANWorkModePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LAN_WORKMODE,this);
			}
			break;	
		case CONFIG_FTP:
			{
				m_pPage[_iIndex] = new CLS_FTPPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FTP,this);
			}
			break;	
		case CONFIG_8D1:
			{
				m_pPage[_iIndex] = new CLS_8D1Page(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_8D1,this);
			}
			break;
		case CONFIG_STORAGE:
			{
				m_pPage[_iIndex] = new CStoragePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE,this);
			}
			break;
		case CONFIG_HARDDISK_INFO:
			{
				m_pPage[_iIndex] = new CLS_HardDiskManage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_HARDDISK_MANAGE,this);
			}
			break;
		case CONFIG_STORAGE_SET:
			{
				m_pPage[_iIndex] = new CLS_StoragePathSetPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_SET,this);
			}
			break;
		case CONFIG_STORAGE_DISKINFO:
			{
				m_pPage[_iIndex] = new CDeviceDiskInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_DISKINFO,this);
			}
			break;
		case CONFIG_HOLIDAY_PLAN:
			{
				m_pPage[_iIndex] = new CLS_HolidayPlanPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_HOLIDAY_PLAN,this);
			}
			break;
		case CONFIG_STORAGE_STRATEGY:
			{
				m_pPage[_iIndex] = new CLS_StorageStrategyPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_STRATEGY,this);
			}
			break;
		case CONFIG_STORAGE_SMART:
			{
				m_pPage[_iIndex] = new CLS_StorageSmartPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_SMART,this);
			}
			break;
		case CONFIG_STORAGE_DISKMANAGE:
			{
				m_pPage[_iIndex] = new CLS_StorageDiskManagePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_DISKMANAGE,this);
			}
			break;
		case CONFIG_STORAGE_ANR:
			{
				m_pPage[_iIndex] = new CLS_StorageANR(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_ANR,this);
			}
			break;
		case CONFIG_STORAGE_HOTBACKUP:
			{
				m_pPage[_iIndex] = new CLS_StorageHotBackup(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_STORAGE_HOTBACKUP,this);
			}
			break;
		case CONFIG_ITS_SIGNAL_CHECK:
			{
				m_pPage[_iIndex] = new CLS_ItsSignalCheck(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_SIGNAL_CHECK,this);
			}
			break;
		case CONFIG_ITS_ROADWAY_COMMON3:
			{
				m_pPage[_iIndex] = new CLS_ItsRoadwayCommon3(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_COMMON3,this);
			}
			break;
		case CONFIG_ITS_PICTURE_COMMON3:
			{
				m_pPage[_iIndex] = new CLS_ItsPictureCommon3(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_PICTURE_COMMON3,this);
			}
			break;
		case CONFIG_ITS_RECOPARAM:
			{
				m_pPage[_iIndex] = new CLS_ItsRecognitionParam(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_RECOPARAM,this);
			}
			break;
		case CONFIG_ITS_RECOPARAM2:
			{
				m_pPage[_iIndex] = new CLS_ItsRecognitionParam2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_RECOPARAM_2,this);
			}
			break;
		case CONFIG_ITS_SYSTEMPARAM1:
			{
				m_pPage[_iIndex] = new CLS_ItsSystemParam1(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_SYSTEMPARAM1,this);
			}
			break;
		case CONFIG_ITS_SYSTEMPARAM2:
			{
				m_pPage[_iIndex] = new CLS_ItsSystemParam2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_SYSTEMPARAM2,this);
			}
			break;
		case CONFIG_ITS_SYSTEMPARAM3:
			{
				m_pPage[_iIndex] = new CLS_ItsSystemParam3(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_SYSTEMPARAM3,this);
			}
			break;
		case CONFIG_HD:
			{
				m_pPage[_iIndex] = new CLS_HDPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_HD,this);
			}
			break;
		case CONFIG_ADV:
			{
				m_pPage[_iIndex] = new CLS_AdvPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ADV,this);
			}
			break;
		case CONFIG_NETTEST:
			{
				m_pPage[_iIndex] = new CLS_NetManage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_NET_TEST,this);
			}
			break;
		case CONFIG_ATM:
			{
				m_pPage[_iIndex] = new CLS_ATMPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ATM,this);
			}
			break;
		case CONFIG_DNVR_ALMSCH:
			{
				m_pPage[_iIndex] = new CLS_DNVRAlmSchPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_ALMSCH,this);
			}
			break;
		case CONFIG_DNVR_ALMLINK:
			{
				m_pPage[_iIndex] = new CLS_DNVRAlmLinkPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_ALMLINK,this);
			}
			break;
		case CONFIG_DNVR_ALMLINKIPC:
			{
				m_pPage[_iIndex] = new CLS_DNVRAlmLinkIPCPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_ALMLINKIPC,this);
			}
			break;
		case CONFIG_ALARM_HUM_TEM:
			{
				m_pPage[_iIndex] = new CLS_DlgAlarmHumTem(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALARM_HUM_TEM,this);
			}
			break;
		case CONFIG_DNVR_DIGIT:
			{
				m_pPage[_iIndex] = new CLS_DNVRDigitPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_DIGIT,this);
			}
			break;
		case CONFIG_DNVR_VIDEOCOMBINE:
			{
				m_pPage[_iIndex] = new CLS_DNVRVideoCombinePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_VIDEOCOMBINE,this);
			}
			break;
		case CONFIG_DNVR_BAWSLICENCEPLATE:
			{
				m_pPage[_iIndex] = new CLS_BAWLicencePlate(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_BANDW_LICENCEPLATE,this);
			}
			break;
		case CONFIG_DNVR_EXCEPTION:
			{
				m_pPage[_iIndex] = new CLS_DNVRExceptionPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_EXCEPTION,this);
			}
			break;
		case CONFIG_DNVR_CREATEFREEV:
			{
				m_pPage[_iIndex] = new CLS_DNVRCreateFree(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_CREATEFREE,this);
			}
			break;
		case CONFIG_DNVR_CHANNEL_INFO:
			{
				m_pPage[_iIndex] = new CLS_DNVRChannelInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_CHANNEL_INFO,this);
			}
			break;
		case CONFIG_DNVR_TEXTPLAN:
			{
				m_pPage[_iIndex] = new CLS_DNVRTextPlan(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DNVR_TEXTPLAN,this);
			}
			break;
		case CONFIG_3G_NORMAL:
			{
				m_pPage[_iIndex] = new CLS_3GNormalPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_3G_NORMAL,this);
			}
			break;
		case CONFIG_3G_DVR:
			{
				m_pPage[_iIndex] = new CLS_3GDVRPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_3G_DVR,this);
			}
			break;
		case CONFIG_3G_ADV:
			{
				m_pPage[_iIndex] = new CLS_3GAdvPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_3G_ADV,this);
			}
			break;
		case CONFIG_4G_NORMAL:
			{
				m_pPage[_iIndex] = new CLS_4GNormal(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_4G_NORMAL,this);
			}
			break;
		case CONFIG_VEHICLE:
			{
				m_pPage[_iIndex] = new CLS_VehiclePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VEHICLE,this);
			}
			break;
		case CONFIG_LOG_NVS:
			{
				m_pPage[_iIndex] = new CLS_NVSLogPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LOG_NVS,this);
			}
			break;
		case CONFIG_LOG_DVR:
			{
				m_pPage[_iIndex] = new CLS_DVRLogPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LOG_DVR,this);
				break;
			}
		case CONFIG_PTZ:
			{
				m_pPage[_iIndex] = new CLS_PTZPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_PTZ,this);
			}
			break;
		case CONFIG_WIFI:
			{
				m_pPage[_iIndex] = new CWifiPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_WIFI,this);
			}
			break;	
		case CONFIG_VCA_RESOURCE:
			{
				m_pPage[_iIndex] = new CLS_VCARESALLOCTION(this);
				m_pPage[_iIndex]->Create(IDD_DLG_VCA_RESOURCE_ALLOCTION,this);
			}
			break;
		case CONFIG_VCA_TARGET:
			{
				m_pPage[_iIndex] = new CLS_VCATargetPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_TARGET,this);
			}
			break;
		case CONFIG_VCA_ADVANCE:
			{
				m_pPage[_iIndex] = new CLS_VCAAdvanceParam(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_ADV_PARAM,this);
			}
			break;
		case CONFIG_VCA_EVENTS:
			{
				m_pPage[_iIndex] = new CLS_VCAEventPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_EVENTS,this);
			}
			break;
		case CONFIG_VCA_ALARM:
			{

			}
			break;
		case CONFIG_VCA_QUERY_FILES:
			{
				m_pPage[_iIndex] = new CLS_DlgCfgVcaQueryFile(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_QUERY_FILE,this);
			}
			break;
		case CONFIG_VCA_TARGET_PICTURE_MNG:
			{
				m_pPage[_iIndex] = new CLS_DlgCfgTargetPicMng(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_TARGET_PICTURE_MNG,this);
			}
			break;
		case CONFIG_VCA_GPS_LOCATION:
			{
				m_pPage[_iIndex] = new CLS_DlgCfgGPSLocation(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_GPS_LOCATION,this);
			}
			break;
		case CONFIG_VCA_FLUXSTATISTIC:
			{
				m_pPage[_iIndex] = new CLS_DlgCfgFluxStatistic(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_FLUXSTATISTIC,this);
			}
			break;
		case CONFIG_VCA_MASK_AREA:
			{
				m_pPage[_iIndex] = new CLS_VcaMaskArea(this);
				m_pPage[_iIndex]->Create(IDD_DLG_VCA_MAKS_AREA,this);
			}
			break;
		case CONFIG_VCA_DECTECT_AREA:
			{
				m_pPage[_iIndex] = new CLS_VcaDetectArea(this);
				m_pPage[_iIndex]->Create(IDD_DLG_VCA_DETECT_AREA,this);
			}
			break;
		case CONFIG_VCA_SCAN_AREA:
			{
				m_pPage[_iIndex] = new CLS_VCAScanArea(this);
				m_pPage[_iIndex]->Create(IDD_DLG_VCA_SCAN_AREA,this);
			}
			break;
		case CONFIG_VCA_3DMASKAREA:
			{
				m_pPage[_iIndex] = new CLS_3DMaskArea(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_3DMASK,this);
			}
			break;
        case CONFIG_VCA_CALIBRATEMODE:
            {
                m_pPage[_iIndex] = new CLS_CalibrateMode(this);
                m_pPage[_iIndex]->Create(IDD_DIG_CFG_VCA_CALIBRATEMODE,this);
            }
            break;
        case CONFIG_VCA_SCENE_HDSCHEDULE_PARA:
            {
                m_pPage[_iIndex] = new CLS_SceneHDScheduleParam(this);
                m_pPage[_iIndex]->Create(IDD_DIG_CFG_VCA_SCENE_HDSCHEDULE,this);
            }
            break;
        case CONFIG_VCA_SCENE_FOCUSAREA_PARA:
            {
                m_pPage[_iIndex] = new CLS_SceneFocusArea(this);
                m_pPage[_iIndex]->Create(IDD_DIG_CFG_VCA_SCENE_FOCUSAREA,this);
            }
            break;
		case CONFIG_VCA_CPC_AREA:
			{
				m_pPage[_iIndex] = new CLS_DlgCPCArea(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_CPC_AREA,this);
			}
			break;
		case CONFIG_COM:
			{
				m_pPage[_iIndex] = new CLS_ComPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_COM,this);
			}
			break;
		case CONFIG_PERIPHERAL:
			{
				m_pPage[_iIndex] = new CPeriphreal(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_PERIPHREAL,this);
			}
			break;
		case CONFIG_IOPORT:
			{
				m_pPage[_iIndex] = new CLS_IOPORTPAGE(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_IOPORT,this);
			}
			break;
		case CONFIG_ADVANCE_VERSION:
			{
				m_pPage[_iIndex] = new CLS_AdvVersionPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ADVANCE_VERSION,this);
			}
			break;
		case CONFIG_ADVANCE_SYSTEM:
			{
				m_pPage[_iIndex] = new CLS_AdvSystemInfoPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ADVANCE_SYSTEM_INFO,this);
			}
			break;
		case CONFIG_ADVANCE_UPGRADE:
			{
				m_pPage[_iIndex] = new CLS_AdvUpgrade(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ADVANCE_UPGRADE,this);
			}
			break;
		case CONFIG_ADVANCE_CHANNEL:
			{
				m_pPage[_iIndex] = new CLS_AdvChannelParamPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ADVANCE_CHANNEL_SET,this);
			}
			break;
		case CONFIG_VCA_ALARM_INFO:
			{
				m_pPage[_iIndex] = new CLS_VCAAlarmInfoPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_ALARM_INFORMATION,this);
			}
			break;
		case CONFIG_VCA_ALARM_SCHEDULE:
			{
				m_pPage[_iIndex] = new CLS_VCAAlarmSchedulePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_ALARM_SCHEDULE,this);
			}
			break;
		case CONFIG_VCA_ALARM_LINK:
			{
				m_pPage[_iIndex] = new CLS_VCAAlarmLinkPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_ALARM_LINK,this);
			}
			break;
		case CONFIG_COOPER_DOME:
			{
				m_pPage[_iIndex] = new CLS_DomeInfrared(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_COOPER_DOME,this);
			}
			break;
		case CONFIG_VIDEO_INTERESTED:
			{
				m_pPage[_iIndex] = new CLS_VideoInterested(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VIDEO_INTERESTED,this);
			}
			break;
		case CONFIG_APPEND_OSD:
			{
				m_pPage[_iIndex] = new CLS_AppendOSDPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_APPEND_OSD,this);
			}
			break;
		case CONFIG_OSD_BACK_GROUND:
			{
				m_pPage[_iIndex] = new CLS_OSDBackgroundPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_OSD_BACK_GROUND,this);
			}
			break;	
		case CONFIG_DOME_STATE:
			{
				m_pPage[_iIndex] = new CLS_DomeState(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_STATE,this);
			}
			break;
		case CONFIG_DOME_SCHEDULE:
			{
				m_pPage[_iIndex] = new CLS_DomeSchedule(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_SCHEDULE,this);
			}
			break;
		case CONFIG_DOME_MENU:
			{
				m_pPage[_iIndex] = new CLS_DomeMenu(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_MENU,this);
			}
			break;
		case CONFIG_WHITE_LIGHT:
			{
				m_pPage[_iIndex] = new CLS_WhiteLightControl(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_WHITE_LIGHT_CONTROL,this);
			}
			break;
		case CONFIG_HTTPPICTURE:
			{
				m_pPage[_iIndex] = new CLS_HttpPicture(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_HTTP_PICTURE,this);
			}
			break;
		case CONFIG_FTP_UPLOAD:
			{
				m_pPage[_iIndex] = new CLS_FtpUpload(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FTP_UPLOAD, this);
			}
			break;
		case CONFIG_DONGHUAN_SET:
			{
				m_pPage[_iIndex] = new CLS_DonghuanSet(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DONGHUAN_SET,this);
			}
			break;
		case CONFIG_DONGHUAN_LINKSET:
			{
				m_pPage[_iIndex] = new CLS_LinkSet(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LINK_SET,this);
			}
			break;
		case CONFIG_DONGHUAN_COMFORT:
			{
				m_pPage[_iIndex] = new CLS_SerialManagePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_COM_PAGE,this);
			}
			break;
		case CONFIG_DONGHUAN_SCHEDULE:
			{
				m_pPage[_iIndex] = new CLS_DefenceTemplatePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DONGHUAN_SCHEDULE,this);
			}
			break;
		case CONFIG_DONGHUAN_OSDSET:
			{
				m_pPage[_iIndex] = new  CLS_OSDManagePage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_OSD_SET,this);
			}
			break;
		case CONFIG_DONGHUAN_DATA:
			{
				m_pPage[_iIndex] = new CLS_PowerEnvParamPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DH_DATA,this);
			}
			break;	
		case CONFIG_REPORT_SET:
			{
				m_pPage[_iIndex] = new CLS_ReportSet(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_REPORT_SET,this);
			}
			break;
		case CONFIG_VCA_EVENTS_ADV:
			{
				m_pPage[_iIndex] = new CLS_VCAEvnetsAdvParam(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_EVENTS_ADV_PARAM,this);
			}
			break;
		case CONFIG_DEMO_CRUSIE:
			{
				m_pPage[_iIndex] = new VCAframDomeCurise(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_CRUSIE,this);
			}
			break;
		case CONFIG_VCA_SMART_SEARCH:
			{
 				m_pPage[_iIndex] = new CLS_VCASmartSearch(this);
 				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_SMART_SEARCH,this);
			}
			break;
		case CONFIG_VCA_SMART_TRACK:
			{
				m_pPage[_iIndex] = new CLS_VCAEvnetsSmartTrack(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_EVENTS_SMART_TRACK,this);
			}
			break;
		case CONFIG_ADVANCE_LOCAL:
			{
				m_pPage[_iIndex] = new CLS_AdvLocalSet(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ADVANCE_LOCAL,this);
			}
			break;
		case CONFIG_DOME_HD1:
			{
				m_pPage[_iIndex] = new CLS_DomeHDParam1(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_HD_PARAM1, this);
			}
			break;
		case CONFIG_DOME_HD2:
			{
				m_pPage[_iIndex] = new CLS_DomeHDParam2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_HD_PARAM2, this);
			}
			break;
		case CONFIG_DOME_HD3:
			{
				m_pPage[_iIndex] = new CLS_DomeHDParam3(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_HD_PARAM3, this);
			}
			break;
		case CONFIG_HD_SCHEDULE:
			{
				m_pPage[_iIndex] = new CLS_HDSchedule(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_HD_SCHEDULE, this);
			}
			break;
		case CONFIG_DOME_BASIC1:
			{
				m_pPage[_iIndex] = new CLS_DomeBasicInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_BASIC1, this);
			}
			break;
		case CONFIG_DOME_BASIC2:
			{
				m_pPage[_iIndex] = new CLS_DomeTitle(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_BASIC2, this);
			}
			break;
		case CONFIG_DOME_RUN1:
			{
				m_pPage[_iIndex] = new CLS_DomeRun1(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_RUN1, this);
			}
			break;
		case CONFIG_DOME_RUN2:
			{
				m_pPage[_iIndex] = new CLS_DomeRun2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DOME_RUN2, this);
			}
			break;
		case CONFIG_ITS_PICTURE_COMMON2:
			{
				m_pPage[_iIndex] = new Cls_ItsPictureCommon2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_PICTURE_COMMON2, this);
			}
			break;
		case CONFIG_ITS_PICTURE_OTHER:
			{
				//Cls_ItsPictureOther
				//IDD_DLG_ITS_PICTURE_OTHER
			}
			break;
		case CONFIG_ITS_SIGNAL_OTHER:
			{
				m_pPage[_iIndex] = new Cls_ItsSignalOther(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_SIGNAL_OTHER, this);
			}
			break;
		case CONFIG_ITS_ROADWAY_COMMON2:
			{
				m_pPage[_iIndex] = new CLS_ItsRoadwayCommon2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_COMMON2,this);
			}
			break;
		case CONFIG_ITS_ROADWAY_COMMON4:
			{
				m_pPage[_iIndex] = new CLS_ItsRoadwayCommon4(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_COMMON4,this);
			}
			break;
		case CONFIG_ITS_ROADWAY_ADVANCED1:
			{
				m_pPage[_iIndex] = new CLS_ITSRoadWayAdvanced1(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_ADVANCED1,this);
			}
			break;
		case CONFIG_ITS_COMPO_PIC:
			{
				m_pPage[_iIndex] = new Cls_ItsCompoPic(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_COMPO_PIC,this);
			}
			break;
		case CONFIG_ITS_TRAFFIC_STATIS:
			{
				m_pPage[_iIndex] = new Cls_ItsTrafficStatistics(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_TRAFFIC_STATIS,this);
			}
			break;
		case CONFIG_ITS_PARKCARNUM:
			{
				m_pPage[_iIndex] = new CLS_ITSParkCarNum(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_ILLEGALPARK_CARNUM,this);
			}
			break;
		case CONFIG_ITS_ILLEGAl_TYPE:
			{
				m_pPage[_iIndex] = new Cls_ItsIllegalTppe(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ILLEGAL_TYPE,this);
			}
			break;
		case CONFIG_ITS_STATE_QUERY:
			{
				m_pPage[_iIndex] = new Cls_ItsStateQuery(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_STATE_QUERY,this);
			}
			break;
		case CONFIG_ITS_FOCUS_AID:
			{
				m_pPage[_iIndex] = new Cls_ItsFocusAid(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_FOCUS_AID,this);
			}
			break;
		case CONFIG_LAN_HTTP:
			{
				m_pPage[_iIndex] = new CLS_LinkHttp(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LAN_HTTP,this);
			}
			break;
		case CONFIG_LAN_PORT:
			{
				m_pPage[_iIndex] = new CLS_PortMapping(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LAN_PORT,this);
			}
			break;
		case CONFIG_LAN_UPNP:
			{
				m_pPage[_iIndex] = new CLS_UPnpPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_LAN_UPNP,this);
			}
			break;
		case CONFIG_LAN_QOS:
			{
				m_pPage[_iIndex] = new CLS_QosPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_QOS,this);
			}
			break;
		case CONFIG_LAN_RTMP:
			{
				m_pPage[_iIndex] = new CLS_RtmpPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_RTMP,this);
			}
			break;
		case CONFIG_ITS_ROADWAY_ADVANCED3:
			{
				m_pPage[_iIndex] = new CLS_ITSRoadWayAdvanced3(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_ADVANCED3,this);
			}
			break;
		case CONFIG_ITS_ROADWAY_CAPTURECFG:
			{
				m_pPage[_iIndex] = new CLS_DLG_ITSRoadwayCaptureCfg(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ROADWAY_CAP,this);
			}
			break;
		case CONFIG_ITS_EXTENDED_CONFIG1:
			{
				m_pPage[_iIndex] = new CLS_ExtendedParam(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_EXTENDED_PARAM1,this);
			}
			break;
		case CONFIG_ITS_EXTENDED_CONFIG2:
			{
				m_pPage[_iIndex] = new CLS_ItsAdvanceConf2(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_ADVANCE_CONF2,this);
			}
			break;
		case CONFIG_AUTO_TEST:
			{
				m_pPage[_iIndex] = new CLS_AutoTestPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_AUTO_TEST, this);
			}
			break;
		case CONFIG_AUTO_TEST_MULT:
			{
				m_pPage[_iIndex] = new CLS_DlgCalibrateXY(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CALIBRATEXY, this);
			}
			break;
		case CONFIG_THERMOGRAPHY:
			{
				m_pPage[_iIndex] = new CLS_DlgThermography(this);
				m_pPage[_iIndex]->Create(IDD_DLG_THERMOGRAPHY, this);
			}
			break;
		case CONFIG_ITS_IO:
			{
				m_pPage[_iIndex] = new CLS_IOData(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_IO_DATA, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_CTG:		//Add by TDY
			{
				m_pPage[_iIndex] = new CLS_DLG_FUNC_COLORTOGRAY(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_COLORTOGRAY, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_VTC:
			{
				m_pPage[_iIndex] = new CLS_DLG_CFG_FUNC_VideoTranceCoding(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_VIDEOTCODING, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_SMARTA:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncSmarta(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_SMARTA, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_DDNS:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncDdns(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_DDNS, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_NETWORK:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncNetwork(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_NETWORK, this);
			}
			break;
		case CONFIG_ADVANCE_VCATOPS:
			{
				m_pPage[_iIndex] = new CLS_VcaTops(this);
				m_pPage[_iIndex]->Create(IDD_DLG_VCA_TOPS, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_SUPPORT:
			{
				m_pPage[_iIndex] = new CLS_IS_SUPPORT_FUNC(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_IS_SUPPORT, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_DECODER:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncDecoder(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_DECORDER, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_PEOPLENUM:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncStatisPeopleNum(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_PEOPLENUMEXCEL, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_ROI:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncRoi(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_ROI, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_POTICS_LAW:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncLaw(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_LAW, this);
			}
			break;
		case CONFIG_ADVANCE_FUNC_DOME:
			{
				m_pPage[_iIndex] = new CLS_DlgFuncDome(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FUNC_DOME, this);
			}
			break;
		case CONFIG_ADVANCE_COMMONENABLE:
			{
				m_pPage[_iIndex] = new CLS_DlgCommonEnable(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_COMMONENABLE, this);
			}
			break;
		case CONFIG_ITS_PLATFORM_CFG:
			{
				m_pPage[_iIndex] = new CLS_ITSPlatformConfig(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_STAR_NVR_PLAT_CFG, this);
			}
			break;
		case CONFIG_ITS_DEV_MANAGE:
			{
				m_pPage[_iIndex] = new CLS_ITSDevManage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_STAR_NVR_DEV_MNG, this);
			}
			break;
		case CONFIG_ITS_LANE_MANAGE:
			{
				m_pPage[_iIndex] = new CLS_ITSLaneManage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_STAR_NVR_LANE_MNG, this);
			}
			break;
		case CONFIG_ITS_BAYONET_DATA:
			{
				m_pPage[_iIndex] = new CLS_ITSBayonetData(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ITS_STAR_NVR_BAYONET_DATA, this);
			}
			break;
		case CONFIG_FEC_HEAT_MAP:
			{
				m_pPage[_iIndex] = new CLS_FECHeatMap(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FEC_HEAT_MAP, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT:
		case CONFIG_UNIQUE_ALERT_ENABLE_STATUS:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertEnable(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_ENABLE_STATUS, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT_PLAN:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertPlan(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_PLAN, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT_CUSTOM_EVENT:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertCustom(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_CUSTOM, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT_CUSTOM_EVENT_PERIMETER:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertEventPerimeter(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_EVENT_PERIMETER, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT_CUSTOM_EVENT_TRIPWIRE:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertEventTripwire(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_EVENT_TRIPWIRE, this);
			}
			break;
        case CONFIG_UNIQUE_ALERT_CUSTOM_EVENT_CLIMBWALL:
            {
                m_pPage[_iIndex] = new CLS_DlgUniqueAlertEventClimbWall(this);
                m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_EVENT_CLIMBWALL, this);
            }
            break;
		case CONFIG_UNIQUE_ALERT_ALARM_LINK:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertAlarmLink(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_ALARM_LINK, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT_FUNC_ASSEMBLE:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertFuncAssemble(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_FUNC_ASSEMBLE, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT_ALARM_MESSAGE:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertAlarmMessage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_ALARM_MESSAGE, this);
			}
			break;
		case CONFIG_UNIQUE_ALERT_TARGET:
			{
				m_pPage[_iIndex] = new CLS_DlgUniqueAlertTarget(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ALERT_TARGET, this);
			}
			break;
		case COMFIG_IRRIGATION_NOTIFY:
			{
				m_pPage[_iIndex] = new IrrigationNotify(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_IRRIGATION_NOTIFY, this);
			}
			break;
		case COMFIG_IRRIGATION_PARAM:
			{
				m_pPage[_iIndex] = new CLS_IrrigationParaConf(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_IRRIGATION_INFO, this);
			}
			break;
		case COMFIG_GENERAL_CONFIG:
			{
				m_pPage[_iIndex] = new CLS_IrrigationGeneralConfig(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_GENERAL_CONFIGURATION, this);
			}
			break;
		case CONFIG_GAUGE_CALIB:
			{
				m_pPage[_iIndex] = new CLS_GaugeCalib(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_GAUGECALIB, this);
			}
			break;
		case CONFIG_DEV_DORMANCY:
			{
				m_pPage[_iIndex] = new CLS_DormancyPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_DORMANCY, this);
			}
			break;
		case CONFIG_WATER_INFO:
			{
				m_pPage[_iIndex] = new CLS_DlgWaterInfo(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_WATERINFO, this);
			}
			break;
		case CONFIG_WATERFLOW_EDIT:
			{
				m_pPage[_iIndex] = new CLS_DlgVerticalline(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_WATERFLOWINFO, this);
			}
			break;
		case CONFIG_TRIBLE_VCA_CONF:
			{
				m_pPage[_iIndex] = new CLS_TribleVCA(this);
				m_pPage[_iIndex]->Create(IDD_DLG_TRIBLE_VCA, this);
			}
			break;
		case CONFIG_SNMP:
			{
				m_pPage[_iIndex] = new CLS_SNMPPage(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_SNMPPARA, this);
			}
			break;
		case CONFIG_SHDB:
			{
				m_pPage[_iIndex] = new CLS_Shdb(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_SHDB, this);
			}
			break;
		case CONFIG_INTERNATION_PRO:
			{
				m_pPage[_iIndex] = new CLS_InternationPro(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_INTERNATION_PRO, this);
			}
			break;
		case CONFIG_FIXED_DISK_STORAGE:
			{
				m_pPage[_iIndex] = new CLS_FixedDiskStorage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_FIXED_DISK_STORAGE, this);
			}
			break;
		case CONFIG_TARGET_DETECT:
			{
				m_pPage[_iIndex] = new CLS_TargetDetect(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_TARGETDETECT, this);
			}
			break;
		case CONFIG_DNVR_XVR:
			{
				m_pPage[_iIndex] = new CLS_XVR(this);
				m_pPage[_iIndex]->Create(IDD_DIG_XVR, this);
			}
			break;
		case CONFIG_XNVR_UPDATED:
			{
				m_pPage[_iIndex] = new CLS_XNVR_UPDATA(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_XNVR_UPDATA, this);
			}
			break;
		case CONFIG_HTTP_XML_CFG:
			{
				m_pPage[_iIndex] = new CLS_HttpXmlCfg(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_HTTP_XML, this);
			}
			break;
		case CONFIG_ITS_RADAR_CFG:
			{
				m_pPage[_iIndex] = new CLS_ItsRadarInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_RADAR, this);
			}
			break;
		case CONFIG_ITS_RADAR_LED_CFG:
			{
				m_pPage[_iIndex] = new CLS_ItsRadarLedInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_LED_INFO, this);
			}
			break;
		case CONFIG_ITS_CALIBRATE:
			{
				m_pPage[_iIndex] = new CLS_Calibrate(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_CALIBRATE, this);
			}
			break;
		case CONFIG_TRAFFIC_VIOLATION:
			{
				m_pPage[_iIndex] = new Cls_ItsTrafficViolationPara(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_TRAFFIC_VIOLATION_PARA, this);
			}
			break;
		case CONFIG_FDD_FUNCTION:
			{
				m_pPage[_iIndex] = new CLS_DlgFDDFunction(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_FDD, this);
			}
			break;
		case CONFIG_WIRELESS_SILENT:
			{
				m_pPage[_iIndex] = new CDlgWirelessSilent(this);
				m_pPage[_iIndex]->Create(IDD_DLG_WIRELESS_SILENT,this);
			}
			break;
		case CONFIG_GB28181SET:
			{
				m_pPage[_iIndex] = new CLS_GBT28181Set(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_GBT28181SET,this);
			}
			break;
		case CONFIG_AIRLINEINFO:
			{
				m_pPage[_iIndex] = new CLS_AirLinesPlan(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_AIRLINESPLAN,this);
			}
			break;
		case CONFIG_CALIBRATION_REFERENCE:
			{
				m_pPage[_iIndex] = new CLS_VCA_Reference(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_REFERENCE,this);
			}
			break;
		case CONFIG_RTPSERVERINFO:
			{
				m_pPage[_iIndex] = new CLS_RTPServerInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_RTPSERVERINFO,this);
			}
			break;
		case CONFIG_CERTIFICATE_AUTHFILE:
			{
				m_pPage[_iIndex] = new CLS_CertificateAndAuthFile(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_GET_CERTIFICATE,this);
			}
			break;
		case CONFIG_CHANNEL_ALARMINFO:
			{
				m_pPage[_iIndex] = new CLS_DlgChannelAlarmInfo(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CHANNEL_ALARMINFO,this);
			}
			break;
		case CONFIG_SEND_COMMONDATA:
			{
				m_pPage[_iIndex] = new CLS_DlgSendCommonData(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_SEND_COMMONDATA,this);
			}
			break;
		case CONFIG_VCA_GUAGEINFO:
			{
				m_pPage[_iIndex] = new CLS_GaugeConfig(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_WATERGAUGE,this);
			}
			break;
		case CONFIG_VCA_WATERSPEED:
			{
				m_pPage[_iIndex] = new CLS_WaterSpeedRule(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_WATERSPEED_RULE,this);
			}
			break;
		case CONFIG_VCA_WSTABLEUSEMODE:
			{
				m_pPage[_iIndex] = new CLS_AnemometerConfig(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_WSTABLE_USE_MODE,this);
			}
			break;
		case CONFIG_VCA_ELECTRONIC_FENCE:
			{
				m_pPage[_iIndex] = new CLS_ElectronicFenceConfig(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_ELECTRONIC_FENCE_CONFIG,this);
			}
			break;
		case CONFIG_VCA_IRRRECEIVERMSG:
			{
				m_pPage[_iIndex] = new CLS_IrrReceiverMsgConfig(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_IRRRECEIVERMSG_CONFIG,this);
			}
			break;
		case CONFIG_VERTICAL_LINE_QUERY:
			{
				m_pPage[_iIndex] = new CLS_VertiLineQuery(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_VERTICAL_LINE_QUERY,this);
			}
			break;
		case CONFIG_THREEDIMTRANS:
			{
				m_pPage[_iIndex] = new CLS_DlgXmlThreeDimTrans(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_THREEDIM_TRANS,this);
			}
			break;

		case CONFIG_ONVIF_VCAALARM:
			{
				m_pPage[_iIndex] = new CLS_OnVif_VcaAlarm(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ONVIF_VCAALARM,this);
			}
			break;
		case CONFIG_ELEVATOR_MONITOR:
			{
				m_pPage[_iIndex] = new CLS_DlgElevatorMonitor(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_ELEVATOR_MONITOR,this);
			}
			break;
		case CONFIG_ELEVATOR_STATE:
			{
				m_pPage[_iIndex] = new CLS_DlgElevatorState(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_ELEVATOR_STATE,this);
			}
			break;
		case CONFIG_ELEVATOR_ALLSTATE:
			{
				m_pPage[_iIndex] = new CLS_DlgElevatorAllState(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_ELEVATOR_ALLSTATE,this);
			}
			break;
		case CONFIG_ADVANCE_PROT_DETECT:
			{
				m_pPage[_iIndex] = new CLS_AdvProtDetectPage(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_ADVANCE_PROT_DETECT,this);
			}
			break;
		case CONFIG_VCA_RADARLINKSCENE:
			{
				m_pPage[_iIndex] = new DlgVcaRadarLinkScene(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_RADARLINKSCENE, this);
			}
			break;
		case CONFIG_VCA_REFBOUNDARYINFO:
			{
				m_pPage[_iIndex] = new DlgVcaRefBoundaryInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_REFBOUNDARYINFO, this);
			}
			break;
		case CONFIG_CGI_FALLINGOBJECT:
			{
				m_pPage[_iIndex] = new CLS_FallingObjectPage(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_FALLINGOBJECT, this);
			}
			break;
		case CONFIG_ALARM_COUNT:
			{
				m_pPage[_iIndex] = new DlgVcaAlarmCountStat(this);
				m_pPage[_iIndex]->Create(IDD_DLG_CFG_VCA_ALARM_COUNTSTAT, this);
			}
			break;
		case CONFIG_MIX_AUDIO_CONFIG:
			{
				m_pPage[_iIndex] = new DlgXmlMixAudioConfig(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_MIXAUDIOCONFIG, this);
			}
			break;
		case CONFIG_LICENSE_PLATE_MANAGEMENT:
			{
				m_pPage[_iIndex] = new CLS_ItsPlateInfo(this);
				m_pPage[_iIndex]->Create(IDD_DLG_ITS_PLATE_INFO, this);
			}
			break;
		case CONFIG_POLICECLOTH:
			{
				m_pPage[_iIndex] = new CLS_PoliceCloth(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_POLICECLOTH, this);
			}
			break;
		case CONFIG_PRISONERCLOTH:
			{
				m_pPage[_iIndex] = new CLS_PrisonerCloth(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_PRISONERCLOTH, this);
			}
			break;
		case CONFIG_QUEUESTAND:
			{
				m_pPage[_iIndex] = new CLS_QueueStand(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_QUEUESTAND, this);
			}
			break;
		case CONFIG_VCA_IRRIDATAUPLOADPARAM:
			{
				m_pPage[_iIndex] = new CLS_DlgIrriDataUploadParam(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_IRRIDATA_UPLOADPARAM, this);
			}
			break;
		case CONFIG_PLATELIB:
			{
				m_pPage[_iIndex] = new CLS_PlateLibManage(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_PLATELIB, this);
			}
			break;
		case CONFIG_STRATEGY_CONDITION:
			{
				m_pPage[_iIndex] = new CLS_StrategyCondition(this);
				m_pPage[_iIndex]->Create(IDD_DIALOG_CFG_XML_STRATEGY_CONDITION, this);
			}
			break;
		default:
			break;
		}
		
		if (m_pPage[_iIndex])
		{
			RECT rcShow = {0};
			GetDlgItem(IDC_STATIC_CENTER_BG)->GetWindowRect(&rcShow);
			ScreenToClient(&rcShow);
			m_pPage[_iIndex]->MoveWindow(&rcShow);
		}
		else
		{
			return -1;
		}
	}

	if(m_pCurrent)
	{
		m_pCurrent->ShowWindow(SW_HIDE);
		CloseVideo();
	}
	m_pCurrent = m_pPage[_iIndex];

	//Notify before display
	if (m_iLogonID < 0)
	{
		LockPage(TRUE);
	}
	else
	{
		LockPage(FALSE);
		m_pCurrent->OnChannelChanged(m_iLogonID,m_iChannelNo,m_iStreamNo);
	}
	m_pCurrent->ShowWindow(SW_SHOW);

	return 0;
}

void CLS_ConfigWindow::OnTvnSelchangedTreeConfig(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMTREEVIEW pNMTreeView = reinterpret_cast<LPNMTREEVIEW>(pNMHDR);

	HTREEITEM hItem = pNMTreeView->itemNew.hItem;
	if(NULL != hItem)
	{
		int iIndex = (int)m_tvConfig.GetItemData(hItem);
		ShowBasePage(iIndex);
	}

	*pResult = 0;
}

void CLS_ConfigWindow::OnDestroy()
{
	CLS_BaseWindow::OnDestroy();

	if (m_pVideo)
	{
		m_pVideo->DestroyWindow();
		delete m_pVideo;
		m_pVideo = NULL;
	}
	for (int i = 0; i < CONFIG_MAX; ++i)
	{
		if (m_pPage[i])
		{
			m_pPage[i]->DestroyWindow();
			delete m_pPage[i];
			m_pPage[i] = NULL;
		}
	}
	m_pCurrent = NULL;
	
}

void CLS_ConfigWindow::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	if (m_iLogonID == _iLogonID && m_iChannelNo == _iChannelNo && m_iStreamNo == _iStreamNo)
	{
		return;
	}

	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	m_iStreamNo = _iStreamNo;
	if (m_iLogonID < 0)
	{
		LockPage(TRUE);
	}
	else
	{
		LockPage(FALSE);
		if (m_pCurrent)
		{
			CloseVideo();
			m_pCurrent->OnChannelChanged(_iLogonID,_iChannelNo,_iStreamNo);
		}
	}
}

int CLS_ConfigWindow::LockPage(BOOL _bLocked)
{
	if (m_bLocked == _bLocked)
	{
		return 0;
	}

	m_bLocked = _bLocked;
	BOOL bEnable = TRUE;
	if (TRUE == _bLocked)
	{
		bEnable = FALSE;
	}
	m_tvConfig.EnableWindow(bEnable);
	if (m_pCurrent)
	{
		m_pCurrent->EnableWindow(bEnable);
	}
	return 0;
}

void CLS_ConfigWindow::OnLanguageChanged( int _iLanguage )
{
	for (int i = 0; i < CONFIG_MAX; ++i)
	{
		if (m_pPage[i])
		{
			m_pPage[i]->OnLanguageChanged(_iLanguage);
		}
	}
	UI_UpdateDialog();
}

void CLS_ConfigWindow::UI_UpdateDialog()
{
	HTREEITEM hItem = NULL;
	HTREEITEM hSubItem = NULL;
	HTREEITEM hThirdItem = NULL;
	
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_AUDIO_VIDEO,CONFIG_VIDEO_PARAM);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_VIDEO_PARAM,CONFIG_VIDEO_PARAM,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_VIDEO_PARAM_SCHEDULE,CONFIG_VIDEO_PARAM_SCHEDULE,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_AUDIO_PARAM,CONFIG_AUDIO_PARAM,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_VIDEO_ENCRYPT,CONFIG_VIDEO_ENCRYPT,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_VIDEO_ENCODE,CONFIG_VIDEO_ENCODE_SLICE,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_VIDEO_INTERESTED,CONFIG_VIDEO_INTERESTED,hItem);

	hItem = InsertItem(m_tvConfig, IDS_CONFIG_OSD, CONFIG_OSD);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIG_OSD, CONFIG_OSD, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CFG_APPEND_OSD, CONFIG_APPEND_OSD, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_OSD_BACK_GROUND, CONFIG_OSD_BACK_GROUND, hItem);
	
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_PTZ,CONFIG_PTZ);

	hItem = InsertItem(m_tvConfig,IDS_CONFIG_COM,CONFIG_COM);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIG_COM, CONFIG_COM, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_PERIPHREAL, CONFIG_PERIPHERAL, hItem);
	
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_8D1,CONFIG_8D1);

	hItem = InsertItem(m_tvConfig, IDS_HD_CAMERA_SET, CONFIG_DOME_HD1);                     //Front-end settings
	hSubItem = InsertItem(m_tvConfig, IDS_DOME_HD_PARAM, CONFIG_DOME_HD1, hItem);           //HD parameters
	hThirdItem = InsertItem(m_tvConfig, IDS_DOME_HD_PARAM1, CONFIG_DOME_HD1, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_DOME_HD_PARAM2, CONFIG_DOME_HD2, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_DOME_HD_PARAM3, CONFIG_DOME_HD3, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_HD_SCHEDULE, CONFIG_HD_SCHEDULE, hSubItem);
    hThirdItem = InsertItem(m_tvConfig,GetTextByLan(_T("标定"), _T("CALIBRATEMODE")), CONFIG_VCA_CALIBRATEMODE,hSubItem);
    hThirdItem = InsertItem(m_tvConfig,GetTextByLan(_T("场景高清模板"), _T("Scene HDSchedule")), CONFIG_VCA_SCENE_HDSCHEDULE_PARA,hSubItem);
    hThirdItem = InsertItem(m_tvConfig,GetTextByLan(_T("场景重点区域"), _T("Scene FocusArea")), CONFIG_VCA_SCENE_FOCUSAREA_PARA,hSubItem);

	hSubItem = InsertItem(m_tvConfig, IDS_CFG_DOME_STATE, CONFIG_DOME_STATE, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_DOME_BASIC_SET, CONFIG_DOME_BASIC1, hItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_DOME_BASIC_INFO, CONFIG_DOME_BASIC1, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_DOME_BASIC_TITLE, CONFIG_DOME_BASIC2, hSubItem);
	hSubItem = InsertItem(m_tvConfig, IDS_DOME_RUN_SET, CONFIG_DOME_RUN1, hItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_DOME_RUN_SET1, CONFIG_DOME_RUN1, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_DOME_RUN_SET2, CONFIG_DOME_RUN2, hSubItem);
	hSubItem = InsertItem(m_tvConfig, IDS_DOME_REDOUT_SET, CONFIG_COOPER_DOME, hItem);	//Cooper ball interface changed to infrared setting interface
	hSubItem = InsertItem(m_tvConfig, IDS_CFG_DOME_SCHEDULE, CONFIG_DOME_SCHEDULE, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CFG_DOME_MENU, CONFIG_DOME_MENU, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIG_HD, CONFIG_HD, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("白光灯"), _T("WhiteLight")), CONFIG_WHITE_LIGHT, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("HTTP推图"), _T("HTTP Picture")), CONFIG_HTTPPICTURE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("发送通用数据"), _T("Send CommonData")), CONFIG_SEND_COMMONDATA, hItem);
	

	//Add ftp upload page

	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("Ftp&上报"), _T("Ftp&Report")), CONFIG_REPORT_SET);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("Ftp&上报"), _T("Ftp&Report")), CONFIG_REPORT_SET, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("Ftp上传"), _T("FtpUpload")), CONFIG_FTP_UPLOAD, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("休眠设置"), _T("Dormancy Set")), CONFIG_DEV_DORMANCY, hItem);

	hItem = InsertItem(m_tvConfig,IDS_CONFIG_CMOS,CONFIG_CMOS);

	hItem = InsertItem(m_tvConfig,IDS_CONFIG_LAN,CONFIG_IP);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_IP,CONFIG_IP,hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIG_IPADV, CONFIG_IPADV, hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_WIFI,CONFIG_WIFI,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_LAN_IPV4,CONFIG_LAN_IPV4,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_LAN_IPV6,CONFIG_LAN_IPV6,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_LAN_WOKEMODE,CONFIG_LAN_WOKEMODE,hItem);	
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_FTP,CONFIG_FTP,hItem); 
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DDNS,CONFIG_DDNS,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_NTP,CONFIG_NTP,hItem);	
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_UDP,CONFIG_UDP,hItem);
	hSubItem = InsertItem(m_tvConfig, "HTTP", CONFIG_LAN_HTTP, hItem);
	hSubItem = InsertItem(m_tvConfig, "PORT", CONFIG_LAN_PORT, hItem);
	hSubItem = InsertItem(m_tvConfig, "QOS", CONFIG_LAN_QOS, hItem);
	hSubItem = InsertItem(m_tvConfig, "UPNP", CONFIG_LAN_UPNP, hItem);
	hSubItem = InsertItem(m_tvConfig, "RTMP", CONFIG_LAN_RTMP, hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_SNMP_PARA,CONFIG_SNMP,hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("无线电静默"), _T("Wireless Silent")), CONFIG_WIRELESS_SILENT, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("RTP组播服务器IP"), _T("RTP multicast server IP")), CONFIG_RTPSERVERINFO, hItem);

	//Platform access
	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("平台接入"),_T("Platform Access")), CONFIG_PLATFORMACCESS);	
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_SO,CONFIG_SO,hItem);
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("GAT1400参数设置"), _T("GAT1400ParamSet")),CONFIG_GAT,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_SIP,CONFIG_SIP,hItem);
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("GB/T28181设置"),_T("GB/T28181Set")),CONFIG_GB28181SET,hItem);


	hItem = InsertItem(m_tvConfig,IDS_CONFIG_3G,CONFIG_3G_NORMAL);
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_3G_NORMAL,CONFIG_3G_NORMAL,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_3G_DVR,CONFIG_3G_DVR,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_3G_ADV,CONFIG_3G_ADV,hItem);

	//4G (Water Conservancy Ball Project)
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_4G,CONFIG_4G_NORMAL);
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_4G_NORMAL,CONFIG_4G_NORMAL,hItem);
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("FDD/TDD"), _T("FDD/TDD")),CONFIG_FDD_FUNCTION,hItem);




	//car settings
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_VEHICLE,CONFIG_VEHICLE);

	//D/NVR
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR,CONFIG_ALARM);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_DIGIT,CONFIG_DNVR_DIGIT,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_VIDEOCOMBINE,CONFIG_DNVR_VIDEOCOMBINE,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_EXCEPTION,CONFIG_DNVR_EXCEPTION,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_CREATEFREEV0,CONFIG_DNVR_CREATEFREEV,hItem);//Screen custom segmentation
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_TEXTPLAN,CONFIG_DNVR_TEXTPLAN,hItem);//Text plan
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_BAWLICENCEPLATE,CONFIG_DNVR_BAWSLICENCEPLATE,hItem);//Black and white license plate
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_CHANNELINFO,CONFIG_DNVR_CHANNEL_INFO,hItem);//DNVR channel information
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("XVR"), _T("XVR")),CONFIG_DNVR_XVR,hItem);//XVR
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("全系列NVR升级"), _T("All Series NVR update")),CONFIG_XNVR_UPDATED,hItem);

	//storage
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_STORAGE,CONFIG_STORAGE);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_STORAGE,CONFIG_STORAGE,hItem);
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("磁盘管理"), _T("HardDiskManage")),CONFIG_HARDDISK_INFO,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_STORAGE_SET,CONFIG_STORAGE_SET,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_STORAGE_DISKINFO,CONFIG_STORAGE_DISKINFO,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_HOLIDAY_PLAN,CONFIG_HOLIDAY_PLAN,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_STORAGE_STRATEGY,CONFIG_STORAGE_STRATEGY,hItem);
	hSubItem = InsertItem(m_tvConfig,"S.M.A.R.T",CONFIG_STORAGE_SMART,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CE_DISKMANAGE,CONFIG_STORAGE_DISKMANAGE,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_STORAGE_CAPTION_ANR,CONFIG_STORAGE_ANR,hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("热备设置"), _T("HotBackupSet")), CONFIG_STORAGE_HOTBACKUP, hItem);

	//log
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_LOG,CONFIG_LOG_NVS);
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_LOG_NVS,CONFIG_LOG_NVS,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_LOG_DVR,CONFIG_LOG_DVR,hItem);

	//Insight
	hItem = InsertItem(m_tvConfig,IDS_CONFIGPAGE_VCA , CONFIG_VCA_EVENTS);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIGPAGE_VCA_EVENTS, CONFIG_VCA_EVENTS, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_VCA_ALARM_SCHEDULE, CONFIG_VCA_ALARM_SCHEDULE, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIG_FTP_LINKMETHOD, CONFIG_VCA_ALARM_LINK, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CFG_CRUSIE, CONFIG_DEMO_CRUSIE, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_VCA_SMART_SEARCH,CONFIG_VCA_SMART_SEARCH, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_VCA_SMART_TRACK,CONFIG_VCA_SMART_TRACK, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_VCA_QUERY_FILE, CONFIG_VCA_QUERY_FILES, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_VCA_TARPIC_PARA, CONFIG_VCA_TARGET_PICTURE_MNG, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("屏蔽区域"), _T("MaskArea")), CONFIG_VCA_MASK_AREA, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("检测区域"), _T("DectectArea")), CONFIG_VCA_DECTECT_AREA, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("扫描区域"), _T("ScanArea")), CONFIG_VCA_SCAN_AREA, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("3D屏蔽区域"), _T("3DMaskArea")), CONFIG_VCA_3DMASKAREA, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("区域人数设置"), _T("CPC Area")), CONFIG_VCA_CPC_AREA, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("标定参考系"), _T("Calibration reference")), CONFIG_CALIBRATION_REFERENCE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("第三方智能分析报警消息"), _T("Third party intelligent analysis alarm message")), CONFIG_ONVIF_VCAALARM, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("雷达联动场景参数"), _T("Radar linkage scene param")), CONFIG_VCA_RADARLINKSCENE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("世界坐标系校验边界"), _T("World coordinate check boundary")), CONFIG_VCA_REFBOUNDARYINFO, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("人数统计信息"), _T("Flux Statistic")), CONFIG_VCA_FLUXSTATISTIC, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("报警计数统计"), _T("Alarm count statistics")), CONFIG_ALARM_COUNT, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("GPS位置信息"), _T("GPS Location")), CONFIG_VCA_GPS_LOCATION, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_VCA_ALARM_INFO, CONFIG_VCA_ALARM_INFO, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIGPAGE_VCA_TARGET, CONFIG_VCA_TARGET, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIGPAGE_VCA_ADV, CONFIG_VCA_ADVANCE, hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CONFIGPAGE_VCA_EVENTS_ADV, CONFIG_VCA_EVENTS_ADV, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("资源分配"), _T("Resource Alloction")), CONFIG_VCA_RESOURCE, hItem);

	// Feature alert
	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("特色警戒"), _T("Unique Alert")), CONFIG_UNIQUE_ALERT);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("使能状态"), _T("Enable Status")), CONFIG_UNIQUE_ALERT_ENABLE_STATUS,hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("警戒预案"), _T("Alert Template")), CONFIG_UNIQUE_ALERT_PLAN,hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("自定义事件参数"), _T("Customized Event")), CONFIG_UNIQUE_ALERT_CUSTOM_EVENT,hItem);
	InsertItem(m_tvConfig, GetTextByLan(_T("周界警戒"), _T("Alert Perimeter")), CONFIG_UNIQUE_ALERT_CUSTOM_EVENT_PERIMETER,hSubItem);
	InsertItem(m_tvConfig, GetTextByLan(_T("绊线警戒"), _T("Alert Tripwire")), CONFIG_UNIQUE_ALERT_CUSTOM_EVENT_TRIPWIRE,hSubItem);
    InsertItem(m_tvConfig, GetTextByLan(_T("翻墙警戒"), _T("Climb Wall")), CONFIG_UNIQUE_ALERT_CUSTOM_EVENT_CLIMBWALL,hSubItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("自定义报警联动"), _T("Customized Alarm Link")), CONFIG_UNIQUE_ALERT_ALARM_LINK,hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("目标参数"), _T("Target Info")), CONFIG_UNIQUE_ALERT_TARGET,hItem);
	hSubItem = InsertItem(m_tvConfig, IDS_CFG_FUNC, CONFIG_UNIQUE_ALERT_FUNC_ASSEMBLE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("报警信息"), _T("Alarm Message")), CONFIG_UNIQUE_ALERT_ALARM_MESSAGE,hItem);

	//Alarm management
	hItem = InsertItem(m_tvConfig,IDS_ALARM_MANAGE , CONFIG_DNVR_ALMLINK);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ALARM,CONFIG_ALARM,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_ALMSCH,CONFIG_DNVR_ALMSCH,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_ALMLINK,CONFIG_DNVR_ALMLINK,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_ALMLINKIPC,CONFIG_DNVR_ALMLINKIPC,hItem);
	hSubItem = InsertItem(m_tvConfig,IDD_DLG_CFG_IOPORT,CONFIG_IOPORT,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_ALARM_HUM_TEM, CONFIG_ALARM_HUM_TEM, hItem);
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("通道报警信息"), _T("Channel Alarm Info")), CONFIG_CHANNEL_ALARMINFO, hItem);

	hItem = InsertItem(m_tvConfig,IDS_CONFIG_ATM,CONFIG_ATM);

	hItem = InsertItem(m_tvConfig,IDS_CONFIG_ITS, CONFIG_ITS_PICTURE_COMMON1);
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_PICTURE, CONFIG_ITS_PICTURE_COMMON1, hItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_PICTURE_COMM1,CONFIG_ITS_PICTURE_COMMON1,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_PICTURE_COMM2,CONFIG_ITS_PICTURE_COMMON2,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_PICTURE_COMM3,CONFIG_ITS_PICTURE_COMMON3,hSubItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_SIGNALlIGHT, CONFIG_ITS_SIGNAL_CHECK, hItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_SIGNALlIGHT_CHECK,CONFIG_ITS_SIGNAL_CHECK,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_OTHER,CONFIG_ITS_SIGNAL_OTHER,hSubItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_ROADWAY, CONFIG_ITS_ROADWAY_COMMON1, hItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ROAD_COMM1,CONFIG_ITS_ROADWAY_COMMON1,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ROAD_COMM2,CONFIG_ITS_ROADWAY_COMMON2,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ROAD_COMM3,CONFIG_ITS_ROADWAY_COMMON3,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ROAD_COMM4,CONFIG_ITS_ROADWAY_COMMON4,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ROAD_ADV1,CONFIG_ITS_ROADWAY_ADVANCED1,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ADVANCED2,CONFIG_ITS_ROADWAY_ADVANCED2,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ADVANCED3,CONFIG_ITS_ROADWAY_ADVANCED3,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ROADWAY_CAP_CFG,CONFIG_ITS_ROADWAY_CAPTURECFG,hSubItem);
	//
	hSubItem = InsertItem(m_tvConfig,IDS_ITS_SYSTEMPARAM1,CONFIG_ITS_SYSTEMPARAM1,hItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_SYS_1,CONFIG_ITS_SYSTEMPARAM1,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_SYS_2,CONFIG_ITS_SYSTEMPARAM2,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_SYS_3,CONFIG_ITS_SYSTEMPARAM3,hSubItem);
	//
	hSubItem = InsertItem(m_tvConfig,IDS_ITS_RECOPARAM,CONFIG_ITS_RECOPARAM,hItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_ITS_RECOPARAM_1, CONFIG_ITS_RECOPARAM, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_ITS_RECOPARAM_2, CONFIG_ITS_RECOPARAM2, hSubItem);
	//
	hSubItem = InsertItem(m_tvConfig,IDS_ITS_EXTENDED_PARAM ,CONFIG_ITS_EXTENDED_CONFIG1,hItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_EXTENDED_PARAM1 , CONFIG_ITS_EXTENDED_CONFIG1, hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_ITS_ADVANCE_CONF2 , CONFIG_ITS_EXTENDED_CONFIG2, hSubItem);
	//
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ITS_OSD,CONFIG_ITS_OSD,hItem);
	//
	hSubItem = InsertItem(m_tvConfig,IDS_ITS_OSD_EX,CONFIG_ITS_OSD_EX,hItem);
	//
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DNVR_ALMLINK,CONFIG_ITS_ALARM_LINK,hItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_COMPO_PIC, CONFIG_ITS_COMPO_PIC, hItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_TRAFFIC_STATIS, CONFIG_ITS_TRAFFIC_STATIS, hItem);
	// illegal parking license plate information
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("违停车辆状态"), _T("Illegal Parking State")), CONFIG_ITS_PARKCARNUM, hItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_ILLEGAL_TYPE,CONFIG_ITS_ILLEGAl_TYPE,hItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_STATE_QUERY, CONFIG_ITS_STATE_QUERY, hItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_FOCUS_AID, CONFIG_ITS_FOCUS_AID, hItem);
	//
	hSubItem = InsertItem(m_tvConfig, IDS_ITS_IO, CONFIG_ITS_IO, hItem);
	//
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ITS_OTHER,CONFIG_ITS_OTHER,hItem);

	hSubItem = InsertItem(m_tvConfig, IDS_ITS_BAYONET_NVR, CONFIG_ITS_PLATFORM_CFG, hItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_ITS_PLATFORM_CFG, CONFIG_ITS_PLATFORM_CFG, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_ITS_DEVICE_MANAGE, CONFIG_ITS_DEV_MANAGE, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_ITS_LANE_MANAGE, CONFIG_ITS_LANE_MANAGE, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, IDS_BAYONET_DATA, CONFIG_ITS_BAYONET_DATA, hSubItem);
	
	//Rayview
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("雷视"), _T("Radar View")), CONFIG_ITS_RADAR_CFG, hItem);
	hThirdItem = InsertItem(m_tvConfig, GetTextByLan(_T("雷达配置"), _T("Radar Configuration")), CONFIG_ITS_RADAR_CFG, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, GetTextByLan(_T("LED配置"), _T("LED Configuration")), CONFIG_ITS_RADAR_LED_CFG, hSubItem);
	hThirdItem = InsertItem(m_tvConfig, GetTextByLan(_T("标定"), _T("Calibrate")), CONFIG_ITS_CALIBRATE, hSubItem);
	//traffic violation
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("交通违法"), _T("Traffic Violation")), CONFIG_TRAFFIC_VIOLATION, hItem);
	//
	hItem = InsertItem(m_tvConfig,IDS_CONFIG_ADVANCE_PAGE,CONFIG_ADVANCE_VERSION);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ADVANCE_VERSION,CONFIG_ADVANCE_VERSION,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ADVANCE_SYSTEM,CONFIG_ADVANCE_SYSTEM,hItem);
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("协议统计"), _T("Protocol Statistics")),CONFIG_ADVANCE_PROT_DETECT,hItem);

	// general enable
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_COMMONENABLE,CONFIG_ADVANCE_COMMONENABLE,hItem);

	hSubItem = InsertItem(m_tvConfig,IDS_CFG_ADV_LOCAL_SET,CONFIG_ADVANCE_LOCAL,hItem);	
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_USER,CONFIG_USER,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_AUTHORITY_MANAGEMENT,CONFIG_AUTHORITY_LOCAL,hItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_CFG_LOCAL_RIGHT,CONFIG_AUTHORITY_LOCAL,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_CFG_REMOTE_RIGHT,CONFIG_AUTHORITY_REMOTE,hSubItem);
	hThirdItem = InsertItem(m_tvConfig,IDS_CFG_CHANNEL_RIGHT,CONFIG_AUTHORITY_CHANNEL,hSubItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_LANGUAGE_TIMEZONE,CONFIG_LANGUAGE,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ADVANCE_UPGRADE_BACKUP,CONFIG_ADVANCE_UPGRADE,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ADVANCE_CHANNEL_PARAM,CONFIG_ADVANCE_CHANNEL,hItem);	
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DZ,CONFIG_DZ,hItem);
    hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_OPTION_PU,CONFIG_OPTION_PU,hItem);
    
		
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_ADV,CONFIG_ADV,hItem);
    hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("网络测试"), _T("Net Test")), CONFIG_NETTEST, hItem);
    hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("韦根参数"), _T("Wiegand")), CONFIG_WIEGAND, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("证书和认证文件"), _T("Certificate And Auth File")), CONFIG_CERTIFICATE_AUTHFILE, hItem);

	hItem = InsertItem(m_tvConfig,IDS_CONFIG_DONGHUAN_MAN,CONFIG_DONGHUAN_SET);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DONGHUAN_SET,CONFIG_DONGHUAN_SET,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DONGHUAN_LINKSET,CONFIG_DONGHUAN_LINKSET,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DONGHUAN_COMFORT,CONFIG_DONGHUAN_COMFORT,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DONGHUAN_SCHEDULE,CONFIG_DONGHUAN_SCHEDULE,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DONGHUAN_OSDSET,CONFIG_DONGHUAN_OSDSET,hItem);
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DONGHUAN_DATA,CONFIG_DONGHUAN_DATA,hItem);

	hItem = InsertItem(m_tvConfig, IDS_AUTOTEST, CONFIG_AUTO_TEST);
	//Coordinate calibration, automatic debugging extension
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("自动化调试扩展"), _T("AutoTestMult")), CONFIG_AUTO_TEST_MULT, hItem);
	
	//Thermal Imaging
	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("热成像"), _T("Thermography")), CONFIG_THERMOGRAPHY);

	// Capability set TDY
	hItem = InsertItem(m_tvConfig,IDS_CFG_FUNC,CONFIG_ADVANCE_FUNCASSEMBLE);
	// Capability set details
	hSubItem = InsertItem(m_tvConfig,GetTextByLan(_T("能力集详情"), _T("Funcability details")),CONFIG_ADVANCE_FUNC_SUPPORT,hItem);	
	//color to black
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_FUNC_CTG,CONFIG_ADVANCE_FUNC_CTG,hItem);
	//Video transcoding
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_FUNC_VIDEOTRANSCODING,CONFIG_ADVANCE_FUNC_VTC,hItem);	
	//DDNS
	hSubItem = InsertItem(m_tvConfig,"DDNS",CONFIG_ADVANCE_FUNC_DDNS,hItem);	
	//People Counting
	hSubItem = InsertItem(m_tvConfig,IDS_VCA_PEOPLENUM,CONFIG_ADVANCE_FUNC_PEOPLENUM,hItem);	
	//political and legal business
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_FUNC_POTICS_LAW,CONFIG_ADVANCE_FUNC_POTICS_LAW,hItem);
	//decoder business
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_FUNC_DECODER,CONFIG_ADVANCE_FUNC_DECODER,hItem);
	//ROI
	hSubItem = InsertItem(m_tvConfig,"ROI",CONFIG_ADVANCE_FUNC_ROI,hItem);
	// ball machine parameters
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_FUNC_DOME,CONFIG_ADVANCE_FUNC_DOME,hItem);	
	//Insight
	hSubItem = InsertItem(m_tvConfig,IDS_CONFIG_DVR_VCA,CONFIG_ADVANCE_FUNC_SMARTA,hItem);	
	//Network parameters  
	hSubItem = InsertItem(m_tvConfig,IDS_NETWORK,CONFIG_ADVANCE_FUNC_NETWORK,hItem);	
	// algorithm computing power
	hSubItem = InsertItem(m_tvConfig,"VcaTops",CONFIG_ADVANCE_VCATOPS,hItem);	
	//add end

	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("水利"), _T("Irrigation")), COMFIG_IRRIGATION_NOTIFY);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("信息上报"), _T("Irrigation Notify")), COMFIG_IRRIGATION_NOTIFY, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("参数配置"), _T("Param Config")), COMFIG_IRRIGATION_PARAM, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("通用配置"), _T("General Config")), COMFIG_GENERAL_CONFIG, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("流量/水位数据"), _T("Flow/Level Data")), CONFIG_WATER_INFO, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("断面垂线信息"), _T("VerticallLine Data")), CONFIG_WATERFLOW_EDIT, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("断面垂线信息结果查询"), _T("VerticallLine Data Result Query")), CONFIG_VERTICAL_LINE_QUERY, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("无人机参数配置"), _T("Flight route generation parameters")), CONFIG_AIRLINEINFO, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("水尺信息配置"), _T("Water Gauge Info Config")), CONFIG_VCA_GUAGEINFO, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("规则参数(流速)配置"), _T("Rule parameter (flow rate) config")), CONFIG_VCA_WATERSPEED, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("流速表使用模式配置"), _T("Flow meter usage mode config")), CONFIG_VCA_WSTABLEUSEMODE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("数据检测上报"), _T("Data detection and reporting")), CONFIG_VCA_IRRIDATAUPLOADPARAM, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("电子围栏和扩展信息"), _T("Electronic fence and extend info")), CONFIG_VCA_ELECTRONIC_FENCE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("联系人信息"), _T("contact information")), CONFIG_VCA_IRRRECEIVERMSG, hItem);

	hItem = InsertItem(m_tvConfig,IDS_CFG_FEC,CONFIG_FEC);
	//heat map
	hSubItem = InsertItem(m_tvConfig,IDS_CFG_FEC_HEAT_MAP,CONFIG_FEC_HEAT_MAP,hItem);


	//Sanzhi
	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("三智"), _T("TribleVCA")), CONFIG_TRIBLE_VCA);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("通用配置"), _T("CommonConf")), CONFIG_TRIBLE_VCA_CONF, hItem);

	

	//Shanghai landmark
	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("定制模块"), _T("Custom module")), CONFIG_CSPM);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("SHDB"), _T("SHDB")), CONFIG_SHDB,hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("国际Pro"), _T("InternationPro")), CONFIG_INTERNATION_PRO, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("定盘回补"), _T("RecordSupplement")), CONFIG_FIXED_DISK_STORAGE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("靶标检测算法"), _T("TargetDetection")), CONFIG_TARGET_DETECT, hItem);
	//CGI protocol, add business interface
	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("CGI协议"), _T("CGI Business module")), CONFIG_CGIBUSINESSMODULE);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("Xml配置"), _T("Xml Config")), CONFIG_HTTP_XML_CFG, hItem);

	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("3D指定通道坐标转换"), _T("Three Dim Trans")), CONFIG_THREEDIMTRANS, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("高空抛物"), _T("Falling Object")), CONFIG_CGI_FALLINGOBJECT, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("混音输入源配置"), _T("Mix input source config")), CONFIG_MIX_AUDIO_CONFIG, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("民警警服检测参数"), _T("Civilian police uniform detection param")), CONFIG_POLICECLOTH, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("识别服检测参数"), _T("Identify service detection param")), CONFIG_PRISONERCLOTH, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("教育队列检测参数"), _T("Education cohort detection param")), CONFIG_QUEUESTAND, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("车牌库管理"), _T("License plate library management")), CONFIG_PLATELIB, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("车牌管理"), _T("License plate management")), CONFIG_LICENSE_PLATE_MANAGEMENT, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("智能场景"), _T("Strategy Input")), CONFIG_STRATEGY_CONDITION, hItem);

	//Add the elevator business interface
	hItem = InsertItem(m_tvConfig, GetTextByLan(_T("电梯业务"), _T("Elevator Business module")), CONFIG_ELEVATOR_BUSINESSMODULE);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("监控参数"), _T("Monitor Config")), CONFIG_ELEVATOR_MONITOR, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("状态参数"), _T("State Config")), CONFIG_ELEVATOR_STATE, hItem);
	hSubItem = InsertItem(m_tvConfig, GetTextByLan(_T("全状态参数"), _T("All State Config")), CONFIG_ELEVATOR_ALLSTATE, hItem);


}

BOOL CLS_ConfigWindow::ShowVideo()
{
	if (NULL == m_pVideo)
	{
		m_pVideo = new CLS_VideoPage(this);
		m_pVideo->Create(IDD_DLG_CFG_VIDEO);
	}
	if (m_pVideo)
	{
		int iChannelNo = 0;
		int iStreamNo = 0;
		if (m_iChannelNo > 0)
		{
			iChannelNo = m_iChannelNo;
		}
		if (m_iStreamNo > 0)
		{
			iStreamNo = m_iStreamNo;
		}
		return m_pVideo->ShowVideo(m_iLogonID,iChannelNo,iStreamNo);
	}
	
	return FALSE;
}

BOOL CLS_ConfigWindow::CloseVideo()
{
	if (m_pVideo)
	{
		return m_pVideo->CloseVideo();
	}
	return FALSE;
}

void CLS_ConfigWindow::OnNMDblclkTreeConfig(NMHDR *pNMHDR, LRESULT *pResult)
{
	HTREEITEM hItem = m_tvConfig.GetSelectedItem();
	if (hItem)
	{
		if (m_tvConfig.ItemHasChildren(hItem))
		{
			return;
		}
		ShowVideo();
	}

	*pResult = 0;
}

void CLS_ConfigWindow::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BaseWindow::OnShowWindow(bShow, nStatus);

	if (FALSE == bShow && 0 == nStatus)
	{
		CloseVideo();
	}
}

void CLS_ConfigWindow::OnMainNotify( int _iLogonID,int _wParam, void* _iLParam, void* _iUser )
{
	if (m_pCurrent)
	{
		m_pCurrent->OnMainNotify(_iLogonID,_wParam,_iLParam,_iUser);
	}
}

void CLS_ConfigWindow::OnParamChangeNotify( int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUser )
{
	if (m_pCurrent)
	{
		m_pCurrent->OnParamChangeNotify(_iLogonID,_iChannelNo,_iParaType,_pPara,_iUser);
	}
}

void CLS_ConfigWindow::OnAlarmNotify( int _iLogonID, int _iChannelNo, int _iAlarmState,int _iAlarmType,int _iUser )
{
	if (m_pCurrent)
	{
		m_pCurrent->OnAlarmNotify(_iLogonID,_iChannelNo,_iAlarmState,_iAlarmType,_iUser);
	}
}

void CLS_ConfigWindow::OnAlarmNotify_V5(int _iLogonID, int _iAlarmType, void* _pInfo, int _iSize, void* _pUser)
{
	if (m_pCurrent)
	{
		m_pCurrent->OnAlarmNotify_V5(_iLogonID,_iAlarmType,_pInfo,_iSize,_pUser);
	}
}

void CLS_ConfigWindow::ChangeCurrentPage()
{
	if(m_iCurrentPage == CONFIG_VCA_EVENTS || m_iCurrentPage == CONFIG_VCA_SMART_TRACK || m_iCurrentPage == CONFIG_VCA_3DMASKAREA || m_iCurrentPage == CONFIG_WATER_INFO)
	{		
		HTREEITEM hItem = m_tvConfig.GetSelectedItem();
		for(int i = 0; i < 3; i++)
		{
			hItem = m_tvConfig.GetNextItem(hItem,TVGN_PREVIOUS); 
		}
		m_tvConfig.Select(hItem,TVGN_CARET);
		m_tvConfig.SetFocus();
		if (m_iCurrentPage == CONFIG_WATER_INFO)
		{
			ShowBasePage(COMFIG_IRRIGATION_NOTIFY);
		}
		else
		{
			ShowBasePage(CONFIG_VCA_TARGET);
		}
	}
}

void CLS_ConfigWindow::OnStnClickedStaticCenterBg()
{
	// TODO: Add your control notification handler code here
}

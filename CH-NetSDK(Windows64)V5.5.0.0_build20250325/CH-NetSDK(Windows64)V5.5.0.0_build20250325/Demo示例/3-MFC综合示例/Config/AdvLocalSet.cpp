
#include "stdafx.h"
#include "NetClientDemo.h"
#include "AdvLocalSet.h"
#include "../Common/Ini.h"

// CLS_AdvLocalSet dialog

IMPLEMENT_DYNAMIC(CLS_AdvLocalSet, CDialog)

CLS_AdvLocalSet::CLS_AdvLocalSet(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_AdvLocalSet::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
}

CLS_AdvLocalSet::~CLS_AdvLocalSet()
{
}

void CLS_AdvLocalSet::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_LOGON_MODE, m_cboLogonMode);
	DDX_Control(pDX, IDC_COMBO_USE_RULE, m_cboDemoUseRule);
	DDX_Control(pDX, IDC_CBO_VCAFPGA_QUERYINFO, m_cboVcaFpgaQueryInfo);
	DDX_Control(pDX, IDC_CBO_VCAFPGA, m_cboVcaFpga);
	DDX_Control(pDX, IDC_COMBO_USE_MODE, m_cboDemoUseMode);
	DDX_Control(pDX, IDC_COMBO_IP_VERSION, m_cboIpVersion);
	DDX_Control(pDX, IDC_CHECK_D3D_RENDER, m_chkVideoRenderD3D);
	DDX_Control(pDX, IDC_CHECK_DRAW_RENDER, m_chkVideoRenderDraw);
	DDX_Control(pDX, IDC_CHECK_WRITE_LOGFILE, m_chkWriteLog);
	DDX_Control(pDX, IDC_COMBO_LOGFILE_LEVEL, m_cboLogfileLevel);
	DDX_Control(pDX, IDC_COMBO_TERMINAL_LEVEL, m_cboTerminalLevel);
	DDX_Control(pDX, IDC_CHECK_D3D11_RENDER, m_chkVideoRenderD3D11);
	DDX_Control(pDX, IDC_COMBO_HWDECODETYPE, m_cboHWDecodeType);
}


BEGIN_MESSAGE_MAP(CLS_AdvLocalSet, CDialog)
	ON_BN_CLICKED(IDC_BTN_LOGON_MODE, &CLS_AdvLocalSet::OnBnClickedBtnLogonMode)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_SET_DEMO_USE_RULE, &CLS_AdvLocalSet::OnBnClickedButtonSetDemoUseRule)
	ON_BN_CLICKED(IDC_BTN_VCAFPGA_SET, &CLS_AdvLocalSet::OnBnClickedBtnVcafpgaSet)
	ON_BN_CLICKED(IDC_BTN_VCAFPGA_QUERYINFO_SET, &CLS_AdvLocalSet::OnBnClickedBtnVcafpgaQueryinfoSet)
	ON_CBN_SELCHANGE(IDC_CBO_VCAFPGA_QUERYINFO, &CLS_AdvLocalSet::OnCbnSelchangeCboVcafpgaQueryinfo)
	ON_CBN_SELCHANGE(IDC_CBO_VCAFPGA, &CLS_AdvLocalSet::OnCbnSelchangeCboVcafpga)
	ON_CBN_SELCHANGE(IDC_COMBO_IP_VERSION, &CLS_AdvLocalSet::OnCbnSelchangeComboIpVersion)
	ON_BN_CLICKED(IDC_CHECK_D3D_RENDER, &CLS_AdvLocalSet::OnBnClickedCheckD3dRender)
	ON_BN_CLICKED(IDC_CHECK_DRAW_RENDER, &CLS_AdvLocalSet::OnBnClickedCheckDrawRender)
	ON_BN_CLICKED(IDC_BUTTON_SET_LOG_LEVEL, &CLS_AdvLocalSet::OnBnClickedButtonSetLogLevel)
	ON_BN_CLICKED(IDC_CHECK_D3D11_RENDER, &CLS_AdvLocalSet::OnBnClickedCheckD3d11Render)
	ON_CBN_SELCHANGE(IDC_COMBO_HWDECODETYPE, &CLS_AdvLocalSet::OnCbnSelchangeComboHwdecodetype)
END_MESSAGE_MAP()


BOOL CLS_AdvLocalSet::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();

	LoadSdkWorkMode();

	LoadDemoUseRule();

	return TRUE;
}

void CLS_AdvLocalSet::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
}

void CLS_AdvLocalSet::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_AdvLocalSet::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
}

void CLS_AdvLocalSet::OnMainNotify(int _ulLogonID,int _iWparam, void* _iLParam, void* _iUser)
{
	if (m_iLogonID < 0 || m_iLogonID != _ulLogonID)
	{
		return;
	}

	int iMsgType = LOWORD(_iWparam);
	switch(iMsgType)
	{
	case WCM_VCAFPGA_QUERYINFO:
		{
			UI_UpdateVcaFpgaQueryInfo();
		}
		break;
	default:
		break;
	}
}

void CLS_AdvLocalSet::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (m_iLogonID < 0 || m_iLogonID != _iLogonID)
	{
		return;
	}

	if (_iChannelNo == m_iChannelNo)//Only refresh the channel whose parameters have changed
	{
		switch(_iParaType)
		{
		case PARA_VCAFPGA:
			{
				UI_UpdateVcaFpga();
			}
			break;
		default:
			break;
		}
	}
}

void CLS_AdvLocalSet::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STC_LOGON_MODE, IDS_CFG_ADV_LOGON_MODE);
	SetDlgItemTextEx(IDC_BTN_LOGON_MODE, IDS_SET);

	SetDlgItemTextEx(IDC_STC_MODE_RESART_TIP, IDS_ADVANCE_LOCAL_HINT);
	SetDlgItemTextEx(IDC_STATIC_USE_RULE, IDS_ADVANCE_LOCAL_USE_METHOD);
	SetDlgItemTextEx(IDC_BUTTON_SET_DEMO_USE_RULE, IDS_SET);

	InsertString(m_cboLogonMode, 0, IDS_CFG_ADV_LOGON_HEAVY);
	m_cboLogonMode.SetItemData(0, HEAVY_MODE);
	InsertString(m_cboLogonMode, 1, IDS_CFG_ADV_LOGON_LIGHT);
	m_cboLogonMode.SetItemData(1, LIGHT_MODE);
	InsertString(m_cboLogonMode, 2, IDS_CFG_ADV_LOGON_EASYX_LIGHT);
	m_cboLogonMode.SetItemData(2, EASYX_LIGHT_MODE);	
	InsertString(m_cboLogonMode, 3, IDS_CFG_ADV_LOGON_MOBILE_LIGHT);
	m_cboLogonMode.SetItemData(3, MOBILE_LIGHT_MODE);	

	InsertString(m_cboDemoUseRule, 0, IDS_DEMO_USE_NORMAL);
	m_cboDemoUseRule.SetItemData(0, NORMAL_USE);
	InsertString(m_cboDemoUseRule, 1, IDS_DEMO_USE_RIVER);
	m_cboDemoUseRule.SetItemData(1, RIVER_USE);

	InsertString(m_cboDemoUseMode, 0, GetTextByLan(_T("异步模式"), _T("Asynchronous Mode")));
	m_cboDemoUseMode.SetItemData(0, ASYN_MODE);
	InsertString(m_cboDemoUseMode, 1, GetTextByLan(_T("同步模式"), _T("Synchronization Mode")));
	m_cboDemoUseMode.SetItemData(1, SYNC_MODE);

	InsertString(m_cboIpVersion, 0, GetTextByLan(_T("IpV4"), _T("IpV4")));
	m_cboIpVersion.SetItemData(0, IP_VERSION_4);
	InsertString(m_cboIpVersion, 1, GetTextByLan(_T("IpV6"), _T("IpV6")));
	m_cboIpVersion.SetItemData(1, IP_VERSION_6);

	InsertString(m_cboVcaFpgaQueryInfo, 0, IDS_ADVANCE_VCAFPGA_QUERY_RESERVR);
	m_cboVcaFpgaQueryInfo.SetItemData(0, VCAFPGA_RESERVR);
	InsertString(m_cboVcaFpgaQueryInfo, 1, IDS_ADVANCE_VCAFPGA_QUERY_TEMPERATURE);
	m_cboVcaFpgaQueryInfo.SetItemData(1, VCAFPGA_TEMPERATURE);	
	InsertString(m_cboVcaFpgaQueryInfo, 2, IDS_ADVANCE_VCAFPGA_QUERY_TIME);
	m_cboVcaFpgaQueryInfo.SetItemData(2, VCAFPGA_TIME);

	InsertString(m_cboVcaFpga, 0, IDS_ADVANCE_VCAFPGA_RESERVR);
	m_cboVcaFpga.SetItemData(0, VCAFPGA_RESERVR);
	InsertString(m_cboVcaFpga, 1, IDS_ADVANCE_VCAFPGA_MERGECNT);
	m_cboVcaFpga.SetItemData(1, VCAFPGA_TEMPERATURE);	
	InsertString(m_cboVcaFpga, 2, IDS_ADVANCE_VCAFPGA_POSTFILETER);
	m_cboVcaFpga.SetItemData(2, VCAFPGA_TIME);

	m_cboVcaFpgaQueryInfo.SetCurSel(0);
	m_cboVcaFpga.SetCurSel(0);

  
	SetDlgItemText(IDC_STATIC_SDK_LOG, GetTextByLan(_T("SDK日志"), _T("SdkLog")));
	SetDlgItemText(IDC_CHECK_WRITE_LOGFILE, GetTextByLan(_T("写日志"), _T("Write Log")));
	SetDlgItemText(IDC_STATIC_LOGFILE_LEVEL, GetTextByLan(_T("日志级别"), _T("Terminal Level")));
	SetDlgItemText(IDC_STATIC_TERMINAL_LEVEL, GetTextByLan(_T("终端级别"), _T("Logfile Level")));
	SetDlgItemTextEx(IDC_BUTTON_SET_LOG_LEVEL, IDS_SET);
	InsertString(m_cboLogfileLevel, 0, GetTextByLan(_T("禁止"), _T("Forbid")));
	m_cboLogfileLevel.SetItemData(0, SDK_LOG_LEVEL_FORBID);
	InsertString(m_cboLogfileLevel, 1, GetTextByLan(_T("错误"), _T("Eoor")));
	m_cboLogfileLevel.SetItemData(1, SDK_LOG_LEVEL_ERROR);
	InsertString(m_cboLogfileLevel, 2, GetTextByLan(_T("信息"), _T("Msg")));
	m_cboLogfileLevel.SetItemData(2, SDK_LOG_LEVEL_MSG);
	InsertString(m_cboLogfileLevel, 3, GetTextByLan(_T("调试"), _T("Debug")));
	m_cboLogfileLevel.SetItemData(3, SDK_LOG_LEVEL_DEBUG);	
	m_cboLogfileLevel.SetCurSel(1);

	InsertString(m_cboTerminalLevel, 0, GetTextByLan(_T("禁止"), _T("Forbid")));
	m_cboTerminalLevel.SetItemData(0, SDK_LOG_LEVEL_FORBID);
	InsertString(m_cboTerminalLevel, 1, GetTextByLan(_T("错误"), _T("Eoor")));
	m_cboTerminalLevel.SetItemData(1, SDK_LOG_LEVEL_ERROR);
	InsertString(m_cboTerminalLevel, 2, GetTextByLan(_T("信息"), _T("Msg")));
	m_cboTerminalLevel.SetItemData(2, SDK_LOG_LEVEL_MSG);
	InsertString(m_cboTerminalLevel, 3, GetTextByLan(_T("调试"), _T("Debug")));
	m_cboTerminalLevel.SetItemData(3, SDK_LOG_LEVEL_DEBUG);	
	m_cboTerminalLevel.SetCurSel(1);

	SetDlgItemText(IDC_STATIC_AVSET, GetTextByLan(_T("解码和渲染"), _T("Decode And Show")));
	SetDlgItemText(IDC_CHECK_D3D_RENDER, GetTextByLan(_T("D3D9渲染"), _T("D3D9 Rendering")));
	SetDlgItemText(IDC_CHECK_D3D11_RENDER, GetTextByLan(_T("D3D11渲染"), _T("D3D11 Rendering")));
	SetDlgItemText(IDC_CHECK_DRAW_RENDER, GetTextByLan(_T("DDraw渲染"), _T("DDraw Rendering")));
	SetDlgItemText(IDC_STATIC_HW, GetTextByLan(_T("硬件加速解码"), _T("Hardware Decodeing")));
	InsertString(m_cboHWDecodeType, 0, GetTextByLan(_T("自动"), _T("Automatic")));
	InsertString(m_cboHWDecodeType, 1, GetTextByLan(_T("Direct3D11 视频加速"), _T("Direct3D11 Video Acceleration")));
	InsertString(m_cboHWDecodeType, 2, GetTextByLan(_T("DirectX 视频加速(DXVA) 2.0"), _T("DirectX Videi Acceleration(DXVA) 2.0")));
	InsertString(m_cboHWDecodeType, 3, GetTextByLan(_T("禁用"), _T("Disable")));
	m_cboHWDecodeType.SetCurSel(0);

	LoadSdkVideoMode();
	OnCbnSelchangeComboHwdecodetype();
}

void CLS_AdvLocalSet::LoadSdkWorkMode()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\SDKWorkMode.ini";
	CIniFile cFile(szNewFile);

	CString szSection = "WorkMode";
	CString szKey = "Mode";
	int iSDKWorkMode = cFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, 0);
	m_cboLogonMode.SetCurSel(iSDKWorkMode);
}

void CLS_AdvLocalSet::LoadSdkVideoMode()
{
	m_chkVideoRenderD3D.SetCheck(BST_UNCHECKED);
	m_chkVideoRenderDraw.SetCheck(BST_UNCHECKED);
	//Save to the configuration file, it will take effect when the software is restarted
	CString szNewFile = GetLocalSaveDirectory() + "\\SDKVideoMode.ini";
	CIniFile cFile(szNewFile);

	CString szSection = "VideoMode";
	int iMode = 0;
	iMode = cFile.ReadInteger((char *)(LPCTSTR)szSection, "DecodeMode", 0);
	m_cboHWDecodeType.SetCurSel(iMode);

	iMode = cFile.ReadInteger((char *)(LPCTSTR)szSection, "RenderMode", 0);
	if (0 == iMode)
	{
		m_chkVideoRenderD3D11.SetCheck(BST_CHECKED);
		OnBnClickedCheckD3d11Render();	}
	else if (1 == iMode)
	{

		m_chkVideoRenderD3D.SetCheck(BST_CHECKED);
		OnBnClickedCheckD3dRender();
	} 
	else
	{
		m_chkVideoRenderDraw.SetCheck(BST_CHECKED);
		OnBnClickedCheckDrawRender();
	}
}

void CLS_AdvLocalSet::SaveSdkVideoMode(CString sKey,int sValue)
{
	//{//For debugging display format
	//	int iShowType = SHOW_SUB_MODE_YUV420;	//YUV420
	//	int iShowType = SHOW_SUB_MODE_YUV422;	//YUV422
	//	NetClient_SetAVMode(CMD_VIDEO_SHOW_SUB_MODE, &iShowType, sizeof(int));
	//}
	

	//Save to the configuration file, it will take effect when the software is restarted
	CString szNewFile = GetLocalSaveDirectory() + "\\SDKVideoMode.ini";
	CIniFile cFile(szNewFile);

	CString szSection = "VideoMode";

	cFile.WriteInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)sKey, sValue);	
}

void CLS_AdvLocalSet::OnBnClickedBtnLogonMode()
{
	DWORD_PTR iSdkWorkMode = m_cboLogonMode.GetItemData(m_cboLogonMode.GetCurSel());

	//Save to the configuration file, it will take effect when the software is restarted
	CString szNewFile = GetLocalSaveDirectory() + "\\SDKWorkMode.ini";
	CIniFile cFile(szNewFile);
	cFile.ResetFile(); //Recreate the configuration file

	CString szSection = "WorkMode";
	CString szKey = "Mode";
	
	cFile.WriteInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, (int)iSdkWorkMode);	
}

void CLS_AdvLocalSet::LoadDemoUseRule()
{
	CString szNewFile = GetLocalSaveDirectory() + "\\DemoUseRule.ini";
	CIniFile cFile(szNewFile);

	CString szSection = "UseRule";
	CString szKey = "Rule";
	int iUseRule = cFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, 0);

	szSection = "UseMode";
	szKey = "Mode";
	int iUseMode = cFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, 0);

	//szSection = "IpVer";
	//szKey = "Version";
	//int iIpVer = cFile.ReadInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, 0);


	m_cboDemoUseRule.SetCurSel(iUseRule);
	m_cboDemoUseMode.SetCurSel(iUseMode);
	//m_cboIpVersion.SetCurSel(iIpVer);
}

void CLS_AdvLocalSet::UI_UpdateVcaFpga()
{
	int iReturnByte = 0;
	VcaFpga tVcaFpga = {0};
	tVcaFpga.iSize = sizeof(VcaFpga);
	tVcaFpga.iType = (int)m_cboVcaFpga.GetItemData(m_cboVcaFpga.GetCurSel());
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_VCAFPGA, m_iChannelNo, &tVcaFpga, sizeof(VcaFpga), &iReturnByte);
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig][NET_CLIENT_VCAFPGA] Get fail,error = %d", GetLastError());
	}
	else
	{
		SetDlgItemInt(IDC_EDT_VCAFPGA_TEMPLATEINDEX, tVcaFpga.iTemplateIndex);
		SetDlgItemInt(IDC_EDT_VCAFPGA, tVcaFpga.iPARA1);
	}
}

void CLS_AdvLocalSet::UI_UpdateVcaFpgaQueryInfo()
{
	VcaFpgaQueryInfo tFpgaQueryInfo = {0};
	tFpgaQueryInfo.iSize = sizeof(VcaFpgaQueryInfo);
	int iRet = NetClient_RecvCommand(m_iLogonID, COMMAND_ID_VCAFPGA_QUERYINFO, m_iChannelNo, &tFpgaQueryInfo, sizeof(VcaFpgaQueryInfo));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_AdvLocalSet][COMMAND_ID_VCAFPGA_QUERYINFO] Get fail,error = %d", GetLastError());
	}
	else
	{
		SetDlgItemInt(IDC_EDT_VCAFPGA_QUERYINFO, tFpgaQueryInfo.iValue);
	}
}

void CLS_AdvLocalSet::OnBnClickedButtonSetDemoUseRule()
{
	int iUseRule = (int)m_cboDemoUseRule.GetItemData(m_cboDemoUseRule.GetCurSel());
	int iUseMode = (int)m_cboDemoUseMode.GetItemData(m_cboDemoUseMode.GetCurSel());
	int iIpVer = (int)m_cboIpVersion.GetItemData(m_cboIpVersion.GetCurSel());
	CString szNewFile = GetLocalSaveDirectory() + "\\DemoUseRule.ini";
	CIniFile cFile(szNewFile);
	cFile.ResetFile(); //Recreate the configuration file

	CString szSection = "UseRule";
	CString szKey = "Rule";
	cFile.WriteInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, iUseRule);

	szSection = "UseMode";
	szKey = "Mode";
	cFile.WriteInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, iUseMode);

	//szSection = "IpVer";
	//szKey = "Version";
	//cFile.WriteInteger((char *)(LPCTSTR)szSection, (char *)(LPCTSTR)szKey, iIpVer);	
}

void CLS_AdvLocalSet::OnBnClickedBtnVcafpgaSet()
{
	if (m_iLogonID < 0)
	{
		return;
	}

	int iType = (int)m_cboVcaFpga.GetItemData(m_cboVcaFpga.GetCurSel());
	int iTemplateIndex = GetDlgItemInt(IDC_EDT_VCAFPGA_TEMPLATEINDEX);
	int iPARA1 = GetDlgItemInt(IDC_EDT_VCAFPGA);
	VcaFpga tVcaFpga = {0};
	tVcaFpga.iSize = sizeof(VcaFpga);
	tVcaFpga.iChannelNo = m_iChannelNo;
	tVcaFpga.iTemplateIndex = iTemplateIndex;
	tVcaFpga.iPARA1 = iPARA1;
	tVcaFpga.iType = iType;

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCAFPGA, m_iChannelNo, &tVcaFpga, sizeof(VcaFpga));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig][NET_CLIENT_VCAFPGA] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig][NET_CLIENT_VCAFPGA] Set Success", m_iLogonID);
	}
}

void CLS_AdvLocalSet::OnBnClickedBtnVcafpgaQueryinfoSet()
{
	if(m_iLogonID < 0)
	{	
		return;
	}

	int iType = (int)m_cboVcaFpgaQueryInfo.GetItemData(m_cboVcaFpgaQueryInfo.GetCurSel());
	VcaFpgaQueryInfo tFpgaQueryInfo = {0};
	tFpgaQueryInfo.iSize = sizeof(VcaFpgaQueryInfo);
	tFpgaQueryInfo.iChannelNo = m_iChannelNo;	
	tFpgaQueryInfo.iType = iType;

	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_VCAFPGA_QUERYINFO, m_iChannelNo, &tFpgaQueryInfo, sizeof(VcaFpgaQueryInfo));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand][COMMAND_ID_VCAFPGA_QUERYINFO] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SendCommand][COMMAND_ID_VCAFPGA_QUERYINFO] Set Success", m_iLogonID);
	}
}

void CLS_AdvLocalSet::OnCbnSelchangeCboVcafpgaQueryinfo()
{
	UI_UpdateVcaFpgaQueryInfo();
}

void CLS_AdvLocalSet::OnCbnSelchangeCboVcafpga()
{
	UI_UpdateVcaFpga();
}

void CLS_AdvLocalSet::OnCbnSelchangeComboIpVersion()
{
	// TODO: Add control notification handler code here
}

void CLS_AdvLocalSet::SetHwDecodeParam(int _iDecodeType)
{
	if (_iDecodeType < 3)
	{
		//Set dxva hard solution
		HwDecodeParam tHwDecInfo = {0};
		tHwDecInfo.iSize = sizeof(HwDecodeParam);
		tHwDecInfo.iLimitType = HWDEC_LIMIT_LOCAL;
		tHwDecInfo.iMaxCount = 100;
		tHwDecInfo.iEnableFlag = HWDEC_ENABLE_STREAM | HWDEC_ENABLE_VOD | HWDEC_ENABLE_FILE;
		tHwDecInfo.iDecodeType = _iDecodeType;
		NetClient_SetAVMode(-1,CMD_AV_DEC_HWDECODE_PARAM, &tHwDecInfo, sizeof(HwDecodeParam));
		IsShowRenderWindow(SW_HIDE);
	}
	else
	{
		IsShowRenderWindow(SW_SHOW);
		HwDecodeParam tHwDecInfo = {0}; //The parameter can be cleared to zero
		tHwDecInfo.iSize = sizeof(HwDecodeParam);
		NetClient_SetAVMode(-1,CMD_AV_DEC_HWDECODE_PARAM, &tHwDecInfo, sizeof(HwDecodeParam));

		int iVideoDecLib = VIDEO_DLIB_FFMPEG;
		NetClient_SetAVMode(-1,CMD_AVMODE_VIDEO_DECLIB, &iVideoDecLib, sizeof(int));
	}
	SaveSdkVideoMode(_T("DecodeMode"),_iDecodeType);

}

void CLS_AdvLocalSet::OnBnClickedCheckD3d11Render()
{
	if (BST_CHECKED == m_chkVideoRenderD3D11.GetCheck())
	{
		int iRenderType = SHOW_MAIN_MODE_D3D11;
		NetClient_SetAVMode(-1,CMD_VIDEO_SHOW_MAIN_MODE, &iRenderType, sizeof(int));
		m_chkVideoRenderDraw.SetCheck(BST_UNCHECKED);
		m_chkVideoRenderD3D.SetCheck(BST_UNCHECKED);

		SaveSdkVideoMode(_T("RenderMode"),0);
	}
}

void CLS_AdvLocalSet::OnBnClickedCheckD3dRender()
{
	// TODO: Add control notification handler code here
	if (BST_CHECKED == m_chkVideoRenderD3D.GetCheck())
	{
		int iRenderType = SHOW_MAIN_MODE_D3D;
		NetClient_SetAVMode(-1,CMD_VIDEO_SHOW_MAIN_MODE, &iRenderType, sizeof(int));
		m_chkVideoRenderDraw.SetCheck(BST_UNCHECKED);
		m_chkVideoRenderD3D11.SetCheck(BST_UNCHECKED);
		SaveSdkVideoMode(_T("RenderMode"),1);
	}
}

void CLS_AdvLocalSet::OnBnClickedCheckDrawRender()
{
	// TODO: Add control notification handler code here
	if (BST_CHECKED == m_chkVideoRenderDraw.GetCheck())
	{
		int iRenderType = SHOW_MAIN_MODE_DRAW;
		NetClient_SetAVMode(-1,CMD_VIDEO_SHOW_MAIN_MODE, &iRenderType, sizeof(int));
		m_chkVideoRenderD3D.SetCheck(BST_UNCHECKED);
		m_chkVideoRenderD3D11.SetCheck(BST_UNCHECKED);
		SaveSdkVideoMode(_T("RenderMode"),2);
	}
}

void CLS_AdvLocalSet::OnBnClickedButtonSetLogLevel()
{
	SdkLogLevel tLogLevel = {0};
	int iIndex = m_cboTerminalLevel.GetCurSel();
	int iLevel = m_cboTerminalLevel.GetItemData(iIndex);
	tLogLevel.iTerminalOutputLevel = iLevel;
	tLogLevel.iIsWriteFile = m_chkWriteLog.GetCheck();
	iIndex = m_cboLogfileLevel.GetCurSel();
	iLevel = m_cboLogfileLevel.GetItemData(iIndex);
	tLogLevel.iLogFileWriteLevel = iLevel;
	NetClient_SetSDKInitConfig(INIT_CONFIG_SET_LOG_LEVEL, &tLogLevel, sizeof(tLogLevel));
}

void CLS_AdvLocalSet::OnCbnSelchangeComboHwdecodetype()
{
	int iDecodeType = m_cboHWDecodeType.GetCurSel();
	SetHwDecodeParam(iDecodeType);
}

void CLS_AdvLocalSet::IsShowRenderWindow(int _iCmd)
{
	m_chkVideoRenderDraw.ShowWindow(_iCmd);
	m_chkVideoRenderD3D.ShowWindow(_iCmd);
	m_chkVideoRenderD3D11.ShowWindow(_iCmd);
}
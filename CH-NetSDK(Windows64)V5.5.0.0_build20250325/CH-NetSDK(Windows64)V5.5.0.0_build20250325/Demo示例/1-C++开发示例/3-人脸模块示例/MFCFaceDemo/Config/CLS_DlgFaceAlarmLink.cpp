// CLS_DlgFaceAlarmLink.cpp : Implementation file
//

#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceAlarmLink.h"


// CLS_DlgFaceAlarmLink Dialog

IMPLEMENT_DYNAMIC(CLS_DlgFaceAlarmLink, CLS_PageBase)

CLS_DlgFaceAlarmLink::CLS_DlgFaceAlarmLink(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceAlarmLink::IDD, pParent)
{

}

CLS_DlgFaceAlarmLink::~CLS_DlgFaceAlarmLink()
{
}

void CLS_DlgFaceAlarmLink::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALARMLINK_LIBKEY, m_cboLibKey);
	DDX_Control(pDX, IDC_CBO_ALARMLINK_ALARM_TYPE, m_cboAlarmType);
	DDX_Control(pDX, IDC_CBO_ALARMLINK_VCATYPE, m_cboVcaType);
	DDX_Control(pDX, IDC_CHK_ALARMLINK_OUTPORT1, m_chkAlarmOutPort[0]);
	DDX_Control(pDX, IDC_CHK_ALARMLINK_OUTPORT2, m_chkAlarmOutPort[1]);
	DDX_Control(pDX, IDC_CBO_ALARMLINK_LINKTYPE, m_cboAlarmLinkType);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceAlarmLink, CDialog)
	ON_BN_CLICKED(IDC_BTN_ALARMLINK_SET, &CLS_DlgFaceAlarmLink::OnBnClickedBtnAlarmlinkSet)
	ON_BN_CLICKED(IDC_BTN_ALARMLINK_GET, &CLS_DlgFaceAlarmLink::OnBnClickedBtnAlarmlinkGet)
	ON_BN_CLICKED(IDC_BTN_ALARMLINK_LIBKEY, &CLS_DlgFaceAlarmLink::OnBnClickedBtnAlarmlinkLibkey)
	ON_CBN_SELCHANGE(IDC_CBO_ALARMLINK_ALARM_TYPE, &CLS_DlgFaceAlarmLink::OnCbnSelchangeCboAlarmlinkAlarmType)
END_MESSAGE_MAP()


// CLS_DlgFaceAlarmLink Message Handler


void CLS_DlgFaceAlarmLink::UI_Init()
{
	m_cboAlarmType.ResetContent();
	m_cboAlarmType.SetItemData(m_cboAlarmType.AddString("IPC Face recognition"), ALARM_TYPE_FACE_IDENT);
	m_cboAlarmType.SetItemData(m_cboAlarmType.AddString("NVR Face recognition"), ALARM_TYPE_NVR_VCA);
	m_cboAlarmType.SetCurSel(0);

	m_cboVcaType.ResetContent();
	m_cboVcaType.InsertString(0, "Blacklist");
	m_cboVcaType.InsertString(1, "Whitelist");
	m_cboVcaType.SetCurSel(0);

	m_cboAlarmLinkType.ResetContent();
	m_cboAlarmLinkType.SetItemData(m_cboAlarmLinkType.AddString("Alarm linkage output"), ALARMLINKTYPE_LINKOUTPORT);
	m_cboAlarmLinkType.SetItemData(m_cboAlarmLinkType.AddString("Alarm linkage IPC output"), ALARMLINKTYPE_IPC_OUTPORT);
	m_cboAlarmLinkType.SetCurSel(0);
}

void CLS_DlgFaceAlarmLink::OnBnClickedBtnAlarmlinkLibkey()
{
	QueryLibkey(m_cboLibKey);
}


void CLS_DlgFaceAlarmLink::OnBnClickedBtnAlarmlinkSet()
{
	int iLibKeySel = -1;
	int iLibKey = 0;
	int iAlarmType = 0;
	int iAlarmSubType = 0;

	iAlarmType = GetItemCurData(m_cboAlarmType);
	iAlarmSubType = m_cboVcaType.GetCurSel();

	if ( (ALARM_TYPE_FACE_IDENT ==  iAlarmType && VCA_IPC_EVENT_FACE_IDENT_COMPARE != iAlarmSubType)
		|| (ALARM_TYPE_NVR_VCA == iAlarmType && VCA_NVR_EVENT_FACE_IDENT_COMPARE != iAlarmSubType) )
	{
		iLibKey = 0;//IPC face recognition - except for comparison alarm (blacklist), other subtypes have nothing to do with iLibKey, and the default value is 0
					//NVR face recognition - except for comparison alarm, other sub types have nothing to do with iLibKey, and the default is 0
	}
	else
	{
		iLibKeySel = m_cboLibKey.GetCurSel();
		CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);
		iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);//Face library ID
	}

	//Alarm linkage parameters
	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);

	tAlarmLinkPara.tAlarmParam.iReserved = iAlarmSubType; //When iAlarmType=20, 0-blacklist, 1-whitelist iAlarmType=21, 0-face detection, 1-face recognition comparison alarm, 2-face recognition stranger alarm, 3-face recognition frequency alarm, 4-face recognition detention alarm
	tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = iLibKey; //Key value of face library: libkey
	tAlarmLinkPara.tLinkParam.iLinkType = GetItemCurData(m_cboAlarmLinkType);//Linkage alarm type alarm linkage output ALARMLINKTYPE_LINKOUTPORT
	
	for (int iPortNum = 0; iPortNum<ALARMLINK_OUTPORT_MAXNUM; iPortNum++)
	{
		if (m_chkAlarmOutPort[iPortNum].GetCheck())
		{
			tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[iPortNum/LEN_32] |= 1<<(iPortNum%LEN_32);
		}		
	}
		
	if (strlen(m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID) > 0)
	{
		strncpy_s(tAlarmLinkPara.tAlarmParam.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tAlarmLinkPara.tAlarmParam.cLibUUID));
	}	

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, iAlarmType, CMD_SET_ALARMLINK_V3, &tAlarmLinkPara);	

}

void CLS_DlgFaceAlarmLink::OnBnClickedBtnAlarmlinkGet()
{
	int iLibKeySel = -1;
	int iLibKey = 0;
	int iAlarmType = 0;
	int iAlarmSubType = 0;

	iAlarmType = GetItemCurData(m_cboAlarmType);
	iAlarmSubType = m_cboVcaType.GetCurSel();

	if ( (ALARM_TYPE_FACE_IDENT ==  iAlarmType && VCA_IPC_EVENT_FACE_IDENT_COMPARE != iAlarmSubType)
		|| (ALARM_TYPE_NVR_VCA == iAlarmType && VCA_NVR_EVENT_FACE_IDENT_COMPARE != iAlarmSubType) )
	{
		iLibKey = 0;//IPC face recognition - except for comparison alarm (blacklist), other subtypes have nothing to do with iLibKey, and the default value is 0
					//NVR face recognition - except for comparison alarm, other sub types have nothing to do with iLibKey, and the default is 0
	}
	else
	{
		iLibKeySel = m_cboLibKey.GetCurSel();
		CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);
		iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);//Face library ID
	}

	TAlarmLinkParam_V3 tAlarmLinkPara = {0};
	tAlarmLinkPara.tAlarmParam.iSize = sizeof(TAlarmParam_V3);
	tAlarmLinkPara.tLinkParam.iSize = sizeof(TLinkParam_V3);
	tAlarmLinkPara.tLinkParam.iLinkType = GetItemCurData(m_cboAlarmLinkType);
	tAlarmLinkPara.tAlarmParam.iReserved = iAlarmSubType;//When iAlarmType=20, 0-blacklist, 1-whitelist iAlarmType=21, 0-face detection, 1-face recognition comparison alarm, 2-face recognition stranger alarm, 3-face recognition frequency alarm, 4-face recognition detention alarm
	tAlarmLinkPara.tAlarmParam.iAlarmTypeParam = iLibKey;
	
	if (strlen(m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID) > 0)
	{
		strncpy_s(tAlarmLinkPara.tAlarmParam.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tAlarmLinkPara.tAlarmParam.cLibUUID));
	}

	int iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNo, iAlarmType, CMD_GET_ALARMLINK_V3, &tAlarmLinkPara);	
	if (iRet >= 0)
	{
		for (int iPortNum = 0; iPortNum < ALARMLINK_OUTPORT_MAXNUM; iPortNum++)
		{
			int iCheck = (1<<(iPortNum%LEN_32))&(tAlarmLinkPara.tLinkParam.uLinkParam.iCommonSet[iPortNum/LEN_32])?BST_CHECKED:BST_UNCHECKED;
			m_chkAlarmOutPort[iPortNum].SetCheck(iCheck);
		}
	}
}

void CLS_DlgFaceAlarmLink::OnCbnSelchangeCboAlarmlinkAlarmType()
{
	if(ALARM_TYPE_FACE_IDENT == m_cboAlarmType.GetItemData(m_cboAlarmType.GetCurSel()))
	{
		m_cboVcaType.ResetContent();
		m_cboVcaType.InsertString(0, "Blacklist");
		m_cboVcaType.InsertString(1, "Whitelist");
		m_cboVcaType.SetCurSel(0);
	}
	else
	{
		m_cboVcaType.ResetContent();
		m_cboVcaType.InsertString(0, "Face Detection");
		m_cboVcaType.InsertString(1, "Face recognition - comparison");
		m_cboVcaType.InsertString(2, "Face recognition - stranger");
		m_cboVcaType.InsertString(3, "Face recognition - frequency");
		m_cboVcaType.InsertString(4, "Face recognition - detention");
		m_cboVcaType.SetCurSel(0);
	}	
}

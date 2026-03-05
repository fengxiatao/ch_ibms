
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceAlarm.h"

typedef enum{
	ITEM_ALARM_INDEX = 0,	//Serial No
	ITEM_ALARM_LIBKEY,		//Library key value
	ITEM_ALARM_NAME,		//Library name
	ITEM_ALARM_TYPE,		//Library type
	ITEM_ALARM_PICCNT,		//Picture count
	ITEM_ALARM_MODELCNT,	//Model count
}ITEM_FACE_ALARM_LIB;

#define VCA_ALARM_DISCERN		1		//Comparison alarm
#define VCA_ALARM_STRANGER		2		//Strangers call the police
#define VCA_ALARM_RATEN			3		//Frequency alarm
#define VCA_ALARM_DETENTION		4		//Detention alarm

#define SHOW_SIMILAR			1
#define SHOW_TIME				2
#define SHOW_COUNT				4
#define SHOW_LIBKEY				8
#define SHOW_GROUP				16
#define SHOW_CHAN				32
#define SHOW_UUID				64

#define WIDGET_INSTANCE			10

IMPLEMENT_DYNAMIC(CLS_DlgFaceAlarm, CLS_PageBase)

CLS_DlgFaceAlarm::CLS_DlgFaceAlarm(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceAlarm::IDD, pParent)
{
}

CLS_DlgFaceAlarm::~CLS_DlgFaceAlarm()
{
}

void CLS_DlgFaceAlarm::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ALARM_TYPE,	m_cboAlarmType);
	DDX_Control(pDX, IDC_CBO_ALARM_VCA_TYPE, m_cboVcaType);
	DDX_Control(pDX, IDC_CHK_ALARM_ENABLE, m_chkEnable);
	DDX_Control(pDX, IDC_LST_ALARM_LIB, m_lstAlarmLib);
	DDX_Control(pDX, IDC_SLD_ALARM_SIMILAR, m_sldFaceAlarmSimilar);
	DDX_Control(pDX, IDC_CHK_ALARM_TYPE_ENABLE, m_chkVcaAlarmEnable);
	DDX_Control(pDX, IDC_COMBO_FREQ_GROUP, m_cboGroupNumber);
}

BEGIN_MESSAGE_MAP(CLS_DlgFaceAlarm, CLS_PageBase)
	ON_BN_CLICKED(IDC_BTN_ALARM_SET, &CLS_DlgFaceAlarm::OnBnClickedBtnAlarmSet)
	ON_CBN_SELCHANGE(IDC_CBO_ALARM_VCA_TYPE, &CLS_DlgFaceAlarm::OnCbnSelchangeCboAlarmVcaType)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ALARM_SIMILAR, &CLS_DlgFaceAlarm::OnNMCustomdrawSlider1)
	ON_CBN_SELCHANGE(IDC_COMBO_FREQ_GROUP, &CLS_DlgFaceAlarm::OnCbnSelchangeComboFreqGroup)
END_MESSAGE_MAP()


void CLS_DlgFaceAlarm::UI_Init()
{
	//Recognition status, only set NVR local face recognition
	m_cboAlarmType.ResetContent();
	m_cboAlarmType.SetItemData(m_cboAlarmType.AddString("IPC Face recognition"), ALARM_TYPE_FACE_IDENT);
	m_cboAlarmType.SetItemData(m_cboAlarmType.AddString("NVR Face recognition"), ALARM_TYPE_NVR_VCA);
	m_cboAlarmType.SetCurSel(1);
	m_cboAlarmType.EnableWindow(FALSE);

	//NVR local intelligent analysis type
	m_cboVcaType.ResetContent();
	m_cboVcaType.SetItemData(m_cboVcaType.AddString("Face recognition-comparison alarm"), VCA_ALARM_DISCERN);
	m_cboVcaType.SetItemData(m_cboVcaType.AddString("Face recognition-stranger alarm"), VCA_ALARM_STRANGER);
	m_cboVcaType.SetItemData(m_cboVcaType.AddString("Face recognition-frequency alarm"), VCA_ALARM_RATEN);
	m_cboVcaType.SetItemData(m_cboVcaType.AddString("Face recognition-detention alarm"), VCA_ALARM_DETENTION);
	m_cboVcaType.SetCurSel(0);

	//Face library list
	m_lstAlarmLib.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT|LVS_EX_CHECKBOXES);
	m_lstAlarmLib.InsertColumn(ITEM_ALARM_INDEX, "No.", LVCFMT_LEFT, 40, -1);
	m_lstAlarmLib.InsertColumn(ITEM_ALARM_LIBKEY, "Libkey", LVCFMT_LEFT, 60, -1);
	m_lstAlarmLib.InsertColumn(ITEM_ALARM_NAME, "Library name", LVCFMT_LEFT, 140, -1);
	m_lstAlarmLib.InsertColumn(ITEM_ALARM_TYPE, "Library type.", LVCFMT_LEFT, 40, -1);
	m_lstAlarmLib.InsertColumn(ITEM_ALARM_PICCNT, "Picture count", LVCFMT_LEFT, 140, -1);
	m_lstAlarmLib.InsertColumn(ITEM_ALARM_MODELCNT, "Model count", LVCFMT_LEFT, 40, -1);
}

void CLS_DlgFaceAlarm::UI_UptateData()
{
	OnCbnSelchangeCboAlarmVcaType();
}

void CLS_DlgFaceAlarm::UI_UpdataLibkey()
{
	m_mapLibkey.clear();
	int iMapSize = 0;

	m_lstAlarmLib.DeleteAllItems();

	FaceLibQuery tQuery = {0};
	tQuery.iSize = sizeof(FaceLibQuery);
	tQuery.iPageCount = FACE_MAX_PAGE_COUNT;
	
	while (TRUE)
	{
		FaceLibQueryResult tResult[FACE_MAX_PAGE_COUNT] = {0};
		int iRet = NetClient_FaceConfig(m_iLogonID, FACE_CMD_LIB_QUERY, tQuery.iChanNo, &tQuery, sizeof(FaceLibQuery), &tResult, sizeof(FaceLibQueryResult));
		if (0 != iRet)
		{
			break;
		}

		for (int iIdx = 0; iIdx < FACE_MAX_PAGE_COUNT && iIdx < tResult[0].iPageCount; ++iIdx) 
		{
			FaceLibInfo tInfo = tResult[iIdx].tFaceLib;
			if (tResult[iIdx].tFaceLib.iSize <= 0) 
			{
				break;
			}	
			m_mapLibkey.insert(pair<int, int>(tInfo.iLibKey, iMapSize++));
			int iLstIndex = m_lstAlarmLib.GetItemCount();
			m_lstAlarmLib.InsertItem(iLstIndex, _T(""));
			m_lstAlarmLib.SetCheck(iLstIndex, FALSE);
			m_lstAlarmLib.SetItemData(iLstIndex, tInfo.iLibKey);
			m_lstAlarmLib.SetItemText(iLstIndex, ITEM_ALARM_INDEX, IntToStr(iLstIndex + 1));
			m_lstAlarmLib.SetItemText(iLstIndex, ITEM_ALARM_LIBKEY, IntToStr(tInfo.iLibKey));
			m_lstAlarmLib.SetItemText(iLstIndex, ITEM_ALARM_NAME, tInfo.cName);
			m_lstAlarmLib.SetItemText(iLstIndex, ITEM_ALARM_TYPE, 0 == tInfo.iLibType ? _T("General face library") : _T("Stanger face library"));
			m_lstAlarmLib.SetItemText(iLstIndex, ITEM_ALARM_PICCNT, IntToStr(tInfo.iLibPicCnt));
			m_lstAlarmLib.SetItemText(iLstIndex, ITEM_ALARM_MODELCNT, IntToStr(tInfo.iModelPicCnt));
		}

		int iPageCount = tResult[0].iTotal / FACE_MAX_PAGE_COUNT;
		if (tResult[0].iTotal % FACE_MAX_PAGE_COUNT > 0)
		{
			iPageCount = iPageCount + 1;
		}

		tQuery.iPageNo++;
		if (tQuery.iPageNo >= iPageCount) 
		{
			break;
		}
	}
}

void CLS_DlgFaceAlarm::UI_UpdataAlarmInfo()
{
	int iRetBT = -1;
	
	//Whether face recognition algorithm is enabled
	FaceDetectArithmetic tParam = {0};
	tParam.iBufSize = sizeof(tParam);
	tParam.iSceneID = 0;
	tParam.iDevType = 1;	//0-IPC, 1-NVR
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, &tParam, sizeof(tParam), &iRetBT);
	m_chkEnable.SetCheck(tParam.iDentification == 2 ? BST_CHECKED : BST_UNCHECKED);

	//Alarm parameters
	FaceAlarmParamIn tIn = {sizeof(FaceAlarmParamIn), GetItemCurData(m_cboVcaType), FACE_MAX_KEY_COUNT};
	FaceAlarmParam	tOut[FACE_MAX_KEY_COUNT] = {0};
	iRet = FaceConfig(FACE_CMD_ALARM_PARAM, &tIn, sizeof(tIn), &tOut, sizeof(FaceAlarmParam));
	SetDlgItemInt(IDC_EDT_ALARM_INTERVAL, tOut[0].iParam2);
	SetDlgItemInt(IDC_EDT_ALARM_FREQ, tOut[0].iParam3);
	m_sldFaceAlarmSimilar.SetPos(tOut[0].iSimilar);
	SetDlgItemText(IDC_STC_ALARM_SIMILAR_VALUE, IntToStr(tOut[0].iSimilar));
	m_chkVcaAlarmEnable.SetCheck(tOut[0].iEnable);
	if (1 <= tOut[0].iFreqGroup && m_cboGroupNumber.GetCount() > tOut[0].iFreqGroup)
	{
		m_cboGroupNumber.SetCurSel(tOut[0].iFreqGroup);
		SetDlgItemText(IDC_EDIT_FREQ_CHAN, tOut[0].cFreqChannelList);
		GetDlgItem(IDC_EDIT_FREQ_CHAN)->EnableWindow(TRUE);
	}
	else
	{
		//tOut[0].iFreqGroup小于0或大于16都认是无效的
		m_cboGroupNumber.SetCurSel(0);
		SetDlgItemText(IDC_EDIT_FREQ_CHAN, _T("0"));
		GetDlgItem(IDC_EDIT_FREQ_CHAN)->EnableWindow(FALSE);
	}

	for (int j = 0; j < FACE_MAX_KEY_COUNT; ++j)
	{
		if (tOut[j].iSize <= 0)
		{
			break;
		}
	
		int iLibKey = _ttoi(tOut[j].cLibkey);
		map<int, int>::iterator it = m_mapLibkey.find(iLibKey);
		if (it != m_mapLibkey.end())
		{
			m_lstAlarmLib.SetCheck(it->second, TRUE);
		}		
	}
}

void CLS_DlgFaceAlarm::OnBnClickedBtnAlarmSet()
{
	int iVcaType = GetItemCurData(m_cboVcaType);
	int iAlarmTime = GetDlgItemInt(IDC_EDT_ALARM_INTERVAL);
	int iAlarmCount = GetDlgItemInt(IDC_EDT_ALARM_FREQ);
	if (VCA_ALARM_RATEN == iVcaType) 
	{
		if (iAlarmTime < 1 || iAlarmTime > 86400)
		{
			MessageBox(_T("Time interval input range: 1~86400!"), "Tips", MB_OK);
			return;
		}
		if (iAlarmCount < 1 || iAlarmCount > 99)
		{
			MessageBox(_T("Frequency input range: 1~99!"), "Tips", MB_OK);
			return;
		}
	}
	else if (VCA_ALARM_DETENTION == iVcaType)
	{
		if (iAlarmTime < 1 || iAlarmTime > 86400)
		{
			MessageBox(_T("Time interval input range: 1~86400!"), "Tips", MB_OK);
			return;
		}
	}
	
	int iRetBT = -1;
	//Face recognition enable
	FaceDetectArithmetic tParam = {0};
	tParam.iBufSize = sizeof(tParam);
	tParam.iSceneID = 0;
	tParam.iDevType = 1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, &tParam, sizeof(tParam), &iRetBT);
	if (0 != iRet)
	{
		return;
	}
	tParam.iDevType = 1;
	tParam.iDentification = m_chkEnable.GetCheck() == BST_CHECKED ? 2 : 1;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, &tParam, sizeof(tParam));
	if (0 != iRet)
	{
		return;
	}	

	FaceAlarmParam tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iAlarmType = ALARM_TYPE_NVR_VCA;
	tInfo.iParam1 = iVcaType;
	tInfo.iParam2 = iAlarmTime;
	tInfo.iParam3 = iAlarmCount;
	tInfo.iSimilar = m_sldFaceAlarmSimilar.GetPos();
	tInfo.iDevType = 1;
	tInfo.iEnable = m_chkVcaAlarmEnable.GetCheck();
	tInfo.iRecognition = 2;//Whether the alarm information is uploaded, 1 - do not upload, 2 - upload
	//Each library needs to set its enabling status
	tInfo.iFreqGroup = m_cboGroupNumber.GetCurSel();

	GetDlgItemText(IDC_EDIT_FACE_LIB_UUID, tInfo.cLibUUIDStranger, sizeof(tInfo.cLibUUIDStranger));
	GetDlgItemText(IDC_EDIT_FREQ_CHAN, tInfo.cFreqChannelList, sizeof(tInfo.cFreqChannelList));
	for (int i = 0; i < m_lstAlarmLib.GetItemCount(); ++i)
	{
		tInfo.iLibEnable = m_lstAlarmLib.GetCheck(i);
		strncpy_s(tInfo.cLibkey, IntToStr((int)(m_lstAlarmLib.GetItemData(i))), sizeof(tInfo.cLibkey));
		iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNo, tInfo.iAlarmType, CMD_ALARM_FACE_PARAM, &tInfo);
	}

	MessageBox(_T("Set Success"), "Tips", MB_OK);
}

void CLS_DlgFaceAlarm::OnCbnSelchangeCboAlarmVcaType()
{
	UI_UpdataLibkey();

	UI_UpdataAlarmInfo();

	UI_UpdataWidget();

	UI_UpdataUUID();
}

void CLS_DlgFaceAlarm::OnNMCustomdrawSlider1(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemText(IDC_STC_ALARM_SIMILAR_VALUE, IntToStr(m_sldFaceAlarmSimilar.GetPos()));
	*pResult = 0;
}

void CLS_DlgFaceAlarm::UI_UpdataWidget()
{
	//Hide controls first
	//Similarity
	GetDlgItem(IDC_STC_ALARM_SIMILAR)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_SLD_ALARM_SIMILAR)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STC_ALARM_SIMILAR_VALUE)->ShowWindow(SW_HIDE);
	//time
	GetDlgItem(IDC_STC_ALARM_INTERVAL)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_EDT_ALARM_INTERVAL)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STATIC_ALARM_INTERVAL_UINT)->ShowWindow(SW_HIDE);
	//frequency
	GetDlgItem(IDC_STC_ALARM_FREQ)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_EDT_ALARM_FREQ)->ShowWindow(SW_HIDE);

	//Frequency group
	GetDlgItem(IDC_COMBO_FREQ_GROUP)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STC_FREQ_GROUP)->ShowWindow(SW_HIDE);

	//Freqency of Channel
	GetDlgItem(IDC_EDIT_FREQ_CHAN)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_STC_FREQ_CHAN)->ShowWindow(SW_HIDE);

	GetDlgItem(IDC_STATIC_FACE_LIB_UUID)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_EDIT_FACE_LIB_UUID)->ShowWindow(SW_HIDE);

	//Library List
	m_lstAlarmLib.ShowWindow(SW_HIDE);

	int iShowValue = 0;
	switch (GetItemCurData(m_cboVcaType))
	{
	case VCA_ALARM_DISCERN:		//Compare alarm	
		iShowValue = SHOW_SIMILAR | SHOW_LIBKEY;//Display similarity and library list
		break;
	case VCA_ALARM_STRANGER:	//Strangers call the police
		iShowValue = SHOW_UUID;
		break;
	case VCA_ALARM_RATEN:		//Frequency alarm
		iShowValue = SHOW_SIMILAR | SHOW_TIME | SHOW_COUNT | SHOW_LIBKEY | SHOW_GROUP | SHOW_CHAN;//Show both
		break;
	case VCA_ALARM_DETENTION:	//Detention alarm	
		iShowValue = SHOW_SIMILAR | SHOW_TIME | SHOW_LIBKEY;//Display similarity, time interval and database list
		break;
	default:
		break;
	}

	//Show and move controls
	RECT tRcLast = UI_GetWndClientRect(GetDlgItem(IDC_CBO_ALARM_VCA_TYPE));
	if ((iShowValue&SHOW_SIMILAR) > 0)	//Similarity
	{
		RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_SLD_ALARM_SIMILAR));
		int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
		tRcLast.bottom = tRcTemp.bottom + iOffSet;
		UI_ShowAndMoveWidget(GetDlgItem(IDC_SLD_ALARM_SIMILAR), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STC_ALARM_SIMILAR), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STC_ALARM_SIMILAR_VALUE), iOffSet);
	}
	if ((iShowValue&SHOW_TIME) > 0)		//time interval
	{
		RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_EDT_ALARM_INTERVAL));
		int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
		tRcLast.bottom = tRcTemp.bottom + iOffSet;
		UI_ShowAndMoveWidget(GetDlgItem(IDC_EDT_ALARM_INTERVAL), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STC_ALARM_INTERVAL), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STATIC_ALARM_INTERVAL_UINT), iOffSet);
	}
	if ((iShowValue&SHOW_COUNT) > 0)	//frequency
	{
		RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_EDT_ALARM_FREQ));
		int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
		tRcLast.bottom = tRcTemp.bottom + iOffSet;
		UI_ShowAndMoveWidget(GetDlgItem(IDC_EDT_ALARM_FREQ), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STC_ALARM_FREQ), iOffSet);
	}
	if ((iShowValue&SHOW_GROUP) > 0)		//Frequency group
	{
		RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_COMBO_FREQ_GROUP));
		int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
		tRcLast.bottom = tRcTemp.bottom + iOffSet;
		UI_ShowAndMoveWidget(GetDlgItem(IDC_COMBO_FREQ_GROUP), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STC_FREQ_GROUP), iOffSet);
	}
	if ((iShowValue&SHOW_CHAN) > 0)		//Freqency of Channel
	{
		RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_EDIT_FREQ_CHAN));
		int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
		tRcLast.bottom = tRcTemp.bottom + iOffSet;
		UI_ShowAndMoveWidget(GetDlgItem(IDC_EDIT_FREQ_CHAN), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STC_FREQ_CHAN), iOffSet);
	}
	if ((iShowValue&SHOW_LIBKEY) > 0)	//Library List
	{
		RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_LST_ALARM_LIB));
		int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
		tRcLast.bottom = tRcTemp.bottom + iOffSet;
		UI_ShowAndMoveWidget(GetDlgItem(IDC_LST_ALARM_LIB), iOffSet);
	}
	if ((iShowValue&SHOW_UUID) > 0)
	{
		RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_STATIC_FACE_LIB_UUID));
		int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
		tRcLast.bottom = tRcTemp.bottom + iOffSet;
		UI_ShowAndMoveWidget(GetDlgItem(IDC_STATIC_FACE_LIB_UUID), iOffSet);
		UI_ShowAndMoveWidget(GetDlgItem(IDC_EDIT_FACE_LIB_UUID), iOffSet);
	}
	//Set button
	RECT tRcTemp = UI_GetWndClientRect(GetDlgItem(IDC_BTN_ALARM_SET));
	int iOffSet = (tRcLast.bottom + WIDGET_INSTANCE) - tRcTemp.top;
	tRcLast.bottom = tRcTemp.bottom + iOffSet;
	UI_ShowAndMoveWidget(GetDlgItem(IDC_BTN_ALARM_SET), iOffSet);
}

void CLS_DlgFaceAlarm::UI_ShowAndMoveWidget(CWnd* _pWnd, int _iOffset)
{
	if (NULL == _pWnd)
	{
		return;
	}
	_pWnd->ShowWindow(SW_SHOW);
	RECT tRcTemp = {0};
	_pWnd->GetWindowRect(&tRcTemp);
	ScreenToClient(&tRcTemp);
	tRcTemp.top += _iOffset;
	tRcTemp.bottom += _iOffset;
	_pWnd->MoveWindow(&tRcTemp);
}

RECT CLS_DlgFaceAlarm::UI_GetWndClientRect(CWnd* _pWnd)
{
	RECT rc = {0};
	if (NULL == _pWnd)
	{
		return rc;
	}
	_pWnd->GetWindowRect(&rc);
	ScreenToClient(&rc);
	return rc;
}

void CLS_DlgFaceAlarm::OnCbnSelchangeComboFreqGroup()
{
	// TODO: 在此添加控件通知处理程序代码
	if (0 == m_cboGroupNumber.GetCurSel())
	{
		//组号0时关联通道号无效，强制置0
		SetDlgItemText(IDC_EDIT_FREQ_CHAN, _T("0"));
		GetDlgItem(IDC_EDIT_FREQ_CHAN)->EnableWindow(FALSE);
	}
	else
	{
		GetDlgItem(IDC_EDIT_FREQ_CHAN)->EnableWindow(TRUE);
	}
}

void CLS_DlgFaceAlarm::UI_UpdataUUID()
{
	FaceAlarmParam tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iAlarmType = ALARM_TYPE_NVR_VCA;
	tInfo.iParam1 = GetItemCurData(m_cboVcaType);
	int iRet = NetClient_GetAlarmConfig( m_iLogonID,m_iChannelNo,tInfo.iAlarmType, CMD_ALARM_FACE_PARAM, &tInfo);
	if (0 != iRet)
	{
		return;
	}
	SetDlgItemText(IDC_EDIT_FACE_LIB_UUID, tInfo.cLibUUIDStranger);
}
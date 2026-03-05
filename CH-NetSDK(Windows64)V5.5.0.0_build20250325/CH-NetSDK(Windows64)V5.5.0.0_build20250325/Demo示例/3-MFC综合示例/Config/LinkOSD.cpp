
#include "stdafx.h"
#include "NetClientDemo.h"
#include "LinkOSD.h"
#include "../Common/CommonFun.h"
#include <Shlwapi.h>
// CLS_LinkOSD dialog

IMPLEMENT_DYNAMIC(CLS_LinkOSD, CDialog)

CLS_LinkOSD::CLS_LinkOSD(CWnd* pParent /*=NULL*/)
	: CDialog(CLS_LinkOSD::IDD, pParent)
{

}

CLS_LinkOSD::~CLS_LinkOSD()
{
}

void CLS_LinkOSD::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDT_DEV_IP, m_edtDevIP);
	DDX_Control(pDX, IDC_CBO_OSD_CH, m_cboOSDCH);
	DDX_Control(pDX, IDC_CBO_OSD_AREA, m_cboOSDArea);
	DDX_Control(pDX, IDC_CBO_OSD_COLOR, m_cboOSDColor);
	DDX_Control(pDX, IDC_CHK_ALARM_INFO, m_chkAlarmInfo);
	DDX_Control(pDX, IDC_CHK_ALARM_TIME, m_cboAlarmTime);
	DDX_Control(pDX, IDC_CHK_CUSTOM, m_chkUserDefine);
	DDX_Control(pDX, IDC_EDT_CUSTOM, m_edtUserDefine);
	DDX_Text(pDX,IDC_EDT_DEV_IP,m_cstrDevIP);
	DDX_Text(pDX,IDC_EDT_CUSTOM,m_cstrUserDefine);
}


BEGIN_MESSAGE_MAP(CLS_LinkOSD, CDialog)
END_MESSAGE_MAP()


// CLS_LinkOSD message handler

BOOL CLS_LinkOSD::OnInitDialog()
{
	CDialog::OnInitDialog();

	SetDlgItemTextEx(IDC_STC_DEV_IP, IDS_CONFIG_LINK_IP);
	SetDlgItemTextEx(IDC_STC_OSD_CH, IDS_CONFIG_LINK_OSD_CH);
	SetDlgItemTextEx(IDC_STC_OSD_AREA, IDS_CONFIG_LINK_OSD_AREA);
	SetDlgItemTextEx(IDC_STC_OSD_COLOR, IDS_CONFIG_LINK_OSD_COLOR);
	SetDlgItemTextEx(IDC_STC_OSD_INFO, IDS_CONFIG_LINK_OSD_INFO);
	SetDlgItemTextEx(IDC_CHK_ALARM_INFO, IDS_CONFIG_LINK_ALARM_INFO);
	SetDlgItemTextEx(IDC_CHK_ALARM_TIME, IDS_CONFIG_LINK_ALARM_TIME);
	SetDlgItemTextEx(IDC_CHK_CUSTOM, IDS_CONFIG_LINK_USERDEFINE);
	
	InitDlgData();

	return TRUE;
}

void CLS_LinkOSD::InitDlgData()
{
	//Initialize the overlay channel
	for (int i = 0; i < MAX_OSD_CHANNELNUM; i++)
	{
		CString cstrNo = "";
		cstrNo.Format("%d",i+i);
		m_cboOSDCH.InsertString(i,cstrNo);
	}
	m_cboOSDCH.SetCurSel(0);
	//Initialize the overlay area
	for (int i = 0; i < MAX_OSD_BLOCKNUM; i++)
	{
		CString cstrNo = "";
		cstrNo.Format("%d",i+i);
		m_cboOSDArea.InsertString(i,cstrNo);
	}
	m_cboOSDArea.SetCurSel(0);
	//Initialize font color
	CString cstrOsdSize;
	int iColString[MAX_COLOUR_NUM] = {IDS_VCA_COL_WHITE,
		IDS_VCA_COL_BLACK,IDS_VCA_COL_RED,IDS_VCA_COL_BLUE,
		IDS_VCA_COL_GREEN};
	for (int i = 0; i < MAX_COLOUR_NUM; i++)
	{
		cstrOsdSize.LoadString(iColString[i]);
		m_cboOSDColor.AddString(cstrOsdSize);
	}
	m_cboOSDColor.SetCurSel(0);
}

void CLS_LinkOSD::SetDlgInfo(AlarmInLink* _pAlarmInLink)
{
	UpdateData(TRUE);


	//_pAlarmInLink->iDisappearType = m_cboLinkOsdRecover.GetItemData(m_cboLinkOsdRecover.GetCurSel());

	CString cstrRecoveryParam = "";
	m_cboAlarmTime.GetWindowText(cstrRecoveryParam);
	if (cstrRecoveryParam != "")
	{
		_pAlarmInLink->iDisappearTime = StrToInt(cstrRecoveryParam);
	}
	else
	{
		_pAlarmInLink->iDisappearTime = -1;
	}


	ParamData tData = {0};

	//IP address
	CString cstrIP; 
	m_edtDevIP.GetWindowText(cstrIP);
	if (cstrIP == "")
	{
		return;
	}
	strcpy_s(tData.cIP, cstrIP);

	//overlay channel
	CString cstrChannel; 
	m_cboOSDCH.GetWindowText(cstrChannel);
	tData.iChannelNo = StrToInt(cstrChannel);

	//overlay area
	CString cstrZone; 
	m_cboOSDArea.GetWindowText(cstrZone);
	tData.iAreaNo = StrToInt(cstrZone);

	//font color
	int iColor; 
	iColor = m_cboOSDColor.GetCurSel() + 1;
	tData.iColor = iColor;

	//Overlay alarm information
	int iAlarmInfo; 
	if (m_chkAlarmInfo.GetCheck())
	{
		iAlarmInfo = 1;
	} 
	else
	{
		iAlarmInfo = 0;
	}
	tData.iAlarmInfo = iAlarmInfo;

	//Overlay overlay alarm time
	int iAlarmTime; 
	if (m_cboAlarmTime.GetCheck())
	{
		iAlarmTime = 1;
	} 
	else
	{
		iAlarmTime = 0;
	}
	tData.iAlarmTime = iAlarmTime;

	// custom selection
	int iText; 
	if (m_chkUserDefine.GetCheck())
	{
		iText = 1;
	} 
	else
	{
		iText = 0;
	}
	tData.iAlarmCustom = iText;

	//custom content
	CString strTextInfo; 
	m_edtUserDefine.SetWindowText(strTextInfo);
	strcpy_s(tData.cText, strTextInfo);

	CString cstrParam;
	StructToString(&tData, cstrParam);

	strcpy_s(_pAlarmInLink->cParam, (LPSTR)(LPCTSTR)cstrParam);
	//device IP address
	//m_cstrDevIP
	//overlay channel
	//m_cboOSDCH
	//overlay area
	//m_cboOSDArea
	//font color
	//m_cboOSDColor
	//overlay information
	//
	m_chkAlarmInfo.SetCheck(TRUE);
	m_cboAlarmTime.SetCheck(TRUE);
	m_chkUserDefine.SetCheck(TRUE);
	if (m_chkUserDefine)
	{
		m_cstrUserDefine = "";
	}

	UpdateData(FALSE);
}

void CLS_LinkOSD::GetDlgInfo(AlarmInLink* _pAlarmInLink)
{
	UpdateData(TRUE);

	//Linkage character overlay information
	LPCTSTR lpszSource = _pAlarmInLink->cParam;
	CString cstrSource = lpszSource;
	ParamData tData = {0};
	StringToStruct(cstrSource, &tData);

	//IP address
	CString cstrIP;
	cstrIP.Format(_T("%s"), tData.cIP);
	m_edtDevIP.SetWindowText(cstrIP);

	//overlay channel
	int iChannel = tData.iChannelNo;
	int iChannelIndex = m_cboOSDCH.FindString(0, (IntToStr(iChannel)).c_str());	
	m_cboOSDCH.SetCurSel(iChannelIndex);

	//overlay area
	int iZone = tData.iAreaNo;
	int iZoneIndex = m_cboOSDArea.FindString(0, (IntToStr(iZone)).c_str());	
	m_cboOSDArea.SetCurSel(iZoneIndex);

	//font color
	int iColor = tData.iColor;
	m_cboOSDColor.SetCurSel(iColor - 1);


	//Overlay alarm information
	int iAlarmInfo = tData.iAlarmInfo;
	if (iAlarmInfo == 1)
	{
		m_chkAlarmInfo.SetCheck(BST_CHECKED);
	} 
	else
	{
		m_chkAlarmInfo.SetCheck(BST_UNCHECKED);
	}

	//Overlay overlay alarm time
	int iAlarmTime = tData.iAlarmTime; 
	if (iAlarmTime == 1)
	{
		m_cboAlarmTime.SetCheck(BST_CHECKED);
	} 
	else
	{
		m_cboAlarmTime.SetCheck(BST_UNCHECKED);
	}


	// custom selection
	int iText = tData.iAlarmCustom;
	if (iText == 1)
	{
		m_chkUserDefine.SetCheck(BST_CHECKED);
	} 
	else
	{
		m_chkUserDefine.SetCheck(BST_UNCHECKED);
	}	
	//OnBnClickedChkUserDefine();

	//custom content
	CString cstrTextInfo;
	cstrTextInfo.Format(_T("%s"), tData.cText); 
	m_edtUserDefine.SetWindowText(cstrTextInfo);	
	//device IP address
	//m_cstrDevIP
	//overlay channel
	//m_cboOSDCH
	//overlay area
	//m_cboOSDArea
	//font color
	//m_cboOSDColor
	//overlay information
	//
	m_chkAlarmInfo.GetCheck();
	m_cboAlarmTime.GetCheck();
	m_chkUserDefine.GetCheck();
	if (m_chkUserDefine)
	{
		m_cstrUserDefine = "";
	}

	UpdateData(FALSE);
}

BOOL CLS_LinkOSD::StringToStruct( CString _strSrc,ParamData* _pDest )
{
	if (NULL == _pDest)
	{
		return FALSE;
	}

// 	CLS_TString tData;
// 	tData.Split(_strSrc);
// 	strcpy_s(_pDest->cIP,(LPSTR)(LPCTSTR)tData.GetValue(_T("ip")));
// 	_pDest->iChannelNo = tData.GetIntValue(_T("channel"));
// 	_pDest->iAreaNo = tData.GetIntValue(_T("area"));
// 	_pDest->iColor = tData.GetIntValue(_T("color"));
// 	_pDest->iAlarmInfo = tData.GetIntValue(_T("info"));
// 	_pDest->iAlarmTime = tData.GetIntValue(_T("time"));
// 	_pDest->iAlarmCustom = tData.GetIntValue(_T("custom"));
// 	strcpy_s(_pDest->cText,(LPSTR)(LPCTSTR)tData.GetValue(_T("text")));

	return TRUE;
}

BOOL CLS_LinkOSD::StructToString(ParamData* _pSrc,CString& _strDest)
{
	if (NULL == _pSrc)
	{
		return FALSE;
	}

// 	CLS_TString tData;
// 	tData.Append(_T("ip"),_pSrc->cIP);
// 	tData.Append(_T("channel"),_pSrc->iChannelNo);
// 	tData.Append(_T("area"),_pSrc->iAreaNo);
// 	tData.Append(_T("color"),_pSrc->iColor);
// 	tData.Append(_T("info"),_pSrc->iAlarmInfo);
// 	tData.Append(_T("time"),_pSrc->iAlarmTime);
// 	tData.Append(_T("custom"),_pSrc->iAlarmCustom);
// 	tData.Append(_T("text"),_pSrc->cText);
// 	tData.Combine(_strDest);

	return TRUE;
}

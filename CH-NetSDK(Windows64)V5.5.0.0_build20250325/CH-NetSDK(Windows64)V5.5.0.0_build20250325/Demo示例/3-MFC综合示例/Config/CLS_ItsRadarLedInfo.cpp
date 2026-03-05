// CLS_ItsRadarLedInfo.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_ItsRadarLedInfo.h"



typedef enum{
	ITEM_LED_INDEX = 0,					//serial number
	ITEM_LED_NO,						//Numbering
	ITEM_LED_NAME,						//prompt screen name
}ITEM_LIST_LED;


// CLS_ItsRadarLedInfo dialog

IMPLEMENT_DYNAMIC(CLS_ItsRadarLedInfo, CDialog)

CLS_ItsRadarLedInfo::CLS_ItsRadarLedInfo(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ItsRadarLedInfo::IDD, pParent)
{
	memset(&m_tLedResult,0,sizeof(m_tLedResult));
	memset(&m_tLedOsdResult,0,sizeof(m_tLedOsdResult));
}

CLS_ItsRadarLedInfo::~CLS_ItsRadarLedInfo()
{
}

void CLS_ItsRadarLedInfo::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_RADAR_LED_TYPE, m_cboLedType);
	DDX_Control(pDX, IDC_COMBO_RADAR_LED_MODEL, m_cboLedModel);
	DDX_Control(pDX, IDC_COMBO_RADAR_LEDOSD_HINT_TYPE, m_cboOsdHintType);
	DDX_Control(pDX, IDC_LIST_RADAR_LED, m_lstLed);
	DDX_Control(pDX, IDC_LIST_RADAR_LEDOSD, m_lstLedOsd);
	DDX_Control(pDX, IDC_COMBO_RADAR_LEDOSD_FOUNT_SIZE, m_cboFountSize);
	DDX_Control(pDX, IDC_COMBO_RADAR_LEDOSD_FOUNT_COLOR, m_cboFountColor);
	DDX_Control(pDX, IDC_EDIT_RADAR_LEDOSD_FAST_INSDERT_CONTENT, m_edtFastInsert);
}


BEGIN_MESSAGE_MAP(CLS_ItsRadarLedInfo, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LED_QUERY, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedQuery)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LED_ADD, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedAdd)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LED_EDIT, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedEdit)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LED_DEL, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedDel)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LED_TEST, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedTest)
	ON_NOTIFY(NM_CLICK, IDC_LIST_RADAR_LED, &CLS_ItsRadarLedInfo::OnNMClickListRadarLed)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LEDOSD_QUERY, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdQuery)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LEDOSD_ADD, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdAdd)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LEDOSD_EDIT, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdEdit)
	ON_BN_CLICKED(IDC_BUTTON_RADAR_LEDOSD_DEL, &CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdDel)
	ON_NOTIFY(NM_CLICK, IDC_LIST_RADAR_LEDOSD, &CLS_ItsRadarLedInfo::OnNMClickListRadarLedosd)
END_MESSAGE_MAP()


// CLS_ItsRadarLedInfo message handler
BOOL CLS_ItsRadarLedInfo::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}
void CLS_ItsRadarLedInfo::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}

}

void CLS_ItsRadarLedInfo::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if(_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}

	UpdatePageUI();
}

void CLS_ItsRadarLedInfo::OnLanguageChanged(int _iLanguage)
{	
	UpdateUIText();
	UpdatePageUI();
}

void CLS_ItsRadarLedInfo::UpdateUIText()
{
	m_lstLed.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstLed.InsertColumn(ITEM_LED_INDEX, "serial number", LVCFMT_LEFT, 40, -1);
	m_lstLed.InsertColumn(ITEM_LED_NO, "Numbering", LVCFMT_LEFT, 80, -1);
	m_lstLed.InsertColumn(ITEM_LED_NAME, "prompt screen name", LVCFMT_LEFT, 160, -1);	

	m_cboFountSize.ResetContent();
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("16*16"), 16);
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("24*24"), 24);
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("26*26"), 26);
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("28*28"), 28);
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("30*30"), 30);
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("32*32"), 32);
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("48*48"), 48);
	m_cboFountSize.SetItemData(m_cboFountSize.AddString("64*64"), 64);
	m_cboFountSize.SetCurSel(0);

	m_lstLedOsd.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstLedOsd.InsertColumn(ITEM_LED_INDEX, "serial number", LVCFMT_LEFT, 40, -1);
	m_lstLedOsd.InsertColumn(ITEM_LED_NO, "Numbering", LVCFMT_LEFT, 80, -1);
	m_lstLedOsd.InsertColumn(ITEM_LED_NAME, "prompt screen name", LVCFMT_LEFT, 160, -1);	

}

void CLS_ItsRadarLedInfo::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_ItsRadarInfo::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}

	DataDicionaryItem tInofIn = {0};
	tInofIn.iSize = sizeof(tInofIn);
	tInofIn.iFuncType = DEVICE_MODE;//15 Device model

	
	DataDicionaryItem tInfoOut[MAX_DATA_PIC_ITEM] = {0};

	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_DATA_DIC_ITEM, m_iChannelNo, &tInofIn, sizeof(tInofIn), &tInfoOut, sizeof(DataDicionaryItem));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","UpdateLedList failed.iFuncType=%d",tInofIn.iFuncType);
	}
	else
	{
		m_cboLedModel.ResetContent();
		for(int i = 0; i < tInfoOut->iTotal && i < MAX_DATA_PIC_ITEM; ++i)
		{
			m_cboLedModel.SetItemData(m_cboLedModel.AddString(tInfoOut[i].cParam), tInfoOut[i].iFuncTypeValueNo);
		}
		m_cboLedModel.SetCurSel(0);
	}

	tInofIn.iFuncType = LED_INSERT_LABEL_ITEM;//16 Led character overlay to quickly insert label items
	memset(&tInfoOut, 0, sizeof(tInfoOut));

	iRet = NetClient_CmdConfig(m_iLogonID, CMD_DATA_DIC_ITEM, m_iChannelNo, &tInofIn, sizeof(tInofIn), &tInfoOut, sizeof(DataDicionaryItem));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","UpdateLedList failed.iFuncType=%d",tInofIn.iFuncType);
	}
	else
	{
		m_edtFastInsert.Clear();
		CString szPointBuf;
		for(int i = 0; i < tInfoOut->iTotal && i < MAX_DATA_PIC_ITEM; ++i)
		{
			CString tmpStr;
			tmpStr.Format("(%d,%s)", tInfoOut[i].iFuncTypeValueNo,  tInfoOut[i].cParam);
			szPointBuf += tmpStr;
		}
		m_edtFastInsert.SetWindowText(szPointBuf);
	}

	tInofIn.iFuncType = LED_DEV_MODE;//17 Led device type
	memset(&tInfoOut, 0, sizeof(tInfoOut));

	iRet = NetClient_CmdConfig(m_iLogonID, CMD_DATA_DIC_ITEM, m_iChannelNo, &tInofIn, sizeof(tInofIn), &tInfoOut, sizeof(DataDicionaryItem));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","UpdateLedList failed.iFuncType=%d",tInofIn.iFuncType);
	}
	else
	{
		m_cboLedType.ResetContent();
		for(int i = 0; i < tInfoOut->iTotal && i < MAX_DATA_PIC_ITEM; ++i)
		{
			m_cboLedType.SetItemData(m_cboLedType.AddString(tInfoOut[i].cParam), tInfoOut[i].iFuncTypeValueNo);
		}
		m_cboLedType.SetCurSel(0);
	}

	tInofIn.iFuncType = LED_HINT_MODE;//18 Hint mode;
	memset(&tInfoOut, 0, sizeof(tInfoOut));

	iRet = NetClient_CmdConfig(m_iLogonID, CMD_DATA_DIC_ITEM, m_iChannelNo, &tInofIn, sizeof(tInofIn), &tInfoOut, sizeof(DataDicionaryItem));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","UpdateLedList failed.iFuncType=%d",tInofIn.iFuncType);
	}
	else
	{
		m_cboOsdHintType.ResetContent();
		for(int i = 0; i < tInfoOut->iTotal && i < MAX_DATA_PIC_ITEM; ++i)
		{
			m_cboOsdHintType.SetItemData(m_cboOsdHintType.AddString(tInfoOut[i].cParam), tInfoOut[i].iFuncTypeValueNo);
		}
		m_cboOsdHintType.SetCurSel(0);

	}


	tInofIn.iFuncType = OSD_COLOR;//23 OSD font color;
	memset(&tInfoOut, 0, sizeof(tInfoOut));

	iRet = NetClient_CmdConfig(m_iLogonID, CMD_DATA_DIC_ITEM, m_iChannelNo, &tInofIn, sizeof(tInofIn), &tInfoOut, sizeof(DataDicionaryItem));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","UpdateLedList failed.iFuncType=%d",tInofIn.iFuncType);
	}
	else
	{
		m_cboFountColor.ResetContent();
		for(int i = 0; i < tInfoOut->iTotal && i < MAX_DATA_PIC_ITEM; ++i)
		{
			m_cboFountColor.SetItemData(m_cboFountColor.AddString(tInfoOut[i].cParam), tInfoOut[i].iFuncTypeValueNo);
		}
		m_cboFountColor.SetCurSel(0);
	}

	return;
} 

int CLS_ItsRadarLedInfo::UpdateLedList()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "UpdateLedList::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return RET_FAILED;
	}

	m_lstLed.DeleteAllItems();

	LedDevParamOpt tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iActionType = LED_ACTION_TYPE_QUERY;

	//LedDevParamResult tLedResult[LED_DEVICE_MAX_NUM] = {0};
	 memset(&m_tLedResult, 0, sizeof(m_tLedResult));
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_LED_DEV_PARA_QUERY, m_iChannelNo, &tInfo, sizeof(tInfo), &m_tLedResult, sizeof(LedDevParamResult));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","UpdateLedList failed.");
		return iRet;
	}

	//update data in list
	int iCount = m_tLedResult[0].iTotal;
	for (int i = 0; i < iCount && i < LED_DEVICE_MAX_NUM; i++)
	{
		int iItem = m_lstLed.GetItemCount();
		m_lstLed.InsertItem(iItem, "");

		LedDevParamResult tResult = m_tLedResult[i];
		m_lstLed.SetItemText(iItem, ITEM_LED_INDEX, IntToCString(i + 1));
		m_lstLed.SetItemText(iItem, ITEM_LED_NO, tResult.tLedDevInfo.cLedDevNo);
		m_lstLed.SetItemText(iItem, ITEM_LED_NAME, tResult.tLedDevInfo.cLedDevName);
	}	

	return iRet;
}

int CLS_ItsRadarLedInfo::UpdateLedUI(int _iIndex)
{
	if (_iIndex < 0 || _iIndex>=LED_DEVICE_MAX_NUM)
	{
		return RET_FAILED;
	}

	LedDevParamResult tResult = m_tLedResult[_iIndex];
	if (tResult.iSize == 0)
	{
		return RET_FAILED;
	}

	SetDlgItemText(IDC_EDIT_RADAR_LED_NO, tResult.tLedDevInfo.cLedDevNo);
	SetDlgItemText(IDC_EDIT_RADAR_LED_NAME, tResult.tLedDevInfo.cLedDevName);

	SetDlgItemInt(IDC_EDIT_RADAR_LED_WIDTH, tResult.tLedDevInfo.iLedDevWidth);
	SetDlgItemInt(IDC_EDIT_RADAR_LED_HEIGHT, tResult.tLedDevInfo.iLedDevHeight);

	SetDlgItemText(IDC_EDIT_RADAR_LED_IP, tResult.tLedDevInfo.cIpAddr);
	SetDlgItemInt(IDC_EDIT_RADAR_LED_PORT, tResult.tLedDevInfo.iPort);

	//Link to update the corresponding content of the OSD
	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NO, tResult.tLedDevInfo.cLedDevNo);
	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NAME, tResult.tLedDevInfo.cLedDevName);
	
	CString csLedSize;
	csLedSize.Format("%d*%d",tResult.tLedDevInfo.iLedDevWidth,tResult.tLedDevInfo.iLedDevHeight);
	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_SIZE, csLedSize);
	return RET_SUCCESS;
}

int CLS_ItsRadarLedInfo::LedOpt(int _iOptType)
{
	LedDevParamOpt tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iActionType = _iOptType;

	CString cstrValue;
	GetDlgItemText(IDC_EDIT_RADAR_LED_NO, cstrValue);
	strncpy_s(tInfo.tLedDevInfo.cLedDevNo, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.tLedDevInfo.cLedDevNo), cstrValue.GetLength()));

	GetDlgItemText(IDC_EDIT_RADAR_LED_NAME, cstrValue);
	strncpy_s(tInfo.tLedDevInfo.cLedDevName, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.tLedDevInfo.cLedDevName), cstrValue.GetLength()));

	tInfo.tLedDevInfo.iLedDevWidth = GetDlgItemInt(IDC_EDIT_RADAR_LED_WIDTH);
	tInfo.tLedDevInfo.iLedDevHeight = GetDlgItemInt(IDC_EDIT_RADAR_LED_HEIGHT);

	GetDlgItemText(IDC_EDIT_RADAR_LED_IP, cstrValue);
	strncpy_s(tInfo.tLedDevInfo.cIpAddr, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.tLedDevInfo.cIpAddr), cstrValue.GetLength()));

	tInfo.tLedDevInfo.iPort = GetDlgItemInt(IDC_EDIT_RADAR_LED_PORT);

	tInfo.tLedDevInfo.iLedDevType = m_cboLedType.GetItemData(m_cboLedType.GetCurSel());
	tInfo.tLedDevInfo.iLedDevModel = m_cboLedModel.GetItemData(m_cboLedModel.GetCurSel());

	LedDevReply tReply = {0};
	tReply.iSize = sizeof(tReply);

	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_LED_DEV_PARA_OPT, m_iChannelNo, &tInfo, sizeof(tInfo), &tReply, sizeof(tReply));
	if (RET_SUCCESS == iRet && RET_SUCCESS == tReply.iActionResult)
	{
		UpdateLedList();
		return RET_SUCCESS;
	}
	else
	{
		UpdateLedList();
		return RET_FAILED;
	}
}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedQuery()
{
	UpdateLedList();
}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedAdd()
{
	if (RET_SUCCESS == LedOpt(LED_ACTION_TYPE_ADD))
	{
		MessageBox(GetTextByLan("ÃÌº”≥…π¶!","Success!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	else
	{
		MessageBox(GetTextByLan("ÃÌº” ß∞‹!","Failed!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedEdit()
{
	if (RET_SUCCESS == LedOpt(LED_ACTION_TYPE_EDIT))
	{
		MessageBox(GetTextByLan("±‡º≠≥…π¶!","Success!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	else
	{
		MessageBox(GetTextByLan("±‡º≠ ß∞‹!","Failed!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedDel()
{
	if (RET_SUCCESS == LedOpt(LED_ACTION_TYPE_DEL))
	{
		MessageBox(GetTextByLan("…æ≥˝≥…π¶!","Success!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	else
	{
		MessageBox(GetTextByLan("…æ≥˝ ß∞‹!","Failed!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedTest()
{
	if (RET_SUCCESS == LedOpt(LED_ACTION_TYPE_TEST))
	{
		MessageBox(GetTextByLan("≤‚ ‘≥…π¶!","Success!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	else
	{
		MessageBox(GetTextByLan("≤‚ ‘ ß∞‹!","Failed!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
}

void CLS_ItsRadarLedInfo::OnNMClickListRadarLed(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	POSITION pPos = m_lstLed.GetFirstSelectedItemPosition();
	if (NULL != pPos)
	{
		int nItem = m_lstLed.GetNextSelectedItem(pPos);
		int iIndex = atoi(m_lstLed.GetItemText(nItem, ITEM_LED_INDEX));

		UpdateLedUI(iIndex-1);
		UpdateLedOsdUI(iIndex-1);
	}
	*pResult = 0;
}



int CLS_ItsRadarLedInfo::UpdateLedOsdList()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "UpdateLedList::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return RET_FAILED;
	}

	m_lstLedOsd.DeleteAllItems();

	LedDevOsdInfo tInfo = {0};
	tInfo.iSize = sizeof(tInfo);

	memset(&m_tLedOsdResult, 0, sizeof(m_tLedOsdResult));
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_LED_DEV_OSD_PARA_LIST, m_iChannelNo, &tInfo, sizeof(tInfo), &m_tLedOsdResult, sizeof(LedDevOsdParamList));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","UpdateLedOsdList failed.");
		return iRet;
	}

	//update data in list
	int iCount = m_tLedOsdResult[0].iTotal;
	for (int i = 0; i < iCount && i < LED_DEVICE_MAX_NUM; i++)
	{
		int iItem = m_lstLedOsd.GetItemCount();
		m_lstLedOsd.InsertItem(iItem, "");

		LedDevOsdParamList tResult = m_tLedOsdResult[i];
		m_lstLedOsd.SetItemText(iItem, ITEM_LED_INDEX, IntToCString(i + 1));
		m_lstLedOsd.SetItemText(iItem, ITEM_LED_NO, tResult.tLedDevOsdInfo.cLedDevNo);
		m_lstLedOsd.SetItemText(iItem, ITEM_LED_NAME, tResult.tLedDevOsdInfo.cledDevName);
	}	

	return iRet;
}

int CLS_ItsRadarLedInfo::UpdateLedOsdUI(int _iIndex)
{
	if (_iIndex < 0 || _iIndex>=LED_DEVICE_MAX_NUM)
	{
		return RET_FAILED;
	}

	LedDevOsdParamList tResult = m_tLedOsdResult[_iIndex];
	if (tResult.iSize == 0)
	{
		return RET_FAILED;
	}

	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NO, tResult.tLedDevOsdInfo.cLedDevNo);
	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NAME, tResult.tLedDevOsdInfo.cledDevName);
	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_SIZE, tResult.tLedDevOsdInfo.cLedSize);
	
	for(int i =0;i<m_cboFountSize.GetCount()&&i<9;i++)
	{
		if (tResult.tLedDevOsdInfo.iFontWidth == m_cboFountSize.GetItemData(i))
		{
			m_cboFountSize.SetCurSel(i);
			break;
		}
	}

	for(int i =0;i<m_cboFountColor.GetCount()&&i<4;i++)
	{
		if (tResult.tLedDevOsdInfo.iFontColor == m_cboFountColor.GetItemData(i))
		{
			m_cboFountColor.SetCurSel(i);
			break;
		}
	}

	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_CAR_TEXT, tResult.tLedDevOsdInfo.tHintInfo[0].cDisplayInfo);
	SetDlgItemInt(IDC_EDIT_RADAR_LEDOSD_CAR_TIME, tResult.tLedDevOsdInfo.tHintInfo[0].iHintTime);
	SetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NOCAR_TEXT, tResult.tLedDevOsdInfo.tHintInfo[1].cDisplayInfo);

	return RET_SUCCESS;
}

int CLS_ItsRadarLedInfo::LedOsdOpt(int _iOptType)
{
	LedDevOsdInfo tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iActionType = _iOptType;

	CString cstrValue;
	GetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NO, cstrValue);
	strncpy_s(tInfo.cLedDevNo, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.cLedDevNo), cstrValue.GetLength()));

	GetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NAME, cstrValue);
	strncpy_s(tInfo.cledDevName, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.cledDevName), cstrValue.GetLength()));

	GetDlgItemText(IDC_EDIT_RADAR_LEDOSD_SIZE, cstrValue);
	strncpy_s(tInfo.cLedSize, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.cLedSize), cstrValue.GetLength()));

	tInfo.iFontWidth = m_cboFountSize.GetItemData(m_cboFountSize.GetCurSel());
	tInfo.iFontHeight = m_cboFountSize.GetItemData(m_cboFountSize.GetCurSel());
	tInfo.iFontColor = m_cboFountColor.GetItemData(m_cboFountColor.GetCurSel());
	tInfo.iAlarmTypeId = m_cboOsdHintType.GetItemData(m_cboOsdHintType.GetCurSel());
	tInfo.iLedTypeId = m_cboLedType.GetItemData(m_cboLedType.GetCurSel());

	GetDlgItemText(IDC_EDIT_RADAR_LEDOSD_CAR_TEXT, cstrValue);
	tInfo.tHintInfo[0].iHintType = 0;
	strncpy_s(tInfo.tHintInfo[0].cDisplayInfo, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.tHintInfo[0].cDisplayInfo), cstrValue.GetLength()));
	tInfo.tHintInfo[0].iHintTime = GetDlgItemInt(IDC_EDIT_RADAR_LEDOSD_CAR_TIME);

	GetDlgItemText(IDC_EDIT_RADAR_LEDOSD_NOCAR_TEXT, cstrValue);
	tInfo.tHintInfo[1].iHintType = 1;
	strncpy_s(tInfo.tHintInfo[1].cDisplayInfo, (LPSTR)(LPCTSTR)cstrValue, min(sizeof(tInfo.tHintInfo[1].cDisplayInfo), cstrValue.GetLength()));

	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_LED_DEV_OSD_PARA, m_iChannelNo, &tInfo, sizeof(tInfo), &tInfo, sizeof(tInfo));
	if (RET_SUCCESS == iRet && RET_SUCCESS == tInfo.iActionResult)
	{
		UpdateLedOsdList();
		return RET_SUCCESS;
	}
	else
	{
		UpdateLedOsdList();
		return RET_FAILED;
	}
}


void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdQuery()
{
	UpdateLedOsdList();
}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdAdd()
{
	if (RET_SUCCESS == LedOsdOpt(LED_ACTION_TYPE_ADD))
	{
		MessageBox(GetTextByLan("ÃÌº”≥…π¶!","Success!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	else
	{
		MessageBox(GetTextByLan("ÃÌº” ß∞‹!","Failed!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}

}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdEdit()
{
	if (RET_SUCCESS == LedOsdOpt(LED_ACTION_TYPE_EDIT))
	{
		MessageBox(GetTextByLan("±‡º≠≥…π¶!","Success!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	else
	{
		MessageBox(GetTextByLan("±‡º≠ ß∞‹!","Failed!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}

}

void CLS_ItsRadarLedInfo::OnBnClickedButtonRadarLedosdDel()
{
	if (RET_SUCCESS == LedOsdOpt(LED_ACTION_TYPE_DEL))
	{
		MessageBox(GetTextByLan("…æ≥˝≥…π¶!","Success!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	else
	{
		MessageBox(GetTextByLan("…æ≥˝ ß∞‹!","Failed!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
}

void CLS_ItsRadarLedInfo::OnNMClickListRadarLedosd(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	POSITION pPos = m_lstLedOsd.GetFirstSelectedItemPosition();
	if (NULL != pPos)
	{
		int nItem = m_lstLedOsd.GetNextSelectedItem(pPos);
		int iIndex = atoi(m_lstLedOsd.GetItemText(nItem, ITEM_LED_INDEX));

		UpdateLedOsdUI(iIndex-1);
	}
	*pResult = 0;
}

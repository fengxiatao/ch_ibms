// CLS_XNVR_UPDATA.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_XNVR_UPDATA.h"
#include <stdlib.h>

#define MONTH_ERROR    0
#define WEEKDAY_ERROR  0
#define DAY_ERROR	   0

// CLS_XNVR_UPDATA dialog

IMPLEMENT_DYNAMIC(CLS_XNVR_UPDATA, CDialog)

CLS_XNVR_UPDATA::CLS_XNVR_UPDATA(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_XNVR_UPDATA::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	memset(&sCloudAutoDetect,-1,sizeof(CloudAutoDetect));
}

CLS_XNVR_UPDATA::~CLS_XNVR_UPDATA()
{
}

void CLS_XNVR_UPDATA::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_Combox_Type);
	DDX_Control(pDX, IDC_COMBO_WEEKDAY, m_Combox_Weekday);
	DDX_Control(pDX, IDC_COMBO_MONTH, m_Combox_Month);
	DDX_Control(pDX, IDC_COMBO_DAY, m_Combox_Day);
	DDX_Control(pDX, IDC_STATIC_WEEKDAY, m_static_weekday);
	DDX_Control(pDX, IDC_STATIC_MONTH, m_static_mouth);
	DDX_Control(pDX, IDC_STATIC_DAY, m_static_day);
	DDX_Control(pDX, IDC_EDIT_NEWVER, m_Edit_NewVer);
	DDX_Control(pDX, IDC_EDIT_RELEASEDATA, m_Edit_ReleaseData);
	DDX_Control(pDX, IDC_DATETIMEPICKER_HMS, m_DateTimeCtrl_hms);
	DDX_Control(pDX, IDC_EDIT_NEWVER_CHANNELNO, m_Edit_ChannelNo);
	DDX_Control(pDX, IDC_EDIT_NEWVER_NEWVERSTAT, m_Edit_NewVerStat);
	DDX_Control(pDX, IDC_EDIT_CHNDEVINFO_CHANNELNO, m_Edit_ChnDevInfo_ChannelNo);
	DDX_Control(pDX, IDC_EDIT_MAC, m_Edit_Mac);
	DDX_Control(pDX, IDC_EDIT_FACTORYID, m_Edit_FactoryId);
	DDX_Control(pDX, IDC_EDIT_BARCODE, m_Edit_BarCode);
}


BEGIN_MESSAGE_MAP(CLS_XNVR_UPDATA, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_IPCAUTOTIMING_SEND, &CLS_XNVR_UPDATA::OnBnClickedButtonIpcautotimingSend)
	ON_CBN_SELCHANGE(IDC_COMBO_TYPE, &CLS_XNVR_UPDATA::OnCbnSelchangeComboIpcautotimingType)
END_MESSAGE_MAP()

void CLS_XNVR_UPDATA::SetUIText()
{
	int i = 1;
	CString cMonth = 0;
	CString cDay = 0;
	m_Combox_Weekday.ShowWindow(FALSE);

	for (i; i < 13; i++)
	{
		IntToCString(i,&cMonth);
		m_Combox_Month.SetItemData(m_Combox_Month.AddString(_T(cMonth)),i);
	}

	for (i = 1; i < 32; i++)
	{
		IntToCString(i,&cDay);
		m_Combox_Day.SetItemData(m_Combox_Day.AddString(_T(cDay)),i);
	}

	m_Combox_Month.SetCurSel(0);
	m_Combox_Day.SetCurSel(0);
	m_Combox_Month.ShowWindow(FALSE);
	m_static_mouth.ShowWindow(FALSE);
	m_static_weekday.ShowWindow(FALSE);
	m_static_day.ShowWindow(FALSE);
	m_Combox_Day.ShowWindow(FALSE);
	m_DateTimeCtrl_hms.ShowWindow(FALSE);

}


BOOL CLS_XNVR_UPDATA::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	SetUIText();
	m_static_weekday.GetWindowRect(&rect);//get the absolute coordinates of the space
	ScreenToClient(&rect);//Get the coordinates relative to the main form
	m_Combox_Weekday.GetWindowRect(&rect1);//get the absolute coordinates of the space
	ScreenToClient(&rect1);//Get the coordinates relative to the main form
	m_static_mouth.GetWindowRect(&rect2);//get the absolute coordinates of the space
	ScreenToClient(&rect2);//Get the coordinates relative to the main form
	m_Combox_Month.GetWindowRect(&rect3);//get the absolute coordinates of the space
	ScreenToClient(&rect3);//Get the coordinates relative to the main form
	m_static_day.GetWindowRect(&rect4);//get the absolute coordinates of the space
	ScreenToClient(&rect4);//Get the coordinates relative to the main form
	m_Combox_Day.GetWindowRect(&rect5);//get the absolute coordinates of the space
	ScreenToClient(&rect5);//Get the coordinates relative to the main form
	m_DateTimeCtrl_hms.GetWindowRect(&rect6);//get the absolute coordinates of the space
	ScreenToClient(&rect6);//Get the coordinates relative to the main form

	UI_UpdateText();
	return TRUE;
}

void CLS_XNVR_UPDATA::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateText();
}

void CLS_XNVR_UPDATA::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else 
	{
		m_iLogonID = _iLogonID;
		m_iChannelNO = _iChannelNo;
	}
	UpdateParameter();
}

void CLS_XNVR_UPDATA::UpdateParameter()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_XNVR_UPDATA]Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	DevAutoTimingParam tDevAutoTimingParam = {0};
	int iReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DEV_AUTOTIMING,m_iChannelNO, &tDevAutoTimingParam, sizeof(tDevAutoTimingParam), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_DEV_AUTOTIMING fail!");
	}
	else
	{
		rect.OffsetRect(CSize(0,0));//Here if the relative position to be moved
		m_static_weekday.MoveWindow(rect);//move to target location

		rect1.OffsetRect(CSize(0,0));//Here if the relative position to be moved
		m_Combox_Weekday.MoveWindow(rect1);//move to target location

		rect2.OffsetRect(CSize(0,0));//Here if the relative position to be moved
		m_static_mouth.MoveWindow(rect2);//move to target location

		rect3.OffsetRect(CSize(0,0));//Here if the relative position to be moved
		m_Combox_Month.MoveWindow(rect3);//move to target location

		rect4.OffsetRect(CSize(0,0));//Here if the relative position to be moved
		m_static_day.MoveWindow(rect4);//move to target location

		rect5.OffsetRect(CSize(0,0));//Here if the relative position to be moved
		m_Combox_Day.MoveWindow(rect5);//move to target location

		rect6.OffsetRect(CSize(0,0));//Here if the relative position to be moved
		m_DateTimeCtrl_hms.MoveWindow(rect6);//move to target location

		XnvrShowWindow(tDevAutoTimingParam.iType);

		m_Combox_Type.SetCurSel(tDevAutoTimingParam.iType);
		if ( MONTH_ERROR == tDevAutoTimingParam.iMonth)
		{
			m_Combox_Month.SetCurSel(tDevAutoTimingParam.iMonth);
			AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_DEV_AUTOTIMING Month Parameter error!");
		}
		else
		{
			m_Combox_Month.SetCurSel(tDevAutoTimingParam.iMonth - 1);
		}

		if ( WEEKDAY_ERROR == tDevAutoTimingParam.iWeekDay)
		{
			m_Combox_Weekday.SetCurSel(tDevAutoTimingParam.iWeekDay);
			AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_DEV_AUTOTIMING Weekday Parameter error!");
		}
		else
		{
			m_Combox_Weekday.SetCurSel(tDevAutoTimingParam.iWeekDay - 1);
		}

		if ( DAY_ERROR == tDevAutoTimingParam.iDay)
		{
			m_Combox_Day.SetCurSel(tDevAutoTimingParam.iDay);
			AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_DEV_AUTOTIMING Day Parameter error!");
		}
		else
		{
			m_Combox_Day.SetCurSel(tDevAutoTimingParam.iDay - 1);
		}
		m_DateTimeCtrl_hms.SetFormat("HH:mm:ss");	
		CTime timeTime(1998, 4, 3, tDevAutoTimingParam.iHour, tDevAutoTimingParam.iMinute, tDevAutoTimingParam.iSecond);
		m_DateTimeCtrl_hms.SetTime(&timeTime);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_DEV_AUTOTIMING success!");
	}

	if (sCloudAutoDetect.iChannelNo == -1)
	{
		m_Edit_ChannelNo.SetWindowText("");
	}
	else
	{
		CString cChannlNo;
		IntToCString(sCloudAutoDetect.iChannelNo,&cChannlNo);
		m_Edit_ChannelNo.SetWindowText(cChannlNo);
	}
	if (sCloudAutoDetect.iNewVerStat == 0)
	{
		m_Edit_NewVerStat.SetWindowText("No New Version");
	}
	if (sCloudAutoDetect.iNewVerStat == 1)
	{
		m_Edit_NewVerStat.SetWindowText("New Version");
	}
	m_Edit_NewVer.SetWindowText(sCloudAutoDetect.cNewVer);
	m_Edit_ReleaseData.SetWindowText(sCloudAutoDetect.cReleaseData);

	ChnDevInfo tChnDevInfo = {0};
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_CHNDEVINFO,m_iChannelNO, &tChnDevInfo, sizeof(tChnDevInfo), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_CHNDEVINFO fail!");
	}
	else
	{
		CString cChannelNo;
		IntToCString(tChnDevInfo.iChannelNo,&cChannelNo);
		m_Edit_ChnDevInfo_ChannelNo.SetWindowText(cChannelNo);
		m_Edit_Mac.SetWindowText(tChnDevInfo.cMac);
		m_Edit_FactoryId.SetWindowText(tChnDevInfo.cFactoryID);
		m_Edit_BarCode.SetWindowText(tChnDevInfo.cBarCode);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_CHNDEVINFO success!");
	}
}

// CLS_XNVR_UPDATA message handler

void CLS_XNVR_UPDATA::UI_UpdateText()
{
	SetDlgItemText(IDC_STATIC_IPCAUTOTIMING, GetTextByLan(_T("设备自动校时时间"), _T("Device AutoTiming")));
	SetDlgItemText(IDC_STATIC_WEEKDAY, GetTextByLan(_T("周"), _T("Weekday")));
	SetDlgItemText(IDC_STATIC_MONTH, GetTextByLan(_T("月"), _T("Month")));
	SetDlgItemText(IDC_STATIC_DAY, GetTextByLan(_T("日"), _T("Day")));
	SetDlgItemText(IDC_BUTTON_IPCAUTOTIMING_SEND, GetTextByLan(_T("发送"), _T("Set")));
	SetDlgItemText(IDC_STATIC_CLOUD_AUTODETECT, GetTextByLan(_T("云升级自动检测结果"), _T("Cloud AutoDetect")));
	SetDlgItemText(IDC_STATIC_TIMINGTYPE, GetTextByLan(_T("校时类型"), _T("TimingType")));
	SetDlgItemText(IDC_STATIC_AUTODETECT_CHN, GetTextByLan(_T("通道号"), _T("ChannelNo")));
	SetDlgItemText(IDC_STATIC_AUTODETECT_NEWVARSTAT, GetTextByLan(_T("新版本"), _T("NewVerStat")));
	SetDlgItemText(IDC_STATIC_AUTODETECT_NEWVER, GetTextByLan(_T("新版本号"), _T("NewVerNo")));
	SetDlgItemText(IDC_STATIC_AUTODETECT_RELEASEDATE, GetTextByLan(_T("发布时间"), _T("ReleaseDate")));
	SetDlgItemText(IDC_STATIC_CHNDEVINFO, GetTextByLan(_T("通道设备信息"), _T("Channel Device Info")));
	SetDlgItemText(IDC_STATIC_CHNDEVINFO_CHANNELNO, GetTextByLan(_T("通道号"), _T("ChannelNo")));
	SetDlgItemText(IDC_STATIC_CHNDEVINFO_MAC, GetTextByLan(_T("Mac地址"), _T("Mac")));
	SetDlgItemText(IDC_STATIC_CHNDEVINFO_FACTORYID, GetTextByLan(_T("出厂ID"), _T("FactoryID")));
	SetDlgItemText(IDC_STATIC_CHNDEVINFO_BARCODE, GetTextByLan(_T("条形码"), _T("BarCode")));

	int iCurSel = m_Combox_Type.GetCurSel();
	m_Combox_Type.ResetContent();
	m_Combox_Type.SetItemData(m_Combox_Type.AddString(GetTextByLan(_T("永不"),_T("Never"))), 0);
	m_Combox_Type.SetItemData(m_Combox_Type.AddString(GetTextByLan(_T("年周期"),_T("Year Cycle"))), 1);
	m_Combox_Type.SetItemData(m_Combox_Type.AddString(GetTextByLan(_T("月周期"),_T("Month Cycle"))), 2);
	m_Combox_Type.SetItemData(m_Combox_Type.AddString(GetTextByLan(_T("周"),_T("Weekday"))), 3);
	m_Combox_Type.SetItemData(m_Combox_Type.AddString(GetTextByLan(_T("天"),_T("Day"))), 4);
	m_Combox_Type.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));


	iCurSel = m_Combox_Weekday.GetCurSel();
	m_Combox_Weekday.ResetContent();
	m_Combox_Weekday.SetItemData(m_Combox_Weekday.AddString(GetTextByLan(_T("星期一"),_T("Monday"))), 1);
	m_Combox_Weekday.SetItemData(m_Combox_Weekday.AddString(GetTextByLan(_T("星期二"),_T("Tuesday"))), 2);
	m_Combox_Weekday.SetItemData(m_Combox_Weekday.AddString(GetTextByLan(_T("星期三"),_T("Wednesday"))), 3);
	m_Combox_Weekday.SetItemData(m_Combox_Weekday.AddString(GetTextByLan(_T("星期四"),_T("Thursday"))), 4);
	m_Combox_Weekday.SetItemData(m_Combox_Weekday.AddString(GetTextByLan(_T("星期五"),_T("Friday"))), 5);
	m_Combox_Weekday.SetItemData(m_Combox_Weekday.AddString(GetTextByLan(_T("星期六"),_T("Saturday"))), 6);
	m_Combox_Weekday.SetItemData(m_Combox_Weekday.AddString(GetTextByLan(_T("星期日"),_T("Sunday"))), 7);
	m_Combox_Weekday.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));

}

void CLS_XNVR_UPDATA::OnBnClickedButtonIpcautotimingSend()
{
	// TODO: Add control notification handler code here
	int iTempType = m_Combox_Type.GetItemData(m_Combox_Type.GetCurSel());
	int iTempMonth = m_Combox_Month.GetItemData(m_Combox_Month.GetCurSel());
	int iTempWeekday = m_Combox_Weekday.GetItemData(m_Combox_Weekday.GetCurSel());
	int iTempDay = m_Combox_Day.GetItemData(m_Combox_Day.GetCurSel());
	SYSTEMTIME tTime = {0};
	memset(&tTime, 0, sizeof(tTime));
	m_DateTimeCtrl_hms.GetTime(&tTime);
	int iTempHour = tTime.wHour;
	int iTempMinute = tTime.wMinute;
	int iTempSecond = tTime.wSecond;

	DevAutoTimingParam tDevAutoTimingParam = {0};
	tDevAutoTimingParam.iType = iTempType;
	tDevAutoTimingParam.iSize = sizeof(tDevAutoTimingParam);
	switch(iTempType)
	{
	case 1:
		{
			tDevAutoTimingParam.iMonth = iTempMonth;
			tDevAutoTimingParam.iDay = iTempDay;
			tDevAutoTimingParam.iHour = iTempHour;
			tDevAutoTimingParam.iMinute = iTempMinute;
			tDevAutoTimingParam.iSecond = iTempSecond;
		}
		break;
	case 2:
		{
			tDevAutoTimingParam.iDay = iTempDay;
			tDevAutoTimingParam.iHour = iTempHour;
			tDevAutoTimingParam.iMinute = iTempMinute;
			tDevAutoTimingParam.iSecond = iTempSecond;
		}
		break;
	case 3:
		{
			tDevAutoTimingParam.iWeekDay = iTempWeekday;
		}
		break;
	case 4:
		{
			tDevAutoTimingParam.iHour = iTempHour;
			tDevAutoTimingParam.iMinute = iTempMinute;
			tDevAutoTimingParam.iSecond = iTempSecond;
		}
		break;
	default:
		break;
	}

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DEV_AUTOTIMING, m_iChannelNO, &tDevAutoTimingParam, sizeof(tDevAutoTimingParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_ENCODE_ENABLE fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_ENCODE_ENABLE success!");
	}
}

void CLS_XNVR_UPDATA::OnCbnSelchangeComboIpcautotimingType()
{
	// TODO: Add control notification handler code here
	rect.OffsetRect(CSize(0,0));//Here if the relative position to be moved
	m_static_weekday.MoveWindow(rect);//move to target location

	rect1.OffsetRect(CSize(0,0));//Here if the relative position to be moved
	m_Combox_Weekday.MoveWindow(rect1);//move to target location

	rect2.OffsetRect(CSize(0,0));//Here if the relative position to be moved
	m_static_mouth.MoveWindow(rect2);//move to target location

	rect3.OffsetRect(CSize(0,0));//Here if the relative position to be moved
	m_Combox_Month.MoveWindow(rect3);//move to target location

	rect4.OffsetRect(CSize(0,0));//Here if the relative position to be moved
	m_static_day.MoveWindow(rect4);//move to target location

	rect5.OffsetRect(CSize(0,0));//Here if the relative position to be moved
	m_Combox_Day.MoveWindow(rect5);//move to target location

	rect6.OffsetRect(CSize(0,0));//Here if the relative position to be moved
	m_DateTimeCtrl_hms.MoveWindow(rect6);//move to target location
	int iTemp = m_Combox_Type.GetItemData(m_Combox_Type.GetCurSel());

	XnvrShowWindow(iTemp);
}

void CLS_XNVR_UPDATA::XnvrShowWindow(int _iType)
{
	switch (_iType)
	{
	case 0:
		{
			m_static_mouth.ShowWindow(FALSE);
			m_Combox_Month.ShowWindow(FALSE);
			m_static_weekday.ShowWindow(FALSE);
			m_Combox_Weekday.ShowWindow(FALSE);
			m_static_day.ShowWindow(FALSE);
			m_Combox_Day.ShowWindow(FALSE);
			m_DateTimeCtrl_hms.ShowWindow(FALSE);
		}
		break;
	case 1:
		{	
			m_static_weekday.ShowWindow(FALSE);
			m_Combox_Weekday.ShowWindow(FALSE);
			m_static_mouth.ShowWindow(TRUE);
			m_Combox_Month.ShowWindow(TRUE);
			m_static_day.ShowWindow(TRUE);
			m_Combox_Day.ShowWindow(TRUE);
			m_DateTimeCtrl_hms.ShowWindow(TRUE);

			m_static_mouth.MoveWindow(rect);
			m_Combox_Month.MoveWindow(rect1);
			m_static_day.MoveWindow(rect2);
			m_Combox_Day.MoveWindow(rect3);
			m_DateTimeCtrl_hms.MoveWindow(rect5);
		}
		break;
	case 2:
		{
			m_static_mouth.ShowWindow(FALSE);
			m_Combox_Month.ShowWindow(FALSE);
			m_static_weekday.ShowWindow(FALSE);
			m_Combox_Weekday.ShowWindow(FALSE);
			m_static_day.ShowWindow(TRUE);
			m_Combox_Day.ShowWindow(TRUE);
			m_DateTimeCtrl_hms.ShowWindow(TRUE);

			m_static_day.MoveWindow(rect);
			m_Combox_Day.MoveWindow(rect1);
			m_DateTimeCtrl_hms.MoveWindow(rect3);
		}
		break;
	case 3:
		{
			m_static_mouth.ShowWindow(FALSE);
			m_Combox_Month.ShowWindow(FALSE);
			m_static_day.ShowWindow(FALSE);
			m_Combox_Day.ShowWindow(FALSE);
			m_static_weekday.ShowWindow(TRUE);
			m_Combox_Weekday.ShowWindow(TRUE);
			m_DateTimeCtrl_hms.ShowWindow(TRUE);

			m_DateTimeCtrl_hms.MoveWindow(rect3);
		}
		break;
	case 4:
		{
			m_static_mouth.ShowWindow(FALSE);
			m_Combox_Month.ShowWindow(FALSE);
			m_static_day.ShowWindow(FALSE);
			m_Combox_Day.ShowWindow(FALSE);
			m_static_weekday.ShowWindow(FALSE);
			m_Combox_Weekday.ShowWindow(FALSE);
			m_DateTimeCtrl_hms.ShowWindow(TRUE);

			m_DateTimeCtrl_hms.MoveWindow(rect1);
		}
		break;
	default:
		break;
	}
}

void CLS_XNVR_UPDATA::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	if (_wParam == WCM_CLOUD_AUTODETECT)
	{
		CloudAutoDetect* tCloudAutoDetect = (CloudAutoDetect*)(_iLParam);
		memset(&sCloudAutoDetect,0,sizeof(CloudAutoDetect));
		sCloudAutoDetect.iSize = tCloudAutoDetect->iSize;
		sCloudAutoDetect.iChannelNo = tCloudAutoDetect->iChannelNo;
		sCloudAutoDetect.iNewVerStat = tCloudAutoDetect->iNewVerStat;
		memcpy(sCloudAutoDetect.cNewVer,tCloudAutoDetect->cNewVer,sizeof(sCloudAutoDetect.cNewVer));
		memcpy(sCloudAutoDetect.cReleaseData,tCloudAutoDetect->cReleaseData,sizeof(sCloudAutoDetect.cReleaseData));
	}
}

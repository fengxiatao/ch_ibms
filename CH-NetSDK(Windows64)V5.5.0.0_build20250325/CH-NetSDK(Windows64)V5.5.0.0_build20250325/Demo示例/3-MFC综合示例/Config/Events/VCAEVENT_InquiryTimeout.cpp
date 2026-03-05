//VCAEVENT_InquiryTimeout.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_InquiryTimeout.h"


// CLS_VCAEVENT_InquiryTimeout dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_InquiryTimeout, CDialog)

CLS_VCAEVENT_InquiryTimeout::CLS_VCAEVENT_InquiryTimeout(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_InquiryTimeout::IDD, pParent)
{

}

CLS_VCAEVENT_InquiryTimeout::~CLS_VCAEVENT_InquiryTimeout()
{
}

void CLS_VCAEVENT_InquiryTimeout::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_EVENTVALID, m_chkEventValid);
	DDX_Control(pDX, IDC_CHECK_SHOW_RULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_SHOW_ALARM_STATIC, m_chkShowAlarmStat);
	DDX_Control(pDX, IDC_CHECK_SHOW_TARGETFRAME, m_chkTargetBox);
	DDX_Control(pDX, IDC_EDIT_MIN, m_edtMinSize);
	DDX_Control(pDX, IDC_EDIT_MAX, m_edtMaxSize);
	DDX_Control(pDX, IDC_EDIT_SENSITIVITY, m_edtSensitivity);
	DDX_Control(pDX, IDC_EDIT_INQUIRY_TIME, m_edtInquryTime);
	DDX_Control(pDX, IDC_EDIT_ALLOW_LEAVE_TIME, m_edtAllowLeaveTime);
	DDX_Control(pDX, IDC_DATETIMEPICKER_START1, m_dtcStartTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_START2, m_dtcStartTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_START3, m_dtcStartTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_START4, m_dtcStartTime[3]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_END1, m_dtcEndTime[0]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_END2, m_dtcEndTime[1]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_END3, m_dtcEndTime[2]);
	DDX_Control(pDX, IDC_DATETIMEPICKER_END4, m_dtcEndTime[3]);
	DDX_Control(pDX, IDC_CHECK_TIME1, m_chkTime[0]);
	DDX_Control(pDX, IDC_CHECK_TIME2, m_chkTime[1]);
	DDX_Control(pDX, IDC_CHECK_TIME3, m_chkTime[2]);
	DDX_Control(pDX, IDC_CHECK_TIME4, m_chkTime[3]);

	DDX_Control(pDX, IDC_COMBO_CHECK_AREA_NUM, m_cboCheckAreaNum);
	DDX_Control(pDX, IDC_COMBO_CHECK_AREA, m_cboCheckArea);
	DDX_Control(pDX, IDC_EDIT_CHECK_AREA, m_edtCheckArea);
	DDX_Control(pDX, IDC_COMBO_NOVALID_AREA_NUM, m_cboNovalidAreaNum);
	DDX_Control(pDX, IDC_COMBO_NOVALID_AREA, m_cboNovalidArea);
	DDX_Control(pDX, IDC_EDIT_NOVALID_AREA, m_edtNovalidArea);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_InquiryTimeout, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_CHECK_AREA, &CLS_VCAEVENT_InquiryTimeout::OnBnClickedButtonCheckArea)
	ON_BN_CLICKED(IDC_BUTTON_NOVALID_AREA, &CLS_VCAEVENT_InquiryTimeout::OnBnClickedButtonNovalidArea)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_VCAEVENT_InquiryTimeout::OnBnClickedButtonSet)
	ON_CBN_SELCHANGE(IDC_COMBO_CHECK_AREA_NUM, &CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboCheckAreaNum)
	ON_CBN_SELCHANGE(IDC_COMBO_CHECK_AREA, &CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboCheckArea)
	ON_CBN_SELCHANGE(IDC_COMBO_NOVALID_AREA_NUM, &CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboNovalidAreaNum)
	ON_CBN_SELCHANGE(IDC_COMBO_NOVALID_AREA, &CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboNovalidArea)
	ON_BN_CLICKED(IDC_CHECK_TIME1, &CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime1)
	ON_BN_CLICKED(IDC_CHECK_TIME3, &CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime3)
	ON_BN_CLICKED(IDC_CHECK_TIME2, &CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime2)
	ON_BN_CLICKED(IDC_CHECK_TIME4, &CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime4)
END_MESSAGE_MAP()

BOOL CLS_VCAEVENT_InquiryTimeout::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	CTime timeNow = CTime::GetCurrentTime();
	timeNow = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(),0, 0, 0);
	for (int i=0; i<4; i++)
	{
		m_dtcStartTime[i].EnableWindow(FALSE);
		m_dtcEndTime[i].EnableWindow(FALSE);
		m_dtcStartTime[i].SetFormat(_T("HH:mm"));
		m_dtcEndTime[i].SetFormat(_T("HH:mm"));
		m_dtcStartTime[i].SetTime(&timeNow);
		m_dtcEndTime[i].SetTime(&timeNow);
	}

	memset(&m_vcaInquityTimeout, 0, sizeof(m_vcaInquityTimeout));

	UpdateUIText();
	return TRUE;
}

void CLS_VCAEVENT_InquiryTimeout::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	CleanText();
	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAEVENT_InquiryTimeout::OnLanguageChanged()
{
	UpdateUIText();
}

void CLS_VCAEVENT_InquiryTimeout::UpdateUIText()
{
	SetDlgItemTextEx(IDC_CHECK_EVENTVALID, IDS_VCAEVENT_EVENT_VALID);
	SetDlgItemTextEx(IDC_CHECK_SHOW_RULE, IDS_VCAEVENT_SHOW_ALARM_RULE);
	SetDlgItemTextEx(IDC_CHECK_SHOW_ALARM_STATIC, IDS_VCAEVENT_SHOW_ALARM_STATISTICS);
	SetDlgItemTextEx(IDC_CHECK_SHOW_TARGETFRAME, IDS_VCAEVENT_SHOW_TARGET_BOX);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_MIN, IDS_VCAEVENT_INQUIRY_TIMEOUT_MIN);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_MAX, IDS_VCAEVENT_INQUIRY_TIMEOUT_MAX);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_SEN, IDS_VCAEVENT_INQUIRY_TIMEOUT_SEN);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_INQUIRYTIME, IDS_VCAEVENT_INQUIRY_TIMEOUT_INQUIRYTIME);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_LEAVETIME, IDS_VCAEVENT_INQUIRY_TIMEOUT_LEAVE_TIME);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_TIME1, IDS_CONFIG_DNVR_ALMSCH_TIME1);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_TIME2, IDS_CONFIG_DNVR_ALMSCH_TIME2);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_TIME3, IDS_CONFIG_DNVR_ALMSCH_TIME3);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_TIME4, IDS_CONFIG_DNVR_ALMSCH_TIME4);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_CHECK_AREA_NUM, IDS_CONFIG_INQUIRY_CHECK_AREA_NUM);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_CHECK_AREA, IDS_CONFIG_INQUIRY_CHECK_AREA);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_NOVALID_AREA_NUM, IDS_CONFIG_INQUIRY_NOVALID_AREA_NUM);
	SetDlgItemTextEx(IDC_STATIC_INQUIRY_NOVALID_AREA, IDS_CONFIG_INQUIRY_NOVALID_AREA);
	SetDlgItemTextEx(IDC_BUTTON_CHECK_AREA, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BUTTON_NOVALID_AREA, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BUTTON_SET, IDS_SET);

	const CString cstNum[] = {"0","1","2","3","4","5","6","7","8"};
	m_cboCheckAreaNum.ResetContent();
	m_cboNovalidAreaNum.ResetContent();
	for(int i = 0; i <= 8; ++i)
	{
		m_cboCheckAreaNum.InsertString(i,cstNum[i]);
		m_cboNovalidAreaNum.InsertString(i,cstNum[i]);
	}
	m_cboCheckAreaNum.SetCurSel(0);
	m_cboNovalidAreaNum.SetCurSel(0);

	m_cboCheckArea.ResetContent();
	m_cboNovalidArea.ResetContent();
	m_cboCheckArea.InsertString(0, cstNum[0]);
	m_cboNovalidArea.InsertString(0, cstNum[0]);
	m_cboCheckArea.SetCurSel(0);
	m_cboNovalidArea.SetCurSel(0);

	SetDlgItemInt(IDC_EDIT_MIN, INQUIRY_TIMEOUT_MIN_SIZE);
	SetDlgItemInt(IDC_EDIT_MAX, INQUIRY_TIMEOUT_MAX_SIZE);
	SetDlgItemInt(IDC_EDIT_SENSITIVITY, INQUIRY_TIMEOUT_SEN);
	SetDlgItemInt(IDC_EDIT_INQUIRY_TIME, INQUIRY_TIMEOUT_INQUIRYTIME);
	SetDlgItemInt(IDC_EDIT_ALLOW_LEAVE_TIME, INQUIRY_TIMEOUT_LEAVETIME);
}

void CLS_VCAEVENT_InquiryTimeout::CleanText()
{
	m_edtMinSize.SetWindowText(_T(""));
	m_edtMaxSize.SetWindowText(_T(""));
	m_edtSensitivity.SetWindowText(_T(""));
	m_edtInquryTime.SetWindowText(_T(""));
	m_edtAllowLeaveTime.SetWindowText(_T(""));
	m_edtCheckArea.SetWindowText(_T(""));
	m_edtNovalidArea.SetWindowText(_T(""));
}

void CLS_VCAEVENT_InquiryTimeout::UpdatePageUI()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VcaInquiryTimeout vi = {0};
	vi.iSize = sizeof(vi);
	vi.iDevType = m_iDevType;
	vi.iRuleID = m_iRuleID;
	vi.iSceneId = m_iSceneID;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_INQUIRY_TIMEOUT,m_iChannelNO, &vi, sizeof(VcaInquiryTimeout));
	if(iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_InquiryTimeout::NetClient_VCAGetConfig[VCA_CMD_STOLEN] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_vcaInquityTimeout = vi;
		m_chkEventValid.SetCheck(vi.iValid);
		m_chkShowRule.SetCheck(vi.iDisplayRule);
		m_chkShowAlarmStat.SetCheck(vi.iDisplayStat);
		m_chkTargetBox.SetCheck(vi.iDisplayTarget);
		SetDlgItemInt(IDC_EDIT_MIN, vi.iMiniSize);
		SetDlgItemInt(IDC_EDIT_MAX, vi.iMaxSize);
		SetDlgItemInt(IDC_EDIT_SENSITIVITY, vi.iSensitivity);
		SetDlgItemInt(IDC_EDIT_INQUIRY_TIME, vi.iInquiryTime);
		SetDlgItemInt(IDC_EDIT_ALLOW_LEAVE_TIME, vi.iLeaveTime);

		SYSTEMTIME sysTime = {0};
		GetLocalTime(&sysTime);
		for (int i=0; i<vi.iBantimeNum; i++)
		{
			sysTime.wHour = vi.tBanTime[i].iStartHour;
			sysTime.wMinute = vi.tBanTime[i].iStartMin;
			sysTime.wSecond = 0;
			m_dtcStartTime[i].SetTime(&sysTime);

			sysTime.wHour = vi.tBanTime[i].iStopHour;
			sysTime.wMinute = vi.tBanTime[i].iStopMin;
			sysTime.wSecond = 0;
			m_dtcEndTime[i].SetTime(&sysTime);
			m_chkTime[i].SetCheck(vi.tBanTime[i].iEnable);
			if (vi.tBanTime[i].iEnable)
			{
				m_dtcStartTime[i].EnableWindow(TRUE);
				m_dtcEndTime[i].EnableWindow(TRUE);
			}
			else
			{
				m_dtcStartTime[i].EnableWindow(FALSE);
				m_dtcEndTime[i].EnableWindow(FALSE );
			}
		}

		int iCheckNum = vi.iDetectRegionNum;
		if(iCheckNum > 0)
		{
			m_cboCheckAreaNum.SetCurSel(iCheckNum);
			OnCbnSelchangeComboCheckAreaNum();
			CString cstPointBufMain = "";
			for (int i=0; i<vi.tDetectRegions[0].iPointNum; i++)
			{
				cstPointBufMain.AppendFormat("(%d,%d)", vi.tDetectRegions[0].stPoints[i].iX, vi.tDetectRegions[0].stPoints[i].iY);
			}
			m_edtCheckArea.SetWindowText(cstPointBufMain);
		}
		else
		{
			m_cboCheckAreaNum.SetCurSel(0);
			OnCbnSelchangeComboCheckAreaNum();
		}

		int iNovalidNum = vi.iInvalidRegionNum;
		if(iNovalidNum > 0)
		{
			m_cboNovalidAreaNum.SetCurSel(iNovalidNum);
			OnCbnSelchangeComboNovalidAreaNum();
			CString cstPointBufMain = "";
			for (int i=0; i<vi.tInvalidRegions[0].iPointNum; i++)
			{
				cstPointBufMain.AppendFormat("(%d,%d)", vi.tInvalidRegions[0].stPoints[i].iX, vi.tInvalidRegions[0].stPoints[i].iY);
			}
			m_edtNovalidArea.SetWindowText(cstPointBufMain);

		}
		else
		{
			m_cboNovalidAreaNum.SetCurSel(0);
			OnCbnSelchangeComboNovalidAreaNum();
		}
	}
}

void CLS_VCAEVENT_InquiryTimeout::DrawOnVideo(CEdit& _edtSluiceGate, int* _piPointCount)
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}

	/* The following code can take out the corresponding parameters from the draw dialog box */
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter);
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, _piPointCount, &iDirection,TRUE);
	if (-1 == iSetRet)
	{
		return;
	}

	if (IDOK == m_pDlgVideoView->DoModal())
	{
		_edtSluiceGate.SetWindowText(cPointBuf);
	}

	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

// CLS_VCAEVENT_InquiryTimeout message handlers

void CLS_VCAEVENT_InquiryTimeout::OnBnClickedButtonCheckArea()
{
	if(m_cboCheckAreaNum.GetCurSel() > 0)
	{
		int iCurSel = m_cboCheckArea.GetCurSel();
		DrawOnVideo(m_edtCheckArea,&m_vcaInquityTimeout.tDetectRegions[iCurSel].iPointNum);
		CString strPointMain;
		GetDlgItemText(IDC_EDIT_CHECK_AREA, strPointMain);
		GetPolyFromStringEx(strPointMain, m_vcaInquityTimeout.tDetectRegions[iCurSel].iPointNum, m_vcaInquityTimeout.tDetectRegions[iCurSel]);
	}
	else
	{
		MessageBox(GetTextByLan("请选择正确的检测区域个数","Please select correct check area num"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnBnClickedButtonNovalidArea()
{
	if(m_cboNovalidAreaNum.GetCurSel() > 0)
	{
		int iCurSel = m_cboNovalidArea.GetCurSel();
		DrawOnVideo(m_edtNovalidArea,&m_vcaInquityTimeout.tInvalidRegions[iCurSel].iPointNum);
		CString strPointMain;
		GetDlgItemText(IDC_EDIT_NOVALID_AREA, strPointMain);
		GetPolyFromStringEx(strPointMain, m_vcaInquityTimeout.tInvalidRegions[iCurSel].iPointNum, m_vcaInquityTimeout.tInvalidRegions[iCurSel]);
	}
	else
	{
		MessageBox(GetTextByLan("请选择正确的无效区域个数","Please select correct novalid area num"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
	}
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnBnClickedButtonSet()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_InquiryTimeout::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	VcaInquiryTimeout vi = m_vcaInquityTimeout;
	vi.iSize = sizeof(vi);
	vi.iDevType = m_iDevType;
	vi.iRuleID = m_iRuleID;
	vi.iSceneId = m_iSceneID;
	vi.iValid = m_chkEventValid.GetCheck();
	vi.iDisplayRule = m_chkShowRule.GetCheck();
	vi.iDisplayStat = m_chkShowAlarmStat.GetCheck();
	vi.iDisplayTarget = m_chkTargetBox.GetCheck();
	int iTemp = GetDlgItemInt(IDC_EDIT_MIN);
	if(iTemp < 0 || iTemp > 100)
	{
		MessageBox(GetTextByLan("最小尺寸，请输入0~100!","Min Size，Please Input0~100!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}
	vi.iMiniSize = iTemp;


	iTemp = GetDlgItemInt(IDC_EDIT_MAX);
	if(iTemp < 0 || iTemp > 100)
	{
		MessageBox(GetTextByLan("最大尺寸，请输入0~100!","Max Size，Please Input0~100!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}
	vi.iMaxSize = iTemp;

	if(vi.iMiniSize >= vi.iMaxSize)
	{
		MessageBox(GetTextByLan("最小尺寸小于最大尺寸","Min Size less than Max Size!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}
	
	iTemp = GetDlgItemInt(IDC_EDIT_SENSITIVITY);
	if(iTemp < 0 || iTemp > 100)
	{
		MessageBox(GetTextByLan("灵敏度，请输入0~100!","Sensitivity，Please Input0~100!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}
	vi.iSensitivity = iTemp;
	
	iTemp = GetDlgItemInt(IDC_EDIT_INQUIRY_TIME);
	if(iTemp < 0 || iTemp > 86400)
	{
		MessageBox(GetTextByLan("讯问时间，请输入0~86400s!","Inquiry Time，Please Input0~86400s!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}
	vi.iInquiryTime = iTemp;
	
	iTemp = GetDlgItemInt(IDC_EDIT_ALLOW_LEAVE_TIME);
	if(iTemp < 0 || iTemp > 3600)
	{
		MessageBox(GetTextByLan("允许离开时间，请输入0~3600s!","Allow Leave Time，Please Input0~3600s!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}
	vi.iLeaveTime = iTemp;

	CTime timeStart[MAX_TIMESEGMENT],timeStop[MAX_TIMESEGMENT];
	int iRecordMode[MAX_TIMESEGMENT];
	for (int i = 0;i < 4;i ++)
	{
		m_dtcStartTime[i].GetTime(timeStart[i]);
		m_dtcEndTime[i].GetTime(timeStop[i]);
		iRecordMode[i] = m_chkTime[i].GetCheck();		
	}
	for (int i = 0;i < 4;i ++)
	{
		if (iRecordMode[i] && timeStart[i] >= timeStop[i])
		{
			MessageBox(GetTextByLan("起始时间必须小于结束时间","Start time must be earlier than end time!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
			return;
		}
	}

	for (int i = 0; i < MAX_TIMESEGMENT; ++i)
	{
		for (int j = i+1; j < MAX_TIMESEGMENT; ++j)
		{
			if (iRecordMode[i] && iRecordMode[j])
			{				
				if (timeStop[i] > timeStart[j] && timeStart[i] < timeStop[j]
				||timeStop[j] > timeStart[i] && timeStart[j] < timeStop[i])
				{
					MessageBox(GetTextByLan("起始时间必须小于结束时间","Start time must be earlier than end time!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
					return;
				}		
			}			
		}
	}

	vi.iBantimeNum = 0;
	for(int i = 0; i < 4; ++i)
	{
		vi.tBanTime[i].iStartHour = 0;
		vi.tBanTime[i].iStartMin = 0;
		vi.tBanTime[i].iStopHour = 0;
		vi.tBanTime[i].iStopMin = 0;
		vi.tBanTime[i].iEnable = 0;
	}

	for(int i = 0; i < 4; ++i)
	{
		if(iRecordMode[i])
		{
			vi.tBanTime[vi.iBantimeNum].iStartHour = timeStart[i].GetHour();
			vi.tBanTime[vi.iBantimeNum].iStartMin = timeStart[i].GetMinute();
			vi.tBanTime[vi.iBantimeNum].iStopHour = timeStop[i].GetHour();
			vi.tBanTime[vi.iBantimeNum].iStopMin = timeStop[i].GetMinute();
			vi.tBanTime[vi.iBantimeNum].iEnable = iRecordMode[i];
			vi.iBantimeNum++;
		}
	}

	vi.iDetectRegionNum = m_cboCheckAreaNum.GetCurSel();
	for(int i = 0; i < vi.iDetectRegionNum; ++i)
	{
		if(vi.tDetectRegions[i].iPointNum < 2)
		{
			MessageBox(GetTextByLan("检测区域绘制不全","Check Area Draw Not All"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
			return;
		}
	}
	vi.iInvalidRegionNum = m_cboNovalidAreaNum.GetCurSel();
	for(int i = 0; i < vi.iInvalidRegionNum; ++i)
	{
		if(vi.tInvalidRegions[i].iPointNum < 2)
		{
			MessageBox(GetTextByLan("无效区域绘制不全","Novalid Area Draw Not All"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
			return;
		}
	}

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_INQUIRY_TIMEOUT, m_iChannelNO, &vi, sizeof(VcaInquiryTimeout));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_InquiryTimeout::NetClient_VCASetConfig[VCA_CMD_INQUIRY_TIMEOUT] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_InquiryTimeout::NetClient_VCASetConfig[VCA_CMD_INQUIRY_TIMEOUT] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboCheckAreaNum()
{	
	const CString cstNum[] = {"0","1","2","3","4","5","6","7","8"};
	int iCurSel = m_cboCheckAreaNum.GetCurSel();
	if(0 == iCurSel)
	{
		m_cboCheckArea.ResetContent();
		m_cboCheckArea.InsertString(0, cstNum[0]);
		m_cboCheckArea.SetCurSel(0);
	}
	else
	{
		m_cboCheckArea.ResetContent();
		for(int i = 0; i < iCurSel; ++i)
		{
			m_cboCheckArea.InsertString(i, cstNum[i+1]);		
		}
		m_cboCheckArea.SetCurSel(0);
	}
	OnCbnSelchangeComboCheckArea();
	m_vcaInquityTimeout.iDetectRegionNum = iCurSel;
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboCheckArea()
{
	m_edtCheckArea.SetWindowText(_T(""));
	if(m_cboCheckAreaNum.GetCurSel() > 0)
	{
		int iCurSel = m_cboCheckArea.GetCurSel();
		CString cstPointBufMain = "";
		for (int i=0; i<m_vcaInquityTimeout.tDetectRegions[iCurSel].iPointNum; i++)
		{
			cstPointBufMain.AppendFormat("(%d,%d)", m_vcaInquityTimeout.tDetectRegions[iCurSel].stPoints[i].iX, m_vcaInquityTimeout.tDetectRegions[iCurSel].stPoints[i].iY);
		}
		m_edtCheckArea.SetWindowText(cstPointBufMain);
	}
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboNovalidAreaNum()
{
	const CString cstNum[] = {"0","1","2","3","4","5","6","7","8"};
	int iCurSel = m_cboNovalidAreaNum.GetCurSel();
	if(0 == iCurSel)
	{
		m_cboNovalidArea.ResetContent();
		m_cboNovalidArea.InsertString(0, cstNum[0]);
		m_cboNovalidArea.SetCurSel(0);
	}
	else
	{
		m_cboNovalidArea.ResetContent();
		for(int i = 0; i < iCurSel; ++i)
		{
			m_cboNovalidArea.InsertString(i, cstNum[i+1]);		
		}
		m_cboNovalidArea.SetCurSel(0);

	}
	OnCbnSelchangeComboNovalidArea();
	m_vcaInquityTimeout.iInvalidRegionNum = iCurSel;
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnCbnSelchangeComboNovalidArea()
{
	m_edtNovalidArea.SetWindowText(_T(""));
	if(m_cboNovalidAreaNum.GetCurSel() > 0)
	{
		int iCurSel = m_cboNovalidArea.GetCurSel();
		CString cstPointBufMain = "";
		for (int i=0; i<m_vcaInquityTimeout.tInvalidRegions[iCurSel].iPointNum; i++)
		{
			cstPointBufMain.AppendFormat("(%d,%d)", m_vcaInquityTimeout.tInvalidRegions[iCurSel].stPoints[i].iX, m_vcaInquityTimeout.tInvalidRegions[iCurSel].stPoints[i].iY);
		}
		m_edtNovalidArea.SetWindowText(cstPointBufMain);
	}
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::CheckSchedtime(int _iIndex)
{
	if(BST_CHECKED == m_chkTime[_iIndex].GetCheck())
	{
		m_dtcStartTime[_iIndex].EnableWindow(true);
		m_dtcEndTime[_iIndex].EnableWindow(true);
	}
	else
	{
		m_dtcStartTime[_iIndex].EnableWindow(false);
		m_dtcEndTime[_iIndex].EnableWindow(false);
		CTime timeNow = CTime::GetCurrentTime();
		timeNow = CTime(timeNow.GetYear(), timeNow.GetMonth(), timeNow.GetDay(),0, 0, 0);
		m_dtcStartTime[_iIndex].SetTime(&timeNow);
		m_dtcEndTime[_iIndex].SetTime(&timeNow);
	}
}

void CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime1()
{
	CheckSchedtime(0);
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime2()
{
	CheckSchedtime(1);
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime3()
{
	CheckSchedtime(2);
	// TODO: Add your control notification handler code here
}

void CLS_VCAEVENT_InquiryTimeout::OnBnClickedCheckTime4()
{
	CheckSchedtime(3);
	// TODO: Add your control notification handler code here
}

// CLS_Shdb.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_Shdb.h"



// CLS_Shdb dialog

IMPLEMENT_DYNAMIC(CLS_Shdb, CDialog)

CLS_Shdb::CLS_Shdb(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_Shdb::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = -1;
	m_iStreamNo = -1;
	memset(&tChanList, 0, sizeof(tChanList));
	m_pclsChanCheck = NULL;
}

CLS_Shdb::~CLS_Shdb()
{
	if (NULL != m_pclsChanCheck)
	{
		delete m_pclsChanCheck;
		m_pclsChanCheck = NULL; 
	}
}

void CLS_Shdb::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_REPAIRSYSNAME, m_cmobo_repairsysyname);
	DDX_Control(pDX, IDC_COMBO_REPAIRTYPE, m_combo_repairtype);
	DDX_Control(pDX, IDC_COMBO_MAINTAINTYPE, m_combo_maintaintype);
	DDX_Control(pDX, IDC_COMBO_MAINREPAIRTYPE, m_combo_mainrepairtype);
	DDX_Control(pDX, IDC_COMBO_TESTOPERTYPE, m_combo_iopertype);
	DDX_Control(pDX, IDC_EDIT_POLICEID, m_edit_policeid);
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_edit_password);
	DDX_Control(pDX, IDC_COMBO_PRETM, m_combo_pretm);
	DDX_Control(pDX, IDC_COMBO_DELAYTM, m_combo_delaytm);
	DDX_Control(pDX, IDC_COMBO_INTERVALTM, m_combo_intervaltm);
	DDX_Control(pDX, IDC_CHECK_ALARMPIC, m_check_alarm);
	DDX_Control(pDX, IDC_CHECK_TIMESNAP, m_check_tmenable);
	//DDX_Control(pDX, IDC_COMBO_GRPNUM, m_combo_timesnap);
	DDX_Control(pDX, IDC_EDIT_BEGINTM1, m_CEditStartTime[0]);
	DDX_Control(pDX, IDC_EDIT_ENDTM1, m_CEditEndTime[0]);
	DDX_Control(pDX, IDC_EDIT_BEGINTM2, m_CEditStartTime[1]);
	DDX_Control(pDX, IDC_EDIT_ENDTM2, m_CEditEndTime[1]);
	DDX_Control(pDX, IDC_CHECK_TMENABLE1, m_ChkGroupEnable[0]);
	DDX_Control(pDX, IDC_CHECK_TMENABLE2, m_ChkGroupEnable[1]);
	DDX_Control(pDX, IDC_COMBO_PICCNT1, m_CComboBoxGroupSnapNum[0]);
	DDX_Control(pDX, IDC_COMBO_PICCNT2, m_CComboBoxGroupSnapNum[1]);
}

BOOL CLS_Shdb::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	SetUIText();
	/*UpdateParam();*/
	return TRUE;  
}

void CLS_Shdb::SetUIText()
{
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("Building Intercom")), 0);
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("perimeter alarm")), 1);
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("Network Alarm")), 2);
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("local alarm")), 3);
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("Video Security")), 4);
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("Access Control")), 5);
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("Electronic Patrol System")), 6);
	m_cmobo_repairsysyname.SetItemData(m_cmobo_repairsysyname.AddString(_T("Other security system")), 7);
	m_cmobo_repairsysyname.SetCurSel(0);

	m_combo_repairtype.SetItemData(m_combo_repairtype.AddString(_T("front end")),0);
	m_combo_repairtype.SetItemData(m_combo_repairtype.AddString(_T("terminal")),1);
	m_combo_repairtype.SetItemData(m_combo_repairtype.AddString(_T("System")),2);
	m_combo_repairtype.SetCurSel(0);

	m_combo_maintaintype.SetItemData(m_combo_maintaintype.AddString(_T("normal")),0);
	m_combo_maintaintype.SetItemData(m_combo_maintaintype.AddString(_T("abnormal, fixed")),1);
	m_combo_maintaintype.SetItemData(m_combo_maintaintype.AddString(_T("Exception, not fixed")),2);
	m_combo_maintaintype.SetItemData(m_combo_maintaintype.AddString(_T("not selected")),3);
	m_combo_maintaintype.SetCurSel(0);

	m_combo_mainrepairtype.SetItemData(m_combo_mainrepairtype.AddString(_T("normal")),0);
	m_combo_mainrepairtype.SetItemData(m_combo_mainrepairtype.AddString(_T("abnormal, repaired")),1);
	m_combo_mainrepairtype.SetItemData(m_combo_mainrepairtype.AddString(_T("abnormal, not repaired")),2);
	m_combo_mainrepairtype.SetItemData(m_combo_mainrepairtype.AddString(_T("not selected")),3);
	m_combo_mainrepairtype.SetCurSel(0);

	m_combo_iopertype.SetItemData(m_combo_iopertype.AddString(_T("Test image upload")),0);
	m_combo_iopertype.SetItemData(m_combo_iopertype.AddString(_T("Maintenance picture upload")),1);
	m_combo_iopertype.SetCurSel(0);

	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("00")),0);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("01")),1);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("02")),2);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("03")),3);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("04")),4);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("05")),5);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("10")),10);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("20")),20);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("30")),30);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("40")),40);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("50")),50);
	m_combo_pretm.SetItemData(m_combo_pretm.AddString(_T("60")),60);
	m_combo_pretm.SetCurSel(0);

	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("00")),0);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("01")),1);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("02")),2);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("03")),3);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("04")),4);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("05")),5);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("10")),10);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("20")),20);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("30")),30);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("40")),40);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("50")),50);
	m_combo_delaytm.SetItemData(m_combo_delaytm.AddString(_T("60")),60);
	m_combo_delaytm.SetCurSel(0);

	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("03")),3);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("04")),4);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("05")),5);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("10")),10);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("20")),20);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("30")),30);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("40")),40);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("50")),50);
	m_combo_intervaltm.SetItemData(m_combo_intervaltm.AddString(_T("60")),60);
	m_combo_intervaltm.SetCurSel(0);
	
	//Number of snapshots in the Nth group
	m_CComboBoxGroupSnapNum[0].SetItemData(m_CComboBoxGroupSnapNum[0].AddString(_T("1")),1);
	m_CComboBoxGroupSnapNum[1].SetItemData(m_CComboBoxGroupSnapNum[1].AddString(_T("1")),1);
	m_CComboBoxGroupSnapNum[0].SetCurSel(0);
	m_CComboBoxGroupSnapNum[1].SetCurSel(0);

	//m_CComboBoxGroupSnapNum[1].EnableWindow(FALSE);
	//m_ChkGroupEnable[1].EnableWindow(FALSE);
	//m_CEditStartTime[1].EnableWindow(FALSE);
	//m_CEditEndTime[1].EnableWindow(FALSE);
	UI_UpdateChanCheck();

}

//void CLS_Shdb::UpdateParam()
//{
//	ShdbRunState tPara = {0};
//	tPara.iRunState = 1;
//	tPara.iSize = (int)sizeof(tPara);
//	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SHDB_RUNSTATE, 0, &tPara, (int)sizeof(tPara),0);
//	if (RET_SUCCESS == iRet)
//	{
//		AddLog(LOG_TYPE_SUCC,"","Get Runstate Param Success!LogonID(%d)",m_iLogonID);
//	}
//	else
//	{
//		AddLog(LOG_TYPE_FAIL,"","Get Runstate Param Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
//	}
//	
//}

BEGIN_MESSAGE_MAP(CLS_Shdb, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_REPAIRSYS, &CLS_Shdb::OnBnClickedButton2)
	ON_BN_CLICKED(IDC_BUTTON_MAINTAIN, &CLS_Shdb::OnBnClickedButtonMaintain)
	ON_BN_CLICKED(IDC_BUTTON_TESTMAINTAIN, &CLS_Shdb::OnBnClickedButtonTestmaintain)
	ON_BN_CLICKED(IDC_CHECK_RUNSTATE, &CLS_Shdb::OnBnClickedCheckRunstate)
	ON_BN_CLICKED(IDC_BUTTON_CHECKMANAGE, &CLS_Shdb::OnBnClickedButtonCheckmanage)
	ON_BN_CLICKED(IDC_BUTTON_ALARMPIC, &CLS_Shdb::OnBnClickedButtonAlarmpic)
	ON_BN_CLICKED(IDC_BUTTON_TIMESNAP, &CLS_Shdb::OnBnClickedButtonTimesnap)
END_MESSAGE_MAP()


// CLS_Shdb message handler

void CLS_Shdb::OnBnClickedCheckRunstate()
{
	// TODO: Add control notification handler code here
	memset(&tShdbRunState,0,sizeof(tShdbRunState));
	
	int iState =((CButton *)GetDlgItem(IDC_CHECK_RUNSTATE))->GetCheck();
	tShdbRunState.iRunState = iState;
	tShdbRunState.iSize = sizeof(tShdbRunState);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SHDB_RUNSTATE, 0, &tShdbRunState, sizeof(tShdbRunState));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig][NET_CLIENT_SHDB_RUNSTATE] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig][NET_CLIENT_SHDB_RUNSTATE] Set Success", m_iLogonID);
	}
}

void CLS_Shdb::OnBnClickedButtonAlarmpic()
{
	// TODO: Add control notification handler code here
	int iState =((CButton *)GetDlgItem(IDC_CHECK_ALARMPIC))->GetCheck();
	int iTempPretm = m_combo_pretm.GetItemData(m_combo_pretm.GetCurSel());
	int iTempDelaytm = m_combo_delaytm.GetItemData(m_combo_delaytm.GetCurSel());
	int iTempIntervaltm = m_combo_intervaltm.GetItemData(m_combo_intervaltm.GetCurSel());

	tShdbAlarmPic.iEnable = iState;
	tShdbAlarmPic.iPreTm = iTempPretm;
	tShdbAlarmPic.iDelayTm = iTempDelaytm;
	tShdbAlarmPic.iIntervalTm = iTempIntervaltm;

	int i = 0;
	int iChannelNum = 0;
	memset(&tChanList,0,sizeof(tChanList));
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	tShdbAlarmPic.iChanCount = 0; 
	memset(tShdbAlarmPic.iChanList,0,sizeof(tShdbAlarmPic.iChanList));
	for(i =0; i <iChannelNum; ++i)
	{
		if (m_pclsChanCheck->m_chkChan[i%64].GetCheck() && m_pclsChanCheck->m_iCurrentPage == (i/64))
		{
			tShdbAlarmPic.iChanList[tShdbAlarmPic.iChanCount++] = i;
		}
	}
	if(tShdbAlarmPic.iChanCount == 0)
	{
		MessageBox("Please select a channel number","Set alarm linkage",0);
		return ;
	}
	tShdbAlarmPic.iSize = sizeof(tShdbAlarmPic);
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SHDB_ALARMPIC, 0, &tShdbAlarmPic, sizeof(tShdbAlarmPic));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig][NET_CLIENT_SHDB_ALARMPIC] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig][NET_CLIENT_SHDB_ALARMPIC] Set Success", m_iLogonID);
	}

}


void CLS_Shdb::OnBnClickedButton2()
{

	memset(&tQueryShdbApprepairSys,0,sizeof(tQueryShdbApprepairSys));
	int iTempRepairSysyName =  m_cmobo_repairsysyname.GetItemData(m_cmobo_repairsysyname.GetCurSel());
	tQueryShdbApprepairSys.iRepairName = iTempRepairSysyName;

	int iTempRepairType =  m_combo_repairtype.GetItemData(m_combo_repairtype.GetCurSel());
	tQueryShdbApprepairSys.iRepairType = iTempRepairType;

	if (iTempRepairSysyName == -1 || iTempRepairType == -1)
	{
		MessageBox("Please select an option","Repair system request",0);
		return;
	}

	tQueryShdbApprepairSys.iSize = sizeof(tQueryShdbApprepairSys);
	
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_SHDB_APPREPAIRSYS, 0, &tQueryShdbApprepairSys, sizeof(tQueryShdbApprepairSys));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_APPREPAIRSYS] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_APPREPAIRSYS] Set Success", m_iLogonID);
	}
}


void CLS_Shdb::OnBnClickedButtonMaintain()
{
	// TODO: Add control notification handler code here

	memset(&tQueryShdbServiceType,0,sizeof(tQueryShdbServiceType));
	int iTempMaintainType =  m_combo_maintaintype.GetItemData(m_combo_maintaintype.GetCurSel());
	tQueryShdbServiceType.iMaintainType = iTempMaintainType;
	
	int iTempRepairType =  m_combo_mainrepairtype.GetItemData(m_combo_mainrepairtype.GetCurSel());
	tQueryShdbServiceType.iRepairType = iTempRepairType;
	tQueryShdbServiceType.iSize = sizeof(tQueryShdbServiceType);

	if (iTempMaintainType == -1 || iTempRepairType == -1)
	{
		MessageBox("Please select an option","Maintenance parameters",0);
		return;
	}

	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_SHDB_SERVICETYPE, 0, &tQueryShdbServiceType, sizeof(tQueryShdbServiceType));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_SERVICETYPE] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_SERVICETYPE] Set Success", m_iLogonID);
	}
}

void CLS_Shdb::OnBnClickedButtonTestmaintain()
{
	// TODO: Add control notification handler code here
	memset(&tQueryShdbTestMainTain,0,sizeof(QueryShdbTestMainTain));
	int iTempOperType = m_combo_iopertype.GetItemData(m_combo_iopertype.GetCurSel());
	tQueryShdbTestMainTain.iOperType = iTempOperType;
	if (iTempOperType == -1)
	{
		MessageBox("Please select an option","Test maintenance parameters",0);
		return;
	}
	
	int i = 0;
	int iChannelNum = 0;
	memset(&tChanList,0,sizeof(tChanList));
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	tQueryShdbTestMainTain.iChanCount = 0; 
	for(i =0; i <iChannelNum; ++i)
	{
		if (m_pclsChanCheck->m_chkChan[i%64].GetCheck() && m_pclsChanCheck->m_iCurrentPage == (i/64))
		{
			tChanList[tQueryShdbTestMainTain.iChanCount++].iChanNo = i;
		}
	}
	tQueryShdbTestMainTain.iChanSize = sizeof(ChannelList);
	tQueryShdbTestMainTain.pChanList = tChanList;

	if(tQueryShdbTestMainTain.iChanCount == 0)
	{
		MessageBox("Please select a channel number","Test maintenance parameters",0);
		return ;
	}
	iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_SHDB_TESTMAINTAIN , 0, &tQueryShdbTestMainTain, sizeof(tQueryShdbTestMainTain));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_TESTMAINTAIN ] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_TESTMAINTAIN ] Set Success", m_iLogonID);
	}

}

void CLS_Shdb::OnBnClickedButtonCheckmanage()
{
	// TODO: Add control notification handler code here
	
	CString cPoliceId;
	CString cPassWord;
	m_edit_policeid.GetWindowText(cPoliceId);
	m_edit_password.GetWindowText(cPassWord);
	if (strlen(cPoliceId) > 32 || strlen(cPassWord) > 32)
	{
		MessageBox("The maximum account password is 32 characters","Acceptance management parameters",0);
		return;
	}

	memset(&tQueryShdbCheckManage,0,sizeof(tQueryShdbCheckManage));
	memcpy(tQueryShdbCheckManage.cPoliceId,cPoliceId,32);
	memcpy(tQueryShdbCheckManage.cPasswd,cPassWord,32);
	
	int i = 0;
	int iChannelNum = 0;
	memset(&tChanList,0,sizeof(tChanList));
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	tQueryShdbCheckManage.iChanCount = 0; 
	for(i =0; i <iChannelNum; ++i)
	{
		if (m_pclsChanCheck->m_chkChan[i%64].GetCheck() && m_pclsChanCheck->m_iCurrentPage == (i/64))
		{
			tChanList[tQueryShdbCheckManage.iChanCount++].iChanNo = i;
		}
	}
	tQueryShdbCheckManage.iChanSize = sizeof(ChannelList);
	tQueryShdbCheckManage.pChanList = tChanList;

	if(tQueryShdbCheckManage.iChanCount == 0)
	{
		MessageBox("Please select a channel number","Acceptance management parameters",0);
		return ;
	}
	iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_SHDB_CHECKMANAGE, 0, &tQueryShdbCheckManage, sizeof(tQueryShdbCheckManage));
	if(RET_SUCCESS > iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_CHECKMANAGE] Set fail,error = %d", GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SendCommand][COMMAND_ID_SHDB_CHECKMANAGE] Set Success", m_iLogonID);
	}
}


void CLS_Shdb::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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

	for (int i = 0 ;i < 64; i++)
	{
		m_pclsChanCheck->m_chkChan[i].EnableWindow(FALSE);
	}

	int iChannelNum = 0;
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	for (int i = 0; i < iChannelNum && i < 64; i++)
	{
		m_pclsChanCheck->m_chkChan[i].EnableWindow(TRUE);
	}
	m_pclsChanCheck->m_iChanNum = iChannelNum;
}

void CLS_Shdb::UI_UpdateChanCheck()
{
	if (m_pclsChanCheck == NULL)
	{
		m_pclsChanCheck = new CLS_ChanCheck();
	}

	if (m_pclsChanCheck == NULL)
	{
		return;
	}

	m_pclsChanCheck->Create(IDD_DLG_CFG_CHANNEL_CHECK, this);
	RECT rc = {0};
	GetDlgItem(IDC_STATIC_LIST)->GetClientRect(&rc);
	GetDlgItem(IDC_STATIC_LIST)->ClientToScreen(&rc);
	this->ScreenToClient(&rc);
	m_pclsChanCheck->MoveWindow(&rc);
	m_pclsChanCheck->ShowWindow(TRUE);
}

void CLS_Shdb::OnBnClickedButtonTimesnap()
{
	// TODO: Add control notification handler code here
	int iRet = RET_FAILED;
	memset(&tShdbTimeSnap,0,sizeof(tShdbTimeSnap));
	int iState =((CButton *)GetDlgItem(IDC_CHECK_TIMESNAP))->GetCheck();
	//int iTempSnapGrpNum = m_combo_timesnap.GetItemData(m_combo_timesnap.GetCurSel());
	tShdbTimeSnap.iEnable = iState;
	tShdbTimeSnap.iTmSnapGrpNum = 2;

	int i = 0;
	int j = 0;
	int iChannelNum = 0;
	iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	for(i=0; i<CUR_GROUPNUM; ++i)
	{
		tShdbTimeSnap.tParam[i].iTmEnable = m_ChkGroupEnable[i].GetCheck();
		tShdbTimeSnap.tParam[i].iPicCnt = m_CComboBoxGroupSnapNum[i] .GetItemData(m_CComboBoxGroupSnapNum[i].GetCurSel());
		CString cstrTime = "";
		m_CEditStartTime[i].GetWindowText(cstrTime);
		tShdbTimeSnap.tParam[i].iBeginTm = atoi(cstrTime);
		m_CEditEndTime[i].GetWindowText(cstrTime);
		tShdbTimeSnap.tParam[i].iEndTm = atoi(cstrTime);
		tShdbTimeSnap.tParam[i].iChanCount = 0; 
		for(j =0; j <iChannelNum; ++j)
		{
			if (m_pclsChanCheck->m_chkChan[j%64].GetCheck() && m_pclsChanCheck->m_iCurrentPage == (j/64))
			{
				tShdbTimeSnap.tParam[i].iChanList[tShdbTimeSnap.tParam[i].iChanCount++] = j;
			}
		}
	}
	
	tShdbTimeSnap.iSize = sizeof(tShdbTimeSnap);
	if(tShdbTimeSnap.tParam->iChanCount == 0)
	{
		MessageBox("Please select a channel number","Set daily pictures",0);
		return ;
	}
	iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_SHDB_TIMESNAP,m_iChannelNO,&tShdbTimeSnap,sizeof(tShdbTimeSnap));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_SHDB_TIMESNAP fail!");

	}
	memset(tChanList, 0, 256);

}


void CLS_Shdb::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	int iMsgType = _wParam & 0xFFFF;
	switch(iMsgType)
	{
	case WCM_SHDB_APPREPAIRSYS:
		{
			ShdbApprepairSysResult *pSysResult = (ShdbApprepairSysResult*)_iLParam;
			int iResult = pSysResult->iResult;
			if (iResult == 0)
			{
				MessageBox("Successfully modified","User repair system request",0);
			}
			else if (iResult == -1)
			{
				MessageBox("fail to edit","User repair system request",0);
			}
		}
		break;
	case WCM_SHDB_SERVICETYPE:
		{
			ShdbServiceTypeResult *pServiceType = (ShdbServiceTypeResult*)_iLParam;
			int iResult = pServiceType->iResult;
			if (iResult == 0)
			{
				MessageBox("Successfully modified","Maintenance and repair parameters",0);
			}
			else if (iResult == -1)
			{
				MessageBox("fail to edit","Maintenance and repair parameters",0);
			}
		}
		break;
	case WCM_SHDB_TESTMAINTAIN:
		 {
			 ShdbTestMainTainResult *pTestMaintain = (ShdbTestMainTainResult*)_iLParam;
			 int iResult = pTestMaintain->iResult;
			 if (iResult == 0)
			 {
				 MessageBox("Successfully modified","Test maintenance parameters",0);
			 }
			 else if (iResult == -1)
			 {
				 MessageBox("fail to edit","Test maintenance parameters",0);
			 }
		 }
		 break;
	case WCM_SHDB_CHECKMANAGE:
		{
			ShdbCheckManageResult *pCheckManage = (ShdbCheckManageResult*)_iLParam;
			int iResult = pCheckManage->iResult;
			if (iResult == 0)
			{
				MessageBox("Successfully modified","Acceptance management parameters",0);
			}
			else if (iResult == -1)
			{
				MessageBox("fail to edit","Acceptance management parameters",0);
			}
		}
		break;
	default:
		break;
	}
}

void CLS_Shdb::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	switch(_iParaType)
	{
	case PARA_SHDB_RUNSTATE:
		{
		ShdbRunState tParam = {0};
		int iReturnBytes = -1;
		tParam.iSize = (int)sizeof(tParam);
		int iRet = NetClient_GetDevConfig(_iLogonID, NET_CLIENT_SHDB_RUNSTATE, _iChannelNo, &tParam, tParam.iSize, &iReturnBytes);
		break;
		}
	case PARA_SHDB_ALARMPIC:
		{
 		ShdbAlarmPic tParam = {0};
		int iReturnBytes = -1;
		tParam.iSize = (int)sizeof(tParam);
		int iRet = NetClient_GetDevConfig(_iLogonID, NET_CLIENT_SHDB_ALARMPIC, _iChannelNo, &tParam, tParam.iSize, &iReturnBytes);
		break;
		}
	case PARA_SHDB_TIMESNAP:
		{
		ShdbTimeSnap tParam = {0};
		int iReturnBytes = -1;
		tParam.iSize = (int)sizeof(tParam);
		int iRet = NetClient_GetDevConfig(_iLogonID, NET_CLIENT_SHDB_TIMESNAP, _iChannelNo, &tParam, tParam.iSize, &iReturnBytes);
		int i = 1;
		break;
		}
	default:
		break;
	}
}


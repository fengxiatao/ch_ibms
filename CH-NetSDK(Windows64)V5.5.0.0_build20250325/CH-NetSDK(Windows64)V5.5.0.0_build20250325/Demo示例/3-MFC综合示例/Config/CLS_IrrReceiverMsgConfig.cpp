// CLS_IrrReceiverMsgConfig.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_IrrReceiverMsgConfig.h"


#pragma comment(lib, "Winmm.lib")


// CLS_IrrReceiverMsgConfig 对话框

IMPLEMENT_DYNAMIC(CLS_IrrReceiverMsgConfig, CDialog)

CLS_IrrReceiverMsgConfig::CLS_IrrReceiverMsgConfig(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_IrrReceiverMsgConfig::IDD, pParent)
{
}

CLS_IrrReceiverMsgConfig::~CLS_IrrReceiverMsgConfig()
{
}

void CLS_IrrReceiverMsgConfig::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_PEOPLEMSG, m_lstMessage);
	DDX_Control(pDX, IDC_COMBO_CONTROL, m_ControlCombox);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_TypeCombox);
}


BEGIN_MESSAGE_MAP(CLS_IrrReceiverMsgConfig, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SEARCH, &CLS_IrrReceiverMsgConfig::OnBnClickedButtonSearch)
	ON_BN_CLICKED(IDC_BUTTONSETAUDIO, &CLS_IrrReceiverMsgConfig::OnBnClickedButtonSetaudio)
	ON_BN_CLICKED(IDC_BUTTON_SAVE, &CLS_IrrReceiverMsgConfig::OnBnClickedButtonSave)
	ON_CBN_CLOSEUP(IDC_COMBO_CONTROL, &CLS_IrrReceiverMsgConfig::OnCbnCloseupComboControl)
END_MESSAGE_MAP()

BOOL CLS_IrrReceiverMsgConfig::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	SetDlgItemText(IDC_STATIC_Control,GetTextByLan(_T("操作类型"),_T("Control")));
	SetDlgItemText(IDC_STATIC_PeopleId,GetTextByLan(_T("联系人序号"),_T("PeopleId")));
	SetDlgItemText(IDC_STATIC_NAME,GetTextByLan(_T("联系人姓名"),_T("Name")));
	SetDlgItemText(IDC_STATIC_PHONENUM,GetTextByLan(_T("联系人电话号码"),_T("PhoneNumber")));
	SetDlgItemText(IDC_STATIC_TYPE,GetTextByLan(_T("联系人操作"),_T("Type")));
	SetDlgItemText(IDC_STATIC_PlayAudioCount,GetTextByLan(_T("语音播报次数"),_T("iPlayAudioCount")));
	SetDlgItemText(IDC_BUTTON_SEARCH,GetTextByLan(_T("获取联系人信息"),_T("SearchMessage")));
	SetDlgItemText(IDC_BUTTONSETAUDIO,GetTextByLan(_T("设置语音播报次数"),_T("SetPlayAudioCount")));
	SetDlgItemText(IDC_BUTTON_SAVE,GetTextByLan(_T("保存"),_T("Save")));

	SetDlgItemText(IDC_STATIC_PAGENO,GetTextByLan(_T("页码"),_T("PageNo")));
	SetDlgItemText(IDC_STATIC_PAGESIZE,GetTextByLan(_T("每页条数"),_T("PageSize")));
	SetDlgItemText(IDC_STATIC_PEOPLESTART,GetTextByLan(_T("联系人的开始序号"),_T("PeopleIdStart")));

	m_lstMessage.SetExtendedStyle(m_lstMessage.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT|LVS_EX_CHECKBOXES);
	m_lstMessage.DeleteAllItems();

	int iColumnIndex = 0;
	InsertColumn( m_lstMessage, iColumnIndex++, GetTextByLan(_T("总条数"),_T("Total")), LVCFMT_CENTER, 50 );
	InsertColumn( m_lstMessage, iColumnIndex++,GetTextByLan(_T("页码"),_T("PageNo")), LVCFMT_CENTER, 40 );
	InsertColumn( m_lstMessage, iColumnIndex++, GetTextByLan(_T("条数"),_T("PageSize")), LVCFMT_CENTER, 40 );
	InsertColumn( m_lstMessage, iColumnIndex++, GetTextByLan(_T("页内序号"),_T("Index")), LVCFMT_CENTER, 60 );
	InsertColumn( m_lstMessage, iColumnIndex++,GetTextByLan(_T("联系人序号"),_T("PeopleId")) , LVCFMT_CENTER, 80 );	
	InsertColumn( m_lstMessage, iColumnIndex++, GetTextByLan(_T("姓名"),_T("Name")), LVCFMT_CENTER, 80 );
	InsertColumn( m_lstMessage, iColumnIndex, GetTextByLan(_T("联系人电话号码"),_T("PhoneNumber")), LVCFMT_CENTER, 100 );

	m_ControlCombox.AddString(GetTextByLan(_T("1.添加"),_T("1.Add")));
	m_ControlCombox.AddString(GetTextByLan(_T("2.编辑"),_T("2.Edit")));
	m_ControlCombox.AddString(GetTextByLan(_T("3.删除"),_T("3.Delete")));
	

	m_TypeCombox.AddString(GetTextByLan(_T("打电话"),_T("CallPhone")));
	m_TypeCombox.AddString(GetTextByLan(_T("发短信"),_T("SendMessage")));
	return TRUE;
}


// CLS_IrrReceiverMsgConfig 消息处理程序

void CLS_IrrReceiverMsgConfig::OnBnClickedButtonSearch()
{
	// TODO: 在此添加控件通知处理程序代码
	m_lstMessage.DeleteAllItems();
	IrrReceiverMSGGet startMessage = {0};
	
	CString strTextPageNo;
	GetDlgItemText(IDC_EDIT_PAGENO, strTextPageNo);
	CString strTextPageSize;
	GetDlgItemText(IDC_EDIT_PAGESIZE, strTextPageSize);
	CString strTextPeopleStart;
	GetDlgItemText(IDC_EDIT_PEOPLESTART, strTextPeopleStart);

	if(strTextPageNo.IsEmpty()|| strTextPageSize.IsEmpty() || strTextPeopleStart.IsEmpty())
	{
		MessageBox(GetTextByLan("查询字段为空，请输入","Empty！Please Input Search Message"));
		return;
	}
	else
	{
		startMessage.iPageNo = _ttoi(strTextPageNo);
		startMessage.iPageSize = _ttoi(strTextPageSize);
		startMessage.iPeopleIdStart = _ttoi(strTextPeopleStart);
	}

	IrrReceiverMSGGetResultArr resultMessage = {0};
	
	int iRet = RET_FAILED;
	iRet = NetClient_CmdConfig(m_iLogonID, CMD_IRRRECEIVERMSG_GET, m_iChannelNO, &startMessage, sizeof(startMessage), &resultMessage, sizeof(resultMessage));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","NetClient_CmdConfig[CMD_IRRRECEIVERMSG_GET] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
		m_lstMessage.DeleteAllItems();
		int i = 0;
		for (; i <  resultMessage.tIrrReceiverMSGGetResult[0].iTotal; i++)
		{
			CString strItoString;
			strItoString.Format(_T("%d"),resultMessage.tIrrReceiverMSGGetResult[i].iTotal);
			m_lstMessage.InsertItem(i, strItoString);
			strItoString.Format(_T("%d"),resultMessage.tIrrReceiverMSGGetResult[i].iPageNo);
			m_lstMessage.SetItemText(i, 1, strItoString);
			strItoString.Format(_T("%d"),resultMessage.tIrrReceiverMSGGetResult[i].iPageSize);
			m_lstMessage.SetItemText(i, 2, strItoString);
			strItoString.Format(_T("%d"),resultMessage.tIrrReceiverMSGGetResult[i].iIndex);
			m_lstMessage.SetItemText(i, 3, strItoString);
			strItoString.Format(_T("%d"),resultMessage.tIrrReceiverMSGGetResult[i].iPeopleId);
			m_lstMessage.SetItemText(i, 4, strItoString);
			m_lstMessage.SetItemText(i, 5, resultMessage.tIrrReceiverMSGGetResult[i].cName);
			m_lstMessage.SetItemText(i, 6, resultMessage.tIrrReceiverMSGGetResult[i].cPhoneNumber);
		}
	}
	else
	{
		MessageBox(GetTextByLan(_T("查找失败"),_T("Search fail")));
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig[CMD_IRRRECEIVERMSG_GET] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}


void CLS_IrrReceiverMsgConfig::OnBnClickedButtonSetaudio()
{
	// TODO: 在此添加控件通知处理程序代码
	IrrReceiverCommonMSG tSetCommonMSG = {0};
	IrrReceiverCommonMSG tSetCommonMSGRuselt = {0};
	memset(&tSetCommonMSGRuselt, 0, sizeof(tSetCommonMSGRuselt));
	int iBytesReturned = 0;

	CString strText;
	GetDlgItemText(IDC_EDIT_PLAYAUDIOCOUNT, strText);
	if(strText.IsEmpty())
	{
		MessageBox(GetTextByLan(_T("语音播放次数不得为空"),_T("PlayAudioCount is empty")));
		return;
	}
	tSetCommonMSG.iPlayAudioCount = _ttoi(strText);
	
	int iRet = RET_FAILED;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRRECEIVERCOMMONMSG, m_iChannelNO, &tSetCommonMSG, sizeof(IrrReceiverCommonMSG));
	if(RET_SUCCESS == iRet && tSetCommonMSG.iPlayAudioCount >= 1 && tSetCommonMSG.iPlayAudioCount <= 5)
	{
		iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IRRRECEIVERCOMMONMSG, m_iChannelNO, &tSetCommonMSGRuselt, sizeof(IrrReceiverCommonMSG), &iBytesReturned);
		if(RET_SUCCESS == iRet)
		{
			CString SetCommonMSGRuselt;
			CString SetCommonMSGRuseltEN;
			SetCommonMSGRuselt.Format(_T("设置语音播报次成功,当前语音播报次数:%d"),tSetCommonMSGRuselt.iPlayAudioCount);
			SetCommonMSGRuseltEN.Format(_T("Set PlayAudioCount Success,Current PlayAudioCount:%d"),tSetCommonMSGRuselt.iPlayAudioCount);
			MessageBox(GetTextByLan(SetCommonMSGRuselt,SetCommonMSGRuseltEN));
			AddLog(LOG_TYPE_SUCC, "","NetClient_GetDevConfig[NET_CLIENT_IRRRECEIVERCOMMONMSG] (%d, %d),PlayAudioCount = %d", m_iLogonID, m_iChannelNO, tSetCommonMSGRuselt.iPlayAudioCount);
		}
		else
		{
			MessageBox(GetTextByLan(_T("设置语音播报次数失败"),_T("Set PlayAudioCount fail")));
		}
	}else{
		MessageBox(GetTextByLan(_T("设置语音播报次数失败"),_T("Set PlayAudioCount fail")));
	}
}

void CLS_IrrReceiverMsgConfig::OnBnClickedButtonSave()
{
	// TODO: 在此添加控件通知处理程序代码
	IrrReceiverMSG tSetInfo = {0};
	IrrReceiverMSGResult tSetResult = {0};

	int iGetControlIndex = m_ControlCombox.GetCurSel();
	unsigned int iGetTypeIndex = m_TypeCombox.GetCurSel();

	tSetInfo.iControl = iGetControlIndex+1;
	tSetInfo.iType = iGetTypeIndex+1;
	CString strText;
	GetDlgItemText(IDC_EDIT_NAME, strText);
	size_t len = strlen(strText.GetBuffer());
	AnsiToUTF8(strText.GetBuffer(),strText);
	
	strcpy_s(tSetInfo.cName,strlen(strText.GetBuffer())+1,strText.GetBuffer());

	GetDlgItemText(IDC_EDIT_PHONENUM, strText);
	AnsiToUTF8(strText.GetBuffer(),strText);
	strcpy_s(tSetInfo.cPhoneNumber,strlen(strText.GetBuffer())+1,strText.GetBuffer());

	GetDlgItemText(IDC_EDIT_PEOPLEID, strText);
	tSetInfo.iPeopleId = _ttoi(strText);
	
	int iRet = RET_FAILED;
	iRet = NetClient_CmdConfig(m_iLogonID, CMD_IRRRECEIVERMSG, m_iChannelNO, &tSetInfo, sizeof(tSetInfo), &tSetResult, sizeof(tSetResult));
	if(RET_SUCCESS == iRet && RET_SUCCESS == tSetResult.iResult)
	{
		MessageBox(GetTextByLan(_T("保存成功"),_T("Save success")));
		AddLog(LOG_TYPE_SUCC, "","NetClient_CmdConfig[CMD_IRRRECEIVERMSG] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		MessageBox(GetTextByLan(_T("保存失败"),_T("Save fail")));
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig[CMD_IRRRECEIVERMSG] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_IrrReceiverMsgConfig::OnCbnCloseupComboControl()
{
	// TODO: 在此添加控件通知处理程序代码
	int iGetControlIndex = m_ControlCombox.GetCurSel();
	
	CEdit* pEditCtrlName = (CEdit*)GetDlgItem(IDC_EDIT_NAME);
	CEdit* pEditCtrlPhoneNum = (CEdit*)GetDlgItem(IDC_EDIT_PHONENUM);

	if(DELETE_MESSAGE_CONFIG == iGetControlIndex)
	{
		pEditCtrlName->EnableWindow(FALSE);
		pEditCtrlPhoneNum->EnableWindow(FALSE);
	}else{
		pEditCtrlName->EnableWindow(TRUE);
		pEditCtrlPhoneNum->EnableWindow(TRUE);
	}
}

void CLS_IrrReceiverMsgConfig::AnsiToUTF8( const char* _pstrIn,CString &_strOut)
{
	WCHAR* strSrc    = NULL;
	TCHAR* szRes    = NULL;

	int i = MultiByteToWideChar(CP_ACP, 0,_pstrIn, -1, NULL, 0);

	strSrc = new WCHAR[i+1];
	if (NULL == strSrc){
		goto EXITFUNC;
	}
	MultiByteToWideChar(CP_ACP, 0,_pstrIn, -1, strSrc, i);

	i = WideCharToMultiByte(CP_UTF8, 0, strSrc, -1, NULL, 0, NULL, NULL);

	szRes = new TCHAR[i+1];
	if (szRes == NULL){
		delete[] strSrc;
		goto EXITFUNC;
	}
	WideCharToMultiByte(CP_UTF8, 0, strSrc, -1, szRes, i, NULL, NULL);

	_strOut = szRes;

	delete[] strSrc;
	delete[] szRes;
EXITFUNC:
	return;
}

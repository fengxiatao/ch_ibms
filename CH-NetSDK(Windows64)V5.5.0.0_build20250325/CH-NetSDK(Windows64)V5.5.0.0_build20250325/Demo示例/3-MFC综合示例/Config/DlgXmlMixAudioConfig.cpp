// DlgXmlMixAudioConfig.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgXmlMixAudioConfig.h"

// 枚举“混音输出名称”，与下标有一一对应关系，访问时使用下标获取
const char g_cOutputNameArr[OUTPUTNAME_NUM][LEN_32] = { "local", "hard", "localChn", "vcChn", "hdmi1", "hdmi2" };
// 枚举“混音输入源名称”，与下标有一一对应关系，访问时使用下标获取
const char g_cSrcNameArr[SRCNAME_NUM][LEN_32] = { "local", "remoteAll", "remote1", "remote2", "remote3", "remote4", "micAll", "mic1", "mic3", "mic5", "mic7", "proof", "exp1", "exp2", "exp3", "exp4", "expOthr" };
// DlgXmlMixAudioConfig 对话框

IMPLEMENT_DYNAMIC(DlgXmlMixAudioConfig, CDialog)

DlgXmlMixAudioConfig::DlgXmlMixAudioConfig(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(DlgXmlMixAudioConfig::IDD, pParent)
{
	memset(m_blSendArr, 0, sizeof(m_blSendArr));
	memset(&m_tConfigList, 0, sizeof(XmlMixAudioConfigList));
}

DlgXmlMixAudioConfig::~DlgXmlMixAudioConfig()
{
}

// 初始化界面
BOOL DlgXmlMixAudioConfig::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	// 设置列表风格
	m_listGetMixAudioConfig.SetExtendedStyle(LVS_EX_FULLROWSELECT | LVS_EX_HEADERDRAGDROP | LVS_EX_GRIDLINES);
	InitCombox();
	UI_UpdateUIText();

	// 混音输出名称 初始化默认显示第一项
	LoadCurrentState(0);
	GetMixAudioConfig();
	return TRUE;
}

// 更新当前选中的通道
void DlgXmlMixAudioConfig::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNO = _iChannelNo;
	GetMixAudioConfig();
}

// 更新当前的显示语言
void DlgXmlMixAudioConfig::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	GetMixAudioConfig();
}

// 更新界面显示文本
void DlgXmlMixAudioConfig::UI_UpdateUIText()
{
	while(m_listGetMixAudioConfig.DeleteColumn(0));
	m_listGetMixAudioConfig.InsertColumn(0, GetTextByLan(_T("预留"), _T("Reserve")), LVCFMT_CENTER, 0);
	m_listGetMixAudioConfig.InsertColumn(1, GetTextByLan(_T("混音输出名称"), _T("Mix output name")), LVCFMT_CENTER, 200);
	m_listGetMixAudioConfig.InsertColumn(2, GetTextByLan(_T("混音输入源名称"), _T("Mix input source name")), LVCFMT_CENTER, 200);
	m_listGetMixAudioConfig.InsertColumn(3, GetTextByLan(_T("使能"), _T("Enable")), LVCFMT_CENTER, 200);
	m_listGetMixAudioConfig.DeleteColumn(0);

	SetDlgItemText(IDC_STATIC_OUTPUT_NAME, GetTextByLan(_T("混音输出名称"), _T("MixAudioOutputName")));
	SetDlgItemText(IDC_BUTTON_SET_MIXAUDIO, GetTextByLan(_T("设置"), _T("Set")));
}

// 初始化下拉框内容
void DlgXmlMixAudioConfig::InitCombox()
{
	// 混音输出名称
	m_cboOutputName.AddString(_T("local"));
	m_cboOutputName.AddString(_T("hard"));
	m_cboOutputName.AddString(_T("localChn"));
	m_cboOutputName.AddString(_T("vcChn"));
	m_cboOutputName.AddString(_T("hdmi1"));
	m_cboOutputName.AddString(_T("hdmi2"));
	m_cboOutputName.SetCurSel(0);

	// 每项混音输入源-使能
	for(int i = 0; i < SRCNAME_NUM; ++i)
	{
		m_cboList[i].AddString(_T("false"));
		m_cboList[i].AddString(_T("true"));
		m_cboList[i].SetCurSel(0);
	}
}

// 更新“是否发送”的状态
void DlgXmlMixAudioConfig::WhetherSend(int _iIndex)
{
	BOOL blCur = FALSE;
	if(1 == m_chkList[_iIndex].GetCheck())
	{
		blCur = TRUE;
	}
	// 根据checkBox选中状态，控制combox是否置灰
	m_cboList[_iIndex].EnableWindow(blCur);
	m_blSendArr[m_cboOutputName.GetCurSel()][_iIndex] = blCur;
}

// 更新“使能”参数的值
void DlgXmlMixAudioConfig::ChangEnable(int _iIndex)
{
	m_tConfigList.tMixAudioConfig[m_cboOutputName.GetCurSel()].tAudioSrcName[_iIndex].iEnabled = m_cboList[_iIndex].GetCurSel();
}

// 显示当前“混音输出名称”对应的“混音输入源名称、使能、是否发送”的状态
void DlgXmlMixAudioConfig::LoadCurrentState(int _iIndex)
{
	for(int i = 0; i < SRCNAME_NUM; ++i)
	{
		m_cboList[i].SetCurSel(m_tConfigList.tMixAudioConfig[_iIndex].tAudioSrcName[i].iEnabled);
		m_chkList[i].SetCheck(m_blSendArr[_iIndex][i]);
		m_cboList[i].EnableWindow(m_blSendArr[_iIndex][i]);
	}
}

// 获取 设备当前混音输入源的配置信息
void DlgXmlMixAudioConfig::GetMixAudioConfig()
{
	m_listGetMixAudioConfig.DeleteAllItems();
	XmlMixAudioConfigList tInfo;
	memset(&tInfo, 0, sizeof(XmlMixAudioConfigList));
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_CMD_MIXAUDIOCONFIG, NULL, 0, &tInfo, sizeof(XmlMixAudioConfigList));
	if(RET_SUCCESS == iRet)
	{
		// 获取结果依次插入到列表中显示
		int iCurLine = 0;
		CString strEnabled;
		for(int i = 0; i < tInfo.iAudioConfigNum && i < MAX_MIXAUDIOCONFIG_NUM; ++i)
		{
			for(int j = 0; j < tInfo.tMixAudioConfig[i].iAudioSrcNum && j < MAX_MIXAUDIOSRC_NUM; ++j)
			{
				strEnabled.Empty();
				if(1 == tInfo.tMixAudioConfig[i].tAudioSrcName[j].iEnabled)
				{
					strEnabled.Format(_T("true"));
				}
				else
				{
					strEnabled.Format(_T("false"));
				}
				m_listGetMixAudioConfig.InsertItem(iCurLine, tInfo.tMixAudioConfig[i].cOutPutName);
				m_listGetMixAudioConfig.SetItemText(iCurLine, 1, tInfo.tMixAudioConfig[i].tAudioSrcName[j].cAudioSrcName);
				m_listGetMixAudioConfig.SetItemText(iCurLine, 2, strEnabled);
				++iCurLine;
			}
		}
		AddLog(LOG_TYPE_SUCC, "","DlgXmlMixAudioConfig::NetClient_XmlGetDevConfig[NETXMLCFG_CMD_MIXAUDIOCONFIG] (%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","DlgXmlMixAudioConfig::NetClient_XmlGetDevConfig[NETXMLCFG_CMD_MIXAUDIOCONFIG] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}

// 控件绑定变量
void DlgXmlMixAudioConfig::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_GET_MIXAUDIO_CONFIG, m_listGetMixAudioConfig);
	DDX_Control(pDX, IDC_COMBO_OUTPUT_NAME, m_cboOutputName);
	for(int i = 0; i < SRCNAME_NUM; ++i)
	{
		// 变量绑定comBox控件
		DDX_Control(pDX, IDC_COMBO_SRC_LOCAL + i, m_cboList[i]);

		// 变量绑定checkBox控件
		DDX_Control(pDX, IDC_CHECK_SRC_LOCAL + i, m_chkList[i]);
	}
}


BEGIN_MESSAGE_MAP(DlgXmlMixAudioConfig, CDialog)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_LOCAL, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcLocal)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_REMOTEALL, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemoteall)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_REMOTE1, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote1)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_REMOTE2, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote2)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_REMOTE3, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote3)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_REMOTE4, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote4)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_MICALL, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMicall)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_MIC1, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic1)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_MIC3, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic3)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_MIC5, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic5)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_MIC7, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic7)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_PROOF, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcProof)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_EXP1, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp1)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_EXP2, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp2)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_EXP3, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp3)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_EXP4, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp4)
	ON_CBN_SELCHANGE(IDC_COMBO_SRC_EXPOTHR, &DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExpothr)
	ON_BN_CLICKED(IDC_CHECK_SRC_LOCAL, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcLocal)
	ON_BN_CLICKED(IDC_CHECK_SRC_REMOTEALL, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemoteall)
	ON_BN_CLICKED(IDC_CHECK_SRC_REMOTE1, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote1)
	ON_BN_CLICKED(IDC_CHECK_SRC_REMOTE2, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote2)
	ON_BN_CLICKED(IDC_CHECK_SRC_REMOTE3, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote3)
	ON_BN_CLICKED(IDC_CHECK_SRC_REMOTE4, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote4)
	ON_BN_CLICKED(IDC_CHECK_SRC_MICALL, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcMicall)
	ON_BN_CLICKED(IDC_CHECK_SRC_MIC1, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic1)
	ON_BN_CLICKED(IDC_CHECK_SRC_MIC3, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic3)
	ON_BN_CLICKED(IDC_CHECK_SRC_MIC5, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic5)
	ON_BN_CLICKED(IDC_CHECK_SRC_MIC7, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic7)
	ON_BN_CLICKED(IDC_CHECK_SRC_PROOF, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcProof)
	ON_BN_CLICKED(IDC_CHECK_SRC_EXP1, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp1)
	ON_BN_CLICKED(IDC_CHECK_SRC_EXP2, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp2)
	ON_BN_CLICKED(IDC_CHECK_SRC_EXP3, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp3)
	ON_BN_CLICKED(IDC_CHECK_SRC_EXP4, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp4)
	ON_BN_CLICKED(IDC_CHECK_SRC_EXPOTHR, &DlgXmlMixAudioConfig::OnBnClickedCheckSrcExpothr)
	ON_BN_CLICKED(IDC_BUTTON_SET_MIXAUDIO, &DlgXmlMixAudioConfig::OnBnClickedButtonSetMixaudio)
	ON_CBN_SELCHANGE(IDC_COMBO_OUTPUT_NAME, &DlgXmlMixAudioConfig::OnCbnSelchangeComboOutputName)
END_MESSAGE_MAP()


// DlgXmlMixAudioConfig 消息处理程序

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcLocal()
{
	ChangEnable(0);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemoteall()
{
	ChangEnable(1);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote1()
{
	ChangEnable(2);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote2()
{
	ChangEnable(3);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote3()
{
	ChangEnable(4);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcRemote4()
{
	ChangEnable(5);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMicall()
{
	ChangEnable(6);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic1()
{
	ChangEnable(7);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic3()
{
	ChangEnable(8);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic5()
{
	ChangEnable(9);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcMic7()
{
	ChangEnable(10);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcProof()
{
	ChangEnable(11);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp1()
{
	ChangEnable(12);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp2()
{
	ChangEnable(13);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp3()
{
	ChangEnable(14);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExp4()
{
	ChangEnable(15);
}

void DlgXmlMixAudioConfig::OnCbnSelchangeComboSrcExpothr()
{
	ChangEnable(16);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcLocal()
{
	WhetherSend(0);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemoteall()
{
	WhetherSend(1);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote1()
{
	WhetherSend(2);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote2()
{
	WhetherSend(3);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote3()
{
	WhetherSend(4);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcRemote4()
{
	WhetherSend(5);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcMicall()
{
	WhetherSend(6);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic1()
{
	WhetherSend(7);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic3()
{
	WhetherSend(8);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic5()
{
	WhetherSend(9);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcMic7()
{
	WhetherSend(10);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcProof()
{
	WhetherSend(11);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp1()
{
	WhetherSend(12);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp2()
{
	WhetherSend(13);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp3()
{
	WhetherSend(14);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcExp4()
{
	WhetherSend(15);
}

void DlgXmlMixAudioConfig::OnBnClickedCheckSrcExpothr()
{
	WhetherSend(16);
}

// 设置按钮的点击事件
void DlgXmlMixAudioConfig::OnBnClickedButtonSetMixaudio()
{
	XmlMixAudioConfigList tInfo;
	memset(&tInfo, 0, sizeof(XmlMixAudioConfigList));

	// 将要发送的信息依次插入结构体中
	int &iOutputCount = tInfo.iAudioConfigNum;
	for(int i = 0; i < OUTPUTNAME_NUM; ++i)
	{
		// 遍历混音输出名称
		int &iSrcNameCount = tInfo.tMixAudioConfig[iOutputCount].iAudioSrcNum;
		for(int j = 0; j < SRCNAME_NUM; ++j)
		{
			// 遍历混音输入源名称
			if(m_blSendArr[i][j])
			{
				// 勾选了“是否发送”的复选框才存，并且计数
				tInfo.tMixAudioConfig[iOutputCount].tAudioSrcName[iSrcNameCount].iEnabled = m_tConfigList.tMixAudioConfig[i].tAudioSrcName[j].iEnabled;
				memcpy(tInfo.tMixAudioConfig[iOutputCount].tAudioSrcName[iSrcNameCount].cAudioSrcName, g_cSrcNameArr[j], LEN_32);
				++iSrcNameCount;
			}
		}
		// 当前混音输入源名称下有设置项，才计数
		if(iSrcNameCount > 0)
		{
			memcpy(tInfo.tMixAudioConfig[iOutputCount].cOutPutName, g_cOutputNameArr[i], LEN_32);
			++iOutputCount;
		}
	}

	// 调试sdk功能的临时代码
	//tInfo.iAudioConfigNum = 2;

	//tInfo.tMixAudioConfig[0].iAudioSrcNum = 2;
	//memcpy(tInfo.tMixAudioConfig[0].cOutPutName, "hard", 4);
	//tInfo.tMixAudioConfig[0].tAudioSrcName[0].iEnabled = 1;
	//tInfo.tMixAudioConfig[0].tAudioSrcName[1].iEnabled = 0;
	//memcpy(tInfo.tMixAudioConfig[0].tAudioSrcName[0].cAudioSrcName, "local", 5);
	//memcpy(tInfo.tMixAudioConfig[0].tAudioSrcName[1].cAudioSrcName, "remote1", 7);

	//tInfo.tMixAudioConfig[1].iAudioSrcNum = 3;
	//memcpy(tInfo.tMixAudioConfig[1].cOutPutName, "localChn", 8);
	//tInfo.tMixAudioConfig[1].tAudioSrcName[0].iEnabled = 1;
	//tInfo.tMixAudioConfig[1].tAudioSrcName[1].iEnabled = 1;
	//tInfo.tMixAudioConfig[1].tAudioSrcName[2].iEnabled = 1;
	//memcpy(tInfo.tMixAudioConfig[1].tAudioSrcName[0].cAudioSrcName, "local", 5);
	//memcpy(tInfo.tMixAudioConfig[1].tAudioSrcName[1].cAudioSrcName, "remote1", 7);
	//memcpy(tInfo.tMixAudioConfig[1].tAudioSrcName[2].cAudioSrcName, "micAll", 6);

	int iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_CMD_MIXAUDIOCONFIG, &tInfo, sizeof(XmlMixAudioConfigList), NULL, 0);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","DlgXmlMixAudioConfig::NetClient_XmlSetDevConfig[NETXMLCFG_CMD_MIXAUDIOCONFIG] (%d)", m_iLogonID);
		GetMixAudioConfig();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","DlgXmlMixAudioConfig::NetClient_XmlSetDevConfig[NETXMLCFG_CMD_MIXAUDIOCONFIG] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}

// 选中的混音输出名称发生变化的事件
void DlgXmlMixAudioConfig::OnCbnSelchangeComboOutputName()
{
	LoadCurrentState(m_cboOutputName.GetCurSel());
}

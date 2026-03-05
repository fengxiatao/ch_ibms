// CLS_FtpUplaod.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_FtpUplaod.h"
#include "shlwapi.h"


// CLS_FtpUplaod dialog
#define FTP_NO_1 1
#define DATATYPE_KAKOU 1
#define DATATYPE_WEIZHANG 2
#define DATATYPE_FACE 3
#define DATATYPE_IRRIGATION 4
#define FTP_TEST 3
#define PANEL_PACK_HEIGHT 12


#define MAX_TEXT_LEN_SERVER_PORT 5
#define DIRECTORY_NULL 0				//null
#define DIRECTORY_DEVICE_NO 1			//device ID
#define DIRECTORY_DEVICE_IP 2			//device IP
#define DIRECTORY_CROSS_NO	 3			//crossing number
#define DIRECTORY_CROSS_NAME 4			//crossing name
#define DIRECTORY_DATE_MONTH 5			//time (year month)
#define DIRECTORY_DATE_DAY 6			//time (year month day)
#define DIRECTORY_CHANNEL_NAME 10		//channel name
#define DIRECTORY_CHANNEL_NO 11			//channel number
#define DIRECTORY_USER_DEFINE 65535		//customize
#define PIC_USER_DEFINE 65535		//customize

#define DIRECTORY_LEVEL_MIN 0
#define DIRECTORY_LEVEL_ROOT (DIRECTORY_LEVEL_MIN+0)
#define DIRECTORY_LEVEL_1 (DIRECTORY_LEVEL_MIN+1)
#define DIRECTORY_LEVEL_2 (DIRECTORY_LEVEL_MIN+2)
#define DIRECTORY_LEVEL_3 (DIRECTORY_LEVEL_MIN+3)
#define DIRECTORY_LEVEL_4 (DIRECTORY_LEVEL_MIN+4)
#define DIRECTORY_LEVEL_MAX (DIRECTORY_LEVEL_MIN+5)

#define MAX_PORT 65535

#define MAX_NAME_COUNT 4


IMPLEMENT_DYNAMIC(CLS_FtpUpload, CDialog)


CLS_FtpUpload::CLS_FtpUpload(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_FtpUpload::IDD, pParent)
{

}

CLS_FtpUpload::~CLS_FtpUpload()
{
}

void CLS_FtpUpload::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	int iLevel = 0;
	DDX_Control(pDX, IDC_CMB_DATATYPE, m_cboDataType);
	DDX_Control(pDX, IDC_CMB_ROOT, m_cboDirectoryStructure);
	DDX_Control(pDX, IDC_STATIC_FIRST_DIR, m_stcDirectory[iLevel]);
	DDX_Control(pDX, IDC_CMB_FIRST, m_cboDirectory[iLevel]);
	DDX_Control(pDX, IDC_STATIC_FIRST_DIR_NAME, m_stcDirectoryName[iLevel]);
	DDX_Control(pDX, IDC_EDIT_FIRST_DIR_NAME, m_edtDirectoryName[iLevel++]);
	DDX_Control(pDX, IDC_STATIC_SEC_DIR, m_stcDirectory[iLevel]);
	DDX_Control(pDX, IDC_CMB_SECOND, m_cboDirectory[iLevel]);
	DDX_Control(pDX, IDC_STATIC_SEC_DIR_NAME, m_stcDirectoryName[iLevel]);
	DDX_Control(pDX, IDC_EDIT_SEC_DIR_NAME, m_edtDirectoryName[iLevel++]);
	DDX_Control(pDX, IDC_STATIC_THIRD_DIR, m_stcDirectory[iLevel]);
	DDX_Control(pDX, IDC_CMB_THIRD, m_cboDirectory[iLevel]);
	DDX_Control(pDX, IDC_STATIC_THIRD_DIR_NAME, m_stcDirectoryName[iLevel]);
	DDX_Control(pDX, IDC_EDIT_THIRD_DIR_NAME, m_edtDirectoryName[iLevel++]);

	DDX_Control(pDX, IDC_STATIC_FORTH_DIR, m_stcDirectory[iLevel]);
	DDX_Control(pDX, IDC_CMB_FORTH, m_cboDirectory[iLevel]);
	DDX_Control(pDX, IDC_STATIC_FORTH_DIR_NAME, m_stcDirectoryName[iLevel]);
	DDX_Control(pDX, IDC_EDIT_FORTH_DIR_NAME, m_edtDirectoryName[iLevel++]);

	DDX_Control(pDX, IDC_IPADDRESS_SERVER, m_IPAddr);
	DDX_Control(pDX, IDC_EDIT_PORT, m_edtPort);
	DDX_Control(pDX, IDC_EDIT_USERNAME, m_edtUser);
	DDX_Control(pDX, IDC_EDIT_PWD, m_edtPwd);
	DDX_Control(pDX, IDC_EDIT_ROOT_DIRECTROY_NAME, m_edtRootDirectory);
	DDX_Control(pDX, IDC_CHECK_ENABLED, m_chkEnable);
	DDX_Control(pDX, IDC_CHK_FACE_UPLOAD, m_chkFaceUpload);
	DDX_Control(pDX, IDC_EDIT_PIC_SEPARATOR, m_editPicSeparator);
	DDX_Control(pDX, IDC_CHK_ILLEGAL_VIDEO_UPLOAD, m_chkIllegalVideoUpload);
	DDX_Control(pDX, IDC_CBO_NAMEINDEX, m_cbo_NameIndex);
	DDX_Control(pDX, IDC_EDT_NAME1, m_edt_namedef[0]);
	DDX_Control(pDX, IDC_EDT_NAME2, m_edt_namedef[1]);
	DDX_Control(pDX, IDC_EDT_NAME3, m_edt_namedef[2]);

	DDX_Control(pDX, IDC_CBO_NAMETYPE1, m_cbo_NameType[0]);
	DDX_Control(pDX, IDC_CBO_NAMETYPE2, m_cbo_NameType[1]);
	DDX_Control(pDX, IDC_CBO_NAMETYPE3, m_cbo_NameType[2]);
	DDX_Control(pDX, IDC_STC_NOTIFY, m_stc_Notify);
	DDX_Control(pDX, IDC_CMB_ENCODETYPE, m_cbEncodeType);
}


BEGIN_MESSAGE_MAP(CLS_FtpUpload, CDialog)
	ON_BN_CLICKED(IDC_CHECK_ENABLED, &CLS_FtpUpload::OnBnClickedCheckEnabled)
	ON_BN_CLICKED(IDC_BUTTON_SAVE, &CLS_FtpUpload::OnBnClickedButtonSave)
	ON_BN_CLICKED(IDC_BUTTON_FTP_TEST, &CLS_FtpUpload::OnBnClickedButtonFtpTest)
	ON_CBN_SELCHANGE(IDC_CMB_ROOT, &CLS_FtpUpload::OnCbnSelchangeCmbRoot)
	ON_CBN_SELCHANGE(IDC_CMB_FIRST, &CLS_FtpUpload::OnCbnSelchangeCmbFirst)
	ON_CBN_SELCHANGE(IDC_CMB_SECOND, &CLS_FtpUpload::OnCbnSelchangeCmbSecond)
	ON_CBN_SELCHANGE(IDC_CMB_THIRD, &CLS_FtpUpload::OnCbnSelchangeCmbThird)
	ON_WM_SHOWWINDOW()
	ON_WM_CTLCOLOR()
	ON_CBN_SELCHANGE(IDC_CBO_NAMEINDEX, &CLS_FtpUpload::OnCbnSelchangeCboNameindex)
	ON_STN_CLICKED(IDC_STATIC_NAMETYPE1, &CLS_FtpUpload::OnStnClickedStaticNametype1)
	ON_CBN_SELCHANGE(IDC_CBO_NAMETYPE1, &CLS_FtpUpload::OnCbnSelchangeCboNametype1)
	ON_CBN_SELCHANGE(IDC_CBO_NAMETYPE2, &CLS_FtpUpload::OnCbnSelchangeCboNametype2)
	ON_CBN_SELCHANGE(IDC_CBO_NAMETYPE3, &CLS_FtpUpload::OnCbnSelchangeCboNametype3)
	ON_CBN_SELCHANGE(IDC_CMB_FORTH, &CLS_FtpUpload::OnCbnSelchangeCmbForth)
END_MESSAGE_MAP()

void CLS_FtpUpload::OnBnClickedCheckEnabled()
{
	UpdateEnable();
}

void CLS_FtpUpload::UpdateEnable()
{
	BOOL bEnable = FALSE;
	if(m_chkEnable.GetCheck())
	{
		bEnable = TRUE;
	}
	m_IPAddr.EnableWindow(bEnable);
	m_edtPort.EnableWindow(bEnable);
	m_edtUser.EnableWindow(bEnable);
	m_edtPwd.EnableWindow(bEnable);
	m_edtRootDirectory.EnableWindow(bEnable);
	m_cboDirectoryStructure.EnableWindow(bEnable);
	m_cboDataType.EnableWindow(bEnable);
	for (int i = 0; i < DIRECTORY_NUM; ++i)
	{
		m_cboDirectory[i].EnableWindow(bEnable);
		m_edtDirectoryName[i].EnableWindow(bEnable);
	}
	GetDlgItem(IDC_CHK_FACE_UPLOAD)->EnableWindow(bEnable);
	GetDlgItem(IDC_BUTTON_FTP_TEST)->EnableWindow(bEnable);
	GetDlgItem(IDC_CHK_ILLEGAL_VIDEO_UPLOAD)->EnableWindow(bEnable);
	GetDlgItem(IDC_EDIT_PIC_SEPARATOR)->EnableWindow(bEnable);
}

BOOL CLS_FtpUpload::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	for (int i = 0;i < DIRECTORY_NUM; i++)
	{
		m_edtDirectoryName[i].setFilter(FILTERKFK_CONFIG, true, false);
	}
	for (int i = 0; i < 3; i++)
	{
		m_edt_namedef[i].setFilter(FILTERKFK_CONFIG, true, false);
	}
	UpdateUI();
	UpdateEnable();

	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_FtpUpload::UpdateUI()
{
	int iIndex = 0;
	SetDlgItemText(IDC_CHK_FACE_UPLOAD, GetTextByLan(_T("上传背景图"), _T("UploadBackground")));
	SetDlgItemText(IDC_CHECK_ENABLED, GetTextByLan(_T("启用"), _T("Enable")));
	SetDlgItemText(IDC_STATIC_DATA_TYPE, GetTextByLan(_T("数据类型"), _T("DataType")));
	SetDlgItemText(IDC_STATIC_SERVER_IP, GetTextByLan(_T("服务器地址"), _T("ServerIp")));
	SetDlgItemText(IDC_STATIC_SERVER_PORT, GetTextByLan(_T("端口"), _T("Port")));
	SetDlgItemText(IDC_STATIC_FTP_USER_NAME, GetTextByLan(_T("用户名"), _T("UserName")));
	SetDlgItemText(IDC_STATIC_FTP_PASSWORD, GetTextByLan(_T("密码"), _T("Password")));
	SetDlgItemText(IDC_STATIC_FTP_PATH, GetTextByLan(_T("路径"), _T("Path")));
	SetDlgItemText(IDC_STATIC_Dir, GetTextByLan(_T("目录结构"), _T("Dir")));
	SetDlgItemText(IDC_STATIC_FIRST_DIR, GetTextByLan(_T("一级目录"), _T("FirstDir")));
	SetDlgItemText(IDC_STATIC_FIRST_DIR_NAME, GetTextByLan(_T("自定义名称"), _T("FirstName")));
	SetDlgItemText(IDC_STATIC_SEC_DIR, GetTextByLan(_T("二级目录"), _T("SecDir")));
	SetDlgItemText(IDC_STATIC_SEC_DIR_NAME, GetTextByLan(_T("自定义名称"), _T("SecName")));
	SetDlgItemText(IDC_STATIC_THIRD_DIR, GetTextByLan(_T("三级目录"), _T("ThirdDir")));
	SetDlgItemText(IDC_STATIC_THIRD_DIR_NAME, GetTextByLan(_T("自定义名称"), _T("ThirdName")));
	SetDlgItemText(IDC_STATIC_FORTH_DIR, GetTextByLan(_T("四级目录"), _T("FourDir")));
	SetDlgItemText(IDC_STATIC_FORTH_DIR_NAME, GetTextByLan(_T("自定义名称"), _T("FourName")));
	SetDlgItemText(IDC_BUTTON_SAVE, GetTextByLan(_T("保存"), _T("Save")));
	SetDlgItemText(IDC_BUTTON_FTP_TEST, GetTextByLan(_T("测试"), _T("Test")));

	//Initialize the data type
	m_cboDataType.ResetContent();
	iIndex = m_cboDataType.AddString(GetTextByLan(_T("保留"), _T("Reserve")));
	m_cboDataType.SetItemData(iIndex,0);
	iIndex = m_cboDataType.AddString(GetTextByLan(_T("卡口数据"), _T("Bayonet")));
	m_cboDataType.SetItemData(iIndex,DATATYPE_KAKOU);
	iIndex = m_cboDataType.AddString(GetTextByLan(_T("违章数据"), _T("Violation")));
	m_cboDataType.SetItemData(iIndex,DATATYPE_WEIZHANG);
	iIndex = m_cboDataType.AddString(GetTextByLan(_T("人脸数据"), _T("Face")));
	m_cboDataType.SetItemData(iIndex,DATATYPE_FACE);
	iIndex = m_cboDataType.AddString(GetTextByLan(_T("水利"), _T("Irrigation")));
	m_cboDataType.SetItemData(iIndex,DATATYPE_IRRIGATION);
	m_cboDataType.SetCurSel(3);

	m_edtPort.SetLimitText(MAX_TEXT_LEN_SERVER_PORT);
	m_edtUser.SetLimitText(LEN_16-1);
	m_edtPwd.SetLimitText(LEN_16-1);
	m_edtRootDirectory.SetLimitText(LEN_32-1);

	for (int i = 0; i < DIRECTORY_NUM; ++i)
	{
		m_edtDirectoryName[i].SetLimitText(LEN_32-1);
	}
	for (int i = 0; i < 3; ++i)
	{
		m_edt_namedef[i].SetLimitText(LEN_32-1);
	}
	//Initialize the directory structure
	iIndex = 0;
	m_cboDirectoryStructure.ResetContent();
	iIndex = m_cboDirectoryStructure.AddString(GetTextByLan(_T("根目录"), _T("Root")));
	m_cboDirectoryStructure.SetItemData(iIndex,DIRECTORY_LEVEL_ROOT);
	iIndex = m_cboDirectoryStructure.AddString(GetTextByLan(_T("一级目录"), _T("FirstDir")));
	m_cboDirectoryStructure.SetItemData(iIndex,DIRECTORY_LEVEL_1);
	iIndex = m_cboDirectoryStructure.AddString(GetTextByLan(_T("二级目录"), _T("SecDir")));
	m_cboDirectoryStructure.SetItemData(iIndex,DIRECTORY_LEVEL_2);
	iIndex = m_cboDirectoryStructure.AddString(GetTextByLan(_T("三级目录"), _T("ThirdDir")));
	m_cboDirectoryStructure.SetItemData(iIndex,DIRECTORY_LEVEL_3);
	iIndex = m_cboDirectoryStructure.AddString(GetTextByLan(_T("四级目录"), _T("ForthDir")));
	m_cboDirectoryStructure.SetItemData(iIndex,DIRECTORY_LEVEL_4);
	m_cboDirectoryStructure.SetCurSel(0);

	iIndex = 0;
	//Initialize the first level directory
	int iDirectory = 0;
	m_cboDirectory[iDirectory].ResetContent();
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("设备IP"), _T("DeviceIP")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_DEVICE_IP);

	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("设备编号"), _T("DeviceNum")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_DEVICE_NO);

	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("自定义"), _T("Customize")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_USER_DEFINE);
	m_cboDirectory[iDirectory].SetCurSel(0);
	m_cboDirectory[iDirectory].ShowWindow(SW_HIDE);
	m_edtDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectory[iDirectory].ShowWindow(SW_HIDE);


	//Initialize the secondary directory
	++iDirectory;
	m_cboDirectory[iDirectory].ResetContent();
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("通道号"), _T("ChannelNO")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_CHANNEL_NO);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("通道名称"), _T("ChannelName")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_CHANNEL_NAME);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("路口编号"), _T("Crossing number")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_CROSS_NO);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("路口名称"), _T("Crossing name")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_CROSS_NAME);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("自定义"), _T("Customize")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_USER_DEFINE);
	m_cboDirectory[iDirectory].SetCurSel(0);
	m_cboDirectory[iDirectory].ShowWindow(SW_HIDE);
	m_edtDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectory[iDirectory].ShowWindow(SW_HIDE);

	//Initialize the third level directory
	++iDirectory;
	m_cboDirectory[iDirectory].ResetContent();
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("时间（年月）"), _T("Time(Year/month)")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_DATE_MONTH);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("时间（年月日）"), _T("Time(Year/month/day)")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_DATE_DAY);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("自定义"), _T("Customize")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_USER_DEFINE);
	m_cboDirectory[iDirectory].SetCurSel(0);
	m_cboDirectory[iDirectory].ShowWindow(SW_HIDE);
	m_edtDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectory[iDirectory].ShowWindow(SW_HIDE);

	//Initialize the fourth level directory
	++iDirectory;
	m_cboDirectory[iDirectory].ResetContent();
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("设备IP"), _T("DeviceIP")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_DEVICE_IP);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("设备编号"), _T("DeviceNum")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_DEVICE_NO);
	iIndex = m_cboDirectory[iDirectory].AddString(GetTextByLan(_T("自定义"), _T("Customize")));
	m_cboDirectory[iDirectory].SetItemData(iIndex,DIRECTORY_USER_DEFINE);
	m_cboDirectory[iDirectory].SetCurSel(0);
	m_cboDirectory[iDirectory].ShowWindow(SW_HIDE);
	m_edtDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectoryName[iDirectory].ShowWindow(SW_HIDE);
	m_stcDirectory[iDirectory].ShowWindow(SW_HIDE);

	m_editPicSeparator.SetLimitText(1);

	//The previous one is wrong, these two controls are useless
	GetDlgItem(IDC_STATIC_FTP_PATH)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_EDIT_ROOT_DIRECTROY_NAME)->ShowWindow(SW_HIDE);

	//Add custom naming function
	SetDlgItemText(IDC_STATIC_PICLEVEL, GetTextByLan(_T("图片命名级数"), _T("Name def Count")));
	SetDlgItemText(IDC_STATIC_NAMEDEF1, GetTextByLan(_T("自定义命名1"), _T("Name def1")));
	SetDlgItemText(IDC_STATIC_NAMEDEF2, GetTextByLan(_T("自定义命名2"), _T("Name def2")));
	SetDlgItemText(IDC_STATIC_NAMEDEF3, GetTextByLan(_T("自定义命名3"), _T("Name def3")));
	SetDlgItemText(IDC_STATIC_NAMETYPE1, GetTextByLan(_T("命名类型1"), _T("Name type1")));
	SetDlgItemText(IDC_STATIC_NAMETYPE2, GetTextByLan(_T("命名类型2"), _T("Name type2")));
	SetDlgItemText(IDC_STATIC_NAMETYPE3, GetTextByLan(_T("命名类型3"), _T("Name type3")));

	SetDlgItemText(IDC_STATIC_PIC_SEPARATOR, GetTextByLan(_T("图片名称分隔符"), _T("Pic name separator")));

	for (int i = 0; i < MAX_NAME_COUNT ;i++)
	{
		CString strIndex = "";
		strIndex.Format("%d", i);
		m_cbo_NameIndex.SetItemData(m_cbo_NameIndex.AddString(strIndex), i);
	}

	m_cbo_NameIndex.SetCurSel(0);
	for (int k = 0; k < 3; k++)
	{
		m_cbo_NameType[k].SetItemData(m_cbo_NameType[k].AddString(GetTextByLan(_T("设备IP"), _T("Device IP"))), 2);
		m_cbo_NameType[k].SetItemData(m_cbo_NameType[k].AddString(GetTextByLan(_T("抓拍时间"), _T("Time"))), 5);
		m_cbo_NameType[k].SetItemData(m_cbo_NameType[k].AddString(GetTextByLan(_T("自定义"), _T("user define"))), 65535);
		m_cbo_NameType[k].SetCurSel(0);
	}

	OnCbnSelchangeCboNameindex();
	m_stc_Notify.SetWindowText(GetTextByLan(_T("注：自定义目录以及图片名称不支持汉语"),_T("Notice:User Define Directory and Pic Name not support our language")));

	SetDlgItemText(IDC_STATIC_ENCODETYPE, GetTextByLan(_T("路径编码方式"), _T("Encode Type")));
	m_cbEncodeType.Clear();
	m_cbEncodeType.SetItemData(m_cbEncodeType.AddString(GetTextByLan(_T("GB2312"), _T("GB2312"))), 0);
	m_cbEncodeType.SetItemData(m_cbEncodeType.AddString(GetTextByLan(_T("UTF-8"), _T("UTF-8"))), 1);
	m_cbEncodeType.SetCurSel(0);
}

void CLS_FtpUpload::OnLanguageChanged(int _iLanguage)
{
	UpdateUI();
}

void CLS_FtpUpload::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else
	{
		m_iLogonID = _iLogonID;
	}
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;    
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	UpdateFtpInfo();
}

void CLS_FtpUpload::OnBnClickedButtonSave()
{
	int iRet = RET_FAILED;
	FtpUpload tParam = {0};
	CString cstrIP;
	CString cstrUserName;
	CString cstrPwd;
	CString cstrDirectoryName;
	CString cstrMsg;
	CString cstrPicSeparator;
	int iPort = 0;
	int iEncodeType = 0;
	CString cstrType;

	if(FALSE == CheckFtpInfo(cstrIP,iPort,cstrUserName,cstrPwd,cstrMsg))
	{
		goto EXIT;
	}

	tParam.iSize = sizeof(tParam);
	tParam.iFtpNum = FTP_NO_1;
	tParam.iFtpType = m_cboDataType.GetItemData(m_cboDataType.GetCurSel());
	tParam.iFtpEnable = m_chkEnable.GetCheck();
	memcpy(tParam.cFtpAddr, (LPSTR)(LPCTSTR)cstrIP, (int)sizeof(tParam.cFtpAddr));
	tParam.iPort = iPort;
	memcpy(tParam.cUserName, (LPSTR)(LPCTSTR)cstrUserName, (int)sizeof(tParam.cUserName));
	memcpy(tParam.cPassWord, (LPSTR)(LPCTSTR)cstrPwd, (int)sizeof(tParam.cPassWord));
	tParam.iListCount = m_cboDirectoryStructure.GetItemData(m_cboDirectoryStructure.GetCurSel());
	if (tParam.iListCount > 0)
	{
		tParam.pstDirectory = new PicDirectory[tParam.iListCount];
		if (NULL == tParam.pstDirectory)
		{
			goto EXIT;
		}

		for (int i = 0; i < tParam.iListCount; ++i)
		{
			PicDirectory& tDirectory = tParam.pstDirectory[i];			
			tDirectory.iDirectoryId = m_cboDirectory[i].GetItemData(m_cboDirectory[i].GetCurSel());
			memset(tDirectory.cUserDefine,0,sizeof(tDirectory.cUserDefine));
			if (DIRECTORY_USER_DEFINE == tDirectory.iDirectoryId)
			{
				m_edtDirectoryName[i].GetWindowText(cstrDirectoryName);
				memcpy(tDirectory.cUserDefine,(LPSTR)(LPCTSTR)cstrDirectoryName,(int)sizeof(tDirectory.cUserDefine));
			}			
		}
	}
	tParam.iPicNameCount = m_cbo_NameIndex.GetCurSel();
	int iPicNameCount = min(tParam.iPicNameCount, MAX_NAME_COUNT - 1);
	if (iPicNameCount > 0)
	{
		tParam.pstPicName = new PicName[iPicNameCount];
	}
	for (int iIndex = 0;iIndex < iPicNameCount; iIndex++)
	{
		int iType = m_cbo_NameType[iIndex].GetItemData(m_cbo_NameType[iIndex].GetCurSel());
		PicName& tPicName = tParam.pstPicName[iIndex];
		tPicName.iDirectoryId = iType;
		memset(tPicName.cUserDefine,0,sizeof(tPicName.cUserDefine));
		if (PIC_USER_DEFINE == tPicName.iDirectoryId)
		{
			CString cstrPicName = "";
			m_edt_namedef[iIndex].GetWindowText(cstrPicName);
			memcpy(tPicName.cUserDefine,(LPSTR)(LPCTSTR)cstrPicName,(int)sizeof(tPicName.cUserDefine));
		}
	}
	tParam.iFaceUpload = m_chkFaceUpload.GetCheck();
	tParam.iIllegalVideoUpload = m_chkIllegalVideoUpload.GetCheck()?1:2;
	m_editPicSeparator.GetWindowText(cstrPicSeparator);
	memcpy(tParam.cSeparator,(LPSTR)(LPCTSTR)cstrPicSeparator,sizeof(tParam.cSeparator));
	
	iEncodeType = m_cbEncodeType.GetItemData(m_cbEncodeType.GetCurSel());
	cstrType.Format("%d", iEncodeType);
	memcpy(tParam.cEncodeType, cstrType, sizeof(tParam.cEncodeType));
	iRet = NetClient_SetITSExtraInfo(m_iLogonID, ITS_EXTRAINFO_CMD_FTP_UPLOAD, 0, &tParam, sizeof(FtpUpload));
	int  iTmp = 1; 
	iRet = NetClient_SendCommand(m_iLogonID,COMMAND_ID_SAVECFG, m_iChannelNO, &iTmp, 0);

EXIT:
	// memory release
	if (NULL != tParam.pstDirectory)
	{
		delete tParam.pstDirectory;
		tParam.pstDirectory = NULL;
	}
	if (NULL != tParam.pstPicName)
	{
		delete tParam.pstPicName;
		tParam.pstPicName = NULL;
	}
}

void CLS_FtpUpload::OnBnClickedButtonFtpTest()
{
	CString cstrIP;
	CString cstrUserName;
	CString cstrPwd;
	CString cstrMsg;
	int iPort = 0;
	if(FALSE == CheckFtpInfo(cstrIP,iPort,cstrUserName,cstrPwd,cstrMsg))
	{
		goto EXIT;
	}
	int iRet = NetClient_SetFTPUsage(m_iLogonID,(LPSTR)(LPCTSTR)cstrIP,iPort,"/",(LPSTR)(LPCTSTR)cstrUserName,(LPSTR)(LPCTSTR)cstrPwd, FTP_TEST);
	if (RET_SUCCESS != iRet)
	{

	}
EXIT:
	return;
}

BOOL CLS_FtpUpload::CheckFtpInfo( CString& _cstrIP,int& _iPort,CString& _cstrUserName,CString& _cstrPwd,CString& _cstrMsg )
{
	BOOL bRet = FALSE;
	CString cstrPwdConfirm;

	m_IPAddr.GetWindowText(_cstrIP);
	if(0 == _cstrIP.Compare(_T("0.0.0.0")))
	{
		_cstrMsg = GetTextByLan(_T("请输入正确的IP地址！"), _T("Please enter a valid IP address!"));
		goto EXIT;
	}

	_iPort = GetDlgItemInt(IDC_EDIT_PORT);
	if (_iPort > MAX_PORT || _iPort <= 0)
	{
		_cstrMsg = GetTextByLan(_T("FTP端口号需在1到65535之间!"), _T("FTP port must between 1 and 65535!"));
		goto EXIT;
	}

	m_edtUser.GetWindowText(_cstrUserName);
	if (_cstrUserName.IsEmpty())
	{
		_cstrMsg = GetTextByLan(_T("用户名不能为空！"), _T("User Name can not be empty!"));
		goto EXIT;
	}
	m_edtPwd.GetWindowText(_cstrPwd);
	bRet = TRUE;

EXIT:
	return bRet;
}

void CLS_FtpUpload::UpdateFtpInfo()
{
	int iDataTypeIndex = 0;
	int iDirectoryLevel = 0;
	FtpUpload tParam = {0};
	tParam.iSize = sizeof(tParam);
	tParam.iFtpNum = FTP_NO_1;
	CString cstrType;
	int iType = 0;

	int iRet = NetClient_GetITSExtraInfo(m_iLogonID, ITS_EXTRAINFO_CMD_FTP_UPLOAD, 0, &tParam, sizeof(FtpUpload));
	if (RET_MALLOC_FALIED == iRet)
	{
		tParam.pstDirectory = new PicDirectory[tParam.iListCount];
		if (NULL == tParam.pstDirectory)
		{
			goto EXIT;
		}

		tParam.pstPicName = new PicName[tParam.iPicNameCount];
		if (NULL == tParam.pstPicName)
		{
			goto EXIT;
		}		
		iRet = NetClient_GetITSExtraInfo(m_iLogonID, ITS_EXTRAINFO_CMD_FTP_UPLOAD, 0, &tParam, sizeof(FtpUpload));
	}

	if (RET_SUCCESS != iRet)
	{
		goto EXIT;
	}


	for (int i = 0; i < m_cboDataType.GetCount(); ++i)
	{
		if (m_cboDataType.GetItemData(i) == tParam.iFtpType)
		{
			iDataTypeIndex = i;
			m_cboDataType.SetCurSel(i);
			break;
		}
	}
	m_IPAddr.SetWindowText(tParam.cFtpAddr);
	SetDlgItemInt(IDC_EDIT_PORT,tParam.iPort);
	m_edtUser.SetWindowText(tParam.cUserName);
	m_edtPwd.SetWindowText(tParam.cPassWord);

	//PicDirectory& tDirectoryRoot = tParam.pstDirectory[iDirectoryLevel++];
	//m_edtRootDirectory.SetWindowText(tDirectoryRoot.cUserDefine);	
	
	m_cboDirectoryStructure.SetCurSel(tParam.iListCount);
	for (int i=0; i<m_cboDirectoryStructure.GetCount(); i++)
	{
		if (m_cboDirectoryStructure.GetItemData(i) == tParam.iListCount)
		{
			m_cboDirectoryStructure.SetCurSel(i);
			break;
		}
	}
	m_cbo_NameIndex.SetCurSel(tParam.iPicNameCount);

	OnCbnSelchangeCmbRoot();
	OnCbnSelchangeCboNameindex();

	for (int i = 0; i < DIRECTORY_NUM && iDirectoryLevel < tParam.iListCount; ++i,++iDirectoryLevel)
	{
		int iIndex = 0;
		PicDirectory& tDirectory = tParam.pstDirectory[iDirectoryLevel];
		for (int j = 0; j < m_cboDirectory[i].GetCount(); ++j)
		{
			if (tDirectory.iDirectoryId == m_cboDirectory[i].GetItemData(j))
			{
				iIndex = j;
				if (DIRECTORY_USER_DEFINE == tDirectory.iDirectoryId)
				{
					m_edtDirectoryName[i].SetWindowText(tDirectory.cUserDefine);
				}
				break;
			}
		}	
		m_cboDirectory[i].SetCurSel(iIndex);
		if (m_cboDirectory[i].GetItemData(iIndex) == DIRECTORY_USER_DEFINE)
		{
			m_edtDirectoryName[i].ShowWindow(SW_SHOW);
			if (i == 0)
			{
				GetDlgItem(IDC_STATIC_FIRST_DIR_NAME)->ShowWindow(SW_SHOW);
			}
			else if(i == 1)
			{
				GetDlgItem(IDC_STATIC_SEC_DIR_NAME)->ShowWindow(SW_SHOW);
			}
			else if (i == 2)
			{
				GetDlgItem(IDC_STATIC_THIRD_DIR_NAME)->ShowWindow(SW_SHOW);
			}
			else if (i == 3)
			{
				GetDlgItem(IDC_STATIC_FORTH_DIR_NAME)->ShowWindow(SW_SHOW);
			}
			
		}
		else{
			if (i == 0)
			{
				GetDlgItem(IDC_STATIC_FIRST_DIR_NAME)->ShowWindow(SW_HIDE);
			}
			else if(i == 1)
			{
				GetDlgItem(IDC_STATIC_SEC_DIR_NAME)->ShowWindow(SW_HIDE);
			}
			else if ( i == 2)
			{
				GetDlgItem(IDC_STATIC_THIRD_DIR_NAME)->ShowWindow(SW_HIDE);
			}
			else if (i == 3)
			{
				GetDlgItem(IDC_STATIC_FORTH_DIR_NAME)->ShowWindow(SW_SHOW);
			}
			m_edtDirectoryName[i].ShowWindow(SW_HIDE);
		}
	}

	int iPicNameCount = 0;

	for (int i = 0; i < tParam.iPicNameCount && i < DIRECTORY_NUM; i++)
	{
		PicName &tPicName = tParam.pstPicName[i];
		int iPicNameID = tPicName.iDirectoryId;
		int iIndex = 0;
		for (iIndex = 0; iIndex < m_cbo_NameType[i].GetCount(); iIndex++)
		{
			if (iPicNameID == m_cbo_NameType[i].GetItemData(iIndex))
			{
				m_cbo_NameType[i].SetCurSel(iIndex);
				if (65535 == iPicNameID)
				{
					m_edt_namedef[i].ShowWindow(SW_SHOW);
					m_edt_namedef[i].SetWindowText(tPicName.cUserDefine);
					GetDlgItem(IDC_STATIC_NAMEDEF1 + i)->ShowWindow(SW_SHOW);
				}
				else
				{
					m_edt_namedef[i].ShowWindow(SW_HIDE);
					GetDlgItem(IDC_STATIC_NAMEDEF1 + i)->ShowWindow(SW_HIDE);
				}
				break;
			}
		}

	}

	CheckDlgButton(IDC_CHK_FACE_UPLOAD,tParam.iFaceUpload);
	m_chkEnable.SetCheck(tParam.iFtpEnable);
	if (1==tParam.iIllegalVideoUpload)
	{
		m_chkIllegalVideoUpload.SetCheck(TRUE);
	}
	else
	{
		m_chkIllegalVideoUpload.SetCheck(FALSE);
	}
	
	m_editPicSeparator.SetWindowText(tParam.cSeparator);
	
	cstrType.Format("%s",tParam.cEncodeType);
	iType = StrToInt(cstrType);

	SetCurSelByData(&m_cbEncodeType, iType);
EXIT:

	UpdateEnable();
	UpdateDirectroyLevel();

	// memory release
	if (NULL != tParam.pstDirectory)
	{
		delete[] tParam.pstDirectory;
	}
	if (NULL != tParam.pstPicName)
	{
		delete[] tParam.pstPicName;
	}
}	

void CLS_FtpUpload::UpdateDirectroyLevel()
{
	int iLevel = m_cboDirectoryStructure.GetCurSel();
	if(0 == iLevel)
	{
		for (int i = 0; i <= DIRECTORY_NUM; ++i)
		{
			if (m_stcDirectory[i])
			{
				m_stcDirectory[i].ShowWindow(SW_HIDE);
			}
			if (m_cboDirectory[i])
			{
				m_cboDirectory[i].ShowWindow(SW_HIDE);
			}
			if (m_stcDirectoryName[i])
			{
				m_stcDirectoryName[i].ShowWindow(SW_HIDE);
			}
			if (m_edtDirectoryName[i])
			{
				m_edtDirectoryName[i].ShowWindow(SW_HIDE);
			}
		}
		m_IPAddr.ShowWindow(SW_SHOW);
		return;
	}
	for (int i = 0; i < iLevel; ++i)
	{
		
		if (m_stcDirectory[i])
		{
			m_stcDirectory[i].ShowWindow(SW_SHOW);
		}
		if (m_cboDirectory[i])
		{
			m_cboDirectory[i].ShowWindow(SW_SHOW);
			if (DIRECTORY_USER_DEFINE == m_cboDirectory[i].GetItemData(m_cboDirectory[i].GetCurSel()))
			{
				m_edtDirectoryName[i].ShowWindow(SW_SHOW);
				m_stcDirectoryName[i].ShowWindow(SW_SHOW);
			}
			else
			{
				m_edtDirectoryName[i].ShowWindow(SW_HIDE);
				m_stcDirectoryName[i].ShowWindow(SW_HIDE);
			}
		}
	}

	for (int i = iLevel; i < DIRECTORY_NUM; ++i)
	{
		if (m_stcDirectory[i])
		{
			m_stcDirectory[i].ShowWindow(SW_HIDE);
		}
		if (m_cboDirectory[i])
		{
			m_cboDirectory[i].ShowWindow(SW_HIDE);
		}
		if (m_stcDirectoryName[i])
		{
			m_stcDirectoryName[i].ShowWindow(SW_HIDE);
		}
		if (m_edtDirectoryName[i])
		{
			m_edtDirectoryName[i].ShowWindow(SW_HIDE);
		}
	}
}

void CLS_FtpUpload::OnCbnSelchangeCmbRoot()
{
	UpdateDirectroyLevel();

}

void CLS_FtpUpload::UpdateDirectroyName( int _iLevel )
{
	if (_iLevel < 0 || _iLevel >= DIRECTORY_NUM)
	{
		goto EXIT;
	}

	int iCmdShowDirectroyName = SW_HIDE;
	if (m_cboDirectory[_iLevel] && m_cboDirectory[_iLevel].IsWindowVisible())
	{
		int iDirectory = m_cboDirectory[_iLevel].GetItemData(m_cboDirectory[_iLevel].GetCurSel());
		if (DIRECTORY_USER_DEFINE == iDirectory)
		{
			iCmdShowDirectroyName = SW_SHOW;
		}		
	}
	if (m_edtDirectoryName[_iLevel])
	{
		m_edtDirectoryName[_iLevel].ShowWindow(iCmdShowDirectroyName);
		m_stcDirectoryName[_iLevel].ShowWindow(iCmdShowDirectroyName);
	}

EXIT:
	return;
}


void CLS_FtpUpload::OnCbnSelchangeCmbFirst()
{
	UpdateDirectroyName(DIRECTORY_LEVEL_1-DIRECTORY_LEVEL_1);

}

void CLS_FtpUpload::OnCbnSelchangeCmbSecond()
{
	UpdateDirectroyName(DIRECTORY_LEVEL_2-DIRECTORY_LEVEL_1);

}

void CLS_FtpUpload::OnCbnSelchangeCmbThird()
{
	UpdateDirectroyName(DIRECTORY_LEVEL_3-DIRECTORY_LEVEL_1);

}

void CLS_FtpUpload::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	UpdateFtpInfo();
}

void CLS_FtpUpload::OnCbnSelchangeCboNameindex()
{
	// TODO: Add your control notification handler code here
	int iCount = m_cbo_NameIndex.GetItemData(m_cbo_NameIndex.GetCurSel());

	for (int i = 0; i < MAX_NAME_COUNT - 1; i++)
	{
		GetDlgItem(IDC_STATIC_NAMEDEF1 + i)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_NAMETYPE1 + i)->ShowWindow(SW_HIDE);
		m_edt_namedef[i].ShowWindow(SW_HIDE);
		m_cbo_NameType[i].ShowWindow(SW_HIDE);
	}

	for (int i = 0; i < iCount;i++)
	{
		GetDlgItem(IDC_STATIC_NAMETYPE1 + i)->ShowWindow(SW_SHOW);
		m_cbo_NameType[i].ShowWindow(SW_SHOW);
		if (m_cbo_NameType[i].GetItemData(m_cbo_NameType[i].GetCurSel()) == PIC_USER_DEFINE)
		{
			m_edt_namedef[i].ShowWindow(SW_SHOW);
			GetDlgItem(IDC_STATIC_NAMEDEF1 + i)->ShowWindow(SW_SHOW);
		}
	}


}

void CLS_FtpUpload::OnStnClickedStaticNametype1()
{
	// TODO: Add your control notification handler code here
}

void CLS_FtpUpload::OnCbnSelchangeCboNametype1()
{
	// TODO: Add your control notification handler code here
	int iType = m_cbo_NameType[0].GetItemData(m_cbo_NameType[0].GetCurSel());
	int iResult = ShowDefine(iType, &m_edt_namedef[0]);
	if(0 == iResult)
	{
		GetDlgItem(IDC_STATIC_NAMEDEF1)->ShowWindow(SW_HIDE);
	}
	else
	{
		GetDlgItem(IDC_STATIC_NAMEDEF1)->ShowWindow(SW_SHOW);
	}
}

int CLS_FtpUpload::ShowDefine(int iType, CEdit *pEdit)
{
	if (PIC_USER_DEFINE == iType)
	{
		pEdit->ShowWindow(SW_SHOW);
		return 1;
	}
	else
	{
		pEdit->ShowWindow(SW_HIDE);
		return 0;
	}
}

void CLS_FtpUpload::OnCbnSelchangeCboNametype2()
{
	// TODO: Add your control notification handler code here
	int iType = m_cbo_NameType[1].GetItemData(m_cbo_NameType[1].GetCurSel());
	int iResult = ShowDefine(iType, &m_edt_namedef[1]);
	if(0 == iResult)
	{
		GetDlgItem(IDC_STATIC_NAMEDEF2)->ShowWindow(SW_HIDE);
	}
	else
	{
		GetDlgItem(IDC_STATIC_NAMEDEF2)->ShowWindow(SW_SHOW);
	}
}

void CLS_FtpUpload::OnCbnSelchangeCboNametype3()
{
	// TODO: Add your control notification handler code here
	int iType = m_cbo_NameType[2].GetItemData(m_cbo_NameType[2].GetCurSel());
	int iResult = ShowDefine(iType, &m_edt_namedef[2]);
	if(0 == iResult)
	{
		GetDlgItem(IDC_STATIC_NAMEDEF3)->ShowWindow(SW_HIDE);
	}
	else
	{
		GetDlgItem(IDC_STATIC_NAMEDEF3)->ShowWindow(SW_SHOW);
	}
}

void CLS_FtpUpload::OnCbnSelchangeCmbForth()
{
	// TODO: Add your control notification handler code here
	UpdateDirectroyName(DIRECTORY_LEVEL_4-DIRECTORY_LEVEL_1);
}

HBRUSH CLS_FtpUpload::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CLS_BasePage::OnCtlColor(pDC, pWnd, nCtlColor);

	// TODO:  Change any attributes of the DC here
	if(pWnd->GetDlgCtrlID()==IDC_STC_NOTIFY)//If it is a static edit box
	{
		pDC->SetTextColor(RGB(255,0,0));//Change font color
	}

	// TODO:  Return a different brush if the default is not desired
	return hbr;
}

BOOL CLS_FtpUpload::SetCurSelByData(CComboBox *_pCombox, int _iData)
{
	BOOL bSet = FALSE;
	if (NULL != _pCombox)
	{
		int iIndex = 0;
		for (int i=0; i<_pCombox->GetCount(); i++)
		{
			if (_iData == _pCombox->GetItemData(i))
			{
				iIndex = i;
				bSet = TRUE;
				break;
			}
		}
		_pCombox->SetCurSel(iIndex);		
	}
	return bSet;
}

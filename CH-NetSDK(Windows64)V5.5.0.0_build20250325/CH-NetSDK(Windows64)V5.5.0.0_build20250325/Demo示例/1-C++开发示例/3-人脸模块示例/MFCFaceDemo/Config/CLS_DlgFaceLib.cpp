
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceLib.h"
#include "CLS_DlgFaceLibEdit.h"


typedef enum{
	ITEM_LIB_INDEX = 0,					//Serial No
	ITEM_LIB_NAME,						//Library name
	ITEM_LIB_VALUE,						//Identification threshold
	ITEM_LIB_UPLOAD,					//Identification information
	ITEM_LIB_DESCRIP,					//describe
	ITEM_LIB_TYPE,						//type
	ITEM_LIB_PICCNT,					//Picture count
	ITEM_LIB_MODELCNT,					//Model count
	ITEM_LIB_UUID,						//Platform UUID
	ITEM_LIB_VERSION,					//Platform version
}ITEM_FACE_LIB;

IMPLEMENT_DYNAMIC(CLS_DlgFaceLib, CLS_PageBase)

CLS_DlgFaceLib::CLS_DlgFaceLib(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceLib::IDD, pParent)
{
}

CLS_DlgFaceLib::~CLS_DlgFaceLib()
{
}

void CLS_DlgFaceLib::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LST_LIB_INFO, m_lstLibInfo);
	DDX_Control(pDX, IDC_STC_LIB_SPCOUNT, m_stcLibSPCount);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceLib, CLS_PageBase)
	ON_BN_CLICKED(IDC_BTN_LIB_ADD, &CLS_DlgFaceLib::OnBnClickedBtnLibAdd)
	ON_BN_CLICKED(IDC_BTN_LIB_MODIFY, &CLS_DlgFaceLib::OnBnClickedBtnLibModify)
	ON_BN_CLICKED(IDC_BTN_LIB_DELETE, &CLS_DlgFaceLib::OnBnClickedBtnLibDelete)
	ON_NOTIFY(NM_DBLCLK, IDC_LST_LIB_INFO, &CLS_DlgFaceLib::OnNMDblclkLstLibInfo)
	ON_BN_CLICKED(IDC_BTN_LIB_MODIFY_IPC, &CLS_DlgFaceLib::OnBnClickedBtnLibModifyIpc)
	ON_BN_CLICKED(IDC_BUTTON_LIB_CLEAR, &CLS_DlgFaceLib::OnBnClickedButtonLibClear)
END_MESSAGE_MAP()


void CLS_DlgFaceLib::UI_Init()
{
	m_lstLibInfo.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);
	m_lstLibInfo.InsertColumn(ITEM_LIB_INDEX, "Num", LVCFMT_LEFT, 75, -1);
	m_lstLibInfo.InsertColumn(ITEM_LIB_NAME, "Library name", LVCFMT_LEFT, 190, -1);
	m_lstLibInfo.InsertColumn(ITEM_LIB_VALUE, "Similarity", LVCFMT_LEFT, 100, -1);	
	m_lstLibInfo.InsertColumn(ITEM_LIB_UPLOAD, "Recognition", LVCFMT_LEFT, 100, -1);
	m_lstLibInfo.InsertColumn(ITEM_LIB_DESCRIP, "Description", LVCFMT_LEFT, 300, -1);
	m_lstLibInfo.InsertColumn(ITEM_LIB_TYPE, "Library Type", LVCFMT_LEFT, 300, -1);
	m_lstLibInfo.InsertColumn(ITEM_LIB_PICCNT, "Picture count", LVCFMT_LEFT, 300, -1);
	m_lstLibInfo.InsertColumn(ITEM_LIB_MODELCNT, "Model count", LVCFMT_LEFT, 300, -1);
}

void CLS_DlgFaceLib::UI_UptateData()
{
	UI_UpdataList();
}

void CLS_DlgFaceLib::UI_UpdataList()
{
	//QUERY
	m_lstLibInfo.DeleteAllItems();
	GetFaceLibInfo(m_mapFaceLibInfo);

	//SHOW
	map<int, FaceLibInfo>::iterator it = m_mapFaceLibInfo.begin();
	int iIndex = 0;
	for (; it != m_mapFaceLibInfo.end(); ++it)
	{
		FaceLibInfo tInfo = it->second;
		if (tInfo.iSize <= 0)
		{
			continue;
		}
		m_lstLibInfo.InsertItem(iIndex,_T(""));
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_INDEX, IntToStr(iIndex + 1));
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_NAME, tInfo.cName);
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_VALUE, IntToStr(tInfo.iThreshold));
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_UPLOAD, tInfo.iAlarmType ? "Upload":"Not upload");
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_DESCRIP, tInfo.cExtrInfo);
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_TYPE, 0 == tInfo.iLibType ? _T("General face library") : _T("Stanger face library"));
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_UUID, tInfo.cLibUUID);
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_PICCNT, IntToStr(tInfo.iLibPicCnt));
		m_lstLibInfo.SetItemText(iIndex, ITEM_LIB_MODELCNT, IntToStr(tInfo.iModelPicCnt));
		m_lstLibInfo.SetItemData(iIndex, tInfo.iLibKey);
		iIndex++;
	}

	FuncAbilityLevel tInfo = {sizeof(FuncAbilityLevel), MAIN_FUNC_TYPE_VCA, 26};
	int iRetBytes = 0;
	NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, 0x7FFFFFFF, &tInfo, sizeof(tInfo), &iRetBytes);
	CString cstrMsg;
	cstrMsg.Format("Face Library, Current Count %d / Max Support %d", m_mapFaceLibInfo.size(), atoi(tInfo.cParam));
	m_stcLibSPCount.SetWindowText(cstrMsg);

	int iProductType = 0;
	NetClient_GetProductTypeEx(m_iLogonID,NULL,&iProductType);
	if (NVRecord_PRODUCT == iProductType || m_iChanCount>4)
	{
		GetDlgItem(IDC_BTN_LIB_MODIFY_IPC)->ShowWindow(TRUE);
	}
	else
	{
		GetDlgItem(IDC_BTN_LIB_MODIFY_IPC)->ShowWindow(FALSE);
	}
}

void CLS_DlgFaceLib::OnBnClickedBtnLibAdd()
{
	CLS_DlgFaceLibEdit cls;
	cls.OnChannelChanged(m_iLogonID, m_iChannelNo, m_iStreamNo);
	cls.DoModal();
	UI_UpdataList();
}

void CLS_DlgFaceLib::OnBnClickedBtnLibModify()
{
	POSITION pPos = m_lstLibInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos){
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}
	int iPos = m_lstLibInfo.GetNextSelectedItem(pPos);
	int iFaceKey = (int)m_lstLibInfo.GetItemData(iPos);
	CLS_DlgFaceLibEdit cls;
	cls.OnChannelChanged(m_iLogonID, m_iChannelNo, m_iStreamNo);
	cls.SetLibInfo(m_mapFaceLibInfo[iFaceKey]);
	cls.DoModal();
	UI_UpdataList();
}

void CLS_DlgFaceLib::OnNMDblclkLstLibInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	POSITION pPos = m_lstLibInfo.GetFirstSelectedItemPosition();
	if (NULL != pPos)
	{
		OnBnClickedBtnLibModify();
	}
	*pResult = 0;
}

void CLS_DlgFaceLib::OnBnClickedBtnLibDelete()
{
	POSITION pPos = m_lstLibInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos){
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}
	FaceLibDelete tInfo = {0};
	tInfo.iSize = sizeof(FaceLibDelete);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iLibKey = (int)m_lstLibInfo.GetItemData(m_lstLibInfo.GetNextSelectedItem(pPos));

	FaceReply tReply = {0};
	int iRet = FaceConfig(FACE_CMD_LIB_DELETE, &tInfo, sizeof(FaceLibEdit), &tReply, sizeof(FaceReply));//Synchronous interface. When there are many base images in the face database, it takes a long time for the interface to return
	if (0 == iRet && 0 == tReply.iResult)
	{
		UI_UpdataList();
	}
	else
	{
		MessageBox("Face library deletion failed, return value:"+IntToStr(iRet)+",Reply:"+IntToStr(tReply.iResult), "Tips", MB_OK);
	}
}

void CLS_DlgFaceLib::OnBnClickedBtnLibModifyIpc()
{
	POSITION pPos = m_lstLibInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos){
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}
	int iPos = m_lstLibInfo.GetNextSelectedItem(pPos);
	int iFaceKey = (int)m_lstLibInfo.GetItemData(iPos);
	CLS_DlgFaceLibEdit cls;
	cls.SetLocal(FALSE);
	cls.OnChannelChanged(m_iLogonID, m_iChannelNo, m_iStreamNo);
	cls.SetLibInfo(m_mapFaceLibInfo[iFaceKey]);
	cls.DoModal();
	UI_UpdataList();
}

void CLS_DlgFaceLib::OnBnClickedButtonLibClear()
{
	POSITION pPos = m_lstLibInfo.GetFirstSelectedItemPosition();
	if (NULL == pPos){
		MessageBox("Please select a record in the form first!", "Tips", MB_OK);
		return;
	}
	FaceLibClearPara tInfo = {0};
	tInfo.iSize = sizeof(FaceLibClearPara);
	tInfo.iChan = m_iChannelNo;
	tInfo.iLibKey = (int)m_lstLibInfo.GetItemData(m_lstLibInfo.GetNextSelectedItem(pPos));
	CString strTemp(m_lstLibInfo.GetItemText(m_lstLibInfo.GetNextSelectedItem(pPos), ITEM_LIB_UUID));
	strncpy(tInfo.cLibUUID, strTemp.GetBuffer(), sizeof(tInfo.cLibUUID));
	strTemp.ReleaseBuffer();

	FaceReply tReply = {0};
	int iRet = FaceConfig(FACE_CMD_LIB_CLEAR, &tInfo, sizeof(FaceLibEdit), &tReply, sizeof(FaceReply));//Synchronous interface. When there are many base images in the face database, it takes a long time for the interface to return
	if (0 == iRet && 0 == tReply.iResult)
	{
		UI_UpdataList();
	}
	else
	{
		MessageBox("Face library clear failed, return value:"+IntToStr(iRet)+",Reply:"+IntToStr(tReply.iResult), "Tips", MB_OK);
	}
}

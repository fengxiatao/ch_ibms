// CLS_DlgFaceLibSync.cpp : Implementation file
//

#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceLibSync.h"


const char* g_strSyncStatus[] = {"Not Sync", "Syncing", "Sync Success", "Sync Fail", "Wait Sync"};
#define MAX_NUM_SYNC_STATUS			(sizeof(g_strSyncStatus)/sizeof(char*))

typedef enum{
	ITEM_LIB_SYNC_INDEX = 0,				//Serial No
	ITEM_LIB_SYNC_CHN,						//passageway
	ITEM_LIB_SYNC_STATUS,					//state
	ITEM_LIB_SYNC_PROGRESS,					//speed of progress
	ITEM_LIB_SYNC_INFO,						//details
}ITEM_FACE_LIB_SYNC;

// CLS_DlgFaceLibSync Dialog

IMPLEMENT_DYNAMIC(CLS_DlgFaceLibSync, CLS_PageBase)

CLS_DlgFaceLibSync::CLS_DlgFaceLibSync(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceLibSync::IDD, pParent)
{
	for (int i=0;i<FACE_MAX_KEY_COUNT;i++)
	{
		for (int j=0;j<LIBSYNC_QUERY_CHANNEL_NUM;j++)
		{
			m_tSyncResult.tResult[i][j].vecInfo.clear();
		}
	}
}

CLS_DlgFaceLibSync::~CLS_DlgFaceLibSync()
{
}

void CLS_DlgFaceLibSync::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LST_LIB_SYNC_INFO, m_lstLibSyncInfo);
	DDX_Control(pDX, IDC_CBO_SYNC_LIB_KEY, m_cboLibKey);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceLibSync, CLS_PageBase)
	ON_BN_CLICKED(IDC_BTN_LIB_START_SYNC, &CLS_DlgFaceLibSync::OnBnClickedBtnLibStartSync)
	ON_BN_CLICKED(IDC_BTN_LIB_STOP_SYNC, &CLS_DlgFaceLibSync::OnBnClickedBtnLibStopSync)
	ON_BN_CLICKED(IDC_BTN_LIB_DELT_SYNC, &CLS_DlgFaceLibSync::OnBnClickedBtnLibDeltSync)
	ON_BN_CLICKED(IDC_BTN_LIB_DELT_SYNC_LIB, &CLS_DlgFaceLibSync::OnBnClickedBtnLibDeltSyncLib)
	ON_BN_CLICKED(IDC_BTN_LIB_REFESH_SYNC, &CLS_DlgFaceLibSync::OnBnClickedBtnLibRefeshSync)
	ON_CBN_SELCHANGE(IDC_CBO_SYNC_LIB_KEY, &CLS_DlgFaceLibSync::OnCbnSelchangeCboSyncLibKey)
	ON_NOTIFY(NM_CLICK, IDC_LST_LIB_SYNC_INFO, &CLS_DlgFaceLibSync::OnNMClickLstLibSyncInfo)
	ON_BN_CLICKED(IDC_BTN_LIB_CLEAR_FAIL_INFO, &CLS_DlgFaceLibSync::OnBnClickedBtnLibClearFailInfo)
	ON_BN_CLICKED(IDC_BTN_LIB_START_SYNC_ADDTO, &CLS_DlgFaceLibSync::OnBnClickedBtnLibStartSyncAddto)
END_MESSAGE_MAP()


// CLS_DlgFaceLibSync Message Handler

void CLS_DlgFaceLibSync::UI_Init()
{
	m_lstLibSyncInfo.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT|LVS_EX_CHECKBOXES);
	m_lstLibSyncInfo.InsertColumn(ITEM_LIB_SYNC_INDEX, "No", LVCFMT_LEFT, 40, -1);
	m_lstLibSyncInfo.InsertColumn(ITEM_LIB_SYNC_CHN, "ChannelNo", LVCFMT_LEFT, 120, -1);
	m_lstLibSyncInfo.InsertColumn(ITEM_LIB_SYNC_STATUS, "Status", LVCFMT_LEFT, 80, -1);	
	m_lstLibSyncInfo.InsertColumn(ITEM_LIB_SYNC_PROGRESS, "Progress", LVCFMT_LEFT, 80, -1);
	m_lstLibSyncInfo.InsertColumn(ITEM_LIB_SYNC_INFO, "Details", LVCFMT_LEFT, 200, -1);
}

void CLS_DlgFaceLibSync::UI_UptateData()
{
	QueryLibkey(m_cboLibKey);
	UI_UpdataSyncInfo();
	UI_UpdataSyncResult();
}

void CLS_DlgFaceLibSync::UI_UpdataSyncInfo()
{
	if (m_iChanCount<=1)
	{
		//Only nvr is updated
		return;
	}

	int iLibKeySel = m_cboLibKey.GetCurSel();
	if (iLibKeySel<0)
	{
		return;
	}

	int iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);
	if (iLibKey < 0)
	{
		return;
	}

	m_lstLibSyncInfo.DeleteAllItems();

	int iChanNum = 0;
	NetClient_GetDigitalChannelNum(m_iLogonID, &iChanNum);	

	FaceLibSyncQuery tQuery = {0};
	tQuery.iSize = sizeof(FaceLibSyncQuery);
	tQuery.iChanNo = 0x7FFFFFFF;//0x7FFFFFFF means to obtain all channels
	tQuery.iQueryResultSize = LIBSYNC_QUERY_CHANNEL_NUM;
	tQuery.iLibKey = iLibKey;

	FaceLibSyncQueryResult tResult[LIBSYNC_QUERY_CHANNEL_NUM] = {0};

	int iRet = FaceConfig(FACE_CMD_LIB_SYNC_STATUS, &tQuery, sizeof(FaceLibSyncQuery), &tResult, sizeof(FaceLibSyncQueryResult));
	if (0 == iRet)
	{
		for (int iIndex = 0; iIndex < iChanNum && iIndex < LIBSYNC_QUERY_CHANNEL_NUM; iIndex++)
		{
			int iStatus = 0;
			CString cstrResultNum;
			if (tResult[iIndex].iState < MAX_NUM_SYNC_STATUS)
			{
				iStatus = tResult[iIndex].iState;
			}
			cstrResultNum.Format("Success%d-Fail%d",tResult[iIndex].iSuccNum,tResult[iIndex].iFailNum);		

			m_lstLibSyncInfo.InsertItem(iIndex,_T(""));	
			m_lstLibSyncInfo.SetItemText(iIndex, ITEM_LIB_SYNC_INDEX, IntToStr(iIndex + 1));			//Serial No
			m_lstLibSyncInfo.SetItemText(iIndex, ITEM_LIB_SYNC_CHN,	IntToStr(iIndex + 1));				//passageway
			m_lstLibSyncInfo.SetItemText(iIndex, ITEM_LIB_SYNC_STATUS, g_strSyncStatus[iStatus]);		//state
			m_lstLibSyncInfo.SetItemText(iIndex, ITEM_LIB_SYNC_PROGRESS, IntToStr(tResult[iIndex].iProcess));	//Progress
			m_lstLibSyncInfo.SetItemText(iIndex, ITEM_LIB_SYNC_INFO, cstrResultNum);					//details
		}

	}		

}

void CLS_DlgFaceLibSync::FaceLibSync(int _iCmd)
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);

	int iLibKey = (int)m_cboLibKey.GetItemData(iLibKeySel);

	if (iLibKey < 0)
	{
		return;
	}

	int iCount = m_lstLibSyncInfo.GetItemCount();
	int iCheckNum = 0;
	int iArraySize = iCount/LEN_32;
	if (0 != iCount%LEN_32)
	{
		iArraySize += 1;
	}

	FaceLibSyncStart tLibSync = {0};
	tLibSync.iSize = sizeof(tLibSync);
	tLibSync.iLibKey = iLibKey;
	tLibSync.iStatus = _iCmd;	//20 Start, 21 Stop 22 Delete Task
	tLibSync.iChanListArraySize = iArraySize;

	for (int i = 0; i < iCount && (i/LEN_32) < FACE_LIBSYNC_CHANLIST_COUNT; i++)
	{
		BOOL blCheck = m_lstLibSyncInfo.GetCheck(i);
		if (1 != blCheck)
		{
			continue;
		}
		iCheckNum++;
		tLibSync.iChanList[i/LEN_32] |= blCheck<<(i%LEN_32);
	}

	if (iCheckNum <= 0)
	{	
		return;
	}

	FaceReply tReply = {0};
	int iRet = FaceConfig(FACE_CMD_LIB_SYNC_START, &tLibSync, sizeof(FaceLibSyncStart), &tReply, sizeof(FaceReply));

	if (0 == iRet && 0 == tReply.iResult)
	{
		UI_UpdataSyncInfo();
		if (FACE_LIBSYNC_STATUS_START == _iCmd || FACE_LIBSYNC_STATUS_DELETE == _iCmd || FACE_LIBSYNC_STATUS_DELETE_IPCLIB == _iCmd || FACE_LIBSYNC_STATUS_START_ADDTO == _iCmd)
		{
			ReleaseFaceLibSyncResult(iLibKeySel);
		}
		UI_UpdataSyncResult();
	}
	else
	{
		MessageBox("Face library sync operation failed,Ret:"+IntToStr(iRet)+",Reply:"+IntToStr(tReply.iResult), "Tip", MB_OK);
	}
}

void CLS_DlgFaceLibSync::OnBnClickedBtnLibStartSync()
{
	FaceLibSync(FACE_LIBSYNC_STATUS_START);
}

void CLS_DlgFaceLibSync::OnBnClickedBtnLibStopSync()
{
	FaceLibSync(FACE_LIBSYNC_STATUS_STOP);
}

void CLS_DlgFaceLibSync::OnBnClickedBtnLibDeltSync()
{
	FaceLibSync(FACE_LIBSYNC_STATUS_DELETE);
}

void CLS_DlgFaceLibSync::OnBnClickedBtnLibDeltSyncLib()
{
	FaceLibSync(FACE_LIBSYNC_STATUS_DELETE_IPCLIB);
}

void CLS_DlgFaceLibSync::OnBnClickedBtnLibRefeshSync()
{
	UI_UpdataSyncInfo();
	UI_UpdataSyncResult();
}

void CLS_DlgFaceLibSync::OnCbnSelchangeCboSyncLibKey()
{
	UI_UpdataSyncInfo();
	UI_UpdataSyncResult();
}

void CLS_DlgFaceLibSync::OnParamChangeNotify( int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUser )
{
	if (_iLogonID != m_iLogonID)
	{
		return;
	}

	switch (_iParaType)
	{
	case PARA_FACE_LIB_SYNC_RESULT:
		UpdateFaceLibSyncResult((FaceLibSyncResult*)_pPara);
		break;
	default:
		break;

	}
}

void CLS_DlgFaceLibSync::UpdateFaceLibSyncResult(FaceLibSyncResult* _tInfo)
{
	if (_tInfo->iLibKey >= 0 && _tInfo->iLibKey < FACE_MAX_KEY_COUNT
		&& _tInfo->iChanNo>= 0 && _tInfo->iChanNo < LIBSYNC_QUERY_CHANNEL_NUM)
	{
		FaceLibSyncResult *ptResult = new FaceLibSyncResult();
		memcpy(ptResult, _tInfo, sizeof(FaceLibSyncResult));
		
		int iLibKeySel=-1;
		int iCount = m_cboLibKey.GetCount();
		for (int i=0;i<iCount && i<FACE_MAX_KEY_COUNT;i++)
		{
			if(m_cboLibKey.GetItemData(i) == _tInfo->iLibKey)
			{
				iLibKeySel = i;
				break;
			}
		}
		if (-1 == iLibKeySel)
		{
			return;
		}

		m_tSyncResult.tResult[iLibKeySel][_tInfo->iChanNo].vecInfo.push_back(ptResult);
	}
	return;
}

void CLS_DlgFaceLibSync::OnNMClickLstLibSyncInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	
	int iPos = pNMItemActivate->iItem;
	if (-1 != iPos)
	{
		//UI_UpdataSyncResult();
	}

	*pResult = 0;
}

void CLS_DlgFaceLibSync::UI_UpdataSyncResult()
{
	int iLibKeySel = m_cboLibKey.GetCurSel();
	if (iLibKeySel<0)
	{
		return;
	}

	CString csResult;
	for (int i=0;i<m_iChanCount && i<LIBSYNC_QUERY_CHANNEL_NUM;i++)
	{
		int iSize = m_tSyncResult.tResult[iLibKeySel][i].vecInfo.size();
		for (int j = 0;j < iSize; j++)
		{
			CString cstrTemp = "";
			FaceLibSyncResult *ptResult = m_tSyncResult.tResult[iLibKeySel][i].vecInfo[j];
			if (NULL != ptResult)
			{
				cstrTemp.Format("No-%04d:  iChanNo(%d) iLibKey(%d) cLibUUID(%s) iFaceKey(%d) cFaceUUID(%s) iSyncResult(%d)\r\n",
					j+1,
					ptResult->iChanNo+1,
					ptResult->iLibKey,
					ptResult->cLibUUID,
					ptResult->iFaceKey,
					ptResult->cFaceUUID,
					ptResult->iSyncResult);
			}
			csResult+= cstrTemp;
		}
	}
	
	SetDlgItemText(IDC_EDIT_SYNC_RESULT, csResult);
}

void CLS_DlgFaceLibSync::ReleaseFaceLibSyncResult(int _iLibkeySel)
{
	for (int i=0;i<m_iChanCount && i<LIBSYNC_QUERY_CHANNEL_NUM;i++)
	{
		int iSize = m_tSyncResult.tResult[_iLibkeySel][i].vecInfo.size();
		for (int j=0;j<iSize;j++)
		{
			if (NULL != m_tSyncResult.tResult[_iLibkeySel][i].vecInfo[j])
			{
				delete m_tSyncResult.tResult[_iLibkeySel][i].vecInfo[j];
				m_tSyncResult.tResult[_iLibkeySel][i].vecInfo[j] = NULL;
			}
		}

		m_tSyncResult.tResult[_iLibkeySel][i].vecInfo.clear();
	}
}


void CLS_DlgFaceLibSync::OnBnClickedBtnLibClearFailInfo()
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);

	ReleaseFaceLibSyncResult(iLibKeySel);
	UI_UpdataSyncResult();
}

void CLS_DlgFaceLibSync::OnLogoff()
{
	for (int i=0; i<FACE_MAX_KEY_COUNT; i++)
	{
		ReleaseFaceLibSyncResult(i);
	}
}
void CLS_DlgFaceLibSync::OnBnClickedBtnLibStartSyncAddto()
{
	FaceLibSync(FACE_LIBSYNC_STATUS_START_ADDTO);
}

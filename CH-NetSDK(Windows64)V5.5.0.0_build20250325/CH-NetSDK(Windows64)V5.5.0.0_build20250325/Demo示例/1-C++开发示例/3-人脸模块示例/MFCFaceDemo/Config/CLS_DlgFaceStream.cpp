
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceStream.h"


IMPLEMENT_DYNAMIC(CLS_DlgFaceStream, CLS_PageBase)

CLS_DlgFaceStream::CLS_DlgFaceStream(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceStream::IDD, pParent)
{
	m_uiRecvID = -1;
	m_pImageFullPic = NULL;
	InitializeCriticalSection(&m_csStreamInfo);
}

CLS_DlgFaceStream::~CLS_DlgFaceStream()
{
	SAFE_DESTORY_IMAGE(m_pImageFullPic);
	EnterCriticalSection(&m_csStreamInfo);
	list<ShowStreamInfo>::iterator it = m_lstStreamInfo.begin();
	for (; it != m_lstStreamInfo.end(); ++it)
	{
		ShowStreamInfo tShow = *it;
		SAFE_DESTORY_IMAGE(tShow.pImage[0]);
		SAFE_DESTORY_IMAGE(tShow.pImage[1]);
	}
	LeaveCriticalSection(&m_csStreamInfo);
	DeleteCriticalSection(&m_csStreamInfo);
}

void CLS_DlgFaceStream::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_STC_STREAM_FULLPIC, m_stcFullPic);
	DDX_Control(pDX, IDC_STC_STREAM_SNAP1, m_stcSnapPic[0]);	
	DDX_Control(pDX, IDC_STC_STREAM_SNAP2, m_stcSnapPic[1]);	
	DDX_Control(pDX, IDC_STC_STREAM_SNAP3, m_stcSnapPic[2]);
	DDX_Control(pDX, IDC_STC_STREAM_SNAP4, m_stcSnapPic[3]);
	DDX_Control(pDX, IDC_STC_STREAM_NEG1, m_stcNegPic[0]);
	DDX_Control(pDX, IDC_STC_STREAM_NEG2, m_stcNegPic[1]);
	DDX_Control(pDX, IDC_STC_STREAM_NEG3, m_stcNegPic[2]);
	DDX_Control(pDX, IDC_STC_STREAM_NEG4, m_stcNegPic[3]);
	DDX_Control(pDX, IDC_STC_STREAM_SIMILAR1, m_stcSimilar[0]);
	DDX_Control(pDX, IDC_STC_STREAM_SIMILAR2, m_stcSimilar[1]);
	DDX_Control(pDX, IDC_STC_STREAM_SIMILAR3, m_stcSimilar[2]);
	DDX_Control(pDX, IDC_STC_STREAM_SIMILAR4, m_stcSimilar[3]);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceStream, CLS_PageBase)
	ON_WM_PAINT()
	ON_BN_CLICKED(IDC_BTN_STREAM_SRART, &CLS_DlgFaceStream::OnBnClickedBtnStreamSrart)
	ON_BN_CLICKED(IDC_BTN_STREAM_STOP, &CLS_DlgFaceStream::OnBnClickedBtnStreamStop)	
END_MESSAGE_MAP()

BOOL CLS_DlgFaceStream::OnInitDialog()
{
	CLS_PageBase::OnInitDialog();
	for (int i = 0; i < MAX_SHOW_PIC_NUM; ++i)
	{
		m_stcSimilar[i].SetWindowText("");
	}
	m_cstrPicStreamPath = GetCurModulePath() + "PicStream\\";
	if (!PathIsDirectory(m_cstrPicStreamPath))
	{
		CreateDirectoryByPath(m_cstrPicStreamPath);
	}
	return TRUE;
}

void CLS_DlgFaceStream::OnPaint()
{
	CPaintDC dc(this);
	ShowSnapPic();
}

void CLS_DlgFaceStream::OnBnClickedBtnStreamSrart()
{
	if (-1 != m_uiRecvID) {
		//One channel can only be opened once
		return;
	}
	//Open picture stream
	NetPicPara tPara = {0};
	tPara.iStructLen = sizeof(NetPicPara);
	tPara.iChannelNo = m_iChannelNo;
	tPara.cbkPicStreamNotify = OnNotify_PicStream;
	tPara.pvUser = this;
	int iRet = NetClient_StartRecvNetPicStream(m_iLogonID, &tPara, sizeof(NetPicPara), &m_uiRecvID);
}

void CLS_DlgFaceStream::OnBnClickedBtnStreamStop()
{
	if (-1 != m_uiRecvID)
	{	//Stop the picture stream
		int iRet = NetClient_StopRecvNetPicStream(m_uiRecvID);
		m_uiRecvID = -1;
	}	
}

int CLS_DlgFaceStream::OnNotify_PicStream(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser)
{
	//Image stream callback function
	CLS_DlgFaceStream* pThis = (CLS_DlgFaceStream*)_pvUser;
	if (NULL != pThis && NET_PICSTREAM_CMD_FACE == _lCommand)
	{	
		//Image stream data processing
		pThis->OnPicStream(_uiRecvID, _pvBuf, _iBufLen);
	}
	return 0;
}

CString GetCapTimeStr(PicTime& _tm)
{
	static unsigned int index = 0;
	CString cstrTime;
	cstrTime.Format("2%03d%02d%02d%02d%02d%02d%d%d", _tm.uiYear, _tm.uiMonth, _tm.uiDay, 
		_tm.uiHour, _tm.uiMinute, _tm.uiSecondsr, _tm.uiMilliseconds, index++);
	return cstrTime;
}

int SavePicture(CString _cstrPath, char* _pcData, int _iLen)
{
	if (NULL == _pcData || _iLen <= 0)
	{
		return -1;
	}

	FILE* pFile = NULL;
	fopen_s(&pFile, (LPSTR)(LPCTSTR)_cstrPath, "wb");
	if (NULL == pFile)
	{
		return -1;
	}

	size_t iWriLen = fwrite(_pcData, _iLen, 1, pFile);
	fclose(pFile);
	pFile = NULL;
	return 0;
}

CString GetPicType(int _iType)
{
	CString cstr = "jpg";
	if (1 == _iType)
	{
		cstr = "png";
	}
	return cstr;
}

CString GetAlarmType(int _iAlarmType)
{
	CString cstrAlarmType = "";
	switch(_iAlarmType)
	{
	case 1:			//Comparison
		cstrAlarmType = "Comparison";
		break;
	case 2:			//Strangers
		cstrAlarmType = "Stranger";
		break;
	case 3:			//Frequency
		cstrAlarmType = "Frequency";
		break;
	case 4:			//Detention
		cstrAlarmType = "Detention";
		break;
	default:
		break;
	}
	return cstrAlarmType;
}

void CLS_DlgFaceStream::OnPicStream(unsigned int _uiRecvID, void* _pvBuf, int _iBufLen)
{
	if (NULL == _pvBuf || _uiRecvID != m_uiRecvID || _iBufLen <= 0) {
		//Parameter legitimacy judgment
		return;
	}	
	//Copy image stream data
	FacePicStream tStream = {0};
	memcpy(&tStream, _pvBuf, min(_iBufLen, sizeof(FacePicStream)));
	if (tStream.iFaceCount <= 0 || NULL == tStream.ptFullData)
	{
		return;
	}

	RecvInfo tRecv = {0};
	NetClient_GetRecvInfoById(_uiRecvID, &tRecv, sizeof(tRecv));

	//Capture time
	CString cstrCapTime = GetCapTimeStr(tStream.ptFullData->tPicTime);

	//Save&Show Panorama
	CString cstrFullPic;
	cstrFullPic.Format("%s%s_ch%d_aFullPic.jpg", m_cstrPicStreamPath, cstrCapTime, tRecv.iChanNo);
	SavePicture(cstrFullPic, tStream.ptFullData->pcPicData, tStream.ptFullData->iDataLen);
	EnterCriticalSection(&m_csStreamInfo);
	SAFE_DESTORY_IMAGE(m_pImageFullPic);
	m_pImageFullPic = LoadImage(cstrFullPic);
	LeaveCriticalSection(&m_csStreamInfo);

	//Save&Display Face Snapshot
	for (int i= 0; i < tStream.iFaceCount && i < MAX_FACE_PICTURE_COUNT; ++i)
	{
		if (NULL == tStream.ptFaceData[i] || tStream.ptFaceData[i]->iDataLen <= 0) 
		{
			continue;
		}
		//Face information
		FacePicData tFace = {0};
		memcpy(&tFace, tStream.ptFaceData[i], min(tStream.iSizeOfFace, sizeof(FacePicData)));

		//Information displayed on the interface
		ShowStreamInfo tShow = {tRecv.iChanNo, tFace.iAlramType, tFace.iSimilatity, "", NULL, NULL};
		Utf8ToAnsi(tFace.cName, tShow.cName);

		//Save snapshot
		CString cstrFacePic;
		cstrFacePic.Format("%s%s_ch%d_bFace_alm%d_%d.jpg", m_cstrPicStreamPath, cstrCapTime, tRecv.iChanNo, tFace.iAlramType, i+1);
		SavePicture(cstrFacePic, tFace.pcPicData, tFace.iDataLen);
		tShow.pImage[0] = LoadImage(cstrFacePic);

		//Save face base map
		if (0 != tFace.iAlramType && 2 != tFace.iAlramType && NULL != tFace.pcNegPicData)
		{
			CString cstrNegPic;
			cstrNegPic.Format("%s%s_ch%d_cNeg_alm%d_%d.%s", m_cstrPicStreamPath, cstrCapTime, tRecv.iChanNo, tFace.iAlramType, i+1, GetPicType(tFace.iNegPicType));
			SavePicture(cstrNegPic, tFace.pcNegPicData, tFace.iNegPicLen);
			tShow.pImage[1] = LoadImage(cstrNegPic);
		}

		m_lstStreamInfo.push_front(tShow);
		if (m_lstStreamInfo.size() > MAX_SHOW_PIC_NUM)
		{
			ShowStreamInfo tPop = m_lstStreamInfo.back();
			SAFE_DESTORY_IMAGE(tPop.pImage[0]);
			SAFE_DESTORY_IMAGE(tPop.pImage[1]);
			m_lstStreamInfo.pop_back();
		}
	}

	//Show Pictures
	ShowSnapPic();
}

void CLS_DlgFaceStream::ShowSnapPic()
{
	EnterCriticalSection(&m_csStreamInfo);
	//Show Large
	ShowImage(m_pImageFullPic, &m_stcFullPic);
	//Display snapshot
	int iIndex = 0;
	list<ShowStreamInfo>::iterator it = m_lstStreamInfo.begin();
	for (; it != m_lstStreamInfo.end(); ++it)
	{
		ShowStreamInfo tShow = *it;
		ShowImage(tShow.pImage[0], &m_stcSnapPic[iIndex]);
		ShowImage(tShow.pImage[1], &m_stcNegPic[iIndex]);
		CString cstrMsg;
		cstrMsg.Format("ch%d, %s, %d, %s", tShow.iChanNo, GetAlarmType(tShow.iAlarmType), tShow.iSimilarity, tShow.cName);
		m_stcSimilar[iIndex].SetWindowText(cstrMsg);
		iIndex++;
	}
	LeaveCriticalSection(&m_csStreamInfo);
}

void CLS_DlgFaceStream::OnLogoff()
{
	if (-1 != m_uiRecvID)
	{	//Stop the picture stream
		int iRet = NetClient_StopRecvNetPicStream(m_uiRecvID);
		m_uiRecvID = -1;
	}
}

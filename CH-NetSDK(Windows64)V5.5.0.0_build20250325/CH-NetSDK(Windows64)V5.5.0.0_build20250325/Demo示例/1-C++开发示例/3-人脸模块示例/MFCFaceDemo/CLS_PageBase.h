#ifndef _PAGE_BASE_H
#define _PAGE_BASE_H

#include "NetSdkClient.h"
#include "CommonFun.h"
#include "InnerDefine.h"
#include "atlimage.h"

#define CHECK_LIB_KEY(_cbo, _iLibkeySel)\
	_iLibkeySel = _cbo.GetCurSel();\
	if (_iLibkeySel < 0) {\
		MessageBox("Please search or select the face library first!", "Tips", MB_OK);\
		return;\
	}\

#define SAFE_DESTORY_IMAGE(x)	if ((x) != NULL)	{(x)->Destroy(); (x) = NULL;}

class CLS_PageBase : public CDialog
{
	DECLARE_DYNAMIC(CLS_PageBase)

public:
	CLS_PageBase(UINT nIDTemplate,CWnd* pParentWnd = NULL);//Need this 
	~CLS_PageBase();

	virtual BOOL OnInitDialog();

	DECLARE_MESSAGE_MAP()
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);

	map<int, ProvienceStr> m_mapProvience;
	
public:
	int		m_iLogonID;
	int		m_iChannelNo;
	int		m_iStreamNo;
	int		m_iChanCount;

	FaceQueryResult m_tFacePicInfo[FACE_MAX_PAGE_COUNT];
	FaceLibQueryResult m_tFaceLibInfo[FACE_MAX_KEY_COUNT];

	int		FaceConfig(int _iCmdId, void* _lpIn, int _iInLen, void* _lpOut, int _iOutLen);
	int		SetVcaStatue(int _iStatus);
	int		GetDevConfig(int _iCmdId, void* _lpBuf, int _iBufLen);
	int		GetFaceAbility(int _iLogonId, int iMainFuncType, int iSubFuncType);

	//Face database, face base map, face retrieval
	CString			m_cstrLocalPicPath;
	unsigned int	m_iDLFacePicId;

	virtual void	StopDownloadFacePic();
	virtual int		StartDownLoadFacePic(FaceInfo&_tInfo, CString _cstrPath);
	virtual void	StartDownloadNextFacePic(int _iRet){};


	void	GetFaceLibInfo(map<int, FaceLibInfo>& _mapLib);
	CImage*	LoadAndShowImage(CString _cstrParh, CWnd* _pWnd);
	CImage*	LoadImage(CString _cstrParh);
	int		ShowImage(CImage* _pImage, CWnd* _pWnd);

	CString GetFailedReason(int _iReason,int _iSubReason=-1);
	
	virtual	void UI_Init(){};
	virtual void UI_UptateData(){};
	virtual void UI_EnbaleWindow(BOOL _blEnbale){};

//	virtual void UI_InitFaceList(CListCtrl& _lst){};
	virtual void UI_UpdateFaceList(CListCtrl& _lst, FaceInfo& _tInfo, int _iLibIndex=-1){};
	
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	virtual void OnMainNotify(int _iLogonID, int _wParam, void* _iLParam);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData){};
	virtual void OnLogoff(){};

	virtual int QueryLibkey(CComboBox &_cbo);
	virtual int OpenPicPath(CEdit &_edt);
	virtual int OpenBoxPath(CEdit &_edt);
	virtual int	ShowPicture(char* _pPicData, int _iDataLen, CWnd* _pWnd);
	void GetNvsFileTime(CDateTimeCtrl* _pDt, OUT NVS_FILE_TIME &_tTime);
};

#endif

#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"

#include <map>
using namespace std;

struct ShowStreamInfo
{  
	int iChanNo;
	int iAlarmType;
	int iSimilarity;
//	char cName[LEN_64];
	CString cName;
	CImage* pImage[2]; 
};

#define MAX_SHOW_PIC_NUM	4

class CLS_DlgFaceStream : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceStream)

public:
	CLS_DlgFaceStream(CWnd* pParent = NULL);
	virtual ~CLS_DlgFaceStream();

	enum { IDD = IDD_DLG_CFG_FACE_STREAM };

	static int __stdcall OnNotify_PicStream(unsigned int _uiRecvID, long _lCommand, void* _pvBuf, int _iBufLen, void* _pvUser);
	void OnPicStream(unsigned int _uiRecvID, void* _pvBuf, int _iBufLen);
	void OnLogoff();

protected:
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()

private:
	unsigned int	m_uiRecvID;
	CImage*			m_pImageFullPic;
	CRITICAL_SECTION m_csStreamInfo;
	list<ShowStreamInfo> m_lstStreamInfo;
	CString	m_cstrPicStreamPath;
	void			ShowSnapPic();

public:
	CStatic 		m_stcFullPic;
	CStatic 		m_stcSnapPic[MAX_SHOW_PIC_NUM];
	CStatic 		m_stcNegPic[MAX_SHOW_PIC_NUM];
	CStatic 		m_stcSimilar[MAX_SHOW_PIC_NUM];

	virtual BOOL 	OnInitDialog();
	afx_msg void	OnPaint();
	afx_msg void 	OnBnClickedBtnStreamSrart();
	afx_msg void 	OnBnClickedBtnStreamStop();
};

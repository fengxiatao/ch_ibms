#ifndef _MESSAGEMANAGER_H
#define _MESSAGEMANAGER_H
#include <list>
using namespace std;

//100->200 Solve the problem of parameter change message failure 20161229
//20170424 Solve the problem that the message parameters of the heat map cover the problem that you need to use the demo to forward. The memory is too large. The application fails
#define		MESSAGE_MEMORY_SIZE		12000	
#define		MESSAGE_MEMORY_COUNT	100

class CLS_MessageManager
{
public:
	static CLS_MessageManager* Instance();
	static void Destroy();
	void* MallocMemory(int _iSize);
	void FreeMemory(void* _pMemory);
	
private:
	CRITICAL_SECTION m_csList;
	CLS_MessageManager(void);
	~CLS_MessageManager(void);

private:
	static CLS_MessageManager* s_pInstance;
	list<void*> m_lstFreeMemory;
};
#endif

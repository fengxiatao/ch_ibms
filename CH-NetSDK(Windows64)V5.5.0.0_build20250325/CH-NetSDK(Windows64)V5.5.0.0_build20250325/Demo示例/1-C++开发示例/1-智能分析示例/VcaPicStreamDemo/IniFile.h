#ifndef __INIFILE_H__
#define __INIFILE_H__

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define LINE_CONTENT_MAX_LEN 256

//using namespace std; 

void IniReadValue(char* section, char* key, char* val, const char* file);
int readStringValue(const char* section, char* key, char* val, const char* file);
int readIntValue(const char* section, char* key,int defaultvalue, const char* file);

void IniWriteValue(const char* section, char* key, char* val, const char* file);
int writeStringVlaue(const char* section, char* key, char* val, const char* file);
int writeIntValue(const char* section, char* key, int val, const char* file);

#endif


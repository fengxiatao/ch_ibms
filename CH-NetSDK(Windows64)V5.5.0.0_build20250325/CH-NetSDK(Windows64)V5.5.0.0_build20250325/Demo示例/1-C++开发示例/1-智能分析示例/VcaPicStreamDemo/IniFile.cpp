#include "IniFile.h"

void IniReadValue(char* section, char* key, char* val, const char* file)
{
	FILE* fp;
	int i = 0;
	int lineContentLen = 0;
	int position = 0;
	char lineContent[LINE_CONTENT_MAX_LEN] = {0};
//	bool bFoundSection = false;
//	bool bFoundKey = false;
	fp = fopen(file, "r");
	if(fp == NULL)
	{
		return;
	}
	while(feof(fp) == 0)
	{
		memset(lineContent, 0, LINE_CONTENT_MAX_LEN);
		fgets(lineContent, LINE_CONTENT_MAX_LEN, fp);
		if((lineContent[0] == ';') || (lineContent[0] == '\0') || (lineContent[0] == '\r') || (lineContent[0] == '\n'))
		{
			continue;
		}

		//check section
		if(strncmp(lineContent, section, strlen(section)) == 0)
		{
//			bFoundSection = true;
			while(feof(fp) == 0)
			{
				memset(lineContent, 0, LINE_CONTENT_MAX_LEN);
				fgets(lineContent, LINE_CONTENT_MAX_LEN, fp);
				//check key
				if(strncmp(lineContent, key, strlen(key)) == 0)
				{
//					bFoundKey = true;
					lineContentLen = strlen(lineContent);
					//find value
					for(i = strlen(key); i < lineContentLen; i++)
					{
						if(lineContent[i] == '=')
						{
							position = i + 1;
							break;
						}
					}
					if(i >= lineContentLen) 
					{
						break;
					}
					strncpy(val, lineContent + position, strlen(lineContent + position));
					lineContentLen = strlen(val);
					for(i = 0; i < lineContentLen; i++)
					{
						if((lineContent[i+position] == '\0') || (lineContent[i+position] == '\r') || (lineContent[i+position] == '\n'))
						{
							val[i] = '\0';
							break;
						}
					}  
				}
				else if(lineContent[0] == '[') 
				{
					break;
				}
			}
			break;
		}
	}
	//if(!bFoundSection){printf("No section = %s\n", section);}
	//else if(!bFoundKey){printf("No key = %s\n", key);}
	fclose(fp);
}

int readStringValue(const char* section, char* key, char* val, const char* file)
{
	char sect[LINE_CONTENT_MAX_LEN] = {0};
	if (section == NULL || key == NULL || val == NULL || file == NULL)
	{
		return -1;
	}

	sprintf(sect, "[%s]", section);
	IniReadValue(sect, key, val, file);

	return 0;
}

int readIntValue(const char* section, char* key,int defaultvalue, const char* file)
{
	char strValue[LINE_CONTENT_MAX_LEN] = {0};
	int iValue = defaultvalue;
	if(readStringValue(section, key, strValue, file) != 0)
	{
		return iValue;
	}

	if (strlen(strValue) > 0) 
	{
		iValue = atoi(strValue);
	}

	return iValue;
}

void IniWriteValue(const char* section, char* key, char* val, const char* file)
{
	FILE* fp;
	int err = 0;//i = 0
	//int lineContentLen = 0;
	//int position = 0;
	char lineContent[LINE_CONTENT_MAX_LEN]={0};
	char strWrite[LINE_CONTENT_MAX_LEN]={0};
//	bool bFoundSection = false;
//	bool bFoundKey = false;

	memset(lineContent, '\0', LINE_CONTENT_MAX_LEN);
	memset(strWrite, '\0', LINE_CONTENT_MAX_LEN);
	sprintf(strWrite, "%s=%s\n", key, val);
	fp = fopen(file, "r+");
	if(fp == NULL)
	{
		return;
	}
	while(feof(fp) == 0)
	{
		memset(lineContent, 0, LINE_CONTENT_MAX_LEN);
		fgets(lineContent, LINE_CONTENT_MAX_LEN, fp);
		if((lineContent[0] == ';') || (lineContent[0] == '\0') || (lineContent[0] == '\r') || (lineContent[0] == '\n'))
		{
			continue;
		}
		//check section
		if(strncmp(lineContent, section, strlen(section)) == 0)
		{
//			bFoundSection = true;
			while(feof(fp) == 0)
			{
				memset(lineContent, 0, LINE_CONTENT_MAX_LEN);
				fgets(lineContent, LINE_CONTENT_MAX_LEN, fp);
				//check key
				if(strncmp(lineContent, key, strlen(key)) == 0)
				{
//					bFoundKey = true;
					fseek(fp, (0-strlen(lineContent)),SEEK_CUR);
					err = fputs(strWrite, fp);
					if(err < 0)
					{
						break; 
					}
				}
				else if(lineContent[0] == '[') 
				{
					break;
				}
			}
			break;
		}
	}
	//if(!bFoundSection){printf("No section = %s\n", section);}
	//else if(!bFoundKey){printf("No key = %s\n", key);}
	fclose(fp);
}

int writeStringVlaue(const char* section, char* key, char* val, const char* file)
{
	char sect[LINE_CONTENT_MAX_LEN];
	if (section == NULL || key == NULL || val == NULL || file == NULL)
	{
		return -1;
	}
	memset(sect, '\0', LINE_CONTENT_MAX_LEN);
	sprintf(sect, "[%s]", section);
	IniWriteValue(sect, key, val, file);

	return 0;
}

int writeIntValue(const char* section, char* key, int val, const char* file)
{
	char strValue[LINE_CONTENT_MAX_LEN];
	memset(strValue, '\0', LINE_CONTENT_MAX_LEN);
	sprintf(strValue, "%-4d", val);

	writeStringVlaue(section, key, strValue, file);
	return 0;
}


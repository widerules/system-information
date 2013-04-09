void insert_sort(char* str, int len)
{
	int i;
	for (i = 1; i < len; i++)
	{
		int j = i - 1;
		char key = str[i];
		while (j >= 0 && (str[j] > key))
		{
			str[j+1] = str[j];
			j--;
		}
		str[j+1] = key;
	}
}

void revert(char* str, int len)
{
	if (len > 1)
	{
		char tmp = str[0];
		str[0] = str[len - 1];
		str[len - 1] = tmp;
		revert(str+1, len-2);
	}
	else
	{
		return;
	}
}

void block_revert(char* str)
{
	int len = 0;
	char* p = str;
	int length = strlen(str);
	int i;
	for (i = 0; i < length; i++)
	{
		if (str[i] == ' ')
		{
			revert(p, len);
			len = 0;
			p = &str[i+1];
		}
		else 
		{
			len++;
		}
	}
	revert(p, len);
}

void main()
{
	char* s1 = "this is a test ";
	char* s2 = "this";
	char s3[128];
	memset(s3, 0, 128);
	strcpy(s3, s2);
	revert(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	strcpy(s3, s1);
	block_revert(s3);
	printf("s3 is %s\n", s3);

	memset(s3, 0, 128);
	strcpy(s3, "54321edcba");
	insert_sort(s3, strlen(s3));
	printf("s3 is %s\n", s3);
}

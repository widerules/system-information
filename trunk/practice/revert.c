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
	revert(str, length);
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

int main(int argc, char* argv[])
{
	if (argc > 1) 
	{
		block_revert(argv[1]);
		printf("%s\n", argv[1]);
	}

	return 0;
}

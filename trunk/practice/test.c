#include <stdlib.h>

void shuffle(char* array, int len)
{
        int i;
        for (i = len-1; i > 0; i--)
        {
                int j = random() % i;
                int tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;
        }
}

void fast_sort()
{
}

void heap_sort()
{
}

void merge_sort()
{
}

void shell_sort()
{
}

void exange(char* str, int len)
{
	int i;
	for (i = len-1; i > 0; i--)
	{
		if (str[i] < str[i-1])
		{
			char tmp = str[i];
			str[i] = str[i-1];
			str[i-1] = tmp;
		}
	}
}

void bubble_sort_recurse(char* str, int len)
{
	if (len > 1)
	{
		exange(str, len);
		bubble_sort_recurse(str+1, len-1);
	}
	else return;
}

void bubble_sort(char* str, int len)
{
	int i;
	for (i = 0; i < len; i++)
	{
		int j;
		for (j = len-1; j > i; j--)
		{
			if (str[j] < str[j-1])
			{
				char tmp = str[j];
				str[j] = str[j-1];
				str[j-1] = tmp;
			}
		}
	}
}

void swap_min(char* str, int len)
{
	int position = 0;
	char key = str[position];
	int i;
	for (i = 1; i < len; i++)
	{
		if (key > str[i])
		{
			position = i;
			key = str[position];
		}
	}
	str[position] = str[0];
	str[0] = key;
}

void selection_sort_recurse(char* str, int len)
{
	if (len > 1)
	{
		swap_min(str, len);
		selection_sort_recurse(str+1, len-1);
	}
	else return;
}

void selection_sort(char* str, int len)
{
	int index = 0;
	while (index < len)
	{
		int position = index;
		char key = str[position];
		int i;
		for (i = index; i < len; i++)
		{
			if (key > str[i])
			{
				key = str[i];
				position = i;
			}
		}
		str[position] = str[index];
		str[index] = key;
		index++;
	}
}

void insert(char* str, int len)
{
	int j = len - 1;
	char key = str[len];
	while (j >= 0 && (str[j] > key))
	{
		str[j+1] = str[j];
		j--;
	}
	str[j+1] = key;
}

void insertion_sort_recurse(char* str, int len)
{
	if (len > 1)
	{
		insertion_sort_recurse(str, len-1);
		insert(str, len-1);
	}
	else return;
}

void insertion_sort(char* str, int len)
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
		printf("%s\n", argv[1]);
		block_revert(argv[1]);
		printf("%s\n", argv[1]);
	}

	int len = 52;
	char* array = (char*) malloc(len+1);
	int i;
	for (i = 0; i < len; i++) array[i] = i+1;
	array[len] = 0;
	for (i = 0; i < len; i++) printf("%d, ", array[i]); 
	printf("\n");
	shuffle(array, strlen(array));
	for (i = 0; i < len; i++) printf("%d, ", array[i]);
	printf("\n");
	bubble_sort(array, strlen(array));
	for (i = 0; i < len; i++) printf("%d, ", array[i]);
	printf("\n");
	free(array);

	printf("size of char is %d\n", sizeof(char));
	printf("size of int is %d\n", sizeof(int));

	char* s1 = "this is a test";
	char* s2 = "this";
	char s3[128];
	char* s4 = "C++";
	printf("size of C++ is %d\n", sizeof(s4));

	memset(s3, 0, 128);
	strcpy(s3, s2);
	revert(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	strcpy(s3, s1);
	block_revert(s3);
	printf("s3 is %s\n", s3);

	memset(s3, 0, 128);
	strcpy(s3, "54321edcba");
	insertion_sort(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	memset(s3, 0, 128);
	strcpy(s3, "54321edcba");
	insertion_sort_recurse(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	memset(s3, 0, 128);
	strcpy(s3, "54321edcba");
	selection_sort(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	memset(s3, 0, 128);
	strcpy(s3, "54321edcba");
	selection_sort_recurse(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	memset(s3, 0, 128);
	strcpy(s3, "54321edcba");
	bubble_sort(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	memset(s3, 0, 128);
	strcpy(s3, "54321edcba");
	bubble_sort_recurse(s3, strlen(s3));
	printf("s3 is %s\n", s3);

	return 0;
}

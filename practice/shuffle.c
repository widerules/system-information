#include <stdlib.h>

void shuffle(int* array, int len)
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

int main(int argc, char* argv[])
{
	int len = 52;
	int deck[len];
	int i;
	for (i = 0; i < len; i++) deck[i] = i;

	shuffle(deck, len);
	for (i = 0; i < len-1; i++) 
		printf("%d, ", deck[i]);
	printf("%d\n", deck[len-1]);

	return 0;
}

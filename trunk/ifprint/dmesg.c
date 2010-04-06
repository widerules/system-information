/* vi: set sw=4 ts=4: */
/*
 *
 * dmesg - display/control kernel ring buffer.
 *
 * Copyright 2006 Rob Landley <rob@landley.net>
 * Copyright 2006 Bernhard Reutner-Fischer <rep.nop@aon.at>
 *
 * Licensed under GPLv2, see file LICENSE in this tarball for details.
 */
#include <sys/klog.h>
#include <stdlib.h>
#include <stdio.h>

int main(int argc, char **argv)
{
	int len, level;
	char *buf;

	len = klogctl(10, NULL, 0); /* read ring buffer size */
	if (len < 16*1024)
		len = 16*1024;
	if (len > 16*1024*1024)
		len = 16*1024*1024;

	buf = malloc(len);
	len = klogctl(3, buf, len); /* read ring buffer */
	if (len < 0)
		return len;
	if (len == 0)
		return EXIT_SUCCESS;

	printf("%s\n", buf);
	free(buf);

	return EXIT_SUCCESS;
}

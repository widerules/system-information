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
#include <jni.h>

char *buf;
void dmesg()
{
	int len, level;

	len = klogctl(10, NULL, 0); /* read ring buffer size */
	if (len < 16*1024)
		len = 16*1024;
	if (len > 16*1024*1024)
		len = 16*1024*1024;

	buf = malloc(len);
	len = klogctl(3, buf, len); /* read ring buffer */
}

jstring Java_sys_info_trial_sysinfo_stringFromJNI( JNIEnv* env, jobject thiz )
{
    dmesg();
    jstring mesg = (*env)->NewStringUTF(env, buf);
    free(buf);
    return mesg;
}

jstring Java_system_info_reader_stringFromJNI( JNIEnv* env, jobject thiz )
{
    dmesg();
    jstring mesg = (*env)->NewStringUTF(env, buf);
    free(buf);
    return mesg;
}

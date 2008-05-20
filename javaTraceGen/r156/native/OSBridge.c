/*
 * This file is part of the Tuning Fork Visualization Platform
 *  (http://sourceforge.net/projects/tuningforkvp)
 *
 * Copyright (c) 2005 - 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */

#include <unistd.h>
#include <pthread.h>
#include <sched.h>
#include <sys/types.h>
#include <sys/syscall.h>

#include "com_ibm_tuningfork_tracegen_impl_OSBridge.h"



JNIEXPORT jint JNICALL 
Java_com_ibm_tuningfork_tracegen_impl_OSBridge_getThreadIdViaNative
(JNIEnv *env, jclass ignore)
{
#ifdef LINUX
	return (jint) syscall(__NR_gettid); /*	should be "gettid()" but there's some weird reason that it doesn't always work */
#else
	return (jint) pthread_self(); /* is this really returning a tid, or just an opaque pointer?? */
#endif
}

JNIEXPORT jint JNICALL 
Java_com_ibm_tuningfork_tracegen_impl_OSBridge_getProcessIdViaNative
(JNIEnv *env, jclass ignore)
{
	return (jint) getpid();
}

JNIEXPORT jint JNICALL 
Java_com_ibm_tuningfork_tracegen_impl_OSBridge_setProcessorAffinityViaNative
(JNIEnv *env, jclass ignore, jint cpu)
{
#ifdef LINUX
	cpu_set_t cpumask;
    __CPU_ZERO(&cpumask);
    __CPU_SET(cpu, &cpumask);
    int rc = sched_setaffinity(0, sizeof(cpumask), &cpumask);
    return rc;
#else
	return -1;
#endif
}

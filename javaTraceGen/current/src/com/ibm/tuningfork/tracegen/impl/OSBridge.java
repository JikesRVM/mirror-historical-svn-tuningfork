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

package com.ibm.tuningfork.tracegen.impl;

public class OSBridge {

    private static boolean loadAttempted = false;

    private static boolean nativesAreAvailable;

    public static final int NO_PID = -1;

    public static final String TF_SUPPORT_LIBRARY = "TuningForkNativeTraceSupport";

    /**
     * Determine whether natives are available, loading the library on demand.
     *
     *
     * @return true if natives are available, false otherwise
     */
    public static boolean initialize() {
	if (nativesAreAvailable) {
	    return true;
	} else if (loadAttempted) {
	    return false; // Don't keep banging our heads against the wall
	} else {
	    try {
		loadAttempted = true;
		System.loadLibrary(TF_SUPPORT_LIBRARY);
		nativesAreAvailable = true;
		return true;
	    } catch (Throwable t) {
		System.err
			.println("ERROR: TuningFork Java Tracing: Unable to load native operating system interface in '"
				+ TF_SUPPORT_LIBRARY + "'");
		// t.printStackTrace();
		// throw new Error("UNABLE TO LOAD TUNING FORK NATIVE SUPPORT
		// LIBRARY");
		return false;
	    }
	}
    }

    /* package */static int getProcessId() {
	if (nativesAreAvailable) {
	    return getProcessIdViaNative();
	} else {
	    return NO_PID;
	}
    }

    /* package */public static int getThreadId() {
	if (nativesAreAvailable) {
	    int tid = getThreadIdViaNative();
	    return tid >= 0 ? tid : NO_PID;
	} else {
	    return NO_PID;
	}
    }

    /* package */static int setProcessorAffinity(int cpu) {
	if (nativesAreAvailable) {
	    return setProcessorAffinityViaNative(cpu);
	} else {
	    return -1;
	}
    }

    private native static int getThreadIdViaNative();

    private native static int getProcessIdViaNative();

    private native static int setProcessorAffinityViaNative(int cpu);
}

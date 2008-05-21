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


abstract class TuningForkDaemonThread extends Thread {

    public final Logger logger;

    TuningForkDaemonThread(Logger logger) {
	this.logger = logger;
	setDaemon(true);
    }

    public final void run() {
	setProcessorAffinity();
	derivedRun();
    }

    public abstract void derivedRun();

    public void setProcessorAffinity() {
	int cpu = logger.getProcessorAffinity();
	if (cpu >= 0) {
	    int rc = OSBridge.setProcessorAffinity(cpu);
	    if (rc != 0) {
		System.err
			.println("WARNING: TuningFork Java Tracing unable to bind "
				+ getClass().getSimpleName()
				+ " to processor "
				+ cpu);
	    }
	}
    }
}
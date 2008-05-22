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

import com.ibm.tuningfork.tracegen.IFeedlet;
import com.ibm.tuningfork.tracegen.types.EventType;


/**
 * A feedlet whose operations are synchronized so that it can be shared between
 * multiple threads.
 *
 * SharedFeedlets should be used with care since the presence of synchronization
 * may cause perturbation of the traced system (probe effect) due to the use of
 * locks on the shared feedlet resource. However, they are a good choice when
 * such probe effect is acceptable and the simplicity at either the trace source
 * or the data visualization are enhanced by just having a single feedlet.
 *
 * Note that for tracing a shared resource with its own synchronization (eg a
 * shared hash table) a better choice would be to either use a feedlet dedicated
 * to the shared resource which relies on the resource's synchronization, or to
 * emit per-feedlet events for the client threads.
 */
public class SharedFeedlet implements IFeedlet {

    IFeedlet feedlet;

    public SharedFeedlet(Logger logger, int index, String name,
	    String description) {
	feedlet = new Feedlet(logger, index, name, description);
    }

    public SharedFeedlet(IFeedlet feedlet) {
	this.feedlet = feedlet;
    }

    public synchronized void addEvent(EventType et) {
	feedlet.addEvent(et);
    }

    public synchronized void addEvent(EventType et, int v) {
	feedlet.addEvent(et, v);
    }

    public synchronized void addEvent(EventType et, long v) {
	feedlet.addEvent(et, v);
    }

    public synchronized void addEvent(EventType et, double v) {
	feedlet.addEvent(et, v);
    }

    public synchronized void addEvent(EventType et, String v) {
	feedlet.addEvent(et, v);
    }

    public synchronized void addEvent(EventType et, int[] idata, long[] ldata,
	    double[] ddata, String[] sdata) {
	feedlet.addEvent(et, idata, ldata, ddata, sdata);
    }

    public void bindToCurrentThread() {
	// Note: should not really be used for shared feedlets. Is it more
	// robust to ignore or to throw an exception??
	feedlet.bindToCurrentThread();
    }

    public boolean isManual() {
	return feedlet.isManual();
    }

    public boolean setTime(long timeStamp) {
	return feedlet.setTime(timeStamp);
    }

    public boolean recordThreadId() {
	// Note: should not really be used for shared feedlets. Is it more
	// robust to ignore or to throw an exception??
	return feedlet.recordThreadId();
    }

    public void setThreadId(int tid) {
	// Note: should not really be used for shared feedlets. Is it more
	// robust to ignore or to throw an exception??
	feedlet.setThreadId(tid);
    }
}

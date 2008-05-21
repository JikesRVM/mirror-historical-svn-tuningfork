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
import com.ibm.tuningfork.tracegen.chunk.EventChunk;
import com.ibm.tuningfork.tracegen.types.EventType;

/**
 * Functionality common to Feedlets and ConversionFeedlets.
 */
public abstract class AbstractFeedlet {

    public boolean doCheckType = false; // Perhaps make this dynamically
					// modifiable

    protected final int index;
    protected final String name;
    protected final String desc;
    private int sequenceNumber = 0;
    protected EventChunk eventChunk;
    protected final Logger logger;

    public abstract long getTime();

    public AbstractFeedlet(Logger logger, int index, String name, String description) {
	this.logger = logger;
	this.index = index;
	this.name = name;
	this.desc = description;
    }

    final int getIndex() {
	return index;
    }

    public final String getName() {
	return name;
    }

    public final String getDescription() {
	return desc;
    }

    public final int getNextSequenceNumber() {
	return sequenceNumber++;
    }

    /*
     * private static ThreadLocal<Feedlet> feedletBinding = new ThreadLocal<Feedlet>(); //
     * 1.5
     *
     * public void bindToCurrentThread() { feedletBinding.set(this); }
     *
     * public static Feedlet getBoundFeedlet() { return feedletBinding.get(); }
     */

    public void bindToCurrentThread() {
	Logger.threadLocalFeedlets.add(this);
	recordThreadId();
    }

    public static Feedlet getBoundFeedlet() {
	return (Feedlet) Logger.threadLocalFeedlets.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.IFeedlet#addEvent(com.ibm.tuningfork.tracegen.EventType)
     */
    public final void addEvent(EventType et) {
	if (logger.feedletMode == Logger.NULL_MODE) {
	    return;
	}
	addEventInternal(getTime(), et);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.IFeedlet#addEvent(com.ibm.tuningfork.tracegen.EventType,
     *      int)
     */
    public final void addEvent(EventType et, int v) {
	if (logger.feedletMode == Logger.NULL_MODE) {
	    return;
	}
	addEventInternal(getTime(), et, v);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.IFeedlet#addEvent(com.ibm.tuningfork.tracegen.EventType,
     *      long)
     */
    public final void addEvent(EventType et, long v) {
	if (logger.feedletMode == Logger.NULL_MODE) {
	    return;
	}
	addEventInternal(getTime(), et, v);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.IFeedlet#addEvent(com.ibm.tuningfork.tracegen.EventType,
     *      double)
     */
    public final void addEvent(EventType et, double v) {
	if (logger.feedletMode == Logger.NULL_MODE) {
	    return;
	}
	addEventInternal(getTime(), et, v);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.IFeedlet#addEvent(com.ibm.tuningfork.tracegen.EventType,
     *      java.lang.String)
     */
    public final void addEvent(EventType et, String v) {
	if (logger.feedletMode == Logger.NULL_MODE) {
	    return;
	}
	addEventInternal(getTime(), et, v);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.IFeedlet#addEvent(com.ibm.tuningfork.tracegen.EventType,
     *      int[], long[], double[], java.lang.String[])
     */
    public final void addEvent(EventType et, int[] idata, long[] ldata,
	    double[] ddata, String[] sdata) {
	if (logger.feedletMode == Logger.NULL_MODE) {
	    return;
	}
	addEventInternal(getTime(), et, idata, ldata, ddata, sdata);
    }

    public void flush() {
	logger.returnFullEventChunk(eventChunk);
	eventChunk = null;
    }

    protected final void ensureEventChunk() {
	if (eventChunk == null) {
	    eventChunk = logger.getEmptyEventChunk();
	    if (eventChunk == null) {
		System.out.println("Feedlet.addEvent failed because there are no event chunks available");
		return;
	    }
	    eventChunk.reset(getIndex(), getNextSequenceNumber());
	}
    }

    protected final boolean checkType(EventType et, int numInt, int numLong,
	    int numDouble, int numString) {
	if (!et.admits(numInt, numLong, numDouble, numString)) {
	    System.err.println("Feedlet.addEvent(" + et.getName()
		    + ",...) called with incompatible argument types");
	    return false;
	}
	return true;
    }

    private void addEventInternal(long timeStamp, EventType et) {
	if (doCheckType && !checkType(et, 0, 0, 0, 0)) {
	    return;
	}
	while (true) {
	    ensureEventChunk();
	    if (eventChunk.addEvent(timeStamp, et)) {
		return;
	    }
	    flush();
	}
    }

    private void addEventInternal(long timeStamp, EventType et, int v) {
	if (doCheckType && !checkType(et, 1, 0, 0, 0)) {
	    return;
	}
	while (true) {
	    ensureEventChunk();
	    if (eventChunk.addEvent(timeStamp, et, v)) {
		return;
	    }
	    flush();
	}
    }

    private void addEventInternal(long timeStamp, EventType et, long v) {
	if (doCheckType && !checkType(et, 0, 1, 0, 0)) {
	    return;
	}
	while (true) {
	    ensureEventChunk();
	    if (eventChunk.addEvent(timeStamp, et, v)) {
		return;
	    }
	    flush();
	}
    }

    private void addEventInternal(long timeStamp, EventType et, double v) {
	if (doCheckType && !checkType(et, 0, 0, 1, 0)) {
	    return;
	}
	while (true) {
	    ensureEventChunk();
	    if (eventChunk.addEvent(timeStamp, et, v)) {
		return;
	    }
	    flush();
	}
    }

    private void addEventInternal(long timeStamp, EventType et, String v) {
	if (doCheckType && !checkType(et, 0, 0, 0, 1)) {
	    return;
	}
	while (true) {
	    ensureEventChunk();
	    if (eventChunk.addEvent(timeStamp, et, v)) {
		return;
	    }
	    flush();
	}
    }

    private final void addEventInternal(long timeStamp, EventType et,
	    int[] idata, long[] ldata, double[] ddata, String[] sdata) {
	int ilen = (idata == null) ? 0 : idata.length;
	int llen = (ldata == null) ? 0 : ldata.length;
	int dlen = (ddata == null) ? 0 : ddata.length;
	int slen = (sdata == null) ? 0 : sdata.length;
	if (doCheckType && !checkType(et, ilen, llen, dlen, slen)) {
	    return;
	}
	while (true) {
	    ensureEventChunk();
	    if (eventChunk.addEvent(timeStamp, et, idata, ldata, ddata, sdata)) {
		return;
	    }
	    flush();
	}
    }

    public boolean recordThreadId() {
	int tid = OSBridge.getThreadId();
	if (tid != OSBridge.NO_PID) {
	    setThreadId(tid);
	    return true;
	} else {
	    return false;
	}
    }

    public void setThreadId(int tid) {
	logger.addFeedletProperty(this, IFeedlet.TID_PROPERTY, "" + tid);
    }
}

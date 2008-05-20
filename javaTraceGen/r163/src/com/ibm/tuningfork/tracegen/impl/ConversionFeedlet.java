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

import com.ibm.tuningfork.tracegen.IConversionFeedlet;
import com.ibm.tuningfork.tracegen.ITimerEvent;
import com.ibm.tuningfork.tracegen.IValueEvent;
import com.ibm.tuningfork.tracegen.types.EventType;


/**
 * A conversion feedlet is used when you want to generate a feed where the
 * timestamps are coming from a different source than the current time (eg when
 * converting a trace file in a different format into a TuningFork trace file).
 */
final class ConversionFeedlet extends AbstractFeedlet implements
	IConversionFeedlet {

    protected long timestamp = 0;

    public long getTime() {
	return timestamp;
    }

    public boolean setTime(long timestamp) {
	if (timestamp < this.timestamp) { // could have the same timestamp:
					    // Wait and Wait_Synthesized
	    System.err.println("Warning: time is going backwards");
	}
	this.timestamp = timestamp;
	return true;
    }

    public ConversionFeedlet(Logger logger, int index, String name,
	    String description) {
	super(logger, index, name, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.tuningfork.tracegen.IConversionFeedlet#addEvent(long,
     *      com.ibm.tuningfork.tracegen.EventType)
     */
    public void addEvent(long timestamp, EventType et) {
	setTime(timestamp);
	addEvent(et);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.tuningfork.tracegen.IConversionFeedlet#addEvent(long,
     *      com.ibm.tuningfork.tracegen.EventType, int)
     */
    public void addEvent(long timestamp, EventType et, int v) {
	setTime(timestamp);
	addEvent(et, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.tuningfork.tracegen.IConversionFeedlet#addEvent(long,
     *      com.ibm.tuningfork.tracegen.EventType, long)
     */
    public void addEvent(long timestamp, EventType et, long v) {
	setTime(timestamp);
	addEvent(et, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.tuningfork.tracegen.IConversionFeedlet#addEvent(long,
     *      com.ibm.tuningfork.tracegen.EventType, double)
     */
    public void addEvent(long timestamp, EventType et, double v) {
	setTime(timestamp);
	addEvent(et, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.tuningfork.tracegen.IConversionFeedlet#addEvent(long,
     *      com.ibm.tuningfork.tracegen.EventType, java.lang.String)
     */
    public void addEvent(long timestamp, EventType et, String v) {
	setTime(timestamp);
	addEvent(et, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.tuningfork.tracegen.IConversionFeedlet#addEvent(long,
     *      com.ibm.tuningfork.tracegen.EventType, int[], long[],
     *      double[], java.lang.String[])
     */
    public void addEvent(long timestamp, EventType et, int[] idata,
	    long[] ldata, double[] ddata, String[] sdata) {
	setTime(timestamp);
	addEvent(et, idata, ldata, ddata, sdata);
    }

    public void addValue(long timestamp, IValueEvent event, double value) {
	setTime(timestamp);
	event.addValue(this, timestamp, value);
    }

    public void startTimer(long timestamp, ITimerEvent event) {
	setTime(timestamp);
	event.start(this, timestamp);
    }

    public void stopTimer(long timestamp, ITimerEvent event) {
	setTime(timestamp);
	event.stop(this, timestamp);
    }

    public boolean recordThreadId() {
	// When converting, there is no "running thread" to query; just fail.
	return false;
    }
}
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

package com.ibm.tuningfork.tracegen;

import com.ibm.tuningfork.tracegen.types.EventType;


/**
 * A feedlet allows the user to add events. The set of parameters used to
 * addEvent is validated against the data expected by the passed EventType.
 *
 * Feedlets are not thread safe, see ILogger for a discussion.
 *
 * @see com.ibm.tuningfork.tracegen.ILogger
 */
public interface IFeedlet {

    public static final String TID_PROPERTY = "tid";

    /**
     * Does this feedlet require manual maintenance of time via the setClock
     * call?
     */
    public boolean isManual();

    /**
     * Sets the current time of the clock maintained by the feedlet. Returns
     * whether the set succeeded.
     */
    public boolean setTime(long timeStamp);

    /**
     * Bind this feedlet to the current thread, so that it is possible to
     * trigger events without specifying the feedlet.
     */
    public void bindToCurrentThread();

    /**
     * Add an event to the feedlet.
     *
     * @param eventType
     *                The type of the event.
     */
    public void addEvent(EventType eventType);

    /**
     * Add an event to the feedlet.
     *
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(EventType eventType, int v);

    /**
     * Add an event to the feedlet.
     *
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(EventType eventType, long v);

    /**
     * Add an event to the feedlet.
     *
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(EventType eventType, double v);

    /**
     * Add an event to the feedlet.
     *
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(EventType eventType, String v);

    /**
     * Add an event to the feedlet.
     *
     * @param eventType
     *                The type of the event.
     * @param idata
     *                The int values for the event.
     * @param ldata
     *                The long values for the event.
     * @param ddata
     *                The double values for the event.
     * @param sdata
     *                The String values for the event.
     */
    public void addEvent(EventType eventType, int[] idata, long[] ldata,
	    double[] ddata, String[] sdata);

    /**
     * Set the thread ID property of this feedlet. This is useful for relating
     * the feedlet to the associated information in OS or JVM traces.
     *
     * @param tid
     *                Thread ID for this feedlet
     */
    public void setThreadId(int tid);

    /**
     * Automatically determine the thread ID of the currently running thread and
     * use it to set the thread ID property of this feedlet.
     *
     * Note that this is to some degree system dependent and will require the
     * installation of some native code or scripts.
     *
     * @return True if succeeded, false if unable to determine thread ID
     */
    public boolean recordThreadId();

}
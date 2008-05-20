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
 * A conversion feedlet is similar to a feedlet but allows the user to
 * explicitly supply timestamp values. This is useful for generating simulated
 * traces or for translating traces from another format.
 */
public interface IConversionFeedlet {

    /**
     * Add a value event to the feedlet.
     * 
     * @param timestamp
     *                The time at which the event occurred
     * @param event
     *                The value event type
     * @param value
     *                The data value
     */
    public void addValue(long timestamp, IValueEvent event, double value);

    /**
     * Log the beginning of a time interval to the feedlet.
     * 
     * @param timestamp
     *                The time at which the event occurred
     * @param event
     *                The timer event
     */
    public void startTimer(long timestamp, ITimerEvent event);

    /**
     * Log the end of a time interval to the feedlet.
     * 
     * @param timestamp
     *                The time at which the event occurred
     * @param event
     *                The timer event
     */
    public void stopTimer(long timestamp, ITimerEvent event);

    /**
     * Add an event to the feedlet.
     * 
     * @param timestamp
     *                The event time stamp.
     * @param eventType
     *                The type of the event.
     */
    public void addEvent(long timestamp, EventType eventType);

    /**
     * Add an event to the feedlet.
     * 
     * @param timestamp
     *                The event time stamp.
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(long timestamp, EventType eventType, int v);

    /**
     * Add an event to the feedlet.
     * 
     * @param timestamp
     *                The event time stamp.
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(long timestamp, EventType eventType, long v);

    /**
     * Add an event to the feedlet.
     * 
     * @param timestamp
     *                The event time stamp.
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(long timestamp, EventType eventType, double v);

    /**
     * Add an event to the feedlet.
     * 
     * @param timestamp
     *                The event time stamp.
     * @param eventType
     *                The type of the event.
     * @param v
     *                The event value.
     */
    public void addEvent(long timestamp, EventType eventType, String v);

    /**
     * Add an event to the feedlet.
     * 
     * @param timestamp
     *                The event time stamp.
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
    public void addEvent(long timestamp, EventType eventType, int[] idata,
	    long[] ldata, double[] ddata, String[] sdata);

}
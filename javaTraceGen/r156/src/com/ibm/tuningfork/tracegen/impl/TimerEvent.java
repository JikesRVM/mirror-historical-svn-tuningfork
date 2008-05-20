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
import com.ibm.tuningfork.tracegen.IFeedlet;
import com.ibm.tuningfork.tracegen.ITimerEvent;
import com.ibm.tuningfork.tracegen.types.EventType;


/**
 * A set of events and supports functions to support time intervals.
 */
final class TimerEvent implements ITimerEvent {

    private final EventType onET, offET;
    private final String name;

    public TimerEvent(Logger logger, String name, String prefix) {
	onET = new EventType(prefix + "Interval Start: " + name, name
		+ " starting");
	offET = new EventType(prefix + "Interval Stop: " + name, name
		+ " ending");
	this.name = name;
	logger.addEventType(onET);
	logger.addEventType(offET);
    }

    public TimerEvent(Logger logger, String name) {
	this(logger, name, "");
    }

    public void start() {
	Feedlet.getBoundFeedlet().addEvent(onET);
    }

    public void stop() {
	Feedlet.getBoundFeedlet().addEvent(offET);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ITimerEvent#start(com.ibm.tuningfork.tracegen.IFeedlet)
     */
    public void start(IFeedlet feedlet) {
	feedlet.addEvent(onET);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ITimerEvent#stop(com.ibm.tuningfork.tracegen.IFeedlet)
     */
    public void stop(IFeedlet feedlet) {
	feedlet.addEvent(offET);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ITimerEvent#start(com.ibm.tuningfork.tracegen.IFeedlet)
     */
    public void start(IConversionFeedlet feedlet, long timeStamp) {
	feedlet.addEvent(timeStamp, onET);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ITimerEvent#stop(com.ibm.tuningfork.tracegen.IFeedlet)
     */
    public void stop(IConversionFeedlet feedlet, long timeStamp) {
	feedlet.addEvent(timeStamp, offET);
    }

    public String getName() {
	return name;
    }

}
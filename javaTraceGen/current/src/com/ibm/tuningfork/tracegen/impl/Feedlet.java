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

public final class Feedlet extends AbstractFeedlet implements IFeedlet {

    public static final long TICK_FREQUENCY = 1000000000L;
    private long timeStamp;

    public final boolean isManual() {
	return logger.feedletMode == Logger.MANUAL_CLOCK_MODE;
    }

    public final boolean setTime(long timeStamp) {
	if (logger.feedletMode != Logger.MANUAL_CLOCK_MODE) {
	    return false;
	}
	this.timeStamp = timeStamp;
	return true;
    }

    public final long getTime() {
	if (logger.feedletMode == Logger.MANUAL_CLOCK_MODE) {
	    return timeStamp;
	}
	// FIXME: For 1.4: return System.currentTimeMillis() * (TICK_FREQUENCY /
	// 1000);
	return System.nanoTime();
    }

    public Feedlet(Logger logger, int index, String name, String description) {
	super(logger, index, name, description);
    }
}

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

/**
 * A value event, which consists of a series of data points recorded over time.
 */
public interface IValueEvent extends IEvent {
    /**
     * Add a new value using the feedlet bound to the current thread.
     * 
     * @param value
     *                The value.
     */
    public void addValue(double value);

    /**
     * Add a new value using the specified feedlet.
     * 
     * @param feedlet
     *                The feedlet to use.
     * @param value
     *                The value.
     */
    public void addValue(IFeedlet feedlet, double value);

    /**
     * Add a new value using the specified feedlet.
     * 
     * @param feedlet
     *                The feedlet to use.
     * @param value
     *                The value.
     */
    public void addValue(IConversionFeedlet feedlet, long timeStamp,
	    double value);

}
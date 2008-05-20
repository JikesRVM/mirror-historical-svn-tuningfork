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
 * A timer event, which consists of a series of start and stop pairs over time.
 */
public interface ITimerEvent extends IEvent {

    /**
     * Start the timer using the feedlet bound to the current thread.
     */
    public void start();

    /**
     * Stop the timer using the feedlet bound to the current thread.
     */
    public void stop();

    /**
     * Start the timer using a specific feedlet.
     * 
     * @param feedlet
     *                The feedlet to use.
     */
    public void start(IFeedlet feedlet);

    /**
     * Start the timer using a specific feedlet.
     * 
     * @param feedlet
     *                The feedlet to use.
     */
    public void stop(IFeedlet feedlet);

    /**
     * Start the timer using a specific feedlet.
     * 
     * @param feedlet
     *                The feedlet to use.
     */
    public void start(IConversionFeedlet feedlet, long timeStamp);

    /**
     * Start the timer using a specific feedlet.
     * 
     * @param feedlet
     *                The feedlet to use.
     */
    public void stop(IConversionFeedlet feedlet, long timeStamp);

}
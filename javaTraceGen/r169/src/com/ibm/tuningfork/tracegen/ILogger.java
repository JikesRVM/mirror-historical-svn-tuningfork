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
 * A logger is responsible for the generation of a complete trace, composed of
 * several feedlets. Once an ILogger has been created, the programmer can
 * proceed to create feedlets.
 * 
 * An ILogger instance is thread safe, so the programmer need not synchronize
 * threads calling methods on the ILogger.
 * 
 * Feedlets are not thread safe. To ensure thread-safety, the programmer may
 * bind feedlets to threads using the bindToCurrentThread on an IFeedlet, and
 * the library will remember the feedlet when calling methods in events that do
 * not take a IFeedlet argument.
 * 
 * Alternatively, the user may take on the responsibility for concurrency
 * control and explicitly pass the IFeedlet instance in when triggering events.
 */
public interface ILogger extends IGenericLogger {

    /**
     * Create a new feedlet.
     * 
     * @param name
     *                The feedlet name.
     * @param description
     *                The feedlet description.
     * @return The new IFeedlet instance.
     */
    public IFeedlet makeFeedlet(String name, String description);
}
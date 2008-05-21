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
 * An IConversionLogger and its related classes is similar to an ILogger except
 * that the addition of all events, whether directly or indirectly, requires
 * passing an explicit timestamp. This mode is useful for batch conversion from
 * other file formats into TuningFork trace files.
 */
public interface IConversionLogger extends IGenericLogger {
    /**
     * Create a new conversion feedlet.
     * 
     * @param name
     *                The feedlet name.
     * @param description
     *                The feedlet description.
     * @return The new IConversionFeedlet instance.
     */
    public IConversionFeedlet makeConversionFeedlet(String name,
	    String description);

}
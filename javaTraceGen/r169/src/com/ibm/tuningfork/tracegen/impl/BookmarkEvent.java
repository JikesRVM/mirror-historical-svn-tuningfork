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

import com.ibm.tuningfork.tracegen.IBookmarkEvent;
import com.ibm.tuningfork.tracegen.IFeedlet;
import com.ibm.tuningfork.tracegen.types.EventAttribute;
import com.ibm.tuningfork.tracegen.types.EventType;
import com.ibm.tuningfork.tracegen.types.ScalarType;

/**
 * A set of events and supports functions to support time intervals.
 */
final class BookmarkEvent implements IBookmarkEvent {

    private static final String PREFIX = "Bookmark: ";

    private final EventType bookmarkET;
    private final String name;

    public BookmarkEvent(Logger logger, String name) {
	EventAttribute attribute = new EventAttribute(name, name,
		ScalarType.STRING);
	bookmarkET = new EventType(PREFIX + name, name, attribute);
	logger.addEventType(bookmarkET);
	this.name = name;
    }

    public void addBookmark(String val) {
	Feedlet.getBoundFeedlet().addEvent(bookmarkET, val);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.IValueEvent#addValue(com.ibm.tuningfork.tracegen.IFeedlet,
     *      double)
     */
    public void addBookmark(IFeedlet feedlet, String val) {
	feedlet.addEvent(bookmarkET, val);
    }

    public String getName() {
	return name;
    }
}

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

package com.ibm.tuningfork.tracegen.chunk;

import com.ibm.tuningfork.tracegen.types.EventTypeSpaceVersion;

public class EventTypeSpaceChunk extends Chunk {

    public static final int EVENT_TYPE_SPACE_ID = 3;

    public EventTypeSpaceChunk(EventTypeSpaceVersion eventTypeSpaceVersion) {
	super(EVENT_TYPE_SPACE_ID, DATA_OFFSET + encodingSpace(eventTypeSpaceVersion.name) + ENCODING_SPAGE_INT);
	addString(eventTypeSpaceVersion.name);
	addInt(eventTypeSpaceVersion.version);
	close();
    }

}

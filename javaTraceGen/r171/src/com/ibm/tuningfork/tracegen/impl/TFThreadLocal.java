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

/*
 * A mapping of thread-local objects without synchronizing on lookup
 * 
 * This is provided instead of the Java 5 ThreadLocal class since we want
 * pre-5.0 compatibility. It has the advantage of being more efficient. It has
 * the disadvantage of having a fixed size and no automatic dispose when the
 * thread goes away. Correctness is based on monotonicity of the added threads,
 * the initial null values which allow the existence of a race to be detected,
 * and the use of a synchronized method to get values when that occurs.
 * 
 */

class TFThreadLocal {
    public static final int DEFAULT_SIZE = 512;

    private int cursor;
    private Thread[] threads;
    private Object[] objects;

    private static final Thread NO_THREAD = new Thread();

    public TFThreadLocal(int size) {
	threads = new Thread[size];
	objects = new Object[size];
	cursor = 0;
    }

    public TFThreadLocal() {
	this(DEFAULT_SIZE);
    }

    public synchronized void add(Thread thread, Object object) {
	if (cursor < threads.length) {
	    threads[cursor] = thread;
	    objects[cursor] = object;
	    cursor++;
	    // System.err.println("TL add thread " + thread + " value " +
	    // ((Feedlet) object).desc);
	} else {
	    throw new RuntimeException("ThreadLocal structure is full");
	}
    }

    public synchronized void add(Object object) {
	add(Thread.currentThread(), object);
    }

    public Object get() {
	return get(Thread.currentThread());
    }

    public Object get(Thread thread) {
	for (int i = 0; i < cursor; i++) {
	    Thread iThread = threads[i];
	    Object iObject = objects[i];
	    if (iThread == null || iObject == null) { // Race between
							// unsynchronized get
							// and synchronized add
		return unracyGet(thread);
	    } else if (iThread == thread) {
		// System.err.println("TL get thread " + thread + " found " +
		// ((Feedlet) iObject).desc);
		return objects[i];
	    }
	}

	return null;
    }

    public synchronized Object unracyGet(Thread thread) {
	for (int i = 0; i < cursor; i++) {
	    if (threads[i] == thread) {
		// System.err.println(" RACY!!!!!!! TL get thread " + thread + "
		// found " + ((Feedlet) objects[i]).desc);
		return objects[i];
	    }
	}

	return null;
    }

    /*
     * Note: since get() is unsynchronized, the user is responsible for a
     * meta-level synchronization protocol that prevents get() from being called
     * after or concurrently with remove().
     */
    public synchronized boolean remove(Thread thread) {
	for (int i = 0; i < cursor; i++) {
	    if (threads[i] == thread) {
		threads[i] = NO_THREAD;
		objects[i] = null;
		if (i == cursor - 1) {
		    cursor--;
		}
		return true;
	    }
	}

	return false; // Not found
    }
}
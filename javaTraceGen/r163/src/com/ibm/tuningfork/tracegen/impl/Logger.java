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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import com.ibm.tuningfork.tracegen.IBookmarkEvent;
import com.ibm.tuningfork.tracegen.IConversionFeedlet;
import com.ibm.tuningfork.tracegen.IConversionLogger;
import com.ibm.tuningfork.tracegen.IEvent;
import com.ibm.tuningfork.tracegen.IFeedlet;
import com.ibm.tuningfork.tracegen.ILogger;
import com.ibm.tuningfork.tracegen.ITimerEvent;
import com.ibm.tuningfork.tracegen.IValueEvent;
import com.ibm.tuningfork.tracegen.chunk.EventChunk;
import com.ibm.tuningfork.tracegen.chunk.EventTypeChunk;
import com.ibm.tuningfork.tracegen.chunk.EventTypeSpaceChunk;
import com.ibm.tuningfork.tracegen.chunk.FeedHeaderChunk;
import com.ibm.tuningfork.tracegen.chunk.FeedletChunk;
import com.ibm.tuningfork.tracegen.chunk.PropertyTableChunk;
import com.ibm.tuningfork.tracegen.chunk.RawChunk;
import com.ibm.tuningfork.tracegen.chunk.StringTableChunk;
import com.ibm.tuningfork.tracegen.types.EventType;
import com.ibm.tuningfork.tracegen.types.EventTypeSpaceVersion;

public class Logger implements ILogger, IConversionLogger {

    private static final int INITIAL_NUMBER_OF_EVENT_CHUNKS = 20;
    private static int ADDITIONAL_NUMBER_OF_EVENT_CHUNKS_PER_FEEDLET = 4;
    private static final int INTER_FLUSH_TIME_MS = 200;
    private final boolean DEBUG_SOCKET = true;

    // We are either in file mode or socket mode.
    private File file;
    private int portNumber;

    // Loggers either create a new trace or convert from a pre-existing one
    public final static int NULL_MODE = 0;
    public final static int AUTO_CLOCK_MODE = 1;
    public final static int MANUAL_CLOCK_MODE = 2;
    public final static int CONVERSION_MODE = 3;
    public int feedletMode = AUTO_CLOCK_MODE;

    // Logging is sent to an outputStream which links to either a file or a
    // socket.
    private OutputStream outputStream;

    // Thread-id indexed feedlets - need to make weak
    static HashMap/* <String, Feedlet> */feedletsByThread = new HashMap/*
									 * <String,
									 * Feedlet>
									 */();

    // The integrity of these data structures is guarded by method-level
    // synchronization
    private EventTypeSpaceVersion[] eventTypeSpaces;
    private final ArrayList/* <RawChunk> */closedMetaChunks = new ArrayList/* <RawChunk> */();
    private final ArrayList/* <RawChunk> */oldMetaChunks = new ArrayList/* <RawChunk> */();
    private PropertyTableChunk propertyTableChunk = new PropertyTableChunk();
    private StringTableChunk stringTableChunk = new StringTableChunk();
    private FeedletChunk feedletChunk = new FeedletChunk();
    private EventTypeChunk eventTypeChunk = new EventTypeChunk();
    private int feedletId = 0;
    private final ArrayList/* <AbstractFeedlet> */feedlets = new ArrayList/* <AbstractFeedlet> */();
    private int stringIndex = 0;

    private int eventChunkCount = 0;
    private int feedletCount = 0;

    private final static int UNSTARTED = 0;
    private final static int RUNNING = 1;
    private final static int SHUTTING_DOWN = 2;
    private final static int SHUT_DOWN = 3;
    private int loggerMode = UNSTARTED;

    // These two structures are not guarded using method synchronization but
    // by a separate lock to reduce contention while the logger thread is
    // running.
    private final Object eventChunkLock = new Object();
    private final ArrayList/* <EventChunk> */readyEventChunks = new ArrayList/* <EventChunk> */();
    private final ArrayList/* <EventChunk> */fullEventChunks = new ArrayList/* <EventChunk> */();

    private final int processorAffinity;
    public static final int NO_PROCESSOR_AFFINITY = -1;
    public static final int DEFAULT_PROCESSOR_AFFINITY = 0; // FIXME: should
							    // default to none;
							    // but must modify
							    // API

    static final TFThreadLocal threadLocalFeedlets = new TFThreadLocal();

    public Logger() {
	feedletMode = NULL_MODE;
	this.processorAffinity = NO_PROCESSOR_AFFINITY;
    }

    Logger(File file, EventTypeSpaceVersion[] eventTypeSpaces,
	    long tickFrequency, int feedletMode, int processorAffinity)
	    throws FileNotFoundException, IOException {
	this.file = file;
	this.feedletMode = feedletMode;
	outputStream = new FileOutputStream(this.file);
	this.eventTypeSpaces = eventTypeSpaces;
	this.processorAffinity = processorAffinity;
	init(tickFrequency);
	writeOldMetaChunks(outputStream);
    }

    public Logger(File file, EventTypeSpaceVersion[] eventTypeSpaces,
	    long tickFrequency, int feedletMode) throws FileNotFoundException,
	    IOException {
	this(file, eventTypeSpaces, tickFrequency, feedletMode,
		DEFAULT_PROCESSOR_AFFINITY);
    }

    Logger(int portNum, EventTypeSpaceVersion[] eventTypeSpaces,
	    long tickFrequency, int feedletMode, int processorAffinity)
	    throws FileNotFoundException, IOException {
	this.portNumber = portNum;
	this.feedletMode = feedletMode;
	this.eventTypeSpaces = eventTypeSpaces;
	this.processorAffinity = processorAffinity;
	init(tickFrequency);
	Thread serverThread = new JavaTracingServerThread(this);
	serverThread.start();
    }

    public Logger(int portNum, EventTypeSpaceVersion[] eventTypeSpaces,
	    long tickFrequency, int feedletMode) throws FileNotFoundException,
	    IOException {
	this(portNum, eventTypeSpaces, tickFrequency, feedletMode,
		DEFAULT_PROCESSOR_AFFINITY);
    }

    private void socketDebugMsg(String str) {
	if (DEBUG_SOCKET) {
	    System.err.println("Info: " + str);
	}
    }

    public void serverRun() {
	ServerSocket serverSocket;
	try {
	    serverSocket = new ServerSocket(portNumber);
	} catch (IOException e) {
	    socketDebugMsg("Unable to create ServerSocket on port "
		    + portNumber);
	    socketDebugMsg("\t" + e.getMessage());
	    socketDebugMsg("Server exiting");
	    return;
	}
	while (true) {
	    try {
		socketDebugMsg("Waiting for connections...");
		Socket clientSocket = serverSocket.accept();
		socketDebugMsg("Accepted connection to " + clientSocket);
		OutputStream os = clientSocket.getOutputStream();
		writeOldMetaChunks(os);
		outputStream = os;
	    } catch (IOException e) {
		socketDebugMsg("Problem establishing socket communation.");
		socketDebugMsg(e.getMessage());
		e.printStackTrace();
		socketDebugMsg("...restarting wait loop.");
	    }
	    while (outputStream != null) {
		try {
		    Thread.sleep(5 * INTER_FLUSH_TIME_MS);
		} catch (InterruptedException ie) {
		}
	    }
	}
    }

    private EventChunk makeNewEventChunk() {
	return new EventChunk((feedletMode == AUTO_CLOCK_MODE)
		&& (file == null));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#init()
     */
    public void init(long tickFrequency) throws IOException {

	OSBridge.initialize();

	FeedHeaderChunk feedHeaderChunk = new FeedHeaderChunk();
	oldMetaChunks.add(feedHeaderChunk);
	for (int i = 0; i < eventTypeSpaces.length; i++) {
	    EventTypeSpaceChunk eventTypeSpaceChunk = new EventTypeSpaceChunk(
		    eventTypeSpaces[i]);
	    oldMetaChunks.add(eventTypeSpaceChunk);
	}

	for (int i = 0; i < INITIAL_NUMBER_OF_EVENT_CHUNKS; i++) {
	    readyEventChunks.add(makeNewEventChunk());
	}
	Runtime.getRuntime().addShutdownHook(
		new JavaTracingShutdownThread(this));
	Thread loggingThread = new JavaTracingThread(this);
	loggingThread.start();
	addProperty("Tick Frequency", "" + tickFrequency);
	addProperty("Trace Thread CPU Affinity", processorAffinity >= 0 ? ""
		+ processorAffinity : "None");
	addJavaAndEnvironmentProperties();
    }

    public void emitterShutdown() {
	close();
    }

    public void emitterRun() {
	loggerMode = RUNNING;
	while (true) {
	    try {
		Thread.sleep(INTER_FLUSH_TIME_MS);
	    } catch (InterruptedException ie) {
	    }
	    boolean shuttingDown = loggerMode == SHUTTING_DOWN;
	    flush(shuttingDown);
	    if (shuttingDown) {
		loggerMode = SHUT_DOWN;
		break;
	    }
	}
    }

    public void close() {
	loggerMode = SHUTTING_DOWN;
	while (loggerMode == SHUTTING_DOWN) {
	    try {
		Thread.sleep(INTER_FLUSH_TIME_MS);
	    } catch (InterruptedException ie) {
	    }
	}
    }

    public int allowableNumberOfEventChunk() {
	if (feedletMode == CONVERSION_MODE) {
	    // In conversion mode, never lose data!
	    return Integer.MAX_VALUE;
	} else {
	    return INITIAL_NUMBER_OF_EVENT_CHUNKS
		    + ADDITIONAL_NUMBER_OF_EVENT_CHUNKS_PER_FEEDLET
		    * feedletCount;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#getEmptyEventChunk()
     */
    public EventChunk getEmptyEventChunk() {
	synchronized (eventChunkLock) {
	    ListIterator/* <EventChunk> */readyIter = readyEventChunks
		    .listIterator();
	    while (readyIter.hasNext()) {
		EventChunk ec = (EventChunk) readyIter.next();
		if (ec != null) {
		    readyEventChunks.remove(ec);
		    return ec;
		}
	    }
	}
	if (eventChunkCount < allowableNumberOfEventChunk()) {
	    eventChunkCount++;
	    EventChunk ec = makeNewEventChunk();
	    return ec;
	}
	// Throw away some old full event chunks
	EventChunk ec = getFullEventChunk();
	if (ec != null) {
	    ec.close();
	    return ec;
	}
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#returnFullEventChunk(com.ibm.tuningfork.tracegen.EventChunk)
     */
    public void returnFullEventChunk(EventChunk ec) {
	synchronized (eventChunkLock) {
	    fullEventChunks.add(ec);
	}
    }

    private void returnReadyEventChunk(EventChunk ec) {
	synchronized (eventChunkLock) {
	    readyEventChunks.add(ec);
	}
    }

    private EventChunk getFullEventChunk() {
	synchronized (eventChunkLock) {
	    ListIterator/* <EventChunk> */readyIter = fullEventChunks
		    .listIterator();
	    while (readyIter.hasNext()) {
		EventChunk ec = (EventChunk) readyIter.next();
		if (ec != null) {
		    fullEventChunks.remove(ec);
		    return ec;
		}
	    }
	}
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#getFeedlet(java.lang.String,
     *      java.lang.String)
     */
    public synchronized IFeedlet getFeedlet(String name, String desc) {
	Thread thread = Thread.currentThread();
	// FIXME: Thread.getId() is not 1.4 -- how to solve?
	long id = thread.getId();
	String key = Long.toString(id).intern();
	IFeedlet feedlet = (Feedlet) feedletsByThread.get(key);
	if (feedlet == null) {
	    feedlet = makeFeedlet(name, desc);
	    feedletsByThread.put(key, feedlet);
	}
	return feedlet;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#makeFeedlet(java.lang.String,
     *      java.lang.String)
     */
    public synchronized IFeedlet makeFeedlet(String name, String desc) {
	if (feedletMode == CONVERSION_MODE) {
	    throw new IllegalArgumentException(
		    "Can not make a non-conversion feedlet for a conversion logger");
	}
	return (IFeedlet) makeFeedletInternal(name, desc);
    }

    public synchronized IConversionFeedlet makeConversionFeedlet(String name,
	    String desc) {
	if (feedletMode != CONVERSION_MODE) {
	    throw new IllegalArgumentException(
		    "Can not make a conversion feedlet for a non-conversion logger");
	}
	return (IConversionFeedlet) makeFeedletInternal(name, desc);
    }

    private AbstractFeedlet makeFeedletInternal(String name, String desc) {
	AbstractFeedlet feedlet = null;
	switch (feedletMode) {
	case AUTO_CLOCK_MODE:
	    feedlet = new Feedlet(this, feedletId, name, desc);
	    break;
	case MANUAL_CLOCK_MODE:
	    feedlet = new Feedlet(this, feedletId, name, desc);
	    break;
	case CONVERSION_MODE:
	    feedlet = new ConversionFeedlet(this, feedletId, name, desc);
	    break;
	case NULL_MODE:
	    feedlet = new Feedlet(this, feedletId, name, desc);
	    break;
	}

	feedlets.add(feedlet);
	feedletChunk.add(feedlet.getIndex(), feedlet.getName(), feedlet.getDescription());
	feedletId++;
	feedletCount++;
	return feedlet;
    }

    public void addFeedletProperty(AbstractFeedlet f, String key, String value) {
	feedletChunk.addProperty(f.getIndex(), key, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#addEventType(com.ibm.tuningfork.tracegen.EventType)
     */
    public synchronized void addEventType(EventType et) {
	while (!eventTypeChunk.add(et)) {
	    flush();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#addString(java.lang.String)
     */
    public synchronized int addString(String val) {
	int index = stringIndex++;
	while (!stringTableChunk.add(index, val)) {
	    flush();
	}
	return index;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#addProperty(java.lang.String,
     *      java.lang.String)
     */
    public synchronized void addProperty(String prop, String val) {
	while (!propertyTableChunk.add(prop, val)) {
	    flush();
	}
    }

    private void writeEvents(OutputStream outputStream) throws IOException {
	while (true) {
	    EventChunk ec = getFullEventChunk();
	    if (ec == null) {
		return;
	    }
	    ec.close();
	    if (outputStream != null) {
		ec.write(outputStream);
	    }
	    // The event chunk is reset when a feedlet acquires it.
	    returnReadyEventChunk(ec);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#flushFeedlets()
     */
    public synchronized void flushFeedlets() {
	ListIterator/* <AbstractFeedlet> */feedletIterator = feedlets
		.listIterator();
	while (feedletIterator.hasNext()) {
	    ((AbstractFeedlet) feedletIterator.next()).flush();
	}
    }

    private synchronized void writeOldMetaChunks(OutputStream outputStream)
	    throws IOException {
	ListIterator chunkIterator = oldMetaChunks.listIterator();
	while (chunkIterator.hasNext()) {
	    RawChunk chunk = (RawChunk) chunkIterator.next();
	    chunk.write(outputStream);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.tuningfork.tracegen.ILogger#flush()
     */
    public synchronized void flush() {
	flush(false);
    }

    private synchronized void flush(boolean shutdown) {
	try {
	    OutputStream os = outputStream;
	    // Grab all the non-empty meta-chunks
	    if (propertyTableChunk.hasData()) {
		propertyTableChunk.close();
		closedMetaChunks.add(propertyTableChunk);
		propertyTableChunk = new PropertyTableChunk();
	    }
	    if (stringTableChunk.hasData()) {
		stringTableChunk.close();
		closedMetaChunks.add(stringTableChunk);
		stringTableChunk = new StringTableChunk();
	    }
	    if (eventTypeChunk.hasData()) {
		eventTypeChunk.close();
		closedMetaChunks.add(eventTypeChunk);
		eventTypeChunk = new EventTypeChunk();
	    }
	    if (feedletChunk.hasData()) {
		feedletChunk.close();
		closedMetaChunks.add(feedletChunk);
		feedletChunk = new FeedletChunk();
	    }
	    if (os != null) {
		ListIterator chunkIterator = closedMetaChunks.listIterator();
		while (chunkIterator.hasNext()) {
		    RawChunk chunk = (RawChunk) chunkIterator.next();
		    chunk.write(os);
		}
	    }
	    oldMetaChunks.addAll(closedMetaChunks);
	    closedMetaChunks.removeAll(oldMetaChunks);
	    if (shutdown) {
		flushFeedlets(); // Actively grab partial event chunks only
				    // on shutdown
	    }
	    writeEvents(os); // Note that this must come after the other
				// blocks (particularly feedlet and event type
	    if (shutdown) {
		if (outputStream != null) {
		    outputStream.close();
		    outputStream = null;
		}
	    }
	} catch (IOException e) {
	    if (outputStream != null && portNumber != 0) {
		outputStream = null;
		return;
	    } else {
		System.out.println("Exception in Logger.flush: " + e.getClass()
			+ " " + e.getMessage());
	    }
	} catch (Exception e) {
	    System.out.println("Exception in Logger.flush: " + e.getClass()
		    + " " + e.getMessage());
	}
    }

    public IBookmarkEvent makeBookmarkEvent(String name) {
	return new BookmarkEvent(this, name);
    }

    public ITimerEvent makeTimerEvent(String name) {
	return new TimerEvent(this, name);
    }

    public ITimerEvent makePerFeedletTimerEvent(String name) {
	return new TimerEvent(this, name, "Feedlet ");
    }

    public IValueEvent makeValueEvent(String name) {
	return new ValueEvent(this, name);
    }

    public void defineOscilloscopeFigure(String name, ITimerEvent[] events) {
	defineFigure("Oscilloscope", name, packageEvents(events));
    }

    public void defineHistogramFigure(String name, ITimerEvent[] events) {
	defineFigure("Histogram", name, packageEvents(events));
    }

    public void definePieChartFigure(String name, ITimerEvent[] events) {
	defineFigure("Pie Chart", name, packageEvents(events));
    }

    public void defineTimeSeriesFigure(String name, IValueEvent[] values,
	    ITimerEvent[] intervals) {
	String valueStreams = packageEvents(values);
	String intervalStreams = packageEvents(intervals);
	String separator = valueStreams.length() > 0
		&& intervalStreams.length() > 0 ? "," : "";
	String streams = valueStreams + separator + intervalStreams;

	defineFigure("Time Series", name, streams);
    }

    private void defineFigure(String kind, String name, String streams) {
	String property = "Figure " + kind + ":" + name;
	addProperty(property, streams);
	// System.out.println("Logger: defining figure " + property + " with
	// streams " + streams);
    }

    private String packageEvents(IEvent[] events) {
	if (events == null || events.length == 0) {
	    return "";
	}

	String streams = "";
	String separator = "";
	for (int i = 0; i < events.length; i++) {
	    streams += separator + events[i].getName();
	    separator = ",";
	}
	return streams;
    }

    public void addJavaAndEnvironmentProperties() {
	addProperties(System.getProperties(), "");
	addProperties(System.getenv(), "environment.");
    }

    public void addProperties(Map map, String prefix) {
	Iterator iterator = map.keySet().iterator();
	while (iterator.hasNext()) {
	    String key = (String) iterator.next();
	    String val = (String) map.get(key);
	    addProperty(prefix + key, val);
	}
    }

    public void addProperties(Properties properties, String prefix) {
	Iterator iter = properties.keySet().iterator();

	while (iter.hasNext()) {
	    String key = (String) iter.next();
	    String value = properties.getProperty((String) key);
	    addProperty(prefix + key, value);
	}
    }

    public int getProcessorAffinity() {
	return processorAffinity;
    }
}
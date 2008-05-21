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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import com.ibm.tuningfork.tracegen.impl.Feedlet;
import com.ibm.tuningfork.tracegen.impl.Logger;
import com.ibm.tuningfork.tracegen.types.EventTypeSpaceVersion;

/**
 * This class is responsible for creating ILogger instances to generate trace
 * files.
 *
 */
public final class LoggerFactory {

    private static final EventTypeSpaceVersion DEFAULT_EVENT_TYPE_SPACE = new EventTypeSpaceVersion(
	    "com.ibm.tuningfork.generic", 1);

    public static final int NO_LOGGING = 0, LOG_TO_FILE = 1, LOG_TO_SOCKET = 2; // Was
										// 1.5:
										// enum
										// LoggerKind

    /**
     * Create an ILogger whose output is a file.
     *
     * @param file
     *                The file to which the trace is written.
     * @return The ILogger instance.
     */
    public static ILogger makeFileLogger(File file) throws IOException {
	return makeFileLogger(file, new EventTypeSpaceVersion[0], true);
    }

    /**
     * Create an ILogger whose output is a file.
     *
     * @param fileName
     *                The name of the file to which the trace is
     * @return The ILogger instance.
     */
    public static ILogger makeFileLogger(String fileName) throws IOException {
	return makeFileLogger(new File(fileName));
    }

    /**
     * Create a logger which is live on a socket to which TuningFork can attach.
     *
     * @param portNumber
     *                The port to listen on.
     * @return The ILogger instance.
     */
    public static ILogger makeServerLogger(int portNumber) throws IOException {
	return makeServerLogger(portNumber, new EventTypeSpaceVersion[0], true);
    }

    private static EventTypeSpaceVersion[] augment(
	    EventTypeSpaceVersion[] eventTypeSpaces) {
	EventTypeSpaceVersion[] result = new EventTypeSpaceVersion[eventTypeSpaces.length + 1];
	System.arraycopy(eventTypeSpaces, 0, result, 0, eventTypeSpaces.length);
	result[eventTypeSpaces.length] = DEFAULT_EVENT_TYPE_SPACE;
	return result;
    }

    /**
     * Create an ILogger whose output is a file.
     *
     * @param file The file to use.
     * @param eventTypeSpaces The event type spaces.
     * @return The ILogger instance.
     */
    public static ILogger makeFileLogger(File file,
	    EventTypeSpaceVersion[] eventTypeSpaces, boolean isAuto)
	    throws IOException {
	Logger logger = new Logger(file, augment(eventTypeSpaces),
		Feedlet.TICK_FREQUENCY, isAuto ? Logger.AUTO_CLOCK_MODE
			: Logger.MANUAL_CLOCK_MODE);
	addDefaultProperties(logger);
	return logger;
    }

    /**
     * Create a logger which is live on a socket to which TuningFork can attach.
     *
     * @param portNumber The port to listen on.
     * @param eventTypeSpaces The event type space.
     * @return The ILogger instance.
     */
    public static ILogger makeServerLogger(int portNumber,
	    EventTypeSpaceVersion[] eventTypeSpaces, boolean isAuto)
	    throws IOException {
	Logger logger = new Logger(portNumber, augment(eventTypeSpaces),
		Feedlet.TICK_FREQUENCY, isAuto ? Logger.AUTO_CLOCK_MODE
			: Logger.MANUAL_CLOCK_MODE);
	addDefaultProperties(logger);
	return logger;
    }

    /**
     * Create a null logger that simply swallows events to avoid
     * conditionalising calls to ILogger.
     *
     * @return The ILogger instance.
     */
    public static ILogger makeNullLogger() {
	return new Logger();
    }

    /**
     * Create a logger of whatever kind is desired, handling the potential
     * exceptions.
     *
     * @return The ILogger instance.
     */
    public static ILogger makeLogger(int kind, String fileName, int portNumber) {
	ILogger logger = null;

	if (kind == LOG_TO_SOCKET) {
	    try {
		logger = LoggerFactory.makeServerLogger(portNumber);
	    } catch (Exception e) {
		throw new RuntimeException(
			"Could not create socket for trace log: " + e);
	    }
	} else if (kind == LOG_TO_FILE) {
	    try {
		logger = LoggerFactory.makeFileLogger(new File(fileName));
	    } catch (Exception e) {
		throw new RuntimeException(
			"Could not create file for trace log: " + e);
	    }
	}

	if (logger == null) {
	    logger = LoggerFactory.makeNullLogger();
	}

	return logger;
    }

    /**
     * Create a logger to be used for converting other traces to TuningFork
     * format
     *
     * @param port
     *                The port number over which the trace will be sent.
     * @return The ILogger instance.
     */
    public static IConversionLogger makeConversionServerLogger(int port,
	    EventTypeSpaceVersion[] eventTypeSpaces, long tickFrequency)
	    throws IOException {
	return new Logger(port, eventTypeSpaces, tickFrequency,
		Logger.CONVERSION_MODE);
    }

    /**
     * Create a logger to be used for converting other traces to TuningFork
     * format
     *
     * @param filename
     *                The name of the file to which the trace will be written.
     * @return The ILogger instance.
     */
    public static IConversionLogger makeConversionLogger(String filename,
	    EventTypeSpaceVersion[] eventTypeSpaces, long tickFrequency)
	    throws IOException {
	return new Logger(new File(filename), eventTypeSpaces, tickFrequency,
		Logger.CONVERSION_MODE);
    }

    /**
     * Create a logger to be used for converting other traces to TuningFork
     * format
     *
     * @param file The file to which the trace will be written.
     * @param eventTypeSpaces the event type spaces
     * @param tickFrequency number of ticks per second.
     * @return The ILogger instance.
     */
    public static IConversionLogger makeConversionLogger(File file,
	    EventTypeSpaceVersion[] eventTypeSpaces, long tickFrequency)
	    throws IOException {
	return new Logger(file, eventTypeSpaces, tickFrequency,
		Logger.CONVERSION_MODE);
    }

    private static void addDefaultProperties(ILogger logger) {
	logger.addProperty("Trace Created", new Date().toString());
	try {
	    String hostname = InetAddress.getLocalHost().getHostName();
	    logger.addProperty("Hostname", hostname);
	} catch (UnknownHostException uhe) {
	}
	/*
	 * Unfortunately, the command line arguments to the Java application is
	 * not available here Properties props = System.getProperties();
	 * Enumeration<?> e = props.propertyNames(); while
	 * (e.hasMoreElements()) { String key = (String) e.nextElement();
	 * System.out.println(key + " " + props.getProperty(key)); }
	 */
    }

    public static String makeDateForTemporaryFilename() {
	Date date = new Date(System.currentTimeMillis());
	String dateStr = date.toString().replace(' ', '_').replace(':', '_');
	return dateStr;
    }
}
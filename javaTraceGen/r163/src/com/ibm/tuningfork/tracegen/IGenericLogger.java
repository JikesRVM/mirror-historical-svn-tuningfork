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

import com.ibm.tuningfork.tracegen.types.EventType;


/**
 * A logger is responsible for the generation of a complete trace, composed of
 * several feedlets. Once a Logger has been created, the programmer can proceed
 * to create feedlets.
 * 
 * The IGenericLogger interface is not meant to be directly implemented, it
 * mainly serves to express functionality common to both ILogger and
 * IConversionLogger.
 */
public interface IGenericLogger {
    /**
     * Create a timer event and register it with the logger.
     * 
     * @param name
     *                The name of the timer event.
     * @return The new ITimerEvent instance.
     */
    public ITimerEvent makeTimerEvent(String name);

    /**
     * Create a bookmark event and register it with the logger.
     * 
     * @param name
     *                The name of the bookmark event.
     * @return The new IBookmarkEvent instance.
     */
    public IBookmarkEvent makeBookmarkEvent(String name);

    /*
     * Create a timer event which can occur in multiple feedlets at the same
     * time and register it with the logger.
     * 
     * FIXME: turned off for 1.0.3 release because still not entirely stable
     * 
     * @param name The name of the timer event. @return The new ITimerEvent
     * instance.
     */
    // public ITimerEvent makePerFeedletTimerEvent(String name);
    /**
     * Create a value event and register it with the logger.
     * 
     * @param name
     *                The name of the value event.
     * @return The new IValueEvent instance.
     */
    public IValueEvent makeValueEvent(String name);

    public void addEventType(EventType et);

    /**
     * Add a string to the string table.
     * 
     * @param value
     *                The string to add.
     */
    public int addString(String value);

    /**
     * Add a property to the trace file.
     * 
     * @param propertyName
     *                The name of the property.
     * @param value
     *                The property value.
     */
    public void addProperty(String propertyName, String value);

    /**
     * An Oscilloscope figure is used for viewing Timer events in TuningFork.
     * This method allows you to define a group of Timer events that will show
     * up in the "Predefined Figures" portion of the TuningFork feed explorer.
     * 
     * @param name
     *                The name for the figure.
     * @param events
     *                An array of Timer events which will comprise the figure.
     */
    public void defineOscilloscopeFigure(String name, ITimerEvent[] events);

    /**
     * A Pie Chart figure is used for viewing durations of Timer events in
     * TuningFork. This method allows you to define a group of Timer events that
     * can be opened together as a Histogram from the "Predefined Figures"
     * portion of the TuningFork feed explorer.
     * 
     * @param name
     *                The name for the figure.
     * @param events
     *                An array of Timer events which will comprise the figure.
     */
    public void definePieChartFigure(String name, ITimerEvent[] events);

    /**
     * A Histogram figure is used for viewing durations of Timer events in
     * TuningFork. This method allows you to define a group of Timer events that
     * can be opened together as a Histogram from the "Predefined Figures"
     * portion of the TuningFork feed explorer.
     * 
     * @param name
     *                The name for the figure.
     * @param events
     *                An array of Timer events which will comprise the figure.
     */
    public void defineHistogramFigure(String name, ITimerEvent[] events);

    /**
     * A TimeSeries figure is used for viewing value events in TuningFork.
     * Intervals can also be superimposed. This method allows you to define a
     * group of Value and Timer events that will show up in the "Predefined
     * Figures" portion of the TuningFork feed explorer.
     * 
     * @param name
     *                The name for the figure.
     * @param values
     *                An array of Value events which will be shown in the
     *                figure.
     * @param intervals
     *                An array of Timer events which will be shown in the
     *                figure.
     */
    public void defineTimeSeriesFigure(String name, IValueEvent[] values,
	    ITimerEvent[] intervals);

    /**
     * This method can be called on shutdown to ensure that data is flushed to
     * the file or socket. Normally, the JVM will automatically activate this on
     * regular shutdown because the library installs a shutdown hook.
     */
    public void close();
}

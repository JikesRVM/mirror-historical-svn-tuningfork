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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Not currently used. But, this code might be useful on some other platform, so
 * not deleting it yet.
 */
public class PidGrabberViaShell {

    public static final int NO_PID = -1;

    public static final String KSH_PATH_ENV_VAR = "JAVA_KSH_PATH";

    // Arg! This should work!
    // public static final String DEFAULT_PATH = "/bin";
    // public static final String COMMAND = "ksh -c \"echo $PPID\"";
    // public static final String COMMAND = "ksh -c 'echo 17'";

    public static final String DEFAULT_PATH = "/Users/dfb/bin";
    public static final String COMMAND = "getpidforjava";

    public static int getPid() {
	return getPid(getPathForScript(), COMMAND);
    }

    public static int getPid(String path) {
	return getPid(path, COMMAND);
    }

    public static int getPid(String path, String command) {
	try {
	    String fullCommand = path.length() == 0 ? command : path + "/"
		    + command;
	    Runtime runtime = Runtime.getRuntime();
	    Process process = runtime.exec(fullCommand);
	    BufferedReader stdout = new BufferedReader(new InputStreamReader(
		    process.getInputStream()));
	    String line = stdout.readLine();
	    int pid = Integer.parseInt(line);
	    int exitVal = process.waitFor();

	    if (exitVal == 0) {
		return pid;
	    } else {
		return NO_PID;
	    }
	} catch (IOException io) {
	    // System.err.println("I/O Exception " + io);
	    return NO_PID;
	} catch (InterruptedException intr) {
	    // System.err.println("Interrupted Exception " + intr);
	    return NO_PID;
	} catch (NumberFormatException nfe) {
	    return NO_PID;
	}
    }

    private static String getPathForScript() {
	String pidScriptPath = System.getenv(KSH_PATH_ENV_VAR);
	return pidScriptPath == null ? DEFAULT_PATH : pidScriptPath;
    }

}

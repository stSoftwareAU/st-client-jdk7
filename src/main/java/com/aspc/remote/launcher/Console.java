/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */
package com.aspc.remote.launcher;

import com.aspc.remote.application.DocFrame;
import com.aspc.remote.application.HelpUtil;
import com.aspc.remote.tail.TailFrame;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.HeadlessException;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.Target;

/**
 * A copy of JConsole.java. Supports StServer menu item in the menu bar.
 * THREAD MODE: SINGLE THREAD
 * @author padma
 * @since 27 Jun 2009
 */
@SuppressWarnings("serial")
public class Console extends DocFrame
{
    private static File currentDir;
    private static String baseDir = "";

    /**
     * new console
     */
    public Console()
    {
        super();
    }

    /**
     * create the main window
     * @return the window
     * @throws Exception a serious problem
     */
    public static Console mainWidnow() throws Exception
    {

       throw new Exception( "java 1.8 needed");
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.launcher.Console");//#LOGGER-NOPMD

}

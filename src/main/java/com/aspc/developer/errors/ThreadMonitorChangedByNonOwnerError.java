/*
 *  Copyright (c) 2001-2004 ASP Converters Pty Ltd.
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
*/
package com.aspc.developer.errors;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;

/**
 *  A object that is being monitored was attempted to be changed by a thread that didn't create the object
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       9 November 2009
 */
public class ThreadMonitorChangedByNonOwnerError extends ThreadCopError
{
    /**
     * Only the owner thread can change the monitor mode.
     * @param msg the message.
     */
    public ThreadMonitorChangedByNonOwnerError( final String msg)
    {
        super( msg);
    }
   
    private static final Log LOGGER = CLogger.getLog( "com.aspc.developer.errors.ThreadMonitorChangedByNonOwnerError");//#LOGGER-NOPMD
}

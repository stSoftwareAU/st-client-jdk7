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

import com.aspc.developer.ThreadCop;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;

/**
 *  Thread Cop Error
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       9 November 2009
 */
public class ThreadCopError extends Error //NOPMD
{
    /**
     * Thread cop error
     * @param msg the message
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ThreadCopError( final String msg)
    {
        super( msg);
        ThreadCop.incrementErrorCount();
        ThreadCop.recordLastError(this);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.developer.errors.ThreadCopError");//#LOGGER-NOPMD
}

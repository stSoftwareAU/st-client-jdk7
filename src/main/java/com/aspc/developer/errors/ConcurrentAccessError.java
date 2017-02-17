/*
 *  Copyright (c) 2001-2004 ASP Converters Pty Ltd.
 *
 *  www.aspconverters.com.au
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
 * Single Thread Object Accessed By Non Creating Thread Error.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       9 November 2009
 */
public class ConcurrentAccessError extends ThreadCopError
{
    /**
     * Concurrent access error
     * @param msg the message
     */
    public ConcurrentAccessError( final String msg)
    {
        super( msg);
    }
   
    private static final Log LOGGER = CLogger.getLog( "com.aspc.developer.errors.ConcurrentAccessError");//#LOGGER-NOPMD
}

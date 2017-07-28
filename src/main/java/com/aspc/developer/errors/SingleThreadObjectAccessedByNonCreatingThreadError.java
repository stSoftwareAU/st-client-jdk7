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
 * Single Thread Object Accessed By Non Creating Thread Error.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       9 November 2009
 */
public class SingleThreadObjectAccessedByNonCreatingThreadError extends ThreadCopError
{
    /**
     * Accessed by thread that didn't create the object
     * @param msg the message
     */
    public SingleThreadObjectAccessedByNonCreatingThreadError( final String msg)
    {
        super( msg);
    }
   
    private static final Log LOGGER = CLogger.getLog( "com.aspc.developer.errors.SingleThreadObjectAccessedByNonCreatingThreadError");//#LOGGER-NOPMD
}

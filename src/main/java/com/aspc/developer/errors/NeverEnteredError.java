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
 * never entered.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       9 November 2009
 */
public class NeverEnteredError extends ThreadCopError
{
    /**
     * never entere
     * @param msg the message
     */
    public NeverEnteredError( final String msg)
    {
        super( msg);
    }
   
    private static final Log LOGGER = CLogger.getLog( "com.aspc.developer.errors.NeverEnteredError");//#LOGGER-NOPMD
}

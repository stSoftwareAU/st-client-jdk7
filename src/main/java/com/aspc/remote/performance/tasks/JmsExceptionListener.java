/*
 *  Copyright (c) 2000-2006 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance.tasks;

import javax.jms.*;

import com.aspc.remote.util.misc.CLogger;
import org.apache.commons.logging.Log;

/**
 * implementation of exception listener
 * 
 * <I>THREAD MODE : SINGLE-THREADED</I>
 * 
 * @author sr83034
 * @since 29 September 2006
 */
public class JmsExceptionListener implements ExceptionListener
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.tasks.JmsExceptionListener");//#LOGGER-NOPMD

    /**
     * Listen for exceptions
     */
    public JmsExceptionListener()
    {
        super();
    }

    /**
     * detects an exception
     * @param je 
     */
    @Override
    public void onException( final JMSException je )
    {
        LOGGER.error( "Received Exception - ");
        LOGGER.error( "Error Code : " + je.getErrorCode() );
        LOGGER.error( "Error Message : " + je.getMessage() );
    }
}

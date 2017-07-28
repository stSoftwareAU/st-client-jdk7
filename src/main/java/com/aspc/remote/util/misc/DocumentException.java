/*
 *  Copyright (c) 2003-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.util.misc;

/**
 *  DOcument Exception
 *
 *  <br>
 *  <i>THREAD MODE: NON-MUTABLE</i>
 *
 *  @author      Liam Itzhaki
 *  @since       3 June 2014
 */
public class DocumentException  extends Exception
{
     /**
     * constructor
     */
    public DocumentException()
    {
        super();
    }
    
    /**
     * constructor
     * @param message the message
     */
    public DocumentException(String message)
    {
        super(message);
    }
    
    /**
     * constructor
     * @param cause the root cause
     */
    public DocumentException(Throwable cause)
    {
        super(cause);
    }
    
    /**
     * constructor
     * @param message the message
     * @param cause the root cause
     */
    public DocumentException(String message, Throwable cause)
    {
        super(message, cause);
    }    
}

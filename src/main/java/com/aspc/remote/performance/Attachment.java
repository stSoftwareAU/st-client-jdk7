/*
 *  Copyright (c) 2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance;

import com.aspc.remote.soap.Client;

/**
 *  App
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       7 April 2001
 */
public abstract class Attachment
{
    /**
     * 
     * @param name 
     */
    public Attachment(String name)
    {
        this.name = name;
    }

    /**
     * 
     * @return the value
     */
    public String getName()
    {
        return name;
    }

    /**
     * 
     * @param data 
     */
    public void setData( String data)
    {
        this.data = data;
    }

    /**
     * 
     * @throws Exception a serious problem
     * @return the value
     */
    public abstract String getContent( ) throws Exception;
    /**
     * 
     * @param client the client to use
     * @throws Exception a serious problem
     */
    protected abstract void process( Client client) throws Exception;

    /** the name */
    protected String  name;
    /** the data */
    protected String data;
}

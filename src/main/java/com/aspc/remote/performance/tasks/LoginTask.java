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
package com.aspc.remote.performance.tasks;

import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.performance.Task;

import com.aspc.remote.soap.Client;
import com.aspc.remote.util.misc.*;
import java.util.StringTokenizer;

/**
 *  App
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       7 April 2001
 */
public class LoginTask extends Task
{
    /**
     * 
     * @param name 
     * @param bmClient 
     */
    public LoginTask(String name, BenchMarkClient bmClient)
    {
        super( name, bmClient);
    }

    /**
     * 
     * @throws Exception a serious problem
     */
    @Override
    protected void process() throws Exception
    {
        String  user = null,
                passwd = null;

        boolean logout = false;

        String data = getData();

        StringTokenizer st = new StringTokenizer( data.trim(), ",");

        while( st.hasMoreTokens())
        {
            String temp = st.nextToken().trim();

            StringTokenizer st2=new StringTokenizer( temp, "=");

            String name = st2.nextToken().trim();

            if( st2.hasMoreTokens())
            {
                String value = st2.nextToken().trim();

                if( name.equalsIgnoreCase( "USER"))
                {
                    user = value;
                }
                else if( name.equalsIgnoreCase( "PASSWD"))
                {
                    passwd = value;
                }
                else if( name.equalsIgnoreCase( "LOGOUT"))
                {
                    if( value.equalsIgnoreCase( "Y"))
                    {
                        logout =true;
                    }
                }
            }
        }

        if(
            StringUtilities.isBlank( user) ||
            StringUtilities.isBlank( passwd)
        )
        {
            throw new Exception( "USER and PASSWD are mandatory");
        }

        Client client = bmClient.getRemoteClient();

        String host = client.getHost();
        String layer = client.getLayer();

        Client tClient = new Client( host);

        tClient.login( user, passwd, layer);

        if( logout)
        {
            tClient.logout();
        }
    }
}

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

import com.aspc.remote.util.misc.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *  Vary list
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       7 April 2001
 */
public final class VaryList
{
    private VaryList( final String data)
    {
        values = new Vector();//NOPMD

        StringTokenizer st = new StringTokenizer(data, "\r\n");

        while( st.hasMoreElements())
        {
            String value = st.nextToken().trim();

            if( StringUtilities.isBlank( value))
            {
                continue;
            }

            values.add( value);
        }
    }

    
    private synchronized String rotateValue()
    {
        if( values.isEmpty())
        {
            return "";
        }

        if( nextPos >= values.size()) nextPos=0;

        String value = (String)values.get( nextPos);

        nextPos++;

        return value;
    }

    private synchronized String rotateValue(int index)//NOPMD
    {
        if( values.isEmpty())
        {
            return "";
        }

        index = index % values.size();
        String value = (String)values.get( index);
        return value;
    }

    /**
     * 
     * @param name 
     * @param index 
     * @return the value
     */
    public static String nextValue( String name, int index)
    {
        VaryList vl = (VaryList)table.get( name);

        if( vl == null)
        {
            return "";
        }
        return vl.rotateValue(index);
    }

    /**
     * 
     * @param name 
     * @return the value
     */
    public static String nextValue( String name)
    {
        VaryList vl = (VaryList)table.get( name);

        if( vl == null)
        {
            return "";
        }

        return vl.rotateValue();
    }

    /**
     *
     * @param name
     * @return the value
     */
    public static int length(String name)
    {
         VaryList vl = (VaryList)table.get( name);
         return vl.size();
    }
    /**
     *
     * @return the value
     */
    public int size()
     {
         return values.size();
     }
    /**
     * 
     * @param name 
     * @param data 
     */
    public static void create( String name, String data)
    {
        table.put( name, new VaryList( data));
    }

    //private String name;
    private int nextPos;
    private Vector values;//NOPMD
    private static final Hashtable table = new Hashtable();
}

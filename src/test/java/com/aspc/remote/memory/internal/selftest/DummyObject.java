/*
 *  Copyright (c) 2007 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.internal.selftest;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  Formula
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 *  @author         Nigel Leck
 *  
 *  @since          January 19, 2001
 */
public class DummyObject
{
    /**
     * the value
     */
    public final int value;
    /**
     * The hash code value
     */
    public final int hashValue;

    /**
     * 
     * @param hashValue the hash value
     * @param value the value
     */
    public DummyObject( final int hashValue, final int value)
    {
        this.value = value;
        this.hashValue = hashValue;
    }

    /**
     * 
     * @param obj the object to check
     * @return true if equal
     */
    @Override @CheckReturnValue
    public boolean equals( final Object obj)
    {
        if( obj instanceof DummyObject)
        {
            if( this.value == ((DummyObject)obj).value)
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 
     * @return the hash code
     */
    @Override @CheckReturnValue
    public int hashCode()
    {
        return hashValue;
    }

    /**
     * 
     * @return the string
     */
    @Override @CheckReturnValue @Nonnull
    public String toString( )
    {
        return "dummy(" + hashValue + "," + value + ")";
    }
}

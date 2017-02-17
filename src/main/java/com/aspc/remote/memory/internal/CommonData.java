/*
 *  Copyright (c) 2001-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.internal;

import org.apache.commons.logging.Log;
import com.aspc.remote.database.*;

import com.aspc.remote.util.misc.*;

/**
 *  CommonData saves memory by reusing common data.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       April 21, 2001, 10:58 AM
 */
public final class CommonData
{
    /**
     * We found the row but the cell was blank.
     */
    public static final NullValueException COMMON_NV = new NullValueException( "This is a dummy NullValueException");

    /**
     * we searched but couldn't find the row.
     */
    public static final NotFoundException COMMON_NF = new NotFoundException( "This is a dummy NotFoundException");

    /**
     * The record is deleted.
     */
    public static final DeletedRecordException COMMON_DEL = new DeletedRecordException( "This is a dummy delete");

    /** dump */
    public static void dump()
    {
        STRINGS.dump();
        INTEGERS.dump();
    }

    /**
     * Recycle the object.
     *
     * @param orginal object
     * @return the recycled value.
     */
    @SuppressWarnings("empty-statement")
    public static Object recycle( final Object orginal)
    {
        if( orginal == null) return null;

        Object data = orginal;

        if( data instanceof String)
        {
            data = STRINGS.intern( data);
        }
        else if( data instanceof Integer)
        {
            data = INTEGERS.intern( data);
        }
        else
        {
            if( data instanceof DoNotRecycle)
            {
                ;
            }
            else if( data instanceof NotFoundException)
            {
                if( LOGGER.isDebugEnabled()==false)
                {
                    data = COMMON_NF;
                }
            }
            else if( data instanceof NullValueException)
            {
                data = COMMON_NV;
            }
            else if( data instanceof Boolean)
            {
                if( ((Boolean)data))
                {
                    return Boolean.TRUE;
                }
                else
                {
                    return Boolean.FALSE;
                }
            }
        }

        return data;
    }

    // Privates
    private CommonData()
    {
    }

    private static final CommonTable STRINGS = new CommonTable( "Common Strings");
    private static final CommonTable INTEGERS = new CommonTable( "Common Integers");

    static
    {
        COMMON_NF.setStackTrace( new StackTraceElement[0]);
        COMMON_NV.setStackTrace( new StackTraceElement[0]);
        COMMON_DEL.setStackTrace(new StackTraceElement[0]);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.CommonData");//#LOGGER-NOPMD
}

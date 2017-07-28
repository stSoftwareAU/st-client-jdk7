/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */
package com.aspc.remote.database.internal;

import static com.aspc.remote.database.internal.SResultSet.ALLOW_OVERFLOW;
import com.aspc.remote.jdbc.SoapSQLException;
import org.apache.commons.logging.Log;
import java.util.*;
import java.sql.*;
import java.text.*;
import com.aspc.remote.util.misc.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  Row
 *
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED sql</i>
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class Row
{
    /**
     *
     */
    protected final TimeZone tz;
    
    /**
     * NULL String
     */
    protected static final String NULL_STR=new String( ); // We need a new object

    /**
     *
     * @param tz
     */
    protected Row(final TimeZone tz)
    {
        this.tz = tz;
    }
    
    /**
     *
     * @param tz
     * @param columns
     * @param data
     */
    @SuppressWarnings("empty-statement")
    public Row( final TimeZone tz, final ArrayList columns, final String data)
    {
        this( tz);
        String temp = data;
        int i,
            colCount,
            type;

        Column column = null;
        Object theObject;
        colCount     = columns.size();
        cells        = new Object[ colCount];

        if( temp.startsWith( "\t"))
        {
            temp = "$NULL$" + temp;
        }

        while( temp.contains("\t\t"))
        {
            temp  = temp.replace( "\t\t", "\t$NULL$\t");
        }
        StringTokenizer cd = new StringTokenizer( temp, "\t");

        i = 1;

        try
        {
            for( ; i <= colCount; i++)
            {
                column = (Column)columns.get(i - 1);
                type = column.getType();

                String od;
                if( cd.hasMoreTokens())
                {
                    od = cd.nextToken();
                }
                else
                {
                    od = "$NULL$";
                }
                theObject = null;
                if( od.equals( "$NULL$") == false)
                {
                    od = StringUtilities.decode(od);
                    theObject = od;

                    switch (type) {
                        case Types.DATE:
                        case Types.TIMESTAMP:
                            final SimpleDateFormat dfs[] = {
                                new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss"),//NOPMD
                                new SimpleDateFormat( "yyyy-MM-dd"),//NOPMD
                                new SimpleDateFormat( "dd-MMM-yyyy hh:mm:ss"),//NOPMD
                                new SimpleDateFormat( "dd-MMM-yyyy"),//NOPMD
                            };  
                            
                            for (SimpleDateFormat df : dfs) {
                                try {
                                    int pos;
                                    pos = od.indexOf('.');
                                    if( pos != -1)
                                    {
                                        od = od.substring(0, pos);
                                    }
                                    if (type == Types.DATE) {
                                        theObject = new SDate(df.parse(od));
                                    } else if (type == Types.TIMESTAMP) {
                                        theObject = new Timestamp(df.parse(od).getTime());
                                    }
                                }
                                catch( Exception e)
                                {
                                    ;//don't care
                                }
                            }   
                            break;
                        case Types.INTEGER:
                            theObject = Integer.valueOf( od);
                            break;
                        case Types.FLOAT:
                            theObject = new Double( od);
                            break;
                        default:
                            break;
                    }
                }

                cells[i-1] = theObject;
            }
        }
        catch( Exception e)
        {
            String t = "Error - Parsing Column (" + i + ")\n";
            if( column !=null)
            {
                t+="\nType = " + column.getType() +
                "\nName = " + column.getName();
            }
            LOGGER.error(t, e);
        }
    }

    /**
     *
     * @param tz
     * @param columns
     * @param inSet
     */
    public Row( final TimeZone tz, final ArrayList columns, final ResultSet inSet)
    {
        this( tz);
        
        int i = 1,
            colCount;

        colCount     = columns.size();
        cells        = new Object[ colCount];

        try
        {
            for( ; i <= colCount; i++)
            {
                Object obj;

                obj = inSet.getObject(i);
                if( obj instanceof Clob)
                {
                    Clob clob =(Clob)obj;
                    obj=clob.getSubString(1, (int)clob.length());
                    //clob.free(); Oracle doesn't support as of 20 Aug 2010.
                }

                cells[i -1] = obj;
            }
        }
        catch( Exception e)
        {
            Column column = (Column)columns.get(i - 1);
            String t = "Error - get column (" + colCount + ")\n" +
                "\nType = " + column.getType() +
                "\nName = " + column.getName();

            LOGGER.error(t,e);
            throw new RuntimeException( t, e);
        }
    }

    /**
     *
     * @param column
     * @return the value
     */
    public final Object getObject( Column column)
    {
        return cells[ column.getNumber()];
    }
         
    /**
     *
     * @param column
     * @return the value
     */
    public final String getString( Column column)
    {
        Object object;

        object = cells[ column.getNumber()];

        if( object == null) 
        {            
            return "";
        }

        String result;
        if( object instanceof Number)
        {
            result = object.toString();
        }
        else if( object instanceof java.util.Date)
        {
            if(column.getType() == Types.DATE)
            {
                result = TimeUtil.getStdFormat((java.util.Date) object, DateUtil.GMT_ZONE);
            }
            else
            {
                result = TimeUtil.getStdFormat((java.util.Date) object, tz);
            }
            int trunc;
            trunc = result.indexOf( "00:00:00");
            if( trunc != -1)
            {
                result = result.substring(0, trunc - 1);
            }
        }
        else
        {
            result = object.toString();
        }

        return result;
    }

    /**
     * is this column null
     * @param column the column
     * @return true if null
     */
    public final boolean isNull( Column column)
    {
        Object object;

        object = cells[ column.getNumber()];

        if( object == null || object == NULL_STR) return true;

        return false;
    }

    /**
     *
     * @param column
     * @return the value
     * @throws com.aspc.remote.jdbc.SoapSQLException
     */
    @CheckReturnValue
    public final int getInt( final Column column) throws SoapSQLException
    {
        long value=getLong( column);
        
        if( value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
        {
            if( ALLOW_OVERFLOW.get())
            {
                if( value > Integer.MAX_VALUE)
                {
                    return Integer.MAX_VALUE;
                }
                else
                {
                    return Integer.MIN_VALUE;
                }
            }
            throw new SoapSQLException("Out of Integer range " + value);
        }
        
        return (int)value; 
    }
    
    @CheckReturnValue
    public final float getFloat( final @Nonnull Column column) throws SQLException
    {
        double d= getDouble(column);
        
        if( d< Float.MIN_VALUE)
        {
            if( d< (double)Float.MIN_VALUE-1)
            {
                throw new SQLException( "below minimum float range " + d);
            }
            return Float.MIN_VALUE;
        }
        else if( d > Float.MAX_VALUE )
        {
            if( d> Double.parseDouble("" + Float.MAX_VALUE)+1)
            {
                throw new SQLException( "above maximum float range " + d);
            }
            
            return Float.MAX_VALUE;
        }
        
        return (float)d;
    }
    
    /**
     *
     * @param column
     * @return the value
     */
    @SuppressWarnings("empty-statement")
    @CheckReturnValue
    public final long getLong( Column column)
    {
        Object        object;

        object        = cells[ column.getNumber()];

        if( object instanceof Number)
        {
             return ((Number)object).longValue();
        }
        
        if( object instanceof String)
        {
            try
            {
                return (long)Double.parseDouble( (String)object);
            }
            catch( NumberFormatException nf)
            {
                // ignore
            }
        }
        
        return 0;
    }

    /**
     *
     * @param column
     * @return the value
     */
    @SuppressWarnings("empty-statement")
    public final double getDouble( Column column)
    {
        Object        object;
        object        = cells[ column.getNumber()];

        if( object instanceof Number)
        {
             return ((Number)object).doubleValue();
        }
        
        if( object instanceof String)
        {
            try
            {
                return Double.parseDouble( (String)object);
            }
            catch( NumberFormatException nf)
            {
                ;// ignore
            }
        }
        
        return 0.0;
    }

    /**
     *
     * @param column
     * @return the value
     */
    public java.sql.Date getDate( Column column)
    {
        Object        object;
        object        = cells[ column.getNumber()];

        if( object == null) return null;

        int             type;

        type            = column.getType();
        if(type == Types.DATE)
        {
            return new TDate((java.util.Date)object);
        }
        else if( type == Types.TIMESTAMP)
        {
            if( object instanceof java.util.Date)
            {
                return new java.sql.Date( ((java.util.Date)object).getTime());
            }
            
        }
        //else
        //{
            return null;
        //}
    }

    /**
     *
     */
    protected Object[] cells;
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.Row");//#LOGGER-NOPMD
}

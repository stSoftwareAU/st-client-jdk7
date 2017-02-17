/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
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
package com.aspc.remote.application.table;
import com.aspc.remote.util.misc.*;
import java.util.*;

/**
 *  TableSortCompare
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       June 18, 1998, 1:42 PM
 */
public class TableSortCompare implements Comparator
{
    int order;
    /**
     *
     */
    public static final int ASCENDING = 1;
    /**
     *
     */
    public static final int DESCENDING = 2;

    /**
     *
     * @param order
     */
    public TableSortCompare( int order)
    {
        this.order = order;
    }
    /**
    * compare
    * returns 0 if equal < 0 if o1 < o2 and > 0 if o1 > o2
    */
    @Override
    public int compare ( Object o1, Object o2)//NOPMD
    {
        TableSortData sd1,
                 sd2;

        sd1 = (TableSortData)o1;
        sd2 = (TableSortData)o2;
        o1 = sd1.o;
        o2 = sd2.o;

        if( o1 instanceof Cell)
        {
            o1 = ((Cell)o1).getData();
        }
        if( o2 instanceof Cell)
        {
            o2 = ((Cell)o2).getData();
        }

        int returnValue = 0;

        if( o1 == null && o2 == null)
        {
            o1 = sd1.row;
            o2 = sd2.row;
        }
        else if( o1 == null)
        {
            returnValue = -1;
        }
        else if( o2 == null)
        {
            returnValue = 1;
        }

        if( returnValue == 0)
        {
            if( o1 instanceof Number)
            {
                double d1, d2;
                d1 = ((Number)o1).doubleValue();
                d2 = ((Number)o2).doubleValue();

                if( d1 < d2 )
                {
                    returnValue = -1;
                }
                else if( d1 > d2 )
                {
                    returnValue = 1;
                }
                else
                {
                    returnValue = 0;
                }
            }
            else if( o1 instanceof Date)
            {
                long s1, s2;
                s1 = ((Date)o1).getTime();
                s2 = ((Date)o2).getTime();
                if( s1 < s2 )
                {
                    returnValue = -1;
                }
                else if( s1 > s2 )
                {
                    returnValue = 1;
                }
                else
                {
                    returnValue = 0;
                }
            }
            else
            {
                returnValue = o1.toString().compareTo( o2.toString());
            }
        }

        if( order == DESCENDING)
        {
            if( returnValue > 0)
            {
                return -1;
            }
            else if( returnValue < 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return returnValue;
        }
    }
}

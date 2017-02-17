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
package com.aspc.remote.task.extdbsync;

import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;

/**
 *
 *  <i>THREAD MODE: NON-MUTABLE</i>
 *
 *
 *  @author      Jason McGrath
 *  @since       9 March 2012
 */
public class TransactionAnalysis
{
    private HashMap<String, ArrayList<Long>>hintMap=HashMapFactory.create();
    private HashMap<Long, String>rowMap=HashMapFactory.create();

    public void add( final String className, final long rowID)
    {
        String key = className.toLowerCase();
        rowMap.put(rowID, key);
        ArrayList<Long> list =hintMap.get( key);
        if( list == null)
        {
            list = new ArrayList<>();

            hintMap.put( key, list);
        }

        list.add( rowID);
    }

    public void appendHint( final String className, final StringBuilder criteria)
    {
        String key = className.toLowerCase();
        ArrayList<Long> list =hintMap.get( key);

        if( list != null)
        {
            criteria.append( "\nHINT ");
            for( Long row: list)
            {
                criteria.append( row);
                criteria.append( ",");
            }
        }
    }

    public boolean interesting( long rowID)
    {
        if( rowMap.containsKey(rowID)) return true;

        return false;
    }

    /**
     * complete the analysis
     */
    public void complete()
    {

    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.task.extdbsync.TransactionAnalysis");//#LOGGER-NOPMD
}

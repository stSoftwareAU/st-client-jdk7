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
package com.aspc.remote.util.links;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  LinkType
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       18 June 1998
 */
public class LinkGroup
{
    private WeakReference<LinkType> types[];
    private final ReadWriteLock rw=new ReentrantReadWriteLock();
    
    /**
     * The code for this type
     */
    protected final String    code;
    
    /**
     * The minimum connections
     */
    protected int       maxConnections=-1;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.links.LinkGroup");//#LOGGER-NOPMD
    
    /**
     * Create a new connection group type
     * @param code 
     */
    public LinkGroup( final String code)
    {
        this.code = code;
    }

    /**
     * The maximum number of connections the Link manager will open.
     * @param max
     */
    public void setMaximumConnections( final int max)
    {
        maxConnections = max;
    }

    public void registerLink( final LinkType linkType)
    {
        Lock gate = rw.writeLock();
        gate.lock();
        try{
            ArrayList<WeakReference<LinkType>> list=new ArrayList<>();

            if( types != null)
            {
                for( WeakReference<LinkType> wr: types)
                {
                    LinkType tmpType = wr.get();

                    if( tmpType != null && tmpType != linkType)
                    {
                        list.add(wr);
                    }
                }
            }

            WeakReference<LinkType> wr = new WeakReference<>( linkType);

            list.add(wr);

            WeakReference<LinkType> tmpTypes[];
            tmpTypes= new WeakReference[list.size()];

            list.toArray( tmpTypes);

            types = tmpTypes;
        }
        finally
        {
            gate.unlock();
        }
    }

    /** 
     * exclude this type.
     * @return the value
     */
    public int calRemainingReserve( )
    {
        int tmpReserve = maxConnections;
        if( tmpReserve < 0) return tmpReserve;
        WeakReference<LinkType> tmpTypes[]=types;

        if( tmpTypes == null) return tmpReserve;

        for( WeakReference<LinkType> wr: types)
        {
            LinkType tmpType = wr.get();

            if( tmpType != null)
            {
               tmpReserve -= tmpType.countConnections();
            }
        }

        if( tmpReserve > 0 ) return tmpReserve;

        return 0;
    }

    /**
     * The maximum number of connections the Link manager will open.
     * @return the value
     */
    public int getMaximumConnections( )
    {
        return maxConnections;
    }
}

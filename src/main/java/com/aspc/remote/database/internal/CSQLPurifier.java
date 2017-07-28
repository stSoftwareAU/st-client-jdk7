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

import org.apache.commons.logging.Log;
import com.aspc.remote.database.*; 
import com.aspc.remote.util.misc.*; 
import java.util.HashMap;

/**
 *  Purify the database connections
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       19 October 2006
 */
public class CSQLPurifier implements ThreadPurifier
{
    private final ThreadLocal connections;
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.CSQLPurifier");//#LOGGER-NOPMD
    
    /**
     * clean up any database transactions that are open.
     * @param connections access to the private list of connections
     */
    public CSQLPurifier( final ThreadLocal connections)
    {
        this.connections = connections;        
    }
    
    /**
     * purify thread.
     */
    @Override
    public void purifyThread()
    {
        HashMap current = (HashMap)connections.get();
        
        if( current != null )
        {
            int size = current.size();
            
            if( size != 0)
            {
                DataBase list[] = new DataBase[size];
                
                current.keySet().toArray( list);
                
                for (DataBase db : list) {
                    LOGGER.warn( "thread {" + Thread.currentThread() + "} purify of database {" + db + "} connections");
                
                    CSQL sql = new CSQL( db);
                    
                    sql.rollback();
                }
            }
        }

        CSQL.clearThreadIDs();
    }        
} 

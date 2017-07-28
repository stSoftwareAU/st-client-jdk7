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
package com.aspc.remote.util.net;

/**
 *  NetUtil
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       August 9, 1999, 5:21 PM
 */
public interface NetClientPool
{
    /**
     * borrow a connection
     * @param url the url
     * @throws Exception a serious problem
     * @return the connection
     */
    Object borrowObject( Object url) throws Exception;
    /**
     * return a client
     * @param client a client
     * @throws Exception a serious problem
     */
    void returnObject( Object client) throws Exception;
    
    /**
     * invalidate a client
     * @param client the client to invalidate
     * @throws Exception a serious problem
     */
    void invalidateObject( Object client) throws Exception;
    
    /**
     * pool size
     * @return The size of the pool.
     * @throws Exception a serious problem
     */
    int size() throws Exception;
    
    /**
     * The number of objects that are currently checked out.
     * @return the borrowed object count.
     */
    public int borrowedObjectsCount();
}
